package dev.imagio.slot.neoforge.screen.ldlib;

import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.*;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceUi.*;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.AtlasItemDrag;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.HotbarSlotDrag;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.TaffyPosition;

/**
 * Compact chest chip stack — replaces the link-era storage strip.
 *
 * <p>Renders as an overlay docked above the Triage panel on the left
 * edge, mirroring Triage's column. Shows one chip per *proximate* claimed
 * chest; non-proximate chests are hidden (the player isn't near them, so
 * they aren't actionable here). See docs/plans/learned-storage.md.
 */
final class StoragePanelBuilder {
    static final int CHIP_HEIGHT = 18;
    static final int PANEL_GAP = 3;
    static final int PANEL_PADDING = 6;
    static final int HEADER_HEIGHT = 12;
    static final int MAX_CHIPS = 7;

    private final SlotWorkspaceUiController host;

    StoragePanelBuilder(SlotWorkspaceUiController host) {
        this.host = host;
    }

    /**
     * Vertical space the chip panel reserves at the top of the left
     * column. Triage uses this to shift its own top down so the two
     * don't overlap. Returns 0 when no proximate chests are visible
     * (panel disappears entirely).
     */
    int reservedHeight() {
        int proximateCount = countProximate();
        if (proximateCount == 0) {
            return 0;
        }
        int chipsCount = Math.min(proximateCount, MAX_CHIPS);
        return HEADER_HEIGHT + PANEL_PADDING * 2
                + chipsCount * CHIP_HEIGHT + Math.max(0, chipsCount - 1) * PANEL_GAP;
    }

    private int countProximate() {
        int proximateCount = 0;
        for (SlotWorkspaceViewModel.ChestChip chip : host.viewModel.chestChips()) {
            if (chip.proximate()) {
                proximateCount++;
            }
        }
        return proximateCount;
    }

    /**
     * Returns null when there are no proximate chests so the overlay
     * disappears entirely (rather than docking an empty capsule above
     * Triage).
     */
    UIElement overlay() {
        int proximateCount = countProximate();
        if (proximateCount == 0) {
            host.storagePanelElement = null;
            return null;
        }

        int top = TriagePanelBuilder.baseTop(host);
        int panelHeight = reservedHeight();
        int finalPanelTop = top;
        int finalPanelHeight = panelHeight;

        UIElement overlay = panel(GLASS).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(8)
                .top(finalPanelTop)
                .width(TRIAGE_PANEL_WIDTH)
                .height(finalPanelHeight)
                .paddingAll(PANEL_PADDING)
                .gapAll(PANEL_GAP)
                .flexDirection(FlexDirection.COLUMN));
        overlay.style(style -> style.zIndex(7));
        overlay.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());

        Label header = label("Nearby chests", ACCENT);
        header.layout(layout -> layout.widthPercent(100).height(HEADER_HEIGHT));
        header.textStyle(style -> style
                .textColor(ACCENT)
                .textShadow(false)
                .fontSize(8)
                .textAlignHorizontal(Horizontal.LEFT)
                .textAlignVertical(Vertical.CENTER));
        header.setAllowHitTest(false);
        overlay.addChild(header);

        int rendered = 0;
        for (SlotWorkspaceViewModel.ChestChip chip : host.viewModel.chestChips()) {
            if (!chip.proximate()) {
                continue;
            }
            overlay.addChild(chestChip(chip));
            if (++rendered >= MAX_CHIPS) {
                break;
            }
        }

        host.storagePanelElement = overlay;
        return overlay;
    }

    /**
     * Legacy entry point — body() returns an empty placeholder so
     * existing callers in AtlasPanelBuilder still compile. The real
     * panel is the overlay; mount {@link #overlay()} on the atlas panel.
     */
    UIElement body() {
        return new UIElement().layout(layout -> layout.width(0).height(0));
    }

    void repopulate() {
        // Overlay is rebuilt each frame from {@link #overlay()}; nothing
        // to do here. Kept for API parity with the old strip.
    }

    private UIElement chestChip(SlotWorkspaceViewModel.ChestChip chip) {
        int fill = (PANEL_ALT & 0x00FFFFFF) | 0xC0000000;

        UIElement element = panel(fill).layout(layout -> layout
                .widthPercent(100)
                .height(CHIP_HEIGHT)
                .paddingHorizontal(6)
                .gapAll(4)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.ROW));

        Label labelEl = label(chip.label(), TEXT);
        labelEl.layout(layout -> layout.flex(1).height(CHIP_HEIGHT));
        labelEl.textStyle(style -> style
                .textColor(TEXT)
                .textShadow(false)
                .fontSize(8)
                .textAlignHorizontal(Horizontal.LEFT)
                .textAlignVertical(Vertical.CENTER));
        labelEl.setAllowHitTest(false);
        element.addChild(labelEl);

        if (chip.slotCapacity() > 0) {
            Label countEl = label(chip.filledSlots() + "/" + chip.slotCapacity(), MUTED);
            countEl.layout(layout -> layout.width(36).height(CHIP_HEIGHT));
            countEl.textStyle(style -> style
                    .textColor(MUTED)
                    .textShadow(false)
                    .fontSize(7)
                    .textAlignHorizontal(Horizontal.RIGHT)
                    .textAlignVertical(Vertical.CENTER));
            countEl.setAllowHitTest(false);
            element.addChild(countEl);
        }

        Label dot = label("●", ACCENT);
        dot.layout(layout -> layout.width(8).height(CHIP_HEIGHT));
        dot.textStyle(style -> style
                .textColor(ACCENT)
                .textShadow(false)
                .fontSize(7)
                .textAlignHorizontal(Horizontal.CENTER)
                .textAlignVertical(Vertical.CENTER));
        dot.setAllowHitTest(false);
        element.addChild(dot);

        installChipDropTarget(element, chip);
        return element;
    }

    /**
     * Drop an atlas-item or hotbar-slot drag onto a chip → deposit the
     * stack into that specific chest. Bypasses affinity routing so the
     * player can override where an item goes.
     */
    private void installChipDropTarget(UIElement chip, SlotWorkspaceViewModel.ChestChip target) {
        String storageId = target.storageId();
        chip.addEventListener(UIEvents.DRAG_ENTER, event -> {
            boolean acceptable = host.drag.atlasItemDrag(event) != null
                    || host.drag.hotbarSlotDrag(event) != null;
            host.drag.updateGenericDropOverlay(chip, acceptable, ACCENT);
        }, true);
        chip.addEventListener(UIEvents.DRAG_UPDATE, event -> {
            boolean acceptable = host.drag.atlasItemDrag(event) != null
                    || host.drag.hotbarSlotDrag(event) != null;
            host.drag.updateGenericDropOverlay(chip, acceptable, ACCENT);
        });
        chip.addEventListener(UIEvents.DRAG_LEAVE, event -> host.drag.clearDropOverlay(chip), true);
        chip.addEventListener(UIEvents.DRAG_PERFORM, event -> {
            host.drag.clearDropOverlay(chip);
            AtlasItemDrag atlasDrag = host.drag.atlasItemDrag(event);
            HotbarSlotDrag hotbarDrag = host.drag.hotbarSlotDrag(event);
            if (atlasDrag == null && hotbarDrag == null) {
                return;
            }
            if (atlasDrag != null) {
                host.rpc.sendDepositCarriedToChest(atlasDrag.identity(), storageId);
            } else {
                host.rpc.sendDepositHotbarToChest(hotbarDrag.hotbarIndex(), storageId);
            }
            event.stopPropagation();
        });
    }
}
