package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.client.policy.ItemBehaviorPolicy;
import dev.imagio.slot.inventory.CarriedPlacementPolicy;
import dev.imagio.slot.inventory.kernel.ActionableSourcePolicy;
import dev.imagio.slot.inventory.kernel.MenuSlotResolver;
import dev.imagio.slot.network.BackpackTransferActionRequests;
import dev.imagio.slot.network.BackpackTransferPayload;
import dev.imagio.slot.compat.sophisticated.SophisticatedBackpackTransferSupport;
import dev.imagio.slot.session.ChestLikeMenuLayout;
import dev.imagio.slot.session.InventoryHostDescriptor;
import dev.imagio.slot.session.StorageViewResolver;
import dev.imagio.slot.storage.adapter.StorageTransferMode;
import dev.imagio.slot.storage.provider.StorageViewProviderSession;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

final class BackpackTransferOperations {
    private BackpackTransferOperations() {
    }

    static int handleExternalToCarried(
            ServerPlayer player,
            AbstractContainerMenu menu,
            ChestLikeMenuLayout layout,
            StorageViewProviderSession primaryStorageSession,
            BackpackTransferPayload payload,
            Map<UUID, CompoundTag> syncedContents
    ) {
        ItemIdentity identity = payload.identity();
        if (identity == null) {
            return 0;
        }

        return moveExternalToCarried(
                player,
                menu,
                layout,
                primaryStorageSession,
                identity,
                requestedTransferLimit(payload),
                syncedContents
        );
    }

    static int handleMenuToExternal(
            ServerPlayer player,
            AbstractContainerMenu menu,
            ChestLikeMenuLayout layout,
            StorageViewProviderSession primaryStorageSession,
            BackpackTransferPayload payload,
            Map<UUID, CompoundTag> syncedContents
    ) {
        int sourceMenuSlot = payload.menuSlot();
        String sourceId = layout.sourceIdForMenuSlot(sourceMenuSlot);
        if (sourceId == null || ChestLikeMenuLayout.SOURCE_OPEN_CONTAINER.equals(sourceId)) {
            return 0;
        }

        Slot sourceSlot = safeMenuSlot(menu, sourceMenuSlot);
        if (sourceSlot == null) {
            return 0;
        }
        if (!sourceSlot.hasItem() || !sourceSlot.mayPickup(player)) {
            return 0;
        }

        int requestedAmount = switch (payload.mode()) {
            case ONE -> 1;
            case STACK, ALL -> sourceSlot.getItem().getCount();
        };
        ItemStack moving = sourceSlot.safeTake(requestedAmount, sourceSlot.getItem().getCount(), player);
        if (moving.isEmpty()) {
            return 0;
        }

        int originalCount = moving.getCount();
        moving = primaryStorageSession.insertIntoPrimary(menu, player, moving);
        int movedCount = originalCount - moving.getCount();
        if (!moving.isEmpty()) {
            moving = sourceSlot.safeInsert(moving);
        }

        SlotDebugLog.log(
                "Server storage transfer menu->external: adapter={} sourceMenuSlot={} requested={} moved={}",
                primaryStorageSession.providerId(),
                sourceMenuSlot,
                requestedAmount,
                movedCount
        );
        return movedCount;
    }

    static int handleBackpackToExternal(
            ServerPlayer player,
            AbstractContainerMenu menu,
            StorageViewProviderSession primaryStorageSession,
            BackpackTransferPayload payload,
            Map<UUID, CompoundTag> syncedContents
    ) {
        ItemIdentity identity = payload.identity();
        if (identity == null) {
            return 0;
        }

        int limit = requestedTransferLimit(payload);
        int movedCount = moveMatchingBackpackStackToExternal(
                player,
                identity,
                limit,
                menu,
                primaryStorageSession,
                syncedContents
        );
        SlotDebugLog.log(
                "Server storage transfer backpack->external: adapter={} identity={} movedCount={}",
                primaryStorageSession.providerId(),
                identity.itemId(),
                movedCount
        );
        return movedCount;
    }

