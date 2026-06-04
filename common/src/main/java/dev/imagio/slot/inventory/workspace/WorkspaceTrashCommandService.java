package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.inventory.action.InventoryActionKind;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityCollections;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.storage.CarriedInventoryRevisions;
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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Common implementation for junk tagging and destructive carried-item cleanup.
 * Platform UIs provide only an identity; all live matching, protection checks,
 * extraction, undo restore, and pickup-overflow behavior stays here.
 */
public final class WorkspaceTrashCommandService {
    private static final int OVERFLOW_THRESHOLD_NUMERATOR = 1;
    private static final int OVERFLOW_THRESHOLD_DENOMINATOR = 2;
    private static final List<BuiltinLane> PICKUP_LANES = List.of(
            new BuiltinLane(BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0, 9),
            new BuiltinLane(BuiltinInventoryIds.PLAYER_MAIN, 27)
    );

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
        SlotDebugLog.log(
                "junk mark update item={} marked={} changed={} junkTags={}",
                identity.itemId(),
                marked,
                changed,
                runtime.workflowProjection().junkTags().size());
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

        recordTrashActivity(runtime, identity, trashed.count(), InventoryActivityProducer.ROUTER_ACTION, true);
        CarriedInventoryRevisions.markChanged(player, "direct_trash");
        recordUndo(player, runtime, identity, trashed);
        return WorkspaceCommandOutcome.accepted("trashed", identity.itemId() + " count=" + trashed.count());
    }

    /**
     * Run junk pressure relief after a world pickup. The caller supplies the
     * authoritative pickup stack; this method deletes whole junk stacks until
     * enough slots are free, preferring the just-picked stack when it is marked
     * junk. The result separates total carried junk swept from the portion of
     * the just-picked stack that was consumed so pickup routing can move only
     * the live remainder.
     */
    public static PostPickupOverflowTrashResult trashOverflowPickup(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            ItemStack pickedTemplate,
            int pickedCount
    ) {
        if (player == null || runtime == null || pickedTemplate == null || pickedTemplate.isEmpty() || pickedCount <= 0) {
            return PostPickupOverflowTrashResult.empty();
        }
        CarriedSourceAccess carried = carriedAccessOrNull();
        if (carried == null) {
            SlotDebugLog.verboseLog(
                    "junk overflow post skipped item={} count={} reason=carried_access_unavailable",
                    itemId(pickedTemplate),
                    pickedCount);
            return PostPickupOverflowTrashResult.empty();
        }
        ItemIdentity identity = ItemIdentityMatcher.create(pickedTemplate);
        runtime.collectionWorkflow().expireJunkTags();
        Set<ItemIdentity> junkTags = runtime.workflowProjection().junkTags();
        if (junkTags.isEmpty()) {
            SlotDebugLog.verboseLog(
                    "junk overflow post skipped item={} count={} reason=no_junk_tags",
                    identity.itemId(),
                    pickedCount);
            return PostPickupOverflowTrashResult.empty();
        }
        boolean pickedJunk = ItemIdentityCollections.contains(junkTags, identity);
        ArrayList<ItemStack> trashedStacks = new ArrayList<>();
        int trashedCount = 0;
        int freedSlots = 0;
        int pickedTrashedCount = 0;
        try {
            CarriedSourceAccess.CarriedStoragePressure pressure =
                    CarriedInventoryRevisions.cachedPressure(player, carried);
            int slotsToFree = pressure.slotsToFreeForThreshold(
                    OVERFLOW_THRESHOLD_NUMERATOR,
                    OVERFLOW_THRESHOLD_DENOMINATOR);
            SlotDebugLog.verboseLog(
                    "junk overflow post pressure item={} count={} pickedJunk={} junkTags={} capacity={} occupied={} free={} slotsToFree={}",
                    identity.itemId(),
                    pickedCount,
                    pickedJunk,
                    junkTags.size(),
                    pressure.slotCapacity(),
                    pressure.occupiedSlots(),
                    freeSlots(pressure),
                    slotsToFree);
            if (slotsToFree <= 0) {
                SlotDebugLog.verboseLog(
                        "junk overflow post skipped item={} count={} reason=below_threshold",
                        identity.itemId(),
                        pickedCount);
                return PostPickupOverflowTrashResult.empty();
            }

            boolean pickedProtected = runtime.protection().protects(identity, InventoryActionKind.TRASH);
            if (pickedJunk && !pickedProtected) {
                ExtractedBatch pickedTrash = extractPickupLaneJunkStacks(
                        carried,
                        player,
                        identity,
                        slotsToFree);
                trashedStacks.addAll(pickedTrash.stacks());
                trashedCount += pickedTrash.count();
                pickedTrashedCount += Math.min(
                        Math.max(0, pickedCount - pickedTrashedCount),
                        pickedTrash.count());
                freedSlots += pickedTrash.freedSlots();
            } else if (pickedJunk) {
                SlotCommon.LOGGER.info(
                        "[SLOT] skipped picked junk overflow trash: protected identity item={} count={}",
                        identity.itemId(),
                        pickedCount);
                SlotDebugLog.log(
                        "junk overflow post skipped picked stack item={} count={} reason=protected",
                        identity.itemId(),
                        pickedCount);
            }

            if (freedSlots < slotsToFree) {
                ExtractedBatch sweptTrash = sweepJunkStacks(
                        carried,
                        player,
                        runtime,
                        junkTags,
                        slotsToFree - freedSlots);
                trashedStacks.addAll(sweptTrash.stacks());
                trashedCount += sweptTrash.count();
                freedSlots += sweptTrash.freedSlots();
            }
        } catch (RuntimeException exception) {
            SlotCommon.LOGGER.warn(
                    "[SLOT] skipped junk overflow trash: carried access failed item={} count={} error={}",
                    identity.itemId(),
                    pickedCount,
                    exception.toString());
            return PostPickupOverflowTrashResult.empty();
        }
        ExtractedBatch trashed = new ExtractedBatch(trashedStacks, trashedCount, freedSlots);
        if (trashed.count() <= 0) {
            SlotDebugLog.verboseLog(
                    "junk overflow post completed item={} count={} result=no_candidates_or_no_extract freedSlots={} slotsTrashed={}",
                    identity.itemId(),
                    pickedCount,
                    freedSlots,
                    trashedStacks.size());
            return PostPickupOverflowTrashResult.empty();
        }
        recordOverflowTrashActivity(runtime, trashed);
        CarriedInventoryRevisions.markChanged(player, "junk_overflow_trash");
        SlotDebugLog.log(
                "junk overflow post trashed triggerItem={} pickedCount={} totalTrashed={} pickedTrashed={} freedSlots={} stacks={}",
                identity.itemId(),
                pickedCount,
                trashed.count(),
                pickedTrashedCount,
                trashed.freedSlots(),
                trashed.stacks().size());
        return new PostPickupOverflowTrashResult(trashed.count(), pickedTrashedCount);
    }

    /**
     * Run pressure relief before vanilla tries to insert an item entity. This
     * covers the full-inventory edge where the post-pickup hook never fires
     * because vanilla cannot pick up even one item. Existing carried junk is
     * swept first; if carried storage is already under pressure and the
     * incoming stack is itself junk, the caller may void that incoming entity.
     */
    public static PickupOverflowTrashResult trashOverflowBeforePickup(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            ItemStack incomingTemplate,
            int incomingCount
    ) {
        if (player == null || runtime == null || incomingTemplate == null
                || incomingTemplate.isEmpty() || incomingCount <= 0) {
            return PickupOverflowTrashResult.empty();
        }
        CarriedSourceAccess carried = carriedAccessOrNull();
        if (carried == null) {
            SlotDebugLog.verboseLog(
                    "junk overflow pre skipped item={} count={} reason=carried_access_unavailable",
                    itemId(incomingTemplate),
                    incomingCount);
            return PickupOverflowTrashResult.empty();
        }
        ItemIdentity identity = ItemIdentityMatcher.create(incomingTemplate);
        runtime.collectionWorkflow().expireJunkTags();
        Set<ItemIdentity> junkTags = runtime.workflowProjection().junkTags();
        if (junkTags.isEmpty()) {
            SlotDebugLog.verboseLog(
                    "junk overflow pre skipped item={} count={} reason=no_junk_tags",
                    identity.itemId(),
                    incomingCount);
            return PickupOverflowTrashResult.empty();
        }
        try {
            CarriedSourceAccess.CarriedStoragePressure pressure =
                    CarriedInventoryRevisions.cachedPressure(player, carried);
            boolean incomingJunkCandidate = ItemIdentityCollections.contains(junkTags, identity);
            boolean incomingProtected = runtime.protection().protects(identity, InventoryActionKind.TRASH);
            int slotsToFree = pressure.slotsToFreeForThreshold(
                    OVERFLOW_THRESHOLD_NUMERATOR,
                    OVERFLOW_THRESHOLD_DENOMINATOR);
            SlotDebugLog.verboseLog(
                    "junk overflow pre pressure item={} count={} incomingJunk={} protected={} junkTags={} capacity={} occupied={} free={} slotsToFree={}",
                    identity.itemId(),
                    incomingCount,
                    incomingJunkCandidate,
                    incomingProtected,
                    junkTags.size(),
                    pressure.slotCapacity(),
                    pressure.occupiedSlots(),
                    freeSlots(pressure),
                    slotsToFree);
            if (slotsToFree <= 0) {
                SlotDebugLog.verboseLog(
                        "junk overflow pre skipped item={} count={} reason=below_threshold",
                        identity.itemId(),
                        incomingCount);
                return PickupOverflowTrashResult.empty();
            }

            ExtractedBatch sweptTrash = sweepJunkStacks(
                    carried,
                    player,
                    runtime,
                    junkTags,
                    slotsToFree);
            if (sweptTrash.count() > 0) {
                recordOverflowTrashActivity(runtime, sweptTrash);
                CarriedInventoryRevisions.markChanged(player, "junk_overflow_pre_pickup_sweep");
            }

            boolean incomingJunk = incomingJunkCandidate && !incomingProtected;
            int incomingTrashed = incomingJunk ? incomingCount : 0;
            if (incomingTrashed > 0) {
                recordTrashActivity(
                        runtime,
                        identity,
                        incomingTrashed,
                        InventoryActivityProducer.WORLD_PICKUP,
                        false);
            }
            if (sweptTrash.count() > 0 || incomingTrashed > 0) {
                SlotDebugLog.log(
                        "junk overflow pre result triggerItem={} incomingCount={} carriedTrashed={} sweptFreedSlots={} incomingTrashed={}",
                        identity.itemId(),
                        incomingCount,
                        sweptTrash.count(),
                        sweptTrash.freedSlots(),
                        incomingTrashed);
            } else {
                SlotDebugLog.verboseLog(
                        "junk overflow pre result triggerItem={} incomingCount={} carriedTrashed={} sweptFreedSlots={} incomingTrashed={}",
                        identity.itemId(),
                        incomingCount,
                        sweptTrash.count(),
                        sweptTrash.freedSlots(),
                        incomingTrashed);
            }
            return new PickupOverflowTrashResult(sweptTrash.count(), incomingTrashed);
        } catch (RuntimeException exception) {
            SlotCommon.LOGGER.warn(
                    "[SLOT] skipped pre-pickup junk overflow trash: carried access failed item={} count={} error={}",
                    identity.itemId(),
                    incomingCount,
                    exception.toString());
            return PickupOverflowTrashResult.empty();
        }
    }

    private static void recordUndo(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            ItemIdentity identity,
            ExtractedBatch trashed
    ) {
        List<ItemStack> restoreStacks = trashed.stacks();
        int count = trashed.count();
        runtime.undoStack().record(
                "trash item",
                ctx -> {
                    restoreTrashedStacks(player, restoreStacks);
                },
                ctx -> {
                    CarriedSourceAccess carried = carriedAccessOrNull();
                    if (carried != null) {
                        ExtractedBatch redone = extractMatching(carried, player, identity, count, false);
                        if (redone.count() > 0) {
                            CarriedInventoryRevisions.markChanged(player, "trash_redo");
                        }
                    }
                }
        );
    }

    private static void restoreTrashedStacks(ServerPlayer player, List<ItemStack> stacks) {
        if (player == null || stacks == null || stacks.isEmpty()) {
            return;
        }
        CarriedSourceAccess carried = carriedAccessOrNull();
        if (carried == null) {
            return;
        }
        boolean attemptedRestore = false;
        for (ItemStack stack : stacks) {
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            attemptedRestore = true;
            ItemStack remainder = carried.insertBestFit(player, stack.copy(), false);
            if (remainder != null && !remainder.isEmpty()) {
                SlotCommon.LOGGER.warn(
                        "[SLOT] trash undo could not restore entire stack item={} restored={} remainder={}",
                        itemId(stack),
                        stack.getCount() - remainder.getCount(),
                        remainder.getCount());
            }
        }
        if (attemptedRestore) {
            CarriedInventoryRevisions.markChanged(player, "trash_undo_restore");
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

    private static ExtractedBatch extractPickupLaneJunkStacks(
            CarriedSourceAccess carried,
            ServerPlayer player,
            ItemIdentity identity,
            int maxSlotsToFree
    ) {
        if (carried == null || player == null || identity == null || maxSlotsToFree <= 0) {
            return ExtractedBatch.empty();
        }
        ArrayList<TrashCandidate> candidates = pickupLaneJunkCandidates(carried, player, identity);
        SlotDebugLog.verboseLog(
                "junk overflow pickup-lane candidates item={} maxSlotsToFree={} candidates={}",
                identity.itemId(),
                maxSlotsToFree,
                candidates.size());
        candidates.sort(Comparator
                .comparingInt(TrashCandidate::stackCount)
                .thenComparingInt(TrashCandidate::sourceOrder)
                .thenComparing(TrashCandidate::sourceId)
                .thenComparingInt(TrashCandidate::slotIndex));
        return extractFullStacks(carried, player, candidates, maxSlotsToFree);
    }

    private static ExtractedBatch sweepJunkStacks(
            CarriedSourceAccess carried,
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            Collection<ItemIdentity> junkTags,
            int maxSlotsToFree
    ) {
        if (carried == null || player == null || runtime == null || junkTags == null
                || junkTags.isEmpty() || maxSlotsToFree <= 0) {
            return ExtractedBatch.empty();
        }
        ArrayList<TrashCandidate> candidates = new ArrayList<>();
        HashSet<String> seenLocations = new HashSet<>();
        int requestedTags = 0;
        int protectedTags = 0;
        int rawLocations = 0;
        int duplicateLocations = 0;
        int protectedLiveMatches = 0;
        for (ItemIdentity junkIdentity : junkTags) {
            if (junkIdentity == null) {
                continue;
            }
            requestedTags++;
            if (runtime.protection().protects(junkIdentity, InventoryActionKind.TRASH)) {
                protectedTags++;
                continue;
            }
            for (CarriedSourceAccess.CarriedLocation location : carried.findAllMatching(player, junkIdentity)) {
                if (location == null) {
                    continue;
                }
                rawLocations++;
                String key = location.sourceId() + "#" + location.slotIndex();
                if (!seenLocations.add(key)) {
                    duplicateLocations++;
                    continue;
                }
                ItemStack stack = carried.peek(player, location.sourceId(), location.slotIndex());
                if (stack == null || stack.isEmpty()) {
                    continue;
                }
                ItemIdentity liveIdentity = ItemIdentityMatcher.create(stack);
                if (!ItemIdentityCollections.contains(junkTags, liveIdentity)
                        || runtime.protection().protects(liveIdentity, InventoryActionKind.TRASH)) {
                    if (runtime.protection().protects(liveIdentity, InventoryActionKind.TRASH)) {
                        protectedLiveMatches++;
                    }
                    continue;
                }
                candidates.add(new TrashCandidate(
                        location.sourceId(),
                        location.slotIndex(),
                        liveIdentity,
                        stack.getCount(),
                        sourceOrder(location.sourceId())));
            }
        }
        SlotDebugLog.verboseLog(
                "junk overflow sweep candidates requestedTags={} protectedTags={} rawLocations={} duplicateLocations={} protectedLiveMatches={} candidates={} maxSlotsToFree={}",
                requestedTags,
                protectedTags,
                rawLocations,
                duplicateLocations,
                protectedLiveMatches,
                candidates.size(),
                maxSlotsToFree);
        candidates.sort(Comparator
                .comparingInt(TrashCandidate::stackCount)
                .thenComparingInt(TrashCandidate::sourceOrder)
                .thenComparing(TrashCandidate::sourceId)
                .thenComparingInt(TrashCandidate::slotIndex));
        return extractFullStacks(carried, player, candidates, maxSlotsToFree);
    }

    private static ExtractedBatch extractFullStacks(
            CarriedSourceAccess carried,
            ServerPlayer player,
            List<TrashCandidate> candidates,
            int maxSlotsToFree
    ) {
        ArrayList<ItemStack> extractedStacks = new ArrayList<>();
        int extractedCount = 0;
        int freedSlots = 0;
        for (TrashCandidate candidate : candidates) {
            if (freedSlots >= maxSlotsToFree) {
                break;
            }
            ItemStack stack = carried.peek(player, candidate.sourceId(), candidate.slotIndex());
            if (!ItemIdentityMatcher.matchesMovable(stack, candidate.identity())) {
                continue;
            }
            int originalCount = stack.getCount();
            ItemStack extracted = carried.extract(
                    player,
                    candidate.sourceId(),
                    candidate.slotIndex(),
                    originalCount,
                    false);
            if (extracted == null || extracted.isEmpty()) {
                continue;
            }
            extractedStacks.add(extracted.copy());
            extractedCount += extracted.getCount();
            ItemStack after = carried.peek(player, candidate.sourceId(), candidate.slotIndex());
            if (after == null || after.isEmpty()) {
                freedSlots++;
            }
        }
        SlotDebugLog.verboseLog(
                "junk overflow extract-full candidates={} maxSlotsToFree={} extractedCount={} freedSlots={} stacks={}",
                candidates.size(),
                maxSlotsToFree,
                extractedCount,
                freedSlots,
                extractedStacks.size());
        return new ExtractedBatch(extractedStacks, extractedCount, freedSlots);
    }

    private static ArrayList<TrashCandidate> pickupLaneJunkCandidates(
            CarriedSourceAccess carried,
            ServerPlayer player,
            ItemIdentity identity
    ) {
        ArrayList<TrashCandidate> candidates = new ArrayList<>();
        for (int laneIndex = 0; laneIndex < PICKUP_LANES.size(); laneIndex++) {
            BuiltinLane lane = PICKUP_LANES.get(laneIndex);
            for (int slot = 0; slot < lane.slotCount(); slot++) {
                ItemStack stack = carried.peek(player, lane.sourceId(), slot);
                if (ItemIdentityMatcher.matchesMovable(stack, identity)) {
                    candidates.add(new TrashCandidate(
                            lane.sourceId(),
                            slot,
                            identity,
                            stack.getCount(),
                            laneIndex));
                }
            }
        }
        return candidates;
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

    private static void recordOverflowTrashActivity(WorkflowDomainRuntime runtime, ExtractedBatch trashed) {
        if (runtime == null || trashed == null || trashed.stacks().isEmpty()) {
            return;
        }
        for (ItemStack stack : trashed.stacks()) {
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            recordTrashActivity(
                    runtime,
                    ItemIdentityMatcher.create(stack),
                    stack.getCount(),
                    InventoryActivityProducer.WORLD_PICKUP,
                    false);
        }
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

    private static int sourceOrder(String sourceId) {
        int builtinRank = builtinLaneRank(sourceId);
        if (builtinRank < 10) {
            return 100 + builtinRank;
        }
        return 0;
    }

    private static String itemId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "empty";
        }
        return stack.getItem().builtInRegistryHolder().key().location().toString();
    }

    private static int freeSlots(CarriedSourceAccess.CarriedStoragePressure pressure) {
        if (pressure == null) {
            return 0;
        }
        return Math.max(0, pressure.slotCapacity() - pressure.occupiedSlots());
    }

    private record BuiltinLane(String sourceId, int slotCount) {
    }

    public record PickupOverflowTrashResult(int carriedTrashed, int incomingTrashed) {
        public PickupOverflowTrashResult {
            carriedTrashed = Math.max(0, carriedTrashed);
            incomingTrashed = Math.max(0, incomingTrashed);
        }

        public static PickupOverflowTrashResult empty() {
            return new PickupOverflowTrashResult(0, 0);
        }

        public boolean anyTrashed() {
            return carriedTrashed > 0 || incomingTrashed > 0;
        }
    }

    public record PostPickupOverflowTrashResult(int carriedTrashed, int pickedTrashed) {
        public PostPickupOverflowTrashResult {
            carriedTrashed = Math.max(0, carriedTrashed);
            pickedTrashed = Math.max(0, pickedTrashed);
            pickedTrashed = Math.min(pickedTrashed, carriedTrashed);
        }

        public static PostPickupOverflowTrashResult empty() {
            return new PostPickupOverflowTrashResult(0, 0);
        }

        public boolean anyTrashed() {
            return carriedTrashed > 0 || pickedTrashed > 0;
        }
    }

    private record TrashCandidate(
            String sourceId,
            int slotIndex,
            ItemIdentity identity,
            int stackCount,
            int sourceOrder
    ) {
    }

    private record ExtractedBatch(List<ItemStack> stacks, int count, int freedSlots) {
        private ExtractedBatch(List<ItemStack> stacks, int count) {
            this(stacks, count, 0);
        }

        private ExtractedBatch {
            stacks = stacks == null
                    ? List.of()
                    : List.copyOf(stacks.stream()
                            .filter(stack -> stack != null && !stack.isEmpty())
                            .map(ItemStack::copy)
                            .toList());
            count = Math.max(0, count);
            freedSlots = Math.max(0, freedSlots);
        }

        static ExtractedBatch empty() {
            return new ExtractedBatch(List.of(), 0, 0);
        }
    }
}
