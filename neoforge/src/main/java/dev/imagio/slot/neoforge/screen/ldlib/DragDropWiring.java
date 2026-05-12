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
import dev.imagio.slot.atlas.lod.SectionOrdinal;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceAtlasLayout;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.AtlasItemDrag;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.ChestStackDrag;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.ChestTileDrag;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.HotbarSlotDrag;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.IslandDrag;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.KitBringDrag;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.KitSlotDrag;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import net.minecraft.world.item.ItemStack;

/**
 * Drag-drop wiring for the sectioned list-view wall. Replaces the prior
 * world-coord canvas drop targets with section-aware grid drops:
 *
 * <ul>
 *   <li>Card drag source — pulls an atlas item from anywhere on the
 *       wall (or hotbar) and drops it on another section to re-home,
 *       or on a card within the same section to reorder.</li>
 *   <li>Section drop target — installed on the section's flow grid;
 *       resolves the drop to an insert ordinal by walking grid
 *       children whose center sits below-or-right of the drop coord
 *       in flow order.</li>
 *   <li>Section header drop target — header drops fall through to
 *       "append to end of section".</li>
 * </ul>
 *
 * <p>Pan/zoom drag (drag-island, drag-viewport-pan) is gone. Section
 * reorder happens via the TOC tab in Phase 2 (or via section-header
 * drag if implemented later).
 */
final class DragDropWiring {
    private final SlotWorkspaceUiController host;

    DragDropWiring(SlotWorkspaceUiController host) {
        this.host = host;
    }

