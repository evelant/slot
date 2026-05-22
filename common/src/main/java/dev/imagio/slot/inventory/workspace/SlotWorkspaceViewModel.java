package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.classification.DynamicHomeCohortPolicy;
import dev.imagio.slot.classification.FacetIndex;
import dev.imagio.slot.classification.FacetIndexHolder;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.ItemComparisonMode;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityCollections;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.core.ItemStackTags;
import dev.imagio.slot.inventory.query.CarriedIdentityCounts;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.inventory.storage.WorldDisplayStorageSource;
import dev.imagio.slot.inventory.triage.ChipSuggestion;
import dev.imagio.slot.inventory.triage.IslandSignalDescriptor;
import dev.imagio.slot.inventory.triage.IslandSuggestionService;
import dev.imagio.slot.inventory.triage.LearnedIslandRuleStore;
import dev.imagio.slot.inventory.triage.TriageIslandRef;
import dev.imagio.slot.workflow.domain.ChestAffinityMap;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ChestClusterMap;
import dev.imagio.slot.workflow.domain.ChestRole;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.ClaimedChestMap;
import dev.imagio.slot.workflow.domain.ContextualSuggestionFeatureFlags;
import dev.imagio.slot.workflow.domain.CraftRunState;
import dev.imagio.slot.workflow.domain.KitActivation;
import dev.imagio.slot.workflow.domain.KitDefinition;
import dev.imagio.slot.workflow.domain.KitMap;
import dev.imagio.slot.workflow.domain.KitPage;
import dev.imagio.slot.workflow.domain.RecentView;
import dev.imagio.slot.workflow.domain.VisualAtlasIsland;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import dev.imagio.slot.workflow.domain.VisualHomeAssignment;
import dev.imagio.slot.workflow.domain.VisualHomeMap;
import dev.imagio.slot.workflow.domain.WorkflowAcceptedInputRule;
import dev.imagio.slot.workflow.domain.WorkflowDomainSnapshot;
import dev.imagio.slot.workflow.domain.WorkflowTabTargets;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Server-projected workspace state. Replaces the old link/area projection
 * with proximate-chest ghost atlas items and chest chips. See
 * docs/plans/learned-storage.md.
 */
