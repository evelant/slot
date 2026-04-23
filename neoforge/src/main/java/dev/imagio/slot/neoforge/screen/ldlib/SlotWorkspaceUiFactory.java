package dev.imagio.slot.neoforge.screen.ldlib;

import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.gui.sync.rpc.RPCEmitter;
import com.lowdragmc.lowdraglib2.gui.sync.rpc.RPCEventBuilder;
import com.lowdragmc.lowdraglib2.gui.texture.ColorRectTexture;
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
import com.lowdragmc.lowdraglib2.gui.ui.elements.GraphView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.lowdragmc.lowdraglib2.gui.ui.event.HoverTooltips;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.util.DrawerHelper;
import dev.imagio.slot.atlas.AtlasSearchIndex;
import dev.imagio.slot.atlas.CameraHistory;
import dev.imagio.slot.atlas.FitCarriedCamera;
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
import net.minecraft.resources.ResourceLocation;
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
import java.util.function.Consumer;

final class SlotWorkspaceUiFactory {
    private static final ResourceLocation FONT_UI =
            ResourceLocation.fromNamespaceAndPath("slot", "slot_ui");
    private static final int BACKGROUND = 0x96060A0E;
    private static final int PANEL = 0xC8162029;
    private static final int PANEL_ALT = 0xD01E2933;
    private static final int GLASS = 0xCC0C141A;
    private static final int ROW = 0xEC24313D;
    private static final int ROW_DIM = 0x7C24313D;
    private static final int ROW_HOVER = 0xEC334354;
    private static final int ROW_MATCH = 0xED345749;
    private static final int SELECTED = 0xF0507E6B;
    private static final int ACTIVE_HOTBAR = 0xF0665B33;
    // Brightened amber for kit-card hover so the active card stays recognizable.
    private static final int ACTIVE_HOTBAR_HOVER = 0xF08A7A4D;
    private static final int ACTIVE_HOTBAR_PRESSED = 0xF0524830;
    // Subtle amber fill behind the active page row inside a kit card.
    private static final int ACTIVE_PAGE_ROW = 0x40A08544;
    private static final int TEXT = 0xFFE8EEF2;
    private static final int MUTED = 0xFFA0AAB3;
    private static final int ACCENT = 0xFF7AC7A7;
    private static final int WARNING = 0xFFFFC66D;
    private static final int CARRIED_CHIP_OK = 0xCC4A8B5E;
    private static final int CARRIED_CHIP_WARN = 0xCCB48A3A;
    private static final int CARRIED_CHIP_DANGER = 0xCCB44A3A;
    private static final int CARRIED_CONTAINER_PIP = 0xCC5A7DB4;
    private static final int COLLECTION = 0xFFBE8CFF;
    private static final int ISLAND_BORDER = 0xA04F6578;
    private static final int STORAGE_ZONE_FILL = 0x501A2430;
    private static final int STORAGE_ZONE_HEADER_FILL = 0xC02E3A48;
    private static final int STORAGE_ZONE_HEADER_HEIGHT = 16;
    private static final int STORAGE_TILE_FILL = 0xD02E3A48;
    private static final int STORAGE_TILE_FILL_DIM = 0x602E3A48;
    private static final int STORAGE_TILE_CELL_FILL = 0x801A2430;
    private static final int STORAGE_TILE_CELL_FILL_DIM = 0x401A2430;
    private static final int LINK_THREAD_COLOR = 0xC07AC7A7;
    // Dimmed thread color used on island hover to preview links to non-proximate
    // chests without competing visually with the full-color proximate threads.
    private static final int LINK_THREAD_DIM_COLOR = 0x407AC7A7;
    private static final int LINK_HIGHLIGHT_COLOR = 0xA07AC7A7;
    private static final int LINK_HIGHLIGHT_THICKNESS = 2;
    private static final int HOVER_TRAIL_COLOR = 0xD0FFC66D;
    private static final int HOVER_TRAIL_THICKNESS = 2;
    private static final int HOVER_ACCENT_OVERLAY = 0x60FFC66D;
    private static final int BROWSE_CELL_PX = 16;
    private static final int READ_CELL_PX = 22;
    private static final int INSPECT_CELL_PX = 44;
    private static final int DETAIL_CELL_PX = 96;
    private static final float CARRIED_FIT_MIN_SCALE = 0.20f;
    private static final float CARRIED_FIT_MAX_SCALE = 2.50f;
    private static final float CARRIED_FIT_READABILITY_MIN_SCALE = 1.00f;
    private static final float CARRIED_FIT_PADDING_PX = 72f;
    private static final int BELT_HEIGHT = 24;
    private static final int BELT_SLOT_SIZE = 20;
    // Fixed-width holding area for the Kit toggle + page cycle button. Wide enough
    // for "Longname 3/3" (≈10 name chars + " N/M" + padding) plus the ">" cycle
    // button. Changes to the kit label grow LEFT inside this slot instead of
    // shoving the hotbar.
    private static final int KIT_CLUSTER_WIDTH = 130;
    private static final int BELT_DIVIDER_HEIGHT = 16;
    private static final int TRIAGE_PANEL_WIDTH = 152;
    private static final float NAV_CAPSULE_INSET_PX = 96f;
    private static final float BELT_CAMERA_INSET_PX = 44f;
    private static final float SIDE_CAMERA_INSET_PX = 48f;
    private static final float GHOST_CARD_ALPHA = 0.18f;
    private static final int GHOST_ICON_OVERLAY_COLOR = 0xC8060A0E;
    // Ghost (non-carried) atlas cards render at this fraction of their
    // allocated cell size at every disclosure level EXCEPT DETAIL. Keeps
    // the close-up view showing 1:1 "this is the home slot" while pushed-
    // out zooms de-emphasise homes the player doesn't currently hold.
    private static final float GHOST_SHRINK_SCALE = 0.6f;
    private static final int DRAG_START_THRESHOLD_PX = 4;

    private SlotWorkspaceUiFactory() {
    }

    static ModularUI create(SlotWorkspaceUiSession session, Player player) {
        return new Controller(session, player).create();
    }

    private static final class Controller {
        private final SlotWorkspaceUiSession session;
        private final Player player;
        private final UIElement root;
        private final UIElement content;

        private SlotWorkspaceViewModel viewModel;
        private final Observable<String> localStatus = new Observable<>("");
        private String searchQuery = "";
        private final Observable<SlotWorkspaceViewModel.IdentityRef> selectedAtlasIdentity = new Observable<>(null);
        private SlotWorkspaceViewModel.IdentityRef hoveredAtlasIdentity;
        private String hoveredIslandId;
        private final Observable<Integer> selectedHotbarIndex = new Observable<>(-1);
        private int hoveredHotbarIndex = -1;
        private final List<Observable.Subscription> atlasContentSubscriptions = new ArrayList<>();
        private final java.util.Map<Integer, UIElement> hotbarSlotElements = new java.util.HashMap<>();
        private SlotWorkspaceViewModel.IdentityRef contextMenuAtlasIdentity;
        private int contextMenuHotbarIndex = -1;
        private String contextMenuKitId;
        private String renamingKitId;
        private String renameKitDraft = "";
        private String confirmDeleteKitId;
        private float contextMenuScreenX;
        private float contextMenuScreenY;
        private final java.util.ArrayDeque<String> recentRehomeIslandIds = new java.util.ArrayDeque<>();
        private static final int RECENT_REHOME_MAX_DISPLAYED = 3;
        private static final int RECENT_REHOME_CAPACITY = 6;
        private String editingIslandId = null;
        private String editingChestStorageId = null;
        private String islandLabelDraft = "";
        private float islandEditScreenX = Float.NaN;
        private float islandEditScreenY = Float.NaN;
        private SlotWorkspaceViewModel.IdentityRef pendingCreateIdentity;
        private int pendingCreateWorldX;
        private int pendingCreateWorldY;
        private String pendingCreateLabel = "";
        private int pendingCreateColor = ISLAND_PALETTE[0];
        private boolean pendingCreateFocusPending;

        private RPCEmitter transferEmitter;
        private RPCEmitter homeEmitter;
        private RPCEmitter createNamedIslandEmitter;
        private RPCEmitter hotbarToAtlasEmitter;
        private RPCEmitter moveIslandEmitter;
        private RPCEmitter moveChestEmitter;
        private RPCEmitter moveStorageZoneEmitter;
        private RPCEmitter relabelChestEmitter;
        private RPCEmitter linkChestEmitter;
        private RPCEmitter unlinkChestEmitter;
        private RPCEmitter depositEmitter;
        private RPCEmitter takeAllEmitter;
        private RPCEmitter renameIslandEmitter;
        private RPCEmitter recolorIslandEmitter;
        private RPCEmitter setIslandIconEmitter;
        private RPCEmitter deleteIslandEmitter;
        private RPCEmitter acceptChipEmitter;
        private RPCEmitter saveKitEmitter;
        private RPCEmitter activateKitEmitter;
        private RPCEmitter deactivateKitEmitter;
        private RPCEmitter undoEmitter;
        private RPCEmitter redoEmitter;
        private RPCEmitter deleteKitEmitter;
        private RPCEmitter switchKitPageEmitter;
        private RPCEmitter addKitPageEmitter;
        private RPCEmitter removeKitPageEmitter;
        private RPCEmitter addKitBringEmitter;
        private RPCEmitter removeKitBringEmitter;
        private RPCEmitter setKitSlotIdentityEmitter;
        private RPCEmitter renameKitEmitter;
        private RPCEmitter duplicateKitEmitter;
        private RPCEmitter swapKitSlotsEmitter;
        private RPCEmitter returnHotbarToHomeEmitter;
        private RPCEmitter assignHomeToFreeHotbarEmitter;
        private RPCEmitter depositCarriedToChestEmitter;
        private RPCEmitter depositHotbarToChestEmitter;
        private RPCEmitter takeFromChestEmitter;
        private RPCEmitter takeOneFromChestEmitter;
        private RPCEmitter assignHomeToHotbarOnlyEmitter;
        private RPCEmitter assignIdentityToHotbarSlotEmitter;
        private RPCEmitter depositHomeToLinkedChestEmitter;
        private RPCEmitter depositOneHomeToLinkedChestEmitter;
        private boolean kitRackOpen;
        private AtlasCamera atlasCamera;
        private final AtlasCameraController cameraController = new AtlasCameraController();
        private SlotAtlasGraphView atlasView;
        private UIElement atlasPanelElement;
        private UIElement hoverTrailOverlayElement;
        private UIElement carriedFreeSlotsChipElement;
        private UIElement topRightActionsElement;
        private UIElement statusBarElement;
        private Label statusBarLabel;
        private boolean atlasContentNeedsScreenTick;
        // Deferred rebuild flag. Every server-sync round trip calls rebuild()
        // via syncBinding's remoteSetter; during rapid bursts (e.g. scroll-
        // wheel item transfer firing N RPCs) this used to destroy and recreate
        // the entire atlas content subtree N times before a single TICK could
        // warm up the new elements, which caused visible font-size, selection,
        // and hotbar-highlight flicker. With this flag, rebuild() just marks
        // the UI dirty; flushRebuildIfPending() in the per-frame tick
        // collapses any number of requests into one actual rebuild per frame.
        private boolean rebuildPending;
        private SlotWorkspaceViewModel.IdentityRef hoveredChestCellIdentity;
        private String hoveredChestCellStorageId;
        // Set by drop targets that handle a ChestStackDrag for something OTHER
        // than "take the item into inventory" (e.g. island assign-home is a
        // pure metadata op — item stays in the chest). The chest cell's
        // DRAG_END reads this to decide whether its default sendTakeFromChest
        // should fire. Reset in DRAG_END regardless.
        private boolean chestDragDropConsumed;
        private boolean peekActive;
        private long peekPressTimeMs;
        private AtlasCamera peekTarget;
        private boolean searchModalActive;
        private String searchBuffer = "";
        private AtlasCamera searchOrigin;
        private long searchLastKeystrokeMs;
        private boolean searchPreviewPanned;
        private boolean searchInteractionDisablesAutoDismiss;
        private boolean searchCommitted;
        private List<AtlasSearchIndex.SearchRow> searchMatches = List.of();
        private int searchMatchIndex;
        private static final long SEARCH_PREVIEW_DELAY_MS = 220L;
        private static final long SEARCH_COMMIT_DELAY_MS = 3500L;
        private String gatherKitId = "";
        private int gatherStep = 0;

