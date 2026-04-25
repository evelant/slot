package dev.imagio.slot.atlas.lod.contributors;

import dev.imagio.slot.atlas.lod.RelevanceContext;
import dev.imagio.slot.atlas.lod.RelevanceContributor;
import dev.imagio.slot.inventory.core.ItemIdentity;

/**
 * Items that recently entered or left carried inventory. Medium
 * weight ({@code 0.6}) — keeps "what was I just dealing with"
 * readable without crowding out higher-priority signals like search
 * or the active kit.
 *
 * <p>Decay is owned by the workflow domain's
 * {@code RecentView.visibleItems()} — items leave the recent set on
 * subsequent inventory opens. The contributor is purely a set
 * lookup; no time math here.
 */
public final class RecentlyTouchedContributor implements RelevanceContributor {
    public static final String NAME = "recently_touched";
    private static final float RECENT_SCORE = 0.6f;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public float score(ItemIdentity identity, RelevanceContext context) {
        return context != null && context.isRecent(identity) ? RECENT_SCORE : 0f;
    }
}
