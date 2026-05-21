package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;

public record CraftRunAlternative(
        ItemIdentity identity,
        String label
) {
    public CraftRunAlternative {
        label = label == null || label.isBlank()
                ? identity == null ? "Ingredient" : identity.itemId()
                : label.trim();
    }

    public boolean present() {
        return identity != null;
    }
}
