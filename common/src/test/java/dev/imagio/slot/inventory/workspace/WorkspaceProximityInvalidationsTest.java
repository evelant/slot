package dev.imagio.slot.inventory.workspace;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkspaceProximityInvalidationsTest {
    @Test
    void proximateEnterProducesStorageLocalInvalidation() {
        WorkspaceInvalidation invalidation = WorkspaceProximityInvalidations.storageProximityChange(
                Set.of(),
                Set.of("storage-a"),
                Set.of(),
                Set.of());

        assertStorageLocal(invalidation, "storage-a");
        assertEquals("storage_proximity_changed", invalidation.diagnostics());
    }

    @Test
    void proximateLeaveProducesStorageLocalInvalidation() {
        WorkspaceInvalidation invalidation = WorkspaceProximityInvalidations.storageProximityChange(
                Set.of("storage-a"),
                Set.of(),
                Set.of(),
                Set.of());

        assertStorageLocal(invalidation, "storage-a");
        assertEquals("storage_proximity_changed", invalidation.diagnostics());
    }

    @Test
    void contextualOnlyChangeIsStillBoundedByStorageId() {
        WorkspaceInvalidation invalidation = WorkspaceProximityInvalidations.storageProximityChange(
                Set.of(),
                Set.of(),
                Set.of("storage-a"),
                Set.of("storage-b"));

        assertFalse(invalidation.requiresFullProjection());
        assertEquals(Set.of("storage-a", "storage-b"), invalidation.storageIds());
        assertEquals("storage_contextual_proximity_changed", invalidation.diagnostics());
    }

    @Test
    void unchangedProximityProducesNoInvalidation() {
        assertNull(WorkspaceProximityInvalidations.storageProximityChange(
                Set.of("storage-a"),
                Set.of("storage-a"),
                Set.of("storage-b"),
                Set.of("storage-b")));
    }

    private static void assertStorageLocal(WorkspaceInvalidation invalidation, String storageId) {
        assertFalse(invalidation.requiresFullProjection());
        assertEquals(WorkspaceInvalidation.Reason.PROXIMITY_CHANGED, invalidation.reason());
        assertEquals(Set.of(storageId), invalidation.storageIds());
        assertTrue(invalidation.identities().isEmpty());
        assertTrue(invalidation.slices().contains(WorkspaceProjectionSlice.CARD));
        assertTrue(invalidation.slices().contains(WorkspaceProjectionSlice.STORAGE));
        assertTrue(invalidation.slices().contains(WorkspaceProjectionSlice.WAYFINDING));
        assertTrue(invalidation.slices().contains(WorkspaceProjectionSlice.DEPOSITABILITY));
    }
}
