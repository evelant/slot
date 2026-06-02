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
        WorkspaceProjectionRequest resolved = request == null
                ? new WorkspaceProjectionRequest(
                        null, null, "ready", "", 0, -1, 0,
                        null, null, null, null, null, null, "",
                        0L, null, null, null, null, null, null, null, null, null)
                : request;
        projectionCount++;
        WorkspaceProjectionFrame frame = resolved.frame();
        String structuralKey = WorkspaceProjectionFingerprint.inputKey(resolved, identityMemo);
        boolean structuralHit = lastStructuralView != null && structuralKey.equals(lastStructuralKey);
        SlotWorkspaceViewModel projected;
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
            projected = engine.project(resolved.withFrame(new WorkspaceProjectionFrame(
                    frame.status(),
                    frame.diagnostics(),
                    frame.pendingCount(),
                    frame.selectedQuickAccessSlot(),
                    0L)), identityMemo);
            lastStructuralKey = structuralKey;
            lastStructuralView = projected.withFrame(
                    0L,
                    frame.status(),
                    frame.diagnostics(),
                    frame.pendingCount(),
                    frame.selectedQuickAccessSlot());
        }

        String contentFingerprint = structuralHit && frame.equals(lastFrame) && lastView != null
                ? lastContentFingerprint
                : WorkspaceProjectionFingerprint.contentKey(projected);
        lastFrame = frame;
        lastView = projected;
        lastContentFingerprint = contentFingerprint;
        return new WorkspaceProjectionResult(projected, contentFingerprint, diagnostics(structuralHit));
    }

    public void clear() {
        lastStructuralKey = "";
        lastFrame = null;
        lastStructuralView = null;
        lastView = null;
        lastContentFingerprint = "";
    }

    public Diagnostics diagnostics() {
        return diagnostics(lastStructuralView != null && lastView != null);
    }

    private Diagnostics diagnostics(boolean structuralHit) {
        return new Diagnostics(
                projectionCount,
                structuralHits,
                structuralMisses,
                structuralHit,
                identityMemo.stats());
    }

    public record Diagnostics(
            long projectionCount,
            long structuralHits,
            long structuralMisses,
            boolean structuralCacheHit,
            ItemIdentityMatcher.MemoStats identityMemoStats
    ) {
        static Diagnostics empty() {
            return new Diagnostics(0L, 0L, 0L, false, new ItemIdentityMatcher.MemoStats(0L, 0L, 0L, 0L));
        }
    }
}
