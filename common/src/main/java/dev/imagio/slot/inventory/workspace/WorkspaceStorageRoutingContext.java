package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.storage.StorageAccessRegistry;
import dev.imagio.slot.inventory.storage.WorldDisplayStorageSource;
import dev.imagio.slot.inventory.storage.WorldStorageAccess;
import dev.imagio.slot.workflow.domain.ChestAffinityMap;
import dev.imagio.slot.workflow.domain.ClaimedChestMap;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import dev.imagio.slot.workflow.domain.WorkflowDomainSnapshot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * One request-scoped storage read model for workspace routing.
 *
 * <p>Callers should build this once per server refresh or command and share
 * its proximate ids, display sources, live storage index, and decayed affinity
 * map with previews and mutations. That keeps "what the UI highlights" and
 * "what the server can mutate" on the same storage/proximity rules.
 */
public record WorkspaceStorageRoutingContext(
        ClaimedChestMap claimedChestMap,
        Set<String> proximateStorageIds,
        Set<String> contextualSuggestionStorageIds,
        List<WorldDisplayStorageSource> displaySources,
        WorkspaceStorageIndex storageIndex,
        ChestAffinityMap affinityMap,
        long tick,
        WorkspaceStorageIndexCache.Diagnostics indexDiagnostics
) {
    public WorkspaceStorageRoutingContext {
        claimedChestMap = claimedChestMap == null ? ClaimedChestMap.empty() : claimedChestMap;
        proximateStorageIds = proximateStorageIds == null ? Set.of() : Set.copyOf(proximateStorageIds);
        contextualSuggestionStorageIds = contextualSuggestionStorageIds == null
                ? Set.of()
                : Set.copyOf(contextualSuggestionStorageIds);
        displaySources = displaySources == null ? List.of() : List.copyOf(displaySources);
        storageIndex = storageIndex == null ? WorkspaceStorageIndex.empty() : storageIndex;
        affinityMap = affinityMap == null ? ChestAffinityMap.empty() : affinityMap;
        tick = Math.max(0L, tick);
        indexDiagnostics = indexDiagnostics == null
                ? WorkspaceStorageIndexCache.Diagnostics.empty()
                : indexDiagnostics;
    }

    public static WorkspaceStorageRoutingContext build(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            InventoryAuthoritySnapshot authority
    ) {
        return build(player, runtime, authority, null);
    }

    public static WorkspaceStorageRoutingContext build(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            InventoryAuthoritySnapshot authority,
            WorkspaceStorageIndexCache storageIndexCache
    ) {
        return build(player, runtime, authority, storageIndexCache, null);
    }

    public static WorkspaceStorageRoutingContext build(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            InventoryAuthoritySnapshot authority,
            WorkspaceStorageIndexCache storageIndexCache,
            InventoryHostDescriptor host
    ) {
        WorkflowDomainSnapshot snapshot = runtime == null ? WorkflowDomainSnapshot.empty() : runtime.snapshot();
        ClaimedChestMap claimedChestMap = runtime == null
                ? snapshot.claimedChestMap()
                : runtime.chestClaimWorkflow().claimedChestMap();
        long tick = player == null ? 0L : player.serverLevel().getGameTime();
        MinecraftServer server = player == null ? null : player.getServer();
        WorldStorageAccess worldStorage = StorageAccessRegistry.isInstalled()
                ? StorageAccessRegistry.worldStorageAccess()
                : null;
        Set<String> proximate = WorkspaceChestProjectionSupport.proximateStorageIds(player, claimedChestMap);
        Set<String> contextual = WorkspaceChestProjectionSupport.proximateStorageIds(
                player,
                claimedChestMap,
                WorkspaceChestProjectionSupport.CONTEXTUAL_SUGGESTION_RADIUS_BLOCKS);
        List<WorldDisplayStorageSource> displaySources =
                WorkspaceChestProjectionSupport.proximateDisplaySources(player, worldStorage);
        List<WorldDisplayStorageSource> observedSources = mergeDisplaySources(
                displaySources,
                host == null ? List.of() : host.hostSession().observedWorldStorageSources(player, host));
        InventoryAuthoritySnapshot resolvedAuthority = authority == null
                ? InventoryAuthoritySnapshot.empty()
                : authority;
        WorkspaceStorageIndex storageIndex = storageIndexCache == null
                ? WorkspaceStorageIndex.build(
                        server,
                        resolvedAuthority,
                        snapshot,
                        worldStorage,
                        proximate,
                        observedSources,
                        tick)
                : storageIndexCache.build(
                        server,
                        resolvedAuthority,
                        snapshot,
                        worldStorage,
                        proximate,
                        observedSources,
                        tick);
        return new WorkspaceStorageRoutingContext(
                claimedChestMap,
                proximate,
                contextual,
                storageIndex.displaySources(),
                storageIndex,
                snapshot.chestAffinityMap().decayed(tick),
                tick,
                storageIndexCache == null
                        ? WorkspaceStorageIndexCache.Diagnostics.empty()
                        : storageIndexCache.diagnostics());
    }

    private static List<WorldDisplayStorageSource> mergeDisplaySources(
            List<WorldDisplayStorageSource> primary,
            List<WorldDisplayStorageSource> secondary
    ) {
        LinkedHashMap<String, WorldDisplayStorageSource> byId = new LinkedHashMap<>();
        if (primary != null) {
            for (WorldDisplayStorageSource source : primary) {
                if (source != null && !source.storageId().isBlank()) {
                    byId.put(source.storageId(), source);
                }
            }
        }
        if (secondary != null) {
            for (WorldDisplayStorageSource source : secondary) {
                if (source != null && !source.storageId().isBlank()) {
                    byId.putIfAbsent(source.storageId(), source);
                }
            }
        }
        return byId.isEmpty() ? List.of() : List.copyOf(byId.values());
    }

    public boolean hasNearbyClaimedOrDisplayStorage() {
        return !proximateStorageIds.isEmpty()
                || displaySources.stream().anyMatch(source -> source != null && !source.contents().isEmpty());
    }

    public boolean hasDisplayDepositTarget() {
        return displaySources.stream().anyMatch(source -> source != null && source.depositTarget());
    }

    public DepositPlanner.ChestContentPresence liveChestContentPresence() {
        return storageIndex.liveChestContentPresence();
    }

    public DepositPlanner.ChestEligibility liveStorageAffinityEligibility() {
        return storageIndex.liveStorageAffinityEligibility();
    }
}
