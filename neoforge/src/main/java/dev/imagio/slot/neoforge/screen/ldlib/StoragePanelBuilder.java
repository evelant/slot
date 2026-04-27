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
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.ChestTileDrag;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;

final class StoragePanelBuilder {
    static final int TAB_STRIP_HEIGHT = 24;
    static final int TAB_HEIGHT = 18;
    static final int TAB_MIN_WIDTH = 96;
    static final int CARD_FLOW_GAP = 6;
    static final int CARD_FLOW_PADDING = 6;

    private final SlotWorkspaceUiController host;

    StoragePanelBuilder(SlotWorkspaceUiController host) {
        this.host = host;
    }

    UIElement body() {
        UIElement strip = new UIElement().layout(layout -> layout
                .widthPercent(100)
                .gapAll(0)
                .flexDirection(FlexDirection.COLUMN));
        strip.addChild(tabStrip());
        SlotWorkspaceViewModel.StorageAreaSnapshot active = activeArea();
        if (active != null) {
            strip.addChild(cardFlow(active));
        }
        return strip;
    }

    SlotWorkspaceViewModel.StorageAreaSnapshot activeArea() {
        String id = host.effectiveStorageAreaId();
        return id == null ? null : host.viewModel.storageArea(id);
    }

    UIElement tabStrip() {
        UIElement row = panel(PANEL).layout(layout -> layout
                .widthPercent(100)
                .height(TAB_STRIP_HEIGHT)
                .paddingHorizontal(CARD_FLOW_PADDING)
                .paddingVertical(3)
                .gapAll(4)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.ROW));
        // Click on the strip background (between tabs) clears selection
        // — same convention as other workspace panels.
        host.clearSelectionOnDirectClick(row);
        // Swallow drags that miss a tab so they don't fall through to
        // the atlas behind the strip.
        row.addEventListener(UIEvents.MOUSE_DOWN, UIEvent::stopPropagation);

        if (host.viewModel.storageAreas().isEmpty()) {
            Label hint = label("No storage areas yet — right-click a chest in world to claim it.", MUTED);
            hint.layout(layout -> layout.flex(1).height(TAB_STRIP_HEIGHT - 6));
            hint.textStyle(style -> style
                    .textColor(MUTED)
                    .textShadow(false)
                    .fontSize(8)
                    .textAlignHorizontal(Horizontal.LEFT)
                    .textAlignVertical(Vertical.CENTER));
            hint.setAllowHitTest(false);
            row.addChild(hint);
            return row;
        }

