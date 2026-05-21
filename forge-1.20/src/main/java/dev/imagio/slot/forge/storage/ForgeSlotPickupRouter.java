package dev.imagio.slot.forge.storage;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.compat.sophisticated.SophisticatedBackpackSupport;
import dev.imagio.slot.compat.sophisticated.SophisticatedBackpackTransferSupport;
import dev.imagio.slot.forge.workflow.ForgePlayerWorkflowRuntimeService;
import dev.imagio.slot.inventory.session.InventoryAcquisitionActivityRecorder;
import dev.imagio.slot.inventory.storage.BackpackReroute;
import dev.imagio.slot.inventory.storage.CarriedInventoryRevisions;
import dev.imagio.slot.inventory.workspace.WorkspaceTrashCommandService;
import dev.imagio.slot.workflow.domain.InventoryActivityConfidence;
import dev.imagio.slot.workflow.domain.InventoryActivityProducer;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

public final class ForgeSlotPickupRouter {
    private static boolean registered;

    private ForgeSlotPickupRouter() {
    }

    public static void init() {
        if (registered) {
            return;
        }
        MinecraftForge.EVENT_BUS.addListener(ForgeSlotPickupRouter::onItemPickupPre);
        MinecraftForge.EVENT_BUS.addListener(ForgeSlotPickupRouter::onItemPickupPost);
        registered = true;
    }

    private static void onItemPickupPre(EntityItemPickupEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack incoming = event.getItem().getItem();
        if (incoming == null || incoming.isEmpty()) {
            return;
        }
        SlotDebugLog.verboseLog(
                "Forge pickup pre hook item={} count={}",
                itemDescription(incoming),
                incoming.getCount());
        WorkflowDomainRuntime runtime = ForgePlayerWorkflowRuntimeService.runtime(player);
        WorkspaceTrashCommandService.PickupOverflowTrashResult result =
                WorkspaceTrashCommandService.trashOverflowBeforePickup(
                        player,
                        runtime,
                        incoming,
                        incoming.getCount());
        if (result.incomingTrashed() > 0) {
            SlotDebugLog.log(
                    "Forge pickup pre voiding incoming junk item={} count={} carriedTrashed={}",
                    itemDescription(incoming),
                    result.incomingTrashed(),
                    result.carriedTrashed());
            event.getItem().discard();
            event.setCanceled(true);
        } else if (result.carriedTrashed() > 0) {
            SlotDebugLog.log(
                    "Forge pickup pre swept carried junk triggerItem={} carriedTrashed={}",
                    itemDescription(incoming),
                    result.carriedTrashed());
        }
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
        SlotDebugLog.verboseLog(
                "Forge pickup post hook item={} pickedCount={}",
                itemDescription(picked),
                pickedCount);

        WorkflowDomainRuntime runtime = ForgePlayerWorkflowRuntimeService.runtime(player);
        InventoryAcquisitionActivityRecorder.recordStackAcquired(
                runtime,
                picked,
                pickedCount,
                InventoryActivityProducer.WORLD_PICKUP,
                InventoryActivityConfidence.AUTHORITATIVE,
                "world_pickup");
        CarriedInventoryRevisions.markChanged(player, "world_pickup");
        WorkspaceTrashCommandService.PostPickupOverflowTrashResult trashResult =
                WorkspaceTrashCommandService.trashOverflowPickup(player, runtime, picked, pickedCount);
        ForgeCarriedActivityTracker.suppressAcquired(player, picked, pickedCount);
        int remaining = pickedCount - trashResult.pickedTrashed();
        int routed = BackpackReroute.routeToBackpack(player, picked, remaining);
        if (trashResult.carriedTrashed() > 0) {
            SlotDebugLog.log(
                    "Forge pickup post result item={} pickedCount={} totalTrashed={} pickedTrashed={} remaining={} routed={}",
                    itemDescription(picked),
                    pickedCount,
                    trashResult.carriedTrashed(),
                    trashResult.pickedTrashed(),
                    remaining,
                    routed);
        } else {
            SlotDebugLog.verboseLog(
                    "Forge pickup post result item={} pickedCount={} totalTrashed={} pickedTrashed={} remaining={} routed={}",
                    itemDescription(picked),
                    pickedCount,
                    trashResult.carriedTrashed(),
                    trashResult.pickedTrashed(),
                    remaining,
                    routed);
        }
        if (remaining > 0 && routed <= 0) {
            logUnroutedPickup(player, picked, remaining);
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
