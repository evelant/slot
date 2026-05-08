package dev.imagio.slot.neoforge.storage;

import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.integration.InventoryHostContext;
import dev.imagio.slot.inventory.integration.InventoryHostFamilyHint;
import dev.imagio.slot.inventory.integration.InventoryHostObservationHints;
import dev.imagio.slot.inventory.integration.InventoryHostResolver;
import dev.imagio.slot.inventory.integration.InventorySlotOwnershipPosture;
import dev.imagio.slot.inventory.query.InventoryAuthorityReadService;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.inventory.session.CarriedAcquisitionActivityTracker;
import dev.imagio.slot.neoforge.workflow.SlotPlayerWorkflowRuntimeService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Map;
import java.util.function.Function;

public final class NeoForgeCarriedActivityTracker {
    private static final CarriedAcquisitionActivityTracker TRACKER = new CarriedAcquisitionActivityTracker();
    private static final Function<InventoryEntrySnapshot, ItemIdentity> IDENTITY_RESOLVER =
            entry -> entry == null ? null : ItemIdentityMatcher.create(entry.stack());
    private static boolean registered;

    private NeoForgeCarriedActivityTracker() {
    }

    public static void init() {
        if (registered) {
            return;
        }
        NeoForge.EVENT_BUS.addListener(NeoForgeCarriedActivityTracker::onServerTick);
        NeoForge.EVENT_BUS.addListener(NeoForgeCarriedActivityTracker::onPlayerLoggedOut);
        NeoForge.EVENT_BUS.addListener(NeoForgeCarriedActivityTracker::onServerStopping);
        registered = true;
    }

    public static void suppressNext(ServerPlayer player) {
        TRACKER.suppressNext(key(player));
    }

    private static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer() == null) {
            return;
        }
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            observe(player, "server_tick");
        }
    }

    private static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            TRACKER.forget(key(player));
        }
    }

    private static void onServerStopping(ServerStoppingEvent event) {
        TRACKER.clear();
    }

    private static void observe(ServerPlayer player, String sessionId) {
        InventoryHostDescriptor host = resolveHost(player);
        if (host == null) {
            return;
        }
        InventoryAuthoritySnapshot authority = InventoryAuthorityReadService.serverAuthority(player, host);
        TRACKER.observe(
                key(player),
                authority,
                SlotPlayerWorkflowRuntimeService.runtime(player),
                IDENTITY_RESOLVER,
                sessionId);
    }

    private static String key(ServerPlayer player) {
        return player == null ? "" : player.getUUID().toString();
    }

    private static InventoryHostDescriptor resolveHost(ServerPlayer player) {
        if (player == null) {
            return null;
        }
        AbstractContainerMenu menu = player.containerMenu;
        if (menu == null) {
            return null;
        }
        return InventoryHostResolver.resolve(new InventoryHostContext(
                menu,
                player.getInventory(),
                Component.literal("SLOT CarriedActivityTracker"),
                NeoForgeCarriedActivityTracker.class.getName(),
                new InventoryHostObservationHints(
                        InventoryHostFamilyHint.CARRIED_ONLY,
                        InventorySlotOwnershipPosture.SLOT_OWNED,
                        true,
                        true,
                        Map.of("carriedActivityTracker", "neoforge")
                )
        ));
    }
}
