package dev.imagio.slot.inventory.intent;

import dev.imagio.slot.inventory.action.InventoryActionMode;
import dev.imagio.slot.inventory.action.InventoryActionRequest;
import dev.imagio.slot.inventory.action.ProjectedRowTransferIntent;

public sealed interface InventoryMutationIntent extends InventoryIntent permits
        InventoryMutationIntent.ExecuteRequest,
        InventoryMutationIntent.ProjectedRowTransfer,
        InventoryMutationIntent.TrashEntry,
        InventoryMutationIntent.VoidEntry,
        InventoryMutationIntent.ToolControl,
        InventoryMutationIntent.CraftingSurface {

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

    record ToolControl(
            String toolId,
            String controlId,
            InventoryActionMode mode,
            String origin
    ) implements InventoryMutationIntent {
        public ToolControl {
            toolId = toolId == null ? "" : toolId;
            controlId = controlId == null ? "" : controlId;
            mode = mode == null ? InventoryActionMode.EXECUTE : mode;
            origin = origin == null ? "" : origin;
        }
    }

    record CraftingSurface(
            String toolId,
            String surfaceId,
            InventoryActionMode mode,
            String origin
    ) implements InventoryMutationIntent {
        public CraftingSurface {
            toolId = toolId == null ? "" : toolId;
            surfaceId = surfaceId == null ? "" : surfaceId;
            mode = mode == null ? InventoryActionMode.EXECUTE : mode;
            origin = origin == null ? "" : origin;
        }
    }
}
