package dev.imagio.slot.neoforge.screen.ldlib;

import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceFormat.*;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.*;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceUi.*;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.event.HoverTooltips;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.util.DrawerHelper;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.AtlasItemDrag;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.HotbarSlotDrag;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.KitBringDrag;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.KitSlotDrag;
import dev.imagio.slot.workflow.domain.KitPage;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

final class KitRackBuilder {
    static final int KIT_CARD_WIDTH = 180;
    static final int KIT_CELL_SIZE = 14;
    static final int KIT_CELL_ICON_SIZE = 11;

    private final SlotWorkspaceUiController host;

    KitRackBuilder(SlotWorkspaceUiController host) {
        this.host = host;
    }

    static int kitCardHeight(int pageCount, int bringCount) {
        int pages = Math.max(1, pageCount);
        // header + per-page rows + add-page footer + bring row + bring label
        int bringHeight = bringCount > 0 ? 28 : 16;
        return 12 + pages * 18 + 14 + bringHeight;
    }

    UIElement kitCluster() {
        // Fixed-width container with right-aligned children so changes to the kit
        // toggle's label (kit name / page indicator) grow LEFT into the cluster's own
        // whitespace instead of pushing the hotbar. Sized to fit the widest reasonable
        // label ("longname 3/3") plus the page-cycle button without truncation.
        UIElement cluster = new UIElement().layout(layout -> layout
                .width(KIT_CLUSTER_WIDTH)
                .height(BELT_SLOT_SIZE)
                .gapAll(2)
                .alignItems(AlignItems.CENTER)
                .justifyContent(AlignContent.FLEX_END)
                .flexDirection(FlexDirection.ROW));
        cluster.addChild(kitsToggleButton());
        SlotWorkspaceViewModel.KitCard activeCard = host.viewModel.activeKit();
        if (activeCard != null && activeCard.pageCount() > 1) {
            cluster.addChild(kitPageCycleButton(activeCard));
        }
        return cluster;
    }

    Button kitsToggleButton() {
        int kitCount = host.viewModel.kits().size();
        SlotWorkspaceViewModel.KitCard activeCard = host.viewModel.activeKit();
        String label;
        if (activeCard != null) {
            String suffix = activeCard.pageCount() > 1
                    ? " " + (activeCard.activePageIndex() + 1) + "/" + activeCard.pageCount()
                    : "";
            label = shorten(activeCard.name(), 10) + suffix;
        } else {
            label = "Kits";
        }
        int bgColor = host.kitRackOpen
                ? PANEL_ALT
                : activeCard != null ? ACTIVE_HOTBAR : PANEL_ALT;
        int textColor = host.kitRackOpen
                ? ACCENT
                : activeCard != null ? TEXT : MUTED;
        Button button = button(label, true, bgColor);
        button.layout(layout -> layout
                .width(Math.max(44, label.length() * 5 + 12))
                .height(BELT_SLOT_SIZE)
                .paddingAll(2)
                .alignItems(AlignItems.CENTER));
        button.textStyle(style -> style
                .textColor(textColor)
                .textShadow(activeCard != null)
                .fontSize(8)
                .textAlignHorizontal(Horizontal.CENTER)
                .textAlignVertical(Vertical.CENTER));
        button.setOnClick(event -> {
            event.stopPropagation();
            host.kitRackOpen = !host.kitRackOpen;
            host.localStatus.set(host.kitRackOpen
                    ? "kit rack open (" + kitCount + " kit" + (kitCount == 1 ? "" : "s") + ")"
                    : "kit rack closed");
            host.rebuild();
        });
        return button;
    }

    Button kitPageCycleButton(SlotWorkspaceViewModel.KitCard activeCard) {
        Button button = button(">", true, ACTIVE_HOTBAR);
        button.layout(layout -> layout
                .width(16)
                .height(BELT_SLOT_SIZE)
                .paddingAll(1)
                .alignItems(AlignItems.CENTER));
        button.textStyle(style -> style
                .textColor(TEXT)
                .textShadow(false)
                .fontSize(8)
                .textAlignHorizontal(Horizontal.CENTER)
                .textAlignVertical(Vertical.CENTER));
        button.addEventListener(UIEvents.HOVER_TOOLTIPS, event -> event.hoverTooltips = new HoverTooltips(
                List.of(Component.literal("Next Kit page (" + activeCard.pageCount() + " total)")),
                null,
                null,
                ItemStack.EMPTY
        ));
        button.setOnClick(event -> {
            event.stopPropagation();
            int direction = (event.button == 1 || Screen.hasShiftDown()) ? -1 : 1;
            host.rpc.sendSwitchKitPage(direction);
        });
        return button;
    }

