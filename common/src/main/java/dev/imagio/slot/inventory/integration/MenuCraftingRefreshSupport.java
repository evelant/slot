package dev.imagio.slot.inventory.integration;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.RecipeType;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class MenuCraftingRefreshSupport {
    private MenuCraftingRefreshSupport() {
    }

    static RefreshPlan resolve(AbstractContainerMenu menu, List<Integer> candidateInputSlots) {
        if (menu == null) {
            return RefreshPlan.unsupported();
        }

        Container inputContainer = resolveInputContainer(menu, candidateInputSlots);
        RefreshPlan upgradePlan = resolveOpenCraftingContainerPlan(menu, inputContainer);
        if (upgradePlan.supported()) {
            return upgradePlan;
        }

        if (inputContainer != null) {
            Method slotsChanged = findOverrideMethod(menu.getClass(), AbstractContainerMenu.class, "slotsChanged", Container.class);
            if (slotsChanged != null) {
                return new RefreshPlan(menu, slotsChanged, new Object[]{inputContainer});
            }

            Method menuCraftMatrixChanged = findMethod(menu.getClass(), "onCraftMatrixChanged", Container.class);
            if (menuCraftMatrixChanged != null) {
                return new RefreshPlan(menu, menuCraftMatrixChanged, new Object[]{inputContainer});
            }
        }

        Method noArgCraftMatrixChanged = findMethod(menu.getClass(), "onCraftMatrixChanged");
        if (noArgCraftMatrixChanged != null) {
            return new RefreshPlan(menu, noArgCraftMatrixChanged, new Object[0]);
        }

        return upgradePlan.state() == RefreshPlan.State.FAILED ? upgradePlan : RefreshPlan.unsupported();
    }

    private static RefreshPlan resolveOpenCraftingContainerPlan(AbstractContainerMenu menu, Container inputContainer) {
        Method resolver = findMethod(menu.getClass(), "getOpenOrFirstCraftingContainer", RecipeType.class);
        if (resolver == null) {
            return RefreshPlan.unsupported();
        }

        try {
            Object value = resolver.invoke(menu, RecipeType.CRAFTING);
            Object craftingContainer = value instanceof Optional<?> optional ? optional.orElse(null) : value;
            if (craftingContainer == null || !matchesInputContainer(craftingContainer, inputContainer)) {
                return RefreshPlan.unsupported();
            }

            if (inputContainer != null) {
                Method onCraftMatrixChanged = findMethod(craftingContainer.getClass(), "onCraftMatrixChanged", Container.class);
                if (onCraftMatrixChanged != null) {
                    return new RefreshPlan(craftingContainer, onCraftMatrixChanged, new Object[]{inputContainer});
                }

                Method slotsChanged = findMethod(craftingContainer.getClass(), "slotsChanged", Container.class);
                if (slotsChanged != null) {
                    return new RefreshPlan(craftingContainer, slotsChanged, new Object[]{inputContainer});
                }
            }

            Method noArgCraftMatrixChanged = findMethod(craftingContainer.getClass(), "onCraftMatrixChanged");
            if (noArgCraftMatrixChanged != null) {
                return new RefreshPlan(craftingContainer, noArgCraftMatrixChanged, new Object[0]);
            }
        } catch (ReflectiveOperationException ignored) {
            return RefreshPlan.failed();
        }

        return RefreshPlan.unsupported();
    }

    private static boolean matchesInputContainer(Object craftingContainer, Container inputContainer) {
        if (craftingContainer == null || inputContainer == null) {
            return true;
        }

        Method recipeSlotsMethod = findMethod(craftingContainer.getClass(), "getRecipeSlots");
        if (recipeSlotsMethod == null) {
            return true;
        }

        try {
            Object recipeSlotsValue = recipeSlotsMethod.invoke(craftingContainer);
            if (!(recipeSlotsValue instanceof List<?> recipeSlots) || recipeSlots.isEmpty()) {
                return true;
            }
            for (Object slotValue : recipeSlots) {
                if (slotValue instanceof Slot recipeSlot && recipeSlot.container == inputContainer) {
                    return true;
                }
            }
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
        return false;
    }

    private static Container resolveInputContainer(AbstractContainerMenu menu, List<Integer> candidateInputSlots) {
        if (menu == null || candidateInputSlots == null || candidateInputSlots.isEmpty()) {
            return null;
        }

        Map<Container, Integer> countsByContainer = new LinkedHashMap<>();
        for (int slotId : candidateInputSlots) {
            Slot slot = safeMenuSlot(menu, slotId);
            if (slot == null || slot.container == null) {
                continue;
            }
            countsByContainer.merge(slot.container, 1, Integer::sum);
        }

        return countsByContainer.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private static Slot safeMenuSlot(AbstractContainerMenu menu, int slotId) {
        if (menu == null || slotId < 0) {
            return null;
        }
        try {
            return menu.getSlot(slotId);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static Method findOverrideMethod(Class<?> type, Class<?> stopTypeExclusive, String name, Class<?>... parameterTypes) {
        Class<?> current = type;
        while (current != null && current != stopTypeExclusive) {
            try {
                Method method = current.getDeclaredMethod(name, parameterTypes);
                method.setAccessible(true);
                return method;
            } catch (ReflectiveOperationException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    private static Method findMethod(Class<?> type, String name, Class<?>... parameterTypes) {
        Class<?> current = type;
        while (current != null) {
            try {
                Method method = current.getDeclaredMethod(name, parameterTypes);
                method.setAccessible(true);
                return method;
            } catch (ReflectiveOperationException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    record RefreshPlan(Object target, Method method, Object[] arguments, State state) {
        private RefreshPlan(Object target, Method method, Object[] arguments) {
            this(target, method, arguments, State.SUPPORTED);
        }

        static RefreshPlan unsupported() {
            return new RefreshPlan(null, null, new Object[0], State.UNSUPPORTED);
        }

        static RefreshPlan failed() {
            return new RefreshPlan(null, null, new Object[0], State.FAILED);
        }

        boolean supported() {
            return state == State.SUPPORTED;
        }

        boolean refresh() {
            if (!supported() || target == null || method == null) {
                return false;
            }
            try {
                method.invoke(target, arguments);
                return true;
            } catch (ReflectiveOperationException ignored) {
                return false;
            }
        }

        enum State {
            SUPPORTED,
            UNSUPPORTED,
            FAILED
        }
    }
}
