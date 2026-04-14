package dev.imagio.slot.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import dev.imagio.slot.client.collection.CollectionStore;
import dev.imagio.slot.client.collection.CollectionStockSummary;
import dev.imagio.slot.client.collection.CollectionViewStateController;
import dev.imagio.slot.client.collection.HotbarLoadoutDefinition;
import dev.imagio.slot.client.collection.HotbarLoadoutSlot;
import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.client.policy.ItemBehaviorPolicy;
import dev.imagio.slot.projection.InventoryViewData;
import dev.imagio.slot.workflow.CollectionWorkflowService;
import dev.imagio.slot.workflow.InspectionService;
import dev.imagio.slot.workflow.SearchWorkflowService;
import dev.imagio.slot.client.screen.debug.SlotDebugSortMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class AbstractInventoryBrowserScreen<M extends AbstractContainerMenu> extends AbstractSlotPanelScreen<M> {
    private static final AtomicLong SCREEN_CONTEXT_IDS = new AtomicLong();

    protected static final int OUTER_MARGIN = 8;
    protected static final int COLLECTION_BUTTON_HEIGHT = 9;
    protected static final int COLLECTION_BUTTON_GAP = 3;
    protected static final int COLLECTION_PIN_BUTTON_WIDTH = 16;
    protected static final int COLLECTION_TOGGLE_BUTTON_WIDTH = 12;
    protected static final int COLLECTION_MENU_BUTTON_WIDTH = 20;
    protected static final int COLLECTION_RESTOCK_BUTTON_WIDTH = 24;
    protected static final int COLLECTION_CREATE_PROMPT_WIDTH = 228;
    protected static final int COLLECTION_CREATE_PROMPT_HEIGHT = 60;
    protected static final int COLLECTION_CREATE_PROMPT_BUTTON_WIDTH = 56;
    protected static final int LOADOUT_ARROW_WIDTH = 10;
    protected static final int LOADOUT_NAME_WIDTH = 64;
    protected static final int LOADOUT_BUTTON_WIDTH = 18;
    protected static final int LOADOUT_SLOT_SIZE = 10;
    protected static final int LOADOUT_SLOT_GAP = 1;
    protected static final int LOADOUT_OFFHAND_GAP = 3;
    protected static final float ROW_TEXT_SCALE = 0.60F;

    protected CollectionStore collectionStore;
    protected Supplier<Component> currentScreenToggleLabel;
    protected final InventoryScreenContext screenContext;
    protected final CollectionViewStateController collectionViewStateController;
    protected final CollectionWorkflowService collectionWorkflow;
    protected final SearchWorkflowService searchWorkflow;
    protected final InspectionService inspectionService;
    protected final QuickAccessService hotbarLoadoutController;
    protected final InventorySectionHeaderSupport sectionHeaderSupport;
    protected final InventoryLoadoutRowSupport loadoutRowSupport;
    protected final InventoryItemRowSupport itemRowSupport;
    private final String historyContextKey = getClass().getName() + "#" + SCREEN_CONTEXT_IDS.incrementAndGet();

    protected InventoryViewData inventoryData;
    protected EditBox searchBox;
    protected EditBox inlineLoadoutRenameBox;
    protected EditBox inlineDesiredCountBox;
    protected EditBox newCollectionNameBox;
    protected Button sortButton;
    protected Button undoButton;
    protected Button redoButton;
    protected Button collectionsButton;
    protected Button settingsButton;
    protected Button screenToggleButton;
    protected Button vanillaButton;
    protected SlotDebugSortMode sortMode = SlotDebugSortMode.NAME;
    protected ItemIdentity selectedIdentity;
    protected String selectedRowId;
    protected InventoryViewData.EntryView selectedEntry;
    protected final InlineLoadoutRenameState inlineLoadoutRenameState = new InlineLoadoutRenameState();
    protected final InlineDesiredCountState inlineDesiredCountState = new InlineDesiredCountState();
    protected final NewCollectionPromptState newCollectionPromptState = new NewCollectionPromptState();
    protected PopupActionMenu rowActionMenu;
    protected final SlotActionFeedbackState actionFeedback = new SlotActionFeedbackState();
    private final InventoryActionOrchestrator.Hooks actionOrchestratorHooks = new InventoryActionOrchestrator.Hooks() {
        @Override
        public void showActionFeedback(SlotActionResult result) {
            AbstractInventoryBrowserScreen.this.showActionFeedback(result);
        }

        @Override
        public void schedulePostActionRefresh() {
            AbstractInventoryBrowserScreen.this.schedulePostActionRefresh();
        }

        @Override
        public void refreshInventoryData() {
            AbstractInventoryBrowserScreen.this.refreshInventoryData();
        }

        @Override
        public void clearPendingPostActionRefresh() {
            AbstractInventoryBrowserScreen.this.clearPendingPostActionRefresh();
        }

        @Override
        public void updateDynamicButtons() {
            AbstractInventoryBrowserScreen.this.updateDynamicButtons();
        }

        @Override
        public String historyContextKey() {
            return AbstractInventoryBrowserScreen.this.historyContextKey();
        }
    };

    protected AbstractInventoryBrowserScreen(
            M menu,
            Inventory playerInventory,
            Component title,
            InventoryScreenContext screenContext,
            CollectionStore collectionStore,
            CollectionViewStateController collectionViewStateController,
            SearchWorkflowService searchWorkflow,
            InspectionService inspectionService
    ) {
        super(menu, playerInventory, title);
        this.collectionStore = collectionStore;
        this.screenContext = screenContext;
        this.collectionViewStateController = collectionViewStateController == null
                ? CollectionViewStateController.NOOP
                : collectionViewStateController;
        this.collectionWorkflow = new CollectionWorkflowService(collectionStore, this.collectionViewStateController);
        this.searchWorkflow = searchWorkflow == null ? new SearchWorkflowService() : searchWorkflow;
        this.inspectionService = inspectionService == null ? new InspectionService(collectionStore) : inspectionService;
        this.hotbarLoadoutController = new QuickAccessService(screenContext);
        this.sectionHeaderSupport = new InventorySectionHeaderSupport(this);
        this.loadoutRowSupport = new InventoryLoadoutRowSupport(this);
        this.itemRowSupport = new InventoryItemRowSupport(this);
    }

    protected void setSelectedEntry(InventoryViewData.EntryView entry) {
        setSelectedEntry(entry, null);
    }

    protected void setSelectedEntry(InventoryViewData.EntryView entry, String rowId) {
        selectedEntry = entry;
        selectedIdentity = entry == null ? null : entry.itemEntry().identity();
        selectedRowId = rowId;
    }

    protected void cycleSort() {
        sortMode = sortMode.next();
        updateSortButton();
        rebuildVisibleEntries();
    }

    protected void updateSortButton() {
        if (sortButton != null) {
            sortButton.setMessage(sortMode.shortLabel());
            sortButton.setTooltip(Tooltip.create(Component.translatable("slot.screen.debug.sort_button", sortMode.displayName())));
        }
    }

    protected void updateDynamicButtons() {
        if (screenToggleButton != null && currentScreenToggleLabel != null) {
            screenToggleButton.setTooltip(Tooltip.create(currentScreenToggleLabel.get()));
        }
        if (undoButton != null) {
            undoButton.active = SlotUndoHistory.canUndo(historyContextKey());
            undoButton.setTooltip(Tooltip.create(Component.translatable("slot.screen.inventory.undo.tooltip")));
        }
        if (redoButton != null) {
            redoButton.active = SlotUndoHistory.canRedo(historyContextKey());
            redoButton.setTooltip(Tooltip.create(Component.translatable("slot.screen.inventory.redo.tooltip")));
        }
    }

    protected boolean collectionHasLoadouts(String collectionId) {
        return collectionWorkflow.collectionHasLoadouts(collectionId);
    }

    protected void normalizeInlineCollectionState() {
        collectionWorkflow.normalizeViewState(inventoryData);

        if (isInlineLoadoutRenameActive() && !inlineLoadoutRenameState.matchesSelectedLoadout(this::selectedLoadout)) {
            cancelInlineLoadoutRename();
        }

        if (isInlineDesiredCountActive()) {
            String collectionId = inlineDesiredCountState.collectionId();
            ItemIdentity identity = inlineDesiredCountState.identity();
            if (collectionId == null
                    || identity == null
                    || !collectionStore.collectionsFor(identity).contains(collectionId)) {
                cancelInlineDesiredCount();
            }
        }
    }

    protected void ensureSelectedLoadoutId(String collectionId) {
        collectionWorkflow.normalizeViewState(inventoryData);
    }

    protected HotbarLoadoutDefinition selectedLoadout(String collectionId) {
        return collectionWorkflow.selectedLoadout(collectionId);
    }

    protected String nextAutoLoadoutName(String collectionId) {
        return collectionWorkflow.nextAutoLoadoutName(
                collectionId,
                index -> Component.translatable("slot.screen.collections.loadout.auto_name", index).getString()
        );
    }

    protected void cycleSelectedLoadoutHotkey(String collectionId) {
        if (collectionWorkflow.cycleSelectedLoadoutHotkey(collectionId)) {
            refreshInventoryData();
        }
    }

    protected void clearSelectedLoadoutHotkey(String collectionId) {
        if (collectionWorkflow.clearSelectedLoadoutHotkey(collectionId)) {
            refreshInventoryData();
        }
    }

    protected void deleteSelectedLoadout(String collectionId) {
        HotbarLoadoutDefinition loadout = selectedLoadout(collectionId);
        if (loadout == null) {
            return;
        }

        if (inlineLoadoutRenameState.isTarget(collectionId, loadout)) {
            cancelInlineLoadoutRename();
        }
        if (collectionWorkflow.deleteSelectedLoadout(collectionId)) {
            refreshInventoryData();
        }
    }

    protected boolean handleSearchKeyPress(int keyCode) {
        if (!isSearchInputActive()) {
            return false;
        }
        if (keyCode != GLFW.GLFW_KEY_ENTER
                && keyCode != GLFW.GLFW_KEY_KP_ENTER
                && keyCode != GLFW.GLFW_KEY_ESCAPE) {
            return false;
        }

        searchBox.setFocused(false);
        if (getFocused() == searchBox) {
            setFocused(null);
        }
        return true;
    }

    protected boolean handleSearchBoxClick(double mouseX, double mouseY, int button) {
        if (searchBox == null || button != 1 || !searchBox.isMouseOver(mouseX, mouseY)) {
            return false;
        }

        searchBox.setValue("");
        searchWorkflow.clear();
        searchBox.setFocused(true);
        setFocused(searchBox);
        return true;
    }

    protected void beginInlineLoadoutRename(String collectionId) {
        HotbarLoadoutDefinition loadout = selectedLoadout(collectionId);
        if (loadout == null || inlineLoadoutRenameBox == null) {
            return;
        }

        inlineLoadoutRenameState.begin(collectionId, loadout);
        inlineLoadoutRenameBox.setValue(loadout.name());
        inlineLoadoutRenameBox.moveCursorToEnd(false);
        inlineLoadoutRenameBox.setVisible(true);
        inlineLoadoutRenameBox.setFocused(true);
        setFocused(inlineLoadoutRenameBox);
    }

    protected boolean isInlineLoadoutRenameActive() {
        return inlineLoadoutRenameBox != null
                && inlineLoadoutRenameBox.isVisible()
                && inlineLoadoutRenameState.isActive();
    }

    protected boolean isInlineLoadoutRenameTarget(String collectionId, HotbarLoadoutDefinition loadout) {
        return inlineLoadoutRenameState.isTarget(collectionId, loadout);
    }

    protected void syncInlineLoadoutRenameBox(String collectionId, int nameX, int nameEditWidth, HotbarLoadoutDefinition loadout, int y) {
        if (!isInlineLoadoutRenameTarget(collectionId, loadout) || inlineLoadoutRenameBox == null) {
            return;
        }

        inlineLoadoutRenameState.markLaidOut();
        inlineLoadoutRenameBox.setRectangle(nameEditWidth, LOADOUT_SLOT_SIZE, nameX, y + 1);
        inlineLoadoutRenameBox.setVisible(true);
    }

    protected boolean commitInlineLoadoutRename() {
        if (!isInlineLoadoutRenameActive()) {
            return false;
        }

        var commit = inlineLoadoutRenameState.commit(inlineLoadoutRenameBox.getValue()).orElse(null);
        cancelInlineLoadoutRename();
        if (commit == null || commit.newName().isEmpty()) {
            return true;
        }

        HotbarLoadoutDefinition loadout = collectionStore.loadout(commit.collectionId(), commit.loadoutId());
        if (collectionWorkflow.renameLoadout(commit.collectionId(), commit.loadoutId(), commit.newName())) {
            refreshInventoryData();
        }
        return true;
    }

    protected void beginInlineDesiredCountEdit(String collectionId, ItemIdentity identity) {
        if (collectionId == null || identity == null || inlineDesiredCountBox == null) {
            return;
        }

        inlineDesiredCountState.begin(collectionId, identity);
        int desiredCount = collectionStore.desiredCount(collectionId, identity);
        inlineDesiredCountBox.setValue(Integer.toString(Math.max(1, desiredCount)));
        inlineDesiredCountBox.moveCursorToEnd(false);
        inlineDesiredCountBox.setHighlightPos(0);
        inlineDesiredCountBox.setVisible(true);
        inlineDesiredCountBox.setFocused(true);
        setFocused(inlineDesiredCountBox);
    }

    protected boolean isInlineDesiredCountActive() {
        return inlineDesiredCountBox != null
                && inlineDesiredCountBox.isVisible()
                && inlineDesiredCountState.isActive();
    }

    protected boolean isInlineDesiredCountTarget(String collectionId, ItemIdentity identity) {
        return inlineDesiredCountState.isTarget(collectionId, identity);
    }

    protected void syncInlineDesiredCountBox(int x, int y, int width, String collectionId, ItemIdentity identity) {
        if (!isInlineDesiredCountTarget(collectionId, identity) || inlineDesiredCountBox == null) {
            return;
        }

        inlineDesiredCountState.markLaidOut();
        inlineDesiredCountBox.setRectangle(width, LOADOUT_SLOT_SIZE, x, y + 1);
        inlineDesiredCountBox.setVisible(true);
    }

    protected boolean commitInlineDesiredCount() {
        if (!isInlineDesiredCountActive()) {
            return false;
        }

        var commit = inlineDesiredCountState.commit(inlineDesiredCountBox.getValue()).orElse(null);
        cancelInlineDesiredCount();
        if (commit == null) {
            return true;
        }

        if (collectionStore.desiredCount(commit.collectionId(), commit.identity()) != commit.desiredCount()) {
            collectionStore.setDesiredCount(commit.collectionId(), commit.identity(), commit.desiredCount());
            refreshInventoryData();
        }
        return true;
    }

    protected void cancelInlineDesiredCount() {
        if (inlineDesiredCountBox != null) {
            inlineDesiredCountBox.setVisible(false);
            inlineDesiredCountBox.setFocused(false);
        }
        if (getFocused() == inlineDesiredCountBox) {
            setFocused(null);
        }
        inlineDesiredCountState.cancel();
    }

    protected void cancelInlineLoadoutRename() {
        if (inlineLoadoutRenameBox != null) {
            inlineLoadoutRenameBox.setVisible(false);
            inlineLoadoutRenameBox.setFocused(false);
        }
        if (getFocused() == inlineLoadoutRenameBox) {
            setFocused(null);
        }
        inlineLoadoutRenameState.cancel();
    }

    protected void beginNewCollectionPrompt(InventoryViewData.EntryView entry) {
        if (entry == null || entry.itemEntry().identity() == null || newCollectionNameBox == null) {
            return;
        }

        setSelectedEntry(entry);
        rowActionMenu = null;
        newCollectionPromptState.begin(entry.itemEntry().identity());
        newCollectionNameBox.setValue(entry.displayName());
        newCollectionNameBox.moveCursorToEnd(false);
        newCollectionNameBox.setHighlightPos(0);
        newCollectionNameBox.setVisible(true);
        newCollectionNameBox.setFocused(true);
        setFocused(newCollectionNameBox);
    }

    protected boolean isNewCollectionPromptActive() {
        return newCollectionNameBox != null
                && newCollectionNameBox.isVisible()
                && newCollectionPromptState.isActive();
    }

    protected boolean commitNewCollectionPrompt() {
        if (!isNewCollectionPromptActive()) {
            return false;
        }

        var commit = newCollectionPromptState.commit(newCollectionNameBox.getValue()).orElse(null);
        cancelNewCollectionPrompt();
        if (commit == null || commit.name().isEmpty()) {
            return true;
        }

        try {
            var created = collectionStore.createCollection(commit.name());
            collectionStore.addToCollection(created.id(), commit.identity());
            onCollectionCreated(created.id());
            refreshInventoryData();
        } catch (IllegalArgumentException ignored) {
        }
        return true;
    }

    protected void cancelNewCollectionPrompt() {
        if (newCollectionNameBox != null) {
            newCollectionNameBox.setVisible(false);
            newCollectionNameBox.setFocused(false);
        }
        if (getFocused() == newCollectionNameBox) {
            setFocused(null);
        }
        newCollectionPromptState.cancel();
    }

    protected CollectionPromptLayout collectionPromptLayout() {
        int promptX = centerPaneX() + Math.max(8, (centerPaneWidth() - COLLECTION_CREATE_PROMPT_WIDTH) / 2);
        int promptY = panelTop() + Math.max(34, (panelHeight() - COLLECTION_CREATE_PROMPT_HEIGHT) / 2);
        int inputX = promptX + 8;
        int inputY = promptY + 20;
        int inputWidth = COLLECTION_CREATE_PROMPT_WIDTH - 16;
        newCollectionNameBox.setRectangle(inputWidth, 18, inputX, inputY);
        newCollectionNameBox.setVisible(true);

        InlineButton cancelButton = new InlineButton(
                promptX + COLLECTION_CREATE_PROMPT_WIDTH - 8 - COLLECTION_CREATE_PROMPT_BUTTON_WIDTH,
                promptY + COLLECTION_CREATE_PROMPT_HEIGHT - 18,
                COLLECTION_CREATE_PROMPT_BUTTON_WIDTH,
                COLLECTION_BUTTON_HEIGHT,
                Component.translatable("gui.cancel").getString(),
                true
        );
        InlineButton createButton = new InlineButton(
                cancelButton.x() - COLLECTION_BUTTON_GAP - COLLECTION_CREATE_PROMPT_BUTTON_WIDTH,
                cancelButton.y(),
                COLLECTION_CREATE_PROMPT_BUTTON_WIDTH,
                COLLECTION_BUTTON_HEIGHT,
                Component.translatable("slot.screen.collections.create").getString(),
                !newCollectionNameBox.getValue().trim().isEmpty()
        );
        return new CollectionPromptLayout(promptX, promptY, inputX, inputY, inputWidth, createButton, cancelButton);
    }

    protected void renderNewCollectionPrompt(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        CollectionPromptLayout layout = collectionPromptLayout();
        guiGraphics.fill(layout.x(), layout.y(), layout.x() + COLLECTION_CREATE_PROMPT_WIDTH, layout.y() + COLLECTION_CREATE_PROMPT_HEIGHT, 0xE0101010);
        guiGraphics.fill(layout.x() + 1, layout.y() + 1, layout.x() + COLLECTION_CREATE_PROMPT_WIDTH - 1, layout.y() + COLLECTION_CREATE_PROMPT_HEIGHT - 1, 0xF0181818);
        guiGraphics.drawString(font, Component.translatable("slot.screen.collections.new_collection_title"), layout.x() + 8, layout.y() + 8, 0xF0F0F0, false);
        newCollectionNameBox.render(guiGraphics, mouseX, mouseY, partialTick);
        drawInlineButton(guiGraphics, layout.createButton().x(), layout.createButton().y(), layout.createButton().width(), layout.createButton().height(), layout.createButton().label(), layout.createButton().enabled(), layout.createButton().contains(mouseX, mouseY));
        drawInlineButton(guiGraphics, layout.cancelButton().x(), layout.cancelButton().y(), layout.cancelButton().width(), layout.cancelButton().height(), layout.cancelButton().label(), layout.cancelButton().enabled(), layout.cancelButton().contains(mouseX, mouseY));
    }

    protected void deleteCollection(String collectionId) {
        if (!collectionStore.isUserCollection(collectionId)) {
            return;
        }
        if (inlineDesiredCountState.targetsCollection(collectionId)) {
            cancelInlineDesiredCount();
        }
        if (inlineLoadoutRenameState.targetsCollection(collectionId)) {
            cancelInlineLoadoutRename();
        }
        if (collectionWorkflow.deleteCollection(collectionId)) {
            refreshInventoryData();
        }
    }

    protected boolean handleInlineDesiredCountClick(double mouseX, double mouseY, int button) {
        if (!isInlineDesiredCountActive()) {
            return false;
        }
        if (inlineDesiredCountBox.isMouseOver(mouseX, mouseY)) {
            if (inlineDesiredCountBox.mouseClicked(mouseX, mouseY, button)) {
                setFocused(inlineDesiredCountBox);
                if (button == 0) {
                    setDragging(true);
                }
                return true;
            }
            return false;
        }
        if (button == 0) {
            commitInlineDesiredCount();
            return true;
        }
        return false;
    }

    protected boolean handleInlineLoadoutRenameClick(double mouseX, double mouseY, int button) {
        if (!isInlineLoadoutRenameActive()) {
            return false;
        }
        if (inlineLoadoutRenameBox.isMouseOver(mouseX, mouseY)) {
            if (inlineLoadoutRenameBox.mouseClicked(mouseX, mouseY, button)) {
                setFocused(inlineLoadoutRenameBox);
                if (button == 0) {
                    setDragging(true);
                }
                return true;
            }
            return false;
        }
        if (button == 0) {
            commitInlineLoadoutRename();
            return true;
        }
        return false;
    }

    protected boolean handleNewCollectionPromptClick(double mouseX, double mouseY, int button) {
        if (!isNewCollectionPromptActive()) {
            return false;
        }
        if (button != 0 && button != 1) {
            return true;
        }

        CollectionPromptLayout layout = collectionPromptLayout();
        if (newCollectionNameBox.isMouseOver(mouseX, mouseY)) {
            if (newCollectionNameBox.mouseClicked(mouseX, mouseY, button)) {
                setFocused(newCollectionNameBox);
                if (button == 0) {
                    setDragging(true);
                }
                return true;
            }
            return false;
        }

        if (button == 0 && layout.createButton().contains(mouseX, mouseY)) {
            commitNewCollectionPrompt();
            return true;
        }
        if (button == 0 && layout.cancelButton().contains(mouseX, mouseY)) {
            cancelNewCollectionPrompt();
            return true;
        }
        if (button == 0) {
            cancelNewCollectionPrompt();
            return true;
        }
        return true;
    }

    protected void showActionFeedback(SlotActionResult result) {
        actionFeedback.show(result);
    }

    protected final void requestImmediatePostActionRefresh() {
        schedulePostActionRefresh();
        refreshInventoryData();
    }

    protected final void applyUndoHistoryResultCommon(
            SlotUndoHistory.ApplyResult result,
            boolean undo,
            Runnable transferSyncHandler
    ) {
        InventoryActionOrchestrator.applyUndoHistoryResult(result, undo, actionOrchestratorHooks, transferSyncHandler);
    }

    protected final void applyConfirmedActionOutcomeCommon() {
        InventoryActionOrchestrator.applyConfirmedActionOutcome(actionOrchestratorHooks);
    }

    protected final void handleActionResultCommon(
            SlotActionResult result,
            InventoryActionOrchestrator.ActionPlan actionPlan
    ) {
        InventoryActionOrchestrator.handleActionResult(result, actionOrchestratorHooks, actionPlan);
    }

    protected final boolean handleRequestedActionCommon(
            boolean requested,
            SlotActionResult requestedResult,
            InventoryActionOrchestrator.ActionPlan actionPlan
    ) {
        return InventoryActionOrchestrator.handleRequestedAction(requested, requestedResult, actionOrchestratorHooks, actionPlan);
    }

    protected final InventoryActionOrchestrator.ActionPlan actionPlan(
            boolean suppressPositiveDeltas,
            boolean schedulePostActionRefresh,
            boolean refreshImmediately
    ) {
        return InventoryActionOrchestrator.ActionPlan.of(
                suppressPositiveDeltas,
                schedulePostActionRefresh,
                refreshImmediately
        );
    }

    protected final InventoryActionOrchestrator.ActionPlan actionPlan(
            boolean suppressPositiveDeltas,
            boolean schedulePostActionRefresh,
            boolean refreshImmediately,
            Runnable onSuccess,
            Runnable onFailure
    ) {
        return actionPlan(suppressPositiveDeltas, schedulePostActionRefresh, refreshImmediately)
                .withCallbacks(onSuccess, onFailure);
    }

    protected void toggleFavorite(InventoryViewData.EntryView entry) {
        if (entry != null && collectionWorkflow.toggleFavorite(entry.itemEntry().identity(), entry.itemEntry().favorite())) {
            refreshInventoryData();
        }
    }

    protected void toggleCollectionMembership(InventoryViewData.EntryView entry, String collectionId) {
        if (entry != null && collectionWorkflow.toggleCollectionMembership(
                entry.itemEntry().identity(),
                entry.itemEntry().collectionIds(),
                collectionId
        )) {
            refreshInventoryData();
        }
    }

    protected void toggleSelectedItemMembership(String collectionId) {
        if (selectedEntry == null || collectionId == null || collectionId.isBlank()) {
            return;
        }

        toggleCollectionMembership(selectedEntry, collectionId);
    }

    protected void dismissRecentEntry(InventoryViewData.EntryView entry) {
        if (entry == null) {
            return;
        }
        RecentLootTracker.dismiss(entry.itemEntry().identity());
        showActionFeedback(SlotActionResult.applied(Component.translatable("slot.screen.recent.dismissed")));
        refreshInventoryData();
    }

    protected void dismissVisibleRecentEntries(List<InventoryViewData.EntryView> entries) {
        List<ItemIdentity> identities = entries.stream()
                .map(InventoryViewData.EntryView::itemEntry)
                .map(itemEntry -> itemEntry.identity())
                .toList();
        if (identities.isEmpty()) {
            return;
        }
        RecentLootTracker.dismissAll(identities);
        showActionFeedback(SlotActionResult.applied(Component.translatable("slot.screen.recent.cleared")));
        refreshInventoryData();
    }

    protected void openCollections() {
        if (minecraft == null) {
            return;
        }

        minecraft.setScreen(new SlotCollectionManagementScreen(
                this,
                collectionStore,
                selectedIdentity,
                selectedEntry == null ? ItemStack.EMPTY : selectedEntry.displayStack().copy(),
                selectedEntry == null ? "" : selectedEntry.displayName(),
                this::refreshInventoryData,
                hotbarLoadoutController,
                inspectionService,
                historyContextKey()
        ));
    }

    protected void renderActionFeedback(GuiGraphics guiGraphics) {
        if (!actionFeedback.active()) {
            return;
        }

        int statusY = headerStatusY();
        guiGraphics.fill(centerPaneX(), statusY - 1, contentRight(), statusY + 8, 0x50101010);
        String text = font.plainSubstrByWidth(actionFeedback.message().getString(), Math.max(80, Math.round((contentRight() - centerPaneX() - 6) / 0.66F)));
        drawScaledText(guiGraphics, text, centerPaneX() + 2, statusY, actionFeedback.color(), 0.66F);
    }

    protected boolean handleRowActionMenuClick(double mouseX, double mouseY, int button) {
        if (rowActionMenu == null) {
            return false;
        }
        if (button != 0) {
            rowActionMenu = null;
            return false;
        }
        if (rowActionMenu.contains(mouseX, mouseY)) {
            PopupActionMenu menu = rowActionMenu;
            rowActionMenu = null;
            menu.click(mouseX, mouseY);
            return true;
        }
        rowActionMenu = null;
        return false;
    }

    protected void openDeleteCollectionConfirmMenu(String collectionId, String collectionName, int anchorX, int anchorY) {
        rowActionMenu = createRowActionMenu(anchorX, anchorY, List.of(
                new ActionMenuItem(
                        Component.translatable("slot.screen.collections.delete_confirm", collectionName).getString(),
                        () -> deleteCollection(collectionId)
                ),
                new ActionMenuItem(Component.translatable("gui.cancel").getString(), () -> {
                })
        ));
    }

    protected PopupActionMenu createRowActionMenu(int anchorX, int anchorY, List<ActionMenuItem> actions) {
        return new PopupActionMenu(
                anchorX,
                anchorY,
                actions,
                label -> scaledTextWidth(label, ROW_TEXT_SCALE),
                centerPaneX(),
                contentRight(),
                contentTop(),
                panelBottom() - OUTER_MARGIN
        );
    }

    protected void drawActionMenuLabel(GuiGraphics guiGraphics, String label, int x, int y, int maxWidth) {
        drawScaledText(guiGraphics, font.plainSubstrByWidth(label, maxWidth), x, y, 0xF0F0F0, ROW_TEXT_SCALE);
    }

    protected String currentSearchQuery() {
        return searchBox == null ? "" : searchBox.getValue().trim().toLowerCase(Locale.ROOT);
    }

    protected void drawScaledText(GuiGraphics guiGraphics, String text, int x, int y, int color, float scale) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, 1.0F);
        guiGraphics.drawString(font, text, Math.round(x / scale), Math.round(y / scale), color, false);
        guiGraphics.pose().popPose();
    }

    protected int scaledTextWidth(String text, float scale) {
        return (int) Math.ceil(font.width(text) * scale);
    }

    protected Font textFont() {
        return font;
    }

    protected void drawWrapped(GuiGraphics guiGraphics, Component component, int x, int y, int width, int color) {
        int lineY = y;
        for (var line : font.split(component, width)) {
            guiGraphics.drawString(font, line, x, lineY, color, false);
            lineY += 10;
        }
    }

    protected void onCollectionCreated(String collectionId) {
    }

    protected boolean isSearchInputActive() {
        return searchBox != null
                && (searchBox.canConsumeInput() || searchBox.isFocused() || getFocused() == searchBox);
    }

    protected void drawInlineButton(
            GuiGraphics guiGraphics,
            int x,
            int y,
            int width,
            int height,
            String label,
            boolean enabled,
            boolean hovered
    ) {
        int background = !enabled
                ? 0x40262626
                : hovered
                ? 0x80507038
                : 0x60404040;
        guiGraphics.fill(x, y, x + width, y + height, background);
        int textWidth = scaledTextWidth(label, ROW_TEXT_SCALE);
        int textX = x + Math.max(2, (width - textWidth) / 2);
        int textY = y + Math.max(0, (height - Math.round(8 * ROW_TEXT_SCALE)) / 2);
        drawScaledText(guiGraphics, label, textX, textY, enabled ? 0xF0F0F0 : 0x808080, ROW_TEXT_SCALE);
    }

    protected boolean shouldRetainCollectionSection(InventoryViewData.Section section) {
        return section.isCollection()
                && (collectionHasLoadouts(section.collectionId()) || CollectionStore.JUNK_ID.equals(section.collectionId()));
    }

    protected List<InventoryViewData.EntryView> recentMatchingEntries(
            String query,
            List<InventoryViewData.EntryView> entries,
            Predicate<InventoryViewData.EntryView> includeEntry
    ) {
        List<ItemIdentity> recentIdentities = RecentLootTracker.visibleRecentIdentities(
                entries.stream().map(InventoryViewData.EntryView::itemEntry).toList(),
                recentCarriedSourceFilter()
        );
        if (recentIdentities.isEmpty()) {
            return List.of();
        }

        Map<ItemIdentity, InventoryViewData.EntryView> entryByIdentity = new LinkedHashMap<>();
        for (InventoryViewData.EntryView entry : entries) {
            entryByIdentity.putIfAbsent(
                    ItemBehaviorPolicy.normalizeTrackedIdentity(entry.itemEntry().identity()),
                    entry
            );
        }

        List<InventoryViewData.EntryView> matches = new ArrayList<>(recentIdentities.size());
        for (ItemIdentity identity : recentIdentities) {
            InventoryViewData.EntryView entry = entryByIdentity.get(identity);
            if (entry == null) {
                continue;
            }
            if (!query.isBlank() && !entry.searchKey().contains(query)) {
                continue;
            }
            if (!includeEntry.test(entry)) {
                continue;
            }
            matches.add(entry);
        }
        return List.copyOf(matches);
    }

    protected List<InventoryViewData.EntryView> recentMatchingEntries(
            String query,
            List<InventoryViewData.EntryView> entries
    ) {
        return recentMatchingEntries(query, entries, ignored -> true);
    }

    protected Predicate<String> recentCarriedSourceFilter() {
        if (screenContext != null) {
            return screenContext.carriedSourceSet()::contains;
        }
        Set<String> fallbackSourceIds = defaultRecentCarriedSourceIds();
        return fallbackSourceIds::contains;
    }

    protected boolean isCollectionCollapsed(String collectionId) {
        return collectionWorkflow.isCollectionCollapsed(collectionId);
    }

    protected boolean shouldShowCollectionLoadouts(String collectionId, boolean expanded) {
        return collectionHasLoadouts(collectionId)
                && (expanded || collectionWorkflow.pinLoadoutsWhenCollectionCollapsed(collectionId));
    }

    protected void toggleCollectionCollapsed(String collectionId) {
        collectionWorkflow.toggleCollectionCollapsed(collectionId);
        rebuildVisibleEntries();
    }

    protected void togglePinnedLoadoutsWhenCollapsed(String collectionId) {
        collectionWorkflow.togglePinLoadoutsWhenCollectionCollapsed(collectionId);
        rebuildVisibleEntries();
    }

    protected CollectionStockSummary collectionStockSummary(String collectionId) {
        return collectionWorkflow.collectionStockSummary(collectionId, this::ownedCountForCollectionStock);
    }

    protected void captureLoadout(String collectionId) {
        if (collectionId == null || collectionId.isBlank()) {
            return;
        }

        var capturedLoadout = hotbarLoadoutController.captureCurrentLoadout();
        if (capturedLoadout.isEmpty()) {
            return;
        }

        HotbarLoadoutDefinition loadout = collectionWorkflow.createLoadout(
                collectionId,
                nextAutoLoadoutName(collectionId),
                capturedLoadout.slots(),
                capturedLoadout.offhandIdentity()
        );
        showActionFeedback(SlotActionResult.applied(Component.translatable("slot.screen.collections.loadout.saved", loadout.name())));
        refreshInventoryData();
    }

    protected void cycleLoadout(String collectionId, int delta) {
        if (collectionWorkflow.cycleSelectedLoadout(collectionId, delta)) {
            rebuildVisibleEntries();
        }
    }

    protected void applySelectedLoadout(String collectionId) {
        HotbarLoadoutDefinition loadout = selectedLoadout(collectionId);
        if (loadout == null) {
            return;
        }

        applyQuickAccessMutation(() -> hotbarLoadoutController.applyLoadoutMutation(loadout));
    }

    protected boolean applyQuickAccessMutation(Supplier<QuickAccessMutationResult> action) {
        var before = hotbarLoadoutController.captureCurrentLoadout();
        SlotUndoHistory.beginQuickAccessBatch(historyContextKey());
        QuickAccessMutationResult result = QuickAccessMutationResult.NONE;
        boolean completed = false;
        try {
            result = action == null ? QuickAccessMutationResult.NONE : action.get();
            completed = true;
        } finally {
            if (!completed) {
                SlotUndoHistory.cancelQuickAccessBatch(historyContextKey());
            }
        }
        SlotUndoHistory.finishQuickAccessBatch(historyContextKey(), before, result);
        if (result == null || !result.changed()) {
            return false;
        }

        RecentLootTracker.suppressPositiveDeltas();
        requestImmediatePostActionRefresh();
        return true;
    }

    protected void undoLastAction() {
        applyUndoHistoryResult(SlotUndoHistory.undo(undoHistoryContext()), true);
    }

    protected void redoLastAction() {
        applyUndoHistoryResult(SlotUndoHistory.redo(undoHistoryContext()), false);
    }

    protected SlotUndoHistory.ActionContext undoHistoryContext() {
        return new SlotUndoHistory.ActionContext(historyContextKey(), screenContext, hotbarLoadoutController);
    }

    protected boolean primaryModifierDown() {
        if (minecraft == null) {
            return false;
        }
        long window = minecraft.getWindow().getWindow();
        return Screen.hasControlDown()
                || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SUPER)
                || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SUPER);
    }

    protected void updateSelectedLoadout(String collectionId) {
        HotbarLoadoutDefinition loadout = selectedLoadout(collectionId);
        if (loadout == null) {
            return;
        }

        var capturedLoadout = hotbarLoadoutController.captureCurrentLoadout();
        if (capturedLoadout.isEmpty()) {
            return;
        }

        HotbarLoadoutDefinition updatedLoadout = collectionWorkflow.updateSelectedLoadout(
                collectionId,
                capturedLoadout.slots(),
                capturedLoadout.offhandIdentity()
        );
        if (updatedLoadout != null) {
            showActionFeedback(SlotActionResult.applied(Component.translatable("slot.screen.collections.loadout.updated", updatedLoadout.name())));
            refreshInventoryData();
        }
    }

    protected ItemStack previewStack(ItemIdentity identity, List<InventoryViewData.EntryView> entries) {
        for (InventoryViewData.EntryView entry : entries) {
            if (ItemBehaviorPolicy.matchesTrackedIdentity(entry.itemEntry().identity(), identity)) {
                return entry.displayStack();
            }
        }
        return ItemBehaviorPolicy.approximateDisplayStack(identity);
    }

    protected LocalPlayer currentPlayer() {
        return minecraft == null ? null : minecraft.player;
    }

    protected static int hotbarKeyIndex(int keyCode) {
        if (keyCode >= GLFW.GLFW_KEY_1 && keyCode <= GLFW.GLFW_KEY_9) {
            return keyCode - GLFW.GLFW_KEY_1;
        }
        if (keyCode >= GLFW.GLFW_KEY_KP_1 && keyCode <= GLFW.GLFW_KEY_KP_9) {
            return keyCode - GLFW.GLFW_KEY_KP_1;
        }
        return -1;
    }

    protected void renderScaledItem(GuiGraphics guiGraphics, ItemStack stack, int x, int y, float scale) {
        if (stack.isEmpty()) {
            return;
        }
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0.0F);
        guiGraphics.pose().scale(scale, scale, 1.0F);
        guiGraphics.renderItem(stack, 0, 0);
        guiGraphics.pose().popPose();
    }

    protected int rowTextY(int y) {
        return y + 4;
    }

    protected int rowItemY(int y, int rowHeight, int rowItemSize) {
        return y + Math.max(0, (rowHeight - rowItemSize) / 2);
    }

    protected String paddedRowCountText(int count) {
        int clamped = Math.max(0, Math.min(9999, count));
        String digits = Integer.toString(clamped);
        return " ".repeat(Math.max(0, 4 - digits.length())) + digits;
    }

    protected int rowSlotY(int y, int rowHeight, int slotSize) {
        return y + Math.max(0, (rowHeight - slotSize) / 2);
    }

    protected static int quickAccessSlotOffset(int slotIndex, int slotSize, int slotGap, int offhandGap) {
        int clampedIndex = Math.max(0, slotIndex);
        int offset = clampedIndex * (slotSize + slotGap);
        if (clampedIndex >= HotbarLoadoutDefinition.OFFHAND_SLOT_INDEX) {
            offset += offhandGap;
        }
        return offset;
    }

    protected static int quickAccessSpanWidth(int slotSize, int slotGap, int offhandGap) {
        return quickAccessSlotOffset(HotbarLoadoutDefinition.QUICK_ACCESS_SLOT_COUNT - 1, slotSize, slotGap, offhandGap) + slotSize;
    }

    protected static String quickAccessSlotLabel(int slotIndex) {
        return slotIndex == HotbarLoadoutDefinition.OFFHAND_SLOT_INDEX ? "F" : Integer.toString(slotIndex + 1);
    }

    protected int loadoutPreviewSpanWidth(int visiblePreviewSlots) {
        if (visiblePreviewSlots <= 0) {
            return 0;
        }
        return quickAccessSlotOffset(visiblePreviewSlots - 1, LOADOUT_SLOT_SIZE, LOADOUT_SLOT_GAP, LOADOUT_OFFHAND_GAP) + LOADOUT_SLOT_SIZE;
    }

    protected abstract void rebuildVisibleEntries();

    protected abstract void refreshInventoryData();

    protected abstract Set<String> defaultRecentCarriedSourceIds();

    protected abstract int ownedCountForCollectionStock(ItemIdentity identity);

    protected abstract void applyUndoHistoryResult(SlotUndoHistory.ApplyResult result, boolean undo);

    protected abstract void schedulePostActionRefresh();

    protected abstract void clearPendingPostActionRefresh();

    protected AbstractContainerMenu historyMenu() {
        return menu;
    }

    protected String historyContextKey() {
        return historyContextKey;
    }

    protected abstract int centerPaneX();

    protected abstract int centerPaneWidth();

    protected abstract int headerStatusY();

    protected abstract int contentRight();

    protected abstract int contentTop();

    protected abstract int panelTop();

    protected abstract int panelHeight();

    protected abstract int panelBottom();

    public record InlineButton(int x, int y, int width, int height, String label, boolean enabled) {
        public boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }
    }

    public record CollectionPromptLayout(
            int x,
            int y,
            int inputX,
            int inputY,
            int inputWidth,
            InlineButton createButton,
            InlineButton cancelButton
    ) {
    }
}
