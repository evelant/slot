package dev.imagio.slot.forge.compat.emi;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import net.minecraft.world.item.ItemStack;

import java.util.List;

final class SlotForgeEmiRecipeViewerAdapter {
    private SlotForgeEmiRecipeViewerAdapter() {
    }

    static boolean openRecipe(ItemIdentity identity) {
        ItemStack stack = SlotWorkspaceViewModel.displayStackForIdentity(identity);
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        EmiStack emiStack = EmiStack.of(stack);
        List<EmiRecipe> recipes = EmiApi.getRecipeManager().getRecipesByOutput(emiStack);
        if (recipes.isEmpty()) {
            return false;
        }
        EmiApi.displayRecipes(emiStack);
        return true;
    }

    static boolean openUses(ItemIdentity identity) {
        ItemStack stack = SlotWorkspaceViewModel.displayStackForIdentity(identity);
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        EmiApi.displayUses(EmiStack.of(stack));
        return true;
    }
}
