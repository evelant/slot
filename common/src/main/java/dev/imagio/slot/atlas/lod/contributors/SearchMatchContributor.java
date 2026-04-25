package dev.imagio.slot.atlas.lod.contributors;

import dev.imagio.slot.atlas.lod.RelevanceContext;
import dev.imagio.slot.atlas.lod.RelevanceContributor;
import dev.imagio.slot.inventory.core.ItemIdentity;

/**
 * Items whose name/key matches the active atlas search query. Score
 * {@code 0.95} — outranks {@code carried} so search results pop out
 * of the local constellation. Match membership is precomputed in the
 * context so this contributor is a pure set lookup.
 */
public final class SearchMatchContributor implements RelevanceContributor {
    public static final String NAME = "search_match";
    private static final float MATCH_SCORE = 0.95f;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public float score(ItemIdentity identity, RelevanceContext context) {
        return context != null && context.matchesActiveSearch(identity) ? MATCH_SCORE : 0f;
    }
}
