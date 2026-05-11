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
import dev.imagio.slot.testsupport.InventoryAuthorityFixtures;
import dev.imagio.slot.workflow.domain.ChestAffinityMap;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlotWorkspaceViewModelDepositTest {
    private static final UUID CHEST_A = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final ItemIdentity REDSTONE = ItemIdentity.of("minecraft:redstone");

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
        return new SlotWorkspaceViewModel.ChestContentsSnapshot(contents.length, List.of(contents));
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
