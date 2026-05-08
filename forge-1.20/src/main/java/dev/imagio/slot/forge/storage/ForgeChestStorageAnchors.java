package dev.imagio.slot.forge.storage;

import dev.imagio.slot.workflow.domain.ChestAnchor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

import java.util.LinkedHashSet;
import java.util.Set;

public final class ForgeChestStorageAnchors {
    private ForgeChestStorageAnchors() {
    }

    public static boolean isClaimable(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.is(BlockTags.SHULKER_BOXES)) {
            return false;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return false;
        }
        IItemHandler handler = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, null).orElse(null);
        return handler != null;
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
