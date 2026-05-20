package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityCollections;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Per-chest projection used by the wayfinding HUD + atlas chip + in-world
 * glow. A target says: this chest holds at least one identity the player
 * still needs (kit-active, or a desired-count gap), with the gap-by-gap
 * intersection in {@code missingIdentities}. Built by
 * {@link SlotWorkspaceViewModel#project} alongside the existing
 * kit-needed projection.
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
                totalMissingCount,
                scope);
    }

    public WayfindingTarget {
        storageId = storageId == null ? "" : storageId;
        dimensionId = dimensionId == null ? "" : dimensionId;
        kitMissingIdentities = copyIdentitySet(kitMissingIdentities);
        desiredMissingIdentities = copyIdentitySet(desiredMissingIdentities);
        wantedMissingIdentities = copyIdentitySet(wantedMissingIdentities);
        LinkedHashSet<ItemIdentity> allMissing = new LinkedHashSet<>();
        if (missingIdentities != null) {
            allMissing.addAll(missingIdentities);
        }
        allMissing.addAll(kitMissingIdentities);
        allMissing.addAll(desiredMissingIdentities);
        allMissing.addAll(wantedMissingIdentities);
        missingIdentities = copyIdentitySet(allMissing);
        totalMissingCount = Math.max(0, totalMissingCount);
        scope = scope == null
                ? inferScope(kitMissingIdentities, desiredMissingIdentities, wantedMissingIdentities)
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

    private static Scope inferScope(
            Set<ItemIdentity> kitMissingIdentities,
            Set<ItemIdentity> desiredMissingIdentities,
            Set<ItemIdentity> wantedMissingIdentities
    ) {
        if (kitMissingIdentities != null && !kitMissingIdentities.isEmpty()) {
            return Scope.KIT;
        }
        if ((desiredMissingIdentities == null || desiredMissingIdentities.isEmpty())
                && wantedMissingIdentities != null && !wantedMissingIdentities.isEmpty()) {
            return Scope.WANTED;
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
        WANTED
    }
}
