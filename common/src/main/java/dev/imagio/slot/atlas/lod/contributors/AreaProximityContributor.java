package dev.imagio.slot.atlas.lod.contributors;

import dev.imagio.slot.atlas.lod.RelevanceContext;
import dev.imagio.slot.atlas.lod.RelevanceContributor;
import dev.imagio.slot.inventory.core.ItemIdentity;

/**
 * Items whose home island links to a chest in a currently-proximate
 * storage area score high, so as the player walks toward base, items
 * homed there pop in the carried zone. Phase 4 of
 * {@code docs/plans/storage-areas.md}; the
 * {@link RelevanceContext#areaProximityBoostedIdentities()} set is
 * precomputed by the context-builder upstream because the visual-home
 * graph + area proximity can't be efficiently re-derived per identity.
 */
public final class AreaProximityContributor implements RelevanceContributor {
    public static final String NAME = "area_proximity";
    private static final float SCORE = 0.8f;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public float score(ItemIdentity identity, RelevanceContext context) {
        return context != null && context.isAreaProximityBoosted(identity) ? SCORE : 0f;
    }
}
