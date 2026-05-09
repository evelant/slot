package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.ui.spi.SlotUiElement;
import dev.imagio.slot.ui.spi.SlotUiEventKind;
import dev.imagio.slot.ui.spi.SlotUiLayout;
import dev.imagio.slot.ui.spi.SlotUiTextStyle;
import net.minecraft.network.chat.Component;

import static dev.imagio.slot.ui.workspace.WorkspaceUiPalette.ACCENT;
import static dev.imagio.slot.ui.workspace.WorkspaceUiPalette.MUTED;
import static dev.imagio.slot.ui.workspace.WorkspaceUiPalette.TEXT;

public final class ActiveChestStripUiBuilder {
    public static final int STRIP_HEIGHT_PX = 16;

    private static final int PANEL = 0xC8162029;

    private final Context context;

    public ActiveChestStripUiBuilder(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context is required");
        }
        this.context = context;
    }

    public SlotUiElement strip(SlotWorkspaceViewModel.ActiveChestPanel panel) {
        if (panel == null || !panel.isPresent()) {
            return null;
        }
        return panel.isClaimed() ? claimedStrip(panel) : unclaimedStrip(panel);
    }

    private SlotUiElement claimedStrip(SlotWorkspaceViewModel.ActiveChestPanel panel) {
        SlotUiElement strip = baseStrip(panel);
        strip.addChild(SlotUiElement.label(claimedDisplayLabel(panel), TEXT)
                .layout(layout -> layout.flex(1).heightPercent(100))
                .textStyle(style -> style
                        .color(TEXT)
                        .fontSize(7)
                        .horizontal(SlotUiTextStyle.Horizontal.LEFT)
                        .vertical(SlotUiTextStyle.Vertical.CENTER)));
        if (!panel.storageId().isBlank()) {
            strip.addChild(SlotUiElement.button("Forget", true, 0xC83B4A56)
                    .tooltip(Component.literal("Forget this claimed chest"))
                    .layout(layout -> layout.width(48).height(STRIP_HEIGHT_PX - 2))
                    .textStyle(style -> style
                            .color(TEXT)
                            .fontSize(7)
                            .horizontal(SlotUiTextStyle.Horizontal.CENTER)
                            .vertical(SlotUiTextStyle.Vertical.CENTER))
                    .on(SlotUiEventKind.CLICK, event -> {
                        if (event.button() != 0) {
                            return;
                        }
                        event.stopPropagation();
                        context.forgetChest(panel.storageId());
                    }));
        }
        return strip;
    }

    private SlotUiElement unclaimedStrip(SlotWorkspaceViewModel.ActiveChestPanel panel) {
        SlotUiElement strip = baseStrip(panel);
        strip.addChild(SlotUiElement.label("Unclaimed chest", MUTED)
                .layout(layout -> layout.flex(1).heightPercent(100))
                .textStyle(style -> style
                        .color(MUTED)
                        .fontSize(7)
                        .horizontal(SlotUiTextStyle.Horizontal.LEFT)
                        .vertical(SlotUiTextStyle.Vertical.CENTER)));
        strip.addChild(SlotUiElement.button("Claim", true, ACCENT)
                .tooltip(Component.literal(
                        "Track this chest. SLOT will learn its contents and route compatible deposits here."))
                .layout(layout -> layout.width(48).height(STRIP_HEIGHT_PX - 2))
                .textStyle(style -> style
                        .color(TEXT)
                        .fontSize(7)
                        .horizontal(SlotUiTextStyle.Horizontal.CENTER)
                        .vertical(SlotUiTextStyle.Vertical.CENTER))
                .on(SlotUiEventKind.CLICK, event -> {
                    if (event.button() != 0) {
                        return;
                    }
                    event.stopPropagation();
                    context.claimChestAt(panel);
                }));
        return strip;
    }

    private SlotUiElement baseStrip(SlotWorkspaceViewModel.ActiveChestPanel panel) {
        return SlotUiElement.panel(PANEL)
                .zIndex(7)
                .attach(WorkspaceUiAttachments.ACTIVE_CHEST_STRIP, panel)
                .layout(layout -> layout
                        .widthPercent(100)
                        .height(STRIP_HEIGHT_PX)
                        .paddingHorizontal(4)
                        .gapAll(4)
                        .alignItems(SlotUiLayout.AlignItems.CENTER)
                        .flexDirection(SlotUiLayout.FlexDirection.ROW))
                .on(SlotUiEventKind.MOUSE_DOWN, event -> {
                    event.stopPropagation();
                    if (event.button() == 1 && panel != null && panel.isClaimed()) {
                        context.openChestMenu(panel.storageId(), event.x(), event.y());
                    }
                });
    }

    private static String claimedDisplayLabel(SlotWorkspaceViewModel.ActiveChestPanel panel) {
        String chest = panel.label().isBlank() ? "Chest" : panel.label();
        if (panel.clusterLabel().isBlank()) {
            return chest;
        }
        return panel.clusterLabel() + " / " + chest;
    }

    public interface Context {
        void claimChestAt(SlotWorkspaceViewModel.ActiveChestPanel panel);

        default void forgetChest(String storageId) {
        }

        default void openChestMenu(String storageId, float screenX, float screenY) {
        }
    }
}
