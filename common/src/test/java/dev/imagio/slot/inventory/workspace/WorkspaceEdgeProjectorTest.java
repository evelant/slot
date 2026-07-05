package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.ItemComparisonMode;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.workflow.domain.CraftRunState;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

class WorkspaceEdgeProjectorTest {
    @Test
    void changedWayfindingTargetRebuildsOnlyThatTarget() {
        SlotWorkspaceViewModel firstView = view(
                List.of(
                        target("storage-a", identity("minecraft:stone"), 1),
                        target("storage-b", identity("minecraft:dirt"), 1)),
                Set.of(ref("minecraft:stone")));
        WorkspaceEdgeProjector.Result first = WorkspaceEdgeProjector.project(firstView, null);

        SlotWorkspaceViewModel secondView = view(
                List.of(
                        target("storage-a", identity("minecraft:stone"), 2),
                        target("storage-b", identity("minecraft:dirt"), 1)),
                Set.of(ref("minecraft:stone")));
        WorkspaceEdgeProjector.Result second = WorkspaceEdgeProjector.project(secondView, first.state());

        assertEquals(1, second.stats().reusedWayfindingTargets());
        assertEquals(1, second.stats().rebuiltWayfindingTargets());
        assertEquals(0, second.stats().removedWayfindingTargets());
        assertEquals(1, second.stats().reusedDepositabilitySets());
        assertEquals(0, second.stats().rebuiltDepositabilitySets());
        assertSame(target(first.viewModel(), "storage-b"), target(second.viewModel(), "storage-b"));
        assertNotSame(target(first.viewModel(), "storage-a"), target(second.viewModel(), "storage-a"));
    }

    @Test
    void removedWayfindingTargetDoesNotRebuildSiblingTarget() {
        SlotWorkspaceViewModel firstView = view(
                List.of(
                        target("storage-a", identity("minecraft:stone"), 1),
                        target("storage-b", identity("minecraft:dirt"), 1)),
                Set.of(ref("minecraft:stone")));
        WorkspaceEdgeProjector.Result first = WorkspaceEdgeProjector.project(firstView, null);

        SlotWorkspaceViewModel secondView = view(
                List.of(target("storage-b", identity("minecraft:dirt"), 1)),
                Set.of(ref("minecraft:stone"), ref("minecraft:dirt")));
        WorkspaceEdgeProjector.Result second = WorkspaceEdgeProjector.project(secondView, first.state());

        assertEquals(1, second.stats().reusedWayfindingTargets());
        assertEquals(0, second.stats().rebuiltWayfindingTargets());
        assertEquals(1, second.stats().removedWayfindingTargets());
        assertEquals(0, second.stats().reusedDepositabilitySets());
        assertEquals(1, second.stats().rebuiltDepositabilitySets());
        assertSame(target(first.viewModel(), "storage-b"), target(second.viewModel(), "storage-b"));
    }

    private static SlotWorkspaceViewModel view(
            List<WayfindingTarget> targets,
            Set<SlotWorkspaceViewModel.IdentityRef> depositable
    ) {
        return new SlotWorkspaceViewModel(
                1L,
                "ready",
                "",
                0,
                0,
                100,
                100,
                0,
                0,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                SlotWorkspaceViewModel.OffhandSlot.empty(),
                List.of(),
                SlotWorkspaceViewModel.LootChestPanel.empty(),
                targets,
                depositable,
                List.of(),
                SlotWorkspaceViewModel.ActiveChestPanel.empty(),
                CraftRunState.empty(),
                List.of());
    }

    private static WayfindingTarget target(String storageId, ItemIdentity identity, int totalCount) {
        return new WayfindingTarget(
                storageId,
                "minecraft:overworld",
                storageId.endsWith("a") ? 1 : 2,
                64,
                0,
                Set.of(identity),
                totalCount,
                WayfindingTarget.Scope.PLAYER);
    }

    private static WayfindingTarget target(SlotWorkspaceViewModel viewModel, String storageId) {
        for (WayfindingTarget target : viewModel.wayfindingTargets()) {
            if (target.storageId().equals(storageId)) {
                return target;
            }
        }
        throw new AssertionError("missing target " + storageId);
    }

    private static SlotWorkspaceViewModel.IdentityRef ref(String itemId) {
        return SlotWorkspaceViewModel.IdentityRef.from(identity(itemId));
    }

    private static ItemIdentity identity(String itemId) {
        return new ItemIdentity(itemId, ItemComparisonMode.ITEM_ID, "");
    }
}
