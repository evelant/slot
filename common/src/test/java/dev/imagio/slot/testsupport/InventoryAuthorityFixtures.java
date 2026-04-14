package dev.imagio.slot.testsupport;

import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.InventoryStackSnapshot;
import dev.imagio.slot.inventory.query.InventoryAuthorityReadService;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntryKey;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.inventory.query.InventorySourceSnapshot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class InventoryAuthorityFixtures {
    private InventoryAuthorityFixtures() {
    }

    public static InventoryAuthoritySnapshot authority(
            InventoryHostDescriptor host,
            Map<String, List<InventoryStackSnapshot>> snapshotsBySourceId,
            Map<String, Integer> slotCapacityBySourceId
    ) {
        if (host == null) {
            return InventoryAuthoritySnapshot.empty();
        }
        Map<String, List<InventoryStackSnapshot>> snapshots = snapshotsBySourceId == null ? Map.of() : snapshotsBySourceId;
        Map<String, Integer> capacities = slotCapacityBySourceId == null ? Map.of() : slotCapacityBySourceId;
        LinkedHashMap<String, InventorySourceSnapshot> sourceSnapshots = new LinkedHashMap<>();
        for (InventorySourceDescriptor source : host.sourceDescriptors()) {
            if (source == null) {
                continue;
            }
            InventorySourceSnapshot snapshot = snapshots.containsKey(source.id())
                    ? slotBackedSource(
                        source,
                        snapshots.get(source.id()),
                        capacities.getOrDefault(source.id(), source.logicalSlotCount())
                )
                    : switch (source.domain()) {
                        case PLAYER, PLAYER_EXTENSION -> slotBackedSource(
                                source,
                                List.of(),
                                capacities.getOrDefault(source.id(), source.logicalSlotCount())
                        );
                        case HOST_STORAGE, TOOL_REGION -> host.hostSession().readSourceSnapshot(host, source.id());
                    };
            sourceSnapshots.put(source.id(), snapshot == null ? InventorySourceSnapshot.empty(source.id()) : snapshot);
        }
        return new InventoryAuthoritySnapshot(
                host,
                Map.copyOf(sourceSnapshots),
                InventoryAuthorityReadService.cursor(host.menu())
        );
    }

    private static InventorySourceSnapshot slotBackedSource(
            InventorySourceDescriptor source,
            List<InventoryStackSnapshot> snapshots,
            int slotCapacity
    ) {
        ArrayList<InventoryEntrySnapshot> entries = new ArrayList<>();
        if (snapshots != null) {
            for (InventoryStackSnapshot snapshot : snapshots) {
                if (snapshot == null) {
                    continue;
                }
                entries.add(new InventoryEntrySnapshot(
                        InventoryEntryKey.slot(source.id(), snapshot.handle()),
                        snapshot.stack(),
                        snapshot.count(),
                        ""
                ));
            }
        }
        int highestSlot = entries.stream().mapToInt(InventoryEntrySnapshot::slotIndex).max().orElse(-1);
        int resolvedCapacity = Math.max(Math.max(0, slotCapacity), Math.max(source.logicalSlotCount(), highestSlot + 1));
        return new InventorySourceSnapshot(source.id(), resolvedCapacity, List.copyOf(entries), "");
    }
}
