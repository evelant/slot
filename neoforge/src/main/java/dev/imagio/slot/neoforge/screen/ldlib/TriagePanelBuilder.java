package dev.imagio.slot.neoforge.screen.ldlib;

import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.*;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceUi.*;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.TaffyPosition;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import dev.imagio.slot.inventory.triage.ChipSuggestion;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceAtlasLayout;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.AtlasItemDrag;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.HotbarSlotDrag;
import net.minecraft.client.gui.screens.Screen;

final class TriagePanelBuilder {
    private final SlotWorkspaceUiController host;

    TriagePanelBuilder(SlotWorkspaceUiController host) {
        this.host = host;
    }

    UIElement overlay() {
        // Clear whichever top-left overlay is showing: the compact "Press / to search"
        // hint (~20 px tall) or the full search modal (~60 px tall at top 10, left 10).
        // Kit rack, when open, docks above the belt and extends rightward from x=16;
        // triage at (x=8, width=152) horizontally overlaps it — lift the bottom above
        // the rack so the two don't visually stack.
        int triageTop = host.searchController.modalActive() ? 78 : 36;
        int baseBottom = BELT_HEIGHT + 12;
        int rackBottom = host.kitRackOpen ? baseBottom + host.kit.kitRackHeight() + 4 : baseBottom;
        UIElement overlay = panel(GLASS).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(8)
                .top(triageTop)
                .bottom(rackBottom)
                .width(TRIAGE_PANEL_WIDTH)
                .paddingAll(6)
                .gapAll(4)
                .flexDirection(FlexDirection.COLUMN));
        overlay.style(style -> style.zIndex(7));
        overlay.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());

        int triageCount = host.viewModel.triageItems().size();
        UIElement headerRow = new UIElement().layout(layout -> layout
                .widthPercent(100)
                .height(14)
                .gapAll(4)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.ROW));
        headerRow.addChildren(
                label("Triage", ACCENT).layout(layout -> layout.flex(1).height(12)),
                label(String.valueOf(triageCount), MUTED).layout(layout -> layout.width(24).height(12))
        );
        headerRow.setAllowHitTest(false);
        overlay.addChild(headerRow);

        Label sortHint = label("most recent first \u2193", MUTED);
        sortHint.layout(layout -> layout.widthPercent(100).height(8));
        sortHint.textStyle(style -> style
                .textColor(MUTED)
                .fontSize(6)
                .textShadow(false)
                .textAlignHorizontal(Horizontal.LEFT)
                .textAlignVertical(Vertical.CENTER));
        sortHint.setAllowHitTest(false);
        overlay.addChild(sortHint);

        UIElement divider = panel(ISLAND_BORDER).layout(layout -> layout.widthPercent(100).height(1));
        divider.setAllowHitTest(false);
        overlay.addChild(divider);

        if (triageCount == 0) {
            Label empty = label("Inbox is empty.", MUTED);
            empty.layout(layout -> layout.widthPercent(100).height(24));
            empty.textStyle(style -> style
                    .textColor(MUTED)
                    .fontSize(7)
                    .textShadow(false)
                    .textAlignHorizontal(Horizontal.LEFT)
                    .textAlignVertical(Vertical.CENTER));
            empty.setAllowHitTest(false);
            overlay.addChild(empty);
        } else {
            ScrollerView scroller = new ScrollerView();
            scroller.layout(layout -> layout.flex(1).widthPercent(100).gapAll(2));
            // LDLib's default per-tick wheel cap is 7px; at our 20px row height that
            // feels glacial. Bump both min + max so each wheel tick scrolls roughly
            // 3 rows.
            scroller.scrollerStyle(style -> style
                    .minScrollPixel(30f)
                    .maxScrollPixel(80f));
            for (SlotWorkspaceViewModel.AtlasItem item : host.viewModel.triageItems()) {
                scroller.addScrollViewChild(row(item));
                for (ChipSuggestion chip : item.chipSuggestions()) {
                    scroller.addScrollViewChild(chip(item, chip));
                }
            }
            overlay.addChild(scroller);
        }

        installDropTarget(overlay);
        return overlay;
    }

    int rowChromeColor(SlotWorkspaceViewModel.AtlasItem item, boolean selected) {
        if (selected) {
            return SELECTED;
        }
        return item.recent() ? ROW_MATCH : ROW;
    }

    UIElement row(SlotWorkspaceViewModel.AtlasItem item) {
        Button row = button("", true, rowChromeColor(item, item.identity().equals(host.selectedAtlasIdentity.get())));
        row.noText();
        host.atlasContentSubscriptions.add(host.selectedAtlasIdentity.subscribeLater(sel -> {
            applyButtonColors(row, true, rowChromeColor(item, item.identity().equals(sel)));
        }));
        boolean[] lastAccent = {false};
        row.addEventListener(UIEvents.TICK, event -> {
            boolean accent = host.shouldAccentTriageRow(item);
            if (accent != lastAccent[0]) {
                row.style(style -> style.overlayTexture(accent ? rect(HOVER_ACCENT_OVERLAY) : IGuiTexture.EMPTY));
                lastAccent[0] = accent;
            }
        });
        row.layout(layout -> layout
                .widthPercent(100)
                .height(20)
                .paddingHorizontal(4)
                .gapAll(4)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.ROW));
        row.setOnClick(event -> {
            event.stopPropagation();
            if (event.button == 0 && Screen.hasShiftDown()) {
                int hotbarIndex = host.hotbarSlotForIdentity(item.identity());
                if (hotbarIndex >= 0) {
                    host.rpc.sendReturnHotbarToHome(hotbarIndex);
                } else {
                    host.localStatus.set(item.name() + " is not in the hotbar");
                    host.rebuild();
                }
                return;
            }
            host.selectedAtlasIdentity.set(item.identity());
            host.selectedHotbarIndex.set(-1);
            host.localStatus.set("selected inbox item: drag to an island, click an island, or accept a chip");
        });
        row.addEventListener(UIEvents.MOUSE_ENTER, event -> host.hoveredAtlasIdentity = item.identity(), true);
        row.addEventListener(UIEvents.MOUSE_LEAVE, event -> {
            if (item.identity().equals(host.hoveredAtlasIdentity)) {
                host.hoveredAtlasIdentity = null;
            }
        }, true);
        host.drag.installAtlasItemDragSource(row, item);
        host.drag.installAtlasHoverTooltip(row, item);

        UIElement icon = itemIcon(item.displayStack(), 16, item.carried());
        icon.layout(layout -> layout.width(16).height(16));
        row.addChild(icon);

        // Item count is already drawn on the icon corner by the vanilla item
        // renderer, so we only need the name. Let LDLib handle any soft clip via
        // the flex(1) layout — we were over-shortening with a hard 22-char cap.
        Label name = label(item.name(), TEXT);
        name.layout(layout -> layout.flex(1).height(12));
        name.textStyle(style -> style
                .textColor(item.carried() ? TEXT : MUTED)
                .fontSize(7)
                .textShadow(false)
                .textAlignHorizontal(Horizontal.LEFT)
                .textAlignVertical(Vertical.CENTER));
        name.setAllowHitTest(false);
        row.addChild(name);
        return row;
    }

    UIElement chip(SlotWorkspaceViewModel.AtlasItem item, ChipSuggestion chip) {
        // Wrapper with a left indent so the chip visibly nests under its parent
        // item instead of spanning the full panel width like a divider.
        UIElement wrapper = new UIElement().layout(layout -> layout
                .widthPercent(100)
                .height(11)
                .paddingLeft(20)
                .flexDirection(FlexDirection.ROW)
                .alignItems(AlignItems.CENTER));
        wrapper.setAllowHitTest(false);

        Button chipButton = button("", true, chip.color());
        chipButton.noText();
        chipButton.layout(layout -> layout
                .flex(1)
                .height(11)
                .paddingHorizontal(4)
                .gapAll(2)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.ROW));
        chipButton.setOnClick(event -> {
            event.stopPropagation();
            host.rpc.sendChipAccept(item, chip);
        });
        // Leading glyph signals "assign to" direction and visually anchors the
        // chip to the item row above. Plain ASCII so it renders in the Inter
        // Tight font without the Unicode-arrow fallback issues.
        Label chipLabel = label("> " + SlotWorkspaceUiController.chipLabelText(chip), TEXT);
        chipLabel.layout(layout -> layout.flex(1).height(9));
        chipLabel.textStyle(style -> style
                .textColor(TEXT)
                .fontSize(6)
                .textShadow(false)
                .textAlignHorizontal(Horizontal.LEFT)
                .textAlignVertical(Vertical.CENTER));
        chipLabel.setAllowHitTest(false);
        chipButton.addChild(chipLabel);
        wrapper.addChild(chipButton);
        return wrapper;
    }

    void installDropTarget(UIElement target) {
        target.addEventListener(UIEvents.DRAG_ENTER, event -> host.drag.updateGenericDropOverlay(target, isDropAcceptable(event), WARNING), true);
        target.addEventListener(UIEvents.DRAG_UPDATE, event -> host.drag.updateGenericDropOverlay(target, isDropAcceptable(event), WARNING));
        target.addEventListener(UIEvents.DRAG_LEAVE, event -> host.drag.clearDropOverlay(target), true);
        target.addEventListener(UIEvents.DRAG_PERFORM, event -> {
            host.drag.clearDropOverlay(target);
            AtlasItemDrag atlasItem = host.drag.atlasItemDrag(event);
            if (atlasItem != null) {
                host.rpc.sendAssignHome(
                        atlasItem.identity(),
                        SlotWorkspaceAtlasLayout.ISLAND_TRIAGE,
                        null
                );
                event.stopPropagation();
                return;
            }
            HotbarSlotDrag hotbarItem = host.drag.hotbarSlotDrag(event);
            if (hotbarItem != null) {
                if (host.drag.hotbarDragHasHome(hotbarItem)) {
                    host.rpc.sendReturnHotbarToHome(hotbarItem.hotbarIndex());
                } else {
                    host.rpc.sendMoveHotbarToAtlas(
                            hotbarItem.hotbarIndex(),
                            SlotWorkspaceAtlasLayout.ISLAND_TRIAGE,
                            null
                    );
                }
                event.stopPropagation();
            }
        });
    }

    boolean isDropAcceptable(UIEvent event) {
        return host.drag.atlasItemDrag(event) != null || host.drag.hotbarSlotDrag(event) != null;
    }
}
