package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;

public record QuickAccessLoadoutEntry(
        LoadoutTarget target,
        ItemIdentity identity
) {
    public QuickAccessLoadoutEntry {
        target = target == null ? new LoadoutTarget.QuickAccessLaneTarget("", 0) : target;
    }
}
