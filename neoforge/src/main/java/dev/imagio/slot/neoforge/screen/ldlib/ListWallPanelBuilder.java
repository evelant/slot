package dev.imagio.slot.neoforge.screen.ldlib;

import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.*;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceUi.*;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceAtlasLayout;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.neoforge.config.SlotClientConfig;
import dev.imagio.slot.neoforge.screen.ldlib.util.Observable;
import dev.imagio.slot.ui.action.WorkspaceActionId;
import dev.imagio.slot.ui.spi.SlotUiElement;
import dev.imagio.slot.ui.workspace.CraftRunUiBuilder;
import dev.imagio.slot.ui.workspace.WallCardUiBuilder;
import dev.imagio.slot.ui.workspace.WallSectionHeaderUiBuilder;
import dev.imagio.slot.ui.workspace.WallSectionUiBuilder;
import dev.imagio.slot.ui.workspace.WallSectionVisibility;
import dev.imagio.slot.ui.workspace.WorkspaceTaskPanelUiBuilder;
import dev.imagio.slot.ui.workspace.WorkflowTabsUiBuilder;
import dev.imagio.slot.ui.workspace.WorkspaceUiAttachments;
import dev.imagio.slot.ui.workspace.WorkspaceUiSessionMemory;
import dev.imagio.slot.workflow.domain.CraftRunRecipeEntry;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import dev.imagio.slot.neoforge.network.SlotCraftRunRecipePayload;
import net.neoforged.neoforge.network.PacketDistributor;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;

