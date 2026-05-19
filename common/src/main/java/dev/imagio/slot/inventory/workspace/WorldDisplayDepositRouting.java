package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.storage.WorldDisplayStorageSource;
import dev.imagio.slot.inventory.storage.WorldStorageAccess;

import net.minecraft.world.item.ItemStack;

final class WorldDisplayDepositRouting {
    private WorldDisplayDepositRouting() {
    }

    /**
     * Bulk deposit may use display storage only when the display already
     * contains the same movable identity. Empty tool racks remain available to
     * explicit deposit gestures, but bulk deposit must not infer a new tool
     * home just because a rack can technically accept the stack.
     */
    static boolean containsMatchingContent(WorldDisplayStorageSource source, ItemIdentity identity) {
        if (source == null || identity == null || !source.depositTarget()) {
            return false;
        }
        for (WorldStorageAccess.SlotContent content : source.contents()) {
            if (content == null) {
                continue;
            }
            ItemStack stack = content.stack();
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            if (ItemIdentityMatcher.matchesMovable(stack, identity)) {
                return true;
            }
        }
        return false;
    }
}
