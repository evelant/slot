package dev.imagio.slot.neoforge.screen.ldlib;

import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceFormat.*;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.*;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceUi.*;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceAtlasLayout;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.AtlasItemDrag;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.HotbarSlotDrag;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

final class IslandChestBuilder {
    private final SlotWorkspaceUiController host;

    IslandChestBuilder(SlotWorkspaceUiController host) {
        this.host = host;
    }

    UIElement islandPanel(SlotAtlasGraphView atlas, SlotWorkspaceViewModel.AtlasIsland island) {
        dev.imagio.slot.atlas.lod.AtlasLayoutResult.IslandPlacement place = host.islandPlacementFor(island);
        UIElement panel = panel(island.color()).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(place.x())
                .top(place.y())
                .width(place.width())
                .height(place.height())
                .paddingAll(8)
                .gapAll(3)
                .flexDirection(FlexDirection.COLUMN));
        // zIndex in LDLib2 only affects hit-test priority (see
        // UIElement.getSortedChildren, used by UIEventDispatcher) — the
        // draw order is child-insertion order. Give the panel a zIndex
        // matching the chest tile panel (1) so a right-click lands on
        // the island body before falling through to the atlas viewport.
        panel.style(style -> style.zIndex(1));

        host.drag.installViewportPanSurface(panel, atlas);
        host.drag.installIslandDropTarget(panel, panel, atlas, island);
        // Body acts as a fallback drag handle when zoomed out far
        // enough that the header strip becomes a few-pixel target on
        // screen. Above the gate scale, item cards take over and the
        // body-drag is suppressed so card interactions stay clean.
        host.drag.installIslandDragSource(panel, atlas, island, 0.6f);

        // Right-click opens the island edit popover anchored near the click,
        // matching how item cards and kit cards surface their context host.menu.
        // Shift+left-click runs the manual tighten gesture (see
        // SlotWorkspaceUiController.tightenIsland and
        // docs/plans/atlas-nudge-layout.md). Capture-phase listener so we
        // run before the viewport pan handler attached on this same panel.
        if (island.kind() == VisualAtlasIslandKind.PLAYER) {
            panel.addEventListener(UIEvents.MOUSE_DOWN, event -> {
                if (event.button == 0 && net.minecraft.client.gui.screens.Screen.hasShiftDown()) {
                    event.stopPropagation();
                    host.tightenIsland(island.islandId());
                    return;
                }
                if (event.button != 1) {
                    return;
                }
                event.stopPropagation();
                host.menu.beginIslandEdit(island, event.x, event.y);
            }, true);
        }

        attachIslandHoverListeners(panel, island);

