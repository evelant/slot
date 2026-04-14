package dev.imagio.slot.network;

import dev.imagio.slot.client.model.ComparisonMode;
import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.projection.InventoryPane;
import dev.imagio.slot.intent.ActionFamily;
import dev.imagio.slot.intent.ActionRequest;
import dev.imagio.slot.intent.ActionRequestId;
import dev.imagio.slot.source.SourceId;
import dev.imagio.slot.source.SourceSlotRef;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class CraftingGridActionRequests {
    private static final int REQUESTED_COUNT_ONE = 1;
    private static final int REQUESTED_COUNT_STACK = Integer.MAX_VALUE;

    public static final String SOURCE_OPEN_CONTAINER = ActionRequestSourceIds.SOURCE_OPEN_CONTAINER;
    public static final String SOURCE_CARRIED = ActionRequestSourceIds.SOURCE_CARRIED;
    public static final String SOURCE_MENU = ActionRequestSourceIds.SOURCE_MENU;

    public static final String KIND_PANE_IDENTITY = "pane_identity";
    public static final String KIND_CARRIED_CURSOR = "carried_cursor";
    public static final String KIND_MENU_SLOT_GROUP = "menu_slot_group";
    public static final String KIND_RESULT_SLOT = "result_slot";
    public static final String KIND_RESULT_ACTION = "result_action";

    private CraftingGridActionRequests() {
    }

    public static ActionRequest placeOne(
            int containerId,
            String expectedSessionFingerprint,
            int targetMenuSlot,
            ItemIdentity identity,
            InventoryPane sourcePane
    ) {
        return new ActionRequest(
                ActionRequest.CURRENT_SCHEMA_VERSION,
                ActionRequestId.create(),
                expectedSessionFingerprint == null ? "" : expectedSessionFingerprint,
                containerId,
                ActionFamily.CRAFT,
                new SourceSlotRef(KIND_PANE_IDENTITY, SourceId.of(sourceIdForPane(sourcePane)), ActionRequestSourceIds.AGGREGATE_PAYLOAD),
                SourceSlotRef.menuSlot(SourceId.of(SOURCE_MENU), targetMenuSlot),
                "",
                ActionRequestIdentityCodec.encode(identity),
                REQUESTED_COUNT_ONE
        );
    }

    public static ActionRequest placeCursor(
            int containerId,
            String expectedSessionFingerprint,
            int targetMenuSlot,
            ItemIdentity carriedIdentity,
            CursorMode cursorMode
    ) {
        return new ActionRequest(
                ActionRequest.CURRENT_SCHEMA_VERSION,
                ActionRequestId.create(),
                expectedSessionFingerprint == null ? "" : expectedSessionFingerprint,
                containerId,
                ActionFamily.CRAFT,
                carriedCursorRef(),
                SourceSlotRef.menuSlot(SourceId.of(SOURCE_MENU), targetMenuSlot),
                "",
                ActionRequestIdentityCodec.encode(carriedIdentity),
                requestedCount(cursorMode)
        );
    }

    public static ActionRequest distributeCursor(
            int containerId,
            String expectedSessionFingerprint,
            List<Integer> targetMenuSlots,
            ItemIdentity carriedIdentity,
            CursorMode cursorMode
    ) {
        return new ActionRequest(
                ActionRequest.CURRENT_SCHEMA_VERSION,
                ActionRequestId.create(),
                expectedSessionFingerprint == null ? "" : expectedSessionFingerprint,
                containerId,
                ActionFamily.CRAFT,
                carriedCursorRef(),
                new SourceSlotRef(KIND_MENU_SLOT_GROUP, SourceId.of(SOURCE_MENU), encodeMenuSlots(targetMenuSlots)),
                "",
                ActionRequestIdentityCodec.encode(carriedIdentity),
                requestedCount(cursorMode)
        );
    }

    public static ActionRequest extractResult(
            int containerId,
            String expectedSessionFingerprint,
            int resultMenuSlot,
            ResultAction resultAction,
            int mouseButton,
            int repeatCount
    ) {
        return new ActionRequest(
                ActionRequest.CURRENT_SCHEMA_VERSION,
                ActionRequestId.create(),
                expectedSessionFingerprint == null ? "" : expectedSessionFingerprint,
                containerId,
                ActionFamily.CRAFT,
                new SourceSlotRef(KIND_RESULT_SLOT, SourceId.of(SOURCE_MENU), Integer.toString(resultMenuSlot)),
                new SourceSlotRef(KIND_RESULT_ACTION, SourceId.of(SOURCE_MENU), encodeResultAction(resultAction, mouseButton)),
                "",
                "",
                Math.max(1, repeatCount)
        );
    }

    public static ActionRequest fromLegacyPayload(CraftingGridPlacementPayload payload) {
        if (payload == null) {
            return null;
        }

        return placeOne(
                payload.containerId(),
                "",
                payload.targetMenuSlot(),
                payload.identity(),
                toPane(payload.sourcePane())
        );
    }

    public static Resolution resolve(ActionRequest request) {
        if (request == null || request.actionFamily() != ActionFamily.CRAFT || request.primarySourceRef() == null) {
            return null;
        }

        SourceSlotRef primary = request.primarySourceRef();
        SourceSlotRef secondary = request.secondarySourceRef();
        if (secondary == null) {
            return null;
        }

        ItemIdentity identity = ActionRequestIdentityCodec.decode(request.identityKey());
        if (KIND_PANE_IDENTITY.equals(primary.kind())) {
            if (!"menu_slot".equals(secondary.kind())) {
                return null;
            }

            int targetMenuSlot = parseMenuSlot(secondary);
            if (targetMenuSlot < 0) {
                return null;
            }
            InventoryPane sourcePane = paneForSourceId(primary.sourceId().value());
            if (sourcePane == null) {
                return null;
            }
            return new Resolution(
                    Route.PANE_IDENTITY_PLACE,
                    spec(request.expectedContainerId(), targetMenuSlot, List.of(targetMenuSlot), sourcePane, null, identity, null, 0, 1)
            );
        }

        if (KIND_RESULT_SLOT.equals(primary.kind())) {
            if (!SOURCE_MENU.equals(primary.sourceId().value()) || !KIND_RESULT_ACTION.equals(secondary.kind())) {
                return null;
            }

            int resultMenuSlot = parseMenuSlot(primary);
            ResultActionSpec resultAction = parseResultAction(secondary.payload());
            if (resultMenuSlot < 0 || resultAction == null) {
                return null;
            }

            return new Resolution(
                    Route.RESULT_EXTRACT,
                    spec(
                            request.expectedContainerId(),
                            resultMenuSlot,
                            List.of(resultMenuSlot),
                            null,
                            null,
                            null,
                            resultAction.action(),
                            resultAction.mouseButton(),
                            Math.max(1, request.requestedCount())
                    )
            );
        }

        if (!isCarriedCursor(primary)) {
            return null;
        }

        CursorMode cursorMode = cursorModeForRequestedCount(request.requestedCount());
        if (cursorMode == null) {
            return null;
        }

        if ("menu_slot".equals(secondary.kind())) {
            int targetMenuSlot = parseMenuSlot(secondary);
            if (targetMenuSlot < 0) {
                return null;
            }
            return new Resolution(
                    Route.CURSOR_PLACE,
                    spec(request.expectedContainerId(), targetMenuSlot, List.of(targetMenuSlot), null, cursorMode, identity, null, 0, 1)
            );
        }

        if (!KIND_MENU_SLOT_GROUP.equals(secondary.kind())) {
            return null;
        }

        List<Integer> targetMenuSlots = parseMenuSlots(secondary);
        if (targetMenuSlots.isEmpty()) {
            return null;
        }

        return new Resolution(
                Route.CURSOR_DISTRIBUTE,
                spec(request.expectedContainerId(), targetMenuSlots.getFirst(), targetMenuSlots, null, cursorMode, identity, null, 0, 1)
        );
    }

    private static CraftSpec spec(
            int containerId,
            int targetMenuSlot,
            List<Integer> targetMenuSlots,
            InventoryPane sourcePane,
            CursorMode cursorMode,
            ItemIdentity identity,
            ResultAction resultAction,
            int mouseButton,
            int repeatCount
    ) {
        ItemIdentity resolvedIdentity = identity == null ? null : identity;
        return new CraftSpec(
                containerId,
                targetMenuSlot,
                targetMenuSlots,
                sourcePane,
                cursorMode,
                resultAction,
                mouseButton,
                repeatCount,
                resolvedIdentity == null ? "" : resolvedIdentity.itemId(),
                resolvedIdentity == null ? ComparisonMode.ITEM_ID : resolvedIdentity.comparisonMode(),
                resolvedIdentity == null ? "" : resolvedIdentity.componentFingerprint()
        );
    }

    private static InventoryPane toPane(CraftingGridPlacementPayload.SourcePane sourcePane) {
        return sourcePane == CraftingGridPlacementPayload.SourcePane.OPEN_CONTAINER
                ? InventoryPane.OPEN_CONTAINER
                : InventoryPane.CARRIED;
    }

    private static String sourceIdForPane(InventoryPane pane) {
        return pane == InventoryPane.OPEN_CONTAINER ? SOURCE_OPEN_CONTAINER : SOURCE_CARRIED;
    }

    private static InventoryPane paneForSourceId(String sourceId) {
        if (SOURCE_OPEN_CONTAINER.equals(sourceId)) {
            return InventoryPane.OPEN_CONTAINER;
        }
        if (SOURCE_CARRIED.equals(sourceId)) {
            return InventoryPane.CARRIED;
        }
        return null;
    }

    private static boolean isCarriedCursor(SourceSlotRef ref) {
        return ref != null
                && KIND_CARRIED_CURSOR.equals(ref.kind())
                && SOURCE_CARRIED.equals(ref.sourceId().value());
    }

    private static SourceSlotRef carriedCursorRef() {
        return new SourceSlotRef(KIND_CARRIED_CURSOR, SourceId.of(SOURCE_CARRIED), ActionRequestSourceIds.AGGREGATE_PAYLOAD);
    }

    private static int requestedCount(CursorMode cursorMode) {
        if (cursorMode == CursorMode.ONE) {
            return REQUESTED_COUNT_ONE;
        }
        return REQUESTED_COUNT_STACK;
    }

    private static CursorMode cursorModeForRequestedCount(int requestedCount) {
        if (requestedCount <= 0) {
            return null;
        }
        if (requestedCount == REQUESTED_COUNT_ONE) {
            return CursorMode.ONE;
        }
        return CursorMode.STACK;
    }

    private static int parseMenuSlot(SourceSlotRef ref) {
        if (ref == null) {
            return -1;
        }
        try {
            return Integer.parseInt(ref.payload());
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private static String encodeMenuSlots(List<Integer> targetMenuSlots) {
        if (targetMenuSlots == null || targetMenuSlots.isEmpty()) {
            return "";
        }

        StringBuilder encoded = new StringBuilder();
        Set<Integer> normalized = new LinkedHashSet<>();
        for (Integer targetMenuSlot : targetMenuSlots) {
            if (targetMenuSlot != null && targetMenuSlot >= 0) {
                normalized.add(targetMenuSlot);
            }
        }
        for (Integer targetMenuSlot : normalized) {
            if (!encoded.isEmpty()) {
                encoded.append(',');
            }
            encoded.append(targetMenuSlot);
        }
        return encoded.toString();
    }

    private static List<Integer> parseMenuSlots(SourceSlotRef ref) {
        if (ref == null || ref.payload().isBlank()) {
            return List.of();
        }

        List<Integer> parsed = new ArrayList<>();
        Set<Integer> deduped = new LinkedHashSet<>();
        for (String token : ref.payload().split(",")) {
            try {
                int menuSlot = Integer.parseInt(token.trim());
                if (menuSlot >= 0 && deduped.add(menuSlot)) {
                    parsed.add(menuSlot);
                }
            } catch (NumberFormatException ignored) {
                return List.of();
            }
        }
        return List.copyOf(parsed);
    }

    private static String encodeResultAction(ResultAction resultAction, int mouseButton) {
        ResultAction action = resultAction == null ? ResultAction.PICKUP : resultAction;
        int resolvedMouseButton = mouseButton == 1 ? 1 : 0;
        return action.name().toLowerCase() + ":" + resolvedMouseButton;
    }

    private static ResultActionSpec parseResultAction(String payload) {
        if (payload == null || payload.isBlank()) {
            return null;
        }

        String[] parts = payload.split(":", 2);
        if (parts.length != 2) {
            return null;
        }

        ResultAction action = switch (parts[0]) {
            case "pickup" -> ResultAction.PICKUP;
            case "quick_move" -> ResultAction.QUICK_MOVE;
            default -> null;
        };
        if (action == null) {
            return null;
        }

        try {
            int mouseButton = Integer.parseInt(parts[1]);
            if (mouseButton != 0 && mouseButton != 1) {
                return null;
            }
            return new ResultActionSpec(action, mouseButton);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static CraftingGridPlacementPayload.SourcePane payloadPane(InventoryPane pane) {
        return pane == InventoryPane.OPEN_CONTAINER
                ? CraftingGridPlacementPayload.SourcePane.OPEN_CONTAINER
                : CraftingGridPlacementPayload.SourcePane.CARRIED;
    }

    public record Resolution(Route route, CraftSpec spec) {
        public CraftingGridPlacementPayload payload() {
            if (route != Route.PANE_IDENTITY_PLACE || spec.sourcePane() == null) {
                return null;
            }

            return new CraftingGridPlacementPayload(
                    spec.containerId(),
                    spec.targetMenuSlot(),
                    payloadPane(spec.sourcePane()),
                    spec.itemId(),
                    spec.comparisonMode(),
                    spec.componentFingerprint()
            );
        }
    }

    public record CraftSpec(
            int containerId,
            int targetMenuSlot,
            List<Integer> targetMenuSlots,
            InventoryPane sourcePane,
            CursorMode cursorMode,
            ResultAction resultAction,
            int mouseButton,
            int repeatCount,
            String itemId,
            ComparisonMode comparisonMode,
            String componentFingerprint
    ) {
        public CraftSpec {
            targetMenuSlots = targetMenuSlots == null || targetMenuSlots.isEmpty()
                    ? targetMenuSlot >= 0 ? List.of(targetMenuSlot) : List.of()
                    : List.copyOf(targetMenuSlots);
            mouseButton = mouseButton == 1 ? 1 : 0;
            repeatCount = Math.max(1, repeatCount);
        }

        public ItemIdentity identity() {
            if (itemId == null || itemId.isBlank()) {
                return null;
            }
            return new ItemIdentity(itemId, comparisonMode, componentFingerprint);
        }
    }

    public enum Route {
        PANE_IDENTITY_PLACE,
        CURSOR_PLACE,
        CURSOR_DISTRIBUTE,
        RESULT_EXTRACT
    }

    public enum CursorMode {
        ONE,
        STACK
    }

    public enum ResultAction {
        PICKUP,
        QUICK_MOVE
    }

    private record ResultActionSpec(ResultAction action, int mouseButton) {
    }
}
