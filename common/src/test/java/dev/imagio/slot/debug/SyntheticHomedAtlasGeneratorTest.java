package dev.imagio.slot.debug;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.workflow.domain.VisualAtlasIsland;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import dev.imagio.slot.workflow.domain.VisualHomeAssignment;
import dev.imagio.slot.workflow.domain.VisualHomeOrigin;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SyntheticHomedAtlasGeneratorTest {
    @Test
    void generatesRequestedNumberOfIslandsAndAssignments() {
        List<ItemIdentity> pool = pool(200);

        SyntheticHomedAtlasPlan plan = SyntheticHomedAtlasGenerator.generate(
                pool,
                100,
                5,
                42L,
                SyntheticHomedAtlasGenerator.Config.defaults()
        );

        assertEquals(5, plan.islands().size());
        assertEquals(100, plan.assignments().size());
    }

    @Test
    void sameSeedProducesSamePlan() {
        List<ItemIdentity> pool = pool(200);

        SyntheticHomedAtlasPlan first = SyntheticHomedAtlasGenerator.generate(
                pool, 40, 4, 99L, SyntheticHomedAtlasGenerator.Config.defaults());
        SyntheticHomedAtlasPlan second = SyntheticHomedAtlasGenerator.generate(
                pool, 40, 4, 99L, SyntheticHomedAtlasGenerator.Config.defaults());

        assertEquals(first.islands(), second.islands());
        assertEquals(first.assignments(), second.assignments());
    }

    @Test
    void everyAssignmentReferencesAValidIsland() {
        List<ItemIdentity> pool = pool(60);

        SyntheticHomedAtlasPlan plan = SyntheticHomedAtlasGenerator.generate(
                pool, 60, 6, 1L, SyntheticHomedAtlasGenerator.Config.defaults());

        HashSet<String> islandIds = new HashSet<>();
        for (VisualAtlasIsland island : plan.islands()) {
            islandIds.add(island.id());
            assertEquals(VisualAtlasIslandKind.PLAYER, island.kind());
            assertTrue(island.id().startsWith(SyntheticHomedAtlasGenerator.SYNTHETIC_ISLAND_ID_PREFIX));
        }
        for (VisualHomeAssignment assignment : plan.assignments().values()) {
            assertTrue(islandIds.contains(assignment.islandId()),
                    "assignment references unknown island: " + assignment.islandId());
            assertEquals(VisualHomeOrigin.PLAYER_PLACED, assignment.origin());
        }
    }

    @Test
    void assignmentsAreDistributedAcrossAllIslands() {
        List<ItemIdentity> pool = pool(40);

        SyntheticHomedAtlasPlan plan = SyntheticHomedAtlasGenerator.generate(
                pool, 40, 4, 1L, SyntheticHomedAtlasGenerator.Config.defaults());

        HashSet<String> assignedIslands = new HashSet<>();
        for (VisualHomeAssignment assignment : plan.assignments().values()) {
            assignedIslands.add(assignment.islandId());
        }
        assertEquals(plan.islands().size(), assignedIslands.size());
    }

    @Test
    void emptyPoolProducesEmptyPlan() {
        SyntheticHomedAtlasPlan plan = SyntheticHomedAtlasGenerator.generate(
                List.of(), 10, 3, 1L, SyntheticHomedAtlasGenerator.Config.defaults());

        assertTrue(plan.islands().isEmpty());
        assertTrue(plan.assignments().isEmpty());
    }

    @Test
    void assignmentLocalCoordinatesFitInsideIslandBounds() {
        List<ItemIdentity> pool = pool(60);

        SyntheticHomedAtlasPlan plan = SyntheticHomedAtlasGenerator.generate(
                pool, 60, 3, 1L, SyntheticHomedAtlasGenerator.Config.defaults());

        for (VisualHomeAssignment assignment : plan.assignments().values()) {
            VisualAtlasIsland island = plan.islands().stream()
                    .filter(candidate -> candidate.id().equals(assignment.islandId()))
                    .findFirst()
                    .orElse(null);
            assertNotNull(island);
            assertTrue(assignment.localX() >= 0);
            assertTrue(assignment.localY() >= 0);
            assertTrue(assignment.localX() < island.width());
            assertTrue(assignment.localY() < island.height());
        }
    }

    private static List<ItemIdentity> pool(int size) {
        ArrayList<ItemIdentity> pool = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            pool.add(ItemIdentity.of("test:item_" + index));
        }
        return pool;
    }
}
