package dev.imagio.slot.neoforge.storage;

import dev.imagio.slot.inventory.core.PortableContainerClassifiers;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;

public final class NeoForgePortableContainerClassifiers {
    private static boolean registered;

    private NeoForgePortableContainerClassifiers() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        PortableContainerClassifiers.register(NeoForgePortableContainerClassifiers::hasItemHandlerCapability);
        registered = true;
    }

    private static boolean hasItemHandlerCapability(ItemStack stack) {
        return stack != null
                && !stack.isEmpty()
                && stack.getCapability(Capabilities.ItemHandler.ITEM) != null;
    }
}
