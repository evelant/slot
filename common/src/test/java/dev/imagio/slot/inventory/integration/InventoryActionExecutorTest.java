package dev.imagio.slot.inventory.integration;

import dev.imagio.slot.inventory.action.InventoryActionConflictPolicy;
import dev.imagio.slot.inventory.action.InventoryActionKind;
import dev.imagio.slot.inventory.action.InventoryActionMode;
import dev.imagio.slot.inventory.action.InventoryActionOutcome;
import dev.imagio.slot.inventory.action.InventoryActionQuantity;
import dev.imagio.slot.inventory.action.InventoryActionRequest;
import dev.imagio.slot.inventory.action.InventoryActionScope;
import dev.imagio.slot.inventory.action.InventoryActionStatus;
import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.core.BuiltinInventoryDescriptors;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.HostInstanceKey;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventoryTopologyDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.core.PlayerRuntimeStateDescriptor;
import dev.imagio.slot.workflow.domain.ProtectionPolicy;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryActionExecutorTest {
    @Test
    void quickAccessAssignReplacesOccupiedHotbarSlotAndDisplacesPreviousStackToSourceSlot() {
        TestMenu menu = new TestMenu();
        InventoryHostDescriptor host = host(menu);
        ServerPlayer player = new ServerPlayer();
        player.containerMenu = menu;
        player.getInventory().items.set(10, new ItemStack("minecraft:crossbow", 1, 1));
        player.getInventory().items.set(2, new ItemStack("toms_storage:inventory_cable", 23, 64));

        InventoryActionRequest request = request(
                host,
                new InventoryActionTarget.SourceSlotTarget(BuiltinInventoryIds.PLAYER_MAIN, 1),
                new InventoryActionTarget.QuickAccessTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, 2),
                player.getInventory().items.get(10)
        );

        InventoryActionOutcome outcome = InventoryActionExecutor.execute(
                host,
                player,
                request,
                ProtectionPolicy.allowAll()
        );

        assertEquals(InventoryActionStatus.SUCCESS, outcome.status());
        assertTrue(outcome.stackRemainder().isEmpty());
        assertEquals("minecraft:crossbow", player.getInventory().items.get(2).itemId());
        assertEquals(1, player.getInventory().items.get(2).getCount());
        assertEquals("toms_storage:inventory_cable", player.getInventory().items.get(10).itemId());
        assertEquals(23, player.getInventory().items.get(10).getCount());
    }

    @Test
    void quickAccessAssignMovesStackIntoEmptyHotbarSlotAndClearsSourceSlot() {
        TestMenu menu = new TestMenu();
        InventoryHostDescriptor host = host(menu);
        ServerPlayer player = new ServerPlayer();
        player.containerMenu = menu;
        player.getInventory().items.set(10, new ItemStack("minecraft:crossbow", 1, 1));

        InventoryActionRequest request = request(
                host,
                new InventoryActionTarget.SourceSlotTarget(BuiltinInventoryIds.PLAYER_MAIN, 1),
                new InventoryActionTarget.QuickAccessTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, 2),
                player.getInventory().items.get(10)
        );

        InventoryActionOutcome outcome = InventoryActionExecutor.execute(
                host,
                player,
                request,
                ProtectionPolicy.allowAll()
        );

        assertEquals(InventoryActionStatus.SUCCESS, outcome.status());
        assertTrue(outcome.stackRemainder().isEmpty());
        assertEquals("minecraft:crossbow", player.getInventory().items.get(2).itemId());
        assertTrue(player.getInventory().items.get(10).isEmpty());
    }

    @Test
    void transferStackMovesHotbarStackIntoMainInventorySource() {
        TestMenu menu = new TestMenu();
        InventoryHostDescriptor host = host(menu);
        ServerPlayer player = new ServerPlayer();
        player.containerMenu = menu;
        player.getInventory().items.set(2, new ItemStack("toms_storage:inventory_cable", 23, 64));

        ItemStack stack = player.getInventory().items.get(2);
        InventoryActionRequest request = new InventoryActionRequest(
                host.hostId(),
                host.serverMenuRef(),
                "test-hotbar-to-main",
                InventoryActionKind.TRANSFER,
                InventoryActionMode.EXECUTE,
                InventoryActionQuantity.STACK,
                InventoryActionScope.SINGLE_TARGET,
                InventoryActionConflictPolicy.INSERT_ONLY,
                "test.hotbar_to_main",
                new InventoryActionTarget.QuickAccessTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, 2),
                new InventoryActionTarget.SourceTarget(BuiltinInventoryIds.PLAYER_MAIN),
                stack.getCount(),
                ItemIdentityMatcher.create(stack),
                stack.copy(),
                null,
                null,
                false,
                ""
        );

        InventoryActionOutcome outcome = InventoryActionExecutor.execute(
                host,
                player,
                request,
                ProtectionPolicy.allowAll()
        );

        assertEquals(InventoryActionStatus.SUCCESS, outcome.status());
        assertTrue(player.getInventory().items.get(2).isEmpty());
        assertEquals("toms_storage:inventory_cable", player.getInventory().items.get(9).itemId());
        assertEquals(23, player.getInventory().items.get(9).getCount());
    }

    private static InventoryActionRequest request(
            InventoryHostDescriptor host,
            InventoryActionTarget source,
            InventoryActionTarget destination,
            ItemStack stack
    ) {
        return new InventoryActionRequest(
                host.hostId(),
                host.serverMenuRef(),
                "test-quick-access-assign",
                InventoryActionKind.ASSIGN,
                InventoryActionMode.EXECUTE,
                InventoryActionQuantity.STACK,
                InventoryActionScope.SINGLE_TARGET,
                InventoryActionConflictPolicy.ASSIGN_WITH_DISPLACE,
                "test.quick_access_assign",
                source,
                destination,
                stack.getCount(),
                ItemIdentityMatcher.create(stack),
                stack.copy(),
                null,
                null,
                false,
                ""
        );
    }

    private static InventoryHostDescriptor host(TestMenu menu) {
        return new InventoryHostDescriptor(
                new HostInstanceKey(TestMenu.class.getName(), 0, "slot.workspace.test", ""),
                InventoryHostDescriptor.serverMenuRef(menu),
                "slot.workspace.test",
                Component.literal("Workspace Test"),
                menu,
                InventoryTopologyDescriptor.empty(),
                InventoryHostSession.empty(),
                List.of(),
                PlayerRuntimeStateDescriptor.vanilla(0),
                List.of(
                        BuiltinInventoryDescriptors.playerMain(InventoryTopologyDescriptor.empty()),
                        BuiltinInventoryDescriptors.quickAccessLane0Source(InventoryTopologyDescriptor.empty())
                ),
                BuiltinInventoryDescriptors.builtInQuickAccessLanes(),
                BuiltinInventoryDescriptors.builtInEquipmentGroups(),
                List.of(),
                InventoryHostObservationHints.defaults(),
                ""
        );
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