        String activeId = host.effectiveStorageAreaId();
        for (SlotWorkspaceViewModel.StorageAreaSnapshot area : host.viewModel.storageAreas()) {
            row.addChild(tabChip(area, area.areaId().equals(activeId)));
        }
        return row;
    }

    UIElement tabChip(SlotWorkspaceViewModel.StorageAreaSnapshot area, boolean isActive) {
        int fill = isActive ? area.color() : (area.color() & 0x00FFFFFF) | 0x60000000;
        int textColor = isActive ? TEXT : MUTED;

        UIElement chip = panel(fill).layout(layout -> layout
                .height(TAB_HEIGHT)
                .minWidth(TAB_MIN_WIDTH)
                .paddingHorizontal(8)
                .gapAll(4)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.ROW));

        Label labelEl = label(area.label(), textColor);
        labelEl.layout(layout -> layout.flex(1).height(TAB_HEIGHT));
        labelEl.textStyle(style -> style
                .textColor(textColor)
                .textShadow(false)
                .fontSize(8)
                .textAlignHorizontal(Horizontal.LEFT)
                .textAlignVertical(Vertical.CENTER));
        labelEl.setAllowHitTest(false);
        chip.addChild(labelEl);

        Label countEl = label("· " + area.chestCount(), MUTED);
        countEl.layout(layout -> layout.width(20).height(TAB_HEIGHT));
        countEl.textStyle(style -> style
                .textColor(MUTED)
                .textShadow(false)
                .fontSize(7)
                .textAlignHorizontal(Horizontal.RIGHT)
                .textAlignVertical(Vertical.CENTER));
        countEl.setAllowHitTest(false);
        chip.addChild(countEl);

        if (area.proximate()) {
            Label dot = label("●", ACCENT);
            dot.layout(layout -> layout.width(8).height(TAB_HEIGHT));
            dot.textStyle(style -> style
                    .textColor(ACCENT)
                    .textShadow(false)
                    .fontSize(7)
                    .textAlignHorizontal(Horizontal.CENTER)
                    .textAlignVertical(Vertical.CENTER));
            dot.setAllowHitTest(false);
            chip.addChild(dot);
        }

        String areaId = area.areaId();
        chip.addEventListener(UIEvents.CLICK, event -> {
            if (event.button != 0) {
                return;
            }
            event.stopPropagation();
            // Click an active tab → clear manual override (collapse).
            // Click a non-active tab → pin it as manual override.
            if (areaId.equals(host.activeStorageAreaId)
                    || (host.activeStorageAreaId == null && isActive)) {
                host.activeStorageAreaId = null;
                host.localStatus.set("storage tab cleared");
            } else {
                host.activeStorageAreaId = areaId;
                host.localStatus.set("storage: " + area.label());
            }
            host.rebuild();
        });

        installTabDropTarget(chip, areaId);
        return chip;
    }

    /**
     * Tab chips accept a {@link ChestTileDrag} → reassigns the chest to
     * this tab's area. Other drag types are ignored (drop falls through
     * to the atlas underneath via the strip-level swallow).
     */
    void installTabDropTarget(UIElement chip, String areaId) {
        chip.addEventListener(UIEvents.DRAG_ENTER, event -> {
            ChestTileDrag drag = host.drag.chestTileDrag(event);
            host.drag.updateGenericDropOverlay(chip, drag != null, ACCENT);
        }, true);
        chip.addEventListener(UIEvents.DRAG_UPDATE, event -> {
            ChestTileDrag drag = host.drag.chestTileDrag(event);
            host.drag.updateGenericDropOverlay(chip, drag != null, ACCENT);
        });
        chip.addEventListener(UIEvents.DRAG_LEAVE, event -> host.drag.clearDropOverlay(chip), true);
        chip.addEventListener(UIEvents.DRAG_PERFORM, event -> {
            host.drag.clearDropOverlay(chip);
            ChestTileDrag drag = host.drag.chestTileDrag(event);
            if (drag == null) {
                return;
            }
            host.rpc.sendMoveChestToArea(drag.storageId(), areaId);
            event.stopPropagation();
        });
    }

    UIElement cardFlow(SlotWorkspaceViewModel.StorageAreaSnapshot area) {
        UIElement flow = panel(PANEL_ALT).layout(layout -> layout
                .widthPercent(100)
                .paddingAll(CARD_FLOW_PADDING)
                .gapAll(CARD_FLOW_GAP)
                .alignItems(AlignItems.STRETCH)
                .flexDirection(FlexDirection.ROW));
        host.clearSelectionOnDirectClick(flow);
        flow.addEventListener(UIEvents.MOUSE_DOWN, UIEvent::stopPropagation);

        if (area.chestTiles().isEmpty()) {
            Label empty = label("No chests claimed for this area yet.", MUTED);
            empty.layout(layout -> layout.flex(1).height(20));
            empty.textStyle(style -> style
                    .textColor(MUTED)
                    .textShadow(false)
                    .fontSize(8)
                    .textAlignHorizontal(Horizontal.LEFT)
                    .textAlignVertical(Vertical.CENTER));
            empty.setAllowHitTest(false);
            flow.addChild(empty);
            return flow;
        }
        for (SlotWorkspaceViewModel.ClaimedChestTile tile : area.chestTiles()) {
            flow.addChild(host.islandChest.chestTilePanelInFlow(tile));
        }
        return flow;
    }
}
