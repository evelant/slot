package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.storage.WorldStorageAccess;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared chest-menu close diffing for learned storage affinity.
 *
 * <p>Platform observers are responsible for identifying which block a menu
 * belongs to and which runtime should receive events. This class owns the
 * inventory delta semantics so Forge and NeoForge do not drift.
 */
public final class ChestDepositObservationSupport {
    private ChestDepositObservationSupport() {
    }

    public static ItemStack[] snapshot(Container container, int slotCount) {
        int slots = boundedSlotCount(container, slotCount);
        ItemStack[] snapshot = new ItemStack[slots];
        for (int i = 0; i < slots; i++) {
            ItemStack stack = container.getItem(i);
            snapshot[i] = stack == null ? ItemStack.EMPTY : stack.copy();
        }
        return snapshot;
    }

    public static ItemStack[] snapshot(AbstractContainerMenu menu, List<Integer> menuSlots) {
        if (menu == null || menuSlots == null || menuSlots.isEmpty()) {
            return new ItemStack[0];
        }
        ItemStack[] snapshot = new ItemStack[menuSlots.size()];
        for (int i = 0; i < menuSlots.size(); i++) {
            Slot slot = safeSlot(menu, menuSlots.get(i));
            ItemStack stack = slot == null ? ItemStack.EMPTY : slot.getItem();
            snapshot[i] = stack == null ? ItemStack.EMPTY : stack.copy();
        }
        return snapshot;
    }

    public static List<WorldStorageAccess.SlotContent> currentContents(
            AbstractContainerMenu menu,
            List<Integer> menuSlots
    ) {
        if (menu == null || menuSlots == null || menuSlots.isEmpty()) {
            return List.of();
        }
        ArrayList<WorldStorageAccess.SlotContent> contents = new ArrayList<>();
        for (int i = 0; i < menuSlots.size(); i++) {
            Slot slot = safeSlot(menu, menuSlots.get(i));
            ItemStack stack = slot == null ? ItemStack.EMPTY : slot.getItem();
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            contents.add(new WorldStorageAccess.SlotContent(i, stack.copy()));
        }
        return List.copyOf(contents);
    }

    public static Observation observe(ItemStack[] initial, Container current, int slotCount) {
        if (initial == null || current == null) {
            return Observation.empty();
        }
        int slots = Math.min(initial.length, boundedSlotCount(current, slotCount));
        return observe(initial, slots, current::getItem);
    }

    public static Observation observe(ItemStack[] initial, List<ItemStack> current) {
        if (initial == null || current == null) {
            return Observation.empty();
        }
        int slots = Math.min(initial.length, current.size());
        return observe(initial, slots, current::get);
    }

    public static Observation observe(ItemStack[] initial, AbstractContainerMenu current, List<Integer> menuSlots) {
        if (initial == null || current == null || menuSlots == null) {
            return Observation.empty();
        }
        int slots = Math.min(initial.length, menuSlots.size());
        return observe(initial, slots, index -> {
            Slot slot = safeSlot(current, menuSlots.get(index));
            return slot == null ? ItemStack.EMPTY : slot.getItem();
        });
    }

    public static List<Integer> storageMenuSlots(AbstractContainerMenu menu, Inventory playerInventory) {
        if (menu == null || playerInventory == null || menu.slots == null || menu.slots.isEmpty()) {
            return List.of();
        }
        ArrayList<Integer> slots = new ArrayList<>();
        for (int menuSlot = 0; menuSlot < menu.slots.size(); menuSlot++) {
            Slot slot = safeSlot(menu, menuSlot);
            if (slot == null || slot.container == null || slot.container == playerInventory) {
                continue;
            }
            slots.add(menuSlot);
        }
        return List.copyOf(slots);
    }

    private static Observation observe(
            ItemStack[] initial,
            int slots,
            java.util.function.IntFunction<ItemStack> currentStack
    ) {
        LinkedHashMap<ItemIdentity, Integer> netDeltas = new LinkedHashMap<>();
        for (int i = 0; i < slots; i++) {
            ItemStack stack = initial[i];
            if (stack != null && !stack.isEmpty()) {
                netDeltas.merge(
                        ItemIdentityMatcher.normalizeMovable(ItemIdentityMatcher.create(stack)),
                        -stack.getCount(),
                        Integer::sum);
            }
        }
        for (int i = 0; i < slots; i++) {
            ItemStack stack = currentStack.apply(i);
            if (stack != null && !stack.isEmpty()) {
                netDeltas.merge(
                        ItemIdentityMatcher.normalizeMovable(ItemIdentityMatcher.create(stack)),
                        stack.getCount(),
                        Integer::sum);
            }
        }
        return new Observation(positiveOnly(netDeltas), negativeOnly(netDeltas));
    }

    private static Slot safeSlot(AbstractContainerMenu menu, int menuSlot) {
        if (menu == null || menuSlot < 0 || menuSlot >= menu.slots.size()) {
            return null;
        }
        return menu.slots.get(menuSlot);
    }

    private static int boundedSlotCount(Container container, int slotCount) {
        if (container == null) {
            return 0;
        }
        int slots = slotCount <= 0 ? container.getContainerSize() : slotCount;
        return Math.max(0, Math.min(slots, container.getContainerSize()));
    }

    private static Map<ItemIdentity, Integer> positiveOnly(Map<ItemIdentity, Integer> deltas) {
        if (deltas == null || deltas.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<ItemIdentity, Integer> out = new LinkedHashMap<>();
        deltas.forEach((id, delta) -> {
            if (id != null && delta != null && delta > 0) {
                out.put(id, delta);
            }
        });
        return Map.copyOf(out);
    }

    private static Map<ItemIdentity, Integer> negativeOnly(Map<ItemIdentity, Integer> deltas) {
        if (deltas == null || deltas.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<ItemIdentity, Integer> out = new LinkedHashMap<>();
        deltas.forEach((id, delta) -> {
            if (id != null && delta != null && delta < 0) {
                out.put(id, -delta);
            }
        });
        return Map.copyOf(out);
    }

    public record Observation(
            Map<ItemIdentity, Integer> deposits,
            Map<ItemIdentity, Integer> takes
    ) {
        public Observation {
            deposits = deposits == null ? Map.of() : Map.copyOf(deposits);
            takes = takes == null ? Map.of() : Map.copyOf(takes);
        }

        public static Observation empty() {
            return new Observation(Map.of(), Map.of());
        }
    }
}
