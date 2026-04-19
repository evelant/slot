package dev.imagio.slot.workflow.domain;

public record KitActivation(String kitId, int pageIndex) {
    public static final KitActivation NONE = new KitActivation("", 0);

    public KitActivation {
        kitId = kitId == null ? "" : kitId;
        pageIndex = Math.max(0, pageIndex);
    }

    public boolean isActive() {
        return !kitId.isBlank();
    }
}