    static int handleCarriedToExternal(
            ServerPlayer player,
            AbstractContainerMenu menu,
            ChestLikeMenuLayout layout,
            StorageViewProviderSession primaryStorageSession,
            BackpackTransferPayload payload,
            Map<UUID, CompoundTag> syncedContents
    ) {
        ItemIdentity identity = payload.identity();
        if (identity == null) {
            return 0;
        }

        int limit = requestedTransferLimit(payload);
        int movedCount = moveMatchingCarriedToExternal(
                player,
                menu,
                layout,
                primaryStorageSession,
                identity,
                limit,
                syncedContents
        );
        SlotDebugLog.log(
                "Server storage transfer carried->external: adapter={} identity={} movedCount={}",
                primaryStorageSession.providerId(),
                identity.itemId(),
                movedCount
        );
        return movedCount;
    }

    static int handleBackpackToMenu(
            ServerPlayer player,
            AbstractContainerMenu menu,
            BackpackTransferActionRequests.LegacyTransferSpec spec,
            Map<UUID, CompoundTag> syncedContents
    ) {
        ItemIdentity identity = resolveIdentity(spec);
        TargetMenuResolution target = resolveTargetMenu(player, menu, spec);
        if (identity == null || target == null) {
            return 0;
        }

        int requestedLimit = requestedTransferLimit(spec);
        int movedCount = spec.targetPolicy() == BackpackTransferActionRequests.TargetPolicy.REPLACE_EXISTING
                ? moveMatchingBackpackStackToMenuReplacingTarget(
                        player,
                        identity,
                        requestedLimit,
                        target.menu(),
                        target.menuSlot(),
                        syncedContents
                )
                : moveMatchingBackpackStackToMenu(
                        player,
                        identity,
                        requestedLimit,
                        target.menu(),
                        List.of(target.menuSlot()),
                        syncedContents
                );
        SlotDebugLog.log(
                "Server storage transfer backpack->target: identity={} targetType={} targetMenu={} targetMenuSlot={} targetPolicy={} movedCount={}",
                identity.itemId(),
                spec.targetType(),
                target.menu().getClass().getName(),
                target.menuSlot(),
                spec.targetPolicy(),
                movedCount
        );
        return movedCount;
    }

    private static int moveExternalToCarried(
            ServerPlayer player,
            AbstractContainerMenu menu,
            ChestLikeMenuLayout layout,
            StorageViewProviderSession primaryStorageSession,
            ItemIdentity identity,
            int requestedAmount,
            Map<UUID, CompoundTag> syncedContents
    ) {
        int totalMovedCount = 0;
        int remaining = Math.max(0, requestedAmount);
        boolean unlimited = requestedAmount == Integer.MAX_VALUE;
        int stackBudget = requestedAmount > 0 ? requestedAmount : -1;
        while (unlimited || remaining > 0 || stackBudget < 0) {
            StorageTransferMode extractMode = unlimited || requestedAmount <= 0 || remaining > 1
                    ? StorageTransferMode.STACK
                    : StorageTransferMode.ONE;
            ItemStack moving = ExternalStorageExtractionSupport.extractMatchingIdentity(
                    menu,
                    player,
                    primaryStorageSession,
                    identity,
                    extractMode
            );
            if (moving.isEmpty()) {
                break;
            }

            ItemStack deferredRemainder = ItemStack.EMPTY;
            if (!unlimited && requestedAmount <= 0) {
                if (stackBudget < 0) {
                    stackBudget = Math.max(1, moving.getMaxStackSize());
                }
                int allowed = Math.max(0, stackBudget - totalMovedCount);
                if (allowed <= 0) {
                    primaryStorageSession.insertIntoPrimary(menu, player, moving);
                    break;
                }
                if (moving.getCount() > allowed) {
                    deferredRemainder = moving.copy();
                    deferredRemainder.setCount(moving.getCount() - allowed);
                    moving.setCount(allowed);
                }
            }

            int originalCount = moving.getCount();
            if (layout.primaryStorageIsCarried()) {
                moving = CarriedPlacementPolicy.insertIntoCarriedMenuSlots(
                        moving,
                        menu,
                        layout,
                        CarriedPlacementPolicy.Intent.GENERAL
                );
                moving = SophisticatedBackpackTransferSupport.insertIntoPlayerBackpacks(player, moving, syncedContents);
            } else {
                moving = SophisticatedBackpackTransferSupport.insertIntoPlayerBackpacks(player, moving, syncedContents);
                moving = CarriedPlacementPolicy.insertIntoCarriedMenuSlots(
                        moving,
                        menu,
                        layout,
                        CarriedPlacementPolicy.Intent.GENERAL
                );
            }
            int movedCount = originalCount - moving.getCount();
            if (movedCount > 0) {
                totalMovedCount += movedCount;
                if (!unlimited) {
                    remaining = Math.max(0, remaining - movedCount);
                }
            }
            ItemStack restoreToPrimary = combinedRemainder(moving, deferredRemainder);
            if (!restoreToPrimary.isEmpty()) {
                primaryStorageSession.insertIntoPrimary(menu, player, restoreToPrimary);
            }
            if (movedCount <= 0) {
                break;
            }
            if (!unlimited && requestedAmount <= 0 && totalMovedCount >= stackBudget) {
                break;
            }
        }

        SlotDebugLog.log(
                "Server storage transfer external->carried: adapter={} identity={} requested={} movedCount={}",
                primaryStorageSession.providerId(),
                identity.itemId(),
                requestedAmount,
                totalMovedCount
        );
        return totalMovedCount;
    }

