package dev.imagio.slot.ui.action;

public record WorkspaceActionEnvelope(
        String sessionId,
        int menuContainerId,
        long viewRevision
) {
    public static final int NO_MENU_CONTAINER = -1;

    public WorkspaceActionEnvelope {
        sessionId = sessionId == null ? "" : sessionId;
        menuContainerId = Math.max(NO_MENU_CONTAINER, menuContainerId);
        viewRevision = Math.max(0L, viewRevision);
    }
}
