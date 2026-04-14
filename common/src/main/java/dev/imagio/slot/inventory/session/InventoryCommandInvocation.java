package dev.imagio.slot.inventory.session;

import dev.imagio.slot.inventory.action.InventoryActionMode;
import dev.imagio.slot.inventory.action.InventoryCommandId;
import dev.imagio.slot.inventory.browse.InventoryBrowseSubjectRef;

public record InventoryCommandInvocation(
        InventorySessionToken sessionToken,
        InventoryBrowseSubjectRef subjectRef,
        InventoryCommandId commandId,
        InventoryActionMode mode,
        String origin
) {
    public InventoryCommandInvocation {
        sessionToken = sessionToken == null ? new InventorySessionToken("", 0L) : sessionToken;
        subjectRef = subjectRef == null ? null : InventoryBrowseSubjectRef.parse(subjectRef.stableKey());
        commandId = commandId == null ? InventoryCommandId.TRANSFER_STACK : commandId;
        mode = mode == null ? InventoryActionMode.EXECUTE : mode;
        origin = origin == null ? "" : origin;
    }
}