    void installAtlasItemDragSource(UIElement source, SlotWorkspaceViewModel.AtlasItem item) {
        source.addEventListener(UIEvents.MOUSE_LEAVE, event -> {
            if (!mouseIsHeldOnSource(source) || isDragging(source)) {
                return;
            }
            // Drag = re-home gesture. Mutually exclusive with carrying
            // a real menu cursor (clicks while carrying go through the
            // universal click table instead).
            if (WorkspaceCursorState.isCarrying()) {
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

    /**
     * True iff mouse button 0 is currently held AND the mouse-down
     * happened on this source (or one of its descendants).
     *
     * <p>{@link UIElement#isMouseDown(int)} only checks
     * {@code modularUI.lastMouseDownButton} — a single global flag —
     * so it returns true any time button 0 is held anywhere in the
     * widget tree, regardless of where the click started. That used to
     * make every drag source's {@code MOUSE_LEAVE} handler think a
     * drag was in progress whenever the player click-and-held one
     * element and then moved through another, picking up a phantom
     * drag on whatever element they happened to leave first. Pairing
     * the global "is the button held" check with the per-element
     * "did the click start on me" check ({@code lastMouseDownElement}
     * walks up to {@code source} or below) restores the intended
     * semantics: drag only begins when the player drags out of the
     * element they originally clicked.
     */
    static boolean mouseIsHeldOnSource(UIElement source) {
        if (!source.isMouseDown(0)) {
            return false;
        }
        var modularUI = source.getModularUI();
        if (modularUI == null) {
            return false;
        }
        UIElement clicked = modularUI.getLastMouseDownElement();
        while (clicked != null) {
            if (clicked == source) {
                return true;
            }
            clicked = clicked.getParent();
        }
        return false;
    }

    void installAtlasHoverTooltip(Button button, SlotWorkspaceViewModel.AtlasItem item) {
        button.addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
            if (item == null || item.displayStack().isEmpty()) {
                return;
            }
            boolean goalTooltipOnly = host.goalTabActive() && host.goalSuppressVanillaTooltip(item);
            ItemStack tooltipStack = goalTooltipOnly ? ItemStack.EMPTY : item.displayStack();
            event.hoverTooltips = new HoverTooltips(
                    goalTooltipOnly
                            ? host.goalTooltipLines(item)
                            : host.goalTabActive()
                            ? WorkspaceFormat.atlasTooltipLines(item, host.goalTooltipLines(item))
                            : WorkspaceFormat.atlasTooltipLines(item),
                    tooltipStack.isEmpty() ? null : tooltipStack.getTooltipImage().orElse(null),
                    null,
                    tooltipStack
            );
        });
    }

    boolean hotbarDragHasHome(HotbarSlotDrag drag) {
        if (drag == null || drag.displayStack() == null || drag.displayStack().isEmpty()) {
            return false;
        }
        SlotWorkspaceViewModel.IdentityRef identity = SlotWorkspaceViewModel.IdentityRef.from(
                dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(drag.displayStack()));
        SlotWorkspaceViewModel.AtlasItem atlasItem = atlasItemFor(identity);
        if (atlasItem == null) {
            return false;
        }
        String islandId = atlasItem.islandId();
        return islandId != null
                && !islandId.isBlank()
                && !SlotWorkspaceAtlasLayout.ISLAND_TRIAGE.equals(islandId);
    }

    /** Lookup helper for hotbar→home checks: finds the atlas item with this identity. */
    private SlotWorkspaceViewModel.AtlasItem atlasItemFor(SlotWorkspaceViewModel.IdentityRef identity) {
        if (identity == null) {
            return null;
        }
        for (SlotWorkspaceViewModel.AtlasItem candidate : host.currentAtlasItems()) {
            if (candidate.identity().equals(identity)) {
                return candidate;
            }
        }
        return null;
    }

    void installHotbarDragSource(UIElement source, SlotWorkspaceViewModel.HotbarSlot slot) {
        if (!slot.occupied()) {
            return;
        }
        source.addEventListener(UIEvents.MOUSE_LEAVE, event -> {
            if (!mouseIsHeldOnSource(source) || isDragging(source)) {
                return;
            }
            if (WorkspaceCursorState.isCarrying()) {
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
                if (hotbarDrag.hotbarIndex() != slot.hotbarIndex()) {
                    host.rpc.sendTransfer(
                            SlotWorkspaceUiSession.TARGET_HOTBAR_SLOT, hotbarDrag.hotbarIndex(),
                            SlotWorkspaceUiSession.TARGET_HOTBAR_SLOT, slot.hotbarIndex());
                }
                event.stopPropagation();
                return;
            }
            AtlasItemDrag drag = atlasItemDrag(event);
            if (drag == null) {
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
            host.rpc.sendAssignToHotbarSlot(item, slot.hotbarIndex());
            event.stopPropagation();
        });
    }

    /**
     * Section grid drop target. The grid's children are atlas cards
     * laid out in flow order; a drop's screen coordinate maps back to
     * an insertion ordinal by walking children left-to-right,
     * top-to-bottom and stopping at the first card whose center sits
     * below-or-right of the drop coord.
     */
    void installSectionDropTarget(UIElement grid, SlotWorkspaceViewModel.AtlasIsland island) {
        if (island.kind() == VisualAtlasIslandKind.TRIAGE) {
            return;
        }
        grid.addEventListener(UIEvents.DRAG_ENTER, event -> updateSectionDropOverlay(grid, island, event), true);
        grid.addEventListener(UIEvents.DRAG_UPDATE, event -> updateSectionDropOverlay(grid, island, event));
        grid.addEventListener(UIEvents.DRAG_LEAVE, event -> clearDropOverlay(grid), true);
        grid.addEventListener(UIEvents.DRAG_PERFORM, event -> {
            clearDropOverlay(grid);
            AtlasItemDrag atlasItem = atlasItemDrag(event);
            if (atlasItem != null) {
                Integer ordinal = resolveSectionDropOrdinal(grid, event);
                host.rpc.sendAssignHome(atlasItem.identity(), island.islandId(), ordinal);
                event.stopPropagation();
                return;
            }
            HotbarSlotDrag hotbarItem = hotbarSlotDrag(event);
            if (hotbarItem != null) {
                if (hotbarDragHasHome(hotbarItem)) {
                    host.rpc.sendReturnHotbarToHome(hotbarItem.hotbarIndex());
                } else {
                    Integer ordinal = resolveSectionDropOrdinal(grid, event);
                    host.rpc.sendMoveHotbarToAtlas(hotbarItem.hotbarIndex(), island.islandId(), ordinal);
                }
                event.stopPropagation();
                return;
            }
            ChestStackDrag chestDrag = chestStackDrag(event);
            if (chestDrag != null) {
                SlotWorkspaceViewModel.IdentityRef identity = SlotWorkspaceViewModel.IdentityRef.from(
                        dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(chestDrag.displayStack()));
                Integer ordinal = resolveSectionDropOrdinal(grid, event);
                host.rpc.sendAssignHome(identity, island.islandId(), ordinal);
                host.chestDragDropConsumed = true;
                event.stopPropagation();
            }
        });
    }

    /**
     * Section header drop target — accepts the same drag types as the
     * grid below it, but always appends (no ordinal resolution).
     */
    void installSectionHeaderDropTarget(Button header, SlotWorkspaceViewModel.AtlasIsland island) {
        if (island.kind() == VisualAtlasIslandKind.TRIAGE) {
            return;
        }
        header.addEventListener(UIEvents.DRAG_ENTER, event -> updateSectionDropOverlay(header, island, event), true);
        header.addEventListener(UIEvents.DRAG_UPDATE, event -> updateSectionDropOverlay(header, island, event));
        header.addEventListener(UIEvents.DRAG_LEAVE, event -> clearDropOverlay(header), true);
        header.addEventListener(UIEvents.DRAG_PERFORM, event -> {
            clearDropOverlay(header);
            // Header reorder: dropping one section header onto another
            // moves the source so it lands above-or-below the anchor
            // depending on which half the drop hit. Same math as the
            // legacy TOC row reorder.
            IslandDrag islandDrag = islandDrag(event);
            if (islandDrag != null) {
                if (!islandDrag.islandId().equals(island.islandId())) {
                    int sourceIndex = playerIslandIndexOf(islandDrag.islandId());
                    int anchorIndex = playerIslandIndexOf(island.islandId());
                    if (sourceIndex >= 0 && anchorIndex >= 0) {
                        float halfHeight = header.getSizeHeight() / 2f;
                        boolean upperHalf = (event.y - header.getPositionY()) < halfHeight;
                        int insertPosition = upperHalf ? anchorIndex : anchorIndex + 1;
                        int targetIndex = sourceIndex < insertPosition ? insertPosition - 1 : insertPosition;
                        if (targetIndex != sourceIndex) {
                            host.rpc.sendReorderIsland(islandDrag.islandId(), targetIndex);
                        }
                    }
                }
                event.stopPropagation();
                return;
            }
            AtlasItemDrag atlasItem = atlasItemDrag(event);
            if (atlasItem != null) {
                host.rpc.sendAssignHome(atlasItem.identity(), island.islandId(), null);
                event.stopPropagation();
                return;
            }
            HotbarSlotDrag hotbarItem = hotbarSlotDrag(event);
            if (hotbarItem != null) {
                if (hotbarDragHasHome(hotbarItem)) {
                    host.rpc.sendReturnHotbarToHome(hotbarItem.hotbarIndex());
                } else {
                    host.rpc.sendMoveHotbarToAtlas(hotbarItem.hotbarIndex(), island.islandId(), null);
                }
                event.stopPropagation();
            }
        });
    }

    /**
     * Drag a wall section header to start an {@link IslandDrag}. The
     * sliver-era TOC has no row-based reorder anymore, so the section
     * header is the canonical reorder grip.
     */
    void installSectionHeaderDragSource(Button header, SlotWorkspaceViewModel.AtlasIsland island) {
        if (island.kind() == VisualAtlasIslandKind.TRIAGE) {
            return;
        }
        header.addEventListener(UIEvents.MOUSE_LEAVE, event -> {
            if (!mouseIsHeldOnSource(header) || isDragging(header)) {
                return;
            }
            int swatchColor = island.color() == 0 ? ACCENT : island.color();
            header.startDrag(
                    new IslandDrag(island.islandId()),
                    rect(swatchColor)
            ).setDragTexture(-12, -2, 24, 4);
            host.localStatus.set("dragging " + island.label());
        }, true);
    }

    private int playerIslandIndexOf(String islandId) {
        int index = 0;
        for (SlotWorkspaceViewModel.AtlasIsland island : host.viewModel.islands()) {
            if (island.kind() == VisualAtlasIslandKind.TRIAGE) {
                continue;
            }
            if (island.islandId().equals(islandId)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    /**
     * Resolve the drop coord against the section's flex children. Walks
     * them in flow order; returns the index of the first child whose
     * center sits below-or-right of the drop. If no child matches the
     * drop is past every existing card and the result is "append" (the
     * children count). Strict row-major: above-the-row beats left-of-card.
     */
    private Integer resolveSectionDropOrdinal(UIElement grid, UIEvent event) {
        if (event == null) {
            return null;
        }
        float dropX = event.x;
        float dropY = event.y;
        int ordinal = 0;
        int childCount = grid.getChildren().size();
        for (int i = 0; i < childCount; i++) {
            UIElement child = grid.getChildren().get(i);
            float left = child.getPositionX();
            float top = child.getPositionY();
            float w = child.getSizeWidth();
            float h = child.getSizeHeight();
            if (w <= 0f || h <= 0f) {
                ordinal++;
                continue;
            }
            float centerX = left + w / 2f;
            float centerY = top + h / 2f;
            if (dropY < centerY - h / 2f) {
                return ordinal;
            }
            if (dropY < centerY + h / 2f && dropX < centerX) {
                return ordinal;
            }
            ordinal++;
        }
        return ordinal;
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

    void updateSectionDropOverlay(UIElement target, SlotWorkspaceViewModel.AtlasIsland island, UIEvent event) {
        IslandDrag islandDrag = islandDrag(event);
        if (islandDrag != null) {
            boolean acceptable = !islandDrag.islandId().equals(island.islandId());
            updateGenericDropOverlay(target, acceptable, ACCENT);
            return;
        }
        boolean acceptable = atlasItemDrag(event) != null
                || hotbarSlotDrag(event) != null
                || chestStackDrag(event) != null;
        updateGenericDropOverlay(target, acceptable, ACCENT);
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

    IGuiTexture dragTexture(ItemStack stack) {
        return new ItemStackTexture(stack == null ? ItemStack.EMPTY : stack.copy());
    }

    @SuppressWarnings("unused")
    void installChestStackDragSource(
            UIElement cell,
            String storageId,
            int chestSlotIndex,
            ItemStack stack,
            String chestLabel
    ) {
        int[] clickScreenX = {Integer.MIN_VALUE};
        int[] clickScreenY = {Integer.MIN_VALUE};
        cell.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button != 0) {
                return;
            }
            clickScreenX[0] = (int) event.x;
            clickScreenY[0] = (int) event.y;
        });
        cell.addEventListener(UIEvents.MOUSE_UP, event -> {
            clickScreenX[0] = Integer.MIN_VALUE;
            clickScreenY[0] = Integer.MIN_VALUE;
        });
        cell.addEventListener(UIEvents.MOUSE_MOVE, event -> {
            if (clickScreenX[0] == Integer.MIN_VALUE) {
                return;
            }
            if (!mouseIsHeldOnSource(cell) || isDragging(cell)) {
                return;
            }
            float dx = event.x - clickScreenX[0];
            float dy = event.y - clickScreenY[0];
            if (dx * dx + dy * dy < DRAG_START_THRESHOLD_PX * DRAG_START_THRESHOLD_PX) {
                return;
            }
            cell.startDrag(
                    new ChestStackDrag(storageId, chestSlotIndex, stack.copy()),
                    dragTexture(stack)
            ).setDragTexture(-10, -10, 20, 20);
            host.localStatus.set("dragging " + stack.getHoverName().getString() + " from " + chestLabel);
        });
        cell.addEventListener(UIEvents.DRAG_END, event -> {
            Object payload = event.dragHandler == null ? null : event.dragHandler.getDraggingObject();
            boolean consumed = host.chestDragDropConsumed;
            host.chestDragDropConsumed = false;
            if (!consumed
                    && payload instanceof ChestStackDrag drag
                    && drag.storageId().equals(storageId)
                    && drag.chestSlotIndex() == chestSlotIndex) {
                host.rpc.sendTakeFromChest(storageId, chestSlotIndex);
            }
            handleDragEnd(event);
        });
    }
}
