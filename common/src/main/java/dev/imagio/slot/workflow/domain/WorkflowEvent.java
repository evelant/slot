package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.LinkedHashSet;
import java.util.Set;

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
        WorkflowEvent.RecentDismissedUpTo {

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
}
