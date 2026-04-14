package dev.imagio.slot.session;

import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.projection.InventoryPane;
import dev.imagio.slot.storage.adapter.ExternalStorageStackSnapshot;
import dev.imagio.slot.storage.adapter.StorageTransferMode;
import dev.imagio.slot.storage.provider.StorageViewProviderSession;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChestLikeMenuLayoutTest {
    @Test
    void carriedSourcesIncludeEquippedSourcesByDefault() {
        assertTrue(ChestLikeMenuLayout.BASE_CARRIED_SOURCES.contains(ChestLikeMenuLayout.SOURCE_PLAYER_ARMOR));
        assertTrue(ChestLikeMenuLayout.BASE_CARRIED_SOURCES.contains(ChestLikeMenuLayout.SOURCE_PLAYER_OFFHAND));
    }

    @Test
    void dualPaneCarriedSourcesExposeOffhandAndMenuBackedActionsWhenAvailable() {
        ChestLikeMenuLayout layout = layout(Map.of(
                ChestLikeMenuLayout.SOURCE_OPEN_CONTAINER, List.of(0, 1),
                ChestLikeMenuLayout.SOURCE_PLAYER_MAIN, List.of(9, 10),
                ChestLikeMenuLayout.SOURCE_PLAYER_HOTBAR, List.of(36, 37),
                ChestLikeMenuLayout.SOURCE_PLAYER_ARMOR, List.of(38, 39),
                ChestLikeMenuLayout.SOURCE_PLAYER_OFFHAND, List.of(45)
        ), false);

        assertTrue(layout.sourceIdsForPane(InventoryPane.CARRIED).contains(ChestLikeMenuLayout.SOURCE_PLAYER_ARMOR));
        assertTrue(layout.sourceIdsForPane(InventoryPane.CARRIED).contains(ChestLikeMenuLayout.SOURCE_PLAYER_OFFHAND));
        assertTrue(layout.transferTargetSourceIds(InventoryPane.OPEN_CONTAINER).contains(ChestLikeMenuLayout.SOURCE_PLAYER_ARMOR));
        assertTrue(layout.transferTargetSourceIds(InventoryPane.OPEN_CONTAINER).contains(ChestLikeMenuLayout.SOURCE_PLAYER_OFFHAND));
        assertTrue(layout.actionSourceIdsForPane(InventoryPane.CARRIED).contains(ChestLikeMenuLayout.SOURCE_PLAYER_ARMOR));
        assertTrue(layout.actionSourceIdsForPane(InventoryPane.CARRIED).contains(ChestLikeMenuLayout.SOURCE_PLAYER_OFFHAND));
    }

    @Test
    void dualPaneActionSourcesDoNotPretendEquippedSourcesAreMenuBackedWhenTheyAreNotPresent() {
        ChestLikeMenuLayout layout = layout(Map.of(
                ChestLikeMenuLayout.SOURCE_OPEN_CONTAINER, List.of(0, 1),
                ChestLikeMenuLayout.SOURCE_PLAYER_MAIN, List.of(),
                ChestLikeMenuLayout.SOURCE_PLAYER_HOTBAR, List.of(),
                ChestLikeMenuLayout.SOURCE_PLAYER_ARMOR, List.of(),
                ChestLikeMenuLayout.SOURCE_PLAYER_OFFHAND, List.of()
        ), false);

        assertTrue(layout.sourceIdsForPane(InventoryPane.CARRIED).contains(ChestLikeMenuLayout.SOURCE_PLAYER_MAIN));
        assertTrue(layout.sourceIdsForPane(InventoryPane.CARRIED).contains(ChestLikeMenuLayout.SOURCE_PLAYER_HOTBAR));
        assertTrue(layout.sourceIdsForPane(InventoryPane.CARRIED).contains(ChestLikeMenuLayout.SOURCE_PLAYER_ARMOR));
        assertTrue(layout.sourceIdsForPane(InventoryPane.CARRIED).contains(ChestLikeMenuLayout.SOURCE_PLAYER_OFFHAND));
        assertFalse(layout.actionSourceIdsForPane(InventoryPane.CARRIED).contains(ChestLikeMenuLayout.SOURCE_PLAYER_MAIN));
        assertFalse(layout.actionSourceIdsForPane(InventoryPane.CARRIED).contains(ChestLikeMenuLayout.SOURCE_PLAYER_HOTBAR));
        assertFalse(layout.actionSourceIdsForPane(InventoryPane.CARRIED).contains(ChestLikeMenuLayout.SOURCE_PLAYER_ARMOR));
        assertFalse(layout.actionSourceIdsForPane(InventoryPane.CARRIED).contains(ChestLikeMenuLayout.SOURCE_PLAYER_OFFHAND));
    }

    @Test
    void menuBackedPlayerSourcesResolveSourceRelativeSlotIndices() {
        ChestLikeMenuLayout layout = layout(Map.of(
                ChestLikeMenuLayout.SOURCE_OPEN_CONTAINER, List.of(0, 1),
                ChestLikeMenuLayout.SOURCE_PLAYER_MAIN, List.of(9, 10, 11),
                ChestLikeMenuLayout.SOURCE_PLAYER_HOTBAR, List.of(36, 37),
                ChestLikeMenuLayout.SOURCE_PLAYER_ARMOR, List.of(5, 6, 7, 8),
                ChestLikeMenuLayout.SOURCE_PLAYER_OFFHAND, List.of(45)
        ), false);

        org.junit.jupiter.api.Assertions.assertEquals(0, layout.sourceSlotIndexForMenuSlot(ChestLikeMenuLayout.SOURCE_PLAYER_MAIN, 9));
        org.junit.jupiter.api.Assertions.assertEquals(2, layout.sourceSlotIndexForMenuSlot(ChestLikeMenuLayout.SOURCE_PLAYER_MAIN, 11));
        org.junit.jupiter.api.Assertions.assertEquals(0, layout.sourceSlotIndexForMenuSlot(ChestLikeMenuLayout.SOURCE_PLAYER_ARMOR, 5));
        org.junit.jupiter.api.Assertions.assertEquals(3, layout.sourceSlotIndexForMenuSlot(ChestLikeMenuLayout.SOURCE_PLAYER_ARMOR, 8));
        org.junit.jupiter.api.Assertions.assertEquals(0, layout.sourceSlotIndexForMenuSlot(ChestLikeMenuLayout.SOURCE_PLAYER_OFFHAND, 45));
        org.junit.jupiter.api.Assertions.assertEquals(11, layout.resolveMenuSlot(ChestLikeMenuLayout.SOURCE_PLAYER_MAIN, 2));
        org.junit.jupiter.api.Assertions.assertEquals(45, layout.resolveMenuSlot(ChestLikeMenuLayout.SOURCE_PLAYER_OFFHAND, 0));
    }

    @Test
    void armorInventorySlotsNormalizeToMenuCompatibleSourceOrder() {
        org.junit.jupiter.api.Assertions.assertEquals(3, ChestLikeMenuLayout.armorSourceSlotIndexForInventorySlot(0));
        org.junit.jupiter.api.Assertions.assertEquals(2, ChestLikeMenuLayout.armorSourceSlotIndexForInventorySlot(1));
        org.junit.jupiter.api.Assertions.assertEquals(1, ChestLikeMenuLayout.armorSourceSlotIndexForInventorySlot(2));
        org.junit.jupiter.api.Assertions.assertEquals(0, ChestLikeMenuLayout.armorSourceSlotIndexForInventorySlot(3));
    }

    @Test
    void playerInventorySlotsNormalizeToSourceRelativeMainAndHotbarIndices() {
        org.junit.jupiter.api.Assertions.assertEquals(0, ChestLikeMenuLayout.hotbarSourceSlotIndexForInventorySlot(0));
        org.junit.jupiter.api.Assertions.assertEquals(8, ChestLikeMenuLayout.hotbarSourceSlotIndexForInventorySlot(8));
        org.junit.jupiter.api.Assertions.assertEquals(0, ChestLikeMenuLayout.mainSourceSlotIndexForInventorySlot(9));
        org.junit.jupiter.api.Assertions.assertEquals(26, ChestLikeMenuLayout.mainSourceSlotIndexForInventorySlot(35));
        org.junit.jupiter.api.Assertions.assertEquals(-1, ChestLikeMenuLayout.mainSourceSlotIndexForInventorySlot(8));
        org.junit.jupiter.api.Assertions.assertEquals(-1, ChestLikeMenuLayout.hotbarSourceSlotIndexForInventorySlot(9));
    }

    private static ChestLikeMenuLayout layout(Map<String, List<Integer>> sourceMenuSlots, boolean primaryStorageIsCarried) {
        return new ChestLikeMenuLayout(
                2,
                List.of(),
                "Test",
                sourceMenuSlots,
                sourceIdsByMenuSlot(sourceMenuSlots),
                primaryStorageIsCarried,
                new TestStorageSession(primaryStorageIsCarried)
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

    private record TestStorageSession(boolean primaryStorageIsCarried) implements StorageViewProviderSession {
        @Override
        public String providerId() {
            return "test";
        }

        @Override
        public InventorySourceDescriptor primaryStorageSource() {
            return InventorySourceDescriptor.builder(primaryStorageIsCarried
                            ? ChestLikeMenuLayout.SOURCE_CARRIED_STORAGE
                            : ChestLikeMenuLayout.SOURCE_OPEN_CONTAINER)
                    .label(net.minecraft.network.chat.Component.literal("Test"))
                    .domain(InventorySourceDomain.HOST_STORAGE)
                    .role(InventorySourceRole.PRIMARY_STORAGE)
                    .slotCount(2)
                    .backingKind(InventorySourceBackingKind.MENU_BACKED)
                    .capabilities(java.util.Set.of(
                            InventorySourceCapability.INSERT,
                            InventorySourceCapability.EXTRACT
                    ))
                    .actionable(true)
                    .menuBacked(true)
                    .build();
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
