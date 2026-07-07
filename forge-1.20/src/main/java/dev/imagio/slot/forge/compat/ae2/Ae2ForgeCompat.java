package dev.imagio.slot.forge.compat.ae2;

import dev.imagio.slot.inventory.integration.InventoryIntegrationRegistry;
import dev.imagio.slot.inventory.storage.WorldStorageAccess;
import net.minecraftforge.fml.ModList;

public final class Ae2ForgeCompat {
    private Ae2ForgeCompat() {
    }

    public static boolean loaded() {
        return ModList.get().isLoaded("ae2");
    }

    public static void registerWorldStorage(WorldStorageAccess worldStorage) {
        if (loaded() && worldStorage != null) {
            worldStorage.registerDelegate(new ForgeAe2WorldStorageDelegate());
        }
    }

    public static void registerInventoryIntegration() {
        if (loaded()) {
            InventoryIntegrationRegistry.register(new Ae2TerminalInventoryIntegrationProvider());
        }
    }
}
