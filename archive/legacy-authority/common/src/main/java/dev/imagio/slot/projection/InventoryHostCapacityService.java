package dev.imagio.slot.projection;

import dev.imagio.slot.session.InventoryHostDescriptor;
import dev.imagio.slot.session.InventorySourceDescriptor;
import dev.imagio.slot.session.InventorySourceDomain;
import dev.imagio.slot.session.InventorySourceRole;
import dev.imagio.slot.storage.adapter.ExternalStorageStackSnapshot;
import dev.imagio.slot.storage.provider.InventoryStackSnapshot;
import dev.imagio.slot.storage.provider.SupplementalCarriedSourceProviderRegistry;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Set;

public final class InventoryHostCapacityService {
    public StorageFillStats measure(
            LocalPlayer player,
            InventoryHostDescriptor host,
            Set<String> sourceIds,
            List<ExternalStorageStackSnapshot> primarySnapshots
    ) {
        if (player == null || host == null || sourceIds == null || sourceIds.isEmpty()) {
            return StorageFillStats.EMPTY;
        }

        int occupiedSlots = 0;
        int totalSlots = 0;
        for (String sourceId : sourceIds) {
            InventorySourceDescriptor source = host.sourceDescriptor(sourceId);
            if (source == null || source.hidden() || source.toolOnly()) {
                continue;
            }
            StorageFillStats sourceStats = measureSource(player, host, source, primarySnapshots);
            occupiedSlots += sourceStats.occupiedSlots();
            totalSlots += sourceStats.totalSlots();
        }

        return new StorageFillStats(occupiedSlots, totalSlots);
    }

    private static StorageFillStats measureSource(
            LocalPlayer player,
            InventoryHostDescriptor host,
            InventorySourceDescriptor source,
            List<ExternalStorageStackSnapshot> primarySnapshots
    ) {
        if (host.topology().sourceMenuBacked(source.id())) {
            return measureMenuBackedSource(host, source);
        }

        if (source.domain() == InventorySourceDomain.PLAYER) {
            return measurePlayerSource(player, source);
        }

        if (source.domain() == InventorySourceDomain.SUPPLEMENTAL_CARRIED) {
            int total = SupplementalCarriedSourceProviderRegistry.slotCapacity(player, host, source.id());
            int occupied = SupplementalCarriedSourceProviderRegistry.readSnapshots(player, host, source.id()).size();
            return new StorageFillStats(occupied, total);
        }

        if (source.domain() == InventorySourceDomain.HOST_STORAGE) {
            return measureProviderSource(host, source, primarySnapshots);
        }

        return StorageFillStats.EMPTY;
    }

    private static StorageFillStats measureMenuBackedSource(
            InventoryHostDescriptor host,
            InventorySourceDescriptor source
    ) {
        int occupied = 0;
        List<Integer> menuSlots = host.topology().menuSlotsForSource(source.id());
        for (int menuSlot : menuSlots) {
            if (menuSlot >= 0
                    && menuSlot < host.menu().slots.size()
                    && !host.menu().getSlot(menuSlot).getItem().isEmpty()) {
                occupied++;
            }
        }
        return new StorageFillStats(occupied, Math.max(source.slotCount(), menuSlots.size()));
    }

    private static StorageFillStats measurePlayerSource(LocalPlayer player, InventorySourceDescriptor source) {
        return switch (source.role()) {
            case MAIN -> new StorageFillStats(countOccupiedPlayerSlots(player, 9, 36), 27);
            case HOTBAR -> "0".equals(source.laneId()) || source.laneId().isBlank()
                    ? new StorageFillStats(countOccupiedPlayerSlots(player, 0, 9), 9)
                    : new StorageFillStats(0, source.slotCount());
            case EQUIPMENT -> new StorageFillStats(countOccupiedArmorSlots(player), Math.max(4, source.slotCount()));
            case OFFHAND -> new StorageFillStats(player.getOffhandItem().isEmpty() ? 0 : 1, 1);
            default -> StorageFillStats.EMPTY;
        };
    }

    private static StorageFillStats measureProviderSource(
            InventoryHostDescriptor host,
            InventorySourceDescriptor source,
            List<ExternalStorageStackSnapshot> primarySnapshots
    ) {
        List<InventoryStackSnapshot> snapshots;
        InventorySourceDescriptor singleHostStorageSource = host.singleHostStorageSource();
        if (singleHostStorageSource != null
                && primarySnapshots != null
                && singleHostStorageSource.id().equals(source.id())) {
            snapshots = primarySnapshots.stream()
                    .map(snapshot -> new InventoryStackSnapshot(snapshot.handle(), snapshot.stack(), snapshot.count()))
                    .toList();
        } else {
            snapshots = host.providerSession().readSnapshots(host, source.id());
        }

        int occupied = 0;
        for (InventoryStackSnapshot snapshot : snapshots) {
            ItemStack stack = snapshot.stack();
            if (!stack.isEmpty() && snapshot.count() > 0) {
                occupied++;
            }
        }
        return new StorageFillStats(occupied, Math.max(source.slotCount(), snapshots.size()));
    }

    private static int countOccupiedPlayerSlots(LocalPlayer player, int startInclusive, int endExclusive) {
        int occupied = 0;
        for (int slot = startInclusive; slot < endExclusive; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty()) {
                occupied++;
            }
        }
        return occupied;
    }

    private static int countOccupiedArmorSlots(LocalPlayer player) {
        int occupied = 0;
        for (ItemStack stack : player.getInventory().armor) {
            if (!stack.isEmpty()) {
                occupied++;
            }
        }
        return occupied;
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
