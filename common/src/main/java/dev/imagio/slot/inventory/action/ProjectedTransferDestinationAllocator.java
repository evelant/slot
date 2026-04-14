package dev.imagio.slot.inventory.action;

import dev.imagio.slot.inventory.core.InventoryActionPolicy;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceRole;
import dev.imagio.slot.inventory.integration.InventoryMutationMode;
import dev.imagio.slot.inventory.integration.InventoryMutationRequest;
import dev.imagio.slot.inventory.integration.InventoryMutationRouter;
import dev.imagio.slot.inventory.integration.MutationResult;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

final class ProjectedTransferDestinationAllocator {
    private ProjectedTransferDestinationAllocator() {
    }

    static AllocationResult allocate(
            ProjectedRowTransferIntent intent,
            ProjectedTransferSourceCandidate sourceCandidate,
            int requestedCount,
            PlannedAuthorityLedger ledger
    ) {
        if (intent == null
                || intent.authority() == null
                || intent.authority().host() == null
                || sourceCandidate == null
                || requestedCount <= 0) {
            return AllocationResult.empty();
        }

        InventoryHostDescriptor host = intent.authority().host();
        List<InventorySourceDescriptor> destinationSources = destinationSources(intent, sourceCandidate, ledger);
        if (destinationSources.isEmpty()) {
            return new AllocationResult(List.of(), 0, requestedCount, List.of("no_destination_sources:" + intent.destination().stableKey()));
        }

        LinkedHashSet<String> diagnostics = new LinkedHashSet<>();
        ArrayList<ProjectedDestinationAllocation> allocations = new ArrayList<>();
        int remaining = requestedCount;
        for (InventorySourceDescriptor destinationSource : destinationSources) {
            if (remaining <= 0) {
                break;
            }
            InventoryActionTarget destinationTarget = new InventoryActionTarget.SourceTarget(destinationSource.id());
            if (!destinationSource.supports(dev.imagio.slot.inventory.core.InventoryCapability.INSERT)
                    || !InventoryActionPolicy.allows(host, intent.kind(), destinationTarget, intent.protectionPolicy())) {
                diagnostics.add("destination_blocked_by_policy:" + destinationSource.id());
                continue;
            }
            ProjectedDestinationAllocation allocation = allocateIntoSource(
                    host,
                    sourceCandidate,
                    destinationSource,
                    remaining,
                    ledger
            );
            if (allocation.acceptedCount() <= 0) {
                if (!allocation.diagnostics().isBlank()) {
                    diagnostics.add(allocation.diagnostics());
                }
                continue;
            }
            allocations.add(allocation);
            if (!allocation.diagnostics().isBlank()) {
                diagnostics.add(allocation.diagnostics());
            }
            remaining -= allocation.acceptedCount();
        }

        return new AllocationResult(
                List.copyOf(allocations),
                requestedCount - remaining,
                remaining,
                List.copyOf(diagnostics)
        );
    }

    private static ProjectedDestinationAllocation allocateIntoSource(
            InventoryHostDescriptor host,
            ProjectedTransferSourceCandidate sourceCandidate,
            InventorySourceDescriptor destinationSource,
            int requestedCount,
            PlannedAuthorityLedger ledger
    ) {
        InventoryActionTarget destinationTarget = new InventoryActionTarget.SourceTarget(destinationSource.id());
        ItemStack requestStack = sourceCandidate.sourceEntry().stack().copy();
        requestStack.setCount(Math.min(Math.max(1, requestedCount), Math.max(1, requestStack.getMaxStackSize())));

        if (!destinationSource.providerBacked()) {
            int accepted = ledger.acceptIntoSource(destinationSource.id(), sourceCandidate.identity(), requestStack, requestedCount);
            return new ProjectedDestinationAllocation(destinationTarget, accepted, false, accepted <= 0 ? "destination_full:" + destinationSource.id() : "");
        }

        if (!destinationSource.simulationSupported()) {
            ledger.noteProviderInsert(destinationSource.id(), sourceCandidate.identity(), requestedCount);
            return new ProjectedDestinationAllocation(
                    destinationTarget,
                    requestedCount,
                    true,
                    "provider_destination_capacity_uncertain:" + destinationSource.id()
            );
        }

        MutationResult result = InventoryMutationRouter.mutate(
                host,
                InventoryMutationRequest.insert(host, null, destinationSource.id(), requestStack),
                InventoryMutationMode.SIMULATE
        );
        int accepted = requestStack.getCount() - (result == null || result.stackRemainder() == null ? 0 : result.stackRemainder().getCount());
        if (accepted > 0) {
            ledger.noteProviderInsert(destinationSource.id(), sourceCandidate.identity(), accepted);
            return new ProjectedDestinationAllocation(destinationTarget, accepted, !result.successful(), result.successful() ? "" : result.diagnostics());
        }
        if (simulationUnavailable(result)) {
            ledger.noteProviderInsert(destinationSource.id(), sourceCandidate.identity(), requestedCount);
            return new ProjectedDestinationAllocation(
                    destinationTarget,
                    requestedCount,
                    true,
                    "provider_destination_capacity_uncertain:" + destinationSource.id()
            );
        }
        return new ProjectedDestinationAllocation(destinationTarget, 0, false, result == null ? "destination_full:" + destinationSource.id() : result.diagnostics());
    }

