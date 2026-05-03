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
     * Pixel width that fits exactly {@link #CARDS_PER_ROW} cards plus
     * inter-card gaps and the surrounding padding/scrollbar chrome.
     * Used as the scroller's fixed width so the wall stays narrow.
     */
    static final int WALL_CONTENT_WIDTH_PX =
            CARD_CELL_PX * CARDS_PER_ROW
                    + CARD_GAP_PX * (CARDS_PER_ROW - 1)
                    + SECTION_INNER_PAD_PX * 2
                    + 8;
    /**
     * Top padding on the wall panel to clear the floating action cluster
     * (Deposit / Gather / undo / vanilla) at top:10 height:16. Inset is
     * applied to in-flow children only; the action cluster is abs-
     * positioned and references the panel's outer bounds.
     */
    static final int WALL_TOP_PAD_PX = 32;
    /**
     * Bottom padding to clear the docked belt (height {@link
     * WorkspaceTheme#BELT_HEIGHT}, bottom:4).
     */
    static final int WALL_BOTTOM_PAD_PX = WorkspaceTheme.BELT_HEIGHT + 8;

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
        // The wall panel is a flex-row: a fixed-width placeholder
        // reserves space on the left for the docked left column (which
        // floats over it as an absolutely-positioned overlay because it
        // has its own dynamic top/bottom anchors against the search input
        // and belt/kit chrome), and the scroller flex(1)s into the rest.
        // This way Taffy computes the scroller's width — no hardcoded
        // left inset on the scroller itself.
        // No panel fill — the wall panel itself is just a flex container.
        // The scroller renders its own translucent backdrop over the
        // card grid; everywhere else is transparent so the vanilla
        // backdrop shows through.
        UIElement panel = new UIElement().layout(layout -> layout
                .flex(1)
                .widthPercent(100)
                .paddingTop(WALL_TOP_PAD_PX)
                .paddingBottom(WALL_BOTTOM_PAD_PX)
                .paddingHorizontal(0)
                .flexDirection(FlexDirection.ROW));
        host.clearSelectionOnDirectClick(panel);

        UIElement leftReservation = new UIElement().layout(layout -> layout
                .width(LeftColumnBuilder.RESERVED_WIDTH)
                .heightPercent(100));
        leftReservation.setAllowHitTest(false);

        ScrollerView scroller = new ScrollerView();
        scroller.layout(layout -> layout
                .width(WALL_CONTENT_WIDTH_PX)
                .heightPercent(100)
                .paddingAll(SECTION_INNER_PAD_PX)
                .gapAll(SECTION_GAP_PX)
                .flexDirection(FlexDirection.COLUMN));
        scroller.scrollerStyle(style -> style.minScrollPixel(20f).maxScrollPixel(60f));
        scroller.style(style -> style.backgroundTexture(rect(0xB810171D)).zIndex(0));

        host.wallScroller = scroller;
        host.wallLeftReservation = leftReservation;
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

        // In-flow children first so flex-row sizes the scroller from
        // the remaining width. The leftReservation reserves space for
        // the docked left column overlay; the scroller flexes into
        // the rest.
        panel.addChild(host.wallLeftReservation);
        panel.addChild(scroller);

        UIElement leftColumn = host.leftColumn.overlay();
        if (leftColumn != null) {
            panel.addChild(leftColumn);
        }
        panel.addChild(host.belt.overlay());
        panel.addChild(host.carriedFreeSlotsChipElement);
        panel.addChild(host.topRightActionsElement);
        if (host.searchController.modalActive()) {
            panel.addChild(host.overlays.searchChipOverlay());
        } else {
            panel.addChild(host.overlays.searchHintOverlay());
        }
        if (host.kitRackOpen) {
            panel.addChild(host.kit.kitRackOverlay());
        }
        UIElement contextMenu = host.menu.contextMenuOverlay();
        if (contextMenu != null) {
            panel.addChild(contextMenu);
        }
        UIElement editPopover = host.menu.islandEditPopover();
        if (editPopover != null) {
            panel.addChild(editPopover);
        }
        UIElement createPopover = host.menu.createIslandPopover();
        if (createPopover != null) {
            panel.addChild(createPopover);
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

        String countText = filtering && visibleCount != totalCount
                ? visibleCount + " / " + totalCount
                : String.valueOf(totalCount);
        Label count = label(countText, MUTED);
        count.layout(layout -> layout.heightPercent(100));
        count.textStyle(style -> style
                .textColor(MUTED)
                .textShadow(false)
                .fontSize(7)
                .textAlignHorizontal(Horizontal.RIGHT)
                .textAlignVertical(Vertical.CENTER));
        count.setAllowHitTest(false);
        header.addChild(count);

        if (island.carriedCount() > 0) {
            Label carried = label(island.carriedCount() + "●", ACCENT);
            carried.layout(layout -> layout.heightPercent(100));
            carried.textStyle(style -> style
                    .textColor(ACCENT)
                    .textShadow(false)
                    .fontSize(7)
                    .textAlignHorizontal(Horizontal.RIGHT)
                    .textAlignVertical(Vertical.CENTER));
            carried.setAllowHitTest(false);
            header.addChild(carried);
        }

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
        host.drag.installSectionHeaderDropTarget(header, island);
        return header;
    }
}
