package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.ui.spi.SlotUiElement;
import dev.imagio.slot.ui.spi.SlotUiEventKind;
import dev.imagio.slot.ui.spi.SlotUiLayout;
import dev.imagio.slot.ui.spi.SlotUiTextStyle;

import java.util.List;

import static dev.imagio.slot.ui.workspace.WorkspaceUiPalette.GHOST_CARD_ALPHA;
import static dev.imagio.slot.ui.workspace.WorkspaceUiPalette.ROW_HOVER;
import static dev.imagio.slot.ui.workspace.WorkspaceUiPalette.ROW_MATCH;
import static dev.imagio.slot.ui.workspace.WorkspaceUiPalette.SELECTED;
import static dev.imagio.slot.ui.workspace.WorkspaceUiPalette.TEXT;

public final class WallCardUiBuilder {
    public static final int CARD_CELL_PX = 22;
    public static final int WAYFINDING_STRIP_WIDTH_PX = 36;

    private final Context context;

    public WallCardUiBuilder(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context is required");
        }
        this.context = context;
    }

    public SlotUiElement card(SlotWorkspaceViewModel.AtlasItem item) {
        boolean selected = item.identity().equals(context.activeIdentity());
        boolean searchMatch = context.matchesItem(item);
        boolean filtering = !context.normalizedSearchQuery().isBlank();
        boolean activeSearchMatch = filtering && searchMatch;
        SlotWorkspaceViewModel.ChestPresenceEntry wayfindingEntry = wayfindingEntryFor(item, activeSearchMatch);
        boolean expanded = wayfindingEntry != null;
        int cardWidth = expanded ? CARD_CELL_PX + WAYFINDING_STRIP_WIDTH_PX : CARD_CELL_PX;

        SlotUiElement button = SlotUiElement.button("",
                        true,
                        cardChromeColor(selected, searchMatch, item.recent(), item.carried(), filtering))
                .noText()
                .zIndex(2)
                .tooltipStack(item.displayStack())
                .attach(WorkspaceUiAttachments.WALL_CARD, Boolean.TRUE)
                .attach(WorkspaceUiAttachments.ATLAS_ITEM, item)
                .layout(layout -> layout
                        .width(cardWidth)
                        .height(CARD_CELL_PX)
                        .paddingAll(0));
        button.on(SlotUiEventKind.MOUSE_ENTER, event -> context.hoverAtlasIdentity(item.identity()), true);
        button.on(SlotUiEventKind.MOUSE_LEAVE, event -> context.clearHoveredAtlasIdentity(item.identity()), true);
        button.on(SlotUiEventKind.TICK, event -> {
            boolean currentSelected = item.identity().equals(context.activeIdentity());
            boolean focused = context.isMapFocusItem(item);
            button.zIndex(focused ? 10 : currentSelected ? 7 : 2);
            button.buttonColor(cardChromeColor(
                    currentSelected,
                    searchMatch,
                    item.recent(),
                    item.carried(),
                    !context.normalizedSearchQuery().isBlank()
            ));
        });

        SlotUiElement body = SlotUiElement.element()
                .allowHitTest(false)
                .attach(WorkspaceUiAttachments.WALL_CARD_BODY, Boolean.TRUE)
                .attach(WorkspaceUiAttachments.ATLAS_ITEM, item)
                .attach(WorkspaceUiAttachments.WALL_CARD_ACTIVE_SEARCH_MATCH, activeSearchMatch)
                .layout(layout -> layout
                        .widthPercent(100)
                        .heightPercent(100)
                        .paddingAll(3)
                        .alignItems(SlotUiLayout.AlignItems.CENTER)
                        .flexDirection(SlotUiLayout.FlexDirection.ROW));
        if (wayfindingEntry != null) {
            body.attach(WorkspaceUiAttachments.WALL_CARD_WAYFINDING_ENTRY, wayfindingEntry);
        }
        buildFallbackBody(body, item);
        button.addChild(body);
        return button;
    }

    private void buildFallbackBody(SlotUiElement body, SlotWorkspaceViewModel.AtlasItem item) {
        body.addChild(SlotUiElement.itemIcon(item.displayStack(), 16, item.carried())
                .renderVanillaCount(false));
        addCountBadge(body, item);
        addProximatePip(body, item);
        addElsewherePip(body, item);
        addDesiredMarker(body, item);
        addWayfindingStrip(body);
    }

    public static int cardChromeColor(
            boolean selected,
            boolean searchMatch,
            boolean recent,
            boolean carried,
            boolean searchActive
    ) {
        int base = cardChromeBaseColor(selected, searchMatch, recent, searchActive);
        if (!carried && !selected) {
            base = dimAlpha(base, GHOST_CARD_ALPHA);
        }
        return base;
    }

    private static int cardChromeBaseColor(
            boolean selected,
            boolean searchMatch,
            boolean recent,
            boolean searchActive
    ) {
        if (selected) {
            return SELECTED;
        }
        if (!searchActive) {
            return recent ? ROW_MATCH : 0xC926313B;
        }
        return searchMatch ? (recent ? ROW_MATCH : ROW_HOVER) : 0x2824313D;
    }

    private static int dimAlpha(int color, float alphaFactor) {
        int alpha = (color >>> 24) & 0xFF;
        int dimmed = Math.round(alpha * alphaFactor);
        return (dimmed << 24) | (color & 0x00FFFFFF);
    }

    private static SlotWorkspaceViewModel.ChestPresenceEntry wayfindingEntryFor(
            SlotWorkspaceViewModel.AtlasItem item,
            boolean activeSearchMatch
    ) {
        if (item == null || item.elsewhere().isEmpty()) {
            return null;
        }
        boolean wantsPointer = activeSearchMatch
                || (hasDesiredGap(item) && item.presence().isEmpty());
        if (!wantsPointer) {
            return null;
        }
        SlotWorkspaceViewModel.ChestPresenceEntry best = null;
        int bestCount = -1;
        for (SlotWorkspaceViewModel.ChestPresenceEntry entry : item.elsewhere()) {
            if (entry == null) {
                continue;
            }
            if (entry.count() > bestCount) {
                bestCount = entry.count();
                best = entry;
            }
        }
        return best;
    }

    private static boolean hasDesiredGap(SlotWorkspaceViewModel.AtlasItem item) {
        if (item == null) {
            return false;
        }
        if (item.kitNeeded()) {
            return true;
        }
        return item.desiredCount() > 0 && item.totalCount() < item.desiredCount();
    }

    private static void addCountBadge(SlotUiElement body, SlotWorkspaceViewModel.AtlasItem item) {
        String text = countBadgeText(item);
        if (text.isBlank()) {
            return;
        }
        int width = Math.max(7, text.length() * 4 + 2);
        int textColor = item.kitNeeded() ? 0xFFFFD166 : TEXT;
        SlotUiElement badge = SlotUiElement.panel(0xD00C141A)
                .allowHitTest(false)
                .zIndex(12)
                .layout(layout -> layout
                        .positionType(SlotUiLayout.PositionType.ABSOLUTE)
                        .right(1)
                        .bottom(1)
                        .width(width)
                        .height(6));
        badge.addChild(SlotUiElement.label(text, textColor)
                .layout(layout -> layout.widthPercent(100).heightPercent(100))
                .textStyle(style -> style
                        .fontSize(6)
                        .color(textColor)
                        .horizontal(SlotUiTextStyle.Horizontal.CENTER)
                        .vertical(SlotUiTextStyle.Vertical.CENTER)));
        body.addChild(badge);
    }

    private static void addProximatePip(SlotUiElement body, SlotWorkspaceViewModel.AtlasItem item) {
        if (presenceCount(item.presence()) <= 0) {
            return;
        }
        body.addChild(SlotUiElement.panel(0xE07AC7A7)
                .allowHitTest(false)
                .zIndex(10)
                .layout(layout -> layout
                        .positionType(SlotUiLayout.PositionType.ABSOLUTE)
                        .left(1)
                        .top(1)
                        .width(5)
                        .height(5)));
    }

    private static void addElsewherePip(SlotUiElement body, SlotWorkspaceViewModel.AtlasItem item) {
        if (presenceCount(item.elsewhere()) <= 0) {
            return;
        }
        body.addChild(SlotUiElement.panel(0xE0809ACB)
                .allowHitTest(false)
                .zIndex(9)
                .layout(layout -> layout
                        .positionType(SlotUiLayout.PositionType.ABSOLUTE)
                        .right(1)
                        .top(1)
                        .width(5)
                        .height(5)));
    }

    private static void addDesiredMarker(SlotUiElement body, SlotWorkspaceViewModel.AtlasItem item) {
        if (!hasDesiredGap(item)) {
            return;
        }
        body.addChild(SlotUiElement.panel(0xE0FFD166)
                .allowHitTest(false)
                .zIndex(11)
                .layout(layout -> layout
                        .positionType(SlotUiLayout.PositionType.ABSOLUTE)
                        .left(1)
                        .bottom(1)
                        .width(5)
                        .height(5)));
    }

    private void addWayfindingStrip(SlotUiElement body) {
        SlotWorkspaceViewModel.ChestPresenceEntry entry = body.attachment(
                WorkspaceUiAttachments.WALL_CARD_WAYFINDING_ENTRY,
                SlotWorkspaceViewModel.ChestPresenceEntry.class);
        if (entry == null) {
            return;
        }
        SlotUiElement strip = SlotUiElement.panel(0xAA1F3448)
                .allowHitTest(false)
                .zIndex(8)
                .layout(layout -> layout
                        .width(28)
                        .height(16)
                        .paddingHorizontal(1)
                        .alignItems(SlotUiLayout.AlignItems.CENTER)
                        .flexDirection(SlotUiLayout.FlexDirection.ROW));
        WayfindingDisplay.CardText initial = wayfindingText(entry);
        SlotUiElement arrow = SlotUiElement.label(initial.arrow(), 0xFFBFD8FF)
                .layout(layout -> layout.width(9).heightPercent(100))
                .textStyle(style -> style
                        .fontSize(6)
                        .color(0xFFBFD8FF)
                        .horizontal(SlotUiTextStyle.Horizontal.CENTER)
                        .vertical(SlotUiTextStyle.Vertical.CENTER));
        SlotUiElement distance = SlotUiElement.label(initial.distance(), 0xFFA0AAB3)
                .layout(layout -> layout.flex(1).heightPercent(100))
                .textStyle(style -> style
                        .fontSize(6)
                        .color(0xFFA0AAB3)
                        .horizontal(SlotUiTextStyle.Horizontal.RIGHT)
                        .vertical(SlotUiTextStyle.Vertical.CENTER));
        strip.addChild(arrow);
        strip.addChild(distance);
        strip.on(SlotUiEventKind.TICK, event -> {
            WayfindingDisplay.CardText next = wayfindingText(entry);
            arrow.text(next.arrow());
            distance.text(next.distance());
        });
        body.addChild(strip);
    }

    private WayfindingDisplay.CardText wayfindingText(SlotWorkspaceViewModel.ChestPresenceEntry entry) {
        WayfindingDisplay.CardText text = context.wayfindingText(entry);
        if (text != null) {
            return text;
        }
        return new WayfindingDisplay.CardText(">", WorkspaceCountFormat.compact(entry.count()));
    }

    private static String countBadgeText(SlotWorkspaceViewModel.AtlasItem item) {
        int carried = Math.max(0, item.totalCount());
        int desired = Math.max(0, item.desiredCount());
        if (desired > 0) {
            return (carried <= 0 ? "0" : WorkspaceCountFormat.compact(carried))
                    + "/" + WorkspaceCountFormat.compact(desired);
        }
        return WorkspaceCountFormat.compact(carried);
    }

    private static int presenceCount(List<SlotWorkspaceViewModel.ChestPresenceEntry> entries) {
        int count = 0;
        if (entries == null) {
            return 0;
        }
        for (SlotWorkspaceViewModel.ChestPresenceEntry entry : entries) {
            if (entry != null) {
                count += Math.max(0, entry.count());
            }
        }
        return count;
    }

    public interface Context {
        SlotWorkspaceViewModel.IdentityRef activeIdentity();

        String normalizedSearchQuery();

        boolean matchesItem(SlotWorkspaceViewModel.AtlasItem item);

        boolean isMapFocusItem(SlotWorkspaceViewModel.AtlasItem item);

        void hoverAtlasIdentity(SlotWorkspaceViewModel.IdentityRef identity);

        void clearHoveredAtlasIdentity(SlotWorkspaceViewModel.IdentityRef identity);

        default WayfindingDisplay.CardText wayfindingText(SlotWorkspaceViewModel.ChestPresenceEntry entry) {
            return null;
        }
    }
}
