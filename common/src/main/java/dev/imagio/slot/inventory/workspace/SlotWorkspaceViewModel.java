package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.classification.DynamicHomeCohortPolicy;
import dev.imagio.slot.classification.FacetIndexHolder;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.ItemComparisonMode;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.goal.GoalPlanState;
import dev.imagio.slot.inventory.goal.GoalRecipeDefaults;
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
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.ClaimedChestMap;
import dev.imagio.slot.workflow.domain.KitActivation;
import dev.imagio.slot.workflow.domain.KitDefinition;
import dev.imagio.slot.workflow.domain.KitMap;
import dev.imagio.slot.workflow.domain.KitPage;
import dev.imagio.slot.workflow.domain.RecentView;
import dev.imagio.slot.workflow.domain.VisualAtlasIsland;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import dev.imagio.slot.workflow.domain.VisualHomeAssignment;
import dev.imagio.slot.workflow.domain.VisualHomeMap;
import dev.imagio.slot.workflow.domain.WorkflowDomainSnapshot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
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
        GoalRecipeDefaults goalRecipeDefaults,
        List<GoalPlanState> goalPlans,
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
        goalRecipeDefaults = goalRecipeDefaults == null ? GoalRecipeDefaults.empty() : goalRecipeDefaults;
        goalPlans = goalPlans == null ? List.of() : List.copyOf(goalPlans);
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
            GoalRecipeDefaults goalRecipeDefaults,
            List<GoalPlanState> goalPlans
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
                goalRecipeDefaults,
                goalPlans,
                List.of()
        );
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
                GoalRecipeDefaults.empty(),
                List.of()
        );
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
            GoalRecipeDefaults goalRecipeDefaults
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
                goalRecipeDefaults,
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
        InventoryAuthoritySnapshot resolvedAuthority = authority == null ? InventoryAuthoritySnapshot.empty() : authority;
        WorkflowDomainSnapshot resolvedWorkflow = workflow == null ? WorkflowDomainSnapshot.empty() : workflow;
        Map<ItemIdentity, Integer> wantedCounts = activeWantedCounts(resolvedAuthority, resolvedWorkflow.playerWantedCounts());
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
        List<WorldDisplayStorageSource> displaySources = worldDisplaySources == null
                ? List.of()
                : List.copyOf(worldDisplaySources);
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
        // Search-as-find: collect non-proximate chest stocks too, with the
        // dimension noted in the label. Hover/zoom on a search hit reveals
        // "Storage Area 2 — nether". See docs/plans/learned-storage.md.
        ElsewhereGhostProjection elsewhereGhosts = ElsewhereGhostProjection.build(
                claimedChestMap, chestContentsResolver, proximate);
        Map<ItemIdentity, List<ChestPresenceEntry>> elsewherePresence = elsewhereGhosts.presenceByIdentity();

        // Kit ghost markers: when a kit is active, every needed-but-not-
        // carried identity (page slots + bring list) is flagged as kit-needed
        // so the atlas card highlights it. Items the player has never seen
        // yet are added as synthesized ghost accumulators below so they
        // render where their visual home is.
        Set<ItemIdentity> kitNeededIdentities = kitNeededIdentities(
                resolvedAuthority, resolvedWorkflow.kitMap(), resolvedWorkflow.kitDesiredCounts());

        // Synthesize ghost accumulators for identities present only in
        // proximate chests (homed-but-not-carried). Carried identities use
        // their existing accumulator + presence pip.
        Set<ItemIdentity> carriedIdentities = new LinkedHashSet<>();
        for (AtlasItemAccumulator accumulator : accumulators) {
            carriedIdentities.add(accumulator.identity());
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
            if (carriedIdentities.contains(identity)) {
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
            ghostIdentities.add(accumulator.identity());
        }
        for (ItemIdentity identity : kitNeededIdentities) {
            if (identity == null || ghostIdentities.contains(identity)) {
                continue;
            }
            ItemStack stack = resolveGhostStack(identity);
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            accumulators.add(AtlasItemAccumulator.ghost(identity, stack, 0));
            ghostIdentities.add(identity);
        }
        // Wanted items behave like persisted fetch targets: surface a ghost
        // card on the item's home even when the item only exists in a remote
        // claimed chest, and let the normal wayfinding projection point at
        // that chest until the carried target is met.
        for (ItemIdentity identity : wantedCounts.keySet()) {
            if (identity == null || ghostIdentities.contains(identity)) {
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
            ghostIdentities.add(identity);
        }
        // Tracked-storage x-ray and search both need remote-only claimed
        // storage identities in the view model. The wall hides ordinary
        // remote ghosts until search / x-ray / active intent asks for them,
        // so carrying them here no longer pollutes the default wall.
        for (Map.Entry<ItemIdentity, ItemStack> entry : elsewhereGhosts.displayStackByIdentity().entrySet()) {
            ItemIdentity identity = entry.getKey();
            if (ghostIdentities.contains(identity)) {
                continue;
            }
            ItemStack stack = entry.getValue();
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            int total = elsewhereGhosts.totalsByIdentity().getOrDefault(identity, 0);
            accumulators.add(AtlasItemAccumulator.ghost(identity, stack, total));
            ghostIdentities.add(identity);
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

        List<AtlasIsland> layoutIslands = SlotWorkspaceAtlasLayout.baseIslands(visualHomeMap);
        ArrayList<AtlasItem> atlasItems = new ArrayList<>();
        ArrayList<AtlasItem> triageItems = new ArrayList<>();
        Set<ItemIdentity> recentIdentities = new LinkedHashSet<>(recents.visibleItems());
        List<TriageIslandRef> triageIslandRefs = triageIslandRefs(visualHomeMap);
        LearnedIslandRuleStore resolvedLearnedRules = learnedRules == null ? new LearnedIslandRuleStore() : learnedRules;
        DynamicHomeCohortPolicy cohortPolicy = signalExtractor == null ? null : DynamicHomeCohortPolicy.current();
        Predicate<String> subsystemQualifier = cohortPolicy == null ? id -> false : cohortPolicy.qualifier();
        Predicate<String> organizationGroupQualifier = cohortPolicy == null
                ? id -> false
                : cohortPolicy.organizationGroupQualifier();

        Map<ItemIdentity, Integer> playerDesiredCounts = resolvedWorkflow.playerDesiredCounts();
        Map<String, Map<ItemIdentity, Integer>> kitDesiredCounts = resolvedWorkflow.kitDesiredCounts();
        KitActivation desiredScopeActivation = resolvedWorkflow.kitMap() == null
                ? null : resolvedWorkflow.kitMap().activation();
        String desiredScopeKitId = desiredScopeActivation != null && desiredScopeActivation.isActive()
                ? desiredScopeActivation.kitId() : null;
        Map<ItemIdentity, Integer> activeKitDesiredCounts = desiredScopeKitId == null
                ? Map.of() : kitDesiredCounts.getOrDefault(desiredScopeKitId, Map.of());
        for (AtlasItemAccumulator accumulator : accumulators) {
            VisualHomeAssignment assignment = visualHomeMap.assignment(accumulator.identity());
            List<ChestPresenceEntry> presence = ghosts.presenceByIdentity().getOrDefault(accumulator.identity(), List.of());
            List<ChestPresenceEntry> elsewhere = elsewherePresence.getOrDefault(accumulator.identity(), List.of());
            boolean ghostOnly = !accumulator.carried();
            int proximateCount = ghosts.totalsByIdentity().getOrDefault(accumulator.identity(), 0);
            boolean kitNeeded = kitNeededIdentities.contains(accumulator.identity()) && !accumulator.carried();
            // Resolve desired count: kit-scoped wins when a kit is active
            // and has a non-zero entry, else the player-global value. Mirror
            // of DesiredCountWorkflowDomainService.resolved — kept inline
            // here so the build pass doesn't need a runtime-service handle.
            int kitDesired = activeKitDesiredCounts.getOrDefault(accumulator.identity(), 0);
            int playerDesired = playerDesiredCounts.getOrDefault(accumulator.identity(), 0);
            int desiredCount = kitDesired > 0 ? kitDesired : playerDesired;
            boolean desiredCountFromKit = kitDesired > 0;
            int wantedCount = wantedCounts.getOrDefault(accumulator.identity(), 0);

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
                                recentIdentities.contains(accumulator.identity()),
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
                        recentIdentities.contains(accumulator.identity()),
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
                        recentIdentities.contains(accumulator.identity()),
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
                    recentIdentities.contains(accumulator.identity()),
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
                    accumulator.largestCarriedSourceId(),
                    accumulator.largestCarriedSlotIndex(),
                    accumulator.largestCarriedSlotCount()
            ));
        }

        triageItems.sort(Comparator
                .comparingInt((AtlasItem item) -> recentRankByIdentity.getOrDefault(item.identity().toIdentity(), Integer.MAX_VALUE))
                .thenComparing(item -> item.name().toLowerCase(Locale.ROOT))
                .thenComparing(item -> item.identity().itemId()));

        List<AtlasIsland> islandsWithCarriedCounts = withCarriedCounts(layoutIslands, atlasItems);

        ChestClusterMap clusterMap = ChestClusterMap.derive(claimedChestMap);
        List<ChestChip> chestChips = chestChips(claimedChestMap, affinityMap, chestContentsResolver, proximate, clusterMap);
        List<ChestClusterDescriptor> chestClusters = chestClusterDescriptors(clusterMap, resolvedWorkflow.clusterLabels());

        List<KitCard> kitCards = kitCards(
                resolvedAuthority, resolvedWorkflow.kitMap(), resolvedWorkflow.kitDesiredCounts());
        LootChestPanel lootPanel = lootChestPanel(
                lootChestSource, visualHomeMap, signalExtractor, resolvedLearnedRules, triageIslandRefs,
                subsystemQualifier, organizationGroupQualifier);
        List<WayfindingTarget> wayfindingTargets = wayfindingTargets(
                resolvedAuthority,
                claimedChestMap,
                chestContentsResolver,
                kitNeededIdentities,
                wantedCounts,
                activeKitDesiredCounts,
                playerDesiredCounts);
        java.util.function.ToIntFunction<ItemIdentity> reservedCountResolver = identity -> reservedCarryCount(
                identity,
                resolvedWorkflow.kitMap(),
                activeKitDesiredCounts,
                playerDesiredCounts,
                wantedCounts);
        Set<IdentityRef> depositableIdentities = depositableIdentities(
                atlasItems,
                claimedChestMap,
                affinityMap,
                proximate,
                chestContentsResolver,
                reservedCountResolver);
        ArrayList<AtlasItem> contextualSuggestionCandidates = new ArrayList<>(atlasItems.size() + triageItems.size());
        contextualSuggestionCandidates.addAll(atlasItems);
        contextualSuggestionCandidates.addAll(triageItems);
        List<ContextualSuggestionLane> contextualSuggestionLanes = ContextualSuggestionScorer.lanes(
                contextualSuggestionCandidates,
                resolvedWorkflow,
                FacetIndexHolder.get(),
                carriedFreeSlotCount,
                carriedSlotCapacity,
                currentTick);
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
                new GoalRecipeDefaults(resolvedWorkflow.goalRecipeDefaults()),
                resolvedWorkflow.goalPlans(),
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

    public static Map<ItemIdentity, Integer> activeWantedCounts(
            InventoryAuthoritySnapshot authority,
            Map<ItemIdentity, Integer> playerWantedCounts
    ) {
        if (playerWantedCounts == null || playerWantedCounts.isEmpty()) {
            return Map.of();
        }
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
            if (carriedMovableCount(authority, identity) < target) {
                active.put(identity, target);
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
            KitMap kitMap,
            Map<ItemIdentity, Integer> activeKitDesiredCounts,
            Map<ItemIdentity, Integer> playerDesiredCounts,
            Map<ItemIdentity, Integer> playerWantedCounts
    ) {
        if (identity == null) {
            return 0;
        }
        int kitDesired = activeKitDesiredCounts == null ? 0
                : activeKitDesiredCounts.getOrDefault(identity, 0);
        int playerDesired = playerDesiredCounts == null ? 0
                : playerDesiredCounts.getOrDefault(identity, 0);
        int playerWanted = playerWantedCounts == null ? 0
                : playerWantedCounts.getOrDefault(identity, 0);
        // Mirror DesiredCountWorkflowDomainService.resolved: kit scope wins
        // when active and non-zero, else player scope.
        int desired = kitDesired > 0 ? kitDesired : playerDesired;
        int kitSlotCount = activeKitPageSlotCount(kitMap, identity);
        return Math.max(Math.max(desired, playerWanted), kitSlotCount);
    }

    private static int activeKitPageSlotCount(KitMap kitMap, ItemIdentity identity) {
        if (kitMap == null || identity == null) {
            return 0;
        }
        KitActivation activation = kitMap.activation();
        if (activation == null || !activation.isActive()) {
            return 0;
        }
        KitDefinition kit = kitMap.kit(activation.kitId());
        if (kit == null) {
            return 0;
        }
        int pageIndex = Math.max(0, Math.min(activation.pageIndex(), kit.pageCount() - 1));
        KitPage page = kit.page(pageIndex);
        if (page == null) {
            return 0;
        }
        int count = 0;
        for (int slotIndex = 0; slotIndex < KitPage.HOTBAR_SLOT_COUNT; slotIndex++) {
            if (identity.equals(page.slot(slotIndex))) {
                count++;
            }
        }
        return count;
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
            java.util.function.ToIntFunction<ItemIdentity> reservedCountResolver
    ) {
        if (atlasItems == null || atlasItems.isEmpty()
                || claimedChestMap == null || claimedChestMap.chests().isEmpty()
                || proximateStorageIds == null || proximateStorageIds.isEmpty()) {
            return Set.of();
        }
        Map<String, Set<ItemIdentity>> contentIdentities = proximateContentIdentitiesByChest(
                claimedChestMap, chestContentsResolver, proximateStorageIds);
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
            for (ClaimedChest chest : claimedChestMap.chests()) {
                if (chest == null) {
                    continue;
                }
                if (!proximateStorageIds.contains(chest.storageId().toString())) {
                    continue;
                }
                if (!storageAffinityEligible(chest.storageId().toString(), chestContentsResolver)) {
                    continue;
                }
                boolean affinityMatch = affinityMap != null && affinityMap.score(chest.storageId(), identity) > 0;
                boolean contentMatch = contentIdentities
                        .getOrDefault(chest.storageId().toString(), Set.of())
                        .contains(ItemIdentityMatcher.normalizeMovable(identity));
                if (affinityMatch || contentMatch) {
                    result.add(item.identity());
                    break;
                }
            }
        }
        return Set.copyOf(result);
    }

    private static Map<String, Set<ItemIdentity>> proximateContentIdentitiesByChest(
            ClaimedChestMap claimedChestMap,
            Function<String, ChestContentsSnapshot> chestContentsResolver,
            Set<String> proximateStorageIds
    ) {
        if (claimedChestMap == null || claimedChestMap.chests().isEmpty()
                || chestContentsResolver == null
                || proximateStorageIds == null || proximateStorageIds.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, Set<ItemIdentity>> out = new LinkedHashMap<>();
        for (ClaimedChest chest : claimedChestMap.chests()) {
            if (chest == null) {
                continue;
            }
            String storageId = chest.storageId().toString();
            if (!proximateStorageIds.contains(storageId)) {
                continue;
            }
            ChestContentsSnapshot snapshot = chestContentsResolver.apply(storageId);
            if (snapshot == null || snapshot.contents().isEmpty()) {
                continue;
            }
            if (!StorageAffinityPolicy.isEligibleSlotCount(snapshot.slotCount())) {
                continue;
            }
            LinkedHashSet<ItemIdentity> identities = new LinkedHashSet<>();
            for (ItemStack stack : snapshot.contents()) {
                if (stack == null || stack.isEmpty()) {
                    continue;
                }
                identities.add(ItemIdentityMatcher.normalizeMovable(ItemIdentityMatcher.create(stack)));
            }
            if (!identities.isEmpty()) {
                out.put(storageId, Set.copyOf(identities));
            }
        }
        return Map.copyOf(out);
    }

    private static boolean storageAffinityEligible(
            String storageId,
            Function<String, ChestContentsSnapshot> chestContentsResolver
    ) {
        if (storageId == null || storageId.isBlank() || chestContentsResolver == null) {
            return true;
        }
        ChestContentsSnapshot snapshot = chestContentsResolver.apply(storageId);
        return snapshot != null && StorageAffinityPolicy.isEligibleSlotCount(snapshot.slotCount());
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
            ItemIdentity identity = ItemIdentityMatcher.create(stack);
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
            InventoryAuthoritySnapshot authority,
            KitMap kitMap,
            Map<String, Map<ItemIdentity, Integer>> kitDesiredCounts
    ) {
        if (kitMap == null || kitMap.kits().isEmpty()) {
            return List.of();
        }
        Set<ItemIdentity> carried = carriedIdentities(authority);
        KitActivation activation = kitMap.activation();
        ArrayList<KitCard> result = new ArrayList<>(kitMap.kits().size());
        for (KitDefinition kit : kitMap.kits()) {
            if (kit == null || kit.id().isBlank()) {
                continue;
            }
            boolean active = activation.isActive() && activation.kitId().equals(kit.id());
            int renderedPage = active ? Math.max(0, Math.min(activation.pageIndex(), kit.pageCount() - 1)) : 0;
            ArrayList<KitPageView> pages = new ArrayList<>(kit.pageCount());
            for (int pageIndex = 0; pageIndex < kit.pageCount(); pageIndex++) {
                KitPage page = kit.page(pageIndex);
                if (page == null) {
                    continue;
                }
                ArrayList<KitSlotState> pageSlots = new ArrayList<>(KitPage.HOTBAR_SLOT_COUNT);
                int pageReady = 0;
                for (int slotIndex = 0; slotIndex < KitPage.HOTBAR_SLOT_COUNT; slotIndex++) {
                    ItemIdentity identity = page.slot(slotIndex);
                    boolean filled = identity != null;
                    boolean present = filled && carriedHasMovable(carried, identity);
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
                int presentCount = carriedMovableCount(authority, identity);
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
                    kit.pageCount(),
                    renderedPage,
                    active,
                    KitPage.HOTBAR_SLOT_COUNT,
                    renderedReady,
                    kit.carriedSlotCount(),
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

    /**
     * Identities the active kit needs that aren't carried right now —
     * page slots on the active page plus every identity with a non-zero
     * kit-scoped desired count. Empty when no kit is active.
     */
    private static Set<ItemIdentity> kitNeededIdentities(
            InventoryAuthoritySnapshot authority,
            KitMap kitMap,
            Map<String, Map<ItemIdentity, Integer>> kitDesiredCounts
    ) {
        if (kitMap == null) {
            return Set.of();
        }
        KitActivation activation = kitMap.activation();
        if (!activation.isActive()) {
            return Set.of();
        }
        KitDefinition kit = kitMap.kit(activation.kitId());
        if (kit == null) {
            return Set.of();
        }
        Set<ItemIdentity> carried = carriedIdentities(authority);
        LinkedHashSet<ItemIdentity> needed = new LinkedHashSet<>();
        int pageIndex = Math.max(0, Math.min(activation.pageIndex(), kit.pageCount() - 1));
        KitPage page = kit.page(pageIndex);
        if (page != null) {
            for (int slotIndex = 0; slotIndex < KitPage.HOTBAR_SLOT_COUNT; slotIndex++) {
                ItemIdentity identity = page.slot(slotIndex);
                if (identity != null && !carriedHasMovable(carried, identity)) {
                    needed.add(identity);
                }
            }
        }
        if (kitDesiredCounts != null) {
            Map<ItemIdentity, Integer> kitWants = kitDesiredCounts.getOrDefault(activation.kitId(), Map.of());
            for (Map.Entry<ItemIdentity, Integer> entry : kitWants.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null && entry.getValue() > 0
                        && !carriedHasMovable(carried, entry.getKey())) {
                    needed.add(entry.getKey());
                }
            }
        }
        return Set.copyOf(needed);
    }

    /**
     * Build per-chest wayfinding targets: chests holding at least one
     * identity the player still needs (active kit page slot, kit-scoped
     * desired-count gap, player-global desired-count gap, or active
     * wanted-count gap). Drives the client-side wayfinding HUD + atlas chip +
     * in-world chest glow.
     *
     * <p>The "missing identity" set unions kit-needed (already
     * carry-aware) with desired and wanted gaps where {@code carriedCount}
     * has not reached the relevant target. Source-specific sets remain
     * separate on {@link WayfindingTarget}.
     */
    private static List<WayfindingTarget> wayfindingTargets(
            InventoryAuthoritySnapshot authority,
            ClaimedChestMap claimedChestMap,
            Function<String, ChestContentsSnapshot> chestContentsResolver,
            Set<ItemIdentity> kitNeededIdentities,
            Map<ItemIdentity, Integer> wantedCounts,
            Map<ItemIdentity, Integer> activeKitDesiredCounts,
            Map<ItemIdentity, Integer> playerDesiredCounts
    ) {
        if (claimedChestMap == null || claimedChestMap.chests().isEmpty() || chestContentsResolver == null) {
            return List.of();
        }
        // Build the missing-identity source map in one pass. Desired and
        // wanted state stay distinct here; display code may collapse them,
        // but downstream logic can still tell why a chest is being surfaced.
        LinkedHashMap<ItemIdentity, WayfindingNeedSources> missingSources = new LinkedHashMap<>();
        for (ItemIdentity identity : kitNeededIdentities) {
            if (identity != null) {
                missingSources.computeIfAbsent(identity, ignored -> new WayfindingNeedSources()).kit = true;
            }
        }
        if (activeKitDesiredCounts != null) {
            for (Map.Entry<ItemIdentity, Integer> entry : activeKitDesiredCounts.entrySet()) {
                ItemIdentity identity = entry.getKey();
                Integer target = entry.getValue();
                if (identity == null || target == null || target <= 0) {
                    continue;
                }
                if (carriedMovableCount(authority, identity) < target) {
                    WayfindingNeedSources sources =
                            missingSources.computeIfAbsent(identity, ignored -> new WayfindingNeedSources());
                    sources.kit = true;
                    sources.desired = true;
                }
            }
        }
        if (playerDesiredCounts != null) {
            for (Map.Entry<ItemIdentity, Integer> entry : playerDesiredCounts.entrySet()) {
                ItemIdentity identity = entry.getKey();
                Integer target = entry.getValue();
                if (identity == null || target == null || target <= 0) {
                    continue;
                }
                // Kit-scoped desired count overrides player-global at the
                // same identity (mirrors the AtlasItem desiredCount kit-wins
                // resolution); skip the player check when the active kit
                // already has a non-zero target for this identity.
                if (activeKitDesiredCounts != null
                        && activeKitDesiredCounts.getOrDefault(identity, 0) > 0) {
                    continue;
                }
                if (carriedMovableCount(authority, identity) < target) {
                    missingSources.computeIfAbsent(identity, ignored -> new WayfindingNeedSources()).desired = true;
                }
            }
        }
        if (wantedCounts != null) {
            for (Map.Entry<ItemIdentity, Integer> entry : wantedCounts.entrySet()) {
                ItemIdentity identity = entry.getKey();
                Integer target = entry.getValue();
                if (identity != null && target != null && target > 0
                        && carriedMovableCount(authority, identity) < target) {
                    missingSources.computeIfAbsent(identity, ignored -> new WayfindingNeedSources()).wanted = true;
                }
            }
        }
        if (missingSources.isEmpty()) {
            return List.of();
        }
        ArrayList<WayfindingTarget> targets = new ArrayList<>();
        for (ClaimedChest chest : claimedChestMap.chests()) {
            if (chest == null || chest.anchors().isEmpty()) {
                continue;
            }
            String storageId = chest.storageId().toString();
            ChestContentsSnapshot snapshot = chestContentsResolver.apply(storageId);
            if (snapshot == null || snapshot.contents().isEmpty()) {
                continue;
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
                continue;
            }
            ChestAnchor primary = chest.anchors().iterator().next();
            targets.add(new WayfindingTarget(
                    storageId,
                    primary.dimensionId(),
                    primary.x(),
                    primary.y(),
                    primary.z(),
                    matched,
                    kitMatched,
                    desiredMatched,
                    wantedMatched,
                    totalMissingCount,
                    wayfindingScope(kitMatched, desiredMatched, wantedMatched)
            ));
        }
        return List.copyOf(targets);
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

    /**
     * Movable-aware membership check for the carried set. Tools (bow, sword,
     * pickaxe, …) get strict identities at capture time because their
     * components include durability and enchantments, while the kit page
     * stored a different snapshot — strict {@link Set#contains} would miss
     * a damaged-but-equipped bow when the kit was captured with a pristine
     * one. Mirrors {@link ItemIdentityMatcher#matchesMovable} so kit progress
     * and star indicators agree with what {@code LoadoutApplyService} would
     * actually source.
     */
    private static boolean carriedHasMovable(Set<ItemIdentity> carried, ItemIdentity identity) {
        if (identity == null) {
            return false;
        }
        for (ItemIdentity candidate : carried) {
            if (ItemIdentityMatcher.matchesMovable(candidate, identity)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sum of stack counts in carry whose identity matches {@code identity}
     * under movable-aware semantics. Drives the kit-bring "M / N" want-vs-
     * have indicator: targetCount comes from kit-scoped desired counts,
     * presentCount comes from this walk.
     */
    public static int carriedMovableCount(InventoryAuthoritySnapshot authority, ItemIdentity identity) {
        if (authority == null || identity == null) {
            return 0;
        }
        int total = 0;
        for (InventorySourceDescriptor source : authority.carriedSources()) {
            for (InventoryEntrySnapshot entry : authority.entries(source.id())) {
                if (entry == null || !entry.present() || entry.stack() == null || entry.stack().isEmpty()) {
                    continue;
                }
                if (ItemIdentityMatcher.matchesMovable(entry.stack(), identity)) {
                    total += entry.count();
                }
            }
        }
        return total;
    }

    private static Set<ItemIdentity> carriedIdentities(InventoryAuthoritySnapshot authority) {
        LinkedHashSet<ItemIdentity> identities = new LinkedHashSet<>();
        if (authority == null) {
            return identities;
        }
        for (InventorySourceDescriptor source : authority.carriedSources()) {
            for (InventoryEntrySnapshot entry : authority.entries(source.id())) {
                if (entry == null || !entry.present()) {
                    continue;
                }
                identities.add(ItemIdentityMatcher.create(entry.stack()));
            }
        }
        return identities;
    }

    private static ItemStack resolveGhostStack(ItemIdentity identity) {
        if (identity == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = resolveGhostStackItem(identity.itemId());
        if (!stack.isEmpty()) {
            return stack;
        }
        String fallbackItemId = syntheticGoalDisplayItem(identity.itemId());
        if (!fallbackItemId.isBlank()) {
            return resolveGhostStackItem(fallbackItemId);
        }
        return ItemStack.EMPTY;
    }

    static ItemStack resolveGhostStack(ItemIdentity identity, int count) {
        ItemStack stack = resolveGhostStack(identity);
        if (stack == null || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack copy = stack.copy();
        copy.setCount(Math.max(1, count));
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

    private static String syntheticGoalDisplayItem(String itemId) {
        if (itemId == null || !itemId.startsWith("slot:emi/")) {
            return "";
        }
        String normalized = itemId.toLowerCase(Locale.ROOT);
        if (normalized.contains("limewater")) {
            return "minecraft:barrel";
        }
        if (normalized.contains("water")) {
            return "minecraft:water_bucket";
        }
        if (normalized.contains("lava")) {
            return "minecraft:lava_bucket";
        }
        if (normalized.contains("fluid") || normalized.contains("liquid")) {
            return "minecraft:bucket";
        }
        return "";
    }

    public static ItemStack displayStackForIdentity(ItemIdentity identity) {
        return resolveGhostStack(identity);
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
                goalRecipeDefaults,
                goalPlans,
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
                goalRecipeDefaults,
                goalPlans,
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
            ChestClusterMap clusterMap
    ) {
        if (map == null || map.chests().isEmpty()) {
            return List.of();
        }
        ArrayList<ChestChip> chips = new ArrayList<>(map.chests().size());
        for (ClaimedChest chest : map.chests()) {
            if (chest == null) {
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
            int slotCapacity = snapshot.slotCount();
            int filledSlots = 0;
            // Roll up the chest's slot-by-slot contents into per-identity
            // summaries. The search-results panel uses this to count
            // matches without re-walking the chest server-side.
            LinkedHashMap<ItemIdentity, ChestContentSummary> summaryByIdentity = new LinkedHashMap<>();
            for (ItemStack stack : snapshot.contents()) {
                if (stack == null || stack.isEmpty()) {
                    continue;
                }
                filledSlots++;
                ItemIdentity identity = ItemIdentityMatcher.create(stack);
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
            boolean isProximate = proximate.contains(storageId);
            int affinityCount = affinityMap == null ? 0 : affinityMap.forChest(chest.storageId()).size();
            String clusterId = clusterMap == null ? "" : clusterMap.clusterId(chest.storageId());
            chips.add(new ChestChip(
                    storageId,
                    dimension,
                    label,
                    chest.anchors().size(),
                    slotCapacity,
                    filledSlots,
                    isProximate,
                    affinityCount,
                    worldX,
                    worldY,
                    worldZ,
                    clusterId == null ? "" : clusterId,
                    List.copyOf(summaryByIdentity.values())
            ));
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
                ItemIdentity identity = ItemIdentityMatcher.create(entry.stack());
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
            String largestCarriedSourceId,
            int largestCarriedSlotIndex,
            int largestCarriedSlotCount
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
            largestCarriedSourceId = largestCarriedSourceId == null ? "" : largestCarriedSourceId;
            largestCarriedSlotIndex = Math.max(-1, largestCarriedSlotIndex);
            largestCarriedSlotCount = Math.max(0, largestCarriedSlotCount);
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
                    false, 0, "", -1, 0);
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
                    false, 0, "", -1, 0);
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
                    false, 0, "", -1, 0);
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
                    kitNeeded, desiredCount, false, 0, "", -1, 0);
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
                    kitNeeded, desiredCount, desiredCountFromKit, 0,
                    largestCarriedSourceId, largestCarriedSlotIndex, largestCarriedSlotCount);
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
                    kitNeeded, desiredCount, desiredCountFromKit, wanted ? 1 : 0,
                    largestCarriedSourceId, largestCarriedSlotIndex, largestCarriedSlotCount);
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
     * the host of the SLOT sidebar. Drives the chest-control strip that
     * shows above the wall — rename / forget gestures + a claim button
     * for unclaimed chests. {@link #isPresent()} guards rendering;
     * {@link #isClaimed()} switches the strip between claim and manage
     * affordances.
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
            String dimensionId
    ) {
        public ActiveChestPanel {
            storageId = storageId == null ? "" : storageId;
            label = label == null ? "" : label;
            clusterId = clusterId == null ? "" : clusterId;
            clusterLabel = clusterLabel == null ? "" : clusterLabel;
            dimensionId = dimensionId == null ? "" : dimensionId;
        }

        public static ActiveChestPanel empty() {
            return new ActiveChestPanel("", "", "", "", 0, 0, 0, 0, "");
        }

        public boolean isPresent() {
            return !dimensionId.isBlank();
        }

        public boolean isClaimed() {
            return !storageId.isBlank();
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
        public KitCard {
            kitId = kitId == null ? "" : kitId;
            name = name == null || name.isBlank() ? kitId : name;
            pageCount = Math.max(1, pageCount);
            activePageIndex = Math.max(0, Math.min(activePageIndex, pageCount - 1));
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
                return new ProximateGhostProjection(Map.of(), Map.of(), Map.of());
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
            ItemIdentity identity = ItemIdentityMatcher.create(stack);
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
                Set<String> proximate
        ) {
            if (map == null || map.chests().isEmpty() || chestContentsResolver == null) {
                return empty();
            }
            Set<String> proximateIds = proximate == null ? Set.of() : proximate;
            LinkedHashMap<ItemIdentity, LinkedHashMap<UUID, int[]>> perChest = new LinkedHashMap<>();
            LinkedHashMap<UUID, String> labelByStorage = new LinkedHashMap<>();
            LinkedHashMap<ItemIdentity, ItemStack> displayStacks = new LinkedHashMap<>();
            LinkedHashMap<ItemIdentity, Integer> totals = new LinkedHashMap<>();
            for (ClaimedChest chest : map.chests()) {
                if (chest == null) {
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
                labelByStorage.put(chest.storageId(), label);
                for (ItemStack stack : snapshot.contents()) {
                    if (stack == null || stack.isEmpty()) {
                        continue;
                    }
                    ItemIdentity identity = ItemIdentityMatcher.create(stack);
                    perChest
                            .computeIfAbsent(identity, ignored -> new LinkedHashMap<>())
                            .computeIfAbsent(chest.storageId(), ignored -> new int[]{0})[0] += stack.getCount();
                    displayStacks.putIfAbsent(identity, stack.copy());
                    totals.merge(identity, stack.getCount(), Integer::sum);
                }
            }
            if (perChest.isEmpty()) {
                return empty();
            }
            LinkedHashMap<ItemIdentity, List<ChestPresenceEntry>> presence = new LinkedHashMap<>();
            for (Map.Entry<ItemIdentity, LinkedHashMap<UUID, int[]>> entry : perChest.entrySet()) {
                ArrayList<ChestPresenceEntry> entries = new ArrayList<>(entry.getValue().size());
                for (Map.Entry<UUID, int[]> chestEntry : entry.getValue().entrySet()) {
                    int count = chestEntry.getValue()[0];
                    if (count <= 0) {
                        continue;
                    }
                    String label = labelByStorage.getOrDefault(chestEntry.getKey(), chestEntry.getKey().toString());
                    entries.add(new ChestPresenceEntry(chestEntry.getKey().toString(), label, count));
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
