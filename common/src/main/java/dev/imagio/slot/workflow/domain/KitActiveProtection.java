package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.action.InventoryActionKind;
import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Protection overlay that guards the active Kit's belt + offhand + bring identities
 * from trash/void/cleanup flows. Composes with the base workflow protection; does not
 * replace it. When no Kit is active the overlay is a no-op.
 */
public final class KitActiveProtection implements ProtectionPolicy {
    private final ProtectionPolicy base;
    private final Set<ItemIdentity> activeKitIdentities;

    public KitActiveProtection(ProtectionPolicy base, Set<ItemIdentity> activeKitIdentities) {
        this.base = base == null ? ProtectionPolicy.allowAll() : base;
        this.activeKitIdentities = activeKitIdentities == null
                ? Set.of()
                : Set.copyOf(new LinkedHashSet<>(activeKitIdentities));
    }

    public static Set<ItemIdentity> identitiesFor(KitMap kitMap) {
        if (kitMap == null) {
            return Set.of();
        }
        KitActivation activation = kitMap.activation();
        if (!activation.isActive()) {
            return Set.of();
        }
        KitDefinition kit = kitMap.kit(activation.kitId());
        if (kit == null) {
            return Set.of();
        }
        LinkedHashSet<ItemIdentity> identities = new LinkedHashSet<>();
        for (KitPage page : kit.pages()) {
            if (page == null) {
                continue;
            }
            for (int slotIndex = 0; slotIndex < KitPage.HOTBAR_SLOT_COUNT; slotIndex++) {
                ItemIdentity identity = page.slot(slotIndex);
                if (identity != null) {
                    identities.add(identity);
                }
            }
        }
        for (ItemIdentity identity : kit.bring()) {
            if (identity != null) {
                identities.add(identity);
            }
        }
        if (kit.offhand() != null) {
            identities.add(kit.offhand());
        }
        return Set.copyOf(identities);
    }

    public static ProtectionPolicy compose(ProtectionPolicy base, KitMap kitMap) {
        Set<ItemIdentity> activeIdentities = identitiesFor(kitMap);
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
        return activeKitIdentities.contains(identity);
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
