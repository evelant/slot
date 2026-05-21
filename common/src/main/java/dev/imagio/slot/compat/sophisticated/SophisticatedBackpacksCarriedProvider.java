package dev.imagio.slot.compat.sophisticated;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.storage.CarriedProvider;
import dev.imagio.slot.inventory.storage.CarriedSourceAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Shared {@link CarriedProvider} for Sophisticated Backpacks carried sources.
 * The implementation stays on the reflection-based compat layer so Forge
 * 1.20 and NeoForge 1.21 register the same source ids and mutation behavior.
 */
public final class SophisticatedBackpacksCarriedProvider implements CarriedProvider {
    public static final String PREFIX = "sophisticatedbackpacks:carried";

    @Override
    public String prefix() {
        return PREFIX;
    }

    /**
     * Sophisticated Backpacks has a bespoke integration provider for its
     * openable menu and carried source descriptors. Auto-synthesis would
     * double-register those sources.
     */
    @Override
    public boolean autoSynthesizeExtension() {
        return false;
    }

    @Override
    public List<String> sourceIds(Player player) {
        if (player == null || !SophisticatedBackpackSupport.isAvailable()) {
            return List.of();
        }
        ArrayList<String> ids = new ArrayList<>();
        for (SophisticatedBackpackSupport.BackpackInventorySnapshot snapshot :
                SophisticatedBackpackSupport.readPlayerBackpacks(player, null)) {
            if (snapshot == null || snapshot.stableContainerId().isBlank()) {
                continue;
            }
            ids.add(PREFIX + "/" + snapshot.stableContainerId());
        }
        return List.copyOf(ids);
    }

    @Override
    public ItemStack peek(Player player, String sourceId, int slotIndex) {
        if (slotIndex < 0) {
            return ItemStack.EMPTY;
        }
        SophisticatedBackpackSupport.BackpackInventorySnapshot snapshot = snapshotFor(player, sourceId);
        if (snapshot == null || slotIndex >= snapshot.slotCount()) {
            return ItemStack.EMPTY;
        }
        for (SophisticatedBackpackSupport.BackpackEntry entry : snapshot.entries()) {
            if (entry.slotIndex() == slotIndex) {
                return entry.stack().copy();
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack extract(ServerPlayer player, String sourceId, int slotIndex, int amount, boolean simulate) {
        if (player == null || slotIndex < 0 || amount <= 0) {
            return ItemStack.EMPTY;
        }
        SophisticatedBackpackSupport.BackpackCarrierRef carrier = carrierFor(player, sourceId);
        if (carrier == null) {
            return ItemStack.EMPTY;
        }
        return SophisticatedBackpackTransferSupport.extractBackpackSlot(
                player,
                carrier,
                slotIndex,
                amount,
                simulate,
                syncedContents());
    }

    @Override
    public ItemStack insert(ServerPlayer player, String sourceId, ItemStack stack, boolean simulate) {
        if (player == null || stack == null || stack.isEmpty()) {
            return stack == null ? ItemStack.EMPTY : stack;
        }
        SophisticatedBackpackSupport.BackpackCarrierRef carrier = carrierFor(player, sourceId);
        if (carrier == null) {
            return stack;
        }
        if (simulate) {
            return SophisticatedBackpackTransferSupport.simulateInsertIntoBackpack(player, carrier, stack);
        }
        return SophisticatedBackpackTransferSupport.insertIntoBackpack(
                player,
                carrier,
                stack,
                syncedContents());
    }

    @Override
    public int slotCount(Player player, String sourceId) {
        SophisticatedBackpackSupport.BackpackInventorySnapshot snapshot = snapshotFor(player, sourceId);
        return snapshot == null ? 0 : snapshot.slotCount();
    }

    @Override
    public CarriedSourceAccess.CarriedStoragePressure carriedStoragePressure(Player player) {
        if (player == null || !SophisticatedBackpackSupport.isAvailable()) {
            return CarriedSourceAccess.CarriedStoragePressure.empty();
        }
        int capacity = 0;
        int occupied = 0;
        for (SophisticatedBackpackSupport.BackpackInventorySnapshot snapshot :
                SophisticatedBackpackSupport.readPlayerBackpacks(player, null)) {
            if (snapshot == null) {
                continue;
            }
            capacity += Math.max(0, snapshot.slotCount());
            occupied += (int) snapshot.entries().stream()
                    .filter(entry -> entry != null && entry.stack() != null && !entry.stack().isEmpty())
                    .count();
        }
        return new CarriedSourceAccess.CarriedStoragePressure(capacity, occupied);
    }

    @Override
    public Optional<CarriedSourceAccess.CarriedLocation> findIdentity(Player player, ItemIdentity identity) {
        if (player == null || identity == null) {
            return Optional.empty();
        }
        for (SophisticatedBackpackSupport.BackpackInventorySnapshot snapshot :
                SophisticatedBackpackSupport.readPlayerBackpacks(player, null)) {
            String sourceId = sourceId(snapshot);
            for (SophisticatedBackpackSupport.BackpackEntry entry : snapshot.entries()) {
                if (ItemIdentityMatcher.matchesMovable(entry.stack(), identity)) {
                    return Optional.of(new CarriedSourceAccess.CarriedLocation(sourceId, entry.slotIndex()));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<CarriedSourceAccess.CarriedLocation> findAllMatching(Player player, ItemIdentity identity) {
        if (player == null || identity == null) {
            return List.of();
        }
        ArrayList<CarriedSourceAccess.CarriedLocation> hits = new ArrayList<>();
        for (SophisticatedBackpackSupport.BackpackInventorySnapshot snapshot :
                SophisticatedBackpackSupport.readPlayerBackpacks(player, null)) {
            String sourceId = sourceId(snapshot);
            for (SophisticatedBackpackSupport.BackpackEntry entry : snapshot.entries()) {
                if (ItemIdentityMatcher.matchesMovable(entry.stack(), identity)) {
                    hits.add(new CarriedSourceAccess.CarriedLocation(sourceId, entry.slotIndex()));
                }
            }
        }
        return List.copyOf(hits);
    }

    private static SophisticatedBackpackSupport.BackpackInventorySnapshot snapshotFor(Player player, String sourceId) {
        if (player == null || sourceId == null || sourceId.isBlank()) {
            return null;
        }
        String stableId = stableIdSuffix(sourceId);
        if (stableId.isBlank()) {
            return null;
        }
        for (SophisticatedBackpackSupport.BackpackInventorySnapshot snapshot :
                SophisticatedBackpackSupport.readPlayerBackpacks(player, null)) {
            if (snapshot != null && stableId.equals(snapshot.stableContainerId())) {
                return snapshot;
            }
        }
        return null;
    }

    private static SophisticatedBackpackSupport.BackpackCarrierRef carrierFor(Player player, String sourceId) {
        SophisticatedBackpackSupport.BackpackInventorySnapshot snapshot = snapshotFor(player, sourceId);
        return snapshot == null ? null : snapshot.carrier();
    }

    private static String stableIdSuffix(String sourceId) {
        int slash = sourceId == null ? -1 : sourceId.indexOf('/', PREFIX.length());
        if (slash < 0 || slash + 1 >= sourceId.length()) {
            return "";
        }
        return sourceId.substring(slash + 1);
    }

    private static String sourceId(SophisticatedBackpackSupport.BackpackInventorySnapshot snapshot) {
        return PREFIX + "/" + (snapshot == null ? "" : snapshot.stableContainerId());
    }

    private static Map<UUID, CompoundTag> syncedContents() {
        return new LinkedHashMap<>();
    }
}
