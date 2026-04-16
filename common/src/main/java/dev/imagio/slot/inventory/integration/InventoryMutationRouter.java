package dev.imagio.slot.inventory.integration;

import dev.imagio.slot.SlotDiagnostics;
import dev.imagio.slot.inventory.action.InventoryActionMode;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.InventoryToolActionId;
import dev.imagio.slot.inventory.core.InventoryToolToggleId;
import net.minecraft.world.item.ItemStack;

public final class InventoryMutationRouter {
    private InventoryMutationRouter() {
    }

    public static MutationResult mutate(
            InventoryHostDescriptor host,
            InventoryMutationRequest request,
            InventoryMutationMode mode
    ) {
        if (host == null || request == null) {
            MutationResult result = MutationResult.blocked(
                    "missing_host_or_request",
                    request == null ? ItemStack.EMPTY : request.stack()
            );
            SlotDiagnostics.mutationRejected("missing_host_or_request", host, null, request, mode, result);
            return result;
        }

        InventorySourceDescriptor source = host.source(request.sourceId());
        if (source == null) {
            MutationResult result = MutationResult.blocked("unknown_source", request.stack());
            SlotDiagnostics.mutationRejected("unknown_source", host, null, request, mode, result);
            return result;
        }
        if (mode == InventoryMutationMode.SIMULATE && !source.simulationSupported()) {
            MutationResult result = MutationResult.blocked("simulation_not_supported_by_source", request.stack());
            SlotDiagnostics.mutationRejected("simulation_not_supported_by_source", host, source, request, mode, result);
            return result;
        }

        String route;
        MutationResult result;
        if (host.ownsHostSource(source.id())) {
            route = "host:" + host.hostSession().providerId();
            result = host.hostSession().mutate(host, request, mode);
        } else {
            PlayerInventoryExtension extension = host.extensionOwningSource(source.id());
            if (extension != null) {
                route = "extension:" + extension.providerId();
                result = extension.mutate(host, request, mode);
            } else {
                switch (source.actionRoute()) {
                    case PLAYER_MUTATION, MENU_MUTATION -> {
                        route = "builtin:" + source.actionRoute();
                        result = BuiltinInventoryActionExecutor.mutateSource(host, request, mode);
                    }
                    case PROVIDER_MUTATION -> {
                        route = "provider_route_missing_owner";
                        result = MutationResult.blocked("provider_route_missing_owner", request.stack());
                    }
                    case NON_ACTIONABLE -> {
                        route = "non_actionable";
                        result = MutationResult.blocked("source_is_non_actionable", request.stack());
                    }
                    default -> throw new IllegalStateException("unhandled_action_route:" + source.actionRoute());
                }
            }
        }

        if (result != null && result.successful()) {
            SlotDiagnostics.mutationRouted(route, host, source, request, mode, result);
        } else {
            SlotDiagnostics.mutationRejected(route, host, source, request, mode, result);
        }
        return result;
    }

    public static ToolActionResult activateTool(
            InventoryHostDescriptor host,
            String toolId,
            InventoryActionMode mode
    ) {
        if (host == null || toolId == null || toolId.isBlank()) {
            return ToolActionResult.blocked("missing_host_or_tool");
        }
        dev.imagio.slot.inventory.core.InventoryToolDescriptor tool = host.tool(toolId);
        if (mode == InventoryActionMode.SIMULATE && tool != null && !tool.activationSimulationSupported()) {
            return ToolActionResult.blocked("simulation_not_supported_by_tool_activation");
        }
        return host.hostSession().activateTool(host, toolId, mode == null ? InventoryActionMode.EXECUTE : mode);
    }

    public static ToolActionResult executeToolAction(
            InventoryHostDescriptor host,
            String toolId,
            InventoryToolActionId actionId,
            InventoryActionMode mode
    ) {
        if (host == null || toolId == null || toolId.isBlank() || actionId == null) {
            return ToolActionResult.blocked("missing_host_tool_or_action");
        }
        dev.imagio.slot.inventory.core.InventoryToolDescriptor tool = host.tool(toolId);
        if (mode == InventoryActionMode.SIMULATE
                && tool != null
                && tool.actions().stream().filter(action -> action.id() == actionId).findFirst().map(dev.imagio.slot.inventory.core.InventoryToolAction::simulationSupported).orElse(true) == false) {
            return ToolActionResult.blocked("simulation_not_supported_by_tool_action");
        }
        return host.hostSession().executeToolAction(host, toolId, actionId, mode == null ? InventoryActionMode.EXECUTE : mode);
    }

    public static ToolActionResult setToolToggle(
            InventoryHostDescriptor host,
            String toolId,
            InventoryToolToggleId toggleId,
            boolean enabled,
            InventoryActionMode mode
    ) {
        if (host == null || toolId == null || toolId.isBlank() || toggleId == null) {
            return ToolActionResult.blocked("missing_host_tool_or_toggle");
        }
        dev.imagio.slot.inventory.core.InventoryToolDescriptor tool = host.tool(toolId);
        if (mode == InventoryActionMode.SIMULATE
                && tool != null
                && tool.toggles().stream().filter(toggle -> toggle.id() == toggleId).findFirst().map(dev.imagio.slot.inventory.core.InventoryToolToggle::simulationSupported).orElse(true) == false) {
            return ToolActionResult.blocked("simulation_not_supported_by_tool_toggle");
        }
        return host.hostSession().setToolToggle(
                host,
                toolId,
                toggleId,
                enabled,
                mode == null ? InventoryActionMode.EXECUTE : mode
        );
    }
}
