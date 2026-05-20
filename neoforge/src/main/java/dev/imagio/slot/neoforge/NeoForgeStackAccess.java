package dev.imagio.slot.neoforge;

import dev.imagio.slot.platform.SlotStackAccess;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

public final class NeoForgeStackAccess implements SlotStackAccess.StackAccess {
    @Override
    public String itemId(ItemStack stack) {
        if (stack == null) {
            return "";
        }
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    @Override
    public boolean stackable(ItemStack stack) {
        return stack != null && stack.isStackable();
    }

    @Override
    public String dataFingerprint(ItemStack stack) {
        if (stack == null) {
            return "";
        }
        DataComponentPatch patch = stack.getComponentsPatch();
        if (patch == null || patch.isEmpty()) {
            return "";
        }
        String fingerprint = patch.toString();
        return "{}".equals(fingerprint) ? "" : fingerprint;
    }

    @Override
    public boolean damageable(ItemStack stack) {
        return stack != null && (stack.isDamageableItem() || stack.getMaxDamage() > 0);
    }

    @Override
    public boolean sameItemAndData(ItemStack first, ItemStack second) {
        if (first == second) {
            return true;
        }
        if (first == null || second == null) {
            return false;
        }
        return ItemStack.isSameItemSameComponents(first, second);
    }
}
