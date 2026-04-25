package dev.imagio.slot.neoforge.screen.ldlib;

import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceFormat.*;
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
import dev.imagio.slot.inventory.workspace.SlotWorkspaceAtlasLayout;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.AtlasItemDrag;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.ChestStackDrag;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.ChestTileDrag;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.HotbarSlotDrag;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.IslandDrag;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.StorageZoneBounds;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.StorageZoneDrag;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

final class IslandChestBuilder {
    private final SlotWorkspaceUiController host;

    IslandChestBuilder(SlotWorkspaceUiController host) {
        this.host = host;
    }

    UIElement islandPanel(SlotAtlasGraphView atlas, SlotWorkspaceViewModel.AtlasIsland island) {
        UIElement panel = panel(island.color()).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(island.x())
                .top(island.y())
                .width(island.width())
                .height(island.height())
                .paddingAll(8)
                .gapAll(3)
                .flexDirection(FlexDirection.COLUMN));
        // zIndex in LDLib2 only affects hit-test priority (see
        // UIElement.getSortedChildren, used by UIEventDispatcher) — the
        // draw order is child-insertion order. Give the panel a zIndex
        // matching the chest tile panel (1) so a right-click lands on
        // the island body before falling through to the atlas viewport.
        panel.style(style -> style.zIndex(1));

        host.drag.installViewportPanSurface(panel, atlas);
        host.drag.installIslandDropTarget(panel, panel, atlas, island);

        // Right-click opens the island edit popover anchored near the click,
        // matching how item cards and kit cards surface their context host.menu.
        if (island.kind() == VisualAtlasIslandKind.PLAYER) {
            panel.addEventListener(UIEvents.MOUSE_DOWN, event -> {
                if (event.button != 1) {
                    return;
                }
                event.stopPropagation();
                host.menu.beginIslandEdit(island, event.x, event.y);
            }, true);
        }

        attachIslandHoverListeners(panel, island);

