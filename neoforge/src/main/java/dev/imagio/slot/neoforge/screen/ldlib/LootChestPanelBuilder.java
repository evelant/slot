package dev.imagio.slot.neoforge.screen.ldlib;

import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.*;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceUi.*;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import dev.imagio.slot.inventory.triage.ChipSuggestion;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceAtlasLayout;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.client.gui.screens.Screen;

/**
 * Atlas-side panel that surfaces the closest unclaimed nearby chest's
 * contents as Triage-style rows with chip suggestions.
 *
 * <p>Each row is a vertical "card" of stacked tiles sharing one ROW
 * background: the top tile is the clickable / draggable item, and any
 * chip-suggestion or already-homed indicator tile sits flush beneath
 * it (no gap), so chips and home indicators read as part of the same
 * row rather than separate strips.
 *
 * <p>Interactions mirror Triage: left-click selects, drag routes to a
 * drop target (assign home / create island), shift+click takes with
 * auto-accept, "Take all" drains the chest after auto-accepting top
 * chips. Docks immediately above Triage in the same left column;
 * disappears when no loot chest is in range.
 */
final class LootChestPanelBuilder {
    static final int PANEL_WIDTH = TRIAGE_PANEL_WIDTH;
    static final int LEFT = 8;
    static final int HEADER_HEIGHT = 14;
    static final int SUBLABEL_HEIGHT = 8;
    static final int DIVIDER_HEIGHT = 1;
    static final int ROW_HEIGHT = 20;
    static final int SUBROW_HEIGHT = 11;
    static final int FOOTER_HEIGHT = 16;
    static final int PANEL_PADDING = 6;
    static final int PANEL_GAP = 4;
    /** Floor for the loot panel's flex(1) share so it stays usable in tight columns. */
    static final int MIN_HEIGHT = 120;

    private final SlotWorkspaceUiController host;

    LootChestPanelBuilder(SlotWorkspaceUiController host) {
        this.host = host;
    }

    /**
     * Returns null when no loot chest is in range so the panel collapses.
     * Otherwise builds a flex item for {@link LeftColumnBuilder}'s
     * column — {@code flex(1)} so it shares remaining vertical space
     * with Triage 50/50 (or claims it all when Triage is empty), with
     * a min-height so it stays usable even in cramped resolutions.
     */
    UIElement overlay() {
        SlotWorkspaceViewModel.LootChestPanel panel = host.viewModel.lootChestPanel();
        if (!panel.isPresent()) {
            host.lootChestPanelElement = null;
            return null;
        }

        UIElement overlay = panel(GLASS).layout(layout -> layout
                .flex(1)
                .widthPercent(100)
                .minHeight(MIN_HEIGHT)
                .paddingAll(PANEL_PADDING)
                .gapAll(PANEL_GAP)
                .flexDirection(FlexDirection.COLUMN));
        overlay.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());

