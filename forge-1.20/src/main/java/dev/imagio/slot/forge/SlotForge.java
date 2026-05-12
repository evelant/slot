package dev.imagio.slot.forge;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.compat.sophisticated.SophisticatedBackpacksCarriedProvider;
import dev.imagio.slot.forge.client.SlotForgeConfigScreens;
import dev.imagio.slot.forge.compat.sacks.SacksNSuchCarriedProvider;
import dev.imagio.slot.forge.compat.sacks.SacksNSuchInventoryIntegrationProvider;
import dev.imagio.slot.forge.classification.Forge120ClassificationLayerReloadListener;
import dev.imagio.slot.forge.config.SlotForgeClientConfig;
import dev.imagio.slot.forge.network.SlotForgeNetworking;
import dev.imagio.slot.forge.storage.ForgeSlotPickupRouter;
import dev.imagio.slot.inventory.integration.InventoryIntegrationRegistry;
import dev.imagio.slot.inventory.storage.CarriedProviderRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(SlotForge.MOD_ID)
public final class SlotForge {

    public static final String MOD_ID = SlotCommon.MOD_ID;

    public SlotForge(FMLJavaModLoadingContext context) {
        context.registerConfig(ModConfig.Type.CLIENT, SlotForgeClientConfig.CLIENT_SPEC);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> SlotForgeConfigScreens.register(context));
        Forge120Platform.bootstrap();
        CarriedProviderRegistry.register(new SophisticatedBackpacksCarriedProvider());
        CarriedProviderRegistry.register(new SacksNSuchCarriedProvider());
        SlotForgeNetworking.register();
        SlotCommon.init();
        InventoryIntegrationRegistry.register(new SacksNSuchInventoryIntegrationProvider());
        Forge120ClassificationLayerReloadListener.init();
        ForgeSlotPickupRouter.init();
        SlotCommon.LOGGER.info("SLOT (forge-1.20) loaded");
    }
}
