package dev.imagio.slot.neoforge.compat.emi;

import com.google.gson.JsonElement;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.goal.GoalDescriptor;
import dev.imagio.slot.inventory.goal.GoalIngredientDescriptor;
import dev.imagio.slot.inventory.goal.GoalRecipeDescriptor;
import dev.imagio.slot.inventory.goal.GoalStackDescriptor;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.ui.workspace.GoalWorkspaceClientState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

final class SlotEmiGoalAdapter {
    private static final int MAX_RECIPE_DEPTH = 4;
    private static final int MAX_RECIPES = 96;
    private static final int MAX_ALTERNATIVES = 32;
    private static final String EMI_RECIPE_SCREEN = "dev.emi.emi.screen.RecipeScreen";
    private static final int GOAL_BUTTON_WIDTH = 50;
    private static final int GOAL_BUTTON_HEIGHT = 14;
    private static boolean recipeScreenReflectionWarningLogged;

    private SlotEmiGoalAdapter() {
    }

    static void decorateRecipe(EmiRecipe recipe, WidgetHolder widgets, Runnable openWorkspace) {
        if (recipe == null || widgets == null) {
            return;
        }
        int x = Math.max(0, widgets.getWidth() - 14);
        int y = Math.max(0, widgets.getHeight() - 14);
        widgets.addButton(x, y, 12, 12, 0, 0, () -> true, (mouseX, mouseY, button) -> {
            if (button != 0) {
                return;
            }
            createGoalFromRecipe(recipe, openWorkspace);
        });
        widgets.addText(Component.literal("S"), x + 3, y + 2, 0xFFFFFFFF, false);
        widgets.addTooltipText(List.of(Component.literal("Add SLOT goal")), x, y, 12, 12);
    }

    static void renderRecipeGoalButtons(Screen screen, GuiGraphics graphics, int mouseX, int mouseY) {
        for (RecipeGoalButton button : recipeGoalButtons(screen)) {
            renderRecipeGoalButton(graphics, button, button.contains(mouseX, mouseY));
        }
    }

    static boolean handleRecipeGoalButtonClick(Screen screen, double mouseX, double mouseY, int mouseButton, Runnable openWorkspace) {
        if (mouseButton != 0) {
            return false;
        }
        List<RecipeGoalButton> buttons = recipeGoalButtons(screen);
        for (int index = buttons.size() - 1; index >= 0; index--) {
            RecipeGoalButton button = buttons.get(index);
            if (button.contains(mouseX, mouseY)) {
                createGoalFromRecipe(button.recipe(), openWorkspace);
                return true;
            }
        }
        return false;
    }

    static GoalDescriptor goalFromRecipe(EmiRecipe recipe) {
        if (recipe == null) {
            return null;
        }
        LinkedHashMap<String, GoalRecipeDescriptor> recipes = new LinkedHashMap<>();
        collectRecipe(recipe, recipes, new LinkedHashSet<>(), 0);
        GoalRecipeDescriptor focused = recipes.get(recipeId(recipe));
        if (focused == null) {
            focused = describeRecipe(recipe, recipeId(recipe));
        }
        GoalStackDescriptor target = firstOutput(focused);
        if (target == null) {
            return null;
        }
        int targetCount = Math.max(1, target.count());
        return new GoalDescriptor(
                "emi:" + focused.recipeId(),
                target.displayName(),
                List.of(target.withCount(targetCount)),
                targetCount,
                focused.recipeId(),
                focused.categoryId(),
                List.copyOf(recipes.values())
        );
    }

    static boolean createGoalFromRecipe(EmiRecipe recipe, Runnable openWorkspace) {
        GoalDescriptor goal = goalFromRecipe(recipe);
        if (goal == null) {
            showStatus("EMI recipe cannot be converted to a SLOT goal");
            SlotCommon.LOGGER.warn("[SLOT][emi] cannot create goal for recipe {}", recipeId(recipe));
            return false;
        }
        GoalWorkspaceClientState.addOrActivate(goal);
        showStatus("Added SLOT goal: " + goal.label());
        if (openWorkspace != null) {
            openWorkspace.run();
        }
        return true;
    }

