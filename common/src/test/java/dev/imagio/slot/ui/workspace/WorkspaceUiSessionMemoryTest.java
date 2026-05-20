package dev.imagio.slot.ui.workspace;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorkspaceUiSessionMemoryTest {
    @Test
    void rememberedSearchOnlyClearsAfterClosedTimeout() {
        String key = "test-" + UUID.randomUUID();
        long closedAtMillis = 100_000L;

        WorkspaceUiSessionMemory.setSearchQuery(key, "torch");

        assertEquals("torch", WorkspaceUiSessionMemory.searchQuery(key, closedAtMillis + 60_000L));

        WorkspaceUiSessionMemory.markClosed(key, closedAtMillis);

        assertEquals("torch", WorkspaceUiSessionMemory.searchQuery(
                key,
                closedAtMillis + WorkspaceUiSessionMemory.SEARCH_CLOSE_CLEAR_MILLIS - 1L));
        assertEquals("", WorkspaceUiSessionMemory.searchQuery(
                key,
                closedAtMillis + WorkspaceUiSessionMemory.SEARCH_CLOSE_CLEAR_MILLIS));
    }
}
