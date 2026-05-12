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
import dev.imagio.slot.inventory.workspace.WayfindingTarget;
import dev.imagio.slot.neoforge.client.wayfinding.WayfindingTargetCache;
import dev.imagio.slot.ui.spi.SlotUiElement;
import dev.imagio.slot.ui.workspace.WallCardTransferGesturePolicy;
import dev.imagio.slot.ui.workspace.WallCardUiBuilder;
import dev.imagio.slot.ui.workspace.WorkspaceGatherUiSupport;
import dev.imagio.slot.ui.workspace.WorkspaceUiAttachments;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

/**
 * Single-LOD pixel-space card renderer for the sectioned list-view wall.
 * Replaces the prior 5-band LOD cascade. Cards render at a fixed
 * {@link ListWallPanelBuilder#CARD_CELL_PX} screen-pixel size; flex
 * layout in the parent grid handles wrap/positioning. No world-unit
 * math, no camera dependency.
 *
 * <p>Card chrome (status border + count badge + proximate pip + search
 * outline + deposit preview + container fullness + selection highlight
 * + carried/ghost tint) is preserved at the new fixed size; sized for
 * legibility at the chosen cell size.
 */
final class AtlasCardBuilder {
    /**
     * Width (px) of the inline wayfinding strip that grows on the right
     * of an expanded card. Sized to fit "↗ 12m" comfortably; cards
     * without wayfinding info stay at the standard square so the grid
     * keeps its rhythm. See Phase 5 of {@code single-column-workspace.md}.
     */
    private static final int WAYFINDING_STRIP_WIDTH_PX = WallCardUiBuilder.WAYFINDING_STRIP_WIDTH_PX;

    private final SlotWorkspaceUiController host;
    private final WallCardUiBuilder cardBuilder;
    private final LdlibSlotUiRenderer cardRenderer;

    AtlasCardBuilder(SlotWorkspaceUiController host) {
        this.host = host;
        this.cardBuilder = new WallCardUiBuilder(new WallCardContext());
        this.cardRenderer = new LdlibSlotUiRenderer(this::installCardInteractions);
    }

    /**
     * Build a single atlas card in pixel space. Caller drops the result
     * into a flex grid; ordering is invariant under reflow because the
     * grid emits cards in atlas-items order and flex-wrap reads them
     * left-to-right, top-to-bottom.
     */
    Button atlasCardButton(SlotWorkspaceViewModel.AtlasItem item) {
        dev.imagio.slot.SlotDebugLog.verboseLog(
                "[card] {} carried={} ghost={} totalCount={} proximate={} presence={} elsewhere={}",
                item.identity().itemId(), item.carried(), item.ghost(), item.totalCount(),
                item.proximateCount(), item.presence().size(), item.elsewhere().size());
        UIElement element = cardRenderer.render(cardBuilder.card(item));
        if (element instanceof Button button) {
            return button;
        }
        throw new IllegalStateException("wall card renderer returned " + element.getClass().getName());
    }

