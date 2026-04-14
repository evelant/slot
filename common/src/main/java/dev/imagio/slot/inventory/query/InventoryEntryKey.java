package dev.imagio.slot.inventory.query;

public record InventoryEntryKey(
        Kind kind,
        String sourceId,
        int slotIndex,
        String entryId
) {
    public InventoryEntryKey {
        kind = kind == null ? Kind.SLOT : kind;
        sourceId = sourceId == null ? "" : sourceId;
        slotIndex = kind == Kind.SLOT ? Math.max(0, slotIndex) : -1;
        entryId = kind == Kind.PROVIDER_ENTRY ? (entryId == null ? "" : entryId) : "";
        if (sourceId.isBlank()) {
            throw new IllegalArgumentException("entry source id must not be blank");
        }
        if (kind == Kind.PROVIDER_ENTRY && entryId.isBlank()) {
            throw new IllegalArgumentException("provider entry id must not be blank");
        }
    }

    public static InventoryEntryKey slot(String sourceId, int slotIndex) {
        return new InventoryEntryKey(Kind.SLOT, sourceId, slotIndex, "");
    }

    public static InventoryEntryKey providerEntry(String sourceId, String entryId) {
        return new InventoryEntryKey(Kind.PROVIDER_ENTRY, sourceId, -1, entryId);
    }

    public boolean slotBacked() {
        return kind == Kind.SLOT;
    }

    public boolean providerEntry() {
        return kind == Kind.PROVIDER_ENTRY;
    }

    public String stableKey() {
        return switch (kind) {
            case SLOT -> "slot:" + sourceId + "#" + slotIndex;
            case PROVIDER_ENTRY -> "entry:" + sourceId + "@" + entryId;
        };
    }

    public enum Kind {
        SLOT,
        PROVIDER_ENTRY
    }
}