    static boolean createGoalFromIngredient(EmiIngredient ingredient, Runnable openWorkspace) {
        EmiRecipe recipe = recipeForIngredient(ingredient);
        if (recipe == null) {
            showStatus("No EMI recipe found for SLOT goal");
            return false;
        }
        return createGoalFromRecipe(recipe, openWorkspace);
    }

    static boolean openRecipe(GoalDescriptor goal) {
        if (goal == null || goal.focusedRecipeId().isBlank()) {
            return false;
        }
        ResourceLocation id = ResourceLocation.tryParse(goal.focusedRecipeId());
        if (id == null) {
            return false;
        }
        EmiRecipe recipe = EmiApi.getRecipeManager().getRecipe(id);
        if (recipe == null) {
            return false;
        }
        EmiApi.displayRecipe(recipe);
        return true;
    }

    static boolean openUses(ItemIdentity identity) {
        if (identity == null) {
            return false;
        }
        ItemStack stack = SlotWorkspaceViewModel.displayStackForIdentity(identity);
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        EmiApi.displayUses(EmiStack.of(stack));
        return true;
    }

    static boolean openChoiceEditor(GoalDescriptor goal, String choiceGroupId) {
        return openRecipe(goal);
    }

    private static EmiRecipe recipeForIngredient(EmiIngredient ingredient) {
        if (ingredient == null || ingredient.isEmpty()) {
            return null;
        }
        EmiRecipe contextual = EmiApi.getRecipeContext(ingredient);
        if (contextual != null) {
            return contextual;
        }
        for (EmiStack stack : ingredient.getEmiStacks()) {
            ItemStack itemStack = stack.getItemStack();
            if (itemStack == null || itemStack.isEmpty()) {
                continue;
            }
            List<EmiRecipe> recipes = EmiApi.getRecipeManager().getRecipesByOutput(stack);
            if (!recipes.isEmpty()) {
                return recipes.get(0);
            }
        }
        return null;
    }

    private static void collectRecipe(
            EmiRecipe recipe,
            LinkedHashMap<String, GoalRecipeDescriptor> recipes,
            Set<String> visiting,
            int depth
    ) {
        if (recipe == null || recipes.size() >= MAX_RECIPES) {
            return;
        }
        String id = recipeId(recipe);
        if (recipes.containsKey(id) || visiting.contains(id)) {
            return;
        }
        visiting.add(id);
        recipes.put(id, describeRecipe(recipe, id));
        if (depth >= MAX_RECIPE_DEPTH) {
            visiting.remove(id);
            return;
        }
        for (EmiIngredient ingredient : recipe.getInputs()) {
            for (EmiStack alternative : ingredient.getEmiStacks()) {
                if (recipes.size() >= MAX_RECIPES) {
                    visiting.remove(id);
                    return;
                }
                ItemStack stack = alternative.getItemStack();
                if (stack == null || stack.isEmpty()) {
                    continue;
                }
                for (EmiRecipe child : EmiApi.getRecipeManager().getRecipesByOutput(alternative)) {
                    collectRecipe(child, recipes, visiting, depth + 1);
                }
            }
        }
        visiting.remove(id);
    }

    private static GoalRecipeDescriptor describeRecipe(EmiRecipe recipe, String recipeId) {
        ArrayList<String> diagnostics = new ArrayList<>();
        List<GoalStackDescriptor> outputs = outputStacks(recipe);
        List<GoalIngredientDescriptor> inputs = ingredients(recipe.getInputs(), recipeId, "input");
        List<GoalIngredientDescriptor> catalysts = ingredients(recipe.getCatalysts(), recipeId, "catalyst");
        if (outputs.isEmpty()) {
            diagnostics.add("recipe_has_no_item_output");
        }
        if (recipe.getOutputs().size() > outputs.size()) {
            diagnostics.add("recipe_has_non_item_output");
        }
        return new GoalRecipeDescriptor(
                recipeId,
                categoryId(recipe),
                recipe.supportsRecipeTree(),
                outputs,
                inputs,
                catalysts,
                diagnostics
        );
    }

