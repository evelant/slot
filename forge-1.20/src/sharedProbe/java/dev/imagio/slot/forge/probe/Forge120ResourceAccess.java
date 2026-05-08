package dev.imagio.slot.forge.probe;

import dev.imagio.slot.platform.SlotResourceAccess;
import net.minecraft.resources.ResourceLocation;

final class Forge120ResourceAccess implements SlotResourceAccess.ResourceAccess {
    @SuppressWarnings("removal")
    @Override
    public ResourceLocation id(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }
}
