package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.inventory.action.InventoryActionOutcome;
import dev.imagio.slot.inventory.action.InventoryActionRequest;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.integration.InventoryActionExecutor;
import dev.imagio.slot.inventory.integration.InventoryHostContext;
import dev.imagio.slot.inventory.integration.InventoryHostFamilyHint;
import dev.imagio.slot.inventory.integration.InventoryHostObservationHints;
import dev.imagio.slot.inventory.integration.InventoryHostResolver;
import dev.imagio.slot.inventory.integration.InventorySlotOwnershipPosture;
import dev.imagio.slot.inventory.query.InventoryAuthorityReadService;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceCommandService;
import dev.imagio.slot.inventory.workspace.WorkspaceCommandOutcome;
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
 * Server-side handler for the in-world kit-page cycle hotkey. Mirrors
 * {@code SlotWorkspaceUiSession.switchKitPage} so the result is
 * identical regardless of whether the player triggered the swap from
 * inside the SLOT UI (RPC path) or while in-world (this packet).
 */
public final class SlotKitPageCyclePayloadHandler {

    private static final Function<InventoryEntrySnapshot, ItemIdentity> KIT_IDENTITY_RESOLVER =
            entry -> entry == null ? null : ItemIdentityMatcher.create(entry.stack());

    private SlotKitPageCyclePayloadHandler() {
    }

    public static void handle(SlotKitPageCyclePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            int direction = Integer.signum(payload.direction());
            if (direction == 0) {
                direction = 1;
            }
            WorkflowDomainRuntime runtime = SlotPlayerWorkflowRuntimeService.runtime(player);
            if (runtime == null) {
                return;
            }
            // Same activation guard as the in-screen path so the
            // hotkey is a no-op when no kit is active or the active
            // kit has only one page.
            var activation = runtime.kitWorkflow().activation();
            if (!activation.isActive()) {
                return;
            }
            InventoryHostDescriptor host = resolveHost(player);
            if (host == null) {
                SlotDebugLog.log(
                        "Kit-page cycle ignored: no inventory host for {} (containerMenu={})",
                        player.getGameProfile().getName(),
                        player.containerMenu == null ? "null" : player.containerMenu.getClass().getName()
                );
                return;
            }
            InventoryAuthoritySnapshot authority =
                    InventoryAuthorityReadService.serverAuthority(player, host);
            ServerPlayer effectivePlayer = player;
            int directionFinal = direction;
            Function<InventoryActionRequest, InventoryActionOutcome> actionExecutor = request -> {
                InventoryActionOutcome outcome = InventoryActionExecutor.execute(
                        host,
                        effectivePlayer,
                        request,
                        ProtectionPolicy.allowAll()
                );
                runtime.recordOutcome(outcome);
                if (outcome != null && outcome.successful()) {
                    NeoForgeCarriedActivityTracker.suppressNext(effectivePlayer);
                }
                return outcome;
            };
            WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.switchKitPage(
                    runtime,
                    authority,
                    ProtectionPolicy.allowAll(),
                    KIT_IDENTITY_RESOLVER,
                    actionExecutor,
                    directionFinal
            );
            SlotDebugLog.log(
                    "In-world kit-page cycle: direction={} success={}",
                    directionFinal,
                    outcome.success()
            );
        });
    }

    private static InventoryHostDescriptor resolveHost(ServerPlayer player) {
        AbstractContainerMenu menu = player.containerMenu;
        if (menu == null) {
            return null;
        }
        return InventoryHostResolver.resolve(new InventoryHostContext(
                menu,
                player.getInventory(),
                Component.literal("SLOT Kit Cycle"),
                SlotKitPageCyclePayloadHandler.class.getName(),
                new InventoryHostObservationHints(
                        InventoryHostFamilyHint.CARRIED_ONLY,
                        InventorySlotOwnershipPosture.SLOT_OWNED,
                        true,
                        true,
                        Map.of("slotKitCycle", "in_world")
                )
        ));
    }
}
