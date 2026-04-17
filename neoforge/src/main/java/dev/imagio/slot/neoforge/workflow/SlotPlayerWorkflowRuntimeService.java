package dev.imagio.slot.neoforge.workflow;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.workflow.domain.InMemoryWorkflowDomainStateRepository;
import dev.imagio.slot.workflow.domain.WorkflowDomainPersistenceService;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import dev.imagio.slot.workflow.domain.persistence.WorkflowDomainFileStore;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class SlotPlayerWorkflowRuntimeService {
    private static final Map<RuntimeKey, WorkflowDomainRuntime> RUNTIMES = new LinkedHashMap<>();
    private static boolean registered;

    private SlotPlayerWorkflowRuntimeService() {
    }

    public static void init() {
        if (registered) {
            return;
        }
        NeoForge.EVENT_BUS.addListener(SlotPlayerWorkflowRuntimeService::onPlayerLoggedOut);
        NeoForge.EVENT_BUS.addListener(SlotPlayerWorkflowRuntimeService::onServerStopping);
        registered = true;
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
        SlotCommon.LOGGER.info("[SLOT] Loaded workflow runtime for {}", player.getScoreboardName());
        return new WorkflowDomainRuntime(repository, persistence);
    }

    private static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
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

    private static void onServerStopping(ServerStoppingEvent event) {
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
