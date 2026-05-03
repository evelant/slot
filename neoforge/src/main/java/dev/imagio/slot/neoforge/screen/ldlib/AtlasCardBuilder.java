package dev.imagio.slot.neoforge.screen.ldlib;

import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.*;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceUi.*;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.client.gui.screens.Screen;

/**
 * Single-LOD pixel-space card renderer for the sectioned list-view wall.
 * Replaces the prior 5-band LOD cascade. Cards render at a fixed
 * {@link ListWallPanelBuilder#CARD_CELL_PX} screen-pixel size; flex
 * layout in the parent grid handles wrap/positioning. No world-unit
 * math, no camera dependency.
 *
 * <p>Card chrome (status border + colored progress bar + count badge +
 * proximate pip + search outline + deposit preview + container fullness
 * + selection highlight + carried/ghost tint) is preserved at the new
 * fixed size; sized for legibility at the chosen cell size.
 */
final class AtlasCardBuilder {
    private final SlotWorkspaceUiController host;

    AtlasCardBuilder(SlotWorkspaceUiController host) {
        this.host = host;
    }

    /**
     * Build a single atlas card in pixel space. Caller drops the result
     * into a flex grid; ordering is invariant under reflow because the
     * grid emits cards in atlas-items order and flex-wrap reads them
     * left-to-right, top-to-bottom.
     */
    Button atlasCardButton(SlotWorkspaceViewModel.AtlasItem item) {
        boolean selected = item.identity().equals(host.selectedAtlasIdentity.get());
        boolean searchMatch = host.searchController.matchesItem(item);
        boolean filtering = !host.searchController.normalizedQuery().isBlank();
        boolean activeSearchMatch = filtering && searchMatch;
        Button button = button("", true,
                WorkspaceFormat.cardChromeColor(selected, searchMatch, item.recent(),
                        item.carried(), filtering));
        button.layout(layout -> layout
                .width(ListWallPanelBuilder.CARD_CELL_PX)
                .height(ListWallPanelBuilder.CARD_CELL_PX)
                .paddingAll(0));
        button.noText();
        button.style(style -> style.zIndex(2));

        installCardClickHandlers(button, item);
        button.addEventListener(UIEvents.MOUSE_ENTER,
                event -> host.hoveredAtlasIdentity = item.identity(), true);
        button.addEventListener(UIEvents.MOUSE_LEAVE, event -> {
            if (item.identity().equals(host.hoveredAtlasIdentity)) {
                host.hoveredAtlasIdentity = null;
            }
        }, true);
        host.drag.installAtlasHoverTooltip(button, item);
        host.drag.installAtlasItemDragSource(button, item);
        installChestHoverPaint(button, item);

        UIElement body = new UIElement().layout(layout -> layout.widthPercent(100).heightPercent(100));
        body.setAllowHitTest(false);
        buildCardBody(body, item, activeSearchMatch);
        button.addChild(body);

        button.addEventListener(UIEvents.TICK, event -> {
            boolean currentSelected = item.identity().equals(host.selectedAtlasIdentity.get());
            boolean focused = host.isMapFocusItem(item);
            button.style(style -> style.zIndex(focused ? 10 : currentSelected ? 7 : 2));
            applyButtonColors(button, true,
                    WorkspaceFormat.cardChromeColor(currentSelected, searchMatch, item.recent(),
                            item.carried(), !host.searchController.normalizedQuery().isBlank()));
        });
        return button;
    }

    /**
     * Per-frame paint flip: when {@code host.hoveredStorageId} matches a
     * chest with presence for this item's identity, paint an accent overlay.
     * Symmetric with the chip-side flip in {@link StoragePanelBuilder}.
     */
    private void installChestHoverPaint(UIElement button, SlotWorkspaceViewModel.AtlasItem item) {
        boolean[] lastLit = {false};
        button.addEventListener(UIEvents.TICK, event -> {
            boolean lit = isItemPresentInHoveredChest(item);
            if (lit == lastLit[0]) {
                return;
            }
            lastLit[0] = lit;
            button.style(style -> style.overlayTexture(lit ? rect(HOVER_ACCENT_OVERLAY) : IGuiTexture.EMPTY));
        });
    }

