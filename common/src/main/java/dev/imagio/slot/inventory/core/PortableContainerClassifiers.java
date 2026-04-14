package dev.imagio.slot.inventory.core;

import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class PortableContainerClassifiers {
    private static final List<PortableContainerClassifier> CLASSIFIERS = new CopyOnWriteArrayList<>();

    private PortableContainerClassifiers() {
    }

    public static void register(PortableContainerClassifier classifier) {
        if (classifier != null) {
            CLASSIFIERS.add(classifier);
        }
    }

    public static boolean isPortableContainer(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (hasContainerComponent(stack)) {
            return true;
        }
        if (!fitsInsideContainerItems(stack)) {
            return true;
        }
        for (PortableContainerClassifier classifier : CLASSIFIERS) {
            if (classifier != null && classifier.isPortableContainer(stack)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasContainerComponent(ItemStack stack) {
        try {
            Class<?> dataComponentsClass = Class.forName("net.minecraft.core.component.DataComponents");
            Field containerField = dataComponentsClass.getField("CONTAINER");
            Object containerComponent = containerField.get(null);
            return containerComponent != null && stack.getClass()
                    .getMethod("has", Class.forName("net.minecraft.core.component.DataComponentType"))
                    .invoke(stack, containerComponent) instanceof Boolean present
                    && present;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    private static boolean fitsInsideContainerItems(ItemStack stack) {
        try {
            Object item = stack.getClass().getMethod("getItem").invoke(stack);
            Object result = item.getClass().getMethod("canFitInsideContainerItems").invoke(item);
            return !(result instanceof Boolean present) || present;
        } catch (ReflectiveOperationException ignored) {
            return true;
        }
    }
}
