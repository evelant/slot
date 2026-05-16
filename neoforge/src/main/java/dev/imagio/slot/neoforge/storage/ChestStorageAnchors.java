package dev.imagio.slot.neoforge.storage;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.inventory.workspace.StorageAffinityPolicy;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.LinkedHashSet;
import java.util.Set;

public final class ChestStorageAnchors {
    private static final TagKey<Block> STORAGE_AFFINITY_ALLOWED =
            TagKey.create(Registries.BLOCK, SlotCommon.id("storage_affinity_allowed"));
    private static final TagKey<Block> NO_STORAGE_AFFINITY =
            TagKey.create(Registries.BLOCK, SlotCommon.id("no_storage_affinity"));

    private ChestStorageAnchors() {
    }

    public static boolean isClaimable(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return false;
        }
        if (state.is(BlockTags.SHULKER_BOXES)) {
            return false;
        }
        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
        if (handler == null) {
            return false;
        }
        return StorageAffinityPolicy.isEligible(
                handler.getSlots(),
                state.is(STORAGE_AFFINITY_ALLOWED),
                state.is(NO_STORAGE_AFFINITY));
    }

    public static Set<ChestAnchor> resolveAnchors(Level level, BlockPos pos) {
        LinkedHashSet<ChestAnchor> anchors = new LinkedHashSet<>();
        if (level == null || pos == null) {
            return anchors;
        }
        anchors.add(toAnchor(level, pos));
        BlockPos paired = pairedChestNeighbor(level, pos);
        if (paired != null) {
            anchors.add(toAnchor(level, paired));
        }
        return anchors;
    }

    public static BlockPos pairedChestNeighbor(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return null;
        }
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof ChestBlock)) {
            return null;
        }
        ChestType type = state.getValue(ChestBlock.TYPE);
        if (type == ChestType.SINGLE) {
            return null;
        }
        Direction connected = ChestBlock.getConnectedDirection(state);
        BlockPos neighbor = pos.relative(connected);
        BlockState neighborState = level.getBlockState(neighbor);
        if (!(neighborState.getBlock() instanceof ChestBlock) || neighborState.getBlock() != state.getBlock()) {
            return null;
        }
        if (neighborState.getValue(ChestBlock.TYPE) == ChestType.SINGLE) {
            return null;
        }
        if (neighborState.getValue(ChestBlock.FACING) != state.getValue(ChestBlock.FACING)) {
            return null;
        }
        return neighbor;
    }

    public static ChestAnchor toAnchor(Level level, BlockPos pos) {
        return new ChestAnchor(dimensionId(level), pos.getX(), pos.getY(), pos.getZ());
    }

    public static String dimensionId(Level level) {
        return level.dimension().location().toString();
    }
}
