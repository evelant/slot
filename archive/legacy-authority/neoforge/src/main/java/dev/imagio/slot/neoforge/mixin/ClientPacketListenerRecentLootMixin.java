package dev.imagio.slot.neoforge.mixin;

import dev.imagio.slot.neoforge.client.SlotRecentLootHooks;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundAwardStatsPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
abstract class ClientPacketListenerRecentLootMixin {
    @Inject(method = "handleTakeItemEntity", at = @At("HEAD"))
    private void slot$recordRecentWorldPickup(ClientboundTakeItemEntityPacket packet, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || packet.getAmount() <= 0 || player.getId() != packet.getPlayerId() || minecraft.level == null) {
            return;
        }

        if (!(minecraft.level.getEntity(packet.getItemId()) instanceof ItemEntity itemEntity)) {
            return;
        }

        ItemStack pickedUpStack = itemEntity.getItem().copy();
        if (pickedUpStack.isEmpty()) {
            return;
        }

        SlotRecentLootHooks.recordClientPickup(pickedUpStack, packet.getAmount());
    }

    @Inject(method = "handleAwardStats", at = @At("HEAD"))
    private void slot$observePickupStats(ClientboundAwardStatsPacket packet, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || packet == null) {
            return;
        }

        for (var entry : packet.stats().object2IntEntrySet()) {
            Stat<?> stat = entry.getKey();
            if (stat == null || stat.getType() != Stats.ITEM_PICKED_UP) {
                continue;
            }

            Object value = stat.getValue();
            if (!(value instanceof Item item)) {
                continue;
            }

            int previous = player.getStats().getValue(stat);
            int current = entry.getIntValue();
            int acquiredCount = current - previous;
            if (acquiredCount > 0) {
                SlotRecentLootHooks.onPickupStatAwarded(item, acquiredCount);
            }
        }
    }
}
