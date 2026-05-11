package dev.imagio.slot.ui.spi;

public final class SlotUiEvent {
    private final SlotUiEventKind kind;
    private final int button;
    private final float x;
    private final float y;
    private final boolean shiftDown;
    private final boolean controlDown;
    private final float wheelDelta;
    private boolean propagationStopped;

    public SlotUiEvent(SlotUiEventKind kind, int button, float x, float y, boolean shiftDown) {
        this(kind, button, x, y, shiftDown, false, 0f);
    }

    public SlotUiEvent(SlotUiEventKind kind, int button, float x, float y, boolean shiftDown, float wheelDelta) {
        this(kind, button, x, y, shiftDown, false, wheelDelta);
    }

    public SlotUiEvent(
            SlotUiEventKind kind,
            int button,
            float x,
            float y,
            boolean shiftDown,
            boolean controlDown,
            float wheelDelta
    ) {
        this.kind = kind == null ? SlotUiEventKind.MOUSE_DOWN : kind;
        this.button = button;
        this.x = x;
        this.y = y;
        this.shiftDown = shiftDown;
        this.controlDown = controlDown;
        this.wheelDelta = wheelDelta;
    }

    public SlotUiEventKind kind() {
        return kind;
    }

    public int button() {
        return button;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public boolean shiftDown() {
        return shiftDown;
    }

    public boolean controlDown() {
        return controlDown;
    }

    public float wheelDelta() {
        return wheelDelta;
    }

    public void stopPropagation() {
        propagationStopped = true;
    }

    public boolean propagationStopped() {
        return propagationStopped;
    }
}
