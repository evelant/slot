package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.ItemComparisonMode;
import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Identity-keyed card reuse for the existing full projection output.
 *
 * <p>The full view-model projection remains the oracle while the card slice is
 * being extracted. This class only reuses a previous card when its complete
 * rendered fingerprint is unchanged, so stale counts, route badges, ghost
 * state, and section placement cannot survive a local dependency change.
 */
final class WorkspaceCardProjector {
    private WorkspaceCardProjector() {
    }

    static Result project(SlotWorkspaceViewModel viewModel, State previous) {
        SlotWorkspaceViewModel resolved = viewModel == null ? SlotWorkspaceViewModel.empty() : viewModel;
        State prior = previous == null ? State.empty() : previous;
        LinkedHashMap<IdentityKey, CardEntry> nextCards = new LinkedHashMap<>();
        StatsCounter stats = new StatsCounter();
        List<SlotWorkspaceViewModel.AtlasItem> atlasItems =
                projectLane(resolved.atlasItems(), CardLane.ATLAS, prior, nextCards, stats);
        List<SlotWorkspaceViewModel.AtlasItem> triageItems =
                projectLane(resolved.triageItems(), CardLane.TRIAGE, prior, nextCards, stats);
        int removed = removedCardCount(prior.cards().keySet(), nextCards.keySet());
        SlotWorkspaceViewModel projected = resolved.withCards(atlasItems, triageItems);
        return new Result(
                projected,
                new State(nextCards),
                new WorkspaceCardProjectionStats(stats.reused(), stats.rebuilt(), removed));
    }

    private static List<SlotWorkspaceViewModel.AtlasItem> projectLane(
            List<SlotWorkspaceViewModel.AtlasItem> cards,
            CardLane lane,
            State previous,
            Map<IdentityKey, CardEntry> nextCards,
            StatsCounter stats
    ) {
        if (cards == null || cards.isEmpty()) {
            return List.of();
        }
        ArrayList<SlotWorkspaceViewModel.AtlasItem> projected = new ArrayList<>(cards.size());
        for (int index = 0; index < cards.size(); index++) {
            SlotWorkspaceViewModel.AtlasItem card = cards.get(index);
            if (card == null) {
                continue;
            }
            IdentityKey identity = IdentityKey.from(card.identity());
            String contentKey = WorkspaceProjectionFingerprint.cardKey(card);
            CardEntry previousCard = previous.cards().get(identity);
            SlotWorkspaceViewModel.AtlasItem nextCard = card;
            if (previousCard != null
                    && previousCard.lane() == lane
                    && previousCard.contentKey().equals(contentKey)) {
                nextCard = previousCard.card();
                stats.reuse();
            } else {
                stats.rebuild();
            }
            projected.add(nextCard);
            nextCards.put(identity, new CardEntry(identity, lane, contentKey, nextCard, index));
        }
        return List.copyOf(projected);
    }

    private static int removedCardCount(Set<IdentityKey> previous, Set<IdentityKey> next) {
        if (previous == null || previous.isEmpty()) {
            return 0;
        }
        LinkedHashSet<IdentityKey> removed = new LinkedHashSet<>(previous);
        if (next != null) {
            removed.removeAll(next);
        }
        return removed.size();
    }

    record State(Map<IdentityKey, CardEntry> cards) {
        State {
            cards = cards == null || cards.isEmpty() ? Map.of() : Map.copyOf(cards);
        }

        static State empty() {
            return new State(Map.of());
        }
    }

    record Result(
            SlotWorkspaceViewModel viewModel,
            State state,
            WorkspaceCardProjectionStats stats
    ) {
        Result {
            viewModel = viewModel == null ? SlotWorkspaceViewModel.empty() : viewModel;
            state = state == null ? State.empty() : state;
            stats = stats == null ? WorkspaceCardProjectionStats.empty() : stats;
        }
    }

    record IdentityKey(String itemId, String comparisonMode, String componentFingerprint) {
        IdentityKey {
            itemId = itemId == null ? "" : itemId;
            comparisonMode = comparisonMode == null || comparisonMode.isBlank()
                    ? ItemComparisonMode.ITEM_ID.name()
                    : comparisonMode;
            componentFingerprint = componentFingerprint == null ? "" : componentFingerprint;
        }

        static IdentityKey from(SlotWorkspaceViewModel.IdentityRef identity) {
            if (identity == null) {
                return new IdentityKey("", ItemComparisonMode.ITEM_ID.name(), "");
            }
            return new IdentityKey(identity.itemId(), identity.comparisonMode(), identity.componentFingerprint());
        }

        static IdentityKey from(ItemIdentity identity) {
            if (identity == null) {
                return new IdentityKey("", ItemComparisonMode.ITEM_ID.name(), "");
            }
            return new IdentityKey(
                    identity.itemId(),
                    identity.comparisonMode() == null ? ItemComparisonMode.ITEM_ID.name() : identity.comparisonMode().name(),
                    identity.componentFingerprint());
        }
    }

    private record CardEntry(
            IdentityKey identity,
            CardLane lane,
            String contentKey,
            SlotWorkspaceViewModel.AtlasItem card,
            int ordinal
    ) {
        private CardEntry {
            identity = identity == null ? new IdentityKey("", ItemComparisonMode.ITEM_ID.name(), "") : identity;
            lane = lane == null ? CardLane.ATLAS : lane;
            contentKey = contentKey == null ? "" : contentKey;
            card = card == null
                    ? new SlotWorkspaceViewModel.AtlasItem(
                            new SlotWorkspaceViewModel.IdentityRef(
                                    identity.itemId(),
                                    identity.comparisonMode(),
                                    identity.componentFingerprint()),
                            net.minecraft.world.item.ItemStack.EMPTY,
                            identity.itemId(),
                            0,
                            0,
                            "",
                            false,
                            false,
                            false,
                            List.of())
                    : card;
            ordinal = Math.max(0, ordinal);
        }
    }

    private enum CardLane {
        ATLAS,
        TRIAGE
    }

    private static final class StatsCounter {
        private int reused;
        private int rebuilt;

        private void reuse() {
            reused++;
        }

        private void rebuild() {
            rebuilt++;
        }

        private int reused() {
            return reused;
        }

        private int rebuilt() {
            return rebuilt;
        }
    }
}
