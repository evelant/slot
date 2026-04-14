package dev.imagio.slot.inventory.intent;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.workflow.domain.ProtectionPolicy;

public sealed interface InventoryWorkflowIntent extends InventoryIntent permits
        InventoryWorkflowIntent.ToggleFavorite,
        InventoryWorkflowIntent.ToggleCollectionMembership,
        InventoryWorkflowIntent.SelectLoadout,
        InventoryWorkflowIntent.CaptureLoadout,
        InventoryWorkflowIntent.ApplyLoadout,
        InventoryWorkflowIntent.DismissRecent {

    @Override
    default InventoryIntentKind kind() {
        return InventoryIntentKind.WORKFLOW;
    }

    record ToggleFavorite(
            ItemIdentity identity,
            String origin
    ) implements InventoryWorkflowIntent {
        public ToggleFavorite {
            origin = origin == null ? "" : origin;
        }
    }

    record ToggleCollectionMembership(
            ItemIdentity identity,
            String collectionId,
            String origin
    ) implements InventoryWorkflowIntent {
        public ToggleCollectionMembership {
            collectionId = collectionId == null ? "" : collectionId;
            origin = origin == null ? "" : origin;
        }
    }

    record SelectLoadout(
            String collectionId,
            String loadoutId,
            String origin
    ) implements InventoryWorkflowIntent {
        public SelectLoadout {
            collectionId = collectionId == null ? "" : collectionId;
            loadoutId = loadoutId == null ? "" : loadoutId;
            origin = origin == null ? "" : origin;
        }
    }

    record CaptureLoadout(
            String collectionId,
            String loadoutName,
            String origin
    ) implements InventoryWorkflowIntent {
        public CaptureLoadout {
            collectionId = collectionId == null ? "" : collectionId;
            loadoutName = loadoutName == null ? "" : loadoutName;
            origin = origin == null ? "" : origin;
        }
    }

    record ApplyLoadout(
            String collectionId,
            String loadoutId,
            ProtectionPolicy protectionPolicy,
            String origin
    ) implements InventoryWorkflowIntent {
        public ApplyLoadout {
            collectionId = collectionId == null ? "" : collectionId;
            loadoutId = loadoutId == null ? "" : loadoutId;
            protectionPolicy = protectionPolicy == null ? ProtectionPolicy.allowAll() : protectionPolicy;
            origin = origin == null ? "" : origin;
        }
    }

    record DismissRecent(
            ItemIdentity identity,
            String origin
    ) implements InventoryWorkflowIntent {
        public DismissRecent {
            origin = origin == null ? "" : origin;
        }
    }
}
