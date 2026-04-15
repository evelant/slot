package dev.imagio.slot.inventory.integration;

import java.util.LinkedHashMap;
import java.util.Map;

public record InventoryHostObservationHints(
        InventoryHostFamilyHint hostFamilyHint,
        InventorySlotOwnershipPosture slotOwnershipPosture,
        boolean carriedOnly,
        boolean recordsRecent,
        Map<String, String> shapeSignals
) {
    public InventoryHostObservationHints {
        hostFamilyHint = hostFamilyHint == null ? InventoryHostFamilyHint.UNKNOWN : hostFamilyHint;
        slotOwnershipPosture = slotOwnershipPosture == null ? InventorySlotOwnershipPosture.UNKNOWN : slotOwnershipPosture;
        shapeSignals = shapeSignals == null ? Map.of() : Map.copyOf(new LinkedHashMap<>(shapeSignals));
    }

    public static InventoryHostObservationHints defaults() {
        return new InventoryHostObservationHints(
                InventoryHostFamilyHint.UNKNOWN,
                InventorySlotOwnershipPosture.UNKNOWN,
                false,
                true,
                Map.of()
        );
    }

    public boolean slotOwned() {
        return slotOwnershipPosture.slotOwned();
    }
}
