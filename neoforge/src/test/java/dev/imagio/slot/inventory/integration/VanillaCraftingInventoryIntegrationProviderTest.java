package dev.imagio.slot.inventory.integration;

import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import dev.imagio.slot.inventory.core.InventoryToolActionId;
import dev.imagio.slot.inventory.core.InventoryToolDescriptor;
import dev.imagio.slot.registry.ProviderResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VanillaCraftingInventoryIntegrationProviderTest {
    @Test
    void inventoryMenuRegistersTwoByTwoCraftingSurfaceWithoutRotate() {
        VanillaCraftingInventoryIntegrationProvider provider = new VanillaCraftingInventoryIntegrationProvider();
        Inventory inventory = new Inventory(new Player());
        TestInventoryMenu menu = new TestInventoryMenu();
        populateSurface(menu, inventory, 4);

        ProviderResult<InventoryHostSession> result = provider.openHost(new InventoryHostContext(
                menu,
                inventory,
                Component.literal("Inventory"),
                "test.inventory",
                false,
                true,
                false
        ));

        assertTrue(result.supported());
        InventoryHostSession session = result.value();
        InventoryToolDescriptor tool = session.tools().getFirst();
        assertEquals(2, tool.craftingSurface().gridWidth());
        assertEquals(2, tool.craftingSurface().gridHeight());
        assertEquals(
                List.of(InventoryToolActionId.CLEAR_GRID, InventoryToolActionId.BALANCE_GRID),
                tool.actions().stream().map(action -> action.id()).toList()
        );
        assertEquals(
                List.of(InventoryPaneMembership.HIDDEN, InventoryPaneMembership.HIDDEN),
                session.hostSources().stream().map(source -> source.paneMembership()).toList()
        );
    }

    @Test
    void craftingMenuRegistersThreeByThreeCraftingSurfaceWithRotate() {
        VanillaCraftingInventoryIntegrationProvider provider = new VanillaCraftingInventoryIntegrationProvider();
        Inventory inventory = new Inventory(new Player());
        TestCraftingMenu menu = new TestCraftingMenu();
        populateSurface(menu, inventory, 9);

        ProviderResult<InventoryHostSession> result = provider.openHost(new InventoryHostContext(
                menu,
                inventory,
                Component.literal("Crafting"),
                "test.crafting",
                false,
                true,
                false
        ));

        assertTrue(result.supported());
        InventoryHostSession session = result.value();
        InventoryToolDescriptor tool = session.tools().getFirst();
        assertEquals(3, tool.craftingSurface().gridWidth());
        assertEquals(3, tool.craftingSurface().gridHeight());
        assertEquals(
                List.of(
                        InventoryToolActionId.CLEAR_GRID,
                        InventoryToolActionId.BALANCE_GRID,
                        InventoryToolActionId.ROTATE_GRID
                ),
                tool.actions().stream().map(action -> action.id()).toList()
        );
        assertEquals(9, tool.craftingSurface().inputSlotCount());
    }

    private static void populateSurface(TestMenuAccess menu, Inventory inventory, int inputCount) {
        Container input = new CraftingContainer();
        Container output = new ResultContainer();
        for (int index = 0; index < inputCount; index++) {
            menu.addTestSlot(new Slot(input, index));
        }
        menu.addTestSlot(new Slot(output, 0));
        for (int index = 0; index < 9; index++) {
            menu.addTestSlot(new Slot(inventory, index));
        }
    }

    private interface TestMenuAccess {
        Slot addTestSlot(Slot slot);
    }

    private static final class TestInventoryMenu extends InventoryMenu implements TestMenuAccess {
        @Override
        public Slot addTestSlot(Slot slot) {
            return addSlot(slot);
        }
    }

    private static final class TestCraftingMenu extends CraftingMenu implements TestMenuAccess {
        @Override
        public Slot addTestSlot(Slot slot) {
            return addSlot(slot);
        }
    }
}
