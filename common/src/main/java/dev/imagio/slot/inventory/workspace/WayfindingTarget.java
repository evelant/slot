package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityCollections;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Per-storage projection used by the wayfinding HUD + atlas chip + in-world
 * glow. Acquisition targets say this storage holds at least one identity the
 * player still needs; put-away targets say this storage is a known destination
 * for carried clutter.
 *
 * <p>Coordinates are flat ints to mirror {@link dev.imagio.slot.workflow.domain.ChestAnchor}
 * — the common module avoids dragging in {@code net.minecraft.core.BlockPos}
 * for view-model records.
 */
public record WayfindingTarget(
        String storageId,
        String dimensionId,
        int worldX,
        int worldY,
        int worldZ,
        Set<ItemIdentity> missingIdentities,
        Set<ItemIdentity> kitMissingIdentities,
        Set<ItemIdentity> desiredMissingIdentities,
        Set<ItemIdentity> wantedMissingIdentities,
        Set<ItemIdentity> putAwayIdentities,
        int totalMissingCount,
        Scope scope
) {
    public WayfindingTarget(
            String storageId,
            String dimensionId,
            int worldX,
            int worldY,
            int worldZ,
            Set<ItemIdentity> missingIdentities,
            int totalMissingCount,
            Scope scope
    ) {
        this(
                storageId,
                dimensionId,
                worldX,
                worldY,
                worldZ,
                missingIdentities,
                scope == Scope.KIT ? missingIdentities : Set.of(),
                scope == Scope.PLAYER ? missingIdentities : Set.of(),
                scope == Scope.WANTED ? missingIdentities : Set.of(),
                scope == Scope.PUT_AWAY ? missingIdentities : Set.of(),
                totalMissingCount,
                scope);
    }

    public WayfindingTarget(
            String storageId,
            String dimensionId,
            int worldX,
            int worldY,
            int worldZ,
            Set<ItemIdentity> missingIdentities,
            Set<ItemIdentity> kitMissingIdentities,
            Set<ItemIdentity> desiredMissingIdentities,
            Set<ItemIdentity> wantedMissingIdentities,
            int totalMissingCount,
            Scope scope
    ) {
        this(
                storageId,
                dimensionId,
                worldX,
                worldY,
                worldZ,
                missingIdentities,
                kitMissingIdentities,
                desiredMissingIdentities,
                wantedMissingIdentities,
                Set.of(),
                totalMissingCount,
                scope);
    }

    public WayfindingTarget {
        storageId = storageId == null ? "" : storageId;
        dimensionId = dimensionId == null ? "" : dimensionId;
        kitMissingIdentities = copyIdentitySet(kitMissingIdentities);
        desiredMissingIdentities = copyIdentitySet(desiredMissingIdentities);
        wantedMissingIdentities = copyIdentitySet(wantedMissingIdentities);
        putAwayIdentities = copyIdentitySet(putAwayIdentities);
        LinkedHashSet<ItemIdentity> allMissing = new LinkedHashSet<>();
        if (missingIdentities != null) {
            allMissing.addAll(missingIdentities);
        }
        allMissing.addAll(kitMissingIdentities);
        allMissing.addAll(desiredMissingIdentities);
        allMissing.addAll(wantedMissingIdentities);
        allMissing.addAll(putAwayIdentities);
        missingIdentities = copyIdentitySet(allMissing);
        totalMissingCount = Math.max(0, totalMissingCount);
        scope = scope == null
                ? inferScope(kitMissingIdentities, desiredMissingIdentities, wantedMissingIdentities,
                        putAwayIdentities)
                : scope;
    }

    public boolean hasKitMissing() {
        return !kitMissingIdentities.isEmpty();
    }

    public boolean hasDesiredMissing() {
        return !desiredMissingIdentities.isEmpty();
    }

    public boolean hasWantedMissing() {
        return !wantedMissingIdentities.isEmpty();
    }

    public boolean hasPutAway() {
        return !putAwayIdentities.isEmpty();
    }

    public boolean putAwayOnly() {
        return hasPutAway()
                && kitMissingIdentities.isEmpty()
                && desiredMissingIdentities.isEmpty()
                && wantedMissingIdentities.isEmpty();
    }

    private static Scope inferScope(
            Set<ItemIdentity> kitMissingIdentities,
            Set<ItemIdentity> desiredMissingIdentities,
            Set<ItemIdentity> wantedMissingIdentities,
            Set<ItemIdentity> putAwayIdentities
    ) {
        if (kitMissingIdentities != null && !kitMissingIdentities.isEmpty()) {
            return Scope.KIT;
        }
        if ((desiredMissingIdentities == null || desiredMissingIdentities.isEmpty())
                && wantedMissingIdentities != null && !wantedMissingIdentities.isEmpty()) {
            return Scope.WANTED;
        }
        if ((desiredMissingIdentities == null || desiredMissingIdentities.isEmpty())
                && (wantedMissingIdentities == null || wantedMissingIdentities.isEmpty())
                && putAwayIdentities != null && !putAwayIdentities.isEmpty()) {
            return Scope.PUT_AWAY;
        }
        return Scope.PLAYER;
    }

    private static Set<ItemIdentity> copyIdentitySet(Set<ItemIdentity> source) {
        if (source == null || source.isEmpty()) {
            return Set.of();
        }
        return ItemIdentityCollections.normalizedSet(source);
    }

    public enum Scope {
        /** At least one missing identity is driven by the active kit. */
        KIT,
        /** All missing identities are player-global desired-count gaps. */
        PLAYER,
        /** All missing identities are player-global wanted-count gaps. */
        WANTED,
        /** Storage is a destination for active-workflow put-away clutter. */
        PUT_AWAY
    }
}
