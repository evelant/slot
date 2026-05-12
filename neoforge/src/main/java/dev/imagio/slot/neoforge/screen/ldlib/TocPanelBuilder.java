package dev.imagio.slot.neoforge.screen.ldlib;

import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.*;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceUi.*;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.network.chat.Component;

/**
 * Thin colored sliver glued to the left edge of the wall scroller. One
 * dot per non-empty non-Triage section, vertical position proportional
 * to where that section sits in the scroll content. Click → scroll the
 * wall to that section; off-screen sections with active-search hits or
 * kit-needed cards pulse via an accent overlay.
 *
 * <p>Section reorder lives on the wall section headers themselves
 * (see {@link DragDropWiring#installSectionHeaderDragSource}); the
 * sliver is navigation-only.
 */
final class TocPanelBuilder {
    static final int SLIVER_WIDTH_PX = 6;
    private static final int DOT_SIZE_PX = 4;
    private static final int DOT_TOP_INSET_PX = 4;

    private final SlotWorkspaceUiController host;

    TocPanelBuilder(SlotWorkspaceUiController host) {
        this.host = host;
    }

    UIElement overlay() {
        java.util.List<SlotWorkspaceViewModel.AtlasIsland> entries = visibleEntries();
        if (entries.isEmpty()) {
            return null;
        }
        UIElement sliver = new UIElement().layout(layout -> layout
                .width(SLIVER_WIDTH_PX)
                .heightPercent(100)
                .paddingAll(0)
                .flexDirection(FlexDirection.COLUMN));
        sliver.style(style -> style.backgroundTexture(rect(0xB810171D)).zIndex(7));
        sliver.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());

        for (SlotWorkspaceViewModel.AtlasIsland island : entries) {
            sliver.addChild(dot(island, entries));
        }
        return sliver;
    }

    private java.util.List<SlotWorkspaceViewModel.AtlasIsland> visibleEntries() {
        java.util.Map<String, Integer> counts = new java.util.HashMap<>();
        for (SlotWorkspaceViewModel.AtlasItem item : host.currentAtlasItems()) {
            counts.merge(item.islandId(), 1, Integer::sum);
        }
        java.util.List<SlotWorkspaceViewModel.AtlasIsland> entries = new java.util.ArrayList<>();
        for (SlotWorkspaceViewModel.AtlasIsland island : host.currentIslands()) {
            if (island.kind() == VisualAtlasIslandKind.TRIAGE) {
                continue;
            }
            if (counts.getOrDefault(island.islandId(), 0) <= 0) {
                continue;
            }
            entries.add(island);
        }
        return entries;
    }

    private UIElement dot(
            SlotWorkspaceViewModel.AtlasIsland island,
            java.util.List<SlotWorkspaceViewModel.AtlasIsland> entries
    ) {
        // The sliver lays its dots out vertically with proportional
        // padding so each dot sits where its section lives in the wall
        // content. Equal spacing is a simple baseline; once we have wall
        // section heights post-build we can switch to a pixel-faithful
        // mapping driven by the scroller's inner layout.
        Button btn = button("", true, 0);
        btn.layout(layout -> layout
                .widthPercent(100)
                .flex(1)
                .paddingAll(0));
        btn.noText();
        btn.style(style -> style.zIndex(8));
        btn.setOnClick(event -> {
            if (event.button != 0) {
                return;
            }
            event.stopPropagation();
            scrollWallToSection(island.islandId());
        });
        host.installTextTooltip(btn, Component.literal(
                island.label() + " — " + countItemsInIsland(island.islandId())));

        int dotColor = island.color() == 0 ? ACCENT : (island.color() | 0xFF000000);
        UIElement swatch = panel(dotColor).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left((SLIVER_WIDTH_PX - DOT_SIZE_PX) / 2)
                .top(DOT_TOP_INSET_PX)
                .width(DOT_SIZE_PX)
                .height(DOT_SIZE_PX));
        swatch.setAllowHitTest(false);
        btn.addChild(swatch);

        installAttentionTracking(btn, island);
        return btn;
    }

    private int countItemsInIsland(String islandId) {
        int count = 0;
        for (SlotWorkspaceViewModel.AtlasItem item : host.currentAtlasItems()) {
            if (islandId.equals(item.islandId())) {
                count++;
            }
        }
        return count;
    }

    private void installAttentionTracking(Button btn, SlotWorkspaceViewModel.AtlasIsland island) {
        boolean[] lastLit = {false};
        btn.addEventListener(UIEvents.TICK, event -> {
            boolean lit = sectionNeedsAttention(island);
            if (lit == lastLit[0]) {
                return;
            }
            lastLit[0] = lit;
            btn.style(style -> style.overlayTexture(
                    lit ? rect(HOVER_ACCENT_OVERLAY) : IGuiTexture.EMPTY));
        });
    }

    private boolean sectionNeedsAttention(SlotWorkspaceViewModel.AtlasIsland island) {
        boolean searching = !host.searchController.normalizedQuery().isBlank();
        if (!isSectionOffscreen(island.islandId())) {
            return false;
        }
        SlotWorkspaceViewModel.IdentityRef hovered = host.currentMapFocusIdentity();
        for (SlotWorkspaceViewModel.AtlasItem item : host.currentAtlasItems()) {
            if (!island.islandId().equals(item.islandId())) {
                continue;
            }
            if (hovered != null && hovered.equals(item.identity())) {
                return true;
            }
            if (searching && host.searchController.matchesItem(item)) {
                return true;
            }
            if (item.kitNeeded() || item.wanted()) {
                return true;
            }
        }
        return false;
    }

    private boolean isSectionOffscreen(String islandId) {
        if (host.wallScroller == null) {
            return false;
        }
        UIElement section = findSectionElement(islandId);
        if (section == null) {
            return false;
        }
        float scrollerTop = host.wallScroller.getPositionY();
        float scrollerHeight = host.wallScroller.getSizeHeight();
        float sectionTop = section.getPositionY();
        float sectionHeight = section.getSizeHeight();
        float sectionBottom = sectionTop + sectionHeight;
        float scrollerBottom = scrollerTop + scrollerHeight;
        return sectionBottom <= scrollerTop || sectionTop >= scrollerBottom;
    }

    private UIElement findSectionElement(String islandId) {
        if (host.wallScroller == null) {
            return null;
        }
        for (UIElement child : host.wallScroller.viewContainer.getChildren()) {
            if (islandId.equals(child.getId())) {
                return child;
            }
        }
        return null;
    }

    void scrollWallToSection(String islandId) {
        if (host.wallScroller == null) {
            return;
        }
        UIElement section = findSectionElement(islandId);
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
