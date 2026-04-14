package dev.imagio.slot.client.model;

public record ItemIdentity(
        String itemId,
        ComparisonMode comparisonMode,
        String componentFingerprint
) {
    public ItemIdentity {
        if (itemId == null || itemId.isBlank()) {
            throw new IllegalArgumentException("itemId must not be blank");
        }
        itemId = itemId.trim();
        componentFingerprint = componentFingerprint == null ? "" : componentFingerprint;
    }

    public static ItemIdentity of(String itemId) {
        return new ItemIdentity(itemId, ComparisonMode.ITEM_ID, "");
    }

    public static ItemIdentity exact(String itemId, String componentFingerprint) {
        return new ItemIdentity(itemId, ComparisonMode.ITEM_ID_AND_COMPONENTS, componentFingerprint);
    }

    public String namespace() {
        int separatorIndex = itemId.indexOf(':');
        return separatorIndex >= 0 ? itemId.substring(0, separatorIndex) : "minecraft";
    }
}
