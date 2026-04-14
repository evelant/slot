package dev.imagio.slot.inventory.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public final class ProjectedRowTransferExecutor {
    private final Function<InventoryActionRequest, InventoryActionOutcome> actionExecutor;

    public ProjectedRowTransferExecutor(Function<InventoryActionRequest, InventoryActionOutcome> actionExecutor) {
        this.actionExecutor = Objects.requireNonNull(actionExecutor, "actionExecutor");
    }

    public List<InventoryActionOutcome> execute(ProjectedRowTransferPlan plan) {
        if (plan == null) {
            return List.of();
        }
        ArrayList<InventoryActionOutcome> outcomes = new ArrayList<>();
        for (InventoryActionRequest request : plan.requests()) {
            if (request != null) {
                outcomes.add(actionExecutor.apply(request));
            }
        }
        return List.copyOf(outcomes);
    }
}
