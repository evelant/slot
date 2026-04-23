package dev.imagio.slot.neoforge.storage;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.storage.CarriedSourceAccess;
import dev.imagio.slot.inventory.storage.StorageAccessRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

// Server-side pickup interceptor. Runs after vanilla pickup finishes — items
// have landed in the main inventory, which is where we want them briefly:
// vanilla pickup animation/sounds fire, quests and advancements trigger,
// pickup-tooltip mods see the event. We then route the picked items out of
// vanilla lanes into any registered CarriedProvider (Sophisticated Backpacks
// today, Curios/others in future) via CarriedSourceAccess.insertIntoProviders
// so main doesn't fill up. The Sophisticated Backpacks magnet upgrade
// intercepts items pre-pickup (bypassing those sibling side-effects), which
// is a usability loss we explicitly avoid here.
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
        if (!StorageAccessRegistry.isInstalled()) {
            return;
        }

        ItemStack original = event.getOriginalStack();
        ItemStack leftOver = event.getCurrentStack();
        int pickedCount = original.getCount() - (leftOver == null ? 0 : leftOver.getCount());
        if (pickedCount <= 0 || original.isEmpty()) {
            return;
        }

        CarriedSourceAccess carried = StorageAccessRegistry.carriedSourceAccess();
        ItemStack toRoute = original.copy();
        toRoute.setCount(pickedCount);

        ItemStack remainder;
        try {
            remainder = carried.insertIntoProviders(player, toRoute, false);
        } catch (RuntimeException failure) {
            SlotDebugLog.log(
                    "SlotPickupRouter insertIntoProviders threw {}; leaving pickup in main inventory",
                    failure.toString()
            );
            return;
        }
        int absorbed = pickedCount - (remainder == null ? 0 : remainder.getCount());
        if (absorbed <= 0) {
            return;
        }

        ItemIdentity identity = ItemIdentityMatcher.create(original);
        shrinkVanillaLanes(carried, player, identity, absorbed);
    }

    // Remove `count` items matching `identity` from the player's vanilla lanes
    // (main/hotbar/offhand). We don't track which slot vanilla pickup landed
    // in — pickup may merge across multiple existing stacks — so walk every
    // match on builtin lanes and extract until the absorbed count is covered.
    // We explicitly skip provider-owned source ids: those are where the items
    // were just routed to.
    private static void shrinkVanillaLanes(CarriedSourceAccess carried, ServerPlayer player, ItemIdentity identity, int count) {
        int remaining = count;
        for (CarriedSourceAccess.CarriedLocation loc : carried.findAllMatching(player, identity)) {
            if (remaining <= 0) {
                break;
            }
            if (!isBuiltinLane(loc.sourceId())) {
                continue;
            }
            ItemStack taken = carried.extract(player, loc.sourceId(), loc.slotIndex(), remaining, false);
            remaining -= taken.getCount();
        }
    }

    private static boolean isBuiltinLane(String sourceId) {
        return BuiltinInventoryIds.PLAYER_MAIN.equals(sourceId)
                || BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0.equals(sourceId)
                || BuiltinInventoryIds.PLAYER_OFFHAND.equals(sourceId);
    }
}
