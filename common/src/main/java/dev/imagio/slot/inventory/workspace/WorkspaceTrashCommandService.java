package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.inventory.action.InventoryActionKind;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.inventory.storage.CarriedSourceAccess;
import dev.imagio.slot.inventory.storage.StorageAccessRegistry;
import dev.imagio.slot.workflow.domain.DomainEventMetadata;
import dev.imagio.slot.workflow.domain.InventoryActivityConfidence;
import dev.imagio.slot.workflow.domain.InventoryActivityEvent;
import dev.imagio.slot.workflow.domain.InventoryActivityKind;
import dev.imagio.slot.workflow.domain.InventoryActivityProducer;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Common implementation for junk tagging and destructive carried-item cleanup.
 * Platform UIs provide only an identity; all live matching, protection checks,
 * extraction, undo restore, and pickup-overflow behavior stays here.
 */
public final class WorkspaceTrashCommandService {
    private static final int OVERFLOW_THRESHOLD_NUMERATOR = 3;
    private static final int OVERFLOW_THRESHOLD_DENOMINATOR = 4;

    private WorkspaceTrashCommandService() {
    }

    public static WorkspaceCommandOutcome setJunk(
            WorkflowDomainRuntime runtime,
            ItemIdentity identity,
            boolean marked
    ) {
        if (runtime == null) {
            return WorkspaceCommandOutcome.rejected("invalid_junk_runtime");
        }
        if (identity == null) {
            return WorkspaceCommandOutcome.rejected("invalid_identity");
        }
        runtime.collectionWorkflow().expireJunkTags();
        boolean changed = runtime.collectionWorkflow().setJunk(
                identity,
                marked,
                DomainEventMetadata.origin(marked
                        ? "slot_workspace.junk.mark"
                        : "slot_workspace.junk.unmark"));
        if (!changed) {
            return WorkspaceCommandOutcome.accepted("junk unchanged", identity.itemId());
        }
        return WorkspaceCommandOutcome.accepted(marked ? "junk marked" : "junk unmarked", identity.itemId());
    }

    public static WorkspaceCommandOutcome trashCarriedIdentity(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            ItemIdentity identity
    ) {
        if (player == null) {
            return WorkspaceCommandOutcome.rejected("invalid_player");
        }
        if (runtime == null) {
            return WorkspaceCommandOutcome.rejected("invalid_trash_runtime");
        }
        if (identity == null) {
            return WorkspaceCommandOutcome.rejected("invalid_identity");
        }
        if (runtime.protection().protects(identity, InventoryActionKind.TRASH)) {
            return WorkspaceCommandOutcome.rejected("trash_protected_target");
        }
        CarriedSourceAccess carried = carriedAccessOrNull();
        if (carried == null) {
            return WorkspaceCommandOutcome.rejected("carried_storage_unavailable");
        }

        runtime.collectionWorkflow().expireJunkTags();
        boolean wasJunk = runtime.collectionWorkflow().isJunk(identity);
        ExtractedBatch trashed;
        try {
            trashed = extractMatching(carried, player, identity, Integer.MAX_VALUE, false);
        } catch (RuntimeException exception) {
            SlotCommon.LOGGER.warn(
                    "[SLOT] trash rejected: carried extraction failed item={} player={} error={}",
                    identity.itemId(),
                    player.getName().getString(),
                    exception.toString());
            return WorkspaceCommandOutcome.rejected("trash_extract_failed");
        }
        if (trashed.count() <= 0) {
            return WorkspaceCommandOutcome.rejected("no_matching_carried_items");
        }

        runtime.collectionWorkflow().setJunk(
                identity,
                true,
                DomainEventMetadata.origin("slot_workspace.trash.mark_junk"));
        recordTrashActivity(runtime, identity, trashed.count(), InventoryActivityProducer.ROUTER_ACTION, true);
        recordUndo(player, runtime, identity, trashed, wasJunk);
        return WorkspaceCommandOutcome.accepted("trashed", identity.itemId() + " count=" + trashed.count());
    }

