package dev.imagio.slot.neoforge.screen.ldlib;

import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceFormat.compactCount;
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
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
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
                host.rpc.sendReturnHotbarToHome(slot.hotbarIndex());
                return;
            }
            SlotWorkspaceViewModel.AtlasItem atlasItem = host.selectedAtlasItem();
            if (atlasItem != null) {
                host.rpc.sendAssignToHotbarSlot(atlasItem, slot.hotbarIndex());
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
        // Capture-phase cursor handler: runs before setOnClick / right-click
        // context menu so a ctrl+right-click pickup or a non-empty cursor
        // drop preempts the slot's normal selection / context-menu behaviour.
        button.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (handleCursorGesture(event, slot)) {
                event.stopPropagation();
            }
        }, true);
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
     * Map a MOUSE_DOWN on this hotbar slot to a cursor pickup or drop, or
     * return false to let the existing select / context-menu handlers run.
     * Pickup is only valid on an occupied slot (nothing to halve from an
     * empty one). Drops are valid on either: empty (placement merges into
     * empty slot via INSERT_ONLY) or same-identity occupied (merges).
     */
    private boolean handleCursorGesture(UIEvent event, SlotWorkspaceViewModel.HotbarSlot slot) {
        WorkspaceCursorGestures.Result mode = WorkspaceCursorGestures.classify(event, host.cursor.isCarrying());
        return switch (mode) {
            case PICKUP_HALF -> {
                if (!slot.occupied()) {
                    yield false;
                }
                ItemStack stack = slot.displayStack();
                SlotWorkspaceViewModel.IdentityRef identity =
                        SlotWorkspaceViewModel.IdentityRef.from(
                                dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(stack));
                boolean picked = host.cursor.pickupHalf(
                        BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0,
                        slot.hotbarIndex(),
                        identity,
                        stack,
                        slot.count());
                if (picked) {
                    host.localStatus.set("cursor: " + host.cursor.current().count() + " " + itemName(stack));
                    host.rebuild();
                } else if (host.cursor.isCarrying()) {
                    // Carrying something else — refuse and surface why so the
                    // player isn't left wondering whether the click registered.
                    host.localStatus.set("cursor already holds another item — drop or ESC first");
                    host.rebuild();
                }
                // Always stopPropagation when ctrl+right was a deliberate
                // pickup attempt, even if refused; otherwise the existing
                // right-click context menu would pop up unexpectedly.
                yield true;
            }
            case DROP_ALL, DROP_ONE, DROP_HALF -> {
                WorkspaceCursorCarry.State state = host.cursor.current();
                if (state == null) {
                    yield false;
                }
                // Self-drop (cursor origin == this slot): the items never
                // physically moved during pickup, so the right thing is to
                // cancel the cursor rather than emit a no-op self-transfer
                // RPC and decrement the count.
                if (BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0.equals(state.sourceId())
                        && state.slotIndex() == slot.hotbarIndex()) {
                    host.cursor.clear();
                    host.localStatus.set("cursor cancelled");
                    host.rebuild();
                    yield true;
                }
                int requested = host.cursor.dropCount(toDropMode(mode));
                if (requested <= 0) {
                    yield false;
                }
                // Client-side capacity + identity clamp. Without it, the
                // server rejects (different item) or accepts only what fits
                // (full slot), but the client decrements the cursor by the
                // full requested amount, so the cursor reads empty while
                // the items are still at the origin. The clamp keeps
                // client/server in sync.
                int capped = clampDropToHotbar(slot, state, requested);
                if (capped <= 0) {
                    host.localStatus.set(slot.occupied()
                            ? "slot " + (slot.hotbarIndex() + 1) + " full or different item"
                            : "drop refused");
                    host.rebuild();
                    yield true;
                }
                host.rpc.sendCursorDropToHotbar(state, slot.hotbarIndex(), capped);
                host.cursor.consume(capped);
                yield true;
            }
            case NONE -> false;
        };
    }

    private static WorkspaceCursorCarry.DropMode toDropMode(WorkspaceCursorGestures.Result result) {
        return switch (result) {
            case DROP_ALL -> WorkspaceCursorCarry.DropMode.ALL;
            case DROP_ONE -> WorkspaceCursorCarry.DropMode.ONE;
            case DROP_HALF -> WorkspaceCursorCarry.DropMode.HALF;
            default -> WorkspaceCursorCarry.DropMode.ALL;
        };
    }

    /**
     * Clamp the requested drop count by the target hotbar slot's actual
     * capacity. Returns 0 if the slot is occupied with a different
     * (non-mergeable) identity — caller surfaces a status and skips the
     * RPC. Empty slots accept up to the cursor stack's max stack size.
     */
    private static int clampDropToHotbar(
            SlotWorkspaceViewModel.HotbarSlot slot,
            WorkspaceCursorCarry.State state,
            int requested
    ) {
        if (state == null || requested <= 0) {
            return 0;
        }
        int max = Math.max(1, state.displayStack().getMaxStackSize());
        if (!slot.occupied()) {
            return Math.min(requested, max);
        }
        if (!dev.imagio.slot.inventory.core.ItemIdentityMatcher.matchesMovable(
                slot.displayStack(), state.identity().toIdentity())) {
            return 0;
        }
        int room = Math.max(0, max - slot.count());
        return Math.min(requested, room);
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
