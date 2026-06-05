package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Session-local recency model for automatic hotbar assignment.
 *
 * <p>The tracker learns from the projected hotbar rather than only SLOT-driven
 * placements, so manually moved or recently used hotbar items are not treated
 * as stale just because this UI did not place them.
 */
public final class HotbarSlotRecencyTracker {
    private final Map<Integer, Long> recencySequence = new HashMap<>();
    private final Map<Integer, String> fingerprints = new HashMap<>();
    private long nextSequence = 1L;

    public synchronized Map<Integer, Long> recencySequence() {
        return Map.copyOf(recencySequence);
    }

    public void observe(SlotWorkspaceViewModel viewModel) {
        if (viewModel == null) {
            return;
        }
        ArrayList<ObservedHotbarSlot> observed = new ArrayList<>(viewModel.hotbarSlots().size());
        for (SlotWorkspaceViewModel.HotbarSlot slot : viewModel.hotbarSlots()) {
            if (slot == null) {
                continue;
            }
            observed.add(new ObservedHotbarSlot(
                    slot.hotbarIndex(),
                    slot.selected(),
                    slot.occupied(),
                    slot.displayStack(),
                    slot.count()));
        }
        observeHotbarSlots(observed);
    }

    public synchronized void observeHotbarSlots(List<ObservedHotbarSlot> slots) {
        if (slots == null) {
            return;
        }
        for (ObservedHotbarSlot slot : slots) {
            if (slot == null) {
                continue;
            }
            int index = slot.hotbarIndex();
            if (index < 0 || index >= 9) {
                continue;
            }
            String fingerprint = slot.occupied() ? fingerprint(slot.displayStack(), slot.count()) : "";
            boolean knownSlot = fingerprints.containsKey(index);
            String previous = fingerprints.put(index, fingerprint);
            if (!slot.occupied()) {
                recencySequence.remove(index);
                continue;
            }
            if (knownSlot && !Objects.equals(previous, fingerprint)) {
                record(index);
            }
            if (slot.selected()) {
                record(index);
            }
        }
    }

    public synchronized void recordUse(int hotbarIndex) {
        record(hotbarIndex);
    }

    public synchronized void recordPlacementOnSuccess(int hotbarIndex, WorkspaceCommandOutcome outcome) {
        if (outcome == null || !outcome.success()) {
            return;
        }
        record(hotbarIndex);
    }

    private void record(int hotbarIndex) {
        if (hotbarIndex < 0 || hotbarIndex >= 9) {
            return;
        }
        recencySequence.put(hotbarIndex, nextSequence++);
    }

    private static String fingerprint(ItemStack stack, int count) {
        if (stack == null || stack.isEmpty()) {
            return "";
        }
        ItemIdentity identity = ItemIdentityMatcher.create(stack);
        return identity.itemId()
                + "|" + identity.comparisonMode()
                + "|" + identity.componentFingerprint()
                + "|" + Math.max(0, count);
    }

    public record ObservedHotbarSlot(
            int hotbarIndex,
            boolean selected,
            boolean occupied,
            ItemStack displayStack,
            int count
    ) {
        public ObservedHotbarSlot {
            displayStack = displayStack == null ? ItemStack.EMPTY : displayStack.copy();
            count = Math.max(0, count);
            occupied = occupied && !displayStack.isEmpty();
        }
    }
}