    private boolean isItemPresentInHoveredChest(SlotWorkspaceViewModel.AtlasItem item) {
        String storageId = host.hoveredStorageId;
        if (storageId == null || item == null) {
            return false;
        }
        for (SlotWorkspaceViewModel.ChestPresenceEntry entry : item.presence()) {
            if (storageId.equals(entry.storageId())) {
                return true;
            }
        }
        for (SlotWorkspaceViewModel.ChestPresenceEntry entry : item.elsewhere()) {
            if (storageId.equals(entry.storageId())) {
                return true;
            }
        }
        return false;
    }

    void buildCardBody(UIElement body, SlotWorkspaceViewModel.AtlasItem item, boolean activeSearchMatch) {
        body.clearAllChildren();
        // Search outline: hollow border framing the card when it matches
        // the active query. Painted before slotPreview so the icon draws
        // over its inner edge but the outline edges remain visible.
        if (activeSearchMatch) {
            addSearchMatchOutline(body);
        }
        body.addChild(slotPreview(item));
        addOverlaySignals(body, item);
    }

    /**
     * Card icon + shell. 32px cell:
     *   - 1px outer breathing room
     *   - shell rect (CARD_SHELL or CARD_SHELL_GHOST)
     *   - 1px inner inset for the inner backing (CARD_INNER / CARD_INNER_GHOST)
     *   - centered item icon
     */
    private UIElement slotPreview(SlotWorkspaceViewModel.AtlasItem item) {
        boolean carried = item.carried();
        int shellPx = ListWallPanelBuilder.CARD_CELL_PX - 2;
        int inset = 1;
        int iconSize = shellPx - inset * 2;

        int shellColor = carried ? CARD_SHELL : CARD_SHELL_GHOST;
        int innerColor = carried ? CARD_INNER : CARD_INNER_GHOST;

        UIElement wrapper = new UIElement().layout(layout -> layout
                .widthPercent(100)
                .heightPercent(100));
        wrapper.setAllowHitTest(false);

        UIElement shell = panel(shellColor).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(1)
                .top(1)
                .width(shellPx)
                .height(shellPx));
        shell.setAllowHitTest(false);
        shell.addChild(panel(innerColor).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(inset)
                .top(inset)
                .width(shellPx - inset * 2)
                .height(shellPx - inset * 2)));
        // Suppress vanilla count decoration — the card draws its own
        // status-aware M/N badge in addCarriedCountBadge.
        shell.addChild(itemIcon(item.displayStack(), iconSize, carried, false).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(inset)
                .top(inset)));
        wrapper.addChild(shell);
        return wrapper;
    }

    private void addOverlaySignals(UIElement body, SlotWorkspaceViewModel.AtlasItem item) {
        if (item.isCarriedContainer()) {
            addContainerFullnessBar(body, item);
        }
        AtlasCardStatus status = AtlasCardStatus.from(item);
        int proximateCount = proximateChestCount(item);
        if (proximateCount > 0) {
            addProximatePip(body, item, status, proximateCount);
        }
        addCarriedCountBadge(body, item, status);
        if (status.wantsBorder()) {
            addStatusBorder(body, status);
        }
        if (status.wantsProgressBar()) {
            addStatusProgressBar(body, status);
        }
        if (host.viewModel.depositableIdentities().contains(item.identity())) {
            addDepositPreviewOutline(body);
        }
        if (isGatherableItem(item)) {
            addGatherPreviewOutline(body);
        }
        if (!host.searchController.normalizedQuery().isBlank()) {
            int storedCount = proximateCount;
            for (SlotWorkspaceViewModel.ChestPresenceEntry entry : item.elsewhere()) {
                storedCount += entry.count();
            }
            if (storedCount > 0) {
                addAlsoStoredBadge(body, status, storedCount);
            }
        }
        if (RelevanceDebugOverlay.enabled()) {
            addRelevanceDebugBadge(body, item);
        }
    }

    private void addSearchMatchOutline(UIElement body) {
        int color = WARNING;
        int thickness = 1;
        outlineRect(body, color, thickness, 263);
    }

    private void addStatusBorder(UIElement body, AtlasCardStatus status) {
        outlineRect(body, status.color(), 1, 261);
    }

    /**
     * Hollow 4-rect border. zIndex pushes pose Z above the +232 item-icon
     * depth so the border survives the icon's depth-write.
     */
    private void outlineRect(UIElement body, int color, int thickness, int zIndex) {
        UIElement top = panel(color).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(0).top(0).widthPercent(100).height(thickness));
        top.style(style -> style.zIndex(zIndex));
        top.setAllowHitTest(false);
        body.addChild(top);
        UIElement bottom = panel(color).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(0).bottom(0).widthPercent(100).height(thickness));
        bottom.style(style -> style.zIndex(zIndex));
        bottom.setAllowHitTest(false);
        body.addChild(bottom);
        UIElement left = panel(color).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(0).top(0).width(thickness).heightPercent(100));
        left.style(style -> style.zIndex(zIndex));
        left.setAllowHitTest(false);
        body.addChild(left);
        UIElement right = panel(color).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .right(0).top(0).width(thickness).heightPercent(100));
        right.style(style -> style.zIndex(zIndex));
        right.setAllowHitTest(false);
        body.addChild(right);
    }

    private void addStatusProgressBar(UIElement body, AtlasCardStatus status) {
        int barHeight = 2;
        int border = status.wantsBorder() ? 1 : 0;
        int gap = status.wantsBorder() ? 1 : 0;
        int barBottom = border + gap;
        UIElement bar = new UIElement().layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(0).right(0).bottom(barBottom).height(barHeight));
        bar.style(style -> style.zIndex(262));
        bar.setAllowHitTest(false);

        int trackColor = status.level() == AtlasCardStatus.Level.FULFILLED
                ? 0x802A323D
                : status.color();
        UIElement track = panel(trackColor).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(0).top(0).widthPercent(100).heightPercent(100));
        track.style(style -> style.zIndex(262));
        track.setAllowHitTest(false);
        bar.addChild(track);

        float carriedFraction = status.carriedFraction();
        if (carriedFraction > 0f) {
            int fillColor = status.kitRelevant()
                    ? STATUS_FULFILLED_KIT
                    : STATUS_FULFILLED_PLAYER;
            UIElement fill = panel(fillColor).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(0).top(0).heightPercent(100)
                    .widthPercent(carriedFraction * 100f));
            fill.style(style -> style.zIndex(263));
            fill.setAllowHitTest(false);
            bar.addChild(fill);
        }
        if (status.level() == AtlasCardStatus.Level.MIXED && status.storedFraction() > 0f) {
            int storedColor = status.kitRelevant()
                    ? STATUS_STORED_KIT
                    : STATUS_STORED_PLAYER;
            UIElement stored = panel((storedColor & 0x00FFFFFF) | 0x80000000).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .leftPercent(carriedFraction * 100f)
                    .top(0).heightPercent(100)
                    .widthPercent(status.storedFraction() * 100f));
            stored.style(style -> style.zIndex(263));
            stored.setAllowHitTest(false);
            bar.addChild(stored);
        }
        body.addChild(bar);
    }

    /**
     * Bottom-right "M" or "M/N" badge replacing both the vanilla count and
     * the standalone desired-count pip. Kit-needed status modulates the
     * status colour rather than introducing a separate glyph.
     */
    private void addCarriedCountBadge(UIElement body, SlotWorkspaceViewModel.AtlasItem item, AtlasCardStatus status) {
        int carried = status.carriedCount();
        int desired = status.desiredCount();
        if (carried <= 0 && desired <= 0) {
            return;
        }
        String text;
        if (desired > 0) {
            text = WorkspaceFormat.compactCount(carried) + "/" + WorkspaceFormat.compactCount(desired);
        } else if (carried > 1) {
            text = WorkspaceFormat.compactCount(carried);
        } else {
            return;
        }
        int border = status.wantsBorder() ? 1 : 0;
        int chrome = border + (status.wantsBorder() ? 1 : 0) + (status.wantsProgressBar() ? 2 : 0);
        int sideInset = border;
        int bottomInset = chrome;
        int pipHeight = 5;
        int pipWidth = Math.max(pipHeight, text.length() * 3 + 2);
        int textColor = status.level() == AtlasCardStatus.Level.NEUTRAL ? TEXT : status.color();

        UIElement pip = panel(0xC8121B1F).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .right(sideInset)
                .bottom(bottomInset)
                .width(pipWidth)
                .height(pipHeight));
        pip.style(style -> style.zIndex(265));
        pip.setAllowHitTest(false);
        Label badge = label(text, textColor);
        badge.layout(layout -> layout.widthPercent(100).heightPercent(100));
        badge.setAllowHitTest(false);
        badge.textStyle(style -> style
                .textColor(textColor)
                .textShadow(false)
                .fontSize(5)
                .textAlignHorizontal(Horizontal.CENTER)
                .textAlignVertical(Vertical.CENTER));
        pip.addChild(badge);
        body.addChild(pip);
    }

    private void addProximatePip(
            UIElement body,
            SlotWorkspaceViewModel.AtlasItem item,
            AtlasCardStatus status,
            int proximateCount
    ) {
        int sideInset = status.wantsBorder() ? 1 : 0;
        int pipSize = 5;
        UIElement pip = panel(LINK_THREAD_COLOR).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .right(sideInset)
                .top(sideInset)
                .width(pipSize)
                .height(pipSize));
        pip.style(style -> style.zIndex(265));
        pip.setAllowHitTest(false);
        Label count = label(String.valueOf(Math.min(proximateCount, 999)), TEXT);
        count.layout(layout -> layout.widthPercent(100).heightPercent(100));
        count.setAllowHitTest(false);
        count.textStyle(style -> style
                .textColor(TEXT)
                .textShadow(false)
                .fontSize(5)
                .textAlignHorizontal(Horizontal.CENTER)
                .textAlignVertical(Vertical.CENTER));
        pip.addChild(count);
        body.addChild(pip);
    }

    private void addAlsoStoredBadge(UIElement body, AtlasCardStatus status, int storedCount) {
        int border = status.wantsBorder() ? 1 : 0;
        int chrome = border + (status.wantsBorder() ? 1 : 0) + (status.wantsProgressBar() ? 2 : 0);
        int sideInset = border;
        int bottomInset = chrome;
        int pipSize = 5;
        UIElement pip = panel(LINK_THREAD_COLOR).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(sideInset)
                .bottom(bottomInset)
                .width(pipSize)
                .height(pipSize));
        pip.style(style -> style.zIndex(265));
        pip.setAllowHitTest(false);
        Label count = label("+" + Math.min(storedCount, 999), TEXT);
        count.layout(layout -> layout.widthPercent(100).heightPercent(100));
        count.setAllowHitTest(false);
        count.textStyle(style -> style
                .textColor(TEXT)
                .textShadow(false)
                .fontSize(5)
                .textAlignHorizontal(Horizontal.CENTER)
                .textAlignVertical(Vertical.CENTER));
        pip.addChild(count);
        body.addChild(pip);
    }

    /**
     * Whether this item would be pulled by a click on the global
     * Gather button: the active kit needs it (carry-gap > 0), AND it's
     * present in a proximate chest. Mirrors the server-side gather
     * walk closely enough for a hover preview.
     */
    static boolean isGatherableItem(SlotWorkspaceViewModel.AtlasItem item) {
        return item != null && item.kitNeeded() && !item.presence().isEmpty();
    }

    /** Twin of {@link #addDepositPreviewOutline}, gated on
     *  {@link SlotWorkspaceUiController#gatherPreviewActive}. Uses the
     *  active-hotbar (kit) palette so the player learns one preview
     *  vocabulary: ACCENT for deposit, kit-color for gather. */
    private void addGatherPreviewOutline(UIElement body) {
        int thickness = 1;
        UIElement preview = new UIElement().layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(0).top(0).widthPercent(100).heightPercent(100));
        preview.style(style -> style.zIndex(264));
        preview.setAllowHitTest(false);
        preview.setVisible(host.gatherPreviewActive);
        boolean[] lastVisible = {host.gatherPreviewActive};
        preview.addEventListener(UIEvents.TICK, event -> {
            boolean now = host.gatherPreviewActive;
            if (now == lastVisible[0]) {
                return;
            }
            lastVisible[0] = now;
            preview.setVisible(now);
        });
        int color = ACTIVE_HOTBAR;
        UIElement top = panel(color).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(0).top(0).widthPercent(100).height(thickness));
        top.setAllowHitTest(false);
        preview.addChild(top);
        UIElement bottom = panel(color).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(0).bottom(0).widthPercent(100).height(thickness));
        bottom.setAllowHitTest(false);
        preview.addChild(bottom);
        UIElement left = panel(color).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(0).top(0).width(thickness).heightPercent(100));
        left.setAllowHitTest(false);
        preview.addChild(left);
        UIElement right = panel(color).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .right(0).top(0).width(thickness).heightPercent(100));
        right.setAllowHitTest(false);
        preview.addChild(right);
        body.addChild(preview);
    }

    private void addDepositPreviewOutline(UIElement body) {
        int thickness = 1;
        UIElement preview = new UIElement().layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(0).top(0).widthPercent(100).heightPercent(100));
        preview.style(style -> style.zIndex(264));
        preview.setAllowHitTest(false);
        preview.setVisible(host.depositPreviewActive);
        boolean[] lastVisible = {host.depositPreviewActive};
        preview.addEventListener(UIEvents.TICK, event -> {
            boolean now = host.depositPreviewActive;
            if (now == lastVisible[0]) {
                return;
            }
            lastVisible[0] = now;
            preview.setVisible(now);
        });
        int color = ACCENT;
        UIElement top = panel(color).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(0).top(0).widthPercent(100).height(thickness));
        top.setAllowHitTest(false);
        preview.addChild(top);
        UIElement bottom = panel(color).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(0).bottom(0).widthPercent(100).height(thickness));
        bottom.setAllowHitTest(false);
        preview.addChild(bottom);
        UIElement left = panel(color).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(0).top(0).width(thickness).heightPercent(100));
        left.setAllowHitTest(false);
        preview.addChild(left);
        UIElement right = panel(color).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .right(0).top(0).width(thickness).heightPercent(100));
        right.setAllowHitTest(false);
        preview.addChild(right);
        body.addChild(preview);
    }

    private void addContainerFullnessBar(UIElement body, SlotWorkspaceViewModel.AtlasItem item) {
        int trackWidth = ListWallPanelBuilder.CARD_CELL_PX - 4;
        int barHeight = 2;
        int capacity = Math.max(0, item.containerSlotCapacity());
        int free = Math.max(0, item.containerFreeSlotCount());
        int filled = Math.max(0, capacity - free);

        UIElement track = panel(CARRIED_CONTAINER_PIP & 0x66FFFFFF).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(2).top(2)
                .width(trackWidth).height(barHeight));
        track.style(style -> style.zIndex(260));
        track.setAllowHitTest(false);
        body.addChild(track);
        if (capacity > 0 && filled > 0) {
            float ratio = Math.min(1f, (float) filled / capacity);
            int fillWidth = Math.max(0, Math.round(trackWidth * ratio));
            int fillColor = WorkspaceFormat.fullnessColor(filled, capacity);
            UIElement fill = panel(fillColor).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(2).top(2)
                    .width(fillWidth).height(barHeight));
            fill.style(style -> style.zIndex(261));
            fill.setAllowHitTest(false);
            body.addChild(fill);
        }
    }

    private void addRelevanceDebugBadge(UIElement body, SlotWorkspaceViewModel.AtlasItem item) {
        String text = RelevanceDebugOverlay.formatScore(
                RelevanceDebugOverlay.scoreFor(item, host.viewModel, host.searchController.normalizedQuery())
        );
        int badgeWidth = 18;
        int badgeHeight = 6;
        UIElement badge = panel(0xCC0C141A).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(1).bottom(1)
                .width(badgeWidth).height(badgeHeight));
        badge.style(style -> style.zIndex(280));
        badge.setAllowHitTest(false);
        Label score = label(text, ACCENT);
        score.layout(layout -> layout.widthPercent(100).heightPercent(100));
        score.setAllowHitTest(false);
        score.textStyle(style -> style
                .textColor(ACCENT)
                .textShadow(false)
                .fontSize(5)
                .textAlignHorizontal(Horizontal.CENTER)
                .textAlignVertical(Vertical.CENTER));
        badge.addChild(score);
        body.addChild(badge);
    }

    /**
     * Standalone "small preview" tile used outside the wall (BeltPanelBuilder
     * carry indicator, etc.). Caller picks the size; this reproduces the
     * same shell + icon + optional marker dot the wall card uses, just at
     * a different scale.
     */
    UIElement slotPreview(SlotWorkspaceViewModel.AtlasItem item, int size, boolean showMarker) {
        int shell = size;
        int inset = 1;
        int icon = Math.max(10, size - 4);
        boolean carried = item.carried();
        int shellColor = carried ? CARD_SHELL : CARD_SHELL_GHOST;
        int innerColor = carried ? CARD_INNER : CARD_INNER_GHOST;
        UIElement shellElement = panel(shellColor).layout(layout -> layout.width(shell).height(shell));
        shellElement.setAllowHitTest(false);
        shellElement.addChild(panel(innerColor).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(inset)
                .top(inset)
                .width(shell - inset * 2)
                .height(shell - inset * 2)));
        shellElement.addChild(itemIcon(item.displayStack(), icon, carried).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(centeredWorld(shell, icon))
                .top(centeredWorld(shell, icon))));
        if (showMarker) {
            shellElement.addChild(panel(WorkspaceFormat.itemMarkerColor(item, host.viewModel.island(item.islandId()))).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .right(1f)
                    .top(1f)
                    .width(3f)
                    .height(3f)));
        }
        return shellElement;
    }

    int proximateChestCount(SlotWorkspaceViewModel.AtlasItem item) {
        if (item == null || item.presence().isEmpty()) {
            return 0;
        }
        int total = 0;
        for (SlotWorkspaceViewModel.ChestPresenceEntry entry : item.presence()) {
            total += entry.count();
        }
        return total;
    }

    /** Reference to a proximate chest holding the identity. The server
     *  resolves the actual slot index when servicing the take RPC. */
    record ChestSlotRef(String storageId, int chestSlotIndex) {
    }

    ChestSlotRef firstProximateChestSlotFor(SlotWorkspaceViewModel.AtlasItem item) {
        if (item == null || item.presence().isEmpty()) {
            return null;
        }
        return new ChestSlotRef(item.presence().get(0).storageId(), 0);
    }

    private void installCardClickHandlers(Button button, SlotWorkspaceViewModel.AtlasItem item) {
        button.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (handleCursorAtlasGesture(event, item)) {
                event.stopPropagation();
            }
        }, true);
        button.setOnClick(event -> {
            event.stopPropagation();
            if (Screen.hasShiftDown()) {
                SlotWorkspaceViewModel.AtlasItem fresh = host.viewModel.atlasItem(item.identity());
                SlotWorkspaceViewModel.AtlasItem target = fresh != null ? fresh : item;
                if (proximateChestCount(target) > 0) {
                    if (host.viewModel.carriedFreeSlotCount() <= 0 && !target.carried()) {
                        host.localStatus.set("carry full — drop something first");
                        host.rebuild();
                        return;
                    }
                    host.rpc.sendTakeStackByIdentity(target.identity());
                } else {
                    host.rpc.sendAssignHomeToFreeHotbar(target);
                }
                return;
            }
            host.selectedAtlasIdentity.set(item.identity());
            host.selectedHotbarIndex.set(-1);
            host.localStatus.set(item.playerPlaced()
                    ? "selected homed item: drag to hotbar or another section"
                    : "selected inbox item: drag to a section or create one");
        });
        button.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button == 1) {
                event.stopPropagation();
                host.menu.openContextMenuForAtlas(item, event.x, event.y);
            }
        });
        float[] scrollAccumulator = {0f};
        float[] desiredScrollAccumulator = {0f};
        button.addEventListener(UIEvents.MOUSE_WHEEL, event -> {
            if (Screen.hasControlDown()) {
                if (host.cursor.isCarrying()) {
                    return;
                }
                float dDelta = event.deltaY != 0f ? event.deltaY : event.deltaX;
                if (dDelta == 0f) {
                    return;
                }
                event.stopPropagation();
                desiredScrollAccumulator[0] += dDelta;
                int desiredDelta = (int) desiredScrollAccumulator[0];
                if (desiredDelta == 0) {
                    return;
                }
                desiredScrollAccumulator[0] -= desiredDelta;
                host.rpc.sendAdjustPlayerDesiredCount(item.identity(), desiredDelta);
                return;
            }
            if (!Screen.hasShiftDown()) {
                return;
            }
            if (host.cursor.isCarrying()) {
                return;
            }
            float delta = event.deltaY != 0f ? event.deltaY : event.deltaX;
            if (delta == 0f) {
                return;
            }
            event.stopPropagation();
            scrollAccumulator[0] += delta;
            int count = (int) scrollAccumulator[0];
            if (count == 0) {
                return;
            }
            scrollAccumulator[0] -= count;
            SlotWorkspaceViewModel.AtlasItem fresh = host.viewModel.atlasItem(item.identity());
            if (fresh == null) {
                return;
            }
            int magnitude = Math.abs(count);
            if (count > 0) {
                if (proximateChestCount(fresh) <= 0) {
                    host.localStatus.set("no nearby chest has " + fresh.name());
                    return;
                }
                if (host.viewModel.carriedFreeSlotCount() <= 0 && !fresh.carried()) {
                    host.localStatus.set("carry full — drop something first");
                    host.rebuild();
                    return;
                }
                for (int i = 0; i < magnitude; i++) {
                    host.rpc.sendTakeOneByIdentity(fresh.identity());
                }
            } else {
                if (!host.anyChestProximate()) {
                    host.localStatus.set("no nearby chest to push " + fresh.name());
                    return;
                }
                if (!fresh.carried()) {
                    host.localStatus.set(fresh.name() + " not carried");
                    return;
                }
                for (int i = 0; i < magnitude; i++) {
                    host.rpc.sendDepositOneHomeToLinkedChest(fresh);
                }
            }
        });
    }

    private boolean handleCursorAtlasGesture(
            com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent event,
            SlotWorkspaceViewModel.AtlasItem item
    ) {
        boolean carrying = host.cursor.isCarrying();
        WorkspaceCursorGestures.Result mode = WorkspaceCursorGestures.classify(event, carrying);
        if (mode == WorkspaceCursorGestures.Result.PICKUP_HALF) {
            if (!item.hasLargestCarriedSlot()) {
                host.localStatus.set(item.name() + " has no carried slot — pick up only works on items you carry");
                host.rebuild();
                return true;
            }
            boolean picked = host.cursor.pickupHalf(
                    item.largestCarriedSourceId(),
                    item.largestCarriedSlotIndex(),
                    item.identity(),
                    item.displayStack(),
                    item.largestCarriedSlotCount());
            if (picked) {
                host.localStatus.set("cursor: " + host.cursor.current().count() + " " + item.name());
            } else if (carrying) {
                host.localStatus.set("cursor already holds another item — drop or ESC first");
            }
            host.rebuild();
            return true;
        }
        if (carrying) {
            host.cursor.clear();
            host.localStatus.set("cursor cancelled");
            host.rebuild();
            return true;
        }
        return false;
    }
}
