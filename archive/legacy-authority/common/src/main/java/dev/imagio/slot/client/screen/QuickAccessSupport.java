package dev.imagio.slot.client.screen;

import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.intent.ActionRequestId;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Set;

final class QuickAccessSupport {
    private final QuickAccessMenuSlotPlanner slotPlanner;
    private final QuickAccessMenuOperations menuOperations;
    private final QuickAccessBackpackFallbackSupport backpackFallback;

    QuickAccessSupport(InventoryScreenContext screenContext) {
        this(screenContext, "", true);
    }

    QuickAccessSupport(InventoryScreenContext screenContext, String requestRoutingKey, boolean trackTransferHistory) {
        this.slotPlanner = new QuickAccessMenuSlotPlanner(screenContext);
        this.menuOperations = new QuickAccessMenuOperations(slotPlanner);
        this.backpackFallback = new QuickAccessBackpackFallbackSupport(
                slotPlanner,
                requestRoutingKey,
                trackTransferHistory
        );
    }

    void tick() {
        // Follow-up execution is driven by the global client tick so it survives screen transitions.
    }

    void queueBackpackUseOffhand(ActionRequestId requestId, ItemIdentity identity) {
        backpackFallback.queueBackpackUseOffhand(requestId, identity);
    }

    void queueBackpackDropMenuSlot(ActionRequestId requestId, ItemIdentity identity, int targetMenuSlot) {
        backpackFallback.queueBackpackDropMenuSlot(requestId, identity, targetMenuSlot);
    }

    Integer findMatchingSourceMenuSlot(AbstractContainerMenu menu, int targetMenuSlot, ItemIdentity identity) {
        return slotPlanner.findMatchingSourceMenuSlot(menu, targetMenuSlot, identity);
    }

    Integer findMatchingSourceMenuSlot(
            AbstractContainerMenu menu,
            int targetMenuSlot,
            ItemIdentity identity,
            List<Integer> candidateMenuSlots
    ) {
        return slotPlanner.findMatchingSourceMenuSlot(menu, targetMenuSlot, identity, candidateMenuSlots);
    }

    List<Integer> candidateSourceMenuSlots(Set<String> preferredSourceIds) {
        return slotPlanner.candidateSourceMenuSlots(preferredSourceIds);
    }

    boolean backpackContainsIdentity(LocalPlayer player, ItemIdentity identity, Set<String> preferredSourceIds) {
        return backpackFallback.backpackContainsIdentity(player, identity, preferredSourceIds);
    }

    ItemStack findMatchingBackpackStack(LocalPlayer player, ItemIdentity identity, Set<String> preferredSourceIds) {
        return backpackFallback.findMatchingBackpackStack(player, identity, preferredSourceIds);
    }

    boolean requestBackpackToMenuSlot(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode,
            ItemIdentity identity,
            int targetMenuSlot,
            Set<String> preferredSourceIds
    ) {
        return backpackFallback.requestBackpackToMenuSlot(menu, player, gameMode, identity, targetMenuSlot, preferredSourceIds);
    }

    boolean requestBackpackToMenuSlotReplacingTarget(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode,
            ItemIdentity identity,
            int targetMenuSlot,
            Set<String> preferredSourceIds
    ) {
        return backpackFallback.requestBackpackToMenuSlotReplacingTarget(menu, player, gameMode, identity, targetMenuSlot, preferredSourceIds);
    }

    ActionRequestId requestBackpackToMenuSlotId(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode,
            ItemIdentity identity,
            int targetMenuSlot,
            Set<String> preferredSourceIds
    ) {
        return backpackFallback.requestBackpackToMenuSlotId(menu, player, gameMode, identity, targetMenuSlot, preferredSourceIds);
    }

    ActionRequestId requestBackpackToMenuSlotReplacingTargetId(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode,
            ItemIdentity identity,
            int targetMenuSlot,
            Set<String> preferredSourceIds
    ) {
        return backpackFallback.requestBackpackToMenuSlotReplacingTargetId(menu, player, gameMode, identity, targetMenuSlot, preferredSourceIds);
    }

    boolean requestBackpackToOffhand(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode,
            ItemIdentity identity,
            Set<String> preferredSourceIds
    ) {
        return backpackFallback.requestBackpackToOffhand(menu, player, gameMode, identity, preferredSourceIds);
    }

