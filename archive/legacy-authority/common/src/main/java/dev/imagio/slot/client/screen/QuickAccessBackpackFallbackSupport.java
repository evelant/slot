package dev.imagio.slot.client.screen;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.client.policy.ItemBehaviorPolicy;
import dev.imagio.slot.intent.ActionRequest;
import dev.imagio.slot.intent.ActionRequestId;
import dev.imagio.slot.inventory.kernel.MenuSlotResolver;
import dev.imagio.slot.network.ActionRequestClientContext;
import dev.imagio.slot.network.ActionRequestRequester;
import dev.imagio.slot.network.BackpackTransferActionRequests;
import dev.imagio.slot.session.ChestLikeMenuLayout;
import dev.imagio.slot.storage.provider.SupplementalCarriedSourceProviderRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

final class QuickAccessBackpackFallbackSupport {
    private final QuickAccessMenuSlotPlanner slotPlanner;
    private final String requestRoutingKey;
    private final boolean trackTransferHistory;

    QuickAccessBackpackFallbackSupport(
            QuickAccessMenuSlotPlanner slotPlanner,
            String requestRoutingKey,
            boolean trackTransferHistory
    ) {
        this.slotPlanner = slotPlanner;
        this.requestRoutingKey = requestRoutingKey == null ? "" : requestRoutingKey;
        this.trackTransferHistory = trackTransferHistory;
    }

    void queueBackpackUseOffhand(ActionRequestId requestId, ItemIdentity identity) {
        QuickAccessFollowUpState.recordUseOffhand(
                requestId,
                resolvedRoutingKey(requestId),
                null,
                identity
        );
    }

    void queueBackpackDropMenuSlot(ActionRequestId requestId, ItemIdentity identity, int targetMenuSlot) {
        QuickAccessFollowUpState.recordDropMenuSlot(
                requestId,
                resolvedRoutingKey(requestId),
                activeExpectedMenu(),
                identity,
                targetMenuSlot
        );
    }

    boolean backpackContainsIdentity(LocalPlayer player, ItemIdentity identity, Set<String> preferredSourceIds) {
        return !findMatchingBackpackStack(player, identity, preferredSourceIds).isEmpty();
    }

    ItemStack findMatchingBackpackStack(LocalPlayer player, ItemIdentity identity, Set<String> preferredSourceIds) {
        if (player == null || identity == null || !QuickAccessPolicy.allowsBackpackFallback(preferredSourceIds)) {
            return ItemStack.EMPTY;
        }

        for (dev.imagio.slot.storage.provider.SupplementalCarriedStackSnapshot backpackStack : SupplementalCarriedSourceProviderRegistry.readSnapshots(
                player,
                ChestLikeMenuLayout.SOURCE_PLAYER_BACKPACK,
                slotPlanner.excludedBackpackSourceReference()
        )) {
            if (ItemBehaviorPolicy.matchesMovableIdentity(backpackStack.stack(), identity)) {
                return backpackStack.stack().copy();
            }
        }
        return ItemStack.EMPTY;
    }

    boolean requestBackpackToMenuSlot(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode,
            ItemIdentity identity,
            int targetMenuSlot,
            Set<String> preferredSourceIds
    ) {
        return requestBackpackToMenuSlotId(menu, player, gameMode, identity, targetMenuSlot, preferredSourceIds, false).present();
    }

    boolean requestBackpackToMenuSlotReplacingTarget(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode,
            ItemIdentity identity,
            int targetMenuSlot,
            Set<String> preferredSourceIds
    ) {
        return requestBackpackToMenuSlotId(menu, player, gameMode, identity, targetMenuSlot, preferredSourceIds, true).present();
    }

    ActionRequestId requestBackpackToMenuSlotId(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode,
            ItemIdentity identity,
            int targetMenuSlot,
            Set<String> preferredSourceIds
    ) {
        return requestBackpackToMenuSlotId(menu, player, gameMode, identity, targetMenuSlot, preferredSourceIds, false);
    }

    ActionRequestId requestBackpackToMenuSlotReplacingTargetId(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode,
            ItemIdentity identity,
            int targetMenuSlot,
            Set<String> preferredSourceIds
    ) {
        return requestBackpackToMenuSlotId(menu, player, gameMode, identity, targetMenuSlot, preferredSourceIds, true);
    }

