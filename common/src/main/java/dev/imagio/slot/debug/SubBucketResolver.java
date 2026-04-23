package dev.imagio.slot.debug;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class SubBucketResolver {
    private SubBucketResolver() {
    }

    public static SubBucketRule resolve(ItemStack stack, SemanticBucket parent) {
        if (stack == null || stack.isEmpty() || parent == null) {
            return null;
        }
        List<SubBucketRule> rules = SubBucketRules.rulesFor(parent);
        if (rules.isEmpty()) {
            return null;
        }
        String path = pathOf(stack);
        if (path == null || path.isBlank()) {
            return null;
        }
        String normalized = "_" + path + "_";
        for (SubBucketRule rule : rules) {
            for (String keyword : rule.keywords()) {
                if (normalized.contains("_" + keyword + "_")) {
                    return rule;
                }
            }
        }
        return null;
    }

    private static String pathOf(ItemStack stack) {
        try {
            ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
            return key == null ? "" : key.getPath();
        } catch (RuntimeException | LinkageError ignored) {
            return "";
        }
    }
}
