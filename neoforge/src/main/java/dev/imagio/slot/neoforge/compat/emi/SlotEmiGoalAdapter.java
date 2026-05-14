package dev.imagio.slot.neoforge.compat.emi;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.goal.GoalChoiceKeys;
import dev.imagio.slot.inventory.goal.GoalDescriptor;
import dev.imagio.slot.inventory.goal.GoalIngredientDescriptor;
import dev.imagio.slot.inventory.goal.GoalProjectionEntry;
import dev.imagio.slot.inventory.goal.GoalProjectionEntryKind;
import dev.imagio.slot.inventory.goal.GoalRecipeDescriptor;
import dev.imagio.slot.inventory.goal.GoalStackDescriptor;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.neoforge.network.SlotGoalPlanPayload;
import dev.imagio.slot.neoforge.network.SlotGoalRecipeDefaultPayload;
import dev.imagio.slot.ui.workspace.GoalWorkspaceClientState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
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
            handleRecipeButtonAction(recipe, openWorkspace);
        });
        widgets.addText(Component.literal(GoalWorkspaceClientState.pendingRecipeChoice() == null ? "S" : "U"),
                x + 3,
                y + 2,
                0xFFFFFFFF,
                false);
        widgets.addTooltipText(List.of(Component.literal(recipeButtonTooltip())), x, y, 12, 12);
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
                handleRecipeButtonAction(button.recipe(), openWorkspace);
                return true;
            }
        }
        return false;
    }

    private static boolean handleRecipeButtonAction(EmiRecipe recipe, Runnable openWorkspace) {
        if (GoalWorkspaceClientState.pendingRecipeChoice() != null) {
            return applyPendingRecipeChoice(recipe, openWorkspace);
        }
        return createGoalFromRecipe(recipe, openWorkspace);
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
        GoalWorkspaceClientState.GoalTab tab = GoalWorkspaceClientState.addOrActivate(goal);
        persistGoal(tab);
        SlotDebugLog.log(
                "[emi][goal] captured recipe goal goal={} focusedRecipe={} recipes={} target={} count={}",
                goal.goalId(),
                goal.focusedRecipeId(),
                goal.recipes().size(),
                goal.primaryTargetOutput().identity().itemId(),
                goal.targetCount());
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
        return displayRecipe(goal.focusedRecipeId());
    }

    static boolean openRecipe(GoalDescriptor goal, GoalProjectionEntry entry) {
        if (entry != null) {
            if (!entry.producerRecipeId().isBlank() && displayRecipe(entry.producerRecipeId())) {
                return true;
            }
            if (entry.kind() == GoalProjectionEntryKind.CHOICE_CARD
                    && !entry.recipeId().isBlank() && displayRecipe(entry.recipeId())) {
                return true;
            }
            if (entry.identity() != null && displayFirstRecipeByOutput(entry.identity())) {
                return true;
            }
        }
        return openRecipe(goal);
    }

    static boolean openRecipe(ItemIdentity identity) {
        return displayRecipesForIdentity(identity);
    }

    private static boolean displayRecipe(String recipeId) {
        ResourceLocation id = ResourceLocation.tryParse(recipeId);
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

    private static boolean displayFirstRecipeByOutput(ItemIdentity identity) {
        ItemStack stack = SlotWorkspaceViewModel.displayStackForIdentity(identity);
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        List<EmiRecipe> recipes = EmiApi.getRecipeManager().getRecipesByOutput(EmiStack.of(stack));
        if (recipes.isEmpty()) {
            return false;
        }
        EmiApi.displayRecipe(recipes.get(0));
        return true;
    }

    private static boolean displayRecipesForIdentity(ItemIdentity identity) {
        ItemStack stack = SlotWorkspaceViewModel.displayStackForIdentity(identity);
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        EmiStack emiStack = EmiStack.of(stack);
        if (EmiApi.getRecipeManager().getRecipesByOutput(emiStack).isEmpty()) {
            return false;
        }
        EmiApi.displayRecipes(emiStack);
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
        return openChoiceEditor(goal, choiceEntry(goal, choiceGroupId));
    }

    static boolean openChoiceEditor(GoalDescriptor goal, GoalProjectionEntry entry) {
        String choiceGroupId = entry == null ? "" : entry.choiceGroupId();
        if (GoalChoiceKeys.isProducerChoiceGroup(choiceGroupId)) {
            ItemIdentity target = entry.identity() == null
                    ? GoalChoiceKeys.producerTargetIdentity(choiceGroupId)
                    : entry.identity();
            if (target == null || !GoalWorkspaceClientState.beginPendingRecipeChoice(
                    goal == null ? "" : goal.goalId(),
                    choiceGroupId,
                    choiceLabel(entry),
                    target)) {
                return false;
            }
            if (!displayRecipesForIdentity(target)) {
                GoalWorkspaceClientState.clearPendingRecipeChoice();
                return false;
            }
            SlotDebugLog.log(
                    "[emi][goal] opened producer choice recipes goal={} choice={} target={}",
                    goal == null ? "" : goal.goalId(),
                    choiceGroupId,
                    target.itemId());
            return true;
        }

        EmiIngredient ingredient = ingredientForChoice(goal, entry);
        if (ingredient == null || ingredient.isEmpty()) {
            SlotDebugLog.log(
                    "[emi][goal] cannot open choice alternatives goal={} choice={} reason=missing_serialized_ingredient",
                    goal == null ? "" : goal.goalId(),
                    choiceGroupId == null ? "" : choiceGroupId);
            if (entry != null && !entry.recipeId().isBlank() && displayRecipe(entry.recipeId())) {
                SlotDebugLog.log(
                        "[emi][goal] opened parent recipe for opaque choice goal={} choice={} recipe={}",
                        goal == null ? "" : goal.goalId(),
                        choiceGroupId == null ? "" : choiceGroupId,
                        entry.recipeId());
                showStatus("Opened parent recipe; EMI did not expose item alternatives for this input");
                return true;
            }
            return false;
        }
        if (!GoalWorkspaceClientState.beginPendingRecipeChoice(
                goal == null ? "" : goal.goalId(),
                choiceGroupId,
                choiceLabel(entry),
                entry == null ? null : entry.identity())) {
            return false;
        }
        EmiApi.displayRecipes(ingredient);
        SlotDebugLog.log(
                "[emi][goal] opened choice alternatives goal={} choice={} stacks={}",
                goal == null ? "" : goal.goalId(),
                choiceGroupId,
                ingredient.getEmiStacks().size());
        return true;
    }

    private static GoalProjectionEntry choiceEntry(GoalDescriptor goal, String choiceGroupId) {
        if (choiceGroupId == null || choiceGroupId.isBlank()) {
            return null;
        }
        return new GoalProjectionEntry(
                GoalProjectionEntryKind.CHOICE_CARD,
                GoalChoiceKeys.producerTargetIdentity(choiceGroupId),
                choiceGroupId,
                GoalChoiceKeys.recipeIdFromChoiceGroup(choiceGroupId),
                GoalChoiceKeys.ingredientIdFromChoiceGroup(choiceGroupId),
                "",
                "",
                0,
                0,
                0,
                0,
                0,
                false,
                choiceGroupId,
                List.of(),
                List.of(goal == null ? "" : goal.label()),
                List.of());
    }

    private static boolean applyPendingRecipeChoice(EmiRecipe recipe, Runnable openWorkspace) {
        GoalWorkspaceClientState.PendingRecipeChoice pending = GoalWorkspaceClientState.pendingRecipeChoice();
        if (pending == null) {
            return false;
        }
        GoalWorkspaceClientState.GoalTab tab = GoalWorkspaceClientState.goalTab(pending.goalId());
        if (tab == null) {
            GoalWorkspaceClientState.clearPendingRecipeChoice();
            showStatus("SLOT goal choice is no longer active");
            return true;
        }
        LinkedHashMap<String, GoalRecipeDescriptor> collected = new LinkedHashMap<>();
        collectRecipe(recipe, collected, new LinkedHashSet<>(), 0);
        String selectedRecipeId = recipeId(recipe);
        GoalRecipeDescriptor selected = collected.get(selectedRecipeId);
        if (selected == null) {
            selected = describeRecipe(recipe, selectedRecipeId);
            collected.put(selected.recipeId(), selected);
        }
        String choiceGroupId = pending.choiceGroupId();
        String producerChoiceGroupId;
        ItemIdentity selectedIdentity = null;
        GoalStackDescriptor selectedAlternative = null;
        String ingredientChoiceGroupId = "";
        if (GoalChoiceKeys.isProducerChoiceGroup(choiceGroupId)) {
            ItemIdentity target = pending.targetIdentity() == null
                    ? GoalChoiceKeys.producerTargetIdentity(choiceGroupId)
                    : pending.targetIdentity();
            if (target == null || !recipeOutputs(selected, target)) {
                showStatus("This recipe does not produce " + pending.label());
                return true;
            }
            producerChoiceGroupId = choiceGroupId;
        } else {
            GoalIngredientDescriptor ingredient = ingredientDescriptorForChoice(tab.descriptor(), choiceGroupId);
            selectedAlternative = outputSatisfyingIngredient(recipe, selected, ingredient);
            selectedIdentity = selectedAlternative == null ? null : selectedAlternative.identity();
            if (selectedIdentity == null) {
                showStatus("This recipe does not satisfy " + pending.label());
                return true;
            }
            ingredientChoiceGroupId = choiceGroupId;
            producerChoiceGroupId = GoalChoiceKeys.producerChoiceGroupId(
                    GoalChoiceKeys.recipeIdFromChoiceGroup(choiceGroupId),
                    GoalChoiceKeys.ingredientIdFromChoiceGroup(choiceGroupId),
                    selectedIdentity);
        }
        if (!GoalWorkspaceClientState.applyManualRecipeChoice(
                tab.goalId(),
                ingredientChoiceGroupId,
                selectedIdentity,
                producerChoiceGroupId,
                selectedRecipeId,
                List.copyOf(collected.values()),
                selectedAlternative)) {
            showStatus("Could not update SLOT goal choice");
            return true;
        }
        ItemIdentity defaultIdentity = selectedIdentity == null
                ? pending.targetIdentity() == null
                ? GoalChoiceKeys.producerTargetIdentity(producerChoiceGroupId)
                : pending.targetIdentity()
                : selectedIdentity;
        rememberRecipeDefault(defaultIdentity, selectedRecipeId);
        SlotDebugLog.log(
                "[emi][goal] selected recipe choice goal={} choice={} recipe={} item={}",
                tab.goalId(),
                producerChoiceGroupId,
                selectedRecipeId,
                selectedIdentity == null ? "" : selectedIdentity.itemId());
        persistGoal(GoalWorkspaceClientState.goalTab(tab.goalId()));
        showStatus("Updated SLOT goal choice");
        if (openWorkspace != null) {
            openWorkspace.run();
        }
        return true;
    }

    private static void rememberRecipeDefault(ItemIdentity outputIdentity, String recipeId) {
        if (outputIdentity == null || recipeId == null || recipeId.isBlank()) {
            return;
        }
        PacketDistributor.sendToServer(new SlotGoalRecipeDefaultPayload(outputIdentity.itemId(), recipeId));
    }

    private static void persistGoal(GoalWorkspaceClientState.GoalTab goal) {
        PacketDistributor.sendToServer(SlotGoalPlanPayload.save(GoalWorkspaceClientState.planState(goal)));
    }

    static GoalDescriptor enrichVisibleAlternatives(GoalDescriptor goal, SlotWorkspaceViewModel source) {
        if (goal == null || source == null) {
            return goal;
        }
        List<VisibleGoalStack> visible = visibleGoalStacks(source);
        if (visible.isEmpty()) {
            return goal;
        }
        boolean changed = false;
        ArrayList<GoalRecipeDescriptor> recipes = new ArrayList<>(goal.recipes().size());
        for (GoalRecipeDescriptor recipe : goal.recipes()) {
            List<GoalIngredientDescriptor> inputs = enrichIngredients(recipe.inputs(), visible);
            if (inputs != recipe.inputs()) {
                changed = true;
                recipes.add(new GoalRecipeDescriptor(
                        recipe.recipeId(),
                        recipe.categoryId(),
                        recipe.supportsTree(),
                        recipe.outputs(),
                        inputs,
                        recipe.catalysts(),
                        recipe.diagnostics()
                ));
            } else {
                recipes.add(recipe);
            }
        }
        return changed
                ? new GoalDescriptor(
                goal.goalId(),
                goal.label(),
                goal.targetOutputs(),
                goal.targetCount(),
                goal.focusedRecipeId(),
                goal.focusedCategoryId(),
                recipes)
                : goal;
    }

    private static List<GoalIngredientDescriptor> enrichIngredients(
            List<GoalIngredientDescriptor> ingredients,
            List<VisibleGoalStack> visible
    ) {
        if (ingredients == null || ingredients.isEmpty() || visible == null || visible.isEmpty()) {
            return ingredients;
        }
        boolean changed = false;
        ArrayList<GoalIngredientDescriptor> result = new ArrayList<>(ingredients.size());
        for (GoalIngredientDescriptor ingredient : ingredients) {
            GoalIngredientDescriptor enriched = enrichIngredient(ingredient, visible);
            if (enriched != ingredient) {
                changed = true;
            }
            result.add(enriched);
        }
        return changed ? List.copyOf(result) : ingredients;
    }

    private static GoalIngredientDescriptor enrichIngredient(
            GoalIngredientDescriptor ingredient,
            List<VisibleGoalStack> visible
    ) {
        if (ingredient == null || ingredient.serializedIngredient().isBlank()) {
            return ingredient;
        }
        EmiIngredient emiIngredient = ingredientFromDescriptor(ingredient);
        if (emiIngredient == null) {
            return ingredient;
        }
        LinkedHashMap<ItemIdentity, GoalStackDescriptor> alternatives = new LinkedHashMap<>();
        for (GoalStackDescriptor alternative : ingredient.alternatives()) {
            alternatives.putIfAbsent(alternative.identity(), alternative);
        }
        for (VisibleGoalStack visibleStack : visible) {
            if (visibleStack == null || visibleStack.identity() == null || visibleStack.stack().isEmpty()) {
                continue;
            }
            if (containsAlternative(alternatives, visibleStack.identity())) {
                continue;
            }
            if (!visibleSatisfiesIngredient(emiIngredient, ingredient, visibleStack)) {
                continue;
            }
            alternatives.put(
                    visibleStack.identity(),
                    new GoalStackDescriptor(visibleStack.identity(), visibleStack.label(), 1));
        }
        if (alternatives.size() == ingredient.alternatives().size()) {
            return ingredient;
        }
        return new GoalIngredientDescriptor(
                ingredient.ingredientId(),
                ingredient.label(),
                ingredient.quantity(),
                ingredient.chance(),
                ingredient.serializedIngredient(),
                List.copyOf(alternatives.values()),
                ingredient.choiceRequired(),
                ingredient.consumed(),
                ingredient.tagOrListLabel(),
                ingredient.diagnostics()
        );
    }

    private static boolean visibleSatisfiesIngredient(
            EmiIngredient emiIngredient,
            GoalIngredientDescriptor ingredient,
            VisibleGoalStack visibleStack
    ) {
        try {
            if (ingredientContainsOutput(emiIngredient, EmiStack.of(visibleStack.stack()), visibleStack.identity())) {
                return true;
            }
        } catch (RuntimeException | LinkageError ignored) {
        }
        return stackMatchesItemTagLabel(visibleStack.stack(), ingredient.tagOrListLabel());
    }

    private static boolean containsAlternative(
            LinkedHashMap<ItemIdentity, GoalStackDescriptor> alternatives,
            ItemIdentity identity
    ) {
        for (ItemIdentity existing : alternatives.keySet()) {
            if (ItemIdentityMatcher.matchesMovable(existing, identity)) {
                return true;
            }
        }
        return false;
    }

    private static boolean stackMatchesItemTagLabel(ItemStack stack, String label) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        String tagId = itemTagId(label);
        if (tagId.isBlank()) {
            return false;
        }
        ResourceLocation id = ResourceLocation.tryParse(tagId);
        if (id == null) {
            return false;
        }
        try {
            return stack.is(TagKey.create(Registries.ITEM, id));
        } catch (RuntimeException | LinkageError ignored) {
            return false;
        }
    }

    private static String itemTagId(String label) {
        if (label == null || label.isBlank()) {
            return "";
        }
        String value = label.trim();
        if (value.startsWith("#")) {
            value = value.substring(1);
        }
        if (value.startsWith("item:")) {
            value = value.substring("item:".length());
        }
        return value.contains(":") ? value : "";
    }

    private static List<VisibleGoalStack> visibleGoalStacks(SlotWorkspaceViewModel source) {
        LinkedHashMap<ItemIdentity, VisibleGoalStack> visible = new LinkedHashMap<>();
        collectVisibleGoalStacks(visible, source.atlasItems());
        collectVisibleGoalStacks(visible, source.triageItems());
        for (SlotWorkspaceViewModel.ChestChip chip : source.chestChips()) {
            if (chip == null) {
                continue;
            }
            for (SlotWorkspaceViewModel.ChestContentSummary summary : chip.contents()) {
                if (summary == null || summary.displayStack().isEmpty()) {
                    continue;
                }
                ItemIdentity identity = ItemIdentityMatcher.create(summary.displayStack());
                visible.putIfAbsent(identity, new VisibleGoalStack(
                        identity,
                        summary.name().isBlank() ? identity.itemId() : summary.name(),
                        summary.displayStack()));
            }
        }
        return List.copyOf(visible.values());
    }

    private static void collectVisibleGoalStacks(
            LinkedHashMap<ItemIdentity, VisibleGoalStack> visible,
            List<SlotWorkspaceViewModel.AtlasItem> items
    ) {
        for (SlotWorkspaceViewModel.AtlasItem item : items) {
            if (item == null || item.displayStack().isEmpty()) {
                continue;
            }
            ItemIdentity identity = item.identity().toIdentity();
            if (identity == null) {
                identity = ItemIdentityMatcher.create(item.displayStack());
            }
            visible.putIfAbsent(identity, new VisibleGoalStack(
                    identity,
                    item.name().isBlank() ? identity.itemId() : item.name(),
                    item.displayStack()));
        }
    }

    private static EmiIngredient ingredientForChoice(GoalDescriptor goal, GoalProjectionEntry entry) {
        String choiceGroupId = entry == null ? "" : entry.choiceGroupId();
        GoalIngredientDescriptor descriptor = ingredientDescriptorForChoice(goal, choiceGroupId);
        String serialized = entry != null && !entry.serializedIngredient().isBlank()
                ? entry.serializedIngredient()
                : descriptor == null ? "" : descriptor.serializedIngredient();
        String label = descriptor == null ? "" : descriptor.label();
        if (serialized.isBlank()) {
            return null;
        }
        try {
            JsonElement json = JsonParser.parseString(serialized);
            EmiIngredient ingredient = EmiIngredientSerializer.getDeserialized(json);
            SlotDebugLog.verboseLog(
                    "[emi][goal] deserialized choice ingredient goal={} choice={} label={} stacks={} serialized={}",
                    goal == null ? "" : goal.goalId(),
                    choiceGroupId,
                    label,
                    ingredient == null ? 0 : ingredient.getEmiStacks().size(),
                    serialized);
            return ingredient;
        } catch (RuntimeException | LinkageError error) {
            SlotDebugLog.log(
                    "[emi][goal] failed to deserialize choice ingredient goal={} choice={} error={}",
                    goal == null ? "" : goal.goalId(),
                    choiceGroupId == null ? "" : choiceGroupId,
                    error.toString());
            return null;
        }
    }

    private static GoalIngredientDescriptor ingredientDescriptorForChoice(GoalDescriptor goal, String choiceGroupId) {
        if (goal == null || choiceGroupId == null || choiceGroupId.isBlank()) {
            return null;
        }
        String recipeId = GoalChoiceKeys.recipeIdFromChoiceGroup(choiceGroupId);
        String ingredientId = GoalChoiceKeys.ingredientIdFromChoiceGroup(choiceGroupId);
        if (recipeId.isBlank() || ingredientId.isBlank()) {
            return null;
        }
        for (GoalRecipeDescriptor recipe : goal.recipes()) {
            if (!recipeId.equals(recipe.recipeId())) {
                continue;
            }
            for (GoalIngredientDescriptor ingredient : recipe.inputs()) {
                if (ingredientId.equals(ingredient.ingredientId())) {
                    return ingredient;
                }
            }
        }
        return null;
    }

    private static boolean recipeOutputs(GoalRecipeDescriptor recipe, ItemIdentity identity) {
        if (recipe == null || identity == null) {
            return false;
        }
        for (GoalStackDescriptor output : recipe.outputs()) {
            if (ItemIdentityMatcher.matchesMovable(output.identity(), identity)) {
                return true;
            }
        }
        return false;
    }

    private static GoalStackDescriptor outputSatisfyingIngredient(
            EmiRecipe emiRecipe,
            GoalRecipeDescriptor recipe,
            GoalIngredientDescriptor ingredient
    ) {
        if (recipe == null || ingredient == null) {
            return null;
        }
        for (GoalStackDescriptor output : recipe.outputs()) {
            GoalStackDescriptor alternative = matchingAlternative(output.identity(), ingredient);
            if (alternative != null) {
                return alternative;
            }
        }
        EmiIngredient serializedIngredient = ingredientFromDescriptor(ingredient);
        if (serializedIngredient == null || emiRecipe == null) {
            return null;
        }
        for (EmiStack output : emiRecipe.getOutputs()) {
            GoalStackDescriptor outputDescriptor = stackDescriptor(output);
            if (outputDescriptor == null) {
                continue;
            }
            GoalStackDescriptor alternative = matchingAlternative(outputDescriptor.identity(), ingredient);
            if (alternative != null) {
                return alternative;
            }
            if (ingredientContainsOutput(serializedIngredient, output, outputDescriptor.identity())) {
                return outputDescriptor;
            }
        }
        return null;
    }

    private static GoalStackDescriptor matchingAlternative(ItemIdentity outputIdentity, GoalIngredientDescriptor ingredient) {
        if (outputIdentity == null || ingredient == null) {
            return null;
        }
        for (GoalStackDescriptor alternative : ingredient.alternatives()) {
            if (ItemIdentityMatcher.matchesMovable(outputIdentity, alternative.identity())) {
                return alternative;
            }
        }
        return null;
    }

    private static EmiIngredient ingredientFromDescriptor(GoalIngredientDescriptor descriptor) {
        String serialized = descriptor == null ? "" : descriptor.serializedIngredient();
        if (serialized.isBlank()) {
            return null;
        }
        try {
            return EmiIngredientSerializer.getDeserialized(JsonParser.parseString(serialized));
        } catch (RuntimeException | LinkageError error) {
            SlotDebugLog.verboseLog(
                    "[emi][goal] failed to deserialize ingredient descriptor id={} error={}",
                    descriptor.ingredientId(),
                    error.toString());
            return null;
        }
    }

    private static boolean ingredientContainsOutput(
            EmiIngredient ingredient,
            EmiStack output,
            ItemIdentity outputIdentity
    ) {
        if (ingredient == null || outputIdentity == null) {
            return false;
        }
        for (EmiStack alternative : ingredient.getEmiStacks()) {
            if (alternative == null || alternative.isEmpty()) {
                continue;
            }
            try {
                if (output != null && output.isEqual(alternative)) {
                    return true;
                }
            } catch (RuntimeException | LinkageError ignored) {
            }
            GoalStackDescriptor alternativeDescriptor = stackDescriptor(alternative);
            if (alternativeDescriptor != null
                    && ItemIdentityMatcher.matchesMovable(alternativeDescriptor.identity(), outputIdentity)) {
                return true;
            }
        }
        return false;
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
            if (stackDescriptor(stack) == null) {
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
        if (recipe == null) {
            return;
        }
        LinkedHashMap<String, PendingRecipe> queue = new LinkedHashMap<>();
        queue.put(recipeId(recipe), new PendingRecipe(recipe, 0));
        boolean truncated = false;
        while (!queue.isEmpty()) {
            String pendingId = queue.keySet().iterator().next();
            PendingRecipe pending = queue.remove(pendingId);
            if (recipes.containsKey(pendingId)) {
                continue;
            }
            if (recipes.size() >= MAX_RECIPES) {
                truncated = true;
                break;
            }
            SlotDebugLog.verboseLog(
                    "[emi][goal] collect recipe depth={} id={} category={} tree={} inputs={} catalysts={} outputs={}",
                    pending.depth(),
                    pendingId,
                    categoryId(pending.recipe()),
                    pending.recipe().supportsRecipeTree(),
                    pending.recipe().getInputs().size(),
                    pending.recipe().getCatalysts().size(),
                    pending.recipe().getOutputs().size());
            recipes.put(pendingId, describeRecipe(pending.recipe(), pendingId));
            if (pending.depth() >= MAX_RECIPE_DEPTH) {
                SlotDebugLog.verboseLog(
                        "[emi][goal] collect depth limit recipe={} depth={} max={}",
                        pendingId,
                        pending.depth(),
                        MAX_RECIPE_DEPTH);
                continue;
            }
            enqueueProducerRecipes(pending.recipe(), pendingId, pending.depth() + 1, recipes, queue);
        }
        if (truncated) {
            SlotDebugLog.verboseLog("[emi][goal] recipe collection truncated at {}", MAX_RECIPES);
        }
    }

    private static void enqueueProducerRecipes(
            EmiRecipe recipe,
            String parentId,
            int childDepth,
            LinkedHashMap<String, GoalRecipeDescriptor> recipes,
            LinkedHashMap<String, PendingRecipe> queue
    ) {
        for (EmiIngredient ingredient : recipe.getInputs()) {
            SlotDebugLog.verboseLog(
                    "[emi][goal] traverse input recipe={} amount={} stacks={} serialized={}",
                    parentId,
                    ingredient.getAmount(),
                    ingredient.getEmiStacks().size(),
                    serializedIngredient(ingredient));
            for (EmiStack alternative : ingredient.getEmiStacks()) {
                GoalStackDescriptor requested = stackDescriptor(alternative);
                if (requested == null) {
                    SlotDebugLog.verboseLog(
                            "[emi][goal] skip opaque alternative recipe={} alternative={}",
                            parentId,
                            alternative);
                    continue;
                }
                List<EmiRecipe> childRecipes = EmiApi.getRecipeManager().getRecipesByOutput(alternative);
                SlotDebugLog.verboseLog(
                        "[emi][goal] producer candidates parent={} alternative={} count={} recipes={}",
                        parentId,
                        requested.identity().itemId(),
                        childRecipes.size(),
                        recipeIds(childRecipes));
                for (EmiRecipe child : childRecipes) {
                    if (!shouldCollectProducerRecipe(child, alternative, parentId)) {
                        continue;
                    }
                    String childId = recipeId(child);
                    if (!recipes.containsKey(childId) && !queue.containsKey(childId)) {
                        queue.put(childId, new PendingRecipe(child, childDepth));
                    }
                }
            }
        }
    }

    private static boolean shouldCollectProducerRecipe(EmiRecipe recipe, EmiStack requestedOutput, String parentId) {
        if (recipe == null) {
            return false;
        }
        String id = recipeId(recipe);
        if (lootTableRecipeId(id)) {
            SlotDebugLog.verboseLog(
                    "[emi][goal] skip loot producer parent={} child={}",
                    parentId,
                    id);
            return false;
        }
        if (!recipe.supportsRecipeTree()) {
            SlotDebugLog.verboseLog(
                    "[emi][goal] skip non-tree producer parent={} child={} category={}",
                    parentId,
                    id,
                    categoryId(recipe));
            return false;
        }
        if (!outputsRequestedStack(recipe, requestedOutput)) {
            SlotDebugLog.verboseLog(
                    "[emi][goal] skip producer with no requested output parent={} child={} requested={}",
                    parentId,
                    id,
                    requestedOutput);
            return false;
        }
        return true;
    }

    private static boolean outputsRequestedStack(EmiRecipe recipe, EmiStack requestedOutput) {
        GoalStackDescriptor requested = stackDescriptor(requestedOutput);
        if (requested == null) {
            return false;
        }
        for (EmiStack output : recipe.getOutputs()) {
            GoalStackDescriptor produced = stackDescriptor(output);
            if (produced != null && ItemIdentityMatcher.matchesMovable(produced.identity(), requested.identity())) {
                return true;
            }
        }
        return false;
    }

    private static boolean lootTableRecipeId(String recipeId) {
        if (recipeId == null || recipeId.isBlank()) {
            return false;
        }
        int separator = recipeId.indexOf(':');
        String path = separator < 0 ? recipeId : recipeId.substring(separator + 1);
        return path.startsWith("/chests/") || path.startsWith("chests/");
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
            EmiIngredient ingredient = ingredients.get(index);
            if (ingredient == null || ingredient.isEmpty()) {
                SlotDebugLog.verboseLog(
                        "[emi][goal] skipped empty recipe ingredient recipe={} id={}",
                        recipeId,
                        prefix + "_" + index);
                continue;
            }
            descriptors.add(ingredientDescriptor(ingredient, recipeId, prefix, index));
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
        String firstNonItemLabel = "";
        boolean allItemAlternativesReturnSelf = true;
        boolean hasItemAlternative = false;
        for (EmiStack stack : ingredient.getEmiStacks()) {
            if (alternatives.size() >= MAX_ALTERNATIVES) {
                diagnostics.add("ingredient_alternatives_truncated");
                break;
            }
            GoalStackDescriptor descriptor = stackDescriptor(stack);
            if (descriptor == null) {
                String nonItemLabel = nonItemAlternativeLabel(stack);
                if (firstNonItemLabel.isBlank()) {
                    firstNonItemLabel = nonItemLabel;
                }
                diagnostics.add(nonItemLabel.isBlank()
                        ? "ingredient_has_non_item_alternative"
                        : "ingredient_has_non_item_alternative=" + nonItemLabel);
                continue;
            }
            hasItemAlternative = true;
            if (!returnsMovableSelf(stack, descriptor.identity())) {
                allItemAlternativesReturnSelf = false;
            }
            alternatives.putIfAbsent(descriptor.identity(), descriptor);
        }
        String serialized = serializedIngredient(ingredient);
        String tagOrList = tagOrListLabel(serialized);
        if (ingredient.getChance() != 1f) {
            diagnostics.add("ingredient_has_chance");
        }
        boolean consumed = !hasItemAlternative || !allItemAlternativesReturnSelf;
        if (!consumed) {
            diagnostics.add("ingredient_not_consumed");
        }
        boolean choiceRequired = alternatives.size() > 1 || (!tagOrList.isBlank() && alternatives.isEmpty());
        String label = choiceRequired && !tagOrList.isBlank()
                ? tagOrList
                : alternatives.isEmpty()
                ? (!firstNonItemLabel.isBlank() ? firstNonItemLabel : prefix + " " + (index + 1))
                : alternatives.values().iterator().next().displayName();
        SlotDebugLog.verboseLog(
                "[emi][goal] ingredient descriptor recipe={} id={} label={} amount={} tagOrList={} alternatives={} choiceRequired={} consumed={} diagnostics={} serialized={}",
                recipeId,
                prefix + "_" + index,
                label,
                safeCount(ingredient.getAmount()),
                tagOrList,
                stackTokens(List.copyOf(alternatives.values())),
                choiceRequired,
                consumed,
                diagnostics,
                serialized);
        return new GoalIngredientDescriptor(
                prefix + "_" + index,
                label,
                safeCount(ingredient.getAmount()),
                ingredient.getChance(),
                serialized,
                List.copyOf(alternatives.values()),
                choiceRequired,
                consumed,
                tagOrList,
                diagnostics
        );
    }

    private static boolean returnsMovableSelf(EmiStack stack, ItemIdentity identity) {
        if (stack == null || identity == null) {
            return false;
        }
        EmiStack remainder = stack.getRemainder();
        GoalStackDescriptor remainderDescriptor = stackDescriptor(remainder);
        return remainderDescriptor != null
                && ItemIdentityMatcher.matchesMovable(remainderDescriptor.identity(), identity);
    }

    private static GoalStackDescriptor stackDescriptor(EmiStack emiStack) {
        if (emiStack == null || emiStack.isEmpty()) {
            return null;
        }
        ItemStack stack = emiStack.getItemStack();
        if (stack != null && !stack.isEmpty()) {
            ItemIdentity identity = ItemIdentityMatcher.create(stack);
            Component name = emiStack.getName();
            return new GoalStackDescriptor(
                    identity,
                    name == null ? stack.getHoverName().getString() : name.getString(),
                    safeCount(emiStack.getAmount())
            );
        }
        String syntheticId = syntheticEmiStackIdentity(emiStack);
        if (syntheticId.isBlank()) {
            return null;
        }
        String label = nonItemAlternativeLabel(emiStack);
        return new GoalStackDescriptor(
                ItemIdentity.of(syntheticId),
                label.isBlank() ? syntheticId : label,
                safeCount(emiStack.getAmount())
        );
    }

    private static String syntheticEmiStackIdentity(EmiStack emiStack) {
        String rawId = emiStackId(emiStack);
        if (rawId.isBlank()) {
            rawId = emiStackKey(emiStack);
        }
        if (rawId.isBlank()) {
            rawId = nonItemAlternativeLabel(emiStack);
        }
        String path = sanitizeIdentityPath(rawId);
        return path.isBlank() ? "" : "slot:emi/" + path;
    }

    private static String emiStackId(EmiStack emiStack) {
        if (emiStack == null) {
            return "";
        }
        try {
            ResourceLocation id = emiStack.getId();
            return id == null ? "" : id.toString();
        } catch (RuntimeException | LinkageError ignored) {
            return "";
        }
    }

    private static String emiStackKey(EmiStack emiStack) {
        if (emiStack == null) {
            return "";
        }
        try {
            Object key = emiStack.getKey();
            return key == null ? "" : key.toString();
        } catch (RuntimeException | LinkageError ignored) {
            return "";
        }
    }

    private static String sanitizeIdentityPath(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT).replace(':', '/');
        StringBuilder builder = new StringBuilder(normalized.length());
        for (int index = 0; index < normalized.length(); index++) {
            char ch = normalized.charAt(index);
            if ((ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9')
                    || ch == '_' || ch == '-' || ch == '.' || ch == '/') {
                builder.append(ch);
            } else {
                builder.append('_');
            }
        }
        return builder.toString();
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
        String label = GoalWorkspaceClientState.pendingRecipeChoice() == null ? "SLOT+" : "USE";
        int labelX = x + Math.max(3, (button.width() - minecraft.font.width(label)) / 2);
        graphics.drawString(minecraft.font, label, labelX, y + 3, 0xFFFFFFFF, true);
    }

    private static String recipeButtonTooltip() {
        GoalWorkspaceClientState.PendingRecipeChoice pending = GoalWorkspaceClientState.pendingRecipeChoice();
        return pending == null ? "Add SLOT goal" : "Use this recipe for " + pending.label();
    }

    private static String choiceLabel(GoalProjectionEntry entry) {
        if (entry == null || entry.label().isBlank()) {
            return "goal choice";
        }
        String label = entry.label();
        String prefix = "Choose recipe for ";
        return label.startsWith(prefix) ? label.substring(prefix.length()).trim() : label;
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

    private static String stackId(ItemStack stack) {
        return stack == null || stack.isEmpty()
                ? ""
                : ItemIdentityMatcher.create(stack).itemId();
    }

    private static String recipeIds(List<EmiRecipe> recipes) {
        if (recipes == null || recipes.isEmpty()) {
            return "[]";
        }
        ArrayList<String> ids = new ArrayList<>();
        for (EmiRecipe recipe : recipes) {
            ids.add(recipeId(recipe));
        }
        return ids.toString();
    }

    private static String stackTokens(List<GoalStackDescriptor> stacks) {
        if (stacks == null || stacks.isEmpty()) {
            return "[]";
        }
        ArrayList<String> tokens = new ArrayList<>();
        for (GoalStackDescriptor stack : stacks) {
            tokens.add(stack.identity().itemId() + "x" + stack.count());
        }
        return tokens.toString();
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

    private record VisibleGoalStack(ItemIdentity identity, String label, ItemStack stack) {
        private VisibleGoalStack {
            label = label == null || label.isBlank() ? identity.itemId() : label.trim();
            stack = stack == null ? ItemStack.EMPTY : stack.copy();
        }
    }

    private record PendingRecipe(EmiRecipe recipe, int depth) {
    }
}
