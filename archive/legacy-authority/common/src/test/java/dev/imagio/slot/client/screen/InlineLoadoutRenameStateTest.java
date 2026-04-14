package dev.imagio.slot.client.screen;

import dev.imagio.slot.client.collection.HotbarLoadoutDefinition;
import dev.imagio.slot.client.collection.HotbarLoadoutSlot;
import dev.imagio.slot.client.model.ItemIdentity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InlineLoadoutRenameStateTest {
    @Test
    void commitReturnsRenameTargetAndClearsState() {
        InlineLoadoutRenameState state = new InlineLoadoutRenameState();
        HotbarLoadoutDefinition loadout = loadout("alpha", "Original");

        state.begin("collection_a", loadout);
        InlineLoadoutRenameState.Commit commit = state.commit("  Renamed  ").orElseThrow();

        assertEquals("collection_a", commit.collectionId());
        assertEquals("alpha", commit.loadoutId());
        assertEquals("Renamed", commit.newName());
        assertFalse(state.isActive());
    }

    @Test
    void layoutCycleCancelsOnlyWhenNotLaidOut() {
        InlineLoadoutRenameState state = new InlineLoadoutRenameState();
        HotbarLoadoutDefinition loadout = loadout("alpha", "Original");

        state.begin("collection_a", loadout);
        state.beginFrame();
        assertTrue(state.shouldCancelAfterLayout());

        state.begin("collection_a", loadout);
        state.beginFrame();
        state.markLaidOut();
        assertFalse(state.shouldCancelAfterLayout());
    }

    @Test
    void selectedLoadoutValidationUsesCollectionAndLoadoutId() {
        InlineLoadoutRenameState state = new InlineLoadoutRenameState();
        HotbarLoadoutDefinition active = loadout("alpha", "Original");
        HotbarLoadoutDefinition other = loadout("beta", "Other");

        state.begin("collection_a", active);
        assertTrue(state.matchesSelectedLoadout(collectionId -> active));
        assertFalse(state.matchesSelectedLoadout(collectionId -> other));
    }

    private static HotbarLoadoutDefinition loadout(String id, String name) {
        return new HotbarLoadoutDefinition(
                id,
                name,
                List.of(new HotbarLoadoutSlot(0, ItemIdentity.of("minecraft:stone")))
        );
    }
}
