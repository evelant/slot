package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.operation.ActionOutcome;
import net.minecraft.nbt.CompoundTag;

import java.util.List;
import java.util.Map;
import java.util.UUID;

record SlotServerDispatchResult(
        ActionOutcome outcome,
        boolean broadcastChanges,
        Map<UUID, CompoundTag> syncedContents
) {
    SlotServerDispatchResult {
        syncedContents = syncedContents == null ? Map.of() : Map.copyOf(syncedContents);
    }

    static SlotServerDispatchResult of(ActionOutcome outcome, boolean broadcastChanges, Map<UUID, CompoundTag> syncedContents) {
        return new SlotServerDispatchResult(outcome, broadcastChanges, syncedContents);
    }
}
