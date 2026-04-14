package dev.imagio.slot.neoforge;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.neoforge.client.SlotNeoForgeClient;
import dev.imagio.slot.neoforge.config.SlotClientConfig;
import dev.imagio.slot.neoforge.network.SlotNetworking;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(SlotCommon.MOD_ID)
public final class SlotNeoForge {
    public SlotNeoForge(IEventBus modBus, Dist dist, ModContainer container) {
        SlotCommon.init();

        container.registerConfig(ModConfig.Type.CLIENT, SlotClientConfig.CLIENT_SPEC);
        SlotNetworking.init(modBus);

        if (dist == Dist.CLIENT) {
            SlotNeoForgeClient.init(modBus);
        }
    }
}
