package dev.imagio.slot.client.screen;

import net.minecraft.network.chat.Component;

public final class SlotActionFeedbackState {
    private static final int DEFAULT_TICKS = 80;

    private Component message = Component.empty();
    private int color = 0xB0B0B0;
    private int ticksRemaining = 0;

    public void tick() {
        if (ticksRemaining > 0) {
            ticksRemaining--;
        }
    }

    public void show(SlotActionResult result) {
        if (result == null || !result.visible()) {
            return;
        }

        message = result.message();
        color = switch (result.status()) {
            case APPLIED -> 0xB8D0A0;
            case REQUESTED -> 0xA8C0D8;
            case BLOCKED -> 0xE0B080;
            case FAILED -> 0xD8A0A0;
            case NONE -> 0xB0B0B0;
        };
        ticksRemaining = DEFAULT_TICKS;
    }

    public boolean active() {
        return ticksRemaining > 0 && message != null && !message.getString().isBlank();
    }

    public Component message() {
        return message;
    }

    public int color() {
        return color;
    }
}
