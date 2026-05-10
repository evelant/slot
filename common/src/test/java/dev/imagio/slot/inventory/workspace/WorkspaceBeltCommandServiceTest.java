package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkspaceBeltCommandServiceTest {
    @Test
    void firstPartialOrFreeHotbarSlotPrefersMatchingPartialStack() {
        SlotWorkspaceViewModel viewModel = viewModel(
                occupied(1, "minecraft:dirt", 64, 64),
                occupied(4, "minecraft:stone", 12, 64)
        );

        int selected = WorkspaceBeltCommandService.firstPartialOrFreeHotbarSlot(
                viewModel,
                ItemIdentity.of("minecraft:stone")
        );

        assertEquals(4, selected);
    }

    @Test
    void firstPartialOrFreeHotbarSlotFallsBackToFirstFreeSlot() {
        SlotWorkspaceViewModel viewModel = viewModel(
                occupied(0, "minecraft:stone", 64, 64),
                occupied(1, "minecraft:dirt", 64, 64)
        );

        int selected = WorkspaceBeltCommandService.firstPartialOrFreeHotbarSlot(
                viewModel,
                ItemIdentity.of("minecraft:stone")
        );

        assertEquals(2, selected);
    }

    @Test
    void firstPartialOrFreeHotbarSlotRejectsFullBelt() {
        List<SlotWorkspaceViewModel.HotbarSlot> slots = new ArrayList<>();
        for (int index = 0; index < 9; index++) {
            slots.add(occupied(index, "minecraft:item_" + index, 64, 64));
        }

        int selected = WorkspaceBeltCommandService.firstPartialOrFreeHotbarSlot(
                viewModel(slots),
                ItemIdentity.of("minecraft:stone")
        );

        assertEquals(-1, selected);
    }

    @Test
    void assignHomeToFreeHotbarUsesCommonFirstSlotSelection() {
        SlotWorkspaceViewModel viewModel = viewModel(
                occupied(0, "minecraft:stone", 64, 64),
                occupied(2, "minecraft:stone", 12, 64)
        );
        AtomicInteger assigned = new AtomicInteger(-1);

        WorkspaceCommandOutcome outcome = WorkspaceBeltCommandService.assignHomeToFreeHotbar(
                null,
                null,
                viewModel,
                ItemIdentity.of("minecraft:stone"),
                true,
                hotbarIndex -> {
                    assigned.set(hotbarIndex);
                    return WorkspaceCommandOutcome.accepted("assigned", "");
                });

        assertTrue(outcome.success());
        assertEquals(2, assigned.get());
    }

    @Test
    void assignHomeToFreeHotbarReportsFullBeltWithoutFallbackMasking() {
        List<SlotWorkspaceViewModel.HotbarSlot> slots = new ArrayList<>();
        for (int index = 0; index < 9; index++) {
            slots.add(occupied(index, "minecraft:item_" + index, 64, 64));
        }

        WorkspaceCommandOutcome outcome = WorkspaceBeltCommandService.assignHomeToFreeHotbar(
                null,
                null,
                viewModel(slots),
                ItemIdentity.of("minecraft:stone"),
                true,
                hotbarIndex -> WorkspaceCommandOutcome.accepted("assigned", ""));

        assertFalse(outcome.success());
        assertEquals("no_free_hotbar_slot", outcome.status());
        assertEquals("all hotbar slots are occupied", outcome.diagnostics());
    }

    @Test
    void hotbarFallbackDepositMissOnlyAllowsKnownMissDiagnostics() {
        assertTrue(WorkspaceBeltCommandService.isHotbarFallbackDepositMiss(
                WorkspaceCommandOutcome.rejected("nothing_to_deposit")));
        assertTrue(WorkspaceBeltCommandService.isHotbarFallbackDepositMiss(
                WorkspaceCommandOutcome.rejected("desired_count_reserved")));
        assertTrue(WorkspaceBeltCommandService.isHotbarFallbackDepositMiss(
                WorkspaceCommandOutcome.rejected("no_linked_proximate_chest_with_room")));
        assertFalse(WorkspaceBeltCommandService.isHotbarFallbackDepositMiss(
                WorkspaceCommandOutcome.rejected("provider_failure")));
        assertFalse(WorkspaceBeltCommandService.isHotbarFallbackDepositMiss(
                WorkspaceCommandOutcome.accepted("deposited_stack", "")));
    }

    private static SlotWorkspaceViewModel viewModel(SlotWorkspaceViewModel.HotbarSlot... occupied) {
        ArrayList<SlotWorkspaceViewModel.HotbarSlot> slots = new ArrayList<>(SlotWorkspaceViewModel.emptyHotbar());
        for (SlotWorkspaceViewModel.HotbarSlot slot : occupied) {
            slots.set(slot.hotbarIndex(), slot);
        }
        return viewModel(slots);
    }

    private static SlotWorkspaceViewModel viewModel(List<SlotWorkspaceViewModel.HotbarSlot> hotbar) {
        return new SlotWorkspaceViewModel(
                0,
                "ready",
                "",
                0,
                0,
                1,
                1,
                0,
                0,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                hotbar,
                SlotWorkspaceViewModel.OffhandSlot.empty(),
                List.of()
        );
    }

    private static SlotWorkspaceViewModel.HotbarSlot occupied(
            int index,
            String itemId,
            int count,
            int maxStackSize
    ) {
        return new SlotWorkspaceViewModel.HotbarSlot(
                index,
                false,
                true,
                new ItemStack(itemId, count, maxStackSize),
                count);
    }
}
