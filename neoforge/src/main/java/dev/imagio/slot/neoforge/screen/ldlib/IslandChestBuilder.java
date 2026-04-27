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
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.HotbarSlotDrag;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

final class IslandChestBuilder {
    private final SlotWorkspaceUiController host;

    IslandChestBuilder(SlotWorkspaceUiController host) {
        this.host = host;
    }

    UIElement islandPanel(SlotAtlasGraphView atlas, SlotWorkspaceViewModel.AtlasIsland island) {
        dev.imagio.slot.atlas.lod.AtlasLayoutResult.IslandPlacement place = host.islandPlacementFor(island);
        UIElement panel = panel(island.color()).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(place.x())
                .top(place.y())
                .width(place.width())
                .height(place.height())
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
     * Track {@code host.hoveredIslandId} on enter/leave so chest cards in
     * the storage strip can highlight themselves when their linked island
     * is hovered. No-op for non-player islands.
     */
    void attachIslandHoverListeners(UIElement element, SlotWorkspaceViewModel.AtlasIsland island) {
        if (element == null || island == null) {
            return;
        }
        if (island.kind() != VisualAtlasIslandKind.PLAYER) {
            return;
        }
        String islandId = island.islandId();
        element.addEventListener(UIEvents.MOUSE_ENTER, event -> {
            host.hoveredIslandId = islandId;
        }, true);
        element.addEventListener(UIEvents.MOUSE_LEAVE, event -> {
            if (islandId.equals(host.hoveredIslandId)) {
                host.hoveredIslandId = null;
            }
        }, true);
    }

    UIElement islandCarriedBadge(SlotAtlasGraphView atlas, SlotWorkspaceViewModel.AtlasIsland island) {
        // Positioned on the title-bar strip above the island panel so it
        // doesn't overlap the first row of item cards. Uses world-unit
        // absolute positioning against atlas host.content like every other
        // atlas-level element.
        dev.imagio.slot.atlas.lod.AtlasLayoutResult.IslandPlacement place = host.islandPlacementFor(island);
        Button badge = button(island.carriedCount() + "●", true, ACTIVE_HOTBAR);
        badge.layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(place.x() + 2)
                .top(place.y() - 14)
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
        dev.imagio.slot.atlas.lod.AtlasLayoutResult.IslandPlacement place = host.islandPlacementFor(island);
        Button header = button(island.label(), true, island.color());
        header.layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(place.x())
                .top(place.y() - 16)
                .width(place.width())
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
        installIslandHoverPaint(header, island.islandId());

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
            // Re-resolve every tick — when the layout reflows (search,
            // kit activation, ordinal drag-drop) the island bounds shift.
            dev.imagio.slot.atlas.lod.AtlasLayoutResult.IslandPlacement currentPlace = host.islandPlacementFor(island);
            float islandScreenWidth = currentPlace.width() * scale;
            float requestedFontPx = Math.min(12f, islandScreenWidth * 0.13f);
            float screenFontPx = headerBreakpointFontPx(Math.max(7f, requestedFontPx));
            float screenHeaderHeight = screenFontPx + 3f;
            // Floor the world height at the carried-count badge's world
            // size (12 world units plus a 2-unit margin = 14) so the badge
            // never overflows the header background. Without this, at
            // scale > ~1 the screen-fixed header shrinks in world space
            // below the badge's world size and the counter visibly
            // escapes its backdrop.
            //
            // Ceiling at SlotWorkspaceAtlasLayout.ISLAND_HEADER_RESERVE
            // (24 wu) so zooming out doesn't grow the header without
            // bound and crash it into neighbours above. AtlasLayout.packAtlas
            // reserves the same band when de-overlapping islands —
            // both constants must stay in sync.
            float screenScaledWorldHeader = screenHeaderHeight / Math.max(0.0001f, scale);
            float worldHeaderHeight = Math.max(14f,
                    Math.min(SlotWorkspaceAtlasLayout.ISLAND_HEADER_RESERVE, screenScaledWorldHeader));
            float screenGap = 2f;
            float worldGap = screenGap / Math.max(0.0001f, scale);

            // Derive worldFontPx FROM the (clamped) strip height — when
            // the screen-scaled height is capped by the world ceiling,
            // the font has to shrink with it or the text overflows the
            // strip vertically. Reserve 3 wu for the strip's internal
            // padding (matching the +3 in screenHeaderHeight above).
            float worldFontPx = Math.max(1f, worldHeaderHeight - 3f);

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
                    .left(currentPlace.x())
                    .top(Math.round(currentPlace.y() - worldHeaderHeight - worldGap))
                    .width(currentPlace.width())
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

    /**
     * Aggregate per-area item presence for an island. Sums atlas-item
     * presence counts across every item homed to {@code island}, keyed
     * by area label. Returns null/empty when the island has no presence.
     */
    UIElement islandPresenceStrip(SlotWorkspaceViewModel.AtlasIsland island) {
        java.util.LinkedHashMap<String, Integer> totalsByArea = new java.util.LinkedHashMap<>();
        for (SlotWorkspaceViewModel.AtlasItem item : host.viewModel.atlasItems()) {
            if (!island.islandId().equals(item.islandId())) {
                continue;
            }
            for (SlotWorkspaceViewModel.ChestPresenceEntry entry : item.presence()) {
                if (entry.areaLabel() == null || entry.areaLabel().isBlank()) {
                    continue;
                }
                totalsByArea.merge(entry.areaLabel(), entry.count(), Integer::sum);
            }
        }
        if (totalsByArea.isEmpty()) {
            return null;
        }
        java.util.List<java.util.Map.Entry<String, Integer>> ordered = new java.util.ArrayList<>(totalsByArea.entrySet());
        ordered.sort(java.util.Map.Entry.<String, Integer>comparingByValue().reversed());
        StringBuilder sb = new StringBuilder();
        int shown = 0;
        for (java.util.Map.Entry<String, Integer> entry : ordered) {
            if (shown > 0) {
                sb.append("  ");
            }
            sb.append(entry.getValue()).append(" ").append(entry.getKey());
            shown++;
            if (shown >= 3) {
                break;
            }
        }
        dev.imagio.slot.atlas.lod.AtlasLayoutResult.IslandPlacement place = host.islandPlacementFor(island);
        Label strip = label(sb.toString(), MUTED);
        strip.layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(place.x())
                .top(place.y() + place.height() + 1)
                .width(place.width())
                .height(8));
        strip.textStyle(style -> style
                .textColor(MUTED)
                .textShadow(false)
                .fontSize(6)
                .textAlignHorizontal(Horizontal.LEFT)
                .textAlignVertical(Vertical.TOP));
        strip.style(style -> style.zIndex(2));
        strip.setAllowHitTest(false);
        return strip;
    }

    /**
     * Per-frame paint flip: when {@code host.hoveredStorageId} matches a
     * chest linked to {@code islandId}, draw an accent overlay on
     * {@code element}. Cheap enough to attach to every island title bar
     * (it's a single equality check + a single overlay setter call when
     * the state actually flips).
     */
    void installIslandHoverPaint(UIElement element, String islandId) {
        boolean[] lastLit = {false};
        element.addEventListener(UIEvents.TICK, event -> {
            boolean lit = isStorageLinkedToIsland(host.hoveredStorageId, islandId);
            if (lit == lastLit[0]) {
                return;
            }
            lastLit[0] = lit;
            element.style(style -> style.overlayTexture(lit ? rect(HOVER_ACCENT_OVERLAY) : IGuiTexture.EMPTY));
        });
    }

    boolean isStorageLinkedToIsland(String storageId, String islandId) {
        if (storageId == null || islandId == null) {
            return false;
        }
        SlotWorkspaceViewModel.ClaimedChestTile tile = host.viewModel.claimedChestTile(storageId);
        return tile != null && tile.linkedIslandIds().contains(islandId);
    }

    /**
     * Strip-flow chest card: header, item grid, link/take buttons, laid
     * out as a flex child of the storage panel's card flow. Sized to the
     * tile's authored width/height; inner elements are absolute-
     * positioned relative to this panel.
     */
    UIElement chestTilePanelInFlow(SlotWorkspaceViewModel.ClaimedChestTile tile) {
        int fill = chestTileFill(tile);
        int textColor = tile.proximate() ? TEXT : MUTED;
        int cellSize = SlotWorkspaceAtlasLayout.CHEST_TILE_CELL;
        int cols = SlotWorkspaceAtlasLayout.CHEST_TILE_COLUMNS;
        int padding = SlotWorkspaceAtlasLayout.CHEST_TILE_PADDING;
        int headerHeight = SlotWorkspaceAtlasLayout.CHEST_TILE_HEADER_HEIGHT;

        UIElement panel = panel(fill).layout(layout -> layout
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
        java.util.List<Integer> contentIndices = tile.contentSlotIndices();
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
                host.drag.installChestStackDragSource(cell, storageId, chestSlotIndex, cellStack, tile.label());
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

        host.drag.installChestTileDragSource(panel, tile);
        installChestTileDropTarget(panel, tile);
        installChestCardHover(panel, tile);
        return panel;
    }

    /**
     * Track {@code host.hoveredStorageId} on enter/leave + paint accent
     * when this tile's linked-from island is hovered.
     */
    void installChestCardHover(UIElement panel, SlotWorkspaceViewModel.ClaimedChestTile tile) {
        String storageId = tile.storageId();
        panel.addEventListener(UIEvents.MOUSE_ENTER, event -> host.hoveredStorageId = storageId, true);
        panel.addEventListener(UIEvents.MOUSE_LEAVE, event -> {
            if (storageId.equals(host.hoveredStorageId)) {
                host.hoveredStorageId = null;
            }
        }, true);
        boolean[] lastLit = {false};
        panel.addEventListener(UIEvents.TICK, event -> {
            boolean lit = host.hoveredIslandId != null
                    && tile.linkedIslandIds().contains(host.hoveredIslandId);
            if (lit == lastLit[0]) {
                return;
            }
            lastLit[0] = lit;
            panel.style(style -> style.overlayTexture(lit ? rect(HOVER_ACCENT_OVERLAY) : IGuiTexture.EMPTY));
        });
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

    /**
     * Resolves the fill color for a chest tile. Tiles inherit their
     * area's color so the player can see at a glance which base a chest
     * belongs to.
     */
    private int chestTileFill(SlotWorkspaceViewModel.ClaimedChestTile tile) {
        SlotWorkspaceViewModel.StorageAreaSnapshot area = host.viewModel.storageArea(tile.areaId());
        if (area == null) {
            return tile.proximate() ? STORAGE_TILE_FILL : STORAGE_TILE_FILL_DIM;
        }
        int areaColor = area.color();
        if (tile.proximate()) {
            return areaColor;
        }
        // Match the legacy proximity-dim ratio: alpha drops from ~0xD0 to
        // ~0x60 while RGB is preserved.
        return (areaColor & 0x00FFFFFF) | 0x60000000;
    }

    UIElement chestTileCell(ItemStack stack, boolean proximate, int cellSize) {
        int chromeColor = proximate ? STORAGE_TILE_CELL_FILL : STORAGE_TILE_CELL_FILL_DIM;
        return itemSlotCard(stack, cellSize, chromeColor, proximate);
    }

}
