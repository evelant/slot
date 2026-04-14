package dev.imagio.slot.inventory.core;

public record ItemIdentity(
        String itemId,
        ItemComparisonMode comparisonMode,
        String componentFingerprint
) {
    public ItemIdentity {
        if (itemId == null || itemId.isBlank()) {
            throw new IllegalArgumentException("itemId must not be blank");
        }
        itemId = itemId.trim();
        comparisonMode = comparisonMode == null ? ItemComparisonMode.ITEM_ID : comparisonMode;
        componentFingerprint = componentFingerprint == null ? "" : componentFingerprint;
    }

    public static ItemIdentity of(String itemId) {
        return new ItemIdentity(itemId, ItemComparisonMode.ITEM_ID, "");
    }

    public static ItemIdentity exact(String itemId, String componentFingerprint) {
        return new ItemIdentity(itemId, ItemComparisonMode.ITEM_ID_AND_COMPONENTS, componentFingerprint);
    }
}
