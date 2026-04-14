package dev.imagio.slot.client.screen.container;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.client.SlotClientCompat;
import dev.imagio.slot.client.collection.HotbarLoadoutDefinition;
import dev.imagio.slot.client.collection.CollectionStore;
import dev.imagio.slot.client.collection.CollectionStockSummary;
import dev.imagio.slot.client.collection.CollectionViewStateController;
import dev.imagio.slot.client.model.ItemEntry;
import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.client.policy.ItemBehaviorPolicy;
import dev.imagio.slot.client.screen.AbstractInventoryBrowserScreen;
import dev.imagio.slot.client.screen.ActionMenuItem;
import dev.imagio.slot.client.screen.CarriedTransferService;
import dev.imagio.slot.client.screen.ContainerFloatingItemRenderSupport;
import dev.imagio.slot.client.screen.EmiLayoutSync;
import dev.imagio.slot.client.screen.InventoryCursorActionSupport;
import dev.imagio.slot.client.screen.InventoryCursorDropTargetSupport;
import dev.imagio.slot.client.screen.InventoryHotbarClickSupport;
import dev.imagio.slot.client.screen.InventoryHotbarInteractionState;
import dev.imagio.slot.client.screen.InventoryItemRowClickSupport;
import dev.imagio.slot.client.screen.InventoryItemRowSupport;
import dev.imagio.slot.client.screen.InventoryLoadoutRowSupport;
import dev.imagio.slot.client.screen.InventoryProjectionSelectionList;
import dev.imagio.slot.client.screen.InventoryRefreshDelayState;
import dev.imagio.slot.client.screen.InventoryRowActionPlanner;
import dev.imagio.slot.client.screen.InventoryMenuStateHash;
import dev.imagio.slot.client.screen.InventoryRailSupport;
import dev.imagio.slot.client.screen.InventoryRailSupport.Kind;
import dev.imagio.slot.client.screen.InventoryRailSupport.Target;
import dev.imagio.slot.client.screen.InventorySectionActionPlanner;
import dev.imagio.slot.client.screen.InventorySectionHeaderSupport;
import dev.imagio.slot.client.screen.InventorySelectionRestoreSupport;
import dev.imagio.slot.client.screen.InventoryScreenRow;
import dev.imagio.slot.client.screen.InventorySectionScreenRow;
import dev.imagio.slot.client.screen.QuickAccessInventoryActionResult;
import dev.imagio.slot.client.screen.SlotCollectionManagementScreen;
import dev.imagio.slot.client.screen.InventoryScreenContext;
import dev.imagio.slot.client.screen.InventoryTransferActionSupport;
import dev.imagio.slot.client.screen.InventoryCapacityIndicator;
import dev.imagio.slot.client.screen.InlineDesiredCountState;
import dev.imagio.slot.client.screen.InlineLoadoutRenameState;
import dev.imagio.slot.client.screen.NewCollectionPromptState;
import dev.imagio.slot.client.screen.PopupActionMenu;
import dev.imagio.slot.client.screen.SlotActionResult;
import dev.imagio.slot.client.screen.SlotItemInspectionScreen;
import dev.imagio.slot.client.screen.SlotUndoHistory;
import dev.imagio.slot.client.screen.ReflectiveContainerRenderHooks;
import dev.imagio.slot.client.screen.RailScrollState;
import dev.imagio.slot.client.screen.SlotSettingsScreen;
import dev.imagio.slot.client.screen.SlotTrashBuffer;
import dev.imagio.slot.client.screen.SlotTrashWarningState;
import dev.imagio.slot.client.screen.SlotTooltipRenderer;
import dev.imagio.slot.projection.InventoryViewData;
import dev.imagio.slot.client.screen.debug.SlotDebugSortMode;
import dev.imagio.slot.session.ChestLikeMenuLayout;
import dev.imagio.slot.session.InventoryHostDescriptor;
import dev.imagio.slot.network.BackpackTransferRequester;
import dev.imagio.slot.network.CursorTransferPayload;
import dev.imagio.slot.network.CursorTransferRequester;
import dev.imagio.slot.projection.InventoryProjection;
import dev.imagio.slot.projection.InventoryProjectionBuilder;
import dev.imagio.slot.storage.adapter.ExternalStorageStackSnapshot;
import dev.imagio.slot.workflow.InspectionService;
import dev.imagio.slot.workflow.SearchWorkflowService;
import dev.imagio.slot.workflow.SettingsService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Supplier;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import dev.imagio.slot.projection.InventoryPane;

public final class SlotInventoryWorkspaceScreen<T extends AbstractContainerMenu> extends AbstractInventoryBrowserScreen<T> {
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
    private static final int PANE_GAP = 6;
    private static final int ROW_HEIGHT = 13;
    private static final int RAIL_ROW_HEIGHT = 13;
    private static final int PANE_HEADER_BASE_HEIGHT = 12;
    private static final int PANE_HEADER_WARNING_HEIGHT = 8;
    private static final int DOCKED_TOOL_PANEL_GAP = 4;
    private static final int COLLECTION_BUTTON_HEIGHT = 9;
    private static final int COLLECTION_BUTTON_GAP = 3;
    private static final int COLLECTION_PIN_BUTTON_WIDTH = 16;
    private static final int COLLECTION_TOGGLE_BUTTON_WIDTH = 12;
    private static final int COLLECTION_MENU_BUTTON_WIDTH = 20;
    private static final int COLLECTION_RESTOCK_BUTTON_WIDTH = 24;
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
    private static final int MAX_PANEL_WIDTH = 940;
    private static final int MAX_SINGLE_PANE_PANEL_WIDTH = 900;
    private static final int EMI_MAX_PANEL_WIDTH = 840;
    private static final int EMI_MAX_SINGLE_PANE_PANEL_WIDTH = 720;
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

    private final SlotContainerInventoryDataBuilder dataBuilder = new SlotContainerInventoryDataBuilder();
    private final ChestLikeMenuLayout layout;
    private final CarriedTransferService actionExecutor;
    private final Runnable openVanillaAction;
    private final boolean emiPresent;
    private final SettingsService settingsController;
    private final Runnable currentScreenToggleAction;
    private final Set<String> emiAggregateSourceIds;

    private List<ExternalStorageStackSnapshot> cachedPrimarySnapshots = List.of();
    private int cachedPrimarySnapshotHash = 1;
    private List<EmiAggregateStackView> cachedEmiAggregateStacks = List.of();
    private InventoryProjectionSelectionList<InventoryScreenRow> openContainerList;
    private InventoryProjectionSelectionList<InventoryScreenRow> carriedList;
    private InventoryPane selectedPane = InventoryPane.OPEN_CONTAINER;
    private List<Target> railTargets = List.of();
    private Map<String, Integer> visibleSectionCounts = Map.of();
    private final RailScrollState railScrollState = new RailScrollState();
    private Map<InventoryPane, Integer> paneVisibleCounts = Map.of(InventoryPane.OPEN_CONTAINER, 0, InventoryPane.CARRIED, 0);
    private Map<InventoryPane, Map<String, Integer>> paneSectionCounts = Map.of(
            InventoryPane.OPEN_CONTAINER, Map.of(),
            InventoryPane.CARRIED, Map.of()
    );
    private Map<InventoryPane, Map<String, List<InventoryViewData.EntryView>>> paneVisibleEntriesBySection = Map.of(
            InventoryPane.OPEN_CONTAINER, Map.of(),
            InventoryPane.CARRIED, Map.of()
    );
    private InventoryProjection inventoryProjection = InventoryProjection.empty();
    private final InventoryHotbarInteractionState hotbarInteractionState = new InventoryHotbarInteractionState();
    private int lastMouseX;
    private int lastMouseY;
    private boolean transferDragActive;
    private final Set<DragTransferKey> transferDraggedEntries = new HashSet<>();
    private int lastObservedMenuStateHash;
    private final InventoryRefreshDelayState postActionRefreshDelay = new InventoryRefreshDelayState();
    private boolean pendingTomsPacketRefresh;
    private int pendingTomsPacketRefreshCount;
    private boolean panelGeometryInitialized;
    private boolean pendingEmiLayoutRefresh;
    private final DockedToolPanel dockedToolPanel;
    private InventoryCapacityIndicator.StorageFillStats carriedCapacity = InventoryCapacityIndicator.StorageFillStats.EMPTY;
    private InventoryCapacityIndicator.StorageFillStats externalCapacity = InventoryCapacityIndicator.StorageFillStats.EMPTY;
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

    public SlotInventoryWorkspaceScreen(
            T menu,
            Inventory playerInventory,
            Component title,
            InventoryHostDescriptor host,
            CollectionStore collectionStore,
            Runnable openVanillaAction,
            boolean emiPresent,
            SettingsService settingsController,
            CollectionViewStateController collectionViewStateController,
            Supplier<Component> currentScreenToggleLabel,
            Runnable currentScreenToggleAction
    ) {
        this(
                menu,
                playerInventory,
                title,
                host,
                collectionStore,
                openVanillaAction,
                emiPresent,
                settingsController,
                collectionViewStateController,
                currentScreenToggleLabel,
                currentScreenToggleAction,
                null,
                null
        );
    }

    public SlotInventoryWorkspaceScreen(
            T menu,
            Inventory playerInventory,
            Component title,
            InventoryHostDescriptor host,
            CollectionStore collectionStore,
            Runnable openVanillaAction,
            boolean emiPresent,
            SettingsService settingsController,
            CollectionViewStateController collectionViewStateController,
            Supplier<Component> currentScreenToggleLabel,
            Runnable currentScreenToggleAction,
            SearchWorkflowService searchWorkflow,
            InspectionService inspectionService
    ) {
        super(
                menu,
                playerInventory,
                title,
                InventoryScreenContext.carriedAndExternal(
                        Objects.requireNonNull(host, "Unsupported chest-like menu").title(),
                        host.menu(),
                        host.layout()
                ),
                collectionStore,
                collectionViewStateController,
                searchWorkflow,
                inspectionService
        );
        this.openVanillaAction = openVanillaAction;
        this.emiPresent = emiPresent || SlotClientCompat.hasEmi();
        this.settingsController = settingsController;
        this.currentScreenToggleLabel = currentScreenToggleLabel;
        this.currentScreenToggleAction = currentScreenToggleAction;
        InventoryHostDescriptor resolvedHost = Objects.requireNonNull(host, "Unsupported chest-like menu");
        if (resolvedHost.menu() != menu) {
            throw new IllegalArgumentException("host menu must match workspace menu");
        }
        this.layout = resolvedHost.layout();
        this.actionExecutor = CarriedTransferService.forWorkspace(layout);
        this.dockedToolPanel = DockedToolPanelResolver.resolve(menu, resolvedHost);
        Set<String> emiSources = new HashSet<>(layout.sourceIdsForPane(InventoryPane.OPEN_CONTAINER));
        emiSources.addAll(layout.sourceIdsForPane(InventoryPane.CARRIED));
        this.emiAggregateSourceIds = Set.copyOf(emiSources);
        this.selectedPane = screenContext.carriedOnly() ? InventoryPane.CARRIED : InventoryPane.OPEN_CONTAINER;
        this.imageWidth = 420;
        this.imageHeight = 220;
    }

