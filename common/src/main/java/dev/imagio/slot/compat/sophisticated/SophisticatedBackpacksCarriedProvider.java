package dev.imagio.slot.compat.sophisticated;

import dev.imagio.slot.inventory.core.InventoryCapability;
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
import java.util.Set;
import java.util.UUID;

/**
 * Shared {@link CarriedProvider} for Sophisticated Backpacks carried sources.
 * The implementation stays on the reflection-based compat layer so Forge
 * 1.20 and NeoForge 1.21 register the same source ids and mutation behavior.
 */
public final class SophisticatedBackpacksCarriedProvider implements CarriedProvider {
    public static final String PREFIX = "sophisticatedbackpacks:carried";
    private static final String CRAFTING_SOURCE_MARKER = "#crafting/";

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
        for (SophisticatedBackpackSupport.BackpackCraftingGridSnapshot snapshot :
                SophisticatedBackpackSupport.readPlayerBackpackCraftingGrids(player, null)) {
            if (snapshot == null || snapshot.stableContainerId().isBlank() || snapshot.entries().isEmpty()) {
                continue;
            }
            ids.add(craftingSourceId(snapshot.stableContainerId(), snapshot.upgradeSlotIndex()));
        }
        return List.copyOf(ids);
    }

    @Override
    public ItemStack peek(Player player, String sourceId, int slotIndex) {
        if (slotIndex < 0) {
            return ItemStack.EMPTY;
        }
        if (isCraftingSource(sourceId)) {
            SophisticatedBackpackSupport.BackpackCraftingGridSnapshot snapshot = craftingGridFor(player, sourceId);
            if (snapshot == null || slotIndex >= snapshot.slotCount()) {
                return ItemStack.EMPTY;
            }
            for (SophisticatedBackpackSupport.BackpackCraftingEntry entry : snapshot.entries()) {
                if (entry.slotIndex() == slotIndex) {
                    return entry.stack().copy();
                }
            }
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
        if (isCraftingSource(sourceId)) {
            SophisticatedBackpackSupport.BackpackCraftingGridSnapshot snapshot = craftingGridFor(player, sourceId);
            if (snapshot == null || slotIndex >= snapshot.slotCount()) {
                return ItemStack.EMPTY;
            }
            return SophisticatedBackpackSupport.extractBackpackCraftingGridSlot(
                    player,
                    snapshot.carrier(),
                    snapshot.upgradeSlotIndex(),
                    slotIndex,
                    amount,
                    simulate);
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
        if (isCraftingSource(sourceId)) {
            return stack;
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
        if (isCraftingSource(sourceId)) {
            SophisticatedBackpackSupport.BackpackCraftingGridSnapshot snapshot = craftingGridFor(player, sourceId);
            return snapshot == null ? 0 : snapshot.slotCount();
        }
        SophisticatedBackpackSupport.BackpackInventorySnapshot snapshot = snapshotFor(player, sourceId);
        return snapshot == null ? 0 : snapshot.slotCount();
    }

    @Override
    public Set<InventoryCapability> capabilities(Player player, String sourceId) {
        return isCraftingSource(sourceId)
                ? Set.of(InventoryCapability.EXTRACT)
                : Set.of(InventoryCapability.INSERT, InventoryCapability.EXTRACT);
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
        for (SophisticatedBackpackSupport.BackpackCraftingGridSnapshot snapshot :
                SophisticatedBackpackSupport.readPlayerBackpackCraftingGrids(player, null)) {
            String sourceId = craftingSourceId(snapshot.stableContainerId(), snapshot.upgradeSlotIndex());
            for (SophisticatedBackpackSupport.BackpackCraftingEntry entry : snapshot.entries()) {
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
        for (SophisticatedBackpackSupport.BackpackCraftingGridSnapshot snapshot :
                SophisticatedBackpackSupport.readPlayerBackpackCraftingGrids(player, null)) {
            String sourceId = craftingSourceId(snapshot.stableContainerId(), snapshot.upgradeSlotIndex());
            for (SophisticatedBackpackSupport.BackpackCraftingEntry entry : snapshot.entries()) {
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

    private static SophisticatedBackpackSupport.BackpackCraftingGridSnapshot craftingGridFor(Player player, String sourceId) {
        if (player == null || sourceId == null || sourceId.isBlank()) {
            return null;
        }
        String stableId = stableIdSuffix(sourceId);
        int upgradeSlotIndex = craftingUpgradeSlotIndex(sourceId);
        if (stableId.isBlank() || upgradeSlotIndex < 0) {
            return null;
        }
        for (SophisticatedBackpackSupport.BackpackCraftingGridSnapshot snapshot :
                SophisticatedBackpackSupport.readPlayerBackpackCraftingGrids(player, null)) {
            if (snapshot != null
                    && stableId.equals(snapshot.stableContainerId())
                    && upgradeSlotIndex == snapshot.upgradeSlotIndex()) {
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
        String suffix = sourceId.substring(slash + 1);
        int craftingMarker = suffix.indexOf(CRAFTING_SOURCE_MARKER);
        return craftingMarker < 0 ? suffix : suffix.substring(0, craftingMarker);
    }

    private static boolean isCraftingSource(String sourceId) {
        return sourceId != null && sourceId.startsWith(PREFIX + "/") && sourceId.contains(CRAFTING_SOURCE_MARKER);
    }

    private static int craftingUpgradeSlotIndex(String sourceId) {
        if (!isCraftingSource(sourceId)) {
            return -1;
        }
        int marker = sourceId.lastIndexOf(CRAFTING_SOURCE_MARKER);
        if (marker < 0 || marker + CRAFTING_SOURCE_MARKER.length() >= sourceId.length()) {
            return -1;
        }
        try {
            return Integer.parseInt(sourceId.substring(marker + CRAFTING_SOURCE_MARKER.length()));
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private static String sourceId(SophisticatedBackpackSupport.BackpackInventorySnapshot snapshot) {
        return PREFIX + "/" + (snapshot == null ? "" : snapshot.stableContainerId());
    }

    private static String craftingSourceId(String stableContainerId, int upgradeSlotIndex) {
        return PREFIX + "/" + (stableContainerId == null ? "" : stableContainerId)
                + CRAFTING_SOURCE_MARKER
                + Math.max(0, upgradeSlotIndex);
    }

    private static Map<UUID, CompoundTag> syncedContents() {
        return new LinkedHashMap<>();
    }
}
