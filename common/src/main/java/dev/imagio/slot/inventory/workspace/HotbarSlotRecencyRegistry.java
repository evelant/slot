package dev.imagio.slot.inventory.workspace;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Player-scoped recency memory for automatic hotbar eviction.
 */
public final class HotbarSlotRecencyRegistry {
    private static final ConcurrentMap<UUID, HotbarSlotRecencyTracker> TRACKERS = new ConcurrentHashMap<>();

    private HotbarSlotRecencyRegistry() {
    }

    public static Map<Integer, Long> recencySequence(ServerPlayer player) {
        HotbarSlotRecencyTracker tracker = tracker(player);
        return tracker == null ? Map.of() : tracker.recencySequence();
    }

    public static void observe(ServerPlayer player, SlotWorkspaceViewModel viewModel) {
        HotbarSlotRecencyTracker tracker = tracker(player);
        if (tracker != null) {
            tracker.observe(viewModel);
        }
    }

    public static void observePlayerHotbar(ServerPlayer player) {
        HotbarSlotRecencyTracker tracker = tracker(player);
        if (tracker == null) {
            return;
        }
        Inventory inventory = player.getInventory();
        ArrayList<HotbarSlotRecencyTracker.ObservedHotbarSlot> slots = new ArrayList<>(9);
        for (int index = 0; index < 9; index++) {
            ItemStack stack = index < inventory.items.size() ? inventory.items.get(index) : ItemStack.EMPTY;
            slots.add(new HotbarSlotRecencyTracker.ObservedHotbarSlot(
                    index,
                    index == inventory.selected,
                    stack != null && !stack.isEmpty(),
                    stack,
                    stack == null ? 0 : stack.getCount()));
        }
        tracker.observeHotbarSlots(slots);
    }

    public static void recordMainHandUse(ServerPlayer player) {
        HotbarSlotRecencyTracker tracker = tracker(player);
        if (tracker == null) {
            return;
        }
        Inventory inventory = player.getInventory();
        int selected = inventory.selected;
        if (selected < 0 || selected >= 9 || selected >= inventory.items.size()) {
            return;
        }
        ItemStack stack = inventory.items.get(selected);
        if (stack == null || stack.isEmpty()) {
            return;
        }
        tracker.recordUse(selected);
    }

    public static void recordPlacementOnSuccess(
            ServerPlayer player,
            int hotbarIndex,
            WorkspaceCommandOutcome outcome
    ) {
        HotbarSlotRecencyTracker tracker = tracker(player);
        if (tracker != null) {
            tracker.recordPlacementOnSuccess(hotbarIndex, outcome);
        }
    }

    public static void forget(ServerPlayer player) {
        if (player != null) {
            forget(player.getUUID());
        }
    }

    public static void forget(UUID playerId) {
        if (playerId != null) {
            TRACKERS.remove(playerId);
        }
    }

    public static void clear() {
        TRACKERS.clear();
    }

    private static HotbarSlotRecencyTracker tracker(ServerPlayer player) {
        if (player == null) {
            return null;
        }
        return TRACKERS.computeIfAbsent(player.getUUID(), ignored -> new HotbarSlotRecencyTracker());
    }
}
