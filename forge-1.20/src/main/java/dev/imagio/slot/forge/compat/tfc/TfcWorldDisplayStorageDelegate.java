package dev.imagio.slot.forge.compat.tfc;

import dev.imagio.slot.inventory.storage.WorldDisplayStorageKind;
import dev.imagio.slot.inventory.storage.WorldDisplayStorageSource;
import dev.imagio.slot.inventory.storage.WorldStorageAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * TerraFirmaCraft item displays. TFC tool racks and placed-item blocks own
 * inventories but do not expose Forge's block item-handler capability, so
 * SLOT reaches their public inventory through reflection and keeps the rest of
 * the storage pipeline generic.
 */
public final class TfcWorldDisplayStorageDelegate implements WorldStorageAccess.Delegate {
    @Override
    public boolean matches(WorldStorageAccess.Target target) {
        return target instanceof WorldStorageAccess.Target.Display display && supports(display.kind());
    }

    @Override
    public Optional<ItemStack> insert(
            MinecraftServer server,
            WorldStorageAccess.Target target,
            ItemStack stack,
            boolean simulate
    ) {
        if (!(target instanceof WorldStorageAccess.Target.Display display) || stack == null || stack.isEmpty()) {
            return Optional.empty();
        }
        Optional<Object> handler = resolveInventory(server, display);
        return handler.map(value -> insertIntoHandler(value, stack, simulate));
    }

    @Override
    public Optional<ItemStack> extract(
            MinecraftServer server,
            WorldStorageAccess.Target target,
            int slotIndex,
            int amount,
            boolean simulate
    ) {
        if (!(target instanceof WorldStorageAccess.Target.Display display) || amount <= 0) {
            return Optional.empty();
        }
        Optional<Object> handler = resolveInventory(server, display);
        return handler.map(value -> extractFromHandler(value, slotIndex, amount, simulate));
    }

    @Override
    public Optional<List<WorldStorageAccess.SlotContent>> enumerate(
            MinecraftServer server,
            WorldStorageAccess.Target target
    ) {
        if (!(target instanceof WorldStorageAccess.Target.Display display)) {
            return Optional.empty();
        }
        Optional<Object> handler = resolveInventory(server, display);
        return handler.map(TfcWorldDisplayStorageDelegate::enumerateHandler);
    }

    @Override
    public Optional<Integer> slotCount(MinecraftServer server, WorldStorageAccess.Target target) {
        if (!(target instanceof WorldStorageAccess.Target.Display display)) {
            return Optional.empty();
        }
        Optional<Object> handler = resolveInventory(server, display);
        return handler.map(TfcWorldDisplayStorageDelegate::slotCount);
    }

