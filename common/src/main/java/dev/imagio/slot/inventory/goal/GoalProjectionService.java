package dev.imagio.slot.inventory.goal;

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
        return project(goal, authority, GoalProjectionOptions.defaults(), GoalChoiceResolution.empty());
    }

    public GoalProjection project(
            GoalDescriptor goal,
            GoalVisibleAuthority authority,
            GoalProjectionOptions options
    ) {
        return project(goal, authority, options, GoalChoiceResolution.empty());
    }

    public GoalProjection project(
            GoalDescriptor goal,
            GoalVisibleAuthority authority,
            GoalProjectionOptions options,
            GoalChoiceResolution manualChoices
    ) {
        return new ProjectionRun(
                goal,
                authority == null ? GoalVisibleAuthority.empty() : authority,
                options == null ? GoalProjectionOptions.defaults() : options,
                manualChoices == null ? GoalChoiceResolution.empty() : manualChoices
        ).project();
    }

    private static final class ProjectionRun {
        private final GoalDescriptor goal;
        private final GoalVisibleAuthority authority;
        private final GoalProjectionOptions options;
        private final GoalChoiceResolution manualChoices;
        private final LinkedHashMap<ItemIdentity, MutableAuthorityCount> remainingCounts = new LinkedHashMap<>();
        private final LinkedHashMap<String, GoalRecipeDescriptor> recipesById = new LinkedHashMap<>();
        private final LinkedHashMap<ItemIdentity, GoalRecipeDescriptor> recipesByOutput = new LinkedHashMap<>();
        private final ArrayList<GoalRequirement> requirements = new ArrayList<>();
        private final ArrayList<GoalChoiceRequirement> choices = new ArrayList<>();
        private final ArrayList<GoalProjectionEntry> entries = new ArrayList<>();
        private final LinkedHashMap<ItemIdentity, Integer> desiredCounts = new LinkedHashMap<>();
        private final LinkedHashSet<String> diagnostics = new LinkedHashSet<>();
        private boolean blocked;
        private int sequence;

        private ProjectionRun(
                GoalDescriptor goal,
                GoalVisibleAuthority authority,
                GoalProjectionOptions options,
                GoalChoiceResolution manualChoices
        ) {
            this.goal = goal;
            this.authority = authority;
            this.options = options;
            this.manualChoices = manualChoices;
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
            return new GoalProjection(
                    goalId,
                    label,
                    targetCount,
                    status,
                    requirements,
                    choices,
                    entries,
                    desiredCounts,
                    List.copyOf(diagnostics)
            );
        }

        private void seedAuthority() {
            for (Map.Entry<ItemIdentity, GoalAuthorityCount> entry : authority.countsByIdentity().entrySet()) {
                GoalAuthorityCount count = entry.getValue();
                remainingCounts.put(entry.getKey(), new MutableAuthorityCount(
                        count.carriedCount(),
                        count.proximateStorageCount(),
                        count.elsewhereStorageCount()
                ));
            }
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
                for (GoalStackDescriptor output : recipe.outputs()) {
                    recipesByOutput.putIfAbsent(output.identity(), recipe);
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
            if (outputIdentity == null) {
                return null;
            }
            GoalRecipeDescriptor exact = recipesByOutput.get(outputIdentity);
            if (exact != null) {
                return exact;
            }
            for (Map.Entry<ItemIdentity, GoalRecipeDescriptor> entry : recipesByOutput.entrySet()) {
                if (ItemIdentityMatcher.matchesMovable(entry.getKey(), outputIdentity)) {
                    return entry.getValue();
                }
            }
            return null;
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
            LinkedHashSet<ItemIdentity> nextPath = new LinkedHashSet<>(outputPath);
            if (requestedOutput != null) {
                nextPath.add(requestedOutput);
            }
            for (GoalIngredientDescriptor ingredient : recipe.inputs()) {
                int required = multiplySaturated(ingredient.quantity(), batches);
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
            for (String diagnostic : ingredient.diagnostics()) {
                diagnostic("ingredient_diagnostic", recipe.recipeId() + ":" + ingredient.ingredientId() + ":" + diagnostic);
            }
            if (ingredient.chance() != 1.0D) {
                diagnostic("ingredient_has_chance", recipe.recipeId() + ":" + ingredient.ingredientId());
            }
            if (ingredient.alternatives().isEmpty()) {
                diagnostic("ingredient_has_no_alternatives", recipe.recipeId() + ":" + ingredient.ingredientId());
                blocked = true;
                addChoiceRequirement(recipe, ingredient, requiredCount, List.of(), breadcrumbs, List.of(
                        "ingredient_has_no_alternatives"
                ));
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
            String choiceGroupId = choiceGroupId(recipe, ingredient);
            ItemIdentity manual = manualChoices.choiceFor(ingredient.ingredientId(), choiceGroupId);
            if (manual != null) {
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
            }
            if (unresolved > 0) {
                addChoiceRequirement(recipe, ingredient, unresolved, resolved, breadcrumbs, List.of());
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
            GoalAuthorityCount consumed = count.consume(requiredCount);
            int visible = consumed.totalCount();
            int missing = Math.max(0, requiredCount - visible);
            List<String> requirementBreadcrumbs = appendBreadcrumb(breadcrumbs, stack.displayName());
            int desiredCount = missing;
            GoalRecipeDescriptor childRecipe = null;
            boolean expandMissing = false;
            if (missing > 0) {
                childRecipe = recipeProducing(stack.identity());
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
                        desiredCount = 0;
                        expandMissing = true;
                    }
                }
            }
            GoalRequirement requirement = new GoalRequirement(
                    "goal_requirement_" + (++sequence),
                    recipe.recipeId(),
                    ingredient.ingredientId(),
                    GoalRequirementKind.CONCRETE,
                    stack.identity(),
                    stack.displayName(),
                    requiredCount,
                    consumed.carriedCount(),
                    consumed.proximateStorageCount(),
                    consumed.elsewhereStorageCount(),
                    missing,
                    desiredCount,
                    choiceInvolved,
                    choiceGroupId,
                    requirementBreadcrumbs,
                    List.of()
            );
            requirements.add(requirement);
            entries.add(GoalProjectionEntry.fromRequirement(requirement));
            if (desiredCount > 0) {
                desiredCounts.merge(stack.identity(), desiredCount, Integer::sum);
            }
            if (expandMissing) {
                expandRecipe(childRecipe, stack.identity(), missing, requirementBreadcrumbs, outputPath, depth + 1);
            }
        }

        private void addChoiceRequirement(
                GoalRecipeDescriptor recipe,
                GoalIngredientDescriptor ingredient,
                int unresolvedCount,
                List<GoalResolvedChoice> resolved,
                List<String> breadcrumbs,
                List<String> localDiagnostics
        ) {
            String choiceGroupId = choiceGroupId(recipe, ingredient);
            List<String> choiceBreadcrumbs = appendBreadcrumb(breadcrumbs, ingredient.label());
            GoalChoiceRequirement choice = new GoalChoiceRequirement(
                    choiceGroupId,
                    recipe.recipeId(),
                    ingredient.ingredientId(),
                    ingredient.label(),
                    unresolvedCount,
                    unresolvedCount,
                    ingredient.alternatives(),
                    resolved,
                    choiceBreadcrumbs,
                    localDiagnostics
            );
            choices.add(choice);
            entries.add(GoalProjectionEntry.fromChoice(choice));
        }

        private String choiceGroupId(GoalRecipeDescriptor recipe, GoalIngredientDescriptor ingredient) {
            return recipe.recipeId() + "#" + ingredient.ingredientId();
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
            MutableAuthorityCount exact = remainingCounts.get(identity);
            if (exact != null) {
                return exact;
            }
            for (Map.Entry<ItemIdentity, MutableAuthorityCount> entry : remainingCounts.entrySet()) {
                if (ItemIdentityMatcher.matchesMovable(entry.getKey(), identity)) {
                    return entry.getValue();
                }
            }
            MutableAuthorityCount created = new MutableAuthorityCount(0, 0, 0);
            remainingCounts.put(identity, created);
            return created;
        }

        private boolean containsMovable(Set<ItemIdentity> identities, ItemIdentity identity) {
            if (identities == null || identities.isEmpty() || identity == null) {
                return false;
            }
            for (ItemIdentity existing : identities) {
                if (ItemIdentityMatcher.matchesMovable(existing, identity)) {
                    return true;
                }
            }
            return false;
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

        private int total() {
            return carried + proximate + elsewhere;
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
    }
}
