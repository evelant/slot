package dev.imagio.slot.recent;

import dev.imagio.slot.client.model.ItemEntry;
import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.client.policy.ItemBehaviorPolicy;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public final class RecentService {
    private static final int MAX_TRACKED_IDENTITIES = 32;
    private static final int VISIBLE_LIMIT = 5;
    private static final int DEFAULT_SUPPRESSION_TICKS = 8;
    private static final long NANOS_PER_TICK = 50_000_000L;

    private final RecentStateStore stateStore = new RecentStateStore(MAX_TRACKED_IDENTITIES);
    private final EnumMap<AcquisitionObservationScope, Map<ItemIdentity, Integer>> baselines =
            new EnumMap<>(AcquisitionObservationScope.class);
    private final EnumMap<AcquisitionObservationScope, ObservationWindow> observationWindows =
            new EnumMap<>(AcquisitionObservationScope.class);
    private final EnumSet<AcquisitionObservationScope> observedScopes =
            EnumSet.noneOf(AcquisitionObservationScope.class);

    private long suppressObservedAcquisitionsUntilNanos;

    public synchronized void recordPickup(ItemStack originalStack, ItemStack currentStack) {
        if (originalStack == null || originalStack.isEmpty()) {
            return;
        }

        int originalCount = originalStack.getCount();
        int remainingCount = currentStack == null || currentStack.isEmpty() ? 0 : currentStack.getCount();
        if (originalCount > remainingCount) {
            recordStack(originalStack, originalCount - remainingCount, AcquisitionProducerId.WORLD_PICKUP);
        }
    }

    public synchronized void recordCrafted(ItemStack craftedStack) {
        recordStack(craftedStack, craftedStack == null ? 0 : craftedStack.getCount(), AcquisitionProducerId.CRAFT_RESULT);
    }

    public synchronized void recordIdentity(ItemIdentity identity) {
        record(new AcquisitionEvent(identity, 1, AcquisitionProducerId.UNKNOWN));
    }

    public synchronized void recordAcquired(ItemStack stack, int acquiredCount) {
        recordStack(stack, acquiredCount, AcquisitionProducerId.WORLD_PICKUP);
    }

    public synchronized void recordOutcomeAcquisitions(String producerId, Collection<String> itemIds) {
        AcquisitionProducerId resolvedProducerId = AcquisitionProducerId.fromSerializedId(producerId);
        if (itemIds == null || itemIds.isEmpty()) {
            return;
        }
        for (String itemId : itemIds) {
            if (itemId == null || itemId.isBlank()) {
                continue;
            }
            record(new AcquisitionEvent(ItemIdentity.of(itemId), 1, resolvedProducerId));
        }
    }

    public synchronized void expectBackpackAcquisition(ItemIdentity identity) {
        ItemIdentity normalized = ItemBehaviorPolicy.normalizeTrackedIdentity(identity);
        if (normalized == null) {
            return;
        }
        expectObservation(
                AcquisitionObservationScope.BACKPACK_CONTENTS,
                AcquisitionProducerId.BACKPACK_PICKUP,
                Set.of(normalized)
        );
    }

    public synchronized void reset() {
        stateStore.reset();
        baselines.clear();
        observationWindows.clear();
        observedScopes.clear();
        suppressObservedAcquisitionsUntilNanos = 0L;
    }

    public synchronized void suppressPositiveDeltas() {
        suppressPositiveDeltas(DEFAULT_SUPPRESSION_TICKS);
    }

    public synchronized void suppressPositiveDeltas(int ticks) {
        long durationNanos = Math.max(0L, ticks) * NANOS_PER_TICK;
        long target = System.nanoTime() + durationNanos;
        suppressObservedAcquisitionsUntilNanos = Math.max(suppressObservedAcquisitionsUntilNanos, target);
    }

    public synchronized boolean observeVanillaSnapshot(Map<ItemIdentity, Integer> counts, boolean ignoredRecordPositiveDeltas) {
        return observeSnapshot(AcquisitionObservationScope.VANILLA_INVENTORY, counts);
    }

    public synchronized boolean observeBackpackSnapshot(Map<ItemIdentity, Integer> counts, boolean ignoredRecordPositiveDeltas) {
        return observeSnapshot(AcquisitionObservationScope.BACKPACK_CONTENTS, counts);
    }

    public synchronized boolean hasVisibleEntries(List<ItemEntry> entries, Predicate<String> carriedSourceFilter) {
        return !visibleRecentIdentities(entries, carriedSourceFilter).isEmpty();
    }

    public synchronized boolean isRecent(ItemIdentity identity) {
        return stateStore.contains(identity);
    }

    public synchronized void dismiss(ItemIdentity identity) {
        stateStore.dismiss(identity);
    }

    public synchronized void dismissAll(Collection<ItemIdentity> identities) {
        stateStore.dismissAll(identities);
    }

    public synchronized List<ItemIdentity> visibleRecentIdentities(
            List<ItemEntry> entries,
            Predicate<String> carriedSourceFilter
    ) {
        return stateStore.visibleRecentIdentities(entries, carriedSourceFilter, VISIBLE_LIMIT);
    }

    private void recordStack(ItemStack stack, int count, AcquisitionProducerId producerId) {
        if (stack == null || stack.isEmpty() || count <= 0) {
            return;
        }

        ItemStack acquiredStack = stack.copy();
        acquiredStack.setCount(Math.max(1, Math.min(count, Math.max(1, acquiredStack.getCount()))));
        record(new AcquisitionEvent(ItemBehaviorPolicy.createIdentity(acquiredStack), acquiredStack.getCount(), producerId));
    }

    private void record(AcquisitionEvent event) {
        if (event == null || !event.valid()) {
            return;
        }
        stateStore.record(event.identity());
    }

    private void expectObservation(
            AcquisitionObservationScope scope,
            AcquisitionProducerId producerId,
            Set<ItemIdentity> expectedIdentities
    ) {
        if (scope == null) {
            return;
        }

        long expiresAtNanos = System.nanoTime() + DEFAULT_SUPPRESSION_TICKS * NANOS_PER_TICK;
        ObservationWindow existingWindow = observationWindows.get(scope);
        observationWindows.put(scope, ObservationWindow.merge(existingWindow, producerId, expectedIdentities, expiresAtNanos));
    }

    private boolean observeSnapshot(AcquisitionObservationScope scope, Map<ItemIdentity, Integer> counts) {
        if (scope == null) {
            return false;
        }

        Map<ItemIdentity, Integer> normalizedCounts = normalizeCounts(counts);
        Map<ItemIdentity, Integer> baseline = baselines.computeIfAbsent(scope, ignored -> new LinkedHashMap<>());
        boolean hadBaseline = observedScopes.contains(scope);
        boolean changed = !baseline.equals(normalizedCounts);
        long now = System.nanoTime();

        ObservationWindow window = observationWindows.remove(scope);
        if (hadBaseline && window != null && window.active(now) && !observedAcquisitionsSuppressed(now)) {
            for (Map.Entry<ItemIdentity, Integer> countEntry : normalizedCounts.entrySet()) {
                int previousCount = baseline.getOrDefault(countEntry.getKey(), 0);
                if (countEntry.getValue() > previousCount && window.matches(countEntry.getKey())) {
                    record(new AcquisitionEvent(
                            countEntry.getKey(),
                            countEntry.getValue() - previousCount,
                            window.producerId()
                    ));
                }
            }
        }

        baseline.clear();
        baseline.putAll(normalizedCounts);
        observedScopes.add(scope);
        return changed;
    }

    private boolean observedAcquisitionsSuppressed(long now) {
        return now < suppressObservedAcquisitionsUntilNanos;
    }

    private static Map<ItemIdentity, Integer> normalizeCounts(Map<ItemIdentity, Integer> counts) {
        if (counts == null || counts.isEmpty()) {
            return Map.of();
        }

        Map<ItemIdentity, Integer> normalized = new LinkedHashMap<>();
        for (Map.Entry<ItemIdentity, Integer> entry : counts.entrySet()) {
            ItemIdentity normalizedIdentity = ItemBehaviorPolicy.normalizeTrackedIdentity(entry.getKey());
            int count = entry.getValue() == null ? 0 : entry.getValue();
            if (normalizedIdentity == null || count <= 0) {
                continue;
            }
            normalized.merge(normalizedIdentity, count, Integer::sum);
        }
        return Map.copyOf(normalized);
    }

    private record ObservationWindow(
            AcquisitionProducerId producerId,
            Set<ItemIdentity> expectedIdentities,
            long expiresAtNanos
    ) {
        private ObservationWindow {
            producerId = producerId == null ? AcquisitionProducerId.UNKNOWN : producerId;
            expectedIdentities = expectedIdentities == null ? Set.of() : Set.copyOf(expectedIdentities);
        }

        boolean active(long now) {
            return now <= expiresAtNanos;
        }

        boolean matches(ItemIdentity identity) {
            return expectedIdentities.isEmpty() || expectedIdentities.contains(identity);
        }

        static ObservationWindow merge(
                ObservationWindow existing,
                AcquisitionProducerId producerId,
                Set<ItemIdentity> expectedIdentities,
                long expiresAtNanos
        ) {
            if (existing == null || !existing.active(System.nanoTime())) {
                return new ObservationWindow(producerId, expectedIdentities, expiresAtNanos);
            }

            Set<ItemIdentity> mergedIdentities = new LinkedHashSet<>(existing.expectedIdentities());
            if (expectedIdentities != null) {
                mergedIdentities.addAll(expectedIdentities);
            }
            return new ObservationWindow(
                    producerId == AcquisitionProducerId.UNKNOWN ? existing.producerId() : producerId,
                    mergedIdentities,
                    Math.max(existing.expiresAtNanos(), expiresAtNanos)
            );
        }
    }
}
