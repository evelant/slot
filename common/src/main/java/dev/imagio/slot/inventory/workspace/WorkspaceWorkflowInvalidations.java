package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.workflow.domain.WorkflowDomainSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class WorkspaceWorkflowInvalidations {
    private WorkspaceWorkflowInvalidations() {
    }

    public static List<WorkspaceInvalidation> localizeSequenceOnlyInvalidations(
            WorkflowDomainSnapshot previousWorkflow,
            WorkflowDomainSnapshot currentWorkflow,
            List<WorkspaceInvalidation> invalidations
    ) {
        if (invalidations == null || invalidations.isEmpty()) {
            return List.of();
        }
        ArrayList<WorkspaceInvalidation> resolved = new ArrayList<>(invalidations.size());
        Boolean sequenceOnly = null;
        for (WorkspaceInvalidation invalidation : invalidations) {
            if (invalidation == null) {
                continue;
            }
            if (!localizableWorkflowSequence(invalidation)) {
                resolved.add(invalidation);
                continue;
            }
            if (sequenceOnly == null) {
                sequenceOnly = projectionInputsEqual(previousWorkflow, currentWorkflow);
            }
            resolved.add(sequenceOnly
                    ? WorkspaceInvalidation.frame(
                            WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED,
                            "workflow_sequence_no_projection_delta")
                    : invalidation);
        }
        return resolved.isEmpty() ? List.of() : List.copyOf(resolved);
    }

    private static boolean localizableWorkflowSequence(WorkspaceInvalidation invalidation) {
        return invalidation != null
                && invalidation.reason() == WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED
                && invalidation.requiresFullProjection();
    }

    private static boolean projectionInputsEqual(
            WorkflowDomainSnapshot previousWorkflow,
            WorkflowDomainSnapshot currentWorkflow
    ) {
        if (previousWorkflow == null || currentWorkflow == null) {
            return false;
        }
        return Objects.equals(previousWorkflow.workflowProjection(), currentWorkflow.workflowProjection())
                && Objects.equals(previousWorkflow.activityProjection(), currentWorkflow.activityProjection())
                && Objects.equals(previousWorkflow.browsePreferences(), currentWorkflow.browsePreferences())
                && Objects.equals(previousWorkflow.browseSessionState(), currentWorkflow.browseSessionState())
                && Objects.equals(previousWorkflow.craftRun(), currentWorkflow.craftRun())
                && Objects.equals(previousWorkflow.contextualSuggestions(), currentWorkflow.contextualSuggestions());
    }
}
