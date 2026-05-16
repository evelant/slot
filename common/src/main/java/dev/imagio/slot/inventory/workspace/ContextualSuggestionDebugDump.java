package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.workflow.domain.ContextualAssociationHint;
import dev.imagio.slot.workflow.domain.ContextualAssociationSet;
import dev.imagio.slot.workflow.domain.ContextualContextAggregate;
import dev.imagio.slot.workflow.domain.ContextualItemAggregate;
import dev.imagio.slot.workflow.domain.ContextualSignalEvent;
import dev.imagio.slot.workflow.domain.ContextualSignalRecord;
import dev.imagio.slot.workflow.domain.ContextualSuggestionState;
import dev.imagio.slot.workflow.domain.ContextualEventSignature;
import dev.imagio.slot.workflow.domain.WorkflowDomainSnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ContextualSuggestionDebugDump {
    private static final int DEFAULT_EVENT_LIMIT = 48;
    private static final int DEFAULT_ASSOCIATION_LIMIT = 32;
    private static final int DEFAULT_SCORE_LIMIT = 6;

    private ContextualSuggestionDebugDump() {
    }

    public static List<String> format(
            WorkflowDomainSnapshot snapshot,
            SlotWorkspaceViewModel viewModel
    ) {
        return format(snapshot, viewModel, DEFAULT_EVENT_LIMIT, DEFAULT_ASSOCIATION_LIMIT, DEFAULT_SCORE_LIMIT);
    }

    public static List<String> format(
            WorkflowDomainSnapshot snapshot,
            SlotWorkspaceViewModel viewModel,
            int eventLimit,
            int associationLimit,
            int scoreLimit
    ) {
        WorkflowDomainSnapshot resolvedSnapshot = snapshot == null ? WorkflowDomainSnapshot.empty() : snapshot;
        ContextualSuggestionState state = resolvedSnapshot.contextualSuggestions();
        ArrayList<String> lines = new ArrayList<>();
        lines.add(prefix(String.format(
                Locale.ROOT,
                "summary nextSeq=%d activeContext=%s items=%d contexts=%d associations=%d recentEvents=%d viewModel=%s",
                state.nextStreamSequence(),
                blank(state.activeContextKey()),
                state.itemAggregates().size(),
                state.contextAggregates().size(),
                state.associationIndex().nextItemsBySignature().size(),
                state.recentSignals().size(),
                viewModel == null ? "absent" : "present rev=" + viewModel.revision())));
        appendSourceSummary(lines, viewModel);
        appendScoring(lines, viewModel, Math.max(1, scoreLimit));
        appendRecentEvents(lines, state, Math.max(1, eventLimit));
        appendAssociations(lines, state, Math.max(1, associationLimit));
        appendItemAggregates(lines, state);
        appendContextAggregates(lines, state);
        return List.copyOf(lines);
    }

    private static void appendSourceSummary(List<String> lines, SlotWorkspaceViewModel viewModel) {
        if (viewModel == null) {
            return;
        }
        ArrayList<SlotWorkspaceViewModel.AtlasItem> candidates = new ArrayList<>();
        candidates.addAll(viewModel.atlasItems());
        candidates.addAll(viewModel.triageItems());
        long carried = candidates.stream()
                .filter(item -> item != null && item.carried())
                .count();
        long proximateStorageOnly = candidates.stream()
                .filter(ContextualSuggestionDebugDump::storageOnly)
                .count();
        long elsewhereOnly = candidates.stream()
                .filter(item -> item != null
                        && !item.carried()
                        && item.proximateCount() <= 0
                        && item.presence().isEmpty()
                        && !item.elsewhere().isEmpty())
                .count();
        long usefulStorageOnly = viewModel.contextualSuggestionLanes().stream()
                .filter(lane -> SlotWorkspaceViewModel.ContextualSuggestionLane.USEFUL_NOW.equals(lane.id()))
                .flatMap(lane -> lane.items().stream())
                .filter(ContextualSuggestionDebugDump::storageOnly)
                .count();
        lines.add(prefix(String.format(
                Locale.ROOT,
                "candidate sources carried=%d proximateStorageOnly=%d elsewhereOnly=%d usefulStorageOnly=%d",
                carried,
                proximateStorageOnly,
                elsewhereOnly,
                usefulStorageOnly)));
    }

    private static boolean storageOnly(SlotWorkspaceViewModel.AtlasItem item) {
        return item != null
                && !item.carried()
                && (item.proximateCount() > 0 || !item.presence().isEmpty());
    }

    private static void appendScoring(
            List<String> lines,
            SlotWorkspaceViewModel viewModel,
            int scoreLimit
    ) {
        if (viewModel == null || viewModel.contextualSuggestionLanes().isEmpty()) {
            lines.add(prefix("scores unavailable: no active workspace view model"));
            return;
        }
        for (SlotWorkspaceViewModel.ContextualSuggestionLane lane : viewModel.contextualSuggestionLanes()) {
            if (lane == null) {
                continue;
            }
            lines.add(prefix(String.format(
                    Locale.ROOT,
                    "lane %s items=%d debug=%d placeholder=%s",
                    lane.id(),
                    lane.items().size(),
                    lane.debugInfo().size(),
                    blank(lane.placeholderText()))));
            int emitted = 0;
            for (SlotWorkspaceViewModel.ContextualSuggestionDebugInfo info : lane.debugInfo()) {
                if (info == null || emitted >= scoreLimit) {
                    break;
                }
                emitted++;
                lines.add(prefix(String.format(
                        Locale.ROOT,
                        "  score %s score=%.2f relevance=%.2f",
                        info.identity().itemId(),
                        info.score(),
                        info.relevance())));
                for (String reason : info.reasons()) {
                    lines.add(prefix("    " + reason));
                }
            }
        }
    }

    private static void appendRecentEvents(
            List<String> lines,
            ContextualSuggestionState state,
            int eventLimit
    ) {
        lines.add(prefix("recent events newest first"));
        List<ContextualSignalRecord> records = state.recentSignals().stream()
                .sorted(Comparator
                        .comparingLong((ContextualSignalRecord record) -> record.envelope().globalSequence())
                        .reversed())
                .limit(eventLimit)
                .toList();
        if (records.isEmpty()) {
            lines.add(prefix("  none"));
            return;
        }
        for (ContextualSignalRecord record : records) {
            ContextualSignalEvent event = record.event();
            String signature = ContextualEventSignature.key(event);
            lines.add(prefix(String.format(
                    Locale.ROOT,
                    "  #%d/%d %s item=%s count=%d tick=%d context=%s label=%s source=%s meta=%s signature=%s",
                    record.envelope().globalSequence(),
                    record.envelope().streamSequence(),
                    event.kind(),
                    item(event.identity()),
                    event.count(),
                    event.observedTick(),
                    blank(event.contextKey()),
                    blank(event.contextLabel()),
                    blank(event.sourceKey()),
                    map(event.metadata()),
                    blank(signature))));
        }
    }

    private static void appendAssociations(
            List<String> lines,
            ContextualSuggestionState state,
            int associationLimit
    ) {
        lines.add(prefix("associations newest first"));
        List<Map.Entry<String, ContextualAssociationSet>> entries =
                state.associationIndex().nextItemsBySignature().entrySet().stream()
                        .sorted(Comparator
                                .<Map.Entry<String, ContextualAssociationSet>>comparingLong(
                                        entry -> entry.getValue().lastSequence())
                                .reversed()
                                .thenComparing(Map.Entry::getKey))
                        .limit(associationLimit)
                        .toList();
        if (entries.isEmpty()) {
            lines.add(prefix("  none"));
            return;
        }
        for (Map.Entry<String, ContextualAssociationSet> entry : entries) {
            lines.add(prefix("  " + entry.getKey() + " -> " + hints(entry.getValue())));
        }
    }

    private static void appendItemAggregates(List<String> lines, ContextualSuggestionState state) {
        lines.add(prefix("item aggregates newest first"));
        List<ContextualItemAggregate> aggregates = state.itemAggregates().values().stream()
                .sorted(Comparator
                        .comparingLong(ContextualSuggestionDebugDump::lastItemSequence)
                        .reversed())
                .limit(16)
                .toList();
        if (aggregates.isEmpty()) {
            lines.add(prefix("  none"));
            return;
        }
        for (ContextualItemAggregate aggregate : aggregates) {
            lines.add(prefix(String.format(
                    Locale.ROOT,
                    "  %s active=%d acquired=%d taken=%d deposited=%d crafted=%d used=%d placed=%d consumed=%d damaged=%d",
                    item(aggregate.identity()),
                    aggregate.lastActiveSequence(),
                    aggregate.timesAcquired(),
                    aggregate.timesTakenFromStorage(),
                    aggregate.timesDepositedToStorage(),
                    aggregate.timesCraftedOrProduced(),
                    aggregate.timesUsed(),
                    aggregate.timesPlaced(),
                    aggregate.timesConsumed(),
                    aggregate.timesDamaged())));
        }
    }

    private static void appendContextAggregates(List<String> lines, ContextualSuggestionState state) {
        lines.add(prefix("context aggregates newest first"));
        List<ContextualContextAggregate> aggregates = state.contextAggregates().values().stream()
                .sorted(Comparator
                        .comparingLong(ContextualContextAggregate::lastSeenSequence)
                        .reversed())
                .limit(12)
                .toList();
        if (aggregates.isEmpty()) {
            lines.add(prefix("  none"));
            return;
        }
        for (ContextualContextAggregate aggregate : aggregates) {
            lines.add(prefix(String.format(
                    Locale.ROOT,
                    "  %s label=%s seen=%d last=%d itemHints=%s",
                    blank(aggregate.contextKey()),
                    blank(aggregate.label()),
                    aggregate.timesSeen(),
                    aggregate.lastSeenSequence(),
                    map(aggregate.itemHints()))));
        }
    }

    private static long lastItemSequence(ContextualItemAggregate aggregate) {
        if (aggregate == null) {
            return 0L;
        }
        return Math.max(
                aggregate.lastActiveSequence(),
                Math.max(aggregate.lastAcquiredSequence(), aggregate.lastDepositedSequence()));
    }

    private static String hints(ContextualAssociationSet set) {
        if (set == null || set.itemHints().isEmpty()) {
            return "none";
        }
        return set.itemHints().values().stream()
                .sorted(Comparator
                        .comparingDouble(ContextualAssociationHint::score)
                        .reversed()
                        .thenComparing(ContextualAssociationHint::itemId))
                .limit(8)
                .map(hint -> String.format(
                        Locale.ROOT,
                        "%s=%.2f(count=%d,avgDelta=%.1f,last=%d)",
                        hint.itemId(),
                        hint.score(),
                        hint.count(),
                        hint.averageDelta(),
                        hint.lastSequence()))
                .reduce((left, right) -> left + ", " + right)
                .orElse("none");
    }

    private static String item(ItemIdentity identity) {
        return identity == null ? "<none>" : blank(identity.itemId());
    }

    private static String map(Map<?, ?> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }
        return map.entrySet().stream()
                .limit(12)
                .map(entry -> String.valueOf(entry.getKey()) + "=" + String.valueOf(entry.getValue()))
                .reduce((left, right) -> left + ", " + right)
                .map(value -> "{" + value + "}")
                .orElse("{}");
    }

    private static String blank(String value) {
        return value == null || value.isBlank() ? "<none>" : value;
    }

    private static String prefix(String value) {
        return "[SLOT][contextual] " + value;
    }
}
