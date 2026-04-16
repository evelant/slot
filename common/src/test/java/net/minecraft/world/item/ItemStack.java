package net.minecraft.world.item;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

public class ItemStack {
    public static final ItemStack EMPTY = new ItemStack("", "", 0, 64, true);

    private final String itemId;
    private final String componentFingerprint;
    private final int maxStackSize;
    private final boolean immutableEmpty;
    private int count;

    public ItemStack() {
        this("", "", 0, 64, false);
    }

    public ItemStack(String itemId, int count, int maxStackSize) {
        this(itemId, "", count, maxStackSize, false);
    }

    public ItemStack(String itemId, String componentFingerprint, int count, int maxStackSize) {
        this(itemId, componentFingerprint, count, maxStackSize, false);
    }

    private ItemStack(String itemId, String componentFingerprint, int count, int maxStackSize, boolean immutableEmpty) {
        this.itemId = itemId == null ? "" : itemId;
        this.componentFingerprint = componentFingerprint == null ? "" : componentFingerprint;
        this.count = Math.max(0, count);
        this.maxStackSize = Math.max(0, maxStackSize);
        this.immutableEmpty = immutableEmpty;
    }

    public ItemStack copy() {
        return isEmpty() ? EMPTY : new ItemStack(itemId, componentFingerprint, count, maxStackSize);
    }

    public void setCount(int count) {
        if (!immutableEmpty) {
            this.count = Math.max(0, count);
        }
    }

    public int getCount() {
        return count;
    }

    public void grow(int amount) {
        if (!immutableEmpty) {
            this.count = Math.max(0, this.count + Math.max(0, amount));
        }
    }

    public void shrink(int amount) {
        if (!immutableEmpty) {
            this.count = Math.max(0, this.count - Math.max(0, amount));
        }
    }

    public boolean isEmpty() {
        return count <= 0 || itemId.isBlank();
    }

    public int getMaxStackSize() {
        return maxStackSize;
    }

    public String itemId() {
        return itemId;
    }

    public String componentFingerprint() {
        return componentFingerprint;
    }

    public Component getHoverName() {
        return Component.literal(itemId);
    }

    public Tag saveOptional(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putString("itemId", itemId);
        tag.putString("componentFingerprint", componentFingerprint);
        tag.putInt("count", count);
        tag.putInt("maxStackSize", maxStackSize);
        return tag;
    }

    public static ItemStack parseOptional(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag == null) {
            return EMPTY;
        }
        return new ItemStack(
                tag.getString("itemId"),
                tag.getString("componentFingerprint"),
                tag.getInt("count"),
                tag.getInt("maxStackSize")
        );
    }

    public static boolean isSameItemSameComponents(ItemStack first, ItemStack second) {
        if (first == second) {
            return true;
        }
        if (first == null || second == null) {
            return false;
        }
        return first.itemId.equals(second.itemId)
                && first.componentFingerprint.equals(second.componentFingerprint);
    }
}
