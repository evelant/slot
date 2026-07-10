package dev.imagio.slot.neoforge.screen.ldlib;

import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.ACCENT;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.CARRIED_CHIP_DANGER;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.CARRIED_CHIP_OK;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.CARRIED_CHIP_WARN;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.GHOST_CARD_ALPHA;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.MUTED;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.ROW_HOVER;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.SELECTED;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.WARNING;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceUi.shorten;

import com.lowdragmc.lowdraglib2.gui.util.DrawerHelper;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.ui.workspace.WorkspaceItemTooltipBuilder;
import dev.imagio.slot.ui.workspace.WorkspaceUiPalette;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Locale;

final class WorkspaceFormat {
    private WorkspaceFormat() {
    }

    static String formatFreeSlots(int count) {
        return Component.translatable("slot.screen.inventory.free_slots", count).getString();
    }

    static int fullnessColor(int filled, int capacity) {
        if (capacity <= 0) {
            return CARRIED_CHIP_WARN;
        }
        float ratio = Math.min(1f, Math.max(0f, (float) filled / capacity));
        if (ratio >= 1f) {
            return CARRIED_CHIP_DANGER;
        }
        if (ratio >= 0.75f) {
            return CARRIED_CHIP_WARN;
        }
        return CARRIED_CHIP_OK;
    }

    static float fullnessBarWidth(int filled, int capacity, float total) {
        if (capacity <= 0 || filled <= 0) {
            return 0f;
        }
        float ratio = Math.min(1f, (float) filled / capacity);
        return Math.max(0f, total * ratio);
    }

    static float clampScreenFontPx(float screenPx) {
        // Quantize to 0.5 screen-px steps. LDLib's TextElement runs a full
        // formattedLines recompute whenever fontSize changes, so a fully
        // continuous font size thrashes layout every zoom frame. Half-px
        // steps keep zooming text stable while cutting recompute frequency
        // in half vs. continuous.
        float clamped = Math.max(3f, screenPx);
        return Math.round(clamped * 2f) / 2f;
    }

    /**
     * Discrete zoom breakpoints for the island header font. 0.5-px
     * quantization (clampScreenFontPx) is effectively continuous — the
     * header visibly shrinks frame-by-frame as the atlas zooms and the
     * carried-count badge (fixed world size = 12) drifts outside the
     * header's world bounds. Breakpoints keep the header's on-screen
     * size at four discrete tiers so the badge either clearly fits or
     * clearly doesn't; combined with the world-height floor enforced
     * below, the badge never overflows.
     */
    static float headerBreakpointFontPx(float screenPx) {
        if (screenPx < 8f) return 7f;
        if (screenPx < 10f) return 9f;
        if (screenPx < 12f) return 11f;
        return 12f;
    }

    static String modToken(SlotWorkspaceViewModel.AtlasItem item, int maxLength) {
        if (item == null) {
            return "";
        }
        String namespace = namespace(item.identity().itemId());
        if (namespace.isBlank() || "minecraft".equals(namespace)) {
            return "";
        }
        return compactItemLabel(namespace.replace('_', ' ').replace('-', ' '), maxLength);
    }

    static String tooltipVariantToken(SlotWorkspaceViewModel.AtlasItem item, int maxLength) {
        List<Component> tooltipLines = vanillaAtlasTooltipLines(item);
        String name = item == null ? "" : item.name();
        String namespace = namespace(item == null ? "" : item.identity().itemId());
        for (Component line : tooltipLines) {
            if (line == null) {
                continue;
            }
            String normalized = normalizeTooltipText(line.getString());
            if (normalized.isBlank()) {
                continue;
            }
            if (normalizeTooltipText(name).equals(normalized)) {
                continue;
            }
            if (isGenericTooltipToken(normalized, namespace)) {
                continue;
            }
            return compactAnchorText(normalized, maxLength);
        }
        return "";
    }

