package dev.imagio.slot.neoforge;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.neoforge.client.SlotNeoForgeClient;
import dev.imagio.slot.neoforge.command.SlotTestCommands;
import dev.imagio.slot.compat.sophisticated.SophisticatedBackpacksCarriedProvider;
import dev.imagio.slot.neoforge.config.SlotClientConfig;
import dev.imagio.slot.neoforge.network.SlotNetworking;
import dev.imagio.slot.inventory.storage.CarriedProviderRegistry;
import dev.imagio.slot.inventory.storage.StorageAccessRegistry;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.platform.SlotResourceAccess;
import dev.imagio.slot.platform.SlotStackAccess;
import dev.imagio.slot.neoforge.screen.ldlib.GhostAtlasStackFactory;
import dev.imagio.slot.neoforge.screen.ldlib.SlotSidebarUiHandles;
import dev.imagio.slot.neoforge.screen.ldlib.SlotWorkspaceLdlibMenus;
import dev.imagio.slot.neoforge.storage.ChestDepositObserver;
import dev.imagio.slot.neoforge.storage.ChestStorageBreakListener;
import dev.imagio.slot.neoforge.storage.NeoForgeCarriedActivityTracker;
import dev.imagio.slot.neoforge.storage.NeoForgeCarriedSourceAccess;
import dev.imagio.slot.neoforge.storage.NeoForgeWorldStorageAccess;
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
        SlotResourceAccess.install(new NeoForgeResourceAccess());
        SlotStackAccess.install(new NeoForgeStackAccess());
        SlotCommon.init();

        container.registerConfig(ModConfig.Type.CLIENT, SlotClientConfig.CLIENT_SPEC);
        SlotWorkspaceViewModel.setGhostStackResolver(GhostAtlasStackFactory::resolve);
        CarriedProviderRegistry.register(new SophisticatedBackpacksCarriedProvider());
        StorageAccessRegistry.installCarriedSourceAccess(new NeoForgeCarriedSourceAccess());
        StorageAccessRegistry.installWorldStorageAccess(new NeoForgeWorldStorageAccess());
        SlotWorkspaceLdlibMenus.init();
        SlotSidebarUiHandles.init();
        SlotNetworking.init(modBus);
        SlotAttachmentTypes.register(modBus);
        SlotPlayerWorkflowRuntimeService.init();
        SlotTestCommands.init();
        ChestStorageBreakListener.init();
        ChestDepositObserver.init();
        NeoForgeCarriedActivityTracker.init();
        SlotPickupRouter.init();

        if (dist == Dist.CLIENT) {
            SlotNeoForgeClient.init(modBus);
        }
    }
}
