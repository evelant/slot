package dev.imagio.slot.neoforge.network;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CursorTransferSupportTest {
    @Test
    void mergeIntoCarriedRespectsExistingStackLimit() {
        TestMenu menu = new TestMenu();
        menu.setCarried(new ItemStack("minecraft:apple", 60, 64));

        ItemStack remainder = CursorTransferSupport.mergeIntoCarried(menu, new ItemStack("minecraft:apple", 10, 64));

        assertEquals(64, menu.getCarried().getCount());
        assertEquals(6, remainder.getCount());
    }

    @Test
    void mergeIntoCarriedRespectsModdedMaxStackSize() {
        TestMenu menu = new TestMenu();
        menu.setCarried(new ItemStack("mod:bundle", 100, 200));

        ItemStack remainder = CursorTransferSupport.mergeIntoCarried(menu, new ItemStack("mod:bundle", 80, 200));

        assertEquals(180, menu.getCarried().getCount());
        assertTrue(remainder.isEmpty());
    }

    @Test
    void mergeIntoEmptyCarriedCapsToIncomingStackLimit() {
        TestMenu menu = new TestMenu();

        ItemStack remainder = CursorTransferSupport.mergeIntoCarried(menu, new ItemStack("minecraft:stone", 100, 64));

        assertEquals(64, menu.getCarried().getCount());
        assertEquals(36, remainder.getCount());
    }

    @Test
    void remainingCursorSpaceUsesCurrentStackLimit() {
        TestMenu menu = new TestMenu();
        menu.setCarried(new ItemStack("mod:bundle", 100, 200));

        assertEquals(100, CursorTransferSupport.remainingCursorSpace(menu));
    }

    private static final class TestMenu extends AbstractContainerMenu {
        private TestMenu() {
            super(null, 0);
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    }
}
