package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.ui.spi.SlotUiElement;
import dev.imagio.slot.ui.spi.SlotUiEventKind;
import dev.imagio.slot.ui.spi.SlotUiLayout;
import dev.imagio.slot.ui.spi.SlotUiTextStyle;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class WorkflowTabsUiBuilder {
    public static final int TAB_ROW_HEIGHT_PX = 18;
    public static final int MAX_TAB_STACK_HEIGHT_PX = TAB_ROW_HEIGHT_PX * 2 + 2;

    private final Context context;

    public WorkflowTabsUiBuilder(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context is required");
        }
        this.context = context;
    }

    public SlotUiElement tabs(SlotWorkspaceViewModel viewModel) {
        SlotWorkspaceViewModel resolved = viewModel == null ? SlotWorkspaceViewModel.empty() : viewModel;
        SlotWorkspaceViewModel.KitCard active = resolved.activeKit();
        SlotWorkspaceViewModel.KitCard activeParent = activeParent(resolved, active);
        List<SlotWorkspaceViewModel.KitCard> variants = activeParent == null
                ? List.of()
                : variantsOf(resolved, activeParent.kitId());
        SlotUiElement column = SlotUiElement.panel(0xB810171D)
                .layout(layout -> layout
                        .widthPercent(100)
                        .height(height(resolved))
                        .paddingAll(2)
                        .gapAll(2)
                        .flexDirection(SlotUiLayout.FlexDirection.COLUMN));
        column.addChild(parentRow(resolved, active, activeParent));
        if (!variants.isEmpty()) {
            column.addChild(variantRow(variants));
        }
        return column;
    }

    public static int height(SlotWorkspaceViewModel viewModel) {
        SlotWorkspaceViewModel resolved = viewModel == null ? SlotWorkspaceViewModel.empty() : viewModel;
        SlotWorkspaceViewModel.KitCard active = resolved.activeKit();
        SlotWorkspaceViewModel.KitCard activeParent = activeParent(resolved, active);
        return activeParent != null && !variantsOf(resolved, activeParent.kitId()).isEmpty()
                ? MAX_TAB_STACK_HEIGHT_PX
                : TAB_ROW_HEIGHT_PX;
    }

    private SlotUiElement parentRow(
            SlotWorkspaceViewModel viewModel,
            SlotWorkspaceViewModel.KitCard active,
            SlotWorkspaceViewModel.KitCard activeParent
    ) {
        SlotUiElement row = row();
        row.addChild(tabButton(
                "All",
                active == null,
                "Show the unfiltered wall",
                34,
                context::selectAll,
                null
        ));
        row.addChild(tabButton(
                "+",
                false,
                "Create a workflow",
                16,
                context::createTab,
                null
        ));
        for (SlotWorkspaceViewModel.KitCard tab : viewModel.kits()) {
            if (tab == null || tab.variant()) {
                continue;
            }
            boolean selected = activeParent != null && activeParent.kitId().equals(tab.kitId());
            row.addChild(tabButton(
                    tabText(tab),
                    selected,
                    "Activate workflow. Right-click for workflow actions.",
                    tabWidth(tab),
                    () -> context.selectTab(tab.kitId()),
                    (x, y) -> context.openTabMenu(tab.kitId(), x, y)
            ));
        }
        return row;
    }

    private SlotUiElement variantRow(List<SlotWorkspaceViewModel.KitCard> variants) {
        SlotUiElement row = row();
        row.addChild(SlotUiElement.label("Variants", WorkspaceUiPalette.MUTED)
                .layout(layout -> layout.width(42).height(14))
                .textStyle(style -> style
                        .color(WorkspaceUiPalette.MUTED)
                        .fontSize(6)
                        .horizontal(SlotUiTextStyle.Horizontal.LEFT)
                        .vertical(SlotUiTextStyle.Vertical.CENTER)));
        for (SlotWorkspaceViewModel.KitCard variant : variants) {
            row.addChild(tabButton(
                    tabText(variant),
                    variant.active(),
                    "Activate workflow variant. Right-click for variant actions.",
                    tabWidth(variant),
                    () -> context.selectTab(variant.kitId()),
                    (x, y) -> context.openTabMenu(variant.kitId(), x, y)
            ));
        }
        return row;
    }

    private static SlotUiElement row() {
        return SlotUiElement.element()
                .layout(layout -> layout
                        .widthPercent(100)
                        .height(14)
                        .gapAll(3)
                        .alignItems(SlotUiLayout.AlignItems.CENTER)
                        .flexDirection(SlotUiLayout.FlexDirection.ROW));
    }

    private SlotUiElement tabButton(
            String text,
            boolean active,
            String tooltip,
            int width,
            Runnable onClick,
            MenuOpener menuOpener
    ) {
        int color = active ? WorkspaceUiPalette.SELECTED : WorkspaceUiPalette.ROW_DIM;
        SlotUiElement button = SlotUiElement.button(text, true, color)
                .tooltip(Component.literal(tooltip == null ? "" : tooltip))
                .layout(layout -> layout
                        .width(width)
                        .height(14)
                        .paddingHorizontal(6))
                .textStyle(style -> style
                        .color(WorkspaceUiPalette.TEXT)
                        .fontSize(7)
                        .horizontal(SlotUiTextStyle.Horizontal.CENTER)
                        .vertical(SlotUiTextStyle.Vertical.CENTER))
                .on(SlotUiEventKind.CLICK, event -> {
                    if (event.button() != 0) {
                        return;
                    }
                    event.stopPropagation();
                    if (onClick != null) {
                        onClick.run();
                    }
                });
        if (menuOpener != null) {
            button.on(SlotUiEventKind.MOUSE_DOWN, event -> {
                if (event.button() != 1) {
                    return;
                }
                event.stopPropagation();
                menuOpener.open(event.x(), event.y());
            }, true);
        }
        return button;
    }

    private static SlotWorkspaceViewModel.KitCard activeParent(
            SlotWorkspaceViewModel viewModel,
            SlotWorkspaceViewModel.KitCard active
    ) {
        if (viewModel == null || active == null) {
            return active;
        }
        if (!active.variant()) {
            return active;
        }
        SlotWorkspaceViewModel.KitCard parent = viewModel.kit(active.parentId());
        return parent == null ? active : parent;
    }

    private static List<SlotWorkspaceViewModel.KitCard> variantsOf(
            SlotWorkspaceViewModel viewModel,
            String parentId
    ) {
        if (viewModel == null || parentId == null || parentId.isBlank()) {
            return List.of();
        }
        ArrayList<SlotWorkspaceViewModel.KitCard> variants = new ArrayList<>();
        for (SlotWorkspaceViewModel.KitCard card : viewModel.kits()) {
            if (card != null && parentId.equals(card.parentId())) {
                variants.add(card);
            }
        }
        return List.copyOf(variants);
    }

    private static String tabText(SlotWorkspaceViewModel.KitCard tab) {
        String label = tab == null ? "Workflow" : tab.name();
        if (label == null || label.isBlank()) {
            label = "Workflow";
        }
        int memberCount = tab == null ? 0 : tab.memberCount();
        int targetCount = memberCount + (tab == null ? 0 : tab.slotCount());
        String clipped = label.length() > 13 ? label.substring(0, 12) + "." : label;
        return targetCount > 0 ? clipped + " " + targetCount : clipped;
    }

    private static int tabWidth(SlotWorkspaceViewModel.KitCard tab) {
        String text = tabText(tab);
        return Math.max(48, Math.min(112, text.length() * 6 + 14));
    }

    private interface MenuOpener {
        void open(float x, float y);
    }

    public interface Context {
        void selectAll();

        void selectTab(String kitId);

        void createTab();

        void openTabMenu(String kitId, float screenX, float screenY);
    }
}
