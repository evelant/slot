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
        // An exact-mode identity with a blank fingerprint has nothing to
        // distinguish — it is functionally an item-id identity. Collapse so
        // that exact(id, "") and of(id) hash + equal the same. Without this,
        // non-stackables with no component data (e.g. vanilla water_bucket)
        // ended up keyed two ways: ItemIdentityMatcher.create() built
        // exact-form from a stack, while ItemIdentity.of() callsites (kit
        // bring lists, populate command, persistence decode) built id-form,
        // splitting the atlas card and breaking matchesMovable.
        if (comparisonMode == ItemComparisonMode.ITEM_ID_AND_COMPONENTS && componentFingerprint.isBlank()) {
            comparisonMode = ItemComparisonMode.ITEM_ID;
        }
    }

    public static ItemIdentity of(String itemId) {
        return new ItemIdentity(itemId, ItemComparisonMode.ITEM_ID, "");
    }

    public static ItemIdentity exact(String itemId, String componentFingerprint) {
        return new ItemIdentity(itemId, ItemComparisonMode.ITEM_ID_AND_COMPONENTS, componentFingerprint);
    }
}