    UIElement kitRackOverlay() {
        // Dock flush with the belt top (belt sits at bottom(4) with height BELT_HEIGHT)
        // so the rack feels like an extension of the belt rather than a floating panel.
        UIElement overlay = panel(GLASS).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(16)
                .right(16)
                .bottom(BELT_HEIGHT + 4)
                .height(kitRackHeight())
                .paddingAll(6)
                .gapAll(6)
                .flexDirection(FlexDirection.COLUMN));
        overlay.style(style -> style.zIndex(7));
        overlay.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());
        overlay.addChild(kitRackHeader());
        overlay.addChild(kitRackBody());
        return overlay;
    }

    UIElement kitRackHeader() {
        UIElement row = new UIElement().layout(layout -> layout
                .widthPercent(100)
                .height(16)
                .gapAll(6)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.ROW));
        int kitCount = host.viewModel.kits().size();
        Label title = label("Kits (" + kitCount + ")", ACCENT);
        title.layout(layout -> layout.flex(1).height(12));
        title.textStyle(style -> style
                .textColor(ACCENT)
                .textShadow(false)
                .fontSize(9)
                .textAlignHorizontal(Horizontal.LEFT)
                .textAlignVertical(Vertical.CENTER));
        SlotWorkspaceViewModel.KitCard activeCard = host.viewModel.activeKit();
        String saveLabel;
        if (activeCard != null) {
            saveLabel = activeCard.pageCount() > 1
                    ? "Update Page " + (activeCard.activePageIndex() + 1)
                    : "Update Active Kit";
        } else {
            saveLabel = "Save Current Belt as Kit";
        }
        Button save = button(saveLabel, true, PANEL_ALT);
        save.layout(layout -> layout.width(Math.max(110, saveLabel.length() * 6 + 10)).height(14));
        save.textStyle(style -> style
                .textColor(ACCENT)
                .textShadow(false)
                .fontSize(8)
                .textAlignHorizontal(Horizontal.CENTER)
                .textAlignVertical(Vertical.CENTER));
        save.setOnClick(event -> {
            event.stopPropagation();
            host.rpc.sendSaveKit();
        });
        Button close = button("x", true, PANEL_ALT);
        close.layout(layout -> layout.width(14).height(14));
        close.textStyle(style -> style
                .textColor(MUTED)
                .textShadow(false)
                .fontSize(8)
                .textAlignHorizontal(Horizontal.CENTER)
                .textAlignVertical(Vertical.CENTER));
        close.setOnClick(event -> {
            event.stopPropagation();
            host.kitRackOpen = false;
            host.rebuild();
        });
        row.addChildren(title, save, close);
        return row;
    }

    int kitRackBodyHeight() {
        int maxPageCount = 1;
        int maxBringCount = 0;
        for (SlotWorkspaceViewModel.KitCard card : host.viewModel.kits()) {
            if (card.pageCount() > maxPageCount) {
                maxPageCount = card.pageCount();
            }
            if (card.bringSlotCount() > maxBringCount) {
                maxBringCount = card.bringSlotCount();
            }
        }
        return Math.max(44, kitCardHeight(maxPageCount, maxBringCount) + 6);
    }

    int kitRackHeight() {
        // padding (6 top + 6 bottom) + header row (14) + gap to body (6) + body height.
        // Kept as a single computation so triagePanelOverlay can lift itself above the
        // rack when it's open.
        return 12 + 14 + 6 + kitRackBodyHeight();
    }

    UIElement kitRackBody() {
        int bodyHeight = kitRackBodyHeight();
        UIElement body = new UIElement().layout(layout -> layout
                .widthPercent(100)
                .height(bodyHeight)
                .gapAll(6)
                .alignItems(AlignItems.FLEX_START)
                .flexDirection(FlexDirection.ROW));
        if (host.viewModel.kits().isEmpty()) {
            Label empty = label("No kits yet. Load your belt, then Save Current Belt.", MUTED);
            empty.layout(layout -> layout.flex(1).height(12));
            empty.textStyle(style -> style
                    .textColor(MUTED)
                    .textShadow(false)
                    .fontSize(8)
                    .textAlignHorizontal(Horizontal.CENTER)
                    .textAlignVertical(Vertical.CENTER));
            body.addChild(empty);
            return body;
        }
        for (SlotWorkspaceViewModel.KitCard card : host.viewModel.kits()) {
            body.addChild(kitCardButton(card));
        }
        return body;
    }

    UIElement kitCardButton(SlotWorkspaceViewModel.KitCard card) {
        int baseColor = card.active() ? ACTIVE_HOTBAR : ROW;
        // Active card uses a brighter amber on hover so it stays visually distinct from
        // the inactive hover (which is slate). The default applyButtonColors hover path
        // maps any full-alpha base to ROW_HOVER — that makes active cards look inactive
        // on hover, which confuses "click to deactivate".
        int hoverColor = card.active() ? ACTIVE_HOTBAR_HOVER : ROW_HOVER;
        int pressedColor = card.active() ? ACTIVE_HOTBAR_PRESSED : SELECTED;
        Button button = new Button();
        button.setActive(true);
        button.buttonStyle(style -> style
                .baseTexture(rect(baseColor))
                .hoverTexture(rect(hoverColor))
                .pressedTexture(rect(pressedColor)));
        button.textStyle(style -> style.font(FONT_UI).textColor(TEXT).textShadow(false).fontSize(8));
        button.layout(layout -> layout
                .width(KIT_CARD_WIDTH)
                .height(kitCardHeight(card.pageCount(), card.bringSlotCount()))
                .paddingAll(4)
                .gapAll(2)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.COLUMN));
        button.noText();
        button.setOnClick(event -> {
            event.stopPropagation();
            if (card.active()) {
                host.rpc.sendDeactivateKit();
            } else {
                host.rpc.sendActivateKit(card.kitId());
            }
        });
        button.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button == 1) {
                event.stopPropagation();
                host.menu.openContextMenuForKit(card.kitId(), event.x, event.y);
            }
        });
        button.addChild(kitCardHeader(card));
        for (SlotWorkspaceViewModel.KitPageView page : card.pages()) {
            button.addChild(kitCardPageRow(card, page));
        }
        button.addChild(kitCardAddPageRow(card));
        button.addChild(kitCardBringRow(card));
        return button;
    }

    UIElement kitCardHeader(SlotWorkspaceViewModel.KitCard card) {
        UIElement row = new UIElement().layout(layout -> layout
                .widthPercent(100)
                .height(12)
                .gapAll(2)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.ROW));
        Label name = label(shorten(card.name(), 18), card.active() ? TEXT : TEXT);
        name.layout(layout -> layout.flex(1).height(10));
        name.textStyle(style -> style
                .textColor(TEXT)
                .textShadow(false)
                .fontSize(8)
                .textAlignHorizontal(Horizontal.LEFT)
                .textAlignVertical(Vertical.CENTER));
        name.setAllowHitTest(false);
        int aggregateSlots = 0;
        int aggregateReady = 0;
        for (SlotWorkspaceViewModel.KitPageView page : card.pages()) {
            aggregateSlots += page.slotCount();
            aggregateReady += page.readyCount();
        }
        final int totalSlots = aggregateSlots;
        final int totalReady = aggregateReady;
        Label readiness = label(totalReady + "/" + totalSlots,
                totalReady == totalSlots ? ACCENT : WARNING);
        readiness.layout(layout -> layout.width(26).height(10));
        readiness.textStyle(style -> style
                .textColor(totalReady == totalSlots ? ACCENT : WARNING)
                .textShadow(false)
                .fontSize(8)
                .textAlignHorizontal(Horizontal.RIGHT)
                .textAlignVertical(Vertical.CENTER));
        readiness.setAllowHitTest(false);
        // Delete moved to the right-click menu with confirm to prevent
        // fat-finger loss of a 10-minute kit setup.
        row.addChildren(name, readiness);
        return row;
    }

    UIElement kitCardPageRow(SlotWorkspaceViewModel.KitCard card, SlotWorkspaceViewModel.KitPageView page) {
        boolean isActivePage = card.active() && card.activePageIndex() == page.pageIndex();
        UIElement row = isActivePage
                ? panel(ACTIVE_PAGE_ROW)
                : new UIElement();
        row.layout(layout -> layout
                .widthPercent(100)
                .height(KIT_CELL_SIZE + 2)
                .paddingHorizontal(2)
                .gapAll(2)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.ROW));
        Label pageLabel = label(String.valueOf(page.pageIndex() + 1), isActivePage ? ACCENT : MUTED);
        pageLabel.layout(layout -> layout.width(8).height(12));
        pageLabel.textStyle(style -> style
                .textColor(isActivePage ? ACCENT : MUTED)
                .textShadow(false)
                .fontSize(8)
                .textAlignHorizontal(Horizontal.CENTER)
                .textAlignVertical(Vertical.CENTER));
        pageLabel.setAllowHitTest(false);
        row.addChild(pageLabel);
        UIElement strip = new UIElement().layout(layout -> layout
                .flex(1)
                .height(KIT_CELL_SIZE)
                .gapAll(1)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.ROW));
        for (SlotWorkspaceViewModel.KitSlotState slot : page.slots()) {
            strip.addChild(kitCardSlotCell(card, page, slot));
        }
        row.addChild(strip);
        if (card.pageCount() > 1) {
            Button remove = button("-", true, PANEL_ALT);
            remove.layout(layout -> layout.width(12).height(12));
            remove.textStyle(style -> style
                    .textColor(MUTED)
                    .textShadow(false)
                    .fontSize(8)
                    .textAlignHorizontal(Horizontal.CENTER)
                    .textAlignVertical(Vertical.CENTER));
            remove.setOnClick(event -> {
                event.stopPropagation();
                host.rpc.sendRemoveKitPage(card.kitId(), page.pageIndex());
            });
            row.addChild(remove);
        }
        return row;
    }

    UIElement kitCardAddPageRow(SlotWorkspaceViewModel.KitCard card) {
        UIElement row = new UIElement().layout(layout -> layout
                .widthPercent(100)
                .height(14)
                .gapAll(2)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.ROW));
        boolean canAdd = (card.carriedSlotCount() + KitPage.HOTBAR_SLOT_COUNT) <= card.carriedSlotCapacity();
        Button add = button(canAdd ? "+ page" : "+ page (full)", true, canAdd ? PANEL_ALT : 0x40202020);
        add.layout(layout -> layout.flex(1).height(12));
        add.textStyle(style -> style
                .textColor(canAdd ? MUTED : 0x80808080)
                .textShadow(false)
                .fontSize(8)
                .textAlignHorizontal(Horizontal.CENTER)
                .textAlignVertical(Vertical.CENTER));
        if (canAdd) {
            add.setOnClick(event -> {
                event.stopPropagation();
                host.rpc.sendAddKitPage(card.kitId());
            });
        } else {
            add.setOnClick(event -> event.stopPropagation());
        }
        row.addChild(add);
        int missing = kitMissingIdentityCount(card);
        if (missing > 0) {
            // Dark background + amber text keeps the button readable against
            // both the card's active-kit olive tint and the inactive ROW bg.
            Button gather = button("gather " + missing, true, PANEL_ALT);
            gather.layout(layout -> layout.flex(1).height(12));
            gather.textStyle(style -> style
                    .textColor(WARNING)
                    .textShadow(false)
                    .fontSize(8)
                    .textAlignHorizontal(Horizontal.CENTER)
                    .textAlignVertical(Vertical.CENTER));
            gather.setOnClick(event -> {
                event.stopPropagation();
                advanceGather(card);
            });
            row.addChild(gather);
        }
        return row;
    }

    int kitMissingIdentityCount(SlotWorkspaceViewModel.KitCard card) {
        int count = 0;
        for (SlotWorkspaceViewModel.KitPageView page : card.pages()) {
            for (SlotWorkspaceViewModel.KitSlotState slot : page.slots()) {
                if (slot.filled() && !slot.ready()) {
                    count++;
                }
            }
        }
        for (SlotWorkspaceViewModel.KitBringItem item : card.bring()) {
            if (!item.ready()) {
                count++;
            }
        }
        return count;
    }

    List<SlotWorkspaceViewModel.IdentityRef> kitMissingIdentities(SlotWorkspaceViewModel.KitCard card) {
        java.util.LinkedHashSet<SlotWorkspaceViewModel.IdentityRef> missing = new java.util.LinkedHashSet<>();
        for (SlotWorkspaceViewModel.KitPageView page : card.pages()) {
            for (SlotWorkspaceViewModel.KitSlotState slot : page.slots()) {
                if (slot.filled() && !slot.ready()) {
                    missing.add(slot.identity());
                }
            }
        }
        for (SlotWorkspaceViewModel.KitBringItem item : card.bring()) {
            if (!item.ready()) {
                missing.add(item.identity());
            }
        }
        return List.copyOf(missing);
    }

    void advanceGather(SlotWorkspaceViewModel.KitCard card) {
        List<SlotWorkspaceViewModel.IdentityRef> missing = kitMissingIdentities(card);
        if (missing.isEmpty()) {
            host.localStatus.set("nothing to gather");
            host.rebuild();
            return;
        }
        // Pull every missing identity that lives in a proximate chest.
        // Each successful take re-applies the kit plan server-side (see
        // {@code reapplyActiveKitFromCarry}), so the items snap into
        // their declared hotbar slots and clear from the missing set
        // on the next view-model refresh. Items that aren't in any
        // proximate chest are listed as still-needed so the player
        // knows what's left to fetch from remote storage.
        int chestPullsRequested = 0;
        int unreachable = 0;
        for (SlotWorkspaceViewModel.IdentityRef identity : missing) {
            SlotWorkspaceViewModel.AtlasItem atlasItem = host.viewModel.atlasItem(identity);
            if (atlasItem != null && atlasItem.proximateCount() > 0) {
                host.rpc.sendTakeStackByIdentity(identity);
                chestPullsRequested++;
            } else {
                unreachable++;
            }
        }
        if (chestPullsRequested > 0) {
            String suffix = unreachable > 0 ? " (" + unreachable + " not in nearby chests)" : "";
            host.localStatus.set("gathering " + chestPullsRequested + " from nearby chests" + suffix);
        } else {
            host.localStatus.set("nothing to gather from nearby chests (" + unreachable + " still needed)");
        }
        host.rebuild();
    }

    UIElement kitCardBringRow(SlotWorkspaceViewModel.KitCard card) {
        UIElement column = new UIElement().layout(layout -> layout
                .widthPercent(100)
                .height(card.bringSlotCount() > 0 ? 28 : 16)
                .gapAll(2)
                .alignItems(AlignItems.FLEX_START)
                .flexDirection(FlexDirection.COLUMN));
        String header = "bring " + card.bringReadyCount() + "/" + card.bringSlotCount();
        Label title = label(header, card.bringSlotCount() == 0 ? MUTED
                : card.bringReadyCount() == card.bringSlotCount() ? ACCENT : WARNING);
        title.layout(layout -> layout.widthPercent(100).height(10));
        title.textStyle(style -> style
                .textColor(card.bringSlotCount() == 0 ? MUTED
                        : card.bringReadyCount() == card.bringSlotCount() ? ACCENT : WARNING)
                .textShadow(false)
                .fontSize(8)
                .textAlignHorizontal(Horizontal.LEFT)
                .textAlignVertical(Vertical.CENTER));
        title.setAllowHitTest(false);
        column.addChild(title);
        UIElement strip = new UIElement().layout(layout -> layout
                .widthPercent(100)
                .height(KIT_CELL_SIZE)
                .gapAll(1)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.ROW));
        for (SlotWorkspaceViewModel.KitBringItem item : card.bring()) {
            strip.addChild(kitCardBringCell(card, item));
        }
        installKitBringDropTarget(strip, card);
        column.addChild(strip);
        return column;
    }

    UIElement kitCardBringCell(SlotWorkspaceViewModel.KitCard card, SlotWorkspaceViewModel.KitBringItem item) {
        int fill = item.ready() ? ROW : ROW_DIM;
        UIElement cell = panel(fill).layout(layout -> layout
                .width(KIT_CELL_SIZE)
                .height(KIT_CELL_SIZE)
                .paddingAll(1)
                .alignItems(AlignItems.CENTER));
        WorkspaceUi.installItemTooltip(cell, item.displayStack());
        cell.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button == 0) {
                event.stopPropagation();
                return;
            }
            if (event.button == 1) {
                event.stopPropagation();
                host.rpc.sendRemoveKitBring(card.kitId(), item.identity());
            }
        });
        cell.addEventListener(UIEvents.MOUSE_LEAVE, event -> {
            if (!cell.isMouseDown(0) || host.drag.isDragging(cell)) {
                return;
            }
            cell.startDrag(
                    new KitBringDrag(card.kitId(), item.identity(), item.displayStack().copy()),
                    host.drag.dragTexture(item.displayStack())
            ).setDragTexture(-10, -10, 20, 20);
            host.localStatus.set("dragging kit bring");
        }, true);
        cell.addEventListener(UIEvents.DRAG_END, host.drag::handleDragEnd);
        if (!item.displayStack().isEmpty()) {
            UIElement icon = itemIcon(item.displayStack(), KIT_CELL_ICON_SIZE, item.ready());
            icon.setAllowHitTest(false);
            cell.addChild(icon);
        }
        return cell;
    }

    void installKitBringDropTarget(UIElement target, SlotWorkspaceViewModel.KitCard card) {
        target.addEventListener(UIEvents.DRAG_ENTER, event -> updateKitBringDropOverlay(target, event), true);
        target.addEventListener(UIEvents.DRAG_UPDATE, event -> updateKitBringDropOverlay(target, event));
        target.addEventListener(UIEvents.DRAG_LEAVE, event -> host.drag.clearDropOverlay(target), true);
        target.addEventListener(UIEvents.DRAG_PERFORM, event -> {
            host.drag.clearDropOverlay(target);
            SlotWorkspaceViewModel.IdentityRef identity = kitDropIdentity(event);
            if (identity == null) {
                return;
            }
            host.rpc.sendAddKitBring(card.kitId(), identity);
            event.stopPropagation();
        });
    }

    void updateKitBringDropOverlay(UIElement target, UIEvent event) {
        SlotWorkspaceViewModel.IdentityRef identity = kitDropIdentity(event);
        host.drag.updateGenericDropOverlay(target, identity != null, ACCENT);
    }

    SlotWorkspaceViewModel.IdentityRef kitDropIdentity(UIEvent event) {
        AtlasItemDrag atlasItem = host.drag.atlasItemDrag(event);
        if (atlasItem != null) {
            return atlasItem.identity();
        }
        HotbarSlotDrag hotbar = host.drag.hotbarSlotDrag(event);
        if (hotbar != null && !hotbar.displayStack().isEmpty()) {
            return SlotWorkspaceViewModel.IdentityRef.from(
                    dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(hotbar.displayStack()));
        }
        KitSlotDrag slotDrag = kitSlotDrag(event);
        if (slotDrag != null && slotDrag.identity() != null && !slotDrag.identity().itemId().isBlank()) {
            return slotDrag.identity();
        }
        KitBringDrag bringDrag = kitBringDrag(event);
        if (bringDrag != null) {
            return bringDrag.identity();
        }
        return null;
    }

    KitSlotDrag kitSlotDrag(UIEvent event) {
        Object payload = event == null || event.dragHandler == null ? null : event.dragHandler.getDraggingObject();
        return payload instanceof KitSlotDrag slotDrag ? slotDrag : null;
    }

    KitBringDrag kitBringDrag(UIEvent event) {
        Object payload = event == null || event.dragHandler == null ? null : event.dragHandler.getDraggingObject();
        return payload instanceof KitBringDrag bringDrag ? bringDrag : null;
    }

    UIElement kitCardSlotCell(
            SlotWorkspaceViewModel.KitCard card,
            SlotWorkspaceViewModel.KitPageView page,
            SlotWorkspaceViewModel.KitSlotState slot
    ) {
        int fill = !slot.filled() ? 0x60141B22 : slot.ready() ? ROW : ROW_DIM;
        UIElement cell = panel(fill).layout(layout -> layout
                .width(KIT_CELL_SIZE)
                .height(KIT_CELL_SIZE)
                .paddingAll(1)
                .alignItems(AlignItems.CENTER));
        if (slot.filled() && !slot.displayStack().isEmpty()) {
            UIElement icon = itemIcon(slot.displayStack(), KIT_CELL_ICON_SIZE, slot.ready());
            icon.setAllowHitTest(false);
            cell.addChild(icon);
            WorkspaceUi.installItemTooltip(cell, slot.displayStack());
            cell.addEventListener(UIEvents.MOUSE_LEAVE, event -> {
                if (!cell.isMouseDown(0) || host.drag.isDragging(cell)) {
                    return;
                }
                cell.startDrag(
                        new KitSlotDrag(card.kitId(), page.pageIndex(), slot.slotIndex(), slot.identity(), slot.displayStack().copy()),
                        host.drag.dragTexture(slot.displayStack())
                ).setDragTexture(-10, -10, 20, 20);
                host.localStatus.set("dragging kit slot");
            }, true);
            cell.addEventListener(UIEvents.DRAG_END, host.drag::handleDragEnd);
        }
        cell.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button == 0) {
                // Stop left-click mouse-down so the surrounding kit-card Button doesn't
                // arm its click tracker. Otherwise a drag-rearrange gesture that ends
                // with mouse-up back inside the card fires activate/deactivate.
                event.stopPropagation();
                return;
            }
            if (event.button == 1 && slot.filled()) {
                event.stopPropagation();
                host.rpc.sendSetKitSlotIdentity(card.kitId(), page.pageIndex(), slot.slotIndex(), null);
            }
            // Right-click on an empty slot: let it bubble so the card context menu
            // still opens for rename / duplicate / delete.
        });
        installKitSlotDropTarget(cell, card, page, slot);
        return cell;
    }

    void installKitSlotDropTarget(
            UIElement target,
            SlotWorkspaceViewModel.KitCard card,
            SlotWorkspaceViewModel.KitPageView page,
            SlotWorkspaceViewModel.KitSlotState slot
    ) {
        target.addEventListener(UIEvents.DRAG_ENTER, event -> updateKitSlotDropOverlay(target, event), true);
        target.addEventListener(UIEvents.DRAG_UPDATE, event -> updateKitSlotDropOverlay(target, event));
        target.addEventListener(UIEvents.DRAG_LEAVE, event -> host.drag.clearDropOverlay(target), true);
        target.addEventListener(UIEvents.DRAG_PERFORM, event -> {
            host.drag.clearDropOverlay(target);
            // Drag within the same kit page: rearrange via swap so the source cell
            // also updates, not just the drop target.
            KitSlotDrag slotDrag = kitSlotDrag(event);
            if (slotDrag != null
                    && card.kitId().equals(slotDrag.kitId())
                    && page.pageIndex() == slotDrag.pageIndex()) {
                if (slotDrag.slotIndex() != slot.slotIndex()) {
                    host.rpc.sendSwapKitSlots(card.kitId(), page.pageIndex(), slotDrag.slotIndex(), slot.slotIndex());
                }
                event.stopPropagation();
                return;
            }
            SlotWorkspaceViewModel.IdentityRef identity = kitDropIdentity(event);
            if (identity == null) {
                return;
            }
            host.rpc.sendSetKitSlotIdentity(card.kitId(), page.pageIndex(), slot.slotIndex(), identity);
            event.stopPropagation();
        });
    }

    void updateKitSlotDropOverlay(UIElement target, UIEvent event) {
        SlotWorkspaceViewModel.IdentityRef identity = kitDropIdentity(event);
        host.drag.updateGenericDropOverlay(target, identity != null, ACCENT);
    }
}
