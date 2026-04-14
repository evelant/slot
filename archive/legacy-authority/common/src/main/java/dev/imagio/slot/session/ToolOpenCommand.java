package dev.imagio.slot.session;

public record ToolOpenCommand(
        String providerId,
        String toolId,
        int tabId
) {
    public ToolOpenCommand {
        providerId = providerId == null ? "" : providerId;
        toolId = toolId == null ? "" : toolId;
        tabId = Math.max(-1, tabId);
    }

    public boolean present() {
        return !providerId.isBlank() && !toolId.isBlank() && tabId >= 0;
    }
}
