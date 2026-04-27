package dev.imagio.slot.atlas.lod;

import dev.imagio.slot.atlas.AtlasSearchIndex;
import dev.imagio.slot.atlas.lod.contributors.AreaProximityContributor;
import dev.imagio.slot.atlas.lod.contributors.CarriedContributor;
import dev.imagio.slot.atlas.lod.contributors.ChestHoldsRelevantContributor;
import dev.imagio.slot.atlas.lod.contributors.KitMemberContributor;
import dev.imagio.slot.atlas.lod.contributors.KitMissingContributor;
import dev.imagio.slot.atlas.lod.contributors.RecentlyTouchedContributor;
import dev.imagio.slot.atlas.lod.contributors.SearchMatchContributor;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Helper that builds a {@link RelevanceContext} from a
 * {@link SlotWorkspaceViewModel} snapshot and scores items through
 * the configured contributor chain.
 *
 * <p>Per
 * {@code docs/decisions/0005-relevance-score-and-layout-locality.md},
 * scoring is a derivation: callers (server-side auto-home, client-side
 * layout) build their own context from the inputs they have and
 * choose the contributor list that fits their consumer.
 * {@link #DEFAULT_CONTRIBUTORS} is the client-side layout/render
 * contributor list — a server-side caller may want a different mix
 * (e.g., omit search match, include classification confidence).
 */
public final class AtlasRelevance {
    /**
     * Client-side contributor list for layout + render. Order does not
     * affect the combined score (max is commutative); it only sets the
     * iteration order in the per-contributor breakdown shown by the
     * debug overlay.
     */
    public static final List<RelevanceContributor> DEFAULT_CONTRIBUTORS = List.of(
            new SearchMatchContributor(),
            new KitMemberContributor(),
            new KitMissingContributor(),
            new CarriedContributor(),
            new RecentlyTouchedContributor(),
            new AreaProximityContributor(),
            new ChestHoldsRelevantContributor()
    );

    private AtlasRelevance() {
    }

    /**
     * Build the context from the view model with no client-only
     * inputs (no search query). Useful for server-side scoring and
     * for tests.
     */
    public static RelevanceContext contextFrom(SlotWorkspaceViewModel viewModel) {
        return contextFrom(viewModel, null);
    }

    /**
     * Build the context from the view model and the active search
     * query. The query is the raw string from
     * {@code SearchController}; matching happens here so contributors
     * remain pure set-membership lookups.
     */
    public static RelevanceContext contextFrom(SlotWorkspaceViewModel viewModel, String activeSearchQuery) {
        if (viewModel == null) {
            return RelevanceContext.empty();
        }
        Set<ItemIdentity> carried = new HashSet<>();
        collectCarried(carried, viewModel.atlasItems());
        collectCarried(carried, viewModel.triageItems());

        Set<ItemIdentity> recents = new HashSet<>();
        collectRecent(recents, viewModel.atlasItems());
        collectRecent(recents, viewModel.triageItems());

        SlotWorkspaceViewModel.KitCard kit = viewModel.activeKit();
        Set<ItemIdentity> kitMembers = collectKitMembers(kit);
        Set<ItemIdentity> kitMissing = collectKitMissing(kit, carried);

        Set<ItemIdentity> searchMatched = collectSearchMatches(viewModel, activeSearchQuery);

        return RelevanceContext.builder()
                .carriedIdentities(carried)
                .recentIdentities(recents)
                .activeKitMembers(kitMembers)
                .activeKitMissing(kitMissing)
                .searchMatchedIdentities(searchMatched)
                .build();
    }

    /**
     * Score a single item against the supplied contributor chain.
     */
    public static RelevanceScore scoreFor(
            SlotWorkspaceViewModel.AtlasItem item,
            RelevanceContext context,
            List<RelevanceContributor> contributors
    ) {
        if (item == null) {
            return RelevanceScore.BASELINE;
        }
        ItemIdentity identity = item.identity().toIdentity();
        return RelevanceScore.compute(identity, context, contributors);
    }

    /**
     * Compute scores for every atlas + triage item in the view model
     * under the supplied contributor chain. Keyed by
     * {@link SlotWorkspaceViewModel.IdentityRef} for stable lookup
     * against the records the renderer already holds.
     */
    public static Map<SlotWorkspaceViewModel.IdentityRef, RelevanceScore> scoresFor(
            SlotWorkspaceViewModel viewModel,
            RelevanceContext context,
            List<RelevanceContributor> contributors
    ) {
        if (viewModel == null) {
            return Map.of();
        }
        RelevanceContext ctx = context == null ? contextFrom(viewModel) : context;
        LinkedHashMap<SlotWorkspaceViewModel.IdentityRef, RelevanceScore> scores = new LinkedHashMap<>();
        accumulate(scores, viewModel.atlasItems(), ctx, contributors);
        accumulate(scores, viewModel.triageItems(), ctx, contributors);
        return Map.copyOf(scores);
    }

    /**
     * Convenience overload that builds a context with no active
     * search query.
     */
    public static Map<SlotWorkspaceViewModel.IdentityRef, RelevanceScore> scoresFor(
            SlotWorkspaceViewModel viewModel,
            List<RelevanceContributor> contributors
    ) {
        return scoresFor(viewModel, contextFrom(viewModel), contributors);
    }

    private static void collectCarried(
            Set<ItemIdentity> sink,
            List<SlotWorkspaceViewModel.AtlasItem> items
    ) {
        if (items == null) {
            return;
        }
        for (SlotWorkspaceViewModel.AtlasItem item : items) {
            if (item != null && item.carried()) {
                sink.add(item.identity().toIdentity());
            }
        }
    }

    private static void collectRecent(
            Set<ItemIdentity> sink,
            List<SlotWorkspaceViewModel.AtlasItem> items
    ) {
        if (items == null) {
            return;
        }
        for (SlotWorkspaceViewModel.AtlasItem item : items) {
            if (item != null && item.recent()) {
                sink.add(item.identity().toIdentity());
            }
        }
    }

    private static Set<ItemIdentity> collectKitMembers(SlotWorkspaceViewModel.KitCard kit) {
        if (kit == null) {
            return Set.of();
        }
        HashSet<ItemIdentity> members = new HashSet<>();
        if (kit.pages() != null) {
            for (SlotWorkspaceViewModel.KitPageView page : kit.pages()) {
                if (page == null || page.slots() == null) {
                    continue;
                }
                for (SlotWorkspaceViewModel.KitSlotState slot : page.slots()) {
                    if (slot != null && slot.filled()) {
                        ItemIdentity id = slot.identity().toIdentity();
                        if (id != null) {
                            members.add(id);
                        }
                    }
                }
            }
        }
        if (kit.bring() != null) {
            for (SlotWorkspaceViewModel.KitBringItem bringItem : kit.bring()) {
                if (bringItem == null) {
                    continue;
                }
                ItemIdentity id = bringItem.identity().toIdentity();
                if (id != null) {
                    members.add(id);
                }
            }
        }
        return members;
    }

    private static Set<ItemIdentity> collectKitMissing(
            SlotWorkspaceViewModel.KitCard kit,
            Set<ItemIdentity> carried
    ) {
        if (kit == null || kit.bring() == null || kit.bring().isEmpty()) {
            return Set.of();
        }
        HashSet<ItemIdentity> missing = new HashSet<>();
        for (SlotWorkspaceViewModel.KitBringItem bringItem : kit.bring()) {
            if (bringItem == null) {
                continue;
            }
            ItemIdentity id = bringItem.identity().toIdentity();
            if (id != null && !carried.contains(id)) {
                missing.add(id);
            }
        }
        return missing;
    }

    private static Set<ItemIdentity> collectSearchMatches(
            SlotWorkspaceViewModel viewModel,
            String activeSearchQuery
    ) {
        if (activeSearchQuery == null || activeSearchQuery.isEmpty()) {
            return Set.of();
        }
        HashSet<ItemIdentity> matches = new HashSet<>();
        collectSearchMatches(matches, viewModel.atlasItems(), activeSearchQuery);
        collectSearchMatches(matches, viewModel.triageItems(), activeSearchQuery);
        return matches;
    }

    private static void collectSearchMatches(
            Set<ItemIdentity> sink,
            List<SlotWorkspaceViewModel.AtlasItem> items,
            String query
    ) {
        if (items == null) {
            return;
        }
        for (SlotWorkspaceViewModel.AtlasItem item : items) {
            if (item == null) {
                continue;
            }
            if (AtlasSearchIndex.matches(item.name(), query)) {
                ItemIdentity id = item.identity().toIdentity();
                if (id != null) {
                    sink.add(id);
                }
            }
        }
    }

    private static void accumulate(
            Map<SlotWorkspaceViewModel.IdentityRef, RelevanceScore> sink,
            List<SlotWorkspaceViewModel.AtlasItem> items,
            RelevanceContext context,
            List<RelevanceContributor> contributors
    ) {
        if (items == null) {
            return;
        }
        for (SlotWorkspaceViewModel.AtlasItem item : items) {
            if (item == null) {
                continue;
            }
            sink.putIfAbsent(item.identity(), scoreFor(item, context, contributors));
        }
    }
}