    private static List<GoalStackDescriptor> outputStacks(EmiRecipe recipe) {
        LinkedHashMap<ItemIdentity, GoalStackDescriptor> stacks = new LinkedHashMap<>();
        for (EmiStack output : recipe.getOutputs()) {
            GoalStackDescriptor descriptor = stackDescriptor(output);
            if (descriptor == null) {
                continue;
            }
            stacks.putIfAbsent(descriptor.identity(), descriptor);
        }
        return List.copyOf(stacks.values());
    }

    private static List<GoalIngredientDescriptor> ingredients(
            List<EmiIngredient> ingredients,
            String recipeId,
            String prefix
    ) {
        if (ingredients == null || ingredients.isEmpty()) {
            return List.of();
        }
        ArrayList<GoalIngredientDescriptor> descriptors = new ArrayList<>(ingredients.size());
        for (int index = 0; index < ingredients.size(); index++) {
            descriptors.add(ingredientDescriptor(ingredients.get(index), recipeId, prefix, index));
        }
        return List.copyOf(descriptors);
    }

    private static GoalIngredientDescriptor ingredientDescriptor(
            EmiIngredient ingredient,
            String recipeId,
            String prefix,
            int index
    ) {
        ArrayList<String> diagnostics = new ArrayList<>();
        LinkedHashMap<ItemIdentity, GoalStackDescriptor> alternatives = new LinkedHashMap<>();
        for (EmiStack stack : ingredient.getEmiStacks()) {
            if (alternatives.size() >= MAX_ALTERNATIVES) {
                diagnostics.add("ingredient_alternatives_truncated");
                break;
            }
            GoalStackDescriptor descriptor = stackDescriptor(stack);
            if (descriptor == null) {
                diagnostics.add("ingredient_has_non_item_alternative");
                continue;
            }
            alternatives.putIfAbsent(descriptor.identity(), descriptor);
        }
        String serialized = serializedIngredient(ingredient);
        String tagOrList = tagOrListLabel(serialized);
        if (ingredient.getChance() != 1f) {
            diagnostics.add("ingredient_has_chance");
        }
        String label = !tagOrList.isBlank()
                ? tagOrList
                : alternatives.isEmpty()
                ? prefix + " " + (index + 1)
                : alternatives.values().iterator().next().displayName();
        return new GoalIngredientDescriptor(
                prefix + "_" + index,
                label,
                safeCount(ingredient.getAmount()),
                ingredient.getChance(),
                serialized,
                List.copyOf(alternatives.values()),
                alternatives.size() > 1 || !tagOrList.isBlank(),
                tagOrList,
                diagnostics
        );
    }

    private static GoalStackDescriptor stackDescriptor(EmiStack emiStack) {
        if (emiStack == null || emiStack.isEmpty()) {
            return null;
        }
        ItemStack stack = emiStack.getItemStack();
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        ItemIdentity identity = ItemIdentityMatcher.create(stack);
        Component name = emiStack.getName();
        return new GoalStackDescriptor(
                identity,
                name == null ? stack.getHoverName().getString() : name.getString(),
                safeCount(emiStack.getAmount())
        );
    }

    private static GoalStackDescriptor firstOutput(GoalRecipeDescriptor recipe) {
        return recipe == null || recipe.outputs().isEmpty() ? null : recipe.outputs().get(0);
    }

