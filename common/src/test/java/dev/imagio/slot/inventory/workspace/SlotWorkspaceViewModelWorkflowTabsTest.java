package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.BuiltinInventoryDescriptors;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.HostInstanceKey;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventoryStackSnapshot;
import dev.imagio.slot.inventory.core.InventoryTopologyDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.PlayerRuntimeStateDescriptor;
import dev.imagio.slot.inventory.integration.InventoryHostObservationHints;
import dev.imagio.slot.inventory.integration.InventoryHostSession;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.storage.WorldDisplayStorageKind;
import dev.imagio.slot.inventory.storage.WorldDisplayStorageSource;
import dev.imagio.slot.inventory.storage.WorldStorageAccess;
import dev.imagio.slot.testsupport.InventoryAuthorityFixtures;
import dev.imagio.slot.ui.workspace.StorageGhostRevealMode;
import dev.imagio.slot.ui.workspace.WallSectionVisibility;
import dev.imagio.slot.ui.workspace.WorkspaceItemTooltipBuilder;
import dev.imagio.slot.workflow.domain.InMemoryWorkflowDomainStateRepository;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.CraftRunAlternative;
import dev.imagio.slot.workflow.domain.CraftRunIngredientGroup;
import dev.imagio.slot.workflow.domain.CraftRunRecipeCapture;
import dev.imagio.slot.workflow.domain.KitDefinition;
import dev.imagio.slot.workflow.domain.VisualAtlasIsland;
import dev.imagio.slot.workflow.domain.WorkflowAcceptedInputRule;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlotWorkspaceViewModelWorkflowTabsTest {
    @Test
    void kitCardMembershipUsesMovableIdentitySemantics() {
        ItemIdentity damagedHammer = ItemIdentity.exact("gtceu:steel_mining_hammer", "{Damage:512}");
        ItemIdentity toolStateHammer = ItemIdentity.exact(
                "gtceu:steel_mining_hammer",
                "{Damage:12,\"GT.Tool\":{MaxDamage:960}}");
        SlotWorkspaceViewModel.KitCard card = new SlotWorkspaceViewModel.KitCard(
                "kit-1",
                "Mining",
                "",
                1,
                0,
                true,
                false,
                1,
                List.of(SlotWorkspaceViewModel.IdentityRef.from(damagedHammer)),
                List.of(),
                0,
                0,
                0,
                0,
                0,
                0,
                List.of(),
                List.of(),
                List.of());

        assertTrue(card.hasMember(SlotWorkspaceViewModel.IdentityRef.from(toolStateHammer)));
    }

    @Test
    void activeWorkflowKeepsUnrelatedCarriedCardsVisible() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        KitDefinition mining = runtime.kitWorkflow().create("Mining");
        runtime.kitWorkflow().setMember(mining.id(), ItemIdentity.of("minecraft:torch"), true);
        runtime.kitWorkflow().activate(mining.id(), 0, Set.of(ItemIdentity.of("minecraft:dirt")));

        SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                carried(
                        new InventoryStackSnapshot(0, new ItemStack("minecraft:torch", 8, 64), 8),
                        new InventoryStackSnapshot(1, new ItemStack("minecraft:dirt", 64, 64), 64)),
                runtime.snapshot(),
                "ready",
                "",
                0,
                0,
                1L);

        List<String> triageIds = viewModel.triageItems().stream()
                .map(item -> item.identity().itemId())
                .toList();
        assertTrue(triageIds.contains("minecraft:torch"));
        assertTrue(triageIds.contains("minecraft:dirt"));

        SlotWorkspaceViewModel.AtlasItem dirt = viewModel.triageItems().stream()
                .filter(item -> "minecraft:dirt".equals(item.identity().itemId()))
                .findFirst()
                .orElseThrow();
        assertTrue(dirt.carried());
        assertEquals(SlotWorkspaceViewModel.PutAwayState.NO_ROUTE, dirt.putAwayState());

        SlotWorkspaceViewModel.ContextualSuggestionLane putAway = viewModel.contextualSuggestionLanes().stream()
                .filter(SlotWorkspaceViewModel.ContextualSuggestionLane::putAway)
                .findFirst()
                .orElseThrow();
        assertEquals(
                List.of("minecraft:dirt"),
                putAway.items().stream().map(item -> item.identity().itemId()).toList());
        assertTrue(putAway.items().get(0).putAwayState().noRoute());
        assertNotNull(viewModel.atlasItem(putAway.items().get(0).identity()));
    }

    @Test
    void activeWorkflowPutAwayWayfindingPointsAtKnownDestination() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        KitDefinition mining = runtime.kitWorkflow().create("Mining");
        runtime.kitWorkflow().setMember(mining.id(), ItemIdentity.of("minecraft:torch"), true);
        runtime.kitWorkflow().activate(mining.id(), 0, Set.of(ItemIdentity.of("minecraft:dirt")));
        ClaimedChest chest = runtime.chestClaimWorkflow().claim(
                Set.of(new ChestAnchor("minecraft:overworld", 12, 64, -5)),
                0,
                0,
                "Blocks");

        SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                carried(new InventoryStackSnapshot(0, new ItemStack("minecraft:dirt", 64, 64), 64)),
                runtime.snapshot(),
                "ready",
                "",
                0,
                0,
                1L,
                null,
                null,
                storageId -> chest.storageId().toString().equals(storageId)
                        ? new SlotWorkspaceViewModel.ChestContentsSnapshot(
                                27,
                                List.of(new ItemStack("minecraft:dirt", 32, 64)))
                        : SlotWorkspaceViewModel.ChestContentsSnapshot.empty(),
                Set.of());

        assertEquals(1, viewModel.wayfindingTargets().size());
        WayfindingTarget target = viewModel.wayfindingTargets().get(0);
        assertEquals(chest.storageId().toString(), target.storageId());
        assertEquals(WayfindingTarget.Scope.PUT_AWAY, target.scope());
        assertTrue(target.putAwayIdentities().contains(ItemIdentity.of("minecraft:dirt")));
        assertTrue(target.missingIdentities().contains(ItemIdentity.of("minecraft:dirt")));
        assertEquals(64, target.totalMissingCount());
        assertEquals(12, target.worldX());
        assertEquals(64, target.worldY());
        assertEquals(-5, target.worldZ());

        SlotWorkspaceViewModel.AtlasItem dirt = viewModel.triageItems().stream()
                .filter(item -> "minecraft:dirt".equals(item.identity().itemId()))
                .findFirst()
                .orElseThrow();
        assertEquals(SlotWorkspaceViewModel.PutAwayState.ROUTED, dirt.putAwayState());

        SlotWorkspaceViewModel.ContextualSuggestionLane putAway = viewModel.contextualSuggestionLanes().stream()
                .filter(SlotWorkspaceViewModel.ContextualSuggestionLane::putAway)
                .findFirst()
                .orElseThrow();
        assertEquals(List.of("minecraft:dirt"),
                putAway.items().stream().map(item -> item.identity().itemId()).toList());
        assertTrue(putAway.items().get(0).putAwayState().routed());
    }

    @Test
    void activeWorkflowPutAwayIgnoresItemsPickedUpAfterActivation() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        KitDefinition mining = runtime.kitWorkflow().create("Mining");
        runtime.kitWorkflow().setMember(mining.id(), ItemIdentity.of("minecraft:torch"), true);
        runtime.kitWorkflow().activate(mining.id(), 0, Set.of(ItemIdentity.of("minecraft:dirt")));

        SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                carried(
                        new InventoryStackSnapshot(0, new ItemStack("minecraft:dirt", 32, 64), 32),
                        new InventoryStackSnapshot(1, new ItemStack("minecraft:cobblestone", 64, 64), 64)),
                runtime.snapshot(),
                "ready",
                "",
                0,
                0,
                1L);

        SlotWorkspaceViewModel.AtlasItem dirt = viewModel.triageItems().stream()
                .filter(item -> "minecraft:dirt".equals(item.identity().itemId()))
                .findFirst()
                .orElseThrow();
        SlotWorkspaceViewModel.AtlasItem cobblestone = viewModel.triageItems().stream()
                .filter(item -> "minecraft:cobblestone".equals(item.identity().itemId()))
                .findFirst()
                .orElseThrow();

        assertEquals(SlotWorkspaceViewModel.PutAwayState.NO_ROUTE, dirt.putAwayState());
        assertEquals(SlotWorkspaceViewModel.PutAwayState.NONE, cobblestone.putAwayState());
        List<String> putAwayIds = viewModel.contextualSuggestionLanes().stream()
                .filter(SlotWorkspaceViewModel.ContextualSuggestionLane::putAway)
                .flatMap(lane -> lane.items().stream())
                .map(item -> item.identity().itemId())
                .toList();
        assertEquals(List.of("minecraft:dirt"), putAwayIds);
    }

    @Test
    void activeWorkflowSearchUsesGlobalItemAndSectionMatching() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        KitDefinition mining = runtime.kitWorkflow().create("Mining");
        runtime.kitWorkflow().setMember(mining.id(), ItemIdentity.of("minecraft:torch"), true);
        runtime.kitWorkflow().activate(mining.id());
        VisualAtlasIsland blocks = runtime.visualAtlasWorkflow().createIsland(
                "Building Blocks",
                0,
                0,
                0xFF6B8E23,
                null);
        runtime.visualAtlasWorkflow().assignHome(ItemIdentity.of("minecraft:dirt"), blocks.id(), 0);

        SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                carried(
                        new InventoryStackSnapshot(0, new ItemStack("minecraft:torch", 8, 64), 8),
                        new InventoryStackSnapshot(1, new ItemStack("minecraft:dirt", 64, 64), 64)),
                runtime.snapshot(),
                "ready",
                "",
                0,
                0,
                1L,
                null,
                null,
                storageId -> SlotWorkspaceViewModel.ChestContentsSnapshot.empty(),
                Set.of(),
                null,
                null,
                "building");

        SlotWorkspaceViewModel.AtlasItem dirt = viewModel.atlasItems().stream()
                .filter(item -> "minecraft:dirt".equals(item.identity().itemId()))
                .findFirst()
                .orElseThrow();

        assertTrue(dirt.carried());
        assertEquals(SlotWorkspaceViewModel.PutAwayState.NONE, dirt.putAwayState());
    }

    @Test
    void activeWorkflowPutAwayDoesNotSuggestEquippedArmor() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        KitDefinition mining = runtime.kitWorkflow().create("Mining");
        runtime.kitWorkflow().setMember(mining.id(), ItemIdentity.of("minecraft:torch"), true);
        runtime.kitWorkflow().activate(mining.id(), 0, Set.of(ItemIdentity.of("minecraft:iron_boots")));

        SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                carriedBySource(Map.of(
                        BuiltinInventoryIds.PLAYER_MAIN,
                        List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:torch", 8, 64), 8)),
                        BuiltinInventoryIds.PLAYER_ARMOR,
                        List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:iron_boots", 1, 1), 1)))),
                runtime.snapshot(),
                "ready",
                "",
                0,
                0,
                1L);

        List<String> putAwayIds = viewModel.contextualSuggestionLanes().stream()
                .filter(SlotWorkspaceViewModel.ContextualSuggestionLane::putAway)
                .flatMap(lane -> lane.items().stream())
                .map(item -> item.identity().itemId())
                .toList();
        assertFalse(putAwayIds.contains("minecraft:iron_boots"));
    }

    @Test
    void acceptedWorkflowInputIsKeptWithoutDesiredOrWantedCount() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        ItemIdentity ore = ItemIdentity.of("minecraft:iron_ore");
        KitDefinition smelting = runtime.kitWorkflow().create("Smelting");
        runtime.kitWorkflow().setAcceptedInput(
                smelting.id(),
                WorkflowAcceptedInputRule.exact(ore),
                true);
        runtime.kitWorkflow().activate(smelting.id(), 0, Set.of(ItemIdentity.of("minecraft:dirt")));

        SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                carried(
                        new InventoryStackSnapshot(0, new ItemStack("minecraft:iron_ore", 8, 64), 8),
                        new InventoryStackSnapshot(1, new ItemStack("minecraft:dirt", 64, 64), 64)),
                runtime.snapshot(),
                "ready",
                "",
                0,
                0,
                1L);

        SlotWorkspaceViewModel.AtlasItem acceptedOre = viewModel.triageItems().stream()
                .filter(item -> "minecraft:iron_ore".equals(item.identity().itemId()))
                .findFirst()
                .orElseThrow();
        assertEquals(0, acceptedOre.desiredCount());
        assertEquals(0, acceptedOre.wantedCount());
        assertEquals(SlotWorkspaceViewModel.PutAwayState.NONE, acceptedOre.putAwayState());

        List<String> putAwayIds = viewModel.contextualSuggestionLanes().stream()
                .filter(SlotWorkspaceViewModel.ContextualSuggestionLane::putAway)
                .flatMap(lane -> lane.items().stream())
                .map(item -> item.identity().itemId())
                .toList();
        assertFalse(putAwayIds.contains("minecraft:iron_ore"));
        assertTrue(putAwayIds.contains("minecraft:dirt"));
    }

    @Test
    void acceptedWorkflowTagRevealsNearbySubstituteGhostWithoutTargetPressure() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        ItemIdentity coke = ItemIdentity.of("tfc:coke");
        KitDefinition smelting = runtime.kitWorkflow().create("Smelting");
        runtime.kitWorkflow().setAcceptedInput(
                smelting.id(),
                WorkflowAcceptedInputRule.itemTag("tfc:blast_furnace_fuel"),
                true);
        runtime.kitWorkflow().activate(smelting.id());
        VisualAtlasIsland fuels = runtime.visualAtlasWorkflow().createIsland(
                "Fuels",
                0,
                0,
                0xFFAA6633,
                null);
        runtime.visualAtlasWorkflow().assignHome(coke, fuels.id(), 0);

        WorldDisplayStorageSource source = new WorldDisplayStorageSource(
                null,
                WorldDisplayStorageKind.PLACED_ITEM,
                "Fuel shelf @ 1,64,0",
                "minecraft:overworld",
                1,
                64,
                0,
                4,
                List.of(new WorldStorageAccess.SlotContent(
                        0,
                        new ItemStack("tfc:coke", 16, 64).withTags("tfc:blast_furnace_fuel"))));

        SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                InventoryAuthoritySnapshot.empty(),
                runtime.snapshot(),
                "ready",
                "",
                0,
                0,
                1L,
                null,
                null,
                storageId -> SlotWorkspaceViewModel.ChestContentsSnapshot.empty(),
                Set.of(),
                null,
                null,
                "",
                0L,
                SlotWorkspaceViewModel.ActiveChestPanel.empty(),
                List.of(source));

        SlotWorkspaceViewModel.AtlasItem item = viewModel.atlasItems().stream()
                .filter(candidate -> "tfc:coke".equals(candidate.identity().itemId()))
                .findFirst()
                .orElseThrow();

        assertTrue(item.ghost());
        assertEquals(16, item.proximateCount());
        assertTrue(item.acceptedWorkflowInput());
        assertFalse(item.kitNeeded());
        assertEquals(0, item.desiredCount());
        assertEquals(0, item.wantedCount());

        WallSectionVisibility.Result collapsed = WallSectionVisibility.classify(
                List.of(item),
                false,
                false,
                StorageGhostRevealMode.COLLAPSED,
                false,
                false);
        assertEquals(List.of(item), collapsed.visibleCards());
    }

    @Test
    void damagedWorkflowToolSatisfiesItemOnlyTarget() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        ItemIdentity hammer = ItemIdentity.of("gtceu:steel_mining_hammer");
        KitDefinition mining = runtime.kitWorkflow().create("Mining");
        runtime.kitWorkflow().setMember(mining.id(), hammer, true);
        runtime.kitWorkflow().activate(mining.id());

        SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                carried(new InventoryStackSnapshot(
                        0,
                        new ItemStack("gtceu:steel_mining_hammer", "{Damage:512}", 1, 1),
                        1)),
                runtime.snapshot(),
                "ready",
                "",
                0,
                0,
                1L);

        SlotWorkspaceViewModel.AtlasItem item = viewModel.triageItems().stream()
                .filter(candidate -> "gtceu:steel_mining_hammer".equals(candidate.identity().itemId()))
                .findFirst()
                .orElseThrow();

        assertTrue(item.carried());
        assertFalse(item.ghost());
        assertEquals(hammer, item.identity().toIdentity());
        assertFalse(item.kitNeeded());
        assertEquals(0, item.wantedCount());
        assertEquals(SlotWorkspaceViewModel.PutAwayState.NONE, item.putAwayState());
    }

    @Test
    void damagedOffhandWorkflowTongsSatisfyItemOnlyTarget() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        ItemIdentity tongs = ItemIdentity.of("tfc:metal/tongs/steel");
        KitDefinition smithing = runtime.kitWorkflow().create("Smithing");
        runtime.kitWorkflow().update(smithing.withOffhand(tongs));
        runtime.kitWorkflow().activate(smithing.id());

        SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                carriedBySource(Map.of(
                        BuiltinInventoryIds.PLAYER_OFFHAND,
                        List.of(new InventoryStackSnapshot(
                                0,
                                new ItemStack("tfc:metal/tongs/steel", "{Damage:50}", 1, 1),
                                1)))),
                runtime.snapshot(),
                "ready",
                "",
                0,
                0,
                1L);

        SlotWorkspaceViewModel.AtlasItem item = viewModel.triageItems().stream()
                .filter(candidate -> "tfc:metal/tongs/steel".equals(candidate.identity().itemId()))
                .findFirst()
                .orElseThrow();

        assertTrue(item.carried());
        assertFalse(item.ghost());
        assertEquals(tongs, item.identity().toIdentity());
        assertFalse(item.kitNeeded());
        assertEquals(0, item.desiredCount());
        assertEquals(SlotWorkspaceViewModel.PutAwayState.NONE, item.putAwayState());
    }

    @Test
    void damagedWorkflowToolMarksBeltSlotReady() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        ItemIdentity hammer = ItemIdentity.of("gtceu:steel_mining_hammer");
        KitDefinition mining = runtime.kitWorkflow().create("Mining");
        runtime.kitWorkflow().setSlotIdentity(mining.id(), 0, 0, hammer);
        runtime.kitWorkflow().activate(mining.id());

        SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                carried(new InventoryStackSnapshot(
                        0,
                        new ItemStack("gtceu:steel_mining_hammer", "{Damage:512}", 1, 1),
                        1)),
                runtime.snapshot(),
                "ready",
                "",
                0,
                0,
                1L);

        SlotWorkspaceViewModel.KitCard card = viewModel.kits().stream()
                .filter(candidate -> mining.id().equals(candidate.kitId()))
                .findFirst()
                .orElseThrow();

        assertEquals(1, card.readyCount());
        assertTrue(card.activePage().slots().get(0).ready());
    }

    @Test
    void exactToolStateWorkflowTargetMatchesLiveToolStateDrift() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        ItemIdentity savedHammer = ItemIdentity.exact(
                "gtceu:steel_mining_hammer",
                "{Damage:0,HideFlags:2,\"GT.Tool\":{MaxDamage:960}}");
        KitDefinition mining = runtime.kitWorkflow().create("Mining");
        runtime.kitWorkflow().setSlotIdentity(mining.id(), 0, 0, savedHammer);
        runtime.kitWorkflow().activate(mining.id());

        SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                carriedBySource(Map.of(
                        BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0,
                        List.of(new InventoryStackSnapshot(
                                0,
                                new ItemStack(
                                        "gtceu:steel_mining_hammer",
                                        "{Damage:512,HideFlags:2,\"GT.Tool\":{MaxDamage:960}}",
                                        1,
                                        1),
                                1)))),
                runtime.snapshot(),
                "ready",
                "",
                0,
                0,
                1L);

        SlotWorkspaceViewModel.KitCard card = viewModel.kits().stream()
                .filter(candidate -> mining.id().equals(candidate.kitId()))
                .findFirst()
                .orElseThrow();
        SlotWorkspaceViewModel.AtlasItem item = viewModel.triageItems().stream()
                .filter(candidate -> "gtceu:steel_mining_hammer".equals(candidate.identity().itemId()))
                .findFirst()
                .orElseThrow();

        assertEquals(1, card.readyCount());
        assertTrue(card.activePage().slots().get(0).ready());
        assertTrue(item.carried());
        assertFalse(item.kitNeeded());
        assertEquals(ItemIdentity.of("gtceu:steel_mining_hammer"), item.identity().toIdentity());
    }

    @Test
    void exactSavedToolTargetWithStackMaxDamageDoesNotCreateFetchGhost() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        ItemIdentity savedHammer = ItemIdentity.exact(
                "mod:stack_data_hammer",
                "{Damage:0,Mode:\"old\"}");
        KitDefinition mining = runtime.kitWorkflow().create("Mining");
        runtime.kitWorkflow().setSlotIdentity(mining.id(), 0, 0, savedHammer);
        runtime.kitWorkflow().activate(mining.id());

        SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                carriedBySource(Map.of(
                        BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0,
                        List.of(new InventoryStackSnapshot(
                                0,
                                new ItemStack(
                                        "mod:stack_data_hammer",
                                        "{Damage:512,Mode:\"wide\"}",
                                        1,
                                        1).maxDamage(960),
                                1)))),
                runtime.snapshot(),
                "ready",
                "",
                0,
                0,
                1L);

        SlotWorkspaceViewModel.KitCard card = viewModel.kits().stream()
                .filter(candidate -> mining.id().equals(candidate.kitId()))
                .findFirst()
                .orElseThrow();
        SlotWorkspaceViewModel.ContextualSuggestionLane fetch = fetchLane(viewModel);
        List<String> fetchItemIds = fetch == null
                ? List.of()
                : fetch.items().stream().map(item -> item.identity().itemId()).toList();

        assertEquals(1, card.readyCount());
        assertTrue(card.activePage().slots().get(0).ready());
        assertTrue(viewModel.triageItems().stream()
                .anyMatch(item -> item.carried()
                        && "mod:stack_data_hammer".equals(item.identity().itemId())
                        && !item.kitNeeded()));
        assertFalse(fetchItemIds.contains("mod:stack_data_hammer"));
    }

    @Test
    void patchouliGuideBookDesiredCountIsSatisfiedByCarriedCopyWithIncidentalData() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        ItemIdentity savedGuide = ItemIdentity.exact(
                "patchouli:guide_book",
                "{\"patchouli:book\":\"tfc:field_guide\"}");
        runtime.desiredCountWorkflow().setPlayer(savedGuide, 1);

        SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                carried(new InventoryStackSnapshot(
                        0,
                        new ItemStack(
                                "patchouli:guide_book",
                                "{display:{Name:\"TerraFirmaGreg Guide\"},\"patchouli:book\":\"tfc:field_guide\"}",
                                1,
                                1),
                        1)),
                runtime.snapshot(),
                "ready",
                "",
                0,
                0,
                1L);

        List<SlotWorkspaceViewModel.AtlasItem> guides = viewModel.triageItems().stream()
                .filter(candidate -> "patchouli:guide_book".equals(candidate.identity().itemId()))
                .toList();
        SlotWorkspaceViewModel.ContextualSuggestionLane fetch = fetchLane(viewModel);
        List<String> fetchItemIds = fetch == null
                ? List.of()
                : fetch.items().stream().map(item -> item.identity().itemId()).toList();

        assertEquals(1, guides.size());
        assertTrue(guides.getFirst().carried());
        assertFalse(guides.getFirst().ghost());
        assertEquals(ItemIdentity.exact("patchouli:guide_book", "patchouli:book=tfc:field_guide"),
                guides.getFirst().identity().toIdentity());
        assertEquals(1, guides.getFirst().desiredCount());
        assertFalse(fetchItemIds.contains("patchouli:guide_book"));
    }

    @Test
    void workflowStorageContainerWithContentsSatisfiesItemOnlyTarget() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        ItemIdentity basket = ItemIdentity.of("sns:straw_basket");
        KitDefinition gathering = runtime.kitWorkflow().create("Gathering");
        runtime.kitWorkflow().setMember(gathering.id(), basket, true);
        runtime.kitWorkflow().activate(gathering.id());

        SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                carried(new InventoryStackSnapshot(
                        0,
                        new ItemStack(
                                "sns:straw_basket",
                                "{Inventory:[{Slot:0b,id:\"minecraft:torch\",Count:8b}]}",
                                1,
                                1),
                        1)),
                runtime.snapshot(),
                "ready",
                "",
                0,
                0,
                1L);

        SlotWorkspaceViewModel.AtlasItem item = viewModel.triageItems().stream()
                .filter(candidate -> "sns:straw_basket".equals(candidate.identity().itemId()))
                .findFirst()
                .orElseThrow();

        assertTrue(item.carried());
        assertFalse(item.ghost());
        assertEquals(basket, item.identity().toIdentity());
        assertFalse(item.kitNeeded());
        assertEquals(0, item.wantedCount());
    }

    @Test
    void workflowStorageContainerInKnownStorageDoesNotRenderAsCraftTarget() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        ItemIdentity basket = ItemIdentity.of("sns:straw_basket");
        KitDefinition gathering = runtime.kitWorkflow().create("Gathering");
        runtime.kitWorkflow().setMember(gathering.id(), basket, true);
        runtime.kitWorkflow().activate(gathering.id());

        WorldDisplayStorageSource source = new WorldDisplayStorageSource(
                null,
                WorldDisplayStorageKind.PLACED_ITEM,
                "Basket shelf @ 1,64,0",
                "minecraft:overworld",
                1,
                64,
                0,
                4,
                List.of(new WorldStorageAccess.SlotContent(
                        0,
                        new ItemStack(
                                "sns:straw_basket",
                                "{Inventory:[{Slot:0b,id:\"minecraft:torch\",Count:8b}]}",
                                1,
                                1))));

        SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                InventoryAuthoritySnapshot.empty(),
                runtime.snapshot(),
                "ready",
                "",
                0,
                0,
                1L,
                null,
                null,
                storageId -> SlotWorkspaceViewModel.ChestContentsSnapshot.empty(),
                Set.of(),
                null,
                null,
                "",
                0L,
                SlotWorkspaceViewModel.ActiveChestPanel.empty(),
                List.of(source));

        SlotWorkspaceViewModel.AtlasItem item = viewModel.triageItems().stream()
                .filter(candidate -> "sns:straw_basket".equals(candidate.identity().itemId()))
                .findFirst()
                .orElseThrow();
        List<String> tooltip = WorkspaceItemTooltipBuilder.slotLines(item).stream()
                .map(Component::getString)
                .toList();

        assertEquals(basket, item.identity().toIdentity());
        assertTrue(item.ghost());
        assertTrue(item.kitNeeded());
        assertEquals(1, item.proximateCount());
        assertFalse(item.presence().isEmpty());
        assertFalse(tooltip.contains("Need to craft/find: 1"));
    }

    @Test
    void craftRunInputCardsRenderWantedCountsNotTabDesiredCounts() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        ItemIdentity coal = ItemIdentity.of("minecraft:coal");
        runtime.craftRunWorkflow().add(craftRunCapture("slot:recipe/torch", "minecraft:torch", coal, 3));

        SlotWorkspaceViewModel.setGhostStackResolver(itemId -> new ItemStack(itemId, 1, 64));
        try {
            SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                    InventoryAuthoritySnapshot.empty(),
                    runtime.snapshot(),
                    "ready",
                    "",
                    0,
                    0,
                    1L);

            SlotWorkspaceViewModel.AtlasItem coalCard = viewModel.atlasItems().stream()
                    .filter(item -> "minecraft:coal".equals(item.identity().itemId()))
                    .findFirst()
                    .orElseThrow();

            assertEquals(0, coalCard.desiredCount());
            assertFalse(coalCard.desiredCountFromKit());
            assertEquals(3, coalCard.wantedCount());
        } finally {
            SlotWorkspaceViewModel.setGhostStackResolver(null);
        }
    }

    @Test
    void activeWorkflowTabKeepsUnrelatedNearbyGhostsForXrayReveal() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        KitDefinition mining = runtime.kitWorkflow().create("Mining");
        runtime.kitWorkflow().setMember(mining.id(), ItemIdentity.of("minecraft:torch"), true);
        runtime.kitWorkflow().activate(mining.id());
        VisualAtlasIsland blocks = runtime.visualAtlasWorkflow().createIsland(
                "Blocks",
                0,
                0,
                0xFF6B8E23,
                null);
        runtime.visualAtlasWorkflow().assignHome(ItemIdentity.of("minecraft:dirt"), blocks.id(), 0);

        WorldDisplayStorageSource source = new WorldDisplayStorageSource(
                null,
                WorldDisplayStorageKind.TOOL_RACK,
                "Tool rack @ 1,64,0",
                "minecraft:overworld",
                1,
                64,
                0,
                4,
                List.of(new WorldStorageAccess.SlotContent(0, new ItemStack("minecraft:dirt", 12, 64))));

        SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                InventoryAuthoritySnapshot.empty(),
                runtime.snapshot(),
                "ready",
                "",
                0,
                0,
                1L,
                null,
                null,
                storageId -> SlotWorkspaceViewModel.ChestContentsSnapshot.empty(),
                Set.of(),
                null,
                null,
                "",
                0L,
                SlotWorkspaceViewModel.ActiveChestPanel.empty(),
                List.of(source));

        SlotWorkspaceViewModel.AtlasItem dirt = viewModel.atlasItems().stream()
                .filter(item -> "minecraft:dirt".equals(item.identity().itemId()))
                .findFirst()
                .orElseThrow();

        assertTrue(dirt.ghost());
        assertEquals(12, dirt.proximateCount());
        assertEquals(SlotWorkspaceViewModel.PutAwayState.NONE, dirt.putAwayState());
    }

    private static InventoryAuthoritySnapshot carried(InventoryStackSnapshot... stacks) {
        return carriedBySource(Map.of(BuiltinInventoryIds.PLAYER_MAIN, List.of(stacks)));
    }

    private static CraftRunRecipeCapture craftRunCapture(
            String recipeId,
            String outputItemId,
            ItemIdentity input,
            int remainingOutputCount
    ) {
        ItemIdentity output = ItemIdentity.of(outputItemId);
        return new CraftRunRecipeCapture(
                "emi:" + recipeId,
                recipeId,
                outputItemId,
                output,
                outputItemId,
                1,
                remainingOutputCount,
                List.of(new CraftRunIngredientGroup(
                        recipeId + "/input",
                        input.itemId(),
                        1,
                        List.of(new CraftRunAlternative(input, input.itemId())),
                        List.of())),
                List.of());
    }

    private static InventoryAuthoritySnapshot carriedBySource(
            Map<String, List<InventoryStackSnapshot>> snapshotsBySource
    ) {
        return InventoryAuthorityFixtures.authority(
                host(),
                snapshotsBySource,
                Map.of());
    }

    private static SlotWorkspaceViewModel.ContextualSuggestionLane fetchLane(SlotWorkspaceViewModel viewModel) {
        return viewModel.contextualSuggestionLanes().stream()
                .filter(lane -> SlotWorkspaceViewModel.ContextualSuggestionLane.FETCH.equals(lane.id()))
                .findFirst()
                .orElse(null);
    }

    private static InventoryHostDescriptor host() {
        TestMenu menu = new TestMenu();
        return new InventoryHostDescriptor(
                new HostInstanceKey(TestMenu.class.getName(), 0, "slot.workspace.workflow-tabs.test", ""),
                InventoryHostDescriptor.serverMenuRef(menu),
                "slot.workspace.workflow-tabs.test",
                Component.literal("Workspace Workflow Tabs Test"),
                menu,
                InventoryTopologyDescriptor.empty(),
                InventoryHostSession.empty(),
                List.of(),
                PlayerRuntimeStateDescriptor.vanilla(0),
                BuiltinInventoryDescriptors.builtInPlayerSources(InventoryTopologyDescriptor.empty()),
                BuiltinInventoryDescriptors.builtInQuickAccessLanes(),
                BuiltinInventoryDescriptors.builtInEquipmentGroups(),
                List.of(),
                InventoryHostObservationHints.defaults(),
                "");
    }

    private static final class TestMenu extends AbstractContainerMenu {
        private TestMenu() {
            super(null, 0);
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    }
}
