package dev.imagio.slot.neoforge.screen.ldlib;

import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.ACCENT;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.MUTED;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.PANEL_ALT;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.TEXT;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceUi.emptyIcon;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceUi.itemIcon;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceUi.label;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceUi.panel;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.inventory.workspace.WayfindingTarget;
import dev.imagio.slot.ui.workspace.WayfindingDisplay;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

/**
 * Single-line chip mounted in the atlas chest panels: chest name on
 * the left (flex), missing-item icons in the middle (capped to
 * {@link #MAX_ICONS}, "+N" tail when more), compass arrow + distance
 * on the right. Cross-dimension targets swap the compass + distance
 * for a dim-shorthand label + chest coords; the rest of the chip is
 * unchanged.
 */
public final class WayfindingChip {
    public static final int MAX_ICONS = 4;

    /**
     * Chip height in screen px. Tight enough that nearby-chest lists
     * stay scannable at standard GUI scale; matches the TOC row height
     * so the left column reads as one consistent compact list.
     */
    private static final int CHIP_HEIGHT = 12;
    /** Item icon size — fits within {@link #CHIP_HEIGHT} with 1px breathing each side. */
    private static final float ICON_SIZE = 10f;

    private WayfindingChip() {
    }

    /**
     * Build a chip for the given {@code target}. {@code chestLabel} is
     * required (the existing auto-name or player-renamed string).
     * When {@code target} is {@code null} the chip renders the same
     * shape minus the missing-item strip — used by the proximity panel
     * for non-wayfinding chests so the look stays uniform.
     */
    public static UIElement build(
            WayfindingTarget target,
            String chestLabel,
            String chestDimensionId,
            int chestWorldX,
            int chestWorldY,
            int chestWorldZ
    ) {
        int fill = (PANEL_ALT & 0x00FFFFFF) | 0xC0000000;

        UIElement chip = panel(fill).layout(layout -> layout
                .widthPercent(100)
                .height(CHIP_HEIGHT)
                .paddingHorizontal(4)
                .paddingVertical(1)
                .gapAll(3)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.ROW));

        Label nameEl = label(chestLabel == null ? "" : chestLabel, TEXT);
        nameEl.layout(layout -> layout.flex(1).heightPercent(100));
        nameEl.textStyle(style -> style
                .textColor(TEXT)
                .textShadow(false)
                .fontSize(6)
                .textAlignHorizontal(Horizontal.LEFT)
                .textAlignVertical(Vertical.CENTER));
        nameEl.setAllowHitTest(false);
        chip.addChild(nameEl);

        if (target != null && !target.missingIdentities().isEmpty()) {
            chip.addChild(buildIconStrip(target));
        }

