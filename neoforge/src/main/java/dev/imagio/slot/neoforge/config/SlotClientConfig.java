package dev.imagio.slot.neoforge.config;

import dev.imagio.slot.ui.workspace.RecentsStripUiBuilder;
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
        public final ModConfigSpec.BooleanValue contextualSuggestionDebugTooltips;
        public final ModConfigSpec.IntValue sidebarLeftMargin;
        public final ModConfigSpec.IntValue sidebarTopMargin;
        public final ModConfigSpec.IntValue sidebarBottomMargin;
        public final ModConfigSpec.IntValue recentsHorizontalOffset;
        public final ModConfigSpec.IntValue recentsTopOffset;
        public final ModConfigSpec.IntValue craftRunRightMargin;
        public final ModConfigSpec.IntValue craftRunTopMargin;
        public final ModConfigSpec.IntValue craftRunBottomMargin;

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

            contextualSuggestionDebugTooltips = builder
                    .translation("slot.config.contextual_suggestion_debug_tooltips")
                    .comment("When true, Useful Now and Put Away item tooltips include contextual suggestion scores and reason tokens.")
                    .define("contextualSuggestionDebugTooltips", false);

            sidebarLeftMargin = builder
                    .translation("slot.config.sidebar_left_margin")
                    .comment("Horizontal screen-pixel gap before the SLOT sidebar. Increase this when a pack renders buttons at the top-left edge.")
                    .defineInRange("sidebarLeftMargin", 0, 0, 400);

            sidebarTopMargin = builder
                    .translation("slot.config.sidebar_top_margin")
                    .comment("Screen-pixel gap above the SLOT sidebar. Increase this when a pack renders buttons at the top-left edge.")
                    .defineInRange("sidebarTopMargin", 0, 0, 400);

            sidebarBottomMargin = builder
                    .translation("slot.config.sidebar_bottom_margin")
                    .comment("Screen-pixel gap below the SLOT sidebar. Increase this when recipe viewer controls sit at the bottom-left edge.")
                    .defineInRange("sidebarBottomMargin", 0, 0, 400);

            recentsHorizontalOffset = builder
                    .translation("slot.config.recents_horizontal_offset")
                    .comment("Screen-pixel offset from centered placement for the floating SLOT Recent strip. Negative moves left; positive moves right.")
                    .defineInRange(
                            "recentsHorizontalOffset",
                            RecentsStripUiBuilder.DEFAULT_HORIZONTAL_OFFSET_PX,
                            -400,
                            400);

            recentsTopOffset = builder
                    .translation("slot.config.recents_top_offset")
                    .comment("Screen-pixel gap above the floating SLOT Recent strip.")
                    .defineInRange(
                            "recentsTopOffset",
                            RecentsStripUiBuilder.DEFAULT_TOP_OFFSET_PX,
                            0,
                            400);

            craftRunRightMargin = builder
                    .translation("slot.config.craft_run_right_margin")
                    .comment("Screen-pixel gap between the right edge and the floating SLOT crafting run panel.")
                    .defineInRange("craftRunRightMargin", 0, 0, 400);

            craftRunTopMargin = builder
                    .translation("slot.config.craft_run_top_margin")
                    .comment("Screen-pixel gap above the floating SLOT crafting run panel.")
                    .defineInRange("craftRunTopMargin", 0, 0, 400);

            craftRunBottomMargin = builder
                    .translation("slot.config.craft_run_bottom_margin")
                    .comment("Screen-pixel gap below the floating SLOT crafting run panel.")
                    .defineInRange("craftRunBottomMargin", 0, 0, 400);

            builder.pop();
        }
    }
}
