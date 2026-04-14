package dev.imagio.slot.client.screen.debug;

import com.mojang.blaze3d.platform.InputConstants;
import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.client.SlotClientCompat;
import dev.imagio.slot.client.collection.CollectionDefinition;
import dev.imagio.slot.client.collection.CollectionStore;
import dev.imagio.slot.client.collection.HotbarLoadoutDefinition;
import dev.imagio.slot.client.collection.CollectionStockSummary;
import dev.imagio.slot.client.collection.CollectionViewStateController;
import dev.imagio.slot.capability.ToolCapabilityDescriptor;
import dev.imagio.slot.client.model.ItemEntry;
import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.client.policy.ItemBehaviorPolicy;
import dev.imagio.slot.client.screen.AbstractInventoryBrowserScreen;
import dev.imagio.slot.client.screen.ActionMenuItem;
import dev.imagio.slot.client.screen.CarriedTransferService;
import dev.imagio.slot.client.screen.ContainerFloatingItemRenderSupport;
import dev.imagio.slot.client.screen.EmiLayoutSync;
import dev.imagio.slot.client.screen.InventoryCapacityIndicator;
import dev.imagio.slot.client.screen.InventoryHotbarClickSupport;
import dev.imagio.slot.client.screen.InventoryCursorActionSupport;
import dev.imagio.slot.client.screen.InventoryCursorDropTargetSupport;
import dev.imagio.slot.client.screen.InventoryHotbarInteractionState;
import dev.imagio.slot.client.screen.InventoryItemRowClickSupport;
import dev.imagio.slot.client.screen.InventoryItemRowSupport;
import dev.imagio.slot.client.screen.InventoryProjectionSelectionList;
import dev.imagio.slot.client.screen.InventoryRowActionPlanner;
import dev.imagio.slot.client.screen.InventoryLoadoutRowSupport;
import dev.imagio.slot.client.screen.InventoryRefreshDelayState;
import dev.imagio.slot.client.screen.InventoryScreenRow;
import dev.imagio.slot.client.screen.InventorySectionScreenRow;
import dev.imagio.slot.client.screen.InventorySectionHeaderSupport;
import dev.imagio.slot.client.screen.InventorySelectionRestoreSupport;
import dev.imagio.slot.client.screen.InventoryMenuStateHash;
import dev.imagio.slot.client.screen.InventoryRailSupport;
import dev.imagio.slot.client.screen.InventoryRailSupport.Kind;
import dev.imagio.slot.client.screen.InventoryRailSupport.Target;
import dev.imagio.slot.client.screen.QuickAccessInventoryActionResult;
import dev.imagio.slot.client.screen.InventoryScreenContext;
import dev.imagio.slot.client.screen.InventorySectionActionPlanner;
import dev.imagio.slot.client.screen.InventoryTransferActionSupport;
import dev.imagio.slot.client.screen.InlineDesiredCountState;
import dev.imagio.slot.client.screen.InlineLoadoutRenameState;
import dev.imagio.slot.client.screen.NewCollectionPromptState;
import dev.imagio.slot.client.screen.PopupActionMenu;
import dev.imagio.slot.client.screen.RailScrollState;
import dev.imagio.slot.client.screen.SlotActionResult;
import dev.imagio.slot.client.screen.SlotUndoHistory;
import dev.imagio.slot.projection.InventoryPane;
import dev.imagio.slot.workflow.InspectionService;
import dev.imagio.slot.workflow.SearchWorkflowService;
import dev.imagio.slot.workflow.SettingsService;
import dev.imagio.slot.session.ChestLikeMenuLayout;
import dev.imagio.slot.session.InventoryHostToolCoordinator;
import dev.imagio.slot.client.screen.container.DockedToolPanel;
import dev.imagio.slot.client.screen.container.DockedToolPanelInteractionSupport;
import dev.imagio.slot.client.screen.container.DockedToolPanelResolver;
import dev.imagio.slot.client.screen.container.SlotBackedToolPanel;
import dev.imagio.slot.client.screen.ReflectiveContainerRenderHooks;
import dev.imagio.slot.client.screen.SlotCollectionManagementScreen;
import dev.imagio.slot.client.screen.SlotItemInspectionScreen;
import dev.imagio.slot.client.screen.SlotSettingsScreen;
import dev.imagio.slot.client.screen.SlotTrashBuffer;
import dev.imagio.slot.client.screen.SlotTrashWarningState;
import dev.imagio.slot.client.screen.SlotTooltipRenderer;
import dev.imagio.slot.network.CursorTransferPayload;
import dev.imagio.slot.network.CursorTransferRequester;
import dev.imagio.slot.projection.InventoryProjection;
import dev.imagio.slot.projection.InventoryProjectionBuilder;
import dev.imagio.slot.storage.adapter.ExternalToolKind;
import dev.imagio.slot.storage.adapter.ExternalToolSlotRole;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Supplier;
import dev.imagio.slot.projection.InventoryViewData;

public class SlotDebugInventoryScreen extends AbstractInventoryBrowserScreen<InventoryMenu> {
    private static final String ALL_TARGET_ID = "__all__";

    private static final int OUTER_MARGIN = 8;
    private static final int COLUMN_GAP = 8;
    private static final int HEADER_Y = 8;
    private static final int CONTROL_Y = 26;
    private static final int HOTBAR_Y = 46;
    private static final int HOTBAR_SLOT_SIZE = 16;
    private static final int HOTBAR_SLOT_GAP = 1;
    private static final int HOTBAR_OFFHAND_GAP = 4;
    private static final int HOTBAR_SECTION_GAP = 8;
    private static final int DOCKED_TOOL_PANEL_GAP = 4;
    private static final int MIN_RAIL_WIDTH = 96;
    private static final int MAX_RAIL_WIDTH = 160;
    private static final int ICON_BUTTON_WIDTH = 14;
    private static final int SORT_BUTTON_WIDTH = 20;
    private static final int TOP_CONTROL_HEIGHT = 14;
    private static final int SEARCH_BOX_HEIGHT = 16;
    private static final int TOP_CONTROL_GAP = 3;
    private static final int ROW_ACTION_COLUMN_WIDTH = 18;
    private static final int ROW_COUNT_COLUMN_WIDTH = 32;
    private static final int ROW_LEADING_COUNT_COLUMN_WIDTH = 18;
    private static final int ROW_HEIGHT = 13;
    private static final int RAIL_ROW_HEIGHT = 13;
    private static final int COLLECTION_BUTTON_HEIGHT = 9;
    private static final int COLLECTION_BUTTON_GAP = 3;
    private static final int COLLECTION_PIN_BUTTON_WIDTH = 16;
    private static final int COLLECTION_TOGGLE_BUTTON_WIDTH = 12;
    private static final int COLLECTION_MENU_BUTTON_WIDTH = 20;
    private static final int COLLECTION_SAVE_BUTTON_WIDTH = 24;
    private static final int TRASH_SLOT_SIZE = 12;
    private static final int TRASH_SLOT_GAP = 6;
    private static final int COLLECTION_CREATE_PROMPT_WIDTH = 228;
    private static final int COLLECTION_CREATE_PROMPT_HEIGHT = 60;
    private static final int COLLECTION_CREATE_PROMPT_BUTTON_WIDTH = 56;
    private static final int LOADOUT_ARROW_WIDTH = 10;
    private static final int LOADOUT_NAME_WIDTH = 64;
    private static final int LOADOUT_BUTTON_WIDTH = 18;
    private static final int LOADOUT_SLOT_SIZE = 10;
    private static final int LOADOUT_SLOT_GAP = 1;
    private static final int LOADOUT_OFFHAND_GAP = 3;
    private static final int MAX_PANEL_WIDTH = 900;
    private static final int EMI_MAX_PANEL_WIDTH = 720;
    private static final int MAX_PANEL_HEIGHT = 560;
    private static final int BASE_LEFT_MARGIN = 40;
    private static final int BASE_RIGHT_MARGIN = 40;
    private static final int EMI_LEFT_MARGIN = 50;
    private static final int EMI_CONTENT_GAP = 4;
    private static final int EMI_RIGHT_GUTTER_MIN = 224;
    private static final int EMI_RIGHT_GUTTER_MAX = 288;
    private static final float ROW_TEXT_SCALE = 0.60F;
    private static final float RAIL_TEXT_SCALE = 0.72F;
    private static final float ROW_ITEM_SCALE = 0.62F;
    private static final int ROW_ITEM_SIZE = Math.round(16 * ROW_ITEM_SCALE);
    private static final ReflectiveContainerRenderHooks CONTAINER_RENDER_HOOKS = ReflectiveContainerRenderHooks.create();

    private final Screen parentScreen;
    private final InventoryViewDataBuilder dataBuilder = new InventoryViewDataBuilder();
    private final CarriedTransferService actionExecutor;
    private final Runnable openVanillaAction;
    private final boolean closeOnInventoryKey;
    private final boolean emiPresent;
    private final SettingsService settingsController;
    private final Runnable currentScreenToggleAction;
    private ToolCapabilityDescriptor craftingUpgradePanelRef;
    private DockedToolPanel dockedToolPanel;

    private InventoryProjectionSelectionList<InventoryScreenRow> entryList;
    private List<Target> railTargets = List.of();
    private Map<String, Integer> visibleSectionCounts = Map.of();
    private InventoryProjection inventoryProjection = InventoryProjection.empty();
    private final RailScrollState railScrollState = new RailScrollState();
    private final InventoryHotbarInteractionState hotbarInteractionState = new InventoryHotbarInteractionState();
    private int lastMouseX;
    private int lastMouseY;
    private boolean panelGeometryInitialized;
    private boolean pendingEmiLayoutRefresh;
    private boolean craftingUpgradeOpenRequested;
    private int lastObservedMenuStateHash;
    private final InventoryRefreshDelayState postActionRefreshDelay = new InventoryRefreshDelayState();
    private InventoryCapacityIndicator.StorageFillStats carriedCapacity = InventoryCapacityIndicator.StorageFillStats.EMPTY;
    private int visibleItemResultCount;
    private static final SearchWorkflowService.SearchPeer EMI_SEARCH_PEER = new SearchWorkflowService.SearchPeer() {
        @Override
        public boolean available() {
            return EmiLayoutSync.hasSearchWidget();
        }

        @Override
        public String query() {
            return EmiLayoutSync.currentSearchQuery();
        }

        @Override
        public void setQuery(String query) {
            EmiLayoutSync.setSearchQuery(query);
        }
    };

    public SlotDebugInventoryScreen(Screen parentScreen, CollectionStore collectionStore, boolean emiPresent) {
        this(parentScreen, collectionStore, Component.translatable("slot.screen.debug.title"), null, false, emiPresent, null, CollectionViewStateController.NOOP, null, null);
    }

    public SlotDebugInventoryScreen(
            Screen parentScreen,
            CollectionStore collectionStore,
            boolean emiPresent,
            CollectionViewStateController collectionViewStateController
    ) {
        this(parentScreen, collectionStore, Component.translatable("slot.screen.debug.title"), null, false, emiPresent, null, collectionViewStateController, null, null);
    }

    public SlotDebugInventoryScreen(
            Screen parentScreen,
            CollectionStore collectionStore,
            boolean emiPresent,
            CollectionViewStateController collectionViewStateController,
            SettingsService settingsController,
            SearchWorkflowService searchWorkflow,
            InspectionService inspectionService
    ) {
        this(
                parentScreen,
                collectionStore,
                Component.translatable("slot.screen.debug.title"),
                null,
                false,
                emiPresent,
                settingsController,
                collectionViewStateController,
                null,
                null,
                null,
                searchWorkflow,
                inspectionService
        );
    }

    protected SlotDebugInventoryScreen(
            Screen parentScreen,
            CollectionStore collectionStore,
            Component title,
            Runnable openVanillaAction,
            boolean closeOnInventoryKey,
            boolean emiPresent
    ) {
        this(parentScreen, collectionStore, title, openVanillaAction, closeOnInventoryKey, emiPresent, null, CollectionViewStateController.NOOP, null, null);
    }

    protected SlotDebugInventoryScreen(
            Screen parentScreen,
            CollectionStore collectionStore,
            Component title,
            Runnable openVanillaAction,
            boolean closeOnInventoryKey,
            boolean emiPresent,
            SettingsService settingsController,
            CollectionViewStateController collectionViewStateController,
            Supplier<Component> currentScreenToggleLabel,
            Runnable currentScreenToggleAction
    ) {
        this(
                parentScreen,
                collectionStore,
                title,
                openVanillaAction,
                closeOnInventoryKey,
                emiPresent,
                settingsController,
                collectionViewStateController,
                currentScreenToggleLabel,
                currentScreenToggleAction,
                null,
                null,
                null
        );
    }

    protected SlotDebugInventoryScreen(
            Screen parentScreen,
            CollectionStore collectionStore,
            Component title,
            Runnable openVanillaAction,
            boolean closeOnInventoryKey,
            boolean emiPresent,
            SettingsService settingsController,
            CollectionViewStateController collectionViewStateController,
            Supplier<Component> currentScreenToggleLabel,
            Runnable currentScreenToggleAction,
            InventoryScreenContext screenContext,
            SearchWorkflowService searchWorkflow,
            InspectionService inspectionService
    ) {
        super(
                resolveInventoryMenu(),
                resolvePlayerInventory(),
                title,
                screenContext,
                collectionStore,
                collectionViewStateController,
                searchWorkflow,
                inspectionService
        );
        this.parentScreen = parentScreen;
        this.openVanillaAction = openVanillaAction;
        this.closeOnInventoryKey = closeOnInventoryKey;
        this.emiPresent = emiPresent || SlotClientCompat.hasEmi();
        this.settingsController = settingsController;
        this.currentScreenToggleLabel = currentScreenToggleLabel;
        this.currentScreenToggleAction = currentScreenToggleAction;
        this.actionExecutor = CarriedTransferService.forPlayer(screenContext);
        this.craftingUpgradePanelRef = resolveCraftingUpgradePanel(screenContext);
        this.dockedToolPanel = reconcileCraftingUpgradePanel(null, screenContext, craftingUpgradePanelRef);
    }

