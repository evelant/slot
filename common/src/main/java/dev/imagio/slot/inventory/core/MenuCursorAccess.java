package dev.imagio.slot.inventory.core;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class MenuCursorAccess {
    private static final Method GET_CARRIED_METHOD = resolveMethod("getCarried");
    private static final Method SET_CARRIED_METHOD = resolveMethod("setCarried", ItemStack.class);
    private static final Field CARRIED_FIELD = resolveField("carried", "carriedItem");

    private MenuCursorAccess() {
    }

    public static ItemStack get(AbstractContainerMenu menu) {
        if (menu == null) {
            return ItemStack.EMPTY;
        }
        try {
            if (GET_CARRIED_METHOD != null) {
                Object value = GET_CARRIED_METHOD.invoke(menu);
                if (value instanceof ItemStack stack) {
                    return stack.copy();
                }
            }
            if (CARRIED_FIELD != null) {
                Object value = CARRIED_FIELD.get(menu);
                if (value instanceof ItemStack stack) {
                    return stack.copy();
                }
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return ItemStack.EMPTY;
    }

    public static void set(AbstractContainerMenu menu, ItemStack stack) {
        if (menu == null) {
            return;
        }
        ItemStack resolved = stack == null ? ItemStack.EMPTY : stack;
        try {
            if (SET_CARRIED_METHOD != null) {
                SET_CARRIED_METHOD.invoke(menu, resolved);
                return;
            }
            if (CARRIED_FIELD != null) {
                CARRIED_FIELD.set(menu, resolved);
            }
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private static Method resolveMethod(String name, Class<?>... parameterTypes) {
        try {
            Method method = AbstractContainerMenu.class.getMethod(name, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static Field resolveField(String... candidateNames) {
        for (String candidateName : candidateNames) {
            Class<?> type = AbstractContainerMenu.class;
            while (type != null) {
                try {
                    Field field = type.getDeclaredField(candidateName);
                    field.setAccessible(true);
                    return field;
                } catch (ReflectiveOperationException ignored) {
                    type = type.getSuperclass();
                }
            }
        }
        return null;
    }
}
