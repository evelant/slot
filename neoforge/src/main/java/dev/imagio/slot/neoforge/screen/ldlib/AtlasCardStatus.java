package dev.imagio.slot.neoforge.screen.ldlib;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.inventory.workspace.WorkspaceItemTargets;

/**
 * Card-level fulfillment status derived from the projection's per-item
 * fields. Drives the status border + count text so the signals share
 * one vocabulary without adding extra chrome.
 *
 * <p>The status answers "do you have enough, and if not how easy is it
 * to fix?" The answer maps to one of {@link Level}; orthogonal kit
 * relevance modulates the colour saturation rather than introducing a
 * separate axis.
 */
record AtlasCardStatus(Level level, boolean kitRelevant, int carriedCount, int storedCount, int targetCount) {
    enum Level {
        /** No target count. No status signal. */
        NEUTRAL,
        /** Carried &gt;= target — explicit fulfilled signal (only when target &gt; 0). */
        FULFILLED,
        /** Gap fully covered by storage; player just needs to walk to a chest. */
        STORED,
        /** Some of the gap in storage, rest must be crafted/sourced elsewhere. */
        MIXED,
        /** No storage holds this; player must craft / find more. */
        CRAFT
    }

    static AtlasCardStatus from(SlotWorkspaceViewModel.AtlasItem item) {
        if (item == null) {
            return new AtlasCardStatus(Level.NEUTRAL, false, 0, 0, 0);
        }
        int carried = item.carried() ? item.totalCount() : 0;
        int stored = 0;
        for (SlotWorkspaceViewModel.ChestPresenceEntry entry : item.presence()) {
            stored += entry.count();
        }
        for (SlotWorkspaceViewModel.ChestPresenceEntry entry : item.elsewhere()) {
            stored += entry.count();
        }
        WorkspaceItemTargets targets = WorkspaceItemTargets.from(item);
        int target = targets.displayTargetCount();
        boolean kitRelevant = item.desiredCountFromKit() || item.kitNeeded();
        // Kit page slots (binary "kit wants one of this") don't carry an
        // explicit desired count, but they're functionally desired=1 for
        // status purposes — without this fallback the old kit-star case
        // would render NEUTRAL and lose all signal.
        if (target <= 0 && item.kitNeeded()) {
            target = 1;
        }
        if (target <= 0) {
            return new AtlasCardStatus(Level.NEUTRAL, kitRelevant, carried, stored, 0);
        }
        if (carried >= target) {
            return new AtlasCardStatus(Level.FULFILLED, kitRelevant, carried, stored, target);
        }
        int gap = target - carried;
        Level level;
        if (stored <= 0) {
            level = Level.CRAFT;
        } else if (stored >= gap) {
            level = Level.STORED;
        } else {
            level = Level.MIXED;
        }
        return new AtlasCardStatus(level, kitRelevant, carried, stored, target);
    }

    /** Status colour for borders, count text, and the progress bar's gap segment. */
    int color() {
        return switch (level) {
            case NEUTRAL -> 0;
            case FULFILLED -> kitRelevant
                    ? WorkspaceTheme.STATUS_FULFILLED_KIT
                    : WorkspaceTheme.STATUS_FULFILLED_PLAYER;
            case STORED -> kitRelevant
                    ? WorkspaceTheme.STATUS_STORED_KIT
                    : WorkspaceTheme.STATUS_STORED_PLAYER;
            case MIXED -> kitRelevant
                    ? WorkspaceTheme.STATUS_MIXED_KIT
                    : WorkspaceTheme.STATUS_MIXED_PLAYER;
            case CRAFT -> kitRelevant
                    ? WorkspaceTheme.STATUS_CRAFT_KIT
                    : WorkspaceTheme.STATUS_CRAFT_PLAYER;
        };
    }

    /** True when the card should paint a status border (carried &lt; desired). */
    boolean wantsBorder() {
        return level == Level.STORED || level == Level.MIXED || level == Level.CRAFT;
    }

}
