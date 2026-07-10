package dev.imagio.slot.forge.storage;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.inventory.storage.WorldDisplayStorageKind;
import dev.imagio.slot.inventory.storage.WorldDisplayStorageSource;
import dev.imagio.slot.inventory.storage.WorldStorageAccess;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ForgeWorldStorageAccess implements WorldStorageAccess {
    private final List<Delegate> delegates = new CopyOnWriteArrayList<>();

    @Override
    public void registerDelegate(Delegate delegate) {
        if (delegate != null) {
            delegates.add(delegate);
        }
    }

    @Override
    public ItemStack insert(MinecraftServer server, Target target, ItemStack stack, boolean simulate) {
        return insert(null, server, target, stack, simulate);
    }

    @Override
    public ItemStack insert(ServerPlayer actor, MinecraftServer server, Target target, ItemStack stack, boolean simulate) {
        if (server == null || target == null || stack == null || stack.isEmpty()) {
            return stack == null ? ItemStack.EMPTY : stack;
        }
        for (Delegate delegate : delegates) {
            if (!delegate.matches(target)) {
                continue;
            }
            Optional<ItemStack> handled = delegate.insert(actor, server, target, stack, simulate);
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
        } catch (RuntimeException | LinkageError exception) {
            SlotCommon.LOGGER.warn("[SLOT] Forge WorldStorageAccess.insert failed: {}", exception.getMessage());
            return stack;
        }
    }

    @Override
    public ItemStack extract(MinecraftServer server, Target target, int slotIndex, int amount, boolean simulate) {
        return extract(null, server, target, slotIndex, amount, simulate);
    }

    @Override
    public ItemStack extract(
            ServerPlayer actor,
            MinecraftServer server,
            Target target,
            int slotIndex,
            int amount,
            boolean simulate
    ) {
        if (server == null || target == null || amount <= 0) {
            return ItemStack.EMPTY;
        }
        for (Delegate delegate : delegates) {
            if (!delegate.matches(target)) {
                continue;
            }
            Optional<ItemStack> handled = delegate.extract(actor, server, target, slotIndex, amount, simulate);
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
        if (target instanceof Target.Display display && display.kind() == WorldDisplayStorageKind.FLUID_TANK) {
            return displayBlockContents(server, display);
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
    public List<FluidContent> enumerateFluids(MinecraftServer server, Target target) {
        if (server == null || target == null) {
            return List.of();
        }
        for (Delegate delegate : delegates) {
            if (!delegate.matches(target)) {
                continue;
            }
            Optional<List<FluidContent>> handled = delegate.enumerateFluids(server, target);
            if (handled.isPresent()) {
                return handled.get();
            }
        }

        ArrayList<FluidContent> contents = new ArrayList<>();
        IFluidHandler directTank = resolveFluidHandler(server, target);
        if (directTank != null) {
            contents.addAll(ForgeFluidContents.directTankContents(directTank));
        }
        for (SlotContent content : enumerate(server, target)) {
            if (content == null || content.stack().isEmpty()) {
                continue;
            }
            contents.addAll(ForgeFluidContents.itemContainerContents(content.slotIndex(), content.stack()));
        }
        return contents.isEmpty() ? List.of() : List.copyOf(contents);
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
        if (handler != null) {
            return handler.getSlots();
        }
        if (target instanceof Target.Display display && display.kind() == WorldDisplayStorageKind.FLUID_TANK) {
            return displayBlockStack(server, display).isEmpty() ? 0 : 1;
        }
        return 0;
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
        if (target instanceof Target.Display display && display.kind() == WorldDisplayStorageKind.FLUID_TANK) {
            return resolveDisplayFluidHandler(server, display) != null;
        }
        return resolveHandler(server, target) != null || resolveFluidHandler(server, target) != null;
    }

    @Override
    public List<WorldDisplayStorageSource> proximateDisplaySources(ServerPlayer player, int radiusBlocks) {
        if (player == null) {
            return List.of();
        }
        ArrayList<WorldDisplayStorageSource> sources = new ArrayList<>();
        sources.addAll(proximateFluidTankSources(player, radiusBlocks));
        for (Delegate delegate : delegates) {
            List<WorldDisplayStorageSource> delegated = delegate.proximateDisplaySources(player, radiusBlocks);
            if (delegated != null && !delegated.isEmpty()) {
                sources.addAll(delegated);
            }
        }
        return sources.isEmpty() ? List.of() : List.copyOf(sources);
    }

    private static IItemHandler resolveHandler(MinecraftServer server, Target target) {
        if (target instanceof Target.Chest chestTarget) {
            return resolveChestHandler(server, chestTarget.chest());
        }
        return null;
    }

    private static IFluidHandler resolveFluidHandler(MinecraftServer server, Target target) {
        if (target instanceof Target.Chest chestTarget) {
            return resolveChestFluidHandler(server, chestTarget.chest());
        }
        if (target instanceof Target.Display display && display.kind() == WorldDisplayStorageKind.FLUID_TANK) {
            return resolveDisplayFluidHandler(server, display);
        }
        return null;
    }

    private static List<WorldDisplayStorageSource> proximateFluidTankSources(ServerPlayer player, int radiusBlocks) {
        if (player == null || player.getServer() == null) {
            return List.of();
        }
        int radius = Math.max(0, radiusBlocks);
        ServerLevel level = player.serverLevel();
        String dimension = level.dimension().location().toString();
        BlockPos center = player.blockPosition();
        long radiusSquared = (long) radius * radius;
        ArrayList<WorldDisplayStorageSource> sources = new ArrayList<>();
        for (BlockPos cursor : BlockPos.betweenClosed(
                center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius))) {
            long dx = (long) cursor.getX() - center.getX();
            long dy = (long) cursor.getY() - center.getY();
            long dz = (long) cursor.getZ() - center.getZ();
            if (dx * dx + dy * dy + dz * dz > radiusSquared || !level.isLoaded(cursor)) {
                continue;
            }
            IFluidHandler handler = fluidHandlerAt(level, cursor);
            if (handler == null) {
                continue;
            }
            List<FluidContent> fluids = ForgeFluidContents.directTankContents(handler);
            ItemStack blockStack = blockItemStack(level.getBlockState(cursor));
            if (fluids.isEmpty() && blockStack.isEmpty()) {
                continue;
            }
            List<SlotContent> contents = blockStack.isEmpty()
                    ? List.of()
                    : List.of(new SlotContent(0, blockStack, 1));
            sources.add(new WorldDisplayStorageSource(
                    WorldDisplayStorageSource.storageId(
                            WorldDisplayStorageKind.FLUID_TANK,
                            dimension,
                            cursor.getX(),
                            cursor.getY(),
                            cursor.getZ()),
                    WorldDisplayStorageKind.FLUID_TANK,
                    fluidTankLabel(blockStack, cursor),
                    dimension,
                    cursor.getX(),
                    cursor.getY(),
                    cursor.getZ(),
                    contents.isEmpty() ? 0 : 1,
                    contents,
                    fluids,
                    List.of()));
        }
        return sources.isEmpty() ? List.of() : List.copyOf(sources);
    }

    private static List<SlotContent> displayBlockContents(MinecraftServer server, Target.Display display) {
        ItemStack stack = displayBlockStack(server, display);
        return stack.isEmpty() ? List.of() : List.of(new SlotContent(0, stack, 1));
    }

    private static ItemStack displayBlockStack(MinecraftServer server, Target.Display display) {
        if (server == null || display == null || display.kind() != WorldDisplayStorageKind.FLUID_TANK) {
            return ItemStack.EMPTY;
        }
        ServerLevel level = level(server, display.dimensionId());
        if (level == null) {
            return ItemStack.EMPTY;
        }
        BlockPos pos = new BlockPos(display.x(), display.y(), display.z());
        if (!level.isLoaded(pos)) {
            return ItemStack.EMPTY;
        }
        return blockItemStack(level.getBlockState(pos));
    }

    private static ItemStack blockItemStack(BlockState state) {
        if (state == null || state.isAir()) {
            return ItemStack.EMPTY;
        }
        Item item = state.getBlock().asItem();
        if (item == null || item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item);
    }

    private static String fluidTankLabel(ItemStack stack, BlockPos pos) {
        if (stack != null && !stack.isEmpty()) {
            String name = stack.getHoverName().getString();
            if (name != null && !name.isBlank()) {
                return name;
            }
        }
        return "Fluid tank at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
    }

    private static IFluidHandler resolveDisplayFluidHandler(MinecraftServer server, Target.Display display) {
        if (server == null || display == null || display.kind() != WorldDisplayStorageKind.FLUID_TANK) {
            return null;
        }
        ServerLevel level = level(server, display.dimensionId());
        if (level == null) {
            return null;
        }
        BlockPos pos = new BlockPos(display.x(), display.y(), display.z());
        if (!level.isLoaded(pos)) {
            return null;
        }
        return fluidHandlerAt(level, pos);
    }

    private static IFluidHandler fluidHandlerAt(ServerLevel level, BlockPos pos) {
        if (level == null || pos == null) {
            return null;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return null;
        }
        try {
            LazyOptional<IFluidHandler> optional = blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, null);
            return optional.orElse(null);
        } catch (RuntimeException | LinkageError ignored) {
            return null;
        }
    }

    private static IItemHandler resolveChestHandler(MinecraftServer server, ClaimedChest chest) {
        if (chest == null || chest.anchors().isEmpty()) {
            return null;
        }
        for (ChestAnchor anchor : chest.anchors()) {
            if (anchor == null || anchor.dimensionId() == null || anchor.dimensionId().isBlank()) {
                continue;
            }
            ServerLevel level = level(server, anchor.dimensionId());
            if (level == null) {
                continue;
            }
            BlockPos pos = new BlockPos(anchor.x(), anchor.y(), anchor.z());
            if (!level.isLoaded(pos)) {
                continue;
            }
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity == null) {
                continue;
            }
            try {
                LazyOptional<IItemHandler> optional = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
                IItemHandler handler = optional.orElse(null);
                if (handler != null) {
                    return handler;
                }
            } catch (RuntimeException | LinkageError ignored) {
            }
        }
        return null;
    }

    private static IFluidHandler resolveChestFluidHandler(MinecraftServer server, ClaimedChest chest) {
        if (chest == null || chest.anchors().isEmpty()) {
            return null;
        }
        for (ChestAnchor anchor : chest.anchors()) {
            if (anchor == null || anchor.dimensionId() == null || anchor.dimensionId().isBlank()) {
                continue;
            }
            ServerLevel level = level(server, anchor.dimensionId());
            if (level == null) {
                continue;
            }
            BlockPos pos = new BlockPos(anchor.x(), anchor.y(), anchor.z());
            if (!level.isLoaded(pos)) {
                continue;
            }
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity == null) {
                continue;
            }
            try {
                LazyOptional<IFluidHandler> optional = blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, null);
                IFluidHandler handler = optional.orElse(null);
                if (handler != null) {
                    return handler;
                }
            } catch (RuntimeException | LinkageError ignored) {
            }
        }
        return null;
    }

    private static ServerLevel level(MinecraftServer server, String dimensionId) {
        for (ServerLevel candidate : server.getAllLevels()) {
            if (candidate.dimension().location().toString().equals(dimensionId)) {
                return candidate;
            }
        }
        return null;
    }
}
