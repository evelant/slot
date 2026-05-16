package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.classification.FacetIndex;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.goal.GoalIngredientDescriptor;
import dev.imagio.slot.inventory.goal.GoalPlanState;
import dev.imagio.slot.inventory.goal.GoalRecipeDescriptor;
import dev.imagio.slot.inventory.goal.GoalStackDescriptor;
import dev.imagio.slot.workflow.domain.ContextualContextAggregate;
import dev.imagio.slot.workflow.domain.ContextualItemAggregate;
import dev.imagio.slot.workflow.domain.ContextualSignalEvent;
import dev.imagio.slot.workflow.domain.ContextualSignalKind;
import dev.imagio.slot.workflow.domain.ContextualSignalRecord;
import dev.imagio.slot.workflow.domain.ContextualSuggestionState;
import dev.imagio.slot.workflow.domain.WorkflowDomainSnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class ContextualSuggestionScorer {
    private static final int LANE_LIMIT = 6;
    private static final double USEFUL_THRESHOLD = 0.9D;
    private static final double PUT_AWAY_THRESHOLD = 1.0D;
    private static final double DESIRED_EXCESS_PUT_AWAY_SCORE = 8.0D;
    private static final double PASSIVE_CARRIED_RELEVANCE_CAP = 0.35D;
    private static final String USEFUL_NOW_WAITING_TEXT = "Waiting for player actions to suggest items...";
    private static final Set<String> LOW_INFORMATION_TEXT_TOKENS = Set.of(
            "and",
            "are",
            "can",
            "for",
            "from",
            "into",
            "its",
            "the",
            "their",
            "this",
            "those",
            "use",
            "used",
            "uses",
            "using",
            "when",
            "while",
            "with",
            "your");

    private ContextualSuggestionScorer() {
    }

    public static List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes(
            List<SlotWorkspaceViewModel.AtlasItem> atlasItems,
            WorkflowDomainSnapshot workflow,
            FacetIndex facetIndex,
            int carriedFreeSlotCount,
            int carriedSlotCapacity
    ) {
        return lanes(atlasItems, workflow, facetIndex, carriedFreeSlotCount, carriedSlotCapacity, 0L);
    }

    public static List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes(
            List<SlotWorkspaceViewModel.AtlasItem> atlasItems,
            WorkflowDomainSnapshot workflow,
            FacetIndex facetIndex,
            int carriedFreeSlotCount,
            int carriedSlotCapacity,
            long currentGameTick
    ) {
        if (atlasItems == null || atlasItems.isEmpty()) {
            return List.of();
        }
        WorkflowDomainSnapshot snapshot = workflow == null ? WorkflowDomainSnapshot.empty() : workflow;
        FacetIndex index = facetIndex == null ? FacetIndex.empty() : facetIndex;
        ContextualSuggestionState state = snapshot.contextualSuggestions();
        Set<ItemIdentity> goalIdentities = goalIdentities(snapshot.goalPlans());
        double pressure = carriedSlotCapacity <= 0
                ? 0D
                : Math.max(0D, Math.min(1D, (carriedSlotCapacity - carriedFreeSlotCount) / (double) carriedSlotCapacity));
        ScoringContext scoringContext = ScoringContext.create(state, index);

        List<ScoredItem> useful = new ArrayList<>();
        List<ScoredItem> putAway = new ArrayList<>();
        ContextualContextAggregate activeContext = state.contextAggregates().get(state.activeContextKey());
        for (SlotWorkspaceViewModel.AtlasItem item : atlasItems) {
            if (item == null || item.identity() == null) {
                continue;
            }
            ItemIdentity identity = item.identity().toIdentity();
            Map<String, Double> vector = scoringContext.facetVector(item.identity().itemId());
            RelevanceBreakdown relevance = relevance(item, vector, scoringContext, identity, activeContext, false);
            double depositPenalty = scoringContext.depositPenalty(identity);
            UsefulScoreInputs usefulInputs = usefulInputs(item, relevance.total(), depositPenalty);
            double usefulScore = usefulInputs.score();
            if (usefulScore >= USEFUL_THRESHOLD) {
                useful.add(new ScoredItem(
                        item,
                        usefulScore,
                        relevance,
                        usefulReasons(item, usefulInputs, relevance)));
            }
            RelevanceBreakdown cleanupRelevance = relevance(item, vector, scoringContext, identity, activeContext, true);
            PutAwayScoreInputs putAwayInputs = putAwayInputs(
                    item, vector, cleanupRelevance, state, index, pressure, goalIdentities, currentGameTick);
            double putAwayScore = putAwayInputs.score();
            if (putAwayScore >= PUT_AWAY_THRESHOLD) {
                putAway.add(new ScoredItem(
                        item,
                        putAwayScore,
                        cleanupRelevance,
                        putAwayReasons(item, putAwayInputs, cleanupRelevance, state)));
            }
        }

        ArrayList<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = new ArrayList<>(2);
        List<ScoredItem> usefulSorted = sorted(useful);
        List<SlotWorkspaceViewModel.AtlasItem> usefulItems = items(usefulSorted);
        lanes.add(new SlotWorkspaceViewModel.ContextualSuggestionLane(
                SlotWorkspaceViewModel.ContextualSuggestionLane.USEFUL_NOW,
                "Useful Now",
                usefulItems,
                usefulItems.isEmpty() ? USEFUL_NOW_WAITING_TEXT : "",
                debugInfo(usefulSorted)));
        List<ScoredItem> putAwaySorted = sorted(putAway);
        List<SlotWorkspaceViewModel.AtlasItem> putAwayItems = items(putAwaySorted);
        if (!putAwayItems.isEmpty()) {
            lanes.add(new SlotWorkspaceViewModel.ContextualSuggestionLane(
                    SlotWorkspaceViewModel.ContextualSuggestionLane.PUT_AWAY,
                    "Put Away",
                    putAwayItems,
                    "",
                    debugInfo(putAwaySorted)));
        }
        return List.copyOf(lanes);
    }

    private static UsefulScoreInputs usefulInputs(
            SlotWorkspaceViewModel.AtlasItem item,
            double relevance,
            double depositPenalty
    ) {
        double resolvedDepositPenalty = Math.max(0D, depositPenalty);
        double carriedBoost = item.carried() && item.desiredCount() <= 0 && relevance > 0.15D ? 0.45D : 0D;
        double targetBoost = item.wantedCount() > 0 || item.kitNeeded() ? 1.35D : 0D;
        double desiredPenalty = item.carried() && item.desiredCount() > 0 && !item.desiredCountFromKit() ? 0.35D : 0D;
        double missingSourcePenalty =
                !item.carried() && item.presence().isEmpty() && item.elsewhere().isEmpty() && !item.kitNeeded()
                        ? 0.8D
                        : 0D;
        return new UsefulScoreInputs(
                item.isCarriedContainer(),
                relevance,
                resolvedDepositPenalty,
                carriedBoost,
                targetBoost,
                desiredPenalty,
                missingSourcePenalty);
    }

    private static PutAwayScoreInputs putAwayInputs(
            SlotWorkspaceViewModel.AtlasItem item,
            Map<String, Double> vector,
            RelevanceBreakdown relevance,
            ContextualSuggestionState state,
            FacetIndex index,
            double pressure,
            Set<ItemIdentity> goalIdentities,
        long currentGameTick
    ) {
        if (!item.carried()) {
            return PutAwayScoreInputs.ineligible();
        }
        boolean desiredExcess = hasDesiredExcess(item);
        if (!desiredExcess && (item.kitNeeded() || item.desiredCount() > 0 || item.wantedCount() > 0)) {
            return PutAwayScoreInputs.ineligible();
        }
        ItemIdentity identity = item.identity().toIdentity();
        if (!desiredExcess && identity != null && goalIdentities.contains(identity)) {
            return PutAwayScoreInputs.ineligible();
        }
        if (!desiredExcess && protectedSource(item.largestCarriedSourceId())) {
            return PutAwayScoreInputs.ineligible();
        }
        double pressureBoost = pressure * 0.85D;
        double storageRouteBoost = !item.presence().isEmpty() || !item.elsewhere().isEmpty() ? 0.45D : 0D;
        if (desiredExcess) {
            int excess = Math.max(1, item.totalCount() - item.desiredCount());
            return PutAwayScoreInputs.desiredExcess(
                    DESIRED_EXCESS_PUT_AWAY_SCORE,
                    Math.min(1.5D, Math.log1p(excess) * 0.35D),
                    pressureBoost,
                    storageRouteBoost);
        }

        ContextualItemAggregate aggregate = identity == null ? null : state.itemAggregates().get(identity);
        double depositHistoryBoost = 0D;
        double shortCarryBoost = 0D;
        double longCarryPenalty = 0D;
        if (aggregate != null) {
            depositHistoryBoost = Math.min(1.2D, aggregate.timesDepositedToStorage() * 0.25D);
            if (aggregate.recentCarriedTicksEwma() > 0D && aggregate.recentCarriedTicksEwma() < 1200D) {
                shortCarryBoost = 0.4D;
            }
            if (aggregate.recentCarriedTicksEwma() > 6000D) {
                longCarryPenalty = 0.8D;
            }
        }
        return PutAwayScoreInputs.normal(
                cleanupPrior(index, item.identity().itemId()),
                pressureBoost,
                storageRouteBoost,
                depositHistoryBoost,
                shortCarryBoost,
                longCarryPenalty,
                activeCarriedDurationBoost(state, identity, currentGameTick),
                relevance.total() * 0.55D,
                toolLike(vector, index, item.identity().itemId()) ? 1.1D : 0D,
                item.isCarriedContainer() ? 0.7D : 0D);
    }

    private static boolean hasDesiredExcess(SlotWorkspaceViewModel.AtlasItem item) {
        return item != null
                && item.carried()
                && item.desiredCount() > 0
                && item.totalCount() > item.desiredCount();
    }

    private static List<String> usefulReasons(
            SlotWorkspaceViewModel.AtlasItem item,
            UsefulScoreInputs inputs,
            RelevanceBreakdown relevance
    ) {
        ArrayList<String> reasons = new ArrayList<>();
        reasons.add("score " + format(inputs.score()) + " / threshold " + format(USEFUL_THRESHOLD));
        reasons.add(candidateLine(item));
        reasons.add(contextLine(relevance));
        addMatchLines(reasons, relevance);
        reasons.add("score terms: relevance " + format(inputs.relevance())
                + ", carried +" + format(inputs.carriedBoost())
                + ", target +" + format(inputs.targetBoost())
                + ", deposit -" + format(inputs.depositPenalty())
                + ", desired -" + format(inputs.desiredPenalty())
                + ", missing-source -" + format(inputs.missingSourcePenalty()));
        if (inputs.carriedContainerExcluded()) {
            reasons.add("excluded: carried storage container");
        }
        if (inputs.depositPenalty() > 0D) {
            reasons.add("- recently deposited " + format(inputs.depositPenalty()));
        }
        if (inputs.carriedBoost() > 0D) {
            reasons.add("+ carried and context-relevant");
        }
        if (item.wantedCount() > 0) {
            reasons.add("+ wanted target active");
        }
        if (item.kitNeeded()) {
            reasons.add("+ active kit needs this");
        }
        if (inputs.desiredPenalty() > 0D) {
            reasons.add("- desired count is a carry reservation");
        }
        if (inputs.missingSourcePenalty() > 0D) {
            reasons.add("- no visible carried/storage source");
        }
        return List.copyOf(reasons);
    }

    private static List<String> putAwayReasons(
            SlotWorkspaceViewModel.AtlasItem item,
            PutAwayScoreInputs inputs,
            RelevanceBreakdown relevance,
            ContextualSuggestionState state
    ) {
        ArrayList<String> reasons = new ArrayList<>();
        reasons.add("score " + format(inputs.score()) + " / threshold " + format(PUT_AWAY_THRESHOLD));
        reasons.add(candidateLine(item));
        if (hasDesiredExcess(item)) {
            reasons.add("+ desired excess " + item.totalCount() + "/" + item.desiredCount());
            reasons.add("score terms: base " + format(inputs.base())
                    + ", excess-count +" + format(inputs.desiredExcessCountBoost())
                    + ", pressure +" + format(inputs.pressureBoost())
                    + ", storage-route +" + format(inputs.storageRouteBoost()));
            if (inputs.storageRouteBoost() > 0D) {
                reasons.add("+ storage route visible");
            }
            return List.copyOf(reasons);
        }
        ItemIdentity identity = item.identity().toIdentity();
        ContextualItemAggregate aggregate = identity == null ? null : state.itemAggregates().get(identity);
        reasons.add(contextLine(relevance));
        addMatchLines(reasons, relevance);
        reasons.add("score terms: cleanup " + format(inputs.cleanupPrior())
                + ", pressure +" + format(inputs.pressureBoost())
                + ", storage-route +" + format(inputs.storageRouteBoost())
                + ", deposits +" + format(inputs.depositHistoryBoost())
                + ", short-carry +" + format(inputs.shortCarryBoost())
                + ", active-carry +" + format(inputs.activeCarryBoost())
                + ", long-carry -" + format(inputs.longCarryPenalty())
                + ", context -" + format(inputs.contextPenalty())
                + ", tool -" + format(inputs.toolPenalty())
                + ", container -" + format(inputs.containerPenalty()));
        reasons.add(historyLine(aggregate));
        reasons.add("+ carry-frequency prior " + format(inputs.cleanupPrior()));
        reasons.add("+ inventory pressure " + format(inputs.pressureBoost()));
        if (inputs.storageRouteBoost() > 0D) {
            reasons.add("+ storage route visible");
        }
        if (aggregate != null && aggregate.timesDepositedToStorage() > 0) {
            reasons.add("+ deposited before x" + aggregate.timesDepositedToStorage());
        }
        if (inputs.shortCarryBoost() > 0D) {
            reasons.add("+ historically short carry");
        }
        if (inputs.activeCarryBoost() > 0D) {
            reasons.add("+ carried duration " + format(inputs.activeCarryBoost()));
        }
        if (inputs.longCarryPenalty() > 0D) {
            reasons.add("- historically long carry");
        }
        if (inputs.contextPenalty() > 0D) {
            reasons.add("- current-context relevance " + format(relevance.total()));
        }
        if (inputs.toolPenalty() > 0D) {
            reasons.add("- tool/equipment-like item");
        }
        if (inputs.containerPenalty() > 0D) {
            reasons.add("- carried storage container");
        }
        return List.copyOf(reasons);
    }

    private static String candidateLine(SlotWorkspaceViewModel.AtlasItem item) {
        String source = item.largestCarriedSourceId() == null || item.largestCarriedSourceId().isBlank()
                ? "none"
                : item.largestCarriedSourceId();
        String desired = Integer.toString(item.desiredCount());
        if (item.desiredCountFromKit()) {
            desired += " kit";
        }
        return "candidate: carried=" + item.carried()
                + ", source=" + source
                + ", proximate=" + chestCount(item.presence())
                + ", elsewhere=" + chestCount(item.elsewhere())
                + ", desired=" + desired
                + ", wanted=" + item.wantedCount()
                + ", kitNeeded=" + item.kitNeeded()
                + ", container=" + item.isCarriedContainer();
    }

    private static String contextLine(RelevanceBreakdown relevance) {
        double unclampedBase = relevance.activeRaw() + relevance.passiveApplied();
        String capped = unclampedBase > 4D
                ? " (base capped from " + format(unclampedBase) + ")"
                : "";
        return "context relevance " + format(relevance.total())
                + " = active " + format(relevance.activeRaw())
                + " + passive " + format(relevance.passiveApplied())
                + " (raw " + format(relevance.passiveRaw())
                + ", cap " + format(PASSIVE_CARRIED_RELEVANCE_CAP)
                + ") + exact-context " + format(relevance.exactContextHint())
                + capped;
    }

    private static void addMatchLines(ArrayList<String> reasons, RelevanceBreakdown relevance) {
        if (!relevance.activeMatches().isEmpty()) {
            reasons.add("active matches: " + contributionList(relevance.activeMatches()));
        }
        if (!relevance.passiveMatches().isEmpty()) {
            reasons.add("passive matches: " + contributionList(relevance.passiveMatches()));
        }
    }

    private static String historyLine(ContextualItemAggregate aggregate) {
        if (aggregate == null) {
            return "history: no aggregate";
        }
        return "history: observed=" + aggregate.timesObservedCarried()
                + ", acquired=" + aggregate.timesAcquired()
                + ", taken=" + aggregate.timesTakenFromStorage()
                + ", deposited=" + aggregate.timesDepositedToStorage()
                + ", crafted=" + aggregate.timesCraftedOrProduced()
                + ", carriedTicks=" + aggregate.totalCarriedTicks()
                + ", carriedEwma=" + format(aggregate.recentCarriedTicksEwma());
    }

    private static String contributionList(List<TokenContribution> contributions) {
        return contributions.stream()
                .map(contribution -> contribution.key() + "=" + format(contribution.score()))
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }

    private static int chestCount(List<SlotWorkspaceViewModel.ChestPresenceEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (SlotWorkspaceViewModel.ChestPresenceEntry entry : entries) {
            if (entry != null && entry.count() > 0) {
                count += entry.count();
            }
        }
        return count;
    }

    private static double activeCarriedDurationBoost(
            ContextualSuggestionState state,
            ItemIdentity identity,
            long currentGameTick
    ) {
        if (state == null || identity == null || currentGameTick <= 0L) {
            return 0D;
        }
        var observation = state.activeCarried().get(identity);
        if (observation == null || observation.firstSeenTick() <= 0L) {
            return 0D;
        }
        long elapsed = Math.max(0L, currentGameTick - observation.firstSeenTick());
        if (elapsed < 1200L) {
            return 0D;
        }
        return Math.min(0.85D, (elapsed - 1200L) / 4800D * 0.85D);
    }

    private static boolean protectedSource(String sourceId) {
        if (sourceId == null || sourceId.isBlank()) {
            return false;
        }
        return BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0.equals(sourceId)
                || BuiltinInventoryIds.PLAYER_OFFHAND.equals(sourceId)
                || BuiltinInventoryIds.PLAYER_ARMOR.equals(sourceId);
    }

    private static double cleanupPrior(FacetIndex index, String itemId) {
        String frequency = index.carryFrequency(itemId).orElse("");
        return switch (frequency) {
            case "display_only" -> 2.2D;
            case "rare" -> 1.9D;
            case "occasional" -> 1.45D;
            case "frequent" -> 0.55D;
            case "everyday" -> -0.75D;
            default -> 0.75D;
        };
    }

    private static RelevanceBreakdown relevance(
            SlotWorkspaceViewModel.AtlasItem item,
            Map<String, Double> vector,
            ScoringContext scoringContext,
            ItemIdentity excludedCarriedIdentity,
            ContextualContextAggregate activeContext,
            boolean includeExactActiveContextHint
    ) {
        RelevanceBreakdown score = scoringContext.contextBreakdown(vector, excludedCarriedIdentity);
        if (includeExactActiveContextHint && activeContext != null) {
            return score.withExactContextHint(activeContext.itemHints().getOrDefault(item.identity().itemId(), 0D) * 0.55D);
        }
        return score;
    }

    private static long latestSequence(ContextualSuggestionState state) {
        long latest = 0L;
        for (ContextualSignalRecord record : state.recentSignals()) {
            if (record != null && record.envelope() != null) {
                latest = Math.max(latest, record.envelope().globalSequence());
            }
        }
        return latest;
    }

    private static double signalWeight(ContextualSignalKind kind) {
        return switch (kind) {
            case ITEM_TAKEN_FROM_STORAGE, ITEM_CRAFTED_OR_PRODUCED -> 1.35D;
            case ITEM_PLACED, ITEM_CONSUMED, ITEM_DAMAGED -> 1.25D;
            case ITEM_USED -> 1.05D;
            case ITEM_ACQUIRED -> 1.05D;
            case CARRIED_SET_CHANGED -> 0.65D;
            case ITEM_DEPOSITED_TO_STORAGE -> 0.35D;
            case STATION_OPENED, STATION_CONTENTS_CHANGED, GOAL_CONTEXT_OBSERVED, RECIPE_CONTEXT_OBSERVED -> 0.75D;
        };
    }

    private static double decay(long age) {
        return 1D / (1D + Math.max(0L, age) / 24D);
    }

    private static Map<String, Double> facetVector(FacetIndex index, String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return Map.of();
        }
        LinkedHashMap<String, Double> vector = new LinkedHashMap<>();
        add(vector, "item:" + itemId, 0.9D);
        index.roleAlternatives(itemId).forEach(value -> add(vector, "role:" + value, 0.7D));
        index.materialFamily(itemId).ifPresent(value -> add(vector, "material:" + value, 0.45D));
        index.workflows(itemId).forEach(value -> add(vector, "workflow:" + value, 1.1D));
        index.workflowRoles(itemId).forEach(value -> add(vector, "workflow_role:" + value, 0.95D));
        index.usedAt(itemId).forEach(value -> add(vector, "used_at:" + value, 1.15D));
        index.processingIn(itemId).forEach(value -> add(vector, "processing_in:" + value, 0.65D));
        index.subsystems(itemId).forEach(value -> add(vector, "subsystem:" + value, 0.35D));
        index.activities(itemId).forEach(value -> add(vector, "activity:" + value, 0.45D));
        index.primaryUses(itemId).forEach(value -> addSemanticText(vector, value, 0.85D));
        index.organizationGroups(itemId).forEach(value -> add(vector, "org:" + value, 0.2D));
        index.flavor(itemId).ifPresent(value -> add(vector, "flavor:" + value, 0.25D));
        index.form(itemId).ifPresent(value -> add(vector, "form:" + value, 0.25D));
        index.carryFrequency(itemId).ifPresent(value -> add(vector, "carry:" + value, 0.15D));
        addItemIdText(vector, itemId, 0.38D);
        if (index.isFuel(itemId)) {
            addCombustionFeatures(vector, 0.95D);
        }
        return vector.isEmpty() ? Map.of() : Map.copyOf(vector);
    }

    private static Map<String, Double> contextEventVector(ContextualSignalEvent event) {
        if (event == null) {
            return Map.of();
        }
        LinkedHashMap<String, Double> vector = new LinkedHashMap<>();
        addSemanticText(vector, event.contextKey(), 0.9D);
        addSemanticText(vector, event.contextLabel(), 0.75D);
        addSemanticText(vector, event.sourceKey(), 0.45D);
        event.metadata().forEach((key, value) -> {
            addSemanticText(vector, key, 0.25D);
            addSemanticText(vector, value, 0.45D);
        });
        return vector.isEmpty() ? Map.of() : Map.copyOf(vector);
    }

    private static void addItemIdText(Map<String, Double> vector, String itemId, double weight) {
        if (itemId == null || itemId.isBlank()) {
            return;
        }
        int namespaceIndex = itemId.indexOf(':');
        String path = namespaceIndex >= 0 ? itemId.substring(namespaceIndex + 1) : itemId;
        addSemanticText(vector, path, weight);
    }

    private static void addSemanticText(Map<String, Double> vector, String text, double weight) {
        if (text == null || text.isBlank() || weight <= 0D) {
            return;
        }
        String normalized = text
                .replaceAll("([a-z])([A-Z])", "$1_$2")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_");
        for (String token : normalized.split("_+")) {
            if (token.length() < 3
                    || LOW_INFORMATION_TEXT_TOKENS.contains(token)
                    || token.chars().allMatch(Character::isDigit)) {
                continue;
            }
            add(vector, "text:" + token, weight);
            addTokenBridges(vector, token, weight);
        }
    }

    private static void addTokenBridges(Map<String, Double> vector, String token, double weight) {
        switch (token) {
            case "campfire", "firepit", "fire", "fires", "igniting", "ignite", "ignition", "kindling" ->
                    addCombustionFeatures(vector, weight);
            case "fuel", "fuels", "charcoal", "coal", "log", "logs", "wood" ->
                    addCombustionFeatures(vector, weight * 0.85D);
            case "cook", "cooking", "cooked", "pot", "pots", "meal", "food", "grain", "grains", "pumpkin" -> {
                add(vector, "activity:cooking", weight * 0.65D);
                add(vector, "workflow:cooking", weight * 0.55D);
                add(vector, "workflow:campfire_cooking", weight * 0.45D);
            }
            default -> {
            }
        }
    }

    private static void addCombustionFeatures(Map<String, Double> vector, double weight) {
        add(vector, "text:fire", weight);
        add(vector, "text:fuel", weight * 0.65D);
        add(vector, "activity:cooking", weight * 0.35D);
        add(vector, "workflow:campfire_cooking", weight * 0.45D);
        add(vector, "used_at:campfire", weight * 0.45D);
    }

    private static Map<String, Double> stationCooccurrenceVector(Map<String, Double> hints) {
        if (hints == null || hints.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, Double> vector = new LinkedHashMap<>();
        String prefix = ContextualSuggestionState.STATION_COOCCURRENCE_HINT_PREFIX;
        hints.forEach((key, value) -> {
            if (key == null || !key.startsWith(prefix) || value == null || value <= 0D || !Double.isFinite(value)) {
                return;
            }
            String itemId = key.substring(prefix.length()).trim();
            if (!itemId.isBlank()) {
                add(vector, "item:" + itemId, value);
            }
        });
        return vector.isEmpty() ? Map.of() : Map.copyOf(vector);
    }

    private static boolean toolLike(Map<String, Double> vector, FacetIndex index, String itemId) {
        if (vector.keySet().stream().anyMatch(key -> key.contains("tool")
                || key.contains("weapon")
                || key.contains("armor")
                || key.contains("equipment")
                || key.contains("protection"))) {
            return true;
        }
        String role = index.role(itemId).orElse("").toLowerCase(Locale.ROOT);
        return role.contains("tool") || role.contains("weapon") || role.contains("armor");
    }

    private static void addVector(Map<String, Double> target, Map<String, Double> source, double multiplier) {
        if (source == null || source.isEmpty() || multiplier <= 0D) {
            return;
        }
        source.forEach((key, value) -> add(target, key, value * multiplier));
    }

    private static void add(Map<String, Double> target, String key, double value) {
        if (target == null || key == null || key.isBlank() || value <= 0D || !Double.isFinite(value)) {
            return;
        }
        target.merge(key.trim(), value, Double::sum);
    }

    private static double rawDot(Map<String, Double> left, Map<String, Double> right) {
        if (left == null || right == null || left.isEmpty() || right.isEmpty()) {
            return 0D;
        }
        double score = 0D;
        for (Map.Entry<String, Double> entry : left.entrySet()) {
            score += entry.getValue() * right.getOrDefault(entry.getKey(), 0D);
        }
        return score;
    }

    private static List<TokenContribution> topContributions(Map<String, Double> left, Map<String, Double> right) {
        if (left == null || right == null || left.isEmpty() || right.isEmpty()) {
            return List.of();
        }
        ArrayList<TokenContribution> contributions = new ArrayList<>();
        for (Map.Entry<String, Double> entry : left.entrySet()) {
            Double rightWeight = right.get(entry.getKey());
            if (entry.getValue() == null || rightWeight == null) {
                continue;
            }
            double contribution = entry.getValue() * rightWeight;
            if (contribution > 0D && Double.isFinite(contribution)) {
                contributions.add(new TokenContribution(entry.getKey(), contribution));
            }
        }
        return contributions.stream()
                .sorted(Comparator
                        .comparingDouble(TokenContribution::score).reversed()
                        .thenComparing(TokenContribution::key))
                .limit(4)
                .toList();
    }

    private static double clampDot(double score) {
        if (!Double.isFinite(score) || score <= 0D) {
            return 0D;
        }
        return Math.min(score, 4D);
    }

    private static List<ScoredItem> sorted(List<ScoredItem> scored) {
        return scored.stream()
                .sorted(Comparator
                        .comparingDouble(ScoredItem::score).reversed()
                        .thenComparing(item -> item.item().identity().itemId()))
                .limit(LANE_LIMIT)
                .toList();
    }

    private static List<SlotWorkspaceViewModel.AtlasItem> items(List<ScoredItem> scored) {
        return scored.stream()
                .map(ScoredItem::item)
                .toList();
    }

    private static List<SlotWorkspaceViewModel.ContextualSuggestionDebugInfo> debugInfo(List<ScoredItem> scored) {
        return scored.stream()
                .map(item -> new SlotWorkspaceViewModel.ContextualSuggestionDebugInfo(
                        item.item().identity(),
                        item.score(),
                        item.relevance().total(),
                        item.reasons()))
                .toList();
    }

    private static String format(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private record UsefulScoreInputs(
            boolean carriedContainerExcluded,
            double relevance,
            double depositPenalty,
            double carriedBoost,
            double targetBoost,
            double desiredPenalty,
            double missingSourcePenalty
    ) {
        private double score() {
            if (carriedContainerExcluded) {
                return Double.NEGATIVE_INFINITY;
            }
            return relevance
                    - depositPenalty
                    + carriedBoost
                    + targetBoost
                    - desiredPenalty
                    - missingSourcePenalty;
        }
    }

    private record PutAwayScoreInputs(
            boolean eligible,
            boolean desiredExcess,
            double base,
            double desiredExcessCountBoost,
            double cleanupPrior,
            double pressureBoost,
            double storageRouteBoost,
            double depositHistoryBoost,
            double shortCarryBoost,
            double longCarryPenalty,
            double activeCarryBoost,
            double contextPenalty,
            double toolPenalty,
            double containerPenalty
    ) {
        private static PutAwayScoreInputs ineligible() {
            return new PutAwayScoreInputs(
                    false, false, 0D, 0D, 0D, 0D, 0D, 0D, 0D, 0D, 0D, 0D, 0D, 0D);
        }

        private static PutAwayScoreInputs desiredExcess(
                double base,
                double desiredExcessCountBoost,
                double pressureBoost,
                double storageRouteBoost
        ) {
            return new PutAwayScoreInputs(
                    true,
                    true,
                    base,
                    desiredExcessCountBoost,
                    0D,
                    pressureBoost,
                    storageRouteBoost,
                    0D,
                    0D,
                    0D,
                    0D,
                    0D,
                    0D,
                    0D);
        }

        private static PutAwayScoreInputs normal(
                double cleanupPrior,
                double pressureBoost,
                double storageRouteBoost,
                double depositHistoryBoost,
                double shortCarryBoost,
                double longCarryPenalty,
                double activeCarryBoost,
                double contextPenalty,
                double toolPenalty,
                double containerPenalty
        ) {
            return new PutAwayScoreInputs(
                    true,
                    false,
                    0D,
                    0D,
                    cleanupPrior,
                    pressureBoost,
                    storageRouteBoost,
                    depositHistoryBoost,
                    shortCarryBoost,
                    longCarryPenalty,
                    activeCarryBoost,
                    contextPenalty,
                    toolPenalty,
                    containerPenalty);
        }

        private double score() {
            if (!eligible) {
                return Double.NEGATIVE_INFINITY;
            }
            if (desiredExcess) {
                return base + desiredExcessCountBoost + pressureBoost + storageRouteBoost;
            }
            return cleanupPrior
                    + pressureBoost
                    + storageRouteBoost
                    + depositHistoryBoost
                    + shortCarryBoost
                    - longCarryPenalty
                    + activeCarryBoost
                    - contextPenalty
                    - toolPenalty
                    - containerPenalty;
        }
    }

    private record RelevanceBreakdown(
            double activeRaw,
            double passiveRaw,
            double passiveApplied,
            double exactContextHint,
            double total,
            List<TokenContribution> activeMatches,
            List<TokenContribution> passiveMatches
    ) {
        private RelevanceBreakdown {
            activeRaw = Double.isFinite(activeRaw) ? activeRaw : 0D;
            passiveRaw = Double.isFinite(passiveRaw) ? passiveRaw : 0D;
            passiveApplied = Double.isFinite(passiveApplied) ? passiveApplied : 0D;
            exactContextHint = Double.isFinite(exactContextHint) ? exactContextHint : 0D;
            total = Double.isFinite(total) ? total : 0D;
            activeMatches = activeMatches == null ? List.of() : List.copyOf(activeMatches);
            passiveMatches = passiveMatches == null ? List.of() : List.copyOf(passiveMatches);
        }

        private RelevanceBreakdown withExactContextHint(double exactHint) {
            double resolved = Double.isFinite(exactHint) ? Math.max(0D, exactHint) : 0D;
            return new RelevanceBreakdown(
                    activeRaw,
                    passiveRaw,
                    passiveApplied,
                    resolved,
                    total + resolved,
                    activeMatches,
                    passiveMatches);
        }
    }

    private record TokenContribution(String key, double score) {
        private TokenContribution {
            key = key == null ? "" : key.trim();
            score = Double.isFinite(score) ? Math.max(0D, score) : 0D;
        }
    }

    private static final class ScoringContext {
        private final FacetIndex index;
        private final Map<String, Map<String, Double>> facetVectorCache = new LinkedHashMap<>();
        private Map<String, Double> activeContextVector = Map.of();
        private Map<String, Double> passiveCarriedContextVector = Map.of();
        private Map<ItemIdentity, Map<String, Double>> passiveIdentityExclusions = Map.of();
        private Map<String, Map<String, Double>> passiveItemIdExclusions = Map.of();
        private Map<ItemIdentity, Double> depositPenalties = Map.of();

        private ScoringContext(FacetIndex index) {
            this.index = index == null ? FacetIndex.empty() : index;
        }

        private static ScoringContext create(ContextualSuggestionState state, FacetIndex index) {
            ScoringContext context = new ScoringContext(index);
            context.capture(state);
            return context;
        }

        private Map<String, Double> facetVector(String itemId) {
            String key = itemId == null ? "" : itemId;
            return facetVectorCache.computeIfAbsent(key, ignored -> ContextualSuggestionScorer.facetVector(index, key));
        }

        private RelevanceBreakdown contextBreakdown(Map<String, Double> vector, ItemIdentity excludedCarriedIdentity) {
            double activeScore = rawDot(vector, activeContextVector);
            double passiveScore = rawDot(vector, passiveCarriedContextVector);
            if (excludedCarriedIdentity != null) {
                passiveScore -= rawDot(vector, passiveIdentityExclusions.get(excludedCarriedIdentity));
                passiveScore -= rawDot(vector, passiveItemIdExclusions.get(excludedCarriedIdentity.itemId()));
            }
            double passiveApplied = Math.min(PASSIVE_CARRIED_RELEVANCE_CAP, Math.max(0D, passiveScore));
            return new RelevanceBreakdown(
                    activeScore,
                    passiveScore,
                    passiveApplied,
                    0D,
                    clampDot(activeScore + passiveApplied),
                    topContributions(vector, activeContextVector),
                    topContributions(vector, passiveContributionVector(excludedCarriedIdentity)));
        }

        private double contextDot(Map<String, Double> vector, ItemIdentity excludedCarriedIdentity) {
            return contextBreakdown(vector, excludedCarriedIdentity).total();
        }

        private double depositPenalty(ItemIdentity identity) {
            return identity == null ? 0D : depositPenalties.getOrDefault(identity, 0D);
        }

        private void capture(ContextualSuggestionState state) {
            if (state == null) {
                return;
            }
            LinkedHashMap<String, Double> activeVector = new LinkedHashMap<>();
            LinkedHashMap<String, Double> passiveCarriedVector = new LinkedHashMap<>();
            LinkedHashMap<ItemIdentity, LinkedHashMap<String, Double>> passiveIdentityExclusionBuilder = new LinkedHashMap<>();
            LinkedHashMap<String, LinkedHashMap<String, Double>> passiveItemIdExclusionBuilder = new LinkedHashMap<>();
            LinkedHashMap<ItemIdentity, Double> depositPenaltyBuilder = new LinkedHashMap<>();

            long latest = latestSequence(state);
            for (ContextualSignalRecord record : state.recentSignals()) {
                if (record == null || record.event() == null) {
                    continue;
                }
                double weight = signalWeight(record.event().kind()) * decay(latest - record.envelope().globalSequence());
                if (record.event().identity() != null) {
                    ItemIdentity identity = record.event().identity();
                    Map<String, Double> contribution = facetVector(identity.itemId());
                    if (record.event().kind() == ContextualSignalKind.CARRIED_SET_CHANGED) {
                        if ("start".equals(record.event().metadataValue("phase"))) {
                            depositPenaltyBuilder.remove(identity);
                        }
                        addVector(passiveCarriedVector, contribution, weight);
                        addExclusion(passiveIdentityExclusionBuilder, identity, contribution, weight);
                        addExclusion(passiveItemIdExclusionBuilder, identity.itemId(), contribution, weight);
                    } else if (record.event().kind() == ContextualSignalKind.ITEM_DEPOSITED_TO_STORAGE) {
                        depositPenaltyBuilder.put(identity, Math.min(1.4D, weight * 1.2D));
                    } else {
                        depositPenaltyBuilder.remove(identity);
                        contribution = activeSignalVector(identity.itemId());
                        addVector(activeVector, contribution, weight);
                        ContextualItemAggregate aggregate = state.itemAggregates().get(identity);
                        if (aggregate != null) {
                            addVector(activeVector, stationCooccurrenceVector(aggregate.cooccurrenceHints()), weight * 0.85D);
                        }
                        if (!record.event().contextKey().isBlank()) {
                            addVector(activeVector, contextEventVector(record.event()), weight * 0.35D);
                        }
                    }
                } else {
                    addVector(activeVector, contextEventVector(record.event()), weight);
                }
            }
            state.activeCarried().forEach((identity, observation) -> {
                if (identity != null) {
                    Map<String, Double> contribution = facetVector(identity.itemId());
                    addVector(passiveCarriedVector, contribution, 0.32D);
                    addExclusion(passiveIdentityExclusionBuilder, identity, contribution, 0.32D);
                    addExclusion(passiveItemIdExclusionBuilder, identity.itemId(), contribution, 0.32D);
                }
            });
            ContextualContextAggregate activeContext = state.contextAggregates().get(state.activeContextKey());
            if (activeContext != null) {
                activeContext.itemHints().forEach((itemId, weight) -> {
                    if (itemId != null) {
                        Map<String, Double> contribution = facetVector(itemId);
                        double contributionWeight = Math.min(0.75D, weight * 0.18D);
                        addVector(passiveCarriedVector, contribution, contributionWeight);
                        addExclusion(passiveItemIdExclusionBuilder, itemId, contribution, contributionWeight);
                    }
                });
            }
            activeContextVector = freeze(activeVector);
            passiveCarriedContextVector = freeze(passiveCarriedVector);
            passiveIdentityExclusions = freezeNested(passiveIdentityExclusionBuilder);
            passiveItemIdExclusions = freezeNested(passiveItemIdExclusionBuilder);
            depositPenalties = freezeIdentityDoubles(depositPenaltyBuilder);
        }

        private static <K> void addExclusion(
                Map<K, LinkedHashMap<String, Double>> exclusions,
                K key,
                Map<String, Double> vector,
                double weight
        ) {
            if (key == null || vector == null || vector.isEmpty() || weight <= 0D) {
                return;
            }
            LinkedHashMap<String, Double> exclusion = exclusions.computeIfAbsent(key, ignored -> new LinkedHashMap<>());
            addVector(exclusion, vector, weight);
        }

        private static Map<String, Double> freeze(Map<String, Double> source) {
            if (source == null || source.isEmpty()) {
                return Map.of();
            }
            LinkedHashMap<String, Double> copy = new LinkedHashMap<>();
            source.forEach((key, value) -> {
                if (key != null && !key.isBlank() && value != null && value > 0D && Double.isFinite(value)) {
                    copy.put(key, value);
                }
            });
            return copy.isEmpty() ? Map.of() : Map.copyOf(copy);
        }

        private static <K> Map<K, Map<String, Double>> freezeNested(
                Map<K, LinkedHashMap<String, Double>> source
        ) {
            if (source == null || source.isEmpty()) {
                return Map.of();
            }
            LinkedHashMap<K, Map<String, Double>> copy = new LinkedHashMap<>();
            source.forEach((key, value) -> {
                Map<String, Double> frozen = freeze(value);
                if (key != null && !frozen.isEmpty()) {
                    copy.put(key, frozen);
                }
            });
            return copy.isEmpty() ? Map.of() : Map.copyOf(copy);
        }

        private Map<String, Double> passiveContributionVector(ItemIdentity excludedCarriedIdentity) {
            if (excludedCarriedIdentity == null) {
                return passiveCarriedContextVector;
            }
            LinkedHashMap<String, Double> adjusted = new LinkedHashMap<>(passiveCarriedContextVector);
            subtractVector(adjusted, passiveIdentityExclusions.get(excludedCarriedIdentity));
            subtractVector(adjusted, passiveItemIdExclusions.get(excludedCarriedIdentity.itemId()));
            return freeze(adjusted);
        }

        private static void subtractVector(Map<String, Double> target, Map<String, Double> source) {
            if (target == null || source == null || source.isEmpty()) {
                return;
            }
            source.forEach((key, value) -> {
                if (key != null && !key.isBlank() && value != null && value > 0D && Double.isFinite(value)) {
                    target.merge(key.trim(), -value, Double::sum);
                }
            });
        }

        private Map<String, Double> activeSignalVector(String itemId) {
            Map<String, Double> vector = facetVector(itemId);
            if (!toolLike(vector, index, itemId)) {
                return vector;
            }
            LinkedHashMap<String, Double> filtered = new LinkedHashMap<>();
            vector.forEach((key, value) -> {
                if (isGenericToolKey(key)) {
                    return;
                }
                add(filtered, key, value);
            });
            return filtered.isEmpty() ? Map.of() : Map.copyOf(filtered);
        }

        private static boolean isGenericToolKey(String key) {
            return "role:tool".equals(key)
                    || "role:weapon".equals(key)
                    || "role:armor".equals(key)
                    || "role:equipment".equals(key)
                    || "workflow_role:tool".equals(key)
                    || "workflow_role:weapon".equals(key)
                    || "workflow_role:armor".equals(key)
                    || "workflow_role:equipment".equals(key);
        }

        private static Map<ItemIdentity, Double> freezeIdentityDoubles(Map<ItemIdentity, Double> source) {
            if (source == null || source.isEmpty()) {
                return Map.of();
            }
            LinkedHashMap<ItemIdentity, Double> copy = new LinkedHashMap<>();
            source.forEach((key, value) -> {
                if (key != null && value != null && value > 0D && Double.isFinite(value)) {
                    copy.put(key, value);
                }
            });
            return copy.isEmpty() ? Map.of() : Map.copyOf(copy);
        }
    }

    private static Set<ItemIdentity> goalIdentities(List<GoalPlanState> goals) {
        if (goals == null || goals.isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<ItemIdentity> identities = new LinkedHashSet<>();
        for (GoalPlanState goal : goals) {
            if (goal == null || goal.descriptor() == null) {
                continue;
            }
            addStacks(identities, goal.descriptor().targetOutputs());
            for (GoalRecipeDescriptor recipe : goal.descriptor().recipes()) {
                if (recipe == null) {
                    continue;
                }
                addStacks(identities, recipe.outputs());
                addIngredients(identities, recipe.inputs());
                addIngredients(identities, recipe.catalysts());
            }
        }
        return identities.isEmpty() ? Set.of() : Set.copyOf(identities);
    }

    private static void addIngredients(Set<ItemIdentity> identities, List<GoalIngredientDescriptor> ingredients) {
        if (ingredients == null) {
            return;
        }
        for (GoalIngredientDescriptor ingredient : ingredients) {
            if (ingredient != null) {
                addStacks(identities, ingredient.alternatives());
            }
        }
    }

    private static void addStacks(Set<ItemIdentity> identities, List<GoalStackDescriptor> stacks) {
        if (stacks == null) {
            return;
        }
        for (GoalStackDescriptor stack : stacks) {
            if (stack != null && stack.identity() != null) {
                identities.add(stack.identity());
            }
        }
    }

    private record ScoredItem(
            SlotWorkspaceViewModel.AtlasItem item,
            double score,
            RelevanceBreakdown relevance,
            List<String> reasons
    ) {
        private ScoredItem {
            relevance = relevance == null
                    ? new RelevanceBreakdown(0D, 0D, 0D, 0D, 0D, List.of(), List.of())
                    : relevance;
            reasons = reasons == null ? List.of() : List.copyOf(reasons);
        }
    }
}
