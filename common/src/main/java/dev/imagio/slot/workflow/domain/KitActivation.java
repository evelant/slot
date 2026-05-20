package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityCollections;

import java.util.Set;

public record KitActivation(String kitId, int pageIndex, Set<ItemIdentity> putAwayIdentities) {
    public static final KitActivation NONE = new KitActivation("", 0);

    public KitActivation(String kitId, int pageIndex) {
        this(kitId, pageIndex, Set.of());
    }

    public KitActivation {
        kitId = kitId == null ? "" : kitId;
        pageIndex = Math.max(0, pageIndex);
        putAwayIdentities = isActive(kitId)
                ? ItemIdentityCollections.normalizedSet(putAwayIdentities)
                : Set.of();
    }

    public boolean isActive() {
        return isActive(kitId);
    }

    private static boolean isActive(String kitId) {
        return kitId != null && !kitId.isBlank();
    }
}
