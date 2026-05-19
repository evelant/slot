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
    public static final int MAX_ROWS = 2;
    public static final int MAX_CARDS_PER_ROW = 8;
    public static final int MAX_CARDS = MAX_ROWS * MAX_CARDS_PER_ROW;
    public static final int MAX_ICONS = MAX_CARDS;
    public static final int STRIP_HEIGHT_PX = CARD_SIZE_PX * MAX_ROWS + GAP_PX + PADDING_PX * 2;

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
                        .widthPercent(100)
                        .height(STRIP_HEIGHT_PX)
                        .paddingHorizontal(PADDING_PX)
                        .paddingVertical(PADDING_PX)
                        .gapAll(GAP_PX)
                        .alignItems(SlotUiLayout.AlignItems.FLEX_START)
                        .flexDirection(SlotUiLayout.FlexDirection.ROW));
        strip.on(SlotUiEventKind.MOUSE_DOWN, event -> event.stopPropagation());

        strip.addChild(SlotUiElement.label("Recent", MUTED)
                .layout(layout -> layout.height(CARD_SIZE_PX * MAX_ROWS + GAP_PX).paddingRight(2))
                .textStyle(style -> style
                        .color(MUTED)
                        .shadow(false)
                        .fontSize(6)
                        .adaptiveWidth(true)
                        .horizontal(SlotUiTextStyle.Horizontal.LEFT)
                        .vertical(SlotUiTextStyle.Vertical.CENTER)));

        SlotUiElement grid = SlotUiElement.element()
                .layout(layout -> layout
                        .flex(1)
                        .height(CARD_SIZE_PX * MAX_ROWS + GAP_PX)
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
