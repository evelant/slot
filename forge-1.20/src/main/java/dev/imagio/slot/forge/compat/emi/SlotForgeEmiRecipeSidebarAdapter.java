package dev.imagio.slot.forge.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.forge.client.ForgeContainerSidebar;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.ui.workspace.RecipeIngredientSidebarSpec;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

final class SlotForgeEmiRecipeSidebarAdapter {
    private static final int MAX_INPUTS_PER_RECIPE = 128;
    private static final int MAX_ALTERNATIVES_PER_INPUT = 128;
    private static final String EMI_RECIPE_SCREEN = "dev.emi.emi.screen.RecipeScreen";
    private static boolean recipeScreenReflectionWarningLogged;

    private SlotForgeEmiRecipeSidebarAdapter() {
    }

    static ForgeContainerSidebar.SidebarHost sidebarHost(Screen screen) {
        if (!isEmiRecipeScreen(screen)) {
            return null;
        }
        Object oldScreen = fieldValue(screen, "old");
        if (oldScreen instanceof AbstractContainerScreen<?> containerScreen) {
            return new ForgeContainerSidebar.SidebarHost(screen, containerScreen);
        }
        return null;
    }

    static RecipeIngredientSidebarSpec recipeSidebarSpec(Screen screen) {
        List<EmiRecipe> recipes = visibleRecipes(screen);
        if (recipes.isEmpty()) {
            return RecipeIngredientSidebarSpec.empty();
        }
        ArrayList<RecipeIngredientSidebarSpec.Ingredient> ingredients = new ArrayList<>();
        StringBuilder sourceKey = new StringBuilder("emi:recipe-sidebar");
        for (int recipeIndex = 0; recipeIndex < recipes.size(); recipeIndex++) {
            EmiRecipe recipe = recipes.get(recipeIndex);
            if (recipe == null) {
                continue;
            }
            String recipeId = recipeId(recipe);
            sourceKey.append("|r:").append(recipeId);
            List<EmiIngredient> inputs = recipe.getInputs();
            if (inputs == null || inputs.isEmpty()) {
                continue;
            }
            for (int inputIndex = 0; inputIndex < inputs.size() && inputIndex < MAX_INPUTS_PER_RECIPE; inputIndex++) {
                EmiIngredient input = inputs.get(inputIndex);
                if (input == null || input.isEmpty()) {
                    continue;
                }
                RecipeIngredientSidebarSpec.Ingredient ingredient =
                        ingredientSpec(input, recipeId, recipeIndex, inputIndex);
                ingredients.add(ingredient);
                appendIngredientKey(sourceKey, ingredient);
            }
        }
        if (ingredients.isEmpty()) {
            return RecipeIngredientSidebarSpec.empty();
        }
        return new RecipeIngredientSidebarSpec(
                sourceKey.toString(),
                recipes.size() == 1 ? "EMI recipe ingredients" : "Visible EMI recipe ingredients",
                ingredients);
    }

    private static RecipeIngredientSidebarSpec.Ingredient ingredientSpec(
            EmiIngredient input,
            String recipeId,
            int recipeIndex,
            int inputIndex
    ) {
        int requiredCount = safeCount(input.getAmount());
        LinkedHashMap<String, RecipeIngredientSidebarSpec.Alternative> alternatives = new LinkedHashMap<>();
        String firstNonItemLabel = "";
        int inspected = 0;
        for (EmiStack emiStack : input.getEmiStacks()) {
            if (inspected++ >= MAX_ALTERNATIVES_PER_INPUT) {
                break;
            }
            if (emiStack == null || emiStack.isEmpty()) {
                continue;
            }
            ItemStack stack = itemStack(emiStack);
            if (stack == null || stack.isEmpty()) {
                if (firstNonItemLabel.isBlank()) {
                    firstNonItemLabel = nonItemAlternativeLabel(emiStack);
                }
                continue;
            }
            ItemIdentity identity = ItemIdentityMatcher.create(stack);
            String label = labelFor(emiStack, stack);
            ItemStack displayStack = stack.copy();
            displayStack.setCount(Math.min(displayStack.getMaxStackSize(), requiredCount));
            alternatives.putIfAbsent(
                    identity.itemId(),
                    new RecipeIngredientSidebarSpec.Alternative(identity, label, requiredCount, displayStack));
        }
        String label = alternatives.isEmpty()
                ? firstNonItemLabel.isBlank() ? "Ingredient " + (inputIndex + 1) : firstNonItemLabel
                : alternatives.values().iterator().next().label();
        return new RecipeIngredientSidebarSpec.Ingredient(
                sanitize(recipeId) + "/input_" + recipeIndex + "_" + inputIndex,
                label,
                requiredCount,
                List.copyOf(alternatives.values()));
    }

