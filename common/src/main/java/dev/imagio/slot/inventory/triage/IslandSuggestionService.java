package dev.imagio.slot.inventory.triage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public final class IslandSuggestionService {
    public static final int MAX_TOTAL_CHIPS = 3;

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
        return suggest(descriptor, learnedRules, existingIslands, dismissedTemplateIds, id -> false);
    }

    public static List<ChipSuggestion> suggest(
            IslandSignalDescriptor descriptor,
            LearnedIslandRuleStore learnedRules,
            Collection<TriageIslandRef> existingIslands,
            Set<String> dismissedTemplateIds,
            Predicate<String> subsystemQualifier
    ) {
        return suggest(descriptor, learnedRules, existingIslands, dismissedTemplateIds, subsystemQualifier, id -> false);
    }

    public static List<ChipSuggestion> suggest(
            IslandSignalDescriptor descriptor,
            LearnedIslandRuleStore learnedRules,
            Collection<TriageIslandRef> existingIslands,
            Set<String> dismissedTemplateIds,
            Predicate<String> subsystemQualifier,
            Predicate<String> organizationGroupQualifier
    ) {
        if (descriptor == null) {
            return List.of();
        }
        LearnedIslandRuleStore rules = learnedRules == null ? new LearnedIslandRuleStore() : learnedRules;
        Map<String, TriageIslandRef> islandsById = indexIslands(existingIslands);
        Set<String> dismissed = dismissedTemplateIds == null ? Set.of() : dismissedTemplateIds;
        Predicate<String> qualifiedSubsystem = subsystemQualifier == null ? id -> false : subsystemQualifier;
        Predicate<String> qualifiedOrganizationGroup =
                organizationGroupQualifier == null ? id -> false : organizationGroupQualifier;
        List<LearnedIslandRule> sortedLearned = sortedLearnedRules(rules.firingRulesFor(descriptor));

        List<ChipSuggestion> result = new ArrayList<>(MAX_TOTAL_CHIPS);

        // Phase 1: a *high-specificity* template chip leads when one
        // fires (STAIRS / SLABS / WALLS / DOORS / FENCES / WINDOWS /
        // LIGHTING / METAL_STOCK / GEMS_CRYSTALS / ORES_RAW_STOCK /
        // DUSTS_POWDERS / WOOD / SEEDS / CROPS / PLANTS /
        // CERAMICS_MOLDS / ORGANIC_MATERIALS / STORAGE / TOOLS /
        // WEAPONS / ARMOR / FOOD, plus the trophy shunt). These keys
        // are narrower signals than NAMESPACE / CREATIVE_TAB learned
        // rules, so a "_wall" form facet should beat "you also homed
        // some other Create item recently" when ranking suggestions.
        ChipSuggestion specificTemplateChip = buildSpecificTemplateChip(
                descriptor, islandsById, dismissed, qualifiedSubsystem, qualifiedOrganizationGroup);
        if (specificTemplateChip != null) {
            result.add(specificTemplateChip);
        }

        // Phase 2: *strong* learned chips (TAG / MATERIAL_FAMILY /
        // SUBSYSTEM / DYE_COLOR — adjacency priority 0). A player who
        // has homed two andesite-family items together, or two of the
        // same dye color, is making a clear placement statement; that
        // beats a generic template.
        addLearnedChips(result, sortedLearned, islandsById, key -> key.priorityRank() == 0);

        // Phase 3: a *generic* template chip (BUILDING / DECORATION /
        // NATURAL / MECHANISMS / REDSTONE / UPGRADES / TRANSPORT /
        // UTILITY / CURIOSITY / WORKBENCHES / MISC) fills the tail
        // when there's room and no specific template already led.
        if (specificTemplateChip == null && result.size() < MAX_TOTAL_CHIPS) {
            ChipSuggestion templateChip = buildTemplateChip(
                    descriptor, result, islandsById, dismissed, qualifiedSubsystem, qualifiedOrganizationGroup);
            if (templateChip != null && !containsIsland(result, templateChip.islandId())) {
                result.add(templateChip);
            }
        }

        // Phase 4: *weak* learned chips (NAMESPACE / CREATIVE_TAB —
        // adjacency priority ≥ 1). These fire on broad signals like
        // "the player homed two other Create items somewhere" — too
        // coarse to outrank a clear template match. They still surface
        // when no template fires at all (modded items without facet
        // data), which is the original justification for the learned
        // layer.
        addLearnedChips(result, sortedLearned, islandsById, key -> key.priorityRank() > 0);

        return List.copyOf(result);
    }

    private static List<LearnedIslandRule> sortedLearnedRules(List<LearnedIslandRule> firing) {
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
        return sorted;
    }

    private static void addLearnedChips(
            List<ChipSuggestion> result,
            List<LearnedIslandRule> sortedLearned,
            Map<String, TriageIslandRef> islandsById,
            Predicate<LearnedAdjacencyKey> keyFilter
    ) {
        for (LearnedIslandRule rule : sortedLearned) {
            if (result.size() >= MAX_TOTAL_CHIPS) {
                return;
            }
            if (!keyFilter.test(rule.adjacency())) {
                continue;
            }
            if (containsIsland(result, rule.islandId())) {
                continue;
            }
            TriageIslandRef island = islandsById.get(rule.islandId());
            if (island == null) {
                continue;
            }
            result.add(ChipSuggestion.learned(
                    island.islandId(),
                    island.label(),
                    island.color(),
                    island.iconIdentity()
            ));
        }
    }

    private static boolean containsIsland(List<ChipSuggestion> chips, String islandId) {
        if (islandId == null || islandId.isBlank()) {
            return false;
        }
        for (ChipSuggestion chip : chips) {
            if (islandId.equals(chip.islandId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Build a chip from the highest-priority template that both
     * matches {@code descriptor} AND
     * {@link IslandSuggestionTemplate#isHighSpecificity}. Returns
     * {@code null} when no specific template fires; the caller falls
     * through to the learned-rule chips, then the generic-template
     * fallback.
     */
    private static ChipSuggestion buildSpecificTemplateChip(
            IslandSignalDescriptor descriptor,
            Map<String, TriageIslandRef> islandsById,
            Set<String> dismissedTemplateIds,
            Predicate<String> subsystemQualifier,
            Predicate<String> organizationGroupQualifier
    ) {
        // Trophies are unconditionally specific — they belong in
        // CURIOSITY regardless of role / namespace / learned rules.
        if (IslandSuggestionTemplate.isTrophy(descriptor)
                && !dismissedTemplateIds.contains(IslandSuggestionTemplate.CURIOSITY.defaultIslandId())) {
            return ChipSuggestion.template(IslandSuggestionTemplate.CURIOSITY, descriptor.identity());
        }
        for (IslandSuggestionTemplate template : IslandSuggestionTemplate.values()) {
            if (!template.matches(descriptor)) {
                continue;
            }
            if (!template.isHighSpecificity()) {
                continue;
            }
            if (dismissedTemplateIds.contains(template.defaultIslandId())) {
                continue;
            }
            if (template.allowsOrganizationGrouping()) {
                ChipSuggestion groupChip = organizationGroupChipIfQualified(
                        descriptor, islandsById, organizationGroupQualifier, template);
                if (groupChip != null) {
                    return groupChip;
                }
            }
            if (template.allowsSubsystemGrouping()) {
                ChipSuggestion subsystemChip = subsystemChipIfQualified(
                        descriptor, islandsById, subsystemQualifier, template);
                if (subsystemChip != null) {
                    return subsystemChip;
                }
            }
            return ChipSuggestion.template(template, descriptor.identity());
        }
        return null;
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

    private static ChipSuggestion buildTemplateChip(
            IslandSignalDescriptor descriptor,
            List<ChipSuggestion> existingChips,
            Map<String, TriageIslandRef> islandsById,
            Set<String> dismissedTemplateIds,
            Predicate<String> subsystemQualifier,
            Predicate<String> organizationGroupQualifier
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
            // Dynamic classifier-aware: if an existing organization group or
            // subsystem island matches the descriptor, route the chip there
            // instead of the generic template island. Count-qualified cohorts
            // can also surface a chip that auto-home / chip-accept will
            // materialize as a real island.
            if (template.allowsOrganizationGrouping()) {
                ChipSuggestion groupChip = organizationGroupChipIfQualified(
                        descriptor, islandsById, organizationGroupQualifier, template);
                if (groupChip != null) {
                    return groupChip;
                }
            }
            if (template.allowsSubsystemGrouping()) {
                ChipSuggestion subsystemChip = subsystemChipIfQualified(
                        descriptor, islandsById, subsystemQualifier, template);
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
        ChipSuggestion groupChip = organizationGroupChipIfQualified(descriptor, islandsById, id -> false, null);
        return groupChip != null ? groupChip : subsystemChipIfQualified(descriptor, islandsById, id -> false, null);
    }

    private static ChipSuggestion organizationGroupChipIfQualified(
            IslandSignalDescriptor descriptor,
            Map<String, TriageIslandRef> islandsById,
            Predicate<String> organizationGroupQualifier,
            IslandSuggestionTemplate parentTemplate
    ) {
        if (descriptor.organizationGroups().isEmpty()) {
            return null;
        }
        Predicate<String> qualifiedGroup = organizationGroupQualifier == null ? id -> false : organizationGroupQualifier;
        for (String groupId : descriptor.organizationGroups()) {
            if (groupId == null || groupId.isBlank()) {
                continue;
            }
            String islandId = IslandTemplateMatch.ORGANIZATION_GROUP_ISLAND_PREFIX + groupId;
            TriageIslandRef island = islandsById.get(islandId);
            if (island != null) {
                return ChipSuggestion.learned(
                        island.islandId(),
                        island.label(),
                        island.color(),
                        island.iconIdentity()
                );
            }
            if (parentTemplate != null && qualifiedGroup.test(groupId)) {
                return ChipSuggestion.learned(
                        islandId,
                        IslandTemplateMatch.formatSubsystemLabel(groupId),
                        parentTemplate.defaultColor(),
                        descriptor.identity()
                );
            }
        }
        return null;
    }

    private static ChipSuggestion subsystemChipIfQualified(
            IslandSignalDescriptor descriptor,
            Map<String, TriageIslandRef> islandsById,
            Predicate<String> subsystemQualifier,
            IslandSuggestionTemplate parentTemplate
    ) {
        if (descriptor.subsystems().isEmpty()) {
            return null;
        }
        Predicate<String> qualifiedSubsystem = subsystemQualifier == null ? id -> false : subsystemQualifier;
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
            if (parentTemplate != null && qualifiedSubsystem.test(subsystemId)) {
                return ChipSuggestion.learned(
                        islandId,
                        IslandTemplateMatch.formatSubsystemLabel(subsystemId),
                        parentTemplate.defaultColor(),
                        descriptor.identity()
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
