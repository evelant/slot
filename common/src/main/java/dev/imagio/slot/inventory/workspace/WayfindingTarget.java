package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;

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
        int totalMissingCount,
        Scope scope
) {
    public WayfindingTarget {
        storageId = storageId == null ? "" : storageId;
        dimensionId = dimensionId == null ? "" : dimensionId;
        missingIdentities = missingIdentities == null
                ? Set.of()
                : Set.copyOf(new LinkedHashSet<>(missingIdentities));
        totalMissingCount = Math.max(0, totalMissingCount);
        scope = scope == null ? Scope.PLAYER : scope;
    }

    public enum Scope {
        /** At least one missing identity is driven by the active kit. */
        KIT,
        /** All missing identities are player-global desired-count gaps. */
        PLAYER
    }
}
