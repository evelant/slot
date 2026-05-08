package dev.imagio.slot.ui.action;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class WorkspaceActionCatalog {
    private static final List<WorkspaceActionDefinition> DEFINITIONS;
    private static final Map<WorkspaceActionId, WorkspaceActionDefinition> BY_ACTION;
    private static final Map<String, WorkspaceActionDefinition> BY_WIRE_ID;

    static {
        LinkedHashMap<WorkspaceActionId, WorkspaceActionDefinition> byAction = new LinkedHashMap<>();
        LinkedHashMap<String, WorkspaceActionDefinition> byWireId = new LinkedHashMap<>();
        for (WorkspaceActionId action : WorkspaceActionId.values()) {
            WorkspaceActionDefinition definition = action.definition();
            WorkspaceActionDefinition duplicateAction = byAction.put(action, definition);
            if (duplicateAction != null) {
                throw new IllegalStateException("Duplicate workspace action: " + action);
            }
            WorkspaceActionDefinition duplicateWireId = byWireId.put(definition.wireId(), definition);
            if (duplicateWireId != null) {
                throw new IllegalStateException("Duplicate workspace action wire id: " + definition.wireId());
            }
        }
        BY_ACTION = Map.copyOf(byAction);
        BY_WIRE_ID = Map.copyOf(byWireId);
        DEFINITIONS = List.copyOf(byAction.values());
    }

    private WorkspaceActionCatalog() {
    }

    public static List<WorkspaceActionDefinition> definitions() {
        return DEFINITIONS;
    }

    public static WorkspaceActionDefinition require(WorkspaceActionId action) {
        WorkspaceActionDefinition definition = BY_ACTION.get(action);
        if (definition == null) {
            throw new IllegalArgumentException("Unknown workspace action: " + action);
        }
        return definition;
    }

    public static Optional<WorkspaceActionDefinition> byWireId(String wireId) {
        if (wireId == null || wireId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(BY_WIRE_ID.get(wireId));
    }
}
