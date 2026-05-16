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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.ToIntFunction;

/**
 * Affinity/content-driven deposit routing.
 *
 * <p>For each carried item, route only to proximate claimed chests that
 * are eligible for persistent storage affinity and either already have a
 * positive learned affinity bond for the exact identity
 * ({@code affinity[chest, identity] > 0}) or currently hold a matching
 * item identity. Similarity, classifier facets, empty-chest fallback, and
 * remote chests are intentionally ignored so deposit never invents
 * organization the player has not taught or already materialized. Highest
 * affinity score wins, content-only chests sort after affinity chests, and
 * stable storage-id ordering breaks remaining ties.
 */
public final class DepositPlanner {
    @FunctionalInterface
    public interface ChestContentPresence {
        boolean contains(ClaimedChest chest, ItemIdentity identity);
    }

    @FunctionalInterface
    public interface ChestEligibility {
        boolean isEligible(ClaimedChest chest);
    }

    private static final ChestContentPresence NO_CONTENT_PRESENCE = (chest, identity) -> false;
    private static final ChestEligibility ALL_CHESTS_ELIGIBLE = chest -> true;

    private DepositPlanner() {
    }

    public static DepositPlan plan(
            InventoryAuthoritySnapshot authority,
            ChestAffinityMap affinityMap,
            ClaimedChestMap claimedChestMap,
            Set<String> proximateStorageIds
    ) {
        return plan(authority, affinityMap, claimedChestMap, proximateStorageIds, null);
    }

    /**
     * Full overload with kit / desired-count protection.
     * {@code reservedCountResolver} returns the number of items of an
     * identity that must remain in carry (active-kit page slot reservations
     * + resolved desired count). The planner caps each identity's
     * deposited total at {@code totalCarried - reserved}, so an item the
     * player needs for an active kit or with a non-zero desired count
     * stays in carry. Pass {@code null} to disable protection (deposit
     * everything matchable, the pre-protection behavior).
     */
    public static DepositPlan plan(
            InventoryAuthoritySnapshot authority,
            ChestAffinityMap affinityMap,
            ClaimedChestMap claimedChestMap,
            Set<String> proximateStorageIds,
            ToIntFunction<ItemIdentity> reservedCountResolver
    ) {
        return plan(authority, affinityMap, claimedChestMap, proximateStorageIds, reservedCountResolver, null);
    }

    /**
     * Full overload with kit / desired-count protection and live chest
     * contents. {@code chestContentPresence} is a deliberately narrow
     * source of truth: it can say "this proximate claimed chest already
     * contains this item identity", but cannot rank by similarity,
     * emptiness, or any other fallback.
     */
    public static DepositPlan plan(
            InventoryAuthoritySnapshot authority,
            ChestAffinityMap affinityMap,
            ClaimedChestMap claimedChestMap,
            Set<String> proximateStorageIds,
            ToIntFunction<ItemIdentity> reservedCountResolver,
            ChestContentPresence chestContentPresence
    ) {
        return plan(authority, affinityMap, claimedChestMap, proximateStorageIds,
                reservedCountResolver, chestContentPresence, null);
    }

