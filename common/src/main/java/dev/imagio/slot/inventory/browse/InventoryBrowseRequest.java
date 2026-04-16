package dev.imagio.slot.inventory.browse;

import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.workflow.domain.WorkflowDomainSnapshot;
import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.function.Function;

public record InventoryBrowseRequest(
        InventoryAuthoritySnapshot authority,
        WorkflowDomainSnapshot workflow,
        InventoryBrowsePreferences preferences,
        InventoryBrowseSessionState sessionState,
        Function<InventoryEntrySnapshot, ItemIdentity> identityResolver
) {
    public InventoryBrowseRequest {
        authority = authority == null ? InventoryAuthoritySnapshot.empty() : authority;
        workflow = workflow == null ? WorkflowDomainSnapshot.empty() : workflow;
        preferences = preferences == null ? InventoryBrowsePreferences.defaults() : preferences;
        sessionState = sessionState == null ? InventoryBrowseSessionState.defaults(preferences) : sessionState;
        identityResolver = identityResolver == null
                ? entry -> entry == null || !entry.present() ? null : dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(entry.stack())
                : identityResolver;
    }
}
