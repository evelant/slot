package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.action.InventoryActionOutcome;
import dev.imagio.slot.inventory.action.InventoryActionRequest;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

public record WorkspaceTransferFeedback(
        String status,
        String diagnostics
) {
    public WorkspaceTransferFeedback {
        status = status == null || status.isBlank() ? "rejected" : status;
        diagnostics = diagnostics == null ? "" : diagnostics;
    }

    public static WorkspaceTransferFeedback rejected(String diagnostics) {
        return new WorkspaceTransferFeedback("transfer rejected", diagnostics);
    }

    public boolean appliedCompletely() {
        return "transfer applied".equals(status);
    }

    public static WorkspaceTransferFeedback interpret(
            InventoryActionRequest request,
            InventoryActionOutcome outcome
    ) {
        if (outcome == null) {
            return rejected("transfer_failed");
        }
        if (!outcome.successful()) {
            return rejected(outcome.diagnostics().isBlank()
                    ? outcome.status().name().toLowerCase(Locale.ROOT)
                    : outcome.diagnostics());
        }

        int requestedCount = request == null || request.requestedCount() <= 0
                ? outcome.requestedCount()
                : request.requestedCount();
        int movedCount = movedCount(request, outcome.stackRemainder(), requestedCount);
        if (requestedCount > 0 && movedCount <= 0) {
            return rejected("destination_full_or_incompatible");
        }
        if (outcome.stackRemainder() != null && !outcome.stackRemainder().isEmpty()) {
            return new WorkspaceTransferFeedback("partial transfer applied", "remainder:" + outcome.stackRemainder().getCount());
        }
        return new WorkspaceTransferFeedback("transfer applied", "");
    }

    private static int movedCount(InventoryActionRequest request, ItemStack remainder, int requestedCount) {
        if (requestedCount <= 0 || remainder == null || remainder.isEmpty()) {
            return Math.max(0, requestedCount);
        }
        if (request == null || request.identity() == null || ItemIdentityMatcher.matchesMovable(remainder, request.identity())) {
            return Math.max(0, requestedCount - remainder.getCount());
        }
        return requestedCount;
    }
}
