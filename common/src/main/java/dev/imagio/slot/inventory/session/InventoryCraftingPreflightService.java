package dev.imagio.slot.inventory.session;

import dev.imagio.slot.inventory.action.InventoryActionKind;
import dev.imagio.slot.inventory.action.InventoryActionConflictPolicy;
import dev.imagio.slot.inventory.action.InventoryActionRequest;
import dev.imagio.slot.inventory.action.InventoryActionQuantity;
import dev.imagio.slot.inventory.action.InventoryActionScope;
import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.action.InventoryCommandReasonCode;
import dev.imagio.slot.inventory.core.InventoryCraftingSurfaceSupport;
import dev.imagio.slot.inventory.core.InventoryToolActionId;
import dev.imagio.slot.inventory.core.InventoryToolToggleId;
import dev.imagio.slot.inventory.intent.InventoryMutationIntent;

public final class InventoryCraftingPreflightService {
    private InventoryCraftingPreflightService() {
    }

    public static InventoryCraftingPlan preflight(
            InventorySessionSnapshot session,
            InventoryMutationIntent mutationIntent
    ) {
        if (session == null) {
            return InventoryCraftingPlan.rejected("missing_session", InventoryCommandReasonCode.INVALID_INTENT);
        }
        if (mutationIntent == null) {
            return InventoryCraftingPlan.rejected("missing_intent", InventoryCommandReasonCode.INVALID_INTENT);
        }
        dev.imagio.slot.inventory.core.InventoryHostDescriptor host = session.host() != null
                ? session.host()
                : session.authority().host();
        if (host == null) {
            return InventoryCraftingPlan.rejected("missing_host", InventoryCommandReasonCode.MISSING_AUTHORITY);
        }

        if (mutationIntent instanceof InventoryMutationIntent.ToolAction toolAction) {
            return toolAction(session, toolAction);
        }
        if (mutationIntent instanceof InventoryMutationIntent.ToolToggle toolToggle) {
            return toolToggle(session, toolToggle);
        }
        if (mutationIntent instanceof InventoryMutationIntent.CraftingPlaceSelected craftPlaceSelected) {
            return InventoryCraftingPlanner.planSelectedPlacement(
                    session,
                    InventoryCraftingSurfaceSupport.resolve(host, craftPlaceSelected.toolId()),
                    craftPlaceSelected.inputIndex(),
                    craftPlaceSelected.placementMode(),
                    craftPlaceSelected.mode(),
                    craftPlaceSelected.origin()
            );
        }
        if (mutationIntent instanceof InventoryMutationIntent.CraftingPlaceCursor craftPlaceCursor) {
            return InventoryCraftingPlanner.planCursorPlacement(
                    session,
                    InventoryCraftingSurfaceSupport.resolve(host, craftPlaceCursor.toolId()),
                    craftPlaceCursor.inputIndex(),
                    craftPlaceCursor.placementMode(),
                    craftPlaceCursor.mode(),
                    craftPlaceCursor.origin()
            );
        }
        if (mutationIntent instanceof InventoryMutationIntent.CraftingDragCursor craftDragCursor) {
            return InventoryCraftingPlanner.planCursorDrag(
                    session,
                    InventoryCraftingSurfaceSupport.resolve(host, craftDragCursor.toolId()),
                    craftDragCursor.orderedInputIndices(),
                    craftDragCursor.dragMode(),
                    craftDragCursor.mode(),
                    craftDragCursor.origin()
            );
        }
        if (mutationIntent instanceof InventoryMutationIntent.CraftingExtractResult craftExtractResult) {
            return InventoryCraftingPlanner.planResultExtraction(
                    session,
                    InventoryCraftingSurfaceSupport.resolve(host, craftExtractResult.toolId()),
                    craftExtractResult.resultMode(),
                    craftExtractResult.mode(),
                    craftExtractResult.origin()
            );
        }
        return InventoryCraftingPlan.rejected("unsupported_crafting_intent", InventoryCommandReasonCode.UNSUPPORTED);
    }

