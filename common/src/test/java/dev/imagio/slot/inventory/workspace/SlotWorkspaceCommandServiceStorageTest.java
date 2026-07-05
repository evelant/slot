package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.ItemComparisonMode;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.InMemoryWorkflowDomainStateRepository;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlotWorkspaceCommandServiceStorageTest {
    @Test
    void moveChestEmitsFrameOnlyInvalidation() {
        WorkflowDomainRuntime runtime = runtime();
        UUID storageId = UUID.randomUUID();
        claimChest(runtime, storageId, "Overflow");

        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.moveChest(
                runtime,
                null,
                storageId.toString(),
                7,
                9);

        assertTrue(outcome.success(), outcome.diagnostics());
        assertEquals(7, runtime.chestClaimWorkflow().claimedChestMap().chest(storageId).atlasX());
        assertEquals(9, runtime.chestClaimWorkflow().claimedChestMap().chest(storageId).atlasY());
        assertEquals(1, outcome.invalidations().size());
        WorkspaceInvalidation invalidation = outcome.invalidations().get(0);
        assertEquals(WorkspaceInvalidation.Reason.COMMAND_OUTCOME, invalidation.reason());
        assertFalse(invalidation.requiresFullProjection());
        assertTrue(invalidation.identities().isEmpty());
        assertTrue(invalidation.storageIds().isEmpty());
        assertEquals(Set.of(WorkspaceProjectionSlice.FRAME), invalidation.slices());
        assertEquals("chest_position_changed", invalidation.diagnostics());
    }

    @Test
    void relabelChestEmitsStorageInvalidation() {
        WorkflowDomainRuntime runtime = runtime();
        UUID storageId = UUID.randomUUID();
        claimChest(runtime, storageId, "Overflow");
        SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                InventoryAuthoritySnapshot.empty(),
                runtime.snapshot(),
                "ready",
                "",
                0,
                0,
                1);
        assertNotNull(viewModel.chestChip(storageId.toString()));

        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.relabelChest(
                runtime,
                viewModel,
                storageId.toString(),
                "Bulk");

        assertTrue(outcome.success(), outcome.diagnostics());
        assertEquals("Bulk", runtime.chestClaimWorkflow().claimedChestMap().chest(storageId).label());
        assertEquals(1, outcome.invalidations().size());
        WorkspaceInvalidation invalidation = outcome.invalidations().get(0);
        assertEquals(WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED, invalidation.reason());
        assertFalse(invalidation.requiresFullProjection());
        assertTrue(invalidation.identities().isEmpty());
        assertEquals(Set.of(storageId.toString()), invalidation.storageIds());
        assertTrue(invalidation.slices().contains(WorkspaceProjectionSlice.STORAGE));
        assertTrue(invalidation.slices().contains(WorkspaceProjectionSlice.FRAME));
        assertEquals("chest_label_changed", invalidation.diagnostics());
    }

    @Test
    void relabelClusterEmitsStorageInvalidationForClusterMembers() {
        WorkflowDomainRuntime runtime = runtime();
        UUID storageId = UUID.randomUUID();
        claimChest(runtime, storageId, "Overflow");
        String clusterId = "cluster-" + storageId;

        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.relabelCluster(
                runtime,
                clusterId,
                "Workshop");

        assertTrue(outcome.success(), outcome.diagnostics());
        assertEquals(1, outcome.invalidations().size());
        WorkspaceInvalidation invalidation = outcome.invalidations().get(0);
        assertEquals(WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED, invalidation.reason());
        assertFalse(invalidation.requiresFullProjection());
        assertTrue(invalidation.identities().isEmpty());
        assertEquals(Set.of(storageId.toString()), invalidation.storageIds());
        assertTrue(invalidation.slices().contains(WorkspaceProjectionSlice.STORAGE));
        assertTrue(invalidation.slices().contains(WorkspaceProjectionSlice.FRAME));
        assertEquals("cluster_label_changed", invalidation.diagnostics());
    }

    @Test
    void forgetItemAffinityEmitsMixedIdentityStorageInvalidation() {
        WorkflowDomainRuntime runtime = runtime();
        UUID storageId = UUID.randomUUID();
        ItemIdentity redstone = ItemIdentity.of("minecraft:redstone");
        claimChest(runtime, storageId, "Overflow");
        runtime.chestClaimWorkflow().recordDeposit(storageId, redstone, 1, 0L);

        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.forgetItemAffinity(
                runtime,
                storageId.toString(),
                redstone.itemId(),
                ItemComparisonMode.ITEM_ID.name(),
                "");

        assertTrue(outcome.success(), outcome.diagnostics());
        assertEquals(0, runtime.chestClaimWorkflow().chestAffinityMap().score(storageId, redstone));
        assertEquals(1, outcome.invalidations().size());
        WorkspaceInvalidation invalidation = outcome.invalidations().get(0);
        assertEquals(WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED, invalidation.reason());
        assertFalse(invalidation.requiresFullProjection());
        assertTrue(invalidation.identities().contains(redstone));
        assertEquals(Set.of(storageId.toString()), invalidation.storageIds());
        assertTrue(invalidation.slices().contains(WorkspaceProjectionSlice.CARD));
        assertTrue(invalidation.slices().contains(WorkspaceProjectionSlice.STORAGE));
        assertTrue(invalidation.slices().contains(WorkspaceProjectionSlice.DEPOSITABILITY));
        assertEquals("affinity_forgotten", invalidation.diagnostics());
    }

    private static WorkflowDomainRuntime runtime() {
        return new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
    }

    private static void claimChest(
            WorkflowDomainRuntime runtime,
            UUID storageId,
            String label
    ) {
        assertNotNull(runtime.chestClaimWorkflow().claimWithId(
                storageId,
                Set.of(new ChestAnchor("minecraft:overworld", 12, 64, 12)),
                0,
                0,
                label));
    }
}
