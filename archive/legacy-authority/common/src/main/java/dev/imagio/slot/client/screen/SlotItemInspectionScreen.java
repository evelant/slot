package dev.imagio.slot.client.screen;

import dev.imagio.slot.client.category.CategorySignal;
import dev.imagio.slot.client.model.ComparisonMode;
import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.client.policy.ItemBehaviorPolicy;
import dev.imagio.slot.workflow.InspectionService;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.StringJoiner;

public final class SlotItemInspectionScreen extends Screen {
    private static final int PANEL_WIDTH = 440;
    private static final int PANEL_HEIGHT = 320;

    private final Screen parentScreen;
    private final InspectionService.InspectionView inspectionView;

    public SlotItemInspectionScreen(
            Screen parentScreen,
            InspectionService.InspectionView inspectionView
    ) {
        super(Component.translatable("slot.screen.inspect.title"));
        this.parentScreen = parentScreen;
        this.inspectionView = inspectionView == null
                ? new InspectionService.InspectionView(null, null, Component.empty(), null)
                : inspectionView;
    }

    @Override
    protected void init() {
        super.init();
        int panelLeft = panelLeft();
        int panelTop = panelTop();
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(panelLeft + 12, panelTop + PANEL_HEIGHT - 28, PANEL_WIDTH - 24, 20)
                .build());
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(parentScreen);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderTransparentBackground(guiGraphics);

        int panelLeft = panelLeft();
        int panelTop = panelTop();
        int lineWidth = PANEL_WIDTH - 24;
        int textLeft = panelLeft + 12;
        int y = panelTop + 12;

        guiGraphics.fill(panelLeft, panelTop, panelLeft + PANEL_WIDTH, panelTop + PANEL_HEIGHT, 0xD0101010);
        guiGraphics.drawCenteredString(font, title, width / 2, y, 0xFFFFFF);
        y += 16;

        guiGraphics.drawString(font, Component.translatable("slot.screen.inspect.subtitle"), textLeft, y, 0xB8B8B8, false);
        y += 18;

        ItemIdentity identity = inspectionView.identity();
        var iconStack = inspectionView.displayStack();
        if (!iconStack.isEmpty()) {
            guiGraphics.renderItem(iconStack, textLeft, y);
        }
        guiGraphics.drawString(font, inspectionName(), textLeft + 22, y + 4, 0xF0F0F0, false);
        y += 26;

        ItemBehaviorPolicy.ItemInspection inspection = inspectionView.inspection();
        y = drawWrappedLine(guiGraphics, Component.translatable("slot.screen.inspect.item_id", valueOrNone(identity == null ? null : identity.itemId())), textLeft, y, lineWidth);
        y = drawWrappedLine(guiGraphics, Component.translatable("slot.screen.inspect.identity_mode", comparisonModeLabel(identity == null ? null : identity.comparisonMode())), textLeft, y, lineWidth);
        if (identity != null && identity.comparisonMode() == ComparisonMode.ITEM_ID_AND_COMPONENTS && !identity.componentFingerprint().isBlank()) {
            y = drawWrappedLine(guiGraphics, Component.translatable("slot.screen.inspect.component_fingerprint", abbreviate(identity.componentFingerprint())), textLeft, y, lineWidth);
        }

        y = drawWrappedLine(guiGraphics, Component.translatable("slot.screen.inspect.category", inspection.category().displayName()), textLeft, y, lineWidth);
        y = drawWrappedLine(guiGraphics, Component.translatable("slot.screen.inspect.category_source", inspection.categoryResolution().source().displayName()), textLeft, y, lineWidth);
        y = drawWrappedLine(guiGraphics, Component.translatable("slot.screen.inspect.signals", formatSignals(inspection.categoryResolution().signals())), textLeft, y, lineWidth);
        y = drawWrappedLine(guiGraphics, Component.translatable("slot.screen.inspect.fallback_group", formatFallbackGrouping(inspection)), textLeft, y, lineWidth);
        y = drawWrappedLine(guiGraphics, Component.translatable("slot.screen.inspect.collections", formatCollections(inspection)), textLeft, y, lineWidth);
        y = drawWrappedLine(guiGraphics, Component.translatable("slot.screen.inspect.desired_counts", formatDesiredCounts(inspection)), textLeft, y, lineWidth);
        y = drawWrappedLine(guiGraphics, Component.translatable("slot.screen.inspect.direct_action", formatDirectAction(inspection)), textLeft, y, lineWidth);
        y = drawWrappedLine(guiGraphics, Component.translatable("slot.screen.inspect.stable_identity", yesNo(inspection.compatibility().stableMovableIdentity())), textLeft, y, lineWidth);
        y = drawWrappedLine(guiGraphics, Component.translatable("slot.screen.inspect.bulk_store_protected", yesNo(inspection.compatibility().protectFromBulkStore())), textLeft, y, lineWidth);
        drawWrappedLine(guiGraphics, Component.translatable("slot.screen.inspect.implicit_junk", yesNo(inspection.compatibility().implicitJunkCandidate())), textLeft, y, lineWidth);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private Component inspectionName() {
        return inspectionView.displayName();
    }

