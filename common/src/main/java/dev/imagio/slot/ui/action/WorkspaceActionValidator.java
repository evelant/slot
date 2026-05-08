package dev.imagio.slot.ui.action;

public final class WorkspaceActionValidator {
    private WorkspaceActionValidator() {
    }

    public static WorkspaceActionValidation validate(WorkspaceActionId action, Object... arguments) {
        if (action == null) {
            return WorkspaceActionValidation.rejected("missing_action");
        }
        WorkspaceActionDefinition definition;
        try {
            definition = WorkspaceActionCatalog.require(action);
        } catch (IllegalArgumentException exception) {
            return WorkspaceActionValidation.rejected("unknown_action");
        }
        Object[] resolvedArguments = arguments == null ? new Object[0] : arguments;
        if (resolvedArguments.length != definition.argumentTypes().size()) {
            return WorkspaceActionValidation.rejected(
                    "argument_count_mismatch:expected=" + definition.argumentTypes().size()
                            + ":actual=" + resolvedArguments.length
            );
        }
        for (int index = 0; index < resolvedArguments.length; index++) {
            Object argument = resolvedArguments[index];
            if (argument == null) {
                continue;
            }
            Class<?> expected = definition.argumentTypes().get(index).javaType();
            if (!expected.isInstance(argument)) {
                return WorkspaceActionValidation.rejected(
                        "argument_type_mismatch:index=" + index
                                + ":expected=" + expected.getSimpleName()
                                + ":actual=" + argument.getClass().getSimpleName()
                );
            }
        }
        return WorkspaceActionValidation.ok();
    }
}
