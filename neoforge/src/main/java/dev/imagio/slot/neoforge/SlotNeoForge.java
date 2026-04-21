package dev.imagio.slot.neoforge;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.neoforge.client.SlotNeoForgeClient;
import dev.imagio.slot.neoforge.command.SlotTestCommands;
import dev.imagio.slot.neoforge.config.SlotClientConfig;
import dev.imagio.slot.neoforge.network.SlotNetworking;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.neoforge.screen.ldlib.GhostAtlasStackFactory;
import dev.imagio.slot.neoforge.screen.ldlib.SlotWorkspaceLdlibMenus;
import dev.imagio.slot.neoforge.storage.ChestStorageBreakListener;
import dev.imagio.slot.neoforge.storage.SlotAttachmentTypes;
import dev.imagio.slot.neoforge.storage.SlotPickupRouter;
import dev.imagio.slot.neoforge.workflow.SlotPlayerWorkflowRuntimeService;
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
        SlotWorkspaceViewModel.setGhostStackResolver(GhostAtlasStackFactory::resolve);
        SlotWorkspaceLdlibMenus.init();
        SlotNetworking.init(modBus);
        SlotAttachmentTypes.register(modBus);
        SlotPlayerWorkflowRuntimeService.init();
        SlotTestCommands.init();
        ChestStorageBreakListener.init();
        SlotPickupRouter.init();

        if (dist == Dist.CLIENT) {
            SlotNeoForgeClient.init(modBus);
        }
    }
}
