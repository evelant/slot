package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.storage.WorldDisplayStorageSource;
import dev.imagio.slot.inventory.triage.IslandSignalDescriptor;
import dev.imagio.slot.inventory.triage.LearnedIslandRuleStore;
import dev.imagio.slot.workflow.domain.WorkflowDomainSnapshot;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Common, loader-neutral input envelope for workspace projection.
 *
 * <p>Mutation paths still read live authority/storage. This request only
 * describes the read model a server session wants projected for the UI.
 */
public record WorkspaceProjectionRequest(
        InventoryAuthoritySnapshot authority,
        WorkflowDomainSnapshot workflow,
        String status,
        String diagnostics,
        int pendingCount,
        int selectedQuickAccessSlot,
        long revision,
        LearnedIslandRuleStore learnedRules,
        Function<ItemStack, IslandSignalDescriptor> signalExtractor,
        Function<String, SlotWorkspaceViewModel.ChestContentsSnapshot> chestContentsResolver,
        Set<String> proximateStorageIds,
        Function<ItemIdentity, SlotWorkspaceViewModel.CarriedContainerInfo> carriedContainerInfoResolver,
        SlotWorkspaceViewModel.LootChestSource lootChestSource,
        String searchQuery,
        long currentTick,
        SlotWorkspaceViewModel.ActiveChestPanel activeChestPanel,
        List<WorldDisplayStorageSource> worldDisplaySources,
        Set<String> contextualSuggestionStorageIds,
        List<WorldDisplayStorageSource> contextualSuggestionDisplaySources,
        Collection<WorkspaceStorageIndex.StorageEntry> trackedDisplayStorageEntries,
        Set<String> depositEligibleStorageIds,
        WorkspaceStorageIndex storageIndex,
        DepositPlanner.ChestContentPresence liveChestContentPresence,
        DepositPlanner.ChestEligibility liveStorageAffinityEligibility
) {
    public WorkspaceProjectionRequest {
        authority = authority == null ? InventoryAuthoritySnapshot.empty() : authority;
        workflow = workflow == null ? WorkflowDomainSnapshot.empty() : workflow;
        status = status == null || status.isBlank() ? "ready" : status;
        diagnostics = diagnostics == null ? "" : diagnostics;
        pendingCount = Math.max(0, pendingCount);
        proximateStorageIds = proximateStorageIds == null ? Set.of() : Set.copyOf(proximateStorageIds);
        searchQuery = searchQuery == null ? "" : searchQuery;
        currentTick = Math.max(0L, currentTick);
        activeChestPanel = activeChestPanel == null ? SlotWorkspaceViewModel.ActiveChestPanel.empty() : activeChestPanel;
        worldDisplaySources = worldDisplaySources == null ? List.of() : List.copyOf(worldDisplaySources);
        contextualSuggestionStorageIds = contextualSuggestionStorageIds == null
                ? Set.of()
                : Set.copyOf(contextualSuggestionStorageIds);
        contextualSuggestionDisplaySources = contextualSuggestionDisplaySources == null
                ? List.of()
                : List.copyOf(contextualSuggestionDisplaySources);
        trackedDisplayStorageEntries = trackedDisplayStorageEntries == null
                ? List.of()
                : List.copyOf(trackedDisplayStorageEntries);
        depositEligibleStorageIds = depositEligibleStorageIds == null ? Set.of() : Set.copyOf(depositEligibleStorageIds);
        storageIndex = storageIndex == null ? WorkspaceStorageIndex.empty() : storageIndex;
    }

    WorkspaceProjectionFrame frame() {
        return new WorkspaceProjectionFrame(status, diagnostics, pendingCount, selectedQuickAccessSlot, revision);
    }

    WorkspaceProjectionRequest withFrame(WorkspaceProjectionFrame frame) {
        WorkspaceProjectionFrame resolved = frame == null ? this.frame() : frame;
        return new WorkspaceProjectionRequest(
                authority,
                workflow,
                resolved.status(),
                resolved.diagnostics(),
                resolved.pendingCount(),
                resolved.selectedQuickAccessSlot(),
                resolved.revision(),
                learnedRules,
                signalExtractor,
                chestContentsResolver,
                proximateStorageIds,
                carriedContainerInfoResolver,
                lootChestSource,
                searchQuery,
                currentTick,
                activeChestPanel,
                worldDisplaySources,
                contextualSuggestionStorageIds,
                contextualSuggestionDisplaySources,
                trackedDisplayStorageEntries,
                depositEligibleStorageIds,
                storageIndex,
                liveChestContentPresence,
                liveStorageAffinityEligibility);
    }
}
