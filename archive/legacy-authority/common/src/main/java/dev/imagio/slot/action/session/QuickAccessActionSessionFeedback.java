package dev.imagio.slot.action.session;

import net.minecraft.network.chat.Component;

final class QuickAccessActionSessionFeedback {
    private QuickAccessActionSessionFeedback() {
    }

    static ActionSessionResult applied(QuickAccessFollowUpActionType actionType) {
        return ActionSessionResult.applied(Component.translatable(baseKey(actionType) + ".applied"));
    }

    static ActionSessionResult blocked(QuickAccessFollowUpActionType actionType) {
        return ActionSessionResult.blocked(Component.translatable(baseKey(actionType) + ".blocked"));
    }

    static ActionSessionResult failed(QuickAccessFollowUpActionType actionType) {
        return ActionSessionResult.failed(Component.translatable(baseKey(actionType) + ".failed"));
    }

    private static String baseKey(QuickAccessFollowUpActionType actionType) {
        return switch (actionType) {
            case USE_HAND -> "slot.screen.action.use";
            case USE_OFFHAND -> "slot.screen.action.use_offhand";
            case DROP_MENU_SLOT -> "slot.screen.action.drop";
        };
    }
}
