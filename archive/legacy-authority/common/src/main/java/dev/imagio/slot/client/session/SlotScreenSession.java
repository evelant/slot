package dev.imagio.slot.client.session;

import dev.imagio.slot.client.screen.InventoryScreenContext;
import dev.imagio.slot.session.InventoryHostDescriptor;
import dev.imagio.slot.session.SlotSessionDescriptor;

public record SlotScreenSession(
        SlotSessionKind kind,
        String screenClassName,
        InventoryHostDescriptor host
) {
    public SlotScreenSession {
        if (kind == null) {
            throw new IllegalArgumentException("kind must not be null");
        }
        screenClassName = screenClassName == null ? "" : screenClassName;
    }

    public boolean recordsRecentLoot() {
        return kind.recordsRecentLoot();
    }

    public boolean slotOwned() {
        return kind == SlotSessionKind.SLOT_CARRIED || kind == SlotSessionKind.SLOT_WORKSPACE;
    }

    public boolean carriedOnlyMode() {
        return kind == SlotSessionKind.PLAYER_INVENTORY
                || kind == SlotSessionKind.SLOT_CARRIED
                || kind == SlotSessionKind.CARRIED_CONTAINER;
    }

    public boolean hasStorageView() {
        return host != null;
    }

    public InventoryScreenContext inventoryContextOrNull() {
        return InventoryScreenContext.fromHost(host);
    }

    public SlotSessionDescriptor descriptor() {
        return LegacySlotSessionDescriptors.describe(this);
    }
}
