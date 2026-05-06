package dev.imagio.slot.neoforge.screen.ldlib;

import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.*;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceUi.*;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceAtlasLayout;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.neoforge.screen.ldlib.util.Observable;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.FlexWrap;
import net.minecraft.client.gui.screens.Screen;

/**
 * Sectioned vertical scroll list that replaces the 2D pan/zoom atlas
 * surface. Each non-Triage island becomes a section block: a colored
 * header row followed by a horizontally-flowing flex grid of item
 * cards. Cards reuse {@link AtlasCardBuilder#atlasCardButton} for
 * single-LOD pixel rendering.
 *
 * <p>See {@code docs/plans/list-view.md} for the design.
 */
final class ListWallPanelBuilder {
    /**
     * Pinned card cell size in screen pixels. Single-LOD: every card on
     * every surface renders at this exact size. Pick once, design well.
     */
    static final int CARD_CELL_PX = 22;
    static final int CARD_GAP_PX = 2;
    /**
     * Hard column count for the section flow grid. Cards never wrap
     * past this many per row, so the wall stays a focused vertical
     * read instead of spreading across the full screen.
     */
    static final int CARDS_PER_ROW = 9;

    static final int SECTION_HEADER_HEIGHT_PX = 11;
    static final int SECTION_GAP_PX = 4;
    static final int SECTION_INNER_PAD_PX = 4;
    /**
     * Width (in screen px) the {@link ScrollerView}'s internal chrome
     * eats out of its content rect: 5 px for the vertical scroller bar
     * on the right of {@code verticalContainer}, plus 5 px on each
     * side from {@code viewPort.paddingAll(5)}, plus a couple of px of
     * safety margin so the rightmost card doesn't sit flush against
     * the scrollbar. Without this slack a 9-card row mathematically
     * fits but visually wraps to 8 because the section grid's
     * effective width is {@code WALL_CONTENT_WIDTH_PX - own padding -
     * 15} not {@code WALL_CONTENT_WIDTH_PX - own padding}.
     */
    private static final int SCROLLBAR_GUTTER_PX = 20;
    /**
     * Pixel width that fits exactly {@link #CARDS_PER_ROW} cards plus
     * inter-card gaps and the surrounding padding/scrollbar chrome.
     * Used as the scroller's fixed width in both standalone and sidebar
     * modes so the wall density stays identical across surfaces.
     */
    static final int WALL_CONTENT_WIDTH_PX =
            CARD_CELL_PX * CARDS_PER_ROW
                    + CARD_GAP_PX * (CARDS_PER_ROW - 1)
                    + SECTION_INNER_PAD_PX * 2
                    + SCROLLBAR_GUTTER_PX;

    private final SlotWorkspaceUiController host;

    ListWallPanelBuilder(SlotWorkspaceUiController host) {
        this.host = host;
    }

    UIElement body() {
        UIElement body = new UIElement().layout(layout -> layout
                .widthPercent(100)
                .flex(1)
                .gapAll(0)
                .flexDirection(FlexDirection.COLUMN));
        body.addChild(wallPanel());
        return body;
    }

    UIElement wallPanel() {
        if (host.wallPanelElement == null) {
            createPersistentWallPanel();
        }
        repopulateWallPanel();
        return host.wallPanelElement;
    }

