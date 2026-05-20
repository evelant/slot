package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityCollections;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ActivityProjection {
    private ActivityProjection() {
    }

    public static Snapshot empty() {
        return Snapshot.empty();
    }

    public static Snapshot replay(
            InventoryActivityStore.Snapshot storeSnapshot,
            Snapshot checkpoint,
            Map<ItemIdentity, Long> dismissedUpToByIdentity
    ) {
        Snapshot current = checkpoint == null ? Snapshot.empty() : checkpoint;
        InventoryActivityStore.Snapshot resolvedStore = storeSnapshot == null ? InventoryActivityStore.Snapshot.empty() : storeSnapshot;
        Map<ItemIdentity, Long> dismissed = dismissedUpToByIdentity == null ? Map.of() : dismissedUpToByIdentity;

        LinkedHashMap<ItemIdentity, Integer> recentCounts = new LinkedHashMap<>(current.recents().countsByIdentity());
        LinkedHashMap<ItemIdentity, Long> recentSequences = new LinkedHashMap<>(current.recents().latestSequenceByIdentity());
        LinkedHashMap<String, InventoryActivityRecord> recoverable = new LinkedHashMap<>();

        for (InventoryActivityRecord record : resolvedStore.records()) {
            if (record == null || record.event() == null || record.envelope() == null) {
                continue;
            }
            InventoryActivityEvent event = record.event();
            long sequence = record.envelope().globalSequence();
            if (event.present() && contributesToRecent(event)) {
                ItemIdentity identity = ItemIdentityCollections.key(event.identity());
                if (identity == null) {
                    continue;
                }
                long dismissedUpTo = ItemIdentityCollections.findOrDefault(dismissed, identity, 0L);
                if (sequence > dismissedUpTo) {
                    ItemIdentityCollections.removeMatching(recentCounts, identity);
                    ItemIdentityCollections.removeMatching(recentSequences, identity);
                    recentCounts.put(identity, event.count());
                    recentSequences.put(identity, sequence);
                }
            }
            if (!event.recoveryToken().isBlank()) {
                switch (event.kind()) {
                    case TRASHED, OVERFLOW_STAGED -> recoverable.put(event.recoveryToken(), record);
                    case RESTORED, VOIDED -> recoverable.remove(event.recoveryToken());
                    default -> {
                    }
                }
            }
        }

        ArrayList<InventoryActivityRecord> cleanupCandidates = new ArrayList<>(recoverable.values());
        cleanupCandidates.sort(java.util.Comparator.comparingLong(record -> record.envelope().globalSequence()));
        ArrayList<InventoryActivityRecord> undoCandidates = new ArrayList<>(cleanupCandidates);

        return new Snapshot(
                new RecentView(recentCounts, recentSequences),
                List.copyOf(cleanupCandidates),
                List.copyOf(undoCandidates)
        );
    }

    public static Snapshot apply(
            Snapshot snapshot,
            InventoryActivityRecord record,
            Map<ItemIdentity, Long> dismissedUpToByIdentity
    ) {
        Snapshot current = snapshot == null ? Snapshot.empty() : snapshot;
        if (record == null || record.event() == null || record.envelope() == null) {
            return current;
        }

        Map<ItemIdentity, Long> dismissed = dismissedUpToByIdentity == null ? Map.of() : dismissedUpToByIdentity;
        LinkedHashMap<ItemIdentity, Integer> recentCounts = new LinkedHashMap<>(current.recents().countsByIdentity());
        LinkedHashMap<ItemIdentity, Long> recentSequences = new LinkedHashMap<>(current.recents().latestSequenceByIdentity());
        LinkedHashMap<String, InventoryActivityRecord> recoverableByToken = cleanupIndex(current.cleanupCandidates());

        InventoryActivityEvent event = record.event();
        long sequence = record.envelope().globalSequence();
        ItemIdentity identity = ItemIdentityCollections.key(event.identity());
        if (identity != null && event.present() && contributesToRecent(event)
                && sequence > ItemIdentityCollections.findOrDefault(dismissed, identity, 0L)) {
            ItemIdentityCollections.removeMatching(recentCounts, identity);
            ItemIdentityCollections.removeMatching(recentSequences, identity);
            recentCounts.put(identity, event.count());
            recentSequences.put(identity, sequence);
        }
        applyRecoverableMutation(recoverableByToken, record);

        return new Snapshot(
                new RecentView(recentCounts, recentSequences),
                sortedRecoverables(recoverableByToken),
                sortedRecoverables(recoverableByToken)
        );
    }

    public static Snapshot applyDismissals(
            Snapshot snapshot,
            Map<ItemIdentity, Long> dismissedUpToByIdentity
    ) {
        Snapshot current = snapshot == null ? Snapshot.empty() : snapshot;
        Map<ItemIdentity, Long> dismissed = dismissedUpToByIdentity == null ? Map.of() : dismissedUpToByIdentity;
        LinkedHashMap<ItemIdentity, Integer> recentCounts = new LinkedHashMap<>();
        LinkedHashMap<ItemIdentity, Long> recentSequences = new LinkedHashMap<>();

        current.recents().countsByIdentity().forEach((identity, count) -> {
            long sequence = current.recents().latestSequenceByIdentity().getOrDefault(identity, 0L);
            ItemIdentity key = ItemIdentityCollections.key(identity);
            if (sequence > ItemIdentityCollections.findOrDefault(dismissed, key, 0L)) {
                recentCounts.put(key, count);
                recentSequences.put(key, sequence);
            }
        });

        return new Snapshot(
                new RecentView(recentCounts, recentSequences),
                current.cleanupCandidates(),
                current.undoCandidates()
        );
    }

    private static boolean contributesToRecent(InventoryActivityEvent event) {
        return switch (event.kind()) {
            case ACQUIRED, CRAFTED, SMELTED -> true;
            default -> false;
        };
    }

    private static LinkedHashMap<String, InventoryActivityRecord> cleanupIndex(List<InventoryActivityRecord> cleanupCandidates) {
        LinkedHashMap<String, InventoryActivityRecord> indexed = new LinkedHashMap<>();
        if (cleanupCandidates == null) {
            return indexed;
        }
        for (InventoryActivityRecord record : cleanupCandidates) {
            if (record != null && record.event() != null && !record.event().recoveryToken().isBlank()) {
                indexed.put(record.event().recoveryToken(), record);
            }
        }
        return indexed;
    }

    private static void applyRecoverableMutation(
            Map<String, InventoryActivityRecord> recoverableByToken,
            InventoryActivityRecord record
    ) {
        if (recoverableByToken == null || record == null || record.event() == null) {
            return;
        }
        String recoveryToken = record.event().recoveryToken();
        if (recoveryToken.isBlank()) {
            return;
        }
        switch (record.event().kind()) {
            case TRASHED, OVERFLOW_STAGED -> recoverableByToken.put(recoveryToken, record);
            case RESTORED, VOIDED -> recoverableByToken.remove(recoveryToken);
            default -> {
            }
        }
    }

    private static List<InventoryActivityRecord> sortedRecoverables(Map<String, InventoryActivityRecord> recoverableByToken) {
        ArrayList<InventoryActivityRecord> sorted = new ArrayList<>(recoverableByToken.values());
        sorted.sort(java.util.Comparator.comparingLong(record -> record.envelope().globalSequence()));
        return List.copyOf(sorted);
    }

    public record Snapshot(
            RecentView recents,
            List<InventoryActivityRecord> cleanupCandidates,
            List<InventoryActivityRecord> undoCandidates
    ) {
        public Snapshot {
            recents = recents == null ? RecentView.empty() : recents;
            cleanupCandidates = cleanupCandidates == null ? List.of() : List.copyOf(cleanupCandidates);
            undoCandidates = undoCandidates == null ? List.of() : List.copyOf(undoCandidates);
        }

        public static Snapshot empty() {
            return new Snapshot(RecentView.empty(), List.of(), List.of());
        }
    }
}
