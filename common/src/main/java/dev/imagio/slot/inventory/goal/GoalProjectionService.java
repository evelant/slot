package dev.imagio.slot.inventory.goal;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class GoalProjectionService {
    public GoalProjection project(GoalDescriptor goal, GoalVisibleAuthority authority) {
        return project(goal, authority, GoalProjectionOptions.defaults(), GoalChoiceResolution.empty(), GoalRecipeDefaults.empty());
    }

    public GoalProjection project(
            GoalDescriptor goal,
            GoalVisibleAuthority authority,
            GoalProjectionOptions options
    ) {
        return project(goal, authority, options, GoalChoiceResolution.empty(), GoalRecipeDefaults.empty());
    }

    public GoalProjection project(
            GoalDescriptor goal,
            GoalVisibleAuthority authority,
            GoalProjectionOptions options,
            GoalChoiceResolution manualChoices
    ) {
        return project(goal, authority, options, manualChoices, GoalRecipeDefaults.empty());
    }

    public GoalProjection project(
            GoalDescriptor goal,
            GoalVisibleAuthority authority,
            GoalProjectionOptions options,
            GoalChoiceResolution manualChoices,
            GoalRecipeDefaults recipeDefaults
    ) {
        return new ProjectionRun(
                goal,
                authority == null ? GoalVisibleAuthority.empty() : authority,
                options == null ? GoalProjectionOptions.defaults() : options,
                manualChoices == null ? GoalChoiceResolution.empty() : manualChoices,
                recipeDefaults == null ? GoalRecipeDefaults.empty() : recipeDefaults
        ).project();
    }

    private static final class ProjectionRun {
        private final GoalDescriptor goal;
        private final GoalVisibleAuthority authority;
        private final GoalProjectionOptions options;
        private final GoalChoiceResolution manualChoices;
        private final GoalRecipeDefaults recipeDefaults;
        private final LinkedHashMap<ItemIdentity, MutableAuthorityCount> remainingCounts = new LinkedHashMap<>();
        private final LinkedHashMap<String, GoalRecipeDescriptor> recipesById = new LinkedHashMap<>();
        private final LinkedHashMap<ItemIdentity, GoalRecipeDescriptor> recipesByOutput = new LinkedHashMap<>();
        private final LinkedHashMap<ItemIdentity, List<String>> producerIdsByOutput = new LinkedHashMap<>();
        private final ArrayList<GoalRequirement> requirements = new ArrayList<>();
        private final ArrayList<GoalChoiceRequirement> choices = new ArrayList<>();
        private final ArrayList<GoalProjectionEntry> entries = new ArrayList<>();
        private final LinkedHashMap<ItemIdentity, Integer> wantedCounts = new LinkedHashMap<>();
        private final LinkedHashSet<String> diagnostics = new LinkedHashSet<>();
        private boolean blocked;
        private boolean recipeBudgetReported;
        private boolean entryBudgetReported;
        private int sequence;
        private int recipeExpansions;

        private ProjectionRun(
                GoalDescriptor goal,
                GoalVisibleAuthority authority,
                GoalProjectionOptions options,
                GoalChoiceResolution manualChoices,
                GoalRecipeDefaults recipeDefaults
        ) {
            this.goal = goal;
            this.authority = authority;
            this.options = options;
            this.manualChoices = manualChoices;
            this.recipeDefaults = recipeDefaults;
            seedAuthority();
            seedRecipes();
        }

        private GoalProjection project() {
            if (goal == null) {
                diagnostic("missing_goal", "");
                blocked = true;
                return build("goal", "goal", 0);
            }
            GoalStackDescriptor targetOutput = goal.primaryTargetOutput();
            if (targetOutput == null) {
                diagnostic("goal_has_no_target_output", goal.goalId());
                blocked = true;
                return build(goal.goalId(), goal.label(), goal.targetCount());
            }
            GoalRecipeDescriptor root = rootRecipe(targetOutput.identity());
            if (root == null) {
                diagnostic("goal_has_no_root_recipe", targetOutput.identity().itemId());
                blocked = true;
                return build(goal.goalId(), goal.label(), resolvedTargetCount(targetOutput));
            }
            trace(
                    "start goal={} label={} target={} targetIdentity={} focusedRecipe={} rootRecipe={} recipes={} authority={}",
                    goal.goalId(),
                    goal.label(),
                    resolvedTargetCount(targetOutput),
                    targetOutput.identity().itemId(),
                    goal.focusedRecipeId(),
                    root.recipeId(),
                    recipesById.size(),
                    authorityToken());
            expandRecipe(
                    root,
                    targetOutput.identity(),
                    resolvedTargetCount(targetOutput),
                    List.of(goal.label()),
                    Set.of(),
                    0
            );
            return build(goal.goalId(), goal.label(), resolvedTargetCount(targetOutput));
        }

        private GoalProjection build(String goalId, String label, int targetCount) {
            GoalProjectionStatus status = blocked
                    ? GoalProjectionStatus.BLOCKED
                    : diagnostics.isEmpty()
                    ? GoalProjectionStatus.READY
                    : GoalProjectionStatus.READY_WITH_DIAGNOSTICS;
            SlotDebugLog.verboseLog(
                    "[goal] projected goal={} target={} status={} requirements={} entries={} choices={} wanted={} diagnostics={}",
                    goalId,
                    targetCount,
                    status,
                    requirements.size(),
                    entries.size(),
                    choices.size(),
                    wantedCounts.size(),
                    diagnostics);
            return new GoalProjection(
                    goalId,
                    label,
                    targetCount,
                    status,
                    requirements,
                    choices,
                    entries,
                    wantedCounts,
                    List.copyOf(diagnostics)
            );
        }

        private void seedAuthority() {
            for (Map.Entry<ItemIdentity, GoalAuthorityCount> entry : authority.countsByIdentity().entrySet()) {
                GoalAuthorityCount count = entry.getValue();
                ItemIdentity key = movableKey(entry.getKey());
                remainingCounts
                        .computeIfAbsent(key, ignored -> new MutableAuthorityCount(0, 0, 0))
                        .add(count);
            }
            trace("seeded authority identities={} counts={}", remainingCounts.size(), authorityToken());
        }

        private void seedRecipes() {
            if (goal == null) {
                return;
            }
            for (GoalRecipeDescriptor recipe : goal.recipes()) {
                recipesById.putIfAbsent(recipe.recipeId(), recipe);
                if (!recipe.supportsTree()) {
                    diagnostic("recipe_does_not_support_tree", recipe.recipeId());
                }
                for (String diagnostic : recipe.diagnostics()) {
                    diagnostic("recipe_diagnostic", recipe.recipeId() + ":" + diagnostic);
                }
                if (recipe.outputs().size() > 1) {
                    diagnostic("recipe_has_multiple_outputs", recipe.recipeId());
                }
                trace(
                        "seed recipe={} category={} tree={} outputs={} inputs={} catalysts={} diagnostics={}",
                        recipe.recipeId(),
                        recipe.categoryId(),
                        recipe.supportsTree(),
                        stackTokens(recipe.outputs()),
                        ingredientTokens(recipe.inputs()),
                        ingredientTokens(recipe.catalysts()),
                        recipe.diagnostics());
                for (GoalStackDescriptor output : recipe.outputs()) {
                    ItemIdentity outputKey = movableKey(output.identity());
                    GoalRecipeDescriptor previous = recipesByOutput.putIfAbsent(outputKey, recipe);
                    producerIdsByOutput
                            .computeIfAbsent(outputKey, ignored -> new ArrayList<>())
                            .add(recipe.recipeId());
                    if (previous != null && !previous.recipeId().equals(recipe.recipeId())) {
                        trace(
                                "producer collision output={} kept={} ignored={}",
                                output.identity().itemId(),
                                previous.recipeId(),
                                recipe.recipeId());
                    }
                }
            }
        }

        private GoalRecipeDescriptor rootRecipe(ItemIdentity targetOutput) {
            if (goal.focusedRecipeId() != null && !goal.focusedRecipeId().isBlank()) {
                GoalRecipeDescriptor focused = recipesById.get(goal.focusedRecipeId());
                if (focused != null) {
                    return focused;
                }
                diagnostic("focused_recipe_missing", goal.focusedRecipeId());
            }
            return recipeProducing(targetOutput);
        }

        private GoalRecipeDescriptor recipeProducing(ItemIdentity outputIdentity) {
            List<GoalRecipeDescriptor> producers = recipesProducing(outputIdentity);
            return producers.isEmpty() ? null : producers.get(0);
        }

        private List<GoalRecipeDescriptor> recipesProducing(ItemIdentity outputIdentity) {
            if (outputIdentity == null) {
                return List.of();
            }
            LinkedHashSet<String> producerIds = new LinkedHashSet<>();
            List<String> exactIds = producerIdsByOutput.get(movableKey(outputIdentity));
            if (exactIds != null) {
                producerIds.addAll(exactIds);
            }
            LinkedHashMap<String, GoalRecipeDescriptor> result = new LinkedHashMap<>();
            for (String producerId : producerIds) {
                GoalRecipeDescriptor recipe = recipesById.get(producerId);
                if (recipe != null) {
                    result.putIfAbsent(recipe.recipeId(), recipe);
                }
            }
            return List.copyOf(result.values());
        }

        private GoalRecipeDescriptor selectProducerRecipe(
                ItemIdentity outputIdentity,
                List<GoalRecipeDescriptor> producers,
                String choiceGroupId
        ) {
            if (producers == null || producers.isEmpty()) {
                return null;
            }
            String manualRecipeId = manualChoices.recipeChoiceFor(choiceGroupId);
            if (!manualRecipeId.isBlank()) {
                for (GoalRecipeDescriptor producer : producers) {
                    if (manualRecipeId.equals(producer.recipeId())) {
                        trace(
                                "manual producer choice output={} choice={} recipe={}",
                                outputIdentity == null ? "" : outputIdentity.itemId(),
                                choiceGroupId,
                                manualRecipeId);
                        return producer;
                    }
                }
                diagnostic("manual_recipe_choice_not_producer", choiceGroupId + ":" + manualRecipeId);
                trace(
                        "manual producer choice rejected output={} choice={} recipe={} producers={}",
                        outputIdentity == null ? "" : outputIdentity.itemId(),
                        choiceGroupId,
                        manualRecipeId,
                        recipeIds(producers));
                return null;
            }
            String defaultRecipeId = recipeDefaults.recipeChoiceFor(outputIdentity);
            if (!defaultRecipeId.isBlank()) {
                for (GoalRecipeDescriptor producer : producers) {
                    if (defaultRecipeId.equals(producer.recipeId())) {
                        trace(
                                "remembered producer default output={} choice={} recipe={}",
                                outputIdentity == null ? "" : outputIdentity.itemId(),
                                choiceGroupId,
                                defaultRecipeId);
                        return producer;
                    }
                }
                trace(
                        "remembered producer default not available output={} choice={} recipe={} producers={}",
                        outputIdentity == null ? "" : outputIdentity.itemId(),
                        choiceGroupId,
                        defaultRecipeId,
                        recipeIds(producers));
            }
            if (producers.size() == 1) {
                return producers.get(0);
            }
            ArrayList<GoalRecipeDescriptor> accessible = new ArrayList<>();
            for (GoalRecipeDescriptor producer : producers) {
                if (recipeInputsVisible(producer)) {
                    accessible.add(producer);
                }
            }
            if (accessible.size() == 1) {
                return accessible.get(0);
            }
            trace(
                    "producer selection unresolved output={} producers={} accessible={}",
                    outputIdentity == null ? "" : outputIdentity.itemId(),
                    recipeIds(producers),
                    recipeIds(accessible));
            return null;
        }

        private boolean recipeInputsVisible(GoalRecipeDescriptor recipe) {
            if (recipe == null || recipe.inputs().isEmpty()) {
                return false;
            }
            for (GoalIngredientDescriptor ingredient : recipe.inputs()) {
                if (!ingredientHasVisibleAlternative(ingredient)) {
                    return false;
                }
            }
            return true;
        }

        private boolean ingredientHasVisibleAlternative(GoalIngredientDescriptor ingredient) {
            if (ingredient == null || ingredient.alternatives().isEmpty()) {
                return false;
            }
            for (GoalStackDescriptor alternative : ingredient.alternatives()) {
                if (visibleCount(alternative.identity()) > 0) {
                    return true;
                }
            }
            return false;
        }

        private int visibleCount(ItemIdentity identity) {
            if (identity == null) {
                return 0;
            }
            MutableAuthorityCount exact = remainingCounts.get(movableKey(identity));
            if (exact != null) {
                return exact.total();
            }
            return 0;
        }

        private int resolvedTargetCount(GoalStackDescriptor targetOutput) {
            if (goal.targetCount() > 0) {
                return goal.targetCount();
            }
            return targetOutput == null ? 0 : Math.max(1, targetOutput.count());
        }

        private boolean expandRecipe(
                GoalRecipeDescriptor recipe,
                ItemIdentity requestedOutput,
                int requestedCount,
                List<String> breadcrumbs,
                Set<ItemIdentity> outputPath,
                int depth
        ) {
            if (recipe == null) {
                diagnostic("missing_recipe", requestedOutput == null ? "" : requestedOutput.itemId());
                blocked = true;
                return false;
            }
            if (++recipeExpansions > options.maxRecipeExpansions()) {
                if (!recipeBudgetReported) {
                    recipeBudgetReported = true;
                    diagnostic("goal_recipe_budget_exceeded", outputToken(requestedOutput, recipe));
                }
                blocked = true;
                return false;
            }
            if (depth > options.maxDepth()) {
                diagnostic("goal_depth_limit_exceeded", outputToken(requestedOutput, recipe));
                blocked = true;
                return false;
            }
            if (requestedOutput != null && containsMovable(outputPath, requestedOutput)) {
                diagnostic("goal_recipe_loop_detected", chainToken(breadcrumbs, requestedOutput));
                blocked = true;
                return false;
            }
            int outputCount = outputCount(recipe, requestedOutput);
            if (outputCount <= 0) {
                diagnostic("recipe_output_missing", recipe.recipeId());
                blocked = true;
                return false;
            }
            int batches = ceilDiv(Math.max(0, requestedCount), outputCount);
            if (batches <= 0) {
                return true;
            }
            trace(
                    "expand depth={} recipe={} requestedOutput={} requested={} outputCount={} batches={} breadcrumbs={}",
                    depth,
                    recipe.recipeId(),
                    requestedOutput == null ? "" : requestedOutput.itemId(),
                    requestedCount,
                    outputCount,
                    batches,
                    breadcrumbs);
            LinkedHashSet<ItemIdentity> nextPath = new LinkedHashSet<>(outputPath);
            if (requestedOutput != null) {
                nextPath.add(movableKey(requestedOutput));
            }
            for (GoalIngredientDescriptor ingredient : recipe.inputs()) {
                int required = ingredient.consumed()
                        ? multiplySaturated(ingredient.quantity(), batches)
                        : Math.max(0, ingredient.quantity());
                handleIngredient(recipe, ingredient, required, breadcrumbs, nextPath, depth);
            }
            return true;
        }

        private void handleIngredient(
                GoalRecipeDescriptor recipe,
                GoalIngredientDescriptor ingredient,
                int requiredCount,
                List<String> breadcrumbs,
                Set<ItemIdentity> outputPath,
                int depth
        ) {
            if (ingredient == null || requiredCount <= 0) {
                return;
            }
            trace(
                    "ingredient recipe={} id={} label={} required={} quantity={} alternatives={} choiceRequired={} diagnostics={}",
                    recipe.recipeId(),
                    ingredient.ingredientId(),
                    ingredient.label(),
                    requiredCount,
                    ingredient.quantity(),
                    stackTokens(ingredient.alternatives()),
                    ingredient.choiceRequired(),
                    ingredient.diagnostics());
            for (String diagnostic : ingredient.diagnostics()) {
                diagnostic("ingredient_diagnostic", recipe.recipeId() + ":" + ingredient.ingredientId() + ":" + diagnostic);
            }
            if (ingredient.chance() != 1.0D) {
                diagnostic("ingredient_has_chance", recipe.recipeId() + ":" + ingredient.ingredientId());
            }
            if (ingredient.alternatives().isEmpty()) {
                diagnostic("ingredient_has_no_alternatives", recipe.recipeId() + ":" + ingredient.ingredientId());
                blocked = true;
                ArrayList<String> localDiagnostics = new ArrayList<>();
                localDiagnostics.add("ingredient_has_no_alternatives");
                localDiagnostics.addAll(ingredient.diagnostics());
                addChoiceRequirement(recipe, ingredient, requiredCount, List.of(), breadcrumbs, localDiagnostics);
                return;
            }
            if (ingredient.choiceRequired() || ingredient.alternatives().size() > 1) {
                handleChoiceIngredient(recipe, ingredient, requiredCount, breadcrumbs, outputPath, depth);
                return;
            }
            addConcreteRequirement(
                    recipe,
                    ingredient,
                    ingredient.alternatives().get(0),
                    requiredCount,
                    false,
                    "",
                    breadcrumbs,
                    outputPath,
                    depth
            );
        }

        private void handleChoiceIngredient(
                GoalRecipeDescriptor recipe,
                GoalIngredientDescriptor ingredient,
                int requiredCount,
                List<String> breadcrumbs,
                Set<ItemIdentity> outputPath,
                int depth
        ) {
            String choiceGroupId = GoalChoiceKeys.ingredientChoiceGroupId(recipe.recipeId(), ingredient.ingredientId());
            ItemIdentity manual = manualChoices.choiceFor(ingredient.ingredientId(), choiceGroupId);
            if (manual != null) {
                trace(
                        "manual choice recipe={} choice={} selected={} required={}",
                        recipe.recipeId(),
                        choiceGroupId,
                        manual.itemId(),
                        requiredCount);
                GoalStackDescriptor selected = findAlternative(ingredient.alternatives(), manual);
                if (selected == null) {
                    diagnostic("manual_choice_not_in_ingredient", choiceGroupId + ":" + manual.itemId());
                    blocked = true;
                    addChoiceRequirement(recipe, ingredient, requiredCount, List.of(), breadcrumbs, List.of(
                            "manual_choice_not_in_ingredient"
                    ));
                    return;
                }
                addConcreteRequirement(recipe, ingredient, selected, requiredCount, true, choiceGroupId, breadcrumbs, outputPath, depth);
                return;
            }

            int unresolved = requiredCount;
            ArrayList<GoalResolvedChoice> resolved = new ArrayList<>();
            for (GoalStackDescriptor alternative : authority.visibleAlternativesInAuthorityOrder(ingredient.alternatives())) {
                if (unresolved <= 0) {
                    break;
                }
                int available = remainingCount(alternative.identity()).total();
                if (available <= 0) {
                    continue;
                }
                int allocation = Math.min(unresolved, available);
                addConcreteRequirement(recipe, ingredient, alternative, allocation, true, choiceGroupId, breadcrumbs, outputPath, depth);
                resolved.add(new GoalResolvedChoice(choiceGroupId, alternative.identity(), alternative.displayName(), allocation, false));
                unresolved -= allocation;
                trace(
                        "auto choice allocation recipe={} choice={} alternative={} available={} allocated={} remaining={}",
                        recipe.recipeId(),
                        choiceGroupId,
                        alternative.identity().itemId(),
                        available,
                        allocation,
                        unresolved);
            }
            if (unresolved > 0) {
                trace(
                        "choice unresolved recipe={} choice={} required={} unresolved={} alternatives={} resolved={}",
                        recipe.recipeId(),
                        choiceGroupId,
                        requiredCount,
                        unresolved,
                        stackTokens(ingredient.alternatives()),
                        resolved);
                addChoiceRequirement(recipe, ingredient, unresolved, resolved, breadcrumbs, ingredient.diagnostics());
            } else if (!resolved.isEmpty()) {
                SlotDebugLog.verboseLog(
                        "[goal] auto-resolved choice goal={} choice={} required={} resolved={} recipe={}",
                        goal == null ? "" : goal.goalId(),
                        choiceGroupId,
                        requiredCount,
                        resolved,
                        recipe.recipeId());
            }
        }

        private void addConcreteRequirement(
                GoalRecipeDescriptor recipe,
                GoalIngredientDescriptor ingredient,
                GoalStackDescriptor stack,
                int requiredCount,
                boolean choiceInvolved,
                String choiceGroupId,
                List<String> breadcrumbs,
                Set<ItemIdentity> outputPath,
                int depth
        ) {
            MutableAuthorityCount count = remainingCount(stack.identity());
            GoalAuthorityCount consumed = ingredient.consumed()
                    ? count.consume(requiredCount)
                    : count.peek(requiredCount);
            int visible = consumed.totalCount();
            int missing = Math.max(0, requiredCount - visible);
            List<String> requirementBreadcrumbs = appendBreadcrumb(breadcrumbs, stack.displayName());
            int wantedCount = missing <= 0 ? 0 : consumed.carriedCount() + missing;
            List<GoalRecipeDescriptor> producerRecipes = recipesProducing(stack.identity());
            String producerChoiceGroupId = GoalChoiceKeys.producerChoiceGroupId(
                    recipe.recipeId(),
                    ingredient.ingredientId(),
                    stack.identity());
            GoalRecipeDescriptor childRecipe = selectProducerRecipe(stack.identity(), producerRecipes, producerChoiceGroupId);
            ArrayList<String> localDiagnostics = new ArrayList<>(ingredient.diagnostics());
            List<String> producerIds = recipeIds(producerRecipes);
            boolean manualProducerChoice = !manualChoices.recipeChoiceFor(producerChoiceGroupId).isBlank();
            boolean defaultProducerChoice = recipeDefaults.hasRecipeChoice(stack.identity());
            if (producerIds.size() > 1) {
                localDiagnostics.add("producer_candidates=" + producerIds.size()
                        + ":selected=" + (childRecipe == null ? "" : childRecipe.recipeId()));
                if (childRecipe == null) {
                    localDiagnostics.add("producer_choice_required");
                }
            }
            if (!ingredient.consumed()) {
                localDiagnostics.add("ingredient_not_consumed");
            }
            boolean expandMissing = false;
            if (missing > 0) {
                if (childRecipe != null) {
                    if (depth >= options.maxDepth()) {
                        diagnostic("goal_depth_limit_exceeded", chainToken(requirementBreadcrumbs, stack.identity()));
                        blocked = true;
                    } else if (containsMovable(outputPath, stack.identity())) {
                        diagnostic("goal_recipe_loop_detected", chainToken(requirementBreadcrumbs, stack.identity()));
                        blocked = true;
                    } else if (outputCount(childRecipe, stack.identity()) <= 0) {
                        diagnostic("recipe_output_missing", childRecipe.recipeId());
                        blocked = true;
                    } else {
                        wantedCount = 0;
                        expandMissing = true;
                    }
                }
            }
            boolean producerChoiceRequired = missing > 0 && childRecipe == null && producerIds.size() > 1;
            trace(
                    "requirement recipe={} ingredient={} identity={} required={} consumed={} missing={} wanted={} childRecipe={} expandMissing={} choice={} breadcrumbs={} diagnostics={}",
                    recipe.recipeId(),
                    ingredient.ingredientId(),
                    stack.identity().itemId(),
                    requiredCount,
                    visible,
                    missing,
                    wantedCount,
                    childRecipe == null ? "" : childRecipe.recipeId(),
                    expandMissing,
                    choiceInvolved ? choiceGroupId : "",
                    requirementBreadcrumbs,
                    localDiagnostics);
            boolean anyChoiceInvolved = choiceInvolved || producerChoiceRequired;
            String resolvedChoiceGroupId = choiceInvolved && !choiceGroupId.isBlank()
                    ? choiceGroupId
                    : (producerChoiceRequired || manualProducerChoice || defaultProducerChoice) ? producerChoiceGroupId : "";
            if (!entryBudgetAvailable(recipe.recipeId() + ":" + ingredient.ingredientId())) {
                return;
            }
            GoalRequirement requirement = new GoalRequirement(
                    "goal_requirement_" + (++sequence),
                    recipe.recipeId(),
                    ingredient.ingredientId(),
                    childRecipe == null ? "" : childRecipe.recipeId(),
                    GoalRequirementKind.CONCRETE,
                    stack.identity(),
                    stack.displayName(),
                    requiredCount,
                    consumed.carriedCount(),
                    consumed.proximateStorageCount(),
                    consumed.elsewhereStorageCount(),
                    missing,
                    wantedCount,
                    anyChoiceInvolved,
                    resolvedChoiceGroupId,
                    requirementBreadcrumbs,
                    localDiagnostics
            );
            requirements.add(requirement);
            entries.add(GoalProjectionEntry.fromRequirement(requirement));
            if (wantedCount > 0) {
                wantedCounts.merge(stack.identity(), wantedCount, Integer::sum);
            }
            if (producerChoiceRequired) {
                addProducerChoiceRequirement(recipe, ingredient, stack, missing, requirementBreadcrumbs, producerRecipes);
            }
            if (expandMissing) {
                expandRecipe(childRecipe, stack.identity(), missing, requirementBreadcrumbs, outputPath, depth + 1);
            }
        }

        private void addProducerChoiceRequirement(
                GoalRecipeDescriptor parentRecipe,
                GoalIngredientDescriptor ingredient,
                GoalStackDescriptor stack,
                int unresolvedCount,
                List<String> breadcrumbs,
                List<GoalRecipeDescriptor> producerRecipes
        ) {
            String choiceGroupId = GoalChoiceKeys.producerChoiceGroupId(
                    parentRecipe.recipeId(),
                    ingredient.ingredientId(),
                    stack.identity());
            if (!entryBudgetAvailable(parentRecipe.recipeId() + ":" + ingredient.ingredientId() + ":producer")) {
                return;
            }
            ArrayList<String> localDiagnostics = new ArrayList<>();
            localDiagnostics.add("producer_choice_required");
            localDiagnostics.add("producer_candidates=" + String.join(",", recipeIds(producerRecipes)));
            GoalChoiceRequirement choice = new GoalChoiceRequirement(
                    choiceGroupId,
                    parentRecipe.recipeId(),
                    ingredient.ingredientId(),
                    "",
                    "Choose recipe for " + stack.displayName(),
                    unresolvedCount,
                    unresolvedCount,
                    stack.identity(),
                    List.of(),
                    List.of(),
                    breadcrumbs,
                    localDiagnostics
            );
            choices.add(choice);
            trace(
                    "producer choice unresolved recipe={} ingredient={} output={} unresolved={} producers={}",
                    parentRecipe.recipeId(),
                    ingredient.ingredientId(),
                    stack.identity().itemId(),
                    unresolvedCount,
                    recipeIds(producerRecipes));
        }

        private void addChoiceRequirement(
                GoalRecipeDescriptor recipe,
                GoalIngredientDescriptor ingredient,
                int unresolvedCount,
                List<GoalResolvedChoice> resolved,
                List<String> breadcrumbs,
                List<String> localDiagnostics
        ) {
            String choiceGroupId = GoalChoiceKeys.ingredientChoiceGroupId(recipe.recipeId(), ingredient.ingredientId());
            if (!entryBudgetAvailable(recipe.recipeId() + ":" + ingredient.ingredientId() + ":choice")) {
                return;
            }
            List<String> choiceBreadcrumbs = appendBreadcrumb(breadcrumbs, ingredient.label());
            GoalChoiceRequirement choice = new GoalChoiceRequirement(
                    choiceGroupId,
                    recipe.recipeId(),
                    ingredient.ingredientId(),
                    ingredient.serializedIngredient(),
                    ingredient.label(),
                    unresolvedCount,
                    unresolvedCount,
                    null,
                    ingredient.alternatives(),
                    resolved,
                    choiceBreadcrumbs,
                    localDiagnostics
            );
            choices.add(choice);
            entries.add(GoalProjectionEntry.fromChoice(choice));
        }

        private boolean entryBudgetAvailable(String detail) {
            if (requirements.size() + choices.size() < options.maxEntries()) {
                return true;
            }
            if (!entryBudgetReported) {
                entryBudgetReported = true;
                diagnostic("goal_entry_budget_exceeded", detail);
            }
            blocked = true;
            return false;
        }

        private GoalStackDescriptor findAlternative(List<GoalStackDescriptor> alternatives, ItemIdentity identity) {
            if (alternatives == null || identity == null) {
                return null;
            }
            for (GoalStackDescriptor alternative : alternatives) {
                if (alternative != null && ItemIdentityMatcher.matchesMovable(alternative.identity(), identity)) {
                    return alternative;
                }
            }
            return null;
        }

        private int outputCount(GoalRecipeDescriptor recipe, ItemIdentity requestedOutput) {
            if (recipe.outputs().isEmpty()) {
                return 0;
            }
            if (requestedOutput != null) {
                for (GoalStackDescriptor output : recipe.outputs()) {
                    if (ItemIdentityMatcher.matchesMovable(output.identity(), requestedOutput)) {
                        return Math.max(0, output.count());
                    }
                }
                return 0;
            }
            return Math.max(0, recipe.outputs().get(0).count());
        }

        private MutableAuthorityCount remainingCount(ItemIdentity identity) {
            ItemIdentity key = movableKey(identity);
            MutableAuthorityCount exact = remainingCounts.get(key);
            if (exact != null) {
                return exact;
            }
            MutableAuthorityCount created = new MutableAuthorityCount(0, 0, 0);
            remainingCounts.put(key, created);
            return created;
        }

        private boolean containsMovable(Set<ItemIdentity> identities, ItemIdentity identity) {
            if (identities == null || identities.isEmpty() || identity == null) {
                return false;
            }
            return identities.contains(movableKey(identity));
        }

        private ItemIdentity movableKey(ItemIdentity identity) {
            ItemIdentity normalized = ItemIdentityMatcher.normalizeMovable(identity);
            return normalized == null ? identity : normalized;
        }

        private List<String> recipeIds(List<GoalRecipeDescriptor> recipes) {
            if (recipes == null || recipes.isEmpty()) {
                return List.of();
            }
            ArrayList<String> ids = new ArrayList<>();
            for (GoalRecipeDescriptor recipe : recipes) {
                if (recipe != null) {
                    ids.add(recipe.recipeId());
                }
            }
            return List.copyOf(ids);
        }

        private List<String> appendBreadcrumb(List<String> breadcrumbs, String label) {
            ArrayList<String> result = new ArrayList<>();
            if (breadcrumbs != null) {
                for (String breadcrumb : breadcrumbs) {
                    if (breadcrumb != null && !breadcrumb.isBlank()) {
                        result.add(breadcrumb.trim());
                    }
                }
            }
            if (label != null && !label.isBlank()) {
                result.add(label.trim());
            }
            return List.copyOf(result);
        }

        private void diagnostic(String code, String detail) {
            String cleanCode = code == null || code.isBlank() ? "goal_projection_diagnostic" : code.trim();
            String cleanDetail = detail == null ? "" : detail.trim();
            diagnostics.add(cleanDetail.isBlank() ? cleanCode : cleanCode + ":" + cleanDetail);
            SlotDebugLog.verboseLog(
                    "[goal] diagnostic goal={} code={} detail={}",
                    goal == null ? "" : goal.goalId(),
                    cleanCode,
                    cleanDetail);
        }

        private void trace(String message, Object... args) {
            SlotDebugLog.verboseLog("[goal][resolve] " + message, args);
        }

        private String authorityToken() {
            if (remainingCounts.isEmpty()) {
                return "[]";
            }
            ArrayList<String> tokens = new ArrayList<>();
            for (Map.Entry<ItemIdentity, MutableAuthorityCount> entry : remainingCounts.entrySet()) {
                MutableAuthorityCount count = entry.getValue();
                tokens.add(entry.getKey().itemId() + "="
                        + count.carried + "/" + count.proximate + "/" + count.elsewhere);
            }
            return tokens.toString();
        }

        private String stackTokens(List<GoalStackDescriptor> stacks) {
            if (stacks == null || stacks.isEmpty()) {
                return "[]";
            }
            ArrayList<String> tokens = new ArrayList<>();
            for (GoalStackDescriptor stack : stacks) {
                if (stack == null || stack.identity() == null) {
                    continue;
                }
                tokens.add(stack.identity().itemId() + "x" + stack.count());
            }
            return tokens.toString();
        }

        private String ingredientTokens(List<GoalIngredientDescriptor> ingredients) {
            if (ingredients == null || ingredients.isEmpty()) {
                return "[]";
            }
            ArrayList<String> tokens = new ArrayList<>();
            for (GoalIngredientDescriptor ingredient : ingredients) {
                if (ingredient == null) {
                    continue;
                }
                tokens.add(ingredient.ingredientId() + "{label=" + ingredient.label()
                        + ",qty=" + ingredient.quantity()
                        + ",alts=" + ingredient.alternatives().size()
                        + ",choice=" + ingredient.choiceRequired()
                        + ",consumed=" + ingredient.consumed()
                        + ",tag=" + ingredient.tagOrListLabel()
                        + ",diagnostics=" + ingredient.diagnostics()
                        + "}");
            }
            return tokens.toString();
        }

        private String outputToken(ItemIdentity output, GoalRecipeDescriptor recipe) {
            if (output != null) {
                return output.itemId();
            }
            return recipe == null ? "" : recipe.recipeId();
        }

        private String chainToken(List<String> breadcrumbs, ItemIdentity identity) {
            ArrayList<String> chain = new ArrayList<>();
            if (breadcrumbs != null) {
                chain.addAll(breadcrumbs);
            }
            if (identity != null) {
                chain.add(identity.itemId());
            }
            return String.join(">", chain);
        }

        private int ceilDiv(int value, int divisor) {
            if (value <= 0 || divisor <= 0) {
                return 0;
            }
            return (value + divisor - 1) / divisor;
        }

        private int multiplySaturated(int left, int right) {
            long value = (long) Math.max(0, left) * (long) Math.max(0, right);
            return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
        }
    }

    private static final class MutableAuthorityCount {
        private int carried;
        private int proximate;
        private int elsewhere;

        private MutableAuthorityCount(int carried, int proximate, int elsewhere) {
            this.carried = Math.max(0, carried);
            this.proximate = Math.max(0, proximate);
            this.elsewhere = Math.max(0, elsewhere);
        }

        private void add(GoalAuthorityCount count) {
            if (count == null) {
                return;
            }
            carried = addSaturated(carried, count.carriedCount());
            proximate = addSaturated(proximate, count.proximateStorageCount());
            elsewhere = addSaturated(elsewhere, count.elsewhereStorageCount());
        }

        private int total() {
            return addSaturated(addSaturated(carried, proximate), elsewhere);
        }

        private GoalAuthorityCount consume(int requested) {
            int remaining = Math.max(0, requested);
            int fromCarried = Math.min(carried, remaining);
            carried -= fromCarried;
            remaining -= fromCarried;
            int fromProximate = Math.min(proximate, remaining);
            proximate -= fromProximate;
            remaining -= fromProximate;
            int fromElsewhere = Math.min(elsewhere, remaining);
            elsewhere -= fromElsewhere;
            return new GoalAuthorityCount(fromCarried, fromProximate, fromElsewhere);
        }

        private GoalAuthorityCount peek(int requested) {
            int remaining = Math.max(0, requested);
            int fromCarried = Math.min(carried, remaining);
            remaining -= fromCarried;
            int fromProximate = Math.min(proximate, remaining);
            remaining -= fromProximate;
            int fromElsewhere = Math.min(elsewhere, remaining);
            return new GoalAuthorityCount(fromCarried, fromProximate, fromElsewhere);
        }

        private int addSaturated(int left, int right) {
            long value = (long) Math.max(0, left) + (long) Math.max(0, right);
            return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
        }
    }
}
