package dev.imagio.slot.inventory.core;

import dev.imagio.slot.inventory.integration.InventoryHostSession;
import dev.imagio.slot.inventory.integration.PlayerInventoryExtension;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record InventoryHostDescriptor(
        HostInstanceKey hostId,
        ServerMenuRef serverMenuRef,
        String screenClassName,
        Component title,
        AbstractContainerMenu menu,
        InventoryTopologyDescriptor topology,
        InventoryHostSession hostSession,
        List<PlayerInventoryExtension> playerExtensions,
        PlayerRuntimeStateDescriptor playerRuntimeState,
        List<InventorySourceDescriptor> sourceDescriptors,
        List<QuickAccessLaneDescriptor> quickAccessLanes,
        List<EquipmentGroupDescriptor> equipmentGroups,
        List<InventoryToolDescriptor> toolDescriptors,
        boolean slotOwned,
        boolean recordsRecent,
        boolean carriedOnly,
        String diagnostics
) {
    public InventoryHostDescriptor {
        hostId = hostId == null ? HostInstanceKey.empty() : hostId;
        serverMenuRef = serverMenuRef == null ? new ServerMenuRef("", -1) : serverMenuRef;
        screenClassName = screenClassName == null ? "" : screenClassName;
        title = title == null ? Component.empty() : title;
        Objects.requireNonNull(menu, "menu");
        topology = topology == null ? InventoryTopologyDescriptor.empty() : topology;
        hostSession = hostSession == null ? InventoryHostSession.empty() : hostSession;
        playerExtensions = playerExtensions == null ? List.of() : List.copyOf(playerExtensions);
        playerRuntimeState = playerRuntimeState == null ? PlayerRuntimeStateDescriptor.vanilla(-1) : playerRuntimeState;
        sourceDescriptors = sourceDescriptors == null ? List.of() : List.copyOf(sourceDescriptors);
        quickAccessLanes = quickAccessLanes == null ? List.of() : List.copyOf(quickAccessLanes);
        equipmentGroups = equipmentGroups == null ? List.of() : List.copyOf(equipmentGroups);
        toolDescriptors = toolDescriptors == null ? List.of() : List.copyOf(toolDescriptors);
        diagnostics = diagnostics == null ? "" : diagnostics;
    }

    public InventorySourceDescriptor source(String sourceId) {
        if (sourceId == null || sourceId.isBlank()) {
            return null;
        }
        return sourceDescriptors.stream()
                .filter(source -> source.id().equals(sourceId))
                .findFirst()
                .orElse(null);
    }

    public QuickAccessLaneDescriptor quickAccessLane(String laneId) {
        if (laneId == null || laneId.isBlank()) {
            return null;
        }
        return quickAccessLanes.stream()
                .filter(lane -> lane.id().equals(laneId))
                .findFirst()
                .orElse(null);
    }

    public EquipmentGroupDescriptor equipmentGroup(String groupId) {
        if (groupId == null || groupId.isBlank()) {
            return null;
        }
        return equipmentGroups.stream()
                .filter(group -> group.id().equals(groupId))
                .findFirst()
                .orElse(null);
    }

    public InventoryToolDescriptor tool(String toolId) {
        if (toolId == null || toolId.isBlank()) {
            return null;
        }
        return toolDescriptors.stream()
                .filter(tool -> tool.id().equals(toolId))
                .findFirst()
                .orElse(null);
    }

    public boolean ownsHostSource(String sourceId) {
        if (sourceId == null || sourceId.isBlank()) {
            return false;
        }
        return hostSession.hostSources().stream().anyMatch(source -> sourceId.equals(source.id()));
    }

    public PlayerInventoryExtension extensionOwningSource(String sourceId) {
        if (sourceId == null || sourceId.isBlank()) {
            return null;
        }
        for (PlayerInventoryExtension extension : playerExtensions) {
            if (extension == null) {
                continue;
            }
            boolean ownsSource = extension.additionalSources().stream()
                    .anyMatch(source -> source != null && sourceId.equals(source.id()));
            if (ownsSource) {
                return extension;
            }
        }
        return null;
    }

    public InventorySourceDescriptor quickAccessSource(String laneId) {
        QuickAccessLaneDescriptor lane = quickAccessLane(laneId);
        return lane == null ? null : source(lane.sourceId());
    }

    public InventorySourceDescriptor equipmentSource(String groupId) {
        EquipmentGroupDescriptor group = equipmentGroup(groupId);
        return group == null ? null : source(group.sourceId());
    }

    public List<InventorySourceDescriptor> carriedSources() {
        return sourceDescriptors.stream().filter(InventorySourceDescriptor::inCarriedPane).toList();
    }

    public List<InventorySourceDescriptor> externalSources() {
        return sourceDescriptors.stream().filter(InventorySourceDescriptor::inExternalPane).toList();
    }

    public Map<String, InventorySourceDescriptor> sourceIndex() {
        LinkedHashMap<String, InventorySourceDescriptor> index = new LinkedHashMap<>();
        sourceDescriptors.forEach(source -> index.put(source.id(), source));
        return Map.copyOf(index);
    }

    public static ServerMenuRef serverMenuRef(AbstractContainerMenu menu) {
        if (menu == null) {
            return new ServerMenuRef("", -1);
        }
        return new ServerMenuRef(menu.getClass().getName(), menu.containerId);
    }
}
