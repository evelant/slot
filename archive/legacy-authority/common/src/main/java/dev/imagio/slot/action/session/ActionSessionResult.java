package dev.imagio.slot.action.session;

import net.minecraft.network.chat.Component;

public record ActionSessionResult(Status status, Component message, boolean transferSyncExpected) {
    public static final ActionSessionResult NONE = new ActionSessionResult(Status.NONE, Component.empty(), false);

    public enum Status {
        NONE,
        APPLIED,
        REQUESTED,
        BLOCKED,
        FAILED
    }

    public static ActionSessionResult applied(Component message) {
        return new ActionSessionResult(Status.APPLIED, message == null ? Component.empty() : message, false);
    }

    public static ActionSessionResult requested(Component message) {
        return new ActionSessionResult(Status.REQUESTED, message == null ? Component.empty() : message, true);
    }

    public static ActionSessionResult blocked(Component message) {
        return new ActionSessionResult(Status.BLOCKED, message == null ? Component.empty() : message, false);
    }

    public static ActionSessionResult failed(Component message) {
        return new ActionSessionResult(Status.FAILED, message == null ? Component.empty() : message, false);
    }

    public boolean successful() {
        return status == Status.APPLIED || status == Status.REQUESTED;
    }

    public boolean visible() {
        return status != Status.NONE && message != null && !message.getString().isBlank();
    }
}
