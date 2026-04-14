package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.action.InventoryActionKind;
import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.core.ItemIdentity;

public interface ProtectionPolicy {
    boolean protects(ItemIdentity identity, InventoryActionKind actionKind);

    boolean protectsTarget(InventoryActionTarget target, InventoryActionKind actionKind);

    boolean protectsPortableContainers();

    static ProtectionPolicy allowAll() {
        return new ProtectionPolicy() {
            @Override
            public boolean protects(ItemIdentity identity, InventoryActionKind actionKind) {
                return false;
            }

            @Override
            public boolean protectsTarget(InventoryActionTarget target, InventoryActionKind actionKind) {
                return false;
            }

            @Override
            public boolean protectsPortableContainers() {
                return false;
            }
        };
    }
}
