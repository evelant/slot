package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.ui.spi.SlotUiElement;
import dev.imagio.slot.ui.spi.SlotUiEventKind;
import dev.imagio.slot.ui.spi.SlotUiLayout;
import dev.imagio.slot.ui.spi.SlotUiTextStyle;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static dev.imagio.slot.ui.workspace.WorkspaceUiPalette.ACCENT;
import static dev.imagio.slot.ui.workspace.WorkspaceUiPalette.MUTED;
import static dev.imagio.slot.ui.workspace.WorkspaceUiPalette.ROW_DIM;
import static dev.imagio.slot.ui.workspace.WorkspaceUiPalette.ROW_HOVER;
import static dev.imagio.slot.ui.workspace.WorkspaceUiPalette.TEXT;

public final class StoragePanelUiBuilder {
    private static final int PANEL = 0xC8162029;
    private static final int CHIP = 0xC024313D;
    private static final int MATCH = 0xE0345749;
    private static final int HOVER_OVERLAY = 0x55365743;
    private static final int HEADER_HEIGHT_PX = 12;
    private static final int CHIP_HEIGHT_PX = 18;
    private static final int ICON_SIZE_PX = 10;
    private static final int MAX_ICONS = 4;
    private static final int MAX_VISIBLE_CHIPS = 5;

    private final Context context;

