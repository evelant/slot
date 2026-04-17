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
    void learnedRuleShowsLearnedChipAlongsideTemplateWhenBothFire() {
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
        assertEquals(ChipSuggestion.ChipKind.LEARNED, chips.get(0).kind());
        assertEquals("island.mining", chips.get(0).islandId());
        assertEquals(ChipSuggestion.ChipKind.TEMPLATE, chips.get(1).kind());
        assertEquals(IslandSuggestionTemplate.MATERIALS, chips.get(1).template());
    }

    @Test
    void twoLearnedRulesSuppressTemplate() {
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

        assertEquals(2, chips.size());
        for (ChipSuggestion chip : chips) {
            assertEquals(ChipSuggestion.ChipKind.LEARNED, chip.kind());
        }
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
    void tagAdjacencyOutranksNamespaceWhenPickingSingleLearnedChip() {
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

        assertEquals(ChipSuggestion.ChipKind.LEARNED, chips.get(0).kind());
        assertEquals("island.metals", chips.get(0).islandId());
    }

    @Test
    void learnedChipPointingAtMaterializedTemplateIslandSuppressesTemplateChip() {
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
        assertEquals(ChipSuggestion.ChipKind.LEARNED, chips.get(0).kind());
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
}
