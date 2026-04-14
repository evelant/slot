package dev.imagio.slot.operation;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CraftingCursorDistributionPlannerTest {
    @Test
    void stackModeSplitsEvenlyAcrossTargets() {
        CraftingCursorDistributionPlanner.Plan plan = CraftingCursorDistributionPlanner.plan(
                8,
                CraftingCursorDistributionPlanner.Mode.STACK,
                List.of(
                        new CraftingCursorDistributionPlanner.Target(0, 64),
                        new CraftingCursorDistributionPlanner.Target(0, 64),
                        new CraftingCursorDistributionPlanner.Target(0, 64)
                )
        );

        assertEquals(List.of(2, 2, 2), plan.allocations().stream().map(CraftingCursorDistributionPlanner.Allocation::placedCount).toList());
        assertEquals(2, plan.remainingCount());
        assertEquals(6, plan.placedCount());
    }

    @Test
    void oneModePlacesOnePerTarget() {
        CraftingCursorDistributionPlanner.Plan plan = CraftingCursorDistributionPlanner.plan(
                5,
                CraftingCursorDistributionPlanner.Mode.ONE,
                List.of(
                        new CraftingCursorDistributionPlanner.Target(0, 64),
                        new CraftingCursorDistributionPlanner.Target(4, 64),
                        new CraftingCursorDistributionPlanner.Target(0, 64)
                )
        );

        assertEquals(List.of(1, 1, 1), plan.allocations().stream().map(CraftingCursorDistributionPlanner.Allocation::placedCount).toList());
        assertEquals(List.of(1, 5, 1), plan.allocations().stream().map(CraftingCursorDistributionPlanner.Allocation::resultingCount).toList());
        assertEquals(2, plan.remainingCount());
    }

    @Test
    void stackModeDoesNotRedistributeLeftoverFromCappedTargets() {
        CraftingCursorDistributionPlanner.Plan plan = CraftingCursorDistributionPlanner.plan(
                5,
                CraftingCursorDistributionPlanner.Mode.STACK,
                List.of(
                        new CraftingCursorDistributionPlanner.Target(63, 64),
                        new CraftingCursorDistributionPlanner.Target(0, 64)
                )
        );

        assertEquals(List.of(1, 2), plan.allocations().stream().map(CraftingCursorDistributionPlanner.Allocation::placedCount).toList());
        assertEquals(List.of(64, 2), plan.allocations().stream().map(CraftingCursorDistributionPlanner.Allocation::resultingCount).toList());
        assertEquals(2, plan.remainingCount());
    }
}
