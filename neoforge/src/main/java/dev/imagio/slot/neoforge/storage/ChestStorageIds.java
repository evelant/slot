package dev.imagio.slot.neoforge.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;
import java.util.UUID;

public final class ChestStorageIds {
    private ChestStorageIds() {
    }

    public static Optional<UUID> read(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return Optional.empty();
        }
        return read(level.getBlockEntity(pos));
    }

    public static Optional<UUID> read(BlockEntity be) {
        if (be == null) {
            return Optional.empty();
        }
        if (!be.hasData(SlotAttachmentTypes.STORAGE_ID)) {
            return Optional.empty();
        }
        UUID value = be.getData(SlotAttachmentTypes.STORAGE_ID);
        return value == null ? Optional.empty() : Optional.of(value);
    }

    public static boolean write(Level level, BlockPos pos, UUID storageId) {
        if (level == null || pos == null || storageId == null) {
            return false;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) {
            return false;
        }
        be.setData(SlotAttachmentTypes.STORAGE_ID, storageId);
        be.setChanged();
        return true;
    }

    public static boolean clear(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return false;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null || !be.hasData(SlotAttachmentTypes.STORAGE_ID)) {
            return false;
        }
        be.removeData(SlotAttachmentTypes.STORAGE_ID);
        be.setChanged();
        return true;
    }
}
