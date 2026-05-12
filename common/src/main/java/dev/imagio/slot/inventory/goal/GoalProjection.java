package dev.imagio.slot.inventory.goal;

import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record GoalProjection(
        String goalId,
        String label,
        int targetCount,
        GoalProjectionStatus status,
        List<GoalRequirement> requirements,
        List<GoalChoiceRequirement> choices,
        List<GoalProjectionEntry> entries,
        Map<ItemIdentity, Integer> wantedCounts,
        List<String> diagnostics
) {
    public GoalProjection {
        goalId = goalId == null || goalId.isBlank() ? "goal" : goalId.trim();
        label = label == null || label.isBlank() ? goalId : label.trim();
        targetCount = Math.max(0, targetCount);
        status = status == null ? GoalProjectionStatus.READY : status;
        requirements = copyRequirements(requirements);
        choices = copyChoices(choices);
        entries = copyEntries(entries);
        wantedCounts = copyWantedCounts(wantedCounts);
        diagnostics = copyStrings(diagnostics);
        if (!diagnostics.isEmpty() && status == GoalProjectionStatus.READY) {
            status = GoalProjectionStatus.READY_WITH_DIAGNOSTICS;
        }
    }

    public String diagnosticsString() {
        return String.join(",", diagnostics);
    }

    private static List<GoalRequirement> copyRequirements(List<GoalRequirement> source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        ArrayList<GoalRequirement> copy = new ArrayList<>(source.size());
        for (GoalRequirement requirement : source) {
            if (requirement != null) {
                copy.add(requirement);
            }
        }
        return List.copyOf(copy);
    }

    private static List<GoalChoiceRequirement> copyChoices(List<GoalChoiceRequirement> source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        ArrayList<GoalChoiceRequirement> copy = new ArrayList<>(source.size());
        for (GoalChoiceRequirement choice : source) {
            if (choice != null) {
                copy.add(choice);
            }
        }
        return List.copyOf(copy);
    }

    private static List<GoalProjectionEntry> copyEntries(List<GoalProjectionEntry> source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        ArrayList<GoalProjectionEntry> copy = new ArrayList<>(source.size());
        for (GoalProjectionEntry entry : source) {
            if (entry != null) {
                copy.add(entry);
            }
        }
        return List.copyOf(copy);
    }

    private static Map<ItemIdentity, Integer> copyWantedCounts(Map<ItemIdentity, Integer> source) {
        LinkedHashMap<ItemIdentity, Integer> copy = new LinkedHashMap<>();
        if (source != null) {
            for (Map.Entry<ItemIdentity, Integer> entry : source.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null && entry.getValue() > 0) {
                    copy.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return Collections.unmodifiableMap(copy);
    }

    private static List<String> copyStrings(List<String> source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        ArrayList<String> copy = new ArrayList<>(source.size());
        for (String value : source) {
            if (value != null && !value.isBlank()) {
                copy.add(value.trim());
            }
        }
        return List.copyOf(copy);
    }
}
