package dev.imagio.slot.ui.action;

public record WorkspaceActionValidation(boolean valid, String diagnostics) {
    public static WorkspaceActionValidation ok() {
        return new WorkspaceActionValidation(true, "");
    }

    public static WorkspaceActionValidation rejected(String diagnostics) {
        return new WorkspaceActionValidation(false, diagnostics == null ? "" : diagnostics);
    }
}
