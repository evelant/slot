package dev.imagio.slot.client.source;

public record BasicInventorySource(
        String id,
        String displayName,
        SourceGroup group,
        int stableOrder,
        boolean primaryCarried,
        boolean canInsert,
        boolean canExtract
) implements InventorySource {
}
