package dev.imagio.slot.intent;

import java.util.Objects;
import java.util.UUID;

public record ActionRequestId(String value) {
    private static final ActionRequestId NONE = new ActionRequestId("");

    public ActionRequestId {
        Objects.requireNonNull(value, "value");
        value = value.trim();
    }

    public static ActionRequestId create() {
        return new ActionRequestId(UUID.randomUUID().toString());
    }

    public static ActionRequestId none() {
        return NONE;
    }

    public boolean present() {
        return !value.isBlank();
    }

    @Override
    public String toString() {
        return value;
    }
}
