package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.action.InventoryActionKind;
import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.LinkedHashSet;
import java.util.Set;

public record ProtectionSnapshotPolicy(
        Set<ItemIdentity> protectedIdentities,
        Set<InventoryActionTarget> protectedTargets,
        boolean protectPortableContainers
) implements ProtectionPolicy {
    public ProtectionSnapshotPolicy {
        protectedIdentities = protectedIdentities == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(protectedIdentities));
        protectedTargets = protectedTargets == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(protectedTargets));
    }

    @Override
    public boolean protects(ItemIdentity identity, InventoryActionKind actionKind) {
        return identity != null && protectedIdentities.contains(identity);
    }

    @Override
    public boolean protectsTarget(InventoryActionTarget target, InventoryActionKind actionKind) {
        return target != null && protectedTargets.stream().anyMatch(candidate -> candidate.stableKey().equals(target.stableKey()));
    }

    @Override
    public boolean protectsPortableContainers() {
        return protectPortableContainers;
    }
}
