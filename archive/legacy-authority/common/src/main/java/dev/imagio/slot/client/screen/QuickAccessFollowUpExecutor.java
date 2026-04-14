package dev.imagio.slot.client.screen;

import dev.imagio.slot.client.policy.ItemBehaviorPolicy;
import dev.imagio.slot.intent.ActionRequestId;
import dev.imagio.slot.inventory.kernel.MenuSlotResolver;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.AbstractContainerMenu;

public final class QuickAccessFollowUpExecutor {
    private QuickAccessFollowUpExecutor() {
    }

    public static void tickClient() {
        var readyActions = QuickAccessFollowUpState.readyActions();
        if (readyActions.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        MultiPlayerGameMode gameMode = minecraft.gameMode;
        if (player == null || gameMode == null) {
            return;
        }

        QuickAccessMenuOperations menuOperations = new QuickAccessMenuOperations(new QuickAccessMenuSlotPlanner(null));
        AbstractContainerMenu currentMenu = player.containerMenu != null ? player.containerMenu : player.inventoryMenu;
        for (QuickAccessFollowUpState.PendingAction pendingAction : readyActions) {
            if (pendingAction == null) {
                continue;
            }
            if (pendingAction.type() == QuickAccessFollowUpState.ActionType.DROP_MENU_SLOT
                    && pendingAction.expectedMenu() != null
                    && pendingAction.expectedMenu() != currentMenu) {
                QuickAccessFollowUpState.completeFailed(new ActionRequestId(pendingAction.requestId()));
                continue;
            }

            boolean applied;
            if (pendingAction.type() == QuickAccessFollowUpState.ActionType.DROP_MENU_SLOT) {
                applied = executeDropMenuSlot(menuOperations, currentMenu, player, gameMode, pendingAction);
            } else {
                applied = ItemBehaviorPolicy.matchesMovableIdentity(player.getOffhandItem(), pendingAction.identity())
                        && menuOperations.useHand(player, gameMode, InteractionHand.OFF_HAND);
            }
            if (applied) {
                QuickAccessFollowUpState.completeApplied(new ActionRequestId(pendingAction.requestId()));
                continue;
            }

            if (pendingAction.type() == QuickAccessFollowUpState.ActionType.USE_OFFHAND
                    && ItemBehaviorPolicy.matchesMovableIdentity(player.getOffhandItem(), pendingAction.identity())) {
                QuickAccessFollowUpState.completeFailed(new ActionRequestId(pendingAction.requestId()));
            }
        }
    }

    private static boolean executeDropMenuSlot(
            QuickAccessMenuOperations menuOperations,
            AbstractContainerMenu menu,
            LocalPlayer player,
            MultiPlayerGameMode gameMode,
            QuickAccessFollowUpState.PendingAction pendingAction
    ) {
        if (menu == null || pendingAction == null || !menu.getCarried().isEmpty()) {
            return false;
        }
        var targetSlot = MenuSlotResolver.safeSlot(menu, pendingAction.targetMenuSlot());
        if (targetSlot == null) {
            return false;
        }
        return ItemBehaviorPolicy.matchesMovableIdentity(
                targetSlot.getItem(),
                pendingAction.identity()
        ) && menuOperations.throwMenuSlot(menu, player, gameMode, pendingAction.targetMenuSlot(), true);
    }
}
