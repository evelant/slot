package dev.imagio.slot.neoforge.screen.ldlib;

import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.*;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceUi.*;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;

/**
 * Sectioned wall table-of-contents. Lists every non-Triage island in
 * display order with a colored swatch, label, and item count. Clicking
 * a row scrolls the wall to that section. Drag-to-reorder is wired
 * through the existing
 * {@link SlotWorkspaceUiController#sendReorderSection(String, int)} hook
 * (TODO: server-side reorder RPC; today drag is a no-op).
 *
 * <p>Status dots (off-screen search match, off-screen kit-needed) are
 * a follow-up — the visual surface and TICK-driven check skeleton are
 * here but the off-screen detection is best-effort. Lights up when a
 * matching card lives in this section AND the section's center sits
 * outside the wall's viewport.
 */
final class TocPanelBuilder {
    private static final int ROW_HEIGHT = 9;
    private static final int SWATCH_WIDTH = 3;
    private static final int PANEL_PADDING = 3;
    private static final int PANEL_GAP = 1;
    private static final float ROW_FONT_PX = 6f;
    /**
     * Visible row count cap before the inner scroller engages. Keeps
     * the TOC from monopolising the left column when a base has many
     * sections; other panels (search results / nearby chests / loot /
     * triage) get to share the column below.
     */
    private static final int MAX_VISIBLE_ROWS = 12;

    private final SlotWorkspaceUiController host;

    TocPanelBuilder(SlotWorkspaceUiController host) {
        this.host = host;
    }

    UIElement overlay() {
        // Per-island counts of currently-surfaced atlas items
        // (carried + proximate-chest ghosts + kit-needed ghosts +
        // search-time elsewhere ghosts; desired-count > 0 identities
        // that aren't surfaced via any of those routes don't show up
        // here, which matches the player's view of "what's actually
        // visible for this section").
        java.util.Map<String, Integer> counts = new java.util.HashMap<>();
        for (SlotWorkspaceViewModel.AtlasItem item : host.viewModel.atlasItems()) {
            counts.merge(item.islandId(), 1, Integer::sum);
        }
        java.util.List<SlotWorkspaceViewModel.AtlasIsland> entries = new java.util.ArrayList<>();
        for (SlotWorkspaceViewModel.AtlasIsland island : host.viewModel.islands()) {
            if (island.kind() == VisualAtlasIslandKind.TRIAGE) {
                continue;
            }
            if (counts.getOrDefault(island.islandId(), 0) <= 0) {
                continue;
            }
            entries.add(island);
        }
        if (entries.isEmpty()) {
            return null;
        }
        int visibleRows = Math.min(entries.size(), MAX_VISIBLE_ROWS);
        int scrollerHeight = visibleRows * ROW_HEIGHT
                + Math.max(0, visibleRows - 1) * PANEL_GAP
                + (entries.size() > MAX_VISIBLE_ROWS ? ROW_HEIGHT / 2 : 0);
        int panelHeight = PANEL_PADDING * 2 + scrollerHeight;
        UIElement panel = panel(GLASS).layout(layout -> layout
                .widthPercent(100)
                .height(panelHeight)
                .paddingAll(PANEL_PADDING)
                .flexDirection(FlexDirection.COLUMN));
        panel.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());
        panel.style(style -> style.zIndex(7));

