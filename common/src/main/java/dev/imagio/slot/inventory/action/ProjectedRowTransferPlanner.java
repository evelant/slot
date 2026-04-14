package dev.imagio.slot.inventory.action;

import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.query.ProjectedInventoryRow;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class ProjectedRowTransferPlanner {
    private ProjectedRowTransferPlanner() {
    }

    public static ProjectedRowTransferPlan plan(ProjectedRowTransferIntent intent) {
        List<String> diagnostics = new ArrayList<>();
        if (intent == null) {
            return ProjectedRowTransferPlan.empty(null, List.of("missing_intent"));
        }
        if (intent.authority() == null || intent.authority().host() == null) {
            return ProjectedRowTransferPlan.empty(intent, List.of("missing_authority"));
        }
        if (intent.visibleRowsInUiOrder().isEmpty()) {
            return ProjectedRowTransferPlan.empty(intent, List.of("missing_visible_rows"));
        }
        if (intent.destination() == null) {
            return ProjectedRowTransferPlan.empty(intent, List.of("missing_destination"));
        }
        if (!validCombination(intent.kind(), intent.scope())) {
            return ProjectedRowTransferPlan.empty(intent, List.of("invalid_kind_scope_combination"));
        }

        InventoryHostDescriptor host = intent.authority().host();
        PlannedAuthorityLedger ledger = new PlannedAuthorityLedger(intent.authority());
        List<ProjectedInventoryRow> selectedRows = ProjectedTransferSourceSelector.selectRows(intent, diagnostics);
        if (selectedRows.isEmpty()) {
            return ProjectedRowTransferPlan.empty(intent, List.copyOf(new LinkedHashSet<>(diagnostics)));
        }

        ArrayList<PlannedRowTransferOperation> operations = new ArrayList<>();
        ArrayList<ProjectedInventoryRow> blockedRows = new ArrayList<>();
        ArrayList<dev.imagio.slot.inventory.query.InventoryEntryKey> blockedEntries = new ArrayList<>();
        int requestedTotalCount = 0;
        int plannedTotalCount = 0;

        for (ProjectedInventoryRow row : selectedRows) {
            if (row == null || row.identity() == null) {
                if (row != null) {
                    blockedRows.add(row);
                }
                diagnostics.add("row_missing_identity");
                continue;
            }

            ArrayList<String> rowDiagnostics = new ArrayList<>();
            List<ProjectedTransferSourceCandidate> candidates = ProjectedTransferSourceSelector.candidates(
                    host,
                    row,
                    intent.kind(),
                    intent.protectionPolicy(),
                    rowDiagnostics,
                    blockedEntries
            );

            int rowRequestedCount = requestedCount(intent, row, candidates);
            int rowPlannedCount = 0;
            ArrayList<PlannedTransferStep> steps = new ArrayList<>();

            for (ProjectedTransferSourceCandidate candidate : candidates) {
                int desiredCount = requestedCount(intent.kind(), candidate.availableCount());
                if (desiredCount <= 0) {
                    continue;
                }
                ProjectedTransferDestinationAllocator.AllocationResult allocationResult = ProjectedTransferDestinationAllocator.allocate(
                        intent,
                        candidate,
                        desiredCount,
                        ledger
                );
                if (allocationResult.plannedCount() <= 0) {
                    blockedEntries.add(candidate.sourceEntry().entryKey());
                    rowDiagnostics.addAll(allocationResult.diagnostics());
                    rowDiagnostics.add("no_destination_for_entry:" + candidate.sourceEntry().entryKey().stableKey());
                    if (intent.scope() == InventoryActionScope.BEST_SINGLE_SOURCE) {
                        continue;
                    }
                    continue;
                }

                int remaining = desiredCount;
                for (ProjectedDestinationAllocation allocation : allocationResult.allocations()) {
                    if (allocation == null || allocation.acceptedCount() <= 0) {
                        continue;
                    }
                    int plannedCount = Math.min(remaining, allocation.acceptedCount());
                    if (plannedCount <= 0) {
                        continue;
                    }
                    ItemStack requestStack = candidate.sourceEntry().stack().copy();
                    requestStack.setCount(Math.min(plannedCount, Math.max(1, requestStack.getMaxStackSize())));
                    InventoryActionRequest request = new InventoryActionRequest(
                            host.hostId(),
                            host.serverMenuRef(),
                            UUID.randomUUID().toString(),
                            intent.kind(),
                            intent.mode(),
                            normalizedOrigin(intent.origin()),
                            candidate.sourceTarget(),
                            allocation.destinationTarget(),
                            plannedCount,
                            row.identity(),
                            requestStack,
                            dev.imagio.slot.inventory.core.InventoryToolActionId.PROVIDER_DEFINED,
                            dev.imagio.slot.inventory.core.InventoryToolToggleId.PROVIDER_DEFINED,
                            false,
                            allocation.diagnostics()
                    );
                    steps.add(new PlannedTransferStep(
                            candidate.sourceEntry(),
                            request,
                            stepStatus(plannedCount, plannedCount, allocation.capacityUncertain()),
                            InventoryCommandReasonCode.fromDiagnostics(
                                    allocation.diagnostics().isBlank() ? List.of() : List.of(allocation.diagnostics())
                            ),
                            plannedCount,
                            plannedCount,
                            allocation.capacityUncertain(),
                            allocation.diagnostics()
                    ));
                    remaining -= plannedCount;
                    rowPlannedCount += plannedCount;
                    plannedTotalCount += plannedCount;
                    ledger.noteExtraction(candidate.sourceEntry(), plannedCount);
                    if (remaining <= 0) {
                        break;
                    }
                }

                if (remaining > 0) {
                    blockedEntries.add(candidate.sourceEntry().entryKey());
                    rowDiagnostics.add("entry_partially_planned:" + candidate.sourceEntry().entryKey().stableKey());
                }
                if (intent.scope() == InventoryActionScope.BEST_SINGLE_SOURCE && !steps.isEmpty()) {
                    break;
                }
            }

            requestedTotalCount += rowRequestedCount;
            if (steps.isEmpty()) {
                blockedRows.add(row);
            }
            if (rowPlannedCount < rowRequestedCount && rowRequestedCount > 0) {
                rowDiagnostics.add("row_partially_planned:" + row.identity().itemId());
            }
            InventoryActionStatus rowStatus = planningStatus(rowRequestedCount, rowPlannedCount, steps.stream().anyMatch(PlannedTransferStep::capacityUncertain));
            operations.add(new PlannedRowTransferOperation(
                    row,
                    List.copyOf(steps),
                    rowStatus,
                    InventoryCommandReasonCode.fromDiagnostics(rowDiagnostics),
                    rowRequestedCount,
                    rowPlannedCount,
                    steps.stream().anyMatch(PlannedTransferStep::capacityUncertain),
                    List.copyOf(new LinkedHashSet<>(rowDiagnostics))
            ));
        }

        LinkedHashSet<String> planDiagnostics = new LinkedHashSet<>(diagnostics);
        for (PlannedRowTransferOperation operation : operations) {
            if (operation != null) {
                planDiagnostics.addAll(operation.diagnostics());
            }
        }

        InventoryActionStatus planStatus = planningStatus(
                requestedTotalCount,
                plannedTotalCount,
                operations.stream().anyMatch(PlannedRowTransferOperation::capacityUncertain)
        );

        return new ProjectedRowTransferPlan(
                intent,
                List.copyOf(operations),
                List.copyOf(new LinkedHashSet<>(blockedRows)),
                List.copyOf(new LinkedHashSet<>(blockedEntries)),
                planStatus,
                InventoryCommandReasonCode.fromDiagnostics(List.copyOf(planDiagnostics)),
                List.copyOf(planDiagnostics),
                requestedTotalCount,
                plannedTotalCount,
                operations.stream().anyMatch(PlannedRowTransferOperation::capacityUncertain)
        );
    }

    private static int requestedCount(ProjectedRowTransferIntent intent, ProjectedInventoryRow row, List<ProjectedTransferSourceCandidate> candidates) {
        if (intent == null || row == null) {
            return 0;
        }
        return switch (intent.scope()) {
            case BEST_SINGLE_SOURCE -> {
                ProjectedTransferSourceCandidate candidate = candidates.stream().filter(Objects::nonNull).findFirst().orElse(null);
                if (candidate != null) {
                    yield requestedCount(intent.kind(), candidate.availableCount());
                }
                int fallback = row.backingEntries().stream()
                        .filter(Objects::nonNull)
                        .mapToInt(entry -> entry.count())
                        .findFirst()
                        .orElse(0);
                yield requestedCount(intent.kind(), fallback);
            }
            case VISIBLE_MATCHES, VISIBLE_ROWS -> row.backingEntries().stream()
                    .filter(Objects::nonNull)
                    .mapToInt(entry -> Math.max(0, entry.count()))
                    .sum();
            case SOURCE_LOCAL -> 0;
        };
    }

    private static int requestedCount(InventoryActionKind kind, int availableCount) {
        if (availableCount <= 0) {
            return 0;
        }
        return switch (kind) {
            case TRANSFER_ONE -> 1;
            case TRANSFER_STACK, TRANSFER_ALL -> availableCount;
            default -> 0;
        };
    }

    private static boolean validCombination(InventoryActionKind kind, InventoryActionScope scope) {
        if (kind == null || scope == null) {
            return false;
        }
        return switch (kind) {
            case TRANSFER_ONE, TRANSFER_STACK -> scope == InventoryActionScope.BEST_SINGLE_SOURCE;
            case TRANSFER_ALL -> scope == InventoryActionScope.VISIBLE_MATCHES || scope == InventoryActionScope.VISIBLE_ROWS;
            default -> false;
        };
    }

    private static String normalizedOrigin(String origin) {
        return origin == null || origin.isBlank() ? "projected_row_transfer" : origin;
    }

    private static InventoryActionStatus stepStatus(int requestedCount, int plannedCount, boolean capacityUncertain) {
        return planningStatus(requestedCount, plannedCount, capacityUncertain);
    }

    private static InventoryActionStatus planningStatus(int requestedCount, int plannedCount, boolean capacityUncertain) {
        if (plannedCount <= 0 && requestedCount > 0) {
            return InventoryActionStatus.BLOCKED;
        }
        if (plannedCount <= 0) {
            return InventoryActionStatus.FAILED;
        }
        if (plannedCount < requestedCount || capacityUncertain) {
            return InventoryActionStatus.PARTIAL;
        }
        return InventoryActionStatus.SUCCESS;
    }
}
