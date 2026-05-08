package dev.imagio.slot.forge;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.forge.network.SlotForgeNetworking;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(SlotForge.MOD_ID)
public final class SlotForge {

    public static final String MOD_ID = SlotCommon.MOD_ID;

    public SlotForge(FMLJavaModLoadingContext context) {
        Forge120Platform.bootstrap();
        SlotForgeNetworking.register();
        SlotCommon.init();
        SlotCommon.LOGGER.info("SLOT (forge-1.20) loaded");
    }
}
