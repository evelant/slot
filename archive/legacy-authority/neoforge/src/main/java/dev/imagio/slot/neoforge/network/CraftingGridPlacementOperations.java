package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.client.policy.ItemBehaviorPolicy;
import dev.imagio.slot.session.ChestLikeMenuLayout;
import dev.imagio.slot.compat.sophisticated.SophisticatedBackpackTransferSupport;
import dev.imagio.slot.operation.CraftingCursorDistributionPlanner;
import dev.imagio.slot.storage.adapter.StorageTransferMode;
import dev.imagio.slot.storage.provider.StorageViewProviderSession;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import dev.imagio.slot.projection.InventoryPane;

final class CraftingGridPlacementOperations {
    private CraftingGridPlacementOperations() {
    }

    static int placeFromOpenContainer(
            ServerPlayer player,
            AbstractContainerMenu menu,
            ChestLikeMenuLayout layout,
            Slot targetSlot,
            int targetMenuSlot,
            ItemIdentity identity
    ) {
        int movedFromMenu = moveFromMenuSources(player, menu, layout, targetSlot, targetMenuSlot, identity, layout.actionSourceIdsForPane(dev.imagio.slot.projection.InventoryPane.OPEN_CONTAINER));
        if (movedFromMenu > 0) {
            return movedFromMenu;
        }

        StorageViewProviderSession session = layout.primaryStorageSession();
        ItemStack moving = ExternalStorageExtractionSupport.extractMatchingIdentity(
                menu,
                player,
                session,
                identity,
                StorageTransferMode.ONE
        );
        if (moving.isEmpty()) {
            SlotDebugLog.log(
                    "Craft grid place failed to extract from external storage: provider={} identity={} targetMenuSlot={}",
                    session.providerId(),
                    identity.itemId(),
                    targetMenuSlot
            );
            return 0;
        }

        int originalMovingCount = moving.getCount();
        ItemStack remainder = targetSlot.safeInsert(moving);
        int movedCount = originalMovingCount - remainder.getCount();
        if (!remainder.isEmpty()) {
            session.insertIntoPrimary(menu, player, remainder);
        }
        SlotDebugLog.log(
                "Craft grid place from external storage: provider={} identity={} targetMenuSlot={} moved={}",
                session.providerId(),
                identity.itemId(),
                targetMenuSlot,
                movedCount
        );
        return movedCount;
    }

    static int placeFromCarriedSources(
            ServerPlayer player,
            AbstractContainerMenu menu,
            ChestLikeMenuLayout layout,
            Slot targetSlot,
            int targetMenuSlot,
            ItemIdentity identity,
            Map<UUID, CompoundTag> syncedContents
    ) {
        int movedFromMenu = moveFromMenuSources(player, menu, layout, targetSlot, targetMenuSlot, identity, layout.actionSourceIdsForPane(dev.imagio.slot.projection.InventoryPane.CARRIED));
        if (movedFromMenu > 0) {
            return movedFromMenu;
        }

        int[] movedCount = new int[]{0};
        boolean moved = SophisticatedBackpackTransferSupport.moveMatchingBackpackStack(
                player,
                identity,
                1,
                stack -> {
                    int originalCount = stack.getCount();
                    ItemStack remainder = targetSlot.safeInsert(stack);
                    movedCount[0] += Math.max(0, originalCount - remainder.getCount());
                    return remainder;
                },
                syncedContents
        );
        SlotDebugLog.log(
                "Craft grid place from backpacks: identity={} targetMenuSlot={} moved={} movedCount={}",
                identity.itemId(),
                targetMenuSlot,
                moved,
                movedCount[0]
        );
        return movedCount[0];
    }

    static int placeFromCursorCarried(
            AbstractContainerMenu menu,
            Slot targetSlot,
            int targetMenuSlot,
            boolean singleItem
    ) {
        ItemStack carried = menu.getCarried();
        if (carried.isEmpty()) {
            return 0;
        }

        ItemStack moving = carried.copy();
        if (singleItem) {
            moving.setCount(1);
        }

        int originalMovingCount = moving.getCount();
        ItemStack remainder = targetSlot.safeInsert(moving);
        int movedCount = originalMovingCount - remainder.getCount();
        if (movedCount <= 0) {
            return 0;
        }

        if (singleItem) {
            ItemStack updatedCarried = carried.copy();
            updatedCarried.shrink(movedCount);
            menu.setCarried(updatedCarried);
        } else {
            menu.setCarried(remainder);
        }

        SlotDebugLog.log(
                "Craft grid place from cursor: targetMenuSlot={} moved={} mode={}",
                targetMenuSlot,
                movedCount,
                singleItem ? "one" : "stack"
        );
        return movedCount;
    }

