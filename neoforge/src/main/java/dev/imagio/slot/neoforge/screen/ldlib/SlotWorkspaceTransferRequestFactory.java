package dev.imagio.slot.neoforge.screen.ldlib;

import dev.imagio.slot.inventory.action.InventoryActionKind;
import dev.imagio.slot.inventory.action.InventoryActionConflictPolicy;
import dev.imagio.slot.inventory.action.InventoryActionMode;
import dev.imagio.slot.inventory.action.InventoryActionQuantity;
import dev.imagio.slot.inventory.action.InventoryActionRequest;
import dev.imagio.slot.inventory.action.InventoryActionScope;
import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.query.InventoryAuthorityReadService;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

final class SlotWorkspaceTransferRequestFactory {
    private SlotWorkspaceTransferRequestFactory() {
    }

    static BuildResult build(
            InventoryHostDescriptor host,
            InventoryAuthoritySnapshot authority,
            InventoryActionTarget source,
            InventoryActionTarget destination,
            String origin
    ) {
        if (host == null || authority == null) {
            return BuildResult.rejected("missing_host_or_authority");
        }
        if (source == null || destination == null) {
            return BuildResult.rejected("missing_transfer_target");
        }

        InventoryEntrySnapshot sourceEntry = InventoryAuthorityReadService.entrySnapshot(authority, source);
        if (sourceEntry == null || !sourceEntry.present()) {
            return BuildResult.rejected("empty_source");
        }

        ItemStack stack = sourceEntry.stack().copy();
        stack.setCount(sourceEntry.count());
        ItemIdentity identity = ItemIdentityMatcher.create(stack);
        String requestId = UUID.randomUUID().toString();
        String resolvedOrigin = origin == null || origin.isBlank() ? "slot_workspace.ldlib.hotbar_transfer" : origin;
        InventoryActionKind kind = actionKind(authority, source, destination);
        InventoryActionConflictPolicy conflictPolicy = kind == InventoryActionKind.ASSIGN
                ? InventoryActionConflictPolicy.ASSIGN_WITH_DISPLACE
                : InventoryActionConflictPolicy.INSERT_ONLY;

        return BuildResult.dispatchable(new InventoryActionRequest(
                host.hostId(),
                host.serverMenuRef(),
                requestId,
                kind,
                InventoryActionMode.EXECUTE,
                InventoryActionQuantity.STACK,
                InventoryActionScope.SINGLE_TARGET,
                conflictPolicy,
                resolvedOrigin,
                requestId,
                "",
                "slot_workspace.ldlib",
                source,
                destination,
                sourceEntry.count(),
                identity,
                stack,
                null,
                null,
                false,
                ""
        ));
    }

    private static InventoryActionKind actionKind(
            InventoryAuthoritySnapshot authority,
            InventoryActionTarget source,
            InventoryActionTarget destination
    ) {
        return quickAccessAssignment(authority, source, destination)
                ? InventoryActionKind.ASSIGN
                : InventoryActionKind.TRANSFER;
    }

    private static boolean quickAccessAssignment(
            InventoryAuthoritySnapshot authority,
            InventoryActionTarget source,
            InventoryActionTarget destination
    ) {
        if (!exactSlotTarget(source) || !(destination instanceof InventoryActionTarget.QuickAccessTarget)) {
            return false;
        }
        String sourceId = InventoryAuthorityReadService.sourceId(authority.host(), source);
        String destinationId = InventoryAuthorityReadService.sourceId(authority.host(), destination);
        return (BuiltinInventoryIds.PLAYER_MAIN.equals(sourceId)
                || BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0.equals(sourceId))
                && BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0.equals(destinationId);
    }

    private static boolean exactSlotTarget(InventoryActionTarget target) {
        return target instanceof InventoryActionTarget.SourceSlotTarget
                || target instanceof InventoryActionTarget.QuickAccessTarget;
    }

    record BuildResult(
            InventoryActionRequest request,
            String diagnostics
    ) {
        static BuildResult dispatchable(InventoryActionRequest request) {
            return new BuildResult(request, "");
        }

        static BuildResult rejected(String diagnostics) {
            return new BuildResult(null, diagnostics == null ? "rejected" : diagnostics);
        }

        boolean dispatchable() {
            return request != null;
        }
    }
}