    private static int moveMatchingBackpackStackToExternal(
            ServerPlayer player,
            ItemIdentity identity,
            int limit,
            AbstractContainerMenu menu,
            StorageViewProviderSession primaryStorageSession,
            Map<UUID, CompoundTag> syncedContents
    ) {
        BackpackSourceResolution resolution = resolveBackpackSource(player, identity);
        if (resolution == null) {
            return 0;
        }

        int[] movedCount = new int[]{0};
        SophisticatedBackpackTransferSupport.moveMatchingBackpackStack(
                player,
                resolution.identity(),
                effectiveRequestedLimit(limit, resolution.previewStack()),
                stack -> {
                    int originalCount = stack.getCount();
                    ItemStack remainder = primaryStorageSession.insertIntoPrimary(menu, player, stack);
                    movedCount[0] += Math.max(0, originalCount - remainder.getCount());
                    return remainder;
                },
                syncedContents
        );
        return movedCount[0];
    }

    private static int moveMatchingBackpackStackToMenu(
            ServerPlayer player,
            ItemIdentity identity,
            int limit,
            AbstractContainerMenu menu,
            List<Integer> targetMenuSlots,
            Map<UUID, CompoundTag> syncedContents
    ) {
        BackpackSourceResolution resolution = resolveBackpackSource(player, identity);
        if (resolution == null) {
            return 0;
        }

        return moveMatchingBackpackStackToMenuWithResolvedIdentity(
                player,
                resolution.identity(),
                limit,
                menu,
                targetMenuSlots,
                syncedContents
        );
    }

