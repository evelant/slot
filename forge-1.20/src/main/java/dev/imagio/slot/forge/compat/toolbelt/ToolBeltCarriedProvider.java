package dev.imagio.slot.forge.compat.toolbelt;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.storage.CarriedProvider;
import dev.imagio.slot.inventory.storage.CarriedSourceAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
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