    public static DepositPlan plan(
            InventoryAuthoritySnapshot authority,
            ChestAffinityMap affinityMap,
            ClaimedChestMap claimedChestMap,
            Set<String> proximateStorageIds,
            ToIntFunction<ItemIdentity> reservedCountResolver,
            ChestContentPresence chestContentPresence,
            ChestEligibility chestEligibility
    ) {
        if (authority == null || claimedChestMap == null) {
            return DepositPlan.empty();
        }
        Set<String> proximate = proximateStorageIds == null ? Set.of() : proximateStorageIds;
        if (proximate.isEmpty()) {
            return DepositPlan.empty();
        }

        List<InventorySourceDescriptor> declaredCarried = authority.carriedSources();
        List<String> sourceIds = declaredCarried.isEmpty()
                ? List.copyOf(authority.sourcesById().keySet())
                : declaredCarried.stream().map(InventorySourceDescriptor::id).toList();

        // Tally carried totals per identity so the protection cap is applied
        // across all carry sources, not slot-by-slot.
        LinkedHashMap<ItemIdentity, Integer> carriedTotals = new LinkedHashMap<>();
        for (String sourceId : sourceIds) {
            for (InventoryEntrySnapshot entry : authority.entries(sourceId)) {
                if (entry == null || !entry.present()) {
                    continue;
                }
                ItemIdentity identity = ItemIdentityMatcher.create(entry.stack());
                carriedTotals.merge(identity, entry.count(), Integer::sum);
            }
        }
        // Per-identity remaining-depositable budget. Walks slots and
        // decrements as we allocate; once 0, further slots holding the
        // same identity are skipped.
        LinkedHashMap<ItemIdentity, Integer> depositBudget = new LinkedHashMap<>();
        for (Map.Entry<ItemIdentity, Integer> e : carriedTotals.entrySet()) {
            int reserved = reservedCountResolver == null ? 0
                    : Math.max(0, reservedCountResolver.applyAsInt(e.getKey()));
            depositBudget.put(e.getKey(), Math.max(0, e.getValue() - reserved));
        }

        ArrayList<DepositPlan.Assignment> assignments = new ArrayList<>();
        for (String sourceId : sourceIds) {
            for (InventoryEntrySnapshot entry : authority.entries(sourceId)) {
                if (entry == null || !entry.present()) {
                    continue;
                }
                ItemIdentity identity = ItemIdentityMatcher.create(entry.stack());
                int remaining = depositBudget.getOrDefault(identity, 0);
                if (remaining <= 0) {
                    continue;
                }
                int allocated = Math.min(entry.count(), remaining);
                if (allocated <= 0) {
                    continue;
                }
                List<String> candidates = rankCandidates(
                        identity, claimedChestMap, affinityMap, proximate, chestContentPresence, chestEligibility);
                if (candidates.isEmpty()) {
                    continue;
                }
                depositBudget.put(identity, remaining - allocated);
                assignments.add(new DepositPlan.Assignment(
                        sourceId,
                        entry.slotIndex(),
                        identity.itemId(),
                        allocated,
                        candidates
                ));
            }
        }
        return new DepositPlan(assignments);
    }

    /**
     * Rank proximate claimed chests for {@code identity} by direct learned
     * affinity or existing contents. Returns storage UUIDs in descending
     * preference order. Public so callers outside the planner (e.g., the
     * cursor smart-deposit path) share the exact same routing rules and
     * never drift.
     */
    public static List<UUID> rankChestsForIdentity(
            ItemIdentity identity,
            ClaimedChestMap claimedChestMap,
            ChestAffinityMap affinityMap,
            Set<String> proximateStorageIds
    ) {
        return rankChestsForIdentity(identity, claimedChestMap, affinityMap, proximateStorageIds, null);
    }

    public static List<UUID> rankChestsForIdentity(
            ItemIdentity identity,
            ClaimedChestMap claimedChestMap,
            ChestAffinityMap affinityMap,
            Set<String> proximateStorageIds,
            ChestContentPresence chestContentPresence
    ) {
        return rankChestsForIdentity(
                identity, claimedChestMap, affinityMap, proximateStorageIds, chestContentPresence, null);
    }

    public static List<UUID> rankChestsForIdentity(
            ItemIdentity identity,
            ClaimedChestMap claimedChestMap,
            ChestAffinityMap affinityMap,
            Set<String> proximateStorageIds,
            ChestContentPresence chestContentPresence,
            ChestEligibility chestEligibility
    ) {
        if (identity == null || claimedChestMap == null) {
            return List.of();
        }
        Set<String> proximate = proximateStorageIds == null ? Set.of() : proximateStorageIds;
        if (proximate.isEmpty()) {
            return List.of();
        }
        ChestContentPresence contentPresence = chestContentPresence == null
                ? NO_CONTENT_PRESENCE
                : chestContentPresence;
        ChestEligibility eligibility = chestEligibility == null
                ? ALL_CHESTS_ELIGIBLE
                : chestEligibility;
        record Candidate(UUID storageId, int score, boolean containsIdentity) {
        }
        ArrayList<Candidate> ranked = new ArrayList<>();
        for (ClaimedChest chest : claimedChestMap.chests()) {
            if (chest == null) {
                continue;
            }
            UUID storageUuid = chest.storageId();
            if (!proximate.contains(storageUuid.toString())) {
                continue;
            }
            if (!eligibility.isEligible(chest)) {
                continue;
            }
            int score = affinityMap == null ? 0 : affinityMap.score(storageUuid, identity);
            boolean containsIdentity = contentPresence.contains(chest, identity);
            if (score <= 0 && !containsIdentity) {
                continue;
            }
            ranked.add(new Candidate(storageUuid, score, containsIdentity));
        }
        if (ranked.isEmpty()) {
            return List.of();
        }
        ranked.sort(Comparator
                .<Candidate>comparingInt(c -> -c.score())
                .thenComparing(c -> !c.containsIdentity())
                .thenComparing(c -> c.storageId().toString()));
        ArrayList<UUID> ids = new ArrayList<>(ranked.size());
        for (Candidate candidate : ranked) {
            ids.add(candidate.storageId());
        }
        return List.copyOf(ids);
    }

