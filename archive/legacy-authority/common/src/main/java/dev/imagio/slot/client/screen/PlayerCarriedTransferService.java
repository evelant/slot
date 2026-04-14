package dev.imagio.slot.client.screen;

import dev.imagio.slot.client.action.ActionPlanner;
import dev.imagio.slot.client.action.ActionPlannerContext;
import dev.imagio.slot.client.model.ItemEntry;
import dev.imagio.slot.client.model.SlotRef;
import dev.imagio.slot.inventory.kernel.ActionableSourcePolicy;
import dev.imagio.slot.inventory.kernel.MenuInteractionExecutor;
import dev.imagio.slot.inventory.kernel.MenuSlotResolver;
import dev.imagio.slot.session.ChestLikeMenuLayout;
import dev.imagio.slot.projection.InventoryViewData;
import dev.imagio.slot.client.source.BasicInventorySource;
import dev.imagio.slot.client.source.InventorySource;
import dev.imagio.slot.client.source.SourceGroup;
import dev.imagio.slot.network.BackpackTransferRequester;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

final class PlayerCarriedTransferService {
    private static final List<InventorySource> BASE_PLAYER_SOURCES = List.of(
            new BasicInventorySource(ChestLikeMenuLayout.SOURCE_PLAYER_HOTBAR, "Hotbar", SourceGroup.PLAYER_HOTBAR, 0, false, true, true),
            new BasicInventorySource(ChestLikeMenuLayout.SOURCE_PLAYER_MAIN, "Main Inventory", SourceGroup.PLAYER_MAIN, 10, false, true, true),
            new BasicInventorySource(ChestLikeMenuLayout.SOURCE_PLAYER_BACKPACK, "Backpack", SourceGroup.CARRIED, 15, false, true, true),
            new BasicInventorySource(ChestLikeMenuLayout.SOURCE_PLAYER_ARMOR, "Armor", SourceGroup.CARRIED, 20, false, true, true),
            new BasicInventorySource(ChestLikeMenuLayout.SOURCE_PLAYER_OFFHAND, "Offhand", SourceGroup.CARRIED, 30, false, true, true)
    );

    private final ActionPlanner actionPlanner = new ActionPlanner();
    private final InventoryScreenContext playerContext;

    PlayerCarriedTransferService(InventoryScreenContext playerContext) {
        this.playerContext = playerContext;
    }

    SlotActionResult moveOne(LocalPlayer player, InventoryViewData.EntryView entry) {
        Optional<InventorySource> source = chooseExistingPlayerSource(entry.itemEntry());
        if (source.isEmpty()) {
            return SlotActionResult.blocked(Component.translatable("slot.screen.action.blocked.no_movable_source"));
        }
        if (isClosedBackpackSource(source.get().id())) {
            return requestBackpackTransferToPlayer(player, entry, 1);
        }

        PlannedPlayerSlot plannedSlot = choosePlayerSourceSlot(player, entry.itemEntry(), source.get());
        if (plannedSlot == null) {
            return SlotActionResult.blocked(Component.translatable("slot.screen.action.blocked.no_movable_source"));
        }

        if (!plannedSlot.menu().getCarried().isEmpty()) {
            return SlotActionResult.blocked(Component.translatable("slot.screen.action.blocked.cursor_busy"));
        }

        MenuSlotResolver resolver = resolverFor(plannedSlot.menu());
        Slot sourceSlot = resolver.safeSlot(plannedSlot.menuSlot());
        if (sourceSlot == null) {
            return SlotActionResult.blocked(Component.translatable("slot.screen.action.blocked.no_movable_source"));
        }

        Integer targetSlot = resolver.firstInsertionTarget(
                playerTargetSlotsFor(player, plannedSlot.menu(), plannedSlot.source().id()),
                sourceSlot.getItem()
        );
        if (targetSlot == null) {
            return SlotActionResult.blocked(Component.translatable("slot.screen.action.blocked.no_destination"));
        }

        MultiPlayerGameMode gameMode = Minecraft.getInstance().gameMode;
        if (gameMode == null) {
            return SlotActionResult.NONE;
        }

        return interactionExecutor(plannedSlot.menu()).moveOne(
                plannedSlot.menu(),
                player,
                gameMode,
                plannedSlot.menuSlot(),
                targetSlot
        )
                ? SlotActionResult.applied(Component.translatable(
                "slot.screen.action.move_one.applied",
                entry.displayStack().getHoverName()
        ))
                : SlotActionResult.blocked(Component.translatable("slot.screen.action.blocked.transfer_rejected"));
    }

