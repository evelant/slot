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
import dev.imagio.slot.inventory.workspace.SlotWorkspaceCommandService;
import dev.imagio.slot.inventory.workspace.WorkspaceCommandOutcome;
import dev.imagio.slot.neoforge.workflow.SlotPlayerWorkflowRuntimeService;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Map;

/**
 * Server-side handler for the recipe-viewer hover wanted shortcut. The
 * client only supplies an item identity; carried-count satisfaction and
 * persistence stay in the common command service.
 */
public final class SlotSetWantedCountPayloadHandler {
    private SlotSetWantedCountPayloadHandler() {
    }

    public static void handle(SlotSetWantedCountPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || payload == null) {
                return;
            }
            InventoryHostDescriptor host = resolveHost(player);
            if (host == null) {
                SlotCommon.LOGGER.info(
                        "[SLOT] rejected wanted hover hotkey: player={} item={} diagnostics=host_resolution_failed",
                        player.getName().getString(),
                        payload.itemId());
                return;
            }
            InventoryAuthoritySnapshot authority = InventoryAuthorityReadService.serverAuthority(player, host);
            WorkflowDomainRuntime runtime = SlotPlayerWorkflowRuntimeService.runtime(player);
            WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.setWantedCount(
                    runtime,
                    authority,
                    payload.itemId(),
                    payload.comparisonMode(),
                    payload.componentFingerprint(),
                    payload.targetCount());
            SlotCommon.LOGGER.info(
                    "[SLOT] wanted hover hotkey: player={} item={} target={} status={} diagnostics={}",
                    player.getName().getString(),
                    payload.itemId(),
                    payload.targetCount(),
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
                Component.literal("SLOT Wanted Hover"),
                SlotSetWantedCountPayloadHandler.class.getName(),
                new InventoryHostObservationHints(
                        InventoryHostFamilyHint.CARRIED_ONLY,
                        InventorySlotOwnershipPosture.SLOT_OWNED,
                        true,
                        true,
                        Map.of("slotWantedHover", "neoforge")
                )
        ));
    }
}
