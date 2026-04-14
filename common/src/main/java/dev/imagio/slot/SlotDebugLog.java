package dev.imagio.slot;

import java.util.Objects;
import java.util.function.BooleanSupplier;

public final class SlotDebugLog {
    private static BooleanSupplier enabledSupplier = () -> false;

    private SlotDebugLog() {
    }

    public static void setEnabledSupplier(BooleanSupplier enabledSupplier) {
        SlotDebugLog.enabledSupplier = Objects.requireNonNullElse(enabledSupplier, () -> false);
    }

    public static boolean enabled() {
        return enabledSupplier.getAsBoolean();
    }

    public static void log(String message, Object... args) {
        if (enabled()) {
            SlotCommon.LOGGER.info("[SLOT] " + message, args);
        }
    }
}
