package dev.imagio.slot.neoforge.client.wayfinding;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.workspace.WayfindingTarget;
import dev.imagio.slot.neoforge.client.input.SlotAtlasKeyMappings;
import dev.imagio.slot.neoforge.screen.ldlib.GhostAtlasStackFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Phase 4 of docs/plans/wayfinding.md — the HUD edge stack of
 * wayfinding chips.
 *
 * <p>Renders only when no GUI screen is open and the toggle in
 * {@link SlotAtlasKeyMappings} is on. Walks the cached wayfinding
 * targets, splits them into "current dimension" (compass + distance)
 * vs "cross-dimension" (dim-shorthand + coords), sorts the
 * current-dim list by squared distance, and stacks the top
 * {@link #MAX_CHIPS} along the right edge below vanilla potion
 * territory. The in-world glow handles "where to walk"; the HUD chip
 * lists "what's in there" — both stay visible at any distance.
 */
public final class WayfindingHudRenderer {
    private static final int MAX_CHIPS = 5;
    private static final int CHIP_WIDTH = 132;
    private static final int CHIP_HEIGHT = 20;
    private static final int CHIP_GAP = 2;
    private static final int RIGHT_MARGIN = 4;
    /** Vanilla potion icons occupy the top-right corner; start the chip stack below. */
    private static final int TOP_OFFSET = 70;
    /** Pose-scale applied to all chip text so the HUD doesn't shout at the player. */
    private static final float TEXT_SCALE = 0.75f;
    /** Pose-scale applied to missing-item icons so they fit inside CHIP_HEIGHT. */
    private static final float ICON_SCALE = 0.625f;
    /** Pixel stride between adjacent missing-item icons in the strip. */
    private static final int ICON_STRIDE = 8;

    private static final int CHIP_FILL = 0xC00C141A;
    private static final int TEXT_RGB = 0xE8EEF2;
    private static final int MUTED_RGB = 0xA0AAB3;
    private static final int ACCENT_RGB = 0x7AC7A7;
    private static final int KIT_RGB = 0xFFB347;
    private static final int PLAYER_RGB = 0x4FB8FF;

    private WayfindingHudRenderer() {
    }

    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (!SlotAtlasKeyMappings.wayfindingHudEnabled()) {
            return;
        }
        // Drain the toggle key here too — the client-tick handler in
        // SlotNeoForgeClient already reads other mappings, but we want
        // the toggle to fire whether or not the workspace is open.
        while (SlotAtlasKeyMappings.toggleWayfindingHudMapping().consumeClick()) {
            SlotAtlasKeyMappings.setWayfindingHudEnabled(!SlotAtlasKeyMappings.wayfindingHudEnabled());
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != null) {
            return;
        }
        if (minecraft.options.hideGui || minecraft.player == null || minecraft.level == null) {
            return;
        }
        List<WayfindingTarget> targets = WayfindingTargetCache.targets();
        if (targets.isEmpty()) {
            return;
        }

        LocalPlayer player = minecraft.player;
        String currentDimension = minecraft.level.dimension().location().toString();
        ArrayList<Ranked> here = new ArrayList<>();
        ArrayList<WayfindingTarget> elsewhere = new ArrayList<>();
        for (WayfindingTarget target : targets) {
            if (currentDimension.equals(target.dimensionId())) {
                double dx = (target.worldX() + 0.5) - player.getX();
                double dy = (target.worldY() + 0.5) - player.getY();
                double dz = (target.worldZ() + 0.5) - player.getZ();
                here.add(new Ranked(target, dx * dx + dy * dy + dz * dz));
            } else {
                elsewhere.add(target);
            }
        }
        here.sort(Comparator.comparingDouble(Ranked::distSq));

        GuiGraphics graphics = event.getGuiGraphics();
        int screenWidth = graphics.guiWidth();
        int x = screenWidth - CHIP_WIDTH - RIGHT_MARGIN;
        int y = TOP_OFFSET;

        int rendered = 0;
        for (Ranked ranked : here) {
            if (rendered >= MAX_CHIPS) {
                break;
            }
            // No distance fade: chips are useful at any range because
            // they list which items live in which chest. The in-world
            // glow shows you *where*; the HUD chip tells you *what*.
            renderChip(graphics, minecraft.font, x, y, ranked.target, false, 1.0f, player);
            y += CHIP_HEIGHT + CHIP_GAP;
            rendered++;
        }
        int overflow = here.size() - rendered;
        if (overflow > 0) {
            renderOverflowChip(graphics, minecraft.font, x, y, overflow);
            y += CHIP_HEIGHT + CHIP_GAP;
        }

        // Cross-dimension chips render in a separate stack below the
        // current-dim list so they don't shift around as the player walks.
        // They get a single line of "+N other dim" if the list is long.
        if (!elsewhere.isEmpty()) {
            int dimRendered = 0;
            for (WayfindingTarget target : elsewhere) {
                if (dimRendered >= 2) {
                    break;
                }
                renderChip(graphics, minecraft.font, x, y, target, true, 1.0f, player);
                y += CHIP_HEIGHT + CHIP_GAP;
                dimRendered++;
            }
            int dimOverflow = elsewhere.size() - dimRendered;
            if (dimOverflow > 0) {
                renderOverflowChip(graphics, minecraft.font, x, y, dimOverflow);
            }
        }
    }


    private static void renderChip(
            GuiGraphics graphics,
            Font font,
            int x,
            int y,
            WayfindingTarget target,
            boolean crossDimension,
            float alpha,
            LocalPlayer player
    ) {
        int alphaByte = Math.max(0, Math.min(255, (int) (alpha * 255f)));
        int fill = (CHIP_FILL & 0x00FFFFFF) | (alphaByte << 24);
        graphics.fill(x, y, x + CHIP_WIDTH, y + CHIP_HEIGHT, fill);
        int scopeRgb = target.scope() == WayfindingTarget.Scope.KIT ? KIT_RGB : PLAYER_RGB;
        // Left scope-color stripe so KIT vs PLAYER reads at a glance.
        graphics.fill(x, y, x + 2, y + CHIP_HEIGHT, withAlpha(scopeRgb, alphaByte));

        int textColor = withAlpha(TEXT_RGB, alphaByte);
        int mutedColor = withAlpha(MUTED_RGB, alphaByte);
        int accentColor = withAlpha(ACCENT_RGB, alphaByte);

        // Right-column reservation in scaled-text units. Compass + " 99m"
        // is the worst case (~22 scaled px); cross-dim "nether" + coords
        // (~36 scaled px) — pick the larger so both layouts share one
        // budget.
        int rightColumnWidth = crossDimension ? 50 : 28;
        int nameAvailable = (int) ((CHIP_WIDTH - 8 - rightColumnWidth) / TEXT_SCALE);
        String label = truncate(font, chestLabel(target), nameAvailable);
        drawScaled(graphics, font, label, x + 5, y + 3, textColor);

        // Missing-item icon strip under the label, capped to 4. Vanilla
        // {@code renderFakeItem} draws at a fixed 16×16; pose-scaled
        // down to {@link #ICON_SCALE} so the strip fits inside
        // {@link #CHIP_HEIGHT} without overflowing the chip background.
        int iconX = x + 5;
        // Anchor the bottom of the scaled icon (16 * ICON_SCALE = 10 px
        // tall) two pixels above the chip's bottom edge so it sits
        // visually balanced against the right-column distance line.
        int iconBottomPadding = 2;
        int iconHeight = (int) (16 * ICON_SCALE);
        int iconY = y + CHIP_HEIGHT - iconBottomPadding - iconHeight;
        int iconsRendered = 0;
        for (ItemIdentity identity : target.missingIdentities()) {
            if (iconsRendered >= 4) {
                break;
            }
            ItemStack stack = GhostAtlasStackFactory.resolve(identity.itemId());
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
        int overflow = target.missingIdentities().size() - iconsRendered;
        if (overflow > 0) {
            drawScaled(graphics, font, "+" + overflow, iconX, y + CHIP_HEIGHT - 8, mutedColor);
        }

        // Right column: compass+distance OR dim-shorthand+coords.
        if (crossDimension) {
            String dim = shortDimension(target.dimensionId());
            float scaledDimWidth = font.width(dim) * TEXT_SCALE;
            drawScaled(graphics, font, dim,
                    x + CHIP_WIDTH - (int) scaledDimWidth - 4, y + 3, accentColor);
            String coords = target.worldX() + " " + target.worldY() + " " + target.worldZ();
            String coordsTruncated = truncate(font, coords, (int) (60 / TEXT_SCALE));
            float scaledCoordsWidth = font.width(coordsTruncated) * TEXT_SCALE;
            drawScaled(graphics, font, coordsTruncated,
                    x + CHIP_WIDTH - (int) scaledCoordsWidth - 4, y + CHIP_HEIGHT - 8, mutedColor);
        } else {
            String compass = compassGlyph(player, target);
            float scaledCompassWidth = font.width(compass) * TEXT_SCALE;
            drawScaled(graphics, font, compass,
                    x + CHIP_WIDTH - (int) scaledCompassWidth - 4, y + 3, accentColor);
            int distMeters = (int) Math.round(distance(player, target));
            String distText = String.format(Locale.ROOT, "%dm", Math.max(0, distMeters));
            float scaledDistWidth = font.width(distText) * TEXT_SCALE;
            drawScaled(graphics, font, distText,
                    x + CHIP_WIDTH - (int) scaledDistWidth - 4, y + CHIP_HEIGHT - 8, mutedColor);
        }
    }

    private static void renderOverflowChip(GuiGraphics graphics, Font font, int x, int y, int overflow) {
        int fill = (CHIP_FILL & 0x00FFFFFF) | 0xC0000000;
        int width = CHIP_WIDTH / 2;
        graphics.fill(x + CHIP_WIDTH - width, y, x + CHIP_WIDTH, y + CHIP_HEIGHT / 2 + 2, fill);
        String text = "+" + overflow + " more";
        float scaledWidth = font.width(text) * TEXT_SCALE;
        drawScaled(graphics, font, text,
                x + CHIP_WIDTH - width / 2 - (int) (scaledWidth / 2),
                y + 2, withAlpha(MUTED_RGB, 0xC0));
    }

    /**
     * Draw text scaled by {@link #TEXT_SCALE} in pose units so the HUD
     * chips read at a comfortable size relative to the vanilla overlays.
     * Translate-scale-translate around the draw so the {@code (x, y)} is
     * the visible top-left of the rendered text.
     */
    private static void drawScaled(GuiGraphics graphics, Font font, String text, int x, int y, int color) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(TEXT_SCALE, TEXT_SCALE, 1f);
        graphics.drawString(font, text, 0, 0, color, false);
        graphics.pose().popPose();
    }

    private static int withAlpha(int rgb, int alpha) {
        return ((alpha & 0xFF) << 24) | (rgb & 0x00FFFFFF);
    }

    private static String chestLabel(WayfindingTarget target) {
        // The HUD doesn't have a server-pushed chest label per target, so
        // synthesize "Chest #abcd" from the storageId — same auto-name
        // shape the workspace uses when no custom label is set.
        String storageId = target.storageId();
        if (storageId == null || storageId.isBlank()) {
            return "Chest";
        }
        int dash = storageId.indexOf('-');
        String shortId = dash < 0 ? storageId : storageId.substring(0, dash);
        if (shortId.length() > 4) {
            shortId = shortId.substring(shortId.length() - 4);
        }
        return "Chest #" + shortId;
    }

    private static String truncate(Font font, String text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return text;
        }
        // Drop trailing chars and append a single ellipsis until it fits.
        String ellipsis = "…";
        int ellipsisWidth = font.width(ellipsis);
        StringBuilder sb = new StringBuilder(text);
        while (sb.length() > 0 && font.width(sb.toString()) + ellipsisWidth > maxWidth) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString() + ellipsis;
    }

    private static String compassGlyph(LocalPlayer player, WayfindingTarget target) {
        double dx = (target.worldX() + 0.5) - player.getX();
        double dz = (target.worldZ() + 0.5) - player.getZ();
        float yawRadians = (float) Math.toRadians(player.getYRot());
        double absoluteBearing = Math.atan2(-dx, dz);
        double relativeBearing = absoluteBearing - yawRadians;
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

    private static double distance(LocalPlayer player, WayfindingTarget target) {
        double dx = (target.worldX() + 0.5) - player.getX();
        double dy = (target.worldY() + 0.5) - player.getY();
        double dz = (target.worldZ() + 0.5) - player.getZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
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

    private record Ranked(WayfindingTarget target, double distSq) {
    }
}