import java.util.ArrayList;
import java.util.List;

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
    static final int CARD_CELL_PX = WallCardUiBuilder.CARD_CELL_PX;
    static final int CARD_GAP_PX = WallSectionUiBuilder.CARD_GAP_PX;
    /**
     * Hard column count for the section flow grid. Cards never wrap
     * past this many per row, so the wall stays a focused vertical
     * read instead of spreading across the full screen.
     */
    static final int CARDS_PER_ROW = 9;

    static final int SECTION_HEADER_HEIGHT_PX = WallSectionHeaderUiBuilder.HEADER_HEIGHT_PX;
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
    private final WallSectionHeaderUiBuilder sectionHeaderBuilder;
    private final WallSectionUiBuilder sectionBuilder;
    private final LdlibSlotUiRenderer sectionRenderer;
    private final LdlibSlotUiRenderer tabsRenderer;

    ListWallPanelBuilder(SlotWorkspaceUiController host) {
        this.host = host;
        this.sectionHeaderBuilder = new WallSectionHeaderUiBuilder(new WallSectionHeaderContext());
        this.sectionBuilder = new WallSectionUiBuilder(sectionHeaderBuilder);
        this.sectionRenderer = new LdlibSlotUiRenderer(this::installSectionInteractions);
        this.tabsRenderer = new LdlibSlotUiRenderer((model, element) -> { });
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

    UIElement taskPanel() {
        List<SlotUiElement> suggestionRows = contextualSuggestionRows();
        List<SlotUiElement> craftRows = new CraftRunUiBuilder(new CraftRunContext()).panelRows(craftRunItems());
        List<SlotUiElement> rows = taskPanelRows(suggestionRows, craftRows);
        if (rows.isEmpty()) {
            return null;
        }
        UIElement panel = panel(PANEL).layout(layout -> layout
                .positionType(dev.vfyjxf.taffy.style.TaffyPosition.ABSOLUTE)
                .right(SlotClientConfig.CLIENT.taskPanelRightMargin.get())
                .top(SlotClientConfig.CLIENT.taskPanelTopMargin.get())
                .bottom(SlotClientConfig.CLIENT.taskPanelBottomMargin.get())
                .width(WorkspaceTaskPanelUiBuilder.PANEL_WIDTH_PX)
                .paddingAll(5)
                .gapAll(4)
                .flexDirection(FlexDirection.COLUMN));
        panel.style(style -> style.zIndex(22));
        panel.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());
        panel.addChild(label(WorkspaceTaskPanelUiBuilder.title(!suggestionRows.isEmpty(), !craftRows.isEmpty()), ACCENT)
                .layout(layout -> layout
                        .widthPercent(100)
                        .height(12)));

        ScrollerView scroller = new ScrollerView();
        scroller.layout(layout -> layout
                .widthPercent(100)
                .flex(1)
                .paddingAll(3)
                .gapAll(SECTION_GAP_PX)
                .flexDirection(FlexDirection.COLUMN));
        scroller.scrollerStyle(style -> style.minScrollPixel(20f).maxScrollPixel(50f));
        scroller.style(style -> style.backgroundTexture(rect(0xA810171D)).zIndex(0));
        for (SlotUiElement row : rows) {
            scroller.addScrollViewChild(sectionRenderer.render(row));
        }
        panel.addChild(scroller);
        return panel;
    }

    boolean taskPanelVisible() {
        return contextualSuggestionRowsVisible() || craftRunRowsVisible();
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
        // at the top, TOC sliver beside the wall scroller in a
        // flex-row mid section, and status / kit
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
        scroller.verticalScroller.registerValueListener(value ->
                host.rememberWallScroll(value == null ? 0f : value));

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
        float rememberedScroll = WorkspaceUiSessionMemory.wallScroll(host.surfaceMemoryKey());
        scroller.verticalScroller.setValue(rememberedScroll);
        host.requestWallScrollRestore(rememberedScroll);

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
        int workflowTabsHeight = WorkflowTabsUiBuilder.height(host.viewModel);
        UIElement workflowRow = new UIElement().layout(layout -> layout
                .widthPercent(100)
                .height(Math.max(workflowTabsHeight, BELT_SLOT_SIZE))
                .gapAll(SECTION_GAP_PX)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.ROW));
        workflowRow.addChild(tabsRenderer.render(new WorkflowTabsUiBuilder(new WorkflowTabsContext()).tabs(host.viewModel))
                .layout(layout -> layout.flex(1).height(workflowTabsHeight)));
        workflowRow.addChild(host.kit.kitCluster());
        panel.addChild(workflowRow);
        // Active-chest control strip — only when the host screen is a
        // chest screen. Recents now floats at root level because it is
        // navigation, not a per-host wall action.
        UIElement activeChest = host.activeChestStrip.overlay();
        if (activeChest != null) {
            panel.addChild(activeChest);
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
        if (host.kitRackOpen) {
            midRow.addChild(host.kit.kitRackOverlay());
        }
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
        UIElement helpPopover = host.overlays.helpPopover();
        if (helpPopover != null) {
            host.popoverSlot.addChild(helpPopover);
        }
    }


    /**
     * Walk the view model's islands and emit one section block per
     * non-Triage island. Triage is only a legacy routing sentinel now,
     * not a visible wall section.
     */
    void buildSections(ScrollerView scroller) {
        boolean filtering = !host.searchController.normalizedQuery().isBlank();
        boolean anyVisibleSection = false;
        for (SlotWorkspaceViewModel.AtlasIsland island : host.currentIslands()) {
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
        if (!anyVisibleSection && host.currentAtlasItems().isEmpty()) {
            Label empty = label("No main inventory stacks visible", MUTED);
            empty.layout(layout -> layout
                    .widthPercent(100)
                    .height(16));
            empty.setAllowHitTest(false);
            scroller.addScrollViewChild(empty);
        }
    }

    private List<SlotWorkspaceViewModel.AtlasItem> craftRunItems() {
        ArrayList<SlotWorkspaceViewModel.AtlasItem> items = new ArrayList<>();
        items.addAll(host.viewModel.atlasItems());
        items.addAll(host.viewModel.triageItems());
        return items.isEmpty() ? List.of() : List.copyOf(items);
    }

    private List<SlotUiElement> contextualSuggestionRows() {
        if (host.recipeSidebarActive()) {
            return List.of();
        }
        boolean filtering = !host.searchController.normalizedQuery().isBlank();
        ArrayList<SlotUiElement> rows = new ArrayList<>();
        for (SlotWorkspaceViewModel.ContextualSuggestionLane lane : host.viewModel.contextualSuggestionLanes()) {
            if (hideFetchLaneForCraftRun(lane)) {
                continue;
            }
            SlotWorkspaceViewModel.ContextualSuggestionLane visibleLane = visibleSuggestionLane(lane, filtering);
            if (WallSectionUiBuilder.shouldRenderSuggestionLane(visibleLane)) {
                rows.add(sectionBuilder.suggestionLane(visibleLane));
            }
        }
        return rows.isEmpty() ? List.of() : List.copyOf(rows);
    }

    private boolean contextualSuggestionRowsVisible() {
        if (host.recipeSidebarActive()) {
            return false;
        }
        boolean filtering = !host.searchController.normalizedQuery().isBlank();
        for (SlotWorkspaceViewModel.ContextualSuggestionLane lane : host.viewModel.contextualSuggestionLanes()) {
            if (hideFetchLaneForCraftRun(lane)) {
                continue;
            }
            if (WallSectionUiBuilder.shouldRenderSuggestionLane(visibleSuggestionLane(lane, filtering))) {
                return true;
            }
        }
        return false;
    }

    private boolean craftRunRowsVisible() {
        return (host.viewModel != null
                && host.viewModel.craftRun() != null
                && host.viewModel.craftRun().active())
                || !host.craftRunRecipeCaptures().isEmpty();
    }

    private static List<SlotUiElement> taskPanelRows(
            List<SlotUiElement> suggestionRows,
            List<SlotUiElement> craftRows
    ) {
        ArrayList<SlotUiElement> rows = new ArrayList<>();
        rows.addAll(suggestionRows == null ? List.of() : suggestionRows);
        rows.addAll(craftRows == null ? List.of() : craftRows);
        return rows.isEmpty() ? List.of() : List.copyOf(rows);
    }

    private boolean hideFetchLaneForCraftRun(SlotWorkspaceViewModel.ContextualSuggestionLane lane) {
        return lane != null
                && lane.fetch()
                && host.viewModel != null
                && host.viewModel.craftRun() != null
                && host.viewModel.craftRun().active();
    }

    private SlotWorkspaceViewModel.ContextualSuggestionLane visibleSuggestionLane(
            SlotWorkspaceViewModel.ContextualSuggestionLane lane,
            boolean filtering
    ) {
        if (lane == null) {
            return new SlotWorkspaceViewModel.ContextualSuggestionLane("", "", List.of());
        }
        if (!filtering) {
            return lane;
        }
        if (lane.items().isEmpty()) {
            return new SlotWorkspaceViewModel.ContextualSuggestionLane(
                    lane.id(), lane.label(), List.of(), lane.placeholderText(), lane.debugInfo());
        }
        ArrayList<SlotWorkspaceViewModel.AtlasItem> visible = new ArrayList<>();
        for (SlotWorkspaceViewModel.AtlasItem item : lane.items()) {
            if (host.searchController.matchesItem(item)) {
                visible.add(item);
            }
        }
        return new SlotWorkspaceViewModel.ContextualSuggestionLane(
                lane.id(), lane.label(), visible, lane.placeholderText(), lane.debugInfo());
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
        for (SlotWorkspaceViewModel.AtlasItem item : host.currentAtlasItems()) {
            if (!island.islandId().equals(item.islandId())) {
                continue;
            }
            totalCards++;
            if (!filtering || host.searchController.matchesItem(item)) {
                visibleCards.add(item);
            }
        }

        if (!shouldShowSection(island, visibleCards, filtering)) {
            return null;
        }

        return sectionRenderer.render(sectionBuilder.section(
                island,
                visibleCards,
                totalCards,
                filtering,
                host.storageGhostRevealMode,
                host.storageGhostSectionExpanded(island.islandId()),
                false,
                !host.activeWorkflowTab()));
    }

    private boolean shouldShowSection(
            SlotWorkspaceViewModel.AtlasIsland island,
            List<SlotWorkspaceViewModel.AtlasItem> visibleCards,
            boolean filtering
    ) {
        return WallSectionVisibility.classify(
                visibleCards,
                filtering,
                host.storageGhostSectionExpanded(island.islandId()),
                host.storageGhostRevealMode,
                false,
                !host.activeWorkflowTab()).hasVisibleContent();
    }

    private void installSectionInteractions(SlotUiElement model, UIElement element) {
        if (model.hasAttachment(WorkspaceUiAttachments.WALL_SUGGESTION_GRID)) {
            SlotWorkspaceViewModel.ContextualSuggestionLane lane = model.attachment(
                    WorkspaceUiAttachments.CONTEXTUAL_SUGGESTION_LANE,
                    SlotWorkspaceViewModel.ContextualSuggestionLane.class);
            boolean forceWayfinding = lane != null && lane.forceWayfindingStrip();
            List<?> cards = model.attachment(WorkspaceUiAttachments.ATLAS_ITEMS, List.class);
            if (cards != null) {
                for (Object cardObject : cards) {
                    if (cardObject instanceof SlotWorkspaceViewModel.AtlasItem item) {
                        Button card = host.atlasCard.atlasCardButton(item, forceWayfinding, lane);
                        element.addChild(card);
                    }
                }
            }
            return;
        }
        if (model.hasAttachment(WorkspaceUiAttachments.WALL_CRAFT_RUN_GRID)) {
            List<?> cards = model.attachment(WorkspaceUiAttachments.ATLAS_ITEMS, List.class);
            if (cards != null) {
                for (Object cardObject : cards) {
                    if (cardObject instanceof SlotWorkspaceViewModel.AtlasItem item) {
                        Button card = host.atlasCard.atlasCardButton(item);
                        element.addChild(card);
                    }
                }
            }
            return;
        }
        if (model.hasAttachment(WorkspaceUiAttachments.WALL_SECTION_GRID)) {
            SlotWorkspaceViewModel.AtlasIsland island = model.attachment(
                    WorkspaceUiAttachments.ATLAS_ISLAND,
                    SlotWorkspaceViewModel.AtlasIsland.class
            );
            if (island == null) {
                return;
            }
            host.drag.installSectionDropTarget(element, island);
            List<?> cards = model.attachment(WorkspaceUiAttachments.ATLAS_ITEMS, List.class);
            if (cards != null) {
                for (Object cardObject : cards) {
                    if (cardObject instanceof SlotWorkspaceViewModel.AtlasItem item) {
                        Button card = host.atlasCard.atlasCardButton(item);
                        element.addChild(card);
                    }
                }
            }
            return;
        }
        if (!model.hasAttachment(WorkspaceUiAttachments.WALL_SECTION_HEADER)) {
            return;
        }
        SlotWorkspaceViewModel.AtlasIsland island = model.attachment(
                WorkspaceUiAttachments.ATLAS_ISLAND,
                SlotWorkspaceViewModel.AtlasIsland.class
        );
        if (island == null || !(element instanceof Button header)) {
            return;
        }
        if (island.kind() == VisualAtlasIslandKind.PLAYER) {
            host.drag.installSectionHeaderDragSource(header, island);
        }
        host.drag.installSectionHeaderDropTarget(header, island);
    }

    private final class WallSectionHeaderContext implements WallSectionHeaderUiBuilder.Context {
        @Override
        public void beginIslandEdit(
                SlotWorkspaceViewModel.AtlasIsland island,
                float screenX,
                float screenY
        ) {
            host.menu.beginIslandEdit(island, screenX, screenY);
        }

        @Override
        public void toggleNearbySection(SlotWorkspaceViewModel.AtlasIsland island) {
            if (island == null) {
                return;
            }
            if (WorkspaceCursorState.isCarrying()) {
                return;
            }
            host.toggleStorageGhostSection(island.islandId());
        }
    }

    private final class WorkflowTabsContext implements WorkflowTabsUiBuilder.Context {
        @Override
        public void selectAll() {
            host.selectAllWorkflowTab();
        }

        @Override
        public void selectTab(String kitId) {
            host.selectWorkflowTab(kitId);
        }

        @Override
        public void createTab() {
            host.rpc.sendCreateWorkflowTab();
        }

        @Override
        public void openTabMenu(String kitId, float screenX, float screenY) {
            host.menu.openContextMenuForKit(kitId, screenX, screenY);
        }
    }

    private final class CraftRunContext implements CraftRunUiBuilder.Context {
        @Override
        public dev.imagio.slot.workflow.domain.CraftRunState craftRun() {
            return host.viewModel.craftRun();
        }

        @Override
        public List<dev.imagio.slot.workflow.domain.CraftRunRecipeCapture> visibleRecipes() {
            return host.craftRunRecipeCaptures();
        }

        @Override
        public void addVisibleRecipe(dev.imagio.slot.workflow.domain.CraftRunRecipeCapture capture) {
            PacketDistributor.sendToServer(SlotCraftRunRecipePayload.add(capture));
            host.localStatus.set("added recipe");
            host.rebuild();
        }

        @Override
        public void stageEntry(String entryId) {
            host.rpc.send(WorkspaceActionId.CRAFT_RUN_STAGE_ENTRY, entryId);
            host.localStatus.set("staging craft ingredients");
        }

        @Override
        public void openRecipe(CraftRunRecipeEntry entry) {
            host.openRecipe(entry == null ? null : entry.outputIdentity());
        }

        @Override
        public void adjustEntry(String entryId, int delta) {
            host.rpc.send(WorkspaceActionId.CRAFT_RUN_ADJUST_ENTRY, entryId, delta);
        }

        @Override
        public void removeEntry(String entryId) {
            host.rpc.send(WorkspaceActionId.CRAFT_RUN_REMOVE_ENTRY, entryId);
        }
    }
}
