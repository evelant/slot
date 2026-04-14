package dev.imagio.slot.workflow;

import dev.imagio.slot.client.collection.CollectionStore;
import dev.imagio.slot.client.collection.CollectionStockSummary;
import dev.imagio.slot.client.collection.CollectionViewStateController;
import dev.imagio.slot.client.collection.HotbarLoadoutDefinition;
import dev.imagio.slot.client.collection.HotbarLoadoutSlot;
import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.projection.InventoryViewData;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

public final class CollectionWorkflowService {
    private final CollectionStore collectionStore;
    private final CollectionViewStateController collectionViewStateController;
    private final Map<String, String> selectedLoadoutIds = new LinkedHashMap<>();

    public CollectionWorkflowService(
            CollectionStore collectionStore,
            CollectionViewStateController collectionViewStateController
    ) {
        this.collectionStore = Objects.requireNonNull(collectionStore, "collectionStore");
        this.collectionViewStateController = collectionViewStateController == null
                ? CollectionViewStateController.NOOP
                : collectionViewStateController;
    }

    public void normalizeViewState(InventoryViewData inventoryViewData) {
        InventoryViewData safeData = inventoryViewData == null
                ? new InventoryViewData(List.of(), List.of(), Map.of(), Map.of())
                : inventoryViewData;
        selectedLoadoutIds.entrySet().removeIf(entry -> {
            if (!safeData.collectionNames().containsKey(entry.getKey())) {
                return true;
            }
            return collectionStore.loadoutsFor(entry.getKey()).stream().noneMatch(loadout -> loadout.id().equals(entry.getValue()));
        });

        for (InventoryViewData.Section section : safeData.sections()) {
            if (section.isCollection() && collectionHasLoadouts(section.collectionId())) {
                ensureSelectedLoadoutId(section.collectionId());
            }
        }
    }

    public boolean collectionHasLoadouts(String collectionId) {
        return collectionId != null && !collectionStore.loadoutsFor(collectionId).isEmpty();
    }

    public HotbarLoadoutDefinition selectedLoadout(String collectionId) {
        if (collectionId == null || collectionId.isBlank()) {
            return null;
        }

        List<HotbarLoadoutDefinition> loadouts = collectionStore.loadoutsFor(collectionId);
        if (loadouts.isEmpty()) {
            return null;
        }

        ensureSelectedLoadoutId(collectionId);
        String selectedLoadoutId = selectedLoadoutIds.get(collectionId);
        for (HotbarLoadoutDefinition loadout : loadouts) {
            if (loadout.id().equals(selectedLoadoutId)) {
                return loadout;
            }
        }
        return loadouts.get(0);
    }

    public String nextAutoLoadoutName(String collectionId, IntFunction<String> autoNameFactory) {
        int index = 1;
        while (true) {
            String candidate = autoNameFactory.apply(index);
            boolean inUse = collectionStore.loadoutsFor(collectionId).stream()
                    .anyMatch(loadout -> loadout.name().equalsIgnoreCase(candidate));
            if (!inUse) {
                return candidate;
            }
            index++;
        }
    }

    public boolean cycleSelectedLoadoutHotkey(String collectionId) {
        HotbarLoadoutDefinition loadout = selectedLoadout(collectionId);
        if (loadout == null) {
            return false;
        }

        Integer nextHotkey = loadout.hotkeySlot() == null
                ? 0
                : loadout.hotkeySlot() >= 8 ? null : loadout.hotkeySlot() + 1;
        if (Objects.equals(nextHotkey, loadout.hotkeySlot())) {
            return false;
        }
        collectionStore.setHotbarLoadoutHotkey(collectionId, loadout.id(), nextHotkey);
        return true;
    }

    public boolean clearSelectedLoadoutHotkey(String collectionId) {
        HotbarLoadoutDefinition loadout = selectedLoadout(collectionId);
        if (loadout == null || loadout.hotkeySlot() == null) {
            return false;
        }

        collectionStore.setHotbarLoadoutHotkey(collectionId, loadout.id(), null);
        return true;
    }

    public boolean deleteSelectedLoadout(String collectionId) {
        HotbarLoadoutDefinition loadout = selectedLoadout(collectionId);
        if (loadout == null) {
            return false;
        }

        collectionStore.deleteHotbarLoadout(collectionId, loadout.id());
        ensureSelectedLoadoutId(collectionId);
        return true;
    }

    public boolean renameLoadout(String collectionId, String loadoutId, String newName) {
        HotbarLoadoutDefinition loadout;
        try {
            loadout = collectionStore.loadout(collectionId, loadoutId);
        } catch (IllegalArgumentException ignored) {
            return false;
        }

        String normalizedName = newName == null ? "" : newName.trim();
        if (loadout.name().equals(normalizedName)) {
            return false;
        }
        collectionStore.renameHotbarLoadout(collectionId, loadoutId, normalizedName);
        ensureSelectedLoadoutId(collectionId);
        return true;
    }

