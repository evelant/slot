package dev.imagio.slot.atlas.lod;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves a world-space drop coordinate to an
 * {@code (islandId, ordinal)} pair against an {@link AtlasLayoutResult}.
 *
 * <p>Phase 2.2 drag-drop is ordinal: the player drops at a pixel, the
 * client converts to "insert at the position the user is targeting"
 * before the RPC fires. Hit-test priority:
 *
 * <ol>
 *   <li>If the drop coord lands on an existing item, the result is that
 *       item's ordinal — the new entry will land in front of it (the
 *       projection shifts the dropped-on item +1).</li>
 *   <li>If the drop coord lands inside an island bounding box but not on
 *       any item, the result is "append to that island" (ordinal = item
 *       count).</li>
 *   <li>Otherwise no resolution — caller decides whether to fall back
 *       to triage, an island under selection, etc.</li>
 * </ol>
 *
 * <p>Triage islands are skipped: triage assignments use a dedicated
 * "clear home" code path, not ordinal placement.
 */
public final class AtlasDropResolver {
    private AtlasDropResolver() {
    }

    /**
     * Resolve the drop coord against the latest atlas layout. Returns
     * {@code null} if no eligible island contains the coordinate.
     *
     * <p>The view model is consulted only to read each item's ordinal in
     * island-local order — the layout result alone does not encode that.
     * The order of {@code viewModel.atlasItems()} is the canonical
     * ordering the server-side projection emits.
     */
    public static Resolution resolve(
            SlotWorkspaceViewModel viewModel,
            AtlasLayoutResult layout,
            int worldX,
            int worldY
    ) {
        if (viewModel == null || layout == null || layout == AtlasLayoutResult.EMPTY) {
            return null;
        }
        // 1) Hit-test items first — they're inside their islands so we
        //    must check them before falling through to "drop on island
        //    body = append".
        AtlasLayoutResult.ItemPlacement hitItem = null;
        SlotWorkspaceViewModel.IdentityRef hitIdentity = null;
        for (SlotWorkspaceViewModel.AtlasItem item : viewModel.atlasItems()) {
            if (item == null) {
                continue;
            }
            AtlasLayoutResult.ItemPlacement place = layout.placementOf(item.identity());
            if (place != null && containsPoint(place, worldX, worldY)) {
                hitItem = place;
                hitIdentity = item.identity();
                break;
            }
        }
        if (hitItem != null) {
            int ordinal = ordinalOf(viewModel, hitItem.islandId(), hitIdentity);
            if (ordinal >= 0) {
                return new Resolution(hitItem.islandId(), ordinal);
            }
        }
        // 2) No item hit — fall back to the island chrome the drop
        //    landed inside. Pick an insertion ordinal based on which
        //    direction the drop coord sits relative to the existing
        //    cards: drop above-or-left of card N → insert at N (shift it
        //    +1); drop below-or-right of the last card → append. This
        //    lets players reorder by dropping in the gap between two
        //    cards rather than having to drop directly onto a target.
        for (SlotWorkspaceViewModel.AtlasIsland island : viewModel.islands()) {
            if (island == null || island.kind() == VisualAtlasIslandKind.TRIAGE) {
                continue;
            }
            AtlasLayoutResult.IslandPlacement place = layout.islandPlacementOf(island.islandId());
            if (place != null && containsIslandPoint(place, worldX, worldY)) {
                int ordinal = nearestInsertionOrdinal(viewModel, layout, island.islandId(), worldX, worldY);
                return new Resolution(island.islandId(), ordinal);
            }
        }
        return null;
    }

    /**
     * Pick the insertion ordinal for a drop landing inside an island's
     * chrome but not on any existing card. Walks the island's cards in
     * canonical (ordinal) order and returns the index of the first card
     * whose center sits below-or-right of the drop coord; if none, we're
     * past every existing card and the result is "append" (count).
     *
     * <p>Row-major comparison: drops on row N take priority over row
     * N+1, so dropping below the first row inserts after every item in
     * the first row, even if the X coord would have been "left of" some
     * specific card on row N+1.
     */
    private static int nearestInsertionOrdinal(
            SlotWorkspaceViewModel viewModel,
            AtlasLayoutResult layout,
            String islandId,
            int worldX,
            int worldY
    ) {
        int ordinal = 0;
        for (SlotWorkspaceViewModel.AtlasItem item : viewModel.atlasItems()) {
            if (item == null || !islandId.equals(item.islandId())) {
                continue;
            }
            AtlasLayoutResult.ItemPlacement place = layout.placementOf(item.identity());
            if (place == null) {
                ordinal++;
                continue;
            }
            int cardCenterX = place.x() + place.width() / 2;
            int cardCenterY = place.y() + place.height() / 2;
            // Strict row-major: if the drop's Y is above the card's
            // vertical midpoint, insert here. If on the same row (within
            // a card's height), insert when the drop is left of center.
            if (worldY < cardCenterY - place.height() / 2) {
                return ordinal;
            }
            if (worldY < cardCenterY + place.height() / 2 && worldX < cardCenterX) {
                return ordinal;
            }
            ordinal++;
        }
        return ordinal;
    }

    /**
     * Count atlas items currently homed to {@code islandId}. Server
     * mutation will clamp if the live snapshot has shifted, so this only
     * needs to match the client-side view.
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
     * The ordinal an identity occupies inside its island's
     * canonical-order list (as emitted by the view model's atlas-items
     * projection). Returns {@code -1} if not found.
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
     * Atlas items grouped by island in canonical order. Useful for tests
     * and debug overlays — keeps callers from re-walking the view model.
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

    private static boolean containsPoint(AtlasLayoutResult.ItemPlacement place, int x, int y) {
        return x >= place.x()
                && y >= place.y()
                && x < place.x() + place.width()
                && y < place.y() + place.height();
    }

    private static boolean containsIslandPoint(AtlasLayoutResult.IslandPlacement place, int x, int y) {
        return x >= place.x()
                && y >= place.y()
                && x < place.x() + place.width()
                && y < place.y() + place.height();
    }

    public record Resolution(String islandId, int ordinal) {
        public Resolution {
            islandId = islandId == null ? "" : islandId;
            ordinal = Math.max(0, ordinal);
        }
    }
}
