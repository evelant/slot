package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkspaceCursorCommandServiceTest {
    @Test
    void chestPickupInvalidationUsesStorageAndIdentityRecords() {
        ItemIdentity stone = ItemIdentity.of("minecraft:stone");
        String storageId = "00000000-0000-0000-0000-000000000101";

        List<WorkspaceInvalidation> invalidations = WorkspaceCursorCommandService.pickupInvalidations(
                stone,
                new WorkspaceCursorCommandService.CursorOrigin(
                        WorkspaceCursorCommandService.CursorSourceKind.CHEST,
                        storageId,
                        2),
                4);

        assertEquals(1, invalidations.size());
        WorkspaceInvalidation invalidation = invalidations.get(0);
        assertEquals(WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED, invalidation.reason());
        assertFalse(invalidation.requiresFullProjection());
        assertEquals(Set.of(stone), invalidation.identities());
        assertEquals(Set.of(storageId), invalidation.storageIds());
        assertTrue(invalidation.slices().contains(WorkspaceProjectionSlice.CARD));
        assertTrue(invalidation.slices().contains(WorkspaceProjectionSlice.STORAGE));
        assertTrue(invalidation.slices().contains(WorkspaceProjectionSlice.WAYFINDING));
        assertEquals("cursor_pickup_chest", invalidation.diagnostics());
    }

    @Test
    void carriedPickupInvalidationUsesCarriedIdentity() {
        ItemIdentity stone = ItemIdentity.of("minecraft:stone");

        List<WorkspaceInvalidation> invalidations = WorkspaceCursorCommandService.pickupInvalidations(
                stone,
                new WorkspaceCursorCommandService.CursorOrigin(
                        WorkspaceCursorCommandService.CursorSourceKind.CARRY,
                        "PLAYER_MAIN",
                        4),
                8);

        assertEquals(1, invalidations.size());
        WorkspaceInvalidation invalidation = invalidations.get(0);
        assertEquals(WorkspaceInvalidation.Reason.CARRIED_REVISION_CHANGED, invalidation.reason());
        assertFalse(invalidation.requiresFullProjection());
        assertEquals(Set.of(stone), invalidation.identities());
        assertEquals(Set.of(), invalidation.storageIds());
        assertTrue(invalidation.slices().contains(WorkspaceProjectionSlice.CARD));
        assertTrue(invalidation.slices().contains(WorkspaceProjectionSlice.HOTBAR));
        assertEquals("cursor_pickup_carry", invalidation.diagnostics());
    }

    @Test
    void pickupWithoutOriginFailsClosedToFullProjection() {
        List<WorkspaceInvalidation> invalidations = WorkspaceCursorCommandService.pickupInvalidations(
                ItemIdentity.of("minecraft:stone"),
                null,
                1);

        assertEquals(1, invalidations.size());
        WorkspaceInvalidation invalidation = invalidations.get(0);
        assertTrue(invalidation.requiresFullProjection());
        assertEquals("cursor_pickup_missing_origin", invalidation.diagnostics());
    }
}