    private static List<RecipeGoalButton> recipeGoalButtons(Screen screen) {
        if (!isEmiRecipeScreen(screen)) {
            return List.of();
        }
        Object currentPage = fieldValue(screen, "currentPage");
        if (!(currentPage instanceof List<?> groups) || groups.isEmpty()) {
            return List.of();
        }
        ArrayList<RecipeGoalButton> buttons = new ArrayList<>();
        for (Object group : groups) {
            EmiRecipe recipe = emiRecipeField(group);
            Integer groupX = intField(group, "x");
            Integer groupY = intField(group, "y");
            Integer groupWidth = intField(group, "width");
            if (recipe == null || groupX == null || groupY == null || groupWidth == null) {
                continue;
            }
            int x = groupX + Math.max(0, groupWidth - GOAL_BUTTON_WIDTH);
            x = Math.max(4, Math.min(screen.width - GOAL_BUTTON_WIDTH - 4, x));
            int y = Math.max(4, groupY - GOAL_BUTTON_HEIGHT - 2);
            buttons.add(new RecipeGoalButton(recipe, x, y, GOAL_BUTTON_WIDTH, GOAL_BUTTON_HEIGHT));
        }
        return List.copyOf(buttons);
    }

    private static boolean isEmiRecipeScreen(Screen screen) {
        return screen != null && EMI_RECIPE_SCREEN.equals(screen.getClass().getName());
    }

    private static EmiRecipe emiRecipeField(Object target) {
        Object recipe = fieldValue(target, "recipe");
        return recipe instanceof EmiRecipe emiRecipe ? emiRecipe : null;
    }

    private static Integer intField(Object target, String fieldName) {
        Object value = fieldValue(target, fieldName);
        return value instanceof Number number ? number.intValue() : null;
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
        SlotCommon.LOGGER.warn("[SLOT][emi] cannot inspect EMI recipe screen for SLOT goal button", error);
    }

    private static void renderRecipeGoalButton(GuiGraphics graphics, RecipeGoalButton button, boolean hovered) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.font == null) {
            return;
        }
        int x = button.x();
        int y = button.y();
        int right = x + button.width();
        int bottom = y + button.height();
        graphics.fill(x, y, right, bottom, hovered ? 0xF02FB56D : 0xD0228154);
        graphics.fill(x, y, right, y + 1, 0xFFFFFFFF);
        graphics.fill(x, bottom - 1, right, bottom, 0xFF0F5135);
        graphics.fill(x, y, x + 1, bottom, 0xFFFFFFFF);
        graphics.fill(right - 1, y, right, bottom, 0xFF0F5135);
        graphics.drawString(minecraft.font, "SLOT+", x + 7, y + 3, 0xFFFFFFFF, true);
    }

    private static String recipeId(EmiRecipe recipe) {
        ResourceLocation id = recipe == null ? null : recipe.getId();
        if (id != null) {
            return id.toString();
        }
        return "slot:anonymous_emi_recipe/" + Integer.toHexString(System.identityHashCode(recipe));
    }

    private static String categoryId(EmiRecipe recipe) {
        if (recipe == null || recipe.getCategory() == null || recipe.getCategory().getId() == null) {
            return "";
        }
        return recipe.getCategory().getId().toString();
    }

    private static String serializedIngredient(EmiIngredient ingredient) {
        JsonElement serialized = EmiIngredientSerializer.getSerialized(ingredient);
        return serialized == null ? "" : serialized.toString();
    }

    private static String tagOrListLabel(String serialized) {
        if (serialized == null || serialized.isBlank()) {
            return "";
        }
        String value = serialized.trim();
        if (value.startsWith("\"#") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        if (value.startsWith("[") || value.contains("\"ingredients\"")) {
            return "list";
        }
        int idIndex = value.indexOf("\"id\"");
        if ((value.contains("\"tag\"") || value.contains("\"type\":\"tag\"")) && idIndex >= 0) {
            int colon = value.indexOf(':', idIndex);
            int firstQuote = value.indexOf('"', colon + 1);
            int secondQuote = value.indexOf('"', firstQuote + 1);
            if (colon >= 0 && firstQuote >= 0 && secondQuote > firstQuote) {
                return "#" + value.substring(firstQuote + 1, secondQuote);
            }
        }
        return "";
    }

    private static int safeCount(long amount) {
        if (amount <= 0) {
            return 1;
        }
        return amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
    }

    private static void showStatus(String message) {
        net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.displayClientMessage(Component.literal(message), true);
        }
    }

    private record RecipeGoalButton(EmiRecipe recipe, int x, int y, int width, int height) {
        boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
        }
    }
}
