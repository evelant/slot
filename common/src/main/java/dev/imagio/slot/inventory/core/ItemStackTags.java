package dev.imagio.slot.inventory.core;

import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

public final class ItemStackTags {
    private ItemStackTags() {
    }

    public static Set<String> itemTagIds(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        try {
            Object value = stack.getClass().getMethod("getTags").invoke(stack);
            if (value instanceof Stream<?> stream) {
                stream.forEach(tagKey -> {
                    String tagId = tagId(tagKey);
                    if (!tagId.isBlank()) {
                        tags.add(tagId);
                    }
                });
            }
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return Set.of();
        }
        return tags.isEmpty() ? Set.of() : Collections.unmodifiableSet(tags);
    }

    private static String tagId(Object tagKey) {
        if (tagKey == null) {
            return "";
        }
        try {
            Object location = tagKey.getClass().getMethod("location").invoke(tagKey);
            return location == null ? "" : location.toString();
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return "";
        }
    }
}
