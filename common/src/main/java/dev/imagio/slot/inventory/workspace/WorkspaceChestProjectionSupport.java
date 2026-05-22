package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.storage.WorldDisplayStorageSource;
import dev.imagio.slot.inventory.storage.WorldStorageAccess;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.ClaimedChestMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

public final class WorkspaceChestProjectionSupport {
    public static final int DEFAULT_RADIUS_BLOCKS = 8;
    public static final int CONTEXTUAL_SUGGESTION_RADIUS_BLOCKS = 50;

    private WorkspaceChestProjectionSupport() {
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
            if (chest != null && chest.role().visibleToWorkspace()
                    && isWithin(chest, dimension, playerPos, radiusSquared)) {
                result.add(chest.storageId().toString());
            }
        }
        return result;
    }

    public static boolean isProximate(ServerPlayer player, ClaimedChest chest) {
        return isProximate(player, chest, DEFAULT_RADIUS_BLOCKS);
    }

    public static boolean isProximate(
            ServerPlayer player,
            ClaimedChest chest,
            int radiusBlocks
    ) {
        if (player == null || chest == null) {
            return false;
        }
        String dimension = player.level().dimension().location().toString();
        BlockPos playerPos = player.blockPosition();
        long radiusSquared = (long) Math.max(0, radiusBlocks) * Math.max(0, radiusBlocks);
        return isWithin(chest, dimension, playerPos, radiusSquared);
    }

    public static boolean isProximate(ServerPlayer player, ChestAnchor anchor) {
        return isProximate(player, anchor, DEFAULT_RADIUS_BLOCKS);
    }

    public static boolean isProximate(
            ServerPlayer player,
            ChestAnchor anchor,
            int radiusBlocks
    ) {
        if (player == null || anchor == null) {
            return false;
        }
        String dimension = player.level().dimension().location().toString();
        BlockPos playerPos = player.blockPosition();
        long radiusSquared = (long) Math.max(0, radiusBlocks) * Math.max(0, radiusBlocks);
        return isWithin(anchor, dimension, playerPos, radiusSquared);
    }

    public static Function<String, SlotWorkspaceViewModel.ChestContentsSnapshot> contentsResolver(
            MinecraftServer server,
            ClaimedChestMap map,
            WorldStorageAccess worldStorage
    ) {
        ClaimedChestMap resolvedMap = map == null ? ClaimedChestMap.empty() : map;
        return storageId -> {
            UUID uuid = parseUuid(storageId);
            if (uuid == null) {
                return SlotWorkspaceViewModel.ChestContentsSnapshot.empty();
            }
            return readContents(server, resolvedMap.chest(uuid), worldStorage);
        };
    }

    public static List<WorldDisplayStorageSource> proximateDisplaySources(
            ServerPlayer player,
            WorldStorageAccess worldStorage
    ) {
        return proximateDisplaySources(player, worldStorage, DEFAULT_RADIUS_BLOCKS);
    }

    public static List<WorldDisplayStorageSource> proximateDisplaySources(
            ServerPlayer player,
            WorldStorageAccess worldStorage,
            int radiusBlocks
    ) {
        if (player == null || worldStorage == null) {
            return List.of();
        }
        List<WorldDisplayStorageSource> sources = worldStorage.proximateDisplaySources(player, radiusBlocks);
        return sources == null ? List.of() : List.copyOf(sources);
    }

    public static SlotWorkspaceViewModel.ChestContentsSnapshot readContents(
            MinecraftServer server,
            ClaimedChest chest,
            WorldStorageAccess worldStorage
    ) {
        if (server == null || chest == null || worldStorage == null) {
            return SlotWorkspaceViewModel.ChestContentsSnapshot.empty();
        }
        WorldStorageAccess.Target target = new WorldStorageAccess.Target.Chest(chest);
        int slots = Math.max(0, worldStorage.slotCount(server, target));
        ArrayList<ItemStack> stacks = new ArrayList<>();
        ArrayList<Integer> slotIndices = new ArrayList<>();
        for (WorldStorageAccess.SlotContent content : worldStorage.enumerate(server, target)) {
            if (content == null || content.stack() == null || content.stack().isEmpty()) {
                continue;
            }
            stacks.add(content.stack().copy());
            slotIndices.add(content.slotIndex());
        }
        return new SlotWorkspaceViewModel.ChestContentsSnapshot(slots, stacks, slotIndices);
    }

    private static boolean isWithin(
            ClaimedChest chest,
            String dimension,
            BlockPos playerPos,
            long radiusSquared
    ) {
        for (ChestAnchor anchor : chest.anchors()) {
            if (isWithin(anchor, dimension, playerPos, radiusSquared)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isWithin(
            ChestAnchor anchor,
            String dimension,
            BlockPos playerPos,
            long radiusSquared
    ) {
        if (anchor == null || playerPos == null || !dimension.equals(anchor.dimensionId())) {
            return false;
        }
        long dx = (long) playerPos.getX() - anchor.x();
        long dy = (long) playerPos.getY() - anchor.y();
        long dz = (long) playerPos.getZ() - anchor.z();
        return dx * dx + dy * dy + dz * dz <= radiusSquared;
    }

    private static UUID parseUuid(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