    private static int moveMatchingBackpackStackToMenuWithResolvedIdentity(
            ServerPlayer player,
            ItemIdentity identity,
            int limit,
            AbstractContainerMenu menu,
            List<Integer> targetMenuSlots,
            Map<UUID, CompoundTag> syncedContents
    ) {
        int[] movedCount = new int[]{0};
        BackpackSourceResolution resolution = resolveBackpackSource(player, identity);
        if (resolution == null) {
            return 0;
        }
        java.util.LinkedHashMap<Integer, Integer> beforeCounts = new java.util.LinkedHashMap<>();
        for (int targetMenuSlot : targetMenuSlots) {
            Slot targetSlot = safeMenuSlot(menu, targetMenuSlot);
            if (targetSlot == null) {
                continue;
            }
            ItemStack stack = targetSlot.getItem();
            beforeCounts.put(
                    targetMenuSlot,
                    !stack.isEmpty() && ItemBehaviorPolicy.matchesMovableIdentity(stack, identity) ? stack.getCount() : 0
            );
        }
        boolean movedAny = SophisticatedBackpackTransferSupport.moveMatchingBackpackStackToMenu(
                player,
                resolution.identity(),
                effectiveRequestedLimit(limit, resolution.previewStack()),
                menu,
                targetMenuSlots,
                syncedContents
        );
        if (movedAny) {
            for (int targetMenuSlot : targetMenuSlots) {
                Slot targetSlot = safeMenuSlot(menu, targetMenuSlot);
                if (targetSlot == null) {
                    continue;
                }
                ItemStack stack = targetSlot.getItem();
                if (!stack.isEmpty() && ItemBehaviorPolicy.matchesMovableIdentity(stack, identity)) {
                    movedCount[0] += Math.max(0, stack.getCount() - beforeCounts.getOrDefault(targetMenuSlot, 0));
                }
            }
            if (movedCount[0] > 0) {
                return movedCount[0];
            }
            return 1;
        }
        return 0;
    }

    private static int moveMatchingBackpackStackToMenuReplacingTarget(
            ServerPlayer player,
            ItemIdentity identity,
            int limit,
            AbstractContainerMenu menu,
            int targetMenuSlot,
            Map<UUID, CompoundTag> syncedContents
    ) {
        BackpackSourceResolution resolution = resolveBackpackSource(player, identity);
        if (resolution == null) {
            return 0;
        }

        Slot targetSlot = safeMenuSlot(menu, targetMenuSlot);
        if (targetSlot == null) {
            return 0;
        }
        if (targetSlot.hasItem() && ItemBehaviorPolicy.matchesMovableIdentity(targetSlot.getItem(), resolution.identity())) {
            return 0;
        }
        if (!targetSlot.mayPlace(resolution.previewStack())) {
            return 0;
        }

        int requestedLimit = effectiveRequestedLimit(limit, resolution.previewStack());
        return SophisticatedBackpackTransferSupport.moveFirstMatchingBackpackStack(
                player,
                resolution.identity(),
                sourceCount -> requestedLimit == Integer.MAX_VALUE ? sourceCount : Math.min(sourceCount, requestedLimit),
                stack -> insertIntoMenuSlotReplacingTarget(player, menu, targetMenuSlot, stack, syncedContents),
                syncedContents
        );
    }

    private static int moveMatchingCarriedToExternal(
            ServerPlayer player,
            AbstractContainerMenu menu,
            ChestLikeMenuLayout layout,
            StorageViewProviderSession primaryStorageSession,
            ItemIdentity identity,
            int limit,
            Map<UUID, CompoundTag> syncedContents
    ) {
        int remaining = Math.max(0, limit);
        boolean unlimited = limit == Integer.MAX_VALUE;
        int totalMovedCount = 0;
        int stackBudget = limit > 0 ? limit : -1;

        for (String sourceId : layout.actionSourceIdsForPane(dev.imagio.slot.projection.InventoryPane.CARRIED)) {
            if (!layout.sourceMenuBacked(sourceId)) {
                continue;
            }

            for (int menuSlot : layout.menuSlotsForSource(sourceId)) {
                if (!unlimited && remaining <= 0 && stackBudget >= 0) {
                    return totalMovedCount;
                }
                Slot sourceSlot = safeMenuSlot(menu, menuSlot);
                if (sourceSlot == null) {
                    continue;
                }
                ItemStack sourceStack = sourceSlot.getItem();
                if (sourceStack.isEmpty() || !sourceSlot.mayPickup(player) || !ItemBehaviorPolicy.matchesMovableIdentity(sourceStack, identity)) {
                    continue;
                }

                if (!unlimited && limit <= 0 && stackBudget < 0) {
                    stackBudget = Math.max(1, sourceStack.getMaxStackSize());
                }

                int request = unlimited
                        ? sourceStack.getCount()
                        : Math.min(dynamicRemainingBudget(remaining, totalMovedCount, stackBudget, limit), sourceStack.getCount());
                if (request <= 0) {
                    return totalMovedCount;
                }
                ItemStack moving = sourceSlot.safeTake(request, sourceStack.getCount(), player);
                if (moving.isEmpty()) {
                    continue;
                }

                int originalCount = moving.getCount();
                moving = primaryStorageSession.insertIntoPrimary(menu, player, moving);
                int movedCount = originalCount - moving.getCount();
                if (!moving.isEmpty()) {
                    moving = sourceSlot.safeInsert(moving);
                }
                if (movedCount > 0) {
                    totalMovedCount += movedCount;
                    if (!unlimited) {
                        remaining = Math.max(0, remaining - movedCount);
                    }
                }
                if (!unlimited && limit <= 0 && totalMovedCount >= stackBudget) {
                    return totalMovedCount;
                }
            }
        }

        if (!unlimited && remaining <= 0 && (limit > 0 || stackBudget >= 0)) {
            return totalMovedCount;
        }

        int backpackBudget = unlimited
                ? Integer.MAX_VALUE
                : limit > 0
                ? remaining
                : Math.max(0, stackBudget < 0 ? 0 : stackBudget - totalMovedCount);
        if (backpackBudget <= 0) {
            return totalMovedCount;
        }
        return totalMovedCount + moveMatchingBackpackStackToExternal(player, identity, backpackBudget, menu, primaryStorageSession, syncedContents);
    }

