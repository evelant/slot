package dev.imagio.slot.inventory.integration;

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
            return MutationResult.blocked("missing_host_or_request", request == null ? ItemStack.EMPTY : request.stack());
        }

        InventorySourceDescriptor source = host.source(request.sourceId());
        if (source == null) {
            return MutationResult.blocked("unknown_source", request.stack());
        }
        if (mode == InventoryMutationMode.SIMULATE && !source.simulationSupported()) {
            return MutationResult.blocked("simulation_not_supported_by_source", request.stack());
        }

        if (host.ownsHostSource(source.id())) {
            return host.hostSession().mutate(host, request, mode);
        }

        PlayerInventoryExtension extension = host.extensionOwningSource(source.id());
        if (extension != null) {
            return extension.mutate(host, request, mode);
        }

        return switch (source.actionRoute()) {
            case PLAYER_MUTATION, MENU_MUTATION -> BuiltinInventoryActionExecutor.mutateSource(host, request, mode);
            case PROVIDER_MUTATION -> MutationResult.blocked("provider_route_missing_owner", request.stack());
            case NON_ACTIONABLE -> MutationResult.blocked("source_is_non_actionable", request.stack());
        };
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
