package dev.imagio.slot.atlas.lod.contributors;

import dev.imagio.slot.atlas.lod.RelevanceContext;
import dev.imagio.slot.atlas.lod.RelevanceContributor;
import dev.imagio.slot.inventory.core.ItemIdentity;

/**
 * Items the active kit references on its belt, offhand, or bring
 * list. Score {@code 0.85} — high enough that a kit-active atlas
 * reads as "kit-shaped," low enough that search matches still
 * outrank it.
 */
public final class KitMemberContributor implements RelevanceContributor {
    public static final String NAME = "kit_member";
    private static final float MEMBER_SCORE = 0.85f;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public float score(ItemIdentity identity, RelevanceContext context) {
        return context != null && context.isActiveKitMember(identity) ? MEMBER_SCORE : 0f;
    }
}
