package dev.imagio.slot.neoforge.client;

import dev.imagio.slot.client.screen.QuickAccessFollowUpExecutor;
import net.neoforged.neoforge.client.event.ClientTickEvent;

final class SlotQuickAccessInventoryActions {
    private SlotQuickAccessInventoryActions() {
    }

    static void onClientTick(ClientTickEvent.Post event) {
        QuickAccessFollowUpExecutor.tickClient();
    }
}
