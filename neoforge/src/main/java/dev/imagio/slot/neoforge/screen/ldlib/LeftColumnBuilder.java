package dev.imagio.slot.neoforge.screen.ldlib;

import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.*;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.TaffyPosition;

/**
 * One absolutely-positioned flex column that owns the entire left edge:
 * search-results panel, chest chip panel, loot panel, Triage panel —
 * each appears in flow order, separated by a small gap.
 *
 * <p>Replaces the prior arrangement where each panel was its own
 * absolute-positioned sibling and had to query the others'
 * {@code reservedHeight()} to compute its own top — a manual layout
 * engine that fought flexbox. With this container the children just
 * declare their own size policy (content-fit for chip / search,
 * {@code flex(1)} with a min-height for loot / Triage) and the column
 * tiles them automatically. Adding or removing a panel becomes a
 * one-line change instead of a chain edit across four files.
 *
 * <p>Container top shifts down to {@code 78} when the search modal is
 * active (so the search input at y=36..78 isn't covered); otherwise
 * docks at {@code 36}. Bottom anchors above the belt + (when open) the
 * kit rack so the column never overlaps the chrome below.
 */
final class LeftColumnBuilder {
    static final int LEFT = 8;
    static final int WIDTH = TRIAGE_PANEL_WIDTH;
    static final int GAP = 6;
    /**
     * Total horizontal space the docked left column reserves on the wall
     * (its left margin + width + the gap before the wall scroller picks
     * up). Owned here so the wall layout reads it from a single source
     * instead of recomputing the same sum.
     */
    static final int RESERVED_WIDTH = LEFT + WIDTH + GAP;

    private final SlotWorkspaceUiController host;

    LeftColumnBuilder(SlotWorkspaceUiController host) {
        this.host = host;
    }

    UIElement overlay() {
        int top = host.searchController.modalActive() ? 78 : 36;
        int baseBottom = BELT_HEIGHT + 12;
        int bottom = host.kitRackOpen ? baseBottom + host.kit.kitRackHeight() + 4 : baseBottom;

        UIElement toc = host.tocPanel.overlay();
        UIElement searchResults = host.searchResultsPanel.overlay();
        UIElement chips = host.storagePanel.overlay();
        UIElement loot = host.lootChestPanel.overlay();
        UIElement triage = host.triagePanel.overlay();

        if (toc == null && searchResults == null && chips == null && loot == null && triage == null) {
            return null;
        }

        UIElement column = new UIElement().layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(LEFT)
                .top(top)
                .bottom(bottom)
                .width(WIDTH)
                .gapAll(GAP)
                .flexDirection(FlexDirection.COLUMN));
        column.style(style -> style.zIndex(7));
        // Same MOUSE_DOWN guard each panel installed individually — keep
        // clicks inside the column from punching through to the atlas.
        column.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());

        if (toc != null) {
            column.addChild(toc);
        }
        if (searchResults != null) {
            column.addChild(searchResults);
        }
        if (chips != null) {
            column.addChild(chips);
        }
        if (loot != null) {
            column.addChild(loot);
        }
        if (triage != null) {
            column.addChild(triage);
        }
        return column;
    }
}
