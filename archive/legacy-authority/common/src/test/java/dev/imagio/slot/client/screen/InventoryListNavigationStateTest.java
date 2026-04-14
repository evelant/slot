package dev.imagio.slot.client.screen;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.OptionalDouble;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryListNavigationStateTest {
    private static final String ALL = "__all__";

    @Test
    void scrollsToIndexedSection() {
        InventoryListNavigationState state = new InventoryListNavigationState(ALL);
        state.indexRows(List.of(row("a"), row(null), row("b")), Row::sectionId);

        OptionalDouble scroll = state.scrollToTarget("b", 20.0, 200.0, 10, index -> 10 + index * 18);

        assertTrue(scroll.isPresent());
        assertEquals(56.0, scroll.getAsDouble());
    }

    @Test
    void allTargetScrollsToTopAndMissingTargetDoesNothing() {
        InventoryListNavigationState state = new InventoryListNavigationState(ALL);
        state.indexRows(List.of(row("a")), Row::sectionId);

        assertEquals(0.0, state.scrollToTarget(ALL, 40.0, 200.0, 10, index -> 10).orElseThrow());
        assertFalse(state.scrollToTarget("missing", 40.0, 200.0, 10, index -> 10).isPresent());
    }

    @Test
    void repeatedRailJumpRestoresPreviousScroll() {
        InventoryListNavigationState state = new InventoryListNavigationState(ALL);
        state.indexRows(List.of(row("a"), row("b")), Row::sectionId);

        OptionalDouble firstJump = state.navigateToTarget("b", ALL, 90.0, 200.0, 10, index -> 10 + index * 18);
        OptionalDouble secondJump = state.navigateToTarget("b", "b", 108.0, 200.0, 10, index -> 10 + index * 18);

        assertEquals(108.0, firstJump.orElseThrow());
        assertEquals(90.0, secondJump.orElseThrow());
    }

    @Test
    void currentSectionUsesNearestVisibleOrPreviousSection() {
        InventoryListNavigationState state = new InventoryListNavigationState(ALL);

        String currentOnHeader = state.currentSectionId(
                4,
                5.0,
                10,
                index -> 28 + index * 18,
                index -> index == 0 ? "a" : null
        );
        String currentAfterHeader = state.currentSectionId(
                4,
                5.0,
                10,
                index -> index == 0 ? 12 : 28 + index * 18,
                index -> index == 0 ? "a" : null
        );

        assertEquals(ALL, state.currentSectionId(0, 5.0, 10, index -> 10, index -> null));
        assertEquals(ALL, state.currentSectionId(4, 0.0, 10, index -> 10, index -> null));
        assertEquals("a", currentOnHeader);
        assertEquals("a", currentAfterHeader);
    }

    @Test
    void findsMatchingRows() {
        List<Row> rows = List.of(row(null), row("a"));

        assertTrue(InventoryListNavigationState.hasMatchingRow(rows, row -> row.sectionId() != null));
        assertEquals("a", InventoryListNavigationState.firstMatchingRow(rows, row -> row.sectionId() != null).sectionId());
        assertFalse(InventoryListNavigationState.hasMatchingRow(rows, row -> "missing".equals(row.sectionId())));
    }

    private static Row row(String sectionId) {
        return new Row(sectionId);
    }

    private record Row(String sectionId) {
    }
}
