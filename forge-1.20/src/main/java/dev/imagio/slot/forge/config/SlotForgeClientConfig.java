package dev.imagio.slot.forge.config;

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

    public static final class Client {
        public final ForgeConfigSpec.IntValue sidebarLeftMargin;
        public final ForgeConfigSpec.IntValue sidebarTopMargin;
        public final ForgeConfigSpec.IntValue sidebarBottomMargin;

        private Client(ForgeConfigSpec.Builder builder) {
            builder.comment("Client-side SLOT settings").push("client");

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

            builder.pop();
        }
    }
}
