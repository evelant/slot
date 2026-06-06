package dev.imagio.slot.forge.ui;

import dev.imagio.slot.forge.client.ForgeWorkspaceClient;
import dev.imagio.slot.forge.client.ForgeContainerSidebar;
import dev.imagio.slot.forge.config.SlotForgeClientConfig;
import dev.imagio.slot.forge.network.ForgeWorkspaceActionChannel;
import dev.imagio.slot.forge.network.ForgeWorkspaceCloseMessage;
import dev.imagio.slot.forge.network.ForgeWorkspaceOpenMessage;
import dev.imagio.slot.forge.network.ForgeWorkspaceViewModelClientCache;
import dev.imagio.slot.forge.network.SlotForgeNetworking;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.core.ItemStackTags;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.inventory.workspace.WorkspaceSearchQuery;
import dev.imagio.slot.ui.action.WorkspaceActionEnvelope;
import dev.imagio.slot.ui.action.WorkspaceActionId;
import dev.imagio.slot.ui.spi.SlotUiElement;
import dev.imagio.slot.ui.spi.SlotUiEventKind;
import dev.imagio.slot.ui.spi.SlotUiLayout;
import dev.imagio.slot.ui.spi.SlotUiTextStyle;
import dev.imagio.slot.ui.workspace.ActiveChestStripUiBuilder;
import dev.imagio.slot.ui.workspace.CraftRunIngredientChoiceRef;
import dev.imagio.slot.ui.workspace.CraftRunUiBuilder;
import dev.imagio.slot.ui.workspace.HotbarBeltUiBuilder;
import dev.imagio.slot.ui.workspace.KitRackUiBuilder;
import dev.imagio.slot.ui.workspace.RecentsStripUiBuilder;
import dev.imagio.slot.ui.workspace.RecipeIngredientSidebarSpec;
import dev.imagio.slot.ui.workspace.RecipeViewerIntegration;
import dev.imagio.slot.ui.workspace.ShiftClickTransferState;
import dev.imagio.slot.ui.workspace.StorageGhostRevealMode;
import dev.imagio.slot.ui.workspace.WallCardTransferGesturePolicy;
import dev.imagio.slot.ui.workspace.WallCardUiBuilder;
import dev.imagio.slot.ui.workspace.WallSectionHeaderUiBuilder;
import dev.imagio.slot.ui.workspace.WallSectionUiBuilder;
import dev.imagio.slot.ui.workspace.WallSectionVisibility;
import dev.imagio.slot.ui.workspace.WayfindingDisplay;
import dev.imagio.slot.ui.workspace.WheelTransferBatcher;
import dev.imagio.slot.ui.workspace.WorkflowTabsUiBuilder;
import dev.imagio.slot.ui.workspace.WorkspaceTaskPanelUiBuilder;
import dev.imagio.slot.ui.workspace.WorkspaceGatherUiSupport;
import dev.imagio.slot.ui.workspace.WorkspaceCountFormat;
import dev.imagio.slot.ui.workspace.WorkspaceHelpContent;
import dev.imagio.slot.ui.workspace.WorkspaceItemTooltipBuilder;
import dev.imagio.slot.ui.workspace.WorkspaceSearchInputPolicy;
import dev.imagio.slot.ui.workspace.WorkspaceUiAttachments;
import dev.imagio.slot.ui.workspace.WorkspaceUiPalette;
import dev.imagio.slot.ui.workspace.WorkspaceUiSessionMemory;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import dev.imagio.slot.workflow.domain.ChestRole;
import dev.imagio.slot.workflow.domain.CraftRunAlternative;
import dev.imagio.slot.workflow.domain.CraftRunIngredientGroup;
import dev.imagio.slot.workflow.domain.CraftRunRecipeCapture;
import dev.imagio.slot.workflow.domain.CraftRunRecipeEntry;
import dev.imagio.slot.workflow.domain.WorkflowAcceptedInputOptions;
import dev.imagio.slot.workflow.domain.WorkflowAcceptedInputRule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Forge-local workspace controller shared by the full-screen surface and
 * vanilla-container sidebar. Common owns the widget builders and semantics;
 * this class owns Forge session, event, and packet plumbing.
 */
public final class ForgeWorkspaceSurface {
    public static final int WIDTH = 260;
    public static final int SIDE_KIT_RACK_WIDTH = 192;
    public static final int SIDE_KIT_RACK_GAP = 4;

    private static final int STANDALONE_BACKGROUND = 0x96060A0E;
    private static final int SIDEBAR_BACKGROUND = 0xD0060A0E;
    private static final int PANEL = 0xC8162029;
    private static final int RECENT_REHOME_CAPACITY = 6;
    private static final int RECENT_REHOME_MAX_DISPLAYED = 3;
    private static final int REHOME_MENU_MAX_DISPLAYED = 8;
    private static final int CRAFT_RUN_CHOICE_MENU_MAX_ALTERNATIVES = 12;
    private static final int WHEEL_ACCUMULATOR_MAX_IDENTITIES = 64;
    private static final float ITEM_CONTEXT_MENU_MIN_WIDTH = 174f;
    private static final float ITEM_CONTEXT_MENU_MAX_WIDTH = 480f;
    private static final int ITEM_CONTEXT_MENU_TEXT_MARGIN = 16;
    private static final int ITEM_CONTEXT_MENU_TEXT_PX_PER_CHAR = 4;
    private static final int ACCEPTED_INPUT_IDENTIFIER_MAX_CHARS = 88;
    private static final long SEARCH_AUTO_CONFIRM_MILLIS = 2_000L;
    private static final float POPOVER_SCREEN_MARGIN = 4f;
    private static final float POPOVER_TOP_MARGIN = 8f;
    private static final float MENU_LABEL_HEIGHT = 11f;
    private static final float MENU_BUTTON_HEIGHT = 14f;
    private static final float MENU_CUSTOM_ROW_HEIGHT = 14f;

    private final Mode mode;
    private final WorkspaceActionEnvelope envelope;
    private final ForgeWorkspaceActionChannel actionChannel;
    private final List<SlotWorkspaceViewModel.AtlasIsland> islands = new ArrayList<>();
    private final List<SlotWorkspaceViewModel.AtlasItem> items = new ArrayList<>();
    private final List<SlotWorkspaceViewModel.IdentityRef> recents = new ArrayList<>();
    private final List<SlotWorkspaceViewModel.HotbarSlot> hotbarSlots = new ArrayList<>();
    private final Map<SlotWorkspaceViewModel.IdentityRef, SlotWorkspaceViewModel.AtlasItem> byIdentity =
            new LinkedHashMap<>();
    private final ShiftClickTransferState shiftClickTransferState = new ShiftClickTransferState();
    private final WheelTransferBatcher wheelTransferBatcher = new WheelTransferBatcher();
    private final ArrayDeque<String> recentRehomeIslandIds = new ArrayDeque<>();
    private final Map<SlotWorkspaceViewModel.IdentityRef, Float> wheelAccumulatorByIdentity = new LinkedHashMap<>();
    private RecipeIngredientSidebarSpec recipeSidebarSpec = RecipeIngredientSidebarSpec.empty();
    private RecipeIngredientSidebarSpec.Projection recipeSidebarProjection;
    private long recipeSidebarProjectionRevision = Long.MIN_VALUE;
    private String recipeSidebarProjectionKey = "";
    private List<CraftRunRecipeCapture> craftRunRecipeCaptures = List.of();

    private ForgeSlotUiTree tree;
    private SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.empty();
    private SlotWorkspaceViewModel.OffhandSlot offhand = SlotWorkspaceViewModel.OffhandSlot.empty();
    private SlotWorkspaceViewModel.IdentityRef hoveredIdentity;
    private String searchQuery = "";
    private int viewportWidth = 320;
    private int viewportHeight = 240;
    private String lastSentSearchQuery = "";
    private boolean searchActive;
    private long lastSearchInputMillis = System.currentTimeMillis();
    private long appliedRevision = -1L;
    private String status;
    private SlotUiElement statusLabel;
    private boolean flushingWheelTransfer;
    private boolean openSessionRequested;
    private boolean rebuildRequested = true;
    private boolean kitRackOpen;
    private SlotWorkspaceViewModel.IdentityRef contextMenuIdentity;
    private SlotWorkspaceViewModel.AtlasItem contextMenuItemSnapshot;
    private String contextMenuIslandId;
    private String contextMenuKitId;
    private String contextMenuChestStorageId;
    private String renamingKitId;
    private String renameKitDraft = "";
    private String confirmDeleteKitId;
    private String renamingChestStorageId;
    private String renameChestDraft = "";
    private float contextMenuX;
    private float contextMenuY;
    private boolean helpPopoverOpen;
    private float helpPopoverX;
    private float helpPopoverY;
    private String editingIslandId;
    private String islandLabelDraft = "";
    private float islandEditX;
    private float islandEditY;
    private SlotWorkspaceViewModel.IdentityRef editingDesiredCountIdentity;
    private String desiredCountDraft = "";
    private SlotWorkspaceViewModel.IdentityRef pendingHomeDragIdentity;
    private String pendingHomeDragOriginIslandId;
    private boolean markWantedKeyConsumed;
    private boolean setWantedHoverKeyConsumed;
    private boolean trashHoverKeyConsumed;
    private boolean storageXrayKeyConsumed;
    private StorageGhostRevealMode storageGhostRevealMode = StorageGhostRevealMode.COLLAPSED;
    private float pendingWallScrollRestore = Float.NaN;
    private boolean pendingWallScrollRestoreActive;

    public ForgeWorkspaceSurface(Mode mode) {
        this(mode, currentMenuContainerId());
    }

    public ForgeWorkspaceSurface(Mode mode, int menuContainerId) {
        this.mode = mode == null ? Mode.STANDALONE : mode;
        this.envelope = new WorkspaceActionEnvelope(
                UUID.randomUUID().toString(),
                normalizedMenuContainerId(menuContainerId),
                0L);
        this.actionChannel = new ForgeWorkspaceActionChannel(envelope);
        this.status = this.mode == Mode.SIDEBAR
                ? "opening SLOT sidebar"
                : "opening SLOT workspace";
        this.searchQuery = WorkspaceUiSessionMemory.searchQuery(surfaceMemoryKey());
    }

    public int contentWidth() {
        return workspaceWidth();
    }

    public void setRecipeSidebarSpec(RecipeIngredientSidebarSpec spec) {
        RecipeIngredientSidebarSpec next = spec == null ? RecipeIngredientSidebarSpec.empty() : spec;
        String currentKey = recipeSidebarSpec == null ? "" : recipeSidebarSpec.sourceKey();
        if (currentKey.equals(next.sourceKey())) {
            return;
        }
        recipeSidebarSpec = next;
        recipeSidebarProjection = null;
        status = next.active() ? next.label() : openedStatus();
        refreshPresentedItems();
        rebuildRequested = true;
    }

