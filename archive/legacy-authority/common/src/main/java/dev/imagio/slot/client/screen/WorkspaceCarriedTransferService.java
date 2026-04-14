package dev.imagio.slot.client.screen;

import dev.imagio.slot.client.action.ActionPlanner;
import dev.imagio.slot.client.action.ActionPlannerContext;
import dev.imagio.slot.client.model.ItemEntry;
import dev.imagio.slot.client.model.SlotRef;
import dev.imagio.slot.inventory.kernel.MenuInteractionExecutor;
import dev.imagio.slot.inventory.kernel.MenuSlotResolver;
import dev.imagio.slot.session.ChestLikeMenuLayout;
import dev.imagio.slot.projection.InventoryPane;
import dev.imagio.slot.projection.InventoryViewData;
import dev.imagio.slot.client.source.InventorySource;
import dev.imagio.slot.network.BackpackTransferPayload;
import dev.imagio.slot.network.BackpackTransferRequester;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

final class WorkspaceCarriedTransferService {
    private final ActionPlanner actionPlanner = new ActionPlanner();
    private final ChestLikeMenuLayout workspaceLayout;

    WorkspaceCarriedTransferService(ChestLikeMenuLayout workspaceLayout) {
        this.workspaceLayout = workspaceLayout;
    }

    SlotActionResult moveOne(LocalPlayer player, AbstractContainerMenu menu, InventoryViewData.EntryView entry, InventoryPane pane) {
        if (shouldRouteExternalToCarriedThroughBackpackTransfer(pane)) {
            return BackpackTransferRequester.requestExternalToCarried(menu.containerId, entry.itemEntry().identity(), BackpackTransferPayload.Mode.ONE)
                    ? SlotActionResult.requested(Component.translatable("slot.screen.action.transfer.requested", entry.displayStack().getHoverName()))
                    : SlotActionResult.blocked(Component.translatable("slot.screen.action.blocked.transfer_rejected"));
        }

        PlannedWorkspaceSlot plannedSlot = chooseWorkspaceSourceSlot(player, menu, entry.itemEntry(), pane);
        if (plannedSlot == null) {
            return tryClosedBackpackTransfer(menu, entry.itemEntry(), pane, BackpackTransferPayload.Mode.ONE, entry.displayStack());
        }
        if (shouldRouteMenuToExternalThroughTransfer(pane)) {
            return BackpackTransferRequester.requestMenuToExternal(menu.containerId, plannedSlot.menuSlot(), BackpackTransferPayload.Mode.ONE)
                    ? SlotActionResult.requested(Component.translatable("slot.screen.action.transfer.requested", entry.displayStack().getHoverName()))
                    : SlotActionResult.blocked(Component.translatable("slot.screen.action.blocked.transfer_rejected"));
        }
        if (!menu.getCarried().isEmpty()) {
            return SlotActionResult.blocked(Component.translatable("slot.screen.action.blocked.cursor_busy"));
        }

        MenuSlotResolver resolver = resolverFor(menu);
        Slot sourceSlot = resolver.safeSlot(plannedSlot.menuSlot());
        if (sourceSlot == null) {
            return SlotActionResult.blocked(Component.translatable("slot.screen.action.blocked.no_movable_source"));
        }
        Integer targetSlot = resolver.firstInsertionTarget(
                transferTargetSlots(menu, pane),
                sourceSlot.getItem()
        );
        if (targetSlot == null) {
            return SlotActionResult.blocked(Component.translatable("slot.screen.action.blocked.no_destination"));
        }

        MultiPlayerGameMode gameMode = Minecraft.getInstance().gameMode;
        if (gameMode == null) {
            return SlotActionResult.NONE;
        }

        return interactionExecutor(menu).moveOne(menu, player, gameMode, plannedSlot.menuSlot(), targetSlot)
                ? SlotActionResult.applied(Component.translatable("slot.screen.action.move_one.applied", entry.displayStack().getHoverName()))
                : SlotActionResult.blocked(Component.translatable("slot.screen.action.blocked.transfer_rejected"));
    }

