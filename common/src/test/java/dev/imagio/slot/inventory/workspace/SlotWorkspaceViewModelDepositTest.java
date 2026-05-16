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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlotWorkspaceViewModelDepositTest {
    private static final UUID CHEST_A = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final ItemIdentity REDSTONE = ItemIdentity.of("minecraft:redstone");
    private static final ItemIdentity STEEL_SAW = ItemIdentity.of("tfc:metal/saw/steel");

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
    void trackedWorldDisplayStorageProjectsLikeChestPresence() {
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
                    index.trackedDisplayEntries());

            SlotWorkspaceViewModel.AtlasItem item = viewModel.atlasItems().stream()
                    .filter(candidate -> REDSTONE.equals(candidate.identity().toIdentity()))
                    .findFirst()
                    .orElseThrow();
            SlotWorkspaceViewModel.ChestChip chip = viewModel.chestChip(target.storageId());

            assertTrue(item.ghost());
            assertEquals(0, item.proximateCount());
            assertEquals(target.storageId(), item.elsewhere().get(0).storageId());
            assertEquals("Placed item @ 3,64,0 — overworld", item.elsewhere().get(0).label());
            assertEquals(target.storageId(), chip.storageId());
            assertFalse(chip.proximate());
            assertEquals(1, chip.contents().get(0).count());
            assertEquals("minecraft:redstone", chip.contents().get(0).itemId());
        } finally {
            SlotWorkspaceViewModel.setGhostStackResolver(null);
        }
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

    private static WorkflowDomainSnapshot workflow(
            VisualHomeMap homeMap,
            ClaimedChestMap claimedChestMap,
            ChestAffinityMap affinityMap
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
                Map.of(),
                Map.of(),
                Map.of()
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
        return new ClaimedChestMap(List.of(new ClaimedChest(
                storageId,
                Set.of(new ChestAnchor("minecraft:overworld", 0, 64, 0)),
                0,
                0,
                "")));
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
