package dev.imagio.slot.ui.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record WorkspaceActionPacket(
        WorkspaceActionEnvelope envelope,
        WorkspaceActionId action,
        List<WorkspaceActionArgumentValue> arguments
) {
    public WorkspaceActionPacket {
        Objects.requireNonNull(action, "action");
        arguments = arguments == null ? List.of() : List.copyOf(arguments);
    }

    public static WorkspaceActionPacket fromObjects(
            WorkspaceActionEnvelope envelope,
            WorkspaceActionId action,
            Object... arguments
    ) {
        WorkspaceActionValidation validation = WorkspaceActionValidator.validate(action, arguments);
        if (!validation.valid()) {
            throw new IllegalArgumentException(validation.diagnostics());
        }
        WorkspaceActionDefinition definition = WorkspaceActionCatalog.require(action);
        Object[] resolvedArguments = arguments == null ? new Object[0] : arguments;
        ArrayList<WorkspaceActionArgumentValue> values = new ArrayList<>(resolvedArguments.length);
        for (int index = 0; index < resolvedArguments.length; index++) {
            values.add(WorkspaceActionArgumentValue.of(definition.argumentTypes().get(index), resolvedArguments[index]));
        }
        return new WorkspaceActionPacket(envelope, action, values);
    }

    public WorkspaceActionValidation validateShape() {
        WorkspaceActionDefinition definition;
        try {
            definition = WorkspaceActionCatalog.require(action);
        } catch (IllegalArgumentException exception) {
            return WorkspaceActionValidation.rejected("unknown_action");
        }
        if (arguments.size() != definition.argumentTypes().size()) {
            return WorkspaceActionValidation.rejected(
                    "argument_count_mismatch:expected=" + definition.argumentTypes().size()
                            + ":actual=" + arguments.size()
            );
        }
        for (int index = 0; index < arguments.size(); index++) {
            WorkspaceActionArgumentValue argument = arguments.get(index);
            WorkspaceActionArgumentType expected = definition.argumentTypes().get(index);
            if (argument == null) {
                return WorkspaceActionValidation.rejected("missing_argument:index=" + index);
            }
            if (argument.type() != expected) {
                return WorkspaceActionValidation.rejected(
                        "argument_type_mismatch:index=" + index
                                + ":expected=" + expected
                                + ":actual=" + argument.type()
                );
            }
        }
        return WorkspaceActionValidation.ok();
    }

    public Object[] toObjects() {
        WorkspaceActionValidation validation = validateShape();
        if (!validation.valid()) {
            throw new IllegalStateException(validation.diagnostics());
        }
        Object[] values = new Object[arguments.size()];
        for (int index = 0; index < arguments.size(); index++) {
            values[index] = arguments.get(index).toObject();
        }
        return values;
    }
}
