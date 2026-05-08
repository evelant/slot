package dev.imagio.slot.forge.network;

import dev.imagio.slot.inventory.core.ItemComparisonMode;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.triage.ChipSuggestion;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.inventory.workspace.WayfindingTarget;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Forge120WorkspaceViewModelCodecTest {
    @BeforeAll
    static void bootstrapMinecraft() throws ReflectiveOperationException {
        SharedConstants.tryDetectVersion();
        // Plain JUnit does not have Forge's network event bus bootstrapped, so
        // avoid Bootstrap.bootStrap() and initialise just the vanilla registries.
        java.lang.reflect.Field bootstrapped = Bootstrap.class.getDeclaredField("isBootstrapped");
        bootstrapped.setAccessible(true);
        bootstrapped.setBoolean(null, true);
        BuiltInRegistries.bootStrap();
    }

    @Test
    void kitCardsRoundTripThroughForgeViewModelCodec() {
        SlotWorkspaceViewModel.IdentityRef stone = identity("minecraft:stone");
        ItemIdentity stoneIdentity = ItemIdentity.of("minecraft:stone");
        SlotWorkspaceViewModel.AtlasItem atlasItem = atlasItem(stone, stoneIdentity);
        SlotWorkspaceViewModel.KitSlotState slot = new SlotWorkspaceViewModel.KitSlotState(
                2,
                true,
                false,
                stone,
                new ItemStack(Items.STONE, 1),
                "Stone");
        SlotWorkspaceViewModel.KitPageView activePage = new SlotWorkspaceViewModel.KitPageView(
                0,
                1,
                0,
                List.of(slot));
        SlotWorkspaceViewModel.KitBringItem bring = new SlotWorkspaceViewModel.KitBringItem(
                stone,
                false,
                new ItemStack(Items.STONE, 1),
                "Stone",
                3,
                8);
        SlotWorkspaceViewModel.KitCard kit = new SlotWorkspaceViewModel.KitCard(
                "kit-1",
                "Mining",
                2,
                0,
                true,
                1,
                0,
                7,
                36,
                1,
                0,
                List.of(slot),
                List.of(activePage, new SlotWorkspaceViewModel.KitPageView(1, 0, 0, List.of())),
                List.of(bring));
        SlotWorkspaceViewModel.ChestChip chestChip = new SlotWorkspaceViewModel.ChestChip(
                "storage-1",
                "minecraft:overworld",
                "Main Base",
                2,
                27,
                4,
                true,
                3,
                10,
                64,
                -3,
                "cluster-a",
                List.of(new SlotWorkspaceViewModel.ChestContentSummary(
                        "minecraft:stone",
                        "",
                        "Stone",
                        new ItemStack(Items.STONE, 1),
                        12)));
        SlotWorkspaceViewModel.LootChestPanel lootChestPanel = new SlotWorkspaceViewModel.LootChestPanel(
                1,
                2,
                3,
                "minecraft:overworld",
                "Loot Chest",
                List.of(atlasItem));
        WayfindingTarget wayfindingTarget = new WayfindingTarget(
                "storage-1",
                "minecraft:overworld",
                10,
                64,
                -3,
                Set.of(stoneIdentity),
                5,
                WayfindingTarget.Scope.KIT);
        SlotWorkspaceViewModel.ActiveChestPanel activeChestPanel = new SlotWorkspaceViewModel.ActiveChestPanel(
                "storage-1",
                "Main Base",
                "cluster-a",
                "Main Cluster",
                0xCC5A6E3D,
                10,
                64,
                -3,
                "minecraft:overworld");

        SlotWorkspaceViewModel original = new SlotWorkspaceViewModel(
                42,
                "ready",
                "",
                0,
                0,
                1,
                1,
                7,
                36,
                List.of(),
                List.of(atlasItem),
                List.of(),
                List.of(chestChip),
                List.of(new SlotWorkspaceViewModel.ChestClusterDescriptor("cluster-a", "Main Cluster", 2)),
                SlotWorkspaceViewModel.emptyHotbar(),
                SlotWorkspaceViewModel.OffhandSlot.empty(),
                List.of(kit),
                lootChestPanel,
                List.of(wayfindingTarget),
                Set.of(stone),
                List.of(),
                activeChestPanel);

        SlotWorkspaceViewModel restored = Forge120WorkspaceViewModelCodec.decode(
                Forge120WorkspaceViewModelCodec.encode(original));

        assertEquals(1, restored.kits().size());
        SlotWorkspaceViewModel.KitCard restoredKit = restored.kits().get(0);
        assertEquals("kit-1", restoredKit.kitId());
        assertEquals("Mining", restoredKit.name());
        assertEquals(2, restoredKit.pageCount());
        assertTrue(restoredKit.active());
        assertEquals(7, restoredKit.carriedSlotCount());
        assertEquals(36, restoredKit.carriedSlotCapacity());
        assertEquals(1, restoredKit.slots().size());
        assertEquals(2, restoredKit.pages().size());
        assertEquals(1, restoredKit.bring().size());

        SlotWorkspaceViewModel.KitSlotState restoredSlot = restoredKit.slots().get(0);
        assertEquals(stone, restoredSlot.identity());
        assertEquals(2, restoredSlot.slotIndex());
        assertTrue(restoredSlot.filled());
        assertFalse(restoredSlot.ready());
        assertEquals(Items.STONE, restoredSlot.displayStack().getItem());

        SlotWorkspaceViewModel.KitBringItem restoredBring = restoredKit.bring().get(0);
        assertEquals(stone, restoredBring.identity());
        assertEquals(3, restoredBring.presentCount());
        assertEquals(8, restoredBring.targetCount());

        SlotWorkspaceViewModel.AtlasItem restoredItem = restored.atlasItems().get(0);
        assertEquals(1, restoredItem.chipSuggestions().size());
        assertTrue(restoredItem.isCarriedContainer());
        assertEquals(5, restoredItem.containerFreeSlotCount());
        assertEquals(27, restoredItem.containerSlotCapacity());
        assertEquals("storage-1", restoredItem.presence().get(0).storageId());

        SlotWorkspaceViewModel.ChestChip restoredChip = restored.chestChips().get(0);
        assertTrue(restoredChip.proximate());
        assertEquals("cluster-a", restoredChip.clusterId());
        assertEquals(1, restoredChip.contents().size());
        assertEquals(Items.STONE, restoredChip.contents().get(0).displayStack().getItem());

        assertEquals("Main Cluster", restored.chestClusters().get(0).label());
        assertEquals("Loot Chest", restored.lootChestPanel().label());
        assertEquals(1, restored.lootChestPanel().items().size());
        assertEquals(WayfindingTarget.Scope.KIT, restored.wayfindingTargets().get(0).scope());
        assertTrue(restored.depositableIdentities().contains(stone));
        assertEquals("storage-1", restored.activeChestPanel().storageId());
    }

    private static SlotWorkspaceViewModel.IdentityRef identity(String itemId) {
        return new SlotWorkspaceViewModel.IdentityRef(itemId, ItemComparisonMode.ITEM_ID.name(), "");
    }

    private static SlotWorkspaceViewModel.AtlasItem atlasItem(
            SlotWorkspaceViewModel.IdentityRef stone,
            ItemIdentity stoneIdentity
    ) {
        return new SlotWorkspaceViewModel.AtlasItem(
                stone,
                new ItemStack(Items.STONE, 5),
                "Stone",
                5,
                0,
                "island-raw",
                true,
                true,
                true,
                false,
                12,
                List.of(ChipSuggestion.learned("island-raw", "Raw", 0xCC5A6E3D, stoneIdentity)),
                List.of(new SlotWorkspaceViewModel.ChestPresenceEntry("storage-1", "Main Base", 12)),
                List.of(new SlotWorkspaceViewModel.ChestPresenceEntry("storage-2", "Remote Base", 3)),
                true,
                5,
                27,
                true,
                10,
                true,
                "player.main",
                4,
                5);
    }
}
