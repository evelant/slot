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

class ChestClaimWorkflowDomainServiceTest {
    private static final String OVERWORLD = "minecraft:overworld";

    @Test
    void claimProducesStorageWithAllAnchorsAndRetrievableByAnchor() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        ChestAnchor left = new ChestAnchor(OVERWORLD, 10, 64, 5);
        ChestAnchor right = new ChestAnchor(OVERWORLD, 11, 64, 5);

        ClaimedChest chest = runtime.chestClaimWorkflow().claim(
                anchors(left, right), 2400, 0, ""
        );

        assertNotNull(chest);
        assertTrue(chest.anchors().contains(left));
        assertTrue(chest.anchors().contains(right));
        assertEquals(chest.storageId(), runtime.chestClaimWorkflow().chestByAnchor(left).storageId());
        assertEquals(chest.storageId(), runtime.chestClaimWorkflow().chestByAnchor(right).storageId());
    }

    @Test
    void claimWithExistingAnchorIsRejected() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        ChestAnchor anchor = new ChestAnchor(OVERWORLD, 10, 64, 5);
        runtime.chestClaimWorkflow().claim(anchors(anchor), 0, 0, "");

        ClaimedChest second = runtime.chestClaimWorkflow().claim(anchors(anchor), 2000, 0, "");

        assertNull(second, "claiming an already-claimed anchor must fail");
    }

    @Test
    void claimWithEmptyAnchorsIsRejected() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        assertNull(runtime.chestClaimWorkflow().claim(Set.of(), 0, 0, ""));
    }

    @Test
    void moveChestUpdatesAtlasPosition() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        ChestAnchor anchor = new ChestAnchor(OVERWORLD, 10, 64, 5);
        ClaimedChest chest = runtime.chestClaimWorkflow().claim(anchors(anchor), 2400, 0, "");

        ClaimedChest moved = runtime.chestClaimWorkflow().moveChest(chest.storageId(), 2560, 160);

        assertEquals(2560, moved.atlasX());
        assertEquals(160, moved.atlasY());
        assertEquals(chest.storageId(), moved.storageId());
    }

    @Test
    void updateAnchorsAddsSecondHalfOfDoubleChest() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        ChestAnchor left = new ChestAnchor(OVERWORLD, 10, 64, 5);
        ChestAnchor right = new ChestAnchor(OVERWORLD, 11, 64, 5);
        ClaimedChest chest = runtime.chestClaimWorkflow().claim(anchors(left), 2400, 0, "");

        ClaimedChest updated = runtime.chestClaimWorkflow().updateAnchors(
                chest.storageId(), anchors(left, right)
        );

        assertEquals(2, updated.anchors().size());
        assertEquals(chest.storageId(), runtime.chestClaimWorkflow().chestByAnchor(right).storageId());
    }

    @Test
    void updateAnchorsRejectsAnchorOwnedByDifferentClaim() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        ChestAnchor anchor1 = new ChestAnchor(OVERWORLD, 10, 64, 5);
        ChestAnchor anchor2 = new ChestAnchor(OVERWORLD, 100, 64, 5);
        ClaimedChest first = runtime.chestClaimWorkflow().claim(anchors(anchor1), 2400, 0, "");
        ClaimedChest second = runtime.chestClaimWorkflow().claim(anchors(anchor2), 2560, 0, "");

        ClaimedChest result = runtime.chestClaimWorkflow().updateAnchors(
                second.storageId(), anchors(anchor1, anchor2)
        );

        assertNull(result, "anchor owned by a different claim must be rejected");
        // second remains unchanged
        assertEquals(1, runtime.chestClaimWorkflow().chest(second.storageId()).anchors().size());
    }

    @Test
    void removeAnchorLeavesChestAliveUntilLastAnchorRemoved() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        ChestAnchor left = new ChestAnchor(OVERWORLD, 10, 64, 5);
        ChestAnchor right = new ChestAnchor(OVERWORLD, 11, 64, 5);
        ClaimedChest chest = runtime.chestClaimWorkflow().claim(anchors(left, right), 2400, 0, "");

        ClaimedChest afterFirst = runtime.chestClaimWorkflow().removeAnchor(chest.storageId(), left);
        assertNotNull(afterFirst);
        assertEquals(1, afterFirst.anchors().size());
        assertTrue(afterFirst.anchors().contains(right));

        ClaimedChest afterSecond = runtime.chestClaimWorkflow().removeAnchor(chest.storageId(), right);
        assertNull(afterSecond, "removing the final anchor deletes the claim");
        assertNull(runtime.chestClaimWorkflow().chest(chest.storageId()));
    }

    @Test
    void relabelChestUpdatesLabel() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        ChestAnchor anchor = new ChestAnchor(OVERWORLD, 10, 64, 5);
        ClaimedChest chest = runtime.chestClaimWorkflow().claim(anchors(anchor), 2400, 0, "");

        ClaimedChest renamed = runtime.chestClaimWorkflow().relabelChest(chest.storageId(), "Ore Chest");

        assertEquals("Ore Chest", renamed.label());
    }

    @Test
    void deleteChestRemovesFromMap() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        ChestAnchor anchor = new ChestAnchor(OVERWORLD, 10, 64, 5);
        ClaimedChest chest = runtime.chestClaimWorkflow().claim(anchors(anchor), 2400, 0, "");

        assertTrue(runtime.chestClaimWorkflow().deleteChest(chest.storageId()));
        assertNull(runtime.chestClaimWorkflow().chest(chest.storageId()));
        assertNull(runtime.chestClaimWorkflow().chestByAnchor(anchor));
        assertFalse(runtime.chestClaimWorkflow().deleteChest(chest.storageId()));
    }

    @Test
    void claimWithIdReusesProvidedStorageId() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        UUID storageId = UUID.randomUUID();
        ChestAnchor anchor = new ChestAnchor(OVERWORLD, 10, 64, 5);

        ClaimedChest chest = runtime.chestClaimWorkflow().claimWithId(
                storageId, anchors(anchor), 2400, 0, ""
        );

        assertEquals(storageId, chest.storageId());
        // Re-claim with same id is rejected
        assertNull(runtime.chestClaimWorkflow().claimWithId(
                storageId, anchors(new ChestAnchor(OVERWORLD, 50, 64, 5)), 2400, 0, ""
        ));
    }

    private static Set<ChestAnchor> anchors(ChestAnchor... items) {
        LinkedHashSet<ChestAnchor> set = new LinkedHashSet<>();
        for (ChestAnchor anchor : items) {
            set.add(anchor);
        }
        return set;
    }
}
