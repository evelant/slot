package dev.imagio.slot.workflow.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Pure spatial clustering of claimed chests.
 *
 * <p>Two chests in the same dimension are in the same cluster iff their
 * primary anchors are within {@link #DEFAULT_THRESHOLD_BLOCKS} blocks
 * (Euclidean distance). Cluster IDs are derived from the lexicographically
 * smallest {@code storageId} in the cluster, so the same chest set always
 * produces the same cluster IDs across runs.
 *
 * <p>Stickiness across topology change is best-effort: as long as the
 * smallest-id chest in a cluster doesn't get deleted or merged into a
 * different cluster, the cluster id is stable. The plan
 * (docs/plans/learned-storage.md) describes a stronger
 * "ordinal sticks with the larger half of a split" rule; that is deferred.
 */
public record ChestClusterMap(
        Map<UUID, String> clusterIdByStorageId,
        List<Cluster> clusters
) {
    public static final double DEFAULT_THRESHOLD_BLOCKS = 16.0;

    public ChestClusterMap {
        clusterIdByStorageId = clusterIdByStorageId == null ? Map.of() : Map.copyOf(clusterIdByStorageId);
        clusters = clusters == null ? List.of() : List.copyOf(clusters);
    }

    public static ChestClusterMap empty() {
        return new ChestClusterMap(Map.of(), List.of());
    }

    public static ChestClusterMap derive(ClaimedChestMap chests) {
        return derive(chests, DEFAULT_THRESHOLD_BLOCKS);
    }

    public static ChestClusterMap derive(ClaimedChestMap chests, double thresholdBlocks) {
        if (chests == null || chests.chests().isEmpty()) {
            return empty();
        }
        double thresholdSquared = Math.max(0.0, thresholdBlocks) * Math.max(0.0, thresholdBlocks);

        // Stable iteration order so cluster id selection is deterministic.
        ArrayList<ClaimedChest> sorted = new ArrayList<>(chests.chests());
        sorted.sort(Comparator.comparing(c -> c.storageId().toString()));

        // Union-find over indices.
        int n = sorted.size();
        int[] parent = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
        }
        for (int i = 0; i < n; i++) {
            ChestAnchor a = primaryAnchor(sorted.get(i));
            if (a == null) {
                continue;
            }
            for (int j = i + 1; j < n; j++) {
                ChestAnchor b = primaryAnchor(sorted.get(j));
                if (b == null || !a.dimensionId().equals(b.dimensionId())) {
                    continue;
                }
                long dx = (long) a.x() - b.x();
                long dy = (long) a.y() - b.y();
                long dz = (long) a.z() - b.z();
                double dsq = dx * dx + dy * dy + dz * dz;
                if (dsq <= thresholdSquared) {
                    union(parent, i, j);
                }
            }
        }

        // Group chests by cluster root, then assign each cluster an id from its
        // smallest-string storageId.
        Map<Integer, List<ClaimedChest>> byRoot = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            int root = find(parent, i);
            byRoot.computeIfAbsent(root, k -> new ArrayList<>()).add(sorted.get(i));
        }

        // Sort clusters by the smallest-id chest within them so cluster ordering
        // is itself deterministic across runs.
        List<List<ClaimedChest>> clusterMembers = new ArrayList<>(byRoot.values());
        clusterMembers.sort(Comparator.comparing(group -> group.get(0).storageId().toString()));

        LinkedHashMap<UUID, String> idByStorage = new LinkedHashMap<>();
        ArrayList<Cluster> clusters = new ArrayList<>();
        int ordinal = 1;
        for (List<ClaimedChest> members : clusterMembers) {
            UUID anchorId = members.get(0).storageId();
            String clusterId = "cluster-" + anchorId;
            String defaultLabel = "Storage Area " + ordinal;
            LinkedHashSet<UUID> ids = new LinkedHashSet<>();
            for (ClaimedChest member : members) {
                ids.add(member.storageId());
                idByStorage.put(member.storageId(), clusterId);
            }
            clusters.add(new Cluster(clusterId, defaultLabel, ordinal, List.copyOf(ids)));
            ordinal++;
        }
        return new ChestClusterMap(Map.copyOf(idByStorage), List.copyOf(clusters));
    }

    public String clusterId(UUID storageId) {
        if (storageId == null) {
            return null;
        }
        return clusterIdByStorageId.get(storageId);
    }

    public Cluster cluster(String clusterId) {
        if (clusterId == null) {
            return null;
        }
        for (Cluster cluster : clusters) {
            if (clusterId.equals(cluster.clusterId())) {
                return cluster;
            }
        }
        return null;
    }

    private static ChestAnchor primaryAnchor(ClaimedChest chest) {
        if (chest == null || chest.anchors().isEmpty()) {
            return null;
        }
        return chest.anchors().iterator().next();
    }

    private static int find(int[] parent, int i) {
        while (parent[i] != i) {
            parent[i] = parent[parent[i]];
            i = parent[i];
        }
        return i;
    }

    private static void union(int[] parent, int a, int b) {
        int ra = find(parent, a);
        int rb = find(parent, b);
        if (ra == rb) {
            return;
        }
        // Attach larger-index root to smaller-index root so the smallest
        // member of each cluster ends up at the root.
        if (ra < rb) {
            parent[rb] = ra;
        } else {
            parent[ra] = rb;
        }
    }

    public record Cluster(String clusterId, String defaultLabel, int ordinal, List<UUID> storageIds) {
        public Cluster {
            clusterId = clusterId == null ? "" : clusterId;
            defaultLabel = defaultLabel == null ? "" : defaultLabel;
            ordinal = Math.max(1, ordinal);
            storageIds = storageIds == null ? List.of() : List.copyOf(storageIds);
        }
    }
}
