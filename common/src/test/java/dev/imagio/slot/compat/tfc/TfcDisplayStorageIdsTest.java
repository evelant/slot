package dev.imagio.slot.compat.tfc;

import dev.imagio.slot.inventory.storage.WorldDisplayStorageKind;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TfcDisplayStorageIdsTest {
    @Test
    void recognizesTfcAndPackToolRackIdShapes() {
        assertEquals(
                WorldDisplayStorageKind.TOOL_RACK,
                TfcDisplayStorageIds.kindForBlockId("tfc", "wood/tool_rack/blackwood"));
        assertEquals(
                WorldDisplayStorageKind.TOOL_RACK,
                TfcDisplayStorageIds.kindForBlockId("tfc", "wood/planks/blackwood_tool_rack"));
        assertEquals(
                WorldDisplayStorageKind.TOOL_RACK,
                TfcDisplayStorageIds.kindForBlockId("tfg", "wood/tool_rack/mahoe"));
        assertEquals(
                WorldDisplayStorageKind.TOOL_RACK,
                TfcDisplayStorageIds.kindForBlockId("beneath", "wood/planks/crimson_tool_rack"));
    }

    @Test
    void recognizesPlacedItemButDoesNotOvermatchOtherBlocks() {
        assertEquals(
                WorldDisplayStorageKind.PLACED_ITEM,
                TfcDisplayStorageIds.kindForBlockId("tfc", "placed_item"));
        assertNull(TfcDisplayStorageIds.kindForBlockId("tfg", "placed_item"));
        assertNull(TfcDisplayStorageIds.kindForBlockId("minecraft", "oak_planks"));
        assertNull(TfcDisplayStorageIds.kindForBlockId("tfc", "wood/chest/blackwood"));
    }
}