    void createPersistentWallPanel() {
        // Wall panel is a flex-column container that owns the entire
        // workspace UI: action cluster + search + carried chip stack
        // at the top, leftColumn (TOC / chests / triage) beside the
        // wall scroller in a flex-row mid section, and status / kit
        // rack / belt as a footer stack. Same layout for both
        // standalone and sidebar surfaces — the only difference
        // between them is whether a vanilla container screen happens
        // to be hosting the workspace.
        UIElement panel = new UIElement().layout(layout -> layout
                .flex(1)
                .widthPercent(100)
                .paddingHorizontal(0)
                .gapAll(SECTION_GAP_PX)
                .flexDirection(FlexDirection.COLUMN));
        host.clearSelectionOnDirectClick(panel);

        ScrollerView scroller = new ScrollerView();
        scroller.layout(layout -> layout
                // Fixed-density wall: CARDS_PER_ROW (9) regardless of
                // how much horizontal space the host gives us.
                .width(WALL_CONTENT_WIDTH_PX)
                .heightPercent(100)
                .paddingAll(SECTION_INNER_PAD_PX)
                .gapAll(SECTION_GAP_PX)
                .flexDirection(FlexDirection.COLUMN));
        scroller.scrollerStyle(style -> style.minScrollPixel(20f).maxScrollPixel(60f));
        scroller.style(style -> style.backgroundTexture(rect(0xB810171D)).zIndex(0));

        host.wallScroller = scroller;
        host.carriedFreeSlotsChipElement = host.overlays.carriedFreeSlotsChip();
        host.topRightActionsElement = host.overlays.topRightActionsOverlay();
        host.wallPanelElement = panel;
    }

    void repopulateWallPanel() {
        for (Observable.Subscription sub : host.wallContentSubscriptions) {
            sub.unsubscribe();
        }
        host.wallContentSubscriptions.clear();
        UIElement panel = host.wallPanelElement;
        ScrollerView scroller = host.wallScroller;

        panel.clearAllChildren();
        scroller.clearAllScrollViewChildren();
        buildSections(scroller);

        // Top row layout. When the search modal isn't active, three
        // content-sized siblings — hint, carried-free chip, actions —
        // distributed via SPACE_BETWEEN with NO_WRAP. When the modal
        // IS active, replace the whole row with a single full-width
        // search bar so the buffer + status text never have to fight
        // the chip + actions for horizontal space (which used to
        // squeeze them into a 30px column where the status text
        // wrapped vertically per-character).
        if (host.searchController.modalActive()) {
            panel.addChild(host.overlays.searchModalRowOverlay());
        } else {
            UIElement topRow = new UIElement().layout(layout -> layout
                    .widthPercent(100)
                    .gapAll(SECTION_GAP_PX)
                    .alignItems(AlignItems.CENTER)
                    .justifyContent(dev.vfyjxf.taffy.style.AlignContent.SPACE_BETWEEN)
                    .wrap(dev.vfyjxf.taffy.style.FlexWrap.NO_WRAP)
                    .flexDirection(FlexDirection.ROW));
            topRow.addChild(host.overlays.searchHintOverlay());
            topRow.addChild(host.carriedFreeSlotsChipElement);
            topRow.addChild(host.topRightActionsElement);
            panel.addChild(topRow);
        }
        // Active-chest control strip — only when the host screen is a
        // chest screen. Shows above the recents strip so the chest
        // controls stay close to the action row, with recents (which is
        // navigation, not a per-host action) just below.
        UIElement activeChest = host.activeChestStrip.overlay();
        if (activeChest != null) {
            panel.addChild(activeChest);
        }
        // Recents strip stays pinned below the top row and outside the
        // wall scroller — "where did the thing I just grabbed end up?"
        // doesn't follow the scroll position. See
        // docs/plans/single-column-workspace.md Phase 3.
        UIElement recentsStrip = host.recentsStrip.overlay();
        if (recentsStrip != null) {
            panel.addChild(recentsStrip);
        }
        // Mid section: TOC sliver glued to the left edge of the wall
        // scroller (fixed CARDS_PER_ROW-derived width). The wall is the
        // primary surface; the sliver is a thin navigation strip and
        // adds only a few px to total width.
        UIElement midRow = new UIElement().layout(layout -> layout
                .widthPercent(100)
                .flex(1)
                .gapAll(SECTION_GAP_PX)
                .alignItems(AlignItems.STRETCH)
                .flexDirection(FlexDirection.ROW));
        UIElement tocSliver = host.tocPanel.overlay();
        if (tocSliver != null) {
            tocSliver.layout(layout -> layout
                    .width(TocPanelBuilder.SLIVER_WIDTH_PX)
                    .heightPercent(100));
            midRow.addChild(tocSliver);
        }
        midRow.addChild(scroller);
        panel.addChild(midRow);
        // Status row below the list — the only footer that lives
        // inside the centered content. Kit rack + belt are rendered as
        // root-level siblings of `content` (full-width, covering the
        // vanilla hotbar in sidebar mode); see
        // SlotWorkspaceUiController.rebuildNow.
        if (host.statusBarElement != null) {
            panel.addChild(host.statusBarElement);
        }
        // Popovers mount on the root-level popover slot so their full-
        // screen catcher actually covers the full screen (not just the
        // wall panel's bounding box). See SlotWorkspaceUiController.
        host.popoverSlot.clearAllChildren();
        UIElement contextMenu = host.menu.contextMenuOverlay();
        if (contextMenu != null) {
            host.popoverSlot.addChild(contextMenu);
        }
        UIElement editPopover = host.menu.islandEditPopover();
        if (editPopover != null) {
            host.popoverSlot.addChild(editPopover);
        }
        UIElement createPopover = host.menu.createIslandPopover();
        if (createPopover != null) {
            host.popoverSlot.addChild(createPopover);
        }
    }


