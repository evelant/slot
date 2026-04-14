package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.action.InventoryActionOutcome;
import dev.imagio.slot.inventory.action.InventoryActionRequest;

import java.util.Objects;
import java.util.function.Function;

public final class LoadoutApplyExecutor {
    private final Function<InventoryActionRequest, InventoryActionOutcome> actionExecutor;

    public LoadoutApplyExecutor(Function<InventoryActionRequest, InventoryActionOutcome> actionExecutor) {
        this.actionExecutor = Objects.requireNonNull(actionExecutor, "actionExecutor");
    }

    public LoadoutApplyResult execute(LoadoutApplyService.LoadoutApplyPlan plan) {
        return LoadoutApplyService.execute(plan, actionExecutor);
    }
}
