package dev.imagio.slot.inventory.integration;

public record ToolActionResult(
        boolean successful,
        String diagnostics
) {
    public ToolActionResult {
        diagnostics = diagnostics == null ? "" : diagnostics;
    }

    public static ToolActionResult success() {
        return new ToolActionResult(true, "");
    }

    public static ToolActionResult blocked(String diagnostics) {
        return new ToolActionResult(false, diagnostics);
    }
}
