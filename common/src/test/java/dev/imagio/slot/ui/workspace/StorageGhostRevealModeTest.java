package dev.imagio.slot.ui.workspace;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StorageGhostRevealModeTest {
    @Test
    void proximateToggleOpensAndClosesNearbyReveal() {
        assertEquals(StorageGhostRevealMode.PROXIMATE, StorageGhostRevealMode.COLLAPSED.toggleProximate());
        assertEquals(StorageGhostRevealMode.COLLAPSED, StorageGhostRevealMode.PROXIMATE.toggleProximate());
    }

    @Test
    void trackedToggleOpensAndClosesTrackedReveal() {
        assertEquals(StorageGhostRevealMode.TRACKED, StorageGhostRevealMode.COLLAPSED.toggleTracked());
        assertEquals(StorageGhostRevealMode.COLLAPSED, StorageGhostRevealMode.TRACKED.toggleTracked());
    }

    @Test
    void togglingOtherModeReplacesCurrentReveal() {
        assertEquals(StorageGhostRevealMode.TRACKED, StorageGhostRevealMode.PROXIMATE.toggleTracked());
        assertEquals(StorageGhostRevealMode.PROXIMATE, StorageGhostRevealMode.TRACKED.toggleProximate());
    }
}
