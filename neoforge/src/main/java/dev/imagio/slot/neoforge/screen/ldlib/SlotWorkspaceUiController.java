package dev.imagio.slot.neoforge.screen.ldlib;

import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceFormat.*;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.*;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceUi.*;

import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.gui.sync.rpc.RPCEmitter;
import com.lowdragmc.lowdraglib2.gui.sync.rpc.RPCEventBuilder;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.BindableValue;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.lowdragmc.lowdraglib2.gui.ui.event.HoverTooltips;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.util.DrawerHelper;
import dev.imagio.slot.atlas.AtlasSearchIndex;
import dev.imagio.slot.atlas.FitCarriedCamera;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.AtlasItemDrag;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.ChestStackDrag;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.ChestTileDrag;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.HotbarSlotDrag;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.IslandDrag;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.KitBringDrag;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.KitSlotDrag;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.StorageZoneBounds;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.StorageZoneDrag;
import dev.imagio.slot.neoforge.screen.ldlib.util.Observable;
import dev.imagio.slot.inventory.triage.ChipSuggestion;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceAtlasLayout;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.workflow.domain.KitPage;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

final class SlotWorkspaceUiController {
    final SlotWorkspaceUiSession session;
    final Player player;
    final UIElement root;
    final UIElement content;

    SlotWorkspaceViewModel viewModel;
    final Observable<String> localStatus = new Observable<>("");
    final Observable<SlotWorkspaceViewModel.IdentityRef> selectedAtlasIdentity = new Observable<>(null);
    SlotWorkspaceViewModel.IdentityRef hoveredAtlasIdentity;
    String hoveredIslandId;
    final Observable<Integer> selectedHotbarIndex = new Observable<>(-1);
    int hoveredHotbarIndex = -1;
    final List<Observable.Subscription> atlasContentSubscriptions = new ArrayList<>();
    final java.util.Map<Integer, UIElement> hotbarSlotElements = new java.util.HashMap<>();
    SlotWorkspaceViewModel.IdentityRef contextMenuAtlasIdentity;
    int contextMenuHotbarIndex = -1;
    String contextMenuKitId;
    String renamingKitId;
    String renameKitDraft = "";
    String confirmDeleteKitId;
    float contextMenuScreenX;
    float contextMenuScreenY;
    final java.util.ArrayDeque<String> recentRehomeIslandIds = new java.util.ArrayDeque<>();
    static final int RECENT_REHOME_MAX_DISPLAYED = 3;
    static final int RECENT_REHOME_CAPACITY = 6;
    String editingIslandId = null;
    String editingChestStorageId = null;
    String islandLabelDraft = "";
    float islandEditScreenX = Float.NaN;
    float islandEditScreenY = Float.NaN;
    SlotWorkspaceViewModel.IdentityRef pendingCreateIdentity;
    int pendingCreateWorldX;
    int pendingCreateWorldY;
    String pendingCreateLabel = "";
    int pendingCreateColor = ISLAND_PALETTE[0];
    boolean pendingCreateFocusPending;

    boolean kitRackOpen;
    AtlasCamera atlasCamera;
    final AtlasCameraController cameraController = new AtlasCameraController();
    final SearchController searchController = new SearchController(this);
    final WorkspaceRpcDispatcher rpc = new WorkspaceRpcDispatcher(this);
    final DragDropWiring drag = new DragDropWiring(this);
    final TriagePanelBuilder triagePanel = new TriagePanelBuilder(this);
    final BeltPanelBuilder belt = new BeltPanelBuilder(this);
    final KitRackBuilder kit = new KitRackBuilder(this);
    final ContextMenuBuilder menu = new ContextMenuBuilder(this);
    final IslandChestBuilder islandChest = new IslandChestBuilder(this);
    final AtlasCardBuilder atlasCard = new AtlasCardBuilder(this);
    SlotAtlasGraphView atlasView;
    UIElement atlasPanelElement;
    UIElement hoverTrailOverlayElement;
    UIElement carriedFreeSlotsChipElement;
    UIElement topRightActionsElement;
    UIElement statusBarElement;
    Label statusBarLabel;
    boolean atlasContentNeedsScreenTick;
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
    // Set by drop targets that handle a ChestStackDrag for something OTHER
    // than "take the item into inventory" (e.g. island assign-home is a
    // pure metadata op — item stays in the chest). The chest cell's
    // DRAG_END reads this to decide whether its default sendTakeFromChest
    // should fire. Reset in DRAG_END regardless.
    boolean chestDragDropConsumed;
    boolean peekActive;
    long peekPressTimeMs;
    AtlasCamera peekTarget;
    String gatherKitId = "";
    int gatherStep = 0;

    SlotWorkspaceUiController(SlotWorkspaceUiSession session, Player player) {
        this.session = session;
        this.player = player;
        this.viewModel = session.viewModel();
        this.root = panel(BACKGROUND).layout(layout -> layout
                .widthPercent(100)
                .heightPercent(100)
                .paddingAll(14)
                .gapAll(8)
                .flexDirection(FlexDirection.COLUMN));
        this.content = new UIElement().layout(layout -> layout
                .widthPercent(100)
                .heightPercent(100)
                .gapAll(8)
                .flexDirection(FlexDirection.COLUMN));
        clearSelectionOnDirectClick(root);
    }

    ModularUI create() {
        rpc.register();
        installBeltHotkeys();
        root.addChildren(syncBinding(), content);
        rebuildNow();
        return ModularUI.of(UI.of(root), player);
    }

    void installBeltHotkeys() {
        root.setEnforceFocus(event -> {
        });
        root.addEventListener(UIEvents.MUI_CHANGED, event -> root.focus());
        root.addEventListener(UIEvents.KEY_DOWN, this::handleBeltHotkey, true);
        root.addEventListener(UIEvents.KEY_DOWN, searchController::handleKeyDown, true);
        root.addEventListener(UIEvents.KEY_DOWN, this::handlePeekKeyDown, true);
        root.addEventListener(UIEvents.KEY_UP, this::handlePeekKeyUp, true);
        root.addEventListener(UIEvents.KEY_DOWN, this::handleCameraHistoryKey, true);
        root.addEventListener(UIEvents.KEY_DOWN, this::handleCycleKitPageKey, true);
        root.addEventListener(UIEvents.KEY_DOWN, this::handleUndoRedoKey, true);
        root.addEventListener(UIEvents.MOUSE_DOWN, this::handleCameraHistoryMouse, true);
        root.addEventListener(UIEvents.CHAR_TYPED, event -> {
            if (event.codePoint >= '1' && event.codePoint <= '9') {
                event.stopPropagation();
            }
        }, true);
        root.addEventListener(UIEvents.CHAR_TYPED, searchController::handleCharTyped, true);
        // Flush pending rebuilds FIRST thing each game tick, before any
        // render can start. Running this from atlas.setPerFrameTick (inside
        // drawBackgroundTexture) caused a visible 1-frame flash on
        // server-driven rebuilds: by the time the flush ran, ancestors
        // (content, body, atlasPanelElement) had already drawn this frame
        // with the old tree, so newly rebuilt children rendered inside a
        // momentarily stale parent layout. ModularUI.tick fires once per
        // game tick before the next render, so the rebuild is complete
        // before anything tries to draw.
        root.addEventListener(UIEvents.TICK, event -> flushRebuildIfPending());
        root.addEventListener(UIEvents.TICK, event -> cameraController.tick());
        root.addEventListener(UIEvents.TICK, event -> searchController.tickIdleTimer());
    }

    void handleBeltHotkey(UIEvent event) {
        int digit = digitFromKeyCode(event.keyCode);
        if (digit < 1 || digit > 9) {
            return;
        }
        event.stopPropagation();
        SlotWorkspaceViewModel.AtlasItem target = hoveredAtlasItem();
        if (target == null) {
            target = selectedAtlasItem();
        }
        if (target == null) {
            localStatus.set("hover or select an atlas item to assign with 1-9");
            rebuild();
            return;
        }
        sendAssignToHotbarSlot(target, digit - 1);
    }

    void handlePeekKeyDown(UIEvent event) {
        if (event.keyCode != GLFW.GLFW_KEY_SPACE) {
            return;
        }
        if (isTextInputFocused() || searchController.modalActive()) {
            return;
        }
        if (peekActive) {
            return;
        }
        if (cameraController.isDragging()) {
            return;
        }
        AtlasCamera target = resolvePeekTarget();
        if (target == null) {
            return;
        }
        event.stopPropagation();
        cameraController.recordOrigin();
        peekTarget = target;
        peekPressTimeMs = System.currentTimeMillis();
        peekActive = true;
        cameraController.ease(target, AtlasCameraController.CUBIC_IN_OUT, AtlasCameraController.PEEK_DURATION_MS);
    }

