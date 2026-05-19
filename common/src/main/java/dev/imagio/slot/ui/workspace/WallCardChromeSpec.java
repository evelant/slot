package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;

import java.util.List;

/**
 * Common item-card state grammar. It intentionally keeps numeric target,
 * storage, and action-route signals in separate lanes so Forge and NeoForge
 * render the same card semantics.
 */
public record WallCardChromeSpec(
        String countText,
        TargetSource targetSource,
        int countBadgeColor,
        String nearbyText,
        boolean nearbyRouteOnly,
        String distantText,
        Ring ring,
        int ringColor,
        Gap gap
) {
    public enum TargetSource {
        NONE,
        GLOBAL_DESIRED,
        WANTED,
        WORKFLOW_TAB
    }

    public enum Ring {
        NONE,
        PREVIEW_DEPOSIT,
        PREVIEW_GATHER,
        PUT_AWAY_ROUTED,
        PUT_AWAY_NO_ROUTE,
        TARGET_STORED,
        TARGET_MIXED,
        TARGET_MISSING
    }

    public enum Gap {
        NONE,
        STORED,
        MIXED,
        MISSING
    }

    public WallCardChromeSpec {
        countText = countText == null ? "" : countText;
        targetSource = targetSource == null ? TargetSource.NONE : targetSource;
        nearbyText = nearbyText == null ? "" : nearbyText;
        distantText = distantText == null ? "" : distantText;
        ring = ring == null ? Ring.NONE : ring;
        gap = gap == null ? Gap.NONE : gap;
    }

    public static WallCardChromeSpec from(
            SlotWorkspaceViewModel.AtlasItem item,
            boolean activeSearchMatch,
            boolean forceDistantContext,
            StorageGhostRevealMode revealMode,
            boolean hasProximateDepositRoute,
            boolean depositPreviewActive,
            boolean gatherPreviewActive,
            boolean gatherPreviewEligible
    ) {
        if (item == null) {
            return empty();
        }
        int carried = carriedCount(item);
        TargetChoice target = targetChoice(item);
        String countText = countText(carried, target.count());
        int countBadgeColor = countBadgeColor(target.source());
        int nearbyCount = presenceCount(item.presence());
        String nearbyText = nearbyCount > 0 ? topCountText(nearbyCount, false) : "";
        boolean nearbyRouteOnly = nearbyCount <= 0 && hasProximateDepositRoute;
        boolean showDistant = activeSearchMatch
                || forceDistantContext
                || revealMode == StorageGhostRevealMode.TRACKED;
        int distantCount = showDistant ? presenceCount(item.elsewhere()) : 0;
        String distantText = distantCount > 0 ? topCountText(distantCount, true) : "";
        Gap gap = gap(carried, target.count(), nearbyCount + presenceCount(item.elsewhere()));
        Ring ring = ring(item, gap, hasProximateDepositRoute, depositPreviewActive, gatherPreviewActive,
                gatherPreviewEligible);
        return new WallCardChromeSpec(
                countText,
                target.source(),
                countBadgeColor,
                nearbyText,
                nearbyRouteOnly,
                distantText,
                ring,
                ringColor(ring),
                gap);
    }

    public static WallCardChromeSpec empty() {
        return new WallCardChromeSpec(
                "",
                TargetSource.NONE,
                WorkspaceUiPalette.COUNT_BADGE_NEUTRAL,
                "",
                false,
                "",
                Ring.NONE,
                0,
                Gap.NONE);
    }

    static TargetChoice targetChoice(SlotWorkspaceViewModel.AtlasItem item) {
        if (item == null) {
            return new TargetChoice(0, TargetSource.NONE);
        }
        int desired = Math.max(0, item.desiredCount());
        int wanted = Math.max(0, item.wantedCount());
        int workflow = item.desiredCountFromKit() ? desired : 0;
        if (workflow <= 0 && item.kitNeeded()) {
            workflow = 1;
        }
        int globalDesired = item.desiredCountFromKit() ? 0 : desired;
        int target = Math.max(Math.max(globalDesired, wanted), workflow);
        if (target <= 0) {
            return new TargetChoice(0, TargetSource.NONE);
        }
        if (workflow == target) {
            return new TargetChoice(target, TargetSource.WORKFLOW_TAB);
        }
        if (wanted == target) {
            return new TargetChoice(target, TargetSource.WANTED);
        }
        return new TargetChoice(target, TargetSource.GLOBAL_DESIRED);
    }

    private static String countText(int carried, int target) {
        if (target > 0) {
            return countPart(carried) + "/" + countPart(target);
        }
        return carried <= 0 ? "" : countPart(carried);
    }

    private static String countPart(int count) {
        return count <= 0 ? "0" : WorkspaceCountFormat.compact(count);
    }

    private static String topCountText(int count, boolean plus) {
        if (count <= 0) {
            return "";
        }
        String part;
        if (count >= 1000) {
            part = Math.min(9, count / 1000) + "k";
        } else if (count >= 100) {
            part = plus ? "99" : "99+";
        } else {
            part = Integer.toString(count);
        }
        return plus ? "+" + part : part;
    }

    private static int countBadgeColor(TargetSource source) {
        return switch (source) {
            case NONE -> WorkspaceUiPalette.COUNT_BADGE_NEUTRAL;
            case GLOBAL_DESIRED -> WorkspaceUiPalette.COUNT_BADGE_DESIRED;
            case WANTED -> WorkspaceUiPalette.COUNT_BADGE_WANTED;
            case WORKFLOW_TAB -> WorkspaceUiPalette.COUNT_BADGE_WORKFLOW;
        };
    }

    private static Gap gap(int carried, int target, int stored) {
        if (target <= 0 || carried >= target) {
            return Gap.NONE;
        }
        int missing = target - carried;
        if (stored <= 0) {
            return Gap.MISSING;
        }
        return stored >= missing ? Gap.STORED : Gap.MIXED;
    }

    private static Ring ring(
            SlotWorkspaceViewModel.AtlasItem item,
            Gap gap,
            boolean hasProximateDepositRoute,
            boolean depositPreviewActive,
            boolean gatherPreviewActive,
            boolean gatherPreviewEligible
    ) {
        if (depositPreviewActive && hasProximateDepositRoute) {
            return Ring.PREVIEW_DEPOSIT;
        }
        if (gatherPreviewActive && gatherPreviewEligible) {
            return Ring.PREVIEW_GATHER;
        }
        if (item != null && item.putAwayState().routed()) {
            return Ring.PUT_AWAY_ROUTED;
        }
        if (item != null && item.putAwayState().noRoute()) {
            return Ring.PUT_AWAY_NO_ROUTE;
        }
        return switch (gap) {
            case NONE -> Ring.NONE;
            case STORED -> Ring.TARGET_STORED;
            case MIXED -> Ring.TARGET_MIXED;
            case MISSING -> Ring.TARGET_MISSING;
        };
    }

    public static int ringColor(Ring ring) {
        return switch (ring == null ? Ring.NONE : ring) {
            case NONE -> 0;
            case PREVIEW_DEPOSIT -> WorkspaceUiPalette.PREVIEW_DEPOSIT;
            case PREVIEW_GATHER -> WorkspaceUiPalette.PREVIEW_GATHER;
            case PUT_AWAY_ROUTED -> WorkspaceUiPalette.PUT_AWAY_ROUTED;
            case PUT_AWAY_NO_ROUTE -> WorkspaceUiPalette.PUT_AWAY_NO_ROUTE;
            case TARGET_STORED -> WorkspaceUiPalette.TARGET_STORED;
            case TARGET_MIXED -> WorkspaceUiPalette.TARGET_MIXED;
            case TARGET_MISSING -> WorkspaceUiPalette.TARGET_MISSING;
        };
    }

    private static int carriedCount(SlotWorkspaceViewModel.AtlasItem item) {
        return item != null && item.carried() ? Math.max(0, item.totalCount()) : 0;
    }

    private static int presenceCount(List<SlotWorkspaceViewModel.ChestPresenceEntry> entries) {
        int count = 0;
        if (entries == null) {
            return 0;
        }
        for (SlotWorkspaceViewModel.ChestPresenceEntry entry : entries) {
            if (entry != null) {
                count += Math.max(0, entry.count());
            }
        }
        return count;
    }

    public record TargetChoice(int count, TargetSource source) {
        public TargetChoice {
            count = Math.max(0, count);
            source = source == null || count <= 0 ? TargetSource.NONE : source;
        }
    }
}
