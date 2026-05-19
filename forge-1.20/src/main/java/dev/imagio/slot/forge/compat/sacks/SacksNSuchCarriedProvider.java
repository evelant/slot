package dev.imagio.slot.forge.compat.sacks;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.inventory.storage.CarriedProvider;
import dev.imagio.slot.inventory.storage.CarriedSourceAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class SacksNSuchCarriedProvider implements CarriedProvider {
    public static final String PREFIX = SacksNSuchSupport.PREFIX;

    @Override
    public String prefix() {
        return PREFIX;
    }

    @Override
    public boolean autoSynthesizeExtension() {
        return false;
    }

    @Override
    public List<String> sourceIds(Player player) {
        if (player == null || !SacksNSuchSupport.isAvailable()) {
            return List.of();
        }
        return SacksNSuchSupport.readPlayerContainers(player).stream()
                .map(SacksNSuchSupport.ContainerSnapshot::sourceId)
                .toList();
    }

    @Override
    public ItemStack peek(Player player, String sourceId, int slotIndex) {
        return SacksNSuchSupport.peek(SacksNSuchSupport.find(player, sourceId), slotIndex);
    }

    @Override
    public ItemStack extract(ServerPlayer player, String sourceId, int slotIndex, int amount, boolean simulate) {
        return SacksNSuchSupport.extract(SacksNSuchSupport.find(player, sourceId), slotIndex, amount, simulate);
    }

    @Override
    public ItemStack insert(ServerPlayer player, String sourceId, ItemStack stack, boolean simulate) {
        return SacksNSuchSupport.insertInto(SacksNSuchSupport.find(player, sourceId), stack, simulate);
    }

    @Override
    public ItemStack insertBestFit(ServerPlayer player, ItemStack stack, boolean simulate) {
        return SacksNSuchSupport.insertBestFit(player, stack, simulate);
    }

    @Override
    public int slotCount(Player player, String sourceId) {
        SacksNSuchSupport.ContainerSnapshot snapshot = SacksNSuchSupport.find(player, sourceId);
        return snapshot == null ? 0 : snapshot.slotCount();
    }

    public static Map<ItemIdentity, SlotWorkspaceViewModel.CarriedContainerInfo> carriedContainerInfoByIdentity(
            Player player
    ) {
        if (player == null || !SacksNSuchSupport.isAvailable()) {
            return Map.of();
        }
        LinkedHashMap<ItemIdentity, int[]> byIdentity = new LinkedHashMap<>();
        for (SacksNSuchSupport.ContainerSnapshot snapshot : SacksNSuchSupport.readPlayerContainers(player)) {
            if (snapshot == null || snapshot.carrierStack().isEmpty() || snapshot.slotCount() <= 0) {
                continue;
            }
            int occupied = 0;
            for (int slot = 0; slot < snapshot.slotCount(); slot++) {
                if (!SacksNSuchSupport.peek(snapshot, slot).isEmpty()) {
                    occupied++;
                }
            }
            ItemIdentity identity = ItemIdentityMatcher.normalizeMovable(
                    ItemIdentityMatcher.create(snapshot.carrierStack()));
            int[] running = byIdentity.computeIfAbsent(identity, ignored -> new int[2]);
            running[0] += Math.max(0, snapshot.slotCount() - occupied);
            running[1] += snapshot.slotCount();
        }
        LinkedHashMap<ItemIdentity, SlotWorkspaceViewModel.CarriedContainerInfo> result =
                new LinkedHashMap<>(byIdentity.size());
        byIdentity.forEach((identity, counts) ->
                result.put(identity, new SlotWorkspaceViewModel.CarriedContainerInfo(counts[0], counts[1])));
        return result.isEmpty() ? Map.of() : Map.copyOf(result);
    }

    @Override
    public Optional<CarriedSourceAccess.CarriedLocation> findIdentity(Player player, ItemIdentity identity) {
        if (player == null || identity == null) {
            return Optional.empty();
        }
        for (SacksNSuchSupport.ContainerSnapshot snapshot : SacksNSuchSupport.readPlayerContainers(player)) {
            for (int slot = 0; slot < snapshot.slotCount(); slot++) {
                ItemStack stack = SacksNSuchSupport.peek(snapshot, slot);
                if (ItemIdentityMatcher.matchesMovable(stack, identity)) {
                    return Optional.of(new CarriedSourceAccess.CarriedLocation(snapshot.sourceId(), slot));
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
        for (SacksNSuchSupport.ContainerSnapshot snapshot : SacksNSuchSupport.readPlayerContainers(player)) {
            for (int slot = 0; slot < snapshot.slotCount(); slot++) {
                ItemStack stack = SacksNSuchSupport.peek(snapshot, slot);
                if (ItemIdentityMatcher.matchesMovable(stack, identity)) {
                    hits.add(new CarriedSourceAccess.CarriedLocation(snapshot.sourceId(), slot));
                }
            }
        }
        return List.copyOf(hits);
    }
}