    public StoragePanelUiBuilder(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context is required");
        }
        this.context = context;
    }

    public SlotUiElement overlay(SlotWorkspaceViewModel viewModel) {
        if (viewModel == null) {
            return null;
        }
        List<SlotWorkspaceViewModel.ChestChip> visible = visibleChips(viewModel);
        if (visible.isEmpty()) {
            return null;
        }

        SlotUiElement panel = SlotUiElement.panel(PANEL)
                .zIndex(5)
                .attach(WorkspaceUiAttachments.STORAGE_PANEL, Boolean.TRUE)
                .layout(layout -> layout
                        .widthPercent(100)
                        .paddingAll(5)
                        .gapAll(3)
                        .flexDirection(SlotUiLayout.FlexDirection.COLUMN));
        panel.on(SlotUiEventKind.MOUSE_DOWN, event -> event.stopPropagation(), true);
        panel.addChild(header(visible.size()));

        Map<String, Integer> visiblePerCluster = visiblePerCluster(visible);
        String currentClusterId = null;
        int rendered = 0;
        for (SlotWorkspaceViewModel.ChestChip chip : visible) {
            if (rendered >= MAX_VISIBLE_CHIPS) {
                break;
            }
            if (!chip.clusterId().equals(currentClusterId)) {
                currentClusterId = chip.clusterId();
                if (visiblePerCluster.getOrDefault(currentClusterId, 0) > 1) {
                    panel.addChild(clusterHeader(viewModel, currentClusterId));
                }
            }
            panel.addChild(chestChip(chip));
            rendered++;
        }
        int hidden = visible.size() - rendered;
        if (hidden > 0) {
            panel.addChild(SlotUiElement.label("+" + hidden + " more", MUTED)
                    .layout(layout -> layout.widthPercent(100).height(10))
                    .textStyle(style -> style
                            .color(MUTED)
                            .fontSize(6)
                            .horizontal(SlotUiTextStyle.Horizontal.LEFT)
                            .vertical(SlotUiTextStyle.Vertical.CENTER)));
        }
        return panel;
    }

    private SlotUiElement header(int visibleCount) {
        return SlotUiElement.element()
                .layout(layout -> layout
                        .widthPercent(100)
                        .height(HEADER_HEIGHT_PX)
                        .alignItems(SlotUiLayout.AlignItems.CENTER)
                        .flexDirection(SlotUiLayout.FlexDirection.ROW))
                .addChild(SlotUiElement.label("Nearby chests", ACCENT)
                        .layout(layout -> layout.flex(1).heightPercent(100))
                        .textStyle(style -> style
                                .color(ACCENT)
                                .fontSize(8)
                                .horizontal(SlotUiTextStyle.Horizontal.LEFT)
                                .vertical(SlotUiTextStyle.Vertical.CENTER)))
                .addChild(SlotUiElement.label(Integer.toString(visibleCount), MUTED)
                        .layout(layout -> layout.width(20).heightPercent(100))
                        .textStyle(style -> style
                                .color(MUTED)
                                .fontSize(7)
                                .horizontal(SlotUiTextStyle.Horizontal.RIGHT)
                                .vertical(SlotUiTextStyle.Vertical.CENTER)));
    }

    private SlotUiElement clusterHeader(SlotWorkspaceViewModel viewModel, String clusterId) {
        SlotWorkspaceViewModel.ChestClusterDescriptor descriptor = cluster(viewModel, clusterId);
        String label = descriptor == null || descriptor.label().isBlank()
                ? "Storage Area"
                : descriptor.label();
        return SlotUiElement.label(label, MUTED)
                .layout(layout -> layout.widthPercent(100).height(10))
                .textStyle(style -> style
                        .color(MUTED)
                        .fontSize(6)
                        .horizontal(SlotUiTextStyle.Horizontal.LEFT)
                        .vertical(SlotUiTextStyle.Vertical.CENTER));
    }

    private SlotUiElement chestChip(SlotWorkspaceViewModel.ChestChip chip) {
        WayfindingDisplay.CardText wayfinding = context.wayfindingText(chip);
        SlotUiElement button = SlotUiElement.button("", true, chipColor(chip))
                .noText()
                .tooltipLines(tooltipLines(chip, wayfinding))
                .attach(WorkspaceUiAttachments.STORAGE_CHIP, chip)
                .layout(layout -> layout
                        .widthPercent(100)
                        .height(CHIP_HEIGHT_PX)
                        .paddingHorizontal(4)
                        .paddingVertical(2)
                        .gapAll(3)
                        .alignItems(SlotUiLayout.AlignItems.CENTER)
                        .flexDirection(SlotUiLayout.FlexDirection.ROW));
        button.on(SlotUiEventKind.MOUSE_ENTER, event -> context.hoverStorage(chip.storageId()));
        button.on(SlotUiEventKind.MOUSE_LEAVE, event -> context.clearHoveredStorage(chip.storageId()));
        button.on(SlotUiEventKind.TICK, event -> button.overlayColor(
                context.isHoveredIdentityPresentInChest(chip.storageId()) ? HOVER_OVERLAY : null));
        button.on(SlotUiEventKind.CLICK, event -> {
            if (event.button() != 0) {
                return;
            }
            event.stopPropagation();
            if (context.isCursorCarrying()) {
                context.dropCursorIntoChest(chip.storageId());
                return;
            }
            context.setStatus(displayLabel(chip) + " "
                    + chip.filledSlots() + "/" + chip.slotCapacity() + " slots");
        });

        button.addChild(SlotUiElement.label(displayLabel(chip), TEXT)
                .layout(layout -> layout.flex(1).heightPercent(100))
                .textStyle(style -> style
                        .color(TEXT)
                        .fontSize(7)
                        .horizontal(SlotUiTextStyle.Horizontal.LEFT)
                        .vertical(SlotUiTextStyle.Vertical.CENTER)));
        addIconStrip(button, chip);
        button.addChild(SlotUiElement.label(wayfinding.arrow(), ACCENT)
                .layout(layout -> layout.width(10).heightPercent(100))
                .textStyle(style -> style
                        .color(ACCENT)
                        .fontSize(7)
                        .horizontal(SlotUiTextStyle.Horizontal.CENTER)
                        .vertical(SlotUiTextStyle.Vertical.CENTER)));
        button.addChild(SlotUiElement.label(wayfinding.distance(), MUTED)
                .layout(layout -> layout.width(24).heightPercent(100))
                .textStyle(style -> style
                        .color(MUTED)
                        .fontSize(6)
                        .horizontal(SlotUiTextStyle.Horizontal.RIGHT)
                        .vertical(SlotUiTextStyle.Vertical.CENTER)));
        return button;
    }

    private void addIconStrip(SlotUiElement button, SlotWorkspaceViewModel.ChestChip chip) {
        List<SlotWorkspaceViewModel.ChestContentSummary> contents = chip.contents();
        if (contents.isEmpty()) {
            button.addChild(SlotUiElement.label(chip.affinityIdentities() + " links", MUTED)
                    .layout(layout -> layout.width(34).heightPercent(100))
                    .textStyle(style -> style
                            .color(MUTED)
                            .fontSize(6)
                            .horizontal(SlotUiTextStyle.Horizontal.RIGHT)
                            .vertical(SlotUiTextStyle.Vertical.CENTER)));
            return;
        }
        SlotUiElement strip = SlotUiElement.element()
                .allowHitTest(false)
                .layout(layout -> layout
                        .heightPercent(100)
                        .gapAll(1)
                        .alignItems(SlotUiLayout.AlignItems.CENTER)
                        .flexDirection(SlotUiLayout.FlexDirection.ROW));
        int rendered = 0;
        for (SlotWorkspaceViewModel.ChestContentSummary summary : contents) {
            if (rendered >= MAX_ICONS) {
                break;
            }
            if (summary.displayStack().isEmpty()) {
                continue;
            }
            strip.addChild(SlotUiElement.itemIcon(summary.displayStack(), ICON_SIZE_PX, false)
                    .renderVanillaCount(false)
                    .layout(layout -> layout.width(ICON_SIZE_PX).height(ICON_SIZE_PX)));
            rendered++;
        }
        int hidden = contents.size() - rendered;
        if (hidden > 0) {
            strip.addChild(SlotUiElement.label("+" + hidden, MUTED)
                    .layout(layout -> layout.width(14).height(10))
                    .textStyle(style -> style
                            .color(MUTED)
                            .fontSize(6)
                            .horizontal(SlotUiTextStyle.Horizontal.LEFT)
                            .vertical(SlotUiTextStyle.Vertical.CENTER)));
        }
        button.addChild(strip);
    }

    private List<SlotWorkspaceViewModel.ChestChip> visibleChips(SlotWorkspaceViewModel viewModel) {
        ArrayList<SlotWorkspaceViewModel.ChestChip> visible = new ArrayList<>();
        for (SlotWorkspaceViewModel.ChestChip chip : viewModel.chestChips()) {
            if (chip != null && isVisible(viewModel, chip)) {
                visible.add(chip);
            }
        }
        return visible;
    }

    private boolean isVisible(SlotWorkspaceViewModel viewModel, SlotWorkspaceViewModel.ChestChip chip) {
        if (chip.proximate()) {
            return true;
        }
        if (context.normalizedSearchQuery().isBlank()) {
            return false;
        }
        return chestHasSearchMatch(viewModel, chip.storageId());
    }

    private boolean chestHasSearchMatch(SlotWorkspaceViewModel viewModel, String storageId) {
        for (SlotWorkspaceViewModel.AtlasItem item : viewModel.atlasItems()) {
            if (!context.matchesSearch(item)) {
                continue;
            }
            if (presenceContains(item.presence(), storageId) || presenceContains(item.elsewhere(), storageId)) {
                return true;
            }
        }
        return false;
    }

    private static boolean presenceContains(List<SlotWorkspaceViewModel.ChestPresenceEntry> entries, String storageId) {
        if (entries == null || storageId == null || storageId.isBlank()) {
            return false;
        }
        for (SlotWorkspaceViewModel.ChestPresenceEntry entry : entries) {
            if (entry != null && storageId.equals(entry.storageId())) {
                return true;
            }
        }
        return false;
    }

    private static Map<String, Integer> visiblePerCluster(List<SlotWorkspaceViewModel.ChestChip> visible) {
        LinkedHashMap<String, Integer> counts = new LinkedHashMap<>();
        for (SlotWorkspaceViewModel.ChestChip chip : visible) {
            counts.merge(chip.clusterId(), 1, Integer::sum);
        }
        return counts;
    }

    private static SlotWorkspaceViewModel.ChestClusterDescriptor cluster(
            SlotWorkspaceViewModel viewModel,
            String clusterId
    ) {
        if (clusterId == null || clusterId.isBlank()) {
            return null;
        }
        for (SlotWorkspaceViewModel.ChestClusterDescriptor descriptor : viewModel.chestClusters()) {
            if (descriptor != null && clusterId.equals(descriptor.clusterId())) {
                return descriptor;
            }
        }
        return null;
    }

    private static String displayLabel(SlotWorkspaceViewModel.ChestChip chip) {
        return chip.label().isBlank() ? "Chest" : chip.label();
    }

    private static int chipColor(SlotWorkspaceViewModel.ChestChip chip) {
        if (!chip.proximate()) {
            return MATCH;
        }
        return chip.filledSlots() >= chip.slotCapacity() && chip.slotCapacity() > 0 ? ROW_DIM : ROW_HOVER;
    }

    private static List<Component> tooltipLines(
            SlotWorkspaceViewModel.ChestChip chip,
            WayfindingDisplay.CardText wayfinding
    ) {
        ArrayList<Component> lines = new ArrayList<>();
        lines.add(Component.literal(displayLabel(chip)));
        lines.add(Component.literal(chip.filledSlots() + "/" + chip.slotCapacity()
                + " slots - " + chip.affinityIdentities() + " learned items"));
        if (wayfinding != null) {
            lines.add(Component.literal(wayfinding.arrow() + " " + wayfinding.distance()));
        }
        lines.add(Component.literal("Left-click while carrying to deposit here"));
        return lines;
    }

    public interface Context {
        String normalizedSearchQuery();

        boolean matchesSearch(SlotWorkspaceViewModel.AtlasItem item);

        boolean isCursorCarrying();

        WayfindingDisplay.CardText wayfindingText(SlotWorkspaceViewModel.ChestChip chip);

        boolean isHoveredIdentityPresentInChest(String storageId);

        void hoverStorage(String storageId);

        void clearHoveredStorage(String storageId);

        void dropCursorIntoChest(String storageId);

        void setStatus(String nextStatus);
    }
}
