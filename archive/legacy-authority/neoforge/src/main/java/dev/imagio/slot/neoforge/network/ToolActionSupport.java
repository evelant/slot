package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.capability.ToolCapabilityDescriptor;
import dev.imagio.slot.session.InventoryHostDescriptor;
import dev.imagio.slot.session.StorageViewResolver;
import dev.imagio.slot.network.ToolActionRequests;
import dev.imagio.slot.operation.ActionReason;
import dev.imagio.slot.storage.adapter.ExternalToolCapability;
import dev.imagio.slot.storage.adapter.ExternalToolSpec;
import dev.imagio.slot.storage.adapter.ExternalToolToggleId;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class ToolActionSupport {
    private ToolActionSupport() {
    }

    static ToolActionResult apply(AbstractContainerMenu menu, ServerPlayer player, String toolId, ToolActionRequests.Action action) {
        if (menu == null || player == null || toolId == null || toolId.isBlank() || action == null) {
            return ToolActionResult.blocked(ActionReason.UNSUPPORTED_TOOL, toolActionSummaryKey());
        }

        ExternalToolSpec tool = resolveTool(menu, player, toolId);
        if (tool == null) {
            return ToolActionResult.blocked(ActionReason.UNSUPPORTED_TOOL, toolActionSummaryKey());
        }

        boolean supported = switch (action) {
            case CLEAR_GRID -> tool.supports(ExternalToolCapability.CLEAR_GRID);
            case BALANCE_GRID -> tool.supports(ExternalToolCapability.BALANCE_GRID);
            case ROTATE_GRID_CW, ROTATE_GRID_CCW -> tool.supports(ExternalToolCapability.ROTATE_GRID);
            case TOGGLE_AUTO_REFILL -> tool.supports(ExternalToolCapability.AUTO_REFILL_TOGGLE);
        };
        if (!supported) {
            return ToolActionResult.blocked(ActionReason.UNSUPPORTED_TOOL, toolActionSummaryKey());
        }

        boolean requiresCraftRefresh = requiresCraftRefresh(action);
        CraftingMenuRefreshSupport.RefreshPlan refreshPlan = requiresCraftRefresh
                ? CraftingMenuRefreshSupport.resolve(menu, tool.menuSlotsForRole(dev.imagio.slot.storage.adapter.ExternalToolSlotRole.INPUT))
                : null;
        if (requiresCraftRefresh && (refreshPlan == null || !refreshPlan.supported())) {
            return ToolActionResult.blocked(ActionReason.COMPAT_UNAVAILABLE, toolActionSummaryKey());
        }

        try (MenuMutationTransaction transaction = refreshPlan == null ? null : MenuMutationTransaction.capture(menu)) {
            boolean applied = switch (action) {
                case CLEAR_GRID -> clearGrid(menu, player, tool);
                case BALANCE_GRID -> balanceGrid(menu, tool);
                case ROTATE_GRID_CW -> rotateGrid(menu, tool, false);
                case ROTATE_GRID_CCW -> rotateGrid(menu, tool, true);
                case TOGGLE_AUTO_REFILL -> toggleAutoRefill(menu, player, tool);
            };

            if (applied) {
                if (refreshPlan != null
                        && refreshPlan.refresh(menu) != CraftingMenuRefreshSupport.RefreshResult.REFRESHED) {
                    return ToolActionResult.failed(ActionReason.COMPAT_ERROR, toolActionSummaryKey());
                }
                if (transaction != null) {
                    transaction.commit();
                }
                return ToolActionResult.confirmed();
            }
            return ToolActionResult.blocked(ActionReason.UNSPECIFIED, toolActionSummaryKey());
        }
    }

    private static ExternalToolSpec resolveTool(AbstractContainerMenu menu, ServerPlayer player, String toolId) {
        return resolveLayoutTool(menu, player, toolId);
    }

    private static ExternalToolSpec resolveLayoutTool(AbstractContainerMenu menu, ServerPlayer player, String toolId) {
        InventoryHostDescriptor host = StorageViewResolver.resolve(null, menu, player.getInventory(), null);
        if (host == null) {
            return null;
        }
        ToolCapabilityDescriptor tool = host.capabilities().toolById(toolId);
        return tool == null || !tool.live() ? null : tool.toolSpec();
    }

    private static boolean clearGrid(AbstractContainerMenu menu, ServerPlayer player, ExternalToolSpec tool) {
        boolean changed = false;
        for (int menuSlotId : tool.menuSlotsForRole(dev.imagio.slot.storage.adapter.ExternalToolSlotRole.INPUT)) {
            Slot slot = resolveMenuSlot(menu, menuSlotId);
            if (slot == null || !slot.hasItem() || !slot.mayPickup(player)) {
                continue;
            }

            ItemStack before = slot.getItem().copy();
            menu.clicked(menuSlotId, 0, ClickType.QUICK_MOVE, player);
            Slot afterSlot = resolveMenuSlot(menu, menuSlotId);
            if (afterSlot == null || !ItemStack.matches(before, afterSlot.getItem())) {
                changed = true;
            }
        }

        SlotDebugLog.log("Applied crafting tool clear action: toolId={} changed={}", tool.id(), changed);
        return changed;
    }

    private static boolean balanceGrid(AbstractContainerMenu menu, ExternalToolSpec tool) {
        Map<String, List<Integer>> slotIdsByKey = new LinkedHashMap<>();
        Map<String, Integer> totalCountsByKey = new LinkedHashMap<>();

        for (int menuSlotId : tool.menuSlotsForRole(dev.imagio.slot.storage.adapter.ExternalToolSlotRole.INPUT)) {
            Slot slot = resolveMenuSlot(menu, menuSlotId);
            if (slot == null || !slot.hasItem()) {
                continue;
            }

            ItemStack stack = slot.getItem();
            if (stack.getMaxStackSize() <= 1) {
                continue;
            }

            String key = Objects.toString(BuiltInRegistries.ITEM.getKey(stack.getItem())) + "@" + stack.getComponentsPatch();
            slotIdsByKey.computeIfAbsent(key, ignored -> new ArrayList<>()).add(menuSlotId);
            totalCountsByKey.merge(key, stack.getCount(), Integer::sum);
        }

        boolean changed = false;
        for (Map.Entry<String, List<Integer>> entry : slotIdsByKey.entrySet()) {
            List<Integer> slotIds = entry.getValue();
            if (slotIds.size() <= 1) {
                continue;
            }

            int totalCount = totalCountsByKey.getOrDefault(entry.getKey(), 0);
            int countPerStack = totalCount / slotIds.size();
            int remainder = totalCount % slotIds.size();
            for (int slotId : slotIds) {
                Slot slot = resolveMenuSlot(menu, slotId);
                if (slot == null || !slot.hasItem()) {
                    continue;
                }
                ItemStack updated = slot.getItem().copy();
                updated.setCount(countPerStack);
                changed |= !ItemStack.matches(slot.getItem(), updated);
                slot.set(updated);
                slot.setChanged();
            }

            int index = 0;
            while (remainder > 0 && !slotIds.isEmpty()) {
                Slot slot = resolveMenuSlot(menu, slotIds.get(index));
                if (slot != null && slot.hasItem() && slot.getItem().getCount() < slot.getItem().getMaxStackSize()) {
                    ItemStack updated = slot.getItem().copy();
                    updated.grow(1);
                    changed |= !ItemStack.matches(slot.getItem(), updated);
                    slot.set(updated);
                    slot.setChanged();
                    remainder--;
                }
                index++;
                if (index >= slotIds.size()) {
                    index = 0;
                }
            }
        }

        SlotDebugLog.log("Applied crafting tool balance action: toolId={} changed={}", tool.id(), changed);
        return changed;
    }

    private static boolean rotateGrid(AbstractContainerMenu menu, ExternalToolSpec tool, boolean counterClockwise) {
        List<Integer> inputSlots = tool.menuSlotsForRole(dev.imagio.slot.storage.adapter.ExternalToolSlotRole.INPUT);
        if (inputSlots.size() != 9) {
            return false;
        }

        List<ItemStack> snapshot = new ArrayList<>(inputSlots.size());
        for (int slotId : inputSlots) {
            Slot slot = resolveMenuSlot(menu, slotId);
            snapshot.add(slot == null ? ItemStack.EMPTY : slot.getItem().copy());
        }

        boolean changed = false;
        for (int sourceIndex = 0; sourceIndex < snapshot.size(); sourceIndex++) {
            int targetIndex = sourceIndex == 4 ? 4 : rotateSlotIndex(sourceIndex, counterClockwise);
            Slot slot = resolveMenuSlot(menu, inputSlots.get(targetIndex));
            if (slot == null) {
                continue;
            }

            ItemStack updated = snapshot.get(sourceIndex).copy();
            changed |= !ItemStack.matches(slot.getItem(), updated);
            slot.set(updated);
            slot.setChanged();
        }

        SlotDebugLog.log(
                "Applied crafting tool rotate action: toolId={} counterClockwise={} changed={}",
                tool.id(),
                counterClockwise,
                changed
        );
        return changed;
    }

    private static int rotateSlotIndex(int slotIndex, boolean counterClockwise) {
        if (!counterClockwise) {
            return switch (slotIndex) {
                case 0 -> 1;
                case 1 -> 2;
                case 2 -> 5;
                case 3 -> 0;
                case 5 -> 8;
                case 6 -> 3;
                case 7 -> 6;
                case 8 -> 7;
                default -> slotIndex;
            };
        }

        return switch (slotIndex) {
            case 0 -> 3;
            case 1 -> 0;
            case 2 -> 1;
            case 3 -> 6;
            case 5 -> 2;
            case 6 -> 7;
            case 7 -> 8;
            case 8 -> 5;
            default -> slotIndex;
        };
    }

    private static boolean toggleAutoRefill(AbstractContainerMenu menu, ServerPlayer player, ExternalToolSpec tool) {
        InventoryHostDescriptor host = player == null ? null : StorageViewResolver.resolve(null, menu, player.getInventory(), null);
        ToolCapabilityDescriptor descriptor = host == null ? null : host.capabilities().toolById(tool.id());
        boolean current = descriptor != null && descriptor.toggleEnabled(ExternalToolToggleId.AUTO_REFILL);
        boolean applied = host != null
                && host.providerSession() != null
                && host.providerSession().setToolToggle(menu, tool.id(), ExternalToolToggleId.AUTO_REFILL, !current);
        SlotDebugLog.log(
                "Applied crafting tool auto-refill toggle: toolId={} enabled={} applied={}",
                tool.id(),
                !current,
                applied
        );
        return applied;
    }

    private static Slot resolveMenuSlot(AbstractContainerMenu menu, int slotId) {
        if (menu == null || slotId < 0) {
            return null;
        }
        try {
            Slot slot = menu.getSlot(slotId);
            return slot;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static boolean requiresCraftRefresh(ToolActionRequests.Action action) {
        return action == ToolActionRequests.Action.CLEAR_GRID
                || action == ToolActionRequests.Action.BALANCE_GRID
                || action == ToolActionRequests.Action.ROTATE_GRID_CW
                || action == ToolActionRequests.Action.ROTATE_GRID_CCW;
    }

    private static String toolActionSummaryKey() {
        return "slot.screen.action.outcome.tool_action";
    }

    record ToolActionResult(boolean applied, boolean failed, ActionReason reason, String summaryKey) {
        static ToolActionResult confirmed() {
            return new ToolActionResult(true, false, ActionReason.NONE, toolActionSummaryKey());
        }

        static ToolActionResult blocked(ActionReason reason, String summaryKey) {
            return new ToolActionResult(false, false, reason == null ? ActionReason.UNSPECIFIED : reason, summaryKey == null ? "" : summaryKey);
        }

        static ToolActionResult failed(ActionReason reason, String summaryKey) {
            return new ToolActionResult(false, true, reason == null ? ActionReason.INTERNAL_ERROR : reason, summaryKey == null ? "" : summaryKey);
        }
    }
}