    boolean requestBackpackToOffhandReplacingTarget(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode,
            ItemIdentity identity,
            Set<String> preferredSourceIds
    ) {
        return backpackFallback.requestBackpackToOffhandReplacingTarget(menu, player, gameMode, identity, preferredSourceIds);
    }

    ActionRequestId requestBackpackToOffhandId(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode,
            ItemIdentity identity,
            Set<String> preferredSourceIds
    ) {
        return backpackFallback.requestBackpackToOffhandId(menu, player, gameMode, identity, preferredSourceIds);
    }

    ActionRequestId requestBackpackToOffhandReplacingTargetId(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode,
            ItemIdentity identity,
            Set<String> preferredSourceIds
    ) {
        return backpackFallback.requestBackpackToOffhandReplacingTargetId(menu, player, gameMode, identity, preferredSourceIds);
    }

    int offhandMenuSlot(AbstractContainerMenu menu) {
        return slotPlanner.offhandMenuSlot(menu);
    }

    List<Integer> hotbarMenuSlots() {
        return slotPlanner.hotbarMenuSlots();
    }

    List<Integer> mainInventoryMenuSlots() {
        return slotPlanner.mainInventoryMenuSlots();
    }

    Integer findEmptyTemporaryMenuSlot(
            AbstractContainerMenu menu,
            ItemStack primaryStack,
            ItemStack secondaryStack,
            Set<Integer> excludedSlots
    ) {
        return menuOperations.findEmptyTemporaryMenuSlot(menu, primaryStack, secondaryStack, excludedSlots);
    }

    boolean moveSourceMenuSlotToTarget(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode,
            int sourceMenuSlot,
            int targetMenuSlot
    ) {
        return menuOperations.moveSourceMenuSlotToTarget(menu, player, gameMode, sourceMenuSlot, targetMenuSlot);
    }

    boolean moveSourceMenuSlotToOffhand(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode,
            int sourceMenuSlot
    ) {
        return menuOperations.moveSourceMenuSlotToOffhand(menu, player, gameMode, sourceMenuSlot);
    }

    boolean clearTargetSlot(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode,
            int targetMenuSlot
    ) {
        return menuOperations.clearTargetSlot(menu, player, gameMode, targetMenuSlot);
    }

    boolean clearOffhandSlot(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode
    ) {
        return menuOperations.clearOffhandSlot(menu, player, gameMode);
    }

    boolean quickMoveMenuSlot(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode,
            int menuSlot
    ) {
        return menuOperations.quickMoveMenuSlot(menu, player, gameMode, menuSlot);
    }

    boolean throwMenuSlot(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode,
            int menuSlot,
            boolean wholeStack
    ) {
        return menuOperations.throwMenuSlot(menu, player, gameMode, menuSlot, wholeStack);
    }

    boolean clickOffhandSlot(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode,
            int mouseButton
    ) {
        return menuOperations.clickOffhandSlot(menu, player, gameMode, mouseButton);
    }

    boolean swapMenuSlotWithOffhand(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode,
            int menuSlot
    ) {
        return menuOperations.swapMenuSlotWithOffhand(menu, player, gameMode, menuSlot);
    }

    boolean useHand(LocalPlayer player, MultiPlayerGameMode gameMode, InteractionHand hand) {
        return menuOperations.useHand(player, gameMode, hand);
    }

    boolean supportsInstantUse(ItemStack stack, LocalPlayer player) {
        return menuOperations.supportsInstantUse(stack, player);
    }

    void restoreTemporaryOffhandState(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode,
            int sourceMenuSlot,
            Integer stageMenuSlot
    ) {
        menuOperations.restoreTemporaryOffhandState(menu, player, gameMode, sourceMenuSlot, stageMenuSlot);
    }

    AbstractContainerMenu activeMenu(LocalPlayer player) {
        return slotPlanner.activeMenu(player);
    }

    boolean canClickTargetSlot(Slot targetSlot, LocalPlayer player, ItemStack carriedStack) {
        return menuOperations.canClickTargetSlot(targetSlot, player, carriedStack);
    }
}
