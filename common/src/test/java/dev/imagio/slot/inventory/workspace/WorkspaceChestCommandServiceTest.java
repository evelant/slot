package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkspaceChestCommandServiceTest {
    @Test
    void shiftClickDepositRespectsDesiredCountReservation() {
        assertEquals(
                2,
                WorkspaceChestCommandService.requestedExplicitDepositCount(
                        5,
                        3,
                        WorkspaceChestCommandService.DepositQuantity.STACK,
                        WorkspaceChestCommandService.DesiredCountPolicy.RESPECT));
    }

    @Test
    void shiftScrollDepositIgnoresDesiredCountReservation() {
        assertEquals(
                1,
                WorkspaceChestCommandService.requestedExplicitDepositCount(
                        5,
                        5,
                        WorkspaceChestCommandService.DepositQuantity.ITEM,
                        WorkspaceChestCommandService.DesiredCountPolicy.IGNORE));
    }

    @Test
    void countedShiftScrollDepositCapsAtRequestedCount() {
        assertEquals(
                3,
                WorkspaceChestCommandService.requestedExplicitDepositCount(
                        5,
                        0,
                        WorkspaceChestCommandService.DepositQuantity.ITEM,
                        WorkspaceChestCommandService.DesiredCountPolicy.IGNORE,
                        3));
    }

    @Test
    void shiftClickDepositStopsWhenDesiredCountConsumesCarry() {
        assertEquals(
                0,
                WorkspaceChestCommandService.requestedExplicitDepositCount(
                        5,
                        5,
                        WorkspaceChestCommandService.DepositQuantity.STACK,
                        WorkspaceChestCommandService.DesiredCountPolicy.RESPECT));
    }

    @Test
    void depositRecordsCreateBoundedChestTransferInvalidation() {
        ItemIdentity stone = ItemIdentity.of("minecraft:stone");
        String storageId = "00000000-0000-0000-0000-000000000101";

        List<WorkspaceInvalidation> invalidations = WorkspaceChestCommandService.depositRecordInvalidations(
                List.of(new DepositExecutor.DepositRecord(storageId, stone, 16)),
                "deposit_records");

        assertEquals(1, invalidations.size());
        WorkspaceInvalidation invalidation = invalidations.get(0);
        assertEquals(WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED, invalidation.reason());
        assertEquals(Set.of(stone), invalidation.identities());
        assertEquals(Set.of(storageId), invalidation.storageIds());
        assertEquals(expectedChestTransferSlices(), invalidation.slices());
        assertEquals("deposit_records", invalidation.diagnostics());
        assertFalse(invalidation.requiresFullProjection());
    }

    @Test
    void takeRecordsWithoutMutationEvidenceRequireFullProjection() {
        List<WorkspaceInvalidation> invalidations = WorkspaceChestCommandService.takeRecordInvalidations(
                List.of(new TakeAllExecutor.TakeRecord("", ItemIdentity.of("minecraft:stone"), 4)),
                "take_records");

        assertEquals(1, invalidations.size());
        WorkspaceInvalidation invalidation = invalidations.get(0);
        assertEquals(WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED, invalidation.reason());
        assertEquals(Set.of(ItemIdentity.of("minecraft:stone")), invalidation.identities());
        assertEquals(Set.of(), invalidation.storageIds());
        assertEquals(WorkspaceProjectionSlice.all(), invalidation.slices());
        assertEquals("take_records_missing_chest_transfer_records", invalidation.diagnostics());
        assertTrue(invalidation.requiresFullProjection());
    }

    private static EnumSet<WorkspaceProjectionSlice> expectedChestTransferSlices() {
        return EnumSet.of(
                WorkspaceProjectionSlice.CARD,
                WorkspaceProjectionSlice.SECTION,
                WorkspaceProjectionSlice.STORAGE,
                WorkspaceProjectionSlice.WAYFINDING,
                WorkspaceProjectionSlice.DEPOSITABILITY,
                WorkspaceProjectionSlice.WORKFLOW,
                WorkspaceProjectionSlice.HOTBAR,
                WorkspaceProjectionSlice.FRAME,
                WorkspaceProjectionSlice.REMOTE_SEARCH);
    }
}