    void handlePeekKeyUp(UIEvent event) {
        if (event.keyCode != GLFW.GLFW_KEY_SPACE) {
            return;
        }
        if (!peekActive) {
            return;
        }
        event.stopPropagation();
        long heldMs = System.currentTimeMillis() - peekPressTimeMs;
        AtlasCamera target = peekTarget;
        AtlasCamera origin = cameraController.origin();
        peekActive = false;
        peekTarget = null;
        cameraController.clearOrigin();
        if (heldMs <= AtlasCameraController.PEEK_TAP_THRESHOLD_MS && target != null) {
            cameraController.commitFrom(
                    origin,
                    target,
                    AtlasCameraController.CommitSource.HOVER_GOTO,
                    AtlasCameraController.CUBIC_IN_OUT,
                    AtlasCameraController.COMMIT_DURATION_MS);
        } else if (origin != null) {
            cameraController.ease(
                    origin,
                    AtlasCameraController.CUBIC_IN_OUT,
                    AtlasCameraController.PEEK_SNAPBACK_DURATION_MS);
        }
    }

    boolean isTextInputFocused() {
        var mui = root.getModularUI();
        if (mui == null) {
            return false;
        }
        UIElement focused = mui.getFocusedElement();
        return focused != null && focused != root && focused instanceof TextField;
    }

    void handleCameraHistoryKey(UIEvent event) {
        if (isTextInputFocused() || searchController.modalActive()) {
            return;
        }
        if (dev.imagio.slot.neoforge.client.input.SlotAtlasKeyMappings
                .matchesBackKey(event.keyCode, event.scanCode)) {
            event.stopPropagation();
            performCameraBack();
        } else if (dev.imagio.slot.neoforge.client.input.SlotAtlasKeyMappings
                .matchesForwardKey(event.keyCode, event.scanCode)) {
            event.stopPropagation();
            performCameraForward();
        }
    }

    void handleCycleKitPageKey(UIEvent event) {
        if (isTextInputFocused() || searchController.modalActive()) {
            return;
        }
        if (!dev.imagio.slot.neoforge.client.input.SlotAtlasKeyMappings
                .matchesCycleKitPage(event.keyCode, event.scanCode)) {
            return;
        }
        SlotWorkspaceViewModel.KitCard active = viewModel.activeKit();
        if (active == null || active.pageCount() <= 1) {
            return;
        }
        event.stopPropagation();
        int direction = Screen.hasShiftDown() ? -1 : 1;
        sendSwitchKitPage(direction);
    }

    void handleUndoRedoKey(UIEvent event) {
        if (isTextInputFocused() || searchController.modalActive()) {
            return;
        }
        if (dev.imagio.slot.neoforge.client.input.SlotAtlasKeyMappings
                .matchesUndo(event.keyCode, event.scanCode)) {
            event.stopPropagation();
            sendUndo();
        } else if (dev.imagio.slot.neoforge.client.input.SlotAtlasKeyMappings
                .matchesRedo(event.keyCode, event.scanCode)) {
            event.stopPropagation();
            sendRedo();
        }
    }

    void sendUndo() {
        if (rpc.undoEmitter != null) {
            localStatus.set("undo");
            rpc.undoEmitter.send();
        }
    }

    void sendRedo() {
        if (rpc.redoEmitter != null) {
            localStatus.set("redo");
            rpc.redoEmitter.send();
        }
    }

    void handleCameraHistoryMouse(UIEvent event) {
        if (dev.imagio.slot.neoforge.client.input.SlotAtlasKeyMappings
                .matchesBackMouse(event.button)) {
            event.stopPropagation();
            performCameraBack();
        } else if (dev.imagio.slot.neoforge.client.input.SlotAtlasKeyMappings
                .matchesForwardMouse(event.button)) {
            event.stopPropagation();
            performCameraForward();
        }
    }

    void performCameraBack() {
        if (!cameraController.back()) {
            localStatus.set("no further camera history");
            rebuild();
        }
    }

    void performCameraForward() {
        if (!cameraController.forward()) {
            localStatus.set("at latest camera");
            rebuild();
        }
    }