    @Override
    public List<WorldDisplayStorageSource> proximateDisplaySources(ServerPlayer player, int radiusBlocks) {
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
            if (dx * dx + dy * dy + dz * dz > radiusSquared) {
                continue;
            }
            BlockState state = level.getBlockState(cursor);
            WorldDisplayStorageKind kind = kindFor(state);
            if (kind == null) {
                continue;
            }
            BlockEntity blockEntity = level.getBlockEntity(cursor);
            Object handler = inventory(blockEntity).orElse(null);
            if (handler == null) {
                continue;
            }
            int slots = slotCount(handler);
            List<WorldStorageAccess.SlotContent> contents = enumerateHandler(handler);
            if (slots <= 0 || (contents.isEmpty() && !kind.depositTarget())) {
                continue;
            }
            sources.add(new WorldDisplayStorageSource(
                    null,
                    kind,
                    label(kind, cursor),
                    dimension,
                    cursor.getX(),
                    cursor.getY(),
                    cursor.getZ(),
                    slots,
                    contents));
        }
        sources.sort(Comparator
                .comparingLong((WorldDisplayStorageSource source) -> distanceSquared(source, center))
                .thenComparing(WorldDisplayStorageSource::storageId));
        return sources.isEmpty() ? List.of() : List.copyOf(sources);
    }

    private static Optional<Object> resolveInventory(
            MinecraftServer server,
            WorldStorageAccess.Target.Display display
    ) {
        if (server == null || display == null || !supports(display.kind())) {
            return Optional.empty();
        }
        ServerLevel level = level(server, display.dimensionId());
        if (level == null) {
            return Optional.empty();
        }
        BlockPos pos = new BlockPos(display.x(), display.y(), display.z());
        if (!level.isLoaded(pos) || kindFor(level.getBlockState(pos)) != display.kind()) {
            return Optional.empty();
        }
        return inventory(level.getBlockEntity(pos));
    }

    private static ServerLevel level(MinecraftServer server, String dimensionId) {
        if (server == null || dimensionId == null || dimensionId.isBlank()) {
            return null;
        }
        for (ServerLevel level : server.getAllLevels()) {
            if (level.dimension().location().toString().equals(dimensionId)) {
                return level;
            }
        }
        return null;
    }

    private static Optional<Object> inventory(BlockEntity blockEntity) {
        if (blockEntity == null) {
            return Optional.empty();
        }
        try {
            Method method = findMethod(blockEntity.getClass(), "getInventory");
            if (method == null) {
                return Optional.empty();
            }
            method.setAccessible(true);
            Object handler = method.invoke(blockEntity);
            return handler == null ? Optional.empty() : Optional.of(handler);
        } catch (IllegalAccessException | InvocationTargetException | RuntimeException | LinkageError ignored) {
            return Optional.empty();
        }
    }

    private static int slotCount(Object handler) {
        if (handler == null) {
            return 0;
        }
        try {
            Method method = findMethod(handler.getClass(), "getSlots");
            if (method == null) {
                return 0;
            }
            method.setAccessible(true);
            Object result = method.invoke(handler);
            return result instanceof Number number ? Math.max(0, number.intValue()) : 0;
        } catch (IllegalAccessException | InvocationTargetException | RuntimeException | LinkageError ignored) {
            return 0;
        }
    }

    private static List<WorldStorageAccess.SlotContent> enumerateHandler(Object handler) {
        int slots = slotCount(handler);
        if (slots <= 0) {
            return List.of();
        }
        Method method = findMethod(handler.getClass(), "getStackInSlot", int.class);
        if (method == null) {
            return List.of();
        }
        method.setAccessible(true);
        ArrayList<WorldStorageAccess.SlotContent> contents = new ArrayList<>(slots);
        for (int slot = 0; slot < slots; slot++) {
            try {
                Object result = method.invoke(handler, slot);
                if (result instanceof ItemStack stack && !stack.isEmpty()) {
                    contents.add(new WorldStorageAccess.SlotContent(slot, stack.copy()));
                }
            } catch (IllegalAccessException | InvocationTargetException | RuntimeException | LinkageError ignored) {
            }
        }
        return contents.isEmpty() ? List.of() : List.copyOf(contents);
    }

    private static ItemStack insertIntoHandler(Object handler, ItemStack stack, boolean simulate) {
        if (handler == null || stack == null || stack.isEmpty()) {
            return stack == null ? ItemStack.EMPTY : stack;
        }
        Method method = findMethod(handler.getClass(), "insertItem", int.class, ItemStack.class, boolean.class);
        if (method == null) {
            return stack;
        }
        method.setAccessible(true);
        ItemStack remaining = stack.copy();
        int slots = slotCount(handler);
        for (int slot = 0; slot < slots && !remaining.isEmpty(); slot++) {
            try {
                Object result = method.invoke(handler, slot, remaining, simulate);
                remaining = result instanceof ItemStack leftover ? leftover : ItemStack.EMPTY;
            } catch (IllegalAccessException | InvocationTargetException | RuntimeException | LinkageError ignored) {
                return stack;
            }
        }
        return remaining == null ? ItemStack.EMPTY : remaining;
    }

    private static ItemStack extractFromHandler(Object handler, int slotIndex, int amount, boolean simulate) {
        if (handler == null || slotIndex < 0 || amount <= 0 || slotIndex >= slotCount(handler)) {
            return ItemStack.EMPTY;
        }
        Method method = findMethod(handler.getClass(), "extractItem", int.class, int.class, boolean.class);
        if (method == null) {
            return ItemStack.EMPTY;
        }
        method.setAccessible(true);
        try {
            Object result = method.invoke(handler, slotIndex, amount, simulate);
            return result instanceof ItemStack stack ? stack : ItemStack.EMPTY;
        } catch (IllegalAccessException | InvocationTargetException | RuntimeException | LinkageError ignored) {
            return ItemStack.EMPTY;
        }
    }

    private static Method findMethod(Class<?> type, String name, Class<?>... parameters) {
        Class<?> cursor = type;
        while (cursor != null) {
            try {
                return cursor.getDeclaredMethod(name, parameters);
            } catch (NoSuchMethodException ignored) {
                cursor = cursor.getSuperclass();
            }
        }
        try {
            return type.getMethod(name, parameters);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    private static WorldDisplayStorageKind kindFor(BlockState state) {
        if (state == null) {
            return null;
        }
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (id == null || !"tfc".equals(id.getNamespace())) {
            return null;
        }
        String path = id.getPath();
        if ("placed_item".equals(path)) {
            return WorldDisplayStorageKind.PLACED_ITEM;
        }
        if (path.startsWith("wood/tool_rack/")) {
            return WorldDisplayStorageKind.TOOL_RACK;
        }
        return null;
    }

    private static boolean supports(WorldDisplayStorageKind kind) {
        return kind == WorldDisplayStorageKind.TOOL_RACK || kind == WorldDisplayStorageKind.PLACED_ITEM;
    }

    private static String label(WorldDisplayStorageKind kind, BlockPos pos) {
        String base = kind == WorldDisplayStorageKind.TOOL_RACK ? "Tool rack" : "Placed item";
        return base + " @ " + pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    private static long distanceSquared(WorldDisplayStorageSource source, BlockPos center) {
        long dx = (long) source.x() - center.getX();
        long dy = (long) source.y() - center.getY();
        long dz = (long) source.z() - center.getZ();
        return dx * dx + dy * dy + dz * dz;
    }
}
