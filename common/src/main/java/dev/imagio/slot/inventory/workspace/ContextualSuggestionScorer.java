package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.classification.FacetIndex;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.goal.GoalIngredientDescriptor;
import dev.imagio.slot.inventory.goal.GoalPlanState;
import dev.imagio.slot.inventory.goal.GoalRecipeDescriptor;
import dev.imagio.slot.inventory.goal.GoalStackDescriptor;
import dev.imagio.slot.workflow.domain.ContextualItemAggregate;
import dev.imagio.slot.workflow.domain.ContextualAssociationHint;
import dev.imagio.slot.workflow.domain.ContextualAssociationSet;
import dev.imagio.slot.workflow.domain.ContextualEventSignature;
import dev.imagio.slot.workflow.domain.ContextualSignalFilters;
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
    private static final int USEFUL_STORAGE_GHOST_RESERVED_SLOTS = 2;
    private static final int PUT_AWAY_DESIRED_EXCESS_LIMIT = 2;
    private static final double USEFUL_THRESHOLD = 0.9D;
    private static final double PUT_AWAY_THRESHOLD = 1.0D;
    private static final double DESIRED_EXCESS_PUT_AWAY_SCORE = 8.0D;
    private static final double FACET_ADVISORY_WEIGHT = 0.55D;
    private static final double FACET_ADVISORY_RELEVANCE_CAP = 1.1D;
    private static final double USEFUL_STRONG_SIGNAL_MIN = 0.9D;
    private static final double USEFUL_CARRIED_ACTIVITY_MIN = 0.6D;
    private static final double USEFUL_STORAGE_ACTIVITY_MIN = 0.75D;
    private static final double USEFUL_STRONG_STRUCTURED_ADVISORY_MIN = 0.9D;
    private static final String USEFUL_NOW_WAITING_TEXT = "Waiting for player actions to suggest items...";
    private static final Set<String> LOW_INFORMATION_TEXT_TOKENS = Set.of(
            "and",
            "are",
            "block",
            "blocks",
            "can",
            "component",
            "components",
            "craft",
            "crafted",
            "crafting",
            "for",
            "from",
            "ingredient",
            "ingredients",
            "into",
            "its",
            "item",
            "items",
            "material",
            "materials",
            "small",
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
        ScoringContext scoringContext = ScoringContext.create(state, index, currentGameTick);

        List<ScoredItem> useful = new ArrayList<>();
        List<ScoredItem> putAway = new ArrayList<>();
        for (SlotWorkspaceViewModel.AtlasItem item : atlasItems) {
            if (item == null || item.identity() == null) {
                continue;
            }
            ItemIdentity identity = item.identity().toIdentity();
            Map<String, Double> vector = scoringContext.facetVector(item.identity().itemId());
            RelevanceBreakdown relevance = scoringContext.contextBreakdown(vector, identity);
            double depositPenalty = scoringContext.depositPenalty(identity);
            double spentPenalty = scoringContext.spentPenalty(identity);
            double passiveSelfPenalty = scoringContext.passiveSelfPenalty(identity);
            UsefulScoreInputs usefulInputs = usefulInputs(
                    item,
                    relevance,
                    depositPenalty,
                    spentPenalty,
                    passiveSelfPenalty);
            double usefulScore = usefulInputs.score();
            if (usefulScore >= USEFUL_THRESHOLD) {
                useful.add(new ScoredItem(
                        item,
                        usefulScore,
                        relevance,
                        usefulReasons(item, usefulInputs, relevance)));
            }
            RelevanceBreakdown cleanupRelevance = scoringContext.contextBreakdown(vector, identity);
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
        List<ScoredItem> usefulSorted = selectUseful(dedupeByIdentity(sortedAll(useful)));
        List<SlotWorkspaceViewModel.AtlasItem> usefulItems = items(usefulSorted);
        lanes.add(new SlotWorkspaceViewModel.ContextualSuggestionLane(
                SlotWorkspaceViewModel.ContextualSuggestionLane.USEFUL_NOW,
                "Useful Now",
                usefulItems,
                usefulItems.isEmpty() ? USEFUL_NOW_WAITING_TEXT : "",
                debugInfo(usefulSorted)));
        List<ScoredItem> putAwaySorted = selectPutAway(dedupeByIdentity(sortedAll(putAway)));
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
            RelevanceBreakdown relevance,
            double depositPenalty,
            double spentPenalty,
            double passiveSelfPenalty
    ) {
        RelevanceBreakdown resolvedRelevance = relevance == null
                ? new RelevanceBreakdown(0D, 0D, 0D, 0D, 0D, 0D, List.of(), List.of())
                : relevance;
        double resolvedDepositPenalty = Math.max(0D, depositPenalty);
        double resolvedSpentPenalty = Math.max(0D, spentPenalty);
        double targetBoost = item.wantedCount() > 0 || item.kitNeeded() ? 1.35D : 0D;
        double desiredPenalty = item.carried() && item.desiredCount() > 0 && !item.desiredCountFromKit() ? 0.35D : 0D;
        boolean sourceAvailable = item.carried()
                || item.proximateCount() > 0
                || !item.presence().isEmpty()
                || item.kitNeeded();
        boolean weakContextExcluded = targetBoost <= 0D && !hasUsefulNowEvidence(item, resolvedRelevance);
        boolean passiveRecentSelfExcluded = targetBoost <= 0D
                && passiveSelfPenalty > 0D
                && resolvedRelevance.historyRaw() <= 0D
                && resolvedRelevance.exactRaw() <= 0D;
        boolean protectedExactUseExcluded = targetBoost <= 0D
                && item.carried()
                && protectedSource(item.largestCarriedSourceId())
                && resolvedRelevance.exactRaw() > 0D
                && resolvedRelevance.historyRaw() <= 0D
                && resolvedRelevance.exactContextHint() <= 0D;
        return new UsefulScoreInputs(
                item.isCarriedContainer(),
                !sourceAvailable,
                weakContextExcluded,
                passiveRecentSelfExcluded,
                protectedExactUseExcluded,
                resolvedRelevance.total(),
                resolvedDepositPenalty,
                resolvedSpentPenalty,
                targetBoost,
                desiredPenalty,
                !sourceAvailable ? 1.25D : 0D,
                Math.max(0D, passiveSelfPenalty));
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
        if (aggregate != null && depositHistoryUsable(aggregate, storageRouteBoost > 0D)) {
            depositHistoryBoost = Math.min(1.2D, aggregate.timesDepositedToStorage() * 0.25D);
        }
        if (storageRouteBoost <= 0D && depositHistoryBoost <= 0D && pressure < 0.85D) {
            return PutAwayScoreInputs.ineligible();
        }
        return PutAwayScoreInputs.normal(
                cleanupPrior(index, item.identity().itemId()),
                pressureBoost,
                storageRouteBoost,
                depositHistoryBoost,
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

    private static boolean depositHistoryUsable(ContextualItemAggregate aggregate, boolean hasStorageRoute) {
        if (aggregate == null || aggregate.timesDepositedToStorage() <= 0) {
            return false;
        }
        if (hasStorageRoute) {
            return true;
        }
        return aggregate.timesDepositedToStorage() >= 2
                && aggregate.lastDepositedSequence() >= aggregate.lastActiveSequence();
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
                + ", target +" + format(inputs.targetBoost())
                + ", deposit -" + format(inputs.depositPenalty())
                + ", spent -" + format(inputs.spentPenalty())
                + ", desired -" + format(inputs.desiredPenalty())
                + ", missing-source -" + format(inputs.missingSourcePenalty())
                + ", recent-self -" + format(inputs.passiveSelfPenalty()));
        if (inputs.carriedContainerExcluded()) {
            reasons.add("excluded: carried storage container");
        }
        if (inputs.weakContextExcluded()) {
            reasons.add("excluded: weak useful-now context");
        }
        if (inputs.passiveRecentSelfExcluded()) {
            reasons.add("excluded: recent pickup/take belongs in Recents");
        }
        if (inputs.protectedExactUseExcluded()) {
            reasons.add("excluded: already visible in quick access/equipment");
        }
        if (inputs.depositPenalty() > 0D) {
            reasons.add("- recently deposited " + format(inputs.depositPenalty()));
        }
        if (inputs.spentPenalty() > 0D) {
            reasons.add("- recently placed/consumed " + format(inputs.spentPenalty()));
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
        double unclampedBase = relevance.historyRaw() + relevance.exactRaw()
                + relevance.advisoryApplied() + relevance.exactContextHint();
        String capped = unclampedBase > 4D
                ? " (base capped from " + format(unclampedBase) + ")"
                : "";
        return "context relevance " + format(relevance.total())
                + " = history " + format(relevance.historyRaw())
                + " + exact " + format(relevance.exactRaw())
                + " + advisory " + format(relevance.advisoryApplied())
                + " (raw " + format(relevance.advisoryRaw())
                + ", cap " + format(FACET_ADVISORY_RELEVANCE_CAP)
                + ") + exact-context " + format(relevance.exactContextHint())
                + capped;
    }

    private static void addMatchLines(ArrayList<String> reasons, RelevanceBreakdown relevance) {
        if (!relevance.historyMatches().isEmpty()) {
            reasons.add("history matches: " + contributionList(relevance.historyMatches()));
        }
        if (!relevance.advisoryMatches().isEmpty()) {
            reasons.add("advisory matches: " + contributionList(relevance.advisoryMatches()));
        }
    }

    private static String historyLine(ContextualItemAggregate aggregate) {
        if (aggregate == null) {
            return "history: no aggregate";
        }
        return "history: acquired=" + aggregate.timesAcquired()
                + ", taken=" + aggregate.timesTakenFromStorage()
                + ", deposited=" + aggregate.timesDepositedToStorage()
                + ", crafted=" + aggregate.timesCraftedOrProduced()
                + ", used=" + aggregate.timesUsed()
                + ", placed=" + aggregate.timesPlaced()
                + ", consumed=" + aggregate.timesConsumed()
                + ", damaged=" + aggregate.timesDamaged();
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
            case ITEM_DEPOSITED_TO_STORAGE -> 0.35D;
            case STATION_OPENED, STATION_CONTENTS_CHANGED, GOAL_CONTEXT_OBSERVED, RECIPE_CONTEXT_OBSERVED -> 0.75D;
        };
    }

    private static double decay(long age) {
        return 1D / (1D + Math.max(0L, age) / 24D);
    }

    private static double tickDecay(long observedTick, long currentGameTick) {
        if (observedTick <= 0L || currentGameTick <= 0L || currentGameTick <= observedTick) {
            return 1D;
        }
        long ageTicks = currentGameTick - observedTick;
        if (ageTicks <= 1200L) {
            return 1D;
        }
        return 1D / (1D + (ageTicks - 1200L) / 2400D);
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
            case "fuel", "fuels", "charcoal", "coal", "log", "logs" ->
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

    private static boolean placeableLike(Map<String, Double> vector) {
        if (vector == null || vector.isEmpty()) {
            return false;
        }
        return vector.containsKey("role:block")
                || vector.containsKey("form:block")
                || vector.containsKey("workflow_role:seed");
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
            if (!usefulAdvisoryKey(entry.getKey())) {
                continue;
            }
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
            if (!usefulAdvisoryKey(entry.getKey())) {
                continue;
            }
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

    private static boolean usefulAdvisoryKey(String key) {
        if (key == null || key.isBlank()) {
            return false;
        }
        if (key.startsWith("workflow:")
                || key.startsWith("workflow_role:")
                || key.startsWith("used_at:")
                || key.startsWith("processing_in:")
                || key.startsWith("subsystem:")
                || key.startsWith("activity:")) {
            return true;
        }
        if (key.startsWith("text:")) {
            String token = key.substring("text:".length()).trim();
            return token.length() >= 3 && !LOW_INFORMATION_TEXT_TOKENS.contains(token);
        }
        return false;
    }

    private static double clampDot(double score) {
        if (!Double.isFinite(score) || score <= 0D) {
            return 0D;
        }
        return Math.min(score, 4D);
    }

    private static List<ScoredItem> dedupeByIdentity(List<ScoredItem> sorted) {
        if (sorted == null || sorted.isEmpty()) {
            return List.of();
        }
        LinkedHashMap<ItemIdentity, ScoredItem> byIdentity = new LinkedHashMap<>();
        for (ScoredItem item : sorted) {
            if (item == null || item.item() == null || item.item().identity() == null) {
                continue;
            }
            ItemIdentity identity = item.item().identity().toIdentity();
            if (identity == null) {
                continue;
            }
            byIdentity.putIfAbsent(identity, item);
        }
        return List.copyOf(byIdentity.values());
    }

    private static List<ScoredItem> sortedAll(List<ScoredItem> scored) {
        return scored.stream()
                .sorted(Comparator
                        .comparingDouble(ScoredItem::score).reversed()
                        .thenComparing(item -> item.item().identity().itemId()))
                .toList();
    }

    private static List<ScoredItem> selectUseful(List<ScoredItem> sorted) {
        if (sorted == null || sorted.isEmpty() || sorted.size() <= LANE_LIMIT) {
            return sorted == null ? List.of() : sorted;
        }
        ArrayList<ScoredItem> selected = new ArrayList<>(sorted.stream().limit(LANE_LIMIT).toList());
        long selectedStorage = selected.stream()
                .filter(item -> storageOnly(item.item()))
                .count();
        long availableStorage = sorted.stream()
                .filter(item -> storageOnly(item.item()))
                .count();
        long targetStorage = Math.min(USEFUL_STORAGE_GHOST_RESERVED_SLOTS, availableStorage);
        if (selectedStorage >= targetStorage) {
            return List.copyOf(selected);
        }
        for (ScoredItem candidate : sorted) {
            if (selectedStorage >= targetStorage || !storageOnly(candidate.item()) || selected.contains(candidate)) {
                continue;
            }
            int replacementIndex = lowestScoringNonStorageIndex(selected);
            if (replacementIndex < 0) {
                break;
            }
            selected.set(replacementIndex, candidate);
            selectedStorage++;
        }
        return sortedAll(selected).stream()
                .limit(LANE_LIMIT)
                .toList();
    }

    private static List<ScoredItem> selectPutAway(List<ScoredItem> sorted) {
        if (sorted == null || sorted.isEmpty()) {
            return List.of();
        }
        ArrayList<ScoredItem> selected = new ArrayList<>(LANE_LIMIT);
        int desiredExcessCount = 0;
        for (ScoredItem candidate : sorted) {
            if (candidate == null || candidate.item() == null) {
                continue;
            }
            boolean desiredExcess = hasDesiredExcess(candidate.item());
            if (desiredExcess) {
                if (desiredExcessCount >= PUT_AWAY_DESIRED_EXCESS_LIMIT) {
                    continue;
                }
                desiredExcessCount++;
            }
            selected.add(candidate);
            if (selected.size() >= LANE_LIMIT) {
                break;
            }
        }
        return List.copyOf(selected);
    }

    private static int lowestScoringNonStorageIndex(List<ScoredItem> selected) {
        int index = -1;
        double lowestScore = Double.POSITIVE_INFINITY;
        for (int i = 0; i < selected.size(); i++) {
            ScoredItem item = selected.get(i);
            if (item == null || storageOnly(item.item())) {
                continue;
            }
            if (item.score() < lowestScore) {
                index = i;
                lowestScore = item.score();
            }
        }
        return index;
    }

    private static boolean storageOnly(SlotWorkspaceViewModel.AtlasItem item) {
        return item != null
                && !item.carried()
                && (item.proximateCount() > 0 || !item.presence().isEmpty());
    }

    private static boolean hasUsefulNowEvidence(SlotWorkspaceViewModel.AtlasItem item, RelevanceBreakdown relevance) {
        if (relevance == null) {
            return false;
        }
        double directActivity = relevance.historyRaw() + relevance.exactRaw();
        double activityThreshold = storageOnly(item) ? USEFUL_STORAGE_ACTIVITY_MIN : USEFUL_CARRIED_ACTIVITY_MIN;
        if (directActivity >= activityThreshold
                || relevance.exactRaw() >= USEFUL_STRONG_SIGNAL_MIN
                || relevance.historyRaw() >= USEFUL_STRONG_SIGNAL_MIN
                || relevance.exactContextHint() >= USEFUL_STRONG_SIGNAL_MIN) {
            return true;
        }
        return hasStrongStructuredAdvisory(relevance);
    }

    private static boolean hasStrongStructuredAdvisory(RelevanceBreakdown relevance) {
        if (relevance == null || relevance.advisoryMatches().isEmpty()) {
            return false;
        }
        for (TokenContribution contribution : relevance.advisoryMatches()) {
            if (contribution.score() < USEFUL_STRONG_STRUCTURED_ADVISORY_MIN) {
                continue;
            }
            String key = contribution.key();
            if (key.startsWith("workflow:")
                    || key.startsWith("workflow_role:")
                    || key.startsWith("used_at:")
                    || key.startsWith("processing_in:")
                    || key.startsWith("subsystem:")) {
                return true;
            }
        }
        return false;
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
            boolean missingSourceExcluded,
            boolean weakContextExcluded,
            boolean passiveRecentSelfExcluded,
            boolean protectedExactUseExcluded,
            double relevance,
            double depositPenalty,
            double spentPenalty,
            double targetBoost,
            double desiredPenalty,
            double missingSourcePenalty,
            double passiveSelfPenalty
    ) {
        private double score() {
            if (carriedContainerExcluded
                    || missingSourceExcluded
                    || weakContextExcluded
                    || passiveRecentSelfExcluded
                    || protectedExactUseExcluded) {
                return Double.NEGATIVE_INFINITY;
            }
            return relevance
                    - depositPenalty
                    - spentPenalty
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
            double contextPenalty,
            double toolPenalty,
            double containerPenalty
    ) {
        private static PutAwayScoreInputs ineligible() {
            return new PutAwayScoreInputs(
                    false, false, 0D, 0D, 0D, 0D, 0D, 0D, 0D, 0D, 0D);
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
                    0D);
        }

        private static PutAwayScoreInputs normal(
                double cleanupPrior,
                double pressureBoost,
                double storageRouteBoost,
                double depositHistoryBoost,
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
                    - contextPenalty
                    - toolPenalty
                    - containerPenalty;
        }
    }

    private record RelevanceBreakdown(
            double historyRaw,
            double exactRaw,
            double advisoryRaw,
            double advisoryApplied,
            double exactContextHint,
            double total,
            List<TokenContribution> historyMatches,
            List<TokenContribution> advisoryMatches
    ) {
        private RelevanceBreakdown {
            historyRaw = Double.isFinite(historyRaw) ? historyRaw : 0D;
            exactRaw = Double.isFinite(exactRaw) ? exactRaw : 0D;
            advisoryRaw = Double.isFinite(advisoryRaw) ? advisoryRaw : 0D;
            advisoryApplied = Double.isFinite(advisoryApplied) ? advisoryApplied : 0D;
            exactContextHint = Double.isFinite(exactContextHint) ? exactContextHint : 0D;
            total = Double.isFinite(total) ? total : 0D;
            historyMatches = historyMatches == null ? List.of() : List.copyOf(historyMatches);
            advisoryMatches = advisoryMatches == null ? List.of() : List.copyOf(advisoryMatches);
        }

        private RelevanceBreakdown withExactContextHint(double exactHint) {
            double resolved = Double.isFinite(exactHint) ? Math.max(0D, exactHint) : 0D;
            return new RelevanceBreakdown(
                    historyRaw,
                    exactRaw,
                    advisoryRaw,
                    advisoryApplied,
                    resolved,
                    total + resolved,
                    historyMatches,
                    advisoryMatches);
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
        private final long currentGameTick;
        private final Map<String, Map<String, Double>> facetVectorCache = new LinkedHashMap<>();
        private Map<String, Double> activeContextVector = Map.of();
        private Map<String, Double> exactItemScores = Map.of();
        private Map<String, Double> historyItemScores = Map.of();
        private Map<String, List<TokenContribution>> historyContributions = Map.of();
        private Map<ItemIdentity, Double> depositPenalties = Map.of();
        private Map<ItemIdentity, Double> spentPenalties = Map.of();
        private Map<ItemIdentity, Double> passiveSelfPenalties = Map.of();

        private ScoringContext(FacetIndex index, long currentGameTick) {
            this.index = index == null ? FacetIndex.empty() : index;
            this.currentGameTick = Math.max(0L, currentGameTick);
        }

        private static ScoringContext create(ContextualSuggestionState state, FacetIndex index, long currentGameTick) {
            ScoringContext context = new ScoringContext(index, currentGameTick);
            context.capture(state);
            return context;
        }

        private Map<String, Double> facetVector(String itemId) {
            String key = itemId == null ? "" : itemId;
            return facetVectorCache.computeIfAbsent(key, ignored -> ContextualSuggestionScorer.facetVector(index, key));
        }

        private RelevanceBreakdown contextBreakdown(Map<String, Double> vector, ItemIdentity identity) {
            String itemId = identity == null ? "" : identity.itemId();
            double historyScore = historyItemScores.getOrDefault(itemId, 0D);
            double exactScore = exactItemScores.getOrDefault(itemId, 0D);
            double advisoryRaw = rawDot(vector, activeContextVector);
            double advisoryApplied = Math.min(
                    FACET_ADVISORY_RELEVANCE_CAP,
                    Math.max(0D, advisoryRaw * FACET_ADVISORY_WEIGHT));
            return new RelevanceBreakdown(
                    historyScore,
                    exactScore,
                    advisoryRaw,
                    advisoryApplied,
                    0D,
                    clampDot(historyScore + exactScore + advisoryApplied),
                    historyContributions.getOrDefault(itemId, List.of()),
                    topContributions(vector, activeContextVector));
        }

        private double depositPenalty(ItemIdentity identity) {
            return identity == null ? 0D : depositPenalties.getOrDefault(identity, 0D);
        }

        private double spentPenalty(ItemIdentity identity) {
            return identity == null ? 0D : spentPenalties.getOrDefault(identity, 0D);
        }

        private double passiveSelfPenalty(ItemIdentity identity) {
            return identity == null ? 0D : passiveSelfPenalties.getOrDefault(identity, 0D);
        }

        private void capture(ContextualSuggestionState state) {
            if (state == null) {
                return;
            }
            LinkedHashMap<String, Double> activeVector = new LinkedHashMap<>();
            LinkedHashMap<String, Double> exactItemBuilder = new LinkedHashMap<>();
            LinkedHashMap<String, Double> historyItemBuilder = new LinkedHashMap<>();
            LinkedHashMap<String, LinkedHashMap<String, Double>> historyContributionBuilder = new LinkedHashMap<>();
            LinkedHashMap<ItemIdentity, Double> depositPenaltyBuilder = new LinkedHashMap<>();
            LinkedHashMap<ItemIdentity, Double> spentPenaltyBuilder = new LinkedHashMap<>();
            LinkedHashMap<ItemIdentity, Double> passiveSelfPenaltyBuilder = new LinkedHashMap<>();

            long latest = latestSequence(state);
            for (ContextualSignalRecord record : state.recentSignals()) {
                if (record == null || record.event() == null) {
                    continue;
                }
                ContextualSignalEvent event = record.event();
                if (ContextualSignalFilters.lowInformationUse(event)
                        || ContextualSignalFilters.passiveOffhandWorldUse(event)) {
                    continue;
                }
                double weight = signalWeight(event.kind())
                        * decay(latest - record.envelope().globalSequence())
                        * tickDecay(event.observedTick(), currentGameTick);
                boolean applyAssociationHistory = false;
                if (event.identity() != null) {
                    ItemIdentity identity = event.identity();
                    if (event.kind() == ContextualSignalKind.ITEM_DEPOSITED_TO_STORAGE) {
                        depositPenaltyBuilder.put(identity, Math.min(1.4D, weight * 1.2D));
                    } else {
                        if (clearsDepositPenalty(event.kind())) {
                            depositPenaltyBuilder.remove(identity);
                        }
                        if (clearsSpentPenalty(event.kind())) {
                            spentPenaltyBuilder.remove(identity);
                        }
                        if (clearsPassiveSelfPenalty(event.kind())) {
                            passiveSelfPenaltyBuilder.remove(identity);
                        }
                        if (event.kind() == ContextualSignalKind.ITEM_ACQUIRED
                                || event.kind() == ContextualSignalKind.ITEM_TAKEN_FROM_STORAGE) {
                            passiveSelfPenaltyBuilder.merge(identity, weight, Math::max);
                        }
                        if (event.kind() == ContextualSignalKind.ITEM_PLACED
                                || event.kind() == ContextualSignalKind.ITEM_CONSUMED) {
                            spentPenaltyBuilder.put(identity, Math.min(1.6D, weight * 1.1D));
                        }
                        Map<String, Double> contribution = activeSignalVector(event);
                        applyAssociationHistory = ContextualEventSignature.trainsAssociations(event);
                        double exactWeight = exactItemWeight(event);
                        if (exactWeight > 0D) {
                            exactItemBuilder.merge(identity.itemId(), weight * exactWeight, Double::sum);
                        }
                        addVector(activeVector, contribution, weight);
                        if (!event.contextKey().isBlank() && !ContextualSignalFilters.targetlessWorldUse(event)) {
                            addVector(activeVector, contextEventVector(event), weight * 0.35D);
                        }
                    }
                } else {
                    addVector(activeVector, contextEventVector(event), weight);
                }

                String signature = ContextualEventSignature.key(event);
                if (applyAssociationHistory && !signature.isBlank()) {
                    ContextualAssociationSet associationSet = state.associationIndex()
                            .nextItemsBySignature()
                            .get(signature);
                    if (associationSet != null) {
                        for (ContextualAssociationHint hint : associationSet.itemHints().values()) {
                            if (hint == null || hint.itemId().isBlank()) {
                                continue;
                            }
                            double score = Math.min(2.25D, hint.score()) * weight * 0.8D;
                            if (score <= 0D || !Double.isFinite(score)) {
                                continue;
                            }
                            historyItemBuilder.merge(hint.itemId(), score, Double::sum);
                            historyContributionBuilder
                                    .computeIfAbsent(hint.itemId(), ignored -> new LinkedHashMap<>())
                                    .merge(signature, score, Double::sum);
                        }
                    }
                }
            }
            activeContextVector = freeze(activeVector);
            exactItemScores = freezeStringDoubles(exactItemBuilder);
            historyItemScores = freezeStringDoubles(historyItemBuilder);
            historyContributions = freezeContributionLists(historyContributionBuilder);
            depositPenalties = freezeIdentityDoubles(depositPenaltyBuilder);
            spentPenalties = freezeIdentityDoubles(spentPenaltyBuilder);
            passiveSelfPenalties = freezeIdentityDoubles(passiveSelfPenaltyBuilder);
        }

        private static boolean clearsDepositPenalty(ContextualSignalKind kind) {
            return switch (kind) {
                case ITEM_ACQUIRED, ITEM_TAKEN_FROM_STORAGE, ITEM_CRAFTED_OR_PRODUCED, ITEM_USED, ITEM_DAMAGED -> true;
                case ITEM_DEPOSITED_TO_STORAGE, ITEM_PLACED, ITEM_CONSUMED, STATION_OPENED,
                        STATION_CONTENTS_CHANGED, GOAL_CONTEXT_OBSERVED, RECIPE_CONTEXT_OBSERVED -> false;
            };
        }

        private static boolean clearsSpentPenalty(ContextualSignalKind kind) {
            return switch (kind) {
                case ITEM_ACQUIRED, ITEM_TAKEN_FROM_STORAGE, ITEM_CRAFTED_OR_PRODUCED, ITEM_USED, ITEM_DAMAGED -> true;
                case ITEM_DEPOSITED_TO_STORAGE, ITEM_PLACED, ITEM_CONSUMED, STATION_OPENED,
                        STATION_CONTENTS_CHANGED, GOAL_CONTEXT_OBSERVED, RECIPE_CONTEXT_OBSERVED -> false;
            };
        }

        private static boolean clearsPassiveSelfPenalty(ContextualSignalKind kind) {
            return switch (kind) {
                case ITEM_CRAFTED_OR_PRODUCED, ITEM_USED, ITEM_DAMAGED -> true;
                case ITEM_ACQUIRED, ITEM_TAKEN_FROM_STORAGE, ITEM_DEPOSITED_TO_STORAGE, ITEM_PLACED, ITEM_CONSUMED,
                        STATION_OPENED, STATION_CONTENTS_CHANGED, GOAL_CONTEXT_OBSERVED, RECIPE_CONTEXT_OBSERVED -> false;
            };
        }

        private double exactItemWeight(ContextualSignalEvent event) {
            if (event == null || event.identity() == null) {
                return 0D;
            }
            String itemId = event.identity().itemId();
            return switch (event.kind()) {
                case ITEM_CRAFTED_OR_PRODUCED -> 1.1D;
                case ITEM_ACQUIRED, ITEM_TAKEN_FROM_STORAGE -> 0D;
                case ITEM_DAMAGED -> {
                    Map<String, Double> vector = facetVector(itemId);
                    yield toolLike(vector, index, itemId) ? 0.9D : 0D;
                }
                case ITEM_USED -> {
                    Map<String, Double> vector = facetVector(itemId);
                    boolean toolUse = toolLike(vector, index, itemId);
                    yield toolUse || (!ContextualSignalFilters.targetlessWorldUse(event) && !placeableLike(vector))
                            ? 1.0D
                            : 0D;
                }
                case ITEM_PLACED, ITEM_CONSUMED, ITEM_DEPOSITED_TO_STORAGE, STATION_OPENED,
                        STATION_CONTENTS_CHANGED, GOAL_CONTEXT_OBSERVED, RECIPE_CONTEXT_OBSERVED -> 0D;
            };
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

        private static Map<String, Double> freezeStringDoubles(Map<String, Double> source) {
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

        private static Map<String, List<TokenContribution>> freezeContributionLists(
                Map<String, LinkedHashMap<String, Double>> source
        ) {
            if (source == null || source.isEmpty()) {
                return Map.of();
            }
            LinkedHashMap<String, List<TokenContribution>> copy = new LinkedHashMap<>();
            source.forEach((itemId, contributions) -> {
                if (itemId == null || itemId.isBlank() || contributions == null || contributions.isEmpty()) {
                    return;
                }
                List<TokenContribution> frozen = contributions.entrySet().stream()
                        .filter(entry -> entry.getKey() != null && !entry.getKey().isBlank())
                        .filter(entry -> entry.getValue() != null && entry.getValue() > 0D && Double.isFinite(entry.getValue()))
                        .map(entry -> new TokenContribution(entry.getKey(), entry.getValue()))
                        .sorted(Comparator
                                .comparingDouble(TokenContribution::score).reversed()
                                .thenComparing(TokenContribution::key))
                        .limit(4)
                        .toList();
                if (!frozen.isEmpty()) {
                    copy.put(itemId, frozen);
                }
            });
            return copy.isEmpty() ? Map.of() : Map.copyOf(copy);
        }

        private Map<String, Double> activeSignalVector(ContextualSignalEvent event) {
            if (event == null || event.identity() == null) {
                return Map.of();
            }
            String itemId = event.identity().itemId();
            Map<String, Double> vector = facetVector(itemId);
            if (ContextualSignalFilters.targetlessWorldUse(event)) {
                return Map.of();
            }
            if (event.kind() == ContextualSignalKind.ITEM_DAMAGED) {
                if (!toolLike(vector, index, itemId)) {
                    return Map.of();
                }
                LinkedHashMap<String, Double> filtered = new LinkedHashMap<>();
                vector.forEach((key, value) -> {
                    if (!usefulToolContextKey(key)) {
                        return;
                    }
                    add(filtered, key, value);
                });
                return filtered.isEmpty() ? Map.of() : Map.copyOf(filtered);
            }
            if (event.kind() == ContextualSignalKind.ITEM_USED && !toolLike(vector, index, itemId)) {
                if (placeableLike(vector)) {
                    return Map.of();
                }
                return withoutExactIdentityKey(vector, itemId);
            }
            if (event.kind() == ContextualSignalKind.ITEM_PLACED
                    || event.kind() == ContextualSignalKind.ITEM_CONSUMED) {
                return withoutExactIdentityKey(vector, itemId);
            }
            if (!toolLike(vector, index, itemId)) {
                return vector;
            }
            LinkedHashMap<String, Double> filtered = new LinkedHashMap<>();
            vector.forEach((key, value) -> {
                if (!usefulToolContextKey(key)) {
                    return;
                }
                add(filtered, key, value);
            });
            return filtered.isEmpty() ? Map.of() : Map.copyOf(filtered);
        }

        private static Map<String, Double> withoutExactIdentityKey(Map<String, Double> vector, String itemId) {
            if (vector == null || vector.isEmpty() || itemId == null || itemId.isBlank()) {
                return vector == null ? Map.of() : vector;
            }
            LinkedHashMap<String, Double> filtered = new LinkedHashMap<>();
            String exactKey = "item:" + itemId;
            vector.forEach((key, value) -> {
                if (exactKey.equals(key)) {
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

        private static boolean usefulToolContextKey(String key) {
            if (key == null || key.isBlank()) {
                return false;
            }
            if (isGenericToolKey(key)) {
                return false;
            }
            return key.startsWith("workflow:")
                    || key.startsWith("workflow_role:")
                    || key.startsWith("used_at:")
                    || key.startsWith("processing_in:")
                    || key.startsWith("subsystem:");
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
                    ? new RelevanceBreakdown(0D, 0D, 0D, 0D, 0D, 0D, List.of(), List.of())
                    : relevance;
            reasons = reasons == null ? List.of() : List.copyOf(reasons);
        }
    }
}