    AtlasCamera resolvePeekTarget() {
        SlotAtlasGraphView atlas = cameraController.graphView();
        if (atlas == null) {
            return null;
        }
        if (hoveredHotbarIndex >= 0 && hoveredHotbarIndex < viewModel.hotbarSlots().size()) {
            SlotWorkspaceViewModel.HotbarSlot slot = viewModel.hotbarSlots().get(hoveredHotbarIndex);
            if (slot.occupied()) {
                SlotWorkspaceViewModel.IdentityRef identity = SlotWorkspaceViewModel.IdentityRef.from(
                        dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(slot.displayStack()));
                SlotWorkspaceViewModel.AtlasItem item = islandChest.atlasItemInIslandLayer(identity);
                if (item != null) {
                    return computeAtlasItemCamera(atlas, item);
                }
            }
        }
        if (hoveredAtlasIdentity != null) {
            SlotWorkspaceViewModel.AtlasItem item = islandChest.atlasItemInIslandLayer(hoveredAtlasIdentity);
            if (item != null) {
                AtlasCamera camera = computeAtlasItemCamera(atlas, item);
                if (camera != null) {
                    return camera;
                }
            }
            if (!viewModel.atlasItems().isEmpty()) {
                SlotWorkspaceViewModel.AtlasItem atlasItem = viewModel.atlasItem(hoveredAtlasIdentity);
                if (atlasItem != null && !atlasItem.presence().isEmpty()) {
                    SlotWorkspaceViewModel.ClaimedChestTile tile = viewModel.claimedChestTile(
                            atlasItem.presence().get(0).storageId());
                    if (tile != null) {
                        return computeChestTileCamera(atlas, tile);
                    }
                }
            }
        }
        if (hoveredChestCellIdentity != null) {
            SlotWorkspaceViewModel.AtlasItem item = islandChest.atlasItemInIslandLayer(hoveredChestCellIdentity);
            if (item != null) {
                AtlasCamera camera = computeAtlasItemCamera(atlas, item);
                if (camera != null) {
                    return camera;
                }
            }
            if (hoveredChestCellStorageId != null) {
                SlotWorkspaceViewModel.ClaimedChestTile tile =
                        viewModel.claimedChestTile(hoveredChestCellStorageId);
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
        FitCarriedCamera.Camera camera = FitCarriedCamera.fit(
                FitCarriedCamera.Rect.of(item.x(), item.y(), item.width(), item.height()),
                viewportWidth,
                viewportHeight,
                CARRIED_FIT_MIN_SCALE,
                CARRIED_FIT_MAX_SCALE,
                CARRIED_FIT_PADDING_PX
        );
        return camera == null ? null : new AtlasCamera(camera.offsetX(), camera.offsetY(), camera.scale());
    }

    int digitFromKeyCode(int keyCode) {
        if (keyCode >= GLFW.GLFW_KEY_1 && keyCode <= GLFW.GLFW_KEY_9) {
            return keyCode - GLFW.GLFW_KEY_1 + 1;
        }
        if (keyCode >= GLFW.GLFW_KEY_KP_1 && keyCode <= GLFW.GLFW_KEY_KP_9) {
            return keyCode - GLFW.GLFW_KEY_KP_1 + 1;
        }
        return 0;
    }


    UIElement syncBinding() {
        BindableValue<Tag> binding = new BindableValue<>();
        binding.bind(DataBindingBuilder.tagS2C(session::viewTag)
                .remoteSetter(tag -> {
                    session.acceptRemoteView(tag);
                    viewModel = session.viewModel();
                    localStatus.set("");
                    rebuild();
                })
                .build());
        binding.layout(layout -> layout.width(0).height(0));
        return binding;
    }

    void rebuild() {
        rebuildPending = true;
    }

    void flushRebuildIfPending() {
        if (rebuildPending) {
            rebuildNow();
        }
    }

    void rebuildNow() {
        rebuildPending = false;
        if (selectedAtlasIdentity.get() != null && viewModel.atlasItem(selectedAtlasIdentity.get()) == null) {
            selectedAtlasIdentity.set(null);
        }
        if (hoveredAtlasIdentity != null && viewModel.atlasItem(hoveredAtlasIdentity) == null) {
            hoveredAtlasIdentity = null;
        }
        if (hoveredHotbarIndex >= 0
                && (hoveredHotbarIndex >= viewModel.hotbarSlots().size()
                || !viewModel.hotbarSlots().get(hoveredHotbarIndex).occupied())) {
            hoveredHotbarIndex = -1;
        }
        hotbarSlotElements.clear();
        if (!contentPopulated) {
            // First build only: create the body/statusBar wrappers and add
            // them to content. Subsequent rebuilds reuse the same wrappers
            // — replacing them caused a blank-frame flash, because
            // rebuildNow runs inside atlas.perFrameTick (i.e. inside
            // atlas.drawBackgroundTexture), which is *after* the parent
            // content element has already drawn this frame with the old
            // children. Replacing the children means next frame renders
            // NEW elements whose layout hasn't settled yet — visible as a
            // 1-frame mismatch / flash.
            content.clearAllChildren();
            content.addChildren(
                    body(),
                    statusBar()
            );
            contentPopulated = true;
        } else {
            // Incremental refresh: atlasPanel() is the persistent panel
            // inside body, and calling it reruns repopulateAtlasPanel()
            // which destroys+rebuilds just the atlas-content subtree
            // (islands/cards/chest tiles). That subtree is what the
            // server sync actually invalidates.
            atlasPanel();
        }
        content.markTaffyStyleDirty();
    }

    boolean contentPopulated;

    UIElement topRightActionsOverlay() {
        // Floating action cluster pinned to the atlas panel's top-right
        // corner. Replaces the former persistent header strip. Vanilla is
        // always visible (primary escape hatch, also bound to a keymap).
        // Deposit only reveals itself when a claimed chest is proximate —
        // the same TICK-poll pattern the old header used, kept here so
        // the chip stays in sync as the player moves.
        UIElement overlay = new UIElement().layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .top(10)
                .right(10)
                .gapAll(6)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.ROW));
        overlay.style(style -> style.zIndex(11));

        boolean initialDepositEnabled = anyChestProximate();
        Button depositButton = button("Deposit", true, ACCENT);
        depositButton.layout(layout -> layout.width(60).height(16));
        depositButton.textStyle(style -> style
                .textColor(TEXT)
                .textShadow(false)
                .fontSize(7)
                .textAlignHorizontal(Horizontal.CENTER)
                .textAlignVertical(Vertical.CENTER));
        depositButton.setVisible(initialDepositEnabled);
        boolean[] lastDepositVisible = {initialDepositEnabled};
        depositButton.addEventListener(UIEvents.TICK, event -> {
            boolean proximate = anyChestProximate();
            if (proximate == lastDepositVisible[0]) {
                return;
            }
            lastDepositVisible[0] = proximate;
            depositButton.setVisible(proximate);
        });
        depositButton.setOnClick(event -> {
            event.stopPropagation();
            if (!anyChestProximate()) {
                return;
            }
            sendDeposit();
        });

        // HISTORY is a counter-clockwise curved arrow (classic undo);
        // ROTATION is a clockwise curved arrow (matches redo convention).
        // LEFT/RIGHT would read as paging, not undo/redo, so avoid those.
        Button undoButton = button("", true, GLASS).noText();
        undoButton.addPreIcon(Icons.HISTORY);
        undoButton.layout(layout -> layout.width(16).height(16));
        undoButton.setOnClick(event -> {
            event.stopPropagation();
            sendUndo();
        });
        installKeybindTooltip(
                undoButton,
                "Undo",
                dev.imagio.slot.neoforge.client.input.SlotAtlasKeyMappings::undoKeyLabel
        );

        Button redoButton = button("", true, GLASS).noText();
        redoButton.addPreIcon(Icons.ROTATION);
        redoButton.layout(layout -> layout.width(16).height(16));
        redoButton.setOnClick(event -> {
            event.stopPropagation();
            sendRedo();
        });
        installKeybindTooltip(
                redoButton,
                "Redo",
                dev.imagio.slot.neoforge.client.input.SlotAtlasKeyMappings::redoKeyLabel
        );

        Button vanillaButton = button("Vanilla", true, GLASS);
        vanillaButton.layout(layout -> layout.width(48).height(16));
        vanillaButton.textStyle(style -> style
                .textColor(MUTED)
                .textShadow(false)
                .fontSize(7)
                .textAlignHorizontal(Horizontal.CENTER)
                .textAlignVertical(Vertical.CENTER));
        vanillaButton.setOnClick(event -> {
            event.stopPropagation();
            dev.imagio.slot.neoforge.client.screen.SlotWorkspaceMountController.openVanillaInventory();
        });

        overlay.addChildren(depositButton, undoButton, redoButton, vanillaButton);
        return overlay;
    }

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
        if (atlasPanelElement == null) {
            createPersistentAtlasPanel();
        }
        repopulateAtlasPanel();
        return atlasPanelElement;
    }

    void createPersistentAtlasPanel() {
        UIElement panel = panel(PANEL).layout(layout -> layout
                .flex(1)
                .heightPercent(100)
                .paddingAll(0));
        clearSelectionOnDirectClick(panel);

        SlotAtlasGraphView atlas = new SlotAtlasGraphView();
        atlas.onCameraChanged(camera -> atlasCamera = camera);
        atlas.setPerFrameTick(() -> {
            // Rebuild flushing used to live here, but running it mid-
            // render (inside drawBackgroundTexture) let ancestors draw
            // with stale trees for one frame. The flush is now in the
            // game-tick TICK listener on root, which runs before any
            // rendering. This hook keeps doing the animation-driven
            // screenTick kick so cards keep receiving TICKs during
            // camera animations even when the tick-rate screenTick
            // already fired.
            boolean wasAnimating = cameraController.isAnimating();
            cameraController.tick();
            if (atlas.getContentWidth() <= 0f) {
                return;
            }
            if (wasAnimating || atlasContentNeedsScreenTick) {
                atlas.screenTick();
                atlasContentNeedsScreenTick = false;
            }
        });
        cameraController.attach(atlas);
        atlas.layout(layout -> layout.widthPercent(100).heightPercent(100));
        atlas.style(style -> style.backgroundTexture(rect(0xB810171D)).zIndex(0));
        atlas.graphViewStyle(style -> style
                .minScale(0.05f)
                .maxScale(4.50f)
                .gridTexture(IGuiTexture.EMPTY)
                .gridSize(48));
        atlas.addEventListener(UIEvents.LAYOUT_CHANGED, event -> {
            if (atlasCamera == null) {
                applyInitialCamera(atlas);
            } else {
                atlas.restoreCamera(atlasCamera);
            }
        });
        drag.installAtlasCanvasDropTarget(panel, atlas);
        drag.installAtlasBackgroundDropTarget(atlas);

        atlasView = atlas;
        hoverTrailOverlayElement = islandChest.hoverTrailOverlay(atlas);
        carriedFreeSlotsChipElement = carriedFreeSlotsChip();
        topRightActionsElement = topRightActionsOverlay();
        atlasPanelElement = panel;
        atlasContentNeedsScreenTick = true;
    }

    void repopulateAtlasPanel() {
        atlasContentNeedsScreenTick = true;
        for (Observable.Subscription sub : atlasContentSubscriptions) {
            sub.unsubscribe();
        }
        atlasContentSubscriptions.clear();
        UIElement panel = atlasPanelElement;
        SlotAtlasGraphView atlas = atlasView;

        // Drop transient children (overlays + popovers). Persistent children
        // (atlasView, hoverTrailOverlayElement) will be re-added below.
        panel.clearAllChildren();

        // Refresh atlas content (islands/cards/chest tiles/link threads) in-place.
        atlas.clearAllContentChildren();
        buildAtlas(atlas);

        panel.addChildren(atlas, triagePanel.overlay(), belt.overlay());
        panel.addChild(hoverTrailOverlayElement);
        panel.addChild(carriedFreeSlotsChipElement);
        panel.addChild(topRightActionsElement);
        if (searchController.modalActive()) {
            panel.addChild(searchChipOverlay());
        } else {
            panel.addChild(searchHintOverlay());
        }
        if (kitRackOpen) {
            panel.addChild(kit.kitRackOverlay());
        }
        UIElement contextMenu = menu.contextMenuOverlay();
        if (contextMenu != null) {
            panel.addChild(contextMenu);
        }
        UIElement editPopover = menu.islandEditPopover();
        if (editPopover != null) {
            panel.addChild(editPopover);
        }
        UIElement createPopover = menu.createIslandPopover();
        if (createPopover != null) {
            panel.addChild(createPopover);
        }
        UIElement linkPopover = menu.chestLinkPopover();
        if (linkPopover != null) {
            panel.addChild(linkPopover);
        }
    }

    UIElement searchChipOverlay() {
        // Anchor at the same top-left location as searchHintOverlay so opening
        // search doesn't feel like the UI jumped — the hint slides seamlessly
        // into the live modal.
        UIElement chip = panel(GLASS).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .top(10)
                .left(10)
                .width(280)
                .paddingAll(6)
                .gapAll(3)
                .flexDirection(FlexDirection.COLUMN));
        chip.style(style -> style.zIndex(12));
        chip.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());

        String bufferDisplay = "/" + searchController.buffer() + "_";
        chip.addChild(label(bufferDisplay, ACCENT).layout(layout -> layout.height(12)));

        String summary;
        if (searchController.buffer().length() < AtlasSearchIndex.DEFAULT_MIN_QUERY_CHARS) {
            summary = "Type " + AtlasSearchIndex.DEFAULT_MIN_QUERY_CHARS
                    + "+ chars  ·  Esc to close";
        } else if (searchController.matches().isEmpty()) {
            summary = "No matches  ·  Esc to close";
        } else {
            String commitHint = searchController.interactionDisablesAutoDismiss()
                    ? "Esc to close"
                    : "idle auto-commits  ·  Esc to abort";
            summary = (searchController.matchIndex() + 1) + " of " + searchController.matches().size()
                    + " matches  ·  Tab cycle  ·  Enter commit  ·  " + commitHint;
        }
        Label summaryLabel = wrappedLabel(summary, MUTED);
        summaryLabel.layout(layout -> layout.widthPercent(100).flex(1));
        chip.addChild(summaryLabel);
        return chip;
    }

    UIElement searchHintOverlay() {
        UIElement hint = panel(GLASS).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .top(10)
                .left(10)
                .paddingHorizontal(8)
                .paddingVertical(4)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.ROW));
        hint.style(style -> style.zIndex(11));
        hint.setAllowHitTest(false);
        hint.addChild(label("Press / to search", MUTED).layout(layout -> layout.height(10)));
        return hint;
    }

    static final int CARRIED_CHIP_WIDTH = 96;
    static final int CARRIED_CHIP_HEIGHT = 20;
    static final int CARRIED_CHIP_BAR_HEIGHT = 3;

    UIElement carriedFreeSlotsChip() {
        int initialFree = viewModel == null ? 0 : viewModel.carriedFreeSlotCount();
        int initialCapacity = viewModel == null ? 0 : viewModel.carriedSlotCapacity();
        UIElement chip = panel(GLASS).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .top(10)
                .width(CARRIED_CHIP_WIDTH)
                .height(CARRIED_CHIP_HEIGHT)
                .paddingHorizontal(8)
                .paddingTop(3)
                .paddingBottom(CARRIED_CHIP_BAR_HEIGHT + 3)
                .alignItems(AlignItems.CENTER)
                .flexDirection(FlexDirection.ROW));
        chip.style(style -> style.zIndex(11));
        chip.setAllowHitTest(false);
        Label valueLabel = label(formatFreeSlots(initialFree), TEXT);
        valueLabel.layout(layout -> layout.widthPercent(100).flex(1));
        valueLabel.textStyle(style -> style.textAlignHorizontal(Horizontal.CENTER));
        chip.addChild(valueLabel);
        // Fullness bar pinned to the bottom edge of the chip: the width
        // is the *filled* portion (capacity - free), so the bar grows
        // toward a warning state as the player's inventory fills up.
        int initialFilled = Math.max(0, initialCapacity - initialFree);
        int initialBarColor = fullnessColor(initialFilled, initialCapacity);
        float initialBarWidth = fullnessBarWidth(initialFilled, initialCapacity,
                CARRIED_CHIP_WIDTH);
        UIElement bar = panel(initialBarColor).layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(0)
                .bottom(0)
                .width(initialBarWidth)
                .height(CARRIED_CHIP_BAR_HEIGHT));
        bar.style(style -> style.zIndex(12));
        bar.setAllowHitTest(false);
        chip.addChild(bar);
        int[] lastFree = {initialFree};
        int[] lastBarColor = {initialBarColor};
        float[] lastBarWidth = {initialBarWidth};
        float[] lastLeft = {-1f};
        chip.addEventListener(UIEvents.TICK, event -> {
            int free = viewModel == null ? 0 : viewModel.carriedFreeSlotCount();
            int capacity = viewModel == null ? 0 : viewModel.carriedSlotCapacity();
            if (free != lastFree[0]) {
                lastFree[0] = free;
                valueLabel.setText(Component.literal(formatFreeSlots(free)));
            }
            int filled = Math.max(0, capacity - free);
            int barColor = fullnessColor(filled, capacity);
            if (barColor != lastBarColor[0]) {
                lastBarColor[0] = barColor;
                bar.style(style -> style.backgroundTexture(rect(barColor)));
            }
            float barWidth = fullnessBarWidth(filled, capacity, CARRIED_CHIP_WIDTH);
            if (Math.abs(barWidth - lastBarWidth[0]) > 0.5f) {
                lastBarWidth[0] = barWidth;
                bar.layout(layout -> layout.width(barWidth));
            }
            float panelWidth = atlasPanelElement == null ? 0f : atlasPanelElement.getContentWidth();
            if (panelWidth <= 0f) {
                return;
            }
            float desiredLeft = Math.max(0f, (panelWidth - CARRIED_CHIP_WIDTH) / 2f);
            if (Math.abs(desiredLeft - lastLeft[0]) > 0.5f) {
                lastLeft[0] = desiredLeft;
                chip.layout(layout -> layout.left(desiredLeft));
            }
        });
        return chip;
    }

    void buildAtlas(SlotAtlasGraphView atlas) {
        StorageZoneBounds bounds = islandChest.storageZoneBounds();
        if (bounds != null) {
            atlas.addContentChild(islandChest.storageZoneBackdrop(bounds));
            atlas.addContentChild(islandChest.storageZoneHeader(bounds, atlas));
        }
        // Link threads + arrows go in FIRST. LDLib's draw order is
        // child-insertion order (UIElement.drawContents iterates the
        // children list, not getSortedChildren — zIndex only affects
        // hit testing). So anything drawn later sits visually on top.
        // Adding threads before islands/chests/items ensures the line's
        // middle section is visible over atlas backdrop while the
        // portion that passes through a chest or island body is hidden
        // behind that body — exactly the intended "connected but not
        // overlapping" look. Arrows sit just outside the tile/island
        // edges, so they never overlap those bodies anyway.
        for (SlotWorkspaceViewModel.ClaimedChestTile tile : viewModel.claimedChestTiles()) {
            if (!tile.proximate()) {
                continue;
            }
            for (String islandId : tile.linkedIslandIds()) {
                SlotWorkspaceViewModel.AtlasIsland island = viewModel.island(islandId);
                if (island == null) {
                    continue;
                }
                islandChest.addLinkAffordances(atlas, tile, island);
            }
        }
        // Hover preview: if the player is hovering an island, draw dim link
        // threads to any of its NON-proximate linked chests so the relationship
        // is discoverable without walking to the chest. Also added before
        // islands so the dim line gets obscured by the island body.
        if (hoveredIslandId != null) {
            SlotWorkspaceViewModel.AtlasIsland hovered = viewModel.island(hoveredIslandId);
            if (hovered != null) {
                for (SlotWorkspaceViewModel.ClaimedChestTile tile : viewModel.claimedChestTiles()) {
                    if (tile.proximate() || !tile.linkedIslandIds().contains(hoveredIslandId)) {
                        continue;
                    }
                    UIElement dimThread = islandChest.dimLinkThread(tile, hovered);
                    if (dimThread != null) {
                        atlas.addContentChild(dimThread);
                    }
                }
            }
        }
        Set<String> highlightedIslandIds = islandChest.highlightedIslandIdsFromProximateTiles();
        for (SlotWorkspaceViewModel.AtlasIsland island : viewModel.islands()) {
            UIElement islandPanelEl = islandChest.islandPanel(atlas, island);
            atlas.addContentChild(islandPanelEl);
            atlas.addContentChild(islandChest.islandTitleBar(atlas, island, islandPanelEl));
            if (island.carriedCount() > 0) {
                atlas.addContentChild(islandChest.islandCarriedBadge(atlas, island));
            }
        }
        for (String islandId : highlightedIslandIds) {
            SlotWorkspaceViewModel.AtlasIsland island = viewModel.island(islandId);
            if (island != null) {
                islandChest.addIslandHighlightFrame(atlas, island);
            }
        }
        for (SlotWorkspaceViewModel.ClaimedChestTile tile : viewModel.claimedChestTiles()) {
            atlas.addContentChild(islandChest.chestTilePanel(atlas, tile));
        }
        for (SlotWorkspaceViewModel.AtlasItem item : viewModel.atlasItems()) {
            atlas.addContentChild(atlasCardButton(atlas, item));
            addAtlasItemChips(atlas, item);
        }
        if (viewModel.atlasItems().isEmpty()) {
            UIElement empty = label("No main inventory stacks visible", MUTED)
                    .layout(layout -> layout
                            .positionType(TaffyPosition.ABSOLUTE)
                            .left(viewModel.canvasWidth() / 2f - 104)
                            .top(viewModel.canvasHeight() / 2f - 8)
                            .width(208)
                            .height(16));
            empty.setAllowHitTest(false);
            atlas.addContentChild(empty);
        }
    }

    void sendDepositCarriedToChest(SlotWorkspaceViewModel.IdentityRef identity, String storageId) {
        if (rpc.depositCarriedToChestEmitter == null || identity == null || storageId == null || storageId.isBlank()) {
            return;
        }
        boolean sent = rpc.depositCarriedToChestEmitter.send(
                identity.itemId(),
                identity.comparisonMode(),
                identity.componentFingerprint(),
                storageId
        );
        if (!sent) {
            localStatus.set("deposit unavailable");
            rebuild();
        }
    }

    void sendDepositHotbarToChest(int hotbarIndex, String storageId) {
        if (rpc.depositHotbarToChestEmitter == null || storageId == null || storageId.isBlank()) {
            return;
        }
        boolean sent = rpc.depositHotbarToChestEmitter.send(hotbarIndex, storageId);
        if (!sent) {
            localStatus.set("deposit unavailable");
            rebuild();
        }
    }

    void sendTakeFromChest(String storageId, int chestSlotIndex) {
        if (rpc.takeFromChestEmitter == null || storageId == null || storageId.isBlank()) {
            return;
        }
        boolean sent = rpc.takeFromChestEmitter.send(storageId, chestSlotIndex);
        if (!sent) {
            localStatus.set("take unavailable");
            rebuild();
        }
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

    void installChestTileDragSource(
            UIElement source,
            SlotAtlasGraphView atlas,
            SlotWorkspaceViewModel.ClaimedChestTile tile
    ) {
        int[] clickWorldX = {Integer.MIN_VALUE};
        int[] clickWorldY = {Integer.MIN_VALUE};
        source.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button != 0) {
                return;
            }
            clickWorldX[0] = atlas.worldX(event.x);
            clickWorldY[0] = atlas.worldY(event.y);
        }, true);
        source.addEventListener(UIEvents.MOUSE_UP, event -> {
            clickWorldX[0] = Integer.MIN_VALUE;
            clickWorldY[0] = Integer.MIN_VALUE;
        }, true);
        source.addEventListener(UIEvents.MOUSE_MOVE, event -> {
            if (clickWorldX[0] == Integer.MIN_VALUE) {
                return;
            }
            if (!source.isMouseDown(0) || drag.isDragging(source)) {
                return;
            }
            float scale = atlas.getScale();
            float screenDx = (atlas.worldX(event.x) - clickWorldX[0]) * scale;
            float screenDy = (atlas.worldY(event.y) - clickWorldY[0]) * scale;
            if (screenDx * screenDx + screenDy * screenDy < DRAG_START_THRESHOLD_PX * DRAG_START_THRESHOLD_PX) {
                return;
            }
            int grabOffsetX = Math.max(0, Math.min(tile.width(), clickWorldX[0] - tile.atlasX()));
            int grabOffsetY = Math.max(0, Math.min(tile.height(), clickWorldY[0] - tile.atlasY()));
            int widthPx = Math.max(48, atlas.screenPixelsForWorldUnits(tile.width()));
            int heightPx = Math.max(20, atlas.screenPixelsForWorldUnits(tile.height()));
            int dragOffsetX = Math.round(grabOffsetX * scale);
            int dragOffsetY = Math.round(grabOffsetY * scale);
            source.startDrag(
                    new ChestTileDrag(tile.storageId(), grabOffsetX, grabOffsetY),
                    rect((STORAGE_TILE_FILL & 0x00FFFFFF) | 0x70000000)
            ).setDragTexture(-dragOffsetX, -dragOffsetY, widthPx, heightPx);
            localStatus.set("dragging " + tile.label());
        });
        source.addEventListener(UIEvents.DRAG_END, event -> drag.handleDragEnd(event));
    }

    void installChestStackDragSource(
            UIElement cell,
            SlotAtlasGraphView atlas,
            String storageId,
            int chestSlotIndex,
            ItemStack stack,
            String chestLabel
    ) {
        int[] clickWorldX = {Integer.MIN_VALUE};
        int[] clickWorldY = {Integer.MIN_VALUE};
        cell.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button != 0) {
                return;
            }
            clickWorldX[0] = atlas.worldX(event.x);
            clickWorldY[0] = atlas.worldY(event.y);
        });
        cell.addEventListener(UIEvents.MOUSE_UP, event -> {
            clickWorldX[0] = Integer.MIN_VALUE;
            clickWorldY[0] = Integer.MIN_VALUE;
        });
        cell.addEventListener(UIEvents.MOUSE_MOVE, event -> {
            if (clickWorldX[0] == Integer.MIN_VALUE) {
                return;
            }
            if (!cell.isMouseDown(0) || drag.isDragging(cell)) {
                return;
            }
            float scale = atlas.getScale();
            float screenDx = (atlas.worldX(event.x) - clickWorldX[0]) * scale;
            float screenDy = (atlas.worldY(event.y) - clickWorldY[0]) * scale;
            if (screenDx * screenDx + screenDy * screenDy < DRAG_START_THRESHOLD_PX * DRAG_START_THRESHOLD_PX) {
                return;
            }
            cell.startDrag(
                    new ChestStackDrag(storageId, chestSlotIndex, stack.copy()),
                    drag.dragTexture(stack)
            ).setDragTexture(-10, -10, 20, 20);
            localStatus.set("dragging " + stack.getHoverName().getString() + " from " + chestLabel);
        });
        cell.addEventListener(UIEvents.DRAG_END, event -> {
            Object payload = event.dragHandler == null ? null : event.dragHandler.getDraggingObject();
            boolean consumed = chestDragDropConsumed;
            chestDragDropConsumed = false;
            if (!consumed
                    && payload instanceof ChestStackDrag drag
                    && drag.storageId().equals(storageId)
                    && drag.chestSlotIndex() == chestSlotIndex) {
                sendTakeFromChest(storageId, chestSlotIndex);
            }
            drag.handleDragEnd(event);
        });
    }

    /**
     * Resolve the ghost-shrink scale factor for an atlas card based on
     * its carried state and the current disclosure level. Returns 1.0
     * for carried items (always full size) and for ghost items at
     * DETAIL zoom (where we want 1:1 clarity on the home slot); returns
     * {@link #GHOST_SHRINK_SCALE} otherwise so pushed-out zooms
     * de-emphasise items the player doesn't currently hold.
     */
    static float ghostScaleFor(SlotWorkspaceViewModel.AtlasItem item, AtlasRenderBudget budget) {
        if (item == null || budget == null || item.carried()) {
            return 1f;
        }
        return budget.level() == DisclosureLevel.DETAIL ? 1f : GHOST_SHRINK_SCALE;
    }

    /**
     * Apply the atlas card's outer button layout at its full
     * cell-allocated size. Ghost-shrink is done via a render-time
     * transform in {@link #applyAtlasCardGhostScale} so taffy sees the
     * card at full size — the card's inner widgets (shell, icon,
     * chips, accent bars, etc.) position themselves absolutely using
     * {@code item.width()}/{@code item.height()}-derived offsets, so
     * shrinking via layout would leave them anchored to the scaled
     * button's top-left and drift. {@code Transform2D} scales
     * rendering + hit-testing around a pivot without touching layout.
     */
    void applyAtlasCardLayout(Button button, SlotWorkspaceViewModel.AtlasItem item) {
        button.layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(item.x())
                .top(item.y())
                .width(item.width())
                .height(item.height())
                .paddingAll(0));
    }

    /**
     * Ghost cards render at {@link #GHOST_SHRINK_SCALE} × their full
     * layout size, scaled around the element centre so neighbours stay
     * aligned with the original cell grid. Non-ghosts / DETAIL zoom
     * reset the transform to identity.
     */
    void applyAtlasCardGhostScale(Button button, SlotWorkspaceViewModel.AtlasItem item, AtlasRenderBudget budget) {
        float scale = ghostScaleFor(item, budget);
        button.transform(transform -> {
            transform.pivot(0.5f, 0.5f);
            transform.scale(scale);
        });
    }

    Button atlasCardButton(SlotAtlasGraphView atlas, SlotWorkspaceViewModel.AtlasItem item) {
        boolean selected = item.identity().equals(selectedAtlasIdentity.get());
        boolean searchMatch = searchController.matchesItem(item);
        boolean activeSearchMatch = !searchController.normalizedQuery().isBlank() && searchMatch;
        AtlasRenderBudget initialBudget = atlasCard.atlasBudget(atlas, item);
        Button button = button("", true, cardChromeColor(initialBudget.level(), selected, searchMatch, item.recent(), item.carried(), !searchController.normalizedQuery().isBlank()));
        applyAtlasCardLayout(button, item);
        applyAtlasCardGhostScale(button, item, initialBudget);
        button.noText();
        button.style(style -> style.zIndex(2));
        button.setOnClick(event -> {
            event.stopPropagation();
            if (Screen.hasShiftDown()) {
                sendAssignHomeToFreeHotbar(item);
                return;
            }
            selectedAtlasIdentity.set(item.identity());
            selectedHotbarIndex.set(-1);
            localStatus.set(item.playerPlaced()
                    ? "selected homed item: drag to hotbar or another island"
                    : "selected inbox item: drag to an island or create one");
        });
        button.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button == 1) {
                event.stopPropagation();
                menu.openContextMenuForAtlas(item, event.x, event.y);
            }
        });
        float[] scrollAccumulator = {0f};
        button.addEventListener(UIEvents.MOUSE_WHEEL, event -> {
            if (!Screen.hasShiftDown()) {
                return;
            }
            // Minecraft swaps scrollX ↔ scrollY when shift is held, so the
            // scroll magnitude lands in deltaX under our shift-scroll gesture.
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
            SlotWorkspaceViewModel.AtlasItem fresh = viewModel.atlasItem(item.identity());
            if (fresh == null) {
                return;
            }
            int magnitude = Math.abs(count);
            if (count > 0) {
                AtlasCardBuilder.ChestSlotRef source = atlasCard.firstProximateChestSlotFor(fresh);
                if (source == null) {
                    localStatus.set("no nearby chest has " + fresh.name());
                    return;
                }
                for (int i = 0; i < magnitude; i++) {
                    sendTakeOneFromChest(source.storageId(), source.chestSlotIndex());
                }
            } else {
                boolean canPush = atlasItemHasDepositTarget(fresh)
                        || atlasCard.firstProximateChestSlotFor(fresh) != null;
                if (!canPush) {
                    localStatus.set("no nearby chest to push " + fresh.name());
                    return;
                }
                if (!fresh.carried()) {
                    localStatus.set(fresh.name() + " not carried");
                    return;
                }
                for (int i = 0; i < magnitude; i++) {
                    sendDepositOneHomeToLinkedChest(fresh);
                }
            }
        });
        button.addEventListener(UIEvents.MOUSE_ENTER, event -> hoveredAtlasIdentity = item.identity(), true);
        button.addEventListener(UIEvents.MOUSE_LEAVE, event -> {
            if (item.identity().equals(hoveredAtlasIdentity)) {
                hoveredAtlasIdentity = null;
            }
        }, true);
        drag.installAtlasHoverTooltip(button, item);
        drag.installAtlasItemDragSource(button, item);

        UIElement body = new UIElement().layout(layout -> layout.widthPercent(100).heightPercent(100));
        body.setAllowHitTest(false);
        atlasCard.rebuildAtlasBody(body, atlas, item, initialBudget, activeSearchMatch);
        button.addChild(body);

        long[] lastSignature = new long[]{atlasCard.atlasLayoutSignature(initialBudget)};
        button.addEventListener(UIEvents.TICK, event -> {
            AtlasRenderBudget budget = atlasCard.atlasBudget(atlas, item);
            boolean currentSelected = item.identity().equals(selectedAtlasIdentity.get());
            boolean focused = isMapFocusItem(item);
            long signature = atlasCard.atlasLayoutSignature(budget);
            // Skip LOD rebuilds while the camera is animating. atlasBudget
            // uses animationTargetScale while rendering uses the live
            // interpolated scale, so a rebuild mid-animation bakes labels
            // for the target and draws them at the current scale — visible
            // as a big-text flash at the start of a zoom-in peek. Letting
            // cards stay at the pre-animation LOD means labels either
            // scale with the zoom or stay absent until the camera settles;
            // either way it's continuous, not a jump.
            if (signature != lastSignature[0] && !cameraController.isAnimating()) {
                atlasCard.rebuildAtlasBody(body, atlas, item, budget, activeSearchMatch);
                // Refresh the ghost-shrink transform too — scale only
                // changes at the DETAIL↔INSPECT boundary, which the
                // budget signature already tracks. Layout stays at
                // full cell size; only the transform pivot/scale
                // change.
                applyAtlasCardGhostScale(button, item, budget);
                body.markTaffyStyleDirty();
                button.markTaffyStyleDirty();
                lastSignature[0] = signature;
            }
            button.style(style -> style.zIndex(focused ? 10 : currentSelected ? 7 : 2));
            applyButtonColors(button, true, cardChromeColor(budget.level(), currentSelected, searchMatch, item.recent(), item.carried(), !searchController.normalizedQuery().isBlank()));
        });
        // Items sit on top of their island panel (z=2 vs z=1) and receive
        // their own mouse enter/leave, so hovering an item inside an
        // island must also flip hoveredIslandId — otherwise the dim
        // link-thread preview only shows when the cursor happens to hit
        // empty island background between cards.
        SlotWorkspaceViewModel.AtlasIsland hoverIsland = viewModel.island(item.islandId());
        islandChest.attachIslandHoverListeners(button, hoverIsland);
        return button;
    }

    void addAtlasItemChips(SlotAtlasGraphView atlas, SlotWorkspaceViewModel.AtlasItem item) {
        List<ChipSuggestion> chips = item.chipSuggestions();
        if (chips.isEmpty()) {
            return;
        }
        int chipHeight = 10;
        int chipGap = 1;
        for (int index = 0; index < chips.size(); index++) {
            ChipSuggestion chip = chips.get(index);
            int top = item.y() + item.height() + 2 + index * (chipHeight + chipGap);
            Button chipButton = button("", true, chip.color());
            chipButton.noText();
            chipButton.layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(item.x())
                    .top(top)
                    .width(item.width())
                    .height(chipHeight)
                    .paddingAll(1)
                    .gapAll(2)
                    .flexDirection(FlexDirection.ROW)
                    .alignItems(AlignItems.CENTER));
            chipButton.style(style -> style.zIndex(3));
            chipButton.setOnClick(event -> {
                event.stopPropagation();
                sendChipAccept(item, chip);
            });
            Label chipLabel = label(chipLabelText(chip), TEXT);
            chipLabel.layout(layout -> layout.flex(1).height(chipHeight - 2));
            chipLabel.textStyle(style -> style
                    .textColor(TEXT)
                    .fontSize(6)
                    .textShadow(false)
                    .textAlignHorizontal(Horizontal.CENTER)
                    .textAlignVertical(Vertical.CENTER));
            chipLabel.setAllowHitTest(false);
            chipButton.addChild(chipLabel);
            atlas.addContentChild(chipButton);
        }
    }

    void sendChipAccept(SlotWorkspaceViewModel.AtlasItem item, ChipSuggestion chip) {
        if (rpc.acceptChipEmitter == null) {
            return;
        }
        selectedAtlasIdentity.set(item.identity());
        String templateName = chip.template() == null ? "" : chip.template().name();
        int accepted = 0;
        // Triage items carry their own chips but live in viewModel.triageItems(),
        // so we need to walk both lists. Walk atlas items first (the batch-apply
        // semantic: accept the same template for every matching homed card) then
        // also include triage items — this fires for the single clicked inbox
        // item that triggered the chip even when no homed cards match.
        java.util.LinkedHashSet<SlotWorkspaceViewModel.AtlasItem> candidates = new java.util.LinkedHashSet<>();
        candidates.addAll(viewModel.atlasItems());
        candidates.addAll(viewModel.triageItems());
        // Always include the clicked item so a chip click on an item the view model
        // no longer returns (e.g., a momentary projection race) still fires.
        candidates.add(item);
        for (SlotWorkspaceViewModel.AtlasItem candidate : candidates) {
            if (!chipMatches(candidate, chip)) {
                continue;
            }
            rpc.acceptChipEmitter.send(
                    candidate.identity().itemId(),
                    candidate.identity().comparisonMode(),
                    candidate.identity().componentFingerprint(),
                    chip.islandId(),
                    templateName
            );
            accepted++;
        }
        localStatus.set(accepted <= 1
                ? "accepting chip: " + chip.label()
                : "accepting chip: " + chip.label() + " x" + accepted);
        rebuild();
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
        String label = chip.kind() == ChipSuggestion.ChipKind.TEMPLATE && chip.template() != null
                ? chip.template().defaultLabel()
                : chip.label();
        return label == null ? "" : shorten(label, 10);
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
        for (SlotWorkspaceViewModel.AtlasIsland island : viewModel.islands()) {
            fitRects.add(FitCarriedCamera.Rect.of(island.x(), island.y(), island.width(), island.height()));
        }
        for (SlotWorkspaceViewModel.AtlasItem item : viewModel.atlasItems()) {
            if (item.carried()) {
                fitRects.add(FitCarriedCamera.Rect.of(item.x(), item.y(), item.width(), item.height()));
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
        // belt overlay (bottom chrome) so they do not occlude content at the
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
            cameraController.commit(
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
            cameraController.commit(
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

    void sendDuplicateKit(String kitId) {
        boolean sent = rpc.duplicateKitEmitter != null && rpc.duplicateKitEmitter.send(kitId);
        localStatus.set(sent ? "duplicating kit..." : "duplicate unavailable");
        rebuild();
    }

    int firstFreeHotbarIndex() {
        for (SlotWorkspaceViewModel.HotbarSlot s : viewModel.hotbarSlots()) {
            if (!s.occupied()) {
                return s.hotbarIndex();
            }
        }
        return -1;
    }

    boolean atlasItemHasDepositTarget(SlotWorkspaceViewModel.AtlasItem item) {
        if (item == null || item.islandId() == null || item.islandId().isBlank()) {
            return false;
        }
        for (SlotWorkspaceViewModel.ClaimedChestTile tile : viewModel.claimedChestTiles()) {
            if (tile.proximate() && tile.linkedIslandIds().contains(item.islandId())) {
                return true;
            }
        }
        return false;
    }

    UIElement inspectorPanel() {
        UIElement panel = panel(PANEL).layout(layout -> layout
                .width(284)
                .heightPercent(100)
                .paddingAll(8)
                .gapAll(6)
                .flexDirection(FlexDirection.COLUMN));
        clearSelectionOnDirectClick(panel);
        panel.addChildren(belt.selectionPanel());
        return panel;
    }

    void sendSaveKit() {
        boolean sent = rpc.saveKitEmitter != null && rpc.saveKitEmitter.send("");
        localStatus.set(sent ? "saving kit..." : "save kit unavailable");
        rebuild();
    }

    void sendActivateKit(String kitId) {
        boolean sent = rpc.activateKitEmitter != null && rpc.activateKitEmitter.send(kitId);
        localStatus.set(sent ? "activating kit..." : "activate kit unavailable");
        rebuild();
    }

    void sendDeactivateKit() {
        boolean sent = rpc.deactivateKitEmitter != null && rpc.deactivateKitEmitter.send();
        localStatus.set(sent ? "deactivating kit..." : "deactivate kit unavailable");
        rebuild();
    }

    void sendDeleteKit(String kitId) {
        boolean sent = rpc.deleteKitEmitter != null && rpc.deleteKitEmitter.send(kitId);
        localStatus.set(sent ? "deleting kit..." : "delete kit unavailable");
        rebuild();
    }

    void sendSwitchKitPage(int direction) {
        boolean sent = rpc.switchKitPageEmitter != null && rpc.switchKitPageEmitter.send(direction);
        localStatus.set(sent ? "switching kit page..." : "page switch unavailable");
        rebuild();
    }

    void sendAddKitPage(String kitId) {
        boolean sent = rpc.addKitPageEmitter != null && rpc.addKitPageEmitter.send(kitId);
        localStatus.set(sent ? "adding kit page..." : "add page unavailable");
        rebuild();
    }

    void sendRemoveKitPage(String kitId, int pageIndex) {
        boolean sent = rpc.removeKitPageEmitter != null && rpc.removeKitPageEmitter.send(kitId, pageIndex);
        localStatus.set(sent ? "removing kit page..." : "remove page unavailable");
        rebuild();
    }

    void sendAddKitBring(String kitId, SlotWorkspaceViewModel.IdentityRef identity) {
        if (identity == null) {
            return;
        }
        boolean sent = rpc.addKitBringEmitter != null && rpc.addKitBringEmitter.send(
                kitId, identity.itemId(), identity.comparisonMode(), identity.componentFingerprint());
        localStatus.set(sent ? "adding to bring..." : "add bring unavailable");
        rebuild();
    }

    void sendRemoveKitBring(String kitId, SlotWorkspaceViewModel.IdentityRef identity) {
        if (identity == null) {
            return;
        }
        boolean sent = rpc.removeKitBringEmitter != null && rpc.removeKitBringEmitter.send(
                kitId, identity.itemId(), identity.comparisonMode(), identity.componentFingerprint());
        localStatus.set(sent ? "removing from bring..." : "remove bring unavailable");
        rebuild();
    }

    void sendSwapKitSlots(String kitId, int pageIndex, int fromIndex, int toIndex) {
        boolean sent = rpc.swapKitSlotsEmitter != null
                && rpc.swapKitSlotsEmitter.send(kitId, pageIndex, fromIndex, toIndex);
        localStatus.set(sent ? "swapping kit slots..." : "swap slots unavailable");
        rebuild();
    }

    void sendSetKitSlotIdentity(String kitId, int pageIndex, int slotIndex, SlotWorkspaceViewModel.IdentityRef identity) {
        String itemId = identity == null ? "" : identity.itemId();
        String comparisonMode = identity == null ? "" : identity.comparisonMode();
        String fingerprint = identity == null ? "" : identity.componentFingerprint();
        boolean sent = rpc.setKitSlotIdentityEmitter != null && rpc.setKitSlotIdentityEmitter.send(
                kitId, pageIndex, slotIndex, itemId, comparisonMode, fingerprint);
        localStatus.set(sent ? "updating kit slot..." : "update slot unavailable");
        rebuild();
    }

    void installHotbarHoverTooltip(Button button, SlotWorkspaceViewModel.HotbarSlot slot) {
        button.addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
            if (!slot.occupied() || slot.displayStack().isEmpty()) {
                return;
            }
            event.hoverTooltips = new HoverTooltips(
                    List.copyOf(DrawerHelper.getItemToolTip(slot.displayStack())),
                    slot.displayStack().getTooltipImage().orElse(null),
                    null,
                    slot.displayStack()
            );
        });
    }

    void installOffhandHoverTooltip(Button button, SlotWorkspaceViewModel.OffhandSlot offhand) {
        button.addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
            if (!offhand.occupied() || offhand.displayStack().isEmpty()) {
                return;
            }
            event.hoverTooltips = new HoverTooltips(
                    List.copyOf(DrawerHelper.getItemToolTip(offhand.displayStack())),
                    offhand.displayStack().getTooltipImage().orElse(null),
                    null,
                    offhand.displayStack()
            );
        });
    }

    UIElement statusBar() {
        if (statusBarElement == null) {
            statusBarLabel = label("", MUTED);
            statusBarLabel.layout(layout -> layout.widthPercent(100).height(12));
            statusBarElement = panel(PANEL_ALT).layout(layout -> layout
                    .widthPercent(100)
                    .height(25)
                    .paddingAll(7));
            statusBarElement.addChild(statusBarLabel);
            localStatus.subscribe(v -> refreshStatusBarLabel());
        }
        refreshStatusBarLabel();
        return statusBarElement;
    }

    void refreshStatusBarLabel() {
        if (statusBarLabel == null) {
            return;
        }
        statusBarLabel.setText(Component.literal(
                "selected: " + selectionLabel()
                        + "  pending: " + viewModel.pendingCount()
                        + "  rev: " + viewModel.revision()
                        + "  " + (localStatus.get().isBlank() ? viewModel.status() : localStatus.get())
                        + (viewModel.diagnostics().isBlank() ? "" : "  " + viewModel.diagnostics())));
    }

    void sendTransfer(int sourceKind, int sourceIndex, int destinationKind, int destinationIndex) {
        boolean sent = rpc.transferEmitter != null && rpc.transferEmitter.send(
                sourceKind,
                sourceIndex,
                destinationKind,
                destinationIndex,
                "slot_workspace.ldlib.hotbar_transfer"
        );
        localStatus.set(sent ? "transfer requested" : "transfer unavailable");
        selectedAtlasIdentity.set(null);
        selectedHotbarIndex.set(-1);
        rebuild();
    }

    void sendAssignHome(String islandId) {
        SlotWorkspaceViewModel.AtlasItem item = selectedAtlasItem();
        if (item == null) {
            localStatus.set("select an atlas item first");
            rebuild();
            return;
        }
        sendAssignHome(item.identity(), islandId, -1, -1);
    }

    void sendAssignHome(
            SlotWorkspaceViewModel.IdentityRef identity,
            String islandId,
            int worldX,
            int worldY
    ) {
        if (identity == null || islandId == null || islandId.isBlank()) {
            localStatus.set("invalid home target");
            rebuild();
            return;
        }
        boolean sent = rpc.homeEmitter != null && rpc.homeEmitter.send(
                identity.itemId(),
                identity.comparisonMode(),
                identity.componentFingerprint(),
                islandId,
                worldX,
                worldY
        );
        if (sent) {
            rememberRehomeTarget(islandId);
        }
        localStatus.set(sent ? "home assignment requested" : "home assignment unavailable");
        selectedAtlasIdentity.set(null);
        selectedHotbarIndex.set(-1);
        rebuild();
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

    void sendReturnHotbarToHome(int hotbarIndex) {
        if (rpc.returnHotbarToHomeEmitter == null) {
            return;
        }
        boolean sent = rpc.returnHotbarToHomeEmitter.send(hotbarIndex);
        if (!sent) {
            localStatus.set("return-to-home unavailable");
            rebuild();
        }
    }

    void sendAssignHomeToFreeHotbar(SlotWorkspaceViewModel.AtlasItem item) {
        if (rpc.assignHomeToFreeHotbarEmitter == null || item == null) {
            return;
        }
        boolean sent = rpc.assignHomeToFreeHotbarEmitter.send(
                item.identity().itemId(),
                item.identity().comparisonMode(),
                item.identity().componentFingerprint()
        );
        if (!sent) {
            localStatus.set("assign-to-hotbar unavailable");
            rebuild();
        }
    }

    void sendAssignHomeToHotbarOnly(SlotWorkspaceViewModel.AtlasItem item) {
        if (rpc.assignHomeToHotbarOnlyEmitter == null || item == null) {
            return;
        }
        boolean sent = rpc.assignHomeToHotbarOnlyEmitter.send(
                item.identity().itemId(),
                item.identity().comparisonMode(),
                item.identity().componentFingerprint()
        );
        if (!sent) {
            localStatus.set("assign-to-hotbar unavailable");
            rebuild();
        }
    }

    // Identity-based hotbar assignment. Superseded the old sendTransfer
    // path for atlas-item → hotbar-slot moves because slot-index-based
    // transfers assumed PLAYER_MAIN, which isn't where the item lives
    // when it's in a carried backpack.
    void sendAssignToHotbarSlot(SlotWorkspaceViewModel.AtlasItem item, int hotbarIndex) {
        if (rpc.assignIdentityToHotbarSlotEmitter == null || item == null) {
            return;
        }
        boolean sent = rpc.assignIdentityToHotbarSlotEmitter.send(
                item.identity().itemId(),
                item.identity().comparisonMode(),
                item.identity().componentFingerprint(),
                hotbarIndex
        );
        localStatus.set(sent ? "transfer requested" : "transfer unavailable");
        selectedAtlasIdentity.set(null);
        selectedHotbarIndex.set(-1);
        // No explicit rebuild here — atlas-card TICK picks up the
        // selection change next frame, and the server sync after the RPC
        // triggers its own rebuild. Calling rebuild() now produced a
        // noticeable blank-frame flash because the entire content tree
        // (header, body, statusBar) got torn down between the local
        // state update and the server's authoritative one.
    }

    void sendDepositHomeToLinkedChest(SlotWorkspaceViewModel.AtlasItem item) {
        if (rpc.depositHomeToLinkedChestEmitter == null || item == null) {
            return;
        }
        boolean sent = rpc.depositHomeToLinkedChestEmitter.send(
                item.identity().itemId(),
                item.identity().comparisonMode(),
                item.identity().componentFingerprint()
        );
        if (!sent) {
            localStatus.set("deposit unavailable");
            rebuild();
        }
    }

    void sendDepositOneHomeToLinkedChest(SlotWorkspaceViewModel.AtlasItem item) {
        if (rpc.depositOneHomeToLinkedChestEmitter == null || item == null) {
            return;
        }
        boolean sent = rpc.depositOneHomeToLinkedChestEmitter.send(
                item.identity().itemId(),
                item.identity().comparisonMode(),
                item.identity().componentFingerprint()
        );
        if (!sent) {
            localStatus.set("deposit unavailable");
            rebuild();
        }
    }

    void sendTakeOneFromChest(String storageId, int chestSlotIndex) {
        if (rpc.takeOneFromChestEmitter == null || storageId == null || storageId.isBlank()) {
            return;
        }
        boolean sent = rpc.takeOneFromChestEmitter.send(storageId, chestSlotIndex);
        if (!sent) {
            localStatus.set("take unavailable");
            rebuild();
        }
    }

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

    void sendMoveHotbarToAtlas(int hotbarIndex, String islandId, int worldX, int worldY) {
        boolean sent = rpc.hotbarToAtlasEmitter != null && rpc.hotbarToAtlasEmitter.send(
                hotbarIndex,
                islandId,
                worldX,
                worldY
        );
        localStatus.set(sent ? "return to atlas requested" : "return to atlas unavailable");
        selectedAtlasIdentity.set(null);
        selectedHotbarIndex.set(-1);
        rebuild();
    }

    void sendMoveIsland(String islandId, int worldX, int worldY) {
        if (islandId == null || islandId.isBlank()) {
            localStatus.set("invalid island move");
            rebuild();
            return;
        }
        boolean sent = rpc.moveIslandEmitter != null && rpc.moveIslandEmitter.send(
                islandId,
                worldX,
                worldY
        );
        dev.imagio.slot.SlotCommon.LOGGER.info(
                "[SLOT] sendMoveIsland id={} worldX={} worldY={} sent={}",
                islandId, worldX, worldY, sent);
        localStatus.set(sent ? "island move requested" : "island move unavailable");
        rebuild();
    }

    void sendTakeAll(String storageId) {
        boolean sent = rpc.takeAllEmitter != null && rpc.takeAllEmitter.send(storageId);
        localStatus.set(sent ? "take-all requested" : "take-all unavailable");
        rebuild();
    }

    void sendDeposit() {
        boolean sent = rpc.depositEmitter != null && rpc.depositEmitter.send();
        localStatus.set(sent ? "deposit requested" : "deposit unavailable");
        rebuild();
    }

    boolean anyChestProximate() {
        for (SlotWorkspaceViewModel.ClaimedChestTile tile : viewModel.claimedChestTiles()) {
            if (tile.proximate()) {
                return true;
            }
        }
        return false;
    }

    void sendLinkChest(String islandId, String storageId) {
        if (islandId == null || islandId.isBlank() || storageId == null || storageId.isBlank()) {
            localStatus.set("invalid chest link");
            rebuild();
            return;
        }
        boolean sent = rpc.linkChestEmitter != null && rpc.linkChestEmitter.send(islandId, storageId);
        localStatus.set(sent ? "chest link requested" : "chest link unavailable");
        rebuild();
    }

    void sendUnlinkChest(String islandId, String storageId) {
        if (islandId == null || islandId.isBlank() || storageId == null || storageId.isBlank()) {
            localStatus.set("invalid chest unlink");
            rebuild();
            return;
        }
        boolean sent = rpc.unlinkChestEmitter != null && rpc.unlinkChestEmitter.send(islandId, storageId);
        localStatus.set(sent ? "chest unlink requested" : "chest unlink unavailable");
        rebuild();
    }

    void sendMoveStorageZone(int deltaX, int deltaY) {
        if (deltaX == 0 && deltaY == 0) {
            return;
        }
        boolean sent = rpc.moveStorageZoneEmitter != null && rpc.moveStorageZoneEmitter.send(deltaX, deltaY);
        localStatus.set(sent ? "storage zone moved" : "storage zone move unavailable");
        rebuild();
    }

    void sendMoveChest(String storageId, int atlasX, int atlasY) {
        if (storageId == null || storageId.isBlank()) {
            localStatus.set("invalid chest move");
            rebuild();
            return;
        }
        boolean sent = rpc.moveChestEmitter != null && rpc.moveChestEmitter.send(
                storageId,
                atlasX,
                atlasY
        );
        localStatus.set(sent ? "chest move requested" : "chest move unavailable");
        rebuild();
    }


    SlotWorkspaceViewModel.AtlasItem hoveredAtlasItem() {
        return viewModel.atlasItem(hoveredAtlasIdentity);
    }

    SlotWorkspaceViewModel.AtlasItem selectedAtlasItem() {
        return viewModel.atlasItem(selectedAtlasIdentity.get());
    }

    SlotWorkspaceViewModel.AtlasItem focusedAtlasItem() {
        SlotWorkspaceViewModel.AtlasItem selected = selectedAtlasItem();
        return selected != null ? selected : hoveredAtlasItem();
    }

    SlotWorkspaceViewModel.IdentityRef currentMapFocusIdentity() {
        if (hoveredAtlasIdentity != null && viewModel.atlasItem(hoveredAtlasIdentity) != null) {
            return hoveredAtlasIdentity;
        }
        return selectedAtlasIdentity.get();
    }

    boolean isMapFocusItem(SlotWorkspaceViewModel.AtlasItem item) {
        SlotWorkspaceViewModel.IdentityRef focusIdentity = currentMapFocusIdentity();
        return item != null && focusIdentity != null && item.identity().equals(focusIdentity);
    }

    SlotWorkspaceViewModel.HotbarSlot selectedHotbarSlot() {
        int idx = selectedHotbarIndex.get();
        if (idx < 0 || idx >= viewModel.hotbarSlots().size()) {
            return null;
        }
        SlotWorkspaceViewModel.HotbarSlot slot = viewModel.hotbarSlots().get(idx);
        return slot.occupied() ? slot : null;
    }

    String selectionLabel() {
        SlotWorkspaceViewModel.AtlasItem atlasItem = selectedAtlasItem();
        if (atlasItem != null) {
            return atlasItem.name();
        }
        SlotWorkspaceViewModel.HotbarSlot hotbar = selectedHotbarSlot();
        if (hotbar != null) {
            return "hotbar " + (hotbar.hotbarIndex() + 1);
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

    UIElement itemIcon(ItemStack stack, float size) {
        return itemIcon(stack, size, true);
    }

    UIElement itemIcon(ItemStack stack, float size, boolean carried) {
        ItemStack iconStack = stack == null ? ItemStack.EMPTY : stack.copy();
        ItemStackTexture texture = new ItemStackTexture(iconStack);
        UIElement icon = new UIElement().layout(layout -> layout.width(size).height(size))
                .style(style -> {
                    style.backgroundTexture(texture);
                    if (!carried) {
                        style.overlayTexture(rect(GHOST_ICON_OVERLAY_COLOR));
                    }
                });
        icon.setAllowHitTest(false);
        return icon;
    }

    UIElement emptyIcon() {
        UIElement icon = panel(0x80323B44).layout(layout -> layout.width(16).height(16));
        icon.setAllowHitTest(false);
        return icon;
    }

    Label anchorLabel(String text, int color, float fontSize) {
        Label label = label(text, color);
        label.textStyle(style -> style
                .fontSize(fontSize)
                .textAlignVertical(Vertical.CENTER)
                .textAlignHorizontal(Horizontal.LEFT));
        return label;
    }

    float centeredWorld(float container, float child) {
        return Math.max(0f, (container - child) / 2f);
    }

    void clearSelectionOnDirectClick(UIElement element) {
        element.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.target == element && (selectedAtlasIdentity.get() != null || selectedHotbarIndex.get() >= 0)) {
                selectedAtlasIdentity.set(null);
                selectedHotbarIndex.set(-1);
                localStatus.set("selection cleared");
            }
        });
    }
}
