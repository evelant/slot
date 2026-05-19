package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.ui.spi.SlotUiElement;
import dev.imagio.slot.ui.spi.SlotUiEventKind;
import dev.imagio.slot.ui.spi.SlotUiLayout;
import dev.imagio.slot.ui.spi.SlotUiTextStyle;
import dev.imagio.slot.workflow.domain.KitPage;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static dev.imagio.slot.ui.workspace.WorkspaceUiPalette.ACCENT;
import static dev.imagio.slot.ui.workspace.WorkspaceUiPalette.MUTED;
import static dev.imagio.slot.ui.workspace.WorkspaceUiPalette.ROW_DIM;
import static dev.imagio.slot.ui.workspace.WorkspaceUiPalette.ROW_HOVER;
import static dev.imagio.slot.ui.workspace.WorkspaceUiPalette.SELECTED;
import static dev.imagio.slot.ui.workspace.WorkspaceUiPalette.TEXT;

public final class KitRackUiBuilder {
    public static final int CLUSTER_HEIGHT_PX = 22;
    public static final int KIT_CELL_SIZE_PX = 14;
    public static final int KIT_CELL_ICON_SIZE_PX = 11;

    private static final int PANEL = 0xC8162029;
    private static final int PANEL_ALT = 0xE01C2832;
    private static final int GLASS = 0xE80B1117;
    private static final int ROW = 0xC926313B;
    private static final int ACTIVE = 0xE0A97935;
    private static final int ACTIVE_PAGE = 0x80365643;
    private static final int EMPTY_CELL = 0x60141B22;
    private static final int GHOST_CELL = 0x80343D49;
    private static final int WARNING = 0xFFFFD166;

    private final Context context;

