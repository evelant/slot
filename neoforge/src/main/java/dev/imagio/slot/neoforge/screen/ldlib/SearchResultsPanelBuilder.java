package dev.imagio.slot.neoforge.screen.ldlib;

import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.*;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceUi.*;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Search-results sister panel to {@link StoragePanelBuilder} ("Nearby
 * chests"). Walks every claimed chest's per-identity content summaries
 * and surfaces those holding items that match the active search query,
 * with a per-chest match-count badge.
 *
 * <p>Renders only while a search query is active. Docks at the right
 * edge of the workspace so it doesn't compete with the proximity-driven
 * chest chip panel on the left column.
 */
final class SearchResultsPanelBuilder {
    static final int PANEL_WIDTH = TRIAGE_PANEL_WIDTH;
    private static final int HEADER_HEIGHT = 14;
    private static final int PANEL_PADDING = 6;
    private static final int PANEL_GAP = 3;

    private final SlotWorkspaceUiController host;

    SearchResultsPanelBuilder(SlotWorkspaceUiController host) {
        this.host = host;
    }

    /**
     * Returns null when neither a search query nor an active kit needs
     * surfacing. Otherwise builds a content-fit pinned panel for
     * {@link LeftColumnBuilder} — under search it lists chests holding
     * query matches; with an active kit it lists chests holding
     * kit-needed items the player still has to fetch.
     */
    UIElement overlay() {
        boolean searchActive = !host.searchController.normalizedQuery().isBlank();
        boolean kitActive = !kitNeededIdentities().isEmpty();
        if (!searchActive && !kitActive) {
            return null;
        }
        List<Match> matches = computeMatches();
        if (matches.isEmpty()) {
            return null;
        }

        UIElement overlay = panel(GLASS).layout(layout -> layout
                .widthPercent(100)
                .paddingAll(PANEL_PADDING)
                .gapAll(PANEL_GAP)
                .flexDirection(FlexDirection.COLUMN));
        overlay.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());

        UIElement headerRow = new UIElement().layout(layout -> layout
                .widthPercent(100)
                .height(HEADER_HEIGHT)
                .gapAll(4)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.ROW));
        int totalMatches = 0;
        for (Match m : matches) {
            totalMatches += m.matchCount;
        }
        headerRow.addChildren(
                label("Chest locator", ACCENT).layout(layout -> layout.flex(1).height(12)),
                label(String.valueOf(totalMatches), MUTED).layout(layout -> layout.width(28).height(12))
        );
        headerRow.setAllowHitTest(false);
        overlay.addChild(headerRow);

        for (Match match : matches) {
            overlay.addChild(matchChip(match));
        }
        return overlay;
    }

    private List<Match> computeMatches() {
        boolean searchActive = !host.searchController.normalizedQuery().isBlank();
        java.util.Set<KitIdentity> kitNeeded = kitNeededIdentities();
        ArrayList<Match> out = new ArrayList<>();
        int proximateSkipped = 0;
        for (SlotWorkspaceViewModel.ChestChip chip : host.viewModel.chestChips()) {
            if (chip.contents().isEmpty()) {
                continue;
            }
            int searchMatchCount = 0;
            int kitMatchCount = 0;
            int matchIdentities = 0;
            for (SlotWorkspaceViewModel.ChestContentSummary summary : chip.contents()) {
                boolean searchHit = searchActive && host.searchController.matchesContentSummary(summary);
                boolean kitHit = matchesKitNeed(summary, kitNeeded);
                if (searchHit || kitHit) {
                    matchIdentities++;
                    if (searchHit) {
                        searchMatchCount += summary.count();
                    }
                    if (kitHit) {
                        kitMatchCount += summary.count();
                    }
                }
            }
            if (matchIdentities <= 0) {
                continue;
            }
            // A chest matched only because of an active kit need (no
            // search hits in it) and it's already in the proximity
            // panel on the same column — listing it twice reads as
            // redundant. Search hits always include their chest
            // regardless of proximity, since search is the only place
            // to find query matches across non-proximate storage.
            if (searchMatchCount == 0 && chip.proximate()) {
                proximateSkipped++;
                continue;
            }
            out.add(new Match(chip, searchMatchCount + kitMatchCount, matchIdentities));
        }
        // Proximate chests first (closer = more actionable), then by match
        // count descending so the chest with the most hits sits on top.
        out.sort(Comparator
                .comparing((Match m) -> !m.chip.proximate())
                .thenComparingInt((Match m) -> -m.matchCount)
                .thenComparing(m -> m.chip.label(), String.CASE_INSENSITIVE_ORDER));
        if (dev.imagio.slot.SlotDebugLog.verbose()) {
            dev.imagio.slot.SlotCommon.LOGGER.info(
                    "[SLOT] Chest locator query searchActive={} query='{}' kitNeededCount={} chipsWalked={} matches={} proximateSkippedDueToProximityPanel={}",
                    searchActive,
                    host.searchController.normalizedQuery(),
                    kitNeeded.size(),
                    host.viewModel.chestChips().size(),
                    out.size(),
                    proximateSkipped
            );
        }
        return out;
    }

    /**
     * Identities the active kit needs but the player isn't carrying.
     * Drives the chest-locator's "for kit" mode: the panel surfaces
     * every chest holding any of these so the player can see at a
     * glance which storage areas to visit. Reads off the atlas-item
     * {@code kitNeeded} flag instead of duplicating the kit-need
     * computation server-side.
     */
    private java.util.Set<KitIdentity> kitNeededIdentities() {
        java.util.LinkedHashSet<KitIdentity> identities = new java.util.LinkedHashSet<>();
        for (SlotWorkspaceViewModel.AtlasItem item : host.viewModel.atlasItems()) {
            if (item.kitNeeded()) {
                identities.add(new KitIdentity(item.identity().itemId(), item.identity().componentFingerprint()));
            }
        }
        return identities;
    }

    private boolean matchesKitNeed(SlotWorkspaceViewModel.ChestContentSummary summary, java.util.Set<KitIdentity> kitNeeded) {
        if (kitNeeded.isEmpty() || summary == null) {
            return false;
        }
        return kitNeeded.contains(new KitIdentity(summary.itemId(), summary.componentFingerprint()));
    }

    private record KitIdentity(String itemId, String componentFingerprint) {
    }

    private UIElement matchChip(Match match) {
        // Same wayfinding chip as the proximity panel — name +
        // missing-icons + compass + distance — so the player sees the
        // same shape regardless of which atlas panel they're scanning.
        dev.imagio.slot.inventory.workspace.WayfindingTarget target =
                dev.imagio.slot.neoforge.client.wayfinding.WayfindingTargetCache.targetFor(match.chip.storageId());
        UIElement chip = WayfindingChip.build(match.chip, target);
        installSearchChipHover(chip, match);
        return chip;
    }

    private SlotWorkspaceViewModel.ChestClusterDescriptor cluster(String clusterId) {
        if (clusterId == null || clusterId.isEmpty()) {
            return null;
        }
        for (SlotWorkspaceViewModel.ChestClusterDescriptor cluster : host.viewModel.chestClusters()) {
            if (clusterId.equals(cluster.clusterId())) {
                return cluster;
            }
        }
        return null;
    }

    /**
     * Cross-surface hover linking: hovering this row sets
     * {@code hoveredStorageId} so atlas cards / chest chips backed by
     * the same chest light up. The reverse direction lights this row
     * when the player hovers an atlas card whose identity has a
     * matching summary in the chest. Reuses the same hover plumbing
     * that {@link StoragePanelBuilder#installChipHover} already drives
     * for the proximity panel.
     */
    private void installSearchChipHover(UIElement chip, Match match) {
        String storageId = match.chip.storageId();
        chip.addEventListener(UIEvents.MOUSE_ENTER, event -> host.hoveredStorageId = storageId, true);
        chip.addEventListener(UIEvents.MOUSE_LEAVE, event -> {
            if (storageId.equals(host.hoveredStorageId)) {
                host.hoveredStorageId = null;
            }
        }, true);
        boolean[] lastLit = {false};
        chip.addEventListener(UIEvents.TICK, event -> {
            boolean lit = isHoveredAtlasIdentityInChest(match.chip);
            if (lit == lastLit[0]) {
                return;
            }
            lastLit[0] = lit;
            chip.style(style -> style.overlayTexture(lit ? rect(HOVER_ACCENT_OVERLAY) : IGuiTexture.EMPTY));
        });
    }

    private boolean isHoveredAtlasIdentityInChest(SlotWorkspaceViewModel.ChestChip target) {
        SlotWorkspaceViewModel.IdentityRef hovered = host.hoveredAtlasIdentity;
        if (hovered == null) {
            return false;
        }
        String hoveredItemId = hovered.itemId();
        String hoveredFingerprint = hovered.componentFingerprint();
        for (SlotWorkspaceViewModel.ChestContentSummary summary : target.contents()) {
            if (hoveredItemId.equals(summary.itemId())
                    && hoveredFingerprint.equals(summary.componentFingerprint())) {
                return true;
            }
        }
        return false;
    }

    private static String shortDimension(String dimensionId) {
        if (dimensionId == null) {
            return "";
        }
        int colon = dimensionId.indexOf(':');
        String tail = colon < 0 ? dimensionId : dimensionId.substring(colon + 1);
        if (tail.startsWith("the_")) {
            tail = tail.substring(4);
        }
        return tail;
    }

    private record Match(SlotWorkspaceViewModel.ChestChip chip, int matchCount, int matchIdentities) {
    }
}
