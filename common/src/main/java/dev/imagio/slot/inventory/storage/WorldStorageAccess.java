package dev.imagio.slot.inventory.storage;

import dev.imagio.slot.workflow.domain.ClaimedChest;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

/**
 * Platform-neutral abstraction for reading and mutating world-block storages
 * — vanilla chests, barrels, shulker boxes, hoppers, dispensers, plus any
 * modded container that exposes the inventory capability on its block
 * (Sophisticated Storage barrels, Create item_vault, Tom's Storage
 * terminal, AE2 terminal). Virtual/aggregated storages (e.g. AE2 networks)
 * plug in via {@link Delegate}.
 *
 * <p>Any code that needs to inspect or mutate a world-block storage MUST go
 * through this interface. Never call
 * {@code level.getCapability(Capabilities.ItemHandler.BLOCK, ...)} directly
 * outside the platform implementation — the dispatch + delegate lookup
 * belongs in one place so adding a new mod-storage provider is an O(1)
 * change (register a delegate) rather than an O(n) change (patch every
 * executor that reads / writes a block).
 *
 * <p>Implementations are installed per platform (see
 * {@link StorageAccessRegistry#installWorldStorageAccess}) and retrieved via
 * {@link StorageAccessRegistry#worldStorageAccess()}.
 */
public interface WorldStorageAccess {

    /**
     * Identifies a world-bound storage. Initially only claimed-chest targets
     * are first-class; raw block positions and virtual storage targets
     * (AE2 networks) extend the set later via {@link Delegate}.
     */
    sealed interface Target {
        record Chest(ClaimedChest chest) implements Target {}
    }

    /** Represents a filled slot in a world storage, used by {@link #enumerate}. */
    record SlotContent(int slotIndex, ItemStack stack) {
        public SlotContent {
            stack = stack == null ? ItemStack.EMPTY : stack;
        }
    }

    /**
     * Simulate: returns the stack portion that would NOT fit. Commit: performs
     * the insertion and returns the remaining portion that could not be
     * placed. Use {@link ItemStack#isEmpty()} on the return value to check
     * "fully inserted".
     */
    ItemStack insert(MinecraftServer server, Target target, ItemStack stack, boolean simulate);

    /**
     * Extract up to {@code amount} items from the given slot of the target.
     * Returns what was actually extracted.
     */
    ItemStack extract(MinecraftServer server, Target target, int slotIndex, int amount, boolean simulate);

    /**
     * Enumerate every filled slot of the target. Used by Take All and by
     * planners that need to inspect a storage's contents.
     */
    List<SlotContent> enumerate(MinecraftServer server, Target target);

    /** Returns the slot capacity of the target, or 0 if unknown / unloaded. */
    int slotCount(MinecraftServer server, Target target);

    /** Returns true when the target is currently reachable (loaded + capability present). */
    boolean isAccessible(MinecraftServer server, Target target);

    /**
     * Adapter that extends {@link WorldStorageAccess} to support storage kinds
     * without a direct block-inventory capability (virtual / aggregated).
     * The platform impl walks registered delegates before falling back to
     * the standard capability path. Order of registration = priority.
     */
    interface Delegate {
        boolean matches(Target target);
        Optional<ItemStack> insert(MinecraftServer server, Target target, ItemStack stack, boolean simulate);
        Optional<ItemStack> extract(MinecraftServer server, Target target, int slotIndex, int amount, boolean simulate);
        Optional<List<SlotContent>> enumerate(MinecraftServer server, Target target);
        Optional<Integer> slotCount(MinecraftServer server, Target target);
    }

    void registerDelegate(Delegate delegate);
}
