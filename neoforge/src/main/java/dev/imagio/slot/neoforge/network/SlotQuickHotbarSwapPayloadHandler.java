package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.inventory.workspace.QuickHotbarSwapHistory;
import dev.imagio.slot.inventory.workspace.WorkspaceCommandOutcome;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class SlotQuickHotbarSwapPayloadHandler {
    private SlotQuickHotbarSwapPayloadHandler() {
    }

    public static void handle(SlotQuickHotbarSwapPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            int direction = payload == null ? -1 : Integer.signum(payload.direction());
            WorkspaceCommandOutcome outcome = direction >= 0
                    ? QuickHotbarSwapHistory.redo(player)
                    : QuickHotbarSwapHistory.undo(player);
            SlotDebugLog.log(
                    "Quick hotbar swap {}: success={} diagnostics={}",
                    direction >= 0 ? "redo" : "undo",
                    outcome.success(),
                    outcome.diagnostics());
        });
    }
}
