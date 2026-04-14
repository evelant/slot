package dev.imagio.slot.inventory.kernel;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

public final class MenuInteractionExecutor {
    private static final int OFFHAND_SWAP_BUTTON = 40;

    private final MenuSlotResolver resolver;

    public MenuInteractionExecutor(MenuSlotResolver resolver) {
        this.resolver = resolver;
    }

    public Integer findEmptyTemporaryMenuSlot(
            AbstractContainerMenu menu,
            ItemStack primaryStack,
            ItemStack secondaryStack,
            Set<Integer> excludedSlots
    ) {
        return dev.imagio.slot.inventory.CarriedPlacementPolicy.findEmptyCarriedMenuSlot(
                menu,
                resolver == null ? null : resolver.layout(),
                dev.imagio.slot.inventory.CarriedPlacementPolicy.Intent.TEMPORARY,
                excludedSlots,
                primaryStack,
                secondaryStack
        );
    }

    public boolean moveOne(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode,
            int sourceMenuSlot,
            int targetMenuSlot
    ) {
        if (!hasSlots(menu, sourceMenuSlot, targetMenuSlot) || !menu.getCarried().isEmpty()) {
            return false;
        }

        ItemStack sourceBefore = menu.getSlot(sourceMenuSlot).getItem().copy();
        ItemStack targetBefore = menu.getSlot(targetMenuSlot).getItem().copy();
        gameMode.handleInventoryMouseClick(menu.containerId, sourceMenuSlot, 0, ClickType.PICKUP, player);
        gameMode.handleInventoryMouseClick(menu.containerId, targetMenuSlot, 1, ClickType.PICKUP, player);
        if (!menu.getCarried().isEmpty()) {
            gameMode.handleInventoryMouseClick(menu.containerId, sourceMenuSlot, 0, ClickType.PICKUP, player);
        }

        Slot sourceSlot = menu.getSlot(sourceMenuSlot);
        Slot targetSlot = menu.getSlot(targetMenuSlot);
        return !ItemStack.matches(sourceBefore, sourceSlot.getItem())
                || !ItemStack.matches(targetBefore, targetSlot.getItem());
    }

    public boolean moveSourceMenuSlotToTarget(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode,
            int sourceMenuSlot,
            int targetMenuSlot
    ) {
        if (sourceMenuSlot == targetMenuSlot || !hasSlots(menu, sourceMenuSlot, targetMenuSlot) || !menu.getCarried().isEmpty()) {
            return false;
        }

        Slot sourceSlot = menu.getSlot(sourceMenuSlot);
        Slot targetSlot = menu.getSlot(targetMenuSlot);
        ItemStack sourceBefore = sourceSlot.getItem().copy();
        if (!sourceSlot.hasItem() || !sourceSlot.mayPickup(player)) {
            return false;
        }
        if (targetSlot.hasItem() && !clearTargetSlot(menu, player, gameMode, targetMenuSlot)) {
            return false;
        }

        targetSlot = menu.getSlot(targetMenuSlot);
        if (targetSlot.hasItem() || !targetSlot.mayPlace(sourceBefore)) {
            return false;
        }

        gameMode.handleInventoryMouseClick(menu.containerId, sourceMenuSlot, 0, ClickType.PICKUP, player);
        if (menu.getCarried().isEmpty()) {
            return false;
        }

        gameMode.handleInventoryMouseClick(menu.containerId, targetMenuSlot, 0, ClickType.PICKUP, player);
        if (!menu.getCarried().isEmpty()) {
            gameMode.handleInventoryMouseClick(menu.containerId, sourceMenuSlot, 0, ClickType.PICKUP, player);
        }

        Slot updatedTargetSlot = menu.getSlot(targetMenuSlot);
        return menu.getCarried().isEmpty() && ItemStack.matches(updatedTargetSlot.getItem(), sourceBefore);
    }

