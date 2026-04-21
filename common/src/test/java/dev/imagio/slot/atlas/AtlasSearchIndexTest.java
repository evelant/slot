package dev.imagio.slot.atlas;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AtlasSearchIndexTest {

    private static AtlasSearchIndex.SearchRow row(String name, String key, AtlasSearchIndex.Pool pool, boolean carried) {
        return new AtlasSearchIndex.SearchRow(name, key, pool, carried, 0f, 0f, 96f, 72f);
    }

    @Test
    void emptyQueryReturnsEmpty() {
        List<AtlasSearchIndex.SearchRow> rows = List.of(
                row("Logs", "logs", AtlasSearchIndex.Pool.PRIMARY, true)
        );
        assertTrue(AtlasSearchIndex.search(rows, "").isEmpty());
        assertTrue(AtlasSearchIndex.search(rows, null).isEmpty());
    }

    @Test
    void prefixMatchIsCaseInsensitive() {
        List<AtlasSearchIndex.SearchRow> rows = List.of(
                row("Logs", "logs", AtlasSearchIndex.Pool.PRIMARY, true),
                row("Stone", "stone", AtlasSearchIndex.Pool.PRIMARY, true)
        );
        List<AtlasSearchIndex.SearchRow> matches = AtlasSearchIndex.search(rows, "LOG");
        assertEquals(1, matches.size());
        assertEquals("Logs", matches.get(0).name());
    }

    @Test
    void primaryRanksBeforeSecondary() {
        List<AtlasSearchIndex.SearchRow> rows = List.of(
                row("Logs Island", "islandA", AtlasSearchIndex.Pool.SECONDARY, false),
                row("Logs", "logs", AtlasSearchIndex.Pool.PRIMARY, true)
        );
        List<AtlasSearchIndex.SearchRow> matches = AtlasSearchIndex.search(rows, "log");
        assertEquals(2, matches.size());
        assertEquals(AtlasSearchIndex.Pool.PRIMARY, matches.get(0).pool());
        assertEquals(AtlasSearchIndex.Pool.SECONDARY, matches.get(1).pool());
    }

    @Test
    void carriedRanksBeforeGhostWithinPool() {
        List<AtlasSearchIndex.SearchRow> rows = List.of(
                row("Log Stripped", "logstripped", AtlasSearchIndex.Pool.PRIMARY, false),
                row("Log Oak", "logoak", AtlasSearchIndex.Pool.PRIMARY, true)
        );
        List<AtlasSearchIndex.SearchRow> matches = AtlasSearchIndex.search(rows, "log");
        assertEquals("Log Oak", matches.get(0).name());
        assertEquals("Log Stripped", matches.get(1).name());
    }

    @Test
    void shorterNameFirstWithinSamePoolAndCarriedFlag() {
        List<AtlasSearchIndex.SearchRow> rows = List.of(
                row("Logbook", "logbook", AtlasSearchIndex.Pool.PRIMARY, true),
                row("Log", "log", AtlasSearchIndex.Pool.PRIMARY, true),
                row("Log Oak", "logoak", AtlasSearchIndex.Pool.PRIMARY, true)
        );
        List<AtlasSearchIndex.SearchRow> matches = AtlasSearchIndex.search(rows, "log");
        assertEquals("Log", matches.get(0).name());
        assertEquals("Log Oak", matches.get(1).name());
        assertEquals("Logbook", matches.get(2).name());
    }

    @Test
    void lexicographicTiebreakerOnSameLength() {
        List<AtlasSearchIndex.SearchRow> rows = List.of(
                row("Log B", "b", AtlasSearchIndex.Pool.PRIMARY, true),
                row("Log A", "a", AtlasSearchIndex.Pool.PRIMARY, true)
        );
        List<AtlasSearchIndex.SearchRow> matches = AtlasSearchIndex.search(rows, "log");
        assertEquals("Log A", matches.get(0).name());
        assertEquals("Log B", matches.get(1).name());
    }

    @Test
    void tiebreakKeyAsFinalStableOrder() {
        List<AtlasSearchIndex.SearchRow> rows = List.of(
                row("Log", "zz", AtlasSearchIndex.Pool.PRIMARY, true),
                row("Log", "aa", AtlasSearchIndex.Pool.PRIMARY, true)
        );
        List<AtlasSearchIndex.SearchRow> matches = AtlasSearchIndex.search(rows, "log");
        assertEquals("aa", matches.get(0).tiebreakKey());
        assertEquals("zz", matches.get(1).tiebreakKey());
    }

    @Test
    void cycleOrderStableAcrossRepeatedCalls() {
        List<AtlasSearchIndex.SearchRow> rows = List.of(
                row("Log Oak", "oak", AtlasSearchIndex.Pool.PRIMARY, true),
                row("Log Birch", "birch", AtlasSearchIndex.Pool.PRIMARY, true),
                row("Log Spruce", "spruce", AtlasSearchIndex.Pool.PRIMARY, false)
        );
        List<AtlasSearchIndex.SearchRow> first = AtlasSearchIndex.search(rows, "log");
        List<AtlasSearchIndex.SearchRow> second = AtlasSearchIndex.search(rows, "log");
        assertEquals(first, second);
    }

    @Test
    void resultIsImmutable() {
        List<AtlasSearchIndex.SearchRow> rows = List.of(
                row("Log", "log", AtlasSearchIndex.Pool.PRIMARY, true)
        );
        List<AtlasSearchIndex.SearchRow> matches = AtlasSearchIndex.search(rows, "log");
        assertEquals(1, matches.size());
        try {
            matches.add(row("Stone", "stone", AtlasSearchIndex.Pool.PRIMARY, false));
            throw new AssertionError("Result list should be immutable");
        } catch (UnsupportedOperationException expected) {
        }
    }

    @Test
    void nonMatchingQueryReturnsEmpty() {
        List<AtlasSearchIndex.SearchRow> rows = List.of(
                row("Logs", "logs", AtlasSearchIndex.Pool.PRIMARY, true)
        );
        assertTrue(AtlasSearchIndex.search(rows, "xxzzz").isEmpty());
    }

    @Test
    void substringMatchFindsSuffixWords() {
        List<AtlasSearchIndex.SearchRow> rows = List.of(
                row("Oak Log", "oak_log", AtlasSearchIndex.Pool.PRIMARY, true),
                row("Birch Log", "birch_log", AtlasSearchIndex.Pool.PRIMARY, true),
                row("Stone", "stone", AtlasSearchIndex.Pool.PRIMARY, true)
        );
        List<AtlasSearchIndex.SearchRow> matches = AtlasSearchIndex.search(rows, "log");
        assertEquals(2, matches.size());
    }

    @Test
    void wordBoundaryMatchRanksBeforeMidWord() {
        List<AtlasSearchIndex.SearchRow> rows = List.of(
                row("Blogpost", "blogpost", AtlasSearchIndex.Pool.PRIMARY, true),
                row("Oak Log", "oak_log", AtlasSearchIndex.Pool.PRIMARY, true)
        );
        List<AtlasSearchIndex.SearchRow> matches = AtlasSearchIndex.search(rows, "log");
        assertEquals("Oak Log", matches.get(0).name());
        assertEquals("Blogpost", matches.get(1).name());
    }

    @Test
    void prefixAndWordBoundaryBothRankBeforeMidWord() {
        List<AtlasSearchIndex.SearchRow> rows = List.of(
                row("Oak Log", "oak_log", AtlasSearchIndex.Pool.PRIMARY, true),
                row("Logs", "logs", AtlasSearchIndex.Pool.PRIMARY, true),
                row("Blog Post", "blog_post", AtlasSearchIndex.Pool.PRIMARY, true)
        );
        List<AtlasSearchIndex.SearchRow> matches = AtlasSearchIndex.search(rows, "log");
        assertEquals(3, matches.size());
        assertEquals("Blog Post", matches.get(2).name());
    }

    @Test
    void singleCharQueryReturnsEmpty() {
        List<AtlasSearchIndex.SearchRow> rows = List.of(
                row("Log", "log", AtlasSearchIndex.Pool.PRIMARY, true)
        );
        assertTrue(AtlasSearchIndex.search(rows, "l").isEmpty());
    }

    @Test
    void twoCharQueryReturnsMatches() {
        List<AtlasSearchIndex.SearchRow> rows = List.of(
                row("Log", "log", AtlasSearchIndex.Pool.PRIMARY, true)
        );
        assertEquals(1, AtlasSearchIndex.search(rows, "lo").size());
    }

    @Test
    void customMinQueryCharsOverridesDefault() {
        List<AtlasSearchIndex.SearchRow> rows = List.of(
                row("Log", "log", AtlasSearchIndex.Pool.PRIMARY, true)
        );
        assertEquals(1, AtlasSearchIndex.search(rows, "l", 1).size());
        assertTrue(AtlasSearchIndex.search(rows, "lo", 3).isEmpty());
    }
}