    public void setCraftRunRecipeCaptures(List<CraftRunRecipeCapture> captures) {
        List<CraftRunRecipeCapture> next = activeCraftRunRecipeCaptures(captures);
        if (craftRunRecipeCaptureKey(craftRunRecipeCaptures).equals(craftRunRecipeCaptureKey(next))) {
            return;
        }
        craftRunRecipeCaptures = next;
        rebuildRequested = true;
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

    public void openSessionIfNeeded() {
        if (openSessionRequested) {
            return;
        }
        openSessionRequested = true;
        boolean sent = SlotForgeNetworking.openWorkspaceSession(new ForgeWorkspaceOpenMessage(envelope));
        status = sent ? openedStatus() : "failed to open SLOT workspace";
        if (sent) {
            syncSearchQuery();
        }
        rebuildRequested = true;
    }

    public void closeSession() {
        flushWheelTransferBatch();
        WorkspaceUiSessionMemory.markClosed(surfaceMemoryKey());
        if (!openSessionRequested) {
            return;
        }
        openSessionRequested = false;
        SlotForgeNetworking.closeWorkspaceSession(new ForgeWorkspaceCloseMessage(envelope));
    }

    public void tick(int width, int height) {
        rememberViewport(width, height);
        boolean shiftDown = Screen.hasShiftDown();
        shiftClickTransferState.observeShiftDown(shiftDown);
        if (!ForgeWorkspaceClient.markWantedDown()) {
            markWantedKeyConsumed = false;
        }
        if (!ForgeWorkspaceClient.setWantedHoverDown()) {
            setWantedHoverKeyConsumed = false;
        }
        if (!ForgeWorkspaceClient.trashHoverDown()) {
            trashHoverKeyConsumed = false;
        }
        if (!ForgeWorkspaceClient.storageXrayDown()) {
            storageXrayKeyConsumed = false;
        }
        openSessionIfNeeded();
        flushWheelTransferBatch(shiftDown);
        autoConfirmSearchIfIdle();
        boolean pointerActive = tree != null && tree.hasActivePointerGesture();
        if (!pointerActive) {
            applySyncedViewIfAvailable();
        }
        if (tree == null || (rebuildRequested && !pointerActive)) {
            rebuild(width, height);
            return;
        }
        tree.tick();
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, int width, int height) {
        rememberViewport(width, height);
        if (tree == null) {
            rebuild(width, height);
        }
        tree.compute(width, height);
        tree.render(graphics, mouseX, mouseY);
        renderCursorStack(graphics, mouseX, mouseY);
        if (!isCursorCarrying()) {
            tree.renderTooltip(graphics, mouseX, mouseY);
        }
    }

    public void rebuild(int width, int height) {
        rememberViewport(width, height);
        rebuildRequested = false;
        float scrollY = tree == null ? 0f : tree.scrollY();
        if (tree == null) {
            requestWallScrollRestore(WorkspaceUiSessionMemory.wallScroll(surfaceMemoryKey()));
        }
        tree = ForgeSlotUiTree.build(Minecraft.getInstance(), buildRoot());
        tree.compute(width, height);
        if (pendingWallScrollRestoreActive) {
            applyPendingWallScrollRestore();
        } else {
            tree.setScrollY(scrollY);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (tree == null) {
            return false;
        }
        pendingHomeDragIdentity = null;
        pendingHomeDragOriginIslandId = null;
        return tree.mouseClicked(mouseX, mouseY, button, Screen.hasShiftDown());
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (tree == null) {
            return false;
        }
        boolean hadActivePointerGesture = tree.hasActivePointerGesture();
        SlotWorkspaceViewModel.AtlasIsland releaseIsland = sectionAt(mouseX, mouseY);
        boolean handled = tree.mouseReleased(mouseX, mouseY, button, Screen.hasShiftDown());
        if (completePendingHomeDrag(releaseIsland)) {
            return true;
        }
        if (!hadActivePointerGesture && button == 0 && isCursorCarrying()
                && assignCursorHomeToSection(releaseIsland)) {
            return true;
        }
        pendingHomeDragIdentity = null;
        pendingHomeDragOriginIslandId = null;
        return handled;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (tree == null) {
            return false;
        }
        boolean handled = tree.mouseScrolled(mouseX, mouseY, delta, 22f, Screen.hasShiftDown());
        if (handled) {
            rememberWallScroll();
        }
        return handled;
    }

    public boolean keyPressed(int keyCode, int scanCode) {
        return keyPressed(keyCode, scanCode, false);
    }

    public boolean keyPressed(int keyCode, int scanCode, boolean hostTextInputFocused) {
        if (hostTextInputFocused && !wantsKeyboardInput()) {
            return false;
        }
        if (helpPopoverOpen) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                closeOverlays();
            }
            return true;
        }
        if (handleEditorKey(keyCode)) {
            return true;
        }
        WorkspaceSearchInputPolicy.ControlKey controlKey = searchControlKey(keyCode);
        if (controlKey != null && applySearchDecision(WorkspaceSearchInputPolicy.keyPressed(
                searchActive,
                searchQuery,
                controlKey))) {
            return true;
        }
        if (searchActive && isSearchTypingKey(keyCode)) {
            return true;
        }
        if (wantsKeyboardInput()) {
            return true;
        }
        if (!wantsKeyboardInput()) {
            if (ForgeWorkspaceClient.matchesUndo(keyCode, scanCode)) {
                sendAction(WorkspaceActionId.UNDO, "undo requested");
                return true;
            }
            if (ForgeWorkspaceClient.matchesRedo(keyCode, scanCode)) {
                sendAction(WorkspaceActionId.REDO, "redo requested");
                return true;
            }
        }
        if (handleAutoHotbarKey(keyCode)) {
            return true;
        }
        if (handleMoveToMainInventoryKey(keyCode, scanCode)) {
            return true;
        }
        if (handleSetWantedHoverKey(keyCode, scanCode)) {
            return true;
        }
        if (handleTrashHoverKey(keyCode, scanCode)) {
            return true;
        }
        if (handleMarkWantedKey(keyCode, scanCode)) {
            return true;
        }
        if (handleStorageXrayKey(keyCode, scanCode)) {
            return true;
        }
        if (handleRecipeViewerKey(keyCode)) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && isCursorCarrying()) {
            sendAction(WorkspaceActionId.CURSOR_CANCEL, "returning cursor");
            return true;
        }
        if (ForgeWorkspaceClient.matchesOpenVanilla(keyCode, scanCode)) {
            return openVanillaInventory();
        }
        if (ForgeWorkspaceClient.matchesCycleKitPage(keyCode, scanCode)) {
            int direction = Screen.hasShiftDown() ? -1 : 1;
            return switchKitPageFromKey(direction);
        }
        if (ForgeWorkspaceClient.matchesGatherActiveKit(keyCode, scanCode)) {
            return gatherActiveKitFromKey();
        }
        if (ForgeWorkspaceClient.matchesDepositPutAway(keyCode, scanCode)) {
            return depositPutAwayFromKey();
        }
        int hotbarIndex = hotbarIndexFromKeyCode(keyCode);
        if (hotbarIndex >= 0) {
            if (mode == Mode.SIDEBAR && hoveredIdentity == null && !searchActive) {
                return false;
            }
            handleHotbarKey(hotbarIndex);
            return true;
        }
        return false;
    }

    private boolean openVanillaInventory() {
        if (mode == Mode.SIDEBAR) {
            return false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.player == null) {
            status = "vanilla inventory unavailable";
            rebuildRequested = true;
            return true;
        }
        ForgeContainerSidebar.openVanillaInventory();
        return true;
    }

    private boolean handleRecipeViewerKey(int keyCode) {
        if (searchActive || wantsKeyboardInput() || Screen.hasControlDown()) {
            return false;
        }
        if (keyCode != GLFW.GLFW_KEY_R && keyCode != GLFW.GLFW_KEY_U) {
            return false;
        }
        if (hoveredIdentity == null || !byIdentity.containsKey(hoveredIdentity)) {
            setStatus("hover an item for recipe or usage details");
            return true;
        }
        SlotWorkspaceViewModel.AtlasItem target = byIdentity.get(hoveredIdentity);
        if (keyCode == GLFW.GLFW_KEY_R) {
            openRecipe(target);
        } else {
            openUses(target);
        }
        return true;
    }

    private boolean handleMarkWantedKey(int keyCode, int scanCode) {
        if (searchActive || wantsKeyboardInput() || Screen.hasControlDown()) {
            return false;
        }
        if (!ForgeWorkspaceClient.matchesMarkWanted(keyCode, scanCode)) {
            return false;
        }
        if (isAltKey(keyCode)) {
            return true;
        }
        if (markWantedKeyConsumed) {
            return true;
        }
        markWantedKeyConsumed = true;
        SlotWorkspaceViewModel.IdentityRef identity = hoveredIdentity;
        if (identity == null || !byIdentity.containsKey(identity)) {
            setStatus("hover an item to mark wanted");
            return true;
        }
        sendIdentityRefAction(WorkspaceActionId.TOGGLE_WANTED_ITEM, identity, "wanted item updated");
        return true;
    }

    private boolean handleSetWantedHoverKey(int keyCode, int scanCode) {
        if (searchActive || wantsKeyboardInput() || Screen.hasControlDown()) {
            return false;
        }
        if (!ForgeWorkspaceClient.matchesSetWantedHover(keyCode, scanCode)) {
            return false;
        }
        if (setWantedHoverKeyConsumed) {
            return true;
        }
        setWantedHoverKeyConsumed = true;
        SlotWorkspaceViewModel.IdentityRef identity = hoveredIdentity;
        SlotWorkspaceViewModel.AtlasItem item = identity == null ? null : byIdentity.get(identity);
        if (identity == null || item == null) {
            setStatus("hover an item to mark wanted");
            return true;
        }
        sendIdentityRefAction(
                WorkspaceActionId.SET_WANTED_COUNT,
                identity,
                "wanted count updated",
                wantedHoverTargetCount(item));
        return true;
    }

    private boolean handleTrashHoverKey(int keyCode, int scanCode) {
        if (searchActive || wantsKeyboardInput() || Screen.hasControlDown()) {
            return false;
        }
        if (!ForgeWorkspaceClient.matchesTrashHover(keyCode, scanCode)) {
            return false;
        }
        if (trashHoverKeyConsumed) {
            return true;
        }
        trashHoverKeyConsumed = true;
        SlotWorkspaceViewModel.IdentityRef identity = hoveredIdentity;
        if (identity == null || !byIdentity.containsKey(identity)) {
            setStatus("hover an item to trash");
            return true;
        }
        sendIdentityRefAction(WorkspaceActionId.TRASH_IDENTITY, identity, "trashing carried item");
        return true;
    }

    private boolean handleStorageXrayKey(int keyCode, int scanCode) {
        if (searchActive || wantsKeyboardInput() || Screen.hasControlDown()) {
            return false;
        }
        if (!ForgeWorkspaceClient.matchesStorageXray(keyCode, scanCode)) {
            return false;
        }
        if (storageXrayKeyConsumed) {
            return true;
        }
        storageXrayKeyConsumed = true;
        toggleStorageGhostRevealMode(Screen.hasShiftDown()
                ? StorageGhostRevealMode.TRACKED
                : StorageGhostRevealMode.PROXIMATE);
        return true;
    }

    private boolean handleAutoHotbarKey(int keyCode) {
        if (keyCode != GLFW.GLFW_KEY_TAB || Screen.hasShiftDown() || editorOpen() || Screen.hasControlDown()) {
            return false;
        }
        SlotWorkspaceViewModel.IdentityRef identity = hoveredIdentity;
        if (identity == null || !byIdentity.containsKey(identity)) {
            return false;
        }
        applySearchDecision(WorkspaceSearchInputPolicy.confirmForHotbar(searchActive, searchQuery));
        sendIdentityToAutoHotbar(identity);
        return true;
    }

    private boolean handleMoveToMainInventoryKey(int keyCode, int scanCode) {
        if (searchActive || wantsKeyboardInput() || editorOpen() || Screen.hasControlDown()) {
            return false;
        }
        boolean toBackpack = ForgeWorkspaceClient.matchesMoveToBackpack(keyCode, scanCode);
        boolean toMain = ForgeWorkspaceClient.matchesMoveToMainInventory(keyCode, scanCode);
        if (!toBackpack && !toMain) {
            return false;
        }
        SlotWorkspaceViewModel.IdentityRef identity = hoveredIdentity;
        if (identity == null || !byIdentity.containsKey(identity)) {
            setStatus(toBackpack ? "hover an item to move to backpack" : "hover an item to move to main inventory");
            return true;
        }
        sendIdentityRefAction(
                toBackpack ? WorkspaceActionId.MOVE_IDENTITY_TO_BACKPACK : WorkspaceActionId.MOVE_IDENTITY_TO_MAIN_INVENTORY,
                identity,
                toBackpack ? "moving to backpack" : "moving to main inventory");
        return true;
    }

    private static boolean isAltKey(int keyCode) {
        return keyCode == GLFW.GLFW_KEY_LEFT_ALT || keyCode == GLFW.GLFW_KEY_RIGHT_ALT;
    }

    private boolean editorOpen() {
        return editingIslandId != null
                || editingDesiredCountIdentity != null
                || renamingKitId != null
                || renamingChestStorageId != null;
    }

    private boolean switchKitPageFromKey(int direction) {
        SlotWorkspaceViewModel.KitCard active = viewModel.activeKit();
        if (active == null || active.pageCount() <= 1) {
            status = active == null ? "activate a workflow first" : "workflow has one page";
            rebuildRequested = true;
            return true;
        }
        sendKitAction(WorkspaceActionId.SWITCH_KIT_PAGE, "switching workflow page", direction);
        return true;
    }

    private boolean gatherActiveKitFromKey() {
        if (!anyGatherableIdentity()) {
            status = "nothing to gather";
            rebuildRequested = true;
            return true;
        }
        sendAction(WorkspaceActionId.GATHER_ACTIVE_KIT, "gathering target items from nearby chests");
        return true;
    }

    private boolean depositPutAwayFromKey() {
        if (viewModel.depositableIdentities().isEmpty()) {
            status = "nothing to put away";
            rebuildRequested = true;
            return true;
        }
        sendAction(WorkspaceActionId.DEPOSIT, "putting away carried clutter");
        return true;
    }

    public boolean charTyped(char codePoint) {
        return charTyped(codePoint, false);
    }

    public boolean charTyped(char codePoint, boolean hostTextInputFocused) {
        if (helpPopoverOpen) {
            return true;
        }
        if (handleEditorChar(codePoint)) {
            return true;
        }
        return applySearchDecision(WorkspaceSearchInputPolicy.charTyped(
                searchActive,
                searchQuery,
                codePoint,
                hostTextInputFocused));
    }

    public boolean wantsKeyboardInput() {
        return searchActive
                || editingIslandId != null
                || editingDesiredCountIdentity != null
                || renamingKitId != null
                || renamingChestStorageId != null;
    }

    public boolean capturesTextInput() {
        return wantsKeyboardInput();
    }

    private String openedStatus() {
        if (mode == Mode.SIDEBAR) {
            return "opened chest sidebar";
        }
        return "opened SLOT workspace";
    }

    private void applySyncedViewIfAvailable() {
        SlotWorkspaceViewModel synced = ForgeWorkspaceViewModelClientCache.latestFor(envelope.sessionId());
        if (synced == null || synced.revision() <= appliedRevision) {
            return;
        }
        appliedRevision = synced.revision();
        applyViewModel(synced);
        status = recipeSidebarActive() ? recipeSidebarSpec.label() : displayStatus(synced.status(), synced.diagnostics());
        rebuildRequested = true;
    }

    private static String displayStatus(String nextStatus, String nextDiagnostics) {
        String base = nextStatus == null || nextStatus.isBlank() ? "ready" : nextStatus;
        if (nextDiagnostics == null || nextDiagnostics.isBlank()) {
            return base;
        }
        return base + " - " + nextDiagnostics;
    }

    private void applyViewModel(SlotWorkspaceViewModel synced) {
        viewModel = synced == null ? SlotWorkspaceViewModel.empty() : synced;
        recipeSidebarProjection = null;
        recents.clear();
        recents.addAll(viewModel.recentIdentities());
        hotbarSlots.clear();
        hotbarSlots.addAll(viewModel.hotbarSlots());
        offhand = viewModel.offhand();
        refreshPresentedItems();
    }

    private void refreshPresentedItems() {
        islands.clear();
        items.clear();
        byIdentity.clear();
        RecipeIngredientSidebarSpec.Projection recipe = recipeProjection();
        if (recipe != null) {
            islands.addAll(recipe.islands());
            items.addAll(recipe.atlasItems());
        } else {
            islands.addAll(viewModel.islands());
            items.addAll(viewModel.atlasItems());
        }
        for (SlotWorkspaceViewModel.AtlasItem item : items) {
            byIdentity.put(item.identity(), item);
        }
        for (SlotWorkspaceViewModel.AtlasItem item : viewModel.triageItems()) {
            byIdentity.putIfAbsent(item.identity(), item);
        }
        for (SlotWorkspaceViewModel.ContextualSuggestionLane lane : viewModel.contextualSuggestionLanes()) {
            if (lane == null) {
                continue;
            }
            for (SlotWorkspaceViewModel.AtlasItem item : lane.items()) {
                if (item != null) {
                    byIdentity.putIfAbsent(item.identity(), item);
                }
            }
        }
    }

    private boolean recipeSidebarActive() {
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

    private int workspaceWidth() {
        return WIDTH + (kitRackOpen ? SIDE_KIT_RACK_GAP + SIDE_KIT_RACK_WIDTH : 0);
    }

    private SlotUiElement spacer(int width, int height) {
        return SlotUiElement.element()
                .allowHitTest(false)
                .layout(layout -> layout.width(width).height(height));
    }

    private String surfaceMemoryKey() {
        return mode == Mode.SIDEBAR ? "forge.sidebar" : "forge.standalone";
    }

    private void rememberViewport(int width, int height) {
        viewportWidth = Math.max(1, width);
        viewportHeight = Math.max(1, height);
    }

    private boolean storageGhostSectionExpanded(String islandId) {
        return WorkspaceUiSessionMemory.storageGhostSectionExpanded(surfaceMemoryKey(), islandId);
    }

    private void toggleStorageGhostSection(String islandId) {
        boolean expanded = WorkspaceUiSessionMemory.toggleStorageGhostSection(surfaceMemoryKey(), islandId);
        setStatus(expanded ? "showing nearby storage" : "hiding nearby storage");
    }

    private void updateStorageGhostRevealMode(StorageGhostRevealMode nextMode) {
        StorageGhostRevealMode mode = nextMode == null ? StorageGhostRevealMode.COLLAPSED : nextMode;
        if (mode == storageGhostRevealMode) {
            return;
        }
        storageGhostRevealMode = mode;
        rebuildRequested = true;
    }

    private void toggleStorageGhostRevealMode(StorageGhostRevealMode requestedMode) {
        StorageGhostRevealMode requested = requestedMode == null
                ? StorageGhostRevealMode.PROXIMATE
                : requestedMode;
        StorageGhostRevealMode next = switch (requested) {
            case TRACKED -> storageGhostRevealMode.toggleTracked();
            case PROXIMATE -> storageGhostRevealMode.toggleProximate();
            case COLLAPSED -> StorageGhostRevealMode.COLLAPSED;
        };
        setStatus(switch (next) {
            case TRACKED -> "showing all tracked storage";
            case PROXIMATE -> "showing nearby storage";
            case COLLAPSED -> "hiding storage ghosts";
        });
        updateStorageGhostRevealMode(next);
    }

    private void rememberWallScroll() {
        if (tree != null) {
            if (pendingWallScrollRestoreActive && tree.maxScrollY() <= 0f) {
                return;
            }
            WorkspaceUiSessionMemory.setWallScroll(surfaceMemoryKey(), tree.scrollY());
        }
    }

    private void requestWallScrollRestore(float scrollY) {
        pendingWallScrollRestore = Float.isFinite(scrollY) ? Math.max(0f, scrollY) : 0f;
        pendingWallScrollRestoreActive = pendingWallScrollRestore > 0f;
    }

    private void applyPendingWallScrollRestore() {
        if (!pendingWallScrollRestoreActive || tree == null) {
            return;
        }
        tree.setScrollY(pendingWallScrollRestore);
        if (tree.maxScrollY() > 0f || appliedRevision >= 0L) {
            pendingWallScrollRestoreActive = false;
        }
    }

    private SlotUiElement buildRoot() {
        if (mode == Mode.SIDEBAR) {
            SlotUiElement root = SlotUiElement.element()
                    .allowHitTest(false)
                    .layout(layout -> layout
                            .widthPercent(100)
                            .heightPercent(100)
                            .alignItems(SlotUiLayout.AlignItems.FLEX_START)
                            .flexDirection(SlotUiLayout.FlexDirection.ROW));
            int leftMargin = SlotForgeClientConfig.sidebarLeftMargin();
            if (leftMargin > 0) {
                root.addChild(spacer(leftMargin, 1));
            }
            SlotUiElement sidebarRail = SlotUiElement.element()
                    .allowHitTest(false)
                    .layout(layout -> layout
                            .width(workspaceWidth())
                            .heightPercent(100)
                            .flexDirection(SlotUiLayout.FlexDirection.COLUMN));
            int topMargin = SlotForgeClientConfig.sidebarTopMargin();
            if (topMargin > 0) {
                sidebarRail.addChild(spacer(1, topMargin));
            }
            sidebarRail.addChild(workspaceColumn(true));
            int bottomMargin = SlotForgeClientConfig.sidebarBottomMargin();
            if (bottomMargin > 0) {
                sidebarRail.addChild(spacer(1, bottomMargin));
            }
            root.addChild(sidebarRail);
            addFloatingRecents(root);
            addTaskPanel(root);
            addActiveOverlay(root);
            return root;
        }
        SlotUiElement root = SlotUiElement.element()
                .layout(layout -> layout
                        .widthPercent(100)
                        .heightPercent(100)
                        .alignItems(SlotUiLayout.AlignItems.CENTER)
                        .flexDirection(SlotUiLayout.FlexDirection.COLUMN));
        root.addChild(workspaceColumn(false));
        addFloatingRecents(root);
        addTaskPanel(root);
        addActiveOverlay(root);
        return root;
    }

    private void addFloatingRecents(SlotUiElement root) {
        SlotUiElement panel = floatingRecents();
        if (root != null && panel != null) {
            root.addChild(panel);
        }
    }

    private void addTaskPanel(SlotUiElement root) {
        SlotUiElement panel = taskPanel();
        if (root != null && panel != null) {
            root.addChild(panel);
        }
    }

    private void addActiveOverlay(SlotUiElement root) {
        SlotUiElement overlay = activeOverlay();
        if (root != null && overlay != null) {
            root.addChild(overlay);
        }
    }

    private SlotUiElement workspaceColumn(boolean sidebarMode) {
        SlotUiElement column = SlotUiElement.panel(sidebarMode ? SIDEBAR_BACKGROUND : STANDALONE_BACKGROUND)
                .layout(layout -> {
                    if (sidebarMode) {
                        layout.width(workspaceWidth()).flex(1);
                    } else {
                        layout.widthPercent(100).alignItems(SlotUiLayout.AlignItems.CENTER);
                    }
                    if (!sidebarMode) {
                        layout.heightPercent(100);
                    }
                    layout.paddingAll(8)
                            .gapAll(4)
                            .flexDirection(SlotUiLayout.FlexDirection.COLUMN);
                });
        column.on(SlotUiEventKind.MOUSE_DOWN, event -> {
            if (!isCursorCarrying()) {
                return;
            }
            if (event.button() == 1) {
                event.stopPropagation();
                sendAction(WorkspaceActionId.CURSOR_CANCEL, "returning cursor");
                return;
            }
            if (event.button() == 0) {
                event.stopPropagation();
                sendAction(WorkspaceActionId.CURSOR_SMART_DEPOSIT, "depositing cursor");
            }
        });

        if (sidebarMode) {
            column.addChild(searchDepositRow(true));
            SlotUiElement activeChestStrip = activeChestStrip(true);
            if (activeChestStrip != null) {
                column.addChild(activeChestStrip);
            }
        } else {
            column.addChild(titleRow(false));
            column.addChild(searchDepositRow(false));
            SlotUiElement activeChestStrip = activeChestStrip(false);
            if (activeChestStrip != null) {
                column.addChild(activeChestStrip);
            }
        }
        column.addChild(workflowTabs(sidebarMode));
        column.addChild(wallArea(sidebarMode));
        column.addChild(statusRow(sidebarMode));
        column.addChild(hotbar(sidebarMode));
        return column;
    }

    private SlotUiElement workflowTabs(boolean sidebarMode) {
        int workflowTabsHeight = WorkflowTabsUiBuilder.height(viewModel);
        SlotUiElement stack = SlotUiElement.element()
                .layout(layout -> {
                    if (sidebarMode) {
                        layout.widthPercent(100);
                    } else {
                        layout.width(workspaceWidth());
                    }
                    layout.gapAll(4)
                            .flexDirection(SlotUiLayout.FlexDirection.COLUMN);
                });
        SlotUiElement workflowRow = SlotUiElement.element()
                .layout(layout -> layout
                        .widthPercent(100)
                        .height(Math.max(workflowTabsHeight, KitRackUiBuilder.CLUSTER_HEIGHT_PX))
                        .gapAll(4)
                        .alignItems(SlotUiLayout.AlignItems.CENTER)
                        .flexDirection(SlotUiLayout.FlexDirection.ROW))
                .addChild(new WorkflowTabsUiBuilder(new WorkflowTabsContext()).tabs(viewModel)
                        .layout(layout -> layout.flex(1).height(workflowTabsHeight)))
                .addChild(new KitRackUiBuilder(new KitContext())
                        .cluster(viewModel, kitRackOpen, true)
                        .layout(layout -> layout.height(KitRackUiBuilder.CLUSTER_HEIGHT_PX)));
        stack.addChild(workflowRow);
        return stack;
    }

    private SlotUiElement titleRow(boolean sidebarMode) {
        SlotUiElement row = SlotUiElement.element()
                .layout(layout -> {
                    if (sidebarMode) {
                        layout.widthPercent(100);
                    } else {
                        layout.width(workspaceWidth());
                    }
                    layout.height(16)
                            .gapAll(4)
                            .alignItems(SlotUiLayout.AlignItems.CENTER)
                            .flexDirection(SlotUiLayout.FlexDirection.ROW);
                });
        row.addChild(SlotUiElement.label("SLOT", WorkspaceUiPalette.ACCENT)
                .layout(layout -> layout.flex(1).heightPercent(100))
                .textStyle(style -> style
                        .color(WorkspaceUiPalette.ACCENT)
                        .fontSize(9)
                        .horizontal(SlotUiTextStyle.Horizontal.LEFT)
                        .vertical(SlotUiTextStyle.Vertical.CENTER)));
        if (!sidebarMode) {
            row.addChild(iconButton(
                            ForgeSlotUiTree.Icon.VANILLA_GRID,
                            true,
                            WorkspaceUiPalette.ROW_DIM,
                            WorkspaceUiPalette.TEXT,
                            "Open the vanilla inventory screen")
                    .on(SlotUiEventKind.CLICK, event -> {
                        if (event.button() != 0) {
                            return;
                        }
                        event.stopPropagation();
                        openVanillaInventory();
                    }));
        }
        return row;
    }

    private SlotUiElement activeChestStrip(boolean sidebarMode) {
        SlotUiElement activeChestStrip = new ActiveChestStripUiBuilder(new ActiveChestContext())
                .strip(viewModel.activeChestPanel());
        if (activeChestStrip != null) {
            if (!sidebarMode) {
                activeChestStrip.layout(layout -> layout.width(workspaceWidth()));
            }
            return activeChestStrip;
        }
        return null;
    }

    private SlotUiElement searchDepositRow(boolean sidebarMode) {
        SlotUiElement row = SlotUiElement.panel(PANEL)
                .layout(layout -> {
                    if (sidebarMode) {
                        layout.widthPercent(100);
                    } else {
                        layout.width(workspaceWidth());
                    }
                    layout.height(16)
                            .paddingHorizontal(6)
                            .gapAll(4)
                            .alignItems(SlotUiLayout.AlignItems.CENTER)
                            .flexDirection(SlotUiLayout.FlexDirection.ROW);
                });
        row.addChild(searchBarLabel("Search", WorkspaceUiPalette.MUTED)
                .layout(layout -> layout.width(34))
                .textStyle(style -> style
                        .color(WorkspaceUiPalette.MUTED)
                        .fontSize(7)
                        .horizontal(SlotUiTextStyle.Horizontal.LEFT)));
        row.addChild(searchBarLabel(searchDisplayText(), WorkspaceUiPalette.TEXT)
                .layout(layout -> layout.flex(1))
                .textStyle(style -> style
                        .color(searchActive ? WorkspaceUiPalette.ACCENT : WorkspaceUiPalette.TEXT)
                        .fontSize(8)
                        .horizontal(SlotUiTextStyle.Horizontal.LEFT)));
        row.addChild(SlotUiElement.label(viewModel.carriedFreeSlotCount() + " free", WorkspaceUiPalette.TEXT)
                .layout(layout -> layout.width(44))
                .textStyle(style -> style
                        .color(WorkspaceUiPalette.TEXT)
                        .fontSize(7)
                        .horizontal(SlotUiTextStyle.Horizontal.CENTER)
                        .vertical(SlotUiTextStyle.Vertical.CENTER)));
        row.addChild(helpButton());
        row.addChild(storageXrayToggleButton("N", StorageGhostRevealMode.PROXIMATE));
        row.addChild(storageXrayToggleButton("T", StorageGhostRevealMode.TRACKED));
        row.addChild(gatherButton());
        row.addChild(depositButton());
        return row;
    }

    private SlotUiElement searchBarLabel(String text, int color) {
        return SlotUiElement.label(text, color)
                .allowHitTest(true)
                .on(SlotUiEventKind.MOUSE_DOWN, event -> {
                    if (event.button() != 1) {
                        return;
                    }
                    event.stopPropagation();
                    clearSearchFromPointer();
                });
    }

    private SlotUiElement helpButton() {
        return SlotUiElement.button("?", true, WorkspaceUiPalette.ROW_DIM)
                .tooltip(Component.literal("SLOT help"))
                .layout(layout -> layout.width(16).height(16))
                .textStyle(style -> style
                        .color(WorkspaceUiPalette.TEXT)
                        .fontSize(8)
                        .horizontal(SlotUiTextStyle.Horizontal.CENTER)
                        .vertical(SlotUiTextStyle.Vertical.CENTER))
                .on(SlotUiEventKind.CLICK, event -> {
                    if (event.button() != 0) {
                        return;
                    }
                    event.stopPropagation();
                    openHelpPopover(event.x(), event.y());
                });
    }

    private SlotUiElement storageXrayToggleButton(String label, StorageGhostRevealMode mode) {
        boolean active = storageGhostRevealMode == mode;
        return SlotUiElement.button(label, true, active ? WorkspaceUiPalette.SELECTED : WorkspaceUiPalette.ROW_DIM)
                .tooltip(Component.literal(storageXrayTooltip(mode)))
                .layout(layout -> layout.width(12).height(16))
                .textStyle(style -> style
                        .color(active ? WorkspaceUiPalette.TEXT : WorkspaceUiPalette.MUTED)
                        .fontSize(7)
                        .horizontal(SlotUiTextStyle.Horizontal.CENTER)
                        .vertical(SlotUiTextStyle.Vertical.CENTER))
                .on(SlotUiEventKind.CLICK, event -> {
                    if (event.button() != 0) {
                        return;
                    }
                    event.stopPropagation();
                    toggleStorageGhostRevealMode(mode);
                });
    }

    private String storageXrayTooltip(StorageGhostRevealMode mode) {
        String key = ForgeWorkspaceClient.storageXrayKeyLabel();
        if (mode == StorageGhostRevealMode.TRACKED) {
            return "Toggle all tracked storage ghosts (Shift+" + key + "). "
                    + "Tracked ghosts are browse-only unless the storage is nearby.";
        }
        return "Toggle nearby storage ghosts (" + key + "). "
                + "Shows proximate storage ghosts until toggled off.";
    }

    private SlotUiElement gatherButton() {
        boolean enabled = anyGatherableIdentity();
        int color = enabled ? WorkspaceUiPalette.ROW_HOVER : WorkspaceUiPalette.ROW_DIM;
        return iconButton(
                        ForgeSlotUiTree.Icon.GATHER,
                        true,
                        color,
                        enabled ? WorkspaceUiPalette.TEXT : WorkspaceUiPalette.MUTED,
                        "Pull target-count gaps and active-workflow needs from nearby chests.")
                .on(SlotUiEventKind.CLICK, event -> {
                    if (event.button() != 0) {
                        return;
                    }
                    event.stopPropagation();
                    if (!enabled) {
                        status = "nothing to gather";
                        rebuildRequested = true;
                        return;
                    }
                    sendAction(WorkspaceActionId.GATHER_ACTIVE_KIT, "gathering target items from nearby chests");
                });
    }

    private SlotUiElement depositButton() {
        return iconButton(
                        ForgeSlotUiTree.Icon.DEPOSIT,
                        true,
                        WorkspaceUiPalette.ROW_HOVER,
                        WorkspaceUiPalette.TEXT,
                        "Deposit carried items into eligible nearby chests by learned affinity or matching contents.")
                .tooltip(Component.literal(
                        "Deposit carried items into eligible nearby chests by learned affinity or matching contents. "
                                + "Items without either signal stay in carry."))
                .on(SlotUiEventKind.CLICK, event -> {
                    if (event.button() != 0) {
                        return;
                    }
                    event.stopPropagation();
                    sendAction(WorkspaceActionId.DEPOSIT, "depositing carried items");
                });
    }

    private SlotUiElement iconButton(
            ForgeSlotUiTree.Icon icon,
            boolean enabled,
            int color,
            int iconColor,
            String tooltip
    ) {
        return SlotUiElement.button("", enabled, color)
                .noText()
                .attach(ForgeSlotUiTree.ICON, icon)
                .tooltip(Component.literal(tooltip == null ? "" : tooltip))
                .layout(layout -> layout.width(16).height(16))
                .textStyle(style -> style
                        .color(iconColor)
                        .horizontal(SlotUiTextStyle.Horizontal.CENTER)
                        .vertical(SlotUiTextStyle.Vertical.CENTER));
    }

    private SlotUiElement kitRack(boolean sidebarMode) {
        SlotUiElement rack = new KitRackUiBuilder(new KitContext()).rack(viewModel)
                .layout(layout -> layout.widthPercent(100).heightPercent(100));
        return SlotUiElement.element()
                .layout(layout -> layout
                        .width(SIDE_KIT_RACK_WIDTH)
                        .heightPercent(100)
                        .flexDirection(SlotUiLayout.FlexDirection.COLUMN))
                .addChild(rack);
    }

    private SlotUiElement floatingRecents() {
        SlotUiElement strip = new RecentsStripUiBuilder(new RecentsContext()).overlay(recents);
        enrichRecentCards(strip);
        return strip.zIndex(18)
                .layout(layout -> layout
                        .positionType(SlotUiLayout.PositionType.ABSOLUTE)
                        .left(RecentsStripUiBuilder.floatingLeft(
                                viewportWidth,
                                SlotForgeClientConfig.recentsHorizontalOffset()))
                        .top(RecentsStripUiBuilder.floatingTop(SlotForgeClientConfig.recentsTopOffset()))
                        .width(RecentsStripUiBuilder.STRIP_WIDTH_PX)
                        .height(RecentsStripUiBuilder.STRIP_HEIGHT_PX));
    }

    private List<SlotUiElement> contextualSuggestionRows() {
        if (recipeSidebarActive()) {
            return List.of();
        }
        boolean filtering = !normalizedSearchQuery().isBlank();
        WallSectionHeaderUiBuilder headerBuilder = new WallSectionHeaderUiBuilder(new HeaderContext());
        WallSectionUiBuilder sectionBuilder = new WallSectionUiBuilder(headerBuilder);
        ArrayList<SlotUiElement> rows = new ArrayList<>();
        for (SlotWorkspaceViewModel.ContextualSuggestionLane lane : viewModel.contextualSuggestionLanes()) {
            if (hideFetchLaneForCraftRun(lane)) {
                continue;
            }
            SlotWorkspaceViewModel.ContextualSuggestionLane visibleLane = visibleSuggestionLane(lane, filtering);
            if (WallSectionUiBuilder.shouldRenderSuggestionLane(visibleLane)) {
                rows.add(sectionBuilder.suggestionLane(visibleLane));
            }
        }
        return rows.isEmpty() ? List.of() : List.copyOf(rows);
    }

    private boolean contextualSuggestionRowsVisible() {
        if (recipeSidebarActive()) {
            return false;
        }
        boolean filtering = !normalizedSearchQuery().isBlank();
        for (SlotWorkspaceViewModel.ContextualSuggestionLane lane : viewModel.contextualSuggestionLanes()) {
            if (hideFetchLaneForCraftRun(lane)) {
                continue;
            }
            if (WallSectionUiBuilder.shouldRenderSuggestionLane(visibleSuggestionLane(lane, filtering))) {
                return true;
            }
        }
        return false;
    }

    private SlotUiElement wallArea(boolean sidebarMode) {
        SlotUiElement area = SlotUiElement.element()
                .layout(layout -> {
                    if (sidebarMode) {
                        layout.widthPercent(100);
                    } else {
                        layout.width(workspaceWidth());
                    }
                    layout.flex(1)
                            .flexDirection(SlotUiLayout.FlexDirection.COLUMN);
                });
        SlotUiElement row = SlotUiElement.element()
                .layout(layout -> layout
                        .widthPercent(100)
                        .heightPercent(100)
                        .gapAll(3)
                        .alignItems(SlotUiLayout.AlignItems.STRETCH)
                        .flexDirection(SlotUiLayout.FlexDirection.ROW));
        row.addChild(tocStrip());
        row.addChild(SlotUiElement.element()
                .layout(layout -> layout
                        .flex(1)
                        .heightPercent(100)
                        .flexDirection(SlotUiLayout.FlexDirection.COLUMN))
                .addChild(wallViewport(sidebarMode)));
        if (kitRackOpen) {
            row.addChild(kitRack(sidebarMode));
        }
        area.addChild(row);
        return area;
    }

    private SlotUiElement tocStrip() {
        List<SlotWorkspaceViewModel.AtlasIsland> entries = tocEntries();
        SlotUiElement strip = SlotUiElement.panel(0x7010171D)
                .tooltip(Component.literal("Section index"))
                .layout(layout -> layout
                        .width(12)
                        .heightPercent(100)
                        .paddingVertical(2)
                        .gapAll(1)
                        .alignItems(SlotUiLayout.AlignItems.CENTER)
                        .flexDirection(SlotUiLayout.FlexDirection.COLUMN));
        if (entries.isEmpty()) {
            strip.allowHitTest(false);
            return strip;
        }
        int count = entries.size();
        for (int index = 0; index < count; index++) {
            SlotWorkspaceViewModel.AtlasIsland island = entries.get(index);
            float fraction = count <= 1 ? 0f : (float) index / (float) (count - 1);
            int color = island.color() == 0 ? WorkspaceUiPalette.MUTED : island.color();
            SlotUiElement dot = SlotUiElement.button("", true, 0x00000000)
                    .noText()
                    .tooltip(Component.literal(island.label()))
                    .layout(layout -> layout
                            .widthPercent(100)
                            .flex(1)
                            .paddingAll(0)
                            .alignItems(SlotUiLayout.AlignItems.CENTER)
                            .flexDirection(SlotUiLayout.FlexDirection.COLUMN));
            dot.addChild(SlotUiElement.panel(color)
                    .allowHitTest(false)
                    .layout(layout -> layout
                            .positionType(SlotUiLayout.PositionType.ABSOLUTE)
                            .left(3)
                            .top(3)
                            .width(6)
                            .height(6)));
            dot.on(SlotUiEventKind.TICK, event -> dotAttention(dot, island));
            dot.on(SlotUiEventKind.MOUSE_DOWN, event -> {
                if (event.button() != 1) {
                    return;
                }
                event.stopPropagation();
                openSectionOrderMenu(island, event.x(), event.y());
            });
            dot.on(SlotUiEventKind.CLICK, event -> {
                if (event.button() != 0) {
                    return;
                }
                event.stopPropagation();
                if (tree != null) {
                    if (!tree.scrollToElementId(island.islandId())) {
                        tree.scrollToFraction(fraction);
                    }
                    rememberWallScroll();
                }
                setStatus(island.label());
            });
            strip.addChild(dot);
        }
        return strip;
    }

    private List<SlotWorkspaceViewModel.AtlasIsland> tocEntries() {
        ArrayList<SlotWorkspaceViewModel.AtlasIsland> entries = new ArrayList<>();
        boolean filtering = !normalizedSearchQuery().isBlank();
        for (SlotWorkspaceViewModel.AtlasIsland island : islands) {
            if (island == null || island.kind() != VisualAtlasIslandKind.PLAYER) {
                continue;
            }
            if (WallSectionVisibility.classify(
                    visibleItemsFor(island, filtering),
                    filtering,
                    storageGhostSectionExpanded(island.islandId()),
                    storageGhostRevealMode,
                    false,
                    !activeWorkflowTab()).hasVisibleContent()) {
                entries.add(island);
            }
        }
        return entries;
    }

    private List<SlotWorkspaceViewModel.AtlasItem> visibleItemsFor(
            SlotWorkspaceViewModel.AtlasIsland island,
            boolean filtering
    ) {
        return items.stream()
                .filter(item -> item != null && island.islandId().equals(item.islandId()))
                .filter(item -> !filtering || matchesSearch(item))
                .toList();
    }

    private void dotAttention(SlotUiElement dot, SlotWorkspaceViewModel.AtlasIsland island) {
        if (dot == null || island == null) {
            return;
        }
        dot.overlayColor(sectionNeedsAttention(island) ? 0x66365743 : null);
    }

    private boolean sectionNeedsAttention(SlotWorkspaceViewModel.AtlasIsland island) {
        if (tree == null || island == null || !tree.isElementOffscreen(island.islandId())) {
            return false;
        }
        boolean searching = !normalizedSearchQuery().isBlank();
        for (SlotWorkspaceViewModel.AtlasItem item : items) {
            if (item == null || !island.islandId().equals(item.islandId())) {
                continue;
            }
            if (hoveredIdentity != null && hoveredIdentity.equals(item.identity())) {
                return true;
            }
            if (searching && matchesSearch(item)) {
                return true;
            }
            if (item.kitNeeded() || item.wanted()) {
                return true;
            }
        }
        return false;
    }

    private SlotUiElement wallViewport(boolean sidebarMode) {
        SlotUiElement viewport = SlotUiElement.panel(0xB810171D)
                .id(ForgeSlotUiTree.PRIMARY_SCROLL_VIEWPORT_ID)
                .attach(ForgeSlotUiTree.SCROLL_VIEWPORT, Boolean.TRUE)
                .layout(layout -> {
                    layout.widthPercent(100)
                            .flex(1)
                            .paddingAll(4)
                            .flexDirection(SlotUiLayout.FlexDirection.COLUMN);
                });
        SlotUiElement content = SlotUiElement.element()
                .layout(layout -> layout
                        .widthPercent(100)
                        .gapAll(4)
                        .flexDirection(SlotUiLayout.FlexDirection.COLUMN));
        WallSectionHeaderUiBuilder headerBuilder = new WallSectionHeaderUiBuilder(new HeaderContext());
        WallSectionUiBuilder sectionBuilder = new WallSectionUiBuilder(headerBuilder);
        boolean filtering = !normalizedSearchQuery().isBlank();
        for (SlotWorkspaceViewModel.AtlasIsland island : islands) {
            List<SlotWorkspaceViewModel.AtlasItem> islandItems = items.stream()
                    .filter(item -> island.islandId().equals(item.islandId()))
                    .toList();
            List<SlotWorkspaceViewModel.AtlasItem> visibleItems = islandItems.stream()
                    .filter(item -> !filtering || matchesSearch(item))
                    .toList();
            if (shouldShowSection(island, visibleItems, filtering)) {
                content.addChild(enrichSection(
                        sectionBuilder.section(
                                island,
                                visibleItems,
                                islandItems.size(),
                                filtering,
                                storageGhostRevealMode,
                                storageGhostSectionExpanded(island.islandId()),
                                false,
                                !activeWorkflowTab())));
            }
        }
        viewport.addChild(content);
        return viewport;
    }

    private SlotUiElement taskPanel() {
        List<SlotUiElement> suggestionRows = contextualSuggestionRows();
        List<SlotUiElement> craftRows = new CraftRunUiBuilder(new CraftRunContext()).panelRows(craftRunItems());
        List<SlotUiElement> rows = taskPanelRows(suggestionRows, craftRows);
        if (rows.isEmpty()) {
            return null;
        }
        int top = SlotForgeClientConfig.taskPanelTopMargin();
        int bottom = SlotForgeClientConfig.taskPanelBottomMargin();
        SlotUiElement panel = SlotUiElement.panel(PANEL)
                .zIndex(20)
                .layout(layout -> layout
                        .positionType(SlotUiLayout.PositionType.ABSOLUTE)
                        .right(SlotForgeClientConfig.taskPanelRightMargin())
                        .top(top)
                        .width(WorkspaceTaskPanelUiBuilder.PANEL_WIDTH_PX)
                        .height(Math.max(1, viewportHeight - top - bottom))
                        .paddingAll(5)
                        .gapAll(4)
                        .flexDirection(SlotUiLayout.FlexDirection.COLUMN))
                .on(SlotUiEventKind.MOUSE_DOWN, event -> event.stopPropagation(), true);
        panel.addChild(SlotUiElement.label(
                        WorkspaceTaskPanelUiBuilder.title(!suggestionRows.isEmpty(), !craftRows.isEmpty()),
                        WorkspaceUiPalette.ACCENT)
                .layout(layout -> layout.widthPercent(100).height(12))
                .textStyle(style -> style
                        .color(WorkspaceUiPalette.ACCENT)
                        .fontSize(8)
                        .horizontal(SlotUiTextStyle.Horizontal.LEFT)
                        .vertical(SlotUiTextStyle.Vertical.CENTER)));
        SlotUiElement content = SlotUiElement.panel(0xA810171D)
                .id("slot.forge.task_panel_scroll")
                .attach(ForgeSlotUiTree.SCROLL_VIEWPORT, Boolean.TRUE)
                .layout(layout -> layout
                        .widthPercent(100)
                        .flex(1)
                        .paddingAll(3)
                        .gapAll(4)
                        .flexDirection(SlotUiLayout.FlexDirection.COLUMN));
        for (SlotUiElement row : rows) {
            content.addChild(enrichSection(row));
        }
        panel.addChild(content);
        return panel;
    }

    private static List<SlotUiElement> taskPanelRows(
            List<SlotUiElement> suggestionRows,
            List<SlotUiElement> craftRows
    ) {
        ArrayList<SlotUiElement> rows = new ArrayList<>();
        rows.addAll(suggestionRows == null ? List.of() : suggestionRows);
        rows.addAll(craftRows == null ? List.of() : craftRows);
        return rows.isEmpty() ? List.of() : List.copyOf(rows);
    }

    private List<SlotWorkspaceViewModel.AtlasItem> craftRunItems() {
        ArrayList<SlotWorkspaceViewModel.AtlasItem> craftItems = new ArrayList<>();
        craftItems.addAll(viewModel.atlasItems());
        craftItems.addAll(viewModel.triageItems());
        return craftItems.isEmpty() ? List.of() : List.copyOf(craftItems);
    }

    private boolean hideFetchLaneForCraftRun(SlotWorkspaceViewModel.ContextualSuggestionLane lane) {
        return lane != null
                && lane.fetch()
                && viewModel != null
                && viewModel.craftRun() != null
                && viewModel.craftRun().active();
    }

    public boolean taskPanelVisible() {
        return contextualSuggestionRowsVisible()
                || (viewModel != null && viewModel.craftRun() != null && viewModel.craftRun().active())
                || (craftRunRecipeCaptures != null && !craftRunRecipeCaptures.isEmpty());
    }

    public TaskPanelBounds taskPanelBounds(int screenWidth, int screenHeight) {
        if (!taskPanelVisible()) {
            return null;
        }
        int width = WorkspaceTaskPanelUiBuilder.PANEL_WIDTH_PX;
        int height = Math.max(1, screenHeight - SlotForgeClientConfig.taskPanelTopMargin()
                - SlotForgeClientConfig.taskPanelBottomMargin());
        return new TaskPanelBounds(
                Math.max(0, screenWidth - SlotForgeClientConfig.taskPanelRightMargin() - width),
                SlotForgeClientConfig.taskPanelTopMargin(),
                width,
                height);
    }

    public record TaskPanelBounds(int x, int y, int width, int height) {
    }

    public RecentsPanelBounds recentsPanelBounds(int screenWidth) {
        return new RecentsPanelBounds(
                RecentsStripUiBuilder.floatingLeft(screenWidth, SlotForgeClientConfig.recentsHorizontalOffset()),
                RecentsStripUiBuilder.floatingTop(SlotForgeClientConfig.recentsTopOffset()),
                RecentsStripUiBuilder.STRIP_WIDTH_PX,
                RecentsStripUiBuilder.STRIP_HEIGHT_PX);
    }

    public record RecentsPanelBounds(int x, int y, int width, int height) {
    }

    public boolean hasActiveOverlay() {
        return helpPopoverOpen
                || editingDesiredCountIdentity != null
                || editingIslandId != null
                || contextMenuKitId != null
                || contextMenuChestStorageId != null
                || contextMenuIslandId != null
                || contextMenuIdentity != null;
    }

    private SlotWorkspaceViewModel.ContextualSuggestionLane visibleSuggestionLane(
            SlotWorkspaceViewModel.ContextualSuggestionLane lane,
            boolean filtering
    ) {
        if (lane == null) {
            return new SlotWorkspaceViewModel.ContextualSuggestionLane("", "", List.of());
        }
        if (!filtering) {
            return lane;
        }
        if (lane.items().isEmpty()) {
            return new SlotWorkspaceViewModel.ContextualSuggestionLane(
                    lane.id(), lane.label(), List.of(), lane.placeholderText(), lane.debugInfo());
        }
        List<SlotWorkspaceViewModel.AtlasItem> visible = lane.items().stream()
                .filter(this::matchesSearch)
                .toList();
        return new SlotWorkspaceViewModel.ContextualSuggestionLane(
                lane.id(), lane.label(), visible, lane.placeholderText(), lane.debugInfo());
    }

    private boolean shouldShowSection(
            SlotWorkspaceViewModel.AtlasIsland island,
            List<SlotWorkspaceViewModel.AtlasItem> visibleItems,
            boolean filtering
    ) {
        return WallSectionVisibility.classify(
                visibleItems,
                filtering,
                storageGhostSectionExpanded(island.islandId()),
                storageGhostRevealMode,
                false,
                !activeWorkflowTab()).hasVisibleContent();
    }

    private boolean activeWorkflowTab() {
        return viewModel.activeKit() != null;
    }

    private void enrichRecentCards(SlotUiElement root) {
        if (root == null) {
            return;
        }
        if (root.hasAttachment(WorkspaceUiAttachments.RECENTS_CARD)) {
            SlotWorkspaceViewModel.AtlasItem item = root.attachment(
                    WorkspaceUiAttachments.ATLAS_ITEM,
                    SlotWorkspaceViewModel.AtlasItem.class);
            if (item != null) {
                enrichCard(root, item);
            }
        }
        for (SlotUiElement child : root.children()) {
            enrichRecentCards(child);
        }
    }

    private SlotUiElement hotbar(boolean sidebarMode) {
        SlotUiElement belt = new HotbarBeltUiBuilder(new HotbarContext()).belt(hotbarSlots, offhand);
        if (!sidebarMode) {
            return SlotUiElement.element()
                    .layout(layout -> layout.width(workspaceWidth()))
                    .addChild(belt);
        }
        return belt;
    }

    private SlotUiElement statusRow(boolean sidebarMode) {
        statusLabel = SlotUiElement.label(status, WorkspaceUiPalette.MUTED)
                .layout(layout -> layout.flex(1).heightPercent(100))
                .textStyle(style -> style
                        .color(WorkspaceUiPalette.MUTED)
                        .fontSize(6)
                        .horizontal(SlotUiTextStyle.Horizontal.LEFT)
                        .vertical(SlotUiTextStyle.Vertical.CENTER));
        return SlotUiElement.panel(PANEL)
                .layout(layout -> {
                    if (sidebarMode) {
                        layout.widthPercent(100).height(12).paddingHorizontal(4);
                    } else {
                        layout.width(workspaceWidth()).height(12).paddingHorizontal(4);
                    }
                    layout.alignItems(SlotUiLayout.AlignItems.CENTER)
                            .flexDirection(SlotUiLayout.FlexDirection.ROW);
                })
                .addChild(statusLabel);
    }

    private SlotUiElement activeOverlay() {
        if (helpPopoverOpen) {
            return helpOverlay();
        }
        if (editingDesiredCountIdentity != null) {
            return desiredCountOverlay();
        }
        if (editingIslandId != null) {
            return islandEditOverlay();
        }
        if (contextMenuKitId != null) {
            return kitContextOverlay();
        }
        if (contextMenuChestStorageId != null) {
            return chestContextOverlay();
        }
        if (contextMenuIslandId != null) {
            return islandOrderOverlay();
        }
        if (contextMenuIdentity != null) {
            return itemContextOverlay();
        }
        return null;
    }

    private SlotUiElement overlayRoot() {
        return SlotUiElement.panel(0x33000000)
                .zIndex(500)
                .layout(layout -> layout
                        .positionType(SlotUiLayout.PositionType.ABSOLUTE)
                        .left(0)
                        .top(0)
                        .widthPercent(100)
                        .heightPercent(100))
                .on(SlotUiEventKind.MOUSE_DOWN, event -> {
                    event.stopPropagation();
                    closeOverlays();
                });
    }

    private SlotUiElement overlayPanel(float screenX, float screenY, float width) {
        return overlayPanel(screenX, screenY, width, menuHeight(6f, 4f, 1, 5), 6f, 6f, 4f, true);
    }

    private SlotUiElement overlayPanel(float screenX, float screenY, float width, float approximateHeight) {
        return overlayPanel(screenX, screenY, width, approximateHeight, 6f, 6f, 4f, true);
    }

    private SlotUiElement overlayPanel(
            float screenX,
            float screenY,
            float width,
            float approximateHeight,
            float horizontalPadding,
            float verticalPadding,
            float gap,
            boolean clampToWorkspace
    ) {
        float minX = POPOVER_SCREEN_MARGIN;
        float maxX = Math.max(POPOVER_SCREEN_MARGIN, viewportWidth - width - POPOVER_SCREEN_MARGIN);
        float x = Math.max(minX, screenX);
        if (mode == Mode.SIDEBAR && clampToWorkspace) {
            minX = SlotForgeClientConfig.sidebarLeftMargin() + POPOVER_SCREEN_MARGIN;
            maxX = Math.max(minX,
                    SlotForgeClientConfig.sidebarLeftMargin() + workspaceWidth() - width - POPOVER_SCREEN_MARGIN);
            x = Math.max(minX, screenX);
        }
        float panelX = Math.min(x, maxX);
        float panelY = anchoredPopoverY(screenY, approximateHeight);
        return SlotUiElement.panel(0xF00B1117)
                .zIndex(501)
                .layout(layout -> layout
                        .positionType(SlotUiLayout.PositionType.ABSOLUTE)
                        .left(panelX)
                        .top(panelY)
                        .width(width)
                        .paddingHorizontal(horizontalPadding)
                        .paddingVertical(verticalPadding)
                        .gapAll(gap)
                        .flexDirection(SlotUiLayout.FlexDirection.COLUMN))
                .on(SlotUiEventKind.MOUSE_DOWN, event -> event.stopPropagation(), true);
    }

    private float anchoredPopoverY(float screenY, float approximateHeight) {
        float y = screenY - POPOVER_SCREEN_MARGIN;
        float availableHeight = Math.max(1f, viewportHeight - popoverBottomInset());
        float maxY = availableHeight - Math.max(0f, approximateHeight) - POPOVER_SCREEN_MARGIN;
        return Math.max(POPOVER_TOP_MARGIN, Math.min(y, Math.max(POPOVER_TOP_MARGIN, maxY)));
    }

    private float popoverBottomInset() {
        return mode == Mode.SIDEBAR ? SlotForgeClientConfig.sidebarBottomMargin() : 0f;
    }

    private static float menuHeight(float verticalPadding, float gap, int labels, int buttons) {
        return menuHeight(verticalPadding, gap, labels, buttons, 0);
    }

    private static float menuHeight(float verticalPadding, float gap, int labels, int buttons, int customRows) {
        int rows = Math.max(0, labels) + Math.max(0, buttons) + Math.max(0, customRows);
        if (rows == 0) {
            return verticalPadding * 2f;
        }
        return verticalPadding * 2f
                + Math.max(0, labels) * MENU_LABEL_HEIGHT
                + Math.max(0, buttons) * MENU_BUTTON_HEIGHT
                + Math.max(0, customRows) * MENU_CUSTOM_ROW_HEIGHT
                + (rows - 1) * gap;
    }

    private SlotUiElement helpOverlay() {
        SlotUiElement overlay = overlayRoot();
        SlotUiElement panel = overlayPanel(
                helpPopoverX,
                helpPopoverY,
                WorkspaceHelpContent.POPOVER_WIDTH_PX,
                WorkspaceHelpContent.POPOVER_HEIGHT_PX,
                6f,
                6f,
                2f,
                false);
        panel.layout(layout -> layout.height(WorkspaceHelpContent.POPOVER_HEIGHT_PX));
        panel.addChild(helpTitle("SLOT basics"));
        SlotUiElement content = helpContentScroller();
        content.addChild(helpSection("Mouse"));
        for (WorkspaceHelpContent.Line line : WorkspaceHelpContent.gestures()) {
            content.addChild(helpLine(line));
        }
        content.addChild(helpSection("Keyboard"));
        for (WorkspaceHelpContent.Line line : WorkspaceHelpContent.keys()) {
            content.addChild(helpLine(line));
        }
        content.addChild(helpSection("Cards / ghosts"));
        for (WorkspaceHelpContent.Line line : WorkspaceHelpContent.ghosts()) {
            content.addChild(helpLine(line));
        }
        content.addChild(helpSection("Markers"));
        for (WorkspaceHelpContent.Line line : WorkspaceHelpContent.markers()) {
            content.addChild(helpLine(line));
        }
        content.addChild(helpSection("Chest roles"));
        for (WorkspaceHelpContent.Line line : WorkspaceHelpContent.storageRoles()) {
            content.addChild(helpLine(line));
        }
        content.addChild(helpSection("Terms"));
        for (WorkspaceHelpContent.Line line : WorkspaceHelpContent.terms()) {
            content.addChild(helpLine(line));
        }
        panel.addChild(content);
        overlay.addChild(panel);
        return overlay;
    }

    private SlotUiElement helpContentScroller() {
        return SlotUiElement.panel(0x00000000)
                .id("slot.forge.help_scroll")
                .attach(ForgeSlotUiTree.SCROLL_VIEWPORT, Boolean.TRUE)
                .layout(layout -> layout
                        .widthPercent(100)
                        .flex(1)
                        .gapAll(2)
                        .flexDirection(SlotUiLayout.FlexDirection.COLUMN));
    }

    private SlotUiElement helpTitle(String text) {
        return SlotUiElement.label(text, WorkspaceUiPalette.ACCENT)
                .layout(layout -> layout.widthPercent(100).height(12))
                .textStyle(style -> style
                        .color(WorkspaceUiPalette.ACCENT)
                        .fontSize(8)
                        .horizontal(SlotUiTextStyle.Horizontal.LEFT)
                        .vertical(SlotUiTextStyle.Vertical.CENTER));
    }

    private SlotUiElement helpSection(String text) {
        return SlotUiElement.label(text, WorkspaceUiPalette.MUTED)
                .layout(layout -> layout.widthPercent(100).height(9))
                .textStyle(style -> style
                        .color(WorkspaceUiPalette.MUTED)
                        .fontSize(6)
                        .horizontal(SlotUiTextStyle.Horizontal.LEFT)
                        .vertical(SlotUiTextStyle.Vertical.CENTER));
    }

    private SlotUiElement helpLine(WorkspaceHelpContent.Line line) {
        SlotUiElement row = SlotUiElement.element()
                .layout(layout -> layout
                        .widthPercent(100)
                        .height(line.heightPx())
                        .paddingVertical(WorkspaceHelpContent.HELP_ROW_PADDING_VERTICAL_PX)
                        .gapAll(WorkspaceHelpContent.HELP_ROW_GAP_PX)
                        .flexDirection(SlotUiLayout.FlexDirection.COLUMN));
        row.addChild(SlotUiElement.label(line.key(), WorkspaceUiPalette.ACCENT)
                .layout(layout -> layout
                        .widthPercent(100)
                        .height(WorkspaceHelpContent.HELP_LABEL_HEIGHT_PX))
                .textStyle(style -> style
                        .color(WorkspaceUiPalette.ACCENT)
                        .fontSize(6.5f)
                        .horizontal(SlotUiTextStyle.Horizontal.LEFT)
                        .vertical(SlotUiTextStyle.Vertical.CENTER)));
        for (String description : line.descriptions()) {
            row.addChild(SlotUiElement.label(description, WorkspaceUiPalette.TEXT)
                    .layout(layout -> layout
                            .widthPercent(100)
                            .height(WorkspaceHelpContent.HELP_DESCRIPTION_HEIGHT_PX))
                    .textStyle(style -> style
                            .color(WorkspaceUiPalette.TEXT)
                            .fontSize(5.5f)
                            .horizontal(SlotUiTextStyle.Horizontal.LEFT)
                            .vertical(SlotUiTextStyle.Vertical.CENTER)));
        }
        return row;
    }

    private SlotUiElement itemContextOverlay() {
        SlotWorkspaceViewModel.AtlasItem liveItem = byIdentity.get(contextMenuIdentity);
        SlotWorkspaceViewModel.AtlasItem item = liveItem != null
                ? liveItem
                : contextMenuItemSnapshot != null && contextMenuIdentity.equals(contextMenuItemSnapshot.identity())
                ? contextMenuItemSnapshot
                : null;
        if (item == null) {
            contextMenuIdentity = null;
            contextMenuItemSnapshot = null;
            return null;
        }
        boolean syntheticCraftRunItem = liveItem == null
                && CraftRunIngredientChoiceRef.forItem(viewModel.craftRun(), item) != null;
        if (recipeSidebarActive() || syntheticCraftRunItem) {
            return recipeItemContextOverlay(item);
        }
        SlotWorkspaceViewModel.KitCard activeTab = viewModel.activeKit();
        List<WorkflowAcceptedInputRule> acceptedInputRules = activeTab == null
                ? List.of()
                : acceptedInputOptions(item);
        List<SlotWorkspaceViewModel.AtlasIsland> rehomeTargets = rehomeMenuTargets(item);
        String activeAffinityStorageId = activeChestAffinityStorageId(item);
        float menuWidth = itemContextMenuWidth(item, activeTab, acceptedInputRules);
        SlotUiElement overlay = overlayRoot();
        SlotUiElement panel = overlayPanel(
                contextMenuX,
                contextMenuY,
                menuWidth,
                itemContextMenuHeight(item, activeTab, acceptedInputRules,
                        rehomeTargets.size(), !activeAffinityStorageId.isBlank()),
                2f,
                3f,
                3f,
                false);
        panel.addChild(menuLabel(
                shorten(item.name(), Math.max(30, Math.round(menuWidth) / ITEM_CONTEXT_MENU_TEXT_PX_PER_CHAR)),
                WorkspaceUiPalette.TEXT));
        if (activeTab != null) {
            boolean member = activeTab.hasMember(item.identity());
            panel.addChild(menuButton(
                    member
                            ? "Remove from " + shorten(activeTab.name(), 16)
                            : "Add to " + shorten(activeTab.name(), 20),
                    true,
                    "Update active workflow membership",
                    closeThen(() -> sendKitAction(
                            WorkspaceActionId.SET_KIT_MEMBER,
                            member ? "removing from workflow" : "adding to workflow",
                            activeTab.kitId(),
                            item.identity().itemId(),
                            item.identity().comparisonMode(),
                            item.identity().componentFingerprint(),
                            member ? 0 : 1))));
            for (WorkflowAcceptedInputRule rule : acceptedInputRules) {
                WorkflowAcceptedInputRule matchedRule = acceptedInputMatch(activeTab, rule);
                boolean accepted = matchedRule != null;
                WorkflowAcceptedInputRule commandRule = accepted ? matchedRule : rule;
                SlotWorkspaceViewModel.IdentityRef identity = commandRule.identity() == null
                        ? new SlotWorkspaceViewModel.IdentityRef("", "", "")
                        : SlotWorkspaceViewModel.IdentityRef.from(commandRule.identity());
                panel.addChild(menuButton(
                        acceptedInputButtonLabel(rule, accepted),
                        true,
                        accepted ? "Stop accepting this workflow input" : "Accept this workflow input",
                        closeThen(() -> sendKitAction(
                                WorkspaceActionId.SET_KIT_ACCEPTED_INPUT,
                                accepted ? "removing accepted workflow input" : "accepting workflow input",
                                activeTab.kitId(),
                                commandRule.kind().name(),
                                identity.itemId(),
                                identity.comparisonMode(),
                                identity.componentFingerprint(),
                                commandRule.tagId(),
                                accepted ? 0 : 1))));
            }
        }
        if (item.ghost()) {
            panel.addChild(menuButton(
                    "Take stack",
                    item.proximateCount() > 0,
                    "Take this stack from a nearby chest",
                    closeThen(() -> sendIdentityRefAction(
                            WorkspaceActionId.TAKE_STACK_BY_IDENTITY,
                            item.identity(),
                            "taking " + item.name()))));
        }
        panel.addChild(menuButton(
                "Put in hotbar",
                item.carried(),
                "Move this carried item to a free hotbar slot",
                closeThen(() -> sendIdentityRefAction(
                        WorkspaceActionId.ASSIGN_HOME_TO_HOTBAR_ONLY,
                        item.identity(),
                        "moving to hotbar"))));
        panel.addChild(menuButton(
                "Deposit to chest",
                item.carried() && anyChestProximate(),
                "Deposit this item into a nearby chest",
                closeThen(() -> sendIdentityRefAction(
                        WorkspaceActionId.DEPOSIT_HOME_TO_LINKED_CHEST,
                        item.identity(),
                        "depositing " + item.name()))));
        if (!activeAffinityStorageId.isBlank()) {
            panel.addChild(menuButton(
                    "Don't auto-deposit here",
                    true,
                    "Stop quick-store from routing this item to the open chest",
                    closeThen(() -> sendAction(
                            WorkspaceActionId.FORGET_ITEM_AFFINITY,
                            "forgetting chest route",
                            activeAffinityStorageId,
                            item.identity().itemId(),
                            item.identity().comparisonMode(),
                            item.identity().componentFingerprint()))));
        }
        panel.addChild(menuButton(
                item.junk() ? "Unmark junk" : "Mark as junk",
                true,
                item.junk() ? "Stop treating this item as low priority" : "Treat this item as low priority",
                closeThen(() -> sendIdentityRefAction(
                        WorkspaceActionId.SET_JUNK,
                        item.identity(),
                        item.junk() ? "unmarking junk" : "marking junk",
                        item.junk() ? 0 : 1))));
        panel.addChild(menuButton(
                "Trash carried item",
                item.carried(),
                "Delete carried stacks of this item",
                closeThen(() -> sendIdentityRefAction(
                        WorkspaceActionId.TRASH_IDENTITY,
                        item.identity(),
                        "trashing " + item.name()))));
        panel.addChild(menuButton(
                item.desiredCount() > 0 ? "Desired: " + item.desiredCount() : "Set desired count",
                true,
                "Set the player-global desired count for this item",
                () -> beginDesiredCountEdit(item)));
        if (item.desiredCount() > 0 && !item.desiredCountFromKit()) {
            panel.addChild(menuButton(
                    "Clear desired",
                    true,
                    "Clear the player-global desired count",
                    closeThen(() -> sendIdentityRefAction(
                            WorkspaceActionId.SET_PLAYER_DESIRED_COUNT,
                            item.identity(),
                            "clearing desired count",
                            0))));
        }
        panel.addChild(menuButton(
                "New section",
                true,
                "Create a new section for this item",
                closeThen(() -> sendIdentityRefAction(
                        WorkspaceActionId.CREATE_NAMED_ISLAND,
                        item.identity(),
                        "creating section",
                        item.name(),
                        WorkspaceUiPalette.ISLAND_SWATCHES[
                                Math.floorMod(islands.size(), WorkspaceUiPalette.ISLAND_SWATCHES.length)],
                        0,
                        0))));
        panel.addChild(menuLabel("Move home", WorkspaceUiPalette.MUTED));
        int targetCount = 0;
        for (SlotWorkspaceViewModel.AtlasIsland island : rehomeTargets) {
            targetCount++;
            panel.addChild(menuButton(
                    shorten(island.label(), 26),
                    true,
                    "Move this item's home to " + island.label(),
                    closeThen(() -> sendAssignHome(item.identity(), island.islandId(), null, "moving home"))));
        }
        if (targetCount == 0) {
            panel.addChild(menuLabel("No other sections", WorkspaceUiPalette.MUTED));
        }
        overlay.addChild(panel);
        return overlay;
    }

    private static List<WorkflowAcceptedInputRule> acceptedInputOptions(SlotWorkspaceViewModel.AtlasItem item) {
        if (item == null || item.identity() == null) {
            return List.of();
        }
        return WorkflowAcceptedInputOptions.forItem(
                item.identity().toIdentity(),
                ItemStackTags.itemTagIds(item.displayStack()));
    }

    private static String acceptedInputButtonLabel(WorkflowAcceptedInputRule rule, boolean accepted) {
        if (rule.itemTag()) {
            return (accepted ? "Stop accepting " : "Accept ")
                    + shorten(rule.displayLabel(), ACCEPTED_INPUT_IDENTIFIER_MAX_CHARS);
        }
        return accepted ? "Stop accepting exact item" : "Accept exact item";
    }

    private static float itemContextMenuWidth(
            SlotWorkspaceViewModel.AtlasItem item,
            SlotWorkspaceViewModel.KitCard activeTab,
            List<WorkflowAcceptedInputRule> acceptedInputRules
    ) {
        int longest = item == null ? 0 : Math.min(item.name().length(), ACCEPTED_INPUT_IDENTIFIER_MAX_CHARS);
        if (activeTab != null && item != null) {
            boolean member = activeTab.hasMember(item.identity());
            String memberLabel = member
                    ? "Remove from " + shorten(activeTab.name(), 16)
                    : "Add to " + shorten(activeTab.name(), 20);
            longest = Math.max(longest, memberLabel.length());
            for (WorkflowAcceptedInputRule rule : acceptedInputRules) {
                WorkflowAcceptedInputRule matchedRule = acceptedInputMatch(activeTab, rule);
                longest = Math.max(longest, acceptedInputButtonLabel(rule, matchedRule != null).length());
            }
        }
        float desired = ITEM_CONTEXT_MENU_TEXT_MARGIN + longest * ITEM_CONTEXT_MENU_TEXT_PX_PER_CHAR;
        return Math.max(ITEM_CONTEXT_MENU_MIN_WIDTH, Math.min(ITEM_CONTEXT_MENU_MAX_WIDTH, desired));
    }

    private static float itemContextMenuHeight(
            SlotWorkspaceViewModel.AtlasItem item,
            SlotWorkspaceViewModel.KitCard activeTab,
            List<WorkflowAcceptedInputRule> acceptedInputRules,
            int rehomeTargetCount,
            boolean canForgetActiveChestAffinity
    ) {
        int labels = 2 + (rehomeTargetCount <= 0 ? 1 : 0);
        int buttons = 6 + Math.max(0, rehomeTargetCount);
        if (activeTab != null) {
            buttons += 1 + acceptedInputRules.size();
        }
        if (item != null && item.ghost()) {
            buttons++;
        }
        if (item != null && item.desiredCount() > 0 && !item.desiredCountFromKit()) {
            buttons++;
        }
        if (canForgetActiveChestAffinity) {
            buttons++;
        }
        return menuHeight(3f, 3f, labels, buttons);
    }

    private static WorkflowAcceptedInputRule acceptedInputMatch(
            SlotWorkspaceViewModel.KitCard activeTab,
            WorkflowAcceptedInputRule rule
    ) {
        if (activeTab == null || rule == null) {
            return null;
        }
        for (WorkflowAcceptedInputRule existing : activeTab.acceptedInputs()) {
            if (existing == null) {
                continue;
            }
            if (existing.equals(rule) || (existing.exactItem() && rule.exactItem()
                    && existing.matches(rule.identity(), java.util.Set.of()))) {
                return existing;
            }
        }
        return null;
    }

    private String activeChestAffinityStorageId(SlotWorkspaceViewModel.AtlasItem item) {
        if (item == null || item.identity() == null || viewModel == null) {
            return "";
        }
        SlotWorkspaceViewModel.ActiveChestPanel panel = viewModel.activeChestPanel();
        if (panel == null || !panel.isClaimed() || !panel.hasAffinity(item.identity())) {
            return "";
        }
        return panel.storageId();
    }

    private SlotUiElement recipeItemContextOverlay(SlotWorkspaceViewModel.AtlasItem item) {
        CraftRunIngredientChoiceRef choiceRef = CraftRunIngredientChoiceRef.forItem(viewModel.craftRun(), item);
        CraftRunIngredientGroup choiceGroup = choiceRef == null ? null : choiceRef.group(viewModel.craftRun());
        int choiceButtons = craftRunChoiceMenuButtons(choiceGroup);
        int choiceLabels = choiceGroup == null ? 0 : 1
                + (choiceGroup.alternatives().size() > CRAFT_RUN_CHOICE_MENU_MAX_ALTERNATIVES ? 1 : 0);
        String activeAffinityStorageId = activeChestAffinityStorageId(item);
        int activeAffinityButton = activeAffinityStorageId.isBlank() ? 0 : 1;
        SlotUiElement overlay = overlayRoot();
        SlotUiElement panel = overlayPanel(
                contextMenuX,
                contextMenuY,
                174,
                menuHeight(6f, 4f, 1 + choiceLabels,
                        5 + (item.ghost() ? 1 : 0) + choiceButtons + activeAffinityButton));
        panel.addChild(menuLabel(shorten(item.name(), 30), WorkspaceUiPalette.ACCENT));
        appendCraftRunChoiceMenu(panel, choiceRef, choiceGroup);
        if (item.ghost()) {
            panel.addChild(menuButton(
                    "Take stack",
                    item.proximateCount() > 0,
                    "Take this stack from a nearby chest",
                    closeThen(() -> sendIdentityRefAction(
                            WorkspaceActionId.TAKE_STACK_BY_IDENTITY,
                            item.identity(),
                            "taking " + item.name()))));
        }
        panel.addChild(menuButton(
                "Put in hotbar",
                item.carried(),
                "Move this carried item to a free hotbar slot",
                closeThen(() -> sendIdentityRefAction(
                        WorkspaceActionId.ASSIGN_HOME_TO_HOTBAR_ONLY,
                        item.identity(),
                        "moving to hotbar"))));
        panel.addChild(menuButton(
                "Deposit to chest",
                item.carried() && anyChestProximate(),
                "Deposit this item into a nearby chest",
                closeThen(() -> sendIdentityRefAction(
                        WorkspaceActionId.DEPOSIT_HOME_TO_LINKED_CHEST,
                        item.identity(),
                        "depositing " + item.name()))));
        if (!activeAffinityStorageId.isBlank()) {
            panel.addChild(menuButton(
                    "Don't auto-deposit here",
                    true,
                    "Stop quick-store from routing this item to the open chest",
                    closeThen(() -> sendAction(
                            WorkspaceActionId.FORGET_ITEM_AFFINITY,
                            "forgetting chest route",
                            activeAffinityStorageId,
                            item.identity().itemId(),
                            item.identity().comparisonMode(),
                            item.identity().componentFingerprint()))));
        }
        boolean recipeViewerItem = item.identity() != null
                && !CraftRunIngredientChoiceRef.isPlaceholder(item.identity().toIdentity());
        panel.addChild(menuButton(
                "Open recipe in EMI",
                recipeViewerItem,
                recipeViewerItem ? "Delegate recipe details to EMI" : "Choose a concrete ingredient first",
                closeThen(() -> openRecipe(item))));
        panel.addChild(menuButton(
                "Open uses in EMI",
                recipeViewerItem,
                recipeViewerItem ? "Delegate usage details to EMI" : "Choose a concrete ingredient first",
                closeThen(() -> openUses(item))));
        panel.addChild(menuButton("Close", true, "Close", this::closeOverlays));
        overlay.addChild(panel);
        return overlay;
    }

    private static int craftRunChoiceMenuButtons(CraftRunIngredientGroup group) {
        if (group == null || group.alternatives().size() <= 1) {
            return 0;
        }
        return Math.min(CRAFT_RUN_CHOICE_MENU_MAX_ALTERNATIVES, group.alternatives().size());
    }

    private void appendCraftRunChoiceMenu(
            SlotUiElement panel,
            CraftRunIngredientChoiceRef choiceRef,
            CraftRunIngredientGroup group
    ) {
        if (panel == null || choiceRef == null || group == null || group.alternatives().size() <= 1) {
            return;
        }
        panel.addChild(menuLabel("Choose ingredient", WorkspaceUiPalette.MUTED));
        int count = 0;
        for (CraftRunAlternative alternative : group.alternatives()) {
            if (alternative == null || alternative.identity() == null) {
                continue;
            }
            if (count >= CRAFT_RUN_CHOICE_MENU_MAX_ALTERNATIVES) {
                break;
            }
            count++;
            SlotWorkspaceViewModel.IdentityRef identity = SlotWorkspaceViewModel.IdentityRef.from(alternative.identity());
            boolean selected = group.selectedAlternativeIdentity() != null
                    && ItemIdentityMatcher.matchesMovable(group.selectedAlternativeIdentity(), alternative.identity());
            panel.addChild(menuButton(
                    (selected ? "Selected: " : "Use ") + shorten(alternative.label(), 22),
                    true,
                    "Use this variant for this recipe ingredient",
                    closeThen(() -> sendAction(
                            WorkspaceActionId.CRAFT_RUN_SELECT_INGREDIENT,
                            "selected recipe ingredient",
                            choiceRef.entryId(),
                            choiceRef.groupId(),
                            identity.itemId(),
                            identity.comparisonMode(),
                            identity.componentFingerprint()))));
        }
        int hidden = group.alternatives().size() - count;
        if (hidden > 0) {
            panel.addChild(menuLabel("+" + hidden + " more variants in EMI", WorkspaceUiPalette.MUTED));
        }
    }

    private SlotUiElement islandOrderOverlay() {
        SlotWorkspaceViewModel.AtlasIsland island = island(contextMenuIslandId);
        if (island == null || island.kind() != VisualAtlasIslandKind.PLAYER) {
            closeOverlayState();
            return null;
        }
        List<SlotWorkspaceViewModel.AtlasIsland> allSections = playerIslandsInOrder();
        int sourceIndex = islandIndex(allSections, island.islandId());
        if (sourceIndex < 0) {
            closeOverlayState();
            return null;
        }
        List<SlotWorkspaceViewModel.AtlasIsland> visibleSections = tocEntries();
        int visibleIndex = islandIndex(visibleSections, island.islandId());
        SlotWorkspaceViewModel.AtlasIsland previousVisible =
                visibleIndex > 0 ? visibleSections.get(visibleIndex - 1) : null;
        SlotWorkspaceViewModel.AtlasIsland nextVisible =
                visibleIndex >= 0 && visibleIndex + 1 < visibleSections.size()
                        ? visibleSections.get(visibleIndex + 1)
                        : null;
        int lastIndex = Math.max(0, allSections.size() - 1);

        SlotUiElement overlay = overlayRoot();
        SlotUiElement panel = overlayPanel(contextMenuX, contextMenuY, 178, menuHeight(6f, 4f, 1, 5));
        panel.addChild(menuLabel(shorten(island.label(), 30), WorkspaceUiPalette.ACCENT));
        panel.addChild(menuButton(
                previousVisible == null
                        ? "Move above"
                        : "Move above " + shorten(previousVisible.label(), 17),
                previousVisible != null,
                "No visible section above",
                closeThen(() -> sendSectionReorderAround(island, previousVisible, true))));
        panel.addChild(menuButton(
                nextVisible == null
                        ? "Move below"
                        : "Move below " + shorten(nextVisible.label(), 17),
                nextVisible != null,
                "No visible section below",
                closeThen(() -> sendSectionReorderAround(island, nextVisible, false))));
        panel.addChild(menuButton(
                "Move to top",
                sourceIndex > 0,
                "Already at the top",
                closeThen(() -> sendSectionReorderToIndex(island, 0))));
        panel.addChild(menuButton(
                "Move to bottom",
                sourceIndex < lastIndex,
                "Already at the bottom",
                closeThen(() -> sendSectionReorderToIndex(island, lastIndex))));
        panel.addChild(menuButton("Close", true, "Close", this::closeOverlays));
        overlay.addChild(panel);
        return overlay;
    }

    private List<SlotWorkspaceViewModel.AtlasIsland> rehomeMenuTargets(SlotWorkspaceViewModel.AtlasItem item) {
        ArrayList<SlotWorkspaceViewModel.AtlasIsland> result = new ArrayList<>();
        String currentIslandId = item == null ? "" : item.islandId();
        for (String islandId : recentRehomeIslandIds) {
            if (islandId.equals(currentIslandId)) {
                continue;
            }
            SlotWorkspaceViewModel.AtlasIsland island = island(islandId);
            if (island == null || island.kind() != VisualAtlasIslandKind.PLAYER) {
                continue;
            }
            result.add(island);
            if (result.size() >= RECENT_REHOME_MAX_DISPLAYED) {
                break;
            }
        }
        for (SlotWorkspaceViewModel.AtlasIsland island : islands) {
            if (island == null
                    || island.kind() != VisualAtlasIslandKind.PLAYER
                    || island.islandId().equals(currentIslandId)
                    || containsIsland(result, island.islandId())) {
                continue;
            }
            result.add(island);
            if (result.size() >= REHOME_MENU_MAX_DISPLAYED) {
                break;
            }
        }
        return List.copyOf(result);
    }

    private static boolean containsIsland(List<SlotWorkspaceViewModel.AtlasIsland> islands, String islandId) {
        if (islands == null || islandId == null) {
            return false;
        }
        for (SlotWorkspaceViewModel.AtlasIsland island : islands) {
            if (island != null && islandId.equals(island.islandId())) {
                return true;
            }
        }
        return false;
    }

    private SlotUiElement kitContextOverlay() {
        SlotWorkspaceViewModel.KitCard kit = viewModel.kit(contextMenuKitId);
        if (kit == null) {
            closeOverlayState();
            return null;
        }
        SlotUiElement overlay = overlayRoot();
        int labels = kit.kitId().equals(renamingKitId) || kit.kitId().equals(confirmDeleteKitId) ? 2 : 1;
        int buttons = kit.kitId().equals(renamingKitId) || kit.kitId().equals(confirmDeleteKitId)
                ? 2
                : 6 + (kit.variant() ? 0 : 1);
        SlotUiElement panel = overlayPanel(contextMenuX, contextMenuY, 178, menuHeight(6f, 4f, labels, buttons));
        panel.addChild(menuLabel(shorten(kit.name(), 30), WorkspaceUiPalette.ACCENT));
        if (kit.kitId().equals(renamingKitId)) {
            panel.addChild(menuLabel("Name: " + renameKitDraft + "_", WorkspaceUiPalette.TEXT));
            panel.addChild(menuButton("Save", true, "Rename this workflow", this::commitKitRenameEdit));
            panel.addChild(menuButton("Cancel", true, "Close", this::closeOverlays));
        } else if (kit.kitId().equals(confirmDeleteKitId)) {
            panel.addChild(menuLabel("Delete this workflow?", WorkspaceUiPalette.MUTED));
            panel.addChild(menuButton(
                    "Delete",
                    true,
                    "Delete this workflow",
                    closeThen(() -> sendKitAction(WorkspaceActionId.DELETE_KIT, "deleting workflow", kit.kitId()))));
            panel.addChild(menuButton("Cancel", true, "Close", () -> {
                confirmDeleteKitId = null;
                rebuildRequested = true;
            }));
        } else {
            panel.addChild(menuButton("Rename...", true, "Rename this workflow", () -> beginKitRenameEdit(kit)));
            panel.addChild(menuButton(
                    "Duplicate",
                    true,
                    "Duplicate this workflow",
                    closeThen(() -> sendKitAction(WorkspaceActionId.DUPLICATE_KIT, "duplicating workflow", kit.kitId()))));
            int siblingIndex = workflowSiblingIndex(kit);
            int siblingCount = workflowSiblingCount(kit);
            panel.addChild(menuButton(
                    "Move left",
                    siblingIndex > 0,
                    "Already first",
                    closeThen(() -> sendKitReorder(kit, siblingIndex - 1))));
            panel.addChild(menuButton(
                    "Move right",
                    siblingIndex >= 0 && siblingIndex + 1 < siblingCount,
                    "Already last",
                    closeThen(() -> sendKitReorder(kit, siblingIndex + 1))));
            if (!kit.variant()) {
                panel.addChild(menuButton(
                        "Create variant",
                        true,
                        "Create a one-level workflow variant",
                        closeThen(() -> sendKitAction(
                                WorkspaceActionId.CREATE_KIT_VARIANT,
                                "creating workflow variant",
                                kit.kitId(),
                                ""))));
            }
            panel.addChild(menuButton("Delete...", true, "Delete this workflow", () -> {
                confirmDeleteKitId = kit.kitId();
                renamingKitId = null;
                renameKitDraft = "";
                rebuildRequested = true;
            }));
            panel.addChild(menuButton("Cancel", true, "Close", this::closeOverlays));
        }
        overlay.addChild(panel);
        return overlay;
    }

    private void sendKitReorder(SlotWorkspaceViewModel.KitCard kit, int targetIndex) {
        if (kit == null || kit.kitId().isBlank()) {
            setStatus("missing workflow");
            return;
        }
        sendKitAction(
                WorkspaceActionId.REORDER_KIT,
                kit.variant() ? "moving workflow variant" : "moving workflow",
                kit.kitId(),
                Math.max(0, targetIndex));
    }

    private int workflowSiblingIndex(SlotWorkspaceViewModel.KitCard kit) {
        if (kit == null) {
            return -1;
        }
        int index = 0;
        for (SlotWorkspaceViewModel.KitCard candidate : viewModel.kits()) {
            if (!sameWorkflowSiblingGroup(kit, candidate)) {
                continue;
            }
            if (kit.kitId().equals(candidate.kitId())) {
                return index;
            }
            index++;
        }
        return -1;
    }

    private int workflowSiblingCount(SlotWorkspaceViewModel.KitCard kit) {
        if (kit == null) {
            return 0;
        }
        int count = 0;
        for (SlotWorkspaceViewModel.KitCard candidate : viewModel.kits()) {
            if (sameWorkflowSiblingGroup(kit, candidate)) {
                count++;
            }
        }
        return count;
    }

    private static boolean sameWorkflowSiblingGroup(
            SlotWorkspaceViewModel.KitCard left,
            SlotWorkspaceViewModel.KitCard right
    ) {
        if (left == null || right == null) {
            return false;
        }
        if (left.variant() || right.variant()) {
            return left.variant()
                    && right.variant()
                    && left.parentId().equals(right.parentId());
        }
        return !right.variant();
    }

    private SlotUiElement chestContextOverlay() {
        SlotWorkspaceViewModel.ChestChip chip = viewModel.chestChip(contextMenuChestStorageId);
        if (chip == null) {
            closeOverlayState();
            return null;
        }
        SlotUiElement overlay = overlayRoot();
        boolean renaming = chip.storageId().equals(renamingChestStorageId);
        SlotUiElement panel = overlayPanel(
                contextMenuX,
                contextMenuY,
                178,
                menuHeight(6f, 4f, renaming ? 2 : 1, 2));
        String label = chip.label().isBlank() ? "Chest" : chip.label();
        panel.addChild(menuLabel(shorten(label, 30), WorkspaceUiPalette.ACCENT));
        if (renaming) {
            panel.addChild(menuLabel("Name: " + renameChestDraft + "_", WorkspaceUiPalette.TEXT));
            panel.addChild(menuButton("Save", true, "Rename this chest", this::commitChestRenameEdit));
            panel.addChild(menuButton("Cancel", true, "Close", this::closeOverlays));
        } else {
            panel.addChild(menuButton("Rename...", true, "Rename this chest", () -> beginChestRenameEdit(chip)));
            panel.addChild(menuButton("Cancel", true, "Close", this::closeOverlays));
        }
        overlay.addChild(panel);
        return overlay;
    }

    private List<SlotWorkspaceViewModel.AtlasIsland> playerIslandsInOrder() {
        ArrayList<SlotWorkspaceViewModel.AtlasIsland> result = new ArrayList<>();
        for (SlotWorkspaceViewModel.AtlasIsland island : islands) {
            if (island != null && island.kind() == VisualAtlasIslandKind.PLAYER) {
                result.add(island);
            }
        }
        return List.copyOf(result);
    }

    private static int islandIndex(List<SlotWorkspaceViewModel.AtlasIsland> sections, String islandId) {
        if (sections == null || islandId == null || islandId.isBlank()) {
            return -1;
        }
        for (int index = 0; index < sections.size(); index++) {
            SlotWorkspaceViewModel.AtlasIsland candidate = sections.get(index);
            if (candidate != null && islandId.equals(candidate.islandId())) {
                return index;
            }
        }
        return -1;
    }

    private SlotUiElement desiredCountOverlay() {
        SlotWorkspaceViewModel.IdentityRef identity = editingDesiredCountIdentity;
        SlotWorkspaceViewModel.AtlasItem item = byIdentity.get(identity);
        SlotUiElement overlay = overlayRoot();
        SlotUiElement panel = overlayPanel(contextMenuX, contextMenuY, 170, menuHeight(6f, 4f, 3, 3));
        panel.addChild(menuLabel("Desired count", WorkspaceUiPalette.ACCENT));
        panel.addChild(menuLabel(shorten(item == null ? identity.itemId() : item.name(), 30), WorkspaceUiPalette.TEXT));
        panel.addChild(menuLabel("Count: " + (desiredCountDraft.isBlank() ? "0" : desiredCountDraft) + "_",
                WorkspaceUiPalette.TEXT));
        panel.addChild(menuButton("Save", true, "Save desired count", this::commitDesiredCountEdit));
        panel.addChild(menuButton("Clear", true, "Clear desired count", () -> {
            desiredCountDraft = "0";
            commitDesiredCountEdit();
        }));
        panel.addChild(menuButton("Cancel", true, "Close", this::closeOverlays));
        overlay.addChild(panel);
        return overlay;
    }

    private SlotUiElement islandEditOverlay() {
        SlotWorkspaceViewModel.AtlasIsland island = island(editingIslandId);
        if (island == null) {
            editingIslandId = null;
            return null;
        }
        SlotUiElement overlay = overlayRoot();
        SlotUiElement panel = overlayPanel(islandEditX, islandEditY, 178, menuHeight(6f, 4f, 2, 5, 1));
        panel.addChild(menuLabel("Edit section", WorkspaceUiPalette.ACCENT));
        panel.addChild(menuLabel("Name: " + islandLabelDraft + "_", WorkspaceUiPalette.TEXT));
        panel.addChild(menuButton("Save name", true, "Rename this section", this::commitIslandNameEdit));

        SlotUiElement swatches = SlotUiElement.element()
                .layout(layout -> layout
                        .widthPercent(100)
                        .height(14)
                        .gapAll(3)
                        .flexDirection(SlotUiLayout.FlexDirection.ROW));
        for (int color : WorkspaceUiPalette.ISLAND_SWATCHES) {
            swatches.addChild(SlotUiElement.button("", true, color)
                    .noText()
                    .tooltip(Component.literal("Set section color"))
                    .layout(layout -> layout.flex(1).height(12))
                    .on(SlotUiEventKind.CLICK, event -> {
                        if (event.button() != 0) {
                            return;
                        }
                        event.stopPropagation();
                        closeOverlayState();
                        sendAction(WorkspaceActionId.RECOLOR_ISLAND, "recoloring section", island.islandId(), color);
                    }));
        }
        panel.addChild(swatches);

        boolean hasHoveredItem = hoveredIdentity != null && byIdentity.containsKey(hoveredIdentity);
        panel.addChild(menuButton(
                "Use hovered icon",
                hasHoveredItem,
                "Set the section icon to the currently hovered item",
                closeThen(() -> {
                    SlotWorkspaceViewModel.IdentityRef icon = hoveredIdentity;
                    sendAction(
                            WorkspaceActionId.SET_ISLAND_ICON,
                            "setting section icon",
                            island.islandId(),
                            icon.itemId(),
                            icon.comparisonMode(),
                            icon.componentFingerprint());
                })));
        panel.addChild(menuButton(
                "Clear icon",
                true,
                "Clear the section icon",
                closeThen(() -> sendAction(WorkspaceActionId.SET_ISLAND_ICON, "clearing section icon",
                        island.islandId(), "", "", ""))));
        panel.addChild(menuButton(
                "Delete section",
                island.itemCount() == 0,
                island.itemCount() == 0 ? "Delete this empty section" : "Move items out before deleting",
                closeThen(() -> sendAction(WorkspaceActionId.DELETE_ISLAND, "deleting section", island.islandId()))));
        panel.addChild(menuButton("Close", true, "Close", this::closeOverlays));
        overlay.addChild(panel);
        return overlay;
    }

    private SlotUiElement menuLabel(String text, int color) {
        return SlotUiElement.label(text, color)
                .layout(layout -> layout.widthPercent(100).height(11))
                .textStyle(style -> style
                        .color(color)
                        .fontSize(7)
                        .horizontal(SlotUiTextStyle.Horizontal.LEFT)
                        .vertical(SlotUiTextStyle.Vertical.CENTER));
    }

    private SlotUiElement menuButton(String text, boolean enabled, String inactiveStatus, Runnable action) {
        return SlotUiElement.button(text, enabled, enabled ? WorkspaceUiPalette.ROW_HOVER : WorkspaceUiPalette.ROW_DIM)
                .tooltip(Component.literal(inactiveStatus == null ? "" : inactiveStatus))
                .layout(layout -> layout.widthPercent(100).height(14))
                .textStyle(style -> style
                        .color(enabled ? WorkspaceUiPalette.TEXT : WorkspaceUiPalette.MUTED)
                        .fontSize(7)
                        .horizontal(SlotUiTextStyle.Horizontal.LEFT)
                        .vertical(SlotUiTextStyle.Vertical.CENTER))
                .on(SlotUiEventKind.CLICK, event -> {
                    if (event.button() != 0) {
                        return;
                    }
                    event.stopPropagation();
                    if (!enabled) {
                        setStatus(inactiveStatus);
                        return;
                    }
                    if (action != null) {
                        action.run();
                    }
                });
    }

    private Runnable closeThen(Runnable action) {
        return () -> {
            closeOverlayState();
            if (action != null) {
                action.run();
            }
        };
    }

    private SlotUiElement enrichSection(SlotUiElement section) {
        SlotUiElement header = null;
        ArrayList<SlotUiElement> grids = new ArrayList<>();
        ArrayList<SlotUiElement> craftRunGrids = new ArrayList<>();
        ArrayList<SlotUiElement> suggestionGrids = new ArrayList<>();
        SlotWorkspaceViewModel.AtlasIsland island = null;
        for (SlotUiElement child : section.children()) {
            if (child.hasAttachment(WorkspaceUiAttachments.WALL_SECTION_HEADER)) {
                header = child;
                island = child.attachment(WorkspaceUiAttachments.ATLAS_ISLAND, SlotWorkspaceViewModel.AtlasIsland.class);
            }
            if (child.hasAttachment(WorkspaceUiAttachments.WALL_SECTION_GRID)) {
                grids.add(child);
                if (island == null) {
                    island = child.attachment(
                            WorkspaceUiAttachments.ATLAS_ISLAND,
                            SlotWorkspaceViewModel.AtlasIsland.class);
                }
            }
            if (child.hasAttachment(WorkspaceUiAttachments.WALL_SUGGESTION_GRID)) {
                suggestionGrids.add(child);
            }
            if (child.hasAttachment(WorkspaceUiAttachments.WALL_CRAFT_RUN_GRID)) {
                craftRunGrids.add(child);
            }
        }
        installSectionHomeTarget(header, island);
        if (grids.isEmpty() && suggestionGrids.isEmpty() && craftRunGrids.isEmpty()) {
            return section;
        }
        WallCardUiBuilder cardBuilder = new WallCardUiBuilder(new CardContext());
        for (SlotUiElement grid : craftRunGrids) {
            List<?> gridItems = grid.attachment(WorkspaceUiAttachments.ATLAS_ITEMS, List.class);
            if (gridItems != null) {
                for (Object gridItem : gridItems) {
                    if (gridItem instanceof SlotWorkspaceViewModel.AtlasItem item) {
                        grid.addChild(enrichCard(cardBuilder.card(item), item));
                    }
                }
            }
        }
        for (SlotUiElement grid : suggestionGrids) {
            SlotWorkspaceViewModel.ContextualSuggestionLane lane = grid.attachment(
                    WorkspaceUiAttachments.CONTEXTUAL_SUGGESTION_LANE,
                    SlotWorkspaceViewModel.ContextualSuggestionLane.class);
            WallCardUiBuilder suggestionCardBuilder = lane != null && lane.forceWayfindingStrip()
                    ? new WallCardUiBuilder(new CardContext(true, lane))
                    : new WallCardUiBuilder(new CardContext(false, lane));
            List<?> gridItems = grid.attachment(WorkspaceUiAttachments.ATLAS_ITEMS, List.class);
            if (gridItems != null) {
                for (Object gridItem : gridItems) {
                    if (gridItem instanceof SlotWorkspaceViewModel.AtlasItem item) {
                        grid.addChild(enrichCard(suggestionCardBuilder.card(item), item));
                    }
                }
            }
        }
        for (SlotUiElement grid : grids) {
            SlotWorkspaceViewModel.AtlasIsland gridIsland = grid.attachment(
                    WorkspaceUiAttachments.ATLAS_ISLAND,
                    SlotWorkspaceViewModel.AtlasIsland.class);
            installSectionHomeTarget(grid, gridIsland == null ? island : gridIsland);
            List<?> gridItems = grid.attachment(WorkspaceUiAttachments.ATLAS_ITEMS, List.class);
            if (gridItems != null) {
                for (Object gridItem : gridItems) {
                    if (gridItem instanceof SlotWorkspaceViewModel.AtlasItem item) {
                        grid.addChild(enrichCard(cardBuilder.card(item), item));
                    }
                }
            }
        }
        return section;
    }

    private SlotUiElement enrichCard(SlotUiElement card, SlotWorkspaceViewModel.AtlasItem item) {
        card.on(SlotUiEventKind.MOUSE_DOWN, event -> {
            if (event.button() == 0) {
                beginHomeDragCandidate(freshItem(item));
                event.stopPropagation();
                return;
            }
            SlotWorkspaceViewModel.AtlasItem target = freshItem(item);
            if (event.button() == 1 && !event.shiftDown() && !isCursorCarrying()) {
                event.stopPropagation();
                openItemContextMenu(target, event.x(), event.y());
                return;
            }
            WallCardTransferGesturePolicy.Decision decision = WallCardTransferGesturePolicy.pointerDown(
                    cardGestureContext(target, event.button(), event.shiftDown()));
            if (dispatchCardGestureDecision(target, decision)) {
                event.stopPropagation();
                return;
            }
            if (event.button() == 1 && !isCursorCarrying()) {
                event.stopPropagation();
                openItemContextMenu(target, event.x(), event.y());
            }
        });
        card.on(SlotUiEventKind.CLICK, event -> {
            if (event.button() != 0) {
                return;
            }
            event.stopPropagation();
            SlotWorkspaceViewModel.AtlasItem target = freshItem(item);
            WallCardTransferGesturePolicy.Decision decision = WallCardTransferGesturePolicy.click(
                    cardGestureContext(target, event.button(), event.shiftDown()));
            dispatchCardGestureDecision(target, decision);
        });
        card.on(SlotUiEventKind.MOUSE_WHEEL, event -> {
            boolean controlDown = event.controlDown() || Screen.hasControlDown();
            boolean wantedAdjustDown = ForgeWorkspaceClient.markWantedDown();
            if (!event.shiftDown() && !controlDown && !wantedAdjustDown) {
                return;
            }
            if (isCursorCarrying()) {
                return;
            }
            float delta = event.wheelDelta();
            if (delta == 0f) {
                return;
            }
            event.stopPropagation();
            SlotWorkspaceViewModel.AtlasItem target =
                    wheelTarget(item, delta, event.shiftDown(), controlDown, wantedAdjustDown);
            int steps = wheelSteps(target == null ? null : target.identity(), delta, controlDown || wantedAdjustDown);
            if (steps == 0) {
                return;
            }
            if (recipeSidebarActive() && wantedAdjustDown) {
                sendIdentityRefAction(
                        WorkspaceActionId.SET_WANTED_COUNT,
                        target.identity(),
                        "wanted count updated",
                        recipeWantedTargetCount(target, steps));
                return;
            }
            WallCardTransferGesturePolicy.Decision decision = WallCardTransferGesturePolicy.wheel(
                    cardGestureContext(target, 0, event.shiftDown(), controlDown, wantedAdjustDown),
                    steps);
            dispatchCardGestureDecision(target, decision);
        });
        return card;
    }

    private void installSectionHomeTarget(
            SlotUiElement target,
            SlotWorkspaceViewModel.AtlasIsland island
    ) {
        if (target == null || island == null || island.kind() == VisualAtlasIslandKind.TRIAGE) {
            return;
        }
        target.on(SlotUiEventKind.MOUSE_DOWN, event -> {
            if (event.propagationStopped() || event.button() != 0 || !isCursorCarrying()) {
                return;
            }
            event.stopPropagation();
            assignCursorHomeToSection(island);
        });
    }

    private SlotWorkspaceViewModel.AtlasItem freshItem(SlotWorkspaceViewModel.AtlasItem item) {
        if (item == null || viewModel == null) {
            return item;
        }
        SlotWorkspaceViewModel.AtlasItem fresh = byIdentity.get(item.identity());
        return fresh == null ? item : fresh;
    }

    private SlotWorkspaceViewModel.AtlasItem wheelTarget(
            SlotWorkspaceViewModel.AtlasItem item,
            float delta,
            boolean shiftDown,
            boolean controlDown,
            boolean wantedAdjustDown
    ) {
        SlotWorkspaceViewModel.AtlasItem target = freshItem(item);
        if (delta <= 0f || !shiftDown || controlDown || wantedAdjustDown) {
            return target;
        }
        SlotWorkspaceViewModel.IdentityRef locked = shiftClickTransferState.takeIdentity(true);
        if (locked == null) {
            return target;
        }
        SlotWorkspaceViewModel.AtlasItem freshLocked = byIdentity.get(locked);
        return freshLocked == null ? target : freshLocked;
    }

    private void beginHomeDragCandidate(SlotWorkspaceViewModel.AtlasItem item) {
        if (item == null || item.identity() == null || isCursorCarrying()) {
            pendingHomeDragIdentity = null;
            pendingHomeDragOriginIslandId = null;
            return;
        }
        pendingHomeDragIdentity = item.identity();
        pendingHomeDragOriginIslandId = item.islandId();
    }

    private boolean completePendingHomeDrag(SlotWorkspaceViewModel.AtlasIsland targetIsland) {
        SlotWorkspaceViewModel.IdentityRef identity = pendingHomeDragIdentity;
        String originIslandId = pendingHomeDragOriginIslandId;
        pendingHomeDragIdentity = null;
        pendingHomeDragOriginIslandId = null;
        if (identity == null || targetIsland == null || targetIsland.kind() == VisualAtlasIslandKind.TRIAGE) {
            return false;
        }
        if (targetIsland.islandId().equals(originIslandId)) {
            return false;
        }
        return sendAssignHome(identity, targetIsland.islandId(), null, "moving home");
    }

    private boolean assignCursorHomeToSection(SlotWorkspaceViewModel.AtlasIsland island) {
        SlotWorkspaceViewModel.IdentityRef identity = cursorIdentity();
        if (identity == null || island == null || island.kind() == VisualAtlasIslandKind.TRIAGE) {
            return false;
        }
        return sendAssignHome(identity, island.islandId(), null, "moving cursor home");
    }

    private SlotWorkspaceViewModel.AtlasIsland sectionAt(double mouseX, double mouseY) {
        return tree == null
                ? null
                : tree.attachmentAt(
                        mouseX,
                        mouseY,
                        WorkspaceUiAttachments.ATLAS_ISLAND,
                        SlotWorkspaceViewModel.AtlasIsland.class);
    }

    private String normalizedSearchQuery() {
        if (recipeSidebarActive()) {
            return "";
        }
        return WorkspaceSearchQuery.normalized(searchQuery);
    }

    private boolean matchesSearch(SlotWorkspaceViewModel.AtlasItem item) {
        if (recipeSidebarActive()) {
            return true;
        }
        return WorkspaceSearchQuery.matchesItem(
                searchQuery,
                item,
                item == null ? null : island(item.islandId()));
    }

    private boolean anyGatherableIdentity() {
        for (SlotWorkspaceViewModel.AtlasItem item : items) {
            if (WorkspaceGatherUiSupport.isGatherableItem(item)) {
                return true;
            }
        }
        return false;
    }

    private SlotWorkspaceViewModel.AtlasIsland island(String islandId) {
        for (SlotWorkspaceViewModel.AtlasIsland island : islands) {
            if (island.islandId().equals(islandId)) {
                return island;
            }
        }
        return null;
    }

    private void openItemContextMenu(SlotWorkspaceViewModel.AtlasItem item, float screenX, float screenY) {
        if (item == null || item.identity() == null) {
            setStatus("missing item identity");
            return;
        }
        contextMenuIdentity = item.identity();
        contextMenuItemSnapshot = item;
        contextMenuIslandId = null;
        contextMenuKitId = null;
        contextMenuChestStorageId = null;
        contextMenuX = screenX;
        contextMenuY = screenY;
        editingIslandId = null;
        editingDesiredCountIdentity = null;
        renamingKitId = null;
        renameKitDraft = "";
        confirmDeleteKitId = null;
        renamingChestStorageId = null;
        renameChestDraft = "";
        status = item.name();
        rebuildRequested = true;
    }

    private void openHelpPopover(float screenX, float screenY) {
        closeOverlayState();
        helpPopoverOpen = true;
        helpPopoverX = screenX;
        helpPopoverY = screenY;
        status = "SLOT help";
        rebuildRequested = true;
    }

    private void openKitContextMenu(String kitId, float screenX, float screenY) {
        if (kitId == null || kitId.isBlank()) {
            setStatus("missing workflow");
            return;
        }
        contextMenuKitId = kitId;
        contextMenuIdentity = null;
        contextMenuItemSnapshot = null;
        contextMenuIslandId = null;
        contextMenuChestStorageId = null;
        contextMenuX = screenX;
        contextMenuY = screenY;
        editingIslandId = null;
        editingDesiredCountIdentity = null;
        renamingKitId = null;
        renameKitDraft = "";
        confirmDeleteKitId = null;
        renamingChestStorageId = null;
        renameChestDraft = "";
        status = "workflow menu";
        rebuildRequested = true;
    }

    private void openChestContextMenu(String storageId, float screenX, float screenY) {
        if (storageId == null || storageId.isBlank()) {
            setStatus("missing chest");
            return;
        }
        contextMenuChestStorageId = storageId;
        contextMenuIdentity = null;
        contextMenuItemSnapshot = null;
        contextMenuIslandId = null;
        contextMenuKitId = null;
        contextMenuX = screenX;
        contextMenuY = screenY;
        editingIslandId = null;
        editingDesiredCountIdentity = null;
        renamingKitId = null;
        renameKitDraft = "";
        confirmDeleteKitId = null;
        renamingChestStorageId = null;
        renameChestDraft = "";
        status = "chest menu";
        rebuildRequested = true;
    }

    private void openSectionOrderMenu(
            SlotWorkspaceViewModel.AtlasIsland island,
            float screenX,
            float screenY
    ) {
        if (island == null || island.kind() != VisualAtlasIslandKind.PLAYER) {
            setStatus("section cannot be reordered");
            return;
        }
        closeOverlayState();
        contextMenuIslandId = island.islandId();
        contextMenuX = screenX;
        contextMenuY = screenY;
        status = "section order";
        rebuildRequested = true;
    }

    private void beginDesiredCountEdit(SlotWorkspaceViewModel.AtlasItem item) {
        if (item == null || item.identity() == null) {
            setStatus("missing item identity");
            return;
        }
        editingDesiredCountIdentity = item.identity();
        desiredCountDraft = item.desiredCount() > 0 && !item.desiredCountFromKit()
                ? Integer.toString(item.desiredCount())
                : "";
        contextMenuIdentity = null;
        contextMenuItemSnapshot = null;
        contextMenuIslandId = null;
        contextMenuKitId = null;
        contextMenuChestStorageId = null;
        editingIslandId = null;
        status = "desired count";
        rebuildRequested = true;
    }

    private void beginIslandEdit(SlotWorkspaceViewModel.AtlasIsland island, float screenX, float screenY) {
        if (island == null || island.kind() != VisualAtlasIslandKind.PLAYER) {
            setStatus("section cannot be edited");
            return;
        }
        editingIslandId = island.islandId();
        islandLabelDraft = island.label();
        islandEditX = screenX;
        islandEditY = screenY;
        contextMenuIdentity = null;
        contextMenuItemSnapshot = null;
        contextMenuIslandId = null;
        contextMenuKitId = null;
        contextMenuChestStorageId = null;
        editingDesiredCountIdentity = null;
        renamingKitId = null;
        renameKitDraft = "";
        confirmDeleteKitId = null;
        renamingChestStorageId = null;
        renameChestDraft = "";
        status = "editing " + island.label();
        rebuildRequested = true;
    }

    private void beginKitRenameEdit(SlotWorkspaceViewModel.KitCard kit) {
        if (kit == null || kit.kitId().isBlank()) {
            setStatus("missing workflow");
            return;
        }
        renamingKitId = kit.kitId();
        renameKitDraft = kit.name();
        confirmDeleteKitId = null;
        status = "renaming workflow";
        rebuildRequested = true;
    }

    private void beginChestRenameEdit(SlotWorkspaceViewModel.ChestChip chip) {
        if (chip == null || chip.storageId().isBlank()) {
            setStatus("missing chest");
            return;
        }
        renamingChestStorageId = chip.storageId();
        renameChestDraft = chip.label();
        status = "renaming chest";
        rebuildRequested = true;
    }

    private boolean handleEditorKey(int keyCode) {
        if (editingDesiredCountIdentity == null
                && editingIslandId == null
                && renamingKitId == null
                && renamingChestStorageId == null) {
            return false;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            closeOverlays();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            if (editingDesiredCountIdentity != null) {
                commitDesiredCountEdit();
            } else if (editingIslandId != null) {
                commitIslandNameEdit();
            } else if (renamingKitId != null) {
                commitKitRenameEdit();
            } else if (renamingChestStorageId != null) {
                commitChestRenameEdit();
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (editingDesiredCountIdentity != null) {
                if (!desiredCountDraft.isEmpty()) {
                    desiredCountDraft = desiredCountDraft.substring(0, desiredCountDraft.length() - 1);
                    rebuildRequested = true;
                }
            } else if (editingIslandId != null && !islandLabelDraft.isEmpty()) {
                islandLabelDraft = islandLabelDraft.substring(0, islandLabelDraft.length() - 1);
                rebuildRequested = true;
            } else if (renamingKitId != null && !renameKitDraft.isEmpty()) {
                renameKitDraft = renameKitDraft.substring(0, renameKitDraft.length() - 1);
                rebuildRequested = true;
            } else if (renamingChestStorageId != null && !renameChestDraft.isEmpty()) {
                renameChestDraft = renameChestDraft.substring(0, renameChestDraft.length() - 1);
                rebuildRequested = true;
            }
            return true;
        }
        return false;
    }

    private boolean handleEditorChar(char codePoint) {
        if (editingDesiredCountIdentity == null
                && editingIslandId == null
                && renamingKitId == null
                && renamingChestStorageId == null) {
            return false;
        }
        if (editingDesiredCountIdentity != null) {
            if (codePoint >= '0' && codePoint <= '9' && desiredCountDraft.length() < 4) {
                desiredCountDraft += codePoint;
                rebuildRequested = true;
            }
            return true;
        }
        if (renamingKitId != null) {
            if (isPrintable(codePoint) && renameKitDraft.length() < 32) {
                renameKitDraft += codePoint;
                rebuildRequested = true;
            }
            return true;
        }
        if (renamingChestStorageId != null) {
            if (isPrintable(codePoint) && renameChestDraft.length() < 32) {
                renameChestDraft += codePoint;
                rebuildRequested = true;
            }
            return true;
        }
        if (isPrintable(codePoint) && islandLabelDraft.length() < 32) {
            islandLabelDraft += codePoint;
            rebuildRequested = true;
        }
        return true;
    }

    private void commitDesiredCountEdit() {
        SlotWorkspaceViewModel.IdentityRef identity = editingDesiredCountIdentity;
        if (identity == null) {
            closeOverlays();
            return;
        }
        int count;
        try {
            count = desiredCountDraft.isBlank() ? 0 : Integer.parseInt(desiredCountDraft);
        } catch (NumberFormatException exception) {
            setStatus("desired count must be a number");
            return;
        }
        closeOverlayState();
        sendIdentityRefAction(WorkspaceActionId.SET_PLAYER_DESIRED_COUNT, identity, "desired count updated", count);
    }

    private void commitIslandNameEdit() {
        if (editingIslandId == null) {
            closeOverlays();
            return;
        }
        String label = islandLabelDraft == null ? "" : islandLabelDraft.trim();
        if (label.isBlank()) {
            setStatus("section name required");
            return;
        }
        String islandId = editingIslandId;
        closeOverlayState();
        sendAction(WorkspaceActionId.RENAME_ISLAND, "renaming section", islandId, label);
    }

    private void commitKitRenameEdit() {
        if (renamingKitId == null) {
            closeOverlays();
            return;
        }
        String label = renameKitDraft == null ? "" : renameKitDraft.trim();
        if (label.isBlank()) {
            setStatus("workflow name required");
            return;
        }
        String kitId = renamingKitId;
        closeOverlayState();
        sendKitAction(WorkspaceActionId.RENAME_KIT, "renaming workflow", kitId, label);
    }

    private void commitChestRenameEdit() {
        if (renamingChestStorageId == null) {
            closeOverlays();
            return;
        }
        String label = renameChestDraft == null ? "" : renameChestDraft.trim();
        if (label.isBlank()) {
            setStatus("chest name required");
            return;
        }
        String storageId = renamingChestStorageId;
        closeOverlayState();
        sendAction(WorkspaceActionId.RELABEL_CHEST, "renaming chest", storageId, label);
    }

    private void closeOverlays() {
        closeOverlayState();
        rebuildRequested = true;
    }

    private void closeOverlayState() {
        helpPopoverOpen = false;
        contextMenuIdentity = null;
        contextMenuItemSnapshot = null;
        contextMenuIslandId = null;
        contextMenuKitId = null;
        contextMenuChestStorageId = null;
        editingIslandId = null;
        editingDesiredCountIdentity = null;
        islandLabelDraft = "";
        desiredCountDraft = "";
        renamingKitId = null;
        renameKitDraft = "";
        confirmDeleteKitId = null;
        renamingChestStorageId = null;
        renameChestDraft = "";
    }

    private static boolean isPrintable(char codePoint) {
        return codePoint >= 32 && codePoint != 127;
    }

    private static String shorten(String text, int maxChars) {
        String value = text == null ? "" : text;
        int max = Math.max(1, maxChars);
        if (value.length() <= max) {
            return value;
        }
        if (max <= 3) {
            return value.substring(0, max);
        }
        return value.substring(0, max - 3) + "...";
    }

    private String searchDisplayText() {
        if (searchActive) {
            return "/" + searchQuery + "_";
        }
        return searchQuery.isBlank() ? "press /" : searchQuery;
    }

    private void syncSearchQuery() {
        if (searchQuery.equals(lastSentSearchQuery)) {
            return;
        }
        boolean sent = actionChannel.send(WorkspaceActionId.SET_SEARCH_QUERY, searchQuery);
        if (sent) {
            lastSentSearchQuery = searchQuery;
        } else {
            status = "failed to sync search query";
        }
        rebuildRequested = true;
    }

    private void setSearchQuery(String value) {
        String next = WorkspaceSearchQuery.cleanInput(value);
        if (next.equals(searchQuery)) {
            WorkspaceUiSessionMemory.setSearchQuery(surfaceMemoryKey(), next);
            return;
        }
        searchQuery = next;
        WorkspaceUiSessionMemory.setSearchQuery(surfaceMemoryKey(), searchQuery);
        syncSearchQuery();
        rebuildRequested = true;
    }

    private boolean applySearchDecision(WorkspaceSearchInputPolicy.Decision decision) {
        if (decision == null || !decision.handled()) {
            return false;
        }
        searchActive = decision.active();
        if (decision.action() == WorkspaceSearchInputPolicy.Action.OPEN
                || decision.action() == WorkspaceSearchInputPolicy.Action.APPEND
                || decision.action() == WorkspaceSearchInputPolicy.Action.BACKSPACE
                || decision.action() == WorkspaceSearchInputPolicy.Action.IGNORE_DIGIT) {
            lastSearchInputMillis = System.currentTimeMillis();
        }
        setSearchQuery(decision.query());
        status = switch (decision.action()) {
            case OPEN -> "search";
            case CONFIRM -> searchQuery.isBlank() ? "search confirmed" : "search: " + searchQuery;
            case CLEAR -> "search cleared";
            case BACKSPACE, APPEND, IGNORE_DIGIT -> "search";
            case NONE -> status;
        };
        rebuildRequested = true;
        return true;
    }

    private void autoConfirmSearchIfIdle() {
        if (!searchActive || System.currentTimeMillis() - lastSearchInputMillis < SEARCH_AUTO_CONFIRM_MILLIS) {
            return;
        }
        applySearchDecision(WorkspaceSearchInputPolicy.confirmForHotbar(true, searchQuery));
    }

    private static WorkspaceSearchInputPolicy.ControlKey searchControlKey(int keyCode) {
        return switch (keyCode) {
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> WorkspaceSearchInputPolicy.ControlKey.ENTER;
            case GLFW.GLFW_KEY_BACKSPACE -> WorkspaceSearchInputPolicy.ControlKey.BACKSPACE;
            case GLFW.GLFW_KEY_BACKSLASH -> WorkspaceSearchInputPolicy.ControlKey.CLEAR;
            default -> null;
        };
    }

    private void clearSearchFromPointer() {
        applySearchDecision(WorkspaceSearchInputPolicy.keyPressed(
                true,
                searchQuery,
                WorkspaceSearchInputPolicy.ControlKey.CLEAR));
    }

    private static boolean isSearchTypingKey(int keyCode) {
        if ((keyCode >= GLFW.GLFW_KEY_0 && keyCode <= GLFW.GLFW_KEY_9)
                || (keyCode >= GLFW.GLFW_KEY_KP_0 && keyCode <= GLFW.GLFW_KEY_KP_9)) {
            return false;
        }
        return keyCode >= GLFW.GLFW_KEY_SPACE && keyCode <= GLFW.GLFW_KEY_WORLD_2;
    }

    private static int hotbarIndexFromKeyCode(int keyCode) {
        if (keyCode >= GLFW.GLFW_KEY_1 && keyCode <= GLFW.GLFW_KEY_9) {
            return keyCode - GLFW.GLFW_KEY_1;
        }
        if (keyCode >= GLFW.GLFW_KEY_KP_1 && keyCode <= GLFW.GLFW_KEY_KP_9) {
            return keyCode - GLFW.GLFW_KEY_KP_1;
        }
        return -1;
    }

    private void handleHotbarKey(int hotbarIndex) {
        SlotWorkspaceViewModel.IdentityRef identity = hoveredIdentity;
        if (identity == null) {
            applySearchDecision(WorkspaceSearchInputPolicy.confirmForHotbar(searchActive, searchQuery));
            setStatus("hover an atlas item to assign with 1-9");
            return;
        }
        applySearchDecision(WorkspaceSearchInputPolicy.confirmForHotbar(searchActive, searchQuery));
        sendIdentityToHotbar(identity, hotbarIndex);
    }

    private void sendAction(WorkspaceActionId action, String sentStatus, Object... args) {
        if (!flushingWheelTransfer) {
            flushWheelTransferBatch();
        }
        boolean sent = actionChannel.send(action, args);
        status = sent ? sentStatus : "failed to send " + action.name().toLowerCase();
        if (flushingWheelTransfer) {
            if (statusLabel != null) {
                statusLabel.text(status);
            }
        } else {
            rebuildRequested = true;
        }
    }

    private void sendSectionReorderAround(
            SlotWorkspaceViewModel.AtlasIsland source,
            SlotWorkspaceViewModel.AtlasIsland anchor,
            boolean above
    ) {
        if (source == null || anchor == null) {
            setStatus("section reorder unavailable");
            return;
        }
        int targetIndex = reorderTargetIndex(source.islandId(), anchor.islandId(), above);
        if (targetIndex < 0) {
            setStatus("section reorder unavailable");
            return;
        }
        sendSectionReorderToIndex(source, targetIndex);
    }

    private int reorderTargetIndex(String sourceIslandId, String anchorIslandId, boolean above) {
        List<SlotWorkspaceViewModel.AtlasIsland> allSections = playerIslandsInOrder();
        int sourceIndex = islandIndex(allSections, sourceIslandId);
        int anchorIndex = islandIndex(allSections, anchorIslandId);
        if (sourceIndex < 0 || anchorIndex < 0 || sourceIndex == anchorIndex) {
            return -1;
        }
        int insertPosition = above ? anchorIndex : anchorIndex + 1;
        return Math.max(0, sourceIndex < insertPosition ? insertPosition - 1 : insertPosition);
    }

    private void sendSectionReorderToIndex(SlotWorkspaceViewModel.AtlasIsland island, int targetIndex) {
        if (island == null || island.islandId().isBlank()) {
            setStatus("section reorder unavailable");
            return;
        }
        sendAction(
                WorkspaceActionId.REORDER_ISLAND,
                "moving " + island.label(),
                island.islandId(),
                Math.max(0, targetIndex));
    }

    private boolean sendAssignHome(
            SlotWorkspaceViewModel.IdentityRef identity,
            String islandId,
            Integer ordinal,
            String sentStatus
    ) {
        if (identity == null || islandId == null || islandId.isBlank()) {
            setStatus("invalid home target");
            return false;
        }
        boolean sent = actionChannel.send(
                WorkspaceActionId.ASSIGN_HOME,
                identity.itemId(),
                identity.comparisonMode(),
                identity.componentFingerprint(),
                islandId,
                ordinal);
        if (sent) {
            rememberRehomeTarget(islandId);
        }
        status = sent ? sentStatus : "failed to send assign_home";
        rebuildRequested = true;
        return sent;
    }

    private void rememberRehomeTarget(String islandId) {
        if (islandId == null || islandId.isBlank()) {
            return;
        }
        recentRehomeIslandIds.remove(islandId);
        recentRehomeIslandIds.addFirst(islandId);
        while (recentRehomeIslandIds.size() > RECENT_REHOME_CAPACITY) {
            recentRehomeIslandIds.removeLast();
        }
    }

    private void sendIdentityToHotbar(SlotWorkspaceViewModel.IdentityRef identity, int hotbarIndex) {
        if (identity == null || hotbarIndex < 0 || hotbarIndex > 8) {
            setStatus("hotbar assign unavailable");
            return;
        }
        sendAction(
                WorkspaceActionId.ASSIGN_IDENTITY_TO_HOTBAR_SLOT,
                "assigning to belt " + (hotbarIndex + 1),
                identity.itemId(),
                identity.comparisonMode(),
                identity.componentFingerprint(),
                hotbarIndex);
    }

    private void sendIdentityToAutoHotbar(SlotWorkspaceViewModel.IdentityRef identity) {
        if (identity == null) {
            setStatus("hotbar assign unavailable");
            return;
        }
        sendAction(
                WorkspaceActionId.ASSIGN_IDENTITY_TO_AUTO_HOTBAR,
                "moving to hotbar",
                identity.itemId(),
                identity.comparisonMode(),
                identity.componentFingerprint());
    }

    private void setStatus(String nextStatus) {
        status = nextStatus == null || nextStatus.isBlank() ? "ready" : nextStatus;
        if (statusLabel != null) {
            statusLabel.text(status);
        }
        rebuildRequested = true;
    }

    private void selectAllTab() {
        refreshPresentedItems();
        setStatus("showing all items");
    }

    private void selectAllWorkflowTab() {
        if (viewModel.activeKit() != null) {
            sendKitAction(WorkspaceActionId.DEACTIVATE_KIT, "showing All");
        } else {
            refreshPresentedItems();
            setStatus("showing All");
        }
    }

    private void selectWorkflowTab(String kitId) {
        if (kitId == null || kitId.isBlank()) {
            selectAllWorkflowTab();
            return;
        }
        SlotWorkspaceViewModel.KitCard tab = viewModel.kit(kitId);
        sendKitAction(WorkspaceActionId.ACTIVATE_KIT, "showing " + (tab == null ? "workflow" : tab.name()), kitId);
    }

    private void openRecipe(SlotWorkspaceViewModel.AtlasItem item) {
        openRecipe(item == null ? null : item.identity().toIdentity());
    }

    private void openRecipe(ItemIdentity identity) {
        if (identity == null) {
            setStatus("item unavailable");
        } else if (RecipeViewerIntegration.openRecipe(identity)) {
            setStatus("opened recipe in EMI");
        } else {
            setStatus("EMI recipe display unavailable");
        }
    }

    private void openUses(SlotWorkspaceViewModel.AtlasItem item) {
        ItemIdentity identity = item == null ? null : item.identity().toIdentity();
        if (identity == null) {
            setStatus("item unavailable");
        } else if (RecipeViewerIntegration.openUses(identity)) {
            setStatus("opened uses in EMI");
        } else {
            setStatus("EMI usage display unavailable");
        }
    }

    private WallCardTransferGesturePolicy.Context cardGestureContext(
            SlotWorkspaceViewModel.AtlasItem item,
            int button,
            boolean shiftDown
    ) {
        return cardGestureContext(item, button, shiftDown, Screen.hasControlDown());
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
        return new WallCardTransferGesturePolicy.Context(
                item,
                button,
                shiftDown,
                controlDown,
                cursorIdentity(),
                isCursorCarrying(),
                mode == Mode.SIDEBAR,
                carriedFreeSlotCount(),
                anyChestProximate(),
                activeChestOpen(),
                wantedAdjustDown,
                shiftClickTransferState.continuingTake(
                        item == null ? null : item.identity(),
                        shiftDown));
    }

    private boolean activeChestOpen() {
        return viewModel != null
                && viewModel.activeChestPanel() != null
                && viewModel.activeChestPanel().isPresent();
    }

    private int wheelSteps(
            SlotWorkspaceViewModel.IdentityRef identity,
            float delta,
            boolean controlDown
    ) {
        if (delta == 0f) {
            return 0;
        }
        if (controlDown) {
            if (identity != null) {
                wheelAccumulatorByIdentity.remove(identity);
            }
            return delta > 0f ? 1 : -1;
        }
        if (identity == null) {
            return (int) delta;
        }
        float accumulated = wheelAccumulatorByIdentity.getOrDefault(identity, 0f) + delta;
        int steps = (int) accumulated;
        float remainder = accumulated - steps;
        rememberWheelRemainder(identity, remainder);
        return steps;
    }

    private void rememberWheelRemainder(SlotWorkspaceViewModel.IdentityRef identity, float remainder) {
        if (identity == null) {
            return;
        }
        if (Math.abs(remainder) < 0.0001f) {
            wheelAccumulatorByIdentity.remove(identity);
            return;
        }
        wheelAccumulatorByIdentity.put(identity, remainder);
        while (wheelAccumulatorByIdentity.size() > WHEEL_ACCUMULATOR_MAX_IDENTITIES) {
            var iterator = wheelAccumulatorByIdentity.keySet().iterator();
            if (!iterator.hasNext()) {
                return;
            }
            iterator.next();
            iterator.remove();
        }
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
            case STATUS -> setStatus(decision.status());
            case PICKUP_TO_CURSOR -> sendIdentityAction(
                    WorkspaceActionId.PICKUP_TO_CURSOR,
                    item,
                    "picking up " + item.name(),
                    count <= 0 ? WallCardTransferGesturePolicy.PICKUP_MAX : count);
            case CURSOR_CANCEL -> sendAction(WorkspaceActionId.CURSOR_CANCEL, "returning cursor");
            case CURSOR_SMART_DEPOSIT -> sendAction(WorkspaceActionId.CURSOR_SMART_DEPOSIT, "depositing cursor");
            case CURSOR_CANCEL_THEN_PICKUP_TO_CURSOR -> {
                sendAction(WorkspaceActionId.CURSOR_CANCEL, "returning cursor");
                sendIdentityAction(
                        WorkspaceActionId.PICKUP_TO_CURSOR,
                        item,
                        "picking up " + item.name(),
                        count <= 0 ? WallCardTransferGesturePolicy.PICKUP_MAX : count);
            }
            case TAKE_DESIRED_GAP_OR_STACK_BY_IDENTITY -> sendIdentityAction(
                    WorkspaceActionId.TAKE_DESIRED_GAP_OR_STACK_BY_IDENTITY,
                    item,
                    "taking " + item.name());
            case TAKE_STACK_BY_IDENTITY -> sendIdentityAction(
                    WorkspaceActionId.TAKE_STACK_BY_IDENTITY,
                    item,
                    "taking " + item.name());
            case TAKE_ITEMS_BY_IDENTITY -> enqueueWheelTransfer(
                    WorkspaceActionId.TAKE_ITEMS_BY_IDENTITY,
                    item,
                    "taking " + item.name(),
                    count);
            case DEPOSIT_HOME_TO_LINKED_CHEST -> sendIdentityAction(
                    WorkspaceActionId.DEPOSIT_HOME_TO_LINKED_CHEST,
                    item,
                    "depositing " + item.name());
            case DEPOSIT_ITEMS_HOME_TO_LINKED_CHEST -> enqueueWheelTransfer(
                    WorkspaceActionId.DEPOSIT_ITEMS_HOME_TO_LINKED_CHEST,
                    item,
                    "depositing " + item.name(),
                    count);
            case CROSS_SURFACE_QUICK_MOVE -> sendIdentityAction(
                    WorkspaceActionId.CROSS_SURFACE_QUICK_MOVE_ATLAS,
                    item,
                    "quick-moving to host",
                    count);
            case ADJUST_PLAYER_DESIRED_COUNT -> sendIdentityAction(
                    WorkspaceActionId.ADJUST_PLAYER_DESIRED_COUNT,
                    item,
                    "desired count updated",
                    count);
            case ADJUST_WANTED_COUNT -> sendIdentityAction(
                    WorkspaceActionId.ADJUST_WANTED_COUNT,
                    item,
                    "wanted count updated",
                    count);
        }
        shiftClickTransferState.record(decision, item == null ? null : item.identity(), Screen.hasShiftDown());
        return true;
    }

    private void sendIdentityAction(
            WorkspaceActionId action,
            SlotWorkspaceViewModel.AtlasItem item,
            String sentStatus,
            Object... tail
    ) {
        if (item == null || item.identity() == null) {
            setStatus("missing item identity");
            return;
        }
        sendIdentityRefAction(action, item.identity(), sentStatus, tail);
    }

    private void enqueueWheelTransfer(
            WorkspaceActionId action,
            SlotWorkspaceViewModel.AtlasItem item,
            String sentStatus,
            int count
    ) {
        if (item == null || item.identity() == null) {
            setStatus("missing item identity");
            return;
        }
        flushWheelTransfer(wheelTransferBatcher.enqueue(action, item.identity(), count, sentStatus));
        setWheelStatus(sentStatus);
    }

    private void flushWheelTransferBatch() {
        flushWheelTransfer(wheelTransferBatcher.flush());
    }

    private void flushWheelTransferBatch(boolean shiftDown) {
        flushWheelTransfer(shiftDown ? wheelTransferBatcher.flushIfIdle() : wheelTransferBatcher.flush());
    }

    private void flushWheelTransfer(WheelTransferBatcher.Pending pending) {
        if (pending == null || pending.identity() == null || pending.count() <= 0) {
            return;
        }
        flushingWheelTransfer = true;
        try {
            sendIdentityRefAction(pending.action(), pending.identity(), pending.status(), pending.count());
        } finally {
            flushingWheelTransfer = false;
        }
    }

    private void setWheelStatus(String nextStatus) {
        status = nextStatus == null ? "" : nextStatus;
        if (statusLabel != null) {
            statusLabel.text(status);
        }
    }

    private void sendIdentityRefAction(
            WorkspaceActionId action,
            SlotWorkspaceViewModel.IdentityRef identity,
            String sentStatus,
            Object... tail
    ) {
        if (identity == null) {
            setStatus("missing item identity");
            return;
        }
        Object[] args = new Object[3 + (tail == null ? 0 : tail.length)];
        args[0] = identity.itemId();
        args[1] = identity.comparisonMode();
        args[2] = identity.componentFingerprint();
        if (tail != null) {
            System.arraycopy(tail, 0, args, 3, tail.length);
        }
        sendAction(action, sentStatus, args);
    }

    private int wantedHoverTargetCount(SlotWorkspaceViewModel.AtlasItem item) {
        if (item == null) {
            return 1;
        }
        if (recipeSidebarActive() && item.desiredCount() > 0) {
            return item.desiredCount();
        }
        return 1;
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

    private boolean sendKitAction(WorkspaceActionId action, String sentStatus, Object... args) {
        boolean sent = actionChannel.send(action, args);
        status = sent ? sentStatus : "failed to send " + action.name().toLowerCase();
        rebuildRequested = true;
        return sent;
    }

    private void sendTakeByIdentity(SlotWorkspaceViewModel.IdentityRef identity) {
        if (identity == null) {
            setStatus("invalid tab identity");
            return;
        }
        sendKitAction(
                WorkspaceActionId.TAKE_STACK_BY_IDENTITY,
                "gathering " + identity.itemId(),
                identity.itemId(),
                identity.comparisonMode(),
                identity.componentFingerprint());
    }

    private int carriedFreeSlotCount() {
        if (appliedRevision < 0 && viewModel.carriedSlotCapacity() == 0) {
            return 9;
        }
        return viewModel.carriedFreeSlotCount();
    }

    private boolean anyChestProximate() {
        for (SlotWorkspaceViewModel.ChestChip chip : viewModel.chestChips()) {
            if (chip != null && chip.proximate()) {
                return true;
            }
        }
        for (SlotWorkspaceViewModel.AtlasItem item : items) {
            if (item != null && !item.presence().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean isCursorCarrying() {
        ItemStack stack = cursorStack();
        return stack != null && !stack.isEmpty();
    }

    private ItemStack cursorStack() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.player.containerMenu == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = minecraft.player.containerMenu.getCarried();
        return stack == null ? ItemStack.EMPTY : stack;
    }

    private SlotWorkspaceViewModel.IdentityRef cursorIdentity() {
        ItemStack stack = cursorStack();
        return stack.isEmpty()
                ? null
                : SlotWorkspaceViewModel.IdentityRef.from(ItemIdentityMatcher.create(stack));
    }

    private void renderCursorStack(GuiGraphics graphics, int mouseX, int mouseY) {
        ItemStack stack = cursorStack();
        if (stack.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        int x = mouseX - 8;
        int y = mouseY - 8;
        graphics.renderItem(stack, x, y);
        graphics.renderItemDecorations(minecraft.font, stack, x, y);
    }

    private static int currentMenuContainerId() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.player.containerMenu == null) {
            return WorkspaceActionEnvelope.NO_MENU_CONTAINER;
        }
        return minecraft.player.containerMenu.containerId;
    }

    private static int normalizedMenuContainerId(int menuContainerId) {
        return Math.max(WorkspaceActionEnvelope.NO_MENU_CONTAINER, menuContainerId);
    }

    public enum Mode {
        STANDALONE,
        SIDEBAR
    }

    private final class WorkflowTabsContext implements WorkflowTabsUiBuilder.Context {
        @Override
        public void selectAll() {
            selectAllWorkflowTab();
        }

        @Override
        public void selectTab(String kitId) {
            selectWorkflowTab(kitId);
        }

        @Override
        public void createTab() {
            sendKitAction(WorkspaceActionId.CREATE_WORKFLOW_TAB, "creating workflow", "");
        }

        @Override
        public void openTabMenu(String kitId, float screenX, float screenY) {
            ForgeWorkspaceSurface.this.openKitContextMenu(kitId, screenX, screenY);
        }
    }

    private final class CraftRunContext implements CraftRunUiBuilder.Context {
        @Override
        public dev.imagio.slot.workflow.domain.CraftRunState craftRun() {
            return viewModel.craftRun();
        }

        @Override
        public List<CraftRunRecipeCapture> visibleRecipes() {
            return craftRunRecipeCaptures == null ? List.of() : craftRunRecipeCaptures;
        }

        @Override
        public void addVisibleRecipe(CraftRunRecipeCapture capture) {
            boolean sent = SlotForgeNetworking.addCraftRunRecipe(capture);
            setStatus(sent ? "added recipe" : "failed to add recipe");
            rebuildRequested = true;
        }

        @Override
        public void stageEntry(String entryId) {
            sendAction(WorkspaceActionId.CRAFT_RUN_STAGE_ENTRY, "staging craft ingredients", entryId);
        }

        @Override
        public void openRecipe(CraftRunRecipeEntry entry) {
            ForgeWorkspaceSurface.this.openRecipe(entry == null ? null : entry.outputIdentity());
        }

        @Override
        public void adjustEntry(String entryId, int delta) {
            sendAction(WorkspaceActionId.CRAFT_RUN_ADJUST_ENTRY, "adjusting recipe", entryId, delta);
        }

        @Override
        public void removeEntry(String entryId) {
            sendAction(WorkspaceActionId.CRAFT_RUN_REMOVE_ENTRY, "removed craft recipe", entryId);
        }
    }

    private final class ActiveChestContext implements ActiveChestStripUiBuilder.Context {
        @Override
        public void setChestRoleAt(SlotWorkspaceViewModel.ActiveChestPanel panel, ChestRole role) {
            if (panel == null || !panel.isPresent()) {
                setStatus("no active chest");
                return;
            }
            if (role == null) {
                setStatus("missing chest role");
                return;
            }
            sendAction(
                    WorkspaceActionId.SET_CHEST_ROLE_AT_POS,
                    "chest role: " + role.displayLabel(),
                    panel.dimensionId(),
                    panel.posX(),
                    panel.posY(),
                    panel.posZ(),
                    role.name());
        }

        @Override
        public void openChestMenu(String storageId, float screenX, float screenY) {
            ForgeWorkspaceSurface.this.openChestContextMenu(storageId, screenX, screenY);
        }
    }

    private final class HeaderContext implements WallSectionHeaderUiBuilder.Context {
        @Override
        public void beginIslandEdit(SlotWorkspaceViewModel.AtlasIsland island, float screenX, float screenY) {
            ForgeWorkspaceSurface.this.beginIslandEdit(island, screenX, screenY);
        }

        @Override
        public void toggleNearbySection(SlotWorkspaceViewModel.AtlasIsland island) {
            if (island == null) {
                return;
            }
            if (isCursorCarrying()) {
                return;
            }
            toggleStorageGhostSection(island.islandId());
        }
    }

    private final class KitContext implements KitRackUiBuilder.Context {
        @Override
        public void toggleKitRack() {
            kitRackOpen = !kitRackOpen;
            setStatus(kitRackOpen
                    ? "workflows open (" + viewModel.kits().size() + ")"
                    : "workflows closed");
        }

        @Override
        public void closeKitRack() {
            kitRackOpen = false;
            setStatus("workflows closed");
        }

        @Override
        public void saveCurrentBeltAsKit() {
            sendKitAction(WorkspaceActionId.SAVE_KIT, "saving belt as workflow", "");
        }

        @Override
        public void createEmptyTab() {
            sendKitAction(WorkspaceActionId.CREATE_WORKFLOW_TAB, "creating workflow", "");
        }

        @Override
        public void activateKit(String kitId) {
            sendKitAction(WorkspaceActionId.ACTIVATE_KIT, "activating workflow", kitId);
        }

        @Override
        public void deactivateKit() {
            sendKitAction(WorkspaceActionId.DEACTIVATE_KIT, "deactivating workflow");
        }

        @Override
        public void switchActiveKitPage(int direction) {
            sendKitAction(WorkspaceActionId.SWITCH_KIT_PAGE, "switching workflow page", direction);
        }

        @Override
        public void addKitPage(String kitId) {
            sendKitAction(WorkspaceActionId.ADD_KIT_PAGE, "adding workflow page", kitId);
        }

        @Override
        public void removeKitPage(String kitId, int pageIndex) {
            sendKitAction(WorkspaceActionId.REMOVE_KIT_PAGE, "removing workflow page", kitId, pageIndex);
        }

        @Override
        public void clearKitSlot(String kitId, int pageIndex, int slotIndex) {
            sendKitAction(
                    WorkspaceActionId.SET_KIT_SLOT_IDENTITY,
                    "clearing workflow slot",
                    kitId,
                    pageIndex,
                    slotIndex,
                    "",
                    "",
                    "");
        }

        @Override
        public void takeStackByIdentity(SlotWorkspaceViewModel.IdentityRef identity) {
            sendTakeByIdentity(identity);
        }

        @Override
        public int proximateCount(SlotWorkspaceViewModel.IdentityRef identity) {
            SlotWorkspaceViewModel.AtlasItem item = byIdentity.get(identity);
            return item == null ? 0 : item.proximateCount();
        }

        @Override
        public void setStatus(String nextStatus) {
            ForgeWorkspaceSurface.this.setStatus(nextStatus);
        }

        @Override
        public void openKitMenu(String kitId, float screenX, float screenY) {
            ForgeWorkspaceSurface.this.openKitContextMenu(kitId, screenX, screenY);
        }
    }

    private final class RecentsContext implements RecentsStripUiBuilder.Context {
        @Override
        public SlotWorkspaceViewModel.AtlasItem atlasItem(SlotWorkspaceViewModel.IdentityRef identity) {
            SlotWorkspaceViewModel.AtlasItem item = viewModel.atlasItem(identity);
            return item == null ? cursorRecentItem(identity) : item;
        }

        @Override
        public void hoverRecent(SlotWorkspaceViewModel.AtlasItem item) {
            hoveredIdentity = item == null ? null : item.identity();
        }

        @Override
        public void clearHoveredRecent(SlotWorkspaceViewModel.AtlasItem item) {
            if (item != null && item.identity().equals(hoveredIdentity)) {
                hoveredIdentity = null;
            }
        }

        private SlotWorkspaceViewModel.AtlasItem cursorRecentItem(SlotWorkspaceViewModel.IdentityRef identity) {
            SlotWorkspaceViewModel.IdentityRef cursor = cursorIdentity();
            if (identity == null || cursor == null || !identity.equals(cursor)) {
                return null;
            }
            ItemStack stack = cursorStack();
            if (stack.isEmpty()) {
                return null;
            }
            return new SlotWorkspaceViewModel.AtlasItem(
                    identity,
                    stack,
                    stack.getHoverName().getString(),
                    stack.getCount(),
                    0,
                    "",
                    true,
                    false,
                    true,
                    List.of());
        }
    }

    private final class CardContext implements WallCardUiBuilder.Context {
        private final boolean forceWayfindingStrip;
        private final SlotWorkspaceViewModel.ContextualSuggestionLane suggestionLane;

        private CardContext() {
            this(false, null);
        }

        private CardContext(boolean forceWayfindingStrip) {
            this(forceWayfindingStrip, null);
        }

        private CardContext(
                boolean forceWayfindingStrip,
                SlotWorkspaceViewModel.ContextualSuggestionLane suggestionLane
        ) {
            this.forceWayfindingStrip = forceWayfindingStrip;
            this.suggestionLane = suggestionLane;
        }

        @Override
        public SlotWorkspaceViewModel.IdentityRef activeIdentity() {
            return cursorIdentity();
        }

        @Override
        public String normalizedSearchQuery() {
            return ForgeWorkspaceSurface.this.normalizedSearchQuery();
        }

        @Override
        public boolean matchesItem(SlotWorkspaceViewModel.AtlasItem item) {
            return matchesSearch(item);
        }

        @Override
        public boolean isMapFocusItem(SlotWorkspaceViewModel.AtlasItem item) {
            return item != null && item.identity().equals(hoveredIdentity);
        }

        @Override
        public void hoverAtlasIdentity(SlotWorkspaceViewModel.IdentityRef identity) {
            hoveredIdentity = identity;
        }

        @Override
        public void clearHoveredAtlasIdentity(SlotWorkspaceViewModel.IdentityRef identity) {
            if (identity != null && identity.equals(hoveredIdentity)) {
                hoveredIdentity = null;
            }
        }

        @Override
        public WayfindingDisplay.CardText wayfindingText(SlotWorkspaceViewModel.ChestPresenceEntry entry) {
            if (entry == null) {
                return WayfindingDisplay.CardText.unavailable();
            }
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null || minecraft.level == null) {
                return WayfindingDisplay.CardText.unavailable();
            }
            return WayfindingDisplay.forStorage(
                    entry.storageId(),
                    viewModel.wayfindingTargets(),
                    viewModel.chestChips(),
                    minecraft.level.dimension().location().toString(),
                    minecraft.player.getX(),
                    minecraft.player.getY(),
                    minecraft.player.getZ(),
                    minecraft.player.getYRot());
        }

        @Override
        public List<Component> tooltipLines(SlotWorkspaceViewModel.AtlasItem item) {
            RecipeIngredientSidebarSpec.Projection recipe = recipeProjection();
            if (recipe != null) {
                return recipe.tooltipLines(item);
            }
            if (SlotForgeClientConfig.contextualSuggestionDebugTooltips() && suggestionLane != null) {
                return WorkspaceItemTooltipBuilder.slotLines(
                        item,
                        suggestionLane,
                        true,
                        hasProximateDepositRoute(item));
            }
            return WorkspaceItemTooltipBuilder.slotLines(item, hasProximateDepositRoute(item));
        }

        @Override
        public boolean choiceInvolved(SlotWorkspaceViewModel.AtlasItem item) {
            return false;
        }

        @Override
        public boolean choiceCard(SlotWorkspaceViewModel.AtlasItem item) {
            return false;
        }

        @Override
        public boolean suppressVanillaTooltip(SlotWorkspaceViewModel.AtlasItem item) {
            RecipeIngredientSidebarSpec.Projection recipe = recipeProjection();
            if (recipe != null) {
                return recipe.suppressVanillaTooltip(item);
            }
            return false;
        }

        @Override
        public StorageGhostRevealMode storageGhostRevealMode() {
            return storageGhostRevealMode;
        }

        @Override
        public boolean forceWayfindingStrip(SlotWorkspaceViewModel.AtlasItem item) {
            return forceWayfindingStrip;
        }

        @Override
        public boolean hasProximateDepositRoute(SlotWorkspaceViewModel.AtlasItem item) {
            return item != null
                    && viewModel.depositableIdentities().contains(item.identity());
        }

        @Override
        public SlotWorkspaceViewModel.ContextualSuggestionLane contextualSuggestionLane() {
            return suggestionLane;
        }
    }

    private final class HotbarContext implements HotbarBeltUiBuilder.Context {
        @Override
        public void returnHotbarToHome(int hotbarIndex) {
            sendAction(WorkspaceActionId.RETURN_HOTBAR_TO_HOME, "returning belt " + (hotbarIndex + 1), hotbarIndex);
        }

        @Override
        public void quickMoveHotbarToHost(int hotbarIndex) {
            sendAction(
                    WorkspaceActionId.CROSS_SURFACE_QUICK_MOVE_HOTBAR,
                    "quick-moving belt " + (hotbarIndex + 1) + " to host",
                    hotbarIndex);
        }

        @Override
        public boolean isCursorCarrying() {
            return ForgeWorkspaceSurface.this.isCursorCarrying();
        }

        @Override
        public void dropCursorAtHotbar(int hotbarIndex, int button) {
            boolean carrying = isCursorCarrying();
            sendAction(
                    WorkspaceActionId.DROP_CURSOR_AT_HOTBAR,
                    carrying
                            ? "dropping cursor on belt " + (hotbarIndex + 1)
                            : "picking up belt " + (hotbarIndex + 1),
                    hotbarIndex,
                    button);
        }

        @Override
        public void setStatus(String status) {
            ForgeWorkspaceSurface.this.setStatus(status);
        }
    }
}
