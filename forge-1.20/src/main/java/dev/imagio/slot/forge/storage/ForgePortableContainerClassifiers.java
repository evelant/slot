package dev.imagio.slot.forge.storage;

import dev.imagio.slot.inventory.core.PortableContainerClassifiers;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

public final class ForgePortableContainerClassifiers {
    private static boolean registered;

    private ForgePortableContainerClassifiers() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        PortableContainerClassifiers.register(ForgePortableContainerClassifiers::hasItemHandlerCapability);
        registered = true;
    }

    private static boolean hasItemHandlerCapability(ItemStack stack) {
        return stack != null
                && !stack.isEmpty()
                && stack.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve().isPresent();
    }
}
