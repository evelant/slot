package dev.imagio.slot.inventory.storage;

import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Shared invalidation point for derived reads over a player's carried storage.
 * Platform hooks and common mutation helpers bump the revision whenever carried
 * slots may have changed; hot-path read helpers cache against that revision.
 */
public final class CarriedInventoryRevisions {
    private static final ConcurrentMap<UUID, RevisionState> STATES = new ConcurrentHashMap<>();

    private CarriedInventoryRevisions() {
    }

    public static long revision(ServerPlayer player) {
        return revision(playerId(player));
    }

    public static long revision(UUID playerId) {
        if (playerId == null) {
            return 0L;
        }
        RevisionState state = STATES.get(playerId);
        return state == null ? 0L : state.revision();
    }

    public static long markChanged(ServerPlayer player, String reason) {
        return markChanged(playerId(player), reason);
    }

    public static long markChanged(UUID playerId, String reason) {
        if (playerId == null) {
            return 0L;
        }
        return state(playerId).markChanged(reason);
    }

    public static CarriedSourceAccess.CarriedStoragePressure cachedPressure(
            ServerPlayer player,
            CarriedSourceAccess access
    ) {
        if (player == null || access == null) {
            return CarriedSourceAccess.CarriedStoragePressure.empty();
        }
        UUID playerId = playerId(player);
        if (playerId == null) {
            CarriedSourceAccess.CarriedStoragePressure pressure = access.carriedStoragePressure(player);
            return pressure == null ? CarriedSourceAccess.CarriedStoragePressure.empty() : pressure;
        }
        return state(playerId).cachedPressure(player, access);
    }

    public static void forget(ServerPlayer player) {
        forget(playerId(player));
    }

    public static void forget(UUID playerId) {
        if (playerId != null) {
            STATES.remove(playerId);
        }
    }

    public static void clear() {
        STATES.clear();
    }

    private static RevisionState state(UUID playerId) {
        return STATES.computeIfAbsent(playerId, ignored -> new RevisionState());
    }

    private static UUID playerId(ServerPlayer player) {
        return player == null ? null : player.getUUID();
    }

    private static final class RevisionState {
        private final AtomicLong revision = new AtomicLong();
        private volatile PressureCache pressureCache;
        @SuppressWarnings("unused")
        private volatile String lastReason = "";

        long revision() {
            return revision.get();
        }

        long markChanged(String reason) {
            long next = revision.incrementAndGet();
            pressureCache = null;
            lastReason = reason == null ? "" : reason;
            return next;
        }

        CarriedSourceAccess.CarriedStoragePressure cachedPressure(
                ServerPlayer player,
                CarriedSourceAccess access
        ) {
            long currentRevision = revision.get();
            PressureCache cached = pressureCache;
            if (cached != null && cached.revision() == currentRevision) {
                return cached.pressure();
            }
            synchronized (this) {
                currentRevision = revision.get();
                cached = pressureCache;
                if (cached != null && cached.revision() == currentRevision) {
                    return cached.pressure();
                }
                CarriedSourceAccess.CarriedStoragePressure measured = access.carriedStoragePressure(player);
                if (measured == null) {
                    measured = CarriedSourceAccess.CarriedStoragePressure.empty();
                }
                pressureCache = new PressureCache(currentRevision, measured);
                return measured;
            }
        }
    }

    private record PressureCache(
            long revision,
            CarriedSourceAccess.CarriedStoragePressure pressure
    ) {
    }
}
