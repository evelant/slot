package dev.imagio.slot.ui.action;

public interface WorkspaceActionChannel {
    boolean send(WorkspaceActionId action, Object... arguments);
}
