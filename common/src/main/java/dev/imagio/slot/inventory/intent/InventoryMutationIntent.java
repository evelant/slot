package dev.imagio.slot.inventory.intent;

import dev.imagio.slot.inventory.action.InventoryActionMode;
import dev.imagio.slot.inventory.action.InventoryActionRequest;
import dev.imagio.slot.inventory.core.InventoryToolActionId;
import dev.imagio.slot.inventory.core.InventoryToolToggleId;
import dev.imagio.slot.inventory.action.ProjectedRowTransferIntent;

public sealed interface InventoryMutationIntent extends InventoryIntent permits
        InventoryMutationIntent.ExecuteRequest,
        InventoryMutationIntent.ProjectedRowTransfer,
        InventoryMutationIntent.TrashEntry,
        InventoryMutationIntent.VoidEntry,
        InventoryMutationIntent.ToolAction,
        InventoryMutationIntent.ToolToggle,
        InventoryMutationIntent.CraftingPlaceSelected,
        InventoryMutationIntent.CraftingPlaceCursor,
        InventoryMutationIntent.CraftingDragCursor,
        InventoryMutationIntent.CraftingExtractResult {

    @Override
    default InventoryIntentKind kind() {
        return InventoryIntentKind.MUTATION;
    }

    record ExecuteRequest(
            InventoryActionRequest request,
            String origin
    ) implements InventoryMutationIntent {
        public ExecuteRequest {
            origin = origin == null ? "" : origin;
        }
    }

    record ProjectedRowTransfer(
            ProjectedRowTransferIntent transferIntent,
            String origin
    ) implements InventoryMutationIntent {
        public ProjectedRowTransfer {
            origin = origin == null ? "" : origin;
        }
    }

    record TrashEntry(
            String entryId,
            InventoryActionMode mode,
            String origin
    ) implements InventoryMutationIntent {
        public TrashEntry {
            entryId = entryId == null ? "" : entryId;
            mode = mode == null ? InventoryActionMode.EXECUTE : mode;
            origin = origin == null ? "" : origin;
        }
    }

    record VoidEntry(
            String entryId,
            InventoryActionMode mode,
            String origin
    ) implements InventoryMutationIntent {
        public VoidEntry {
            entryId = entryId == null ? "" : entryId;
            mode = mode == null ? InventoryActionMode.EXECUTE : mode;
            origin = origin == null ? "" : origin;
        }
    }

    record ToolAction(
            String toolId,
            InventoryToolActionId actionId,
            InventoryActionMode mode,
            String origin
    ) implements InventoryMutationIntent {
        public ToolAction {
            toolId = toolId == null ? "" : toolId;
            actionId = actionId == null ? InventoryToolActionId.PROVIDER_DEFINED : actionId;
            mode = mode == null ? InventoryActionMode.EXECUTE : mode;
            origin = origin == null ? "" : origin;
        }
    }

    record ToolToggle(
            String toolId,
            InventoryToolToggleId toggleId,
            boolean desiredState,
            InventoryActionMode mode,
            String origin
    ) implements InventoryMutationIntent {
        public ToolToggle {
            toolId = toolId == null ? "" : toolId;
            toggleId = toggleId == null ? InventoryToolToggleId.PROVIDER_DEFINED : toggleId;
            mode = mode == null ? InventoryActionMode.EXECUTE : mode;
            origin = origin == null ? "" : origin;
        }
    }

    record CraftingPlaceSelected(
            String toolId,
            int inputIndex,
            CraftingPlacementMode placementMode,
            InventoryActionMode mode,
            String origin
    ) implements InventoryMutationIntent {
        public CraftingPlaceSelected {
            toolId = toolId == null ? "" : toolId;
            inputIndex = Math.max(0, inputIndex);
            placementMode = placementMode == null ? CraftingPlacementMode.STACK : placementMode;
            mode = mode == null ? InventoryActionMode.EXECUTE : mode;
            origin = origin == null ? "" : origin;
        }
    }

    record CraftingPlaceCursor(
            String toolId,
            int inputIndex,
            CraftingPlacementMode placementMode,
            InventoryActionMode mode,
            String origin
    ) implements InventoryMutationIntent {
        public CraftingPlaceCursor {
            toolId = toolId == null ? "" : toolId;
            inputIndex = Math.max(0, inputIndex);
            placementMode = placementMode == null ? CraftingPlacementMode.STACK : placementMode;
            mode = mode == null ? InventoryActionMode.EXECUTE : mode;
            origin = origin == null ? "" : origin;
        }
    }

    record CraftingDragCursor(
            String toolId,
            java.util.List<Integer> orderedInputIndices,
            CraftingDragMode dragMode,
            InventoryActionMode mode,
            String origin
    ) implements InventoryMutationIntent {
        public CraftingDragCursor {
            toolId = toolId == null ? "" : toolId;
            orderedInputIndices = orderedInputIndices == null
                    ? java.util.List.of()
                    : java.util.List.copyOf(orderedInputIndices.stream()
                    .filter(index -> index != null && index >= 0)
                    .distinct()
                    .toList());
            dragMode = dragMode == null ? CraftingDragMode.EVEN_SPLIT : dragMode;
            mode = mode == null ? InventoryActionMode.EXECUTE : mode;
            origin = origin == null ? "" : origin;
        }
    }

    record CraftingExtractResult(
            String toolId,
            CraftingResultMode resultMode,
            InventoryActionMode mode,
            String origin
    ) implements InventoryMutationIntent {
        public CraftingExtractResult {
            toolId = toolId == null ? "" : toolId;
            resultMode = resultMode == null ? CraftingResultMode.PICKUP : resultMode;
            mode = mode == null ? InventoryActionMode.EXECUTE : mode;
            origin = origin == null ? "" : origin;
        }
    }
}
