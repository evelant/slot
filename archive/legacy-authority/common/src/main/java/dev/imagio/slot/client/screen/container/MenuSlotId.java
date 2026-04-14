package dev.imagio.slot.client.screen.container;

public record MenuSlotId(int value) {
    public static final MenuSlotId INVALID = new MenuSlotId(-1);

    public static MenuSlotId of(int value) {
        return value >= 0 ? new MenuSlotId(value) : INVALID;
    }

    public boolean isValid() {
        return value >= 0;
    }

    public int orElse(int fallback) {
        return isValid() ? value : fallback;
    }
}