    /**
     * Walk the view model's islands and emit one section block per
     * non-Triage island. Triage stays as the docked overlay panel; it is
     * never a wall section. See {@code MEMORY.md#project_triage_panel}.
     */
    void buildSections(ScrollerView scroller) {
        boolean filtering = !host.searchController.normalizedQuery().isBlank();
        boolean anyVisibleSection = false;
        for (SlotWorkspaceViewModel.AtlasIsland island : host.viewModel.islands()) {
            if (island.kind() == VisualAtlasIslandKind.TRIAGE) {
                continue;
            }
            UIElement section = sectionBlock(island, filtering);
            if (section == null) {
                continue;
            }
            scroller.addScrollViewChild(section);
            anyVisibleSection = true;
        }
        if (!anyVisibleSection && host.viewModel.atlasItems().isEmpty()) {
            Label empty = label("No main inventory stacks visible", MUTED);
            empty.layout(layout -> layout
                    .widthPercent(100)
                    .height(16));
            empty.setAllowHitTest(false);
            scroller.addScrollViewChild(empty);
        }
    }

    /**
     * Build a section: header row + flow grid of cards homed to the
     * island. When {@code filtering} is true, only matching cards
     * appear; when no card matches, the section collapses to a
     * one-line "(0 shown)" header rather than disappearing entirely
     * so the player sees what the filter excluded.
     */
    UIElement sectionBlock(SlotWorkspaceViewModel.AtlasIsland island, boolean filtering) {
        java.util.ArrayList<SlotWorkspaceViewModel.AtlasItem> visibleCards = new java.util.ArrayList<>();
        int totalCards = 0;
        for (SlotWorkspaceViewModel.AtlasItem item : host.viewModel.atlasItems()) {
            if (!island.islandId().equals(item.islandId())) {
                continue;
            }
            totalCards++;
            if (!filtering || host.searchController.matchesItem(item)) {
                visibleCards.add(item);
            }
        }

        // Empty sections stay in the wall as bare headers — they're
        // valid drop targets for re-homing carried items, and hiding
        // them strands the player without a way to send something to
        // a curated-but-empty section. The TOC's own zero-item filter
        // can hide them from the navigation strip; the wall keeps
        // them so drops have somewhere to land.

        UIElement section = new UIElement().layout(layout -> layout
                .widthPercent(100)
                .gapAll(2)
                .paddingAll(0)
                .flexDirection(FlexDirection.COLUMN));
        section.setId(island.islandId());

        UIElement header = sectionHeader(island, visibleCards.size(), totalCards, filtering);
        section.addChild(header);

        if (filtering && visibleCards.isEmpty()) {
            return section;
        }

        UIElement grid = new UIElement().layout(layout -> layout
                .widthPercent(100)
                .gapAll(CARD_GAP_PX)
                .paddingAll(0)
                .flexDirection(FlexDirection.ROW)
                .flexWrap(FlexWrap.WRAP)
                .alignItems(AlignItems.FLEX_START)
                .alignContent(AlignContent.FLEX_START));
        host.drag.installSectionDropTarget(grid, island);
        for (SlotWorkspaceViewModel.AtlasItem item : visibleCards) {
            Button card = host.atlasCard.atlasCardButton(item);
            grid.addChild(card);
        }
        section.addChild(grid);
        return section;
    }