    private static InventoryCraftingPlan toolAction(
            InventorySessionSnapshot session,
            InventoryMutationIntent.ToolAction toolAction
    ) {
        dev.imagio.slot.inventory.core.InventoryHostDescriptor host = session.host() != null
                ? session.host()
                : session.authority().host();
        InventoryCraftingSurfaceSupport.ResolvedCraftingSurface surface =
                InventoryCraftingSurfaceSupport.resolve(host, toolAction.toolId());
        if (!surface.present()) {
            return InventoryCraftingPlan.rejected("crafting_surface_not_present", InventoryCommandReasonCode.INVALID_INTENT);
        }
        if (!surface.supportsAction(toolAction.actionId())) {
            return InventoryCraftingPlan.rejected("unsupported_tool_action", InventoryCommandReasonCode.UNSUPPORTED);
        }
        if (mutatesCraftingGrid(toolAction.actionId())
                && InventoryCraftingPlanner.protectedCraftingInputs(session, surface)) {
            return InventoryCraftingPlan.rejected("source_inputs_blocked_by_policy", InventoryCommandReasonCode.SOURCE_BLOCKED_BY_POLICY);
        }

        InventoryActionTarget primaryTarget = new InventoryActionTarget.ToolControlTarget(
                surface.tool().id(),
                surface.actionStableId(toolAction.actionId())
        );
        InventoryActionRequest request = new InventoryActionRequest(
                host.hostId(),
                host.serverMenuRef(),
                "",
                InventoryActionKind.TOOL_ACTION,
                toolAction.mode(),
                InventoryActionQuantity.DEFAULT,
                InventoryActionScope.SINGLE_TARGET,
                InventoryActionConflictPolicy.DEFAULT,
                toolAction.origin(),
                primaryTarget,
                null,
                0,
                null,
                net.minecraft.world.item.ItemStack.EMPTY,
                toolAction.actionId() == null ? InventoryToolActionId.PROVIDER_DEFINED : toolAction.actionId(),
                InventoryToolToggleId.PROVIDER_DEFINED,
                false,
                ""
        );
        return new InventoryCraftingPlan(java.util.List.of(request), java.util.List.of(), "");
    }

    private static InventoryCraftingPlan toolToggle(
            InventorySessionSnapshot session,
            InventoryMutationIntent.ToolToggle toolToggle
    ) {
        dev.imagio.slot.inventory.core.InventoryHostDescriptor host = session.host() != null
                ? session.host()
                : session.authority().host();
        InventoryCraftingSurfaceSupport.ResolvedCraftingSurface surface =
                InventoryCraftingSurfaceSupport.resolve(host, toolToggle.toolId());
        if (!surface.present()) {
            return InventoryCraftingPlan.rejected("crafting_surface_not_present", InventoryCommandReasonCode.INVALID_INTENT);
        }
        if (!surface.supportsToggle(toolToggle.toggleId())) {
            return InventoryCraftingPlan.rejected("unsupported_tool_toggle", InventoryCommandReasonCode.UNSUPPORTED);
        }

        InventoryActionRequest request = new InventoryActionRequest(
                host.hostId(),
                host.serverMenuRef(),
                "",
                InventoryActionKind.TOOL_TOGGLE,
                toolToggle.mode(),
                InventoryActionQuantity.DEFAULT,
                InventoryActionScope.SINGLE_TARGET,
                InventoryActionConflictPolicy.DEFAULT,
                toolToggle.origin(),
                new InventoryActionTarget.ToolControlTarget(surface.tool().id(), surface.toggleStableId(toolToggle.toggleId())),
                null,
                0,
                null,
                net.minecraft.world.item.ItemStack.EMPTY,
                InventoryToolActionId.PROVIDER_DEFINED,
                toolToggle.toggleId() == null ? InventoryToolToggleId.PROVIDER_DEFINED : toolToggle.toggleId(),
                toolToggle.desiredState(),
                ""
        );
        return new InventoryCraftingPlan(java.util.List.of(request), java.util.List.of(), "");
    }

    private static boolean mutatesCraftingGrid(InventoryToolActionId actionId) {
        return actionId == InventoryToolActionId.CLEAR_GRID
                || actionId == InventoryToolActionId.BALANCE_GRID
                || actionId == InventoryToolActionId.ROTATE_GRID;
    }
}
