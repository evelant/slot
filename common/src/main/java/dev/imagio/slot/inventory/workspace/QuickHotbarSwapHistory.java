package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.storage.CarriedInventoryRevisions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Player-scoped history for the auto-Tab hotbar swap flow.
 */
public final class QuickHotbarSwapHistory {
    private static final int CAPACITY = 16;
    private static final ConcurrentMap<UUID, History> HISTORIES = new ConcurrentHashMap<>();

    private QuickHotbarSwapHistory() {
    }

    public static void recordSwap(
            ServerPlayer player,
            int hotbarIndex,
            ItemStack before,
            ItemStack after,
            String label
    ) {
        if (player == null || hotbarIndex < 0 || hotbarIndex >= 9) {
            return;
        }
        ItemStack beforeCopy = before == null ? ItemStack.EMPTY : before.copy();
        ItemStack afterCopy = after == null ? ItemStack.EMPTY : after.copy();
        if (ItemStack.matches(beforeCopy, afterCopy)) {
            return;
        }
        history(player).record(new Entry(
                hotbarIndex,
                beforeCopy,
                afterCopy,
                label == null || label.isBlank() ? "quick hotbar swap" : label));
    }

    public static WorkspaceCommandOutcome undo(ServerPlayer player) {
        History history = existingHistory(player);
        if (history == null) {
            return WorkspaceCommandOutcome.rejected("nothing_to_undo_quick_hotbar_swap");
        }
        return history.undo(player);
    }

    public static WorkspaceCommandOutcome redo(ServerPlayer player) {
        History history = existingHistory(player);
        if (history == null) {
            return WorkspaceCommandOutcome.rejected("nothing_to_redo_quick_hotbar_swap");
        }
        return history.redo(player);
    }

    public static void forget(ServerPlayer player) {
        if (player != null) {
            forget(player.getUUID());
        }
    }

    public static void forget(UUID playerId) {
        if (playerId != null) {
            HISTORIES.remove(playerId);
        }
    }

    public static void clear() {
        HISTORIES.clear();
    }

    private static History history(ServerPlayer player) {
        return HISTORIES.computeIfAbsent(player.getUUID(), ignored -> new History());
    }

    private static History existingHistory(ServerPlayer player) {
        return player == null ? null : HISTORIES.get(player.getUUID());
    }

    private static final class History {
        private final Deque<Entry> undoStack = new ArrayDeque<>();
        private final Deque<Entry> redoStack = new ArrayDeque<>();

        synchronized void record(Entry entry) {
            redoStack.clear();
            while (undoStack.size() >= CAPACITY) {
                undoStack.removeFirst();
            }
            undoStack.addLast(entry);
        }

        synchronized WorkspaceCommandOutcome undo(ServerPlayer player) {
            if (undoStack.isEmpty()) {
                return WorkspaceCommandOutcome.rejected("nothing_to_undo_quick_hotbar_swap");
            }
            Entry entry = undoStack.peekLast();
            ItemStack currentAfter = WorkspaceHotbarSlotReverser.peekSlot(player, entry.hotbarIndex());
            if (!slotStillRepresents(currentAfter, entry.after())) {
                return WorkspaceCommandOutcome.rejected("quick_hotbar_swap_slot_changed");
            }
            undoStack.removeLast();
            WorkspaceHotbarSlotReverser.restoreSlot(player, entry.hotbarIndex(), entry.before());
            redoStack.addLast(new Entry(entry.hotbarIndex(), entry.before(), currentAfter, entry.label()));
            markChanged(player, "quick_hotbar_swap_undo");
            return acceptedSwapOutcome(
                    "quick_hotbar_swap_undone",
                    entry.label(),
                    "quick_hotbar_swap_undo",
                    entry.before(),
                    currentAfter);
        }

        synchronized WorkspaceCommandOutcome redo(ServerPlayer player) {
            if (redoStack.isEmpty()) {
                return WorkspaceCommandOutcome.rejected("nothing_to_redo_quick_hotbar_swap");
            }
            Entry entry = redoStack.peekLast();
            ItemStack currentBefore = WorkspaceHotbarSlotReverser.peekSlot(player, entry.hotbarIndex());
            if (!slotStillRepresents(currentBefore, entry.before())) {
                return WorkspaceCommandOutcome.rejected("quick_hotbar_swap_slot_changed");
            }
            redoStack.removeLast();
            WorkspaceHotbarSlotReverser.restoreSlot(player, entry.hotbarIndex(), entry.after());
            undoStack.addLast(new Entry(entry.hotbarIndex(), currentBefore, entry.after(), entry.label()));
            markChanged(player, "quick_hotbar_swap_redo");
            return acceptedSwapOutcome(
                    "quick_hotbar_swap_redone",
                    entry.label(),
                    "quick_hotbar_swap_redo",
                    currentBefore,
                    entry.after());
        }

        private boolean slotStillRepresents(ItemStack current, ItemStack expected) {
            if (current == null || current.isEmpty()) {
                return true;
            }
            if (expected == null || expected.isEmpty()) {
                return false;
            }
            return ItemIdentityMatcher.matchesMovable(current, ItemIdentityMatcher.create(expected));
        }

        private void markChanged(ServerPlayer player, String reason) {
            CarriedInventoryRevisions.markChanged(player, reason);
            HotbarSlotRecencyRegistry.observePlayerHotbar(player);
        }
    }

    private record Entry(
            int hotbarIndex,
            ItemStack before,
            ItemStack after,
            String label
    ) {
        private Entry {
            before = before == null ? ItemStack.EMPTY : before.copy();
            after = after == null ? ItemStack.EMPTY : after.copy();
            label = label == null ? "" : label;
        }
    }

    static WorkspaceCommandOutcome acceptedSwapOutcome(
            String status,
            String label,
            String diagnostics,
            ItemStack before,
            ItemStack after
    ) {
        return WorkspaceBeltCommandService.withCarriedIdentityInvalidation(
                WorkspaceCommandOutcome.accepted(status, label),
                diagnostics,
                stackIdentity(before),
                stackIdentity(after));
    }

    private static ItemIdentity stackIdentity(ItemStack stack) {
        return stack == null || stack.isEmpty()
                ? null
                : ItemIdentityMatcher.normalizeMovable(ItemIdentityMatcher.create(stack));
    }
}
