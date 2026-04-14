package dev.imagio.slot.client.screen;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.client.collection.HotbarLoadoutCapture;
import dev.imagio.slot.client.collection.HotbarLoadoutDefinition;
import dev.imagio.slot.client.collection.HotbarLoadoutSlot;
import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.client.policy.ItemBehaviorPolicy;
import dev.imagio.slot.inventory.kernel.MenuSlotResolver;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class QuickAccessLoadoutService {
    private final QuickAccessSupport support;

    QuickAccessLoadoutService(QuickAccessSupport support) {
        this.support = support;
    }

    HotbarLoadoutCapture captureCurrentLoadout() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return new HotbarLoadoutCapture(List.of(), null);
        }

        List<HotbarLoadoutSlot> slots = new ArrayList<>();
        for (int slotIndex = 0; slotIndex < HotbarLoadoutDefinition.HOTBAR_SLOT_COUNT; slotIndex++) {
            ItemStack stack = player.getInventory().getItem(slotIndex);
            if (stack.isEmpty()) {
                continue;
            }
            slots.add(new HotbarLoadoutSlot(
                    slotIndex,
                    ItemBehaviorPolicy.normalizeQuickAccessIdentity(ItemBehaviorPolicy.createIdentity(stack))
            ));
        }

        ItemStack offhandStack = player.getOffhandItem();
        ItemIdentity offhandIdentity = offhandStack.isEmpty()
                ? null
                : ItemBehaviorPolicy.normalizeQuickAccessIdentity(ItemBehaviorPolicy.createIdentity(offhandStack));
        return new HotbarLoadoutCapture(List.copyOf(slots), offhandIdentity);
    }

    List<HotbarLoadoutSlot> captureCurrentHotbar() {
        return captureCurrentLoadout().slots();
    }

    boolean restoreCapturedLoadout(HotbarLoadoutCapture capture) {
        return restoreCapturedLoadoutMutation(capture).changed();
    }

    QuickAccessMutationResult restoreCapturedLoadoutMutation(HotbarLoadoutCapture capture) {
        if (capture == null) {
            return QuickAccessMutationResult.NONE;
        }
        if (hasPendingQuickAccessWorkflow()) {
            return QuickAccessMutationResult.NONE;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        MultiPlayerGameMode gameMode = minecraft.gameMode;
        if (player == null || gameMode == null) {
            return QuickAccessMutationResult.NONE;
        }

        HotbarLoadoutCapture before = captureCurrentLoadout();
        RequestedLoadoutOverlay requestedOverlay = new RequestedLoadoutOverlay();
        AbstractContainerMenu menu = support.activeMenu(player);
        List<Integer> hotbarMenuSlots = support.hotbarMenuSlots();
        if (hotbarMenuSlots.size() < HotbarLoadoutDefinition.HOTBAR_SLOT_COUNT) {
            return QuickAccessMutationResult.NONE;
        }

        Map<Integer, ItemIdentity> desiredBySlot = new LinkedHashMap<>();
        for (HotbarLoadoutSlot slot : capture.slots()) {
            if (slot == null || slot.identity() == null) {
                continue;
            }
            if (slot.slotIndex() < 0 || slot.slotIndex() >= HotbarLoadoutDefinition.HOTBAR_SLOT_COUNT) {
                continue;
            }
            desiredBySlot.put(slot.slotIndex(), slot.identity());
        }

        for (int slotIndex = 0; slotIndex < HotbarLoadoutDefinition.HOTBAR_SLOT_COUNT; slotIndex++) {
            int targetMenuSlot = hotbarMenuSlots.get(slotIndex);
            ItemIdentity desiredIdentity = desiredBySlot.get(slotIndex);
            Slot targetSlot = MenuSlotResolver.safeSlot(menu, targetMenuSlot);
            if (targetSlot == null) {
                continue;
            }

            if (pendingQuickAccessTarget(slotIndex)) {
                continue;
            }

            if (desiredIdentity == null) {
                if (targetSlot.hasItem()) {
                    support.clearTargetSlot(menu, player, gameMode, targetMenuSlot);
                }
                continue;
            }

            if (ItemBehaviorPolicy.matchesMovableIdentity(targetSlot.getItem(), desiredIdentity)) {
                continue;
            }

            Integer sourceMenuSlot = support.findMatchingSourceMenuSlot(menu, targetMenuSlot, desiredIdentity);
            if (sourceMenuSlot != null && support.moveSourceMenuSlotToTarget(menu, player, gameMode, sourceMenuSlot, targetMenuSlot)) {
                continue;
            }

            var requestId = support.requestBackpackToMenuSlotReplacingTargetId(menu, player, gameMode, desiredIdentity, targetMenuSlot, Set.of());
            if (requestId.present()) {
                requestedOverlay.recordSlotIdentity(slotIndex, desiredIdentity, requestId);
            }
        }

        ItemIdentity desiredOffhand = capture.offhandIdentity();
        if (pendingQuickAccessTarget(HotbarLoadoutDefinition.OFFHAND_SLOT_INDEX)) {
            return mutationResult(before, requestedOverlay);
        }
        if (desiredOffhand == null) {
            if (!player.getOffhandItem().isEmpty()) {
                support.clearOffhandSlot(menu, player, gameMode);
            }
        } else if (!ItemBehaviorPolicy.matchesMovableIdentity(player.getOffhandItem(), desiredOffhand)) {
            Integer offhandSourceMenuSlot = support.findMatchingSourceMenuSlot(menu, -1, desiredOffhand);
            if (offhandSourceMenuSlot != null && support.moveSourceMenuSlotToOffhand(menu, player, gameMode, offhandSourceMenuSlot)) {
            } else {
                var requestId = support.requestBackpackToOffhandReplacingTargetId(menu, player, gameMode, desiredOffhand, Set.of());
                if (requestId.present()) {
                    requestedOverlay.recordOffhandIdentity(desiredOffhand, requestId);
                }
            }
        }

        return mutationResult(before, requestedOverlay);
    }

    boolean applyLoadout(HotbarLoadoutDefinition loadout) {
        return applyLoadoutMutation(loadout).changed();
    }

    QuickAccessMutationResult applyLoadoutMutation(HotbarLoadoutDefinition loadout) {
        if (loadout == null || hasPendingQuickAccessWorkflow()) {
            return QuickAccessMutationResult.NONE;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        MultiPlayerGameMode gameMode = minecraft.gameMode;
        if (player == null || gameMode == null) {
            return QuickAccessMutationResult.NONE;
        }

        HotbarLoadoutCapture before = captureCurrentLoadout();
        RequestedLoadoutOverlay requestedOverlay = new RequestedLoadoutOverlay();
        AbstractContainerMenu menu = support.activeMenu(player);
        List<Integer> hotbarMenuSlots = support.hotbarMenuSlots();

        if (hotbarMenuSlots.size() >= HotbarLoadoutDefinition.HOTBAR_SLOT_COUNT) {
            for (HotbarLoadoutSlot target : loadout.slots()) {
                if (target.slotIndex() < 0 || target.slotIndex() >= hotbarMenuSlots.size()) {
                    continue;
                }
                if (pendingQuickAccessTarget(target.slotIndex())) {
                    continue;
                }

                int targetMenuSlot = hotbarMenuSlots.get(target.slotIndex());
                Slot targetSlot = MenuSlotResolver.safeSlot(menu, targetMenuSlot);
                if (targetSlot == null) {
                    continue;
                }
                if (ItemBehaviorPolicy.matchesMovableIdentity(targetSlot.getItem(), target.identity())) {
                    continue;
                }

                Integer sourceMenuSlot = support.findMatchingSourceMenuSlot(menu, targetMenuSlot, target.identity());
                if (sourceMenuSlot != null && support.moveSourceMenuSlotToTarget(menu, player, gameMode, sourceMenuSlot, targetMenuSlot)) {
                    continue;
                }

                var requestId = support.requestBackpackToMenuSlotReplacingTargetId(menu, player, gameMode, target.identity(), targetMenuSlot, Set.of());
                if (requestId.present()) {
                    requestedOverlay.recordSlotIdentity(target.slotIndex(), target.identity(), requestId);
                }
            }
        }

        ItemIdentity desiredOffhand = loadout.offhandIdentity();
        if (!pendingQuickAccessTarget(HotbarLoadoutDefinition.OFFHAND_SLOT_INDEX)) {
            if (desiredOffhand == null) {
                if (!player.getOffhandItem().isEmpty()) {
                    support.clearOffhandSlot(menu, player, gameMode);
                }
            } else if (!ItemBehaviorPolicy.matchesMovableIdentity(player.getOffhandItem(), desiredOffhand)) {
                Integer offhandSourceMenuSlot = support.findMatchingSourceMenuSlot(menu, -1, desiredOffhand);
                if (offhandSourceMenuSlot != null && support.moveSourceMenuSlotToOffhand(menu, player, gameMode, offhandSourceMenuSlot)) {
                } else {
                    var requestId = support.requestBackpackToOffhandReplacingTargetId(menu, player, gameMode, desiredOffhand, Set.of());
                    if (requestId.present()) {
                        requestedOverlay.recordOffhandIdentity(desiredOffhand, requestId);
                    }
                }
            }
        }

        return mutationResult(before, requestedOverlay);
    }

    boolean assignToQuickAccess(ItemIdentity identity, int quickAccessIndex) {
        return assignToQuickAccessMutation(identity, quickAccessIndex, Set.of()).changed();
    }

    boolean assignToQuickAccess(ItemIdentity identity, int quickAccessIndex, Set<String> preferredSourceIds) {
        return assignToQuickAccessMutation(identity, quickAccessIndex, preferredSourceIds).changed();
    }

    QuickAccessMutationResult assignToQuickAccessMutation(ItemIdentity identity, int quickAccessIndex) {
        return assignToQuickAccessMutation(identity, quickAccessIndex, Set.of());
    }

    QuickAccessMutationResult assignToQuickAccessMutation(ItemIdentity identity, int quickAccessIndex, Set<String> preferredSourceIds) {
        if (identity == null || !QuickAccessPolicy.isQuickAccessIndex(quickAccessIndex)) {
            return QuickAccessMutationResult.NONE;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        MultiPlayerGameMode gameMode = minecraft.gameMode;
        if (player == null || gameMode == null) {
            return QuickAccessMutationResult.NONE;
        }

        HotbarLoadoutCapture before = captureCurrentLoadout();
        RequestedLoadoutOverlay requestedOverlay = new RequestedLoadoutOverlay();
        AbstractContainerMenu menu = support.activeMenu(player);
        if (QuickAccessPolicy.isOffhandIndex(quickAccessIndex)) {
            if (pendingQuickAccessTarget(quickAccessIndex)) {
                return QuickAccessMutationResult.NONE;
            }
            if (ItemBehaviorPolicy.matchesMovableIdentity(player.getOffhandItem(), identity)) {
                return QuickAccessMutationResult.NONE;
            }

            Integer sourceMenuSlot = support.findMatchingSourceMenuSlot(menu, -1, identity, support.candidateSourceMenuSlots(preferredSourceIds));
            if (sourceMenuSlot == null) {
                var requestId = support.requestBackpackToOffhandReplacingTargetId(menu, player, gameMode, identity, preferredSourceIds);
                if (requestId.present()) {
                    requestedOverlay.recordOffhandIdentity(identity, requestId);
                    SlotDebugLog.log("Offhand assign requested from backpack: identity={} preferredSources={}", identity.itemId(), preferredSourceIds);
                } else {
                    SlotDebugLog.log("Offhand assign skipped: no source slot found for identity={} preferredSources={}", identity.itemId(), preferredSourceIds);
                }
                return mutationResult(before, requestedOverlay);
            }

            boolean moved = support.moveSourceMenuSlotToOffhand(menu, player, gameMode, sourceMenuSlot);
            SlotDebugLog.log(
                    "Offhand assign attempted: identity={} sourceMenuSlot={} moved={}",
                    identity.itemId(),
                    sourceMenuSlot,
                    moved
            );
            return mutationResult(before, requestedOverlay);
        }

        List<Integer> hotbarMenuSlots = support.hotbarMenuSlots();
        if (hotbarMenuSlots.size() < HotbarLoadoutDefinition.HOTBAR_SLOT_COUNT) {
            return QuickAccessMutationResult.NONE;
        }
        if (pendingQuickAccessTarget(quickAccessIndex)) {
            return QuickAccessMutationResult.NONE;
        }

        int targetMenuSlot = hotbarMenuSlots.get(quickAccessIndex);
        Slot targetSlot = MenuSlotResolver.safeSlot(menu, targetMenuSlot);
        if (targetSlot == null) {
            return QuickAccessMutationResult.NONE;
        }
        if (ItemBehaviorPolicy.matchesMovableIdentity(targetSlot.getItem(), identity)) {
            return QuickAccessMutationResult.NONE;
        }

        Integer sourceMenuSlot = support.findMatchingSourceMenuSlot(menu, targetMenuSlot, identity, support.candidateSourceMenuSlots(preferredSourceIds));
        if (sourceMenuSlot == null) {
            var requestId = support.requestBackpackToMenuSlotReplacingTargetId(menu, player, gameMode, identity, targetMenuSlot, preferredSourceIds);
            if (!requestId.present()) {
                SlotDebugLog.log("Hotbar assign skipped: no source slot found for identity={} hotbarIndex={} preferredSources={}", identity.itemId(), quickAccessIndex, preferredSourceIds);
            } else {
                requestedOverlay.recordSlotIdentity(quickAccessIndex, identity, requestId);
            }
            return mutationResult(before, requestedOverlay);
        }

        boolean moved = support.moveSourceMenuSlotToTarget(menu, player, gameMode, sourceMenuSlot, targetMenuSlot);
        SlotDebugLog.log(
                "Hotbar assign attempted: identity={} hotbarIndex={} sourceMenuSlot={} targetMenuSlot={} moved={}",
                identity.itemId(),
                quickAccessIndex,
                sourceMenuSlot,
                targetMenuSlot,
                moved
        );
        return mutationResult(before, requestedOverlay);
    }

    boolean canAssignToQuickAccess(ItemIdentity identity) {
        return canAssignToQuickAccess(identity, Set.of());
    }

    boolean canAssignToQuickAccess(ItemIdentity identity, Set<String> preferredSourceIds) {
        if (identity == null) {
            return false;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return false;
        }

        if (ItemBehaviorPolicy.matchesMovableIdentity(player.getOffhandItem(), identity)) {
            return true;
        }

        for (ItemStack stack : player.getInventory().items) {
            if (ItemBehaviorPolicy.matchesMovableIdentity(stack, identity)) {
                return true;
            }
        }

        AbstractContainerMenu menu = support.activeMenu(player);
        for (int sourceMenuSlot : support.candidateSourceMenuSlots(preferredSourceIds)) {
            Slot sourceSlot = MenuSlotResolver.safeSlot(menu, sourceMenuSlot);
            if (sourceSlot == null) {
                continue;
            }
            if (ItemBehaviorPolicy.matchesMovableIdentity(sourceSlot.getItem(), identity)) {
                return true;
            }
        }
        return support.backpackContainsIdentity(player, identity, preferredSourceIds);
    }

    boolean stashQuickAccessSlot(int quickAccessIndex) {
        return stashQuickAccessSlotMutation(quickAccessIndex).changed();
    }

    QuickAccessMutationResult stashQuickAccessSlotMutation(int quickAccessIndex) {
        if (!QuickAccessPolicy.isQuickAccessIndex(quickAccessIndex)) {
            return QuickAccessMutationResult.NONE;
        }
        if (pendingQuickAccessTarget(quickAccessIndex)) {
            return QuickAccessMutationResult.NONE;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        MultiPlayerGameMode gameMode = minecraft.gameMode;
        if (player == null || gameMode == null) {
            return QuickAccessMutationResult.NONE;
        }

        HotbarLoadoutCapture before = captureCurrentLoadout();
        AbstractContainerMenu menu = support.activeMenu(player);
        if (QuickAccessPolicy.isOffhandIndex(quickAccessIndex)) {
            boolean moved = support.clearOffhandSlot(menu, player, gameMode);
            SlotDebugLog.log("Offhand stash attempted: moved={}", moved);
            HotbarLoadoutCapture after = captureCurrentLoadout();
            return QuickAccessMutationResult.of(before, after, after, false, List.of());
        }

        List<Integer> hotbarMenuSlots = support.hotbarMenuSlots();
        if (hotbarMenuSlots.size() < HotbarLoadoutDefinition.HOTBAR_SLOT_COUNT) {
            return QuickAccessMutationResult.NONE;
        }

        int targetMenuSlot = hotbarMenuSlots.get(quickAccessIndex);
        Slot targetSlot = MenuSlotResolver.safeSlot(menu, targetMenuSlot);
        if (targetSlot == null) {
            return QuickAccessMutationResult.NONE;
        }
        if (!targetSlot.hasItem() || !targetSlot.mayPickup(player)) {
            return QuickAccessMutationResult.NONE;
        }

        boolean moved = support.clearTargetSlot(menu, player, gameMode, targetMenuSlot);
        SlotDebugLog.log(
                "Hotbar stash attempted: hotbarIndex={} targetMenuSlot={} moved={}",
                quickAccessIndex,
                targetMenuSlot,
                moved
        );
        HotbarLoadoutCapture after = captureCurrentLoadout();
        return QuickAccessMutationResult.of(before, after, after, false, List.of());
    }

    boolean clickQuickAccessSlot(int quickAccessIndex, int mouseButton) {
        return clickQuickAccessSlotMutation(quickAccessIndex, mouseButton).changed();
    }

    QuickAccessMutationResult clickQuickAccessSlotMutation(int quickAccessIndex, int mouseButton) {
        if (!QuickAccessPolicy.isQuickAccessIndex(quickAccessIndex) || (mouseButton != 0 && mouseButton != 1)) {
            return QuickAccessMutationResult.NONE;
        }
        if (pendingQuickAccessTarget(quickAccessIndex)) {
            return QuickAccessMutationResult.NONE;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        MultiPlayerGameMode gameMode = minecraft.gameMode;
        if (player == null || gameMode == null) {
            return QuickAccessMutationResult.NONE;
        }

        HotbarLoadoutCapture before = captureCurrentLoadout();
        AbstractContainerMenu menu = support.activeMenu(player);
        if (QuickAccessPolicy.isOffhandIndex(quickAccessIndex)) {
            support.clickOffhandSlot(menu, player, gameMode, mouseButton);
            HotbarLoadoutCapture after = captureCurrentLoadout();
            return QuickAccessMutationResult.of(before, after, after, false, List.of());
        }

        List<Integer> hotbarMenuSlots = support.hotbarMenuSlots();
        if (hotbarMenuSlots.size() < HotbarLoadoutDefinition.HOTBAR_SLOT_COUNT) {
            return QuickAccessMutationResult.NONE;
        }

        int targetMenuSlot = hotbarMenuSlots.get(quickAccessIndex);
        Slot targetSlot = MenuSlotResolver.safeSlot(menu, targetMenuSlot);
        if (targetSlot == null) {
            return QuickAccessMutationResult.NONE;
        }
        ItemStack beforeCarried = menu.getCarried().copy();
        ItemStack beforeTarget = targetSlot.getItem().copy();
        if (!support.canClickTargetSlot(targetSlot, player, beforeCarried)) {
            SlotDebugLog.log(
                    "Hotbar click blocked: hotbarIndex={} targetMenuSlot={} button={} carriedEmpty={} targetEmpty={} mayPickup={}",
                    quickAccessIndex,
                    targetMenuSlot,
                    mouseButton,
                    beforeCarried.isEmpty(),
                    beforeTarget.isEmpty(),
                    targetSlot.mayPickup(player)
            );
            return QuickAccessMutationResult.NONE;
        }

        gameMode.handleInventoryMouseClick(menu.containerId, targetMenuSlot, mouseButton, ClickType.PICKUP, player);
        ItemStack afterCarried = menu.getCarried();
        Slot updatedTargetSlot = MenuSlotResolver.safeSlot(menu, targetMenuSlot);
        ItemStack afterTarget = updatedTargetSlot == null ? ItemStack.EMPTY : updatedTargetSlot.getItem();
        boolean changed = !ItemStack.matches(beforeCarried, afterCarried) || !ItemStack.matches(beforeTarget, afterTarget);
        SlotDebugLog.log(
                "Hotbar click forwarded: hotbarIndex={} targetMenuSlot={} button={} changed={} carriedNowEmpty={}",
                quickAccessIndex,
                targetMenuSlot,
                mouseButton,
                changed,
                afterCarried.isEmpty()
        );
        HotbarLoadoutCapture after = captureCurrentLoadout();
        return QuickAccessMutationResult.of(before, after, after, false, List.of());
    }

    private QuickAccessMutationResult mutationResult(
            HotbarLoadoutCapture before,
            RequestedLoadoutOverlay requestedOverlay
    ) {
        HotbarLoadoutCapture localAfter = captureCurrentLoadout();
        HotbarLoadoutCapture historyAfter = requestedOverlay.apply(localAfter);
        QuickAccessMutationResult result = QuickAccessMutationResult.of(
                before,
                localAfter,
                historyAfter,
                requestedOverlay.hasRequestedChanges(),
                requestedOverlay.pendingChanges()
        );
        if (result.transferSyncExpected() && !result.pendingChanges().isEmpty()) {
            QuickAccessPendingState.recordRequestedChanges(result.pendingChanges());
        }
        return result;
    }

    private static boolean pendingQuickAccessTarget(int quickAccessIndex) {
        return QuickAccessPendingState.isPendingTarget(quickAccessIndex);
    }

    private static boolean hasPendingQuickAccessWorkflow() {
        return QuickAccessPendingState.hasPendingTargets()
                || QuickAccessFollowUpState.hasPendingActions();
    }

    private static final class RequestedLoadoutOverlay {
        private final Map<Integer, ItemIdentity> slotOverrides = new LinkedHashMap<>();
        private final List<QuickAccessMutationResult.RequestedChange> pendingChanges = new ArrayList<>();
        private boolean offhandOverridden;
        private ItemIdentity offhandIdentity;

        void recordSlotIdentity(int slotIndex, ItemIdentity identity, dev.imagio.slot.intent.ActionRequestId requestId) {
            ItemIdentity normalized = ItemBehaviorPolicy.normalizeQuickAccessIdentity(identity);
            slotOverrides.put(slotIndex, normalized);
            pendingChanges.add(new QuickAccessMutationResult.RequestedChange(slotIndex, normalized, requestId));
        }

        void recordOffhandIdentity(ItemIdentity identity, dev.imagio.slot.intent.ActionRequestId requestId) {
            ItemIdentity normalized = ItemBehaviorPolicy.normalizeQuickAccessIdentity(identity);
            offhandOverridden = true;
            offhandIdentity = normalized;
            pendingChanges.add(new QuickAccessMutationResult.RequestedChange(HotbarLoadoutDefinition.OFFHAND_SLOT_INDEX, normalized, requestId));
        }

        private void setSlotIdentity(int slotIndex, ItemIdentity identity) {
            slotOverrides.put(slotIndex, ItemBehaviorPolicy.normalizeQuickAccessIdentity(identity));
        }

        boolean hasRequestedChanges() {
            return !slotOverrides.isEmpty() || offhandOverridden;
        }

        List<QuickAccessMutationResult.RequestedChange> pendingChanges() {
            return List.copyOf(pendingChanges);
        }

        HotbarLoadoutCapture apply(HotbarLoadoutCapture base) {
            if (base == null) {
                base = new HotbarLoadoutCapture(List.of(), null);
            }

            Map<Integer, ItemIdentity> slotIdentities = new LinkedHashMap<>();
            for (HotbarLoadoutSlot slot : base.slots()) {
                if (slot != null && slot.identity() != null) {
                    slotIdentities.put(slot.slotIndex(), ItemBehaviorPolicy.normalizeQuickAccessIdentity(slot.identity()));
                }
            }

            for (Map.Entry<Integer, ItemIdentity> override : slotOverrides.entrySet()) {
                if (override.getValue() == null) {
                    slotIdentities.remove(override.getKey());
                } else {
                    slotIdentities.put(override.getKey(), override.getValue());
                }
            }

            List<HotbarLoadoutSlot> slots = new ArrayList<>();
            for (int slotIndex = 0; slotIndex < HotbarLoadoutDefinition.HOTBAR_SLOT_COUNT; slotIndex++) {
                ItemIdentity identity = slotIdentities.get(slotIndex);
                if (identity != null) {
                    slots.add(new HotbarLoadoutSlot(slotIndex, identity));
                }
            }

            ItemIdentity resolvedOffhandIdentity = offhandOverridden ? offhandIdentity : base.offhandIdentity();
            return new HotbarLoadoutCapture(List.copyOf(slots), resolvedOffhandIdentity);
        }
    }
}
