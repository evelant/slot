package dev.imagio.slot.neoforge.screen.ldlib;

import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.ItemComparisonMode;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.workflow.domain.CollectionDefinition;
import dev.imagio.slot.workflow.domain.CollectionProjection;
import dev.imagio.slot.workflow.domain.RecentView;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import dev.imagio.slot.workflow.domain.VisualHomeAssignment;
import dev.imagio.slot.workflow.domain.VisualHomeMap;
import dev.imagio.slot.workflow.domain.WorkflowDomainSnapshot;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
        InventoryAuthoritySnapshot resolvedAuthority = authority == null ? InventoryAuthoritySnapshot.empty() : authority;
        WorkflowDomainSnapshot resolvedWorkflow = workflow == null ? WorkflowDomainSnapshot.empty() : workflow;
        CollectionProjection collectionsProjection = resolvedWorkflow.collections();
        RecentView recents = resolvedWorkflow.recents();
        VisualHomeMap visualHomeMap = resolvedWorkflow.visualHomeMap();

        List<MainAccumulator> accumulators = groupedMainEntries(resolvedAuthority);
        Map<ItemIdentity, Integer> recentRankByIdentity = recentRankByIdentity(recents);
        accumulators.sort(Comparator
                .comparingInt((MainAccumulator accumulator) -> recentRankByIdentity.getOrDefault(accumulator.identity(), Integer.MAX_VALUE))
                .thenComparing(accumulator -> accumulator.name().toLowerCase(Locale.ROOT))
                .thenComparing(accumulator -> accumulator.identity().itemId())
                .thenComparingInt(MainAccumulator::firstSlotIndex));

        List<AtlasIsland> layoutIslands = SlotWorkspaceAtlasLayout.baseIslands(visualHomeMap);
        ArrayList<AtlasItem> atlasItems = new ArrayList<>();
        LinkedHashMap<String, Integer> fallbackOrdinalByIsland = new LinkedHashMap<>();
        Set<ItemIdentity> recentIdentities = new LinkedHashSet<>(recents.visibleItems());

        for (MainAccumulator accumulator : accumulators) {
            VisualHomeAssignment assignment = visualHomeMap.assignment(accumulator.identity());
            String islandId = assignment == null
                    ? SlotWorkspaceAtlasLayout.defaultIslandId(accumulator.displayStack())
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
                    List.copyOf(collectionsProjection.memberships().getOrDefault(accumulator.identity(), Set.of()))
            ));
        }

        atlasItems.sort(Comparator
                .comparing(AtlasItem::islandId)
                .thenComparingInt(AtlasItem::y)
                .thenComparingInt(AtlasItem::x)
                .thenComparing(item -> item.name().toLowerCase(Locale.ROOT)));

        List<CollectionEntry> collectionEntries = collections(collectionsProjection);
        return new SlotWorkspaceViewModel(
                revision,
                status,
                diagnostics,
                pendingCount,
                selectedQuickAccessSlot,
                SlotWorkspaceAtlasLayout.CANVAS_WIDTH,
                SlotWorkspaceAtlasLayout.CANVAS_HEIGHT,
                SlotWorkspaceAtlasLayout.fittedIslands(layoutIslands, atlasItems),
                atlasItems,
                collectionEntries,
                hotbarSlots(resolvedAuthority, selectedQuickAccessSlot),
                OffhandSlot.from(resolvedAuthority)
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

    public CompoundTag toTag(HolderLookup.Provider provider) {
        return toTag(provider, true);
    }

    public CompoundTag toTag(HolderLookup.Provider provider, boolean includeRevision) {
        CompoundTag tag = new CompoundTag();
        if (includeRevision) {
            tag.putLong("revision", revision);
        }
        tag.putString("status", status);
        tag.putString("diagnostics", diagnostics);
        tag.putInt("pendingCount", pendingCount);
        tag.putInt("selectedQuickAccessSlot", selectedQuickAccessSlot);
        tag.putInt("canvasWidth", canvasWidth);
        tag.putInt("canvasHeight", canvasHeight);

        ListTag islandTags = new ListTag();
        for (AtlasIsland island : islands) {
            islandTags.add(island.toTag());
        }
        tag.put("islands", islandTags);

        ListTag itemTags = new ListTag();
        for (AtlasItem atlasItem : atlasItems) {
            itemTags.add(atlasItem.toTag(provider));
        }
        tag.put("atlasItems", itemTags);

        ListTag collectionTags = new ListTag();
        for (CollectionEntry collection : collections) {
            collectionTags.add(collection.toTag());
        }
        tag.put("collections", collectionTags);

        ListTag hotbarTags = new ListTag();
        for (HotbarSlot slot : hotbarSlots) {
            hotbarTags.add(slot.toTag(provider));
        }
        tag.put("hotbarSlots", hotbarTags);
        tag.put("offhand", offhand.toTag(provider));
        return tag;
    }

    public static SlotWorkspaceViewModel fromTag(HolderLookup.Provider provider, Tag tag) {
        if (!(tag instanceof CompoundTag compoundTag)) {
            return empty();
        }

        ArrayList<AtlasIsland> islands = new ArrayList<>();
        ListTag islandTags = compoundTag.getList("islands", Tag.TAG_COMPOUND);
        for (int index = 0; index < islandTags.size(); index++) {
            islands.add(AtlasIsland.fromTag(islandTags.getCompound(index)));
        }

        ArrayList<AtlasItem> atlasItems = new ArrayList<>();
        ListTag itemTags = compoundTag.getList("atlasItems", Tag.TAG_COMPOUND);
        for (int index = 0; index < itemTags.size(); index++) {
            atlasItems.add(AtlasItem.fromTag(provider, itemTags.getCompound(index)));
        }

        ArrayList<CollectionEntry> collections = new ArrayList<>();
        ListTag collectionTags = compoundTag.getList("collections", Tag.TAG_COMPOUND);
        for (int index = 0; index < collectionTags.size(); index++) {
            collections.add(CollectionEntry.fromTag(collectionTags.getCompound(index)));
        }

        ArrayList<HotbarSlot> hotbarSlots = new ArrayList<>();
        ListTag hotbarTags = compoundTag.getList("hotbarSlots", Tag.TAG_COMPOUND);
        for (int index = 0; index < hotbarTags.size(); index++) {
            hotbarSlots.add(HotbarSlot.fromTag(provider, hotbarTags.getCompound(index)));
        }

        return new SlotWorkspaceViewModel(
                compoundTag.getLong("revision"),
                compoundTag.getString("status"),
                compoundTag.getString("diagnostics"),
                compoundTag.getInt("pendingCount"),
                compoundTag.getInt("selectedQuickAccessSlot"),
                compoundTag.getInt("canvasWidth"),
                compoundTag.getInt("canvasHeight"),
                islands.isEmpty()
                        ? SlotWorkspaceAtlasLayout.fittedIslands(
                        SlotWorkspaceAtlasLayout.baseIslands(VisualHomeMap.empty()),
                        List.of()
                )
                        : islands,
                atlasItems,
                collections,
                hotbarSlots.isEmpty() ? emptyHotbar() : hotbarSlots,
                OffhandSlot.fromTag(provider, compoundTag.getCompound("offhand"))
        );
    }

    private static List<MainAccumulator> groupedMainEntries(InventoryAuthoritySnapshot authority) {
        Map<ItemIdentity, MainAccumulator> byIdentity = new LinkedHashMap<>();
        for (InventoryEntrySnapshot entry : authority.entries(BuiltinInventoryIds.PLAYER_MAIN)) {
            if (entry == null || !entry.present()) {
                continue;
            }
            ItemIdentity identity = ItemIdentityMatcher.create(entry.stack());
            byIdentity.computeIfAbsent(identity, ignored -> new MainAccumulator(identity, entry)).add(entry);
        }
        return new ArrayList<>(byIdentity.values());
    }

    private static Map<ItemIdentity, Integer> recentRankByIdentity(RecentView recentView) {
        LinkedHashMap<ItemIdentity, Integer> ranks = new LinkedHashMap<>();
        List<ItemIdentity> visible = recentView == null ? List.of() : recentView.visibleItems();
        for (int index = 0; index < visible.size(); index++) {
            ranks.put(visible.get(index), index);
        }
        return Map.copyOf(ranks);
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

    private static List<HotbarSlot> emptyHotbar() {
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

        static IdentityRef from(ItemIdentity identity) {
            return identity == null
                    ? new IdentityRef("", ItemComparisonMode.ITEM_ID.name(), "")
                    : new IdentityRef(identity.itemId(), identity.comparisonMode().name(), identity.componentFingerprint());
        }

        ItemIdentity toIdentity() {
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

        CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
            tag.putString("itemId", itemId);
            tag.putString("comparisonMode", comparisonMode);
            tag.putString("componentFingerprint", componentFingerprint);
            return tag;
        }

        static IdentityRef fromTag(CompoundTag tag) {
            return new IdentityRef(
                    tag.getString("itemId"),
                    tag.getString("comparisonMode"),
                    tag.getString("componentFingerprint")
            );
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
            int itemCount
    ) {
        public AtlasIsland {
            islandId = islandId == null ? "" : islandId;
            label = label == null || label.isBlank() ? islandId : label;
            kind = kind == null ? VisualAtlasIslandKind.PLAYER : kind;
            width = Math.max(96, width);
            height = Math.max(72, height);
            itemCount = Math.max(0, itemCount);
        }

        private CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
            tag.putString("islandId", islandId);
            tag.putString("label", label);
            tag.putString("kind", kind.name());
            tag.putInt("x", x);
            tag.putInt("y", y);
            tag.putInt("width", width);
            tag.putInt("height", height);
            tag.putInt("color", color);
            tag.putInt("itemCount", itemCount);
            return tag;
        }

        private static AtlasIsland fromTag(CompoundTag tag) {
            return new AtlasIsland(
                    tag.getString("islandId"),
                    tag.getString("label"),
                    decodeIslandKind(tag.getString("kind")),
                    tag.getInt("x"),
                    tag.getInt("y"),
                    tag.getInt("width"),
                    tag.getInt("height"),
                    tag.getInt("color"),
                    tag.getInt("itemCount")
            );
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
            List<String> collectionIds
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
        }

        private CompoundTag toTag(HolderLookup.Provider provider) {
            CompoundTag tag = new CompoundTag();
            tag.put("identity", identity.toTag());
            tag.put("displayStack", displayStack.saveOptional(provider));
            tag.putString("name", name);
            tag.putInt("totalCount", totalCount);
            tag.putInt("firstSlotIndex", firstSlotIndex);
            tag.putString("islandId", islandId);
            tag.putInt("x", x);
            tag.putInt("y", y);
            tag.putInt("width", width);
            tag.putInt("height", height);
            tag.putBoolean("recent", recent);
            tag.putBoolean("playerPlaced", playerPlaced);
            ListTag collectionTags = new ListTag();
            for (String collectionId : collectionIds) {
                CompoundTag collectionTag = new CompoundTag();
                collectionTag.putString("collectionId", collectionId);
                collectionTags.add(collectionTag);
            }
            tag.put("collectionIds", collectionTags);
            return tag;
        }

        private static AtlasItem fromTag(HolderLookup.Provider provider, CompoundTag tag) {
            ArrayList<String> collectionIds = new ArrayList<>();
            ListTag collectionTags = tag.getList("collectionIds", Tag.TAG_COMPOUND);
            for (int index = 0; index < collectionTags.size(); index++) {
                collectionIds.add(collectionTags.getCompound(index).getString("collectionId"));
            }
            return new AtlasItem(
                    IdentityRef.fromTag(tag.getCompound("identity")),
                    ItemStack.parseOptional(provider, tag.getCompound("displayStack")),
                    tag.getString("name"),
                    tag.getInt("totalCount"),
                    tag.getInt("firstSlotIndex"),
                    tag.getString("islandId"),
                    tag.getInt("x"),
                    tag.getInt("y"),
                    tag.getInt("width"),
                    tag.getInt("height"),
                    tag.getBoolean("recent"),
                    tag.getBoolean("playerPlaced"),
                    collectionIds
            );
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

        private CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
            tag.putString("collectionId", collectionId);
            tag.putString("label", label);
            tag.putInt("memberCount", memberCount);
            return tag;
        }

        private static CollectionEntry fromTag(CompoundTag tag) {
            return new CollectionEntry(
                    tag.getString("collectionId"),
                    tag.getString("label"),
                    tag.getInt("memberCount")
            );
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

        private CompoundTag toTag(HolderLookup.Provider provider) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("hotbarIndex", hotbarIndex);
            tag.putBoolean("selected", selected);
            tag.putBoolean("occupied", occupied);
            tag.put("displayStack", displayStack.saveOptional(provider));
            tag.putInt("count", count);
            return tag;
        }

        private static HotbarSlot fromTag(HolderLookup.Provider provider, CompoundTag tag) {
            return new HotbarSlot(
                    tag.getInt("hotbarIndex"),
                    tag.getBoolean("selected"),
                    tag.getBoolean("occupied"),
                    ItemStack.parseOptional(provider, tag.getCompound("displayStack")),
                    tag.getInt("count")
            );
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

        static OffhandSlot empty() {
            return new OffhandSlot(false, ItemStack.EMPTY, 0);
        }

        static OffhandSlot from(InventoryAuthoritySnapshot authority) {
            InventoryEntrySnapshot entry = authority.slotEntry(BuiltinInventoryIds.PLAYER_OFFHAND, 0);
            boolean occupied = entry != null && entry.present();
            ItemStack stack = occupied ? entry.stack().copy() : ItemStack.EMPTY;
            return new OffhandSlot(occupied, stack, occupied ? entry.count() : 0);
        }

        private CompoundTag toTag(HolderLookup.Provider provider) {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("occupied", occupied);
            tag.put("displayStack", displayStack.saveOptional(provider));
            tag.putInt("count", count);
            return tag;
        }

        private static OffhandSlot fromTag(HolderLookup.Provider provider, CompoundTag tag) {
            return new OffhandSlot(
                    tag.getBoolean("occupied"),
                    ItemStack.parseOptional(provider, tag.getCompound("displayStack")),
                    tag.getInt("count")
            );
        }
    }

    private static VisualAtlasIslandKind decodeIslandKind(String raw) {
        try {
            return raw == null || raw.isBlank()
                    ? VisualAtlasIslandKind.PLAYER
                    : VisualAtlasIslandKind.valueOf(raw);
        } catch (IllegalArgumentException ignored) {
            return VisualAtlasIslandKind.PLAYER;
        }
    }

    private static final class MainAccumulator {
        private final ItemIdentity identity;
        private final ItemStack displayStack;
        private final String name;
        private int totalCount;
        private int firstSlotIndex;

        private MainAccumulator(ItemIdentity identity, InventoryEntrySnapshot firstEntry) {
            this.identity = identity;
            this.displayStack = firstEntry.stack().copy();
            this.name = firstEntry.stack().getHoverName().getString();
            this.firstSlotIndex = firstEntry.slotIndex();
        }

        private void add(InventoryEntrySnapshot entry) {
            totalCount += entry.count();
            if (entry.slotIndex() < firstSlotIndex) {
                firstSlotIndex = entry.slotIndex();
            }
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
            return firstSlotIndex;
        }
    }
}