        UIElement headerRow = new UIElement().layout(layout -> layout
                .widthPercent(100)
                .height(HEADER_HEIGHT)
                .gapAll(4)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.ROW));
        headerRow.addChildren(
                label("Loot chest", ACCENT).layout(layout -> layout.flex(1).height(12)),
                label(String.valueOf(panel.items().size()), MUTED).layout(layout -> layout.width(24).height(12))
        );
        headerRow.setAllowHitTest(false);
        overlay.addChild(headerRow);

        Label coords = label(panel.label(), MUTED);
        coords.layout(layout -> layout.widthPercent(100).height(SUBLABEL_HEIGHT));
        coords.textStyle(style -> style
                .textColor(MUTED)
                .fontSize(6)
                .textShadow(false)
                .textAlignHorizontal(Horizontal.LEFT)
                .textAlignVertical(Vertical.CENTER));
        coords.setAllowHitTest(false);
        overlay.addChild(coords);

        UIElement divider = panel(ISLAND_BORDER).layout(layout -> layout.widthPercent(100).height(DIVIDER_HEIGHT));
        divider.setAllowHitTest(false);
        overlay.addChild(divider);

        // Take-all sits at the TOP, just below the divider — the loot
        // panel's primary action is "drain this chest", and the player
        // shouldn't have to scroll to find the button on a long chest.
        // The "open vanilla here" escape hatch is the global V hotkey:
        // the workspace's open-vanilla key now opens the chest GUI for
        // this loot chest when one is present, falling back to the
        // player inventory otherwise. Avoids a redundant button.
        if (!panel.items().isEmpty()) {
            overlay.addChild(takeAllButton(panel));
        }

        if (panel.items().isEmpty()) {
            Label empty = label("Chest is empty.", MUTED);
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
            scroller.scrollerStyle(style -> style
                    .minScrollPixel(30f)
                    .maxScrollPixel(80f));
            for (SlotWorkspaceViewModel.AtlasItem item : panel.items()) {
                scroller.addScrollViewChild(itemBlock(item, panel));
            }
            overlay.addChild(scroller);
        }

        installClaimAndDepositDropTarget(overlay, panel);
        host.lootChestPanelElement = overlay;
        return overlay;
    }

    /**
     * Drag-carried-onto-loot-panel claim+deposit. The loot panel
     * accepts an atlas-item drag whose identity is currently in the
     * player's carry; on drop the server auto-claims the chest and
     * deposits the dragged stack. Replaces the prior "open vanilla
     * here, deposit through vanilla, observer auto-claims on close"
     * dance for players who never want to leave the SLOT workspace.
     */
    private void installClaimAndDepositDropTarget(UIElement target, SlotWorkspaceViewModel.LootChestPanel panel) {
        target.addEventListener(UIEvents.DRAG_ENTER, event -> host.drag.updateGenericDropOverlay(
                target, isClaimAndDepositAcceptable(event), ACCENT), true);
        target.addEventListener(UIEvents.DRAG_UPDATE, event -> host.drag.updateGenericDropOverlay(
                target, isClaimAndDepositAcceptable(event), ACCENT));
        target.addEventListener(UIEvents.DRAG_LEAVE, event -> host.drag.clearDropOverlay(target), true);
        target.addEventListener(UIEvents.DRAG_PERFORM, event -> {
            host.drag.clearDropOverlay(target);
            WorkspaceDrags.AtlasItemDrag drag = host.drag.atlasItemDrag(event);
            if (drag == null) {
                return;
            }
            SlotWorkspaceViewModel.AtlasItem item = host.viewModel.atlasItem(drag.identity());
            if (item == null || !item.carried()) {
                return;
            }
            event.stopPropagation();
            host.rpc.sendLootChestClaimAndDeposit(panel, drag.identity());
        });
    }

    private boolean isClaimAndDepositAcceptable(com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent event) {
        WorkspaceDrags.AtlasItemDrag drag = host.drag.atlasItemDrag(event);
        if (drag == null) {
            return false;
        }
        SlotWorkspaceViewModel.AtlasItem item = host.viewModel.atlasItem(drag.identity());
        return item != null && item.carried();
    }

    private static boolean isHomed(SlotWorkspaceViewModel.AtlasItem item) {
        return item.playerPlaced() || (!item.islandId().isBlank()
                && !item.islandId().equals(SlotWorkspaceAtlasLayout.ISLAND_TRIAGE));
    }

    /**
     * Vertical card container that owns the row chrome. The top tile is
     * the clickable/draggable item line; the optional bottom tile is
     * either the homed-home indicator or a stack of chip-suggestion
     * tiles, all flush against the top so the whole card reads as a
     * single row in the panel.
     */
    private UIElement itemBlock(SlotWorkspaceViewModel.AtlasItem item, SlotWorkspaceViewModel.LootChestPanel panel) {
        UIElement card = panel(ROW).layout(layout -> layout
                .widthPercent(100)
                .flexDirection(FlexDirection.COLUMN));

        card.addChild(itemRow(item, panel));
        if (isHomed(item)) {
            card.addChild(homedTile(item));
        } else {
            for (ChipSuggestion chip : item.chipSuggestions()) {
                card.addChild(chipTile(item, chip));
            }
        }
        return card;
    }

    /**
     * Top tile of an {@link #itemBlock} card: icon + name + count, with
     * click handling that mirrors Triage (left-click selects, shift+click
     * takes with auto-accept, drag routes through the standard atlas
     * drop targets so the player can drag onto an island to home or onto
     * empty atlas to create a new island).
     */
    private UIElement itemRow(SlotWorkspaceViewModel.AtlasItem item, SlotWorkspaceViewModel.LootChestPanel panel) {
        // Match the parent card's ROW chrome — the visual continuity
        // comes from the card panel painting ROW under everything; the
        // button's own fill matters for hover transitions.
        Button row = button("", true, ROW);
        row.noText();
        row.layout(layout -> layout
                .widthPercent(100)
                .height(ROW_HEIGHT)
                .paddingHorizontal(4)
                .gapAll(4)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.ROW));
        row.setOnClick(event -> {
            event.stopPropagation();
            if (event.button != 0) {
                return;
            }
            // Shift+click is the fast path: accept the top chip (if any)
            // and take immediately. Plain click only selects so the player
            // can drag the row out to an island / empty atlas without
            // accidentally home-and-take on every press.
            if (Screen.hasShiftDown()) {
                if (!item.chipSuggestions().isEmpty()) {
                    host.rpc.sendChipAccept(item, item.chipSuggestions().get(0));
                }
                host.rpc.sendLootChestTakeIdentity(panel, item);
                return;
            }
            host.selectedAtlasIdentity.set(item.identity());
            host.selectedHotbarIndex.set(-1);
            host.localStatus.set("selected " + item.name() + ": drag to an island, or shift-click to take");
        });
        host.drag.installAtlasHoverTooltip(row, item);
        host.drag.installAtlasItemDragSource(row, item);

        UIElement icon = itemIcon(item.displayStack(), 16, true);
        icon.layout(layout -> layout.width(16).height(16));
        row.addChild(icon);

        Label name = label(item.name(), TEXT);
        name.layout(layout -> layout.flex(1).height(12));
        name.textStyle(style -> style
                .textColor(TEXT)
                .fontSize(7)
                .textShadow(false)
                .textAlignHorizontal(Horizontal.LEFT)
                .textAlignVertical(Vertical.CENTER));
        name.setAllowHitTest(false);
        row.addChild(name);

        Label count = label("×" + item.totalCount(), MUTED);
        count.layout(layout -> layout.width(28).height(12));
        count.textStyle(style -> style
                .textColor(MUTED)
                .fontSize(6)
                .textShadow(false)
                .textAlignHorizontal(Horizontal.RIGHT)
                .textAlignVertical(Vertical.CENTER));
        count.setAllowHitTest(false);
        row.addChild(count);
        return row;
    }

    /**
     * Bottom tile for already-homed items. Sits flush under {@link
     * #itemRow} inside the same {@link #itemBlock} card so it reads as
     * part of the row rather than a separate strip. Background is
     * intentionally transparent — the parent card paints ROW.
     */
    private UIElement homedTile(SlotWorkspaceViewModel.AtlasItem item) {
        UIElement wrapper = new UIElement().layout(layout -> layout
                .widthPercent(100)
                .height(SUBROW_HEIGHT)
                .paddingLeft(20)
                .paddingRight(4)
                .alignItems(AlignItems.CENTER)
                .gapAll(4)
                .flexDirection(FlexDirection.ROW));
        wrapper.setAllowHitTest(false);

        SlotWorkspaceViewModel.AtlasIsland island = host.viewModel.island(item.islandId());
        int swatchColor = island == null || island.color() == 0 ? ACCENT : (island.color() | 0xFF000000);

        UIElement swatch = panel(swatchColor).layout(layout -> layout
                .width(6)
                .height(SUBROW_HEIGHT - 2));
        swatch.setAllowHitTest(false);
        wrapper.addChild(swatch);

        Label homeLabel = label(islandLabel(item.islandId()), TEXT);
        homeLabel.layout(layout -> layout.flex(1).height(SUBROW_HEIGHT));
        homeLabel.textStyle(style -> style
                .textColor(TEXT)
                .fontSize(6)
                .textShadow(false)
                .textAlignHorizontal(Horizontal.LEFT)
                .textAlignVertical(Vertical.CENTER));
        homeLabel.setAllowHitTest(false);
        wrapper.addChild(homeLabel);
        return wrapper;
    }

    private String islandLabel(String islandId) {
        if (islandId == null || islandId.isBlank()
                || islandId.equals(SlotWorkspaceAtlasLayout.ISLAND_TRIAGE)) {
            return "Triage";
        }
        SlotWorkspaceViewModel.AtlasIsland island = host.viewModel.island(islandId);
        if (island != null && !island.label().isBlank()) {
            return island.label();
        }
        return islandId;
    }

    /**
     * Bottom tile for chip suggestions. Visually distinct from the
     * homed tile: a colored button you can click to accept that home,
     * still sitting flush under {@link #itemRow} inside the same card.
     */
    private UIElement chipTile(SlotWorkspaceViewModel.AtlasItem item, ChipSuggestion chip) {
        UIElement wrapper = new UIElement().layout(layout -> layout
                .widthPercent(100)
                .height(SUBROW_HEIGHT)
                .paddingLeft(20)
                .paddingRight(4)
                .flexDirection(FlexDirection.ROW)
                .alignItems(AlignItems.CENTER));
        wrapper.setAllowHitTest(false);

        Button chipButton = button("", true, chip.color());
        chipButton.noText();
        chipButton.layout(layout -> layout
                .flex(1)
                .height(SUBROW_HEIGHT)
                .paddingHorizontal(4)
                .gapAll(2)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.ROW));
        chipButton.setOnClick(event -> {
            event.stopPropagation();
            host.rpc.sendChipAccept(item, chip);
        });
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

    private UIElement takeAllButton(SlotWorkspaceViewModel.LootChestPanel panel) {
        Button buttonEl = button("", true, ROW_MATCH);
        buttonEl.noText();
        buttonEl.layout(layout -> layout
                .widthPercent(100)
                .height(FOOTER_HEIGHT)
                .alignItems(AlignItems.CENTER));
        buttonEl.setOnClick(event -> {
            if (event.button != 0) {
                return;
            }
            event.stopPropagation();
            host.rpc.sendLootChestTakeAll(panel);
        });
        Label label = label("Take all", ACCENT);
        label.layout(layout -> layout.widthPercent(100).heightPercent(100));
        label.textStyle(style -> style
                .textColor(ACCENT)
                .fontSize(7)
                .textShadow(false)
                .textAlignHorizontal(Horizontal.CENTER)
                .textAlignVertical(Vertical.CENTER));
        label.setAllowHitTest(false);
        buttonEl.addChild(label);
        return buttonEl;
    }
}
