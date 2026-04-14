package dev.imagio.slot.action.session;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.client.collection.HotbarLoadoutCapture;
import dev.imagio.slot.client.collection.HotbarLoadoutDefinition;
import dev.imagio.slot.client.collection.HotbarLoadoutSlot;
import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.intent.ActionRequest;
import dev.imagio.slot.intent.ActionRequestId;
import dev.imagio.slot.network.ActionRequestIdentityCodec;
import dev.imagio.slot.network.BackpackTransferActionRequests;
import dev.imagio.slot.operation.ActionStatus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class ActionSessionStore {
    private static final long SESSION_TIMEOUT_NANOS = 60_000_000_000L;
    private static final long CONFIRMED_FOLLOW_UP_TIMEOUT_NANOS = 3_000_000_000L;
    private static final int MAX_PENDING_SESSIONS = 4096;
    private static final int MAX_PENDING_OUTCOMES = 4096;

    private static final Map<String, Session> sessionsByRequestId = new LinkedHashMap<>();
    private static final Map<String, String> quickAccessRequestByScopedIndex = new LinkedHashMap<>();
    private static final List<PublishedActionOutcome> publishedOutcomes = new ArrayList<>();
    private static final Map<String, PendingHistoryTransferRequest> pendingHistoryTransferRequests = new LinkedHashMap<>();
    private static final Map<String, PendingHistoryTransferTransition> pendingHistoryTransferTransitions = new LinkedHashMap<>();
    private static final Map<String, PendingHistoryQuickAccessOperation> pendingHistoryQuickAccessOperations = new LinkedHashMap<>();
    private static final Set<String> pendingHistoryTransferTransitionContexts = new LinkedHashSet<>();
    private static final Set<String> pendingHistoryQuickAccessTransitionContexts = new LinkedHashSet<>();
    private static final List<HistorySettlement> pendingHistorySettlements = new ArrayList<>();

    private ActionSessionStore() {
    }

    public static void bindContext(String key) {
        pruneExpired();
    }

    public static void clear() {
        sessionsByRequestId.clear();
        quickAccessRequestByScopedIndex.clear();
        publishedOutcomes.clear();
        clearDeferredHistoryState();
    }

    public static void recordRequest(ActionRequest request) {
        recordRequest("", request, "");
    }

    public static void recordRequest(ActionRequest request, String routingKey) {
        recordRequest("", request, routingKey);
    }

    public static void recordRequest(UUID hostId, ActionRequest request, String routingKey) {
        recordRequest(hostId == null ? "" : hostId.toString(), request, routingKey);
    }

    public static void recordRequest(String hostKey, ActionRequest request, String routingKey) {
        pruneExpired();
        if (request == null || request.requestId() == null || !request.requestId().present()) {
            return;
        }

        String requestId = request.requestId().value();
        sessionsByRequestId.put(
                requestId,
                session(hostKey, requestId).withRoutingKey(routingKey == null ? "" : routingKey)
        );
        trimSessions();
    }

    public static String resolveContextKey(ActionRequestId requestId, String fallbackKey) {
        pruneExpired();
        if (requestId != null && requestId.present()) {
            Session session = sessionsByRequestId.get(requestId.value());
            if (session != null && !session.routingKey().isBlank()) {
                return session.routingKey();
            }
        }
        return fallbackKey == null ? "" : fallbackKey;
    }

    public static void recordQuickAccessTargets(List<QuickAccessRequestedTarget> pendingChanges) {
        pruneExpired();
        if (pendingChanges == null || pendingChanges.isEmpty()) {
            return;
        }

        long now = System.nanoTime();
        for (QuickAccessRequestedTarget change : pendingChanges) {
            if (change == null || change.requestId() == null || !change.requestId().present()) {
                continue;
            }

            String requestId = change.requestId().value();
            Session session = session(requestId)
                    .withCreatedAt(now)
                    .withQuickAccessIndex(change.quickAccessIndex());
            sessionsByRequestId.put(requestId, session);

            String replacedRequestId = quickAccessRequestByScopedIndex.put(
                    scopedQuickAccessIndexKey(session.hostKey(), change.quickAccessIndex()),
                    requestId
            );
            if (replacedRequestId != null && !Objects.equals(replacedRequestId, requestId)) {
                Session replacedSession = sessionsByRequestId.get(replacedRequestId);
                if (replacedSession != null) {
                    Session updated = replacedSession.withoutQuickAccessIndex(change.quickAccessIndex());
                    if (updated.empty()) {
                        sessionsByRequestId.remove(replacedRequestId);
                    } else {
                        sessionsByRequestId.put(replacedRequestId, updated);
                    }
                }
            }
        }
        trimSessions();
    }

    public static boolean isPendingQuickAccessIndex(int quickAccessIndex) {
        return isPendingQuickAccessIndex("", quickAccessIndex);
    }

    public static boolean isPendingQuickAccessIndex(String hostKey, int quickAccessIndex) {
        pruneExpired();
        return quickAccessRequestByScopedIndex.containsKey(scopedQuickAccessIndexKey(hostKey, quickAccessIndex));
    }

    public static boolean hasPendingQuickAccessTargets() {
        return hasPendingQuickAccessTargets("");
    }

    public static boolean hasPendingQuickAccessTargets(String hostKey) {
        pruneExpired();
        if (hostKey == null || hostKey.isBlank()) {
            return !quickAccessRequestByScopedIndex.isEmpty();
        }
        for (Session session : sessionsByRequestId.values()) {
            if (session != null
                    && hostKey.equals(session.hostKey())
                    && !session.quickAccessIndices().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public static void recordUseOffhand(ActionRequestId requestId, String routingKey, Object expectedMenu, ItemIdentity identity) {
        recordFollowUp(requestId, routingKey, QuickAccessFollowUpActionType.USE_OFFHAND, expectedMenu, identity, -1);
    }

    public static void recordDropMenuSlot(
            ActionRequestId requestId,
            String routingKey,
            Object expectedMenu,
            ItemIdentity identity,
            int targetMenuSlot
    ) {
        recordFollowUp(requestId, routingKey, QuickAccessFollowUpActionType.DROP_MENU_SLOT, expectedMenu, identity, targetMenuSlot);
    }

    public static boolean handleFollowUpTransferOutcome(ActionRequestId requestId, ActionStatus status) {
        pruneExpired();
        String requestValue = requestValue(requestId);
        if (requestValue.isBlank() || status == null) {
            return false;
        }

        Session session = sessionsByRequestId.get(requestValue);
        if (session == null || session.followUp() == null) {
            return false;
        }

        switch (status) {
            case REQUESTED, PENDING -> {
                return true;
            }
            case CONFIRMED -> {
                sessionsByRequestId.put(requestValue, session.confirmed(System.nanoTime()));
                return true;
            }
            case BLOCKED -> {
                publishFollowUpOutcome(session, QuickAccessActionSessionFeedback.blocked(session.followUp().type()));
                removeSession(requestValue);
                return true;
            }
            case FAILED -> {
                publishFollowUpOutcome(session, QuickAccessActionSessionFeedback.failed(session.followUp().type()));
                removeSession(requestValue);
                return true;
            }
        }
        return false;
    }

    public static List<PendingQuickAccessFollowUp> readyFollowUps() {
        return readyFollowUps("");
    }

    public static List<PendingQuickAccessFollowUp> readyFollowUps(String hostKey) {
        pruneExpired();
        List<PendingQuickAccessFollowUp> ready = new ArrayList<>();
        for (Session session : sessionsByRequestId.values()) {
            if (session != null
                    && session.followUp() != null
                    && session.confirmed()
                    && matchesHost(session, hostKey)) {
                ready.add(session.followUp().toPendingAction(
                        session.requestId(),
                        session.routingKey(),
                        session.createdAtNanos(),
                        session.confirmedAtNanos()
                ));
            }
        }
        return List.copyOf(ready);
    }

    public static void completeFollowUpApplied(ActionRequestId requestId) {
        completeFollowUp(requestId, true);
    }

    public static void completeFollowUpFailed(ActionRequestId requestId) {
        completeFollowUp(requestId, false);
    }

    public static boolean hasPendingFollowUpIdentity(ItemIdentity identity) {
        return hasPendingFollowUpIdentity("", identity);
    }

    public static boolean hasPendingFollowUpIdentity(String hostKey, ItemIdentity identity) {
        pruneExpired();
        if (identity == null) {
            return false;
        }

        for (Session session : sessionsByRequestId.values()) {
            if (session != null
                    && matchesHost(session, hostKey)
                    && session.followUp() != null
                    && identity.equals(session.followUp().identity())) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasPendingFollowUps() {
        return hasPendingFollowUps("");
    }

    public static boolean hasPendingFollowUps(String hostKey) {
        pruneExpired();
        for (Session session : sessionsByRequestId.values()) {
            if (session != null
                    && matchesHost(session, hostKey)
                    && session.followUp() != null) {
                return true;
            }
        }
        return false;
    }

    public static void completeQuickAccessRequest(ActionRequestId requestId) {
        pruneExpired();
        String requestValue = requestValue(requestId);
        if (requestValue.isBlank()) {
            return;
        }

        Session session = sessionsByRequestId.get(requestValue);
        if (session == null) {
            return;
        }

        Session updated = session.withoutQuickAccessIndices();
        for (int quickAccessIndex : session.quickAccessIndices()) {
            quickAccessRequestByScopedIndex.remove(scopedQuickAccessIndexKey(session.hostKey(), quickAccessIndex), requestValue);
        }

        if (updated.empty()) {
            sessionsByRequestId.remove(requestValue);
        } else {
            sessionsByRequestId.put(requestValue, updated);
        }
    }

    public static void completeRequest(ActionRequestId requestId) {
        pruneExpired();
        String requestValue = requestValue(requestId);
        if (requestValue.isBlank()) {
            return;
        }

        Session session = sessionsByRequestId.get(requestValue);
        if (session == null || session.followUp() != null || !session.quickAccessIndices().isEmpty()) {
            return;
        }
        sessionsByRequestId.remove(requestValue);
    }

    public static void publishOutcome(String routingKey, ActionRequestId requestId, ActionSessionResult result) {
        pruneExpired();
        if (routingKey == null || routingKey.isBlank() || result == null || !result.visible()) {
            return;
        }

        publishedOutcomes.add(new PublishedActionOutcome(
                routingKey,
                requestValue(requestId),
                result,
                System.nanoTime()
        ));
        trimOutcomes();
    }

    public static List<PublishedActionOutcome> pollAllOutcomes(String routingKey) {
        return pollOutcomes(routingKey, null);
    }

    public static List<PublishedActionOutcome> pollOutcomes(String routingKey, Set<String> requestIds) {
        pruneExpired();
        if (routingKey == null || routingKey.isBlank() || publishedOutcomes.isEmpty()) {
            return List.of();
        }

        List<PublishedActionOutcome> outcomes = new ArrayList<>();
        Iterator<PublishedActionOutcome> iterator = publishedOutcomes.iterator();
        while (iterator.hasNext()) {
            PublishedActionOutcome outcome = iterator.next();
            if (!routingKey.equals(outcome.routingKey())) {
                continue;
            }
            if (requestIds != null && !requestIds.contains(outcome.requestId())) {
                continue;
            }
            outcomes.add(outcome);
            iterator.remove();
        }
        return List.copyOf(outcomes);
    }

    public static void clearDeferredHistoryState() {
        pendingHistoryTransferRequests.clear();
        pendingHistoryTransferTransitions.clear();
        pendingHistoryQuickAccessOperations.clear();
        pendingHistoryTransferTransitionContexts.clear();
        pendingHistoryQuickAccessTransitionContexts.clear();
        pendingHistorySettlements.clear();
    }

    public static boolean hasPendingHistoryMutation(String contextKey) {
        return hasPendingHistoryMutation("", contextKey);
    }

    public static boolean hasPendingHistoryMutation(String hostKey, String contextKey) {
        pruneExpired();
        String resolvedKey = contextKey == null ? "" : contextKey;
        if (hasPendingQuickAccessTargets(hostKey) || hasPendingFollowUps(hostKey)) {
            return true;
        }
        if (pendingHistoryTransferTransitionContexts.contains(resolvedKey)
                || pendingHistoryQuickAccessTransitionContexts.contains(resolvedKey)) {
            return true;
        }

        for (PendingHistoryTransferRequest pendingRequest : pendingHistoryTransferRequests.values()) {
            if (Objects.equals(resolvedKey, pendingRequest.contextKey())) {
                return true;
            }
        }

        for (PendingHistoryTransferTransition pendingTransition : pendingHistoryTransferTransitions.values()) {
            if (Objects.equals(resolvedKey, pendingTransition.contextKey())) {
                return true;
            }
        }

        for (PendingHistoryQuickAccessOperation pendingOperation : pendingHistoryQuickAccessOperations.values()) {
            if (Objects.equals(resolvedKey, pendingOperation.contextKey())) {
                return true;
            }
        }

        return false;
    }

    public static void recordDeferredHistoryTransferRequest(String contextKey, ActionRequest request) {
        pruneExpired();
        if (request == null || !request.requestId().present() || contextKey == null || contextKey.isBlank()) {
            return;
        }

        PendingHistoryTransferRequest pendingRequest = PendingHistoryTransferRequest.from(contextKey, request, System.nanoTime());
        if (pendingRequest == null) {
            return;
        }

        pendingHistoryTransferRequests.put(request.requestId().value(), pendingRequest);
    }

    public static void registerDeferredQuickAccessHistoryRecord(
            String contextKey,
            HotbarLoadoutCapture before,
            HotbarLoadoutCapture localAfter,
            List<QuickAccessRequestedTarget> pendingChanges
    ) {
        pruneExpired();
        PendingHistoryQuickAccessRecord record = PendingHistoryQuickAccessRecord.create(
                contextKey,
                before,
                localAfter,
                pendingChanges,
                System.nanoTime()
        );
        if (record == null) {
            return;
        }
        registerPendingHistoryQuickAccessOperation(record);
    }

    public static void registerDeferredQuickAccessHistoryTransition(
            String contextKey,
            HistoryReplayDirection direction,
            HotbarLoadoutCapture actionBefore,
            HotbarLoadoutCapture actionAfter,
            HotbarLoadoutCapture startBefore,
            HotbarLoadoutCapture localAfter,
            List<QuickAccessRequestedTarget> pendingChanges
    ) {
        pruneExpired();
        PendingHistoryQuickAccessTransition transition = PendingHistoryQuickAccessTransition.create(
                contextKey,
                direction,
                actionBefore,
                actionAfter,
                startBefore,
                localAfter,
                pendingChanges,
                System.nanoTime()
        );
        if (transition == null) {
            return;
        }
        registerPendingHistoryQuickAccessOperation(transition);
        pendingHistoryQuickAccessTransitionContexts.add(contextKey == null ? "" : contextKey);
    }

    public static void registerDeferredTransferHistoryTransition(
            String contextKey,
            HistoryReplayDirection direction,
            HistoryTransferDirection actionDirection,
            List<RequestedHistoryTransfer> requestedTransfers
    ) {
        pruneExpired();
        PendingHistoryTransferTransition transition = PendingHistoryTransferTransition.create(
                contextKey,
                direction,
                actionDirection,
                requestedTransfers,
                System.nanoTime()
        );
        if (transition == null) {
            return;
        }
        registerPendingHistoryTransferTransition(transition);
        pendingHistoryTransferTransitionContexts.add(contextKey == null ? "" : contextKey);
    }

    public static void recordDeferredHistoryOutcome(ActionRequestId requestId, ActionStatus status, int affectedCount) {
        pruneExpired();
        if (requestId == null || !requestId.present()) {
            return;
        }

        PendingHistoryTransferTransition pendingTransferTransition = pendingHistoryTransferTransitions.remove(requestId.value());
        if (pendingTransferTransition != null) {
            pendingTransferTransition.resolve(requestId.value(), status, affectedCount);
            if (pendingTransferTransition.complete()) {
                clearPendingHistoryTransferTransition(pendingTransferTransition);
                pendingHistorySettlements.add(pendingTransferTransition.toSettlement());
            }
            return;
        }

        PendingHistoryQuickAccessOperation pendingQuickAccessAction = pendingHistoryQuickAccessOperations.remove(requestId.value());
        if (pendingQuickAccessAction != null) {
            pendingQuickAccessAction.resolve(requestId.value(), status);
            if (pendingQuickAccessAction.complete()) {
                clearPendingHistoryQuickAccessOperation(pendingQuickAccessAction);
                pendingHistorySettlements.add(pendingQuickAccessAction.toSettlement());
            }
            return;
        }

        PendingHistoryTransferRequest pendingRequest = pendingHistoryTransferRequests.remove(requestId.value());
        if (pendingRequest == null) {
            return;
        }
        if (status != ActionStatus.CONFIRMED || affectedCount <= 0) {
            return;
        }
        pendingHistorySettlements.add(new ConfirmedTransferRecordSettlement(
                pendingRequest.contextKey(),
                pendingRequest.direction(),
                List.of(new HistoryIdentityCount(pendingRequest.identity(), affectedCount))
        ));
    }

    public static List<HistorySettlement> drainHistorySettlements() {
        pruneExpired();
        if (pendingHistorySettlements.isEmpty()) {
            return List.of();
        }
        List<HistorySettlement> drained = List.copyOf(pendingHistorySettlements);
        pendingHistorySettlements.clear();
        return drained;
    }

    private static void completeFollowUp(ActionRequestId requestId, boolean applied) {
        pruneExpired();
        String requestValue = requestValue(requestId);
        if (requestValue.isBlank()) {
            return;
        }

        Session session = sessionsByRequestId.get(requestValue);
        if (session == null || session.followUp() == null) {
            return;
        }

        publishFollowUpOutcome(
                session,
                applied
                        ? QuickAccessActionSessionFeedback.applied(session.followUp().type())
                        : QuickAccessActionSessionFeedback.failed(session.followUp().type())
        );
        removeSession(requestValue);
    }

    private static void publishFollowUpOutcome(Session session, ActionSessionResult result) {
        if (session == null) {
            return;
        }
        publishOutcome(session.routingKey(), new ActionRequestId(session.requestId()), result);
    }

    private static void recordFollowUp(
            ActionRequestId requestId,
            String routingKey,
            QuickAccessFollowUpActionType type,
            Object expectedMenu,
            ItemIdentity identity,
            int targetMenuSlot
    ) {
        pruneExpired();
        String requestValue = requestValue(requestId);
        if (requestValue.isBlank() || identity == null || type == null) {
            return;
        }

        Session session = session(requestValue)
                .withRoutingKey(routingKey == null ? "" : routingKey)
                .withFollowUp(new FollowUp(type, expectedMenu, identity, targetMenuSlot));
        sessionsByRequestId.put(requestValue, session);
        trimSessions();
    }

    private static Session session(String requestId) {
        return session("", requestId);
    }

    private static Session session(String hostKey, String requestId) {
        Session existing = sessionsByRequestId.get(requestId);
        return existing == null ? new Session(hostKey == null ? "" : hostKey, requestId, "", -1L, Set.of(), null, -1L) : existing;
    }

    private static void removeSession(String requestId) {
        Session session = sessionsByRequestId.remove(requestId);
        if (session == null) {
            return;
        }
        for (int quickAccessIndex : session.quickAccessIndices()) {
            quickAccessRequestByScopedIndex.remove(scopedQuickAccessIndexKey(session.hostKey(), quickAccessIndex), requestId);
        }
    }

    private static boolean matchesHost(Session session, String hostKey) {
        if (session == null) {
            return false;
        }
        return hostKey == null || hostKey.isBlank() || hostKey.equals(session.hostKey());
    }

    private static String scopedQuickAccessIndexKey(String hostKey, int quickAccessIndex) {
        return (hostKey == null ? "" : hostKey) + "|" + quickAccessIndex;
    }

    private static void pruneExpired() {
        long now = System.nanoTime();

        Iterator<Map.Entry<String, Session>> sessionIterator = sessionsByRequestId.entrySet().iterator();
        while (sessionIterator.hasNext()) {
            Map.Entry<String, Session> entry = sessionIterator.next();
            Session session = entry.getValue();
            if (session == null || session.expired(now)) {
                if (session != null) {
                    for (int quickAccessIndex : session.quickAccessIndices()) {
                        quickAccessRequestByScopedIndex.remove(scopedQuickAccessIndexKey(session.hostKey(), quickAccessIndex), entry.getKey());
                    }
                    if (session.followUp() != null) {
                        publishFollowUpOutcome(
                                session,
                                QuickAccessActionSessionFeedback.failed(session.followUp().type())
                        );
                    }
                }
                sessionIterator.remove();
            }
        }

        Iterator<PublishedActionOutcome> outcomeIterator = publishedOutcomes.iterator();
        while (outcomeIterator.hasNext()) {
            PublishedActionOutcome outcome = outcomeIterator.next();
            if (outcome == null || now - outcome.publishedAtNanos() > SESSION_TIMEOUT_NANOS) {
                outcomeIterator.remove();
            }
        }

        Iterator<Map.Entry<String, PendingHistoryTransferRequest>> requestIterator = pendingHistoryTransferRequests.entrySet().iterator();
        while (requestIterator.hasNext()) {
            Map.Entry<String, PendingHistoryTransferRequest> entry = requestIterator.next();
            PendingHistoryTransferRequest pendingRequest = entry.getValue();
            if (pendingRequest == null || now - pendingRequest.createdAtNanos() > SESSION_TIMEOUT_NANOS) {
                requestIterator.remove();
            }
        }

        Set<PendingHistoryTransferTransition> expiredTransitions = new LinkedHashSet<>();
        for (PendingHistoryTransferTransition transition : pendingHistoryTransferTransitions.values()) {
            if (transition != null && now - transition.createdAtNanos() > SESSION_TIMEOUT_NANOS) {
                expiredTransitions.add(transition);
            }
        }
        for (PendingHistoryTransferTransition transition : expiredTransitions) {
            clearPendingHistoryTransferTransition(transition);
            pendingHistorySettlements.add(transition.toSettlement());
        }

        Set<PendingHistoryQuickAccessOperation> expiredQuickAccessOperations = new LinkedHashSet<>();
        for (PendingHistoryQuickAccessOperation operation : pendingHistoryQuickAccessOperations.values()) {
            if (operation != null && now - operation.createdAtNanos() > SESSION_TIMEOUT_NANOS) {
                expiredQuickAccessOperations.add(operation);
            }
        }
        for (PendingHistoryQuickAccessOperation operation : expiredQuickAccessOperations) {
            clearPendingHistoryQuickAccessOperation(operation);
            pendingHistorySettlements.add(operation.toSettlement());
        }
    }

    private static void trimSessions() {
        if (sessionsByRequestId.size() <= MAX_PENDING_SESSIONS) {
            return;
        }

        int removed = 0;
        Iterator<Map.Entry<String, Session>> iterator = sessionsByRequestId.entrySet().iterator();
        while (sessionsByRequestId.size() > MAX_PENDING_SESSIONS && iterator.hasNext()) {
            Map.Entry<String, Session> entry = iterator.next();
            Session session = entry.getValue();
            iterator.remove();
            if (session != null) {
                for (int quickAccessIndex : session.quickAccessIndices()) {
                    quickAccessRequestByScopedIndex.remove(scopedQuickAccessIndexKey(session.hostKey(), quickAccessIndex), entry.getKey());
                }
                if (session.followUp() != null) {
                    publishFollowUpOutcome(session, QuickAccessActionSessionFeedback.failed(session.followUp().type()));
                }
            }
            removed++;
        }
        if (removed > 0) {
            SlotDebugLog.log("Action session overflow trimmed: removed={} remaining={}", removed, sessionsByRequestId.size());
        }
    }

    private static void trimOutcomes() {
        if (publishedOutcomes.size() <= MAX_PENDING_OUTCOMES) {
            return;
        }

        int removed = 0;
        while (publishedOutcomes.size() > MAX_PENDING_OUTCOMES) {
            publishedOutcomes.remove(0);
            removed++;
        }
        if (removed > 0) {
            SlotDebugLog.log("Action outcome overflow trimmed: removed={} remaining={}", removed, publishedOutcomes.size());
        }
    }

    private static String requestValue(ActionRequestId requestId) {
        return requestId != null && requestId.present() ? requestId.value() : "";
    }

    private static HotbarLoadoutCapture applyQuickAccessChange(
            HotbarLoadoutCapture capture,
            QuickAccessRequestedTarget change
    ) {
        if (capture == null || change == null || change.identity() == null) {
            return capture;
        }

        Map<Integer, ItemIdentity> identitiesBySlot = new LinkedHashMap<>();
        for (HotbarLoadoutSlot slot : capture.slots()) {
            if (slot != null && slot.identity() != null) {
                identitiesBySlot.put(slot.slotIndex(), slot.identity());
            }
        }
        if (change.quickAccessIndex() == HotbarLoadoutDefinition.OFFHAND_SLOT_INDEX) {
            List<HotbarLoadoutSlot> slots = orderedQuickAccessSlots(identitiesBySlot);
            return new HotbarLoadoutCapture(slots, change.identity());
        }

        identitiesBySlot.put(change.quickAccessIndex(), change.identity());
        return new HotbarLoadoutCapture(orderedQuickAccessSlots(identitiesBySlot), capture.offhandIdentity());
    }

    private static List<HotbarLoadoutSlot> orderedQuickAccessSlots(Map<Integer, ItemIdentity> identitiesBySlot) {
        List<HotbarLoadoutSlot> slots = new ArrayList<>();
        for (int slotIndex = 0; slotIndex < HotbarLoadoutDefinition.HOTBAR_SLOT_COUNT; slotIndex++) {
            ItemIdentity identity = identitiesBySlot.get(slotIndex);
            if (identity != null) {
                slots.add(new HotbarLoadoutSlot(slotIndex, identity));
            }
        }
        return List.copyOf(slots);
    }

    private static void registerPendingHistoryQuickAccessOperation(PendingHistoryQuickAccessOperation operation) {
        if (operation == null) {
            return;
        }

        for (String requestId : operation.requestIds()) {
            pendingHistoryQuickAccessOperations.put(requestId, operation);
        }
    }

    private static void clearPendingHistoryQuickAccessOperation(PendingHistoryQuickAccessOperation operation) {
        if (operation == null) {
            return;
        }
        for (String requestId : operation.requestIds()) {
            pendingHistoryQuickAccessOperations.remove(requestId, operation);
        }
        if (operation instanceof PendingHistoryQuickAccessTransition transition) {
            pendingHistoryQuickAccessTransitionContexts.remove(transition.contextKey());
        }
    }

    private static void registerPendingHistoryTransferTransition(PendingHistoryTransferTransition transition) {
        if (transition == null) {
            return;
        }

        for (String requestId : transition.requestIds()) {
            pendingHistoryTransferTransitions.put(requestId, transition);
            pendingHistoryTransferRequests.remove(requestId);
        }
    }

    private static void clearPendingHistoryTransferTransition(PendingHistoryTransferTransition transition) {
        if (transition == null) {
            return;
        }
        for (String requestId : transition.requestIds()) {
            pendingHistoryTransferTransitions.remove(requestId, transition);
        }
        pendingHistoryTransferTransitionContexts.remove(transition.contextKey());
    }

    private record FollowUp(
            QuickAccessFollowUpActionType type,
            Object expectedMenu,
            ItemIdentity identity,
            int targetMenuSlot
    ) {
        private FollowUp {
            Objects.requireNonNull(type, "type");
            Objects.requireNonNull(identity, "identity");
        }

        private PendingQuickAccessFollowUp toPendingAction(
                String requestId,
                String routingKey,
                long createdAtNanos,
                long confirmedAtNanos
        ) {
            return new PendingQuickAccessFollowUp(
                    requestId,
                    routingKey,
                    type,
                    expectedMenu,
                    identity,
                    targetMenuSlot,
                    createdAtNanos,
                    confirmedAtNanos
            );
        }
    }

    private record Session(
            String hostKey,
            String requestId,
            String routingKey,
            long createdAtNanos,
            Set<Integer> quickAccessIndices,
            FollowUp followUp,
            long confirmedAtNanos
    ) {
        private Session {
            hostKey = hostKey == null ? "" : hostKey;
            requestId = requestId == null ? "" : requestId;
            routingKey = routingKey == null ? "" : routingKey;
            quickAccessIndices = quickAccessIndices == null || quickAccessIndices.isEmpty()
                    ? Set.of()
                    : Set.copyOf(new LinkedHashSet<>(quickAccessIndices));
        }

        private Session withRoutingKey(String routingKey) {
            return new Session(hostKey, requestId, routingKey == null ? this.routingKey : routingKey, createdAtNanos, quickAccessIndices, followUp, confirmedAtNanos);
        }

        private Session withCreatedAt(long createdAtNanos) {
            return new Session(hostKey, requestId, routingKey, createdAtNanos, quickAccessIndices, followUp, confirmedAtNanos);
        }

        private Session withQuickAccessIndex(int quickAccessIndex) {
            LinkedHashSet<Integer> indices = new LinkedHashSet<>(quickAccessIndices);
            indices.add(quickAccessIndex);
            long created = createdAtNanos < 0L ? System.nanoTime() : createdAtNanos;
            return new Session(hostKey, requestId, routingKey, created, indices, followUp, confirmedAtNanos);
        }

        private Session withoutQuickAccessIndex(int quickAccessIndex) {
            if (!quickAccessIndices.contains(quickAccessIndex)) {
                return this;
            }
            LinkedHashSet<Integer> indices = new LinkedHashSet<>(quickAccessIndices);
            indices.remove(quickAccessIndex);
            return new Session(hostKey, requestId, routingKey, createdAtNanos, indices, followUp, confirmedAtNanos);
        }

        private Session withoutQuickAccessIndices() {
            return new Session(hostKey, requestId, routingKey, createdAtNanos, Set.of(), followUp, confirmedAtNanos);
        }

        private Session withFollowUp(FollowUp followUp) {
            long created = createdAtNanos < 0L ? System.nanoTime() : createdAtNanos;
            return new Session(hostKey, requestId, routingKey, created, quickAccessIndices, followUp, confirmedAtNanos);
        }

        private Session confirmed(long confirmedAtNanos) {
            return new Session(hostKey, requestId, routingKey, createdAtNanos < 0L ? System.nanoTime() : createdAtNanos, quickAccessIndices, followUp, confirmedAtNanos);
        }

        private boolean confirmed() {
            return followUp != null && confirmedAtNanos >= 0L;
        }

        private boolean expired(long now) {
            long created = createdAtNanos < 0L ? now : createdAtNanos;
            if (confirmed()) {
                return now - confirmedAtNanos > CONFIRMED_FOLLOW_UP_TIMEOUT_NANOS;
            }
            return now - created > SESSION_TIMEOUT_NANOS;
        }

        private boolean empty() {
            return quickAccessIndices.isEmpty() && followUp == null;
        }
    }

    public enum HistoryTransferDirection {
        EXTERNAL_TO_CARRIED,
        CARRIED_TO_EXTERNAL
    }

    public enum HistoryReplayDirection {
        UNDO,
        REDO
    }

    public record HistoryIdentityCount(ItemIdentity identity, int count) {
        public HistoryIdentityCount {
            Objects.requireNonNull(identity, "identity");
            if (count <= 0) {
                throw new IllegalArgumentException("count must be positive");
            }
        }
    }

    public record RequestedHistoryTransfer(String requestId, ItemIdentity identity, int requestedCount) {
        public RequestedHistoryTransfer {
            requestId = requestId == null ? "" : requestId;
            Objects.requireNonNull(identity, "identity");
            if (requestedCount <= 0) {
                throw new IllegalArgumentException("requestedCount must be positive");
            }
        }

        public boolean pending() {
            return !requestId.isBlank();
        }
    }

    public sealed interface HistorySettlement permits
            ConfirmedTransferRecordSettlement,
            QuickAccessRecordSettlement,
            TransferTransitionSettlement,
            QuickAccessTransitionSettlement {
        String contextKey();
    }

    public record ConfirmedTransferRecordSettlement(
            String contextKey,
            HistoryTransferDirection direction,
            List<HistoryIdentityCount> moved
    ) implements HistorySettlement {
    }

    public record QuickAccessRecordSettlement(
            String contextKey,
            HotbarLoadoutCapture before,
            HotbarLoadoutCapture settledAfter
    ) implements HistorySettlement {
    }

    public record TransferTransitionSettlement(
            String contextKey,
            HistoryReplayDirection direction,
            HistoryTransferDirection actionDirection,
            List<HistoryIdentityCount> confirmed,
            List<HistoryIdentityCount> residual
    ) implements HistorySettlement {
    }

    public record QuickAccessTransitionSettlement(
            String contextKey,
            HistoryReplayDirection direction,
            HotbarLoadoutCapture actionBefore,
            HotbarLoadoutCapture actionAfter,
            HotbarLoadoutCapture startBefore,
            HotbarLoadoutCapture settledAfter
    ) implements HistorySettlement {
    }

    private record PendingHistoryTransferRequest(
            String contextKey,
            HistoryTransferDirection direction,
            ItemIdentity identity,
            long createdAtNanos
    ) {
        private PendingHistoryTransferRequest {
            Objects.requireNonNull(contextKey, "contextKey");
            Objects.requireNonNull(direction, "direction");
            Objects.requireNonNull(identity, "identity");
        }

        private static PendingHistoryTransferRequest from(String contextKey, ActionRequest request, long createdAtNanos) {
            if (request == null || request.primarySourceRef() == null) {
                return null;
            }

            ItemIdentity identity = ActionRequestIdentityCodec.decode(request.identityKey());
            if (identity == null) {
                return null;
            }

            String sourceKind = request.primarySourceRef().kind();
            String sourceId = request.primarySourceRef().sourceId().value();
            HistoryTransferDirection direction = switch (request.actionFamily()) {
                case TRANSFER -> BackpackTransferActionRequests.KIND_EXTERNAL_IDENTITY.equals(sourceKind)
                        && BackpackTransferActionRequests.SOURCE_OPEN_CONTAINER.equals(sourceId)
                        ? HistoryTransferDirection.EXTERNAL_TO_CARRIED
                        : null;
                case STORE -> BackpackTransferActionRequests.KIND_CARRIED_IDENTITY.equals(sourceKind)
                        && BackpackTransferActionRequests.SOURCE_CARRIED.equals(sourceId)
                        ? HistoryTransferDirection.CARRIED_TO_EXTERNAL
                        : null;
                default -> null;
            };
            if (direction == null) {
                return null;
            }

            return new PendingHistoryTransferRequest(contextKey, direction, identity, createdAtNanos);
        }
    }

    private abstract static class PendingHistoryQuickAccessOperation {
        private final String contextKey;
        private final long createdAtNanos;
        private HotbarLoadoutCapture settledAfter;
        private final LinkedHashMap<String, QuickAccessRequestedTarget> pendingChangesByRequestId;
        private final List<String> requestIds;

        private PendingHistoryQuickAccessOperation(
                String contextKey,
                long createdAtNanos,
                HotbarLoadoutCapture settledAfter,
                LinkedHashMap<String, QuickAccessRequestedTarget> pendingChangesByRequestId
        ) {
            this.contextKey = contextKey == null ? "" : contextKey;
            this.createdAtNanos = createdAtNanos;
            this.settledAfter = settledAfter;
            this.pendingChangesByRequestId = pendingChangesByRequestId;
            this.requestIds = List.copyOf(pendingChangesByRequestId.keySet());
        }

        static LinkedHashMap<String, QuickAccessRequestedTarget> pendingChangesByRequestId(
                List<QuickAccessRequestedTarget> pendingChanges
        ) {
            if (pendingChanges == null || pendingChanges.isEmpty()) {
                return null;
            }

            LinkedHashMap<String, QuickAccessRequestedTarget> changesByRequestId = new LinkedHashMap<>();
            for (QuickAccessRequestedTarget change : pendingChanges) {
                if (change == null || change.requestId() == null || !change.requestId().present()) {
                    return null;
                }
                changesByRequestId.put(change.requestId().value(), change);
            }
            return changesByRequestId.size() == pendingChanges.size() ? changesByRequestId : null;
        }

        final String contextKey() {
            return contextKey;
        }

        final long createdAtNanos() {
            return createdAtNanos;
        }

        final List<String> requestIds() {
            return requestIds;
        }

        final void resolve(String requestId, ActionStatus status) {
            QuickAccessRequestedTarget change = pendingChangesByRequestId.remove(requestId);
            if (change == null) {
                return;
            }
            if (status == ActionStatus.CONFIRMED) {
                settledAfter = applyQuickAccessChange(settledAfter, change);
            }
        }

        final boolean complete() {
            return pendingChangesByRequestId.isEmpty();
        }

        final HotbarLoadoutCapture settledAfter() {
            return settledAfter;
        }

        abstract HistorySettlement toSettlement();
    }

    private static final class PendingHistoryQuickAccessRecord extends PendingHistoryQuickAccessOperation {
        private final HotbarLoadoutCapture before;

        private PendingHistoryQuickAccessRecord(
                String contextKey,
                long createdAtNanos,
                HotbarLoadoutCapture before,
                HotbarLoadoutCapture settledAfter,
                LinkedHashMap<String, QuickAccessRequestedTarget> pendingChangesByRequestId
        ) {
            super(contextKey, createdAtNanos, settledAfter, pendingChangesByRequestId);
            this.before = before;
        }

        private static PendingHistoryQuickAccessRecord create(
                String contextKey,
                HotbarLoadoutCapture before,
                HotbarLoadoutCapture localAfter,
                List<QuickAccessRequestedTarget> pendingChanges,
                long createdAtNanos
        ) {
            LinkedHashMap<String, QuickAccessRequestedTarget> changesByRequestId =
                    pendingChangesByRequestId(pendingChanges);
            if (before == null || localAfter == null || changesByRequestId == null) {
                return null;
            }
            return new PendingHistoryQuickAccessRecord(contextKey, createdAtNanos, before, localAfter, changesByRequestId);
        }

        @Override
        HistorySettlement toSettlement() {
            return new QuickAccessRecordSettlement(contextKey(), before, settledAfter());
        }
    }

    private static final class PendingHistoryQuickAccessTransition extends PendingHistoryQuickAccessOperation {
        private final HistoryReplayDirection direction;
        private final HotbarLoadoutCapture actionBefore;
        private final HotbarLoadoutCapture actionAfter;
        private final HotbarLoadoutCapture startBefore;

        private PendingHistoryQuickAccessTransition(
                String contextKey,
                long createdAtNanos,
                HistoryReplayDirection direction,
                HotbarLoadoutCapture actionBefore,
                HotbarLoadoutCapture actionAfter,
                HotbarLoadoutCapture startBefore,
                HotbarLoadoutCapture settledAfter,
                LinkedHashMap<String, QuickAccessRequestedTarget> pendingChangesByRequestId
        ) {
            super(contextKey, createdAtNanos, settledAfter, pendingChangesByRequestId);
            this.direction = direction;
            this.actionBefore = actionBefore;
            this.actionAfter = actionAfter;
            this.startBefore = startBefore;
        }

        private static PendingHistoryQuickAccessTransition create(
                String contextKey,
                HistoryReplayDirection direction,
                HotbarLoadoutCapture actionBefore,
                HotbarLoadoutCapture actionAfter,
                HotbarLoadoutCapture startBefore,
                HotbarLoadoutCapture localAfter,
                List<QuickAccessRequestedTarget> pendingChanges,
                long createdAtNanos
        ) {
            LinkedHashMap<String, QuickAccessRequestedTarget> changesByRequestId =
                    pendingChangesByRequestId(pendingChanges);
            if (direction == null
                    || actionBefore == null
                    || actionAfter == null
                    || startBefore == null
                    || localAfter == null
                    || changesByRequestId == null) {
                return null;
            }
            return new PendingHistoryQuickAccessTransition(
                    contextKey,
                    createdAtNanos,
                    direction,
                    actionBefore,
                    actionAfter,
                    startBefore,
                    localAfter,
                    changesByRequestId
            );
        }

        @Override
        HistorySettlement toSettlement() {
            return new QuickAccessTransitionSettlement(
                    contextKey(),
                    direction,
                    actionBefore,
                    actionAfter,
                    startBefore,
                    settledAfter()
            );
        }
    }

    private static final class PendingHistoryTransferTransition {
        private final String contextKey;
        private final long createdAtNanos;
        private final HistoryReplayDirection direction;
        private final HistoryTransferDirection actionDirection;
        private final List<RequestedTransfer> requestedTransfers;
        private final LinkedHashMap<String, RequestedTransfer> pendingTransfersByRequestId;

        private PendingHistoryTransferTransition(
                String contextKey,
                long createdAtNanos,
                HistoryReplayDirection direction,
                HistoryTransferDirection actionDirection,
                List<RequestedTransfer> requestedTransfers,
                LinkedHashMap<String, RequestedTransfer> pendingTransfersByRequestId
        ) {
            this.contextKey = contextKey == null ? "" : contextKey;
            this.createdAtNanos = createdAtNanos;
            this.direction = direction;
            this.actionDirection = actionDirection;
            this.requestedTransfers = requestedTransfers;
            this.pendingTransfersByRequestId = pendingTransfersByRequestId;
        }

        private static PendingHistoryTransferTransition create(
                String contextKey,
                HistoryReplayDirection direction,
                HistoryTransferDirection actionDirection,
                List<RequestedHistoryTransfer> requestedTransfers,
                long createdAtNanos
        ) {
            if (direction == null || actionDirection == null || requestedTransfers == null || requestedTransfers.isEmpty()) {
                return null;
            }

            LinkedHashMap<String, RequestedTransfer> pendingTransfersByRequestId = new LinkedHashMap<>();
            List<RequestedTransfer> normalized = new ArrayList<>(requestedTransfers.size());
            for (RequestedHistoryTransfer requestedTransfer : requestedTransfers) {
                if (requestedTransfer == null || requestedTransfer.identity() == null || requestedTransfer.requestedCount() <= 0) {
                    return null;
                }

                RequestedTransfer normalizedTransfer = requestedTransfer.pending()
                        ? new RequestedTransfer(requestedTransfer.requestId(), requestedTransfer.identity(), requestedTransfer.requestedCount(), 0)
                        : new RequestedTransfer("", requestedTransfer.identity(), requestedTransfer.requestedCount(), 0);
                normalized.add(normalizedTransfer);
                if (normalizedTransfer.pending()) {
                    pendingTransfersByRequestId.put(normalizedTransfer.requestId(), normalizedTransfer);
                }
            }

            return new PendingHistoryTransferTransition(
                    contextKey,
                    createdAtNanos,
                    direction,
                    actionDirection,
                    List.copyOf(normalized),
                    pendingTransfersByRequestId
            );
        }

        private List<String> requestIds() {
            return List.copyOf(pendingTransfersByRequestId.keySet());
        }

        private String contextKey() {
            return contextKey;
        }

        private long createdAtNanos() {
            return createdAtNanos;
        }

        private void resolve(String requestId, ActionStatus status, int affectedCount) {
            RequestedTransfer requestedTransfer = pendingTransfersByRequestId.remove(requestId);
            if (requestedTransfer == null || status != ActionStatus.CONFIRMED) {
                return;
            }
            requestedTransfer.confirm(Math.max(0, Math.min(requestedTransfer.requestedCount(), affectedCount)));
        }

        private boolean complete() {
            return pendingTransfersByRequestId.isEmpty();
        }

        private TransferTransitionSettlement toSettlement() {
            List<HistoryIdentityCount> confirmed = new ArrayList<>();
            List<HistoryIdentityCount> residual = new ArrayList<>();
            for (RequestedTransfer requestedTransfer : requestedTransfers) {
                if (requestedTransfer.confirmedCount() > 0) {
                    confirmed.add(new HistoryIdentityCount(requestedTransfer.identity(), requestedTransfer.confirmedCount()));
                }
                int residualCount = requestedTransfer.requestedCount() - requestedTransfer.confirmedCount();
                if (residualCount > 0) {
                    residual.add(new HistoryIdentityCount(requestedTransfer.identity(), residualCount));
                }
            }
            return new TransferTransitionSettlement(contextKey, direction, actionDirection, List.copyOf(confirmed), List.copyOf(residual));
        }

        private static final class RequestedTransfer {
            private final String requestId;
            private final ItemIdentity identity;
            private final int requestedCount;
            private int confirmedCount;

            private RequestedTransfer(String requestId, ItemIdentity identity, int requestedCount, int confirmedCount) {
                this.requestId = requestId == null ? "" : requestId;
                this.identity = Objects.requireNonNull(identity, "identity");
                this.requestedCount = requestedCount;
                this.confirmedCount = confirmedCount;
            }

            private boolean pending() {
                return !requestId.isBlank();
            }

            private String requestId() {
                return requestId;
            }

            private ItemIdentity identity() {
                return identity;
            }

            private int requestedCount() {
                return requestedCount;
            }

            private int confirmedCount() {
                return confirmedCount;
            }

            private void confirm(int affectedCount) {
                confirmedCount = Math.max(0, Math.min(requestedCount, affectedCount));
            }
        }
    }
}
