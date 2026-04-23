package dev.imagio.slot.neoforge.storage;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.inventory.storage.WorldStorageAccess;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * NeoForge implementation of {@link WorldStorageAccess}. Default resolution
 * chain:
 *
 * <ol>
 *   <li>Registered {@link Delegate}s in registration order — lets mod-specific
 *       adapters (AE2 virtual inventories, storage networks, etc.) handle
 *       targets the capability system can't represent directly.
 *   <li>Vanilla {@code Capabilities.ItemHandler.BLOCK} on the claimed chest's
 *       loaded anchor. Handles every block-bound inventory that follows
 *       NeoForge's conventions: vanilla chests / barrels / shulker boxes /
 *       hoppers, Sophisticated Storage barrels, Create item_vault, Tom's
 *       Storage chests, etc.
 * </ol>
 */
public final class NeoForgeWorldStorageAccess implements WorldStorageAccess {
    private final List<Delegate> delegates = new CopyOnWriteArrayList<>();

    @Override
    public void registerDelegate(Delegate delegate) {
        if (delegate == null) {
            return;
        }
        delegates.add(delegate);
    }

    @Override
    public ItemStack insert(MinecraftServer server, Target target, ItemStack stack, boolean simulate) {
        if (server == null || target == null || stack == null || stack.isEmpty()) {
            return stack == null ? ItemStack.EMPTY : stack;
        }
        for (Delegate delegate : delegates) {
            if (!delegate.matches(target)) {
                continue;
            }
            Optional<ItemStack> handled = delegate.insert(server, target, stack, simulate);
            if (handled.isPresent()) {
                return handled.get();
            }
        }
        IItemHandler handler = resolveHandler(server, target);
        if (handler == null) {
            return stack;
        }
        try {
            ItemStack remaining = ItemHandlerHelper.insertItemStacked(handler, stack.copy(), simulate);
            return remaining == null ? ItemStack.EMPTY : remaining;
        } catch (RuntimeException | LinkageError ex) {
            SlotCommon.LOGGER.warn("[SLOT] WorldStorageAccess.insert failed: {}", ex.getMessage());
            return stack;
        }
    }

    @Override
    public ItemStack extract(MinecraftServer server, Target target, int slotIndex, int amount, boolean simulate) {
        if (server == null || target == null || amount <= 0) {
            return ItemStack.EMPTY;
        }
        for (Delegate delegate : delegates) {
            if (!delegate.matches(target)) {
                continue;
            }
            Optional<ItemStack> handled = delegate.extract(server, target, slotIndex, amount, simulate);
            if (handled.isPresent()) {
                return handled.get();
            }
        }
        IItemHandler handler = resolveHandler(server, target);
        if (handler == null || slotIndex < 0 || slotIndex >= handler.getSlots()) {
            return ItemStack.EMPTY;
        }
        try {
            ItemStack extracted = handler.extractItem(slotIndex, amount, simulate);
            return extracted == null ? ItemStack.EMPTY : extracted;
        } catch (RuntimeException | LinkageError ignored) {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public List<SlotContent> enumerate(MinecraftServer server, Target target) {
        if (server == null || target == null) {
            return List.of();
        }
        for (Delegate delegate : delegates) {
            if (!delegate.matches(target)) {
                continue;
            }
            Optional<List<SlotContent>> handled = delegate.enumerate(server, target);
            if (handled.isPresent()) {
                return handled.get();
            }
        }
        IItemHandler handler = resolveHandler(server, target);
        if (handler == null) {
            return List.of();
        }
        ArrayList<SlotContent> contents = new ArrayList<>(handler.getSlots());
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack;
            try {
                stack = handler.getStackInSlot(slot);
            } catch (RuntimeException | LinkageError ignored) {
                continue;
            }
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            contents.add(new SlotContent(slot, stack.copy()));
        }
        return List.copyOf(contents);
    }

    @Override
    public int slotCount(MinecraftServer server, Target target) {
        if (server == null || target == null) {
            return 0;
        }
        for (Delegate delegate : delegates) {
            if (!delegate.matches(target)) {
                continue;
            }
            Optional<Integer> handled = delegate.slotCount(server, target);
            if (handled.isPresent()) {
                return handled.get();
            }
        }
        IItemHandler handler = resolveHandler(server, target);
        return handler == null ? 0 : handler.getSlots();
    }

    @Override
    public boolean isAccessible(MinecraftServer server, Target target) {
        if (server == null || target == null) {
            return false;
        }
        for (Delegate delegate : delegates) {
            if (delegate.matches(target)) {
                return delegate.enumerate(server, target).isPresent()
                        || delegate.slotCount(server, target).isPresent();
            }
        }
        return resolveHandler(server, target) != null;
    }

    private static IItemHandler resolveHandler(MinecraftServer server, Target target) {
        if (target instanceof Target.Chest chestTarget) {
            return resolveChestHandler(server, chestTarget.chest());
        }
        return null;
    }

    private static IItemHandler resolveChestHandler(MinecraftServer server, ClaimedChest chest) {
        if (chest == null || chest.anchors().isEmpty()) {
            return null;
        }
        for (ChestAnchor anchor : chest.anchors()) {
            if (anchor == null || anchor.dimensionId() == null || anchor.dimensionId().isBlank()) {
                continue;
            }
            ServerLevel level = null;
            for (ServerLevel candidate : server.getAllLevels()) {
                if (candidate.dimension().location().toString().equals(anchor.dimensionId())) {
                    level = candidate;
                    break;
                }
            }
            if (level == null) {
                continue;
            }
            BlockPos pos = new BlockPos(anchor.x(), anchor.y(), anchor.z());
            if (!level.isLoaded(pos)) {
                continue;
            }
            IItemHandler handler;
            try {
                handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
            } catch (RuntimeException | LinkageError ignored) {
                continue;
            }
            if (handler != null) {
                return handler;
            }
        }
        return null;
    }
}
