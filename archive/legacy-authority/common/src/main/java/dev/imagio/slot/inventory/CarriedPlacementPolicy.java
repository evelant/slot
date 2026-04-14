package dev.imagio.slot.inventory;

import dev.imagio.slot.inventory.kernel.ActionableSourcePolicy;
import dev.imagio.slot.inventory.kernel.MenuSlotResolver;
import dev.imagio.slot.session.ChestLikeMenuLayout;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class CarriedPlacementPolicy {
    private CarriedPlacementPolicy() {
    }

    public enum Intent {
        GENERAL,
        STASH,
        TEMPORARY
    }

    public static ItemStack insertIntoCarriedMenuSlots(
            ItemStack stack,
            AbstractContainerMenu menu,
            ChestLikeMenuLayout layout,
            Intent intent
    ) {
        if (stack == null || stack.isEmpty() || menu == null) {
            return stack == null ? ItemStack.EMPTY : stack;
        }

        ItemStack remainder = stack;
        for (int menuSlot : mergeCandidateSlots(menu, layout, intent, remainder, Set.of())) {
            remainder = menu.getSlot(menuSlot).safeInsert(remainder);
            if (remainder.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }

        for (int menuSlot : emptyCandidateSlots(menu, layout, intent, remainder, Set.of())) {
            remainder = menu.getSlot(menuSlot).safeInsert(remainder);
            if (remainder.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }

        return remainder;
    }

    public static Integer findBestCarriedMenuSlot(
            AbstractContainerMenu menu,
            ChestLikeMenuLayout layout,
            ItemStack stack,
            Intent intent,
            Set<Integer> excludedSlots
    ) {
        if (stack == null || stack.isEmpty() || menu == null) {
            return null;
        }

        List<Integer> mergeCandidates = mergeCandidateSlots(menu, layout, intent, stack, excludedSlots == null ? Set.of() : excludedSlots);
        if (!mergeCandidates.isEmpty()) {
            return mergeCandidates.get(0);
        }

        List<Integer> emptyCandidates = emptyCandidateSlots(menu, layout, intent, stack, excludedSlots == null ? Set.of() : excludedSlots);
        if (!emptyCandidates.isEmpty()) {
            return emptyCandidates.get(0);
        }

        return null;
    }

    public static Integer findEmptyCarriedMenuSlot(
            AbstractContainerMenu menu,
            ChestLikeMenuLayout layout,
            Intent intent,
            Set<Integer> excludedSlots,
            ItemStack... requiredPlaceableStacks
    ) {
        if (menu == null) {
            return null;
        }

        Set<Integer> resolvedExcludedSlots = excludedSlots == null ? Set.of() : excludedSlots;
        MenuSlotResolver resolver = new MenuSlotResolver(menu, layout);
        for (int menuSlot : orderedSlots(menu, layout, intent)) {
            if (resolvedExcludedSlots.contains(menuSlot)) {
                continue;
            }

            Slot slot = resolver.safeSlot(menuSlot);
            if (slot == null) {
                continue;
            }
            if (!slot.getItem().isEmpty()) {
                continue;
            }

            boolean allPlaceable = true;
            if (requiredPlaceableStacks != null) {
                for (ItemStack requiredStack : requiredPlaceableStacks) {
                    if (requiredStack != null && !requiredStack.isEmpty() && !slot.mayPlace(requiredStack)) {
                        allPlaceable = false;
                        break;
                    }
                }
            }

            if (allPlaceable) {
                return menuSlot;
            }
        }

        return null;
    }

    private static List<Integer> mergeCandidateSlots(
            AbstractContainerMenu menu,
            ChestLikeMenuLayout layout,
            Intent intent,
        ItemStack stack,
        Set<Integer> excludedSlots
    ) {
        MenuSlotResolver resolver = new MenuSlotResolver(menu, layout);
        List<Integer> candidates = new ArrayList<>();
        for (int menuSlot : orderedSlots(menu, layout, intent)) {
            if (excludedSlots.contains(menuSlot)) {
                continue;
            }

            Slot slot = resolver.safeSlot(menuSlot);
            if (slot == null) {
                continue;
            }
            ItemStack existing = slot.getItem();
            if (existing.isEmpty()) {
                continue;
            }
            if (!slot.mayPlace(stack) || !ItemStack.isSameItemSameComponents(existing, stack)) {
                continue;
            }

            int maxSize = Math.min(existing.getMaxStackSize(), slot.getMaxStackSize(existing));
            if (existing.getCount() < maxSize) {
                candidates.add(menuSlot);
            }
        }
        return List.copyOf(candidates);
    }

    private static List<Integer> emptyCandidateSlots(
            AbstractContainerMenu menu,
            ChestLikeMenuLayout layout,
            Intent intent,
        ItemStack stack,
        Set<Integer> excludedSlots
    ) {
        MenuSlotResolver resolver = new MenuSlotResolver(menu, layout);
        List<Integer> candidates = new ArrayList<>();
        for (int menuSlot : orderedSlots(menu, layout, intent)) {
            if (excludedSlots.contains(menuSlot)) {
                continue;
            }

            Slot slot = resolver.safeSlot(menuSlot);
            if (slot == null) {
                continue;
            }
            if (slot.getItem().isEmpty() && slot.mayPlace(stack)) {
                candidates.add(menuSlot);
            }
        }
        return List.copyOf(candidates);
    }

    private static List<Integer> orderedSlots(AbstractContainerMenu menu, ChestLikeMenuLayout layout, Intent intent) {
        LinkedHashSet<Integer> orderedSlots = new LinkedHashSet<>();
        if (intent == Intent.TEMPORARY) {
            orderedSlots.addAll(mainInventorySlots(menu, layout));
            orderedSlots.addAll(carriedStorageSlots(menu, layout));
        } else {
            orderedSlots.addAll(carriedStorageSlots(menu, layout));
            orderedSlots.addAll(mainInventorySlots(menu, layout));
        }
        if (intent != Intent.STASH) {
            orderedSlots.addAll(hotbarSlots(menu, layout));
        }
        return List.copyOf(orderedSlots);
    }

    private static List<Integer> mainInventorySlots(AbstractContainerMenu menu, ChestLikeMenuLayout layout) {
        return new ActionableSourcePolicy(new MenuSlotResolver(menu, layout)).mainInventoryMenuSlots();
    }

    private static List<Integer> hotbarSlots(AbstractContainerMenu menu, ChestLikeMenuLayout layout) {
        return new ActionableSourcePolicy(new MenuSlotResolver(menu, layout)).hotbarMenuSlots();
    }

    private static List<Integer> carriedStorageSlots(AbstractContainerMenu menu, ChestLikeMenuLayout layout) {
        if (layout == null) {
            return List.of();
        }
        return new MenuSlotResolver(menu, layout).menuSlotsForSource(ChestLikeMenuLayout.SOURCE_CARRIED_STORAGE);
    }
}
