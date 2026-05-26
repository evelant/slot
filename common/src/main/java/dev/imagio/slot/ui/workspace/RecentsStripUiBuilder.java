package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.ui.spi.SlotUiElement;
import dev.imagio.slot.ui.spi.SlotUiEventKind;
import dev.imagio.slot.ui.spi.SlotUiLayout;
import dev.imagio.slot.ui.spi.SlotUiTextStyle;

import java.util.List;

import static dev.imagio.slot.ui.workspace.WorkspaceUiPalette.MUTED;

public final class RecentsStripUiBuilder {
    public static final int CARD_SIZE_PX = WallCardUiBuilder.CARD_CELL_PX;
    public static final int GAP_PX = 2;
    public static final int PADDING_PX = 3;
    public static final int MAX_ROWS = 3;
    public static final int MAX_CARDS_PER_ROW = 8;
    public static final int MAX_CARDS = MAX_ROWS * MAX_CARDS_PER_ROW;
    public static final int MAX_ICONS = MAX_CARDS;
    public static final int LABEL_WIDTH_PX = 28;
    public static final int LABEL_HEIGHT_PX = 7;
    public static final int GRID_WIDTH_PX =
            CARD_SIZE_PX * MAX_CARDS_PER_ROW + GAP_PX * (MAX_CARDS_PER_ROW - 1);
    public static final int GRID_HEIGHT_PX =
            CARD_SIZE_PX * MAX_ROWS + GAP_PX * (MAX_ROWS - 1);
    public static final int STRIP_WIDTH_PX = GRID_WIDTH_PX + PADDING_PX * 2;
    public static final int STRIP_HEIGHT_PX = GRID_HEIGHT_PX + PADDING_PX * 2;
    public static final int DEFAULT_HORIZONTAL_OFFSET_PX = 0;
    public static final int DEFAULT_TOP_OFFSET_PX = 8;

    private static final int STRIP_BACKGROUND = 0xB810171D;

    private final Context context;

