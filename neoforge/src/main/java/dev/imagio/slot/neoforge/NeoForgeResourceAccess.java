package dev.imagio.slot.neoforge;

import dev.imagio.slot.platform.SlotResourceAccess;
import net.minecraft.resources.ResourceLocation;

public final class NeoForgeResourceAccess implements SlotResourceAccess.ResourceAccess {
    @Override
    public ResourceLocation id(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }
}
