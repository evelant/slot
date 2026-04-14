package dev.imagio.slot.neoforge.client;

import dev.imagio.slot.client.screen.RecentLootTracker;
import dev.imagio.slot.client.screen.SlotPanelScreen;
import dev.imagio.slot.compat.sophisticated.SophisticatedBackpackSupport;
import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.client.policy.ItemBehaviorPolicy;
import dev.imagio.slot.client.session.SlotScreenSessionResolver;
import dev.imagio.slot.network.BackpackContentsRequestRequester;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

import java.util.LinkedHashMap;
import java.util.Map;

public final class SlotRecentLootHooks {
    private SlotRecentLootHooks() {
    }

    public static void onItemPickup(ItemEntityPickupEvent.Post event) {
        if (!isLocalClientPlayer(event.getPlayer())) {
            return;
        }

        recordClientPickup(event.getOriginalStack(), event.getOriginalStack().getCount() - event.getCurrentStack().getCount());
    }

    public static void recordClientPickup(ItemStack stack, int acquiredCount) {
        if (stack == null || stack.isEmpty() || acquiredCount <= 0) {
            return;
        }

        RecentLootTracker.recordAcquired(stack, acquiredCount);
        Minecraft minecraft = Minecraft.getInstance();
        if (SophisticatedBackpackSupport.isAvailable()
                && minecraft.player != null
                && SophisticatedBackpackSupport.countPlayerBackpackSlots(minecraft.player) > 0) {
            BackpackContentsRequestRequester.requestSync();
        }
        if (minecraft.screen instanceof SlotPanelScreen slotPanelScreen) {
            slotPanelScreen.slotRefreshContents();
        }
    }

    public static void onPickupStatAwarded(Item item, int acquiredCount) {
        if (item == null || acquiredCount <= 0) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ItemIdentity identity = ItemIdentity.of(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).toString());
        if (SophisticatedBackpackSupport.isAvailable()
                && minecraft.player != null
                && SophisticatedBackpackSupport.countPlayerBackpackSlots(minecraft.player) > 0) {
            RecentLootTracker.expectBackpackAcquisition(identity);
            BackpackContentsRequestRequester.requestSync();
            return;
        }
        RecentLootTracker.recordIdentity(identity);
    }

    public static void onBackpackContentsSynced() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            RecentLootTracker.observeBackpackSnapshot(Map.of(), false);
            return;
        }

        RecentLootTracker.observeBackpackSnapshot(buildBackpackSnapshot(player), shouldRecordRecentLoot(minecraft, player));
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            RecentLootTracker.observeVanillaSnapshot(Map.of(), false);
            RecentLootTracker.observeBackpackSnapshot(Map.of(), false);
            return;
        }

        boolean recordRecentLoot = shouldRecordRecentLoot(minecraft, player);
        boolean vanillaSnapshotChanged = RecentLootTracker.observeVanillaSnapshot(buildVanillaSnapshot(player), recordRecentLoot);
        boolean backpackSnapshotChanged = RecentLootTracker.observeBackpackSnapshot(buildBackpackSnapshot(player), recordRecentLoot);
        if (minecraft.screen instanceof SlotPanelScreen slotPanelScreen
                && (vanillaSnapshotChanged || backpackSnapshotChanged)) {
            slotPanelScreen.slotRefreshContents();
        }
    }

    private static boolean shouldRecordRecentLoot(Minecraft minecraft, LocalPlayer player) {
        return SlotScreenSessionResolver.recordsRecentLoot(minecraft, player);
    }

    private static boolean isLocalClientPlayer(Player player) {
        return player != null
                && player.level().isClientSide()
                && Minecraft.getInstance().player != null
                && player.getUUID().equals(Minecraft.getInstance().player.getUUID());
    }

    private static Map<ItemIdentity, Integer> buildVanillaSnapshot(LocalPlayer player) {
        Map<ItemIdentity, Integer> snapshot = new LinkedHashMap<>();
        if (player == null) {
            return Map.of();
        }

        addStacks(snapshot, player.getInventory().items);
        addStacks(snapshot, player.getInventory().armor);
        addStacks(snapshot, player.getInventory().offhand);
        return Map.copyOf(snapshot);
    }

    private static Map<ItemIdentity, Integer> buildBackpackSnapshot(LocalPlayer player) {
        Map<ItemIdentity, Integer> snapshot = new LinkedHashMap<>();
        if (player == null) {
            return Map.of();
        }

        for (SophisticatedBackpackSupport.BackpackSlotStack backpackStack : SophisticatedBackpackSupport.readPlayerBackpackStacks(player)) {
            ItemStack stack = backpackStack.stack();
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            snapshot.merge(ItemBehaviorPolicy.createIdentity(stack), stack.getCount(), Integer::sum);
        }
        return Map.copyOf(snapshot);
    }

    private static void addStacks(Map<ItemIdentity, Integer> snapshot, Iterable<ItemStack> stacks) {
        if (snapshot == null || stacks == null) {
            return;
        }
        for (ItemStack stack : stacks) {
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            snapshot.merge(ItemBehaviorPolicy.createIdentity(stack), stack.getCount(), Integer::sum);
        }
    }
}