    static String namespace(String itemId) {
        if (itemId == null) {
            return "";
        }
        int separator = itemId.indexOf(':');
        return separator < 0 ? "" : itemId.substring(0, separator);
    }

    static String normalizeTooltipText(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace('\n', ' ')
                .replaceAll("\\s+", " ")
                .replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit} ]", "")
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    static boolean isGenericTooltipToken(String normalizedText, String namespace) {
        if (normalizedText.isBlank()) {
            return true;
        }
        String normalizedNamespace = normalizeTooltipText(namespace == null ? "" : namespace.replace('_', ' '));
        if (!normalizedNamespace.isBlank() && normalizedText.equals(normalizedNamespace)) {
            return true;
        }
        return normalizedText.startsWith("hold ")
                || normalizedText.startsWith("press ")
                || normalizedText.startsWith("when ")
                || normalizedText.contains(" shift")
                || normalizedText.contains(" ctrl")
                || normalizedText.contains("details")
                || normalizedText.startsWith("durability")
                || normalizedText.startsWith("emc ")
                || normalizedText.startsWith("energy ")
                || normalizedText.startsWith("burn time");
    }

    static String compactAnchorText(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        String normalized = text.replace('\n', ' ').replaceAll("\\s+", " ").trim();
        if (normalized.isBlank()) {
            return "";
        }
        if (maxLength <= 0 || normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, maxLength);
    }

    static String compactItemLabel(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        String normalized = text.replace('\n', ' ').replaceAll("\\s+", " ").trim();
        if (normalized.isBlank()) {
            return "";
        }
        String[] words = normalized.split(" ");
        if (words.length >= 2) {
            StringBuilder builder = new StringBuilder();
            for (int index = 0; index < Math.min(2, words.length); index++) {
                String word = words[index];
                int remaining = maxLength - builder.length() - (builder.isEmpty() ? 0 : 1);
                if (remaining <= 0) {
                    break;
                }
                String piece = truncateWord(word, remaining);
                if (piece.isBlank()) {
                    continue;
                }
                if (!builder.isEmpty()) {
                    builder.append(' ');
                }
                builder.append(piece);
            }
            if (!builder.isEmpty()) {
                return builder.toString();
            }
        }
        return shorten(normalized, maxLength);
    }

    static String truncateWord(String word, int maxLength) {
        if (word == null || word.isBlank() || maxLength <= 0) {
            return "";
        }
        return word.length() <= maxLength ? word : word.substring(0, maxLength);
    }

    static int itemMarkerColor(SlotWorkspaceViewModel.AtlasItem item, SlotWorkspaceViewModel.AtlasIsland island) {
        if (item.recent()) {
            return WARNING;
        }
        if (item.playerPlaced()) {
            return ACCENT;
        }
        return island != null && island.kind() != VisualAtlasIslandKind.TRIAGE
                ? 0xFF94D8B8
                : MUTED;
    }

    static String compactCount(int count) {
        if (count <= 0) {
            return "0";
        }
        if (count > 99) {
            return "99+";
        }
        return Integer.toString(count);
    }


    static String selectionHomeStatus(SlotWorkspaceViewModel.AtlasItem item, SlotWorkspaceViewModel.AtlasIsland island) {
        if (item.playerPlaced()) {
            return "player-placed home";
        }
        if (island != null && island.kind() != VisualAtlasIslandKind.TRIAGE) {
            return "starter home";
        }
        return "awaiting placement";
    }


    static int cardChromeColor(
            boolean selected,
            boolean searchMatch,
            boolean recent,
            boolean carried,
            boolean searchActive
    ) {
        int base = cardChromeBaseColor(selected, searchMatch, searchActive);
        if (!carried && !selected) {
            base = dimAlpha(base, GHOST_CARD_ALPHA);
        }
        return base;
    }

    private static int cardChromeBaseColor(
            boolean selected,
            boolean searchMatch,
            boolean searchActive
    ) {
        if (selected) {
            return SELECTED;
        }
        if (!searchActive) {
            return 0xC926313B;
        }
        return searchMatch ? ROW_HOVER : 0x2824313D;
    }

    static int dimAlpha(int color, float alphaFactor) {
        int alpha = (color >>> 24) & 0xFF;
        int dimmed = Math.round(alpha * alphaFactor);
        return (dimmed << 24) | (color & 0x00FFFFFF);
    }

    static String itemName(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "empty";
        }
        String name = stack.getHoverName().getString();
        return name.isBlank() ? "unknown" : name;
    }

    static List<Component> atlasTooltipLines(SlotWorkspaceViewModel.AtlasItem item) {
        return atlasTooltipLines(item, WorkspaceItemTooltipBuilder.slotLines(item));
    }

    static List<Component> atlasTooltipLines(
            SlotWorkspaceViewModel.AtlasItem item,
            List<Component> extraLines
    ) {
        List<Component> vanilla = vanillaAtlasTooltipLines(item);
        List<Component> slot = styledSlotTooltipLines(extraLines);
        if (slot.isEmpty()) {
            return vanilla;
        }
        java.util.ArrayList<Component> lines = new java.util.ArrayList<>(vanilla.size() + slot.size());
        lines.addAll(vanilla);
        lines.addAll(slot);
        return List.copyOf(lines);
    }

    static ItemStack atlasTooltipStack(SlotWorkspaceViewModel.AtlasItem item) {
        if (item == null || item.fluidResource() || item.displayStack().isEmpty()) {
            return ItemStack.EMPTY;
        }
        return item.displayStack();
    }

    private static List<Component> vanillaAtlasTooltipLines(SlotWorkspaceViewModel.AtlasItem item) {
        ItemStack tooltipStack = atlasTooltipStack(item);
        if (tooltipStack.isEmpty()) {
            return List.of();
        }
        return List.copyOf(DrawerHelper.getItemToolTip(tooltipStack));
    }

    private static List<Component> styledSlotTooltipLines(List<Component> lines) {
        if (lines == null || lines.isEmpty()) {
            return List.of();
        }
        java.util.ArrayList<Component> styled = new java.util.ArrayList<>(lines.size());
        for (Component line : lines) {
            styled.add(styleSlotTooltipLine(line));
        }
        return List.copyOf(styled);
    }

    private static Component styleSlotTooltipLine(Component line) {
        if (line == null) {
            return Component.empty();
        }
        String text = line.getString();
        if (text == null || text.isBlank()) {
            return Component.empty();
        }
        int color = slotTooltipColor(text);
        return Component.literal(text)
                .withStyle(style -> style.withColor(TextColor.fromRgb(color & 0x00FFFFFF)));
    }

    private static int slotTooltipColor(String text) {
        if ("SLOT".equals(text)) {
            return WorkspaceUiPalette.ACCENT;
        }
        if (text.startsWith("Desired target")) {
            return WorkspaceUiPalette.COUNT_BADGE_DESIRED;
        }
        if (text.startsWith("Wanted target")) {
            return WorkspaceUiPalette.COUNT_BADGE_WANTED;
        }
        if (text.startsWith("Nearby stored") || text.startsWith("Nearby route")) {
            return WorkspaceUiPalette.ACCENT;
        }
        if (text.startsWith("Stored elsewhere")) {
            return WorkspaceUiPalette.MUTED;
        }
        if (text.startsWith("Workflow target")) {
            return WorkspaceUiPalette.COUNT_BADGE_WORKFLOW;
        }
        if (text.startsWith("Container")) {
            return 0xFF8DB7D6;
        }
        if (text.startsWith("Contextual score")) {
            return WorkspaceUiPalette.ACCENT;
        }
        if (text.startsWith("  +")) {
            return 0xFF7AC7A7;
        }
        if (text.startsWith("  -")) {
            return WARNING;
        }
        return WorkspaceUiPalette.TEXT;
    }
}
