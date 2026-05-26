package dev.imagio.slot.forge.config;

import dev.imagio.slot.ui.workspace.RecentsStripUiBuilder;
import net.minecraftforge.common.ForgeConfigSpec;

public final class SlotForgeClientConfig {
    public static final Client CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        CLIENT = new Client(builder);
        CLIENT_SPEC = builder.build();
    }

    private SlotForgeClientConfig() {
    }

    public static int sidebarLeftMargin() {
        return CLIENT.sidebarLeftMargin.get();
    }

    public static int sidebarTopMargin() {
        return CLIENT.sidebarTopMargin.get();
    }

    public static int sidebarBottomMargin() {
        return CLIENT.sidebarBottomMargin.get();
    }

    public static int recentsHorizontalOffset() {
        return CLIENT.recentsHorizontalOffset.get();
    }

    public static int recentsTopOffset() {
        return CLIENT.recentsTopOffset.get();
    }

    public static int craftRunRightMargin() {
        return CLIENT.craftRunRightMargin.get();
    }

    public static int craftRunTopMargin() {
        return CLIENT.craftRunTopMargin.get();
    }

    public static int craftRunBottomMargin() {
        return CLIENT.craftRunBottomMargin.get();
    }

    public static boolean contextualSuggestionDebugTooltips() {
        return CLIENT.contextualSuggestionDebugTooltips.get();
    }

    public static final class Client {
        public final ForgeConfigSpec.BooleanValue contextualSuggestionDebugTooltips;
        public final ForgeConfigSpec.IntValue sidebarLeftMargin;
        public final ForgeConfigSpec.IntValue sidebarTopMargin;
        public final ForgeConfigSpec.IntValue sidebarBottomMargin;
        public final ForgeConfigSpec.IntValue recentsHorizontalOffset;
        public final ForgeConfigSpec.IntValue recentsTopOffset;
        public final ForgeConfigSpec.IntValue craftRunRightMargin;
        public final ForgeConfigSpec.IntValue craftRunTopMargin;
        public final ForgeConfigSpec.IntValue craftRunBottomMargin;

        private Client(ForgeConfigSpec.Builder builder) {
            builder.comment("Client-side SLOT settings").push("client");

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
