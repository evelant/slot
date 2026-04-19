package dev.imagio.slot.neoforge.storage;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel.ChestContentsSnapshot;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;

public final class ChestContentsReader {
    private ChestContentsReader() {
    }

    public static ChestContentsSnapshot read(MinecraftServer server, ClaimedChest chest) {
        if (server == null || chest == null) {
            return ChestContentsSnapshot.empty();
        }
        for (ChestAnchor anchor : chest.anchors()) {
            ServerLevel level = resolveLevel(server, anchor);
            if (level == null) {
                continue;
            }
            BlockPos pos = new BlockPos(anchor.x(), anchor.y(), anchor.z());
            if (!level.isLoaded(pos)) {
                continue;
            }
            IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
            if (handler == null) {
                continue;
            }
            return captureHandler(handler);
        }
        return ChestContentsSnapshot.empty();
    }

    private static ChestContentsSnapshot captureHandler(IItemHandler handler) {
        int slots = handler.getSlots();
        ArrayList<ItemStack> stacks = new ArrayList<>();
        for (int slot = 0; slot < slots; slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (stack != null && !stack.isEmpty()) {
                stacks.add(stack.copy());
            }
        }
        return new ChestContentsSnapshot(slots, stacks);
    }

    private static ServerLevel resolveLevel(MinecraftServer server, ChestAnchor anchor) {
        if (anchor == null || anchor.dimensionId() == null || anchor.dimensionId().isBlank()) {
            return null;
        }
        for (ServerLevel level : server.getAllLevels()) {
            if (level.dimension().location().toString().equals(anchor.dimensionId())) {
                return level;
            }
        }
        return null;
    }
}
