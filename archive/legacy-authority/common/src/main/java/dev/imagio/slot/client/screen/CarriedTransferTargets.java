package dev.imagio.slot.client.screen;

import dev.imagio.slot.inventory.kernel.ActionableSourcePolicy;
import dev.imagio.slot.inventory.kernel.MenuSlotResolver;
import dev.imagio.slot.session.ChestLikeMenuLayout;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

final class CarriedTransferTargets {
    private CarriedTransferTargets() {
    }

    static int insertionCapacity(Slot slot, ItemStack sourceStack) {
        return MenuSlotResolver.insertionCapacity(slot, sourceStack);
    }

    static List<Integer> vanillaTargetSlotsFor(String sourceId) {
        return vanillaTargetSlotsFor(null, sourceId);
    }

    static List<Integer> vanillaTargetSlotsFor(AbstractContainerMenu menu, String sourceId) {
        return new ActionableSourcePolicy(new MenuSlotResolver(menu, null)).playerTransferTargets(sourceId);
    }
}
