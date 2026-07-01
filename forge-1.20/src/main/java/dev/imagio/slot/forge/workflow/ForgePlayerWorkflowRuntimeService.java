package dev.imagio.slot.forge.workflow;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.forge.SlotForge;
import dev.imagio.slot.forge.storage.ForgeChestStorageIds;
import dev.imagio.slot.inventory.workspace.ChestClaimPersistenceReconciliation;
import dev.imagio.slot.inventory.workspace.ClaimedStorageBreakCleanup;
import dev.imagio.slot.inventory.workspace.WorkspaceStorageMemoryStore;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.InMemoryWorkflowDomainStateRepository;
import dev.imagio.slot.workflow.domain.WorkflowDomainPersistenceService;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import dev.imagio.slot.workflow.domain.persistence.WorkflowDomainFileStore;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = SlotForge.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ForgePlayerWorkflowRuntimeService {
    private static final int PENDING_SAVE_FLUSH_INTERVAL_TICKS = 20;
    private static final Map<RuntimeKey, WorkflowDomainRuntime> RUNTIMES = new LinkedHashMap<>();
    private static long lastPendingSaveFlushTick = -1L;

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

    public static int removeBrokenStorageAnchor(ServerPlayer player, UUID storageId, ChestAnchor anchor) {
        if (player == null) {
            return 0;
        }
        runtime(player);
        return removeBrokenStorageAnchor(player.serverLevel().getServer(), storageId, anchor);
    }

    public static int removeBrokenStorageAnchor(MinecraftServer server, UUID storageId, ChestAnchor anchor) {
        if (server == null || storageId == null || anchor == null) {
            return 0;
        }
        Path root = worldRoot(server);
        List<WorkflowDomainRuntime> runtimes = new ArrayList<>();
        synchronized (RUNTIMES) {
            for (Map.Entry<RuntimeKey, WorkflowDomainRuntime> entry : RUNTIMES.entrySet()) {
                if (entry.getKey().worldRoot().equals(root) && entry.getValue() != null) {
                    runtimes.add(entry.getValue());
                }
            }
        }
        int changed = 0;
        for (WorkflowDomainRuntime runtime : runtimes) {
            if (ClaimedStorageBreakCleanup.removeBrokenAnchor(server, runtime, storageId, anchor)) {
                changed++;
            }
        }
        if (changed == 0) {
            ClaimedStorageBreakCleanup.forgetRememberedContents(server, storageId);
        }
        return changed;
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
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.getServer() == null) {
            return;
        }
        flushPendingSaves(event.getServer().getTickCount());
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
            lastPendingSaveFlushTick = -1L;
            WorkspaceStorageMemoryStore.clearCachedStores();
        }
    }

    private static void flushPendingSaves(long serverTick) {
        if (lastPendingSaveFlushTick >= 0
                && serverTick - lastPendingSaveFlushTick < PENDING_SAVE_FLUSH_INTERVAL_TICKS) {
            return;
        }
        lastPendingSaveFlushTick = serverTick;
        List<WorkflowDomainRuntime> runtimes;
        synchronized (RUNTIMES) {
            if (RUNTIMES.isEmpty()) {
                return;
            }
            runtimes = new ArrayList<>(RUNTIMES.values());
        }
        for (WorkflowDomainRuntime runtime : runtimes) {
            if (runtime != null) {
                runtime.flushPendingSave();
            }
        }
    }

    private static Path path(MinecraftServer server, UUID playerId) {
        return worldRoot(server)
                .resolve("slot")
                .resolve("workflow")
                .resolve((playerId == null ? "unknown" : playerId.toString()) + ".json");
    }

    private static Path worldRoot(MinecraftServer server) {
        return server == null
                ? Path.of(".").toAbsolutePath().normalize()
                : server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize();
    }

    private record RuntimeKey(
            Path worldRoot,
            UUID playerId
    ) {
        private static RuntimeKey of(ServerPlayer player) {
            MinecraftServer server = player.serverLevel().getServer();
            return new RuntimeKey(ForgePlayerWorkflowRuntimeService.worldRoot(server), player.getUUID());
        }
    }
}
