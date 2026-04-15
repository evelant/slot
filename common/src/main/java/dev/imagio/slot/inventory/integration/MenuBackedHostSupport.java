package dev.imagio.slot.inventory.integration;

import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.InventoryStackSnapshot;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.query.InventoryEntryKey;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.inventory.query.InventorySourceSnapshot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class MenuBackedHostSupport {
    private MenuBackedHostSupport() {
    }

    static int inferSupportedStorageSlots(AbstractContainerMenu menu, Inventory playerInventory, String screenClassName) {
        if (menu instanceof ChestMenu chestMenu) {
            return chestMenu.getRowCount() * 9;
        }
        if (menu instanceof ShulkerBoxMenu) {
            return 27;
        }

        Integer reflectedStorageSlots = reflectedStorageSlotCount(menu);
        if (reflectedStorageSlots != null && reflectedStorageSlots > 0 && menu.slots.size() >= reflectedStorageSlots) {
            return reflectedStorageSlots;
        }

        if (!isSupportedModdedStorage(menu, screenClassName)) {
            return -1;
        }
        int totalSlots = menu.slots.size();
        if (totalSlots <= 36) {
            return -1;
        }

        int trailingPlayerSlots = 0;
        for (int slotIndex = totalSlots - 1; slotIndex >= 0; slotIndex--) {
            if (menu.getSlot(slotIndex).container == playerInventory) {
                trailingPlayerSlots++;
            } else {
                break;
            }
        }
        if (trailingPlayerSlots != 36) {
            return -1;
        }

        int containerSlots = totalSlots - trailingPlayerSlots;
        return containerSlots > 0 ? containerSlots : -1;
    }

    static List<Integer> slotRange(int startInclusive, int endInclusive) {
        if (endInclusive < startInclusive) {
            return List.of();
        }
        ArrayList<Integer> slots = new ArrayList<>(endInclusive - startInclusive + 1);
        for (int slot = startInclusive; slot <= endInclusive; slot++) {
            slots.add(slot);
        }
        return List.copyOf(slots);
    }

    static Map<Integer, String> sourceIdsByMenuSlot(String sourceId, List<Integer> menuSlots) {
        LinkedHashMap<Integer, String> sourceIds = new LinkedHashMap<>();
        for (int menuSlot : menuSlots) {
            sourceIds.put(menuSlot, sourceId);
        }
        return Map.copyOf(sourceIds);
    }

    static List<InventoryStackSnapshot> readSnapshots(AbstractContainerMenu menu, List<Integer> menuSlots) {
        if (menu == null || menuSlots == null || menuSlots.isEmpty()) {
            return List.of();
        }
        ArrayList<InventoryStackSnapshot> snapshots = new ArrayList<>();
        for (int logicalSlot = 0; logicalSlot < menuSlots.size(); logicalSlot++) {
            int menuSlot = menuSlots.get(logicalSlot);
            Slot slot = safeSlot(menu, menuSlot);
            if (slot == null) {
                continue;
            }
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) {
                continue;
            }
            snapshots.add(new InventoryStackSnapshot(logicalSlot, stack.copy(), stack.getCount()));
        }
        return List.copyOf(snapshots);
    }

    static InventorySourceSnapshot readSourceSnapshot(AbstractContainerMenu menu, String sourceId, List<Integer> menuSlots) {
        if (sourceId == null || sourceId.isBlank()) {
            return InventorySourceSnapshot.empty("__missing__");
        }
        if (menu == null || menuSlots == null || menuSlots.isEmpty()) {
            return new InventorySourceSnapshot(sourceId, 0, List.of(), "");
        }
        ArrayList<InventoryEntrySnapshot> entries = new ArrayList<>();
        for (int logicalSlot = 0; logicalSlot < menuSlots.size(); logicalSlot++) {
            int menuSlot = menuSlots.get(logicalSlot);
            Slot slot = safeSlot(menu, menuSlot);
            if (slot == null) {
                continue;
            }
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) {
                continue;
            }
            entries.add(new InventoryEntrySnapshot(
                    InventoryEntryKey.slot(sourceId, logicalSlot),
                    stack.copy(),
                    stack.getCount(),
                    ""
            ));
        }
        return new InventorySourceSnapshot(sourceId, menuSlots.size(), List.copyOf(entries), "");
    }

    static MutationResult mutateMenuSlots(
            InventoryHostDescriptor host,
            InventoryMutationRequest request,
            InventoryMutationMode mode,
            List<Integer> menuSlots
    ) {
        AbstractContainerMenu menu = host == null ? null : host.menu();
        ServerPlayer player = request == null ? null : request.player();
        if (menu == null || request == null || menuSlots == null || menuSlots.isEmpty()) {
            return MutationResult.blocked("missing_menu_request_or_slots", request == null ? ItemStack.EMPTY : request.stack());
        }

        return switch (request.kind()) {
            case INSERT -> mutateInsert(menu, request.stack(), mode, menuSlots, request);
            case EXTRACT -> mutateExtract(menu, player, request, mode, menuSlots);
            case ACTIVATE_TARGET, UNSPECIFIED -> MutationResult.blocked("unsupported_mutation", request.stack());
        };
    }

    private static MutationResult mutateInsert(
            AbstractContainerMenu menu,
            ItemStack stack,
            InventoryMutationMode mode,
            List<Integer> menuSlots,
            InventoryMutationRequest request
    ) {
        if (stack == null || stack.isEmpty()) {
            return MutationResult.success(ItemStack.EMPTY);
        }
        if (request != null && request.targetsExactSlot()) {
            int logicalSlot = request.slotIndex();
            if (logicalSlot < 0 || logicalSlot >= menuSlots.size()) {
                return MutationResult.blocked("invalid_slot_index", stack);
            }
            Slot slot = safeSlot(menu, menuSlots.get(logicalSlot));
            if (slot == null) {
                return MutationResult.blocked("missing_menu_slot", stack);
            }
            ItemStack remainder = mode == InventoryMutationMode.SIMULATE
                    ? simulateInsert(slot, stack.copy())
                    : slot.safeInsert(stack.copy());
            return mode == InventoryMutationMode.SIMULATE && !remainder.isEmpty()
                    ? MutationResult.blocked("simulation_incomplete", remainder)
                    : MutationResult.success(remainder);
        }
        ItemStack remainder = stack.copy();
        for (int menuSlot : menuSlots) {
            Slot slot = safeSlot(menu, menuSlot);
            if (slot == null) {
                continue;
            }
            remainder = mode == InventoryMutationMode.SIMULATE
                    ? simulateInsert(slot, remainder)
                    : slot.safeInsert(remainder);
            if (remainder.isEmpty()) {
                return MutationResult.success(ItemStack.EMPTY);
            }
        }
        return mode == InventoryMutationMode.SIMULATE
                ? MutationResult.blocked("simulation_incomplete", remainder)
                : MutationResult.success(remainder);
    }

    private static MutationResult mutateExtract(
            AbstractContainerMenu menu,
            ServerPlayer player,
            InventoryMutationRequest request,
            InventoryMutationMode mode,
            List<Integer> menuSlots
    ) {
        if (request.targetsExactSlot()) {
            int logicalSlot = request.slotIndex();
            if (logicalSlot < 0 || logicalSlot >= menuSlots.size()) {
                return MutationResult.blocked("invalid_slot_index", ItemStack.EMPTY);
            }
            Slot slot = safeSlot(menu, menuSlots.get(logicalSlot));
            ItemStack stack = slot == null ? ItemStack.EMPTY : slot.getItem();
            if (slot == null
                    || stack.isEmpty()
                    || (player != null && !slot.mayPickup(player))
                    || (request.identity() != null && !ItemIdentityMatcher.matchesMovable(stack, request.identity()))) {
                return MutationResult.blocked("no_matching_stack", ItemStack.EMPTY);
            }
            int amount = requestedExtractAmount(request, stack.getCount());
            if (mode == InventoryMutationMode.SIMULATE) {
                ItemStack simulated = stack.copy();
                simulated.setCount(Math.min(amount, simulated.getCount()));
                return MutationResult.success(simulated);
            }
            return MutationResult.success(slot.safeTake(amount, stack.getCount(), player));
        }
        if (request.identity() == null) {
            return MutationResult.blocked("missing_identity", ItemStack.EMPTY);
        }
        for (int menuSlot : menuSlots) {
            Slot slot = safeSlot(menu, menuSlot);
            ItemStack stack = slot == null ? ItemStack.EMPTY : slot.getItem();
            if (slot == null
                    || stack.isEmpty()
                    || (player != null && !slot.mayPickup(player))
                    || !ItemIdentityMatcher.matchesMovable(stack, request.identity())) {
                continue;
            }

            int amount = requestedExtractAmount(request, stack.getCount());
            if (mode == InventoryMutationMode.SIMULATE) {
                ItemStack simulated = stack.copy();
                simulated.setCount(Math.min(amount, simulated.getCount()));
                return MutationResult.success(simulated);
            }
            return MutationResult.success(slot.safeTake(amount, stack.getCount(), player));
        }
        return MutationResult.blocked("no_matching_stack", ItemStack.EMPTY);
    }

    private static int requestedExtractAmount(
            InventoryMutationRequest request,
            int available
    ) {
        if (request != null && request.requestedCount() > 0) {
            return Math.min(request.requestedCount(), Math.max(1, available));
        }
        return switch (request == null ? InventoryTransferMode.ONE : request.transferMode()) {
            case ONE -> 1;
            case STACK, ALL -> Math.max(1, available);
        };
    }

    private static ItemStack simulateInsert(Slot slot, ItemStack sourceStack) {
        if (slot == null || sourceStack == null || sourceStack.isEmpty() || !slot.mayPlace(sourceStack)) {
            return sourceStack == null ? ItemStack.EMPTY : sourceStack;
        }

        ItemStack existing = slot.getItem();
        int transferable;
        if (existing.isEmpty()) {
            transferable = Math.min(sourceStack.getCount(), Math.min(sourceStack.getMaxStackSize(), slot.getMaxStackSize(sourceStack)));
        } else if (ItemStack.isSameItemSameComponents(existing, sourceStack)) {
            int capacity = Math.max(0, Math.min(existing.getMaxStackSize(), slot.getMaxStackSize(existing)) - existing.getCount());
            transferable = Math.min(sourceStack.getCount(), capacity);
        } else {
            transferable = 0;
        }

        if (transferable <= 0) {
            return sourceStack;
        }
        ItemStack remainder = sourceStack.copy();
        remainder.setCount(Math.max(0, remainder.getCount() - transferable));
        return remainder;
    }

    private static Slot safeSlot(AbstractContainerMenu menu, int slotId) {
        if (menu == null || slotId < 0) {
            return null;
        }
        try {
            return menu.getSlot(slotId);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static boolean isSupportedModdedStorage(AbstractContainerMenu menu, String screenClassName) {
        String resolvedScreenClassName = screenClassName == null ? "" : screenClassName;
        return classChainContains(menu.getClass(), "net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase")
                || "net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageScreen".equals(resolvedScreenClassName)
                || "net.p3pp3rf1y.sophisticatedstorage.client.gui.LimitedBarrelScreen".equals(resolvedScreenClassName);
    }

    private static boolean classChainContains(Class<?> type, String className) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            if (className.equals(current.getName())) {
                return true;
            }
        }
        return false;
    }

    private static Integer reflectedStorageSlotCount(AbstractContainerMenu menu) {
        try {
            Method method = menu.getClass().getMethod("getNumberOfStorageInventorySlots");
            Object value = method.invoke(menu);
            if (value instanceof Integer storageSlots && storageSlots > 0) {
                return storageSlots;
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return null;
    }
}
