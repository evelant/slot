package dev.imagio.slot.storage.provider;

import net.minecraft.world.item.ItemStack;

public record MutationResult(
        boolean successful,
        ItemStack stackRemainder,
        String diagnostics
) {
    public MutationResult {
        stackRemainder = stackRemainder == null ? ItemStack.EMPTY : stackRemainder;
        diagnostics = diagnostics == null ? "" : diagnostics;
    }

    public static MutationResult success(ItemStack stackRemainder) {
        return new MutationResult(true, stackRemainder, "");
    }

    public static MutationResult blocked(String diagnostics, ItemStack stackRemainder) {
        return new MutationResult(false, stackRemainder, diagnostics);
    }
}
