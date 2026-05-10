package dev.imagio.slot.classification;

import dev.imagio.slot.inventory.triage.IslandSignal;
import dev.imagio.slot.inventory.triage.IslandSignalDescriptor;
import dev.imagio.slot.inventory.triage.IslandSuggestionTemplate;
import dev.imagio.slot.inventory.triage.IslandTemplateMatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class ClassificationInspectFormatter {

    private static final int LIST_LIMIT = 14;

    private ClassificationInspectFormatter() {
    }

    public static List<String> format(IslandSignalDescriptor descriptor) {
        if (descriptor == null || descriptor.identity() == null) {
            return List.of("[SLOT] classification inspect: no descriptor");
        }

        ArrayList<String> lines = new ArrayList<>();
        IslandSuggestionTemplate template = IslandSuggestionTemplate.firstMatchOrMisc(descriptor);
        DynamicHomeCohortPolicy cohortPolicy = DynamicHomeCohortPolicy.current();
        IslandTemplateMatch dynamicMatch = IslandSuggestionTemplate.firstMatchExtendedOrMisc(
                descriptor, cohortPolicy.qualifier());
        IslandTemplateMatch possibleSubsystemMatch = IslandSuggestionTemplate.firstMatchExtendedOrMisc(
                descriptor, id -> true);
        List<String> matchingTemplates = matchingTemplates(descriptor);

        lines.add("[SLOT] classification inspect: " + descriptor.identity().itemId());
        lines.add("  template=" + template.name() + " (" + template.defaultLabel() + ")"
                + (possibleSubsystemMatch.isSubsystem()
                ? " possible_subsystem=" + possibleSubsystemMatch.subsystemId().orElse("")
                + " label=" + possibleSubsystemMatch.label()
                : ""));
        lines.add("  auto_home_target=" + dynamicMatch.islandId()
                + " label=" + dynamicMatch.label()
                + " subsystem_counts=" + summarizeSubsystemCounts(descriptor.subsystems(), cohortPolicy));
        lines.add("  matching_templates=" + summarize(matchingTemplates));
        lines.add("  role=" + noneIfBlank(descriptor.role())
                + " alternatives=" + summarize(descriptor.roleAlternatives())
                + " material_family=" + noneIfBlank(descriptor.materialFamily())
                + " form=" + noneIfBlank(descriptor.form())
                + " emits_light=" + descriptor.emitsLight());
        lines.add("  subsystems=" + summarize(descriptor.subsystems())
                + " activities=" + summarize(descriptor.activities()));
        lines.add("  flavor=" + noneIfBlank(descriptor.flavor())
                + " carry_frequency=" + noneIfBlank(descriptor.carryFrequency())
                + " rarity=" + noneIfBlank(descriptor.rarity())
                + " origin=" + noneIfBlank(descriptor.origin()));
        lines.add("  dye_color=" + noneIfBlank(descriptor.dyeColor())
                + " palette=" + summarize(descriptor.palette()));
        lines.add("  class_signals=" + summarizeSignals(descriptor.classSignals()));
        lines.add("  item_tags=" + summarize(descriptor.itemTags()));
        return List.copyOf(lines);
    }

    private static List<String> matchingTemplates(IslandSignalDescriptor descriptor) {
        ArrayList<String> matches = new ArrayList<>();
        for (IslandSuggestionTemplate candidate : IslandSuggestionTemplate.values()) {
            if (candidate.matches(descriptor)) {
                matches.add(candidate.name());
            }
        }
        return matches;
    }

    private static String summarizeSignals(Set<IslandSignal> signals) {
        if (signals == null || signals.isEmpty()) {
            return "none";
        }
        ArrayList<String> values = new ArrayList<>();
        for (IslandSignal signal : signals) {
            values.add(signal.name());
        }
        values.sort(Comparator.naturalOrder());
        return summarize(values);
    }

    private static String summarize(Collection<String> values) {
        if (values == null || values.isEmpty()) {
            return "none";
        }
        ArrayList<String> filtered = new ArrayList<>();
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                filtered.add(value);
            }
        }
        if (filtered.isEmpty()) {
            return "none";
        }
        filtered.sort(Comparator.naturalOrder());
        if (filtered.size() <= LIST_LIMIT) {
            return String.join(",", filtered);
        }
        return String.join(",", filtered.subList(0, LIST_LIMIT)) + ",...+" + (filtered.size() - LIST_LIMIT);
    }

    private static String noneIfBlank(String value) {
        return value == null || value.isBlank() ? "none" : value;
    }

    private static String summarizeSubsystemCounts(List<String> subsystemIds, DynamicHomeCohortPolicy policy) {
        if (subsystemIds == null || subsystemIds.isEmpty() || policy == null) {
            return "none";
        }
        ArrayList<String> values = new ArrayList<>();
        for (String subsystemId : subsystemIds) {
            if (subsystemId == null || subsystemId.isBlank()) {
                continue;
            }
            values.add(subsystemId + "=" + policy.count(subsystemId) + "/" + policy.minSubsystemItems());
        }
        return values.isEmpty() ? "none" : String.join(",", values);
    }
}
