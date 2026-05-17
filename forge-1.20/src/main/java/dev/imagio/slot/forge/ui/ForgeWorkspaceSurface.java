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
import dev.imagio.slot.inventory.goal.GoalProjectionEntry;
import dev.imagio.slot.inventory.goal.GoalStackDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.inventory.workspace.WorkspaceSearchQuery;
import dev.imagio.slot.ui.action.WorkspaceActionEnvelope;
import dev.imagio.slot.ui.action.WorkspaceActionId;
import dev.imagio.slot.ui.spi.SlotUiElement;
import dev.imagio.slot.ui.spi.SlotUiEventKind;
import dev.imagio.slot.ui.spi.SlotUiLayout;
import dev.imagio.slot.ui.spi.SlotUiTextStyle;
import dev.imagio.slot.ui.workspace.ActiveChestStripUiBuilder;
import dev.imagio.slot.ui.workspace.GoalTabsUiBuilder;
import dev.imagio.slot.ui.workspace.GoalWorkspaceClientState;
import dev.imagio.slot.ui.workspace.GoalWorkspaceIntegration;
import dev.imagio.slot.ui.workspace.GoalWorkspaceProjection;
import dev.imagio.slot.ui.workspace.GoalWorkspaceProjectionCache;
import dev.imagio.slot.ui.workspace.HotbarBeltUiBuilder;
import dev.imagio.slot.ui.workspace.KitRackUiBuilder;
import dev.imagio.slot.ui.workspace.RecentsStripUiBuilder;
import dev.imagio.slot.ui.workspace.RecipeIngredientSidebarSpec;
import dev.imagio.slot.ui.workspace.ShiftClickTransferState;
import dev.imagio.slot.ui.workspace.StorageGhostRevealMode;
import dev.imagio.slot.ui.workspace.WallCardTransferGesturePolicy;
import dev.imagio.slot.ui.workspace.WallCardUiBuilder;
import dev.imagio.slot.ui.workspace.WallSectionHeaderUiBuilder;
import dev.imagio.slot.ui.workspace.WallSectionUiBuilder;
import dev.imagio.slot.ui.workspace.WallSectionVisibility;
import dev.imagio.slot.ui.workspace.WayfindingDisplay;
import dev.imagio.slot.ui.workspace.WheelTransferBatcher;
import dev.imagio.slot.ui.workspace.WorkspaceGatherUiSupport;
import dev.imagio.slot.ui.workspace.WorkspaceCountFormat;
import dev.imagio.slot.ui.workspace.WorkspaceItemTooltipBuilder;
import dev.imagio.slot.ui.workspace.WorkspaceSearchInputPolicy;
import dev.imagio.slot.ui.workspace.WorkspaceUiAttachments;
import dev.imagio.slot.ui.workspace.WorkspaceUiPalette;
import dev.imagio.slot.ui.workspace.WorkspaceUiSessionMemory;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
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
    private static final int WHEEL_ACCUMULATOR_MAX_IDENTITIES = 64;

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
    private final GoalWorkspaceProjectionCache goalProjectionCache = new GoalWorkspaceProjectionCache();
    private final ArrayDeque<String> recentRehomeIslandIds = new ArrayDeque<>();
    private final Map<SlotWorkspaceViewModel.IdentityRef, Float> wheelAccumulatorByIdentity = new LinkedHashMap<>();
    private RecipeIngredientSidebarSpec recipeSidebarSpec = RecipeIngredientSidebarSpec.empty();
    private RecipeIngredientSidebarSpec.Projection recipeSidebarProjection;
    private long recipeSidebarProjectionRevision = Long.MIN_VALUE;
    private String recipeSidebarProjectionKey = "";

    private ForgeSlotUiTree tree;
    private SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.empty();
    private SlotWorkspaceViewModel.OffhandSlot offhand = SlotWorkspaceViewModel.OffhandSlot.empty();
    private SlotWorkspaceViewModel.IdentityRef hoveredIdentity;
    private String searchQuery = "";
    private String lastSentSearchQuery = "";
    private boolean searchActive;
    private long appliedRevision = -1L;
    private String status;
    private boolean openSessionRequested;
    private boolean rebuildRequested = true;
    private int appliedGoalStateRevision = GoalWorkspaceClientState.revision();
    private boolean kitRackOpen;
    private SlotWorkspaceViewModel.IdentityRef contextMenuIdentity;
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
    private boolean storageXrayKeyConsumed;
    private StorageGhostRevealMode storageGhostRevealMode = StorageGhostRevealMode.COLLAPSED;
    private float pendingWallScrollRestore = Float.NaN;
    private boolean pendingWallScrollRestoreActive;

    public ForgeWorkspaceSurface(Mode mode) {
        this.mode = mode == null ? Mode.STANDALONE : mode;
        this.envelope = new WorkspaceActionEnvelope(
                UUID.randomUUID().toString(),
                currentMenuContainerId(),
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
        if (!openSessionRequested) {
            return;
        }
        openSessionRequested = false;
        SlotForgeNetworking.closeWorkspaceSession(new ForgeWorkspaceCloseMessage(envelope));
    }

    public void tick(int width, int height) {
        shiftClickTransferState.observeShiftDown(Screen.hasShiftDown());
        if (!ForgeWorkspaceClient.markWantedDown()) {
            markWantedKeyConsumed = false;
        }
        if (!ForgeWorkspaceClient.setWantedHoverDown()) {
            setWantedHoverKeyConsumed = false;
        }
        if (!ForgeWorkspaceClient.storageXrayDown()) {
            storageXrayKeyConsumed = false;
        }
        openSessionIfNeeded();
        flushWheelTransferBatch();
        applyGoalStateIfChanged();
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
        if (!goalTabActive() && completePendingHomeDrag(releaseIsland)) {
            return true;
        }
        if (!goalTabActive() && !hadActivePointerGesture && button == 0 && isCursorCarrying()
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
        if (handleFocusHoveredItemKey(keyCode)) {
            return true;
        }
        if (handleAutoHotbarKey(keyCode)) {
            return true;
        }
        if (handleSetWantedHoverKey(keyCode, scanCode)) {
            return true;
        }
        if (handleMarkWantedKey(keyCode, scanCode)) {
            return true;
        }
        if (handleStorageXrayKey(keyCode, scanCode)) {
            return true;
        }
        if (handleGoalRecipeKey(keyCode)) {
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

    private boolean handleGoalRecipeKey(int keyCode) {
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
        if (goalTabActive()) {
            setStatus("goal tab is browse only");
            return true;
        }
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
        if (goalTabActive()) {
            setStatus("goal tab is browse only");
            return true;
        }
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
        if (goalTabActive()) {
            setStatus("goal tab is browse only");
            return true;
        }
        applySearchDecision(WorkspaceSearchInputPolicy.confirmForHotbar(searchActive, searchQuery));
        sendIdentityToAutoHotbar(identity);
        return true;
    }

    private boolean handleFocusHoveredItemKey(int keyCode) {
        if (keyCode != GLFW.GLFW_KEY_TAB || !Screen.hasShiftDown() || editorOpen() || Screen.hasControlDown()) {
            return false;
        }
        SlotWorkspaceViewModel.AtlasItem item = hoveredIdentity == null ? null : byIdentity.get(hoveredIdentity);
        if (item == null) {
            return false;
        }
        focusWallItem(item);
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
            status = active == null ? "activate a kit first" : "kit has one page";
            rebuildRequested = true;
            return true;
        }
        sendKitAction(WorkspaceActionId.SWITCH_KIT_PAGE, "switching kit page", direction);
        return true;
    }

    private boolean gatherActiveKitFromKey() {
        if (goalTabActive()) {
            status = "goal tab is browse only";
            rebuildRequested = true;
            return true;
        }
        if (!anyGatherableIdentity()) {
            status = "nothing to gather";
            rebuildRequested = true;
            return true;
        }
        sendAction(WorkspaceActionId.GATHER_ACTIVE_KIT, "gathering desired items from nearby chests");
        return true;
    }

    public boolean charTyped(char codePoint) {
        return charTyped(codePoint, false);
    }

    public boolean charTyped(char codePoint, boolean hostTextInputFocused) {
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
        if (GoalWorkspaceClientState.hydratePersistedGoalsIfEmpty(viewModel.goalPlans())) {
            appliedGoalStateRevision = GoalWorkspaceClientState.revision();
        }
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
        } else if (goalTabActive()) {
            GoalWorkspaceProjection goal = goalProjection();
            if (goal != null) {
                islands.addAll(goal.islands());
                items.addAll(goal.atlasItems());
            }
        } else {
            islands.addAll(viewModel.islands());
            items.addAll(viewModel.atlasItems());
        }
        for (SlotWorkspaceViewModel.AtlasItem item : items) {
            byIdentity.put(item.identity(), item);
        }
        if (!goalTabActive()) {
            for (SlotWorkspaceViewModel.AtlasItem item : viewModel.triageItems()) {
                byIdentity.putIfAbsent(item.identity(), item);
            }
        }
    }

    private void applyGoalStateIfChanged() {
        int nextRevision = GoalWorkspaceClientState.revision();
        if (nextRevision == appliedGoalStateRevision) {
            return;
        }
        appliedGoalStateRevision = nextRevision;
        refreshPresentedItems();
        rebuildRequested = true;
    }

    private GoalWorkspaceProjection goalProjection() {
        return goalProjectionCache.get(viewModel);
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

    private boolean goalTabActive() {
        return !recipeSidebarActive() && GoalWorkspaceClientState.hasActiveGoal();
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
            return root;
        }
        return workspaceColumn(false);
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
        column.addChild(goalTabs(sidebarMode));
        column.addChild(recents(sidebarMode));
        SlotUiElement suggestions = contextualSuggestions(sidebarMode);
        if (suggestions != null) {
            column.addChild(suggestions);
        }
        column.addChild(wallArea(sidebarMode));
        column.addChild(statusRow(sidebarMode));
        column.addChild(hotbar(sidebarMode));
        SlotUiElement overlay = activeOverlay();
        if (overlay != null) {
            column.addChild(overlay);
        }
        return column;
    }

    private SlotUiElement goalTabs(boolean sidebarMode) {
        return SlotUiElement.element()
                .layout(layout -> {
                    if (sidebarMode) {
                        layout.widthPercent(100);
                    } else {
                        layout.width(workspaceWidth());
                    }
                    layout.height(Math.max(GoalTabsUiBuilder.TAB_ROW_HEIGHT_PX, KitRackUiBuilder.CLUSTER_HEIGHT_PX))
                            .gapAll(4)
                            .alignItems(SlotUiLayout.AlignItems.CENTER)
                            .flexDirection(SlotUiLayout.FlexDirection.ROW);
                })
                .addChild(new GoalTabsUiBuilder(new GoalTabsContext()).tabs()
                        .layout(layout -> layout.flex(1).height(GoalTabsUiBuilder.TAB_ROW_HEIGHT_PX)))
                .addChild(new KitRackUiBuilder(new KitContext())
                        .cluster(viewModel, kitRackOpen, true)
                        .layout(layout -> layout.height(KitRackUiBuilder.CLUSTER_HEIGHT_PX)));
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
        row.addChild(storageXrayToggleButton("N", StorageGhostRevealMode.PROXIMATE));
        row.addChild(storageXrayToggleButton("T", StorageGhostRevealMode.TRACKED));
        row.addChild(gatherButton());
        row.addChild(depositButton());
        return row;
    }

    private SlotUiElement searchBarLabel(String text, int color) {
        return SlotUiElement.label(text, color)
                .on(SlotUiEventKind.MOUSE_DOWN, event -> {
                    if (event.button() != 1) {
                        return;
                    }
                    event.stopPropagation();
                    clearSearchFromPointer();
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
                        "Pull target-count gaps and active-kit needs from nearby chests.")
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
                    sendAction(WorkspaceActionId.GATHER_ACTIVE_KIT, "gathering desired items from nearby chests");
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

    private SlotUiElement recents(boolean sidebarMode) {
        SlotUiElement strip = new RecentsStripUiBuilder(new RecentsContext()).overlay(recents);
        enrichRecentCards(strip);
        if (sidebarMode) {
            return strip;
        }
        return SlotUiElement.element()
                .layout(layout -> layout.width(workspaceWidth()))
                .addChild(strip);
    }

    private SlotUiElement contextualSuggestions(boolean sidebarMode) {
        if (goalTabActive() || recipeSidebarActive()) {
            return null;
        }
        boolean filtering = !normalizedSearchQuery().isBlank();
        WallSectionHeaderUiBuilder headerBuilder = new WallSectionHeaderUiBuilder(new HeaderContext());
        WallSectionUiBuilder sectionBuilder = new WallSectionUiBuilder(headerBuilder);
        SlotUiElement lanes = SlotUiElement.element()
                .layout(layout -> {
                    if (sidebarMode) {
                        layout.widthPercent(100);
                    } else {
                        layout.width(workspaceWidth());
                    }
                    layout.gapAll(4)
                            .flexDirection(SlotUiLayout.FlexDirection.COLUMN);
                });
        for (SlotWorkspaceViewModel.ContextualSuggestionLane lane : viewModel.contextualSuggestionLanes()) {
            SlotWorkspaceViewModel.ContextualSuggestionLane visibleLane = visibleSuggestionLane(lane, filtering);
            if (visibleLane.displayable()) {
                lanes.addChild(enrichSection(sectionBuilder.suggestionLane(visibleLane)));
            }
        }
        return lanes.children().isEmpty() ? null : lanes;
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
            if (storageGhostRevealMode.revealsProximate()
                    || WallSectionVisibility.classify(
                            visibleItemsFor(island, filtering),
                            filtering,
                            storageGhostSectionExpanded(island.islandId()),
                            storageGhostRevealMode,
                            goalTabActive()).hasVisibleContent()) {
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
                                goalTabActive())));
            }
        }
        viewport.addChild(content);
        return viewport;
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
        if (storageGhostRevealMode.revealsProximate()) {
            return true;
        }
        return WallSectionVisibility.classify(
                visibleItems,
                filtering,
                storageGhostSectionExpanded(island.islandId()),
                storageGhostRevealMode,
                goalTabActive()).hasVisibleContent();
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
                .addChild(SlotUiElement.label(status, WorkspaceUiPalette.MUTED)
                        .layout(layout -> layout.flex(1).heightPercent(100))
                        .textStyle(style -> style
                                .color(WorkspaceUiPalette.MUTED)
                                .fontSize(6)
                                .horizontal(SlotUiTextStyle.Horizontal.LEFT)
                                .vertical(SlotUiTextStyle.Vertical.CENTER)));
    }

    private SlotUiElement activeOverlay() {
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
        float x = mode == Mode.SIDEBAR
                ? Math.max(4f, Math.min(screenX, workspaceWidth() - width - 4f))
                : Math.max(4f, screenX);
        float y = Math.max(8f, screenY - 4f);
        return SlotUiElement.panel(0xF00B1117)
                .zIndex(501)
                .layout(layout -> layout
                        .positionType(SlotUiLayout.PositionType.ABSOLUTE)
                        .left(x)
                        .top(y)
                        .width(width)
                        .paddingAll(6)
                        .gapAll(4)
                        .flexDirection(SlotUiLayout.FlexDirection.COLUMN))
                .on(SlotUiEventKind.MOUSE_DOWN, event -> event.stopPropagation(), true);
    }

    private SlotUiElement itemContextOverlay() {
        SlotWorkspaceViewModel.AtlasItem item = byIdentity.get(contextMenuIdentity);
        if (item == null) {
            contextMenuIdentity = null;
            return null;
        }
        if (recipeSidebarActive()) {
            return recipeItemContextOverlay(item);
        }
        if (goalTabActive()) {
            return goalItemContextOverlay(item);
        }
        SlotUiElement overlay = overlayRoot();
        SlotUiElement panel = overlayPanel(contextMenuX, contextMenuY, 174);
        panel.addChild(menuLabel(shorten(item.name(), 30), WorkspaceUiPalette.TEXT));
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
        for (SlotWorkspaceViewModel.AtlasIsland island : rehomeMenuTargets(item)) {
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

    private SlotUiElement recipeItemContextOverlay(SlotWorkspaceViewModel.AtlasItem item) {
        SlotUiElement overlay = overlayRoot();
        SlotUiElement panel = overlayPanel(contextMenuX, contextMenuY, 174);
        panel.addChild(menuLabel(shorten(item.name(), 30), WorkspaceUiPalette.ACCENT));
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
        panel.addChild(menuButton(
                "Open recipe in EMI",
                true,
                "Delegate recipe details to EMI",
                closeThen(() -> openRecipe(item))));
        panel.addChild(menuButton(
                "Open uses in EMI",
                true,
                "Delegate usage details to EMI",
                closeThen(() -> openUses(item))));
        panel.addChild(menuButton("Close", true, "Close", this::closeOverlays));
        overlay.addChild(panel);
        return overlay;
    }

    private SlotUiElement islandOrderOverlay() {
        SlotWorkspaceViewModel.AtlasIsland island = island(contextMenuIslandId);
        if (island == null || island.kind() != VisualAtlasIslandKind.PLAYER || goalTabActive()) {
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
        SlotUiElement panel = overlayPanel(contextMenuX, contextMenuY, 178);
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

    private SlotUiElement goalItemContextOverlay(SlotWorkspaceViewModel.AtlasItem item) {
        SlotUiElement overlay = overlayRoot();
        SlotUiElement panel = overlayPanel(contextMenuX, contextMenuY, 178);
        panel.addChild(menuLabel(shorten(item.name(), 30), WorkspaceUiPalette.ACCENT));
        panel.addChild(menuButton(
                "Open recipe in EMI",
                true,
                "Delegate recipe details to EMI",
                closeThen(() -> openGoalRecipe(item))));
        panel.addChild(menuButton(
                "Open uses in EMI",
                true,
                "Delegate usage details to EMI",
                closeThen(() -> openGoalUses(item))));
        GoalWorkspaceProjection goal = goalProjection();
        if (goal != null && goal.hasChoiceControls(item)) {
            List<GoalStackDescriptor> alternatives = goal.choiceAlternatives(item);
            if (!alternatives.isEmpty()) {
                panel.addChild(menuLabel("Use ingredient", WorkspaceUiPalette.MUTED));
                for (GoalStackDescriptor alternative : alternatives.stream().limit(8).toList()) {
                    panel.addChild(menuButton(
                            shorten(alternative.displayName(), 26),
                            true,
                            "Use this item for the recipe alternative",
                            closeThen(() -> chooseGoalAlternative(item, alternative))));
                }
            }
            panel.addChild(menuButton(
                    alternatives.isEmpty() ? "Choose recipe in EMI" : "Browse in EMI",
                    true,
                    "Delegate this recipe alternative to EMI",
                    closeThen(() -> openGoalChoiceEditor(item))));
            if (goal.hasManualChoice(item)) {
                panel.addChild(menuButton(
                        "Clear manual choice",
                        true,
                        "Clear this manual recipe alternative",
                        closeThen(() -> clearGoalChoice(item))));
            }
        }
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
        SlotUiElement panel = overlayPanel(contextMenuX, contextMenuY, 178);
        panel.addChild(menuLabel(shorten(kit.name(), 30), WorkspaceUiPalette.ACCENT));
        if (kit.kitId().equals(renamingKitId)) {
            panel.addChild(menuLabel("Name: " + renameKitDraft + "_", WorkspaceUiPalette.TEXT));
            panel.addChild(menuButton("Save", true, "Rename this kit", this::commitKitRenameEdit));
            panel.addChild(menuButton("Cancel", true, "Close", this::closeOverlays));
        } else if (kit.kitId().equals(confirmDeleteKitId)) {
            panel.addChild(menuLabel("Delete this kit?", WorkspaceUiPalette.MUTED));
            panel.addChild(menuButton(
                    "Delete",
                    true,
                    "Delete this kit",
                    closeThen(() -> sendKitAction(WorkspaceActionId.DELETE_KIT, "deleting kit", kit.kitId()))));
            panel.addChild(menuButton("Cancel", true, "Close", () -> {
                confirmDeleteKitId = null;
                rebuildRequested = true;
            }));
        } else {
            panel.addChild(menuButton("Rename...", true, "Rename this kit", () -> beginKitRenameEdit(kit)));
            panel.addChild(menuButton(
                    "Duplicate",
                    true,
                    "Duplicate this kit",
                    closeThen(() -> sendKitAction(WorkspaceActionId.DUPLICATE_KIT, "duplicating kit", kit.kitId()))));
            panel.addChild(menuButton("Delete...", true, "Delete this kit", () -> {
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

    private SlotUiElement chestContextOverlay() {
        SlotWorkspaceViewModel.ChestChip chip = viewModel.chestChip(contextMenuChestStorageId);
        if (chip == null) {
            closeOverlayState();
            return null;
        }
        SlotUiElement overlay = overlayRoot();
        SlotUiElement panel = overlayPanel(contextMenuX, contextMenuY, 178);
        String label = chip.label().isBlank() ? "Chest" : chip.label();
        panel.addChild(menuLabel(shorten(label, 30), WorkspaceUiPalette.ACCENT));
        if (chip.storageId().equals(renamingChestStorageId)) {
            panel.addChild(menuLabel("Name: " + renameChestDraft + "_", WorkspaceUiPalette.TEXT));
            panel.addChild(menuButton("Save", true, "Rename this chest", this::commitChestRenameEdit));
            panel.addChild(menuButton("Cancel", true, "Close", this::closeOverlays));
        } else {
            panel.addChild(menuButton("Rename...", true, "Rename this chest", () -> beginChestRenameEdit(chip)));
            panel.addChild(menuButton(
                    "Forget chest",
                    true,
                    "Forget this claimed chest",
                    closeThen(() -> sendAction(WorkspaceActionId.FORGET_CHEST, "forgetting chest", chip.storageId()))));
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
        SlotUiElement panel = overlayPanel(contextMenuX, contextMenuY, 170);
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
        SlotUiElement panel = overlayPanel(islandEditX, islandEditY, 178);
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
                        .horizontal(SlotUiTextStyle.Horizontal.CENTER)
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
        }
        installSectionHomeTarget(header, island);
        if (grids.isEmpty() && suggestionGrids.isEmpty()) {
            return section;
        }
        WallCardUiBuilder cardBuilder = new WallCardUiBuilder(new CardContext());
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
            addNearbyChip(grid, gridIsland == null ? island : gridIsland);
        }
        return section;
    }

    private void addNearbyChip(SlotUiElement grid, SlotWorkspaceViewModel.AtlasIsland island) {
        if (grid == null || island == null) {
            return;
        }
        Integer count = grid.attachment(WorkspaceUiAttachments.WALL_SECTION_NEARBY_CHIP_COUNT, Integer.class);
        if (count == null || count <= 0) {
            return;
        }
        Boolean expanded = grid.attachment(WorkspaceUiAttachments.WALL_SECTION_NEARBY_CHIP_EXPANDED, Boolean.class);
        SlotUiElement chip = WallSectionUiBuilder.nearbyChip(island, count, Boolean.TRUE.equals(expanded));
        chip.on(SlotUiEventKind.CLICK, event -> {
            event.stopPropagation();
            toggleStorageGhostSection(island.islandId());
        });
        grid.addChild(chip);
    }

    private SlotUiElement enrichCard(SlotUiElement card, SlotWorkspaceViewModel.AtlasItem item) {
        card.on(SlotUiEventKind.MOUSE_DOWN, event -> {
            if (goalTabActive()) {
                event.stopPropagation();
                SlotWorkspaceViewModel.AtlasItem target = freshItem(item);
                if (event.button() == 1 && event.shiftDown()) {
                    setStatus("goal tab is browse only");
                } else if (event.button() == 1 && !isCursorCarrying()) {
                    openItemContextMenu(target, event.x(), event.y());
                } else if (isCursorCarrying()) {
                    setStatus("goal tab is browse only");
                }
                return;
            }
            if (event.button() == 0) {
                beginHomeDragCandidate(freshItem(item));
                event.stopPropagation();
                return;
            }
            SlotWorkspaceViewModel.AtlasItem target = freshItem(item);
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
            if (goalTabActive()) {
                SlotWorkspaceViewModel.AtlasItem target = freshItem(item);
                setStatus((target == null ? "goal card" : target.name()) + " in active goal");
                return;
            }
            SlotWorkspaceViewModel.AtlasItem target = freshItem(item);
            WallCardTransferGesturePolicy.Decision decision = WallCardTransferGesturePolicy.click(
                    cardGestureContext(target, event.button(), event.shiftDown()));
            dispatchCardGestureDecision(target, decision);
        });
        card.on(SlotUiEventKind.MOUSE_WHEEL, event -> {
            boolean controlDown = event.controlDown() || Screen.hasControlDown();
            boolean wantedAdjustDown = ForgeWorkspaceClient.markWantedDown();
            if (goalTabActive()) {
                if (!controlDown || isCursorCarrying()) {
                    return;
                }
                float delta = event.wheelDelta();
                if (delta == 0f) {
                    return;
                }
                event.stopPropagation();
                adjustGoalTargetCount("", delta > 0f ? 1 : -1);
                return;
            }
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
            SlotWorkspaceViewModel.AtlasItem target = freshItem(item);
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
        if (goalTabActive()) {
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
        if (goalTabActive()) {
            return false;
        }
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

    private void openKitContextMenu(String kitId, float screenX, float screenY) {
        if (kitId == null || kitId.isBlank()) {
            setStatus("missing kit");
            return;
        }
        contextMenuKitId = kitId;
        contextMenuIdentity = null;
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
        status = "kit menu";
        rebuildRequested = true;
    }

    private void openChestContextMenu(String storageId, float screenX, float screenY) {
        if (storageId == null || storageId.isBlank()) {
            setStatus("missing chest");
            return;
        }
        contextMenuChestStorageId = storageId;
        contextMenuIdentity = null;
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
        if (goalTabActive()) {
            setStatus("goal tab is browse only");
            return;
        }
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
            setStatus("missing kit");
            return;
        }
        renamingKitId = kit.kitId();
        renameKitDraft = kit.name();
        confirmDeleteKitId = null;
        status = "renaming kit";
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
            setStatus("kit name required");
            return;
        }
        String kitId = renamingKitId;
        closeOverlayState();
        sendKitAction(WorkspaceActionId.RENAME_KIT, "renaming kit", kitId, label);
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
        contextMenuIdentity = null;
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
        if (goalTabActive()) {
            setStatus("goal tab is browse only");
            return;
        }
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
        boolean sent = actionChannel.send(action, args);
        status = sent ? sentStatus : "failed to send " + action.name().toLowerCase();
        rebuildRequested = true;
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

    private void focusWallItem(SlotWorkspaceViewModel.AtlasItem item) {
        hoveredIdentity = item == null ? null : item.identity();
        if (item != null && tree != null) {
            tree.scrollToElementId(item.islandId());
            rememberWallScroll();
        }
        setStatus(item == null ? "ready" : item.name());
    }

    private void setStatus(String nextStatus) {
        status = nextStatus == null || nextStatus.isBlank() ? "ready" : nextStatus;
        rebuildRequested = true;
    }

    private void selectAllTab() {
        GoalWorkspaceClientState.selectAll();
        refreshPresentedItems();
        setStatus("showing all items");
    }

    private void selectGoalTab(String goalId) {
        if (!GoalWorkspaceClientState.selectGoal(goalId)) {
            setStatus("goal tab no longer exists");
            return;
        }
        refreshPresentedItems();
        GoalWorkspaceClientState.GoalTab active = GoalWorkspaceClientState.activeGoal();
        setStatus("showing " + (active == null ? "goal" : active.label()));
    }

    private void removeGoalTab(String goalId) {
        if (!GoalWorkspaceClientState.removeGoal(goalId)) {
            setStatus("goal tab no longer exists");
            return;
        }
        GoalWorkspaceIntegration.removePersistedGoal(goalId);
        refreshPresentedItems();
        setStatus(goalTabActive() ? "removed goal" : "showing all items");
    }

    private void adjustGoalTargetCount(String goalId, int delta) {
        if (!GoalWorkspaceClientState.adjustTargetCount(goalId, delta)) {
            return;
        }
        refreshPresentedItems();
        GoalWorkspaceClientState.GoalTab active = GoalWorkspaceClientState.activeGoal();
        GoalWorkspaceIntegration.persistGoal(active);
        setStatus(active == null ? "updated goal" : active.label() + " x" + active.targetCount());
    }

    private void openGoalRecipe(SlotWorkspaceViewModel.AtlasItem item) {
        GoalWorkspaceClientState.GoalTab active = GoalWorkspaceClientState.activeGoal();
        GoalProjectionEntry entry = goalEntry(item);
        if (active == null) {
            setStatus("no active goal");
        } else if (GoalWorkspaceIntegration.openRecipe(active.descriptor(), entry)) {
            setStatus("opened recipe in EMI");
        } else {
            setStatus("EMI recipe display unavailable");
        }
    }

    private void openRecipe(SlotWorkspaceViewModel.AtlasItem item) {
        if (goalTabActive()) {
            openGoalRecipe(item);
            return;
        }
        ItemIdentity identity = item == null ? null : item.identity().toIdentity();
        if (identity == null) {
            setStatus("item unavailable");
        } else if (GoalWorkspaceIntegration.openRecipe(identity)) {
            setStatus("opened recipe in EMI");
        } else {
            setStatus("EMI recipe display unavailable");
        }
    }

    private void openGoalUses(SlotWorkspaceViewModel.AtlasItem item) {
        GoalWorkspaceProjection goal = goalProjection();
        ItemIdentity identity = goal == null ? (item == null ? null : item.identity().toIdentity()) : goal.delegationIdentity(item);
        if (identity == null) {
            setStatus("item unavailable");
        } else if (GoalWorkspaceIntegration.openUses(identity)) {
            setStatus("opened uses in EMI");
        } else {
            setStatus("EMI usage display unavailable");
        }
    }

    private void openUses(SlotWorkspaceViewModel.AtlasItem item) {
        openGoalUses(item);
    }

    private void openGoalChoiceEditor(SlotWorkspaceViewModel.AtlasItem item) {
        GoalWorkspaceClientState.GoalTab active = GoalWorkspaceClientState.activeGoal();
        String choiceGroupId = goalChoiceGroupId(item);
        GoalProjectionEntry entry = goalEntry(item);
        if (active == null || choiceGroupId.isBlank()) {
            setStatus("goal choice unavailable");
        } else if (GoalWorkspaceIntegration.openChoiceEditor(active.descriptor(), entry)) {
            setStatus("choose recipe in EMI");
        } else {
            setStatus("EMI choice display unavailable");
        }
    }

    private void clearGoalChoice(SlotWorkspaceViewModel.AtlasItem item) {
        GoalWorkspaceClientState.GoalTab active = GoalWorkspaceClientState.activeGoal();
        String choiceGroupId = goalChoiceGroupId(item);
        if (active == null || choiceGroupId.isBlank()) {
            setStatus("goal choice unavailable");
        } else if (GoalWorkspaceClientState.clearManualChoice(active.goalId(), choiceGroupId)) {
            setStatus("cleared goal choice");
            GoalWorkspaceIntegration.persistGoal(GoalWorkspaceClientState.goalTab(active.goalId()));
        } else {
            setStatus("no manual choice to clear");
        }
        refreshPresentedItems();
    }

    private void chooseGoalAlternative(SlotWorkspaceViewModel.AtlasItem item, GoalStackDescriptor alternative) {
        GoalWorkspaceClientState.GoalTab active = GoalWorkspaceClientState.activeGoal();
        String choiceGroupId = goalChoiceGroupId(item);
        if (active == null || choiceGroupId.isBlank() || alternative == null) {
            setStatus("goal choice unavailable");
        } else if (GoalWorkspaceClientState.setManualChoice(active.goalId(), choiceGroupId, alternative.identity())) {
            setStatus("using " + alternative.displayName());
            GoalWorkspaceIntegration.persistGoal(GoalWorkspaceClientState.goalTab(active.goalId()));
        } else {
            setStatus("could not update goal choice");
        }
        refreshPresentedItems();
    }

    private String goalChoiceGroupId(SlotWorkspaceViewModel.AtlasItem item) {
        GoalProjectionEntry entry = goalEntry(item);
        return entry == null ? "" : entry.choiceGroupId();
    }

    private GoalProjectionEntry goalEntry(SlotWorkspaceViewModel.AtlasItem item) {
        GoalWorkspaceProjection goal = goalProjection();
        return goal == null ? null : goal.entry(item);
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
        status = sentStatus == null ? "" : sentStatus;
        rebuildRequested = true;
    }

    private void flushWheelTransferBatch() {
        flushWheelTransfer(wheelTransferBatcher.flush());
    }

    private void flushWheelTransfer(WheelTransferBatcher.Pending pending) {
        if (pending == null || pending.identity() == null || pending.count() <= 0) {
            return;
        }
        sendIdentityRefAction(pending.action(), pending.identity(), pending.status(), pending.count());
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
            setStatus("invalid kit identity");
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

    public enum Mode {
        STANDALONE,
        SIDEBAR
    }

    private final class GoalTabsContext implements GoalTabsUiBuilder.Context {
        @Override
        public boolean goalActive() {
            return goalTabActive();
        }

        @Override
        public List<GoalTabsUiBuilder.GoalTab> goalTabs() {
            ArrayList<GoalTabsUiBuilder.GoalTab> tabs = new ArrayList<>();
            for (GoalWorkspaceClientState.GoalTab tab : GoalWorkspaceClientState.goalTabs()) {
                String status = "";
                if (tab.active()) {
                    GoalWorkspaceProjection projection = goalProjection();
                    status = projection == null ? "" : projection.projection().status().name();
                }
                tabs.add(new GoalTabsUiBuilder.GoalTab(
                        tab.goalId(),
                        tab.label(),
                        tab.targetCount(),
                        status,
                        tab.active()));
            }
            return List.copyOf(tabs);
        }

        @Override
        public void selectAll() {
            selectAllTab();
        }

        @Override
        public void selectGoal(String goalId) {
            selectGoalTab(goalId);
        }

        @Override
        public void removeGoal(String goalId) {
            removeGoalTab(goalId);
        }

        @Override
        public void adjustGoalTargetCount(String goalId, int delta) {
            ForgeWorkspaceSurface.this.adjustGoalTargetCount(goalId, delta);
        }
    }

    private final class ActiveChestContext implements ActiveChestStripUiBuilder.Context {
        @Override
        public void claimChestAt(SlotWorkspaceViewModel.ActiveChestPanel panel) {
            if (panel == null || !panel.isPresent()) {
                setStatus("no active chest to claim");
                return;
            }
            sendAction(
                    WorkspaceActionId.CLAIM_CHEST_AT_POS,
                    "claiming chest",
                    panel.dimensionId(),
                    panel.posX(),
                    panel.posY(),
                    panel.posZ());
        }

        @Override
        public void forgetChest(String storageId) {
            if (storageId == null || storageId.isBlank()) {
                setStatus("no claimed chest to forget");
                return;
            }
            sendAction(WorkspaceActionId.FORGET_CHEST, "forgetting chest", storageId);
        }

        @Override
        public void openChestMenu(String storageId, float screenX, float screenY) {
            ForgeWorkspaceSurface.this.openChestContextMenu(storageId, screenX, screenY);
        }
    }

    private final class HeaderContext implements WallSectionHeaderUiBuilder.Context {
        @Override
        public void beginIslandEdit(SlotWorkspaceViewModel.AtlasIsland island, float screenX, float screenY) {
            if (goalTabActive()) {
                setStatus("goal tab is browse only");
                return;
            }
            ForgeWorkspaceSurface.this.beginIslandEdit(island, screenX, screenY);
        }
    }

    private final class KitContext implements KitRackUiBuilder.Context {
        @Override
        public void toggleKitRack() {
            kitRackOpen = !kitRackOpen;
            setStatus(kitRackOpen
                    ? "kit rack open (" + viewModel.kits().size() + ")"
                    : "kit rack closed");
        }

        @Override
        public void closeKitRack() {
            kitRackOpen = false;
            setStatus("kit rack closed");
        }

        @Override
        public void saveCurrentBeltAsKit() {
            sendKitAction(WorkspaceActionId.SAVE_KIT, "saving belt as kit", "");
        }

        @Override
        public void activateKit(String kitId) {
            sendKitAction(WorkspaceActionId.ACTIVATE_KIT, "activating kit", kitId);
        }

        @Override
        public void deactivateKit() {
            sendKitAction(WorkspaceActionId.DEACTIVATE_KIT, "deactivating kit");
        }

        @Override
        public void switchActiveKitPage(int direction) {
            sendKitAction(WorkspaceActionId.SWITCH_KIT_PAGE, "switching kit page", direction);
        }

        @Override
        public void addKitPage(String kitId) {
            sendKitAction(WorkspaceActionId.ADD_KIT_PAGE, "adding kit page", kitId);
        }

        @Override
        public void removeKitPage(String kitId, int pageIndex) {
            sendKitAction(WorkspaceActionId.REMOVE_KIT_PAGE, "removing kit page", kitId, pageIndex);
        }

        @Override
        public void clearKitSlot(String kitId, int pageIndex, int slotIndex) {
            sendKitAction(
                    WorkspaceActionId.SET_KIT_SLOT_IDENTITY,
                    "clearing kit slot",
                    kitId,
                    pageIndex,
                    slotIndex,
                    "",
                    "",
                    "");
        }

        @Override
        public void clearKitBring(String kitId, SlotWorkspaceViewModel.IdentityRef identity) {
            if (identity == null) {
                setStatus("invalid kit bring item");
                return;
            }
            sendKitAction(
                    WorkspaceActionId.SET_KIT_SCOPED_DESIRED_COUNT,
                    "clearing kit desired item",
                    kitId,
                    identity.itemId(),
                    identity.comparisonMode(),
                    identity.componentFingerprint(),
                    0);
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
            return byIdentity.get(identity);
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
            if (entry.storageId() != null && entry.storageId().startsWith("goal:")) {
                return new WayfindingDisplay.CardText(">", WorkspaceCountFormat.compact(entry.count()));
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
            GoalWorkspaceProjection goal = goalProjection();
            if (SlotForgeClientConfig.contextualSuggestionDebugTooltips() && suggestionLane != null) {
                return WorkspaceItemTooltipBuilder.slotLines(
                        item,
                        suggestionLane,
                        true,
                        hasProximateDepositRoute(item));
            }
            return goal == null
                    ? WorkspaceItemTooltipBuilder.slotLines(item, hasProximateDepositRoute(item))
                    : goal.tooltipLines(item);
        }

        @Override
        public boolean choiceInvolved(SlotWorkspaceViewModel.AtlasItem item) {
            if (recipeSidebarActive()) {
                return false;
            }
            GoalWorkspaceProjection goal = goalProjection();
            return goal != null && goal.choiceInvolved(item);
        }

        @Override
        public boolean choiceCard(SlotWorkspaceViewModel.AtlasItem item) {
            if (recipeSidebarActive()) {
                return false;
            }
            GoalWorkspaceProjection goal = goalProjection();
            return goal != null && goal.choiceCard(item);
        }

        @Override
        public boolean suppressVanillaTooltip(SlotWorkspaceViewModel.AtlasItem item) {
            RecipeIngredientSidebarSpec.Projection recipe = recipeProjection();
            if (recipe != null) {
                return recipe.suppressVanillaTooltip(item);
            }
            GoalWorkspaceProjection goal = goalProjection();
            return goal != null && goal.suppressVanillaTooltip(item);
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
                    && !goalTabActive()
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
