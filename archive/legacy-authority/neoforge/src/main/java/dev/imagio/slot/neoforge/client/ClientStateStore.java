package dev.imagio.slot.neoforge.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.client.collection.CollectionDefinition;
import dev.imagio.slot.client.collection.CollectionDisplayMode;
import dev.imagio.slot.client.collection.CollectionStore;
import dev.imagio.slot.client.collection.HotbarLoadoutDefinition;
import dev.imagio.slot.client.collection.HotbarLoadoutSlot;
import dev.imagio.slot.client.model.ComparisonMode;
import dev.imagio.slot.client.model.ItemIdentity;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class ClientStateStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final int STATE_VERSION = 5;

    private final Path statePath;

    ClientStateStore(Path statePath) {
        this.statePath = statePath;
    }

    ClientStateSnapshot load(ClientStateDefaults defaults) {
        ClientStateDefaults safeDefaults = defaults == null
                ? new ClientStateDefaults(true, true, true, true)
                : defaults;
        ClientPreferences defaultPreferences = new ClientPreferences(
                safeDefaults.enabled(),
                safeDefaults.replacePlayerInventory(),
                safeDefaults.replaceChestLikeStorage(),
                safeDefaults.syncSearchWithEmi()
        );

        if (!Files.exists(statePath)) {
            return new ClientStateSnapshot(defaultPreferences, emptyCollectionSnapshot(), Map.of());
        }

        try (Reader reader = Files.newBufferedReader(statePath)) {
            StateData state = GSON.fromJson(reader, StateData.class);
            if (state == null) {
                return new ClientStateSnapshot(defaultPreferences, emptyCollectionSnapshot(), Map.of());
            }

            ClientPreferences preferences = loadPreferences(state, defaultPreferences);
            CollectionStore.Snapshot collectionSnapshot = loadCollectionSnapshot(state);
            Map<String, CollectionViewStateData> collectionViewStates = loadCollectionViewStates(state);
            return new ClientStateSnapshot(preferences, collectionSnapshot, collectionViewStates);
        } catch (IOException | JsonParseException | IllegalArgumentException exception) {
            SlotCommon.LOGGER.warn("Failed to load SLOT client state from {}", statePath, exception);
            return new ClientStateSnapshot(defaultPreferences, emptyCollectionSnapshot(), Map.of());
        }
    }

    void save(ClientStateSnapshot snapshot) {
        ClientStateSnapshot safeSnapshot = snapshot == null
                ? new ClientStateSnapshot(new ClientPreferences(true, true, true, true), emptyCollectionSnapshot(), Map.of())
                : snapshot;

        StateData state = new StateData();
        state.version = STATE_VERSION;
        state.preferences = new PreferencesData();
        state.preferences.enabled = safeSnapshot.preferences().enabled();
        state.preferences.replacePlayerInventory = safeSnapshot.preferences().replacePlayerInventory();
        state.preferences.replaceChestLikeStorage = safeSnapshot.preferences().replaceChestLikeStorage();
        state.preferences.syncSearchWithEmi = safeSnapshot.preferences().syncSearchWithEmi();

        CollectionStore.Snapshot collectionSnapshot = safeSnapshot.collectionSnapshot();
        state.collections = new ArrayList<>();
        for (CollectionDefinition collection : collectionSnapshot.userCollections()) {
            CollectionData collectionData = new CollectionData();
            collectionData.id = collection.id();
            collectionData.name = collection.name();
            collectionData.displayMode = collection.displayMode().name();
            state.collections.add(collectionData);
        }

        state.memberships = new ArrayList<>();
        for (Map.Entry<ItemIdentity, Set<String>> entry : collectionSnapshot.memberships().entrySet()) {
            MembershipData membershipData = new MembershipData();
            membershipData.itemId = entry.getKey().itemId();
            membershipData.comparisonMode = entry.getKey().comparisonMode().name();
            membershipData.componentFingerprint = entry.getKey().componentFingerprint();
            membershipData.collections = List.copyOf(entry.getValue());
            state.memberships.add(membershipData);
        }

        state.desiredCounts = new ArrayList<>();
        for (Map.Entry<String, Map<ItemIdentity, Integer>> entry : collectionSnapshot.desiredCountsByCollection().entrySet()) {
            for (Map.Entry<ItemIdentity, Integer> desiredEntry : entry.getValue().entrySet()) {
                DesiredCountData desiredCountData = new DesiredCountData();
                desiredCountData.collectionId = entry.getKey();
                desiredCountData.itemId = desiredEntry.getKey().itemId();
                desiredCountData.comparisonMode = desiredEntry.getKey().comparisonMode().name();
                desiredCountData.componentFingerprint = desiredEntry.getKey().componentFingerprint();
                desiredCountData.desiredCount = desiredEntry.getValue();
                state.desiredCounts.add(desiredCountData);
            }
        }

        state.hotbarLoadouts = new ArrayList<>();
        for (Map.Entry<String, List<HotbarLoadoutDefinition>> entry : collectionSnapshot.loadoutsByCollection().entrySet()) {
            for (HotbarLoadoutDefinition loadout : entry.getValue()) {
                LoadoutData loadoutData = new LoadoutData();
                loadoutData.collectionId = entry.getKey();
                loadoutData.id = loadout.id();
                loadoutData.name = loadout.name();
                loadoutData.hotkeySlot = loadout.hotkeySlot();
                if (loadout.offhandIdentity() != null) {
                    loadoutData.offhandItemId = loadout.offhandIdentity().itemId();
                    loadoutData.offhandComparisonMode = loadout.offhandIdentity().comparisonMode().name();
                    loadoutData.offhandComponentFingerprint = loadout.offhandIdentity().componentFingerprint();
                }
                loadoutData.slots = new ArrayList<>();
                for (HotbarLoadoutSlot slot : loadout.slots()) {
                    LoadoutSlotData slotData = new LoadoutSlotData();
                    slotData.slotIndex = slot.slotIndex();
                    slotData.itemId = slot.identity().itemId();
                    slotData.comparisonMode = slot.identity().comparisonMode().name();
                    slotData.componentFingerprint = slot.identity().componentFingerprint();
                    loadoutData.slots.add(slotData);
                }
                state.hotbarLoadouts.add(loadoutData);
            }
        }

        state.collectionViews = new ArrayList<>();
        for (Map.Entry<String, CollectionViewStateData> entry : safeSnapshot.collectionViewStates().entrySet()) {
            CollectionViewStateData viewState = entry.getValue();
            if (viewState == null || (!viewState.collapsed() && viewState.pinLoadoutsWhenCollapsed())) {
                continue;
            }
            CollectionViewData viewData = new CollectionViewData();
            viewData.collectionId = entry.getKey();
            viewData.collapsed = viewState.collapsed();
            viewData.pinLoadoutsWhenCollapsed = viewState.pinLoadoutsWhenCollapsed();
            state.collectionViews.add(viewData);
        }

        try {
            Files.createDirectories(statePath.getParent());
            try (Writer writer = Files.newBufferedWriter(statePath)) {
                GSON.toJson(state, writer);
            }
        } catch (IOException exception) {
            SlotCommon.LOGGER.warn("Failed to save SLOT client state to {}", statePath, exception);
        }
    }

    private static ClientPreferences loadPreferences(StateData state, ClientPreferences defaults) {
        if (state.preferences == null) {
            return defaults;
        }
        return new ClientPreferences(
                state.preferences.enabled == null ? defaults.enabled() : state.preferences.enabled,
                state.preferences.replacePlayerInventory == null ? defaults.replacePlayerInventory() : state.preferences.replacePlayerInventory,
                state.preferences.replaceChestLikeStorage == null ? defaults.replaceChestLikeStorage() : state.preferences.replaceChestLikeStorage,
                state.preferences.syncSearchWithEmi == null ? defaults.syncSearchWithEmi() : state.preferences.syncSearchWithEmi
        );
    }

    private static CollectionStore.Snapshot loadCollectionSnapshot(StateData state) {
        List<CollectionDefinition> userCollections = new ArrayList<>();
        if (state.collections != null) {
            for (CollectionData collection : state.collections) {
                if (collection == null || collection.id == null || collection.name == null) {
                    continue;
                }

                CollectionDisplayMode displayMode = parseCollectionDisplayMode(collection.displayMode);
                userCollections.add(new CollectionDefinition(collection.id, collection.name, false, displayMode));
            }
        }

        Map<ItemIdentity, Set<String>> memberships = new LinkedHashMap<>();
        if (state.memberships != null) {
            for (MembershipData membership : state.memberships) {
                if (membership == null || membership.itemId == null || membership.itemId.isBlank()) {
                    continue;
                }

                ComparisonMode comparisonMode = parseComparisonMode(membership.comparisonMode);
                ItemIdentity identity = new ItemIdentity(
                        membership.itemId,
                        comparisonMode,
                        membership.componentFingerprint == null ? "" : membership.componentFingerprint
                );
                memberships.put(identity, membership.collections == null ? Set.of() : Set.copyOf(membership.collections));
            }
        }

        Map<String, List<HotbarLoadoutDefinition>> loadoutsByCollection = new LinkedHashMap<>();
        if (state.hotbarLoadouts != null) {
            for (LoadoutData loadout : state.hotbarLoadouts) {
                if (loadout == null || loadout.collectionId == null || loadout.id == null || loadout.name == null) {
                    continue;
                }

                List<HotbarLoadoutSlot> slots = new ArrayList<>();
                if (loadout.slots != null) {
                    for (LoadoutSlotData slot : loadout.slots) {
                        if (slot == null || slot.slotIndex == null || slot.itemId == null || slot.itemId.isBlank()) {
                            continue;
                        }

                        ComparisonMode comparisonMode = parseComparisonMode(slot.comparisonMode);
                        ItemIdentity identity = new ItemIdentity(
                                slot.itemId,
                                comparisonMode,
                                slot.componentFingerprint == null ? "" : slot.componentFingerprint
                        );
                        slots.add(new HotbarLoadoutSlot(slot.slotIndex, identity));
                    }
                }

                ItemIdentity offhandIdentity = null;
                if (loadout.offhandItemId != null && !loadout.offhandItemId.isBlank()) {
                    ComparisonMode comparisonMode = parseComparisonMode(loadout.offhandComparisonMode);
                    offhandIdentity = new ItemIdentity(
                            loadout.offhandItemId,
                            comparisonMode,
                            loadout.offhandComponentFingerprint == null ? "" : loadout.offhandComponentFingerprint
                    );
                }

                if (slots.isEmpty() && offhandIdentity == null) {
                    continue;
                }

                loadoutsByCollection.computeIfAbsent(loadout.collectionId, ignored -> new ArrayList<>())
                        .add(new HotbarLoadoutDefinition(loadout.id, loadout.name, loadout.hotkeySlot, slots, offhandIdentity));
            }
        }

        Map<String, Map<ItemIdentity, Integer>> desiredCountsByCollection = new LinkedHashMap<>();
        if (state.desiredCounts != null) {
            for (DesiredCountData desiredCount : state.desiredCounts) {
                if (desiredCount == null
                        || desiredCount.collectionId == null
                        || desiredCount.itemId == null
                        || desiredCount.itemId.isBlank()) {
                    continue;
                }

                ComparisonMode comparisonMode = parseComparisonMode(desiredCount.comparisonMode);
                ItemIdentity identity = new ItemIdentity(
                        desiredCount.itemId,
                        comparisonMode,
                        desiredCount.componentFingerprint == null ? "" : desiredCount.componentFingerprint
                );
                desiredCountsByCollection
                        .computeIfAbsent(desiredCount.collectionId, ignored -> new LinkedHashMap<>())
                        .put(identity, desiredCount.desiredCount == null ? 1 : Math.max(1, desiredCount.desiredCount));
            }
        }

        return new CollectionStore.Snapshot(userCollections, memberships, loadoutsByCollection, desiredCountsByCollection);
    }

    private static Map<String, CollectionViewStateData> loadCollectionViewStates(StateData state) {
        Map<String, CollectionViewStateData> collectionViewStates = new LinkedHashMap<>();
        if (state.collectionViews != null) {
            for (CollectionViewData view : state.collectionViews) {
                if (view == null || view.collectionId == null || view.collectionId.isBlank()) {
                    continue;
                }
                collectionViewStates.put(
                        view.collectionId,
                        new CollectionViewStateData(
                                Boolean.TRUE.equals(view.collapsed),
                                view.pinLoadoutsWhenCollapsed == null || view.pinLoadoutsWhenCollapsed
                        )
                );
            }
        }
        return Map.copyOf(collectionViewStates);
    }

    private static CollectionDisplayMode parseCollectionDisplayMode(String serialized) {
        if (serialized == null || serialized.isBlank()) {
            return CollectionDisplayMode.OWNED_ONLY;
        }
        try {
            return CollectionDisplayMode.valueOf(serialized);
        } catch (IllegalArgumentException ignored) {
            return CollectionDisplayMode.OWNED_ONLY;
        }
    }

    private static ComparisonMode parseComparisonMode(String serialized) {
        if (serialized == null || serialized.isBlank()) {
            return ComparisonMode.ITEM_ID;
        }
        try {
            return ComparisonMode.valueOf(serialized);
        } catch (IllegalArgumentException ignored) {
            return ComparisonMode.ITEM_ID;
        }
    }

    private static CollectionStore.Snapshot emptyCollectionSnapshot() {
        return new CollectionStore.Snapshot(List.of(), Map.of(), Map.of(), Map.of());
    }

    record ClientStateDefaults(
            boolean enabled,
            boolean replacePlayerInventory,
            boolean replaceChestLikeStorage,
            boolean syncSearchWithEmi
    ) {
    }

    record ClientPreferences(
            boolean enabled,
            boolean replacePlayerInventory,
            boolean replaceChestLikeStorage,
            boolean syncSearchWithEmi
    ) {
    }

    record CollectionViewStateData(
            boolean collapsed,
            boolean pinLoadoutsWhenCollapsed
    ) {
    }

    record ClientStateSnapshot(
            ClientPreferences preferences,
            CollectionStore.Snapshot collectionSnapshot,
            Map<String, CollectionViewStateData> collectionViewStates
    ) {
        ClientStateSnapshot {
            preferences = preferences == null ? new ClientPreferences(true, true, true, true) : preferences;
            collectionSnapshot = collectionSnapshot == null ? emptyCollectionSnapshot() : collectionSnapshot;
            collectionViewStates = collectionViewStates == null ? Map.of() : Map.copyOf(collectionViewStates);
        }
    }

    private static final class StateData {
        private Integer version;
        private PreferencesData preferences;
        private List<CollectionData> collections;
        private List<MembershipData> memberships;
        private List<DesiredCountData> desiredCounts;
        private List<LoadoutData> hotbarLoadouts;
        private List<CollectionViewData> collectionViews;
    }

    private static final class PreferencesData {
        private Boolean enabled;
        private Boolean replacePlayerInventory;
        private Boolean replaceChestLikeStorage;
        private Boolean syncSearchWithEmi;
    }

    private static final class CollectionData {
        private String id;
        private String name;
        private String displayMode;
    }

    private static final class MembershipData {
        private String itemId;
        private String comparisonMode;
        private String componentFingerprint;
        private List<String> collections;
    }

    private static final class LoadoutData {
        private String collectionId;
        private String id;
        private String name;
        private Integer hotkeySlot;
        private String offhandItemId;
        private String offhandComparisonMode;
        private String offhandComponentFingerprint;
        private List<LoadoutSlotData> slots;
    }

    private static final class DesiredCountData {
        private String collectionId;
        private String itemId;
        private String comparisonMode;
        private String componentFingerprint;
        private Integer desiredCount;
    }

    private static final class LoadoutSlotData {
        private Integer slotIndex;
        private String itemId;
        private String comparisonMode;
        private String componentFingerprint;
    }

    private static final class CollectionViewData {
        private String collectionId;
        private Boolean collapsed;
        private Boolean pinLoadoutsWhenCollapsed;
    }
}
