package dev.imagio.slot.inventory.core;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemIdentityCollectionsTest {
    private static final ItemIdentity HAMMER = ItemIdentity.of("gtceu:steel_mining_hammer");
    private static final ItemIdentity DAMAGED_HAMMER =
            ItemIdentity.exact("gtceu:steel_mining_hammer", "{Damage:512}");
    private static final ItemIdentity MODDED_HAMMER =
            ItemIdentity.exact("gtceu:steel_mining_hammer", "{Damage:12,\"GT.Tool\":{MaxDamage:960}}");
    private static final ItemIdentity CONFIG_A =
            ItemIdentity.exact("mod:configurable_gadget", "{Mode:\"wide\"}");
    private static final ItemIdentity CONFIG_B =
            ItemIdentity.exact("mod:configurable_gadget", "{Mode:\"tall\"}");

    @Test
    void containsUsesMovableMatchingWithoutFlatteningRealComponents() {
        assertTrue(ItemIdentityCollections.contains(Set.of(DAMAGED_HAMMER), HAMMER));
        assertTrue(ItemIdentityCollections.contains(Set.of(HAMMER), MODDED_HAMMER));
        assertFalse(ItemIdentityCollections.contains(Set.of(CONFIG_A), CONFIG_B));
    }

    @Test
    void countReturnsBestMovableMatch() {
        LinkedHashMap<ItemIdentity, Integer> counts = new LinkedHashMap<>();
        counts.put(DAMAGED_HAMMER, 1);
        counts.put(MODDED_HAMMER, 3);
        counts.put(CONFIG_A, 9);

        assertEquals(3, ItemIdentityCollections.count(counts, HAMMER));
        assertEquals(9, ItemIdentityCollections.count(counts, CONFIG_A));
        assertEquals(0, ItemIdentityCollections.count(counts, CONFIG_B));
    }

    @Test
    void putOrClearRemovesOldMovableKeysAndStoresCanonicalKey() {
        LinkedHashMap<ItemIdentity, Integer> counts = new LinkedHashMap<>();
        counts.put(DAMAGED_HAMMER, 1);
        counts.put(CONFIG_A, 2);

        ItemIdentityCollections.putOrClear(counts, MODDED_HAMMER, 4);

        assertEquals(Map.of(HAMMER, 4, CONFIG_A, 2), counts);

        ItemIdentityCollections.putOrClear(counts, DAMAGED_HAMMER, 0);

        assertFalse(counts.containsKey(HAMMER));
        assertEquals(2, counts.get(CONFIG_A));
    }

    @Test
    void removeMatchingRemovesAllMovableDuplicates() {
        LinkedHashSet<ItemIdentity> identities = new LinkedHashSet<>();
        identities.add(DAMAGED_HAMMER);
        identities.add(MODDED_HAMMER);
        identities.add(CONFIG_A);

        assertTrue(ItemIdentityCollections.removeMatching(identities, HAMMER));

        assertEquals(Set.of(CONFIG_A), identities);
    }

    @Test
    void putIfAbsentDoesNotCreateMovableDuplicates() {
        LinkedHashMap<ItemIdentity, String> entries = new LinkedHashMap<>();
        entries.put(DAMAGED_HAMMER, "old");

        assertEquals("old", ItemIdentityCollections.putIfAbsent(entries, MODDED_HAMMER, "new"));
        assertEquals(1, entries.size());
        assertEquals("old", ItemIdentityCollections.find(entries, HAMMER));

        assertNull(ItemIdentityCollections.putIfAbsent(entries, CONFIG_A, "config"));
        assertEquals("config", entries.get(CONFIG_A));
    }

    @Test
    void normalizedSetStoresCanonicalMovableKeys() {
        Set<ItemIdentity> identities = ItemIdentityCollections.normalizedSet(Set.of(DAMAGED_HAMMER, MODDED_HAMMER));

        assertEquals(Set.of(HAMMER), identities);
    }

    @Test
    void findReturnsExistingMovableEntry() {
        LinkedHashMap<ItemIdentity, Object> entries = new LinkedHashMap<>();
        Object value = new Object();
        entries.put(DAMAGED_HAMMER, value);

        assertSame(value, ItemIdentityCollections.find(entries, MODDED_HAMMER));
    }
}
