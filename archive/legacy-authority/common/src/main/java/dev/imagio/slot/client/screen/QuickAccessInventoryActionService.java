package dev.imagio.slot.client.screen;

import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.client.policy.ItemBehaviorPolicy;
import dev.imagio.slot.inventory.kernel.MenuSlotResolver;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

final class QuickAccessInventoryActionService {
    private final QuickAccessSupport support;

    QuickAccessInventoryActionService(QuickAccessSupport support) {
        this.support = support;
    }

    boolean canUseFromInventory(ItemIdentity identity) {
        return canUseFromInventory(identity, Set.of());
    }

    boolean canUseFromInventory(ItemIdentity identity, Set<String> preferredSourceIds) {
        if (identity == null) {
            return false;
        }
        if (QuickAccessFollowUpState.hasPendingIdentity(identity)) {
            return false;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        MultiPlayerGameMode gameMode = minecraft.gameMode;
        if (player == null || gameMode == null) {
            return false;
        }

        AbstractContainerMenu menu = support.activeMenu(player);
        if (!menu.getCarried().isEmpty()) {
            return false;
        }

        if (ItemBehaviorPolicy.matchesMovableIdentity(player.getOffhandItem(), identity)) {
            return support.supportsInstantUse(player.getOffhandItem(), player);
        }

        ItemStack mainHandStack = player.getMainHandItem();
        if (ItemBehaviorPolicy.matchesMovableIdentity(mainHandStack, identity)) {
            return support.supportsInstantUse(mainHandStack, player);
        }

        Integer sourceMenuSlot = support.findMatchingSourceMenuSlot(menu, -1, identity, support.candidateSourceMenuSlots(preferredSourceIds));
        if (sourceMenuSlot == null) {
            ItemStack previewStack = previewBackpackStack(player, identity, preferredSourceIds);
            return !previewStack.isEmpty()
                    && support.supportsInstantUse(previewStack, player);
        }

        var sourceSlot = MenuSlotResolver.safeSlot(menu, sourceMenuSlot);
        if (sourceSlot == null) {
            return false;
        }
        ItemStack sourceStack = sourceSlot.getItem();
        if (!support.supportsInstantUse(sourceStack, player)) {
            return false;
        }

        ItemStack offhandStack = player.getOffhandItem();
        return offhandStack.isEmpty()
                || support.findEmptyTemporaryMenuSlot(menu, sourceStack, offhandStack, Set.of(sourceMenuSlot)) != null;
    }

    QuickAccessInventoryActionResult useFromInventoryAction(ItemIdentity identity) {
        return useFromInventoryAction(identity, Set.of());
    }

    QuickAccessInventoryActionResult useFromInventoryAction(ItemIdentity identity, Set<String> preferredSourceIds) {
        if (identity == null) {
            return QuickAccessInventoryActionResult.NONE;
        }
        if (QuickAccessFollowUpState.hasPendingIdentity(identity)) {
            return QuickAccessInventoryActionResult.NONE;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        MultiPlayerGameMode gameMode = minecraft.gameMode;
        if (player == null || gameMode == null) {
            return QuickAccessInventoryActionResult.NONE;
        }

        AbstractContainerMenu menu = support.activeMenu(player);
        if (!menu.getCarried().isEmpty()) {
            return QuickAccessInventoryActionResult.NONE;
        }

        if (ItemBehaviorPolicy.matchesMovableIdentity(player.getOffhandItem(), identity)) {
            return support.useHand(player, gameMode, InteractionHand.OFF_HAND)
                    ? QuickAccessInventoryActionResult.started(
                    QuickAccessInventoryActionFeedback.applied(QuickAccessFollowUpState.ActionType.USE_HAND)
            )
                    : QuickAccessInventoryActionResult.NONE;
        }

        if (ItemBehaviorPolicy.matchesMovableIdentity(player.getMainHandItem(), identity)) {
            return support.useHand(player, gameMode, InteractionHand.MAIN_HAND)
                    ? QuickAccessInventoryActionResult.started(
                    QuickAccessInventoryActionFeedback.applied(QuickAccessFollowUpState.ActionType.USE_HAND)
            )
                    : QuickAccessInventoryActionResult.NONE;
        }

        Integer sourceMenuSlot = support.findMatchingSourceMenuSlot(menu, -1, identity, support.candidateSourceMenuSlots(preferredSourceIds));
        if (sourceMenuSlot == null) {
            ItemStack previewStack = previewBackpackStack(player, identity, preferredSourceIds);
            if (previewStack.isEmpty() || !support.supportsInstantUse(previewStack, player)) {
                return QuickAccessInventoryActionResult.NONE;
            }

            var requestId = support.requestBackpackToOffhandReplacingTargetId(menu, player, gameMode, identity, preferredSourceIds);
            if (requestId.present()) {
                support.queueBackpackUseOffhand(requestId, identity);
                return QuickAccessInventoryActionResult.started(
                        QuickAccessInventoryActionFeedback.requested(QuickAccessFollowUpState.ActionType.USE_OFFHAND)
                );
            }
            return QuickAccessInventoryActionResult.NONE;
        }

        var sourceSlot = MenuSlotResolver.safeSlot(menu, sourceMenuSlot);
        if (sourceSlot == null) {
            return QuickAccessInventoryActionResult.NONE;
        }
        ItemStack sourceStack = sourceSlot.getItem();
        if (!support.supportsInstantUse(sourceStack, player)) {
            return QuickAccessInventoryActionResult.NONE;
        }

        Integer stageMenuSlot = null;
        if (!player.getOffhandItem().isEmpty()) {
            stageMenuSlot = support.findEmptyTemporaryMenuSlot(menu, sourceStack, player.getOffhandItem(), Set.of(sourceMenuSlot));
            if (stageMenuSlot == null || !support.swapMenuSlotWithOffhand(menu, player, gameMode, stageMenuSlot)) {
                return QuickAccessInventoryActionResult.NONE;
            }
        }

        if (!support.moveSourceMenuSlotToOffhand(menu, player, gameMode, sourceMenuSlot)) {
            if (stageMenuSlot != null) {
                support.swapMenuSlotWithOffhand(menu, player, gameMode, stageMenuSlot);
            }
            return QuickAccessInventoryActionResult.NONE;
        }

        boolean used = support.useHand(player, gameMode, InteractionHand.OFF_HAND);
        support.restoreTemporaryOffhandState(menu, player, gameMode, sourceMenuSlot, stageMenuSlot);
        return used
                ? QuickAccessInventoryActionResult.started(
                QuickAccessInventoryActionFeedback.applied(QuickAccessFollowUpState.ActionType.USE_HAND)
        )
                : QuickAccessInventoryActionResult.NONE;
    }

    boolean canDropFromInventory(ItemIdentity identity) {
        return canDropFromInventory(identity, Set.of());
    }

    boolean canDropFromInventory(ItemIdentity identity, Set<String> preferredSourceIds) {
        if (identity == null) {
            return false;
        }
        if (QuickAccessFollowUpState.hasPendingIdentity(identity)) {
            return false;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        MultiPlayerGameMode gameMode = minecraft.gameMode;
        if (player == null || gameMode == null) {
            return false;
        }

        AbstractContainerMenu menu = support.activeMenu(player);
        if (!menu.getCarried().isEmpty()) {
            return false;
        }

        if (ItemBehaviorPolicy.matchesMovableIdentity(player.getOffhandItem(), identity)) {
            return support.findEmptyTemporaryMenuSlot(menu, player.getOffhandItem(), ItemStack.EMPTY, Set.of()) != null;
        }

        Integer sourceMenuSlot = support.findMatchingSourceMenuSlot(menu, -1, identity, support.candidateSourceMenuSlots(preferredSourceIds));
        if (sourceMenuSlot != null) {
            return true;
        }

        ItemStack backpackStack = support.findMatchingBackpackStack(player, identity, preferredSourceIds);
        ItemStack previewStack = backpackStack.isEmpty()
                ? ItemBehaviorPolicy.approximateDisplayStack(identity)
                : backpackStack;
        return !previewStack.isEmpty()
                && support.findEmptyTemporaryMenuSlot(menu, previewStack, ItemStack.EMPTY, Set.of()) != null;
    }

    QuickAccessInventoryActionResult dropFromInventoryAction(ItemIdentity identity) {
        return dropFromInventoryAction(identity, Set.of());
    }

    QuickAccessInventoryActionResult dropFromInventoryAction(ItemIdentity identity, Set<String> preferredSourceIds) {
        if (identity == null) {
            return QuickAccessInventoryActionResult.NONE;
        }
        if (QuickAccessFollowUpState.hasPendingIdentity(identity)) {
            return QuickAccessInventoryActionResult.NONE;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        MultiPlayerGameMode gameMode = minecraft.gameMode;
        if (player == null || gameMode == null) {
            return QuickAccessInventoryActionResult.NONE;
        }

        AbstractContainerMenu menu = support.activeMenu(player);
        if (!menu.getCarried().isEmpty()) {
            return QuickAccessInventoryActionResult.NONE;
        }

        if (ItemBehaviorPolicy.matchesMovableIdentity(player.getOffhandItem(), identity)) {
            ItemStack offhandStack = player.getOffhandItem();
            Integer stagingSlot = support.findEmptyTemporaryMenuSlot(menu, offhandStack, ItemStack.EMPTY, Set.of());
            if (stagingSlot == null || !support.swapMenuSlotWithOffhand(menu, player, gameMode, stagingSlot)) {
                return QuickAccessInventoryActionResult.NONE;
            }

            boolean dropped = support.throwMenuSlot(menu, player, gameMode, stagingSlot, true);
            if (!dropped) {
                support.swapMenuSlotWithOffhand(menu, player, gameMode, stagingSlot);
            }
            return dropped
                    ? QuickAccessInventoryActionResult.started(
                    QuickAccessInventoryActionFeedback.applied(QuickAccessFollowUpState.ActionType.DROP_MENU_SLOT)
            )
                    : QuickAccessInventoryActionResult.NONE;
        }

        Integer sourceMenuSlot = support.findMatchingSourceMenuSlot(menu, -1, identity, support.candidateSourceMenuSlots(preferredSourceIds));
        if (sourceMenuSlot != null) {
            return support.throwMenuSlot(menu, player, gameMode, sourceMenuSlot, true)
                    ? QuickAccessInventoryActionResult.started(
                    QuickAccessInventoryActionFeedback.applied(QuickAccessFollowUpState.ActionType.DROP_MENU_SLOT)
            )
                    : QuickAccessInventoryActionResult.NONE;
        }

        ItemStack previewStack = previewBackpackStack(player, identity, preferredSourceIds);
        Integer stagingSlot = previewStack.isEmpty()
                ? null
                : support.findEmptyTemporaryMenuSlot(menu, previewStack, ItemStack.EMPTY, Set.of());
        if (stagingSlot == null) {
            return QuickAccessInventoryActionResult.NONE;
        }

        var requestId = support.requestBackpackToMenuSlotId(menu, player, gameMode, identity, stagingSlot, preferredSourceIds);
        if (requestId.present()) {
            support.queueBackpackDropMenuSlot(requestId, identity, stagingSlot);
            return QuickAccessInventoryActionResult.started(
                    QuickAccessInventoryActionFeedback.requested(QuickAccessFollowUpState.ActionType.DROP_MENU_SLOT)
            );
        }
        return QuickAccessInventoryActionResult.NONE;
    }

    boolean useFromInventory(ItemIdentity identity) {
        return useFromInventoryAction(identity).started();
    }

    boolean useFromInventory(ItemIdentity identity, Set<String> preferredSourceIds) {
        return useFromInventoryAction(identity, preferredSourceIds).started();
    }

    boolean dropFromInventory(ItemIdentity identity) {
        return dropFromInventoryAction(identity).started();
    }

    boolean dropFromInventory(ItemIdentity identity, Set<String> preferredSourceIds) {
        return dropFromInventoryAction(identity, preferredSourceIds).started();
    }

    private ItemStack previewBackpackStack(LocalPlayer player, ItemIdentity identity, Set<String> preferredSourceIds) {
        ItemStack backpackStack = support.findMatchingBackpackStack(player, identity, preferredSourceIds);
        if (!backpackStack.isEmpty()) {
            return backpackStack;
        }
        return ItemBehaviorPolicy.approximateDisplayStack(identity);
    }
}
