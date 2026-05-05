package dev.imagio.slot.neoforge.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public final class SlotClientConfig {
    public static final Client CLIENT;
    public static final ModConfigSpec CLIENT_SPEC;

    static {
        Pair<Client, ModConfigSpec> clientSpec = new ModConfigSpec.Builder().configure(Client::new);
        CLIENT = clientSpec.getLeft();
        CLIENT_SPEC = clientSpec.getRight();
    }

    private SlotClientConfig() {
    }

    public static final class Client {
        public final ModConfigSpec.BooleanValue debugLogging;
        public final ModConfigSpec.BooleanValue verboseLogging;
        public final ModConfigSpec.BooleanValue slotEnabled;

        private Client(ModConfigSpec.Builder builder) {
            builder.comment("Client-side SLOT settings").push("client");

            debugLogging = builder
                    .translation("slot.config.debug_logging")
                    .comment("Logs SLOT-specific diagnostic events (screen mount, sidebar drag, status-line transitions, cross-surface RPCs). Default on during the prototype so issue reports include the trace; flip to false to silence.")
                    .define("debugLogging", true);

            verboseLogging = builder
                    .translation("slot.config.verbose_logging")
                    .comment("Adds steady-state per-tick / per-frame traces (identity resolution dump, chest locator query) on top of debugLogging. Off by default — these drown out higher-signal events when always on. Flip when triaging a specific projection / locator issue.")
                    .define("verboseLogging", false);

            slotEnabled = builder
                    .translation("slot.config.slot_enabled")
                    .comment(
                            "When false, SLOT does not intercept the inventory key — the vanilla",
                            "inventory screen opens instead. Toggle in-game via the Disable SLOT",
                            "icon in the atlas top-right, or the Re-enable SLOT pill on the",
                            "vanilla inventory screen."
                    )
                    .define("slotEnabled", true);

            builder.pop();
        }
    }
}
