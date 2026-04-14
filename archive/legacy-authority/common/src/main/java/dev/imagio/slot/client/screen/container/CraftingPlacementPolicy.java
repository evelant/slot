package dev.imagio.slot.client.screen.container;

import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.client.policy.ItemBehaviorPolicy;
import dev.imagio.slot.storage.adapter.ExternalToolSlotRole;

final class CraftingPlacementPolicy {
    private CraftingPlacementPolicy() {
    }

    static CraftingPlacementDecision evaluate(
            MenuSlotId targetMenuSlotId,
            boolean resolved,
            ExternalToolSlotRole slotRole,
            boolean acceptsPlacement,
            ItemIdentity existingIdentity,
            ItemIdentity requestedIdentity
    ) {
        if (!resolved
                || targetMenuSlotId == null
                || !targetMenuSlotId.isValid()
                || slotRole != ExternalToolSlotRole.INPUT
                || !acceptsPlacement
                || requestedIdentity == null) {
            return CraftingPlacementDecision.notRequested();
        }

        if (existingIdentity != null && !ItemBehaviorPolicy.matchesMovableIdentity(existingIdentity, requestedIdentity)) {
            return CraftingPlacementDecision.notRequested();
        }

        return new CraftingPlacementDecision(true, targetMenuSlotId);
    }

    record CraftingPlacementDecision(
            boolean requested,
            MenuSlotId targetMenuSlotId
    ) {
        private static final CraftingPlacementDecision NOT_REQUESTED = new CraftingPlacementDecision(false, MenuSlotId.INVALID);

        static CraftingPlacementDecision notRequested() {
            return NOT_REQUESTED;
        }
    }
}
