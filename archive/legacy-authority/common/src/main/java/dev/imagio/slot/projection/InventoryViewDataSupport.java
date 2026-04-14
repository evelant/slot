package dev.imagio.slot.projection;

import dev.imagio.slot.client.category.InventoryGroupingOverrides;
import dev.imagio.slot.client.category.SlotCategory;
import dev.imagio.slot.client.collection.CollectionDefinition;
import dev.imagio.slot.client.collection.CollectionStore;
import dev.imagio.slot.client.model.ItemEntry;
import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.client.policy.ItemBehaviorPolicy;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public final class InventoryViewDataSupport {
    private InventoryViewDataSupport() {
    }

    public static Set<String> resolveCollections(CollectionStore collectionStore, ItemEntry entry, ItemStack displayStack) {
        if (entry == null) {
            return Set.of();
        }

        Set<String> resolvedCollections = new LinkedHashSet<>(entry.collectionIds());
        if (collectionStore != null && shouldImplicitlyTagAsJunk(entry, displayStack)) {
            resolvedCollections.add(CollectionStore.JUNK_ID);
        }
        return Set.copyOf(resolvedCollections);
    }

    public static List<InventoryViewData.Section> buildSections(
            CollectionStore collectionStore,
            List<ItemEntry> entries,
            Predicate<String> carriedSourceFilter
    ) {
        List<InventoryViewData.Section> sections = new ArrayList<>();
        int order = 0;
        sections.add(InventoryViewData.Section.recent(Component.translatable("slot.screen.recent.title").getString(), order++));
        for (CollectionDefinition collection : collectionStore.allCollections()) {
            sections.add(InventoryViewData.Section.collection(collection.id(), collection.name(), order++));
        }

        for (SlotCategory category : SlotCategory.values()) {
            if (category == SlotCategory.MISC) {
                continue;
            }
            sections.add(InventoryViewData.Section.category(category, order++));
        }

        Map<String, String> fallbackGroups = new LinkedHashMap<>();
        if (entries != null) {
            entries.stream()
                    .filter(entry -> entry.category() == SlotCategory.MISC)
                    .filter(entry -> entry.fallbackGroupId() != null && entry.fallbackGroupLabel() != null)
                    .sorted(Comparator.comparing(ItemEntry::fallbackGroupLabel, String.CASE_INSENSITIVE_ORDER))
                    .forEach(entry -> fallbackGroups.putIfAbsent(entry.fallbackGroupId(), entry.fallbackGroupLabel()));
        }
        for (Map.Entry<String, String> fallbackGroup : fallbackGroups.entrySet()) {
            sections.add(InventoryViewData.Section.modBucket(fallbackGroup.getKey(), fallbackGroup.getValue(), order++));
        }

        boolean hasVanillaMisc = entries != null && entries.stream()
                .anyMatch(entry -> entry.category() == SlotCategory.MISC && entry.fallbackGroupId() == null);
        if (hasVanillaMisc) {
            sections.add(InventoryViewData.Section.category(SlotCategory.MISC, order));
        }
        return List.copyOf(sections);
    }

    public static Map<String, String> buildCollectionNames(CollectionStore collectionStore) {
        Map<String, String> collectionNames = new LinkedHashMap<>();
        for (CollectionDefinition collection : collectionStore.allCollections()) {
            collectionNames.put(collection.id(), collection.name());
        }
        return Map.copyOf(collectionNames);
    }

    public static List<InventoryViewData.EntryView> buildEntryViews(
            List<ItemEntry> aggregated,
            Map<ItemIdentity, ItemStack> displayStacks
    ) {
        List<InventoryViewData.EntryView> entries = new ArrayList<>(aggregated.size());
        for (ItemEntry itemEntry : aggregated) {
            ItemStack displayStack = displayStacks.get(itemEntry.identity());
            if (displayStack == null) {
                continue;
            }

            String displayName = displayStack.getHoverName().getString();
            String searchKey = (displayName + " " + itemEntry.identity().itemId()).toLowerCase(Locale.ROOT);
            entries.add(new InventoryViewData.EntryView(itemEntry, displayStack, displayName, searchKey));
        }
        return List.copyOf(entries);
    }

    public static InventoryViewData.EntryView buildGhostCollectionEntryView(CollectionStore collectionStore, ItemIdentity identity) {
        if (collectionStore == null || identity == null) {
            return null;
        }

        ItemStack displayStack = ItemBehaviorPolicy.approximateDisplayStack(identity);
        if (displayStack.isEmpty()) {
            return null;
        }

        SlotCategory category = ItemBehaviorPolicy.resolveCategory(identity, displayStack);
        InventoryGroupingOverrides.GroupingBucket fallbackGrouping = ItemBehaviorPolicy.resolveFallbackGrouping(identity, category);
        Set<String> collectionIds = collectionStore.collectionsFor(identity);
        ItemEntry itemEntry = new ItemEntry(
                identity,
                0,
                Map.of(),
                List.of(),
                category == null ? SlotCategory.MISC : category,
                collectionStore.isFavorite(identity),
                collectionIds,
                fallbackGrouping == null ? null : fallbackGrouping.id(),
                fallbackGrouping == null ? null : fallbackGrouping.label()
        );
        String displayName = displayStack.getHoverName().getString();
        String searchKey = (displayName + " " + identity.itemId()).toLowerCase(Locale.ROOT);
        return new InventoryViewData.EntryView(itemEntry, displayStack, displayName, searchKey);
    }

    private static boolean shouldImplicitlyTagAsJunk(ItemEntry entry, ItemStack displayStack) {
        if (entry == null || displayStack == null || displayStack.isEmpty() || entry.totalCount() <= 0 || entry.totalCount() >= 5) {
            return false;
        }
        if (!entry.collectionIds().isEmpty() && entry.collectionIds().stream().anyMatch(collectionId -> !CollectionStore.JUNK_ID.equals(collectionId))) {
            return false;
        }
        return ItemBehaviorPolicy.compatibility(displayStack).implicitJunkCandidate();
    }
}
