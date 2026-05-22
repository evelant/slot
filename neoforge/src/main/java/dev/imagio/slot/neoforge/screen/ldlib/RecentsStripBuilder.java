package dev.imagio.slot.neoforge.screen.ldlib;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.ui.spi.SlotUiElement;
import dev.imagio.slot.ui.workspace.RecentsStripUiBuilder;
import dev.imagio.slot.ui.workspace.WorkspaceUiAttachments;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Pinned MRU strip that lives above the wall scroller and outside it.
 * Recents render as normal atlas cards so the same card shortcuts work
 * there; Shift+Tab handles the explicit scroll-to-home gesture.
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
        if (model.hasAttachment(WorkspaceUiAttachments.WALL_CARD)
                || model.hasAttachment(WorkspaceUiAttachments.WALL_CARD_BODY)) {
            host.atlasCard.installCardInteractions(model, element);
        }
    }

    private final class RecentsContext implements RecentsStripUiBuilder.Context {
        @Override
        public SlotWorkspaceViewModel.AtlasItem atlasItem(SlotWorkspaceViewModel.IdentityRef identity) {
            SlotWorkspaceViewModel.AtlasItem item = host.viewModel.atlasItem(identity);
            return item == null ? cursorRecentItem(identity) : item;
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

        private SlotWorkspaceViewModel.AtlasItem cursorRecentItem(SlotWorkspaceViewModel.IdentityRef identity) {
            SlotWorkspaceViewModel.IdentityRef cursorIdentity = WorkspaceCursorState.carriedIdentity();
            if (identity == null || cursorIdentity == null || !identity.equals(cursorIdentity)) {
                return null;
            }
            ItemStack stack = WorkspaceCursorState.carriedStack();
            if (stack.isEmpty()) {
                return null;
            }
            return new SlotWorkspaceViewModel.AtlasItem(
                    identity,
                    stack,
                    stack.getHoverName().getString(),
                    stack.getCount(),
                    0,
                    "",
                    true,
                    false,
                    true,
                    List.of());
        }
    }
}
