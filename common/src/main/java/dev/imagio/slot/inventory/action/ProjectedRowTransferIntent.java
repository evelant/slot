package dev.imagio.slot.inventory.action;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.ProjectedInventoryRow;
import dev.imagio.slot.workflow.domain.ProtectionPolicy;

import java.util.List;
import java.util.Objects;

public record ProjectedRowTransferIntent(
        InventoryAuthoritySnapshot authority,
        List<ProjectedInventoryRow> visibleRowsInUiOrder,
        ProjectedInventoryRow anchorRow,
        InventoryActionKind kind,
        InventoryActionQuantity quantity,
        InventoryActionScope scope,
        InventoryActionDestination destination,
        ProtectionPolicy protectionPolicy,
        InventoryActionMode mode,
        String origin
) {
    public ProjectedRowTransferIntent {
        authority = authority == null ? InventoryAuthoritySnapshot.empty() : authority;
        visibleRowsInUiOrder = visibleRowsInUiOrder == null
                ? List.of()
                : List.copyOf(visibleRowsInUiOrder.stream().filter(Objects::nonNull).toList());
        protectionPolicy = protectionPolicy == null ? ProtectionPolicy.allowAll() : protectionPolicy;
        kind = kind == null ? InventoryActionKind.TRANSFER : kind;
        quantity = quantity == null ? InventoryActionQuantity.STACK : quantity;
        mode = mode == null ? InventoryActionMode.EXECUTE : mode;
        origin = origin == null ? "" : origin;
    }

    public ItemIdentity anchorIdentity() {
        return anchorRow == null ? null : anchorRow.identity();
    }
}
