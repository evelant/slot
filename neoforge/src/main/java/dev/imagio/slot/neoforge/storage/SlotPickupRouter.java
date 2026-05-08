package dev.imagio.slot.neoforge.storage;

import dev.imagio.slot.inventory.session.InventoryAcquisitionActivityRecorder;
import dev.imagio.slot.neoforge.workflow.SlotPlayerWorkflowRuntimeService;
import dev.imagio.slot.workflow.domain.InventoryActivityConfidence;
import dev.imagio.slot.workflow.domain.InventoryActivityProducer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
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
        NeoForge.EVENT_BUS.addListener(SlotPickupRouter::onItemPickupPost);
        registered = true;
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

        InventoryAcquisitionActivityRecorder.recordStackAcquired(
                SlotPlayerWorkflowRuntimeService.runtime(player),
                original,
                pickedCount,
                InventoryActivityProducer.WORLD_PICKUP,
                InventoryActivityConfidence.AUTHORITATIVE,
                "world_pickup");
        NeoForgeCarriedActivityTracker.suppressNext(player);
        BackpackReroute.routeToBackpack(player, original, pickedCount);
    }
}
