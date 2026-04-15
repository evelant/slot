package dev.imagio.slot.inventory.session;

import dev.imagio.slot.inventory.browse.InventoryBrowseDocument;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.workspace.InventoryWorkspaceComposer;
import dev.imagio.slot.inventory.workspace.InventoryWorkspaceModel;
import dev.imagio.slot.workflow.domain.WorkflowDomainSnapshot;

import java.util.List;
import java.util.Objects;

public record InventorySessionSnapshot(
        InventorySessionToken token,
        InventoryHostDescriptor host,
        InventoryAuthoritySnapshot authority,
        WorkflowDomainSnapshot workflow,
        InventoryBrowseDocument browseDocument,
        List<PendingInventoryAction> pendingActions,
        String diagnostics,
        InventoryWorkspaceModel workspaceModel
) {
    public InventorySessionSnapshot {
        token = token == null ? new InventorySessionToken("", 0L) : token;
        authority = authority == null ? InventoryAuthoritySnapshot.empty() : authority;
        workflow = workflow == null ? WorkflowDomainSnapshot.empty() : workflow;
        browseDocument = browseDocument == null
                ? new InventoryBrowseDocument(null, null, null, workflow.browseSessionState(), "")
                : browseDocument;
        pendingActions = pendingActions == null ? List.of() : List.copyOf(pendingActions.stream().filter(Objects::nonNull).toList());
        diagnostics = diagnostics == null ? "" : diagnostics;
        workspaceModel = workspaceModel == null
                ? InventoryWorkspaceModel.empty(token, host == null ? null : host.hostId())
                : workspaceModel;
    }

    public static InventorySessionSnapshot create(
            InventorySessionToken token,
            InventoryHostDescriptor host,
            InventoryAuthoritySnapshot authority,
            WorkflowDomainSnapshot workflow,
            InventoryBrowseDocument browseDocument,
            List<PendingInventoryAction> pendingActions,
            String diagnostics
    ) {
        InventorySessionSnapshot seed = new InventorySessionSnapshot(
                token,
                host,
                authority,
                workflow,
                browseDocument,
                pendingActions,
                diagnostics,
                InventoryWorkspaceModel.empty(token, host == null ? null : host.hostId())
        );
        return new InventorySessionSnapshot(
                token,
                host,
                authority,
                workflow,
                browseDocument,
                pendingActions,
                diagnostics,
                InventoryWorkspaceComposer.compose(seed)
        );
    }

    public static InventorySessionSnapshot empty() {
        return create(
                new InventorySessionToken("", 0L),
                null,
                InventoryAuthoritySnapshot.empty(),
                WorkflowDomainSnapshot.empty(),
                new InventoryBrowseDocument(null, null, null, WorkflowDomainSnapshot.empty().browseSessionState(), ""),
                List.of(),
                ""
        );
    }
}