        boolean crossDimension = isCrossDimension(chestDimensionId);
        if (crossDimension) {
            Label dimEl = label(shortDimension(chestDimensionId), ACCENT);
            dimEl.layout(layout -> layout.heightPercent(100));
            dimEl.textStyle(style -> style
                    .textColor(ACCENT)
                    .textShadow(false)
                    .fontSize(6)
                    .adaptiveWidth(true)
                    .textAlignVertical(Vertical.CENTER));
            dimEl.setAllowHitTest(false);
            chip.addChild(dimEl);

            Label coordsEl = label(chestWorldX + " " + chestWorldY + " " + chestWorldZ, MUTED);
            coordsEl.layout(layout -> layout.heightPercent(100));
            coordsEl.textStyle(style -> style
                    .textColor(MUTED)
                    .textShadow(false)
                    .fontSize(5)
                    .adaptiveWidth(true)
                    .textAlignVertical(Vertical.CENTER));
            coordsEl.setAllowHitTest(false);
            chip.addChild(coordsEl);
        } else {
            Label compassEl = label("·", ACCENT);
            compassEl.layout(layout -> layout.heightPercent(100));
            compassEl.textStyle(style -> style
                    .textColor(ACCENT)
                    .textShadow(false)
                    .fontSize(7)
                    .adaptiveWidth(true)
                    .textAlignVertical(Vertical.CENTER));
            compassEl.setAllowHitTest(false);
            chip.addChild(compassEl);

            Label distanceEl = label("--m", MUTED);
            distanceEl.layout(layout -> layout.heightPercent(100));
            distanceEl.textStyle(style -> style
                    .textColor(MUTED)
                    .textShadow(false)
                    .fontSize(6)
                    .adaptiveWidth(true)
                    .textAlignVertical(Vertical.CENTER));
            distanceEl.setAllowHitTest(false);
            chip.addChild(distanceEl);

            // Per-frame compass + distance update. Reads off the current
            // local player; chip stays static when the player isn't
            // available (no level loaded, etc.).
            chip.addEventListener(UIEvents.TICK, ignored -> updateCompassAndDistance(
                    compassEl, distanceEl,
                    chestDimensionId, chestWorldX, chestWorldY, chestWorldZ));
        }
        return chip;
    }

    private static UIElement buildIconStrip(WayfindingTarget target) {
        float iconSize = ICON_SIZE;
        UIElement strip = new UIElement().layout(layout -> layout
                .heightPercent(100)
                .gapAll(2)
                .flexDirection(FlexDirection.ROW)
                .alignItems(AlignItems.CENTER));
        strip.setAllowHitTest(false);
        int rendered = 0;
        int total = target.missingIdentities().size();
        for (ItemIdentity identity : target.missingIdentities()) {
            if (rendered >= MAX_ICONS) {
                break;
            }
            ItemStack stack = GhostAtlasStackFactory.resolve(identity.itemId());
            UIElement icon = stack.isEmpty()
                    ? emptyIcon().layout(layout -> layout.width(iconSize).height(iconSize))
                    : itemIcon(stack, iconSize, false);
            strip.addChild(icon);
            rendered++;
        }
        int overflow = total - rendered;
        if (overflow > 0) {
            Label more = label("+" + overflow, MUTED);
            more.layout(layout -> layout.height((int) iconSize));
            more.textStyle(style -> style
                    .textColor(MUTED)
                    .textShadow(false)
                    .fontSize(6)
                    .textAlignHorizontal(Horizontal.LEFT)
                    .textAlignVertical(Vertical.CENTER));
            strip.addChild(more);
        }
        return strip;
    }

    private static void updateCompassAndDistance(
            Label compassEl,
            Label distanceEl,
            String chestDimensionId,
            int chestWorldX,
            int chestWorldY,
            int chestWorldZ
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null) {
            return;
        }
        if (!minecraft.level.dimension().location().toString().equals(chestDimensionId)) {
            return;
        }
        double dx = (chestWorldX + 0.5) - player.getX();
        double dz = (chestWorldZ + 0.5) - player.getZ();
        double dy = (chestWorldY + 0.5) - player.getY();
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);
        double dist = Math.sqrt(horizontalDist * horizontalDist + dy * dy);
        // 8-direction compass — bearing relative to the player's yaw.
        // Yaw is in MC convention (south=0, increasing toward west);
        // convert the world-space dx/dz into a relative bearing in [-π, π].
        float yawRadians = (float) Math.toRadians(player.getYRot());
        double absoluteBearing = Math.atan2(-dx, dz);
        double relativeBearing = absoluteBearing - yawRadians;
        compassEl.setText(net.minecraft.network.chat.Component.literal(
                arrowGlyph(relativeBearing)));
        distanceEl.setText(net.minecraft.network.chat.Component.literal(
                String.format(Locale.ROOT, "%dm", Math.max(0, (int) Math.round(dist)))));
    }

    /**
     * Map a relative bearing (radians, 0 = directly ahead, +π/2 =
     * directly to the right) to an 8-way arrow glyph. Quantizing to
     * eight directions reads more clearly at small fonts than a
     * continuously-rotated arrow would.
     */
    private static String arrowGlyph(double relativeBearing) {
        double normalized = ((relativeBearing % (Math.PI * 2)) + Math.PI * 2) % (Math.PI * 2);
        int sector = (int) Math.floor((normalized + Math.PI / 8.0) / (Math.PI / 4.0)) % 8;
        return switch (sector) {
            case 0 -> "↑";
            case 1 -> "↗";
            case 2 -> "→";
            case 3 -> "↘";
            case 4 -> "↓";
            case 5 -> "↙";
            case 6 -> "←";
            case 7 -> "↖";
            default -> "·";
        };
    }

    private static boolean isCrossDimension(String chestDimensionId) {
        if (chestDimensionId == null || chestDimensionId.isBlank()) {
            return false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return false;
        }
        return !minecraft.level.dimension().location().toString().equals(chestDimensionId);
    }

    private static String shortDimension(String dimensionId) {
        if (dimensionId == null) {
            return "";
        }
        int colon = dimensionId.indexOf(':');
        String tail = colon < 0 ? dimensionId : dimensionId.substring(colon + 1);
        if (tail.startsWith("the_")) {
            tail = tail.substring(4);
        }
        return tail;
    }

    /** Convenience for atlas chip builders that have a {@link SlotWorkspaceViewModel.ChestChip}. */
    public static UIElement build(
            SlotWorkspaceViewModel.ChestChip chip,
            WayfindingTarget target
    ) {
        return build(
                target,
                target == null ? chip.label() : WayfindingDisplay.targetLabel(target),
                chip.dimensionId(),
                chip.worldX(),
                chip.worldY(),
                chip.worldZ()
        );
    }
}
