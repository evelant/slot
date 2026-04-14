package dev.imagio.slot.client.intent;

import dev.imagio.slot.network.ActionRequestClientContext;

import java.util.Locale;

public record ToolActionIntent(
        String expectedSessionFingerprint,
        int expectedContainerId,
        String toolId,
        Action action
) {
    public ToolActionIntent {
        expectedSessionFingerprint = expectedSessionFingerprint == null ? "" : expectedSessionFingerprint;
        toolId = toolId == null ? "" : toolId;
    }

    public static ToolActionIntent forCurrentSession(int containerId, String toolId, Action action) {
        return new ToolActionIntent(
                ActionRequestClientContext.currentSessionFingerprint(containerId),
                containerId,
                toolId,
                action
        );
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
    }
}
