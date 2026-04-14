package dev.imagio.slot.client.screen;

import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.projection.InventoryPane;
import dev.imagio.slot.session.ChestLikeMenuLayout;
import dev.imagio.slot.session.InventoryHostDescriptor;
import dev.imagio.slot.storage.adapter.ExternalStorageStackSnapshot;
import dev.imagio.slot.storage.adapter.StorageTransferMode;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import dev.imagio.slot.storage.provider.StorageViewProviderSession;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class QuickAccessMenuSlotPlannerTest {
    @Test
    void offhandSlotPrefersLayoutSourceMappingForNonInventoryMenus() {
        TestMenu menu = new TestMenu(12);
        ChestLikeMenuLayout layout = new ChestLikeMenuLayout(
                2,
                List.of(),
                "Test",
                Map.of(
                        ChestLikeMenuLayout.SOURCE_OPEN_CONTAINER, List.of(0, 1),
                        ChestLikeMenuLayout.SOURCE_PLAYER_MAIN, List.of(6, 7),
                        ChestLikeMenuLayout.SOURCE_PLAYER_HOTBAR, List.of(8, 9, 10),
                        ChestLikeMenuLayout.SOURCE_PLAYER_OFFHAND, List.of(11)
                ),
                Map.of(
                        0, ChestLikeMenuLayout.SOURCE_OPEN_CONTAINER,
                        1, ChestLikeMenuLayout.SOURCE_OPEN_CONTAINER,
                        6, ChestLikeMenuLayout.SOURCE_PLAYER_MAIN,
                        7, ChestLikeMenuLayout.SOURCE_PLAYER_MAIN,
                        8, ChestLikeMenuLayout.SOURCE_PLAYER_HOTBAR,
                        9, ChestLikeMenuLayout.SOURCE_PLAYER_HOTBAR,
                        10, ChestLikeMenuLayout.SOURCE_PLAYER_HOTBAR,
                        11, ChestLikeMenuLayout.SOURCE_PLAYER_OFFHAND
                ),
                false,
                new TestStorageSession()
        );
        InventoryScreenContext context = InventoryScreenContext.fromHost(
                InventoryHostDescriptor.create("screen:test", Component.literal("Test"), menu, layout, false, false, false)
        );

        QuickAccessMenuSlotPlanner planner = new QuickAccessMenuSlotPlanner(context);
        assertEquals(11, planner.offhandMenuSlot(menu));
    }

    @Test
    void vanillaHotbarAndMainFallbackRangesExcludeBoundarySlots() {
        QuickAccessMenuSlotPlanner planner = new QuickAccessMenuSlotPlanner(null);

        assertEquals(List.of(36, 37, 38, 39, 40, 41, 42, 43, 44), planner.hotbarMenuSlots());
        assertEquals(27, planner.mainInventoryMenuSlots().size());
        assertEquals(9, planner.mainInventoryMenuSlots().get(0));
        assertEquals(35, planner.mainInventoryMenuSlots().get(planner.mainInventoryMenuSlots().size() - 1));
    }

    @Test
    void layoutAwarePlannerDoesNotInventVanillaPlayerRowsWhenMenuOmitsThem() {
        TestMenu menu = new TestMenu(20);
        ChestLikeMenuLayout layout = new ChestLikeMenuLayout(
                6,
                List.of(),
                "Test",
                Map.of(
                        ChestLikeMenuLayout.SOURCE_OPEN_CONTAINER, List.of(0, 1, 2, 3, 4, 5),
                        ChestLikeMenuLayout.SOURCE_PLAYER_MAIN, List.of(),
                        ChestLikeMenuLayout.SOURCE_PLAYER_HOTBAR, List.of(),
                        ChestLikeMenuLayout.SOURCE_PLAYER_ARMOR, List.of(18),
                        ChestLikeMenuLayout.SOURCE_PLAYER_OFFHAND, List.of(19)
                ),
                Map.of(
                        0, ChestLikeMenuLayout.SOURCE_OPEN_CONTAINER,
                        1, ChestLikeMenuLayout.SOURCE_OPEN_CONTAINER,
                        2, ChestLikeMenuLayout.SOURCE_OPEN_CONTAINER,
                        3, ChestLikeMenuLayout.SOURCE_OPEN_CONTAINER,
                        4, ChestLikeMenuLayout.SOURCE_OPEN_CONTAINER,
                        5, ChestLikeMenuLayout.SOURCE_OPEN_CONTAINER,
                        18, ChestLikeMenuLayout.SOURCE_PLAYER_ARMOR,
                        19, ChestLikeMenuLayout.SOURCE_PLAYER_OFFHAND
                ),
                false,
                new TestStorageSession()
        );
        InventoryScreenContext context = InventoryScreenContext.fromHost(
                InventoryHostDescriptor.create("screen:test", Component.literal("Test"), menu, layout, false, false, false)
        );

        QuickAccessMenuSlotPlanner planner = new QuickAccessMenuSlotPlanner(context);

        assertEquals(List.of(), planner.mainInventoryMenuSlots());
        assertEquals(List.of(), planner.hotbarMenuSlots());
        assertEquals(List.of(18, 19), planner.candidateSourceMenuSlots(Set.of()));
        assertFalse(planner.candidateSourceMenuSlots(Set.of()).contains(36));
    }

    private static final class TestMenu extends AbstractContainerMenu {
        TestMenu(int slotCount) {
            super(null, 0);
            SimpleContainer container = new SimpleContainer(slotCount);
            for (int slot = 0; slot < slotCount; slot++) {
                addSlot(new Slot(container, slot, 0, 0));
            }
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    }

    private static final class TestStorageSession implements StorageViewProviderSession {
        @Override
        public String providerId() {
            return "test";
        }

        @Override
        public dev.imagio.slot.session.InventorySourceDescriptor primaryStorageSource() {
            return dev.imagio.slot.session.InventorySourceDescriptor.builder(ChestLikeMenuLayout.SOURCE_OPEN_CONTAINER)
                    .label(net.minecraft.network.chat.Component.literal("Test"))
                    .domain(dev.imagio.slot.session.InventorySourceDomain.HOST_STORAGE)
                    .role(dev.imagio.slot.session.InventorySourceRole.PRIMARY_STORAGE)
                    .slotCount(2)
                    .backingKind(dev.imagio.slot.session.InventorySourceBackingKind.MENU_BACKED)
                    .capabilities(java.util.Set.of(
                            dev.imagio.slot.session.InventorySourceCapability.INSERT,
                            dev.imagio.slot.session.InventorySourceCapability.EXTRACT
                    ))
                    .actionable(true)
                    .menuBacked(true)
                    .build();
        }

        @Override
        public boolean primaryStorageIsCarried() {
            return false;
        }

        @Override
        public List<Integer> primaryMenuSlots() {
            return List.of(0, 1);
        }

        @Override
        public List<ExternalStorageStackSnapshot> readClientPrimarySnapshots(AbstractContainerMenu menu) {
            return List.of();
        }

        @Override
        public ItemStack extractFromPrimary(AbstractContainerMenu menu, ServerPlayer player, ItemIdentity identity, StorageTransferMode mode) {
            return new ItemStack();
        }

        @Override
        public ItemStack insertIntoPrimary(AbstractContainerMenu menu, ServerPlayer player, ItemStack stack) {
            return stack;
        }

    }
}
