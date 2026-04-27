package dev.imagio.slot.inventory.triage;

import java.util.Objects;

public record LearnedAdjacencyKey(Kind kind, String value) implements Comparable<LearnedAdjacencyKey> {
    public LearnedAdjacencyKey {
        Objects.requireNonNull(kind, "kind");
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }
    }

    public static LearnedAdjacencyKey tag(String tagId) {
        return new LearnedAdjacencyKey(Kind.TAG, tagId);
    }

    public static LearnedAdjacencyKey materialFamily(String value) {
        return new LearnedAdjacencyKey(Kind.MATERIAL_FAMILY, value);
    }

    public static LearnedAdjacencyKey namespace(String namespace) {
        return new LearnedAdjacencyKey(Kind.NAMESPACE, namespace);
    }

    public static LearnedAdjacencyKey creativeTab(String tabId) {
        return new LearnedAdjacencyKey(Kind.CREATIVE_TAB, tabId);
    }

    public int priorityRank() {
        return kind.priorityRank;
    }

    @Override
    public int compareTo(LearnedAdjacencyKey other) {
        int priorityCompare = Integer.compare(priorityRank(), other.priorityRank());
        if (priorityCompare != 0) {
            return priorityCompare;
        }
        return value.compareTo(other.value);
    }

    public enum Kind {
        TAG(0),
        // FacetIndex `material_family` (e.g. wood_birch, iron, copper).
        // Same priority as TAG: both are strong specific signals. Tags are
        // game-authored; material_family is dataset-authored; we want
        // either to fire a learned rule when N≥2 confirmations land.
        MATERIAL_FAMILY(0),
        NAMESPACE(1),
        CREATIVE_TAB(2);

        private final int priorityRank;

        Kind(int priorityRank) {
            this.priorityRank = priorityRank;
        }
    }
}
