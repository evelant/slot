package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.ClaimedChestMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Shared loaded-chunk scan for the nearest unclaimed storage block near the
 * player. Loader adapters provide only the storage capability predicate and
 * persisted storage-id reader.
 */
public final class LootChestProjectionSupport {
    public static final int DEFAULT_RADIUS_BLOCKS = 8;

    private LootChestProjectionSupport() {
    }

    public static Optional<BlockPos> closest(
            ServerPlayer player,
            ClaimedChestMap claimedMap,
            ClaimableStorageLookup claimableStorageLookup,
            ChestClaimPersistenceReconciliation.StorageIdLookup storageIdLookup
    ) {
        return closest(player, claimedMap, claimableStorageLookup, storageIdLookup, DEFAULT_RADIUS_BLOCKS);
    }

    public static Optional<BlockPos> closest(
            ServerPlayer player,
            ClaimedChestMap claimedMap,
            ClaimableStorageLookup claimableStorageLookup,
            ChestClaimPersistenceReconciliation.StorageIdLookup storageIdLookup,
            int radiusBlocks
    ) {
        if (player == null || claimableStorageLookup == null || storageIdLookup == null) {
            return Optional.empty();
        }
        ServerLevel level = player.serverLevel();
        BlockPos center = player.blockPosition();
        long radiusSquared = (long) radiusBlocks * radiusBlocks;
        int chunkRadius = (radiusBlocks >> 4) + 1;
        int playerChunkX = center.getX() >> 4;
        int playerChunkZ = center.getZ() >> 4;

        BlockPos best = null;
        long bestDistanceSquared = Long.MAX_VALUE;

        for (int dcx = -chunkRadius; dcx <= chunkRadius; dcx++) {
            for (int dcz = -chunkRadius; dcz <= chunkRadius; dcz++) {
                ChunkAccess chunk = level.getChunkSource().getChunkNow(playerChunkX + dcx, playerChunkZ + dcz);
                if (!(chunk instanceof LevelChunk levelChunk)) {
                    continue;
                }
                Map<BlockPos, BlockEntity> blockEntities = levelChunk.getBlockEntities();
                if (blockEntities.isEmpty()) {
                    continue;
                }
                for (BlockEntity blockEntity : blockEntities.values()) {
                    BlockPos pos = blockEntity.getBlockPos();
                    if (!claimableStorageLookup.isClaimable(level, pos)) {
                        continue;
                    }
                    long distanceSquared = distanceSquared(pos, center);
                    if (distanceSquared > radiusSquared || distanceSquared >= bestDistanceSquared) {
                        continue;
                    }
                    if (isAlreadyClaimed(level, pos, claimedMap, storageIdLookup)) {
                        continue;
                    }
                    best = pos.immutable();
                    bestDistanceSquared = distanceSquared;
                }
            }
        }
        return Optional.ofNullable(best);
    }

    private static boolean isAlreadyClaimed(
            ServerLevel level,
            BlockPos pos,
            ClaimedChestMap claimedMap,
            ChestClaimPersistenceReconciliation.StorageIdLookup storageIdLookup
    ) {
        Optional<UUID> storageId = storageIdLookup.read(level, pos);
        if (storageId.isPresent()) {
            return true;
        }
        if (claimedMap == null) {
            return false;
        }
        ChestAnchor anchor = new ChestAnchor(
                level.dimension().location().toString(),
                pos.getX(),
                pos.getY(),
                pos.getZ());
        ClaimedChest match = claimedMap.chestByAnchor(anchor);
        return match != null;
    }

    public static Comparator<BlockPos> byDistanceFrom(BlockPos center) {
        return Comparator.comparingLong(pos -> distanceSquared(pos, center));
    }

    private static long distanceSquared(BlockPos pos, BlockPos center) {
        long dx = (long) pos.getX() - center.getX();
        long dy = (long) pos.getY() - center.getY();
        long dz = (long) pos.getZ() - center.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    public interface ClaimableStorageLookup {
        boolean isClaimable(ServerLevel level, BlockPos pos);
    }
}