    /**
     * Delete newly picked-up junk when carried storage is under pressure. The
     * caller supplies the authoritative pickup count; this method removes at
     * most that many matching items from carried storage and prefers builtin
     * vanilla lanes because pickup routing runs before backpack reroute.
     */
    public static int trashOverflowPickup(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            ItemStack pickedTemplate,
            int pickedCount
    ) {
        if (player == null || runtime == null || pickedTemplate == null || pickedTemplate.isEmpty() || pickedCount <= 0) {
            return 0;
        }
        CarriedSourceAccess carried = carriedAccessOrNull();
        if (carried == null) {
            return 0;
        }
        ItemIdentity identity = ItemIdentityMatcher.create(pickedTemplate);
        runtime.collectionWorkflow().expireJunkTags();
        if (!runtime.collectionWorkflow().isJunk(identity)) {
            return 0;
        }
        if (runtime.protection().protects(identity, InventoryActionKind.TRASH)) {
            SlotCommon.LOGGER.info(
                    "[SLOT] skipped junk overflow trash: protected identity item={} count={}",
                    identity.itemId(),
                    pickedCount);
            return 0;
        }
        ExtractedBatch trashed;
        try {
            if (!inventoryOverThreshold(carried.currentAuthority(player))) {
                return 0;
            }
            trashed = extractMatching(carried, player, identity, pickedCount, true);
        } catch (RuntimeException exception) {
            SlotCommon.LOGGER.warn(
                    "[SLOT] skipped junk overflow trash: carried access failed item={} count={} error={}",
                    identity.itemId(),
                    pickedCount,
                    exception.toString());
            return 0;
        }
        if (trashed.count() <= 0) {
            return 0;
        }
        recordTrashActivity(runtime, identity, trashed.count(), InventoryActivityProducer.WORLD_PICKUP, false);
        return trashed.count();
    }

    private static void recordUndo(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            ItemIdentity identity,
            ExtractedBatch trashed,
            boolean wasJunk
    ) {
        List<ItemStack> restoreStacks = trashed.stacks();
        int count = trashed.count();
        runtime.undoStack().record(
                "trash item",
                ctx -> {
                    restoreTrashedStacks(player, restoreStacks);
                    restoreJunkState(ctx.runtime(), identity, wasJunk);
                },
                ctx -> {
                    CarriedSourceAccess carried = carriedAccessOrNull();
                    if (carried != null) {
                        extractMatching(carried, player, identity, count, false);
                    }
                    if (ctx.runtime() != null) {
                        ctx.runtime().collectionWorkflow().setJunk(
                                identity,
                                true,
                                DomainEventMetadata.origin("slot_workspace.trash.redo_mark_junk"));
                    }
                }
        );
    }

    private static void restoreJunkState(WorkflowDomainRuntime runtime, ItemIdentity identity, boolean wasJunk) {
        if (runtime == null || identity == null) {
            return;
        }
        runtime.collectionWorkflow().setJunk(
                identity,
                wasJunk,
                DomainEventMetadata.origin(wasJunk
                        ? "slot_workspace.trash.undo_restore_junk"
                        : "slot_workspace.trash.undo_clear_junk"));
    }

    private static void restoreTrashedStacks(ServerPlayer player, List<ItemStack> stacks) {
        if (player == null || stacks == null || stacks.isEmpty()) {
            return;
        }
        CarriedSourceAccess carried = carriedAccessOrNull();
        if (carried == null) {
            return;
        }
        for (ItemStack stack : stacks) {
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            ItemStack remainder = carried.insertBestFit(player, stack.copy(), false);
            if (remainder != null && !remainder.isEmpty()) {
                SlotCommon.LOGGER.warn(
                        "[SLOT] trash undo could not restore entire stack item={} restored={} remainder={}",
                        itemId(stack),
                        stack.getCount() - remainder.getCount(),
                        remainder.getCount());
            }
        }
    }

