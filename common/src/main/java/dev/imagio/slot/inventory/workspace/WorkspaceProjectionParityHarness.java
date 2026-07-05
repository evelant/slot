package dev.imagio.slot.inventory.workspace;

import java.util.Collection;

/**
 * Test/support harness for proving an invalidated projection matches a fresh
 * full projection oracle for the same request.
 */
public final class WorkspaceProjectionParityHarness {
    public Result compareAfterSeed(
            WorkspaceProjectionRequest seedRequest,
            WorkspaceProjectionRequest changedRequest,
            Collection<WorkspaceInvalidation> invalidations
    ) {
        WorkspaceProjectionSessionCache incremental = new WorkspaceProjectionSessionCache();
        incremental.project(seedRequest, WorkspaceInvalidation.full(
                WorkspaceInvalidation.Reason.SESSION_OPEN,
                "parity_seed"));
        WorkspaceProjectionResult invalidated = incremental.project(changedRequest, invalidations);

        WorkspaceProjectionSessionCache oracle = new WorkspaceProjectionSessionCache();
        WorkspaceProjectionResult full = oracle.project(changedRequest, WorkspaceInvalidation.full(
                WorkspaceInvalidation.Reason.PARITY_ORACLE,
                "fresh_full_projection"));
        return new Result(
                invalidated.contentFingerprint().equals(full.contentFingerprint()),
                invalidated,
                full);
    }

    public record Result(
            boolean matches,
            WorkspaceProjectionResult invalidated,
            WorkspaceProjectionResult full
    ) {
        public Result {
            invalidated = invalidated == null
                    ? new WorkspaceProjectionResult(null, "", null)
                    : invalidated;
            full = full == null ? new WorkspaceProjectionResult(null, "", null) : full;
        }
    }
}
