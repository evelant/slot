package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityCollections;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record VisualHomeMap(
        List<VisualAtlasIsland> playerIslands,
        Map<ItemIdentity, VisualHomeAssignment> assignments,
        Set<String> dismissedTemplateIds
) {
    public VisualHomeMap {
        playerIslands = copyIslands(playerIslands);
        assignments = copyAssignments(assignments);
        dismissedTemplateIds = copyDismissedTemplates(dismissedTemplateIds);
    }

    public VisualHomeMap(
            List<VisualAtlasIsland> playerIslands,
            Map<ItemIdentity, VisualHomeAssignment> assignments
    ) {
        this(playerIslands, assignments, Set.of());
    }

    public static VisualHomeMap empty() {
        return new VisualHomeMap(List.of(), Map.of(), Set.of());
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
        return ItemIdentityCollections.findCanonical(assignments, identity);
    }

    public boolean templateDismissed(String templateId) {
        return templateId != null && !templateId.isBlank() && dismissedTemplateIds.contains(templateId);
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
            ItemIdentity normalized = ItemIdentityCollections.key(identity);
            copied.put(normalized, new VisualHomeAssignment(
                    normalized,
                    assignment.islandId(),
                    assignment.ordinal(),
                    assignment.origin(),
                    assignment.locked()
            ));
        });
        return Map.copyOf(copied);
    }

    public static Set<String> copyDismissedTemplates(Set<String> source) {
        if (source == null || source.isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<String> copied = new LinkedHashSet<>();
        for (String templateId : source) {
            if (templateId != null && !templateId.isBlank()) {
                copied.add(templateId);
            }
        }
        return Set.copyOf(copied);
    }
}
