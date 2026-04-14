package dev.imagio.slot.inventory.action;

import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.query.ProjectedEntryRef;
import dev.imagio.slot.inventory.query.ProjectedInventoryRow;

record ProjectedTransferSourceCandidate(
        ProjectedInventoryRow row,
        ProjectedEntryRef sourceEntry,
        InventorySourceDescriptor source,
        InventoryActionTarget sourceTarget,
        ItemIdentity identity,
        int availableCount
) {
    ProjectedTransferSourceCandidate {
        if (row == null) {
            throw new IllegalArgumentException("row must not be null");
        }
        if (sourceEntry == null) {
            throw new IllegalArgumentException("source entry must not be null");
        }
        if (source == null) {
            throw new IllegalArgumentException("source descriptor must not be null");
        }
        if (sourceTarget == null) {
            throw new IllegalArgumentException("source target must not be null");
        }
        availableCount = Math.max(0, availableCount);
    }
}
