package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.action.InventoryActionKind;
import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Cleanup-protection overlay backed by the player-global desired count
 * map. Any identity the player has marked as "always keep N" is protected
 * from trash/void/cleanup flows regardless of the active kit. Composes
 * with the base policy and with {@link KitActiveProtection}; does not
 * replace either. Empty when no global desired counts are set.
 */
public final class DesiredCountProtection implements ProtectionPolicy {
    private final ProtectionPolicy base;
    private final Set<ItemIdentity> protectedIdentities;

    public DesiredCountProtection(ProtectionPolicy base, Set<ItemIdentity> protectedIdentities) {
        this.base = base == null ? ProtectionPolicy.allowAll() : base;
        this.protectedIdentities = protectedIdentities == null
                ? Set.of()
                : Set.copyOf(new LinkedHashSet<>(protectedIdentities));
    }

    public static Set<ItemIdentity> identitiesFor(Map<ItemIdentity, Integer> playerDesiredCounts) {
        if (playerDesiredCounts == null || playerDesiredCounts.isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<ItemIdentity> identities = new LinkedHashSet<>();
        for (Map.Entry<ItemIdentity, Integer> entry : playerDesiredCounts.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null && entry.getValue() > 0) {
                identities.add(entry.getKey());
            }
        }
        return Set.copyOf(identities);
    }

    public static ProtectionPolicy compose(ProtectionPolicy base, Map<ItemIdentity, Integer> playerDesiredCounts) {
        Set<ItemIdentity> identities = identitiesFor(playerDesiredCounts);
        if (identities.isEmpty()) {
            return base == null ? ProtectionPolicy.allowAll() : base;
        }
        return new DesiredCountProtection(base, identities);
    }

    public Set<ItemIdentity> protectedIdentities() {
        return protectedIdentities;
    }

    @Override
    public boolean protects(ItemIdentity identity, InventoryActionKind actionKind) {
        if (base.protects(identity, actionKind)) {
            return true;
        }
        if (identity == null || !isCleanupKind(actionKind)) {
            return false;
        }
        return protectedIdentities.contains(identity);
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
        if (!(other instanceof DesiredCountProtection that)) {
            return false;
        }
        return Objects.equals(base, that.base)
                && Objects.equals(protectedIdentities, that.protectedIdentities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(base, protectedIdentities);
    }
}
