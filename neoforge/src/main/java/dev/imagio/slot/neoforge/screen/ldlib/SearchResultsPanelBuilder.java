package dev.imagio.slot.neoforge.screen.ldlib;

import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.*;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceUi.*;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
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
    private static final int CHIP_HEIGHT = 18;
    private static final int HEADER_HEIGHT = 14;
    private static final int PANEL_PADDING = 6;
    private static final int PANEL_GAP = 3;
    private static final int MAX_CHIPS = 12;

    private final SlotWorkspaceUiController host;

    SearchResultsPanelBuilder(SlotWorkspaceUiController host) {
        this.host = host;
    }

    private int panelHeight(int matchCount) {
        int bodyChips = Math.min(matchCount, MAX_CHIPS);
        // Layout components: 2 paddings + header + 1 gap (header→scroller)
        // + chip stack (chips + inter-chip gaps) + scroller chrome buffer.
        // The chrome buffer is what was previously under-sized: a single-
        // chip panel was sizing exactly to the content rect and the
        // ScrollerView's internal overhead (a few px between its frame
        // and the contained child stack) pushed the chip a thumbnail's
        // worth past the visible area, so the scrollbar engaged with a
        // tiny scroll range. CHIP_HEIGHT / 2 is empirically generous
        // across GUI scales without making the panel feel oversized at
        // larger match counts.
        int chipStack = bodyChips * CHIP_HEIGHT + Math.max(0, bodyChips - 1) * PANEL_GAP;
        int scrollerChromeBuffer = CHIP_HEIGHT / 2;
        return PANEL_PADDING * 2 + HEADER_HEIGHT + PANEL_GAP + chipStack + scrollerChromeBuffer;
    }

    /**
     * Returns null when neither a search query nor an active kit needs
     * surfacing. Otherwise builds a flex item for {@link LeftColumnBuilder}'s
     * column — content-fit height capped at {@link #MAX_CHIPS} rows
     * (the inner scroller handles longer lists). The panel doubles as
     * a "chest locator": under search it lists chests holding query
     * matches; with an active kit it lists chests holding kit-needed
     * items the player still has to fetch.
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
        int height = panelHeight(matches.size());

        UIElement overlay = panel(GLASS).layout(layout -> layout
                .widthPercent(100)
                .height(height)
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

        ScrollerView scroller = new ScrollerView();
        scroller.layout(layout -> layout.flex(1).widthPercent(100).gapAll(PANEL_GAP));
        scroller.scrollerStyle(style -> style.minScrollPixel(20f).maxScrollPixel(60f));
        for (Match match : matches) {
            scroller.addScrollViewChild(matchChip(match));
        }
        overlay.addChild(scroller);
        return overlay;
    }

    private List<Match> computeMatches() {
        boolean searchActive = !host.searchController.normalizedQuery().isBlank();
        java.util.Set<KitIdentity> kitNeeded = kitNeededIdentities();
        ArrayList<Match> out = new ArrayList<>();
        for (SlotWorkspaceViewModel.ChestChip chip : host.viewModel.chestChips()) {
            if (chip.contents().isEmpty()) {
                continue;
            }
            int matchCount = 0;
            int matchIdentities = 0;
            for (SlotWorkspaceViewModel.ChestContentSummary summary : chip.contents()) {
                boolean matches = (searchActive && host.searchController.matchesContentSummary(summary))
                        || matchesKitNeed(summary, kitNeeded);
                if (matches) {
                    matchCount += summary.count();
                    matchIdentities++;
                }
            }
            if (matchIdentities <= 0) {
                continue;
            }
            out.add(new Match(chip, matchCount, matchIdentities));
        }
        // Proximate chests first (closer = more actionable), then by match
        // count descending so the chest with the most hits sits on top.
        out.sort(Comparator
                .comparing((Match m) -> !m.chip.proximate())
                .thenComparingInt((Match m) -> -m.matchCount)
                .thenComparing(m -> m.chip.label(), String.CASE_INSENSITIVE_ORDER));
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
        int fill = (PANEL_ALT & 0x00FFFFFF) | 0xC0000000;
        UIElement chip = panel(fill).layout(layout -> layout
                .widthPercent(100)
                .height(CHIP_HEIGHT)
                .paddingHorizontal(6)
                .gapAll(4)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.ROW));

        Label labelEl = label(match.chip.label(), TEXT);
        labelEl.layout(layout -> layout.flex(1).height(CHIP_HEIGHT));
        labelEl.textStyle(style -> style
                .textColor(TEXT)
                .textShadow(false)
                .fontSize(8)
                .textAlignHorizontal(Horizontal.LEFT)
                .textAlignVertical(Vertical.CENTER));
        labelEl.setAllowHitTest(false);
        chip.addChild(labelEl);

        // Dimension hint for non-proximate chests so the player knows
        // where to walk. Proximate chests get a bullet instead — the
        // dimension is the player's current dimension by definition.
        String dim = shortDimension(match.chip.dimensionId());
        if (!match.chip.proximate() && !dim.isBlank()) {
            Label dimLabel = label(dim, MUTED);
            dimLabel.layout(layout -> layout.width(36).height(CHIP_HEIGHT));
            dimLabel.textStyle(style -> style
                    .textColor(MUTED)
                    .textShadow(false)
                    .fontSize(6)
                    .textAlignHorizontal(Horizontal.RIGHT)
                    .textAlignVertical(Vertical.CENTER));
            dimLabel.setAllowHitTest(false);
            chip.addChild(dimLabel);
        }

        Label count = label("×" + match.matchCount, ACCENT);
        count.layout(layout -> layout.width(32).height(CHIP_HEIGHT));
        count.textStyle(style -> style
                .textColor(ACCENT)
                .textShadow(false)
                .fontSize(7)
                .textAlignHorizontal(Horizontal.RIGHT)
                .textAlignVertical(Vertical.CENTER));
        count.setAllowHitTest(false);
        chip.addChild(count);
        installSearchChipHover(chip, match);
        return chip;
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
