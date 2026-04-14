package dev.imagio.slot.client.screen;

import dev.imagio.slot.client.model.ItemIdentity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InlineDesiredCountStateTest {
    @Test
    void commitNormalizesBlankAndSmallValues() {
        InlineDesiredCountState state = new InlineDesiredCountState();
        ItemIdentity identity = ItemIdentity.of("minecraft:stone");

        state.begin("collection_a", identity);
        InlineDesiredCountState.Commit blankCommit = state.commit(" ").orElseThrow();
        assertEquals(1, blankCommit.desiredCount());

        state.begin("collection_a", identity);
        InlineDesiredCountState.Commit smallCommit = state.commit("0").orElseThrow();
        assertEquals(1, smallCommit.desiredCount());
    }

    @Test
    void layoutCycleTracksWhetherTargetWasPlacedThisFrame() {
        InlineDesiredCountState state = new InlineDesiredCountState();
        ItemIdentity identity = ItemIdentity.of("minecraft:stone");

        state.begin("collection_a", identity);
        state.beginFrame();
        assertTrue(state.shouldCancelAfterLayout());

        state.begin("collection_a", identity);
        state.beginFrame();
        state.markLaidOut();
        assertFalse(state.shouldCancelAfterLayout());
    }

    @Test
    void targetMatchingUsesCollectionAndIdentity() {
        InlineDesiredCountState state = new InlineDesiredCountState();
        ItemIdentity identity = ItemIdentity.of("minecraft:stone");

        state.begin("collection_a", identity);
        assertTrue(state.isTarget("collection_a", identity));
        assertFalse(state.isTarget("collection_b", identity));
    }
}
