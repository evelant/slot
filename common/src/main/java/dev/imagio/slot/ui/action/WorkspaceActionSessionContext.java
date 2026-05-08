package dev.imagio.slot.ui.action;

public record WorkspaceActionSessionContext(
        String sessionId,
        int menuContainerId,
        long latestViewRevision
) {
    public WorkspaceActionSessionContext {
        sessionId = sessionId == null ? "" : sessionId;
        menuContainerId = Math.max(WorkspaceActionEnvelope.NO_MENU_CONTAINER, menuContainerId);
        latestViewRevision = Math.max(0L, latestViewRevision);
    }
}