        private Controller(SlotWorkspaceUiSession session, Player player) {
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

        private ModularUI create() {
            registerRpcs();
            installBeltHotkeys();
            root.addChildren(syncBinding(), content);
            rebuildNow();
            return ModularUI.of(UI.of(root), player);
        }

        private void installBeltHotkeys() {
            root.setEnforceFocus(event -> {
            });
            root.addEventListener(UIEvents.MUI_CHANGED, event -> root.focus());
            root.addEventListener(UIEvents.KEY_DOWN, this::handleBeltHotkey, true);
            root.addEventListener(UIEvents.KEY_DOWN, this::handleSearchKeyDown, true);
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
            root.addEventListener(UIEvents.CHAR_TYPED, this::handleSearchCharTyped, true);
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
            root.addEventListener(UIEvents.TICK, event -> tickSearchIdleTimer());
        }

        private void handleBeltHotkey(UIEvent event) {
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

        private void handlePeekKeyDown(UIEvent event) {
            if (event.keyCode != GLFW.GLFW_KEY_SPACE) {
                return;
            }
            if (isTextInputFocused() || searchModalActive) {
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

        private void handlePeekKeyUp(UIEvent event) {
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

        private boolean isTextInputFocused() {
            var mui = root.getModularUI();
            if (mui == null) {
                return false;
            }
            UIElement focused = mui.getFocusedElement();
            return focused != null && focused != root && focused instanceof TextField;
        }

        private void handleCameraHistoryKey(UIEvent event) {
            if (isTextInputFocused() || searchModalActive) {
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

        private void handleCycleKitPageKey(UIEvent event) {
            if (isTextInputFocused() || searchModalActive) {
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

        private void handleUndoRedoKey(UIEvent event) {
            if (isTextInputFocused() || searchModalActive) {
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

        private void sendUndo() {
            if (undoEmitter != null) {
                localStatus.set("undo");
                undoEmitter.send();
            }
        }

        private void sendRedo() {
            if (redoEmitter != null) {
                localStatus.set("redo");
                redoEmitter.send();
            }
        }

        private void handleCameraHistoryMouse(UIEvent event) {
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

        private void performCameraBack() {
            if (!cameraController.back()) {
                localStatus.set("no further camera history");
                rebuild();
            }
        }

        private void performCameraForward() {
            if (!cameraController.forward()) {
                localStatus.set("at latest camera");
                rebuild();
            }
        }

        private void handleSearchCharTyped(UIEvent event) {
            char codePoint = event.codePoint;
            if (codePoint == '/' && !searchModalActive && !peekActive && !isTextInputFocused()) {
                event.stopPropagation();
                openSearchModal();
                return;
            }
            if (!searchModalActive) {
                return;
            }
            if (codePoint == '/') {
                event.stopPropagation();
                if (searchInteractionDisablesAutoDismiss || !searchBuffer.isEmpty()) {
                    closeSearchModal();
                    openSearchModal();
                }
                return;
            }
            if (searchInteractionDisablesAutoDismiss) {
                // Locked in via Tab/Enter — further typing is ignored so the user
                // can browse results without clobbering their query.
                if (codePoint >= 0x20 && codePoint < 0x7F) {
                    event.stopPropagation();
                }
                return;
            }
            if (codePoint >= '0' && codePoint <= '9') {
                return;
            }
            if (codePoint >= 0x20 && codePoint < 0x7F) {
                event.stopPropagation();
                appendSearchBuffer(codePoint);
            }
        }

        private void handleSearchKeyDown(UIEvent event) {
            if (!searchModalActive) {
                return;
            }
            switch (event.keyCode) {
                case GLFW.GLFW_KEY_ESCAPE -> {
                    event.stopPropagation();
                    abortSearch();
                }
                case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
                    event.stopPropagation();
                    searchInteractionDisablesAutoDismiss = true;
                    commitSearch(AtlasCameraController.CommitSource.SEARCH_ENTER, false);
                }
                case GLFW.GLFW_KEY_BACKSPACE -> {
                    event.stopPropagation();
                    if (!searchInteractionDisablesAutoDismiss) {
                        popSearchBuffer();
                    }
                }
                case GLFW.GLFW_KEY_TAB -> {
                    event.stopPropagation();
                    cycleSearchMatch();
                }
                default -> {
                }
            }
        }

        private void openSearchModal() {
            if (!cameraController.hasGraphView()) {
                return;
            }
            searchModalActive = true;
            searchBuffer = "";
            searchOrigin = cameraController.currentCamera();
            searchLastKeystrokeMs = System.currentTimeMillis();
            searchPreviewPanned = false;
            searchInteractionDisablesAutoDismiss = false;
            searchCommitted = false;
            searchMatches = List.of();
            searchMatchIndex = 0;
            searchQuery = "";
            setScreenClosesOnEsc(false);
            rebuild();
        }

        private void appendSearchBuffer(char codePoint) {
            searchBuffer += codePoint;
            searchLastKeystrokeMs = System.currentTimeMillis();
            searchPreviewPanned = false;
            recomputeSearchMatches();
            syncSearchQuery();
            rebuild();
        }

        private void popSearchBuffer() {
            if (searchBuffer.isEmpty()) {
                return;
            }
            searchBuffer = searchBuffer.substring(0, searchBuffer.length() - 1);
            searchLastKeystrokeMs = System.currentTimeMillis();
            searchPreviewPanned = false;
            recomputeSearchMatches();
            syncSearchQuery();
            rebuild();
        }

        private void cycleSearchMatch() {
            if (searchMatches.isEmpty()) {
                return;
            }
            searchMatchIndex = (searchMatchIndex + 1) % searchMatches.size();
            searchPreviewPanned = true;
            searchLastKeystrokeMs = System.currentTimeMillis();
            searchInteractionDisablesAutoDismiss = true;
            easeToCurrentSearchMatch();
            rebuild();
        }

        private void commitSearch(AtlasCameraController.CommitSource source, boolean closeAfter) {
            if (searchMatches.isEmpty()) {
                if (closeAfter) {
                    abortSearch();
                }
                return;
            }
            AtlasCamera origin = searchCommitted ? cameraController.currentCamera() : searchOrigin;
            AtlasCamera target = cameraForSearchMatch(searchMatches.get(searchMatchIndex));
            if (target != null) {
                cameraController.commitFrom(
                        origin,
                        target,
                        source,
                        AtlasCameraController.CUBIC_IN_OUT,
                        AtlasCameraController.COMMIT_DURATION_MS);
                searchCommitted = true;
            }
            searchLastKeystrokeMs = System.currentTimeMillis();
            if (closeAfter) {
                closeSearchModal();
                searchQuery = "";
            }
            rebuild();
        }

        private void abortSearch() {
            AtlasCamera origin = searchOrigin;
            boolean wasCommitted = searchCommitted;
            closeSearchModal();
            if (!wasCommitted && origin != null) {
                cameraController.snap(origin);
            }
            searchQuery = "";
            rebuild();
        }

        private void closeSearchModal() {
            searchModalActive = false;
            searchBuffer = "";
            searchOrigin = null;
            searchMatches = List.of();
            searchMatchIndex = 0;
            searchInteractionDisablesAutoDismiss = false;
            searchCommitted = false;
            setScreenClosesOnEsc(true);
        }

        private void setScreenClosesOnEsc(boolean enabled) {
            com.lowdragmc.lowdraglib2.gui.ui.ModularUI mui = root.getModularUI();
            if (mui != null) {
                mui.shouldCloseOnEsc(enabled);
            }
        }

        private void recomputeSearchMatches() {
            if (searchBuffer.isEmpty()) {
                searchMatches = List.of();
                searchMatchIndex = 0;
                return;
            }
            searchMatches = AtlasSearchIndex.search(collectSearchRows(), searchBuffer);
            searchMatchIndex = 0;
        }

        private List<AtlasSearchIndex.SearchRow> collectSearchRows() {
            ArrayList<AtlasSearchIndex.SearchRow> rows = new ArrayList<>();
            for (SlotWorkspaceViewModel.AtlasItem item : viewModel.atlasItems()) {
                rows.add(new AtlasSearchIndex.SearchRow(
                        item.name(),
                        item.identity().itemId(),
                        AtlasSearchIndex.Pool.PRIMARY,
                        item.carried(),
                        item.x(),
                        item.y(),
                        item.width(),
                        item.height()
                ));
            }
            for (SlotWorkspaceViewModel.AtlasIsland island : viewModel.islands()) {
                rows.add(new AtlasSearchIndex.SearchRow(
                        island.label(),
                        island.islandId(),
                        AtlasSearchIndex.Pool.SECONDARY,
                        island.carriedCount() > 0,
                        island.x(),
                        island.y(),
                        island.width(),
                        island.height()
                ));
            }
            return rows;
        }

        private void easeToCurrentSearchMatch() {
            if (searchMatches.isEmpty()) {
                return;
            }
            AtlasCamera target = cameraForSearchMatch(searchMatches.get(searchMatchIndex));
            if (target != null) {
                cameraController.ease(
                        target,
                        AtlasCameraController.CUBIC_IN_OUT,
                        AtlasCameraController.SEARCH_PREVIEW_DURATION_MS);
            }
        }

        private AtlasCamera cameraForSearchMatch(AtlasSearchIndex.SearchRow row) {
            SlotAtlasGraphView atlas = cameraController.graphView();
            if (atlas == null || row == null) {
                return null;
            }
            float viewportWidth = atlas.getContentWidth();
            float viewportHeight = atlas.getContentHeight();
            if (viewportWidth <= 0f || viewportHeight <= 0f) {
                return null;
            }
            FitCarriedCamera.Camera camera = FitCarriedCamera.fit(
                    FitCarriedCamera.Rect.of(row.targetX(), row.targetY(), row.targetWidth(), row.targetHeight()),
                    viewportWidth,
                    viewportHeight,
                    CARRIED_FIT_MIN_SCALE,
                    CARRIED_FIT_MAX_SCALE,
                    CARRIED_FIT_PADDING_PX
            );
            return camera == null ? null : new AtlasCamera(camera.offsetX(), camera.offsetY(), camera.scale());
        }

        private void syncSearchQuery() {
            searchQuery = searchBuffer;
        }

        private void tickSearchIdleTimer() {
            if (!searchModalActive || searchMatches.isEmpty()) {
                return;
            }
            long idleMs = System.currentTimeMillis() - searchLastKeystrokeMs;
            if (!searchPreviewPanned && idleMs >= SEARCH_PREVIEW_DELAY_MS) {
                searchPreviewPanned = true;
                easeToCurrentSearchMatch();
            }
            if (!searchInteractionDisablesAutoDismiss && idleMs >= SEARCH_COMMIT_DELAY_MS) {
                commitSearch(AtlasCameraController.CommitSource.SEARCH_COMMIT, true);
            }
        }

        private AtlasCamera resolvePeekTarget() {
            SlotAtlasGraphView atlas = cameraController.graphView();
            if (atlas == null) {
                return null;
            }
            if (hoveredHotbarIndex >= 0 && hoveredHotbarIndex < viewModel.hotbarSlots().size()) {
                SlotWorkspaceViewModel.HotbarSlot slot = viewModel.hotbarSlots().get(hoveredHotbarIndex);
                if (slot.occupied()) {
                    SlotWorkspaceViewModel.IdentityRef identity = SlotWorkspaceViewModel.IdentityRef.from(
                            dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(slot.displayStack()));
                    SlotWorkspaceViewModel.AtlasItem item = atlasItemInIslandLayer(identity);
                    if (item != null) {
                        return computeAtlasItemCamera(atlas, item);
                    }
                }
            }
            if (hoveredAtlasIdentity != null) {
                SlotWorkspaceViewModel.AtlasItem item = atlasItemInIslandLayer(hoveredAtlasIdentity);
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
                SlotWorkspaceViewModel.AtlasItem item = atlasItemInIslandLayer(hoveredChestCellIdentity);
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

        private AtlasCamera computeAtlasItemCamera(SlotAtlasGraphView atlas, SlotWorkspaceViewModel.AtlasItem item) {
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

        private int digitFromKeyCode(int keyCode) {
            if (keyCode >= GLFW.GLFW_KEY_1 && keyCode <= GLFW.GLFW_KEY_9) {
                return keyCode - GLFW.GLFW_KEY_1 + 1;
            }
            if (keyCode >= GLFW.GLFW_KEY_KP_1 && keyCode <= GLFW.GLFW_KEY_KP_9) {
                return keyCode - GLFW.GLFW_KEY_KP_1 + 1;
            }
            return 0;
        }

        private void registerRpcs() {
            transferEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    Integer.class,
                    Integer.class,
                    Integer.class,
                    Integer.class,
                    String.class,
                    session::transfer
            ));
            homeEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    Integer.class,
                    Integer.class,
                    session::assignHome
            ));
            createNamedIslandEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    Integer.class,
                    Integer.class,
                    Integer.class,
                    session::createNamedIslandForItem
            ));
            hotbarToAtlasEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    Integer.class,
                    String.class,
                    Integer.class,
                    Integer.class,
                    session::moveHotbarToAtlas
            ));
            moveIslandEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    Integer.class,
                    Integer.class,
                    session::moveIsland
            ));
            moveChestEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    Integer.class,
                    Integer.class,
                    session::moveChest
            ));
            moveStorageZoneEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    Integer.class,
                    Integer.class,
                    session::moveStorageZone
            ));
            relabelChestEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    String.class,
                    session::relabelChest
            ));
            linkChestEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    String.class,
                    session::linkIslandToChest
            ));
            unlinkChestEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    String.class,
                    session::unlinkIslandFromChest
            ));
            depositEmitter = root.addRPCEvent(RPCEventBuilder.simple((Runnable) session::deposit));
            takeAllEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    session::takeAllFromChest
            ));
            renameIslandEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    String.class,
                    session::renameIsland
            ));
            recolorIslandEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    Integer.class,
                    session::recolorIsland
            ));
            setIslandIconEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    session::setIslandIcon
            ));
            deleteIslandEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    session::deleteIsland
            ));
            acceptChipEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    session::acceptChip
            ));
            saveKitEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    session::saveBeltAsKit
            ));
            activateKitEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    session::activateKit
            ));
            deactivateKitEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    (Runnable) session::deactivateKit
            ));
            undoEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    (Runnable) session::performUndo
            ));
            redoEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    (Runnable) session::performRedo
            ));
            deleteKitEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    session::deleteKit
            ));
            switchKitPageEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    Integer.class,
                    session::switchKitPage
            ));
            addKitPageEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    session::addKitPage
            ));
            removeKitPageEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    Integer.class,
                    session::removeKitPage
            ));
            addKitBringEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    session::addKitBring
            ));
            removeKitBringEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    session::removeKitBring
            ));
            setKitSlotIdentityEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    Integer.class,
                    Integer.class,
                    String.class,
                    String.class,
                    String.class,
                    session::setKitSlotIdentity
            ));
            renameKitEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    String.class,
                    session::renameKit
            ));
            duplicateKitEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    session::duplicateKit
            ));
            swapKitSlotsEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    Integer.class,
                    Integer.class,
                    Integer.class,
                    session::swapKitSlots
            ));
            returnHotbarToHomeEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    Integer.class,
                    session::returnHotbarToHome
            ));
            assignHomeToFreeHotbarEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    String.class,
                    String.class,
                    session::assignHomeToFreeHotbar
            ));
            depositCarriedToChestEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    session::depositCarriedToChest
            ));
            depositHotbarToChestEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    Integer.class,
                    String.class,
                    session::depositHotbarToChest
            ));
            takeFromChestEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    Integer.class,
                    session::takeFromChest
            ));
            takeOneFromChestEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    Integer.class,
                    session::takeOneFromChest
            ));
            assignHomeToHotbarOnlyEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    String.class,
                    String.class,
                    session::assignHomeToHotbarOnly
            ));
            assignIdentityToHotbarSlotEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    String.class,
                    String.class,
                    Integer.class,
                    session::assignIdentityToHotbarSlot
            ));
            depositHomeToLinkedChestEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    String.class,
                    String.class,
                    session::depositHomeToLinkedChest
            ));
            depositOneHomeToLinkedChestEmitter = root.addRPCEvent(RPCEventBuilder.simple(
                    String.class,
                    String.class,
                    String.class,
                    session::depositOneHomeToLinkedChest
            ));
        }

        private UIElement syncBinding() {
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

        private void rebuild() {
            rebuildPending = true;
        }

        private void flushRebuildIfPending() {
            if (rebuildPending) {
                rebuildNow();
            }
        }

        private void rebuildNow() {
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

        private boolean contentPopulated;

        private UIElement topRightActionsOverlay() {
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

        private void installKeybindTooltip(
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

        private UIElement body() {
            UIElement body = new UIElement().layout(layout -> layout
                    .widthPercent(100)
                    .flex(1)
                    .gapAll(8)
                    .flexDirection(FlexDirection.ROW));
            body.addChildren(atlasPanel());
            return body;
        }

        private UIElement atlasPanel() {
            if (atlasPanelElement == null) {
                createPersistentAtlasPanel();
            }
            repopulateAtlasPanel();
            return atlasPanelElement;
        }

        private void createPersistentAtlasPanel() {
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
            installAtlasCanvasDropTarget(panel, atlas);
            installAtlasBackgroundDropTarget(atlas);

            atlasView = atlas;
            hoverTrailOverlayElement = hoverTrailOverlay(atlas);
            carriedFreeSlotsChipElement = carriedFreeSlotsChip();
            topRightActionsElement = topRightActionsOverlay();
            atlasPanelElement = panel;
            atlasContentNeedsScreenTick = true;
        }

        private void repopulateAtlasPanel() {
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

            panel.addChildren(atlas, triagePanelOverlay(), beltOverlay());
            panel.addChild(hoverTrailOverlayElement);
            panel.addChild(carriedFreeSlotsChipElement);
            panel.addChild(topRightActionsElement);
            if (searchModalActive) {
                panel.addChild(searchChipOverlay());
            } else {
                panel.addChild(searchHintOverlay());
            }
            if (kitRackOpen) {
                panel.addChild(kitRackOverlay());
            }
            UIElement contextMenu = contextMenuOverlay();
            if (contextMenu != null) {
                panel.addChild(contextMenu);
            }
            UIElement editPopover = islandEditPopover();
            if (editPopover != null) {
                panel.addChild(editPopover);
            }
            UIElement createPopover = createIslandPopover();
            if (createPopover != null) {
                panel.addChild(createPopover);
            }
            UIElement linkPopover = chestLinkPopover();
            if (linkPopover != null) {
                panel.addChild(linkPopover);
            }
        }

        private UIElement beltOverlay() {
            UIElement overlay = beltPanel();
            overlay.layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(0)
                    .right(0)
                    .bottom(4)
                    .height(BELT_HEIGHT));
            overlay.style(style -> style.zIndex(6));
            return overlay;
        }

        private UIElement searchChipOverlay() {
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

            String bufferDisplay = "/" + searchBuffer + "_";
            chip.addChild(label(bufferDisplay, ACCENT).layout(layout -> layout.height(12)));

            String summary;
            if (searchBuffer.length() < AtlasSearchIndex.DEFAULT_MIN_QUERY_CHARS) {
                summary = "Type " + AtlasSearchIndex.DEFAULT_MIN_QUERY_CHARS
                        + "+ chars  ·  Esc to close";
            } else if (searchMatches.isEmpty()) {
                summary = "No matches  ·  Esc to close";
            } else {
                String commitHint = searchInteractionDisablesAutoDismiss
                        ? "Esc to close"
                        : "idle auto-commits  ·  Esc to abort";
                summary = (searchMatchIndex + 1) + " of " + searchMatches.size()
                        + " matches  ·  Tab cycle  ·  Enter commit  ·  " + commitHint;
            }
            Label summaryLabel = wrappedLabel(summary, MUTED);
            summaryLabel.layout(layout -> layout.widthPercent(100).flex(1));
            chip.addChild(summaryLabel);
            return chip;
        }

        private UIElement searchHintOverlay() {
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

        private static final int CARRIED_CHIP_WIDTH = 96;
        private static final int CARRIED_CHIP_HEIGHT = 20;
        private static final int CARRIED_CHIP_BAR_HEIGHT = 3;

        private UIElement carriedFreeSlotsChip() {
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

        private static String formatFreeSlots(int count) {
            return Component.translatable("slot.screen.inventory.free_slots", count).getString();
        }

        private static int fullnessColor(int filled, int capacity) {
            if (capacity <= 0) {
                return CARRIED_CHIP_WARN;
            }
            float ratio = Math.min(1f, Math.max(0f, (float) filled / capacity));
            if (ratio >= 1f) {
                return CARRIED_CHIP_DANGER;
            }
            if (ratio >= 0.75f) {
                return CARRIED_CHIP_WARN;
            }
            return CARRIED_CHIP_OK;
        }

        private static float fullnessBarWidth(int filled, int capacity, float total) {
            if (capacity <= 0 || filled <= 0) {
                return 0f;
            }
            float ratio = Math.min(1f, (float) filled / capacity);
            return Math.max(0f, total * ratio);
        }

        private UIElement triagePanelOverlay() {
            // Clear whichever top-left overlay is showing: the compact "Press / to search"
            // hint (~20 px tall) or the full search modal (~60 px tall at top 10, left 10).
            // Kit rack, when open, docks above the belt and extends rightward from x=16;
            // triage at (x=8, width=152) horizontally overlaps it — lift the bottom above
            // the rack so the two don't visually stack.
            int triageTop = searchModalActive ? 78 : 36;
            int baseBottom = BELT_HEIGHT + 12;
            int rackBottom = kitRackOpen ? baseBottom + kitRackHeight() + 4 : baseBottom;
            UIElement overlay = panel(GLASS).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(8)
                    .top(triageTop)
                    .bottom(rackBottom)
                    .width(TRIAGE_PANEL_WIDTH)
                    .paddingAll(6)
                    .gapAll(4)
                    .flexDirection(FlexDirection.COLUMN));
            overlay.style(style -> style.zIndex(7));
            overlay.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());

            int triageCount = viewModel.triageItems().size();
            UIElement headerRow = new UIElement().layout(layout -> layout
                    .widthPercent(100)
                    .height(14)
                    .gapAll(4)
                    .alignItems(AlignItems.CENTER)
                    .flexDirection(FlexDirection.ROW));
            headerRow.addChildren(
                    label("Triage", ACCENT).layout(layout -> layout.flex(1).height(12)),
                    label(String.valueOf(triageCount), MUTED).layout(layout -> layout.width(24).height(12))
            );
            headerRow.setAllowHitTest(false);
            overlay.addChild(headerRow);

            Label sortHint = label("most recent first \u2193", MUTED);
            sortHint.layout(layout -> layout.widthPercent(100).height(8));
            sortHint.textStyle(style -> style
                    .textColor(MUTED)
                    .fontSize(6)
                    .textShadow(false)
                    .textAlignHorizontal(Horizontal.LEFT)
                    .textAlignVertical(Vertical.CENTER));
            sortHint.setAllowHitTest(false);
            overlay.addChild(sortHint);

            UIElement divider = panel(ISLAND_BORDER).layout(layout -> layout.widthPercent(100).height(1));
            divider.setAllowHitTest(false);
            overlay.addChild(divider);

            if (triageCount == 0) {
                Label empty = label("Inbox is empty.", MUTED);
                empty.layout(layout -> layout.widthPercent(100).height(24));
                empty.textStyle(style -> style
                        .textColor(MUTED)
                        .fontSize(7)
                        .textShadow(false)
                        .textAlignHorizontal(Horizontal.LEFT)
                        .textAlignVertical(Vertical.CENTER));
                empty.setAllowHitTest(false);
                overlay.addChild(empty);
            } else {
                ScrollerView scroller = new ScrollerView();
                scroller.layout(layout -> layout.flex(1).widthPercent(100).gapAll(2));
                // LDLib's default per-tick wheel cap is 7px; at our 20px row height that
                // feels glacial. Bump both min + max so each wheel tick scrolls roughly
                // 3 rows.
                scroller.scrollerStyle(style -> style
                        .minScrollPixel(30f)
                        .maxScrollPixel(80f));
                for (SlotWorkspaceViewModel.AtlasItem item : viewModel.triageItems()) {
                    scroller.addScrollViewChild(triagePanelRow(item));
                    for (ChipSuggestion chip : item.chipSuggestions()) {
                        scroller.addScrollViewChild(triagePanelChip(item, chip));
                    }
                }
                overlay.addChild(scroller);
            }

            installTriagePanelDropTarget(overlay);
            return overlay;
        }

        private int triageRowChromeColor(SlotWorkspaceViewModel.AtlasItem item, boolean selected) {
            if (selected) {
                return SELECTED;
            }
            return item.recent() ? ROW_MATCH : ROW;
        }

        private UIElement triagePanelRow(SlotWorkspaceViewModel.AtlasItem item) {
            Button row = button("", true, triageRowChromeColor(item, item.identity().equals(selectedAtlasIdentity.get())));
            row.noText();
            atlasContentSubscriptions.add(selectedAtlasIdentity.subscribeLater(sel -> {
                applyButtonColors(row, true, triageRowChromeColor(item, item.identity().equals(sel)));
            }));
            boolean[] lastAccent = {false};
            row.addEventListener(UIEvents.TICK, event -> {
                boolean accent = shouldAccentTriageRow(item);
                if (accent != lastAccent[0]) {
                    row.style(style -> style.overlayTexture(accent ? rect(HOVER_ACCENT_OVERLAY) : IGuiTexture.EMPTY));
                    lastAccent[0] = accent;
                }
            });
            row.layout(layout -> layout
                    .widthPercent(100)
                    .height(20)
                    .paddingHorizontal(4)
                    .gapAll(4)
                    .alignItems(AlignItems.CENTER)
                    .flexDirection(FlexDirection.ROW));
            row.setOnClick(event -> {
                event.stopPropagation();
                if (event.button == 0 && Screen.hasShiftDown()) {
                    int hotbarIndex = hotbarSlotForIdentity(item.identity());
                    if (hotbarIndex >= 0) {
                        sendReturnHotbarToHome(hotbarIndex);
                    } else {
                        localStatus.set(item.name() + " is not in the hotbar");
                        rebuild();
                    }
                    return;
                }
                selectedAtlasIdentity.set(item.identity());
                selectedHotbarIndex.set(-1);
                localStatus.set("selected inbox item: drag to an island, click an island, or accept a chip");
            });
            row.addEventListener(UIEvents.MOUSE_ENTER, event -> hoveredAtlasIdentity = item.identity(), true);
            row.addEventListener(UIEvents.MOUSE_LEAVE, event -> {
                if (item.identity().equals(hoveredAtlasIdentity)) {
                    hoveredAtlasIdentity = null;
                }
            }, true);
            installAtlasItemDragSource(row, item);
            installAtlasHoverTooltip(row, item);

            UIElement icon = itemIcon(item.displayStack(), 16, item.carried());
            icon.layout(layout -> layout.width(16).height(16));
            row.addChild(icon);

            // Item count is already drawn on the icon corner by the vanilla item
            // renderer, so we only need the name. Let LDLib handle any soft clip via
            // the flex(1) layout — we were over-shortening with a hard 22-char cap.
            Label name = label(item.name(), TEXT);
            name.layout(layout -> layout.flex(1).height(12));
            name.textStyle(style -> style
                    .textColor(item.carried() ? TEXT : MUTED)
                    .fontSize(7)
                    .textShadow(false)
                    .textAlignHorizontal(Horizontal.LEFT)
                    .textAlignVertical(Vertical.CENTER));
            name.setAllowHitTest(false);
            row.addChild(name);
            return row;
        }

        private UIElement triagePanelChip(SlotWorkspaceViewModel.AtlasItem item, ChipSuggestion chip) {
            // Wrapper with a left indent so the chip visibly nests under its parent
            // item instead of spanning the full panel width like a divider.
            UIElement wrapper = new UIElement().layout(layout -> layout
                    .widthPercent(100)
                    .height(11)
                    .paddingLeft(20)
                    .flexDirection(FlexDirection.ROW)
                    .alignItems(AlignItems.CENTER));
            wrapper.setAllowHitTest(false);

            Button chipButton = button("", true, chip.color());
            chipButton.noText();
            chipButton.layout(layout -> layout
                    .flex(1)
                    .height(11)
                    .paddingHorizontal(4)
                    .gapAll(2)
                    .alignItems(AlignItems.CENTER)
                    .flexDirection(FlexDirection.ROW));
            chipButton.setOnClick(event -> {
                event.stopPropagation();
                sendChipAccept(item, chip);
            });
            // Leading glyph signals "assign to" direction and visually anchors the
            // chip to the item row above. Plain ASCII so it renders in the Inter
            // Tight font without the Unicode-arrow fallback issues.
            Label chipLabel = label("> " + chipLabelText(chip), TEXT);
            chipLabel.layout(layout -> layout.flex(1).height(9));
            chipLabel.textStyle(style -> style
                    .textColor(TEXT)
                    .fontSize(6)
                    .textShadow(false)
                    .textAlignHorizontal(Horizontal.LEFT)
                    .textAlignVertical(Vertical.CENTER));
            chipLabel.setAllowHitTest(false);
            chipButton.addChild(chipLabel);
            wrapper.addChild(chipButton);
            return wrapper;
        }

        private void installTriagePanelDropTarget(UIElement target) {
            target.addEventListener(UIEvents.DRAG_ENTER, event -> updateGenericDropOverlay(target, isTriagePanelDropAcceptable(event), WARNING), true);
            target.addEventListener(UIEvents.DRAG_UPDATE, event -> updateGenericDropOverlay(target, isTriagePanelDropAcceptable(event), WARNING));
            target.addEventListener(UIEvents.DRAG_LEAVE, event -> clearDropOverlay(target), true);
            target.addEventListener(UIEvents.DRAG_PERFORM, event -> {
                clearDropOverlay(target);
                AtlasItemDrag atlasItem = atlasItemDrag(event);
                if (atlasItem != null) {
                    sendAssignHome(
                            atlasItem.identity(),
                            SlotWorkspaceAtlasLayout.ISLAND_TRIAGE,
                            0,
                            0
                    );
                    event.stopPropagation();
                    return;
                }
                HotbarSlotDrag hotbarItem = hotbarSlotDrag(event);
                if (hotbarItem != null) {
                    if (hotbarDragHasHome(hotbarItem)) {
                        sendReturnHotbarToHome(hotbarItem.hotbarIndex());
                    } else {
                        sendMoveHotbarToAtlas(
                                hotbarItem.hotbarIndex(),
                                SlotWorkspaceAtlasLayout.ISLAND_TRIAGE,
                                0,
                                0
                        );
                    }
                    event.stopPropagation();
                }
            });
        }

        private boolean isTriagePanelDropAcceptable(UIEvent event) {
            return atlasItemDrag(event) != null || hotbarSlotDrag(event) != null;
        }

        private void buildAtlas(SlotAtlasGraphView atlas) {
            StorageZoneBounds bounds = storageZoneBounds();
            if (bounds != null) {
                atlas.addContentChild(storageZoneBackdrop(bounds));
                atlas.addContentChild(storageZoneHeader(bounds, atlas));
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
                    addLinkAffordances(atlas, tile, island);
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
                        UIElement dimThread = dimLinkThread(tile, hovered);
                        if (dimThread != null) {
                            atlas.addContentChild(dimThread);
                        }
                    }
                }
            }
            Set<String> highlightedIslandIds = highlightedIslandIdsFromProximateTiles();
            for (SlotWorkspaceViewModel.AtlasIsland island : viewModel.islands()) {
                UIElement islandPanelEl = islandPanel(atlas, island);
                atlas.addContentChild(islandPanelEl);
                atlas.addContentChild(islandTitleBar(atlas, island, islandPanelEl));
                if (island.carriedCount() > 0) {
                    atlas.addContentChild(islandCarriedBadge(atlas, island));
                }
            }
            for (String islandId : highlightedIslandIds) {
                SlotWorkspaceViewModel.AtlasIsland island = viewModel.island(islandId);
                if (island != null) {
                    addIslandHighlightFrame(atlas, island);
                }
            }
            for (SlotWorkspaceViewModel.ClaimedChestTile tile : viewModel.claimedChestTiles()) {
                atlas.addContentChild(chestTilePanel(atlas, tile));
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

        private UIElement islandPanel(SlotAtlasGraphView atlas, SlotWorkspaceViewModel.AtlasIsland island) {
            UIElement panel = panel(island.color()).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(island.x())
                    .top(island.y())
                    .width(island.width())
                    .height(island.height())
                    .paddingAll(8)
                    .gapAll(3)
                    .flexDirection(FlexDirection.COLUMN));
            // zIndex in LDLib2 only affects hit-test priority (see
            // UIElement.getSortedChildren, used by UIEventDispatcher) — the
            // draw order is child-insertion order. Give the panel a zIndex
            // matching the chest tile panel (1) so a right-click lands on
            // the island body before falling through to the atlas viewport.
            panel.style(style -> style.zIndex(1));

            installViewportPanSurface(panel, atlas);
            installIslandDropTarget(panel, panel, atlas, island);

            // Right-click opens the island edit popover anchored near the click,
            // matching how item cards and kit cards surface their context menu.
            if (island.kind() == VisualAtlasIslandKind.PLAYER) {
                panel.addEventListener(UIEvents.MOUSE_DOWN, event -> {
                    if (event.button != 1) {
                        return;
                    }
                    event.stopPropagation();
                    beginIslandEdit(island, event.x, event.y);
                }, true);
            }

            attachIslandHoverListeners(panel, island);

            return panel;
        }

        /**
         * Install MOUSE_ENTER/LEAVE listeners on any atlas-level element that
         * belongs visually to an island (panel, header, item cards, badge)
         * so hovering ANY of them surfaces the dim non-proximate link threads.
         * Atlas content is flat — these elements are siblings, not nested —
         * so each one needs its own listener to flip {@code hoveredIslandId}.
         * No-op for non-player islands and islands without non-proximate links.
         */
        private void attachIslandHoverListeners(UIElement element, SlotWorkspaceViewModel.AtlasIsland island) {
            if (element == null || island == null) {
                return;
            }
            if (island.kind() != VisualAtlasIslandKind.PLAYER) {
                return;
            }
            if (!islandHasNonProximateLinks(island.islandId())) {
                return;
            }
            String islandId = island.islandId();
            element.addEventListener(UIEvents.MOUSE_ENTER, event -> {
                if (!islandId.equals(hoveredIslandId)) {
                    hoveredIslandId = islandId;
                    rebuild();
                }
            }, true);
            element.addEventListener(UIEvents.MOUSE_LEAVE, event -> {
                if (islandId.equals(hoveredIslandId)) {
                    hoveredIslandId = null;
                    rebuild();
                }
            }, true);
        }

        private boolean islandHasNonProximateLinks(String islandId) {
            if (islandId == null || islandId.isBlank()) {
                return false;
            }
            for (SlotWorkspaceViewModel.ClaimedChestTile tile : viewModel.claimedChestTiles()) {
                if (tile.proximate()) {
                    continue;
                }
                if (tile.linkedIslandIds().contains(islandId)) {
                    return true;
                }
            }
            return false;
        }

        private UIElement islandCarriedBadge(SlotAtlasGraphView atlas, SlotWorkspaceViewModel.AtlasIsland island) {
            // Positioned on the title-bar strip above the island panel so it
            // doesn't overlap the first row of item cards. Uses world-unit
            // absolute positioning against atlas content like every other
            // atlas-level element.
            Button badge = button(island.carriedCount() + "●", true, ACTIVE_HOTBAR);
            badge.layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(island.x() + 2)
                    .top(island.y() - 14)
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
                panToIsland(atlas, island);
                localStatus.set("panned to " + island.label());
            });
            return badge;
        }

        private UIElement islandTitleBar(
                SlotAtlasGraphView atlas,
                SlotWorkspaceViewModel.AtlasIsland island,
                UIElement islandPanelEl
        ) {
            Button header = button(island.label(), true, island.color());
            header.layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(island.x())
                    .top(island.y() - 16)
                    .width(island.width())
                    .height(14));
            header.style(style -> style.zIndex(3));
            header.addEventListener(UIEvents.CLICK, event -> {
                if (event.button != 0) {
                    return;
                }
                event.stopPropagation();
                if (selectedAtlasItem() == null) {
                    localStatus.set("select a triage or homed item first");
                    return;
                }
                sendAssignHome(island.islandId());
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
                    beginIslandEdit(island, event.x, event.y);
                }, true);
            }
            installIslandDragSource(header, atlas, island);
            installIslandDropTarget(header, islandPanelEl, atlas, island);
            attachIslandHoverListeners(header, island);

            float[] lastScale = {Float.NaN};
            int[] lastWorldFontQuarter = {-1};
            Runnable applyHeaderScale = () -> {
                // Track the actual render scale (not animation target) so the
                // header stays sized to the live view each frame. Using
                // animationTargetScale caused the header to be sized for the
                // final scale while the pose stack drew at the interpolated
                // current scale — visible as a flash at animation boundaries.
                float scale = Math.max(0.0001f, atlas.getScale());
                if (scale == lastScale[0]) {
                    return;
                }
                lastScale[0] = scale;
                float islandScreenWidth = island.width() * scale;
                float requestedFontPx = Math.min(12f, islandScreenWidth * 0.13f);
                float screenFontPx = headerBreakpointFontPx(Math.max(7f, requestedFontPx));
                float worldFontPx = screenFontPx / Math.max(0.0001f, scale);
                float screenHeaderHeight = screenFontPx + 3f;
                // Floor the world height at the carried-count badge's world
                // size (12 world units plus a 2-unit margin = 14) so the badge
                // never overflows the header background. Without this, at
                // scale > ~1 the screen-fixed header shrinks in world space
                // below the badge's world size and the counter visibly
                // escapes its backdrop.
                float worldHeaderHeight = Math.max(14f, screenHeaderHeight / Math.max(0.0001f, scale));
                float screenGap = 2f;
                float worldGap = screenGap / Math.max(0.0001f, scale);

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
                        .left(island.x())
                        .top(Math.round(island.y() - worldHeaderHeight - worldGap))
                        .width(island.width())
                        .height(Math.round(worldHeaderHeight)));
                header.markTaffyStyleDirty();
            };
            // Prime at build time so the first rendered frame after a rebuild
            // already has the scale-correct font/layout. Without this, the
            // header renders at Button's default fontSize until the next
            // screen tick fires — which, during rapid rebuilds from scroll-
            // wheel transfer, could be several frames of flicker.
            applyHeaderScale.run();
            header.addEventListener(UIEvents.TICK, event -> applyHeaderScale.run());
            return header;
        }

        private StorageZoneBounds storageZoneBounds() {
            List<SlotWorkspaceViewModel.ClaimedChestTile> tiles = viewModel.claimedChestTiles();
            if (tiles.isEmpty()) {
                return null;
            }
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;
            for (SlotWorkspaceViewModel.ClaimedChestTile tile : tiles) {
                minX = Math.min(minX, tile.atlasX());
                minY = Math.min(minY, tile.atlasY());
                maxX = Math.max(maxX, tile.atlasX() + tile.width());
                maxY = Math.max(maxY, tile.atlasY() + tile.height());
            }
            int pad = SlotWorkspaceAtlasLayout.STORAGE_ZONE_PADDING;
            return new StorageZoneBounds(
                    minX - pad,
                    minY - pad,
                    (maxX - minX) + pad * 2,
                    (maxY - minY) + pad * 2
            );
        }

        private UIElement storageZoneBackdrop(StorageZoneBounds bounds) {
            UIElement backdrop = panel(STORAGE_ZONE_FILL).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(bounds.left())
                    .top(bounds.top())
                    .width(bounds.width())
                    .height(bounds.height()));
            backdrop.style(style -> style.zIndex(0));
            backdrop.setAllowHitTest(false);
            return backdrop;
        }

        private UIElement storageZoneHeader(StorageZoneBounds bounds, SlotAtlasGraphView atlas) {
            int headerHeight = STORAGE_ZONE_HEADER_HEIGHT;
            UIElement header = panel(STORAGE_ZONE_HEADER_FILL).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(bounds.left())
                    .top(bounds.top() - headerHeight)
                    .width(bounds.width())
                    .height(headerHeight)
                    .paddingHorizontal(8)
                    .alignItems(AlignItems.CENTER)
                    .flexDirection(FlexDirection.ROW));
            header.style(style -> style.zIndex(1));
            Label title = label("Storage", ACCENT);
            title.layout(layout -> layout.flex(1).height(headerHeight));
            title.textStyle(style -> style
                    .textColor(ACCENT)
                    .textShadow(false)
                    .fontSize(8)
                    .textAlignHorizontal(Horizontal.LEFT)
                    .textAlignVertical(Vertical.CENTER));
            title.setAllowHitTest(false);
            header.addChild(title);
            installStorageZoneDragSource(header, atlas, bounds);
            return header;
        }

        private void installStorageZoneDragSource(UIElement source, SlotAtlasGraphView atlas, StorageZoneBounds bounds) {
            int[] clickWorldX = {Integer.MIN_VALUE};
            int[] clickWorldY = {Integer.MIN_VALUE};
            source.addEventListener(UIEvents.MOUSE_DOWN, event -> {
                if (event.button != 0) {
                    return;
                }
                clickWorldX[0] = atlas.worldX(event.x);
                clickWorldY[0] = atlas.worldY(event.y);
            });
            source.addEventListener(UIEvents.MOUSE_UP, event -> {
                clickWorldX[0] = Integer.MIN_VALUE;
                clickWorldY[0] = Integer.MIN_VALUE;
            });
            source.addEventListener(UIEvents.MOUSE_MOVE, event -> {
                if (clickWorldX[0] == Integer.MIN_VALUE) {
                    return;
                }
                if (!source.isMouseDown(0) || isDragging(source)) {
                    return;
                }
                float scale = atlas.getScale();
                float screenDx = (atlas.worldX(event.x) - clickWorldX[0]) * scale;
                float screenDy = (atlas.worldY(event.y) - clickWorldY[0]) * scale;
                if (screenDx * screenDx + screenDy * screenDy < DRAG_START_THRESHOLD_PX * DRAG_START_THRESHOLD_PX) {
                    return;
                }
                int grabOffsetX = clickWorldX[0] - bounds.left();
                int grabOffsetY = clickWorldY[0] - bounds.top();
                int widthPx = Math.max(48, atlas.screenPixelsForWorldUnits(bounds.width()));
                int heightPx = Math.max(20, atlas.screenPixelsForWorldUnits(bounds.height() + STORAGE_ZONE_HEADER_HEIGHT));
                int dragOffsetX = Math.round(grabOffsetX * scale);
                int dragOffsetY = Math.round((grabOffsetY + STORAGE_ZONE_HEADER_HEIGHT) * scale);
                source.startDrag(
                        new StorageZoneDrag(grabOffsetX, grabOffsetY, bounds.left(), bounds.top()),
                        rect((STORAGE_ZONE_FILL & 0x00FFFFFF) | 0x60000000)
                ).setDragTexture(-dragOffsetX, -dragOffsetY, widthPx, heightPx);
                localStatus.set("moving storage zone");
            });
            source.addEventListener(UIEvents.DRAG_END, event -> handleDragEnd(event));
        }

        private UIElement chestTilePanel(SlotAtlasGraphView atlas, SlotWorkspaceViewModel.ClaimedChestTile tile) {
            int fill = tile.proximate() ? STORAGE_TILE_FILL : STORAGE_TILE_FILL_DIM;
            int textColor = tile.proximate() ? TEXT : MUTED;
            int cellSize = SlotWorkspaceAtlasLayout.CHEST_TILE_CELL;
            int cols = SlotWorkspaceAtlasLayout.CHEST_TILE_COLUMNS;
            int padding = SlotWorkspaceAtlasLayout.CHEST_TILE_PADDING;
            int headerHeight = SlotWorkspaceAtlasLayout.CHEST_TILE_HEADER_HEIGHT;

            UIElement panel = panel(fill).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(tile.atlasX())
                    .top(tile.atlasY())
                    .width(tile.width())
                    .height(tile.height())
                    .paddingAll(0));
            panel.style(style -> style.zIndex(1));

            Label header = label(tile.label(), textColor);
            header.layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(padding)
                    .top(0)
                    .width(tile.width() - padding * 2)
                    .height(headerHeight));
            header.textStyle(style -> style
                    .textColor(textColor)
                    .textShadow(false)
                    .fontSize(8)
                    .textAlignHorizontal(Horizontal.LEFT)
                    .textAlignVertical(Vertical.CENTER));
            panel.addChild(header);

            int cellsToRender = tile.contents().size();
            List<Integer> contentIndices = tile.contentSlotIndices();
            for (int index = 0; index < cellsToRender; index++) {
                ItemStack stack = tile.contents().get(index);
                int chestSlotIndex = index < contentIndices.size() ? contentIndices.get(index) : index;
                int col = index % cols;
                int row = index / cols;
                int cellX = padding + col * cellSize;
                int cellY = headerHeight + row * cellSize;
                UIElement cell = chestTileCell(stack, tile.proximate(), cellSize);
                cell.layout(layout -> layout
                        .positionType(TaffyPosition.ABSOLUTE)
                        .left(cellX)
                        .top(cellY)
                        .width(cellSize)
                        .height(cellSize));
                if (stack != null && !stack.isEmpty()) {
                    SlotWorkspaceViewModel.IdentityRef cellIdentity = SlotWorkspaceViewModel.IdentityRef.from(
                            dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(stack));
                    String cellStorageId = tile.storageId();
                    cell.addEventListener(UIEvents.MOUSE_ENTER, event -> {
                        hoveredChestCellIdentity = cellIdentity;
                        hoveredChestCellStorageId = cellStorageId;
                    }, true);
                    cell.addEventListener(UIEvents.MOUSE_LEAVE, event -> {
                        if (cellIdentity.equals(hoveredChestCellIdentity)
                                && cellStorageId.equals(hoveredChestCellStorageId)) {
                            hoveredChestCellIdentity = null;
                            hoveredChestCellStorageId = null;
                        }
                    }, true);
                }
                if (tile.proximate() && stack != null && !stack.isEmpty()) {
                    String storageId = tile.storageId();
                    ItemStack cellStack = stack;
                    cell.addEventListener(UIEvents.CLICK, event -> {
                        if (event.button != 0) {
                            return;
                        }
                        event.stopPropagation();
                        if (Screen.hasShiftDown()) {
                            sendTakeFromChest(storageId, chestSlotIndex);
                            return;
                        }
                        SlotWorkspaceViewModel.IdentityRef identityRef = SlotWorkspaceViewModel.IdentityRef.from(
                                dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(cellStack));
                        selectedAtlasIdentity.set(identityRef);
                        selectedHotbarIndex.set(-1);
                        localStatus.set("selected " + cellStack.getHoverName().getString());
                    });
                    installChestStackDragSource(cell, atlas, storageId, chestSlotIndex, cellStack, tile.label());
                }
                panel.addChild(cell);
            }

            Button linkButton = button("Link", true, tile.linkedIslandIds().isEmpty() ? PANEL_ALT : ACCENT);
            linkButton.layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .right(padding)
                    .top(0)
                    .width(28)
                    .height(headerHeight));
            linkButton.textStyle(style -> style
                    .textColor(TEXT)
                    .textShadow(false)
                    .fontSize(7)
                    .textAlignHorizontal(Horizontal.CENTER)
                    .textAlignVertical(Vertical.CENTER));
            linkButton.style(style -> style.zIndex(3));
            linkButton.setOnClick(event -> {
                event.stopPropagation();
                beginChestLinkEdit(tile);
            });
            panel.addChild(linkButton);

            boolean canTake = tile.proximate() && !tile.contents().isEmpty();
            Button takeAllButton = button("Take", canTake, canTake ? ROW : PANEL_ALT);
            takeAllButton.layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .right(padding + 30)
                    .top(0)
                    .width(28)
                    .height(headerHeight));
            takeAllButton.textStyle(style -> style
                    .textColor(canTake ? TEXT : MUTED)
                    .textShadow(false)
                    .fontSize(7)
                    .textAlignHorizontal(Horizontal.CENTER)
                    .textAlignVertical(Vertical.CENTER));
            takeAllButton.style(style -> style.zIndex(3));
            String takeStorageId = tile.storageId();
            takeAllButton.setOnClick(event -> {
                event.stopPropagation();
                if (!canTake) {
                    localStatus.set(tile.proximate() ? "chest is empty" : "chest is too far");
                    rebuild();
                    return;
                }
                sendTakeAll(takeStorageId);
            });
            panel.addChild(takeAllButton);

            installChestTileDragSource(panel, atlas, tile);
            installChestTileDropTarget(panel, tile);
            return panel;
        }

        private void installChestTileDropTarget(UIElement target, SlotWorkspaceViewModel.ClaimedChestTile tile) {
            String storageId = tile.storageId();
            boolean proximate = tile.proximate();
            target.addEventListener(UIEvents.DRAG_ENTER, event -> updateChestTileDropOverlay(target, proximate, event), true);
            target.addEventListener(UIEvents.DRAG_UPDATE, event -> updateChestTileDropOverlay(target, proximate, event));
            target.addEventListener(UIEvents.DRAG_LEAVE, event -> clearDropOverlay(target), true);
            target.addEventListener(UIEvents.DRAG_PERFORM, event -> {
                clearDropOverlay(target);
                AtlasItemDrag atlasDrag = atlasItemDrag(event);
                HotbarSlotDrag hotbarDrag = hotbarSlotDrag(event);
                if (atlasDrag == null && hotbarDrag == null) {
                    return;
                }
                if (!proximate) {
                    localStatus.set("chest is too far");
                    rebuild();
                    event.stopPropagation();
                    return;
                }
                if (atlasDrag != null) {
                    sendDepositCarriedToChest(atlasDrag.identity(), storageId);
                } else {
                    sendDepositHotbarToChest(hotbarDrag.hotbarIndex(), storageId);
                }
                event.stopPropagation();
            });
        }

        private void updateChestTileDropOverlay(UIElement target, boolean proximate, UIEvent event) {
            boolean acceptable = atlasItemDrag(event) != null || hotbarSlotDrag(event) != null;
            updateGenericDropOverlay(target, acceptable, proximate ? ACCENT : WARNING);
        }

        private void sendDepositCarriedToChest(SlotWorkspaceViewModel.IdentityRef identity, String storageId) {
            if (depositCarriedToChestEmitter == null || identity == null || storageId == null || storageId.isBlank()) {
                return;
            }
            boolean sent = depositCarriedToChestEmitter.send(
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

        private void sendDepositHotbarToChest(int hotbarIndex, String storageId) {
            if (depositHotbarToChestEmitter == null || storageId == null || storageId.isBlank()) {
                return;
            }
            boolean sent = depositHotbarToChestEmitter.send(hotbarIndex, storageId);
            if (!sent) {
                localStatus.set("deposit unavailable");
                rebuild();
            }
        }

        private void sendTakeFromChest(String storageId, int chestSlotIndex) {
            if (takeFromChestEmitter == null || storageId == null || storageId.isBlank()) {
                return;
            }
            boolean sent = takeFromChestEmitter.send(storageId, chestSlotIndex);
            if (!sent) {
                localStatus.set("take unavailable");
                rebuild();
            }
        }

        private Set<String> highlightedIslandIdsFromProximateTiles() {
            LinkedHashSet<String> highlighted = new LinkedHashSet<>();
            for (SlotWorkspaceViewModel.ClaimedChestTile tile : viewModel.claimedChestTiles()) {
                if (tile.proximate()) {
                    highlighted.addAll(tile.linkedIslandIds());
                }
            }
            return highlighted;
        }

        private void addIslandHighlightFrame(SlotAtlasGraphView atlas, SlotWorkspaceViewModel.AtlasIsland island) {
            int thickness = LINK_HIGHLIGHT_THICKNESS;
            int color = LINK_HIGHLIGHT_COLOR;
            int x = island.x();
            int y = island.y();
            int w = island.width();
            int h = island.height();
            atlas.addContentChild(highlightFrameSegment(color, x - thickness, y - thickness, w + thickness * 2, thickness));
            atlas.addContentChild(highlightFrameSegment(color, x - thickness, y + h, w + thickness * 2, thickness));
            atlas.addContentChild(highlightFrameSegment(color, x - thickness, y, thickness, h));
            atlas.addContentChild(highlightFrameSegment(color, x + w, y, thickness, h));
        }

        private UIElement highlightFrameSegment(int color, int x, int y, int w, int h) {
            UIElement segment = panel(color).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(x)
                    .top(y)
                    .width(w)
                    .height(h));
            segment.style(style -> style.zIndex(5));
            segment.setAllowHitTest(false);
            return segment;
        }

        private void addLinkAffordances(
                SlotAtlasGraphView atlas,
                SlotWorkspaceViewModel.ClaimedChestTile tile,
                SlotWorkspaceViewModel.AtlasIsland island
        ) {
            UIElement thread = linkThread(tile, island);
            if (thread != null) {
                atlas.addContentChild(thread);
            }

            float tileCx = tile.atlasX() + tile.width() / 2f;
            float tileCy = tile.atlasY() + tile.height() / 2f;
            float islandCx = island.x() + island.width() / 2f;
            float islandCy = island.y() + island.height() / 2f;
            float dx = islandCx - tileCx;
            float dy = islandCy - tileCy;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            if (distance < 1f) {
                return;
            }
            float cosA = dx / distance;
            float sinA = dy / distance;
            float angleDeg = (float) Math.toDegrees(Math.atan2(dy, dx));

            float tileEdge = rectEdgeAlongDirection(tile.width(), tile.height(), cosA, sinA);
            float tileArrowX = tileCx + (tileEdge + 6f) * cosA;
            float tileArrowY = tileCy + (tileEdge + 6f) * sinA;
            atlas.addContentChild(linkArrow(tileArrowX, tileArrowY, angleDeg, () -> {
                panToIsland(atlas, island);
                localStatus.set("linked island: " + island.label());
            }));

            float islandEdge = rectEdgeAlongDirection(island.width(), island.height(), cosA, sinA);
            float islandArrowX = islandCx - (islandEdge + 6f) * cosA;
            float islandArrowY = islandCy - (islandEdge + 6f) * sinA;
            atlas.addContentChild(linkArrow(islandArrowX, islandArrowY, angleDeg + 180f, () -> {
                panToChestTile(atlas, tile);
                localStatus.set("linked chest: " + tile.label());
            }));
        }

        private float rectEdgeAlongDirection(int width, int height, float cosA, float sinA) {
            float ax = Math.abs(cosA);
            float ay = Math.abs(sinA);
            float tx = ax < 0.001f ? Float.POSITIVE_INFINITY : (width / 2f) / ax;
            float ty = ay < 0.001f ? Float.POSITIVE_INFINITY : (height / 2f) / ay;
            return Math.min(tx, ty);
        }

        private UIElement linkArrow(float worldX, float worldY, float rotationDeg, Runnable onClick) {
            int size = 14;
            Button arrow = button("\u25B6", true, 0x00000000);
            arrow.layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(Math.round(worldX - size / 2f))
                    .top(Math.round(worldY - size / 2f))
                    .width(size)
                    .height(size));
            arrow.textStyle(style -> style
                    .textColor(LINK_THREAD_COLOR)
                    .textShadow(false)
                    .fontSize(10f)
                    .textAlignHorizontal(Horizontal.CENTER)
                    .textAlignVertical(Vertical.CENTER));
            arrow.buttonStyle(style -> {
                style.baseTexture(IGuiTexture.EMPTY);
                style.hoverTexture(rect(0x40FFFFFF));
                style.pressedTexture(rect(0x60FFFFFF));
            });
            // Draw order is controlled by insertion order in buildAtlas
            // (threads/arrows added before islands/chests). zIndex only
            // influences hit-testing priority; leaving it at 0 keeps island
            // and chest bodies (zIndex 1) ahead for clicks while still
            // letting the arrow receive clicks where nothing else overlaps.
            arrow.style(style -> style.zIndex(0));
            arrow.transform(transform -> transform.pivot(0.5f, 0.5f).rotation(rotationDeg));
            arrow.setOnClick(event -> {
                if (event.button != 0) {
                    return;
                }
                event.stopPropagation();
                onClick.run();
            });
            return arrow;
        }

        private UIElement linkThread(
                SlotWorkspaceViewModel.ClaimedChestTile tile,
                SlotWorkspaceViewModel.AtlasIsland island
        ) {
            return buildThread(tile, island, LINK_THREAD_COLOR, 2);
        }

        private UIElement dimLinkThread(
                SlotWorkspaceViewModel.ClaimedChestTile tile,
                SlotWorkspaceViewModel.AtlasIsland island
        ) {
            return buildThread(tile, island, LINK_THREAD_DIM_COLOR, 1);
        }

        private UIElement buildThread(
                SlotWorkspaceViewModel.ClaimedChestTile tile,
                SlotWorkspaceViewModel.AtlasIsland island,
                int color,
                int thickness
        ) {
            int tileCenterX = tile.atlasX() + tile.width() / 2;
            int tileCenterY = tile.atlasY() + tile.height() / 2;
            int islandCenterX = island.x() + island.width() / 2;
            int islandCenterY = island.y() + island.height() / 2;
            int dx = islandCenterX - tileCenterX;
            int dy = islandCenterY - tileCenterY;
            double distance = Math.sqrt((double) dx * dx + (double) dy * dy);
            if (distance < 1.0) {
                return null;
            }
            int length = Math.max(1, (int) Math.round(distance));
            float angleDeg = (float) Math.toDegrees(Math.atan2(dy, dx));
            UIElement thread = panel(color).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(tileCenterX)
                    .top(tileCenterY - thickness / 2)
                    .width(length)
                    .height(thickness));
            thread.style(style -> style.zIndex(0));
            thread.transform(transform -> transform.pivot(0f, 0.5f).rotation(angleDeg));
            thread.setAllowHitTest(false);
            return thread;
        }

        private UIElement hoverTrailOverlay(SlotAtlasGraphView atlas) {
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
                UIElement slotElement = hotbarSlotElements.get(endpoints.hotbarIndex());
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
                int worldTargetX = endpoints.atlasItem().x() + endpoints.atlasItem().width() / 2;
                int worldTargetY = endpoints.atlasItem().y() + endpoints.atlasItem().height() / 2;
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

        private HoverTrailEndpoints resolveHoverTrail() {
            if (hoveredHotbarIndex >= 0 && hoveredHotbarIndex < viewModel.hotbarSlots().size()) {
                SlotWorkspaceViewModel.HotbarSlot slot = viewModel.hotbarSlots().get(hoveredHotbarIndex);
                if (slot.occupied()) {
                    SlotWorkspaceViewModel.IdentityRef identity = SlotWorkspaceViewModel.IdentityRef.from(
                            dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(slot.displayStack()));
                    SlotWorkspaceViewModel.AtlasItem item = atlasItemInIslandLayer(identity);
                    if (item != null) {
                        return new HoverTrailEndpoints(slot.hotbarIndex(), item);
                    }
                }
            }
            if (hoveredAtlasIdentity != null) {
                int hotbarIndex = hotbarSlotForIdentity(hoveredAtlasIdentity);
                SlotWorkspaceViewModel.AtlasItem item = atlasItemInIslandLayer(hoveredAtlasIdentity);
                if (hotbarIndex >= 0 && item != null) {
                    return new HoverTrailEndpoints(hotbarIndex, item);
                }
            }
            return null;
        }

        private SlotWorkspaceViewModel.AtlasItem atlasItemInIslandLayer(SlotWorkspaceViewModel.IdentityRef identity) {
            if (identity == null) {
                return null;
            }
            for (SlotWorkspaceViewModel.AtlasItem candidate : viewModel.atlasItems()) {
                if (candidate.identity().equals(identity)) {
                    return candidate;
                }
            }
            return null;
        }

        private boolean shouldAccentHotbarSlot(SlotWorkspaceViewModel.HotbarSlot slot) {
            if (hoveredAtlasIdentity == null) {
                return false;
            }
            SlotWorkspaceViewModel.IdentityRef slotIdentity = SlotWorkspaceViewModel.IdentityRef.from(
                    dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(slot.displayStack()));
            return hoveredAtlasIdentity.equals(slotIdentity);
        }

        private boolean shouldAccentTriageRow(SlotWorkspaceViewModel.AtlasItem item) {
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

        private record HoverTrailEndpoints(int hotbarIndex, SlotWorkspaceViewModel.AtlasItem atlasItem) {
        }

        private UIElement chestTileCell(ItemStack stack, boolean proximate, int cellSize) {
            int chromeColor = proximate ? STORAGE_TILE_CELL_FILL : STORAGE_TILE_CELL_FILL_DIM;
            UIElement cell = panel(chromeColor);
            if (stack != null && !stack.isEmpty()) {
                int iconSize = Math.max(8, cellSize - 2);
                UIElement icon = itemIcon(stack, iconSize, proximate);
                icon.layout(layout -> layout
                        .positionType(TaffyPosition.ABSOLUTE)
                        .left(1)
                        .top(1));
                cell.addChild(icon);
            }
            return cell;
        }

        private void installChestTileDragSource(
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
                if (!source.isMouseDown(0) || isDragging(source)) {
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
            source.addEventListener(UIEvents.DRAG_END, event -> handleDragEnd(event));
        }

        private void installChestStackDragSource(
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
                if (!cell.isMouseDown(0) || isDragging(cell)) {
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
                        dragTexture(stack)
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
                handleDragEnd(event);
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
        private static float ghostScaleFor(SlotWorkspaceViewModel.AtlasItem item, AtlasRenderBudget budget) {
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
        private void applyAtlasCardLayout(Button button, SlotWorkspaceViewModel.AtlasItem item) {
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
        private void applyAtlasCardGhostScale(Button button, SlotWorkspaceViewModel.AtlasItem item, AtlasRenderBudget budget) {
            float scale = ghostScaleFor(item, budget);
            button.transform(transform -> {
                transform.pivot(0.5f, 0.5f);
                transform.scale(scale);
            });
        }

        private Button atlasCardButton(SlotAtlasGraphView atlas, SlotWorkspaceViewModel.AtlasItem item) {
            boolean selected = item.identity().equals(selectedAtlasIdentity.get());
            boolean searchMatch = matchesSearch(item);
            boolean activeSearchMatch = !normalizedSearchQuery().isBlank() && searchMatch;
            AtlasRenderBudget initialBudget = atlasBudget(atlas, item);
            Button button = button("", true, cardChromeColor(initialBudget.level(), selected, searchMatch, item.recent(), item.carried()));
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
                    openContextMenuForAtlas(item, event.x, event.y);
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
                    ChestSlotRef source = firstProximateChestSlotFor(fresh);
                    if (source == null) {
                        localStatus.set("no nearby chest has " + fresh.name());
                        return;
                    }
                    for (int i = 0; i < magnitude; i++) {
                        sendTakeOneFromChest(source.storageId(), source.chestSlotIndex());
                    }
                } else {
                    boolean canPush = atlasItemHasDepositTarget(fresh)
                            || firstProximateChestSlotFor(fresh) != null;
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
            installAtlasHoverTooltip(button, item);
            installAtlasItemDragSource(button, item);

            UIElement body = new UIElement().layout(layout -> layout.widthPercent(100).heightPercent(100));
            body.setAllowHitTest(false);
            rebuildAtlasBody(body, atlas, item, initialBudget, activeSearchMatch);
            button.addChild(body);

            long[] lastSignature = new long[]{atlasLayoutSignature(initialBudget)};
            button.addEventListener(UIEvents.TICK, event -> {
                AtlasRenderBudget budget = atlasBudget(atlas, item);
                boolean currentSelected = item.identity().equals(selectedAtlasIdentity.get());
                boolean focused = isMapFocusItem(item);
                long signature = atlasLayoutSignature(budget);
                // Skip LOD rebuilds while the camera is animating. atlasBudget
                // uses animationTargetScale while rendering uses the live
                // interpolated scale, so a rebuild mid-animation bakes labels
                // for the target and draws them at the current scale — visible
                // as a big-text flash at the start of a zoom-in peek. Letting
                // cards stay at the pre-animation LOD means labels either
                // scale with the zoom or stay absent until the camera settles;
                // either way it's continuous, not a jump.
                if (signature != lastSignature[0] && !cameraController.isAnimating()) {
                    rebuildAtlasBody(body, atlas, item, budget, activeSearchMatch);
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
                applyButtonColors(button, true, cardChromeColor(budget.level(), currentSelected, searchMatch, item.recent(), item.carried()));
            });
            // Items sit on top of their island panel (z=2 vs z=1) and receive
            // their own mouse enter/leave, so hovering an item inside an
            // island must also flip hoveredIslandId — otherwise the dim
            // link-thread preview only shows when the cursor happens to hit
            // empty island background between cards.
            SlotWorkspaceViewModel.AtlasIsland hoverIsland = viewModel.island(item.islandId());
            attachIslandHoverListeners(button, hoverIsland);
            return button;
        }

        private void addAtlasItemChips(SlotAtlasGraphView atlas, SlotWorkspaceViewModel.AtlasItem item) {
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

        private void sendChipAccept(SlotWorkspaceViewModel.AtlasItem item, ChipSuggestion chip) {
            if (acceptChipEmitter == null) {
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
                acceptChipEmitter.send(
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

        private static boolean chipMatches(SlotWorkspaceViewModel.AtlasItem item, ChipSuggestion target) {
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

        private static String chipLabelText(ChipSuggestion chip) {
            String label = chip.kind() == ChipSuggestion.ChipKind.TEMPLATE && chip.template() != null
                    ? chip.template().defaultLabel()
                    : chip.label();
            return label == null ? "" : shorten(label, 10);
        }

        private void applyInitialCamera(SlotAtlasGraphView atlas) {
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

        private AtlasCamera computeOverviewCamera(float viewportWidth, float viewportHeight) {
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

        private void panToChestTile(SlotAtlasGraphView atlas, SlotWorkspaceViewModel.ClaimedChestTile tile) {
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

        private AtlasCamera computeChestTileCamera(SlotAtlasGraphView atlas, SlotWorkspaceViewModel.ClaimedChestTile tile) {
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

        private void panToIsland(SlotAtlasGraphView atlas, SlotWorkspaceViewModel.AtlasIsland island) {
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

        private AtlasCamera computeIslandCamera(SlotAtlasGraphView atlas, SlotWorkspaceViewModel.AtlasIsland island) {
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

        private static final int[] ISLAND_PALETTE = {
                0xCC7D5A3A, 0xCC5A6E3D, 0xCC6E3D3D, 0xCC3D5A6E,
                0xCC3D6E5A, 0xCC5A3D6E, 0xCC5A4A6E, 0xCC4E5A4A
        };

        private void beginChestLinkEdit(SlotWorkspaceViewModel.ClaimedChestTile tile) {
            if (tile == null) {
                return;
            }
            editingChestStorageId = tile.storageId();
            localStatus.set("linking " + tile.label());
            rebuild();
        }

        private void endChestLinkEdit() {
            editingChestStorageId = null;
            rebuild();
        }

        private UIElement chestLinkPopover() {
            if (editingChestStorageId == null) {
                return null;
            }
            SlotWorkspaceViewModel.ClaimedChestTile tile = viewModel.claimedChestTile(editingChestStorageId);
            if (tile == null) {
                editingChestStorageId = null;
                return null;
            }

            UIElement capsule = panel(GLASS).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .right(10)
                    .top(10)
                    .width(250)
                    .paddingAll(8)
                    .gapAll(6)
                    .flexDirection(FlexDirection.COLUMN));
            capsule.style(style -> style.zIndex(20));
            capsule.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());

            UIElement titleRow = new UIElement().layout(layout -> layout
                    .widthPercent(100)
                    .height(16)
                    .gapAll(6)
                    .alignItems(AlignItems.CENTER)
                    .flexDirection(FlexDirection.ROW));
            Label title = label("Manage chest", ACCENT);
            title.layout(layout -> layout.flex(1).height(12));
            Button close = button("x", true, PANEL_ALT);
            close.layout(layout -> layout.width(18).height(14));
            close.setOnClick(event -> {
                event.stopPropagation();
                endChestLinkEdit();
            });
            titleRow.addChildren(title, close);
            capsule.addChild(titleRow);

            TextField nameInput = new TextField();
            nameInput.setAnyString();
            nameInput.setText(tile.label(), false);
            nameInput.layout(layout -> layout.widthPercent(100).height(20));
            nameInput.style(style -> style.backgroundTexture(rect(0xC60D1318)));
            nameInput.textFieldStyle(style -> style
                    .font(FONT_UI)
                    .placeholder(Component.literal("Chest name"))
                    .textColor(TEXT)
                    .cursorColor(ACCENT)
                    .textShadow(false)
                    .fontSize(10));
            String currentStorageId = tile.storageId();
            String currentLabel = tile.label();
            nameInput.setTextResponder(value -> {
                String next = value == null ? "" : value;
                String trimmed = next.trim();
                if (trimmed.equals(currentLabel)) {
                    return;
                }
                if (relabelChestEmitter != null) {
                    relabelChestEmitter.send(currentStorageId, trimmed);
                }
            });
            capsule.addChild(nameInput);

            List<SlotWorkspaceViewModel.AtlasIsland> playerIslands = new ArrayList<>();
            for (SlotWorkspaceViewModel.AtlasIsland island : viewModel.islands()) {
                if (island.kind() == VisualAtlasIslandKind.PLAYER) {
                    playerIslands.add(island);
                }
            }
            if (playerIslands.isEmpty()) {
                Label hint = label("No player islands yet — create one first", MUTED);
                hint.layout(layout -> layout.widthPercent(100).height(14));
                capsule.addChild(hint);
                return capsule;
            }

            for (SlotWorkspaceViewModel.AtlasIsland island : playerIslands) {
                boolean linked = tile.linkedIslandIds().contains(island.islandId());
                UIElement row = new UIElement().layout(layout -> layout
                        .widthPercent(100)
                        .height(18)
                        .gapAll(6)
                        .alignItems(AlignItems.CENTER)
                        .flexDirection(FlexDirection.ROW));
                Label name = label(island.label(), linked ? ACCENT : TEXT);
                name.layout(layout -> layout.flex(1).height(12));
                Button action = button(linked ? "Unlink" : "Link", true, linked ? PANEL_ALT : ROW);
                action.layout(layout -> layout.width(54).height(14));
                String islandId = island.islandId();
                String storageId = tile.storageId();
                action.setOnClick(event -> {
                    event.stopPropagation();
                    if (linked) {
                        sendUnlinkChest(islandId, storageId);
                    } else {
                        sendLinkChest(islandId, storageId);
                    }
                });
                row.addChildren(name, action);
                capsule.addChild(row);
            }
            return capsule;
        }

        private void openContextMenuForAtlas(SlotWorkspaceViewModel.AtlasItem item, float screenX, float screenY) {
            if (item == null) {
                return;
            }
            contextMenuAtlasIdentity = item.identity();
            contextMenuHotbarIndex = -1;
            contextMenuScreenX = screenX;
            contextMenuScreenY = screenY;
            rebuild();
        }

        private void openContextMenuForHotbar(SlotWorkspaceViewModel.HotbarSlot slot, float screenX, float screenY) {
            if (slot == null || !slot.occupied()) {
                return;
            }
            contextMenuHotbarIndex = slot.hotbarIndex();
            contextMenuAtlasIdentity = null;
            contextMenuKitId = null;
            contextMenuScreenX = screenX;
            contextMenuScreenY = screenY;
            rebuild();
        }

        private void openContextMenuForKit(String kitId, float screenX, float screenY) {
            if (kitId == null || kitId.isBlank()) {
                return;
            }
            contextMenuKitId = kitId;
            contextMenuAtlasIdentity = null;
            contextMenuHotbarIndex = -1;
            renamingKitId = null;
            renameKitDraft = "";
            confirmDeleteKitId = null;
            contextMenuScreenX = screenX;
            contextMenuScreenY = screenY;
            rebuild();
        }

        private void closeContextMenu() {
            contextMenuAtlasIdentity = null;
            contextMenuHotbarIndex = -1;
            contextMenuKitId = null;
            renamingKitId = null;
            renameKitDraft = "";
            confirmDeleteKitId = null;
            rebuild();
        }

        private UIElement contextMenuOverlay() {
            if (contextMenuAtlasIdentity != null) {
                SlotWorkspaceViewModel.AtlasItem item = viewModel.atlasItem(contextMenuAtlasIdentity);
                if (item == null) {
                    contextMenuAtlasIdentity = null;
                    return null;
                }
                return buildAtlasContextMenu(item);
            }
            if (contextMenuHotbarIndex >= 0 && contextMenuHotbarIndex < viewModel.hotbarSlots().size()) {
                SlotWorkspaceViewModel.HotbarSlot slot = viewModel.hotbarSlots().get(contextMenuHotbarIndex);
                if (!slot.occupied()) {
                    contextMenuHotbarIndex = -1;
                    return null;
                }
                return buildHotbarContextMenu(slot);
            }
            if (contextMenuKitId != null) {
                SlotWorkspaceViewModel.KitCard card = viewModel.kit(contextMenuKitId);
                if (card == null) {
                    closeContextMenu();
                    return null;
                }
                return buildKitContextMenu(card);
            }
            return null;
        }

        private UIElement contextMenuCatcher(Runnable onDismiss) {
            UIElement catcher = new UIElement().layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(0).right(0).top(0).bottom(0));
            catcher.style(style -> style.zIndex(21));
            catcher.addEventListener(UIEvents.MOUSE_DOWN, event -> {
                event.stopPropagation();
                onDismiss.run();
            });
            return catcher;
        }

        private UIElement buildAtlasContextMenu(SlotWorkspaceViewModel.AtlasItem item) {
            UIElement catcher = contextMenuCatcher(this::closeContextMenu);
            UIElement menu = panel(GLASS).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .width(160)
                    .paddingAll(4)
                    .gapAll(2)
                    .flexDirection(FlexDirection.COLUMN));
            anchorPopover(menu, contextMenuScreenX, contextMenuScreenY, 160, 96);
            menu.style(style -> style.zIndex(22));
            menu.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());

            menu.addChild(label(shorten(item.name(), 22), ACCENT)
                    .layout(layout -> layout.widthPercent(100).height(12)));

            int freeHotbarIndex = firstFreeHotbarIndex();
            if (item.carried() && freeHotbarIndex >= 0) {
                menu.addChild(menuButton(
                        "Send to hotbar",
                        true,
                        null,
                        () -> {
                            sendAssignHomeToHotbarOnly(item);
                            closeContextMenu();
                        }
                ));
            }

            if (item.carried() && atlasItemHasDepositTarget(item)) {
                menu.addChild(menuButton(
                        "Deposit to linked chest",
                        true,
                        null,
                        () -> {
                            sendDepositHomeToLinkedChest(item);
                            closeContextMenu();
                        }
                ));
            }

            List<SlotWorkspaceViewModel.AtlasIsland> recent = recentRehomeTargets(item);
            for (SlotWorkspaceViewModel.AtlasIsland target : recent) {
                String targetIslandId = target.islandId();
                menu.addChild(menuButton(
                        "Move to " + shorten(target.label(), 18),
                        true,
                        null,
                        () -> {
                            sendAssignHome(item.identity(), targetIslandId, 0, 0);
                            closeContextMenu();
                        }
                ));
            }

            menu.addChild(menuButton("Cancel", true, null, this::closeContextMenu));

            UIElement wrapper = new UIElement().layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(0).right(0).top(0).bottom(0));
            wrapper.addChildren(catcher, menu);
            return wrapper;
        }

        private UIElement buildHotbarContextMenu(SlotWorkspaceViewModel.HotbarSlot slot) {
            UIElement catcher = contextMenuCatcher(this::closeContextMenu);
            UIElement menu = panel(GLASS).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .width(160)
                    .paddingAll(4)
                    .gapAll(2)
                    .flexDirection(FlexDirection.COLUMN));
            anchorPopover(menu, contextMenuScreenX, contextMenuScreenY, 160, 80);
            menu.style(style -> style.zIndex(22));
            menu.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());

            String name = slot.displayStack() == null || slot.displayStack().isEmpty()
                    ? "hotbar " + (slot.hotbarIndex() + 1)
                    : slot.displayStack().getHoverName().getString();
            menu.addChild(label(shorten(name, 22), ACCENT)
                    .layout(layout -> layout.widthPercent(100).height(12)));

            int hotbarIdx = slot.hotbarIndex();
            menu.addChild(menuButton(
                    "Send to home",
                    true,
                    null,
                    () -> {
                        sendReturnHotbarToHome(hotbarIdx);
                        closeContextMenu();
                    }
            ));

            menu.addChild(menuButton("Cancel", true, null, this::closeContextMenu));

            UIElement wrapper = new UIElement().layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(0).right(0).top(0).bottom(0));
            wrapper.addChildren(catcher, menu);
            return wrapper;
        }

        private UIElement buildKitContextMenu(SlotWorkspaceViewModel.KitCard card) {
            UIElement catcher = contextMenuCatcher(this::closeContextMenu);
            UIElement menu = panel(GLASS).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .width(180)
                    .paddingAll(6)
                    .gapAll(4)
                    .flexDirection(FlexDirection.COLUMN));
            int approxHeight = 80;
            if (card.kitId().equals(renamingKitId)) {
                approxHeight = 70;
            } else if (card.kitId().equals(confirmDeleteKitId)) {
                approxHeight = 64;
            }
            anchorPopover(menu, contextMenuScreenX, contextMenuScreenY, 180, approxHeight);
            menu.style(style -> style.zIndex(22));
            menu.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());

            menu.addChild(label(shorten(card.name(), 22), ACCENT)
                    .layout(layout -> layout.widthPercent(100).height(12)));

            if (card.kitId().equals(renamingKitId)) {
                appendKitRenameBody(menu, card);
            } else if (card.kitId().equals(confirmDeleteKitId)) {
                appendKitDeleteConfirmBody(menu, card);
            } else {
                menu.addChild(menuButton("Rename\u2026", true, null, () -> {
                    renamingKitId = card.kitId();
                    renameKitDraft = card.name();
                    rebuild();
                }));
                menu.addChild(menuButton("Duplicate", true, null, () -> {
                    sendDuplicateKit(card.kitId());
                    closeContextMenu();
                }));
                menu.addChild(menuButton("Delete\u2026", true, null, () -> {
                    confirmDeleteKitId = card.kitId();
                    rebuild();
                }));
            }

            UIElement wrapper = new UIElement().layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(0).right(0).top(0).bottom(0));
            wrapper.addChildren(catcher, menu);
            return wrapper;
        }

        private void appendKitRenameBody(UIElement menu, SlotWorkspaceViewModel.KitCard card) {
            TextField nameInput = new TextField();
            nameInput.setAnyString();
            nameInput.setText(renameKitDraft, false);
            nameInput.layout(layout -> layout.widthPercent(100).height(18));
            nameInput.style(style -> style.backgroundTexture(rect(0xC60D1318)));
            nameInput.textFieldStyle(style -> style
                    .font(FONT_UI)
                    .placeholder(Component.literal("Kit name"))
                    .textColor(TEXT)
                    .cursorColor(ACCENT)
                    .textShadow(false)
                    .fontSize(9));
            nameInput.setTextResponder(value -> renameKitDraft = value == null ? "" : value);
            Runnable commit = () -> {
                String trimmed = renameKitDraft == null ? "" : renameKitDraft.trim();
                if (trimmed.isBlank() || trimmed.equals(card.name())) {
                    closeContextMenu();
                    return;
                }
                if (renameKitEmitter != null) {
                    renameKitEmitter.send(card.kitId(), trimmed);
                }
                closeContextMenu();
            };
            nameInput.addEventListener(UIEvents.KEY_DOWN, event -> {
                if (event.keyCode == GLFW.GLFW_KEY_ENTER || event.keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                    commit.run();
                    event.stopPropagation();
                } else if (event.keyCode == GLFW.GLFW_KEY_ESCAPE) {
                    closeContextMenu();
                    event.stopPropagation();
                }
            });
            menu.addChild(nameInput);
            UIElement row = new UIElement().layout(layout -> layout
                    .widthPercent(100)
                    .height(14)
                    .gapAll(4)
                    .flexDirection(FlexDirection.ROW));
            Button save = button("Save", true, ACCENT);
            save.layout(layout -> layout.flex(1).height(14));
            save.textStyle(style -> style.textColor(TEXT).textShadow(false).fontSize(8)
                    .textAlignHorizontal(Horizontal.CENTER).textAlignVertical(Vertical.CENTER));
            save.setOnClick(event -> {
                event.stopPropagation();
                commit.run();
            });
            Button cancel = button("Cancel", true, PANEL_ALT);
            cancel.layout(layout -> layout.flex(1).height(14));
            cancel.textStyle(style -> style.textColor(MUTED).textShadow(false).fontSize(8)
                    .textAlignHorizontal(Horizontal.CENTER).textAlignVertical(Vertical.CENTER));
            cancel.setOnClick(event -> {
                event.stopPropagation();
                closeContextMenu();
            });
            row.addChildren(save, cancel);
            menu.addChild(row);
        }

        private void appendKitDeleteConfirmBody(UIElement menu, SlotWorkspaceViewModel.KitCard card) {
            Label prompt = label("Delete " + shorten(card.name(), 18) + "?", WARNING);
            prompt.layout(layout -> layout.widthPercent(100).height(12));
            prompt.textStyle(style -> style.textColor(WARNING).textShadow(false).fontSize(8)
                    .textAlignHorizontal(Horizontal.LEFT).textAlignVertical(Vertical.CENTER));
            prompt.setAllowHitTest(false);
            menu.addChild(prompt);
            UIElement row = new UIElement().layout(layout -> layout
                    .widthPercent(100)
                    .height(14)
                    .gapAll(4)
                    .flexDirection(FlexDirection.ROW));
            Button confirm = button("Delete", true, WARNING);
            confirm.layout(layout -> layout.flex(1).height(14));
            confirm.textStyle(style -> style.textColor(TEXT).textShadow(true).fontSize(8)
                    .textAlignHorizontal(Horizontal.CENTER).textAlignVertical(Vertical.CENTER));
            confirm.setOnClick(event -> {
                event.stopPropagation();
                sendDeleteKit(card.kitId());
                closeContextMenu();
            });
            Button cancel = button("Cancel", true, PANEL_ALT);
            cancel.layout(layout -> layout.flex(1).height(14));
            cancel.textStyle(style -> style.textColor(MUTED).textShadow(false).fontSize(8)
                    .textAlignHorizontal(Horizontal.CENTER).textAlignVertical(Vertical.CENTER));
            cancel.setOnClick(event -> {
                event.stopPropagation();
                closeContextMenu();
            });
            row.addChildren(confirm, cancel);
            menu.addChild(row);
        }

        private void sendDuplicateKit(String kitId) {
            boolean sent = duplicateKitEmitter != null && duplicateKitEmitter.send(kitId);
            localStatus.set(sent ? "duplicating kit..." : "duplicate unavailable");
            rebuild();
        }

        private Button menuButton(String text, boolean enabled, String disabledHint, Runnable onClick) {
            String label = enabled || disabledHint == null ? text : text + " (" + disabledHint + ")";
            Button button = button(label, enabled, enabled ? ROW : PANEL_ALT);
            button.layout(layout -> layout.widthPercent(100).height(14));
            button.textStyle(style -> style
                    .textColor(enabled ? TEXT : MUTED)
                    .textShadow(false)
                    .fontSize(7)
                    .textAlignHorizontal(Horizontal.LEFT)
                    .textAlignVertical(Vertical.CENTER));
            if (enabled) {
                button.setOnClick(event -> {
                    event.stopPropagation();
                    if (event.button != 0) {
                        return;
                    }
                    onClick.run();
                });
            }
            return button;
        }

        private void anchorPopover(UIElement menu, float screenX, float screenY, int width, int approxHeight) {
            float originX = atlasPanelElement != null ? atlasPanelElement.getPositionX() : 0f;
            float originY = atlasPanelElement != null ? atlasPanelElement.getPositionY() : 0f;
            int left = Math.max(4, Math.round(screenX - originX) + 4);
            int top = Math.max(4, Math.round(screenY - originY) + 4);
            menu.layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(left)
                    .top(top)
                    .width(width));
        }

        private int firstFreeHotbarIndex() {
            for (SlotWorkspaceViewModel.HotbarSlot s : viewModel.hotbarSlots()) {
                if (!s.occupied()) {
                    return s.hotbarIndex();
                }
            }
            return -1;
        }

        private boolean atlasItemHasDepositTarget(SlotWorkspaceViewModel.AtlasItem item) {
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

        private void beginIslandEdit(SlotWorkspaceViewModel.AtlasIsland island, float screenX, float screenY) {
            if (island == null) {
                return;
            }
            editingIslandId = island.islandId();
            islandLabelDraft = island.label();
            islandEditScreenX = screenX;
            islandEditScreenY = screenY;
            localStatus.set("editing " + island.label());
            rebuild();
        }

        private void endIslandEdit() {
            editingIslandId = null;
            islandLabelDraft = "";
            islandEditScreenX = Float.NaN;
            islandEditScreenY = Float.NaN;
            rebuild();
        }

        private UIElement islandEditPopover() {
            if (editingIslandId == null) {
                return null;
            }
            SlotWorkspaceViewModel.AtlasIsland island = viewModel.island(editingIslandId);
            if (island == null || island.kind() != VisualAtlasIslandKind.PLAYER) {
                editingIslandId = null;
                islandLabelDraft = "";
                return null;
            }

            // Fullscreen catcher dismisses the popover when the user clicks
            // anywhere outside the capsule — matches the context-menu pattern
            // (buildAtlasContextMenu at ~3298) and replaces the old dedicated
            // "x" close button. Catcher sits just below the capsule's zIndex
            // so the capsule's own MOUSE_DOWN (stopPropagation) wins inside.
            UIElement catcher = contextMenuCatcher(this::endIslandEdit);

            UIElement capsule = panel(GLASS).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .width(250)
                    .paddingAll(8)
                    .gapAll(6)
                    .flexDirection(FlexDirection.COLUMN));
            anchorPopover(capsule, islandEditScreenX, islandEditScreenY, 250, 240);
            capsule.style(style -> style.zIndex(22));
            capsule.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());

            Label title = label("Edit island", ACCENT);
            title.layout(layout -> layout.widthPercent(100).height(12));
            capsule.addChild(title);

            TextField nameInput = new TextField();
            nameInput.setAnyString();
            nameInput.setText(islandLabelDraft, false);
            nameInput.layout(layout -> layout.widthPercent(100).height(20));
            nameInput.style(style -> style.backgroundTexture(rect(0xC60D1318)));
            nameInput.textFieldStyle(style -> style
                    .font(FONT_UI)
                    .placeholder(Component.literal("Island name"))
                    .textColor(TEXT)
                    .cursorColor(ACCENT)
                    .textShadow(false)
                    .fontSize(10));
            nameInput.setTextResponder(value -> islandLabelDraft = value == null ? "" : value);
            Runnable commitRename = () -> {
                if (editingIslandId == null) {
                    return;
                }
                String trimmed = islandLabelDraft == null ? "" : islandLabelDraft.trim();
                if (trimmed.isBlank() || trimmed.equals(island.label())) {
                    return;
                }
                if (renameIslandEmitter != null) {
                    renameIslandEmitter.send(editingIslandId, trimmed);
                }
            };
            nameInput.addEventListener(UIEvents.BLUR, event -> commitRename.run());
            nameInput.addEventListener(UIEvents.KEY_DOWN, event -> {
                if (event.keyCode == GLFW.GLFW_KEY_ENTER || event.keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                    commitRename.run();
                    event.stopPropagation();
                }
            });
            capsule.addChild(nameInput);

            capsule.addChild(label("Color", MUTED).layout(layout -> layout.height(10)));
            UIElement paletteRow = new UIElement().layout(layout -> layout
                    .widthPercent(100)
                    .height(18)
                    .gapAll(4)
                    .flexDirection(FlexDirection.ROW));
            for (int color : ISLAND_PALETTE) {
                boolean selected = color == island.color();
                Button swatch = button("", true, color);
                swatch.layout(layout -> layout.flex(1).height(18));
                swatch.noText();
                if (selected) {
                    swatch.style(style -> style.zIndex(1));
                }
                int finalColor = color;
                swatch.setOnClick(event -> {
                    event.stopPropagation();
                    if (finalColor == island.color()) {
                        return;
                    }
                    boolean sent = recolorIslandEmitter != null && recolorIslandEmitter.send(editingIslandId, finalColor);
                    localStatus.set(sent ? "recolor requested" : "recolor unavailable");
                    rebuild();
                });
                paletteRow.addChild(swatch);
            }
            capsule.addChild(paletteRow);

            SlotWorkspaceViewModel.AtlasItem selected = selectedAtlasItem();
            boolean canSetIcon = selected != null;
            Button setIcon = button(
                    canSetIcon ? "Set icon: " + shorten(selected.name(), 16) : "Select an item to set icon",
                    canSetIcon
            );
            setIcon.layout(layout -> layout.widthPercent(100).height(18));
            setIcon.setOnClick(event -> {
                event.stopPropagation();
                if (selected == null) {
                    localStatus.set("select an atlas item first");
                    rebuild();
                    return;
                }
                boolean sent = setIslandIconEmitter != null && setIslandIconEmitter.send(
                        editingIslandId,
                        selected.identity().itemId(),
                        selected.identity().comparisonMode(),
                        selected.identity().componentFingerprint()
                );
                localStatus.set(sent ? "set icon requested" : "set icon unavailable");
                rebuild();
            });
            capsule.addChild(setIcon);

            Button clearIcon = button("Clear icon", true);
            clearIcon.layout(layout -> layout.widthPercent(100).height(18));
            clearIcon.setOnClick(event -> {
                event.stopPropagation();
                boolean sent = setIslandIconEmitter != null && setIslandIconEmitter.send(editingIslandId, "", "", "");
                localStatus.set(sent ? "clear icon requested" : "clear icon unavailable");
                rebuild();
            });
            capsule.addChild(clearIcon);

            boolean empty = island.itemCount() == 0;
            Button deleteButton = button(empty ? "Delete island" : "Delete (move items first)", empty);
            deleteButton.layout(layout -> layout.widthPercent(100).height(18));
            deleteButton.setOnClick(event -> {
                event.stopPropagation();
                if (!empty) {
                    localStatus.set("move all items off this island first");
                    rebuild();
                    return;
                }
                boolean sent = deleteIslandEmitter != null && deleteIslandEmitter.send(editingIslandId);
                localStatus.set(sent ? "delete requested" : "delete unavailable");
                if (sent) {
                    endIslandEdit();
                    return;
                }
                rebuild();
            });
            capsule.addChild(deleteButton);

            UIElement wrapper = new UIElement().layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(0).right(0).top(0).bottom(0));
            wrapper.addChildren(catcher, capsule);
            return wrapper;
        }

        private void beginCreateIsland(SlotWorkspaceViewModel.AtlasItem item, int worldX, int worldY) {
            if (item == null) {
                return;
            }
            pendingCreateIdentity = item.identity();
            pendingCreateWorldX = worldX;
            pendingCreateWorldY = worldY;
            pendingCreateLabel = item.name();
            pendingCreateColor = ISLAND_PALETTE[0];
            pendingCreateFocusPending = true;
            localStatus.set("name the new island");
            rebuild();
        }

        private void endCreateIsland() {
            pendingCreateIdentity = null;
            pendingCreateLabel = "";
            pendingCreateColor = ISLAND_PALETTE[0];
            pendingCreateFocusPending = false;
            rebuild();
        }

        private UIElement createIslandPopover() {
            if (pendingCreateIdentity == null) {
                return null;
            }
            SlotWorkspaceViewModel.AtlasItem item = viewModel.atlasItem(pendingCreateIdentity);
            if (item == null) {
                pendingCreateIdentity = null;
                return null;
            }

            UIElement capsule = panel(GLASS).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .right(10)
                    .top(10)
                    .width(260)
                    .paddingAll(8)
                    .gapAll(6)
                    .flexDirection(FlexDirection.COLUMN));
            capsule.style(style -> style.zIndex(20));
            capsule.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());

            UIElement titleRow = new UIElement().layout(layout -> layout
                    .widthPercent(100)
                    .height(16)
                    .gapAll(6)
                    .alignItems(AlignItems.CENTER)
                    .flexDirection(FlexDirection.ROW));
            Label title = label("New island for " + shorten(item.name(), 18), ACCENT);
            title.layout(layout -> layout.flex(1).height(12));
            Button close = button("x", true, PANEL_ALT);
            close.layout(layout -> layout.width(18).height(14));
            close.setOnClick(event -> {
                event.stopPropagation();
                endCreateIsland();
            });
            titleRow.addChildren(title, close);
            capsule.addChild(titleRow);

            TextField nameInput = new TextField();
            nameInput.setAnyString();
            nameInput.setText(pendingCreateLabel, false);
            nameInput.layout(layout -> layout.widthPercent(100).height(20));
            nameInput.style(style -> style.backgroundTexture(rect(0xC60D1318)));
            nameInput.textFieldStyle(style -> style
                    .font(FONT_UI)
                    .placeholder(Component.literal("Island name"))
                    .textColor(TEXT)
                    .cursorColor(ACCENT)
                    .textShadow(false)
                    .fontSize(10));
            nameInput.setTextResponder(value -> pendingCreateLabel = value == null ? "" : value);
            if (pendingCreateFocusPending) {
                pendingCreateFocusPending = false;
                nameInput.addEventListener(UIEvents.LAYOUT_CHANGED, event -> {
                    nameInput.focus();
                    String current = nameInput.getValue();
                    int length = current == null ? 0 : current.length();
                    nameInput.setCursor(length);
                    nameInput.setSelection(0, length);
                }, true);
            }
            capsule.addChild(nameInput);

            capsule.addChild(label("Color", MUTED).layout(layout -> layout.height(10)));
            UIElement paletteRow = new UIElement().layout(layout -> layout
                    .widthPercent(100)
                    .height(18)
                    .gapAll(4)
                    .flexDirection(FlexDirection.ROW));
            for (int color : ISLAND_PALETTE) {
                boolean selected = color == pendingCreateColor;
                Button swatch = button("", true, color);
                swatch.layout(layout -> layout.flex(1).height(18));
                swatch.noText();
                if (selected) {
                    swatch.style(style -> style.zIndex(1));
                }
                int finalColor = color;
                swatch.setOnClick(event -> {
                    event.stopPropagation();
                    pendingCreateColor = finalColor;
                    rebuild();
                });
                paletteRow.addChild(swatch);
            }
            capsule.addChild(paletteRow);

            UIElement actionRow = new UIElement().layout(layout -> layout
                    .widthPercent(100)
                    .height(20)
                    .gapAll(6)
                    .flexDirection(FlexDirection.ROW));
            Button cancel = button("Cancel", true, PANEL_ALT);
            cancel.layout(layout -> layout.flex(1).height(20));
            cancel.setOnClick(event -> {
                event.stopPropagation();
                endCreateIsland();
            });
            boolean nameReady = pendingCreateLabel != null && !pendingCreateLabel.trim().isBlank();
            Button create = button("Create", nameReady);
            create.layout(layout -> layout.flex(1).height(20));
            create.setOnClick(event -> {
                event.stopPropagation();
                String trimmed = pendingCreateLabel == null ? "" : pendingCreateLabel.trim();
                if (trimmed.isBlank()) {
                    localStatus.set("enter an island name");
                    rebuild();
                    return;
                }
                boolean sent = createNamedIslandEmitter != null && createNamedIslandEmitter.send(
                        pendingCreateIdentity.itemId(),
                        pendingCreateIdentity.comparisonMode(),
                        pendingCreateIdentity.componentFingerprint(),
                        trimmed,
                        pendingCreateColor,
                        pendingCreateWorldX,
                        pendingCreateWorldY
                );
                localStatus.set(sent ? "create island requested" : "create island unavailable");
                if (sent) {
                    endCreateIsland();
                    return;
                }
                rebuild();
            });
            actionRow.addChildren(cancel, create);
            capsule.addChild(actionRow);

            return capsule;
        }

        private UIElement inspectorPanel() {
            UIElement panel = panel(PANEL).layout(layout -> layout
                    .width(284)
                    .heightPercent(100)
                    .paddingAll(8)
                    .gapAll(6)
                    .flexDirection(FlexDirection.COLUMN));
            clearSelectionOnDirectClick(panel);
            panel.addChildren(selectionPanel());
            return panel;
        }

        private UIElement selectionPanel() {
            UIElement panel = panel(PANEL_ALT).layout(layout -> layout
                    .widthPercent(100)
                    .paddingAll(6)
                    .gapAll(4)
                    .flexDirection(FlexDirection.COLUMN));
            SlotWorkspaceViewModel.AtlasItem atlasItem = focusedAtlasItem();
            SlotWorkspaceViewModel.HotbarSlot hotbar = selectedHotbarSlot();
            if (atlasItem != null) {
                SlotWorkspaceViewModel.AtlasIsland island = viewModel.island(atlasItem.islandId());
                ArrayList<UIElement> children = new ArrayList<>();
                UIElement hero = new UIElement().layout(layout -> layout
                        .widthPercent(100)
                        .height(20)
                        .gapAll(6)
                        .alignItems(AlignItems.CENTER)
                        .flexDirection(FlexDirection.ROW));
                hero.addChildren(
                        slotPreview(atlasItem, 18, true),
                        label(shorten(atlasItem.name(), 24), TEXT).layout(layout -> layout.flex(1).height(12)),
                        label("x" + compactCount(atlasItem.totalCount()), ACCENT).layout(layout -> layout.width(28).height(12))
                );
                children.add(label(selectedAtlasItem() != null ? "Selected Item" : "Focused Item", ACCENT).layout(layout -> layout.height(12)));
                children.add(hero);
                children.add(wrappedLabel("id: " + atlasItem.identity().itemId(), MUTED));
                children.add(wrappedLabel("source: main:" + atlasItem.firstSlotIndex(), MUTED));
                children.add(wrappedLabel("home: " + (island == null ? atlasItem.islandId() : island.label()), MUTED));
                children.add(label(selectionHomeStatus(atlasItem), atlasItem.playerPlaced() ? ACCENT : island != null && island.kind() == VisualAtlasIslandKind.TRIAGE ? WARNING : ACCENT)
                        .layout(layout -> layout.height(12)));
                children.add(wrappedLabel("Drag to move this home. Drop on a hotbar slot to assign quick access.", MUTED));
                appendTooltipPreview(children, atlasItem);
                panel.addChildren(children.toArray(UIElement[]::new));
            } else if (hotbar != null) {
                panel.addChildren(
                        label("Selected Hotbar", ACCENT).layout(layout -> layout.height(12)),
                        label("slot " + (hotbar.hotbarIndex() + 1), TEXT).layout(layout -> layout.height(12)),
                        label(hotbar.occupied() ? itemName(hotbar.displayStack()) : "empty", MUTED).layout(layout -> layout.height(12))
                );
            } else {
                panel.addChildren(
                        label("Selection", ACCENT).layout(layout -> layout.height(12)),
                        wrappedLabel("Select an anchor or hotbar slot to inspect it. Rich detail lives here so atlas homes can stay compact.", MUTED)
                );
            }
            return panel;
        }

        private UIElement beltPanel() {
            UIElement panel = panel(PANEL).layout(layout -> layout
                    .widthPercent(100)
                    .height(BELT_HEIGHT)
                    .paddingAll(2)
                    .gapAll(2)
                    .alignItems(AlignItems.CENTER)
                    .flexDirection(FlexDirection.ROW));
            clearSelectionOnDirectClick(panel);
            // Swallow mouse and drag events that land on the belt chrome (gaps between
            // slots, dividers, spacers) so they don't bubble to the atlas underneath.
            // Individual slot handlers fire first and call stopPropagation() themselves;
            // this catcher only sees events that missed a slot.
            panel.addEventListener(UIEvents.MOUSE_DOWN, UIEvent::stopPropagation);
            panel.addEventListener(UIEvents.DRAG_PERFORM, UIEvent::stopPropagation);
            panel.addChild(beltSpacer());
            panel.addChild(kitCluster());
            panel.addChild(beltDivider());
            for (SlotWorkspaceViewModel.HotbarSlot slot : viewModel.hotbarSlots()) {
                panel.addChild(beltSlotButton(slot));
            }
            panel.addChild(beltDivider());
            panel.addChild(offhandSlotButton(viewModel.offhand()));
            panel.addChild(beltSpacer());
            return panel;
        }

        private UIElement kitCluster() {
            // Fixed-width container with right-aligned children so changes to the kit
            // toggle's label (kit name / page indicator) grow LEFT into the cluster's own
            // whitespace instead of pushing the hotbar. Sized to fit the widest reasonable
            // label ("longname 3/3") plus the page-cycle button without truncation.
            UIElement cluster = new UIElement().layout(layout -> layout
                    .width(KIT_CLUSTER_WIDTH)
                    .height(BELT_SLOT_SIZE)
                    .gapAll(2)
                    .alignItems(AlignItems.CENTER)
                    .justifyContent(AlignContent.FLEX_END)
                    .flexDirection(FlexDirection.ROW));
            cluster.addChild(kitsToggleButton());
            SlotWorkspaceViewModel.KitCard activeCard = viewModel.activeKit();
            if (activeCard != null && activeCard.pageCount() > 1) {
                cluster.addChild(kitPageCycleButton(activeCard));
            }
            return cluster;
        }

        private Button kitsToggleButton() {
            int kitCount = viewModel.kits().size();
            SlotWorkspaceViewModel.KitCard activeCard = viewModel.activeKit();
            String label;
            if (activeCard != null) {
                String suffix = activeCard.pageCount() > 1
                        ? " " + (activeCard.activePageIndex() + 1) + "/" + activeCard.pageCount()
                        : "";
                label = shorten(activeCard.name(), 10) + suffix;
            } else {
                label = "Kits";
            }
            int bgColor = kitRackOpen
                    ? PANEL_ALT
                    : activeCard != null ? ACTIVE_HOTBAR : PANEL_ALT;
            int textColor = kitRackOpen
                    ? ACCENT
                    : activeCard != null ? TEXT : MUTED;
            Button button = button(label, true, bgColor);
            button.layout(layout -> layout
                    .width(Math.max(44, label.length() * 5 + 12))
                    .height(BELT_SLOT_SIZE)
                    .paddingAll(2)
                    .alignItems(AlignItems.CENTER));
            button.textStyle(style -> style
                    .textColor(textColor)
                    .textShadow(activeCard != null)
                    .fontSize(8)
                    .textAlignHorizontal(Horizontal.CENTER)
                    .textAlignVertical(Vertical.CENTER));
            button.setOnClick(event -> {
                event.stopPropagation();
                kitRackOpen = !kitRackOpen;
                localStatus.set(kitRackOpen
                        ? "kit rack open (" + kitCount + " kit" + (kitCount == 1 ? "" : "s") + ")"
                        : "kit rack closed");
                rebuild();
            });
            return button;
        }

        private Button kitPageCycleButton(SlotWorkspaceViewModel.KitCard activeCard) {
            Button button = button(">", true, ACTIVE_HOTBAR);
            button.layout(layout -> layout
                    .width(16)
                    .height(BELT_SLOT_SIZE)
                    .paddingAll(1)
                    .alignItems(AlignItems.CENTER));
            button.textStyle(style -> style
                    .textColor(TEXT)
                    .textShadow(false)
                    .fontSize(8)
                    .textAlignHorizontal(Horizontal.CENTER)
                    .textAlignVertical(Vertical.CENTER));
            button.addEventListener(UIEvents.HOVER_TOOLTIPS, event -> event.hoverTooltips = new HoverTooltips(
                    List.of(Component.literal("Next Kit page (" + activeCard.pageCount() + " total)")),
                    null,
                    null,
                    ItemStack.EMPTY
            ));
            button.setOnClick(event -> {
                event.stopPropagation();
                int direction = (event.button == 1 || Screen.hasShiftDown()) ? -1 : 1;
                sendSwitchKitPage(direction);
            });
            return button;
        }

        private UIElement beltSpacer() {
            UIElement spacer = new UIElement().layout(layout -> layout.flex(1).height(1));
            spacer.setAllowHitTest(false);
            return spacer;
        }

        private UIElement beltDivider() {
            UIElement divider = panel(ISLAND_BORDER).layout(layout -> layout.width(1).height(BELT_DIVIDER_HEIGHT));
            divider.setAllowHitTest(false);
            return divider;
        }

        private int beltSlotChromeColor(SlotWorkspaceViewModel.HotbarSlot slot, boolean selected) {
            if (selected) {
                return SELECTED;
            }
            return slot.selected() ? ACTIVE_HOTBAR : ROW;
        }

        private Button beltSlotButton(SlotWorkspaceViewModel.HotbarSlot slot) {
            boolean selected = selectedHotbarIndex.get() == slot.hotbarIndex();
            Button button = button("", true, beltSlotChromeColor(slot, selected));
            atlasContentSubscriptions.add(selectedHotbarIndex.subscribeLater(idx -> {
                applyButtonColors(button, true, beltSlotChromeColor(slot, idx == slot.hotbarIndex()));
            }));
            button.layout(layout -> layout
                    .width(BELT_SLOT_SIZE)
                    .height(BELT_SLOT_SIZE)
                    .paddingAll(1)
                    .alignItems(AlignItems.CENTER)
                    .flexDirection(FlexDirection.COLUMN));
            button.noText();
            button.setOnClick(event -> {
                event.stopPropagation();
                if (Screen.hasShiftDown() && slot.occupied()) {
                    sendReturnHotbarToHome(slot.hotbarIndex());
                    return;
                }
                SlotWorkspaceViewModel.AtlasItem atlasItem = selectedAtlasItem();
                if (atlasItem != null) {
                    sendAssignToHotbarSlot(atlasItem, slot.hotbarIndex());
                    return;
                }
                if (!slot.occupied()) {
                    selectedHotbarIndex.set(-1);
                    localStatus.set("belt " + (slot.hotbarIndex() + 1) + " is empty");
                    return;
                }
                selectedHotbarIndex.set(slot.hotbarIndex());
                selectedAtlasIdentity.set(null);
                localStatus.set("selected belt " + (slot.hotbarIndex() + 1) + " -> drag to atlas to return");
            });
            button.addEventListener(UIEvents.MOUSE_DOWN, event -> {
                if (event.button == 1 && slot.occupied()) {
                    event.stopPropagation();
                    openContextMenuForHotbar(slot, event.x, event.y);
                }
            });
            installHotbarDragSource(button, slot);
            installHotbarDropTarget(button, slot);
            installHotbarHoverTooltip(button, slot);
            button.addEventListener(UIEvents.MOUSE_ENTER, event -> {
                if (slot.occupied()) {
                    hoveredHotbarIndex = slot.hotbarIndex();
                }
            }, true);
            button.addEventListener(UIEvents.MOUSE_LEAVE, event -> {
                if (hoveredHotbarIndex == slot.hotbarIndex()) {
                    hoveredHotbarIndex = -1;
                }
            }, true);
            hotbarSlotElements.put(slot.hotbarIndex(), button);
            if (slot.occupied()) {
                boolean[] lastAccent = {false};
                button.addEventListener(UIEvents.TICK, event -> {
                    boolean accent = shouldAccentHotbarSlot(slot);
                    if (accent != lastAccent[0]) {
                        button.style(style -> style.overlayTexture(accent ? rect(HOVER_ACCENT_OVERLAY) : IGuiTexture.EMPTY));
                        lastAccent[0] = accent;
                    }
                });
            }

            UIElement iconSlot = slot.occupied() ? itemIcon(slot.displayStack(), 16) : emptyIcon();
            iconSlot.layout(layout -> layout.width(16).height(16));
            button.addChild(iconSlot);
            if (slot.occupied() && slot.count() > 1) {
                Label countBadge = label(compactCount(slot.count()), ACCENT);
                countBadge.layout(layout -> layout
                        .positionType(TaffyPosition.ABSOLUTE)
                        .right(1)
                        .bottom(0)
                        .height(6));
                countBadge.textStyle(style -> style
                        .textColor(ACCENT)
                        .fontSize(6)
                        .textShadow(true)
                        .textAlignHorizontal(Horizontal.RIGHT)
                        .textAlignVertical(Vertical.BOTTOM));
                countBadge.setAllowHitTest(false);
                button.addChild(countBadge);
            }
            Label indexBadge = label(Integer.toString(slot.hotbarIndex() + 1), slot.selected() ? WARNING : MUTED);
            indexBadge.layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(1)
                    .top(0)
                    .height(6));
            indexBadge.textStyle(style -> style
                    .textColor(slot.selected() ? WARNING : MUTED)
                    .fontSize(6)
                    .textShadow(true)
                    .textAlignHorizontal(Horizontal.LEFT)
                    .textAlignVertical(Vertical.TOP));
            indexBadge.setAllowHitTest(false);
            button.addChild(indexBadge);
            return button;
        }

        private UIElement offhandSlotButton(SlotWorkspaceViewModel.OffhandSlot offhand) {
            Button button = button("", false, ROW_DIM);
            button.layout(layout -> layout
                    .width(BELT_SLOT_SIZE)
                    .height(BELT_SLOT_SIZE)
                    .paddingAll(1)
                    .alignItems(AlignItems.CENTER)
                    .flexDirection(FlexDirection.COLUMN));
            button.noText();
            button.setActive(false);
            installOffhandHoverTooltip(button, offhand);
            UIElement iconSlot = offhand.occupied() ? itemIcon(offhand.displayStack(), 16) : emptyIcon();
            iconSlot.layout(layout -> layout.width(16).height(16));
            button.addChild(iconSlot);
            if (offhand.occupied() && offhand.count() > 1) {
                Label countBadge = label(compactCount(offhand.count()), MUTED);
                countBadge.layout(layout -> layout
                        .positionType(TaffyPosition.ABSOLUTE)
                        .right(1)
                        .bottom(0)
                        .height(6));
                countBadge.textStyle(style -> style
                        .textColor(MUTED)
                        .fontSize(6)
                        .textShadow(true)
                        .textAlignHorizontal(Horizontal.RIGHT)
                        .textAlignVertical(Vertical.BOTTOM));
                countBadge.setAllowHitTest(false);
                button.addChild(countBadge);
            }
            Label offLabel = label("off", MUTED);
            offLabel.layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(1)
                    .top(0)
                    .height(6));
            offLabel.textStyle(style -> style
                    .textColor(MUTED)
                    .fontSize(6)
                    .textShadow(true)
                    .textAlignHorizontal(Horizontal.LEFT)
                    .textAlignVertical(Vertical.TOP));
            offLabel.setAllowHitTest(false);
            button.addChild(offLabel);
            return button;
        }

        private UIElement kitRackOverlay() {
            // Dock flush with the belt top (belt sits at bottom(4) with height BELT_HEIGHT)
            // so the rack feels like an extension of the belt rather than a floating panel.
            UIElement overlay = panel(GLASS).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(16)
                    .right(16)
                    .bottom(BELT_HEIGHT + 4)
                    .height(kitRackHeight())
                    .paddingAll(6)
                    .gapAll(6)
                    .flexDirection(FlexDirection.COLUMN));
            overlay.style(style -> style.zIndex(7));
            overlay.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());
            overlay.addChild(kitRackHeader());
            overlay.addChild(kitRackBody());
            return overlay;
        }

        private UIElement kitRackHeader() {
            UIElement row = new UIElement().layout(layout -> layout
                    .widthPercent(100)
                    .height(16)
                    .gapAll(6)
                    .alignItems(AlignItems.CENTER)
                    .flexDirection(FlexDirection.ROW));
            int kitCount = viewModel.kits().size();
            Label title = label("Kits (" + kitCount + ")", ACCENT);
            title.layout(layout -> layout.flex(1).height(12));
            title.textStyle(style -> style
                    .textColor(ACCENT)
                    .textShadow(false)
                    .fontSize(9)
                    .textAlignHorizontal(Horizontal.LEFT)
                    .textAlignVertical(Vertical.CENTER));
            SlotWorkspaceViewModel.KitCard activeCard = viewModel.activeKit();
            String saveLabel;
            if (activeCard != null) {
                saveLabel = activeCard.pageCount() > 1
                        ? "Update Page " + (activeCard.activePageIndex() + 1)
                        : "Update Active Kit";
            } else {
                saveLabel = "Save Current Belt as Kit";
            }
            Button save = button(saveLabel, true, PANEL_ALT);
            save.layout(layout -> layout.width(Math.max(110, saveLabel.length() * 6 + 10)).height(14));
            save.textStyle(style -> style
                    .textColor(ACCENT)
                    .textShadow(false)
                    .fontSize(8)
                    .textAlignHorizontal(Horizontal.CENTER)
                    .textAlignVertical(Vertical.CENTER));
            save.setOnClick(event -> {
                event.stopPropagation();
                sendSaveKit();
            });
            Button close = button("x", true, PANEL_ALT);
            close.layout(layout -> layout.width(14).height(14));
            close.textStyle(style -> style
                    .textColor(MUTED)
                    .textShadow(false)
                    .fontSize(8)
                    .textAlignHorizontal(Horizontal.CENTER)
                    .textAlignVertical(Vertical.CENTER));
            close.setOnClick(event -> {
                event.stopPropagation();
                kitRackOpen = false;
                rebuild();
            });
            row.addChildren(title, save, close);
            return row;
        }

        private int kitRackBodyHeight() {
            int maxPageCount = 1;
            int maxBringCount = 0;
            for (SlotWorkspaceViewModel.KitCard card : viewModel.kits()) {
                if (card.pageCount() > maxPageCount) {
                    maxPageCount = card.pageCount();
                }
                if (card.bringSlotCount() > maxBringCount) {
                    maxBringCount = card.bringSlotCount();
                }
            }
            return Math.max(44, kitCardHeight(maxPageCount, maxBringCount) + 6);
        }

        private int kitRackHeight() {
            // padding (6 top + 6 bottom) + header row (14) + gap to body (6) + body height.
            // Kept as a single computation so triagePanelOverlay can lift itself above the
            // rack when it's open.
            return 12 + 14 + 6 + kitRackBodyHeight();
        }

        private UIElement kitRackBody() {
            int bodyHeight = kitRackBodyHeight();
            UIElement body = new UIElement().layout(layout -> layout
                    .widthPercent(100)
                    .height(bodyHeight)
                    .gapAll(6)
                    .alignItems(AlignItems.FLEX_START)
                    .flexDirection(FlexDirection.ROW));
            if (viewModel.kits().isEmpty()) {
                Label empty = label("No kits yet. Load your belt, then Save Current Belt.", MUTED);
                empty.layout(layout -> layout.flex(1).height(12));
                empty.textStyle(style -> style
                        .textColor(MUTED)
                        .textShadow(false)
                        .fontSize(8)
                        .textAlignHorizontal(Horizontal.CENTER)
                        .textAlignVertical(Vertical.CENTER));
                body.addChild(empty);
                return body;
            }
            for (SlotWorkspaceViewModel.KitCard card : viewModel.kits()) {
                body.addChild(kitCardButton(card));
            }
            return body;
        }

        private static final int KIT_CARD_WIDTH = 180;
        private static final int KIT_CELL_SIZE = 14;
        private static final int KIT_CELL_ICON_SIZE = 11;

        private static int kitCardHeight(int pageCount, int bringCount) {
            int pages = Math.max(1, pageCount);
            // header + per-page rows + add-page footer + bring row + bring label
            int bringHeight = bringCount > 0 ? 28 : 16;
            return 12 + pages * 18 + 14 + bringHeight;
        }

        private UIElement kitCardButton(SlotWorkspaceViewModel.KitCard card) {
            int baseColor = card.active() ? ACTIVE_HOTBAR : ROW;
            // Active card uses a brighter amber on hover so it stays visually distinct from
            // the inactive hover (which is slate). The default applyButtonColors hover path
            // maps any full-alpha base to ROW_HOVER — that makes active cards look inactive
            // on hover, which confuses "click to deactivate".
            int hoverColor = card.active() ? ACTIVE_HOTBAR_HOVER : ROW_HOVER;
            int pressedColor = card.active() ? ACTIVE_HOTBAR_PRESSED : SELECTED;
            Button button = new Button();
            button.setActive(true);
            button.buttonStyle(style -> style
                    .baseTexture(rect(baseColor))
                    .hoverTexture(rect(hoverColor))
                    .pressedTexture(rect(pressedColor)));
            button.textStyle(style -> style.font(FONT_UI).textColor(TEXT).textShadow(false).fontSize(8));
            button.layout(layout -> layout
                    .width(KIT_CARD_WIDTH)
                    .height(kitCardHeight(card.pageCount(), card.bringSlotCount()))
                    .paddingAll(4)
                    .gapAll(2)
                    .alignItems(AlignItems.CENTER)
                    .flexDirection(FlexDirection.COLUMN));
            button.noText();
            button.setOnClick(event -> {
                event.stopPropagation();
                if (card.active()) {
                    sendDeactivateKit();
                } else {
                    sendActivateKit(card.kitId());
                }
            });
            button.addEventListener(UIEvents.MOUSE_DOWN, event -> {
                if (event.button == 1) {
                    event.stopPropagation();
                    openContextMenuForKit(card.kitId(), event.x, event.y);
                }
            });
            button.addChild(kitCardHeader(card));
            for (SlotWorkspaceViewModel.KitPageView page : card.pages()) {
                button.addChild(kitCardPageRow(card, page));
            }
            button.addChild(kitCardAddPageRow(card));
            button.addChild(kitCardBringRow(card));
            return button;
        }

        private UIElement kitCardHeader(SlotWorkspaceViewModel.KitCard card) {
            UIElement row = new UIElement().layout(layout -> layout
                    .widthPercent(100)
                    .height(12)
                    .gapAll(2)
                    .alignItems(AlignItems.CENTER)
                    .flexDirection(FlexDirection.ROW));
            Label name = label(shorten(card.name(), 18), card.active() ? TEXT : TEXT);
            name.layout(layout -> layout.flex(1).height(10));
            name.textStyle(style -> style
                    .textColor(TEXT)
                    .textShadow(false)
                    .fontSize(8)
                    .textAlignHorizontal(Horizontal.LEFT)
                    .textAlignVertical(Vertical.CENTER));
            name.setAllowHitTest(false);
            int aggregateSlots = 0;
            int aggregateReady = 0;
            for (SlotWorkspaceViewModel.KitPageView page : card.pages()) {
                aggregateSlots += page.slotCount();
                aggregateReady += page.readyCount();
            }
            final int totalSlots = aggregateSlots;
            final int totalReady = aggregateReady;
            Label readiness = label(totalReady + "/" + totalSlots,
                    totalReady == totalSlots ? ACCENT : WARNING);
            readiness.layout(layout -> layout.width(26).height(10));
            readiness.textStyle(style -> style
                    .textColor(totalReady == totalSlots ? ACCENT : WARNING)
                    .textShadow(false)
                    .fontSize(8)
                    .textAlignHorizontal(Horizontal.RIGHT)
                    .textAlignVertical(Vertical.CENTER));
            readiness.setAllowHitTest(false);
            // Delete moved to the right-click menu with confirm to prevent
            // fat-finger loss of a 10-minute kit setup.
            row.addChildren(name, readiness);
            return row;
        }

        private UIElement kitCardPageRow(SlotWorkspaceViewModel.KitCard card, SlotWorkspaceViewModel.KitPageView page) {
            boolean isActivePage = card.active() && card.activePageIndex() == page.pageIndex();
            UIElement row = isActivePage
                    ? panel(ACTIVE_PAGE_ROW)
                    : new UIElement();
            row.layout(layout -> layout
                    .widthPercent(100)
                    .height(KIT_CELL_SIZE + 2)
                    .paddingHorizontal(2)
                    .gapAll(2)
                    .alignItems(AlignItems.CENTER)
                    .flexDirection(FlexDirection.ROW));
            Label pageLabel = label(String.valueOf(page.pageIndex() + 1), isActivePage ? ACCENT : MUTED);
            pageLabel.layout(layout -> layout.width(8).height(12));
            pageLabel.textStyle(style -> style
                    .textColor(isActivePage ? ACCENT : MUTED)
                    .textShadow(false)
                    .fontSize(8)
                    .textAlignHorizontal(Horizontal.CENTER)
                    .textAlignVertical(Vertical.CENTER));
            pageLabel.setAllowHitTest(false);
            row.addChild(pageLabel);
            UIElement strip = new UIElement().layout(layout -> layout
                    .flex(1)
                    .height(KIT_CELL_SIZE)
                    .gapAll(1)
                    .alignItems(AlignItems.CENTER)
                    .flexDirection(FlexDirection.ROW));
            for (SlotWorkspaceViewModel.KitSlotState slot : page.slots()) {
                strip.addChild(kitCardSlotCell(card, page, slot));
            }
            row.addChild(strip);
            if (card.pageCount() > 1) {
                Button remove = button("-", true, PANEL_ALT);
                remove.layout(layout -> layout.width(12).height(12));
                remove.textStyle(style -> style
                        .textColor(MUTED)
                        .textShadow(false)
                        .fontSize(8)
                        .textAlignHorizontal(Horizontal.CENTER)
                        .textAlignVertical(Vertical.CENTER));
                remove.setOnClick(event -> {
                    event.stopPropagation();
                    sendRemoveKitPage(card.kitId(), page.pageIndex());
                });
                row.addChild(remove);
            }
            return row;
        }

        private UIElement kitCardAddPageRow(SlotWorkspaceViewModel.KitCard card) {
            UIElement row = new UIElement().layout(layout -> layout
                    .widthPercent(100)
                    .height(14)
                    .gapAll(2)
                    .alignItems(AlignItems.CENTER)
                    .flexDirection(FlexDirection.ROW));
            boolean canAdd = (card.carriedSlotCount() + KitPage.HOTBAR_SLOT_COUNT) <= card.carriedSlotCapacity();
            Button add = button(canAdd ? "+ page" : "+ page (full)", true, canAdd ? PANEL_ALT : 0x40202020);
            add.layout(layout -> layout.flex(1).height(12));
            add.textStyle(style -> style
                    .textColor(canAdd ? MUTED : 0x80808080)
                    .textShadow(false)
                    .fontSize(8)
                    .textAlignHorizontal(Horizontal.CENTER)
                    .textAlignVertical(Vertical.CENTER));
            if (canAdd) {
                add.setOnClick(event -> {
                    event.stopPropagation();
                    sendAddKitPage(card.kitId());
                });
            } else {
                add.setOnClick(event -> event.stopPropagation());
            }
            row.addChild(add);
            int missing = kitMissingIdentityCount(card);
            if (missing > 0) {
                // Dark background + amber text keeps the button readable against
                // both the card's active-kit olive tint and the inactive ROW bg.
                Button gather = button("gather " + missing, true, PANEL_ALT);
                gather.layout(layout -> layout.flex(1).height(12));
                gather.textStyle(style -> style
                        .textColor(WARNING)
                        .textShadow(false)
                        .fontSize(8)
                        .textAlignHorizontal(Horizontal.CENTER)
                        .textAlignVertical(Vertical.CENTER));
                gather.setOnClick(event -> {
                    event.stopPropagation();
                    advanceGather(card);
                });
                row.addChild(gather);
            }
            return row;
        }

        private int kitMissingIdentityCount(SlotWorkspaceViewModel.KitCard card) {
            int count = 0;
            for (SlotWorkspaceViewModel.KitPageView page : card.pages()) {
                for (SlotWorkspaceViewModel.KitSlotState slot : page.slots()) {
                    if (slot.filled() && !slot.ready()) {
                        count++;
                    }
                }
            }
            for (SlotWorkspaceViewModel.KitBringItem item : card.bring()) {
                if (!item.ready()) {
                    count++;
                }
            }
            return count;
        }

        private List<SlotWorkspaceViewModel.IdentityRef> kitMissingIdentities(SlotWorkspaceViewModel.KitCard card) {
            java.util.LinkedHashSet<SlotWorkspaceViewModel.IdentityRef> missing = new java.util.LinkedHashSet<>();
            for (SlotWorkspaceViewModel.KitPageView page : card.pages()) {
                for (SlotWorkspaceViewModel.KitSlotState slot : page.slots()) {
                    if (slot.filled() && !slot.ready()) {
                        missing.add(slot.identity());
                    }
                }
            }
            for (SlotWorkspaceViewModel.KitBringItem item : card.bring()) {
                if (!item.ready()) {
                    missing.add(item.identity());
                }
            }
            return List.copyOf(missing);
        }

        private void advanceGather(SlotWorkspaceViewModel.KitCard card) {
            List<SlotWorkspaceViewModel.IdentityRef> missing = kitMissingIdentities(card);
            if (missing.isEmpty()) {
                localStatus.set("nothing to gather");
                rebuild();
                return;
            }
            if (!card.kitId().equals(gatherKitId)) {
                gatherKitId = card.kitId();
                gatherStep = 0;
            }
            int step = Math.floorMod(gatherStep, missing.size());
            SlotWorkspaceViewModel.IdentityRef identity = missing.get(step);
            SlotWorkspaceViewModel.AtlasItem atlasItem = viewModel.atlasItem(identity);
            if (atlasItem != null) {
                SlotWorkspaceViewModel.AtlasIsland island = viewModel.island(atlasItem.islandId());
                if (island != null && atlasView != null) {
                    panToIsland(atlasView, island);
                }
                localStatus.set("gather " + (step + 1) + "/" + missing.size() + ": " + atlasItem.name());
            } else {
                localStatus.set("gather " + (step + 1) + "/" + missing.size() + ": " + identity.itemId() + " (no home)");
            }
            gatherStep = (step + 1) % missing.size();
            rebuild();
        }

        private UIElement kitCardBringRow(SlotWorkspaceViewModel.KitCard card) {
            UIElement column = new UIElement().layout(layout -> layout
                    .widthPercent(100)
                    .height(card.bringSlotCount() > 0 ? 28 : 16)
                    .gapAll(2)
                    .alignItems(AlignItems.FLEX_START)
                    .flexDirection(FlexDirection.COLUMN));
            String header = "bring " + card.bringReadyCount() + "/" + card.bringSlotCount();
            Label title = label(header, card.bringSlotCount() == 0 ? MUTED
                    : card.bringReadyCount() == card.bringSlotCount() ? ACCENT : WARNING);
            title.layout(layout -> layout.widthPercent(100).height(10));
            title.textStyle(style -> style
                    .textColor(card.bringSlotCount() == 0 ? MUTED
                            : card.bringReadyCount() == card.bringSlotCount() ? ACCENT : WARNING)
                    .textShadow(false)
                    .fontSize(8)
                    .textAlignHorizontal(Horizontal.LEFT)
                    .textAlignVertical(Vertical.CENTER));
            title.setAllowHitTest(false);
            column.addChild(title);
            UIElement strip = new UIElement().layout(layout -> layout
                    .widthPercent(100)
                    .height(KIT_CELL_SIZE)
                    .gapAll(1)
                    .alignItems(AlignItems.CENTER)
                    .flexDirection(FlexDirection.ROW));
            for (SlotWorkspaceViewModel.KitBringItem item : card.bring()) {
                strip.addChild(kitCardBringCell(card, item));
            }
            installKitBringDropTarget(strip, card);
            column.addChild(strip);
            return column;
        }

        private UIElement kitCardBringCell(SlotWorkspaceViewModel.KitCard card, SlotWorkspaceViewModel.KitBringItem item) {
            int fill = item.ready() ? ROW : ROW_DIM;
            UIElement cell = panel(fill).layout(layout -> layout
                    .width(KIT_CELL_SIZE)
                    .height(KIT_CELL_SIZE)
                    .paddingAll(1)
                    .alignItems(AlignItems.CENTER));
            cell.addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
                if (item.displayStack().isEmpty()) {
                    return;
                }
                event.hoverTooltips = new HoverTooltips(
                        List.copyOf(DrawerHelper.getItemToolTip(item.displayStack())),
                        item.displayStack().getTooltipImage().orElse(null),
                        null,
                        item.displayStack()
                );
            });
            cell.addEventListener(UIEvents.MOUSE_DOWN, event -> {
                if (event.button == 0) {
                    event.stopPropagation();
                    return;
                }
                if (event.button == 1) {
                    event.stopPropagation();
                    sendRemoveKitBring(card.kitId(), item.identity());
                }
            });
            cell.addEventListener(UIEvents.MOUSE_LEAVE, event -> {
                if (!cell.isMouseDown(0) || isDragging(cell)) {
                    return;
                }
                cell.startDrag(
                        new KitBringDrag(card.kitId(), item.identity(), item.displayStack().copy()),
                        dragTexture(item.displayStack())
                ).setDragTexture(-10, -10, 20, 20);
                localStatus.set("dragging kit bring");
            }, true);
            cell.addEventListener(UIEvents.DRAG_END, this::handleDragEnd);
            if (!item.displayStack().isEmpty()) {
                UIElement icon = itemIcon(item.displayStack(), KIT_CELL_ICON_SIZE, item.ready());
                icon.setAllowHitTest(false);
                cell.addChild(icon);
            }
            return cell;
        }

        private void installKitBringDropTarget(UIElement target, SlotWorkspaceViewModel.KitCard card) {
            target.addEventListener(UIEvents.DRAG_ENTER, event -> updateKitBringDropOverlay(target, event), true);
            target.addEventListener(UIEvents.DRAG_UPDATE, event -> updateKitBringDropOverlay(target, event));
            target.addEventListener(UIEvents.DRAG_LEAVE, event -> clearDropOverlay(target), true);
            target.addEventListener(UIEvents.DRAG_PERFORM, event -> {
                clearDropOverlay(target);
                SlotWorkspaceViewModel.IdentityRef identity = kitDropIdentity(event);
                if (identity == null) {
                    return;
                }
                sendAddKitBring(card.kitId(), identity);
                event.stopPropagation();
            });
        }

        private void updateKitBringDropOverlay(UIElement target, UIEvent event) {
            SlotWorkspaceViewModel.IdentityRef identity = kitDropIdentity(event);
            updateGenericDropOverlay(target, identity != null, ACCENT);
        }

        private SlotWorkspaceViewModel.IdentityRef kitDropIdentity(UIEvent event) {
            AtlasItemDrag atlasItem = atlasItemDrag(event);
            if (atlasItem != null) {
                return atlasItem.identity();
            }
            HotbarSlotDrag hotbar = hotbarSlotDrag(event);
            if (hotbar != null && !hotbar.displayStack().isEmpty()) {
                return SlotWorkspaceViewModel.IdentityRef.from(
                        dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(hotbar.displayStack()));
            }
            KitSlotDrag slotDrag = kitSlotDrag(event);
            if (slotDrag != null && slotDrag.identity() != null && !slotDrag.identity().itemId().isBlank()) {
                return slotDrag.identity();
            }
            KitBringDrag bringDrag = kitBringDrag(event);
            if (bringDrag != null) {
                return bringDrag.identity();
            }
            return null;
        }

        private KitSlotDrag kitSlotDrag(UIEvent event) {
            Object payload = event == null || event.dragHandler == null ? null : event.dragHandler.getDraggingObject();
            return payload instanceof KitSlotDrag slotDrag ? slotDrag : null;
        }

        private KitBringDrag kitBringDrag(UIEvent event) {
            Object payload = event == null || event.dragHandler == null ? null : event.dragHandler.getDraggingObject();
            return payload instanceof KitBringDrag bringDrag ? bringDrag : null;
        }

        private UIElement kitCardSlotCell(
                SlotWorkspaceViewModel.KitCard card,
                SlotWorkspaceViewModel.KitPageView page,
                SlotWorkspaceViewModel.KitSlotState slot
        ) {
            int fill = !slot.filled() ? 0x60141B22 : slot.ready() ? ROW : ROW_DIM;
            UIElement cell = panel(fill).layout(layout -> layout
                    .width(KIT_CELL_SIZE)
                    .height(KIT_CELL_SIZE)
                    .paddingAll(1)
                    .alignItems(AlignItems.CENTER));
            if (slot.filled() && !slot.displayStack().isEmpty()) {
                UIElement icon = itemIcon(slot.displayStack(), KIT_CELL_ICON_SIZE, slot.ready());
                icon.setAllowHitTest(false);
                cell.addChild(icon);
                cell.addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
                    if (slot.displayStack().isEmpty()) {
                        return;
                    }
                    event.hoverTooltips = new HoverTooltips(
                            List.copyOf(DrawerHelper.getItemToolTip(slot.displayStack())),
                            slot.displayStack().getTooltipImage().orElse(null),
                            null,
                            slot.displayStack()
                    );
                });
                cell.addEventListener(UIEvents.MOUSE_LEAVE, event -> {
                    if (!cell.isMouseDown(0) || isDragging(cell)) {
                        return;
                    }
                    cell.startDrag(
                            new KitSlotDrag(card.kitId(), page.pageIndex(), slot.slotIndex(), slot.identity(), slot.displayStack().copy()),
                            dragTexture(slot.displayStack())
                    ).setDragTexture(-10, -10, 20, 20);
                    localStatus.set("dragging kit slot");
                }, true);
                cell.addEventListener(UIEvents.DRAG_END, this::handleDragEnd);
            }
            cell.addEventListener(UIEvents.MOUSE_DOWN, event -> {
                if (event.button == 0) {
                    // Stop left-click mouse-down so the surrounding kit-card Button doesn't
                    // arm its click tracker. Otherwise a drag-rearrange gesture that ends
                    // with mouse-up back inside the card fires activate/deactivate.
                    event.stopPropagation();
                    return;
                }
                if (event.button == 1 && slot.filled()) {
                    event.stopPropagation();
                    sendSetKitSlotIdentity(card.kitId(), page.pageIndex(), slot.slotIndex(), null);
                }
                // Right-click on an empty slot: let it bubble so the card context menu
                // still opens for rename / duplicate / delete.
            });
            installKitSlotDropTarget(cell, card, page, slot);
            return cell;
        }

        private void installKitSlotDropTarget(
                UIElement target,
                SlotWorkspaceViewModel.KitCard card,
                SlotWorkspaceViewModel.KitPageView page,
                SlotWorkspaceViewModel.KitSlotState slot
        ) {
            target.addEventListener(UIEvents.DRAG_ENTER, event -> updateKitSlotDropOverlay(target, event), true);
            target.addEventListener(UIEvents.DRAG_UPDATE, event -> updateKitSlotDropOverlay(target, event));
            target.addEventListener(UIEvents.DRAG_LEAVE, event -> clearDropOverlay(target), true);
            target.addEventListener(UIEvents.DRAG_PERFORM, event -> {
                clearDropOverlay(target);
                // Drag within the same kit page: rearrange via swap so the source cell
                // also updates, not just the drop target.
                KitSlotDrag slotDrag = kitSlotDrag(event);
                if (slotDrag != null
                        && card.kitId().equals(slotDrag.kitId())
                        && page.pageIndex() == slotDrag.pageIndex()) {
                    if (slotDrag.slotIndex() != slot.slotIndex()) {
                        sendSwapKitSlots(card.kitId(), page.pageIndex(), slotDrag.slotIndex(), slot.slotIndex());
                    }
                    event.stopPropagation();
                    return;
                }
                SlotWorkspaceViewModel.IdentityRef identity = kitDropIdentity(event);
                if (identity == null) {
                    return;
                }
                sendSetKitSlotIdentity(card.kitId(), page.pageIndex(), slot.slotIndex(), identity);
                event.stopPropagation();
            });
        }

        private void updateKitSlotDropOverlay(UIElement target, UIEvent event) {
            SlotWorkspaceViewModel.IdentityRef identity = kitDropIdentity(event);
            updateGenericDropOverlay(target, identity != null, ACCENT);
        }

        private void sendSaveKit() {
            boolean sent = saveKitEmitter != null && saveKitEmitter.send("");
            localStatus.set(sent ? "saving kit..." : "save kit unavailable");
            rebuild();
        }

        private void sendActivateKit(String kitId) {
            boolean sent = activateKitEmitter != null && activateKitEmitter.send(kitId);
            localStatus.set(sent ? "activating kit..." : "activate kit unavailable");
            rebuild();
        }

        private void sendDeactivateKit() {
            boolean sent = deactivateKitEmitter != null && deactivateKitEmitter.send();
            localStatus.set(sent ? "deactivating kit..." : "deactivate kit unavailable");
            rebuild();
        }

        private void sendDeleteKit(String kitId) {
            boolean sent = deleteKitEmitter != null && deleteKitEmitter.send(kitId);
            localStatus.set(sent ? "deleting kit..." : "delete kit unavailable");
            rebuild();
        }

        private void sendSwitchKitPage(int direction) {
            boolean sent = switchKitPageEmitter != null && switchKitPageEmitter.send(direction);
            localStatus.set(sent ? "switching kit page..." : "page switch unavailable");
            rebuild();
        }

        private void sendAddKitPage(String kitId) {
            boolean sent = addKitPageEmitter != null && addKitPageEmitter.send(kitId);
            localStatus.set(sent ? "adding kit page..." : "add page unavailable");
            rebuild();
        }

        private void sendRemoveKitPage(String kitId, int pageIndex) {
            boolean sent = removeKitPageEmitter != null && removeKitPageEmitter.send(kitId, pageIndex);
            localStatus.set(sent ? "removing kit page..." : "remove page unavailable");
            rebuild();
        }

        private void sendAddKitBring(String kitId, SlotWorkspaceViewModel.IdentityRef identity) {
            if (identity == null) {
                return;
            }
            boolean sent = addKitBringEmitter != null && addKitBringEmitter.send(
                    kitId, identity.itemId(), identity.comparisonMode(), identity.componentFingerprint());
            localStatus.set(sent ? "adding to bring..." : "add bring unavailable");
            rebuild();
        }

        private void sendRemoveKitBring(String kitId, SlotWorkspaceViewModel.IdentityRef identity) {
            if (identity == null) {
                return;
            }
            boolean sent = removeKitBringEmitter != null && removeKitBringEmitter.send(
                    kitId, identity.itemId(), identity.comparisonMode(), identity.componentFingerprint());
            localStatus.set(sent ? "removing from bring..." : "remove bring unavailable");
            rebuild();
        }

        private void sendSwapKitSlots(String kitId, int pageIndex, int fromIndex, int toIndex) {
            boolean sent = swapKitSlotsEmitter != null
                    && swapKitSlotsEmitter.send(kitId, pageIndex, fromIndex, toIndex);
            localStatus.set(sent ? "swapping kit slots..." : "swap slots unavailable");
            rebuild();
        }

        private void sendSetKitSlotIdentity(String kitId, int pageIndex, int slotIndex, SlotWorkspaceViewModel.IdentityRef identity) {
            String itemId = identity == null ? "" : identity.itemId();
            String comparisonMode = identity == null ? "" : identity.comparisonMode();
            String fingerprint = identity == null ? "" : identity.componentFingerprint();
            boolean sent = setKitSlotIdentityEmitter != null && setKitSlotIdentityEmitter.send(
                    kitId, pageIndex, slotIndex, itemId, comparisonMode, fingerprint);
            localStatus.set(sent ? "updating kit slot..." : "update slot unavailable");
            rebuild();
        }

        private void installHotbarHoverTooltip(Button button, SlotWorkspaceViewModel.HotbarSlot slot) {
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

        private void installOffhandHoverTooltip(Button button, SlotWorkspaceViewModel.OffhandSlot offhand) {
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

        private UIElement statusBar() {
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

        private void refreshStatusBarLabel() {
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

        private void sendTransfer(int sourceKind, int sourceIndex, int destinationKind, int destinationIndex) {
            boolean sent = transferEmitter != null && transferEmitter.send(
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

        private void sendAssignHome(String islandId) {
            SlotWorkspaceViewModel.AtlasItem item = selectedAtlasItem();
            if (item == null) {
                localStatus.set("select an atlas item first");
                rebuild();
                return;
            }
            sendAssignHome(item.identity(), islandId, -1, -1);
        }

        private void sendAssignHome(
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
            boolean sent = homeEmitter != null && homeEmitter.send(
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

        private void rememberRehomeTarget(String islandId) {
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

        private List<SlotWorkspaceViewModel.AtlasIsland> recentRehomeTargets(SlotWorkspaceViewModel.AtlasItem forItem) {
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

        private void sendReturnHotbarToHome(int hotbarIndex) {
            if (returnHotbarToHomeEmitter == null) {
                return;
            }
            boolean sent = returnHotbarToHomeEmitter.send(hotbarIndex);
            if (!sent) {
                localStatus.set("return-to-home unavailable");
                rebuild();
            }
        }

        private void sendAssignHomeToFreeHotbar(SlotWorkspaceViewModel.AtlasItem item) {
            if (assignHomeToFreeHotbarEmitter == null || item == null) {
                return;
            }
            boolean sent = assignHomeToFreeHotbarEmitter.send(
                    item.identity().itemId(),
                    item.identity().comparisonMode(),
                    item.identity().componentFingerprint()
            );
            if (!sent) {
                localStatus.set("assign-to-hotbar unavailable");
                rebuild();
            }
        }

        private void sendAssignHomeToHotbarOnly(SlotWorkspaceViewModel.AtlasItem item) {
            if (assignHomeToHotbarOnlyEmitter == null || item == null) {
                return;
            }
            boolean sent = assignHomeToHotbarOnlyEmitter.send(
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
        private void sendAssignToHotbarSlot(SlotWorkspaceViewModel.AtlasItem item, int hotbarIndex) {
            if (assignIdentityToHotbarSlotEmitter == null || item == null) {
                return;
            }
            boolean sent = assignIdentityToHotbarSlotEmitter.send(
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

        private void sendDepositHomeToLinkedChest(SlotWorkspaceViewModel.AtlasItem item) {
            if (depositHomeToLinkedChestEmitter == null || item == null) {
                return;
            }
            boolean sent = depositHomeToLinkedChestEmitter.send(
                    item.identity().itemId(),
                    item.identity().comparisonMode(),
                    item.identity().componentFingerprint()
            );
            if (!sent) {
                localStatus.set("deposit unavailable");
                rebuild();
            }
        }

        private void sendDepositOneHomeToLinkedChest(SlotWorkspaceViewModel.AtlasItem item) {
            if (depositOneHomeToLinkedChestEmitter == null || item == null) {
                return;
            }
            boolean sent = depositOneHomeToLinkedChestEmitter.send(
                    item.identity().itemId(),
                    item.identity().comparisonMode(),
                    item.identity().componentFingerprint()
            );
            if (!sent) {
                localStatus.set("deposit unavailable");
                rebuild();
            }
        }

        private void sendTakeOneFromChest(String storageId, int chestSlotIndex) {
            if (takeOneFromChestEmitter == null || storageId == null || storageId.isBlank()) {
                return;
            }
            boolean sent = takeOneFromChestEmitter.send(storageId, chestSlotIndex);
            if (!sent) {
                localStatus.set("take unavailable");
                rebuild();
            }
        }

        private int hotbarSlotForIdentity(SlotWorkspaceViewModel.IdentityRef identity) {
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

        private void sendMoveHotbarToAtlas(int hotbarIndex, String islandId, int worldX, int worldY) {
            boolean sent = hotbarToAtlasEmitter != null && hotbarToAtlasEmitter.send(
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

        private void sendMoveIsland(String islandId, int worldX, int worldY) {
            if (islandId == null || islandId.isBlank()) {
                localStatus.set("invalid island move");
                rebuild();
                return;
            }
            boolean sent = moveIslandEmitter != null && moveIslandEmitter.send(
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

        private void sendTakeAll(String storageId) {
            boolean sent = takeAllEmitter != null && takeAllEmitter.send(storageId);
            localStatus.set(sent ? "take-all requested" : "take-all unavailable");
            rebuild();
        }

        private void sendDeposit() {
            boolean sent = depositEmitter != null && depositEmitter.send();
            localStatus.set(sent ? "deposit requested" : "deposit unavailable");
            rebuild();
        }

        private boolean anyChestProximate() {
            for (SlotWorkspaceViewModel.ClaimedChestTile tile : viewModel.claimedChestTiles()) {
                if (tile.proximate()) {
                    return true;
                }
            }
            return false;
        }

        private void sendLinkChest(String islandId, String storageId) {
            if (islandId == null || islandId.isBlank() || storageId == null || storageId.isBlank()) {
                localStatus.set("invalid chest link");
                rebuild();
                return;
            }
            boolean sent = linkChestEmitter != null && linkChestEmitter.send(islandId, storageId);
            localStatus.set(sent ? "chest link requested" : "chest link unavailable");
            rebuild();
        }

        private void sendUnlinkChest(String islandId, String storageId) {
            if (islandId == null || islandId.isBlank() || storageId == null || storageId.isBlank()) {
                localStatus.set("invalid chest unlink");
                rebuild();
                return;
            }
            boolean sent = unlinkChestEmitter != null && unlinkChestEmitter.send(islandId, storageId);
            localStatus.set(sent ? "chest unlink requested" : "chest unlink unavailable");
            rebuild();
        }

        private void sendMoveStorageZone(int deltaX, int deltaY) {
            if (deltaX == 0 && deltaY == 0) {
                return;
            }
            boolean sent = moveStorageZoneEmitter != null && moveStorageZoneEmitter.send(deltaX, deltaY);
            localStatus.set(sent ? "storage zone moved" : "storage zone move unavailable");
            rebuild();
        }

        private void sendMoveChest(String storageId, int atlasX, int atlasY) {
            if (storageId == null || storageId.isBlank()) {
                localStatus.set("invalid chest move");
                rebuild();
                return;
            }
            boolean sent = moveChestEmitter != null && moveChestEmitter.send(
                    storageId,
                    atlasX,
                    atlasY
            );
            localStatus.set(sent ? "chest move requested" : "chest move unavailable");
            rebuild();
        }

        private void installAtlasItemDragSource(UIElement source, SlotWorkspaceViewModel.AtlasItem item) {
            source.addEventListener(UIEvents.MOUSE_LEAVE, event -> {
                if (!source.isMouseDown(0) || isDragging(source)) {
                    return;
                }
                source.startDrag(
                        new AtlasItemDrag(item.identity(), item.displayStack().copy(), item.islandId()),
                        dragTexture(item.displayStack())
                ).setDragTexture(-10, -10, 20, 20);
                localStatus.set("dragging " + item.name());
            }, true);
            source.addEventListener(UIEvents.DRAG_END, event -> handleDragEnd(event));
        }

        private void installIslandDragSource(
                UIElement source,
                SlotAtlasGraphView atlas,
                SlotWorkspaceViewModel.AtlasIsland island
        ) {
            if (island.kind() != VisualAtlasIslandKind.PLAYER) {
                return;
            }
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
                if (!source.isMouseDown(0) || isDragging(source)) {
                    return;
                }
                float scale = atlas.getScale();
                float screenDx = (atlas.worldX(event.x) - clickWorldX[0]) * scale;
                float screenDy = (atlas.worldY(event.y) - clickWorldY[0]) * scale;
                if (screenDx * screenDx + screenDy * screenDy < DRAG_START_THRESHOLD_PX * DRAG_START_THRESHOLD_PX) {
                    return;
                }
                int grabOffsetX = Math.max(0, Math.min(island.width(), clickWorldX[0] - island.x()));
                int grabOffsetY = Math.max(0, Math.min(island.height(), clickWorldY[0] - island.y()));
                // Render the ghost at the actual island screen size (no minimum
                // clamp — small islands got spuriously wide ghosts). Cap at a
                // reasonable maximum so huge islands don't occlude the viewport.
                int actualWidthPx = atlas.screenPixelsForWorldUnits(island.width());
                int actualHeightPx = atlas.screenPixelsForWorldUnits(island.height());
                float dragScale = Math.min(1f, Math.min(260f / Math.max(1, actualWidthPx), 180f / Math.max(1, actualHeightPx)));
                int dragWidthPx = Math.max(1, Math.round(actualWidthPx * dragScale));
                int dragHeightPx = Math.max(1, Math.round(actualHeightPx * dragScale));
                // The island title bar lives above the island rect; include a
                // proportional strip in the ghost so it represents the whole
                // island shape the player sees.
                int headerHeightPx = Math.max(6, Math.round(14f * dragScale));
                int dragOffsetX = Math.round(grabOffsetX * scale * dragScale);
                int dragOffsetY = Math.round(grabOffsetY * scale * dragScale);
                source.startDrag(
                        new IslandDrag(island.islandId(), grabOffsetX, grabOffsetY),
                        rect((island.color() & 0x00FFFFFF) | 0x5A000000)
                ).setDragTexture(
                        -dragOffsetX,
                        -(dragOffsetY + headerHeightPx),
                        dragWidthPx,
                        dragHeightPx + headerHeightPx);
                localStatus.set("dragging island " + island.label());
            }, true);
            source.addEventListener(UIEvents.DRAG_END, event -> handleDragEnd(event));
        }

        private void installAtlasHoverTooltip(Button button, SlotWorkspaceViewModel.AtlasItem item) {
            button.addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
                if (item == null || item.displayStack().isEmpty()) {
                    return;
                }
                event.hoverTooltips = new HoverTooltips(
                        atlasTooltipLines(item),
                        item.displayStack().getTooltipImage().orElse(null),
                        null,
                        item.displayStack()
                );
            });
        }

        private boolean hotbarDragHasHome(HotbarSlotDrag drag) {
            if (drag == null || drag.displayStack() == null || drag.displayStack().isEmpty()) {
                return false;
            }
            SlotWorkspaceViewModel.IdentityRef identity = SlotWorkspaceViewModel.IdentityRef.from(
                    dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(drag.displayStack()));
            SlotWorkspaceViewModel.AtlasItem atlasItem = atlasItemInIslandLayer(identity);
            if (atlasItem == null) {
                return false;
            }
            String islandId = atlasItem.islandId();
            return islandId != null
                    && !islandId.isBlank()
                    && !SlotWorkspaceAtlasLayout.ISLAND_TRIAGE.equals(islandId);
        }

        private void installHotbarDragSource(UIElement source, SlotWorkspaceViewModel.HotbarSlot slot) {
            if (!slot.occupied()) {
                return;
            }
            source.addEventListener(UIEvents.MOUSE_LEAVE, event -> {
                if (!source.isMouseDown(0) || isDragging(source)) {
                    return;
                }
                source.startDrag(
                        new HotbarSlotDrag(slot.hotbarIndex(), slot.displayStack().copy()),
                        dragTexture(slot.displayStack())
                ).setDragTexture(-10, -10, 20, 20);
                localStatus.set("dragging hotbar " + (slot.hotbarIndex() + 1));
            }, true);
            source.addEventListener(UIEvents.DRAG_END, event -> handleDragEnd(event));
        }

        private void installHotbarDropTarget(Button target, SlotWorkspaceViewModel.HotbarSlot slot) {
            target.addEventListener(UIEvents.DRAG_ENTER, event -> updateHotbarDropOverlay(target, slot, event), true);
            target.addEventListener(UIEvents.DRAG_UPDATE, event -> updateHotbarDropOverlay(target, slot, event));
            target.addEventListener(UIEvents.DRAG_LEAVE, event -> clearDropOverlay(target), true);
            target.addEventListener(UIEvents.DRAG_PERFORM, event -> {
                clearDropOverlay(target);
                HotbarSlotDrag hotbarDrag = hotbarSlotDrag(event);
                if (hotbarDrag != null) {
                    // Drag between two hotbar slots = swap. ASSIGN against two player-bound
                    // quick-access slots swaps their contents atomically.
                    if (hotbarDrag.hotbarIndex() != slot.hotbarIndex()) {
                        sendTransfer(
                                SlotWorkspaceUiSession.TARGET_HOTBAR_SLOT, hotbarDrag.hotbarIndex(),
                                SlotWorkspaceUiSession.TARGET_HOTBAR_SLOT, slot.hotbarIndex());
                    }
                    event.stopPropagation();
                    return;
                }
                AtlasItemDrag drag = atlasItemDrag(event);
                if (drag == null) {
                    // No recognized drag type: still stop propagation so the drop doesn't
                    // fall through to an atlas card positioned behind the belt.
                    event.stopPropagation();
                    return;
                }
                SlotWorkspaceViewModel.AtlasItem item = viewModel.atlasItem(drag.identity());
                if (item == null) {
                    localStatus.set("dragged item is no longer visible");
                    rebuild();
                    event.stopPropagation();
                    return;
                }
                if (!item.carried()) {
                    localStatus.set("can't move " + item.name() + " to hotbar — none carried");
                    rebuild();
                    event.stopPropagation();
                    return;
                }
                sendAssignToHotbarSlot(item, slot.hotbarIndex());
                event.stopPropagation();
            });
        }

        private void installAtlasBackgroundDropTarget(SlotAtlasGraphView atlas) {
            atlas.addEventListener(UIEvents.DRAG_ENTER, event -> updateAtlasBackgroundDropOverlay(atlas, event), true);
            atlas.addEventListener(UIEvents.DRAG_UPDATE, event -> updateAtlasBackgroundDropOverlay(atlas, event));
            atlas.addEventListener(UIEvents.DRAG_LEAVE, event -> clearDropOverlay(atlas), true);
            atlas.addEventListener(UIEvents.DRAG_PERFORM, event -> {
                if (!isDirectDragTarget(event, atlas)) {
                    return;
                }
                clearDropOverlay(atlas);
                AtlasItemDrag atlasItem = atlasItemDrag(event);
                if (atlasItem != null) {
                    int worldX = atlas.worldX(event.x);
                    int worldY = atlas.worldY(event.y);
                    if (wasDraggedFromTriage(atlasItem)) {
                        SlotWorkspaceViewModel.AtlasItem item = viewModel.atlasItem(atlasItem.identity());
                        if (item == null) {
                            localStatus.set("dragged item is no longer visible");
                            rebuild();
                            event.stopPropagation();
                            return;
                        }
                        beginCreateIsland(item, worldX, worldY);
                    } else {
                        sendAssignHome(
                                atlasItem.identity(),
                                SlotWorkspaceAtlasLayout.ISLAND_TRIAGE,
                                worldX,
                                worldY
                        );
                    }
                    event.stopPropagation();
                    return;
                }
                IslandDrag islandDrag = islandDrag(event);
                if (islandDrag != null) {
                    sendMoveIsland(
                            islandDrag.islandId(),
                            atlas.worldX(event.x) - islandDrag.grabOffsetX(),
                            atlas.worldY(event.y) - islandDrag.grabOffsetY()
                    );
                    event.stopPropagation();
                    return;
                }
                ChestTileDrag chestDrag = chestTileDrag(event);
                if (chestDrag != null) {
                    sendMoveChest(
                            chestDrag.storageId(),
                            atlas.worldX(event.x) - chestDrag.grabOffsetX(),
                            atlas.worldY(event.y) - chestDrag.grabOffsetY()
                    );
                    event.stopPropagation();
                    return;
                }
                StorageZoneDrag zoneDrag = storageZoneDrag(event);
                if (zoneDrag != null) {
                    int newLeft = atlas.worldX(event.x) - zoneDrag.grabOffsetX();
                    int newTop = atlas.worldY(event.y) - zoneDrag.grabOffsetY();
                    sendMoveStorageZone(newLeft - zoneDrag.originX(), newTop - zoneDrag.originY());
                    event.stopPropagation();
                    return;
                }
                HotbarSlotDrag hotbarItem = hotbarSlotDrag(event);
                if (hotbarItem != null) {
                    if (hotbarDragHasHome(hotbarItem)) {
                        sendReturnHotbarToHome(hotbarItem.hotbarIndex());
                    } else {
                        sendMoveHotbarToAtlas(
                                hotbarItem.hotbarIndex(),
                                SlotWorkspaceAtlasLayout.ISLAND_TRIAGE,
                                atlas.worldX(event.x),
                                atlas.worldY(event.y)
                        );
                    }
                    event.stopPropagation();
                    return;
                }
                KitSlotDrag kitSlot = kitSlotDrag(event);
                if (kitSlot != null) {
                    sendSetKitSlotIdentity(kitSlot.kitId(), kitSlot.pageIndex(), kitSlot.slotIndex(), null);
                    event.stopPropagation();
                    return;
                }
                KitBringDrag kitBring = kitBringDrag(event);
                if (kitBring != null) {
                    sendRemoveKitBring(kitBring.kitId(), kitBring.identity());
                    event.stopPropagation();
                }
            });
        }

        private boolean wasDraggedFromTriage(AtlasItemDrag drag) {
            if (drag == null) {
                return false;
            }
            String originIslandId = drag.originIslandId();
            if (SlotWorkspaceAtlasLayout.ISLAND_TRIAGE.equals(originIslandId)) {
                return true;
            }
            SlotWorkspaceViewModel.AtlasIsland origin = viewModel.island(originIslandId);
            return origin != null && origin.kind() == VisualAtlasIslandKind.TRIAGE;
        }

        private void installAtlasCanvasDropTarget(UIElement target, SlotAtlasGraphView atlas) {
            target.addEventListener(UIEvents.DRAG_PERFORM, event -> {
                IslandDrag islandDrag = islandDrag(event);
                if (islandDrag != null) {
                    if (event.target == atlas) {
                        return;
                    }
                    sendMoveIsland(
                            islandDrag.islandId(),
                            atlas.worldX(event.x) - islandDrag.grabOffsetX(),
                            atlas.worldY(event.y) - islandDrag.grabOffsetY()
                    );
                    event.stopPropagation();
                    return;
                }
                ChestTileDrag chestDrag = chestTileDrag(event);
                if (chestDrag != null) {
                    if (event.target == atlas) {
                        return;
                    }
                    sendMoveChest(
                            chestDrag.storageId(),
                            atlas.worldX(event.x) - chestDrag.grabOffsetX(),
                            atlas.worldY(event.y) - chestDrag.grabOffsetY()
                    );
                    event.stopPropagation();
                    return;
                }
                StorageZoneDrag zoneDrag = storageZoneDrag(event);
                if (zoneDrag != null) {
                    if (event.target == atlas) {
                        return;
                    }
                    int newLeft = atlas.worldX(event.x) - zoneDrag.grabOffsetX();
                    int newTop = atlas.worldY(event.y) - zoneDrag.grabOffsetY();
                    sendMoveStorageZone(newLeft - zoneDrag.originX(), newTop - zoneDrag.originY());
                    event.stopPropagation();
                }
            });
        }

        private void installIslandDropTarget(
                UIElement target,
                UIElement highlightTarget,
                SlotAtlasGraphView atlas,
                SlotWorkspaceViewModel.AtlasIsland island
        ) {
            target.addEventListener(UIEvents.DRAG_ENTER, event -> updateIslandDropOverlay(highlightTarget, island, event), true);
            target.addEventListener(UIEvents.DRAG_UPDATE, event -> updateIslandDropOverlay(highlightTarget, island, event));
            target.addEventListener(UIEvents.DRAG_LEAVE, event -> clearDropOverlay(highlightTarget), true);
            target.addEventListener(UIEvents.DRAG_PERFORM, event -> {
                clearDropOverlay(highlightTarget);
                IslandDrag islandDrag = islandDrag(event);
                if (islandDrag != null) {
                    sendMoveIsland(
                            islandDrag.islandId(),
                            atlas.worldX(event.x) - islandDrag.grabOffsetX(),
                            atlas.worldY(event.y) - islandDrag.grabOffsetY()
                    );
                    event.stopPropagation();
                    return;
                }
                AtlasItemDrag atlasItem = atlasItemDrag(event);
                if (atlasItem != null) {
                    sendAssignHome(
                            atlasItem.identity(),
                            island.islandId(),
                            atlas.worldX(event.x),
                            atlas.worldY(event.y)
                    );
                    event.stopPropagation();
                    return;
                }
                HotbarSlotDrag hotbarItem = hotbarSlotDrag(event);
                if (hotbarItem != null) {
                    if (hotbarDragHasHome(hotbarItem)) {
                        sendReturnHotbarToHome(hotbarItem.hotbarIndex());
                    } else {
                        sendMoveHotbarToAtlas(
                                hotbarItem.hotbarIndex(),
                                island.islandId(),
                                atlas.worldX(event.x),
                                atlas.worldY(event.y)
                        );
                    }
                    event.stopPropagation();
                    return;
                }
                ChestStackDrag chestDrag = chestStackDrag(event);
                if (chestDrag != null) {
                    // Pure metadata assign — the item stays in the chest, we
                    // only record the island as the visual home for this
                    // identity. Mark the drag as consumed so the chest cell's
                    // DRAG_END skips its default take-into-inventory path.
                    SlotWorkspaceViewModel.IdentityRef identity = SlotWorkspaceViewModel.IdentityRef.from(
                            dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(chestDrag.displayStack()));
                    sendAssignHome(
                            identity,
                            island.islandId(),
                            atlas.worldX(event.x),
                            atlas.worldY(event.y)
                    );
                    chestDragDropConsumed = true;
                    event.stopPropagation();
                }
            });
        }

        private void installViewportPanSurface(UIElement target, SlotAtlasGraphView atlas) {
            target.addEventListener(UIEvents.MOUSE_DOWN, event -> {
                if (event.target != target) {
                    return;
                }
                if (atlas.beginViewportPan(event)) {
                    event.stopPropagation();
                }
            });
        }

        private void updateHotbarDropOverlay(Button target, SlotWorkspaceViewModel.HotbarSlot slot, UIEvent event) {
            HotbarSlotDrag hotbarDrag = hotbarSlotDrag(event);
            if (hotbarDrag != null) {
                if (hotbarDrag.hotbarIndex() == slot.hotbarIndex()) {
                    clearDropOverlay(target);
                } else {
                    updateGenericDropOverlay(target, true, ACCENT);
                }
                return;
            }
            AtlasItemDrag drag = atlasItemDrag(event);
            if (drag == null) {
                clearDropOverlay(target);
                return;
            }
            SlotWorkspaceViewModel.AtlasItem item = viewModel.atlasItem(drag.identity());
            boolean carried = item != null && item.carried();
            updateGenericDropOverlay(target, carried, carried ? (slot.occupied() ? ACTIVE_HOTBAR : ACCENT) : WARNING);
        }

        private void updateIslandDropOverlay(UIElement highlightTarget, SlotWorkspaceViewModel.AtlasIsland island, UIEvent event) {
            boolean acceptable = atlasItemDrag(event) != null || hotbarSlotDrag(event) != null || islandDrag(event) != null;
            updateGenericDropOverlay(
                    highlightTarget,
                    acceptable,
                    islandDrag(event) != null
                            ? SELECTED
                            : island.kind() == VisualAtlasIslandKind.TRIAGE ? WARNING : ACCENT
            );
        }

        private void updateAtlasBackgroundDropOverlay(SlotAtlasGraphView atlas, UIEvent event) {
            if (!isDirectDragTarget(event, atlas)) {
                clearDropOverlay(atlas);
                return;
            }
            IslandDrag islandDrag = islandDrag(event);
            ChestTileDrag chestDrag = chestTileDrag(event);
            boolean acceptable = atlasItemDrag(event) != null
                    || hotbarSlotDrag(event) != null
                    || islandDrag != null
                    || chestDrag != null;
            int color = islandDrag != null || chestDrag != null ? SELECTED : WARNING;
            updateGenericDropOverlay(atlas, acceptable, color);
        }

        private void updateGenericDropOverlay(UIElement target, boolean active) {
            updateGenericDropOverlay(target, active, ACCENT);
        }

        private void updateGenericDropOverlay(UIElement target, boolean active, int color) {
            target.style(style -> style.overlayTexture(active ? rect((color & 0x00FFFFFF) | 0x44000000) : IGuiTexture.EMPTY));
        }

        private void clearDropOverlay(UIElement target) {
            target.style(style -> style.overlayTexture(IGuiTexture.EMPTY));
        }

        private void handleDragEnd(UIEvent event) {
            if (event.relatedTarget == null) {
                localStatus.set("drag cancelled");
            }
        }

        private boolean isDragging(UIElement element) {
            return element.getModularUI() != null && element.getModularUI().getDragHandler().isDragging();
        }

        private AtlasItemDrag atlasItemDrag(UIEvent event) {
            Object payload = event == null || event.dragHandler == null ? null : event.dragHandler.getDraggingObject();
            return payload instanceof AtlasItemDrag atlasItemDrag ? atlasItemDrag : null;
        }

        private HotbarSlotDrag hotbarSlotDrag(UIEvent event) {
            Object payload = event == null || event.dragHandler == null ? null : event.dragHandler.getDraggingObject();
            return payload instanceof HotbarSlotDrag hotbarSlotDrag ? hotbarSlotDrag : null;
        }

        private IslandDrag islandDrag(UIEvent event) {
            Object payload = event == null || event.dragHandler == null ? null : event.dragHandler.getDraggingObject();
            return payload instanceof IslandDrag islandDrag ? islandDrag : null;
        }

        private ChestTileDrag chestTileDrag(UIEvent event) {
            Object payload = event == null || event.dragHandler == null ? null : event.dragHandler.getDraggingObject();
            return payload instanceof ChestTileDrag chestTileDrag ? chestTileDrag : null;
        }

        private ChestStackDrag chestStackDrag(UIEvent event) {
            Object payload = event == null || event.dragHandler == null ? null : event.dragHandler.getDraggingObject();
            return payload instanceof ChestStackDrag chestStackDrag ? chestStackDrag : null;
        }

        private StorageZoneDrag storageZoneDrag(UIEvent event) {
            Object payload = event == null || event.dragHandler == null ? null : event.dragHandler.getDraggingObject();
            return payload instanceof StorageZoneDrag storageZoneDrag ? storageZoneDrag : null;
        }

        private boolean isDirectDragTarget(UIEvent event, UIElement element) {
            return event != null && event.target == element;
        }

        private IGuiTexture dragTexture(ItemStack stack) {
            return new ItemStackTexture(stack == null ? ItemStack.EMPTY : stack.copy());
        }

        private void rebuildAtlasBody(
                UIElement container,
                SlotAtlasGraphView atlas,
                SlotWorkspaceViewModel.AtlasItem item,
                AtlasRenderBudget budget,
                boolean searchMatch
        ) {
            float pinned = animationTargetScale(atlas);
            atlas.setPinnedContentScale(pinned);
            try {
                container.clearAllChildren();
                container.addChild(buildAtlasBody(atlas, item, budget, searchMatch));
            } finally {
                atlas.setPinnedContentScale(null);
            }
        }

        private UIElement buildAtlasBody(
                SlotAtlasGraphView atlas,
                SlotWorkspaceViewModel.AtlasItem item,
                AtlasRenderBudget budget,
                boolean searchMatch
        ) {
            return switch (budget.level()) {
                case REGION -> regionAtlasBody(atlas, item, budget, searchMatch);
                case BROWSE -> browseAtlasBody(atlas, item, budget, searchMatch);
                case READ -> readAtlasBody(atlas, item, budget, searchMatch);
                case INSPECT -> inspectAtlasBody(atlas, item, budget, searchMatch);
                case DETAIL -> detailAtlasBody(atlas, item, budget, searchMatch);
            };
        }

        private AtlasRenderBudget atlasBudget(SlotAtlasGraphView atlas, SlotWorkspaceViewModel.AtlasItem item) {
            float scale = animationTargetScale(atlas);
            int screenBudget = Math.max(1, Math.round(Math.min(item.width(), item.height()) * scale));
            return AtlasRenderBudget.forScreenBudget(screenBudget);
        }

        // Rebuild key for atlas card bodies. Only true LOD transitions trigger
        // a rebuild: disclosure level, READ's 1↔2 line flip at
        // cellBudgetPx>=40, and INSPECT's secondary-reveal at >=58. Fine-
        // grained scale tracking (keeping on-screen font px roughly constant
        // inside a level) is handled per-label inside anchorTextBand via a
        // TICK listener, which avoids thrashing the whole body subtree.
        private long atlasLayoutSignature(AtlasRenderBudget budget) {
            long signature = budget.level().ordinal() & 0x7L;
            signature = (signature << 1) | (budget.cellBudgetPx() >= 40 ? 1L : 0L);
            signature = (signature << 1) | (budget.cellBudgetPx() >= 58 ? 1L : 0L);
            return signature;
        }

        private static float clampScreenFontPx(float screenPx) {
            // Quantize to 0.5 screen-px steps. LDLib's TextElement runs a full
            // formattedLines recompute whenever fontSize changes, so a fully
            // continuous font size thrashes layout every zoom frame. Half-px
            // keeps MSDF's sub-pixel crispness at small sizes while cutting
            // recompute frequency in half vs. continuous.
            float clamped = Math.max(3f, screenPx);
            return Math.round(clamped * 2f) / 2f;
        }

        /**
         * Discrete zoom breakpoints for the island header font. 0.5-px
         * quantization (clampScreenFontPx) is effectively continuous — the
         * header visibly shrinks frame-by-frame as the atlas zooms and the
         * carried-count badge (fixed world size = 12) drifts outside the
         * header's world bounds. Breakpoints keep the header's on-screen
         * size at four discrete tiers so the badge either clearly fits or
         * clearly doesn't; combined with the world-height floor enforced
         * below, the badge never overflows.
         */
        private static float headerBreakpointFontPx(float screenPx) {
            if (screenPx < 8f) return 7f;
            if (screenPx < 10f) return 9f;
            if (screenPx < 12f) return 11f;
            return 12f;
        }

        private float worldFontSizeFor(SlotAtlasGraphView atlas, float screenPx) {
            // Use scaleForContent so that rebuilds done with a pinned scale
            // (setPinnedContentScale) bake labels at the same scale everything
            // else in the body uses via worldUnitsForPixels. getScale() is the
            // raw interpolated scale during animations and leaves labels
            // inconsistent with shell/icon sizing when the pin is active.
            float scale = atlas == null ? 1f : Math.max(0.0001f, atlas.scaleForContent());
            return clampScreenFontPx(screenPx) / scale;
        }

        private float animationTargetScale(SlotAtlasGraphView atlas) {
            if (cameraController.isAnimating()) {
                AtlasCamera target = cameraController.animTarget();
                if (target != null) {
                    return target.scale();
                }
            }
            if (atlasCamera != null) {
                return atlasCamera.scale();
            }
            return atlas.getScale();
        }

        private UIElement regionAtlasBody(
                SlotAtlasGraphView atlas,
                SlotWorkspaceViewModel.AtlasItem item,
                AtlasRenderBudget budget,
                boolean searchMatch
        ) {
            UIElement body = atlasBodyContainer();
            float cardBound = Math.min(item.width(), item.height());
            float shell = Math.min(cardBound, atlas.worldUnitsForPixels(budget.shellPx()));
            float shellLeft = centeredWorld(item.width(), shell);
            float shellTop = centeredWorld(item.height(), shell);
            addCommonAtlasSignals(body, atlas, item, budget, searchMatch);
            body.addChild(slotPreview(atlas, item, budget).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(shellLeft)
                    .top(shellTop)));
            addOverlaySignals(body, atlas, item, budget);
            return body;
        }

        private UIElement browseAtlasBody(
                SlotAtlasGraphView atlas,
                SlotWorkspaceViewModel.AtlasItem item,
                AtlasRenderBudget budget,
                boolean searchMatch
        ) {
            UIElement body = atlasBodyContainer();
            float cardBound = Math.min(item.width(), item.height());
            float shell = Math.min(cardBound, atlas.worldUnitsForPixels(budget.shellPx()));
            float shellLeft = centeredWorld(item.width(), shell);
            float shellTop = centeredWorld(item.height(), shell);
            addCommonAtlasSignals(body, atlas, item, budget, searchMatch);
            body.addChild(slotPreview(atlas, item, budget).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(shellLeft)
                    .top(shellTop)));
            addOverlaySignals(body, atlas, item, budget);
            return body;
        }

        private UIElement readAtlasBody(
                SlotAtlasGraphView atlas,
                SlotWorkspaceViewModel.AtlasItem item,
                AtlasRenderBudget budget,
                boolean searchMatch
        ) {
            UIElement body = atlasBodyContainer();
            float sidePad = atlas.worldUnitsForPixels(1f);
            float gap = atlas.worldUnitsForPixels(1f);
            float shellPx = budget.shellPx();
            float iconPx = budget.iconPx();
            float shell = atlas.worldUnitsForPixels(shellPx);
            float shellTop = atlas.worldUnitsForPixels(1f);
            int labelLines = budget.cellBudgetPx() >= 40 ? 2 : 1;
            float labelScreenHeight = budget.primaryLineHeightPx() * labelLines + (labelLines > 1 ? 1f : 0f);
            float labelHeight = atlas.worldUnitsForPixels(labelScreenHeight);
            addCommonAtlasSignals(body, atlas, item, budget, searchMatch);
            float shellLeft = (item.width() - shell) / 2f;
            body.addChild(slotPreview(atlas, item, shellPx, iconPx).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(shellLeft)
                    .top(shellTop)));
            body.addChild(anchorTextBand(
                    atlas,
                    preferredPrimaryLabel(item, budget),
                    searchMatch ? ACCENT : TEXT,
                    budget.primaryFontPx(),
                    budget.primaryMaxChars(),
                    labelLines,
                    0xB4111921,
                    Horizontal.CENTER
            ).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(sidePad)
                    .top(shellTop + shell + gap)
                    .width(item.width() - sidePad * 2f)
                    .height(labelHeight)));
            addOverlaySignals(body, atlas, item, budget);
            return body;
        }

        private UIElement inspectAtlasBody(
                SlotAtlasGraphView atlas,
                SlotWorkspaceViewModel.AtlasItem item,
                AtlasRenderBudget budget,
                boolean searchMatch
        ) {
            UIElement body = atlasBodyContainer();
            float sidePad = atlas.worldUnitsForPixels(1f);
            float gap = atlas.worldUnitsForPixels(1f);
            String secondary = preferredSecondaryLabel(item, budget);
            boolean showSecondary = !secondary.isBlank() && budget.cellBudgetPx() >= 58;
            float shellPx = showSecondary
                    ? Math.min(budget.cellBudgetPx() * 0.48f, budget.shellPx() + 4f)
                    : Math.min(budget.cellBudgetPx() * 0.60f, budget.shellPx() + 10f);
            float iconPx = Math.max(10f, shellPx - 4f);
            float shell = atlas.worldUnitsForPixels(shellPx);
            float topPad = atlas.worldUnitsForPixels(1f);
            float primaryHeight = atlas.worldUnitsForPixels(budget.primaryLineHeightPx() * 2f + 1f);
            float secondaryHeight = atlas.worldUnitsForPixels(budget.secondaryLineHeightPx());
            addCommonAtlasSignals(body, atlas, item, budget, searchMatch);
            float shellLeft = (item.width() - shell) / 2f;
            body.addChild(slotPreview(atlas, item, shellPx, iconPx).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(shellLeft)
                    .top(topPad)));
            float cursorTop = topPad + shell + gap;
            body.addChild(anchorTextBand(
                    atlas,
                    preferredPrimaryLabel(item, budget),
                    TEXT,
                    budget.primaryFontPx(),
                    budget.primaryMaxChars(),
                    2,
                    0xB4111921,
                    Horizontal.LEFT
            ).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(sidePad)
                    .top(cursorTop)
                    .width(item.width() - sidePad * 2f)
                    .height(primaryHeight)));
            if (showSecondary) {
                body.addChild(anchorTextBand(
                        atlas,
                        secondary,
                        searchMatch ? ACCENT : MUTED,
                        budget.secondaryFontPx(),
                        budget.secondaryMaxChars(),
                        1,
                        0x9A111921,
                        Horizontal.LEFT
                ).layout(layout -> layout
                        .positionType(TaffyPosition.ABSOLUTE)
                        .left(sidePad)
                        .top(cursorTop + primaryHeight + gap)
                        .width(item.width() - sidePad * 2f)
                        .height(secondaryHeight)));
            }
            addOverlaySignals(body, atlas, item, budget);
            return body;
        }

        private UIElement detailAtlasBody(
                SlotAtlasGraphView atlas,
                SlotWorkspaceViewModel.AtlasItem item,
                AtlasRenderBudget budget,
                boolean searchMatch
        ) {
            UIElement body = atlasBodyContainer();
            float topPad = atlas.worldUnitsForPixels(2f);
            float sidePad = atlas.worldUnitsForPixels(2f);
            float gap = atlas.worldUnitsForPixels(2f);
            String secondary = preferredSecondaryLabel(item, budget);
            String auxiliary = preferredAuxiliaryLabel(item, budget);
            boolean hasSecondary = !secondary.isBlank();
            boolean hasAuxiliary = !auxiliary.isBlank();
            float shellPx = !hasSecondary && !hasAuxiliary
                    ? Math.min(budget.cellBudgetPx() * 0.64f, budget.shellPx() + 14f)
                    : Math.min(budget.cellBudgetPx() * 0.54f, budget.shellPx() + 6f);
            float iconPx = Math.max(14f, shellPx - 6f);
            int primaryLines = hasSecondary || hasAuxiliary ? 2 : 3;
            float nameHeight = atlas.worldUnitsForPixels(budget.primaryLineHeightPx() * primaryLines + (primaryLines - 1));
            float shell = atlas.worldUnitsForPixels(shellPx);
            float shellTop = topPad;
            float auxLineHeight = atlas.worldUnitsForPixels(budget.secondaryLineHeightPx());
            addCommonAtlasSignals(body, atlas, item, budget, searchMatch);
            body.addChild(slotPreview(atlas, item, shellPx, iconPx).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(sidePad)
                    .top(shellTop)));
            float cursorTop = shellTop + shell + gap;
            float primaryTop = cursorTop;
            body.addChild(anchorTextBand(
                    atlas,
                    preferredPrimaryLabel(item, budget),
                    TEXT,
                    budget.primaryFontPx(),
                    budget.primaryMaxChars(),
                    primaryLines,
                    0xB8111921,
                    Horizontal.LEFT
            ).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(sidePad)
                    .top(primaryTop)
                    .width(item.width() - sidePad * 2f)
                    .height(nameHeight)));
            cursorTop += nameHeight;
            if (hasSecondary) {
                cursorTop += gap;
                float secondaryTop = cursorTop;
                body.addChild(anchorTextBand(
                        atlas,
                        secondary,
                        searchMatch ? ACCENT : MUTED,
                        budget.secondaryFontPx(),
                        budget.secondaryMaxChars(),
                        1,
                        0x90111921,
                        Horizontal.LEFT
                ).layout(layout -> layout
                        .positionType(TaffyPosition.ABSOLUTE)
                        .left(sidePad)
                        .top(secondaryTop)
                        .width(item.width() - sidePad * 2f)
                        .height(auxLineHeight)));
                cursorTop += auxLineHeight;
            }
            if (hasAuxiliary) {
                cursorTop += gap;
                float auxiliaryTop = cursorTop;
                body.addChild(anchorTextBand(
                        atlas,
                        auxiliary,
                        MUTED,
                        budget.secondaryFontPx() - 0.5f,
                        budget.secondaryMaxChars(),
                        1,
                        0x80111921,
                        Horizontal.LEFT
                ).layout(layout -> layout
                        .positionType(TaffyPosition.ABSOLUTE)
                        .left(sidePad)
                        .top(auxiliaryTop)
                        .width(item.width() - sidePad * 2f)
                        .height(auxLineHeight)));
                cursorTop += auxLineHeight;
            }
            if (item.isCarriedContainer()) {
                cursorTop += gap;
                float containerTop = cursorTop;
                body.addChild(anchorTextBand(
                        atlas,
                        formatFreeSlots(item.containerFreeSlotCount()),
                        item.containerFreeSlotCount() == 0 ? WARNING : ACCENT,
                        budget.secondaryFontPx(),
                        budget.secondaryMaxChars(),
                        1,
                        0x90111921,
                        Horizontal.LEFT
                ).layout(layout -> layout
                        .positionType(TaffyPosition.ABSOLUTE)
                        .left(sidePad)
                        .top(containerTop)
                        .width(item.width() - sidePad * 2f)
                        .height(auxLineHeight)));
                cursorTop += auxLineHeight;
            }
            if (!item.presence().isEmpty()) {
                cursorTop += gap;
                UIElement strip = presenceStrip(atlas, item, budget);
                if (strip != null) {
                    float presenceTop = cursorTop;
                    body.addChild(strip.layout(layout -> layout
                            .positionType(TaffyPosition.ABSOLUTE)
                            .left(sidePad)
                            .top(presenceTop)
                            .width(item.width() - sidePad * 2f)
                            .height(auxLineHeight)));
                }
            }
            addOverlaySignals(body, atlas, item, budget);
            return body;
        }

        private UIElement presenceStrip(
                SlotAtlasGraphView atlas,
                SlotWorkspaceViewModel.AtlasItem item,
                AtlasRenderBudget budget
        ) {
            if (item.presence().isEmpty()) {
                return null;
            }
            StringBuilder text = new StringBuilder("in: ");
            int maxEntries = Math.min(item.presence().size(), 3);
            for (int index = 0; index < maxEntries; index++) {
                SlotWorkspaceViewModel.ChestPresenceEntry entry = item.presence().get(index);
                if (index > 0) {
                    text.append(" · ");
                }
                text.append(entry.label()).append(" · ").append(entry.count());
            }
            if (item.presence().size() > maxEntries) {
                text.append(" · +").append(item.presence().size() - maxEntries);
            }
            int maxChars = Math.max(8, budget.secondaryMaxChars() + 12);
            UIElement band = anchorTextBand(
                    atlas,
                    text.toString(),
                    ACCENT,
                    budget.secondaryFontPx() - 0.5f,
                    maxChars,
                    1,
                    0x80121B1F,
                    Horizontal.LEFT
            );
            band.setAllowHitTest(true);
            String targetStorageId = item.presence().get(0).storageId();
            band.addEventListener(UIEvents.CLICK, event -> {
                SlotWorkspaceViewModel.ClaimedChestTile tile = viewModel.claimedChestTile(targetStorageId);
                if (tile != null) {
                    event.stopPropagation();
                    panToChestTile(atlas, tile);
                    localStatus.set("panned to " + tile.label());
                    rebuild();
                }
            });
            return band;
        }

        private UIElement atlasBodyContainer() {
            UIElement body = new UIElement().layout(layout -> layout.widthPercent(100).heightPercent(100));
            body.setAllowHitTest(false);
            return body;
        }

        private UIElement slotPreview(
                SlotAtlasGraphView atlas,
                SlotWorkspaceViewModel.AtlasItem item,
                AtlasRenderBudget budget
        ) {
            return slotPreview(atlas, item, budget.shellPx(), budget.iconPx());
        }

        private UIElement slotPreview(
                SlotAtlasGraphView atlas,
                SlotWorkspaceViewModel.AtlasItem item,
                float shellPx,
                float iconPx
        ) {
            float cardBound = Math.min(item.width(), item.height());
            float shell = Math.min(cardBound, atlas.worldUnitsForPixels(shellPx));
            float inset = Math.min(shell * 0.5f, atlas.worldUnitsForPixels(1f));
            float icon = Math.max(0f, Math.min(shell - inset * 2f, atlas.worldUnitsForPixels(iconPx)));
            boolean carried = item.carried();
            int shellColor = carried ? 0xB0141B23 : dimAlpha(0xB0141B23, GHOST_CARD_ALPHA);
            int innerColor = carried ? 0xD90A1218 : dimAlpha(0xD90A1218, GHOST_CARD_ALPHA);
            UIElement shellElement = panel(shellColor).layout(layout -> layout.width(shell).height(shell));
            shellElement.setAllowHitTest(false);
            shellElement.addChild(panel(innerColor).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(inset)
                    .top(inset)
                    .width(shell - inset * 2f)
                    .height(shell - inset * 2f)));
            shellElement.addChild(itemIcon(item.displayStack(), icon, carried).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(centeredWorld(shell, icon))
                    .top(centeredWorld(shell, icon))));
            return shellElement;
        }

        private UIElement slotPreview(SlotWorkspaceViewModel.AtlasItem item, int size, boolean showMarker) {
            float shell = size;
            float inset = 1f;
            float icon = Math.max(10f, size - 4f);
            boolean carried = item.carried();
            int shellColor = carried ? 0xB0141B23 : dimAlpha(0xB0141B23, GHOST_CARD_ALPHA);
            int innerColor = carried ? 0xD90A1218 : dimAlpha(0xD90A1218, GHOST_CARD_ALPHA);
            UIElement shellElement = panel(shellColor).layout(layout -> layout.width(shell).height(shell));
            shellElement.setAllowHitTest(false);
            shellElement.addChild(panel(innerColor).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(inset)
                    .top(inset)
                    .width(shell - inset * 2f)
                    .height(shell - inset * 2f)));
            shellElement.addChild(itemIcon(item.displayStack(), icon, carried).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(centeredWorld(shell, icon))
                    .top(centeredWorld(shell, icon))));
            if (showMarker) {
                shellElement.addChild(panel(itemMarkerColor(item)).layout(layout -> layout
                        .positionType(TaffyPosition.ABSOLUTE)
                        .right(1f)
                        .top(1f)
                        .width(3f)
                        .height(3f)));
            }
            return shellElement;
        }

private void addCommonAtlasSignals(
                UIElement body,
                SlotAtlasGraphView atlas,
                SlotWorkspaceViewModel.AtlasItem item,
                AtlasRenderBudget budget,
                boolean searchMatch
        ) {
            // Signals drawn *below* the item icon. LDLib2's drawContents paints
            // children in insertion order (zIndex only affects pose Z, not
            // paint order within a parent), so addOverlaySignals must be
            // called AFTER the slotPreview is added to keep overlays on top.
            if (searchMatch) {
                float sideInset = Math.min(item.width() * 0.04f, atlas.worldUnitsForPixels(2f));
                float bottomInset = Math.min(item.height() * 0.04f, atlas.worldUnitsForPixels(1f));
                float barHeight = Math.min(item.height() * 0.08f, atlas.worldUnitsForPixels(2f));
                float barWidth = Math.max(item.width() * 0.4f, item.width() - sideInset * 2f);
                body.addChild(panel(ACCENT).layout(layout -> layout
                        .positionType(TaffyPosition.ABSOLUTE)
                        .left(sideInset)
                        .bottom(bottomInset)
                        .width(barWidth)
                        .height(barHeight)));
            }
        }

        private void addOverlaySignals(
                UIElement body,
                SlotAtlasGraphView atlas,
                SlotWorkspaceViewModel.AtlasItem item,
                AtlasRenderBudget budget
        ) {
            // Must be invoked AFTER slotPreview is added to body so these paint
            // over the item icon. DrawerHelper.drawItemStack translates pose Z
            // by +232, but that only matters for depth testing — within a
            // single parent, sibling paint order is the list order, not the
            // zIndex order.
            if (item.isCarriedContainer()) {
                addContainerFullnessBar(body, atlas, item);
            }
            int proximateCount = proximateChestCount(item);
            if (proximateCount > 0) {
                float inset = Math.min(item.width() * 0.04f, atlas.worldUnitsForPixels(2f));
                float pipSizeRaw = Math.min(item.width() * 0.22f, atlas.worldUnitsForPixels(10f));
                float pipSize = Math.max(pipSizeRaw, item.width() * 0.08f);
                final float finalPipSize = pipSize;
                UIElement pip = panel(LINK_THREAD_COLOR).layout(layout -> layout
                        .positionType(TaffyPosition.ABSOLUTE)
                        .right(inset)
                        .top(inset)
                        .width(finalPipSize)
                        .height(finalPipSize));
                // zIndex pushes pose Z above the +232 item-icon depth so the
                // pip survives the icon's depth-write before the shader clears
                // the depth buffer.
                pip.style(style -> style.zIndex(260));
                pip.setAllowHitTest(false);
                if (budget.level() != DisclosureLevel.REGION) {
                    Label count = label(String.valueOf(Math.min(proximateCount, 999)), TEXT);
                    count.layout(layout -> layout.widthPercent(100).heightPercent(100));
                    count.setAllowHitTest(false);
                    float requestedPipFontPx = finalPipSize * 0.7f * atlas.getScale();
                    float pipFontWorld = clampScreenFontPx(requestedPipFontPx) / Math.max(0.0001f, atlas.getScale());
                    count.textStyle(style -> style
                            .textColor(TEXT)
                            .textShadow(false)
                            .fontSize(pipFontWorld)
                            .textAlignHorizontal(Horizontal.CENTER)
                            .textAlignVertical(Vertical.CENTER));
                    pip.addChild(count);
                }
                body.addChild(pip);
            }
        }

        private void addContainerFullnessBar(
                UIElement body,
                SlotAtlasGraphView atlas,
                SlotWorkspaceViewModel.AtlasItem item
        ) {
            float inset = Math.min(item.width() * 0.04f, atlas.worldUnitsForPixels(2f));
            float trackWidth = item.width() - inset * 2f;
            if (trackWidth <= 0f) {
                return;
            }
            float barHeight = Math.max(
                    atlas.worldUnitsForPixels(2f),
                    Math.min(item.height() * 0.06f, atlas.worldUnitsForPixels(4f)));
            int capacity = Math.max(0, item.containerSlotCapacity());
            int free = Math.max(0, item.containerFreeSlotCount());
            int filled = Math.max(0, capacity - free);
            // Dim track keeps the bar visible even on an empty container, so the
            // bar itself is the "this is a carried container" signal.
            UIElement track = panel(CARRIED_CONTAINER_PIP & 0x66FFFFFF).layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(inset)
                    .top(inset)
                    .width(trackWidth)
                    .height(barHeight));
            // zIndex > DrawerHelper.drawItemStack's +232 Z push so the bar stays
            // visible when it overlaps the item icon (which can happen at large
            // LODs where the icon spans most of the card).
            track.style(style -> style.zIndex(260));
            track.setAllowHitTest(false);
            body.addChild(track);
            if (capacity > 0 && filled > 0) {
                float ratio = Math.min(1f, (float) filled / capacity);
                float fillWidth = Math.max(0f, trackWidth * ratio);
                int fillColor = fullnessColor(filled, capacity);
                UIElement fill = panel(fillColor).layout(layout -> layout
                        .positionType(TaffyPosition.ABSOLUTE)
                        .left(inset)
                        .top(inset)
                        .width(fillWidth)
                        .height(barHeight));
                fill.style(style -> style.zIndex(261));
                fill.setAllowHitTest(false);
                body.addChild(fill);
            }
        }

        private int proximateChestCount(SlotWorkspaceViewModel.AtlasItem item) {
            if (item == null || item.presence().isEmpty()) {
                return 0;
            }
            int total = 0;
            for (SlotWorkspaceViewModel.ChestPresenceEntry entry : item.presence()) {
                SlotWorkspaceViewModel.ClaimedChestTile tile =
                        viewModel.claimedChestTile(entry.storageId());
                if (tile != null && tile.proximate()) {
                    total += entry.count();
                }
            }
            return total;
        }

        private record ChestSlotRef(String storageId, int chestSlotIndex) {
        }

        private ChestSlotRef firstProximateChestSlotFor(SlotWorkspaceViewModel.AtlasItem item) {
            if (item == null) {
                return null;
            }
            SlotWorkspaceViewModel.IdentityRef identity = item.identity();
            for (SlotWorkspaceViewModel.ClaimedChestTile tile : viewModel.claimedChestTiles()) {
                if (!tile.proximate()) {
                    continue;
                }
                java.util.List<ItemStack> contents = tile.contents();
                java.util.List<Integer> indices = tile.contentSlotIndices();
                for (int i = 0; i < contents.size(); i++) {
                    ItemStack stack = contents.get(i);
                    if (stack == null || stack.isEmpty()) {
                        continue;
                    }
                    SlotWorkspaceViewModel.IdentityRef cellIdentity = SlotWorkspaceViewModel.IdentityRef.from(
                            dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(stack));
                    if (identity.equals(cellIdentity)) {
                        int slotIdx = i < indices.size() ? indices.get(i) : i;
                        return new ChestSlotRef(tile.storageId(), slotIdx);
                    }
                }
            }
            return null;
        }

        private UIElement anchorTextBand(
                SlotAtlasGraphView atlas,
                String text,
                int color,
                float screenFontPx,
                int maxLength,
                int lines,
                int backgroundColor,
                Horizontal align
        ) {
            // Label rendering sits inside GraphView's scaled content transform,
            // so a world-unit fontSize renders at (fontSize * atlas.getScale())
            // screen pixels. Baking fontSize once at build-time makes the label
            // drift as zoom changes (shrinks on zoom-out, grows on zoom-in).
            // Instead, we install a TICK listener that recomputes
            // world-fontSize = screenFontPx / currentScale each frame, keeping
            // rendered screen pixels ~constant. This mirrors the island-header
            // pattern.
            // Use the actual render scale (getScale) instead of scaleForContent
            // for sizing the label. During a camera animation rebuildAtlasBody
            // pins scaleForContent to the animation *target* — so if we baked
            // the world fontSize from that, the first rendered frame would
            // draw fontSize×currentScale, and when target diverges strongly
            // from current (e.g. zoom-out peek) the label flashes oversized
            // for a frame before the TICK below corrects it. getScale() is
            // always the real scale the pose stack will apply, so the initial
            // value already matches what you see on screen.
            float initialScale = Math.max(0.0001f, atlas.getScale());
            float snappedScreenFont = clampScreenFontPx(screenFontPx);
            float initialWorldFont = snappedScreenFont / initialScale;
            float initialWorldPad = 1f / initialScale;
            float initialLineSpacing = lines > 1 ? initialWorldPad : 0f;

            UIElement band = panel(backgroundColor).layout(layout -> layout
                    .paddingHorizontal(initialWorldPad)
                    .paddingVertical(initialWorldPad)
                    .alignItems(AlignItems.CENTER)
                    .flexDirection(FlexDirection.ROW));
            band.setAllowHitTest(false);
            if (lines <= 1) {
                band.setOverflowVisible(false);
            }
            String displayText = compactAnchorText(text, maxLength);
            Label token = anchorLabel(displayText, color, initialWorldFont);
            token.layout(layout -> layout.widthPercent(100).heightPercent(100));
            token.textStyle(style -> style
                    .fontSize(initialWorldFont)
                    .lineSpacing(initialLineSpacing)
                    .textWrap(lines > 1 ? TextWrap.WRAP : TextWrap.NONE)
                    .textAlignVertical(lines > 1 ? Vertical.TOP : Vertical.CENTER)
                    .textAlignHorizontal(align));
            band.addChild(token);

            // Quantize the *world* fontSize to quarter-unit steps so tiny zoom
            // deltas don't trigger a TextElement.recompute every frame. That
            // keeps rendered pixels within ~0.25 screen-px of the target while
            // capping fontSize writes to a handful per zoom range.
            int[] lastFontQuarter = {Math.round(initialWorldFont * 4f)};
            float[] lastScale = {initialScale};
            band.addEventListener(UIEvents.TICK, event -> {
                // Track the actual render scale (not animation target) so the
                // label stays sized correctly every frame during animations
                // instead of flashing to the target-scale bake until the
                // animation settles.
                float scale = Math.max(0.0001f, atlas.getScale());
                float worldFont = snappedScreenFont / scale;
                int fontQuarter = Math.max(1, Math.round(worldFont * 4f));
                boolean scaleChanged = scale != lastScale[0];
                if (fontQuarter != lastFontQuarter[0]) {
                    lastFontQuarter[0] = fontQuarter;
                    float quantizedWorldFont = fontQuarter / 4f;
                    float lineSpacing = lines > 1 ? 1f / scale : 0f;
                    token.textStyle(style -> style
                            .fontSize(quantizedWorldFont)
                            .lineSpacing(lineSpacing));
                }
                if (scaleChanged) {
                    lastScale[0] = scale;
                    float pad = 1f / scale;
                    band.layout(layout -> layout
                            .paddingHorizontal(pad)
                            .paddingVertical(pad)
                            .alignItems(AlignItems.CENTER)
                            .flexDirection(FlexDirection.ROW));
                    band.markTaffyStyleDirty();
                }
            });

            return band;
        }

        private String preferredPrimaryLabel(SlotWorkspaceViewModel.AtlasItem item, AtlasRenderBudget budget) {
            return compactAnchorText(item == null ? "" : item.name(), budget.primaryMaxChars());
        }

        private String preferredSecondaryLabel(SlotWorkspaceViewModel.AtlasItem item, AtlasRenderBudget budget) {
            String variant = tooltipVariantToken(item, budget.secondaryMaxChars());
            String mod = modToken(item, budget.secondaryMaxChars());
            String primary = preferredPrimaryLabel(item, budget);
            if (!variant.isBlank() && !normalizeTooltipText(variant).equals(normalizeTooltipText(primary))) {
                return variant;
            }
            if (item.recent()) {
                return "new";
            }
            if (!mod.isBlank()) {
                return mod;
            }
            return "";
        }

        private String preferredAuxiliaryLabel(SlotWorkspaceViewModel.AtlasItem item, AtlasRenderBudget budget) {
            String secondary = preferredSecondaryLabel(item, budget);
            String mod = modToken(item, budget.secondaryMaxChars());
            if (!mod.isBlank() && !normalizeTooltipText(mod).equals(normalizeTooltipText(secondary))) {
                return mod;
            }
            return "";
        }

        private String modToken(SlotWorkspaceViewModel.AtlasItem item, int maxLength) {
            if (item == null) {
                return "";
            }
            String namespace = namespace(item.identity().itemId());
            if (namespace.isBlank() || "minecraft".equals(namespace)) {
                return "";
            }
            return compactItemLabel(namespace.replace('_', ' ').replace('-', ' '), maxLength);
        }

        private String tooltipVariantToken(SlotWorkspaceViewModel.AtlasItem item, int maxLength) {
            List<Component> tooltipLines = atlasTooltipLines(item);
            String name = item == null ? "" : item.name();
            String namespace = namespace(item == null ? "" : item.identity().itemId());
            for (Component line : tooltipLines) {
                if (line == null) {
                    continue;
                }
                String normalized = normalizeTooltipText(line.getString());
                if (normalized.isBlank()) {
                    continue;
                }
                if (normalizeTooltipText(name).equals(normalized)) {
                    continue;
                }
                if (isGenericTooltipToken(normalized, namespace)) {
                    continue;
                }
                return compactAnchorText(normalized, maxLength);
            }
            return "";
        }

        private String namespace(String itemId) {
            if (itemId == null) {
                return "";
            }
            int separator = itemId.indexOf(':');
            return separator < 0 ? "" : itemId.substring(0, separator);
        }

        private String normalizeTooltipText(String text) {
            if (text == null) {
                return "";
            }
            return text
                    .replace('\n', ' ')
                    .replaceAll("\\s+", " ")
                    .replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit} ]", "")
                    .trim()
                    .toLowerCase(Locale.ROOT);
        }

        private boolean isGenericTooltipToken(String normalizedText, String namespace) {
            if (normalizedText.isBlank()) {
                return true;
            }
            String normalizedNamespace = normalizeTooltipText(namespace == null ? "" : namespace.replace('_', ' '));
            if (!normalizedNamespace.isBlank() && normalizedText.equals(normalizedNamespace)) {
                return true;
            }
            return normalizedText.startsWith("hold ")
                    || normalizedText.startsWith("press ")
                    || normalizedText.startsWith("when ")
                    || normalizedText.contains(" shift")
                    || normalizedText.contains(" ctrl")
                    || normalizedText.contains("details")
                    || normalizedText.startsWith("durability")
                    || normalizedText.startsWith("emc ")
                    || normalizedText.startsWith("energy ")
                    || normalizedText.startsWith("burn time");
        }

        private String compactAnchorText(String text, int maxLength) {
            if (text == null) {
                return "";
            }
            String normalized = text.replace('\n', ' ').replaceAll("\\s+", " ").trim();
            if (normalized.isBlank()) {
                return "";
            }
            if (maxLength <= 0 || normalized.length() <= maxLength) {
                return normalized;
            }
            return normalized.substring(0, maxLength);
        }

        private String compactItemLabel(String text, int maxLength) {
            if (text == null) {
                return "";
            }
            String normalized = text.replace('\n', ' ').replaceAll("\\s+", " ").trim();
            if (normalized.isBlank()) {
                return "";
            }
            String[] words = normalized.split(" ");
            if (words.length >= 2) {
                StringBuilder builder = new StringBuilder();
                for (int index = 0; index < Math.min(2, words.length); index++) {
                    String word = words[index];
                    int remaining = maxLength - builder.length() - (builder.isEmpty() ? 0 : 1);
                    if (remaining <= 0) {
                        break;
                    }
                    String piece = truncateWord(word, remaining);
                    if (piece.isBlank()) {
                        continue;
                    }
                    if (!builder.isEmpty()) {
                        builder.append(' ');
                    }
                    builder.append(piece);
                }
                if (!builder.isEmpty()) {
                    return builder.toString();
                }
            }
            return shorten(normalized, maxLength);
        }

        private String truncateWord(String word, int maxLength) {
            if (word == null || word.isBlank() || maxLength <= 0) {
                return "";
            }
            return word.length() <= maxLength ? word : word.substring(0, maxLength);
        }

        private int itemMarkerColor(SlotWorkspaceViewModel.AtlasItem item) {
            if (item.recent()) {
                return WARNING;
            }
            if (item.playerPlaced()) {
                return ACCENT;
            }
            SlotWorkspaceViewModel.AtlasIsland island = viewModel.island(item.islandId());
            return island != null && island.kind() != VisualAtlasIslandKind.TRIAGE
                    ? 0xFF94D8B8
                    : MUTED;
        }

        private String compactCount(int count) {
            if (count <= 0) {
                return "0";
            }
            if (count > 99) {
                return "99+";
            }
            return Integer.toString(count);
        }

        private SlotWorkspaceViewModel.AtlasItem hoveredAtlasItem() {
            return viewModel.atlasItem(hoveredAtlasIdentity);
        }

        private SlotWorkspaceViewModel.AtlasItem selectedAtlasItem() {
            return viewModel.atlasItem(selectedAtlasIdentity.get());
        }

        private SlotWorkspaceViewModel.AtlasItem focusedAtlasItem() {
            SlotWorkspaceViewModel.AtlasItem selected = selectedAtlasItem();
            return selected != null ? selected : hoveredAtlasItem();
        }

        private SlotWorkspaceViewModel.IdentityRef currentMapFocusIdentity() {
            if (hoveredAtlasIdentity != null && viewModel.atlasItem(hoveredAtlasIdentity) != null) {
                return hoveredAtlasIdentity;
            }
            return selectedAtlasIdentity.get();
        }

        private boolean isMapFocusItem(SlotWorkspaceViewModel.AtlasItem item) {
            SlotWorkspaceViewModel.IdentityRef focusIdentity = currentMapFocusIdentity();
            return item != null && focusIdentity != null && item.identity().equals(focusIdentity);
        }

        private SlotWorkspaceViewModel.HotbarSlot selectedHotbarSlot() {
            int idx = selectedHotbarIndex.get();
            if (idx < 0 || idx >= viewModel.hotbarSlots().size()) {
                return null;
            }
            SlotWorkspaceViewModel.HotbarSlot slot = viewModel.hotbarSlots().get(idx);
            return slot.occupied() ? slot : null;
        }

        private String selectionLabel() {
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

        private boolean matchesSearch(SlotWorkspaceViewModel.AtlasItem item) {
            String query = normalizedSearchQuery();
            if (query.isBlank()) {
                return true;
            }
            StringBuilder searchable = new StringBuilder();
            searchable.append(item.name().toLowerCase(Locale.ROOT)).append(' ')
                    .append(item.identity().itemId().toLowerCase(Locale.ROOT)).append(' ');
            SlotWorkspaceViewModel.AtlasIsland island = viewModel.island(item.islandId());
            if (island != null) {
                searchable.append(island.label().toLowerCase(Locale.ROOT)).append(' ');
                searchable.append(island.kind().name().toLowerCase(Locale.ROOT)).append(' ');
            }
            return searchable.toString().contains(query);
        }

        private String normalizedSearchQuery() {
            return searchQuery == null ? "" : searchQuery.trim().toLowerCase(Locale.ROOT);
        }

        private String islandSubtitle(SlotWorkspaceViewModel.AtlasIsland island) {
            String count = island.itemCount() + " item" + (island.itemCount() == 1 ? "" : "s");
            String carriedBadge = island.carriedCount() > 0 ? "  ·  " + island.carriedCount() + " carried" : "";
            return switch (island.kind()) {
                case TRIAGE -> count + " awaiting placement" + carriedBadge;
                case PLAYER -> count + " player-authored homes" + carriedBadge;
            };
        }

        private String itemMarker(SlotWorkspaceViewModel.AtlasItem item) {
            if (item.recent()) {
                return "new";
            }
            SlotWorkspaceViewModel.AtlasIsland island = viewModel.island(item.islandId());
            if (item.playerPlaced()) {
                return "set";
            }
            if (island != null && island.kind() != VisualAtlasIslandKind.TRIAGE) {
                return "auto";
            }
            return "inbox";
        }

        private String selectionHomeStatus(SlotWorkspaceViewModel.AtlasItem item) {
            SlotWorkspaceViewModel.AtlasIsland island = viewModel.island(item.islandId());
            if (item.playerPlaced()) {
                return "player-placed home";
            }
            if (island != null && island.kind() != VisualAtlasIslandKind.TRIAGE) {
                return "starter home";
            }
            return "awaiting placement";
        }

        private int cardChromeColor(DisclosureLevel level, boolean selected, boolean searchMatch, boolean recent) {
            return cardChromeColor(level, selected, searchMatch, recent, true);
        }

        private int cardChromeColor(
                DisclosureLevel level,
                boolean selected,
                boolean searchMatch,
                boolean recent,
                boolean carried
        ) {
            int base = cardChromeBaseColor(level, selected, searchMatch, recent);
            if (!carried && !selected) {
                base = dimAlpha(base, GHOST_CARD_ALPHA);
            }
            return base;
        }

        private int cardChromeBaseColor(DisclosureLevel level, boolean selected, boolean searchMatch, boolean recent) {
            if (level == DisclosureLevel.REGION) {
                if (selected) {
                    return 0x68435F55;
                }
                if (normalizedSearchQuery().isBlank()) {
                    return recent ? 0x242B433A : 0x1410161B;
                }
                return searchMatch ? (recent ? 0x342B433A : 0x241A242C) : 0x05000000;
            }
            if (selected) {
                return SELECTED;
            }
            if (normalizedSearchQuery().isBlank()) {
                return recent ? ROW_MATCH : 0xC926313B;
            }
            return searchMatch ? (recent ? ROW_MATCH : ROW_HOVER) : 0x2824313D;
        }

        private static int dimAlpha(int color, float alphaFactor) {
            int alpha = (color >>> 24) & 0xFF;
            int dimmed = Math.round(alpha * alphaFactor);
            return (dimmed << 24) | (color & 0x00FFFFFF);
        }

        private String itemName(ItemStack stack) {
            if (stack == null || stack.isEmpty()) {
                return "empty";
            }
            String name = stack.getHoverName().getString();
            return name.isBlank() ? "unknown" : name;
        }

        private List<Component> atlasTooltipLines(SlotWorkspaceViewModel.AtlasItem item) {
            if (item == null || item.displayStack().isEmpty()) {
                return List.of();
            }
            return List.copyOf(DrawerHelper.getItemToolTip(item.displayStack()));
        }

        private void appendTooltipPreview(List<UIElement> children, SlotWorkspaceViewModel.AtlasItem item) {
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

        private UIElement itemIcon(ItemStack stack, float size) {
            return itemIcon(stack, size, true);
        }

        private UIElement itemIcon(ItemStack stack, float size, boolean carried) {
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

        private UIElement emptyIcon() {
            UIElement icon = panel(0x80323B44).layout(layout -> layout.width(16).height(16));
            icon.setAllowHitTest(false);
            return icon;
        }

        private Label anchorLabel(String text, int color, float fontSize) {
            Label label = label(text, color);
            label.textStyle(style -> style
                    .fontSize(fontSize)
                    .textAlignVertical(Vertical.CENTER)
                    .textAlignHorizontal(Horizontal.LEFT));
            return label;
        }

        private float centeredWorld(float container, float child) {
            return Math.max(0f, (container - child) / 2f);
        }

        private void clearSelectionOnDirectClick(UIElement element) {
            element.addEventListener(UIEvents.MOUSE_DOWN, event -> {
                if (event.target == element && (selectedAtlasIdentity.get() != null || selectedHotbarIndex.get() >= 0)) {
                    selectedAtlasIdentity.set(null);
                    selectedHotbarIndex.set(-1);
                    localStatus.set("selection cleared");
                }
            });
        }
    }

    private record AtlasItemDrag(
            SlotWorkspaceViewModel.IdentityRef identity,
            ItemStack displayStack,
            String originIslandId
    ) {
    }

    private record HotbarSlotDrag(
            int hotbarIndex,
            ItemStack displayStack
    ) {
    }

    private record IslandDrag(
            String islandId,
            int grabOffsetX,
            int grabOffsetY
    ) {
    }

    private record ChestTileDrag(
            String storageId,
            int grabOffsetX,
            int grabOffsetY
    ) {
    }

    private record ChestStackDrag(
            String storageId,
            int chestSlotIndex,
            ItemStack displayStack
    ) {
    }

    private record StorageZoneBounds(int left, int top, int width, int height) {
    }

    private record StorageZoneDrag(
            int grabOffsetX,
            int grabOffsetY,
            int originX,
            int originY
    ) {
    }

    private record KitSlotDrag(
            String kitId,
            int pageIndex,
            int slotIndex,
            SlotWorkspaceViewModel.IdentityRef identity,
            ItemStack displayStack
    ) {
    }

    private record KitBringDrag(
            String kitId,
            SlotWorkspaceViewModel.IdentityRef identity,
            ItemStack displayStack
    ) {
    }

    private static String shorten(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    private static UIElement panel(int color) {
        return new UIElement().style(style -> style.backgroundTexture(rect(color)));
    }

    private static Button button(String text, boolean active) {
        return button(text, active, active ? ROW : PANEL_ALT);
    }

    private static Button button(String text, boolean active, int color) {
        Button button = new Button();
        button.setText(Component.literal(text));
        button.setActive(active);
        applyButtonColors(button, active, color);
        return button;
    }

    private static void applyButtonColors(Button button, boolean active, int color) {
        button.buttonStyle(style -> {
            style.baseTexture(rect(color));
            style.hoverTexture(rect(active ? hoverColor(color) : color));
            style.pressedTexture(rect(active ? SELECTED : color));
        });
        button.textStyle(style -> style.font(FONT_UI).textColor(active ? TEXT : MUTED).textShadow(false).fontSize(8));
    }

    private static int hoverColor(int color) {
        if (color == ROW_DIM) {
            return ROW;
        }
        int baseAlpha = (color >>> 24) & 0xFF;
        if (baseAlpha < 0x80) {
            // Dim / ghost cards: keep hover within the same alpha envelope so they
            // don't suddenly look as prominent as a full-opacity carried card.
            return (baseAlpha << 24) | (ROW_HOVER & 0x00FFFFFF);
        }
        return ROW_HOVER;
    }

    private static Label label(String text, int color) {
        Label label = new Label();
        label.setText(Component.literal(text == null ? "" : text));
        label.textStyle(style -> style
                .font(FONT_UI)
                .textColor(color)
                .fontSize(8)
                .textShadow(false)
                .textAlignVertical(Vertical.CENTER)
                .textAlignHorizontal(Horizontal.LEFT));
        label.setAllowHitTest(false);
        return label;
    }

    private static Label wrappedLabel(String text, int color) {
        Label label = label(text, color);
        label.layout(layout -> layout.widthPercent(100));
        label.textStyle(style -> style.textWrap(TextWrap.WRAP).textAlignVertical(Vertical.TOP));
        return label;
    }

    private static ColorRectTexture rect(int color) {
        return new ColorRectTexture(color);
    }

    private static final class AtlasCameraController {
        @FunctionalInterface
        interface Easing {
            float apply(float t);
        }

        static final Easing LINEAR = t -> t;
        static final Easing CUBIC_IN_OUT = t -> t < 0.5f
                ? 4f * t * t * t
                : 1f - (float) Math.pow(-2f * t + 2f, 3) / 2f;

        static final long PEEK_DURATION_MS = 800L;
        static final long COMMIT_DURATION_MS = 800L;
        static final long SEARCH_PREVIEW_DURATION_MS = 320L;
        // A typical human key-tap is ~100–200 ms, so 100 ms was too tight:
        // most "tap to goto" presses came back longer than the threshold and
        // were treated as a hold-then-snapback instead.
        static final long PEEK_TAP_THRESHOLD_MS = 250L;
        static final long PEEK_SNAPBACK_DURATION_MS = 450L;

        enum CommitSource {
            HOVER_GOTO,
            SEARCH_COMMIT,
            SEARCH_ENTER,
            PAN_TO_ISLAND,
            PAN_TO_CHEST,
            CHIP_ACCEPT,
            ISLAND_CREATE_FOCUS,
            REHOME_PICK
        }

        private final CameraHistory<AtlasCamera> history = new CameraHistory<>();
        private SlotAtlasGraphView graphView;
        private AtlasCamera animStart;
        private AtlasCamera animTarget;
        private long animStartMs;
        private long animDurationMs;
        private Easing animEasing = LINEAR;
        private boolean animating;
        private AtlasCamera origin;

        void attach(SlotAtlasGraphView view) {
            this.graphView = view;
        }

        SlotAtlasGraphView graphView() {
            return graphView;
        }

        boolean hasGraphView() {
            return graphView != null;
        }

        boolean isDragging() {
            return graphView != null
                    && graphView.getModularUI() != null
                    && graphView.getModularUI().getDragHandler() != null
                    && graphView.getModularUI().getDragHandler().isDragging();
        }

        AtlasCamera currentCamera() {
            if (graphView == null) {
                return null;
            }
            return new AtlasCamera(graphView.getOffsetX(), graphView.getOffsetY(), graphView.getScale());
        }

        void ease(AtlasCamera target, Easing easing, long durationMs) {
            if (target == null || graphView == null) {
                return;
            }
            this.animStart = currentCamera();
            this.animTarget = target;
            this.animStartMs = System.currentTimeMillis();
            this.animDurationMs = Math.max(1L, durationMs);
            this.animEasing = easing != null ? easing : LINEAR;
            this.animating = true;
        }

        void snap(AtlasCamera target) {
            if (target == null || graphView == null) {
                return;
            }
            animating = false;
            animStart = null;
            animTarget = null;
            graphView.restoreCamera(target);
        }

        void commit(AtlasCamera target, CommitSource source, Easing easing, long durationMs) {
            if (target == null || graphView == null) {
                return;
            }
            history.recordCommit(currentCamera());
            ease(target, easing, durationMs);
        }

        void commitFrom(AtlasCamera origin, AtlasCamera target, CommitSource source, Easing easing, long durationMs) {
            if (target == null || graphView == null) {
                return;
            }
            if (origin != null) {
                history.recordCommit(origin);
            }
            ease(target, easing, durationMs);
        }

        boolean back() {
            if (graphView == null) {
                return false;
            }
            Optional<AtlasCamera> popped = history.back(currentCamera());
            if (popped.isEmpty()) {
                return false;
            }
            ease(popped.get(), CUBIC_IN_OUT, COMMIT_DURATION_MS);
            return true;
        }

        boolean forward() {
            if (graphView == null) {
                return false;
            }
            Optional<AtlasCamera> popped = history.forward(currentCamera());
            if (popped.isEmpty()) {
                return false;
            }
            ease(popped.get(), CUBIC_IN_OUT, COMMIT_DURATION_MS);
            return true;
        }

        void recordOrigin() {
            origin = currentCamera();
        }

        void clearOrigin() {
            origin = null;
        }

        AtlasCamera origin() {
            return origin;
        }

        CameraHistory<AtlasCamera> history() {
            return history;
        }

        boolean isAnimating() {
            return animating;
        }

        AtlasCamera animTarget() {
            return animTarget;
        }

        void tick() {
            if (!animating || animTarget == null || graphView == null) {
                return;
            }
            long now = System.currentTimeMillis();
            float t = Math.max(0f, Math.min(1f, (now - animStartMs) / (float) animDurationMs));
            float eased = animEasing.apply(t);
            AtlasCamera start = animStart != null ? animStart : animTarget;
            float viewW = graphView.getContentWidth();
            float viewH = graphView.getContentHeight();
            if (viewW <= 0f || viewH <= 0f) {
                return;
            }
            float startCenterX = start.offsetX() + viewW / (2f * start.scale());
            float startCenterY = start.offsetY() + viewH / (2f * start.scale());
            float endCenterX = animTarget.offsetX() + viewW / (2f * animTarget.scale());
            float endCenterY = animTarget.offsetY() + viewH / (2f * animTarget.scale());
            float centerX = startCenterX + (endCenterX - startCenterX) * eased;
            float centerY = startCenterY + (endCenterY - startCenterY) * eased;
            float startLog = (float) Math.log(Math.max(0.0001f, start.scale()));
            float endLog = (float) Math.log(Math.max(0.0001f, animTarget.scale()));
            float scale = (float) Math.exp(startLog + (endLog - startLog) * eased);
            float offsetX = centerX - viewW / (2f * scale);
            float offsetY = centerY - viewH / (2f * scale);
            graphView.restoreCamera(new AtlasCamera(offsetX, offsetY, scale));
            if (t >= 1f) {
                graphView.restoreCamera(animTarget);
                animating = false;
                animStart = null;
                animTarget = null;
            }
        }
    }

    private static final class SlotAtlasGraphView extends GraphView {
        private Consumer<AtlasCamera> cameraListener = camera -> {
        };
        private Runnable perFrameTick = () -> {
        };
        private Float pinnedContentScale = null;

        void setPinnedContentScale(Float scale) {
            this.pinnedContentScale = scale;
        }

        private float scaleForContent() {
            return pinnedContentScale != null ? pinnedContentScale : getScale();
        }

        private void onCameraChanged(Consumer<AtlasCamera> listener) {
            cameraListener = listener == null ? camera -> {
            } : listener;
        }

        private void setPerFrameTick(Runnable hook) {
            perFrameTick = hook == null ? () -> {
            } : hook;
        }

        @Override
        public void drawBackgroundTexture(com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext guiContext) {
            perFrameTick.run();
            super.drawBackgroundTexture(guiContext);
        }

        private void captureCamera() {
            cameraListener.accept(new AtlasCamera(getOffsetX(), getOffsetY(), getScale()));
        }

        private void restoreCamera(AtlasCamera camera) {
            if (camera == null || getContentWidth() <= 0 || getContentHeight() <= 0) {
                return;
            }
            float viewWidth = getContentWidth() / camera.scale();
            float viewHeight = getContentHeight() / camera.scale();
            fit(
                    camera.offsetX(),
                    camera.offsetY(),
                    camera.offsetX() + viewWidth,
                    camera.offsetY() + viewHeight,
                    camera.scale()
            );
            captureCamera();
        }

        private void resetToOverview() {
            fitToChildren(72f, 0.45f);
            captureCamera();
        }

        private boolean beginViewportPan(UIEvent event) {
            if (event == null || !getGraphViewStyle().allowPan()) {
                return false;
            }
            if (event.button != 0 && event.button != 2) {
                return false;
            }
            if (!isSelfOrChildHover() || !isMouseOverContent(event.x, event.y)) {
                return false;
            }
            startDrag(new DragOffset(getOffsetX(), getOffsetY()), null);
            return true;
        }

        private int worldX(float screenX) {
            return Math.round(getOffsetX() + (screenX - getContentX()) / Math.max(0.0001f, getScale()));
        }

        private int worldY(float screenY) {
            return Math.round(getOffsetY() + (screenY - getContentY()) / Math.max(0.0001f, getScale()));
        }

        private float screenX(float worldX) {
            return (worldX - getOffsetX()) * getScale() + getContentX();
        }

        private float screenY(float worldY) {
            return (worldY - getOffsetY()) * getScale() + getContentY();
        }

        private float worldUnitsForPixels(float pixels) {
            return pixels / Math.max(0.0001f, scaleForContent());
        }

        private int screenPixelsForWorldUnits(float worldUnits) {
            return Math.round(worldUnits * scaleForContent());
        }

        @Override
        protected void onMouseWheel(com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent event) {
            // If a child listener already consumed the wheel (e.g. shift-scroll
            // transfer on an atlas card) we must not also zoom. The default
            // GraphView early-returns unless event.target == this; the hack
            // below rewrites target so super sees itself. Skip the rewrite
            // when propagation was stopped so the zoom doesn't fire alongside
            // the child handler's own action.
            if (event.propagationStopped) {
                return;
            }
            UIElement target = event.target;
            if (target != this && isSelfOrChildHover()) {
                event.target = this;
            }
            super.onMouseWheel(event);
            event.target = target;
            captureCamera();
        }

        @Override
        protected void onDragSourceUpdate(com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent event) {
            super.onDragSourceUpdate(event);
            captureCamera();
        }
    }

    private record AtlasCamera(
            float offsetX,
            float offsetY,
            float scale
    ) {
    }

    private record IslandRenderBudget(
            float titleFontPx,
            float subtitleFontPx,
            float headerHeightPx,
            float subtitleHeightPx,
            float ruleHeightPx,
            boolean showSubtitle
    ) {
        private static IslandRenderBudget forScreenBudget(int islandScreenWidthPx) {
            int clamped = Math.max(1, islandScreenWidthPx);
            float titleFont = Math.max(8.5f, Math.min(12.5f, clamped * 0.026f));
            float subtitleFont = Math.max(6.5f, Math.min(8.5f, clamped * 0.018f));
            boolean showSubtitle = clamped >= 220;
            return new IslandRenderBudget(
                    titleFont,
                    subtitleFont,
                    titleFont + 5.5f,
                    subtitleFont + 3.5f,
                    showSubtitle ? 1.5f : 1f,
                    showSubtitle
            );
        }
    }

    private record AtlasRenderBudget(
            DisclosureLevel level,
            int cellBudgetPx,
            float shellPx,
            float iconPx,
            float pipPx,
            float primaryFontPx,
            float secondaryFontPx,
            float primaryLineHeightPx,
            float secondaryLineHeightPx,
            int primaryMaxChars,
            int secondaryMaxChars
    ) {
        private static AtlasRenderBudget forScreenBudget(int cellBudgetPx) {
            int clamped = Math.max(1, cellBudgetPx);
            if (clamped >= DETAIL_CELL_PX) {
                return new AtlasRenderBudget(
                        DisclosureLevel.DETAIL,
                        clamped,
                        clamp(clamped * 0.42f, 30f, 54f),
                        clamp(clamped * 0.36f, 24f, 48f),
                        4f,
                        clamp(clamped * 0.078f, 7.75f, 9.0f),
                        clamp(clamped * 0.062f, 6.75f, 7.5f),
                        clamp(clamped * 0.100f, 11.0f, 13.0f),
                        clamp(clamped * 0.076f, 8.5f, 10.0f),
                        38,
                        26
                );
            }
            if (clamped >= INSPECT_CELL_PX) {
                return new AtlasRenderBudget(
                        DisclosureLevel.INSPECT,
                        clamped,
                        clamp(clamped * 0.40f, 26f, 44f),
                        clamp(clamped * 0.34f, 20f, 38f),
                        4f,
                        clamp(clamped * 0.072f, 7.25f, 8.5f),
                        clamp(clamped * 0.058f, 6.5f, 7.25f),
                        clamp(clamped * 0.090f, 10.0f, 12.0f),
                        clamp(clamped * 0.068f, 8.0f, 9.25f),
                        32,
                        18
                );
            }
            if (clamped >= READ_CELL_PX) {
                return new AtlasRenderBudget(
                        DisclosureLevel.READ,
                        clamped,
                        clamp(clamped * 0.70f, 14f, 32f),
                        clamp(clamped * 0.62f, 12f, 28f),
                        4f,
                        clamp(clamped * 0.066f, 6.75f, 7.75f),
                        0f,
                        clamp(clamped * 0.086f, 9.0f, 11.0f),
                        0f,
                        28,
                        0
                );
            }
            if (clamped >= BROWSE_CELL_PX) {
                return new AtlasRenderBudget(
                        DisclosureLevel.BROWSE,
                        clamped,
                        clamp(clamped - 4f, 22f, 46f),
                        clamp(clamped - 8f, 18f, 40f),
                        4f,
                        0f,
                        0f,
                        0f,
                        0f,
                        0,
                        0
                );
            }
            return new AtlasRenderBudget(
                    DisclosureLevel.REGION,
                    clamped,
                    clamp(clamped - 2f, 16f, 34f),
                    clamp(clamped - 6f, 12f, 28f),
                    3f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0,
                    0
            );
        }

        private static float clamp(float value, float min, float max) {
            return Math.max(min, Math.min(max, value));
        }
    }

    private enum DisclosureLevel {
        REGION,
        BROWSE,
        READ,
        INSPECT,
        DETAIL;

        static DisclosureLevel fromScreenBudget(int cellBudgetPx) {
            return AtlasRenderBudget.forScreenBudget(cellBudgetPx).level();
        }

        boolean atLeast(DisclosureLevel minimum) {
            return ordinal() >= minimum.ordinal();
        }
    }
}
