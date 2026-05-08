package dev.imagio.slot.inventory.workspace;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    void shiftClickDepositStopsWhenDesiredCountConsumesCarry() {
        assertEquals(
                0,
                WorkspaceChestCommandService.requestedExplicitDepositCount(
                        5,
                        5,
                        WorkspaceChestCommandService.DepositQuantity.STACK,
                        WorkspaceChestCommandService.DesiredCountPolicy.RESPECT));
    }
}
