package dev.imagio.slot.ui.action;

public final class WorkspaceActionSessionValidator {
    private WorkspaceActionSessionValidator() {
    }

    public static WorkspaceActionValidation validate(
            WorkspaceActionEnvelope envelope,
            WorkspaceActionSessionContext current
    ) {
        if (envelope == null) {
            return WorkspaceActionValidation.rejected("missing_envelope");
        }
        if (envelope.sessionId().isBlank()) {
            return WorkspaceActionValidation.rejected("missing_session_id");
        }
        if (current == null || current.sessionId().isBlank()) {
            return WorkspaceActionValidation.rejected("session_unavailable");
        }
        if (!current.sessionId().equals(envelope.sessionId())) {
            return WorkspaceActionValidation.rejected("stale_session");
        }
        if (current.menuContainerId() != envelope.menuContainerId()) {
            return WorkspaceActionValidation.rejected(
                    "wrong_menu:expected=" + current.menuContainerId()
                            + ":actual=" + envelope.menuContainerId()
            );
        }
        return WorkspaceActionValidation.ok();
    }
}
