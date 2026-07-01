package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.ItemIdentity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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

    @Test
    void visualHomeAssignmentsUseMovableIdentitySemantics() {
        ItemIdentity hammer = ItemIdentity.of("gtceu:steel_mining_hammer");
        ItemIdentity damagedHammer = ItemIdentity.exact("gtceu:steel_mining_hammer", "{Damage:512}");
        ItemIdentity toolStateHammer = ItemIdentity.exact(
                "gtceu:steel_mining_hammer",
                "{Damage:12,\"GT.Tool\":{MaxDamage:960}}");
        VisualHomeMap map = new VisualHomeMap(
                List.of(new VisualAtlasIsland("tools", "Tools", VisualAtlasIslandKind.PLAYER, 0, 0, 0xFFFFFF, null)),
                Map.of(damagedHammer, new VisualHomeAssignment(
                        damagedHammer,
                        "tools",
                        3,
                        VisualHomeOrigin.PLAYER_PLACED,
                        false)));

        assertEquals(hammer, map.assignments().keySet().iterator().next());
        assertEquals("tools", map.assignment(toolStateHammer).islandId());
    }

    @Test
    void broadVisualHomeAssignmentsApplyToSpecificFluidVariantsOnlyInThatDirection() {
        ItemIdentity flask = ItemIdentity.of("waterflasks:iron_flask");
        ItemIdentity waterFlask = ItemIdentity.exact("waterflasks:iron_flask", "fluid=minecraft:water");
        ItemIdentity lavaFlask = ItemIdentity.exact("waterflasks:iron_flask", "fluid=minecraft:lava");
        VisualAtlasIsland hydration = new VisualAtlasIsland(
                "hydration",
                "Hydration",
                VisualAtlasIslandKind.PLAYER,
                0,
                0,
                0xFFFFFF,
                null);
        VisualHomeMap broadMap = new VisualHomeMap(
                List.of(hydration),
                Map.of(flask, new VisualHomeAssignment(
                        flask,
                        hydration.id(),
                        0,
                        VisualHomeOrigin.PLAYER_PLACED,
                        false)));
        VisualHomeMap waterMap = new VisualHomeMap(
                List.of(hydration),
                Map.of(waterFlask, new VisualHomeAssignment(
                        waterFlask,
                        hydration.id(),
                        0,
                        VisualHomeOrigin.PLAYER_PLACED,
                        false)));

        assertEquals(hydration.id(), broadMap.assignment(waterFlask).islandId());
        assertNull(waterMap.assignment(flask));
        assertNull(waterMap.assignment(lavaFlask));
    }
}
