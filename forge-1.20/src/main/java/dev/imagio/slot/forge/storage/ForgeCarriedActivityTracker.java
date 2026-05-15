package dev.imagio.slot.forge.storage;

import dev.imagio.slot.forge.SlotForge;
import dev.imagio.slot.forge.workflow.ForgePlayerWorkflowRuntimeService;
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
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Mod.EventBusSubscriber(modid = SlotForge.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ForgeCarriedActivityTracker {
    private static final CarriedAcquisitionActivityTracker TRACKER = new CarriedAcquisitionActivityTracker();
    private static final Function<InventoryEntrySnapshot, ItemIdentity> IDENTITY_RESOLVER =
            entry -> entry == null ? null : ItemIdentityMatcher.create(entry.stack());
    private static final Map<UUID, ObservationHandle> HANDLES = new ConcurrentHashMap<>();
    private static final Set<UUID> DIRTY_PLAYERS = ConcurrentHashMap.newKeySet();

    private ForgeCarriedActivityTracker() {
    }

    public static void suppressNext(ServerPlayer player) {
        TRACKER.suppressNext(key(player));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.getServer() == null) {
            return;
        }
        if (DIRTY_PLAYERS.isEmpty()) {
            return;
        }
        List<UUID> dirty = new ArrayList<>(DIRTY_PLAYERS);
        for (UUID playerId : dirty) {
            DIRTY_PLAYERS.remove(playerId);
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(playerId);
            if (player == null) {
                detach(playerId);
                TRACKER.forget(playerId.toString());
                continue;
            }
            observe(player, "menu_slot_changed");
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            attach(player, player.inventoryMenu, "player_login");
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            detach(player.getUUID());
            TRACKER.forget(key(player));
        }
    }

    @SubscribeEvent
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            attach(player, event.getContainer(), "container_open");
        }
    }

    @SubscribeEvent
    public static void onContainerClose(PlayerContainerEvent.Close event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ObservationHandle handle = HANDLES.get(player.getUUID());
            if (handle != null && handle.menu == event.getContainer()) {
                attach(player, player.inventoryMenu, "container_close");
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        for (UUID playerId : new ArrayList<>(HANDLES.keySet())) {
            detach(playerId);
        }
        DIRTY_PLAYERS.clear();
        TRACKER.clear();
    }

    private static void attach(ServerPlayer player, AbstractContainerMenu menu, String sessionId) {
        if (player == null || menu == null) {
            return;
        }
        UUID playerId = player.getUUID();
        ObservationHandle existing = HANDLES.get(playerId);
        if (existing != null && existing.menu == menu) {
            return;
        }
        detach(playerId);
        ContainerListener listener = new ContainerListener() {
            @Override
            public void slotChanged(AbstractContainerMenu changedMenu, int slotIndex, ItemStack stack) {
                if (changedMenu == menu) {
                    DIRTY_PLAYERS.add(playerId);
                }
            }

            @Override
            public void dataChanged(AbstractContainerMenu changedMenu, int dataIndex, int value) {
                // Carried-acquisition recents derive from item slots only.
            }
        };
        menu.addSlotListener(listener);
        HANDLES.put(playerId, new ObservationHandle(menu, listener));
        observe(player, sessionId);
        DIRTY_PLAYERS.remove(playerId);
    }

    private static void detach(UUID playerId) {
        if (playerId == null) {
            return;
        }
        ObservationHandle handle = HANDLES.remove(playerId);
        if (handle != null) {
            handle.menu.removeSlotListener(handle.listener);
        }
        DIRTY_PLAYERS.remove(playerId);
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
                ForgePlayerWorkflowRuntimeService.runtime(player),
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
                ForgeCarriedActivityTracker.class.getName(),
                new InventoryHostObservationHints(
                        InventoryHostFamilyHint.CARRIED_ONLY,
                        InventorySlotOwnershipPosture.SLOT_OWNED,
                        true,
                        true,
                        Map.of("carriedActivityTracker", "forge")
                )
        ));
    }

    private record ObservationHandle(AbstractContainerMenu menu, ContainerListener listener) {
    }
}