    SlotActionResult moveStack(LocalPlayer player, AbstractContainerMenu menu, InventoryViewData.EntryView entry, InventoryPane pane) {
        if (shouldRouteExternalToCarriedThroughBackpackTransfer(pane)) {
            return BackpackTransferRequester.requestExternalToCarried(menu.containerId, entry.itemEntry().identity(), BackpackTransferPayload.Mode.STACK)
                    ? SlotActionResult.requested(Component.translatable("slot.screen.action.transfer.requested", entry.displayStack().getHoverName()))
                    : SlotActionResult.blocked(Component.translatable("slot.screen.action.blocked.transfer_rejected"));
        }

        PlannedWorkspaceSlot plannedSlot = chooseWorkspaceSourceSlot(player, menu, entry.itemEntry(), pane);
        if (plannedSlot != null) {
            if (shouldRouteMenuToExternalThroughTransfer(pane)) {
                return BackpackTransferRequester.requestMenuToExternal(menu.containerId, plannedSlot.menuSlot(), BackpackTransferPayload.Mode.STACK)
                        ? SlotActionResult.requested(Component.translatable("slot.screen.action.transfer.requested", entry.displayStack().getHoverName()))
                        : SlotActionResult.blocked(Component.translatable("slot.screen.action.blocked.transfer_rejected"));
            }
            return quickMoveWorkspaceSlot(player, menu, plannedSlot.menuSlot())
                    ? SlotActionResult.applied(Component.translatable("slot.screen.action.move_stack.applied", entry.displayStack().getHoverName()))
                    : SlotActionResult.blocked(Component.translatable("slot.screen.action.blocked.no_movable_source"));
        }
        return tryClosedBackpackTransfer(menu, entry.itemEntry(), pane, BackpackTransferPayload.Mode.STACK, entry.displayStack());
    }

    SlotActionResult moveAllOfType(LocalPlayer player, AbstractContainerMenu menu, InventoryViewData.EntryView entry, InventoryPane pane) {
        if (shouldRouteExternalToCarriedThroughBackpackTransfer(pane)) {
            return BackpackTransferRequester.requestExternalToCarried(menu.containerId, entry.itemEntry().identity(), BackpackTransferPayload.Mode.ALL)
                    ? SlotActionResult.requested(Component.translatable("slot.screen.action.transfer.requested", entry.displayStack().getHoverName()))
                    : SlotActionResult.blocked(Component.translatable("slot.screen.action.blocked.transfer_rejected"));
        }

        Optional<InventorySource> source = chooseExistingWorkspaceSource(entry.itemEntry(), pane);
        if (source.isEmpty()) {
            return tryClosedBackpackTransfer(menu, entry.itemEntry(), pane, BackpackTransferPayload.Mode.ALL, entry.displayStack());
        }

        boolean movedAny = false;
        List<Integer> menuSlots = entry.itemEntry().backingSlots().stream()
                .filter(slotRef -> slotRef.sourceId().equals(source.get().id()))
                .map(slotRef -> resolveWorkspaceMenuSlot(menu, slotRef))
                .filter(java.util.Objects::nonNull)
                .sorted(Comparator.naturalOrder())
                .distinct()
                .toList();

        for (int menuSlot : menuSlots) {
            movedAny |= shouldRouteMenuToExternalThroughTransfer(pane)
                    ? BackpackTransferRequester.requestMenuToExternal(menu.containerId, menuSlot, BackpackTransferPayload.Mode.STACK)
                    : quickMoveWorkspaceSlot(player, menu, menuSlot);
        }
        return movedAny
                ? shouldRouteMenuToExternalThroughTransfer(pane)
                ? SlotActionResult.requested(Component.translatable("slot.screen.action.transfer.requested", entry.displayStack().getHoverName()))
                : SlotActionResult.applied(Component.translatable("slot.screen.action.move_all_type.applied", entry.displayStack().getHoverName()))
                : SlotActionResult.blocked(Component.translatable("slot.screen.action.blocked.transfer_rejected"));
    }

    SlotActionResult moveVisible(LocalPlayer player, AbstractContainerMenu menu, List<InventoryViewData.EntryView> visibleEntries, InventoryPane pane) {
        boolean movedAny = false;
        int movedTypes = 0;
        for (InventoryViewData.EntryView visibleEntry : visibleEntries) {
            SlotActionResult result = moveAllOfType(player, menu, visibleEntry, pane);
            if (result.successful()) {
                movedAny = true;
                movedTypes++;
            }
        }
        if (!movedAny) {
            return SlotActionResult.blocked(Component.translatable("slot.screen.action.blocked.transfer_rejected"));
        }

        boolean requested = shouldRouteExternalToCarriedThroughBackpackTransfer(pane)
                || shouldRouteMenuToExternalThroughTransfer(pane);
        return requested
                ? SlotActionResult.requested(Component.translatable("slot.screen.action.transfer_visible.requested", movedTypes))
                : SlotActionResult.applied(Component.translatable("slot.screen.action.move_visible.applied", movedTypes));
    }

    private Optional<InventorySource> chooseExistingWorkspaceSource(ItemEntry entry, InventoryPane pane) {
        Set<String> candidateSourceIds = new LinkedHashSet<>(entry.perSourceCounts().keySet());
        candidateSourceIds.retainAll(workspaceLayout.actionSourceIdsForPane(pane));
        return actionPlanner.chooseExistingSource(candidateSourceIds, ActionPlannerContext.of(
                workspaceLayout.sources(),
                workspaceLayout.preferredSourceId(pane),
                workspaceLayout.preferredSourceId(pane),
                null
        ));
    }

