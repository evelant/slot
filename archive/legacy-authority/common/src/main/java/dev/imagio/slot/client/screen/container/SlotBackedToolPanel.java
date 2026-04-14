package dev.imagio.slot.client.screen.container;

import dev.imagio.slot.storage.adapter.ExternalToolSlotRegion;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public interface SlotBackedToolPanel extends DockedToolPanel {
    int slotAt(double mouseX, double mouseY);

    Slot menuSlot(int slotId);

    default MenuSlotId logicalMenuSlotId(int slotId) {
        return ToolSlotMapping.logicalMenuSlotId(slotId);
    }

    default int menuSlotId(int slotId) {
        return logicalMenuSlotId(slotId).value();
    }

    ExternalToolSlotRegion regionForSlot(int slotId);

    default ToolSlotRef slotRef(int slotId) {
        if (slotId < 0) {
            return ToolSlotRef.unresolved(slotId);
        }
        return new ToolSlotRef(slotId, logicalMenuSlotId(slotId), menuSlot(slotId), regionForSlot(slotId));
    }

    default ToolSlotRef slotRefAt(double mouseX, double mouseY) {
        return slotRef(slotAt(mouseX, mouseY));
    }

    default boolean acceptsPlacement(int slotId, ItemStack stack) {
        Slot slot = menuSlot(slotId);
        return slot != null && !stack.isEmpty() && slot.mayPlace(stack);
    }
}
