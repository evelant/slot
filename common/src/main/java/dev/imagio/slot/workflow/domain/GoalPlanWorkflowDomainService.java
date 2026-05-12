package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.goal.GoalPlanState;

import java.util.List;

/**
 * Durable player-authored EMI goal tabs. Captured goal descriptors are large
 * enough that the client may edit them locally while choosing recipes, but the
 * workflow projection is the reconnect-safe source of truth.
 */
public final class GoalPlanWorkflowDomainService {
    private final WorkflowDomainStateRepository repository;
    private final Runnable mutationObserver;

    public GoalPlanWorkflowDomainService(
            WorkflowDomainStateRepository repository,
            Runnable mutationObserver
    ) {
        this.repository = repository;
        this.mutationObserver = mutationObserver == null ? () -> {} : mutationObserver;
    }

    public List<GoalPlanState> all() {
        return repository.workflowProjection().goalPlans();
    }

    public boolean save(GoalPlanState goal) {
        return save(goal, DomainEventMetadata.origin("workflow.goal_plan.save"));
    }

    public boolean save(GoalPlanState goal, DomainEventMetadata metadata) {
        if (goal == null || goal.goalId().isBlank() || goal.descriptor() == null) {
            return false;
        }
        GoalPlanState current = get(goal.goalId());
        if (goal.equals(current)) {
            return false;
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.GoalPlanSaved(goal),
                (metadata == null ? DomainEventMetadata.origin("") : metadata)
                        .withOrigin("workflow.goal_plan.save")
        );
        mutationObserver.run();
        return true;
    }

    public boolean remove(String goalId) {
        return remove(goalId, DomainEventMetadata.origin("workflow.goal_plan.remove"));
    }

    public boolean remove(String goalId, DomainEventMetadata metadata) {
        String id = clean(goalId);
        if (id.isBlank() || get(id) == null) {
            return false;
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.GoalPlanRemoved(id),
                (metadata == null ? DomainEventMetadata.origin("") : metadata)
                        .withOrigin("workflow.goal_plan.remove")
        );
        mutationObserver.run();
        return true;
    }

    private GoalPlanState get(String goalId) {
        String id = clean(goalId);
        if (id.isBlank()) {
            return null;
        }
        for (GoalPlanState goal : repository.workflowProjection().goalPlans()) {
            if (goal.goalId().equals(id)) {
                return goal;
            }
        }
        return null;
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
