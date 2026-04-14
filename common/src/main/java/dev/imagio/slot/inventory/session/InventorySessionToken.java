package dev.imagio.slot.inventory.session;

public record InventorySessionToken(
        String sessionId,
        long revision
) {
    public InventorySessionToken {
        sessionId = sessionId == null ? "" : sessionId;
        revision = Math.max(0L, revision);
    }

    public boolean present() {
        return !sessionId.isBlank() && revision > 0L;
    }
}
