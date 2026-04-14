package dev.imagio.slot.neoforge.client;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.inventory.action.InventoryActionRequestPayload;
import dev.imagio.slot.inventory.integration.InventoryHostContext;
import dev.imagio.slot.inventory.integration.InventoryHostResolver;
import dev.imagio.slot.inventory.query.InventoryAuthorityReadService;
import dev.imagio.slot.inventory.session.InventoryIntentRouter;
import dev.imagio.slot.inventory.session.InventorySessionCoordinator;
import dev.imagio.slot.inventory.session.InventorySessionSource;
import dev.imagio.slot.neoforge.config.SlotClientConfig;
import dev.imagio.slot.neoforge.persistence.WorkflowDomainFileStore;
import dev.imagio.slot.workflow.domain.InMemoryWorkflowDomainStateRepository;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import dev.imagio.slot.workflow.domain.WorkflowDomainPersistenceService;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.network.PacketDistributor;

import java.nio.file.Path;

public final class SlotNeoForgeClient {
    private static WorkflowDomainRuntime workflowRuntime;
    private static InventorySessionCoordinator sessionCoordinator;
    private static InventoryIntentRouter intentRouter;
    private static boolean shutdownHookRegistered;

    private SlotNeoForgeClient() {
    }

    public static void init(IEventBus modBus) {
        SlotDebugLog.setEnabledSupplier(() -> SlotClientConfig.CLIENT.debugLogging.get());
        InMemoryWorkflowDomainStateRepository workflowStateRepository = new InMemoryWorkflowDomainStateRepository();
        WorkflowDomainPersistenceService workflowPersistenceService = new WorkflowDomainPersistenceService(new WorkflowDomainFileStore(defaultWorkflowStatePath()));
        workflowPersistenceService.loadInto(workflowStateRepository);
        workflowRuntime = new WorkflowDomainRuntime(workflowStateRepository, workflowPersistenceService);
        sessionCoordinator = new InventorySessionCoordinator(
                new ClientInventorySessionSource(),
                workflowRuntime,
                request -> PacketDistributor.sendToServer(new InventoryActionRequestPayload(request))
        );
        intentRouter = new InventoryIntentRouter(sessionCoordinator);
        registerShutdownHook();
    }

    public static WorkflowDomainRuntime workflowRuntime() {
        return workflowRuntime;
    }

    public static InventorySessionCoordinator sessionCoordinator() {
        return sessionCoordinator;
    }

    public static InventoryIntentRouter intentRouter() {
        return intentRouter;
    }

    public static void saveWorkflowState() {
        if (workflowRuntime != null) {
            workflowRuntime.saveNow();
        }
    }

    private static void registerShutdownHook() {
        if (shutdownHookRegistered) {
            return;
        }
        Runtime.getRuntime().addShutdownHook(new Thread(SlotNeoForgeClient::saveWorkflowState, "slot-workflow-state-save"));
        shutdownHookRegistered = true;
    }

    private static Path defaultWorkflowStatePath() {
        return FMLPaths.CONFIGDIR.get().resolve("slot-workflow-state.json");
    }

    private static final class ClientInventorySessionSource implements InventorySessionSource {
        @Override
        public dev.imagio.slot.inventory.core.InventoryHostDescriptor resolveHost() {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft == null || minecraft.player == null || minecraft.player.containerMenu == null) {
                return null;
            }
            return InventoryHostResolver.resolve(new InventoryHostContext(
                    minecraft.player.containerMenu,
                    minecraft.player.getInventory(),
                    Component.empty(),
                    "",
                    false,
                    true,
                    false
            ));
        }

        @Override
        public dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot readAuthority(
                dev.imagio.slot.inventory.core.InventoryHostDescriptor host
        ) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft == null || minecraft.player == null || host == null) {
                return dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot.empty();
            }
            return InventoryAuthorityReadService.clientAuthority(minecraft.player, host);
        }
    }
}
