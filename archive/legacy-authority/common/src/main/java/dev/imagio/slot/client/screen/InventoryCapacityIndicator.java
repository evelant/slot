package dev.imagio.slot.client.screen;

import dev.imagio.slot.projection.InventoryHostCapacityService;
import dev.imagio.slot.session.ChestLikeMenuLayout;
import dev.imagio.slot.storage.adapter.ExternalStorageStackSnapshot;
import net.minecraft.client.player.LocalPlayer;

import java.util.List;
import java.util.Set;

public final class InventoryCapacityIndicator {
    private static final Set<String> DEFAULT_PLAYER_SOURCES = Set.of(
            ChestLikeMenuLayout.SOURCE_PLAYER_MAIN,
            ChestLikeMenuLayout.SOURCE_PLAYER_HOTBAR,
            ChestLikeMenuLayout.SOURCE_PLAYER_BACKPACK
    );
    private static final InventoryHostCapacityService CAPACITY_SERVICE = new InventoryHostCapacityService();

    private InventoryCapacityIndicator() {
    }

    public static StorageFillStats measureCarried(LocalPlayer player, InventoryScreenContext screenContext) {
        return measureCarried(player, screenContext, null);
    }

    public static StorageFillStats measureCarried(
            LocalPlayer player,
            InventoryScreenContext screenContext,
            List<ExternalStorageStackSnapshot> primarySnapshots
    ) {
        Set<String> sourceIds = carriedCapacitySourceIds(screenContext == null ? DEFAULT_PLAYER_SOURCES : screenContext.carriedSourceIds());
        return measure(player, screenContext, sourceIds, primarySnapshots);
    }

    static Set<String> carriedCapacitySourceIds(Set<String> sourceIds) {
        if (sourceIds == null || sourceIds.isEmpty()) {
            return Set.of();
        }
        java.util.LinkedHashSet<String> resolved = new java.util.LinkedHashSet<>();
        for (String sourceId : sourceIds) {
            if (ChestLikeMenuLayout.SOURCE_PLAYER_MAIN.equals(sourceId)
                    || ChestLikeMenuLayout.SOURCE_PLAYER_HOTBAR.equals(sourceId)
                    || ChestLikeMenuLayout.SOURCE_PLAYER_BACKPACK.equals(sourceId)
                    || ChestLikeMenuLayout.SOURCE_CARRIED_STORAGE.equals(sourceId)) {
                resolved.add(sourceId);
            }
        }
        return Set.copyOf(resolved);
    }

    public static StorageFillStats measure(LocalPlayer player, InventoryScreenContext screenContext, Set<String> sourceIds) {
        return measure(player, screenContext, sourceIds, null);
    }

    public static StorageFillStats measure(
            LocalPlayer player,
            InventoryScreenContext screenContext,
            Set<String> sourceIds,
            List<ExternalStorageStackSnapshot> primarySnapshots
    ) {
        InventoryHostCapacityService.StorageFillStats stats = CAPACITY_SERVICE.measure(
                player,
                screenContext == null ? null : screenContext.host(),
                sourceIds,
                primarySnapshots
        );
        return new StorageFillStats(stats.occupiedSlots(), stats.totalSlots());
    }

    public record StorageFillStats(int occupiedSlots, int totalSlots) {
        public static final StorageFillStats EMPTY = new StorageFillStats(0, 0);

        public StorageFillStats {
            occupiedSlots = Math.max(0, occupiedSlots);
            totalSlots = Math.max(0, totalSlots);
            if (occupiedSlots > totalSlots && totalSlots > 0) {
                occupiedSlots = totalSlots;
            }
        }

        public boolean available() {
            return totalSlots > 0;
        }

        public float fillRatio() {
            if (totalSlots <= 0) {
                return 0.0F;
            }
            return Math.min(1.0F, occupiedSlots / (float) totalSlots);
        }

        public int freeSlots() {
            return Math.max(0, totalSlots - occupiedSlots);
        }
    }
}
