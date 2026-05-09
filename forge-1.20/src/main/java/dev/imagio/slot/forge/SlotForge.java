package dev.imagio.slot.forge;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.compat.sophisticated.SophisticatedBackpacksCarriedProvider;
import dev.imagio.slot.forge.classification.Forge120ClassificationLayerReloadListener;
import dev.imagio.slot.forge.network.SlotForgeNetworking;
import dev.imagio.slot.forge.storage.ForgeSlotPickupRouter;
import dev.imagio.slot.inventory.storage.CarriedProviderRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(SlotForge.MOD_ID)
public final class SlotForge {

    public static final String MOD_ID = SlotCommon.MOD_ID;

    public SlotForge(FMLJavaModLoadingContext context) {
        Forge120Platform.bootstrap();
        CarriedProviderRegistry.register(new SophisticatedBackpacksCarriedProvider());
        SlotForgeNetworking.register();
        SlotCommon.init();
        Forge120ClassificationLayerReloadListener.init();
        ForgeSlotPickupRouter.init();
        SlotCommon.LOGGER.info("SLOT (forge-1.20) loaded");
    }
}
