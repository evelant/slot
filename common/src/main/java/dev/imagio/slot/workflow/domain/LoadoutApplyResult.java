package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.action.InventoryActionOutcome;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public record LoadoutApplyResult(
        String loadoutId,
        List<LoadoutTarget> requestedTargets,
        List<LoadoutTarget> satisfiedTargets,
        List<LoadoutTarget> missingTargets,
        Map<LoadoutTarget, InventoryActionOutcome> outcomesByTarget,
        List<String> diagnostics
) {
    public LoadoutApplyResult {
        loadoutId = loadoutId == null ? "" : loadoutId;
        requestedTargets = requestedTargets == null ? List.of() : List.copyOf(new LinkedHashSet<>(requestedTargets));
        satisfiedTargets = satisfiedTargets == null ? List.of() : List.copyOf(new LinkedHashSet<>(satisfiedTargets));
        missingTargets = missingTargets == null ? List.of() : List.copyOf(new LinkedHashSet<>(missingTargets));
        outcomesByTarget = outcomesByTarget == null ? Map.of() : Map.copyOf(new LinkedHashMap<>(outcomesByTarget));
        diagnostics = diagnostics == null ? List.of() : List.copyOf(diagnostics);
    }

    public static LoadoutApplyResult empty(String loadoutId) {
        return new LoadoutApplyResult(loadoutId, List.of(), List.of(), List.of(), Map.of(), List.of());
    }
}
