package dev.imagio.slot.neoforge.screen.ldlib;

import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.*;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceUi.*;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.neoforge.config.SlotClientConfig;
import dev.imagio.slot.ui.action.WorkspaceActionId;
import dev.imagio.slot.ui.spi.SlotUiElement;
import dev.imagio.slot.ui.workspace.StorageGhostRevealMode;
import dev.imagio.slot.ui.workspace.WayfindingDisplay;
import dev.imagio.slot.ui.workspace.WallCardTransferGesturePolicy;
import dev.imagio.slot.ui.workspace.WallCardUiBuilder;
import dev.imagio.slot.ui.workspace.WorkspaceCountFormat;
import dev.imagio.slot.ui.workspace.WorkspaceGatherUiSupport;
import dev.imagio.slot.ui.workspace.WorkspaceItemTooltipBuilder;
import dev.imagio.slot.ui.workspace.WorkspaceUiAttachments;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Single-LOD pixel-space card renderer for the sectioned list-view wall.
 * Replaces the prior 5-band LOD cascade. Cards render at a fixed
 * {@link ListWallPanelBuilder#CARD_CELL_PX} screen-pixel size; flex
 * layout in the parent grid handles wrap/positioning. No world-unit
 * math, no camera dependency.
 *
 * <p>Card chrome is rendered by the common {@link WallCardUiBuilder}
 * so Forge and NeoForge share the same badges, wayfinding strip, and
 * status ring. This adapter only supplies platform context and
 * interaction wiring.
 */
final class AtlasCardBuilder {
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
        return atlasCardButton(item, false);
    }

    Button atlasCardButton(SlotWorkspaceViewModel.AtlasItem item, boolean forceWayfindingStrip) {
        return atlasCardButton(item, forceWayfindingStrip, null);
    }

    Button atlasCardButton(
            SlotWorkspaceViewModel.AtlasItem item,
            boolean forceWayfindingStrip,
            SlotWorkspaceViewModel.ContextualSuggestionLane suggestionLane
    ) {
        dev.imagio.slot.SlotDebugLog.verboseLog(
                "[card] {} carried={} ghost={} totalCount={} proximate={} presence={} elsewhere={}",
                item.identity().itemId(), item.carried(), item.ghost(), item.totalCount(),
                item.proximateCount(), item.presence().size(), item.elsewhere().size());
        WallCardUiBuilder builder = forceWayfindingStrip || suggestionLane != null
                ? new WallCardUiBuilder(new WallCardContext(forceWayfindingStrip, suggestionLane))
                : cardBuilder;
        UIElement element = cardRenderer.render(builder.card(item));
        if (element instanceof Button button) {
            return button;
        }
        throw new IllegalStateException("wall card renderer returned " + element.getClass().getName());
    }

    void installCardInteractions(SlotUiElement model, UIElement element) {
        if (model.hasAttachment(WorkspaceUiAttachments.WALL_CARD_BODY)) {
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
        SlotWorkspaceViewModel.ContextualSuggestionLane suggestionLane = model.attachment(
                WorkspaceUiAttachments.CONTEXTUAL_SUGGESTION_LANE,
                SlotWorkspaceViewModel.ContextualSuggestionLane.class);
        host.drag.installAtlasHoverTooltip(button, item, suggestionLane);
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

    boolean hasProximateDepositRoute(SlotWorkspaceViewModel.AtlasItem item) {
        return item != null
                && !host.goalTabActive()
                && host.viewModel.depositableIdentities().contains(item.identity());
    }

    private void installCardClickHandlers(Button button, SlotWorkspaceViewModel.AtlasItem item) {
        // Capture-phase MOUSE_DOWN handles right-click cursor gestures.
        // Plain left-click pickup runs on UIEvents.CLICK (below), which
        // fires on mouseReleased only when no drag started.
        button.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (host.goalTabActive()) {
                event.stopPropagation();
                if (event.button == 1 && Screen.hasShiftDown()) {
                    host.localStatus.set("goal tab is browse only");
                    host.rebuild();
                } else if (event.button == 1 && !WorkspaceCursorState.isCarrying()) {
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
            if (host.recipeSidebarActive() && wantedAdjustDown) {
                host.rpc.sendSetWantedCount(target.identity(), recipeWantedTargetCount(target, steps));
                return;
            }
            WallCardTransferGesturePolicy.Decision decision = WallCardTransferGesturePolicy.wheel(
                    cardGestureContext(target, 0, Screen.hasShiftDown(), Screen.hasControlDown(), wantedAdjustDown),
                    steps);
            dispatchCardGestureDecision(target, decision);
        });
    }

    private int recipeWantedTargetCount(SlotWorkspaceViewModel.AtlasItem item, int delta) {
        if (item == null) {
            return Math.max(0, delta);
        }
        int base = item.wantedCount() > 0 ? item.wantedCount() : item.desiredCount();
        if (base <= 0) {
            base = item.carried() ? item.totalCount() : 0;
        }
        return Math.max(0, base + delta);
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
                host.activeChestOpen(),
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
            case TAKE_ITEMS_BY_IDENTITY -> host.enqueueWheelTransfer(
                    WorkspaceActionId.TAKE_ITEMS_BY_IDENTITY,
                    item.identity(),
                    count,
                    "taking " + item.name());
            case DEPOSIT_HOME_TO_LINKED_CHEST -> host.rpc.sendDepositHomeToLinkedChest(item);
            case DEPOSIT_ITEMS_HOME_TO_LINKED_CHEST -> host.enqueueWheelTransfer(
                    WorkspaceActionId.DEPOSIT_ITEMS_HOME_TO_LINKED_CHEST,
                    item.identity(),
                    count,
                    "depositing " + item.name());
            case CROSS_SURFACE_QUICK_MOVE -> host.rpc.sendCrossSurfaceQuickMove(item.identity(), count);
            case ADJUST_PLAYER_DESIRED_COUNT -> host.rpc.sendAdjustPlayerDesiredCount(item.identity(), count);
            case ADJUST_WANTED_COUNT -> host.rpc.sendAdjustWantedCount(item.identity(), count);
        }
        host.shiftClickTransferState.record(decision, item == null ? null : item.identity(), Screen.hasShiftDown());
        return true;
    }

    private final class WallCardContext implements WallCardUiBuilder.Context {
        private final boolean forceWayfindingStrip;
        private final SlotWorkspaceViewModel.ContextualSuggestionLane suggestionLane;

        private WallCardContext() {
            this(false, null);
        }

        private WallCardContext(boolean forceWayfindingStrip) {
            this(forceWayfindingStrip, null);
        }

        private WallCardContext(
                boolean forceWayfindingStrip,
                SlotWorkspaceViewModel.ContextualSuggestionLane suggestionLane
        ) {
            this.forceWayfindingStrip = forceWayfindingStrip;
            this.suggestionLane = suggestionLane;
        }

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
        public WayfindingDisplay.CardText wayfindingText(SlotWorkspaceViewModel.ChestPresenceEntry entry) {
            if (entry == null) {
                return WayfindingDisplay.CardText.unavailable();
            }
            if (entry.storageId() != null && entry.storageId().startsWith("goal:")) {
                return new WayfindingDisplay.CardText(">", WorkspaceCountFormat.compact(entry.count()));
            }
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null || minecraft.level == null || host.viewModel == null) {
                return WayfindingDisplay.CardText.unavailable();
            }
            return WayfindingDisplay.forStorage(
                    entry.storageId(),
                    host.viewModel.wayfindingTargets(),
                    host.viewModel.chestChips(),
                    minecraft.level.dimension().location().toString(),
                    minecraft.player.getX(),
                    minecraft.player.getY(),
                    minecraft.player.getZ(),
                    minecraft.player.getYRot());
        }

        @Override
        public java.util.List<Component> tooltipLines(SlotWorkspaceViewModel.AtlasItem item) {
            if (host.recipeSidebarActive()) {
                return host.recipeTooltipLines(item);
            }
            if (suggestionLane != null && SlotClientConfig.CLIENT.contextualSuggestionDebugTooltips.get()) {
                return WorkspaceItemTooltipBuilder.slotLines(
                        item,
                        suggestionLane,
                        true,
                        hasProximateDepositRoute(item));
            }
            return host.goalTabActive()
                    ? host.goalTooltipLines(item)
                    : WorkspaceItemTooltipBuilder.slotLines(item, hasProximateDepositRoute(item));
        }

        @Override
        public boolean choiceInvolved(SlotWorkspaceViewModel.AtlasItem item) {
            return host.goalChoiceInvolved(item);
        }

        @Override
        public boolean choiceCard(SlotWorkspaceViewModel.AtlasItem item) {
            return host.goalChoiceCard(item);
        }

        @Override
        public boolean suppressVanillaTooltip(SlotWorkspaceViewModel.AtlasItem item) {
            return host.recipeSidebarActive()
                    ? host.recipeSuppressVanillaTooltip(item)
                    : host.goalSuppressVanillaTooltip(item);
        }

        @Override
        public StorageGhostRevealMode storageGhostRevealMode() {
            return host.storageGhostRevealMode;
        }

        @Override
        public boolean forceWayfindingStrip(SlotWorkspaceViewModel.AtlasItem item) {
            return forceWayfindingStrip;
        }

        @Override
        public boolean hasProximateDepositRoute(SlotWorkspaceViewModel.AtlasItem item) {
            return AtlasCardBuilder.this.hasProximateDepositRoute(item);
        }

        @Override
        public boolean depositPreviewActive() {
            return host.depositPreviewActive;
        }

        @Override
        public boolean gatherPreviewActive() {
            return host.gatherPreviewActive;
        }

        @Override
        public boolean gatherPreviewEligible(SlotWorkspaceViewModel.AtlasItem item) {
            return !host.goalTabActive() && WorkspaceGatherUiSupport.isGatherableItem(item);
        }

        @Override
        public SlotWorkspaceViewModel.ContextualSuggestionLane contextualSuggestionLane() {
            return suggestionLane;
        }
    }

}
