package dev.imagio.slot.projection;

import dev.imagio.slot.client.category.SlotCategory;
import dev.imagio.slot.client.model.ItemEntry;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public record InventoryViewData(
        List<EntryView> entries,
        List<Section> sections,
        Map<String, SourceInfo> sources,
        Map<String, String> collectionNames
) {
    public record EntryView(
            ItemEntry itemEntry,
            ItemStack displayStack,
            String displayName,
            String searchKey
    ) {
        public EntryView {
            Objects.requireNonNull(itemEntry, "itemEntry");
            Objects.requireNonNull(displayStack, "displayStack");
            displayStack = displayStack.copy();
            displayStack.setCount(1);
            displayName = Objects.requireNonNull(displayName, "displayName");
            searchKey = searchKey == null ? "" : searchKey;
        }
    }

    public record Section(
            String id,
            String label,
            SlotCategory category,
            String collectionId,
            String fallbackGroupId,
            boolean recent,
            int order
    ) {
        public Section {
            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException("id must not be blank");
            }
            if (label == null || label.isBlank()) {
                throw new IllegalArgumentException("label must not be blank");
            }
            int targetCount = 0;
            if (category != null) {
                targetCount++;
            }
            if (collectionId != null) {
                targetCount++;
            }
            if (fallbackGroupId != null) {
                targetCount++;
            }
            if (recent) {
                targetCount++;
            }
            if (targetCount != 1) {
                throw new IllegalArgumentException("Section must target exactly one of category, collection, fallback group, or recent");
            }
        }

        public static Section collection(String collectionId, String label, int order) {
            return new Section("collection/" + collectionId, label, null, collectionId, null, false, order);
        }

        public static Section category(SlotCategory category, int order) {
            return new Section(
                    "category/" + category.name().toLowerCase(Locale.ROOT),
                    category.displayName(),
                    category,
                    null,
                    null,
                    false,
                    order
            );
        }

        public static Section modBucket(String groupId, String label, int order) {
            return new Section(
                    "mod/" + groupId.toLowerCase(Locale.ROOT),
                    label,
                    null,
                    null,
                    groupId.toLowerCase(Locale.ROOT),
                    false,
                    order
            );
        }

        public static Section recent(String label, int order) {
            return new Section(
                    "recent",
                    label,
                    null,
                    null,
                    null,
                    true,
                    order
            );
        }

        public boolean isCollection() {
            return collectionId != null;
        }

        public boolean isModBucket() {
            return fallbackGroupId != null;
        }

        public boolean isRecent() {
            return recent;
        }

        public boolean matches(ItemEntry entry) {
            if (isRecent()) {
                return false;
            }
            if (isCollection()) {
                return entry.collectionIds().contains(collectionId);
            }
            if (isModBucket()) {
                return entry.category() == SlotCategory.MISC
                        && fallbackGroupId.equals(entry.fallbackGroupId());
            }
            if (category == SlotCategory.MISC) {
                return entry.category() == SlotCategory.MISC
                        && entry.fallbackGroupId() == null;
            }
            return entry.category() == category;
        }
    }

    public record SourceInfo(String id, String label, int stableOrder) {
        public SourceInfo {
            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException("id must not be blank");
            }
            if (label == null || label.isBlank()) {
                throw new IllegalArgumentException("label must not be blank");
            }
        }
    }
}
