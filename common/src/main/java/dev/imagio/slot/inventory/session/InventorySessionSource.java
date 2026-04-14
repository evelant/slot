package dev.imagio.slot.inventory.session;

import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;

public interface InventorySessionSource {
    InventoryHostDescriptor resolveHost();

    InventoryAuthoritySnapshot readAuthority(InventoryHostDescriptor host);
}
