package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.ItemComparisonMode;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

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
        LootChestPanel lootChestPanel
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
                hotbarSlots, offhand, kits, LootChestPanel.empty());
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
                hotbarSlots, offhand, kits, LootChestPanel.empty());
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
                lootChestSource, "");
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
            String searchQuery
    ) {
        InventoryAuthoritySnapshot resolvedAuthority = authority == null ? InventoryAuthoritySnapshot.empty() : authority;
        WorkflowDomainSnapshot resolvedWorkflow = workflow == null ? WorkflowDomainSnapshot.empty() : workflow;
        RecentView recents = resolvedWorkflow.recents();
        VisualHomeMap visualHomeMap = resolvedWorkflow.visualHomeMap();
        ClaimedChestMap claimedChestMap = resolvedWorkflow.claimedChestMap();
        ChestAffinityMap affinityMap = resolvedWorkflow.chestAffinityMap();
        Set<String> proximate = proximateStorageIds == null ? Set.of() : proximateStorageIds;
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
                claimedChestMap, chestContentsResolver, proximate, visualHomeMap);
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
        // Search-as-find: synthesize ghost accumulators for identities
        // that live ONLY in non-proximate claimed chests (no carry, no
        // proximate ghost, no kit-needed entry) — but ONLY while the
        // player is searching, and ONLY for identities matching the
        // query. Without the gate these ghosts pollute the atlas
        // full-time with cards for items the player isn't trying to
        // find; without the synthesis searching for "oak" when an oak
        // chest in the next room over is the only place oak exists
        // returns zero atlas hits, leaving the player no visual
        // confirmation. Kit-needed remote items still surface via the
        // earlier kit-ghost loop (kitNeededIdentities seeds an
        // accumulator regardless of search state).
        String normalizedQuery = searchQuery == null ? "" : searchQuery.trim().toLowerCase(Locale.ROOT);
        if (!normalizedQuery.isBlank()) {
            for (Map.Entry<ItemIdentity, ItemStack> entry : elsewhereGhosts.displayStackByIdentity().entrySet()) {
                ItemIdentity identity = entry.getKey();
                if (ghostIdentities.contains(identity)) {
                    continue;
                }
                ItemStack stack = entry.getValue();
                if (stack == null || stack.isEmpty()) {
                    continue;
                }
                if (!matchesQuery(identity, stack, normalizedQuery)) {
                    continue;
                }
                int total = elsewhereGhosts.totalsByIdentity().getOrDefault(identity, 0);
                accumulators.add(AtlasItemAccumulator.ghost(identity, stack, total));
                ghostIdentities.add(identity);
            }
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

            if (assignment == null) {
                List<ChipSuggestion> chipSuggestions = List.of();
                if (accumulator.carried() && signalExtractor != null) {
                    IslandSignalDescriptor descriptor = signalExtractor.apply(accumulator.displayStack());
                    if (descriptor != null) {
                        chipSuggestions = IslandSuggestionService.suggest(
                                descriptor,
                                resolvedLearnedRules,
                                triageIslandRefs,
                                visualHomeMap.dismissedTemplateIds()
                        );
                    }
                }
                CarriedContainerInfo containerInfo = containerResolver.apply(accumulator.identity());
                boolean isContainer = containerInfo != null;
                int containerFree = isContainer ? containerInfo.freeSlots() : 0;
                int containerCapacity = isContainer ? containerInfo.slotCapacity() : 0;
                if (ghostOnly) {
                    // Unhomed ghost item — not carried, not assigned. Don't
                    // render: triage is for *carried* items needing a home.
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
                lootChestSource, visualHomeMap, signalExtractor, resolvedLearnedRules, triageIslandRefs);
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
                lootPanel
        );
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
            List<TriageIslandRef> triageIslandRefs
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
                            visualHomeMap.dismissedTemplateIds()
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
    private static int carriedMovableCount(InventoryAuthoritySnapshot authority, ItemIdentity identity) {
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
        try {
            ItemStack stack = ghostStackResolver.apply(identity.itemId());
            return stack == null ? ItemStack.EMPTY : stack;
        } catch (RuntimeException | LinkageError ignored) {
            return ItemStack.EMPTY;
        }
    }

    /**
     * Substring match for the search-as-find ghost-synthesis filter:
     * lowercase {@code itemId} or display name contains the (already
     * normalized) query. Mirrors {@code SearchController.matchesItem}'s
     * shape so the server-side gate matches what the client would
     * highlight as a hit.
     */
    private static boolean matchesQuery(ItemIdentity identity, ItemStack stack, String normalizedQuery) {
        if (normalizedQuery == null || normalizedQuery.isBlank()) {
            return false;
        }
        StringBuilder searchable = new StringBuilder();
        if (identity != null && identity.itemId() != null) {
            searchable.append(identity.itemId().toLowerCase(Locale.ROOT)).append(' ');
        }
        if (stack != null && !stack.isEmpty()) {
            searchable.append(stack.getHoverName().getString().toLowerCase(Locale.ROOT));
        }
        return searchable.toString().contains(normalizedQuery);
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
                lootChestPanel
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
                lootChestPanel
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

    /**
     * Per-identity atlas projection.
     *
     * <p>{@code ghost} = the player isn't carrying this; the card represents
     * stock present in some proximate chest. Renders faded with
     * {@code proximateCount} as the count badge. Hover/zoom reveals
     * per-chest breakdown via {@code presence}.
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
            largestCarriedSourceId = largestCarriedSourceId == null ? "" : largestCarriedSourceId;
            largestCarriedSlotIndex = Math.max(-1, largestCarriedSlotIndex);
            largestCarriedSlotCount = Math.max(0, largestCarriedSlotCount);
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
                    false, "", -1, 0);
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
                    false, "", -1, 0);
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
                    false, "", -1, 0);
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
                    kitNeeded, desiredCount, false, "", -1, 0);
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
                VisualHomeMap visualHomeMap
        ) {
            if (map == null || map.chests().isEmpty() || chestContentsResolver == null
                    || proximate == null || proximate.isEmpty()) {
                return new ProximateGhostProjection(Map.of(), Map.of(), Map.of());
            }
            LinkedHashMap<ItemIdentity, Integer> totals = new LinkedHashMap<>();
            LinkedHashMap<ItemIdentity, LinkedHashMap<UUID, int[]>> perChest = new LinkedHashMap<>();
            LinkedHashMap<ItemIdentity, ItemStack> displayByIdentity = new LinkedHashMap<>();
            LinkedHashMap<UUID, String> labelByStorage = new LinkedHashMap<>();
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
                labelByStorage.put(chest.storageId(), label);
                ChestContentsSnapshot snapshot = chestContentsResolver.apply(storageId);
                if (snapshot == null) {
                    continue;
                }
                for (ItemStack stack : snapshot.contents()) {
                    if (stack == null || stack.isEmpty()) {
                        continue;
                    }
                    ItemIdentity identity = ItemIdentityMatcher.create(stack);
                    totals.merge(identity, stack.getCount(), Integer::sum);
                    perChest
                            .computeIfAbsent(identity, ignored -> new LinkedHashMap<>())
                            .computeIfAbsent(chest.storageId(), ignored -> new int[]{0})[0] += stack.getCount();
                    displayByIdentity.putIfAbsent(identity, stack.copy());
                }
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
            return new ProximateGhostProjection(
                    Map.copyOf(totals),
                    Map.copyOf(presence),
                    Map.copyOf(displayByIdentity)
            );
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
