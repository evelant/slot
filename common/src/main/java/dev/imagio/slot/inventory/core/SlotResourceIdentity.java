package dev.imagio.slot.inventory.core;

import java.util.Locale;

public record SlotResourceIdentity(
        SlotResourceKind kind,
        String id,
        String fingerprint
) {
    private static final String FLUID_SYNTHETIC_PREFIX = "slot:fluid/";

    public SlotResourceIdentity {
        kind = kind == null ? SlotResourceKind.ITEM : kind;
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("resource id must not be blank");
        }
        id = id.trim();
        fingerprint = fingerprint == null ? "" : fingerprint.trim();
    }

    public static SlotResourceIdentity item(ItemIdentity identity) {
        ItemIdentity key = ItemIdentityCollections.key(identity);
        if (key == null) {
            return null;
        }
        return new SlotResourceIdentity(
                SlotResourceKind.ITEM,
                key.itemId(),
                key.comparisonMode() == ItemComparisonMode.ITEM_ID ? "" : key.componentFingerprint());
    }

    public static SlotResourceIdentity item(String itemId) {
        return item(ItemIdentity.of(itemId));
    }

    public static SlotResourceIdentity fluid(String fluidId) {
        return fluid(fluidId, "");
    }

    public static SlotResourceIdentity fluid(String fluidId, String fingerprint) {
        return new SlotResourceIdentity(SlotResourceKind.FLUID, fluidId, fingerprint);
    }

    public boolean item() {
        return kind == SlotResourceKind.ITEM;
    }

    public boolean fluid() {
        return kind == SlotResourceKind.FLUID;
    }

    public ItemIdentity toItemIdentity() {
        if (!item()) {
            return null;
        }
        return fingerprint.isBlank()
                ? ItemIdentity.of(id)
                : ItemIdentity.exact(id, fingerprint);
    }

    public String stableKey() {
        return kind.name().toLowerCase(Locale.ROOT) + "|" + id + "|" + fingerprint;
    }

    public String syntheticItemId() {
        if (item()) {
            return id;
        }
        String normalized = id.toLowerCase(Locale.ROOT).replace(':', '/');
        if (!fingerprint.isBlank()) {
            normalized = normalized + "/" + Integer.toHexString(fingerprint.hashCode());
        }
        return FLUID_SYNTHETIC_PREFIX + normalized;
    }

    public static boolean syntheticFluidItemId(String itemId) {
        return itemId != null && itemId.startsWith(FLUID_SYNTHETIC_PREFIX);
    }
}