    private static List<EmiRecipe> visibleRecipes(Screen screen) {
        if (!isEmiRecipeScreen(screen)) {
            return List.of();
        }
        Object currentPage = fieldValue(screen, "currentPage");
        if (!(currentPage instanceof List<?> groups) || groups.isEmpty()) {
            return List.of();
        }
        ArrayList<EmiRecipe> recipes = new ArrayList<>();
        for (Object group : groups) {
            EmiRecipe recipe = emiRecipeField(group);
            if (recipe != null) {
                recipes.add(recipe);
            }
        }
        return List.copyOf(recipes);
    }

    private static boolean isEmiRecipeScreen(Screen screen) {
        return screen != null && EMI_RECIPE_SCREEN.equals(screen.getClass().getName());
    }

    private static EmiRecipe emiRecipeField(Object target) {
        Object recipe = fieldValue(target, "recipe");
        return recipe instanceof EmiRecipe emiRecipe ? emiRecipe : null;
    }

    private static Object fieldValue(Object target, String fieldName) {
        if (target == null) {
            return null;
        }
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (ReflectiveOperationException | RuntimeException error) {
            logRecipeScreenReflectionFailure(error);
            return null;
        }
    }

    private static void logRecipeScreenReflectionFailure(Throwable error) {
        if (recipeScreenReflectionWarningLogged) {
            return;
        }
        recipeScreenReflectionWarningLogged = true;
        SlotCommon.LOGGER.warn("[SLOT][emi] cannot inspect EMI recipe screen for Forge recipe sidebar", error);
    }

    private static void appendIngredientKey(StringBuilder sourceKey, RecipeIngredientSidebarSpec.Ingredient ingredient) {
        sourceKey.append("|i:")
                .append(ingredient.ingredientId())
                .append("x")
                .append(ingredient.requiredCount());
        for (RecipeIngredientSidebarSpec.Alternative alternative : ingredient.alternatives()) {
            sourceKey.append(":")
                    .append(alternative.identity() == null ? "opaque" : alternative.identity().itemId());
        }
    }

    private static ItemStack itemStack(EmiStack emiStack) {
        try {
            ItemStack stack = emiStack.getItemStack();
            return stack == null ? ItemStack.EMPTY : stack;
        } catch (RuntimeException | LinkageError ignored) {
            return ItemStack.EMPTY;
        }
    }

    private static String labelFor(EmiStack emiStack, ItemStack fallback) {
        try {
            Component name = emiStack.getName();
            if (name != null && !name.getString().isBlank()) {
                return name.getString();
            }
        } catch (RuntimeException | LinkageError ignored) {
        }
        return fallback == null || fallback.isEmpty() ? "Ingredient" : fallback.getHoverName().getString();
    }

    private static String nonItemAlternativeLabel(EmiStack emiStack) {
        if (emiStack == null || emiStack.isEmpty()) {
            return "";
        }
        try {
            Component name = emiStack.getName();
            if (name != null && !name.getString().isBlank()) {
                return name.getString();
            }
        } catch (RuntimeException | LinkageError ignored) {
        }
        try {
            ResourceLocation id = emiStack.getId();
            if (id != null) {
                return id.toString();
            }
        } catch (RuntimeException | LinkageError ignored) {
        }
        try {
            Object key = emiStack.getKey();
            return key == null ? "" : key.toString();
        } catch (RuntimeException | LinkageError ignored) {
            return "";
        }
    }

    private static String recipeId(EmiRecipe recipe) {
        ResourceLocation id = recipe == null ? null : recipe.getId();
        if (id != null) {
            return id.toString();
        }
        return "slot:anonymous_emi_recipe/" + Integer.toHexString(System.identityHashCode(recipe));
    }

    private static int safeCount(long amount) {
        if (amount <= 0) {
            return 1;
        }
        return amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
    }

    private static String sanitize(String value) {
        String input = value == null || value.isBlank() ? "ingredient" : value;
        StringBuilder builder = new StringBuilder(input.length());
        for (int index = 0; index < input.length(); index++) {
            char ch = Character.toLowerCase(input.charAt(index));
            if ((ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9')
                    || ch == '_' || ch == '-' || ch == '.' || ch == '/') {
                builder.append(ch);
            } else {
                builder.append('_');
            }
        }
        return builder.length() == 0 ? "ingredient" : builder.toString();
    }
}
