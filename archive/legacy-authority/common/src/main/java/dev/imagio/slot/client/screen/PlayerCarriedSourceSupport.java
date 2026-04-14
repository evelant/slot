package dev.imagio.slot.client.screen;

import dev.imagio.slot.inventory.kernel.ActionableSourcePolicy;
import dev.imagio.slot.inventory.kernel.MenuSlotResolver;

import java.util.List;

final class PlayerCarriedSourceSupport {
    private PlayerCarriedSourceSupport() {
    }

    static boolean sourceActionableInContext(InventoryScreenContext playerContext, String sourceId) {
        return policy(playerContext).sourceActionable(sourceId);
    }

    static List<Integer> transferTargetSlotsForContext(InventoryScreenContext playerContext, String sourceId) {
        return policy(playerContext).playerTransferTargets(sourceId);
    }

    private static ActionableSourcePolicy policy(InventoryScreenContext playerContext) {
        return new ActionableSourcePolicy(new MenuSlotResolver(
                playerContext == null ? null : playerContext.menu(),
                playerContext == null ? null : playerContext.layout()
        ));
    }
}
