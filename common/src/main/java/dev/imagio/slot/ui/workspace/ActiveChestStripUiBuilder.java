package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.ui.spi.SlotUiElement;
import dev.imagio.slot.ui.spi.SlotUiEventKind;
import dev.imagio.slot.ui.spi.SlotUiLayout;
import dev.imagio.slot.ui.spi.SlotUiTextStyle;
import dev.imagio.slot.workflow.domain.ChestRole;
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
        return roleStrip(panel);
    }

    private SlotUiElement roleStrip(SlotWorkspaceViewModel.ActiveChestPanel panel) {
        SlotUiElement strip = baseStrip(panel);
        int labelColor = panel.role() == ChestRole.IGNORE ? MUTED : TEXT;
        strip.addChild(SlotUiElement.label(displayLabel(panel), labelColor)
                .layout(layout -> layout.flex(1).heightPercent(100))
                .textStyle(style -> style
                        .color(labelColor)
                        .fontSize(7)
                        .horizontal(SlotUiTextStyle.Horizontal.LEFT)
                        .vertical(SlotUiTextStyle.Vertical.CENTER)));
        strip.addChild(SlotUiElement.button(panel.role().displayLabel(), true, roleColor(panel.role()))
                .tooltip(Component.literal(roleTooltip(panel.role())))
                .layout(layout -> layout.width(58).height(STRIP_HEIGHT_PX - 2))
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
                    context.setChestRoleAt(panel, panel.nextRole());
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

    private static String displayLabel(SlotWorkspaceViewModel.ActiveChestPanel panel) {
        if (!panel.isClaimed()) {
            return "Chest";
        }
        String chest = panel.label().isBlank() ? "Chest" : panel.label();
        if (panel.clusterLabel().isBlank()) {
            return chest;
        }
        return panel.clusterLabel() + " / " + chest;
    }

    private static int roleColor(ChestRole role) {
        return switch (role == null ? ChestRole.IGNORE : role) {
            case STORAGE -> ACCENT;
            case INPUT -> 0xC8758B6B;
            case OUTPUT -> 0xC86BA875;
            case IGNORE -> 0xC83B4A56;
        };
    }

    private static String roleTooltip(ChestRole role) {
        return switch (role == null ? ChestRole.IGNORE : role) {
            case STORAGE -> "Storage: visible, searchable, learns homes, and accepts quick store. Click for Input.";
            case INPUT -> "Input: visible and searchable; lowest take priority and accepts put only while open. Click for Output.";
            case OUTPUT -> "Output: visible and searchable; highest take priority and accepts put only while open. Click for Ignore.";
            case IGNORE -> "Ignore: hidden from SLOT storage and routing. Click for Storage.";
        };
    }

    public interface Context {
        void setChestRoleAt(SlotWorkspaceViewModel.ActiveChestPanel panel, ChestRole role);

        default void openChestMenu(String storageId, float screenX, float screenY) {
        }
    }
}
