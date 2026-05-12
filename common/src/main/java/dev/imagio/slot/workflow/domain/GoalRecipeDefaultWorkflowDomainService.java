package dev.imagio.slot.workflow.domain;

import java.util.Map;

/**
 * Durable player preference for producer recipes selected while resolving EMI
 * goal choices. Keying by output item id intentionally makes the preference
 * reusable across future goals and reconnects.
 */
public final class GoalRecipeDefaultWorkflowDomainService {
    private final WorkflowDomainStateRepository repository;
    private final Runnable mutationObserver;

    public GoalRecipeDefaultWorkflowDomainService(
            WorkflowDomainStateRepository repository,
            Runnable mutationObserver
    ) {
        this.repository = repository;
        this.mutationObserver = mutationObserver == null ? () -> {} : mutationObserver;
    }

    public Map<String, String> all() {
        return repository.workflowProjection().goalRecipeDefaults();
    }

    public String get(String outputItemId) {
        String key = clean(outputItemId);
        return key.isBlank() ? "" : repository.workflowProjection().goalRecipeDefaults().getOrDefault(key, "");
    }

    public boolean set(String outputItemId, String recipeId) {
        return set(outputItemId, recipeId, DomainEventMetadata.origin("workflow.goal_recipe_default.set"));
    }

    public boolean set(String outputItemId, String recipeId, DomainEventMetadata metadata) {
        String key = clean(outputItemId);
        String value = clean(recipeId);
        if (key.isBlank()) {
            return false;
        }
        String current = get(key);
        if (current.equals(value)) {
            return false;
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.GoalRecipeDefaultSet(key, value),
                (metadata == null ? DomainEventMetadata.origin("") : metadata)
                        .withOrigin("workflow.goal_recipe_default.set")
        );
        mutationObserver.run();
        return true;
    }

    public boolean clear(String outputItemId) {
        return set(outputItemId, "", DomainEventMetadata.origin("workflow.goal_recipe_default.clear"));
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
