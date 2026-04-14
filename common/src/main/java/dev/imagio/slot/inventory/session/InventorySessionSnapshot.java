package dev.imagio.slot.inventory.session;

import dev.imagio.slot.inventory.browse.InventoryBrowseDocument;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
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
        String diagnostics
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
    }

    public static InventorySessionSnapshot empty() {
        return new InventorySessionSnapshot(
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
