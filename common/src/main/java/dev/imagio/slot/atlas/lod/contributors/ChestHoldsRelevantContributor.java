package dev.imagio.slot.atlas.lod.contributors;

import dev.imagio.slot.atlas.lod.RelevanceContext;
import dev.imagio.slot.atlas.lod.RelevanceContributor;
import dev.imagio.slot.inventory.core.ItemIdentity;

/**
 * Items present inside a chest that itself contains a kit-missing /
 * search-matched item score moderately. The intent is that when a
 * search "lights up" a chest off-base, its other contents become
 * context — the player can see what else is in there at a glance.
 *
 * <p>{@link RelevanceContext#chestHoldsRelevantIdentities()} is
 * precomputed by the context-builder upstream from chest contents +
 * active-kit/search state. Phase 4 of
 * {@code docs/plans/storage-areas.md}.
 */
public final class ChestHoldsRelevantContributor implements RelevanceContributor {
    public static final String NAME = "chest_holds_relevant";
    private static final float SCORE = 0.6f;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public float score(ItemIdentity identity, RelevanceContext context) {
        return context != null && context.chestHoldsRelevant(identity) ? SCORE : 0f;
    }
}
