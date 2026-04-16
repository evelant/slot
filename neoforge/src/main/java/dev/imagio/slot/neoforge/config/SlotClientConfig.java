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

        private Client(ModConfigSpec.Builder builder) {
            builder.comment("Client-side SLOT settings").push("client");

            debugLogging = builder
                    .translation("slot.config.debug_logging")
                    .comment("Logs screen-observation events during early development.")
                    .define("debugLogging", false);

            builder.pop();
        }
    }
}
