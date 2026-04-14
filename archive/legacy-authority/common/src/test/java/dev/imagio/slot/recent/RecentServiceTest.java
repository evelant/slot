package dev.imagio.slot.recent;

import dev.imagio.slot.client.category.SlotCategory;
import dev.imagio.slot.client.model.ItemEntry;
import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.client.model.SlotRef;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecentServiceTest {
    private static final ItemIdentity STONE = ItemIdentity.of("minecraft:stone");
    private static final ItemIdentity DIRT = ItemIdentity.of("minecraft:dirt");
    private static final ItemIdentity BACKPACK = ItemIdentity.of("sophisticatedbackpacks:backpack");

    private final RecentService service = new RecentService();

    @Test
    void doesNotPrefillRecentsFromInitialSnapshot() {
        service.observeVanillaSnapshot(Map.of(STONE, 32), true);

        assertTrue(service.visibleRecentIdentities(List.of(entry(STONE, 32, "player_main")), sourceId -> true).isEmpty());
    }

    @Test
    void ignoresBackpackPositiveDeltaWithoutExplicitObservationWindow() {
        service.observeBackpackSnapshot(Map.of(), true);
        service.observeBackpackSnapshot(Map.of(BACKPACK, 1), true);

        assertFalse(service.hasVisibleEntries(List.of(entry(BACKPACK, 1, "player_backpack")), "player_backpack"::equals));
    }

    @Test
    void recordsBackpackPositiveDeltaFromObservationWindow() {
        service.observeBackpackSnapshot(Map.of(), true);
        service.expectBackpackAcquisition(BACKPACK);
        service.observeBackpackSnapshot(Map.of(BACKPACK, 1), true);

        assertEquals(List.of(BACKPACK), service.visibleRecentIdentities(
                List.of(entry(BACKPACK, 1, "player_backpack")),
                "player_backpack"::equals
        ));
    }

    @Test
    void suppressionBlocksObservedBackpackAcquisition() {
        service.observeBackpackSnapshot(Map.of(), true);
        service.expectBackpackAcquisition(BACKPACK);
        service.suppressPositiveDeltas(4);
        service.observeBackpackSnapshot(Map.of(BACKPACK, 1), true);

        assertFalse(service.hasVisibleEntries(List.of(entry(BACKPACK, 1, "player_backpack")), "player_backpack"::equals));
    }

    @Test
    void outcomeAcquisitionsMarkRecentsImmediately() {
        service.recordOutcomeAcquisitions(AcquisitionProducerId.CRAFT_RESULT.serializedId(), List.of(STONE.itemId(), DIRT.itemId()));

        assertEquals(List.of(DIRT, STONE), service.visibleRecentIdentities(
                List.of(entry(STONE, 1, "player_main"), entry(DIRT, 1, "player_main")),
                sourceId -> true
        ));
    }

    private static ItemEntry entry(ItemIdentity identity, int count, String sourceId) {
        return new ItemEntry(
                identity,
                count,
                Map.of(sourceId, count),
                List.of(new SlotRef(sourceId, 0)),
                SlotCategory.MISC,
                false,
                Set.of()
        );
    }
}
