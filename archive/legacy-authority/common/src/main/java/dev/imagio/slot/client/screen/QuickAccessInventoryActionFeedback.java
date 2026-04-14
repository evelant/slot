package dev.imagio.slot.client.screen;

import net.minecraft.network.chat.Component;

final class QuickAccessInventoryActionFeedback {
    private QuickAccessInventoryActionFeedback() {
    }

    static SlotActionResult requested(QuickAccessFollowUpState.ActionType actionType) {
        return SlotActionResult.requested(Component.translatable(baseKey(actionType) + ".requested"));
    }

    static SlotActionResult applied(QuickAccessFollowUpState.ActionType actionType) {
        return SlotActionResult.applied(Component.translatable(baseKey(actionType) + ".applied"));
    }

    static SlotActionResult blocked(QuickAccessFollowUpState.ActionType actionType) {
        return SlotActionResult.blocked(Component.translatable(baseKey(actionType) + ".blocked"));
    }

    static SlotActionResult failed(QuickAccessFollowUpState.ActionType actionType) {
        return SlotActionResult.failed(Component.translatable(baseKey(actionType) + ".failed"));
    }

    private static String baseKey(QuickAccessFollowUpState.ActionType actionType) {
        return switch (actionType) {
            case USE_HAND, USE_OFFHAND -> "slot.screen.action.outcome.quick_access.use";
            case DROP_MENU_SLOT -> "slot.screen.action.outcome.quick_access.drop";
        };
    }
}
