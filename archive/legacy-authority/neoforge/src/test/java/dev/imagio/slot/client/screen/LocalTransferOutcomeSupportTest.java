package dev.imagio.slot.client.screen;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalTransferOutcomeSupportTest {
    @Test
    void movedAnyAcceptsLogicalMenuSlots() {
        LogicalSlotMenu menu = new LogicalSlotMenu();
        menu.logicalSourceSlot.set(new ItemStack("minecraft:stone", 2, 64));
        menu.targetSlot.set(ItemStack.EMPTY);

        ItemStack sourceBefore = menu.logicalSourceSlot.getItem().copy();
        ItemStack targetBefore = menu.targetSlot.getItem().copy();

        menu.logicalSourceSlot.set(new ItemStack("minecraft:stone", 1, 64));
        menu.targetSlot.set(new ItemStack("minecraft:stone", 1, 64));

        assertTrue(LocalTransferOutcomeSupport.movedAny(menu, 5, 0, sourceBefore, targetBefore));
    }

    private static final class LogicalSlotMenu extends AbstractContainerMenu {
        private final Slot targetSlot;
        private final Slot logicalSourceSlot;

        private LogicalSlotMenu() {
            super(null, 0);
            SimpleContainer targetContainer = new SimpleContainer(1);
            SimpleContainer sourceContainer = new SimpleContainer(1);
            targetSlot = addSlot(new Slot(targetContainer, 0, 0, 0));
            logicalSourceSlot = new Slot(sourceContainer, 0, 0, 0);
        }

        @Override
        public Slot getSlot(int index) {
            if (index == 5) {
                return logicalSourceSlot;
            }
            return super.getSlot(index);
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    }
}
