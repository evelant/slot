package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.classification.FacetIndex;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.workflow.domain.ContextualSignalEvent;
import dev.imagio.slot.workflow.domain.ContextualSignalKind;
import dev.imagio.slot.workflow.domain.ContextualSuggestionState;
import dev.imagio.slot.workflow.domain.CraftRunAlternative;
import dev.imagio.slot.workflow.domain.CraftRunIngredientGroup;
import dev.imagio.slot.workflow.domain.CraftRunRecipeEntry;
import dev.imagio.slot.workflow.domain.CraftRunState;
import dev.imagio.slot.workflow.domain.DomainEventMetadata;
import dev.imagio.slot.workflow.domain.InMemoryWorkflowDomainStateRepository;
import dev.imagio.slot.workflow.domain.WorkflowDomainSnapshot;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContextualSuggestionScorerTest {
    @Test
    void usefulNowUsesPickupAsContextWithoutRepeatingThePickedUpItem() {
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
                        storedItem("minecraft:iron_ore", true)),
                repository.snapshot(),
                index,
                12,
                36);

        List<String> useful = laneItems(lanes, SlotWorkspaceViewModel.ContextualSuggestionLane.USEFUL_NOW);
        assertFalse(useful.contains("minecraft:charcoal"));
        assertTrue(useful.contains("minecraft:iron_ore"));
        List<String> reasons = debugReasons(
                lanes,
                SlotWorkspaceViewModel.ContextualSuggestionLane.USEFUL_NOW,
                "minecraft:iron_ore");
        assertTrue(reasons.stream().anyMatch(reason -> reason.startsWith("candidate: carried=false")));
        assertTrue(reasons.stream().anyMatch(reason -> reason.startsWith("context relevance ")));
        assertTrue(reasons.stream().anyMatch(reason -> reason.startsWith("score terms: relevance ")));
    }

    @Test
    void usefulNowIncludesNearbyStoredGhostsButNotRemoteOnlyGhosts() {
        FacetIndex index = FacetIndex.load(new StringReader(facetLayer()));
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        repository.appendContextualSignal(
                ContextualSignalEvent.item(
                        ContextualSignalKind.ITEM_ACQUIRED,
                        ItemIdentity.of("minecraft:iron_ore"),
                        1,
                        ""),
                DomainEventMetadata.origin("test"));

        List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = ContextualSuggestionScorer.lanes(
                List.of(
                        storedItem("minecraft:charcoal", true),
                        storedItem("minecraft:bowl", false)),
                repository.snapshot(),
                index,
                12,
                36);

        List<String> useful = laneItems(lanes, SlotWorkspaceViewModel.ContextualSuggestionLane.USEFUL_NOW);
        assertTrue(useful.contains("minecraft:charcoal"));
        assertFalse(useful.contains("minecraft:bowl"));
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
    void putAwayDoesNotUseRareCarryFrequencyAloneWhenPressureIsModerate() {
        FacetIndex index = FacetIndex.load(new StringReader(facetLayer()));

        List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = ContextualSuggestionScorer.lanes(
                List.of(item("minecraft:flower_pot", true, BuiltinInventoryIds.PLAYER_MAIN)),
                WorkflowDomainSnapshot.empty(),
                index,
                20,
                36);

        assertFalse(laneItems(lanes, SlotWorkspaceViewModel.ContextualSuggestionLane.PUT_AWAY)
                .contains("minecraft:flower_pot"));
    }

    @Test
    void putAwayCarryFrequencyDoesNotNeedContextualCarryHistory() {
        FacetIndex index = FacetIndex.load(new StringReader(facetLayer()));

        List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = ContextualSuggestionScorer.lanes(
                List.of(item("minecraft:flower_pot", true, BuiltinInventoryIds.PLAYER_MAIN)),
                WorkflowDomainSnapshot.empty(),
                index,
                4,
                36,
                120L);

        assertTrue(laneItems(lanes, SlotWorkspaceViewModel.ContextualSuggestionLane.PUT_AWAY)
                .contains("minecraft:flower_pot"));
    }

    @Test
    void putAwayCanUseInventoryPressureWhenFacetPriorIsUnknown() {
        List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = ContextualSuggestionScorer.lanes(
                List.of(item("minecraft:dirt", true, BuiltinInventoryIds.PLAYER_MAIN)),
                WorkflowDomainSnapshot.empty(),
                FacetIndex.empty(),
                0,
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
    void usefulNowDoesNotPromoteCarriedCandidateWithoutStrongEvent() {
        FacetIndex index = FacetIndex.load(new StringReader(facetLayer()));

        List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = ContextualSuggestionScorer.lanes(
                List.of(item("minecraft:charcoal", true, BuiltinInventoryIds.PLAYER_MAIN)),
                WorkflowDomainSnapshot.empty(),
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

        List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = ContextualSuggestionScorer.lanes(
                List.of(item(
                        "minecraft:charcoal",
                        true,
                        "sophisticatedbackpacks:carried/test",
                        32,
                        false,
                        0)),
                WorkflowDomainSnapshot.empty(),
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
    void usefulNowDepositPenaltyClearsWhenItemIsActivelyUsedAgain() {
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
                        ContextualSignalKind.ITEM_USED,
                        ItemIdentity.of("minecraft:charcoal"),
                        1,
                        110L,
                        "world:right_click_block",
                        "block:tfc:charcoal_forge",
                        BuiltinInventoryIds.PLAYER_MAIN,
                        Map.of("action", "right_click_block", "target", "block:tfc:charcoal_forge")),
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
                        ContextualSignalKind.ITEM_PLACED,
                        ItemIdentity.of("minecraft:oak_log"),
                        8,
                        100L,
                        "world:place_block",
                        "block:minecraft:oak_log",
                        BuiltinInventoryIds.PLAYER_MAIN,
                        Map.of("action", "place_block", "target", "block:minecraft:oak_log")),
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
        assertTrue(reasons.stream().anyMatch(reason -> reason.startsWith("history matches: ")));
        assertTrue(reasons.stream().anyMatch(reason -> reason.startsWith("score terms: relevance ")));
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
    void usefulNowDoesNotSelfPromotePlacedSaplings() {
        FacetIndex index = FacetIndex.load(new StringReader(facetLayer()));
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        repository.appendContextualSignal(
                new ContextualSignalEvent(
                        ContextualSignalKind.ITEM_USED,
                        ItemIdentity.of("minecraft:oak_sapling"),
                        1,
                        100L,
                        "world:right_click_block",
                        "block:minecraft:dirt",
                        "hand:main_hand",
                        Map.of("action", "right_click_block", "target", "block:minecraft:dirt")),
                DomainEventMetadata.origin("test"));
        repository.appendContextualSignal(
                new ContextualSignalEvent(
                        ContextualSignalKind.ITEM_PLACED,
                        ItemIdentity.of("minecraft:oak_sapling"),
                        1,
                        101L,
                        "world:place_block",
                        "block:minecraft:oak_sapling",
                        "hand:main_hand",
                        Map.of("action", "place_block", "target", "block:minecraft:oak_sapling")),
                DomainEventMetadata.origin("test"));

        List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = ContextualSuggestionScorer.lanes(
                List.of(item("minecraft:oak_sapling", true, BuiltinInventoryIds.PLAYER_MAIN)),
                repository.snapshot(),
                index,
                12,
                36,
                120L);

        assertFalse(laneItems(lanes, SlotWorkspaceViewModel.ContextualSuggestionLane.USEFUL_NOW)
                .contains("minecraft:oak_sapling"));
    }

    @Test
    void usefulNowIgnoresPersistedPlacedSelfAssociations() {
        FacetIndex index = FacetIndex.load(new StringReader(facetLayer()));
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        ContextualSuggestionState base = repository.contextualSuggestionState();
        ContextualSuggestionState withPollutedAssociation = new ContextualSuggestionState(
                base.nextStreamSequence(),
                base.itemAggregates(),
                base.contextAggregates(),
                base.associationIndex().learnNextItem(
                        "item_placed|item=minecraft:oak_sapling|target=block:minecraft:oak_sapling",
                        ItemIdentity.of("minecraft:oak_sapling"),
                        1L,
                        1L,
                        4.0D),
                base.recentSignals(),
                base.activeContextKey());
        repository.replaceContextualSuggestionState(withPollutedAssociation);
        repository.appendContextualSignal(
                new ContextualSignalEvent(
                        ContextualSignalKind.ITEM_PLACED,
                        ItemIdentity.of("minecraft:oak_sapling"),
                        1,
                        100L,
                        "world:place_block",
                        "block:minecraft:oak_sapling",
                        "hand:main_hand",
                        Map.of("action", "place_block", "target", "block:minecraft:oak_sapling")),
                DomainEventMetadata.origin("test"));

        List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = ContextualSuggestionScorer.lanes(
                List.of(item("minecraft:oak_sapling", true, BuiltinInventoryIds.PLAYER_MAIN)),
                repository.snapshot(),
                index,
                12,
                36,
                120L);

        assertFalse(laneItems(lanes, SlotWorkspaceViewModel.ContextualSuggestionLane.USEFUL_NOW)
                .contains("minecraft:oak_sapling"));
    }

    @Test
    void usefulNowIgnoresPersistedNonToolUseSelfAssociations() {
        FacetIndex index = FacetIndex.load(new StringReader(facetLayer()));
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        ContextualSuggestionState base = repository.contextualSuggestionState();
        repository.replaceContextualSuggestionState(new ContextualSuggestionState(
                base.nextStreamSequence(),
                base.itemAggregates(),
                base.contextAggregates(),
                base.associationIndex().learnNextItem(
                        "item_used|item=minecraft:oak_sapling|action=right_click_block|target=block:minecraft:dirt",
                        ItemIdentity.of("minecraft:oak_sapling"),
                        1L,
                        1L,
                        4.0D),
                base.recentSignals(),
                base.activeContextKey()));
        repository.appendContextualSignal(
                new ContextualSignalEvent(
                        ContextualSignalKind.ITEM_USED,
                        ItemIdentity.of("minecraft:oak_sapling"),
                        1,
                        100L,
                        "world:right_click_block",
                        "block:minecraft:dirt",
                        "hand:main_hand",
                        Map.of("action", "right_click_block", "target", "block:minecraft:dirt")),
                DomainEventMetadata.origin("test"));

        List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = ContextualSuggestionScorer.lanes(
                List.of(item("minecraft:oak_sapling", true, BuiltinInventoryIds.PLAYER_MAIN)),
                repository.snapshot(),
                index,
                12,
                36,
                120L);

        assertFalse(laneItems(lanes, SlotWorkspaceViewModel.ContextualSuggestionLane.USEFUL_NOW)
                .contains("minecraft:oak_sapling"));
    }

    @Test
    void usefulNowRecentToolUseExpiresByWallClockTicks() {
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
                List.of(item("slot:test_saw", true, BuiltinInventoryIds.PLAYER_MAIN)),
                repository.snapshot(),
                index,
                12,
                36,
                20000L);

        assertFalse(laneItems(lanes, SlotWorkspaceViewModel.ContextualSuggestionLane.USEFUL_NOW)
                .contains("slot:test_saw"));
    }

    @Test
    void usefulNowReservesRoomForNearbyStorageGhosts() {
        FacetIndex index = FacetIndex.load(new StringReader(facetLayer()));
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        for (String itemId : List.of(
                "slot:test_construction_wand",
                "slot:test_grappling_hook",
                "slot:test_hoe",
                "slot:test_knife",
                "slot:test_pickaxe",
                "slot:test_saw")) {
            repository.appendContextualSignal(
                    new ContextualSignalEvent(
                            ContextualSignalKind.ITEM_USED,
                            ItemIdentity.of(itemId),
                            1,
                            100L,
                            "world:right_click_block",
                            "block:minecraft:oak_log",
                            "hand:main_hand",
                            Map.of("action", "right_click_block", "target", "block:minecraft:oak_log")),
                    DomainEventMetadata.origin("test"));
        }
        repository.appendContextualSignal(
                ContextualSignalEvent.item(
                        ContextualSignalKind.ITEM_ACQUIRED,
                        ItemIdentity.of("minecraft:iron_ore"),
                        1,
                        ""),
                DomainEventMetadata.origin("test"));

        List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = ContextualSuggestionScorer.lanes(
                List.of(
                        item("slot:test_construction_wand", true, BuiltinInventoryIds.PLAYER_MAIN),
                        item("slot:test_grappling_hook", true, BuiltinInventoryIds.PLAYER_MAIN),
                        item("slot:test_hoe", true, BuiltinInventoryIds.PLAYER_MAIN),
                        item("slot:test_knife", true, BuiltinInventoryIds.PLAYER_MAIN),
                        item("slot:test_pickaxe", true, BuiltinInventoryIds.PLAYER_MAIN),
                        item("slot:test_saw", true, BuiltinInventoryIds.PLAYER_MAIN),
                        storedItem("minecraft:charcoal", true)),
                repository.snapshot(),
                index,
                12,
                36,
                120L);

        assertTrue(laneItems(lanes, SlotWorkspaceViewModel.ContextualSuggestionLane.USEFUL_NOW)
                .contains("minecraft:charcoal"));
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
    void usefulNowIgnoresStorageOpenRightClickNoiseForNearbyFuelGhosts() {
        FacetIndex index = FacetIndex.load(new StringReader(facetLayer()));
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        repository.appendContextualSignal(
                new ContextualSignalEvent(
                        ContextualSignalKind.ITEM_USED,
                        ItemIdentity.of("slot:test_wrench"),
                        1,
                        100L,
                        "world:right_click_block",
                        "block:tfc:wood/chest/blackwood",
                        "hand:main_hand",
                        Map.of("action", "right_click_block", "target", "block:tfc:wood/chest/blackwood")),
                DomainEventMetadata.origin("test"));

        List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = ContextualSuggestionScorer.lanes(
                List.of(storedItem("afc:wood/fallen_leaves/black_oak", true)),
                repository.snapshot(),
                index,
                12,
                36,
                120L);

        assertFalse(laneItems(lanes, SlotWorkspaceViewModel.ContextualSuggestionLane.USEFUL_NOW)
                .contains("afc:wood/fallen_leaves/black_oak"));
    }

    @Test
    void usefulNowDoesNotMirrorQuickAccessMachineWrenchOrPromoteLeaves() {
        FacetIndex index = FacetIndex.load(new StringReader(facetLayer()));
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        repository.appendContextualSignal(
                new ContextualSignalEvent(
                        ContextualSignalKind.ITEM_USED,
                        ItemIdentity.of("slot:test_wrench"),
                        1,
                        100L,
                        "world:right_click_block",
                        "block:create:rotation_speed_controller",
                        "hand:main_hand",
                        Map.of("action", "right_click_block", "target", "block:create:rotation_speed_controller")),
                DomainEventMetadata.origin("test"));

        List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = ContextualSuggestionScorer.lanes(
                List.of(
                        item("slot:test_wrench", true, BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0),
                        storedItem("afc:wood/fallen_leaves/black_oak", true)),
                repository.snapshot(),
                index,
                12,
                36,
                120L);

        List<String> useful = laneItems(lanes, SlotWorkspaceViewModel.ContextualSuggestionLane.USEFUL_NOW);
        assertFalse(useful.contains("slot:test_wrench"));
        assertFalse(useful.contains("afc:wood/fallen_leaves/black_oak"));
    }

    @Test
    void usefulNowDoesNotPromoteNearbyGhostFromWeakAdvisoryOnlyMatch() {
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
                List.of(storedItem("afc:wood/fallen_leaves/black_oak", true)),
                repository.snapshot(),
                index,
                12,
                36,
                120L);

        assertFalse(laneItems(lanes, SlotWorkspaceViewModel.ContextualSuggestionLane.USEFUL_NOW)
                .contains("afc:wood/fallen_leaves/black_oak"));
    }

    @Test
    void targetlessRightClickDoesNotCreateGhostContextOrQuickAccessSuggestion() {
        FacetIndex index = FacetIndex.load(new StringReader(facetLayer()));
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        repository.appendContextualSignal(
                new ContextualSignalEvent(
                        ContextualSignalKind.ITEM_USED,
                        ItemIdentity.of("slot:test_wrench"),
                        1,
                        100L,
                        "world:right_click_block",
                        "block:minecraft:air",
                        "hand:main_hand",
                        Map.of("action", "right_click_block", "target", "block:minecraft:air")),
                DomainEventMetadata.origin("test"));

        List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = ContextualSuggestionScorer.lanes(
                List.of(
                        item("slot:test_wrench", true, BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0),
                        storedItem("afc:wood/fallen_leaves/black_oak", true)),
                repository.snapshot(),
                index,
                12,
                36,
                120L);

        List<String> useful = laneItems(lanes, SlotWorkspaceViewModel.ContextualSuggestionLane.USEFUL_NOW);
        assertFalse(useful.contains("slot:test_wrench"));
        assertFalse(useful.contains("afc:wood/fallen_leaves/black_oak"));
    }

    @Test
    void usefulNowDeduplicatesMultipleCardsForSameIdentity() {
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
                        item("slot:test_saw", true, "sophisticatedbackpacks:carried/test")),
                repository.snapshot(),
                index,
                12,
                36,
                120L);

        List<String> useful = laneItems(lanes, SlotWorkspaceViewModel.ContextualSuggestionLane.USEFUL_NOW);
        assertEquals(1, useful.stream().filter("slot:test_saw"::equals).count());
    }

    @Test
    void usefulNowIgnoresPersistedPassiveOffhandUseAndKeepsMainHandMaterialUse() {
        FacetIndex index = FacetIndex.load(new StringReader(facetLayer()));
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        for (int i = 0; i < 16; i++) {
            repository.appendContextualSignal(
                    new ContextualSignalEvent(
                            ContextualSignalKind.ITEM_USED,
                            ItemIdentity.of("tfc:metal/shield/bronze"),
                            1,
                            100L + i,
                            "world:right_click_block",
                            "block:tfc:wood/planks/pine_loom",
                            "hand:off_hand",
                            Map.of("action", "right_click_block", "target", "block:tfc:wood/planks/pine_loom")),
                    DomainEventMetadata.origin("test"));
            repository.appendContextualSignal(
                    new ContextualSignalEvent(
                            ContextualSignalKind.ITEM_USED,
                            ItemIdentity.of("tfc:wool_yarn"),
                            1,
                            100L + i,
                            "world:right_click_block",
                            "block:tfc:wood/planks/pine_loom",
                            "hand:main_hand",
                            Map.of("action", "right_click_block", "target", "block:tfc:wood/planks/pine_loom")),
                    DomainEventMetadata.origin("test"));
        }

        List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = ContextualSuggestionScorer.lanes(
                List.of(
                        item("tfc:metal/shield/bronze", true, BuiltinInventoryIds.PLAYER_OFFHAND),
                        item("tfc:wool_yarn", true, BuiltinInventoryIds.PLAYER_MAIN)),
                repository.snapshot(),
                index,
                12,
                36,
                140L);

        List<String> useful = laneItems(lanes, SlotWorkspaceViewModel.ContextualSuggestionLane.USEFUL_NOW);
        assertTrue(useful.contains("tfc:wool_yarn"));
        assertFalse(useful.contains("tfc:metal/shield/bronze"));
    }

    @Test
    void usefulNowDoesNotPromoteCopperStorageGhostsFromToolMaterialOnly() {
        FacetIndex index = FacetIndex.load(new StringReader(facetLayer()));
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        repository.appendContextualSignal(
                new ContextualSignalEvent(
                        ContextualSignalKind.ITEM_USED,
                        ItemIdentity.of("gtceu:copper_hammer"),
                        1,
                        100L,
                        "world:right_click_block",
                        "block:tfc:wood/planks/pine_loom",
                        "hand:main_hand",
                        Map.of("action", "right_click_block", "target", "block:tfc:wood/planks/pine_loom")),
                DomainEventMetadata.origin("test"));

        List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = ContextualSuggestionScorer.lanes(
                List.of(
                        item("gtceu:copper_hammer", true, BuiltinInventoryIds.PLAYER_MAIN),
                        storedItem("create:copper_valve_handle", true)),
                repository.snapshot(),
                index,
                12,
                36,
                120L);

        List<String> useful = laneItems(lanes, SlotWorkspaceViewModel.ContextualSuggestionLane.USEFUL_NOW);
        assertTrue(useful.contains("gtceu:copper_hammer"));
        assertFalse(useful.contains("create:copper_valve_handle"));
    }

    @Test
    void usefulNowDoesNotPromoteCarriedItemsFromWeakTextOnlyContext() {
        FacetIndex index = FacetIndex.load(new StringReader(facetLayer()));
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        repository.appendContextualSignal(
                ContextualSignalEvent.item(
                        ContextualSignalKind.ITEM_ACQUIRED,
                        ItemIdentity.of("minecraft:oak_log"),
                        1,
                        ""),
                DomainEventMetadata.origin("test"));

        List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = ContextualSuggestionScorer.lanes(
                List.of(item("afc:wood/sapling/giant_rosewood", true, BuiltinInventoryIds.PLAYER_MAIN)),
                repository.snapshot(),
                index,
                12,
                36,
                120L);

        assertFalse(laneItems(lanes, SlotWorkspaceViewModel.ContextualSuggestionLane.USEFUL_NOW)
                .contains("afc:wood/sapling/giant_rosewood"));
    }

    @Test
    void usefulNowDoesNotPromoteStorageGhostFromWeakAssociationAndGenericAdvisory() {
        FacetIndex index = FacetIndex.load(new StringReader(facetLayer()));
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        ContextualSuggestionState base = repository.contextualSuggestionState();
        repository.replaceContextualSuggestionState(new ContextualSuggestionState(
                base.nextStreamSequence(),
                base.itemAggregates(),
                base.contextAggregates(),
                base.associationIndex().learnNextItem(
                        "item_taken|item=gtceu:wrought_iron_plate",
                        ItemIdentity.of("minecraft:brick"),
                        1L,
                        18L,
                        0.48D),
                base.recentSignals(),
                base.activeContextKey()));
        repository.appendContextualSignal(
                ContextualSignalEvent.item(
                        ContextualSignalKind.ITEM_TAKEN_FROM_STORAGE,
                        ItemIdentity.of("gtceu:wrought_iron_plate"),
                        1,
                        ""),
                DomainEventMetadata.origin("test"));

        List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = ContextualSuggestionScorer.lanes(
                List.of(storedItem("minecraft:brick", true)),
                repository.snapshot(),
                index,
                12,
                36,
                120L);

        assertFalse(laneItems(lanes, SlotWorkspaceViewModel.ContextualSuggestionLane.USEFUL_NOW)
                .contains("minecraft:brick"));
    }

    @Test
    void usefulNowUsesSpentAndDamagedSignalsWithoutSelfPromotingPlacedItems() {
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
        assertFalse(useful.contains("minecraft:oak_log"));
        assertTrue(useful.contains("minecraft:bowl"));
        assertTrue(useful.contains("slot:test_saw"));
    }

    @Test
    void putAwayExcludesProtectedTargetsAndCraftRunItems() {
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
        SlotWorkspaceViewModel.AtlasItem craftRunOre = item(
                "minecraft:iron_ore",
                true,
                BuiltinInventoryIds.PLAYER_MAIN);

        WorkflowDomainSnapshot snapshot = new WorkflowDomainSnapshot(
                1L,
                null,
                null,
                null,
                null,
                null,
                null,
                craftRun("minecraft:iron_ore"),
                null);
        List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = ContextualSuggestionScorer.lanes(
                List.of(hotbarDecor, desiredDecor, wantedDecor, craftRunOre),
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
        SlotWorkspaceViewModel.AtlasItem excessCraftRunItem = item(
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
                null,
                null,
                null,
                null,
                null,
                null,
                craftRun("minecraft:iron_ore"),
                null);
        List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = ContextualSuggestionScorer.lanes(
                List.of(excessCraftRunItem),
                snapshot,
                index,
                12,
                36);

        assertTrue(laneItems(lanes, SlotWorkspaceViewModel.ContextualSuggestionLane.PUT_AWAY)
                .contains("minecraft:iron_ore"));
    }

    @Test
    void putAwayCapsDesiredExcessSoCleanupDoesNotBecomeAQuotaAudit() {
        FacetIndex index = FacetIndex.load(new StringReader(facetLayer()));

        List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = ContextualSuggestionScorer.lanes(
                List.of(
                        item("minecraft:iron_ore", true, BuiltinInventoryIds.PLAYER_MAIN, 1, false, 0, false, 8),
                        item("minecraft:charcoal", true, BuiltinInventoryIds.PLAYER_MAIN, 1, false, 0, false, 8),
                        item("minecraft:carrot", true, BuiltinInventoryIds.PLAYER_MAIN, 1, false, 0, false, 8),
                        item("minecraft:bowl", true, BuiltinInventoryIds.PLAYER_MAIN, 1, false, 0, false, 8),
                        item("minecraft:flower_pot", true, BuiltinInventoryIds.PLAYER_MAIN)),
                WorkflowDomainSnapshot.empty(),
                index,
                0,
                36);

        List<String> putAway = laneItems(lanes, SlotWorkspaceViewModel.ContextualSuggestionLane.PUT_AWAY);
        long desiredExcessCards = putAway.stream()
                .filter(itemId -> List.of("minecraft:iron_ore", "minecraft:charcoal", "minecraft:carrot", "minecraft:bowl")
                        .contains(itemId))
                .count();
        assertEquals(2, desiredExcessCards);
        assertTrue(putAway.contains("minecraft:flower_pot"));
    }

    @Test
    void putAwayIgnoresStaleSingleDepositHistoryWithoutRoute() {
        FacetIndex index = FacetIndex.load(new StringReader(facetLayer()));
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        repository.appendContextualSignal(
                new ContextualSignalEvent(
                        ContextualSignalKind.ITEM_DEPOSITED_TO_STORAGE,
                        ItemIdentity.of("minecraft:flower_pot"),
                        1,
                        100L,
                        "",
                        "",
                        "storage",
                        Map.of()),
                DomainEventMetadata.origin("test"));
        repository.appendContextualSignal(
                ContextualSignalEvent.item(
                        ContextualSignalKind.ITEM_ACQUIRED,
                        ItemIdentity.of("minecraft:flower_pot"),
                        1,
                        BuiltinInventoryIds.PLAYER_MAIN),
                DomainEventMetadata.origin("test"));

        List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = ContextualSuggestionScorer.lanes(
                List.of(item("minecraft:flower_pot", true, BuiltinInventoryIds.PLAYER_MAIN)),
                repository.snapshot(),
                index,
                12,
                36);

        assertFalse(laneItems(lanes, SlotWorkspaceViewModel.ContextualSuggestionLane.PUT_AWAY)
                .contains("minecraft:flower_pot"));
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
                        item("minecraft:charcoal", true, BuiltinInventoryIds.PLAYER_MAIN),
                        item("minecraft:bowl", true, BuiltinInventoryIds.PLAYER_MAIN)),
                repository.snapshot(),
                index,
                8,
                36);

        List<String> useful = laneItems(lanes, SlotWorkspaceViewModel.ContextualSuggestionLane.USEFUL_NOW);
        assertTrue(useful.contains("minecraft:charcoal"));
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

    private static SlotWorkspaceViewModel.AtlasItem storedItem(String itemId, boolean nearby) {
        return new SlotWorkspaceViewModel.AtlasItem(
                SlotWorkspaceViewModel.IdentityRef.from(ItemIdentity.of(itemId)),
                new ItemStack(itemId, 1, 64),
                itemId,
                1,
                0,
                "main",
                false,
                false,
                false,
                true,
                nearby ? 1 : 0,
                List.of(),
                nearby ? List.of(new SlotWorkspaceViewModel.ChestPresenceEntry("near", "Near Chest", 1)) : List.of(),
                nearby ? List.of() : List.of(new SlotWorkspaceViewModel.ChestPresenceEntry("remote", "Remote Chest", 1)),
                false,
                0,
                0,
                false,
                0,
                false,
                0,
                "",
                -1,
                0);
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

    private static CraftRunState craftRun(String itemId) {
        ItemIdentity identity = ItemIdentity.of(itemId);
        CraftRunRecipeEntry entry = new CraftRunRecipeEntry(
                "craft-run-test",
                1,
                "test:recipe",
                "test:recipe",
                "Craft run",
                identity,
                itemId,
                1,
                1,
                List.of(new CraftRunIngredientGroup(
                        "input",
                        itemId,
                        1,
                        List.of(new CraftRunAlternative(identity, itemId)),
                        List.of())),
                List.of());
        return new CraftRunState(1, entry.entryId(), List.of(entry));
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
                    "minecraft:oak_sapling": {
                      "facets": {
                        "role": {"value": "block"},
                        "workflow": {"values": ["forestry"]},
                        "workflow_role": {"values": ["seed"]},
                        "primary_uses": {"values": ["planting trees"]},
                        "carry_frequency": {"value": "occasional"}
                      }
                    },
                    "minecraft:brick": {
                      "facets": {
                        "role": {"value": "ingredient"},
                        "primary_uses": {"values": ["crafting building material"]},
                        "carry_frequency": {"value": "rare"}
                      }
                    },
                    "gtceu:wrought_iron_plate": {
                      "facets": {
                        "role": {"value": "ingredient"},
                        "primary_uses": {"values": ["crafting machine components"]},
                        "carry_frequency": {"value": "occasional"}
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
                    },
                    "slot:test_wrench": {
                      "facets": {
                        "role": {"value": "tool"},
                        "primary_uses": {"values": ["dismantling machines blocks"]},
                        "workflow_role": {"values": ["tool"]},
                        "carry_frequency": {"value": "everyday"}
                      }
                    },
                    "gtceu:copper_hammer": {
                      "facets": {
                        "role": {"value": "tool"},
                        "material_family": {"value": "copper"},
                        "form": {"value": "tool"},
                        "workflow_role": {"values": ["tool"]},
                        "carry_frequency": {"value": "everyday"}
                      }
                    },
                    "create:copper_valve_handle": {
                      "facets": {
                        "role": {"value": "material"},
                        "material_family": {"value": "copper"},
                        "carry_frequency": {"value": "occasional"}
                      }
                    },
                    "tfc:metal/shield/bronze": {
                      "facets": {
                        "role": {"value": "equipment"},
                        "primary_uses": {"values": ["blocking attacks defense"]},
                        "workflow_role": {"values": ["equipment"]},
                        "carry_frequency": {"value": "everyday"}
                      }
                    },
                    "tfc:wool_yarn": {
                      "facets": {
                        "role": {"value": "material"},
                        "workflow": {"values": ["weaving"]},
                        "workflow_role": {"values": ["input"]},
                        "used_at": {"values": ["loom"]},
                        "carry_frequency": {"value": "occasional"}
                      }
                    },
                    "afc:wood/sapling/giant_rosewood": {
                      "facets": {
                        "role": {"value": "block"},
                        "workflow": {"values": ["forestry"]},
                        "workflow_role": {"values": ["seed"]},
                        "primary_uses": {"values": ["planting trees"]},
                        "carry_frequency": {"value": "occasional"}
                      }
                    },
                    "afc:wood/fallen_leaves/black_oak": {
                      "facets": {
                        "role": {"value": "block"},
                        "primary_uses": {"values": ["fuel"]},
                        "used_at": {"values": ["campfire"]},
                        "carry_frequency": {"value": "rare"},
                        "is_fuel": {"value": true}
                      }
                    }
                  }
                }
                """;
    }
}
