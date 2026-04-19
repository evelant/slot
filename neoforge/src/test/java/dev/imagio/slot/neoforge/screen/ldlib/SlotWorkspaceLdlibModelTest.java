package dev.imagio.slot.neoforge.screen.ldlib;

import dev.imagio.slot.inventory.action.InventoryActionConflictPolicy;
import dev.imagio.slot.inventory.action.InventoryActionKind;
import dev.imagio.slot.inventory.action.InventoryActionMode;
import dev.imagio.slot.inventory.action.InventoryActionOutcome;
import dev.imagio.slot.inventory.action.InventoryActionQuantity;
import dev.imagio.slot.inventory.action.InventoryActionRequest;
import dev.imagio.slot.inventory.action.InventoryActionScope;
import dev.imagio.slot.inventory.action.InventoryActionStatus;
import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.core.BuiltinInventoryDescriptors;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.HostInstanceKey;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventoryStackSnapshot;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.InventoryTopologyDescriptor;
import dev.imagio.slot.inventory.core.PlayerRuntimeStateDescriptor;
import dev.imagio.slot.inventory.integration.InventoryHostObservationHints;
import dev.imagio.slot.inventory.integration.InventoryHostSession;
import dev.imagio.slot.inventory.query.InventoryAuthorityReadService;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.triage.ChipSuggestion;
import dev.imagio.slot.inventory.triage.IslandSignalDescriptor;
import dev.imagio.slot.inventory.triage.IslandSuggestionTemplate;
import dev.imagio.slot.inventory.triage.LearnedIslandRuleStore;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceAtlasLayout;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceTransferRequestFactory;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.inventory.workspace.WorkspaceTransferFeedback;
import dev.imagio.slot.neoforge.triage.IslandSignalExtractor;
import dev.imagio.slot.testsupport.InventoryAuthorityFixtures;
import dev.imagio.slot.workflow.domain.InMemoryWorkflowDomainStateRepository;
import dev.imagio.slot.workflow.domain.VisualAtlasIsland;
import dev.imagio.slot.workflow.domain.VisualHomeAssignment;
import dev.imagio.slot.workflow.domain.VisualHomeMap;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlotWorkspaceLdlibModelTest {
    @Test
    void mainRowToHotbarBuildsServerDerivedQuickAccessAssignRequest() {
        InventoryHostDescriptor host = host();
        InventoryAuthoritySnapshot authority = authority(host, Map.of(
                BuiltinInventoryIds.PLAYER_MAIN,
                List.of(new InventoryStackSnapshot(4, new ItemStack("minecraft:stone", 32, 64), 32))
        ));

        SlotWorkspaceTransferRequestFactory.BuildResult build = SlotWorkspaceTransferRequestFactory.build(
                host,
                authority,
                new InventoryActionTarget.SourceSlotTarget(BuiltinInventoryIds.PLAYER_MAIN, 4),
                new InventoryActionTarget.QuickAccessTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, 2),
                "test.main_to_hotbar"
        );

        assertTrue(build.dispatchable());
        InventoryActionRequest request = build.request();
        assertEquals(InventoryActionKind.ASSIGN, request.kind());
        assertEquals(InventoryActionMode.EXECUTE, request.mode());
        assertEquals(InventoryActionQuantity.STACK, request.quantity());
        assertEquals(InventoryActionScope.SINGLE_TARGET, request.scope());
        assertEquals(InventoryActionConflictPolicy.ASSIGN_WITH_DISPLACE, request.conflictPolicy());
        assertEquals(new InventoryActionTarget.SourceSlotTarget(BuiltinInventoryIds.PLAYER_MAIN, 4), request.primaryTarget());
        assertEquals(new InventoryActionTarget.QuickAccessTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, 2), request.secondaryTarget());
        assertEquals(32, request.requestedCount());
        assertEquals("minecraft:stone", request.identity().itemId());
        assertEquals("minecraft:stone", request.stack().itemId());
        assertEquals(32, request.stack().getCount());
        assertEquals("test.main_to_hotbar", request.origin());
        assertFalse(request.requestId().isBlank());
    }

    @Test
    void hotbarToMainBuildsSourceDestinationTransferRequest() {
        InventoryHostDescriptor host = host();
        InventoryAuthoritySnapshot authority = authority(host, Map.of(
                BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0,
                List.of(new InventoryStackSnapshot(7, new ItemStack("minecraft:torch", 12, 64), 12))
        ));

        SlotWorkspaceTransferRequestFactory.BuildResult build = SlotWorkspaceTransferRequestFactory.build(
                host,
                authority,
                new InventoryActionTarget.QuickAccessTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, 7),
                new InventoryActionTarget.SourceTarget(BuiltinInventoryIds.PLAYER_MAIN),
                "test.hotbar_to_main"
        );

        assertTrue(build.dispatchable());
        InventoryActionRequest request = build.request();
        assertEquals(InventoryActionKind.TRANSFER, request.kind());
        assertEquals(InventoryActionQuantity.STACK, request.quantity());
        assertEquals(InventoryActionScope.SINGLE_TARGET, request.scope());
        assertEquals(InventoryActionConflictPolicy.INSERT_ONLY, request.conflictPolicy());
        assertEquals(new InventoryActionTarget.QuickAccessTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, 7), request.primaryTarget());
        assertEquals(new InventoryActionTarget.SourceTarget(BuiltinInventoryIds.PLAYER_MAIN), request.secondaryTarget());
        assertEquals(12, request.requestedCount());
        assertEquals("minecraft:torch", request.identity().itemId());
    }

    @Test
    void occupiedHotbarBuildsQuickAccessAssignRequestForReplacement() {
        InventoryHostDescriptor host = host();
        InventoryAuthoritySnapshot authority = authority(host, Map.of(
                BuiltinInventoryIds.PLAYER_MAIN,
                List.of(new InventoryStackSnapshot(1, new ItemStack("minecraft:crossbow", 1, 1), 1)),
                BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0,
                List.of(new InventoryStackSnapshot(2, new ItemStack("toms_storage:inventory_cable", 23, 64), 23))
        ));

        SlotWorkspaceTransferRequestFactory.BuildResult build = SlotWorkspaceTransferRequestFactory.build(
                host,
                authority,
                new InventoryActionTarget.SourceSlotTarget(BuiltinInventoryIds.PLAYER_MAIN, 1),
                new InventoryActionTarget.QuickAccessTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, 2),
                "test.replace_hotbar"
        );

        assertTrue(build.dispatchable());
        InventoryActionRequest request = build.request();
        assertEquals(InventoryActionKind.ASSIGN, request.kind());
        assertEquals(InventoryActionConflictPolicy.ASSIGN_WITH_DISPLACE, request.conflictPolicy());
        assertEquals(new InventoryActionTarget.SourceSlotTarget(BuiltinInventoryIds.PLAYER_MAIN, 1), request.primaryTarget());
        assertEquals(new InventoryActionTarget.QuickAccessTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, 2), request.secondaryTarget());
        assertEquals(1, request.requestedCount());
        assertEquals("minecraft:crossbow", request.identity().itemId());
    }

    @Test
    void occupiedHotbarBuildsQuickAccessAssignRequestFromLiveServerAuthority() {
        TestMenu menu = new TestMenu();
        InventoryHostDescriptor host = host(menu);
        ServerPlayer player = new ServerPlayer();
        player.containerMenu = menu;
        player.getInventory().items.set(11, new ItemStack("minecraft:arrow", 62, 64));
        player.getInventory().items.set(3, new ItemStack("minecraft:oak_log", 1, 64));
        InventoryAuthoritySnapshot authority = InventoryAuthorityReadService.serverAuthority(player, host);

        SlotWorkspaceTransferRequestFactory.BuildResult build = SlotWorkspaceTransferRequestFactory.build(
                host,
                authority,
                new InventoryActionTarget.SourceSlotTarget(BuiltinInventoryIds.PLAYER_MAIN, 2),
                new InventoryActionTarget.QuickAccessTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, 3),
                "test.live_replace_hotbar"
        );

        assertTrue(build.dispatchable());
        assertEquals(InventoryActionKind.ASSIGN, build.request().kind());
        assertEquals(InventoryActionConflictPolicy.ASSIGN_WITH_DISPLACE, build.request().conflictPolicy());
        assertEquals("minecraft:arrow", build.request().identity().itemId());
        assertEquals(62, build.request().requestedCount());
    }

    @Test
    void emptySourceIsRejectedBeforeExecution() {
        InventoryHostDescriptor host = host();
        InventoryAuthoritySnapshot authority = authority(host, Map.of());

        SlotWorkspaceTransferRequestFactory.BuildResult build = SlotWorkspaceTransferRequestFactory.build(
                host,
                authority,
                new InventoryActionTarget.SourceSlotTarget(BuiltinInventoryIds.PLAYER_MAIN, 0),
                new InventoryActionTarget.QuickAccessTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, 0),
                "test.empty_source"
        );

        assertFalse(build.dispatchable());
        assertEquals("empty_source", build.diagnostics());
    }

    @Test
    void missingHostOrAuthorityRejectsTransfer() {
        InventoryHostDescriptor host = host();
        InventoryAuthoritySnapshot authority = authority(host, Map.of(
                BuiltinInventoryIds.PLAYER_MAIN,
                List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:dirt", 1, 64), 1))
        ));

        assertFalse(SlotWorkspaceTransferRequestFactory.build(
                null,
                authority,
                new InventoryActionTarget.SourceSlotTarget(BuiltinInventoryIds.PLAYER_MAIN, 0),
                new InventoryActionTarget.QuickAccessTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, 0),
                "test.missing"
        ).dispatchable());
        assertFalse(SlotWorkspaceTransferRequestFactory.build(
                host,
                null,
                new InventoryActionTarget.SourceSlotTarget(BuiltinInventoryIds.PLAYER_MAIN, 0),
                new InventoryActionTarget.QuickAccessTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, 0),
                "test.missing"
        ).dispatchable());
    }

    @Test
    void viewModelProjectsAtlasHomesCollectionsAndHotbar() {
        InventoryHostDescriptor host = host();
        InventoryAuthoritySnapshot authority = authority(host, Map.of(
                BuiltinInventoryIds.PLAYER_MAIN,
                List.of(
                        new InventoryStackSnapshot(8, new ItemStack("minecraft:stone", 12, 64), 12),
                        new InventoryStackSnapshot(2, new ItemStack("minecraft:apple", 3, 64), 3),
                        new InventoryStackSnapshot(4, new ItemStack("minecraft:stone", 5, 64), 5)
                ),
                BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0,
                List.of(new InventoryStackSnapshot(1, new ItemStack("minecraft:torch", 16, 64), 16))
        ));
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        String buildKit = runtime.collectionWorkflow().createCollection("Build Kit").id();
        runtime.collectionWorkflow().toggleCollectionMembership(ItemIdentity.of("minecraft:stone"), buildKit);
        VisualAtlasIsland machines = runtime.visualAtlasWorkflow().createIsland(
                "Machines",
                744,
                104,
                320,
                196,
                0xCC5A4A6E,
                ItemIdentity.of("minecraft:apple")
        );
        runtime.visualAtlasWorkflow().assignHome(ItemIdentity.of("minecraft:apple"), machines.id(), 16, 60);

        SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                authority,
                runtime.snapshot(),
                "ready",
                "",
                0,
                1,
                9
        );

        assertTrue(viewModel.islands().stream().anyMatch(island -> island.islandId().equals(SlotWorkspaceAtlasLayout.ISLAND_TRIAGE)));
        assertTrue(viewModel.islands().stream().anyMatch(island -> island.islandId().equals(machines.id())));

        assertEquals(3, viewModel.atlasItems().size());
        SlotWorkspaceViewModel.AtlasItem apple = viewModel.atlasItems().stream()
                .filter(item -> item.identity().itemId().equals("minecraft:apple"))
                .findFirst()
                .orElseThrow();
        SlotWorkspaceViewModel.AtlasItem stone = viewModel.atlasItems().stream()
                .filter(item -> item.identity().itemId().equals("minecraft:stone"))
                .findFirst()
                .orElseThrow();
        SlotWorkspaceViewModel.AtlasItem torch = viewModel.atlasItems().stream()
                .filter(item -> item.identity().itemId().equals("minecraft:torch"))
                .findFirst()
                .orElseThrow();
        assertEquals(machines.id(), apple.islandId());
        assertTrue(apple.playerPlaced());
        assertTrue(apple.carried());
        assertEquals(3, apple.totalCount());
        assertEquals(machines.x() + 16, apple.x());
        assertEquals(machines.y() + 60, apple.y());
        assertEquals(SlotWorkspaceAtlasLayout.CARD_WIDTH, apple.width());
        assertEquals(SlotWorkspaceAtlasLayout.CARD_HEIGHT, apple.height());
        assertEquals(SlotWorkspaceAtlasLayout.ISLAND_TRIAGE, stone.islandId());
        assertTrue(stone.carried());
        assertEquals(17, stone.totalCount());
        assertEquals(SlotWorkspaceAtlasLayout.CARD_WIDTH, stone.width());
        assertEquals(SlotWorkspaceAtlasLayout.CARD_HEIGHT, stone.height());
        assertEquals(SlotWorkspaceAtlasLayout.ISLAND_TRIAGE, torch.islandId());
        assertTrue(torch.carried());
        assertEquals(16, torch.totalCount());

        assertEquals(9, viewModel.hotbarSlots().size());
        assertFalse(viewModel.hotbarSlots().get(0).occupied());
        assertTrue(viewModel.hotbarSlots().get(1).occupied());
        assertTrue(viewModel.hotbarSlots().get(1).selected());
        assertEquals("minecraft:torch", viewModel.hotbarSlots().get(1).displayStack().itemId());
        assertEquals(16, viewModel.hotbarSlots().get(1).count());
    }

    @Test
    void playerIslandDraftUsesLayoutCoordinatesWithoutRequiringPersistedId() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);

        SlotWorkspaceAtlasLayout.PlayerIslandDraft first = SlotWorkspaceAtlasLayout.createNextPlayerIslandDraft(
                "Machines",
                ItemIdentity.of("minecraft:redstone"),
                runtime.snapshot().visualHomeMap()
        );
        runtime.visualAtlasWorkflow().createIsland(
                first.label(),
                first.x(),
                first.y(),
                first.width(),
                first.height(),
                first.color(),
                first.iconIdentity()
        );
        SlotWorkspaceAtlasLayout.PlayerIslandDraft second = SlotWorkspaceAtlasLayout.createNextPlayerIslandDraft(
                "Storage",
                ItemIdentity.of("minecraft:chest"),
                runtime.snapshot().visualHomeMap()
        );

        assertEquals("Machines", first.label());
        assertTrue(first.width() >= 96);
        assertTrue(first.height() >= 72);
        assertTrue(second.x() > first.x());
    }

    @Test
    void placementStartsBelowIslandHeaderReserve() {
        List<SlotWorkspaceViewModel.AtlasIsland> islands = SlotWorkspaceAtlasLayout.fittedIslands(
                SlotWorkspaceAtlasLayout.baseIslands(runtime().snapshot().visualHomeMap()),
                List.of()
        );
        SlotWorkspaceViewModel.AtlasIsland triage = islands.stream()
                .filter(island -> island.islandId().equals(SlotWorkspaceAtlasLayout.ISLAND_TRIAGE))
                .findFirst()
                .orElseThrow();

        SlotWorkspaceAtlasLayout.Placement placement = SlotWorkspaceAtlasLayout.placementForOrdinal(
                islands,
                SlotWorkspaceAtlasLayout.ISLAND_TRIAGE,
                0
        );

        assertEquals(SlotWorkspaceAtlasLayout.ISLAND_TRIAGE, placement.islandId());
        assertTrue(placement.localX() >= SlotWorkspaceAtlasLayout.ISLAND_CONTENT_PADDING_X);
        assertTrue(placement.localY() >= SlotWorkspaceAtlasLayout.ISLAND_CONTENT_TOP);
        assertTrue(placement.x() >= triage.x() + SlotWorkspaceAtlasLayout.ISLAND_CONTENT_PADDING_X);
        assertTrue(placement.y() >= triage.y() + SlotWorkspaceAtlasLayout.ISLAND_CONTENT_TOP);
    }

    @Test
    void dropPlacementFloorsToContentMinimumButAllowsGrowthPastEdge() {
        List<SlotWorkspaceViewModel.AtlasIsland> islands = SlotWorkspaceAtlasLayout.fittedIslands(
                SlotWorkspaceAtlasLayout.baseIslands(runtime().snapshot().visualHomeMap()),
                List.of()
        );
        SlotWorkspaceViewModel.AtlasIsland triage = islands.stream()
                .filter(island -> island.islandId().equals(SlotWorkspaceAtlasLayout.ISLAND_TRIAGE))
                .findFirst()
                .orElseThrow();

        SlotWorkspaceAtlasLayout.Placement topLeft = SlotWorkspaceAtlasLayout.placementForDrop(
                islands,
                SlotWorkspaceAtlasLayout.ISLAND_TRIAGE,
                triage.x() - 200,
                triage.y() - 200
        );
        SlotWorkspaceAtlasLayout.Placement beyondRight = SlotWorkspaceAtlasLayout.placementForDrop(
                islands,
                SlotWorkspaceAtlasLayout.ISLAND_TRIAGE,
                triage.x() + triage.width() + 200,
                triage.y() + triage.height() + 200
        );

        assertTrue(topLeft.localX() >= SlotWorkspaceAtlasLayout.ISLAND_CONTENT_PADDING_X);
        assertTrue(topLeft.localY() >= SlotWorkspaceAtlasLayout.ISLAND_CONTENT_TOP);
        assertTrue(topLeft.x() >= triage.x() + SlotWorkspaceAtlasLayout.ISLAND_CONTENT_PADDING_X);
        assertTrue(topLeft.y() >= triage.y() + SlotWorkspaceAtlasLayout.ISLAND_CONTENT_TOP);
        assertTrue(beyondRight.localX() > triage.width(),
                "drop past right edge should preserve requested localX so fitIsland can grow the island");
        assertTrue(beyondRight.localY() > triage.height(),
                "drop past bottom edge should preserve requested localY so fitIsland can grow the island");
    }

    @Test
    void fittedIslandsGrowWhenContentsReachCurrentEdge() {
        List<SlotWorkspaceViewModel.AtlasIsland> base = SlotWorkspaceAtlasLayout.fittedIslands(
                SlotWorkspaceAtlasLayout.baseIslands(VisualHomeMap.empty()),
                List.of()
        );
        SlotWorkspaceViewModel.AtlasIsland triage = base.stream()
                .filter(island -> island.islandId().equals(SlotWorkspaceAtlasLayout.ISLAND_TRIAGE))
                .findFirst()
                .orElseThrow();

        SlotWorkspaceViewModel.AtlasItem edgeItem = new SlotWorkspaceViewModel.AtlasItem(
                SlotWorkspaceViewModel.IdentityRef.from(ItemIdentity.of("minecraft:stone")),
                new ItemStack("minecraft:stone", 1, 64),
                "Stone",
                1,
                0,
                SlotWorkspaceAtlasLayout.ISLAND_TRIAGE,
                triage.x() + triage.width() - SlotWorkspaceAtlasLayout.ISLAND_CONTENT_PADDING_X - SlotWorkspaceAtlasLayout.CARD_WIDTH,
                triage.y() + SlotWorkspaceAtlasLayout.ISLAND_CONTENT_TOP,
                SlotWorkspaceAtlasLayout.CARD_WIDTH,
                SlotWorkspaceAtlasLayout.CARD_HEIGHT,
                false,
                false,
                false,
                List.of()
        );

        List<SlotWorkspaceViewModel.AtlasIsland> fitted = SlotWorkspaceAtlasLayout.fittedIslands(base, List.of(edgeItem));
        SlotWorkspaceViewModel.AtlasIsland grown = fitted.stream()
                .filter(island -> island.islandId().equals(SlotWorkspaceAtlasLayout.ISLAND_TRIAGE))
                .findFirst()
                .orElseThrow();

        assertTrue(grown.width() > triage.width());
        assertEquals(1, grown.itemCount());
    }

    @Test
    void storedLocalHomeCoordinatesProjectBackIntoAtlasSpace() {
        WorkflowDomainRuntime runtime = runtime();
        VisualAtlasIsland island = runtime.visualAtlasWorkflow().createIsland(
                "Machines",
                744,
                104,
                320,
                196,
                0xCC5A4A6E,
                ItemIdentity.of("minecraft:redstone")
        );
        runtime.visualAtlasWorkflow().assignHome(ItemIdentity.of("minecraft:redstone"), island.id(), 24, 60);

        List<SlotWorkspaceViewModel.AtlasIsland> islands = SlotWorkspaceAtlasLayout.baseIslands(runtime.snapshot().visualHomeMap());
        SlotWorkspaceAtlasLayout.Placement placement = SlotWorkspaceAtlasLayout.resolvePlacement(
                islands,
                island.id(),
                runtime.snapshot().visualHomeMap().assignment(ItemIdentity.of("minecraft:redstone")).localX(),
                runtime.snapshot().visualHomeMap().assignment(ItemIdentity.of("minecraft:redstone")).localY()
        );

        assertEquals(island.id(), placement.islandId());
        assertEquals(24, placement.localX());
        assertEquals(60, placement.localY());
        assertEquals(island.x() + 24, placement.x());
        assertEquals(island.y() + 60, placement.y());
    }

    @Test
    void movedIslandReprojectsStoredLocalHomes() {
        WorkflowDomainRuntime runtime = runtime();
        VisualAtlasIsland island = runtime.visualAtlasWorkflow().createIsland(
                "Machines",
                744,
                104,
                320,
                196,
                0xCC5A4A6E,
                ItemIdentity.of("minecraft:redstone")
        );
        runtime.visualAtlasWorkflow().assignHome(ItemIdentity.of("minecraft:redstone"), island.id(), 24, 60);
        runtime.visualAtlasWorkflow().moveIsland(island.id(), 960, 280);

        InventoryAuthoritySnapshot authority = authority(host(), Map.of(
                BuiltinInventoryIds.PLAYER_MAIN,
                List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:redstone", 8, 64), 8))
        ));
        SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                authority,
                runtime.snapshot(),
                "ready",
                "",
                0,
                0,
                1
        );

        SlotWorkspaceViewModel.AtlasItem item = viewModel.atlasItems().stream()
                .filter(candidate -> candidate.identity().itemId().equals("minecraft:redstone"))
                .findFirst()
                .orElseThrow();
        SlotWorkspaceViewModel.AtlasIsland moved = viewModel.island(island.id());

        assertEquals(960, moved.x());
        assertEquals(280, moved.y());
        assertEquals(984, item.x());
        assertEquals(340, item.y());
    }

    @Test
    void viewModelRoundTripsThroughSyncTag() {
        InventoryHostDescriptor host = host();
        InventoryAuthoritySnapshot authority = authority(host, Map.of(
                BuiltinInventoryIds.PLAYER_MAIN,
                List.of(new InventoryStackSnapshot(3, new ItemStack("minecraft:oak_log", 8, 64), 8)),
                BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0,
                List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:stone_pickaxe", 1, 1), 1))
        ));
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        String tracked = runtime.collectionWorkflow().createCollection("Tracked").id();
        runtime.collectionWorkflow().toggleCollectionMembership(ItemIdentity.of("minecraft:oak_log"), tracked);
        HolderLookup.Provider provider = new HolderLookup.Provider() {
        };

        SlotWorkspaceViewModel original = SlotWorkspaceViewModel.project(
                authority,
                runtime.snapshot(),
                "transfer rejected",
                "full_destination",
                1,
                0,
                12
        );
        SlotWorkspaceViewModel restored = SlotWorkspaceViewModelCodec.decode(provider, SlotWorkspaceViewModelCodec.encode(original, provider));

        assertEquals(original.revision(), restored.revision());
        assertEquals(original.status(), restored.status());
        assertEquals(original.diagnostics(), restored.diagnostics());
        assertEquals(original.atlasItems().size(), restored.atlasItems().size());
        assertEquals(original.atlasItems().getFirst().identity().itemId(), restored.atlasItems().getFirst().identity().itemId());
        assertEquals(original.islands().size(), restored.islands().size());
        assertEquals(original.hotbarSlots().getFirst().displayStack().itemId(), restored.hotbarSlots().getFirst().displayStack().itemId());
        assertTrue(restored.hotbarSlots().getFirst().selected());
    }

    @Test
    void fullDestinationRemainderIsReportedAsRejectedNoOp() {
        InventoryHostDescriptor host = host();
        InventoryAuthoritySnapshot authority = authority(host, Map.of(
                BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0,
                List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:torch", 12, 64), 12))
        ));
        InventoryActionRequest request = SlotWorkspaceTransferRequestFactory.build(
                host,
                authority,
                new InventoryActionTarget.QuickAccessTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, 0),
                new InventoryActionTarget.SourceTarget(BuiltinInventoryIds.PLAYER_MAIN),
                "test.full_destination"
        ).request();
        InventoryActionOutcome outcome = new InventoryActionOutcome(
                request.hostId(),
                request.serverMenuRef(),
                request.requestId(),
                request.kind(),
                request.mode(),
                request.quantity(),
                request.scope(),
                request.conflictPolicy(),
                request.origin(),
                request.primaryTarget(),
                request.secondaryTarget(),
                InventoryActionStatus.SUCCESS,
                List.of(),
                request.requestedCount(),
                request.requestedCount(),
                false,
                List.of(),
                request.stack().copy(),
                ""
        );

        WorkspaceTransferFeedback feedback = WorkspaceTransferFeedback.interpret(request, outcome);

        assertEquals("transfer rejected", feedback.status());
        assertEquals("destination_full_or_incompatible", feedback.diagnostics());
    }

    @Test
    void chipAcceptPipelineCreatesIslandHomesIdentityAndRecordsLearnedRule() {
        WorkflowDomainRuntime runtime = runtime();
        LearnedIslandRuleStore store = new LearnedIslandRuleStore();

        ItemIdentity seed = ItemIdentity.of("modded:iron_ingot");
        acceptTemplateChip(runtime, store, IslandSuggestionTemplate.MATERIALS, seed);

        VisualAtlasIsland materialized = runtime.visualAtlasWorkflow().visualHomeMap().playerIslands().stream()
                .filter(island -> IslandSuggestionTemplate.MATERIALS.defaultLabel().equalsIgnoreCase(island.label()))
                .findFirst()
                .orElseThrow();
        VisualHomeAssignment assignment = runtime.visualAtlasWorkflow().visualHomeMap().assignment(seed);
        assertEquals(materialized.id(), assignment.islandId());
        assertEquals(1, store.allRules().stream()
                .filter(rule -> rule.islandId().equals(materialized.id()))
                .count());

        ItemIdentity second = ItemIdentity.of("modded:gold_ingot");
        applyManualAssignment(runtime, store, second, materialized.id());

        InventoryHostDescriptor host = host();
        InventoryAuthoritySnapshot authority = authority(host, Map.of(
                BuiltinInventoryIds.PLAYER_MAIN,
                List.of(new InventoryStackSnapshot(0, new ItemStack("modded:copper_ingot", 1, 64), 1))
        ));

        SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                authority,
                runtime.snapshot(),
                "ready",
                "",
                0,
                0,
                1,
                store,
                IslandSignalExtractor::extract
        );

        SlotWorkspaceViewModel.AtlasItem copper = viewModel.atlasItems().stream()
                .filter(item -> item.identity().itemId().equals("modded:copper_ingot"))
                .findFirst()
                .orElseThrow();
        assertEquals(SlotWorkspaceAtlasLayout.ISLAND_TRIAGE, copper.islandId());
        assertFalse(copper.chipSuggestions().isEmpty());
        ChipSuggestion learnedChip = copper.chipSuggestions().stream()
                .filter(chip -> chip.kind() == ChipSuggestion.ChipKind.LEARNED)
                .findFirst()
                .orElseThrow();
        assertEquals(materialized.id(), learnedChip.islandId());
    }

    @Test
    void manualAssignPipelineProducesIdenticalStateToChipAccept() {
        ItemIdentity first = ItemIdentity.of("modded:iron_ingot");
        ItemIdentity second = ItemIdentity.of("modded:gold_ingot");

        WorkflowDomainRuntime chipRuntime = runtime();
        LearnedIslandRuleStore chipStore = new LearnedIslandRuleStore();
        acceptTemplateChip(chipRuntime, chipStore, IslandSuggestionTemplate.MATERIALS, first);
        String chipIslandId = chipRuntime.visualAtlasWorkflow().visualHomeMap().playerIslands().stream()
                .filter(island -> IslandSuggestionTemplate.MATERIALS.defaultLabel().equalsIgnoreCase(island.label()))
                .findFirst()
                .orElseThrow()
                .id();
        applyManualAssignment(chipRuntime, chipStore, second, chipIslandId);

        WorkflowDomainRuntime manualRuntime = runtime();
        LearnedIslandRuleStore manualStore = new LearnedIslandRuleStore();
        SlotWorkspaceAtlasLayout.PlayerIslandDraft draft = SlotWorkspaceAtlasLayout.createNextPlayerIslandDraft(
                IslandSuggestionTemplate.MATERIALS.defaultLabel(),
                first,
                manualRuntime.visualAtlasWorkflow().visualHomeMap()
        );
        VisualAtlasIsland manualIsland = manualRuntime.visualAtlasWorkflow().createIsland(
                draft.label(),
                draft.x(),
                draft.y(),
                draft.width(),
                draft.height(),
                IslandSuggestionTemplate.MATERIALS.defaultColor(),
                first
        );
        applyManualAssignment(manualRuntime, manualStore, first, manualIsland.id());
        applyManualAssignment(manualRuntime, manualStore, second, manualIsland.id());

        assertEquals(chipIslandId, manualIsland.id());
        assertEquals(chipRuntime.visualAtlasWorkflow().visualHomeMap().playerIslands().size(),
                manualRuntime.visualAtlasWorkflow().visualHomeMap().playerIslands().size());
        assertEquals(chipRuntime.visualAtlasWorkflow().visualHomeMap().assignment(first).islandId(),
                manualRuntime.visualAtlasWorkflow().visualHomeMap().assignment(first).islandId());
        assertEquals(chipRuntime.visualAtlasWorkflow().visualHomeMap().assignment(second).islandId(),
                manualRuntime.visualAtlasWorkflow().visualHomeMap().assignment(second).islandId());
        assertEquals(chipStore.allRules().size(), manualStore.allRules().size());
        assertEquals(chipStore.firingRulesFor(descriptorFor(ItemIdentity.of("modded:copper_ingot"))).size(),
                manualStore.firingRulesFor(descriptorFor(ItemIdentity.of("modded:copper_ingot"))).size());
    }

    @Test
    void carriedFlagDerivesFromAnyCarriedLane() {
        InventoryHostDescriptor host = host();
        InventoryAuthoritySnapshot authority = authority(host, Map.of(
                BuiltinInventoryIds.PLAYER_MAIN,
                List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:stone", 1, 64), 1)),
                BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0,
                List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:torch", 1, 64), 1))
        ));
        WorkflowDomainRuntime runtime = runtime();

        SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                authority, runtime.snapshot(), "ready", "", 0, 0, 1);

        assertTrue(viewModel.atlasItems().stream()
                .filter(item -> item.identity().itemId().equals("minecraft:stone"))
                .findFirst().orElseThrow().carried());
        assertTrue(viewModel.atlasItems().stream()
                .filter(item -> item.identity().itemId().equals("minecraft:torch"))
                .findFirst().orElseThrow().carried());
    }

    @Test
    void ghostItemAppearsForHomedIdentityNotInCarried() {
        WorkflowDomainRuntime runtime = runtime();
        ItemIdentity absent = ItemIdentity.of("minecraft:diamond");
        VisualAtlasIsland gems = runtime.visualAtlasWorkflow().createIsland(
                "Gems", 800, 200, 260, 180, 0xCC5A4A6E, absent);
        runtime.visualAtlasWorkflow().assignHome(absent, gems.id(), 16, 60);

        InventoryAuthoritySnapshot authority = authority(host(), Map.of());

        SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                authority, runtime.snapshot(), "ready", "", 0, 0, 1);

        SlotWorkspaceViewModel.AtlasItem ghostDiamond = viewModel.atlasItems().stream()
                .filter(item -> item.identity().itemId().equals("minecraft:diamond"))
                .findFirst()
                .orElseThrow();
        assertFalse(ghostDiamond.carried(), "homed identity not in any carried lane should be a ghost");
        assertEquals(gems.id(), ghostDiamond.islandId());
    }

    @Test
    void islandCarriedCountEqualsCarriedHomesInThatIsland() {
        WorkflowDomainRuntime runtime = runtime();
        VisualAtlasIsland materials = runtime.visualAtlasWorkflow().createIsland(
                "Materials", 500, 200, 260, 180, 0xCC6E5A3C, ItemIdentity.of("minecraft:stone"));
        VisualAtlasIsland gems = runtime.visualAtlasWorkflow().createIsland(
                "Gems", 900, 200, 260, 180, 0xCC3C5A6E, ItemIdentity.of("minecraft:diamond"));
        runtime.visualAtlasWorkflow().assignHome(ItemIdentity.of("minecraft:stone"), materials.id(), 16, 60);
        runtime.visualAtlasWorkflow().assignHome(ItemIdentity.of("minecraft:cobblestone"), materials.id(), 48, 60);
        runtime.visualAtlasWorkflow().assignHome(ItemIdentity.of("minecraft:diamond"), gems.id(), 16, 60);

        InventoryAuthoritySnapshot authority = authority(host(), Map.of(
                BuiltinInventoryIds.PLAYER_MAIN,
                List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:stone", 32, 64), 32)),
                BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0,
                List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:cobblestone", 12, 64), 12))
        ));

        SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                authority, runtime.snapshot(), "ready", "", 0, 0, 1);

        SlotWorkspaceViewModel.AtlasIsland materialsView = viewModel.island(materials.id());
        SlotWorkspaceViewModel.AtlasIsland gemsView = viewModel.island(gems.id());
        assertEquals(2, materialsView.carriedCount(), "stone + cobblestone carried into materials");
        assertEquals(0, gemsView.carriedCount(), "diamond is homed but not carried");
    }

    private static void acceptTemplateChip(
            WorkflowDomainRuntime runtime,
            LearnedIslandRuleStore store,
            IslandSuggestionTemplate template,
            ItemIdentity identity
    ) {
        SlotWorkspaceAtlasLayout.PlayerIslandDraft draft = SlotWorkspaceAtlasLayout.createNextPlayerIslandDraft(
                template.defaultLabel(),
                identity,
                runtime.visualAtlasWorkflow().visualHomeMap()
        );
        VisualAtlasIsland created = runtime.visualAtlasWorkflow().createIsland(
                draft.label(),
                draft.x(),
                draft.y(),
                draft.width(),
                draft.height(),
                template.defaultColor(),
                identity
        );
        applyManualAssignment(runtime, store, identity, created.id());
    }

    private static void applyManualAssignment(
            WorkflowDomainRuntime runtime,
            LearnedIslandRuleStore store,
            ItemIdentity identity,
            String islandId
    ) {
        SlotWorkspaceAtlasLayout.Placement placement = SlotWorkspaceAtlasLayout.placementForOrdinal(
                SlotWorkspaceAtlasLayout.baseIslands(runtime.visualAtlasWorkflow().visualHomeMap()),
                islandId,
                0
        );
        runtime.visualAtlasWorkflow().assignHome(identity, islandId, placement.localX(), placement.localY());
        store.recordAssignment(descriptorFor(identity), islandId, System.currentTimeMillis());
    }

    private static IslandSignalDescriptor descriptorFor(ItemIdentity identity) {
        IslandSignalDescriptor base = IslandSignalExtractor.extract(null);
        return new IslandSignalDescriptor(
                identity,
                base.classSignals(),
                base.itemTags(),
                namespaceOf(identity.itemId()),
                ""
        );
    }

    private static String namespaceOf(String itemId) {
        if (itemId == null) {
            return "";
        }
        int colon = itemId.indexOf(':');
        return colon <= 0 ? "" : itemId.substring(0, colon);
    }

    private static InventoryAuthoritySnapshot authority(
            InventoryHostDescriptor host,
            Map<String, List<InventoryStackSnapshot>> snapshotsBySource
    ) {
        return InventoryAuthorityFixtures.authority(
                host,
                snapshotsBySource,
                Map.of(
                        BuiltinInventoryIds.PLAYER_MAIN, 27,
                        BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0, 9
                )
        );
    }

    private static InventoryHostDescriptor host() {
        return host(new TestMenu());
    }

    private static WorkflowDomainRuntime runtime() {
        return new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
    }

    private static InventoryHostDescriptor host(TestMenu menu) {
        return new InventoryHostDescriptor(
                new HostInstanceKey(TestMenu.class.getName(), 0, "slot.workspace.test", ""),
                InventoryHostDescriptor.serverMenuRef(menu),
                "slot.workspace.test",
                Component.literal("Workspace Test"),
                menu,
                InventoryTopologyDescriptor.empty(),
                InventoryHostSession.empty(),
                List.of(),
                PlayerRuntimeStateDescriptor.vanilla(0),
                List.of(
                        BuiltinInventoryDescriptors.playerMain(InventoryTopologyDescriptor.empty()),
                        BuiltinInventoryDescriptors.quickAccessLane0Source(InventoryTopologyDescriptor.empty())
                ),
                BuiltinInventoryDescriptors.builtInQuickAccessLanes(),
                BuiltinInventoryDescriptors.builtInEquipmentGroups(),
                List.of(),
                InventoryHostObservationHints.defaults(),
                ""
        );
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
