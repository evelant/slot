package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.storage.WorldStorageAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;

final class StorageMutationProbe {
    private StorageMutationProbe() {
    }

    static boolean canInsertAny(
            MinecraftServer server,
            WorldStorageAccess worldStorage,
            WorldStorageAccess.Target target,
            ItemStack stack,
            int requestedCount
    ) {
        if (worldStorage == null || target == null || stack == null || stack.isEmpty() || requestedCount <= 0) {
            return false;
        }
        int probeCount = Math.max(1, Math.min(stack.getCount(), requestedCount));
        int maxStackSize = Math.max(1, stack.getMaxStackSize());
        probeCount = Math.min(probeCount, maxStackSize);
        ItemStack probe = stack.copy();
        probe.setCount(probeCount);
        try {
            ItemStack leftover = worldStorage.insert(server, target, probe, true);
            int leftoverCount = leftover == null || leftover.isEmpty() ? 0 : leftover.getCount();
            return leftoverCount < probeCount;
        } catch (RuntimeException | LinkageError exception) {
            return false;
        }
    }

    static boolean canExtractAny(
            MinecraftServer server,
            WorldStorageAccess worldStorage,
            WorldStorageAccess.Target target,
            SlotWorkspaceViewModel.ChestContentsSnapshot snapshot
    ) {
        if (worldStorage == null || target == null || snapshot == null || snapshot.contents().isEmpty()) {
            return false;
        }
        for (int i = 0; i < snapshot.contents().size(); i++) {
            ItemStack stack = snapshot.contents().get(i);
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            int slotIndex = i < snapshot.slotIndices().size() ? snapshot.slotIndices().get(i) : i;
            try {
                ItemStack extracted = worldStorage.extract(server, target, slotIndex, 1, true);
                if (extracted != null && !extracted.isEmpty()) {
                    return true;
                }
            } catch (RuntimeException | LinkageError ignored) {
                return false;
            }
        }
        return false;
    }
}
