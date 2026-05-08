package dev.imagio.slot.ui.action;

import java.util.Objects;

public record WorkspaceActionArgumentValue(
        WorkspaceActionArgumentType type,
        boolean isNull,
        String stringValue,
        Integer integerValue,
        Double doubleValue
) {
    public WorkspaceActionArgumentValue {
        Objects.requireNonNull(type, "type");
        if (isNull) {
            stringValue = null;
            integerValue = null;
            doubleValue = null;
        }
    }

    public static WorkspaceActionArgumentValue of(WorkspaceActionArgumentType type, Object value) {
        Objects.requireNonNull(type, "type");
        if (value == null) {
            return new WorkspaceActionArgumentValue(type, true, null, null, null);
        }
        return switch (type) {
            case STRING -> new WorkspaceActionArgumentValue(type, false, (String) value, null, null);
            case INTEGER -> new WorkspaceActionArgumentValue(type, false, null, (Integer) value, null);
            case DOUBLE -> new WorkspaceActionArgumentValue(type, false, null, null, (Double) value);
        };
    }

    public Object toObject() {
        if (isNull) {
            return null;
        }
        return switch (type) {
            case STRING -> stringValue;
            case INTEGER -> integerValue;
            case DOUBLE -> doubleValue;
        };
    }
}
