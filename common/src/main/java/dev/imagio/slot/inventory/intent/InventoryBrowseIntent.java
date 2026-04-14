package dev.imagio.slot.inventory.intent;

import dev.imagio.slot.inventory.browse.InventoryBrowseFilter;
import dev.imagio.slot.inventory.browse.InventoryBrowseSessionState;
import dev.imagio.slot.inventory.core.InventoryPaneMembership;

public sealed interface InventoryBrowseIntent extends InventoryIntent permits
        InventoryBrowseIntent.UpdateBrowseState,
        InventoryBrowseIntent.UpdateFilter,
        InventoryBrowseIntent.SelectPane,
        InventoryBrowseIntent.SelectSubject,
        InventoryBrowseIntent.PinTool {

    @Override
    default InventoryIntentKind kind() {
        return InventoryIntentKind.BROWSE;
    }

    record UpdateBrowseState(
            InventoryBrowseSessionState state,
            String origin
    ) implements InventoryBrowseIntent {
        public UpdateBrowseState {
            origin = origin == null ? "" : origin;
        }
    }

    record UpdateFilter(
            InventoryBrowseFilter filter,
            String origin
    ) implements InventoryBrowseIntent {
        public UpdateFilter {
            origin = origin == null ? "" : origin;
        }
    }

    record SelectPane(
            InventoryPaneMembership paneMembership,
            String origin
    ) implements InventoryBrowseIntent {
        public SelectPane {
            paneMembership = paneMembership == null ? InventoryPaneMembership.CARRIED : paneMembership;
            origin = origin == null ? "" : origin;
        }
    }

    record SelectSubject(
            dev.imagio.slot.inventory.browse.InventoryBrowseSubjectRef subjectRef,
            String origin
    ) implements InventoryBrowseIntent {
        public SelectSubject {
            subjectRef = subjectRef == null ? null : dev.imagio.slot.inventory.browse.InventoryBrowseSubjectRef.parse(subjectRef.stableKey());
            origin = origin == null ? "" : origin;
        }
    }

    record PinTool(
            String toolId,
            String origin
    ) implements InventoryBrowseIntent {
        public PinTool {
            toolId = toolId == null ? "" : toolId;
            origin = origin == null ? "" : origin;
        }
    }
}