    SlotActionResult moveStack(LocalPlayer player, InventoryViewData.EntryView entry) {
        Optional<InventorySource> source = chooseExistingPlayerSource(entry.itemEntry());
        if (source.isEmpty()) {
            return SlotActionResult.blocked(Component.translatable("slot.screen.action.blocked.no_movable_source"));
        }
        if (isClosedBackpackSource(source.get().id())) {
            int requestedCount = requestedStackCount(
                    entry.displayStack(),
                    entry.itemEntry().perSourceCounts().getOrDefault(
                            ChestLikeMenuLayout.SOURCE_PLAYER_BACKPACK,
                            entry.displayStack().getCount()
                    )
            );
            return requestBackpackTransferToPlayer(player, entry, requestedCount);
        }

        PlannedPlayerSlot plannedSlot = choosePlayerSourceSlot(player, entry.itemEntry(), source.get());
        if (plannedSlot == null) {
            return SlotActionResult.blocked(Component.translatable("slot.screen.action.blocked.no_movable_source"));
        }

        return quickMovePlayerSlot(player, plannedSlot.menu(), plannedSlot.menuSlot())
                ? SlotActionResult.applied(Component.translatable(
                "slot.screen.action.move_stack.applied",
                entry.displayStack().getHoverName()
        ))
                : SlotActionResult.blocked(Component.translatable("slot.screen.action.blocked.no_movable_source"));
    }

    SlotActionResult moveAllOfType(LocalPlayer player, InventoryViewData.EntryView entry) {
        Optional<InventorySource> source = chooseExistingPlayerSource(entry.itemEntry());
        if (source.isEmpty()) {
            return SlotActionResult.blocked(Component.translatable("slot.screen.action.blocked.no_movable_source"));
        }
        if (isClosedBackpackSource(source.get().id())) {
            int requestedCount = Math.max(1, entry.itemEntry().perSourceCounts().getOrDefault(ChestLikeMenuLayout.SOURCE_PLAYER_BACKPACK, entry.itemEntry().totalCount()));
            return requestBackpackTransferToPlayer(player, entry, requestedCount);
        }

        boolean movedAny = false;
        List<Integer> menuSlots = entry.itemEntry().backingSlots().stream()
                .filter(slotRef -> slotRef.sourceId().equals(source.get().id()))
                .map(this::resolvePlayerMenuSlot)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(Integer::intValue))
                .distinct()
                .toList();

        for (int menuSlot : menuSlots) {
            movedAny |= quickMovePlayerSlot(player, playerMenuForSource(source.get().id()), menuSlot);
        }

