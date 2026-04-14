package dev.imagio.slot.client.screen;

import dev.imagio.slot.client.model.ItemIdentity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class InventorySelectionRestoreSupportTest {
    @Test
    void findsRowByIdentity() {
        Row stone = row("minecraft:stone");
        Row dirt = row("minecraft:dirt");

        assertEquals(
                dirt,
                InventorySelectionRestoreSupport.findRowByIdentity(ItemIdentity.of("minecraft:dirt"), List.of(stone, dirt), Row::identity)
        );
    }

    @Test
    void missingIdentityClearsSelection() {
        assertNull(InventorySelectionRestoreSupport.findRowByIdentity(
                ItemIdentity.of("minecraft:dirt"),
                List.of(row("minecraft:stone")),
                Row::identity
        ));
        assertNull(InventorySelectionRestoreSupport.findRowByIdentity(null, List.of(row("minecraft:stone")), Row::identity));
    }

    @Test
    void findsRowByStableRowId() {
        Row first = new Row(ItemIdentity.of("minecraft:stone"), "carried/item/category/building/minecraft:stone");
        Row second = new Row(ItemIdentity.of("minecraft:stone"), "open/item/category/building/minecraft:stone");

        assertEquals(
                second,
                InventorySelectionRestoreSupport.findRowByValue(
                        "open/item/category/building/minecraft:stone",
                        List.of(first, second),
                        Row::rowId
                )
        );
    }

    @Test
    void respectsPaneSearchOrder() {
        Row openStone = row("minecraft:stone");
        Row carriedStone = row("minecraft:stone");

        InventorySelectionRestoreSupport.Selection<Row, Pane> selection = InventorySelectionRestoreSupport.findInPaneOrder(
                ItemIdentity.of("minecraft:stone"),
                Row::identity,
                List.of(
                        new InventorySelectionRestoreSupport.PaneRows<>(Pane.CARRIED, List.of(carriedStone)),
                        new InventorySelectionRestoreSupport.PaneRows<>(Pane.OPEN, List.of(openStone))
                )
        );

        assertEquals(carriedStone, selection.row());
        assertEquals(Pane.CARRIED, selection.pane());
    }

    @Test
    void respectsPaneSearchOrderForRowIds() {
        Row openStone = new Row(ItemIdentity.of("minecraft:stone"), "open/item/category/building/minecraft:stone");
        Row carriedStone = new Row(ItemIdentity.of("minecraft:stone"), "carried/item/category/building/minecraft:stone");

        InventorySelectionRestoreSupport.Selection<Row, Pane> selection = InventorySelectionRestoreSupport.findInPaneOrderByValue(
                "open/item/category/building/minecraft:stone",
                Row::rowId,
                List.of(
                        new InventorySelectionRestoreSupport.PaneRows<>(Pane.CARRIED, List.of(carriedStone)),
                        new InventorySelectionRestoreSupport.PaneRows<>(Pane.OPEN, List.of(openStone))
                )
        );

        assertEquals(openStone, selection.row());
        assertEquals(Pane.OPEN, selection.pane());
    }

    private static Row row(String itemId) {
        return new Row(ItemIdentity.of(itemId), null);
    }

    private record Row(ItemIdentity identity, String rowId) {
    }

    private enum Pane {
        OPEN,
        CARRIED
    }
}