    UIElement sectionHeader(
            SlotWorkspaceViewModel.AtlasIsland island,
            int visibleCount,
            int totalCount,
            boolean filtering
    ) {
        Button header = button("", true, island.color());
        header.layout(layout -> layout
                .widthPercent(100)
                .height(SECTION_HEADER_HEIGHT_PX)
                .paddingHorizontal(6)
                .gapAll(4)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.ROW));
        header.style(style -> style.zIndex(3));
        header.noText();

        Label title = label(island.label(), TEXT);
        title.layout(layout -> layout.flex(1).heightPercent(100));
        title.textStyle(style -> style
                .textColor(TEXT)
                .textShadow(true)
                .fontSize(8)
                .textAlignHorizontal(Horizontal.LEFT)
                .textAlignVertical(Vertical.CENTER));
        title.setAllowHitTest(false);
        header.addChild(title);

        // Right-edge count label. When the player has any of this section's
        // items carried, the label tints ACCENT and prefixes the carried
        // count: "5/12●" reads "5 of 12 carried." Otherwise just "12" in
        // MUTED. Single label avoids the double-text overlap that fell out
        // of stacking two right-aligned labels at the same anchor.
        boolean hasCarried = island.carriedCount() > 0;
        String countText;
        if (filtering && visibleCount != totalCount) {
            countText = visibleCount + " / " + totalCount;
        } else if (hasCarried) {
            countText = island.carriedCount() + "/" + totalCount + "●";
        } else {
            countText = String.valueOf(totalCount);
        }
        int countColor = hasCarried ? ACCENT : MUTED;
        Label count = label(countText, countColor);
        count.layout(layout -> layout.heightPercent(100));
        count.textStyle(style -> style
                .textColor(countColor)
                .textShadow(false)
                .fontSize(7)
                .textAlignHorizontal(Horizontal.RIGHT)
                .textAlignVertical(Vertical.CENTER));
        count.setAllowHitTest(false);
        header.addChild(count);

        // Right-click → island edit popover. Left-click on header with a
        // selected atlas item assigns the item's home to this island
        // (matches the old island title bar behaviour).
        if (island.kind() == VisualAtlasIslandKind.PLAYER) {
            header.addEventListener(UIEvents.MOUSE_DOWN, event -> {
                if (event.button == 1) {
                    event.stopPropagation();
                    host.menu.beginIslandEdit(island, event.x, event.y);
                    return;
                }
                if (event.button == 0 && Screen.hasShiftDown()) {
                    event.stopPropagation();
                    return;
                }
            }, true);
            header.setOnClick(event -> {
                if (event.button != 0) {
                    return;
                }
                event.stopPropagation();
                if (host.selectedAtlasItem() == null) {
                    host.localStatus.set("select a triage or homed item first");
                    return;
                }
                host.rpc.sendAssignHome(island.islandId());
            });
        }
        if (island.kind() == VisualAtlasIslandKind.PLAYER) {
            host.drag.installSectionHeaderDragSource(header, island);
        }
        host.drag.installSectionHeaderDropTarget(header, island);
        return header;
    }
}
