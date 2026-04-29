package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.workflow.domain.ChestAffinityMap;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.ClaimedChestMap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Affinity-driven deposit routing.
 *
 * <p>For each carried-pane item, find proximate claimed chests with
 * {@code affinity[chest, identity] > 0}. Highest score wins (ties broken
 * by stable storage-id ordering); the planner emits all ranked candidates
 * so the executor can spill on full.
 *
 * <p>Items with no affinity anywhere stay in carry — see the
 * "needs a home" hint per docs/plans/learned-storage.md.
 */
public final class DepositPlanner {
    private DepositPlanner() {
    }

    public static DepositPlan plan(
            InventoryAuthoritySnapshot authority,
            ChestAffinityMap affinityMap,
            ClaimedChestMap claimedChestMap,
            Set<String> proximateStorageIds
    ) {
        if (authority == null || affinityMap == null || claimedChestMap == null) {
            return DepositPlan.empty();
        }
        Set<String> proximate = proximateStorageIds == null ? Set.of() : proximateStorageIds;
        if (proximate.isEmpty()) {
            return DepositPlan.empty();
        }

        ArrayList<DepositPlan.Assignment> assignments = new ArrayList<>();
        List<InventorySourceDescriptor> declaredCarried = authority.carriedSources();
        Iterable<String> sourceIds = declaredCarried.isEmpty()
                ? authority.sourcesById().keySet()
                : declaredCarried.stream().map(InventorySourceDescriptor::id).toList();
        for (String sourceId : sourceIds) {
            for (InventoryEntrySnapshot entry : authority.entries(sourceId)) {
                if (entry == null || !entry.present()) {
                    continue;
                }
                ItemIdentity identity = ItemIdentityMatcher.create(entry.stack());
                List<String> candidates = rankCandidates(identity, claimedChestMap, affinityMap, proximate);
                if (candidates.isEmpty()) {
                    continue;
                }
                assignments.add(new DepositPlan.Assignment(
                        sourceId,
                        entry.slotIndex(),
                        identity.itemId(),
                        candidates
                ));
            }
        }
        return new DepositPlan(assignments);
    }

    /**
     * Rank proximate claimed chests for {@code identity} by stored
     * affinity score (descending). Returns storage-id strings.
     */
    private static List<String> rankCandidates(
            ItemIdentity identity,
            ClaimedChestMap claimedChestMap,
            ChestAffinityMap affinityMap,
            Set<String> proximate
    ) {
        record Candidate(String storageId, int score) {
        }
        ArrayList<Candidate> ranked = new ArrayList<>();
        for (ClaimedChest chest : claimedChestMap.chests()) {
            if (chest == null) {
                continue;
            }
            String idString = chest.storageId().toString();
            if (!proximate.contains(idString)) {
                continue;
            }
            int score = affinityMap.score(chest.storageId(), identity);
            if (score <= 0) {
                continue;
            }
            ranked.add(new Candidate(idString, score));
        }
        if (ranked.isEmpty()) {
            return List.of();
        }
        ranked.sort(Comparator.<Candidate>comparingInt(Candidate::score).reversed()
                .thenComparing(Candidate::storageId));
        ArrayList<String> ids = new ArrayList<>(ranked.size());
        for (Candidate candidate : ranked) {
            ids.add(candidate.storageId());
        }
        return List.copyOf(ids);
    }

    @SuppressWarnings("unused")
    private static UUID parseStorageId(String raw) {
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
