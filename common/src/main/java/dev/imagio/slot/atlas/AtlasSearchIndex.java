package dev.imagio.slot.atlas;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class AtlasSearchIndex {

    public enum Pool {
        PRIMARY,
        SECONDARY
    }

    public record SearchRow(
            String name,
            String tiebreakKey,
            Pool pool,
            boolean carried,
            float targetX,
            float targetY,
            float targetWidth,
            float targetHeight
    ) {
        public SearchRow {
            name = name == null ? "" : name;
            tiebreakKey = tiebreakKey == null ? "" : tiebreakKey;
            pool = pool == null ? Pool.PRIMARY : pool;
            targetWidth = Math.max(1f, targetWidth);
            targetHeight = Math.max(1f, targetHeight);
        }
    }

    public static final int DEFAULT_MIN_QUERY_CHARS = 2;

    private AtlasSearchIndex() {
    }

    public static List<SearchRow> search(List<SearchRow> rows, String query) {
        return search(rows, query, DEFAULT_MIN_QUERY_CHARS);
    }

    /**
     * True if {@code name} matches {@code query} under the same rules
     * {@link #search} uses (lowercased substring, minimum query
     * length). Used by relevance scoring to precompute per-identity
     * match membership without paying for the full ranked search.
     */
    public static boolean matches(String name, String query, int minQueryChars) {
        if (name == null || query == null) {
            return false;
        }
        String needle = query.toLowerCase(Locale.ROOT);
        if (needle.length() < Math.max(1, minQueryChars)) {
            return false;
        }
        return name.toLowerCase(Locale.ROOT).contains(needle);
    }

    public static boolean matches(String name, String query) {
        return matches(name, query, DEFAULT_MIN_QUERY_CHARS);
    }

    public static List<SearchRow> search(List<SearchRow> rows, String query, int minQueryChars) {
        if (rows == null || rows.isEmpty() || query == null) {
            return List.of();
        }
        String needle = query.toLowerCase(Locale.ROOT);
        if (needle.length() < Math.max(1, minQueryChars)) {
            return List.of();
        }
        ArrayList<SearchRow> matches = new ArrayList<>();
        for (SearchRow row : rows) {
            if (row == null) {
                continue;
            }
            String name = row.name().toLowerCase(Locale.ROOT);
            if (name.contains(needle)) {
                matches.add(row);
            }
        }
        matches.sort(Comparator
                .comparingInt((SearchRow r) -> wordBoundaryRank(r.name(), needle))
                .thenComparingInt(r -> r.pool().ordinal())
                .thenComparing(r -> r.carried() ? 0 : 1)
                .thenComparingInt(r -> r.name().length())
                .thenComparing(r -> r.name().toLowerCase(Locale.ROOT))
                .thenComparing(SearchRow::tiebreakKey));
        return List.copyOf(matches);
    }

    private static int wordBoundaryRank(String name, String needle) {
        String lower = name.toLowerCase(Locale.ROOT);
        int idx = lower.indexOf(needle);
        if (idx < 0) {
            return 2;
        }
        if (idx == 0) {
            return 0;
        }
        char prev = lower.charAt(idx - 1);
        return Character.isLetterOrDigit(prev) ? 1 : 0;
    }
}
