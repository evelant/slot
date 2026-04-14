package dev.imagio.slot.client.screen;

import dev.imagio.slot.client.collection.CollectionStore;
import dev.imagio.slot.client.policy.ItemBehaviorPolicy;
import dev.imagio.slot.projection.InventoryViewData;
import dev.imagio.slot.session.ChestLikeMenuLayout;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public final class InventoryRowActionPlanner {
    private InventoryRowActionPlanner() {
    }

    public static boolean canAssignToQuickAccess(
            InventoryViewData.EntryView entry,
            QuickAccessCapability quickAccess,
            Set<String> preferredSourceIds
    ) {
        if (entry == null || quickAccess == null) {
            return false;
        }
        Set<String> resolvedSourceIds = preferredSourceIds == null ? Set.of() : preferredSourceIds;
        return resolvedSourceIds.isEmpty()
                ? quickAccess.canAssignToQuickAccess(entry.itemEntry().identity())
                : quickAccess.canAssignToQuickAccess(entry.itemEntry().identity(), resolvedSourceIds);
    }

    public static boolean isEquippedOnly(
            InventoryViewData.EntryView entry,
            boolean carriedPane,
            QuickAccessCapability quickAccess,
            Set<String> preferredSourceIds
    ) {
        if (!carriedPane || entry == null || canAssignToQuickAccess(entry, quickAccess, preferredSourceIds)) {
            return false;
        }
        return hasEquippedOnlySources(entry.itemEntry().perSourceCounts(), preferredSourceIds);
    }

    public static List<RowActionSpec> buildActions(
            InventoryViewData.EntryView entry,
            boolean carriedPane,
            boolean recentSection,
            QuickAccessCapability quickAccess,
            Set<String> preferredSourceIds,
            CollectionStore collectionStore,
            Runnable useAction,
            Runnable dropAction,
            Runnable dismissRecentAction,
            Runnable toggleFavoriteAction,
            Runnable inspectAction,
            Runnable toggleJunkAction,
            Runnable addToNewCollectionAction,
            Function<String, Runnable> toggleCollectionMembershipAction
    ) {
        if (entry == null || quickAccess == null || collectionStore == null) {
            return List.of();
        }

        List<RowActionSpec> actions = new ArrayList<>();
        Set<String> resolvedSourceIds = preferredSourceIds == null ? Set.of() : preferredSourceIds;
        var identity = entry.itemEntry().identity();
        var directAction = ItemBehaviorPolicy.directInventoryAction(entry.displayStack());

        if (carriedPane
                && directAction != null
                && supportsUse(quickAccess, identity, resolvedSourceIds)
                && useAction != null) {
            actions.add(new RowActionSpec(
                    Component.translatable(directAction.translationKey()).getString(),
                    useAction
            ));
        }

        if (carriedPane
                && supportsDrop(quickAccess, identity, resolvedSourceIds)
                && dropAction != null) {
            actions.add(new RowActionSpec(
                    Component.translatable("slot.screen.inventory.drop_from_inventory").getString(),
                    dropAction
            ));
        }

        if (carriedPane && recentSection && dismissRecentAction != null) {
            actions.add(new RowActionSpec(
                    Component.translatable("slot.screen.recent.dismiss").getString(),
                    dismissRecentAction
            ));
        }

        if (toggleFavoriteAction != null) {
            actions.add(new RowActionSpec(
                    Component.translatable(entry.itemEntry().favorite() ? "slot.screen.debug.unfavorite" : "slot.screen.debug.favorite").getString(),
                    toggleFavoriteAction
            ));
        }

        if (inspectAction != null) {
            actions.add(new RowActionSpec(
                    Component.translatable("slot.screen.inspect.button").getString(),
                    inspectAction
            ));
        }

        if (toggleJunkAction != null) {
            actions.add(new RowActionSpec(
                    Component.translatable(
                            collectionStore.isJunk(identity)
                                    ? "slot.screen.collections.remove_from_junk"
                                    : "slot.screen.collections.add_to_junk"
                    ).getString(),
                    toggleJunkAction
            ));
        }

        if (addToNewCollectionAction != null) {
            actions.add(new RowActionSpec(
                    Component.translatable("slot.screen.collections.add_to_new").getString(),
                    addToNewCollectionAction
            ));
        }

        if (toggleCollectionMembershipAction != null) {
            for (var collection : collectionStore.userCollections()) {
                boolean member = entry.itemEntry().collectionIds().contains(collection.id());
                String label = Component.translatable(
                        member ? "slot.screen.collections.remove_from" : "slot.screen.collections.add_to",
                        collection.name()
                ).getString();
                actions.add(new RowActionSpec(label, toggleCollectionMembershipAction.apply(collection.id())));
            }
        }

        return List.copyOf(actions);
    }

    private static boolean supportsUse(QuickAccessCapability quickAccess, dev.imagio.slot.client.model.ItemIdentity identity, Set<String> preferredSourceIds) {
        return preferredSourceIds.isEmpty()
                ? quickAccess.canUseFromInventory(identity)
                : quickAccess.canUseFromInventory(identity, preferredSourceIds);
    }

    private static boolean supportsDrop(QuickAccessCapability quickAccess, dev.imagio.slot.client.model.ItemIdentity identity, Set<String> preferredSourceIds) {
        return preferredSourceIds.isEmpty()
                ? quickAccess.canDropFromInventory(identity)
                : quickAccess.canDropFromInventory(identity, preferredSourceIds);
    }

    static boolean hasEquippedOnlySources(Map<String, Integer> perSourceCounts) {
        return hasEquippedOnlySources(perSourceCounts, Set.of());
    }

    static boolean hasEquippedOnlySources(Map<String, Integer> perSourceCounts, Set<String> preferredSourceIds) {
        if (perSourceCounts == null || perSourceCounts.isEmpty()) {
            return false;
        }

        Set<String> relevantSourceIds = preferredSourceIds == null ? Set.of() : preferredSourceIds;
        boolean scoped = !relevantSourceIds.isEmpty();
        boolean hasEquipped = false;
        for (var sourceEntry : perSourceCounts.entrySet()) {
            if (sourceEntry.getValue() == null || sourceEntry.getValue() <= 0) {
                continue;
            }
            if (scoped && !relevantSourceIds.contains(sourceEntry.getKey())) {
                continue;
            }
            if (ChestLikeMenuLayout.SOURCE_PLAYER_ARMOR.equals(sourceEntry.getKey())
                    || ChestLikeMenuLayout.SOURCE_PLAYER_OFFHAND.equals(sourceEntry.getKey())) {
                hasEquipped = true;
                continue;
            }
            return false;
        }
        return hasEquipped;
    }

    public record RowActionSpec(String label, Runnable action) {
    }
}
