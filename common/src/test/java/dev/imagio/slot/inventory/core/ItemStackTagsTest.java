package dev.imagio.slot.inventory.core;

import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ItemStackTagsTest {
    @Test
    void itemTagIdsReadsMinecraftStackTags() {
        ItemStack stack = new ItemStack("tfc:ore/normal_hematite/granite", 1, 64)
                .withTags("c:ores/cast_iron/normal", "tfc:metal_ores");

        assertEquals(
                Set.of("c:ores/cast_iron/normal", "tfc:metal_ores"),
                ItemStackTags.itemTagIds(stack));
    }
}
