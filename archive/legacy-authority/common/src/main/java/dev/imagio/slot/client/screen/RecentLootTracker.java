package dev.imagio.slot.client.screen;

import dev.imagio.slot.client.model.ItemEntry;
import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.recent.RecentService;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public final class RecentLootTracker {
    private static final RecentService SERVICE = new RecentService();

    private RecentLootTracker() {
    }

    public static void recordPickup(ItemStack originalStack, ItemStack currentStack) {
        SERVICE.recordPickup(originalStack, currentStack);
    }

    public static void recordCrafted(ItemStack craftedStack) {
        SERVICE.recordCrafted(craftedStack);
    }

    public static void recordIdentity(ItemIdentity identity) {
        SERVICE.recordIdentity(identity);
    }

    public static void recordAcquired(ItemStack stack, int acquiredCount) {
        SERVICE.recordAcquired(stack, acquiredCount);
    }

    public static void recordOutcomeAcquisitions(String producerId, Collection<String> itemIds) {
        SERVICE.recordOutcomeAcquisitions(producerId, itemIds);
    }

    public static void expectBackpackAcquisition(ItemIdentity identity) {
        SERVICE.expectBackpackAcquisition(identity);
    }

    public static void reset() {
        SERVICE.reset();
    }

    public static void suppressPositiveDeltas() {
        SERVICE.suppressPositiveDeltas();
    }

    public static void suppressPositiveDeltas(int ticks) {
        SERVICE.suppressPositiveDeltas(ticks);
    }

    public static boolean observeVanillaSnapshot(java.util.Map<ItemIdentity, Integer> counts, boolean recordPositiveDeltas) {
        return SERVICE.observeVanillaSnapshot(counts, recordPositiveDeltas);
    }

    public static boolean observeBackpackSnapshot(java.util.Map<ItemIdentity, Integer> counts, boolean recordPositiveDeltas) {
        return SERVICE.observeBackpackSnapshot(counts, recordPositiveDeltas);
    }

    public static boolean hasVisibleEntries(List<ItemEntry> entries, Predicate<String> carriedSourceFilter) {
        return SERVICE.hasVisibleEntries(entries, carriedSourceFilter);
    }

    public static boolean isRecent(ItemIdentity identity) {
        return SERVICE.isRecent(identity);
    }

    public static void dismiss(ItemIdentity identity) {
        SERVICE.dismiss(identity);
    }

    public static void dismissAll(Collection<ItemIdentity> identities) {
        SERVICE.dismissAll(identities);
    }

    public static List<ItemIdentity> visibleRecentIdentities(
            List<ItemEntry> entries,
            Predicate<String> carriedSourceFilter
    ) {
        return SERVICE.visibleRecentIdentities(entries, carriedSourceFilter);
    }
}
