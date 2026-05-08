package dev.imagio.slot;

import com.mojang.logging.LogUtils;
import dev.imagio.slot.inventory.integration.InventoryIntegrationRegistry;
import dev.imagio.slot.inventory.integration.MenuBackedInventoryIntegrationProvider;
import dev.imagio.slot.inventory.integration.SophisticatedBackpackInventoryIntegrationProvider;
import dev.imagio.slot.inventory.integration.TomsStorageInventoryIntegrationProvider;
import dev.imagio.slot.inventory.integration.VanillaCraftingInventoryIntegrationProvider;
import dev.imagio.slot.inventory.storage.DefaultCarriedProviderIntegration;
import dev.imagio.slot.platform.SlotResourceAccess;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public final class SlotCommon {
    public static final String MOD_ID = "slot";
    public static final String MOD_NAME = "SLOT";
    public static final Logger LOGGER = LogUtils.getLogger();

    private SlotCommon() {
    }

    public static void init() {
        LOGGER.info("Initializing {}", MOD_NAME);
        InventoryIntegrationRegistry.clear();
        InventoryIntegrationRegistry.register(new MenuBackedInventoryIntegrationProvider());
        InventoryIntegrationRegistry.register(new VanillaCraftingInventoryIntegrationProvider());
        InventoryIntegrationRegistry.register(new TomsStorageInventoryIntegrationProvider());
        InventoryIntegrationRegistry.register(new SophisticatedBackpackInventoryIntegrationProvider());
        // Auto-synthesises PlayerInventoryExtensions from CarriedProviderRegistry
        // so minimal carried mods (Curios, future providers) get snapshot +
        // mutate routing for free. Providers that ship their own bespoke
        // InventoryIntegrationProvider opt out via CarriedProvider.autoSynthesizeExtension.
        InventoryIntegrationRegistry.register(new DefaultCarriedProviderIntegration());
        InventoryIntegrationRegistry.markBootstrapped();
    }

    public static ResourceLocation id(String path) {
        return SlotResourceAccess.current().id(MOD_ID, path);
    }
}
