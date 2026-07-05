package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuickHotbarSwapHistoryTest {
    @Test
    void swapOutcomeInvalidatesBothHotbarIdentities() {
        WorkspaceCommandOutcome outcome = QuickHotbarSwapHistory.acceptedSwapOutcome(
                "quick_hotbar_swap_undone",
                "swap",
                "quick_hotbar_swap_undo",
                new ItemStack("minecraft:torch", 16, 64),
                new ItemStack("minecraft:stone", 32, 64));

        assertEquals("quick_hotbar_swap_undone", outcome.status());
        assertEquals(1, outcome.invalidations().size());
        WorkspaceInvalidation invalidation = outcome.invalidations().get(0);
        assertEquals(WorkspaceInvalidation.Reason.CARRIED_REVISION_CHANGED, invalidation.reason());
        assertFalse(invalidation.requiresFullProjection());
        assertEquals(Set.of(ItemIdentity.of("minecraft:torch"), ItemIdentity.of("minecraft:stone")), invalidation.identities());
        assertTrue(invalidation.slices().contains(WorkspaceProjectionSlice.CARD));
        assertTrue(invalidation.slices().contains(WorkspaceProjectionSlice.HOTBAR));
        assertEquals("quick_hotbar_swap_undo", invalidation.diagnostics());
    }
}
