package dev.imagio.slot.inventory.triage;

import dev.imagio.slot.inventory.core.ItemIdentity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IslandSuggestionServiceTest {
    @Test
    void templateChipOnlyWhenNoLearnedRules() {
        IslandSignalDescriptor pickaxe = descriptor(
                "minecraft:iron_pickaxe",
                Set.of(IslandSignal.DIGGER_TOOL),
                Set.of()
        );

        List<ChipSuggestion> chips = IslandSuggestionService.suggest(
                pickaxe,
                new LearnedIslandRuleStore(),
                List.of()
        );

        assertEquals(1, chips.size());
        ChipSuggestion chip = chips.get(0);
        assertEquals(ChipSuggestion.ChipKind.TEMPLATE, chip.kind());
        assertEquals(IslandSuggestionTemplate.TOOLS, chip.template());
        assertEquals("template.tools", chip.islandId());
    }

    @Test
    void itemWithNoSignalsProducesNoChips() {
        IslandSignalDescriptor stone = IslandSignalDescriptor.empty(ItemIdentity.of("minecraft:stone"));
        assertTrue(IslandSuggestionService.suggest(stone, new LearnedIslandRuleStore(), List.of()).isEmpty());
    }

    @Test
    void specificTemplateLeadsLearnedChipWhenBothFire() {
        // INGOTS is a high-specificity template (specific c:ingots
        // tag trigger), so it leads when it fires — the learned chip
        // for "Mining" follows. Players grabbing a copper_ingot are
        // typically looking for the INGOTS pile first, with their
        // historical Mining drop as a second option.
        LearnedIslandRuleStore rules = new LearnedIslandRuleStore();
        rules.recordAssignment(descriptor("minecraft:iron_ingot", Set.of(), Set.of("c:ingots")), "island.mining", 1L);
        rules.recordAssignment(descriptor("minecraft:gold_ingot", Set.of(), Set.of("c:ingots")), "island.mining", 2L);

        TriageIslandRef mining = new TriageIslandRef("island.mining", "Mining", 0xCC888888, null);
        List<ChipSuggestion> chips = IslandSuggestionService.suggest(
                descriptor("minecraft:copper_ingot", Set.of(), Set.of("c:ingots")),
                rules,
                List.of(mining)
        );

        assertEquals(2, chips.size());
        assertEquals(ChipSuggestion.ChipKind.TEMPLATE, chips.get(0).kind());
        assertEquals(IslandSuggestionTemplate.INGOTS, chips.get(0).template());
        assertEquals(ChipSuggestion.ChipKind.LEARNED, chips.get(1).kind());
        assertEquals("island.mining", chips.get(1).islandId());
    }

    @Test
    void specificTemplateAndTwoLearnedRulesAllSurface() {
        // Three chips total now (raised cap). Specific template
        // (INGOTS) leads, then both learned rules. Old behavior
        // suppressed the template entirely under the 2-chip cap; the
        // new ordering keeps the most-confident signal visible.
        LearnedIslandRuleStore rules = new LearnedIslandRuleStore();
        rules.recordAssignment(descriptor("minecraft:iron_ingot", Set.of(), Set.of("c:ingots")), "island.mining", 1L);
        rules.recordAssignment(descriptor("minecraft:gold_ingot", Set.of(), Set.of("c:ingots")), "island.mining", 2L);
        rules.recordAssignment(descriptor("minecraft:iron_ingot", Set.of(), Set.of("c:ingots")), "island.industry", 3L);
        rules.recordAssignment(descriptor("minecraft:gold_ingot", Set.of(), Set.of("c:ingots")), "island.industry", 4L);

        List<ChipSuggestion> chips = IslandSuggestionService.suggest(
                descriptor("minecraft:copper_ingot", Set.of(), Set.of("c:ingots")),
                rules,
                List.of(
                        new TriageIslandRef("island.mining", "Mining", 0x888888, null),
                        new TriageIslandRef("island.industry", "Industry", 0x999999, null)
                )
        );

        assertEquals(3, chips.size());
        assertEquals(ChipSuggestion.ChipKind.TEMPLATE, chips.get(0).kind());
        assertEquals(IslandSuggestionTemplate.INGOTS, chips.get(0).template());
        assertEquals(ChipSuggestion.ChipKind.LEARNED, chips.get(1).kind());
        assertEquals(ChipSuggestion.ChipKind.LEARNED, chips.get(2).kind());
    }

    @Test
    void hardCapIsTwoTotal() {
        LearnedIslandRuleStore rules = new LearnedIslandRuleStore();
        for (int i = 0; i < 6; i++) {
            rules.recordAssignment(descriptor("minecraft:iron_ingot", Set.of(), Set.of("c:ingots")), "island." + i, i);
            rules.recordAssignment(descriptor("minecraft:gold_ingot", Set.of(), Set.of("c:ingots")), "island." + i, i);
        }

        List<TriageIslandRef> islands = new java.util.ArrayList<>();
        for (int i = 0; i < 6; i++) {
            islands.add(new TriageIslandRef("island." + i, "Island " + i, 0x444444, null));
        }

        List<ChipSuggestion> chips = IslandSuggestionService.suggest(
                descriptor("minecraft:copper_ingot", Set.of(), Set.of("c:ingots")),
                rules,
                islands
        );

        assertEquals(IslandSuggestionService.MAX_TOTAL_CHIPS, chips.size());
    }

    @Test
    void tagAdjacencyOutranksNamespaceWithinLearnedChips() {
        // Among the LEARNED chips (which now sort behind a specific
        // template chip when one fires), TAG-priority rules still beat
        // NAMESPACE-priority. INGOTS template leads via the c:ingots
        // tag; "Metals" (TAG-driven) is the next chip; "Dump"
        // (NAMESPACE-driven) trails.
        LearnedIslandRuleStore rules = new LearnedIslandRuleStore();
        rules.recordAssignment(descriptor("modded:iron_ingot", Set.of(), Set.of("c:ingots")), "island.metals", 1L);
        rules.recordAssignment(descriptor("modded:gold_ingot", Set.of(), Set.of("c:ingots")), "island.metals", 2L);
        rules.recordAssignment(descriptor("modded:arrow", Set.of()), "island.vanilla_dump", 3L);
        rules.recordAssignment(descriptor("modded:stone", Set.of()), "island.vanilla_dump", 4L);

        List<ChipSuggestion> chips = IslandSuggestionService.suggest(
                descriptor("modded:copper_ingot", Set.of(), Set.of("c:ingots")),
                rules,
                List.of(
                        new TriageIslandRef("island.metals", "Metals", 0x111111, null),
                        new TriageIslandRef("island.vanilla_dump", "Dump", 0x222222, null)
                )
        );

        // chips[0] = INGOTS template (high-specificity leader)
        assertEquals(ChipSuggestion.ChipKind.TEMPLATE, chips.get(0).kind());
        assertEquals(IslandSuggestionTemplate.INGOTS, chips.get(0).template());
        // chips[1] = TAG-driven Metals beats NAMESPACE-driven Dump
        assertEquals(ChipSuggestion.ChipKind.LEARNED, chips.get(1).kind());
        assertEquals("island.metals", chips.get(1).islandId());
    }

    @Test
    void learnedChipPointingAtMaterializedTemplateIslandDedupesToOneChip() {
        // When a learned rule and the matching template both point at
        // the same island id (the player materialized the template's
        // default island), only one chip surfaces — the specific
        // template chip leads, the learned rule for the same island
        // is deduped.
        LearnedIslandRuleStore rules = new LearnedIslandRuleStore();
        rules.recordAssignment(descriptor("modded:iron_pickaxe", Set.of(IslandSignal.DIGGER_TOOL), Set.of()), "template.tools", 1L);
        rules.recordAssignment(descriptor("modded:iron_axe", Set.of(IslandSignal.DIGGER_TOOL), Set.of()), "template.tools", 2L);

        TriageIslandRef tools = new TriageIslandRef("template.tools", "Tools", IslandSuggestionTemplate.TOOLS.defaultColor(), null);
        List<ChipSuggestion> chips = IslandSuggestionService.suggest(
                descriptor("modded:iron_shovel", Set.of(IslandSignal.DIGGER_TOOL), Set.of()),
                rules,
                List.of(tools)
        );

        assertEquals(1, chips.size());
        assertEquals(ChipSuggestion.ChipKind.TEMPLATE, chips.get(0).kind());
        assertEquals("template.tools", chips.get(0).islandId());
    }

    @Test
    void unknownLearnedIslandIsDropped() {
        LearnedIslandRuleStore rules = new LearnedIslandRuleStore();
        rules.recordAssignment(descriptor("minecraft:iron_ingot", Set.of(), Set.of("c:ingots")), "island.ghost", 1L);
        rules.recordAssignment(descriptor("minecraft:gold_ingot", Set.of(), Set.of("c:ingots")), "island.ghost", 2L);

        List<ChipSuggestion> chips = IslandSuggestionService.suggest(
                descriptor("minecraft:copper_ingot", Set.of(), Set.of("c:ingots")),
                rules,
                List.of()
        );

        assertEquals(1, chips.size());
        assertEquals(ChipSuggestion.ChipKind.TEMPLATE, chips.get(0).kind());
    }

    @Test
    void chipCarriesIconIdentityOfTriagedItem() {
        IslandSignalDescriptor apple = descriptor("minecraft:apple", Set.of(IslandSignal.FOOD), Set.of());
        List<ChipSuggestion> chips = IslandSuggestionService.suggest(apple, new LearnedIslandRuleStore(), List.of());

        assertEquals(1, chips.size());
        ChipSuggestion chip = chips.get(0);
        assertNotNull(chip.iconIdentity());
        assertEquals("minecraft:apple", chip.iconIdentity().itemId());
    }

    @Test
    void nullDescriptorYieldsEmpty() {
        assertTrue(IslandSuggestionService.suggest(null, new LearnedIslandRuleStore(), List.of()).isEmpty());
    }

    @Test
    void dismissedTemplateIsSkipped() {
        IslandSignalDescriptor apple = descriptor("minecraft:apple", Set.of(IslandSignal.FOOD), Set.of());

        List<ChipSuggestion> suppressed = IslandSuggestionService.suggest(
                apple,
                new LearnedIslandRuleStore(),
                List.of(),
                Set.of(IslandSuggestionTemplate.FOOD.defaultIslandId())
        );
        assertTrue(suppressed.isEmpty());

        List<ChipSuggestion> allowed = IslandSuggestionService.suggest(
                apple,
                new LearnedIslandRuleStore(),
                List.of(),
                Set.of("template.tools")
        );
        assertEquals(1, allowed.size());
        assertEquals(IslandSuggestionTemplate.FOOD, allowed.get(0).template());
    }

    @Test
    void subsystemAdjacencyDrivesLearnedChipEndToEnd() {
        // Two Create-mechanical-power items confirm "Workshop"; the
        // service should surface a LEARNED chip for the matching island
        // when a third subsystem-tagged identity arrives, even when its
        // tags / namespace alone wouldn't reach minimum confirmations.
        LearnedIslandRuleStore rules = new LearnedIslandRuleStore();
        rules.recordAssignment(subsystemDescriptor("create:cogwheel",
                "create:mechanical_power"), "island.workshop", 1L);
        rules.recordAssignment(subsystemDescriptor("create:large_cogwheel",
                "create:mechanical_power"), "island.workshop", 2L);

        TriageIslandRef workshop = new TriageIslandRef(
                "island.workshop", "Workshop", 0xCC8A5E24, null);
        List<ChipSuggestion> chips = IslandSuggestionService.suggest(
                subsystemDescriptor("create:gear", "create:mechanical_power"),
                rules,
                List.of(workshop)
        );

        assertNotNull(chips);
        assertTrue(chips.stream().anyMatch(c -> c.kind() == ChipSuggestion.ChipKind.LEARNED
                        && "island.workshop".equals(c.islandId())),
                "Workshop should surface as a LEARNED chip via SUBSYSTEM adjacency");
    }

    @Test
    void dyeColorAdjacencyDrivesLearnedChipEndToEnd() {
        LearnedIslandRuleStore rules = new LearnedIslandRuleStore();
        rules.recordAssignment(dyedDescriptor("modded:white_wool", "white"),
                "island.white-decor", 1L);
        rules.recordAssignment(dyedDescriptor("modded:white_carpet", "white"),
                "island.white-decor", 2L);

        TriageIslandRef whiteDecor = new TriageIslandRef(
                "island.white-decor", "White Decoration", 0xCCAAAAAA, null);
        List<ChipSuggestion> chips = IslandSuggestionService.suggest(
                dyedDescriptor("modded:white_concrete", "white"),
                rules,
                List.of(whiteDecor)
        );

        assertTrue(chips.stream().anyMatch(c -> c.kind() == ChipSuggestion.ChipKind.LEARNED
                        && "island.white-decor".equals(c.islandId())),
                "White Decoration should surface as a LEARNED chip via DYE_COLOR adjacency");
    }

    @Test
    void nullStoreIsHandledAsEmpty() {
        List<ChipSuggestion> chips = IslandSuggestionService.suggest(
                descriptor("minecraft:apple", Set.of(IslandSignal.FOOD), Set.of()),
                null,
                List.of()
        );

        assertEquals(1, chips.size());
        assertEquals(ChipSuggestion.ChipKind.TEMPLATE, chips.get(0).kind());
        assertEquals(IslandSuggestionTemplate.FOOD, chips.get(0).template());
    }

    private static IslandSignalDescriptor descriptor(String itemId, Set<IslandSignal> signals) {
        return descriptor(itemId, signals, Set.of());
    }

    private static IslandSignalDescriptor descriptor(
            String itemId,
            Set<IslandSignal> signals,
            Set<String> tags
    ) {
        return new IslandSignalDescriptor(
                ItemIdentity.of(itemId),
                signals,
                tags,
                itemId.contains(":") ? itemId.substring(0, itemId.indexOf(':')) : "",
                ""
        );
    }

    private static IslandSignalDescriptor subsystemDescriptor(String itemId, String subsystemId) {
        return new IslandSignalDescriptor(
                ItemIdentity.of(itemId),
                Set.of(),
                Set.of(),
                itemId.contains(":") ? itemId.substring(0, itemId.indexOf(':')) : "",
                "",
                "mechanism",
                null,
                null,
                subsystemId == null ? List.of() : List.of(subsystemId),
                List.of(),
                null,
                null,
                null,
                null,
                null,
                List.of(),
                null,
                false
        );
    }

    private static IslandSignalDescriptor dyedDescriptor(String itemId, String dyeColor) {
        return new IslandSignalDescriptor(
                ItemIdentity.of(itemId),
                Set.of(),
                Set.of(),
                itemId.contains(":") ? itemId.substring(0, itemId.indexOf(':')) : "",
                "",
                "decorative_block",
                null,
                null,
                List.of(),
                List.of(),
                null,
                null,
                null,
                null,
                dyeColor,
                List.of(),
                null,
                false
        );
    }
}
