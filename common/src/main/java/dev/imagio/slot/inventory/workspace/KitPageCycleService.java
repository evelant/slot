package dev.imagio.slot.inventory.workspace;

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
import dev.imagio.slot.workflow.domain.ProtectionPolicy;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Shared server-side kit-page cycle route for in-screen and in-world
 * transports. Platform adapters supply only the runtime and any local
 * activity-tracker side effect after a successful inventory outcome.
 */
public final class KitPageCycleService {
    private static final Function<InventoryEntrySnapshot, ItemIdentity> KIT_IDENTITY_RESOLVER =
            entry -> entry == null ? null : ItemIdentityMatcher.create(entry.stack());

    private KitPageCycleService() {
    }

    public static WorkspaceCommandOutcome switchActivePage(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            int direction,
            String route,
            Consumer<InventoryActionOutcome> outcomeObserver
    ) {
        if (player == null) {
            return WorkspaceCommandOutcome.rejected("missing_player");
        }
        if (runtime == null) {
            return WorkspaceCommandOutcome.rejected("missing_runtime");
        }
        InventoryHostDescriptor host = resolveHost(player, route);
        if (host == null) {
            return WorkspaceCommandOutcome.rejected("host_resolution_failed");
        }
        InventoryAuthoritySnapshot authority = InventoryAuthorityReadService.serverAuthority(player, host);
        Function<InventoryActionRequest, InventoryActionOutcome> actionExecutor = request -> {
            InventoryActionOutcome outcome = InventoryActionExecutor.execute(
                    host,
                    player,
                    request,
                    ProtectionPolicy.allowAll()
            );
            runtime.recordOutcome(outcome);
            if (outcomeObserver != null) {
                outcomeObserver.accept(outcome);
            }
            return outcome;
        };
        return SlotWorkspaceCommandService.switchKitPage(
                runtime,
                authority,
                ProtectionPolicy.allowAll(),
                KIT_IDENTITY_RESOLVER,
                actionExecutor,
                direction);
    }

    private static InventoryHostDescriptor resolveHost(ServerPlayer player, String route) {
        AbstractContainerMenu menu = player.containerMenu;
        if (menu == null) {
            return null;
        }
        String normalizedRoute = route == null || route.isBlank() ? "server" : route;
        return InventoryHostResolver.resolve(new InventoryHostContext(
                menu,
                player.getInventory(),
                Component.literal("SLOT Kit Cycle"),
                KitPageCycleService.class.getName(),
                new InventoryHostObservationHints(
                        InventoryHostFamilyHint.CARRIED_ONLY,
                        InventorySlotOwnershipPosture.SLOT_OWNED,
                        true,
                        true,
                        Map.of("slotKitCycle", normalizedRoute)
                )
        ));
    }
}
