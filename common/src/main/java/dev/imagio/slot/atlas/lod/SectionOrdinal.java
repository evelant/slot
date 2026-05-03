package dev.imagio.slot.atlas.lod;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Helpers for reading per-section (per-island) ordinals from the
 * authoritative server-projected {@code atlasItems} list. Items within
 * a section are emitted in canonical ordinal order; client code reads
 * positions from the iteration order.
 *
 * <p>Replaces the world-coord {@code AtlasDropResolver} from the
 * pan/zoom era — flow-grid drag/drop reads ordinals out of the
 * section's flex children, not by hit-testing world coordinates.
 */
public final class SectionOrdinal {
    private SectionOrdinal() {
    }

    /**
     * The ordinal an identity occupies inside its island's
     * canonical-order list. Returns {@code -1} if not found.
     */
    public static int ordinalOf(
            SlotWorkspaceViewModel viewModel,
            String islandId,
            SlotWorkspaceViewModel.IdentityRef identity
    ) {
        if (viewModel == null || islandId == null || identity == null) {
            return -1;
        }
        int ordinal = 0;
        for (SlotWorkspaceViewModel.AtlasItem candidate : viewModel.atlasItems()) {
            if (candidate == null || !islandId.equals(candidate.islandId())) {
                continue;
            }
            if (candidate.identity().equals(identity)) {
                return ordinal;
            }
            ordinal++;
        }
        return -1;
    }

    /**
     * Count atlas items currently homed to {@code islandId}.
     */
    public static int countItemsInIsland(SlotWorkspaceViewModel viewModel, String islandId) {
        if (viewModel == null || islandId == null || islandId.isBlank()) {
            return 0;
        }
        int count = 0;
        for (SlotWorkspaceViewModel.AtlasItem item : viewModel.atlasItems()) {
            if (item != null && islandId.equals(item.islandId())) {
                count++;
            }
        }
        return count;
    }

    /**
     * Atlas items grouped by island in canonical order.
     */
    public static List<SlotWorkspaceViewModel.AtlasItem> itemsInIsland(
            SlotWorkspaceViewModel viewModel,
            String islandId
    ) {
        ArrayList<SlotWorkspaceViewModel.AtlasItem> result = new ArrayList<>();
        if (viewModel == null || islandId == null || islandId.isBlank()) {
            return result;
        }
        for (SlotWorkspaceViewModel.AtlasItem item : viewModel.atlasItems()) {
            if (item != null && islandId.equals(item.islandId())) {
                result.add(item);
            }
        }
        return result;
    }
}
