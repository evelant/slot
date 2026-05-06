package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.inventory.triage.IslandSignalDescriptor;
import dev.imagio.slot.inventory.triage.LearnedAdjacencyKey;
import dev.imagio.slot.workflow.domain.ChestAffinity;
import dev.imagio.slot.workflow.domain.ChestAffinityMap;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.ClaimedChestMap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.ToIntFunction;

/**
 * Affinity-driven deposit routing.
 *
 * <p>For each carried-pane item, find proximate claimed chests in three
 * tiers:
 * <ol>
 *   <li><b>Direct affinity</b> — {@code affinity[chest, identity] > 0}.
 *       Highest score wins (ties broken by stable storage-id ordering).</li>
 *   <li><b>Facet-similar affinity</b> — when no direct bond exists, sum
 *       affinity over chest residents that share a learned-rule
 *       adjacency key with the carried identity (tag, material_family,
 *       subsystem, dye_color, namespace, creative_tab). A brand-new
 *       netherite_ingot deposits into the "Mining" chest because it
 *       shares c:ingots / material_family / namespace with the iron and
 *       gold ingots already there. This requires a {@code descriptorLookup}
 *       that maps stored identities back to their facet descriptors.</li>
 *   <li><b>Presence</b> — the chest already holds at least one stack of
 *       this identity right now. Covers shared-world multiplayer (other
 *       players organised the chest without using SLOT) and any chest
 *       seeded with content the player hasn't personally deposited
 *       into yet. Requires a {@code chestContentsLookup} that returns
 *       the set of identities currently in a given chest.</li>
 * </ol>
 * Direct &gt; facet &gt; presence. Items still with no signal anywhere
 * stay in carry.
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
        return plan(authority, affinityMap, claimedChestMap, proximateStorageIds, null, null, null);
    }

    /**
     * Cluster-aware overload. {@code descriptorLookup} maps an
     * {@link ItemIdentity} to its {@link IslandSignalDescriptor} so the
     * facet-affinity fallback can fire. Pass {@code null} to disable
     * the fallback (legacy behavior — exact-match identity affinity
     * only).
     */
    public static DepositPlan plan(
            InventoryAuthoritySnapshot authority,
            ChestAffinityMap affinityMap,
            ClaimedChestMap claimedChestMap,
            Set<String> proximateStorageIds,
            Function<ItemIdentity, IslandSignalDescriptor> descriptorLookup
    ) {
        return plan(authority, affinityMap, claimedChestMap, proximateStorageIds,
                descriptorLookup, null, null);
    }

    /**
     * Overload with the presence fallback. {@code chestContentsLookup}
     * returns the set of identities currently present in a chest, keyed
     * by storage UUID. Pass {@code null} to disable the presence tier.
     */
    public static DepositPlan plan(
            InventoryAuthoritySnapshot authority,
            ChestAffinityMap affinityMap,
            ClaimedChestMap claimedChestMap,
            Set<String> proximateStorageIds,
            Function<ItemIdentity, IslandSignalDescriptor> descriptorLookup,
            Function<UUID, Set<ItemIdentity>> chestContentsLookup
    ) {
        return plan(authority, affinityMap, claimedChestMap, proximateStorageIds,
                descriptorLookup, chestContentsLookup, null);
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
            Function<ItemIdentity, IslandSignalDescriptor> descriptorLookup,
            Function<UUID, Set<ItemIdentity>> chestContentsLookup,
            ToIntFunction<ItemIdentity> reservedCountResolver
    ) {
        if (authority == null || affinityMap == null || claimedChestMap == null) {
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
                        identity, claimedChestMap, affinityMap, proximate,
                        descriptorLookup, chestContentsLookup);
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
     * Rank proximate claimed chests for {@code identity} across three
     * tiers: direct affinity, facet-affinity fallback, and presence
     * (chest already holds the identity). Returns storage UUIDs in
     * descending preference order. Public so callers outside the
     * planner (e.g., the cursor smart-deposit path) share the exact
     * same routing rules and never drift.
     *
     * @param chestContentsLookup nullable; when {@code null} the
     *     presence tier is skipped.
     */
    public static List<UUID> rankChestsForIdentity(
            ItemIdentity identity,
            ClaimedChestMap claimedChestMap,
            ChestAffinityMap affinityMap,
            Set<String> proximateStorageIds,
            Function<ItemIdentity, IslandSignalDescriptor> descriptorLookup,
            Function<UUID, Set<ItemIdentity>> chestContentsLookup
    ) {
        if (identity == null || claimedChestMap == null || affinityMap == null) {
            return List.of();
        }
        Set<String> proximate = proximateStorageIds == null ? Set.of() : proximateStorageIds;
        if (proximate.isEmpty()) {
            return List.of();
        }
        record Candidate(UUID storageId, int directScore, int facetScore, boolean present) {
        }
        Set<LearnedAdjacencyKey> targetKeys = targetAdjacencyKeys(identity, descriptorLookup);
        ArrayList<Candidate> ranked = new ArrayList<>();
        for (ClaimedChest chest : claimedChestMap.chests()) {
            if (chest == null) {
                continue;
            }
            UUID storageUuid = chest.storageId();
            if (!proximate.contains(storageUuid.toString())) {
                continue;
            }
            int directScore = affinityMap.score(storageUuid, identity);
            int facetScore = 0;
            if (directScore <= 0 && !targetKeys.isEmpty()) {
                facetScore = facetAffinityScore(
                        storageUuid, affinityMap, targetKeys, descriptorLookup);
            }
            boolean present = false;
            if (directScore <= 0 && facetScore <= 0 && chestContentsLookup != null) {
                Set<ItemIdentity> contents = chestContentsLookup.apply(storageUuid);
                present = contents != null && contents.contains(identity);
            }
            if (directScore <= 0 && facetScore <= 0 && !present) {
                continue;
            }
            ranked.add(new Candidate(storageUuid, directScore, facetScore, present));
        }
        if (ranked.isEmpty()) {
            return List.of();
        }
        // Tiered ordering: direct > facet > presence. Within a tier,
        // higher score wins; storage id breaks ties.
        ranked.sort(Comparator
                .<Candidate>comparingInt(c -> {
                    if (c.directScore() > 0) return 0;
                    if (c.facetScore() > 0) return 1;
                    return 2;
                })
                .thenComparing(Comparator.<Candidate>comparingInt(c -> -c.directScore()))
                .thenComparing(Comparator.<Candidate>comparingInt(c -> -c.facetScore()))
                .thenComparing(c -> c.storageId().toString()));
        ArrayList<UUID> ids = new ArrayList<>(ranked.size());
        for (Candidate candidate : ranked) {
            ids.add(candidate.storageId());
        }
        return List.copyOf(ids);
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
            Function<ItemIdentity, IslandSignalDescriptor> descriptorLookup,
            Function<UUID, Set<ItemIdentity>> chestContentsLookup
    ) {
        List<UUID> uuids = rankChestsForIdentity(
                identity, claimedChestMap, affinityMap, proximate,
                descriptorLookup, chestContentsLookup);
        if (uuids.isEmpty()) {
            return List.of();
        }
        ArrayList<String> ids = new ArrayList<>(uuids.size());
        for (UUID uuid : uuids) {
            ids.add(uuid.toString());
        }
        return List.copyOf(ids);
    }

    private static Set<LearnedAdjacencyKey> targetAdjacencyKeys(
            ItemIdentity identity,
            Function<ItemIdentity, IslandSignalDescriptor> descriptorLookup
    ) {
        if (descriptorLookup == null) {
            return Set.of();
        }
        IslandSignalDescriptor descriptor = descriptorLookup.apply(identity);
        if (descriptor == null) {
            return Set.of();
        }
        return new LinkedHashSet<>(LearnedAdjacencyKey.keysFor(descriptor));
    }

    private static int facetAffinityScore(
            UUID storageId,
            ChestAffinityMap affinityMap,
            Set<LearnedAdjacencyKey> targetKeys,
            Function<ItemIdentity, IslandSignalDescriptor> descriptorLookup
    ) {
        Map<ItemIdentity, ChestAffinity> bonds = affinityMap.forChest(storageId);
        if (bonds.isEmpty()) {
            return 0;
        }
        int total = 0;
        for (Map.Entry<ItemIdentity, ChestAffinity> bond : bonds.entrySet()) {
            ItemIdentity other = bond.getKey();
            IslandSignalDescriptor otherDescriptor = descriptorLookup.apply(other);
            if (otherDescriptor == null) {
                continue;
            }
            for (LearnedAdjacencyKey key : LearnedAdjacencyKey.keysFor(otherDescriptor)) {
                if (targetKeys.contains(key)) {
                    total += bond.getValue().score();
                    break;
                }
            }
        }
        return total;
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
