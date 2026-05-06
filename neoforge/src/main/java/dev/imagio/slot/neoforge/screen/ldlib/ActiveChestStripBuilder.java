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
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.network.chat.Component;

/**
 * Sidebar-mode chest control strip. Shows above the wall whenever the
 * player has a chest screen open as the host; collapses to nothing
 * otherwise. Two states:
 *
 * <ul>
 *   <li><b>Claimed</b> — single line "{cluster} / {chest} [⚙]". The
 *       gear opens the existing chest context menu (rename / forget),
 *       which is the same surface the old chest-chip panel used.</li>
 *   <li><b>Unclaimed</b> — single line "Unclaimed chest [Claim]". The
 *       button fires {@link WorkspaceRpcDispatcher#sendClaimChestAt}
 *       which auto-claims via the same path the deposit observer
 *       uses on first deposit.</li>
 * </ul>
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
        if (panel.isClaimed()) {
            return claimedStrip(panel);
        }
        return unclaimedStrip(panel);
    }

    private UIElement claimedStrip(SlotWorkspaceViewModel.ActiveChestPanel panel) {
        UIElement strip = panel(GLASS).layout(layout -> layout
                .widthPercent(100)
                .height(STRIP_HEIGHT)
                .paddingHorizontal(STRIP_PADDING)
                .gapAll(4)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.ROW));
        strip.style(style -> style.zIndex(7));
        strip.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());

        Label nameLabel = label(claimedDisplayLabel(panel), TEXT);
        nameLabel.layout(layout -> layout.flex(1).heightPercent(100));
        nameLabel.textStyle(style -> style
                .textColor(TEXT)
                .textShadow(false)
                .fontSize(7)
                .textAlignHorizontal(Horizontal.LEFT)
                .textAlignVertical(Vertical.CENTER));
        nameLabel.setAllowHitTest(false);
        strip.addChild(nameLabel);

        Button gear = button("", true, GLASS).noText();
        gear.addPreIcon(Icons.SETTINGS);
        gear.layout(layout -> layout.width(STRIP_HEIGHT - 2).height(STRIP_HEIGHT - 2));
        gear.setOnClick(event -> {
            event.stopPropagation();
            host.menu.openContextMenuForChest(panel.storageId(), event.x, event.y);
        });
        host.installTextTooltip(gear, Component.literal("Rename / Forget"));
        strip.addChild(gear);
        return strip;
    }

    private static String claimedDisplayLabel(SlotWorkspaceViewModel.ActiveChestPanel panel) {
        String chest = panel.label().isBlank() ? "Chest" : panel.label();
        if (panel.clusterLabel().isBlank()) {
            return chest;
        }
        return panel.clusterLabel() + " / " + chest;
    }

    private UIElement unclaimedStrip(SlotWorkspaceViewModel.ActiveChestPanel panel) {
        UIElement strip = panel(GLASS).layout(layout -> layout
                .widthPercent(100)
                .height(STRIP_HEIGHT)
                .paddingHorizontal(STRIP_PADDING)
                .gapAll(4)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.ROW));
        strip.style(style -> style.zIndex(7));
        strip.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());

        Label hint = label("Unclaimed chest", MUTED);
        hint.layout(layout -> layout.flex(1).heightPercent(100));
        hint.textStyle(style -> style
                .textColor(MUTED)
                .textShadow(false)
                .fontSize(7)
                .textAlignHorizontal(Horizontal.LEFT)
                .textAlignVertical(Vertical.CENTER));
        hint.setAllowHitTest(false);
        strip.addChild(hint);

        Button claim = button("Claim", true, ACCENT);
        claim.layout(layout -> layout.width(48).height(STRIP_HEIGHT - 2));
        claim.textStyle(style -> style
                .textColor(TEXT)
                .textShadow(false)
                .fontSize(7)
                .textAlignHorizontal(Horizontal.CENTER)
                .textAlignVertical(Vertical.CENTER));
        claim.setOnClick(event -> {
            event.stopPropagation();
            host.rpc.sendClaimChestAt(panel.dimensionId(), panel.posX(), panel.posY(), panel.posZ());
        });
        host.installTextTooltip(claim, Component.literal(
                "Track this chest. SLOT will learn its contents and route compatible deposits here."));
        strip.addChild(claim);
        return strip;
    }
}
