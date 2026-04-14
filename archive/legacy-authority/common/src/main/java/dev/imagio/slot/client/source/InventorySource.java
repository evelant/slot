package dev.imagio.slot.client.source;

public interface InventorySource {
    String id();

    String displayName();

    SourceGroup group();

    int stableOrder();

    boolean primaryCarried();

    boolean canInsert();

    boolean canExtract();
}