    public KitRackUiBuilder(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context is required");
        }
        this.context = context;
    }

    public SlotUiElement cluster(SlotWorkspaceViewModel viewModel, boolean open) {
        return cluster(viewModel, open, false);
    }

    public SlotUiElement cluster(SlotWorkspaceViewModel viewModel, boolean open, boolean compact) {
        SlotWorkspaceViewModel.KitCard active = viewModel == null ? null : viewModel.activeKit();
        String label = clusterLabel(active, compact);
        SlotUiElement cluster = SlotUiElement.element()
                .attach(WorkspaceUiAttachments.KIT_CLUSTER, Boolean.TRUE)
                .layout(layout -> layout
                        .height(CLUSTER_HEIGHT_PX)
                        .gapAll(3)
                        .alignItems(SlotUiLayout.AlignItems.CENTER)
                        .flexDirection(SlotUiLayout.FlexDirection.ROW));
        SlotUiElement toggle = button(label, open || active != null, open ? ACCENT : active == null ? MUTED : TEXT)
                .layout(layout -> layout
                        .width(Math.max(compact ? 34 : 44, label.length() * (compact ? 4 : 5) + (compact ? 8 : 12)))
                        .height(CLUSTER_HEIGHT_PX));
        toggle.on(SlotUiEventKind.CLICK, event -> {
            if (event.button() != 0) {
                return;
            }
            event.stopPropagation();
            context.toggleKitRack();
        });
        cluster.addChild(toggle);
        if (active != null && active.pageCount() > 1) {
            SlotUiElement page = button(">", true, TEXT)
                    .layout(layout -> layout.width(compact ? 12 : 16).height(CLUSTER_HEIGHT_PX));
            page.on(SlotUiEventKind.CLICK, event -> {
                if (event.button() != 0 && event.button() != 1) {
                    return;
                }
                event.stopPropagation();
                context.switchActiveKitPage(event.button() == 1 || event.shiftDown() ? -1 : 1);
            });
            cluster.addChild(page);
        }
        return cluster;
    }

    private static String clusterLabel(SlotWorkspaceViewModel.KitCard active, boolean compact) {
        if (active == null) {
            return "Tabs";
        }
        if (compact) {
            return active.pageCount() > 1
                    ? "Page " + (active.activePageIndex() + 1) + "/" + active.pageCount()
                    : shorten(active.name(), 7);
        }
        return shorten(active.name(), 10) + (active.pageCount() > 1
                ? " " + (active.activePageIndex() + 1) + "/" + active.pageCount()
                : "");
    }

    public SlotUiElement rack(SlotWorkspaceViewModel viewModel) {
        List<SlotWorkspaceViewModel.KitCard> kits = viewModel == null ? List.of() : viewModel.kits();
        SlotUiElement rack = SlotUiElement.panel(GLASS)
                .zIndex(7)
                .attach(WorkspaceUiAttachments.KIT_RACK, Boolean.TRUE)
                .layout(layout -> layout
                        .widthPercent(100)
                        .paddingAll(6)
                        .gapAll(6)
                        .flexDirection(SlotUiLayout.FlexDirection.COLUMN));
        rack.on(SlotUiEventKind.MOUSE_DOWN, event -> event.stopPropagation(), true);
        rack.addChild(header(kits));
        if (kits.isEmpty()) {
            rack.addChild(emptyBody());
            return rack;
        }
        SlotUiElement body = SlotUiElement.element()
                .layout(layout -> layout
                        .widthPercent(100)
                        .gapAll(5)
                        .flexDirection(SlotUiLayout.FlexDirection.COLUMN));
        for (SlotWorkspaceViewModel.KitCard kit : kits) {
            body.addChild(kitCard(kit));
        }
        rack.addChild(body);
        return rack;
    }

    private SlotUiElement header(List<SlotWorkspaceViewModel.KitCard> kits) {
        int count = kits == null ? 0 : kits.size();
        SlotWorkspaceViewModel.KitCard active = activeKit(kits);
        String saveText = active == null
                ? "Save Belt"
                : active.pageCount() > 1 ? "Update Page " + (active.activePageIndex() + 1) : "Update Tab";
        SlotUiElement row = SlotUiElement.element()
                .layout(layout -> layout
                        .widthPercent(100)
                        .height(16)
                        .gapAll(4)
                        .alignItems(SlotUiLayout.AlignItems.CENTER)
                        .flexDirection(SlotUiLayout.FlexDirection.ROW));
        row.addChild(SlotUiElement.label("Tabs (" + count + ")", ACCENT)
                .layout(layout -> layout.flex(1).height(12))
                .textStyle(style -> style
                        .color(ACCENT)
                        .fontSize(9)
                        .horizontal(SlotUiTextStyle.Horizontal.LEFT)
                        .vertical(SlotUiTextStyle.Vertical.CENTER)));
        SlotUiElement create = button("New Tab", true, PANEL_ALT)
                .layout(layout -> layout.width(48).height(14));
        create.on(SlotUiEventKind.CLICK, event -> {
            if (event.button() != 0) {
                return;
            }
            event.stopPropagation();
            context.createEmptyTab();
        });
        row.addChild(create);
        SlotUiElement save = button(saveText, true, ACCENT)
                .layout(layout -> layout.width(Math.max(58, saveText.length() * 5 + 12)).height(14));
        save.on(SlotUiEventKind.CLICK, event -> {
            if (event.button() != 0) {
                return;
            }
            event.stopPropagation();
            context.saveCurrentBeltAsKit();
        });
        row.addChild(save);
        SlotUiElement close = button("x", true, MUTED)
                .layout(layout -> layout.width(14).height(14));
        close.on(SlotUiEventKind.CLICK, event -> {
            if (event.button() != 0) {
                return;
            }
            event.stopPropagation();
            context.closeKitRack();
        });
        row.addChild(close);
        return row;
    }

    private SlotUiElement emptyBody() {
        return SlotUiElement.panel(PANEL)
                .layout(layout -> layout
                        .widthPercent(100)
                        .height(32)
                        .paddingAll(4)
                        .alignItems(SlotUiLayout.AlignItems.CENTER))
                .addChild(SlotUiElement.label("No workflow tabs yet. Create one or save the current belt.", MUTED)
                        .layout(layout -> layout.widthPercent(100).height(12))
                        .textStyle(style -> style
                                .color(MUTED)
                                .fontSize(8)
                                .horizontal(SlotUiTextStyle.Horizontal.CENTER)
                                .vertical(SlotUiTextStyle.Vertical.CENTER)));
    }

    private SlotUiElement kitCard(SlotWorkspaceViewModel.KitCard card) {
        SlotUiElement button = SlotUiElement.button("", true, card.active() ? ACTIVE : ROW)
                .noText()
                .attach(WorkspaceUiAttachments.KIT_CARD, card)
                .layout(layout -> layout
                        .widthPercent(100)
                        .paddingAll(4)
                        .gapAll(3)
                        .alignItems(SlotUiLayout.AlignItems.CENTER)
                        .flexDirection(SlotUiLayout.FlexDirection.COLUMN));
        button.on(SlotUiEventKind.CLICK, event -> {
            if (event.button() != 0) {
                return;
            }
            event.stopPropagation();
            if (card.active()) {
                context.deactivateKit();
            } else {
                context.activateKit(card.kitId());
            }
        });
        button.on(SlotUiEventKind.MOUSE_DOWN, event -> {
            if (event.button() != 1) {
                return;
            }
            event.stopPropagation();
            context.openKitMenu(card.kitId(), event.x(), event.y());
        });
        button.addChild(kitCardHeader(card));
        for (SlotWorkspaceViewModel.KitPageView page : card.pages()) {
            button.addChild(kitCardPageRow(card, page));
        }
        button.addChild(kitCardActions(card));
        return button;
    }

    private SlotUiElement kitCardHeader(SlotWorkspaceViewModel.KitCard card) {
        int totalSlots = 0;
        int totalReady = 0;
        for (SlotWorkspaceViewModel.KitPageView page : card.pages()) {
            totalSlots += page.slotCount();
            totalReady += page.readyCount();
        }
        int readinessColor = totalSlots > 0 && totalReady == totalSlots ? ACCENT : WARNING;
        return SlotUiElement.element()
                .layout(layout -> layout
                        .widthPercent(100)
                        .height(12)
                        .gapAll(3)
                        .alignItems(SlotUiLayout.AlignItems.CENTER)
                        .flexDirection(SlotUiLayout.FlexDirection.ROW))
                .addChild(SlotUiElement.label(shorten(card.name(), 24), TEXT)
                        .layout(layout -> layout.flex(1).height(10))
                        .textStyle(style -> style
                                .color(TEXT)
                                .fontSize(8)
                                .horizontal(SlotUiTextStyle.Horizontal.LEFT)
                                .vertical(SlotUiTextStyle.Vertical.CENTER)))
                .addChild(SlotUiElement.label(totalReady + "/" + totalSlots, readinessColor)
                        .layout(layout -> layout.width(30).height(10))
                        .textStyle(style -> style
                                .color(readinessColor)
                                .fontSize(8)
                                .horizontal(SlotUiTextStyle.Horizontal.RIGHT)
                                .vertical(SlotUiTextStyle.Vertical.CENTER)));
    }

    private SlotUiElement kitCardPageRow(SlotWorkspaceViewModel.KitCard card, SlotWorkspaceViewModel.KitPageView page) {
        boolean activePage = card.active() && card.activePageIndex() == page.pageIndex();
        SlotUiElement row = SlotUiElement.panel(activePage ? ACTIVE_PAGE : 0x00000000)
                .attach(WorkspaceUiAttachments.KIT_PAGE, page)
                .layout(layout -> layout
                        .widthPercent(100)
                        .height(KIT_CELL_SIZE_PX + 2)
                        .paddingHorizontal(2)
                        .gapAll(2)
                        .alignItems(SlotUiLayout.AlignItems.CENTER)
                        .flexDirection(SlotUiLayout.FlexDirection.ROW));
        row.addChild(SlotUiElement.label(Integer.toString(page.pageIndex() + 1), activePage ? ACCENT : MUTED)
                .layout(layout -> layout.width(8).height(12))
                .textStyle(style -> style
                        .color(activePage ? ACCENT : MUTED)
                        .fontSize(8)
                        .horizontal(SlotUiTextStyle.Horizontal.CENTER)
                        .vertical(SlotUiTextStyle.Vertical.CENTER)));
        SlotUiElement strip = SlotUiElement.element()
                .layout(layout -> layout
                        .flex(1)
                        .height(KIT_CELL_SIZE_PX)
                        .gapAll(1)
                        .alignItems(SlotUiLayout.AlignItems.CENTER)
                        .flexDirection(SlotUiLayout.FlexDirection.ROW));
        for (SlotWorkspaceViewModel.KitSlotState slot : page.slots()) {
            strip.addChild(kitSlotCell(card, page, slot));
        }
        row.addChild(strip);
        if (card.pageCount() > 1) {
            SlotUiElement remove = button("-", true, MUTED)
                    .layout(layout -> layout.width(12).height(12));
            remove.on(SlotUiEventKind.CLICK, event -> {
                if (event.button() != 0) {
                    return;
                }
                event.stopPropagation();
                context.removeKitPage(card.kitId(), page.pageIndex());
            });
            row.addChild(remove);
        }
        return row;
    }

    private SlotUiElement kitCardActions(SlotWorkspaceViewModel.KitCard card) {
        SlotUiElement row = SlotUiElement.element()
                .layout(layout -> layout
                        .widthPercent(100)
                        .height(14)
                        .gapAll(3)
                        .alignItems(SlotUiLayout.AlignItems.CENTER)
                        .flexDirection(SlotUiLayout.FlexDirection.ROW));
        boolean canAdd = card.carriedSlotCount() + KitPage.HOTBAR_SLOT_COUNT <= card.carriedSlotCapacity();
        SlotUiElement add = button(canAdd ? "+ page" : "+ page full", canAdd, canAdd ? MUTED : ROW_DIM)
                .layout(layout -> layout.flex(1).height(12));
        add.on(SlotUiEventKind.CLICK, event -> {
            if (event.button() != 0) {
                return;
            }
            event.stopPropagation();
            if (canAdd) {
                context.addKitPage(card.kitId());
            } else {
                context.setStatus("no room for another tab page");
            }
        });
        row.addChild(add);

        List<SlotWorkspaceViewModel.IdentityRef> missing = missingIdentities(card);
        if (!missing.isEmpty()) {
            int pullable = countPullable(missing);
            String label = pullable > 0 ? "gather " + pullable : "need " + missing.size();
            SlotUiElement gather = button(label, pullable > 0, pullable > 0 ? WARNING : MUTED)
                    .layout(layout -> layout.flex(1).height(12));
            gather.on(SlotUiEventKind.CLICK, event -> {
                if (event.button() != 0) {
                    return;
                }
                event.stopPropagation();
                if (pullable <= 0) {
                    context.setStatus(missing.size() + " tab targets not in nearby chests");
                    return;
                }
                int requested = 0;
                for (SlotWorkspaceViewModel.IdentityRef identity : missing) {
                    if (context.proximateCount(identity) > 0) {
                        context.takeStackByIdentity(identity);
                        requested++;
                    }
                }
                context.setStatus("gathering " + requested + " tab target" + (requested == 1 ? "" : "s"));
            });
            row.addChild(gather);
        }
        return row;
    }

    private SlotUiElement kitSlotCell(
            SlotWorkspaceViewModel.KitCard card,
            SlotWorkspaceViewModel.KitPageView page,
            SlotWorkspaceViewModel.KitSlotState slot
    ) {
        int fill = !slot.filled() ? EMPTY_CELL : slot.ready() ? ROW : GHOST_CELL;
        SlotUiElement cell = SlotUiElement.panel(fill)
                .attach(WorkspaceUiAttachments.KIT_SLOT, slot)
                .layout(layout -> layout
                        .width(KIT_CELL_SIZE_PX)
                        .height(KIT_CELL_SIZE_PX)
                        .paddingAll(1)
                        .alignItems(SlotUiLayout.AlignItems.CENTER));
        cell.on(SlotUiEventKind.MOUSE_DOWN, event -> {
            if (event.button() == 0) {
                event.stopPropagation();
                return;
            }
            if (event.button() == 1 && slot.filled()) {
                event.stopPropagation();
                context.clearKitSlot(card.kitId(), page.pageIndex(), slot.slotIndex());
            }
        });
        if (slot.filled() && !slot.displayStack().isEmpty()) {
            cell.tooltipStack(slot.displayStack());
            cell.addChild(SlotUiElement.itemIcon(slot.displayStack(), KIT_CELL_ICON_SIZE_PX, slot.ready())
                    .renderVanillaCount(false));
        }
        return cell;
    }

    private int countPullable(List<SlotWorkspaceViewModel.IdentityRef> identities) {
        int count = 0;
        for (SlotWorkspaceViewModel.IdentityRef identity : identities) {
            if (context.proximateCount(identity) > 0) {
                count++;
            }
        }
        return count;
    }

    private static List<SlotWorkspaceViewModel.IdentityRef> missingIdentities(SlotWorkspaceViewModel.KitCard card) {
        LinkedHashSet<SlotWorkspaceViewModel.IdentityRef> missing = new LinkedHashSet<>();
        for (SlotWorkspaceViewModel.KitPageView page : card.pages()) {
            for (SlotWorkspaceViewModel.KitSlotState slot : page.slots()) {
                if (slot.filled() && !slot.ready()) {
                    missing.add(slot.identity());
                }
            }
        }
        for (SlotWorkspaceViewModel.KitBringItem item : card.bring()) {
            if (!item.ready()) {
                missing.add(item.identity());
            }
        }
        return List.copyOf(new ArrayList<>(missing));
    }

    private static SlotWorkspaceViewModel.KitCard activeKit(List<SlotWorkspaceViewModel.KitCard> kits) {
        if (kits == null) {
            return null;
        }
        for (SlotWorkspaceViewModel.KitCard kit : kits) {
            if (kit != null && kit.active()) {
                return kit;
            }
        }
        return null;
    }

    private static SlotUiElement button(String text, boolean active, int textColor) {
        return SlotUiElement.button(text, active, active ? PANEL_ALT : 0x40202020)
                .textStyle(style -> style
                        .color(textColor)
                        .fontSize(8)
                        .horizontal(SlotUiTextStyle.Horizontal.CENTER)
                        .vertical(SlotUiTextStyle.Vertical.CENTER));
    }

    private static String shorten(String text, int max) {
        String value = text == null ? "" : text;
        if (value.length() <= max) {
            return value;
        }
        return value.substring(0, Math.max(0, max - 1)) + ".";
    }

    public interface Context {
        void toggleKitRack();

        void closeKitRack();

        void saveCurrentBeltAsKit();

        void createEmptyTab();

        void activateKit(String kitId);

        void deactivateKit();

        void switchActiveKitPage(int direction);

        void addKitPage(String kitId);

        void removeKitPage(String kitId, int pageIndex);

        void clearKitSlot(String kitId, int pageIndex, int slotIndex);

        void takeStackByIdentity(SlotWorkspaceViewModel.IdentityRef identity);

        int proximateCount(SlotWorkspaceViewModel.IdentityRef identity);

        void setStatus(String status);

        default void openKitMenu(String kitId, float screenX, float screenY) {
        }
    }
}
