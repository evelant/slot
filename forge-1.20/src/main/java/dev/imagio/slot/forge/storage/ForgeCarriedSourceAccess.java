package dev.imagio.slot.forge.storage;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.integration.InventoryHostContext;
import dev.imagio.slot.inventory.integration.InventoryHostFamilyHint;
import dev.imagio.slot.inventory.integration.InventoryHostObservationHints;
import dev.imagio.slot.inventory.integration.InventoryHostResolver;
import dev.imagio.slot.inventory.integration.InventorySlotOwnershipPosture;
import dev.imagio.slot.inventory.query.InventoryAuthorityReadService;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.storage.CarriedInventoryRevisions;
import dev.imagio.slot.inventory.storage.CarriedProvider;
import dev.imagio.slot.inventory.storage.CarriedProviderRegistry;
import dev.imagio.slot.inventory.storage.CarriedSourceAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ForgeCarriedSourceAccess implements CarriedSourceAccess {
    @Override
    public ItemStack peek(ServerPlayer player, String sourceId, int slotIndex) {
        if (player == null || sourceId == null) {
            return ItemStack.EMPTY;
        }
        if (isBuiltinLane(sourceId)) {
            return builtinPeek(player, sourceId, slotIndex);
        }
        return CarriedProviderRegistry.forSource(sourceId)
                .map(provider -> provider.peek(player, sourceId, slotIndex))
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public ItemStack extract(ServerPlayer player, String sourceId, int slotIndex, int amount, boolean simulate) {
        if (player == null || sourceId == null || amount <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack extracted;
        if (isBuiltinLane(sourceId)) {
            extracted = builtinExtract(player, sourceId, slotIndex, amount, simulate);
        } else {
            extracted = CarriedProviderRegistry.forSource(sourceId)
                    .map(provider -> provider.extract(player, sourceId, slotIndex, amount, simulate))
                    .orElse(ItemStack.EMPTY);
        }
        if (!simulate && extracted != null && !extracted.isEmpty()) {
            CarriedInventoryRevisions.markChanged(player, "carried_extract");
        }
        return extracted == null ? ItemStack.EMPTY : extracted;
    }

    @Override
    public ItemStack insertBestFit(ServerPlayer player, ItemStack stack, boolean simulate) {
        if (player == null || stack == null || stack.isEmpty()) {
            return stack == null ? ItemStack.EMPTY : stack;
        }
        if (simulate) {
            return simulateInsertBestFit(player, stack.copy());
        }
        int originalCount = stack.getCount();
        ItemStack remaining = stack.copy();
        for (CarriedProvider provider : CarriedProviderRegistry.all()) {
            if (remaining.isEmpty()) {
                CarriedInventoryRevisions.markChanged(player, "carried_insert_best_fit");
                return ItemStack.EMPTY;
            }
            try {
                ItemStack result = provider.insertBestFit(player, remaining, false);
                if (result != null) {
                    remaining = result;
                }
            } catch (RuntimeException | LinkageError failure) {
                SlotCommon.LOGGER.warn(
                        "[SLOT] Forge carried provider insertBestFit failed provider={} simulate=false item={} count={} error={}",
                        provider.prefix(),
                        itemDescription(remaining),
                        remaining.getCount(),
                        failure.toString());
            }
        }
        if (remaining.isEmpty()) {
            CarriedInventoryRevisions.markChanged(player, "carried_insert_best_fit");
            return ItemStack.EMPTY;
        }
        boolean added = player.getInventory().add(remaining);
        if (added && remaining.isEmpty()) {
            CarriedInventoryRevisions.markChanged(player, "carried_insert_best_fit");
            return ItemStack.EMPTY;
        }
        if (remaining.getCount() < originalCount) {
            CarriedInventoryRevisions.markChanged(player, "carried_insert_best_fit");
        }
        return remaining;
    }

    @Override
    public ItemStack insertIntoProviders(ServerPlayer player, ItemStack stack, boolean simulate) {
        if (player == null || stack == null || stack.isEmpty()) {
            return stack == null ? ItemStack.EMPTY : stack;
        }
        int originalCount = stack.getCount();
        ItemStack remaining = stack.copy();
        for (CarriedProvider provider : CarriedProviderRegistry.all()) {
            if (remaining.isEmpty()) {
                break;
            }
            try {
                ItemStack result = provider.insertBestFit(player, remaining, simulate);
                remaining = result == null ? ItemStack.EMPTY : result;
            } catch (RuntimeException | LinkageError failure) {
                SlotCommon.LOGGER.warn(
                        "[SLOT] Forge carried provider insertIntoProviders failed provider={} simulate={} item={} count={} error={}",
                        provider.prefix(),
                        simulate,
                        itemDescription(remaining),
                        remaining.getCount(),
                        failure.toString());
            }
        }
        if (!simulate && remaining.getCount() < originalCount) {
            CarriedInventoryRevisions.markChanged(player, "carried_insert_providers");
        }
        return remaining;
    }

    @Override
    public Optional<CarriedLocation> findIdentity(ServerPlayer player, ItemIdentity identity) {
        if (player == null || identity == null) {
            return Optional.empty();
        }
        for (CarriedProvider provider : CarriedProviderRegistry.all()) {
            Optional<CarriedLocation> hit = provider.findIdentity(player, identity);
            if (hit.isPresent()) {
                return hit;
            }
        }
        return findBuiltinIdentity(player, identity);
    }

    @Override
    public List<CarriedLocation> findAllMatching(ServerPlayer player, ItemIdentity identity) {
        if (player == null || identity == null) {
            return List.of();
        }
        ArrayList<CarriedLocation> hits = new ArrayList<>();
        for (CarriedProvider provider : CarriedProviderRegistry.all()) {
            hits.addAll(provider.findAllMatching(player, identity));
        }
        collectBuiltinMatches(player, identity, hits);
        return List.copyOf(hits);
    }

    @Override
    public InventoryAuthoritySnapshot currentAuthority(ServerPlayer player) {
        InventoryHostDescriptor host = resolveHost(player);
        if (host == null) {
            return InventoryAuthoritySnapshot.empty();
        }
        return InventoryAuthorityReadService.serverAuthority(player, host);
    }

    @Override
    public CarriedSourceAccess.CarriedStoragePressure carriedStoragePressure(ServerPlayer player) {
        if (player == null) {
            return CarriedSourceAccess.CarriedStoragePressure.empty();
        }
        PressureCounter counter = new PressureCounter();
        Inventory inventory = player.getInventory();
        countSlots(counter, inventory.items, 9, 36);
        countSlots(counter, inventory.items, 0, 9);
        countSlots(counter, inventory.armor, 0, inventory.armor.size());
        countSlots(counter, inventory.offhand, 0, inventory.offhand.size());
        for (CarriedProvider provider : CarriedProviderRegistry.all()) {
            CarriedSourceAccess.CarriedStoragePressure pressure = provider.carriedStoragePressure(player);
            counter.capacity += pressure.slotCapacity();
            counter.occupied += pressure.occupiedSlots();
        }
        return counter.snapshot();
    }

    private static void countSlots(PressureCounter counter, List<ItemStack> stacks, int startInclusive, int endExclusive) {
        if (stacks == null) {
            return;
        }
        int start = Math.max(0, startInclusive);
        int end = Math.min(stacks.size(), Math.max(start, endExclusive));
        counter.capacity += Math.max(0, end - start);
        for (int index = start; index < end; index++) {
            ItemStack stack = stacks.get(index);
            if (stack != null && !stack.isEmpty()) {
                counter.occupied++;
            }
        }
    }

    private static final class PressureCounter {
        int capacity;
        int occupied;

        CarriedSourceAccess.CarriedStoragePressure snapshot() {
            return new CarriedSourceAccess.CarriedStoragePressure(capacity, occupied);
        }
    }

    private static boolean isBuiltinLane(String sourceId) {
        return BuiltinInventoryIds.PLAYER_MAIN.equals(sourceId)
                || BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0.equals(sourceId)
                || BuiltinInventoryIds.PLAYER_OFFHAND.equals(sourceId);
    }

    private static String itemDescription(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "empty";
        }
        return stack.getItem().builtInRegistryHolder().key().location().toString();
    }

    private static ItemStack builtinPeek(ServerPlayer player, String sourceId, int slotIndex) {
        Inventory inventory = player.getInventory();
        if (BuiltinInventoryIds.PLAYER_MAIN.equals(sourceId)) {
            int raw = slotIndex + 9;
            if (raw < 0 || raw >= inventory.items.size()) {
                return ItemStack.EMPTY;
            }
            return inventory.items.get(raw);
        }
        if (BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0.equals(sourceId)) {
            if (slotIndex < 0 || slotIndex >= 9) {
                return ItemStack.EMPTY;
            }
            return inventory.items.get(slotIndex);
        }
        if (BuiltinInventoryIds.PLAYER_OFFHAND.equals(sourceId)) {
            if (slotIndex != 0 || inventory.offhand.isEmpty()) {
                return ItemStack.EMPTY;
            }
            return inventory.offhand.get(0);
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack builtinExtract(
            ServerPlayer player,
            String sourceId,
            int slotIndex,
            int amount,
            boolean simulate
    ) {
        ItemStack current = builtinPeek(player, sourceId, slotIndex);
        if (current.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int take = Math.min(amount, current.getCount());
        ItemStack extracted = current.copy();
        extracted.setCount(take);
        if (simulate) {
            return extracted;
        }
        Inventory inventory = player.getInventory();
        int remaining = current.getCount() - take;
        if (BuiltinInventoryIds.PLAYER_OFFHAND.equals(sourceId)) {
            if (remaining <= 0) {
                inventory.offhand.set(0, ItemStack.EMPTY);
            } else {
                ItemStack left = current.copy();
                left.setCount(remaining);
                inventory.offhand.set(0, left);
            }
        } else {
            int raw = BuiltinInventoryIds.PLAYER_MAIN.equals(sourceId) ? slotIndex + 9 : slotIndex;
            if (remaining <= 0) {
                inventory.setItem(raw, ItemStack.EMPTY);
            } else {
                ItemStack left = current.copy();
                left.setCount(remaining);
                inventory.setItem(raw, left);
            }
        }
        inventory.setChanged();
        return extracted;
    }

    private static Optional<CarriedLocation> findBuiltinIdentity(ServerPlayer player, ItemIdentity identity) {
        Inventory inventory = player.getInventory();
        for (int raw = 9; raw < inventory.items.size() && raw < 36; raw++) {
            ItemStack stack = inventory.items.get(raw);
            if (!stack.isEmpty() && ItemIdentityMatcher.matchesMovable(stack, identity)) {
                return Optional.of(new CarriedLocation(BuiltinInventoryIds.PLAYER_MAIN, raw - 9));
            }
        }
        for (int raw = 0; raw < 9 && raw < inventory.items.size(); raw++) {
            ItemStack stack = inventory.items.get(raw);
            if (!stack.isEmpty() && ItemIdentityMatcher.matchesMovable(stack, identity)) {
                return Optional.of(new CarriedLocation(BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0, raw));
            }
        }
        if (!inventory.offhand.isEmpty()) {
            ItemStack stack = inventory.offhand.get(0);
            if (!stack.isEmpty() && ItemIdentityMatcher.matchesMovable(stack, identity)) {
                return Optional.of(new CarriedLocation(BuiltinInventoryIds.PLAYER_OFFHAND, 0));
            }
        }
        return Optional.empty();
    }

    private static void collectBuiltinMatches(ServerPlayer player, ItemIdentity identity, List<CarriedLocation> out) {
        Inventory inventory = player.getInventory();
        for (int raw = 9; raw < inventory.items.size() && raw < 36; raw++) {
            ItemStack stack = inventory.items.get(raw);
            if (!stack.isEmpty() && ItemIdentityMatcher.matchesMovable(stack, identity)) {
                out.add(new CarriedLocation(BuiltinInventoryIds.PLAYER_MAIN, raw - 9));
            }
        }
        for (int raw = 0; raw < 9 && raw < inventory.items.size(); raw++) {
            ItemStack stack = inventory.items.get(raw);
            if (!stack.isEmpty() && ItemIdentityMatcher.matchesMovable(stack, identity)) {
                out.add(new CarriedLocation(BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0, raw));
            }
        }
        if (!inventory.offhand.isEmpty()) {
            ItemStack stack = inventory.offhand.get(0);
            if (!stack.isEmpty() && ItemIdentityMatcher.matchesMovable(stack, identity)) {
                out.add(new CarriedLocation(BuiltinInventoryIds.PLAYER_OFFHAND, 0));
            }
        }
    }

    private static ItemStack simulateInsertBestFit(ServerPlayer player, ItemStack stack) {
        ItemStack remaining = stack.copy();
        Inventory inventory = player.getInventory();
        for (int index = 0; index < inventory.items.size() && !remaining.isEmpty(); index++) {
            ItemStack existing = inventory.items.get(index);
            if (existing.isEmpty()) {
                int move = Math.min(remaining.getMaxStackSize(), remaining.getCount());
                remaining.shrink(move);
                continue;
            }
            if (ItemStack.isSameItemSameTags(existing, remaining)) {
                int room = Math.max(0, existing.getMaxStackSize() - existing.getCount());
                int move = Math.min(room, remaining.getCount());
                remaining.shrink(move);
            }
        }
        return remaining;
    }

    private static InventoryHostDescriptor resolveHost(ServerPlayer player) {
        if (player == null) {
            return null;
        }
        AbstractContainerMenu menu = player.containerMenu;
        if (menu == null) {
            return null;
        }
        return InventoryHostResolver.resolve(new InventoryHostContext(
                menu,
                player.getInventory(),
                Component.literal("SLOT CarriedSourceAccess"),
                ForgeCarriedSourceAccess.class.getName(),
                new InventoryHostObservationHints(
                        InventoryHostFamilyHint.CARRIED_ONLY,
                        InventorySlotOwnershipPosture.SLOT_OWNED,
                        true,
                        true,
                        Map.of("carriedSourceAccess", "true")
                )
        ));
    }
}
