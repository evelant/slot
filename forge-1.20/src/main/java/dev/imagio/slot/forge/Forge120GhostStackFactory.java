package dev.imagio.slot.forge;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class Forge120GhostStackFactory {
    private Forge120GhostStackFactory() {
    }

    public static ItemStack resolve(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return ItemStack.EMPTY;
        }
        try {
            ResourceLocation location = ResourceLocation.tryParse(itemId);
            if (location == null) {
                return ItemStack.EMPTY;
            }
            Item item = BuiltInRegistries.ITEM.get(location);
            if (item == null) {
                return ItemStack.EMPTY;
            }
            return new ItemStack(item);
        } catch (RuntimeException | LinkageError ignored) {
            return ItemStack.EMPTY;
        }
    }
}
