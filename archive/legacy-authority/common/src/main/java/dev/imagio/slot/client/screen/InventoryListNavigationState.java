package dev.imagio.slot.client.screen;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;

public final class InventoryListNavigationState {
    private final String allTargetId;
    private final Map<String, Integer> sectionIndices = new LinkedHashMap<>();
    private String lastRailJumpTargetId;
    private double lastRailJumpPreviousScroll = -1.0;

    public InventoryListNavigationState(String allTargetId) {
        this.allTargetId = allTargetId;
    }

    public <T> void indexRows(List<T> rows, Function<T, String> sectionIdResolver) {
        sectionIndices.clear();
        clearRailJumpHistory();
        if (rows == null || sectionIdResolver == null) {
            return;
        }

        for (int index = 0; index < rows.size(); index++) {
            String sectionId = sectionIdResolver.apply(rows.get(index));
            if (sectionId != null) {
                sectionIndices.put(sectionId, index);
            }
        }
    }

    public OptionalDouble navigateToTarget(
            String targetId,
            String currentSectionId,
            double currentScroll,
            double maxScroll,
            int listY,
            IntUnaryOperator rowTopForIndex
    ) {
        if (targetId != null
                && targetId.equals(lastRailJumpTargetId)
                && targetId.equals(currentSectionId)
                && lastRailJumpPreviousScroll >= 0.0) {
            double previousScroll = lastRailJumpPreviousScroll;
            clearRailJumpHistory();
            return OptionalDouble.of(clampScroll(previousScroll, maxScroll));
        }

        lastRailJumpTargetId = targetId;
        lastRailJumpPreviousScroll = currentScroll;
        return scrollToTarget(targetId, currentScroll, maxScroll, listY, rowTopForIndex);
    }

    public OptionalDouble scrollToTarget(
            String targetId,
            double currentScroll,
            double maxScroll,
            int listY,
            IntUnaryOperator rowTopForIndex
    ) {
        if (allTargetId.equals(targetId)) {
            return OptionalDouble.of(0.0);
        }

        Integer anchorIndex = sectionIndices.get(targetId);
        if (anchorIndex == null || rowTopForIndex == null) {
            return OptionalDouble.empty();
        }

        double desiredScroll = currentScroll + rowTopForIndex.applyAsInt(anchorIndex) - listY;
        return OptionalDouble.of(clampScroll(desiredScroll, maxScroll));
    }

    public String currentSectionId(
            int itemCount,
            double scrollAmount,
            int listY,
            IntUnaryOperator rowBottomForIndex,
            IntFunction<String> sectionIdAtIndex
    ) {
        if (itemCount <= 0 || scrollAmount <= 0.0 || rowBottomForIndex == null || sectionIdAtIndex == null) {
            return allTargetId;
        }

        String current = allTargetId;
        int probeY = listY + 4;
        for (int index = 0; index < itemCount; index++) {
            String sectionId = sectionIdAtIndex.apply(index);
            if (rowBottomForIndex.applyAsInt(index) < probeY) {
                if (sectionId != null) {
                    current = sectionId;
                }
                continue;
            }
            return sectionId != null ? sectionId : current;
        }
        return current;
    }

    public static <T> boolean hasMatchingRow(List<T> rows, Predicate<T> predicate) {
        return firstMatchingRow(rows, predicate) != null;
    }

    public static <T> T firstMatchingRow(List<T> rows, Predicate<T> predicate) {
        if (rows == null || predicate == null) {
            return null;
        }
        for (T row : rows) {
            if (predicate.test(row)) {
                return row;
            }
        }
        return null;
    }

    private void clearRailJumpHistory() {
        lastRailJumpTargetId = null;
        lastRailJumpPreviousScroll = -1.0;
    }

    private static double clampScroll(double scroll, double maxScroll) {
        return Math.max(0.0, Math.min(scroll, maxScroll));
    }
}
