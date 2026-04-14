package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.ItemIdentity;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorkflowDomainModelTest {
    @Test
    void loadoutsUseTypedTargetsForQuickAccessAndEquipmentBindings() {
        QuickAccessLoadoutDefinition loadout = new QuickAccessLoadoutDefinition(
                "combat",
                "Combat",
                Set.of(
                        new QuickAccessLoadoutEntry(
                                new LoadoutTarget.QuickAccessLaneTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, 2),
                                ItemIdentity.of("minecraft:bow")
                        ),
                        new QuickAccessLoadoutEntry(
                                new LoadoutTarget.EquipmentSlotTarget(BuiltinInventoryIds.EQUIPMENT_GROUP_OFFHAND, 0),
                                ItemIdentity.of("minecraft:shield")
                        )
                )
        );

        assertEquals(2, loadout.entries().size());
        assertEquals(
                Set.of(
                        "quick_access:" + BuiltinInventoryIds.QUICK_ACCESS_LANE_0 + "#2",
                        "equipment:" + BuiltinInventoryIds.EQUIPMENT_GROUP_OFFHAND + "#0"
                ),
                loadout.entries().stream().map(entry -> entry.target().stableKey()).collect(java.util.stream.Collectors.toSet())
        );
    }
}
