package dev.imagio.slot.client.intent;

import dev.imagio.slot.intent.ActionRequest;
import dev.imagio.slot.client.screen.container.MenuSlotId;
import dev.imagio.slot.network.ActionRequestRequester;
import dev.imagio.slot.network.CraftingGridActionRequests;
import dev.imagio.slot.network.ToolActionRequests;

public final class IntentRouter {
    private IntentRouter() {
    }

    public static boolean route(CraftingIntent intent) {
        return route(intent, ActionRequestRequester::request);
    }

    static boolean route(CraftingIntent intent, ActionRequestSender sender) {
        if (intent instanceof CraftingIntent.PlaceOne placeOne) {
            return routePlaceOne(placeOne, sender);
        }
        if (intent instanceof CraftingIntent.PlaceCursor placeCursor) {
            return routePlaceCursor(placeCursor, sender);
        }
        if (intent instanceof CraftingIntent.DistributeCursor distributeCursor) {
            return routeDistributeCursor(distributeCursor, sender);
        }
        if (intent instanceof CraftingIntent.ExtractResult extractResult) {
            return routeExtractResult(extractResult, sender);
        }
        return false;
    }

    private static boolean routePlaceOne(CraftingIntent.PlaceOne intent, ActionRequestSender sender) {
        if (intent == null
                || sender == null
                || intent.expectedContainerId() < 0
                || intent.identity() == null
                || intent.sourcePane() == null
                || intent.targetMenuSlotId() == null
                || !intent.targetMenuSlotId().isValid()) {
            return false;
        }

        return sender.send(
                CraftingGridActionRequests.placeOne(
                        intent.expectedContainerId(),
                        intent.expectedSessionFingerprint(),
                        intent.targetMenuSlotId().value(),
                        intent.identity(),
                        intent.sourcePane()
                )
        );
    }

    private static boolean routePlaceCursor(CraftingIntent.PlaceCursor intent, ActionRequestSender sender) {
        if (intent == null
                || sender == null
                || intent.expectedContainerId() < 0
                || intent.carriedIdentity() == null
                || intent.cursorMode() == null
                || intent.targetMenuSlotId() == null
                || !intent.targetMenuSlotId().isValid()) {
            return false;
        }

        return sender.send(
                CraftingGridActionRequests.placeCursor(
                        intent.expectedContainerId(),
                        intent.expectedSessionFingerprint(),
                        intent.targetMenuSlotId().value(),
                        intent.carriedIdentity(),
                        switch (intent.cursorMode()) {
                            case ONE -> CraftingGridActionRequests.CursorMode.ONE;
                            case STACK -> CraftingGridActionRequests.CursorMode.STACK;
                        }
                )
        );
    }

    private static boolean routeDistributeCursor(CraftingIntent.DistributeCursor intent, ActionRequestSender sender) {
        if (intent == null
                || sender == null
                || intent.expectedContainerId() < 0
                || intent.carriedIdentity() == null
                || intent.cursorMode() == null
                || intent.targetMenuSlotIds() == null
                || intent.targetMenuSlotIds().isEmpty()
                || intent.targetMenuSlotIds().stream().anyMatch(targetMenuSlotId -> targetMenuSlotId == null || !targetMenuSlotId.isValid())) {
            return false;
        }

        return sender.send(
                CraftingGridActionRequests.distributeCursor(
                        intent.expectedContainerId(),
                        intent.expectedSessionFingerprint(),
                        intent.targetMenuSlotIds().stream().map(MenuSlotId::value).toList(),
                        intent.carriedIdentity(),
                        switch (intent.cursorMode()) {
                            case ONE -> CraftingGridActionRequests.CursorMode.ONE;
                            case STACK -> CraftingGridActionRequests.CursorMode.STACK;
                        }
                )
        );
    }

    private static boolean routeExtractResult(CraftingIntent.ExtractResult intent, ActionRequestSender sender) {
        if (intent == null
                || sender == null
                || intent.expectedContainerId() < 0
                || intent.resultAction() == null
                || intent.resultMenuSlotId() == null
                || !intent.resultMenuSlotId().isValid()
                || intent.repeatCount() <= 0
                || (intent.mouseButton() != 0 && intent.mouseButton() != 1)) {
            return false;
        }

        return sender.send(
                CraftingGridActionRequests.extractResult(
                        intent.expectedContainerId(),
                        intent.expectedSessionFingerprint(),
                        intent.resultMenuSlotId().value(),
                        switch (intent.resultAction()) {
                            case QUICK_MOVE -> CraftingGridActionRequests.ResultAction.QUICK_MOVE;
                            case PICKUP -> CraftingGridActionRequests.ResultAction.PICKUP;
                        },
                        intent.mouseButton(),
                        intent.repeatCount()
                )
        );
    }

    public static boolean route(ToolActionIntent intent) {
        return route(intent, ActionRequestRequester::request);
    }

    static boolean route(ToolActionIntent intent, ActionRequestSender sender) {
        if (intent == null
                || sender == null
                || intent.expectedContainerId() < 0
                || intent.toolId().isBlank()
                || intent.action() == null) {
            return false;
        }

        return sender.send(
                ToolActionRequests.request(
                        intent.expectedContainerId(),
                        intent.expectedSessionFingerprint(),
                        intent.toolId(),
                        switch (intent.action()) {
                            case CLEAR_GRID -> ToolActionRequests.Action.CLEAR_GRID;
                            case BALANCE_GRID -> ToolActionRequests.Action.BALANCE_GRID;
                            case ROTATE_GRID_CW -> ToolActionRequests.Action.ROTATE_GRID_CW;
                            case ROTATE_GRID_CCW -> ToolActionRequests.Action.ROTATE_GRID_CCW;
                            case TOGGLE_AUTO_REFILL -> ToolActionRequests.Action.TOGGLE_AUTO_REFILL;
                        }
                )
        );
    }

    @FunctionalInterface
    interface ActionRequestSender {
        boolean send(ActionRequest request);
    }
}
