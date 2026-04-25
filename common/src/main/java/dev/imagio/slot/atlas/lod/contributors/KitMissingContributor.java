package dev.imagio.slot.atlas.lod.contributors;

import dev.imagio.slot.atlas.lod.RelevanceContext;
import dev.imagio.slot.atlas.lod.RelevanceContributor;
import dev.imagio.slot.inventory.core.ItemIdentity;

/**
 * Items the active kit wants but the player does not currently have
 * anywhere — kit "missing" / wanted-but-absent. Same weight as
 * {@link KitMemberContributor} ({@code 0.85}) so wanted ghosts stay
 * readable and the gather flow has visible targets at any zoom.
 *
 * <p>Membership is computed by {@code AtlasRelevance} when building
 * the context (kit's bring list ∖ carried identities). The contributor
 * itself is a pure set lookup.
 */
public final class KitMissingContributor implements RelevanceContributor {
    public static final String NAME = "kit_missing";
    private static final float MISSING_SCORE = 0.85f;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public float score(ItemIdentity identity, RelevanceContext context) {
        return context != null && context.isActiveKitMissing(identity) ? MISSING_SCORE : 0f;
    }
}
