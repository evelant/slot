package dev.imagio.slot.neoforge.client.screen;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.inventory.action.InventoryActionMode;
import dev.imagio.slot.inventory.action.InventoryCommandId;
import dev.imagio.slot.inventory.browse.InventoryBrowseSubjectRef;
import dev.imagio.slot.inventory.intent.InventoryBrowseIntent;
import dev.imagio.slot.inventory.session.InventoryCommandInvocation;
import dev.imagio.slot.inventory.session.InventoryIntentRouter;
import dev.imagio.slot.inventory.session.InventoryRoutingStatus;
import dev.imagio.slot.inventory.session.InventorySessionCoordinator;
import dev.imagio.slot.inventory.session.InventorySessionSnapshot;

public final class SlotWorkspaceInputMapper {
    private final InventorySessionCoordinator coordinator;
    private final InventoryIntentRouter router;

    public SlotWorkspaceInputMapper(InventorySessionCoordinator coordinator, InventoryIntentRouter router) {
        this.coordinator = coordinator;
        this.router = router;
    }

    public boolean selectBrowseSubject(InventoryBrowseSubjectRef subjectRef, String origin) {
        if (coordinator == null || router == null || subjectRef == null) {
            return false;
        }
        InventorySessionSnapshot snapshot = coordinator.snapshot();
        var result = router.route(snapshot.token(), new InventoryBrowseIntent.SelectSubject(subjectRef, origin));
        logResult("select", result.status(), result.diagnostics());
        return result.status() != InventoryRoutingStatus.REJECTED;
    }

    public boolean invokeBrowseCommand(
            InventoryBrowseSubjectRef subjectRef,
            InventoryCommandId commandId,
            InventoryActionMode mode,
            String origin
    ) {
        if (coordinator == null || router == null || subjectRef == null || commandId == null) {
            return false;
        }
        var result = router.route(new InventoryCommandInvocation(
                coordinator.sessionToken(),
                subjectRef,
                commandId,
                mode,
                origin
        ));
        logResult("command:" + commandId, result.status(), result.diagnostics());
        return result.status() != InventoryRoutingStatus.REJECTED;
    }

    private static void logResult(String action, InventoryRoutingStatus status, String diagnostics) {
        if (status == InventoryRoutingStatus.REJECTED || (diagnostics != null && !diagnostics.isBlank())) {
            SlotDebugLog.log("Workspace input {} -> {} {}", action, status, diagnostics == null ? "" : diagnostics);
        }
    }
}
