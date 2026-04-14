package dev.imagio.slot.client.screen;

import dev.imagio.slot.client.collection.CollectionStore;
import dev.imagio.slot.client.collection.CollectionViewStateController;
import dev.imagio.slot.projection.InventoryViewData;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class InventorySectionActionPlanner {
    private InventorySectionActionPlanner() {
    }

    public static List<SectionActionSpec> buildSharedActions(
            InventoryViewData.Section section,
            CollectionStore collectionStore,
            CollectionViewStateController viewStateController,
            boolean hasVisibleRecentEntries,
            boolean collectionCollapsed,
            boolean collectionHasLoadouts,
            Runnable clearRecentAction,
            Runnable captureCurrentHotbarAction,
            Runnable deleteCollectionAction,
            Runnable toggleCollapseAction,
            Runnable togglePinnedLoadoutsAction,
            Runnable deleteJunkAction
    ) {
        if (section == null || collectionStore == null || viewStateController == null) {
            return List.of();
        }

        List<SectionActionSpec> actions = new ArrayList<>();
        if (section.isRecent() && hasVisibleRecentEntries && clearRecentAction != null) {
            actions.add(new SectionActionSpec(
                    Component.translatable("slot.screen.recent.clear").getString(),
                    clearRecentAction
            ));
        }

        if (section.isCollection() && collectionStore.isUserCollection(section.collectionId())) {
            if (captureCurrentHotbarAction != null) {
                actions.add(new SectionActionSpec(
                        Component.translatable("slot.screen.collections.capture_current_hotbar").getString(),
                        captureCurrentHotbarAction
                ));
            }
            if (deleteCollectionAction != null) {
                actions.add(new SectionActionSpec(
                        Component.translatable("slot.screen.collections.delete").getString(),
                        deleteCollectionAction
                ));
            }
        }

        if (section.isCollection() && toggleCollapseAction != null) {
            actions.add(new SectionActionSpec(
                    Component.translatable(
                            collectionCollapsed
                                    ? "slot.screen.collections.expand"
                                    : "slot.screen.collections.collapse"
                    ).getString(),
                    toggleCollapseAction
            ));

            if (collectionHasLoadouts && togglePinnedLoadoutsAction != null) {
                actions.add(new SectionActionSpec(
                        Component.translatable(
                                viewStateController.pinLoadoutsWhenCollectionCollapsed(section.collectionId())
                                        ? "slot.screen.collections.unpin_loadouts_when_collapsed"
                                        : "slot.screen.collections.pin_loadouts_when_collapsed"
                        ).getString(),
                        togglePinnedLoadoutsAction
                ));
            }

            if (CollectionStore.JUNK_ID.equals(section.collectionId()) && deleteJunkAction != null) {
                actions.add(new SectionActionSpec(
                        Component.translatable("slot.screen.collections.junk.delete_all_now").getString(),
                        deleteJunkAction
                ));
            }
        }

        return List.copyOf(actions);
    }

    public record SectionActionSpec(String label, Runnable action) {
    }
}
