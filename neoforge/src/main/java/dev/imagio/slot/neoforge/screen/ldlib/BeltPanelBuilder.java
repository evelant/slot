package dev.imagio.slot.neoforge.screen.ldlib;

import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceFormat.compactCount;
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
        // In-flow row at the bottom of the wall panel — same shape
        // regardless of surface.
        overlay.layout(layout -> layout.widthPercent(100).height(BELT_HEIGHT));
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
            children.add(label("Focused Item", ACCENT).layout(layout -> layout.height(12)));
            children.add(hero);
            children.add(wrappedLabel("id: " + atlasItem.identity().itemId(), MUTED));
            children.add(wrappedLabel("source: main:" + atlasItem.firstSlotIndex(), MUTED));
            children.add(wrappedLabel("home: " + (island == null ? atlasItem.islandId() : island.label()), MUTED));
            children.add(label(selectionHomeStatus(atlasItem, island), atlasItem.playerPlaced() ? ACCENT : island != null && island.kind() == VisualAtlasIslandKind.TRIAGE ? WARNING : ACCENT)
                    .layout(layout -> layout.height(12)));
            host.appendTooltipPreview(children, atlasItem);
            panelEl.addChildren(children.toArray(UIElement[]::new));
        } else {
            panelEl.addChildren(
                    label("Focus", ACCENT).layout(layout -> layout.height(12)),
                    wrappedLabel("Hover a carried item to inspect it.", MUTED)
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
        // this catcher only sees events that missed a slot. While carrying the
        // menu cursor we let the click bubble through to root so the universal
        // cancel / smart-deposit handlers fire (rows 1, 8).
        panel.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (!WorkspaceCursorState.isCarrying()) {
                event.stopPropagation();
            }
        });
        panel.addEventListener(UIEvents.DRAG_PERFORM, UIEvent::stopPropagation);
        UIElement centerRail = new UIElement().layout(layout -> layout
                .flex(1)
                .heightPercent(100)
                .gapAll(2)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.ROW));
        centerRail.addChild(spacer());
        centerRail.addChild(offhandButton(host.viewModel.offhand()));
        centerRail.addChild(fixedGap(5));
        for (SlotWorkspaceViewModel.HotbarSlot slot : host.viewModel.hotbarSlots()) {
            centerRail.addChild(slotButton(slot));
        }
        centerRail.addChild(spacer());
        panel.addChild(centerRail);
        return panel;
    }

    UIElement spacer() {
        UIElement spacer = new UIElement().layout(layout -> layout.flex(1).height(1));
        spacer.setAllowHitTest(false);
        return spacer;
    }

    UIElement fixedGap(int width) {
        UIElement spacer = new UIElement().layout(layout -> layout.width(width).height(1));
        spacer.setAllowHitTest(false);
        return spacer;
    }

    int slotChromeColor(SlotWorkspaceViewModel.HotbarSlot slot) {
        // Only paint the vanilla "currently-held" amber on slots that
        // actually carry an item. Without this, an empty slot whose
        // index happens to match the held-hotbar-index gets the same
        // amber chrome as a fully-loaded held slot — and when a kit
        // activation leaves several slots empty for the player to fetch,
        // exactly one of them looked highlighted while the rest didn't,
        // suggesting kit-state semantics that aren't there. The amber
        // is a "use this with left-click" indicator; nothing's usable
        // in an empty slot, so plain ROW reads correctly.
        if (slot.selected() && slot.occupied()) {
            return ACTIVE_HOTBAR;
        }
        return ROW;
    }

    Button slotButton(SlotWorkspaceViewModel.HotbarSlot slot) {
        Button button = button("", true, slotChromeColor(slot));
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
                if (event.button == 0) {
                    host.rpc.sendCrossSurfaceQuickMoveHotbar(slot.hotbarIndex());
                } else if (event.button == 1) {
                    host.rpc.sendReturnHotbarToHome(slot.hotbarIndex());
                }
                return;
            }
            if (!slot.occupied()) {
                host.localStatus.set("belt " + (slot.hotbarIndex() + 1) + " is empty");
                return;
            }
            host.localStatus.set("");
        });
        // Capture-phase real cursor handler: while carrying the menu cursor,
        // left-click drops all/merge/swap and right-click drops one (vanilla
        // PICKUP semantics on a player-inventory slot). Beats the legacy
        // virtual-cursor path below + the right-click context menu.
        button.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (!WorkspaceCursorState.isCarrying()) {
                return;
            }
            if (event.button != 0 && event.button != 1) {
                return;
            }
            event.stopPropagation();
            host.rpc.sendDropCursorAtHotbar(slot.hotbarIndex(), event.button);
        }, true);
        // Capture-phase ctrl+right-click pickup-half: extracts half of the
        // hotbar slot's stack onto the menu cursor. Beats the bubble-phase
        // right-click context menu.
        button.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button != 1 || !Screen.hasControlDown()) {
                return;
            }
            if (!slot.occupied()) {
                return;
            }
            ItemStack stack = slot.displayStack();
            SlotWorkspaceViewModel.IdentityRef identity =
                    SlotWorkspaceViewModel.IdentityRef.from(
                            dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(stack));
            SlotWorkspaceViewModel.IdentityRef cursorId = WorkspaceCursorState.carriedIdentity();
            boolean cursorEmpty = cursorId == null;
            boolean cursorMatches = cursorId != null && cursorId.equals(identity);
            if (cursorEmpty || cursorMatches) {
                event.stopPropagation();
                int half = Math.max(1, stack.getMaxStackSize() / 2);
                host.rpc.sendPickupToCursor(identity, half);
            }
        }, true);
        button.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button == 1 && slot.occupied()) {
                // Right-click while carrying = universal cancel; let it bubble.
                if (WorkspaceCursorState.isCarrying()) {
                    return;
                }
                if (Screen.hasShiftDown()) {
                    event.stopPropagation();
                    return;
                }
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

        UIElement iconSlot;
        if (slot.occupied()) {
            iconSlot = itemSlotCard(slot.displayStack(), 16, 0x00000000, true, slot.count());
        } else {
            // Kit-needed ghost: when an active kit declares an item for
            // this hotbar position but the slot is currently empty, paint
            // a faded preview of the needed item so the player sees what
            // the slot is for. Without this, an empty slot looks
            // identical whether the kit needs nothing or needs an item
            // the player still has to fetch from a chest.
            ItemStack kitNeeded = kitNeededDisplayStackFor(slot.hotbarIndex());
            iconSlot = kitNeeded.isEmpty()
                    ? emptyIcon()
                    : itemSlotCard(kitNeeded, 16, 0x00000000, false, 0);
        }
        iconSlot.layout(layout -> layout.width(16).height(16));
        button.addChild(iconSlot);
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


    /**
     * Display stack the active kit's current page declares for a given
     * hotbar index, or {@link ItemStack#EMPTY} when no kit is active or
     * the kit doesn't bind that slot. Drives the ghost-on-empty-slot
     * preview in {@link #slotButton} so the belt visibly anticipates
     * what's still missing right after activation.
     */
    private ItemStack kitNeededDisplayStackFor(int hotbarIndex) {
        SlotWorkspaceViewModel.KitCard active = host.viewModel.activeKit();
        if (active == null) {
            return ItemStack.EMPTY;
        }
        for (SlotWorkspaceViewModel.KitSlotState slotState : active.slots()) {
            if (slotState.slotIndex() != hotbarIndex) {
                continue;
            }
            ItemStack stack = slotState.displayStack();
            return stack == null ? ItemStack.EMPTY : stack;
        }
        return ItemStack.EMPTY;
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
        UIElement iconSlot = offhand.occupied()
                ? itemSlotCard(offhand.displayStack(), 16, 0x00000000, true, offhand.count())
                : emptyIcon();
        iconSlot.layout(layout -> layout.width(16).height(16));
        button.addChild(iconSlot);
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
