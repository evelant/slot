package dev.imagio.slot.inventory.workspace;

/**
 * Server-side projection intent for non-proximate remembered/tracked storage.
 */
public enum RemoteStorageDetailIntent {
    NONE,
    INTENT_ONLY,
    SEARCH,
    TRACKED_XRAY;

    public static RemoteStorageDetailIntent parse(String value) {
        if (value == null || value.isBlank()) {
            return INTENT_ONLY;
        }
        for (RemoteStorageDetailIntent intent : values()) {
            if (intent.name().equalsIgnoreCase(value)) {
                return intent;
            }
        }
        return INTENT_ONLY;
    }

    public static RemoteStorageDetailIntent effective(
            RemoteStorageDetailIntent requested,
            String searchQuery
    ) {
        RemoteStorageDetailIntent intent = requested == null ? INTENT_ONLY : requested;
        if (intent == TRACKED_XRAY) {
            return TRACKED_XRAY;
        }
        if (!WorkspaceSearchQuery.normalized(searchQuery).isBlank()) {
            return SEARCH;
        }
        return intent;
    }

    public boolean includesAllRemote() {
        return this == TRACKED_XRAY;
    }

    public boolean includesSearchMatches() {
        return this == SEARCH || this == TRACKED_XRAY;
    }
}
