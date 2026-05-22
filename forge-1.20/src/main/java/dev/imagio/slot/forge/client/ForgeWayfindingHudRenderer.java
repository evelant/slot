package dev.imagio.slot.forge.client;

import dev.imagio.slot.forge.Forge120GhostStackFactory;
import dev.imagio.slot.forge.network.ForgeWorkspaceViewModelClientCache;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.inventory.workspace.WayfindingTarget;
import dev.imagio.slot.ui.workspace.WayfindingDisplay;
import dev.imagio.slot.ui.workspace.WayfindingGlowMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGuiEvent;

import java.util.List;

public final class ForgeWayfindingHudRenderer {
    private static final int MAX_CHIPS = 5;
    private static final int CHIP_WIDTH = 132;
    private static final int CHIP_HEIGHT = 20;
    private static final int CHIP_GAP = 2;
    private static final int RIGHT_MARGIN = 4;
    private static final int TOP_OFFSET = 70;
    private static final float TEXT_SCALE = 0.75f;
    private static final float ICON_SCALE = 0.625f;
    private static final int ICON_STRIDE = 8;

    private static final int CHIP_FILL = 0xC00C141A;
    private static final int TEXT_RGB = 0xE8EEF2;
    private static final int MUTED_RGB = 0xA0AAB3;
    private static final int ACCENT_RGB = 0x7AC7A7;

    private ForgeWayfindingHudRenderer() {
    }

    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (!ForgeWorkspaceClient.wayfindingHudEnabled()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != null || minecraft.options.hideGui || minecraft.player == null || minecraft.level == null) {
            return;
        }
        SlotWorkspaceViewModel viewModel = ForgeWorkspaceViewModelClientCache.latest();
        List<WayfindingTarget> targets = viewModel == null ? List.of() : viewModel.wayfindingTargets();
        if (targets.isEmpty()) {
            return;
        }

        LocalPlayer player = minecraft.player;
        String dimensionId = minecraft.level.dimension().location().toString();
        WayfindingDisplay.HudTargets split = WayfindingDisplay.splitTargets(
                targets,
                dimensionId,
                player.getX(),
                player.getY(),
                player.getZ());

