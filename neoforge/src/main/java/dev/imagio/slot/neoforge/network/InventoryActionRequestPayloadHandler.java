package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.inventory.action.InventoryActionOutcome;
import dev.imagio.slot.inventory.action.InventoryActionRequest;
import dev.imagio.slot.inventory.action.InventoryActionRequestPayload;
import dev.imagio.slot.inventory.action.InventoryActionOutcomePayload;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.integration.InventoryActionExecutor;
import dev.imagio.slot.inventory.integration.InventoryHostContext;
import dev.imagio.slot.inventory.integration.InventoryHostFamilyHint;
import dev.imagio.slot.inventory.integration.InventoryHostObservationHints;
import dev.imagio.slot.inventory.integration.InventoryHostResolver;
import dev.imagio.slot.inventory.integration.InventorySlotOwnershipPosture;
import dev.imagio.slot.workflow.domain.ProtectionPolicy;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Map;

public final class InventoryActionRequestPayloadHandler {
    private InventoryActionRequestPayloadHandler() {
    }

    public static void handle(InventoryActionRequestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            InventoryActionRequest request = payload == null ? null : payload.request();
            if (request == null) {
                return;
            }

            InventoryHostDescriptor host = InventoryHostResolver.resolve(new InventoryHostContext(
                    player.containerMenu,
                    player.getInventory(),
                    Component.empty(),
                    "",
                    new InventoryHostObservationHints(
                            player.containerMenu instanceof InventoryMenu
                                    ? InventoryHostFamilyHint.CARRIED_ONLY
                                    : InventoryHostFamilyHint.UNKNOWN,
                            player.containerMenu instanceof InventoryMenu || player.containerMenu instanceof CraftingMenu
                                    ? InventorySlotOwnershipPosture.SLOT_OWNED
                                    : InventorySlotOwnershipPosture.UNKNOWN,
                            player.containerMenu instanceof InventoryMenu,
                            true,
                            Map.of("serverContext", "true")
                    )
            ));
            InventoryActionOutcome outcome = InventoryActionExecutor.execute(
                    host,
                    player,
                    request,
                    ProtectionPolicy.allowAll()
            );
            PacketDistributor.sendToPlayer(player, new InventoryActionOutcomePayload(outcome));
            if (player.containerMenu != null) {
                player.containerMenu.broadcastChanges();
            }
        });
    }
}
