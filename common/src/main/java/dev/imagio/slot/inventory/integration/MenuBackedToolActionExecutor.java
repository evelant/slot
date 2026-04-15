package dev.imagio.slot.inventory.integration;

import dev.imagio.slot.inventory.action.InventoryActionMode;
import dev.imagio.slot.inventory.core.InventoryCapability;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventoryToolActionId;
import dev.imagio.slot.inventory.core.InventoryToolDescriptor;
import dev.imagio.slot.inventory.core.ToolRegionDescriptor;
import dev.imagio.slot.inventory.core.ToolRegionRole;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class MenuBackedToolActionExecutor {
    private MenuBackedToolActionExecutor() {
    }

    static ToolActionResult execute(
            InventoryHostDescriptor host,
            InventoryToolDescriptor tool,
            InventoryToolActionId actionId,
            InventoryActionMode mode
    ) {
        if (host == null || tool == null || actionId == null) {
            return ToolActionResult.blocked("missing_host_tool_or_action");
        }

        ToolRegionDescriptor inputRegion = tool.regions().stream()
                .filter(region -> region.role() == ToolRegionRole.INPUT)
                .findFirst()
                .orElse(null);
        if (inputRegion == null || !inputRegion.supports(InventoryCapability.TOOL_REGION_MUTATION)) {
            return ToolActionResult.blocked("tool_has_no_mutable_input_region");
        }

        List<Integer> inputSlots = host.topology().menuSlotsForToolRegion(inputRegion.id());
        if (inputSlots.isEmpty()) {
            return ToolActionResult.blocked("tool_input_region_unbound");
        }
        if (mode == InventoryActionMode.SIMULATE) {
            return ToolActionResult.success();
        }

        boolean changed = switch (actionId) {
            case CLEAR_GRID -> clearGrid(host.menu(), inputSlots);
            case BALANCE_GRID -> balanceGrid(host.menu(), inputSlots);
            case ROTATE_GRID -> rotateGrid(host.menu(), inputSlots);
            case PROVIDER_DEFINED -> false;
        };
        if (!changed) {
            return ToolActionResult.blocked("tool_action_had_no_effect");
        }
        return ToolActionResult.success();
    }

    private static boolean clearGrid(AbstractContainerMenu menu, List<Integer> inputSlots) {
        boolean changed = false;
        for (int slotId : inputSlots) {
            Slot slot = safeMenuSlot(menu, slotId);
            if (slot == null || !slot.hasItem()) {
                continue;
            }
            slot.set(ItemStack.EMPTY);
            slot.setChanged();
            changed = true;
        }
        return changed;
    }

    private static boolean balanceGrid(AbstractContainerMenu menu, List<Integer> inputSlots) {
        Map<String, List<Integer>> slotsByKey = new LinkedHashMap<>();
        Map<String, Integer> countsByKey = new LinkedHashMap<>();

        for (int slotId : inputSlots) {
            Slot slot = safeMenuSlot(menu, slotId);
            if (slot == null || !slot.hasItem()) {
                continue;
            }
            ItemStack stack = slot.getItem();
            if (stack.getMaxStackSize() <= 1) {
                continue;
            }
            String key = stack.getItem() + "@" + stack.getComponentsPatch();
            slotsByKey.computeIfAbsent(key, ignored -> new ArrayList<>()).add(slotId);
            countsByKey.merge(key, stack.getCount(), Integer::sum);
        }

        boolean changed = false;
        for (Map.Entry<String, List<Integer>> entry : slotsByKey.entrySet()) {
            List<Integer> slotIds = entry.getValue();
            if (slotIds.size() <= 1) {
                continue;
            }
            int total = countsByKey.getOrDefault(entry.getKey(), 0);
            int perStack = total / slotIds.size();
            int remainder = total % slotIds.size();
            for (int slotId : slotIds) {
                Slot slot = safeMenuSlot(menu, slotId);
                if (slot == null || !slot.hasItem()) {
                    continue;
                }
                ItemStack updated = slot.getItem().copy();
                updated.setCount(perStack);
                changed |= !ItemStack.matches(slot.getItem(), updated);
                slot.set(updated);
                slot.setChanged();
            }
            int index = 0;
            while (remainder > 0 && !slotIds.isEmpty()) {
                Slot slot = safeMenuSlot(menu, slotIds.get(index));
                if (slot != null && slot.hasItem() && slot.getItem().getCount() < slot.getItem().getMaxStackSize()) {
                    ItemStack updated = slot.getItem().copy();
                    updated.grow(1);
                    changed |= !ItemStack.matches(slot.getItem(), updated);
                    slot.set(updated);
                    slot.setChanged();
                    remainder--;
                }
                index = (index + 1) % slotIds.size();
            }
        }
        return changed;
    }

    private static boolean rotateGrid(AbstractContainerMenu menu, List<Integer> inputSlots) {
        if (inputSlots.size() != 9) {
            return false;
        }

        List<ItemStack> snapshot = inputSlots.stream()
                .map(slotId -> {
                    Slot slot = safeMenuSlot(menu, slotId);
                    return slot == null ? ItemStack.EMPTY : slot.getItem().copy();
                })
                .toList();

        boolean changed = false;
        for (int sourceIndex = 0; sourceIndex < snapshot.size(); sourceIndex++) {
            int targetIndex = switch (sourceIndex) {
                case 0 -> 1;
                case 1 -> 2;
                case 2 -> 5;
                case 3 -> 0;
                case 4 -> 4;
                case 5 -> 8;
                case 6 -> 3;
                case 7 -> 6;
                case 8 -> 7;
                default -> sourceIndex;
            };
            Slot slot = safeMenuSlot(menu, inputSlots.get(targetIndex));
            if (slot == null) {
                continue;
            }
            ItemStack updated = snapshot.get(sourceIndex).copy();
            changed |= !ItemStack.matches(slot.getItem(), updated);
            slot.set(updated);
            slot.setChanged();
        }
        return changed;
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
}
