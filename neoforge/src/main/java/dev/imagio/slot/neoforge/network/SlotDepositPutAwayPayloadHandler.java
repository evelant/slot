package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.integration.InventoryHostContext;
import dev.imagio.slot.inventory.integration.InventoryHostFamilyHint;
import dev.imagio.slot.inventory.integration.InventoryHostObservationHints;
import dev.imagio.slot.inventory.integration.InventoryHostResolver;
import dev.imagio.slot.inventory.integration.InventorySlotOwnershipPosture;
import dev.imagio.slot.inventory.query.InventoryAuthorityReadService;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.workspace.WorkspaceChestCommandService;
import dev.imagio.slot.inventory.workspace.WorkspaceCommandOutcome;
import dev.imagio.slot.neoforge.workflow.SlotPlayerWorkflowRuntimeService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Map;

/**
 * Server-side handler for the in-world put-away hotkey. Delegates to the
 * same common deposit command used by the workspace button and RPC action.
 */
public final class SlotDepositPutAwayPayloadHandler {
    private SlotDepositPutAwayPayloadHandler() {
    }

    public static void handle(SlotDepositPutAwayPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            InventoryHostDescriptor host = resolveHost(player);
            if (host == null) {
                SlotCommon.LOGGER.info(
                        "[SLOT] rejected put-away hotkey (in-world): player={} diagnostics=host_resolution_failed",
                        player.getName().getString());
                return;
            }
            InventoryAuthoritySnapshot authority = InventoryAuthorityReadService.serverAuthority(player, host);
            WorkspaceCommandOutcome outcome = WorkspaceChestCommandService.deposit(
                    player,
                    SlotPlayerWorkflowRuntimeService.runtime(player),
                    authority);
            SlotCommon.LOGGER.info(
                    "[SLOT] put-away hotkey (in-world): player={} status={} diagnostics={}",
                    player.getName().getString(),
                    outcome.status(),
                    outcome.diagnostics());
        });
    }

    private static InventoryHostDescriptor resolveHost(ServerPlayer player) {
        if (player == null) {
            return null;
        }
        AbstractContainerMenu menu = player.containerMenu;
        if (menu == null) {
            return null;
        }
        return InventoryHostResolver.resolve(new InventoryHostContext(
                menu,
                player.getInventory(),
                Component.literal("SLOT Put Away"),
                SlotDepositPutAwayPayloadHandler.class.getName(),
                new InventoryHostObservationHints(
                        InventoryHostFamilyHint.CARRIED_ONLY,
                        InventorySlotOwnershipPosture.SLOT_OWNED,
                        true,
                        true,
                        Map.of("slotPutAway", "neoforge")
                )
        ));
    }
}
