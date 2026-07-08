package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.core.SlotResourceCollections;
import dev.imagio.slot.inventory.core.SlotResourceIdentity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class CraftRunDomainService {
    private final Consumer<CraftRunState> mutationObserver;
    private CraftRunState state = CraftRunState.empty();
    private long nextSequence = 1L;

    public CraftRunDomainService() {
        this(CraftRunState.empty(), state -> {
        });
    }

    public CraftRunDomainService(CraftRunState initialState, Consumer<CraftRunState> mutationObserver) {
        this.state = initialState == null ? CraftRunState.empty() : initialState;
        this.nextSequence = nextSequenceAfter(this.state);
        this.mutationObserver = mutationObserver == null ? state -> {
        } : mutationObserver;
    }

    public CraftRunState state() {
        return state;
    }

    public boolean add(CraftRunRecipeCapture capture) {
        if (capture == null || !capture.active()) {
            return false;
        }
        CraftRunRecipeEntry entry = entryFrom(capture);
        ArrayList<CraftRunRecipeEntry> entries = new ArrayList<>(state.entries());
        entries.add(entry);
        replaceState(new CraftRunState(state.revision() + 1, entry.entryId(), applyDependencyFloors(entries)));
        return true;
    }

    public boolean remove(String entryId) {
        if (entryId == null || entryId.isBlank() || !state.active()) {
            return false;
        }
        ArrayList<CraftRunRecipeEntry> entries = new ArrayList<>();
        boolean changed = false;
        for (CraftRunRecipeEntry entry : state.entries()) {
            if (entry.entryId().equals(entryId)) {
                changed = true;
                continue;
            }
            entries.add(entry);
        }
        if (!changed) {
            return false;
        }
        String selected = state.selectedEntryId().equals(entryId) && !entries.isEmpty()
                ? entries.get(entries.size() - 1).entryId()
                : state.selectedEntryId();
        replaceState(new CraftRunState(state.revision() + 1, selected, entries));
        return true;
    }

    public boolean adjustRemainingOutput(String entryId, int delta) {
        if (entryId == null || entryId.isBlank() || delta == 0 || !state.active()) {
            return false;
        }
        ArrayList<CraftRunRecipeEntry> entries = new ArrayList<>(state.entries().size());
        boolean changed = false;
        for (CraftRunRecipeEntry entry : state.entries()) {
            if (!entry.entryId().equals(entryId)) {
                entries.add(entry);
                continue;
            }
            if (entry.complete() && delta < 0) {
                entries.add(entry);
                continue;
            }
            long next = Math.max(entry.outputAmountPerBatch(), entry.remainingOutputAmount() + delta);
            if (next != entry.remainingOutputAmount()) {
                changed = true;
            }
            entries.add(entry.withRemainingOutputAmount(next));
        }
        if (!changed) {
            return false;
        }
        List<CraftRunRecipeEntry> floored = applyDependencyFloors(entries);
        if (floored.equals(state.entries())) {
            return false;
        }
        replaceState(new CraftRunState(state.revision() + 1, state.selectedEntryId(), floored));
        return true;
    }

    public boolean selectIngredientAlternative(String entryId, String groupId, ItemIdentity identity) {
        return selectIngredientAlternative(entryId, groupId, SlotResourceIdentity.item(identity));
    }

    public boolean selectIngredientAlternative(String entryId, String groupId, SlotResourceIdentity identity) {
        if (entryId == null || entryId.isBlank() || groupId == null || groupId.isBlank() || !state.active()) {
            return false;
        }
        ArrayList<CraftRunRecipeEntry> entries = new ArrayList<>(state.entries().size());
        boolean changed = false;
        for (CraftRunRecipeEntry entry : state.entries()) {
            if (!entry.entryId().equals(entryId)) {
                entries.add(entry);
                continue;
            }
            CraftRunRecipeEntry next = entry.withSelectedAlternative(groupId, identity);
            changed = changed || next != entry;
            entries.add(next);
        }
        if (!changed) {
            return false;
        }
        replaceState(new CraftRunState(state.revision() + 1, state.selectedEntryId(), applyDependencyFloors(entries)));
        return true;
    }

    public boolean observeActivityRecord(InventoryActivityRecord record) {
        InventoryActivityEvent event = record == null ? null : record.event();
        if (event == null || !event.present() || !decrementsCraftRun(event.kind()) || !state.active()) {
            return false;
        }
        SlotResourceIdentity eventResource = SlotResourceCollections.key(event.resourceIdentity());
        long remainingEventAmount = Math.max(0L, event.amount());
        ArrayList<CraftRunRecipeEntry> entries = new ArrayList<>(state.entries().size());
        boolean changed = false;
        for (CraftRunRecipeEntry entry : state.entries()) {
            if (remainingEventAmount <= 0L
                    || entry.outputResourceIdentity() == null
                    || !resourcesMatch(eventResource, entry.outputResourceIdentity())) {
                entries.add(entry);
                continue;
            }
            long decrement = Math.min(remainingEventAmount, entry.remainingOutputAmount());
            remainingEventAmount -= decrement;
            long nextRemaining = entry.remainingOutputAmount() - decrement;
            changed = changed || decrement > 0L;
            entries.add(entry.withRemainingOutputAmount(nextRemaining));
        }
        if (!changed) {
            return false;
        }
        replaceState(new CraftRunState(state.revision() + 1, state.selectedEntryId(), entries));
        return true;
    }

    private CraftRunRecipeEntry entryFrom(CraftRunRecipeCapture capture) {
        long sequence = nextSequence++;
        return CraftRunRecipeEntry.fromCapture("craft-run-" + sequence, sequence, capture);
    }

    private void replaceState(CraftRunState next) {
        state = next == null ? CraftRunState.empty() : next;
        mutationObserver.accept(state);
    }

    private static long nextSequenceAfter(CraftRunState state) {
        if (state == null || state.entries().isEmpty()) {
            return 1L;
        }
        long next = 1L;
        for (CraftRunRecipeEntry entry : state.entries()) {
            if (entry != null && entry.sequence() >= next && entry.sequence() < Long.MAX_VALUE) {
                next = entry.sequence() + 1L;
            }
        }
        return Math.max(1L, next);
    }

    private static List<CraftRunRecipeEntry> applyDependencyFloors(List<CraftRunRecipeEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        ArrayList<CraftRunRecipeEntry> adjusted = new ArrayList<>(entries.size());
        for (CraftRunRecipeEntry entry : entries) {
            if (entry == null || !entry.active() || !firstProducerForOutput(entry, entries)) {
                adjusted.add(entry);
                continue;
            }
            int floor = dependencyFloor(entry, entries);
            adjusted.add(floor > entry.remainingOutputCount()
                    ? entry.withRemainingOutputCount(floor)
                    : entry);
        }
        return List.copyOf(adjusted);
    }

    private static boolean firstProducerForOutput(CraftRunRecipeEntry candidate, List<CraftRunRecipeEntry> entries) {
        if (candidate == null || candidate.outputResourceIdentity() == null || entries == null) {
            return false;
        }
        for (CraftRunRecipeEntry entry : entries) {
            if (entry == candidate) {
                return true;
            }
            if (entry != null
                    && entry.active()
                    && resourcesMatch(entry.outputResourceIdentity(), candidate.outputResourceIdentity())) {
                return false;
            }
        }
        return true;
    }

    private static int dependencyFloor(CraftRunRecipeEntry producer, List<CraftRunRecipeEntry> entries) {
        if (producer == null || producer.outputResourceIdentity() == null || entries == null || entries.isEmpty()) {
            return 0;
        }
        long consumedRequired = 0L;
        int reusableRequired = 0;
        for (CraftRunRecipeEntry consumer : entries) {
            if (consumer == null
                    || !consumer.pending()
                    || consumer.entryId().equals(producer.entryId())
                    || consumer.inputs().isEmpty()) {
                continue;
            }
            int batches = consumer.remainingBatches();
            for (CraftRunIngredientGroup group : consumer.inputs()) {
                if (groupRequires(group, producer.outputResourceIdentity())) {
                    long requiredForGroup = group.requiredAmountForBatches(batches);
                    if (group.consumed()) {
                        consumedRequired += requiredForGroup;
                    } else {
                        reusableRequired = (int) Math.max(reusableRequired, Math.min(Integer.MAX_VALUE, requiredForGroup));
                    }
                    if (consumedRequired + reusableRequired >= Integer.MAX_VALUE) {
                        return Integer.MAX_VALUE;
                    }
                }
            }
        }
        return (int) Math.max(0L, consumedRequired + reusableRequired);
    }

    private static boolean groupRequires(CraftRunIngredientGroup group, ItemIdentity identity) {
        return groupRequires(group, SlotResourceIdentity.item(identity));
    }

    private static boolean groupRequires(CraftRunIngredientGroup group, SlotResourceIdentity identity) {
        if (group == null || identity == null || group.alternatives().isEmpty()) {
            return false;
        }
        for (CraftRunAlternative alternative : group.selectedOrAllAlternatives()) {
            if (alternative != null && resourcesMatch(alternative.resourceIdentity(), identity)) {
                return true;
            }
        }
        return false;
    }

    private static boolean resourcesMatch(SlotResourceIdentity left, SlotResourceIdentity right) {
        SlotResourceIdentity leftKey = SlotResourceCollections.key(left);
        SlotResourceIdentity rightKey = SlotResourceCollections.key(right);
        if (leftKey == null || rightKey == null || leftKey.kind() != rightKey.kind()) {
            return false;
        }
        if (leftKey.item()) {
            return ItemIdentityMatcher.matchesMovable(leftKey.toItemIdentity(), rightKey.toItemIdentity());
        }
        return leftKey.equals(rightKey);
    }

    private static boolean decrementsCraftRun(InventoryActivityKind kind) {
        return kind == InventoryActivityKind.ACQUIRED
                || kind == InventoryActivityKind.CRAFTED
                || kind == InventoryActivityKind.SMELTED;
    }
}
