package dev.imagio.slot.neoforge.screen.ldlib;

import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.*;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceUi.*;

import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.workflow.domain.ChestRole;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.network.chat.Component;

/**
 * Sidebar-mode chest control strip. Shows above the wall whenever the
 * player has a chest screen open as the host; collapses to nothing
 * otherwise. The role button cycles Storage → Buffer → Ignore, matching the
 * active chest's player-authored SLOT participation mode.
 *
 * <p>Per the single-column-workspace plan, this strip is the sole
 * surface for per-chest management now that the parallel chest-chip
 * panel is gone — actions follow the player's focus instead of
 * enumerating every chest they might care about.
 */
final class ActiveChestStripBuilder {
    static final int STRIP_HEIGHT = 16;
    static final int STRIP_PADDING = 4;

    private final SlotWorkspaceUiController host;

    ActiveChestStripBuilder(SlotWorkspaceUiController host) {
        this.host = host;
    }

    UIElement overlay() {
        SlotWorkspaceViewModel.ActiveChestPanel panel = host.viewModel.activeChestPanel();
        if (!panel.isPresent()) {
            return null;
        }
        return roleStrip(panel);
    }

    private UIElement roleStrip(SlotWorkspaceViewModel.ActiveChestPanel panel) {
        UIElement strip = panel(GLASS).layout(layout -> layout
                .widthPercent(100)
                .height(STRIP_HEIGHT)
                .paddingHorizontal(STRIP_PADDING)
                .gapAll(4)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.ROW));
        strip.style(style -> style.zIndex(7));
        strip.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());

        int labelColor = panel.role() == ChestRole.IGNORE ? MUTED : TEXT;
        Label nameLabel = label(displayLabel(panel), labelColor);
        nameLabel.layout(layout -> layout.flex(1).heightPercent(100));
        nameLabel.textStyle(style -> style
                .textColor(labelColor)
                .textShadow(false)
                .fontSize(7)
                .textAlignHorizontal(Horizontal.LEFT)
                .textAlignVertical(Vertical.CENTER));
        nameLabel.setAllowHitTest(false);
        strip.addChild(nameLabel);

        Button role = button(panel.role().displayLabel(), true, roleColor(panel.role()));
        role.layout(layout -> layout.width(58).height(STRIP_HEIGHT - 2));
        role.textStyle(style -> style
                .textColor(TEXT)
                .textShadow(false)
                .fontSize(7)
                .textAlignHorizontal(Horizontal.CENTER)
                .textAlignVertical(Vertical.CENTER));
        role.setOnClick(event -> {
            event.stopPropagation();
            host.rpc.sendSetChestRoleAt(panel, panel.nextRole());
        });
        host.installTextTooltip(role, Component.literal(roleTooltip(panel.role())));
        strip.addChild(role);

        if (panel.isClaimed()) {
            Button gear = button("", true, GLASS).noText();
            gear.addPreIcon(Icons.SETTINGS);
            gear.layout(layout -> layout.width(STRIP_HEIGHT - 2).height(STRIP_HEIGHT - 2));
            gear.setOnClick(event -> {
                event.stopPropagation();
                host.menu.openContextMenuForChest(panel.storageId(), event.x, event.y);
            });
            host.installTextTooltip(gear, Component.literal("Rename chest"));
            strip.addChild(gear);
        }
        return strip;
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
            case BUFFER -> 0xC8758B6B;
            case IGNORE -> 0xC83B4A56;
        };
    }

    private static String roleTooltip(ChestRole role) {
        return switch (role == null ? ChestRole.IGNORE : role) {
            case STORAGE -> "Storage: visible, searchable, learns homes, and accepts quick store. Click for Buffer.";
            case BUFFER -> "Buffer: visible and searchable, but never learns homes or accepts quick store. Click for Ignore.";
            case IGNORE -> "Ignore: hidden from SLOT storage and routing. Click for Storage.";
        };
    }
}