    public RecentsStripUiBuilder(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context is required");
        }
        this.context = context;
    }

    public SlotUiElement overlay(List<SlotWorkspaceViewModel.IdentityRef> recentIdentities) {
        SlotUiElement strip = SlotUiElement.element()
                .backgroundColor(STRIP_BACKGROUND)
                .zIndex(1)
                .attach(WorkspaceUiAttachments.RECENTS_STRIP, Boolean.TRUE)
                .layout(layout -> layout
                        .width(STRIP_WIDTH_PX)
                        .height(STRIP_HEIGHT_PX)
                        .paddingHorizontal(PADDING_PX)
                        .paddingVertical(PADDING_PX)
                        .gapAll(GAP_PX)
                        .alignItems(SlotUiLayout.AlignItems.FLEX_START)
                        .flexDirection(SlotUiLayout.FlexDirection.ROW));
        strip.on(SlotUiEventKind.MOUSE_DOWN, event -> event.stopPropagation());

        strip.addChild(SlotUiElement.label("Recent", MUTED)
                .zIndex(4)
                .layout(layout -> layout
                        .positionType(SlotUiLayout.PositionType.ABSOLUTE)
                        .left(PADDING_PX + 1)
                        .top(0)
                        .width(LABEL_WIDTH_PX)
                        .height(LABEL_HEIGHT_PX))
                .textStyle(style -> style
                        .color(MUTED)
                        .shadow(false)
                        .fontSize(5)
                        .horizontal(SlotUiTextStyle.Horizontal.LEFT)
                        .vertical(SlotUiTextStyle.Vertical.CENTER)));

        SlotUiElement grid = SlotUiElement.element()
                .layout(layout -> layout
                        .width(GRID_WIDTH_PX)
                        .height(GRID_HEIGHT_PX)
                        .gapAll(GAP_PX)
                        .flexWrap(SlotUiLayout.FlexWrap.WRAP)
                        .alignItems(SlotUiLayout.AlignItems.FLEX_START)
                        .flexDirection(SlotUiLayout.FlexDirection.ROW));
        strip.addChild(grid);

        List<SlotWorkspaceViewModel.IdentityRef> identities = safeList(recentIdentities);
        int rendered = 0;
        for (SlotWorkspaceViewModel.IdentityRef identity : identities) {
            if (rendered >= MAX_CARDS) {
                break;
            }
            SlotWorkspaceViewModel.AtlasItem item = context.atlasItem(identity);
            if (item == null) {
                continue;
            }
            grid.addChild(recentCard(item));
            rendered++;
        }
        if (rendered == 0) {
            grid.addChild(SlotUiElement.label(identities.isEmpty() ? "nothing yet" : "not visible", MUTED)
                    .layout(layout -> layout.flex(1).height(CARD_SIZE_PX))
                    .textStyle(style -> style
                            .color(MUTED)
                            .shadow(false)
                            .fontSize(6)
                            .horizontal(SlotUiTextStyle.Horizontal.LEFT)
                            .vertical(SlotUiTextStyle.Vertical.CENTER)));
        }
        return strip;
    }

    public static int floatingLeft(int screenWidth, int horizontalOffset) {
        int maxLeft = Math.max(0, screenWidth - STRIP_WIDTH_PX);
        int centered = Math.round((screenWidth - STRIP_WIDTH_PX) / 2.0f) + horizontalOffset;
        return Math.max(0, Math.min(maxLeft, centered));
    }

    public static int floatingTop(int topOffset) {
        return Math.max(0, topOffset);
    }

    private SlotUiElement recentCard(SlotWorkspaceViewModel.AtlasItem item) {
        SlotUiElement card = new WallCardUiBuilder(new RecentsCardContext()).card(item)
                .attach(WorkspaceUiAttachments.RECENTS_CARD, Boolean.TRUE)
                .attach(WorkspaceUiAttachments.RECENTS_ICON, Boolean.TRUE)
                .layout(layout -> layout.width(CARD_SIZE_PX).height(CARD_SIZE_PX));
        card.on(SlotUiEventKind.MOUSE_ENTER, event -> context.hoverRecent(item), true);
        card.on(SlotUiEventKind.MOUSE_LEAVE, event -> context.clearHoveredRecent(item), true);
        return card;
    }

    private static List<SlotWorkspaceViewModel.IdentityRef> safeList(
            List<SlotWorkspaceViewModel.IdentityRef> recentIdentities
    ) {
        return recentIdentities == null ? List.of() : List.copyOf(recentIdentities);
    }

    private final class RecentsCardContext implements WallCardUiBuilder.Context {
        @Override
        public SlotWorkspaceViewModel.IdentityRef activeIdentity() {
            return context.activeIdentity();
        }

        @Override
        public String normalizedSearchQuery() {
            return "";
        }

        @Override
        public boolean matchesItem(SlotWorkspaceViewModel.AtlasItem item) {
            return true;
        }

        @Override
        public boolean isMapFocusItem(SlotWorkspaceViewModel.AtlasItem item) {
            return context.isMapFocusItem(item);
        }

        @Override
        public void hoverAtlasIdentity(SlotWorkspaceViewModel.IdentityRef identity) {
            context.hoverRecent(context.atlasItem(identity));
        }

        @Override
        public void clearHoveredAtlasIdentity(SlotWorkspaceViewModel.IdentityRef identity) {
            context.clearHoveredRecent(context.atlasItem(identity));
        }

        @Override
        public boolean choiceInvolved(SlotWorkspaceViewModel.AtlasItem item) {
            return context.choiceInvolved(item);
        }

        @Override
        public boolean choiceCard(SlotWorkspaceViewModel.AtlasItem item) {
            return context.choiceCard(item);
        }

        @Override
        public boolean showWayfindingStrip(SlotWorkspaceViewModel.AtlasItem item) {
            return false;
        }
    }

    public interface Context {
        SlotWorkspaceViewModel.AtlasItem atlasItem(SlotWorkspaceViewModel.IdentityRef identity);

        default SlotWorkspaceViewModel.IdentityRef activeIdentity() {
            return null;
        }

        default boolean isMapFocusItem(SlotWorkspaceViewModel.AtlasItem item) {
            return false;
        }

        void hoverRecent(SlotWorkspaceViewModel.AtlasItem item);

        void clearHoveredRecent(SlotWorkspaceViewModel.AtlasItem item);

        default boolean choiceInvolved(SlotWorkspaceViewModel.AtlasItem item) {
            return false;
        }

        default boolean choiceCard(SlotWorkspaceViewModel.AtlasItem item) {
            return false;
        }
    }
}
