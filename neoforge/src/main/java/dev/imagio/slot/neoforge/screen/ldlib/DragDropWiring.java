package dev.imagio.slot.neoforge.screen.ldlib;

import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.*;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceUi.*;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.event.HoverTooltips;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceAtlasLayout;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.KitBringDrag;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.KitSlotDrag;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.AtlasItemDrag;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.ChestStackDrag;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.ChestTileDrag;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.HotbarSlotDrag;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.IslandDrag;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.StorageZoneDrag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

final class DragDropWiring {
    private final SlotWorkspaceUiController host;

    DragDropWiring(SlotWorkspaceUiController host) {
        this.host = host;
    }

    void installAtlasItemDragSource(UIElement source, SlotWorkspaceViewModel.AtlasItem item) {
        source.addEventListener(UIEvents.MOUSE_LEAVE, event -> {
            if (!source.isMouseDown(0) || isDragging(source)) {
                return;
            }
            source.startDrag(
                    new AtlasItemDrag(item.identity(), item.displayStack().copy(), item.islandId()),
                    dragTexture(item.displayStack())
            ).setDragTexture(-10, -10, 20, 20);
            host.localStatus.set("dragging " + item.name());
        }, true);
        source.addEventListener(UIEvents.DRAG_END, event -> handleDragEnd(event));
    }

