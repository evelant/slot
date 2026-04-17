package dev.imagio.slot.inventory.triage;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import dev.imagio.slot.inventory.core.ItemIdentity;

public record LearnedIslandRule(
        LearnedAdjacencyKey adjacency,
        String islandId,
        Set<ItemIdentity> confirmingIdentities,
        long lastConfirmedAtEpochMillis
) {
    public LearnedIslandRule {
        Objects.requireNonNull(adjacency, "adjacency");
        Objects.requireNonNull(islandId, "islandId");
        if (islandId.isBlank()) {
            throw new IllegalArgumentException("islandId must not be blank");
        }
        confirmingIdentities = confirmingIdentities == null
                ? Set.of()
                : Set.copyOf(new LinkedHashSet<>(confirmingIdentities));
    }

    public int confirmations() {
        return confirmingIdentities.size();
    }
}
