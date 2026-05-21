package dev.imagio.slot.neoforge.storage;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.inventory.session.InventoryAcquisitionActivityRecorder;
import dev.imagio.slot.inventory.storage.BackpackReroute;
import dev.imagio.slot.inventory.storage.CarriedInventoryRevisions;
import dev.imagio.slot.inventory.workspace.WorkspaceTrashCommandService;
import dev.imagio.slot.neoforge.workflow.SlotPlayerWorkflowRuntimeService;
import dev.imagio.slot.workflow.domain.InventoryActivityConfidence;
import dev.imagio.slot.workflow.domain.InventoryActivityProducer;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

// Server-side world-pickup interceptor. Runs after vanilla pickup finishes
// — items have landed in the main inventory, which is where we want them
// briefly: vanilla pickup animation/sounds fire, quests and advancements
// trigger, pickup-tooltip mods see the event. We then migrate the picked
// items out of vanilla lanes into any registered CarriedProvider via
// BackpackReroute so main doesn't fill up. The Sophisticated Backpacks
// magnet upgrade intercepts items pre-pickup (bypassing those sibling
// side-effects), which is a usability loss we explicitly avoid here.
public final class SlotPickupRouter {
    private static boolean registered;

    private SlotPickupRouter() {
    }

    public static void init() {
        if (registered) {
            return;
        }
        NeoForge.EVENT_BUS.addListener(SlotPickupRouter::onItemPickupPre);
        NeoForge.EVENT_BUS.addListener(SlotPickupRouter::onItemPickupPost);
        registered = true;
    }

    private static void onItemPickupPre(ItemEntityPickupEvent.Pre event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack incoming = event.getItemEntity().getItem();
        if (incoming == null || incoming.isEmpty()) {
            return;
        }
        SlotDebugLog.verboseLog(
                "NeoForge pickup pre hook item={} count={}",
                itemDescription(incoming),
                incoming.getCount());
        WorkflowDomainRuntime runtime = SlotPlayerWorkflowRuntimeService.runtime(player);
        WorkspaceTrashCommandService.PickupOverflowTrashResult result =
                WorkspaceTrashCommandService.trashOverflowBeforePickup(
                        player,
                        runtime,
                        incoming,
                        incoming.getCount());
        if (result.incomingTrashed() > 0) {
            SlotDebugLog.log(
                    "NeoForge pickup pre voiding incoming junk item={} count={} carriedTrashed={}",
                    itemDescription(incoming),
                    result.incomingTrashed(),
                    result.carriedTrashed());
            event.getItemEntity().discard();
            event.setCanPickup(TriState.FALSE);
        } else if (result.carriedTrashed() > 0) {
            SlotDebugLog.log(
                    "NeoForge pickup pre swept carried junk triggerItem={} carriedTrashed={}",
                    itemDescription(incoming),
                    result.carriedTrashed());
        }
    }

    private static void onItemPickupPost(ItemEntityPickupEvent.Post event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }

        ItemStack original = event.getOriginalStack();
        ItemStack leftOver = event.getCurrentStack();
        int pickedCount = original.getCount() - (leftOver == null ? 0 : leftOver.getCount());
        if (pickedCount <= 0 || original.isEmpty()) {
            return;
        }
        SlotDebugLog.verboseLog(
                "NeoForge pickup post hook item={} pickedCount={} original={} leftover={}",
                itemDescription(original),
                pickedCount,
                original.getCount(),
                leftOver == null ? 0 : leftOver.getCount());

        WorkflowDomainRuntime runtime = SlotPlayerWorkflowRuntimeService.runtime(player);
        InventoryAcquisitionActivityRecorder.recordStackAcquired(
                runtime,
                original,
                pickedCount,
                InventoryActivityProducer.WORLD_PICKUP,
                InventoryActivityConfidence.AUTHORITATIVE,
                "world_pickup");
        CarriedInventoryRevisions.markChanged(player, "world_pickup");
        WorkspaceTrashCommandService.PostPickupOverflowTrashResult trashResult =
                WorkspaceTrashCommandService.trashOverflowPickup(player, runtime, original, pickedCount);
        NeoForgeCarriedActivityTracker.suppressAcquired(player, original, pickedCount);
        int remaining = pickedCount - trashResult.pickedTrashed();
        int routed = BackpackReroute.routeToBackpack(player, original, remaining);
        if (trashResult.carriedTrashed() > 0) {
            SlotDebugLog.log(
                    "NeoForge pickup post result item={} pickedCount={} totalTrashed={} pickedTrashed={} remaining={} routed={}",
                    itemDescription(original),
                    pickedCount,
                    trashResult.carriedTrashed(),
                    trashResult.pickedTrashed(),
                    remaining,
                    routed);
        } else {
            SlotDebugLog.verboseLog(
                    "NeoForge pickup post result item={} pickedCount={} totalTrashed={} pickedTrashed={} remaining={} routed={}",
                    itemDescription(original),
                    pickedCount,
                    trashResult.carriedTrashed(),
                    trashResult.pickedTrashed(),
                    remaining,
                    routed);
        }
    }

    private static String itemDescription(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "empty";
        }
        return stack.getItem().builtInRegistryHolder().key().location().toString();
    }
}
