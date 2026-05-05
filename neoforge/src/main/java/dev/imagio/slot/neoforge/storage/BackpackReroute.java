package dev.imagio.slot.neoforge.storage;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.storage.CarriedSourceAccess;
import dev.imagio.slot.inventory.storage.StorageAccessRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

// Shared routing helper: takes items that just landed in a player's
// vanilla lanes (main / hotbar / offhand) and migrates them into any
// registered CarriedProvider (backpacks, etc.). Used by SlotPickupRouter
// for world pickups and by SlotShiftCraftMixin for shift-click on
// crafting result slots — both code paths share the same goal:
// backpack-first routing per AGENTS.md.
public final class BackpackReroute {
    private BackpackReroute() {
    }

    // template: a stack carrying the identity (item + components) of the
    //   items that just landed in vanilla lanes. Count on `template`
    //   itself is ignored — `count` is authoritative.
    // count: the number of items to migrate from vanilla lanes into
    //   providers.
    // Returns the number of items actually absorbed by providers (and
    // therefore extracted from vanilla lanes); zero if nothing was
    // absorbed (no providers, providers full, or registry not installed).
    public static int routeToBackpack(ServerPlayer player, ItemStack template, int count) {
        if (count <= 0 || template.isEmpty()) {
            return 0;
        }
        if (!StorageAccessRegistry.isInstalled()) {
            return 0;
        }

        CarriedSourceAccess carried = StorageAccessRegistry.carriedSourceAccess();
        ItemStack toRoute = template.copy();
        toRoute.setCount(count);

        ItemStack remainder;
        try {
            remainder = carried.insertIntoProviders(player, toRoute, false);
        } catch (RuntimeException failure) {
            SlotDebugLog.log(
                    "BackpackReroute insertIntoProviders threw {}; leaving items in main inventory",
                    failure.toString()
            );
            return 0;
        }
        int absorbed = count - (remainder == null ? 0 : remainder.getCount());
        if (absorbed <= 0) {
            return 0;
        }

        ItemIdentity identity = ItemIdentityMatcher.create(template);
        shrinkVanillaLanes(carried, player, identity, absorbed);
        return absorbed;
    }

    // Remove `count` items matching `identity` from the player's vanilla
    // lanes (main / hotbar / offhand). We don't track which slot vanilla
    // landed the items in — they may have merged across multiple existing
    // stacks — so walk every match on builtin lanes and extract until the
    // absorbed count is covered. Provider-owned source ids are skipped:
    // those are where the items were just routed *to*.
    private static void shrinkVanillaLanes(CarriedSourceAccess carried, ServerPlayer player,
                                           ItemIdentity identity, int count) {
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
