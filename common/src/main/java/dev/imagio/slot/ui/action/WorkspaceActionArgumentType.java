package dev.imagio.slot.ui.action;

public enum WorkspaceActionArgumentType {
    STRING(String.class),
    INTEGER(Integer.class),
    DOUBLE(Double.class);

    private final Class<?> javaType;

    WorkspaceActionArgumentType(Class<?> javaType) {
        this.javaType = javaType;
    }

    public Class<?> javaType() {
        return javaType;
    }
}
