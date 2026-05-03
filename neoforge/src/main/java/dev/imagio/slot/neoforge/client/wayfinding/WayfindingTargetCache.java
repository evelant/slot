package dev.imagio.slot.neoforge.client.wayfinding;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.inventory.workspace.WayfindingTarget;
import dev.imagio.slot.neoforge.client.SlotClientWorkspaceCache;

import java.util.List;

/**
 * Thin accessor over the client-side workspace cache that exposes only
 * the wayfinding-target list. Glow + chip + HUD renderers consume this
 * instead of reaching into the full view-model surface so the renderers
 * stay focused on their concern.
 *
 * <p>No separate storage: the latest view-model already lives in
 * {@link SlotClientWorkspaceCache}, and the wayfinding list is part of
 * that record. Reading per frame is cheap (the list is immutable).
 */
public final class WayfindingTargetCache {
    private WayfindingTargetCache() {
    }

    public static List<WayfindingTarget> targets() {
        SlotWorkspaceViewModel viewModel = SlotClientWorkspaceCache.latest();
        return viewModel == null ? List.of() : viewModel.wayfindingTargets();
    }

    public static WayfindingTarget targetFor(String storageId) {
        if (storageId == null || storageId.isBlank()) {
            return null;
        }
        for (WayfindingTarget target : targets()) {
            if (storageId.equals(target.storageId())) {
                return target;
            }
        }
        return null;
    }
}