    static int distributeFromCursorCarried(
            AbstractContainerMenu menu,
            List<Integer> targetMenuSlots,
            boolean singleItem
    ) {
        if (menu == null || targetMenuSlots == null || targetMenuSlots.isEmpty()) {
            return 0;
        }

        ItemStack carried = menu.getCarried();
        if (carried.isEmpty()) {
            return 0;
        }

        List<CursorDistributionTarget> eligibleTargets = new ArrayList<>(targetMenuSlots.size());
        for (int targetMenuSlot : targetMenuSlots) {
            Slot slot = resolveMenuSlot(menu, targetMenuSlot);
            if (slot == null
                    || !AbstractContainerMenu.canItemQuickReplace(slot, carried, true)
                    || !slot.mayPlace(carried)
                    || !menu.canDragTo(slot)) {
                continue;
            }

            int maxCount = Math.min(carried.getMaxStackSize(), slot.getMaxStackSize(carried));
            eligibleTargets.add(new CursorDistributionTarget(
                    targetMenuSlot,
                    slot,
                    slot.hasItem() ? slot.getItem().getCount() : 0,
                    maxCount
            ));
        }

        if (eligibleTargets.isEmpty()) {
            return 0;
        }
        if (eligibleTargets.size() == 1) {
            CursorDistributionTarget singleTarget = eligibleTargets.getFirst();
            return placeFromCursorCarried(menu, singleTarget.slot(), singleTarget.menuSlotId(), singleItem);
        }

        CraftingCursorDistributionPlanner.Plan plan = CraftingCursorDistributionPlanner.plan(
                carried.getCount(),
                singleItem ? CraftingCursorDistributionPlanner.Mode.ONE : CraftingCursorDistributionPlanner.Mode.STACK,
                eligibleTargets.stream()
                        .map(target -> new CraftingCursorDistributionPlanner.Target(target.existingCount(), target.maxCount()))
                        .toList()
        );

        int movedCount = 0;
        for (int index = 0; index < eligibleTargets.size(); index++) {
            CraftingCursorDistributionPlanner.Allocation allocation = plan.allocations().get(index);
            if (allocation == null || allocation.placedCount() <= 0) {
                continue;
            }

            CursorDistributionTarget target = eligibleTargets.get(index);
            target.slot().setByPlayer(carried.copyWithCount(allocation.resultingCount()));
            target.slot().setChanged();
            movedCount += allocation.placedCount();
        }

        if (movedCount <= 0) {
            return 0;
        }

        if (plan.remainingCount() <= 0) {
            menu.setCarried(ItemStack.EMPTY);
        } else {
            ItemStack updatedCarried = carried.copy();
            updatedCarried.setCount(plan.remainingCount());
            menu.setCarried(updatedCarried);
        }

        SlotDebugLog.log(
                "Craft grid drag distribute from cursor: targets={} moved={} remaining={} mode={}",
                eligibleTargets.stream().map(CursorDistributionTarget::menuSlotId).toList(),
                movedCount,
                plan.remainingCount(),
                singleItem ? "one" : "stack"
        );
        return movedCount;
    }

    static Slot resolveMenuSlot(AbstractContainerMenu menu, int slotId) {
        if (menu == null || slotId < 0) {
            return null;
        }
        try {
            Slot slot = menu.getSlot(slotId);
            return slot;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static int moveFromMenuSources(
            ServerPlayer player,
            AbstractContainerMenu menu,
            ChestLikeMenuLayout layout,
            Slot targetSlot,
            int targetMenuSlot,
            ItemIdentity identity,
            Set<String> sourceIds
    ) {
        for (String sourceId : sourceIds) {
            for (int menuSlot : layout.menuSlotsForSource(sourceId)) {
                if (menuSlot == targetMenuSlot) {
                    continue;
                }

                Slot sourceSlot = resolveMenuSlot(menu, menuSlot);
                if (sourceSlot == null || !sourceSlot.hasItem() || !sourceSlot.mayPickup(player)) {
                    continue;
                }

                ItemStack sourceStack = sourceSlot.getItem();
                if (!ItemBehaviorPolicy.matchesMovableIdentity(sourceStack, identity)) {
                    continue;
                }

                ItemStack moving = sourceSlot.safeTake(1, sourceStack.getCount(), player);
                if (moving.isEmpty()) {
                    continue;
                }

                int originalMovingCount = moving.getCount();
                ItemStack remainder = targetSlot.safeInsert(moving);
                int movedCount = originalMovingCount - remainder.getCount();
                if (!remainder.isEmpty()) {
                    sourceSlot.safeInsert(remainder);
                }

                SlotDebugLog.log(
                        "Craft grid place from menu source: sourceId={} sourceMenuSlot={} targetMenuSlot={} identity={} moved={}",
                        sourceId,
                        menuSlot,
                        targetMenuSlot,
                        identity.itemId(),
                        movedCount
                );
                if (movedCount > 0) {
                    return movedCount;
                }
            }
        }
        return 0;
    }

    private record CursorDistributionTarget(
            int menuSlotId,
            Slot slot,
            int existingCount,
            int maxCount
    ) {
    }
}
