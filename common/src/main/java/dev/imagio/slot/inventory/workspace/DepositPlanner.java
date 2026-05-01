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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

/**
 * Affinity-driven deposit routing.
 *
 * <p>For each carried-pane item, find proximate claimed chests by
 * affinity. The planner ranks candidates in two tiers:
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
 * </ol>
 * Direct affinity always outranks facet affinity. Items still with no
 * affinity anywhere stay in carry.
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
        return plan(authority, affinityMap, claimedChestMap, proximateStorageIds, null);
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
                List<String> candidates = rankCandidates(
                        identity, claimedChestMap, affinityMap, proximate, descriptorLookup);
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
     * affinity score (descending), with a facet-affinity fallback for
     * chests that have no direct identity bond. Returns storage-id
     * strings.
     */
    private static List<String> rankCandidates(
            ItemIdentity identity,
            ClaimedChestMap claimedChestMap,
            ChestAffinityMap affinityMap,
            Set<String> proximate,
            Function<ItemIdentity, IslandSignalDescriptor> descriptorLookup
    ) {
        record Candidate(String storageId, int directScore, int facetScore) {
        }
        Set<LearnedAdjacencyKey> targetKeys = targetAdjacencyKeys(identity, descriptorLookup);
        ArrayList<Candidate> ranked = new ArrayList<>();
        for (ClaimedChest chest : claimedChestMap.chests()) {
            if (chest == null) {
                continue;
            }
            String idString = chest.storageId().toString();
            if (!proximate.contains(idString)) {
                continue;
            }
            int directScore = affinityMap.score(chest.storageId(), identity);
            int facetScore = 0;
            if (directScore <= 0 && !targetKeys.isEmpty()) {
                facetScore = facetAffinityScore(
                        chest.storageId(), affinityMap, targetKeys, descriptorLookup);
            }
            if (directScore <= 0 && facetScore <= 0) {
                continue;
            }
            ranked.add(new Candidate(idString, directScore, facetScore));
        }
        if (ranked.isEmpty()) {
            return List.of();
        }
        // Direct-affinity chests always outrank facet-affinity chests.
        // Within each tier, higher score wins; storage id breaks ties.
        ranked.sort(Comparator
                .<Candidate>comparingInt(c -> c.directScore() > 0 ? 0 : 1)
                .thenComparing(Comparator.<Candidate>comparingInt(c -> -c.directScore()))
                .thenComparing(Comparator.<Candidate>comparingInt(c -> -c.facetScore()))
                .thenComparing(Candidate::storageId));
        ArrayList<String> ids = new ArrayList<>(ranked.size());
        for (Candidate candidate : ranked) {
            ids.add(candidate.storageId());
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