    private ActionRequestId requestBackpackToMenuSlotId(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode,
            ItemIdentity identity,
            int targetMenuSlot,
            Set<String> preferredSourceIds,
            boolean replaceTargetOccupant
    ) {
        if (identity == null
                || player == null
                || gameMode == null
                || !QuickAccessPolicy.allowsBackpackFallback(preferredSourceIds)) {
            return ActionRequestId.none();
        }

        Slot targetSlot = MenuSlotResolver.safeSlot(menu, targetMenuSlot);
        if (targetSlot == null) {
            return ActionRequestId.none();
        }
        if (ItemBehaviorPolicy.matchesMovableIdentity(targetSlot.getItem(), identity)) {
            return ActionRequestId.none();
        }
        if (!replaceTargetOccupant && targetSlot.hasItem()) {
            return ActionRequestId.none();
        }

        ActionRequest request = BackpackTransferActionRequests.backpackToMenu(
                menu.containerId,
                ActionRequestClientContext.currentSessionFingerprint(menu.containerId),
                identity,
                targetMenuSlot,
                0,
                replaceTargetOccupant
                        ? BackpackTransferActionRequests.TargetPolicy.REPLACE_EXISTING
                        : BackpackTransferActionRequests.TargetPolicy.FILL_ONLY
        );
        boolean requested = trackTransferHistory
                ? ActionRequestRequester.requestTracked(request)
                : !requestRoutingKey.isBlank() && ActionRequestRequester.requestRouted(request, requestRoutingKey);
        SlotDebugLog.log(
                "Backpack->menu quick-access request: identity={} targetMenuSlot={} replaceTarget={} requested={}",
                identity.itemId(),
                targetMenuSlot,
                replaceTargetOccupant,
                requested
        );
        return requested ? request.requestId() : ActionRequestId.none();
    }

    private String resolvedRoutingKey(ActionRequestId requestId) {
        return SlotActionRequestRoutingState.resolveContextKey(requestId, requestRoutingKey);
    }

    private AbstractContainerMenu activeExpectedMenu() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player == null ? null : slotPlanner.activeMenu(player);
    }

    boolean requestBackpackToOffhand(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode,
            ItemIdentity identity,
            Set<String> preferredSourceIds
    ) {
        return requestBackpackToOffhandId(menu, player, gameMode, identity, preferredSourceIds).present();
    }

    boolean requestBackpackToOffhandReplacingTarget(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode,
            ItemIdentity identity,
            Set<String> preferredSourceIds
    ) {
        return requestBackpackToOffhandReplacingTargetId(menu, player, gameMode, identity, preferredSourceIds).present();
    }

    ActionRequestId requestBackpackToOffhandId(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode,
            ItemIdentity identity,
            Set<String> preferredSourceIds
    ) {
        if (identity == null
                || player == null
                || gameMode == null
                || !QuickAccessPolicy.allowsBackpackFallback(preferredSourceIds)) {
            return ActionRequestId.none();
        }

        ActionRequest request = BackpackTransferActionRequests.backpackToOffhand(
                menu.containerId,
                ActionRequestClientContext.currentSessionFingerprint(menu.containerId),
                identity,
                0,
                BackpackTransferActionRequests.TargetPolicy.FILL_ONLY
        );
        boolean requested = trackTransferHistory
                ? ActionRequestRequester.requestTracked(request)
                : !requestRoutingKey.isBlank() && ActionRequestRequester.requestRouted(request, requestRoutingKey);
        SlotDebugLog.log(
                "Backpack->offhand quick-access request: identity={} replaceTarget={} requested={}",
                identity.itemId(),
                false,
                requested
        );
        return requested ? request.requestId() : ActionRequestId.none();
    }

    ActionRequestId requestBackpackToOffhandReplacingTargetId(
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode,
            ItemIdentity identity,
            Set<String> preferredSourceIds
    ) {
        if (identity == null
                || player == null
                || gameMode == null
                || !QuickAccessPolicy.allowsBackpackFallback(preferredSourceIds)) {
            return ActionRequestId.none();
        }

        ActionRequest request = BackpackTransferActionRequests.backpackToOffhand(
                menu.containerId,
                ActionRequestClientContext.currentSessionFingerprint(menu.containerId),
                identity,
                0,
                BackpackTransferActionRequests.TargetPolicy.REPLACE_EXISTING
        );
        boolean requested = trackTransferHistory
                ? ActionRequestRequester.requestTracked(request)
                : !requestRoutingKey.isBlank() && ActionRequestRequester.requestRouted(request, requestRoutingKey);
        SlotDebugLog.log(
                "Backpack->offhand quick-access request: identity={} replaceTarget={} requested={}",
                identity.itemId(),
                true,
                requested
        );
        return requested ? request.requestId() : ActionRequestId.none();
    }
}
