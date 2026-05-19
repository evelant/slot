package dev.imagio.slot.inventory.core;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class ItemStackTags {
    private ItemStackTags() {
    }

    public static Set<String> itemTagIds(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        stack.getTags().forEach(tagKey -> {
            ResourceLocation location = tagKey.location();
            if (location != null) {
                tags.add(location.toString());
            }
        });
        return tags.isEmpty() ? Set.of() : Collections.unmodifiableSet(tags);
    }
}
