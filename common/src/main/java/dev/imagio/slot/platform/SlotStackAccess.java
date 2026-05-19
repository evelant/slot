package dev.imagio.slot.platform;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public final class SlotStackAccess {
    private static volatile StackAccess current = DefaultStackAccess.INSTANCE;

    private SlotStackAccess() {
    }

    public static StackAccess current() {
        return current;
    }

    public static void install(StackAccess access) {
        current = Objects.requireNonNull(access, "access");
    }

    public interface StackAccess {
        String itemId(ItemStack stack);

        boolean stackable(ItemStack stack);

        String dataFingerprint(ItemStack stack);

        default boolean damageable(ItemStack stack) {
            return DefaultStackAccess.damageableStack(stack);
        }

        boolean sameItemAndData(ItemStack first, ItemStack second);
    }

    // Keeps common unit tests and early bootstrap usable. Loader modules should
    // install a direct implementation before production inventory work runs.
    @SuppressWarnings("deprecation")
    private enum DefaultStackAccess implements StackAccess {
        INSTANCE;

        @Override
        public String itemId(ItemStack stack) {
            if (stack == null) {
                return "";
            }
            try {
                return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            } catch (RuntimeException | LinkageError ignored) {
            }

            String reflectedItemId = invokeStringMethod(stack, "itemId");
            if (!reflectedItemId.isBlank()) {
                return reflectedItemId;
            }
            return stack.toString();
        }

        @Override
        public boolean stackable(ItemStack stack) {
            if (stack == null) {
                return false;
            }
            try {
                return stack.isStackable();
            } catch (RuntimeException | LinkageError ignored) {
            }
            try {
                return stack.getMaxStackSize() > 1;
            } catch (RuntimeException | LinkageError ignored) {
            }
            return true;
        }

        @Override
        public String dataFingerprint(ItemStack stack) {
            if (stack == null) {
                return "";
            }
            String components = componentPatchFingerprint(stack);
            if (!components.isBlank()) {
                return components;
            }
            String testFingerprint = invokeStringMethod(stack, "componentFingerprint");
            if (!testFingerprint.isBlank()) {
                return testFingerprint;
            }
            String tagFingerprint = tagFingerprint(stack);
            return "{}".equals(tagFingerprint) ? "" : tagFingerprint;
        }

        @Override
        public boolean damageable(ItemStack stack) {
            return damageableStack(stack);
        }

        @Override
        public boolean sameItemAndData(ItemStack first, ItemStack second) {
            if (first == second) {
                return true;
            }
            if (first == null || second == null) {
                return false;
            }
            Boolean modern = invokeStaticBoolean("isSameItemSameComponents", first, second);
            if (modern != null) {
                return modern;
            }
            Boolean legacy = invokeStaticBoolean("isSameItemSameTags", first, second);
            if (legacy != null) {
                return legacy;
            }
            if (isEmpty(first) || isEmpty(second)) {
                return isEmpty(first) && isEmpty(second);
            }
            return Objects.equals(itemId(first), itemId(second))
                    && Objects.equals(dataFingerprint(first), dataFingerprint(second));
        }

        private static boolean isEmpty(ItemStack stack) {
            try {
                return stack == null || stack.isEmpty();
            } catch (RuntimeException | LinkageError ignored) {
                return stack == null;
            }
        }

        private static boolean damageableStack(ItemStack stack) {
            if (stack == null) {
                return false;
            }
            Boolean damageable = invokeBooleanMethod(stack, "isDamageableItem");
            if (damageable != null) {
                return damageable;
            }
            Integer maxDamage = invokeIntMethod(stack, "getMaxDamage");
            return maxDamage != null && maxDamage > 0;
        }

        private static String componentPatchFingerprint(ItemStack stack) {
            Object patch = invokeObjectMethod(stack, "getComponentsPatch");
            if (patch == null) {
                return "";
            }
            try {
                Method isEmpty = patch.getClass().getMethod("isEmpty");
                Object empty = isEmpty.invoke(patch);
                if (empty instanceof Boolean booleanValue && booleanValue) {
                    return "";
                }
            } catch (ReflectiveOperationException | RuntimeException ignored) {
            }
            String fingerprint = patch.toString();
            return "{}".equals(fingerprint) ? "" : fingerprint;
        }

        private static String tagFingerprint(ItemStack stack) {
            Object tag = invokeObjectMethod(stack, "getTag");
            if (tag == null) {
                return "";
            }
            try {
                Method isEmpty = tag.getClass().getMethod("isEmpty");
                Object empty = isEmpty.invoke(tag);
                if (empty instanceof Boolean booleanValue && booleanValue) {
                    return "";
                }
            } catch (ReflectiveOperationException | RuntimeException ignored) {
            }
            return tag.toString();
        }

        private static String invokeStringMethod(ItemStack stack, String methodName) {
            Object value = invokeObjectMethod(stack, methodName);
            return value instanceof String string ? string : "";
        }

        private static Boolean invokeBooleanMethod(ItemStack stack, String methodName) {
            Object value = invokeObjectMethod(stack, methodName);
            return value instanceof Boolean booleanValue ? booleanValue : null;
        }

        private static Integer invokeIntMethod(ItemStack stack, String methodName) {
            Object value = invokeObjectMethod(stack, methodName);
            return value instanceof Number number ? number.intValue() : null;
        }

        private static Object invokeObjectMethod(ItemStack stack, String methodName) {
            if (stack == null || methodName == null || methodName.isBlank()) {
                return null;
            }
            try {
                Method method = stack.getClass().getMethod(methodName);
                return method.invoke(stack);
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                return null;
            }
        }

        private static Boolean invokeStaticBoolean(
                String methodName,
                ItemStack first,
                ItemStack second
        ) {
            try {
                Method method = ItemStack.class.getMethod(methodName, ItemStack.class, ItemStack.class);
                Object value = method.invoke(null, first, second);
                return value instanceof Boolean booleanValue ? booleanValue : null;
            } catch (NoSuchMethodException ignored) {
                return null;
            } catch (IllegalAccessException | InvocationTargetException | RuntimeException ignored) {
                return null;
            }
        }
    }
}