        return panel;
    }

    /**
     * Track {@code host.hoveredIslandId} on enter/leave so chest cards in
     * the storage strip can highlight themselves when their linked island
     * is hovered. No-op for non-player islands.
     */
    void attachIslandHoverListeners(UIElement element, SlotWorkspaceViewModel.AtlasIsland island) {
        if (element == null || island == null) {
            return;
        }
        if (island.kind() != VisualAtlasIslandKind.PLAYER) {
            return;
        }
        String islandId = island.islandId();
        element.addEventListener(UIEvents.MOUSE_ENTER, event -> {
            host.hoveredIslandId = islandId;
        }, true);
        element.addEventListener(UIEvents.MOUSE_LEAVE, event -> {
            if (islandId.equals(host.hoveredIslandId)) {
                host.hoveredIslandId = null;
            }
        }, true);
    }

    UIElement islandCarriedBadge(SlotAtlasGraphView atlas, SlotWorkspaceViewModel.AtlasIsland island) {
        // Positioned on the title-bar strip above the island panel so it
        // doesn't overlap the first row of item cards. Uses world-unit
        // absolute positioning against atlas host.content like every other
        // atlas-level element.
        dev.imagio.slot.atlas.lod.AtlasLayoutResult.IslandPlacement place = host.islandPlacementFor(island);
        Button badge = button(island.carriedCount() + "●", true, ACTIVE_HOTBAR);
        badge.layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(place.x() + 2)
                .top(place.y() - 14)
                .width(26)
                .height(12));
        badge.textStyle(style -> style
                .textColor(TEXT)
                .textShadow(false)
                .fontSize(7)
                .textAlignHorizontal(Horizontal.CENTER)
                .textAlignVertical(Vertical.CENTER));
        badge.style(style -> style.zIndex(4));
        badge.setOnClick(event -> {
            event.stopPropagation();
            host.camera.panToIsland(atlas, island);
            host.localStatus.set("panned to " + island.label());
        });
        return badge;
    }

    UIElement islandTitleBar(
            SlotAtlasGraphView atlas,
            SlotWorkspaceViewModel.AtlasIsland island,
            UIElement islandPanelEl
    ) {
        dev.imagio.slot.atlas.lod.AtlasLayoutResult.IslandPlacement place = host.islandPlacementFor(island);
        Button header = button(island.label(), true, island.color());
        header.layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(place.x())
                .top(place.y() - 16)
                .width(place.width())
                .height(14));
        header.style(style -> style.zIndex(3));
        header.addEventListener(UIEvents.CLICK, event -> {
            if (event.button != 0) {
                return;
            }
            event.stopPropagation();
            if (host.selectedAtlasItem() == null) {
                host.localStatus.set("select a triage or homed item first");
                return;
            }
            host.rpc.sendAssignHome(island.islandId());
        });
        // Right-click on the header opens the edit popover, matching the
        // island body's behaviour. Without this, right-click on the
        // header would fall through to the atlas viewport (which pans on
        // right-drag) instead of surfacing the context menu the user
        // expects when targeting the island by its label.
        if (island.kind() == VisualAtlasIslandKind.PLAYER) {
            header.addEventListener(UIEvents.MOUSE_DOWN, event -> {
                if (event.button != 1) {
                    return;
                }
                event.stopPropagation();
                host.menu.beginIslandEdit(island, event.x, event.y);
            }, true);
        }
        host.drag.installIslandDragSource(header, atlas, island);
        host.drag.installIslandDropTarget(header, islandPanelEl, atlas, island);
        attachIslandHoverListeners(header, island);
        installIslandHoverPaint(header, island.islandId());

        float[] lastScale = {Float.NaN};
        int[] lastWorldFontQuarter = {-1};
        Runnable applyHeaderScale = () -> {
            // Track the actual render scale (not animation target) so the
            // header stays sized to the live view each frame. Using
            // host.animationTargetScale caused the header to be sized for the
            // final scale while the pose stack drew at the interpolated
            // current scale — visible as a flash at animation boundaries.
            float scale = Math.max(0.0001f, atlas.getScale());
            if (scale == lastScale[0]) {
                return;
            }
            lastScale[0] = scale;
            // Re-resolve every tick — when the layout reflows (search,
            // kit activation, ordinal drag-drop) the island bounds shift.
            dev.imagio.slot.atlas.lod.AtlasLayoutResult.IslandPlacement currentPlace = host.islandPlacementFor(island);
            float islandScreenWidth = currentPlace.width() * scale;
            float requestedFontPx = Math.min(12f, islandScreenWidth * 0.13f);
            float screenFontPx = headerBreakpointFontPx(Math.max(7f, requestedFontPx));
            float screenHeaderHeight = screenFontPx + 3f;
            // Floor the world height at the carried-count badge's world
            // size (12 world units plus a 2-unit margin = 14) so the badge
            // never overflows the header background. Without this, at
            // scale > ~1 the screen-fixed header shrinks in world space
            // below the badge's world size and the counter visibly
            // escapes its backdrop.
            //
            // Ceiling at SlotWorkspaceAtlasLayout.ISLAND_HEADER_RESERVE
            // (24 wu) so zooming out doesn't grow the header without
            // bound and crash it into neighbours above. AtlasLayout.packAtlas
            // reserves the same band when de-overlapping islands —
            // both constants must stay in sync.
            float screenScaledWorldHeader = screenHeaderHeight / Math.max(0.0001f, scale);
            float worldHeaderHeight = Math.max(14f,
                    Math.min(SlotWorkspaceAtlasLayout.ISLAND_HEADER_RESERVE, screenScaledWorldHeader));
            float screenGap = 2f;
            float worldGap = screenGap / Math.max(0.0001f, scale);

            // Derive worldFontPx FROM the (clamped) strip height — when
            // the screen-scaled height is capped by the world ceiling,
            // the font has to shrink with it or the text overflows the
            // strip vertically. Reserve 3 wu for the strip's internal
            // padding (matching the +3 in screenHeaderHeight above).
            float worldFontPx = Math.max(1f, worldHeaderHeight - 3f);

            // Re-apply textStyle whenever the quantized world fontSize
            // changes. The previous gate on screenFontPx alone missed the
            // case where scale shifts within a clamped range (min/max of
            // screenFontPx), which left worldFontPx baked at the old scale
            // and the rendered screen pixels drifting with zoom.
            int worldFontQuarter = Math.max(1, Math.round(worldFontPx * 4f));
            if (worldFontQuarter != lastWorldFontQuarter[0]) {
                lastWorldFontQuarter[0] = worldFontQuarter;
                float quantizedWorldFont = worldFontQuarter / 4f;
                header.textStyle(style -> style
                        .textColor(TEXT)
                        .textShadow(true)
                        .fontSize(quantizedWorldFont)
                        .textAlignHorizontal(Horizontal.CENTER)
                        .textAlignVertical(Vertical.CENTER));
            }
            header.layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(currentPlace.x())
                    .top(Math.round(currentPlace.y() - worldHeaderHeight - worldGap))
                    .width(currentPlace.width())
                    .height(Math.round(worldHeaderHeight)));
            header.markTaffyStyleDirty();
        };
        // Prime at build time so the first rendered frame after a host.rebuild
        // already has the scale-correct font/layout. Without this, the
        // header renders at Button's default fontSize until the next
        // screen tick fires — which, during rapid rebuilds from scroll-
        // wheel transfer, could be several frames of flicker.
        applyHeaderScale.run();
        header.addEventListener(UIEvents.TICK, event -> applyHeaderScale.run());
        return header;
    }

    /**
     * Aggregate proximate-chest counts for items homed to this island.
     * Surfaced as a single line under the island, e.g. "12 in 2 chests".
     * Returns null when the island has no proximate stock.
     */
    UIElement islandPresenceStrip(SlotWorkspaceViewModel.AtlasIsland island) {
        int totalCount = 0;
        java.util.LinkedHashSet<String> chestIds = new java.util.LinkedHashSet<>();
        for (SlotWorkspaceViewModel.AtlasItem item : host.viewModel.atlasItems()) {
            if (!island.islandId().equals(item.islandId())) {
                continue;
            }
            for (SlotWorkspaceViewModel.ChestPresenceEntry entry : item.presence()) {
                totalCount += entry.count();
                if (entry.storageId() != null && !entry.storageId().isBlank()) {
                    chestIds.add(entry.storageId());
                }
            }
        }
        if (totalCount == 0) {
            return null;
        }
        String text = chestIds.size() <= 1
                ? totalCount + " in chest"
                : totalCount + " in " + chestIds.size() + " chests";
        dev.imagio.slot.atlas.lod.AtlasLayoutResult.IslandPlacement place = host.islandPlacementFor(island);
        Label strip = label(text, MUTED);
        strip.layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(place.x())
                .top(place.y() + place.height() + 1)
                .width(place.width())
                .height(8));
        strip.textStyle(style -> style
                .textColor(MUTED)
                .textShadow(false)
                .fontSize(6)
                .textAlignHorizontal(Horizontal.LEFT)
                .textAlignVertical(Vertical.TOP));
        strip.style(style -> style.zIndex(2));
        strip.setAllowHitTest(false);
        return strip;
    }

    /**
     * Per-frame paint flip: when {@code host.hoveredStorageId} matches a
     * chest whose contents include an item homed to {@code islandId}, draw
     * an accent overlay. Replaces the link-era explicit chest-island
     * link as a derived overlap query.
     */
    void installIslandHoverPaint(UIElement element, String islandId) {
        boolean[] lastLit = {false};
        element.addEventListener(UIEvents.TICK, event -> {
            boolean lit = isHoveredChestRelatedToIsland(host.hoveredStorageId, islandId);
            if (lit == lastLit[0]) {
                return;
            }
            lastLit[0] = lit;
            element.style(style -> style.overlayTexture(lit ? rect(HOVER_ACCENT_OVERLAY) : IGuiTexture.EMPTY));
        });
    }

    /** Hovered chest is "related" to an island when any item homed to that
     *  island has presence in that chest. */
    private boolean isHoveredChestRelatedToIsland(String storageId, String islandId) {
        if (storageId == null || islandId == null) {
            return false;
        }
        for (SlotWorkspaceViewModel.AtlasItem item : host.viewModel.atlasItems()) {
            if (!islandId.equals(item.islandId())) {
                continue;
            }
            for (SlotWorkspaceViewModel.ChestPresenceEntry entry : item.presence()) {
                if (storageId.equals(entry.storageId())) {
                    return true;
                }
            }
        }
        return false;
    }

    // Chest tiles no longer render. See docs/plans/learned-storage.md —
    // chests live as chips in the StoragePanelBuilder, and per-chest
    // detail surfaces only on demand.

    SlotWorkspaceViewModel.AtlasItem atlasItemInIslandLayer(SlotWorkspaceViewModel.IdentityRef identity) {
        if (identity == null) {
            return null;
        }
        for (SlotWorkspaceViewModel.AtlasItem candidate : host.viewModel.atlasItems()) {
            if (candidate.identity().equals(identity)) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * Hover-trail line from a hovered hotbar slot to its homed atlas card.
     * Surfaces "this hotbar slot belongs to that island". Trail collapses
     * to nothing when no hover association is active.
     */
    UIElement hoverTrailOverlay(SlotAtlasGraphView atlas) {
        UIElement trail = panel(HOVER_TRAIL_COLOR);
        trail.style(style -> style.zIndex(9));
        trail.setAllowHitTest(false);
        trail.layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(0)
                .top(0)
                .width(0)
                .height(0));
        int[] lastLength = {0};
        trail.addEventListener(UIEvents.TICK, event -> {
            HoverTrailEndpoints endpoints = resolveHoverTrail();
            if (endpoints == null) {
                if (lastLength[0] != 0) {
                    trail.layout(layout -> layout
                            .positionType(TaffyPosition.ABSOLUTE)
                            .left(0).top(0).width(0).height(0));
                    trail.markTaffyStyleDirty();
                    lastLength[0] = 0;
                }
                return;
            }
            UIElement slotElement = host.hotbarSlotElements.get(endpoints.hotbarIndex());
            if (slotElement == null) {
                return;
            }
            float panelLeft = atlas.getPositionX();
            float panelTop = atlas.getPositionY();
            float slotW = slotElement.getSizeWidth();
            float slotH = slotElement.getSizeHeight();
            if (slotW <= 0f || slotH <= 0f) {
                return;
            }
            float originScreenX = slotElement.getPositionX() + slotW / 2f;
            float originScreenY = slotElement.getPositionY() + slotH / 2f;
            dev.imagio.slot.atlas.lod.AtlasLayoutResult.ItemPlacement endpointPlace =
                    host.placementFor(endpoints.atlasItem());
            int worldTargetX = endpointPlace.x() + endpointPlace.width() / 2;
            int worldTargetY = endpointPlace.y() + endpointPlace.height() / 2;
            float targetScreenX = atlas.screenX(worldTargetX);
            float targetScreenY = atlas.screenY(worldTargetY);
            float dx = targetScreenX - originScreenX;
            float dy = targetScreenY - originScreenY;
            double distance = Math.sqrt((double) dx * dx + (double) dy * dy);
            if (distance < 1.0) {
                return;
            }
            int length = Math.max(1, (int) Math.round(distance));
            float angleDeg = (float) Math.toDegrees(Math.atan2(dy, dx));
            int leftRelative = Math.round(originScreenX - panelLeft);
            int topRelative = Math.round(originScreenY - panelTop) - HOVER_TRAIL_THICKNESS / 2;
            trail.layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(leftRelative)
                    .top(topRelative)
                    .width(length)
                    .height(HOVER_TRAIL_THICKNESS));
            trail.transform(transform -> transform.pivot(0f, 0.5f).rotation(angleDeg));
            trail.markTaffyStyleDirty();
            lastLength[0] = length;
        });
        return trail;
    }

    HoverTrailEndpoints resolveHoverTrail() {
        if (host.hoveredHotbarIndex >= 0 && host.hoveredHotbarIndex < host.viewModel.hotbarSlots().size()) {
            SlotWorkspaceViewModel.HotbarSlot slot = host.viewModel.hotbarSlots().get(host.hoveredHotbarIndex);
            if (slot.occupied()) {
                SlotWorkspaceViewModel.IdentityRef identity = SlotWorkspaceViewModel.IdentityRef.from(
                        dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(slot.displayStack()));
                SlotWorkspaceViewModel.AtlasItem item = atlasItemInIslandLayer(identity);
                if (item != null) {
                    return new HoverTrailEndpoints(slot.hotbarIndex(), item);
                }
            }
        }
        if (host.hoveredAtlasIdentity != null) {
            int hotbarIndex = host.hotbarSlotForIdentity(host.hoveredAtlasIdentity);
            SlotWorkspaceViewModel.AtlasItem item = atlasItemInIslandLayer(host.hoveredAtlasIdentity);
            if (hotbarIndex >= 0 && item != null) {
                return new HoverTrailEndpoints(hotbarIndex, item);
            }
        }
        return null;
    }

    record HoverTrailEndpoints(int hotbarIndex, SlotWorkspaceViewModel.AtlasItem atlasItem) {
    }

}