    private int drawWrappedLine(GuiGraphics guiGraphics, Component component, int x, int y, int width) {
        for (var line : font.split(component, width)) {
            guiGraphics.drawString(font, line, x, y, 0xD8D8D8, false);
            y += 10;
        }
        return y + 4;
    }

    private int panelLeft() {
        return width / 2 - PANEL_WIDTH / 2;
    }

    private int panelTop() {
        return Math.max(18, height / 2 - PANEL_HEIGHT / 2);
    }

    private static String comparisonModeLabel(ComparisonMode mode) {
        if (mode == null) {
            return noneLabel();
        }
        return switch (mode) {
            case ITEM_ID -> "Item ID";
            case ITEM_ID_AND_COMPONENTS -> "Item ID + Components";
        };
    }

    private static String formatSignals(Iterable<CategorySignal> signals) {
        StringJoiner joiner = new StringJoiner(", ");
        for (CategorySignal signal : signals) {
            joiner.add(toTitleCase(signal.name()));
        }
        String value = joiner.toString();
        return value.isBlank() ? noneLabel() : value;
    }

    private static String formatFallbackGrouping(ItemBehaviorPolicy.ItemInspection inspection) {
        if (inspection.fallbackGrouping() == null) {
            return noneLabel();
        }
        return inspection.fallbackGrouping().label() + " (" + inspection.fallbackGrouping().id() + ")";
    }

    private static String formatCollections(ItemBehaviorPolicy.ItemInspection inspection) {
        if (inspection.collections().isEmpty()) {
            return noneLabel();
        }
        StringJoiner joiner = new StringJoiner(", ");
        for (String collectionId : inspection.collections()) {
            joiner.add(collectionId);
        }
        return joiner.toString();
    }

    private static String formatDesiredCounts(ItemBehaviorPolicy.ItemInspection inspection) {
        if (inspection.desiredCountsByCollection().isEmpty()) {
            return noneLabel();
        }
        StringJoiner joiner = new StringJoiner(", ");
        inspection.desiredCountsByCollection().forEach((collectionId, desiredCount) -> joiner.add(collectionId + "=" + desiredCount));
        return joiner.toString();
    }

    private static String formatDirectAction(ItemBehaviorPolicy.ItemInspection inspection) {
        ItemBehaviorPolicy.DirectInventoryAction action = inspection.compatibility().directInventoryAction();
        if (action == null) {
            return noneLabel();
        }
        return Component.translatable(action.translationKey()).getString();
    }

    private static String yesNo(boolean value) {
        return value ? "Yes" : "No";
    }

    private static String valueOrNone(String value) {
        return value == null || value.isBlank() ? noneLabel() : value;
    }

    private static String abbreviate(String value) {
        if (value == null || value.isBlank()) {
            return noneLabel();
        }
        return value.length() <= 96 ? value : value.substring(0, 93) + "...";
    }

    private static String toTitleCase(String raw) {
        String normalized = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT).replace('_', ' ');
        if (normalized.isBlank()) {
            return noneLabel();
        }

        StringJoiner joiner = new StringJoiner(" ");
        for (String word : normalized.split("\\s+")) {
            if (word.isBlank()) {
                continue;
            }
            joiner.add(Character.toUpperCase(word.charAt(0)) + word.substring(1));
        }
        return joiner.toString();
    }

    private static String noneLabel() {
        return "None";
    }
}
