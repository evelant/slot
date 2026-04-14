package net.minecraft.world.item;

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
        if (immutableEmpty) {
            return;
        }
        this.count = Math.max(0, count);
    }

    public int getCount() {
        return count;
    }

    public void grow(int count) {
        if (immutableEmpty) {
            return;
        }
        this.count += count;
    }

    public void shrink(int count) {
        if (immutableEmpty) {
            return;
        }
        this.count = Math.max(0, this.count - count);
    }

    public boolean isEmpty() {
        return count <= 0 || itemId.isBlank();
    }

    public int getMaxStackSize() {
        return maxStackSize;
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

    public static boolean matches(ItemStack first, ItemStack second) {
        if (first == second) {
            return true;
        }
        if (first == null || second == null) {
            return false;
        }
        if (first.isEmpty() && second.isEmpty()) {
            return true;
        }
        return isSameItemSameComponents(first, second)
                && first.count == second.count;
    }
}
