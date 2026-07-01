package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.ItemIdentityMatcher;

public final class WorkspaceProjectionSessionCache {
    private final WorkspaceProjectionEngine engine;
    private final ItemIdentityMatcher.Memo identityMemo = new ItemIdentityMatcher.Memo();

    private String lastStructuralKey = "";
    private WorkspaceProjectionFrame lastFrame;
    private SlotWorkspaceViewModel lastStructuralView;
    private SlotWorkspaceViewModel lastView;
    private String lastContentFingerprint = "";
    private long projectionCount;
    private long structuralHits;
    private long structuralMisses;

    public WorkspaceProjectionSessionCache() {
        this(new WorkspaceProjectionEngine());
    }

    WorkspaceProjectionSessionCache(WorkspaceProjectionEngine engine) {
        this.engine = engine == null ? new WorkspaceProjectionEngine() : engine;
    }

    public WorkspaceProjectionResult project(WorkspaceProjectionRequest request) {
        long totalStart = System.nanoTime();
        WorkspaceProjectionRequest resolved = request == null
                ? new WorkspaceProjectionRequest(
                        null, null, "ready", "", 0, -1, 0,
                        null, null, null, null, null, null, "",
                        null, 0L, null, null, null, null, null, null, null, null, null)
                : request;
        projectionCount++;
        WorkspaceProjectionFrame frame = resolved.frame();
        long inputStart = System.nanoTime();
        String structuralKey = WorkspaceProjectionFingerprint.inputKey(resolved, identityMemo);
        long inputNanos = System.nanoTime() - inputStart;
        boolean structuralHit = lastStructuralView != null && structuralKey.equals(lastStructuralKey);
        SlotWorkspaceViewModel projected;
        long projectNanos = 0L;
        if (structuralHit) {
            structuralHits++;
            projected = lastStructuralView.withFrame(
                    frame.revision(),
                    frame.status(),
                    frame.diagnostics(),
                    frame.pendingCount(),
                    frame.selectedQuickAccessSlot());
        } else {
            structuralMisses++;
            long projectStart = System.nanoTime();
            projected = engine.project(resolved.withFrame(new WorkspaceProjectionFrame(
                    frame.status(),
                    frame.diagnostics(),
                    frame.pendingCount(),
                    frame.selectedQuickAccessSlot(),
                    0L)), identityMemo);
            projectNanos = System.nanoTime() - projectStart;
            lastStructuralKey = structuralKey;
            lastStructuralView = projected.withFrame(
                    0L,
                    frame.status(),
                    frame.diagnostics(),
                    frame.pendingCount(),
                    frame.selectedQuickAccessSlot());
        }

        long contentNanos = 0L;
        String contentFingerprint;
        if (structuralHit && frame.equals(lastFrame) && lastView != null) {
            contentFingerprint = lastContentFingerprint;
        } else {
            long contentStart = System.nanoTime();
            contentFingerprint = WorkspaceProjectionFingerprint.contentKey(projected);
            contentNanos = System.nanoTime() - contentStart;
        }
        lastFrame = frame;
        lastView = projected;
        lastContentFingerprint = contentFingerprint;
        WorkspaceProjectionTiming timing = new WorkspaceProjectionTiming(
                inputNanos,
                projectNanos,
                contentNanos,
                System.nanoTime() - totalStart);
        return new WorkspaceProjectionResult(projected, contentFingerprint, diagnostics(structuralHit, timing));
    }

    public void clear() {
        lastStructuralKey = "";
        lastFrame = null;
        lastStructuralView = null;
        lastView = null;
        lastContentFingerprint = "";
    }

    public Diagnostics diagnostics() {
        return diagnostics(lastStructuralView != null && lastView != null, WorkspaceProjectionTiming.empty());
    }

    private Diagnostics diagnostics(boolean structuralHit) {
        return diagnostics(structuralHit, WorkspaceProjectionTiming.empty());
    }

    private Diagnostics diagnostics(boolean structuralHit, WorkspaceProjectionTiming timing) {
        return new Diagnostics(
                projectionCount,
                structuralHits,
                structuralMisses,
                structuralHit,
                identityMemo.stats(),
                timing);
    }

    public record Diagnostics(
            long projectionCount,
            long structuralHits,
            long structuralMisses,
            boolean structuralCacheHit,
            ItemIdentityMatcher.MemoStats identityMemoStats,
            WorkspaceProjectionTiming timing
    ) {
        static Diagnostics empty() {
            return new Diagnostics(
                    0L,
                    0L,
                    0L,
                    false,
                    ItemIdentityMatcher.MemoStats.empty(),
                    WorkspaceProjectionTiming.empty());
        }
    }
}
