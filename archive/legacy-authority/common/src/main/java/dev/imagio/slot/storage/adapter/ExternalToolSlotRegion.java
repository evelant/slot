package dev.imagio.slot.storage.adapter;

import java.util.List;

public record ExternalToolSlotRegion(
        String id,
        ExternalToolSlotRole role,
        int columns,
        List<Integer> menuSlots
) {
    public ExternalToolSlotRegion {
        id = id == null ? "" : id;
        role = role == null ? ExternalToolSlotRole.PREVIEW : role;
        columns = Math.max(1, columns);
        menuSlots = menuSlots == null ? List.of() : List.copyOf(menuSlots);
    }

    public static ExternalToolSlotRegion grid(String id, ExternalToolSlotRole role, int columns, List<Integer> menuSlots) {
        return new ExternalToolSlotRegion(id, role, columns, menuSlots);
    }

    public static ExternalToolSlotRegion single(String id, ExternalToolSlotRole role, int menuSlot) {
        return new ExternalToolSlotRegion(id, role, 1, List.of(menuSlot));
    }

    public boolean containsMenuSlot(int menuSlot) {
        return menuSlots.contains(menuSlot);
    }
}
