package dev.imagio.slot.inventory.session;

import dev.imagio.slot.inventory.action.InventoryActionKind;
import dev.imagio.slot.inventory.action.InventoryActionConflictPolicy;
import dev.imagio.slot.inventory.action.InventoryActionQuantity;
import dev.imagio.slot.inventory.action.InventoryActionRequest;
import dev.imagio.slot.inventory.action.InventoryActionScope;
import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.action.InventoryCommandReasonCode;
import dev.imagio.slot.inventory.browse.InventoryBrowseDocumentQueries;
import dev.imagio.slot.inventory.browse.InventoryBrowseEntry;
import dev.imagio.slot.inventory.browse.InventoryBrowseSubjectRef;
import dev.imagio.slot.inventory.core.HostInstanceKey;
import dev.imagio.slot.inventory.core.InventoryActionPolicy;
import dev.imagio.slot.inventory.core.InventoryCraftingSurfaceSupport;
import dev.imagio.slot.inventory.core.InventoryToolActionId;
import dev.imagio.slot.inventory.core.InventoryToolToggleId;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.core.ItemStackEquivalence;
import dev.imagio.slot.inventory.core.ServerMenuRef;
import dev.imagio.slot.inventory.intent.CraftingDragMode;
import dev.imagio.slot.inventory.intent.CraftingPlacementMode;
import dev.imagio.slot.inventory.intent.CraftingResultMode;
import dev.imagio.slot.inventory.query.InventoryAuthorityReadService;
import dev.imagio.slot.inventory.query.InventoryEntryKey;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.inventory.query.ProjectedEntryRef;
import dev.imagio.slot.workflow.domain.ProtectionPolicy;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class InventoryCraftingPlanner {
    private InventoryCraftingPlanner() {
    }

    public static InventoryCraftingPlan planSelectedPlacement(
            InventorySessionSnapshot session,
            InventoryCraftingSurfaceSupport.ResolvedCraftingSurface surface,
            int inputIndex,
            CraftingPlacementMode placementMode,
            dev.imagio.slot.inventory.action.InventoryActionMode mode,
            String origin
    ) {
        InventoryHostDescriptor host = host(session);
        if (host == null || surface == null || !surface.present()) {
            return InventoryCraftingPlan.rejected("crafting_surface_not_present", InventoryCommandReasonCode.INVALID_INTENT);
        }
        if (!surface.validInputIndex(inputIndex)) {
            return InventoryCraftingPlan.rejected("invalid_crafting_input_index", InventoryCommandReasonCode.INVALID_INTENT);
        }

        InventoryBrowseSubjectRef selectedSubject = session.workflow().browseSessionState().selectedSubject();
        if (!(selectedSubject instanceof InventoryBrowseSubjectRef.ItemRowRef itemRowRef)) {
            return InventoryCraftingPlan.rejected("selected_subject_not_item_row", InventoryCommandReasonCode.INVALID_INTENT);
        }
        InventoryBrowseEntry.ItemEntry selectedEntry = InventoryBrowseDocumentQueries.findItemEntry(session.browseDocument(), itemRowRef);
        if (selectedEntry == null) {
            return InventoryCraftingPlan.rejected("selected_subject_not_visible", InventoryCommandReasonCode.NOT_VISIBLE_IN_SCOPE);
        }
        ItemIdentity identity = selectedEntry.row().identity();
        if (identity == null) {
            return InventoryCraftingPlan.rejected("selected_row_missing_identity", InventoryCommandReasonCode.MISSING_IDENTITY);
        }

        SourceCandidate sourceCandidate = firstSourceCandidate(
                session,
                selectedEntry.row().backingEntries(),
                InventoryActionKind.TRANSFER,
                identity
        );
        if (sourceCandidate == null) {
            return InventoryCraftingPlan.rejected("no_backing_entries_for_selected_row", InventoryCommandReasonCode.NO_BACKING_ENTRIES);
        }

        InventoryActionTarget destinationTarget = surface.inputTarget(inputIndex);
        int requestedCount = transferableCount(session, destinationTarget, sourceCandidate.stack(), placementMode == CraftingPlacementMode.SINGLE);
        if (requestedCount <= 0) {
            return InventoryCraftingPlan.rejected("destination_full_for_crafting_input", InventoryCommandReasonCode.DESTINATION_FULL);
        }

        InventoryActionKind kind = InventoryActionKind.TRANSFER;
        InventoryActionQuantity quantity = placementMode == CraftingPlacementMode.SINGLE
                ? InventoryActionQuantity.ONE
                : InventoryActionQuantity.STACK;
        if (blockedByProtection(session, kind, destinationTarget, identity, sourceCandidate.stack())) {
            return InventoryCraftingPlan.rejected("destination_blocked_by_policy", InventoryCommandReasonCode.DESTINATION_BLOCKED_BY_POLICY);
        }

        InventoryActionRequest request = new InventoryActionRequest(
                host.hostId(),
                host.serverMenuRef(),
                "",
                kind,
                mode,
                quantity,
                InventoryActionScope.SINGLE_TARGET,
                InventoryActionConflictPolicy.INSERT_ONLY,
                normalizedOrigin(origin, "crafting_place_selected"),
                sourceCandidate.target(),
                destinationTarget,
                requestedCount,
                identity,
                stackWithCount(sourceCandidate.stack(), requestedCount),
                InventoryToolActionId.PROVIDER_DEFINED,
                InventoryToolToggleId.PROVIDER_DEFINED,
                false,
                ""
        );
        return new InventoryCraftingPlan(List.of(request), List.of(), "");
    }

    public static InventoryCraftingPlan planCursorPlacement(
            InventorySessionSnapshot session,
            InventoryCraftingSurfaceSupport.ResolvedCraftingSurface surface,
            int inputIndex,
            CraftingPlacementMode placementMode,
            dev.imagio.slot.inventory.action.InventoryActionMode mode,
            String origin
    ) {
        InventoryHostDescriptor host = host(session);
        if (host == null || surface == null || !surface.present()) {
            return InventoryCraftingPlan.rejected("crafting_surface_not_present", InventoryCommandReasonCode.INVALID_INTENT);
        }
        if (!surface.validInputIndex(inputIndex)) {
            return InventoryCraftingPlan.rejected("invalid_crafting_input_index", InventoryCommandReasonCode.INVALID_INTENT);
        }

        ItemStack cursorStack = session.authority().cursorState().stack();
        if (cursorStack == null || cursorStack.isEmpty()) {
            return InventoryCraftingPlan.rejected("cursor_stack_missing", InventoryCommandReasonCode.INVALID_INTENT);
        }

        InventoryActionTarget destinationTarget = surface.inputTarget(inputIndex);
        ItemIdentity identity = ItemIdentityMatcher.create(cursorStack);
        if (blockedByProtection(session, InventoryActionKind.CURSOR_PLACE, destinationTarget, identity, cursorStack)) {
            return InventoryCraftingPlan.rejected("destination_blocked_by_policy", InventoryCommandReasonCode.DESTINATION_BLOCKED_BY_POLICY);
        }

        int requestedCount = transferableCount(session, destinationTarget, cursorStack, placementMode == CraftingPlacementMode.SINGLE);
        if (requestedCount <= 0) {
            return InventoryCraftingPlan.rejected("destination_full_for_crafting_input", InventoryCommandReasonCode.DESTINATION_FULL);
        }

        InventoryActionRequest request = new InventoryActionRequest(
                host.hostId(),
                host.serverMenuRef(),
                "",
                InventoryActionKind.CURSOR_PLACE,
                mode,
                placementMode == CraftingPlacementMode.SINGLE ? InventoryActionQuantity.ONE : InventoryActionQuantity.STACK,
                InventoryActionScope.SINGLE_TARGET,
                InventoryActionConflictPolicy.INSERT_ONLY,
                normalizedOrigin(origin, "crafting_place_cursor"),
                destinationTarget,
                null,
                requestedCount,
                identity,
                stackWithCount(cursorStack, requestedCount),
                InventoryToolActionId.PROVIDER_DEFINED,
                InventoryToolToggleId.PROVIDER_DEFINED,
                false,
                ""
        );
        return new InventoryCraftingPlan(List.of(request), List.of(), "");
    }

    public static InventoryCraftingPlan planCursorDrag(
            InventorySessionSnapshot session,
            InventoryCraftingSurfaceSupport.ResolvedCraftingSurface surface,
            List<Integer> orderedInputIndices,
            CraftingDragMode dragMode,
            dev.imagio.slot.inventory.action.InventoryActionMode mode,
            String origin
    ) {
        InventoryHostDescriptor host = host(session);
        if (host == null || surface == null || !surface.present()) {
            return InventoryCraftingPlan.rejected("crafting_surface_not_present", InventoryCommandReasonCode.INVALID_INTENT);
        }
        if (orderedInputIndices == null || orderedInputIndices.isEmpty()) {
            return InventoryCraftingPlan.rejected("missing_drag_targets", InventoryCommandReasonCode.INVALID_INTENT);
        }

        ItemStack cursorStack = session.authority().cursorState().stack();
        if (cursorStack == null || cursorStack.isEmpty()) {
            return InventoryCraftingPlan.rejected("cursor_stack_missing", InventoryCommandReasonCode.INVALID_INTENT);
        }
        ItemIdentity identity = ItemIdentityMatcher.create(cursorStack);

        ArrayList<DragTarget> eligibleTargets = new ArrayList<>();
        for (int inputIndex : orderedInputIndices) {
            if (!surface.validInputIndex(inputIndex)) {
                continue;
            }
            InventoryActionTarget target = surface.inputTarget(inputIndex);
            if (target == null || blockedByProtection(session, InventoryActionKind.CURSOR_PLACE, target, identity, cursorStack)) {
                continue;
            }
            int capacity = transferableCount(session, target, cursorStack, false);
            if (capacity <= 0) {
                continue;
            }
            eligibleTargets.add(new DragTarget(inputIndex, target, capacity));
        }
        if (eligibleTargets.isEmpty()) {
            return InventoryCraftingPlan.rejected("no_destination_for_drag", InventoryCommandReasonCode.NO_DESTINATION_AVAILABLE);
        }

        int totalToDistribute = Math.min(cursorStack.getCount(), eligibleTargets.stream().mapToInt(DragTarget::capacity).sum());
        if (dragMode == CraftingDragMode.SINGLE_PER_SLOT) {
            totalToDistribute = Math.min(totalToDistribute, eligibleTargets.size());
        }
        int[] requestedCounts = distributeCounts(eligibleTargets, totalToDistribute);
        ArrayList<InventoryActionRequest> requests = new ArrayList<>();
        for (int index = 0; index < eligibleTargets.size(); index++) {
            int requestedCount = requestedCounts[index];
            if (requestedCount <= 0) {
                continue;
            }
            DragTarget target = eligibleTargets.get(index);
            requests.add(new InventoryActionRequest(
                    host.hostId(),
                    host.serverMenuRef(),
                    "",
                    InventoryActionKind.CURSOR_PLACE,
                    mode,
                    dragMode == CraftingDragMode.SINGLE_PER_SLOT
                            ? InventoryActionQuantity.SINGLE_PER_TARGET
                            : InventoryActionQuantity.EVEN_SPLIT,
                    InventoryActionScope.SELECTED_TARGETS,
                    InventoryActionConflictPolicy.INSERT_ONLY,
                    normalizedOrigin(origin, "crafting_drag_cursor"),
                    target.target(),
                    null,
                    requestedCount,
                    identity,
                    stackWithCount(cursorStack, requestedCount),
                    InventoryToolActionId.PROVIDER_DEFINED,
                    InventoryToolToggleId.PROVIDER_DEFINED,
                    false,
                    ""
            ));
        }
        return requests.isEmpty()
                ? InventoryCraftingPlan.rejected("no_destination_for_drag", InventoryCommandReasonCode.NO_DESTINATION_AVAILABLE)
                : new InventoryCraftingPlan(List.copyOf(requests), List.of(), "");
    }

    public static InventoryCraftingPlan planResultExtraction(
            InventorySessionSnapshot session,
            InventoryCraftingSurfaceSupport.ResolvedCraftingSurface surface,
            CraftingResultMode resultMode,
            dev.imagio.slot.inventory.action.InventoryActionMode mode,
            String origin
    ) {
        InventoryHostDescriptor host = host(session);
        if (host == null || surface == null || !surface.present()) {
            return InventoryCraftingPlan.rejected("crafting_surface_not_present", InventoryCommandReasonCode.INVALID_INTENT);
        }
        if (resultMode == CraftingResultMode.PICKUP && session.authority().cursorState().present()) {
            return InventoryCraftingPlan.rejected("cursor_not_empty_for_result_pickup", InventoryCommandReasonCode.INVALID_INTENT);
        }
        if (protectedCraftingInputs(session, surface)) {
            return InventoryCraftingPlan.rejected("source_inputs_blocked_by_policy", InventoryCommandReasonCode.SOURCE_BLOCKED_BY_POLICY);
        }

        InventoryActionTarget outputTarget = surface.outputTarget();
        InventoryEntrySnapshot outputEntry = InventoryAuthorityReadService.entrySnapshot(session.authority(), outputTarget);
        if (outputEntry == null || !outputEntry.present()) {
            return InventoryCraftingPlan.rejected("crafting_result_slot_empty", InventoryCommandReasonCode.NO_BACKING_ENTRIES);
        }

        InventoryActionRequest request = new InventoryActionRequest(
                host.hostId(),
                host.serverMenuRef(),
                "",
                resultMode == CraftingResultMode.QUICK_MOVE ? InventoryActionKind.QUICK_MOVE : InventoryActionKind.CURSOR_PICKUP,
                mode,
                InventoryActionQuantity.STACK,
                InventoryActionScope.SINGLE_TARGET,
                InventoryActionConflictPolicy.DEFAULT,
                normalizedOrigin(origin, "crafting_extract_result"),
                outputTarget,
                null,
                0,
                ItemIdentityMatcher.create(outputEntry.stack()),
                outputEntry.stack().copy(),
                InventoryToolActionId.PROVIDER_DEFINED,
                InventoryToolToggleId.PROVIDER_DEFINED,
                false,
                ""
        );
        return new InventoryCraftingPlan(List.of(request), List.of(), "");
    }

    static boolean protectedCraftingInputs(
            InventorySessionSnapshot session,
            InventoryCraftingSurfaceSupport.ResolvedCraftingSurface surface
    ) {
        if (surface == null || !surface.present()) {
            return false;
        }
        for (int inputIndex = 0; inputIndex < surface.inputCount(); inputIndex++) {
            InventoryActionTarget inputTarget = surface.inputTarget(inputIndex);
            InventoryEntrySnapshot inputEntry = InventoryAuthorityReadService.entrySnapshot(session.authority(), inputTarget);
            if (inputEntry == null || !inputEntry.present()) {
                continue;
            }
            if (blockedByProtection(
                    session,
                    InventoryActionKind.QUICK_MOVE,
                    inputTarget,
                    ItemIdentityMatcher.create(inputEntry.stack()),
                    inputEntry.stack()
            )) {
                return true;
            }
        }
        return false;
    }

    private static SourceCandidate firstSourceCandidate(
            InventorySessionSnapshot session,
            List<ProjectedEntryRef> backingEntries,
            InventoryActionKind kind,
            ItemIdentity identity
    ) {
        if (session == null || backingEntries == null || backingEntries.isEmpty()) {
            return null;
        }
        InventoryHostDescriptor host = host(session);
        ProtectionPolicy protection = protection(session);
        for (ProjectedEntryRef backingEntry : backingEntries) {
            if (backingEntry == null || backingEntry.stack() == null || backingEntry.stack().isEmpty() || backingEntry.count() <= 0) {
                continue;
            }
            InventoryActionTarget target = sourceTarget(backingEntry);
            if (target == null) {
                continue;
            }
            if (!InventoryActionPolicy.allows(host, kind, target, protection)
                    || blockedByProtection(session, kind, target, identity, backingEntry.stack())) {
                continue;
            }
            return new SourceCandidate(target, backingEntry.stack().copy(), backingEntry.count());
        }
        return null;
    }

    private static InventoryActionTarget sourceTarget(ProjectedEntryRef backingEntry) {
        if (backingEntry == null || backingEntry.entryKey() == null) {
            return null;
        }
        InventoryEntryKey key = backingEntry.entryKey();
        return key.kind() == InventoryEntryKey.Kind.PROVIDER_ENTRY
                ? new InventoryActionTarget.SourceEntryTarget(backingEntry.sourceId(), key.entryId())
                : new InventoryActionTarget.SourceSlotTarget(backingEntry.sourceId(), key.slotIndex());
    }

    private static int transferableCount(
            InventorySessionSnapshot session,
            InventoryActionTarget destinationTarget,
            ItemStack movingStack,
            boolean singleOnly
    ) {
        if (movingStack == null || movingStack.isEmpty()) {
            return 0;
        }
        InventoryEntrySnapshot destinationEntry = InventoryAuthorityReadService.entrySnapshot(session.authority(), destinationTarget);
        int maxAdditional;
        if (destinationEntry == null || !destinationEntry.present() || destinationEntry.stack().isEmpty()) {
            maxAdditional = movingStack.getMaxStackSize();
        } else if (ItemStackEquivalence.sameItemAndData(destinationEntry.stack(), movingStack)) {
            maxAdditional = Math.max(0, destinationEntry.stack().getMaxStackSize() - destinationEntry.count());
        } else {
            maxAdditional = 0;
        }
        if (maxAdditional <= 0) {
            return 0;
        }
        return singleOnly ? 1 : Math.min(movingStack.getCount(), maxAdditional);
    }

    private static int[] distributeCounts(
            List<DragTarget> eligibleTargets,
            int totalToDistribute
    ) {
        int[] counts = new int[eligibleTargets.size()];
        int remaining = Math.max(0, totalToDistribute);
        while (remaining > 0) {
            boolean progressed = false;
            for (int index = 0; index < eligibleTargets.size() && remaining > 0; index++) {
                if (counts[index] >= eligibleTargets.get(index).capacity()) {
                    continue;
                }
                counts[index]++;
                remaining--;
                progressed = true;
            }
            if (!progressed) {
                break;
            }
        }
        return counts;
    }

    private static boolean blockedByProtection(
            InventorySessionSnapshot session,
            InventoryActionKind kind,
            InventoryActionTarget target,
            ItemIdentity identity,
            ItemStack stack
    ) {
        InventoryHostDescriptor host = host(session);
        ProtectionPolicy protection = protection(session);
        return InventoryActionPolicy.blockedByProtection(kind, target, identity, stack, protection)
                || (target != null && protection.protectsTarget(target, kind))
                || (identity != null && protection.protects(identity, kind));
    }

    private static ProtectionPolicy protection(InventorySessionSnapshot session) {
        return session == null ? ProtectionPolicy.allowAll() : session.workflow().protection();
    }

    private static InventoryHostDescriptor host(InventorySessionSnapshot session) {
        if (session == null) {
            return null;
        }
        return session.host() != null ? session.host() : session.authority().host();
    }

    private static ItemStack stackWithCount(ItemStack stack, int requestedCount) {
        if (stack == null || stack.isEmpty() || requestedCount <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack copy = stack.copy();
        copy.setCount(Math.min(requestedCount, Math.max(1, copy.getMaxStackSize())));
        return copy;
    }

    private static String normalizedOrigin(String origin, String fallback) {
        return origin == null || origin.isBlank() ? fallback : origin;
    }

    private record SourceCandidate(
            InventoryActionTarget target,
            ItemStack stack,
            int availableCount
    ) {
    }

    private record DragTarget(
            int inputIndex,
            InventoryActionTarget target,
            int capacity
    ) {
    }
}
