package dev.imagio.slot.inventory.core;

import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class PortableContainerClassifiers {
    private static final List<PortableContainerClassifier> CLASSIFIERS = new CopyOnWriteArrayList<>();
    private static final ConcurrentMap<Class<?>, Optional<Method>> STACK_GET_ITEM_METHODS = new ConcurrentHashMap<>();
    private static final ConcurrentMap<Class<?>, Optional<Method>> ITEM_CAN_FIT_METHODS = new ConcurrentHashMap<>();

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
        if (DataComponentContainerAccess.CONTAINER_COMPONENT == null
                || DataComponentContainerAccess.DATA_COMPONENT_TYPE_CLASS == null) {
            return false;
        }
        Optional<Method> maybeHasMethod = DataComponentContainerAccess.HAS_METHODS.computeIfAbsent(
                stack.getClass(),
                PortableContainerClassifiers::findDataComponentHasMethod);
        if (maybeHasMethod.isEmpty()) {
            return false;
        }
        try {
            return maybeHasMethod.get()
                    .invoke(stack, DataComponentContainerAccess.CONTAINER_COMPONENT) instanceof Boolean present
                    && present;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return false;
        }
    }

    private static boolean fitsInsideContainerItems(ItemStack stack) {
        Optional<Method> maybeGetItem = STACK_GET_ITEM_METHODS.computeIfAbsent(
                stack.getClass(),
                PortableContainerClassifiers::findGetItemMethod);
        if (maybeGetItem.isEmpty()) {
            return true;
        }
        try {
            Object item = maybeGetItem.get().invoke(stack);
            if (item == null) {
                return true;
            }
            Optional<Method> maybeCanFit = ITEM_CAN_FIT_METHODS.computeIfAbsent(
                    item.getClass(),
                    PortableContainerClassifiers::findCanFitInsideContainerItemsMethod);
            if (maybeCanFit.isEmpty()) {
                return true;
            }
            Object result = maybeCanFit.get().invoke(item);
            return !(result instanceof Boolean present) || present;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return true;
        }
    }

    private static Optional<Method> findDataComponentHasMethod(Class<?> stackClass) {
        try {
            return Optional.of(stackClass.getMethod("has", DataComponentContainerAccess.DATA_COMPONENT_TYPE_CLASS));
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return Optional.empty();
        }
    }

    private static Optional<Method> findGetItemMethod(Class<?> stackClass) {
        try {
            return Optional.of(stackClass.getMethod("getItem"));
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return Optional.empty();
        }
    }

    private static Optional<Method> findCanFitInsideContainerItemsMethod(Class<?> itemClass) {
        try {
            return Optional.of(itemClass.getMethod("canFitInsideContainerItems"));
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return Optional.empty();
        }
    }

    private static final class DataComponentContainerAccess {
        private static final Object CONTAINER_COMPONENT;
        private static final Class<?> DATA_COMPONENT_TYPE_CLASS;
        private static final ConcurrentMap<Class<?>, Optional<Method>> HAS_METHODS = new ConcurrentHashMap<>();

        static {
            Object containerComponent = null;
            Class<?> dataComponentTypeClass = null;
            try {
                Class<?> dataComponentsClass = Class.forName("net.minecraft.core.component.DataComponents");
                dataComponentTypeClass = Class.forName("net.minecraft.core.component.DataComponentType");
                Field containerField = dataComponentsClass.getField("CONTAINER");
                containerComponent = containerField.get(null);
            } catch (ReflectiveOperationException | LinkageError ignored) {
                containerComponent = null;
                dataComponentTypeClass = null;
            }
            CONTAINER_COMPONENT = containerComponent;
            DATA_COMPONENT_TYPE_CLASS = dataComponentTypeClass;
        }
    }
}