    private void installCardInteractions(SlotUiElement model, UIElement element) {
        if (model.hasAttachment(WorkspaceUiAttachments.WALL_CARD_BODY)) {
            SlotWorkspaceViewModel.AtlasItem item = model.attachment(
                    WorkspaceUiAttachments.ATLAS_ITEM,
                    SlotWorkspaceViewModel.AtlasItem.class
            );
            if (item == null) {
                return;
            }
            Boolean activeSearchMatch = model.attachment(
                    WorkspaceUiAttachments.WALL_CARD_ACTIVE_SEARCH_MATCH,
                    Boolean.class
            );
            SlotWorkspaceViewModel.ChestPresenceEntry wayfindingEntry = model.attachment(
                    WorkspaceUiAttachments.WALL_CARD_WAYFINDING_ENTRY,
                    SlotWorkspaceViewModel.ChestPresenceEntry.class
            );
            if (wayfindingEntry != null) {
                element.clearAllChildren();
                UIElement iconCell = new UIElement().layout(layout -> layout
                        .positionType(TaffyPosition.ABSOLUTE)
                        .left(0).top(0)
                        .width(ListWallPanelBuilder.CARD_CELL_PX)
                        .height(ListWallPanelBuilder.CARD_CELL_PX));
                iconCell.setAllowHitTest(false);
                buildCardBody(iconCell, item, Boolean.TRUE.equals(activeSearchMatch));
                element.addChild(iconCell);
                element.addChild(buildWayfindingStrip(wayfindingEntry));
            } else {
                buildCardBody(element, item, Boolean.TRUE.equals(activeSearchMatch));
            }
            return;
        }
        if (!model.hasAttachment(WorkspaceUiAttachments.WALL_CARD)) {
            return;
        }
        SlotWorkspaceViewModel.AtlasItem item = model.attachment(
                WorkspaceUiAttachments.ATLAS_ITEM,
                SlotWorkspaceViewModel.AtlasItem.class
        );
        if (item == null || !(element instanceof Button button)) {
            return;
        }
        installCardClickHandlers(button, item);
        host.drag.installAtlasHoverTooltip(button, item);
        if (!host.goalTabActive()) {
            host.drag.installAtlasItemDragSource(button, item);
        }
        installChestHoverPaint(button, item);
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
        if (!host.goalTabActive() && host.viewModel.depositableIdentities().contains(item.identity())) {
            addDepositPreviewOutline(body);
        }
        if (!host.goalTabActive() && WorkspaceGatherUiSupport.isGatherableItem(item)) {
            addGatherPreviewOutline(body);
        }
        if (host.goalChoiceInvolved(item)) {
            addChoiceIndicator(body, item);
        }
        if (!host.searchController.normalizedQuery().isBlank()) {
            int storedCount = proximateCount;
            for (SlotWorkspaceViewModel.ChestPresenceEntry entry : item.elsewhere()) {
                storedCount += entry.count();
            }
            if (storedCount > 0) {
                addSearchStoredBadge(body, status, storedCount);
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

    /**
     * Bottom-right "M" or "M/N" badge replacing both the vanilla count and
     * the standalone desired-count pip. Kit-needed status modulates the
     * status colour rather than introducing a separate glyph.
     */
    private void addCarriedCountBadge(UIElement body, SlotWorkspaceViewModel.AtlasItem item, AtlasCardStatus status) {
        int carried = status.carriedCount();
        int target = status.targetCount();
        if (carried <= 0 && target <= 0) {
            return;
        }
        String text;
        if (target > 0) {
            text = WorkspaceFormat.compactCount(carried) + "/" + WorkspaceFormat.compactCount(target);
        } else if (carried > 0) {
            text = WorkspaceFormat.compactCount(carried);
        } else {
            return;
        }
        int border = status.wantsBorder() ? 1 : 0;
        int chrome = border + (status.wantsBorder() ? 1 : 0);
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

    private void addSearchStoredBadge(UIElement body, AtlasCardStatus status, int storedCount) {
        int sideInset = status.wantsBorder() ? 1 : 0;
        String text = "+" + WorkspaceFormat.compactCount(storedCount);
        int pipHeight = 5;
        int pipWidth = Math.max(pipHeight, text.length() * 3 + 2);
        UIElement pip = panel(LINK_THREAD_COLOR).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(sideInset)
                .top(sideInset)
                .width(pipWidth)
                .height(pipHeight));
        pip.style(style -> style.zIndex(265));
        pip.setAllowHitTest(false);
        Label count = label(text, TEXT);
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

    private void addChoiceIndicator(UIElement body, SlotWorkspaceViewModel.AtlasItem item) {
        int color = 0xE00B1117;
        int markColor = host.goalChoiceCard(item) ? 0xFFFFD27A : 0xFFE7D9FF;
        UIElement pip = panel(color).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(6)
                .top(0)
                .width(6)
                .height(6));
        pip.style(style -> style.zIndex(270));
        pip.setAllowHitTest(false);
        Label mark = label("?", markColor);
        mark.layout(layout -> layout.widthPercent(100).heightPercent(100));
        mark.setAllowHitTest(false);
        mark.textStyle(style -> style
                .textColor(markColor)
                .textShadow(false)
                .fontSize(6)
                .textAlignHorizontal(Horizontal.CENTER)
                .textAlignVertical(Vertical.CENTER));
        pip.addChild(mark);
        body.addChild(pip);
    }

    private UIElement buildWayfindingStrip(SlotWorkspaceViewModel.ChestPresenceEntry entry) {
        UIElement strip = new UIElement().layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(ListWallPanelBuilder.CARD_CELL_PX)
                .top(0)
                .width(WAYFINDING_STRIP_WIDTH_PX)
                .height(ListWallPanelBuilder.CARD_CELL_PX)
                .paddingHorizontal(2)
                .gapAll(2)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.ROW));
        strip.setAllowHitTest(false);

        Label arrow = label("·", ACCENT);
        arrow.layout(layout -> layout.height(ListWallPanelBuilder.CARD_CELL_PX));
        arrow.textStyle(style -> style
                .textColor(ACCENT)
                .textShadow(false)
                .fontSize(7)
                .adaptiveWidth(true)
                .textAlignVertical(Vertical.CENTER));
        arrow.setAllowHitTest(false);
        strip.addChild(arrow);

        Label distance = label("--m", MUTED);
        distance.layout(layout -> layout.flex(1).height(ListWallPanelBuilder.CARD_CELL_PX));
        distance.textStyle(style -> style
                .textColor(MUTED)
                .textShadow(false)
                .fontSize(6)
                .textAlignHorizontal(Horizontal.RIGHT)
                .textAlignVertical(Vertical.CENTER));
        distance.setAllowHitTest(false);
        strip.addChild(distance);

        strip.addEventListener(UIEvents.TICK, ignored ->
                updateWayfindingStrip(arrow, distance, entry.storageId()));
        return strip;
    }

    private void updateWayfindingStrip(Label arrow, Label distance, String storageId) {
        // wayfindingTargets() only carries chests with kit/desired-gap
        // missing identities; for plain search-only matches we fall back
        // to chestChips, which has every claimed chest's coords.
        String dimensionId = null;
        int worldX = 0, worldY = 0, worldZ = 0;
        WayfindingTarget target = WayfindingTargetCache.targetFor(storageId);
        if (target != null) {
            dimensionId = target.dimensionId();
            worldX = target.worldX();
            worldY = target.worldY();
            worldZ = target.worldZ();
        } else {
            for (SlotWorkspaceViewModel.ChestChip chip : host.viewModel.chestChips()) {
                if (storageId.equals(chip.storageId())) {
                    dimensionId = chip.dimensionId();
                    worldX = chip.worldX();
                    worldY = chip.worldY();
                    worldZ = chip.worldZ();
                    break;
                }
            }
        }
        if (dimensionId == null) {
            arrow.setText(Component.literal("·"));
            distance.setText(Component.literal("--m"));
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc == null ? null : mc.player;
        if (player == null || mc.level == null) {
            arrow.setText(Component.literal("·"));
            distance.setText(Component.literal("--m"));
            return;
        }
        if (!mc.level.dimension().location().toString().equals(dimensionId)) {
            arrow.setText(Component.literal(shortDimension(dimensionId)));
            distance.setText(Component.literal(""));
            return;
        }
        double dx = (worldX + 0.5) - player.getX();
        double dz = (worldZ + 0.5) - player.getZ();
        double dy = (worldY + 0.5) - player.getY();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        double dist = Math.sqrt(horizontal * horizontal + dy * dy);
        float yawRadians = (float) Math.toRadians(player.getYRot());
        double absoluteBearing = Math.atan2(-dx, dz);
        double relativeBearing = absoluteBearing - yawRadians;
        arrow.setText(Component.literal(arrowGlyph(relativeBearing)));
        distance.setText(Component.literal(((int) Math.round(dist)) + "m"));
    }

    private static String arrowGlyph(double relativeBearing) {
        double normalized = ((relativeBearing % (Math.PI * 2)) + Math.PI * 2) % (Math.PI * 2);
        int sector = (int) Math.floor((normalized + Math.PI / 8.0) / (Math.PI / 4.0)) % 8;
        return switch (sector) {
            case 0 -> "↑";
            case 1 -> "↗";
            case 2 -> "→";
            case 3 -> "↘";
            case 4 -> "↓";
            case 5 -> "↙";
            case 6 -> "←";
            case 7 -> "↖";
            default -> "·";
        };
    }

    private static String shortDimension(String dimensionId) {
        if (dimensionId == null) {
            return "";
        }
        int colon = dimensionId.indexOf(':');
        String tail = colon < 0 ? dimensionId : dimensionId.substring(colon + 1);
        if (tail.startsWith("the_")) {
            tail = tail.substring(4);
        }
        return tail;
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
        return WallCardTransferGesturePolicy.proximateChestCount(item);
    }

    private void installCardClickHandlers(Button button, SlotWorkspaceViewModel.AtlasItem item) {
        // Capture-phase MOUSE_DOWN handles right-click cursor gestures.
        // Plain left-click pickup runs on UIEvents.CLICK (below), which
        // fires on mouseReleased only when no drag started.
        button.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (host.goalTabActive()) {
                event.stopPropagation();
                if (event.button == 1 && !WorkspaceCursorState.isCarrying()) {
                    host.menu.openContextMenuForAtlas(item, event.x, event.y);
                } else if (WorkspaceCursorState.isCarrying()) {
                    host.localStatus.set("goal tab is browse only");
                    host.rebuild();
                }
                return;
            }
            WallCardTransferGesturePolicy.Decision decision = WallCardTransferGesturePolicy.pointerDown(
                    cardGestureContext(item, event.button, Screen.hasShiftDown(), Screen.hasControlDown()));
            if (dispatchCardGestureDecision(item, decision)) {
                event.stopPropagation();
            }
        }, true);
        // Use UIEvents.CLICK rather than Button#setOnClick because the
        // latter fires on MOUSE_DOWN — that picks up to cursor before a
        // drag can start, which kills drag-to-rehome. CLICK fires on
        // mouseReleased only when the release element matches the
        // mousedown element (i.e. no drag), so quick clicks still
        // trigger pickup but a press-drag-release goes through the
        // drag pipeline instead.
        button.addEventListener(UIEvents.CLICK, event -> {
            if (event.button != 0) {
                return;
            }
            event.stopPropagation();
            if (host.goalTabActive()) {
                host.localStatus.set(item.name() + " in active goal");
                host.rebuild();
                return;
            }
            SlotWorkspaceViewModel.AtlasItem target = freshItem(item);
            WallCardTransferGesturePolicy.Decision decision = WallCardTransferGesturePolicy.click(
                    cardGestureContext(target, event.button, Screen.hasShiftDown(), Screen.hasControlDown()));
            if (dispatchCardGestureDecision(target, decision)) {
                return;
            }
        });
        button.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (host.goalTabActive()) {
                return;
            }
            if (event.button == 1) {
                if (WorkspaceCursorState.isCarrying()) {
                    return;
                }
                event.stopPropagation();
                host.menu.openContextMenuForAtlas(item, event.x, event.y);
            }
        });
        float[] scrollAccumulator = {0f};
        float[] desiredScrollAccumulator = {0f};
        button.addEventListener(UIEvents.MOUSE_WHEEL, event -> {
            boolean wantedAdjustDown = dev.imagio.slot.neoforge.client.input.SlotAtlasKeyMappings.markWantedDown();
            if (host.goalTabActive()) {
                if (!Screen.hasControlDown() || WorkspaceCursorState.isCarrying()) {
                    return;
                }
                float delta = event.deltaY != 0f ? event.deltaY : event.deltaX;
                if (delta == 0f) {
                    return;
                }
                event.stopPropagation();
                host.adjustGoalTargetCount("", delta > 0f ? 1 : -1);
                return;
            }
            if (Screen.hasControlDown()) {
                if (WorkspaceCursorState.isCarrying()) {
                    return;
                }
            } else if (!wantedAdjustDown && (!Screen.hasShiftDown() || WorkspaceCursorState.isCarrying())) {
                return;
            }
            float delta = event.deltaY != 0f ? event.deltaY : event.deltaX;
            if (delta == 0f) {
                return;
            }
            event.stopPropagation();
            float[] accumulator = (Screen.hasControlDown() || wantedAdjustDown)
                    ? desiredScrollAccumulator
                    : scrollAccumulator;
            accumulator[0] += delta;
            int steps = (int) accumulator[0];
            if (steps == 0) {
                return;
            }
            accumulator[0] -= steps;
            SlotWorkspaceViewModel.AtlasItem target = freshItem(item);
            WallCardTransferGesturePolicy.Decision decision = WallCardTransferGesturePolicy.wheel(
                    cardGestureContext(target, 0, Screen.hasShiftDown(), Screen.hasControlDown(), wantedAdjustDown),
                    steps);
            dispatchCardGestureDecision(target, decision);
        });
    }

    private SlotWorkspaceViewModel.AtlasItem freshItem(SlotWorkspaceViewModel.AtlasItem item) {
        if (item == null || host.viewModel == null) {
            return item;
        }
        SlotWorkspaceViewModel.AtlasItem fresh = host.currentAtlasItem(item.identity());
        return fresh != null ? fresh : item;
    }

    private WallCardTransferGesturePolicy.Context cardGestureContext(
            SlotWorkspaceViewModel.AtlasItem item,
            int button,
            boolean shiftDown,
            boolean controlDown
    ) {
        return cardGestureContext(item, button, shiftDown, controlDown, false);
    }

    private WallCardTransferGesturePolicy.Context cardGestureContext(
            SlotWorkspaceViewModel.AtlasItem item,
            int button,
            boolean shiftDown,
            boolean controlDown,
            boolean wantedAdjustDown
    ) {
        int freeSlots = host.viewModel == null ? 0 : host.viewModel.carriedFreeSlotCount();
        return new WallCardTransferGesturePolicy.Context(
                item,
                button,
                shiftDown,
                controlDown,
                WorkspaceCursorState.carriedIdentity(),
                WorkspaceCursorState.isCarrying(),
                SlotSidebarClientUi.isActive(),
                freeSlots,
                host.anyChestProximate(),
                wantedAdjustDown,
                host.shiftClickTransferState.continuingTake(
                        item == null ? null : item.identity(),
                        shiftDown));
    }

    private boolean dispatchCardGestureDecision(
            SlotWorkspaceViewModel.AtlasItem item,
            WallCardTransferGesturePolicy.Decision decision
    ) {
        if (decision == null || !decision.handled()) {
            return false;
        }
        int count = decision.count();
        switch (decision.action()) {
            case NONE -> {
                return false;
            }
            case STATUS -> {
                host.localStatus.set(decision.status());
                host.rebuild();
            }
            case PICKUP_TO_CURSOR -> host.rpc.sendPickupToCursor(
                    item.identity(),
                    count <= 0 ? WallCardTransferGesturePolicy.PICKUP_MAX : count);
            case CURSOR_CANCEL -> host.rpc.sendCursorCancel();
            case CURSOR_SMART_DEPOSIT -> host.rpc.sendCursorSmartDeposit();
            case CURSOR_CANCEL_THEN_PICKUP_TO_CURSOR -> {
                host.rpc.sendCursorCancel();
                host.rpc.sendPickupToCursor(
                        item.identity(),
                        count <= 0 ? WallCardTransferGesturePolicy.PICKUP_MAX : count);
            }
            case TAKE_DESIRED_GAP_OR_STACK_BY_IDENTITY -> host.rpc.sendTakeDesiredGapOrStackByIdentity(item.identity());
            case TAKE_STACK_BY_IDENTITY -> host.rpc.sendTakeStackByIdentity(item.identity());
            case TAKE_ONE_BY_IDENTITY -> repeat(count, () -> host.rpc.sendTakeOneByIdentity(item.identity()));
            case DEPOSIT_HOME_TO_LINKED_CHEST -> host.rpc.sendDepositHomeToLinkedChest(item);
            case DEPOSIT_ONE_HOME_TO_LINKED_CHEST -> repeat(count, () -> host.rpc.sendDepositOneHomeToLinkedChest(item));
            case CROSS_SURFACE_QUICK_MOVE -> host.rpc.sendCrossSurfaceQuickMove(item.identity(), count);
            case ADJUST_PLAYER_DESIRED_COUNT -> host.rpc.sendAdjustPlayerDesiredCount(item.identity(), count);
            case ADJUST_WANTED_COUNT -> host.rpc.sendAdjustWantedCount(item.identity(), count);
        }
        host.shiftClickTransferState.record(decision, item == null ? null : item.identity(), Screen.hasShiftDown());
        return true;
    }

    private void repeat(int count, Runnable action) {
        int safeCount = Math.max(0, count);
        for (int i = 0; i < safeCount; i++) {
            action.run();
        }
    }

    private final class WallCardContext implements WallCardUiBuilder.Context {
        @Override
        public SlotWorkspaceViewModel.IdentityRef activeIdentity() {
            return host.activeIdentity();
        }

        @Override
        public String normalizedSearchQuery() {
            return host.searchController.normalizedQuery();
        }

        @Override
        public boolean matchesItem(SlotWorkspaceViewModel.AtlasItem item) {
            return host.searchController.matchesItem(item);
        }

        @Override
        public boolean isMapFocusItem(SlotWorkspaceViewModel.AtlasItem item) {
            return host.isMapFocusItem(item);
        }

        @Override
        public void hoverAtlasIdentity(SlotWorkspaceViewModel.IdentityRef identity) {
            host.hoveredAtlasIdentity = identity;
        }

        @Override
        public void clearHoveredAtlasIdentity(SlotWorkspaceViewModel.IdentityRef identity) {
            if (identity != null && identity.equals(host.hoveredAtlasIdentity)) {
                host.hoveredAtlasIdentity = null;
            }
        }

        @Override
        public java.util.List<Component> tooltipLines(SlotWorkspaceViewModel.AtlasItem item) {
            return host.goalTooltipLines(item);
        }

        @Override
        public boolean choiceInvolved(SlotWorkspaceViewModel.AtlasItem item) {
            return host.goalChoiceInvolved(item);
        }

        @Override
        public boolean choiceCard(SlotWorkspaceViewModel.AtlasItem item) {
            return host.goalChoiceCard(item);
        }
    }

}
