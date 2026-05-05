package dev.imagio.slot;

import java.util.Objects;
import java.util.function.BooleanSupplier;

public final class SlotDebugLog {
    // Default off pre-bootstrap: the production supplier (wired in
    // SlotNeoForgeClient.onClientSetup) reads the client config which
    // ships with debugLogging=true, so end users see SLOT diagnostics
    // by default. The in-memory default stays false so common-module
    // tests — which run without Minecraft on the classpath — don't
    // class-load SlotCommon.LOGGER (LogUtils is Minecraft-only) when
    // production code paths reach SlotDebugLog.log via the diagnostics
    // helpers.
    private static BooleanSupplier enabledSupplier = () -> false;
    // Verbose: opt-in second tier for steady-state spam (per-tick
    // identity dumps, per-frame chest locator traces). Off by default
    // even when enabledSupplier is on — the standard {@link #log}
    // channel stays usable for high-signal events without drowning in
    // these.
    private static BooleanSupplier verboseSupplier = () -> false;

    private SlotDebugLog() {
    }

    public static void setEnabledSupplier(BooleanSupplier enabledSupplier) {
        SlotDebugLog.enabledSupplier = Objects.requireNonNullElse(enabledSupplier, () -> false);
    }

    public static void setVerboseSupplier(BooleanSupplier verboseSupplier) {
        SlotDebugLog.verboseSupplier = Objects.requireNonNullElse(verboseSupplier, () -> false);
    }

    public static boolean enabled() {
        return enabledSupplier.getAsBoolean();
    }

    public static boolean verbose() {
        return enabledSupplier.getAsBoolean() && verboseSupplier.getAsBoolean();
    }

    public static void log(String message, Object... args) {
        if (enabled()) {
            SlotCommon.LOGGER.info("[SLOT] " + message, args);
        }
    }

    /**
     * Verbose log — same as {@link #log} but only fires when both the
     * regular debug flag AND the verbose opt-in are set. Use for
     * steady-state per-tick / per-frame traces that drown out
     * higher-signal events when always on.
     */
    public static void verboseLog(String message, Object... args) {
        if (verbose()) {
            SlotCommon.LOGGER.info("[SLOT] " + message, args);
        }
    }
}
