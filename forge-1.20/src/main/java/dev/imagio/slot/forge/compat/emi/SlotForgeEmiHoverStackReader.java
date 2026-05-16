package dev.imagio.slot.forge.compat.emi;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import net.minecraft.world.item.ItemStack;

public final class SlotForgeEmiHoverStackReader {
    private SlotForgeEmiHoverStackReader() {
    }

    public static boolean isSearchFocused() {
        return EmiApi.isSearchFocused();
    }

    public static ItemStack hoveredItemStack() {
        EmiStackInteraction interaction = EmiApi.getHoveredStack(true);
        if (interaction == null || interaction.isEmpty()) {
            return ItemStack.EMPTY;
        }
        EmiIngredient ingredient = interaction.getStack();
        if (ingredient == null || ingredient.isEmpty()) {
            return ItemStack.EMPTY;
        }
        for (EmiStack candidate : ingredient.getEmiStacks()) {
            if (candidate == null || candidate.isEmpty()) {
                continue;
            }
            ItemStack stack = candidate.getItemStack();
            if (stack != null && !stack.isEmpty()) {
                return stack.copy();
            }
        }
        return ItemStack.EMPTY;
    }
}
