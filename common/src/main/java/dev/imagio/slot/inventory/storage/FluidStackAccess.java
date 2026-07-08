package dev.imagio.slot.inventory.storage;

import dev.imagio.slot.inventory.core.SlotResourceIdentity;

/**
 * Loader-neutral read model for one concrete fluid stack observed through a
 * platform fluid API. It deliberately carries no fill/drain methods; v1 fluid
 * support is discovery and accounting only.
 */
public interface FluidStackAccess {
    SlotResourceIdentity identity();

    long amount();

    String label();

    default boolean present() {
        return identity() != null && identity().fluid() && amount() > 0L;
    }
}
