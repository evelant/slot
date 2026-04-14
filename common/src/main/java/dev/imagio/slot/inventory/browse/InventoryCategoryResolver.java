package dev.imagio.slot.inventory.browse;

import dev.imagio.slot.inventory.core.ItemIdentity;

public interface InventoryCategoryResolver {
    ItemCategory resolve(ItemIdentity identity);
}
