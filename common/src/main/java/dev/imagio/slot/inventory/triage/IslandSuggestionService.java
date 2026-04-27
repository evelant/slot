package dev.imagio.slot.inventory.triage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class IslandSuggestionService {
    public static final int MAX_TOTAL_CHIPS = 2;
    public static final int MAX_TEMPLATE_CHIPS = 1;
    public static final int MAX_LEARNED_CHIPS = 2;

    private IslandSuggestionService() {
    }

    public static List<ChipSuggestion> suggest(
            IslandSignalDescriptor descriptor,
            LearnedIslandRuleStore learnedRules,
            Collection<TriageIslandRef> existingIslands
    ) {
        return suggest(descriptor, learnedRules, existingIslands, Set.of());
    }

    public static List<ChipSuggestion> suggest(
            IslandSignalDescriptor descriptor,
            LearnedIslandRuleStore learnedRules,
            Collection<TriageIslandRef> existingIslands,
            Set<String> dismissedTemplateIds
    ) {
        if (descriptor == null) {
            return List.of();
        }
        LearnedIslandRuleStore rules = learnedRules == null ? new LearnedIslandRuleStore() : learnedRules;
        Map<String, TriageIslandRef> islandsById = indexIslands(existingIslands);
        Set<String> dismissed = dismissedTemplateIds == null ? Set.of() : dismissedTemplateIds;

        List<ChipSuggestion> learnedChips = buildLearnedChips(descriptor, rules, islandsById);
        List<ChipSuggestion> result = new ArrayList<>(Math.min(MAX_TOTAL_CHIPS, learnedChips.size()));
        for (ChipSuggestion chip : learnedChips) {
            if (result.size() >= MAX_LEARNED_CHIPS || result.size() >= MAX_TOTAL_CHIPS) {
                break;
            }
            result.add(chip);
        }

        if (result.size() < MAX_TOTAL_CHIPS) {
            ChipSuggestion templateChip = buildTemplateChip(descriptor, result, islandsById, dismissed);
            if (templateChip != null) {
                result.add(templateChip);
            }
        }
        return List.copyOf(result);
    }

    private static Map<String, TriageIslandRef> indexIslands(Collection<TriageIslandRef> islands) {
        if (islands == null || islands.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, TriageIslandRef> byId = new LinkedHashMap<>();
        for (TriageIslandRef island : islands) {
            if (island != null) {
                byId.put(island.islandId(), island);
            }
        }
        return byId;
    }

    private static List<ChipSuggestion> buildLearnedChips(
            IslandSignalDescriptor descriptor,
            LearnedIslandRuleStore rules,
            Map<String, TriageIslandRef> islandsById
    ) {
        List<LearnedIslandRule> firing = rules.firingRulesFor(descriptor);
        if (firing.isEmpty()) {
            return List.of();
        }
        ArrayList<LearnedIslandRule> sorted = new ArrayList<>(firing);
        sorted.sort((a, b) -> {
            int priorityCompare = Integer.compare(a.adjacency().priorityRank(), b.adjacency().priorityRank());
            if (priorityCompare != 0) {
                return priorityCompare;
            }
            int confirmationCompare = Integer.compare(b.confirmations(), a.confirmations());
            if (confirmationCompare != 0) {
                return confirmationCompare;
            }
            return Long.compare(b.lastConfirmedAtEpochMillis(), a.lastConfirmedAtEpochMillis());
        });

        ArrayList<ChipSuggestion> chips = new ArrayList<>();
        for (LearnedIslandRule rule : sorted) {
            TriageIslandRef island = islandsById.get(rule.islandId());
            if (island == null) {
                continue;
            }
            chips.add(ChipSuggestion.learned(
                    island.islandId(),
                    island.label(),
                    island.color(),
                    island.iconIdentity()
            ));
            if (chips.size() >= MAX_LEARNED_CHIPS) {
                break;
            }
        }
        return List.copyOf(chips);
    }

    private static ChipSuggestion buildTemplateChip(
            IslandSignalDescriptor descriptor,
            List<ChipSuggestion> existingChips,
            Map<String, TriageIslandRef> islandsById,
            Set<String> dismissedTemplateIds
    ) {
        // Trophy shunt: rarity=unique or role=trophy items belong on
        // display (CURIOSITY), regardless of whatever else they'd match.
        // Mirrors the same rule the populate generator applies.
        if (IslandSuggestionTemplate.isTrophy(descriptor)
                && !dismissedTemplateIds.contains(IslandSuggestionTemplate.CURIOSITY.defaultIslandId())) {
            if (isTemplateSuppressed(IslandSuggestionTemplate.CURIOSITY, existingChips, islandsById)) {
                return null;
            }
            return ChipSuggestion.template(IslandSuggestionTemplate.CURIOSITY, descriptor.identity());
        }
        for (IslandSuggestionTemplate template : IslandSuggestionTemplate.values()) {
            if (!template.matches(descriptor)) {
                continue;
            }
            if (dismissedTemplateIds.contains(template.defaultIslandId())) {
                continue;
            }
            if (isTemplateSuppressed(template, existingChips, islandsById)) {
                return null;
            }
            // Subsystem-aware: if any existing island matches one of this
            // descriptor's subsystems, route the chip there instead of the
            // generic template island. Mirrors the player's mental model
            // ("Create — Mechanical Power" already exists, drop into it
            // rather than spawning a parallel MECHANISMS pile).
            // Only honored for parent templates the player actually wants
            // mod-segregated — see IslandSuggestionTemplate#allowsSubsystemGrouping.
            if (template.allowsSubsystemGrouping()) {
                ChipSuggestion subsystemChip = subsystemChipIfExists(descriptor, islandsById);
                if (subsystemChip != null) {
                    return subsystemChip;
                }
            }
            return ChipSuggestion.template(template, descriptor.identity());
        }
        // Even when no template fired (very rare for classified items), a
        // subsystem-island that already exists should still capture the
        // item — the chip surfaces the place the player has already
        // sanctioned.
        return subsystemChipIfExists(descriptor, islandsById);
    }

    private static ChipSuggestion subsystemChipIfExists(
            IslandSignalDescriptor descriptor,
            Map<String, TriageIslandRef> islandsById
    ) {
        if (descriptor.subsystems().isEmpty() || islandsById.isEmpty()) {
            return null;
        }
        for (String subsystemId : descriptor.subsystems()) {
            if (subsystemId == null || subsystemId.isBlank()) {
                continue;
            }
            String islandId = IslandTemplateMatch.SUBSYSTEM_ISLAND_PREFIX + subsystemId;
            TriageIslandRef island = islandsById.get(islandId);
            if (island != null) {
                return ChipSuggestion.learned(
                        island.islandId(),
                        island.label(),
                        island.color(),
                        island.iconIdentity()
                );
            }
        }
        return null;
    }

    private static boolean isTemplateSuppressed(
            IslandSuggestionTemplate template,
            List<ChipSuggestion> existingChips,
            Map<String, TriageIslandRef> islandsById
    ) {
        for (ChipSuggestion chip : existingChips) {
            if (chip.kind() != ChipSuggestion.ChipKind.LEARNED) {
                continue;
            }
            if (chip.islandId().equals(template.defaultIslandId())) {
                return true;
            }
            TriageIslandRef island = islandsById.get(chip.islandId());
            if (island != null && template.defaultLabel().equalsIgnoreCase(island.label())) {
                return true;
            }
        }
        return false;
    }
}
