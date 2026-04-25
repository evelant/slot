package dev.imagio.slot.neoforge.screen.ldlib;

import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.*;

import dev.imagio.slot.atlas.FitCarriedCamera;
import dev.imagio.slot.atlas.lod.AtlasLayoutResult;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceAtlasLayout;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;

import java.util.ArrayList;
import java.util.List;

final class CameraNavigator {
    private final SlotWorkspaceUiController host;

    CameraNavigator(SlotWorkspaceUiController host) {
        this.host = host;
    }

    AtlasCamera resolvePeekTarget() {
        SlotAtlasGraphView atlas = host.cameraController.graphView();
        if (atlas == null) {
            return null;
        }
        if (host.hoveredHotbarIndex >= 0 && host.hoveredHotbarIndex < host.viewModel.hotbarSlots().size()) {
            SlotWorkspaceViewModel.HotbarSlot slot = host.viewModel.hotbarSlots().get(host.hoveredHotbarIndex);
            if (slot.occupied()) {
                SlotWorkspaceViewModel.IdentityRef identity = SlotWorkspaceViewModel.IdentityRef.from(
                        dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(slot.displayStack()));
                SlotWorkspaceViewModel.AtlasItem item = host.islandChest.atlasItemInIslandLayer(identity);
                if (item != null) {
                    return computeAtlasItemCamera(atlas, item);
                }
            }
        }
        if (host.hoveredAtlasIdentity != null) {
            SlotWorkspaceViewModel.AtlasItem item = host.islandChest.atlasItemInIslandLayer(host.hoveredAtlasIdentity);
            if (item != null) {
                AtlasCamera camera = computeAtlasItemCamera(atlas, item);
                if (camera != null) {
                    return camera;
                }
            }
            if (!host.viewModel.atlasItems().isEmpty()) {
                SlotWorkspaceViewModel.AtlasItem atlasItem = host.viewModel.atlasItem(host.hoveredAtlasIdentity);
                if (atlasItem != null && !atlasItem.presence().isEmpty()) {
                    SlotWorkspaceViewModel.ClaimedChestTile tile = host.viewModel.claimedChestTile(
                            atlasItem.presence().get(0).storageId());
                    if (tile != null) {
                        return computeChestTileCamera(atlas, tile);
                    }
                }
            }
        }
        if (host.hoveredChestCellIdentity != null) {
            SlotWorkspaceViewModel.AtlasItem item = host.islandChest.atlasItemInIslandLayer(host.hoveredChestCellIdentity);
            if (item != null) {
                AtlasCamera camera = computeAtlasItemCamera(atlas, item);
                if (camera != null) {
                    return camera;
                }
            }
            if (host.hoveredChestCellStorageId != null) {
                SlotWorkspaceViewModel.ClaimedChestTile tile =
                        host.viewModel.claimedChestTile(host.hoveredChestCellStorageId);
                if (tile != null) {
                    return computeChestTileCamera(atlas, tile);
                }
            }
        }
        return null;
    }

    AtlasCamera computeAtlasItemCamera(SlotAtlasGraphView atlas, SlotWorkspaceViewModel.AtlasItem item) {
        if (atlas == null || item == null) {
            return null;
        }
        float viewportWidth = atlas.getContentWidth();
        float viewportHeight = atlas.getContentHeight();
        if (viewportWidth <= 0f || viewportHeight <= 0f) {
            return null;
        }
        AtlasLayoutResult.ItemPlacement place = host.placementFor(item);
        FitCarriedCamera.Camera camera = FitCarriedCamera.fit(
                FitCarriedCamera.Rect.of(place.x(), place.y(), place.width(), place.height()),
                viewportWidth,
                viewportHeight,
                CARRIED_FIT_MIN_SCALE,
                CARRIED_FIT_MAX_SCALE,
                CARRIED_FIT_PADDING_PX
        );
        return camera == null ? null : new AtlasCamera(camera.offsetX(), camera.offsetY(), camera.scale());
    }

    void applyInitialCamera(SlotAtlasGraphView atlas) {
        float viewportWidth = atlas.getContentWidth();
        float viewportHeight = atlas.getContentHeight();
        if (viewportWidth <= 0f || viewportHeight <= 0f) {
            // Viewport not laid out yet; leave atlasCamera null so the next
            // LAYOUT_CHANGED pass can compute the real fit-carried camera.
            return;
        }

        AtlasCamera camera = computeOverviewCamera(viewportWidth, viewportHeight);
        if (camera == null) {
            atlas.fitToChildren(CARRIED_FIT_PADDING_PX, 0.45f);
            atlas.captureCamera();
            return;
        }
        atlas.restoreCamera(camera);
    }

