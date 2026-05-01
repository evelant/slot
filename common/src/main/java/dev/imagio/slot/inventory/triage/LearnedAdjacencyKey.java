package dev.imagio.slot.inventory.triage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record LearnedAdjacencyKey(Kind kind, String value) implements Comparable<LearnedAdjacencyKey> {

    /**
     * Same broad-namespace blacklist
     * {@link LearnedIslandRuleStore} uses internally — exposed so
     * other consumers (e.g. {@code DepositPlanner}'s facet-affinity
     * fallback) can build the exact same adjacency-key set without
     * importing the rule store.
     */
    private static final Set<String> OVERLY_BROAD_NAMESPACES = Set.of(
            "minecraft",
            "c",
            "forge",
            "neoforge"
    );

    public LearnedAdjacencyKey {
        Objects.requireNonNull(kind, "kind");
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }
    }

    /**
     * Build the canonical adjacency-key list for an
     * {@link IslandSignalDescriptor}. Single source of truth — both
     * {@link LearnedIslandRuleStore} (for chip suggestions) and
     * {@code DepositPlanner} (for facet-affinity deposit fallback) call
     * this so a player's "items I home together" learnings line up
     * with their "items I deposit together" learnings.
     */
    public static List<LearnedAdjacencyKey> keysFor(IslandSignalDescriptor descriptor) {
        if (descriptor == null) {
            return List.of();
        }
        ArrayList<LearnedAdjacencyKey> keys = new ArrayList<>();
        for (String tagId : descriptor.itemTags()) {
            if (tagId != null && !tagId.isBlank()) {
                keys.add(tag(tagId));
            }
        }
        String materialFamily = descriptor.materialFamily();
        if (materialFamily != null && !materialFamily.isBlank()) {
            keys.add(materialFamily(materialFamily));
        }
        for (String subsystem : descriptor.subsystems()) {
            if (subsystem != null && !subsystem.isBlank()) {
                keys.add(subsystem(subsystem));
            }
        }
        String dyeColor = descriptor.dyeColor();
        if (dyeColor != null && !dyeColor.isBlank()) {
            keys.add(dyeColor(dyeColor));
        }
        String namespace = descriptor.namespace();
        if (namespace != null && !namespace.isBlank()
                && !OVERLY_BROAD_NAMESPACES.contains(namespace)) {
            keys.add(namespace(namespace));
        }
        String creativeTabId = descriptor.creativeTabId();
        if (creativeTabId != null && !creativeTabId.isBlank()) {
            keys.add(creativeTab(creativeTabId));
        }
        return List.copyOf(keys);
    }

    public static LearnedAdjacencyKey tag(String tagId) {
        return new LearnedAdjacencyKey(Kind.TAG, tagId);
    }

    public static LearnedAdjacencyKey materialFamily(String value) {
        return new LearnedAdjacencyKey(Kind.MATERIAL_FAMILY, value);
    }

    public static LearnedAdjacencyKey subsystem(String subsystemId) {
        return new LearnedAdjacencyKey(Kind.SUBSYSTEM, subsystemId);
    }

    public static LearnedAdjacencyKey dyeColor(String dyeColor) {
        return new LearnedAdjacencyKey(Kind.DYE_COLOR, dyeColor);
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
        // FacetIndex `mod_subsystem` (e.g. create:mechanical_power,
        // create:logistics). Same strength as TAG / MATERIAL_FAMILY:
        // a player who homes two cogwheels to "Create Workshop" almost
        // certainly wants the third cogwheel chip to suggest the same
        // island. Subsystem-primary template matching covers the *first*
        // placement; this learned-rule key covers the player's manual
        // override that diverges from the template default.
        SUBSYSTEM(0),
        // FacetIndex `dye_color` (e.g. white, light_gray, gray, black).
        // Strong intent signal when a player builds a color-themed
        // island (e.g. "White Decoration") — a fourth white-prefix
        // identity should suggest the same place after two have landed
        // there. Constrained to 16 canonical values so the false-match
        // surface is bounded.
        DYE_COLOR(0),
        NAMESPACE(1),
        CREATIVE_TAB(2);

        private final int priorityRank;

        Kind(int priorityRank) {
            this.priorityRank = priorityRank;
        }
    }
}
