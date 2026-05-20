package dev.imagio.slot.forge.probe;

import dev.imagio.slot.platform.SlotStackAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

@SuppressWarnings("deprecation")
final class Forge120StackAccess implements SlotStackAccess.StackAccess {
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
        CompoundTag tag = stack.getTag();
        if (tag == null || tag.isEmpty()) {
            return "";
        }
        String fingerprint = tag.toString();
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
        return ItemStack.isSameItemSameTags(first, second);
    }
}
