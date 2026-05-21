package dev.imagio.slot.inventory.storage;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.core.ItemStackEquivalence;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared backpack-first overflow routing for items that just landed in
 * vanilla carried lanes. Platform hooks detect the event; this helper owns
 * the common "insert into providers, then shrink vanilla lanes" behavior.
 */
public final class BackpackReroute {
    private BackpackReroute() {
    }

    /**
     * @param template stack carrying the identity of the just-arrived items;
     *                 its count is ignored in favor of {@code count}
     * @param count    authoritative number of items to migrate
     * @return count absorbed by registered carried providers
     */
    public static int routeToBackpack(ServerPlayer player, ItemStack template, int count) {
        if (player == null || count <= 0 || template == null || template.isEmpty()) {
            return 0;
        }
        if (!StorageAccessRegistry.isInstalled()) {
            SlotCommon.LOGGER.warn(
                    "[SLOT] BackpackReroute unavailable: storage access is not installed item={} count={}",
                    itemId(template),
                    count);
            return 0;
        }

        CarriedSourceAccess carried = StorageAccessRegistry.carriedSourceAccess();
        ItemStack toRoute = template.copy();
        toRoute.setCount(count);
        List<CarriedProvider> providers = CarriedProviderRegistry.all();
        if (providers.isEmpty()) {
            SlotCommon.LOGGER.warn(
                    "[SLOT] BackpackReroute skipped: no carried providers registered item={} count={}",
                    itemId(template),
                    count);
            return 0;
        }

        ItemStack remainder;
        try {
            remainder = carried.insertIntoProviders(player, toRoute, false);
        } catch (RuntimeException failure) {
            ProviderDiagnostics diagnostics = diagnoseProviders(player, providers, carried, toRoute, count);
            SlotCommon.LOGGER.warn(
                    "[SLOT] BackpackReroute insertIntoProviders threw; leaving items in main inventory item={} count={} providers={} sources={} slots={} empty={} simulatedAbsorb={} error={}",
                    itemId(template),
                    count,
                    diagnostics.providerNames,
                    diagnostics.sourceCount,
                    diagnostics.slotCount,
                    diagnostics.emptySlots,
                    diagnostics.simulatedAbsorb,
                    failure.toString());
            SlotDebugLog.log(
                    "BackpackReroute insertIntoProviders threw {}; leaving items in main inventory",
                    failure.toString()
            );
            return 0;
        }
        int absorbed = count - (remainder == null ? 0 : remainder.getCount());
        if (absorbed <= 0) {
            ProviderDiagnostics diagnostics = diagnoseProviders(player, providers, carried, toRoute, count);
            if (diagnostics.simulatedAbsorb > 0 || diagnostics.matchingRoom > 0) {
                SlotCommon.LOGGER.warn(
                        "[SLOT] BackpackReroute inserted nothing despite visible provider capacity item={} count={} providers={} sources={} slots={} empty={} matchingRoom={} simulatedAbsorb={} errors={}",
                        itemId(template),
                        count,
                        diagnostics.providerNames,
                        diagnostics.sourceCount,
                        diagnostics.slotCount,
                        diagnostics.emptySlots,
                        diagnostics.matchingRoom,
                        diagnostics.simulatedAbsorb,
                        diagnostics.errors);
            } else {
                SlotDebugLog.verboseLog(
                        "BackpackReroute left item in main inventory; providers appear full item={} count={} providers={} sources={} slots={}",
                        itemId(template),
                        count,
                        diagnostics.providerNames,
                        diagnostics.sourceCount,
                        diagnostics.slotCount);
            }
            return 0;
        }

        ItemIdentity identity = ItemIdentityMatcher.create(template);
        int unshrunk = shrinkVanillaLanes(carried, player, identity, absorbed);
        CarriedInventoryRevisions.markChanged(player, "pickup_reroute");
        if (unshrunk > 0) {
            SlotCommon.LOGGER.warn(
                    "[SLOT] BackpackReroute inserted into provider but could not remove all routed items from vanilla lanes item={} count={} absorbed={} unshrunk={} providers={}",
                    identity.itemId(),
                    count,
                    absorbed,
                    unshrunk,
                    providers.stream().map(CarriedProvider::prefix).toList());
        } else if (SlotDebugLog.verbose()) {
            SlotDebugLog.verboseLog(
                    "BackpackReroute routed item={} count={} absorbed={} providers={}",
                    identity.itemId(),
                    count,
                    absorbed,
                    providers.stream().map(CarriedProvider::prefix).toList());
        }
        return absorbed;
    }

