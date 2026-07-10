package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.BuiltinInventoryDescriptors;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.HostInstanceKey;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.InventoryStackSnapshot;
import dev.imagio.slot.inventory.core.InventoryTopologyDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.PlayerRuntimeStateDescriptor;
import dev.imagio.slot.inventory.core.SlotResourceIdentity;
import dev.imagio.slot.inventory.integration.InventoryHostObservationHints;
import dev.imagio.slot.inventory.integration.InventoryHostSession;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.storage.WorldDisplayStorageKind;
import dev.imagio.slot.inventory.storage.WorldDisplayStorageSource;
import dev.imagio.slot.inventory.storage.WorldStorageAccess;
import dev.imagio.slot.testsupport.InventoryAuthorityFixtures;
import dev.imagio.slot.workflow.domain.ChestAffinityMap;
import dev.imagio.slot.workflow.domain.ChestAffinity;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.ClaimedChestMap;
import dev.imagio.slot.workflow.domain.KitMap;
import dev.imagio.slot.workflow.domain.ProtectionSnapshotPolicy;
import dev.imagio.slot.workflow.domain.VisualAtlasIsland;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import dev.imagio.slot.workflow.domain.VisualHomeAssignment;
import dev.imagio.slot.workflow.domain.VisualHomeMap;
import dev.imagio.slot.workflow.domain.VisualHomeOrigin;
import dev.imagio.slot.workflow.domain.WorkflowDomainSnapshot;
import dev.imagio.slot.workflow.domain.WorkflowProjection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlotWorkspaceViewModelDepositTest {
    private static final UUID CHEST_A = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final String TEST_BACKPACK_SOURCE = "test:backpack";
    private static final ItemIdentity REDSTONE = ItemIdentity.of("minecraft:redstone");
    private static final ItemIdentity STEEL_SAW = ItemIdentity.of("tfc:metal/saw/steel");
    private static final ItemIdentity IRON_FLASK = ItemIdentity.of("waterflasks:iron_flask");
    private static final ItemIdentity WATER_IRON_FLASK =
            ItemIdentity.exact("waterflasks:iron_flask", "fluid=minecraft:water");

    @Test
    void depositableIdentitiesIncludeExistingProximateContentsWithoutAffinity() {
        SlotWorkspaceViewModel viewModel = project(
                carried("minecraft:redstone", 16),
                workflow(homeMap(REDSTONE), claimedMap(CHEST_A), ChestAffinityMap.empty()),
                storageId -> CHEST_A.toString().equals(storageId)
                        ? snapshotOf(stack("minecraft:redstone", 32))
                        : SlotWorkspaceViewModel.ChestContentsSnapshot.empty(),
                Set.of(CHEST_A.toString())
        );

        assertTrue(viewModel.depositableIdentities().contains(SlotWorkspaceViewModel.IdentityRef.from(REDSTONE)));
    }

    @Test
    void depositableIdentitiesStillIgnoreSimilarityWithoutAffinityOrContents() {
        SlotWorkspaceViewModel viewModel = project(
                carried("minecraft:netherite_ingot", 1),
                workflow(
                        homeMap(ItemIdentity.of("minecraft:netherite_ingot")),
                        claimedMap(CHEST_A),
                        ChestAffinityMap.empty()),
                storageId -> CHEST_A.toString().equals(storageId)
                        ? snapshotOf(stack("minecraft:iron_ingot", 32))
                        : SlotWorkspaceViewModel.ChestContentsSnapshot.empty(),
                Set.of(CHEST_A.toString())
        );

        assertFalse(viewModel.depositableIdentities().contains(
                SlotWorkspaceViewModel.IdentityRef.from(ItemIdentity.of("minecraft:netherite_ingot"))));
    }

    @Test
    void depositableIdentitiesIgnoreSmallStationsEvenWithAffinity() {
        SlotWorkspaceViewModel viewModel = project(
                carried("tfc:hot_metal_part", 1),
                workflow(
                        homeMap(ItemIdentity.of("tfc:hot_metal_part")),
                        claimedMap(CHEST_A),
                        affinity(CHEST_A, ItemIdentity.of("tfc:hot_metal_part"), 1)),
                storageId -> CHEST_A.toString().equals(storageId)
                        ? new SlotWorkspaceViewModel.ChestContentsSnapshot(
                                1,
                                List.of(stack("tfc:hot_metal_part", 1)))
                        : SlotWorkspaceViewModel.ChestContentsSnapshot.empty(),
                Set.of(CHEST_A.toString())
        );

        assertFalse(viewModel.depositableIdentities().contains(
                SlotWorkspaceViewModel.IdentityRef.from(ItemIdentity.of("tfc:hot_metal_part"))));
    }

    @Test
    void depositableIdentitiesUseLivePlannerEligibilityWhenProvided() {
        SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                carried("minecraft:redstone", 16),
                workflow(homeMap(REDSTONE), claimedMap(CHEST_A), affinity(CHEST_A, REDSTONE, 1)),
                "ready",
                "",
                0,
                0,
                1L,
                null,
                null,
                storageId -> CHEST_A.toString().equals(storageId)
                        ? snapshotOf(stack("minecraft:redstone", 32))
                        : SlotWorkspaceViewModel.ChestContentsSnapshot.empty(),
                Set.of(CHEST_A.toString()),
                null,
                null,
                "",
                0L,
                SlotWorkspaceViewModel.ActiveChestPanel.empty(),
                List.of(),
                Set.of(CHEST_A.toString()),
                List.of(),
                List.of(),
                Set.of(CHEST_A.toString()),
                (chest, identity) -> true,
                chest -> false);

        assertFalse(viewModel.depositableIdentities().contains(SlotWorkspaceViewModel.IdentityRef.from(REDSTONE)));
    }

    @Test
    void proximateWorldDisplayStorageCreatesNearbyGhostPresence() {
        WorldDisplayStorageSource source = new WorldDisplayStorageSource(
                null,
                WorldDisplayStorageKind.TOOL_RACK,
                "Tool rack @ 1,64,0",
                "minecraft:overworld",
                1,
                64,
                0,
                4,
                List.of(new WorldStorageAccess.SlotContent(0, stack("minecraft:redstone", 1))));
        SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                InventoryAuthoritySnapshot.empty(),
                workflow(homeMap(REDSTONE), ClaimedChestMap.empty(), ChestAffinityMap.empty()),
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
                .filter(candidate -> REDSTONE.equals(candidate.identity().toIdentity()))
                .findFirst()
                .orElseThrow();

        assertTrue(item.ghost());
        assertEquals(1, item.proximateCount());
        assertEquals(source.storageId(), item.presence().get(0).storageId());
        assertEquals("Tool rack @ 1,64,0", item.presence().get(0).label());
    }

    @Test
    void proximateFluidDisplayStorageCreatesFluidCardWithoutItemDisplayStack() {
        SlotResourceIdentity oxygen = SlotResourceIdentity.fluid("gtceu:oxygen");
        WorldDisplayStorageSource source = new WorldDisplayStorageSource(
                null,
                WorldDisplayStorageKind.FLUID_TANK,
                "Stainless Steel Drum",
                "minecraft:overworld",
                1,
                64,
                0,
                1,
                List.of(new WorldStorageAccess.SlotContent(0, stack("gtceu:stainless_steel_drum", 1))),
                List.of(new WorldStorageAccess.FluidContent(
                        0,
                        WorldStorageAccess.FluidContent.DIRECT_TANK_SLOT,
                        oxygen,
                        16_000L,
                        "Oxygen")),
                List.of());
        WorkspaceStorageIndex index = WorkspaceStorageIndex.forTesting(
                null,
                InventoryAuthoritySnapshot.empty(),
                ClaimedChestMap.empty(),
                null,
                Set.of(source.storageId()),
                List.of(source),
                Map.of());
        SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                InventoryAuthoritySnapshot.empty(),
                workflow(homeMap(REDSTONE), ClaimedChestMap.empty(), ChestAffinityMap.empty()),
                "ready",
                "",
                0,
                0,
                1L,
                null,
                null,
                index.contentsResolver(),
                Set.of(),
                null,
                null,
                "",
                0L,
                SlotWorkspaceViewModel.ActiveChestPanel.empty(),
                index.displaySources(),
                Set.of(source.storageId()),
                index.displaySources(),
                index.projectableTrackedDisplayEntries(),
                index.liveDepositStorageIds(),
                index.liveChestContentPresence(),
                index.liveStorageAffinityEligibility(),
                RemoteStorageDetailIntent.INTENT_ONLY);

        SlotWorkspaceViewModel.AtlasItem fluid = viewModel.atlasItems().stream()
                .filter(candidate -> candidate.resource() != null && oxygen.equals(candidate.resource().toIdentity()))
                .findFirst()
                .orElseThrow();

        assertTrue(fluid.fluidResource());
        assertTrue(fluid.displayStack().isEmpty());
        assertEquals("Oxygen", fluid.name());
        assertEquals(16_000L, fluid.resourceAmount());
        assertEquals(16_000, fluid.proximateCount());
        assertEquals(source.storageId(), fluid.presence().get(0).storageId());
    }

    @Test
    void proximateAe2DisplayStorageProjectsLogicalNetworkCount() {
        WorldDisplayStorageSource source = new WorldDisplayStorageSource(
                null,
                WorldDisplayStorageKind.AE2_TERMINAL,
                "ME network @ 1,64,0",
                "minecraft:overworld",
                1,
                64,
                0,
                1,
                List.of(new WorldStorageAccess.SlotContent(
                        0,
                        stack("minecraft:redstone", 64),
                        10_000)));
        SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                InventoryAuthoritySnapshot.empty(),
                workflow(homeMap(REDSTONE), ClaimedChestMap.empty(), ChestAffinityMap.empty()),
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
                .filter(candidate -> REDSTONE.equals(candidate.identity().toIdentity()))
                .findFirst()
                .orElseThrow();

        assertTrue(item.ghost());
        assertEquals(10_000, item.proximateCount());
        assertEquals(10_000, item.presence().get(0).count());
        assertEquals(64, item.displayStack().getCount());
    }

    @Test
    void proximateAe2DisplayStorageMakesMatchingCarriedIdentityDepositable() {
        WorldDisplayStorageSource source = new WorldDisplayStorageSource(
                null,
                WorldDisplayStorageKind.AE2_TERMINAL,
                "ME network @ 1,64,0",
                "minecraft:overworld",
                1,
                64,
                0,
                1,
                List.of(new WorldStorageAccess.SlotContent(
                        0,
                        stack("minecraft:redstone", 64),
                        10_000)));
        WorkspaceStorageIndex index = WorkspaceStorageIndex.forTesting(
                null,
                carried("minecraft:redstone", 16),
                ClaimedChestMap.empty(),
                null,
                Set.of(),
                List.of(source),
                Map.of());

        SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                carried("minecraft:redstone", 16),
                workflow(homeMap(REDSTONE), ClaimedChestMap.empty(), ChestAffinityMap.empty()),
                "ready",
                "",
                0,
                0,
                1L,
                null,
                null,
                index.contentsResolver(),
                Set.of(),
                null,
                null,
                "",
                0L,
                SlotWorkspaceViewModel.ActiveChestPanel.empty(),
                index.displaySources(),
                Set.of(),
                index.displaySources(),
                index.liveDisplayEntries(),
                index.liveDepositStorageIds(),
                index.liveChestContentPresence(),
                index.liveStorageAffinityEligibility());

        assertTrue(viewModel.depositableIdentities().contains(SlotWorkspaceViewModel.IdentityRef.from(REDSTONE)));
    }

    @Test
    void proximateAe2StorageBusAliasProjectsDeduplicatedNetworkCount() {
        WorldDisplayStorageSource terminal = new WorldDisplayStorageSource(
                null,
                WorldDisplayStorageKind.AE2_TERMINAL,
                "ME network @ 1,64,0",
                "minecraft:overworld",
                1,
                64,
                0,
                1,
                List.of(new WorldStorageAccess.SlotContent(
                        0,
                        stack("minecraft:redstone", 64),
                        132)),
                List.of(new WorldDisplayStorageSource.AliasedBlock("minecraft:overworld", 0, 64, 0)));
        WorkspaceStorageIndex index = WorkspaceStorageIndex.forTesting(
                null,
                InventoryAuthoritySnapshot.empty(),
                claimedMap(CHEST_A),
                new SingleChestWorldStorage(List.of(
                        new WorldStorageAccess.SlotContent(0, stack("minecraft:redstone", 32)))),
                Set.of(CHEST_A.toString()),
                List.of(terminal),
                Map.of());

        SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                InventoryAuthoritySnapshot.empty(),
                workflow(homeMap(REDSTONE), claimedMap(CHEST_A), ChestAffinityMap.empty()),
                "ready",
                "",
                0,
                0,
                1L,
                null,
                null,
                index.contentsResolver(),
                Set.of(CHEST_A.toString()),
                null,
                null,
                "",
                0L,
                SlotWorkspaceViewModel.ActiveChestPanel.empty(),
                index.displaySources(),
                Set.of(CHEST_A.toString()),
                index.displaySources(),
                index.liveTrackedDisplayEntries(),
                index.liveDepositStorageIds(),
                index.liveChestContentPresence(),
                index.liveStorageAffinityEligibility(),
                RemoteStorageDetailIntent.SEARCH);

        SlotWorkspaceViewModel.AtlasItem item = viewModel.atlasItems().stream()
                .filter(candidate -> REDSTONE.equals(candidate.identity().toIdentity()))
                .findFirst()
                .orElseThrow();

        assertTrue(item.ghost());
        assertEquals(132, item.proximateCount());
        assertEquals(2, item.presence().size());
        assertEquals(100, item.presence().get(0).count());
        assertEquals(32, item.presence().get(1).count());
    }

    @Test
    void proximateWorldDisplayToolRackNormalizesMovableToolIdentity() {
        ItemIdentity exactSaw = ItemIdentity.exact("tfc:metal/saw/steel", "damage=7");
        WorldDisplayStorageSource source = new WorldDisplayStorageSource(
                null,
                WorldDisplayStorageKind.TOOL_RACK,
                "Tool rack @ 1,64,0",
                "minecraft:overworld",
                1,
                64,
                0,
                4,
                List.of(new WorldStorageAccess.SlotContent(
                        0,
                        new ItemStack("tfc:metal/saw/steel", "damage=7", 1, 1))));
        SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                InventoryAuthoritySnapshot.empty(),
                workflow(homeMap(STEEL_SAW), ClaimedChestMap.empty(), ChestAffinityMap.empty()),
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
                .filter(candidate -> STEEL_SAW.equals(candidate.identity().toIdentity()))
                .findFirst()
                .orElseThrow();

        assertTrue(item.ghost());
        assertEquals(1, item.proximateCount());
        assertEquals(source.storageId(), item.presence().get(0).storageId());
        assertTrue(viewModel.atlasItems().stream()
                .noneMatch(candidate -> exactSaw.equals(candidate.identity().toIdentity())));
    }

    @Test
    void bulkDisplayDepositRequiresExistingMatchingContent() {
        WorldDisplayStorageSource emptyRack = new WorldDisplayStorageSource(
                null,
                WorldDisplayStorageKind.TOOL_RACK,
                "Tool rack @ 1,64,0",
                "minecraft:overworld",
                1,
                64,
                0,
                4,
                List.of());
        WorldDisplayStorageSource sawRack = new WorldDisplayStorageSource(
                null,
                WorldDisplayStorageKind.TOOL_RACK,
                "Tool rack @ 2,64,0",
                "minecraft:overworld",
                2,
                64,
                0,
                4,
                List.of(new WorldStorageAccess.SlotContent(
                        0,
                        new ItemStack("tfc:metal/saw/steel", "damage=7", 1, 1))));
        WorldDisplayStorageSource emptyNetwork = new WorldDisplayStorageSource(
                null,
                WorldDisplayStorageKind.AE2_TERMINAL,
                "ME network @ 3,64,0",
                "minecraft:overworld",
                3,
                64,
                0,
                1,
                List.of());
        WorldDisplayStorageSource redstoneNetwork = new WorldDisplayStorageSource(
                null,
                WorldDisplayStorageKind.AE2_TERMINAL,
                "ME network @ 4,64,0",
                "minecraft:overworld",
                4,
                64,
                0,
                1,
                List.of(new WorldStorageAccess.SlotContent(
                        0,
                        stack("minecraft:redstone", 64),
                        10_000)));

        assertFalse(WorldDisplayDepositRouting.containsMatchingContent(emptyRack, STEEL_SAW));
        assertTrue(WorldDisplayDepositRouting.containsMatchingContent(sawRack, STEEL_SAW));
        assertFalse(WorldDisplayDepositRouting.containsMatchingContent(emptyNetwork, REDSTONE));
        assertTrue(WorldDisplayDepositRouting.containsMatchingContent(redstoneNetwork, REDSTONE));
    }

    @Test
    void rememberedTrackedWorldDisplayStorageDoesNotProjectAsAvailableStock() {
        SlotWorkspaceViewModel.setGhostStackResolver(id -> new ItemStack(id, 1, 64));
        try {
            WorldStorageAccess.Target.Display target = new WorldStorageAccess.Target.Display(
                    WorldDisplayStorageKind.PLACED_ITEM,
                    "minecraft:overworld",
                    3,
                    64,
                    0);
            RememberedStorageContents remembered = RememberedStorageContents.fromCounts(
                    StorageTargetRef.display(target, "Placed item @ 3,64,0", false, true, false),
                    1,
                    Map.of(REDSTONE, 1),
                    10L,
                    "test");
            WorkspaceStorageIndex index = WorkspaceStorageIndex.forTesting(
                    null,
                    null,
                    ClaimedChestMap.empty(),
                    null,
                    Set.of(),
                    List.of(),
                    Map.of(target.storageId(), remembered));

            SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                    InventoryAuthoritySnapshot.empty(),
                    workflow(homeMap(REDSTONE), ClaimedChestMap.empty(), ChestAffinityMap.empty()),
                    "ready",
                    "",
                    0,
                    0,
                    1L,
                    null,
                    null,
                    index.liveContentsResolver(),
                    Set.of(),
                    null,
                    null,
                    "",
                    0L,
                    SlotWorkspaceViewModel.ActiveChestPanel.empty(),
                    index.displaySources(),
                    Set.of(),
                    index.displaySources(),
                    index.liveTrackedDisplayEntries());

            SlotWorkspaceViewModel.ChestChip chip = viewModel.chestChip(target.storageId());

            assertTrue(viewModel.atlasItems().stream()
                    .noneMatch(candidate -> REDSTONE.equals(candidate.identity().toIdentity())));
            assertNull(chip);
        } finally {
            SlotWorkspaceViewModel.setGhostStackResolver(null);
        }
    }

    @Test
    void rememberedClaimedChestProjectsAsElsewhereSearchGuidanceWithoutDepositAuthority() {
        SlotWorkspaceViewModel.setGhostStackResolver(id -> new ItemStack(id, 1, 64));
        try {
            RememberedStorageContents remembered = RememberedStorageContents.fromCounts(
                    StorageTargetRef.claimed(claimed(CHEST_A), false, true, false),
                    27,
                    Map.of(REDSTONE, 8),
                    10L,
                    "test");
            WorkspaceStorageIndex index = WorkspaceStorageIndex.forTesting(
                    null,
                    InventoryAuthoritySnapshot.empty(),
                    claimedMap(CHEST_A),
                    null,
                    Set.of(),
                    List.of(),
                    Map.of(CHEST_A.toString(), remembered));

            SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                    InventoryAuthoritySnapshot.empty(),
                    workflow(homeMap(REDSTONE), claimedMap(CHEST_A), ChestAffinityMap.empty()),
                    "ready",
                    "",
                    0,
                    0,
                    1L,
                    null,
                    null,
                    index.contentsResolver(),
                    Set.of(),
                    null,
                    null,
                    "redstone",
                    0L,
                    SlotWorkspaceViewModel.ActiveChestPanel.empty(),
                    index.displaySources(),
                    Set.of(),
                    index.displaySources(),
                    index.liveTrackedDisplayEntries(),
                    index.liveDepositStorageIds(),
                    index.liveChestContentPresence(),
                    index.liveStorageAffinityEligibility(),
                    RemoteStorageDetailIntent.SEARCH);

            SlotWorkspaceViewModel.AtlasItem redstone = viewModel.atlasItems().stream()
                    .filter(candidate -> REDSTONE.equals(candidate.identity().toIdentity()))
                    .findFirst()
                    .orElseThrow();

            assertFalse(redstone.carried());
            assertEquals(0, redstone.proximateCount());
            assertEquals(1, redstone.elsewhere().size());
            assertEquals(8, redstone.elsewhere().get(0).count());
            assertFalse(viewModel.depositableIdentities().contains(SlotWorkspaceViewModel.IdentityRef.from(REDSTONE)));
        } finally {
            SlotWorkspaceViewModel.setGhostStackResolver(null);
        }
    }

    @Test
    void rememberedRemoteStorageOnlyProjectsWhenRequestedByIntent() {
        SlotWorkspaceViewModel.setGhostStackResolver(id -> new ItemStack(id, 1, 64));
        try {
            RememberedStorageContents remembered = RememberedStorageContents.fromCounts(
                    StorageTargetRef.claimed(claimed(CHEST_A), false, true, false),
                    27,
                    Map.of(REDSTONE, 8),
                    10L,
                    "test");
            WorkspaceStorageIndex index = WorkspaceStorageIndex.forTesting(
                    null,
                    InventoryAuthoritySnapshot.empty(),
                    claimedMap(CHEST_A),
                    null,
                    Set.of(),
                    List.of(),
                    Map.of(CHEST_A.toString(), remembered));

            SlotWorkspaceViewModel collapsed = SlotWorkspaceViewModel.project(
                    InventoryAuthoritySnapshot.empty(),
                    workflow(homeMap(REDSTONE), claimedMap(CHEST_A), ChestAffinityMap.empty()),
                    "ready",
                    "",
                    0,
                    0,
                    1L,
                    null,
                    null,
                    index.contentsResolver(),
                    Set.of(),
                    null,
                    null,
                    "",
                    0L,
                    SlotWorkspaceViewModel.ActiveChestPanel.empty(),
                    index.displaySources(),
                    Set.of(),
                    index.displaySources(),
                    index.liveTrackedDisplayEntries(),
                    index.liveDepositStorageIds(),
                    index.liveChestContentPresence(),
                    index.liveStorageAffinityEligibility(),
                    RemoteStorageDetailIntent.INTENT_ONLY);

            SlotWorkspaceViewModel expanded = SlotWorkspaceViewModel.project(
                    InventoryAuthoritySnapshot.empty(),
                    workflow(homeMap(REDSTONE), claimedMap(CHEST_A), ChestAffinityMap.empty()),
                    "ready",
                    "",
                    0,
                    0,
                    1L,
                    null,
                    null,
                    index.contentsResolver(),
                    Set.of(),
                    null,
                    null,
                    "",
                    0L,
                    SlotWorkspaceViewModel.ActiveChestPanel.empty(),
                    index.displaySources(),
                    Set.of(),
                    index.displaySources(),
                    index.liveTrackedDisplayEntries(),
                    index.liveDepositStorageIds(),
                    index.liveChestContentPresence(),
                    index.liveStorageAffinityEligibility(),
                    RemoteStorageDetailIntent.TRACKED_XRAY);

            assertTrue(collapsed.atlasItems().stream()
                    .noneMatch(candidate -> REDSTONE.equals(candidate.identity().toIdentity())));

            SlotWorkspaceViewModel.AtlasItem redstone = expanded.atlasItems().stream()
                    .filter(candidate -> REDSTONE.equals(candidate.identity().toIdentity()))
                    .findFirst()
                    .orElseThrow();

            assertTrue(redstone.ghost());
            assertEquals(1, redstone.elsewhere().size());
            assertEquals(8, redstone.elsewhere().get(0).count());
        } finally {
            SlotWorkspaceViewModel.setGhostStackResolver(null);
        }
    }

    @Test
    void rememberedRemoteWantedStorageProjectsInCollapsedIntentMode() {
        SlotWorkspaceViewModel.setGhostStackResolver(id -> new ItemStack(id, 1, 64));
        try {
            RememberedStorageContents remembered = RememberedStorageContents.fromCounts(
                    StorageTargetRef.claimed(claimed(CHEST_A), false, true, false),
                    27,
                    Map.of(REDSTONE, 8),
                    10L,
                    "test");
            WorkspaceStorageIndex index = WorkspaceStorageIndex.forTesting(
                    null,
                    InventoryAuthoritySnapshot.empty(),
                    claimedMap(CHEST_A),
                    null,
                    Set.of(),
                    List.of(),
                    Map.of(CHEST_A.toString(), remembered));

            SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                    InventoryAuthoritySnapshot.empty(),
                    workflow(
                            homeMap(REDSTONE),
                            claimedMap(CHEST_A),
                            ChestAffinityMap.empty(),
                            Map.of(),
                            Map.of(REDSTONE, 1)),
                    "ready",
                    "",
                    0,
                    0,
                    1L,
                    null,
                    null,
                    index.contentsResolver(),
                    Set.of(),
                    null,
                    null,
                    "",
                    0L,
                    SlotWorkspaceViewModel.ActiveChestPanel.empty(),
                    index.displaySources(),
                    Set.of(),
                    index.displaySources(),
                    index.liveTrackedDisplayEntries(),
                    index.liveDepositStorageIds(),
                    index.liveChestContentPresence(),
                    index.liveStorageAffinityEligibility(),
                    RemoteStorageDetailIntent.INTENT_ONLY);

            SlotWorkspaceViewModel.AtlasItem redstone = viewModel.atlasItems().stream()
                    .filter(candidate -> REDSTONE.equals(candidate.identity().toIdentity()))
                    .findFirst()
                    .orElseThrow();

            assertTrue(redstone.ghost());
            assertEquals(1, redstone.elsewhere().size());
            assertEquals(8, redstone.elsewhere().get(0).count());
            assertTrue(viewModel.wayfindingTargets().stream()
                    .anyMatch(target -> target.storageId().equals(CHEST_A.toString())));
        } finally {
            SlotWorkspaceViewModel.setGhostStackResolver(null);
        }
    }

    @Test
    void rememberedAe2NetworkProjectsAsTrackedRemoteStorageWithoutLiveTerminalSource() {
        SlotWorkspaceViewModel.setGhostStackResolver(id -> new ItemStack(id, 1, 64));
        try {
            WorldDisplayStorageSource observedNetwork = ae2Network("ae2:network:test", 10_000);
            RememberedStorageContents remembered = RememberedStorageContents.fromSourceSnapshot(
                    StorageTargetRef.display(observedNetwork, false, true),
                    WorkspaceStorageIndex.snapshotFromDisplay(observedNetwork),
                    observedNetwork,
                    10L,
                    "test");
            WorkspaceStorageIndex index = WorkspaceStorageIndex.forTesting(
                    null,
                    InventoryAuthoritySnapshot.empty(),
                    ClaimedChestMap.empty(),
                    null,
                    Set.of(),
                    List.of(),
                    Map.of(observedNetwork.storageId(), remembered));

            SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                    InventoryAuthoritySnapshot.empty(),
                    workflow(
                            homeMap(REDSTONE),
                            ClaimedChestMap.empty(),
                            ChestAffinityMap.empty(),
                            Map.of(),
                            Map.of(REDSTONE, 1)),
                    "ready",
                    "",
                    0,
                    0,
                    1L,
                    null,
                    null,
                    index.contentsResolver(),
                    Set.of(),
                    null,
                    null,
                    "",
                    0L,
                    SlotWorkspaceViewModel.ActiveChestPanel.empty(),
                    index.displaySources(),
                    Set.of(),
                    index.displaySources(),
                    index.projectableTrackedDisplayEntries(),
                    index.liveDepositStorageIds(),
                    index.liveChestContentPresence(),
                    index.liveStorageAffinityEligibility(),
                    RemoteStorageDetailIntent.INTENT_ONLY);

            SlotWorkspaceViewModel.AtlasItem redstone = viewModel.atlasItems().stream()
                    .filter(candidate -> REDSTONE.equals(candidate.identity().toIdentity()))
                    .findFirst()
                    .orElseThrow();

            assertTrue(redstone.ghost());
            assertEquals(0, redstone.proximateCount());
            assertEquals(1, redstone.elsewhere().size());
            assertEquals(observedNetwork.storageId(), redstone.elsewhere().get(0).storageId());
            assertEquals(10_000, redstone.elsewhere().get(0).count());
            assertTrue(redstone.elsewhere().get(0).label().contains("ME network"));
            assertTrue(viewModel.wayfindingTargets().stream()
                    .anyMatch(target -> target.storageId().equals(observedNetwork.storageId())
                            && target.worldX() == 8
                            && target.worldY() == 64
                            && target.worldZ() == 0));
        } finally {
            SlotWorkspaceViewModel.setGhostStackResolver(null);
        }
    }

    @Test
    void chestContentSummariesNormalizeMovableContainers() {
        SlotWorkspaceViewModel viewModel = project(
                InventoryAuthoritySnapshot.empty(),
                workflow(homeMap(ItemIdentity.of("sns:straw_basket")), claimedMap(CHEST_A), ChestAffinityMap.empty()),
                storageId -> CHEST_A.toString().equals(storageId)
                        ? snapshotOf(
                                new ItemStack(
                                        "sns:straw_basket",
                                        "{Inventory:[{Slot:0b,id:\"minecraft:torch\",Count:8b}]}",
                                        1,
                                        1),
                                new ItemStack(
                                        "sns:straw_basket",
                                        "{Inventory:[{Slot:0b,id:\"minecraft:stick\",Count:3b}]}",
                                        1,
                                        1))
                        : SlotWorkspaceViewModel.ChestContentsSnapshot.empty(),
                Set.of(CHEST_A.toString())
        );

        SlotWorkspaceViewModel.ChestChip chip = viewModel.chestChip(CHEST_A.toString());

        assertEquals(1, chip.contents().size());
        assertEquals("sns:straw_basket", chip.contents().get(0).itemId());
        assertEquals("", chip.contents().get(0).componentFingerprint());
        assertEquals(2, chip.contents().get(0).count());
    }

    @Test
    void broadFlaskHomeKeepsFilledWaterFlasksCarriedAndSearchableAcrossCarriedSources() {
        SlotWorkspaceViewModel viewModel = project(
                carriedWaterFlasksAcrossMainAndBackpack(),
                workflow(homeMap(IRON_FLASK), ClaimedChestMap.empty(), ChestAffinityMap.empty()),
                storageId -> SlotWorkspaceViewModel.ChestContentsSnapshot.empty(),
                Set.of()
        );

        SlotWorkspaceViewModel.AtlasItem waterFlasks = viewModel.atlasItems().stream()
                .filter(candidate -> WATER_IRON_FLASK.equals(candidate.identity().toIdentity()))
                .findFirst()
                .orElseThrow();

        assertEquals("materials", waterFlasks.islandId());
        assertTrue(waterFlasks.carried());
        assertEquals(4, waterFlasks.totalCount());
        assertEquals("Water Iron Flask", waterFlasks.name());
        assertTrue(WorkspaceSearchQuery.matchesItem(
                "water",
                waterFlasks,
                viewModel.island(waterFlasks.islandId())));
        assertTrue(viewModel.triageItems().stream()
                .noneMatch(candidate -> WATER_IRON_FLASK.equals(candidate.identity().toIdentity())));
    }

    @Test
    void worldDisplayStorageIdsRoundTripToTargets() {
        String storageId = WorldDisplayStorageSource.storageId(
                WorldDisplayStorageKind.PLACED_ITEM,
                "minecraft:overworld",
                -3,
                70,
                5);

        WorldStorageAccess.Target.Display target = WorldDisplayStorageSource.targetFromStorageId(storageId)
                .orElseThrow();

        assertEquals(WorldDisplayStorageKind.PLACED_ITEM, target.kind());
        assertEquals("minecraft:overworld", target.dimensionId());
        assertEquals(-3, target.x());
        assertEquals(70, target.y());
        assertEquals(5, target.z());
    }

    private static SlotWorkspaceViewModel project(
            InventoryAuthoritySnapshot authority,
            WorkflowDomainSnapshot workflow,
            java.util.function.Function<String, SlotWorkspaceViewModel.ChestContentsSnapshot> contents,
            Set<String> proximate
    ) {
        return SlotWorkspaceViewModel.project(
                authority,
                workflow,
                "ready",
                "",
                0,
                0,
                1L,
                null,
                null,
                contents,
                proximate
        );
    }

    private static InventoryAuthoritySnapshot carried(String itemId, int count) {
        return InventoryAuthorityFixtures.authority(
                host(),
                Map.of(BuiltinInventoryIds.PLAYER_MAIN,
                        List.of(new InventoryStackSnapshot(0, stack(itemId, count), count))),
                Map.of());
    }

    private static InventoryAuthoritySnapshot carriedWaterFlasksAcrossMainAndBackpack() {
        return InventoryAuthorityFixtures.authority(
                hostWithBackpack(),
                Map.of(
                        BuiltinInventoryIds.PLAYER_MAIN,
                        List.of(
                                new InventoryStackSnapshot(0, waterIronFlask(2000), 1),
                                new InventoryStackSnapshot(1, waterIronFlask(1900), 1)),
                        TEST_BACKPACK_SOURCE,
                        List.of(
                                new InventoryStackSnapshot(0, waterIronFlask(1800), 1),
                                new InventoryStackSnapshot(1, waterIronFlask(1700), 1))),
                Map.of(TEST_BACKPACK_SOURCE, 9));
    }

    private static WorkflowDomainSnapshot workflow(
            VisualHomeMap homeMap,
            ClaimedChestMap claimedChestMap,
            ChestAffinityMap affinityMap
    ) {
        return workflow(homeMap, claimedChestMap, affinityMap, Map.of(), Map.of());
    }

    private static WorkflowDomainSnapshot workflow(
            VisualHomeMap homeMap,
            ClaimedChestMap claimedChestMap,
            ChestAffinityMap affinityMap,
            Map<ItemIdentity, Integer> playerDesiredCounts,
            Map<ItemIdentity, Integer> playerWantedCounts
    ) {
        WorkflowProjection.Snapshot projection = new WorkflowProjection.Snapshot(
                List.of(),
                Map.of(),
                Map.of(),
                Set.of(),
                Set.of(),
                new ProtectionSnapshotPolicy(Set.of(), Set.of(), false),
                Map.of(),
                homeMap,
                claimedChestMap,
                affinityMap,
                Map.of(),
                KitMap.empty(),
                playerDesiredCounts,
                Map.of(),
                playerWantedCounts
        );
        return new WorkflowDomainSnapshot(1L, projection, null, null, null, null, null);
    }

    private static VisualHomeMap homeMap(ItemIdentity identity) {
        VisualAtlasIsland island = new VisualAtlasIsland(
                "materials",
                "Materials",
                VisualAtlasIslandKind.PLAYER,
                0,
                0,
                0,
                identity);
        return new VisualHomeMap(
                List.of(island),
                Map.of(identity, new VisualHomeAssignment(
                        identity,
                        island.id(),
                        0,
                        VisualHomeOrigin.PLAYER_PLACED,
                        false)));
    }

    private static ClaimedChestMap claimedMap(UUID storageId) {
        return new ClaimedChestMap(List.of(claimed(storageId)));
    }

    private static ClaimedChest claimed(UUID storageId) {
        return new ClaimedChest(
                storageId,
                Set.of(new ChestAnchor("minecraft:overworld", 0, 64, 0)),
                0,
                0,
                "");
    }

    private static SlotWorkspaceViewModel.ChestContentsSnapshot snapshotOf(ItemStack... contents) {
        return new SlotWorkspaceViewModel.ChestContentsSnapshot(27, List.of(contents));
    }

    private static ChestAffinityMap affinity(UUID storageId, ItemIdentity identity, int score) {
        return new ChestAffinityMap(Map.of(storageId,
                Map.of(identity, new ChestAffinity(identity, score, 0L))));
    }

    private static ItemStack stack(String itemId, int count) {
        return new ItemStack(itemId, count, 64);
    }

    private static ItemStack waterIronFlask(int amount) {
        return new ItemStack(
                "waterflasks:iron_flask",
                "{Fluid:{FluidName:\"minecraft:water\",Amount:" + amount + "}}",
                1,
                1)
                .setHoverName(Component.literal("Water Iron Flask"));
    }

    private static WorldDisplayStorageSource ae2Network(String storageId, int redstoneCount) {
        return new WorldDisplayStorageSource(
                storageId,
                WorldDisplayStorageKind.AE2_NETWORK,
                "ME network @ 8,64,0",
                "minecraft:overworld",
                8,
                64,
                0,
                1,
                List.of(new WorldStorageAccess.SlotContent(
                        0,
                        stack("minecraft:redstone", 64),
                        redstoneCount)),
                List.of(),
                List.of("cell-a"),
                new WorldStorageAccess.Target.Virtual(
                        "ae2",
                        storageId,
                        "terminal",
                        "minecraft:overworld",
                        8,
                        64,
                        0));
    }

    private static InventoryHostDescriptor host() {
        TestMenu menu = new TestMenu();
        return new InventoryHostDescriptor(
                new HostInstanceKey(TestMenu.class.getName(), 0, "slot.workspace.deposit.test", ""),
                InventoryHostDescriptor.serverMenuRef(menu),
                "slot.workspace.deposit.test",
                Component.literal("Workspace Deposit Test"),
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
                ""
        );
    }

    private static InventoryHostDescriptor hostWithBackpack() {
        InventoryHostDescriptor base = host();
        ArrayList<InventorySourceDescriptor> sources = new ArrayList<>(base.sourceDescriptors());
        sources.add(InventorySourceDescriptor.builder(TEST_BACKPACK_SOURCE)
                .label(Component.literal("Test Backpack"))
                .domain(dev.imagio.slot.inventory.core.InventorySourceDomain.PLAYER_EXTENSION)
                .role(dev.imagio.slot.inventory.core.InventorySourceRole.PROVIDER_DEFINED)
                .logicalSlotCount(9)
                .bindingRoute(dev.imagio.slot.inventory.core.InventoryBindingRoute.PROVIDER)
                .capabilities(Set.of(
                        dev.imagio.slot.inventory.core.InventoryCapability.INSERT,
                        dev.imagio.slot.inventory.core.InventoryCapability.EXTRACT))
                .actionRoute(dev.imagio.slot.inventory.core.InventoryActionRoute.PROVIDER_MUTATION)
                .paneMembership(dev.imagio.slot.inventory.core.InventoryPaneMembership.CARRIED)
                .stableOrder(15)
                .build());
        return new InventoryHostDescriptor(
                base.hostId(),
                base.serverMenuRef(),
                base.screenClassName(),
                base.title(),
                base.menu(),
                base.topology(),
                base.hostSession(),
                base.playerExtensions(),
                base.playerRuntimeState(),
                sources,
                base.quickAccessLanes(),
                base.equipmentGroups(),
                base.toolDescriptors(),
                base.observationHints(),
                base.diagnostics()
        );
    }

    private static final class SingleChestWorldStorage implements WorldStorageAccess {
        private final List<SlotContent> contents;

        private SingleChestWorldStorage(List<SlotContent> contents) {
            this.contents = contents == null ? List.of() : List.copyOf(contents);
        }

        @Override
        public ItemStack insert(MinecraftServer server, Target target, ItemStack stack, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack extract(MinecraftServer server, Target target, int slotIndex, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public List<SlotContent> enumerate(MinecraftServer server, Target target) {
            return target instanceof Target.Chest ? contents : List.of();
        }

        @Override
        public int slotCount(MinecraftServer server, Target target) {
            return target instanceof Target.Chest ? 27 : 0;
        }

        @Override
        public boolean isAccessible(MinecraftServer server, Target target) {
            return target instanceof Target.Chest;
        }

        @Override
        public void registerDelegate(Delegate delegate) {
        }
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