    public boolean moveSourceMenuSlotToOffhand(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode,
            int sourceMenuSlot
    ) {
        if (resolver.safeSlot(sourceMenuSlot) == null || !menu.getCarried().isEmpty()) {
            return false;
        }

        Slot sourceSlot = menu.getSlot(sourceMenuSlot);
        ItemStack sourceBefore = sourceSlot.getItem().copy();
        if (!sourceSlot.hasItem() || !sourceSlot.mayPickup(player)) {
            return false;
        }

        ItemStack offhandBefore = player.getOffhandItem().copy();
        if (!swapMenuSlotWithOffhand(menu, player, gameMode, sourceMenuSlot)) {
            return false;
        }

        return ItemStack.matches(player.getOffhandItem(), sourceBefore)
                && !ItemStack.matches(offhandBefore, player.getOffhandItem());
    }

    public boolean clearTargetSlot(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode,
            int targetMenuSlot
    ) {
        Slot targetSlot = resolver.safeSlot(targetMenuSlot);
        if (targetSlot == null) {
            return false;
        }
        if (!targetSlot.hasItem()) {
            return true;
        }
        if (!targetSlot.mayPickup(player)) {
            return false;
        }

        ItemStack beforeTarget = targetSlot.getItem().copy();
        if (quickMoveMenuSlot(menu, player, gameMode, targetMenuSlot) && !menu.getSlot(targetMenuSlot).hasItem()) {
            return true;
        }

        Integer destinationSlot = dev.imagio.slot.inventory.CarriedPlacementPolicy.findBestCarriedMenuSlot(
                menu,
                resolver == null ? null : resolver.layout(),
                beforeTarget,
                dev.imagio.slot.inventory.CarriedPlacementPolicy.Intent.STASH,
                Set.of(targetMenuSlot)
        );
        if (destinationSlot == null) {
            return false;
        }

        gameMode.handleInventoryMouseClick(menu.containerId, targetMenuSlot, 0, ClickType.PICKUP, player);
        if (menu.getCarried().isEmpty()) {
            return false;
        }

        gameMode.handleInventoryMouseClick(menu.containerId, destinationSlot, 0, ClickType.PICKUP, player);
        if (!menu.getCarried().isEmpty()) {
            gameMode.handleInventoryMouseClick(menu.containerId, targetMenuSlot, 0, ClickType.PICKUP, player);
        }

        return menu.getCarried().isEmpty() && menu.getSlot(targetMenuSlot).getItem().isEmpty();
    }

    public boolean clearOffhandSlot(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode
    ) {
        ItemStack offhandStack = player.getOffhandItem();
        if (offhandStack.isEmpty()) {
            return true;
        }

        Integer stagingSlot = findEmptyTemporaryMenuSlot(menu, offhandStack, ItemStack.EMPTY, Set.of());
        if (stagingSlot == null) {
            return false;
        }
        return swapMenuSlotWithOffhand(menu, player, gameMode, stagingSlot) && player.getOffhandItem().isEmpty();
    }

    public boolean quickMoveMenuSlot(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode,
            int menuSlot
    ) {
        Slot slot = resolver.safeSlot(menuSlot);
        if (slot == null) {
            return false;
        }
        ItemStack before = slot.getItem().copy();
        if (!slot.hasItem() || !slot.mayPickup(player)) {
            return false;
        }

        gameMode.handleInventoryMouseClick(menu.containerId, menuSlot, 0, ClickType.QUICK_MOVE, player);
        return !ItemStack.matches(before, menu.getSlot(menuSlot).getItem());
    }

    public boolean throwMenuSlot(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode,
            int menuSlot,
            boolean wholeStack
    ) {
        Slot slot = resolver.safeSlot(menuSlot);
        if (slot == null) {
            return false;
        }

        ItemStack before = slot.getItem().copy();
        if (!slot.hasItem() || !slot.mayPickup(player)) {
            return false;
        }

        gameMode.handleInventoryMouseClick(menu.containerId, menuSlot, wholeStack ? 1 : 0, ClickType.THROW, player);
        return !ItemStack.matches(before, menu.getSlot(menuSlot).getItem());
    }

