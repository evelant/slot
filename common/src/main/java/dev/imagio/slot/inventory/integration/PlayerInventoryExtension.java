package dev.imagio.slot.inventory.integration;

import dev.imagio.slot.inventory.core.EquipmentGroupDescriptor;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.InventoryStackSnapshot;
import dev.imagio.slot.inventory.core.QuickAccessLaneDescriptor;
import dev.imagio.slot.inventory.query.InventoryEntryKey;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.inventory.query.InventorySourceSnapshot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public interface PlayerInventoryExtension {
    String providerId();

    default List<InventorySourceDescriptor> additionalSources() {
        return List.of();
    }

    default List<QuickAccessLaneDescriptor> additionalQuickAccessLanes() {
        return List.of();
    }

    default List<EquipmentGroupDescriptor> additionalEquipmentGroups() {
        return List.of();
    }

    default InventorySourceSnapshot readSourceSnapshot(
            Player player,
            InventoryHostDescriptor host,
            String sourceId
    ) {
        return legacySourceSnapshot(host, sourceId, readSnapshots(player, host, sourceId), slotCapacity(player, host, sourceId), diagnostics());
    }

    default List<InventoryStackSnapshot> readSnapshots(
            Player player,
            InventoryHostDescriptor host,
            String sourceId
    ) {
        return List.of();
    }

    default InventorySourceSnapshot readSourceSnapshot(
            ServerPlayer player,
            InventoryHostDescriptor host,
            String sourceId
    ) {
        return legacySourceSnapshot(host, sourceId, readServerSnapshots(player, host, sourceId), serverSlotCapacity(player, host, sourceId), diagnostics());
    }

    default List<InventoryStackSnapshot> readServerSnapshots(
            ServerPlayer player,
            InventoryHostDescriptor host,
            String sourceId
    ) {
        return List.of();
    }

    default int slotCapacity(Player player, InventoryHostDescriptor host, String sourceId) {
        return 0;
    }

    default int serverSlotCapacity(ServerPlayer player, InventoryHostDescriptor host, String sourceId) {
        return 0;
    }

    default MutationResult mutate(
            InventoryHostDescriptor host,
            InventoryMutationRequest request,
            InventoryMutationMode mode
    ) {
        return MutationResult.blocked("unsupported_extension_mutation", request == null ? null : request.stack());
    }

    default String diagnostics() {
        return "";
    }

    private static InventorySourceSnapshot legacySourceSnapshot(
            InventoryHostDescriptor host,
            String sourceId,
            List<InventoryStackSnapshot> snapshots,
            int slotCapacity,
            String diagnostics
    ) {
        if (sourceId == null || sourceId.isBlank()) {
            return InventorySourceSnapshot.empty("__missing__");
        }
        InventorySourceDescriptor source = host == null ? null : host.source(sourceId);
        int highestSlot = snapshots == null ? -1 : snapshots.stream()
                .filter(snapshot -> snapshot != null)
                .mapToInt(InventoryStackSnapshot::handle)
                .max()
                .orElse(-1);
        int resolvedCapacity = Math.max(
                Math.max(0, slotCapacity),
                Math.max(source == null ? 0 : source.logicalSlotCount(), highestSlot + 1)
        );
        ArrayList<InventoryEntrySnapshot> entries = new ArrayList<>();
        if (snapshots != null) {
            for (InventoryStackSnapshot snapshot : snapshots) {
                if (snapshot == null) {
                    continue;
                }
                entries.add(new InventoryEntrySnapshot(
                        InventoryEntryKey.slot(sourceId, snapshot.handle()),
                        snapshot.stack(),
                        snapshot.count(),
                        ""
                ));
            }
        }
        return new InventorySourceSnapshot(
                sourceId,
                resolvedCapacity,
                List.copyOf(entries),
                diagnostics == null ? "" : diagnostics
        );
    }
}