    public boolean isCollectionCollapsed(String collectionId) {
        return collectionId != null && collectionViewStateController.isCollectionCollapsed(collectionId);
    }

    public boolean pinLoadoutsWhenCollectionCollapsed(String collectionId) {
        return collectionViewStateController.pinLoadoutsWhenCollectionCollapsed(collectionId);
    }

    public void toggleCollectionCollapsed(String collectionId) {
        if (collectionId == null || collectionId.isBlank()) {
            return;
        }
        collectionViewStateController.toggleCollectionCollapsed(collectionId);
    }

    public void togglePinLoadoutsWhenCollectionCollapsed(String collectionId) {
        if (collectionId == null || collectionId.isBlank()) {
            return;
        }
        collectionViewStateController.togglePinLoadoutsWhenCollectionCollapsed(collectionId);
    }

    public CollectionStockSummary collectionStockSummary(
            String collectionId,
            ToIntFunction<ItemIdentity> ownedCountResolver
    ) {
        if (collectionId == null || collectionId.isBlank() || !collectionStore.isUserCollection(collectionId)) {
            return CollectionStockSummary.NONE;
        }
        return CollectionStockSummary.summarize(
                collectionStore,
                collectionId,
                identity -> ownedCountResolver == null ? 0 : ownedCountResolver.applyAsInt(identity)
        );
    }

    public HotbarLoadoutDefinition createLoadout(
            String collectionId,
            String name,
            List<HotbarLoadoutSlot> slots,
            ItemIdentity offhandIdentity
    ) {
        HotbarLoadoutDefinition loadout = collectionStore.createHotbarLoadout(collectionId, name, slots, offhandIdentity);
        selectedLoadoutIds.put(collectionId, loadout.id());
        return loadout;
    }

    public boolean cycleSelectedLoadout(String collectionId, int delta) {
        List<HotbarLoadoutDefinition> loadouts = collectionStore.loadoutsFor(collectionId);
        if (loadouts.size() < 2) {
            return false;
        }

        String currentId = selectedLoadoutIds.get(collectionId);
        int currentIndex = 0;
        for (int index = 0; index < loadouts.size(); index++) {
            if (loadouts.get(index).id().equals(currentId)) {
                currentIndex = index;
                break;
            }
        }

        int nextIndex = Math.floorMod(currentIndex + delta, loadouts.size());
        String nextId = loadouts.get(nextIndex).id();
        if (Objects.equals(nextId, currentId)) {
            return false;
        }
        selectedLoadoutIds.put(collectionId, nextId);
        return true;
    }

    public HotbarLoadoutDefinition updateSelectedLoadout(
            String collectionId,
            List<HotbarLoadoutSlot> slots,
            ItemIdentity offhandIdentity
    ) {
        HotbarLoadoutDefinition loadout = selectedLoadout(collectionId);
        if (loadout == null) {
            return null;
        }

        collectionStore.updateHotbarLoadout(collectionId, loadout.id(), slots, offhandIdentity);
        selectedLoadoutIds.put(collectionId, loadout.id());
        return collectionStore.loadout(collectionId, loadout.id());
    }

    public boolean toggleFavorite(ItemIdentity identity, boolean currentlyFavorite) {
        if (identity == null) {
            return false;
        }
        collectionStore.setFavorite(identity, !currentlyFavorite);
        return true;
    }

    public boolean toggleCollectionMembership(ItemIdentity identity, Set<String> currentCollectionIds, String collectionId) {
        if (identity == null || collectionId == null || collectionId.isBlank()) {
            return false;
        }
        if (currentCollectionIds != null && currentCollectionIds.contains(collectionId)) {
            collectionStore.removeFromCollection(collectionId, identity);
        } else {
            collectionStore.addToCollection(collectionId, identity);
        }
        return true;
    }

    public boolean setDesiredCount(String collectionId, ItemIdentity identity, int desiredCount) {
        if (collectionId == null || identity == null) {
            return false;
        }
        if (collectionStore.desiredCount(collectionId, identity) == Math.max(1, desiredCount)) {
            return false;
        }
        collectionStore.setDesiredCount(collectionId, identity, desiredCount);
        return true;
    }

    public boolean deleteCollection(String collectionId) {
        if (!collectionStore.isUserCollection(collectionId)) {
            return false;
        }
        collectionStore.deleteCollection(collectionId);
        selectedLoadoutIds.remove(collectionId);
        return true;
    }

    private void ensureSelectedLoadoutId(String collectionId) {
        List<HotbarLoadoutDefinition> loadouts = collectionStore.loadoutsFor(collectionId);
        if (loadouts.isEmpty()) {
            selectedLoadoutIds.remove(collectionId);
            return;
        }

        String selectedLoadoutId = selectedLoadoutIds.get(collectionId);
        if (selectedLoadoutId == null || loadouts.stream().noneMatch(loadout -> loadout.id().equals(selectedLoadoutId))) {
            selectedLoadoutIds.put(collectionId, loadouts.get(0).id());
        }
    }
}
