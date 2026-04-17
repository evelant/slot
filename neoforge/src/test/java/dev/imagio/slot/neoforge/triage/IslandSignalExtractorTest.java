package dev.imagio.slot.neoforge.triage;

import dev.imagio.slot.inventory.triage.IslandSignalDescriptor;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IslandSignalExtractorTest {
    @Test
    void nullStackYieldsEmptyAirDescriptor() {
        IslandSignalDescriptor descriptor = IslandSignalExtractor.extract(null);

        assertEquals("minecraft:air", descriptor.identity().itemId());
        assertTrue(descriptor.classSignals().isEmpty());
        assertTrue(descriptor.itemTags().isEmpty());
    }

    @Test
    void emptyStackYieldsEmptyAirDescriptor() {
        IslandSignalDescriptor descriptor = IslandSignalExtractor.extract(ItemStack.EMPTY);

        assertEquals("minecraft:air", descriptor.identity().itemId());
        assertTrue(descriptor.classSignals().isEmpty());
        assertTrue(descriptor.itemTags().isEmpty());
    }
}
