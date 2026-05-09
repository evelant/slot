package dev.imagio.slot.forge.storage;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.compat.sophisticated.SophisticatedBackpackSupport;
import dev.imagio.slot.compat.sophisticated.SophisticatedBackpackTransferSupport;
import dev.imagio.slot.forge.workflow.ForgePlayerWorkflowRuntimeService;
import dev.imagio.slot.inventory.session.InventoryAcquisitionActivityRecorder;
import dev.imagio.slot.inventory.storage.BackpackReroute;
import dev.imagio.slot.workflow.domain.InventoryActivityConfidence;
import dev.imagio.slot.workflow.domain.InventoryActivityProducer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;

public final class ForgeSlotPickupRouter {
    private static boolean registered;

    private ForgeSlotPickupRouter() {
    }

    public static void init() {
        if (registered) {
            return;
        }
        MinecraftForge.EVENT_BUS.addListener(ForgeSlotPickupRouter::onItemPickupPost);
        registered = true;
    }

    private static void onItemPickupPost(PlayerEvent.ItemPickupEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack picked = event.getStack();
        if (picked == null || picked.isEmpty()) {
            return;
        }
        int pickedCount = picked.getCount();
        if (pickedCount <= 0) {
            return;
        }

        InventoryAcquisitionActivityRecorder.recordStackAcquired(
                ForgePlayerWorkflowRuntimeService.runtime(player),
                picked,
                pickedCount,
                InventoryActivityProducer.WORLD_PICKUP,
                InventoryActivityConfidence.AUTHORITATIVE,
                "world_pickup");
        ForgeCarriedActivityTracker.suppressNext(player);
        int routed = BackpackReroute.routeToBackpack(player, picked, pickedCount);
        if (routed <= 0) {
            logUnroutedPickup(player, picked, pickedCount);
        }
    }

    private static void logUnroutedPickup(ServerPlayer player, ItemStack picked, int pickedCount) {
        if (!hasBackpackItem(player)) {
            return;
        }
        int readableBackpacks = SophisticatedBackpackSupport.readPlayerBackpacks(player, null).size();
        if (readableBackpacks <= 0) {
            SlotCommon.LOGGER.warn(
                    "[SLOT] Forge pickup backpack route failed: backpack item present but no readable backpack sources item={} count={} readAvailable={} transferAvailable={}",
                    itemDescription(picked),
                    pickedCount,
                    SophisticatedBackpackSupport.isAvailable(),
                    SophisticatedBackpackTransferSupport.isAvailable());
            return;
        }
        SlotCommon.LOGGER.info(
                "[SLOT] Forge pickup backpack route moved nothing item={} count={} readableBackpacks={} readAvailable={} transferAvailable={}",
                itemDescription(picked),
                pickedCount,
                readableBackpacks,
                SophisticatedBackpackSupport.isAvailable(),
                SophisticatedBackpackTransferSupport.isAvailable());
    }

    private static boolean hasBackpackItem(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        for (ItemStack stack : inventory.items) {
            if (isBackpackStack(stack)) {
                return true;
            }
        }
        for (ItemStack stack : inventory.armor) {
            if (isBackpackStack(stack)) {
                return true;
            }
        }
        for (ItemStack stack : inventory.offhand) {
            if (isBackpackStack(stack)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isBackpackStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (SophisticatedBackpackSupport.isBackpackItem(stack)) {
            return true;
        }
        String item = itemDescription(stack);
        return item.startsWith("sophisticatedbackpacks:") && item.contains("backpack");
    }

    private static String itemDescription(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "empty";
        }
        return stack.getItem().builtInRegistryHolder().key().location().toString();
    }
}
