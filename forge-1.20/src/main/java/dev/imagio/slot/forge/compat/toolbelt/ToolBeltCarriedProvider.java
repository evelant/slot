package dev.imagio.slot.forge.compat.toolbelt;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.storage.CarriedProvider;
import dev.imagio.slot.inventory.storage.CarriedSourceAccess;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ToolBeltCarriedProvider implements CarriedProvider {
    public static final String PREFIX = ToolBeltSupport.PREFIX;

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
        if (player == null || !ToolBeltSupport.isAvailable()) {
            return List.of();
        }
        return ToolBeltSupport.readPlayerBelts(player).stream()
                .map(ToolBeltSupport.BeltSnapshot::sourceId)
                .toList();
    }

    @Override
    public ItemStack peek(Player player, String sourceId, int slotIndex) {
        return ToolBeltSupport.peek(ToolBeltSupport.find(player, sourceId), slotIndex);
    }

    @Override
    public ItemStack extract(ServerPlayer player, String sourceId, int slotIndex, int amount, boolean simulate) {
        return ToolBeltSupport.extract(ToolBeltSupport.find(player, sourceId), slotIndex, amount, simulate);
    }

    @Override
    public ItemStack insert(ServerPlayer player, String sourceId, ItemStack stack, boolean simulate) {
        return ToolBeltSupport.insertInto(ToolBeltSupport.find(player, sourceId), stack, simulate);
    }

    @Override
    public ItemStack insertBestFit(ServerPlayer player, ItemStack stack, boolean simulate) {
        return ToolBeltSupport.insertBestFit(player, stack, simulate);
    }

    @Override
    public int slotCount(Player player, String sourceId) {
        ToolBeltSupport.BeltSnapshot snapshot = ToolBeltSupport.find(player, sourceId);
        return snapshot == null ? 0 : snapshot.slotCount();
    }

    public static Map<ItemIdentity, SlotWorkspaceViewModel.CarriedContainerInfo> carriedContainerInfoByIdentity(
            Player player
    ) {
        if (player == null || !ToolBeltSupport.isAvailable()) {
            return Map.of();
        }
        LinkedHashMap<ItemIdentity, int[]> byIdentity = new LinkedHashMap<>();
        for (ToolBeltSupport.BeltSnapshot snapshot : ToolBeltSupport.readPlayerBelts(player)) {
            if (snapshot == null || snapshot.carrierStack().isEmpty() || snapshot.slotCount() <= 0) {
                continue;
            }
            int occupied = 0;
            for (int slot = 0; slot < snapshot.slotCount(); slot++) {
                if (!ToolBeltSupport.peek(snapshot, slot).isEmpty()) {
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
        for (ToolBeltSupport.BeltSnapshot snapshot : ToolBeltSupport.readPlayerBelts(player)) {
            for (int slot = 0; slot < snapshot.slotCount(); slot++) {
                ItemStack stack = ToolBeltSupport.peek(snapshot, slot);
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
        for (ToolBeltSupport.BeltSnapshot snapshot : ToolBeltSupport.readPlayerBelts(player)) {
            for (int slot = 0; slot < snapshot.slotCount(); slot++) {
                ItemStack stack = ToolBeltSupport.peek(snapshot, slot);
                if (ItemIdentityMatcher.matchesMovable(stack, identity)) {
                    hits.add(new CarriedSourceAccess.CarriedLocation(snapshot.sourceId(), slot));
                }
            }
        }
        return List.copyOf(hits);
    }
}
