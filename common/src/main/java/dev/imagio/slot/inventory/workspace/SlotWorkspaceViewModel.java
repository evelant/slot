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
import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ChestLinkMap;
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
import java.util.function.Function;

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
        List<ClaimedChestTile> claimedChestTiles,
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
        claimedChestTiles = claimedChestTiles == null ? List.of() : List.copyOf(claimedChestTiles);
        hotbarSlots = hotbarSlots == null ? List.of() : List.copyOf(hotbarSlots);
        offhand = offhand == null ? OffhandSlot.empty() : offhand;
        kits = kits == null ? List.of() : List.copyOf(kits);
    }

    public SlotWorkspaceViewModel(
            long revision,
            String status,
            String diagnostics,
            int pendingCount,
            int selectedQuickAccessSlot,
            int canvasWidth,
            int canvasHeight,
            List<AtlasIsland> islands,
            List<AtlasItem> atlasItems,
            List<ClaimedChestTile> claimedChestTiles,
            List<HotbarSlot> hotbarSlots,
            OffhandSlot offhand
    ) {
        this(revision, status, diagnostics, pendingCount, selectedQuickAccessSlot,
                canvasWidth, canvasHeight, 0, 0, islands, atlasItems, List.of(), claimedChestTiles,
                hotbarSlots, offhand, List.of());
    }

    public SlotWorkspaceViewModel(
            long revision,
            String status,
            String diagnostics,
            int pendingCount,
            int selectedQuickAccessSlot,
            int canvasWidth,
            int canvasHeight,
            List<AtlasIsland> islands,
            List<AtlasItem> atlasItems,
            List<ClaimedChestTile> claimedChestTiles,
            List<HotbarSlot> hotbarSlots,
            OffhandSlot offhand,
            List<KitCard> kits
    ) {
        this(revision, status, diagnostics, pendingCount, selectedQuickAccessSlot,
                canvasWidth, canvasHeight, 0, 0, islands, atlasItems, List.of(), claimedChestTiles,
                hotbarSlots, offhand, kits);
    }

    public SlotWorkspaceViewModel(
            long revision,
            String status,
            String diagnostics,
            int pendingCount,
            int selectedQuickAccessSlot,
            int canvasWidth,
            int canvasHeight,
            List<AtlasIsland> islands,
            List<AtlasItem> atlasItems,
            List<AtlasItem> triageItems,
            List<ClaimedChestTile> claimedChestTiles,
            List<HotbarSlot> hotbarSlots,
            OffhandSlot offhand,
            List<KitCard> kits
    ) {
        this(revision, status, diagnostics, pendingCount, selectedQuickAccessSlot,
                canvasWidth, canvasHeight, 0, 0, islands, atlasItems, triageItems, claimedChestTiles,
                hotbarSlots, offhand, kits);
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
                SlotWorkspaceAtlasLayout.fittedIslands(
                        SlotWorkspaceAtlasLayout.baseIslands(VisualHomeMap.empty()),
                        List.of()
                ),
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
                null, null);
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
        int[] carriedCounts = countCarriedFreeSlotsAndCapacity(resolvedAuthority);
        int carriedFreeSlotCount = carriedCounts[0];
        int carriedSlotCapacity = carriedCounts[1];
        Function<ItemIdentity, CarriedContainerInfo> containerResolver = carriedContainerInfoResolver == null
                ? identity -> null
                : carriedContainerInfoResolver;

        List<AtlasItemAccumulator> accumulators = groupedAtlasEntries(resolvedAuthority, visualHomeMap);
        Map<ItemIdentity, Integer> recentRankByIdentity = recentRankByIdentity(recents);
        accumulators.sort(Comparator
                .comparingInt((AtlasItemAccumulator accumulator) -> recentRankByIdentity.getOrDefault(accumulator.identity(), Integer.MAX_VALUE))
                .thenComparing(accumulator -> accumulator.name().toLowerCase(Locale.ROOT))
                .thenComparing(accumulator -> accumulator.identity().itemId())
                .thenComparingInt(AtlasItemAccumulator::firstSlotIndex));

        List<AtlasIsland> layoutIslands = SlotWorkspaceAtlasLayout.baseIslands(visualHomeMap);
        ArrayList<AtlasItem> atlasItems = new ArrayList<>();
        ArrayList<AtlasItem> triageItems = new ArrayList<>();
        Set<ItemIdentity> recentIdentities = new LinkedHashSet<>(recents.visibleItems());
        List<TriageIslandRef> triageIslandRefs = triageIslandRefs(visualHomeMap);
        LearnedIslandRuleStore resolvedLearnedRules = learnedRules == null ? new LearnedIslandRuleStore() : learnedRules;

        for (AtlasItemAccumulator accumulator : accumulators) {
            VisualHomeAssignment assignment = visualHomeMap.assignment(accumulator.identity());
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
                triageItems.add(new AtlasItem(
                        IdentityRef.from(accumulator.identity()),
                        accumulator.displayStack(),
                        accumulator.name(),
                        accumulator.totalCount(),
                        accumulator.firstSlotIndex(),
                        SlotWorkspaceAtlasLayout.ISLAND_TRIAGE,
                        0,
                        0,
                        SlotWorkspaceAtlasLayout.CARD_WIDTH,
                        SlotWorkspaceAtlasLayout.CARD_HEIGHT,
                        recentIdentities.contains(accumulator.identity()),
                        false,
                        accumulator.carried(),
                        chipSuggestions,
                        List.of(),
                        isContainer,
                        containerFree,
                        containerCapacity
                ));
                continue;
            }
            SlotWorkspaceAtlasLayout.Placement placement = SlotWorkspaceAtlasLayout.resolvePlacement(
                    layoutIslands,
                    assignment.islandId(),
                    assignment.localX(),
                    assignment.localY()
            );
            String islandId = placement.islandId();
            boolean playerPlaced = assignment.origin() == dev.imagio.slot.workflow.domain.VisualHomeOrigin.PLAYER_PLACED;
            // Orphaned assignment (target island deleted) — fall back to triage rather than render an unrooted card.
            CarriedContainerInfo containerInfo = containerResolver.apply(accumulator.identity());
            boolean isContainer = containerInfo != null;
            int containerFree = isContainer ? containerInfo.freeSlots() : 0;
            int containerCapacity = isContainer ? containerInfo.slotCapacity() : 0;
            if (SlotWorkspaceAtlasLayout.ISLAND_TRIAGE.equals(islandId)
                    && SlotWorkspaceAtlasLayout.island(layoutIslands, islandId) == null) {
                triageItems.add(new AtlasItem(
                        IdentityRef.from(accumulator.identity()),
                        accumulator.displayStack(),
                        accumulator.name(),
                        accumulator.totalCount(),
                        accumulator.firstSlotIndex(),
                        SlotWorkspaceAtlasLayout.ISLAND_TRIAGE,
                        0,
                        0,
                        SlotWorkspaceAtlasLayout.CARD_WIDTH,
                        SlotWorkspaceAtlasLayout.CARD_HEIGHT,
                        recentIdentities.contains(accumulator.identity()),
                        false,
                        accumulator.carried(),
                        List.of(),
                        List.of(),
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
                    placement.x(),
                    placement.y(),
                    SlotWorkspaceAtlasLayout.CARD_WIDTH,
                    SlotWorkspaceAtlasLayout.CARD_HEIGHT,
                    recentIdentities.contains(accumulator.identity()),
                    playerPlaced,
                    accumulator.carried(),
                    List.of(),
                    List.of(),
                    isContainer,
                    containerFree,
                    containerCapacity
            ));
        }

        atlasItems.sort(Comparator
                .comparing(AtlasItem::islandId)
                .thenComparingInt(AtlasItem::y)
                .thenComparingInt(AtlasItem::x)
                .thenComparing(item -> item.name().toLowerCase(Locale.ROOT)));
        triageItems.sort(Comparator
                .comparing((AtlasItem item) -> item.name().toLowerCase(Locale.ROOT))
                .thenComparing(item -> item.identity().itemId()));

        List<AtlasIsland> fittedIslands = SlotWorkspaceAtlasLayout.fittedIslands(layoutIslands, atlasItems);
        List<AtlasIsland> islandsWithCarriedCounts = withCarriedCounts(fittedIslands, atlasItems);

        List<ClaimedChestTile> tiles = claimedChestTiles(
                resolvedWorkflow.claimedChestMap(),
                resolvedWorkflow.chestLinkMap(),
                chestContentsResolver,
                proximateStorageIds
        );
        Map<String, List<ChestPresenceEntry>> presenceByItemId = presenceByItemId(tiles);
        if (!presenceByItemId.isEmpty()) {
            for (int index = 0; index < atlasItems.size(); index++) {
                AtlasItem item = atlasItems.get(index);
                List<ChestPresenceEntry> entries = presenceByItemId.get(item.identity().itemId());
                if (entries != null && !entries.isEmpty()) {
                    atlasItems.set(index, item.withPresence(entries));
                }
            }
            for (int index = 0; index < triageItems.size(); index++) {
                AtlasItem item = triageItems.get(index);
                List<ChestPresenceEntry> entries = presenceByItemId.get(item.identity().itemId());
                if (entries != null && !entries.isEmpty()) {
                    triageItems.set(index, item.withPresence(entries));
                }
            }
        }
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
                tiles,
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
            KitPage page = kit.page(renderedPage);
            if (page == null) {
                continue;
            }
            ArrayList<KitSlotState> slots = new ArrayList<>(KitPage.HOTBAR_SLOT_COUNT);
            int ready = 0;
            for (int slotIndex = 0; slotIndex < KitPage.HOTBAR_SLOT_COUNT; slotIndex++) {
                ItemIdentity identity = page.slot(slotIndex);
                boolean filled = identity != null;
                boolean present = filled && carried.contains(identity);
                if (filled && present) {
                    ready++;
                }
                ItemStack stack = filled ? resolveGhostStack(identity) : ItemStack.EMPTY;
                String name = filled && !stack.isEmpty() ? stack.getHoverName().getString() : filled ? identity.itemId() : "";
                slots.add(new KitSlotState(
                        slotIndex,
                        filled,
                        present,
                        IdentityRef.from(identity),
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
                    ready,
                    List.copyOf(slots)
            ));
        }
        return List.copyOf(result);
    }

    private static Set<ItemIdentity> carriedIdentities(InventoryAuthoritySnapshot authority) {
        LinkedHashSet<ItemIdentity> identities = new LinkedHashSet<>();
        if (authority == null) {
            return identities;
        }
        // Iterate every source flagged as the CARRIED pane (player main,
        // hotbar, offhand, armor, *and* carried backpacks). A hardcoded
        // player-only lane list used to miss Sophisticated Backpack contents.
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
                claimedChestTiles,
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

    public ClaimedChestTile claimedChestTile(String storageId) {
        if (storageId == null || storageId.isBlank()) {
            return null;
        }
        for (ClaimedChestTile tile : claimedChestTiles) {
            if (tile.storageId().equals(storageId)) {
                return tile;
            }
        }
        return null;
    }

    private static List<ClaimedChestTile> claimedChestTiles(
            ClaimedChestMap map,
            ChestLinkMap linkMap,
            Function<String, ChestContentsSnapshot> chestContentsResolver,
            Set<String> proximateStorageIds
    ) {
        if (map == null || map.chests().isEmpty()) {
            return List.of();
        }
        ChestLinkMap resolvedLinkMap = linkMap == null ? ChestLinkMap.empty() : linkMap;
        ArrayList<ClaimedChestTile> tiles = new ArrayList<>(map.chests().size());
        for (ClaimedChest chest : map.chests()) {
            if (chest == null) {
                continue;
            }
            ChestAnchor primary = chest.anchors().iterator().next();
            String dimension = primary == null ? "" : primary.dimensionId();
            String label = chest.label() == null || chest.label().isBlank()
                    ? autoLabel(chest)
                    : chest.label();
            String storageId = chest.storageId().toString();
            ChestContentsSnapshot snapshot = chestContentsResolver == null
                    ? ChestContentsSnapshot.empty()
                    : chestContentsResolver.apply(storageId);
            if (snapshot == null) {
                snapshot = ChestContentsSnapshot.empty();
            }
            boolean proximate = proximateStorageIds != null && proximateStorageIds.contains(storageId);
            int width = SlotWorkspaceAtlasLayout.chestTileWidth();
            int height = SlotWorkspaceAtlasLayout.chestTileHeight(snapshot.contents().size());
            List<String> linkedIslandIds = List.copyOf(resolvedLinkMap.islandsLinkedTo(chest.storageId()));
            tiles.add(new ClaimedChestTile(
                    storageId,
                    dimension,
                    chest.atlasX(),
                    chest.atlasY(),
                    width,
                    height,
                    label,
                    chest.anchors().size(),
                    snapshot.slotCount(),
                    snapshot.contents(),
                    snapshot.slotIndices(),
                    proximate,
                    linkedIslandIds
            ));
        }
        return List.copyOf(tiles);
    }

    private static Map<String, List<ChestPresenceEntry>> presenceByItemId(List<ClaimedChestTile> tiles) {
        if (tiles == null || tiles.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, LinkedHashMap<String, int[]>> counts = new LinkedHashMap<>();
        LinkedHashMap<String, String> labelByStorageId = new LinkedHashMap<>();
        for (ClaimedChestTile tile : tiles) {
            if (tile == null || tile.contents().isEmpty()) {
                continue;
            }
            labelByStorageId.put(tile.storageId(), tile.label());
            for (ItemStack stack : tile.contents()) {
                if (stack == null || stack.isEmpty()) {
                    continue;
                }
                String itemId = ItemIdentityMatcher.create(stack).itemId();
                counts.computeIfAbsent(itemId, ignored -> new LinkedHashMap<>())
                        .computeIfAbsent(tile.storageId(), ignored -> new int[]{0})[0] += stack.getCount();
            }
        }
        LinkedHashMap<String, List<ChestPresenceEntry>> result = new LinkedHashMap<>();
        for (Map.Entry<String, LinkedHashMap<String, int[]>> entry : counts.entrySet()) {
            ArrayList<ChestPresenceEntry> list = new ArrayList<>(entry.getValue().size());
            for (Map.Entry<String, int[]> chestEntry : entry.getValue().entrySet()) {
                String storageId = chestEntry.getKey();
                int count = chestEntry.getValue()[0];
                if (count <= 0) {
                    continue;
                }
                list.add(new ChestPresenceEntry(storageId, labelByStorageId.getOrDefault(storageId, storageId), count));
            }
            list.sort(Comparator.<ChestPresenceEntry>comparingInt(ChestPresenceEntry::count).reversed()
                    .thenComparing(ChestPresenceEntry::label, String.CASE_INSENSITIVE_ORDER));
            result.put(entry.getKey(), List.copyOf(list));
        }
        return Map.copyOf(result);
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

    private static List<AtlasItemAccumulator> groupedAtlasEntries(
            InventoryAuthoritySnapshot authority,
            VisualHomeMap visualHomeMap
    ) {
        LinkedHashMap<ItemIdentity, AtlasItemAccumulator> byIdentity = new LinkedHashMap<>();
        // Walk every CARRIED-pane source — includes carried backpacks that
        // the Sophisticated Backpacks provider contributes via
        // PlayerInventoryExtension.additionalSources(). Previously we only
        // looped a hardcoded list of player lanes, which is why backpack
        // contents never appeared in the atlas / triage.
        for (InventorySourceDescriptor source : authority.carriedSources()) {
            for (InventoryEntrySnapshot entry : authority.entries(source.id())) {
                if (entry == null || !entry.present()) {
                    continue;
                }
                ItemIdentity identity = ItemIdentityMatcher.create(entry.stack());
                byIdentity.computeIfAbsent(identity, ignored -> new AtlasItemAccumulator(identity, entry)).add(entry);
            }
        }
        if (visualHomeMap != null) {
            for (ItemIdentity identity : visualHomeMap.assignments().keySet()) {
                if (identity == null || byIdentity.containsKey(identity)) {
                    continue;
                }
                AtlasItemAccumulator ghost = ghostAccumulator(identity);
                if (ghost != null) {
                    byIdentity.put(identity, ghost);
                }
            }
        }
        return new ArrayList<>(byIdentity.values());
    }

    private static java.util.function.Function<String, ItemStack> ghostStackResolver = identity -> ItemStack.EMPTY;

    public static void setGhostStackResolver(java.util.function.Function<String, ItemStack> resolver) {
        ghostStackResolver = resolver == null ? id -> ItemStack.EMPTY : resolver;
    }

    private static AtlasItemAccumulator ghostAccumulator(ItemIdentity identity) {
        ItemStack stack = ItemStack.EMPTY;
        String name = identity.itemId();
        try {
            ItemStack resolved = ghostStackResolver.apply(identity.itemId());
            if (resolved != null && !resolved.isEmpty()) {
                stack = resolved;
                name = resolved.getHoverName().getString();
            }
        } catch (RuntimeException | LinkageError ignored) {
        }
        return new AtlasItemAccumulator(identity, stack, name);
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
            int x,
            int y,
            int width,
            int height,
            int color,
            int itemCount,
            int carriedCount
    ) {
        public AtlasIsland {
            islandId = islandId == null ? "" : islandId;
            label = label == null || label.isBlank() ? islandId : label;
            kind = kind == null ? VisualAtlasIslandKind.PLAYER : kind;
            width = Math.max(96, width);
            height = Math.max(72, height);
            itemCount = Math.max(0, itemCount);
            carriedCount = Math.max(0, carriedCount);
        }

        public AtlasIsland(
                String islandId,
                String label,
                VisualAtlasIslandKind kind,
                int x,
                int y,
                int width,
                int height,
                int color,
                int itemCount
        ) {
            this(islandId, label, kind, x, y, width, height, color, itemCount, 0);
        }

        public AtlasIsland withCarriedCount(int newCarriedCount) {
            return new AtlasIsland(islandId, label, kind, x, y, width, height, color, itemCount, newCarriedCount);
        }
    }

    public record AtlasItem(
            IdentityRef identity,
            ItemStack displayStack,
            String name,
            int totalCount,
            int firstSlotIndex,
            String islandId,
            int x,
            int y,
            int width,
            int height,
            boolean recent,
            boolean playerPlaced,
            boolean carried,
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
            width = Math.max(SlotWorkspaceAtlasLayout.CARD_WIDTH, width);
            height = Math.max(SlotWorkspaceAtlasLayout.CARD_HEIGHT, height);
            chipSuggestions = chipSuggestions == null ? List.of() : List.copyOf(chipSuggestions);
            presence = presence == null ? List.of() : List.copyOf(presence);
            containerFreeSlotCount = isCarriedContainer ? Math.max(0, containerFreeSlotCount) : 0;
            containerSlotCapacity = isCarriedContainer ? Math.max(containerFreeSlotCount, containerSlotCapacity) : 0;
        }

        public AtlasItem(
                IdentityRef identity,
                ItemStack displayStack,
                String name,
                int totalCount,
                int firstSlotIndex,
                String islandId,
                int x,
                int y,
                int width,
                int height,
                boolean recent,
                boolean playerPlaced,
                boolean carried,
                List<ChipSuggestion> chipSuggestions
        ) {
            this(identity, displayStack, name, totalCount, firstSlotIndex, islandId, x, y, width, height,
                    recent, playerPlaced, carried, chipSuggestions, List.of(), false, 0, 0);
        }

        public AtlasItem(
                IdentityRef identity,
                ItemStack displayStack,
                String name,
                int totalCount,
                int firstSlotIndex,
                String islandId,
                int x,
                int y,
                int width,
                int height,
                boolean recent,
                boolean playerPlaced,
                boolean carried,
                List<ChipSuggestion> chipSuggestions,
                List<ChestPresenceEntry> presence
        ) {
            this(identity, displayStack, name, totalCount, firstSlotIndex, islandId, x, y, width, height,
                    recent, playerPlaced, carried, chipSuggestions, presence, false, 0, 0);
        }

        public AtlasItem withPresence(List<ChestPresenceEntry> entries) {
            return new AtlasItem(identity, displayStack, name, totalCount, firstSlotIndex, islandId, x, y, width, height,
                    recent, playerPlaced, carried, chipSuggestions, entries, isCarriedContainer,
                    containerFreeSlotCount, containerSlotCapacity);
        }
    }

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

    public record ClaimedChestTile(
            String storageId,
            String dimensionId,
            int atlasX,
            int atlasY,
            int width,
            int height,
            String label,
            int anchorCount,
            int slotCount,
            List<ItemStack> contents,
            List<Integer> contentSlotIndices,
            boolean proximate,
            List<String> linkedIslandIds
    ) {
        public ClaimedChestTile {
            storageId = storageId == null ? "" : storageId;
            dimensionId = dimensionId == null ? "" : dimensionId;
            width = Math.max(1, width);
            height = Math.max(1, height);
            label = label == null ? "" : label;
            anchorCount = Math.max(1, anchorCount);
            slotCount = Math.max(0, slotCount);
            List<ItemStack> sourceContents = contents == null ? List.of() : contents;
            List<Integer> sourceIndices = contentSlotIndices == null ? List.of() : contentSlotIndices;
            ArrayList<ItemStack> copiedContents = new ArrayList<>(sourceContents.size());
            ArrayList<Integer> copiedIndices = new ArrayList<>(sourceContents.size());
            for (int i = 0; i < sourceContents.size(); i++) {
                ItemStack stack = sourceContents.get(i);
                if (stack == null || stack.isEmpty()) {
                    continue;
                }
                copiedContents.add(stack.copy());
                int slotIdx = i < sourceIndices.size() && sourceIndices.get(i) != null ? sourceIndices.get(i) : i;
                copiedIndices.add(Math.max(0, slotIdx));
            }
            contents = List.copyOf(copiedContents);
            contentSlotIndices = List.copyOf(copiedIndices);
            if (linkedIslandIds == null) {
                linkedIslandIds = List.of();
            } else {
                LinkedHashSet<String> filtered = new LinkedHashSet<>();
                for (String islandId : linkedIslandIds) {
                    if (islandId != null && !islandId.isBlank()) {
                        filtered.add(islandId);
                    }
                }
                linkedIslandIds = List.copyOf(filtered);
            }
        }

        public ClaimedChestTile(
                String storageId,
                String dimensionId,
                int atlasX,
                int atlasY,
                int width,
                int height,
                String label,
                int anchorCount,
                int slotCount,
                List<ItemStack> contents,
                boolean proximate,
                List<String> linkedIslandIds
        ) {
            this(storageId, dimensionId, atlasX, atlasY, width, height, label, anchorCount,
                    slotCount, contents, List.of(), proximate, linkedIslandIds);
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
            List<KitSlotState> slots
    ) {
        public KitCard {
            kitId = kitId == null ? "" : kitId;
            name = name == null || name.isBlank() ? kitId : name;
            pageCount = Math.max(1, pageCount);
            activePageIndex = Math.max(0, Math.min(activePageIndex, pageCount - 1));
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

        private AtlasItemAccumulator(ItemIdentity identity, ItemStack ghostStack, String ghostName) {
            this.identity = identity;
            this.displayStack = ghostStack;
            this.name = ghostName;
            this.firstSlotIndex = Integer.MAX_VALUE;
            this.totalCount = 0;
            this.carried = false;
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
}
