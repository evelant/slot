package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public sealed interface WorkflowEvent permits
        WorkflowEvent.CollectionCreated,
        WorkflowEvent.CollectionRenamed,
        WorkflowEvent.CollectionDeleted,
        WorkflowEvent.CollectionItemAdded,
        WorkflowEvent.CollectionItemRemoved,
        WorkflowEvent.DesiredCountSet,
        WorkflowEvent.LoadoutCreated,
        WorkflowEvent.LoadoutRenamed,
        WorkflowEvent.LoadoutUpdated,
        WorkflowEvent.LoadoutDeleted,
        WorkflowEvent.FavoriteMarked,
        WorkflowEvent.FavoriteUnmarked,
        WorkflowEvent.JunkMarked,
        WorkflowEvent.JunkUnmarked,
        WorkflowEvent.ProtectedIdentityMarked,
        WorkflowEvent.ProtectedIdentityUnmarked,
        WorkflowEvent.ProtectedTargetMarked,
        WorkflowEvent.ProtectedTargetUnmarked,
        WorkflowEvent.PortableContainerProtectionSet,
        WorkflowEvent.RecentDismissedUpTo,
        WorkflowEvent.VisualIslandCreated,
        WorkflowEvent.VisualIslandMoved,
        WorkflowEvent.VisualIslandRenamed,
        WorkflowEvent.VisualIslandRecolored,
        WorkflowEvent.VisualIslandIconChanged,
        WorkflowEvent.VisualIslandDeleted,
        WorkflowEvent.VisualHomeAssigned,
        WorkflowEvent.VisualHomeCleared,
        WorkflowEvent.TemplateIslandDismissed,
        WorkflowEvent.ClaimedChestCreated,
        WorkflowEvent.ClaimedChestMoved,
        WorkflowEvent.ClaimedChestAnchorsChanged,
        WorkflowEvent.ClaimedChestRelabeled,
        WorkflowEvent.ClaimedChestDeleted,
        WorkflowEvent.ChestLinkCreated,
        WorkflowEvent.ChestLinkRemoved,
        WorkflowEvent.KitCreated,
        WorkflowEvent.KitUpdated,
        WorkflowEvent.KitDeleted,
        WorkflowEvent.KitActivated,
        WorkflowEvent.KitDeactivated,
        WorkflowEvent.KitPageSwitched {

    record CollectionCreated(
            String collectionId,
            String name
    ) implements WorkflowEvent {
        public CollectionCreated {
            collectionId = collectionId == null ? "" : collectionId;
            name = name == null ? "" : name;
        }
    }

    record CollectionRenamed(
            String collectionId,
            String name
    ) implements WorkflowEvent {
        public CollectionRenamed {
            collectionId = collectionId == null ? "" : collectionId;
            name = name == null ? "" : name;
        }
    }

    record CollectionDeleted(
            String collectionId
    ) implements WorkflowEvent {
        public CollectionDeleted {
            collectionId = collectionId == null ? "" : collectionId;
        }
    }

    record CollectionItemAdded(
            String collectionId,
            ItemIdentity identity
    ) implements WorkflowEvent {
        public CollectionItemAdded {
            collectionId = collectionId == null ? "" : collectionId;
        }
    }

    record CollectionItemRemoved(
            String collectionId,
            ItemIdentity identity
    ) implements WorkflowEvent {
        public CollectionItemRemoved {
            collectionId = collectionId == null ? "" : collectionId;
        }
    }

    record DesiredCountSet(
            String collectionId,
            ItemIdentity identity,
            int desiredCount
    ) implements WorkflowEvent {
        public DesiredCountSet {
            collectionId = collectionId == null ? "" : collectionId;
            desiredCount = Math.max(1, desiredCount);
        }
    }

    record LoadoutCreated(
            String collectionId,
            QuickAccessLoadoutDefinition loadout
    ) implements WorkflowEvent {
        public LoadoutCreated {
            collectionId = collectionId == null ? "" : collectionId;
        }
    }

    record LoadoutRenamed(
            String collectionId,
            String loadoutId,
            String name
    ) implements WorkflowEvent {
        public LoadoutRenamed {
            collectionId = collectionId == null ? "" : collectionId;
            loadoutId = loadoutId == null ? "" : loadoutId;
            name = name == null ? "" : name;
        }
    }

    record LoadoutUpdated(
            String collectionId,
            String loadoutId,
            Set<QuickAccessLoadoutEntry> entries
    ) implements WorkflowEvent {
        public LoadoutUpdated {
            collectionId = collectionId == null ? "" : collectionId;
            loadoutId = loadoutId == null ? "" : loadoutId;
            entries = entries == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(entries));
        }
    }

    record LoadoutDeleted(
            String collectionId,
            String loadoutId
    ) implements WorkflowEvent {
        public LoadoutDeleted {
            collectionId = collectionId == null ? "" : collectionId;
            loadoutId = loadoutId == null ? "" : loadoutId;
        }
    }

    record FavoriteMarked(
            ItemIdentity identity
    ) implements WorkflowEvent {
    }

    record FavoriteUnmarked(
            ItemIdentity identity
    ) implements WorkflowEvent {
    }

    record JunkMarked(
            ItemIdentity identity
    ) implements WorkflowEvent {
    }

    record JunkUnmarked(
            ItemIdentity identity
    ) implements WorkflowEvent {
    }

    record ProtectedIdentityMarked(
            ItemIdentity identity
    ) implements WorkflowEvent {
    }

    record ProtectedIdentityUnmarked(
            ItemIdentity identity
    ) implements WorkflowEvent {
    }

    record ProtectedTargetMarked(
            InventoryActionTarget target
    ) implements WorkflowEvent {
    }

    record ProtectedTargetUnmarked(
            InventoryActionTarget target
    ) implements WorkflowEvent {
    }

    record PortableContainerProtectionSet(
            boolean enabled
    ) implements WorkflowEvent {
    }

    record RecentDismissedUpTo(
            ItemIdentity identity,
            long dismissedUpToGlobalSequence
    ) implements WorkflowEvent {
        public RecentDismissedUpTo {
            dismissedUpToGlobalSequence = Math.max(0L, dismissedUpToGlobalSequence);
        }
    }

    record VisualIslandCreated(
            VisualAtlasIsland island
    ) implements WorkflowEvent {
    }

    record VisualIslandMoved(
            String islandId,
            int x,
            int y
    ) implements WorkflowEvent {
        public VisualIslandMoved {
            islandId = islandId == null ? "" : islandId;
        }
    }

    record VisualIslandRenamed(
            String islandId,
            String label
    ) implements WorkflowEvent {
        public VisualIslandRenamed {
            islandId = islandId == null ? "" : islandId;
            label = label == null ? "" : label;
        }
    }

    record VisualIslandRecolored(
            String islandId,
            int color
    ) implements WorkflowEvent {
        public VisualIslandRecolored {
            islandId = islandId == null ? "" : islandId;
        }
    }

    record VisualIslandIconChanged(
            String islandId,
            ItemIdentity iconIdentity
    ) implements WorkflowEvent {
        public VisualIslandIconChanged {
            islandId = islandId == null ? "" : islandId;
        }
    }

    record VisualIslandDeleted(
            String islandId
    ) implements WorkflowEvent {
        public VisualIslandDeleted {
            islandId = islandId == null ? "" : islandId;
        }
    }

    record VisualHomeAssigned(
            VisualHomeAssignment assignment
    ) implements WorkflowEvent {
    }

    record VisualHomeCleared(
            ItemIdentity identity
    ) implements WorkflowEvent {
    }

    record TemplateIslandDismissed(
            String templateId
    ) implements WorkflowEvent {
        public TemplateIslandDismissed {
            templateId = templateId == null ? "" : templateId;
        }
    }

    record ClaimedChestCreated(
            ClaimedChest chest
    ) implements WorkflowEvent {
    }

    record ClaimedChestMoved(
            UUID storageId,
            int atlasX,
            int atlasY
    ) implements WorkflowEvent {
    }

    record ClaimedChestAnchorsChanged(
            UUID storageId,
            Set<ChestAnchor> anchors
    ) implements WorkflowEvent {
        public ClaimedChestAnchorsChanged {
            anchors = anchors == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(anchors));
        }
    }

    record ClaimedChestRelabeled(
            UUID storageId,
            String label
    ) implements WorkflowEvent {
        public ClaimedChestRelabeled {
            label = label == null ? "" : label;
        }
    }

    record ClaimedChestDeleted(
            UUID storageId
    ) implements WorkflowEvent {
    }

    record ChestLinkCreated(
            String islandId,
            UUID storageId
    ) implements WorkflowEvent {
        public ChestLinkCreated {
            islandId = islandId == null ? "" : islandId.trim();
        }
    }

    record ChestLinkRemoved(
            String islandId,
            UUID storageId
    ) implements WorkflowEvent {
        public ChestLinkRemoved {
            islandId = islandId == null ? "" : islandId.trim();
        }
    }

    record KitCreated(KitDefinition kit) implements WorkflowEvent {
    }

    record KitUpdated(KitDefinition kit) implements WorkflowEvent {
    }

    record KitDeleted(String kitId) implements WorkflowEvent {
        public KitDeleted {
            kitId = kitId == null ? "" : kitId;
        }
    }

    record KitActivated(String kitId, int pageIndex) implements WorkflowEvent {
        public KitActivated {
            kitId = kitId == null ? "" : kitId;
            pageIndex = Math.max(0, pageIndex);
        }
    }

    record KitDeactivated() implements WorkflowEvent {
    }

    record KitPageSwitched(int pageIndex) implements WorkflowEvent {
        public KitPageSwitched {
            pageIndex = Math.max(0, pageIndex);
        }
    }
}
