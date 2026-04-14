package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.intent.ActionFamily;
import dev.imagio.slot.intent.ActionRequestId;
import dev.imagio.slot.operation.ActionOutcome;
import dev.imagio.slot.operation.ActionReason;
import dev.imagio.slot.operation.RefreshScope;
import dev.imagio.slot.network.SlotActionOutcomePayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.PacketDistributor;

public final class SlotActionOutcomeSupport {
    private SlotActionOutcomeSupport() {
    }

    public static String menuKey(AbstractContainerMenu menu) {
        if (menu == null) {
            return "";
        }
        return menu.getClass().getName() + "#" + menu.containerId;
    }

    public static void send(ServerPlayer player, AbstractContainerMenu menu, ActionFamily actionFamily, int affectedCount) {
        if (player == null || menu == null || actionFamily == null) {
            return;
        }

        send(
                player,
                menu,
                affectedCount > 0
                        ? ActionOutcome.confirmed(ActionRequestId.none(), actionFamily, affectedCount, RefreshScope.SESSION)
                        : ActionOutcome.blocked(ActionRequestId.none(), actionFamily, ActionReason.UNSPECIFIED, RefreshScope.SESSION)
        );
    }

    public static void send(ServerPlayer player, AbstractContainerMenu menu, ActionOutcome outcome) {
        if (player == null || menu == null || outcome == null) {
            return;
        }

        PacketDistributor.sendToPlayer(player, SlotActionOutcomePayload.from(menuKey(menu), outcome));
    }
}
