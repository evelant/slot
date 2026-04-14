package dev.imagio.slot.client.screen.container;

import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.storage.adapter.ExternalToolSlotRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CraftingPlacementPolicyTest {
    private static final ItemIdentity STONE = ItemIdentity.of("minecraft:stone");
    private static final ItemIdentity DIRT = ItemIdentity.of("minecraft:dirt");

    @Test
    void requestsPlacementUsingResolvedLogicalMenuSlot() {
        CraftingPlacementPolicy.CraftingPlacementDecision decision = CraftingPlacementPolicy.evaluate(
                MenuSlotId.of(166),
                true,
                ExternalToolSlotRole.INPUT,
                true,
                null,
                STONE
        );

        assertTrue(decision.requested());
        assertEquals(MenuSlotId.of(166), decision.targetMenuSlotId());
    }

    @Test
    void rejectsPlacementForNonInputSlots() {
        CraftingPlacementPolicy.CraftingPlacementDecision decision = CraftingPlacementPolicy.evaluate(
                MenuSlotId.of(175),
                true,
                ExternalToolSlotRole.OUTPUT,
                true,
                null,
                STONE
        );

        assertFalse(decision.requested());
        assertEquals(MenuSlotId.INVALID, decision.targetMenuSlotId());
    }

    @Test
    void rejectsPlacementWhenExistingIdentityDoesNotMatchRequestedIdentity() {
        CraftingPlacementPolicy.CraftingPlacementDecision decision = CraftingPlacementPolicy.evaluate(
                MenuSlotId.of(168),
                true,
                ExternalToolSlotRole.INPUT,
                true,
                DIRT,
                STONE
        );

        assertFalse(decision.requested());
        assertEquals(MenuSlotId.INVALID, decision.targetMenuSlotId());
    }
}
