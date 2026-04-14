package dev.imagio.slot.inventory.kernel;

import dev.imagio.slot.client.model.SlotRef;
import dev.imagio.slot.session.ChestLikeMenuLayout;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class MenuSlotResolver {
    private final AbstractContainerMenu menu;
    private final ChestLikeMenuLayout layout;

    public MenuSlotResolver(AbstractContainerMenu menu, ChestLikeMenuLayout layout) {
        this.menu = menu;
        this.layout = layout;
    }

    public AbstractContainerMenu menu() {
        return menu;
    }

    public ChestLikeMenuLayout layout() {
        return layout;
    }

    public Slot safeSlot(int slotId) {
        return safeSlot(menu, slotId);
    }

    public boolean hasLogicalSlot(int slotId) {
        return safeSlot(slotId) != null;
    }

    public boolean sourceMenuBacked(String sourceId) {
        return !menuSlotsForSource(sourceId).isEmpty();
    }

    public List<Integer> menuSlotsForSource(String sourceId) {
        if (layout == null || sourceId == null || sourceId.isBlank()) {
            return List.of();
        }

        LinkedHashSet<Integer> resolved = new LinkedHashSet<>();
        for (int slotId : layout.menuSlotsForSource(sourceId)) {
            if (safeSlot(slotId) != null) {
                resolved.add(slotId);
            }
        }
        return List.copyOf(resolved);
    }

    public Integer resolveMenuSlot(String sourceId, int slotIndex) {
        if (layout == null || sourceId == null || sourceId.isBlank()) {
            return null;
        }
        Integer resolved = layout.resolveMenuSlot(sourceId, slotIndex);
        return resolved == null || safeSlot(resolved) == null ? null : resolved;
    }

    public Integer resolveMenuSlot(SlotRef slotRef) {
        if (slotRef == null) {
            return null;
        }
        return resolveMenuSlot(slotRef.sourceId(), slotRef.slotIndex());
    }

    public List<Integer> insertionTargets(List<Integer> candidateSlots, ItemStack stack) {
        if (stack == null || stack.isEmpty() || candidateSlots == null || candidateSlots.isEmpty()) {
            return List.of();
        }

        ArrayList<Integer> mergeTargets = new ArrayList<>();
        ArrayList<Integer> emptyTargets = new ArrayList<>();
        for (int slotId : candidateSlots) {
            Slot slot = safeSlot(slotId);
            if (slot == null) {
                continue;
            }
            int capacity = insertionCapacity(slot, stack);
            if (capacity <= 0) {
                continue;
            }
            if (slot.getItem().isEmpty()) {
                emptyTargets.add(slotId);
            } else {
                mergeTargets.add(slotId);
            }
        }

        ArrayList<Integer> ordered = new ArrayList<>(mergeTargets.size() + emptyTargets.size());
        ordered.addAll(mergeTargets);
        ordered.addAll(emptyTargets);
        return List.copyOf(ordered);
    }

    public Integer firstInsertionTarget(List<Integer> candidateSlots, ItemStack stack) {
        List<Integer> ordered = insertionTargets(candidateSlots, stack);
        return ordered.isEmpty() ? null : ordered.get(0);
    }

    public static Slot safeSlot(AbstractContainerMenu menu, int slotId) {
        if (menu == null || slotId < 0) {
            return null;
        }
        try {
            return menu.getSlot(slotId);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    public static int insertionCapacity(Slot slot, ItemStack sourceStack) {
        if (slot == null || sourceStack == null || sourceStack.isEmpty() || !slot.mayPlace(sourceStack)) {
            return 0;
        }

        ItemStack existing = slot.getItem();
        if (existing.isEmpty()) {
            return Math.min(sourceStack.getMaxStackSize(), slot.getMaxStackSize(sourceStack));
        }
        if (!ItemStack.isSameItemSameComponents(existing, sourceStack)) {
            return 0;
        }
        return Math.max(0, Math.min(existing.getMaxStackSize(), slot.getMaxStackSize(existing)) - existing.getCount());
    }
}