        return panel;
    }

    /**
     * Install MOUSE_ENTER/LEAVE listeners on any atlas-level element that
     * belongs visually to an island (panel, header, item cards, badge)
     * so hovering ANY of them surfaces the dim non-proximate link threads.
     * Atlas host.content is flat — these elements are siblings, not nested —
     * so each one needs its own listener to flip {@code host.hoveredIslandId}.
     * No-op for non-host.player islands and islands without non-proximate links.
     */
    void attachIslandHoverListeners(UIElement element, SlotWorkspaceViewModel.AtlasIsland island) {
        if (element == null || island == null) {
            return;
        }
        if (island.kind() != VisualAtlasIslandKind.PLAYER) {
            return;
        }
        if (!islandHasNonProximateLinks(island.islandId())) {
            return;
        }
        String islandId = island.islandId();
        element.addEventListener(UIEvents.MOUSE_ENTER, event -> {
            if (!islandId.equals(host.hoveredIslandId)) {
                host.hoveredIslandId = islandId;
                host.rebuild();
            }
        }, true);
        element.addEventListener(UIEvents.MOUSE_LEAVE, event -> {
            if (islandId.equals(host.hoveredIslandId)) {
                host.hoveredIslandId = null;
                host.rebuild();
            }
        }, true);
    }

    boolean islandHasNonProximateLinks(String islandId) {
        if (islandId == null || islandId.isBlank()) {
            return false;
        }
        for (SlotWorkspaceViewModel.ClaimedChestTile tile : host.viewModel.claimedChestTiles()) {
            if (tile.proximate()) {
                continue;
            }
            if (tile.linkedIslandIds().contains(islandId)) {
                return true;
            }
        }
        return false;
    }

    UIElement islandCarriedBadge(SlotAtlasGraphView atlas, SlotWorkspaceViewModel.AtlasIsland island) {
        // Positioned on the title-bar strip above the island panel so it
        // doesn't overlap the first row of item cards. Uses world-unit
        // absolute positioning against atlas host.content like every other
        // atlas-level element.
        Button badge = button(island.carriedCount() + "●", true, ACTIVE_HOTBAR);
        badge.layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(island.x() + 2)
                .top(island.y() - 14)
                .width(26)
                .height(12));
        badge.textStyle(style -> style
                .textColor(TEXT)
                .textShadow(false)
                .fontSize(7)
                .textAlignHorizontal(Horizontal.CENTER)
                .textAlignVertical(Vertical.CENTER));
        badge.style(style -> style.zIndex(4));
        badge.setOnClick(event -> {
            event.stopPropagation();
            host.camera.panToIsland(atlas, island);
            host.localStatus.set("panned to " + island.label());
        });
        return badge;
    }

    UIElement islandTitleBar(
            SlotAtlasGraphView atlas,
            SlotWorkspaceViewModel.AtlasIsland island,
            UIElement islandPanelEl
    ) {
        Button header = button(island.label(), true, island.color());
        header.layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(island.x())
                .top(island.y() - 16)
                .width(island.width())
                .height(14));
        header.style(style -> style.zIndex(3));
        header.addEventListener(UIEvents.CLICK, event -> {
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
        // Right-click on the header opens the edit popover, matching the
        // island body's behaviour. Without this, right-click on the
        // header would fall through to the atlas viewport (which pans on
        // right-drag) instead of surfacing the context menu the user
        // expects when targeting the island by its label.
        if (island.kind() == VisualAtlasIslandKind.PLAYER) {
            header.addEventListener(UIEvents.MOUSE_DOWN, event -> {
                if (event.button != 1) {
                    return;
                }
                event.stopPropagation();
                host.menu.beginIslandEdit(island, event.x, event.y);
            }, true);
        }
        host.drag.installIslandDragSource(header, atlas, island);
        host.drag.installIslandDropTarget(header, islandPanelEl, atlas, island);
        attachIslandHoverListeners(header, island);

        float[] lastScale = {Float.NaN};
        int[] lastWorldFontQuarter = {-1};
        Runnable applyHeaderScale = () -> {
            // Track the actual render scale (not animation target) so the
            // header stays sized to the live view each frame. Using
            // host.animationTargetScale caused the header to be sized for the
            // final scale while the pose stack drew at the interpolated
            // current scale — visible as a flash at animation boundaries.
            float scale = Math.max(0.0001f, atlas.getScale());
            if (scale == lastScale[0]) {
                return;
            }
            lastScale[0] = scale;
            float islandScreenWidth = island.width() * scale;
            float requestedFontPx = Math.min(12f, islandScreenWidth * 0.13f);
            float screenFontPx = headerBreakpointFontPx(Math.max(7f, requestedFontPx));
            float worldFontPx = screenFontPx / Math.max(0.0001f, scale);
            float screenHeaderHeight = screenFontPx + 3f;
            // Floor the world height at the carried-count badge's world
            // size (12 world units plus a 2-unit margin = 14) so the badge
            // never overflows the header background. Without this, at
            // scale > ~1 the screen-fixed header shrinks in world space
            // below the badge's world size and the counter visibly
            // escapes its backdrop.
            float worldHeaderHeight = Math.max(14f, screenHeaderHeight / Math.max(0.0001f, scale));
            float screenGap = 2f;
            float worldGap = screenGap / Math.max(0.0001f, scale);

            // Re-apply textStyle whenever the quantized world fontSize
            // changes. The previous gate on screenFontPx alone missed the
            // case where scale shifts within a clamped range (min/max of
            // screenFontPx), which left worldFontPx baked at the old scale
            // and the rendered screen pixels drifting with zoom.
            int worldFontQuarter = Math.max(1, Math.round(worldFontPx * 4f));
            if (worldFontQuarter != lastWorldFontQuarter[0]) {
                lastWorldFontQuarter[0] = worldFontQuarter;
                float quantizedWorldFont = worldFontQuarter / 4f;
                header.textStyle(style -> style
                        .textColor(TEXT)
                        .textShadow(true)
                        .fontSize(quantizedWorldFont)
                        .textAlignHorizontal(Horizontal.CENTER)
                        .textAlignVertical(Vertical.CENTER));
            }
            header.layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(island.x())
                    .top(Math.round(island.y() - worldHeaderHeight - worldGap))
                    .width(island.width())
                    .height(Math.round(worldHeaderHeight)));
            header.markTaffyStyleDirty();
        };
        // Prime at build time so the first rendered frame after a host.rebuild
        // already has the scale-correct font/layout. Without this, the
        // header renders at Button's default fontSize until the next
        // screen tick fires — which, during rapid rebuilds from scroll-
        // wheel transfer, could be several frames of flicker.
        applyHeaderScale.run();
        header.addEventListener(UIEvents.TICK, event -> applyHeaderScale.run());
        return header;
    }

    StorageZoneBounds storageZoneBounds() {
        List<SlotWorkspaceViewModel.ClaimedChestTile> tiles = host.viewModel.claimedChestTiles();
        if (tiles.isEmpty()) {
            return null;
        }
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (SlotWorkspaceViewModel.ClaimedChestTile tile : tiles) {
            minX = Math.min(minX, tile.atlasX());
            minY = Math.min(minY, tile.atlasY());
            maxX = Math.max(maxX, tile.atlasX() + tile.width());
            maxY = Math.max(maxY, tile.atlasY() + tile.height());
        }
        int pad = SlotWorkspaceAtlasLayout.STORAGE_ZONE_PADDING;
        return new StorageZoneBounds(
                minX - pad,
                minY - pad,
                (maxX - minX) + pad * 2,
                (maxY - minY) + pad * 2
        );
    }

    UIElement storageZoneBackdrop(StorageZoneBounds bounds) {
        UIElement backdrop = panel(STORAGE_ZONE_FILL).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(bounds.left())
                .top(bounds.top())
                .width(bounds.width())
                .height(bounds.height()));
        backdrop.style(style -> style.zIndex(0));
        backdrop.setAllowHitTest(false);
        return backdrop;
    }

    UIElement storageZoneHeader(StorageZoneBounds bounds, SlotAtlasGraphView atlas) {
        int headerHeight = STORAGE_ZONE_HEADER_HEIGHT;
        UIElement header = panel(STORAGE_ZONE_HEADER_FILL).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(bounds.left())
                .top(bounds.top() - headerHeight)
                .width(bounds.width())
                .height(headerHeight)
                .paddingHorizontal(8)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.ROW));
        header.style(style -> style.zIndex(1));
        Label title = label("Storage", ACCENT);
        title.layout(layout -> layout.flex(1).height(headerHeight));
        title.textStyle(style -> style
                .textColor(ACCENT)
                .textShadow(false)
                .fontSize(8)
                .textAlignHorizontal(Horizontal.LEFT)
                .textAlignVertical(Vertical.CENTER));
        title.setAllowHitTest(false);
        header.addChild(title);
        installStorageZoneDragSource(header, atlas, bounds);
        return header;
    }

    void installStorageZoneDragSource(UIElement source, SlotAtlasGraphView atlas, StorageZoneBounds bounds) {
        int[] clickWorldX = {Integer.MIN_VALUE};
        int[] clickWorldY = {Integer.MIN_VALUE};
        source.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button != 0) {
                return;
            }
            clickWorldX[0] = atlas.worldX(event.x);
            clickWorldY[0] = atlas.worldY(event.y);
        });
        source.addEventListener(UIEvents.MOUSE_UP, event -> {
            clickWorldX[0] = Integer.MIN_VALUE;
            clickWorldY[0] = Integer.MIN_VALUE;
        });
        source.addEventListener(UIEvents.MOUSE_MOVE, event -> {
            if (clickWorldX[0] == Integer.MIN_VALUE) {
                return;
            }
            if (!source.isMouseDown(0) || host.drag.isDragging(source)) {
                return;
            }
            float scale = atlas.getScale();
            float screenDx = (atlas.worldX(event.x) - clickWorldX[0]) * scale;
            float screenDy = (atlas.worldY(event.y) - clickWorldY[0]) * scale;
            if (screenDx * screenDx + screenDy * screenDy < DRAG_START_THRESHOLD_PX * DRAG_START_THRESHOLD_PX) {
                return;
            }
            int grabOffsetX = clickWorldX[0] - bounds.left();
            int grabOffsetY = clickWorldY[0] - bounds.top();
            int widthPx = Math.max(48, atlas.screenPixelsForWorldUnits(bounds.width()));
            int heightPx = Math.max(20, atlas.screenPixelsForWorldUnits(bounds.height() + STORAGE_ZONE_HEADER_HEIGHT));
            int dragOffsetX = Math.round(grabOffsetX * scale);
            int dragOffsetY = Math.round((grabOffsetY + STORAGE_ZONE_HEADER_HEIGHT) * scale);
            source.startDrag(
                    new StorageZoneDrag(grabOffsetX, grabOffsetY, bounds.left(), bounds.top()),
                    rect((STORAGE_ZONE_FILL & 0x00FFFFFF) | 0x60000000)
            ).setDragTexture(-dragOffsetX, -dragOffsetY, widthPx, heightPx);
            host.localStatus.set("moving storage zone");
        });
        source.addEventListener(UIEvents.DRAG_END, event -> host.drag.handleDragEnd(event));
    }

    UIElement chestTilePanel(SlotAtlasGraphView atlas, SlotWorkspaceViewModel.ClaimedChestTile tile) {
        int fill = tile.proximate() ? STORAGE_TILE_FILL : STORAGE_TILE_FILL_DIM;
        int textColor = tile.proximate() ? TEXT : MUTED;
        int cellSize = SlotWorkspaceAtlasLayout.CHEST_TILE_CELL;
        int cols = SlotWorkspaceAtlasLayout.CHEST_TILE_COLUMNS;
        int padding = SlotWorkspaceAtlasLayout.CHEST_TILE_PADDING;
        int headerHeight = SlotWorkspaceAtlasLayout.CHEST_TILE_HEADER_HEIGHT;

        UIElement panel = panel(fill).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(tile.atlasX())
                .top(tile.atlasY())
                .width(tile.width())
                .height(tile.height())
                .paddingAll(0));
        panel.style(style -> style.zIndex(1));

        Label header = label(tile.label(), textColor);
        header.layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(padding)
                .top(0)
                .width(tile.width() - padding * 2)
                .height(headerHeight));
        header.textStyle(style -> style
                .textColor(textColor)
                .textShadow(false)
                .fontSize(8)
                .textAlignHorizontal(Horizontal.LEFT)
                .textAlignVertical(Vertical.CENTER));
        panel.addChild(header);

        int cellsToRender = tile.contents().size();
        List<Integer> contentIndices = tile.contentSlotIndices();
        for (int index = 0; index < cellsToRender; index++) {
            ItemStack stack = tile.contents().get(index);
            int chestSlotIndex = index < contentIndices.size() ? contentIndices.get(index) : index;
            int col = index % cols;
            int row = index / cols;
            int cellX = padding + col * cellSize;
            int cellY = headerHeight + row * cellSize;
            UIElement cell = chestTileCell(stack, tile.proximate(), cellSize);
            cell.layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(cellX)
                    .top(cellY)
                    .width(cellSize)
                    .height(cellSize));
            if (stack != null && !stack.isEmpty()) {
                SlotWorkspaceViewModel.IdentityRef cellIdentity = SlotWorkspaceViewModel.IdentityRef.from(
                        dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(stack));
                String cellStorageId = tile.storageId();
                cell.addEventListener(UIEvents.MOUSE_ENTER, event -> {
                    host.hoveredChestCellIdentity = cellIdentity;
                    host.hoveredChestCellStorageId = cellStorageId;
                }, true);
                cell.addEventListener(UIEvents.MOUSE_LEAVE, event -> {
                    if (cellIdentity.equals(host.hoveredChestCellIdentity)
                            && cellStorageId.equals(host.hoveredChestCellStorageId)) {
                        host.hoveredChestCellIdentity = null;
                        host.hoveredChestCellStorageId = null;
                    }
                }, true);
            }
            if (tile.proximate() && stack != null && !stack.isEmpty()) {
                String storageId = tile.storageId();
                ItemStack cellStack = stack;
                cell.addEventListener(UIEvents.CLICK, event -> {
                    if (event.button != 0) {
                        return;
                    }
                    event.stopPropagation();
                    if (Screen.hasShiftDown()) {
                        host.rpc.sendTakeFromChest(storageId, chestSlotIndex);
                        return;
                    }
                    SlotWorkspaceViewModel.IdentityRef identityRef = SlotWorkspaceViewModel.IdentityRef.from(
                            dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(cellStack));
                    host.selectedAtlasIdentity.set(identityRef);
                    host.selectedHotbarIndex.set(-1);
                    host.localStatus.set("selected " + cellStack.getHoverName().getString());
                });
                host.drag.installChestStackDragSource(cell, atlas, storageId, chestSlotIndex, cellStack, tile.label());
            }
            panel.addChild(cell);
        }

        Button linkButton = button("Link", true, tile.linkedIslandIds().isEmpty() ? PANEL_ALT : ACCENT);
        linkButton.layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .right(padding)
                .top(0)
                .width(28)
                .height(headerHeight));
        linkButton.textStyle(style -> style
                .textColor(TEXT)
                .textShadow(false)
                .fontSize(7)
                .textAlignHorizontal(Horizontal.CENTER)
                .textAlignVertical(Vertical.CENTER));
        linkButton.style(style -> style.zIndex(3));
        linkButton.setOnClick(event -> {
            event.stopPropagation();
            host.menu.beginChestLinkEdit(tile);
        });
        panel.addChild(linkButton);

        boolean canTake = tile.proximate() && !tile.contents().isEmpty();
        Button takeAllButton = button("Take", canTake, canTake ? ROW : PANEL_ALT);
        takeAllButton.layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .right(padding + 30)
                .top(0)
                .width(28)
                .height(headerHeight));
        takeAllButton.textStyle(style -> style
                .textColor(canTake ? TEXT : MUTED)
                .textShadow(false)
                .fontSize(7)
                .textAlignHorizontal(Horizontal.CENTER)
                .textAlignVertical(Vertical.CENTER));
        takeAllButton.style(style -> style.zIndex(3));
        String takeStorageId = tile.storageId();
        takeAllButton.setOnClick(event -> {
            event.stopPropagation();
            if (!canTake) {
                host.localStatus.set(tile.proximate() ? "chest is empty" : "chest is too far");
                host.rebuild();
                return;
            }
            host.rpc.sendTakeAll(takeStorageId);
        });
        panel.addChild(takeAllButton);

        host.drag.installChestTileDragSource(panel, atlas, tile);
        installChestTileDropTarget(panel, tile);
        return panel;
    }

    void installChestTileDropTarget(UIElement target, SlotWorkspaceViewModel.ClaimedChestTile tile) {
        String storageId = tile.storageId();
        boolean proximate = tile.proximate();
        target.addEventListener(UIEvents.DRAG_ENTER, event -> updateChestTileDropOverlay(target, proximate, event), true);
        target.addEventListener(UIEvents.DRAG_UPDATE, event -> updateChestTileDropOverlay(target, proximate, event));
        target.addEventListener(UIEvents.DRAG_LEAVE, event -> host.drag.clearDropOverlay(target), true);
        target.addEventListener(UIEvents.DRAG_PERFORM, event -> {
            host.drag.clearDropOverlay(target);
            AtlasItemDrag atlasDrag = host.drag.atlasItemDrag(event);
            HotbarSlotDrag hotbarDrag = host.drag.hotbarSlotDrag(event);
            if (atlasDrag == null && hotbarDrag == null) {
                return;
            }
            if (!proximate) {
                host.localStatus.set("chest is too far");
                host.rebuild();
                event.stopPropagation();
                return;
            }
            if (atlasDrag != null) {
                host.rpc.sendDepositCarriedToChest(atlasDrag.identity(), storageId);
            } else {
                host.rpc.sendDepositHotbarToChest(hotbarDrag.hotbarIndex(), storageId);
            }
            event.stopPropagation();
        });
    }

    void updateChestTileDropOverlay(UIElement target, boolean proximate, UIEvent event) {
        boolean acceptable = host.drag.atlasItemDrag(event) != null || host.drag.hotbarSlotDrag(event) != null;
        host.drag.updateGenericDropOverlay(target, acceptable, proximate ? ACCENT : WARNING);
    }


    Set<String> highlightedIslandIdsFromProximateTiles() {
        LinkedHashSet<String> highlighted = new LinkedHashSet<>();
        for (SlotWorkspaceViewModel.ClaimedChestTile tile : host.viewModel.claimedChestTiles()) {
            if (tile.proximate()) {
                highlighted.addAll(tile.linkedIslandIds());
            }
        }
        return highlighted;
    }

    void addIslandHighlightFrame(SlotAtlasGraphView atlas, SlotWorkspaceViewModel.AtlasIsland island) {
        int thickness = LINK_HIGHLIGHT_THICKNESS;
        int color = LINK_HIGHLIGHT_COLOR;
        int x = island.x();
        int y = island.y();
        int w = island.width();
        int h = island.height();
        atlas.addContentChild(highlightFrameSegment(color, x - thickness, y - thickness, w + thickness * 2, thickness));
        atlas.addContentChild(highlightFrameSegment(color, x - thickness, y + h, w + thickness * 2, thickness));
        atlas.addContentChild(highlightFrameSegment(color, x - thickness, y, thickness, h));
        atlas.addContentChild(highlightFrameSegment(color, x + w, y, thickness, h));
    }

    UIElement highlightFrameSegment(int color, int x, int y, int w, int h) {
        UIElement segment = panel(color).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(x)
                .top(y)
                .width(w)
                .height(h));
        segment.style(style -> style.zIndex(5));
        segment.setAllowHitTest(false);
        return segment;
    }

    void addLinkAffordances(
            SlotAtlasGraphView atlas,
            SlotWorkspaceViewModel.ClaimedChestTile tile,
            SlotWorkspaceViewModel.AtlasIsland island
    ) {
        UIElement thread = linkThread(tile, island);
        if (thread != null) {
            atlas.addContentChild(thread);
        }

        float tileCx = tile.atlasX() + tile.width() / 2f;
        float tileCy = tile.atlasY() + tile.height() / 2f;
        float islandCx = island.x() + island.width() / 2f;
        float islandCy = island.y() + island.height() / 2f;
        float dx = islandCx - tileCx;
        float dy = islandCy - tileCy;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        if (distance < 1f) {
            return;
        }
        float cosA = dx / distance;
        float sinA = dy / distance;
        float angleDeg = (float) Math.toDegrees(Math.atan2(dy, dx));

        float tileEdge = rectEdgeAlongDirection(tile.width(), tile.height(), cosA, sinA);
        float tileArrowX = tileCx + (tileEdge + 6f) * cosA;
        float tileArrowY = tileCy + (tileEdge + 6f) * sinA;
        atlas.addContentChild(linkArrow(tileArrowX, tileArrowY, angleDeg, () -> {
            host.camera.panToIsland(atlas, island);
            host.localStatus.set("linked island: " + island.label());
        }));

        float islandEdge = rectEdgeAlongDirection(island.width(), island.height(), cosA, sinA);
        float islandArrowX = islandCx - (islandEdge + 6f) * cosA;
        float islandArrowY = islandCy - (islandEdge + 6f) * sinA;
        atlas.addContentChild(linkArrow(islandArrowX, islandArrowY, angleDeg + 180f, () -> {
            host.camera.panToChestTile(atlas, tile);
            host.localStatus.set("linked chest: " + tile.label());
        }));
    }

    float rectEdgeAlongDirection(int width, int height, float cosA, float sinA) {
        float ax = Math.abs(cosA);
        float ay = Math.abs(sinA);
        float tx = ax < 0.001f ? Float.POSITIVE_INFINITY : (width / 2f) / ax;
        float ty = ay < 0.001f ? Float.POSITIVE_INFINITY : (height / 2f) / ay;
        return Math.min(tx, ty);
    }

    UIElement linkArrow(float worldX, float worldY, float rotationDeg, Runnable onClick) {
        int size = 14;
        Button arrow = button("\u25B6", true, 0x00000000);
        arrow.layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(Math.round(worldX - size / 2f))
                .top(Math.round(worldY - size / 2f))
                .width(size)
                .height(size));
        arrow.textStyle(style -> style
                .textColor(LINK_THREAD_COLOR)
                .textShadow(false)
                .fontSize(10f)
                .textAlignHorizontal(Horizontal.CENTER)
                .textAlignVertical(Vertical.CENTER));
        arrow.buttonStyle(style -> {
            style.baseTexture(IGuiTexture.EMPTY);
            style.hoverTexture(rect(0x40FFFFFF));
            style.pressedTexture(rect(0x60FFFFFF));
        });
        // Draw order is controlled by insertion order in buildAtlas
        // (threads/arrows added before islands/chests). zIndex only
        // influences hit-testing priority; leaving it at 0 keeps island
        // and chest bodies (zIndex 1) ahead for clicks while still
        // letting the arrow receive clicks where nothing else overlaps.
        arrow.style(style -> style.zIndex(0));
        arrow.transform(transform -> transform.pivot(0.5f, 0.5f).rotation(rotationDeg));
        arrow.setOnClick(event -> {
            if (event.button != 0) {
                return;
            }
            event.stopPropagation();
            onClick.run();
        });
        return arrow;
    }

    UIElement linkThread(
            SlotWorkspaceViewModel.ClaimedChestTile tile,
            SlotWorkspaceViewModel.AtlasIsland island
    ) {
        return buildThread(tile, island, LINK_THREAD_COLOR, 2);
    }

    UIElement dimLinkThread(
            SlotWorkspaceViewModel.ClaimedChestTile tile,
            SlotWorkspaceViewModel.AtlasIsland island
    ) {
        return buildThread(tile, island, LINK_THREAD_DIM_COLOR, 1);
    }

    UIElement buildThread(
            SlotWorkspaceViewModel.ClaimedChestTile tile,
            SlotWorkspaceViewModel.AtlasIsland island,
            int color,
            int thickness
    ) {
        int tileCenterX = tile.atlasX() + tile.width() / 2;
        int tileCenterY = tile.atlasY() + tile.height() / 2;
        int islandCenterX = island.x() + island.width() / 2;
        int islandCenterY = island.y() + island.height() / 2;
        int dx = islandCenterX - tileCenterX;
        int dy = islandCenterY - tileCenterY;
        double distance = Math.sqrt((double) dx * dx + (double) dy * dy);
        if (distance < 1.0) {
            return null;
        }
        int length = Math.max(1, (int) Math.round(distance));
        float angleDeg = (float) Math.toDegrees(Math.atan2(dy, dx));
        UIElement thread = panel(color).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(tileCenterX)
                .top(tileCenterY - thickness / 2)
                .width(length)
                .height(thickness));
        thread.style(style -> style.zIndex(0));
        thread.transform(transform -> transform.pivot(0f, 0.5f).rotation(angleDeg));
        thread.setAllowHitTest(false);
        return thread;
    }

    UIElement hoverTrailOverlay(SlotAtlasGraphView atlas) {
        UIElement trail = panel(HOVER_TRAIL_COLOR);
        trail.style(style -> style.zIndex(9));
        trail.setAllowHitTest(false);
        trail.layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(0)
                .top(0)
                .width(0)
                .height(0));
        int[] lastLength = {0};
        trail.addEventListener(UIEvents.TICK, event -> {
            HoverTrailEndpoints endpoints = resolveHoverTrail();
            if (endpoints == null) {
                if (lastLength[0] != 0) {
                    trail.layout(layout -> layout
                            .positionType(TaffyPosition.ABSOLUTE)
                            .left(0).top(0).width(0).height(0));
                    trail.markTaffyStyleDirty();
                    lastLength[0] = 0;
                }
                return;
            }
            UIElement slotElement = host.hotbarSlotElements.get(endpoints.hotbarIndex());
            if (slotElement == null) {
                return;
            }
            float panelLeft = atlas.getPositionX();
            float panelTop = atlas.getPositionY();
            float slotW = slotElement.getSizeWidth();
            float slotH = slotElement.getSizeHeight();
            if (slotW <= 0f || slotH <= 0f) {
                return;
            }
            float originScreenX = slotElement.getPositionX() + slotW / 2f;
            float originScreenY = slotElement.getPositionY() + slotH / 2f;
            dev.imagio.slot.atlas.lod.AtlasLayoutResult.ItemPlacement endpointPlace =
                    host.placementFor(endpoints.atlasItem());
            int worldTargetX = endpointPlace.x() + endpointPlace.width() / 2;
            int worldTargetY = endpointPlace.y() + endpointPlace.height() / 2;
            float targetScreenX = atlas.screenX(worldTargetX);
            float targetScreenY = atlas.screenY(worldTargetY);
            float dx = targetScreenX - originScreenX;
            float dy = targetScreenY - originScreenY;
            double distance = Math.sqrt((double) dx * dx + (double) dy * dy);
            if (distance < 1.0) {
                return;
            }
            int length = Math.max(1, (int) Math.round(distance));
            float angleDeg = (float) Math.toDegrees(Math.atan2(dy, dx));
            int leftRelative = Math.round(originScreenX - panelLeft);
            int topRelative = Math.round(originScreenY - panelTop) - HOVER_TRAIL_THICKNESS / 2;
            trail.layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(leftRelative)
                    .top(topRelative)
                    .width(length)
                    .height(HOVER_TRAIL_THICKNESS));
            trail.transform(transform -> transform.pivot(0f, 0.5f).rotation(angleDeg));
            trail.markTaffyStyleDirty();
            lastLength[0] = length;
        });
        return trail;
    }

    HoverTrailEndpoints resolveHoverTrail() {
        if (host.hoveredHotbarIndex >= 0 && host.hoveredHotbarIndex < host.viewModel.hotbarSlots().size()) {
            SlotWorkspaceViewModel.HotbarSlot slot = host.viewModel.hotbarSlots().get(host.hoveredHotbarIndex);
            if (slot.occupied()) {
                SlotWorkspaceViewModel.IdentityRef identity = SlotWorkspaceViewModel.IdentityRef.from(
                        dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(slot.displayStack()));
                SlotWorkspaceViewModel.AtlasItem item = atlasItemInIslandLayer(identity);
                if (item != null) {
                    return new HoverTrailEndpoints(slot.hotbarIndex(), item);
                }
            }
        }
        if (host.hoveredAtlasIdentity != null) {
            int hotbarIndex = host.hotbarSlotForIdentity(host.hoveredAtlasIdentity);
            SlotWorkspaceViewModel.AtlasItem item = atlasItemInIslandLayer(host.hoveredAtlasIdentity);
            if (hotbarIndex >= 0 && item != null) {
                return new HoverTrailEndpoints(hotbarIndex, item);
            }
        }
        return null;
    }

    SlotWorkspaceViewModel.AtlasItem atlasItemInIslandLayer(SlotWorkspaceViewModel.IdentityRef identity) {
        if (identity == null) {
            return null;
        }
        for (SlotWorkspaceViewModel.AtlasItem candidate : host.viewModel.atlasItems()) {
            if (candidate.identity().equals(identity)) {
                return candidate;
            }
        }
        return null;
    }


    record HoverTrailEndpoints(int hotbarIndex, SlotWorkspaceViewModel.AtlasItem atlasItem) {
    }

    UIElement chestTileCell(ItemStack stack, boolean proximate, int cellSize) {
        int chromeColor = proximate ? STORAGE_TILE_CELL_FILL : STORAGE_TILE_CELL_FILL_DIM;
        UIElement cell = panel(chromeColor);
        if (stack != null && !stack.isEmpty()) {
            int iconSize = Math.max(8, cellSize - 2);
            UIElement icon = itemIcon(stack, iconSize, proximate);
            icon.layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(1)
                    .top(1));
            cell.addChild(icon);
        }
        return cell;
    }

}
