package dev.imagio.slot.debug;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoundedDiagnosticThrottleTest {
    @Test
    void suppressesRepeatedKeysUntilIntervalExpires() {
        BoundedDiagnosticThrottle throttle = new BoundedDiagnosticThrottle(100L, 8);

        assertTrue(throttle.shouldEmit("candidate", 1_000L));
        assertFalse(throttle.shouldEmit("candidate", 1_050L));
        assertTrue(throttle.shouldEmit("candidate", 1_100L));
    }

    @Test
    void boundsDistinctKeysEvenWhenTheyAreRecent() {
        BoundedDiagnosticThrottle throttle = new BoundedDiagnosticThrottle(10_000L, 3);

        for (int index = 0; index < 10; index++) {
            assertTrue(throttle.shouldEmit("candidate-" + index, index));
        }

        assertTrue(throttle.size() <= 3);
    }
}
