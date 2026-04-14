package dev.imagio.slot.inventory.core;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MenuBackedPlayerTopologyResolver {
    private MenuBackedPlayerTopologyResolver() {
    }

    public static InventoryTopologyDescriptor resolve(AbstractContainerMenu menu, Inventory playerInventory) {
        if (menu == null || playerInventory == null) {
            return InventoryTopologyDescriptor.empty();
        }

        Map<String, List<Integer>> menuSlotsBySource = new LinkedHashMap<>();
        menuSlotsBySource.put(BuiltinInventoryIds.PLAYER_MAIN, new ArrayList<>());
        menuSlotsBySource.put(BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0, new ArrayList<>());
        menuSlotsBySource.put(BuiltinInventoryIds.PLAYER_ARMOR, new ArrayList<>());
        menuSlotsBySource.put(BuiltinInventoryIds.PLAYER_OFFHAND, new ArrayList<>());

        Map<Integer, String> sourceByMenuSlot = new LinkedHashMap<>();
        for (int menuSlot = 0; menuSlot < menu.slots.size(); menuSlot++) {
            Slot slot = menu.getSlot(menuSlot);
            if (slot.container != playerInventory) {
                continue;
            }

            int playerSlot = slot.getContainerSlot();
            String sourceId = sourceIdForPlayerSlot(playerSlot);
            if (sourceId == null) {
                continue;
            }
            menuSlotsBySource.computeIfAbsent(sourceId, ignored -> new ArrayList<>()).add(menuSlot);
            sourceByMenuSlot.put(menuSlot, sourceId);
        }

        return new InventoryTopologyDescriptor(copy(menuSlotsBySource), sourceByMenuSlot, Map.of());
    }

    public static int logicalSlotIndex(String sourceId, int playerSlot) {
        if (BuiltinInventoryIds.PLAYER_MAIN.equals(sourceId)) {
            return playerSlot >= 9 && playerSlot < 36 ? playerSlot - 9 : -1;
        }
        if (BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0.equals(sourceId)) {
            return playerSlot >= 0 && playerSlot < 9 ? playerSlot : -1;
        }
        if (BuiltinInventoryIds.PLAYER_ARMOR.equals(sourceId)) {
            return playerSlot >= 36 && playerSlot < 40 ? 39 - playerSlot : -1;
        }
        if (BuiltinInventoryIds.PLAYER_OFFHAND.equals(sourceId)) {
            return playerSlot == 40 ? 0 : -1;
        }
        return -1;
    }

    private static String sourceIdForPlayerSlot(int playerSlot) {
        if (playerSlot >= 0 && playerSlot < 9) {
            return BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0;
        }
        if (playerSlot >= 9 && playerSlot < 36) {
            return BuiltinInventoryIds.PLAYER_MAIN;
        }
        if (playerSlot >= 36 && playerSlot < 40) {
            return BuiltinInventoryIds.PLAYER_ARMOR;
        }
        if (playerSlot == 40) {
            return BuiltinInventoryIds.PLAYER_OFFHAND;
        }
        return null;
    }

    private static Map<String, List<Integer>> copy(Map<String, List<Integer>> source) {
        LinkedHashMap<String, List<Integer>> copy = new LinkedHashMap<>();
        source.forEach((key, value) -> copy.put(key, value == null ? List.of() : List.copyOf(value)));
        return Map.copyOf(copy);
    }
}
