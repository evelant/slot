package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChestDepositObservationSupportTest {
    @Test
    void observesNetDepositsByIdentity() {
        ItemStack[] snapshot = {
                new ItemStack("minecraft:redstone", 4, 64),
                ItemStack.EMPTY
        };
        List<ItemStack> current = List.of(
                new ItemStack("minecraft:redstone", 7, 64),
                new ItemStack("minecraft:stone", 3, 64));

        ChestDepositObservationSupport.Observation observation =
                ChestDepositObservationSupport.observe(snapshot, current);

        assertEquals(3, observation.deposits().get(ItemIdentity.of("minecraft:redstone")));
        assertEquals(3, observation.deposits().get(ItemIdentity.of("minecraft:stone")));
        assertTrue(observation.takes().isEmpty());
    }

    @Test
    void observesNetTakesByIdentity() {
        ItemStack[] snapshot = {
                new ItemStack("minecraft:redstone", 12, 64),
                new ItemStack("minecraft:stone", 3, 64)
        };
        List<ItemStack> current = List.of(
                new ItemStack("minecraft:redstone", 5, 64),
                ItemStack.EMPTY);

        ChestDepositObservationSupport.Observation observation =
                ChestDepositObservationSupport.observe(snapshot, current);

        assertTrue(observation.deposits().isEmpty());
        assertEquals(7, observation.takes().get(ItemIdentity.of("minecraft:redstone")));
        assertEquals(3, observation.takes().get(ItemIdentity.of("minecraft:stone")));
    }

    @Test
    void storageMenuSlotsIgnorePlayerInventorySlots() {
        Inventory playerInventory = new Inventory(null);
        FakeContainer storage = new FakeContainer();
        TestMenu menu = new TestMenu()
                .withSlot(storage, 0, ItemStack.EMPTY)
                .withSlot(playerInventory, 0, ItemStack.EMPTY)
                .withSlot(storage, 1, ItemStack.EMPTY)
                .withSlot(playerInventory, 1, ItemStack.EMPTY);

        List<Integer> storageSlots = ChestDepositObservationSupport.storageMenuSlots(menu, playerInventory);

        assertEquals(List.of(0, 2), storageSlots);
    }

    @Test
    void observesMenuStorageSlotsByMenuIndex() {
        Inventory playerInventory = new Inventory(null);
        FakeContainer storage = new FakeContainer();
        TestMenu menu = new TestMenu()
                .withSlot(storage, 0, new ItemStack("minecraft:apple", 2, 64))
                .withSlot(playerInventory, 0, ItemStack.EMPTY)
                .withSlot(storage, 1, ItemStack.EMPTY);
        List<Integer> storageSlots = ChestDepositObservationSupport.storageMenuSlots(menu, playerInventory);
        ItemStack[] snapshot = ChestDepositObservationSupport.snapshot(menu, storageSlots);
        menu.getSlot(0).set(new ItemStack("minecraft:apple", 5, 64));
        menu.getSlot(2).set(new ItemStack("minecraft:stone", 1, 64));
        playerInventory.items.set(0, new ItemStack("minecraft:redstone", 64, 64));

        ChestDepositObservationSupport.Observation observation =
                ChestDepositObservationSupport.observe(snapshot, menu, storageSlots);

        assertEquals(3, observation.deposits().get(ItemIdentity.of("minecraft:apple")));
        assertEquals(1, observation.deposits().get(ItemIdentity.of("minecraft:stone")));
        assertTrue(observation.takes().isEmpty());
    }

    private static final class TestMenu extends AbstractContainerMenu {
        private TestMenu() {
            super(null, 1);
        }

        private TestMenu withSlot(Container container, int containerSlot, ItemStack stack) {
            addSlot(new Slot(container, containerSlot, stack));
            return this;
        }

        @Override
        public boolean stillValid(net.minecraft.world.entity.player.Player player) {
            return true;
        }
    }

    private static final class FakeContainer implements Container {
    }
}
