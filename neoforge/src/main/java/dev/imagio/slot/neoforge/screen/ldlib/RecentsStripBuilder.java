package dev.imagio.slot.neoforge.screen.ldlib;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.ui.spi.SlotUiElement;
import dev.imagio.slot.ui.workspace.RecentsStripUiBuilder;
import dev.imagio.slot.ui.workspace.WorkspaceUiAttachments;

/**
 * Pinned MRU strip of small item icons that lives above the wall
 * scroller and outside it. Click an icon → scroll the wall to that
 * identity's home section and focus the homed card.
 *
 * <p>Folds the player's "where did the thing I just grabbed end up?"
 * question into a single decorative pointer surface — these are not
 * full cards, no badges, no drag.
 */
final class RecentsStripBuilder {
    private final SlotWorkspaceUiController host;
    private final RecentsStripUiBuilder builder;
    private final LdlibSlotUiRenderer renderer;

    RecentsStripBuilder(SlotWorkspaceUiController host) {
        this.host = host;
        this.builder = new RecentsStripUiBuilder(new RecentsContext());
        this.renderer = new LdlibSlotUiRenderer(this::installRecentsInteractions);
    }

    UIElement overlay() {
        return renderer.render(builder.overlay(host.viewModel.recentIdentities()));
    }

    private void installRecentsInteractions(SlotUiElement model, UIElement element) {
        if (!model.hasAttachment(WorkspaceUiAttachments.RECENTS_ICON) || !(element instanceof Button button)) {
            return;
        }
        SlotWorkspaceViewModel.AtlasItem item =
                model.attachment(WorkspaceUiAttachments.ATLAS_ITEM, SlotWorkspaceViewModel.AtlasItem.class);
        host.drag.installAtlasHoverTooltip(button, item);
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
        host.rememberWallScroll(normalized);
    }

    private final class RecentsContext implements RecentsStripUiBuilder.Context {
        @Override
        public SlotWorkspaceViewModel.AtlasItem atlasItem(SlotWorkspaceViewModel.IdentityRef identity) {
            return host.viewModel.atlasItem(identity);
        }

        @Override
        public void focusRecent(SlotWorkspaceViewModel.AtlasItem item) {
            host.hoveredAtlasIdentity = item.identity();
            scrollWallToIsland(item.islandId());
        }

        @Override
        public void hoverRecent(SlotWorkspaceViewModel.AtlasItem item) {
            host.hoveredAtlasIdentity = item == null ? null : item.identity();
        }

        @Override
        public void clearHoveredRecent(SlotWorkspaceViewModel.AtlasItem item) {
            if (item != null && item.identity().equals(host.hoveredAtlasIdentity)) {
                host.hoveredAtlasIdentity = null;
            }
        }
    }
}
