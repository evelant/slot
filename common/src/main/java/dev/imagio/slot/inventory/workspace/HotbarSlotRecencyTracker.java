package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
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
    private final Map<Integer, Long> placementSequence = new HashMap<>();
    private final Map<Integer, String> fingerprints = new HashMap<>();
    private long nextSequence = 1L;

    public Map<Integer, Long> placementSequence() {
        return Map.copyOf(placementSequence);
    }

    public void observe(SlotWorkspaceViewModel viewModel) {
        if (viewModel == null) {
            return;
        }
        for (SlotWorkspaceViewModel.HotbarSlot slot : viewModel.hotbarSlots()) {
            if (slot == null) {
                continue;
            }
            int index = slot.hotbarIndex();
            if (!slot.occupied()) {
                placementSequence.remove(index);
                fingerprints.remove(index);
                continue;
            }
            String fingerprint = fingerprint(slot);
            String previous = fingerprints.put(index, fingerprint);
            if (!Objects.equals(previous, fingerprint) || !placementSequence.containsKey(index)) {
                record(index);
            }
            if (slot.selected()) {
                record(index);
            }
        }
    }

    public void recordPlacementOnSuccess(int hotbarIndex, WorkspaceCommandOutcome outcome) {
        if (outcome == null || !outcome.success()) {
            return;
        }
        record(hotbarIndex);
    }

    private void record(int hotbarIndex) {
        if (hotbarIndex < 0 || hotbarIndex >= 9) {
            return;
        }
        placementSequence.put(hotbarIndex, nextSequence++);
    }

    private static String fingerprint(SlotWorkspaceViewModel.HotbarSlot slot) {
        ItemStack stack = slot.displayStack();
        if (stack == null || stack.isEmpty()) {
            return "";
        }
        ItemIdentity identity = ItemIdentityMatcher.create(stack);
        return identity.itemId()
                + "|" + identity.comparisonMode()
                + "|" + identity.componentFingerprint()
                + "|" + slot.count();
    }
}