    void installIslandDragSource(
            UIElement source,
            SlotAtlasGraphView atlas,
            SlotWorkspaceViewModel.AtlasIsland island
    ) {
        if (island.kind() != VisualAtlasIslandKind.PLAYER) {
            return;
        }
        int[] clickWorldX = {Integer.MIN_VALUE};
        int[] clickWorldY = {Integer.MIN_VALUE};
        source.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button != 0) {
                return;
            }
            clickWorldX[0] = atlas.worldX(event.x);
            clickWorldY[0] = atlas.worldY(event.y);
        }, true);
        source.addEventListener(UIEvents.MOUSE_UP, event -> {
            clickWorldX[0] = Integer.MIN_VALUE;
            clickWorldY[0] = Integer.MIN_VALUE;
        }, true);
        source.addEventListener(UIEvents.MOUSE_MOVE, event -> {
            if (clickWorldX[0] == Integer.MIN_VALUE) {
                return;
            }
            if (!source.isMouseDown(0) || isDragging(source)) {
                return;
            }
            float scale = atlas.getScale();
            float screenDx = (atlas.worldX(event.x) - clickWorldX[0]) * scale;
            float screenDy = (atlas.worldY(event.y) - clickWorldY[0]) * scale;
            if (screenDx * screenDx + screenDy * screenDy < DRAG_START_THRESHOLD_PX * DRAG_START_THRESHOLD_PX) {
                return;
            }
            int grabOffsetX = Math.max(0, Math.min(island.width(), clickWorldX[0] - island.x()));
            int grabOffsetY = Math.max(0, Math.min(island.height(), clickWorldY[0] - island.y()));
            // Render the ghost at the actual island screen size (no minimum
            // clamp — small islands got spuriously wide ghosts). Cap at a
            // reasonable maximum so huge islands don't occlude the viewport.
            int actualWidthPx = atlas.screenPixelsForWorldUnits(island.width());
            int actualHeightPx = atlas.screenPixelsForWorldUnits(island.height());
            float dragScale = Math.min(1f, Math.min(260f / Math.max(1, actualWidthPx), 180f / Math.max(1, actualHeightPx)));
            int dragWidthPx = Math.max(1, Math.round(actualWidthPx * dragScale));
            int dragHeightPx = Math.max(1, Math.round(actualHeightPx * dragScale));
            // The island title bar lives above the island rect; include a
            // proportional strip in the ghost so it represents the whole
            // island shape the host.player sees.
            int headerHeightPx = Math.max(6, Math.round(14f * dragScale));
            int dragOffsetX = Math.round(grabOffsetX * scale * dragScale);
            int dragOffsetY = Math.round(grabOffsetY * scale * dragScale);
            source.startDrag(
                    new IslandDrag(island.islandId(), grabOffsetX, grabOffsetY),
                    rect((island.color() & 0x00FFFFFF) | 0x5A000000)
            ).setDragTexture(
                    -dragOffsetX,
                    -(dragOffsetY + headerHeightPx),
                    dragWidthPx,
                    dragHeightPx + headerHeightPx);
            host.localStatus.set("dragging island " + island.label());
        }, true);
        source.addEventListener(UIEvents.DRAG_END, event -> handleDragEnd(event));
    }

    void installAtlasHoverTooltip(Button button, SlotWorkspaceViewModel.AtlasItem item) {
        button.addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
            if (item == null || item.displayStack().isEmpty()) {
                return;
            }
            event.hoverTooltips = new HoverTooltips(
                    WorkspaceFormat.atlasTooltipLines(item),
                    item.displayStack().getTooltipImage().orElse(null),
                    null,
                    item.displayStack()
            );
        });
    }

    boolean hotbarDragHasHome(HotbarSlotDrag drag) {
        if (drag == null || drag.displayStack() == null || drag.displayStack().isEmpty()) {
            return false;
        }
        SlotWorkspaceViewModel.IdentityRef identity = SlotWorkspaceViewModel.IdentityRef.from(
                dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(drag.displayStack()));
        SlotWorkspaceViewModel.AtlasItem atlasItem = host.islandChest.atlasItemInIslandLayer(identity);
        if (atlasItem == null) {
            return false;
        }
        String islandId = atlasItem.islandId();
        return islandId != null
                && !islandId.isBlank()
                && !SlotWorkspaceAtlasLayout.ISLAND_TRIAGE.equals(islandId);
    }

    void installHotbarDragSource(UIElement source, SlotWorkspaceViewModel.HotbarSlot slot) {
        if (!slot.occupied()) {
            return;
        }
        source.addEventListener(UIEvents.MOUSE_LEAVE, event -> {
            if (!source.isMouseDown(0) || isDragging(source)) {
                return;
            }
            source.startDrag(
                    new HotbarSlotDrag(slot.hotbarIndex(), slot.displayStack().copy()),
                    dragTexture(slot.displayStack())
            ).setDragTexture(-10, -10, 20, 20);
            host.localStatus.set("dragging hotbar " + (slot.hotbarIndex() + 1));
        }, true);
        source.addEventListener(UIEvents.DRAG_END, event -> handleDragEnd(event));
    }

    void installHotbarDropTarget(Button target, SlotWorkspaceViewModel.HotbarSlot slot) {
        target.addEventListener(UIEvents.DRAG_ENTER, event -> updateHotbarDropOverlay(target, slot, event), true);
        target.addEventListener(UIEvents.DRAG_UPDATE, event -> updateHotbarDropOverlay(target, slot, event));
        target.addEventListener(UIEvents.DRAG_LEAVE, event -> clearDropOverlay(target), true);
        target.addEventListener(UIEvents.DRAG_PERFORM, event -> {
            clearDropOverlay(target);
            HotbarSlotDrag hotbarDrag = hotbarSlotDrag(event);
            if (hotbarDrag != null) {
                // Drag between two hotbar slots = swap. ASSIGN against two host.player-bound
                // quick-access slots swaps their contents atomically.
                if (hotbarDrag.hotbarIndex() != slot.hotbarIndex()) {
                    host.sendTransfer(
                            SlotWorkspaceUiSession.TARGET_HOTBAR_SLOT, hotbarDrag.hotbarIndex(),
                            SlotWorkspaceUiSession.TARGET_HOTBAR_SLOT, slot.hotbarIndex());
                }
                event.stopPropagation();
                return;
            }
            AtlasItemDrag drag = atlasItemDrag(event);
            if (drag == null) {
                // No recognized drag type: still stop propagation so the drop doesn't
                // fall through to an atlas card positioned behind the belt.
                event.stopPropagation();
                return;
            }
            SlotWorkspaceViewModel.AtlasItem item = host.viewModel.atlasItem(drag.identity());
            if (item == null) {
                host.localStatus.set("dragged item is no longer visible");
                host.rebuild();
                event.stopPropagation();
                return;
            }
            if (!item.carried()) {
                host.localStatus.set("can't move " + item.name() + " to hotbar — none carried");
                host.rebuild();
                event.stopPropagation();
                return;
            }
            host.sendAssignToHotbarSlot(item, slot.hotbarIndex());
            event.stopPropagation();
        });
    }

    void installAtlasBackgroundDropTarget(SlotAtlasGraphView atlas) {
        atlas.addEventListener(UIEvents.DRAG_ENTER, event -> updateAtlasBackgroundDropOverlay(atlas, event), true);
        atlas.addEventListener(UIEvents.DRAG_UPDATE, event -> updateAtlasBackgroundDropOverlay(atlas, event));
        atlas.addEventListener(UIEvents.DRAG_LEAVE, event -> clearDropOverlay(atlas), true);
        atlas.addEventListener(UIEvents.DRAG_PERFORM, event -> {
            if (!isDirectDragTarget(event, atlas)) {
                return;
            }
            clearDropOverlay(atlas);
            AtlasItemDrag atlasItem = atlasItemDrag(event);
            if (atlasItem != null) {
                int worldX = atlas.worldX(event.x);
                int worldY = atlas.worldY(event.y);
                if (wasDraggedFromTriage(atlasItem)) {
                    SlotWorkspaceViewModel.AtlasItem item = host.viewModel.atlasItem(atlasItem.identity());
                    if (item == null) {
                        host.localStatus.set("dragged item is no longer visible");
                        host.rebuild();
                        event.stopPropagation();
                        return;
                    }
                    host.menu.beginCreateIsland(item, worldX, worldY);
                } else {
                    host.sendAssignHome(
                            atlasItem.identity(),
                            SlotWorkspaceAtlasLayout.ISLAND_TRIAGE,
                            worldX,
                            worldY
                    );
                }
                event.stopPropagation();
                return;
            }
            IslandDrag islandDrag = islandDrag(event);
            if (islandDrag != null) {
                host.sendMoveIsland(
                        islandDrag.islandId(),
                        atlas.worldX(event.x) - islandDrag.grabOffsetX(),
                        atlas.worldY(event.y) - islandDrag.grabOffsetY()
                );
                event.stopPropagation();
                return;
            }
            ChestTileDrag chestDrag = chestTileDrag(event);
            if (chestDrag != null) {
                host.sendMoveChest(
                        chestDrag.storageId(),
                        atlas.worldX(event.x) - chestDrag.grabOffsetX(),
                        atlas.worldY(event.y) - chestDrag.grabOffsetY()
                );
                event.stopPropagation();
                return;
            }
            StorageZoneDrag zoneDrag = storageZoneDrag(event);
            if (zoneDrag != null) {
                int newLeft = atlas.worldX(event.x) - zoneDrag.grabOffsetX();
                int newTop = atlas.worldY(event.y) - zoneDrag.grabOffsetY();
                host.sendMoveStorageZone(newLeft - zoneDrag.originX(), newTop - zoneDrag.originY());
                event.stopPropagation();
                return;
            }
            HotbarSlotDrag hotbarItem = hotbarSlotDrag(event);
            if (hotbarItem != null) {
                if (hotbarDragHasHome(hotbarItem)) {
                    host.sendReturnHotbarToHome(hotbarItem.hotbarIndex());
                } else {
                    host.sendMoveHotbarToAtlas(
                            hotbarItem.hotbarIndex(),
                            SlotWorkspaceAtlasLayout.ISLAND_TRIAGE,
                            atlas.worldX(event.x),
                            atlas.worldY(event.y)
                    );
                }
                event.stopPropagation();
                return;
            }
            KitSlotDrag kitSlot = host.kit.kitSlotDrag(event);
            if (kitSlot != null) {
                host.sendSetKitSlotIdentity(kitSlot.kitId(), kitSlot.pageIndex(), kitSlot.slotIndex(), null);
                event.stopPropagation();
                return;
            }
            KitBringDrag kitBring = host.kit.kitBringDrag(event);
            if (kitBring != null) {
                host.sendRemoveKitBring(kitBring.kitId(), kitBring.identity());
                event.stopPropagation();
            }
        });
    }

    boolean wasDraggedFromTriage(AtlasItemDrag drag) {
        if (drag == null) {
            return false;
        }
        String originIslandId = drag.originIslandId();
        if (SlotWorkspaceAtlasLayout.ISLAND_TRIAGE.equals(originIslandId)) {
            return true;
        }
        SlotWorkspaceViewModel.AtlasIsland origin = host.viewModel.island(originIslandId);
        return origin != null && origin.kind() == VisualAtlasIslandKind.TRIAGE;
    }

    void installAtlasCanvasDropTarget(UIElement target, SlotAtlasGraphView atlas) {
        target.addEventListener(UIEvents.DRAG_PERFORM, event -> {
            IslandDrag islandDrag = islandDrag(event);
            if (islandDrag != null) {
                if (event.target == atlas) {
                    return;
                }
                host.sendMoveIsland(
                        islandDrag.islandId(),
                        atlas.worldX(event.x) - islandDrag.grabOffsetX(),
                        atlas.worldY(event.y) - islandDrag.grabOffsetY()
                );
                event.stopPropagation();
                return;
            }
            ChestTileDrag chestDrag = chestTileDrag(event);
            if (chestDrag != null) {
                if (event.target == atlas) {
                    return;
                }
                host.sendMoveChest(
                        chestDrag.storageId(),
                        atlas.worldX(event.x) - chestDrag.grabOffsetX(),
                        atlas.worldY(event.y) - chestDrag.grabOffsetY()
                );
                event.stopPropagation();
                return;
            }
            StorageZoneDrag zoneDrag = storageZoneDrag(event);
            if (zoneDrag != null) {
                if (event.target == atlas) {
                    return;
                }
                int newLeft = atlas.worldX(event.x) - zoneDrag.grabOffsetX();
                int newTop = atlas.worldY(event.y) - zoneDrag.grabOffsetY();
                host.sendMoveStorageZone(newLeft - zoneDrag.originX(), newTop - zoneDrag.originY());
                event.stopPropagation();
            }
        });
    }

    void installIslandDropTarget(
            UIElement target,
            UIElement highlightTarget,
            SlotAtlasGraphView atlas,
            SlotWorkspaceViewModel.AtlasIsland island
    ) {
        target.addEventListener(UIEvents.DRAG_ENTER, event -> updateIslandDropOverlay(highlightTarget, island, event), true);
        target.addEventListener(UIEvents.DRAG_UPDATE, event -> updateIslandDropOverlay(highlightTarget, island, event));
        target.addEventListener(UIEvents.DRAG_LEAVE, event -> clearDropOverlay(highlightTarget), true);
        target.addEventListener(UIEvents.DRAG_PERFORM, event -> {
            clearDropOverlay(highlightTarget);
            IslandDrag islandDrag = islandDrag(event);
            if (islandDrag != null) {
                host.sendMoveIsland(
                        islandDrag.islandId(),
                        atlas.worldX(event.x) - islandDrag.grabOffsetX(),
                        atlas.worldY(event.y) - islandDrag.grabOffsetY()
                );
                event.stopPropagation();
                return;
            }
            AtlasItemDrag atlasItem = atlasItemDrag(event);
            if (atlasItem != null) {
                host.sendAssignHome(
                        atlasItem.identity(),
                        island.islandId(),
                        atlas.worldX(event.x),
                        atlas.worldY(event.y)
                );
                event.stopPropagation();
                return;
            }
            HotbarSlotDrag hotbarItem = hotbarSlotDrag(event);
            if (hotbarItem != null) {
                if (hotbarDragHasHome(hotbarItem)) {
                    host.sendReturnHotbarToHome(hotbarItem.hotbarIndex());
                } else {
                    host.sendMoveHotbarToAtlas(
                            hotbarItem.hotbarIndex(),
                            island.islandId(),
                            atlas.worldX(event.x),
                            atlas.worldY(event.y)
                    );
                }
                event.stopPropagation();
                return;
            }
            ChestStackDrag chestDrag = chestStackDrag(event);
            if (chestDrag != null) {
                // Pure metadata assign — the item stays in the chest, we
                // only record the island as the visual home for this
                // identity. Mark the drag as consumed so the chest cell's
                // DRAG_END skips its default take-into-inventory path.
                SlotWorkspaceViewModel.IdentityRef identity = SlotWorkspaceViewModel.IdentityRef.from(
                        dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(chestDrag.displayStack()));
                host.sendAssignHome(
                        identity,
                        island.islandId(),
                        atlas.worldX(event.x),
                        atlas.worldY(event.y)
                );
                host.chestDragDropConsumed = true;
                event.stopPropagation();
            }
        });
    }

    void installViewportPanSurface(UIElement target, SlotAtlasGraphView atlas) {
        target.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.target != target) {
                return;
            }
            if (atlas.beginViewportPan(event)) {
                event.stopPropagation();
            }
        });
    }

    void updateHotbarDropOverlay(Button target, SlotWorkspaceViewModel.HotbarSlot slot, UIEvent event) {
        HotbarSlotDrag hotbarDrag = hotbarSlotDrag(event);
        if (hotbarDrag != null) {
            if (hotbarDrag.hotbarIndex() == slot.hotbarIndex()) {
                clearDropOverlay(target);
            } else {
                updateGenericDropOverlay(target, true, ACCENT);
            }
            return;
        }
        AtlasItemDrag drag = atlasItemDrag(event);
        if (drag == null) {
            clearDropOverlay(target);
            return;
        }
        SlotWorkspaceViewModel.AtlasItem item = host.viewModel.atlasItem(drag.identity());
        boolean carried = item != null && item.carried();
        updateGenericDropOverlay(target, carried, carried ? (slot.occupied() ? ACTIVE_HOTBAR : ACCENT) : WARNING);
    }

    void updateIslandDropOverlay(UIElement highlightTarget, SlotWorkspaceViewModel.AtlasIsland island, UIEvent event) {
        boolean acceptable = atlasItemDrag(event) != null || hotbarSlotDrag(event) != null || islandDrag(event) != null;
        updateGenericDropOverlay(
                highlightTarget,
                acceptable,
                islandDrag(event) != null
                        ? SELECTED
                        : island.kind() == VisualAtlasIslandKind.TRIAGE ? WARNING : ACCENT
        );
    }

    void updateAtlasBackgroundDropOverlay(SlotAtlasGraphView atlas, UIEvent event) {
        if (!isDirectDragTarget(event, atlas)) {
            clearDropOverlay(atlas);
            return;
        }
        IslandDrag islandDrag = islandDrag(event);
        ChestTileDrag chestDrag = chestTileDrag(event);
        boolean acceptable = atlasItemDrag(event) != null
                || hotbarSlotDrag(event) != null
                || islandDrag != null
                || chestDrag != null;
        int color = islandDrag != null || chestDrag != null ? SELECTED : WARNING;
        updateGenericDropOverlay(atlas, acceptable, color);
    }

    void updateGenericDropOverlay(UIElement target, boolean active) {
        updateGenericDropOverlay(target, active, ACCENT);
    }

    void updateGenericDropOverlay(UIElement target, boolean active, int color) {
        target.style(style -> style.overlayTexture(active ? rect((color & 0x00FFFFFF) | 0x44000000) : IGuiTexture.EMPTY));
    }

    void clearDropOverlay(UIElement target) {
        target.style(style -> style.overlayTexture(IGuiTexture.EMPTY));
    }

    void handleDragEnd(UIEvent event) {
        if (event.relatedTarget == null) {
            host.localStatus.set("drag cancelled");
        }
    }

    boolean isDragging(UIElement element) {
        return element.getModularUI() != null && element.getModularUI().getDragHandler().isDragging();
    }

    AtlasItemDrag atlasItemDrag(UIEvent event) {
        Object payload = event == null || event.dragHandler == null ? null : event.dragHandler.getDraggingObject();
        return payload instanceof AtlasItemDrag atlasItemDrag ? atlasItemDrag : null;
    }

    HotbarSlotDrag hotbarSlotDrag(UIEvent event) {
        Object payload = event == null || event.dragHandler == null ? null : event.dragHandler.getDraggingObject();
        return payload instanceof HotbarSlotDrag hotbarSlotDrag ? hotbarSlotDrag : null;
    }

    IslandDrag islandDrag(UIEvent event) {
        Object payload = event == null || event.dragHandler == null ? null : event.dragHandler.getDraggingObject();
        return payload instanceof IslandDrag islandDrag ? islandDrag : null;
    }

    ChestTileDrag chestTileDrag(UIEvent event) {
        Object payload = event == null || event.dragHandler == null ? null : event.dragHandler.getDraggingObject();
        return payload instanceof ChestTileDrag chestTileDrag ? chestTileDrag : null;
    }

    ChestStackDrag chestStackDrag(UIEvent event) {
        Object payload = event == null || event.dragHandler == null ? null : event.dragHandler.getDraggingObject();
        return payload instanceof ChestStackDrag chestStackDrag ? chestStackDrag : null;
    }

    StorageZoneDrag storageZoneDrag(UIEvent event) {
        Object payload = event == null || event.dragHandler == null ? null : event.dragHandler.getDraggingObject();
        return payload instanceof StorageZoneDrag storageZoneDrag ? storageZoneDrag : null;
    }

    boolean isDirectDragTarget(UIEvent event, UIElement element) {
        return event != null && event.target == element;
    }

    IGuiTexture dragTexture(ItemStack stack) {
        return new ItemStackTexture(stack == null ? ItemStack.EMPTY : stack.copy());
    }

}
