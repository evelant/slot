package dev.imagio.slot.neoforge.storage;

import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.integration.InventoryHostContext;
import dev.imagio.slot.inventory.integration.InventoryHostFamilyHint;
import dev.imagio.slot.inventory.integration.InventoryHostObservationHints;
import dev.imagio.slot.inventory.integration.InventoryHostResolver;
import dev.imagio.slot.inventory.integration.InventorySlotOwnershipPosture;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryAuthorityReadService;
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

/**
 * NeoForge implementation of {@link CarriedSourceAccess}. Dispatches by source id:
 *
 * <ul>
 *   <li>Builtin lanes (main / hotbar / offhand) — direct vanilla {@link Inventory}
 *       access.
 *   <li>Provider-backed carried sources — routed through
 *       {@link CarriedProviderRegistry}. Each {@link CarriedProvider}
 *       (Sophisticated Backpacks, Curios, future mods) owns a prefix and
 *       answers all ops for its own sources. Adding a new provider is
 *       additive: register a {@code CarriedProvider}, done — no edits here.
 * </ul>
 */
public final class NeoForgeCarriedSourceAccess implements CarriedSourceAccess {

    @Override
    public ItemStack peek(ServerPlayer player, String sourceId, int slotIndex) {
        if (player == null || sourceId == null) {
            return ItemStack.EMPTY;
        }
        if (isBuiltinLane(sourceId)) {
            return builtinPeek(player, sourceId, slotIndex);
        }
        return CarriedProviderRegistry.forSource(sourceId)
                .map(p -> p.peek(player, sourceId, slotIndex))
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public ItemStack extract(ServerPlayer player, String sourceId, int slotIndex, int amount, boolean simulate) {
        if (player == null || sourceId == null || amount <= 0) {
            return ItemStack.EMPTY;
        }
        if (isBuiltinLane(sourceId)) {
            return builtinExtract(player, sourceId, slotIndex, amount, simulate);
        }
        return CarriedProviderRegistry.forSource(sourceId)
                .map(p -> p.extract(player, sourceId, slotIndex, amount, simulate))
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public ItemStack insertBestFit(ServerPlayer player, ItemStack stack, boolean simulate) {
        if (player == null || stack == null || stack.isEmpty()) {
            return stack == null ? ItemStack.EMPTY : stack;
        }
        if (simulate) {
            // Vanilla Inventory.add has no simulate mode. Approximate: clone the
            // stack and walk potential destinations in stableOrder, stopping as
            // soon as we can account for the whole count.
            return simulateInsertBestFit(player, stack.copy());
        }
        ItemStack remaining = stack.copy();
        // Inventory.add consults SB's pickup mixin, which routes into backpacks
        // first before returning false. Any mod that mixes into vanilla pickup
        // is automatically respected. Mods that DON'T mix into pickup need an
        // explicit fall-through below (none yet).
        boolean added = player.getInventory().add(remaining);
        if (added && remaining.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return remaining;
    }

    @Override
    public ItemStack insertIntoProviders(ServerPlayer player, ItemStack stack, boolean simulate) {
        if (player == null || stack == null || stack.isEmpty()) {
            return stack == null ? ItemStack.EMPTY : stack;
        }
        ItemStack remaining = stack.copy();
        for (CarriedProvider provider : CarriedProviderRegistry.all()) {
            if (remaining.isEmpty()) {
                break;
            }
            try {
                ItemStack result = provider.insertBestFit(player, remaining, simulate);
                if (result == null) {
                    result = ItemStack.EMPTY;
                }
                remaining = result;
            } catch (RuntimeException | LinkageError ignored) {
            }
        }
        return remaining;
    }

    @Override
    public Optional<CarriedLocation> findIdentity(ServerPlayer player, ItemIdentity identity) {
        if (player == null || identity == null) {
            return Optional.empty();
        }
        // Builtin lanes first (stableOrder preference: main/hotbar/offhand
        // before backpacks), then each provider in registration order.
        Optional<CarriedLocation> builtin = findBuiltinIdentity(player, identity);
        if (builtin.isPresent()) {
            return builtin;
        }
        for (CarriedProvider provider : CarriedProviderRegistry.all()) {
            Optional<CarriedLocation> hit = provider.findIdentity(player, identity);
            if (hit.isPresent()) {
                return hit;
            }
        }
        return Optional.empty();
    }

    @Override
    public List<CarriedLocation> findAllMatching(ServerPlayer player, ItemIdentity identity) {
        if (player == null || identity == null) {
            return List.of();
        }
        ArrayList<CarriedLocation> hits = new ArrayList<>();
        collectBuiltinMatches(player, identity, hits);
        for (CarriedProvider provider : CarriedProviderRegistry.all()) {
            hits.addAll(provider.findAllMatching(player, identity));
        }
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

    private static boolean isBuiltinLane(String sourceId) {
        return BuiltinInventoryIds.PLAYER_MAIN.equals(sourceId)
                || BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0.equals(sourceId)
                || BuiltinInventoryIds.PLAYER_OFFHAND.equals(sourceId);
    }

    private static ItemStack builtinPeek(ServerPlayer player, String sourceId, int slotIndex) {
        Inventory inv = player.getInventory();
        if (BuiltinInventoryIds.PLAYER_MAIN.equals(sourceId)) {
            int raw = slotIndex + 9;
            if (raw < 0 || raw >= inv.items.size()) {
                return ItemStack.EMPTY;
            }
            return inv.items.get(raw);
        }
        if (BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0.equals(sourceId)) {
            if (slotIndex < 0 || slotIndex >= 9) {
                return ItemStack.EMPTY;
            }
            return inv.items.get(slotIndex);
        }
        if (BuiltinInventoryIds.PLAYER_OFFHAND.equals(sourceId)) {
            if (slotIndex != 0 || inv.offhand.isEmpty()) {
                return ItemStack.EMPTY;
            }
            return inv.offhand.get(0);
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack builtinExtract(ServerPlayer player, String sourceId, int slotIndex, int amount, boolean simulate) {
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
        Inventory inv = player.getInventory();
        int remaining = current.getCount() - take;
        if (BuiltinInventoryIds.PLAYER_OFFHAND.equals(sourceId)) {
            if (remaining <= 0) {
                inv.offhand.set(0, ItemStack.EMPTY);
            } else {
                ItemStack left = current.copy();
                left.setCount(remaining);
                inv.offhand.set(0, left);
            }
        } else {
            int raw = BuiltinInventoryIds.PLAYER_MAIN.equals(sourceId) ? slotIndex + 9 : slotIndex;
            if (remaining <= 0) {
                inv.setItem(raw, ItemStack.EMPTY);
            } else {
                ItemStack left = current.copy();
                left.setCount(remaining);
                inv.setItem(raw, left);
            }
        }
        inv.setChanged();
        return extracted;
    }

    private static Optional<CarriedLocation> findBuiltinIdentity(ServerPlayer player, ItemIdentity identity) {
        Inventory inv = player.getInventory();
        // Main: raw slots 9..35 → logical 0..26
        for (int raw = 9; raw < inv.items.size() && raw < 36; raw++) {
            ItemStack s = inv.items.get(raw);
            if (!s.isEmpty() && dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(s).equals(identity)) {
                return Optional.of(new CarriedLocation(BuiltinInventoryIds.PLAYER_MAIN, raw - 9));
            }
        }
        // Hotbar: raw slots 0..8
        for (int raw = 0; raw < 9 && raw < inv.items.size(); raw++) {
            ItemStack s = inv.items.get(raw);
            if (!s.isEmpty() && dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(s).equals(identity)) {
                return Optional.of(new CarriedLocation(BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0, raw));
            }
        }
        // Offhand
        if (!inv.offhand.isEmpty()) {
            ItemStack s = inv.offhand.get(0);
            if (!s.isEmpty() && dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(s).equals(identity)) {
                return Optional.of(new CarriedLocation(BuiltinInventoryIds.PLAYER_OFFHAND, 0));
            }
        }
        return Optional.empty();
    }

    private static void collectBuiltinMatches(ServerPlayer player, ItemIdentity identity, List<CarriedLocation> out) {
        Inventory inv = player.getInventory();
        for (int raw = 9; raw < inv.items.size() && raw < 36; raw++) {
            ItemStack s = inv.items.get(raw);
            if (!s.isEmpty() && dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(s).equals(identity)) {
                out.add(new CarriedLocation(BuiltinInventoryIds.PLAYER_MAIN, raw - 9));
            }
        }
        for (int raw = 0; raw < 9 && raw < inv.items.size(); raw++) {
            ItemStack s = inv.items.get(raw);
            if (!s.isEmpty() && dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(s).equals(identity)) {
                out.add(new CarriedLocation(BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0, raw));
            }
        }
        if (!inv.offhand.isEmpty()) {
            ItemStack s = inv.offhand.get(0);
            if (!s.isEmpty() && dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(s).equals(identity)) {
                out.add(new CarriedLocation(BuiltinInventoryIds.PLAYER_OFFHAND, 0));
            }
        }
    }

    private static ItemStack simulateInsertBestFit(ServerPlayer player, ItemStack stack) {
        // Simulation via a clone of the inventory layout. Not perfectly fidelity
        // with Inventory.add's stack-merge order, but good enough for
        // "is there room anywhere" checks. Returns remainder.
        ItemStack remaining = stack.copy();
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.items.size() && !remaining.isEmpty(); i++) {
            ItemStack existing = inv.items.get(i);
            if (existing.isEmpty()) {
                int cap = Math.min(remaining.getMaxStackSize(), remaining.getCount());
                remaining.shrink(cap);
                continue;
            }
            if (ItemStack.isSameItemSameComponents(existing, remaining)) {
                int room = Math.max(0, existing.getMaxStackSize() - existing.getCount());
                int move = Math.min(room, remaining.getCount());
                remaining.shrink(move);
            }
        }
        return remaining;
    }

    private static InventoryHostDescriptor resolveHost(ServerPlayer player) {
        AbstractContainerMenu menu = player.containerMenu;
        if (menu == null) {
            return null;
        }
        return InventoryHostResolver.resolve(new InventoryHostContext(
                menu,
                player.getInventory(),
                Component.literal("SLOT CarriedSourceAccess"),
                NeoForgeCarriedSourceAccess.class.getName(),
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
