package dev.imagio.slot.client.screen;

import dev.imagio.slot.client.model.ItemIdentity;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public final class InventorySelectionRestoreSupport {
    private InventorySelectionRestoreSupport() {
    }

    public static <R, K> R findRowByValue(
            K selectedValue,
            List<R> rows,
            Function<R, K> valueResolver
    ) {
        if (selectedValue == null || rows == null || valueResolver == null) {
            return null;
        }

        for (R row : rows) {
            K rowValue = valueResolver.apply(row);
            if (Objects.equals(selectedValue, rowValue)) {
                return row;
            }
        }
        return null;
    }

    public static <R> R findRowByIdentity(
            ItemIdentity selectedIdentity,
            List<R> rows,
            Function<R, ItemIdentity> identityResolver
    ) {
        return findRowByValue(selectedIdentity, rows, identityResolver);
    }

    public static <R, P, K> Selection<R, P> findInPaneOrderByValue(
            K selectedValue,
            Function<R, K> valueResolver,
            List<PaneRows<R, P>> paneRows
    ) {
        if (selectedValue == null || paneRows == null || valueResolver == null) {
            return Selection.empty();
        }

        for (PaneRows<R, P> pane : paneRows) {
            R row = findRowByValue(selectedValue, pane.rows(), valueResolver);
            if (row != null) {
                return new Selection<>(row, pane.pane());
            }
        }
        return Selection.empty();
    }

    public static <R, P> Selection<R, P> findInPaneOrder(
            ItemIdentity selectedIdentity,
            Function<R, ItemIdentity> identityResolver,
            List<PaneRows<R, P>> paneRows
    ) {
        return findInPaneOrderByValue(selectedIdentity, identityResolver, paneRows);
    }

    public record PaneRows<R, P>(P pane, List<R> rows) {
    }

    public record Selection<R, P>(R row, P pane) {
        private static <R, P> Selection<R, P> empty() {
            return new Selection<>(null, null);
        }
    }
}
