package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.ui.spi.SlotUiElement;
import dev.imagio.slot.ui.spi.SlotUiEventKind;
import dev.imagio.slot.ui.spi.SlotUiLayout;
import dev.imagio.slot.ui.spi.SlotUiTextStyle;

import java.util.List;

import static dev.imagio.slot.ui.workspace.WorkspaceUiPalette.MUTED;
import static dev.imagio.slot.ui.workspace.WorkspaceUiPalette.ROW_DIM;

public final class RecentsStripUiBuilder {
    public static final int ICON_SIZE_PX = 16;
    public static final int GAP_PX = 2;
    public static final int PADDING_PX = 3;
    public static final int MAX_ICONS = 12;

    private static final int STRIP_BACKGROUND = 0xB810171D;

    private final Context context;

    public RecentsStripUiBuilder(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context is required");
        }
        this.context = context;
    }

    public SlotUiElement overlay(List<SlotWorkspaceViewModel.IdentityRef> recentIdentities) {
        SlotUiElement strip = SlotUiElement.element()
                .backgroundColor(STRIP_BACKGROUND)
                .zIndex(1)
                .attach(WorkspaceUiAttachments.RECENTS_STRIP, Boolean.TRUE)
                .layout(layout -> layout
                        .widthPercent(100)
                        .height(ICON_SIZE_PX + PADDING_PX * 2)
                        .paddingHorizontal(PADDING_PX)
                        .paddingVertical(PADDING_PX)
                        .gapAll(GAP_PX)
                        .alignItems(SlotUiLayout.AlignItems.CENTER)
                        .flexDirection(SlotUiLayout.FlexDirection.ROW));
        strip.on(SlotUiEventKind.MOUSE_DOWN, event -> event.stopPropagation());

        strip.addChild(SlotUiElement.label("Recent", MUTED)
                .layout(layout -> layout.height(ICON_SIZE_PX).paddingRight(2))
                .textStyle(style -> style
                        .color(MUTED)
                        .shadow(false)
                        .fontSize(6)
                        .adaptiveWidth(true)
                        .horizontal(SlotUiTextStyle.Horizontal.LEFT)
                        .vertical(SlotUiTextStyle.Vertical.CENTER)));

        List<SlotWorkspaceViewModel.IdentityRef> identities = safeList(recentIdentities);
        int rendered = 0;
        for (SlotWorkspaceViewModel.IdentityRef identity : identities) {
            if (rendered >= MAX_ICONS) {
                break;
            }
            SlotWorkspaceViewModel.AtlasItem item = context.atlasItem(identity);
            if (item == null) {
                continue;
            }
            strip.addChild(iconButton(item));
            rendered++;
        }
        if (rendered == 0) {
            strip.addChild(SlotUiElement.label(identities.isEmpty() ? "nothing yet" : "not visible", MUTED)
                    .layout(layout -> layout.flex(1).height(ICON_SIZE_PX))
                    .textStyle(style -> style
                            .color(MUTED)
                            .shadow(false)
                            .fontSize(6)
                            .horizontal(SlotUiTextStyle.Horizontal.LEFT)
                            .vertical(SlotUiTextStyle.Vertical.CENTER)));
        }
        return strip;
    }

    private SlotUiElement iconButton(SlotWorkspaceViewModel.AtlasItem item) {
        SlotUiElement button = SlotUiElement.button("", true, ROW_DIM)
                .noText()
                .zIndex(2)
                .tooltipStack(item.displayStack())
                .attach(WorkspaceUiAttachments.RECENTS_ICON, Boolean.TRUE)
                .attach(WorkspaceUiAttachments.ATLAS_ITEM, item)
                .layout(layout -> layout
                        .width(ICON_SIZE_PX)
                        .height(ICON_SIZE_PX)
                        .paddingAll(0));
        button.on(SlotUiEventKind.MOUSE_DOWN, event -> {
            if (event.button() != 0) {
                return;
            }
            event.stopPropagation();
            context.focusRecent(item);
        });
        button.addChild(SlotUiElement.itemIcon(item.displayStack(), ICON_SIZE_PX, item.carried())
                .renderVanillaCount(false)
                .layout(layout -> layout.widthPercent(100).heightPercent(100)));
        return button;
    }

    private static List<SlotWorkspaceViewModel.IdentityRef> safeList(
            List<SlotWorkspaceViewModel.IdentityRef> recentIdentities
    ) {
        return recentIdentities == null ? List.of() : List.copyOf(recentIdentities);
    }

    public interface Context {
        SlotWorkspaceViewModel.AtlasItem atlasItem(SlotWorkspaceViewModel.IdentityRef identity);

        void focusRecent(SlotWorkspaceViewModel.AtlasItem item);
    }
}
