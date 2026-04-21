package dev.imagio.slot.atlas;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CameraHistoryTest {

    @Test
    void freshHistoryIsEmpty() {
        CameraHistory<String> history = new CameraHistory<>();
        assertFalse(history.canGoBack());
        assertFalse(history.canGoForward());
        assertEquals(0, history.backSize());
        assertEquals(0, history.forwardSize());
    }

    @Test
    void backOnEmptyReturnsEmpty() {
        CameraHistory<String> history = new CameraHistory<>();
        assertTrue(history.back("current").isEmpty());
        assertTrue(history.forward("current").isEmpty());
    }

    @Test
    void recordCommitPushesPreviousCamera() {
        CameraHistory<String> history = new CameraHistory<>();
        history.recordCommit("A");
        assertTrue(history.canGoBack());
        assertEquals(1, history.backSize());

        Optional<String> popped = history.back("B");
        assertEquals(Optional.of("A"), popped);
        assertFalse(history.canGoBack());
        assertTrue(history.canGoForward());
        assertEquals(1, history.forwardSize());
    }

    @Test
    void forwardReturnsAfterBack() {
        CameraHistory<String> history = new CameraHistory<>();
        history.recordCommit("A");
        history.back("B");
        Optional<String> forward = history.forward("A");
        assertEquals(Optional.of("B"), forward);
        assertTrue(history.canGoBack());
        assertFalse(history.canGoForward());
    }

    @Test
    void newCommitAfterBackClearsForwardStack() {
        CameraHistory<String> history = new CameraHistory<>();
        history.recordCommit("A");
        history.back("B");
        assertEquals(1, history.forwardSize());

        history.recordCommit("A");
        assertEquals(0, history.forwardSize());
        assertEquals(1, history.backSize());
    }

    @Test
    void ringBufferEvictsOldestBeyondBound() {
        CameraHistory<Integer> history = new CameraHistory<>(3);
        history.recordCommit(1);
        history.recordCommit(2);
        history.recordCommit(3);
        history.recordCommit(4);
        assertEquals(3, history.backSize());

        assertEquals(Optional.of(4), history.back(99));
        assertEquals(Optional.of(3), history.back(4));
        assertEquals(Optional.of(2), history.back(3));
        assertTrue(history.back(2).isEmpty());
    }

    @Test
    void recordCommitIgnoresNull() {
        CameraHistory<String> history = new CameraHistory<>();
        history.recordCommit(null);
        assertEquals(0, history.backSize());
    }

    @Test
    void backWithNullCurrentStillPopsButSkipsForwardPush() {
        CameraHistory<String> history = new CameraHistory<>();
        history.recordCommit("A");
        Optional<String> popped = history.back(null);
        assertEquals(Optional.of("A"), popped);
        assertEquals(0, history.forwardSize());
    }

    @Test
    void clearEmptiesBothStacks() {
        CameraHistory<String> history = new CameraHistory<>();
        history.recordCommit("A");
        history.recordCommit("B");
        history.back("C");
        history.clear();
        assertEquals(0, history.backSize());
        assertEquals(0, history.forwardSize());
    }

    @Test
    void twentyFiveCommitsThenTwentyBacks() {
        CameraHistory<Integer> history = new CameraHistory<>();
        for (int i = 1; i <= 25; i++) {
            history.recordCommit(i);
        }
        assertEquals(20, history.backSize());
        int current = 26;
        for (int i = 0; i < 20; i++) {
            Optional<Integer> popped = history.back(current);
            assertTrue(popped.isPresent());
            current = popped.get();
        }
        assertFalse(history.canGoBack());
        assertEquals(6, current);
    }

    @Test
    void invalidMaxEntriesThrows() {
        assertThrows(IllegalArgumentException.class, () -> new CameraHistory<>(0));
        assertThrows(IllegalArgumentException.class, () -> new CameraHistory<>(-5));
    }
}
