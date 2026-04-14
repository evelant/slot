package dev.imagio.slot.inventory.core;

import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface PortableContainerClassifier {
    boolean isPortableContainer(ItemStack stack);
}