    private PlannedWorkspaceSlot chooseWorkspaceSourceSlot(LocalPlayer player, AbstractContainerMenu menu, ItemEntry entry, InventoryPane pane) {
        Optional<InventorySource> source = chooseExistingWorkspaceSource(entry, pane);
        if (source.isEmpty()) {
            return null;
        }

        return entry.backingSlots().stream()
                .filter(slotRef -> slotRef.sourceId().equals(source.get().id()))
                .sorted(Comparator.comparingInt(SlotRef::slotIndex))
                .map(slotRef -> {
                    Integer menuSlot = resolveWorkspaceMenuSlot(menu, slotRef);
                    return menuSlot == null ? null : new PlannedWorkspaceSlot(source.get(), menuSlot);
                })
                .filter(java.util.Objects::nonNull)
                .filter(planned -> {
                    Slot slot = resolverFor(menu).safeSlot(planned.menuSlot());
                    return slot.hasItem() && slot.mayPickup(player);
                })
                .findFirst()
                .orElse(null);
    }

    private Integer resolveWorkspaceMenuSlot(AbstractContainerMenu menu, SlotRef slotRef) {
        if (slotRef == null) {
            return null;
        }
        return resolverFor(menu).resolveMenuSlot(slotRef);
    }

    private boolean quickMoveWorkspaceSlot(LocalPlayer player, AbstractContainerMenu menu, int menuSlot) {
        MultiPlayerGameMode gameMode = Minecraft.getInstance().gameMode;
        if (gameMode == null) {
            return false;
        }

        Slot slot = resolverFor(menu).safeSlot(menuSlot);
        if (slot == null || !slot.hasItem() || !slot.mayPickup(player)) {
            return false;
        }

        ItemStack before = slot.getItem().copy();
        return interactionExecutor(menu).quickMoveMenuSlot(menu, player, gameMode, menuSlot)
                && !ItemStack.matches(before, resolverFor(menu).safeSlot(menuSlot).getItem());
    }

    private boolean shouldRouteExternalToCarriedThroughBackpackTransfer(InventoryPane pane) {
        return !workspaceLayout.primaryStorageIsCarried() && pane == InventoryPane.OPEN_CONTAINER;
    }

    private boolean shouldRouteMenuToExternalThroughTransfer(InventoryPane pane) {
        return !workspaceLayout.primaryStorageIsCarried()
                && pane == InventoryPane.CARRIED
                && !workspaceLayout.primaryStorageMenuBacked();
    }

    private List<Integer> transferTargetSlots(AbstractContainerMenu menu, InventoryPane sourcePane) {
        Set<String> targetSourceIds = workspaceLayout.transferTargetSourceIds(sourcePane);
        java.util.ArrayList<Integer> slots = new java.util.ArrayList<>();
        MenuSlotResolver resolver = resolverFor(menu);
        for (InventorySource source : workspaceLayout.sources()) {
            if (targetSourceIds.contains(source.id())) {
                slots.addAll(resolver.menuSlotsForSource(source.id()));
            }
        }
        return List.copyOf(slots);
    }

    private MenuSlotResolver resolverFor(AbstractContainerMenu menu) {
        return new MenuSlotResolver(menu, workspaceLayout);
    }

    private MenuInteractionExecutor interactionExecutor(AbstractContainerMenu menu) {
        return new MenuInteractionExecutor(resolverFor(menu));
    }

    private SlotActionResult tryClosedBackpackTransfer(
            AbstractContainerMenu menu,
            ItemEntry entry,
            InventoryPane pane,
            BackpackTransferPayload.Mode mode,
            ItemStack displayStack
    ) {
        if (workspaceLayout.primaryStorageIsCarried() || pane != InventoryPane.CARRIED) {
            return SlotActionResult.blocked(Component.translatable("slot.screen.action.blocked.no_movable_source"));
        }
        if (!entry.perSourceCounts().containsKey(ChestLikeMenuLayout.SOURCE_PLAYER_BACKPACK)) {
            return SlotActionResult.blocked(Component.translatable("slot.screen.action.blocked.no_movable_source"));
        }
        return BackpackTransferRequester.requestBackpackToExternal(menu.containerId, entry.identity(), mode)
                ? SlotActionResult.requested(Component.translatable("slot.screen.action.transfer.requested", displayStack.getHoverName()))
                : SlotActionResult.blocked(Component.translatable("slot.screen.action.blocked.transfer_rejected"));
    }

    private record PlannedWorkspaceSlot(InventorySource source, int menuSlot) {
    }
}
