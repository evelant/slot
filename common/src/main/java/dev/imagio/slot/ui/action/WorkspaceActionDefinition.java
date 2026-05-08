package dev.imagio.slot.ui.action;

import java.util.List;
import java.util.Objects;

public record WorkspaceActionDefinition(
        WorkspaceActionId action,
        String wireId,
        List<WorkspaceActionArgumentType> argumentTypes
) {
    public WorkspaceActionDefinition {
        Objects.requireNonNull(action, "action");
        if (wireId == null || wireId.isBlank()) {
            throw new IllegalArgumentException("wireId must not be blank");
        }
        argumentTypes = argumentTypes == null ? List.of() : List.copyOf(argumentTypes);
    }

    public Class<?>[] argumentClasses() {
        Class<?>[] classes = new Class<?>[argumentTypes.size()];
        for (int index = 0; index < argumentTypes.size(); index++) {
            classes[index] = argumentTypes.get(index).javaType();
        }
        return classes;
    }
}
