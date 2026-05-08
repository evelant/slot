package dev.imagio.slot.forge.workflow;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.forge.SlotForge;
import dev.imagio.slot.forge.storage.ForgeChestStorageIds;
import dev.imagio.slot.inventory.workspace.ChestClaimPersistenceReconciliation;
import dev.imagio.slot.workflow.domain.InMemoryWorkflowDomainStateRepository;
import dev.imagio.slot.workflow.domain.WorkflowDomainPersistenceService;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import dev.imagio.slot.workflow.domain.persistence.WorkflowDomainFileStore;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = SlotForge.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ForgePlayerWorkflowRuntimeService {
    private static final Map<RuntimeKey, WorkflowDomainRuntime> RUNTIMES = new LinkedHashMap<>();

    private ForgePlayerWorkflowRuntimeService() {
    }

    public static WorkflowDomainRuntime runtime(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        RuntimeKey key = RuntimeKey.of(player);
        synchronized (RUNTIMES) {
            WorkflowDomainRuntime existing = RUNTIMES.get(key);
            if (existing != null) {
                return existing;
            }
            WorkflowDomainRuntime created = createRuntime(player);
            RUNTIMES.put(key, created);
            return created;
        }
    }

    private static WorkflowDomainRuntime createRuntime(ServerPlayer player) {
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        MinecraftServer server = player.serverLevel().getServer();
        if (server == null) {
            return new WorkflowDomainRuntime(repository, null);
        }
        WorkflowDomainPersistenceService persistence = new WorkflowDomainPersistenceService(
                new WorkflowDomainFileStore(path(server, player.getUUID()))
        );
        persistence.loadInto(repository);
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(repository, persistence);
        ChestClaimPersistenceReconciliation.reconcile(server, runtime, ForgeChestStorageIds::read);
        SlotCommon.LOGGER.info("[SLOT] Loaded Forge workflow runtime for {}", player.getScoreboardName());
        return runtime;
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        RuntimeKey key = RuntimeKey.of(player);
        WorkflowDomainRuntime runtime;
        synchronized (RUNTIMES) {
            runtime = RUNTIMES.remove(key);
        }
        if (runtime != null) {
            runtime.saveNow();
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        synchronized (RUNTIMES) {
            for (WorkflowDomainRuntime runtime : RUNTIMES.values()) {
                if (runtime != null) {
                    runtime.saveNow();
                }
            }
            RUNTIMES.clear();
        }
    }

    private static Path path(MinecraftServer server, UUID playerId) {
        return server.getWorldPath(LevelResource.ROOT)
                .resolve("slot")
                .resolve("workflow")
                .resolve((playerId == null ? "unknown" : playerId.toString()) + ".json");
    }

    private record RuntimeKey(
            Path worldRoot,
            UUID playerId
    ) {
        private static RuntimeKey of(ServerPlayer player) {
            MinecraftServer server = player.serverLevel().getServer();
            Path root = server == null ? Path.of(".") : server.getWorldPath(LevelResource.ROOT);
            return new RuntimeKey(root.toAbsolutePath().normalize(), player.getUUID());
        }
    }
}