    public boolean clickOffhandSlot(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode,
            int mouseButton
    ) {
        if (mouseButton != 0 && mouseButton != 1) {
            return false;
        }

        ItemStack beforeCarried = menu.getCarried().copy();
        ItemStack beforeOffhand = player.getOffhandItem().copy();
        boolean changed;

        if (beforeCarried.isEmpty()) {
            if (beforeOffhand.isEmpty()) {
                return false;
            }

            Integer stagingSlot = findEmptyTemporaryMenuSlot(menu, beforeOffhand, ItemStack.EMPTY, Set.of());
            if (stagingSlot == null || !swapMenuSlotWithOffhand(menu, player, gameMode, stagingSlot)) {
                return false;
            }

            gameMode.handleInventoryMouseClick(menu.containerId, stagingSlot, mouseButton, ClickType.PICKUP, player);
            changed = !ItemStack.matches(beforeCarried, menu.getCarried())
                    || !ItemStack.matches(beforeOffhand, player.getOffhandItem());
        } else {
            Integer stagingSlot = findEmptyTemporaryMenuSlot(menu, beforeCarried, beforeOffhand, Set.of());
            if (stagingSlot == null) {
                return false;
            }

            gameMode.handleInventoryMouseClick(menu.containerId, stagingSlot, mouseButton, ClickType.PICKUP, player);
            if (menu.getSlot(stagingSlot).getItem().isEmpty()) {
                return false;
            }
            if (!swapMenuSlotWithOffhand(menu, player, gameMode, stagingSlot)) {
                gameMode.handleInventoryMouseClick(menu.containerId, stagingSlot, 0, ClickType.PICKUP, player);
                return false;
            }

            if (menu.getSlot(stagingSlot).hasItem()) {
                gameMode.handleInventoryMouseClick(menu.containerId, stagingSlot, mouseButton, ClickType.PICKUP, player);
            }
            changed = !ItemStack.matches(beforeCarried, menu.getCarried())
                    || !ItemStack.matches(beforeOffhand, player.getOffhandItem());
        }

        return changed;
    }

    public boolean swapMenuSlotWithOffhand(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode,
            int menuSlot
    ) {
        Slot slot = resolver.safeSlot(menuSlot);
        if (slot == null || !menu.getCarried().isEmpty()) {
            return false;
        }

        ItemStack beforeSlot = slot.getItem().copy();
        ItemStack beforeOffhand = player.getOffhandItem().copy();
        gameMode.handleInventoryMouseClick(menu.containerId, menuSlot, OFFHAND_SWAP_BUTTON, ClickType.SWAP, player);
        ItemStack afterSlot = menu.getSlot(menuSlot).getItem();
        ItemStack afterOffhand = player.getOffhandItem();
        return !ItemStack.matches(beforeSlot, afterSlot) || !ItemStack.matches(beforeOffhand, afterOffhand);
    }

    public boolean useHand(LocalPlayer player, MultiPlayerGameMode gameMode, InteractionHand hand) {
        InteractionResult result = gameMode.useItem(player, hand);
        return result.consumesAction() || player.isUsingItem();
    }

    public boolean supportsInstantUse(ItemStack stack, LocalPlayer player) {
        return !stack.isEmpty() && stack.getUseDuration(player) <= 0;
    }

    public void restoreTemporaryOffhandState(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode,
            int sourceMenuSlot,
            Integer stageMenuSlot
    ) {
        if (player.containerMenu != menu || !menu.getCarried().isEmpty()) {
            return;
        }

        swapMenuSlotWithOffhand(menu, player, gameMode, sourceMenuSlot);
        if (stageMenuSlot != null && player.containerMenu == menu && menu.getCarried().isEmpty()) {
            swapMenuSlotWithOffhand(menu, player, gameMode, stageMenuSlot);
        }
    }

    public boolean canClickTargetSlot(Slot targetSlot, LocalPlayer player, ItemStack carriedStack) {
        if (targetSlot == null) {
            return false;
        }

        ItemStack targetStack = targetSlot.getItem();
        if (carriedStack.isEmpty()) {
            return targetStack.isEmpty() || targetSlot.mayPickup(player);
        }

        if (targetStack.isEmpty()) {
            return targetSlot.mayPlace(carriedStack);
        }

        if (ItemStack.isSameItemSameComponents(targetStack, carriedStack)) {
            return targetSlot.mayPlace(carriedStack);
        }

        return targetSlot.mayPickup(player) && targetSlot.mayPlace(carriedStack);
    }

    private boolean hasSlots(AbstractContainerMenu menu, int firstSlot, int secondSlot) {
        return resolver.safeSlot(firstSlot) != null && resolver.safeSlot(secondSlot) != null;
    }
}