        return movedAny
                ? SlotActionResult.applied(Component.translatable(
                "slot.screen.action.move_all_type.applied",
                entry.displayStack().getHoverName()
        ))
                : SlotActionResult.blocked(Component.translatable("slot.screen.action.blocked.no_movable_source"));
    }

    SlotActionResult moveVisible(LocalPlayer player, List<InventoryViewData.EntryView> visibleEntries) {
        boolean movedAny = false;
        int movedTypes = 0;
        boolean requestedAny = false;
        for (InventoryViewData.EntryView visibleEntry : visibleEntries) {
            SlotActionResult result = moveAllOfType(player, visibleEntry);
            if (result.successful()) {
                movedAny = true;
                movedTypes++;
                requestedAny |= result.status() == SlotActionResult.Status.REQUESTED;
            }
        }
        return movedAny
                ? requestedAny
                ? SlotActionResult.requested(Component.translatable("slot.screen.action.transfer_visible.requested", movedTypes))
                : SlotActionResult.applied(Component.translatable("slot.screen.action.move_visible.applied", movedTypes))
                : SlotActionResult.blocked(Component.translatable("slot.screen.action.blocked.no_movable_source"));
    }

    private Optional<InventorySource> chooseExistingPlayerSource(ItemEntry entry) {
        Set<String> actionableSources = new LinkedHashSet<>(entry.perSourceCounts().keySet());
        actionableSources.removeIf(sourceId -> playerSourceByIdOrNull(sourceId) == null || !canHandlePlayerSource(sourceId));
        return actionPlanner.chooseExistingSource(actionableSources, ActionPlannerContext.of(
                playerSources().values(),
                preferredPlayerSourceId(),
                preferredPlayerSourceId(),
                null
        ));
    }

    private PlannedPlayerSlot choosePlayerSourceSlot(LocalPlayer player, ItemEntry entry, InventorySource source) {
        if (source == null || isClosedBackpackSource(source.id())) {
            return null;
        }

        return entry.backingSlots().stream()
                .filter(slotRef -> slotRef.sourceId().equals(source.id()))
                .sorted(Comparator.comparingInt(SlotRef::slotIndex))
                .map(slotRef -> {
                    Integer menuSlot = resolvePlayerMenuSlot(slotRef);
                    return menuSlot == null ? null : new PlannedPlayerSlot(source, playerMenuForSource(slotRef.sourceId()), menuSlot);
                })
                .filter(Objects::nonNull)
                .filter(planned -> {
                    Slot slot = planned.menu().getSlot(planned.menuSlot());
                    return slot.hasItem() && slot.mayPickup(player);
                })
                .findFirst()
                .orElse(null);
    }

    private List<Integer> playerTargetSlotsFor(LocalPlayer player, AbstractContainerMenu menu, String sourceId) {
        if (playerContext != null) {
            if (menu != playerContext.menu()) {
                return List.of();
            }
            return new ActionableSourcePolicy(new MenuSlotResolver(menu, playerContext.layout())).playerTransferTargets(sourceId);
        }

        return new ActionableSourcePolicy(new MenuSlotResolver(menu, null)).playerTransferTargets(sourceId);
    }

    private Integer resolvePlayerMenuSlot(SlotRef slotRef) {
        if (slotRef == null) {
            return null;
        }
        if (playerContext != null) {
            if (!playerContextBacksSource(slotRef.sourceId())) {
                return null;
            }
            return new MenuSlotResolver(playerContext.menu(), playerContext.layout()).resolveMenuSlot(slotRef);
        }
        AbstractContainerMenu menu = playerMenuForSource(slotRef.sourceId());
        if (menu == null) {
            return null;
        }
        List<Integer> sourceSlots = new ActionableSourcePolicy(new MenuSlotResolver(menu, null)).menuSlotsForSource(slotRef.sourceId());
        if (slotRef.slotIndex() < 0 || slotRef.slotIndex() >= sourceSlots.size()) {
            return null;
        }
        return sourceSlots.get(slotRef.slotIndex());
    }

    private AbstractContainerMenu playerMenuForSource(String sourceId) {
        if (playerContext != null) {
            return playerContextBacksSource(sourceId) ? playerContext.menu() : null;
        }
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        return player == null ? null : player.inventoryMenu;
    }

    private boolean playerContextBacksSource(String sourceId) {
        return playerContext != null && playerContext.menuBacksSource(sourceId);
    }

    private boolean canHandlePlayerSource(String sourceId) {
        return PlayerCarriedSourceSupport.sourceActionableInContext(playerContext, sourceId);
    }

    private boolean shouldRetryQuickMoveFromCarried(AbstractContainerMenu menu, int menuSlot) {
        return playerContext != null
                && menu == playerContext.menu()
                && ChestLikeMenuLayout.SOURCE_CARRIED_STORAGE.equals(playerContext.layout().sourceIdForMenuSlot(menuSlot));
    }

    private boolean spillPlayerSlotIntoCarriedStorage(
            LocalPlayer player,
            AbstractContainerMenu menu,
            MultiPlayerGameMode gameMode,
            int excludedMenuSlot
    ) {
        if (playerContext == null || gameMode == null || menu != playerContext.menu()) {
            return false;
        }

        List<Integer> playerSlots = new java.util.ArrayList<>();
        MenuSlotResolver resolver = new MenuSlotResolver(menu, playerContext.layout());
        playerSlots.addAll(resolver.menuSlotsForSource(ChestLikeMenuLayout.SOURCE_PLAYER_MAIN));
        playerSlots.addAll(resolver.menuSlotsForSource(ChestLikeMenuLayout.SOURCE_PLAYER_HOTBAR));
        for (int playerMenuSlot : playerSlots) {
            if (playerMenuSlot == excludedMenuSlot) {
                continue;
            }

            Slot slot = resolver.safeSlot(playerMenuSlot);
            if (slot == null) {
                continue;
            }
            ItemStack before = slot.getItem().copy();
            if (!slot.hasItem() || !slot.mayPickup(player)) {
                continue;
            }

            if (interactionExecutor(menu).quickMoveMenuSlot(menu, player, gameMode, playerMenuSlot)
                    && !ItemStack.matches(before, resolver.safeSlot(playerMenuSlot).getItem())) {
                return true;
            }
        }
        return false;
    }

    private String preferredPlayerSourceId() {
        return playerContext == null ? null : ChestLikeMenuLayout.SOURCE_CARRIED_STORAGE;
    }

    private static int requestedStackCount(ItemStack stack, int availableCount) {
        if (availableCount <= 0) {
            return 1;
        }
        if (stack == null || stack.isEmpty()) {
            return availableCount;
        }
        return Math.max(1, Math.min(availableCount, stack.getMaxStackSize()));
    }

    private boolean isClosedBackpackSource(String sourceId) {
        return ChestLikeMenuLayout.SOURCE_PLAYER_BACKPACK.equals(sourceId) && !playerContextBacksSource(sourceId);
    }

    private SlotActionResult requestBackpackTransferToPlayer(LocalPlayer player, InventoryViewData.EntryView entry, int requestedCount) {
        AbstractContainerMenu menu = playerContext != null
                ? playerContext.menu()
                : playerMenuForSource(ChestLikeMenuLayout.SOURCE_PLAYER_BACKPACK);
        if (player == null || menu == null || entry == null || entry.itemEntry().identity() == null) {
            return SlotActionResult.blocked(Component.translatable("slot.screen.action.blocked.no_movable_source"));
        }

        List<Integer> orderedTargets = orderedInsertTargets(player, menu, ChestLikeMenuLayout.SOURCE_PLAYER_BACKPACK, entry.displayStack());
        if (orderedTargets.isEmpty()) {
            return SlotActionResult.blocked(Component.translatable("slot.screen.action.blocked.no_destination"));
        }

        int remaining = Math.max(1, requestedCount);
        boolean sentAny = false;
        for (int targetMenuSlot : orderedTargets) {
            int capacity = CarriedTransferTargets.insertionCapacity(
                    MenuSlotResolver.safeSlot(menu, targetMenuSlot),
                    entry.displayStack()
            );
            if (capacity <= 0) {
                continue;
            }

            int requestCount = Math.min(remaining, capacity);
            if (BackpackTransferRequester.requestBackpackToMenu(menu.containerId, entry.itemEntry().identity(), targetMenuSlot, requestCount)) {
                sentAny = true;
                remaining -= requestCount;
                if (remaining <= 0) {
                    break;
                }
            }
        }

        return sentAny
                ? SlotActionResult.requested(Component.translatable(
                "slot.screen.action.transfer.requested",
                entry.displayStack().getHoverName()
        ))
                : SlotActionResult.blocked(Component.translatable("slot.screen.action.blocked.transfer_rejected"));
    }

    private List<Integer> orderedInsertTargets(LocalPlayer player, AbstractContainerMenu menu, String sourceId, ItemStack sourceStack) {
        if (sourceStack == null || sourceStack.isEmpty()) {
            return List.of();
        }
        return resolverFor(menu).insertionTargets(playerTargetSlotsFor(player, menu, sourceId), sourceStack);
    }

    private boolean quickMovePlayerSlot(LocalPlayer player, AbstractContainerMenu menu, int menuSlot) {
        MultiPlayerGameMode gameMode = Minecraft.getInstance().gameMode;
        if (gameMode == null || menu == null) {
            return false;
        }

        Slot slot = resolverFor(menu).safeSlot(menuSlot);
        if (slot == null || !slot.hasItem() || !slot.mayPickup(player)) {
            return false;
        }

        ItemStack before = slot.getItem().copy();
        if (interactionExecutor(menu).quickMoveMenuSlot(menu, player, gameMode, menuSlot)
                && !ItemStack.matches(before, resolverFor(menu).safeSlot(menuSlot).getItem())) {
            return true;
        }

        if (!shouldRetryQuickMoveFromCarried(menu, menuSlot) || !spillPlayerSlotIntoCarriedStorage(player, menu, gameMode, menuSlot)) {
            return false;
        }

        Slot retrySlot = resolverFor(menu).safeSlot(menuSlot);
        ItemStack retryBefore = retrySlot == null ? ItemStack.EMPTY : retrySlot.getItem().copy();
        if (retryBefore.isEmpty()) {
            return true;
        }

        return interactionExecutor(menu).quickMoveMenuSlot(menu, player, gameMode, menuSlot)
                && !ItemStack.matches(retryBefore, resolverFor(menu).safeSlot(menuSlot).getItem());
    }

    private MenuSlotResolver resolverFor(AbstractContainerMenu menu) {
        return new MenuSlotResolver(menu, playerContext == null ? null : playerContext.layout());
    }

    private MenuInteractionExecutor interactionExecutor(AbstractContainerMenu menu) {
        return new MenuInteractionExecutor(resolverFor(menu));
    }

    private Map<String, InventorySource> playerSources() {
        Map<String, InventorySource> resolved = new LinkedHashMap<>();
        for (InventorySource source : BASE_PLAYER_SOURCES) {
            resolved.put(source.id(), source);
        }
        if (playerContext != null) {
            for (InventorySource source : playerContext.layout().sources()) {
                if (!playerContext.menuBackedCarriedSourceIds().contains(source.id())) {
                    continue;
                }
                resolved.put(source.id(), source);
            }
        }
        return Map.copyOf(resolved);
    }

    private InventorySource playerSourceByIdOrNull(String sourceId) {
        return playerSources().get(sourceId);
    }

    private record PlannedPlayerSlot(InventorySource source, AbstractContainerMenu menu, int menuSlot) {
    }
}
