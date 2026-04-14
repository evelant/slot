package dev.imagio.slot.inventory.action;

import dev.imagio.slot.inventory.core.InventoryActionPolicy;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.query.ProjectedEntryRef;
import dev.imagio.slot.inventory.query.ProjectedInventoryRow;
import dev.imagio.slot.workflow.domain.ProtectionPolicy;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

final class ProjectedTransferSourceSelector {
    private ProjectedTransferSourceSelector() {
    }

    static List<ProjectedInventoryRow> selectRows(ProjectedRowTransferIntent intent, List<String> diagnostics) {
        if (intent == null) {
            return List.of();
        }
        List<ProjectedInventoryRow> visibleRows = intent.visibleRowsInUiOrder();
        if (visibleRows.isEmpty()) {
            return List.of();
        }
        ProjectedInventoryRow visibleAnchor = resolveVisibleAnchor(intent);
        return switch (intent.scope()) {
            case BEST_SINGLE_SOURCE -> {
                if (visibleAnchor == null) {
                    addDiagnostic(diagnostics, "anchor_row_not_visible");
                    yield List.of();
                }
                yield List.of(visibleAnchor);
            }
            case VISIBLE_MATCHES -> {
                if (visibleAnchor == null || visibleAnchor.identity() == null) {
                    addDiagnostic(diagnostics, visibleAnchor == null ? "anchor_row_not_visible" : "anchor_row_missing_identity");
                    yield List.of();
                }
                ItemIdentity anchorIdentity = visibleAnchor.identity();
                yield visibleRows.stream()
                        .filter(Objects::nonNull)
                        .filter(row -> row.identity() != null)
                        .filter(row -> dev.imagio.slot.inventory.core.ItemIdentityMatcher.matchesMovable(row.identity(), anchorIdentity))
                        .toList();
            }
            case VISIBLE_ROWS -> List.copyOf(visibleRows);
            case SOURCE_LOCAL -> {
                addDiagnostic(diagnostics, "source_local_scope_not_supported_by_row_transfer_planner");
                yield List.of();
            }
        };
    }

    static List<ProjectedTransferSourceCandidate> candidates(
            InventoryHostDescriptor host,
            ProjectedInventoryRow row,
            InventoryActionKind kind,
            ProtectionPolicy protectionPolicy,
            List<String> diagnostics,
            List<dev.imagio.slot.inventory.query.InventoryEntryKey> blockedEntries
    ) {
        if (host == null || row == null || row.backingEntries().isEmpty()) {
            return List.of();
        }
        ArrayList<ProjectedTransferSourceCandidate> candidates = new ArrayList<>();
        ProtectionPolicy resolvedProtection = protectionPolicy == null ? ProtectionPolicy.allowAll() : protectionPolicy;
        for (ProjectedEntryRef entry : row.backingEntries()) {
            if (entry == null || entry.count() <= 0 || entry.stack() == null || entry.stack().isEmpty()) {
                continue;
            }
            InventorySourceDescriptor source = host.source(entry.sourceId());
            if (source == null || !source.supports(dev.imagio.slot.inventory.core.InventoryCapability.EXTRACT)) {
                addBlocked(blockedEntries, entry);
                addDiagnostic(diagnostics, "source_entry_not_extractable:" + entry.entryKey().stableKey());
                continue;
            }
            InventoryActionTarget sourceTarget = entry.slotBacked()
                    ? new InventoryActionTarget.SourceSlotTarget(entry.sourceId(), entry.entryKey().slotIndex())
                    : new InventoryActionTarget.SourceEntryTarget(entry.sourceId(), entry.entryKey().entryId());
            if (!InventoryActionPolicy.allows(host, kind, sourceTarget, resolvedProtection)
                    || InventoryActionPolicy.blockedByProtection(kind, sourceTarget, row.identity(), entry.stack(), resolvedProtection)) {
                addBlocked(blockedEntries, entry);
                addDiagnostic(diagnostics, "source_entry_blocked_by_policy:" + entry.entryKey().stableKey());
                continue;
            }
            candidates.add(new ProjectedTransferSourceCandidate(
                    row,
                    entry,
                    source,
                    sourceTarget,
                    row.identity(),
                    entry.count()
            ));
        }
        return List.copyOf(candidates);
    }

    private static ProjectedInventoryRow resolveVisibleAnchor(ProjectedRowTransferIntent intent) {
        if (intent == null || intent.anchorRow() == null) {
            return null;
        }
        for (ProjectedInventoryRow row : intent.visibleRowsInUiOrder()) {
            if (sameRow(row, intent.anchorRow())) {
                return row;
            }
        }
        return null;
    }

    private static boolean sameRow(ProjectedInventoryRow left, ProjectedInventoryRow right) {
        if (left == null || right == null) {
            return false;
        }
        return Objects.equals(left.identity(), right.identity())
                && Objects.equals(new LinkedHashSet<>(left.backingEntryKeys()), new LinkedHashSet<>(right.backingEntryKeys()));
    }

    private static void addBlocked(List<dev.imagio.slot.inventory.query.InventoryEntryKey> blockedEntries, ProjectedEntryRef entry) {
        if (blockedEntries != null && entry != null) {
            blockedEntries.add(entry.entryKey());
        }
    }

    private static void addDiagnostic(List<String> diagnostics, String diagnostic) {
        if (diagnostics != null && diagnostic != null && !diagnostic.isBlank()) {
            diagnostics.add(diagnostic);
        }
    }
}
