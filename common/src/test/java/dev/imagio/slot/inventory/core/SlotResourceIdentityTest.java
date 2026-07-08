package dev.imagio.slot.inventory.core;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlotResourceIdentityTest {
    @Test
    void itemResourceRoundTripsThroughItemIdentity() {
        ItemIdentity exact = ItemIdentity.exact("patchouli:guide_book", "patchouli:book=tfc:field_guide");

        SlotResourceIdentity resource = SlotResourceIdentity.item(exact);

        assertEquals(SlotResourceKind.ITEM, resource.kind());
        assertEquals("patchouli:guide_book", resource.id());
        assertEquals("patchouli:book=tfc:field_guide", resource.fingerprint());
        assertEquals(exact, resource.toItemIdentity());
        assertEquals("patchouli:guide_book", resource.syntheticItemId());
    }

    @Test
    void fluidResourceKeepsFingerprintOutOfItemIdentity() {
        SlotResourceIdentity water = SlotResourceIdentity.fluid("minecraft:water", "{temperature:300}");
        SlotResourceIdentity coldWater = SlotResourceIdentity.fluid("minecraft:water", "{temperature:280}");

        assertTrue(water.fluid());
        assertFalse(water.item());
        assertNull(water.toItemIdentity());
        assertEquals("fluid|minecraft:water|{temperature:300}", water.stableKey());
        assertNotEquals(water, coldWater);
        assertTrue(SlotResourceIdentity.syntheticFluidItemId(water.syntheticItemId()));
        assertNotEquals(water.syntheticItemId(), coldWater.syntheticItemId());
    }

    @Test
    void resourceAmountsAggregateInLongMillibuckets() {
        SlotResourceIdentity water = SlotResourceIdentity.fluid("minecraft:water");
        LinkedHashMap<SlotResourceIdentity, Long> amounts = new LinkedHashMap<>();

        SlotResourceCollections.mergeAmount(amounts, water, 750L);
        SlotResourceCollections.mergeAmount(amounts, SlotResourceIdentity.fluid("minecraft:water"), 500L);
        SlotResourceCollections.mergeAmount(amounts, water, -1L);

        assertEquals(1250L, SlotResourceCollections.count(amounts, water));
        assertEquals(Map.of(water, 1250L), SlotResourceCollections.normalizeAmounts(amounts));
    }

    @Test
    void fluidFormattingUsesBucketsOnlyForWholeBuckets() {
        SlotResourceIdentity water = SlotResourceIdentity.fluid("minecraft:water");

        assertEquals("999 mB", SlotResourceDisplay.formatAmount(water, 999L));
        assertEquals("1 B", SlotResourceDisplay.formatAmount(water, 1000L));
        assertEquals("2500 mB", SlotResourceDisplay.formatAmount(water, 2500L));
        assertEquals("2", SlotResourceDisplay.formatAmount(SlotResourceIdentity.item("minecraft:stick"), 2L));
    }
}
