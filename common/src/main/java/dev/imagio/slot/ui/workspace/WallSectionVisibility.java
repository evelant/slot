package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.inventory.workspace.WorkspaceItemTargets;

import java.util.ArrayList;
import java.util.List;

public final class WallSectionVisibility {
    private WallSectionVisibility() {
    }

    public record Result(
            List<SlotWorkspaceViewModel.AtlasItem> visibleCards,
            int nearbyToggleCount,
            boolean nearbyExpanded
    ) {
        public Result {
            visibleCards = visibleCards == null ? List.of() : List.copyOf(visibleCards);
            nearbyToggleCount = Math.max(0, nearbyToggleCount);
        }

        public boolean showNearbyToggle() {
            return nearbyToggleCount > 0;
        }

        public boolean hasVisibleContent() {
            return !visibleCards.isEmpty() || showNearbyToggle();
        }
    }

    public static Result classify(
            List<SlotWorkspaceViewModel.AtlasItem> cards,
            boolean filtering,
            boolean nearbyExpanded,
            StorageGhostRevealMode revealMode,
            boolean forceRevealGhosts
    ) {
        return classify(cards, filtering, nearbyExpanded, revealMode, forceRevealGhosts, true);
    }

    public static Result classify(
            List<SlotWorkspaceViewModel.AtlasItem> cards,
            boolean filtering,
            boolean nearbyExpanded,
            StorageGhostRevealMode revealMode,
            boolean forceRevealGhosts,
            boolean allowCollapsedNearbyToggle
    ) {
        if (cards == null || cards.isEmpty()) {
            return new Result(List.of(), 0, nearbyExpanded);
        }
        StorageGhostRevealMode mode = revealMode == null ? StorageGhostRevealMode.COLLAPSED : revealMode;
        ArrayList<SlotWorkspaceViewModel.AtlasItem> visible = new ArrayList<>(cards.size());
        int ordinaryProximateGhosts = 0;
        for (SlotWorkspaceViewModel.AtlasItem item : cards) {
            if (item == null) {
                continue;
            }
            if (item.carried()) {
                visible.add(item);
                continue;
            }
            boolean proximateGhost = isProximateGhost(item);
            boolean intentGhost = isIntentGhost(item);
            boolean ordinaryProximate = proximateGhost && !intentGhost;
            if (allowCollapsedNearbyToggle
                    && ordinaryProximate
                    && !filtering
                    && !forceRevealGhosts
                    && mode == StorageGhostRevealMode.COLLAPSED) {
                ordinaryProximateGhosts++;
            }
            if (shouldRevealGhost(item, filtering, nearbyExpanded, mode, forceRevealGhosts)) {
                visible.add(item);
            }
        }
        boolean showToggle = ordinaryProximateGhosts > 0
                && allowCollapsedNearbyToggle
                && !filtering
                && !forceRevealGhosts
                && mode == StorageGhostRevealMode.COLLAPSED;
        return new Result(visible, showToggle ? ordinaryProximateGhosts : 0, nearbyExpanded);
    }

    private static boolean shouldRevealGhost(
            SlotWorkspaceViewModel.AtlasItem item,
            boolean filtering,
            boolean nearbyExpanded,
            StorageGhostRevealMode mode,
            boolean forceRevealGhosts
    ) {
        if (forceRevealGhosts || filtering || isIntentGhost(item)) {
            return true;
        }
        if (isProximateGhost(item)) {
            return nearbyExpanded || mode.revealsProximate();
        }
        return isTrackedGhost(item) && mode.revealsTracked();
    }

    private static boolean isIntentGhost(SlotWorkspaceViewModel.AtlasItem item) {
        if (item == null) {
            return false;
        }
        if (item.kitNeeded()) {
            return true;
        }
        if (item.acceptedWorkflowInput() && isProximateGhost(item)) {
            return true;
        }
        int carried = item.carried() ? Math.max(0, item.totalCount()) : 0;
        return WorkspaceItemTargets.from(item).hasAnyGap(carried);
    }

    private static boolean isProximateGhost(SlotWorkspaceViewModel.AtlasItem item) {
        return item != null && !item.carried() && Math.max(0, item.proximateCount()) > 0;
    }

    private static boolean isTrackedGhost(SlotWorkspaceViewModel.AtlasItem item) {
        return item != null && !item.carried() && !item.elsewhere().isEmpty();
    }
}
