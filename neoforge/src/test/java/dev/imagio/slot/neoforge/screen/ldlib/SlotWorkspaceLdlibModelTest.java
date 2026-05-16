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
import dev.imagio.slot.inventory.workspace.SlotWorkspaceCommandService;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceTransferRequestFactory;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.inventory.workspace.WorkspaceTransferFeedback;
import dev.imagio.slot.neoforge.triage.IslandSignalExtractor;
import dev.imagio.slot.testsupport.InventoryAuthorityFixtures;
import dev.imagio.slot.workflow.domain.ChestAnchor;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
                0xCC5A4A6E,
                ItemIdentity.of("minecraft:apple")
        );
        runtime.visualAtlasWorkflow().assignHome(ItemIdentity.of("minecraft:apple"), machines.id(), 0);

        SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                authority,
                runtime.snapshot(),
                "ready",
                "",
                0,
                1,
                9
        );

        assertFalse(viewModel.islands().stream().anyMatch(island -> island.islandId().equals(SlotWorkspaceAtlasLayout.ISLAND_TRIAGE)),
                "Triage is now a docked panel, not an atlas island");
        assertTrue(viewModel.islands().stream().anyMatch(island -> island.islandId().equals(machines.id())));

        assertEquals(1, viewModel.atlasItems().size(), "only the homed apple lives on the atlas");
        assertEquals(2, viewModel.triageItems().size(), "stone and torch are unhomed and go to triage");
        SlotWorkspaceViewModel.AtlasItem apple = viewModel.atlasItems().stream()
                .filter(item -> item.identity().itemId().equals("minecraft:apple"))
                .findFirst()
                .orElseThrow();
        SlotWorkspaceViewModel.AtlasItem stone = viewModel.triageItems().stream()
                .filter(item -> item.identity().itemId().equals("minecraft:stone"))
                .findFirst()
                .orElseThrow();
        SlotWorkspaceViewModel.AtlasItem torch = viewModel.triageItems().stream()
                .filter(item -> item.identity().itemId().equals("minecraft:torch"))
                .findFirst()
                .orElseThrow();
        assertEquals(machines.id(), apple.islandId());
        assertTrue(apple.playerPlaced());
        assertTrue(apple.carried());
        assertEquals(3, apple.totalCount());
        // Position/size moved to client-side AtlasLayout as of ADR 0005;
        // wire format no longer carries them on AtlasItem.
        assertEquals(SlotWorkspaceAtlasLayout.ISLAND_TRIAGE, stone.islandId());
        assertTrue(stone.carried());
        assertEquals(17, stone.totalCount());
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
                first.color(),
                first.iconIdentity()
        );
        SlotWorkspaceAtlasLayout.PlayerIslandDraft second = SlotWorkspaceAtlasLayout.createNextPlayerIslandDraft(
                "Storage",
                ItemIdentity.of("minecraft:chest"),
                runtime.snapshot().visualHomeMap()
        );

        assertEquals("Machines", first.label());
        assertTrue(second.x() > first.x() || second.y() > first.y(),
                "second draft should land at a distinct origin from the first");
    }

    @Test
    void movedIslandReprojectsStoredOrdinalHomes() {
        WorkflowDomainRuntime runtime = runtime();
        VisualAtlasIsland island = runtime.visualAtlasWorkflow().createIsland(
                "Machines",
                744,
                104,
                0xCC5A4A6E,
                ItemIdentity.of("minecraft:redstone")
        );
        runtime.visualAtlasWorkflow().assignHome(ItemIdentity.of("minecraft:redstone"), island.id(), 0);
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
        // The atlas item's island binding is preserved; per ADR 0005 its
        // exact world coordinates are computed client-side by AtlasLayout
        // and no longer carried on AtlasItem.
        assertEquals(island.id(), item.islandId());
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
        assertEquals(original.triageItems().size(), restored.triageItems().size());
        assertEquals(original.triageItems().getFirst().identity().itemId(), restored.triageItems().getFirst().identity().itemId());
        assertEquals(original.islands().size(), restored.islands().size());
        assertEquals(original.hotbarSlots().getFirst().displayStack().itemId(), restored.hotbarSlots().getFirst().displayStack().itemId());
        assertTrue(restored.hotbarSlots().getFirst().selected());
        assertFalse(restored.contextualSuggestionLanes().isEmpty());
        assertEquals(
                original.contextualSuggestionLanes().getFirst().placeholderText(),
                restored.contextualSuggestionLanes().getFirst().placeholderText());
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

        SlotWorkspaceViewModel.AtlasItem copper = viewModel.triageItems().stream()
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
    void proximateClaimedChestGhostWithoutHomeIsQueuedForAutoHome() {
        WorkflowDomainRuntime runtime = runtime();
        ItemIdentity identity = ItemIdentity.of("minecraft:cobblestone");
        var claimed = runtime.chestClaimWorkflow().claim(
                Set.of(new ChestAnchor("minecraft:overworld", 10, 64, 10)),
                0,
                0,
                "Mine"
        );
        String storageId = claimed.storageId().toString();

        SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                InventoryAuthoritySnapshot.empty(),
                runtime.snapshot(),
                "ready",
                "",
                0,
                0,
                1,
                new LearnedIslandRuleStore(),
                null,
                id -> storageId.equals(id)
                        ? new SlotWorkspaceViewModel.ChestContentsSnapshot(
                        27,
                        List.of(new ItemStack("minecraft:cobblestone", 32, 64)),
                        List.of(0))
                        : SlotWorkspaceViewModel.ChestContentsSnapshot.empty(),
                Set.of(storageId)
        );

        assertTrue(viewModel.atlasItems().isEmpty(), "unhomed chest ghosts are not rendered before auto-home");
        SlotWorkspaceViewModel.AtlasItem queued = viewModel.triageItems().stream()
                .filter(item -> item.identity().toIdentity().equals(identity))
                .findFirst()
                .orElseThrow();
        assertTrue(queued.ghost());
        assertFalse(queued.carried());
        assertEquals(32, queued.proximateCount());

        assertTrue(SlotWorkspaceCommandService.autoHomeTriageItems(runtime, viewModel, new java.util.HashSet<>()));
        assertNotNull(runtime.visualAtlasWorkflow().visualHomeMap().assignment(identity));

        SlotWorkspaceViewModel afterAutoHome = SlotWorkspaceViewModel.project(
                InventoryAuthoritySnapshot.empty(),
                runtime.snapshot(),
                "ready",
                "",
                0,
                0,
                2,
                new LearnedIslandRuleStore(),
                null,
                id -> storageId.equals(id)
                        ? new SlotWorkspaceViewModel.ChestContentsSnapshot(
                        27,
                        List.of(new ItemStack("minecraft:cobblestone", 32, 64)),
                        List.of(0))
                        : SlotWorkspaceViewModel.ChestContentsSnapshot.empty(),
                Set.of(storageId)
        );

        SlotWorkspaceViewModel.AtlasItem ghost = afterAutoHome.atlasItems().stream()
                .filter(item -> item.identity().toIdentity().equals(identity))
                .findFirst()
                .orElseThrow();
        assertTrue(ghost.ghost());
        assertEquals(32, ghost.proximateCount());
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

        assertTrue(viewModel.triageItems().stream()
                .filter(item -> item.identity().itemId().equals("minecraft:stone"))
                .findFirst().orElseThrow().carried());
        assertTrue(viewModel.triageItems().stream()
                .filter(item -> item.identity().itemId().equals("minecraft:torch"))
                .findFirst().orElseThrow().carried());
    }

    @Test
    void carriedFreeSlotCountEmptyMainIsFullPlayerCapacity() {
        InventoryHostDescriptor host = host();
        InventoryAuthoritySnapshot authority = authority(host, Map.of());
        WorkflowDomainRuntime runtime = runtime();

        SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                authority, runtime.snapshot(), "ready", "", 0, 0, 1);

        assertEquals(36, viewModel.carriedFreeSlotCount(),
                "empty main (27) + hotbar (9) with no backpacks = 36 free slots");
        assertEquals(36, viewModel.carriedSlotCapacity(),
                "capacity is the full 36 across the two carried sources");
    }

    @Test
    void carriedFreeSlotCountFullMainAndHotbarIsZero() {
        InventoryHostDescriptor host = host();
        java.util.List<InventoryStackSnapshot> mainSlots = new java.util.ArrayList<>();
        for (int i = 0; i < 27; i++) {
            mainSlots.add(new InventoryStackSnapshot(i, new ItemStack("minecraft:stone", 1, 64), 1));
        }
        java.util.List<InventoryStackSnapshot> hotbarSlots = new java.util.ArrayList<>();
        for (int i = 0; i < 9; i++) {
            hotbarSlots.add(new InventoryStackSnapshot(i, new ItemStack("minecraft:stone", 1, 64), 1));
        }
        InventoryAuthoritySnapshot authority = authority(host, Map.of(
                BuiltinInventoryIds.PLAYER_MAIN, mainSlots,
                BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0, hotbarSlots
        ));
        WorkflowDomainRuntime runtime = runtime();

        SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                authority, runtime.snapshot(), "ready", "", 0, 0, 1);

        assertEquals(0, viewModel.carriedFreeSlotCount(),
                "every main + hotbar slot occupied = 0 free slots");
    }

    @Test
    void carriedFreeSlotCountExcludesOccupiedSlotsOnly() {
        InventoryHostDescriptor host = host();
        InventoryAuthoritySnapshot authority = authority(host, Map.of(
                BuiltinInventoryIds.PLAYER_MAIN,
                List.of(
                        new InventoryStackSnapshot(0, new ItemStack("minecraft:stone", 12, 64), 12),
                        new InventoryStackSnapshot(3, new ItemStack("minecraft:apple", 3, 64), 3)
                ),
                BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0,
                List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:torch", 16, 64), 16))
        ));
        WorkflowDomainRuntime runtime = runtime();

        SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                authority, runtime.snapshot(), "ready", "", 0, 0, 1);

        assertEquals(33, viewModel.carriedFreeSlotCount(),
                "3 occupied (2 main + 1 hotbar) = 33 free of 36");
    }

    @Test
    void carriedFreeSlotCountRoundTripsThroughCodec() {
        InventoryHostDescriptor host = host();
        InventoryAuthoritySnapshot authority = authority(host, Map.of(
                BuiltinInventoryIds.PLAYER_MAIN,
                List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:stone", 1, 64), 1))
        ));
        HolderLookup.Provider provider = new HolderLookup.Provider() {
        };

        SlotWorkspaceViewModel original = SlotWorkspaceViewModel.project(
                authority, runtime().snapshot(), "ready", "", 0, 0, 1);
        SlotWorkspaceViewModel restored = SlotWorkspaceViewModelCodec.decode(
                provider, SlotWorkspaceViewModelCodec.encode(original, provider));

        assertEquals(original.carriedFreeSlotCount(), restored.carriedFreeSlotCount());
    }

    @Test
    void carriedContainerFlagSetByResolverAndHomedCarriersKeepIt() {
        InventoryHostDescriptor host = host();
        ItemStack backpackStack = new ItemStack("sophisticatedbackpacks:backpack", 1, 1);
        InventoryAuthoritySnapshot authority = authority(host, Map.of(
                BuiltinInventoryIds.PLAYER_MAIN,
                List.of(
                        new InventoryStackSnapshot(0, backpackStack, 1),
                        new InventoryStackSnapshot(1, new ItemStack("minecraft:stone", 1, 64), 1)
                )
        ));
        WorkflowDomainRuntime runtime = runtime();
        // Use the matcher-derived identity so the assignment lines up with
        // the identity the projection synthesises from the carried stack
        // (backpack identities default to ITEM_ID_AND_COMPONENTS because
        // their components are part of identity; ItemIdentity.of(...) bare
        // would produce ITEM_ID and never match).
        ItemIdentity backpackIdentity = dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(backpackStack);
        VisualAtlasIsland gear = runtime.visualAtlasWorkflow().createIsland(
                "Gear", 500, 200, 0xCC6E5A3C, backpackIdentity);
        runtime.visualAtlasWorkflow().assignHome(backpackIdentity, gear.id(), 0);

        java.util.function.Function<ItemIdentity, SlotWorkspaceViewModel.CarriedContainerInfo> containerResolver = identity ->
                "sophisticatedbackpacks:backpack".equals(identity.itemId())
                        ? new SlotWorkspaceViewModel.CarriedContainerInfo(7, 27)
                        : null;
        SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                authority, runtime.snapshot(), "ready", "", 0, 0, 1,
                null, null, null, null, containerResolver);

        SlotWorkspaceViewModel.AtlasItem backpack = viewModel.atlasItems().stream()
                .filter(item -> "sophisticatedbackpacks:backpack".equals(item.identity().itemId()))
                .findFirst().orElseThrow();
        SlotWorkspaceViewModel.AtlasItem stone = viewModel.triageItems().stream()
                .filter(item -> "minecraft:stone".equals(item.identity().itemId()))
                .findFirst().orElseThrow();
        assertTrue(backpack.isCarriedContainer());
        assertEquals(7, backpack.containerFreeSlotCount());
        assertFalse(stone.isCarriedContainer());
        assertEquals(0, stone.containerFreeSlotCount());
    }

    @Test
    void atlasItemCarriedContainerFlagsRoundTripThroughCodec() {
        InventoryHostDescriptor host = host();
        InventoryAuthoritySnapshot authority = authority(host, Map.of(
                BuiltinInventoryIds.PLAYER_MAIN,
                List.of(new InventoryStackSnapshot(0, new ItemStack("sophisticatedbackpacks:backpack", 1, 1), 1))
        ));
        WorkflowDomainRuntime runtime = runtime();
        HolderLookup.Provider provider = new HolderLookup.Provider() {
        };

        java.util.function.Function<ItemIdentity, SlotWorkspaceViewModel.CarriedContainerInfo> containerResolver = identity ->
                "sophisticatedbackpacks:backpack".equals(identity.itemId())
                        ? new SlotWorkspaceViewModel.CarriedContainerInfo(11, 27)
                        : null;
        SlotWorkspaceViewModel projected = SlotWorkspaceViewModel.project(
                authority, runtime.snapshot(), "ready", "", 0, 0, 1,
                null, null, null, null, containerResolver);
        SlotWorkspaceViewModel restored = SlotWorkspaceViewModelCodec.decode(
                provider, SlotWorkspaceViewModelCodec.encode(projected, provider));

        SlotWorkspaceViewModel.AtlasItem projectedItem = projected.triageItems().stream()
                .filter(item -> "sophisticatedbackpacks:backpack".equals(item.identity().itemId()))
                .findFirst().orElseThrow();
        SlotWorkspaceViewModel.AtlasItem restoredItem = restored.triageItems().stream()
                .filter(item -> "sophisticatedbackpacks:backpack".equals(item.identity().itemId()))
                .findFirst().orElseThrow();
        assertTrue(projectedItem.isCarriedContainer());
        assertEquals(11, projectedItem.containerFreeSlotCount());
        assertEquals(27, projectedItem.containerSlotCapacity());
        assertTrue(restoredItem.isCarriedContainer());
        assertEquals(11, restoredItem.containerFreeSlotCount());
        assertEquals(27, restoredItem.containerSlotCapacity());

        SlotWorkspaceViewModel withoutResolver = SlotWorkspaceViewModel.project(
                authority, runtime.snapshot(), "ready", "", 0, 0, 1);
        SlotWorkspaceViewModel.AtlasItem withoutItem = withoutResolver.triageItems().stream()
                .filter(item -> "sophisticatedbackpacks:backpack".equals(item.identity().itemId()))
                .findFirst().orElseThrow();
        assertFalse(withoutItem.isCarriedContainer(),
                "Sophisticated Backpacks unavailable (no resolver) must not flag the item");
        assertEquals(0, withoutItem.containerFreeSlotCount());
    }

    @Test
    void islandCarriedCountEqualsCarriedHomesInThatIsland() {
        WorkflowDomainRuntime runtime = runtime();
        VisualAtlasIsland materials = runtime.visualAtlasWorkflow().createIsland(
                "Materials", 500, 200, 0xCC6E5A3C, ItemIdentity.of("minecraft:stone"));
        VisualAtlasIsland gems = runtime.visualAtlasWorkflow().createIsland(
                "Gems & Crystals", 900, 200, 0xCC3C5A6E, ItemIdentity.of("minecraft:diamond"));
        runtime.visualAtlasWorkflow().assignHome(ItemIdentity.of("minecraft:stone"), materials.id(), 0);
        runtime.visualAtlasWorkflow().assignHome(ItemIdentity.of("minecraft:cobblestone"), materials.id(), 1);
        runtime.visualAtlasWorkflow().assignHome(ItemIdentity.of("minecraft:diamond"), gems.id(), 0);

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
        // Phase 2.2: append to the destination island. The projection
        // shifts existing ordinals as needed.
        int append = 0;
        for (var assignment : runtime.visualAtlasWorkflow().visualHomeMap().assignments().values()) {
            if (assignment != null && islandId.equals(assignment.islandId())) {
                append++;
            }
        }
        runtime.visualAtlasWorkflow().assignHome(identity, islandId, append);
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
