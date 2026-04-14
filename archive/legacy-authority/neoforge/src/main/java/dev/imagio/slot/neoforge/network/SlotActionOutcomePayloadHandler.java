package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.client.screen.RecentLootTracker;
import dev.imagio.slot.client.screen.QuickAccessFollowUpState;
import dev.imagio.slot.client.screen.QuickAccessPendingState;
import dev.imagio.slot.client.screen.SlotActionRequestRoutingState;
import dev.imagio.slot.client.screen.SlotActionOutcomeState;
import dev.imagio.slot.client.screen.SlotActionResult;
import dev.imagio.slot.client.screen.SlotUndoHistory;
import dev.imagio.slot.intent.ActionFamily;
import dev.imagio.slot.network.SlotActionOutcomePayload;
import dev.imagio.slot.operation.ActionStatus;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class SlotActionOutcomePayloadHandler {
    private SlotActionOutcomePayloadHandler() {
    }

    public static void handle(SlotActionOutcomePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            boolean handledByQuickAccessFollowUp = false;
            if (payload != null) {
                SlotUndoHistory.recordTransferOutcome(payload.requestId(), payload.status(), payload.affectedCount());
                handledByQuickAccessFollowUp = QuickAccessFollowUpState.handleTransferOutcome(payload.requestId(), payload.status());
            }
            if (payload != null
                    && payload.status() == ActionStatus.CONFIRMED
                    && !payload.acquisitionItemIds().isEmpty()) {
                RecentLootTracker.recordOutcomeAcquisitions(payload.acquisitionProducerId(), payload.acquisitionItemIds());
            }
            if (!handledByQuickAccessFollowUp) {
                String routingKey = SlotActionRequestRoutingState.resolveContextKey(
                        payload == null ? null : payload.requestId(),
                        payload == null ? "" : payload.menuKey()
                );
                SlotActionOutcomeState.publish(
                        routingKey,
                        payload == null ? null : payload.requestId(),
                        toActionResult(payload)
                );
            }
            if (payload != null && payload.status() != ActionStatus.REQUESTED && payload.status() != ActionStatus.PENDING) {
                QuickAccessPendingState.completeRequest(payload.requestId());
                SlotActionRequestRoutingState.completeRequest(payload.requestId());
            }
        });
    }

    private static SlotActionResult toActionResult(SlotActionOutcomePayload payload) {
        if (payload == null) {
            return SlotActionResult.NONE;
        }

        String baseKey = payload.summaryKey().isBlank() ? actionOutcomeBaseKey(payload.actionFamily()) : payload.summaryKey();
        return switch (payload.status()) {
            case REQUESTED, PENDING -> SlotActionResult.requested(Component.translatable(baseKey + ".requested"));
            case CONFIRMED -> SlotActionResult.applied(Component.translatable(baseKey + ".applied", payload.affectedCount()));
            case BLOCKED -> SlotActionResult.blocked(Component.translatable(baseKey + ".blocked"));
            case FAILED -> SlotActionResult.failed(Component.translatable(baseKey + ".failed"));
        };
    }

    private static String actionOutcomeBaseKey(ActionFamily actionFamily) {
        if (actionFamily == null) {
            return "slot.screen.action.outcome.generic";
        }
        return switch (actionFamily) {
            case TRANSFER -> "slot.screen.action.outcome.transfer";
            case STORE -> "slot.screen.action.outcome.store";
            case PICKUP -> "slot.screen.action.outcome.pickup";
            case DROP -> "slot.screen.action.outcome.drop";
            case TRASH -> "slot.screen.action.outcome.trash";
            case VOID -> "slot.screen.action.outcome.void";
            case CRAFT -> "slot.screen.action.outcome.craft";
            case TOOL_ACTION -> "slot.screen.action.outcome.tool_action";
            default -> "slot.screen.action.outcome.generic";
        };
    }
}
