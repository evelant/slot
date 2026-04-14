package dev.imagio.slot.client.screen;

import dev.imagio.slot.session.ChestLikeMenuLayout;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CarriedTransferTargetsTest {
    private static final int ARMOR_SLOT_END = 9;
    private static final int INV_SLOT_START = 9;
    private static final int INV_SLOT_END = 36;
    private static final int USE_ROW_SLOT_START = 36;
    private static final int USE_ROW_SLOT_END = 45;

    @Test
    void vanillaTargetRangesUseExclusiveInventoryMenuEndBounds() {
        List<Integer> mainSlots = CarriedTransferTargets.vanillaTargetSlotsFor(ChestLikeMenuLayout.SOURCE_PLAYER_MAIN);
        List<Integer> hotbarSlots = CarriedTransferTargets.vanillaTargetSlotsFor(ChestLikeMenuLayout.SOURCE_PLAYER_HOTBAR);
        List<Integer> armorSlots = CarriedTransferTargets.vanillaTargetSlotsFor(ChestLikeMenuLayout.SOURCE_PLAYER_ARMOR);

        assertEquals(INV_SLOT_START, mainSlots.get(0));
        assertEquals(INV_SLOT_END - 1, mainSlots.get(mainSlots.size() - 1));
        assertEquals(INV_SLOT_END - INV_SLOT_START, mainSlots.size());
        assertFalse(mainSlots.contains(INV_SLOT_END));

        assertEquals(USE_ROW_SLOT_START, hotbarSlots.get(0));
        assertEquals(USE_ROW_SLOT_END - 1, hotbarSlots.get(hotbarSlots.size() - 1));
        assertEquals(USE_ROW_SLOT_END - USE_ROW_SLOT_START, hotbarSlots.size());
        assertFalse(hotbarSlots.contains(USE_ROW_SLOT_END));

        assertEquals(List.of(5, 6, 7, 8), armorSlots);
        assertFalse(armorSlots.contains(ARMOR_SLOT_END));
    }
}
