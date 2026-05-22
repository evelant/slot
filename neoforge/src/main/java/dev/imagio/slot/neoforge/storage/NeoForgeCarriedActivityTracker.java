package dev.imagio.slot.neoforge.storage;

import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.core.ItemStackStructuralKey;
import dev.imagio.slot.inventory.action.InventoryActionOutcome;
import dev.imagio.slot.inventory.integration.InventoryHostContext;
import dev.imagio.slot.inventory.integration.InventoryHostFamilyHint;
import dev.imagio.slot.inventory.integration.InventoryHostObservationHints;
import dev.imagio.slot.inventory.integration.InventoryHostResolver;
import dev.imagio.slot.inventory.integration.InventorySlotOwnershipPosture;
import dev.imagio.slot.inventory.query.InventoryAuthorityReadService;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.inventory.session.CarriedAcquisitionActivityTracker;
import dev.imagio.slot.inventory.storage.CarriedInventoryRevisions;
import dev.imagio.slot.neoforge.workflow.SlotPlayerWorkflowRuntimeService;
import dev.imagio.slot.workflow.domain.InventoryActivityEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class NeoForgeCarriedActivityTracker {
    private static final CarriedAcquisitionActivityTracker TRACKER = new CarriedAcquisitionActivityTracker();
    private static final Function<InventoryEntrySnapshot, ItemIdentity> IDENTITY_RESOLVER =
            entry -> entry == null ? null : ItemIdentityMatcher.create(entry.stack());
    private static final Map<UUID, ObservationHandle> HANDLES = new ConcurrentHashMap<>();
    private static final Set<UUID> DIRTY_PLAYERS = ConcurrentHashMap.newKeySet();
    private static boolean registered;

    private NeoForgeCarriedActivityTracker() {
    }

    public static void init() {
        if (registered) {
            return;
        }
        NeoForge.EVENT_BUS.addListener(NeoForgeCarriedActivityTracker::onServerTick);
        NeoForge.EVENT_BUS.addListener(NeoForgeCarriedActivityTracker::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(NeoForgeCarriedActivityTracker::onPlayerLoggedOut);
        NeoForge.EVENT_BUS.addListener(NeoForgeCarriedActivityTracker::onContainerOpen);
        NeoForge.EVENT_BUS.addListener(NeoForgeCarriedActivityTracker::onContainerClose);
        NeoForge.EVENT_BUS.addListener(NeoForgeCarriedActivityTracker::onServerStopping);
        registered = true;
    }

    public static void suppressNext(ServerPlayer player) {
        TRACKER.suppressNext(key(player));
    }

    public static void suppressAcquired(ServerPlayer player, ItemStack stack, int count) {
        if (stack == null || stack.isEmpty() || count <= 0) {
            return;
        }
        TRACKER.suppressAcquired(key(player), ItemIdentityMatcher.create(stack), count);
        markDirty(player, "suppress_acquired");
    }

    public static void suppressAcquired(ServerPlayer player, Collection<InventoryActivityEvent> events) {
        TRACKER.suppressAcquired(key(player), events);
        if (events != null && !events.isEmpty()) {
            markDirty(player, "suppress_acquired");
        }
    }

    public static void suppressOutcome(ServerPlayer player, InventoryActionOutcome outcome) {
        if (outcome != null && outcome.successful()) {
            suppressAcquired(player, outcome.activityEvents());
        }
    }

    public static void markDirty(ServerPlayer player, String sessionId) {
        if (player == null) {
            return;
        }
        CarriedInventoryRevisions.markChanged(player, sessionId == null ? "" : sessionId);
        DIRTY_PLAYERS.add(player.getUUID());
    }

    private static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer() == null || DIRTY_PLAYERS.isEmpty()) {
            return;
        }
        List<UUID> dirty = new ArrayList<>(DIRTY_PLAYERS);
        for (UUID playerId : dirty) {
            DIRTY_PLAYERS.remove(playerId);
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(playerId);
            if (player == null) {
                detach(playerId);
                TRACKER.forget(playerId.toString());
                CarriedInventoryRevisions.forget(playerId);
                continue;
            }
            observe(player, "menu_slot_changed");
        }
    }

    private static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            attach(player, player.inventoryMenu, "player_login");
        }
    }

    private static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            detach(player.getUUID());
            TRACKER.forget(key(player));
            CarriedInventoryRevisions.forget(player);
        }
    }

    private static void onContainerOpen(PlayerContainerEvent.Open event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            attach(player, event.getContainer(), "container_open");
        }
    }

    private static void onContainerClose(PlayerContainerEvent.Close event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ObservationHandle handle = HANDLES.get(player.getUUID());
            if (handle != null && handle.menu == event.getContainer()) {
                attach(player, player.inventoryMenu, "container_close");
            }
        }
    }

    private static void onServerStopping(ServerStoppingEvent event) {
        for (UUID playerId : new ArrayList<>(HANDLES.keySet())) {
            detach(playerId);
        }
        DIRTY_PLAYERS.clear();
        TRACKER.clear();
        CarriedInventoryRevisions.clear();
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
        Map<Integer, ItemStackStructuralKey> slotKeys = seedSlotKeys(menu);
        ContainerListener listener = new ContainerListener() {
            @Override
            public void slotChanged(AbstractContainerMenu changedMenu, int slotIndex, ItemStack stack) {
                if (changedMenu == menu && updateSlotKey(slotKeys, slotIndex, stack)) {
                    CarriedInventoryRevisions.markChanged(playerId, "menu_slot_changed");
                    DIRTY_PLAYERS.add(playerId);
                }
            }

            @Override
            public void dataChanged(AbstractContainerMenu changedMenu, int dataIndex, int value) {
                // Carried-acquisition recents derive from item slots only.
            }
        };
        ObservationHandle handle = new ObservationHandle(menu, listener, slotKeys);
        menu.addSlotListener(listener);
        HANDLES.put(playerId, handle);
        CarriedInventoryRevisions.markChanged(playerId, sessionId);
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

    private static Map<Integer, ItemStackStructuralKey> seedSlotKeys(AbstractContainerMenu menu) {
        HashMap<Integer, ItemStackStructuralKey> keys = new HashMap<>();
        if (menu == null) {
            return keys;
        }
        for (int slotIndex = 0; slotIndex < menu.slots.size(); slotIndex++) {
            keys.put(slotIndex, ItemStackStructuralKey.from(menu.slots.get(slotIndex).getItem()));
        }
        return keys;
    }

    private static boolean updateSlotKey(Map<Integer, ItemStackStructuralKey> slotKeys, int slotIndex, ItemStack stack) {
        if (slotIndex < 0) {
            return true;
        }
        ItemStackStructuralKey next = ItemStackStructuralKey.from(stack);
        ItemStackStructuralKey previous = slotKeys.put(slotIndex, next);
        return previous == null || !previous.equals(next);
    }

    private record ObservationHandle(
            AbstractContainerMenu menu,
            ContainerListener listener,
            Map<Integer, ItemStackStructuralKey> slotKeys
    ) {
    }
}