public record SlotWorkspaceViewModel(
        long revision,
        String status,
        String diagnostics,
        int pendingCount,
        int selectedQuickAccessSlot,
        int canvasWidth,
        int canvasHeight,
        int carriedFreeSlotCount,
        int carriedSlotCapacity,
        List<AtlasIsland> islands,
        List<AtlasItem> atlasItems,
        List<AtlasItem> triageItems,
        List<ChestChip> chestChips,
        List<ChestClusterDescriptor> chestClusters,
        List<HotbarSlot> hotbarSlots,
        OffhandSlot offhand,
        List<KitCard> kits,
        LootChestPanel lootChestPanel,
        List<WayfindingTarget> wayfindingTargets,
        Set<IdentityRef> depositableIdentities,
        List<IdentityRef> recentIdentities,
        ActiveChestPanel activeChestPanel,
        CraftRunState craftRun,
        List<ContextualSuggestionLane> contextualSuggestionLanes
) {
    public SlotWorkspaceViewModel {
        status = status == null || status.isBlank() ? "ready" : status;
        diagnostics = diagnostics == null ? "" : diagnostics;
        pendingCount = Math.max(0, pendingCount);
        canvasWidth = Math.max(1, canvasWidth);
        canvasHeight = Math.max(1, canvasHeight);
        carriedFreeSlotCount = Math.max(0, carriedFreeSlotCount);
        carriedSlotCapacity = Math.max(carriedFreeSlotCount, carriedSlotCapacity);
        islands = islands == null ? List.of() : List.copyOf(islands);
        atlasItems = atlasItems == null ? List.of() : List.copyOf(atlasItems);
        triageItems = triageItems == null ? List.of() : List.copyOf(triageItems);
        chestChips = chestChips == null ? List.of() : List.copyOf(chestChips);
        chestClusters = chestClusters == null ? List.of() : List.copyOf(chestClusters);
        hotbarSlots = hotbarSlots == null ? List.of() : List.copyOf(hotbarSlots);
        offhand = offhand == null ? OffhandSlot.empty() : offhand;
        kits = kits == null ? List.of() : List.copyOf(kits);
        lootChestPanel = lootChestPanel == null ? LootChestPanel.empty() : lootChestPanel;
        wayfindingTargets = wayfindingTargets == null ? List.of() : List.copyOf(wayfindingTargets);
        depositableIdentities = depositableIdentities == null
                ? Set.of()
                : Set.copyOf(new LinkedHashSet<>(depositableIdentities));
        recentIdentities = recentIdentities == null ? List.of() : List.copyOf(recentIdentities);
        activeChestPanel = activeChestPanel == null ? ActiveChestPanel.empty() : activeChestPanel;
        craftRun = craftRun == null ? CraftRunState.empty() : craftRun;
        contextualSuggestionLanes = contextualSuggestionLanes == null ? List.of() : List.copyOf(contextualSuggestionLanes);
    }

    public SlotWorkspaceViewModel(
            long revision,
            String status,
            String diagnostics,
            int pendingCount,
            int selectedQuickAccessSlot,
            int canvasWidth,
            int canvasHeight,
            int carriedFreeSlotCount,
            int carriedSlotCapacity,
            List<AtlasIsland> islands,
            List<AtlasItem> atlasItems,
            List<AtlasItem> triageItems,
            List<ChestChip> chestChips,
            List<ChestClusterDescriptor> chestClusters,
            List<HotbarSlot> hotbarSlots,
            OffhandSlot offhand,
            List<KitCard> kits,
            LootChestPanel lootChestPanel,
            List<WayfindingTarget> wayfindingTargets,
            Set<IdentityRef> depositableIdentities,
            List<IdentityRef> recentIdentities,
            ActiveChestPanel activeChestPanel,
            List<ContextualSuggestionLane> contextualSuggestionLanes
    ) {
        this(
                revision,
                status,
                diagnostics,
                pendingCount,
                selectedQuickAccessSlot,
                canvasWidth,
                canvasHeight,
                carriedFreeSlotCount,
                carriedSlotCapacity,
                islands,
                atlasItems,
                triageItems,
                chestChips,
                chestClusters,
                hotbarSlots,
                offhand,
                kits,
                lootChestPanel,
                wayfindingTargets,
                depositableIdentities,
                recentIdentities,
                activeChestPanel,
                CraftRunState.empty(),
                contextualSuggestionLanes);
    }

    public SlotWorkspaceViewModel(
            long revision,
            String status,
            String diagnostics,
            int pendingCount,
            int selectedQuickAccessSlot,
            int canvasWidth,
            int canvasHeight,
            int carriedFreeSlotCount,
            int carriedSlotCapacity,
            List<AtlasIsland> islands,
            List<AtlasItem> atlasItems,
            List<AtlasItem> triageItems,
            List<ChestChip> chestChips,
            List<ChestClusterDescriptor> chestClusters,
            List<HotbarSlot> hotbarSlots,
            OffhandSlot offhand,
            List<KitCard> kits,
            LootChestPanel lootChestPanel,
            List<WayfindingTarget> wayfindingTargets,
            Set<IdentityRef> depositableIdentities,
            List<IdentityRef> recentIdentities,
            ActiveChestPanel activeChestPanel
    ) {
        this(
                revision,
                status,
                diagnostics,
                pendingCount,
                selectedQuickAccessSlot,
                canvasWidth,
                canvasHeight,
                carriedFreeSlotCount,
                carriedSlotCapacity,
                islands,
                atlasItems,
                triageItems,
                chestChips,
                chestClusters,
                hotbarSlots,
                offhand,
                kits,
                lootChestPanel,
                wayfindingTargets,
                depositableIdentities,
                recentIdentities,
                activeChestPanel,
                List.of()
        );
    }

    /** Backwards-compatible constructor: defaults chestClusters + lootChestPanel. */
    public SlotWorkspaceViewModel(
            long revision,
            String status,
            String diagnostics,
            int pendingCount,
            int selectedQuickAccessSlot,
            int canvasWidth,
            int canvasHeight,
            int carriedFreeSlotCount,
            int carriedSlotCapacity,
            List<AtlasIsland> islands,
            List<AtlasItem> atlasItems,
            List<AtlasItem> triageItems,
            List<ChestChip> chestChips,
            List<HotbarSlot> hotbarSlots,
            OffhandSlot offhand,
            List<KitCard> kits
    ) {
        this(revision, status, diagnostics, pendingCount, selectedQuickAccessSlot,
                canvasWidth, canvasHeight, carriedFreeSlotCount, carriedSlotCapacity,
                islands, atlasItems, triageItems, chestChips, List.of(),
                hotbarSlots, offhand, kits, LootChestPanel.empty(), List.of(), Set.of(), List.of(), ActiveChestPanel.empty());
    }

    /** Backwards-compatible constructor: defaults lootChestPanel. */
    public SlotWorkspaceViewModel(
            long revision,
            String status,
            String diagnostics,
            int pendingCount,
            int selectedQuickAccessSlot,
            int canvasWidth,
            int canvasHeight,
            int carriedFreeSlotCount,
            int carriedSlotCapacity,
            List<AtlasIsland> islands,
            List<AtlasItem> atlasItems,
            List<AtlasItem> triageItems,
            List<ChestChip> chestChips,
            List<ChestClusterDescriptor> chestClusters,
            List<HotbarSlot> hotbarSlots,
            OffhandSlot offhand,
            List<KitCard> kits
    ) {
        this(revision, status, diagnostics, pendingCount, selectedQuickAccessSlot,
                canvasWidth, canvasHeight, carriedFreeSlotCount, carriedSlotCapacity,
                islands, atlasItems, triageItems, chestChips, chestClusters,
                hotbarSlots, offhand, kits, LootChestPanel.empty(), List.of(), Set.of(), List.of(), ActiveChestPanel.empty());
    }

    /** Backwards-compatible constructor: defaults wayfindingTargets. */
    public SlotWorkspaceViewModel(
            long revision,
            String status,
            String diagnostics,
            int pendingCount,
            int selectedQuickAccessSlot,
            int canvasWidth,
            int canvasHeight,
            int carriedFreeSlotCount,
            int carriedSlotCapacity,
            List<AtlasIsland> islands,
            List<AtlasItem> atlasItems,
            List<AtlasItem> triageItems,
            List<ChestChip> chestChips,
            List<ChestClusterDescriptor> chestClusters,
            List<HotbarSlot> hotbarSlots,
            OffhandSlot offhand,
            List<KitCard> kits,
            LootChestPanel lootChestPanel
    ) {
        this(revision, status, diagnostics, pendingCount, selectedQuickAccessSlot,
                canvasWidth, canvasHeight, carriedFreeSlotCount, carriedSlotCapacity,
                islands, atlasItems, triageItems, chestChips, chestClusters,
                hotbarSlots, offhand, kits, lootChestPanel, List.of(), Set.of(), List.of(), ActiveChestPanel.empty());
    }

    /** Backwards-compatible constructor: defaults depositableIdentities. */
    public SlotWorkspaceViewModel(
            long revision,
            String status,
            String diagnostics,
            int pendingCount,
            int selectedQuickAccessSlot,
            int canvasWidth,
            int canvasHeight,
            int carriedFreeSlotCount,
            int carriedSlotCapacity,
            List<AtlasIsland> islands,
            List<AtlasItem> atlasItems,
            List<AtlasItem> triageItems,
            List<ChestChip> chestChips,
            List<ChestClusterDescriptor> chestClusters,
            List<HotbarSlot> hotbarSlots,
            OffhandSlot offhand,
            List<KitCard> kits,
            LootChestPanel lootChestPanel,
            List<WayfindingTarget> wayfindingTargets
    ) {
        this(revision, status, diagnostics, pendingCount, selectedQuickAccessSlot,
                canvasWidth, canvasHeight, carriedFreeSlotCount, carriedSlotCapacity,
                islands, atlasItems, triageItems, chestChips, chestClusters,
                hotbarSlots, offhand, kits, lootChestPanel, wayfindingTargets, Set.of(), List.of(), ActiveChestPanel.empty());
    }

    public static SlotWorkspaceViewModel empty() {
        return new SlotWorkspaceViewModel(
                0,
                "waiting for server view",
                "",
                0,
                -1,
                SlotWorkspaceAtlasLayout.CANVAS_WIDTH,
                SlotWorkspaceAtlasLayout.CANVAS_HEIGHT,
                0,
                0,
                SlotWorkspaceAtlasLayout.baseIslands(VisualHomeMap.empty()),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                emptyHotbar(),
                OffhandSlot.empty(),
                List.of(),
                LootChestPanel.empty()
        );
    }

    public static SlotWorkspaceViewModel project(
            InventoryAuthoritySnapshot authority,
            WorkflowDomainSnapshot workflow,
            String status,
            String diagnostics,
            int pendingCount,
            int selectedQuickAccessSlot,
            long revision
    ) {
        return project(authority, workflow, status, diagnostics, pendingCount, selectedQuickAccessSlot, revision, null, null);
    }

    public static SlotWorkspaceViewModel project(
            InventoryAuthoritySnapshot authority,
            WorkflowDomainSnapshot workflow,
            String status,
            String diagnostics,
            int pendingCount,
            int selectedQuickAccessSlot,
            long revision,
            LearnedIslandRuleStore learnedRules
    ) {
        return project(authority, workflow, status, diagnostics, pendingCount, selectedQuickAccessSlot, revision, learnedRules, null);
    }

    public static SlotWorkspaceViewModel project(
            InventoryAuthoritySnapshot authority,
            WorkflowDomainSnapshot workflow,
            String status,
            String diagnostics,
            int pendingCount,
            int selectedQuickAccessSlot,
            long revision,
            LearnedIslandRuleStore learnedRules,
            Function<ItemStack, IslandSignalDescriptor> signalExtractor
    ) {
        return project(authority, workflow, status, diagnostics, pendingCount,
                selectedQuickAccessSlot, revision, learnedRules, signalExtractor,
                null, null, null);
    }

    public static SlotWorkspaceViewModel project(
            InventoryAuthoritySnapshot authority,
            WorkflowDomainSnapshot workflow,
            String status,
            String diagnostics,
            int pendingCount,
            int selectedQuickAccessSlot,
            long revision,
            LearnedIslandRuleStore learnedRules,
            Function<ItemStack, IslandSignalDescriptor> signalExtractor,
            Function<String, ChestContentsSnapshot> chestContentsResolver,
            Set<String> proximateStorageIds
    ) {
        return project(authority, workflow, status, diagnostics, pendingCount,
                selectedQuickAccessSlot, revision, learnedRules, signalExtractor,
                chestContentsResolver, proximateStorageIds, null, null);
    }

    public static SlotWorkspaceViewModel project(
            InventoryAuthoritySnapshot authority,
            WorkflowDomainSnapshot workflow,
            String status,
            String diagnostics,
            int pendingCount,
            int selectedQuickAccessSlot,
            long revision,
            LearnedIslandRuleStore learnedRules,
            Function<ItemStack, IslandSignalDescriptor> signalExtractor,
            Function<String, ChestContentsSnapshot> chestContentsResolver,
            Set<String> proximateStorageIds,
            Function<ItemIdentity, CarriedContainerInfo> carriedContainerInfoResolver
    ) {
        return project(authority, workflow, status, diagnostics, pendingCount,
                selectedQuickAccessSlot, revision, learnedRules, signalExtractor,
                chestContentsResolver, proximateStorageIds, carriedContainerInfoResolver, null);
    }

    public static SlotWorkspaceViewModel project(
            InventoryAuthoritySnapshot authority,
            WorkflowDomainSnapshot workflow,
            String status,
            String diagnostics,
            int pendingCount,
            int selectedQuickAccessSlot,
            long revision,
            LearnedIslandRuleStore learnedRules,
            Function<ItemStack, IslandSignalDescriptor> signalExtractor,
            Function<String, ChestContentsSnapshot> chestContentsResolver,
            Set<String> proximateStorageIds,
            Function<ItemIdentity, CarriedContainerInfo> carriedContainerInfoResolver,
            LootChestSource lootChestSource
    ) {
        return project(authority, workflow, status, diagnostics, pendingCount,
                selectedQuickAccessSlot, revision, learnedRules, signalExtractor,
                chestContentsResolver, proximateStorageIds, carriedContainerInfoResolver,
                lootChestSource, "", 0L);
    }

    /**
     * Legacy overload without {@code currentTick}; used by tests and any
     * caller that doesn't have a clock. Affinity decay is computed
     * against tick 0, which is "no decay applied" for typical bonds
     * created at tick &gt; 0.
     */
    public static SlotWorkspaceViewModel project(
            InventoryAuthoritySnapshot authority,
            WorkflowDomainSnapshot workflow,
            String status,
            String diagnostics,
            int pendingCount,
            int selectedQuickAccessSlot,
            long revision,
            LearnedIslandRuleStore learnedRules,
            Function<ItemStack, IslandSignalDescriptor> signalExtractor,
            Function<String, ChestContentsSnapshot> chestContentsResolver,
            Set<String> proximateStorageIds,
            Function<ItemIdentity, CarriedContainerInfo> carriedContainerInfoResolver,
            LootChestSource lootChestSource,
            String searchQuery
    ) {
        return project(authority, workflow, status, diagnostics, pendingCount,
                selectedQuickAccessSlot, revision, learnedRules, signalExtractor,
                chestContentsResolver, proximateStorageIds, carriedContainerInfoResolver,
                lootChestSource, searchQuery, 0L);
    }

    public static SlotWorkspaceViewModel project(
            InventoryAuthoritySnapshot authority,
            WorkflowDomainSnapshot workflow,
            String status,
            String diagnostics,
            int pendingCount,
            int selectedQuickAccessSlot,
            long revision,
            LearnedIslandRuleStore learnedRules,
            Function<ItemStack, IslandSignalDescriptor> signalExtractor,
            Function<String, ChestContentsSnapshot> chestContentsResolver,
            Set<String> proximateStorageIds,
            Function<ItemIdentity, CarriedContainerInfo> carriedContainerInfoResolver,
            LootChestSource lootChestSource,
            String searchQuery,
            long currentTick
    ) {
        return project(authority, workflow, status, diagnostics, pendingCount,
                selectedQuickAccessSlot, revision, learnedRules, signalExtractor,
                chestContentsResolver, proximateStorageIds, carriedContainerInfoResolver,
                lootChestSource, searchQuery, currentTick, ActiveChestPanel.empty());
    }

    public static SlotWorkspaceViewModel project(
            InventoryAuthoritySnapshot authority,
            WorkflowDomainSnapshot workflow,
            String status,
            String diagnostics,
            int pendingCount,
            int selectedQuickAccessSlot,
            long revision,
            LearnedIslandRuleStore learnedRules,
            Function<ItemStack, IslandSignalDescriptor> signalExtractor,
            Function<String, ChestContentsSnapshot> chestContentsResolver,
            Set<String> proximateStorageIds,
            Function<ItemIdentity, CarriedContainerInfo> carriedContainerInfoResolver,
            LootChestSource lootChestSource,
            String searchQuery,
            long currentTick,
            ActiveChestPanel activeChestPanel
    ) {
        return project(
                authority,
                workflow,
                status,
                diagnostics,
                pendingCount,
                selectedQuickAccessSlot,
                revision,
                learnedRules,
                signalExtractor,
                chestContentsResolver,
                proximateStorageIds,
                carriedContainerInfoResolver,
                lootChestSource,
                searchQuery,
                currentTick,
                activeChestPanel,
                List.of());
    }

    public static SlotWorkspaceViewModel project(
            InventoryAuthoritySnapshot authority,
            WorkflowDomainSnapshot workflow,
            String status,
            String diagnostics,
            int pendingCount,
            int selectedQuickAccessSlot,
            long revision,
            LearnedIslandRuleStore learnedRules,
            Function<ItemStack, IslandSignalDescriptor> signalExtractor,
            Function<String, ChestContentsSnapshot> chestContentsResolver,
            Set<String> proximateStorageIds,
            Function<ItemIdentity, CarriedContainerInfo> carriedContainerInfoResolver,
            LootChestSource lootChestSource,
            String searchQuery,
            long currentTick,
            ActiveChestPanel activeChestPanel,
            List<WorldDisplayStorageSource> worldDisplaySources
    ) {
        return project(
                authority,
                workflow,
                status,
                diagnostics,
                pendingCount,
                selectedQuickAccessSlot,
                revision,
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
                proximateStorageIds,
                worldDisplaySources,
                List.of());
    }

    public static SlotWorkspaceViewModel project(
            InventoryAuthoritySnapshot authority,
            WorkflowDomainSnapshot workflow,
            String status,
            String diagnostics,
            int pendingCount,
            int selectedQuickAccessSlot,
            long revision,
            LearnedIslandRuleStore learnedRules,
            Function<ItemStack, IslandSignalDescriptor> signalExtractor,
            Function<String, ChestContentsSnapshot> chestContentsResolver,
            Set<String> proximateStorageIds,
            Function<ItemIdentity, CarriedContainerInfo> carriedContainerInfoResolver,
            LootChestSource lootChestSource,
            String searchQuery,
            long currentTick,
            ActiveChestPanel activeChestPanel,
            List<WorldDisplayStorageSource> worldDisplaySources,
            Set<String> contextualSuggestionStorageIds,
            List<WorldDisplayStorageSource> contextualSuggestionDisplaySources
    ) {
        return project(
                authority,
                workflow,
                status,
                diagnostics,
                pendingCount,
                selectedQuickAccessSlot,
                revision,
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
                List.of());
    }

    public static SlotWorkspaceViewModel project(
            InventoryAuthoritySnapshot authority,
            WorkflowDomainSnapshot workflow,
            String status,
            String diagnostics,
            int pendingCount,
            int selectedQuickAccessSlot,
            long revision,
            LearnedIslandRuleStore learnedRules,
            Function<ItemStack, IslandSignalDescriptor> signalExtractor,
            Function<String, ChestContentsSnapshot> chestContentsResolver,
            Set<String> proximateStorageIds,
            Function<ItemIdentity, CarriedContainerInfo> carriedContainerInfoResolver,
            LootChestSource lootChestSource,
            String searchQuery,
            long currentTick,
            ActiveChestPanel activeChestPanel,
            List<WorldDisplayStorageSource> worldDisplaySources,
            Set<String> contextualSuggestionStorageIds,
            List<WorldDisplayStorageSource> contextualSuggestionDisplaySources,
            Collection<WorkspaceStorageIndex.StorageEntry> trackedDisplayStorageEntries
    ) {
        return project(
                authority,
                workflow,
                status,
                diagnostics,
                pendingCount,
                selectedQuickAccessSlot,
                revision,
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
                proximateStorageIds);
    }

    public static SlotWorkspaceViewModel project(
            InventoryAuthoritySnapshot authority,
            WorkflowDomainSnapshot workflow,
            String status,
            String diagnostics,
            int pendingCount,
            int selectedQuickAccessSlot,
            long revision,
            LearnedIslandRuleStore learnedRules,
            Function<ItemStack, IslandSignalDescriptor> signalExtractor,
            Function<String, ChestContentsSnapshot> chestContentsResolver,
            Set<String> proximateStorageIds,
            Function<ItemIdentity, CarriedContainerInfo> carriedContainerInfoResolver,
            LootChestSource lootChestSource,
            String searchQuery,
            long currentTick,
            ActiveChestPanel activeChestPanel,
            List<WorldDisplayStorageSource> worldDisplaySources,
            Set<String> contextualSuggestionStorageIds,
            List<WorldDisplayStorageSource> contextualSuggestionDisplaySources,
            Collection<WorkspaceStorageIndex.StorageEntry> trackedDisplayStorageEntries,
            Set<String> depositEligibleStorageIds
        ) {
        return project(
                authority,
                workflow,
                status,
                diagnostics,
                pendingCount,
                selectedQuickAccessSlot,
                revision,
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
                null,
                null);
    }

    public static SlotWorkspaceViewModel project(
            InventoryAuthoritySnapshot authority,
            WorkflowDomainSnapshot workflow,
            String status,
            String diagnostics,
            int pendingCount,
            int selectedQuickAccessSlot,
            long revision,
            LearnedIslandRuleStore learnedRules,
            Function<ItemStack, IslandSignalDescriptor> signalExtractor,
            Function<String, ChestContentsSnapshot> chestContentsResolver,
            Set<String> proximateStorageIds,
            Function<ItemIdentity, CarriedContainerInfo> carriedContainerInfoResolver,
            LootChestSource lootChestSource,
            String searchQuery,
            long currentTick,
            ActiveChestPanel activeChestPanel,
            List<WorldDisplayStorageSource> worldDisplaySources,
            Set<String> contextualSuggestionStorageIds,
            List<WorldDisplayStorageSource> contextualSuggestionDisplaySources,
            Collection<WorkspaceStorageIndex.StorageEntry> trackedDisplayStorageEntries,
            Set<String> depositEligibleStorageIds,
            DepositPlanner.ChestContentPresence liveChestContentPresence,
            DepositPlanner.ChestEligibility liveStorageAffinityEligibility
        ) {
        InventoryAuthoritySnapshot resolvedAuthority = authority == null ? InventoryAuthoritySnapshot.empty() : authority;
        WorkflowDomainSnapshot resolvedWorkflow = workflow == null ? WorkflowDomainSnapshot.empty() : workflow;
        CarriedIdentityCounts carriedIdentityCounts = CarriedIdentityCounts.from(resolvedAuthority);
        WorkflowTabTargets.Resolution targetResolution = WorkflowTabTargets.resolve(carriedIdentityCounts, resolvedWorkflow);
        Map<ItemIdentity, Integer> wantedCounts = targetResolution.wantedCounts();
        RecentView recents = resolvedWorkflow.recents();
        VisualHomeMap visualHomeMap = resolvedWorkflow.visualHomeMap();
        ClaimedChestMap claimedChestMap = resolvedWorkflow.claimedChestMap();
        // Decay the affinity map at projection time so downstream
        // consumers (deposit-preview, depositableIdentities, chest
        // chip ranking) see the same scores the deposit-RPC planner
        // does — both apply the same per-bond time decay. Without
        // this, a stale bond can light up the deposit-preview while
        // the planner refuses to deposit (its decayed score is 0).
        ChestAffinityMap affinityMap = resolvedWorkflow.chestAffinityMap().decayed(currentTick);
        Set<String> proximate = proximateStorageIds == null ? Set.of() : proximateStorageIds;
        Set<String> depositEligible = depositEligibleStorageIds == null ? proximate : depositEligibleStorageIds;
        List<WorldDisplayStorageSource> displaySources = worldDisplaySources == null
                ? List.of()
                : List.copyOf(worldDisplaySources);
        List<WorkspaceStorageIndex.StorageEntry> trackedDisplayEntries = trackedDisplayStorageEntries == null
                ? List.of()
                : trackedDisplayStorageEntries.stream()
                        .filter(entry -> entry != null
                                && entry.target() != null
                                && entry.target().displayTarget()
                                && entry.target().displayKind() != null
                                && entry.target().displayKind().trackedStorage())
                        .toList();
        int[] carriedCounts = countCarriedFreeSlotsAndCapacity(resolvedAuthority);
        int carriedFreeSlotCount = carriedCounts[0];
        int carriedSlotCapacity = carriedCounts[1];
        Function<ItemIdentity, CarriedContainerInfo> containerResolver = carriedContainerInfoResolver == null
                ? identity -> null
                : carriedContainerInfoResolver;

        List<AtlasItemAccumulator> accumulators = groupedAtlasEntries(resolvedAuthority);
        Map<ItemIdentity, Integer> recentRankByIdentity = recentRankByIdentity(recents);

        // Build proximate-chest ghost projection: union all stacks across
        // proximate chests, keyed by identity, with per-chest breakdown for
        // hover/zoom drill-down. Items not currently carried but present in
        // some proximate chest are surfaced as faded ghost cards on their
        // homed island. See docs/plans/learned-storage.md.
        ProximateGhostProjection ghosts = ProximateGhostProjection.build(
                claimedChestMap, chestContentsResolver, proximate, visualHomeMap, displaySources);
        Set<String> contextualSuggestionStorage = contextualSuggestionStorageIds == null
                ? proximate
                : contextualSuggestionStorageIds;
        List<WorldDisplayStorageSource> contextualSuggestionDisplays = contextualSuggestionDisplaySources == null
                ? displaySources
                : contextualSuggestionDisplaySources;
        ProximateGhostProjection contextualSuggestionGhosts = ContextualSuggestionFeatureFlags.ROWS_ENABLED
                ? ProximateGhostProjection.build(
                        claimedChestMap,
                        chestContentsResolver,
                        contextualSuggestionStorage,
                        visualHomeMap,
                        contextualSuggestionDisplays)
                : ProximateGhostProjection.empty();
        // Search-as-find: collect non-proximate chest stocks too, with the
        // dimension noted in the label. Hover/zoom on a search hit reveals
        // "Storage Area 2 — nether". See docs/plans/learned-storage.md.
        ElsewhereGhostProjection elsewhereGhosts = ElsewhereGhostProjection.build(
                claimedChestMap, chestContentsResolver, proximate, trackedDisplayEntries);
        Map<ItemIdentity, List<ChestPresenceEntry>> elsewherePresence = elsewhereGhosts.presenceByIdentity();

        // Kit ghost markers: when a kit is active, every needed-but-not-
        // carried identity (page slots + bring list) is flagged as kit-needed
        // so the atlas card highlights it. Items the player has never seen
        // yet are added as synthesized ghost accumulators below so they
        // render where their visual home is.
        Set<ItemIdentity> kitNeededIdentities =
                ItemIdentityCollections.normalizedSet(targetResolution.missingWorkflowIdentities());
        Map<ItemIdentity, Integer> playerDesiredCounts = resolvedWorkflow.playerDesiredCounts();
        Map<ItemIdentity, Integer> activeDesiredCounts = activeDesiredCounts(
                carriedIdentityCounts,
                targetResolution.desiredCounts());
        Set<ItemIdentity> desiredFromWorkflowTabIdentities =
                ItemIdentityCollections.normalizedSet(targetResolution.desiredFromWorkflowTab());

        // Synthesize ghost accumulators for identities present only in
        // proximate chests (homed-but-not-carried). Carried identities use
        // their existing accumulator + presence pip.
        Set<ItemIdentity> carriedIdentities = new LinkedHashSet<>();
        for (AtlasItemAccumulator accumulator : accumulators) {
            ItemIdentityCollections.add(carriedIdentities, accumulator.identity());
        }
        dev.imagio.slot.SlotDiagnostics.identityResolution(
                carriedIdentities,
                ghosts.totalsByIdentity(),
                elsewhereGhosts.totalsByIdentity(),
                kitNeededIdentities,
                resolvedWorkflow.kitMap() == null
                        ? ""
                        : resolvedWorkflow.kitMap().activation().kitId()
        );
        for (Map.Entry<ItemIdentity, Integer> entry : ghosts.totalsByIdentity().entrySet()) {
            ItemIdentity identity = entry.getKey();
            if (ItemIdentityCollections.containsCanonical(carriedIdentities, identity)) {
                continue;
            }
            ItemStack ghostStack = ghosts.displayStackByIdentity().get(identity);
            if (ghostStack == null || ghostStack.isEmpty()) {
                continue;
            }
            accumulators.add(AtlasItemAccumulator.ghost(identity, ghostStack, entry.getValue()));
        }

        // Kit ghosts: synthesize accumulators for kit-needed identities that
        // aren't already covered by carry + proximate ghosts, so the player
        // sees them on their home island even when no chest currently has
        // them.
        Set<ItemIdentity> ghostIdentities = new LinkedHashSet<>(carriedIdentities);
        for (AtlasItemAccumulator accumulator : accumulators) {
            ItemIdentityCollections.add(ghostIdentities, accumulator.identity());
        }
        for (ItemIdentity identity : kitNeededIdentities) {
            if (identity == null || ItemIdentityCollections.containsCanonical(ghostIdentities, identity)) {
                continue;
            }
            ItemStack stack = resolveGhostStack(identity);
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            accumulators.add(AtlasItemAccumulator.ghost(identity, stack, 0));
            ItemIdentityCollections.add(ghostIdentities, identity);
        }
        // Desired-count ghosts: a standing desired target should still be
        // visible in the Fetch lane after the player has used up their last
        // carried copy, even when no storage currently has the item.
        for (ItemIdentity identity : activeDesiredCounts.keySet()) {
            if (identity == null || ItemIdentityCollections.containsCanonical(ghostIdentities, identity)) {
                continue;
            }
            ItemStack stack = ghosts.displayStackByIdentity().get(identity);
            int total = ghosts.totalsByIdentity().getOrDefault(identity, 0);
            if (stack == null || stack.isEmpty()) {
                stack = elsewhereGhosts.displayStackByIdentity().get(identity);
                total = elsewhereGhosts.totalsByIdentity().getOrDefault(identity, 0);
            }
            if (stack == null || stack.isEmpty()) {
                stack = resolveGhostStack(identity);
            }
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            accumulators.add(AtlasItemAccumulator.ghost(identity, stack, total));
            ItemIdentityCollections.add(ghostIdentities, identity);
        }
        // Wanted items behave like persisted fetch targets: surface a ghost
        // card on the item's home even when the item only exists in a remote
        // claimed chest, and let the normal wayfinding projection point at
        // that chest until the carried target is met.
        for (ItemIdentity identity : wantedCounts.keySet()) {
            if (identity == null || ItemIdentityCollections.containsCanonical(ghostIdentities, identity)) {
                continue;
            }
            ItemStack stack = ghosts.displayStackByIdentity().get(identity);
            int total = ghosts.totalsByIdentity().getOrDefault(identity, 0);
            if (stack == null || stack.isEmpty()) {
                stack = elsewhereGhosts.displayStackByIdentity().get(identity);
                total = elsewhereGhosts.totalsByIdentity().getOrDefault(identity, 0);
            }
            if (stack == null || stack.isEmpty()) {
                stack = resolveGhostStack(identity);
            }
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            accumulators.add(AtlasItemAccumulator.ghost(identity, stack, total));
            ItemIdentityCollections.add(ghostIdentities, identity);
        }
        // Tracked-storage x-ray and search both need remote-only claimed
        // storage identities in the view model. The wall hides ordinary
        // remote ghosts until search / x-ray / active intent asks for them,
        // so carrying them here no longer pollutes the default wall.
        for (Map.Entry<ItemIdentity, ItemStack> entry : elsewhereGhosts.displayStackByIdentity().entrySet()) {
            ItemIdentity identity = entry.getKey();
            if (ItemIdentityCollections.containsCanonical(ghostIdentities, identity)) {
                continue;
            }
            ItemStack stack = entry.getValue();
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            int total = elsewhereGhosts.totalsByIdentity().getOrDefault(identity, 0);
            accumulators.add(AtlasItemAccumulator.ghost(identity, stack, total));
            ItemIdentityCollections.add(ghostIdentities, identity);
        }

        accumulators.sort(Comparator
                .comparing((AtlasItemAccumulator a) -> {
                    VisualHomeAssignment hm = visualHomeMap.assignment(a.identity());
                    return hm == null ? "" : hm.islandId();
                })
                .thenComparingInt(a -> {
                    VisualHomeAssignment hm = visualHomeMap.assignment(a.identity());
                    return hm == null
                            ? recentRankByIdentity.getOrDefault(a.identity(), Integer.MAX_VALUE)
                            : hm.ordinal();
                })
                .thenComparing(a -> a.name().toLowerCase(Locale.ROOT))
                .thenComparing(a -> a.identity().itemId())
                .thenComparingInt(AtlasItemAccumulator::firstSlotIndex));

        ArrayList<AtlasIsland> layoutIslands = new ArrayList<>(SlotWorkspaceAtlasLayout.baseIslands(visualHomeMap));
        ArrayList<AtlasItem> atlasItems = new ArrayList<>();
        ArrayList<AtlasItem> triageItems = new ArrayList<>();
        Set<ItemIdentity> recentIdentities = ItemIdentityCollections.normalizedSet(recents.visibleItems());
        Set<ItemIdentity> junkTags = ItemIdentityCollections.normalizedSet(
                resolvedWorkflow.workflowProjection().junkTags());
        List<TriageIslandRef> triageIslandRefs = triageIslandRefs(visualHomeMap);
        LearnedIslandRuleStore resolvedLearnedRules = learnedRules == null ? new LearnedIslandRuleStore() : learnedRules;
        DynamicHomeCohortPolicy cohortPolicy = signalExtractor == null ? null : DynamicHomeCohortPolicy.current();
        Predicate<String> subsystemQualifier = cohortPolicy == null ? id -> false : cohortPolicy.qualifier();
        Predicate<String> organizationGroupQualifier = cohortPolicy == null
                ? id -> false
                : cohortPolicy.organizationGroupQualifier();

        for (AtlasItemAccumulator accumulator : accumulators) {
            VisualHomeAssignment assignment = visualHomeMap.assignment(accumulator.identity());
            List<ChestPresenceEntry> presence = ghosts.presenceByIdentity().getOrDefault(accumulator.identity(), List.of());
            List<ChestPresenceEntry> elsewhere = elsewherePresence.getOrDefault(accumulator.identity(), List.of());
            boolean ghostOnly = !accumulator.carried();
            int proximateCount = ghosts.totalsByIdentity().getOrDefault(accumulator.identity(), 0);
            boolean kitNeeded = ItemIdentityCollections.containsCanonical(kitNeededIdentities, accumulator.identity())
                    && !accumulator.carried();
            int desiredCount = targetResolution.desiredCount(accumulator.identity());
            boolean desiredCountFromKit = targetResolution.desiredFromWorkflowTab(accumulator.identity());
            int wantedCount = targetResolution.wantedCount(accumulator.identity());
            boolean junk = ItemIdentityCollections.containsCanonical(junkTags, accumulator.identity());

            if (assignment == null) {
                List<ChipSuggestion> chipSuggestions = List.of();
                if (signalExtractor != null) {
                    IslandSignalDescriptor descriptor = signalExtractor.apply(accumulator.displayStack());
                    if (descriptor != null) {
                        chipSuggestions = IslandSuggestionService.suggest(
                                descriptor,
                                resolvedLearnedRules,
                                triageIslandRefs,
                                visualHomeMap.dismissedTemplateIds(),
                                subsystemQualifier,
                                organizationGroupQualifier
                        );
                    }
                }
                CarriedContainerInfo containerInfo = containerResolver.apply(accumulator.identity());
                boolean isContainer = containerInfo != null;
                int containerFree = isContainer ? containerInfo.freeSlots() : 0;
                int containerCapacity = isContainer ? containerInfo.slotCapacity() : 0;
                if (ghostOnly) {
                    boolean intentGhost = kitNeeded || desiredCount > 0 || wantedCount > 0;
                    if (proximateCount > 0) {
                        // Nearby claimed-chest contents can be the first time
                        // SLOT sees an identity on an existing world. Queue
                        // them for the same common auto-home pass as carried
                        // items so existing storage becomes visible without a
                        // take/deposit round-trip. Triage is not rendered as a
                        // product section; here it is a bounded work queue.
                        triageItems.add(new AtlasItem(
                                IdentityRef.from(accumulator.identity()),
                                accumulator.displayStack(),
                                accumulator.name(),
                                accumulator.totalCount(),
                                accumulator.firstSlotIndex(),
                                SlotWorkspaceAtlasLayout.ISLAND_TRIAGE,
                                ItemIdentityCollections.containsCanonical(recentIdentities, accumulator.identity()),
                                false,
                                false,
                                true,
                                proximateCount,
                                chipSuggestions,
                                presence,
                                elsewhere,
                                isContainer,
                                containerFree,
                                containerCapacity,
                                kitNeeded,
                                desiredCount,
                                desiredCountFromKit,
                                wantedCount,
                                junk,
                                accumulator.largestCarriedSourceId(),
                                accumulator.largestCarriedSlotIndex(),
                                accumulator.largestCarriedSlotCount()
                        ));
                    } else if (intentGhost && ensureMiscIsland(layoutIslands)) {
                        atlasItems.add(new AtlasItem(
                                IdentityRef.from(accumulator.identity()),
                                accumulator.displayStack(),
                                accumulator.name(),
                                accumulator.totalCount(),
                                accumulator.firstSlotIndex(),
                                SlotWorkspaceAtlasLayout.ISLAND_MISC,
                                ItemIdentityCollections.containsCanonical(recentIdentities, accumulator.identity()),
                                false,
                                false,
                                true,
                                proximateCount,
                                chipSuggestions,
                                presence,
                                elsewhere,
                                isContainer,
                                containerFree,
                                containerCapacity,
                                kitNeeded,
                                desiredCount,
                                desiredCountFromKit,
                                wantedCount,
                                junk,
                                accumulator.largestCarriedSourceId(),
                                accumulator.largestCarriedSlotIndex(),
                                accumulator.largestCarriedSlotCount()
                        ));
                    }
                    continue;
                }
                triageItems.add(new AtlasItem(
                        IdentityRef.from(accumulator.identity()),
                        accumulator.displayStack(),
                        accumulator.name(),
                        accumulator.totalCount(),
                        accumulator.firstSlotIndex(),
                        SlotWorkspaceAtlasLayout.ISLAND_TRIAGE,
                        ItemIdentityCollections.containsCanonical(recentIdentities, accumulator.identity()),
                        false,
                        accumulator.carried(),
                        false,
                        proximateCount,
                        chipSuggestions,
                        presence,
                        elsewhere,
                        isContainer,
                        containerFree,
                        containerCapacity,
                        kitNeeded,
                        desiredCount,
                        desiredCountFromKit,
                        wantedCount,
                        junk,
                        accumulator.largestCarriedSourceId(),
                        accumulator.largestCarriedSlotIndex(),
                        accumulator.largestCarriedSlotCount()
                ));
                continue;
            }
            String islandId = assignment.islandId();
            boolean playerPlaced = assignment.origin() == dev.imagio.slot.workflow.domain.VisualHomeOrigin.PLAYER_PLACED;
            CarriedContainerInfo containerInfo = containerResolver.apply(accumulator.identity());
            boolean isContainer = containerInfo != null;
            int containerFree = isContainer ? containerInfo.freeSlots() : 0;
            int containerCapacity = isContainer ? containerInfo.slotCapacity() : 0;
            if (SlotWorkspaceAtlasLayout.island(layoutIslands, islandId) == null) {
                if (ghostOnly) {
                    continue;
                }
                triageItems.add(new AtlasItem(
                        IdentityRef.from(accumulator.identity()),
                        accumulator.displayStack(),
                        accumulator.name(),
                        accumulator.totalCount(),
                        accumulator.firstSlotIndex(),
                        SlotWorkspaceAtlasLayout.ISLAND_TRIAGE,
                        ItemIdentityCollections.containsCanonical(recentIdentities, accumulator.identity()),
                        false,
                        accumulator.carried(),
                        false,
                        proximateCount,
                        List.of(),
                        presence,
                        elsewhere,
                        isContainer,
                        containerFree,
                        containerCapacity,
                        kitNeeded,
                        desiredCount,
                        desiredCountFromKit,
                        wantedCount,
                        junk,
                        accumulator.largestCarriedSourceId(),
                        accumulator.largestCarriedSlotIndex(),
                        accumulator.largestCarriedSlotCount()
                ));
                continue;
            }
            atlasItems.add(new AtlasItem(
                    IdentityRef.from(accumulator.identity()),
                    accumulator.displayStack(),
                    accumulator.name(),
                    accumulator.totalCount(),
                    accumulator.firstSlotIndex(),
                    islandId,
                    ItemIdentityCollections.containsCanonical(recentIdentities, accumulator.identity()),
                    playerPlaced,
                    accumulator.carried(),
                    ghostOnly,
                    proximateCount,
                    List.of(),
                    presence,
                    elsewhere,
                    isContainer,
                    containerFree,
                    containerCapacity,
                    kitNeeded,
                    desiredCount,
                    desiredCountFromKit,
                    wantedCount,
                    junk,
                    accumulator.largestCarriedSourceId(),
                    accumulator.largestCarriedSlotIndex(),
                    accumulator.largestCarriedSlotCount()
            ));
        }

        triageItems.sort(Comparator
                .comparingInt((AtlasItem item) -> recentRankByIdentity.getOrDefault(item.identity().toIdentity(), Integer.MAX_VALUE))
                .thenComparing(item -> item.name().toLowerCase(Locale.ROOT))
                .thenComparing(item -> item.identity().itemId()));

        java.util.function.ToIntFunction<ItemIdentity> reservedCountResolver = identity -> reservedCarryCount(
                identity,
                targetResolution);
        List<AtlasItem> workflowPutAwayItems = List.of();
        PutAwayRouteProjection putAwayRoutes = PutAwayRouteProjection.empty();
        if (resolvedWorkflow.kitMap() != null && !resolvedWorkflow.kitMap().activeLineage().isEmpty()) {
            Map<String, AtlasIsland> islandsById = islandsById(layoutIslands);
            Set<ItemIdentity> activationPutAwayIdentities =
                    resolvedWorkflow.kitMap().activation().putAwayIdentities();
            Set<IdentityRef> proximatePutAwayRouted = depositableIdentities(
                    atlasItems,
                    claimedChestMap,
                    affinityMap,
                    proximate,
                    chestContentsResolver,
                    reservedCountResolver,
                    depositEligible,
                    liveChestContentPresence,
                    liveStorageAffinityEligibility);
            putAwayRoutes = putAwayRouteProjection(
                    atlasItems,
                    triageItems,
                    targetResolution,
                    activationPutAwayIdentities,
                    claimedChestMap,
                    affinityMap,
                    chestContentsResolver,
                    trackedDisplayEntries);
            Set<IdentityRef> putAwayRouted = unionIdentityRefs(
                    proximatePutAwayRouted,
                    putAwayRoutes.routedIdentities());
            workflowPutAwayItems = activeWorkflowPutAwayItems(
                    atlasItems,
                    triageItems,
                    targetResolution,
                    activationPutAwayIdentities,
                    putAwayRouted);
            atlasItems = filterForActiveWorkflowTab(
                    atlasItems,
                    targetResolution,
                    activationPutAwayIdentities,
                    putAwayRouted,
                    searchQuery,
                    islandsById);
            triageItems = filterForActiveWorkflowTab(
                    triageItems,
                    targetResolution,
                    activationPutAwayIdentities,
                    putAwayRouted,
                    searchQuery,
                    islandsById);
        }

        List<AtlasIsland> islandsWithCarriedCounts = withCarriedCounts(layoutIslands, atlasItems);

        ChestClusterMap clusterMap = ChestClusterMap.derive(claimedChestMap);
        List<ChestChip> chestChips = chestChips(
                claimedChestMap,
                affinityMap,
                chestContentsResolver,
                proximate,
                clusterMap,
                trackedDisplayEntries);
        List<ChestClusterDescriptor> chestClusters = chestClusterDescriptors(clusterMap, resolvedWorkflow.clusterLabels());

        List<KitCard> kitCards = kitCards(
                carriedIdentityCounts, resolvedWorkflow.kitMap(), resolvedWorkflow.kitDesiredCounts());
        LootChestPanel lootPanel = lootChestPanel(
                lootChestSource, visualHomeMap, signalExtractor, resolvedLearnedRules, triageIslandRefs,
                subsystemQualifier, organizationGroupQualifier);
        List<WayfindingTarget> wayfindingTargets = wayfindingTargets(
                carriedIdentityCounts,
                claimedChestMap,
                chestContentsResolver,
                trackedDisplayEntries,
                kitNeededIdentities,
                wantedCounts,
                targetResolution.desiredCounts(),
                desiredFromWorkflowTabIdentities);
        wayfindingTargets = mergeWayfindingTargets(wayfindingTargets, putAwayRoutes.targets());
        Set<IdentityRef> depositableIdentities = depositableIdentities(
                atlasItems,
                claimedChestMap,
                affinityMap,
                proximate,
                chestContentsResolver,
                reservedCountResolver,
                depositEligible,
                liveChestContentPresence,
                liveStorageAffinityEligibility);
        ArrayList<AtlasItem> contextualSuggestionCandidates = new ArrayList<>(atlasItems.size() + triageItems.size());
        contextualSuggestionCandidates.addAll(atlasItems);
        contextualSuggestionCandidates.addAll(triageItems);
        if (ContextualSuggestionFeatureFlags.ROWS_ENABLED) {
            addContextualSuggestionStorageGhosts(
                    contextualSuggestionCandidates,
                    contextualSuggestionGhosts,
                    recentIdentities,
                    kitNeededIdentities,
                    targetResolution.desiredCounts(),
                    desiredFromWorkflowTabIdentities,
                    wantedCounts,
                    visualHomeMap);
        }
        List<ContextualSuggestionLane> contextualSuggestionLanes = contextualSuggestionLanes(
                contextualSuggestionCandidates,
                workflowPutAwayItems,
                resolvedWorkflow,
                FacetIndexHolder.get(),
                carriedFreeSlotCount,
                carriedSlotCapacity,
                currentTick,
                ContextualSuggestionFeatureFlags.ROWS_ENABLED);
        List<IdentityRef> recentIdentitiesList = recentIdentityRefs(recents);
        return new SlotWorkspaceViewModel(
                revision,
                status,
                diagnostics,
                pendingCount,
                selectedQuickAccessSlot,
                SlotWorkspaceAtlasLayout.CANVAS_WIDTH,
                SlotWorkspaceAtlasLayout.CANVAS_HEIGHT,
                carriedFreeSlotCount,
                carriedSlotCapacity,
                islandsWithCarriedCounts,
                atlasItems,
                triageItems,
                chestChips,
                chestClusters,
                hotbarSlots(resolvedAuthority, selectedQuickAccessSlot),
                OffhandSlot.from(resolvedAuthority),
                kitCards,
                lootPanel,
                wayfindingTargets,
                depositableIdentities,
                recentIdentitiesList,
                activeChestPanel == null ? ActiveChestPanel.empty() : activeChestPanel,
                resolvedWorkflow.craftRun(),
                contextualSuggestionLanes
        );
    }

    private static List<IdentityRef> recentIdentityRefs(RecentView recents) {
        if (recents == null) {
            return List.of();
        }
        List<ItemIdentity> visible = recents.visibleItems();
        if (visible.isEmpty()) {
            return List.of();
        }
        ArrayList<IdentityRef> refs = new ArrayList<>(visible.size());
        for (ItemIdentity identity : visible) {
            if (identity != null) {
                refs.add(IdentityRef.from(identity));
            }
        }
        return List.copyOf(refs);
    }

    private static List<ContextualSuggestionLane> contextualSuggestionLanes(
            List<AtlasItem> candidates,
            List<AtlasItem> workflowPutAwayItems,
            WorkflowDomainSnapshot workflow,
            FacetIndex facetIndex,
            int carriedFreeSlotCount,
            int carriedSlotCapacity,
            long currentTick,
            boolean includeExperimentalRows
    ) {
        ArrayList<ContextualSuggestionLane> lanes = new ArrayList<>();
        ContextualSuggestionLane fetch = fetchLane(candidates);
        if (fetch.displayable()) {
            lanes.add(fetch);
        }
        ContextualSuggestionLane workflowPutAway = workflowPutAwayLane(workflowPutAwayItems);
        if (workflowPutAway.displayable()) {
            lanes.add(workflowPutAway);
        }
        if (!includeExperimentalRows) {
            return lanes.isEmpty() ? List.of() : List.copyOf(lanes);
        }
        List<ContextualSuggestionLane> scored = ContextualSuggestionScorer.lanes(
                candidates,
                workflow,
                facetIndex,
                carriedFreeSlotCount,
                carriedSlotCapacity,
                currentTick);
        if (workflowPutAway.displayable()) {
            for (ContextualSuggestionLane lane : scored) {
                if (lane != null && !lane.putAway()) {
                    lanes.add(lane);
                }
            }
        } else {
            lanes.addAll(scored);
        }
        return lanes.isEmpty() ? List.of() : List.copyOf(lanes);
    }

    private static ContextualSuggestionLane workflowPutAwayLane(List<AtlasItem> items) {
        if (items == null || items.isEmpty()) {
            return new ContextualSuggestionLane(ContextualSuggestionLane.PUT_AWAY, "Put Away", List.of());
        }
        LinkedHashMap<IdentityRef, AtlasItem> unique = new LinkedHashMap<>();
        for (AtlasItem item : items) {
            if (item == null || item.identity() == null) {
                continue;
            }
            unique.putIfAbsent(item.identity(), item);
        }
        ArrayList<AtlasItem> sorted = new ArrayList<>(unique.values());
        sorted.sort(Comparator
                .comparing((AtlasItem item) -> item.putAwayState() != PutAwayState.ROUTED)
                .thenComparing(item -> item.name().toLowerCase(Locale.ROOT))
                .thenComparing(item -> item.identity().itemId()));
        return new ContextualSuggestionLane(
                ContextualSuggestionLane.PUT_AWAY,
                "Put Away",
                sorted,
                "");
    }

    private static ContextualSuggestionLane fetchLane(List<AtlasItem> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return new ContextualSuggestionLane(ContextualSuggestionLane.FETCH, "Fetch", List.of());
        }
        LinkedHashMap<IdentityRef, AtlasItem> fetchItems = new LinkedHashMap<>();
        for (AtlasItem item : candidates) {
            if (item == null || item.identity() == null) {
                continue;
            }
            if (hasFetchGap(item)) {
                fetchItems.putIfAbsent(item.identity(), item);
            }
        }
        return new ContextualSuggestionLane(
                ContextualSuggestionLane.FETCH,
                "Fetch",
                List.copyOf(fetchItems.values()));
    }

    private static PutAwayRouteProjection putAwayRouteProjection(
            List<AtlasItem> atlasItems,
            List<AtlasItem> triageItems,
            WorkflowTabTargets.Resolution targets,
            Set<ItemIdentity> activationPutAwayIdentities,
            ClaimedChestMap claimedChestMap,
            ChestAffinityMap affinityMap,
            Function<String, ChestContentsSnapshot> chestContentsResolver,
            Collection<WorkspaceStorageIndex.StorageEntry> trackedDisplayEntries
    ) {
        if (targets == null || activationPutAwayIdentities == null || activationPutAwayIdentities.isEmpty()) {
            return PutAwayRouteProjection.empty();
        }
        ArrayList<AtlasItem> candidates = new ArrayList<>();
        collectPutAwayRouteCandidates(candidates, atlasItems, targets, activationPutAwayIdentities);
        collectPutAwayRouteCandidates(candidates, triageItems, targets, activationPutAwayIdentities);
        if (candidates.isEmpty()) {
            return PutAwayRouteProjection.empty();
        }

        Set<String> claimedRouteIds = claimedStorageIds(claimedChestMap);
        DepositPlanner.ChestContentPresence contentPresence = contentPresenceFromResolver(chestContentsResolver);
        DepositPlanner.ChestEligibility chestEligibility = eligibilityFromResolver(chestContentsResolver);
        LinkedHashMap<String, PutAwayRouteAccumulator> routesByStorage = new LinkedHashMap<>();
        LinkedHashSet<IdentityRef> routedIdentities = new LinkedHashSet<>();
        for (AtlasItem item : candidates) {
            ItemIdentity identity = item.identity().toIdentity();
            if (identity == null) {
                continue;
            }
            boolean routed = false;
            for (UUID storageId : DepositPlanner.rankChestsForIdentity(
                    identity,
                    claimedChestMap,
                    affinityMap,
                    claimedRouteIds,
                    contentPresence,
                    chestEligibility)) {
                ClaimedChest chest = claimedChestMap == null ? null : claimedChestMap.chest(storageId);
                if (chest == null || chest.anchors().isEmpty()) {
                    continue;
                }
                ChestAnchor anchor = chest.anchors().iterator().next();
                addPutAwayRoute(
                        routesByStorage,
                        storageId.toString(),
                        anchor.dimensionId(),
                        anchor.x(),
                        anchor.y(),
                        anchor.z(),
                        identity,
                        item.totalCount());
                routed = true;
            }
            for (WorkspaceStorageIndex.StorageEntry entry : displayPutAwayRouteEntries(trackedDisplayEntries, identity)) {
                StorageTargetRef target = entry.target();
                addPutAwayRoute(
                        routesByStorage,
                        target.storageId(),
                        target.dimensionId(),
                        target.x(),
                        target.y(),
                        target.z(),
                        identity,
                        item.totalCount());
                routed = true;
            }
            if (routed) {
                routedIdentities.add(item.identity());
            }
        }
        if (routesByStorage.isEmpty()) {
            return PutAwayRouteProjection.empty();
        }
        ArrayList<WayfindingTarget> targetsOut = new ArrayList<>(routesByStorage.size());
        for (PutAwayRouteAccumulator route : routesByStorage.values()) {
            targetsOut.add(route.toTarget());
        }
        return new PutAwayRouteProjection(routedIdentities, List.copyOf(targetsOut));
    }

    private static void collectPutAwayRouteCandidates(
            List<AtlasItem> output,
            List<AtlasItem> items,
            WorkflowTabTargets.Resolution targets,
            Set<ItemIdentity> activationPutAwayIdentities
    ) {
        if (output == null || items == null || items.isEmpty() || targets == null) {
            return;
        }
        for (AtlasItem item : items) {
            if (activeWorkflowPutAwayCandidate(item, targets, activationPutAwayIdentities)) {
                output.add(item);
            }
        }
    }

    private static boolean activeWorkflowPutAwayCandidate(
            AtlasItem item,
            WorkflowTabTargets.Resolution targets,
            Set<ItemIdentity> activationPutAwayIdentities
    ) {
        if (item == null
                || !item.carried()
                || item.identity() == null
                || targets == null
                || activationPutAwayIdentities == null
                || activationPutAwayIdentities.isEmpty()) {
            return false;
        }
        if (protectedPutAwaySource(item.largestCarriedSourceId())) {
            return false;
        }
        ItemIdentity identity = item.identity().toIdentity();
        return identity != null
                && ItemIdentityCollections.containsCanonical(activationPutAwayIdentities, identity)
                && !targets.workflowRelevant(identity, ItemStackTags.itemTagIds(item.displayStack()));
    }

    private static Set<String> claimedStorageIds(ClaimedChestMap claimedChestMap) {
        if (claimedChestMap == null || claimedChestMap.chests().isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<String> ids = new LinkedHashSet<>();
        for (ClaimedChest chest : claimedChestMap.chests()) {
            if (chest != null && chest.role().quickDepositTarget()) {
                ids.add(chest.storageId().toString());
            }
        }
        return ids.isEmpty() ? Set.of() : Set.copyOf(ids);
    }

    private static List<WorkspaceStorageIndex.StorageEntry> displayPutAwayRouteEntries(
            Collection<WorkspaceStorageIndex.StorageEntry> trackedDisplayEntries,
            ItemIdentity identity
    ) {
        if (trackedDisplayEntries == null || trackedDisplayEntries.isEmpty() || identity == null) {
            return List.of();
        }
        ArrayList<WorkspaceStorageIndex.StorageEntry> entries = new ArrayList<>();
        for (WorkspaceStorageIndex.StorageEntry entry : trackedDisplayEntries) {
            if (entry == null || entry.target() == null
                    || !entry.target().displayTarget()
                    || !entry.target().depositTarget()
                    || entry.target().displayKind() == null
                    || !entry.target().displayKind().trackedStorage()) {
                continue;
            }
            if (displayEntryContainsMatchingContent(entry, identity)) {
                entries.add(entry);
            }
        }
        return entries.isEmpty() ? List.of() : List.copyOf(entries);
    }

    private static boolean displayEntryContainsMatchingContent(
            WorkspaceStorageIndex.StorageEntry entry,
            ItemIdentity identity
    ) {
        if (entry == null || identity == null || entry.snapshot() == null) {
            return false;
        }
        for (ItemStack stack : entry.snapshot().contents()) {
            if (stack != null && !stack.isEmpty()
                    && ItemIdentityMatcher.matchesMovable(stack, identity)) {
                return true;
            }
        }
        return false;
    }

    private static void addPutAwayRoute(
            Map<String, PutAwayRouteAccumulator> routesByStorage,
            String storageId,
            String dimensionId,
            int worldX,
            int worldY,
            int worldZ,
            ItemIdentity identity,
            int count
    ) {
        if (routesByStorage == null || storageId == null || storageId.isBlank() || identity == null) {
            return;
        }
        routesByStorage.computeIfAbsent(
                        storageId,
                        ignored -> new PutAwayRouteAccumulator(storageId, dimensionId, worldX, worldY, worldZ))
                .add(identity, count);
    }

    private static List<WayfindingTarget> mergeWayfindingTargets(
            List<WayfindingTarget> acquisitionTargets,
            List<WayfindingTarget> putAwayTargets
    ) {
        if ((acquisitionTargets == null || acquisitionTargets.isEmpty())
                && (putAwayTargets == null || putAwayTargets.isEmpty())) {
            return List.of();
        }
        LinkedHashMap<String, WayfindingTargetAccumulator> merged = new LinkedHashMap<>();
        mergeWayfindingTargetsInto(merged, acquisitionTargets);
        mergeWayfindingTargetsInto(merged, putAwayTargets);
        ArrayList<WayfindingTarget> out = new ArrayList<>(merged.size());
        for (WayfindingTargetAccumulator accumulator : merged.values()) {
            out.add(accumulator.toTarget());
        }
        return out.isEmpty() ? List.of() : List.copyOf(out);
    }

    private static void mergeWayfindingTargetsInto(
            Map<String, WayfindingTargetAccumulator> merged,
            List<WayfindingTarget> targets
    ) {
        if (merged == null || targets == null || targets.isEmpty()) {
            return;
        }
        for (WayfindingTarget target : targets) {
            if (target == null || target.storageId().isBlank()) {
                continue;
            }
            merged.computeIfAbsent(target.storageId(), ignored -> new WayfindingTargetAccumulator(target))
                    .add(target);
        }
    }

    private static Set<IdentityRef> unionIdentityRefs(Set<IdentityRef> left, Set<IdentityRef> right) {
        if ((left == null || left.isEmpty()) && (right == null || right.isEmpty())) {
            return Set.of();
        }
        LinkedHashSet<IdentityRef> out = new LinkedHashSet<>();
        if (left != null) {
            out.addAll(left);
        }
        if (right != null) {
            out.addAll(right);
        }
        return out.isEmpty() ? Set.of() : Set.copyOf(out);
    }

    private static boolean hasFetchGap(AtlasItem item) {
        if (item == null) {
            return false;
        }
        int carried = item.carried() ? Math.max(0, item.totalCount()) : 0;
        return WorkspaceItemTargets.from(item).hasAnyGap(carried);
    }

    private static ArrayList<AtlasItem> filterForActiveWorkflowTab(
            List<AtlasItem> items,
            WorkflowTabTargets.Resolution targets,
            Set<ItemIdentity> activationPutAwayIdentities,
            Set<IdentityRef> routedPutAwayIdentities,
            String searchQuery,
            Map<String, AtlasIsland> islandsById
    ) {
        if (items == null || items.isEmpty() || targets == null) {
            return new ArrayList<>();
        }
        Set<IdentityRef> putAway = routedPutAwayIdentities == null ? Set.of() : routedPutAwayIdentities;
        String normalizedSearch = WorkspaceSearchQuery.normalized(searchQuery);
        boolean searchActive = !normalizedSearch.isBlank();
        ArrayList<AtlasItem> filtered = new ArrayList<>(items.size());
        for (AtlasItem item : items) {
            if (item == null || item.identity() == null) {
                continue;
            }
            ItemIdentity identity = item.identity().toIdentity();
            Set<String> itemTags = ItemStackTags.itemTagIds(item.displayStack());
            boolean acceptedInput = targets.acceptedInput(identity, itemTags);
            boolean workflowRelevant = targets.workflowRelevant(identity, itemTags);
            boolean storageGhost = !item.carried() && (item.proximateCount() > 0 || !item.elsewhere().isEmpty());
            boolean carried = item.carried();
            boolean putAwayCandidate = activeWorkflowPutAwayCandidate(item, targets, activationPutAwayIdentities);
            boolean searchMatch = searchActive && WorkspaceSearchQuery.matchesItem(
                    normalizedSearch,
                    item,
                    islandsById == null ? null : islandsById.get(item.islandId()));
            if (identity != null && (carried || workflowRelevant || putAwayCandidate || storageGhost || searchMatch)) {
                AtlasItem visibleItem = item.withAcceptedWorkflowInput(acceptedInput);
                filtered.add(putAwayCandidate
                        ? visibleItem.withPutAwayState(
                                putAway.contains(item.identity()) ? PutAwayState.ROUTED : PutAwayState.NO_ROUTE)
                        : visibleItem.withPutAwayState(PutAwayState.NONE));
            }
        }
        return filtered;
    }

    private static Map<String, AtlasIsland> islandsById(List<AtlasIsland> islands) {
        if (islands == null || islands.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, AtlasIsland> byId = new LinkedHashMap<>();
        for (AtlasIsland island : islands) {
            if (island != null && island.islandId() != null && !island.islandId().isBlank()) {
                byId.putIfAbsent(island.islandId(), island);
            }
        }
        return byId.isEmpty() ? Map.of() : Map.copyOf(byId);
    }

    private static List<AtlasItem> activeWorkflowPutAwayItems(
            List<AtlasItem> atlasItems,
            List<AtlasItem> triageItems,
            WorkflowTabTargets.Resolution targets,
            Set<ItemIdentity> activationPutAwayIdentities,
            Set<IdentityRef> routedPutAwayIdentities
    ) {
        if (targets == null || activationPutAwayIdentities == null || activationPutAwayIdentities.isEmpty()) {
            return List.of();
        }
        ArrayList<AtlasItem> items = new ArrayList<>();
        collectActiveWorkflowPutAwayItems(items, atlasItems, targets, activationPutAwayIdentities, routedPutAwayIdentities);
        collectActiveWorkflowPutAwayItems(items, triageItems, targets, activationPutAwayIdentities, routedPutAwayIdentities);
        return items.isEmpty() ? List.of() : List.copyOf(items);
    }

    private static void collectActiveWorkflowPutAwayItems(
            List<AtlasItem> output,
            List<AtlasItem> items,
            WorkflowTabTargets.Resolution targets,
            Set<ItemIdentity> activationPutAwayIdentities,
            Set<IdentityRef> routedPutAwayIdentities
    ) {
        if (output == null || items == null || items.isEmpty() || targets == null) {
            return;
        }
        Set<IdentityRef> routed = routedPutAwayIdentities == null ? Set.of() : routedPutAwayIdentities;
        for (AtlasItem item : items) {
            if (!activeWorkflowPutAwayCandidate(item, targets, activationPutAwayIdentities)) {
                continue;
            }
            output.add(item.withPutAwayState(
                    routed.contains(item.identity()) ? PutAwayState.ROUTED : PutAwayState.NO_ROUTE));
        }
    }

    private static boolean protectedPutAwaySource(String sourceId) {
        if (sourceId == null || sourceId.isBlank()) {
            return false;
        }
        return BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0.equals(sourceId)
                || BuiltinInventoryIds.PLAYER_OFFHAND.equals(sourceId)
                || BuiltinInventoryIds.PLAYER_ARMOR.equals(sourceId);
    }

    private static void addContextualSuggestionStorageGhosts(
            List<AtlasItem> candidates,
            ProximateGhostProjection suggestionGhosts,
            Set<ItemIdentity> recentIdentities,
            Set<ItemIdentity> kitNeededIdentities,
            Map<ItemIdentity, Integer> desiredCounts,
            Set<ItemIdentity> desiredFromWorkflowTab,
            Map<ItemIdentity, Integer> wantedCounts,
            VisualHomeMap visualHomeMap
    ) {
        if (candidates == null || suggestionGhosts == null || suggestionGhosts.totalsByIdentity().isEmpty()) {
            return;
        }
        LinkedHashMap<ItemIdentity, AtlasItem> existing = new LinkedHashMap<>();
        for (AtlasItem item : candidates) {
            if (item != null && item.identity() != null) {
                existing.putIfAbsent(ItemIdentityCollections.key(item.identity().toIdentity()), item);
            }
        }
        for (Map.Entry<ItemIdentity, Integer> entry : suggestionGhosts.totalsByIdentity().entrySet()) {
            ItemIdentity identity = entry.getKey();
            if (identity == null) {
                continue;
            }
            List<ChestPresenceEntry> presence =
                    suggestionGhosts.presenceByIdentity().getOrDefault(identity, List.of());
            int proximateCount = entry.getValue();
            if (proximateCount <= 0 || presence.isEmpty()) {
                continue;
            }
            AtlasItem base = ItemIdentityCollections.findCanonical(existing, identity);
            if (base != null && (base.carried() || base.proximateCount() > 0 || !base.presence().isEmpty())) {
                continue;
            }
            ItemStack displayStack = base == null
                    ? suggestionGhosts.displayStackByIdentity().get(identity)
                    : base.displayStack();
            if (displayStack == null || displayStack.isEmpty()) {
                continue;
            }
            VisualHomeAssignment assignment = visualHomeMap == null ? null : visualHomeMap.assignment(identity);
            String islandId = base == null
                    ? assignment == null ? SlotWorkspaceAtlasLayout.ISLAND_TRIAGE : assignment.islandId()
                    : base.islandId();
            boolean playerPlaced = base != null
                    ? base.playerPlaced()
                    : assignment != null
                            && assignment.origin() == dev.imagio.slot.workflow.domain.VisualHomeOrigin.PLAYER_PLACED;
            int desiredCount = base == null
                    ? ItemIdentityCollections.count(desiredCounts, identity)
                    : base.desiredCount();
            boolean desiredFromKit = base == null
                    ? ItemIdentityCollections.containsCanonical(desiredFromWorkflowTab, identity)
                    : base.desiredCountFromKit();
            candidates.add(new AtlasItem(
                    IdentityRef.from(identity),
                    displayStack,
                    base == null ? identity.itemId() : base.name(),
                    Math.max(proximateCount, base == null ? 0 : base.totalCount()),
                    base == null ? 0 : base.firstSlotIndex(),
                    islandId,
                    ItemIdentityCollections.containsCanonical(recentIdentities, identity)
                            || (base != null && base.recent()),
                    playerPlaced,
                    false,
                    true,
                    proximateCount,
                    base == null ? List.of() : base.chipSuggestions(),
                    presence,
                    base == null ? List.of() : base.elsewhere(),
                    false,
                    0,
                    0,
                    base == null
                            ? ItemIdentityCollections.containsCanonical(kitNeededIdentities, identity)
                            : base.kitNeeded(),
                    desiredCount,
                    desiredFromKit,
                    base == null ? ItemIdentityCollections.count(wantedCounts, identity) : base.wantedCount(),
                    base != null && base.junk(),
                    "",
                    -1,
                    0));
        }
    }

    public static Map<ItemIdentity, Integer> activeWantedCounts(
            InventoryAuthoritySnapshot authority,
            Map<ItemIdentity, Integer> playerWantedCounts
    ) {
        return activeWantedCounts(CarriedIdentityCounts.from(authority), playerWantedCounts);
    }

    private static Map<ItemIdentity, Integer> activeWantedCounts(
            CarriedIdentityCounts carriedCounts,
            Map<ItemIdentity, Integer> playerWantedCounts
    ) {
        if (playerWantedCounts == null || playerWantedCounts.isEmpty()) {
            return Map.of();
        }
        CarriedIdentityCounts carried = carriedCounts == null ? CarriedIdentityCounts.empty() : carriedCounts;
        LinkedHashMap<ItemIdentity, Integer> active = new LinkedHashMap<>();
        for (Map.Entry<ItemIdentity, Integer> entry : playerWantedCounts.entrySet()) {
            ItemIdentity identity = entry.getKey();
            if (identity == null) {
                continue;
            }
            int target = entry.getValue() == null ? 0 : Math.max(0, entry.getValue());
            if (target <= 0) {
                continue;
            }
            if (carried.count(identity) < target) {
                ItemIdentityCollections.mergePositive(active, identity, target);
            }
        }
        return active.isEmpty() ? Map.of() : Collections.unmodifiableMap(active);
    }

    private static Map<ItemIdentity, Integer> activeDesiredCounts(
            CarriedIdentityCounts carriedCounts,
            Map<ItemIdentity, Integer> desiredCounts
    ) {
        if (desiredCounts == null || desiredCounts.isEmpty()) {
            return Map.of();
        }
        CarriedIdentityCounts carried = carriedCounts == null ? CarriedIdentityCounts.empty() : carriedCounts;
        LinkedHashMap<ItemIdentity, Integer> active = new LinkedHashMap<>();
        for (Map.Entry<ItemIdentity, Integer> entry : desiredCounts.entrySet()) {
            ItemIdentity identity = entry.getKey();
            if (identity == null) {
                continue;
            }
            int target = entry.getValue() == null ? 0 : entry.getValue();
            if (target <= 0) {
                continue;
            }
            if (carried.count(identity) < target) {
                ItemIdentityCollections.mergePositive(active, identity, target);
            }
        }
        return active.isEmpty() ? Map.of() : Collections.unmodifiableMap(active);
    }

    /**
     * Items the player must keep in carry: at least one of each active
     * kit-page slot identity, plus the resolved desired count (kit
     * scope > player scope), plus active wanted count. The deposit planner caps the depositable
     * total at {@code totalCarried - reserved}, and the deposit-preview
     * uses the same cap so the highlighted set matches what would
     * actually move.
     */
    public static int reservedCarryCount(
            ItemIdentity identity,
            WorkflowTabTargets.Resolution targets
    ) {
        return targets == null ? 0 : targets.reservedCarryCount(identity);
    }

    /**
     * Carried atlas-item identities that have a positive direct affinity
     * score or matching live contents against at least one proximate
     * claimed chest.
     * Drives the "Deposit (N)" button label and the deposit-preview
     * highlight on atlas cards.
     *
     * <p>Only explicit player-taught signals are checked: direct affinity
     * and existing chest contents. Item similarity, classifier facets, and
     * empty-chest fallback are intentionally ignored so the preview matches
     * the server planner.
     */
    private static Set<IdentityRef> depositableIdentities(
            List<AtlasItem> atlasItems,
            ClaimedChestMap claimedChestMap,
            ChestAffinityMap affinityMap,
            Set<String> proximateStorageIds,
            Function<String, ChestContentsSnapshot> chestContentsResolver,
            java.util.function.ToIntFunction<ItemIdentity> reservedCountResolver,
            Set<String> depositEligibleStorageIds,
            DepositPlanner.ChestContentPresence liveChestContentPresence,
            DepositPlanner.ChestEligibility liveStorageAffinityEligibility
    ) {
        if (atlasItems == null || atlasItems.isEmpty()
                || claimedChestMap == null || claimedChestMap.chests().isEmpty()
                || proximateStorageIds == null || proximateStorageIds.isEmpty()) {
            return Set.of();
        }
        Set<String> depositEligible = intersectStorageIds(
                proximateStorageIds,
                depositEligibleStorageIds == null ? proximateStorageIds : depositEligibleStorageIds);
        if (depositEligible.isEmpty()) {
            return Set.of();
        }
        DepositPlanner.ChestContentPresence contentPresence = liveChestContentPresence == null
                ? contentPresenceFromResolver(chestContentsResolver)
                : liveChestContentPresence;
        DepositPlanner.ChestEligibility chestEligibility = liveStorageAffinityEligibility == null
                ? eligibilityFromResolver(chestContentsResolver)
                : liveStorageAffinityEligibility;
        LinkedHashSet<IdentityRef> result = new LinkedHashSet<>();
        for (AtlasItem item : atlasItems) {
            if (!item.carried()) {
                continue;
            }
            ItemIdentity identity = item.identity().toIdentity();
            if (identity == null) {
                continue;
            }
            // Kit / desired-count protection: items the planner would
            // refuse to deposit (because all carried instances are
            // reserved) shouldn't show in the highlight. Without this
            // the deposit-preview lights up an item the click would
            // leave alone.
            if (reservedCountResolver != null) {
                int reserved = Math.max(0, reservedCountResolver.applyAsInt(identity));
                if (item.totalCount() <= reserved) {
                    continue;
                }
            }
            if (!DepositPlanner.rankChestsForIdentity(
                    identity,
                    claimedChestMap,
                    affinityMap,
                    depositEligible,
                    contentPresence,
                    chestEligibility).isEmpty()) {
                result.add(item.identity());
            }
        }
        return Set.copyOf(result);
    }

    private static Set<String> intersectStorageIds(Set<String> left, Set<String> right) {
        if (left == null || left.isEmpty() || right == null || right.isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<String> out = new LinkedHashSet<>();
        for (String value : left) {
            if (value != null && right.contains(value)) {
                out.add(value);
            }
        }
        return out.isEmpty() ? Set.of() : Set.copyOf(out);
    }

    private static DepositPlanner.ChestContentPresence contentPresenceFromResolver(
            Function<String, ChestContentsSnapshot> chestContentsResolver
    ) {
        if (chestContentsResolver == null) {
            return (chest, identity) -> false;
        }
        return (chest, identity) -> {
            if (chest == null || identity == null) {
                return false;
            }
            if (!chest.role().quickDepositTarget()) {
                return false;
            }
            ChestContentsSnapshot snapshot = chestContentsResolver.apply(chest.storageId().toString());
            if (snapshot == null || snapshot.contents().isEmpty()
                    || !StorageAffinityPolicy.isEligibleSlotCount(snapshot.slotCount())) {
                return false;
            }
            for (ItemStack stack : snapshot.contents()) {
                if (stack != null && !stack.isEmpty()
                        && ItemIdentityMatcher.matchesMovable(stack, identity)) {
                    return true;
                }
            }
            return false;
        };
    }

    private static DepositPlanner.ChestEligibility eligibilityFromResolver(
            Function<String, ChestContentsSnapshot> chestContentsResolver
    ) {
        if (chestContentsResolver == null) {
            return chest -> true;
        }
        return chest -> {
            if (chest == null) {
                return false;
            }
            ChestContentsSnapshot snapshot = chestContentsResolver.apply(chest.storageId().toString());
            return snapshot != null
                    && chest.role().quickDepositTarget()
                    && StorageAffinityPolicy.isEligibleSlotCount(snapshot.slotCount());
        };
    }

    /**
     * Build the atlas-side loot panel: one Triage-style {@link AtlasItem}
     * per unique identity in {@code source}, with chip suggestions for
     * unhomed identities (via the same signal-extractor + suggestion
     * pipeline as Triage). Returns {@link LootChestPanel#empty()} when
     * the source is null/empty so the panel disappears.
     */
    private static LootChestPanel lootChestPanel(
            LootChestSource source,
            VisualHomeMap visualHomeMap,
            Function<ItemStack, IslandSignalDescriptor> signalExtractor,
            LearnedIslandRuleStore learnedRules,
            List<TriageIslandRef> triageIslandRefs,
            Predicate<String> subsystemQualifier,
            Predicate<String> organizationGroupQualifier
    ) {
        if (source == null || source.contents() == null || source.contents().isEmpty()) {
            return LootChestPanel.empty();
        }
        // Group by identity, picking the first-seen stack as the display
        // and the first non-empty slot index as the take-from-chest hint.
        LinkedHashMap<ItemIdentity, ItemStack> displayByIdentity = new LinkedHashMap<>();
        LinkedHashMap<ItemIdentity, Integer> totals = new LinkedHashMap<>();
        LinkedHashMap<ItemIdentity, Integer> firstSlotByIdentity = new LinkedHashMap<>();
        int slotIndex = 0;
        for (ItemStack stack : source.contents()) {
            if (stack == null || stack.isEmpty()) {
                slotIndex++;
                continue;
            }
            ItemIdentity identity = ItemIdentityMatcher.normalizeMovable(ItemIdentityMatcher.create(stack));
            displayByIdentity.putIfAbsent(identity, stack.copy());
            totals.merge(identity, stack.getCount(), Integer::sum);
            firstSlotByIdentity.putIfAbsent(identity, slotIndex);
            slotIndex++;
        }
        ArrayList<AtlasItem> items = new ArrayList<>(displayByIdentity.size());
        for (Map.Entry<ItemIdentity, ItemStack> entry : displayByIdentity.entrySet()) {
            ItemIdentity identity = entry.getKey();
            ItemStack stack = entry.getValue();
            int total = totals.getOrDefault(identity, 0);
            int firstSlot = firstSlotByIdentity.getOrDefault(identity, 0);
            VisualHomeAssignment assignment = visualHomeMap.assignment(identity);
            String islandId = assignment == null ? SlotWorkspaceAtlasLayout.ISLAND_TRIAGE : assignment.islandId();
            boolean playerPlaced = assignment != null
                    && assignment.origin() == dev.imagio.slot.workflow.domain.VisualHomeOrigin.PLAYER_PLACED;
            List<ChipSuggestion> chips = List.of();
            if (assignment == null && signalExtractor != null) {
                IslandSignalDescriptor descriptor = signalExtractor.apply(stack);
                if (descriptor != null) {
                    chips = IslandSuggestionService.suggest(
                            descriptor,
                            learnedRules == null ? new LearnedIslandRuleStore() : learnedRules,
                            triageIslandRefs == null ? List.of() : triageIslandRefs,
                            visualHomeMap.dismissedTemplateIds(),
                            subsystemQualifier,
                            organizationGroupQualifier
                    );
                }
            }
            String name = stack.getHoverName().getString();
            items.add(new AtlasItem(
                    IdentityRef.from(identity),
                    stack.copy(),
                    name,
                    total,
                    firstSlot,
                    islandId,
                    false,
                    playerPlaced,
                    false,
                    false,
                    0,
                    chips,
                    List.of(),
                    List.of(),
                    false,
                    0,
                    0,
                    false
            ));
        }
        items.sort(Comparator
                .comparing((AtlasItem item) -> item.islandId().equals(SlotWorkspaceAtlasLayout.ISLAND_TRIAGE) ? 1 : 0)
                .thenComparing(item -> item.name().toLowerCase(Locale.ROOT))
                .thenComparing(item -> item.identity().itemId()));
        return new LootChestPanel(
                source.chestX(),
                source.chestY(),
                source.chestZ(),
                source.dimensionId(),
                source.label(),
                List.copyOf(items)
        );
    }

    private static List<ChestClusterDescriptor> chestClusterDescriptors(
            ChestClusterMap clusterMap,
            Map<String, String> customLabels
    ) {
        if (clusterMap == null || clusterMap.clusters().isEmpty()) {
            return List.of();
        }
        Map<String, String> labels = customLabels == null ? Map.of() : customLabels;
        ArrayList<ChestClusterDescriptor> out = new ArrayList<>(clusterMap.clusters().size());
        for (ChestClusterMap.Cluster cluster : clusterMap.clusters()) {
            String custom = labels.get(cluster.clusterId());
            String label = custom == null || custom.isBlank() ? cluster.defaultLabel() : custom;
            out.add(new ChestClusterDescriptor(
                    cluster.clusterId(),
                    label,
                    cluster.ordinal()
            ));
        }
        return List.copyOf(out);
    }

    private static int[] countCarriedFreeSlotsAndCapacity(InventoryAuthoritySnapshot authority) {
        if (authority == null) {
            return new int[]{0, 0};
        }
        int free = 0;
        int capacityTotal = 0;
        for (InventorySourceDescriptor source : authority.carriedSources()) {
            if (source == null) {
                continue;
            }
            int capacity = authority.slotCapacity(source.id());
            if (capacity <= 0) {
                continue;
            }
            int present = 0;
            for (InventoryEntrySnapshot entry : authority.entries(source.id())) {
                if (entry != null && entry.present() && entry.slotIndex() >= 0) {
                    present++;
                }
            }
            free += Math.max(0, capacity - present);
            capacityTotal += capacity;
        }
        return new int[]{free, capacityTotal};
    }

    private static List<KitCard> kitCards(
            CarriedIdentityCounts carriedCounts,
            KitMap kitMap,
            Map<String, Map<ItemIdentity, Integer>> kitDesiredCounts
    ) {
        if (kitMap == null || kitMap.kits().isEmpty()) {
            return List.of();
        }
        CarriedIdentityCounts carried = carriedCounts == null ? CarriedIdentityCounts.empty() : carriedCounts;
        KitActivation activation = kitMap.activation();
        ArrayList<KitCard> result = new ArrayList<>(kitMap.kits().size());
        for (KitDefinition kit : kitMap.kits()) {
            if (kit == null || kit.id().isBlank()) {
                continue;
            }
            boolean active = activation.isActive() && activation.kitId().equals(kit.id());
            KitDefinition pageOwner = pageOwner(kitMap, kit);
            int renderedPage = active ? Math.max(0, Math.min(activation.pageIndex(), pageOwner.pageCount() - 1)) : 0;
            ArrayList<KitPageView> pages = new ArrayList<>(pageOwner.pageCount());
            for (int pageIndex = 0; pageIndex < pageOwner.pageCount(); pageIndex++) {
                KitPage page = pageOwner.page(pageIndex);
                if (page == null) {
                    continue;
                }
                ArrayList<KitSlotState> pageSlots = new ArrayList<>(KitPage.HOTBAR_SLOT_COUNT);
                int pageReady = 0;
                for (int slotIndex = 0; slotIndex < KitPage.HOTBAR_SLOT_COUNT; slotIndex++) {
                    ItemIdentity identity = page.slot(slotIndex);
                    boolean filled = identity != null;
                    boolean present = filled && carried.contains(identity);
                    if (filled && present) {
                        pageReady++;
                    }
                    ItemStack stack = filled ? resolveGhostStack(identity) : ItemStack.EMPTY;
                    String name = filled && !stack.isEmpty() ? stack.getHoverName().getString() : filled ? identity.itemId() : "";
                    pageSlots.add(new KitSlotState(
                            slotIndex,
                            filled,
                            present,
                            IdentityRef.from(identity),
                            stack,
                            name
                    ));
                }
                pages.add(new KitPageView(
                        pageIndex,
                        KitPage.HOTBAR_SLOT_COUNT,
                        pageReady,
                        List.copyOf(pageSlots)
                ));
            }
            KitPageView renderedPageView = pages.isEmpty() ? null : pages.get(Math.min(renderedPage, pages.size() - 1));
            List<KitSlotState> renderedSlots = renderedPageView == null ? List.of() : renderedPageView.slots();
            int renderedReady = renderedPageView == null ? 0 : renderedPageView.readyCount();
            // Bring list is now derived from kit-scoped desired counts. Each
            // identity with a non-zero kit-scoped count surfaces as a
            // KitBringItem on the kit card so AtlasRelevance and existing
            // KitCard consumers see the same shape they used to.
            Map<ItemIdentity, Integer> kitWants = kitDesiredCounts == null
                    ? Map.of()
                    : kitDesiredCounts.getOrDefault(kit.id(), Map.of());
            ArrayList<KitBringItem> bringItems = new ArrayList<>(kitWants.size());
            int bringReady = 0;
            for (Map.Entry<ItemIdentity, Integer> entry : kitWants.entrySet()) {
                ItemIdentity identity = entry.getKey();
                if (identity == null || entry.getValue() == null || entry.getValue() <= 0) {
                    continue;
                }
                int target = entry.getValue();
                int presentCount = carried.count(identity);
                boolean present = presentCount >= target;
                if (present) {
                    bringReady++;
                }
                ItemStack stack = resolveGhostStack(identity);
                String name = !stack.isEmpty() ? stack.getHoverName().getString() : identity.itemId();
                bringItems.add(new KitBringItem(
                        IdentityRef.from(identity),
                        present,
                        stack,
                        name,
                        presentCount,
                        target
                ));
            }
            result.add(new KitCard(
                    kit.id(),
                    kit.name(),
                    kit.parentId(),
                    pageOwner.pageCount(),
                    renderedPage,
                    active,
                    kit.variant(),
                    kit.members().size(),
                    kit.members().stream().map(IdentityRef::from).toList(),
                    kit.acceptedInputs().stream().toList(),
                    KitPage.HOTBAR_SLOT_COUNT,
                    renderedReady,
                    pageOwner.carriedSlotCount(),
                    KitDefinition.MAX_CARRIED_CAPACITY,
                    bringItems.size(),
                    bringReady,
                    renderedSlots,
                    List.copyOf(pages),
                    List.copyOf(bringItems)
            ));
        }
        return List.copyOf(result);
    }

    private static KitDefinition pageOwner(KitMap kitMap, KitDefinition kit) {
        if (kit == null || !kit.variant() || hasExplicitBeltPage(kit)) {
            return kit;
        }
        KitDefinition parent = kitMap == null ? null : kitMap.kit(kit.parentId());
        return parent == null ? kit : parent;
    }

    private static boolean hasExplicitBeltPage(KitDefinition kit) {
        if (kit == null) {
            return false;
        }
        if (kit.offhand() != null) {
            return true;
        }
        for (KitPage page : kit.pages()) {
            if (page != null && page.filledSlotCount() > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Build per-storage wayfinding targets: chests/displays holding at least one
     * identity the player still needs (active kit page slot, kit-scoped
     * desired-count gap, player-global desired-count gap, or active
     * wanted-count gap). Drives the client-side wayfinding HUD + atlas chip +
     * in-world storage glow.
     *
     * <p>The "missing identity" set unions kit-needed (already
     * carry-aware) with desired and wanted gaps where {@code carriedCount}
     * has not reached the relevant target. Source-specific sets remain
     * separate on {@link WayfindingTarget}.
     */
    private static List<WayfindingTarget> wayfindingTargets(
            CarriedIdentityCounts carriedCounts,
            ClaimedChestMap claimedChestMap,
            Function<String, ChestContentsSnapshot> chestContentsResolver,
            Collection<WorkspaceStorageIndex.StorageEntry> trackedDisplayEntries,
            Set<ItemIdentity> kitNeededIdentities,
            Map<ItemIdentity, Integer> wantedCounts,
            Map<ItemIdentity, Integer> desiredCounts,
            Set<ItemIdentity> desiredFromWorkflowTab
    ) {
        boolean hasClaimedChests = claimedChestMap != null && !claimedChestMap.chests().isEmpty();
        boolean hasDisplayEntries = trackedDisplayEntries != null && !trackedDisplayEntries.isEmpty();
        if ((!hasClaimedChests && !hasDisplayEntries) || chestContentsResolver == null) {
            return List.of();
        }
        CarriedIdentityCounts carried = carriedCounts == null ? CarriedIdentityCounts.empty() : carriedCounts;
        // Build the missing-identity source map in one pass. Desired and
        // wanted state stay distinct here; display code may collapse them,
        // but downstream logic can still tell why a chest is being surfaced.
        LinkedHashMap<ItemIdentity, WayfindingNeedSources> missingSources = new LinkedHashMap<>();
        for (ItemIdentity identity : kitNeededIdentities) {
            if (identity != null) {
                missingSources.computeIfAbsent(ItemIdentityCollections.key(identity), ignored -> new WayfindingNeedSources()).kit = true;
            }
        }
        if (desiredCounts != null) {
            for (Map.Entry<ItemIdentity, Integer> entry : desiredCounts.entrySet()) {
                ItemIdentity identity = entry.getKey();
                Integer target = entry.getValue();
                if (identity == null || target == null || target <= 0) {
                    continue;
                }
                if (carried.count(identity) < target) {
                    WayfindingNeedSources sources = missingSources.computeIfAbsent(
                            ItemIdentityCollections.key(identity),
                            ignored -> new WayfindingNeedSources());
                    sources.desired = true;
                    if (ItemIdentityCollections.contains(desiredFromWorkflowTab, identity)) {
                        sources.kit = true;
                    }
                }
            }
        }
        if (wantedCounts != null) {
            for (Map.Entry<ItemIdentity, Integer> entry : wantedCounts.entrySet()) {
                ItemIdentity identity = entry.getKey();
                Integer target = entry.getValue();
                if (identity != null && target != null && target > 0
                        && carried.count(identity) < target) {
                    missingSources.computeIfAbsent(ItemIdentityCollections.key(identity), ignored -> new WayfindingNeedSources()).wanted = true;
                }
            }
        }
        if (missingSources.isEmpty()) {
            return List.of();
        }
        ArrayList<WayfindingTarget> targets = new ArrayList<>();
        if (hasClaimedChests) {
            for (ClaimedChest chest : claimedChestMap.chests()) {
                if (chest == null || chest.anchors().isEmpty() || !chest.role().visibleToWorkspace()) {
                    continue;
                }
                String storageId = chest.storageId().toString();
                ChestContentsSnapshot snapshot = chestContentsResolver.apply(storageId);
                ChestAnchor primary = chest.anchors().iterator().next();
                WayfindingTarget target = wayfindingTargetForStorage(
                        storageId,
                        primary.dimensionId(),
                        primary.x(),
                        primary.y(),
                        primary.z(),
                        snapshot,
                        missingSources);
                if (target != null) {
                    targets.add(target);
                }
            }
        }
        if (hasDisplayEntries) {
            for (WorkspaceStorageIndex.StorageEntry entry : trackedDisplayEntries) {
                if (entry == null || entry.target() == null || !entry.target().displayTarget()) {
                    continue;
                }
                StorageTargetRef targetRef = entry.target();
                if (targetRef.displayKind() == null || !targetRef.displayKind().trackedStorage()) {
                    continue;
                }
                ChestContentsSnapshot snapshot = entry.snapshot();
                if (snapshot == null) {
                    snapshot = chestContentsResolver.apply(targetRef.storageId());
                }
                WayfindingTarget target = wayfindingTargetForStorage(
                        targetRef.storageId(),
                        targetRef.dimensionId(),
                        targetRef.x(),
                        targetRef.y(),
                        targetRef.z(),
                        snapshot,
                        missingSources);
                if (target != null) {
                    targets.add(target);
                }
            }
        }
        return List.copyOf(targets);
    }

    private static WayfindingTarget wayfindingTargetForStorage(
            String storageId,
            String dimensionId,
            int worldX,
            int worldY,
            int worldZ,
            ChestContentsSnapshot snapshot,
            Map<ItemIdentity, WayfindingNeedSources> missingSources
    ) {
        if (storageId == null || storageId.isBlank()
                || snapshot == null
                || snapshot.contents().isEmpty()
                || missingSources == null
                || missingSources.isEmpty()) {
            return null;
        }
        LinkedHashSet<ItemIdentity> matched = new LinkedHashSet<>();
        LinkedHashSet<ItemIdentity> kitMatched = new LinkedHashSet<>();
        LinkedHashSet<ItemIdentity> desiredMatched = new LinkedHashSet<>();
        LinkedHashSet<ItemIdentity> wantedMatched = new LinkedHashSet<>();
        int totalMissingCount = 0;
        for (ItemStack stack : snapshot.contents()) {
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            for (Map.Entry<ItemIdentity, WayfindingNeedSources> entry : missingSources.entrySet()) {
                ItemIdentity needed = entry.getKey();
                if (!ItemIdentityMatcher.matchesMovable(stack, needed)) {
                    continue;
                }
                WayfindingNeedSources sources = entry.getValue();
                matched.add(needed);
                if (sources.kit) {
                    kitMatched.add(needed);
                }
                if (sources.desired) {
                    desiredMatched.add(needed);
                }
                if (sources.wanted) {
                    wantedMatched.add(needed);
                }
                totalMissingCount += stack.getCount();
                break;
            }
        }
        if (matched.isEmpty()) {
            return null;
        }
        return new WayfindingTarget(
                storageId,
                dimensionId,
                worldX,
                worldY,
                worldZ,
                matched,
                kitMatched,
                desiredMatched,
                wantedMatched,
                totalMissingCount,
                wayfindingScope(kitMatched, desiredMatched, wantedMatched)
        );
    }

    private static WayfindingTarget.Scope wayfindingScope(
            Set<ItemIdentity> kitMatched,
            Set<ItemIdentity> desiredMatched,
            Set<ItemIdentity> wantedMatched
    ) {
        if (kitMatched != null && !kitMatched.isEmpty()) {
            return WayfindingTarget.Scope.KIT;
        }
        if ((desiredMatched == null || desiredMatched.isEmpty())
                && wantedMatched != null && !wantedMatched.isEmpty()) {
            return WayfindingTarget.Scope.WANTED;
        }
        return WayfindingTarget.Scope.PLAYER;
    }

    private static final class WayfindingNeedSources {
        private boolean kit;
        private boolean desired;
        private boolean wanted;
    }

    private record PutAwayRouteProjection(
            Set<IdentityRef> routedIdentities,
            List<WayfindingTarget> targets
    ) {
        private PutAwayRouteProjection {
            routedIdentities = routedIdentities == null
                    ? Set.of()
                    : Collections.unmodifiableSet(new LinkedHashSet<>(routedIdentities));
            targets = targets == null ? List.of() : List.copyOf(targets);
        }

        static PutAwayRouteProjection empty() {
            return new PutAwayRouteProjection(Set.of(), List.of());
        }
    }

    private static final class PutAwayRouteAccumulator {
        private final String storageId;
        private final String dimensionId;
        private final int worldX;
        private final int worldY;
        private final int worldZ;
        private final LinkedHashSet<ItemIdentity> identities = new LinkedHashSet<>();
        private int totalCount;

        private PutAwayRouteAccumulator(
                String storageId,
                String dimensionId,
                int worldX,
                int worldY,
                int worldZ
        ) {
            this.storageId = storageId == null ? "" : storageId;
            this.dimensionId = dimensionId == null ? "" : dimensionId;
            this.worldX = worldX;
            this.worldY = worldY;
            this.worldZ = worldZ;
        }

        private void add(ItemIdentity identity, int count) {
            if (identity == null) {
                return;
            }
            identities.add(ItemIdentityCollections.key(identity));
            totalCount += Math.max(0, count);
        }

        private WayfindingTarget toTarget() {
            Set<ItemIdentity> putAway = Collections.unmodifiableSet(new LinkedHashSet<>(identities));
            return new WayfindingTarget(
                    storageId,
                    dimensionId,
                    worldX,
                    worldY,
                    worldZ,
                    Set.of(),
                    Set.of(),
                    Set.of(),
                    Set.of(),
                    putAway,
                    totalCount,
                    WayfindingTarget.Scope.PUT_AWAY);
        }
    }

    private static final class WayfindingTargetAccumulator {
        private final String storageId;
        private final String dimensionId;
        private final int worldX;
        private final int worldY;
        private final int worldZ;
        private final LinkedHashSet<ItemIdentity> missingIdentities = new LinkedHashSet<>();
        private final LinkedHashSet<ItemIdentity> kitMissingIdentities = new LinkedHashSet<>();
        private final LinkedHashSet<ItemIdentity> desiredMissingIdentities = new LinkedHashSet<>();
        private final LinkedHashSet<ItemIdentity> wantedMissingIdentities = new LinkedHashSet<>();
        private final LinkedHashSet<ItemIdentity> putAwayIdentities = new LinkedHashSet<>();
        private int totalMissingCount;

        private WayfindingTargetAccumulator(WayfindingTarget target) {
            this.storageId = target == null ? "" : target.storageId();
            this.dimensionId = target == null ? "" : target.dimensionId();
            this.worldX = target == null ? 0 : target.worldX();
            this.worldY = target == null ? 0 : target.worldY();
            this.worldZ = target == null ? 0 : target.worldZ();
        }

        private void add(WayfindingTarget target) {
            if (target == null) {
                return;
            }
            addAll(missingIdentities, target.missingIdentities());
            addAll(kitMissingIdentities, target.kitMissingIdentities());
            addAll(desiredMissingIdentities, target.desiredMissingIdentities());
            addAll(wantedMissingIdentities, target.wantedMissingIdentities());
            addAll(putAwayIdentities, target.putAwayIdentities());
            totalMissingCount += Math.max(0, target.totalMissingCount());
        }

        private WayfindingTarget toTarget() {
            return new WayfindingTarget(
                    storageId,
                    dimensionId,
                    worldX,
                    worldY,
                    worldZ,
                    missingIdentities,
                    kitMissingIdentities,
                    desiredMissingIdentities,
                    wantedMissingIdentities,
                    putAwayIdentities,
                    totalMissingCount,
                    null);
        }

        private static void addAll(Set<ItemIdentity> out, Set<ItemIdentity> identities) {
            if (out == null || identities == null || identities.isEmpty()) {
                return;
            }
            for (ItemIdentity identity : identities) {
                if (identity != null) {
                    out.add(ItemIdentityCollections.key(identity));
                }
            }
        }
    }

    /**
     * Sum of stack counts in carry whose identity matches {@code identity}
     * under movable-aware semantics. Drives the kit-bring "M / N" want-vs-
     * have indicator: targetCount comes from kit-scoped desired counts,
     * presentCount comes from this walk.
     */
    public static int carriedMovableCount(InventoryAuthoritySnapshot authority, ItemIdentity identity) {
        return CarriedIdentityCounts.from(authority).count(identity);
    }

    private static ItemStack resolveGhostStack(ItemIdentity identity) {
        if (identity == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = resolveGhostStackItem(identity.itemId());
        if (!stack.isEmpty()) {
            return stack;
        }
        return ItemStack.EMPTY;
    }

    static ItemStack resolveGhostStack(ItemIdentity identity, int count) {
        ItemStack stack = resolveGhostStack(identity);
        if (stack == null || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack copy = stack.copy();
        copy.setCount(Math.max(1, Math.min(count, Math.max(1, copy.getMaxStackSize()))));
        return copy;
    }

    private static ItemStack resolveGhostStackItem(String itemId) {
        try {
            ItemStack stack = ghostStackResolver.apply(itemId);
            return stack == null ? ItemStack.EMPTY : stack;
        } catch (RuntimeException | LinkageError ignored) {
            return ItemStack.EMPTY;
        }
    }

    public static ItemStack displayStackForIdentity(ItemIdentity identity) {
        return resolveGhostStack(identity);
    }

    private static boolean ensureMiscIsland(ArrayList<AtlasIsland> islands) {
        if (islands == null) {
            return false;
        }
        for (AtlasIsland island : islands) {
            if (island != null && SlotWorkspaceAtlasLayout.ISLAND_MISC.equals(island.islandId())) {
                return true;
            }
        }
        islands.add(new AtlasIsland(
                SlotWorkspaceAtlasLayout.ISLAND_MISC,
                SlotWorkspaceAtlasLayout.ISLAND_MISC_LABEL,
                VisualAtlasIslandKind.PLAYER,
                0,
                0,
                SlotWorkspaceAtlasLayout.ISLAND_MISC_COLOR,
                0,
                0
        ));
        return true;
    }

    private static List<AtlasIsland> withCarriedCounts(List<AtlasIsland> islands, List<AtlasItem> atlasItems) {
        if (islands == null || islands.isEmpty()) {
            return List.of();
        }
        Map<String, Integer> carriedByIsland = new LinkedHashMap<>();
        for (AtlasItem item : atlasItems) {
            if (item.carried()) {
                carriedByIsland.merge(item.islandId(), 1, Integer::sum);
            }
        }
        ArrayList<AtlasIsland> result = new ArrayList<>(islands.size());
        for (AtlasIsland island : islands) {
            result.add(island.withCarriedCount(carriedByIsland.getOrDefault(island.islandId(), 0)));
        }
        return List.copyOf(result);
    }

    public SlotWorkspaceViewModel withIslandHome(String islandId, double homeX, double homeY) {
        if (islandId == null) {
            return this;
        }
        java.util.ArrayList<AtlasIsland> updated = new java.util.ArrayList<>(islands.size());
        boolean changed = false;
        for (AtlasIsland island : islands) {
            if (island.islandId().equals(islandId)
                    && (island.x() != homeX || island.y() != homeY)) {
                updated.add(new AtlasIsland(
                        island.islandId(),
                        island.label(),
                        island.kind(),
                        homeX,
                        homeY,
                        island.color(),
                        island.itemCount(),
                        island.carriedCount()
                ));
                changed = true;
            } else {
                updated.add(island);
            }
        }
        if (!changed) {
            return this;
        }
        return new SlotWorkspaceViewModel(
                revision,
                status,
                diagnostics,
                pendingCount,
                selectedQuickAccessSlot,
                canvasWidth,
                canvasHeight,
                carriedFreeSlotCount,
                carriedSlotCapacity,
                List.copyOf(updated),
                atlasItems,
                triageItems,
                chestChips,
                chestClusters,
                hotbarSlots,
                offhand,
                kits,
                lootChestPanel,
                wayfindingTargets,
                depositableIdentities,
                recentIdentities,
                activeChestPanel,
                craftRun,
                contextualSuggestionLanes
        );
    }

    public SlotWorkspaceViewModel withRevision(long nextRevision) {
        return new SlotWorkspaceViewModel(
                nextRevision,
                status,
                diagnostics,
                pendingCount,
                selectedQuickAccessSlot,
                canvasWidth,
                canvasHeight,
                carriedFreeSlotCount,
                carriedSlotCapacity,
                islands,
                atlasItems,
                triageItems,
                chestChips,
                chestClusters,
                hotbarSlots,
                offhand,
                kits,
                lootChestPanel,
                wayfindingTargets,
                depositableIdentities,
                recentIdentities,
                activeChestPanel,
                craftRun,
                contextualSuggestionLanes
        );
    }

    public record CarriedContainerInfo(int freeSlots, int slotCapacity) {
        public CarriedContainerInfo {
            freeSlots = Math.max(0, freeSlots);
            slotCapacity = Math.max(freeSlots, slotCapacity);
        }
    }

    public KitCard kit(String kitId) {
        if (kitId == null || kitId.isBlank()) {
            return null;
        }
        for (KitCard card : kits) {
            if (card.kitId().equals(kitId)) {
                return card;
            }
        }
        return null;
    }

    public KitCard activeKit() {
        for (KitCard card : kits) {
            if (card.active()) {
                return card;
            }
        }
        return null;
    }

    public ChestChip chestChip(String storageId) {
        if (storageId == null || storageId.isBlank()) {
            return null;
        }
        for (ChestChip chip : chestChips) {
            if (chip.storageId().equals(storageId)) {
                return chip;
            }
        }
        return null;
    }

    private static List<ChestChip> chestChips(
            ClaimedChestMap map,
            ChestAffinityMap affinityMap,
            Function<String, ChestContentsSnapshot> chestContentsResolver,
            Set<String> proximate,
            ChestClusterMap clusterMap,
            List<WorkspaceStorageIndex.StorageEntry> trackedDisplayEntries
    ) {
        boolean hasChests = map != null && !map.chests().isEmpty();
        boolean hasDisplays = trackedDisplayEntries != null && !trackedDisplayEntries.isEmpty();
        if (!hasChests && !hasDisplays) {
            return List.of();
        }
        ArrayList<ChestChip> chips = new ArrayList<>(
                (hasChests ? map.chests().size() : 0)
                        + (hasDisplays ? trackedDisplayEntries.size() : 0));
        if (hasChests) {
            for (ClaimedChest chest : map.chests()) {
                if (chest == null) {
                    continue;
                }
                if (!chest.role().visibleToWorkspace()) {
                    continue;
                }
                ChestAnchor primary = chest.anchors().iterator().next();
                String dimension = primary == null ? "" : primary.dimensionId();
                int worldX = primary == null ? 0 : primary.x();
                int worldY = primary == null ? 0 : primary.y();
                int worldZ = primary == null ? 0 : primary.z();
                String storageId = chest.storageId().toString();
                String label = chest.label() == null || chest.label().isBlank()
                        ? autoLabel(chest)
                        : chest.label();
                ChestContentsSnapshot snapshot = chestContentsResolver == null
                        ? ChestContentsSnapshot.empty()
                        : chestContentsResolver.apply(storageId);
                if (snapshot == null) {
                    snapshot = ChestContentsSnapshot.empty();
                }
                boolean isProximate = proximate.contains(storageId);
                int affinityCount = affinityMap == null ? 0 : affinityMap.forChest(chest.storageId()).size();
                String clusterId = clusterMap == null ? "" : clusterMap.clusterId(chest.storageId());
                chips.add(new ChestChip(
                        storageId,
                        dimension,
                        label,
                        chest.anchors().size(),
                        snapshot.slotCount(),
                        filledSlotCount(snapshot),
                        isProximate,
                        affinityCount,
                        worldX,
                        worldY,
                        worldZ,
                        clusterId == null ? "" : clusterId,
                        contentSummaries(snapshot)
                ));
            }
        }
        if (hasDisplays) {
            for (WorkspaceStorageIndex.StorageEntry entry : trackedDisplayEntries) {
                if (entry == null || entry.target() == null || !entry.target().displayTarget()) {
                    continue;
                }
                StorageTargetRef target = entry.target();
                ChestContentsSnapshot snapshot = entry.snapshot();
                String label = target.label().isBlank()
                        ? target.storageId()
                        : target.label();
                String clusterId = "";
                int affinityCount = 0;
                if (target.displayKind() == null || !target.displayKind().trackedStorage()) {
                    continue;
                }
                if (snapshot == null) {
                    snapshot = ChestContentsSnapshot.empty();
                }
                chips.add(new ChestChip(
                        target.storageId(),
                        target.dimensionId(),
                        label,
                        1,
                        snapshot.slotCount(),
                        filledSlotCount(snapshot),
                        target.proximate(),
                        affinityCount,
                        target.x(),
                        target.y(),
                        target.z(),
                        clusterId,
                        contentSummaries(snapshot)
                ));
            }
        }
        // Cluster ordinal lookup for stable in-cluster grouping during sort.
        java.util.Map<String, Integer> clusterOrdinals = new java.util.HashMap<>();
        if (clusterMap != null) {
            for (ChestClusterMap.Cluster cluster : clusterMap.clusters()) {
                clusterOrdinals.put(cluster.clusterId(), cluster.ordinal());
            }
        }
        chips.sort(Comparator
                .comparing((ChestChip chip) -> !chip.proximate())
                .thenComparingInt(chip -> clusterOrdinals.getOrDefault(chip.clusterId(), Integer.MAX_VALUE))
                .thenComparing(ChestChip::label, String.CASE_INSENSITIVE_ORDER));
        return List.copyOf(chips);
    }

    private static int filledSlotCount(ChestContentsSnapshot snapshot) {
        if (snapshot == null || snapshot.contents().isEmpty()) {
            return 0;
        }
        return snapshot.contents().size();
    }

    private static List<ChestContentSummary> contentSummaries(ChestContentsSnapshot snapshot) {
        if (snapshot == null || snapshot.contents().isEmpty()) {
            return List.of();
        }
        // Roll up slot-by-slot contents into per-identity summaries. Storage
        // chips use this to count matches without re-walking the target.
        LinkedHashMap<ItemIdentity, ChestContentSummary> summaryByIdentity = new LinkedHashMap<>();
        for (ItemStack stack : snapshot.contents()) {
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            ItemIdentity identity = ItemIdentityMatcher.normalizeMovable(ItemIdentityMatcher.create(stack));
            ChestContentSummary existing = summaryByIdentity.get(identity);
            if (existing == null) {
                summaryByIdentity.put(identity, new ChestContentSummary(
                        identity.itemId(),
                        identity.componentFingerprint(),
                        stack.getHoverName().getString(),
                        stack.copy(),
                        stack.getCount()
                ));
            } else {
                summaryByIdentity.put(identity, new ChestContentSummary(
                        existing.itemId(),
                        existing.componentFingerprint(),
                        existing.name(),
                        existing.displayStack(),
                        existing.count() + stack.getCount()
                ));
            }
        }
        return summaryByIdentity.isEmpty() ? List.of() : List.copyOf(summaryByIdentity.values());
    }

    private static String autoLabel(ClaimedChest chest) {
        String hex = chest.storageId().toString();
        int dash = hex.indexOf('-');
        String shortId = dash < 0 ? hex : hex.substring(0, dash);
        if (shortId.length() > 4) {
            shortId = shortId.substring(shortId.length() - 4);
        }
        return "Chest #" + shortId;
    }

    public AtlasItem atlasItem(IdentityRef identityRef) {
        if (identityRef == null) {
            return null;
        }
        for (AtlasItem item : atlasItems) {
            if (item.identity().equals(identityRef)) {
                return item;
            }
        }
        for (AtlasItem item : triageItems) {
            if (item.identity().equals(identityRef)) {
                return item;
            }
        }
        for (ContextualSuggestionLane lane : contextualSuggestionLanes) {
            if (lane == null || lane.items().isEmpty()) {
                continue;
            }
            for (AtlasItem item : lane.items()) {
                if (item.identity().equals(identityRef)) {
                    return item;
                }
            }
        }
        return null;
    }

    public AtlasIsland island(String islandId) {
        return SlotWorkspaceAtlasLayout.island(islands, islandId);
    }

    private static List<AtlasItemAccumulator> groupedAtlasEntries(InventoryAuthoritySnapshot authority) {
        LinkedHashMap<ItemIdentity, AtlasItemAccumulator> byIdentity = new LinkedHashMap<>();
        for (InventorySourceDescriptor source : authority.carriedSources()) {
            for (InventoryEntrySnapshot entry : authority.entries(source.id())) {
                if (entry == null || !entry.present()) {
                    continue;
                }
                ItemIdentity identity = ItemIdentityMatcher.normalizeMovable(ItemIdentityMatcher.create(entry.stack()));
                byIdentity.computeIfAbsent(identity, ignored -> new AtlasItemAccumulator(identity, entry)).add(entry);
            }
        }
        return new ArrayList<>(byIdentity.values());
    }

    private static java.util.function.Function<String, ItemStack> ghostStackResolver = identity -> ItemStack.EMPTY;

    public static void setGhostStackResolver(java.util.function.Function<String, ItemStack> resolver) {
        ghostStackResolver = resolver == null ? id -> ItemStack.EMPTY : resolver;
    }

    private static Map<ItemIdentity, Integer> recentRankByIdentity(RecentView recentView) {
        LinkedHashMap<ItemIdentity, Integer> ranks = new LinkedHashMap<>();
        List<ItemIdentity> visible = recentView == null ? List.of() : recentView.visibleItems();
        for (int index = 0; index < visible.size(); index++) {
            ranks.put(visible.get(index), index);
        }
        return Map.copyOf(ranks);
    }

    private static List<TriageIslandRef> triageIslandRefs(VisualHomeMap visualHomeMap) {
        if (visualHomeMap == null || visualHomeMap.playerIslands().isEmpty()) {
            return List.of();
        }
        ArrayList<TriageIslandRef> refs = new ArrayList<>(visualHomeMap.playerIslands().size());
        for (VisualAtlasIsland island : visualHomeMap.playerIslands()) {
            if (island == null) {
                continue;
            }
            refs.add(new TriageIslandRef(island.id(), island.label(), island.color(), island.iconIdentity()));
        }
        return List.copyOf(refs);
    }

    private static List<HotbarSlot> hotbarSlots(InventoryAuthoritySnapshot authority, int selectedQuickAccessSlot) {
        ArrayList<HotbarSlot> slots = new ArrayList<>(9);
        for (int index = 0; index < 9; index++) {
            InventoryEntrySnapshot entry = authority.slotEntry(BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0, index);
            boolean occupied = entry != null && entry.present();
            ItemStack stack = occupied ? entry.stack().copy() : ItemStack.EMPTY;
            slots.add(new HotbarSlot(index, index == selectedQuickAccessSlot, occupied, stack, occupied ? entry.count() : 0));
        }
        return List.copyOf(slots);
    }

    public static List<HotbarSlot> emptyHotbar() {
        ArrayList<HotbarSlot> slots = new ArrayList<>(9);
        for (int index = 0; index < 9; index++) {
            slots.add(new HotbarSlot(index, false, false, ItemStack.EMPTY, 0));
        }
        return List.copyOf(slots);
    }

    public record IdentityRef(
            String itemId,
            String comparisonMode,
            String componentFingerprint
    ) {
        public IdentityRef {
            itemId = itemId == null ? "" : itemId;
            comparisonMode = comparisonMode == null || comparisonMode.isBlank()
                    ? ItemComparisonMode.ITEM_ID.name()
                    : comparisonMode;
            componentFingerprint = componentFingerprint == null ? "" : componentFingerprint;
        }

        public static IdentityRef from(ItemIdentity identity) {
            return identity == null
                    ? new IdentityRef("", ItemComparisonMode.ITEM_ID.name(), "")
                    : new IdentityRef(identity.itemId(), identity.comparisonMode().name(), identity.componentFingerprint());
        }

        public ItemIdentity toIdentity() {
            if (itemId.isBlank()) {
                return null;
            }
            ItemComparisonMode mode = ItemComparisonMode.ITEM_ID;
            try {
                mode = ItemComparisonMode.valueOf(comparisonMode);
            } catch (IllegalArgumentException ignored) {
            }
            return new ItemIdentity(itemId, mode, componentFingerprint);
        }
    }

    public record AtlasIsland(
            String islandId,
            String label,
            VisualAtlasIslandKind kind,
            double x,
            double y,
            int color,
            int itemCount,
            int carriedCount
    ) {
        public AtlasIsland {
            islandId = islandId == null ? "" : islandId;
            label = label == null || label.isBlank() ? islandId : label;
            kind = kind == null ? VisualAtlasIslandKind.PLAYER : kind;
            itemCount = Math.max(0, itemCount);
            carriedCount = Math.max(0, carriedCount);
        }

        public AtlasIsland(
                String islandId,
                String label,
                VisualAtlasIslandKind kind,
                double x,
                double y,
                int color,
                int itemCount
        ) {
            this(islandId, label, kind, x, y, color, itemCount, 0);
        }

        public AtlasIsland withCarriedCount(int newCarriedCount) {
            return new AtlasIsland(islandId, label, kind, x, y, color, itemCount, newCarriedCount);
        }
    }

    public record ContextualSuggestionLane(
            String id,
            String label,
            List<AtlasItem> items,
            String placeholderText,
            List<ContextualSuggestionDebugInfo> debugInfo
    ) {
        public static final String FETCH = "fetch";
        public static final String USEFUL_NOW = "useful_now";
        public static final String PUT_AWAY = "put_away";

        public ContextualSuggestionLane(String id, String label, List<AtlasItem> items) {
            this(id, label, items, "");
        }

        public ContextualSuggestionLane(String id, String label, List<AtlasItem> items, String placeholderText) {
            this(id, label, items, placeholderText, List.of());
        }

        public ContextualSuggestionLane {
            id = id == null ? "" : id.trim();
            label = label == null || label.isBlank() ? id : label.trim();
            items = items == null ? List.of() : List.copyOf(items);
            placeholderText = placeholderText == null ? "" : placeholderText.trim();
            debugInfo = debugInfo == null ? List.of() : List.copyOf(debugInfo);
        }

        public boolean putAway() {
            return PUT_AWAY.equals(id);
        }

        public boolean fetch() {
            return FETCH.equals(id);
        }

        public boolean forceWayfindingStrip() {
            return fetch() || putAway();
        }

        public boolean displayable() {
            return !items.isEmpty() || !placeholderText.isBlank();
        }

        public ContextualSuggestionDebugInfo debugInfoFor(AtlasItem item) {
            if (item == null || item.identity() == null || debugInfo.isEmpty()) {
                return null;
            }
            for (ContextualSuggestionDebugInfo info : debugInfo) {
                if (info != null && item.identity().equals(info.identity())) {
                    return info;
                }
            }
            return null;
        }
    }

    public record ContextualSuggestionDebugInfo(
            IdentityRef identity,
            double score,
            double relevance,
            List<String> reasons
    ) {
        public ContextualSuggestionDebugInfo {
            identity = identity == null ? new IdentityRef("", ItemComparisonMode.ITEM_ID.name(), "") : identity;
            score = Double.isFinite(score) ? score : 0D;
            relevance = Double.isFinite(relevance) ? relevance : 0D;
            reasons = reasons == null ? List.of() : List.copyOf(reasons.stream()
                    .filter(reason -> reason != null && !reason.isBlank())
                    .map(String::trim)
                    .toList());
        }
    }

    public enum PutAwayState {
        NONE,
        ROUTED,
        NO_ROUTE;

        public static PutAwayState parse(String value) {
            if (value == null || value.isBlank()) {
                return NONE;
            }
            try {
                return PutAwayState.valueOf(value);
            } catch (IllegalArgumentException ignored) {
                return NONE;
            }
        }

        public boolean active() {
            return this != NONE;
        }

        public boolean routed() {
            return this == ROUTED;
        }

        public boolean noRoute() {
            return this == NO_ROUTE;
        }
    }

    /**
     * Per-identity atlas projection.
     *
     * <p>{@code ghost} = the player isn't carrying this; the card represents
     * stock present in some proximate chest. Renders faded with storage
     * presence shown separately from the carried-count badge. Hover/zoom
     * reveals per-chest breakdown via {@code presence}.
     */
    public record AtlasItem(
            IdentityRef identity,
            ItemStack displayStack,
            String name,
            int totalCount,
            int firstSlotIndex,
            String islandId,
            boolean recent,
            boolean playerPlaced,
            boolean carried,
            boolean ghost,
            int proximateCount,
            List<ChipSuggestion> chipSuggestions,
            List<ChestPresenceEntry> presence,
            List<ChestPresenceEntry> elsewhere,
            boolean isCarriedContainer,
            int containerFreeSlotCount,
            int containerSlotCapacity,
            boolean kitNeeded,
            int desiredCount,
            boolean desiredCountFromKit,
            int wantedCount,
            boolean junk,
            boolean acceptedWorkflowInput,
            String largestCarriedSourceId,
            int largestCarriedSlotIndex,
            int largestCarriedSlotCount,
            PutAwayState putAwayState
    ) {
        public AtlasItem {
            identity = identity == null ? new IdentityRef("", ItemComparisonMode.ITEM_ID.name(), "") : identity;
            displayStack = displayStack == null ? ItemStack.EMPTY : displayStack.copy();
            name = name == null || name.isBlank() ? identity.itemId() : name;
            totalCount = Math.max(0, totalCount);
            firstSlotIndex = Math.max(0, firstSlotIndex);
            islandId = islandId == null ? "" : islandId;
            proximateCount = Math.max(0, proximateCount);
            chipSuggestions = chipSuggestions == null ? List.of() : List.copyOf(chipSuggestions);
            presence = presence == null ? List.of() : List.copyOf(presence);
            elsewhere = elsewhere == null ? List.of() : List.copyOf(elsewhere);
            containerFreeSlotCount = isCarriedContainer ? Math.max(0, containerFreeSlotCount) : 0;
            containerSlotCapacity = isCarriedContainer ? Math.max(containerFreeSlotCount, containerSlotCapacity) : 0;
            // kitNeeded passes through unchanged.
            desiredCount = Math.max(0, desiredCount);
            // desiredCountFromKit only meaningful when desiredCount > 0; force false otherwise so equality + hashing stay deterministic.
            desiredCountFromKit = desiredCount > 0 && desiredCountFromKit;
            wantedCount = Math.max(0, wantedCount);
            // Wanted is a persisted fetch target that auto-clears once
            // satisfied. Projection normally clears satisfied entries before
            // construction; this guard keeps manually built carried test
            // cards from reporting already-satisfied wants.
            if (carried && totalCount >= wantedCount) {
                wantedCount = 0;
            }
            // Accepted workflow inputs are relevance-only: they may reveal a
            // nearby substitute ghost, but they must not act like missing
            // workflow targets.
            largestCarriedSourceId = largestCarriedSourceId == null ? "" : largestCarriedSourceId;
            largestCarriedSlotIndex = Math.max(-1, largestCarriedSlotIndex);
            largestCarriedSlotCount = Math.max(0, largestCarriedSlotCount);
            putAwayState = putAwayState == null ? PutAwayState.NONE : putAwayState;
        }

        public boolean wanted() {
            return wantedCount > 0;
        }

        /**
         * True iff the AtlasItem carries enough source-slot info for the
         * split-cursor pickup gesture to act on it directly (no hotbar
         * fallback needed).
         */
        public boolean hasLargestCarriedSlot() {
            return !largestCarriedSourceId.isBlank() && largestCarriedSlotIndex >= 0 && largestCarriedSlotCount > 0;
        }

        public AtlasItem withPutAwayState(PutAwayState state) {
            PutAwayState next = state == null ? PutAwayState.NONE : state;
            if (putAwayState == next) {
                return this;
            }
            return new AtlasItem(
                    identity,
                    displayStack,
                    name,
                    totalCount,
                    firstSlotIndex,
                    islandId,
                    recent,
                    playerPlaced,
                    carried,
                    ghost,
                    proximateCount,
                    chipSuggestions,
                    presence,
                    elsewhere,
                    isCarriedContainer,
                    containerFreeSlotCount,
                    containerSlotCapacity,
                    kitNeeded,
                    desiredCount,
                    desiredCountFromKit,
                    wantedCount,
                    junk,
                    acceptedWorkflowInput,
                    largestCarriedSourceId,
                    largestCarriedSlotIndex,
                    largestCarriedSlotCount,
                    next);
        }

        public AtlasItem withAcceptedWorkflowInput(boolean accepted) {
            if (acceptedWorkflowInput == accepted) {
                return this;
            }
            return new AtlasItem(
                    identity,
                    displayStack,
                    name,
                    totalCount,
                    firstSlotIndex,
                    islandId,
                    recent,
                    playerPlaced,
                    carried,
                    ghost,
                    proximateCount,
                    chipSuggestions,
                    presence,
                    elsewhere,
                    isCarriedContainer,
                    containerFreeSlotCount,
                    containerSlotCapacity,
                    kitNeeded,
                    desiredCount,
                    desiredCountFromKit,
                    wantedCount,
                    junk,
                    accepted,
                    largestCarriedSourceId,
                    largestCarriedSlotIndex,
                    largestCarriedSlotCount,
                    putAwayState);
        }

        /** Backward-compat constructor: defaults accepted-workflow-input state. */
        public AtlasItem(
                IdentityRef identity,
                ItemStack displayStack,
                String name,
                int totalCount,
                int firstSlotIndex,
                String islandId,
                boolean recent,
                boolean playerPlaced,
                boolean carried,
                boolean ghost,
                int proximateCount,
                List<ChipSuggestion> chipSuggestions,
                List<ChestPresenceEntry> presence,
                List<ChestPresenceEntry> elsewhere,
                boolean isCarriedContainer,
                int containerFreeSlotCount,
                int containerSlotCapacity,
                boolean kitNeeded,
                int desiredCount,
                boolean desiredCountFromKit,
                int wantedCount,
                boolean junk,
                String largestCarriedSourceId,
                int largestCarriedSlotIndex,
                int largestCarriedSlotCount,
                PutAwayState putAwayState
        ) {
            this(identity, displayStack, name, totalCount, firstSlotIndex, islandId,
                    recent, playerPlaced, carried, ghost, proximateCount, chipSuggestions, presence, elsewhere,
                    isCarriedContainer, containerFreeSlotCount, containerSlotCapacity, kitNeeded, desiredCount,
                    desiredCountFromKit, wantedCount, junk, false, largestCarriedSourceId, largestCarriedSlotIndex,
                    largestCarriedSlotCount, putAwayState);
        }

        /** Backward-compat constructor: defaults junk and accepted-workflow-input state. */
        public AtlasItem(
                IdentityRef identity,
                ItemStack displayStack,
                String name,
                int totalCount,
                int firstSlotIndex,
                String islandId,
                boolean recent,
                boolean playerPlaced,
                boolean carried,
                boolean ghost,
                int proximateCount,
                List<ChipSuggestion> chipSuggestions,
                List<ChestPresenceEntry> presence,
                List<ChestPresenceEntry> elsewhere,
                boolean isCarriedContainer,
                int containerFreeSlotCount,
                int containerSlotCapacity,
                boolean kitNeeded,
                int desiredCount,
                boolean desiredCountFromKit,
                int wantedCount,
                String largestCarriedSourceId,
                int largestCarriedSlotIndex,
                int largestCarriedSlotCount,
                PutAwayState putAwayState
        ) {
            this(identity, displayStack, name, totalCount, firstSlotIndex, islandId,
                    recent, playerPlaced, carried, ghost, proximateCount, chipSuggestions, presence, elsewhere,
                    isCarriedContainer, containerFreeSlotCount, containerSlotCapacity, kitNeeded, desiredCount,
                    desiredCountFromKit, wantedCount, false, false, largestCarriedSourceId, largestCarriedSlotIndex,
                    largestCarriedSlotCount, putAwayState);
        }

        /** Backward-compat constructor: defaults put-away guidance state. */
        public AtlasItem(
                IdentityRef identity,
                ItemStack displayStack,
                String name,
                int totalCount,
                int firstSlotIndex,
                String islandId,
                boolean recent,
                boolean playerPlaced,
                boolean carried,
                boolean ghost,
                int proximateCount,
                List<ChipSuggestion> chipSuggestions,
                List<ChestPresenceEntry> presence,
                List<ChestPresenceEntry> elsewhere,
                boolean isCarriedContainer,
                int containerFreeSlotCount,
                int containerSlotCapacity,
                boolean kitNeeded,
                int desiredCount,
                boolean desiredCountFromKit,
                int wantedCount,
                boolean junk,
                String largestCarriedSourceId,
                int largestCarriedSlotIndex,
                int largestCarriedSlotCount
        ) {
            this(identity, displayStack, name, totalCount, firstSlotIndex, islandId,
                    recent, playerPlaced, carried, ghost, proximateCount, chipSuggestions, presence, elsewhere,
                    isCarriedContainer, containerFreeSlotCount, containerSlotCapacity, kitNeeded, desiredCount,
                    desiredCountFromKit, wantedCount, junk, false, largestCarriedSourceId, largestCarriedSlotIndex,
                    largestCarriedSlotCount, PutAwayState.NONE);
        }

        /** Backward-compat constructor: defaults junk, accepted-workflow-input, and put-away state. */
        public AtlasItem(
                IdentityRef identity,
                ItemStack displayStack,
                String name,
                int totalCount,
                int firstSlotIndex,
                String islandId,
                boolean recent,
                boolean playerPlaced,
                boolean carried,
                boolean ghost,
                int proximateCount,
                List<ChipSuggestion> chipSuggestions,
                List<ChestPresenceEntry> presence,
                List<ChestPresenceEntry> elsewhere,
                boolean isCarriedContainer,
                int containerFreeSlotCount,
                int containerSlotCapacity,
                boolean kitNeeded,
                int desiredCount,
                boolean desiredCountFromKit,
                int wantedCount,
                String largestCarriedSourceId,
                int largestCarriedSlotIndex,
                int largestCarriedSlotCount
        ) {
            this(identity, displayStack, name, totalCount, firstSlotIndex, islandId,
                    recent, playerPlaced, carried, ghost, proximateCount, chipSuggestions, presence, elsewhere,
                    isCarriedContainer, containerFreeSlotCount, containerSlotCapacity, kitNeeded, desiredCount,
                    desiredCountFromKit, wantedCount, false, false, largestCarriedSourceId, largestCarriedSlotIndex,
                    largestCarriedSlotCount, PutAwayState.NONE);
        }

        /** Backward-compat constructor: defaults elsewhere/kitNeeded/desiredCount/largestCarriedSlot. */
        public AtlasItem(
                IdentityRef identity,
                ItemStack displayStack,
                String name,
                int totalCount,
                int firstSlotIndex,
                String islandId,
                boolean recent,
                boolean playerPlaced,
                boolean carried,
                boolean ghost,
                int proximateCount,
                List<ChipSuggestion> chipSuggestions,
                List<ChestPresenceEntry> presence,
                boolean isCarriedContainer,
                int containerFreeSlotCount,
                int containerSlotCapacity
        ) {
            this(identity, displayStack, name, totalCount, firstSlotIndex, islandId,
                    recent, playerPlaced, carried, ghost, proximateCount, chipSuggestions,
                    presence, List.of(), isCarriedContainer, containerFreeSlotCount, containerSlotCapacity, false, 0,
                    false, 0, false, false, "", -1, 0, PutAwayState.NONE);
        }

        /** Backward-compat constructor: defaults kitNeeded/desiredCount/largestCarriedSlot. */
        public AtlasItem(
                IdentityRef identity,
                ItemStack displayStack,
                String name,
                int totalCount,
                int firstSlotIndex,
                String islandId,
                boolean recent,
                boolean playerPlaced,
                boolean carried,
                boolean ghost,
                int proximateCount,
                List<ChipSuggestion> chipSuggestions,
                List<ChestPresenceEntry> presence,
                List<ChestPresenceEntry> elsewhere,
                boolean isCarriedContainer,
                int containerFreeSlotCount,
                int containerSlotCapacity
        ) {
            this(identity, displayStack, name, totalCount, firstSlotIndex, islandId,
                    recent, playerPlaced, carried, ghost, proximateCount, chipSuggestions,
                    presence, elsewhere, isCarriedContainer, containerFreeSlotCount, containerSlotCapacity, false, 0,
                    false, 0, false, false, "", -1, 0, PutAwayState.NONE);
        }

        /** Backward-compat constructor: defaults desiredCount/largestCarriedSlot. */
        public AtlasItem(
                IdentityRef identity,
                ItemStack displayStack,
                String name,
                int totalCount,
                int firstSlotIndex,
                String islandId,
                boolean recent,
                boolean playerPlaced,
                boolean carried,
                boolean ghost,
                int proximateCount,
                List<ChipSuggestion> chipSuggestions,
                List<ChestPresenceEntry> presence,
                List<ChestPresenceEntry> elsewhere,
                boolean isCarriedContainer,
                int containerFreeSlotCount,
                int containerSlotCapacity,
                boolean kitNeeded
        ) {
            this(identity, displayStack, name, totalCount, firstSlotIndex, islandId,
                    recent, playerPlaced, carried, ghost, proximateCount, chipSuggestions,
                    presence, elsewhere, isCarriedContainer, containerFreeSlotCount, containerSlotCapacity, kitNeeded, 0,
                    false, 0, false, false, "", -1, 0, PutAwayState.NONE);
        }

        /** Backward-compat constructor: defaults largestCarriedSlot only. */
        public AtlasItem(
                IdentityRef identity,
                ItemStack displayStack,
                String name,
                int totalCount,
                int firstSlotIndex,
                String islandId,
                boolean recent,
                boolean playerPlaced,
                boolean carried,
                boolean ghost,
                int proximateCount,
                List<ChipSuggestion> chipSuggestions,
                List<ChestPresenceEntry> presence,
                List<ChestPresenceEntry> elsewhere,
                boolean isCarriedContainer,
                int containerFreeSlotCount,
                int containerSlotCapacity,
                boolean kitNeeded,
                int desiredCount
        ) {
            this(identity, displayStack, name, totalCount, firstSlotIndex, islandId,
                    recent, playerPlaced, carried, ghost, proximateCount, chipSuggestions,
                    presence, elsewhere, isCarriedContainer, containerFreeSlotCount, containerSlotCapacity,
                    kitNeeded, desiredCount, false, 0, false, false, "", -1, 0, PutAwayState.NONE);
        }

        /** Backward-compat constructor: defaults wantedCount only. */
        public AtlasItem(
                IdentityRef identity,
                ItemStack displayStack,
                String name,
                int totalCount,
                int firstSlotIndex,
                String islandId,
                boolean recent,
                boolean playerPlaced,
                boolean carried,
                boolean ghost,
                int proximateCount,
                List<ChipSuggestion> chipSuggestions,
                List<ChestPresenceEntry> presence,
                List<ChestPresenceEntry> elsewhere,
                boolean isCarriedContainer,
                int containerFreeSlotCount,
                int containerSlotCapacity,
                boolean kitNeeded,
                int desiredCount,
                boolean desiredCountFromKit,
                String largestCarriedSourceId,
                int largestCarriedSlotIndex,
                int largestCarriedSlotCount
        ) {
            this(identity, displayStack, name, totalCount, firstSlotIndex, islandId,
                    recent, playerPlaced, carried, ghost, proximateCount, chipSuggestions,
                    presence, elsewhere, isCarriedContainer, containerFreeSlotCount, containerSlotCapacity,
                    kitNeeded, desiredCount, desiredCountFromKit, 0, false, false,
                    largestCarriedSourceId, largestCarriedSlotIndex, largestCarriedSlotCount, PutAwayState.NONE);
        }

        /** Backward-compat constructor: maps the previous boolean wanted flag to a one-item target. */
        public AtlasItem(
                IdentityRef identity,
                ItemStack displayStack,
                String name,
                int totalCount,
                int firstSlotIndex,
                String islandId,
                boolean recent,
                boolean playerPlaced,
                boolean carried,
                boolean ghost,
                int proximateCount,
                List<ChipSuggestion> chipSuggestions,
                List<ChestPresenceEntry> presence,
                List<ChestPresenceEntry> elsewhere,
                boolean isCarriedContainer,
                int containerFreeSlotCount,
                int containerSlotCapacity,
                boolean kitNeeded,
                int desiredCount,
                boolean desiredCountFromKit,
                boolean wanted,
                String largestCarriedSourceId,
                int largestCarriedSlotIndex,
                int largestCarriedSlotCount
        ) {
            this(identity, displayStack, name, totalCount, firstSlotIndex, islandId,
                    recent, playerPlaced, carried, ghost, proximateCount, chipSuggestions,
                    presence, elsewhere, isCarriedContainer, containerFreeSlotCount, containerSlotCapacity,
                    kitNeeded, desiredCount, desiredCountFromKit, wanted ? 1 : 0, false, false,
                    largestCarriedSourceId, largestCarriedSlotIndex, largestCarriedSlotCount, PutAwayState.NONE);
        }

        /**
         * Convenience for tests — minimal AtlasItem without ghost/presence/container info.
         */
        public AtlasItem(
                IdentityRef identity,
                ItemStack displayStack,
                String name,
                int totalCount,
                int firstSlotIndex,
                String islandId,
                boolean recent,
                boolean playerPlaced,
                boolean carried,
                List<ChipSuggestion> chipSuggestions
        ) {
            this(identity, displayStack, name, totalCount, firstSlotIndex, islandId,
                    recent, playerPlaced, carried, false, 0, chipSuggestions, List.of(),
                    List.of(), false, 0, 0);
        }
    }

    /** Per-chest breakdown of where an identity lives. Hover/zoom drill-down. */
    public record ChestPresenceEntry(
            String storageId,
            String label,
            int count
    ) {
        public ChestPresenceEntry {
            storageId = storageId == null ? "" : storageId;
            label = label == null ? "" : label;
            count = Math.max(0, count);
        }
    }

    /**
     * Sidebar chip per claimed chest. Replaces the old chest-tile-as-primary
     * surface; the actual chest grid is a detail surface, not rendered in
     * the workspace by default.
     */
    public record ChestChip(
            String storageId,
            String dimensionId,
            String label,
            int anchorCount,
            int slotCapacity,
            int filledSlots,
            boolean proximate,
            int affinityIdentities,
            int worldX,
            int worldY,
            int worldZ,
            String clusterId,
            List<ChestContentSummary> contents
    ) {
        public ChestChip {
            storageId = storageId == null ? "" : storageId;
            dimensionId = dimensionId == null ? "" : dimensionId;
            label = label == null ? "" : label;
            anchorCount = Math.max(1, anchorCount);
            slotCapacity = Math.max(0, slotCapacity);
            filledSlots = Math.max(0, Math.min(filledSlots, slotCapacity));
            affinityIdentities = Math.max(0, affinityIdentities);
            clusterId = clusterId == null ? "" : clusterId;
            contents = contents == null ? List.of() : List.copyOf(contents);
        }

        public ChestChip(
                String storageId,
                String dimensionId,
                String label,
                int anchorCount,
                int slotCapacity,
                int filledSlots,
                boolean proximate,
                int affinityIdentities,
                int worldX,
                int worldY,
                int worldZ,
                String clusterId
        ) {
            this(storageId, dimensionId, label, anchorCount, slotCapacity,
                    filledSlots, proximate, affinityIdentities, worldX, worldY, worldZ,
                    clusterId, List.of());
        }

        public ChestChip(
                String storageId,
                String dimensionId,
                String label,
                int anchorCount,
                int slotCapacity,
                int filledSlots,
                boolean proximate,
                int affinityIdentities,
                int worldX,
                int worldY,
                int worldZ
        ) {
            this(storageId, dimensionId, label, anchorCount, slotCapacity,
                    filledSlots, proximate, affinityIdentities, worldX, worldY, worldZ, "", List.of());
        }
    }

    /**
     * Per-identity summary of what a single claimed chest holds. Used by
     * the search-results panel to count matches per chest without making
     * the chip carry full {@link ItemStack}s for every slot. Display
     * stack is one representative copy of the identity for the chip's
     * mini-icon row; the count rolls up every slot of that identity.
     */
    public record ChestContentSummary(
            String itemId,
            String componentFingerprint,
            String name,
            ItemStack displayStack,
            int count
    ) {
        public ChestContentSummary {
            itemId = itemId == null ? "" : itemId;
            componentFingerprint = componentFingerprint == null ? "" : componentFingerprint;
            name = name == null ? "" : name;
            displayStack = displayStack == null ? ItemStack.EMPTY : displayStack.copy();
            count = Math.max(0, count);
        }
    }

    /** Derived spatial cluster of chest chips. Header label drives the chip-panel grouping. */
    public record ChestClusterDescriptor(
            String clusterId,
            String label,
            int ordinal
    ) {
        public ChestClusterDescriptor {
            clusterId = clusterId == null ? "" : clusterId;
            label = label == null ? "" : label;
            ordinal = Math.max(1, ordinal);
        }
    }

    /**
     * Server-side description of an opened-but-unclaimed (or simply nearby
     * unclaimed) chest. Passed to {@link #project} so the projection can
     * walk its contents and emit a Triage-style panel for the player to
     * accept suggestions / take items from. Bypasses the
     * {@code chestContentsResolver} pipeline because the chest is not in
     * {@code claimedChestMap}.
     */
    public record LootChestSource(
            int chestX,
            int chestY,
            int chestZ,
            String dimensionId,
            String label,
            List<ItemStack> contents
    ) {
        public LootChestSource {
            dimensionId = dimensionId == null ? "" : dimensionId;
            label = label == null ? "" : label;
            if (contents == null) {
                contents = List.of();
            } else {
                ArrayList<ItemStack> copy = new ArrayList<>(contents.size());
                for (ItemStack stack : contents) {
                    copy.add(stack == null ? ItemStack.EMPTY : stack.copy());
                }
                contents = List.copyOf(copy);
            }
        }
    }

    /**
     * Atlas-side projection of an unclaimed nearby chest. Renders as a
     * left-panel surface (Triage-style: per-identity rows with chip
     * suggestions for unhomed identities). Empty when no loot chest is
     * active. {@code chestX/Y/Z + dimensionId} pin the source so take
     * RPCs can target it.
     */
    public record LootChestPanel(
            int chestX,
            int chestY,
            int chestZ,
            String dimensionId,
            String label,
            List<AtlasItem> items
    ) {
        public LootChestPanel {
            dimensionId = dimensionId == null ? "" : dimensionId;
            label = label == null ? "" : label;
            items = items == null ? List.of() : List.copyOf(items);
        }

        public static LootChestPanel empty() {
            return new LootChestPanel(0, 0, 0, "", "", List.of());
        }

        public boolean isPresent() {
            return !dimensionId.isBlank();
        }
    }

    /**
     * Per-frame snapshot of the chest the player currently has open as
     * the host of the SLOT sidebar. Drives the chest-role strip that
     * shows above the wall. {@link #isPresent()} guards rendering;
     * unclaimed active chests surface as {@link ChestRole#IGNORE} until
     * the player deposits into them or explicitly cycles the role.
     */
    public record ActiveChestPanel(
            String storageId,
            String label,
            String clusterId,
            String clusterLabel,
            int swatchColor,
            int posX,
            int posY,
            int posZ,
            String dimensionId,
            ChestRole role,
            List<IdentityRef> affinityIdentities
    ) {
        public ActiveChestPanel {
            storageId = storageId == null ? "" : storageId;
            label = label == null ? "" : label;
            clusterId = clusterId == null ? "" : clusterId;
            clusterLabel = clusterLabel == null ? "" : clusterLabel;
            dimensionId = dimensionId == null ? "" : dimensionId;
            role = role == null ? ChestRole.IGNORE : role;
            affinityIdentities = affinityIdentities == null ? List.of() : List.copyOf(affinityIdentities);
        }

        public ActiveChestPanel(
                String storageId,
                String label,
                String clusterId,
                String clusterLabel,
                int swatchColor,
                int posX,
                int posY,
                int posZ,
                String dimensionId
        ) {
            this(storageId, label, clusterId, clusterLabel, swatchColor, posX, posY, posZ,
                    dimensionId, storageId == null || storageId.isBlank() ? ChestRole.IGNORE : ChestRole.STORAGE,
                    List.of());
        }

        public static ActiveChestPanel empty() {
            return new ActiveChestPanel("", "", "", "", 0, 0, 0, 0, "", ChestRole.IGNORE, List.of());
        }

        public boolean isPresent() {
            return !dimensionId.isBlank();
        }

        public boolean isClaimed() {
            return !storageId.isBlank();
        }

        public ChestRole nextRole() {
            return role.next();
        }

        public boolean hasAffinity(IdentityRef identity) {
            if (identity == null || affinityIdentities.isEmpty()) {
                return false;
            }
            return affinityIdentities.contains(identity);
        }
    }

    public record ChestContentsSnapshot(int slotCount, List<ItemStack> contents, List<Integer> slotIndices) {
        public ChestContentsSnapshot {
            slotCount = Math.max(0, slotCount);
            List<ItemStack> source = contents == null ? List.of() : contents;
            ArrayList<ItemStack> copy = new ArrayList<>(source.size());
            for (ItemStack stack : source) {
                copy.add(stack == null ? ItemStack.EMPTY : stack.copy());
            }
            contents = List.copyOf(copy);
            List<Integer> sourceIndices = slotIndices == null ? List.of() : slotIndices;
            ArrayList<Integer> indexCopy = new ArrayList<>(contents.size());
            for (int i = 0; i < contents.size(); i++) {
                int idx = i < sourceIndices.size() && sourceIndices.get(i) != null ? sourceIndices.get(i) : i;
                indexCopy.add(Math.max(0, idx));
            }
            slotIndices = List.copyOf(indexCopy);
        }

        public ChestContentsSnapshot(int slotCount, List<ItemStack> contents) {
            this(slotCount, contents, List.of());
        }

        public static ChestContentsSnapshot empty() {
            return new ChestContentsSnapshot(0, List.of(), List.of());
        }
    }

    public record HotbarSlot(
            int hotbarIndex,
            boolean selected,
            boolean occupied,
            ItemStack displayStack,
            int count
    ) {
        public HotbarSlot {
            hotbarIndex = Math.max(0, hotbarIndex);
            displayStack = displayStack == null ? ItemStack.EMPTY : displayStack.copy();
            count = Math.max(0, count);
            occupied = occupied && !displayStack.isEmpty();
        }
    }

    public record KitCard(
            String kitId,
            String name,
            String parentId,
            int pageCount,
            int activePageIndex,
            boolean active,
            boolean variant,
            int memberCount,
            List<IdentityRef> members,
            List<WorkflowAcceptedInputRule> acceptedInputs,
            int slotCount,
            int readyCount,
            int carriedSlotCount,
            int carriedSlotCapacity,
            int bringSlotCount,
            int bringReadyCount,
            List<KitSlotState> slots,
            List<KitPageView> pages,
            List<KitBringItem> bring
    ) {
        public KitCard {
            kitId = kitId == null ? "" : kitId;
            name = name == null || name.isBlank() ? kitId : name;
            parentId = parentId == null ? "" : parentId.trim();
            pageCount = Math.max(1, pageCount);
            activePageIndex = Math.max(0, Math.min(activePageIndex, pageCount - 1));
            variant = variant || !parentId.isBlank();
            members = members == null ? List.of() : List.copyOf(members);
            memberCount = Math.max(Math.max(0, memberCount), members.size());
            acceptedInputs = acceptedInputs == null ? List.of() : List.copyOf(acceptedInputs);
            slotCount = Math.max(0, slotCount);
            readyCount = Math.max(0, Math.min(readyCount, slotCount));
            carriedSlotCount = Math.max(0, carriedSlotCount);
            carriedSlotCapacity = Math.max(carriedSlotCount, carriedSlotCapacity);
            bringSlotCount = Math.max(0, bringSlotCount);
            bringReadyCount = Math.max(0, Math.min(bringReadyCount, bringSlotCount));
            slots = slots == null ? List.of() : List.copyOf(slots);
            pages = pages == null ? List.of() : List.copyOf(pages);
            bring = bring == null ? List.of() : List.copyOf(bring);
        }

        public boolean hasMember(IdentityRef identity) {
            if (identity == null || members.isEmpty()) {
                return false;
            }
            LinkedHashSet<ItemIdentity> memberIdentities = new LinkedHashSet<>();
            for (IdentityRef member : members) {
                if (member != null) {
                    ItemIdentityCollections.add(memberIdentities, member.toIdentity());
                }
            }
            return ItemIdentityCollections.contains(memberIdentities, identity.toIdentity());
        }

        public KitCard(
                String kitId,
                String name,
                int pageCount,
                int activePageIndex,
                boolean active,
                int slotCount,
                int readyCount,
                int carriedSlotCount,
                int carriedSlotCapacity,
                int bringSlotCount,
                int bringReadyCount,
                List<KitSlotState> slots,
                List<KitPageView> pages,
                List<KitBringItem> bring
        ) {
            this(
                    kitId,
                    name,
                    "",
                    pageCount,
                    activePageIndex,
                    active,
                    false,
                    0,
                    List.of(),
                    List.of(),
                    slotCount,
                    readyCount,
                    carriedSlotCount,
                    carriedSlotCapacity,
                    bringSlotCount,
                    bringReadyCount,
                    slots,
                    pages,
                    bring
            );
        }

        public KitPageView activePage() {
            if (pages.isEmpty()) {
                return null;
            }
            int index = Math.max(0, Math.min(activePageIndex, pages.size() - 1));
            return pages.get(index);
        }
    }

    public record KitBringItem(
            IdentityRef identity,
            boolean ready,
            ItemStack displayStack,
            String name,
            int presentCount,
            int targetCount
    ) {
        public KitBringItem {
            identity = identity == null
                    ? new IdentityRef("", ItemComparisonMode.ITEM_ID.name(), "")
                    : identity;
            displayStack = displayStack == null ? ItemStack.EMPTY : displayStack.copy();
            name = name == null ? "" : name;
            presentCount = Math.max(0, presentCount);
            targetCount = Math.max(0, targetCount);
        }

        public KitBringItem(IdentityRef identity, boolean ready, ItemStack displayStack, String name) {
            this(identity, ready, displayStack, name, 0, 0);
        }
    }

    public record KitPageView(
            int pageIndex,
            int slotCount,
            int readyCount,
            List<KitSlotState> slots
    ) {
        public KitPageView {
            pageIndex = Math.max(0, pageIndex);
            slotCount = Math.max(0, slotCount);
            readyCount = Math.max(0, Math.min(readyCount, slotCount));
            slots = slots == null ? List.of() : List.copyOf(slots);
        }
    }

    public record KitSlotState(
            int slotIndex,
            boolean filled,
            boolean ready,
            IdentityRef identity,
            ItemStack displayStack,
            String name
    ) {
        public KitSlotState {
            slotIndex = Math.max(0, slotIndex);
            identity = identity == null
                    ? new IdentityRef("", ItemComparisonMode.ITEM_ID.name(), "")
                    : identity;
            displayStack = displayStack == null ? ItemStack.EMPTY : displayStack.copy();
            name = name == null ? "" : name;
        }
    }

    public record OffhandSlot(
            boolean occupied,
            ItemStack displayStack,
            int count
    ) {
        public OffhandSlot {
            displayStack = displayStack == null ? ItemStack.EMPTY : displayStack.copy();
            count = Math.max(0, count);
            occupied = occupied && !displayStack.isEmpty();
        }

        public static OffhandSlot empty() {
            return new OffhandSlot(false, ItemStack.EMPTY, 0);
        }

        public static OffhandSlot from(InventoryAuthoritySnapshot authority) {
            InventoryEntrySnapshot entry = authority.slotEntry(BuiltinInventoryIds.PLAYER_OFFHAND, 0);
            boolean occupied = entry != null && entry.present();
            ItemStack stack = occupied ? entry.stack().copy() : ItemStack.EMPTY;
            return new OffhandSlot(occupied, stack, occupied ? entry.count() : 0);
        }
    }

    private static final class AtlasItemAccumulator {
        private final ItemIdentity identity;
        private final ItemStack displayStack;
        private final String name;
        private int totalCount;
        private int firstSlotIndex;
        private boolean carried;
        // Largest single carried slot for this identity. The split-cursor
        // pickup gesture on atlas cards uses these to source from a real
        // slot (cursor count = half of one specific slot, not half of the
        // virtual aggregate). Empty / non-slot-backed entries leave these
        // at the empty-slot defaults below.
        private String largestCarriedSourceId = "";
        private int largestCarriedSlotIndex = -1;
        private int largestCarriedSlotCount = 0;

        private AtlasItemAccumulator(ItemIdentity identity, InventoryEntrySnapshot firstEntry) {
            this.identity = identity;
            this.displayStack = firstEntry.stack().copy();
            this.name = firstEntry.stack().getHoverName().getString();
            this.firstSlotIndex = firstEntry.slotIndex();
            this.carried = true;
        }

        private AtlasItemAccumulator(ItemIdentity identity, ItemStack displayStack, int totalCount) {
            this.identity = identity;
            this.displayStack = displayStack.copy();
            this.name = displayStack.getHoverName().getString();
            this.firstSlotIndex = 0;
            this.totalCount = Math.max(0, totalCount);
            this.carried = false;
        }

        private static AtlasItemAccumulator ghost(ItemIdentity identity, ItemStack displayStack, int totalCount) {
            return new AtlasItemAccumulator(identity, displayStack, totalCount);
        }

        private void add(InventoryEntrySnapshot entry) {
            totalCount += entry.count();
            if (entry.slotIndex() < firstSlotIndex) {
                firstSlotIndex = entry.slotIndex();
            }
            if (entry.slotBacked() && entry.count() > largestCarriedSlotCount) {
                largestCarriedSlotCount = entry.count();
                largestCarriedSlotIndex = entry.slotIndex();
                largestCarriedSourceId = entry.sourceId();
            }
            carried = true;
        }

        private ItemIdentity identity() {
            return identity;
        }

        private ItemStack displayStack() {
            return displayStack;
        }

        private String name() {
            return name;
        }

        private int totalCount() {
            return totalCount;
        }

        private int firstSlotIndex() {
            return firstSlotIndex == Integer.MAX_VALUE ? 0 : firstSlotIndex;
        }

        private String largestCarriedSourceId() {
            return largestCarriedSourceId;
        }

        private int largestCarriedSlotIndex() {
            return largestCarriedSlotIndex;
        }

        private int largestCarriedSlotCount() {
            return largestCarriedSlotCount;
        }

        private boolean carried() {
            return carried;
        }
    }

    /**
     * Walks proximate-chest contents once and builds (a) per-identity total
     * counts, (b) per-identity presence breakdown by chest, (c) display
     * stack per identity. Drives ghost-card rendering.
     */
    private record ProximateGhostProjection(
            Map<ItemIdentity, Integer> totalsByIdentity,
            Map<ItemIdentity, List<ChestPresenceEntry>> presenceByIdentity,
            Map<ItemIdentity, ItemStack> displayStackByIdentity
    ) {
        static ProximateGhostProjection empty() {
            return new ProximateGhostProjection(Map.of(), Map.of(), Map.of());
        }

        static ProximateGhostProjection build(
                ClaimedChestMap map,
                Function<String, ChestContentsSnapshot> chestContentsResolver,
                Set<String> proximate,
                VisualHomeMap visualHomeMap,
                List<WorldDisplayStorageSource> worldDisplaySources
        ) {
            boolean hasClaimedChests = map != null && !map.chests().isEmpty()
                    && chestContentsResolver != null && proximate != null && !proximate.isEmpty();
            boolean hasDisplaySources = worldDisplaySources != null && !worldDisplaySources.isEmpty();
            if (!hasClaimedChests && !hasDisplaySources) {
                return empty();
            }
            LinkedHashMap<ItemIdentity, Integer> totals = new LinkedHashMap<>();
            LinkedHashMap<ItemIdentity, LinkedHashMap<String, int[]>> perStorage = new LinkedHashMap<>();
            LinkedHashMap<ItemIdentity, ItemStack> displayByIdentity = new LinkedHashMap<>();
            LinkedHashMap<String, String> labelByStorage = new LinkedHashMap<>();
            if (hasClaimedChests) {
                for (ClaimedChest chest : map.chests()) {
                    if (chest == null) {
                        continue;
                    }
                    if (!chest.role().visibleToWorkspace()) {
                        continue;
                    }
                    String storageId = chest.storageId().toString();
                    if (!proximate.contains(storageId)) {
                        continue;
                    }
                    String label = chest.label() == null || chest.label().isBlank()
                            ? autoLabelFor(chest)
                            : chest.label();
                    labelByStorage.put(storageId, label);
                    ChestContentsSnapshot snapshot = chestContentsResolver.apply(storageId);
                    if (snapshot == null) {
                        continue;
                    }
                    for (ItemStack stack : snapshot.contents()) {
                        addPresenceStack(totals, perStorage, displayByIdentity, storageId, stack);
                    }
                }
            }
            if (hasDisplaySources) {
                for (WorldDisplayStorageSource source : worldDisplaySources) {
                    if (source == null || source.storageId().isBlank() || source.contents().isEmpty()) {
                        continue;
                    }
                    labelByStorage.put(source.storageId(), source.label());
                    for (dev.imagio.slot.inventory.storage.WorldStorageAccess.SlotContent content : source.contents()) {
                        if (content == null) {
                            continue;
                        }
                        addPresenceStack(
                                totals,
                                perStorage,
                                displayByIdentity,
                                source.storageId(),
                                content.stack());
                    }
                }
            }
            LinkedHashMap<ItemIdentity, List<ChestPresenceEntry>> presence = new LinkedHashMap<>();
            for (Map.Entry<ItemIdentity, LinkedHashMap<String, int[]>> entry : perStorage.entrySet()) {
                ArrayList<ChestPresenceEntry> entries = new ArrayList<>(entry.getValue().size());
                for (Map.Entry<String, int[]> storageEntry : entry.getValue().entrySet()) {
                    int count = storageEntry.getValue()[0];
                    if (count <= 0) {
                        continue;
                    }
                    String storageId = storageEntry.getKey();
                    String label = labelByStorage.getOrDefault(storageId, storageId);
                    entries.add(new ChestPresenceEntry(storageId, label, count));
                }
                entries.sort(Comparator.<ChestPresenceEntry>comparingInt(ChestPresenceEntry::count).reversed()
                        .thenComparing(ChestPresenceEntry::label, String.CASE_INSENSITIVE_ORDER));
                presence.put(entry.getKey(), List.copyOf(entries));
            }
            return new ProximateGhostProjection(
                    Map.copyOf(totals),
                    Map.copyOf(presence),
                    Map.copyOf(displayByIdentity)
            );
        }

        private static void addPresenceStack(
                LinkedHashMap<ItemIdentity, Integer> totals,
                LinkedHashMap<ItemIdentity, LinkedHashMap<String, int[]>> perStorage,
                LinkedHashMap<ItemIdentity, ItemStack> displayByIdentity,
                String storageId,
                ItemStack stack
        ) {
            if (storageId == null || storageId.isBlank() || stack == null || stack.isEmpty()) {
                return;
            }
            ItemIdentity identity = ItemIdentityMatcher.normalizeMovable(ItemIdentityMatcher.create(stack));
            totals.merge(identity, stack.getCount(), Integer::sum);
            perStorage
                    .computeIfAbsent(identity, ignored -> new LinkedHashMap<>())
                    .computeIfAbsent(storageId, ignored -> new int[]{0})[0] += stack.getCount();
            displayByIdentity.putIfAbsent(identity, stack.copy());
        }

        private static String autoLabelFor(ClaimedChest chest) {
            String hex = chest.storageId().toString();
            int dash = hex.indexOf('-');
            String shortId = dash < 0 ? hex : hex.substring(0, dash);
            if (shortId.length() > 4) {
                shortId = shortId.substring(shortId.length() - 4);
            }
            return "Chest #" + shortId;
        }
    }

    /**
     * Walks <em>non-proximate</em> claimed chests and surfaces their stocks
     * with a dimension-tagged label. Drives search-as-find: when the player
     * queries an item that lives in a remote base, the projection produces
     * an "elsewhere" presence list pointing to the chest by name + dimension.
     *
     * <p>Best-effort: chests whose chunks aren't loaded just return empty
     * contents and contribute nothing. The cross-dimension case is handled
     * by labeling each entry with the chest's dimension so the player can
     * tell where to walk.
     */
    record ElsewhereGhostProjection(
            Map<ItemIdentity, List<ChestPresenceEntry>> presenceByIdentity,
            Map<ItemIdentity, ItemStack> displayStackByIdentity,
            Map<ItemIdentity, Integer> totalsByIdentity
    ) {
        static ElsewhereGhostProjection empty() {
            return new ElsewhereGhostProjection(Map.of(), Map.of(), Map.of());
        }

        static ElsewhereGhostProjection build(
                ClaimedChestMap map,
                Function<String, ChestContentsSnapshot> chestContentsResolver,
                Set<String> proximate,
                List<WorkspaceStorageIndex.StorageEntry> trackedDisplayEntries
        ) {
            boolean hasChests = map != null && !map.chests().isEmpty() && chestContentsResolver != null;
            boolean hasDisplays = trackedDisplayEntries != null && !trackedDisplayEntries.isEmpty();
            if (!hasChests && !hasDisplays) {
                return empty();
            }
            Set<String> proximateIds = proximate == null ? Set.of() : proximate;
            LinkedHashMap<ItemIdentity, LinkedHashMap<String, int[]>> perStorage = new LinkedHashMap<>();
            LinkedHashMap<String, String> labelByStorage = new LinkedHashMap<>();
            LinkedHashMap<ItemIdentity, ItemStack> displayStacks = new LinkedHashMap<>();
            LinkedHashMap<ItemIdentity, Integer> totals = new LinkedHashMap<>();
            if (hasChests) {
                for (ClaimedChest chest : map.chests()) {
                    if (chest == null) {
                        continue;
                    }
                    if (!chest.role().visibleToWorkspace()) {
                        continue;
                    }
                    String storageId = chest.storageId().toString();
                    if (proximateIds.contains(storageId)) {
                        continue;
                    }
                    ChestContentsSnapshot snapshot = chestContentsResolver.apply(storageId);
                    if (snapshot == null || snapshot.contents().isEmpty()) {
                        continue;
                    }
                    String baseLabel = chest.label() == null || chest.label().isBlank()
                            ? autoLabelForChest(chest)
                            : chest.label();
                    String dimension = chest.anchors().isEmpty()
                            ? ""
                            : shortDimension(chest.anchors().iterator().next().dimensionId());
                    String label = dimension.isBlank() ? baseLabel : baseLabel + " — " + dimension;
                    labelByStorage.put(storageId, label);
                    addElsewhereStacks(perStorage, displayStacks, totals, storageId, snapshot.contents());
                }
            }
            if (hasDisplays) {
                for (WorkspaceStorageIndex.StorageEntry entry : trackedDisplayEntries) {
                    if (entry == null || entry.target() == null || !entry.target().displayTarget()
                            || entry.target().proximate()) {
                        continue;
                    }
                    StorageTargetRef target = entry.target();
                    if (target.displayKind() == null || !target.displayKind().trackedStorage()) {
                        continue;
                    }
                    ChestContentsSnapshot snapshot = entry.snapshot();
                    if (snapshot == null || snapshot.contents().isEmpty()) {
                        continue;
                    }
                    String baseLabel = target.label().isBlank() ? target.storageId() : target.label();
                    String dimension = shortDimension(target.dimensionId());
                    String label = dimension.isBlank() ? baseLabel : baseLabel + " — " + dimension;
                    labelByStorage.put(target.storageId(), label);
                    addElsewhereStacks(perStorage, displayStacks, totals, target.storageId(), snapshot.contents());
                }
            }
            if (perStorage.isEmpty()) {
                return empty();
            }
            LinkedHashMap<ItemIdentity, List<ChestPresenceEntry>> presence = new LinkedHashMap<>();
            for (Map.Entry<ItemIdentity, LinkedHashMap<String, int[]>> entry : perStorage.entrySet()) {
                ArrayList<ChestPresenceEntry> entries = new ArrayList<>(entry.getValue().size());
                for (Map.Entry<String, int[]> chestEntry : entry.getValue().entrySet()) {
                    int count = chestEntry.getValue()[0];
                    if (count <= 0) {
                        continue;
                    }
                    String storageId = chestEntry.getKey();
                    String label = labelByStorage.getOrDefault(storageId, storageId);
                    entries.add(new ChestPresenceEntry(storageId, label, count));
                }
                entries.sort(Comparator.<ChestPresenceEntry>comparingInt(ChestPresenceEntry::count).reversed()
                        .thenComparing(ChestPresenceEntry::label, String.CASE_INSENSITIVE_ORDER));
                presence.put(entry.getKey(), List.copyOf(entries));
            }
            return new ElsewhereGhostProjection(
                    Map.copyOf(presence),
                    Map.copyOf(displayStacks),
                    Map.copyOf(totals)
            );
        }

        private static void addElsewhereStacks(
                LinkedHashMap<ItemIdentity, LinkedHashMap<String, int[]>> perStorage,
                LinkedHashMap<ItemIdentity, ItemStack> displayStacks,
                LinkedHashMap<ItemIdentity, Integer> totals,
                String storageId,
                List<ItemStack> stacks
        ) {
            if (storageId == null || storageId.isBlank() || stacks == null || stacks.isEmpty()) {
                return;
            }
            for (ItemStack stack : stacks) {
                if (stack == null || stack.isEmpty()) {
                    continue;
                }
                ItemIdentity identity = ItemIdentityMatcher.normalizeMovable(ItemIdentityMatcher.create(stack));
                perStorage
                        .computeIfAbsent(identity, ignored -> new LinkedHashMap<>())
                        .computeIfAbsent(storageId, ignored -> new int[]{0})[0] += stack.getCount();
                displayStacks.putIfAbsent(identity, stack.copy());
                totals.merge(identity, stack.getCount(), Integer::sum);
            }
        }

        private static String autoLabelForChest(ClaimedChest chest) {
            String hex = chest.storageId().toString();
            int dash = hex.indexOf('-');
            String shortId = dash < 0 ? hex : hex.substring(0, dash);
            if (shortId.length() > 4) {
                shortId = shortId.substring(shortId.length() - 4);
            }
            return "Chest #" + shortId;
        }

        /** "minecraft:the_nether" → "nether"; "modid:custom_dim" → "custom_dim". */
        private static String shortDimension(String dimensionId) {
            if (dimensionId == null) {
                return "";
            }
            int colon = dimensionId.indexOf(':');
            String tail = colon < 0 ? dimensionId : dimensionId.substring(colon + 1);
            if (tail.startsWith("the_")) {
                tail = tail.substring(4);
            }
            return tail;
        }
    }
}
