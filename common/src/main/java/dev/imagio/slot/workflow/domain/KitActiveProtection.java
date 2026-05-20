package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.action.InventoryActionKind;
import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityCollections;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Protection overlay that guards the active workflow tab's effective local
 * targets (parent + variant membership, desired/wanted counts, belt/offhand)
 * from trash/void/cleanup flows. Composes with the base workflow protection;
 * does not replace it. When no tab is active the overlay is a no-op.
 */
public final class KitActiveProtection implements ProtectionPolicy {
    private final ProtectionPolicy base;
    private final Set<ItemIdentity> activeKitIdentities;

    public KitActiveProtection(ProtectionPolicy base, Set<ItemIdentity> activeKitIdentities) {
        this.base = base == null ? ProtectionPolicy.allowAll() : base;
        this.activeKitIdentities = ItemIdentityCollections.normalizedSet(activeKitIdentities);
    }

    public static Set<ItemIdentity> identitiesFor(KitMap kitMap) {
        return identitiesFor(kitMap, Map.of(), Map.of());
    }

    /**
     * Collect the identities the active workflow tab considers protected.
     */
    public static Set<ItemIdentity> identitiesFor(
            KitMap kitMap,
            Map<String, Map<ItemIdentity, Integer>> kitDesiredCounts
    ) {
        return identitiesFor(kitMap, kitDesiredCounts, Map.of());
    }

    public static Set<ItemIdentity> identitiesFor(
            KitMap kitMap,
            Map<String, Map<ItemIdentity, Integer>> kitDesiredCounts,
            Map<String, Map<ItemIdentity, Integer>> kitWantedCounts
    ) {
        return WorkflowTabTargets.protectedIdentities(kitMap, kitDesiredCounts, kitWantedCounts);
    }

    public static ProtectionPolicy compose(ProtectionPolicy base, KitMap kitMap) {
        return compose(base, kitMap, Map.of());
    }

    public static ProtectionPolicy compose(
            ProtectionPolicy base,
            KitMap kitMap,
            Map<String, Map<ItemIdentity, Integer>> kitDesiredCounts
    ) {
        return compose(base, kitMap, kitDesiredCounts, Map.of());
    }

    public static ProtectionPolicy compose(
            ProtectionPolicy base,
            KitMap kitMap,
            Map<String, Map<ItemIdentity, Integer>> kitDesiredCounts,
            Map<String, Map<ItemIdentity, Integer>> kitWantedCounts
    ) {
        Set<ItemIdentity> activeIdentities = identitiesFor(kitMap, kitDesiredCounts, kitWantedCounts);
        if (activeIdentities.isEmpty()) {
            return base == null ? ProtectionPolicy.allowAll() : base;
        }
        return new KitActiveProtection(base, activeIdentities);
    }

    public Set<ItemIdentity> activeKitIdentities() {
        return activeKitIdentities;
    }

    @Override
    public boolean protects(ItemIdentity identity, InventoryActionKind actionKind) {
        if (base.protects(identity, actionKind)) {
            return true;
        }
        if (identity == null) {
            return false;
        }
        if (!isCleanupKind(actionKind)) {
            return false;
        }
        return ItemIdentityCollections.contains(activeKitIdentities, identity);
    }

    @Override
    public boolean protectsTarget(InventoryActionTarget target, InventoryActionKind actionKind) {
        return base.protectsTarget(target, actionKind);
    }

    @Override
    public boolean protectsPortableContainers() {
        return base.protectsPortableContainers();
    }

    private static boolean isCleanupKind(InventoryActionKind kind) {
        if (kind == null) {
            return true;
        }
        return switch (kind) {
            case TRASH, VOID, DROP_TO_WORLD -> true;
            default -> false;
        };
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof KitActiveProtection that)) {
            return false;
        }
        return Objects.equals(base, that.base)
                && Objects.equals(activeKitIdentities, that.activeKitIdentities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(base, activeKitIdentities);
    }
}
