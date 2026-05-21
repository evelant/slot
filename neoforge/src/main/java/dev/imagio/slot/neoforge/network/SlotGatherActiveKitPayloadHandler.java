package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.inventory.action.InventoryActionOutcome;
import dev.imagio.slot.inventory.action.InventoryActionRequest;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.integration.InventoryActionExecutor;
import dev.imagio.slot.inventory.integration.InventoryHostContext;
import dev.imagio.slot.inventory.integration.InventoryHostFamilyHint;
import dev.imagio.slot.inventory.integration.InventoryHostObservationHints;
import dev.imagio.slot.inventory.integration.InventoryHostResolver;
import dev.imagio.slot.inventory.integration.InventorySlotOwnershipPosture;
import dev.imagio.slot.inventory.query.InventoryAuthorityReadService;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.workspace.KitGatherService;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceCommandService;
import dev.imagio.slot.neoforge.storage.NeoForgeCarriedActivityTracker;
import dev.imagio.slot.neoforge.workflow.SlotPlayerWorkflowRuntimeService;
import dev.imagio.slot.workflow.domain.ProtectionPolicy;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Map;
import java.util.function.Function;

/**
 * Server-side handler for the in-world gather hotkey. Delegates to
 * {@link KitGatherService} so the in-screen RPC path and the in-world
 * packet path produce identical state changes.
 */
public final class SlotGatherActiveKitPayloadHandler {
    private SlotGatherActiveKitPayloadHandler() {
    }

    public static void handle(SlotGatherActiveKitPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            WorkflowDomainRuntime runtime = SlotPlayerWorkflowRuntimeService.runtime(player);
            KitGatherService.Outcome outcome = KitGatherService.gatherActiveKit(player, runtime);
            reapplyActiveKitFromCarry(player, runtime);
            SlotCommon.LOGGER.info(
                    "[SLOT] gather hotkey (in-world): player={} reason={} pulled={} unreachable={}",
                    player.getName().getString(),
                    outcome.reason(),
                    outcome.totalItemsPulled(),
                    outcome.identitiesUnreachable()
            );
        });
    }

    private static void reapplyActiveKitFromCarry(ServerPlayer player, WorkflowDomainRuntime runtime) {
        if (player == null || runtime == null || !runtime.kitWorkflow().activation().isActive()) {
            return;
        }
        InventoryHostDescriptor host = resolveHost(player);
        if (host == null) {
            return;
        }
        InventoryAuthoritySnapshot authority = InventoryAuthorityReadService.serverAuthority(player, host);
        SlotWorkspaceCommandService.reapplyActiveKit(
                runtime,
                authority,
                ProtectionPolicy.allowAll(),
                entry -> entry == null ? null : ItemIdentityMatcher.create(entry.stack()),
                actionExecutor(runtime, host, player));
    }

    private static Function<InventoryActionRequest, InventoryActionOutcome> actionExecutor(
            WorkflowDomainRuntime runtime,
            InventoryHostDescriptor host,
            ServerPlayer player
    ) {
        return request -> {
            InventoryActionOutcome outcome = InventoryActionExecutor.execute(
                    host,
                    player,
                    request,
                    ProtectionPolicy.allowAll()
            );
            runtime.recordOutcome(outcome);
            if (outcome != null && outcome.successful()) {
                NeoForgeCarriedActivityTracker.suppressOutcome(player, outcome);
            }
            return outcome;
        };
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
                Component.literal("SLOT Kit Gather"),
                SlotGatherActiveKitPayloadHandler.class.getName(),
                new InventoryHostObservationHints(
                        InventoryHostFamilyHint.CARRIED_ONLY,
                        InventorySlotOwnershipPosture.SLOT_OWNED,
                        true,
                        true,
                        Map.of("slotKitGather", "neoforge")
                )
        ));
    }
}
