package dev.imagio.slot.neoforge.screen.ldlib;

import dev.imagio.slot.atlas.CameraHistory;

import java.util.Optional;

final class AtlasCameraController {
    @FunctionalInterface
    interface Easing {
        float apply(float t);
    }

    static final Easing LINEAR = t -> t;
    static final Easing CUBIC_IN_OUT = t -> t < 0.5f
            ? 4f * t * t * t
            : 1f - (float) Math.pow(-2f * t + 2f, 3) / 2f;

    static final long PEEK_DURATION_MS = 800L;
    static final long COMMIT_DURATION_MS = 800L;
    static final long SEARCH_PREVIEW_DURATION_MS = 320L;
    // A typical human key-tap is ~100–200 ms, so 100 ms was too tight:
    // most "tap to goto" presses came back longer than the threshold and
    // were treated as a hold-then-snapback instead.
    static final long PEEK_TAP_THRESHOLD_MS = 250L;
    static final long PEEK_SNAPBACK_DURATION_MS = 450L;

    enum CommitSource {
        HOVER_GOTO,
        SEARCH_COMMIT,
        SEARCH_ENTER,
        PAN_TO_ISLAND,
        PAN_TO_CHEST,
        CHIP_ACCEPT,
        ISLAND_CREATE_FOCUS,
        REHOME_PICK
    }

    private final CameraHistory<AtlasCamera> history = new CameraHistory<>();
    private SlotAtlasGraphView graphView;
    private AtlasCamera animStart;
    private AtlasCamera animTarget;
    private long animStartMs;
    private long animDurationMs;
    private Easing animEasing = LINEAR;
    private boolean animating;
    private AtlasCamera origin;

    void attach(SlotAtlasGraphView view) {
        this.graphView = view;
    }

    SlotAtlasGraphView graphView() {
        return graphView;
    }

    boolean hasGraphView() {
        return graphView != null;
    }

    boolean isDragging() {
        return graphView != null
                && graphView.getModularUI() != null
                && graphView.getModularUI().getDragHandler() != null
                && graphView.getModularUI().getDragHandler().isDragging();
    }

    AtlasCamera currentCamera() {
        if (graphView == null) {
            return null;
        }
        return new AtlasCamera(graphView.getOffsetX(), graphView.getOffsetY(), graphView.getScale());
    }

    void ease(AtlasCamera target, Easing easing, long durationMs) {
        if (target == null || graphView == null) {
            return;
        }
        this.animStart = currentCamera();
        this.animTarget = target;
        this.animStartMs = System.currentTimeMillis();
        this.animDurationMs = Math.max(1L, durationMs);
        this.animEasing = easing != null ? easing : LINEAR;
        this.animating = true;
    }

    void snap(AtlasCamera target) {
        if (target == null || graphView == null) {
            return;
        }
        animating = false;
        animStart = null;
        animTarget = null;
        graphView.restoreCamera(target);
    }

    void commit(AtlasCamera target, CommitSource source, Easing easing, long durationMs) {
        if (target == null || graphView == null) {
            return;
        }
        history.recordCommit(currentCamera());
        ease(target, easing, durationMs);
    }

    void commitFrom(AtlasCamera origin, AtlasCamera target, CommitSource source, Easing easing, long durationMs) {
        if (target == null || graphView == null) {
            return;
        }
        if (origin != null) {
            history.recordCommit(origin);
        }
        ease(target, easing, durationMs);
    }

    boolean back() {
        if (graphView == null) {
            return false;
        }
        Optional<AtlasCamera> popped = history.back(currentCamera());
        if (popped.isEmpty()) {
            return false;
        }
        ease(popped.get(), CUBIC_IN_OUT, COMMIT_DURATION_MS);
        return true;
    }

    boolean forward() {
        if (graphView == null) {
            return false;
        }
        Optional<AtlasCamera> popped = history.forward(currentCamera());
        if (popped.isEmpty()) {
            return false;
        }
        ease(popped.get(), CUBIC_IN_OUT, COMMIT_DURATION_MS);
        return true;
    }

    void recordOrigin() {
        origin = currentCamera();
    }

    void clearOrigin() {
        origin = null;
    }

    AtlasCamera origin() {
        return origin;
    }

    CameraHistory<AtlasCamera> history() {
        return history;
    }

    boolean isAnimating() {
        return animating;
    }

    AtlasCamera animTarget() {
        return animTarget;
    }

    void tick() {
        if (!animating || animTarget == null || graphView == null) {
            return;
        }
        long now = System.currentTimeMillis();
        float t = Math.max(0f, Math.min(1f, (now - animStartMs) / (float) animDurationMs));
        float eased = animEasing.apply(t);
        AtlasCamera start = animStart != null ? animStart : animTarget;
        float viewW = graphView.getContentWidth();
        float viewH = graphView.getContentHeight();
        if (viewW <= 0f || viewH <= 0f) {
            return;
        }
        float startCenterX = start.offsetX() + viewW / (2f * start.scale());
        float startCenterY = start.offsetY() + viewH / (2f * start.scale());
        float endCenterX = animTarget.offsetX() + viewW / (2f * animTarget.scale());
        float endCenterY = animTarget.offsetY() + viewH / (2f * animTarget.scale());
        float centerX = startCenterX + (endCenterX - startCenterX) * eased;
        float centerY = startCenterY + (endCenterY - startCenterY) * eased;
        float startLog = (float) Math.log(Math.max(0.0001f, start.scale()));
        float endLog = (float) Math.log(Math.max(0.0001f, animTarget.scale()));
        float scale = (float) Math.exp(startLog + (endLog - startLog) * eased);
        float offsetX = centerX - viewW / (2f * scale);
        float offsetY = centerY - viewH / (2f * scale);
        graphView.restoreCamera(new AtlasCamera(offsetX, offsetY, scale));
        if (t >= 1f) {
            graphView.restoreCamera(animTarget);
            animating = false;
            animStart = null;
            animTarget = null;
        }
    }
}
