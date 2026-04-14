package dev.imagio.slot.client.screen;

import dev.imagio.slot.projection.InventoryPane;
import dev.imagio.slot.projection.InventoryViewData;
import dev.imagio.slot.network.CursorTransferPayload;
import net.minecraft.network.chat.Component;

public final class InventoryCursorActionSupport {
    private static final RequestedCursorAction NOT_REQUESTED = new RequestedCursorAction(
            CursorTransferService.CursorTransferOutcome.notRequested(),
            SlotActionResult.NONE
    );

    private InventoryCursorActionSupport() {
    }

    public static RequestedCursorAction drop(int containerId, InventoryPane pane, boolean singleItem) {
        CursorTransferService.CursorTransferOutcome outcome = CursorTransferService.requestDrop(containerId, pane, singleItem);
        if (!outcome.requested()) {
            return NOT_REQUESTED;
        }
        return new RequestedCursorAction(
                outcome,
                SlotActionResult.requested(Component.translatable(outcome.dropFeedbackKey()))
        );
    }

    public static RequestedCursorAction pickup(
            int containerId,
            InventoryPane pane,
            InventoryViewData.EntryView entry,
            CursorTransferPayload.Mode mode
    ) {
        if (entry == null || entry.itemEntry() == null || entry.itemEntry().identity() == null) {
            return NOT_REQUESTED;
        }

        CursorTransferService.CursorTransferOutcome outcome = CursorTransferService.requestPickup(
                containerId,
                pane,
                entry.itemEntry().identity(),
                mode
        );
        if (!outcome.requested()) {
            return NOT_REQUESTED;
        }
        return new RequestedCursorAction(
                outcome,
                SlotActionResult.requested(Component.translatable(
                        outcome.pickupFeedbackKey(),
                        entry.displayStack().getHoverName()
                ))
        );
    }

    public static RequestedCursorAction trash(int containerId, boolean singleItem) {
        CursorTransferService.CursorTransferOutcome outcome = CursorTransferService.requestTrash(containerId, singleItem);
        if (!outcome.requested()) {
            return NOT_REQUESTED;
        }
        return new RequestedCursorAction(
                outcome,
                SlotActionResult.requested(Component.translatable(outcome.trashFeedbackKey()))
        );
    }

    public record RequestedCursorAction(
            CursorTransferService.CursorTransferOutcome outcome,
            SlotActionResult requestedResult
    ) {
        public boolean requested() {
            return outcome != null && outcome.requested();
        }

        public boolean suppressPositiveDeltas() {
            return outcome != null && outcome.suppressPositiveDeltas();
        }

        public String modeLabel() {
            return outcome == null ? "" : outcome.modeLabel();
        }
    }
}
