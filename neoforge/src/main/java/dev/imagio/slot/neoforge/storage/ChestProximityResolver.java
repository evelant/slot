package dev.imagio.slot.neoforge.storage;

import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.ClaimedChestMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import java.util.LinkedHashSet;
import java.util.Set;

public final class ChestProximityResolver {
    public static final int DEFAULT_RADIUS_BLOCKS = 8;

    private ChestProximityResolver() {
    }

    public static Set<String> proximateStorageIds(ServerPlayer player, ClaimedChestMap map) {
        return proximateStorageIds(player, map, DEFAULT_RADIUS_BLOCKS);
    }

    public static Set<String> proximateStorageIds(
            ServerPlayer player,
            ClaimedChestMap map,
            int radiusBlocks
    ) {
        if (player == null || map == null || map.chests().isEmpty()) {
            return Set.of();
        }
        String dimension = player.level().dimension().location().toString();
        BlockPos playerPos = player.blockPosition();
        long radiusSquared = (long) radiusBlocks * radiusBlocks;
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (ClaimedChest chest : map.chests()) {
            if (chest == null) {
                continue;
            }
            if (isWithin(chest, dimension, playerPos, radiusSquared)) {
                result.add(chest.storageId().toString());
            }
        }
        return result;
    }

    private static boolean isWithin(
            ClaimedChest chest,
            String dimension,
            BlockPos playerPos,
            long radiusSquared
    ) {
        for (ChestAnchor anchor : chest.anchors()) {
            if (!dimension.equals(anchor.dimensionId())) {
                continue;
            }
            long dx = (long) playerPos.getX() - anchor.x();
            long dy = (long) playerPos.getY() - anchor.y();
            long dz = (long) playerPos.getZ() - anchor.z();
            if (dx * dx + dy * dy + dz * dz <= radiusSquared) {
                return true;
            }
        }
        return false;
    }
}