    private static ExtractedBatch extractMatching(
            CarriedSourceAccess carried,
            ServerPlayer player,
            ItemIdentity identity,
            int maxCount,
            boolean builtinFirst
    ) {
        if (carried == null || player == null || identity == null || maxCount <= 0) {
            return ExtractedBatch.empty();
        }
        ArrayList<CarriedSourceAccess.CarriedLocation> locations =
                new ArrayList<>(carried.findAllMatching(player, identity));
        if (builtinFirst) {
            locations.sort(Comparator.comparingInt(location -> builtinLaneRank(location.sourceId())));
        }
        ArrayList<ItemStack> extractedStacks = new ArrayList<>();
        int remaining = maxCount;
        int extractedCount = 0;
        for (CarriedSourceAccess.CarriedLocation location : locations) {
            if (remaining <= 0) {
                break;
            }
            ItemStack stack = carried.peek(player, location.sourceId(), location.slotIndex());
            if (!ItemIdentityMatcher.matchesMovable(stack, identity)) {
                continue;
            }
            ItemStack extracted = carried.extract(
                    player,
                    location.sourceId(),
                    location.slotIndex(),
                    Math.min(remaining, stack.getCount()),
                    false);
            if (extracted == null || extracted.isEmpty()) {
                continue;
            }
            extractedStacks.add(extracted.copy());
            extractedCount += extracted.getCount();
            remaining -= extracted.getCount();
        }
        return new ExtractedBatch(extractedStacks, extractedCount);
    }

    private static boolean inventoryOverThreshold(InventoryAuthoritySnapshot authority) {
        if (authority == null) {
            return false;
        }
        int capacity = 0;
        int occupied = 0;
        for (InventorySourceDescriptor source : authority.carriedSources()) {
            if (source == null) {
                continue;
            }
            int sourceCapacity = authority.slotCapacity(source.id());
            capacity += Math.max(0, sourceCapacity);
            for (InventoryEntrySnapshot entry : authority.entries(source.id())) {
                if (entry != null && entry.present()) {
                    occupied++;
                }
            }
        }
        return capacity > 0
                && occupied * OVERFLOW_THRESHOLD_DENOMINATOR
                > capacity * OVERFLOW_THRESHOLD_NUMERATOR;
    }

    private static void recordTrashActivity(
            WorkflowDomainRuntime runtime,
            ItemIdentity identity,
            int count,
            InventoryActivityProducer producer,
            boolean recoverable
    ) {
        if (runtime == null || identity == null || count <= 0) {
            return;
        }
        String token = recoverable ? "trash:" + UUID.randomUUID() : "";
        runtime.recordActivityEvent(
                new InventoryActivityEvent(
                        InventoryActivityKind.TRASHED,
                        producer,
                        InventoryActivityConfidence.AUTHORITATIVE,
                        identity,
                        count,
                        null,
                        null,
                        recoverable ? token : "",
                        token,
                        List.of(),
                        recoverable ? "direct_trash" : "junk_overflow_pickup"),
                DomainEventMetadata.origin(recoverable
                        ? "slot_workspace.trash"
                        : "slot_workspace.junk_overflow"));
    }

    private static CarriedSourceAccess carriedAccessOrNull() {
        try {
            return StorageAccessRegistry.carriedSourceAccess();
        } catch (IllegalStateException exception) {
            return null;
        }
    }

    private static int builtinLaneRank(String sourceId) {
        if (BuiltinInventoryIds.PLAYER_MAIN.equals(sourceId)) {
            return 0;
        }
        if (BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0.equals(sourceId)) {
            return 1;
        }
        if (BuiltinInventoryIds.PLAYER_OFFHAND.equals(sourceId)) {
            return 2;
        }
        if (BuiltinInventoryIds.PLAYER_ARMOR.equals(sourceId)) {
            return 3;
        }
        return 10;
    }

    private static String itemId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "empty";
        }
        return stack.getItem().builtInRegistryHolder().key().location().toString();
    }

    private record ExtractedBatch(List<ItemStack> stacks, int count) {
        private ExtractedBatch {
            stacks = stacks == null
                    ? List.of()
                    : List.copyOf(stacks.stream()
                            .filter(stack -> stack != null && !stack.isEmpty())
                            .map(ItemStack::copy)
                            .toList());
            count = Math.max(0, count);
        }

        static ExtractedBatch empty() {
            return new ExtractedBatch(List.of(), 0);
        }
    }
}
