package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.goal.GoalChoiceResolution;
import dev.imagio.slot.inventory.goal.GoalChoiceKeys;
import dev.imagio.slot.inventory.goal.GoalDescriptor;
import dev.imagio.slot.inventory.goal.GoalIngredientDescriptor;
import dev.imagio.slot.inventory.goal.GoalPlanState;
import dev.imagio.slot.inventory.goal.GoalRecipeDescriptor;
import dev.imagio.slot.inventory.goal.GoalRecipeDefaults;
import dev.imagio.slot.inventory.goal.GoalStackDescriptor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public final class GoalWorkspaceClientState {
    private static final LinkedHashMap<String, MutableGoalTab> GOALS = new LinkedHashMap<>();
    private static String activeGoalId = "";
    private static PendingRecipeChoice pendingRecipeChoice;
    private static GoalRecipeDefaults rememberedRecipeDefaults = GoalRecipeDefaults.empty();
    private static int revision;

    private GoalWorkspaceClientState() {
    }

    public static synchronized int revision() {
        return revision;
    }

    public static synchronized List<GoalTab> goalTabs() {
        ArrayList<GoalTab> tabs = new ArrayList<>(GOALS.size());
        for (MutableGoalTab tab : GOALS.values()) {
            tabs.add(tab.snapshot(tab.goalId.equals(activeGoalId)));
        }
        return List.copyOf(tabs);
    }

    public static synchronized GoalTab activeGoal() {
        MutableGoalTab tab = GOALS.get(activeGoalId);
        return tab == null ? null : tab.snapshot(true);
    }

    public static synchronized boolean hasActiveGoal() {
        return GOALS.containsKey(activeGoalId);
    }

    public static synchronized GoalTab addOrActivate(GoalDescriptor descriptor) {
        if (descriptor == null || descriptor.primaryTargetOutput() == null) {
            return null;
        }
        int targetCount = resolvedTargetCount(descriptor);
        GoalDescriptor normalized = withTargetCount(descriptor, targetCount);
        MutableGoalTab existing = GOALS.get(normalized.goalId());
        if (existing == null) {
            existing = new MutableGoalTab(normalized.goalId(), normalized.label(), targetCount, normalized);
            GOALS.put(existing.goalId, existing);
        } else {
            existing.label = normalized.label();
            existing.targetCount = targetCount;
            existing.descriptor = normalized;
        }
        activeGoalId = existing.goalId;
        revision++;
        return existing.snapshot(true);
    }

    public static synchronized boolean hydratePersistedGoalsIfEmpty(List<GoalPlanState> persistedGoals) {
        if (!GOALS.isEmpty() || persistedGoals == null || persistedGoals.isEmpty()) {
            return false;
        }
        for (GoalPlanState goal : persistedGoals) {
            if (goal == null || goal.descriptor() == null || goal.descriptor().primaryTargetOutput() == null) {
                continue;
            }
            GoalDescriptor descriptor = withTargetCount(goal.descriptor(), goal.targetCount());
            MutableGoalTab tab = new MutableGoalTab(
                    goal.goalId(),
                    goal.label(),
                    Math.max(1, goal.targetCount()),
                    descriptor
            );
            tab.choiceResolution = goal.choiceResolution();
            GOALS.put(tab.goalId, tab);
        }
        if (GOALS.isEmpty()) {
            return false;
        }
        activeGoalId = GOALS.keySet().iterator().next();
        revision++;
        return true;
    }

    public static synchronized void selectAll() {
        if (activeGoalId.isEmpty()) {
            return;
        }
        activeGoalId = "";
        revision++;
    }

    public static synchronized boolean selectGoal(String goalId) {
        String id = clean(goalId);
        if (id.isEmpty() || !GOALS.containsKey(id)) {
            return false;
        }
        if (id.equals(activeGoalId)) {
            return true;
        }
        activeGoalId = id;
        revision++;
        return true;
    }

    public static synchronized boolean removeGoal(String goalId) {
        String id = clean(goalId);
        if (id.isEmpty()) {
            return false;
        }
        MutableGoalTab removed = GOALS.remove(id);
        if (removed == null) {
            return false;
        }
        if (pendingRecipeChoice != null && id.equals(pendingRecipeChoice.goalId())) {
            pendingRecipeChoice = null;
        }
        if (id.equals(activeGoalId)) {
            activeGoalId = GOALS.isEmpty() ? "" : GOALS.keySet().iterator().next();
        }
        revision++;
        return true;
    }

    public static synchronized boolean adjustTargetCount(String goalId, int delta) {
        String id = clean(goalId);
        if (id.isEmpty()) {
            GoalTab active = activeGoal();
            id = active == null ? "" : active.goalId();
        }
        MutableGoalTab tab = GOALS.get(id);
        if (tab == null || delta == 0) {
            return false;
        }
        int next = Math.max(1, tab.targetCount + delta);
        if (next == tab.targetCount) {
            return false;
        }
        tab.targetCount = next;
        tab.descriptor = withTargetCount(tab.descriptor, next);
        activeGoalId = tab.goalId;
        revision++;
        return true;
    }

    public static synchronized boolean setManualChoice(String goalId, String choiceGroupId, ItemIdentity identity) {
        String id = clean(goalId);
        if (id.isEmpty()) {
            GoalTab active = activeGoal();
            id = active == null ? "" : active.goalId();
        }
        MutableGoalTab tab = GOALS.get(id);
        if (tab == null || choiceGroupId == null || choiceGroupId.isBlank() || identity == null) {
            return false;
        }
        tab.choiceResolution = tab.choiceResolution.withChoice(choiceGroupId, identity);
        activeGoalId = tab.goalId;
        revision++;
        return true;
    }

    public static synchronized boolean applyManualRecipeChoice(
            String goalId,
            String ingredientChoiceGroupId,
            ItemIdentity selectedIdentity,
            String recipeChoiceGroupId,
            String recipeId,
            List<GoalRecipeDescriptor> additionalRecipes
    ) {
        return applyManualRecipeChoice(
                goalId,
                ingredientChoiceGroupId,
                selectedIdentity,
                recipeChoiceGroupId,
                recipeId,
                additionalRecipes,
                null
        );
    }

    public static synchronized boolean applyManualRecipeChoice(
            String goalId,
            String ingredientChoiceGroupId,
            ItemIdentity selectedIdentity,
            String recipeChoiceGroupId,
            String recipeId,
            List<GoalRecipeDescriptor> additionalRecipes,
            GoalStackDescriptor selectedAlternative
    ) {
        String id = clean(goalId);
        MutableGoalTab tab = GOALS.get(id);
        if (tab == null || recipeChoiceGroupId == null || recipeChoiceGroupId.isBlank()
                || recipeId == null || recipeId.isBlank()) {
            return false;
        }
        GoalChoiceResolution next = tab.choiceResolution.withRecipeChoice(recipeChoiceGroupId, recipeId);
        if (selectedIdentity != null && ingredientChoiceGroupId != null && !ingredientChoiceGroupId.isBlank()) {
            next = next.withChoice(ingredientChoiceGroupId, selectedIdentity);
        }
        tab.choiceResolution = next;
        tab.descriptor = withMergedRecipes(tab.descriptor, additionalRecipes);
        tab.descriptor = withMergedIngredientAlternative(tab.descriptor, ingredientChoiceGroupId, selectedAlternative);
        ItemIdentity targetIdentity = selectedIdentity == null
                ? GoalChoiceKeys.producerTargetIdentity(recipeChoiceGroupId)
                : selectedIdentity;
        rememberedRecipeDefaults = rememberedRecipeDefaults.withRecipeChoice(targetIdentity, recipeId);
        if (pendingRecipeChoice != null && id.equals(pendingRecipeChoice.goalId())) {
            pendingRecipeChoice = null;
        }
        activeGoalId = tab.goalId;
        revision++;
        return true;
    }

    public static synchronized boolean clearManualChoice(String goalId, String choiceGroupId) {
        String id = clean(goalId);
        if (id.isEmpty()) {
            GoalTab active = activeGoal();
            id = active == null ? "" : active.goalId();
        }
        MutableGoalTab tab = GOALS.get(id);
        if (tab == null || choiceGroupId == null || choiceGroupId.isBlank()
                || !tab.choiceResolution.hasChoice(choiceGroupId)) {
            return false;
        }
        tab.choiceResolution = tab.choiceResolution.withoutChoice(choiceGroupId);
        if (pendingRecipeChoice != null
                && tab.goalId.equals(pendingRecipeChoice.goalId())
                && choiceGroupId.trim().equals(pendingRecipeChoice.choiceGroupId())) {
            pendingRecipeChoice = null;
        }
        activeGoalId = tab.goalId;
        revision++;
        return true;
    }

    public static synchronized boolean beginPendingRecipeChoice(
            String goalId,
            String choiceGroupId,
            String label,
            ItemIdentity targetIdentity
    ) {
        String id = clean(goalId);
        MutableGoalTab tab = GOALS.get(id);
        String choice = clean(choiceGroupId);
        if (tab == null || choice.isBlank()) {
            return false;
        }
        pendingRecipeChoice = new PendingRecipeChoice(
                tab.goalId,
                choice,
                label == null || label.isBlank() ? tab.label : label.trim(),
                targetIdentity
        );
        return true;
    }

    public static synchronized PendingRecipeChoice pendingRecipeChoice() {
        return pendingRecipeChoice;
    }

    public static synchronized GoalRecipeDefaults rememberedRecipeDefaults() {
        return rememberedRecipeDefaults;
    }

    public static synchronized void clearPendingRecipeChoice() {
        pendingRecipeChoice = null;
    }

    public static synchronized GoalTab goalTab(String goalId) {
        MutableGoalTab tab = GOALS.get(clean(goalId));
        return tab == null ? null : tab.snapshot(tab.goalId.equals(activeGoalId));
    }

    public static GoalPlanState planState(GoalTab tab) {
        if (tab == null || tab.descriptor() == null) {
            return null;
        }
        return new GoalPlanState(
                tab.goalId(),
                tab.label(),
                tab.targetCount(),
                tab.descriptor(),
                tab.choiceResolution()
        );
    }

    public static synchronized void clear() {
        if (GOALS.isEmpty() && activeGoalId.isEmpty() && pendingRecipeChoice == null
                && rememberedRecipeDefaults.isEmpty()) {
            return;
        }
        GOALS.clear();
        activeGoalId = "";
        pendingRecipeChoice = null;
        rememberedRecipeDefaults = GoalRecipeDefaults.empty();
        revision++;
    }

    public static GoalDescriptor withTargetCount(GoalDescriptor descriptor, int targetCount) {
        if (descriptor == null) {
            return null;
        }
        return new GoalDescriptor(
                descriptor.goalId(),
                descriptor.label(),
                descriptor.targetOutputs(),
                Math.max(1, targetCount),
                descriptor.focusedRecipeId(),
                descriptor.focusedCategoryId(),
                descriptor.recipes()
        );
    }

    private static GoalDescriptor withMergedRecipes(GoalDescriptor descriptor, List<GoalRecipeDescriptor> additionalRecipes) {
        if (descriptor == null || additionalRecipes == null || additionalRecipes.isEmpty()) {
            return descriptor;
        }
        LinkedHashMap<String, GoalRecipeDescriptor> recipes = new LinkedHashMap<>();
        for (GoalRecipeDescriptor recipe : descriptor.recipes()) {
            recipes.put(recipe.recipeId(), recipe);
        }
        for (GoalRecipeDescriptor recipe : additionalRecipes) {
            if (recipe != null) {
                recipes.put(recipe.recipeId(), recipe);
            }
        }
        return new GoalDescriptor(
                descriptor.goalId(),
                descriptor.label(),
                descriptor.targetOutputs(),
                descriptor.targetCount(),
                descriptor.focusedRecipeId(),
                descriptor.focusedCategoryId(),
                List.copyOf(recipes.values())
        );
    }

    private static GoalDescriptor withMergedIngredientAlternative(
            GoalDescriptor descriptor,
            String choiceGroupId,
            GoalStackDescriptor selectedAlternative
    ) {
        if (descriptor == null || choiceGroupId == null || choiceGroupId.isBlank() || selectedAlternative == null) {
            return descriptor;
        }
        String recipeId = GoalChoiceKeys.recipeIdFromChoiceGroup(choiceGroupId);
        String ingredientId = GoalChoiceKeys.ingredientIdFromChoiceGroup(choiceGroupId);
        if (recipeId.isBlank() || ingredientId.isBlank()) {
            return descriptor;
        }
        boolean changed = false;
        ArrayList<GoalRecipeDescriptor> recipes = new ArrayList<>(descriptor.recipes().size());
        for (GoalRecipeDescriptor recipe : descriptor.recipes()) {
            if (!recipeId.equals(recipe.recipeId())) {
                recipes.add(recipe);
                continue;
            }
            ArrayList<GoalIngredientDescriptor> inputs = new ArrayList<>(recipe.inputs().size());
            boolean recipeChanged = false;
            for (GoalIngredientDescriptor ingredient : recipe.inputs()) {
                if (!ingredientId.equals(ingredient.ingredientId())) {
                    inputs.add(ingredient);
                    continue;
                }
                GoalIngredientDescriptor merged = withMergedAlternative(ingredient, selectedAlternative);
                inputs.add(merged);
                recipeChanged = recipeChanged || merged != ingredient;
            }
            recipes.add(recipeChanged
                    ? new GoalRecipeDescriptor(
                    recipe.recipeId(),
                    recipe.categoryId(),
                    recipe.supportsTree(),
                    recipe.outputs(),
                    inputs,
                    recipe.catalysts(),
                    recipe.diagnostics())
                    : recipe);
            changed = changed || recipeChanged;
        }
        if (!changed) {
            return descriptor;
        }
        return new GoalDescriptor(
                descriptor.goalId(),
                descriptor.label(),
                descriptor.targetOutputs(),
                descriptor.targetCount(),
                descriptor.focusedRecipeId(),
                descriptor.focusedCategoryId(),
                recipes
        );
    }

    private static GoalIngredientDescriptor withMergedAlternative(
            GoalIngredientDescriptor ingredient,
            GoalStackDescriptor selectedAlternative
    ) {
        for (GoalStackDescriptor alternative : ingredient.alternatives()) {
            if (ItemIdentityMatcher.matchesMovable(alternative.identity(), selectedAlternative.identity())) {
                return ingredient;
            }
        }
        ArrayList<GoalStackDescriptor> alternatives = new ArrayList<>(ingredient.alternatives());
        alternatives.add(selectedAlternative);
        return new GoalIngredientDescriptor(
                ingredient.ingredientId(),
                ingredient.label(),
                ingredient.quantity(),
                ingredient.chance(),
                ingredient.serializedIngredient(),
                alternatives,
                ingredient.choiceRequired(),
                ingredient.consumed(),
                ingredient.tagOrListLabel(),
                ingredient.diagnostics()
        );
    }

    private static int resolvedTargetCount(GoalDescriptor descriptor) {
        if (descriptor.targetCount() > 0) {
            return descriptor.targetCount();
        }
        GoalStackDescriptor output = descriptor.primaryTargetOutput();
        return output == null ? 1 : Math.max(1, output.count());
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    public record GoalTab(
            String goalId,
            String label,
            int targetCount,
            GoalDescriptor descriptor,
            GoalChoiceResolution choiceResolution,
            boolean active
    ) {
        public GoalTab {
            goalId = goalId == null || goalId.isBlank() ? "goal" : goalId.trim();
            label = label == null || label.isBlank() ? goalId : label.trim();
            targetCount = Math.max(1, targetCount);
            choiceResolution = choiceResolution == null ? GoalChoiceResolution.empty() : choiceResolution;
        }
    }

    public record PendingRecipeChoice(
            String goalId,
            String choiceGroupId,
            String label,
            ItemIdentity targetIdentity
    ) {
        public PendingRecipeChoice {
            goalId = clean(goalId);
            choiceGroupId = clean(choiceGroupId);
            label = label == null || label.isBlank() ? "goal choice" : label.trim();
        }
    }

    private static final class MutableGoalTab {
        private final String goalId;
        private String label;
        private int targetCount;
        private GoalDescriptor descriptor;
        private GoalChoiceResolution choiceResolution = GoalChoiceResolution.empty();

        private MutableGoalTab(String goalId, String label, int targetCount, GoalDescriptor descriptor) {
            this.goalId = goalId;
            this.label = label;
            this.targetCount = targetCount;
            this.descriptor = descriptor;
        }

        private GoalTab snapshot(boolean active) {
            return new GoalTab(goalId, label, targetCount, descriptor, choiceResolution, active);
        }
    }
}