    @Override
    protected void init() {
        this.imageWidth = desiredPanelWidth();
        this.imageHeight = desiredPanelHeight();
        super.init();
        this.leftPos = desiredPanelLeft();
        this.topPos = desiredPanelTop();
        this.panelGeometryInitialized = true;

        SlotDebugLog.log(
                "SLOT workspace screen init: emiPresent={} screen={} width={} height={} reservedLeft={} reservedRight={} desiredPanelWidth={} desiredPanelHeight={} panelLeft={} panelTop={} panelWidth={} panelVisualWidth={} panelHeight={} panelRight={} panelVisualRight={} imageWidth={} imageHeight={} leftPos={} topPos={} singlePane={} carriedOnlyContext={}",
                emiPresent,
                getClass().getName(),
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
                singlePaneMode(),
                screenContext.carriedOnly()
        );

        String searchValue = searchBox == null ? searchWorkflow.currentQuery() : searchBox.getValue();

        inventoryData = dataBuilder.build(screenContext, collectionStore);
        railTargets = buildRailTargets();

        int controlsX = centerPaneX();
        int controlsWidth = contentWidth();
        int sortWidth = SORT_BUTTON_WIDTH;
        int undoWidth = ICON_BUTTON_WIDTH;
        int redoWidth = ICON_BUTTON_WIDTH;
        int collectionsWidth = ICON_BUTTON_WIDTH;
        int settingsWidth = settingsController == null ? 0 : ICON_BUTTON_WIDTH;
        int toggleWidth = currentScreenToggleAction == null ? 0 : ICON_BUTTON_WIDTH;
        int vanillaWidth = ICON_BUTTON_WIDTH;
        int controlsRight = controlsX + controlsWidth;

        controlsRight -= vanillaWidth;
        vanillaButton = addRenderableWidget(Button.builder(Component.literal("V"), button -> openVanillaAction.run())
                .tooltip(Tooltip.create(Component.translatable("slot.screen.inventory.open_vanilla")))
                .bounds(controlsRight, controlY(), vanillaWidth, TOP_CONTROL_HEIGHT)
                .build());
        controlsRight -= TOP_CONTROL_GAP;

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

        int searchWidth = Math.max(100, controlsRight - controlsX - sortWidth - undoWidth - redoWidth - 20);
        searchBox = new EditBox(font, controlsX, controlY(), searchWidth, SEARCH_BOX_HEIGHT, Component.translatable("slot.screen.debug.search"));
        searchBox.setHint(Component.translatable("slot.screen.debug.search_hint"));
        searchBox.setValue(searchValue);
        searchBox.setResponder(value -> {
            searchWorkflow.remember(value);
            rebuildVisibleEntries();
        });
        addRenderableWidget(searchBox);

        inlineLoadoutRenameBox = new EditBox(font, controlsX, controlY(), 80, 14, Component.translatable("slot.screen.collections.loadout_name"));
        inlineLoadoutRenameBox.setHint(Component.translatable("slot.screen.collections.loadout_name_hint"));
        inlineLoadoutRenameBox.setMaxLength(48);
        inlineLoadoutRenameBox.setVisible(false);
        addRenderableWidget(inlineLoadoutRenameBox);

        inlineDesiredCountBox = new EditBox(font, controlsX, controlY(), 40, 14, Component.translatable("slot.screen.collections.desired_count_hint"));
        inlineDesiredCountBox.setHint(Component.translatable("slot.screen.collections.desired_count_hint"));
        inlineDesiredCountBox.setMaxLength(4);
        inlineDesiredCountBox.setFilter(value -> value.chars().allMatch(Character::isDigit));
        inlineDesiredCountBox.setVisible(false);
        addRenderableWidget(inlineDesiredCountBox);

        newCollectionNameBox = new EditBox(font, controlsX, controlY(), 120, SEARCH_BOX_HEIGHT, Component.translatable("slot.screen.collections.name"));
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

        int listTop = paneListTop();
        int openListHeight = openPaneListHeight();
        int carriedListHeight = carriedPaneListHeight();
        openContainerList = new InventoryProjectionSelectionList<>(
                Minecraft.getInstance(),
                paneWidth(),
                openListHeight,
                listTop,
                openPaneX(),
                ROW_HEIGHT,
                ALL_TARGET_ID,
                button -> button == 0 || button == 1 || button == 2,
                this::isPaneItemRow,
                this::isPaneItemRow,
                0x664B7F35
        );
        carriedList = new InventoryProjectionSelectionList<>(
                Minecraft.getInstance(),
                paneWidth(),
                carriedListHeight,
                listTop,
                carriedPaneX(),
                ROW_HEIGHT,
                ALL_TARGET_ID,
                button -> button == 0 || button == 1 || button == 2,
                this::isPaneItemRow,
                this::isPaneItemRow,
                0x664B7F35
        );
        if (!singlePaneMode()) {
            addWidget(openContainerList);
        }
        addWidget(carriedList);

        if (dockedToolPanel != null && !singlePaneMode()) {
            dockedToolPanel.layout(openPaneX(), dockedToolPanelY(), paneWidth());
        }

        sortButton.setTooltip(Tooltip.create(Component.translatable("slot.screen.debug.sort_button", sortMode.displayName())));
        updateSortButton();
        updateDynamicButtons();
        refreshInventoryData();
        searchWorkflow.initialize(searchField(), shouldSyncSearchWithEmi(), EMI_SEARCH_PEER);
        setInitialFocus(searchBox);
        pendingEmiLayoutRefresh = emiPresent;
        EmiLayoutSync.refreshIfPresent("slot_container_screen_init");
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (dockedToolPanel != null) {
            dockedToolPanel.containerTick();
        }
        hotbarLoadoutController.tick();
        searchWorkflow.tick(searchField(), shouldSyncSearchWithEmi(), EMI_SEARCH_PEER);
        applyConfirmedActionOutcome();
        if (pendingTomsPacketRefresh) {
            int coalescedPacketCount = pendingTomsPacketRefreshCount;
            pendingTomsPacketRefresh = false;
            pendingTomsPacketRefreshCount = 0;
            postActionRefreshDelay.clear();
            long refreshStart = System.nanoTime();
            refreshInventoryData();
            logTomsPerf(
                    "Coalesced Tom's packet refresh: packets={} refreshMs={}",
                    refreshStart,
                    8.0,
                    coalescedPacketCount,
                    millisSince(refreshStart)
            );
            actionFeedback.tick();
            attemptAutoVoidJunk();
            return;
        }
        int currentHash = computeObservedMenuStateHash();
        if (currentHash != lastObservedMenuStateHash) {
            SlotDebugLog.log(
                    "Container contents changed while SLOT screen is open: menu={} oldHash={} newHash={}",
                    menu.getClass().getName(),
                    lastObservedMenuStateHash,
                    currentHash
            );
            refreshInventoryData();
            postActionRefreshDelay.clear();
            return;
        }

        if (postActionRefreshDelay.active()) {
            if (postActionRefreshDelay.tick(isTomsStorageWorkspace())) {
                SlotDebugLog.log(
                        "Refreshing SLOT container view while awaiting menu sync: menu={} ticksRemaining={}",
                        menu.getClass().getName(),
                        postActionRefreshDelay.ticksRemaining()
                );
                refreshInventoryData();
            }
        }

        actionFeedback.tick();
        attemptAutoVoidJunk();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public boolean handleTomsStorageClientPacket(CompoundTag tag) {
        if (tag == null || !isTomsStorageWorkspace()) {
            return false;
        }

        try {
            long applyStart = System.nanoTime();
            boolean applied = applyTomsStorageClientPacket(tag);
            SlotDebugLog.log(
                    "Applied Tom's terminal client packet to menu: menu={} keys={} applied={}",
                    menu.getClass().getName(),
                    describeTagKeys(tag),
                    applied
            );
            if (applied) {
                postActionRefreshDelay.clear();
                pendingTomsPacketRefresh = true;
                pendingTomsPacketRefreshCount++;
                logTomsPerf(
                        "Applied Tom's terminal client packet: keys={} applyMs={} queuedPackets={}",
                        applyStart,
                        3.0,
                        describeTagKeys(tag),
                        millisSince(applyStart),
                        pendingTomsPacketRefreshCount
                );
            }
            return applied;
        } catch (ReflectiveOperationException exception) {
            SlotDebugLog.log(
                    "Failed to apply Tom's terminal client packet: menu={} error={}",
                    menu.getClass().getName(),
                    exception.getClass().getName()
            );
            return false;
        }
    }

    public List<EmiAggregateStackView> emiAggregateStacks() {
        return cachedEmiAggregateStacks;
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
        if (!isSearchInputActive()
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
            PaneItemRowEntry assignmentRow = hotbarAssignmentRow();
            if (assignmentRow != null) {
                applyQuickAccessMutation(() -> hotbarLoadoutController.assignToQuickAccessMutation(
                        assignmentRow.entry.itemEntry().identity(),
                        hotbarIndex,
                        layout.sourceIdsForPane(assignmentRow.pane)
                ));
                return true;
            }
        }
        if (minecraft != null && minecraft.options.keySwapOffhand.matches(keyCode, scanCode)) {
            PaneItemRowEntry assignmentRow = hotbarAssignmentRow();
            if (assignmentRow != null) {
                applyQuickAccessMutation(() -> hotbarLoadoutController.assignToQuickAccessMutation(
                        assignmentRow.entry.itemEntry().identity(),
                        HotbarLoadoutDefinition.OFFHAND_SLOT_INDEX,
                        layout.sourceIdsForPane(assignmentRow.pane)
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
        if (handlePaneHeaderClick(mouseX, mouseY, button)) {
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
        if (DockedToolPanelInteractionSupport.dispatchClick(
                dockedToolPanel,
                mouseX,
                mouseY,
                button,
                null,
                () -> {
                    consumeDockedToolPanelRefreshRequest();
                    setFocused(null);
                    if (button == 0) {
                        setDragging(true);
                    }
                }
        )) {
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
        if (handleCursorPaneDrop(mouseX, mouseY, button)) {
            return true;
        }
        if (!singlePaneMode() && openContainerList != null && openContainerList.mouseClicked(mouseX, mouseY, button)) {
            setFocused(openContainerList);
            if (button == 0) {
                setDragging(true);
            }
            return true;
        }
        if (carriedList != null && carriedList.mouseClicked(mouseX, mouseY, button)) {
            setFocused(carriedList);
            if (button == 0) {
                setDragging(true);
            }
            return true;
        }
        return false;
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

        SlotUndoHistory.ApplyResult result = undo
                ? SlotUndoHistory.undo(undoHistoryContext())
                : SlotUndoHistory.redo(undoHistoryContext());
        applyUndoHistoryResult(result, undo);
        return true;
    }

    private boolean handlePaneHeaderClick(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }
        if (!singlePaneMode()) {
            InlineButton openButton = paneHeaderMenuButton(openPaneX(), InventoryPane.OPEN_CONTAINER);
            if (openButton.enabled() && openButton.contains(mouseX, mouseY)) {
                openPaneActionMenu(InventoryPane.OPEN_CONTAINER, openButton.x(), openButton.y() + openButton.height());
                return true;
            }
        }

        InlineButton carriedButton = paneHeaderMenuButton(carriedPaneX(), InventoryPane.CARRIED);
        if (carriedButton.enabled() && carriedButton.contains(mouseX, mouseY)) {
            openPaneActionMenu(InventoryPane.CARRIED, carriedButton.x(), carriedButton.y() + carriedButton.height());
            return true;
        }
        return false;
    }

    private boolean handleCraftingGridSelectionClick(double mouseX, double mouseY, int button) {
        if (button != 0 || !(dockedToolPanel instanceof SlotBackedToolPanel slotBackedToolPanel)) {
            return false;
        }
        if (!menu.getCarried().isEmpty() || selectedEntry == null) {
            return false;
        }

        ItemIdentity identity = selectedEntry.itemEntry().identity();
        ItemStack selectedStack = selectedEntry.displayStack();
        CraftingToolService.PlacementRequestResult placement = CraftingToolService.requestPlaceOne(
                menu.containerId,
                slotBackedToolPanel,
                mouseX,
                mouseY,
                identity,
                selectedStack,
                selectedPane
        );
        if (!placement.requested()) {
            return false;
        }

        SlotDebugLog.log(
                "Requested craft grid placement from selection: pane={} targetMenuSlot={} identity={}",
                selectedPane,
                placement.targetMenuSlotId().value(),
                identity.itemId()
        );
        if (isTomsStorageWorkspace()) {
            schedulePostActionRefresh();
            return true;
        }
        requestImmediatePostActionRefresh();
        return true;
    }

    private boolean handleCursorPaneDrop(double mouseX, double mouseY, int button) {
        PaneItemRowEntry hoveredRow = hoveredItemRow(mouseX, mouseY);
        InventoryProjectionSelectionList<InventoryScreenRow> paneList = hoveredPaneList(mouseX, mouseY);
        InventoryPane hoveredPane = paneList == null
                ? null
                : paneList == openContainerList ? InventoryPane.OPEN_CONTAINER : InventoryPane.CARRIED;
        InventoryCursorDropTargetSupport.DropTarget target = InventoryCursorDropTargetSupport.paneDropTarget(
                button,
                !menu.getCarried().isEmpty(),
                isOverDockedToolPanel(mouseX, mouseY),
                hoveredRow != null,
                hoveredPane
        );
        if (!target.present()) {
            return false;
        }
        return requestCursorPaneDrop(target.pane(), button);
    }

    private boolean requestCursorPaneDrop(InventoryPane targetPane, int button) {
        if (targetPane == null || (button != 0 && button != 1) || menu.getCarried().isEmpty()) {
            return false;
        }

        selectedPane = targetPane;

        InventoryCursorActionSupport.RequestedCursorAction action = InventoryCursorActionSupport.drop(
                menu.containerId,
                targetPane,
                button == 1
        );
        if (action.requested()) {
            SlotDebugLog.log(
                    "Requested cursor drop into pane: pane={} mode={}",
                    targetPane,
                    action.modeLabel()
            );
        }
        return handleRequestedActionCommon(
                action.requested(),
                action.requestedResult(),
                actionPlan(
                        false,
                        false,
                        false,
                        () -> queueTransferRefresh(targetPane),
                        null
                )
        );
    }

    private boolean handleTrashSlotClick(double mouseX, double mouseY, int button) {
        if ((button != 0 && button != 1) || menu.getCarried().isEmpty() || !trashSlotLayout().contains(mouseX, mouseY)) {
            return false;
        }

        ItemStack trashedPreview = menu.getCarried().copy();
        if (button == 1) {
            trashedPreview.setCount(1);
        }

        InventoryCursorActionSupport.RequestedCursorAction action = InventoryCursorActionSupport.trash(
                menu.containerId,
                button == 1
        );
        if (action.requested()) {
            SlotDebugLog.log(
                    "Requested cursor trash from workspace screen: mode={}",
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
        if (menu.getCarried().isEmpty() || !trashSlotLayout().contains(mouseX, mouseY)) {
            return false;
        }
        return handleTrashSlotClick(mouseX, mouseY, 0);
    }

    private void consumeDockedToolPanelRefreshRequest() {
        DockedToolPanelInteractionSupport.consumeRefreshRequest(
                dockedToolPanel,
                () -> {
                    if (isTomsStorageWorkspace()) {
                        schedulePostActionRefresh();
                        return;
                    }
                    requestImmediatePostActionRefresh();
                }
        );
    }

    private boolean handleLoadoutHotkeyClick(double mouseX, double mouseY, int button) {
        if ((button != 0 && button != 1) || carriedList == null) {
            return false;
        }

        CollectionLoadoutEntry hoveredLoadout = collectionLoadoutEntry(carriedList.entryAtPosition(mouseX, mouseY));
        if (hoveredLoadout == null) {
            return false;
        }

        int rowIndex = carriedList.children().indexOf(hoveredLoadout);
        if (rowIndex < 0) {
            return false;
        }

        int rowLeft = carriedList.getRowLeft();
        int rowTop = carriedList.rowTop(rowIndex);
        int rowWidth = carriedList.getRowWidth();
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

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && completeHotbarDrop(mouseX, mouseY)) {
            clearTransferDrag();
            setDragging(false);
            return true;
        }
        if ((button == 0 || button == 1) && completeHotbarCursorRelease(mouseX, mouseY)) {
            clearTransferDrag();
            setDragging(false);
            return true;
        }
        if (button == 0 && isDragging() && handleTrashSlotRelease(mouseX, mouseY)) {
            clearTransferDrag();
            setDragging(false);
            return true;
        }
        clearTransferDrag();
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
        for (GuiEventListener child : children()) {
            if (child == focused || !isExternalChild(child)) {
                continue;
            }
            if (child.mouseReleased(mouseX, mouseY, button)) {
                return true;
            }
        }
        if (openContainerList != focused && openContainerList != null && openContainerList.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        return carriedList != focused && carriedList != null && carriedList.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && transferDragActive) {
            continueTransferDrag(mouseX, mouseY);
            return true;
        }
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
        for (GuiEventListener child : children()) {
            if (!isExternalChild(child)) {
                continue;
            }
            if (child.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
                return true;
            }
        }
        if (DockedToolPanelInteractionSupport.dispatchScroll(
                dockedToolPanel,
                mouseX,
                mouseY,
                scrollX,
                scrollY,
                null,
                this::consumeDockedToolPanelRefreshRequest
        )) {
            return true;
        }
        if (handleRailScroll(mouseX, mouseY, scrollY)) {
            return true;
        }

        if (Screen.hasShiftDown()) {
            PaneItemRowEntry hoveredRow = hoveredItemRow(mouseX, mouseY);
            if (hoveredRow != null && !hoveredRow.isActionHit(mouseX)) {
                runMoveOne(hoveredRow.entry, hoveredRow.pane);
                return true;
            }
        }

        InventoryProjectionSelectionList<InventoryScreenRow> hoveredPaneList = hoveredPaneList(mouseX, mouseY);
        if (hoveredPaneList != null && hoveredPaneList.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
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

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        inlineDesiredCountState.beginFrame();
        inlineLoadoutRenameState.beginFrame();
        if (inlineDesiredCountBox != null) {
            inlineDesiredCountBox.setVisible(false);
        }
        if (inlineLoadoutRenameBox != null) {
            inlineLoadoutRenameBox.setVisible(false);
        }
        if (pendingEmiLayoutRefresh) {
            pendingEmiLayoutRefresh = false;
            EmiLayoutSync.refreshIfPresent("slot_container_screen_first_render");
        }
        renderTransparentBackground(guiGraphics);
        renderPanels(guiGraphics);
        CONTAINER_RENDER_HOOKS.postBackground(this, guiGraphics, mouseX, mouseY);
        renderExternalRenderables(guiGraphics, mouseX, mouseY, partialTick);

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
        vanillaButton.render(guiGraphics, mouseX, mouseY, partialTick);
        if (!singlePaneMode()) {
            openContainerList.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        carriedList.render(guiGraphics, mouseX, mouseY, partialTick);
        if (isInlineDesiredCountActive()) {
            if (inlineDesiredCountState.shouldCancelAfterLayout()) {
                cancelInlineDesiredCount();
            } else {
                inlineDesiredCountBox.render(guiGraphics, mouseX, mouseY, partialTick);
            }
        }
        if (isInlineLoadoutRenameActive()) {
            if (inlineLoadoutRenameState.shouldCancelAfterLayout()) {
                cancelInlineLoadoutRename();
            } else {
                inlineLoadoutRenameBox.render(guiGraphics, mouseX, mouseY, partialTick);
            }
        }
        if (dockedToolPanel != null && !singlePaneMode()) {
            dockedToolPanel.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        renderRail(guiGraphics, mouseX, mouseY);
        renderHeader(guiGraphics);
        renderLiveHotbar(guiGraphics, mouseX, mouseY);
        if (!singlePaneMode()) {
            renderPaneHeader(guiGraphics, openPaneX(), InventoryPane.OPEN_CONTAINER);
        }
        renderPaneHeader(guiGraphics, carriedPaneX(), InventoryPane.CARRIED);
        if (!singlePaneMode()) {
            renderPaneEmptyState(guiGraphics, openContainerList, openPaneX());
        }
        renderPaneEmptyState(guiGraphics, carriedList, carriedPaneX());
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
        boolean overDockedToolPanel = dockedToolPanel != null && !singlePaneMode() && dockedToolPanel.contains(mouseX, mouseY);
        PaneItemRowEntry hoveredRow = hoveredItemRow(mouseX, mouseY);
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

    private void renderExternalRenderables(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        for (GuiEventListener child : children()) {
            if (!(child instanceof Renderable renderable) || isOwnedRenderable(renderable)) {
                continue;
            }
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    private boolean hasVisibleCarriedStack() {
        return !menu.getCarried().isEmpty();
    }

    private boolean hasRenderableFloatingItems() {
        return ContainerFloatingItemRenderSupport.hasRenderableFloatingItems(this, menu.getCarried());
    }

    private boolean isOverDockedToolPanel(double mouseX, double mouseY) {
        return DockedToolPanelInteractionSupport.isOver(dockedToolPanel, mouseX, mouseY);
    }

    private ItemStack visibleFloatingStack() {
        return ContainerFloatingItemRenderSupport.visibleFloatingStack(this, menu.getCarried());
    }

    private void renderFloatingItems(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        ContainerFloatingItemRenderSupport.renderFloatingItems(
                this,
                font,
                guiGraphics,
                leftPos,
                topPos,
                menu.getCarried(),
                mouseX,
                mouseY
        );
    }

    private boolean isOwnedRenderable(Renderable renderable) {
        return renderable == searchBox
                || renderable == inlineDesiredCountBox
                || renderable == inlineLoadoutRenameBox
                || renderable == newCollectionNameBox
                || renderable == sortButton
                || renderable == undoButton
                || renderable == redoButton
                || renderable == collectionsButton
                || renderable == settingsButton
                || renderable == screenToggleButton
                || renderable == vanillaButton
                || renderable == openContainerList
                || renderable == carriedList;
    }

    private boolean isOwnedControlChild(GuiEventListener child) {
        return child == searchBox
                || child == inlineDesiredCountBox
                || child == inlineLoadoutRenameBox
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
        return child == openContainerList || child == carriedList;
    }

    private boolean isExternalChild(GuiEventListener child) {
        return !isOwnedControlChild(child) && !isOwnedListChild(child);
    }

    private boolean isTomsStorageWorkspace() {
        return layout.primaryStorageSession() != null
                && "toms_storage_terminal".equals(layout.primaryStorageSession().providerId());
    }

    private boolean applyTomsStorageClientPacket(CompoundTag tag) throws ReflectiveOperationException {
        Field syncField = findField(menu.getClass(), "sync");
        Field playerInventoryField = findField(menu.getClass(), "pinv");
        if (syncField == null || playerInventoryField == null) {
            return false;
        }

        Object sync = syncField.get(menu);
        Object playerInventoryValue = playerInventoryField.get(menu);
        if (sync == null || !(playerInventoryValue instanceof Inventory playerInventory)) {
            return false;
        }

        Method receiveUpdate = findMethod(sync.getClass(), "receiveUpdate", RegistryAccess.class, CompoundTag.class);
        Method getAsList = findMethod(sync.getClass(), "getAsList");
        Method getAmount = findMethodByArity(sync.getClass(), "getAmount", 1);
        Field itemListField = findField(menu.getClass(), "itemList");
        Field itemListClientField = findField(menu.getClass(), "itemListClient");
        Field itemsLoadedField = findField(menu.getClass(), "itemsLoaded");
        Field noSortField = findField(menu.getClass(), "noSort");
        Field searchField = findField(menu.getClass(), "search");
        if (receiveUpdate == null || getAsList == null || itemListField == null || itemListClientField == null) {
            return false;
        }

        boolean updated = Boolean.TRUE.equals(receiveUpdate.invoke(sync, playerInventory.player.registryAccess(), tag));
        if (updated) {
            Object syncedItemsValue = getAsList.invoke(sync);
            List<?> syncedItems = syncedItemsValue instanceof List<?> list ? list : List.of();
            List<Object> copiedSyncedItems = new ArrayList<>(syncedItems.size());
            for (Object syncedItem : syncedItems) {
                copiedSyncedItems.add(syncedItem);
            }
            itemListField.set(menu, copiedSyncedItems);

            boolean noSort = noSortField != null && noSortField.getBoolean(menu);
            if (noSort && getAmount != null) {
                Object itemListClientValue = itemListClientField.get(menu);
                if (itemListClientValue instanceof List<?> clientItems) {
                    Method setCount = null;
                    for (Object storedItem : clientItems) {
                        if (storedItem == null) {
                            continue;
                        }
                        if (setCount == null) {
                            setCount = findMethod(storedItem.getClass(), "setCount", long.class);
                            if (setCount == null) {
                                break;
                            }
                        }
                        Object amount = getAmount.invoke(sync, storedItem);
                        long quantity = amount instanceof Number number ? number.longValue() : 0L;
                        setCount.invoke(storedItem, quantity);
                    }
                }
            } else {
                itemListClientField.set(menu, new ArrayList<>(copiedSyncedItems));
            }

            if (itemsLoadedField != null) {
                itemsLoadedField.setBoolean(menu, true);
            }
            playerInventory.setChanged();
        }

        if (searchField != null && tag.contains("s")) {
            searchField.set(menu, tag.getString("s"));
        }
        return updated || tag.contains("s");
    }

    private static Method findMethod(Class<?> type, String name, Class<?>... parameterTypes) {
        Class<?> current = type;
        while (current != null) {
            try {
                Method method = current.getDeclaredMethod(name, parameterTypes);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    private static Method findMethodByArity(Class<?> type, String name, int parameterCount) {
        Class<?> current = type;
        while (current != null) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getName().equals(name) && method.getParameterCount() == parameterCount) {
                    method.setAccessible(true);
                    return method;
                }
            }
            current = current.getSuperclass();
        }
        return null;
    }

    private static Field findField(Class<?> type, String name) {
        Class<?> current = type;
        while (current != null) {
            try {
                Field field = current.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    private static String describeTagKeys(CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            return "[]";
        }
        StringJoiner joiner = new StringJoiner(", ", "[", "]");
        for (String key : tag.getAllKeys()) {
            joiner.add(key);
        }
        return joiner.toString();
    }

    public record EmiAggregateStackView(ItemStack stack, long quantity) {
        public EmiAggregateStackView {
            stack = stack.copy();
            stack.setCount(Math.max(1, Math.min(stack.getMaxStackSize(), stack.getCount())));
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
    }

    private void renderPanels(GuiGraphics guiGraphics) {
        guiGraphics.fill(panelLeft(), panelTop(), panelVisualRight(), panelBottom(), 0xD0101010);
        guiGraphics.fill(panelLeft() + 1, panelTop() + 1, panelVisualRight() - 1, panelBottom() - 1, 0xE0161616);
        int chromeBottom = contentTop() - 10;
        guiGraphics.fill(panelLeft() + 1, panelTop() + 1, panelVisualRight() - 1, chromeBottom, 0xD9121214);
        guiGraphics.fill(panelLeft() + 1, chromeBottom, panelVisualRight() - 1, chromeBottom + 1, 0x7048484A);
        guiGraphics.fill(railX(), contentTop() - 8, railX() + railWidth(), panelBottom() - OUTER_MARGIN, 0xA0101010);
        if (!singlePaneMode()) {
            guiGraphics.fill(openPaneX(), contentTop() - 8, openPaneX() + paneWidth(), panelBottom() - OUTER_MARGIN, 0xA0181818);
            guiGraphics.fill(openPaneX() + paneWidth() + (PANE_GAP / 2), contentTop() - 8, openPaneX() + paneWidth() + (PANE_GAP / 2) + 1, panelBottom() - OUTER_MARGIN, 0x402E3238);
        }
        guiGraphics.fill(carriedPaneX(), contentTop() - 8, carriedPaneX() + paneWidth(), panelBottom() - OUTER_MARGIN, 0xA0181818);
    }

    private void renderHeader(GuiGraphics guiGraphics) {
        guiGraphics.drawString(font, title, centerPaneX(), headerY(), 0xFFFFFF, false);
        String summary = Component.translatable("slot.screen.debug.summary", inventoryData.entries().size()).getString();
        drawScaledText(guiGraphics, summary, contentRight() - scaledTextWidth(summary, 0.72F), headerY() + 1, 0xA8B0B8, 0.72F);
        renderActionFeedback(guiGraphics);
    }

    private void renderTrashSlot(GuiGraphics guiGraphics, TrashSlotLayout layout) {
        SlotTrashWarningState warningState = trashWarningState();
        boolean hovered = layout.contains(lastMouseX, lastMouseY);
        int frameColor;
        int innerColor;
        if (warningState.pausedForExternalStorage()) {
            frameColor = hovered ? 0xC09E7A32 : 0xA0745826;
            innerColor = hovered ? 0xD02A1E10 : 0xB0201810;
        } else if (warningState.active()) {
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

    private int capacityFillColor(InventoryCapacityIndicator.StorageFillStats stats) {
        float ratio = stats.fillRatio();
        if (ratio >= 0.9F) {
            return 0xFFC44949;
        }
        if (ratio >= 0.7F) {
            return 0xFFD08A2E;
        }
        return 0xFF4E7A34;
    }

    private int capacityTextColor(InventoryCapacityIndicator.StorageFillStats stats) {
        float ratio = stats.fillRatio();
        if (ratio >= 0.9F) {
            return 0xF0B0B0;
        }
        if (ratio >= 0.7F) {
            return 0xE0C090;
        }
        return 0xB8C8B0;
    }

    private void renderPaneHeader(GuiGraphics guiGraphics, int x, InventoryPane pane) {
        int count = paneVisibleCounts.getOrDefault(pane, 0);
        int headerY = contentTop();
        int headerBottom = paneListTop() - 2;
        guiGraphics.fill(x, headerY - 2, x + paneWidth(), headerBottom, pane == InventoryPane.CARRIED ? 0x6420262A : 0x5C1D1F23);
        guiGraphics.fill(x, headerBottom, x + paneWidth(), headerBottom + 1, 0x503C4046);
        InlineButton menuButton = paneHeaderMenuButton(x, pane);
        if (menuButton.enabled()) {
            drawInlineButton(
                    guiGraphics,
                    menuButton.x(),
                    menuButton.y(),
                    menuButton.width(),
                    menuButton.height(),
                    menuButton.label(),
                    menuButton.enabled(),
                    menuButton.contains(lastMouseX, lastMouseY)
            );
        }

        InventoryCapacityIndicator.StorageFillStats stats = pane == InventoryPane.CARRIED ? carriedCapacity : externalCapacity;
        int capacityLeft = x + paneWidth() - 4;
        if (menuButton.enabled()) {
            capacityLeft = menuButton.x() - 6;
        }
        if (stats.available()) {
            capacityLeft = renderPaneCapacityIndicator(guiGraphics, x, headerY, capacityLeft, stats);
        }

        String label = Component.translatable("slot.screen.container.pane.summary", layout.paneTitle(pane), count).getString();
        int labelX = x + 6;
        int labelWidth = Math.max(24, capacityLeft - labelX - 8);
        String trimmedLabel = font.plainSubstrByWidth(label, labelWidth);
        guiGraphics.drawString(font, trimmedLabel, labelX, headerY, 0xD8D8D8, false);
        if (pane == InventoryPane.CARRIED) {
            renderCarriedTrashWarning(guiGraphics, x, headerY);
        }
    }

    private void renderCarriedTrashWarning(GuiGraphics guiGraphics, int paneX, int headerY) {
        SlotTrashWarningState warningState = trashWarningState();
        if (!warningState.active()) {
            return;
        }

        float scale = 0.65F;
        String text = warningState.pausedForExternalStorage()
                ? Component.translatable("slot.screen.inventory.trash.auto_void_paused_short", warningState.nextStack().getHoverName()).getString()
                : Component.translatable("slot.screen.inventory.trash.auto_void_warning_short", warningState.nextStack().getHoverName()).getString();
        int maxWidth = Math.max(24, paneWidth() - 12);
        text = font.plainSubstrByWidth(text, Math.max(24, Math.round(maxWidth / scale)));
        drawScaledText(
                guiGraphics,
                text,
                paneX + 6,
                headerY + 8,
                warningState.pausedForExternalStorage() ? 0xDCC37A : 0xE0A080,
                scale
        );
    }

    private InlineButton paneHeaderMenuButton(int x, InventoryPane pane) {
        List<ActionMenuItem> actions = buildPaneActions(pane);
        return new InlineButton(
                x + paneWidth() - COLLECTION_MENU_BUTTON_WIDTH,
                contentTop() + 1,
                COLLECTION_MENU_BUTTON_WIDTH,
                COLLECTION_BUTTON_HEIGHT,
                "...",
                !actions.isEmpty()
        );
    }

    private int renderPaneCapacityIndicator(
            GuiGraphics guiGraphics,
            int paneX,
            int headerY,
            int right,
            InventoryCapacityIndicator.StorageFillStats stats
    ) {
        String countText = stats.occupiedSlots() + "/" + stats.totalSlots();
        int countWidth = scaledTextWidth(countText, ROW_TEXT_SCALE);
        int barWidth = 52;
        int barHeight = 6;
        int barGap = 4;
        int countX = right - countWidth;
        int barRight = countX - barGap;
        int barLeft = barRight - barWidth;
        int minLeft = paneX + 72;
        if (barLeft < minLeft) {
            return right;
        }

        int barTop = headerY + 2;
        int barBottom = barTop + barHeight;
        guiGraphics.fill(barLeft, barTop, barRight, barBottom, 0xA0242424);
        guiGraphics.fill(barLeft + 1, barTop + 1, barRight - 1, barBottom - 1, 0xC0101010);
        int fillWidth = Math.round((barWidth - 2) * stats.fillRatio());
        if (fillWidth > 0) {
            guiGraphics.fill(barLeft + 1, barTop + 1, barLeft + 1 + fillWidth, barBottom - 1, capacityFillColor(stats));
        }
        drawScaledText(guiGraphics, countText, countX, headerY + 1, capacityTextColor(stats), ROW_TEXT_SCALE);
        return barLeft;
    }

    private void renderRail(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        clampRailScroll();
        int x = railX() + 4;
        int y = contentTop() - railScrollState.offset();
        int viewportTop = contentTop();
        int viewportBottom = panelBottom() - OUTER_MARGIN;
        String highlighted = currentHighlightedSection();

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
            boolean empty = !ALL_TARGET_ID.equals(target.id()) && !isRailTargetAccessible(target.id(), InventoryPane.CARRIED);
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

    private void renderPaneEmptyState(GuiGraphics guiGraphics, InventoryProjectionSelectionList<InventoryScreenRow> paneList, int paneX) {
        if (paneList == null || paneVisibleCounts.getOrDefault(paneForList(paneList), 0) > 0) {
            return;
        }

        int x = paneX + 12;
        int y = paneListTop() + 12;
        drawWrapped(guiGraphics, Component.translatable("slot.screen.debug.empty_state"), x, y, paneWidth() - 24, 0xA0A0A0);
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
            int background = hoveredSlot == slotIndex ? 0x90507038 : 0x60303030;
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
        if (openContainerList == null || carriedList == null) {
            return;
        }

        String query = searchBox == null ? "" : searchBox.getValue().trim().toLowerCase(Locale.ROOT);
        inventoryProjection = buildInventoryProjection(query);
        InventoryProjection.PaneProjection openPane = inventoryProjection.pane(InventoryPane.OPEN_CONTAINER);
        InventoryProjection.PaneProjection carriedPane = inventoryProjection.pane(InventoryPane.CARRIED);

        paneVisibleEntriesBySection = Map.of(
                InventoryPane.OPEN_CONTAINER, openPane.visibleEntriesBySection(),
                InventoryPane.CARRIED, carriedPane.visibleEntriesBySection()
        );
        visibleSectionCounts = inventoryProjection.combinedSectionCounts();
        paneVisibleCounts = Map.of(
                InventoryPane.OPEN_CONTAINER, openPane.visibleEntryCount(),
                InventoryPane.CARRIED, carriedPane.visibleEntryCount()
        );
        paneSectionCounts = Map.of(
                InventoryPane.OPEN_CONTAINER, openPane.sectionCounts(),
                InventoryPane.CARRIED, carriedPane.sectionCounts()
        );

        openContainerList.setRows(materializePaneRows(openPane.rows()));
        carriedList.setRows(materializePaneRows(carriedPane.rows()));
        clampRailScroll();
        restoreSelection();
    }

    private InventoryProjection buildInventoryProjection(String query) {
        return InventoryProjectionBuilder.buildWorkspace(
                new InventoryProjectionBuilder.WorkspaceInput(
                        inventoryData.sections(),
                        inventoryData.entries(),
                        recentMatchingEntries(query, InventoryPane.CARRIED),
                        query,
                        collectionStore,
                        sortMode.comparator(),
                        this::localCount,
                        this::projectionSectionOptions
                )
        );
    }

    private InventoryProjectionBuilder.PaneSectionOptions projectionSectionOptions(
            InventoryViewData.Section section,
            InventoryPane pane
    ) {
        boolean expanded = !section.isCollection() || !isCollectionCollapsed(section.collectionId());
        boolean includeLoadoutRows = pane == InventoryPane.CARRIED
                && section.isCollection()
                && shouldShowCollectionLoadouts(section.collectionId(), expanded);
        return new InventoryProjectionBuilder.PaneSectionOptions(
                isSectionVisibleInPane(section, pane),
                pane == InventoryPane.CARRIED && (shouldRetainCollectionSection(section) || section.isRecent()),
                expanded,
                includeLoadoutRows,
                includeLoadoutRows && !singlePaneMode()
        );
    }

    private List<InventoryScreenRow> materializePaneRows(List<InventoryProjection.RowProjection> rows) {
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
                                InventoryProjectionSelectionList<InventoryScreenRow> list = listFor(sectionRow.pane());
                                return list.rowTop(list.children().indexOf(row));
                            }

                            @Override
                            public int rowLeft() {
                                return listFor(sectionRow.pane()).getRowLeft();
                            }

                            @Override
                            public int rowWidth() {
                                return listFor(sectionRow.pane()).getRowWidth();
                            }
                        },
                        (x, y, width) -> sectionHeaderState(sectionRow.section(), sectionRow.pane(), x, y, width),
                        (target, headerState) -> handleSectionRowClick(sectionRow.section(), sectionRow.pane(), target, headerState)
                ));
                continue;
            }
            if (row instanceof InventoryProjection.LoadoutRowProjection loadoutRow) {
                materializedRows.add(new CollectionLoadoutEntry(
                        loadoutRow.rowId(),
                        loadoutRow.section(),
                        loadoutRow.pane()
                ));
                continue;
            }
            if (row instanceof InventoryProjection.LoadoutPreviewRowProjection loadoutPreviewRow) {
                materializedRows.add(new CollectionLoadoutPreviewEntry(
                        loadoutPreviewRow.rowId(),
                        loadoutPreviewRow.section(),
                        loadoutPreviewRow.pane()
                ));
                continue;
            }
            if (row instanceof InventoryProjection.ItemRowProjection itemRow) {
                materializedRows.add(new PaneItemRowEntry(
                        itemRow.rowId(),
                        itemRow.entry(),
                        itemRow.pane(),
                        itemRow.section().collectionId(),
                        itemRow.section().isRecent()
                ));
            }
        }
        return List.copyOf(materializedRows);
    }

    private InventorySectionHeaderSupport.SectionHeaderState sectionHeaderState(
            InventoryViewData.Section section,
            InventoryPane pane,
            int x,
            int y,
            int width
    ) {
        boolean carriedSection = pane == InventoryPane.CARRIED;
        boolean collectionSection = carriedSection && section.isCollection();
        return sectionHeaderSupport.buildState(
                section,
                x,
                y,
                width,
                new InventorySectionHeaderSupport.SectionHeaderOptions(
                        collectionSection,
                        carriedSection,
                        !buildSectionActions(section, pane, x, y + COLLECTION_BUTTON_HEIGHT).isEmpty(),
                        collectionSection && !singlePaneMode(),
                        collectionNeedsRestock(section.collectionId()),
                        x + 48
                )
        );
    }

    private boolean handleSectionRowClick(
            InventoryViewData.Section section,
            InventoryPane pane,
            InventorySectionHeaderSupport.SectionHeaderClickTarget target,
            InventorySectionHeaderSupport.SectionHeaderState headerState
    ) {
        return switch (target) {
            case TOGGLE -> {
                toggleCollectionCollapsed(section.collectionId());
                selectedPane = InventoryPane.CARRIED;
                yield true;
            }
            case PIN -> {
                togglePinnedLoadoutsWhenCollapsed(section.collectionId());
                selectedPane = InventoryPane.CARRIED;
                yield true;
            }
            case MENU -> {
                InlineButton menuButton = headerState.menuButton();
                openSectionActionMenu(section, pane, menuButton.x(), menuButton.y() + menuButton.height());
                selectedPane = pane;
                yield true;
            }
            case RESTOCK -> {
                restockCollection(section.collectionId());
                selectedPane = InventoryPane.CARRIED;
                yield true;
            }
            case NAVIGATE -> {
                listFor(pane).scrollToTarget(section.id());
                selectedPane = pane;
                yield true;
            }
        };
    }

    private boolean isSectionVisibleInPane(InventoryViewData.Section section, InventoryPane pane) {
        if (section.isRecent()) {
            return pane == InventoryPane.CARRIED;
        }
        return pane != InventoryPane.OPEN_CONTAINER || !isInternalOnlyCollectionSection(section);
    }

    private boolean isInternalOnlyCollectionSection(InventoryViewData.Section section) {
        return section.isCollection() && CollectionStore.JUNK_ID.equals(section.collectionId());
    }

    private List<InventoryViewData.EntryView> recentMatchingEntries(String query, InventoryPane pane) {
        if (pane != InventoryPane.CARRIED) {
            return List.of();
        }
        return recentMatchingEntries(query, inventoryData.entries(), entry -> localCount(entry, pane) > 0);
    }

    @Override
    protected Set<String> defaultRecentCarriedSourceIds() {
        return layout.sourceIdsForPane(InventoryPane.CARRIED);
    }

    @Override
    protected int ownedCountForCollectionStock(ItemIdentity identity) {
        return carriedCountFor(identity);
    }

    private void restoreSelection() {
        List<InventorySelectionRestoreSupport.PaneRows<InventoryScreenRow, InventoryPane>> paneOrder;
        if (!singlePaneMode() && selectedPane == InventoryPane.OPEN_CONTAINER) {
            paneOrder = List.of(
                    new InventorySelectionRestoreSupport.PaneRows<>(InventoryPane.OPEN_CONTAINER, openContainerList.children()),
                    new InventorySelectionRestoreSupport.PaneRows<>(InventoryPane.CARRIED, carriedList.children())
            );
        } else {
            paneOrder = List.of(
                    new InventorySelectionRestoreSupport.PaneRows<>(InventoryPane.CARRIED, carriedList.children()),
                    new InventorySelectionRestoreSupport.PaneRows<>(InventoryPane.OPEN_CONTAINER, openContainerList.children())
            );
        }
        InventorySelectionRestoreSupport.Selection<InventoryScreenRow, InventoryPane> selection =
                InventorySelectionRestoreSupport.findInPaneOrderByValue(
                        selectedRowId,
                        row -> {
                            PaneItemRowEntry itemRow = paneItemRow(row);
                            return itemRow == null ? null : itemRow.rowId();
                        },
                        paneOrder
                );
        if (paneItemRow(selection.row()) == null) {
            selection = InventorySelectionRestoreSupport.findInPaneOrder(
                        selectedIdentity,
                        row -> {
                            PaneItemRowEntry itemRow = paneItemRow(row);
                            return itemRow == null ? null : itemRow.entry.itemEntry().identity();
                        },
                        paneOrder
                );
        }
        InventoryScreenRow selectedRow = selection.row();
        PaneListEntry selectedPaneRow = paneListEntry(selectedRow);

        openContainerList.setSelected(selectedPaneRow != null && selectedPaneRow.pane == InventoryPane.OPEN_CONTAINER ? selectedRow : null);
        carriedList.setSelected(selectedPaneRow != null && selectedPaneRow.pane == InventoryPane.CARRIED ? selectedRow : null);

        PaneItemRowEntry selectedItemRow = paneItemRow(selectedRow);
        if (selectedItemRow != null) {
            setSelectedEntry(selectedItemRow.entry, selectedItemRow.pane, selectedItemRow.rowId());
        } else {
            selectedEntry = null;
            selectedIdentity = null;
            selectedRowId = null;
        }
    }

    private void setSelectedEntry(InventoryViewData.EntryView entry, InventoryPane pane) {
        setSelectedEntry(entry, pane, null);
    }

    private void setSelectedEntry(InventoryViewData.EntryView entry, InventoryPane pane, String rowId) {
        selectedEntry = entry;
        selectedIdentity = entry == null ? null : entry.itemEntry().identity();
        selectedRowId = rowId;
        selectedPane = pane;
    }

    private void toggleFavorite(InventoryViewData.EntryView entry, InventoryPane pane) {
        if (entry == null) {
            return;
        }

        selectedPane = pane;
        super.toggleFavorite(entry);
    }

    private void toggleCollectionMembership(InventoryViewData.EntryView entry, String collectionId, InventoryPane pane) {
        if (entry == null || collectionId == null || collectionId.isBlank()) {
            return;
        }

        selectedPane = pane;
        super.toggleCollectionMembership(entry, collectionId);
    }

    @Override
    protected void toggleSelectedItemMembership(String collectionId) {
        if (selectedEntry == null || collectionId == null || collectionId.isBlank()) {
            return;
        }

        toggleCollectionMembership(selectedEntry, collectionId, InventoryPane.CARRIED);
    }

    @Override
    protected void applyUndoHistoryResult(SlotUndoHistory.ApplyResult result, boolean undo) {
        applyUndoHistoryResultCommon(result, undo, () -> queueTransferRefresh(InventoryPane.CARRIED));
    }

    private void applyConfirmedActionOutcome() {
        applyConfirmedActionOutcomeCommon();
    }

    private void runMoveOne(InventoryViewData.EntryView entry, InventoryPane pane) {
        runMove(entry, pane, InventoryTransferActionSupport.EntryMoveMode.ONE);
    }

    private void runMoveStack(InventoryViewData.EntryView entry, InventoryPane pane) {
        runMove(entry, pane, InventoryTransferActionSupport.EntryMoveMode.STACK);
    }

    private void runMoveAllType(InventoryViewData.EntryView entry, InventoryPane pane) {
        runMove(entry, pane, InventoryTransferActionSupport.EntryMoveMode.ALL_OF_TYPE);
    }

    private void runMove(
            InventoryViewData.EntryView entry,
            InventoryPane pane,
            InventoryTransferActionSupport.EntryMoveMode mode
    ) {
        if (entry == null || pane == null) {
            return;
        }
        if (shouldDeferExternalTransfer(pane)) {
            return;
        }

        selectedPane = pane;
        SlotActionResult result = InventoryTransferActionSupport.moveWorkspaceEntry(
                actionExecutor,
                currentPlayer(),
                menu,
                entry,
                pane,
                mode
        );
        handleActionResultCommon(
                result,
                actionPlan(
                        false,
                        false,
                        false,
                        () -> queueTransferRefresh(pane),
                        null
                )
        );
    }

    private void queueTransferRefresh(InventoryPane pane) {
        selectedPane = pane;
        rowActionMenu = null;
        schedulePostActionRefresh();
        if (singlePaneMode()) {
            refreshInventoryData();
        }
    }

    protected void refreshInventoryData() {
        long refreshStart = System.nanoTime();
        cachedPrimarySnapshots = readPrimarySnapshots();
        long afterSnapshots = System.nanoTime();
        cachedPrimarySnapshotHash = hashPrimarySnapshots(cachedPrimarySnapshots);
        inventoryData = dataBuilder.build(screenContext, collectionStore, cachedPrimarySnapshots);
        long afterData = System.nanoTime();
        cachedEmiAggregateStacks = buildEmiAggregateStacks(inventoryData);
        long afterEmi = System.nanoTime();
        SlotUndoHistory.bindContext(historyContextKey());
        long afterHistory = System.nanoTime();
        carriedCapacity = InventoryCapacityIndicator.measureCarried(currentPlayer(), screenContext, cachedPrimarySnapshots);
        externalCapacity = singlePaneMode()
                ? InventoryCapacityIndicator.StorageFillStats.EMPTY
                : InventoryCapacityIndicator.measure(currentPlayer(), screenContext, screenContext.externalSourceIds(), cachedPrimarySnapshots);
        long afterCapacity = System.nanoTime();
        lastObservedMenuStateHash = computeObservedMenuStateHash();
        long afterHash = System.nanoTime();
        railTargets = buildRailTargets();
        long afterRail = System.nanoTime();
        normalizeInlineCollectionState();
        rowActionMenu = null;
        rebuildVisibleEntries();
        long afterVisible = System.nanoTime();
        updateDynamicButtons();
        logTomsPerf(
                "Tom's refresh timings: totalMs={} snapshotsMs={} dataMs={} emiMs={} historyMs={} capacityMs={} hashMs={} railMs={} visibleMs={} entries={} sections={} primarySnapshots={}",
                refreshStart,
                8.0,
                millisBetween(refreshStart, afterVisible),
                millisBetween(refreshStart, afterSnapshots),
                millisBetween(afterSnapshots, afterData),
                millisBetween(afterData, afterEmi),
                millisBetween(afterEmi, afterHistory),
                millisBetween(afterHistory, afterCapacity),
                millisBetween(afterCapacity, afterHash),
                millisBetween(afterHash, afterRail),
                millisBetween(afterRail, afterVisible),
                inventoryData == null ? 0 : inventoryData.entries().size(),
                inventoryData == null ? 0 : inventoryData.sections().size(),
                cachedPrimarySnapshots.size()
        );
    }

    private boolean shouldAutoVoidJunk() {
        return inventoryData != null
                && carriedCapacity.available()
                && carriedCapacity.freeSlots() <= 1
                && !autoVoidPausedForExternalStorage()
                && !postActionRefreshDelay.active()
                && !hasVisibleCarriedStack()
                && !hasHotbarDrag()
                && !transferDragActive
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

        ItemIdentity identity = nextJunkIdentityToVoid();
        if (identity == null || !CursorTransferRequester.requestVoidMatchingCarried(menu.containerId, identity)) {
            return false;
        }

        ItemStack previewStack = ItemBehaviorPolicy.approximateDisplayStack(identity);
        if (!previewStack.isEmpty()) {
            previewStack.setCount(1);
            SlotTrashBuffer.rememberAutoVoid(previewStack);
        }
        SlotDebugLog.log(
                "Requested junk auto-void from workspace screen: identity={} freeSlots={}",
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

    private void beginNewCollectionPrompt(InventoryViewData.EntryView entry, InventoryPane pane) {
        if (entry == null || entry.itemEntry().identity() == null || newCollectionNameBox == null) {
            return;
        }

        setSelectedEntry(entry, pane);
        selectedPane = pane;
        rowActionMenu = null;
        newCollectionPromptState.begin(entry.itemEntry().identity());
        newCollectionNameBox.setValue(entry.displayName());
        newCollectionNameBox.moveCursorToEnd(false);
        newCollectionNameBox.setHighlightPos(0);
        newCollectionNameBox.setVisible(true);
        newCollectionNameBox.setFocused(true);
        setFocused(newCollectionNameBox);
    }

    @Override
    public void slotRefreshContents() {
        postActionRefreshDelay.clear();
        refreshInventoryData();
    }

    private void beginTransferDrag(InventoryViewData.EntryView entry, InventoryPane pane) {
        transferDragActive = true;
        transferDraggedEntries.clear();
        transferDraggedEntries.add(new DragTransferKey(pane, entry.itemEntry().identity()));
    }

    private void continueTransferDrag(double mouseX, double mouseY) {
        if (!Screen.hasShiftDown()) {
            clearTransferDrag();
            return;
        }

        PaneItemRowEntry hoveredRow = hoveredItemRow(mouseX, mouseY);
        if (hoveredRow == null || hoveredRow.isActionHit(mouseX)) {
            return;
        }

        DragTransferKey key = new DragTransferKey(hoveredRow.pane, hoveredRow.entry.itemEntry().identity());
        if (!transferDraggedEntries.add(key)) {
            return;
        }

        runMoveAllType(hoveredRow.entry, hoveredRow.pane);
    }

    private void clearTransferDrag() {
        transferDragActive = false;
        transferDraggedEntries.clear();
    }

    private boolean shouldDeferExternalTransfer(InventoryPane pane) {
        return postActionRefreshDelay.active()
                && pane == InventoryPane.OPEN_CONTAINER
                && !layout.primaryStorageIsCarried();
    }

    @Override
    protected void schedulePostActionRefresh() {
        postActionRefreshDelay.schedule(isTomsStorageWorkspace() ? 4 : 8);
    }

    @Override
    protected void clearPendingPostActionRefresh() {
        postActionRefreshDelay.clear();
    }

    private ItemStack previewStack(ItemIdentity identity) {
        return previewStack(identity, inventoryData.entries());
    }

    private void logTomsPerf(String pattern, long startNanos, double thresholdMs, Object... args) {
        if (!isTomsStorageWorkspace() || !SlotDebugLog.enabled()) {
            return;
        }
        if (millisSince(startNanos) < thresholdMs) {
            return;
        }
        SlotDebugLog.log(pattern, args);
    }

    private static double millisSince(long startNanos) {
        return millisBetween(startNanos, System.nanoTime());
    }

    private static double millisBetween(long startNanos, long endNanos) {
        return Math.round(((endNanos - startNanos) / 1_000_000.0D) * 10.0D) / 10.0D;
    }

    private PaneItemRowEntry hoveredItemRow(double mouseX, double mouseY) {
        if (!singlePaneMode() && openContainerList != null) {
            PaneItemRowEntry hovered = paneItemRowAtPosition(openContainerList, mouseX, mouseY);
            if (hovered != null) {
                return hovered;
            }
        }
        if (carriedList != null) {
            return paneItemRowAtPosition(carriedList, mouseX, mouseY);
        }
        return null;
    }

    private PaneItemRowEntry hoveredHotbarCandidateRow() {
        PaneItemRowEntry hoveredRow = hoveredItemRow(lastMouseX, lastMouseY);
        if (hoveredRow == null) {
            return null;
        }
        return hoveredRow;
    }

    private PaneItemRowEntry hotbarAssignmentRow() {
        PaneItemRowEntry hoveredRow = hoveredHotbarCandidateRow();
        if (hoveredRow != null) {
            return hoveredRow;
        }
        if (isSearchInputActive()) {
            return null;
        }
        if (selectedEntry == null) {
            return null;
        }
        return new PaneItemRowEntry("selection_preview", selectedEntry, selectedPane, null, false);
    }

    private boolean shouldSyncSearchWithEmi() {
        return emiPresent && settingsController != null && settingsController.syncSearchWithEmi();
    }

    private InventoryProjectionSelectionList<InventoryScreenRow> hoveredPaneList(double mouseX, double mouseY) {
        if (carriedList != null && carriedList.isMouseOver(mouseX, mouseY)) {
            return carriedList;
        }
        if (!singlePaneMode() && openContainerList != null && openContainerList.isMouseOver(mouseX, mouseY)) {
            return openContainerList;
        }
        return null;
    }

    private boolean isPaneItemRow(InventoryScreenRow row) {
        return PaneItemRowEntry.class.isInstance(row);
    }

    @SuppressWarnings("unchecked")
    private PaneItemRowEntry paneItemRow(InventoryScreenRow row) {
        return PaneItemRowEntry.class.isInstance(row) ? (PaneItemRowEntry) row : null;
    }

    @SuppressWarnings("unchecked")
    private PaneListEntry paneListEntry(InventoryScreenRow row) {
        return PaneListEntry.class.isInstance(row) ? (PaneListEntry) row : null;
    }

    @SuppressWarnings("unchecked")
    private CollectionLoadoutEntry collectionLoadoutEntry(InventoryScreenRow row) {
        return CollectionLoadoutEntry.class.isInstance(row) ? (CollectionLoadoutEntry) row : null;
    }

    private PaneItemRowEntry paneItemRowAtPosition(
            InventoryProjectionSelectionList<InventoryScreenRow> paneList,
            double mouseX,
            double mouseY
    ) {
        InventoryScreenRow row = paneList.entryAtPosition(mouseX, mouseY);
        return paneItemRow(row);
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
        if (!singlePaneMode()) {
            openContainerList.navigateToTarget(resolveVisibleTargetId(target.id(), InventoryPane.OPEN_CONTAINER));
        }
        carriedList.navigateToTarget(resolveVisibleTargetId(target.id(), InventoryPane.CARRIED));
        selectedPane = InventoryPane.CARRIED;
        return true;
    }

    private List<Target> buildRailTargets() {
        return InventoryRailSupport.buildTargets(
                ALL_TARGET_ID,
                Component.translatable("slot.screen.debug.rail.all").getString(),
                inventoryData.sections()
        );
    }

    private String resolveVisibleTargetId(String requestedId, InventoryPane pane) {
        return InventoryRailSupport.resolveVisibleTargetId(
                ALL_TARGET_ID,
                railTargets,
                requestedId,
                targetId -> isRailTargetAccessible(targetId, pane)
        );
    }

    private boolean isRailTargetAccessible(String targetId, InventoryPane pane) {
        if (ALL_TARGET_ID.equals(targetId)) {
            return true;
        }
        Map<String, Integer> sectionCounts = paneSectionCounts.getOrDefault(pane, Map.of());
        if (sectionCounts.containsKey(targetId)) {
            return true;
        }
        if (pane != InventoryPane.CARRIED || !targetId.startsWith("collection/")) {
            return false;
        }
        String collectionId = targetId.substring("collection/".length());
        return collectionHasLoadouts(collectionId) || CollectionStore.JUNK_ID.equals(collectionId);
    }

    private String currentHighlightedSection() {
        if (singlePaneMode()) {
            return carriedList.hasAnyRows() ? carriedList.currentSectionId() : ALL_TARGET_ID;
        }
        if (openContainerList.hasAnyRows() && carriedList.hasAnyRows()) {
            String openCurrent = openContainerList.currentSectionId();
            String carriedCurrent = carriedList.currentSectionId();
            if (Objects.equals(openCurrent, carriedCurrent)) {
                return openCurrent;
            }
        }
        if (selectedPane == InventoryPane.CARRIED && carriedList.hasAnyRows()) {
            return carriedList.currentSectionId();
        }
        if (openContainerList.hasAnyRows()) {
            return openContainerList.currentSectionId();
        }
        if (carriedList.hasAnyRows()) {
            return carriedList.currentSectionId();
        }
        return ALL_TARGET_ID;
    }

    private int localCount(InventoryViewData.EntryView entry, InventoryPane pane) {
        return entry.itemEntry().perSourceCounts().entrySet().stream()
                .filter(sourceEntry -> layout.sourceIdsForPane(pane).contains(sourceEntry.getKey()))
                .mapToInt(Map.Entry::getValue)
                .sum();
    }

    private int compareCount(InventoryViewData.EntryView entry, InventoryPane pane) {
        return entry.itemEntry().perSourceCounts().entrySet().stream()
                .filter(sourceEntry -> layout.compareSourceIdsForPane(pane).contains(sourceEntry.getKey()))
                .mapToInt(Map.Entry::getValue)
                .sum();
    }

    private String carriedSourceSummary(InventoryViewData.EntryView entry, int maxWidth) {
        StringJoiner joiner = new StringJoiner(" | ");
        for (var source : layout.sources()) {
            if (!layout.sourceIdsForPane(InventoryPane.CARRIED).contains(source.id())) {
                continue;
            }

            int count = entry.itemEntry().perSourceCounts().getOrDefault(source.id(), 0);
            if (count <= 0) {
                continue;
            }
            joiner.add(layout.shortSource(source.id()) + " " + count);
        }

        String summary = joiner.toString();
        if (summary.isEmpty()) {
            return "";
        }
        return font.plainSubstrByWidth(summary, maxWidth);
    }

    private String rowSecondaryText(InventoryViewData.EntryView entry, InventoryPane pane, int maxWidth) {
        if (pane == InventoryPane.CARRIED) {
            return carriedSourceSummary(entry, maxWidth);
        }

        int otherCount = compareCount(entry, pane);
        if (otherCount <= 0) {
            return "";
        }
        return font.plainSubstrByWidth(layout.compareHintLabel(pane) + " " + otherCount, maxWidth);
    }

    private void openCollectionsForEntry(InventoryViewData.EntryView entry, InventoryPane pane) {
        setSelectedEntry(entry, pane);
        openCollections();
    }

    private void openInspectionForEntry(InventoryViewData.EntryView entry, InventoryPane pane) {
        if (minecraft == null || entry == null) {
            return;
        }

        setSelectedEntry(entry, pane);
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

    private int railX() {
        return panelLeft() + OUTER_MARGIN;
    }

    protected int centerPaneX() {
        return railX() + railWidth() + COLUMN_GAP;
    }

    @Override
    protected int centerPaneWidth() {
        return contentWidth();
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

    private int contentWidth() {
        return Math.max(240, contentRight() - centerPaneX());
    }

    private int paneAreaWidth() {
        return contentWidth();
    }

    private int paneWidth() {
        if (singlePaneMode()) {
            return paneAreaWidth();
        }
        return Math.max(110, (paneAreaWidth() - PANE_GAP) / 2);
    }

    private int openPaneX() {
        return singlePaneMode() ? centerPaneX() : carriedPaneX() + paneWidth() + PANE_GAP;
    }

    private int carriedPaneX() {
        return centerPaneX();
    }

    private int hotbarY() {
        return panelTop() + HOTBAR_Y;
    }

    private int hotbarStripWidth() {
        return quickAccessSpanWidth(HOTBAR_SLOT_SIZE, HOTBAR_SLOT_GAP, HOTBAR_OFFHAND_GAP);
    }

    private int hotbarStripX() {
        return carriedPaneX() + Math.max(0, (paneWidth() - hotbarStripWidth()) / 2);
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

    private int paneListTop() {
        return contentTop() + paneHeaderHeight();
    }

    private int paneHeaderHeight() {
        return PANE_HEADER_BASE_HEIGHT + (trashWarningState().active() ? PANE_HEADER_WARNING_HEIGHT : 0);
    }

    private int paneListHeight() {
        return Math.max(100, panelBottom() - OUTER_MARGIN - paneListTop());
    }

    private int openPaneListHeight() {
        return Math.max(100, paneListHeight() - dockedToolPanelReservedHeight());
    }

    private int carriedPaneListHeight() {
        return paneListHeight();
    }

    private int dockedToolPanelY() {
        return paneListTop() + openPaneListHeight() + DOCKED_TOOL_PANEL_GAP;
    }

    private int dockedToolPanelReservedHeight() {
        if (dockedToolPanel == null || singlePaneMode()) {
            return 0;
        }
        return DOCKED_TOOL_PANEL_GAP + dockedToolPanel.preferredHeight();
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
        return Math.max(panelLeft() + 420, panelRight() - EMI_CONTENT_GAP);
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
        int availableWidth = Math.max(420, width - reservedLeftMargin() - reservedRightMargin());
        int maxWidth;
        if (emiPresent) {
            maxWidth = singlePaneMode() ? EMI_MAX_SINGLE_PANE_PANEL_WIDTH : EMI_MAX_PANEL_WIDTH;
        } else {
            maxWidth = singlePaneMode() ? MAX_SINGLE_PANE_PANEL_WIDTH : MAX_PANEL_WIDTH;
        }
        return Math.min(maxWidth, availableWidth);
    }

    private int desiredPanelHeight() {
        return Math.min(MAX_PANEL_HEIGHT, Math.max(220, height - 24));
    }

    private int computeObservedMenuStateHash() {
        int hash = InventoryMenuStateHash.sourceSlots(menu, menuSlot -> layout.sourceIdForMenuSlot(menuSlot) != null, 1);
        if (!layout.primaryStorageMenuBacked()) {
            hash = InventoryMenuStateHash.appendInt(hash, cachedPrimarySnapshotHash);
        }
        return hash;
    }

    private List<ExternalStorageStackSnapshot> readPrimarySnapshots() {
        if (layout.primaryStorageMenuBacked()) {
            return List.of();
        }
        return List.copyOf(layout.primaryStorageSession().readClientPrimarySnapshots(menu));
    }

    private List<EmiAggregateStackView> buildEmiAggregateStacks(InventoryViewData data) {
        if (data == null) {
            return List.of();
        }

        List<EmiAggregateStackView> stacks = new ArrayList<>();
        for (InventoryViewData.EntryView entry : data.entries()) {
            long quantity = 0L;
            for (var sourceEntry : entry.itemEntry().perSourceCounts().entrySet()) {
                if (emiAggregateSourceIds.contains(sourceEntry.getKey())) {
                    quantity += sourceEntry.getValue();
                }
            }
            if (quantity > 0L && !entry.displayStack().isEmpty()) {
                stacks.add(new EmiAggregateStackView(entry.displayStack(), quantity));
            }
        }
        return List.copyOf(stacks);
    }

    private static int hashPrimarySnapshots(List<ExternalStorageStackSnapshot> snapshots) {
        int hash = 1;
        for (ExternalStorageStackSnapshot snapshot : snapshots) {
            ItemStack stack = snapshot.stack();
            hash = 31 * hash + snapshot.handle();
            hash = 31 * hash + snapshot.count();
            hash = InventoryMenuStateHash.appendStack(hash, stack);
        }
        return hash;
    }

    private int headerY() {
        return panelTop() + HEADER_Y;
    }

    protected int headerStatusY() {
        return headerY() + 11;
    }

    @Override
    protected void onCollectionCreated(String collectionId) {
        selectedPane = InventoryPane.CARRIED;
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

    private void beginHotbarDrag(InventoryViewData.EntryView entry, InventoryPane pane) {
        if (!canAssignToQuickAccess(entry, pane)) {
            return;
        }
        hotbarInteractionState.beginDrag(entry.itemEntry().identity(), entry.displayStack(), pane);
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
                !menu.getCarried().isEmpty(),
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

        int hotbarSlot = hotbarSlotAt(mouseX, mouseY);
        if (hotbarSlot < 0 || drag.identity() == null) {
            return false;
        }

        applyQuickAccessMutation(() -> hotbarLoadoutController.assignToQuickAccessMutation(
                drag.identity(),
                hotbarSlot,
                layout.sourceIdsForPane(drag.pane())
        ));
        return true;
    }

    private boolean completeHotbarCursorRelease(double mouseX, double mouseY) {
        InventoryHotbarInteractionState.CursorInteraction interaction = hotbarInteractionState.consumeCursorInteraction();
        if (!interaction.active()) {
            return false;
        }

        if (menu.getCarried().isEmpty()) {
            return false;
        }

        int hotbarSlot = hotbarSlotAt(mouseX, mouseY);
        if (hotbarSlot >= 0) {
            if (hotbarSlot != interaction.originSlot()) {
                applyQuickAccessMutation(() -> hotbarLoadoutController.clickQuickAccessSlotMutation(hotbarSlot, interaction.button()));
            }
            return true;
        }

        if (hoveredPaneList(mouseX, mouseY) != null) {
            return handleCursorPaneDrop(mouseX, mouseY, interaction.button());
        }

        return false;
    }

    private boolean canAssignToQuickAccess(InventoryViewData.EntryView entry, InventoryPane pane) {
        return InventoryRowActionPlanner.canAssignToQuickAccess(entry, hotbarLoadoutController, layout.sourceIdsForPane(pane));
    }

    private boolean isEquippedOnly(InventoryViewData.EntryView entry, InventoryPane pane) {
        return InventoryRowActionPlanner.isEquippedOnly(entry, pane == InventoryPane.CARRIED, hotbarLoadoutController, layout.sourceIdsForPane(pane));
    }

    private boolean useItemFromInventory(InventoryViewData.EntryView entry, InventoryPane pane) {
        if (pane != InventoryPane.CARRIED || entry == null) {
            return false;
        }

        QuickAccessInventoryActionResult result =
                hotbarLoadoutController.useFromInventoryAction(
                        entry.itemEntry().identity(),
                        layout.sourceIdsForPane(pane)
                );
        if (!result.started()) {
            return false;
        }

        showActionFeedback(result.feedback());
        if (minecraft != null && minecraft.screen == this) {
            requestImmediatePostActionRefresh();
        }
        return true;
    }

    private boolean dropItemFromInventory(InventoryViewData.EntryView entry, InventoryPane pane) {
        if (pane != InventoryPane.CARRIED || entry == null) {
            return false;
        }

        QuickAccessInventoryActionResult result =
                hotbarLoadoutController.dropFromInventoryAction(
                        entry.itemEntry().identity(),
                        layout.sourceIdsForPane(pane)
                );
        if (!result.started()) {
            return false;
        }

        showActionFeedback(result.feedback());
        if (minecraft != null && minecraft.screen == this) {
            requestImmediatePostActionRefresh();
        }
        return true;
    }

    private boolean singlePaneMode() {
        return screenContext.carriedOnly();
    }

    private int rowItemY(int y) {
        return rowItemY(y, ROW_HEIGHT, ROW_ITEM_SIZE);
    }

    private int rowSlotY(int y, int slotSize) {
        return rowSlotY(y, ROW_HEIGHT, slotSize);
    }

    private void openRowActionMenu(InventoryViewData.EntryView entry, InventoryPane pane, boolean recentSection, int anchorX, int anchorY) {
        List<ActionMenuItem> actions = buildRowActions(entry, pane, recentSection);
        if (actions.isEmpty()) {
            rowActionMenu = null;
            return;
        }

        rowActionMenu = createRowActionMenu(anchorX, anchorY, actions);
    }

    private List<ActionMenuItem> buildRowActions(InventoryViewData.EntryView entry, InventoryPane pane, boolean recentSection) {
        return InventoryRowActionPlanner.buildActions(
                        entry,
                        pane == InventoryPane.CARRIED,
                        recentSection,
                        hotbarLoadoutController,
                        layout.sourceIdsForPane(pane),
                        collectionStore,
                        () -> useItemFromInventory(entry, pane),
                        () -> dropItemFromInventory(entry, pane),
                        () -> dismissRecentEntry(entry),
                        () -> toggleFavorite(entry, pane),
                        () -> openInspectionForEntry(entry, pane),
                        () -> toggleCollectionMembership(entry, CollectionStore.JUNK_ID, pane),
                        () -> beginNewCollectionPrompt(entry, pane),
                        collectionId -> () -> toggleCollectionMembership(entry, collectionId, pane)
                ).stream()
                .map(action -> new ActionMenuItem(action.label(), action.action()))
                .toList();
    }

    private void openCollectionActionMenu(InventoryViewData.Section section, int anchorX, int anchorY) {
        openSectionActionMenu(section, InventoryPane.CARRIED, anchorX, anchorY);
    }

    private void openSectionActionMenu(InventoryViewData.Section section, InventoryPane pane, int anchorX, int anchorY) {
        List<ActionMenuItem> actions = buildSectionActions(section, pane, anchorX, anchorY);
        if (actions.isEmpty()) {
            rowActionMenu = null;
            return;
        }
        rowActionMenu = createRowActionMenu(anchorX, anchorY, actions);
    }

    private List<ActionMenuItem> buildSectionActions(InventoryViewData.Section section, InventoryPane pane, int anchorX, int anchorY) {
        List<ActionMenuItem> actions = new ArrayList<>();
        if (section == null) {
            return List.of();
        }

        if (pane == InventoryPane.CARRIED && !singlePaneMode()) {
            boolean excludeTrackedCollections = !section.isCollection();
            if (hasSectionTransferCandidates(section, pane, excludeTrackedCollections)) {
                actions.add(new ActionMenuItem(
                        Component.translatable(section.isRecent()
                                ? "slot.screen.recent.store"
                                : "slot.screen.container.action.stash_matching").getString(),
                        () -> storeSectionEntries(section, pane, excludeTrackedCollections)
                ));
            }
        }

        actions.addAll(InventorySectionActionPlanner.buildSharedActions(
                section,
                collectionStore,
                collectionViewStateController,
                pane == InventoryPane.CARRIED && !recentMatchingEntries(currentSearchQuery(), pane).isEmpty(),
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
                pane == InventoryPane.CARRIED ? () -> deleteVisibleJunkNow(section) : null
        ).stream().map(action -> new ActionMenuItem(action.label(), action.action())).toList());

        return List.copyOf(actions);
    }

    private List<ActionMenuItem> buildPaneActions(InventoryPane pane) {
        List<ActionMenuItem> actions = new ArrayList<>();
        if (pane == InventoryPane.CARRIED && !singlePaneMode()) {
            if (hasPaneMatchingTransferCandidates(pane, true)) {
                actions.add(new ActionMenuItem(
                        Component.translatable("slot.screen.container.action.stash_matching").getString(),
                        () -> storeMatchingPaneEntries(pane, true)
                ));
            }
            if (hasPaneTransferCandidates(pane, true)) {
                actions.add(new ActionMenuItem(
                        Component.translatable("slot.screen.container.action.stash_all").getString(),
                        () -> storePaneEntries(pane, true)
                ));
            }
        }
        return List.copyOf(actions);
    }

    private void openPaneActionMenu(InventoryPane pane, int anchorX, int anchorY) {
        List<ActionMenuItem> actions = buildPaneActions(pane);
        if (actions.isEmpty()) {
            rowActionMenu = null;
            return;
        }
        rowActionMenu = createRowActionMenu(anchorX, anchorY, actions);
    }

    private void dismissVisibleRecentEntries() {
        dismissVisibleRecentEntries(recentMatchingEntries(currentSearchQuery(), InventoryPane.CARRIED));
    }

    private void deleteVisibleJunkNow(InventoryViewData.Section section) {
        if (section == null) {
            return;
        }

        List<ItemIdentity> identities = paneVisibleEntriesBySection.getOrDefault(InventoryPane.CARRIED, Map.of())
                .getOrDefault(section.id(), List.of()).stream()
                .map(InventoryViewData.EntryView::itemEntry)
                .map(ItemEntry::identity)
                .distinct()
                .toList();
        if (identities.isEmpty()) {
            return;
        }

        int requested = 0;
        for (ItemIdentity identity : identities) {
            if (CursorTransferRequester.requestVoidMatchingCarriedAll(menu.containerId, identity)) {
                requested++;
            }
        }
        handleRequestedActionCommon(
                requested > 0,
                SlotActionResult.requested(Component.translatable("slot.screen.collections.junk.delete_all_requested", requested)),
                actionPlan(
                        false,
                        false,
                        false,
                        () -> queueTransferRefresh(InventoryPane.CARRIED),
                        null
                )
        );
    }

    private abstract class PaneListEntry extends InventoryScreenRow {
        protected final InventoryPane pane;

        private PaneListEntry(InventoryPane pane, String rowId) {
            super(rowId);
            this.pane = pane;
        }
    }

    private final class CollectionLoadoutEntry extends PaneListEntry {
        private final InventoryViewData.Section section;

        private CollectionLoadoutEntry(String rowId, InventoryViewData.Section section, InventoryPane pane) {
            super(pane, rowId);
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
                    new InventoryLoadoutRowSupport.LoadoutRowOptions(ROW_HEIGHT, singlePaneMode(), 3),
                    x,
                    y,
                    width,
                    mouseX,
                    mouseY
            );
            if (rowLayout == null) {
                return;
            }

            if (!singlePaneMode()) {
                return;
            }

            for (int slotIndex = 0; slotIndex < rowLayout.visiblePreviewSlots(); slotIndex++) {
                int slotX = rowLayout.previewX() + quickAccessSlotOffset(slotIndex, LOADOUT_SLOT_SIZE, LOADOUT_SLOT_GAP, LOADOUT_OFFHAND_GAP);
                int slotY = rowSlotY(y, LOADOUT_SLOT_SIZE);
                guiGraphics.fill(slotX, slotY, slotX + LOADOUT_SLOT_SIZE, slotY + LOADOUT_SLOT_SIZE, 0x60303030);
                ItemIdentity identity = loadout.identityForQuickAccessSlot(slotIndex);
                if (identity == null) {
                    renderQuickAccessOverlay(guiGraphics, slotIndex, slotX, slotY, 0.52F);
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

            InventoryProjectionSelectionList<InventoryScreenRow> list = listFor(InventoryPane.CARRIED);
            int rowLeft = list.getRowLeft();
            int rowTop = list.rowTop(list.children().indexOf(this));
            int rowWidth = list.getRowWidth();
            var rowLayout = loadoutRowSupport.rowLayout(
                    section.collectionId(),
                    selectedLoadout(section.collectionId()),
                    collectionStore.loadoutsFor(section.collectionId()).size(),
                    new InventoryLoadoutRowSupport.LoadoutRowOptions(ROW_HEIGHT, singlePaneMode(), 3),
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
                    new InventoryLoadoutRowSupport.LoadoutRowOptions(ROW_HEIGHT, singlePaneMode(), 3),
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

    private final class CollectionLoadoutPreviewEntry extends PaneListEntry {
        private final InventoryViewData.Section section;

        private CollectionLoadoutPreviewEntry(String rowId, InventoryViewData.Section section, InventoryPane pane) {
            super(pane, rowId);
            this.section = section;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick) {
            guiGraphics.fill(x, y + 1, x + width, y + height - 1, 0x36202020);

            HotbarLoadoutDefinition loadout = selectedLoadout(section.collectionId());
            if (loadout == null) {
                return;
            }

            int previewSpan = quickAccessSpanWidth(LOADOUT_SLOT_SIZE, LOADOUT_SLOT_GAP, LOADOUT_OFFHAND_GAP);
            int previewX = x + Math.max(6, (width - previewSpan) / 2);
            for (int slotIndex = 0; slotIndex < HotbarLoadoutDefinition.QUICK_ACCESS_SLOT_COUNT; slotIndex++) {
                int slotX = previewX + quickAccessSlotOffset(slotIndex, LOADOUT_SLOT_SIZE, LOADOUT_SLOT_GAP, LOADOUT_OFFHAND_GAP);
                int slotY = rowSlotY(y, LOADOUT_SLOT_SIZE);
                guiGraphics.fill(slotX, slotY, slotX + LOADOUT_SLOT_SIZE, slotY + LOADOUT_SLOT_SIZE, 0x60303030);
                ItemIdentity identity = loadout.identityForQuickAccessSlot(slotIndex);
                if (identity == null) {
                    renderQuickAccessOverlay(guiGraphics, slotIndex, slotX, slotY, 0.52F);
                    continue;
                }
                ItemStack previewStack = previewStack(identity);
                if (!previewStack.isEmpty()) {
                    renderScaledItem(guiGraphics, previewStack, slotX, slotY, LOADOUT_SLOT_SIZE / 16.0F);
                }
                renderQuickAccessOverlay(guiGraphics, slotIndex, slotX, slotY, 0.52F);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return button == 0;
        }
    }

    private final class PaneItemRowEntry extends PaneListEntry {
        private final InventoryViewData.EntryView entry;
        private final int localCount;
        private final String collectionId;
        private final boolean recentSection;

        private PaneItemRowEntry(
                String rowId,
                InventoryViewData.EntryView entry,
                InventoryPane pane,
                String collectionId,
                boolean recentSection
        ) {
            super(pane, rowId);
            this.entry = entry;
            this.localCount = localCount(entry, pane);
            this.collectionId = collectionId;
            this.recentSection = recentSection;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick) {
            boolean collectionTracked = pane == InventoryPane.CARRIED && collectionId != null;
            boolean equippedOnly = isEquippedOnly(entry, pane);
            String secondaryText = equippedOnly
                    ? Component.translatable("slot.screen.hotbar.equipped").getString()
                    : rowSecondaryText(entry, pane, Math.min(116, Math.max(48, width / 2)));
            itemRowSupport.renderRow(
                    guiGraphics,
                    new InventoryItemRowSupport.RowPresentation(
                            entry,
                            localCount,
                            collectionId,
                            collectionTracked,
                            desiredCount(),
                            secondaryText,
                            equippedOnly,
                            countTextColor(),
                            collectionTracked && localCount <= 0 ? 0xB8B8B8 : 0xFFFFFF,
                            Math.min(116, Math.max(48, width / 2))
                    ),
                    itemRowOptions(),
                    x,
                    y,
                    width
            );
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 2) {
                selectThis();
                beginHotbarDrag(entry, pane);
                return true;
            }
            if (button != 0 && button != 1) {
                return false;
            }

            selectThis();
            boolean desiredCountActive = pane == InventoryPane.CARRIED && collectionId != null;
            InventoryItemRowSupport.ClickTarget clickTarget = itemRowSupport.clickTarget(rowLayout(), desiredCountActive, mouseX);
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
                    localCount,
                    InventoryItemRowClickSupport.EmptyPrimaryClick.CONSUME
            );
            return InventoryItemRowClickSupport.execute(
                    intent,
                    new InventoryItemRowClickSupport.RowClickActions(
                            () -> openRowActionMenu(entry, pane, recentSection, iconMenuX(), actionMenuY()),
                            () -> beginInlineDesiredCountEdit(collectionId, entry.itemEntry().identity()),
                            () -> openRowActionMenu(entry, pane, recentSection, actionMenuX(), actionMenuY()),
                            () -> requestCursorPickup(entry, pane, CursorTransferPayload.Mode.HALF),
                            () -> requestCursorPaneDrop(pane, button),
                            () -> runMoveAllType(entry, pane),
                            () -> runMoveOne(entry, pane),
                            () -> requestCursorPickup(entry, pane, CursorTransferPayload.Mode.STACK),
                            () -> beginHotbarDrag(entry, pane)
                    )
            );
        }

        private InventoryItemRowSupport.RowLayout rowLayout() {
            InventoryProjectionSelectionList<InventoryScreenRow> list = listFor(pane);
            int rowIndex = list.children().indexOf(this);
            return itemRowSupport.rowLayout(list.getRowLeft(), list.rowTop(rowIndex), list.getRowWidth(), itemRowOptions());
        }

        private boolean isActionHit(double mouseX) {
            return itemRowSupport.clickTarget(rowLayout(), false, mouseX) == InventoryItemRowSupport.ClickTarget.ACTION;
        }

        private int actionMenuX() {
            return itemRowSupport.actionMenuX(listFor(pane).getRowRight());
        }

        private int iconMenuX() {
            return itemRowSupport.iconMenuX(rowLayout(), itemRowOptions());
        }

        private int actionMenuY() {
            return itemRowSupport.actionMenuY(rowLayout(), itemRowOptions());
        }

        private void selectThis() {
            openContainerList.setSelected(pane == InventoryPane.OPEN_CONTAINER ? this : null);
            carriedList.setSelected(pane == InventoryPane.CARRIED ? this : null);
            setSelectedEntry(entry, pane, rowId());
        }

        private int desiredCount() {
            return collectionId == null ? 0 : Math.max(1, collectionStore.desiredCount(collectionId, entry.itemEntry().identity()));
        }

        private int countTextColor() {
            if (pane != InventoryPane.CARRIED || collectionId == null) {
                return 0xEAEAEA;
            }
            if (localCount <= 0) {
                return 0xE0A0A0;
            }
            if (localCount < desiredCount()) {
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
                20
        );
    }

    private boolean requestCursorPickup(InventoryViewData.EntryView entry, InventoryPane pane, CursorTransferPayload.Mode mode) {
        if (entry == null || pane == null || entry.itemEntry().identity() == null) {
            return false;
        }
        InventoryCursorActionSupport.RequestedCursorAction action = InventoryCursorActionSupport.pickup(
                menu.containerId,
                pane,
                entry,
                mode
        );
        if (action.requested()) {
            SlotDebugLog.log(
                    "Requested cursor pickup from pane row: pane={} identity={} mode={}",
                    pane,
                    entry.itemEntry().identity().itemId(),
                    action.modeLabel()
            );
        }
        return handleRequestedActionCommon(
                action.requested(),
                action.requestedResult(),
                actionPlan(
                        action.suppressPositiveDeltas(),
                        false,
                        false,
                        () -> queueTransferRefresh(pane),
                        null
                )
        );
    }

    private boolean collectionNeedsRestock(String collectionId) {
        if (collectionId == null || collectionId.isBlank() || singlePaneMode() || CollectionStore.JUNK_ID.equals(collectionId)) {
            return false;
        }
        for (CollectionStore.CollectionItemTarget target : collectionStore.trackedItems(collectionId)) {
            int desiredCount = Math.max(1, target.desiredCount());
            int ownedCount = carriedCountFor(target.identity());
            if (ownedCount < desiredCount) {
                return true;
            }
        }
        return false;
    }

    private int carriedCountFor(ItemIdentity identity) {
        if (identity == null) {
            return 0;
        }
        for (InventoryViewData.EntryView entry : inventoryData.entries()) {
            if (ItemBehaviorPolicy.matchesTrackedIdentity(identity, entry.itemEntry().identity())) {
                return localCount(entry, InventoryPane.CARRIED);
            }
        }
        return 0;
    }

    private ItemIdentity nextJunkIdentityToVoid() {
        ItemIdentity bestIdentity = null;
        int bestCount = 0;
        for (InventoryViewData.EntryView entry : inventoryData.entries()) {
            if (!entry.itemEntry().collectionIds().contains(CollectionStore.JUNK_ID)) {
                continue;
            }

            int carriedCount = localCount(entry, InventoryPane.CARRIED);
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

    private void restockCollection(String collectionId) {
        if (collectionId == null || collectionId.isBlank() || singlePaneMode() || CollectionStore.JUNK_ID.equals(collectionId)) {
            return;
        }

        List<ItemIdentity> requestedIdentities = new ArrayList<>();
        List<CollectionStore.CollectionItemTarget> pendingTargets = new ArrayList<>();
        for (CollectionStore.CollectionItemTarget target : collectionStore.trackedItems(collectionId)) {
            int ownedCount = carriedCountFor(target.identity());
            int desiredCount = Math.max(1, target.desiredCount());
            int requestedCount = Math.max(0, desiredCount - ownedCount);
            if (requestedCount <= 0) {
                continue;
            }
            requestedIdentities.add(target.identity());
            pendingTargets.add(target);
        }

        if (pendingTargets.isEmpty()) {
            return;
        }

        boolean sentAny = false;
        for (CollectionStore.CollectionItemTarget target : pendingTargets) {
            int ownedCount = carriedCountFor(target.identity());
            int desiredCount = Math.max(1, target.desiredCount());
            int requestedCount = Math.max(0, desiredCount - ownedCount);
            if (requestedCount <= 0) {
                continue;
            }
            sentAny |= BackpackTransferRequester.requestExternalToCarried(menu.containerId, target.identity(), requestedCount);
        }

        if (!sentAny) {
            handleRequestedActionCommon(false, SlotActionResult.NONE, actionPlan(false, false, false));
            return;
        }

        SlotDebugLog.log(
                "Requested collection restock from external storage: collection={} trackedItems={}",
                collectionId,
                collectionStore.trackedItems(collectionId).size()
        );
        var definition = collectionStore.collectionOrNull(collectionId);
        handleActionResultCommon(
                SlotActionResult.requested(Component.translatable(
                        "slot.screen.action.restock_collection.requested",
                        definition == null ? collectionId : definition.name()
                )),
                actionPlan(
                        false,
                        false,
                        false,
                        () -> queueTransferRefresh(InventoryPane.CARRIED),
                        null
                )
        );
    }

    private List<InventoryViewData.EntryView> visibleEntriesForSection(InventoryViewData.Section section, InventoryPane pane, boolean excludeTrackedCollections) {
        if (section == null || pane == null) {
            return List.of();
        }
        return paneVisibleEntriesBySection.getOrDefault(pane, Map.of()).getOrDefault(section.id(), List.of()).stream()
                .filter(entry -> !isProtectedFromBulkStore(entry, excludeTrackedCollections))
                .toList();
    }

    private boolean hasSectionTransferCandidates(InventoryViewData.Section section, InventoryPane pane, boolean excludeTrackedCollections) {
        return !visibleEntriesForSection(section, pane, excludeTrackedCollections).isEmpty();
    }

    private void storeSectionEntries(InventoryViewData.Section section, InventoryPane pane, boolean excludeTrackedCollections) {
        if (section == null || pane != InventoryPane.CARRIED || singlePaneMode() || shouldDeferExternalTransfer(pane)) {
            return;
        }

        List<InventoryViewData.EntryView> entries = visibleEntriesForSection(section, pane, excludeTrackedCollections);
        if (entries.isEmpty()) {
            return;
        }

        if (!runVisibleStoreTransfer(pane, entries)) {
            return;
        }

        SlotDebugLog.log(
                "Requested section store into external storage: section={} excludeTracked={} entryCount={}",
                section.id(),
                excludeTrackedCollections,
                entries.size()
        );
    }

    private boolean hasPaneTransferCandidates(InventoryPane pane, boolean excludeTrackedCollections) {
        return !visibleEntriesForPane(pane, excludeTrackedCollections).isEmpty();
    }

    private boolean hasPaneMatchingTransferCandidates(InventoryPane pane, boolean excludeTrackedCollections) {
        return !visibleMatchingEntriesForPane(pane, excludeTrackedCollections).isEmpty();
    }

    private List<InventoryViewData.EntryView> visibleEntriesForPane(InventoryPane pane, boolean excludeTrackedCollections) {
        if (pane == null) {
            return List.of();
        }
        return inventoryData.entries().stream()
                .filter(entry -> localCount(entry, pane) > 0)
                .filter(entry -> !isProtectedFromBulkStore(entry, excludeTrackedCollections))
                .toList();
    }

    private List<InventoryViewData.EntryView> visibleMatchingEntriesForPane(InventoryPane pane, boolean excludeTrackedCollections) {
        if (pane == null) {
            return List.of();
        }
        return inventoryData.entries().stream()
                .filter(entry -> localCount(entry, pane) > 0)
                .filter(entry -> compareCount(entry, pane) > 0)
                .filter(entry -> !isProtectedFromBulkStore(entry, excludeTrackedCollections))
                .toList();
    }

    private boolean isProtectedFromBulkStore(InventoryViewData.EntryView entry, boolean excludeTrackedCollections) {
        if (entry == null) {
            return false;
        }
        if (ItemBehaviorPolicy.shouldProtectFromBulkStore(entry.displayStack())) {
            return true;
        }
        return excludeTrackedCollections && hasTrackedCollectionMembership(entry.itemEntry());
    }

    private boolean hasTrackedCollectionMembership(ItemEntry entry) {
        if (entry == null || entry.identity() == null) {
            return false;
        }
        return !collectionStore.collectionsFor(entry.identity()).isEmpty();
    }

    private void storePaneEntries(InventoryPane pane, boolean excludeTrackedCollections) {
        if (pane != InventoryPane.CARRIED || singlePaneMode() || shouldDeferExternalTransfer(pane)) {
            return;
        }

        List<InventoryViewData.EntryView> entries = visibleEntriesForPane(pane, excludeTrackedCollections);
        if (entries.isEmpty()) {
            return;
        }

        if (!runVisibleStoreTransfer(pane, entries)) {
            return;
        }

        SlotDebugLog.log(
                "Requested carried pane store into external storage: excludeTracked={} entryCount={}",
                excludeTrackedCollections,
                entries.size()
        );
    }

    private void storeMatchingPaneEntries(InventoryPane pane, boolean excludeTrackedCollections) {
        if (pane != InventoryPane.CARRIED || singlePaneMode() || shouldDeferExternalTransfer(pane)) {
            return;
        }

        List<InventoryViewData.EntryView> entries = visibleMatchingEntriesForPane(pane, excludeTrackedCollections);
        if (entries.isEmpty()) {
            return;
        }

        if (!runVisibleStoreTransfer(pane, entries)) {
            return;
        }

        SlotDebugLog.log(
                "Requested carried pane matching store into external storage: excludeTracked={} entryCount={}",
                excludeTrackedCollections,
                entries.size()
        );
    }

    private boolean runVisibleStoreTransfer(InventoryPane pane, List<InventoryViewData.EntryView> entries) {
        if (pane == null || entries == null || entries.isEmpty()) {
            return false;
        }

        SlotActionResult result = InventoryTransferActionSupport.moveWorkspaceVisible(
                actionExecutor,
                currentPlayer(),
                menu,
                entries,
                pane
        );
        handleActionResultCommon(
                result,
                actionPlan(
                        false,
                        false,
                        false,
                        () -> queueTransferRefresh(pane),
                        null
                )
        );
        return result.successful();
    }

    private List<Component> trashTooltipLines() {
        return SlotTrashBuffer.buildTooltipLines(trashWarningState());
    }

    private TrashSlotLayout trashSlotLayout() {
        int top = hotbarY() + Math.max(0, (HOTBAR_SLOT_SIZE - TRASH_SLOT_SIZE) / 2);
        int left = hotbarStripX() - TRASH_SLOT_GAP - TRASH_SLOT_SIZE;
        int minLeft = carriedPaneX() + 4;
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

    private boolean autoVoidPausedForExternalStorage() {
        return !singlePaneMode();
    }

    private SlotTrashWarningState trashWarningState() {
        if (inventoryData == null || !carriedCapacity.available() || carriedCapacity.freeSlots() > 1) {
            return SlotTrashWarningState.NONE;
        }

        ItemStack nextStack = nextJunkPreviewStack();
        if (nextStack.isEmpty()) {
            return SlotTrashWarningState.NONE;
        }
        return new SlotTrashWarningState(nextStack, autoVoidPausedForExternalStorage());
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

    private InventoryProjectionSelectionList<InventoryScreenRow> listFor(InventoryPane pane) {
        return pane == InventoryPane.OPEN_CONTAINER ? openContainerList : carriedList;
    }

    private InventoryPane paneForList(InventoryProjectionSelectionList<InventoryScreenRow> paneList) {
        return paneList == openContainerList ? InventoryPane.OPEN_CONTAINER : InventoryPane.CARRIED;
    }

    private record DragTransferKey(InventoryPane pane, ItemIdentity identity) {
    }

}
