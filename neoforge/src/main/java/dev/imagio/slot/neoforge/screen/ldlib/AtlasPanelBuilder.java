package dev.imagio.slot.neoforge.screen.ldlib;

import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.*;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceUi.*;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.StorageZoneBounds;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.TaffyPosition;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import dev.imagio.slot.neoforge.screen.ldlib.util.Observable;

import java.util.Set;

final class AtlasPanelBuilder {
    private final SlotWorkspaceUiController host;

    AtlasPanelBuilder(SlotWorkspaceUiController host) {
        this.host = host;
    }

    UIElement body() {
        UIElement body = new UIElement().layout(layout -> layout
                .widthPercent(100)
                .flex(1)
                .gapAll(8)
                .flexDirection(FlexDirection.ROW));
        body.addChildren(atlasPanel());
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
                .heightPercent(100)
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

        // Refresh atlas host.content (islands/cards/chest tiles/link threads) in-place.
        atlas.clearAllContentChildren();
        buildAtlas(atlas);

        panel.addChildren(atlas, host.triagePanel.overlay(), host.belt.overlay());
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
        UIElement linkPopover = host.menu.chestLinkPopover();
        if (linkPopover != null) {
            panel.addChild(linkPopover);
        }
    }

    void buildAtlas(SlotAtlasGraphView atlas) {
        // Phase 3 of docs/plans/storage-areas.md: render storage areas as
        // first-class atlas containers. Each area is either expanded (its
        // chest tiles render with a per-area backdrop + header, link
        // threads draw normally) or collapsed (a small chip stands in).
        java.util.Set<String> visibleStorageIds = new java.util.HashSet<>();
        for (SlotWorkspaceViewModel.StorageAreaSnapshot area : host.viewModel.storageAreas()) {
            boolean expanded = area.shouldExpand() || host.expandedAreaIds.contains(area.areaId());
            if (expanded) {
                StorageZoneBounds bounds = host.islandChest.storageAreaBounds(area);
                if (bounds != null) {
                    atlas.addContentChild(host.islandChest.storageZoneBackdrop(bounds));
                    atlas.addContentChild(host.islandChest.storageAreaHeader(bounds, atlas, area));
                }
                for (SlotWorkspaceViewModel.ClaimedChestTile tile : area.chestTiles()) {
                    visibleStorageIds.add(tile.storageId());
                }
            }
        }
        // Link threads + arrows go in FIRST. LDLib's draw order is
        // child-insertion order (UIElement.drawContents iterates the
        // children list, not getSortedChildren — zIndex only affects
        // hit testing). So anything drawn later sits visually on top.
        // Threads + dim threads only render when the source chest is
        // visible (its area is expanded); when the area is collapsed
        // into a chip, the chip itself stands in for the affordance.
        for (SlotWorkspaceViewModel.ClaimedChestTile tile : host.viewModel.claimedChestTiles()) {
            if (!tile.proximate() || !visibleStorageIds.contains(tile.storageId())) {
                continue;
            }
            for (String islandId : tile.linkedIslandIds()) {
                SlotWorkspaceViewModel.AtlasIsland island = host.viewModel.island(islandId);
                if (island == null) {
                    continue;
                }
                host.islandChest.addLinkAffordances(atlas, tile, island);
            }
        }
        host.dimLinkThreadsByIsland.clear();
        for (SlotWorkspaceViewModel.ClaimedChestTile tile : host.viewModel.claimedChestTiles()) {
            if (tile.proximate() || !visibleStorageIds.contains(tile.storageId())) {
                continue;
            }
            for (String linkedIslandId : tile.linkedIslandIds()) {
                SlotWorkspaceViewModel.AtlasIsland linked = host.viewModel.island(linkedIslandId);
                if (linked == null) {
                    continue;
                }
                UIElement dimThread = host.islandChest.dimLinkThread(tile, linked);
                if (dimThread == null) {
                    continue;
                }
                dimThread.setVisible(linkedIslandId.equals(host.hoveredIslandId));
                host.dimLinkThreadsByIsland
                        .computeIfAbsent(linkedIslandId, k -> new java.util.ArrayList<>())
                        .add(dimThread);
                atlas.addContentChild(dimThread);
            }
        }
        Set<String> highlightedIslandIds = host.islandChest.highlightedIslandIdsFromProximateTiles();
        for (SlotWorkspaceViewModel.AtlasIsland island : host.viewModel.islands()) {
            UIElement islandPanelEl = host.islandChest.islandPanel(atlas, island);
            atlas.addContentChild(islandPanelEl);
            atlas.addContentChild(host.islandChest.islandTitleBar(atlas, island, islandPanelEl));
            if (island.carriedCount() > 0) {
                atlas.addContentChild(host.islandChest.islandCarriedBadge(atlas, island));
            }
        }
        for (String islandId : highlightedIslandIds) {
            SlotWorkspaceViewModel.AtlasIsland island = host.viewModel.island(islandId);
            if (island != null) {
                host.islandChest.addIslandHighlightFrame(atlas, island);
            }
        }
        // Render chest tiles for expanded areas, and chips for collapsed.
        for (SlotWorkspaceViewModel.StorageAreaSnapshot area : host.viewModel.storageAreas()) {
            boolean expanded = area.shouldExpand() || host.expandedAreaIds.contains(area.areaId());
            if (expanded) {
                for (SlotWorkspaceViewModel.ClaimedChestTile tile : area.chestTiles()) {
                    atlas.addContentChild(host.islandChest.chestTilePanel(atlas, tile));
                }
            } else {
                atlas.addContentChild(host.islandChest.storageAreaChip(atlas, area));
            }
        }
        // Backwards-compat: if a save somehow has chest tiles outside any
        // area (shouldn't happen post-Phase 1 migration, but the projection
        // doesn't hard-fail on dangling area refs), still render them at
        // their atlas position so the player can see + relabel them.
        if (host.viewModel.storageAreas().isEmpty()) {
            for (SlotWorkspaceViewModel.ClaimedChestTile tile : host.viewModel.claimedChestTiles()) {
                atlas.addContentChild(host.islandChest.chestTilePanel(atlas, tile));
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
