package dev.imagio.slot.atlas.lod.contributors;

import dev.imagio.slot.atlas.lod.RelevanceContext;
import dev.imagio.slot.atlas.lod.RelevanceContributor;
import dev.imagio.slot.inventory.core.ItemIdentity;

/**
 * Items in the player's carried inventory (mainhand, offhand, hotbar,
 * main) score high. The seed of the relevance model — see
 * {@code docs/design/relevance-lod.md § Relevance contributors}.
 */
public final class CarriedContributor implements RelevanceContributor {
    public static final String NAME = "carried";
    private static final float CARRIED_SCORE = 0.9f;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public float score(ItemIdentity identity, RelevanceContext context) {
        return context != null && context.isCarried(identity) ? CARRIED_SCORE : 0f;
    }
}
