package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.inventory.workspace.WorkspaceItemTargets;
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
    public static final int CARD_CELL_PX = 22;
    public static final int WAYFINDING_STRIP_WIDTH_PX = 36;
    private static final int FOCUS_OVERLAY = 0x44365743;
    private static final int STOCK_PIP_HEIGHT_PX = 6;
    private static final float STOCK_PIP_FONT_SIZE = 5.5f;

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
        SlotWorkspaceViewModel.ChestPresenceEntry wayfindingEntry = context.showWayfindingStrip(item)
                ? wayfindingEntryFor(item, activeSearchMatch, context.forceWayfindingStrip(item))
                : null;
        MissingTargetDisplay missingTarget = wayfindingEntry == null && context.showWayfindingStrip(item)
                ? missingTargetFor(item, activeSearchMatch, context.forceWayfindingStrip(item))
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
        buildFallbackBody(body, item, activeSearchMatch);
        button.addChild(body);
        return button;
    }

    private void buildFallbackBody(SlotUiElement body, SlotWorkspaceViewModel.AtlasItem item, boolean activeSearchMatch) {
        body.addChild(SlotUiElement.itemIcon(item.displayStack(), 16, item.carried())
                .renderVanillaCount(false)
                .zIndex(100)
                .layout(layout -> layout
                        .positionType(SlotUiLayout.PositionType.ABSOLUTE)
                        .left(3)
                        .top(3)
                        .width(16)
                        .height(16)));
        addCountBadge(body, item);
        addProximatePip(body, item, context.hasProximateDepositRoute(item) || item.putAwayState().routed());
        addSearchStoredPip(body, item, activeSearchMatch, context.storageGhostRevealMode());
        addChoiceIndicator(body, item);
        addDesiredMarker(body, item);
        addPutAwayBorder(body, item);
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
        if (item == null || hasKnownStorage(item)) {
            return null;
        }
        WorkspaceItemTargets targets = WorkspaceItemTargets.from(item);
        int missing = targets.displayTargetCount() - carriedCount(item);
        if (missing <= 0) {
            return null;
        }
        boolean wantsMissingState = activeSearchMatch || force || hasDesiredGap(item);
        return wantsMissingState ? new MissingTargetDisplay(missing) : null;
    }

    private static boolean hasKnownStorage(SlotWorkspaceViewModel.AtlasItem item) {
        return presenceCount(item.presence()) > 0 || presenceCount(item.elsewhere()) > 0;
    }

    private static boolean hasDesiredGap(SlotWorkspaceViewModel.AtlasItem item) {
        if (item == null) {
            return false;
        }
        if (item.kitNeeded()) {
            return true;
        }
        int carried = carriedCount(item);
        return WorkspaceItemTargets.from(item).hasAnyGap(carried);
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
                .zIndex(320)
                .layout(layout -> layout
                        .positionType(SlotUiLayout.PositionType.ABSOLUTE)
                        .right(0)
                        .bottom(0)
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

    private static void addProximatePip(
            SlotUiElement body,
            SlotWorkspaceViewModel.AtlasItem item,
            boolean hasProximateDepositRoute
    ) {
        int count = presenceCount(item.presence());
        if (count <= 0 && !hasProximateDepositRoute) {
            return;
        }
        String text = count > 0 ? WorkspaceCountFormat.compact(count) : "+";
        SlotUiElement pip = SlotUiElement.panel(0xE07AC7A7)
                .allowHitTest(false)
                .zIndex(321)
                .layout(layout -> layout
                        .positionType(SlotUiLayout.PositionType.ABSOLUTE)
                        .right(0)
                        .top(0)
                        .width(stockPipWidth(text))
                        .height(STOCK_PIP_HEIGHT_PX));
        pip.addChild(SlotUiElement.label(text, TEXT)
                .allowHitTest(false)
                .layout(layout -> layout.widthPercent(100).heightPercent(100))
                .textStyle(style -> style
                        .color(TEXT)
                        .fontSize(STOCK_PIP_FONT_SIZE)
                        .horizontal(SlotUiTextStyle.Horizontal.CENTER)
                        .vertical(SlotUiTextStyle.Vertical.CENTER)));
        body.addChild(pip);
    }

    private static void addSearchStoredPip(
            SlotUiElement body,
            SlotWorkspaceViewModel.AtlasItem item,
            boolean activeSearchMatch,
            StorageGhostRevealMode revealMode
    ) {
        boolean trackedXray = revealMode == StorageGhostRevealMode.TRACKED;
        if (!activeSearchMatch && (!trackedXray || item.elsewhere().isEmpty())) {
            return;
        }
        int count = activeSearchMatch
                ? presenceCount(item.presence()) + presenceCount(item.elsewhere())
                : presenceCount(item.elsewhere());
        if (count <= 0) {
            return;
        }
        String text = "+" + WorkspaceCountFormat.compact(count);
        SlotUiElement pip = SlotUiElement.panel(0xE0809ACB)
                .allowHitTest(false)
                .zIndex(321)
                .layout(layout -> layout
                        .positionType(SlotUiLayout.PositionType.ABSOLUTE)
                        .left(0)
                        .top(0)
                        .width(stockPipWidth(text))
                        .height(STOCK_PIP_HEIGHT_PX));
        pip.addChild(SlotUiElement.label(text, TEXT)
                .allowHitTest(false)
                .layout(layout -> layout.widthPercent(100).heightPercent(100))
                .textStyle(style -> style
                        .color(TEXT)
                        .fontSize(STOCK_PIP_FONT_SIZE)
                        .horizontal(SlotUiTextStyle.Horizontal.CENTER)
                        .vertical(SlotUiTextStyle.Vertical.CENTER)));
        body.addChild(pip);
    }

    private static void addDesiredMarker(SlotUiElement body, SlotWorkspaceViewModel.AtlasItem item) {
        if (!hasDesiredGap(item)) {
            return;
        }
        body.addChild(SlotUiElement.panel(0xE0FFD166)
                .allowHitTest(false)
                .zIndex(319)
                .layout(layout -> layout
                        .positionType(SlotUiLayout.PositionType.ABSOLUTE)
                        .left(0)
                        .bottom(0)
                        .width(5)
                        .height(5)));
    }

    private static void addPutAwayBorder(SlotUiElement body, SlotWorkspaceViewModel.AtlasItem item) {
        if (item == null || !item.putAwayState().active()) {
            return;
        }
        int color = item.putAwayState().routed() ? 0xE04ADE80 : 0xE0F59E0B;
        body.addChild(SlotUiElement.panel(color)
                .allowHitTest(false)
                .zIndex(318)
                .layout(layout -> layout
                        .positionType(SlotUiLayout.PositionType.ABSOLUTE)
                        .left(0)
                        .top(0)
                        .width(CARD_CELL_PX)
                        .height(1)));
        body.addChild(SlotUiElement.panel(color)
                .allowHitTest(false)
                .zIndex(318)
                .layout(layout -> layout
                        .positionType(SlotUiLayout.PositionType.ABSOLUTE)
                        .left(0)
                        .bottom(0)
                        .width(CARD_CELL_PX)
                        .height(1)));
        body.addChild(SlotUiElement.panel(color)
                .allowHitTest(false)
                .zIndex(318)
                .layout(layout -> layout
                        .positionType(SlotUiLayout.PositionType.ABSOLUTE)
                        .left(0)
                        .top(0)
                        .width(1)
                        .height(CARD_CELL_PX)));
        body.addChild(SlotUiElement.panel(color)
                .allowHitTest(false)
                .zIndex(318)
                .layout(layout -> layout
                        .positionType(SlotUiLayout.PositionType.ABSOLUTE)
                        .right(0)
                        .top(0)
                        .width(1)
                        .height(CARD_CELL_PX)));
    }

    private void addChoiceIndicator(SlotUiElement body, SlotWorkspaceViewModel.AtlasItem item) {
        if (!context.choiceInvolved(item)) {
            return;
        }
        int fillColor = context.choiceCard(item) ? 0xFFFFD166 : 0xFFE7D9FF;
        int markColor = 0xFF071017;
        SlotUiElement frame = SlotUiElement.panel(0xF0071017)
                .allowHitTest(false)
                .zIndex(430)
                .layout(layout -> layout
                        .positionType(SlotUiLayout.PositionType.ABSOLUTE)
                        .left(0)
                        .top(0)
                        .width(10)
                        .height(10)
                        .paddingAll(1));
        SlotUiElement pip = SlotUiElement.panel(fillColor)
                .allowHitTest(false)
                .layout(layout -> layout
                        .widthPercent(100)
                        .heightPercent(100));
        pip.addChild(SlotUiElement.label("?", markColor)
                .allowHitTest(false)
                .layout(layout -> layout.widthPercent(100).heightPercent(100))
                .textStyle(style -> style
                        .color(markColor)
                        .fontSize(8)
                        .horizontal(SlotUiTextStyle.Horizontal.CENTER)
                        .vertical(SlotUiTextStyle.Vertical.CENTER)));
        frame.addChild(pip);
        body.addChild(frame);
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
                        .top(3)
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
                        .top(3)
                        .width(28)
                        .height(16)
                        .paddingHorizontal(1)
                        .alignItems(SlotUiLayout.AlignItems.CENTER)
                        .flexDirection(SlotUiLayout.FlexDirection.ROW));
        strip.addChild(SlotUiElement.label("craft", 0xFFFFD166)
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

    private static String countBadgeText(SlotWorkspaceViewModel.AtlasItem item) {
        int carried = carriedCount(item);
        int target = WorkspaceItemTargets.from(item).displayTargetCount();
        if (target > 0) {
            return (carried <= 0 ? "0" : WorkspaceCountFormat.compact(carried))
                    + "/" + WorkspaceCountFormat.compact(target);
        }
        return carried <= 0 ? "" : WorkspaceCountFormat.compact(carried);
    }

    public record MissingTargetDisplay(int count) {
        public MissingTargetDisplay {
            count = Math.max(0, count);
        }
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

    private static int stockPipWidth(String text) {
        String value = text == null ? "" : text;
        return Math.max(STOCK_PIP_HEIGHT_PX, Math.round(value.length() * 3.25f + 1));
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

        default SlotWorkspaceViewModel.ContextualSuggestionLane contextualSuggestionLane() {
            return null;
        }

        default StorageGhostRevealMode storageGhostRevealMode() {
            return StorageGhostRevealMode.COLLAPSED;
        }
    }
}
