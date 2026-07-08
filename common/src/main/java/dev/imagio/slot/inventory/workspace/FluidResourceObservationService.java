package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.SlotResourceCollections;
import dev.imagio.slot.inventory.core.SlotResourceIdentity;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;

import java.util.LinkedHashMap;
import java.util.Map;

public final class FluidResourceObservationService {
    private FluidResourceObservationService() {
    }

    public static boolean observe(
            WorkflowDomainRuntime runtime,
            WorkspaceStorageIndex storageIndex,
            Map<SlotResourceIdentity, Long> carriedFluidCounts,
            String diagnostics
    ) {
        if (runtime == null) {
            return false;
        }
        return runtime.observeFluidResourceCounts(
                aggregate(storageIndex, carriedFluidCounts),
                diagnostics == null || diagnostics.isBlank()
                        ? "workspace_projection_fluid_observation"
                        : diagnostics);
    }

    public static Map<SlotResourceIdentity, Long> aggregate(
            WorkspaceStorageIndex storageIndex,
            Map<SlotResourceIdentity, Long> carriedFluidCounts
    ) {
        LinkedHashMap<SlotResourceIdentity, Long> counts = new LinkedHashMap<>();
        Map<SlotResourceIdentity, Long> carried = SlotResourceCollections.normalizeAmounts(carriedFluidCounts);
        if (!carried.isEmpty()) {
            merge(counts, carried);
        } else if (storageIndex != null) {
            merge(counts, storageIndex.carriedFluidCountsByIdentity());
        }
        if (storageIndex != null) {
            merge(counts, storageIndex.liveWorldFluidCountsByIdentity());
            merge(counts, storageIndex.rememberedWorldFluidCountsByIdentity());
        }
        return SlotResourceCollections.normalizeAmounts(counts);
    }

    private static void merge(
            Map<SlotResourceIdentity, Long> target,
            Map<SlotResourceIdentity, Long> source
    ) {
        if (target == null || source == null || source.isEmpty()) {
            return;
        }
        for (Map.Entry<SlotResourceIdentity, Long> entry : source.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null && entry.getKey().fluid()) {
                SlotResourceCollections.mergeAmount(target, entry.getKey(), entry.getValue());
            }
        }
    }
}
