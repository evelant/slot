package dev.imagio.slot.network;

import dev.imagio.slot.intent.ActionFamily;
import dev.imagio.slot.intent.ActionRequest;
import dev.imagio.slot.intent.ActionRequestId;
import dev.imagio.slot.source.SourceId;
import dev.imagio.slot.source.SourceSlotRef;

import java.util.Locale;

public final class ToolActionRequests {
    public static final String SOURCE_TOOL = ActionRequestSourceIds.SOURCE_TOOL;
    public static final String KIND_TOOL_ACTION = "tool_action";

    private ToolActionRequests() {
    }

    public static ActionRequest request(
            int containerId,
            String expectedSessionFingerprint,
            String toolId,
            Action action
    ) {
        return new ActionRequest(
                ActionRequest.CURRENT_SCHEMA_VERSION,
                ActionRequestId.create(),
                expectedSessionFingerprint == null ? "" : expectedSessionFingerprint,
                containerId,
                ActionFamily.TOOL_ACTION,
                new SourceSlotRef(KIND_TOOL_ACTION, SourceId.of(SOURCE_TOOL), action == null ? "" : action.token()),
                null,
                toolId == null ? "" : toolId,
                "",
                1
        );
    }

    public static ActionRequest fromLegacyPayload(ToolActionPayload payload) {
        if (payload == null) {
            return null;
        }

        return request(
                payload.containerId(),
                "",
                payload.toolId(),
                Action.fromLegacy(payload.action())
        );
    }

    public static Resolution resolve(ActionRequest request) {
        if (request == null || request.actionFamily() != ActionFamily.TOOL_ACTION || request.primarySourceRef() == null) {
            return null;
        }

        SourceSlotRef primary = request.primarySourceRef();
        if (!KIND_TOOL_ACTION.equals(primary.kind())
                || !SOURCE_TOOL.equals(primary.sourceId().value())
                || request.toolRef().isBlank()) {
            return null;
        }

        Action action = Action.parse(primary.payload());
        if (action == null) {
            return null;
        }
        return new Resolution(request.expectedContainerId(), request.toolRef(), action);
    }

    public record Resolution(int containerId, String toolId, Action action) {
        public ToolActionPayload payload() {
            return new ToolActionPayload(containerId, toolId, action.toLegacy());
        }
    }

    public enum Action {
        CLEAR_GRID,
        BALANCE_GRID,
        ROTATE_GRID_CW,
        ROTATE_GRID_CCW,
        TOGGLE_AUTO_REFILL;

        private final String token;

        Action() {
            this.token = name().toLowerCase(Locale.ROOT);
        }

        public String token() {
            return token;
        }

        public ToolActionPayload.Action toLegacy() {
            return switch (this) {
                case CLEAR_GRID -> ToolActionPayload.Action.CLEAR_GRID;
                case BALANCE_GRID -> ToolActionPayload.Action.BALANCE_GRID;
                case ROTATE_GRID_CW -> ToolActionPayload.Action.ROTATE_GRID_CW;
                case ROTATE_GRID_CCW -> ToolActionPayload.Action.ROTATE_GRID_CCW;
                case TOGGLE_AUTO_REFILL -> ToolActionPayload.Action.TOGGLE_AUTO_REFILL;
            };
        }

        public static Action fromLegacy(ToolActionPayload.Action action) {
            if (action == null) {
                return null;
            }
            return switch (action) {
                case CLEAR_GRID -> CLEAR_GRID;
                case BALANCE_GRID -> BALANCE_GRID;
                case ROTATE_GRID_CW -> ROTATE_GRID_CW;
                case ROTATE_GRID_CCW -> ROTATE_GRID_CCW;
                case TOGGLE_AUTO_REFILL -> TOGGLE_AUTO_REFILL;
            };
        }

        public static Action parse(String token) {
            if (token == null || token.isBlank()) {
                return null;
            }

            for (Action action : values()) {
                if (action.token.equals(token)) {
                    return action;
                }
            }
            return null;
        }
    }
}
