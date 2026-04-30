package dev.imagio.slot.neoforge.storage;

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

/**
 * Locate the closest <em>unclaimed</em> chest near the player. Drives the
 * atlas-side loot panel: the next time the player opens the workspace
 * with an unclaimed chest in range, that chest's contents render as a
 * Triage-style panel on the left.
 *
 * <p>Iterates loaded chunks in a 3×3 (or larger if radius demands)
 * neighborhood around the player, examines each chunk's block entities,
 * filters to anything {@link ChestStorageAnchors#isClaimable} accepts
 * (item-handler-capable blocks excluding shulker boxes — covers
 * barrels, trapped chests, modded chest-likes), and skips any that
 * already carry a SLOT storage-id attachment (those are storage
 * chests). Returns the nearest match by squared distance, or empty
 * when none.
 */
public final class LootChestProximityResolver {
    public static final int DEFAULT_RADIUS_BLOCKS = 8;

    private LootChestProximityResolver() {
    }

    public static Optional<BlockPos> closest(ServerPlayer player, ClaimedChestMap claimedMap) {
        return closest(player, claimedMap, DEFAULT_RADIUS_BLOCKS);
    }

    public static Optional<BlockPos> closest(
            ServerPlayer player,
            ClaimedChestMap claimedMap,
            int radiusBlocks
    ) {
        if (player == null) {
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
                for (BlockEntity be : blockEntities.values()) {
                    BlockPos pos = be.getBlockPos();
                    if (!ChestStorageAnchors.isClaimable(level, pos)) {
                        continue;
                    }
                    long dx = (long) pos.getX() - center.getX();
                    long dy = (long) pos.getY() - center.getY();
                    long dz = (long) pos.getZ() - center.getZ();
                    long distSq = dx * dx + dy * dy + dz * dz;
                    if (distSq > radiusSquared || distSq >= bestDistanceSquared) {
                        continue;
                    }
                    if (isAlreadyClaimed(be, claimedMap, level)) {
                        continue;
                    }
                    best = pos.immutable();
                    bestDistanceSquared = distSq;
                }
            }
        }
        return Optional.ofNullable(best);
    }

    private static boolean isAlreadyClaimed(BlockEntity be, ClaimedChestMap claimedMap, ServerLevel level) {
        Optional<java.util.UUID> storageId = ChestStorageIds.read(be);
        if (storageId.isPresent()) {
            return true;
        }
        // Fallback: a fresh BE without the attachment may still have a
        // workflow-domain claim by anchor (test populate path doesn't stamp
        // the attachment). Treat anchor-matched chests as claimed too.
        if (claimedMap == null) {
            return false;
        }
        var anchor = ChestStorageAnchors.toAnchor(level, be.getBlockPos());
        ClaimedChest match = claimedMap.chestByAnchor(anchor);
        return match != null;
    }

    public static Comparator<BlockPos> byDistanceFrom(BlockPos center) {
        return Comparator.comparingLong(p -> {
            long dx = (long) p.getX() - center.getX();
            long dy = (long) p.getY() - center.getY();
            long dz = (long) p.getZ() - center.getZ();
            return dx * dx + dy * dy + dz * dz;
        });
    }
}
