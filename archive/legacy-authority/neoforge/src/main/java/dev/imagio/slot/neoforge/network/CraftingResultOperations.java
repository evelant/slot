package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.client.policy.ItemBehaviorPolicy;
import dev.imagio.slot.network.CraftingGridActionRequests;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

final class CraftingResultOperations {
    private CraftingResultOperations() {
    }

    static ExtractionResult extractResult(
            AbstractContainerMenu menu,
            ServerPlayer player,
            int resultMenuSlot,
            CraftingGridActionRequests.ResultAction resultAction,
            int mouseButton,
            int repeatCount
    ) {
        if (menu == null || player == null || resultAction == null || repeatCount <= 0) {
            return ExtractionResult.none();
        }

        Slot resultSlot = CraftingGridPlacementOperations.resolveMenuSlot(menu, resultMenuSlot);
        if (resultSlot == null) {
            return ExtractionResult.none();
        }

        int extractedCount = 0;
        int successfulAttempts = 0;
        String acquiredItemId = "";
        for (int attempt = 0; attempt < repeatCount; attempt++) {
            ItemStack beforeResult = resultSlot.getItem().copy();
            if (beforeResult.isEmpty()) {
                break;
            }

            ItemIdentity extractedIdentity = ItemBehaviorPolicy.createIdentity(beforeResult);
            int beforeCount = countMatchingItems(menu, extractedIdentity);
            menu.clicked(resultMenuSlot, mouseButton, clickType(resultAction), player);
            int afterCount = countMatchingItems(menu, extractedIdentity);
            int gainedCount = Math.max(0, afterCount - beforeCount);
            if (gainedCount <= 0) {
                break;
            }

            extractedCount += gainedCount;
            successfulAttempts++;
            if (acquiredItemId.isBlank() && extractedIdentity != null) {
                acquiredItemId = extractedIdentity.itemId();
            }
        }

        SlotDebugLog.log(
                "Craft result extraction: resultMenuSlot={} action={} mouseButton={} repeats={} successfulAttempts={} extractedCount={}",
                resultMenuSlot,
                resultAction,
                mouseButton,
                repeatCount,
                successfulAttempts,
                extractedCount
        );
        return extractedCount <= 0
                ? ExtractionResult.none()
                : new ExtractionResult(extractedCount, acquiredItemId);
    }

    private static ClickType clickType(CraftingGridActionRequests.ResultAction resultAction) {
        return resultAction == CraftingGridActionRequests.ResultAction.QUICK_MOVE
                ? ClickType.QUICK_MOVE
                : ClickType.PICKUP;
    }

    private static int countMatchingItems(AbstractContainerMenu menu, ItemIdentity identity) {
        if (menu == null || identity == null) {
            return 0;
        }

        int totalCount = 0;
        for (Slot slot : menu.slots) {
            if (slot != null && slot.hasItem() && ItemBehaviorPolicy.matchesMovableIdentity(slot.getItem(), identity)) {
                totalCount += slot.getItem().getCount();
            }
        }

        ItemStack carried = menu.getCarried();
        if (!carried.isEmpty() && ItemBehaviorPolicy.matchesMovableIdentity(carried, identity)) {
            totalCount += carried.getCount();
        }
        return totalCount;
    }

    record ExtractionResult(int extractedCount, String acquiredItemId) {
        private static ExtractionResult none() {
            return new ExtractionResult(0, "");
        }
    }
}
