package dev.imagio.slot.client.screen;

import dev.imagio.slot.client.model.ItemIdentity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NewCollectionPromptStateTest {
    @Test
    void commitReturnsIdentityAndTrimmedName() {
        NewCollectionPromptState state = new NewCollectionPromptState();
        ItemIdentity identity = ItemIdentity.of("minecraft:stone");

        state.begin(identity);
        NewCollectionPromptState.Commit commit = state.commit("  Building Blocks  ").orElseThrow();

        assertEquals(identity, commit.identity());
        assertEquals("Building Blocks", commit.name());
        assertFalse(state.isActive());
    }

    @Test
    void cancelClearsPromptState() {
        NewCollectionPromptState state = new NewCollectionPromptState();
        state.begin(ItemIdentity.of("minecraft:stone"));
        assertTrue(state.isActive());

        state.cancel();
        assertFalse(state.isActive());
    }
}
