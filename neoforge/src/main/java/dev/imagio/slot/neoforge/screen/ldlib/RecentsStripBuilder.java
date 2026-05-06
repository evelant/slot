package dev.imagio.slot.neoforge.screen.ldlib;

import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.*;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceUi.*;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;

/**
 * Pinned MRU strip of small item icons that lives above the wall
 * scroller and outside it. Click an icon → scroll the wall to that
 * identity's home section AND select the homed card.
 *
 * <p>Folds the player's "where did the thing I just grabbed end up?"
 * question into a single decorative pointer surface — these are not
 * full cards, no badges, no drag.
 */
final class RecentsStripBuilder {
    static final int ICON_SIZE_PX = 16;
    static final int GAP_PX = 2;
    static final int PADDING_PX = 3;
    static final int MAX_ICONS = 12;

    private final SlotWorkspaceUiController host;

    RecentsStripBuilder(SlotWorkspaceUiController host) {
        this.host = host;
    }

    UIElement overlay() {
        UIElement strip = new UIElement().layout(layout -> layout
                .widthPercent(100)
                .height(ICON_SIZE_PX + PADDING_PX * 2)
                .paddingHorizontal(PADDING_PX)
                .paddingVertical(PADDING_PX)
                .gapAll(GAP_PX)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.ROW));
        strip.style(style -> style.backgroundTexture(rect(0xB810171D)).zIndex(1));
        strip.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());

        // The "Recents" affordance label sits on the left edge so the
        // strip reads the same way whether it's full of icons or empty.
        // Plan calls it out as an optional but useful pointer for the
        // player to recognise the surface.
        Label header = label("Recent", MUTED);
        header.layout(layout -> layout.height(ICON_SIZE_PX).paddingRight(2));
        header.textStyle(style -> style
                .textColor(MUTED)
                .textShadow(false)
                .fontSize(6)
                .textAlignHorizontal(Horizontal.LEFT)
                .textAlignVertical(Vertical.CENTER)
                .adaptiveWidth(true));
        header.setAllowHitTest(false);
        strip.addChild(header);

        int rendered = 0;
        for (SlotWorkspaceViewModel.IdentityRef identity : host.viewModel.recentIdentities()) {
            if (rendered >= MAX_ICONS) {
                break;
            }
            SlotWorkspaceViewModel.AtlasItem item = host.viewModel.atlasItem(identity);
            if (item == null) {
                continue;
            }
            strip.addChild(iconButton(item));
            rendered++;
        }
        if (rendered == 0) {
            // Empty state: keep the strip mounted so the player learns
            // where Recents lives even before they pick anything up.
            Label hint = label("nothing yet", MUTED);
            hint.layout(layout -> layout.flex(1).height(ICON_SIZE_PX));
            hint.textStyle(style -> style
                    .textColor(MUTED)
                    .textShadow(false)
                    .fontSize(6)
                    .textAlignHorizontal(Horizontal.LEFT)
                    .textAlignVertical(Vertical.CENTER));
            hint.setAllowHitTest(false);
            strip.addChild(hint);
        }
        return strip;
    }

    private UIElement iconButton(SlotWorkspaceViewModel.AtlasItem item) {
        Button btn = button("", true, ROW_DIM);
        btn.layout(layout -> layout
                .width(ICON_SIZE_PX)
                .height(ICON_SIZE_PX)
                .paddingAll(0));
        btn.noText();
        btn.style(style -> style.zIndex(2));
        btn.setOnClick(event -> {
            if (event.button != 0) {
                return;
            }
            event.stopPropagation();
            host.selectedAtlasIdentity.set(item.identity());
            host.selectedHotbarIndex.set(-1);
            scrollWallToIsland(item.islandId());
        });
        host.drag.installAtlasHoverTooltip(btn, item);

        UIElement icon = itemIcon(item.displayStack(), ICON_SIZE_PX, item.carried(), false);
        icon.layout(layout -> layout.widthPercent(100).heightPercent(100));
        icon.setAllowHitTest(false);
        btn.addChild(icon);
        return btn;
    }

    private void scrollWallToIsland(String islandId) {
        if (host.wallScroller == null || islandId == null || islandId.isBlank()) {
            return;
        }
        UIElement section = null;
        for (UIElement child : host.wallScroller.viewContainer.getChildren()) {
            if (islandId.equals(child.getId())) {
                section = child;
                break;
            }
        }
        if (section == null) {
            return;
        }
        float containerScreenY = host.wallScroller.viewContainer.getPositionY();
        float sectionLogicalY = section.getPositionY() - containerScreenY;
        float containerHeight = host.wallScroller.getContainerHeight();
        float viewportHeight = host.wallScroller.getSizeHeight();
        float maxScroll = Math.max(1f, containerHeight - viewportHeight);
        float normalized = Math.max(0f, Math.min(1f, sectionLogicalY / maxScroll));
        host.wallScroller.verticalScroller.setValue(normalized);
    }
}
