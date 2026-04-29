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
        List<HotbarSlot> hotbarSlots,
        OffhandSlot offhand,
        List<KitCard> kits
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
        hotbarSlots = hotbarSlots == null ? List.of() : List.copyOf(hotbarSlots);
        offhand = offhand == null ? OffhandSlot.empty() : offhand;
        kits = kits == null ? List.of() : List.copyOf(kits);
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
                emptyHotbar(),
                OffhandSlot.empty(),
                List.of()
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
                chestContentsResolver, proximateStorageIds, null);
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

        // Synthesize ghost accumulators for identities present only in
        // proximate chests (homed-but-not-carried). Carried identities use
        // their existing accumulator + presence pip.
        Set<ItemIdentity> carriedIdentities = new LinkedHashSet<>();
        for (AtlasItemAccumulator accumulator : accumulators) {
            carriedIdentities.add(accumulator.identity());
        }
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

        for (AtlasItemAccumulator accumulator : accumulators) {
            VisualHomeAssignment assignment = visualHomeMap.assignment(accumulator.identity());
            List<ChestPresenceEntry> presence = ghosts.presenceByIdentity().getOrDefault(accumulator.identity(), List.of());
            boolean ghostOnly = !accumulator.carried();
            int proximateCount = ghosts.totalsByIdentity().getOrDefault(accumulator.identity(), 0);

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
                        isContainer,
                        containerFree,
                        containerCapacity
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
                        isContainer,
                        containerFree,
                        containerCapacity
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
                    isContainer,
                    containerFree,
                    containerCapacity
            ));
        }

        triageItems.sort(Comparator
                .comparingInt((AtlasItem item) -> recentRankByIdentity.getOrDefault(item.identity().toIdentity(), Integer.MAX_VALUE))
                .thenComparing(item -> item.name().toLowerCase(Locale.ROOT))
                .thenComparing(item -> item.identity().itemId()));

        List<AtlasIsland> islandsWithCarriedCounts = withCarriedCounts(layoutIslands, atlasItems);

        List<ChestChip> chestChips = chestChips(claimedChestMap, affinityMap, chestContentsResolver, proximate);

        List<KitCard> kitCards = kitCards(resolvedAuthority, resolvedWorkflow.kitMap());
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
                hotbarSlots(resolvedAuthority, selectedQuickAccessSlot),
                OffhandSlot.from(resolvedAuthority),
                kitCards
        );
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

    private static List<KitCard> kitCards(InventoryAuthoritySnapshot authority, KitMap kitMap) {
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
            ArrayList<KitBringItem> bringItems = new ArrayList<>(kit.bring().size());
            int bringReady = 0;
            for (ItemIdentity identity : kit.bring()) {
                if (identity == null) {
                    continue;
                }
                boolean present = carried.contains(identity);
                if (present) {
                    bringReady++;
                }
                ItemStack stack = resolveGhostStack(identity);
                String name = !stack.isEmpty() ? stack.getHoverName().getString() : identity.itemId();
                bringItems.add(new KitBringItem(
                        IdentityRef.from(identity),
                        present,
                        stack,
                        name
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
                    kit.bring().size(),
                    bringReady,
                    renderedSlots,
                    List.copyOf(pages),
                    List.copyOf(bringItems)
            ));
        }
        return List.copyOf(result);
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
                hotbarSlots,
                offhand,
                kits
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
                hotbarSlots,
                offhand,
                kits
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
            Set<String> proximate
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
            for (ItemStack stack : snapshot.contents()) {
                if (stack != null && !stack.isEmpty()) {
                    filledSlots++;
                }
            }
            boolean isProximate = proximate.contains(storageId);
            int affinityCount = affinityMap == null ? 0 : affinityMap.forChest(chest.storageId()).size();
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
                    worldZ
            ));
        }
        chips.sort(Comparator
                .comparing((ChestChip chip) -> !chip.proximate())
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
            boolean isCarriedContainer,
            int containerFreeSlotCount,
            int containerSlotCapacity
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
            containerFreeSlotCount = isCarriedContainer ? Math.max(0, containerFreeSlotCount) : 0;
            containerSlotCapacity = isCarriedContainer ? Math.max(containerFreeSlotCount, containerSlotCapacity) : 0;
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
                    false, 0, 0);
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
            int worldZ
    ) {
        public ChestChip {
            storageId = storageId == null ? "" : storageId;
            dimensionId = dimensionId == null ? "" : dimensionId;
            label = label == null ? "" : label;
            anchorCount = Math.max(1, anchorCount);
            slotCapacity = Math.max(0, slotCapacity);
            filledSlots = Math.max(0, Math.min(filledSlots, slotCapacity));
            affinityIdentities = Math.max(0, affinityIdentities);
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
            String name
    ) {
        public KitBringItem {
            identity = identity == null
                    ? new IdentityRef("", ItemComparisonMode.ITEM_ID.name(), "")
                    : identity;
            displayStack = displayStack == null ? ItemStack.EMPTY : displayStack.copy();
            name = name == null ? "" : name;
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
}