    AtlasCamera computeOverviewCamera(float viewportWidth, float viewportHeight) {
        ArrayList<FitCarriedCamera.Rect> fitRects = new ArrayList<>();
        for (SlotWorkspaceViewModel.AtlasIsland island : host.viewModel.islands()) {
            AtlasLayoutResult.IslandPlacement placement = host.currentLayout.islandPlacementOf(island.islandId());
            if (placement != null) {
                fitRects.add(FitCarriedCamera.Rect.of(
                        placement.x(), placement.y(), placement.width(), placement.height()));
            } else {
                fitRects.add(FitCarriedCamera.Rect.of(
                        island.x(), island.y(), island.width(), island.height()));
            }
        }
        for (SlotWorkspaceViewModel.AtlasItem item : host.viewModel.atlasItems()) {
            if (item.carried()) {
                AtlasLayoutResult.ItemPlacement place = host.placementFor(item);
                fitRects.add(FitCarriedCamera.Rect.of(place.x(), place.y(), place.width(), place.height()));
            }
        }
        if (fitRects.isEmpty()) {
            return null;
        }
        FitCarriedCamera.Rect bbox = FitCarriedCamera.union(fitRects);
        if (bbox == null) {
            return null;
        }
        // Reserve screen space for the nav capsule (top-left chrome) and the
        // belt overlay (bottom chrome) so they do not occlude host.content at the
        // default overview zoom.
        float effectiveWidth = Math.max(1f, viewportWidth - 2f * SIDE_CAMERA_INSET_PX);
        float effectiveHeight = Math.max(1f, viewportHeight - NAV_CAPSULE_INSET_PX - BELT_CAMERA_INSET_PX);
        float scale = Math.min(
                effectiveWidth / Math.max(1f, bbox.width()),
                effectiveHeight / Math.max(1f, bbox.height())
        );
        scale = Math.max(CARRIED_FIT_MIN_SCALE, Math.min(CARRIED_FIT_MAX_SCALE, scale));
        float centerScreenX = viewportWidth / 2f;
        float centerScreenY = (NAV_CAPSULE_INSET_PX + viewportHeight - BELT_CAMERA_INSET_PX) / 2f;
        float offsetX = bbox.centerX() - centerScreenX / scale;
        float offsetY = bbox.centerY() - centerScreenY / scale;
        return new AtlasCamera(offsetX, offsetY, scale);
    }

    void panToChestTile(SlotAtlasGraphView atlas, SlotWorkspaceViewModel.ClaimedChestTile tile) {
        if (atlas == null || tile == null) {
            return;
        }
        AtlasCamera target = computeChestTileCamera(atlas, tile);
        if (target != null) {
            host.cameraController.commit(
                    target,
                    AtlasCameraController.CommitSource.PAN_TO_CHEST,
                    AtlasCameraController.CUBIC_IN_OUT,
                    AtlasCameraController.COMMIT_DURATION_MS);
        }
    }

    AtlasCamera computeChestTileCamera(SlotAtlasGraphView atlas, SlotWorkspaceViewModel.ClaimedChestTile tile) {
        if (atlas == null || tile == null) {
            return null;
        }
        float viewportWidth = atlas.getContentWidth();
        float viewportHeight = atlas.getContentHeight();
        if (viewportWidth <= 0f || viewportHeight <= 0f) {
            return null;
        }
        FitCarriedCamera.Camera camera = FitCarriedCamera.fit(
                FitCarriedCamera.Rect.of(tile.atlasX(), tile.atlasY(), tile.width(), tile.height()),
                viewportWidth,
                viewportHeight,
                CARRIED_FIT_MIN_SCALE,
                CARRIED_FIT_MAX_SCALE,
                CARRIED_FIT_PADDING_PX
        );
        return camera == null ? null : new AtlasCamera(camera.offsetX(), camera.offsetY(), camera.scale());
    }

    void panToIsland(SlotAtlasGraphView atlas, SlotWorkspaceViewModel.AtlasIsland island) {
        if (atlas == null || island == null) {
            return;
        }
        AtlasCamera target = computeIslandCamera(atlas, island);
        if (target != null) {
            host.cameraController.commit(
                    target,
                    AtlasCameraController.CommitSource.PAN_TO_ISLAND,
                    AtlasCameraController.CUBIC_IN_OUT,
                    AtlasCameraController.COMMIT_DURATION_MS);
        }
    }

    AtlasCamera computeIslandCamera(SlotAtlasGraphView atlas, SlotWorkspaceViewModel.AtlasIsland island) {
        if (atlas == null || island == null) {
            return null;
        }
        float viewportWidth = atlas.getContentWidth();
        float viewportHeight = atlas.getContentHeight();
        if (viewportWidth <= 0f || viewportHeight <= 0f) {
            return null;
        }
        FitCarriedCamera.Camera camera = FitCarriedCamera.fit(
                FitCarriedCamera.Rect.of(island.x(), island.y(), island.width(), island.height()),
                viewportWidth,
                viewportHeight,
                CARRIED_FIT_MIN_SCALE,
                CARRIED_FIT_MAX_SCALE,
                CARRIED_FIT_PADDING_PX
        );
        return camera == null ? null : new AtlasCamera(camera.offsetX(), camera.offsetY(), camera.scale());
    }

}