        ScrollerView scroller = new ScrollerView();
        scroller.layout(layout -> layout
                .widthPercent(100)
                .heightPercent(100)
                .gapAll(PANEL_GAP)
                .flexDirection(FlexDirection.COLUMN));
        scroller.scrollerStyle(style -> style.minScrollPixel(12f).maxScrollPixel(40f));
        for (SlotWorkspaceViewModel.AtlasIsland island : entries) {
            scroller.addScrollViewChild(row(island));
        }
        panel.addChild(scroller);
        return panel;
    }

    private UIElement row(SlotWorkspaceViewModel.AtlasIsland island) {
        Button row = button("", true, ROW_DIM);
        row.layout(layout -> layout
                .widthPercent(100)
                .height(ROW_HEIGHT)
                .paddingAll(0)
                .paddingHorizontal(2)
                .gapAll(3)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.ROW));
        row.style(style -> style.zIndex(8));
        row.noText();
        row.setOnClick(event -> {
            if (event.button != 0) {
                return;
            }
            event.stopPropagation();
            scrollWallToSection(island.islandId());
        });

        UIElement swatch = panel(island.color()).layout(layout -> layout
                .width(SWATCH_WIDTH)
                .heightPercent(100));
        swatch.setAllowHitTest(false);
        row.addChild(swatch);

        Label name = label(WorkspaceUi.shorten(island.label(), 14), TEXT);
        name.layout(layout -> layout.flex(1).heightPercent(100));
        name.textStyle(style -> style
                .textColor(TEXT)
                .textShadow(false)
                .fontSize(ROW_FONT_PX)
                .textAlignHorizontal(Horizontal.LEFT)
                .textAlignVertical(Vertical.CENTER));
        name.setAllowHitTest(false);
        row.addChild(name);

        int totalCount = countItemsInIsland(island.islandId());
        Label count = label(String.valueOf(totalCount), MUTED);
        count.layout(layout -> layout.heightPercent(100));
        count.textStyle(style -> style
                .textColor(MUTED)
                .textShadow(false)
                .fontSize(ROW_FONT_PX)
                .textAlignHorizontal(Horizontal.RIGHT)
                .textAlignVertical(Vertical.CENTER));
        count.setAllowHitTest(false);
        row.addChild(count);

        installRowStateTracking(row, count, island);
        return row;
    }

    private int countItemsInIsland(String islandId) {
        int count = 0;
        for (SlotWorkspaceViewModel.AtlasItem item : host.viewModel.atlasItems()) {
            if (islandId.equals(item.islandId())) {
                count++;
            }
        }
        return count;
    }

    /**
     * TICK-driven row state. Layered:
     * <ol>
     *   <li><strong>Action preview</strong> (Deposit / Gather hover):
     *       paints the row + swaps the count label to show how many
     *       distinct identities would be moved by the hovered action.
     *       Mirrors AtlasCardBuilder's per-card outline. Different
     *       palettes per action (deposit = ACCENT, gather =
     *       ACTIVE_HOTBAR / kit) so the player learns one vocabulary.</li>
     *   <li><strong>Status dot</strong>: when no preview is active,
     *       accent overlay if an off-screen card in this section
     *       matches the active search OR is kit-needed.</li>
     * </ol>
     */
    private void installRowStateTracking(Button row, Label count, SlotWorkspaceViewModel.AtlasIsland island) {
        // null = no preview, otherwise PREVIEW_DEPOSIT or PREVIEW_GATHER.
        Object[] lastPreview = {null};
        int[] lastPreviewCount = {-1};
        boolean[] lastLit = {false};
        row.addEventListener(UIEvents.TICK, event -> {
            String activePreview = activePreview();
            int previewCount = activePreview == null ? 0 : countPreviewIdentities(activePreview, island.islandId());

            if (activePreview != null && previewCount > 0) {
                int previewColor = previewColor(activePreview);
                if (!activePreview.equals(lastPreview[0])) {
                    lastPreview[0] = activePreview;
                    row.style(style -> style.overlayTexture(rect((previewColor & 0x00FFFFFF) | 0x44000000)));
                }
                if (previewCount != lastPreviewCount[0]) {
                    lastPreviewCount[0] = previewCount;
                    count.setText(net.minecraft.network.chat.Component.literal(String.valueOf(previewCount)));
                    count.textStyle(style -> style
                            .textColor(previewColor)
                            .textShadow(false)
                            .fontSize(ROW_FONT_PX)
                            .textAlignHorizontal(Horizontal.RIGHT)
                            .textAlignVertical(Vertical.CENTER));
                }
                lastLit[0] = false;
                return;
            }

            if (lastPreview[0] != null) {
                // Preview just turned off — restore plain count + clear overlay.
                lastPreview[0] = null;
                lastPreviewCount[0] = -1;
                count.setText(net.minecraft.network.chat.Component.literal(String.valueOf(countItemsInIsland(island.islandId()))));
                count.textStyle(style -> style
                        .textColor(MUTED)
                        .textShadow(false)
                        .fontSize(ROW_FONT_PX)
                        .textAlignHorizontal(Horizontal.RIGHT)
                        .textAlignVertical(Vertical.CENTER));
                lastLit[0] = false;
                row.style(style -> style.overlayTexture(IGuiTexture.EMPTY));
            }

            boolean lit = sectionNeedsAttention(island);
            if (lit == lastLit[0]) {
                return;
            }
            lastLit[0] = lit;
            row.style(style -> style.overlayTexture(
                    lit ? rect(HOVER_ACCENT_OVERLAY) : IGuiTexture.EMPTY));
        });
    }

    private static final String PREVIEW_DEPOSIT = "deposit";
    private static final String PREVIEW_GATHER = "gather";

    /** Active hover preview tag, or null if none. Deposit wins ties. */
    private String activePreview() {
        if (host.depositPreviewActive) {
            return PREVIEW_DEPOSIT;
        }
        if (host.gatherPreviewActive) {
            return PREVIEW_GATHER;
        }
        return null;
    }

    private int previewColor(String preview) {
        return PREVIEW_GATHER.equals(preview) ? ACTIVE_HOTBAR : ACCENT;
    }

    private int countPreviewIdentities(String preview, String islandId) {
        if (PREVIEW_DEPOSIT.equals(preview)) {
            java.util.Set<SlotWorkspaceViewModel.IdentityRef> depositable = host.viewModel.depositableIdentities();
            if (depositable.isEmpty()) {
                return 0;
            }
            int count = 0;
            for (SlotWorkspaceViewModel.AtlasItem item : host.viewModel.atlasItems()) {
                if (islandId.equals(item.islandId()) && depositable.contains(item.identity())) {
                    count++;
                }
            }
            return count;
        }
        if (PREVIEW_GATHER.equals(preview)) {
            int count = 0;
            for (SlotWorkspaceViewModel.AtlasItem item : host.viewModel.atlasItems()) {
                if (islandId.equals(item.islandId()) && AtlasCardBuilder.isGatherableItem(item)) {
                    count++;
                }
            }
            return count;
        }
        return 0;
    }

    private boolean sectionNeedsAttention(SlotWorkspaceViewModel.AtlasIsland island) {
        boolean searching = !host.searchController.normalizedQuery().isBlank();
        boolean offscreen = isSectionOffscreen(island.islandId());
        if (!offscreen) {
            return false;
        }
        for (SlotWorkspaceViewModel.AtlasItem item : host.viewModel.atlasItems()) {
            if (!island.islandId().equals(item.islandId())) {
                continue;
            }
            if (searching && host.searchController.matchesItem(item)) {
                return true;
            }
            if (item.kitNeeded()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Heuristic check: is the section currently scrolled out of the wall
     * viewport? Walks the scroller's children to find the section by
     * island id, then compares its top to the visible content rect.
     */
    private boolean isSectionOffscreen(String islandId) {
        if (host.wallScroller == null) {
            return false;
        }
        UIElement section = findSectionElement(islandId);
        if (section == null) {
            return false;
        }
        float scrollerTop = host.wallScroller.getPositionY();
        float scrollerHeight = host.wallScroller.getSizeHeight();
        float sectionTop = section.getPositionY();
        float sectionHeight = section.getSizeHeight();
        float sectionBottom = sectionTop + sectionHeight;
        float scrollerBottom = scrollerTop + scrollerHeight;
        return sectionBottom <= scrollerTop || sectionTop >= scrollerBottom;
    }

    private UIElement findSectionElement(String islandId) {
        if (host.wallScroller == null) {
            return null;
        }
        for (UIElement child : host.wallScroller.viewContainer.getChildren()) {
            if (islandId.equals(child.getId())) {
                return child;
            }
        }
        return null;
    }

    /**
     * Scroll the wall so the named section sits at the top of the
     * viewport. The vertical scroller's value is normalized to [0, 1]
     * (see {@code ScrollerView#onVerticalScroll}: the viewContainer's
     * {@code top} layout is set to {@code -value * (containerHeight -
     * viewportHeight)}). So:
     *
     * <ul>
     *   <li>Section's logical Y inside the container = its current
     *       screen Y minus the container's current screen Y (the
     *       layout offset stays constant across scrolls).</li>
     *   <li>Max scroll distance = container height - viewport height,
     *       approximated here as scroller's own height (the viewport
     *       fills the scroller minus scrollbar chrome, which is small
     *       relative to the wall).</li>
     *   <li>Target value = sectionLogicalY / maxScroll, clamped.</li>
     * </ul>
     */
    private void scrollWallToSection(String islandId) {
        if (host.wallScroller == null) {
            return;
        }
        UIElement section = findSectionElement(islandId);
        if (section == null) {
            return;
        }
        float containerScreenY = host.wallScroller.viewContainer.getPositionY();
        float sectionLogicalY = section.getPositionY() - containerScreenY;
        float containerHeight = host.wallScroller.getContainerHeight();
        float viewportHeight = host.wallScroller.getSizeHeight();
        float maxScroll = Math.max(1f, containerHeight - viewportHeight);
        float normalized = Math.max(0f, Math.min(1f, sectionLogicalY / maxScroll));
        host.wallScroller.verticalScroller.setValue(normalized);
    }
}
