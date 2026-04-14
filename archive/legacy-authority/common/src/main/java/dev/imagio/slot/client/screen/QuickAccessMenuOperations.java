package dev.imagio.slot.client.screen;

import dev.imagio.slot.inventory.kernel.MenuInteractionExecutor;
import dev.imagio.slot.inventory.kernel.MenuSlotResolver;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

final class QuickAccessMenuOperations {
    private final QuickAccessMenuSlotPlanner slotPlanner;

    QuickAccessMenuOperations(QuickAccessMenuSlotPlanner slotPlanner) {
        this.slotPlanner = slotPlanner;
    }

    Integer findEmptyTemporaryMenuSlot(
            AbstractContainerMenu menu,
            ItemStack primaryStack,
            ItemStack secondaryStack,
            Set<Integer> excludedSlots
    ) {
        return interactionExecutor(menu).findEmptyTemporaryMenuSlot(menu, primaryStack, secondaryStack, excludedSlots);
    }

    boolean moveSourceMenuSlotToTarget(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode,
            int sourceMenuSlot,
            int targetMenuSlot
    ) {
        return interactionExecutor(menu).moveSourceMenuSlotToTarget(menu, player, gameMode, sourceMenuSlot, targetMenuSlot);
    }

    boolean moveSourceMenuSlotToOffhand(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode,
            int sourceMenuSlot
    ) {
        return interactionExecutor(menu).moveSourceMenuSlotToOffhand(menu, player, gameMode, sourceMenuSlot);
    }

    boolean clearTargetSlot(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode,
            int targetMenuSlot
    ) {
        return interactionExecutor(menu).clearTargetSlot(menu, player, gameMode, targetMenuSlot);
    }

    boolean clearOffhandSlot(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode
    ) {
        return interactionExecutor(menu).clearOffhandSlot(menu, player, gameMode);
    }

    boolean quickMoveMenuSlot(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode,
            int menuSlot
    ) {
        return interactionExecutor(menu).quickMoveMenuSlot(menu, player, gameMode, menuSlot);
    }

    boolean throwMenuSlot(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode,
            int menuSlot,
            boolean wholeStack
    ) {
        return interactionExecutor(menu).throwMenuSlot(menu, player, gameMode, menuSlot, wholeStack);
    }

    boolean clickOffhandSlot(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode,
            int mouseButton
    ) {
        return interactionExecutor(menu).clickOffhandSlot(menu, player, gameMode, mouseButton);
    }

    boolean swapMenuSlotWithOffhand(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode,
            int menuSlot
    ) {
        return interactionExecutor(menu).swapMenuSlotWithOffhand(menu, player, gameMode, menuSlot);
    }

    boolean useHand(LocalPlayer player, MultiPlayerGameMode gameMode, InteractionHand hand) {
        return interactionExecutor(slotPlanner.activeMenu(player)).useHand(player, gameMode, hand);
    }

    boolean supportsInstantUse(ItemStack stack, LocalPlayer player) {
        return interactionExecutor(slotPlanner.activeMenu(player)).supportsInstantUse(stack, player);
    }

    void restoreTemporaryOffhandState(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode,
            int sourceMenuSlot,
            Integer stageMenuSlot
    ) {
        interactionExecutor(menu).restoreTemporaryOffhandState(menu, player, gameMode, sourceMenuSlot, stageMenuSlot);
    }

    boolean canClickTargetSlot(Slot targetSlot, LocalPlayer player, ItemStack carriedStack) {
        return interactionExecutor(slotPlanner.activeMenu(player)).canClickTargetSlot(targetSlot, player, carriedStack);
    }

    private MenuInteractionExecutor interactionExecutor(AbstractContainerMenu menu) {
        return new MenuInteractionExecutor(new MenuSlotResolver(menu, slotPlanner.currentLayout()));
    }
}
