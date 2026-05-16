package dev.imagio.slot.inventory.workspace;

/**
 * Shared policy for deciding whether a world inventory is allowed to teach
 * persistent storage affinity.
 *
 * <p>Small processing/station inventories often accept items for a temporary
 * workflow step (fuel, cooling, molds, machine inputs). Those moves are valid
 * inventory actions, but they are not durable "this item lives here" evidence.
 */
public final class StorageAffinityPolicy {
    public static final int DEFAULT_MIN_SLOT_COUNT = 6;

    private StorageAffinityPolicy() {
    }

    public static boolean isEligibleSlotCount(int slotCount) {
        return slotCount >= DEFAULT_MIN_SLOT_COUNT;
    }

    public static boolean isEligible(int slotCount, boolean allowTagged, boolean denyTagged) {
        if (denyTagged) {
            return false;
        }
        if (allowTagged) {
            return true;
        }
        return isEligibleSlotCount(slotCount);
    }
}