    private static List<InventorySourceDescriptor> destinationSources(
            ProjectedRowTransferIntent intent,
            ProjectedTransferSourceCandidate sourceCandidate,
            PlannedAuthorityLedger ledger
    ) {
        if (intent == null || intent.authority() == null || intent.authority().host() == null || intent.destination() == null) {
            return List.of();
        }
        List<InventorySourceDescriptor> baseSources = switch (intent.destination()) {
            case InventoryActionDestination.SourceDestination sourceDestination -> {
                InventorySourceDescriptor source = intent.authority().host().source(sourceDestination.sourceId());
                yield source == null ? List.of() : List.of(source);
            }
            case InventoryActionDestination.PaneDestination paneDestination -> paneSources(intent.authority().host(), paneDestination.paneMembership());
        };
        ArrayList<InventorySourceDescriptor> matching = new ArrayList<>();
        ArrayList<InventorySourceDescriptor> others = new ArrayList<>();
        for (InventorySourceDescriptor source : baseSources) {
            if (source == null
                    || !source.supports(dev.imagio.slot.inventory.core.InventoryCapability.INSERT)
                    || source.id().equals(sourceCandidate.source().id())) {
                continue;
            }
            if (ledger.sourceContainsIdentity(source.id(), sourceCandidate.identity())) {
                matching.add(source);
            } else {
                others.add(source);
            }
        }
        ArrayList<InventorySourceDescriptor> ordered = new ArrayList<>(matching);
        ordered.addAll(others);
        return List.copyOf(ordered);
    }

    private static List<InventorySourceDescriptor> paneSources(
            InventoryHostDescriptor host,
            InventoryPaneMembership paneMembership
    ) {
        if (host == null || (paneMembership != InventoryPaneMembership.CARRIED && paneMembership != InventoryPaneMembership.EXTERNAL)) {
            return List.of();
        }
        return host.sourceDescriptors().stream()
                .filter(source -> source != null && source.paneMembership() == paneMembership)
                .sorted((left, right) -> Integer.compare(left.stableOrder(), right.stableOrder()))
                .filter(source -> paneMembership != InventoryPaneMembership.CARRIED || genericCarriedDestination(source))
                .toList();
    }

    private static boolean genericCarriedDestination(InventorySourceDescriptor source) {
        if (source == null) {
            return false;
        }
        return source.role() != InventorySourceRole.QUICK_ACCESS
                && source.role() != InventorySourceRole.EQUIPMENT
                && source.role() != InventorySourceRole.OFFHAND;
    }

    private static boolean simulationUnavailable(MutationResult result) {
        if (result == null || result.diagnostics() == null || result.diagnostics().isBlank()) {
            return true;
        }
        return result.diagnostics().contains("simulation_not_supported")
                || result.diagnostics().contains("provider_does_not_support_simulation")
                || result.diagnostics().contains("unsupported_source");
    }

    record AllocationResult(
            List<ProjectedDestinationAllocation> allocations,
            int plannedCount,
            int remainderCount,
            List<String> diagnostics
    ) {
        AllocationResult {
            allocations = allocations == null ? List.of() : List.copyOf(allocations);
            plannedCount = Math.max(0, plannedCount);
            remainderCount = Math.max(0, remainderCount);
            diagnostics = diagnostics == null ? List.of() : List.copyOf(diagnostics);
        }

        static AllocationResult empty() {
            return new AllocationResult(List.of(), 0, 0, List.of());
        }
    }
}
