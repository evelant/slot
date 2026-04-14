package dev.imagio.slot.client.screen;

import net.minecraft.network.chat.Component;

public record SlotActionResult(Status status, Component message, boolean transferSyncExpected) {
    public static final SlotActionResult NONE = new SlotActionResult(Status.NONE, Component.empty(), false);

    public enum Status {
        NONE,
        APPLIED,
        REQUESTED,
        BLOCKED,
        FAILED
    }

    public static SlotActionResult applied(Component message) {
        return new SlotActionResult(Status.APPLIED, message == null ? Component.empty() : message, false);
    }

    public static SlotActionResult requested(Component message) {
        return new SlotActionResult(Status.REQUESTED, message == null ? Component.empty() : message, true);
    }

    public static SlotActionResult blocked(Component message) {
        return new SlotActionResult(Status.BLOCKED, message == null ? Component.empty() : message, false);
    }

    public static SlotActionResult failed(Component message) {
        return new SlotActionResult(Status.FAILED, message == null ? Component.empty() : message, false);
    }

    public boolean successful() {
        return status == Status.APPLIED || status == Status.REQUESTED;
    }

    public boolean visible() {
        return status != Status.NONE && message != null && !message.getString().isBlank();
    }
}