    public Screen parentScreen() {
        return parentScreen;
    }

    @Override
    protected void init() {
        refreshCraftingUpgradePanelState(false);
        this.imageWidth = desiredPanelWidth();
        this.imageHeight = desiredPanelHeight();
        super.init();
        this.leftPos = desiredPanelLeft();
        this.topPos = desiredPanelTop();
        this.panelGeometryInitialized = true;

        SlotDebugLog.log(
                "SLOT carried screen init: emiPresent={} width={} height={} reservedLeft={} reservedRight={} desiredPanelWidth={} desiredPanelHeight={} panelLeft={} panelTop={} panelWidth={} panelVisualWidth={} panelHeight={} panelRight={} panelVisualRight={} imageWidth={} imageHeight={} leftPos={} topPos={} contextCarriedOnly={}",
                emiPresent,
                width,
                height,
                reservedLeftMargin(),
                reservedRightMargin(),
                desiredPanelWidth(),
                desiredPanelHeight(),
                panelLeft(),
                panelTop(),
                panelWidth(),
                panelVisualWidth(),
                panelHeight(),
                panelRight(),
                panelVisualRight(),
                imageWidth,
                imageHeight,
                leftPos,
                topPos,
                screenContext == null || screenContext.carriedOnly()
        );

        String searchValue = searchBox == null ? searchWorkflow.currentQuery() : searchBox.getValue();

        inventoryData = dataBuilder.build(Objects.requireNonNull(minecraft).player, collectionStore, screenContext);
        railTargets = buildRailTargets();

        int centerX = centerPaneX();
        int centerWidth = centerPaneWidth();
        int listHeight = listHeight();
        int listTop = contentTop();

        int sortWidth = SORT_BUTTON_WIDTH;
        int undoWidth = ICON_BUTTON_WIDTH;
        int redoWidth = ICON_BUTTON_WIDTH;
        int collectionsWidth = ICON_BUTTON_WIDTH;
        int settingsWidth = settingsController == null ? 0 : ICON_BUTTON_WIDTH;
        int toggleWidth = currentScreenToggleAction == null ? 0 : ICON_BUTTON_WIDTH;
        int vanillaWidth = openVanillaAction == null ? 0 : ICON_BUTTON_WIDTH;

        int controlsRight = centerX + centerWidth;
        if (vanillaWidth > 0) {
            controlsRight -= vanillaWidth;
            vanillaButton = addRenderableWidget(Button.builder(Component.literal("V"), button -> openVanillaAction.run())
                    .tooltip(Tooltip.create(Component.translatable("slot.screen.inventory.open_vanilla")))
                    .bounds(controlsRight, controlY(), vanillaWidth, TOP_CONTROL_HEIGHT)
                    .build());
            controlsRight -= TOP_CONTROL_GAP;
        } else {
            vanillaButton = null;
        }

        if (toggleWidth > 0) {
            controlsRight -= toggleWidth;
            screenToggleButton = addRenderableWidget(Button.builder(Component.literal("X"), button -> currentScreenToggleAction.run())
                    .bounds(controlsRight, controlY(), toggleWidth, TOP_CONTROL_HEIGHT)
                    .build());
            controlsRight -= TOP_CONTROL_GAP;
        } else {
            screenToggleButton = null;
        }

        if (settingsWidth > 0) {
            controlsRight -= settingsWidth;
            settingsButton = addRenderableWidget(Button.builder(Component.literal("S"), button -> openSettings())
                    .tooltip(Tooltip.create(Component.translatable("slot.screen.settings.button")))
                    .bounds(controlsRight, controlY(), settingsWidth, TOP_CONTROL_HEIGHT)
                    .build());
            controlsRight -= TOP_CONTROL_GAP;
        } else {
            settingsButton = null;
        }

        collectionsButton = null;

        int searchWidth = Math.max(80, controlsRight - centerX - sortWidth - undoWidth - redoWidth - 20);
        searchBox = new EditBox(font, centerX, controlY(), searchWidth, SEARCH_BOX_HEIGHT, Component.translatable("slot.screen.debug.search"));
        searchBox.setHint(Component.translatable("slot.screen.debug.search_hint"));
        searchBox.setValue(searchValue);
        searchBox.setResponder(value -> {
            searchWorkflow.remember(value);
            rebuildVisibleEntries();
        });
        addRenderableWidget(searchBox);

        inlineLoadoutRenameBox = new EditBox(font, centerX, controlY(), 80, 14, Component.translatable("slot.screen.collections.loadout_name"));
        inlineLoadoutRenameBox.setHint(Component.translatable("slot.screen.collections.loadout_name_hint"));
        inlineLoadoutRenameBox.setMaxLength(48);
        inlineLoadoutRenameBox.setVisible(false);
        addRenderableWidget(inlineLoadoutRenameBox);

        inlineDesiredCountBox = new EditBox(font, centerX, controlY(), 40, 14, Component.translatable("slot.screen.collections.desired_count_hint"));
        inlineDesiredCountBox.setHint(Component.translatable("slot.screen.collections.desired_count_hint"));
        inlineDesiredCountBox.setMaxLength(4);
        inlineDesiredCountBox.setFilter(value -> value.chars().allMatch(Character::isDigit));
        inlineDesiredCountBox.setVisible(false);
        addRenderableWidget(inlineDesiredCountBox);

        newCollectionNameBox = new EditBox(font, centerX, controlY(), 120, SEARCH_BOX_HEIGHT, Component.translatable("slot.screen.collections.name"));
        newCollectionNameBox.setHint(Component.translatable("slot.screen.collections.name_hint"));
        newCollectionNameBox.setMaxLength(48);
        newCollectionNameBox.setVisible(false);
        addRenderableWidget(newCollectionNameBox);

        sortButton = addRenderableWidget(Button.builder(Component.empty(), button -> cycleSort())
                .bounds(searchBox.getX() + searchBox.getWidth() + TOP_CONTROL_GAP + 2, controlY(), sortWidth, TOP_CONTROL_HEIGHT)
                .build());
        undoButton = addRenderableWidget(Button.builder(Component.literal("U"), button -> undoLastAction())
                .bounds(sortButton.getX() + sortButton.getWidth() + TOP_CONTROL_GAP, controlY(), undoWidth, TOP_CONTROL_HEIGHT)
                .build());
        redoButton = addRenderableWidget(Button.builder(Component.literal("R"), button -> redoLastAction())
                .bounds(undoButton.getX() + undoButton.getWidth() + TOP_CONTROL_GAP, controlY(), redoWidth, TOP_CONTROL_HEIGHT)
                .build());

        entryList = new InventoryProjectionSelectionList<>(
                Minecraft.getInstance(),
                centerWidth,
                listHeight,
                listTop,
                centerX,
                ROW_HEIGHT,
                ALL_TARGET_ID,
                button -> button == 0 || button == 1,
                row -> row instanceof ItemRowEntry,
                row -> row instanceof ItemRowEntry,
                0x664B7F35
        );
        addWidget(entryList);
        if (dockedToolPanel != null) {
            dockedToolPanel.layout(centerX, dockedToolPanelY(), centerWidth);
        }

        sortButton.setTooltip(Tooltip.create(Component.translatable("slot.screen.debug.sort_button", sortMode.displayName())));
        updateSortButton();
        updateDynamicButtons();
        refreshInventoryData();
        searchWorkflow.initialize(searchField(), shouldSyncSearchWithEmi(), EMI_SEARCH_PEER);
        setInitialFocus(searchBox);
        pendingEmiLayoutRefresh = emiPresent;
        EmiLayoutSync.refreshIfPresent("slot_player_screen_init");
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (refreshCraftingUpgradePanelState(true)) {
            return;
        }
        searchWorkflow.tick(searchField(), shouldSyncSearchWithEmi(), EMI_SEARCH_PEER);
        if (dockedToolPanel != null) {
            dockedToolPanel.containerTick();
        }
        hotbarLoadoutController.tick();
        applyConfirmedActionOutcome();

        AbstractContainerMenu observedMenu = observedMenu();
        if (observedMenu == null) {
            return;
        }

        int currentHash = computeObservedMenuStateHash(observedMenu);
        if (currentHash != lastObservedMenuStateHash) {
            SlotDebugLog.log(
                    "Observed carried menu contents changed while SLOT screen is open: menu={} oldHash={} newHash={}",
                    observedMenu.getClass().getName(),
                    lastObservedMenuStateHash,
                    currentHash
            );
            refreshInventoryData();
            postActionRefreshDelay.clear();
            return;
        }

        if (postActionRefreshDelay.tick(false)) {
            SlotDebugLog.log(
                    "Refreshing SLOT carried view while awaiting menu sync: menu={} ticksRemaining={}",
                    observedMenu.getClass().getName(),
                    postActionRefreshDelay.ticksRemaining()
            );
            refreshInventoryData();
        }

        actionFeedback.tick();
        attemptAutoVoidJunk();
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parentScreen);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isNewCollectionPromptActive()) {
            if (minecraft != null && minecraft.options.keyInventory.matches(keyCode, scanCode)) {
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                commitNewCollectionPrompt();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                cancelNewCollectionPrompt();
                return true;
            }
            if (newCollectionNameBox.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        if (isInlineDesiredCountActive()) {
            if (minecraft != null && minecraft.options.keyInventory.matches(keyCode, scanCode)) {
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                commitInlineDesiredCount();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                cancelInlineDesiredCount();
                return true;
            }
            if (inlineDesiredCountBox.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        if (isInlineLoadoutRenameActive()) {
            if (minecraft != null && minecraft.options.keyInventory.matches(keyCode, scanCode)) {
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                commitInlineLoadoutRename();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                cancelInlineLoadoutRename();
                return true;
            }
            if (inlineLoadoutRenameBox.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        if (isSearchInputActive()
                && minecraft != null
                && minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            return true;
        }
        if (handleSearchKeyPress(keyCode)) {
            return true;
        }
        if (handleUndoRedoKeyPress(keyCode)) {
            return true;
        }
        if (handleLoadoutHotkey(keyCode)) {
            return true;
        }
        if (closeOnInventoryKey
                && !isSearchInputActive()
                && minecraft != null
                && minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        if (dockedToolPanel != null && dockedToolPanel.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }

        int hotbarIndex = hotbarKeyIndex(keyCode);
        if (hotbarIndex >= 0) {
            InventoryViewData.EntryView assignmentEntry = hotbarAssignmentEntry();
            if (assignmentEntry != null) {
                applyQuickAccessMutation(() -> hotbarLoadoutController.assignToQuickAccessMutation(assignmentEntry.itemEntry().identity(), hotbarIndex));
                return true;
            }
        }
        if (minecraft != null && minecraft.options.keySwapOffhand.matches(keyCode, scanCode)) {
            InventoryViewData.EntryView assignmentEntry = hotbarAssignmentEntry();
            if (assignmentEntry != null) {
                applyQuickAccessMutation(() -> hotbarLoadoutController.assignToQuickAccessMutation(
                        assignmentEntry.itemEntry().identity(),
                        HotbarLoadoutDefinition.OFFHAND_SLOT_INDEX
                ));
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private boolean handleLoadoutHotkey(int keyCode) {
        if (!primaryModifierDown()) {
            return false;
        }

        int hotbarIndex = hotbarKeyIndex(keyCode);
        if (hotbarIndex < 0) {
            return false;
        }

        HotbarLoadoutDefinition loadout = collectionStore.loadoutForHotkey(hotbarIndex);
        if (loadout == null) {
            return false;
        }

        applyQuickAccessMutation(() -> hotbarLoadoutController.applyLoadoutMutation(loadout));
        return true;
    }

    private boolean handleUndoRedoKeyPress(int keyCode) {
        if (isSearchInputActive() || !primaryModifierDown()) {
            return false;
        }

        boolean undo = keyCode == GLFW.GLFW_KEY_Z && !Screen.hasShiftDown();
        boolean redo = keyCode == GLFW.GLFW_KEY_Y || (keyCode == GLFW.GLFW_KEY_Z && Screen.hasShiftDown());
        if (!undo && !redo) {
            return false;
        }

        applyUndoHistoryResult(
                undo ? SlotUndoHistory.undo(undoHistoryContext()) : SlotUndoHistory.redo(undoHistoryContext()),
                undo
        );
        return true;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (isNewCollectionPromptActive() && newCollectionNameBox.charTyped(codePoint, modifiers)) {
            return true;
        }
        if (isInlineDesiredCountActive() && inlineDesiredCountBox.charTyped(codePoint, modifiers)) {
            return true;
        }
        if (isInlineLoadoutRenameActive() && inlineLoadoutRenameBox.charTyped(codePoint, modifiers)) {
            return true;
        }
        if (dockedToolPanel != null && dockedToolPanel.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        inlineLoadoutRenameState.beginFrame();
        inlineDesiredCountState.beginFrame();
        if (inlineLoadoutRenameBox != null) {
            inlineLoadoutRenameBox.setVisible(false);
        }
        if (inlineDesiredCountBox != null) {
            inlineDesiredCountBox.setVisible(false);
        }
        syncDockedToolPanelLayout();
        if (pendingEmiLayoutRefresh) {
            pendingEmiLayoutRefresh = false;
            EmiLayoutSync.refreshIfPresent("slot_player_screen_first_render");
        }
        renderTransparentBackground(guiGraphics);
        renderPanels(guiGraphics);
        CONTAINER_RENDER_HOOKS.postBackground(this, guiGraphics, mouseX, mouseY);
        if (allowForeignContainerChildren()) {
            renderExternalRenderables(guiGraphics, mouseX, mouseY, partialTick);
        }
        searchBox.render(guiGraphics, mouseX, mouseY, partialTick);
        sortButton.render(guiGraphics, mouseX, mouseY, partialTick);
        undoButton.render(guiGraphics, mouseX, mouseY, partialTick);
        redoButton.render(guiGraphics, mouseX, mouseY, partialTick);
        if (collectionsButton != null) {
            collectionsButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        if (settingsButton != null) {
            settingsButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        if (screenToggleButton != null) {
            screenToggleButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        if (vanillaButton != null) {
            vanillaButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        entryList.render(guiGraphics, mouseX, mouseY, partialTick);
        if (isInlineLoadoutRenameActive()) {
            if (inlineLoadoutRenameState.shouldCancelAfterLayout()) {
                cancelInlineLoadoutRename();
            } else {
                inlineLoadoutRenameBox.render(guiGraphics, mouseX, mouseY, partialTick);
            }
        }
        if (isInlineDesiredCountActive()) {
            if (inlineDesiredCountState.shouldCancelAfterLayout()) {
                cancelInlineDesiredCount();
            } else {
                inlineDesiredCountBox.render(guiGraphics, mouseX, mouseY, partialTick);
            }
        }
        if (dockedToolPanel != null) {
            dockedToolPanel.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        renderRail(guiGraphics, mouseX, mouseY);
        renderHeader(guiGraphics);
        renderLiveHotbar(guiGraphics, mouseX, mouseY);
        renderCenterEmptyState(guiGraphics);
        CONTAINER_RENDER_HOOKS.postForeground(this, guiGraphics, mouseX, mouseY);
        if (rowActionMenu != null) {
            rowActionMenu.render(guiGraphics, mouseX, mouseY, this::drawActionMenuLabel);
        }
        if (isNewCollectionPromptActive()) {
            renderNewCollectionPrompt(guiGraphics, mouseX, mouseY, partialTick);
        }
        boolean renderableFloatingStack = hasRenderableFloatingItems();
        if (renderableFloatingStack) {
            renderFloatingItems(guiGraphics, mouseX, mouseY);
        } else if (hasHotbarDrag()) {
            guiGraphics.renderItem(hotbarInteractionState.dragStack(), mouseX - 8, mouseY - 8);
        }
        boolean overDockedToolPanel = dockedToolPanel != null && dockedToolPanel.contains(mouseX, mouseY);
        ItemRowEntry hoveredRow = entryList == null ? null : entryList.entryAtPositionAs(mouseX, mouseY, ItemRowEntry.class);
        if (!hasHotbarDrag()
                && !renderableFloatingStack
                && !overDockedToolPanel
                && hoveredRow != null
                && (rowActionMenu == null || !rowActionMenu.contains(mouseX, mouseY))
                && !hoveredRow.isActionHit(mouseX)) {
            SlotTooltipRenderer.renderItemTooltip(guiGraphics, font, hoveredRow.entry.displayStack(), mouseX, mouseY, width, height);
        } else if (!hasHotbarDrag() && !renderableFloatingStack && !overDockedToolPanel) {
            int hoveredHotbarSlot = hotbarSlotAt(mouseX, mouseY);
            if (hoveredHotbarSlot >= 0) {
                ItemStack stack = hotbarStack(hoveredHotbarSlot);
                if (!stack.isEmpty()) {
                    SlotTooltipRenderer.renderItemTooltip(guiGraphics, font, stack, mouseX, mouseY, width, height);
                }
            } else if (trashSlotLayout().contains(mouseX, mouseY)) {
                SlotTooltipRenderer.renderTextTooltip(guiGraphics, font, trashTooltipLines(), mouseX, mouseY, width, height);
            }
        }
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
    }

    private void renderExternalRenderables(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        for (GuiEventListener child : children()) {
            if (!(child instanceof Renderable renderable) || isOwnedRenderable(renderable)) {
                continue;
            }
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    private boolean hasVisibleCarriedStack() {
        AbstractContainerMenu observedMenu = observedMenu();
        return observedMenu != null && !observedMenu.getCarried().isEmpty();
    }

    private boolean hasRenderableFloatingItems() {
        return ContainerFloatingItemRenderSupport.hasRenderableFloatingItems(this, renderCarriedSourceStack());
    }

    private ItemStack visibleFloatingStack() {
        return ContainerFloatingItemRenderSupport.visibleFloatingStack(this, renderCarriedSourceStack());
    }

    private void renderFloatingItems(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        ContainerFloatingItemRenderSupport.renderFloatingItems(
                this,
                font,
                guiGraphics,
                leftPos,
                topPos,
                renderCarriedSourceStack(),
                mouseX,
                mouseY
        );
    }

    private ItemStack renderCarriedSourceStack() {
        AbstractContainerMenu observedMenu = observedMenu();
        return observedMenu == null ? ItemStack.EMPTY : observedMenu.getCarried();
    }

    private boolean isOwnedRenderable(Renderable renderable) {
        return renderable == searchBox
                || renderable == inlineLoadoutRenameBox
                || renderable == inlineDesiredCountBox
                || renderable == newCollectionNameBox
                || renderable == sortButton
                || renderable == undoButton
                || renderable == redoButton
                || renderable == collectionsButton
                || renderable == settingsButton
                || renderable == screenToggleButton
                || renderable == vanillaButton
                || renderable == entryList;
    }

    private boolean isOwnedControlChild(GuiEventListener child) {
        return child == searchBox
                || child == inlineLoadoutRenameBox
                || child == inlineDesiredCountBox
                || child == newCollectionNameBox
                || child == sortButton
                || child == undoButton
                || child == redoButton
                || child == collectionsButton
                || child == settingsButton
                || child == screenToggleButton
                || child == vanillaButton;
    }

    private boolean isOwnedListChild(GuiEventListener child) {
        return child == entryList;
    }

    private boolean isExternalChild(GuiEventListener child) {
        return !isOwnedControlChild(child) && !isOwnedListChild(child);
    }

    private boolean allowForeignContainerChildren() {
        return screenContext == null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (handleNewCollectionPromptClick(mouseX, mouseY, button)) {
            return true;
        }
        if (handleInlineDesiredCountClick(mouseX, mouseY, button)) {
            return true;
        }
        if (handleInlineLoadoutRenameClick(mouseX, mouseY, button)) {
            return true;
        }
        if (handleRowActionMenuClick(mouseX, mouseY, button)) {
            return true;
        }
        if (handleSearchBoxClick(mouseX, mouseY, button)) {
            return true;
        }
        if (handleRailClick(mouseX, mouseY, button)) {
            return true;
        }
        if (handleHotbarClick(mouseX, mouseY, button)) {
            return true;
        }
        if (handleCraftingGridSelectionClick(mouseX, mouseY, button)) {
            return true;
        }
        if (handleTrashSlotClick(mouseX, mouseY, button)) {
            return true;
        }
        if (handleLoadoutHotkeyClick(mouseX, mouseY, button)) {
            return true;
        }
        if (handleDockedToolPanelClick(mouseX, mouseY, button)) {
            setFocused(null);
            if (button == 0) {
                setDragging(true);
            }
            return true;
        }
        for (GuiEventListener child : children()) {
            if (!isOwnedControlChild(child)) {
                continue;
            }
            if (child.mouseClicked(mouseX, mouseY, button)) {
                setFocused(child);
                if (button == 0) {
                    setDragging(true);
                }
                return true;
            }
        }
        if (allowForeignContainerChildren()) {
            for (GuiEventListener child : children()) {
                if (!isExternalChild(child)) {
                    continue;
                }
                if (child.mouseClicked(mouseX, mouseY, button)) {
                    setFocused(child);
                    if (button == 0) {
                        setDragging(true);
                    }
                    return true;
                }
            }
        }
        if (handleCursorCarriedDrop(mouseX, mouseY, button)) {
            return true;
        }
        if (entryList != null && entryList.mouseClicked(mouseX, mouseY, button)) {
            setFocused(entryList);
            if (button == 0) {
                setDragging(true);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && completeHotbarDrop(mouseX, mouseY)) {
            setDragging(false);
            return true;
        }
        if ((button == 0 || button == 1) && completeHotbarCursorRelease(mouseX, mouseY)) {
            setDragging(false);
            return true;
        }
        if (button == 0 && isDragging() && handleTrashSlotRelease(mouseX, mouseY)) {
            setDragging(false);
            return true;
        }
        clearHotbarDrag();
        clearHotbarCursorInteraction();
        setDragging(false);
        if (DockedToolPanelInteractionSupport.dispatchRelease(
                dockedToolPanel,
                mouseX,
                mouseY,
                button,
                this::consumeDockedToolPanelRefreshRequest
        )) {
            return true;
        }
        GuiEventListener focused = getFocused();
        if (focused != null && focused.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        for (GuiEventListener child : children()) {
            if (child == focused || !isOwnedControlChild(child)) {
                continue;
            }
            if (child.mouseReleased(mouseX, mouseY, button)) {
                return true;
            }
        }
        if (allowForeignContainerChildren()) {
            for (GuiEventListener child : children()) {
                if (child == focused || !isExternalChild(child)) {
                    continue;
                }
                if (child.mouseReleased(mouseX, mouseY, button)) {
                    return true;
                }
            }
        }
        return entryList != focused && entryList != null && entryList.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && hasHotbarDrag()) {
            return true;
        }
        if (DockedToolPanelInteractionSupport.dispatchDrag(
                dockedToolPanel,
                mouseX,
                mouseY,
                button,
                dragX,
                dragY,
                this::consumeDockedToolPanelRefreshRequest
        )) {
            return true;
        }
        GuiEventListener focused = getFocused();
        if (focused != null && isDragging() && focused.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (allowForeignContainerChildren()) {
            for (GuiEventListener child : children()) {
                if (!isExternalChild(child)) {
                    continue;
                }
                if (child.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
                    return true;
                }
            }
        }
        if (handleDockedToolPanelScroll(mouseX, mouseY, scrollX, scrollY)) {
            return true;
        }
        if (handleRailScroll(mouseX, mouseY, scrollY)) {
            return true;
        }
        if (entryList != null && entryList.isMouseOver(mouseX, mouseY) && entryList.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            return true;
        }
        for (GuiEventListener child : children()) {
            if (!isOwnedControlChild(child)) {
                continue;
            }
            if (child.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
                return true;
            }
        }
        return false;
    }

    private void renderPanels(GuiGraphics guiGraphics) {
        guiGraphics.fill(panelLeft(), panelTop(), panelVisualRight(), panelBottom(), 0xD0101010);
        guiGraphics.fill(panelLeft() + 1, panelTop() + 1, panelVisualRight() - 1, panelBottom() - 1, 0xE0161616);
        int chromeBottom = contentTop() - 10;
        guiGraphics.fill(panelLeft() + 1, panelTop() + 1, panelVisualRight() - 1, chromeBottom, 0xD9121214);
        guiGraphics.fill(panelLeft() + 1, chromeBottom, panelVisualRight() - 1, chromeBottom + 1, 0x7048484A);
        guiGraphics.fill(railX(), contentTop() - 8, railX() + railWidth(), panelBottom() - OUTER_MARGIN, 0xA0101010);
        guiGraphics.fill(centerPaneX(), contentTop() - 8, centerPaneX() + centerPaneWidth(), panelBottom() - OUTER_MARGIN, 0xA0181818);
        guiGraphics.fill(railX() + railWidth() + 2, contentTop() - 8, railX() + railWidth() + 3, panelBottom() - OUTER_MARGIN, 0x402E3238);
    }

    private void renderHeader(GuiGraphics guiGraphics) {
        guiGraphics.drawString(font, title, centerPaneX(), headerY(), 0xFFFFFF, false);
        String summary = Component.translatable("slot.screen.debug.summary", inventoryData.entries().size()).getString();
        drawScaledText(guiGraphics, summary, contentRight() - scaledTextWidth(summary, 0.72F), headerY() + 1, 0xA8B0B8, 0.72F);
        renderActionFeedback(guiGraphics);
        renderCapacityIndicator(guiGraphics);
    }

    private void renderCapacityIndicator(GuiGraphics guiGraphics) {
        if (!carriedCapacity.available()) {
            return;
        }

        String label = Component.translatable(
                "slot.screen.inventory.capacity",
                carriedCapacity.occupiedSlots(),
                carriedCapacity.totalSlots()
        ).getString();
        int labelX = railX() + 4;
        int labelY = headerY();
        guiGraphics.drawString(font, label, labelX, labelY, capacityTextColor(), false);

        int barWidth = 84;
        int barHeight = 6;
        int barGap = 6;
        int barLeft = labelX;
        int barRight = barLeft + barWidth;
        int barTop = labelY + font.lineHeight + 1;
        int barBottom = barTop + barHeight;
        guiGraphics.fill(barLeft, barTop, barRight, barBottom, 0xA0242424);
        guiGraphics.fill(barLeft + 1, barTop + 1, barRight - 1, barBottom - 1, 0xC0101010);
        int fillWidth = Math.round((barWidth - 2) * carriedCapacity.fillRatio());
        if (fillWidth > 0) {
            guiGraphics.fill(barLeft + 1, barTop + 1, barLeft + 1 + fillWidth, barBottom - 1, capacityFillColor());
        }

        SlotTrashWarningState warningState = trashWarningState();
        if (warningState.active()) {
            float scale = 0.65F;
            String text = Component.translatable("slot.screen.inventory.trash.auto_void_warning_short", warningState.nextStack().getHoverName()).getString();
            text = font.plainSubstrByWidth(text, Math.max(24, Math.round(120 / scale)));
            drawScaledText(guiGraphics, text, labelX, barBottom + 2, 0xE0A080, scale);
        }
    }

    private void renderTrashSlot(GuiGraphics guiGraphics, TrashSlotLayout layout) {
        SlotTrashWarningState warningState = trashWarningState();
        boolean hovered = layout.contains(lastMouseX, lastMouseY);
        int frameColor;
        int innerColor;
        if (warningState.active()) {
            frameColor = hovered ? 0xC09E5C3A : 0xA0744428;
            innerColor = hovered ? 0xD0241610 : 0xB01C1210;
        } else {
            frameColor = hovered ? 0xC06E4545 : 0xA0503030;
            innerColor = hovered ? 0xD01A1010 : 0xB0141010;
        }
        guiGraphics.fill(layout.left(), layout.top(), layout.right(), layout.bottom(), frameColor);
        guiGraphics.fill(layout.left() + 1, layout.top() + 1, layout.right() - 1, layout.bottom() - 1, innerColor);
        guiGraphics.fill(layout.left() + 3, layout.top() + 6, layout.right() - 3, layout.top() + 7, 0x907B4A4A);

        ItemStack previewStack = SlotTrashBuffer.previewStack();
        if (previewStack.isEmpty()) {
            return;
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(layout.left() + 1, layout.top() + 1, 0.0F);
        guiGraphics.pose().scale(ROW_ITEM_SCALE, ROW_ITEM_SCALE, 1.0F);
        guiGraphics.renderItem(previewStack, 0, 0);
        guiGraphics.pose().popPose();
    }

    private int capacityFillColor() {
        float ratio = carriedCapacity.fillRatio();
        if (ratio >= 0.9F) {
            return 0xFFC44949;
        }
        if (ratio >= 0.7F) {
            return 0xFFD08A2E;
        }
        return 0xFF4E7A34;
    }

    private int capacityTextColor() {
        float ratio = carriedCapacity.fillRatio();
        if (ratio >= 0.9F) {
            return 0xF0B0B0;
        }
        if (ratio >= 0.7F) {
            return 0xE0C090;
        }
        return 0xB8C8B0;
    }

    private void renderRail(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        clampRailScroll();
        int x = railX() + 4;
        int y = contentTop() - railScrollState.offset();
        int viewportTop = contentTop();
        int viewportBottom = panelBottom() - OUTER_MARGIN;
        String highlighted = entryList == null ? ALL_TARGET_ID : entryList.currentSectionId();

        Kind previousKind = null;
        for (Target target : railTargets) {
            if (target.kind() == Kind.COLLECTION && previousKind != Kind.COLLECTION) {
                if (y + 10 >= viewportTop && y <= viewportBottom) {
                    drawScaledText(guiGraphics, Component.translatable("slot.screen.debug.rail.collections").getString(), x, y + 1, 0xB0B0B0, RAIL_TEXT_SCALE);
                }
                y += 12;
            } else if (target.kind() == Kind.MOD_BUCKET && previousKind != Kind.MOD_BUCKET) {
                if (y + 10 >= viewportTop && y <= viewportBottom) {
                    drawScaledText(guiGraphics, Component.translatable("slot.screen.debug.rail.mods").getString(), x, y + 1, 0xB0B0B0, RAIL_TEXT_SCALE);
                }
                y += 12;
            } else if (target.kind() == Kind.CATEGORY && previousKind != Kind.CATEGORY) {
                if (y + 10 >= viewportTop && y <= viewportBottom) {
                    drawScaledText(guiGraphics, Component.translatable("slot.screen.debug.rail.categories").getString(), x, y + 1, 0xB0B0B0, RAIL_TEXT_SCALE);
                }
                y += 12;
            }

            int rowTop = y - 2;
            int rowBottom = y + RAIL_ROW_HEIGHT - 2;
            boolean visible = rowBottom >= viewportTop && rowTop <= viewportBottom;
            boolean hovered = visible && mouseX >= x - 4 && mouseX <= railX() + railWidth() - 4 && mouseY >= rowTop && mouseY <= rowBottom;
            boolean active = target.id().equals(highlighted);
            boolean empty = !ALL_TARGET_ID.equals(target.id()) && !isRailTargetAccessible(target.id());
            int background = active ? 0x9043661F : hovered ? 0x50383838 : 0;
            if (visible && background != 0) {
                guiGraphics.fill(x - 4, rowTop, railX() + railWidth() - 4, rowBottom, background);
            }

            String label = ALL_TARGET_ID.equals(target.id())
                    ? target.label()
                    : target.label() + " (" + visibleSectionCounts.getOrDefault(target.id(), 0) + ")";
            label = font.plainSubstrByWidth(label, railWidth() - 24);
            int color = empty ? 0x9A9A9A : active ? 0xFFFFFF : 0xD8D8D8;
            if (visible) {
                drawScaledText(guiGraphics, label, x, y + 3, color, RAIL_TEXT_SCALE);
            }
            y += RAIL_ROW_HEIGHT;
            previousKind = target.kind();
        }
        renderRailScrollbar(guiGraphics);
    }

    private void renderCenterEmptyState(GuiGraphics guiGraphics) {
        if (entryList == null || visibleItemResultCount > 0) {
            return;
        }

        int x = centerPaneX() + 12;
        int y = contentTop() + 12;
        drawWrapped(guiGraphics, Component.translatable("slot.screen.debug.empty_state"), x, y, centerPaneWidth() - 24, 0xA0A0A0);
    }

    private void renderLiveHotbar(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int slotY = hotbarY();
        int stripX = hotbarStripX();
        int stripWidth = hotbarStripWidth();
        TrashSlotLayout trashLayout = trashSlotLayout();
        int bandLeft = Math.min(stripX - 4, trashLayout.left() - 4);
        guiGraphics.fill(bandLeft, slotY - 4, stripX + stripWidth + 4, slotY + HOTBAR_SLOT_SIZE + 4, 0x40121212);
        renderTrashSlot(guiGraphics, trashLayout);

        int hoveredSlot = hotbarSlotAt(mouseX, mouseY);
        for (int slotIndex = 0; slotIndex < HotbarLoadoutDefinition.QUICK_ACCESS_SLOT_COUNT; slotIndex++) {
            int slotX = hotbarSlotX(slotIndex);
            int background = hoveredSlot == slotIndex
                    ? 0x90507038
                    : hasHotbarDrag() && hoveredSlot == slotIndex
                    ? 0x90507038
                    : 0x60303030;
            guiGraphics.fill(slotX, slotY, slotX + HOTBAR_SLOT_SIZE, slotY + HOTBAR_SLOT_SIZE, background);
            guiGraphics.fill(slotX + 1, slotY + 1, slotX + HOTBAR_SLOT_SIZE - 1, slotY + HOTBAR_SLOT_SIZE - 1, 0x90181818);

            ItemStack stack = hotbarStack(slotIndex);
            if (!stack.isEmpty()) {
                guiGraphics.renderItem(stack, slotX + 1, slotY + 1);
                guiGraphics.renderItemDecorations(font, stack, slotX + 1, slotY + 1);
            }

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0.0F, 0.0F, 250.0F);
            guiGraphics.fill(slotX + 1, slotY + 1, slotX + 7, slotY + 7, 0xB0101010);
            drawScaledText(guiGraphics, quickAccessSlotLabel(slotIndex), slotX + 2, slotY + 1, 0xD8D8D8, 0.70F);
            guiGraphics.pose().popPose();
        }
    }

    protected void rebuildVisibleEntries() {
        if (entryList == null) {
            return;
        }

        String query = searchBox == null ? "" : searchBox.getValue().trim().toLowerCase(Locale.ROOT);
        inventoryProjection = buildInventoryProjection(query);
        InventoryProjection.PaneProjection carriedPane = inventoryProjection.pane(InventoryPane.CARRIED);

        visibleSectionCounts = carriedPane.sectionCounts();
        visibleItemResultCount = carriedPane.visibleEntryCount();
        clampRailScroll();
        entryList.setRows(materializeRows(carriedPane.rows()));
        restoreSelection();
    }

    private InventoryProjection buildInventoryProjection(String query) {
        return InventoryProjectionBuilder.buildWorkspace(
                new InventoryProjectionBuilder.WorkspaceInput(
                        inventoryData.sections(),
                        inventoryData.entries(),
                        recentMatchingEntries(query),
                        query,
                        collectionStore,
                        sortMode.comparator(),
                        (entry, pane) -> pane == InventoryPane.CARRIED ? Math.max(0, entry.itemEntry().totalCount()) : 0,
                        this::projectionSectionOptions
                )
        );
    }

    private InventoryProjectionBuilder.PaneSectionOptions projectionSectionOptions(
            InventoryViewData.Section section,
            InventoryPane pane
    ) {
        boolean expanded = !section.isCollection() || !isCollectionCollapsed(section.collectionId());
        return new InventoryProjectionBuilder.PaneSectionOptions(
                pane == InventoryPane.CARRIED,
                pane == InventoryPane.CARRIED && (shouldRetainCollectionSection(section) || section.isRecent()),
                expanded,
                pane == InventoryPane.CARRIED
                        && section.isCollection()
                        && shouldShowCollectionLoadouts(section.collectionId(), expanded),
                false
        );
    }

    private List<InventoryScreenRow> materializeRows(List<InventoryProjection.RowProjection> rows) {
        List<InventoryScreenRow> materializedRows = new ArrayList<>(rows.size());
        for (InventoryProjection.RowProjection row : rows) {
            if (row instanceof InventoryProjection.SectionRowProjection sectionRow) {
                materializedRows.add(new InventorySectionScreenRow(
                        sectionRow.rowId(),
                        sectionRow.section(),
                        sectionRow.count(),
                        sectionHeaderSupport,
                        new InventorySectionScreenRow.Geometry() {
                            @Override
                            public int rowTop(InventorySectionScreenRow row) {
                                return entryList.rowTop(entryList.children().indexOf(row));
                            }

                            @Override
                            public int rowLeft() {
                                return entryList.getRowLeft();
                            }

                            @Override
                            public int rowWidth() {
                                return entryList.getRowWidth();
                            }
                        },
                        (x, y, width) -> sectionHeaderState(sectionRow.section(), x, y, width),
                        (target, headerState) -> handleSectionRowClick(sectionRow.section(), target, headerState)
                ));
                continue;
            }
            if (row instanceof InventoryProjection.LoadoutRowProjection loadoutRow) {
                materializedRows.add(new CollectionLoadoutEntry(loadoutRow.rowId(), loadoutRow.section()));
                continue;
            }
            if (row instanceof InventoryProjection.ItemRowProjection itemRow) {
                materializedRows.add(new ItemRowEntry(
                        itemRow.rowId(),
                        itemRow.entry(),
                        itemRow.section().collectionId(),
                        itemRow.section().isRecent()
                ));
            }
        }
        return List.copyOf(materializedRows);
    }

    private InventorySectionHeaderSupport.SectionHeaderState sectionHeaderState(
            InventoryViewData.Section section,
            int x,
            int y,
            int width
    ) {
        return sectionHeaderSupport.buildState(
                section,
                x,
                y,
                width,
                new InventorySectionHeaderSupport.SectionHeaderOptions(
                        section.isCollection(),
                        section.isCollection() || section.isRecent(),
                        !buildSectionActions(section, x, y + COLLECTION_BUTTON_HEIGHT).isEmpty(),
                        false,
                        false,
                        x + 36
                )
        );
    }

    private boolean handleSectionRowClick(
            InventoryViewData.Section section,
            InventorySectionHeaderSupport.SectionHeaderClickTarget target,
            InventorySectionHeaderSupport.SectionHeaderState headerState
    ) {
        return switch (target) {
            case TOGGLE -> {
                toggleCollectionCollapsed(section.collectionId());
                yield true;
            }
            case PIN -> {
                togglePinnedLoadoutsWhenCollapsed(section.collectionId());
                yield true;
            }
            case MENU -> {
                InlineButton menuButton = headerState.menuButton();
                openSectionActionMenu(section, menuButton.x(), menuButton.y() + menuButton.height());
                yield true;
            }
            case RESTOCK, NAVIGATE -> {
                entryList.navigateToTarget(section.id());
                yield true;
            }
        };
    }

    private List<InventoryViewData.EntryView> recentMatchingEntries(String query) {
        return recentMatchingEntries(query, inventoryData.entries());
    }

    @Override
    protected Set<String> defaultRecentCarriedSourceIds() {
        return ChestLikeMenuLayout.BASE_CARRIED_SOURCES;
    }

    @Override
    protected int ownedCountForCollectionStock(ItemIdentity identity) {
        if (identity == null || inventoryData == null) {
            return 0;
        }
        for (InventoryViewData.EntryView entry : inventoryData.entries()) {
            if (ItemBehaviorPolicy.matchesTrackedIdentity(identity, entry.itemEntry().identity())) {
                return entry.itemEntry().totalCount();
            }
        }
        return 0;
    }

    private void restoreSelection() {
        if (entryList == null) {
            return;
        }

        InventoryScreenRow restoredRow = InventorySelectionRestoreSupport.findRowByValue(
                selectedRowId,
                entryList.children(),
                row -> row instanceof ItemRowEntry itemRow ? itemRow.rowId() : null
        );
        if (!(restoredRow instanceof ItemRowEntry)) {
            restoredRow = InventorySelectionRestoreSupport.findRowByIdentity(
                    selectedIdentity,
                    entryList.children(),
                    row -> row instanceof ItemRowEntry itemRow ? itemRow.entry.itemEntry().identity() : null
            );
        }

        if (restoredRow instanceof ItemRowEntry selectedRow) {
            entryList.setSelected(selectedRow);
            setSelectedEntry(selectedRow.entry, selectedRow.rowId());
        } else {
            entryList.setSelected(null);
            setSelectedEntry(null);
        }
    }

    private void clearSelectedEntry() {
        if (entryList != null) {
            entryList.setSelected(null);
        }
        setSelectedEntry(null);
    }

    @Override
    protected void applyUndoHistoryResult(SlotUndoHistory.ApplyResult result, boolean undo) {
        applyUndoHistoryResultCommon(result, undo, this::requestImmediatePostActionRefresh);
    }

    private void applyConfirmedActionOutcome() {
        applyConfirmedActionOutcomeCommon();
    }

    protected AbstractContainerMenu historyMenu() {
        AbstractContainerMenu observedMenu = observedMenu();
        return observedMenu != null ? observedMenu : menu;
    }


    private void runMoveOne(InventoryViewData.EntryView entry) {
        runMove(entry, InventoryTransferActionSupport.EntryMoveMode.ONE);
    }

    private void runMoveStack(InventoryViewData.EntryView entry) {
        runMove(entry, InventoryTransferActionSupport.EntryMoveMode.STACK);
    }

    private void runMoveAllType(InventoryViewData.EntryView entry) {
        runMove(entry, InventoryTransferActionSupport.EntryMoveMode.ALL_OF_TYPE);
    }

    private void runMove(InventoryViewData.EntryView entry, InventoryTransferActionSupport.EntryMoveMode mode) {
        if (entry == null) {
            return;
        }

        SlotActionResult result = InventoryTransferActionSupport.movePlayerEntry(
                actionExecutor,
                currentPlayer(),
                entry,
                mode
        );
        handleActionResultCommon(result, actionPlan(true, true, true));
    }

    protected void refreshInventoryData() {
        inventoryData = dataBuilder.build(Objects.requireNonNull(minecraft).player, collectionStore, screenContext);
        SlotUndoHistory.bindContext(historyContextKey());
        carriedCapacity = InventoryCapacityIndicator.measureCarried(currentPlayer(), screenContext);
        lastObservedMenuStateHash = computeObservedMenuStateHash(observedMenu());
        railTargets = buildRailTargets();
        normalizeInlineCollectionState();
        rowActionMenu = null;
        rebuildVisibleEntries();
        updateDynamicButtons();
    }

    private boolean shouldAutoVoidJunk() {
        return inventoryData != null
                && carriedCapacity.available()
                && carriedCapacity.freeSlots() <= 1
                && !postActionRefreshDelay.active()
                && !hasVisibleCarriedStack()
                && !hasHotbarDrag()
                && !hotbarInteractionState.cursorInteractionActive()
                && rowActionMenu == null
                && !isInlineDesiredCountActive()
                && !isInlineLoadoutRenameActive()
                && !isNewCollectionPromptActive();
    }

    private boolean attemptAutoVoidJunk() {
        if (!shouldAutoVoidJunk()) {
            return false;
        }

        AbstractContainerMenu activeMenu = activeMenu();
        ItemIdentity identity = nextJunkIdentityToVoid();
        if (activeMenu == null || identity == null || !CursorTransferRequester.requestVoidMatchingCarried(activeMenu.containerId, identity)) {
            return false;
        }

        ItemStack previewStack = ItemBehaviorPolicy.approximateDisplayStack(identity);
        if (!previewStack.isEmpty()) {
            previewStack.setCount(1);
            SlotTrashBuffer.rememberAutoVoid(previewStack);
        }
        SlotDebugLog.log(
                "Requested junk auto-void from carried screen: identity={} freeSlots={}",
                identity.itemId(),
                carriedCapacity.freeSlots()
        );
        handleActionResultCommon(
                SlotActionResult.requested(Component.translatable(
                        "slot.screen.action.auto_void.requested",
                        previewStack.getHoverName()
                )),
                actionPlan(false, true, true)
        );
        return true;
    }

    @Override
    public void slotRefreshContents() {
        if (refreshCraftingUpgradePanelState(true)) {
            return;
        }
        postActionRefreshDelay.clear();
        refreshInventoryData();
    }

    private boolean handleCraftingGridSelectionClick(double mouseX, double mouseY, int button) {
        if (button != 0
                || !(dockedToolPanel instanceof SlotBackedToolPanel slotBackedToolPanel)
                || selectedEntry == null
                || hasVisibleCarriedStack()) {
            return false;
        }

        AbstractContainerMenu observedMenu = observedMenu();
        if (observedMenu == null || !ensureCraftingUpgradeOpen()) {
            return false;
        }

        ItemIdentity identity = selectedEntry.itemEntry().identity();
        ItemStack selectedStack = selectedEntry.displayStack();
        var placement = dev.imagio.slot.client.screen.container.CraftingToolService.requestPlaceOne(
                observedMenu.containerId,
                slotBackedToolPanel,
                mouseX,
                mouseY,
                identity,
                selectedStack,
                InventoryPane.CARRIED
        );
        if (!placement.requested()) {
            return false;
        }

        SlotDebugLog.log(
                "Requested backpack craft grid placement from selection: targetMenuSlot={} identity={}",
                placement.targetMenuSlotId().value(),
                identity.itemId()
        );
        clearSelectedEntry();
        requestImmediatePostActionRefresh();
        return true;
    }

    private boolean handleDockedToolPanelClick(double mouseX, double mouseY, int button) {
        syncDockedToolPanelLayout();
        return DockedToolPanelInteractionSupport.dispatchSlotBackedClick(
                dockedToolPanel,
                mouseX,
                mouseY,
                button,
                this::ensureCraftingUpgradeOpen,
                this::consumeDockedToolPanelRefreshRequest
        );
    }

    private boolean handleDockedToolPanelScroll(double mouseX, double mouseY, double scrollX, double scrollY) {
        syncDockedToolPanelLayout();
        return DockedToolPanelInteractionSupport.dispatchSlotBackedScroll(
                dockedToolPanel,
                mouseX,
                mouseY,
                scrollX,
                scrollY,
                this::ensureCraftingUpgradeOpen,
                this::consumeDockedToolPanelRefreshRequest
        );
    }

    private boolean handleCursorCarriedDrop(double mouseX, double mouseY, int button) {
        ItemRowEntry hoveredRow = entryList == null ? null : entryList.entryAtPositionAs(mouseX, mouseY, ItemRowEntry.class);
        InventoryCursorDropTargetSupport.DropTarget target = InventoryCursorDropTargetSupport.carriedDropTarget(
                button,
                hasVisibleCarriedStack(),
                isOverDockedToolPanel(mouseX, mouseY),
                hoveredRow != null,
                isInsideCarriedDropZone(mouseX, mouseY)
        );
        if (!target.present()) {
            return false;
        }

        return requestCursorCarriedDrop(button);
    }

    private boolean requestCursorCarriedDrop(int button) {
        if ((button != 0 && button != 1) || !hasVisibleCarriedStack()) {
            return false;
        }

        AbstractContainerMenu observedMenu = observedMenu();
        InventoryCursorActionSupport.RequestedCursorAction action = InventoryCursorActionSupport.drop(
                observedMenu == null ? -1 : observedMenu.containerId,
                InventoryPane.CARRIED,
                button == 1
        );
        if (action.requested()) {
            SlotDebugLog.log(
                    "Requested carried cursor drop into backpack-crafted carried inventory: mode={}",
                    action.modeLabel()
            );
        }
        return handleRequestedActionCommon(
                action.requested(),
                action.requestedResult(),
                actionPlan(action.suppressPositiveDeltas(), true, false)
        );
    }

    private boolean requestCursorPickup(InventoryViewData.EntryView entry, CursorTransferPayload.Mode mode) {
        if (entry == null || entry.itemEntry().identity() == null) {
            return false;
        }

        AbstractContainerMenu activeMenu = activeMenu();
        InventoryCursorActionSupport.RequestedCursorAction action = InventoryCursorActionSupport.pickup(
                activeMenu == null ? -1 : activeMenu.containerId,
                InventoryPane.CARRIED,
                entry,
                mode
        );
        if (action.requested()) {
            SlotDebugLog.log(
                    "Requested cursor pickup from carried row: identity={} mode={}",
                    entry.itemEntry().identity().itemId(),
                    action.modeLabel()
            );
        }
        return handleRequestedActionCommon(
                action.requested(),
                action.requestedResult(),
                actionPlan(action.suppressPositiveDeltas(), true, false)
        );
    }

    private boolean handleTrashSlotClick(double mouseX, double mouseY, int button) {
        AbstractContainerMenu activeMenu = activeMenu();
        if ((button != 0 && button != 1) || activeMenu == null || activeMenu.getCarried().isEmpty() || !trashSlotLayout().contains(mouseX, mouseY)) {
            return false;
        }

        ItemStack trashedPreview = activeMenu.getCarried().copy();
        if (button == 1) {
            trashedPreview.setCount(1);
        }

        InventoryCursorActionSupport.RequestedCursorAction action = InventoryCursorActionSupport.trash(
                activeMenu.containerId,
                button == 1
        );
        if (action.requested()) {
            SlotDebugLog.log(
                    "Requested cursor trash from carried screen: mode={}",
                    action.modeLabel()
            );
        }
        return handleRequestedActionCommon(
                action.requested(),
                action.requestedResult(),
                actionPlan(
                        action.suppressPositiveDeltas(),
                        true,
                        true,
                        () -> SlotTrashBuffer.remember(trashedPreview),
                        null
                )
        );
    }

    private boolean handleTrashSlotRelease(double mouseX, double mouseY) {
        AbstractContainerMenu activeMenu = activeMenu();
        if (activeMenu == null || activeMenu.getCarried().isEmpty() || !trashSlotLayout().contains(mouseX, mouseY)) {
            return false;
        }
        return handleTrashSlotClick(mouseX, mouseY, 0);
    }

    private void consumeDockedToolPanelRefreshRequest() {
        DockedToolPanelInteractionSupport.consumeRefreshRequest(
                dockedToolPanel,
                this::requestImmediatePostActionRefresh
        );
    }

    private boolean handleLoadoutHotkeyClick(double mouseX, double mouseY, int button) {
        if ((button != 0 && button != 1) || entryList == null) {
            return false;
        }

        CollectionLoadoutEntry hoveredLoadout = entryList.entryAtPositionAs(mouseX, mouseY, CollectionLoadoutEntry.class);
        if (hoveredLoadout == null) {
            return false;
        }

        int rowIndex = entryList.children().indexOf(hoveredLoadout);
        if (rowIndex < 0) {
            return false;
        }

        int rowLeft = entryList.getRowLeft();
        int rowTop = entryList.rowTop(rowIndex);
        int rowWidth = entryList.getRowWidth();
        InlineButton hotkeyButton = hoveredLoadout.hotkeyButton(rowLeft, rowTop, rowWidth);
        if (!hotkeyButton.contains(mouseX, mouseY)) {
            return false;
        }

        if (button == 1) {
            clearSelectedLoadoutHotkey(hoveredLoadout.section.collectionId());
        } else {
            cycleSelectedLoadoutHotkey(hoveredLoadout.section.collectionId());
        }
        return true;
    }

    private boolean isInsideCarriedDropZone(double mouseX, double mouseY) {
        if (isOverDockedToolPanel(mouseX, mouseY) || isInsideCraftingUpgradeReservationArea(mouseX, mouseY)) {
            return false;
        }
        return mouseX >= panelLeft()
                && mouseX <= panelVisualRight()
                && mouseY >= hotbarY() - 4
                && mouseY <= panelBottom() - OUTER_MARGIN;
    }

    private boolean isInsideCraftingUpgradeReservationArea(double mouseX, double mouseY) {
        if (!hasCraftingUpgradePanelReservation() || !panelGeometryInitialized) {
            return false;
        }

        int left = centerPaneX();
        int right = left + centerPaneWidth();
        int top = dockedToolPanelY();
        int bottom = top + reservedCraftingUpgradePanelHeight();
        return mouseX >= left && mouseX < right && mouseY >= top && mouseY < bottom;
    }

    private boolean isOverDockedToolPanel(double mouseX, double mouseY) {
        syncDockedToolPanelLayout();
        return DockedToolPanelInteractionSupport.isOver(dockedToolPanel, mouseX, mouseY);
    }

    private boolean ensureCraftingUpgradeOpen() {
        return craftingUpgradePanelRef == null
                || craftingUpgradePanelRef.live()
                || (screenContext != null
                && screenContext.host() != null
                && screenContext.host().providerSession() != null
                && screenContext.host().providerSession().activateTool(observedMenu(), craftingUpgradePanelRef.id()));
    }

    public final AbstractContainerMenu emiObservedMenu() {
        return observedMenu();
    }

    public final boolean emiEnsureCraftingUpgradeOpen() {
        return ensureCraftingUpgradeOpen();
    }

    private AbstractContainerMenu observedMenu() {
        if (screenContext == null) {
            return null;
        }
        return screenContext.menu();
    }

    private AbstractContainerMenu activeMenu() {
        AbstractContainerMenu observedMenu = observedMenu();
        return observedMenu != null ? observedMenu : menu;
    }

    @Override
    protected void schedulePostActionRefresh() {
        postActionRefreshDelay.schedule(8);
    }

    @Override
    protected void clearPendingPostActionRefresh() {
        postActionRefreshDelay.clear();
    }

    private ItemStack previewStack(ItemIdentity identity) {
        return previewStack(identity, inventoryData.entries());
    }

    private int computeObservedMenuStateHash(AbstractContainerMenu observedMenu) {
        return InventoryMenuStateHash.allSlotsAndCarried(observedMenu);
    }

    private boolean handleRailClick(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }

        Target target = InventoryRailSupport.hitTarget(
                railTargets,
                mouseX,
                mouseY,
                railX(),
                railX() + railWidth() - 4,
                contentTop(),
                railScrollState.offset(),
                RAIL_ROW_HEIGHT,
                12
        );
        if (target == null) {
            return false;
        }

        if (target.kind() == Kind.COLLECTION && target.id().startsWith("collection/")) {
            ensureSelectedLoadoutId(target.id().substring("collection/".length()));
        }
        entryList.navigateToTarget(resolveVisibleTargetId(target.id()));
        return true;
    }

    private List<Target> buildRailTargets() {
        return InventoryRailSupport.buildTargets(
                ALL_TARGET_ID,
                Component.translatable("slot.screen.debug.rail.all").getString(),
                inventoryData.sections()
        );
    }

    private String resolveVisibleTargetId(String requestedId) {
        return InventoryRailSupport.resolveVisibleTargetId(ALL_TARGET_ID, railTargets, requestedId, this::isRailTargetAccessible);
    }

    private boolean isRailTargetAccessible(String targetId) {
        if (ALL_TARGET_ID.equals(targetId)) {
            return true;
        }
        if (visibleSectionCounts.getOrDefault(targetId, 0) > 0) {
            return true;
        }
        if (!targetId.startsWith("collection/")) {
            return false;
        }
        String collectionId = targetId.substring("collection/".length());
        return collectionHasLoadouts(collectionId) || CollectionStore.JUNK_ID.equals(collectionId);
    }

    private String sourceSummary(ItemEntry itemEntry) {
        List<Map.Entry<String, Integer>> sourceCounts = itemEntry.perSourceCounts().entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> inventoryData.sources().get(entry.getKey()).stableOrder()))
                .toList();
        if (sourceCounts.isEmpty()) {
            return "";
        }

        if (sourceCounts.size() > 2) {
            Map.Entry<String, Integer> first = sourceCounts.get(0);
            Map.Entry<String, Integer> second = sourceCounts.get(1);
            return shortSource(first.getKey()) + " " + first.getValue()
                    + " | " + shortSource(second.getKey()) + " " + second.getValue()
                    + " +" + (sourceCounts.size() - 2);
        }

        StringJoiner joiner = new StringJoiner(" | ");
        for (Map.Entry<String, Integer> sourceCount : sourceCounts) {
            joiner.add(shortSource(sourceCount.getKey()) + " " + sourceCount.getValue());
        }
        return joiner.toString();
    }

    private String shortSource(String sourceId) {
        return switch (sourceId) {
            case ChestLikeMenuLayout.SOURCE_PLAYER_HOTBAR -> Component.translatable("slot.source.short.hotbar").getString();
            case ChestLikeMenuLayout.SOURCE_PLAYER_MAIN -> Component.translatable("slot.source.short.main").getString();
            case ChestLikeMenuLayout.SOURCE_PLAYER_BACKPACK -> Component.translatable("slot.source.short.backpack").getString();
            case ChestLikeMenuLayout.SOURCE_CARRIED_STORAGE -> shortCarriedStorageSource();
            case ChestLikeMenuLayout.SOURCE_PLAYER_ARMOR -> Component.translatable("slot.source.short.armor").getString();
            case ChestLikeMenuLayout.SOURCE_PLAYER_OFFHAND -> Component.translatable("slot.source.short.offhand").getString();
            default -> sourceId;
        };
    }

    private String shortCarriedStorageSource() {
        InventoryViewData.SourceInfo source = inventoryData.sources().get("carried_storage");
        String label = source == null ? "" : source.label().toLowerCase(Locale.ROOT);
        if (label.contains("backpack") || label.contains("pack")) {
            return Component.translatable("slot.source.short.backpack").getString();
        }
        return Component.translatable("slot.source.short.carried").getString();
    }

    private void openCollectionsForEntry(InventoryViewData.EntryView entry) {
        setSelectedEntry(entry);
        openCollections();
    }

    private void openInspectionForEntry(InventoryViewData.EntryView entry) {
        if (minecraft == null || entry == null) {
            return;
        }

        setSelectedEntry(entry);
        minecraft.setScreen(new SlotItemInspectionScreen(this, inspectionService.inspect(
                entry.itemEntry().identity(),
                entry.displayStack().copy(),
                entry.displayName()
        )));
    }

    private void openSettings() {
        if (minecraft == null || settingsController == null) {
            return;
        }

        minecraft.setScreen(new SlotSettingsScreen(this, settingsController, this::refreshInventoryData));
    }

    private SearchWorkflowService.SearchField searchField() {
        if (searchBox == null) {
            return null;
        }
        return new SearchWorkflowService.SearchField() {
            @Override
            public String query() {
                return searchBox.getValue();
            }

            @Override
            public void setQuery(String query) {
                searchBox.setValue(query);
            }

            @Override
            public boolean focused() {
                return searchBox.isFocused();
            }
        };
    }

    private static ToolCapabilityDescriptor resolveCraftingUpgradePanel(InventoryScreenContext screenContext) {
        if (screenContext == null || screenContext.host() == null) {
            return null;
        }
        return InventoryHostToolCoordinator.firstTool(
                screenContext.host(),
                ExternalToolKind.CRAFTING_GRID,
                "sophisticatedbackpacks"
        );
    }

    private static DockedToolPanel reconcileCraftingUpgradePanel(
            DockedToolPanel existingPanel,
            InventoryScreenContext screenContext,
            ToolCapabilityDescriptor panelRef
    ) {
        if (screenContext == null || panelRef == null || !panelRef.live()) {
            return null;
        }
        return DockedToolPanelResolver.reconcile(screenContext.menu(), existingPanel, panelRef);
    }

    private boolean refreshCraftingUpgradePanelState(boolean allowRebuild) {
        if (screenContext == null || !screenContext.carriedOnly()) {
            return false;
        }

        boolean hadReservedPanelArea = hasCraftingUpgradePanelReservation();
        boolean hadLiveCraftingPanel = dockedToolPanel != null;
        int previousReservedHeight = reservedCraftingUpgradePanelHeight();
        ToolCapabilityDescriptor previousPanelRef = craftingUpgradePanelRef;

        if (!allowRebuild && previousPanelRef != null && previousPanelRef.live()) {
            craftingUpgradePanelRef = previousPanelRef;
            dockedToolPanel = reconcileCraftingUpgradePanel(dockedToolPanel, screenContext, previousPanelRef);
            craftingUpgradeOpenRequested = false;
            return false;
        }

        ToolCapabilityDescriptor resolvedPanelRef = resolveCraftingUpgradePanel(screenContext);
        craftingUpgradePanelRef = resolvedPanelRef;
        dockedToolPanel = reconcileCraftingUpgradePanel(dockedToolPanel, screenContext, resolvedPanelRef);

        if (resolvedPanelRef == null || resolvedPanelRef.live()) {
            craftingUpgradeOpenRequested = false;
        } else if (!craftingUpgradeOpenRequested && InventoryHostToolCoordinator.activationCommand(resolvedPanelRef) != null) {
            if (screenContext.host().providerSession() != null
                    && screenContext.host().providerSession().activateTool(observedMenu(), resolvedPanelRef.id())) {
                craftingUpgradeOpenRequested = true;
                schedulePostActionRefresh();
                SlotDebugLog.log(
                        "Requested provider-owned crafting tool open while SLOT owns the carried screen: menu={} toolId={} provider={}",
                        screenContext.menu().getClass().getName(),
                        resolvedPanelRef.id(),
                        resolvedPanelRef.providerId()
                );
            }
        }

        if (resolvedPanelRef != null && resolvedPanelRef.live() && resolvedPanelRef.toolSpec() != null && !hadLiveCraftingPanel) {
            SlotDebugLog.log(
                    "Attached Sophisticated Backpack crafting upgrade panel to SLOT carried screen: menu={} inputSlots={} resultSlot={}",
                    screenContext.menu().getClass().getName(),
                    resolvedPanelRef.toolSpec().menuSlotsForRole(ExternalToolSlotRole.INPUT),
                    resolvedPanelRef.toolSpec().menuSlotsForRole(ExternalToolSlotRole.OUTPUT)
            );
        }

        boolean panelReservationChanged = hadReservedPanelArea != hasCraftingUpgradePanelReservation();
        boolean livePanelChanged = hadLiveCraftingPanel != (dockedToolPanel != null);
        boolean reservedHeightChanged = previousReservedHeight != reservedCraftingUpgradePanelHeight();
        if (allowRebuild && panelGeometryInitialized && (panelReservationChanged || livePanelChanged || reservedHeightChanged)) {
            if (canRelayoutDockedToolPanelInPlace()) {
                relayoutDockedToolPanelInPlace();
                return false;
            }
            rebuildWidgets();
            return true;
        }

        return false;
    }

    private boolean canRelayoutDockedToolPanelInPlace() {
        return entryList != null
                && searchBox != null
                && sortButton != null
                && imageWidth == desiredPanelWidth()
                && imageHeight == desiredPanelHeight()
                && leftPos == desiredPanelLeft()
                && topPos == desiredPanelTop();
    }

    private void relayoutDockedToolPanelInPlace() {
        int centerX = centerPaneX();
        int centerWidth = centerPaneWidth();
        entryList.setRectangle(centerWidth, listHeight(), centerX, contentTop());
        if (dockedToolPanel != null) {
            dockedToolPanel.layout(centerX, dockedToolPanelY(), centerWidth);
        }
        if (emiPresent) {
            pendingEmiLayoutRefresh = true;
        }
        SlotDebugLog.log(
                "Relaid out SLOT carried screen in place for backpack crafting panel state: livePanel={} reservedHeight={} centerX={} centerWidth={} listHeight={}",
                dockedToolPanel != null,
                reservedCraftingUpgradePanelHeight(),
                centerX,
                centerWidth,
                listHeight()
        );
    }

    private void syncDockedToolPanelLayout() {
        if (!panelGeometryInitialized || dockedToolPanel == null) {
            return;
        }
        dockedToolPanel.layout(centerPaneX(), dockedToolPanelY(), centerPaneWidth());
    }

    private int railX() {
        return panelLeft() + 2;
    }

    protected int centerPaneX() {
        return railX() + railWidth() + COLUMN_GAP;
    }

    protected int centerPaneWidth() {
        return Math.max(180, contentRight() - centerPaneX());
    }

    private int hotbarY() {
        return panelTop() + HOTBAR_Y;
    }

    private int hotbarStripWidth() {
        return quickAccessSpanWidth(HOTBAR_SLOT_SIZE, HOTBAR_SLOT_GAP, HOTBAR_OFFHAND_GAP);
    }

    private int hotbarStripX() {
        return centerPaneX() + Math.max(0, (centerPaneWidth() - hotbarStripWidth()) / 2);
    }

    private int hotbarSlotX(int slotIndex) {
        return hotbarStripX() + quickAccessSlotOffset(slotIndex, HOTBAR_SLOT_SIZE, HOTBAR_SLOT_GAP, HOTBAR_OFFHAND_GAP);
    }

    private int hotbarSlotAt(double mouseX, double mouseY) {
        int slotY = hotbarY();
        if (mouseY < slotY || mouseY > slotY + HOTBAR_SLOT_SIZE) {
            return -1;
        }
        for (int slotIndex = 0; slotIndex < HotbarLoadoutDefinition.QUICK_ACCESS_SLOT_COUNT; slotIndex++) {
            int slotX = hotbarSlotX(slotIndex);
            if (mouseX >= slotX && mouseX <= slotX + HOTBAR_SLOT_SIZE) {
                return slotIndex;
            }
        }
        return -1;
    }

    protected int contentTop() {
        return hotbarY() + HOTBAR_SLOT_SIZE + HOTBAR_SECTION_GAP;
    }

    private int railWidth() {
        int width = MIN_RAIL_WIDTH;
        width = Math.max(width, scaledTextWidth(Component.translatable("slot.screen.debug.rail.collections").getString(), RAIL_TEXT_SCALE) + 18);
        width = Math.max(width, scaledTextWidth(Component.translatable("slot.screen.debug.rail.categories").getString(), RAIL_TEXT_SCALE) + 18);
        width = Math.max(width, scaledTextWidth(Component.translatable("slot.screen.debug.rail.mods").getString(), RAIL_TEXT_SCALE) + 18);
        for (Target target : railTargets) {
            String label = ALL_TARGET_ID.equals(target.id())
                    ? target.label()
                    : target.label() + " (" + visibleSectionCounts.getOrDefault(target.id(), 0) + ")";
            width = Math.max(width, scaledTextWidth(label, RAIL_TEXT_SCALE) + 20);
        }
        return Mth.clamp(width, MIN_RAIL_WIDTH, MAX_RAIL_WIDTH);
    }

    private boolean isMouseOverRail(double mouseX, double mouseY) {
        return mouseX >= railX()
                && mouseX <= railX() + railWidth()
                && mouseY >= contentTop() - 8
                && mouseY <= panelBottom() - OUTER_MARGIN;
    }

    private boolean handleRailScroll(double mouseX, double mouseY, double scrollY) {
        if (!isMouseOverRail(mouseX, mouseY) || railMaxScroll() <= 0) {
            return false;
        }
        return railScrollState.scrollWheel(scrollY, RAIL_ROW_HEIGHT + 6, railMaxScroll());
    }

    private int railContentHeight() {
        return InventoryRailSupport.contentHeight(railTargets, RAIL_ROW_HEIGHT, 12);
    }

    private int railViewportHeight() {
        return Math.max(0, panelBottom() - OUTER_MARGIN - contentTop());
    }

    private int railMaxScroll() {
        return RailScrollState.maxScroll(railContentHeight(), railViewportHeight());
    }

    private void clampRailScroll() {
        railScrollState.clamp(railMaxScroll());
    }

    private void renderRailScrollbar(GuiGraphics guiGraphics) {
        int maxScroll = railMaxScroll();
        if (maxScroll <= 0) {
            return;
        }

        int trackLeft = railX() + railWidth() - 3;
        int trackTop = contentTop() + 2;
        int trackBottom = panelBottom() - OUTER_MARGIN - 2;
        RailScrollState.ScrollbarThumb thumb = railScrollState.scrollbar(trackTop, trackBottom, railViewportHeight(), railContentHeight());
        if (thumb == null) {
            return;
        }
        guiGraphics.fill(trackLeft, trackTop, trackLeft + 1, trackBottom, 0x40464646);
        guiGraphics.fill(trackLeft - 1, thumb.top(), trackLeft + 2, thumb.top() + thumb.height(), 0xB07D8550);
    }

    private int listHeight() {
        int bottom = hasCraftingUpgradePanelReservation()
                ? dockedToolPanelY() - DOCKED_TOOL_PANEL_GAP
                : panelBottom() - OUTER_MARGIN;
        return Math.max(hasCraftingUpgradePanelReservation() ? 60 : 100, bottom - contentTop());
    }

    private int dockedToolPanelY() {
        return panelBottom() - OUTER_MARGIN - reservedCraftingUpgradePanelHeight();
    }

    private int panelLeft() {
        return panelGeometryInitialized ? leftPos : desiredPanelLeft();
    }

    protected int panelTop() {
        return panelGeometryInitialized ? topPos : desiredPanelTop();
    }

    private int panelRight() {
        return panelLeft() + panelWidth();
    }

    protected int panelBottom() {
        return panelTop() + panelHeight();
    }

    private int panelVisualRight() {
        if (!emiPresent) {
            return panelRight();
        }
        return Math.max(panelLeft() + 320, panelRight() - EMI_CONTENT_GAP);
    }

    private int panelVisualWidth() {
        return panelVisualRight() - panelLeft();
    }

    protected int contentRight() {
        return panelVisualRight() - OUTER_MARGIN;
    }

    protected int panelWidth() {
        return panelGeometryInitialized ? imageWidth : desiredPanelWidth();
    }

    protected int panelHeight() {
        return panelGeometryInitialized ? imageHeight : desiredPanelHeight();
    }

    @Override
    protected int slotPanelLeft() {
        return panelLeft();
    }

    @Override
    protected int slotPanelTop() {
        return panelTop();
    }

    @Override
    protected int slotPanelVisualWidth() {
        return panelVisualWidth();
    }

    @Override
    protected int slotPanelHeight() {
        return panelHeight();
    }

    private int desiredPanelLeft() {
        int availableWidth = Math.max(0, width - reservedLeftMargin() - reservedRightMargin());
        if (emiPresent) {
            return reservedLeftMargin();
        }
        return reservedLeftMargin() + Math.max(0, (availableWidth - desiredPanelWidth()) / 2);
    }

    private int desiredPanelTop() {
        return (height - desiredPanelHeight()) / 2;
    }

    private int desiredPanelWidth() {
        int availableWidth = Math.max(320, width - reservedLeftMargin() - reservedRightMargin());
        int maxWidth = emiPresent ? EMI_MAX_PANEL_WIDTH : MAX_PANEL_WIDTH;
        return Math.min(maxWidth, availableWidth);
    }

    private int desiredPanelHeight() {
        return Math.min(MAX_PANEL_HEIGHT, Math.max(hasCraftingUpgradePanelReservation() ? 260 : 180, height - 24));
    }

    private boolean hasCraftingUpgradePanelReservation() {
        return craftingUpgradePanelRef != null
                && (craftingUpgradePanelRef.live() || InventoryHostToolCoordinator.activationCommand(craftingUpgradePanelRef) != null);
    }

    private int reservedCraftingUpgradePanelHeight() {
        if (craftingUpgradePanelRef == null) {
            return 0;
        }
        return craftingUpgradePanelRef.toolSpec() == null ? 70 : craftingUpgradePanelRef.toolSpec().preferredHeight();
    }

    private int headerY() {
        return panelTop() + HEADER_Y;
    }

    protected int headerStatusY() {
        return headerY() + 11;
    }

    private int controlY() {
        return panelTop() + CONTROL_Y;
    }

    private int reservedLeftMargin() {
        if (emiPresent) {
            return EMI_LEFT_MARGIN;
        }
        return BASE_LEFT_MARGIN;
    }

    private int reservedRightMargin() {
        if (emiPresent) {
            return Math.max(BASE_RIGHT_MARGIN, Math.min(EMI_RIGHT_GUTTER_MAX, Math.max(EMI_RIGHT_GUTTER_MIN, width / 4)));
        }
        return BASE_RIGHT_MARGIN;
    }

    private ItemStack hotbarStack(int slotIndex) {
        LocalPlayer player = currentPlayer();
        if (player == null || slotIndex < 0 || slotIndex >= HotbarLoadoutDefinition.QUICK_ACCESS_SLOT_COUNT) {
            return ItemStack.EMPTY;
        }
        if (slotIndex == HotbarLoadoutDefinition.OFFHAND_SLOT_INDEX) {
            return player.getOffhandItem();
        }
        return player.getInventory().getItem(slotIndex);
    }

    private void beginHotbarDrag(InventoryViewData.EntryView entry) {
        if (!canAssignToQuickAccess(entry)) {
            return;
        }
        hotbarInteractionState.beginDrag(entry.itemEntry().identity(), entry.displayStack(), InventoryPane.CARRIED);
    }

    private boolean hasHotbarDrag() {
        return hotbarInteractionState.hasDrag();
    }

    private void clearHotbarDrag() {
        hotbarInteractionState.clearDrag();
    }

    private void armHotbarCursorInteraction(int originSlot, int mouseButton) {
        hotbarInteractionState.armCursorInteraction(originSlot, mouseButton);
    }

    private void clearHotbarCursorInteraction() {
        hotbarInteractionState.clearCursorInteraction();
    }

    private boolean handleHotbarClick(double mouseX, double mouseY, int button) {
        int hotbarSlot = hotbarSlotAt(mouseX, mouseY);
        InventoryHotbarClickSupport.HotbarClickIntent intent = InventoryHotbarClickSupport.resolve(
                hotbarSlot,
                Screen.hasShiftDown(),
                button,
                hasVisibleCarriedStack(),
                hotbarSlot >= 0 && !hotbarStack(hotbarSlot).isEmpty()
        );
        return handleHotbarClickIntent(intent, hotbarSlot, button);
    }

    private boolean handleHotbarClickIntent(InventoryHotbarClickSupport.HotbarClickIntent intent, int hotbarSlot, int button) {
        return switch (intent) {
            case IGNORED -> false;
            case STASH_SLOT -> {
                if (applyQuickAccessMutation(() -> hotbarLoadoutController.stashQuickAccessSlotMutation(hotbarSlot))) {
                    clearHotbarCursorInteraction();
                }
                yield true;
            }
            case CLICK_SLOT -> {
                if (applyQuickAccessMutation(() -> hotbarLoadoutController.clickQuickAccessSlotMutation(hotbarSlot, button))) {
                    armHotbarCursorInteraction(hotbarSlot, button);
                } else {
                    clearHotbarCursorInteraction();
                }
                yield true;
            }
            case CLEAR_CURSOR -> {
                clearHotbarCursorInteraction();
                yield false;
            }
        };
    }

    private boolean completeHotbarDrop(double mouseX, double mouseY) {
        InventoryHotbarInteractionState.DragSnapshot drag = hotbarInteractionState.consumeDrag();
        if (!drag.active()) {
            return false;
        }

        if (completeCraftingGridDragPlacement(mouseX, mouseY, drag.identity(), drag.stack())) {
            return true;
        }

        int hotbarSlot = hotbarSlotAt(mouseX, mouseY);
        if (hotbarSlot < 0 || drag.identity() == null) {
            return false;
        }

        applyQuickAccessMutation(() -> hotbarLoadoutController.assignToQuickAccessMutation(drag.identity(), hotbarSlot));
        return true;
    }

    private boolean completeHotbarCursorRelease(double mouseX, double mouseY) {
        InventoryHotbarInteractionState.CursorInteraction interaction = hotbarInteractionState.consumeCursorInteraction();
        if (!interaction.active()) {
            return false;
        }

        if (!hasVisibleCarriedStack()) {
            return false;
        }

        int hotbarSlot = hotbarSlotAt(mouseX, mouseY);
        if (hotbarSlot >= 0) {
            if (hotbarSlot != interaction.originSlot()) {
                applyQuickAccessMutation(() -> hotbarLoadoutController.clickQuickAccessSlotMutation(hotbarSlot, interaction.button()));
            }
            return true;
        }

        if (isInsideCarriedDropZone(mouseX, mouseY)) {
            return handleCursorCarriedDrop(mouseX, mouseY, interaction.button());
        }

        return false;
    }

    private boolean completeCraftingGridDragPlacement(double mouseX, double mouseY, ItemIdentity identity, ItemStack stack) {
        if (identity == null || stack.isEmpty() || hasVisibleCarriedStack()) {
            return false;
        }

        syncDockedToolPanelLayout();
        if (!(dockedToolPanel instanceof SlotBackedToolPanel slotBackedToolPanel)) {
            return false;
        }

        AbstractContainerMenu observedMenu = observedMenu();
        if (observedMenu == null || !ensureCraftingUpgradeOpen()) {
            return false;
        }

        var placement = dev.imagio.slot.client.screen.container.CraftingToolService.requestPlaceOne(
                observedMenu.containerId,
                slotBackedToolPanel,
                mouseX,
                mouseY,
                identity,
                stack,
                InventoryPane.CARRIED
        );
        if (!placement.requested()) {
            return false;
        }

        SlotDebugLog.log(
                "Requested backpack craft grid placement from drag: targetMenuSlot={} identity={}",
                placement.targetMenuSlotId().value(),
                identity.itemId()
        );
        clearSelectedEntry();
        requestImmediatePostActionRefresh();
        return true;
    }

    private InventoryViewData.EntryView hoveredAssignableEntry() {
        if (entryList == null) {
            return null;
        }
        ItemRowEntry hoveredRow = entryList.entryAtPositionAs(lastMouseX, lastMouseY, ItemRowEntry.class);
        if (hoveredRow == null) {
            return null;
        }
        return hoveredRow.entry;
    }

    private InventoryViewData.EntryView hotbarAssignmentEntry() {
        InventoryViewData.EntryView hovered = hoveredAssignableEntry();
        if (hovered != null) {
            return hovered;
        }
        if (isSearchInputActive()) {
            return null;
        }
        return selectedEntry;
    }

    private boolean shouldSyncSearchWithEmi() {
        return emiPresent && settingsController != null && settingsController.syncSearchWithEmi();
    }

    private boolean canAssignToQuickAccess(InventoryViewData.EntryView entry) {
        return InventoryRowActionPlanner.canAssignToQuickAccess(entry, hotbarLoadoutController, Set.of());
    }

    private boolean isEquippedOnly(InventoryViewData.EntryView entry) {
        return InventoryRowActionPlanner.isEquippedOnly(entry, true, hotbarLoadoutController, Set.of());
    }

    private boolean useItemFromInventory(InventoryViewData.EntryView entry) {
        if (entry == null) {
            return false;
        }

        QuickAccessInventoryActionResult result =
                hotbarLoadoutController.useFromInventoryAction(entry.itemEntry().identity());
        if (!result.started()) {
            return false;
        }

        showActionFeedback(result.feedback());
        if (minecraft != null && minecraft.screen == this) {
            refreshInventoryData();
        }
        return true;
    }

    private boolean dropItemFromInventory(InventoryViewData.EntryView entry) {
        if (entry == null) {
            return false;
        }

        QuickAccessInventoryActionResult result =
                hotbarLoadoutController.dropFromInventoryAction(entry.itemEntry().identity());
        if (!result.started()) {
            return false;
        }

        showActionFeedback(result.feedback());
        if (minecraft != null && minecraft.screen == this) {
            refreshInventoryData();
        }
        return true;
    }

    private int rowItemY(int y) {
        return rowItemY(y, ROW_HEIGHT, ROW_ITEM_SIZE);
    }

    private int rowSlotY(int y, int slotSize) {
        return rowSlotY(y, ROW_HEIGHT, slotSize);
    }

    private void openRowActionMenu(InventoryViewData.EntryView entry, boolean recentSection, int anchorX, int anchorY) {
        List<ActionMenuItem> actions = buildRowActions(entry, recentSection);
        if (actions.isEmpty()) {
            rowActionMenu = null;
            return;
        }

        rowActionMenu = createRowActionMenu(anchorX, anchorY, actions);
    }

    private List<ActionMenuItem> buildRowActions(InventoryViewData.EntryView entry, boolean recentSection) {
        return InventoryRowActionPlanner.buildActions(
                        entry,
                        true,
                        recentSection,
                        hotbarLoadoutController,
                        Set.of(),
                        collectionStore,
                        () -> useItemFromInventory(entry),
                        () -> dropItemFromInventory(entry),
                        () -> dismissRecentEntry(entry),
                        () -> toggleFavorite(entry),
                        () -> openInspectionForEntry(entry),
                        () -> toggleCollectionMembership(entry, CollectionStore.JUNK_ID),
                        () -> beginNewCollectionPrompt(entry),
                        collectionId -> () -> toggleCollectionMembership(entry, collectionId)
                ).stream()
                .map(action -> new ActionMenuItem(action.label(), action.action()))
                .toList();
    }

    private ItemIdentity nextJunkIdentityToVoid() {
        ItemIdentity bestIdentity = null;
        int bestCount = 0;
        for (InventoryViewData.EntryView entry : inventoryData.entries()) {
            if (!entry.itemEntry().collectionIds().contains(CollectionStore.JUNK_ID)) {
                continue;
            }

            int carriedCount = carriedCountForAutoVoid(entry);
            if (carriedCount <= 0) {
                continue;
            }

            if (bestIdentity == null
                    || carriedCount > bestCount
                    || (carriedCount == bestCount && entry.itemEntry().identity().itemId().compareToIgnoreCase(bestIdentity.itemId()) < 0)) {
                bestIdentity = entry.itemEntry().identity();
                bestCount = carriedCount;
            }
        }
        return bestIdentity;
    }

    private int carriedCountForAutoVoid(InventoryViewData.EntryView entry) {
        return entry.itemEntry().perSourceCounts().entrySet().stream()
                .filter(sourceEntry -> isAutoVoidCarriedSource(sourceEntry.getKey()))
                .mapToInt(Map.Entry::getValue)
                .sum();
    }

    private boolean isAutoVoidCarriedSource(String sourceId) {
        if (ChestLikeMenuLayout.SOURCE_PLAYER_ARMOR.equals(sourceId)
                || ChestLikeMenuLayout.SOURCE_PLAYER_OFFHAND.equals(sourceId)) {
            return false;
        }
        if (screenContext != null) {
            return screenContext.carriedSourceIds().contains(sourceId);
        }
        return "player_main".equals(sourceId)
                || "player_hotbar".equals(sourceId)
                || "player_backpack".equals(sourceId);
    }

    private List<Component> trashTooltipLines() {
        return SlotTrashBuffer.buildTooltipLines(trashWarningState());
    }

    private TrashSlotLayout trashSlotLayout() {
        int top = hotbarY() + Math.max(0, (HOTBAR_SLOT_SIZE - TRASH_SLOT_SIZE) / 2);
        int left = hotbarStripX() - TRASH_SLOT_GAP - TRASH_SLOT_SIZE;
        int minLeft = centerPaneX() + 4;
        if (left < minLeft) {
            left = hotbarStripX() + hotbarStripWidth() + TRASH_SLOT_GAP;
        }
        return new TrashSlotLayout(left, top, TRASH_SLOT_SIZE);
    }

    private record TrashSlotLayout(int left, int top, int size) {
        private int right() {
            return left + size;
        }

        private int bottom() {
            return top + size;
        }

        private boolean contains(double mouseX, double mouseY) {
            return mouseX >= left && mouseX <= right() && mouseY >= top && mouseY <= bottom();
        }
    }

    private SlotTrashWarningState trashWarningState() {
        if (inventoryData == null || !carriedCapacity.available() || carriedCapacity.freeSlots() > 1) {
            return SlotTrashWarningState.NONE;
        }

        ItemStack nextStack = nextJunkPreviewStack();
        if (nextStack.isEmpty()) {
            return SlotTrashWarningState.NONE;
        }
        return new SlotTrashWarningState(nextStack, false);
    }

    private ItemStack nextJunkPreviewStack() {
        ItemIdentity identity = nextJunkIdentityToVoid();
        if (identity == null) {
            return ItemStack.EMPTY;
        }

        ItemStack previewStack = previewStack(identity);
        if (previewStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        previewStack.setCount(1);
        return previewStack;
    }

    private void openSectionActionMenu(InventoryViewData.Section section, int anchorX, int anchorY) {
        List<ActionMenuItem> actions = buildSectionActions(section, anchorX, anchorY);
        if (actions.isEmpty()) {
            rowActionMenu = null;
            return;
        }
        rowActionMenu = createRowActionMenu(anchorX, anchorY, actions);
    }

    private List<ActionMenuItem> buildSectionActions(InventoryViewData.Section section, int anchorX, int anchorY) {
        List<ActionMenuItem> actions = new ArrayList<>();
        if (section == null) {
            return List.of();
        }
        actions.addAll(InventorySectionActionPlanner.buildSharedActions(
                section,
                collectionStore,
                collectionViewStateController,
                !recentMatchingEntries(currentSearchQuery()).isEmpty(),
                section.isCollection() && isCollectionCollapsed(section.collectionId()),
                section.isCollection() && collectionHasLoadouts(section.collectionId()),
                this::dismissVisibleRecentEntries,
                () -> captureLoadout(section.collectionId()),
                () -> {
                    String collectionName = Objects.requireNonNull(collectionStore.collectionOrNull(section.collectionId())).name();
                    openDeleteCollectionConfirmMenu(section.collectionId(), collectionName, anchorX, anchorY);
                },
                () -> toggleCollectionCollapsed(section.collectionId()),
                () -> togglePinnedLoadoutsWhenCollapsed(section.collectionId()),
                this::deleteVisibleJunkNow
        ).stream().map(action -> new ActionMenuItem(action.label(), action.action())).toList());
        return List.copyOf(actions);
    }

    private void dismissVisibleRecentEntries() {
        dismissVisibleRecentEntries(recentMatchingEntries(currentSearchQuery()));
    }

    private void deleteVisibleJunkNow() {
        AbstractContainerMenu activeMenu = activeMenu();
        if (activeMenu == null) {
            return;
        }

        List<ItemIdentity> identities = visibleJunkEntries().stream()
                .map(InventoryViewData.EntryView::itemEntry)
                .map(ItemEntry::identity)
                .distinct()
                .toList();
        if (identities.isEmpty()) {
            return;
        }

        int requested = 0;
        for (ItemIdentity identity : identities) {
            if (CursorTransferRequester.requestVoidMatchingCarriedAll(activeMenu.containerId, identity)) {
                requested++;
            }
        }
        handleRequestedActionCommon(
                requested > 0,
                SlotActionResult.requested(Component.translatable("slot.screen.collections.junk.delete_all_requested", requested)),
                actionPlan(false, true, true)
        );
    }

    private List<InventoryViewData.EntryView> visibleJunkEntries() {
        String query = currentSearchQuery();
        return inventoryData.entries().stream()
                .filter(entry -> entry.itemEntry().collectionIds().contains(CollectionStore.JUNK_ID))
                .filter(entry -> query.isBlank() || entry.searchKey().contains(query))
                .toList();
    }

    private static InventoryMenu resolveInventoryMenu() {
        LocalPlayer player = Objects.requireNonNull(Minecraft.getInstance().player, "SLOT inventory screen requires a local player");
        return player.inventoryMenu;
    }

    private static Inventory resolvePlayerInventory() {
        LocalPlayer player = Objects.requireNonNull(Minecraft.getInstance().player, "SLOT inventory screen requires a local player");
        return player.getInventory();
    }

    private abstract class ListEntry extends InventoryScreenRow {
        private ListEntry(String rowId) {
            super(rowId);
        }
    }

    private final class CollectionLoadoutEntry extends ListEntry {
        private final InventoryViewData.Section section;

        private CollectionLoadoutEntry(String rowId, InventoryViewData.Section section) {
            super(rowId);
            this.section = section;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick) {
            HotbarLoadoutDefinition loadout = selectedLoadout(section.collectionId());
            var rowLayout = loadoutRowSupport.renderRow(
                    guiGraphics,
                    section.collectionId(),
                    loadout,
                    collectionStore.loadoutsFor(section.collectionId()).size(),
                    new InventoryLoadoutRowSupport.LoadoutRowOptions(ROW_HEIGHT, true, 3),
                    x,
                    y,
                    width,
                    mouseX,
                    mouseY
            );
            if (rowLayout == null) {
                return;
            }

            for (int slotIndex = 0; slotIndex < rowLayout.visiblePreviewSlots(); slotIndex++) {
                int slotX = rowLayout.previewX() + quickAccessSlotOffset(slotIndex, LOADOUT_SLOT_SIZE, LOADOUT_SLOT_GAP, LOADOUT_OFFHAND_GAP);
                int slotY = rowSlotY(y, LOADOUT_SLOT_SIZE);
                guiGraphics.fill(slotX, slotY, slotX + LOADOUT_SLOT_SIZE, slotY + LOADOUT_SLOT_SIZE, 0x60303030);
                ItemIdentity identity = loadout.identityForQuickAccessSlot(slotIndex);
                if (identity == null) {
                    continue;
                }
                ItemStack previewStack = previewStack(identity);
                if (!previewStack.isEmpty()) {
                    renderScaledItem(guiGraphics, previewStack, slotX, slotY, LOADOUT_SLOT_SIZE / 16.0F);
                }
                renderQuickAccessOverlay(guiGraphics, slotIndex, slotX, slotY, 0.52F);
            }

            if (rowLayout.hiddenPreviewSlots() > 0) {
                drawScaledText(
                        guiGraphics,
                        "+" + rowLayout.hiddenPreviewSlots(),
                        rowLayout.previewX() + loadoutPreviewSpanWidth(rowLayout.visiblePreviewSlots()),
                        rowTextY(y),
                        0xA0A0A0,
                        ROW_TEXT_SCALE
                );
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button != 0 && button != 1) {
                return false;
            }

            int rowLeft = entryList.getRowLeft();
            int rowTop = entryList.rowTop(entryList.children().indexOf(this));
            int rowWidth = entryList.getRowWidth();
            var rowLayout = loadoutRowSupport.rowLayout(
                    section.collectionId(),
                    selectedLoadout(section.collectionId()),
                    collectionStore.loadoutsFor(section.collectionId()).size(),
                    new InventoryLoadoutRowSupport.LoadoutRowOptions(ROW_HEIGHT, true, 3),
                    rowLeft,
                    rowTop,
                    rowWidth
            );
            return switch (loadoutRowSupport.clickTarget(rowLayout, rowTop, ROW_HEIGHT, mouseX, mouseY)) {
                case PREVIOUS -> {
                    cycleLoadout(section.collectionId(), -1);
                    yield true;
                }
                case NEXT -> {
                    cycleLoadout(section.collectionId(), 1);
                    yield true;
                }
                case HOTKEY -> {
                    if (button == 1) {
                        clearSelectedLoadoutHotkey(section.collectionId());
                    } else {
                        cycleSelectedLoadoutHotkey(section.collectionId());
                    }
                    yield true;
                }
                case NAME -> {
                    beginInlineLoadoutRename(section.collectionId());
                    yield true;
                }
                case UPDATE -> {
                    updateSelectedLoadout(section.collectionId());
                    yield true;
                }
                case APPLY -> {
                    applySelectedLoadout(section.collectionId());
                    yield true;
                }
                case DELETE -> {
                    deleteSelectedLoadout(section.collectionId());
                    yield true;
                }
                case NONE -> true;
            };
        }

        private InlineButton hotkeyButton(int x, int y, int width) {
            return loadoutRowSupport.rowLayout(
                    section.collectionId(),
                    selectedLoadout(section.collectionId()),
                    collectionStore.loadoutsFor(section.collectionId()).size(),
                    new InventoryLoadoutRowSupport.LoadoutRowOptions(ROW_HEIGHT, true, 3),
                    x,
                    y,
                    width
            ).hotkeyButton();
        }
    }

    private void renderQuickAccessOverlay(GuiGraphics guiGraphics, int slotIndex, int slotX, int slotY, float scale) {
        if (slotIndex != HotbarLoadoutDefinition.OFFHAND_SLOT_INDEX) {
            return;
        }
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 250.0F);
        guiGraphics.fill(slotX + 1, slotY + 1, slotX + 7, slotY + 7, 0xB0101010);
        drawScaledText(guiGraphics, "F", slotX + 2, slotY + 1, 0xD8D8D8, scale);
        guiGraphics.pose().popPose();
    }

    private final class ItemRowEntry extends ListEntry {
        private final InventoryViewData.EntryView entry;
        private final String collectionId;
        private final boolean recentSection;
        private final String sourceSummary;

        private ItemRowEntry(String rowId, InventoryViewData.EntryView entry, String collectionId, boolean recentSection) {
            super(rowId);
            this.entry = entry;
            this.collectionId = collectionId;
            this.recentSection = recentSection;
            this.sourceSummary = sourceSummary(entry.itemEntry());
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick) {
            boolean collectionTracked = collectionId != null;
            boolean equippedOnly = isEquippedOnly(entry);
            String summaryText = equippedOnly
                    ? Component.translatable("slot.screen.hotbar.equipped").getString()
                    : sourceSummary;
            itemRowSupport.renderRow(
                    guiGraphics,
                    new InventoryItemRowSupport.RowPresentation(
                            entry,
                            entry.itemEntry().totalCount(),
                            collectionId,
                            collectionTracked,
                            desiredCount(),
                            summaryText,
                            equippedOnly,
                            countTextColor(),
                            collectionTracked && entry.itemEntry().totalCount() == 0 ? 0xB8B8B8 : 0xFFFFFF,
                            Math.min(110, Math.max(52, width / 3))
                    ),
                    itemRowOptions(),
                    x,
                    y,
                    width
            );
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button != 0 && button != 1) {
                return false;
            }

            entryList.setSelected(this);
            setSelectedEntry(entry, rowId());

            InventoryItemRowSupport.ClickTarget clickTarget = itemRowSupport.clickTarget(rowLayout(), collectionId != null, mouseX);
            ItemStack visibleFloatingStack = visibleFloatingStack();
            boolean cursorCarryingStack = !visibleFloatingStack.isEmpty();
            boolean cursorMatchesRow = cursorCarryingStack
                    && ItemBehaviorPolicy.matchesMovableIdentity(visibleFloatingStack, entry.itemEntry().identity());
            InventoryItemRowClickSupport.ClickIntent intent = InventoryItemRowClickSupport.resolve(
                    button,
                    clickTarget,
                    Screen.hasShiftDown(),
                    Screen.hasControlDown(),
                    cursorCarryingStack,
                    cursorMatchesRow,
                    entry.itemEntry().totalCount(),
                    InventoryItemRowClickSupport.EmptyPrimaryClick.START_HOTBAR_DRAG
            );
            return InventoryItemRowClickSupport.execute(
                    intent,
                    new InventoryItemRowClickSupport.RowClickActions(
                            () -> openRowActionMenu(entry, recentSection, iconMenuX(), actionMenuY()),
                            () -> beginInlineDesiredCountEdit(collectionId, entry.itemEntry().identity()),
                            () -> openRowActionMenu(entry, recentSection, actionMenuX(), actionMenuY()),
                            () -> requestCursorPickup(entry, CursorTransferPayload.Mode.HALF),
                            () -> requestCursorCarriedDrop(button),
                            () -> runMoveAllType(entry),
                            () -> runMoveOne(entry),
                            () -> requestCursorPickup(entry, CursorTransferPayload.Mode.STACK),
                            () -> beginHotbarDrag(entry)
                    )
            );
        }

        private InventoryItemRowSupport.RowLayout rowLayout() {
            int rowIndex = entryList.children().indexOf(this);
            return itemRowSupport.rowLayout(entryList.getRowLeft(), entryList.rowTop(rowIndex), entryList.getRowWidth(), itemRowOptions());
        }

        private boolean isActionHit(double mouseX) {
            return itemRowSupport.clickTarget(rowLayout(), false, mouseX) == InventoryItemRowSupport.ClickTarget.ACTION;
        }

        private int actionMenuX() {
            return itemRowSupport.actionMenuX(entryList.getRowRight());
        }

        private int iconMenuX() {
            return itemRowSupport.iconMenuX(rowLayout(), itemRowOptions());
        }

        private int actionMenuY() {
            return itemRowSupport.actionMenuY(rowLayout(), itemRowOptions());
        }

        @Override
        public Component getNarration() {
            return Component.literal(entry.displayName());
        }

        private int desiredCount() {
            return collectionId == null ? 0 : Math.max(1, collectionStore.desiredCount(collectionId, entry.itemEntry().identity()));
        }

        private int countTextColor() {
            if (collectionId == null) {
                return 0xEAEAEA;
            }
            int ownedCount = entry.itemEntry().totalCount();
            int desiredCount = desiredCount();
            if (ownedCount <= 0) {
                return 0xE0A0A0;
            }
            if (ownedCount < desiredCount) {
                return 0xE0C090;
            }
            return 0xB8D0B8;
        }
    }

    private InventoryItemRowSupport.RowOptions itemRowOptions() {
        return new InventoryItemRowSupport.RowOptions(
                ROW_HEIGHT,
                ROW_ITEM_SIZE,
                ROW_ITEM_SCALE,
                ROW_ACTION_COLUMN_WIDTH,
                ROW_COUNT_COLUMN_WIDTH,
                ROW_LEADING_COUNT_COLUMN_WIDTH,
                24
        );
    }

}
