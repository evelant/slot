package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.workspace.WorkspaceSearchQuery;

/**
 * Shared keyboard semantics for the workspace search modal.
 *
 * <p>Platform adapters still translate raw key codes into the semantic
 * {@link ControlKey} values here, but the modal state transitions live in
 * common so Forge and NeoForge cannot drift into different search behavior.
 */
public final class WorkspaceSearchInputPolicy {
    private WorkspaceSearchInputPolicy() {
    }

    public static Decision charTyped(boolean active, String query, char codePoint, boolean textInputFocused) {
        String cleanQuery = clean(query);
        if (codePoint == '/' && !active && !textInputFocused) {
            return Decision.handled(Action.OPEN, true, "");
        }
        if (!active) {
            return Decision.unhandled(active, cleanQuery);
        }
        if (codePoint == '/') {
            return Decision.handled(Action.OPEN, true, "");
        }
        if (codePoint >= '0' && codePoint <= '9') {
            // Digits are hotbar verbs, not search text. Consume the char
            // after key handling so it cannot corrupt the active query.
            return Decision.handled(Action.IGNORE_DIGIT, true, cleanQuery);
        }
        if (codePoint >= 0x20 && codePoint < 0x7F) {
            if (cleanQuery.length() >= WorkspaceSearchQuery.MAX_QUERY_LENGTH) {
                return Decision.handled(Action.APPEND, true, cleanQuery);
            }
            return Decision.handled(Action.APPEND, true, clean(cleanQuery + codePoint));
        }
        return Decision.unhandled(active, cleanQuery);
    }

    public static Decision keyPressed(boolean active, String query, ControlKey key) {
        String cleanQuery = clean(query);
        if (!active || key == null) {
            return Decision.unhandled(active, cleanQuery);
        }
        return switch (key) {
            case ENTER -> Decision.handled(Action.CONFIRM, false, cleanQuery);
            case ESCAPE -> Decision.handled(Action.DISMISS, false, "");
            case BACKSPACE -> cleanQuery.isEmpty()
                    ? Decision.handled(Action.BACKSPACE, true, cleanQuery)
                    : Decision.handled(
                            Action.BACKSPACE,
                            true,
                            clean(cleanQuery.substring(0, cleanQuery.length() - 1)));
        };
    }

    public static Decision confirmForHotbar(boolean active, String query) {
        return active
                ? Decision.handled(Action.CONFIRM, false, clean(query))
                : Decision.unhandled(false, clean(query));
    }

    private static String clean(String query) {
        return WorkspaceSearchQuery.cleanInput(query == null ? "" : query);
    }

    public enum ControlKey {
        ENTER,
        ESCAPE,
        BACKSPACE
    }

    public enum Action {
        NONE,
        OPEN,
        APPEND,
        BACKSPACE,
        CONFIRM,
        DISMISS,
        IGNORE_DIGIT
    }

    public record Decision(
            boolean handled,
            Action action,
            boolean active,
            String query
    ) {
        private static Decision handled(Action action, boolean active, String query) {
            return new Decision(true, action == null ? Action.NONE : action, active, query == null ? "" : query);
        }

        private static Decision unhandled(boolean active, String query) {
            return new Decision(false, Action.NONE, active, query == null ? "" : query);
        }
    }
}
