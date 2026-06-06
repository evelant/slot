package dev.imagio.slot.neoforge.screen.ldlib;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.neoforge.config.SlotClientConfig;
import dev.imagio.slot.ui.spi.SlotUiElement;
import dev.imagio.slot.ui.workspace.RecentsStripUiBuilder;
import dev.imagio.slot.ui.workspace.WorkspaceUiAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Pinned MRU strip that lives above the wall scroller and outside it.
 * Recents render as normal atlas cards so the same card shortcuts work there.
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

    UIElement floatingOverlay() {
        UIElement strip = overlay();
        strip.layout(layout -> layout
                .positionType(dev.vfyjxf.taffy.style.TaffyPosition.ABSOLUTE)
                .left(RecentsStripUiBuilder.floatingLeft(screenWidth(),
                        SlotClientConfig.CLIENT.recentsHorizontalOffset.get()))
                .top(RecentsStripUiBuilder.floatingTop(SlotClientConfig.CLIENT.recentsTopOffset.get()))
                .width(RecentsStripUiBuilder.STRIP_WIDTH_PX)
                .height(RecentsStripUiBuilder.STRIP_HEIGHT_PX));
        strip.style(style -> style.zIndex(18));
        return strip;
    }

    private static int screenWidth() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft == null ? RecentsStripUiBuilder.STRIP_WIDTH_PX : minecraft.getWindow().getGuiScaledWidth();
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
