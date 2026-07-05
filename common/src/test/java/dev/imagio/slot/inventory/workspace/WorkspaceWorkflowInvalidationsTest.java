package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.workflow.domain.CraftRunState;
import dev.imagio.slot.workflow.domain.WorkflowDomainSnapshot;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkspaceWorkflowInvalidationsTest {
    @Test
    void sequenceOnlyWorkflowChangeBecomesFrameOnly() {
        WorkspaceInvalidation original = workflowFull();
        List<WorkspaceInvalidation> localized = WorkspaceWorkflowInvalidations.localizeSequenceOnlyInvalidations(
                WorkflowDomainSnapshot.empty(),
                withSequence(WorkflowDomainSnapshot.empty(), 2L),
                List.of(original));

        WorkspaceInvalidation invalidation = single(localized);
        assertFalse(invalidation.requiresFullProjection());
        assertTrue(invalidation.identities().isEmpty());
        assertTrue(invalidation.storageIds().isEmpty());
        assertEquals(java.util.EnumSet.of(WorkspaceProjectionSlice.FRAME), invalidation.slices());
        assertEquals("workflow_sequence_no_projection_delta", invalidation.diagnostics());
    }

    @Test
    void visibleWorkflowChangeKeepsOriginalFullInvalidation() {
        WorkspaceInvalidation original = workflowFull();
        List<WorkspaceInvalidation> localized = WorkspaceWorkflowInvalidations.localizeSequenceOnlyInvalidations(
                WorkflowDomainSnapshot.empty(),
                withCraftRunRevision(WorkflowDomainSnapshot.empty(), 1),
                List.of(original));

        assertSame(original, single(localized));
    }

    @Test
    void missingPreviousWorkflowKeepsOriginalFullInvalidation() {
        WorkspaceInvalidation original = workflowFull();
        List<WorkspaceInvalidation> localized = WorkspaceWorkflowInvalidations.localizeSequenceOnlyInvalidations(
                null,
                WorkflowDomainSnapshot.empty(),
                List.of(original));

        assertSame(original, single(localized));
    }

    private static WorkspaceInvalidation single(List<WorkspaceInvalidation> invalidations) {
        assertEquals(1, invalidations.size());
        return invalidations.get(0);
    }

    private static WorkspaceInvalidation workflowFull() {
        return WorkspaceInvalidation.full(
                WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED,
                "workflow_sequence_changed_not_localized");
    }

    private static WorkflowDomainSnapshot withSequence(WorkflowDomainSnapshot source, long sequence) {
        WorkflowDomainSnapshot resolved = source == null ? WorkflowDomainSnapshot.empty() : source;
        return new WorkflowDomainSnapshot(
                sequence,
                resolved.workflowProjection(),
                resolved.workflowEvents(),
                resolved.activityProjection(),
                resolved.activityEvents(),
                resolved.browsePreferences(),
                resolved.browseSessionState(),
                resolved.craftRun(),
                resolved.contextualSuggestions());
    }

    private static WorkflowDomainSnapshot withCraftRunRevision(WorkflowDomainSnapshot source, int revision) {
        WorkflowDomainSnapshot resolved = source == null ? WorkflowDomainSnapshot.empty() : source;
        return new WorkflowDomainSnapshot(
                resolved.nextGlobalSequence() + 1L,
                resolved.workflowProjection(),
                resolved.workflowEvents(),
                resolved.activityProjection(),
                resolved.activityEvents(),
                resolved.browsePreferences(),
                resolved.browseSessionState(),
                new CraftRunState(revision, "", List.of()),
                resolved.contextualSuggestions());
    }
}
