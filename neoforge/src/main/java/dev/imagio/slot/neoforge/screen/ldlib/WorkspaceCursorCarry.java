package dev.imagio.slot.neoforge.screen.ldlib;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.neoforge.screen.ldlib.util.Observable;
import net.minecraft.world.item.ItemStack;

/**
 * Client-side virtual cursor for split-stack moves. The cursor "carries" a
 * count of an identity but never holds a real {@link ItemStack} — drops
 * resolve to existing transfer RPCs that move the actual items from
 * {@link #origin()} to the drop target. ctrl+right-click on a source
 * adds half of that source's current count to the cursor (cumulative on
 * repeat). ESC, or any click that lands outside a valid drop target,
 * clears the cursor.
 *
 * <p>Origins are intentionally narrow: only carried sources (hotbar, main,
 * backpack slots). Atlas cards and chest chips don't form an origin —
 * they're identity / location handles, not slot handles. A drop on those
 * surfaces extracts from the recorded slot origin.
 */
final class WorkspaceCursorCarry {
    private final Observable<State> state = new Observable<>(null);

    Observable<State> state() {
        return state;
    }

    State current() {
        return state.get();
    }

    boolean isEmpty() {
        State s = state.get();
        return s == null || s.count() <= 0;
    }

    boolean isCarrying() {
        return !isEmpty();
    }

    /**
     * Pick up half of the source's current count. If cursor already carries
     * the same identity from the same origin slot, halve the *remaining*
     * source count and add that to the cursor (cumulative successive
     * halvings, mirroring vanilla right-click). Returns true if anything
     * was picked up.
     *
     * <p>Refuses if cursor carries a different identity or a different
     * origin — the player must drop or cancel first.
     */
    boolean pickupHalf(
            String sourceId,
            int slotIndex,
            SlotWorkspaceViewModel.IdentityRef identity,
            ItemStack stack,
            int sourceCount
    ) {
        if (sourceId == null || sourceId.isBlank() || slotIndex < 0
                || identity == null || stack == null || stack.isEmpty()
                || sourceCount <= 0) {
            return false;
        }
        State existing = state.get();
        int alreadyTaken = 0;
        if (existing != null) {
            if (!existing.identity().equals(identity)
                    || !sourceId.equals(existing.sourceId())
                    || slotIndex != existing.slotIndex()) {
                return false;
            }
            alreadyTaken = existing.count();
        }
        int remaining = Math.max(0, sourceCount - alreadyTaken);
        if (remaining <= 0) {
            return false;
        }
        int half = (remaining + 1) / 2;
        int newCount = alreadyTaken + half;
        state.set(new State(sourceId, slotIndex, identity, stack.copy(), newCount));
        return true;
    }

    /** Reduce the cursor count by {@code amount}; clear if it reaches zero. */
    void consume(int amount) {
        State s = state.get();
        if (s == null || amount <= 0) {
            return;
        }
        int remaining = s.count() - amount;
        if (remaining <= 0) {
            state.set(null);
        } else {
            state.set(s.withCount(remaining));
        }
    }

    void clear() {
        state.set(null);
    }

    int dropCount(DropMode mode) {
        State s = state.get();
        if (s == null) {
            return 0;
        }
        return switch (mode) {
            case ALL -> s.count();
            case ONE -> 1;
            case HALF -> Math.max(1, (s.count() + 1) / 2);
        };
    }

    enum DropMode { ALL, ONE, HALF }

    /**
     * Cursor carry snapshot. {@code displayStack} is a copy of the source's
     * stack (with unchanged item) used for rendering the ghost overlay;
     * {@code count} is the virtual cursor count.
     */
    record State(
            String sourceId,
            int slotIndex,
            SlotWorkspaceViewModel.IdentityRef identity,
            ItemStack displayStack,
            int count
    ) {
        State withCount(int newCount) {
            return new State(sourceId, slotIndex, identity, displayStack, Math.max(0, newCount));
        }
    }
}
