package dev.imagio.slot.session;

public record ServerMenuRef(
        String menuClassName,
        int containerId
) {
    public ServerMenuRef {
        menuClassName = menuClassName == null ? "" : menuClassName;
        containerId = Math.max(-1, containerId);
    }

    public boolean present() {
        return !menuClassName.isBlank() && containerId >= 0;
    }
}
