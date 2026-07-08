package dev.imagio.slot.inventory.integration;

import dev.imagio.slot.inventory.action.InventoryActionMode;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.InventoryStackSnapshot;
import dev.imagio.slot.inventory.core.InventoryToolActionId;
import dev.imagio.slot.inventory.core.InventoryToolDescriptor;
import dev.imagio.slot.inventory.core.InventoryToolToggleId;
import dev.imagio.slot.inventory.core.InventoryTopologyDescriptor;
import dev.imagio.slot.inventory.query.InventoryEntryKey;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.inventory.query.InventorySourceSnapshot;
import dev.imagio.slot.inventory.storage.WorldDisplayStorageSource;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public interface InventoryHostSession {
    String providerId();

    default String providerScopeId() {
        return "";
    }

    default List<InventorySourceDescriptor> hostSources() {
        return List.of();
    }

    default InventoryTopologyDescriptor topology() {
        return InventoryTopologyDescriptor.empty();
    }

    default List<InventoryToolDescriptor> tools() {
        return List.of();
    }

    default InventorySourceSnapshot readSourceSnapshot(InventoryHostDescriptor host, String sourceId) {
        List<InventoryStackSnapshot> snapshots = readSnapshots(host, sourceId);
        if (sourceId == null || sourceId.isBlank()) {
            return InventorySourceSnapshot.empty("__missing__");
        }
        InventorySourceDescriptor source = host == null ? null : host.source(sourceId);
        int highestSlot = snapshots == null ? -1 : snapshots.stream()
                .filter(snapshot -> snapshot != null)
                .mapToInt(InventoryStackSnapshot::handle)
                .max()
                .orElse(-1);
        int slotCapacity = source == null
                ? Math.max(0, highestSlot + 1)
                : Math.max(Math.max(0, source.logicalSlotCount()), highestSlot + 1);
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
        return new InventorySourceSnapshot(sourceId, slotCapacity, List.copyOf(entries), diagnostics());
    }

    default List<InventoryStackSnapshot> readSnapshots(InventoryHostDescriptor host, String sourceId) {
        return List.of();
    }

    default MutationResult mutate(
            InventoryHostDescriptor host,
            InventoryMutationRequest request,
            InventoryMutationMode mode
    ) {
        return MutationResult.blocked("unsupported_mutation", request == null ? null : request.stack());
    }

    default ToolActionResult activateTool(
            InventoryHostDescriptor host,
            String toolId,
            InventoryActionMode mode
    ) {
        return ToolActionResult.blocked("unsupported_tool_activation");
    }

    default ToolActionResult executeToolAction(
            InventoryHostDescriptor host,
            String toolId,
            InventoryToolActionId actionId,
            InventoryActionMode mode
    ) {
        return ToolActionResult.blocked("unsupported_tool_action");
    }

    default ToolActionResult setToolToggle(
            InventoryHostDescriptor host,
            String toolId,
            InventoryToolToggleId toggleId,
            boolean enabled,
            InventoryActionMode mode
    ) {
        return ToolActionResult.blocked("unsupported_tool_toggle");
    }

    default String diagnostics() {
        return "";
    }

    default List<WorldDisplayStorageSource> observedWorldStorageSources(
            ServerPlayer player,
            InventoryHostDescriptor host
    ) {
        return List.of();
    }

    static InventoryHostSession empty() {
        return EmptyInventoryHostSession.INSTANCE;
    }

    final class EmptyInventoryHostSession implements InventoryHostSession {
        private static final EmptyInventoryHostSession INSTANCE = new EmptyInventoryHostSession();

        private EmptyInventoryHostSession() {
        }

        @Override
        public String providerId() {
            return "none";
        }
    }
}
