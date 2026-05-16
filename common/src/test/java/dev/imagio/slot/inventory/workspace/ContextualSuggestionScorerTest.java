package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.classification.FacetIndex;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.goal.GoalDescriptor;
import dev.imagio.slot.inventory.goal.GoalIngredientDescriptor;
import dev.imagio.slot.inventory.goal.GoalPlanState;
import dev.imagio.slot.inventory.goal.GoalRecipeDescriptor;
import dev.imagio.slot.inventory.goal.GoalStackDescriptor;
import dev.imagio.slot.workflow.domain.ContextualItemAggregate;
import dev.imagio.slot.workflow.domain.ContextualSignalEvent;
import dev.imagio.slot.workflow.domain.ContextualSignalKind;
import dev.imagio.slot.workflow.domain.ContextualSuggestionState;
import dev.imagio.slot.workflow.domain.DomainEventMetadata;
import dev.imagio.slot.workflow.domain.InMemoryWorkflowDomainStateRepository;
import dev.imagio.slot.workflow.domain.WorkflowDomainSnapshot;
import dev.imagio.slot.workflow.domain.WorkflowProjection;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContextualSuggestionScorerTest {
    @Test
    void usefulNowIncludesAlreadyCarriedItemsRelevantToRecentContext() {
        FacetIndex index = FacetIndex.load(new StringReader(facetLayer()));
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        repository.appendContextualSignal(
                ContextualSignalEvent.item(
                        ContextualSignalKind.ITEM_ACQUIRED,
                        ItemIdentity.of("minecraft:charcoal"),
                        1,
                        ""),
                DomainEventMetadata.origin("test"));

        List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = ContextualSuggestionScorer.lanes(
                List.of(
                        item("minecraft:charcoal", true, BuiltinInventoryIds.PLAYER_MAIN),
                        item("minecraft:iron_ore", false, "")),
                repository.snapshot(),
                index,
                12,
                36);

        assertTrue(laneItems(lanes, SlotWorkspaceViewModel.ContextualSuggestionLane.USEFUL_NOW)
                .contains("minecraft:charcoal"));
        List<String> reasons = debugReasons(
                lanes,
                SlotWorkspaceViewModel.ContextualSuggestionLane.USEFUL_NOW,
                "minecraft:charcoal");
        assertTrue(reasons.stream().anyMatch(reason -> reason.startsWith("candidate: carried=true")));
        assertTrue(reasons.stream().anyMatch(reason -> reason.startsWith("context relevance ")));
        assertTrue(reasons.stream().anyMatch(reason -> reason.startsWith("score terms: relevance ")));
    }

    @Test
    void putAwayWorksFromCarryFrequencyWithoutDestinationRoute() {
        FacetIndex index = FacetIndex.load(new StringReader(facetLayer()));

        List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = ContextualSuggestionScorer.lanes(
                List.of(item("minecraft:flower_pot", true, BuiltinInventoryIds.PLAYER_MAIN)),
                WorkflowDomainSnapshot.empty(),
                index,
                4,
                36);

        assertTrue(laneItems(lanes, SlotWorkspaceViewModel.ContextualSuggestionLane.PUT_AWAY)
                .contains("minecraft:flower_pot"));
        SlotWorkspaceViewModel.ContextualSuggestionLane putAway = lanes.stream()
                .filter(lane -> SlotWorkspaceViewModel.ContextualSuggestionLane.PUT_AWAY.equals(lane.id()))
                .findFirst()
                .orElseThrow();
        assertFalse(putAway.debugInfo().isEmpty());
        List<String> reasons = putAway.debugInfo().get(0).reasons();
        assertTrue(reasons.stream().anyMatch(reason -> reason.startsWith("score ")));
        assertTrue(reasons.stream().anyMatch(reason -> reason.startsWith("candidate: carried=true")));
        assertTrue(reasons.stream().anyMatch(reason -> reason.startsWith("context relevance ")));
        assertTrue(reasons.stream().anyMatch(reason -> reason.startsWith("score terms: cleanup ")));
        assertTrue(reasons.contains("history: no aggregate"));
    }

    @Test
    void putAwayCarryFrequencyIsNotSuppressedByItsOwnCarriedSnapshot() {
        FacetIndex index = FacetIndex.load(new StringReader(facetLayer()));
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        repository.appendContextualSignal(
                new ContextualSignalEvent(
                        ContextualSignalKind.CARRIED_SET_CHANGED,
                        ItemIdentity.of("minecraft:flower_pot"),
                        1,
                        100L,
                        "",
                        "",
                        BuiltinInventoryIds.PLAYER_MAIN,
                        Map.of("phase", "start")),
                DomainEventMetadata.origin("test"));

        List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = ContextualSuggestionScorer.lanes(
                List.of(item("minecraft:flower_pot", true, BuiltinInventoryIds.PLAYER_MAIN)),
                repository.snapshot(),
                index,
                4,
                36,
                120L);

        assertTrue(laneItems(lanes, SlotWorkspaceViewModel.ContextualSuggestionLane.PUT_AWAY)
                .contains("minecraft:flower_pot"));
    }

    @Test
    void putAwayCanUseLongActiveCarryTimeWhenFacetPriorIsUnknown() {
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        repository.appendContextualSignal(
                new ContextualSignalEvent(
                        ContextualSignalKind.CARRIED_SET_CHANGED,
                        ItemIdentity.of("minecraft:dirt"),
                        1,
                        100L,
                        "",
                        "",
                        BuiltinInventoryIds.PLAYER_MAIN,
                        Map.of("phase", "start")),
                DomainEventMetadata.origin("test"));

        List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = ContextualSuggestionScorer.lanes(
                List.of(item("minecraft:dirt", true, BuiltinInventoryIds.PLAYER_MAIN)),
                repository.snapshot(),
                FacetIndex.empty(),
                20,
                36,
                7200L);

        assertTrue(laneItems(lanes, SlotWorkspaceViewModel.ContextualSuggestionLane.PUT_AWAY)
                .contains("minecraft:dirt"));
    }

    @Test
    void usefulNowShowsWaitingPlaceholderWhenNothingIsRelevantYet() {
        FacetIndex index = FacetIndex.load(new StringReader(facetLayer()));

        List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = ContextualSuggestionScorer.lanes(
                List.of(item("minecraft:bowl", true, BuiltinInventoryIds.PLAYER_MAIN)),
                WorkflowDomainSnapshot.empty(),
                index,
                36,
                36);

        SlotWorkspaceViewModel.ContextualSuggestionLane usefulNow = lanes.stream()
                .filter(lane -> SlotWorkspaceViewModel.ContextualSuggestionLane.USEFUL_NOW.equals(lane.id()))
                .findFirst()
                .orElseThrow();
        assertEquals(List.of(), usefulNow.items());
        assertFalse(usefulNow.placeholderText().isBlank());
        assertTrue(usefulNow.displayable());
    }

    @Test
    void usefulNowDoesNotPromoteCarriedSnapshotAsSelfEvidence() {
        FacetIndex index = FacetIndex.load(new StringReader(facetLayer()));
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        repository.appendContextualSignal(
                new ContextualSignalEvent(
                        ContextualSignalKind.CARRIED_SET_CHANGED,
                        ItemIdentity.of("minecraft:charcoal"),
                        1,
                        100L,
                        "",
                        "",
                        BuiltinInventoryIds.PLAYER_MAIN,
                        Map.of("phase", "start")),
                DomainEventMetadata.origin("test"));

        List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = ContextualSuggestionScorer.lanes(
                List.of(item("minecraft:charcoal", true, BuiltinInventoryIds.PLAYER_MAIN)),
                repository.snapshot(),
                index,
                12,
                36,
                120L);

        assertFalse(laneItems(lanes, SlotWorkspaceViewModel.ContextualSuggestionLane.USEFUL_NOW)
                .contains("minecraft:charcoal"));
    }

    @Test
    void usefulNowDoesNotTreatDesiredCarryReservationsAsCurrentUse() {
        FacetIndex index = FacetIndex.load(new StringReader(facetLayer()));
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        repository.appendContextualSignal(
                new ContextualSignalEvent(
                        ContextualSignalKind.CARRIED_SET_CHANGED,
                        ItemIdentity.of("minecraft:charcoal"),
                        1,
                        100L,
                        "",
                        "",
                        "sophisticatedbackpacks:carried/test",
                        Map.of("phase", "start")),
                DomainEventMetadata.origin("test"));

        List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = ContextualSuggestionScorer.lanes(
                List.of(item(
                        "minecraft:charcoal",
                        true,
                        "sophisticatedbackpacks:carried/test",
                        32,
                        false,
                        0)),
                repository.snapshot(),
                index,
                12,
                36,
                120L);

        assertFalse(laneItems(lanes, SlotWorkspaceViewModel.ContextualSuggestionLane.USEFUL_NOW)
                .contains("minecraft:charcoal"));
    }

    @Test
    void usefulNowDoesNotLetPassiveCarriedToolsMutuallyPromote() {
        FacetIndex index = FacetIndex.load(new StringReader(facetLayer()));
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        List<String> carriedTools = List.of(
                "slot:test_construction_wand",
                "slot:test_grappling_hook",
                "slot:test_hoe",
                "slot:test_knife");
        for (String itemId : carriedTools) {
            repository.appendContextualSignal(
                    new ContextualSignalEvent(
                            ContextualSignalKind.CARRIED_SET_CHANGED,
                            ItemIdentity.of(itemId),
                            1,
                            100L,
                            "",
                            "",
                            BuiltinInventoryIds.PLAYER_MAIN,
                            Map.of("phase", "start")),
                    DomainEventMetadata.origin("test"));
        }

        List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = ContextualSuggestionScorer.lanes(
                carriedTools.stream()
                        .map(itemId -> item(itemId, true, BuiltinInventoryIds.PLAYER_MAIN))
                        .toList(),
                repository.snapshot(),
                index,
                12,
                36,
                120L);

        assertTrue(laneItems(lanes, SlotWorkspaceViewModel.ContextualSuggestionLane.USEFUL_NOW).isEmpty());
    }

    @Test
    void usefulNowDoesNotTreatDepositedItemsAsActiveUseContext() {
        FacetIndex index = FacetIndex.load(new StringReader(facetLayer()));
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        repository.appendContextualSignal(
                new ContextualSignalEvent(
                        ContextualSignalKind.ITEM_DEPOSITED_TO_STORAGE,
                        ItemIdentity.of("minecraft:flower_pot"),
                        1,
                        105L,
                        "",
                        "",
                        "storage",
                        Map.of()),
                DomainEventMetadata.origin("test"));

        List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = ContextualSuggestionScorer.lanes(
                List.of(item("minecraft:flower_pot", true, BuiltinInventoryIds.PLAYER_MAIN)),
                repository.snapshot(),
                index,
                12,
                36,
                120L);

        assertFalse(laneItems(lanes, SlotWorkspaceViewModel.ContextualSuggestionLane.USEFUL_NOW)
                .contains("minecraft:flower_pot"));
    }

    @Test
    void usefulNowDepositPenaltyClearsWhenItemIsPickedUpAgain() {
        FacetIndex index = FacetIndex.load(new StringReader(facetLayer()));
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        repository.appendContextualSignal(
                new ContextualSignalEvent(
                        ContextualSignalKind.ITEM_DEPOSITED_TO_STORAGE,
                        ItemIdentity.of("minecraft:charcoal"),
                        1,
                        105L,
                        "",
                        "",
                        "storage",
                        Map.of()),
                DomainEventMetadata.origin("test"));
        repository.appendContextualSignal(
                new ContextualSignalEvent(
                        ContextualSignalKind.CARRIED_SET_CHANGED,
                        ItemIdentity.of("minecraft:charcoal"),
                        1,
                        110L,
                        "",
                        "",
                        BuiltinInventoryIds.PLAYER_MAIN,
                        Map.of("phase", "start")),
                DomainEventMetadata.origin("test"));
        repository.appendContextualSignal(
                ContextualSignalEvent.item(
                        ContextualSignalKind.ITEM_ACQUIRED,
                        ItemIdentity.of("minecraft:iron_ore"),
                        1,
                        ""),
                DomainEventMetadata.origin("test"));

        List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = ContextualSuggestionScorer.lanes(
                List.of(item("minecraft:charcoal", true, BuiltinInventoryIds.PLAYER_MAIN)),
                repository.snapshot(),
                index,
                12,
                36,
                120L);

        assertTrue(laneItems(lanes, SlotWorkspaceViewModel.ContextualSuggestionLane.USEFUL_NOW)
                .contains("minecraft:charcoal"));
    }

    @Test
    void usefulNowExcludesCarriedStorageContainers() {
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        repository.appendContextualSignal(
                ContextualSignalEvent.item(
                        ContextualSignalKind.ITEM_ACQUIRED,
                        ItemIdentity.of("sns:straw_basket"),
                        1,
                        BuiltinInventoryIds.PLAYER_MAIN),
                DomainEventMetadata.origin("test"));

        List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = ContextualSuggestionScorer.lanes(
                List.of(item(
                        "sns:straw_basket",
                        true,
                        BuiltinInventoryIds.PLAYER_MAIN,
                        0,
                        false,
                        0,
                        true)),
                repository.snapshot(),
                FacetIndex.empty(),
                12,
                36);

        assertFalse(laneItems(lanes, SlotWorkspaceViewModel.ContextualSuggestionLane.USEFUL_NOW)
                .contains("sns:straw_basket"));
    }

    @Test
    void usefulNowCanSurfaceIgnitionToolFromFuelAndCampfirePotContext() {
        FacetIndex index = FacetIndex.load(new StringReader(facetLayer()));
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        repository.appendContextualSignal(
                new ContextualSignalEvent(
                        ContextualSignalKind.CARRIED_SET_CHANGED,
                        ItemIdentity.of("minecraft:oak_log"),
                        8,
                        100L,
                        "",
                        "",
                        BuiltinInventoryIds.PLAYER_MAIN,
                        Map.of("phase", "start")),
                DomainEventMetadata.origin("test"));
        repository.appendContextualSignal(
                new ContextualSignalEvent(
                        ContextualSignalKind.STATION_OPENED,
                        null,
                        1,
                        105L,
                        "menu:net.dries007.tfc.common.container.CampfirePotContainer",
                        "Campfire Pot",
                        "",
                        Map.of()),
                DomainEventMetadata.origin("test"));

        List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = ContextualSuggestionScorer.lanes(
                List.of(
                        item("minecraft:flint_and_steel", true, BuiltinInventoryIds.PLAYER_MAIN),
                        item("minecraft:oak_log", true, BuiltinInventoryIds.PLAYER_MAIN)),
                repository.snapshot(),
                index,
                12,
                36,
                120L);

        assertTrue(laneItems(lanes, SlotWorkspaceViewModel.ContextualSuggestionLane.USEFUL_NOW)
                .contains("minecraft:flint_and_steel"));
    }

    @Test
    void usefulNowDoesNotLearnStationCooccurrenceFromPassiveCarriedItems() {
        FacetIndex index = FacetIndex.load(new StringReader(facetLayer()));
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        repository.appendContextualSignal(
                new ContextualSignalEvent(
                        ContextualSignalKind.CARRIED_SET_CHANGED,
                        ItemIdentity.of("slot:test_knife"),
                        1,
                        100L,
                        "",
                        "",
                        BuiltinInventoryIds.PLAYER_MAIN,
                        Map.of("phase", "start")),
                DomainEventMetadata.origin("test"));
        repository.appendContextualSignal(
                new ContextualSignalEvent(
                        ContextualSignalKind.STATION_CONTENTS_CHANGED,
                        ItemIdentity.of("tfc:leather"),
                        1,
                        105L,
                        "menu:tfc.leather_knapping",
                        "Leather Knapping",
                        "tool.input",
                        Map.of("change", "increase")),
                DomainEventMetadata.origin("test"));

        List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = ContextualSuggestionScorer.lanes(
                List.of(
                        item("slot:test_knife", true, BuiltinInventoryIds.PLAYER_MAIN),
                        item("slot:test_pickaxe", true, BuiltinInventoryIds.PLAYER_MAIN)),
                repository.snapshot(),
                index,
                12,
                36,
                120L);

        List<String> useful = laneItems(lanes, SlotWorkspaceViewModel.ContextualSuggestionLane.USEFUL_NOW);
        assertFalse(useful.contains("slot:test_knife"));
        assertFalse(useful.contains("slot:test_pickaxe"));
    }

    @Test
    void usefulNowUsesStationCooccurrenceHintsFromMovedStationItems() {
        FacetIndex index = FacetIndex.load(new StringReader(facetLayer()));
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        repository.appendContextualSignal(
                new ContextualSignalEvent(
                        ContextualSignalKind.STATION_CONTENTS_CHANGED,
                        ItemIdentity.of("slot:test_saw"),
                        1,
                        100L,
                        "menu:crafting",
                        "Crafting",
                        "tool.input",
                        Map.of("change", "increase")),
                DomainEventMetadata.origin("test"));
        repository.appendContextualSignal(
                new ContextualSignalEvent(
                        ContextualSignalKind.STATION_CONTENTS_CHANGED,
                        ItemIdentity.of("minecraft:oak_log"),
                        1,
                        101L,
                        "menu:crafting",
                        "Crafting",
                        "tool.input",
                        Map.of("change", "increase")),
                DomainEventMetadata.origin("test"));
        for (int i = 0; i < 20; i++) {
            repository.appendContextualSignal(
                    ContextualSignalEvent.item(
                            ContextualSignalKind.ITEM_ACQUIRED,
                            ItemIdentity.of("minecraft:carrot"),
                            1,
                            ""),
                    DomainEventMetadata.origin("test"));
        }
        repository.appendContextualSignal(
                new ContextualSignalEvent(
                        ContextualSignalKind.STATION_CONTENTS_CHANGED,
                        ItemIdentity.of("minecraft:oak_log"),
                        1,
                        130L,
                        "menu:crafting",
                        "Crafting",
                        "tool.input",
                        Map.of("change", "increase")),
                DomainEventMetadata.origin("test"));

        List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = ContextualSuggestionScorer.lanes(
                List.of(
                        item("slot:test_saw", true, BuiltinInventoryIds.PLAYER_MAIN),
                        item("slot:test_pickaxe", true, BuiltinInventoryIds.PLAYER_MAIN)),
                repository.snapshot(),
                index,
                12,
                36,
                120L);

        List<String> useful = laneItems(lanes, SlotWorkspaceViewModel.ContextualSuggestionLane.USEFUL_NOW);
        assertTrue(useful.contains("slot:test_saw"));
        assertFalse(useful.contains("slot:test_pickaxe"));
        List<String> reasons = debugReasons(
                lanes,
                SlotWorkspaceViewModel.ContextualSuggestionLane.USEFUL_NOW,
                "slot:test_saw");
        assertTrue(reasons.stream().anyMatch(reason -> reason.startsWith("active matches: ")));
        assertTrue(reasons.stream().anyMatch(reason -> reason.startsWith("score terms: relevance ")));
    }

    @Test
    void usefulNowIgnoresLegacyUnscopedCooccurrenceHints() {
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        ItemIdentity brass = ItemIdentity.of("tfc:metal/ingot/brass");
        repository.appendContextualSignal(
                ContextualSignalEvent.item(
                        ContextualSignalKind.ITEM_CRAFTED_OR_PRODUCED,
                        brass,
                        1,
                        ""),
                DomainEventMetadata.origin("test"));
        ContextualSuggestionState state = repository.contextualSuggestionState();
        LinkedHashMap<ItemIdentity, ContextualItemAggregate> items = new LinkedHashMap<>(state.itemAggregates());
        items.put(brass, ContextualItemAggregate.empty(brass)
                .withHint("item:tfc:food/rennet", 40D)
                .withHint("item:grapplemod:grapplinghook", 40D));
        repository.replaceContextualSuggestionState(new ContextualSuggestionState(
                state.nextStreamSequence(),
                items,
                state.contextAggregates(),
                state.activeCarried(),
                state.recentSignals(),
                state.activeContextKey()));

        List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = ContextualSuggestionScorer.lanes(
                List.of(
                        item("tfc:food/rennet", true, BuiltinInventoryIds.PLAYER_MAIN),
                        item("grapplemod:grapplinghook", true, BuiltinInventoryIds.PLAYER_MAIN)),
                repository.snapshot(),
                FacetIndex.empty(),
                12,
                36);

        List<String> useful = laneItems(lanes, SlotWorkspaceViewModel.ContextualSuggestionLane.USEFUL_NOW);
        assertFalse(useful.contains("tfc:food/rennet"));
        assertFalse(useful.contains("grapplemod:grapplinghook"));
    }

    @Test
    void usefulNowDoesNotPromoteSameNamespaceFoodDuringMetalSmelting() {
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        for (int i = 0; i < 12; i++) {
            repository.appendContextualSignal(
                    ContextualSignalEvent.item(
                            ContextualSignalKind.ITEM_CRAFTED_OR_PRODUCED,
                            ItemIdentity.of("tfc:metal/ingot/brass"),
                            1,
                            ""),
                    DomainEventMetadata.origin("test"));
        }

        List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = ContextualSuggestionScorer.lanes(
                List.of(item("tfc:food/rennet", true, BuiltinInventoryIds.PLAYER_MAIN)),
                repository.snapshot(),
                FacetIndex.empty(),
                12,
                36);

        assertFalse(laneItems(lanes, SlotWorkspaceViewModel.ContextualSuggestionLane.USEFUL_NOW)
                .contains("tfc:food/rennet"));
    }

    @Test
    void usefulNowUsesWorldItemUseWithoutPromotingEveryTool() {
        FacetIndex index = FacetIndex.load(new StringReader(facetLayer()));
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        repository.appendContextualSignal(
                new ContextualSignalEvent(
                        ContextualSignalKind.ITEM_USED,
                        ItemIdentity.of("slot:test_saw"),
                        1,
                        100L,
                        "world:right_click_block",
                        "block:minecraft:oak_log",
                        "hand:main_hand",
                        Map.of("action", "right_click_block", "target", "block:minecraft:oak_log")),
                DomainEventMetadata.origin("test"));

        List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = ContextualSuggestionScorer.lanes(
                List.of(
                        item("slot:test_saw", true, BuiltinInventoryIds.PLAYER_MAIN),
                        item("slot:test_pickaxe", true, BuiltinInventoryIds.PLAYER_MAIN)),
                repository.snapshot(),
                index,
                12,
                36,
                120L);

        List<String> useful = laneItems(lanes, SlotWorkspaceViewModel.ContextualSuggestionLane.USEFUL_NOW);
        assertTrue(useful.contains("slot:test_saw"));
        assertFalse(useful.contains("slot:test_pickaxe"));
    }

    @Test
    void usefulNowIgnoresLowInformationTextTokenOverlap() {
        FacetIndex index = FacetIndex.load(new StringReader(facetLayer()));
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        repository.appendContextualSignal(
                new ContextualSignalEvent(
                        ContextualSignalKind.ITEM_USED,
                        ItemIdentity.of("slot:test_smithing_tongs"),
                        1,
                        100L,
                        "world:right_click_block",
                        "block:tfc:anvil",
                        "hand:main_hand",
                        Map.of("action", "right_click_block")),
                DomainEventMetadata.origin("test"));

        List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = ContextualSuggestionScorer.lanes(
                List.of(item("slot:test_grappling_hook", true, BuiltinInventoryIds.PLAYER_MAIN)),
                repository.snapshot(),
                index,
                12,
                36,
                120L);

        assertFalse(laneItems(lanes, SlotWorkspaceViewModel.ContextualSuggestionLane.USEFUL_NOW)
                .contains("slot:test_grappling_hook"));
    }

    @Test
    void usefulNowUsesPlacedConsumedAndDamagedSignals() {
        FacetIndex index = FacetIndex.load(new StringReader(facetLayer()));
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        repository.appendContextualSignal(
                new ContextualSignalEvent(
                        ContextualSignalKind.ITEM_PLACED,
                        ItemIdentity.of("minecraft:oak_log"),
                        1,
                        100L,
                        "world:place_block",
                        "block:minecraft:oak_log",
                        "",
                        Map.of("action", "place_block")),
                DomainEventMetadata.origin("test"));
        repository.appendContextualSignal(
                new ContextualSignalEvent(
                        ContextualSignalKind.ITEM_CONSUMED,
                        ItemIdentity.of("minecraft:carrot"),
                        1,
                        101L,
                        "world:consume_finish",
                        "",
                        "",
                        Map.of("action", "consume_finish")),
                DomainEventMetadata.origin("test"));
        repository.appendContextualSignal(
                new ContextualSignalEvent(
                        ContextualSignalKind.ITEM_DAMAGED,
                        ItemIdentity.of("slot:test_saw"),
                        1,
                        102L,
                        "world:item_destroyed",
                        "",
                        "hand:main_hand",
                        Map.of("action", "item_destroyed")),
                DomainEventMetadata.origin("test"));

        List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = ContextualSuggestionScorer.lanes(
                List.of(
                        item("minecraft:oak_log", true, BuiltinInventoryIds.PLAYER_MAIN),
                        item("minecraft:bowl", true, BuiltinInventoryIds.PLAYER_MAIN),
                        item("slot:test_saw", true, BuiltinInventoryIds.PLAYER_MAIN)),
                repository.snapshot(),
                index,
                12,
                36,
                120L);

        List<String> useful = laneItems(lanes, SlotWorkspaceViewModel.ContextualSuggestionLane.USEFUL_NOW);
        assertTrue(useful.contains("minecraft:oak_log"));
        assertTrue(useful.contains("minecraft:bowl"));
        assertTrue(useful.contains("slot:test_saw"));
    }

    @Test
    void putAwayExcludesProtectedTargetsAndGoalRelevantItems() {
        FacetIndex index = FacetIndex.load(new StringReader(facetLayer()));
        SlotWorkspaceViewModel.AtlasItem hotbarDecor = item(
                "minecraft:flower_pot",
                true,
                BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0);
        SlotWorkspaceViewModel.AtlasItem desiredDecor = item(
                "minecraft:flower_pot",
                true,
                BuiltinInventoryIds.PLAYER_MAIN,
                8,
                false,
                0);
        SlotWorkspaceViewModel.AtlasItem wantedDecor = item(
                "minecraft:flower_pot",
                true,
                BuiltinInventoryIds.PLAYER_MAIN,
                0,
                false,
                4);
        SlotWorkspaceViewModel.AtlasItem goalOre = item(
                "minecraft:iron_ore",
                true,
                BuiltinInventoryIds.PLAYER_MAIN);

        WorkflowDomainSnapshot snapshot = new WorkflowDomainSnapshot(
                1L,
                workflowWithGoals(List.of(goal("minecraft:iron_ore"))),
                null,
                null,
                null,
                null,
                null);
        List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = ContextualSuggestionScorer.lanes(
                List.of(hotbarDecor, desiredDecor, wantedDecor, goalOre),
                snapshot,
                index,
                1,
                36);

        List<String> putAway = laneItems(lanes, SlotWorkspaceViewModel.ContextualSuggestionLane.PUT_AWAY);
        assertFalse(putAway.contains("minecraft:flower_pot"));
        assertFalse(putAway.contains("minecraft:iron_ore"));
    }

    @Test
    void putAwayAlwaysIncludesDesiredCountExcess() {
        FacetIndex index = FacetIndex.load(new StringReader(facetLayer()));
        SlotWorkspaceViewModel.AtlasItem excessGoalItem = item(
                "minecraft:iron_ore",
                true,
                BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0,
                8,
                false,
                0,
                false,
                16);

        WorkflowDomainSnapshot snapshot = new WorkflowDomainSnapshot(
                1L,
                workflowWithGoals(List.of(goal("minecraft:iron_ore"))),
                null,
                null,
                null,
                null,
                null);
        List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = ContextualSuggestionScorer.lanes(
                List.of(excessGoalItem),
                snapshot,
                index,
                12,
                36);

        assertTrue(laneItems(lanes, SlotWorkspaceViewModel.ContextualSuggestionLane.PUT_AWAY)
                .contains("minecraft:iron_ore"));
    }

    @Test
    void mixedRecentContextCanKeepOlderClusterAndRaiseNewCluster() {
        FacetIndex index = FacetIndex.load(new StringReader(facetLayer()));
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        repository.appendContextualSignal(
                ContextualSignalEvent.item(
                        ContextualSignalKind.ITEM_ACQUIRED,
                        ItemIdentity.of("minecraft:iron_ore"),
                        1,
                        ""),
                DomainEventMetadata.origin("test"));
        repository.appendContextualSignal(
                ContextualSignalEvent.item(
                        ContextualSignalKind.ITEM_ACQUIRED,
                        ItemIdentity.of("minecraft:carrot"),
                        1,
                        ""),
                DomainEventMetadata.origin("test"));

        List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = ContextualSuggestionScorer.lanes(
                List.of(
                        item("minecraft:iron_ore", true, BuiltinInventoryIds.PLAYER_MAIN),
                        item("minecraft:bowl", true, BuiltinInventoryIds.PLAYER_MAIN)),
                repository.snapshot(),
                index,
                8,
                36);

        List<String> useful = laneItems(lanes, SlotWorkspaceViewModel.ContextualSuggestionLane.USEFUL_NOW);
        assertTrue(useful.contains("minecraft:iron_ore"));
        assertTrue(useful.contains("minecraft:bowl"));
    }

    @Test
    void facetIndexExposesContextualFacetAccessors() {
        FacetIndex index = FacetIndex.load(new StringReader(facetLayer()));

        assertTrue(index.workflows("minecraft:charcoal").contains("metallurgy"));
        assertTrue(index.workflowRoles("minecraft:charcoal").contains("fuel"));
        assertTrue(index.usedAt("minecraft:charcoal").contains("forge"));
        assertTrue(index.processingIn("minecraft:iron_ore").contains("smelting"));
        assertTrue(index.primaryUses("minecraft:flint_and_steel").contains("igniting fires"));
        assertTrue(index.isFuel("minecraft:oak_log"));
    }

    private static List<String> laneItems(
            List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes,
            String laneId
    ) {
        return lanes.stream()
                .filter(lane -> laneId.equals(lane.id()))
                .findFirst()
                .map(lane -> lane.items().stream().map(item -> item.identity().itemId()).toList())
                .orElse(List.of());
    }

    private static List<String> debugReasons(
            List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes,
            String laneId,
            String itemId
    ) {
        return lanes.stream()
                .filter(lane -> laneId.equals(lane.id()))
                .findFirst()
                .flatMap(lane -> lane.debugInfo().stream()
                        .filter(info -> itemId.equals(info.identity().itemId()))
                        .findFirst())
                .map(SlotWorkspaceViewModel.ContextualSuggestionDebugInfo::reasons)
                .orElse(List.of());
    }

    private static SlotWorkspaceViewModel.AtlasItem item(String itemId, boolean carried, String sourceId) {
        return item(itemId, carried, sourceId, 0, false, 0);
    }

    private static SlotWorkspaceViewModel.AtlasItem item(
            String itemId,
            boolean carried,
            String sourceId,
            int desiredCount,
            boolean kitNeeded,
            int wantedCount
    ) {
        return item(itemId, carried, sourceId, desiredCount, kitNeeded, wantedCount, false);
    }

    private static SlotWorkspaceViewModel.AtlasItem item(
            String itemId,
            boolean carried,
            String sourceId,
            int desiredCount,
            boolean kitNeeded,
            int wantedCount,
            boolean isCarriedContainer
    ) {
        return item(itemId, carried, sourceId, desiredCount, kitNeeded, wantedCount, isCarriedContainer, 1);
    }

    private static SlotWorkspaceViewModel.AtlasItem item(
            String itemId,
            boolean carried,
            String sourceId,
            int desiredCount,
            boolean kitNeeded,
            int wantedCount,
            boolean isCarriedContainer,
            int totalCount
    ) {
        return new SlotWorkspaceViewModel.AtlasItem(
                SlotWorkspaceViewModel.IdentityRef.from(ItemIdentity.of(itemId)),
                new ItemStack(itemId, 1, 64),
                itemId,
                totalCount,
                0,
                "main",
                false,
                false,
                carried,
                !carried,
                0,
                List.of(),
                List.of(),
                List.of(),
                isCarriedContainer,
                isCarriedContainer ? 4 : 0,
                isCarriedContainer ? 9 : 0,
                kitNeeded,
                desiredCount,
                false,
                wantedCount,
                sourceId,
                0,
                1);
    }

    private static GoalPlanState goal(String itemId) {
        GoalStackDescriptor output = GoalStackDescriptor.of(itemId, 1);
        GoalRecipeDescriptor recipe = new GoalRecipeDescriptor(
                "recipe",
                "crafting",
                true,
                List.of(output),
                List.of(GoalIngredientDescriptor.concrete("input", output, 1)),
                List.of(),
                List.of());
        GoalDescriptor descriptor = new GoalDescriptor(
                "goal",
                "Goal",
                List.of(output),
                1,
                "recipe",
                "crafting",
                List.of(recipe));
        return new GoalPlanState("goal", "Goal", 1, descriptor, null);
    }

    private static WorkflowProjection.Snapshot workflowWithGoals(List<GoalPlanState> goals) {
        WorkflowProjection.Snapshot empty = WorkflowProjection.Snapshot.empty();
        return new WorkflowProjection.Snapshot(
                empty.userCollections(),
                empty.memberships(),
                empty.loadoutsByCollection(),
                empty.favoriteTags(),
                empty.junkTags(),
                empty.protection(),
                empty.recentDismissedUpToByIdentity(),
                empty.visualHomeMap(),
                empty.claimedChestMap(),
                empty.chestAffinityMap(),
                empty.clusterLabels(),
                empty.kitMap(),
                empty.playerDesiredCounts(),
                empty.kitDesiredCounts(),
                empty.playerWantedCounts(),
                goals,
                empty.goalRecipeDefaults());
    }

    private static String facetLayer() {
        return """
                {
                  "schema_version": 1,
                  "layer": "server",
                  "entries": {
                    "minecraft:charcoal": {
                      "facets": {
                        "workflow": {"values": ["metallurgy"]},
                        "workflow_role": {"values": ["fuel"]},
                        "used_at": {"values": ["forge"]},
                        "carry_frequency": {"value": "occasional"}
                      }
                    },
                    "minecraft:iron_ore": {
                      "facets": {
                        "workflow": {"values": ["metallurgy"]},
                        "workflow_role": {"values": ["input"]},
                        "used_at": {"values": ["forge"]},
                        "processing_in": {"values": ["smelting"]},
                        "carry_frequency": {"value": "rare"}
                      }
                    },
                    "minecraft:carrot": {
                      "facets": {
                        "workflow": {"values": ["cooking"]},
                        "workflow_role": {"values": ["ingredient"]},
                        "used_at": {"values": ["cooking_pot"]},
                        "carry_frequency": {"value": "occasional"}
                      }
                    },
                    "minecraft:bowl": {
                      "facets": {
                        "workflow": {"values": ["cooking"]},
                        "workflow_role": {"values": ["container"]},
                        "used_at": {"values": ["cooking_pot"]},
                        "carry_frequency": {"value": "frequent"}
                      }
                    },
                    "minecraft:flower_pot": {
                      "facets": {
                        "workflow": {"values": ["decoration"]},
                        "workflow_role": {"values": ["display"]},
                        "carry_frequency": {"value": "display_only"}
                      }
                    },
                    "minecraft:flint_and_steel": {
                      "facets": {
                        "role": {"value": "tool"},
                        "primary_uses": {"values": ["igniting fires"]},
                        "carry_frequency": {"value": "occasional"}
                      }
                    },
                    "minecraft:oak_log": {
                      "facets": {
                        "role": {"value": "block"},
                        "primary_uses": {"values": ["fuel", "crafting planks"]},
                        "carry_frequency": {"value": "everyday"},
                        "is_fuel": {"value": true}
                      }
                    },
                    "slot:test_construction_wand": {
                      "facets": {
                        "role": {"value": "tool"},
                        "workflow": {"values": ["building"]},
                        "workflow_role": {"values": ["tool"]},
                        "carry_frequency": {"value": "everyday"}
                      }
                    },
                    "slot:test_grappling_hook": {
                      "facets": {
                        "role": {"value": "tool"},
                        "primary_uses": {"values": ["launch and pull"]},
                        "workflow_role": {"values": ["tool"]},
                        "carry_frequency": {"value": "everyday"}
                      }
                    },
                    "slot:test_hoe": {
                      "facets": {
                        "role": {"value": "tool"},
                        "workflow": {"values": ["building"]},
                        "workflow_role": {"values": ["tool"]},
                        "carry_frequency": {"value": "everyday"}
                      }
                    },
                    "slot:test_knife": {
                      "facets": {
                        "role": {"value": "tool"},
                        "workflow": {"values": ["building"]},
                        "workflow_role": {"values": ["tool"]},
                        "carry_frequency": {"value": "everyday"}
                      }
                    },
                    "slot:test_saw": {
                      "facets": {
                        "role": {"value": "tool"},
                        "workflow": {"values": ["woodworking"]},
                        "workflow_role": {"values": ["tool"]},
                        "carry_frequency": {"value": "everyday"}
                      }
                    },
                    "slot:test_smithing_tongs": {
                      "facets": {
                        "role": {"value": "tool"},
                        "primary_uses": {"values": ["heat and smith"]},
                        "workflow_role": {"values": ["tool"]},
                        "carry_frequency": {"value": "everyday"}
                      }
                    },
                    "slot:test_pickaxe": {
                      "facets": {
                        "role": {"value": "tool"},
                        "workflow": {"values": ["mining"]},
                        "workflow_role": {"values": ["tool"]},
                        "carry_frequency": {"value": "everyday"}
                      }
                    }
                  }
                }
                """;
    }
}
