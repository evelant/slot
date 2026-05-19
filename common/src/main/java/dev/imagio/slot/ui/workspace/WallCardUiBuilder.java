package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.ui.spi.SlotUiElement;
import dev.imagio.slot.ui.spi.SlotUiEventKind;
import dev.imagio.slot.ui.spi.SlotUiLayout;
import dev.imagio.slot.ui.spi.SlotUiTextStyle;
import net.minecraft.network.chat.Component;

import java.util.List;

import static dev.imagio.slot.ui.workspace.WorkspaceUiPalette.GHOST_CARD_ALPHA;
import static dev.imagio.slot.ui.workspace.WorkspaceUiPalette.ROW_HOVER;
import static dev.imagio.slot.ui.workspace.WorkspaceUiPalette.SELECTED;
import static dev.imagio.slot.ui.workspace.WorkspaceUiPalette.TEXT;

public final class WallCardUiBuilder {
    public static final int CARD_CELL_PX = 24;
    public static final int WAYFINDING_STRIP_WIDTH_PX = 36;
    private static final int FOCUS_OVERLAY = 0x44365743;
    private static final int STOCK_PIP_HEIGHT_PX = 7;
    private static final int TOP_BADGE_HORIZONTAL_PAD_PX = 1;
    private static final float STOCK_PIP_FONT_SIZE = 5.0f;
    private static final float COUNT_BADGE_FONT_SIZE = 5.5f;
    private static final int BADGE_Z = 321;
    private static final int COUNT_BADGE_Z = 322;
    private static final int RING_Z = 318;

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
        boolean forceWayfinding = context.forceWayfindingStrip(item);
        SlotWorkspaceViewModel.ChestPresenceEntry wayfindingEntry = context.showWayfindingStrip(item)
                ? wayfindingEntryFor(item, activeSearchMatch, forceWayfinding)
                : null;
        MissingTargetDisplay missingTarget = wayfindingEntry == null && context.showWayfindingStrip(item)
                ? missingTargetFor(item, activeSearchMatch, forceWayfinding)
                : null;
        SlotWorkspaceViewModel.ContextualSuggestionLane suggestionLane = context.contextualSuggestionLane();
        boolean expanded = wayfindingEntry != null || missingTarget != null;
        int cardWidth = expanded ? CARD_CELL_PX + WAYFINDING_STRIP_WIDTH_PX : CARD_CELL_PX;

