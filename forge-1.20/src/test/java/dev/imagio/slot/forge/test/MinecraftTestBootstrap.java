package dev.imagio.slot.forge.test;

import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Bootstrap;

public final class MinecraftTestBootstrap {
    private static boolean bootstrapped;

    private MinecraftTestBootstrap() {
    }

    public static synchronized void bootstrapVanillaRegistries() throws ReflectiveOperationException {
        if (bootstrapped) {
            return;
        }
        SharedConstants.tryDetectVersion();
        // Plain JUnit does not have Forge's network event bus bootstrapped, so
        // avoid Bootstrap.bootStrap() and initialise just the vanilla registries.
        java.lang.reflect.Field bootstrapFlag = Bootstrap.class.getDeclaredField("isBootstrapped");
        bootstrapFlag.setAccessible(true);
        bootstrapFlag.setBoolean(null, true);
        try {
            BuiltInRegistries.bootStrap();
        } catch (IllegalStateException exception) {
            if (!alreadyBootstrapped(exception)) {
                throw exception;
            }
        }
        bootstrapped = true;
    }

    private static boolean alreadyBootstrapped(IllegalStateException exception) {
        String message = exception.getMessage();
        return message != null && message.contains("override already set default value");
    }
}
