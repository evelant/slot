package dev.imagio.slot.client.screen;

import dev.imagio.slot.inventory.kernel.MenuSlotResolver;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

final class LocalTransferOutcomeSupport {
    private LocalTransferOutcomeSupport() {
    }

    static boolean movedAny(
            AbstractContainerMenu menu,
            int sourceMenuSlot,
            int targetMenuSlot,
            ItemStack sourceBefore,
            ItemStack targetBefore
    ) {
        if (sourceBefore == null || targetBefore == null) {
            return false;
        }

        var resolver = new MenuSlotResolver(menu, null);
        var sourceSlot = resolver.safeSlot(sourceMenuSlot);
        var targetSlot = resolver.safeSlot(targetMenuSlot);
        if (sourceSlot == null || targetSlot == null) {
            return false;
        }

        return !ItemStack.matches(sourceBefore, sourceSlot.getItem())
                || !ItemStack.matches(targetBefore, targetSlot.getItem());
    }
}
