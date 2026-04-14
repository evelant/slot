package dev.imagio.slot.client.screen.container;

import dev.imagio.slot.client.intent.CraftingIntent;
import dev.imagio.slot.client.intent.IntentRouter;
import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.client.policy.ItemBehaviorPolicy;
import net.minecraft.world.item.ItemStack;
import dev.imagio.slot.projection.InventoryPane;

public final class CraftingToolService {
    private static final PlacementRequester DEFAULT_REQUESTER = (containerId, targetMenuSlot, identity, sourcePane) ->
            targetMenuSlot != null
                    && targetMenuSlot.isValid()
                    && IntentRouter.route(CraftingIntent.PlaceOne.forCurrentSession(containerId, targetMenuSlot, identity, sourcePane));

    private CraftingToolService() {
    }

    public static PlacementRequestResult requestPlaceOne(
            int containerId,
            SlotBackedToolPanel toolPanel,
            double mouseX,
            double mouseY,
            ItemIdentity identity,
            ItemStack stack,
            InventoryPane sourcePane
    ) {
        return requestPlaceOne(containerId, toolPanel, mouseX, mouseY, identity, stack, sourcePane, DEFAULT_REQUESTER);
    }

    static PlacementRequestResult requestPlaceOne(
            int containerId,
            SlotBackedToolPanel toolPanel,
            double mouseX,
            double mouseY,
            ItemIdentity identity,
            ItemStack stack,
            InventoryPane sourcePane,
            PlacementRequester requester
    ) {
        if (containerId < 0 || toolPanel == null || identity == null || stack == null || stack.isEmpty() || sourcePane == null || requester == null) {
            return PlacementRequestResult.notRequested();
        }

        ToolSlotRef slotRef = toolPanel.slotRefAt(mouseX, mouseY);
        ItemStack existing = slotRef.stack();
        ItemIdentity existingIdentity = existing.isEmpty() ? null : ItemBehaviorPolicy.createIdentity(existing);
        CraftingPlacementPolicy.CraftingPlacementDecision decision = CraftingPlacementPolicy.evaluate(
                slotRef.menuSlotId(),
                slotRef.isResolved(),
                slotRef.region() == null ? null : slotRef.region().role(),
                toolPanel.acceptsPlacement(slotRef.displaySlotId(), stack),
                existingIdentity,
                identity
        );
        if (!decision.requested()) {
            return PlacementRequestResult.notRequested();
        }

        boolean requested = requester.requestPlaceOne(containerId, decision.targetMenuSlotId(), identity, sourcePane);
        return requested ? new PlacementRequestResult(true, slotRef) : PlacementRequestResult.notRequested();
    }

    @FunctionalInterface
    interface PlacementRequester {
        boolean requestPlaceOne(int containerId, MenuSlotId targetMenuSlot, ItemIdentity identity, InventoryPane sourcePane);
    }

    public record PlacementRequestResult(
            boolean requested,
            ToolSlotRef slotRef
    ) {
        private static final PlacementRequestResult NOT_REQUESTED = new PlacementRequestResult(false, ToolSlotRef.unresolved(-1));

        public static PlacementRequestResult notRequested() {
            return NOT_REQUESTED;
        }

        public MenuSlotId targetMenuSlotId() {
            return slotRef.menuSlotId();
        }
    }
}