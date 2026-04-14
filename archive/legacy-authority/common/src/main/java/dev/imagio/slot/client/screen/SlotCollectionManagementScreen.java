package dev.imagio.slot.client.screen;

import dev.imagio.slot.client.collection.CollectionDefinition;
import dev.imagio.slot.client.collection.CollectionStore;
import dev.imagio.slot.client.collection.HotbarLoadoutDefinition;
import dev.imagio.slot.client.collection.HotbarLoadoutSlot;
import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.workflow.InspectionService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class SlotCollectionManagementScreen extends Screen {
    private static final long PENDING_REQUEST_TIMEOUT_NANOS = 60_000_000_000L;
    private static final int ROW_HEIGHT = 20;
    private static final int PANEL_WIDTH = 620;
    private static final int PANEL_HEIGHT = 292;
    private static final int COLUMN_WIDTH = 290;
    private static final int COLUMN_GAP = 16;

    private final Screen parentScreen;
    private final CollectionStore collectionStore;
    private final ItemIdentity selectedItemIdentity;
    private final ItemStack selectedItemStack;
    private final String selectedItemName;
    private final Runnable onChanged;
    private final QuickAccessService hotbarLoadoutController;
    private final InspectionService inspectionService;
    private final String historyContextKey;
    private final SlotActionFeedbackState actionFeedback = new SlotActionFeedbackState();
    private final Map<String, Long> pendingQuickAccessRequestIds = new LinkedHashMap<>();

    private CollectionList collectionList;
    private LoadoutList loadoutList;
    private EditBox nameBox;
    private EditBox loadoutNameBox;
    private Button createButton;
    private Button renameButton;
    private Button deleteButton;
    private Button membershipButton;
    private Button captureLoadoutButton;
    private Button renameLoadoutButton;
    private Button deleteLoadoutButton;
    private Button applyLoadoutButton;
    private Button updateLoadoutButton;
    private Button loadoutHotkeyButton;
    private Button inspectItemButton;

    private String selectedCollectionId;
    private String selectedLoadoutId;

    public SlotCollectionManagementScreen(
            Screen parentScreen,
            CollectionStore collectionStore,
            ItemIdentity selectedItemIdentity,
            ItemStack selectedItemStack,
            String selectedItemName,
            Runnable onChanged
    ) {
        this(parentScreen, collectionStore, selectedItemIdentity, selectedItemStack, selectedItemName, onChanged, null, null);
    }

    public SlotCollectionManagementScreen(
            Screen parentScreen,
            CollectionStore collectionStore,
            ItemIdentity selectedItemIdentity,
            ItemStack selectedItemStack,
            String selectedItemName,
            Runnable onChanged,
            QuickAccessService hotbarLoadoutController
    ) {
        this(parentScreen, collectionStore, selectedItemIdentity, selectedItemStack, selectedItemName, onChanged, hotbarLoadoutController, null);
    }

    public SlotCollectionManagementScreen(
            Screen parentScreen,
            CollectionStore collectionStore,
            ItemIdentity selectedItemIdentity,
            ItemStack selectedItemStack,
            String selectedItemName,
            Runnable onChanged,
            QuickAccessService hotbarLoadoutController,
            InspectionService inspectionService
    ) {
        this(
                parentScreen,
                collectionStore,
                selectedItemIdentity,
                selectedItemStack,
                selectedItemName,
                onChanged,
                hotbarLoadoutController,
                inspectionService,
                ""
        );
    }

    public SlotCollectionManagementScreen(
            Screen parentScreen,
            CollectionStore collectionStore,
            ItemIdentity selectedItemIdentity,
            ItemStack selectedItemStack,
            String selectedItemName,
            Runnable onChanged,
            QuickAccessService hotbarLoadoutController,
            InspectionService inspectionService,
            String historyContextKey
    ) {
        super(Component.translatable("slot.screen.collections.title"));
        this.parentScreen = parentScreen;
        this.collectionStore = collectionStore;
        this.selectedItemIdentity = selectedItemIdentity;
        this.selectedItemStack = selectedItemStack == null ? ItemStack.EMPTY : selectedItemStack.copy();
        this.selectedItemName = selectedItemName == null ? "" : selectedItemName;
        this.onChanged = onChanged;
        this.hotbarLoadoutController = hotbarLoadoutController;
        this.inspectionService = inspectionService == null ? new InspectionService(collectionStore) : inspectionService;
        this.historyContextKey = historyContextKey == null ? "" : historyContextKey;
    }

    @Override
    protected void init() {
        super.init();
        if (!historyContextKey.isBlank()) {
            SlotUndoHistory.bindContext(historyContextKey);
        }

        int panelLeft = panelLeft();
        int panelTop = panelTop();
        int listTop = panelTop + 48;
        int listHeight = 124;
        int leftColumnX = panelLeft + 12;
        int rightColumnX = leftColumnX + COLUMN_WIDTH + COLUMN_GAP;

        nameBox = new EditBox(font, leftColumnX, panelTop + 186, 184, 18, Component.translatable("slot.screen.collections.name"));
        nameBox.setHint(Component.translatable("slot.screen.collections.name_hint"));
        nameBox.setResponder(ignored -> updateButtons());
        addRenderableWidget(nameBox);

        loadoutNameBox = new EditBox(font, rightColumnX, panelTop + 186, 184, 18, Component.translatable("slot.screen.collections.loadout_name"));
        loadoutNameBox.setHint(Component.translatable("slot.screen.collections.loadout_name_hint"));
        loadoutNameBox.setResponder(ignored -> updateButtons());
        addRenderableWidget(loadoutNameBox);

        collectionList = new CollectionList(Minecraft.getInstance(), COLUMN_WIDTH, listHeight, listTop, leftColumnX);
        loadoutList = new LoadoutList(Minecraft.getInstance(), COLUMN_WIDTH, listHeight, listTop, rightColumnX);
        addWidget(collectionList);
        addWidget(loadoutList);

        createButton = addRenderableWidget(Button.builder(Component.translatable("slot.screen.collections.create"), button -> createCollection())
                .bounds(leftColumnX + 192, panelTop + 186, 98, 18)
                .build());
        renameButton = addRenderableWidget(Button.builder(Component.translatable("slot.screen.collections.rename"), button -> renameCollection())
                .bounds(leftColumnX, panelTop + 208, 92, 18)
                .build());
        deleteButton = addRenderableWidget(Button.builder(Component.translatable("slot.screen.collections.delete"), button -> deleteCollection())
                .bounds(leftColumnX + 98, panelTop + 208, 92, 18)
                .build());
        membershipButton = addRenderableWidget(Button.builder(Component.empty(), button -> toggleMembership())
                .bounds(leftColumnX + 196, panelTop + 208, 94, 18)
                .build());
        inspectItemButton = addRenderableWidget(Button.builder(Component.translatable("slot.screen.inspect.button"), button -> openItemInspection())
                .bounds(leftColumnX, panelTop + 230, COLUMN_WIDTH, 18)
                .build());

        captureLoadoutButton = addRenderableWidget(Button.builder(Component.translatable("slot.screen.collections.capture_loadout"), button -> captureLoadout())
                .bounds(rightColumnX + 192, panelTop + 186, 98, 18)
                .build());
        renameLoadoutButton = addRenderableWidget(Button.builder(Component.translatable("slot.screen.collections.rename"), button -> renameLoadout())
                .bounds(rightColumnX, panelTop + 208, 92, 18)
                .build());
        deleteLoadoutButton = addRenderableWidget(Button.builder(Component.translatable("slot.screen.collections.delete"), button -> deleteLoadout())
                .bounds(rightColumnX + 98, panelTop + 208, 92, 18)
                .build());
        applyLoadoutButton = addRenderableWidget(Button.builder(Component.translatable("slot.screen.collections.apply_loadout"), button -> applyLoadout())
                .bounds(rightColumnX + 196, panelTop + 208, 94, 18)
                .build());
        updateLoadoutButton = addRenderableWidget(Button.builder(Component.translatable("slot.screen.collections.inline.update"), button -> updateLoadout())
                .bounds(rightColumnX, panelTop + 230, 92, 18)
                .build());
        loadoutHotkeyButton = addRenderableWidget(Button.builder(Component.empty(), button -> cycleLoadoutHotkey())
                .bounds(rightColumnX + 98, panelTop + 230, 192, 18)
                .build());

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(panelLeft + 12, panelTop + 252, PANEL_WIDTH - 24, 20)
                .build());

        rebuildCollections();
        setInitialFocus(nameBox);
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(parentScreen);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        actionFeedback.tick();
        if (hotbarLoadoutController != null) {
            hotbarLoadoutController.tick();
        }
        applyConfirmedActionOutcome();
        updateButtons();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderTransparentBackground(guiGraphics);

        int panelLeft = panelLeft();
        int panelTop = panelTop();
        int leftColumnX = panelLeft + 12;
        int rightColumnX = leftColumnX + COLUMN_WIDTH + COLUMN_GAP;

        guiGraphics.fill(panelLeft, panelTop, panelLeft + PANEL_WIDTH, panelTop + PANEL_HEIGHT, 0xC0101010);
        guiGraphics.drawCenteredString(font, title, width / 2, panelTop + 8, 0xFFFFFF);

        Component itemLabel = selectedItemIdentity == null
                ? Component.translatable("slot.screen.collections.selected_item.none")
                : Component.translatable("slot.screen.collections.selected_item.value", selectedItemName);
        guiGraphics.drawString(font, itemLabel, panelLeft + 12, panelTop + 22, 0xB0B0B0, false);

        guiGraphics.drawString(font, Component.translatable("slot.screen.collections.column.collections"), leftColumnX, panelTop + 36, 0xE0E0E0, false);
        guiGraphics.drawString(font, Component.translatable("slot.screen.collections.column.loadouts"), rightColumnX, panelTop + 36, 0xE0E0E0, false);
        if (actionFeedback.active()) {
            String feedback = font.plainSubstrByWidth(actionFeedback.message().getString(), PANEL_WIDTH - 24);
            guiGraphics.drawCenteredString(font, feedback, width / 2, panelTop + 22, actionFeedback.color());
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
        collectionList.render(guiGraphics, mouseX, mouseY, partialTick);
        loadoutList.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private int panelLeft() {
        return width / 2 - PANEL_WIDTH / 2;
    }

    private int panelTop() {
        return 28;
    }

    private void createCollection() {
        try {
            CollectionDefinition created = collectionStore.createCollection(nameBox.getValue());
            selectedCollectionId = created.id();
            selectedLoadoutId = null;
            nameBox.setValue("");
            onChanged.run();
            rebuildCollections();
        } catch (IllegalArgumentException ignored) {
        }
    }

    private void renameCollection() {
        if (selectedCollectionId == null) {
            return;
        }

        try {
            collectionStore.renameCollection(selectedCollectionId, nameBox.getValue());
            onChanged.run();
            rebuildCollections();
        } catch (IllegalArgumentException ignored) {
        }
    }

    private void deleteCollection() {
        if (selectedCollectionId == null) {
            return;
        }

        try {
            collectionStore.deleteCollection(selectedCollectionId);
            selectedCollectionId = null;
            selectedLoadoutId = null;
            onChanged.run();
            rebuildCollections();
        } catch (IllegalArgumentException ignored) {
        }
    }

    private void toggleMembership() {
        if (selectedItemIdentity == null || selectedCollectionId == null) {
            return;
        }

        if (collectionStore.collectionsFor(selectedItemIdentity).contains(selectedCollectionId)) {
            collectionStore.removeFromCollection(selectedCollectionId, selectedItemIdentity);
        } else {
            collectionStore.addToCollection(selectedCollectionId, selectedItemIdentity);
        }

        onChanged.run();
        rebuildCollections();
    }

    private void captureLoadout() {
        if (selectedCollectionId == null || hotbarLoadoutController == null || hasPendingQuickAccessWorkflow()) {
            return;
        }

        try {
            var capturedLoadout = hotbarLoadoutController.captureCurrentLoadout();
            if (capturedLoadout.isEmpty()) {
                return;
            }
            HotbarLoadoutDefinition loadout = collectionStore.createHotbarLoadout(
                    selectedCollectionId,
                    loadoutNameBox.getValue(),
                    capturedLoadout.slots(),
                    capturedLoadout.offhandIdentity()
            );
            selectedLoadoutId = loadout.id();
            onChanged.run();
            rebuildCollections();
        } catch (IllegalArgumentException ignored) {
        }
    }

    private void renameLoadout() {
        if (selectedCollectionId == null || selectedLoadoutId == null) {
            return;
        }

        try {
            collectionStore.renameHotbarLoadout(selectedCollectionId, selectedLoadoutId, loadoutNameBox.getValue());
            onChanged.run();
            rebuildCollections();
        } catch (IllegalArgumentException ignored) {
        }
    }

    private void deleteLoadout() {
        if (selectedCollectionId == null || selectedLoadoutId == null) {
            return;
        }

        try {
            collectionStore.deleteHotbarLoadout(selectedCollectionId, selectedLoadoutId);
            selectedLoadoutId = null;
            onChanged.run();
            rebuildCollections();
        } catch (IllegalArgumentException ignored) {
        }
    }

    private void applyLoadout() {
        if (selectedCollectionId == null || selectedLoadoutId == null || hotbarLoadoutController == null) {
            return;
        }

        HotbarLoadoutDefinition loadout = selectedLoadout();
        QuickAccessMutationResult result = loadout == null
                ? QuickAccessMutationResult.NONE
                : applyQuickAccessMutation(() -> hotbarLoadoutController.applyLoadoutMutation(loadout));
        if (result.changed() && !result.transferSyncExpected()) {
            onChanged.run();
        }
    }

    private QuickAccessMutationResult applyQuickAccessMutation(Supplier<QuickAccessMutationResult> action) {
        if (hotbarLoadoutController == null) {
            return QuickAccessMutationResult.NONE;
        }

        var before = hotbarLoadoutController.captureCurrentLoadout();
        QuickAccessMutationResult result = action == null ? QuickAccessMutationResult.NONE : action.get();
        if (!historyContextKey.isBlank()) {
            SlotUndoHistory.recordQuickAccessMutation(historyContextKey, before, result);
        }
        if (result == null || !result.changed()) {
            return QuickAccessMutationResult.NONE;
        }

        if (result.transferSyncExpected()) {
            long now = System.nanoTime();
            for (QuickAccessMutationResult.RequestedChange change : result.pendingChanges()) {
                if (change == null || change.requestId() == null || !change.requestId().present()) {
                    continue;
                }
                pendingQuickAccessRequestIds.put(change.requestId().value(), now);
            }
        }

        actionFeedback.show(result.transferSyncExpected()
                ? SlotActionResult.requested(Component.translatable("slot.screen.action.outcome.generic.requested"))
                : SlotActionResult.applied(Component.translatable("slot.screen.action.outcome.generic.applied")));
        RecentLootTracker.suppressPositiveDeltas();
        updateButtons();
        return result;
    }

    private void applyConfirmedActionOutcome() {
        if (historyContextKey.isBlank()) {
            return;
        }
        pruneExpiredPendingQuickAccessRequests();
        if (pendingQuickAccessRequestIds.isEmpty()) {
            return;
        }

        List<SlotActionOutcomeState.PublishedOutcome> outcomes =
                SlotActionOutcomeState.pollMatching(historyContextKey, pendingQuickAccessRequestIds.keySet());
        if (outcomes.isEmpty()) {
            return;
        }

        for (SlotActionOutcomeState.PublishedOutcome outcome : outcomes) {
            if (outcome == null) {
                continue;
            }
            pendingQuickAccessRequestIds.remove(outcome.requestId());
        }

        InventoryActionOrchestrator.OutcomeSummary summary = InventoryActionOrchestrator.summarizeOutcomes(outcomes);
        if (summary.feedback().visible()) {
            actionFeedback.show(summary.feedback());
        }
        if (summary.anySuccessful()) {
            onChanged.run();
        }
        updateButtons();
    }

    private void pruneExpiredPendingQuickAccessRequests() {
        if (pendingQuickAccessRequestIds.isEmpty()) {
            return;
        }

        long now = System.nanoTime();
        pendingQuickAccessRequestIds.entrySet().removeIf(entry -> entry == null
                || entry.getValue() == null
                || now - entry.getValue() > PENDING_REQUEST_TIMEOUT_NANOS);
    }

    private void updateLoadout() {
        if (selectedCollectionId == null
                || selectedLoadoutId == null
                || hotbarLoadoutController == null
                || hasPendingQuickAccessWorkflow()) {
            return;
        }

        HotbarLoadoutDefinition loadout = selectedLoadout();
        if (loadout == null) {
            return;
        }

        try {
            var capturedLoadout = hotbarLoadoutController.captureCurrentLoadout();
            if (capturedLoadout.isEmpty()) {
                return;
            }
            collectionStore.updateHotbarLoadout(
                    selectedCollectionId,
                    loadout.id(),
                    capturedLoadout.slots(),
                    capturedLoadout.offhandIdentity()
            );
            onChanged.run();
            rebuildLoadouts();
        } catch (IllegalArgumentException ignored) {
        }
    }

    private void cycleLoadoutHotkey() {
        if (selectedCollectionId == null || selectedLoadoutId == null) {
            return;
        }

        HotbarLoadoutDefinition loadout = selectedLoadout();
        if (loadout == null) {
            return;
        }

        Integer nextHotkey = loadout.hotkeySlot() == null
                ? 0
                : loadout.hotkeySlot() >= 8 ? null : loadout.hotkeySlot() + 1;
        collectionStore.setHotbarLoadoutHotkey(selectedCollectionId, loadout.id(), nextHotkey);
        onChanged.run();
        rebuildLoadouts();
    }

    private void openItemInspection() {
        if (minecraft == null || selectedItemIdentity == null) {
            return;
        }

        minecraft.setScreen(new SlotItemInspectionScreen(this, inspectionService.inspect(selectedItemIdentity, selectedItemStack, selectedItemName)));
    }

    private void rebuildCollections() {
        if (collectionList == null) {
            return;
        }

        List<CollectionDefinition> collections = List.copyOf(collectionStore.allCollections());
        collectionList.setCollections(collections);
        if (selectedCollectionId != null) {
            CollectionRowEntry selected = collectionList.findById(selectedCollectionId);
            collectionList.setSelected(selected);
            if (selected == null) {
                selectedCollectionId = null;
            }
        }

        rebuildLoadouts();
        updateButtons();
    }

    private void rebuildLoadouts() {
        if (loadoutList == null) {
            return;
        }

        if (selectedCollectionId == null) {
            loadoutList.setLoadouts(List.of());
            selectedLoadoutId = null;
            updateButtons();
            return;
        }

        List<HotbarLoadoutDefinition> loadouts = collectionStore.loadoutsFor(selectedCollectionId);
        loadoutList.setLoadouts(loadouts);
        if (selectedLoadoutId != null) {
            LoadoutRowEntry selected = loadoutList.findById(selectedLoadoutId);
            loadoutList.setSelected(selected);
            if (selected == null) {
                selectedLoadoutId = null;
            }
        }
        updateButtons();
    }

    private void updateButtons() {
        CollectionDefinition selectedCollection = selectedCollection();
        HotbarLoadoutDefinition selectedLoadout = selectedLoadout();
        boolean hasSelectedCollection = selectedCollection != null;
        boolean hasSelectedUserCollection = hasSelectedCollection && !selectedCollection.builtIn();
        boolean hasSelectedItem = selectedItemIdentity != null && hasSelectedCollection;
        boolean hasSelectedLoadout = selectedLoadout != null;
        boolean quickAccessPending = hasPendingQuickAccessWorkflow();
        boolean selectedLoadoutPending = hasSelectedLoadout && hasPendingTargets(selectedLoadout);

        createButton.active = !nameBox.getValue().trim().isEmpty();
        renameButton.active = hasSelectedUserCollection && !nameBox.getValue().trim().isEmpty();
        deleteButton.active = hasSelectedUserCollection;
        membershipButton.active = hasSelectedItem;
        membershipButton.setMessage(hasSelectedItem
                ? Component.translatable(
                collectionStore.collectionsFor(selectedItemIdentity).contains(selectedCollection.id())
                        ? "slot.screen.collections.remove_selected"
                        : "slot.screen.collections.add_selected"
                )
                : Component.translatable("slot.screen.collections.add_selected"));
        inspectItemButton.active = selectedItemIdentity != null;

        captureLoadoutButton.active = hasSelectedCollection
                && hotbarLoadoutController != null
                && !loadoutNameBox.getValue().trim().isEmpty()
                && !quickAccessPending;
        renameLoadoutButton.active = hasSelectedLoadout && !loadoutNameBox.getValue().trim().isEmpty();
        deleteLoadoutButton.active = hasSelectedLoadout;
        applyLoadoutButton.active = hasSelectedLoadout
                && hotbarLoadoutController != null
                && !quickAccessPending
                && !selectedLoadoutPending;
        updateLoadoutButton.active = hasSelectedLoadout && hotbarLoadoutController != null && !quickAccessPending;
        loadoutHotkeyButton.active = hasSelectedLoadout;
        loadoutHotkeyButton.setMessage(Component.translatable("slot.screen.collections.hotkey.button", selectedLoadout == null ? "-" : selectedLoadout.hotkeyIndicator()));
    }

    private boolean hasPendingTargets(HotbarLoadoutDefinition loadout) {
        if (loadout == null) {
            return false;
        }
        if (loadout.offhandIdentity() != null
                && QuickAccessPendingState.isPendingTarget(HotbarLoadoutDefinition.OFFHAND_SLOT_INDEX)) {
            return true;
        }
        for (HotbarLoadoutSlot slot : loadout.slots()) {
            if (slot != null && QuickAccessPendingState.isPendingTarget(slot.slotIndex())) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasPendingQuickAccessWorkflow() {
        return QuickAccessPendingState.hasPendingTargets()
                || QuickAccessFollowUpState.hasPendingActions();
    }

    private CollectionDefinition selectedCollection() {
        if (selectedCollectionId == null) {
            return null;
        }

        return collectionStore.allCollections().stream()
                .filter(collection -> collection.id().equals(selectedCollectionId))
                .findFirst()
                .orElse(null);
    }

    private HotbarLoadoutDefinition selectedLoadout() {
        if (selectedCollectionId == null || selectedLoadoutId == null) {
            return null;
        }

        return collectionStore.loadoutsFor(selectedCollectionId).stream()
                .filter(loadout -> loadout.id().equals(selectedLoadoutId))
                .findFirst()
                .orElse(null);
    }

    private final class CollectionRowEntry extends ObjectSelectionList.Entry<CollectionRowEntry> {
        private final CollectionDefinition collection;

        private CollectionRowEntry(CollectionDefinition collection) {
            this.collection = collection;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick) {
            boolean containsSelectedItem = selectedItemIdentity != null && collectionStore.collectionsFor(selectedItemIdentity).contains(collection.id());
            String prefix = containsSelectedItem ? "[x] " : "[ ] ";
            int color = collection.builtIn() ? 0xE6D588 : 0xE0E0E0;
            guiGraphics.drawString(font, prefix + collection.name(), x + 6, y + 6, color, false);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button != 0) {
                return false;
            }

            selectedCollectionId = collection.id();
            selectedLoadoutId = null;
            collectionList.setSelected(this);
            if (!collection.builtIn()) {
                nameBox.setValue(collection.name());
            } else {
                nameBox.setValue("");
            }
            loadoutNameBox.setValue("");
            rebuildLoadouts();
            updateButtons();
            return true;
        }

        @Override
        public Component getNarration() {
            return Component.literal(collection.name());
        }
    }

    private final class LoadoutRowEntry extends ObjectSelectionList.Entry<LoadoutRowEntry> {
        private final HotbarLoadoutDefinition loadout;

        private LoadoutRowEntry(HotbarLoadoutDefinition loadout) {
            this.loadout = loadout;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick) {
            String label = loadout.name()
                    + " [" + loadout.hotkeyIndicator() + "]"
                    + (loadout.offhandIdentity() == null ? "" : " +Off")
                    + " (" + loadout.configuredSlotCount() + ")";
            guiGraphics.drawString(font, label, x + 6, y + 6, 0xE0E0E0, false);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button != 0) {
                return false;
            }

            selectedLoadoutId = loadout.id();
            loadoutList.setSelected(this);
            loadoutNameBox.setValue(loadout.name());
            updateButtons();
            return true;
        }

        @Override
        public Component getNarration() {
            return Component.literal(loadout.name());
        }
    }

    private final class CollectionList extends ObjectSelectionList<CollectionRowEntry> {
        private CollectionList(Minecraft minecraft, int width, int height, int top, int left) {
            super(minecraft, width, height, top, ROW_HEIGHT);
            setPosition(left, top);
        }

        public void setCollections(List<CollectionDefinition> collections) {
            clearEntries();
            for (CollectionDefinition collection : collections) {
                addEntry(new CollectionRowEntry(collection));
            }
            setScrollAmount(Math.min(getScrollAmount(), getMaxScroll()));
        }

        public CollectionRowEntry findById(String collectionId) {
            for (CollectionRowEntry entry : children()) {
                if (entry.collection.id().equals(collectionId)) {
                    return entry;
                }
            }
            return null;
        }

        @Override
        public int getRowWidth() {
            return getWidth() - 12;
        }

        @Override
        protected int getScrollbarPosition() {
            return getX() + getWidth() - 6;
        }

        @Override
        protected void renderListBackground(GuiGraphics guiGraphics) {
        }

        @Override
        protected void renderListSeparators(GuiGraphics guiGraphics) {
        }

        @Override
        protected void renderSelection(GuiGraphics guiGraphics, int y, int width, int height, int outerColor, int innerColor) {
            guiGraphics.fill(getRowLeft(), y, getRowRight(), y + height, 0x664B7F35);
        }
    }

    private final class LoadoutList extends ObjectSelectionList<LoadoutRowEntry> {
        private LoadoutList(Minecraft minecraft, int width, int height, int top, int left) {
            super(minecraft, width, height, top, ROW_HEIGHT);
            setPosition(left, top);
        }

        public void setLoadouts(List<HotbarLoadoutDefinition> loadouts) {
            clearEntries();
            for (HotbarLoadoutDefinition loadout : loadouts) {
                addEntry(new LoadoutRowEntry(loadout));
            }
            setScrollAmount(Math.min(getScrollAmount(), getMaxScroll()));
        }

        public LoadoutRowEntry findById(String loadoutId) {
            for (LoadoutRowEntry entry : children()) {
                if (entry.loadout.id().equals(loadoutId)) {
                    return entry;
                }
            }
            return null;
        }

        @Override
        public int getRowWidth() {
            return getWidth() - 12;
        }

        @Override
        protected int getScrollbarPosition() {
            return getX() + getWidth() - 6;
        }

        @Override
        protected void renderListBackground(GuiGraphics guiGraphics) {
        }

        @Override
        protected void renderListSeparators(GuiGraphics guiGraphics) {
        }

        @Override
        protected void renderSelection(GuiGraphics guiGraphics, int y, int width, int height, int outerColor, int innerColor) {
            guiGraphics.fill(getRowLeft(), y, getRowRight(), y + height, 0x664B7F35);
        }
    }
}
