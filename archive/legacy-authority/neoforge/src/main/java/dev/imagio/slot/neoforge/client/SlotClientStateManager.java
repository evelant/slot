package dev.imagio.slot.neoforge.client;

import dev.imagio.slot.client.collection.CollectionStore;
import dev.imagio.slot.client.collection.CollectionViewStateController;
import dev.imagio.slot.neoforge.config.SlotClientConfig;
import dev.imagio.slot.workflow.InspectionService;
import dev.imagio.slot.workflow.SearchWorkflowService;
import dev.imagio.slot.workflow.SettingsService;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class SlotClientStateManager implements SettingsService, CollectionViewStateController {
    private final CollectionStore collectionStore = new CollectionStore();
    private final SearchWorkflowService searchWorkflow = new SearchWorkflowService();
    private final InspectionService inspectionService = new InspectionService(collectionStore);
    private final ClientStateStore stateStore;
    private final Map<String, CollectionViewState> collectionViewStates = new LinkedHashMap<>();

    private boolean enabled;
    private boolean replacePlayerInventory;
    private boolean replaceChestLikeStorage;
    private boolean syncSearchWithEmi = true;
    private boolean suppressSave;

    public SlotClientStateManager(Path gameDirectory, SlotClientConfig.Client defaults) {
        stateStore = new ClientStateStore(gameDirectory.resolve("config").resolve("slot-client-state.json"));
        enabled = defaults.enabled.get();
        replacePlayerInventory = defaults.replacePlayerInventory.get();
        replaceChestLikeStorage = defaults.replaceChestLikeStorage.get();
        collectionStore.setChangeListener(this::save);
        load();
    }

    public CollectionStore collectionStore() {
        return collectionStore;
    }

    public SearchWorkflowService searchWorkflow() {
        return searchWorkflow;
    }

    public InspectionService inspectionService() {
        return inspectionService;
    }

    @Override
    public boolean slotEnabled() {
        return enabled;
    }

    @Override
    public void setSlotEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            save();
        }
    }

    @Override
    public boolean replacePlayerInventory() {
        return replacePlayerInventory;
    }

    @Override
    public void setReplacePlayerInventory(boolean enabled) {
        if (replacePlayerInventory != enabled) {
            replacePlayerInventory = enabled;
            save();
        }
    }

    @Override
    public boolean replaceChestLikeStorage() {
        return replaceChestLikeStorage;
    }

    @Override
    public void setReplaceChestLikeStorage(boolean enabled) {
        if (replaceChestLikeStorage != enabled) {
            replaceChestLikeStorage = enabled;
            save();
        }
    }

    @Override
    public boolean syncSearchWithEmi() {
        return syncSearchWithEmi;
    }

    @Override
    public void setSyncSearchWithEmi(boolean enabled) {
        if (syncSearchWithEmi != enabled) {
            syncSearchWithEmi = enabled;
            save();
        }
    }

    private void load() {
        suppressSave = true;
        try {
            ClientStateStore.ClientStateSnapshot state = stateStore.load(new ClientStateStore.ClientStateDefaults(
                    enabled,
                    replacePlayerInventory,
                    replaceChestLikeStorage,
                    syncSearchWithEmi
            ));

            enabled = state.preferences().enabled();
            replacePlayerInventory = state.preferences().replacePlayerInventory();
            replaceChestLikeStorage = state.preferences().replaceChestLikeStorage();
            syncSearchWithEmi = state.preferences().syncSearchWithEmi();

            collectionViewStates.clear();
            state.collectionViewStates().forEach((collectionId, viewState) -> collectionViewStates.put(
                    collectionId,
                    new CollectionViewState(viewState.collapsed(), viewState.pinLoadoutsWhenCollapsed())
            ));
            collectionStore.replaceWith(state.collectionSnapshot());
        } finally {
            suppressSave = false;
        }
    }

    private void save() {
        if (suppressSave) {
            return;
        }

        Map<String, ClientStateStore.CollectionViewStateData> persistedViewStates = new LinkedHashMap<>();
        collectionViewStates.forEach((collectionId, viewState) -> persistedViewStates.put(
                collectionId,
                new ClientStateStore.CollectionViewStateData(
                        viewState.collapsed(),
                        viewState.pinLoadoutsWhenCollapsed()
                )
        ));

        stateStore.save(new ClientStateStore.ClientStateSnapshot(
                new ClientStateStore.ClientPreferences(
                        enabled,
                        replacePlayerInventory,
                        replaceChestLikeStorage,
                        syncSearchWithEmi
                ),
                collectionStore.snapshot(),
                persistedViewStates
        ));
    }

    @Override
    public boolean isCollectionCollapsed(String collectionId) {
        return collectionViewStates.getOrDefault(collectionId, defaultCollectionViewState()).collapsed();
    }

    @Override
    public void setCollectionCollapsed(String collectionId, boolean collapsed) {
        updateCollectionViewState(collectionId, collapsed, pinLoadoutsWhenCollectionCollapsed(collectionId));
    }

    @Override
    public boolean pinLoadoutsWhenCollectionCollapsed(String collectionId) {
        return collectionViewStates.getOrDefault(collectionId, defaultCollectionViewState()).pinLoadoutsWhenCollapsed();
    }

    @Override
    public void setPinLoadoutsWhenCollectionCollapsed(String collectionId, boolean pinned) {
        updateCollectionViewState(collectionId, isCollectionCollapsed(collectionId), pinned);
    }

    private void updateCollectionViewState(String collectionId, boolean collapsed, boolean pinLoadoutsWhenCollapsed) {
        if (collectionId == null || collectionId.isBlank()) {
            return;
        }

        CollectionViewState nextState = new CollectionViewState(collapsed, pinLoadoutsWhenCollapsed);
        CollectionViewState previous = collectionViewStates.get(collectionId);
        if (nextState.equals(defaultCollectionViewState())) {
            if (collectionViewStates.remove(collectionId) != null) {
                save();
            }
            return;
        }
        if (!nextState.equals(previous)) {
            collectionViewStates.put(collectionId, nextState);
            save();
        }
    }

    private static CollectionViewState defaultCollectionViewState() {
        return new CollectionViewState(false, true);
    }

    private record CollectionViewState(boolean collapsed, boolean pinLoadoutsWhenCollapsed) {
    }
}
