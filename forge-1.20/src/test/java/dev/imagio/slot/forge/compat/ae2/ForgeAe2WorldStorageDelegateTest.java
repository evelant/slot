package dev.imagio.slot.forge.compat.ae2;

import dev.imagio.slot.inventory.storage.WorldDisplayStorageKind;
import dev.imagio.slot.inventory.storage.WorldDisplayStorageSource;
import org.junit.jupiter.api.Test;

import java.util.List;

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
}
