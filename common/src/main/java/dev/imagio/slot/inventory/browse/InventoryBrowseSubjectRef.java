package dev.imagio.slot.inventory.browse;

import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import dev.imagio.slot.inventory.core.ItemComparisonMode;
import dev.imagio.slot.inventory.core.ItemIdentity;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;

public sealed interface InventoryBrowseSubjectRef permits
        InventoryBrowseSubjectRef.PaneRef,
        InventoryBrowseSubjectRef.SectionRef,
        InventoryBrowseSubjectRef.ItemRowRef,
        InventoryBrowseSubjectRef.PlaceholderRef,
        InventoryBrowseSubjectRef.LoadoutRef {

    String stableKey();

    static InventoryBrowseSubjectRef parse(String stableKey) {
        if (stableKey == null || stableKey.isBlank()) {
            return null;
        }
        String[] parts = stableKey.split("\\|", -1);
        if (parts.length == 0) {
            return null;
        }
        return switch (parts[0]) {
            case "pane" -> parts.length == 2 ? new PaneRef(parsePane(parts[1])) : null;
            case "section" -> parts.length == 3 ? new SectionRef(parsePane(parts[1]), decode(parts[2])) : null;
            case "item" -> parts.length == 5
                    ? new ItemRowRef(parsePane(parts[1]), identity(parts[2], parts[3], parts[4]))
                    : null;
            case "placeholder" -> parts.length == 5
                    ? new PlaceholderRef(decode(parts[1]), identity(parts[2], parts[3], parts[4]))
                    : null;
            case "loadout" -> parts.length == 3 ? new LoadoutRef(decode(parts[1]), decode(parts[2])) : null;
            default -> null;
        };
    }

    private static InventoryPaneMembership parsePane(String value) {
        if (value == null || value.isBlank()) {
            return InventoryPaneMembership.CARRIED;
        }
        try {
            return InventoryPaneMembership.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return InventoryPaneMembership.CARRIED;
        }
    }

    private static ItemIdentity identity(
            String encodedItemId,
            String comparisonMode,
            String encodedFingerprint
    ) {
        String itemId = decode(encodedItemId);
        if (itemId.isBlank()) {
            return null;
        }
        ItemComparisonMode mode;
        try {
            mode = comparisonMode == null || comparisonMode.isBlank()
                    ? ItemComparisonMode.ITEM_ID
                    : ItemComparisonMode.valueOf(comparisonMode);
        } catch (IllegalArgumentException ignored) {
            mode = ItemComparisonMode.ITEM_ID;
        }
        return new ItemIdentity(itemId, mode, decode(encodedFingerprint));
    }

    private static String encode(String value) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }

    record PaneRef(InventoryPaneMembership paneMembership) implements InventoryBrowseSubjectRef {
        public PaneRef {
            paneMembership = paneMembership == null ? InventoryPaneMembership.CARRIED : paneMembership;
        }

        @Override
        public String stableKey() {
            return "pane|" + paneMembership.name().toLowerCase(Locale.ROOT);
        }
    }

    record SectionRef(
            InventoryPaneMembership paneMembership,
            String sectionId
    ) implements InventoryBrowseSubjectRef {
        public SectionRef {
            paneMembership = paneMembership == null ? InventoryPaneMembership.CARRIED : paneMembership;
            sectionId = sectionId == null ? "" : sectionId;
        }

        @Override
        public String stableKey() {
            return "section|"
                    + paneMembership.name().toLowerCase(Locale.ROOT)
                    + "|"
                    + encode(sectionId);
        }
    }

    record ItemRowRef(
            InventoryPaneMembership paneMembership,
            ItemIdentity identity
    ) implements InventoryBrowseSubjectRef {
        public ItemRowRef {
            paneMembership = paneMembership == null ? InventoryPaneMembership.CARRIED : paneMembership;
        }

        @Override
        public String stableKey() {
            if (identity == null) {
                return "item|" + paneMembership.name().toLowerCase(Locale.ROOT) + "|||";
            }
            return "item|"
                    + paneMembership.name().toLowerCase(Locale.ROOT)
                    + "|"
                    + encode(identity.itemId())
                    + "|"
                    + identity.comparisonMode().name()
                    + "|"
                    + encode(identity.componentFingerprint());
        }
    }

    record PlaceholderRef(
            String collectionId,
            ItemIdentity identity
    ) implements InventoryBrowseSubjectRef {
        public PlaceholderRef {
            collectionId = collectionId == null ? "" : collectionId;
        }

        @Override
        public String stableKey() {
            if (identity == null) {
                return "placeholder|" + encode(collectionId) + "|||";
            }
            return "placeholder|"
                    + encode(collectionId)
                    + "|"
                    + encode(identity.itemId())
                    + "|"
                    + identity.comparisonMode().name()
                    + "|"
                    + encode(identity.componentFingerprint());
        }
    }

    record LoadoutRef(
            String collectionId,
            String loadoutId
    ) implements InventoryBrowseSubjectRef {
        public LoadoutRef {
            collectionId = collectionId == null ? "" : collectionId;
            loadoutId = loadoutId == null ? "" : loadoutId;
        }

        @Override
        public String stableKey() {
            return "loadout|" + encode(collectionId) + "|" + encode(loadoutId);
        }
    }
}
