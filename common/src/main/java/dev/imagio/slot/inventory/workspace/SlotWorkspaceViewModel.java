package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
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
import dev.imagio.slot.workflow.domain.CollectionDefinition;
import dev.imagio.slot.workflow.domain.CollectionProjection;
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
        List<AtlasIsland> islands,
        List<AtlasItem> atlasItems,
        List<CollectionEntry> collections,
        List<HotbarSlot> hotbarSlots,
        OffhandSlot offhand
) {
    public SlotWorkspaceViewModel {
        status = status == null || status.isBlank() ? "ready" : status;
        diagnostics = diagnostics == null ? "" : diagnostics;
        pendingCount = Math.max(0, pendingCount);
        canvasWidth = Math.max(1, canvasWidth);
        canvasHeight = Math.max(1, canvasHeight);
        islands = islands == null ? List.of() : List.copyOf(islands);
        atlasItems = atlasItems == null ? List.of() : List.copyOf(atlasItems);
        collections = collections == null ? List.of() : List.copyOf(collections);
        hotbarSlots = hotbarSlots == null ? List.of() : List.copyOf(hotbarSlots);
        offhand = offhand == null ? OffhandSlot.empty() : offhand;
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
                SlotWorkspaceAtlasLayout.fittedIslands(
                        SlotWorkspaceAtlasLayout.baseIslands(VisualHomeMap.empty()),
                        List.of()
                ),
                List.of(),
                List.of(),
                emptyHotbar(),
                OffhandSlot.empty()
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
        InventoryAuthoritySnapshot resolvedAuthority = authority == null ? InventoryAuthoritySnapshot.empty() : authority;
        WorkflowDomainSnapshot resolvedWorkflow = workflow == null ? WorkflowDomainSnapshot.empty() : workflow;
        CollectionProjection collectionsProjection = resolvedWorkflow.collections();
        RecentView recents = resolvedWorkflow.recents();
        VisualHomeMap visualHomeMap = resolvedWorkflow.visualHomeMap();

        List<AtlasItemAccumulator> accumulators = groupedAtlasEntries(resolvedAuthority, visualHomeMap);
        Map<ItemIdentity, Integer> recentRankByIdentity = recentRankByIdentity(recents);
        accumulators.sort(Comparator
                .comparingInt((AtlasItemAccumulator accumulator) -> recentRankByIdentity.getOrDefault(accumulator.identity(), Integer.MAX_VALUE))
                .thenComparing(accumulator -> accumulator.name().toLowerCase(Locale.ROOT))
                .thenComparing(accumulator -> accumulator.identity().itemId())
                .thenComparingInt(AtlasItemAccumulator::firstSlotIndex));

        List<AtlasIsland> layoutIslands = SlotWorkspaceAtlasLayout.baseIslands(visualHomeMap);
        ArrayList<AtlasItem> atlasItems = new ArrayList<>();
        LinkedHashMap<String, Integer> fallbackOrdinalByIsland = new LinkedHashMap<>();
        Set<ItemIdentity> recentIdentities = new LinkedHashSet<>(recents.visibleItems());
        List<TriageIslandRef> triageIslandRefs = triageIslandRefs(visualHomeMap);
        LearnedIslandRuleStore resolvedLearnedRules = learnedRules == null ? new LearnedIslandRuleStore() : learnedRules;

        for (AtlasItemAccumulator accumulator : accumulators) {
            VisualHomeAssignment assignment = visualHomeMap.assignment(accumulator.identity());
            String islandId = assignment == null
                    ? SlotWorkspaceAtlasLayout.ISLAND_TRIAGE
                    : assignment.islandId();
            SlotWorkspaceAtlasLayout.Placement placement;
            boolean playerPlaced = false;
            if (assignment != null) {
                placement = SlotWorkspaceAtlasLayout.resolvePlacement(
                        layoutIslands,
                        islandId,
                        assignment.localX(),
                        assignment.localY()
                );
                islandId = placement.islandId();
                playerPlaced = assignment.origin() == dev.imagio.slot.workflow.domain.VisualHomeOrigin.PLAYER_PLACED;
            } else {
                int ordinal = fallbackOrdinalByIsland.getOrDefault(islandId, 0);
                fallbackOrdinalByIsland.put(islandId, ordinal + 1);
                placement = SlotWorkspaceAtlasLayout.placementForOrdinal(layoutIslands, islandId, ordinal);
                islandId = placement.islandId();
            }
            List<ChipSuggestion> chipSuggestions = List.of();
            if (assignment == null && accumulator.carried() && signalExtractor != null) {
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
                    List.copyOf(collectionsProjection.memberships().getOrDefault(accumulator.identity(), Set.of())),
                    chipSuggestions
            ));
        }

        atlasItems.sort(Comparator
                .comparing(AtlasItem::islandId)
                .thenComparingInt(AtlasItem::y)
                .thenComparingInt(AtlasItem::x)
                .thenComparing(item -> item.name().toLowerCase(Locale.ROOT)));

        List<AtlasIsland> fittedIslands = SlotWorkspaceAtlasLayout.fittedIslands(layoutIslands, atlasItems);
        List<AtlasIsland> islandsWithCarriedCounts = withCarriedCounts(fittedIslands, atlasItems);

        List<CollectionEntry> collectionEntries = collections(collectionsProjection);
        return new SlotWorkspaceViewModel(
                revision,
                status,
                diagnostics,
                pendingCount,
                selectedQuickAccessSlot,
                SlotWorkspaceAtlasLayout.CANVAS_WIDTH,
                SlotWorkspaceAtlasLayout.CANVAS_HEIGHT,
                islandsWithCarriedCounts,
                atlasItems,
                collectionEntries,
                hotbarSlots(resolvedAuthority, selectedQuickAccessSlot),
                OffhandSlot.from(resolvedAuthority)
        );
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
                islands,
                atlasItems,
                collections,
                hotbarSlots,
                offhand
        );
    }

    public AtlasItem atlasItem(IdentityRef identityRef) {
        if (identityRef == null) {
            return null;
        }
        return atlasItems.stream()
                .filter(item -> item.identity().equals(identityRef))
                .findFirst()
                .orElse(null);
    }

    public AtlasIsland island(String islandId) {
        return SlotWorkspaceAtlasLayout.island(islands, islandId);
    }

    public String collectionLabel(String collectionId) {
        if (collectionId == null || collectionId.isBlank()) {
            return "";
        }
        return collections.stream()
                .filter(collection -> collection.collectionId().equals(collectionId))
                .map(CollectionEntry::label)
                .findFirst()
                .orElse(collectionId);
    }

    private static final String[] CARRIED_LANE_IDS = new String[]{
            BuiltinInventoryIds.PLAYER_MAIN,
            BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0,
            BuiltinInventoryIds.PLAYER_OFFHAND
    };

    private static List<AtlasItemAccumulator> groupedAtlasEntries(
            InventoryAuthoritySnapshot authority,
            VisualHomeMap visualHomeMap
    ) {
        LinkedHashMap<ItemIdentity, AtlasItemAccumulator> byIdentity = new LinkedHashMap<>();
        for (String laneId : CARRIED_LANE_IDS) {
            for (InventoryEntrySnapshot entry : authority.entries(laneId)) {
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

    private static List<CollectionEntry> collections(CollectionProjection projection) {
        if (projection == null || projection.userCollections().isEmpty()) {
            return List.of();
        }
        ArrayList<CollectionEntry> collections = new ArrayList<>();
        for (CollectionDefinition definition : projection.userCollections()) {
            if (definition == null) {
                continue;
            }
            int memberCount = 0;
            for (Set<String> collectionIds : projection.memberships().values()) {
                if (collectionIds != null && collectionIds.contains(definition.id())) {
                    memberCount++;
                }
            }
            collections.add(new CollectionEntry(definition.id(), definition.name(), memberCount));
        }
        collections.sort(Comparator.comparing(entry -> entry.label().toLowerCase(Locale.ROOT)));
        return List.copyOf(collections);
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
            List<String> collectionIds,
            List<ChipSuggestion> chipSuggestions
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
            collectionIds = collectionIds == null ? List.of() : List.copyOf(collectionIds);
            chipSuggestions = chipSuggestions == null ? List.of() : List.copyOf(chipSuggestions);
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
                List<String> collectionIds,
                List<ChipSuggestion> chipSuggestions
        ) {
            this(identity, displayStack, name, totalCount, firstSlotIndex, islandId, x, y, width, height,
                    recent, playerPlaced, totalCount > 0, collectionIds, chipSuggestions);
        }
    }

    public record CollectionEntry(
            String collectionId,
            String label,
            int memberCount
    ) {
        public CollectionEntry {
            collectionId = collectionId == null ? "" : collectionId;
            label = label == null || label.isBlank() ? collectionId : label;
            memberCount = Math.max(0, memberCount);
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
