package dev.imagio.slot.forge.compat.ae2;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;

final class Ae2CellSlotMapping {
    private Ae2CellSlotMapping() {
    }

    static List<CellSlot> cellSlots(int cellCount, int inventorySize, IntPredicate handledCellSlot) {
        int cells = Math.max(0, cellCount);
        int slots = Math.max(0, inventorySize);
        if (cells == 0 || slots == 0 || handledCellSlot == null) {
            return List.of();
        }
        if (slots == cells) {
            ArrayList<CellSlot> out = new ArrayList<>();
            for (int slot = 0; slot < cells; slot++) {
                if (handledCellSlot.test(slot)) {
                    out.add(new CellSlot(slot, slot));
                }
            }
            return out.isEmpty() ? List.of() : List.copyOf(out);
        }
        ArrayList<CellSlot> out = new ArrayList<>();
        int cellIndex = 0;
        for (int inventorySlot = 0; inventorySlot < slots && cellIndex < cells; inventorySlot++) {
            if (!handledCellSlot.test(inventorySlot)) {
                continue;
            }
            out.add(new CellSlot(inventorySlot, cellIndex));
            cellIndex++;
        }
        return out.isEmpty() ? List.of() : List.copyOf(out);
    }

    record CellSlot(int inventorySlot, int cellIndex) {
        CellSlot {
            inventorySlot = Math.max(0, inventorySlot);
            cellIndex = Math.max(0, cellIndex);
        }
    }
}
