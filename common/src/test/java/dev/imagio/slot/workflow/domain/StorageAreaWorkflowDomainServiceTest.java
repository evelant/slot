package dev.imagio.slot.workflow.domain;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StorageAreaWorkflowDomainServiceTest {
    private static final String OVERWORLD = "minecraft:overworld";

    @Test
    void createAreaAssignsFreshIdAndAppendsDisplayOrder() {
        WorkflowDomainRuntime runtime = newRuntime();

        StorageArea first = runtime.storageAreaWorkflow().createArea("Mountain Mine", 4000, 0);
        StorageArea second = runtime.storageAreaWorkflow().createArea("Oil Derrick", 4400, 0);

        assertNotNull(first);
        assertNotNull(second);
        assertEquals("Mountain Mine", first.label());
        assertEquals("Oil Derrick", second.label());
        assertTrue(first.displayOrder() < second.displayOrder(),
                "second area's displayOrder should follow the first");
        assertEquals(2, runtime.storageAreaWorkflow().storageAreaMap().areas().size());
    }

    @Test
    void createAreaRejectsBlankLabel() {
        WorkflowDomainRuntime runtime = newRuntime();

        assertNull(runtime.storageAreaWorkflow().createArea("", 0, 0));
        assertNull(runtime.storageAreaWorkflow().createArea("   ", 0, 0));
        assertEquals(0, runtime.storageAreaWorkflow().storageAreaMap().areas().size());
    }

    @Test
    void renameRecolorMoveAreaUpdateInPlace() {
        WorkflowDomainRuntime runtime = newRuntime();
        StorageArea area = runtime.storageAreaWorkflow().createArea("Mountain Mine", 4000, 0);

        StorageArea renamed = runtime.storageAreaWorkflow().renameArea(area.areaId(), "Iron Mine");
        assertEquals("Iron Mine", renamed.label());

        StorageArea recolored = runtime.storageAreaWorkflow().recolorArea(area.areaId(), 0xFFAA0000);
        assertEquals(0xFFAA0000, recolored.color());

        StorageArea moved = runtime.storageAreaWorkflow().moveArea(area.areaId(), 4800, 320);
        assertEquals(4800, moved.atlasX());
        assertEquals(320, moved.atlasY());
    }

    @Test
    void renameRejectsBlankAndUnknownArea() {
        WorkflowDomainRuntime runtime = newRuntime();
        StorageArea area = runtime.storageAreaWorkflow().createArea("Mountain Mine", 4000, 0);

        StorageArea blank = runtime.storageAreaWorkflow().renameArea(area.areaId(), "   ");
        assertEquals("Mountain Mine", blank.label(), "blank rename is a no-op");

        assertNull(runtime.storageAreaWorkflow().renameArea(UUID.randomUUID(), "Ghost"));
    }

    @Test
    void deleteAreaRefusesDefault() {
        WorkflowDomainRuntime runtime = newRuntime();
        // Materialise the default area by claiming a chest.
        runtime.chestClaimWorkflow().claim(
                anchorsOf(new ChestAnchor(OVERWORLD, 1, 64, 1)),
                2400, 0, "", StorageAreaMap.DEFAULT_AREA_ID
        );

        assertFalse(runtime.storageAreaWorkflow().deleteArea(StorageAreaMap.DEFAULT_AREA_ID));
        assertNotNull(runtime.storageAreaWorkflow().area(StorageAreaMap.DEFAULT_AREA_ID));
    }

    @Test
    void deleteAreaRefusesIfChestsStillReferenceIt() {
        WorkflowDomainRuntime runtime = newRuntime();
        StorageArea mountain = runtime.storageAreaWorkflow().createArea("Mountain Mine", 4000, 0);
        runtime.chestClaimWorkflow().claim(
                anchorsOf(new ChestAnchor(OVERWORLD, 1, 64, 1)),
                4000, 0, "", mountain.areaId()
        );

        assertFalse(runtime.storageAreaWorkflow().deleteArea(mountain.areaId()));
        assertNotNull(runtime.storageAreaWorkflow().area(mountain.areaId()));
    }

    @Test
    void deleteAreaRemovesEmptyArea() {
        WorkflowDomainRuntime runtime = newRuntime();
        StorageArea mountain = runtime.storageAreaWorkflow().createArea("Mountain Mine", 4000, 0);

        assertTrue(runtime.storageAreaWorkflow().deleteArea(mountain.areaId()));
        assertNull(runtime.storageAreaWorkflow().area(mountain.areaId()));
    }

    @Test
    void persistenceRoundTripsAreasAndChestAreaAssignments() {
        WorkflowDomainRuntime runtime = newRuntime();
        StorageArea mountain = runtime.storageAreaWorkflow().createArea("Mountain Mine", 4000, 0);
        StorageArea derrick = runtime.storageAreaWorkflow().createArea("Oil Derrick", 4400, 0);
        runtime.storageAreaWorkflow().recolorArea(derrick.areaId(), 0xFF112233);
        runtime.storageAreaWorkflow().moveArea(mountain.areaId(), 4800, 320);

        ClaimedChest defaultChest = runtime.chestClaimWorkflow().claim(
                anchorsOf(new ChestAnchor(OVERWORLD, 1, 64, 1)),
                2400, 0, "", StorageAreaMap.DEFAULT_AREA_ID
        );
        ClaimedChest mountainChest = runtime.chestClaimWorkflow().claim(
                anchorsOf(new ChestAnchor(OVERWORLD, 100, 64, 1)),
                4800, 320, "", mountain.areaId()
        );
        runtime.chestClaimWorkflow().moveChestToArea(defaultChest.storageId(), derrick.areaId());

        WorkflowProjection.Snapshot snapshot = runtime.workflowProjection();
        WorkflowEventStore.Snapshot events = runtime.snapshot().workflowEvents();
        WorkflowProjection.Snapshot replayed = WorkflowProjection.replay(events, WorkflowProjection.Snapshot.empty());

        assertEquals(snapshot.storageAreaMap().areas().size(), replayed.storageAreaMap().areas().size());
        assertEquals(0xFF112233, replayed.storageAreaMap().area(derrick.areaId()).color());
        assertEquals(4800, replayed.storageAreaMap().area(mountain.areaId()).atlasX());
        assertEquals(derrick.areaId(),
                replayed.claimedChestMap().chest(defaultChest.storageId()).areaId());
        assertEquals(mountain.areaId(),
                replayed.claimedChestMap().chest(mountainChest.storageId()).areaId());
    }

    private static WorkflowDomainRuntime newRuntime() {
        return new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
    }

    private static Set<ChestAnchor> anchorsOf(ChestAnchor... items) {
        LinkedHashSet<ChestAnchor> set = new LinkedHashSet<>();
        for (ChestAnchor anchor : items) {
            set.add(anchor);
        }
        return set;
    }
}
