package dev.imagio.slot.neoforge.screen.ldlib;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import dev.vfyjxf.taffy.style.FlexDirection;

/**
 * In-flow flex column that owns the left side of the workspace. The
 * chest-locator (search results) is pinned at the top of the column so it
 * stays visible while the player scans; everything else — TOC, nearby
 * chests, loot, Triage — sits inside a single {@link ScrollerView} below
 * it. Each constituent panel renders content-fit (no inner scrollers) and
 * the outer scroller handles overflow.
 *
 * <p>Sized via {@code maxWidth} on the call site
 * ({@link ListWallPanelBuilder#repopulateWallPanel}) so the wall stays
 * the primary horizontal claim.
 */
final class LeftColumnBuilder {
    static final int GAP = 6;

    private final SlotWorkspaceUiController host;

    LeftColumnBuilder(SlotWorkspaceUiController host) {
        this.host = host;
    }

    UIElement overlay() {
        UIElement searchResults = host.searchResultsPanel.overlay();
        UIElement toc = host.tocPanel.overlay();
        UIElement chips = host.storagePanel.overlay();
        UIElement loot = host.lootChestPanel.overlay();
        UIElement triage = host.triagePanel.overlay();

        if (searchResults == null && toc == null && chips == null && loot == null && triage == null) {
            return null;
        }

        UIElement column = new UIElement().layout(layout -> layout
                .widthPercent(100)
                .heightPercent(100)
                .gapAll(GAP)
                .flexDirection(FlexDirection.COLUMN));
        column.style(style -> style.zIndex(7));
        // Swallow MOUSE_DOWN so clicks inside the column don't punch
        // through to the wall (which would clear selection / cancel
        // the cursor).
        column.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());

        // Chest locator stays pinned outside the scroller — when active
        // it's the most relevant info on the column and we don't want it
        // scrolling away.
        if (searchResults != null) {
            column.addChild(searchResults);
        }

        boolean anyScrollable = toc != null || chips != null || loot != null || triage != null;
        if (anyScrollable) {
            ScrollerView scroller = new ScrollerView();
            scroller.layout(layout -> layout
                    .flex(1)
                    .widthPercent(100)
                    .gapAll(GAP)
                    .flexDirection(FlexDirection.COLUMN));
            scroller.scrollerStyle(style -> style.minScrollPixel(20f).maxScrollPixel(60f));
            if (toc != null) {
                scroller.addScrollViewChild(toc);
            }
            if (chips != null) {
                scroller.addScrollViewChild(chips);
            }
            if (loot != null) {
                scroller.addScrollViewChild(loot);
            }
            if (triage != null) {
                scroller.addScrollViewChild(triage);
            }
            column.addChild(scroller);
        }
        return column;
    }
}
