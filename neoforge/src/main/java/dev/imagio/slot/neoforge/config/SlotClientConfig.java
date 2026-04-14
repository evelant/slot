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
        public final ModConfigSpec.BooleanValue enabled;
        public final ModConfigSpec.BooleanValue replacePlayerInventory;
        public final ModConfigSpec.BooleanValue replaceChestLikeStorage;
        public final ModConfigSpec.BooleanValue debugLogging;

        private Client(ModConfigSpec.Builder builder) {
            builder.comment("Client-side SLOT settings").push("client");

            enabled = builder
                    .translation("slot.config.enabled")
                    .comment("Master enable switch for SLOT.")
                    .define("enabled", true);

            replacePlayerInventory = builder
                    .translation("slot.config.replace_player_inventory")
                    .comment("Replaces the vanilla player inventory screen when supported. Disabled by default during the core authority cut.")
                    .define("replacePlayerInventory", false);

            replaceChestLikeStorage = builder
                    .translation("slot.config.replace_chest_like_storage")
                    .comment("Replaces supported chest-like storage screens. Disabled by default during the core authority cut.")
                    .define("replaceChestLikeStorage", false);

            debugLogging = builder
                    .translation("slot.config.debug_logging")
                    .comment("Logs screen-observation events during early development.")
                    .define("debugLogging", false);

            builder.pop();
        }
    }
}
