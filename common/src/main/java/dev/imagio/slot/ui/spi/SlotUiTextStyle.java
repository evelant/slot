package dev.imagio.slot.ui.spi;

public final class SlotUiTextStyle {
    public enum Horizontal {
        LEFT,
        CENTER,
        RIGHT
    }

    public enum Vertical {
        TOP,
        CENTER,
        BOTTOM
    }

    private int color = 0xFFFFFFFF;
    private float fontSize = 8;
    private boolean shadow;
    private boolean adaptiveWidth;
    private Horizontal horizontal = Horizontal.LEFT;
    private Vertical vertical = Vertical.CENTER;

    public SlotUiTextStyle color(int value) {
        this.color = value;
        return this;
    }

    public SlotUiTextStyle fontSize(float value) {
        this.fontSize = value;
        return this;
    }

    public SlotUiTextStyle shadow(boolean value) {
        this.shadow = value;
        return this;
    }

    public SlotUiTextStyle adaptiveWidth(boolean value) {
        this.adaptiveWidth = value;
        return this;
    }

    public SlotUiTextStyle horizontal(Horizontal value) {
        this.horizontal = value == null ? Horizontal.LEFT : value;
        return this;
    }

    public SlotUiTextStyle vertical(Vertical value) {
        this.vertical = value == null ? Vertical.CENTER : value;
        return this;
    }

    public int color() {
        return color;
    }

    public float fontSize() {
        return fontSize;
    }

    public boolean shadow() {
        return shadow;
    }

    public boolean adaptiveWidth() {
        return adaptiveWidth;
    }

    public Horizontal horizontal() {
        return horizontal;
    }

    public Vertical vertical() {
        return vertical;
    }
}
