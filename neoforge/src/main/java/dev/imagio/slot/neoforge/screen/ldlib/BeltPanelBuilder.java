package dev.imagio.slot.neoforge.screen.ldlib;

import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceFormat.compactCount;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceFormat.islandSubtitle;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceFormat.itemName;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceFormat.selectionHomeStatus;
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
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;

final class BeltPanelBuilder {
    private final SlotWorkspaceUiController host;

    BeltPanelBuilder(SlotWorkspaceUiController host) {
        this.host = host;
    }

    UIElement overlay() {
        UIElement overlay = buildPanel();
        overlay.layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(0)
                .right(0)
                .bottom(4)
                .height(BELT_HEIGHT));
        overlay.style(style -> style.zIndex(6));
        return overlay;
    }

    UIElement selectionPanel() {
        UIElement panelEl = panel(PANEL_ALT).layout(layout -> layout
                .widthPercent(100)
                .paddingAll(6)
                .gapAll(4)
                .flexDirection(FlexDirection.COLUMN));
        SlotWorkspaceViewModel.AtlasItem atlasItem = host.focusedAtlasItem();
        SlotWorkspaceViewModel.HotbarSlot hotbar = host.selectedHotbarSlot();
        if (atlasItem != null) {
            SlotWorkspaceViewModel.AtlasIsland island = host.viewModel.island(atlasItem.islandId());
            ArrayList<UIElement> children = new ArrayList<>();
            UIElement hero = new UIElement().layout(layout -> layout
                    .widthPercent(100)
                    .height(20)
                    .gapAll(6)
                    .alignItems(AlignItems.CENTER)
                    .flexDirection(FlexDirection.ROW));
            hero.addChildren(
                    host.atlasCard.slotPreview(atlasItem, 18, true),
                    label(shorten(atlasItem.name(), 24), TEXT).layout(layout -> layout.flex(1).height(12)),
                    label("x" + compactCount(atlasItem.totalCount()), ACCENT).layout(layout -> layout.width(28).height(12))
            );
            children.add(label(host.selectedAtlasItem() != null ? "Selected Item" : "Focused Item", ACCENT).layout(layout -> layout.height(12)));
            children.add(hero);
            children.add(wrappedLabel("id: " + atlasItem.identity().itemId(), MUTED));
            children.add(wrappedLabel("source: main:" + atlasItem.firstSlotIndex(), MUTED));
            children.add(wrappedLabel("home: " + (island == null ? atlasItem.islandId() : island.label()), MUTED));
            children.add(label(selectionHomeStatus(atlasItem, island), atlasItem.playerPlaced() ? ACCENT : island != null && island.kind() == VisualAtlasIslandKind.TRIAGE ? WARNING : ACCENT)
                    .layout(layout -> layout.height(12)));
            children.add(wrappedLabel("Drag to move this home. Drop on a hotbar slot to assign quick access.", MUTED));
            host.appendTooltipPreview(children, atlasItem);
            panelEl.addChildren(children.toArray(UIElement[]::new));
        } else if (hotbar != null) {
            panelEl.addChildren(
                    label("Selected Hotbar", ACCENT).layout(layout -> layout.height(12)),
                    label("slot " + (hotbar.hotbarIndex() + 1), TEXT).layout(layout -> layout.height(12)),
                    label(hotbar.occupied() ? itemName(hotbar.displayStack()) : "empty", MUTED).layout(layout -> layout.height(12))
            );
        } else {
            panelEl.addChildren(
                    label("Selection", ACCENT).layout(layout -> layout.height(12)),
                    wrappedLabel("Select an anchor or hotbar slot to inspect it. Rich detail lives here so atlas homes can stay compact.", MUTED)
            );
        }
        return panelEl;
    }

    UIElement buildPanel() {
        UIElement panel = panel(PANEL).layout(layout -> layout
                .widthPercent(100)
                .height(BELT_HEIGHT)
                .paddingAll(2)
                .gapAll(2)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.ROW));
        host.clearSelectionOnDirectClick(panel);
        // Swallow mouse and drag events that land on the belt chrome (gaps between
        // slots, dividers, spacers) so they don't bubble to the atlas underneath.
        // Individual slot handlers fire first and call stopPropagation() themselves;
        // this catcher only sees events that missed a slot.
        panel.addEventListener(UIEvents.MOUSE_DOWN, UIEvent::stopPropagation);
        panel.addEventListener(UIEvents.DRAG_PERFORM, UIEvent::stopPropagation);
        panel.addChild(spacer());
        panel.addChild(host.kit.kitCluster());
        panel.addChild(buildDivider());
        for (SlotWorkspaceViewModel.HotbarSlot slot : host.viewModel.hotbarSlots()) {
            panel.addChild(slotButton(slot));
        }
        panel.addChild(buildDivider());
        panel.addChild(offhandButton(host.viewModel.offhand()));
        panel.addChild(spacer());
        return panel;
    }

    UIElement spacer() {
        UIElement spacer = new UIElement().layout(layout -> layout.flex(1).height(1));
        spacer.setAllowHitTest(false);
        return spacer;
    }

    UIElement buildDivider() {
        UIElement divider = panel(ISLAND_BORDER).layout(layout -> layout.width(1).height(BELT_DIVIDER_HEIGHT));
        divider.setAllowHitTest(false);
        return divider;
    }

    int slotChromeColor(SlotWorkspaceViewModel.HotbarSlot slot, boolean selected) {
        if (selected) {
            return SELECTED;
        }
        return slot.selected() ? ACTIVE_HOTBAR : ROW;
    }

    Button slotButton(SlotWorkspaceViewModel.HotbarSlot slot) {
        boolean selected = host.selectedHotbarIndex.get() == slot.hotbarIndex();
        Button button = button("", true, slotChromeColor(slot, selected));
        host.atlasContentSubscriptions.add(host.selectedHotbarIndex.subscribeLater(idx -> {
            applyButtonColors(button, true, slotChromeColor(slot, idx == slot.hotbarIndex()));
        }));
        button.layout(layout -> layout
                .width(BELT_SLOT_SIZE)
                .height(BELT_SLOT_SIZE)
                .paddingAll(1)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.COLUMN));
        button.noText();
        button.setOnClick(event -> {
            event.stopPropagation();
            if (Screen.hasShiftDown() && slot.occupied()) {
                host.sendReturnHotbarToHome(slot.hotbarIndex());
                return;
            }
            SlotWorkspaceViewModel.AtlasItem atlasItem = host.selectedAtlasItem();
            if (atlasItem != null) {
                host.sendAssignToHotbarSlot(atlasItem, slot.hotbarIndex());
                return;
            }
            if (!slot.occupied()) {
                host.selectedHotbarIndex.set(-1);
                host.localStatus.set("belt " + (slot.hotbarIndex() + 1) + " is empty");
                return;
            }
            host.selectedHotbarIndex.set(slot.hotbarIndex());
            host.selectedAtlasIdentity.set(null);
            host.localStatus.set("selected belt " + (slot.hotbarIndex() + 1) + " -> drag to atlas to return");
        });
        button.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button == 1 && slot.occupied()) {
                event.stopPropagation();
                host.menu.openContextMenuForHotbar(slot, event.x, event.y);
            }
        });
        host.drag.installHotbarDragSource(button, slot);
        host.drag.installHotbarDropTarget(button, slot);
        host.installHotbarHoverTooltip(button, slot);
        button.addEventListener(UIEvents.MOUSE_ENTER, event -> {
            if (slot.occupied()) {
                host.hoveredHotbarIndex = slot.hotbarIndex();
            }
        }, true);
        button.addEventListener(UIEvents.MOUSE_LEAVE, event -> {
            if (host.hoveredHotbarIndex == slot.hotbarIndex()) {
                host.hoveredHotbarIndex = -1;
            }
        }, true);
        host.hotbarSlotElements.put(slot.hotbarIndex(), button);
        if (slot.occupied()) {
            boolean[] lastAccent = {false};
            button.addEventListener(UIEvents.TICK, event -> {
                boolean accent = host.shouldAccentHotbarSlot(slot);
                if (accent != lastAccent[0]) {
                    button.style(style -> style.overlayTexture(accent ? rect(HOVER_ACCENT_OVERLAY) : IGuiTexture.EMPTY));
                    lastAccent[0] = accent;
                }
            });
        }

        UIElement iconSlot = slot.occupied() ? host.itemIcon(slot.displayStack(), 16) : host.emptyIcon();
        iconSlot.layout(layout -> layout.width(16).height(16));
        button.addChild(iconSlot);
        if (slot.occupied() && slot.count() > 1) {
            Label countBadge = label(compactCount(slot.count()), ACCENT);
            countBadge.layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .right(1)
                    .bottom(0)
                    .height(6));
            countBadge.textStyle(style -> style
                    .textColor(ACCENT)
                    .fontSize(6)
                    .textShadow(true)
                    .textAlignHorizontal(Horizontal.RIGHT)
                    .textAlignVertical(Vertical.BOTTOM));
            countBadge.setAllowHitTest(false);
            button.addChild(countBadge);
        }
        Label indexBadge = label(Integer.toString(slot.hotbarIndex() + 1), slot.selected() ? WARNING : MUTED);
        indexBadge.layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(1)
                .top(0)
                .height(6));
        indexBadge.textStyle(style -> style
                .textColor(slot.selected() ? WARNING : MUTED)
                .fontSize(6)
                .textShadow(true)
                .textAlignHorizontal(Horizontal.LEFT)
                .textAlignVertical(Vertical.TOP));
        indexBadge.setAllowHitTest(false);
        button.addChild(indexBadge);
        return button;
    }

    UIElement offhandButton(SlotWorkspaceViewModel.OffhandSlot offhand) {
        Button button = button("", false, ROW_DIM);
        button.layout(layout -> layout
                .width(BELT_SLOT_SIZE)
                .height(BELT_SLOT_SIZE)
                .paddingAll(1)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.COLUMN));
        button.noText();
        button.setActive(false);
        host.installOffhandHoverTooltip(button, offhand);
        UIElement iconSlot = offhand.occupied() ? host.itemIcon(offhand.displayStack(), 16) : host.emptyIcon();
        iconSlot.layout(layout -> layout.width(16).height(16));
        button.addChild(iconSlot);
        if (offhand.occupied() && offhand.count() > 1) {
            Label countBadge = label(compactCount(offhand.count()), MUTED);
            countBadge.layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .right(1)
                    .bottom(0)
                    .height(6));
            countBadge.textStyle(style -> style
                    .textColor(MUTED)
                    .fontSize(6)
                    .textShadow(true)
                    .textAlignHorizontal(Horizontal.RIGHT)
                    .textAlignVertical(Vertical.BOTTOM));
            countBadge.setAllowHitTest(false);
            button.addChild(countBadge);
        }
        Label offLabel = label("off", MUTED);
        offLabel.layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(1)
                .top(0)
                .height(6));
        offLabel.textStyle(style -> style
                .textColor(MUTED)
                .fontSize(6)
                .textShadow(true)
                .textAlignHorizontal(Horizontal.LEFT)
                .textAlignVertical(Vertical.TOP));
        offLabel.setAllowHitTest(false);
        button.addChild(offLabel);
        return button;
    }
}