    private static int shrinkVanillaLanes(
            CarriedSourceAccess carried,
            ServerPlayer player,
            ItemIdentity identity,
            int count
    ) {
        int remaining = count;
        remaining = shrinkBuiltinLane(carried, player, identity, BuiltinInventoryIds.PLAYER_MAIN, 27, remaining);
        remaining = shrinkBuiltinLane(
                carried,
                player,
                identity,
                BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0,
                9,
                remaining);
        remaining = shrinkBuiltinLane(carried, player, identity, BuiltinInventoryIds.PLAYER_OFFHAND, 1, remaining);
        return remaining;
    }

    private static int shrinkBuiltinLane(
            CarriedSourceAccess carried,
            ServerPlayer player,
            ItemIdentity identity,
            String sourceId,
            int slotCount,
            int count
    ) {
        int remaining = count;
        for (int slot = 0; slot < slotCount && remaining > 0; slot++) {
            ItemStack current = carried.peek(player, sourceId, slot);
            if (!ItemIdentityMatcher.matchesMovable(current, identity)) {
                continue;
            }
            ItemStack taken = carried.extract(player, sourceId, slot, remaining, false);
            remaining -= taken == null ? 0 : taken.getCount();
        }
        return remaining;
    }

    private static ProviderDiagnostics diagnoseProviders(
            ServerPlayer player,
            List<CarriedProvider> providers,
            CarriedSourceAccess carried,
            ItemStack template,
            int count
    ) {
        providers = providers == null ? List.of() : providers;
        ArrayList<String> providerNames = new ArrayList<>();
        ArrayList<String> errors = new ArrayList<>();
        int sourceCount = 0;
        int slotCount = 0;
        int emptySlots = 0;
        int matchingRoom = 0;
        for (CarriedProvider provider : providers) {
            if (provider == null) {
                continue;
            }
            providerNames.add(provider.prefix());
            List<String> sourceIds;
            try {
                sourceIds = provider.sourceIds(player);
            } catch (RuntimeException | LinkageError failure) {
                errors.add(provider.prefix() + ":sourceIds:" + failure.getClass().getSimpleName());
                continue;
            }
            sourceCount += sourceIds.size();
            for (String sourceId : sourceIds) {
                int slots;
                try {
                    slots = provider.slotCount(player, sourceId);
                } catch (RuntimeException | LinkageError failure) {
                    errors.add(provider.prefix() + ":slotCount:" + failure.getClass().getSimpleName());
                    continue;
                }
                slotCount += Math.max(0, slots);
                for (int slot = 0; slot < slots; slot++) {
                    ItemStack stack;
                    try {
                        stack = provider.peek(player, sourceId, slot);
                    } catch (RuntimeException | LinkageError failure) {
                        errors.add(provider.prefix() + ":peek:" + failure.getClass().getSimpleName());
                        break;
                    }
                    if (stack == null || stack.isEmpty()) {
                        emptySlots++;
                    } else if (ItemStackEquivalence.sameItemAndData(stack, template)) {
                        matchingRoom += Math.max(0, stack.getMaxStackSize() - stack.getCount());
                    }
                }
            }
        }

        int simulatedAbsorb = 0;
        try {
            ItemStack simulatedRemainder = carried.insertIntoProviders(player, template.copy(), true);
            simulatedAbsorb = count - (simulatedRemainder == null ? 0 : simulatedRemainder.getCount());
        } catch (RuntimeException | LinkageError failure) {
            errors.add("simulate:" + failure.getClass().getSimpleName());
        }

        return new ProviderDiagnostics(
                providers.size(),
                List.copyOf(providerNames),
                sourceCount,
                slotCount,
                emptySlots,
                matchingRoom,
                Math.max(0, simulatedAbsorb),
                List.copyOf(errors)
        );
    }

    private static String itemId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "empty";
        }
        return ItemIdentityMatcher.create(stack).itemId();
    }

    private record ProviderDiagnostics(
            int providerCount,
            List<String> providerNames,
            int sourceCount,
            int slotCount,
            int emptySlots,
            int matchingRoom,
            int simulatedAbsorb,
            List<String> errors
    ) {
    }
}
