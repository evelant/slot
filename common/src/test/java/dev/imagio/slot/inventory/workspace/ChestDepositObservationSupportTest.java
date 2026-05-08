package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChestDepositObservationSupportTest {
    @Test
    void observesNetDepositsByIdentity() {
        ItemStack[] snapshot = {
                new ItemStack("minecraft:redstone", 4, 64),
                ItemStack.EMPTY
        };
        List<ItemStack> current = List.of(
                new ItemStack("minecraft:redstone", 7, 64),
                new ItemStack("minecraft:stone", 3, 64));

        ChestDepositObservationSupport.Observation observation =
                ChestDepositObservationSupport.observe(snapshot, current);

        assertEquals(3, observation.deposits().get(ItemIdentity.of("minecraft:redstone")));
        assertEquals(3, observation.deposits().get(ItemIdentity.of("minecraft:stone")));
        assertTrue(observation.takes().isEmpty());
    }

    @Test
    void observesNetTakesByIdentity() {
        ItemStack[] snapshot = {
                new ItemStack("minecraft:redstone", 12, 64),
                new ItemStack("minecraft:stone", 3, 64)
        };
        List<ItemStack> current = List.of(
                new ItemStack("minecraft:redstone", 5, 64),
                ItemStack.EMPTY);

        ChestDepositObservationSupport.Observation observation =
                ChestDepositObservationSupport.observe(snapshot, current);

        assertTrue(observation.deposits().isEmpty());
        assertEquals(7, observation.takes().get(ItemIdentity.of("minecraft:redstone")));
        assertEquals(3, observation.takes().get(ItemIdentity.of("minecraft:stone")));
    }
}
