package dev.imagio.slot.inventory.session;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.workflow.domain.InventoryActivityConfidence;
import dev.imagio.slot.workflow.domain.InventoryActivityEvent;
import dev.imagio.slot.workflow.domain.InventoryActivityKind;
import dev.imagio.slot.workflow.domain.InventoryActivityProducer;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class InventoryAcquisitionActivityRecorder {
    private InventoryAcquisitionActivityRecorder() {
    }

    public static boolean recordStackAcquired(
            WorkflowDomainRuntime runtime,
            ItemStack stack,
            int count,
            InventoryActivityProducer producer,
            InventoryActivityConfidence confidence,
            String diagnostics
    ) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return recordIdentityAcquired(
                runtime,
                ItemIdentityMatcher.create(stack),
                count,
                producer,
                confidence,
                diagnostics);
    }

    public static boolean recordIdentityAcquired(
            WorkflowDomainRuntime runtime,
            ItemIdentity identity,
            int count,
            InventoryActivityProducer producer,
            InventoryActivityConfidence confidence,
            String diagnostics
    ) {
        if (runtime == null || identity == null || count <= 0) {
            return false;
        }
        return runtime.recordActivityEvent(new InventoryActivityEvent(
                InventoryActivityKind.ACQUIRED,
                producer == null ? InventoryActivityProducer.UNKNOWN_EXTERNAL : producer,
                confidence == null ? InventoryActivityConfidence.OBSERVED : confidence,
                identity,
                count,
                null,
                null,
                "",
                "",
                List.of(),
                diagnostics == null ? "" : diagnostics
        ));
    }
}