    /**
     * Explicit single-identity deposit ranking. This is intentionally the
     * same affinity/content rule as bulk deposit: a per-item gesture may
     * choose how much to deposit, but it still must not choose a chest by
     * inferred similarity or emptiness.
     */
    public static List<UUID> rankChestsForExplicitDeposit(
            ItemIdentity identity,
            ClaimedChestMap claimedChestMap,
            ChestAffinityMap affinityMap,
            Set<String> proximateStorageIds
    ) {
        return rankChestsForIdentity(identity, claimedChestMap, affinityMap, proximateStorageIds);
    }

    public static List<UUID> rankChestsForExplicitDeposit(
            ItemIdentity identity,
            ClaimedChestMap claimedChestMap,
            ChestAffinityMap affinityMap,
            Set<String> proximateStorageIds,
            ChestContentPresence chestContentPresence
    ) {
        return rankChestsForIdentity(
                identity, claimedChestMap, affinityMap, proximateStorageIds, chestContentPresence);
    }

    public static List<UUID> rankChestsForExplicitDeposit(
            ItemIdentity identity,
            ClaimedChestMap claimedChestMap,
            ChestAffinityMap affinityMap,
            Set<String> proximateStorageIds,
            ChestContentPresence chestContentPresence,
            ChestEligibility chestEligibility
    ) {
        return rankChestsForIdentity(
                identity, claimedChestMap, affinityMap, proximateStorageIds,
                chestContentPresence, chestEligibility);
    }

    /**
     * Rank proximate claimed chests for {@code identity} by direct
     * affinity score (descending). Returns the chest objects so callers
     * can pass them straight to {@code TakeAllExecutor.takeByIdentity}.
     * Public so the four take-by-identity call sites (Gather button,
     * cursor smart-pull, kit auto-fetch, take-one/stack RPCs) share the
     * exact same ranking and never drift.
     *
     * <p>Affinity-only — no facet or presence tier. Take semantics
     * differ from deposit: a chest the player has never deposited into
     * but happens to hold the identity is still pulled from after the
     * affinity-ranked walk runs out of matches at each call site, since
     * {@code TakeAllExecutor.takeByIdentity} is a no-op when the chest
     * doesn't contain the identity.
     */
    public static List<ClaimedChest> rankProximateChestsForTake(
            ItemIdentity identity,
            ClaimedChestMap claimedChestMap,
            ChestAffinityMap affinityMap,
            Set<String> proximateStorageIds
    ) {
        if (identity == null || claimedChestMap == null || affinityMap == null) {
            return List.of();
        }
        Set<String> proximate = proximateStorageIds == null ? Set.of() : proximateStorageIds;
        if (proximate.isEmpty()) {
            return List.of();
        }
        ArrayList<ClaimedChest> ranked = new ArrayList<>();
        for (ClaimedChest chest : claimedChestMap.chests()) {
            if (chest == null) {
                continue;
            }
            if (!proximate.contains(chest.storageId().toString())) {
                continue;
            }
            ranked.add(chest);
        }
        ranked.sort((a, b) -> Integer.compare(
                affinityMap.score(b.storageId(), identity),
                affinityMap.score(a.storageId(), identity)));
        return List.copyOf(ranked);
    }

    private static List<String> rankCandidates(
            ItemIdentity identity,
            ClaimedChestMap claimedChestMap,
            ChestAffinityMap affinityMap,
            Set<String> proximate,
            ChestContentPresence chestContentPresence,
            ChestEligibility chestEligibility
    ) {
        List<UUID> uuids = rankChestsForIdentity(
                identity, claimedChestMap, affinityMap, proximate, chestContentPresence, chestEligibility);
        if (uuids.isEmpty()) {
            return List.of();
        }
        ArrayList<String> ids = new ArrayList<>(uuids.size());
        for (UUID uuid : uuids) {
            ids.add(uuid.toString());
        }
        return List.copyOf(ids);
    }
}
