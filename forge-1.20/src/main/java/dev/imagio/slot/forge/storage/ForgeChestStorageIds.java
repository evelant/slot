package dev.imagio.slot.forge.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;
import java.util.UUID;

public final class ForgeChestStorageIds {
    private static final String STORAGE_ID_KEY = "slot_storage_id";

    private ForgeChestStorageIds() {
    }

    public static Optional<UUID> read(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return Optional.empty();
        }
        return read(level.getBlockEntity(pos));
    }

    public static Optional<UUID> read(BlockEntity blockEntity) {
        if (blockEntity == null) {
            return Optional.empty();
        }
        CompoundTag data = blockEntity.getPersistentData();
        if (!data.hasUUID(STORAGE_ID_KEY)) {
            return Optional.empty();
        }
        return Optional.of(data.getUUID(STORAGE_ID_KEY));
    }

    public static boolean write(Level level, BlockPos pos, UUID storageId) {
        if (level == null || pos == null || storageId == null) {
            return false;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return false;
        }
        blockEntity.getPersistentData().putUUID(STORAGE_ID_KEY, storageId);
        blockEntity.setChanged();
        return true;
    }

    public static boolean clear(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return false;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return false;
        }
        CompoundTag data = blockEntity.getPersistentData();
        if (!data.contains(STORAGE_ID_KEY)) {
            return false;
        }
        data.remove(STORAGE_ID_KEY);
        blockEntity.setChanged();
        return true;
    }
}
