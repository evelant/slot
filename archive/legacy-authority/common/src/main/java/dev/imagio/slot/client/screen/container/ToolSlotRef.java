package dev.imagio.slot.client.screen.container;

import dev.imagio.slot.storage.adapter.ExternalToolSlotRegion;
import dev.imagio.slot.storage.adapter.ExternalToolSlotRole;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public record ToolSlotRef(
        int displaySlotId,
        MenuSlotId menuSlotId,
        Slot slot,
        ExternalToolSlotRegion region
) {
    public static ToolSlotRef unresolved(int displaySlotId) {
        return new ToolSlotRef(displaySlotId, MenuSlotId.INVALID, null, null);
    }

    public boolean isResolved() {
        return displaySlotId >= 0 && menuSlotId != null && menuSlotId.isValid() && slot != null;
    }

    public boolean hasRole(ExternalToolSlotRole role) {
        return region != null && region.role() == role;
    }

    public ItemStack stack() {
        return slot == null ? ItemStack.EMPTY : slot.getItem();
    }
}
