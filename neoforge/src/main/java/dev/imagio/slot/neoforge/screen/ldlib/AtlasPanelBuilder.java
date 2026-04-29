package dev.imagio.slot.neoforge.screen.ldlib;

import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.*;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceUi.*;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.TaffyPosition;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import dev.imagio.slot.neoforge.screen.ldlib.util.Observable;

final class AtlasPanelBuilder {
    private final SlotWorkspaceUiController host;

    AtlasPanelBuilder(SlotWorkspaceUiController host) {
        this.host = host;
    }

    UIElement body() {
        UIElement body = new UIElement().layout(layout -> layout
                .widthPercent(100)
                .flex(1)
                .gapAll(0)
                .flexDirection(FlexDirection.COLUMN));
        body.addChild(atlasPanel());
        return body;
    }

    UIElement atlasPanel() {
        if (host.atlasPanelElement == null) {
            createPersistentAtlasPanel();
        }
        repopulateAtlasPanel();
        return host.atlasPanelElement;
    }

    void createPersistentAtlasPanel() {
        UIElement panel = panel(PANEL).layout(layout -> layout
                .flex(1)
                .widthPercent(100)
                .paddingAll(0));
        host.clearSelectionOnDirectClick(panel);

        SlotAtlasGraphView atlas = new SlotAtlasGraphView();
        atlas.onCameraChanged(camera -> host.atlasCamera = camera);
        atlas.setPerFrameTick(() -> {
            // Rebuild flushing used to live here, but running it mid-
            // render (inside drawBackgroundTexture) let ancestors draw
            // with stale trees for one frame. The flush is now in the
            // game-tick TICK listener on host.root, which runs before any
            // rendering. This hook keeps doing the animation-driven
            // screenTick kick so cards keep receiving TICKs during
            // camera animations even when the tick-rate screenTick
            // already fired.
            boolean wasAnimating = host.cameraController.isAnimating();
            host.cameraController.tick();
            if (atlas.getContentWidth() <= 0f) {
                return;
            }
            if (wasAnimating || host.atlasContentNeedsScreenTick) {
                atlas.screenTick();
                host.atlasContentNeedsScreenTick = false;
            }
        });
        host.cameraController.attach(atlas);
        atlas.layout(layout -> layout.widthPercent(100).heightPercent(100));
        atlas.style(style -> style.backgroundTexture(rect(0xB810171D)).zIndex(0));
        atlas.graphViewStyle(style -> style
                .minScale(0.05f)
                .maxScale(4.50f)
                .gridTexture(IGuiTexture.EMPTY)
                .gridSize(48));
        atlas.addEventListener(UIEvents.LAYOUT_CHANGED, event -> {
            if (host.atlasCamera == null) {
                host.camera.applyInitialCamera(atlas);
            } else {
                atlas.restoreCamera(host.atlasCamera);
            }
        });
        host.drag.installAtlasCanvasDropTarget(panel, atlas);
        host.drag.installAtlasBackgroundDropTarget(atlas);

        host.atlasView = atlas;
        host.hoverTrailOverlayElement = host.islandChest.hoverTrailOverlay(atlas);
        host.carriedFreeSlotsChipElement = host.overlays.carriedFreeSlotsChip();
        host.topRightActionsElement = host.overlays.topRightActionsOverlay();
        host.atlasPanelElement = panel;
        host.atlasContentNeedsScreenTick = true;
    }

    void repopulateAtlasPanel() {
        host.atlasContentNeedsScreenTick = true;
        for (Observable.Subscription sub : host.atlasContentSubscriptions) {
            sub.unsubscribe();
        }
        host.atlasContentSubscriptions.clear();
        UIElement panel = host.atlasPanelElement;
        SlotAtlasGraphView atlas = host.atlasView;

        // Drop transient children (overlays + popovers). Persistent children
        // (host.atlasView, host.hoverTrailOverlayElement) will be re-added below.
        panel.clearAllChildren();

        // Refresh atlas host.content (islands/cards) in-place.
        atlas.clearAllContentChildren();
        buildAtlas(atlas);

        panel.addChildren(atlas, host.triagePanel.overlay(), host.belt.overlay());
        UIElement chestChips = host.storagePanel.overlay();
        if (chestChips != null) {
            panel.addChild(chestChips);
        }
        panel.addChild(host.hoverTrailOverlayElement);
        panel.addChild(host.carriedFreeSlotsChipElement);
        panel.addChild(host.topRightActionsElement);
        if (host.searchController.modalActive()) {
            panel.addChild(host.overlays.searchChipOverlay());
        } else {
            panel.addChild(host.overlays.searchHintOverlay());
        }
        if (host.kitRackOpen) {
            panel.addChild(host.kit.kitRackOverlay());
        }
        UIElement contextMenu = host.menu.contextMenuOverlay();
        if (contextMenu != null) {
            panel.addChild(contextMenu);
        }
        UIElement editPopover = host.menu.islandEditPopover();
        if (editPopover != null) {
            panel.addChild(editPopover);
        }
        UIElement createPopover = host.menu.createIslandPopover();
        if (createPopover != null) {
            panel.addChild(createPopover);
        }
    }

    void buildAtlas(SlotAtlasGraphView atlas) {
        for (SlotWorkspaceViewModel.AtlasIsland island : host.viewModel.islands()) {
            UIElement islandPanelEl = host.islandChest.islandPanel(atlas, island);
            atlas.addContentChild(islandPanelEl);
            atlas.addContentChild(host.islandChest.islandTitleBar(atlas, island, islandPanelEl));
            if (island.carriedCount() > 0) {
                atlas.addContentChild(host.islandChest.islandCarriedBadge(atlas, island));
            }
            UIElement presence = host.islandChest.islandPresenceStrip(island);
            if (presence != null) {
                atlas.addContentChild(presence);
            }
        }
        for (SlotWorkspaceViewModel.AtlasItem item : host.viewModel.atlasItems()) {
            atlas.addContentChild(host.atlasCard.atlasCardButton(atlas, item));
            host.atlasCard.addAtlasItemChips(atlas, item);
        }
        if (host.viewModel.atlasItems().isEmpty()) {
            UIElement empty = label("No main inventory stacks visible", MUTED)
                    .layout(layout -> layout
                            .positionType(TaffyPosition.ABSOLUTE)
                            .left(host.viewModel.canvasWidth() / 2f - 104)
                            .top(host.viewModel.canvasHeight() / 2f - 8)
                            .width(208)
                            .height(16));
            empty.setAllowHitTest(false);
            atlas.addContentChild(empty);
        }
    }

}