        GuiGraphics graphics = event.getGuiGraphics();
        int x = graphics.guiWidth() - CHIP_WIDTH - RIGHT_MARGIN;
        int y = TOP_OFFSET;
        int rendered = 0;
        for (WayfindingDisplay.RankedTarget ranked : split.here()) {
            if (rendered >= MAX_CHIPS) {
                break;
            }
            renderChip(graphics, minecraft.font, x, y, ranked.target(), false, player);
            y += CHIP_HEIGHT + CHIP_GAP;
            rendered++;
        }
        int overflow = split.here().size() - rendered;
        if (overflow > 0) {
            renderOverflowChip(graphics, minecraft.font, x, y, overflow);
            y += CHIP_HEIGHT + CHIP_GAP;
        }
        int dimRendered = 0;
        for (WayfindingTarget target : split.elsewhere()) {
            if (dimRendered >= 2) {
                break;
            }
            renderChip(graphics, minecraft.font, x, y, target, true, player);
            y += CHIP_HEIGHT + CHIP_GAP;
            dimRendered++;
        }
        int dimOverflow = split.elsewhere().size() - dimRendered;
        if (dimOverflow > 0) {
            renderOverflowChip(graphics, minecraft.font, x, y, dimOverflow);
        }
    }

    private static void renderChip(
            GuiGraphics graphics,
            Font font,
            int x,
            int y,
            WayfindingTarget target,
            boolean crossDimension,
            LocalPlayer player
    ) {
        graphics.fill(x, y, x + CHIP_WIDTH, y + CHIP_HEIGHT, CHIP_FILL);
        int scopeRgb = WayfindingGlowMath.scopeRgb(target);
        graphics.fill(x, y, x + 2, y + CHIP_HEIGHT, withAlpha(scopeRgb, 0xFF));

        int rightColumnWidth = crossDimension ? 50 : 28;
        int nameAvailable = (int) ((CHIP_WIDTH - 8 - rightColumnWidth) / TEXT_SCALE);
        drawScaled(graphics, font, truncate(font, WayfindingDisplay.targetLabel(target), nameAvailable),
                x + 5, y + 3, TEXT_RGB);

        int iconX = x + 5;
        int iconHeight = (int) (16 * ICON_SCALE);
        int iconY = y + CHIP_HEIGHT - 2 - iconHeight;
        int iconsRendered = 0;
        for (ItemIdentity identity : target.missingIdentities()) {
            if (iconsRendered >= 4) {
                break;
            }
            ItemStack stack = Forge120GhostStackFactory.resolve(identity.itemId());
            if (!stack.isEmpty()) {
                graphics.pose().pushPose();
                graphics.pose().translate(iconX, iconY, 0);
                graphics.pose().scale(ICON_SCALE, ICON_SCALE, 1f);
                graphics.renderFakeItem(stack, 0, 0);
                graphics.pose().popPose();
            }
            iconX += ICON_STRIDE;
            iconsRendered++;
        }
        int itemOverflow = target.missingIdentities().size() - iconsRendered;
        if (itemOverflow > 0) {
            drawScaled(graphics, font, "+" + itemOverflow, iconX, y + CHIP_HEIGHT - 8, MUTED_RGB);
        }

        if (crossDimension) {
            String dim = WayfindingDisplay.shortDimension(target.dimensionId());
            drawRightScaled(graphics, font, dim, x + CHIP_WIDTH - 4, y + 3, ACCENT_RGB);
            String coords = target.worldX() + " " + target.worldY() + " " + target.worldZ();
            drawRightScaled(graphics, font, truncate(font, coords, (int) (60 / TEXT_SCALE)),
                    x + CHIP_WIDTH - 4, y + CHIP_HEIGHT - 8, MUTED_RGB);
        } else {
            WayfindingDisplay.CardText text = WayfindingDisplay.forLocation(
                    target.dimensionId(),
                    target.worldX(),
                    target.worldY(),
                    target.worldZ(),
                    target.dimensionId(),
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    player.getYRot());
            drawRightScaled(graphics, font, text.arrow(), x + CHIP_WIDTH - 4, y + 3, ACCENT_RGB);
            drawRightScaled(graphics, font, text.distance(), x + CHIP_WIDTH - 4, y + CHIP_HEIGHT - 8, MUTED_RGB);
        }
    }

    private static void renderOverflowChip(GuiGraphics graphics, Font font, int x, int y, int overflow) {
        int width = CHIP_WIDTH / 2;
        graphics.fill(x + CHIP_WIDTH - width, y, x + CHIP_WIDTH, y + CHIP_HEIGHT / 2 + 2, CHIP_FILL);
        String text = "+" + overflow + " more";
        float scaledWidth = textWidth(font, text) * TEXT_SCALE;
        drawScaled(graphics, font, text,
                x + CHIP_WIDTH - width / 2 - (int) (scaledWidth / 2),
                y + 2,
                MUTED_RGB);
    }

    private static void drawRightScaled(GuiGraphics graphics, Font font, String text, int rightX, int y, int color) {
        float scaledWidth = textWidth(font, text) * TEXT_SCALE;
        drawScaled(graphics, font, text, rightX - (int) scaledWidth, y, color);
    }

    private static void drawScaled(GuiGraphics graphics, Font font, String text, int x, int y, int color) {
        if (text == null || text.isBlank()) {
            return;
        }
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(TEXT_SCALE, TEXT_SCALE, 1f);
        graphics.drawString(font, uiText(text), 0, 0, color, false);
        graphics.pose().popPose();
    }

    private static int textWidth(Font font, String text) {
        return font.width(uiText(text));
    }

    private static Component uiText(String text) {
        return Component.literal(text == null ? "" : text)
                .withStyle(style -> style.withFont(ForgeSlotUiFonts.uiFont()));
    }

    private static int withAlpha(int rgb, int alpha) {
        return ((alpha & 0xFF) << 24) | (rgb & 0x00FFFFFF);
    }

    private static String truncate(Font font, String text, int maxWidth) {
        if (text == null) {
            return "";
        }
        if (textWidth(font, text) <= maxWidth) {
            return text;
        }
        String ellipsis = "…";
        int ellipsisWidth = textWidth(font, ellipsis);
        StringBuilder sb = new StringBuilder(text);
        while (sb.length() > 0 && textWidth(font, sb.toString()) + ellipsisWidth > maxWidth) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb + ellipsis;
    }
}
