package dev.imagio.slot;

import dev.imagio.slot.inventory.action.InventoryActionOutcome;
import dev.imagio.slot.inventory.action.InventoryActionRequest;
import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.integration.InventoryMutationMode;
import dev.imagio.slot.inventory.integration.InventoryMutationRequest;
import dev.imagio.slot.inventory.integration.MutationResult;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public final class SlotDiagnostics {
    private SlotDiagnostics() {
    }

    public static void hostResolved(String origin, InventoryHostDescriptor host) {
        if (!SlotDebugLog.enabled()) {
            return;
        }
        SlotCommon.LOGGER.info(
                "[SLOT] Host resolved origin={} host={} menu={} sources={} topology={} diagnostics='{}'",
                clean(origin),
                hostId(host),
                menuSummary(host == null ? null : host.menu()),
                sourceSummary(host),
                topologySummary(host),
                host == null ? "" : host.diagnostics()
        );
    }

    public static void workspaceTransferInvalid(
            Integer sourceKind,
            Integer sourceIndex,
            Integer destinationKind,
            Integer destinationIndex,
            String origin
    ) {
        SlotCommon.LOGGER.warn(
                "[SLOT] Workspace transfer rejected before host resolution: invalid_transfer_target sourceKind={} sourceIndex={} destinationKind={} destinationIndex={} origin={}",
                sourceKind,
                sourceIndex,
                destinationKind,
                destinationIndex,
                clean(origin)
        );
    }

    public static void workspaceTransferHostMissing(
            InventoryActionTarget source,
            InventoryActionTarget destination,
            AbstractContainerMenu menu
    ) {
        SlotCommon.LOGGER.warn(
                "[SLOT] Workspace transfer rejected: host_resolution_failed source={} destination={} menu={}",
                targetSummary(source),
                targetSummary(destination),
                menuSummary(menu)
        );
    }

    public static void workspaceTransferRequested(
            String origin,
            InventoryHostDescriptor host,
            InventoryActionTarget source,
            InventoryActionTarget destination,
            InventoryEntrySnapshot sourceEntry
    ) {
        SlotCommon.LOGGER.info(
                "[SLOT] Workspace transfer requested origin={} source={} destination={} sourceEntry={} host={} menu={} topology={} hostDiagnostics='{}'",
                clean(origin),
                targetSummary(source),
                targetSummary(destination),
                entrySummary(sourceEntry),
                hostId(host),
                menuSummary(host == null ? null : host.menu()),
                topologySummary(host),
                host == null ? "" : host.diagnostics()
        );
    }

    public static void workspaceTransferBuildRejected(
            String diagnostics,
            InventoryHostDescriptor host,
            InventoryActionTarget source,
            InventoryActionTarget destination,
            InventoryEntrySnapshot sourceEntry
    ) {
        SlotCommon.LOGGER.warn(
                "[SLOT] Workspace transfer rejected during request build: diagnostics={} source={} destination={} sourceEntry={} host={} menu={} topology={} hostDiagnostics='{}'",
                clean(diagnostics),
                targetSummary(source),
                targetSummary(destination),
                entrySummary(sourceEntry),
                hostId(host),
                menuSummary(host == null ? null : host.menu()),
                topologySummary(host),
                host == null ? "" : host.diagnostics()
        );
    }

    public static void workspaceTransferRequestBuilt(
            InventoryHostDescriptor host,
            InventoryActionRequest request
    ) {
        if (!SlotDebugLog.enabled()) {
            return;
        }
        SlotCommon.LOGGER.info(
                "[SLOT] Workspace transfer request built requestId={} origin={} source={} destination={} identity={} count={} stack={} host={} menu={}",
                request == null ? "" : request.requestId(),
                request == null ? "" : request.origin(),
                targetSummary(request == null ? null : request.primaryTarget()),
                targetSummary(request == null ? null : request.secondaryTarget()),
                request == null || request.identity() == null ? "" : request.identity().itemId(),
                request == null ? 0 : request.requestedCount(),
                request == null ? "empty" : stackSummary(request.stack()),
                hostId(host),
                menuSummary(host == null ? null : host.menu())
        );
    }

    public static void workspaceTransferExecuted(
            InventoryHostDescriptor host,
            InventoryActionRequest request,
            InventoryActionOutcome outcome,
            String feedbackStatus,
            String feedbackDiagnostics
    ) {
        boolean rejected = "transfer rejected".equals(feedbackStatus);
        String outcomeDiagnostics = outcome == null || outcome.diagnostics().isBlank()
                ? clean(feedbackDiagnostics)
                : outcome.diagnostics();
        if (rejected) {
            SlotCommon.LOGGER.warn(
                    "[SLOT] Workspace transfer rejected after execution: kind={} status={} feedback={} outcomeDiagnostics='{}' requestedCount={} appliedCount={} remainder={} source={} destination={} requestId={} origin={} host={} menu={} topology={} hostDiagnostics='{}'",
                    request == null ? "" : request.kind(),
                    outcome == null ? "missing_outcome" : outcome.status(),
                    clean(feedbackDiagnostics),
                    outcomeDiagnostics,
                    request == null ? 0 : request.requestedCount(),
                    outcome == null ? 0 : outcome.appliedCount(),
                    outcome == null ? "missing" : stackSummary(outcome.stackRemainder()),
                    targetSummary(request == null ? null : request.primaryTarget()),
                    targetSummary(request == null ? null : request.secondaryTarget()),
                    request == null ? "" : request.requestId(),
                    request == null ? "" : request.origin(),
                    hostId(host),
                    menuSummary(host == null ? null : host.menu()),
                    topologySummary(host),
                    host == null ? "" : host.diagnostics()
            );
            return;
        }

        SlotCommon.LOGGER.info(
                "[SLOT] Workspace transfer applied: kind={} status={} feedback={} requestedCount={} appliedCount={} remainder={} source={} destination={} requestId={} origin={} host={} menu={}",
                request == null ? "" : request.kind(),
                outcome == null ? "missing_outcome" : outcome.status(),
                clean(feedbackDiagnostics),
                request == null ? 0 : request.requestedCount(),
                outcome == null ? 0 : outcome.appliedCount(),
                outcome == null ? "missing" : stackSummary(outcome.stackRemainder()),
                targetSummary(request == null ? null : request.primaryTarget()),
                targetSummary(request == null ? null : request.secondaryTarget()),
                request == null ? "" : request.requestId(),
                request == null ? "" : request.origin(),
                hostId(host),
                menuSummary(host == null ? null : host.menu())
        );
    }

    public static void actionBlocked(
            InventoryHostDescriptor host,
            InventoryActionRequest request,
            InventoryActionOutcome outcome
    ) {
        if (request != null && request.mode() == dev.imagio.slot.inventory.action.InventoryActionMode.SIMULATE) {
            return;
        }
        SlotCommon.LOGGER.warn(
                "[SLOT] Inventory action blocked kind={} origin={} requestId={} source={} destination={} requestedCount={} status={} diagnostics='{}' remainder={} host={} menu={} topology={}",
                request == null ? "" : request.kind(),
                request == null ? "" : request.origin(),
                request == null ? "" : request.requestId(),
                targetSummary(request == null ? null : request.primaryTarget()),
                targetSummary(request == null ? null : request.secondaryTarget()),
                request == null ? 0 : request.requestedCount(),
                outcome == null ? "missing_outcome" : outcome.status(),
                outcome == null ? "" : outcome.diagnostics(),
                outcome == null ? "missing" : stackSummary(outcome.stackRemainder()),
                hostId(host),
                menuSummary(host == null ? null : host.menu()),
                topologySummary(host)
        );
    }

    public static void mutationRejected(
            String route,
            InventoryHostDescriptor host,
            InventorySourceDescriptor source,
            InventoryMutationRequest request,
            InventoryMutationMode mode,
            MutationResult result
    ) {
        if (mode == InventoryMutationMode.SIMULATE) {
            return;
        }
        SlotCommon.LOGGER.warn(
                "[SLOT] Inventory mutation rejected route={} mode={} kind={} source={} sourceRole={} targetSlot={} requestedCount={} stack={} diagnostics='{}' remainder={} host={} menu={} topology={}",
                clean(route),
                mode,
                request == null ? "" : request.kind(),
                request == null ? "" : request.sourceId(),
                source == null ? "" : source.role(),
                request == null ? -1 : request.slotIndex(),
                request == null ? 0 : request.requestedCount(),
                request == null ? "empty" : stackSummary(request.stack()),
                result == null ? "" : result.diagnostics(),
                result == null ? "missing" : stackSummary(result.stackRemainder()),
                hostId(host),
                menuSummary(host == null ? null : host.menu()),
                topologySummary(host)
        );
    }

    public static void mutationRouted(
            String route,
            InventoryHostDescriptor host,
            InventorySourceDescriptor source,
            InventoryMutationRequest request,
            InventoryMutationMode mode,
            MutationResult result
    ) {
        if (!SlotDebugLog.enabled()) {
            return;
        }
        SlotCommon.LOGGER.info(
                "[SLOT] Inventory mutation routed route={} mode={} kind={} source={} sourceRole={} targetSlot={} requestedCount={} stack={} successful={} remainder={} host={}",
                clean(route),
                mode,
                request == null ? "" : request.kind(),
                request == null ? "" : request.sourceId(),
                source == null ? "" : source.role(),
                request == null ? -1 : request.slotIndex(),
                request == null ? 0 : request.requestedCount(),
                request == null ? "empty" : stackSummary(request.stack()),
                result != null && result.successful(),
                result == null ? "missing" : stackSummary(result.stackRemainder()),
                hostId(host)
        );
    }

    public static String menuSummary(AbstractContainerMenu menu) {
        if (menu == null) {
            return "null";
        }
        return menu.getClass().getName() + "#" + menu.containerId + " slots=" + menu.slots.size();
    }

    public static String topologySummary(InventoryHostDescriptor host) {
        if (host == null || host.topology() == null) {
            return "missing";
        }
        return "mainSlots=" + host.topology().menuSlotsForSource(BuiltinInventoryIds.PLAYER_MAIN)
                + " hotbarSlots=" + host.topology().menuSlotsForSource(BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0);
    }

    public static String entrySummary(InventoryEntrySnapshot entry) {
        if (entry == null || !entry.present()) {
            return "empty";
        }
        return entry.sourceId() + ":" + entry.slotIndex() + " " + stackSummary(entry.stack()) + " count=" + entry.count();
    }

    public static String stackSummary(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "empty";
        }
        return ItemIdentityMatcher.create(stack).itemId() + "x" + stack.getCount();
    }

    private static String targetSummary(InventoryActionTarget target) {
        return target == null ? "" : target.stableKey();
    }

    private static String hostId(InventoryHostDescriptor host) {
        return host == null ? "missing" : String.valueOf(host.hostId());
    }

    private static String sourceSummary(InventoryHostDescriptor host) {
        if (host == null || host.sourceDescriptors().isEmpty()) {
            return "[]";
        }
        return host.sourceDescriptors().stream()
                .map(source -> source.id() + ":" + source.role() + ":" + source.bindingRoute())
                .toList()
                .toString();
    }

    private static String clean(String value) {
        return value == null ? "" : value;
    }
}