        SlotUiElement button = SlotUiElement.button("",
                        true,
                        cardChromeColor(selected, searchMatch, item.recent(), item.carried(), filtering))
                .noText()
                .zIndex(2)
                .tooltipStack(context.suppressVanillaTooltip(item) ? null : item.displayStack())
                .tooltipLines(context.tooltipLines(item))
                .attach(WorkspaceUiAttachments.WALL_CARD, Boolean.TRUE)
                .attach(WorkspaceUiAttachments.ATLAS_ITEM, item)
                .attach(WorkspaceUiAttachments.CONTEXTUAL_SUGGESTION_LANE, suggestionLane)
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
            button.overlayColor(focused ? FOCUS_OVERLAY : null);
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
                        .paddingAll(0)
                        .alignItems(SlotUiLayout.AlignItems.CENTER)
                        .flexDirection(SlotUiLayout.FlexDirection.ROW));
        if (wayfindingEntry != null) {
            body.attach(WorkspaceUiAttachments.WALL_CARD_WAYFINDING_ENTRY, wayfindingEntry);
        }
        if (missingTarget != null) {
            body.attach(WorkspaceUiAttachments.WALL_CARD_MISSING_TARGET, missingTarget);
        }
        buildFallbackBody(body, item, activeSearchMatch, forceWayfinding);
        button.addChild(body);
        return button;
    }

    private void buildFallbackBody(
            SlotUiElement body,
            SlotWorkspaceViewModel.AtlasItem item,
            boolean activeSearchMatch,
            boolean forceDistantContext
    ) {
        boolean routeOnly = context.hasProximateDepositRoute(item) || item.putAwayState().routed();
        boolean gatherPreviewEligible = context.gatherPreviewEligible(item);
        WallCardChromeSpec chrome = WallCardChromeSpec.from(
                item,
                activeSearchMatch,
                forceDistantContext,
                context.storageGhostRevealMode(),
                routeOnly,
                context.depositPreviewActive(),
                context.gatherPreviewActive(),
                gatherPreviewEligible);
        body.attach(WorkspaceUiAttachments.WALL_CARD_CHROME_SPEC, chrome);
        SlotUiElement iconCell = SlotUiElement.element()
                .allowHitTest(false)
                .attach(WorkspaceUiAttachments.WALL_CARD_CHROME_SPEC, chrome)
                .layout(layout -> layout
                        .positionType(SlotUiLayout.PositionType.ABSOLUTE)
                        .left(0)
                        .top(0)
                        .width(CARD_CELL_PX)
                        .height(CARD_CELL_PX)
                        .paddingAll(0));
        addCardShell(iconCell, item);
        iconCell.addChild(SlotUiElement.itemIcon(item.displayStack(), 18, item.carried())
                .renderVanillaCount(false)
                .zIndex(100)
                .layout(layout -> layout
                        .positionType(SlotUiLayout.PositionType.ABSOLUTE)
                        .left(3)
                        .top(3)
                        .width(18)
                        .height(18)));
        addCountBadge(iconCell, chrome);
        addProximatePip(iconCell, chrome);
        addSearchStoredPip(iconCell, chrome);
        addJunkIndicator(iconCell, item);
        addPrimaryRing(iconCell, item, chrome, routeOnly, gatherPreviewEligible);
        body.addChild(iconCell);
        addWayfindingStrip(body);
    }

    public static int cardChromeColor(
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

    private static int dimAlpha(int color, float alphaFactor) {
        int alpha = (color >>> 24) & 0xFF;
        int dimmed = Math.round(alpha * alphaFactor);
        return (dimmed << 24) | (color & 0x00FFFFFF);
    }

    private static SlotWorkspaceViewModel.ChestPresenceEntry wayfindingEntryFor(
            SlotWorkspaceViewModel.AtlasItem item,
            boolean activeSearchMatch,
            boolean force
    ) {
        if (item == null) {
            return null;
        }
        List<SlotWorkspaceViewModel.ChestPresenceEntry> candidates = item.elsewhere();
        if (candidates.isEmpty()) {
            return null;
        }
        boolean wantsPointer = activeSearchMatch
                || force
                || (hasDesiredGap(item) && item.presence().isEmpty());
        if (!wantsPointer) {
            return null;
        }
        SlotWorkspaceViewModel.ChestPresenceEntry best = null;
        int bestCount = -1;
        for (SlotWorkspaceViewModel.ChestPresenceEntry entry : candidates) {
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

    private static MissingTargetDisplay missingTargetFor(
            SlotWorkspaceViewModel.AtlasItem item,
            boolean activeSearchMatch,
            boolean force
    ) {
        if (item == null) {
            return null;
        }
        int target = WallCardChromeSpec.targetChoice(item).count();
        int missing = target
                - carriedCount(item)
                - presenceCount(item.presence())
                - presenceCount(item.elsewhere());
        if (missing <= 0) {
            return null;
        }
        boolean wantsMissingState = activeSearchMatch || force || hasDesiredGap(item);
        return wantsMissingState ? new MissingTargetDisplay(missing) : null;
    }

    private static boolean hasDesiredGap(SlotWorkspaceViewModel.AtlasItem item) {
        if (item == null) {
            return false;
        }
        if (item.kitNeeded()) {
            return true;
        }
        int carried = carriedCount(item);
        int target = WallCardChromeSpec.targetChoice(item).count();
        return target > 0 && carried < target;
    }

    private static void addCardShell(SlotUiElement body, SlotWorkspaceViewModel.AtlasItem item) {
        boolean carried = item == null || item.carried();
        int shellColor = carried ? WorkspaceUiPalette.CARD_SHELL : WorkspaceUiPalette.CARD_SHELL_GHOST;
        int innerColor = carried ? WorkspaceUiPalette.CARD_INNER : WorkspaceUiPalette.CARD_INNER_GHOST;
        body.addChild(SlotUiElement.panel(shellColor)
                .allowHitTest(false)
                .zIndex(10)
                .layout(layout -> layout
                        .positionType(SlotUiLayout.PositionType.ABSOLUTE)
                        .left(1)
                        .top(1)
                        .width(CARD_CELL_PX - 2)
                        .height(CARD_CELL_PX - 2)));
        body.addChild(SlotUiElement.panel(innerColor)
                .allowHitTest(false)
                .zIndex(11)
                .layout(layout -> layout
                        .positionType(SlotUiLayout.PositionType.ABSOLUTE)
                        .left(2)
                        .top(2)
                        .width(CARD_CELL_PX - 4)
                        .height(CARD_CELL_PX - 4)));
    }

    private static void addCountBadge(SlotUiElement body, WallCardChromeSpec chrome) {
        String text = chrome.countText();
        if (text.isBlank()) {
            return;
        }
        boolean targetBadge = chrome.targetSource() != WallCardChromeSpec.TargetSource.NONE;
        SlotUiElement badge = badgeLabel(text, chrome.countBadgeColor(), COUNT_BADGE_FONT_SIZE, !targetBadge)
                .zIndex(COUNT_BADGE_Z)
                .attach(WorkspaceUiAttachments.WALL_CARD_COUNT_BADGE, chrome.targetSource())
                .layout(layout -> layout
                        .positionType(SlotUiLayout.PositionType.ABSOLUTE)
                        .right(0)
                        .bottom(0)
                        .height(STOCK_PIP_HEIGHT_PX));
        if (targetBadge) {
            badge.layout(layout -> layout.width(CARD_CELL_PX));
        } else {
            badge.layout(layout -> layout.paddingHorizontal(TOP_BADGE_HORIZONTAL_PAD_PX));
            badge.textStyle(style -> style.adaptiveWidth(true));
        }
        body.addChild(badge);
    }

    private static void addProximatePip(
            SlotUiElement body,
            WallCardChromeSpec chrome
    ) {
        if (chrome.nearbyText().isBlank() && !chrome.nearbyRouteOnly()) {
            return;
        }
        if (chrome.nearbyRouteOnly()) {
            addRouteNotch(body);
            return;
        }
        String text = chrome.nearbyText();
        SlotUiElement pip = badgeLabel(text, WorkspaceUiPalette.NEARBY_BADGE, STOCK_PIP_FONT_SIZE, true)
                .zIndex(BADGE_Z)
                .attach(WorkspaceUiAttachments.WALL_CARD_NEARBY_BADGE, Boolean.TRUE)
                .layout(layout -> layout
                        .positionType(SlotUiLayout.PositionType.ABSOLUTE)
                        .right(0)
                        .top(0)
                        .height(STOCK_PIP_HEIGHT_PX)
                        .paddingHorizontal(TOP_BADGE_HORIZONTAL_PAD_PX));
        body.addChild(pip);
    }

    private static void addRouteNotch(SlotUiElement body) {
        SlotUiElement notch = SlotUiElement.element()
                .allowHitTest(false)
                .zIndex(BADGE_Z)
                .attach(WorkspaceUiAttachments.WALL_CARD_NEARBY_ROUTE_NOTCH, Boolean.TRUE)
                .layout(layout -> layout
                        .positionType(SlotUiLayout.PositionType.ABSOLUTE)
                        .right(0)
                        .top(0)
                        .width(6)
                        .height(STOCK_PIP_HEIGHT_PX));
        notch.addChild(SlotUiElement.panel(WorkspaceUiPalette.NEARBY_ROUTE_NOTCH)
                .allowHitTest(false)
                .layout(layout -> layout
                        .positionType(SlotUiLayout.PositionType.ABSOLUTE)
                        .right(0)
                        .top(0)
                        .width(6)
                        .height(2)));
        notch.addChild(SlotUiElement.panel(WorkspaceUiPalette.NEARBY_ROUTE_NOTCH)
                .allowHitTest(false)
                .layout(layout -> layout
                        .positionType(SlotUiLayout.PositionType.ABSOLUTE)
                        .right(0)
                        .top(0)
                        .width(2)
                        .height(6)));
        body.addChild(notch);
    }

    private static void addSearchStoredPip(SlotUiElement body, WallCardChromeSpec chrome) {
        String text = chrome.distantText();
        if (text.isBlank()) {
            return;
        }
        SlotUiElement pip = badgeLabel(text, WorkspaceUiPalette.DISTANT_BADGE, STOCK_PIP_FONT_SIZE, true)
                .zIndex(BADGE_Z)
                .attach(WorkspaceUiAttachments.WALL_CARD_DISTANT_BADGE, Boolean.TRUE)
                .layout(layout -> layout
                        .positionType(SlotUiLayout.PositionType.ABSOLUTE)
                        .left(0)
                        .top(0)
                        .height(STOCK_PIP_HEIGHT_PX)
                        .paddingHorizontal(TOP_BADGE_HORIZONTAL_PAD_PX));
        body.addChild(pip);
    }

    private static void addJunkIndicator(SlotUiElement body, SlotWorkspaceViewModel.AtlasItem item) {
        if (item == null || !item.junk()) {
            return;
        }
        SlotUiElement mark = SlotUiElement.element()
                .allowHitTest(false)
                .zIndex(COUNT_BADGE_Z + 1)
                .attach(WorkspaceUiAttachments.WALL_CARD_JUNK_MARK, Boolean.TRUE)
                .layout(layout -> layout
                        .positionType(SlotUiLayout.PositionType.ABSOLUTE)
                        .left(0)
                        .bottom(0)
                        .width(7)
                        .height(7));
        mark.addChild(SlotUiElement.panel(WorkspaceUiPalette.JUNK_MARK)
                .allowHitTest(false)
                .layout(layout -> layout
                        .positionType(SlotUiLayout.PositionType.ABSOLUTE)
                        .left(0)
                        .bottom(0)
                        .width(7)
                        .height(2)));
        mark.addChild(SlotUiElement.panel(WorkspaceUiPalette.JUNK_MARK)
                .allowHitTest(false)
                .layout(layout -> layout
                        .positionType(SlotUiLayout.PositionType.ABSOLUTE)
                        .left(0)
                        .bottom(0)
                        .width(2)
                        .height(7)));
        body.addChild(mark);
    }

    private void addPrimaryRing(
            SlotUiElement body,
            SlotWorkspaceViewModel.AtlasItem item,
            WallCardChromeSpec chrome,
            boolean hasProximateDepositRoute,
            boolean gatherPreviewEligible
    ) {
        if (chrome.ring() == WallCardChromeSpec.Ring.NONE
                && !hasProximateDepositRoute
                && !gatherPreviewEligible) {
            return;
        }
        body.addChild(ringSegment(item, hasProximateDepositRoute, gatherPreviewEligible, chrome.ringColor(),
                SegmentEdge.TOP));
        body.addChild(ringSegment(item, hasProximateDepositRoute, gatherPreviewEligible, chrome.ringColor(),
                SegmentEdge.BOTTOM));
        body.addChild(ringSegment(item, hasProximateDepositRoute, gatherPreviewEligible, chrome.ringColor(),
                SegmentEdge.LEFT));
        body.addChild(ringSegment(item, hasProximateDepositRoute, gatherPreviewEligible, chrome.ringColor(),
                SegmentEdge.RIGHT));
    }

    private SlotUiElement ringSegment(
            SlotWorkspaceViewModel.AtlasItem item,
            boolean hasProximateDepositRoute,
            boolean gatherPreviewEligible,
            int initialColor,
            SegmentEdge edge
    ) {
        SlotUiElement segment = SlotUiElement.panel(initialColor)
                .allowHitTest(false)
                .zIndex(RING_Z)
                .attach(WorkspaceUiAttachments.WALL_CARD_RING, Boolean.TRUE)
                .layout(layout -> applyRingLayout(layout, edge));
        segment.on(SlotUiEventKind.TICK, event -> segment.backgroundColor(currentRingColor(
                item,
                hasProximateDepositRoute,
                gatherPreviewEligible)));
        return segment;
    }

    private static void applyRingLayout(SlotUiLayout layout, SegmentEdge edge) {
        layout.positionType(SlotUiLayout.PositionType.ABSOLUTE);
        switch (edge) {
            case TOP -> layout.left(0).top(0).width(CARD_CELL_PX).height(1);
            case BOTTOM -> layout.left(0).bottom(0).width(CARD_CELL_PX).height(1);
            case LEFT -> layout.left(0).top(0).width(1).height(CARD_CELL_PX);
            case RIGHT -> layout.right(0).top(0).width(1).height(CARD_CELL_PX);
        }
    }

    private int currentRingColor(
            SlotWorkspaceViewModel.AtlasItem item,
            boolean hasProximateDepositRoute,
            boolean gatherPreviewEligible
    ) {
        return WallCardChromeSpec.from(
                item,
                false,
                false,
                context.storageGhostRevealMode(),
                hasProximateDepositRoute,
                context.depositPreviewActive(),
                context.gatherPreviewActive(),
                gatherPreviewEligible).ringColor();
    }

    private void addWayfindingStrip(SlotUiElement body) {
        SlotWorkspaceViewModel.ChestPresenceEntry entry = body.attachment(
                WorkspaceUiAttachments.WALL_CARD_WAYFINDING_ENTRY,
                SlotWorkspaceViewModel.ChestPresenceEntry.class);
        if (entry == null) {
            addMissingTargetStrip(body);
            return;
        }
        SlotUiElement strip = SlotUiElement.panel(0xAA1F3448)
                .allowHitTest(false)
                .zIndex(310)
                .layout(layout -> layout
                        .positionType(SlotUiLayout.PositionType.ABSOLUTE)
                        .left(CARD_CELL_PX)
                        .top(2)
                        .width(WAYFINDING_STRIP_WIDTH_PX - 2)
                        .height(CARD_CELL_PX - 4)
                        .paddingHorizontal(2)
                        .alignItems(SlotUiLayout.AlignItems.CENTER)
                        .flexDirection(SlotUiLayout.FlexDirection.ROW));
        WayfindingDisplay.CardText initial = wayfindingText(entry);
        SlotUiElement arrow = SlotUiElement.label("", 0xFFBFD8FF)
                .layout(layout -> layout.width(9).heightPercent(100))
                .textStyle(style -> style
                        .fontSize(6)
                        .color(0xFFBFD8FF)
                        .horizontal(SlotUiTextStyle.Horizontal.CENTER)
                        .vertical(SlotUiTextStyle.Vertical.CENTER));
        SlotUiElement distance = SlotUiElement.label("", 0xFFA0AAB3)
                .layout(layout -> layout.flex(1).heightPercent(100))
                .textStyle(style -> style
                        .fontSize(6)
                        .color(0xFFA0AAB3)
                        .horizontal(SlotUiTextStyle.Horizontal.RIGHT)
                        .vertical(SlotUiTextStyle.Vertical.CENTER));
        applyWayfindingText(arrow, distance, initial);
        arrow.on(SlotUiEventKind.TICK, event ->
                applyWayfindingText(arrow, distance, wayfindingText(entry)));
        distance.on(SlotUiEventKind.TICK, event ->
                applyWayfindingText(arrow, distance, wayfindingText(entry)));
        strip.addChild(arrow);
        strip.addChild(distance);
        body.addChild(strip);
    }

    private static void applyWayfindingText(
            SlotUiElement arrow,
            SlotUiElement distance,
            WayfindingDisplay.CardText text
    ) {
        WayfindingDisplay.CardText next = text == null ? WayfindingDisplay.CardText.unavailable() : text;
        if (next.distance().isBlank()) {
            arrow.text("");
            distance.text(next.arrow());
            return;
        }
        arrow.text(next.arrow());
        distance.text(next.distance());
    }

    private static void addMissingTargetStrip(SlotUiElement body) {
        MissingTargetDisplay missing = body.attachment(
                WorkspaceUiAttachments.WALL_CARD_MISSING_TARGET,
                MissingTargetDisplay.class);
        if (missing == null) {
            return;
        }
        SlotUiElement strip = SlotUiElement.panel(0xB08C5A22)
                .allowHitTest(false)
                .zIndex(310)
                .layout(layout -> layout
                        .positionType(SlotUiLayout.PositionType.ABSOLUTE)
                        .left(CARD_CELL_PX)
                        .top(2)
                        .width(WAYFINDING_STRIP_WIDTH_PX - 2)
                        .height(CARD_CELL_PX - 4)
                        .paddingHorizontal(2)
                        .alignItems(SlotUiLayout.AlignItems.CENTER)
                        .flexDirection(SlotUiLayout.FlexDirection.ROW));
        strip.addChild(SlotUiElement.label("need", 0xFFFFD166)
                .layout(layout -> layout.flex(1).heightPercent(100))
                .textStyle(style -> style
                        .fontSize(5.5f)
                        .color(0xFFFFD166)
                        .horizontal(SlotUiTextStyle.Horizontal.LEFT)
                        .vertical(SlotUiTextStyle.Vertical.CENTER)));
        strip.addChild(SlotUiElement.label(WorkspaceCountFormat.compact(missing.count()), 0xFFE6EDF3)
                .layout(layout -> layout.width(9).heightPercent(100))
                .textStyle(style -> style
                        .fontSize(6)
                        .color(0xFFE6EDF3)
                        .horizontal(SlotUiTextStyle.Horizontal.RIGHT)
                        .vertical(SlotUiTextStyle.Vertical.CENTER)));
        body.addChild(strip);
    }

    private WayfindingDisplay.CardText wayfindingText(SlotWorkspaceViewModel.ChestPresenceEntry entry) {
        WayfindingDisplay.CardText text = context.wayfindingText(entry);
        if (text != null) {
            return text;
        }
        return new WayfindingDisplay.CardText(">", WorkspaceCountFormat.compact(entry.count()));
    }

    public record MissingTargetDisplay(int count) {
        public MissingTargetDisplay {
            count = Math.max(0, count);
        }
    }

    private enum SegmentEdge {
        TOP,
        BOTTOM,
        LEFT,
        RIGHT
    }

    private static int carriedCount(SlotWorkspaceViewModel.AtlasItem item) {
        return item != null && item.carried() ? Math.max(0, item.totalCount()) : 0;
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

    private static SlotUiElement badgeLabel(String text, int backgroundColor, float fontSize, boolean adaptiveWidth) {
        return SlotUiElement.label(text, TEXT)
                .allowHitTest(false)
                .backgroundColor(backgroundColor)
                .layout(layout -> layout
                        .height(STOCK_PIP_HEIGHT_PX))
                .textStyle(style -> style
                        .color(TEXT)
                        .fontSize(fontSize)
                        .adaptiveWidth(adaptiveWidth)
                        .horizontal(SlotUiTextStyle.Horizontal.CENTER)
                        .vertical(SlotUiTextStyle.Vertical.CENTER));
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

        default List<Component> tooltipLines(SlotWorkspaceViewModel.AtlasItem item) {
            return WorkspaceItemTooltipBuilder.slotLines(item, hasProximateDepositRoute(item));
        }

        default boolean choiceInvolved(SlotWorkspaceViewModel.AtlasItem item) {
            return false;
        }

        default boolean choiceCard(SlotWorkspaceViewModel.AtlasItem item) {
            return false;
        }

        default boolean suppressVanillaTooltip(SlotWorkspaceViewModel.AtlasItem item) {
            return false;
        }

        default boolean showWayfindingStrip(SlotWorkspaceViewModel.AtlasItem item) {
            return true;
        }

        default boolean forceWayfindingStrip(SlotWorkspaceViewModel.AtlasItem item) {
            return false;
        }

        default boolean hasProximateDepositRoute(SlotWorkspaceViewModel.AtlasItem item) {
            return false;
        }

        default boolean depositPreviewActive() {
            return false;
        }

        default boolean gatherPreviewActive() {
            return false;
        }

        default boolean gatherPreviewEligible(SlotWorkspaceViewModel.AtlasItem item) {
            return false;
        }

        default SlotWorkspaceViewModel.ContextualSuggestionLane contextualSuggestionLane() {
            return null;
        }

        default StorageGhostRevealMode storageGhostRevealMode() {
            return StorageGhostRevealMode.COLLAPSED;
        }
    }
}
