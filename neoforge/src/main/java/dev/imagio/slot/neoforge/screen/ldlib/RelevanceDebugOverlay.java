package dev.imagio.slot.neoforge.screen.ldlib;

import dev.imagio.slot.atlas.lod.AtlasRelevance;
import dev.imagio.slot.atlas.lod.RelevanceContext;
import dev.imagio.slot.atlas.lod.RelevanceScore;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;

import java.util.Locale;

/**
 * Toggleable debug overlay that surfaces per-card relevance scores so
 * we can see why an item picked the band it did. Off by default; the
 * key binding is unbound and surfaced in the Controls menu.
 *
 * <p>State is process-global — it's a debug switch, not session
 * state. The toggle survives screen close/reopen, which is what we
 * want when iterating on contributor tuning.
 */
final class RelevanceDebugOverlay {
    private static volatile boolean enabled = false;

    private RelevanceDebugOverlay() {
    }

    static boolean enabled() {
        return enabled;
    }

    static void toggle() {
        enabled = !enabled;
    }

    /**
     * Compute the score for an item under the default contributor
     * chain. Cheap — no caching needed at this stage; revisit if the
     * per-frame cost grows. The {@code activeSearchQuery} is read so
     * the badge reflects the search contributor too when a query is
     * active.
     */
    static RelevanceScore scoreFor(
            SlotWorkspaceViewModel.AtlasItem item,
            SlotWorkspaceViewModel viewModel,
            String activeSearchQuery
    ) {
        if (item == null) {
            return RelevanceScore.BASELINE;
        }
        RelevanceContext ctx = AtlasRelevance.contextFrom(viewModel, activeSearchQuery);
        return AtlasRelevance.scoreFor(item, ctx, AtlasRelevance.DEFAULT_CONTRIBUTORS);
    }

    /**
     * "0.90", "0.00", etc. Two decimal places — enough resolution to
     * see contributor differences without crowding the badge.
     */
    static String formatScore(RelevanceScore score) {
        if (score == null) {
            return "0.00";
        }
        return String.format(Locale.ROOT, "%.2f", score.value());
    }
}
