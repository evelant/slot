package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record VisualHomeMap(
        List<VisualAtlasIsland> playerIslands,
        Map<ItemIdentity, VisualHomeAssignment> assignments
) {
    public VisualHomeMap {
        playerIslands = copyIslands(playerIslands);
        assignments = copyAssignments(assignments);
    }

    public static VisualHomeMap empty() {
        return new VisualHomeMap(List.of(), Map.of());
    }

    public VisualAtlasIsland island(String islandId) {
        if (islandId == null || islandId.isBlank()) {
            return null;
        }
        return playerIslands.stream()
                .filter(island -> island != null && island.id().equals(islandId))
                .findFirst()
                .orElse(null);
    }

    public VisualHomeAssignment assignment(ItemIdentity identity) {
        return identity == null ? null : assignments.get(identity);
    }

    public static List<VisualAtlasIsland> copyIslands(List<VisualAtlasIsland> source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        ArrayList<VisualAtlasIsland> copied = new ArrayList<>();
        for (VisualAtlasIsland island : source) {
            if (island != null) {
                copied.add(island);
            }
        }
        return List.copyOf(copied);
    }

    public static Map<ItemIdentity, VisualHomeAssignment> copyAssignments(
            Map<ItemIdentity, VisualHomeAssignment> source
    ) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<ItemIdentity, VisualHomeAssignment> copied = new LinkedHashMap<>();
        source.forEach((key, assignment) -> {
            ItemIdentity identity = key != null ? key : assignment == null ? null : assignment.identity();
            if (identity == null || assignment == null) {
                return;
            }
            copied.put(identity, new VisualHomeAssignment(
                    identity,
                    assignment.islandId(),
                    assignment.localX(),
                    assignment.localY(),
                    assignment.origin(),
                    assignment.locked()
            ));
        });
        return Map.copyOf(copied);
    }
}
