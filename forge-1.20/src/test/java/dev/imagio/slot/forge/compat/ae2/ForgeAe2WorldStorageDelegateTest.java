package dev.imagio.slot.forge.compat.ae2;

import dev.imagio.slot.inventory.storage.WorldDisplayStorageKind;
import dev.imagio.slot.inventory.storage.WorldDisplayStorageSource;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForgeAe2WorldStorageDelegateTest {
    @Test
    void nearestSourcesByNetworkKeepsOnlyClosestTerminalPerGridIdentity() {
        Object firstNetwork = new Object();
        Object secondNetwork = new Object();
        WorldDisplayStorageSource farFirst = source(6);
        WorldDisplayStorageSource nearFirst = source(2);
        WorldDisplayStorageSource second = source(4);

        List<WorldDisplayStorageSource> selected = ForgeAe2WorldStorageDelegate.nearestSourcesByNetwork(List.of(
                new ForgeAe2WorldStorageDelegate.NetworkDisplayCandidate(firstNetwork, 36, farFirst),
                new ForgeAe2WorldStorageDelegate.NetworkDisplayCandidate(firstNetwork, 4, nearFirst),
                new ForgeAe2WorldStorageDelegate.NetworkDisplayCandidate(secondNetwork, 16, second)));

        assertEquals(2, selected.size());
        assertTrue(selected.contains(nearFirst));
        assertTrue(selected.contains(second));
        assertFalse(selected.contains(farFirst));
    }

    @Test
    void nearestSourcesByNetworkUsesObjectIdentityNotEquality() {
        String firstNetwork = new String("same-grid-id");
        String secondNetwork = new String("same-grid-id");
        WorldDisplayStorageSource first = source(1);
        WorldDisplayStorageSource second = source(2);

        List<WorldDisplayStorageSource> selected = ForgeAe2WorldStorageDelegate.nearestSourcesByNetwork(List.of(
                new ForgeAe2WorldStorageDelegate.NetworkDisplayCandidate(firstNetwork, 1, first),
                new ForgeAe2WorldStorageDelegate.NetworkDisplayCandidate(secondNetwork, 4, second)));

        assertEquals(2, selected.size());
        assertTrue(selected.contains(first));
        assertTrue(selected.contains(second));
    }

    @Test
    void unreadableMediaIdsCanIdentifyNetworkWhenAggregateContentsExist() {
        List<String> mediaIds = Ae2NetworkIdentitySupport.mediaIdsForNetworkIdentity(
                Set.of(),
                Set.of("cell-b", "cell-a"),
                true);

        assertEquals(List.of("cell-a", "cell-b"), mediaIds);
    }

    @Test
    void activeMediaIdsWinOverUnreadableFallbacks() {
        List<String> mediaIds = Ae2NetworkIdentitySupport.mediaIdsForNetworkIdentity(
                Set.of("cell-active"),
                Set.of("cell-unreadable"),
                true);

        assertEquals(List.of("cell-active"), mediaIds);
    }

    @Test
    void unreadableMediaIdsDoNotIdentifyEmptyAggregateNetwork() {
        List<String> mediaIds = Ae2NetworkIdentitySupport.mediaIdsForNetworkIdentity(
                Set.of(),
                Set.of("cell-a"),
                false);

        assertTrue(mediaIds.isEmpty());
    }

    @Test
    void cellSlotMappingUsesDirectSlotsForDrives() {
        List<Ae2CellSlotMapping.CellSlot> slots = Ae2CellSlotMapping.cellSlots(
                10,
                10,
                slot -> slot == 3 || slot == 7);

        assertEquals(List.of(
                new Ae2CellSlotMapping.CellSlot(3, 3),
                new Ae2CellSlotMapping.CellSlot(7, 7)), slots);
    }

    @Test
    void cellSlotMappingFindsCombinedInventoryCellSlotForMeChest() {
        List<Ae2CellSlotMapping.CellSlot> slots = Ae2CellSlotMapping.cellSlots(
                1,
                2,
                slot -> slot == 1);

        assertEquals(List.of(new Ae2CellSlotMapping.CellSlot(1, 0)), slots);
    }

    @Test
    void gridMachineScanIncludesConcreteImplementorsOfRequestedInterface() {
        List<Class<?>> classes = Ae2GridMachineScan.assignableMachineClasses(
                List.of(FakeDriveMachine.class, UnrelatedMachine.class, FakeCellDockMachine.class),
                FakeChestOrDrive.class);

        assertEquals(List.of(FakeDriveMachine.class, FakeCellDockMachine.class), classes);
    }

    private static WorldDisplayStorageSource source(int x) {
        return new WorldDisplayStorageSource(
                null,
                WorldDisplayStorageKind.AE2_TERMINAL,
                "ME network @ " + x + ",64,0",
                "minecraft:overworld",
                x,
                64,
                0,
                0,
                List.of());
    }

    private interface FakeChestOrDrive {
    }

    private static final class FakeDriveMachine implements FakeChestOrDrive {
    }

    private static final class FakeCellDockMachine implements FakeChestOrDrive {
    }

    private static final class UnrelatedMachine {
    }
}
