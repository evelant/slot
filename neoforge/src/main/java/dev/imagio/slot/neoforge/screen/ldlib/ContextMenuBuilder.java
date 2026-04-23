package dev.imagio.slot.neoforge.screen.ldlib;

import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceFormat.*;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.*;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceUi.*;

import com.lowdragmc.lowdraglib2.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceAtlasLayout;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

final class ContextMenuBuilder {
    private final SlotWorkspaceUiController host;

    ContextMenuBuilder(SlotWorkspaceUiController host) {
        this.host = host;
    }

    void beginChestLinkEdit(SlotWorkspaceViewModel.ClaimedChestTile tile) {
        if (tile == null) {
            return;
        }
        host.editingChestStorageId = tile.storageId();
        host.localStatus.set("linking " + tile.label());
        host.rebuild();
    }

    void endChestLinkEdit() {
        host.editingChestStorageId = null;
        host.rebuild();
    }

    UIElement chestLinkPopover() {
        if (host.editingChestStorageId == null) {
            return null;
        }
        SlotWorkspaceViewModel.ClaimedChestTile tile = host.viewModel.claimedChestTile(host.editingChestStorageId);
        if (tile == null) {
            host.editingChestStorageId = null;
            return null;
        }

        UIElement capsule = panel(GLASS).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .right(10)
                .top(10)
                .width(250)
                .paddingAll(8)
                .gapAll(6)
                .flexDirection(FlexDirection.COLUMN));
        capsule.style(style -> style.zIndex(20));
        capsule.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());

        UIElement titleRow = new UIElement().layout(layout -> layout
                .widthPercent(100)
                .height(16)
                .gapAll(6)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.ROW));
        Label title = label("Manage chest", ACCENT);
        title.layout(layout -> layout.flex(1).height(12));
        Button close = button("x", true, PANEL_ALT);
        close.layout(layout -> layout.width(18).height(14));
        close.setOnClick(event -> {
            event.stopPropagation();
            endChestLinkEdit();
        });
        titleRow.addChildren(title, close);
        capsule.addChild(titleRow);

        TextField nameInput = new TextField();
        nameInput.setAnyString();
        nameInput.setText(tile.label(), false);
        nameInput.layout(layout -> layout.widthPercent(100).height(20));
        nameInput.style(style -> style.backgroundTexture(rect(0xC60D1318)));
        nameInput.textFieldStyle(style -> style
                .font(FONT_UI)
                .placeholder(Component.literal("Chest name"))
                .textColor(TEXT)
                .cursorColor(ACCENT)
                .textShadow(false)
                .fontSize(10));
        String currentStorageId = tile.storageId();
        String currentLabel = tile.label();
        nameInput.setTextResponder(value -> {
            String next = value == null ? "" : value;
            String trimmed = next.trim();
            if (trimmed.equals(currentLabel)) {
                return;
            }
            if (host.rpc.relabelChestEmitter != null) {
                host.rpc.relabelChestEmitter.send(currentStorageId, trimmed);
            }
        });
        capsule.addChild(nameInput);

        List<SlotWorkspaceViewModel.AtlasIsland> playerIslands = new ArrayList<>();
        for (SlotWorkspaceViewModel.AtlasIsland island : host.viewModel.islands()) {
            if (island.kind() == VisualAtlasIslandKind.PLAYER) {
                playerIslands.add(island);
            }
        }
        if (playerIslands.isEmpty()) {
            Label hint = label("No host.player islands yet — create one first", MUTED);
            hint.layout(layout -> layout.widthPercent(100).height(14));
            capsule.addChild(hint);
            return capsule;
        }

        for (SlotWorkspaceViewModel.AtlasIsland island : playerIslands) {
            boolean linked = tile.linkedIslandIds().contains(island.islandId());
            UIElement row = new UIElement().layout(layout -> layout
                    .widthPercent(100)
                    .height(18)
                    .gapAll(6)
                    .alignItems(AlignItems.CENTER)
                    .flexDirection(FlexDirection.ROW));
            Label name = label(island.label(), linked ? ACCENT : TEXT);
            name.layout(layout -> layout.flex(1).height(12));
            Button action = button(linked ? "Unlink" : "Link", true, linked ? PANEL_ALT : ROW);
            action.layout(layout -> layout.width(54).height(14));
            String islandId = island.islandId();
            String storageId = tile.storageId();
            action.setOnClick(event -> {
                event.stopPropagation();
                if (linked) {
                    host.sendUnlinkChest(islandId, storageId);
                } else {
                    host.sendLinkChest(islandId, storageId);
                }
            });
            row.addChildren(name, action);
            capsule.addChild(row);
        }
        return capsule;
    }

    void openContextMenuForAtlas(SlotWorkspaceViewModel.AtlasItem item, float screenX, float screenY) {
        if (item == null) {
            return;
        }
        host.contextMenuAtlasIdentity = item.identity();
        host.contextMenuHotbarIndex = -1;
        host.contextMenuScreenX = screenX;
        host.contextMenuScreenY = screenY;
        host.rebuild();
    }

    void openContextMenuForHotbar(SlotWorkspaceViewModel.HotbarSlot slot, float screenX, float screenY) {
        if (slot == null || !slot.occupied()) {
            return;
        }
        host.contextMenuHotbarIndex = slot.hotbarIndex();
        host.contextMenuAtlasIdentity = null;
        host.contextMenuKitId = null;
        host.contextMenuScreenX = screenX;
        host.contextMenuScreenY = screenY;
        host.rebuild();
    }

    void openContextMenuForKit(String kitId, float screenX, float screenY) {
        if (kitId == null || kitId.isBlank()) {
            return;
        }
        host.contextMenuKitId = kitId;
        host.contextMenuAtlasIdentity = null;
        host.contextMenuHotbarIndex = -1;
        host.renamingKitId = null;
        host.renameKitDraft = "";
        host.confirmDeleteKitId = null;
        host.contextMenuScreenX = screenX;
        host.contextMenuScreenY = screenY;
        host.rebuild();
    }

    void closeContextMenu() {
        host.contextMenuAtlasIdentity = null;
        host.contextMenuHotbarIndex = -1;
        host.contextMenuKitId = null;
        host.renamingKitId = null;
        host.renameKitDraft = "";
        host.confirmDeleteKitId = null;
        host.rebuild();
    }

    UIElement contextMenuOverlay() {
        if (host.contextMenuAtlasIdentity != null) {
            SlotWorkspaceViewModel.AtlasItem item = host.viewModel.atlasItem(host.contextMenuAtlasIdentity);
            if (item == null) {
                host.contextMenuAtlasIdentity = null;
                return null;
            }
            return buildAtlasContextMenu(item);
        }
        if (host.contextMenuHotbarIndex >= 0 && host.contextMenuHotbarIndex < host.viewModel.hotbarSlots().size()) {
            SlotWorkspaceViewModel.HotbarSlot slot = host.viewModel.hotbarSlots().get(host.contextMenuHotbarIndex);
            if (!slot.occupied()) {
                host.contextMenuHotbarIndex = -1;
                return null;
            }
            return buildHotbarContextMenu(slot);
        }
        if (host.contextMenuKitId != null) {
            SlotWorkspaceViewModel.KitCard card = host.viewModel.kit(host.contextMenuKitId);
            if (card == null) {
                closeContextMenu();
                return null;
            }
            return buildKitContextMenu(card);
        }
        return null;
    }

    UIElement contextMenuCatcher(Runnable onDismiss) {
        UIElement catcher = new UIElement().layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(0).right(0).top(0).bottom(0));
        catcher.style(style -> style.zIndex(21));
        catcher.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            event.stopPropagation();
            onDismiss.run();
        });
        return catcher;
    }

    UIElement buildAtlasContextMenu(SlotWorkspaceViewModel.AtlasItem item) {
        UIElement catcher = contextMenuCatcher(this::closeContextMenu);
        UIElement menu = panel(GLASS).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .width(160)
                .paddingAll(4)
                .gapAll(2)
                .flexDirection(FlexDirection.COLUMN));
        anchorPopover(menu, host.contextMenuScreenX, host.contextMenuScreenY, 160, 96);
        menu.style(style -> style.zIndex(22));
        menu.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());

        menu.addChild(label(shorten(item.name(), 22), ACCENT)
                .layout(layout -> layout.widthPercent(100).height(12)));

        int freeHotbarIndex = host.firstFreeHotbarIndex();
        if (item.carried() && freeHotbarIndex >= 0) {
            menu.addChild(menuButton(
                    "Send to hotbar",
                    true,
                    null,
                    () -> {
                        host.sendAssignHomeToHotbarOnly(item);
                        closeContextMenu();
                    }
            ));
        }

        if (item.carried() && host.atlasItemHasDepositTarget(item)) {
            menu.addChild(menuButton(
                    "Deposit to linked chest",
                    true,
                    null,
                    () -> {
                        host.sendDepositHomeToLinkedChest(item);
                        closeContextMenu();
                    }
            ));
        }

        List<SlotWorkspaceViewModel.AtlasIsland> recent = host.recentRehomeTargets(item);
        for (SlotWorkspaceViewModel.AtlasIsland target : recent) {
            String targetIslandId = target.islandId();
            menu.addChild(menuButton(
                    "Move to " + shorten(target.label(), 18),
                    true,
                    null,
                    () -> {
                        host.sendAssignHome(item.identity(), targetIslandId, 0, 0);
                        closeContextMenu();
                    }
            ));
        }

        menu.addChild(menuButton("Cancel", true, null, this::closeContextMenu));

        UIElement wrapper = new UIElement().layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(0).right(0).top(0).bottom(0));
        wrapper.addChildren(catcher, menu);
        return wrapper;
    }

    UIElement buildHotbarContextMenu(SlotWorkspaceViewModel.HotbarSlot slot) {
        UIElement catcher = contextMenuCatcher(this::closeContextMenu);
        UIElement menu = panel(GLASS).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .width(160)
                .paddingAll(4)
                .gapAll(2)
                .flexDirection(FlexDirection.COLUMN));
        anchorPopover(menu, host.contextMenuScreenX, host.contextMenuScreenY, 160, 80);
        menu.style(style -> style.zIndex(22));
        menu.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());

        String name = slot.displayStack() == null || slot.displayStack().isEmpty()
                ? "hotbar " + (slot.hotbarIndex() + 1)
                : slot.displayStack().getHoverName().getString();
        menu.addChild(label(shorten(name, 22), ACCENT)
                .layout(layout -> layout.widthPercent(100).height(12)));

        int hotbarIdx = slot.hotbarIndex();
        menu.addChild(menuButton(
                "Send to home",
                true,
                null,
                () -> {
                    host.sendReturnHotbarToHome(hotbarIdx);
                    closeContextMenu();
                }
        ));

        menu.addChild(menuButton("Cancel", true, null, this::closeContextMenu));

        UIElement wrapper = new UIElement().layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(0).right(0).top(0).bottom(0));
        wrapper.addChildren(catcher, menu);
        return wrapper;
    }

    UIElement buildKitContextMenu(SlotWorkspaceViewModel.KitCard card) {
        UIElement catcher = contextMenuCatcher(this::closeContextMenu);
        UIElement menu = panel(GLASS).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .width(180)
                .paddingAll(6)
                .gapAll(4)
                .flexDirection(FlexDirection.COLUMN));
        int approxHeight = 80;
        if (card.kitId().equals(host.renamingKitId)) {
            approxHeight = 70;
        } else if (card.kitId().equals(host.confirmDeleteKitId)) {
            approxHeight = 64;
        }
        anchorPopover(menu, host.contextMenuScreenX, host.contextMenuScreenY, 180, approxHeight);
        menu.style(style -> style.zIndex(22));
        menu.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());

        menu.addChild(label(shorten(card.name(), 22), ACCENT)
                .layout(layout -> layout.widthPercent(100).height(12)));

        if (card.kitId().equals(host.renamingKitId)) {
            appendKitRenameBody(menu, card);
        } else if (card.kitId().equals(host.confirmDeleteKitId)) {
            appendKitDeleteConfirmBody(menu, card);
        } else {
            menu.addChild(menuButton("Rename\u2026", true, null, () -> {
                host.renamingKitId = card.kitId();
                host.renameKitDraft = card.name();
                host.rebuild();
            }));
            menu.addChild(menuButton("Duplicate", true, null, () -> {
                host.sendDuplicateKit(card.kitId());
                closeContextMenu();
            }));
            menu.addChild(menuButton("Delete\u2026", true, null, () -> {
                host.confirmDeleteKitId = card.kitId();
                host.rebuild();
            }));
        }

        UIElement wrapper = new UIElement().layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(0).right(0).top(0).bottom(0));
        wrapper.addChildren(catcher, menu);
        return wrapper;
    }

    void appendKitRenameBody(UIElement menu, SlotWorkspaceViewModel.KitCard card) {
        TextField nameInput = new TextField();
        nameInput.setAnyString();
        nameInput.setText(host.renameKitDraft, false);
        nameInput.layout(layout -> layout.widthPercent(100).height(18));
        nameInput.style(style -> style.backgroundTexture(rect(0xC60D1318)));
        nameInput.textFieldStyle(style -> style
                .font(FONT_UI)
                .placeholder(Component.literal("Kit name"))
                .textColor(TEXT)
                .cursorColor(ACCENT)
                .textShadow(false)
                .fontSize(9));
        nameInput.setTextResponder(value -> host.renameKitDraft = value == null ? "" : value);
        Runnable commit = () -> {
            String trimmed = host.renameKitDraft == null ? "" : host.renameKitDraft.trim();
            if (trimmed.isBlank() || trimmed.equals(card.name())) {
                closeContextMenu();
                return;
            }
            if (host.rpc.renameKitEmitter != null) {
                host.rpc.renameKitEmitter.send(card.kitId(), trimmed);
            }
            closeContextMenu();
        };
        nameInput.addEventListener(UIEvents.KEY_DOWN, event -> {
            if (event.keyCode == GLFW.GLFW_KEY_ENTER || event.keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                commit.run();
                event.stopPropagation();
            } else if (event.keyCode == GLFW.GLFW_KEY_ESCAPE) {
                closeContextMenu();
                event.stopPropagation();
            }
        });
        menu.addChild(nameInput);
        UIElement row = new UIElement().layout(layout -> layout
                .widthPercent(100)
                .height(14)
                .gapAll(4)
                .flexDirection(FlexDirection.ROW));
        Button save = button("Save", true, ACCENT);
        save.layout(layout -> layout.flex(1).height(14));
        save.textStyle(style -> style.textColor(TEXT).textShadow(false).fontSize(8)
                .textAlignHorizontal(Horizontal.CENTER).textAlignVertical(Vertical.CENTER));
        save.setOnClick(event -> {
            event.stopPropagation();
            commit.run();
        });
        Button cancel = button("Cancel", true, PANEL_ALT);
        cancel.layout(layout -> layout.flex(1).height(14));
        cancel.textStyle(style -> style.textColor(MUTED).textShadow(false).fontSize(8)
                .textAlignHorizontal(Horizontal.CENTER).textAlignVertical(Vertical.CENTER));
        cancel.setOnClick(event -> {
            event.stopPropagation();
            closeContextMenu();
        });
        row.addChildren(save, cancel);
        menu.addChild(row);
    }

    void appendKitDeleteConfirmBody(UIElement menu, SlotWorkspaceViewModel.KitCard card) {
        Label prompt = label("Delete " + shorten(card.name(), 18) + "?", WARNING);
        prompt.layout(layout -> layout.widthPercent(100).height(12));
        prompt.textStyle(style -> style.textColor(WARNING).textShadow(false).fontSize(8)
                .textAlignHorizontal(Horizontal.LEFT).textAlignVertical(Vertical.CENTER));
        prompt.setAllowHitTest(false);
        menu.addChild(prompt);
        UIElement row = new UIElement().layout(layout -> layout
                .widthPercent(100)
                .height(14)
                .gapAll(4)
                .flexDirection(FlexDirection.ROW));
        Button confirm = button("Delete", true, WARNING);
        confirm.layout(layout -> layout.flex(1).height(14));
        confirm.textStyle(style -> style.textColor(TEXT).textShadow(true).fontSize(8)
                .textAlignHorizontal(Horizontal.CENTER).textAlignVertical(Vertical.CENTER));
        confirm.setOnClick(event -> {
            event.stopPropagation();
            host.sendDeleteKit(card.kitId());
            closeContextMenu();
        });
        Button cancel = button("Cancel", true, PANEL_ALT);
        cancel.layout(layout -> layout.flex(1).height(14));
        cancel.textStyle(style -> style.textColor(MUTED).textShadow(false).fontSize(8)
                .textAlignHorizontal(Horizontal.CENTER).textAlignVertical(Vertical.CENTER));
        cancel.setOnClick(event -> {
            event.stopPropagation();
            closeContextMenu();
        });
        row.addChildren(confirm, cancel);
        menu.addChild(row);
    }

    Button menuButton(String text, boolean enabled, String disabledHint, Runnable onClick) {
        String label = enabled || disabledHint == null ? text : text + " (" + disabledHint + ")";
        Button button = button(label, enabled, enabled ? ROW : PANEL_ALT);
        button.layout(layout -> layout.widthPercent(100).height(14));
        button.textStyle(style -> style
                .textColor(enabled ? TEXT : MUTED)
                .textShadow(false)
                .fontSize(7)
                .textAlignHorizontal(Horizontal.LEFT)
                .textAlignVertical(Vertical.CENTER));
        if (enabled) {
            button.setOnClick(event -> {
                event.stopPropagation();
                if (event.button != 0) {
                    return;
                }
                onClick.run();
            });
        }
        return button;
    }

    void anchorPopover(UIElement menu, float screenX, float screenY, int width, int approxHeight) {
        float originX = host.atlasPanelElement != null ? host.atlasPanelElement.getPositionX() : 0f;
        float originY = host.atlasPanelElement != null ? host.atlasPanelElement.getPositionY() : 0f;
        int left = Math.max(4, Math.round(screenX - originX) + 4);
        int top = Math.max(4, Math.round(screenY - originY) + 4);
        menu.layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(left)
                .top(top)
                .width(width));
    }

    void beginIslandEdit(SlotWorkspaceViewModel.AtlasIsland island, float screenX, float screenY) {
        if (island == null) {
            return;
        }
        host.editingIslandId = island.islandId();
        host.islandLabelDraft = island.label();
        host.islandEditScreenX = screenX;
        host.islandEditScreenY = screenY;
        host.localStatus.set("editing " + island.label());
        host.rebuild();
    }

    void endIslandEdit() {
        host.editingIslandId = null;
        host.islandLabelDraft = "";
        host.islandEditScreenX = Float.NaN;
        host.islandEditScreenY = Float.NaN;
        host.rebuild();
    }

    UIElement islandEditPopover() {
        if (host.editingIslandId == null) {
            return null;
        }
        SlotWorkspaceViewModel.AtlasIsland island = host.viewModel.island(host.editingIslandId);
        if (island == null || island.kind() != VisualAtlasIslandKind.PLAYER) {
            host.editingIslandId = null;
            host.islandLabelDraft = "";
            return null;
        }

        // Fullscreen catcher dismisses the popover when the user clicks
        // anywhere outside the capsule — matches the context-menu pattern
        // (buildAtlasContextMenu at ~3298) and replaces the old dedicated
        // "x" close button. Catcher sits just below the capsule's zIndex
        // so the capsule's own MOUSE_DOWN (stopPropagation) wins inside.
        UIElement catcher = contextMenuCatcher(this::endIslandEdit);

        UIElement capsule = panel(GLASS).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .width(250)
                .paddingAll(8)
                .gapAll(6)
                .flexDirection(FlexDirection.COLUMN));
        anchorPopover(capsule, host.islandEditScreenX, host.islandEditScreenY, 250, 240);
        capsule.style(style -> style.zIndex(22));
        capsule.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());

        Label title = label("Edit island", ACCENT);
        title.layout(layout -> layout.widthPercent(100).height(12));
        capsule.addChild(title);

        TextField nameInput = new TextField();
        nameInput.setAnyString();
        nameInput.setText(host.islandLabelDraft, false);
        nameInput.layout(layout -> layout.widthPercent(100).height(20));
        nameInput.style(style -> style.backgroundTexture(rect(0xC60D1318)));
        nameInput.textFieldStyle(style -> style
                .font(FONT_UI)
                .placeholder(Component.literal("Island name"))
                .textColor(TEXT)
                .cursorColor(ACCENT)
                .textShadow(false)
                .fontSize(10));
        nameInput.setTextResponder(value -> host.islandLabelDraft = value == null ? "" : value);
        Runnable commitRename = () -> {
            if (host.editingIslandId == null) {
                return;
            }
            String trimmed = host.islandLabelDraft == null ? "" : host.islandLabelDraft.trim();
            if (trimmed.isBlank() || trimmed.equals(island.label())) {
                return;
            }
            if (host.rpc.renameIslandEmitter != null) {
                host.rpc.renameIslandEmitter.send(host.editingIslandId, trimmed);
            }
        };
        nameInput.addEventListener(UIEvents.BLUR, event -> commitRename.run());
        nameInput.addEventListener(UIEvents.KEY_DOWN, event -> {
            if (event.keyCode == GLFW.GLFW_KEY_ENTER || event.keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                commitRename.run();
                event.stopPropagation();
            }
        });
        capsule.addChild(nameInput);

        capsule.addChild(label("Color", MUTED).layout(layout -> layout.height(10)));
        UIElement paletteRow = new UIElement().layout(layout -> layout
                .widthPercent(100)
                .height(18)
                .gapAll(4)
                .flexDirection(FlexDirection.ROW));
        for (int color : ISLAND_PALETTE) {
            boolean selected = color == island.color();
            Button swatch = button("", true, color);
            swatch.layout(layout -> layout.flex(1).height(18));
            swatch.noText();
            if (selected) {
                swatch.style(style -> style.zIndex(1));
            }
            int finalColor = color;
            swatch.setOnClick(event -> {
                event.stopPropagation();
                if (finalColor == island.color()) {
                    return;
                }
                boolean sent = host.rpc.recolorIslandEmitter != null && host.rpc.recolorIslandEmitter.send(host.editingIslandId, finalColor);
                host.localStatus.set(sent ? "recolor requested" : "recolor unavailable");
                host.rebuild();
            });
            paletteRow.addChild(swatch);
        }
        capsule.addChild(paletteRow);

        SlotWorkspaceViewModel.AtlasItem selected = host.selectedAtlasItem();
        boolean canSetIcon = selected != null;
        Button setIcon = button(
                canSetIcon ? "Set icon: " + shorten(selected.name(), 16) : "Select an item to set icon",
                canSetIcon
        );
        setIcon.layout(layout -> layout.widthPercent(100).height(18));
        setIcon.setOnClick(event -> {
            event.stopPropagation();
            if (selected == null) {
                host.localStatus.set("select an atlas item first");
                host.rebuild();
                return;
            }
            boolean sent = host.rpc.setIslandIconEmitter != null && host.rpc.setIslandIconEmitter.send(
                    host.editingIslandId,
                    selected.identity().itemId(),
                    selected.identity().comparisonMode(),
                    selected.identity().componentFingerprint()
            );
            host.localStatus.set(sent ? "set icon requested" : "set icon unavailable");
            host.rebuild();
        });
        capsule.addChild(setIcon);

        Button clearIcon = button("Clear icon", true);
        clearIcon.layout(layout -> layout.widthPercent(100).height(18));
        clearIcon.setOnClick(event -> {
            event.stopPropagation();
            boolean sent = host.rpc.setIslandIconEmitter != null && host.rpc.setIslandIconEmitter.send(host.editingIslandId, "", "", "");
            host.localStatus.set(sent ? "clear icon requested" : "clear icon unavailable");
            host.rebuild();
        });
        capsule.addChild(clearIcon);

        boolean empty = island.itemCount() == 0;
        Button deleteButton = button(empty ? "Delete island" : "Delete (move items first)", empty);
        deleteButton.layout(layout -> layout.widthPercent(100).height(18));
        deleteButton.setOnClick(event -> {
            event.stopPropagation();
            if (!empty) {
                host.localStatus.set("move all items off this island first");
                host.rebuild();
                return;
            }
            boolean sent = host.rpc.deleteIslandEmitter != null && host.rpc.deleteIslandEmitter.send(host.editingIslandId);
            host.localStatus.set(sent ? "delete requested" : "delete unavailable");
            if (sent) {
                endIslandEdit();
                return;
            }
            host.rebuild();
        });
        capsule.addChild(deleteButton);

        UIElement wrapper = new UIElement().layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(0).right(0).top(0).bottom(0));
        wrapper.addChildren(catcher, capsule);
        return wrapper;
    }

    void beginCreateIsland(SlotWorkspaceViewModel.AtlasItem item, int worldX, int worldY) {
        if (item == null) {
            return;
        }
        host.pendingCreateIdentity = item.identity();
        host.pendingCreateWorldX = worldX;
        host.pendingCreateWorldY = worldY;
        host.pendingCreateLabel = item.name();
        host.pendingCreateColor = ISLAND_PALETTE[0];
        host.pendingCreateFocusPending = true;
        host.localStatus.set("name the new island");
        host.rebuild();
    }

    void endCreateIsland() {
        host.pendingCreateIdentity = null;
        host.pendingCreateLabel = "";
        host.pendingCreateColor = ISLAND_PALETTE[0];
        host.pendingCreateFocusPending = false;
        host.rebuild();
    }

    UIElement createIslandPopover() {
        if (host.pendingCreateIdentity == null) {
            return null;
        }
        SlotWorkspaceViewModel.AtlasItem item = host.viewModel.atlasItem(host.pendingCreateIdentity);
        if (item == null) {
            host.pendingCreateIdentity = null;
            return null;
        }

        UIElement capsule = panel(GLASS).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .right(10)
                .top(10)
                .width(260)
                .paddingAll(8)
                .gapAll(6)
                .flexDirection(FlexDirection.COLUMN));
        capsule.style(style -> style.zIndex(20));
        capsule.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());

        UIElement titleRow = new UIElement().layout(layout -> layout
                .widthPercent(100)
                .height(16)
                .gapAll(6)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.ROW));
        Label title = label("New island for " + shorten(item.name(), 18), ACCENT);
        title.layout(layout -> layout.flex(1).height(12));
        Button close = button("x", true, PANEL_ALT);
        close.layout(layout -> layout.width(18).height(14));
        close.setOnClick(event -> {
            event.stopPropagation();
            endCreateIsland();
        });
        titleRow.addChildren(title, close);
        capsule.addChild(titleRow);

        TextField nameInput = new TextField();
        nameInput.setAnyString();
        nameInput.setText(host.pendingCreateLabel, false);
        nameInput.layout(layout -> layout.widthPercent(100).height(20));
        nameInput.style(style -> style.backgroundTexture(rect(0xC60D1318)));
        nameInput.textFieldStyle(style -> style
                .font(FONT_UI)
                .placeholder(Component.literal("Island name"))
                .textColor(TEXT)
                .cursorColor(ACCENT)
                .textShadow(false)
                .fontSize(10));
        nameInput.setTextResponder(value -> host.pendingCreateLabel = value == null ? "" : value);
        if (host.pendingCreateFocusPending) {
            host.pendingCreateFocusPending = false;
            nameInput.addEventListener(UIEvents.LAYOUT_CHANGED, event -> {
                nameInput.focus();
                String current = nameInput.getValue();
                int length = current == null ? 0 : current.length();
                nameInput.setCursor(length);
                nameInput.setSelection(0, length);
            }, true);
        }
        capsule.addChild(nameInput);

        capsule.addChild(label("Color", MUTED).layout(layout -> layout.height(10)));
        UIElement paletteRow = new UIElement().layout(layout -> layout
                .widthPercent(100)
                .height(18)
                .gapAll(4)
                .flexDirection(FlexDirection.ROW));
        for (int color : ISLAND_PALETTE) {
            boolean selected = color == host.pendingCreateColor;
            Button swatch = button("", true, color);
            swatch.layout(layout -> layout.flex(1).height(18));
            swatch.noText();
            if (selected) {
                swatch.style(style -> style.zIndex(1));
            }
            int finalColor = color;
            swatch.setOnClick(event -> {
                event.stopPropagation();
                host.pendingCreateColor = finalColor;
                host.rebuild();
            });
            paletteRow.addChild(swatch);
        }
        capsule.addChild(paletteRow);

        UIElement actionRow = new UIElement().layout(layout -> layout
                .widthPercent(100)
                .height(20)
                .gapAll(6)
                .flexDirection(FlexDirection.ROW));
        Button cancel = button("Cancel", true, PANEL_ALT);
        cancel.layout(layout -> layout.flex(1).height(20));
        cancel.setOnClick(event -> {
            event.stopPropagation();
            endCreateIsland();
        });
        boolean nameReady = host.pendingCreateLabel != null && !host.pendingCreateLabel.trim().isBlank();
        Button create = button("Create", nameReady);
        create.layout(layout -> layout.flex(1).height(20));
        create.setOnClick(event -> {
            event.stopPropagation();
            String trimmed = host.pendingCreateLabel == null ? "" : host.pendingCreateLabel.trim();
            if (trimmed.isBlank()) {
                host.localStatus.set("enter an island name");
                host.rebuild();
                return;
            }
            boolean sent = host.rpc.createNamedIslandEmitter != null && host.rpc.createNamedIslandEmitter.send(
                    host.pendingCreateIdentity.itemId(),
                    host.pendingCreateIdentity.comparisonMode(),
                    host.pendingCreateIdentity.componentFingerprint(),
                    trimmed,
                    host.pendingCreateColor,
                    host.pendingCreateWorldX,
                    host.pendingCreateWorldY
            );
            host.localStatus.set(sent ? "create island requested" : "create island unavailable");
            if (sent) {
                endCreateIsland();
                return;
            }
            host.rebuild();
        });
        actionRow.addChildren(cancel, create);
        capsule.addChild(actionRow);

        return capsule;
    }
}
