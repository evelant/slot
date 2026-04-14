package dev.imagio.slot.inventory;

import dev.imagio.slot.session.ChestLikeMenuLayout;
import dev.imagio.slot.storage.adapter.ExternalStorageStackSnapshot;
import dev.imagio.slot.storage.adapter.StorageTransferMode;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CarriedPlacementPolicyTest {
    @Test
    void layoutAwareTemporarySlotSearchDoesNotFallBackToVanillaPlayerRows() {
        TestMenu menu = new TestMenu(54);
        ChestLikeMenuLayout layout = layout(Map.of(
                ChestLikeMenuLayout.SOURCE_OPEN_CONTAINER, List.of(0, 1, 2, 3, 4, 5),
                ChestLikeMenuLayout.SOURCE_PLAYER_MAIN, List.of(),
                ChestLikeMenuLayout.SOURCE_PLAYER_HOTBAR, List.of(),
                ChestLikeMenuLayout.SOURCE_PLAYER_ARMOR, List.of(),
                ChestLikeMenuLayout.SOURCE_PLAYER_OFFHAND, List.of()
        ));

        Integer emptySlot = CarriedPlacementPolicy.findEmptyCarriedMenuSlot(
                menu,
                layout,
                CarriedPlacementPolicy.Intent.TEMPORARY,
                java.util.Set.of(),
                new ItemStack("minecraft:stone", 1, 64)
        );

        assertNull(emptySlot);
    }

    @Test
    void layoutAwareTemporarySlotSearchUsesExplicitCarriedStorageWhenAvailable() {
        TestMenu menu = new TestMenu(54);
        ChestLikeMenuLayout layout = layout(Map.of(
                ChestLikeMenuLayout.SOURCE_OPEN_CONTAINER, List.of(0, 1, 2, 3, 4, 5),
                ChestLikeMenuLayout.SOURCE_CARRIED_STORAGE, List.of(50, 51),
                ChestLikeMenuLayout.SOURCE_PLAYER_MAIN, List.of(),
                ChestLikeMenuLayout.SOURCE_PLAYER_HOTBAR, List.of(),
                ChestLikeMenuLayout.SOURCE_PLAYER_ARMOR, List.of(),
                ChestLikeMenuLayout.SOURCE_PLAYER_OFFHAND, List.of()
        ));

        Integer emptySlot = CarriedPlacementPolicy.findEmptyCarriedMenuSlot(
                menu,
                layout,
                CarriedPlacementPolicy.Intent.TEMPORARY,
                java.util.Set.of(),
                new ItemStack("minecraft:stone", 1, 64)
        );

        assertEquals(50, emptySlot);
    }

    private static ChestLikeMenuLayout layout(Map<String, List<Integer>> sourceMenuSlots) {
        return new ChestLikeMenuLayout(
                sourceMenuSlots.getOrDefault(ChestLikeMenuLayout.SOURCE_OPEN_CONTAINER, List.of()).size(),
                List.of(),
                "Test",
                sourceMenuSlots,
                sourceIdsByMenuSlot(sourceMenuSlots),
                false,
                new TestStorageSession()
        );
    }

    private static Map<Integer, String> sourceIdsByMenuSlot(Map<String, List<Integer>> sourceMenuSlots) {
        java.util.LinkedHashMap<Integer, String> sourceIds = new java.util.LinkedHashMap<>();
        for (Map.Entry<String, List<Integer>> entry : sourceMenuSlots.entrySet()) {
            for (int menuSlot : entry.getValue()) {
                sourceIds.put(menuSlot, entry.getKey());
            }
        }
        return Map.copyOf(sourceIds);
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
                    .slotCount(6)
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
            return List.of(0, 1, 2, 3, 4, 5);
        }

        @Override
        public List<ExternalStorageStackSnapshot> readClientPrimarySnapshots(AbstractContainerMenu menu) {
            return List.of();
        }

        @Override
        public ItemStack extractFromPrimary(AbstractContainerMenu menu, ServerPlayer player, dev.imagio.slot.client.model.ItemIdentity identity, StorageTransferMode mode) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertIntoPrimary(AbstractContainerMenu menu, ServerPlayer player, ItemStack stack) {
            return stack;
        }

    }
}
