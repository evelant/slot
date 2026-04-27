package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.InMemoryWorkflowDomainStateRepository;
import dev.imagio.slot.workflow.domain.StorageArea;
import dev.imagio.slot.workflow.domain.StorageAreaMap;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlotWorkspaceStorageAreaProjectionTest {
    private static final String OVERWORLD = "minecraft:overworld";

    @Test
    void storageAreaSnapshotsGroupChestsByAreaInDisplayOrder() {
        WorkflowDomainRuntime runtime = newRuntime();
        StorageArea mountain = runtime.storageAreaWorkflow().createArea("Mountain Mine", 4000, 0);
        StorageArea derrick = runtime.storageAreaWorkflow().createArea("Oil Derrick", 4400, 0);

        runtime.chestClaimWorkflow().claim(
                anchorsOf(new ChestAnchor(OVERWORLD, 1, 64, 1)),
                2400, 0, "", StorageAreaMap.DEFAULT_AREA_ID
        );
        runtime.chestClaimWorkflow().claim(
                anchorsOf(new ChestAnchor(OVERWORLD, 100, 64, 1)),
                4000, 0, "", mountain.areaId()
        );
        runtime.chestClaimWorkflow().claim(
                anchorsOf(new ChestAnchor(OVERWORLD, 101, 64, 1)),
                4040, 0, "", mountain.areaId()
        );
        runtime.chestClaimWorkflow().claim(
                anchorsOf(new ChestAnchor(OVERWORLD, 200, 64, 1)),
                4400, 0, "", derrick.areaId()
        );

        SlotWorkspaceViewModel vm = SlotWorkspaceViewModel.project(
                InventoryAuthoritySnapshot.empty(),
                runtime.snapshot(),
                "ready",
                "",
                0,
                -1,
                1L
        );

        assertEquals(3, vm.storageAreas().size(), "Main Base + Mountain Mine + Oil Derrick");

        SlotWorkspaceViewModel.StorageAreaSnapshot defaultSnap = vm.storageArea(StorageAreaMap.DEFAULT_AREA_ID.toString());
        assertNotNull(defaultSnap);
        assertEquals(1, defaultSnap.chestCount());
        assertEquals(StorageAreaMap.DEFAULT_AREA_LABEL, defaultSnap.label());

        SlotWorkspaceViewModel.StorageAreaSnapshot mountainSnap = vm.storageArea(mountain.areaId().toString());
        assertNotNull(mountainSnap);
        assertEquals(2, mountainSnap.chestCount());
        for (SlotWorkspaceViewModel.ClaimedChestTile tile : mountainSnap.chestTiles()) {
            assertEquals(mountain.areaId().toString(), tile.areaId());
        }

        SlotWorkspaceViewModel.StorageAreaSnapshot derrickSnap = vm.storageArea(derrick.areaId().toString());
        assertNotNull(derrickSnap);
        assertEquals(1, derrickSnap.chestCount());

        // displayOrder is monotonically non-decreasing across the projected list.
        int previousOrder = -1;
        for (SlotWorkspaceViewModel.StorageAreaSnapshot snapshot : vm.storageAreas()) {
            assertTrue(snapshot.displayOrder() >= previousOrder,
                    "snapshots must be sorted by displayOrder");
            previousOrder = snapshot.displayOrder();
        }
    }

    @Test
    void emptyWorkspaceProducesNoStorageAreaSnapshots() {
        WorkflowDomainRuntime runtime = newRuntime();

        SlotWorkspaceViewModel vm = SlotWorkspaceViewModel.project(
                InventoryAuthoritySnapshot.empty(),
                runtime.snapshot(),
                "ready",
                "",
                0,
                -1,
                1L
        );

        assertEquals(0, vm.storageAreas().size(),
                "no claims means no Main Base materialised yet");
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
