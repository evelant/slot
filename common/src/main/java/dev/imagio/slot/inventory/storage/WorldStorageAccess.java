package dev.imagio.slot.inventory.storage;

import dev.imagio.slot.workflow.domain.ClaimedChest;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
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

        record Display(WorldDisplayStorageKind kind, String dimensionId, int x, int y, int z) implements Target {
            public Display {
                if (kind == null) {
                    throw new IllegalArgumentException("kind must not be null");
                }
                dimensionId = dimensionId == null ? "" : dimensionId;
            }

            public String storageId() {
                return WorldDisplayStorageSource.storageId(kind, dimensionId, x, y, z);
            }
        }
    }

    /** Represents a filled slot or logical entry in a world storage, used by {@link #enumerate}. */
    record SlotContent(int slotIndex, ItemStack stack, int count) {
        public SlotContent(int slotIndex, ItemStack stack) {
            this(slotIndex, stack, stack == null ? 0 : stack.getCount());
        }

        public SlotContent {
            stack = stack == null ? ItemStack.EMPTY : stack;
            count = stack.isEmpty() ? 0 : Math.max(1, count);
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
     * Actor-aware form for storages whose mutation rules need the player
     * context (for example AE2 power/security/action-source checks).
     */
    default ItemStack insert(
            ServerPlayer actor,
            MinecraftServer server,
            Target target,
            ItemStack stack,
            boolean simulate
    ) {
        return insert(server, target, stack, simulate);
    }

    /**
     * Extract up to {@code amount} items from the given slot of the target.
     * Returns what was actually extracted.
     */
    ItemStack extract(MinecraftServer server, Target target, int slotIndex, int amount, boolean simulate);

    /**
     * Actor-aware form for storages whose extraction rules need the player
     * context (for example AE2 power/security/action-source checks).
     */
    default ItemStack extract(
            ServerPlayer actor,
            MinecraftServer server,
            Target target,
            int slotIndex,
            int amount,
            boolean simulate
    ) {
        return extract(server, target, slotIndex, amount, simulate);
    }

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
     * Returns live nearby item-display blocks that should appear as browseable
     * storage ghosts. Default is empty so loaders opt in explicitly.
     */
    default List<WorldDisplayStorageSource> proximateDisplaySources(ServerPlayer player, int radiusBlocks) {
        return List.of();
    }

    /**
     * Adapter that extends {@link WorldStorageAccess} to support storage kinds
     * without a direct block-inventory capability (virtual / aggregated).
     * The platform impl walks registered delegates before falling back to
     * the standard capability path. Order of registration = priority.
     */
    interface Delegate {
        boolean matches(Target target);

        default Optional<ItemStack> insert(MinecraftServer server, Target target, ItemStack stack, boolean simulate) {
            return Optional.empty();
        }

        default Optional<ItemStack> insert(
                ServerPlayer actor,
                MinecraftServer server,
                Target target,
                ItemStack stack,
                boolean simulate
        ) {
            return insert(server, target, stack, simulate);
        }

        default Optional<ItemStack> extract(
                MinecraftServer server,
                Target target,
                int slotIndex,
                int amount,
                boolean simulate
        ) {
            return Optional.empty();
        }

        default Optional<ItemStack> extract(
                ServerPlayer actor,
                MinecraftServer server,
                Target target,
                int slotIndex,
                int amount,
                boolean simulate
        ) {
            return extract(server, target, slotIndex, amount, simulate);
        }

        default Optional<List<SlotContent>> enumerate(MinecraftServer server, Target target) {
            return Optional.empty();
        }

        default Optional<Integer> slotCount(MinecraftServer server, Target target) {
            return Optional.empty();
        }

        default List<WorldDisplayStorageSource> proximateDisplaySources(ServerPlayer player, int radiusBlocks) {
            return List.of();
        }
    }

    void registerDelegate(Delegate delegate);
}
