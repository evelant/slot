package dev.imagio.slot.neoforge.screen.ldlib;

import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceFormat.*;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.*;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceUi.*;

import dev.imagio.slot.SlotDebugLog;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.BindableValue;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.event.HoverTooltips;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import dev.imagio.slot.neoforge.screen.ldlib.util.Observable;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.triage.ChipSuggestion;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceAtlasLayout;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.ui.workspace.RecipeIngredientSidebarSpec;
import dev.imagio.slot.ui.workspace.RecipeViewerIntegration;
import dev.imagio.slot.ui.workspace.ShiftClickTransferState;
import dev.imagio.slot.ui.workspace.StorageGhostRevealMode;
import dev.imagio.slot.ui.workspace.WheelTransferBatcher;
import dev.imagio.slot.ui.workspace.WorkspaceUiSessionMemory;
import dev.imagio.slot.ui.action.WorkspaceActionId;
import dev.imagio.slot.workflow.domain.CraftRunRecipeCapture;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class SlotWorkspaceUiController {
    /**
     * Natural content width (in screen px) of the workspace UI tree —
     * wall scroller (fixed card-density) + TOC sliver + flex gap +
     * root padding on each side. Single source of truth so the sidebar
     * and standalone surfaces render the same widget tree at the same
     * width. Phase 7 of the single-column workspace plan dropped the
     * left column entirely; the sliver carries TOC navigation in ~6 px.
     */
    static final int WORKSPACE_BASE_WIDTH_PX =
            ListWallPanelBuilder.WALL_CONTENT_WIDTH_PX
                    + ListWallPanelBuilder.SECTION_GAP_PX
                    + TocPanelBuilder.SLIVER_WIDTH_PX
                    + 14 * 2;
    static final int WORKSPACE_WIDTH_PX =
            WORKSPACE_BASE_WIDTH_PX
                    + ListWallPanelBuilder.SECTION_GAP_PX
                    + KitRackBuilder.KIT_RACK_PANEL_WIDTH;

    final SlotWorkspaceUiSession session;
    final Player player;
    final boolean sidebarMode;
    final UIElement root;
    final UIElement content;
    final UIElement sidebarFrame;
    final UIElement recentsPanelSlot;
    final UIElement taskPanelSlot;
    UIElement popoverSlot;

    SlotWorkspaceViewModel viewModel;
    final Observable<String> localStatus = new Observable<>("");
    /**
     * Identity that was on the menu cursor right before the most recent
     * drop / cancel RPC. Drives the wall card's "active" chrome after
     * the cursor goes empty so the player can still see "this is what I
     * just dropped." Cleared when a fresh pickup reassigns the cursor.
     */
    SlotWorkspaceViewModel.IdentityRef lastDroppedIdentity;
    SlotWorkspaceViewModel.IdentityRef hoveredAtlasIdentity;
    /**
     * Cross-surface hover cursor for storage chest cards: hovering a
     * chest card sets this; island elements observe it to highlight
     * their linked-from counterparts.
     */
    String hoveredStorageId;
    int hoveredHotbarIndex = -1;
    final List<Observable.Subscription> wallContentSubscriptions = new ArrayList<>();
    final Map<Integer, UIElement> hotbarSlotElements = new HashMap<>();
    SlotWorkspaceViewModel.IdentityRef contextMenuAtlasIdentity;
    SlotWorkspaceViewModel.AtlasItem contextMenuAtlasItemSnapshot;
    int contextMenuHotbarIndex = -1;
    String contextMenuKitId;
    String contextMenuChestStorageId;
    /** Identity currently being edited via "Set desired count..." in the
     * atlas context menu. Non-null swaps the menu body to a numeric
     * TextField + Set/Clear actions for that identity. */
    SlotWorkspaceViewModel.IdentityRef editingDesiredCountIdentity;
    String desiredCountDraft = "";
    String renamingKitId;
    String renameKitDraft = "";
    String confirmDeleteKitId;
    String renamingChestStorageId;
    String renameChestDraft = "";
    float contextMenuScreenX;
    float contextMenuScreenY;
    boolean helpPopoverOpen;
    float helpPopoverScreenX;
    float helpPopoverScreenY;
    final ArrayDeque<String> recentRehomeIslandIds = new ArrayDeque<>();
    private RecipeIngredientSidebarSpec recipeSidebarSpec = RecipeIngredientSidebarSpec.empty();
    private RecipeIngredientSidebarSpec.Projection recipeSidebarProjection;
    private long recipeSidebarProjectionRevision = Long.MIN_VALUE;
    private String recipeSidebarProjectionKey = "";
    private List<CraftRunRecipeCapture> craftRunRecipeCaptures = List.of();
    static final int RECENT_REHOME_MAX_DISPLAYED = 3;
    static final int RECENT_REHOME_CAPACITY = 6;
    String editingIslandId = null;
    String islandLabelDraft = "";
    String editingClusterId = null;
    String clusterLabelDraft = "";
    float islandEditScreenX = Float.NaN;
    float islandEditScreenY = Float.NaN;
    SlotWorkspaceViewModel.IdentityRef pendingCreateIdentity;
    int pendingCreateWorldX;
    int pendingCreateWorldY;
    String pendingCreateLabel = "";
    int pendingCreateColor = ISLAND_PALETTE[0];
    boolean pendingCreateFocusPending;

    boolean kitRackOpen;
    /**
     * True while the deposit button is hovered (or otherwise being
     * previewed). Atlas cards check this in their TICK handler to draw
     * an accent outline on identities the planner would actually move,
     * so the player can see "what would happen if I click" before
     * clicking.
     */
    boolean depositPreviewActive;
    /**
     * Mirror of {@link #depositPreviewActive} for the global Gather
     * button. Atlas cards + TOC rows light up when the cursor is over
     * Gather to show what would actually be pulled if clicked. Set
     * by MOUSE_ENTER/MOUSE_LEAVE on the button itself.
     */
    boolean gatherPreviewActive;
    StorageGhostRevealMode storageGhostRevealMode = StorageGhostRevealMode.COLLAPSED;
    final SearchController searchController = new SearchController(this);
    final ShiftClickTransferState shiftClickTransferState = new ShiftClickTransferState();
    final WheelTransferBatcher wheelTransferBatcher = new WheelTransferBatcher();
    final WorkspaceRpcDispatcher rpc = new WorkspaceRpcDispatcher(this);
    final DragDropWiring drag = new DragDropWiring(this);
    final HotkeyRouter hotkeys = new HotkeyRouter(this);
    final WorkspaceOverlays overlays = new WorkspaceOverlays(this);
    final ListWallPanelBuilder listWall = new ListWallPanelBuilder(this);
    final BeltPanelBuilder belt = new BeltPanelBuilder(this);
    final KitRackBuilder kit = new KitRackBuilder(this);
    final ContextMenuBuilder menu = new ContextMenuBuilder(this);
    final AtlasCardBuilder atlasCard = new AtlasCardBuilder(this);
    final StoragePanelBuilder storagePanel = new StoragePanelBuilder(this);
    final LootChestPanelBuilder lootChestPanel = new LootChestPanelBuilder(this);
    final TocPanelBuilder tocPanel = new TocPanelBuilder(this);
    final RecentsStripBuilder recentsStrip = new RecentsStripBuilder(this);
    final ActiveChestStripBuilder activeChestStrip = new ActiveChestStripBuilder(this);
    ScrollerView wallScroller;
    /**
     * Persistent container for the belt (hotbar). Standalone mounts it at
     * root level; sidebar mode mounts it inside the left sidebar frame.
     */
    UIElement beltSlot;
    UIElement wallPanelElement;
    UIElement storagePanelElement;
    UIElement lootChestPanelElement;
    UIElement carriedFreeSlotsChipElement;
    UIElement topRightActionsElement;
    UIElement statusBarElement;
    Label statusBarLabel;
    // Deferred rebuild flag. Every server-sync round trip calls rebuild()
    // via syncBinding's remoteSetter; during rapid bursts (e.g. scroll-
    // wheel item transfer firing N RPCs) this used to destroy and recreate
    // the entire atlas content subtree N times before a single TICK could
    // warm up the new elements, which caused visible font-size, selection,
    // and hotbar-highlight flicker. With this flag, rebuild() just marks
    // the UI dirty; flushRebuildIfPending() in the per-frame tick
    // collapses any number of requests into one actual rebuild per frame.
    boolean rebuildPending;
    SlotWorkspaceViewModel.IdentityRef hoveredChestCellIdentity;
    String hoveredChestCellStorageId;
    private float pendingWallScrollRestore = Float.NaN;
    private boolean pendingWallScrollRestoreActive;
    private boolean flushingWheelTransfer;
    // Set by drop targets that handle a ChestStackDrag for something OTHER
    // than "take the item into inventory" (e.g. island assign-home is a
    // pure metadata op — item stays in the chest). The chest cell's
    // DRAG_END reads this to decide whether its default sendTakeFromChest
    // should fire. Reset in DRAG_END regardless.
    boolean chestDragDropConsumed;

    SlotWorkspaceUiController(SlotWorkspaceUiSession session, Player player) {
        this(session, player, false);
    }

    SlotWorkspaceUiController(SlotWorkspaceUiSession session, Player player, boolean sidebarMode) {
        this.session = session;
        this.player = player;
        this.sidebarMode = sidebarMode;
        this.viewModel = session.viewModel();
        // Root is transparent so the vanilla screen backdrop (dimmed
        // world / panorama) shows through everywhere we don't explicitly
        // paint a panel. Same idea as the vanilla inventory: chrome is
        // localized to specific widgets, the world stays visible.
        //
        // Root spans the full screen so the belt at the bottom can be
        // full-width (covering the vanilla hotbar in sidebar mode).
        // The actual workspace content is centered via the inner
        // `content` wrapper which carries the maxWidth + auto-margins.
        //
        // widthPercent(100) on root (rather than a fixed width) is
        // also load-bearing because of an LDLib2 ModularUI.init bug:
        // when the root's WIDTH style is fixed, layoutWidth becomes
        // NaN and Taffy gets MAX_CONTENT for BOTH axes (a typo in the
        // calculateStyleAndLayout call uses layoutWidth in the second
        // NaN check instead of layoutHeight). With unbounded height
        // the scroller never scrolls and the belt gets pushed off
        // screen. Keeping root at widthPercent(100) sidesteps this.
        this.root = new UIElement().layout(layout -> {
            layout.widthPercent(100)
                    .heightPercent(100)
                    .paddingAll(0)
                    .gapAll(0)
                    .alignItems(sidebarMode ? AlignItems.FLEX_START : AlignItems.CENTER)
                    .flexDirection(FlexDirection.COLUMN);
        });
        if (sidebarMode) {
            this.root.setAllowHitTest(false);
        }
        // Content stack: workspace top + mid + status. Standalone
        // surfaces center it in the full-screen root; sidebar surfaces
        // pin it to the configured left/top mount.
        this.content = new UIElement().layout(layout -> layout
                .width(contentWidth())
                .flex(1)
                .paddingAll(14)
                .gapAll(8)
                .flexDirection(FlexDirection.COLUMN));
        if (!sidebarMode) {
            this.content.layout(layout -> layout.marginHorizontalAuto());
        }
        // Standalone keeps the belt as a root-level footer. Sidebar mode
        // frames content + belt together so the left rail keeps its own
        // configured margins while floating panels use screen coordinates.
        this.beltSlot = new UIElement().layout(layout -> layout
                .widthPercent(100)
                .flexDirection(FlexDirection.COLUMN));
        if (sidebarMode) {
            this.sidebarFrame = new UIElement().layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(dev.imagio.slot.neoforge.config.SlotClientConfig.CLIENT.sidebarLeftMargin.get())
                    .top(dev.imagio.slot.neoforge.config.SlotClientConfig.CLIENT.sidebarTopMargin.get())
                    .bottom(dev.imagio.slot.neoforge.config.SlotClientConfig.CLIENT.sidebarBottomMargin.get())
                    .width(contentWidth())
                    .flexDirection(FlexDirection.COLUMN));
            this.sidebarFrame.setAllowHitTest(false);
            this.sidebarFrame.addChildren(content, beltSlot);
        } else {
            this.sidebarFrame = null;
        }
        this.recentsPanelSlot = new UIElement().layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(0).right(0).top(0).bottom(0));
        this.recentsPanelSlot.style(style -> style.zIndex(18));
        this.recentsPanelSlot.setAllowHitTest(false);
        this.taskPanelSlot = new UIElement().layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(0).right(0).top(0).bottom(0));
        this.taskPanelSlot.style(style -> style.zIndex(20));
        this.taskPanelSlot.setAllowHitTest(false);
        // Popovers (context menus, island edit, create-island) render
        // here — at root level with absolute fill — so their full-screen
        // dismiss catcher actually covers the full screen instead of
        // being scoped to whichever ancestor's bounding box happens to
        // be in scope. The slot itself is non-hit-testing so its
        // always-present empty bounds don't absorb clicks meant for the
        // wall / sections / chest list underneath; popover children
        // (catcher, capsule) keep their own hit-testing.
        this.popoverSlot = new UIElement().layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(0).right(0).top(0).bottom(0));
        this.popoverSlot.style(style -> style.zIndex(50));
        this.popoverSlot.setAllowHitTest(false);
        clearSelectionOnDirectClick(root);
        searchController.restoreRememberedQuery();
    }

    ModularUI create() {
        rpc.register();
        if (player.level().isClientSide()) {
            SlotWorkspaceTextInputCapture.register(this);
        }
        hotkeys.installBeltHotkeys();
        // Passive status-line tracer: every actual change flows through
        // localStatus.set(...) (Observable.set short-circuits no-ops),
        // so subscribing once gives a clean event log of "what just
        // happened" — useful for diagnosing cross-surface drag, RPC
        // round trips, and any user-visible status bar message
        // without sprinkling log statements at every set call site.
        // Gated by SlotDebugLog so production users don't see noise.
        localStatus.subscribeLater(value -> {
            if (value == null || value.isBlank()) {
                return;
            }
            SlotDebugLog.log("[status] {}", value);
        });
        // Cursor overlay sits at root, above content + every panel — its
        // ghost item must always render on top regardless of whichever
        // chrome the mouse is over. Added once during create() because
        // its TICK + MOUSE_MOVE listeners on root persist across rebuilds;
        // recreating it on every rebuildNow() would leak handlers.
        // Order: invisible sync binding, workspace frame/content, floating
        // recents, floating task panel, and popovers. The flex-column
        // flow gives content the leftover height above the bottom slots.
        if (sidebarMode) {
            root.addChildren(syncBinding(), sidebarFrame, recentsPanelSlot, taskPanelSlot, popoverSlot);
        } else {
            root.addChildren(syncBinding(), content, beltSlot, recentsPanelSlot, taskPanelSlot, popoverSlot);
        }
        root.addEventListener(UIEvents.REMOVED, event -> markSurfaceClosed());
        // Bubble-phase universal handlers for the real menu cursor:
        // rows 1 (right-click cancel) + 8 (left-click smart-deposit) of
        // the universal click table. Reaching root means no specific
        // drop target intercepted (drop targets call stopPropagation
        // when they handle a cursor click). Vanilla slot clicks short-
        // circuit this path entirely because vanilla slots aren't in our
        // LDLib widget tree — events on them never reach our root.
        root.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (!WorkspaceCursorState.isCarrying()) {
                return;
            }
            if (event.button == 1) {
                rpc.sendCursorCancel();
            } else if (event.button == 0) {
                rpc.sendCursorSmartDeposit();
            }
        });
        rebuildNow();
        searchController.syncRememberedQuery();
        return ModularUI.of(UI.of(root), player);
    }

    void markSurfaceClosed() {
        SlotWorkspaceTextInputCapture.unregister(this);
        flushWheelTransferBatch();
        WorkspaceUiSessionMemory.markClosed(surfaceMemoryKey());
    }

    boolean capturesTextInput() {
        return searchController.modalActive()
                || hotkeys.isTextInputFocused()
                || editingIslandId != null
                || editingClusterId != null
                || editingDesiredCountIdentity != null
                || renamingKitId != null
                || renamingChestStorageId != null
                || pendingCreateIdentity != null;
    }

    UIElement syncBinding() {
        BindableValue<Tag> binding = new BindableValue<>();
        binding.bind(DataBindingBuilder.tagS2C(session::viewTag)
                .remoteSetter(tag -> {
                    session.acceptRemoteView(tag);
                    viewModel = session.viewModel();
                    recipeSidebarProjection = null;
                    dev.imagio.slot.neoforge.client.SlotClientWorkspaceCache.update(viewModel);
                    localStatus.set(recipeSidebarActive() ? recipeSidebarSpec.label() : "");
                    rebuild();
                })
                .build());
        binding.layout(layout -> layout.width(0).height(0));
        return binding;
    }

    void rebuild() {
        rebuildPending = true;
    }

    int contentWidth() {
        return kitRackOpen ? WORKSPACE_WIDTH_PX : WORKSPACE_BASE_WIDTH_PX;
    }

    String surfaceMemoryKey() {
        return sidebarMode ? "neoforge.sidebar" : "neoforge.standalone";
    }

    boolean activeChestOpen() {
        return viewModel != null
                && viewModel.activeChestPanel() != null
                && viewModel.activeChestPanel().isPresent();
    }

    void rememberWallScroll(float scroll) {
        WorkspaceUiSessionMemory.setWallScroll(surfaceMemoryKey(), scroll);
    }

    boolean storageGhostSectionExpanded(String islandId) {
        return WorkspaceUiSessionMemory.storageGhostSectionExpanded(surfaceMemoryKey(), islandId);
    }

    void toggleStorageGhostSection(String islandId) {
        boolean expanded = WorkspaceUiSessionMemory.toggleStorageGhostSection(surfaceMemoryKey(), islandId);
        localStatus.set(expanded ? "showing nearby storage" : "hiding nearby storage");
        rebuild();
    }

    void updateStorageGhostRevealMode(StorageGhostRevealMode nextMode) {
        StorageGhostRevealMode mode = nextMode == null ? StorageGhostRevealMode.COLLAPSED : nextMode;
        if (mode == storageGhostRevealMode) {
            return;
        }
        storageGhostRevealMode = mode;
        rebuild();
    }

    void setCraftRunRecipeCaptures(List<CraftRunRecipeCapture> captures) {
        List<CraftRunRecipeCapture> next = activeCraftRunRecipeCaptures(captures);
        if (craftRunRecipeCaptureKey(craftRunRecipeCaptures).equals(craftRunRecipeCaptureKey(next))) {
            return;
        }
        craftRunRecipeCaptures = next;
        rebuild();
    }

    List<CraftRunRecipeCapture> craftRunRecipeCaptures() {
        return craftRunRecipeCaptures == null ? List.of() : craftRunRecipeCaptures;
    }

    private static List<CraftRunRecipeCapture> activeCraftRunRecipeCaptures(List<CraftRunRecipeCapture> captures) {
        if (captures == null || captures.isEmpty()) {
            return List.of();
        }
        ArrayList<CraftRunRecipeCapture> active = new ArrayList<>();
        for (CraftRunRecipeCapture capture : captures) {
            if (capture != null && capture.active()) {
                active.add(capture);
            }
        }
        return active.isEmpty() ? List.of() : List.copyOf(active);
    }

    private static String craftRunRecipeCaptureKey(List<CraftRunRecipeCapture> captures) {
        if (captures == null || captures.isEmpty()) {
            return "";
        }
        StringBuilder key = new StringBuilder();
        for (CraftRunRecipeCapture capture : captures) {
            if (capture != null && capture.active()) {
                key.append('|').append(capture.sourceKey());
            }
        }
        return key.toString();
    }

    void toggleStorageGhostRevealMode(StorageGhostRevealMode requestedMode) {
        StorageGhostRevealMode requested = requestedMode == null
                ? StorageGhostRevealMode.PROXIMATE
                : requestedMode;
        StorageGhostRevealMode next = switch (requested) {
            case TRACKED -> storageGhostRevealMode.toggleTracked();
            case PROXIMATE -> storageGhostRevealMode.toggleProximate();
            case COLLAPSED -> StorageGhostRevealMode.COLLAPSED;
        };
        localStatus.set(switch (next) {
            case TRACKED -> "showing all tracked storage";
            case PROXIMATE -> "showing nearby storage";
            case COLLAPSED -> "hiding storage ghosts";
        });
        updateStorageGhostRevealMode(next);
    }

    void requestWallScrollRestore(float scroll) {
        pendingWallScrollRestore = Float.isFinite(scroll) ? Math.max(0f, scroll) : 0f;
        pendingWallScrollRestoreActive = true;
    }

    void applyPendingWallScrollRestore() {
        if (!pendingWallScrollRestoreActive || wallScroller == null) {
            return;
        }
        pendingWallScrollRestoreActive = false;
        wallScroller.verticalScroller.setValue(pendingWallScrollRestore);
    }

    void enqueueWheelTransfer(
            WorkspaceActionId action,
            SlotWorkspaceViewModel.IdentityRef identity,
            int count,
            String status
    ) {
        flushWheelTransfer(wheelTransferBatcher.enqueue(action, identity, count, status));
        localStatus.set(status == null ? "" : status);
    }

    void flushWheelTransferBatch() {
        flushWheelTransfer(wheelTransferBatcher.flush());
    }

    void tickWheelTransferBatch(boolean shiftDown) {
        flushWheelTransfer(shiftDown ? wheelTransferBatcher.flushIfIdle() : wheelTransferBatcher.flush());
    }

    void flushWheelTransferBeforeAction() {
        if (!flushingWheelTransfer) {
            flushWheelTransferBatch();
        }
    }

    private void flushWheelTransfer(WheelTransferBatcher.Pending pending) {
        if (pending == null || pending.identity() == null || pending.count() <= 0) {
            return;
        }
        flushingWheelTransfer = true;
        try {
            boolean sent = rpc.send(
                    pending.action(),
                    pending.identity().itemId(),
                    pending.identity().comparisonMode(),
                    pending.identity().componentFingerprint(),
                    pending.count());
            localStatus.set(sent ? pending.status() : "transfer unavailable");
            if (!sent) {
                rebuild();
            }
        } finally {
            flushingWheelTransfer = false;
        }
    }

    void flushRebuildIfPending() {
        if (rebuildPending) {
            rebuildNow();
        }
    }

    void rebuildNow() {
        rebuildPending = false;
        if (hoveredAtlasIdentity != null && currentAtlasItem(hoveredAtlasIdentity) == null) {
            hoveredAtlasIdentity = null;
        }
        if (hoveredHotbarIndex >= 0
                && (hoveredHotbarIndex >= viewModel.hotbarSlots().size()
                || !viewModel.hotbarSlots().get(hoveredHotbarIndex).occupied())) {
            hoveredHotbarIndex = -1;
        }
        hotbarSlotElements.clear();
        if (!contentPopulated) {
            content.clearAllChildren();
            // Status bar is now placed inside the wall panel (between
            // the scroller mid-row and the kit/belt footer) by
            // ListWallPanelBuilder, so it sits above the hotbar
            // instead of taking a strip below it. Eagerly construct it
            // here so the lazy-init in overlays.statusBar() runs once
            // — repopulateWallPanel only references the cached element.
            overlays.statusBar();
            content.addChildren(listWall.body());
            contentPopulated = true;
        } else {
            storagePanel.repopulate();
            listWall.wallPanel();
        }
        content.layout(layout -> layout.width(contentWidth()));
        if (sidebarFrame != null) {
            sidebarFrame.layout(layout -> layout.width(contentWidth()));
        }
        // The belt is rebuilt each refresh because its hotbar slot subtree
        // depends on view-model state.
        beltSlot.clearAllChildren();
        beltSlot.addChild(belt.overlay());
        recentsPanelSlot.clearAllChildren();
        recentsPanelSlot.addChild(recentsStrip.floatingOverlay());
        taskPanelSlot.clearAllChildren();
        UIElement taskPanel = listWall.taskPanel();
        if (taskPanel != null) {
            taskPanelSlot.addChild(taskPanel);
        }
        content.markTaffyStyleDirty();
    }

    boolean contentPopulated;


    void installKeybindTooltip(
            Button button,
            String action,
            java.util.function.Supplier<String> keyLabelSupplier
    ) {
        button.addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
            String binding = "";
            try {
                binding = keyLabelSupplier.get();
            } catch (RuntimeException ignored) {
            }
            String tooltipText = binding == null || binding.isBlank()
                    ? action
                    : action + " (" + binding + ")";
            event.hoverTooltips = new HoverTooltips(
                    List.of(Component.literal(tooltipText)),
                    null,
                    null,
                    null
            );
        });
    }

    /** Install a static text tooltip on hover. Mirrors {@link #installKeybindTooltip} for non-keymap buttons. */
    void installTextTooltip(Button button, Component text) {
        button.addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
            event.hoverTooltips = new HoverTooltips(
                    List.of(text),
                    null,
                    null,
                    null
            );
        });
    }

    /** UIElement overload: same hover-tooltip wiring for non-Button surfaces (chest chips, etc.). */
    void installTextTooltip(com.lowdragmc.lowdraglib2.gui.ui.UIElement element, Component text) {
        element.addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
            event.hoverTooltips = new HoverTooltips(
                    List.of(text),
                    null,
                    null,
                    null
            );
        });
    }













    boolean shouldAccentHotbarSlot(SlotWorkspaceViewModel.HotbarSlot slot) {
        if (hoveredAtlasIdentity == null) {
            return false;
        }
        SlotWorkspaceViewModel.IdentityRef slotIdentity = SlotWorkspaceViewModel.IdentityRef.from(
                dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(slot.displayStack()));
        return hoveredAtlasIdentity.equals(slotIdentity);
    }

    boolean shouldAccentTriageRow(SlotWorkspaceViewModel.AtlasItem item) {
        if (hoveredHotbarIndex < 0 || hoveredHotbarIndex >= viewModel.hotbarSlots().size()) {
            return false;
        }
        SlotWorkspaceViewModel.HotbarSlot slot = viewModel.hotbarSlots().get(hoveredHotbarIndex);
        if (!slot.occupied()) {
            return false;
        }
        SlotWorkspaceViewModel.IdentityRef slotIdentity = SlotWorkspaceViewModel.IdentityRef.from(
                dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(slot.displayStack()));
        return item.identity().equals(slotIdentity);
    }







    static boolean chipMatches(SlotWorkspaceViewModel.AtlasItem item, ChipSuggestion target) {
        if (item == null || target == null) {
            return false;
        }
        for (ChipSuggestion candidate : item.chipSuggestions()) {
            if (candidate == null || candidate.kind() != target.kind()) {
                continue;
            }
            if (target.kind() == ChipSuggestion.ChipKind.TEMPLATE) {
                if (candidate.template() == target.template()) {
                    return true;
                }
            } else if (candidate.islandId().equals(target.islandId())) {
                return true;
            }
        }
        return false;
    }

    static String chipLabelText(ChipSuggestion chip) {
        // No hard truncation — chip labels live inside flex(1) layouts that
        // soft-clip when the container is narrower than the text. A fixed
        // 10-char cap was ellipsizing well-formed labels (e.g. "Mechanis...")
        // even when the surrounding row had plenty of horizontal slack.
        String label = chip.kind() == ChipSuggestion.ChipKind.TEMPLATE && chip.template() != null
                ? chip.template().defaultLabel()
                : chip.label();
        return label == null ? "" : label;
    }








    int firstFreeHotbarIndex() {
        for (SlotWorkspaceViewModel.HotbarSlot s : viewModel.hotbarSlots()) {
            if (!s.occupied()) {
                return s.hotbarIndex();
            }
        }
        return -1;
    }

    /**
     * Whether this carried atlas item has a "push" target. With
     * affinity/content routing the answer is just "any proximate chest is
     * available", since the deposit RPC applies the exact server-side
     * routing rule. Without proximate chests there's nothing to push to.
     */
    boolean atlasItemHasDepositTarget(SlotWorkspaceViewModel.AtlasItem item) {
        if (item == null) {
            return false;
        }
        return anyChestProximate();
    }













    void installHotbarHoverTooltip(Button button, SlotWorkspaceViewModel.HotbarSlot slot) {
        if (!slot.occupied()) {
            return;
        }
        WorkspaceUi.installItemTooltip(button, slot.displayStack());
    }

    void installOffhandHoverTooltip(Button button, SlotWorkspaceViewModel.OffhandSlot offhand) {
        if (!offhand.occupied()) {
            return;
        }
        WorkspaceUi.installItemTooltip(button, offhand.displayStack());
    }






    void rememberRehomeTarget(String islandId) {
        if (islandId == null || islandId.isBlank()) {
            return;
        }
        if (SlotWorkspaceAtlasLayout.ISLAND_TRIAGE.equals(islandId)) {
            return;
        }
        recentRehomeIslandIds.remove(islandId);
        recentRehomeIslandIds.addFirst(islandId);
        while (recentRehomeIslandIds.size() > RECENT_REHOME_CAPACITY) {
            recentRehomeIslandIds.removeLast();
        }
    }

    List<SlotWorkspaceViewModel.AtlasIsland> recentRehomeTargets(SlotWorkspaceViewModel.AtlasItem forItem) {
        ArrayList<SlotWorkspaceViewModel.AtlasIsland> result = new ArrayList<>();
        String currentIsland = forItem == null ? "" : forItem.islandId();
        for (String islandId : recentRehomeIslandIds) {
            if (islandId.equals(currentIsland)) {
                continue;
            }
            SlotWorkspaceViewModel.AtlasIsland island = viewModel.island(islandId);
            if (island == null) {
                continue;
            }
            result.add(island);
            if (result.size() >= RECENT_REHOME_MAX_DISPLAYED) {
                break;
            }
        }
        return result;
    }




    // Identity-based hotbar assignment. Superseded the old sendTransfer
    // path for atlas-item → hotbar-slot moves because slot-index-based
    // transfers assumed PLAYER_MAIN, which isn't where the item lives
    // when it's in a carried backpack.




    int hotbarSlotForIdentity(SlotWorkspaceViewModel.IdentityRef identity) {
        if (identity == null) {
            return -1;
        }
        for (SlotWorkspaceViewModel.HotbarSlot slot : viewModel.hotbarSlots()) {
            if (!slot.occupied()) {
                continue;
            }
            if (identity.equals(SlotWorkspaceViewModel.IdentityRef.from(
                    dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(slot.displayStack())))) {
                return slot.hotbarIndex();
            }
        }
        return -1;
    }





    boolean anyChestProximate() {
        for (SlotWorkspaceViewModel.ChestChip chip : viewModel.chestChips()) {
            if (chip.proximate()) {
                return true;
            }
        }
        return false;
    }






    SlotWorkspaceViewModel.AtlasItem hoveredAtlasItem() {
        return currentAtlasItem(hoveredAtlasIdentity);
    }

    SlotWorkspaceViewModel.AtlasItem focusedAtlasItem() {
        return hoveredAtlasItem();
    }

    SlotWorkspaceViewModel.IdentityRef currentMapFocusIdentity() {
        if (hoveredAtlasIdentity != null && currentAtlasItem(hoveredAtlasIdentity) != null) {
            return hoveredAtlasIdentity;
        }
        return null;
    }

    void setRecipeSidebarSpec(RecipeIngredientSidebarSpec spec) {
        RecipeIngredientSidebarSpec next = spec == null ? RecipeIngredientSidebarSpec.empty() : spec;
        String currentKey = recipeSidebarSpec == null ? "" : recipeSidebarSpec.sourceKey();
        if (currentKey.equals(next.sourceKey())) {
            return;
        }
        recipeSidebarSpec = next;
        recipeSidebarProjection = null;
        localStatus.set(next.active() ? next.label() : "");
        rebuild();
    }

    boolean recipeSidebarActive() {
        return recipeSidebarSpec != null && recipeSidebarSpec.active();
    }

    private RecipeIngredientSidebarSpec.Projection recipeProjection() {
        if (!recipeSidebarActive()) {
            return null;
        }
        long revision = viewModel == null ? -1L : viewModel.revision();
        String key = recipeSidebarSpec.sourceKey();
        if (recipeSidebarProjection == null
                || recipeSidebarProjectionRevision != revision
                || !recipeSidebarProjectionKey.equals(key)) {
            recipeSidebarProjection = recipeSidebarSpec.project(viewModel);
            recipeSidebarProjectionRevision = revision;
            recipeSidebarProjectionKey = key;
        }
        return recipeSidebarProjection;
    }

    boolean activeWorkflowTab() {
        return viewModel.activeKit() != null;
    }

    List<SlotWorkspaceViewModel.AtlasIsland> currentIslands() {
        RecipeIngredientSidebarSpec.Projection recipe = recipeProjection();
        if (recipe != null) {
            return recipe.islands();
        }
        return viewModel.islands();
    }

    SlotWorkspaceViewModel.AtlasIsland currentIsland(String islandId) {
        if (islandId == null || islandId.isBlank()) {
            return null;
        }
        for (SlotWorkspaceViewModel.AtlasIsland island : currentIslands()) {
            if (islandId.equals(island.islandId())) {
                return island;
            }
        }
        return null;
    }

    List<SlotWorkspaceViewModel.AtlasItem> currentAtlasItems() {
        RecipeIngredientSidebarSpec.Projection recipe = recipeProjection();
        if (recipe != null) {
            return recipe.atlasItems();
        }
        return viewModel.atlasItems();
    }

    SlotWorkspaceViewModel.AtlasItem currentAtlasItem(SlotWorkspaceViewModel.IdentityRef identity) {
        if (identity == null) {
            return null;
        }
        RecipeIngredientSidebarSpec.Projection recipe = recipeProjection();
        if (recipe != null) {
            return recipe.atlasItem(identity);
        }
        return viewModel.atlasItem(identity);
    }

    boolean recipeSuppressVanillaTooltip(SlotWorkspaceViewModel.AtlasItem item) {
        RecipeIngredientSidebarSpec.Projection recipe = recipeProjection();
        return recipe != null && recipe.suppressVanillaTooltip(item);
    }

    boolean taskPanelVisible() {
        return listWall.taskPanelVisible();
    }

    List<Component> recipeTooltipLines(SlotWorkspaceViewModel.AtlasItem item) {
        RecipeIngredientSidebarSpec.Projection recipe = recipeProjection();
        return recipe == null
                ? dev.imagio.slot.ui.workspace.WorkspaceItemTooltipBuilder.slotLines(item)
                : recipe.tooltipLines(item);
    }

    void selectAllTab() {
        localStatus.set("showing all items");
        rebuild();
    }

    void selectAllWorkflowTab() {
        if (viewModel != null && viewModel.activeKit() != null) {
            rpc.sendDeactivateKit();
            localStatus.set("showing All");
        } else {
            localStatus.set("showing All");
            rebuild();
        }
    }

    void selectWorkflowTab(String kitId) {
        if (kitId == null || kitId.isBlank()) {
            selectAllWorkflowTab();
            return;
        }
        SlotWorkspaceViewModel.KitCard tab = viewModel == null ? null : viewModel.kit(kitId);
        rpc.sendActivateKit(kitId);
        localStatus.set("showing " + (tab == null ? "workflow" : tab.name()));
        rebuild();
    }

    void openRecipe(SlotWorkspaceViewModel.AtlasItem item) {
        openRecipe(item == null ? null : item.identity().toIdentity());
    }

    void openRecipe(ItemIdentity identity) {
        if (identity == null) {
            localStatus.set("item unavailable");
        } else if (RecipeViewerIntegration.openRecipe(identity)) {
            localStatus.set("opened recipe in EMI");
        } else {
            localStatus.set("EMI recipe display unavailable");
        }
        rebuild();
    }

    void openUses(SlotWorkspaceViewModel.AtlasItem item) {
        ItemIdentity identity = item == null ? null : item.identity().toIdentity();
        if (identity == null) {
            localStatus.set("item unavailable");
        } else if (RecipeViewerIntegration.openUses(identity)) {
            localStatus.set("opened uses in EMI");
        } else {
            localStatus.set("EMI usage display unavailable");
        }
        rebuild();
    }

    /**
     * The identity that should drive "active" chrome on wall / triage /
     * loot rows. The vanilla menu cursor takes precedence (so picking up
     * an identity onto cursor lights up its card), then the last dropped
     * identity keeps the card warm after the cursor empties. Plain click
     * selection is intentionally not a command mode.
     */
    SlotWorkspaceViewModel.IdentityRef activeIdentity() {
        SlotWorkspaceViewModel.IdentityRef cursor = WorkspaceCursorState.carriedIdentity();
        if (cursor != null) {
            return cursor;
        }
        if (lastDroppedIdentity != null) {
            return lastDroppedIdentity;
        }
        return null;
    }

    boolean isMapFocusItem(SlotWorkspaceViewModel.AtlasItem item) {
        SlotWorkspaceViewModel.IdentityRef focusIdentity = currentMapFocusIdentity();
        return item != null && focusIdentity != null && item.identity().equals(focusIdentity);
    }

    String selectionLabel() {
        SlotWorkspaceViewModel.AtlasItem atlasItem = focusedAtlasItem();
        if (atlasItem != null) {
            return atlasItem.name();
        }
        return "none";
    }

    void appendTooltipPreview(List<UIElement> children, SlotWorkspaceViewModel.AtlasItem item) {
        List<Component> tooltipLines = atlasTooltipLines(item);
        if (tooltipLines.isEmpty()) {
            return;
        }
        ArrayList<String> preview = new ArrayList<>();
        String itemName = item.name();
        for (Component line : tooltipLines) {
            if (line == null) {
                continue;
            }
            String text = line.getString();
            if (text == null || text.isBlank()) {
                continue;
            }
            if (preview.isEmpty() && text.equals(itemName)) {
                continue;
            }
            preview.add(text);
        }
        if (preview.isEmpty()) {
            return;
        }
        children.add(label("Tooltip Preview", ACCENT).layout(layout -> layout.height(12)));
        int previewCount = Math.min(5, preview.size());
        for (int index = 0; index < previewCount; index++) {
            children.add(wrappedLabel(preview.get(index), MUTED));
        }
        if (preview.size() > previewCount) {
            children.add(wrappedLabel("(+" + (preview.size() - previewCount) + " more lines)", MUTED));
        }
    }






    void clearSelectionOnDirectClick(UIElement element) {
        element.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            // Click hit the bare root chrome (no widget consumed it) — treated
            // as "click on nothing." Carrying takes precedence: the
            // root-level universal cancel/smart-deposit handlers fire on
            // the same MOUSE_DOWN, so we skip selection clearing while
            // carrying to avoid stomping the cursor's own status.
            if (event.target != element) {
                return;
            }
            if (WorkspaceCursorState.isCarrying()) {
                return;
            }
        });
    }
}
