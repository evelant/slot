package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityCollections;
import dev.imagio.slot.inventory.core.SlotResourceCollections;
import dev.imagio.slot.inventory.core.SlotResourceIdentity;

public record CraftRunAlternative(
        ItemIdentity identity,
        String label,
        SlotResourceIdentity resourceIdentity
) {
    public CraftRunAlternative(ItemIdentity identity, String label) {
        this(identity, label, SlotResourceIdentity.item(identity));
    }

    public CraftRunAlternative {
        resourceIdentity = SlotResourceCollections.key(resourceIdentity != null
                ? resourceIdentity
                : SlotResourceIdentity.item(identity));
        identity = resourceIdentity != null && resourceIdentity.item()
                ? resourceIdentity.toItemIdentity()
                : ItemIdentityCollections.key(identity);
        if (resourceIdentity != null && resourceIdentity.fluid()) {
            identity = null;
        }
        label = label == null || label.isBlank()
                ? resourceIdentity == null ? "Ingredient" : resourceIdentity.id()
                : label.trim();
    }

    public boolean present() {
        return resourceIdentity != null;
    }
}
