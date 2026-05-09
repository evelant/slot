package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.ui.spi.SlotUiElement;
import dev.imagio.slot.ui.spi.SlotUiEventKind;
import dev.imagio.slot.ui.spi.SlotUiLayout;
import dev.imagio.slot.ui.spi.SlotUiTextStyle;

import java.util.List;

import static dev.imagio.slot.ui.workspace.WorkspaceUiPalette.ACCENT;
import static dev.imagio.slot.ui.workspace.WorkspaceUiPalette.MUTED;
import static dev.imagio.slot.ui.workspace.WorkspaceUiPalette.ROW_DIM;

public final class HotbarBeltUiBuilder {
    public static final int BELT_HEIGHT_PX = 24;
    public static final int BELT_WITH_LEADING_HEIGHT_PX = 28;
    public static final int SLOT_SIZE_PX = 20;
    public static final int ICON_SIZE_PX = 16;

    private static final int PANEL = 0xC8162029;
    private static final int ROW = 0xC926313B;
    private static final int ACTIVE_HOTBAR = 0xE0A97935;
    private static final int EMPTY_SLOT = 0x4424313D;

    private final Context context;

    public HotbarBeltUiBuilder(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context is required");
        }
        this.context = context;
    }

    public SlotUiElement belt(
            List<SlotWorkspaceViewModel.HotbarSlot> hotbarSlots,
            SlotWorkspaceViewModel.OffhandSlot offhand
    ) {
        return belt(hotbarSlots, offhand, null);
    }

    public SlotUiElement belt(
            List<SlotWorkspaceViewModel.HotbarSlot> hotbarSlots,
            SlotWorkspaceViewModel.OffhandSlot offhand,
            SlotUiElement leading
    ) {
        int slotSize = leading == null ? SLOT_SIZE_PX : 16;
        int iconSize = leading == null ? ICON_SIZE_PX : 14;
        SlotUiElement strip = SlotUiElement.panel(PANEL)
                .zIndex(6)
                .attach(WorkspaceUiAttachments.HOTBAR_STRIP, Boolean.TRUE)
                .layout(layout -> layout
                        .widthPercent(100)
                        .height(leading == null ? BELT_HEIGHT_PX : BELT_WITH_LEADING_HEIGHT_PX)
                        .paddingAll(3)
                        .gapAll(leading == null ? 2 : 1)
                        .alignItems(SlotUiLayout.AlignItems.CENTER)
                        .flexDirection(SlotUiLayout.FlexDirection.ROW));
        strip.on(SlotUiEventKind.MOUSE_DOWN, event -> event.stopPropagation());

        if (leading != null) {
            strip.addChild(leading);
        }
        for (SlotWorkspaceViewModel.HotbarSlot slot : safeHotbar(hotbarSlots)) {
            strip.addChild(slotButton(slot, slotSize, iconSize));
        }
        strip.addChild(offhandButton(
                offhand == null ? SlotWorkspaceViewModel.OffhandSlot.empty() : offhand,
                slotSize,
                iconSize));
        return strip;
    }

    private SlotUiElement slotButton(SlotWorkspaceViewModel.HotbarSlot slot, int slotSize, int iconSize) {
        SlotUiElement button = SlotUiElement.button("", true, slotColor(slot))
                .noText()
                .zIndex(2)
                .attach(WorkspaceUiAttachments.HOTBAR_SLOT, slot)
                .layout(layout -> layout
                        .width(slotSize)
                        .height(slotSize)
                        .paddingAll(1)
                        .alignItems(SlotUiLayout.AlignItems.CENTER)
                        .flexDirection(SlotUiLayout.FlexDirection.COLUMN));
        if (slot.occupied()) {
            button.tooltipStack(slot.displayStack());
        }
        button.on(SlotUiEventKind.CLICK, event -> {
            if (context.isCursorCarrying()) {
                if (event.button() != 0 && event.button() != 1) {
                    return;
                }
                event.stopPropagation();
                context.dropCursorAtHotbar(slot.hotbarIndex(), event.button());
                return;
            }
            if (event.button() != 0) {
                return;
            }
            event.stopPropagation();
            if (event.shiftDown() && slot.occupied()) {
                context.returnHotbarToHome(slot.hotbarIndex());
                return;
            }
            if (!slot.occupied()) {
                context.setStatus("belt " + (slot.hotbarIndex() + 1) + " is empty");
                return;
            }
            context.setStatus("ready");
        });
        button.addChild(slot.occupied()
                ? SlotUiElement.itemIcon(slot.displayStack(), iconSize, true).renderVanillaCount(true)
                : SlotUiElement.panel(EMPTY_SLOT).layout(layout -> layout.width(iconSize).height(iconSize)));
        button.addChild(slotIndexLabel(Integer.toString(slot.hotbarIndex() + 1), slot.selected() ? ACCENT : MUTED));
        return button;
    }

    private int slotColor(SlotWorkspaceViewModel.HotbarSlot slot) {
        return slot.selected() && slot.occupied() ? ACTIVE_HOTBAR : ROW;
    }

    private SlotUiElement offhandButton(SlotWorkspaceViewModel.OffhandSlot offhand, int slotSize, int iconSize) {
        SlotUiElement button = SlotUiElement.button("", false, ROW_DIM)
                .noText()
                .zIndex(1)
                .attach(WorkspaceUiAttachments.OFFHAND_SLOT, offhand)
                .layout(layout -> layout
                        .width(slotSize)
                        .height(slotSize)
                        .paddingAll(1)
                        .alignItems(SlotUiLayout.AlignItems.CENTER)
                        .flexDirection(SlotUiLayout.FlexDirection.COLUMN));
        button.buttonActive(false);
        if (offhand.occupied()) {
            button.tooltipStack(offhand.displayStack());
        }
        button.addChild(offhand.occupied()
                ? SlotUiElement.itemIcon(offhand.displayStack(), iconSize, true).renderVanillaCount(true)
                : SlotUiElement.panel(EMPTY_SLOT).layout(layout -> layout.width(iconSize).height(iconSize)));
        button.addChild(slotIndexLabel("off", MUTED));
        return button;
    }

    private SlotUiElement slotIndexLabel(String text, int color) {
        return SlotUiElement.label(text, color)
                .layout(layout -> layout
                        .positionType(SlotUiLayout.PositionType.ABSOLUTE)
                        .left(1)
                        .top(0)
                        .height(6))
                .textStyle(style -> style
                        .color(color)
                        .fontSize(6)
                        .shadow(true)
                        .horizontal(SlotUiTextStyle.Horizontal.LEFT)
                        .vertical(SlotUiTextStyle.Vertical.TOP));
    }

    private static List<SlotWorkspaceViewModel.HotbarSlot> safeHotbar(
            List<SlotWorkspaceViewModel.HotbarSlot> slots
    ) {
        return slots == null || slots.isEmpty() ? SlotWorkspaceViewModel.emptyHotbar() : List.copyOf(slots);
    }

    public interface Context {
        void returnHotbarToHome(int hotbarIndex);

        boolean isCursorCarrying();

        void dropCursorAtHotbar(int hotbarIndex, int button);

        void setStatus(String status);
    }
}
