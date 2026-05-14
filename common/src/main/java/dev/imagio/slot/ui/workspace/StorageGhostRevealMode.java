package dev.imagio.slot.ui.workspace;

public enum StorageGhostRevealMode {
    COLLAPSED,
    PROXIMATE,
    TRACKED;

    public boolean revealsProximate() {
        return this == PROXIMATE || this == TRACKED;
    }

    public boolean revealsTracked() {
        return this == TRACKED;
    }
}