    static ItemStack insertIntoMenuSlots(ItemStack stack, AbstractContainerMenu menu, Iterable<Integer> targetMenuSlots) {
        ItemStack remainder = stack;
        for (int menuSlot : targetMenuSlots) {
            if (remainder.isEmpty()) {
                continue;
            }
            Slot slot = safeMenuSlot(menu, menuSlot);
            if (slot == null) {
                continue;
            }
            remainder = slot.safeInsert(remainder);
        }
        return remainder;
    }

    static int requestedTransferLimit(BackpackTransferPayload payload) {
        if (payload.requestedCount() > 0) {
            return payload.requestedCount();
        }
        return switch (payload.mode()) {
            case ONE -> 1;
            case STACK -> 0;
            case ALL -> Integer.MAX_VALUE;
        };
    }

    static int requestedTransferLimit(BackpackTransferActionRequests.LegacyTransferSpec spec) {
        return spec == null ? 0 : normalizedRequestedLimit(spec.requestedCount());
    }

    private static int normalizedRequestedLimit(int requestedCount) {
        if (requestedCount == Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return Math.max(0, requestedCount);
    }

    private static int effectiveRequestedLimit(int requestedCount, ItemStack referenceStack) {
        if (requestedCount == Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (requestedCount > 0) {
            return requestedCount;
        }
        if (referenceStack == null || referenceStack.isEmpty()) {
            return 1;
        }
        return Math.max(1, referenceStack.getMaxStackSize());
    }

    private static int dynamicRemainingBudget(int remaining, int totalMovedCount, int stackBudget, int requestedCount) {
        if (requestedCount > 0) {
            return Math.max(0, remaining);
        }
        if (stackBudget < 0) {
            return 0;
        }
        return Math.max(0, stackBudget - totalMovedCount);
    }

    private static ItemStack combinedRemainder(ItemStack primaryRemainder, ItemStack deferredRemainder) {
        if (primaryRemainder == null || primaryRemainder.isEmpty()) {
            return deferredRemainder == null ? ItemStack.EMPTY : deferredRemainder;
        }
        if (deferredRemainder == null || deferredRemainder.isEmpty()) {
            return primaryRemainder;
        }

        ItemStack combined = primaryRemainder.copy();
        combined.grow(deferredRemainder.getCount());
        return combined;
    }

    private static ItemIdentity resolveIdentity(BackpackTransferActionRequests.LegacyTransferSpec spec) {
        if (spec == null || spec.itemId() == null || spec.itemId().isBlank()) {
            return null;
        }
        return new ItemIdentity(spec.itemId(), spec.comparisonMode(), spec.componentFingerprint());
    }

    private static BackpackSourceResolution resolveBackpackSource(ServerPlayer player, ItemIdentity identity) {
        if (player == null || identity == null) {
            return null;
        }

        ItemStack preview = SophisticatedBackpackTransferSupport.copyFirstMatchingBackpackStack(player, identity);
        if (!preview.isEmpty()) {
            return new BackpackSourceResolution(identity, preview);
        }
        return null;
    }

    private static TargetMenuResolution resolveTargetMenu(
            ServerPlayer player,
            AbstractContainerMenu menu,
            BackpackTransferActionRequests.LegacyTransferSpec spec
    ) {
        if (player == null || spec == null) {
            return null;
        }

        if (spec.targetType() == BackpackTransferActionRequests.TargetType.PLAYER_OFFHAND) {
            if (menu != null) {
                int currentMenuOffhandSlot = resolveMenuOffhandSlot(menu, player);
                if (currentMenuOffhandSlot >= 0) {
                    return new TargetMenuResolution(menu, currentMenuOffhandSlot);
                }
            }

            AbstractContainerMenu inventoryMenu = player.inventoryMenu;
            Integer offhandMenuSlot = actionableSourcePolicy(inventoryMenu, null).offhandMenuSlot();
            if (inventoryMenu != null && offhandMenuSlot != null) {
                return new TargetMenuResolution(inventoryMenu, offhandMenuSlot);
            }
            return null;
        }

        int targetMenuSlot = spec.menuSlot();
        if (safeMenuSlot(menu, targetMenuSlot) == null) {
            return null;
        }
        return new TargetMenuResolution(menu, targetMenuSlot);
    }

    private static int resolveMenuOffhandSlot(AbstractContainerMenu menu, ServerPlayer player) {
        if (menu == null || player == null) {
            return -1;
        }
        Integer offhandSlot = actionableSourcePolicy(menu, resolveLayout(menu, player)).offhandMenuSlot();
        return offhandSlot == null ? -1 : offhandSlot;
    }

    private static ItemStack insertIntoMenuSlotReplacingTarget(
            ServerPlayer player,
            AbstractContainerMenu menu,
            int targetMenuSlot,
            ItemStack stack,
            Map<UUID, CompoundTag> syncedContents
    ) {
        if (stack.isEmpty()) {
            return stack;
        }

        Slot targetSlot = safeMenuSlot(menu, targetMenuSlot);
        if (targetSlot == null) {
            return stack;
        }
        if (!targetSlot.hasItem()) {
            return targetSlot.safeInsert(stack);
        }
        if (!targetSlot.mayPickup(player)) {
            return stack;
        }

        List<ItemStack> snapshot = snapshotMenu(menu);
        if (!stashDisplacedTarget(player, menu, targetMenuSlot)) {
            restoreMenuSnapshot(menu, snapshot);
            return stack;
        }

        ItemStack remainder = targetSlot.safeInsert(stack);
        int insertedCount = Math.max(0, stack.getCount() - remainder.getCount());
        if (insertedCount <= 0) {
            restoreMenuSnapshot(menu, snapshot);
            return stack;
        }

        return remainder;
    }

    private static DisplacedTargetStashPlan resolveDisplacedTargetStashPlan(
            ServerPlayer player,
            AbstractContainerMenu menu,
            ItemStack stack,
            int excludedMenuSlot
    ) {
        if (stack.isEmpty()) {
            return DisplacedTargetStashPlan.EMPTY;
        }

        ItemStack remainder = stack.copy();
        LinkedHashMap<Integer, ItemStack> simulatedContents = new LinkedHashMap<>();
        List<Integer> plannedMenuSlots = new ArrayList<>();
        for (int menuSlot : stashCandidateMenuSlots(menu, player, excludedMenuSlot)) {
            if (remainder.isEmpty()) {
                break;
            }

            Slot slot = safeMenuSlot(menu, menuSlot);
            if (slot == null) {
                continue;
            }
            ItemStack existing = simulatedContents.get(menuSlot);
            if (existing == null) {
                existing = slot.getItem().copy();
            }

            int capacity = insertionCapacity(slot, existing, remainder);
            if (capacity <= 0) {
                continue;
            }

            int movedCount = Math.min(capacity, remainder.getCount());
            plannedMenuSlots.add(menuSlot);
            simulatedContents.put(menuSlot, withAdditionalItems(existing, remainder, movedCount));
            remainder = shrinkCopy(remainder, movedCount);
        }

        if (!remainder.isEmpty()) {
            return null;
        }

        return new DisplacedTargetStashPlan(List.copyOf(plannedMenuSlots));
    }

    private static List<Integer> stashCandidateMenuSlots(
            AbstractContainerMenu menu,
            ServerPlayer player,
            int excludedMenuSlot
    ) {
        if (menu == null) {
            return List.of();
        }

        LinkedHashSet<Integer> orderedSlots = new LinkedHashSet<>();
        ChestLikeMenuLayout layout = resolveLayout(menu, player);
        if (layout != null) {
            ActionableSourcePolicy policy = actionableSourcePolicy(menu, layout);
            orderedSlots.addAll(policy.mainInventoryMenuSlots());
            orderedSlots.addAll(policy.hotbarMenuSlots());
            orderedSlots.addAll(layout.menuSlotsForSource(ChestLikeMenuLayout.SOURCE_CARRIED_STORAGE));
        } else {
            orderedSlots.addAll(actionableSourcePolicy(menu, null).playerInsertTargetSlots());
        }

        orderedSlots.remove(excludedMenuSlot);
        return List.copyOf(orderedSlots);
    }

    private static int insertionCapacity(Slot slot, ItemStack existing, ItemStack sourceStack) {
        if (slot == null || sourceStack.isEmpty() || !slot.mayPlace(sourceStack)) {
            return 0;
        }
        if (existing == null || existing.isEmpty()) {
            return Math.min(sourceStack.getMaxStackSize(), slot.getMaxStackSize(sourceStack));
        }
        if (!ItemStack.isSameItemSameComponents(existing, sourceStack)) {
            return 0;
        }
        return Math.max(0, Math.min(existing.getMaxStackSize(), slot.getMaxStackSize(existing)) - existing.getCount());
    }

    private static ItemStack withAdditionalItems(ItemStack existing, ItemStack added, int count) {
        if (existing == null || existing.isEmpty()) {
            ItemStack copy = added.copy();
            copy.setCount(count);
            return copy;
        }

        ItemStack copy = existing.copy();
        copy.grow(count);
        return copy;
    }

    private static ItemStack shrinkCopy(ItemStack stack, int count) {
        if (stack.isEmpty() || count <= 0) {
            return stack;
        }

        ItemStack copy = stack.copy();
        copy.shrink(count);
        return copy;
    }

    private static void restoreTargetSlot(
            Slot targetSlot,
            ItemStack stack,
            int targetMenuSlot,
            String failureContext
    ) {
        if (targetSlot == null || stack.isEmpty()) {
            return;
        }

        ItemStack restoreRemainder = targetSlot.safeInsert(stack);
        if (!restoreRemainder.isEmpty()) {
            SlotDebugLog.log(
                    "{}: targetMenuSlot={} remaining={}",
                    failureContext,
                    targetMenuSlot,
                    restoreRemainder.getCount()
            );
        }
    }

    private static List<ItemStack> snapshotMenu(AbstractContainerMenu menu) {
        List<ItemStack> snapshot = new ArrayList<>(menu.slots.size());
        for (Slot slot : menu.slots) {
            snapshot.add(slot.getItem().copy());
        }
        return List.copyOf(snapshot);
    }

    private static void restoreMenuSnapshot(AbstractContainerMenu menu, List<ItemStack> snapshot) {
        if (menu == null || snapshot == null || snapshot.size() != menu.slots.size()) {
            return;
        }

        for (int menuSlot = 0; menuSlot < menu.slots.size(); menuSlot++) {
            Slot slot = menu.getSlot(menuSlot);
            slot.set(snapshot.get(menuSlot).copy());
            slot.setChanged();
        }
    }

    private static boolean stashDisplacedTarget(
            ServerPlayer player,
            AbstractContainerMenu menu,
            int targetMenuSlot
    ) {
        Slot targetSlot = safeMenuSlot(menu, targetMenuSlot);
        if (targetSlot == null) {
            return false;
        }
        if (!targetSlot.hasItem()) {
            return true;
        }

        Slot postQuickMoveTarget = safeMenuSlot(menu, targetMenuSlot);
        if (tryQuickMoveMenuSlot(player, menu, targetMenuSlot)
                && postQuickMoveTarget != null
                && !postQuickMoveTarget.hasItem()) {
            return true;
        }

        ItemStack displaced = targetSlot.safeTake(targetSlot.getItem().getCount(), targetSlot.getItem().getCount(), player);
        if (displaced.isEmpty()) {
            return false;
        }

        DisplacedTargetStashPlan stashPlan = resolveDisplacedTargetStashPlan(player, menu, displaced, targetMenuSlot);
        if (stashPlan == null) {
            restoreTargetSlot(targetSlot, displaced, targetMenuSlot, "No valid stash plan for displaced quick-access target");
            return false;
        }

        ItemStack stashRemainder = stashPlan.apply(menu, displaced);
        if (stashRemainder.isEmpty()) {
            return true;
        }

        restoreTargetSlot(targetSlot, displaced, targetMenuSlot, "Failed to stash displaced quick-access target");
        return false;
    }

    private static boolean tryQuickMoveMenuSlot(
            ServerPlayer player,
            AbstractContainerMenu menu,
            int menuSlot
    ) {
        if (player == null || menu == null) {
            return false;
        }

        Slot slot = safeMenuSlot(menu, menuSlot);
        if (slot == null) {
            return false;
        }
        ItemStack before = slot.getItem().copy();
        if (!slot.hasItem() || !slot.mayPickup(player)) {
            return false;
        }

        ItemStack moved = menu.quickMoveStack(player, menuSlot);
        Slot afterSlot = safeMenuSlot(menu, menuSlot);
        return !moved.isEmpty() || (afterSlot != null && !ItemStack.matches(before, afterSlot.getItem()));
    }

    private static ChestLikeMenuLayout resolveLayout(AbstractContainerMenu menu, ServerPlayer player) {
        if (menu == null || player == null) {
            return null;
        }

        InventoryHostDescriptor host = StorageViewResolver.resolve(null, menu, player.getInventory(), null);
        return host == null ? null : host.layout();
    }

    private record BackpackSourceResolution(
            ItemIdentity identity,
            ItemStack previewStack
    ) {
    }

    private record TargetMenuResolution(
            AbstractContainerMenu menu,
            int menuSlot
    ) {
    }

    private record DisplacedTargetStashPlan(List<Integer> menuSlots) {
        private static final DisplacedTargetStashPlan EMPTY = new DisplacedTargetStashPlan(List.of());

        private ItemStack apply(AbstractContainerMenu menu, ItemStack stack) {
            ItemStack remainder = stack;
            for (int menuSlot : menuSlots) {
                if (remainder.isEmpty()) {
                    continue;
                }
                Slot slot = safeMenuSlot(menu, menuSlot);
                if (slot == null) {
                    continue;
                }
                remainder = slot.safeInsert(remainder);
            }
            return remainder;
        }
    }

    private static Slot safeMenuSlot(AbstractContainerMenu menu, int slotId) {
        return MenuSlotResolver.safeSlot(menu, slotId);
    }

    private static ActionableSourcePolicy actionableSourcePolicy(AbstractContainerMenu menu, ChestLikeMenuLayout layout) {
        return new ActionableSourcePolicy(new MenuSlotResolver(menu, layout));
    }
}
