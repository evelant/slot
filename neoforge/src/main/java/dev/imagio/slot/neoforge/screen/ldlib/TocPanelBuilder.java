package dev.imagio.slot.neoforge.screen.ldlib;

import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.*;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceUi.*;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.IslandDrag;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;

/**
 * Sectioned wall table-of-contents. Lists every non-empty non-Triage
 * island in display order with a colored swatch, label, and item count.
 *
 * <ul>
 *   <li>Click a row → scroll the wall to that section.</li>
 *   <li>Drag a row → reorder the section in the wall via
 *       {@code sendReorderIsland}; the drop position (upper or lower
 *       half of the anchor row) decides whether the source lands
 *       before or after the anchor in the {@code playerIslands}
 *       list. Empty (hidden) sections keep their position because the
 *       command resolves indices in the projection's full
 *       {@code playerIslands} order, not the visible TOC.</li>
 * </ul>
 *
 * <p>Status dots (off-screen search match, off-screen kit-needed) light
 * up when a matching card lives in this section and the section's
 * center sits outside the wall's viewport.
 */
final class TocPanelBuilder {
    private static final int ROW_HEIGHT = 9;
    private static final int SWATCH_WIDTH = 3;
    private static final int PANEL_PADDING = 3;
    private static final int PANEL_GAP = 1;
    private static final float ROW_FONT_PX = 6f;

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
        UIElement panel = panel(GLASS).layout(layout -> layout
                .widthPercent(100)
                .paddingAll(PANEL_PADDING)
                .gapAll(PANEL_GAP)
                .flexDirection(FlexDirection.COLUMN));
        panel.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());
        panel.style(style -> style.zIndex(7));

        for (SlotWorkspaceViewModel.AtlasIsland island : entries) {
            panel.addChild(row(island));
        }
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
        installRowDragSource(row, island);
        installRowDropTarget(row, island);

        UIElement swatch = panel(island.color()).layout(layout -> layout
                .width(SWATCH_WIDTH)
                .heightPercent(100));
        swatch.setAllowHitTest(false);
        row.addChild(swatch);

        // No hard truncation — the row's flex(1) label container
        // soft-clips when the column is narrower than the text. A
        // hardcoded 14-char cap was ellipsizing well-formed labels
        // ("Mechanisms" → "Mechanism…", "Workbenches" → "Workbench…")
        // even when the row had plenty of horizontal slack, especially
        // in sidebar mode where the left column is much wider than
        // standalone. paddingRight gives the text room to breathe
        // before it bumps against the count / status indicators on the
        // right.
        Label name = label(island.label(), TEXT);
        name.layout(layout -> layout.flex(1).heightPercent(100).paddingRight(6));
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
     * Drag-source binding for a TOC row. Mouse-down + drag past the row
     * boundary starts an {@link IslandDrag} carrying just the source
     * island id; the drag texture is a small colored stripe in the
     * island's swatch color so the player has a visual handle to track.
     */
    private void installRowDragSource(Button row, SlotWorkspaceViewModel.AtlasIsland island) {
        row.addEventListener(UIEvents.MOUSE_LEAVE, event -> {
            if (!DragDropWiring.mouseIsHeldOnSource(row)) {
                return;
            }
            if (row.getModularUI() != null && row.getModularUI().getDragHandler().isDragging()) {
                return;
            }
            row.startDrag(
                    new IslandDrag(island.islandId()),
                    rect(island.color())
            ).setDragTexture(-12, -2, 24, 4);
            host.localStatus.set("dragging " + island.label());
        }, true);
    }

    /**
     * Drop-target binding. Resolves the drop's vertical half (above or
     * below the anchor row's center) into an insert position relative
     * to the anchor's index in the canonical {@code playerIslands}
     * list, then converts that to the post-removal final index the
     * server expects (the projection clamps + reinserts). Drops onto
     * the source's own row are no-ops.
     */
    private void installRowDropTarget(Button row, SlotWorkspaceViewModel.AtlasIsland anchor) {
        row.addEventListener(UIEvents.DRAG_ENTER, event -> updateDropOverlay(row, event), true);
        row.addEventListener(UIEvents.DRAG_UPDATE, event -> updateDropOverlay(row, event));
        row.addEventListener(UIEvents.DRAG_LEAVE, event -> clearDropOverlay(row), true);
        row.addEventListener(UIEvents.DRAG_PERFORM, event -> {
            clearDropOverlay(row);
            IslandDrag drag = islandDrag(event);
            if (drag == null) {
                return;
            }
            event.stopPropagation();
            if (anchor.islandId().equals(drag.islandId())) {
                return;
            }
            int sourceIndex = playerIslandIndexOf(drag.islandId());
            int anchorIndex = playerIslandIndexOf(anchor.islandId());
            if (sourceIndex < 0 || anchorIndex < 0) {
                return;
            }
            float halfHeight = row.getSizeHeight() / 2f;
            boolean upperHalf = (event.y - row.getPositionY()) < halfHeight;
            int insertPosition = upperHalf ? anchorIndex : anchorIndex + 1;
            // The projection removes the source first, then inserts at
            // the clamped target. So if source sat to the left of the
            // logical insert position, every later index drops by one.
            int targetIndex = sourceIndex < insertPosition ? insertPosition - 1 : insertPosition;
            if (targetIndex == sourceIndex) {
                return;
            }
            host.rpc.sendReorderIsland(drag.islandId(), targetIndex);
        });
    }

    private void updateDropOverlay(UIElement row, UIEvent event) {
        if (islandDrag(event) == null) {
            clearDropOverlay(row);
            return;
        }
        row.style(style -> style.overlayTexture(rect((ACCENT & 0x00FFFFFF) | 0x44000000)));
    }

    private void clearDropOverlay(UIElement row) {
        row.style(style -> style.overlayTexture(IGuiTexture.EMPTY));
    }

    private IslandDrag islandDrag(UIEvent event) {
        Object payload = event == null || event.dragHandler == null ? null : event.dragHandler.getDraggingObject();
        return payload instanceof IslandDrag islandDrag ? islandDrag : null;
    }

    /**
     * Index of {@code islandId} in the view model's player-island list,
     * which is in {@code playerIslands} order (Triage is never in the
     * view model). Returns -1 if absent.
     */
    private int playerIslandIndexOf(String islandId) {
        int index = 0;
        for (SlotWorkspaceViewModel.AtlasIsland island : host.viewModel.islands()) {
            if (island.kind() == VisualAtlasIslandKind.TRIAGE) {
                continue;
            }
            if (islandId.equals(island.islandId())) {
                return index;
            }
            index++;
        }
        return -1;
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
