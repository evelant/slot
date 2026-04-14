package dev.imagio.slot.client.screen;

public record QuickAccessInventoryActionResult(boolean started, SlotActionResult feedback) {
    public static final QuickAccessInventoryActionResult NONE =
            new QuickAccessInventoryActionResult(false, SlotActionResult.NONE);

    static QuickAccessInventoryActionResult started(SlotActionResult feedback) {
        return new QuickAccessInventoryActionResult(true, feedback == null ? SlotActionResult.NONE : feedback);
    }
}
