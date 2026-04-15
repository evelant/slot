package dev.imagio.slot.inventory.integration;

import dev.imagio.slot.inventory.core.BuiltinInventoryDescriptors;
import dev.imagio.slot.inventory.core.EquipmentGroupDescriptor;
import dev.imagio.slot.inventory.core.HostInstanceKey;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.InventoryToolDescriptor;
import dev.imagio.slot.inventory.core.InventoryTopologyDescriptor;
import dev.imagio.slot.inventory.core.MenuBackedPlayerTopologyResolver;
import dev.imagio.slot.inventory.core.PlayerRuntimeStateDescriptor;
import dev.imagio.slot.inventory.core.QuickAccessLaneDescriptor;
import dev.imagio.slot.inventory.core.ToolRegionDescriptor;
import dev.imagio.slot.registry.ProviderResult;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class InventoryHostResolver {
    private InventoryHostResolver() {
    }

    public static InventoryHostDescriptor resolve(InventoryHostContext context) {
        if (context == null || context.menu() == null || context.playerInventory() == null) {
            return null;
        }

        ProviderResult<InventoryHostSession> hostResult = InventoryIntegrationRegistry.openHost(context);
        InventoryHostSession hostSession = hostResult != null && hostResult.supported()
                ? hostResult.value()
                : InventoryHostSession.empty();

        InventoryTopologyDescriptor topology = mergeTopologies(
                MenuBackedPlayerTopologyResolver.resolve(context.menu(), context.playerInventory()),
                hostSession.topology()
        );

        List<InventorySourceDescriptor> sources = new ArrayList<>(BuiltinInventoryDescriptors.builtInPlayerSources(topology));
        appendUniqueSources(sources, hostSession.hostSources(), "host:" + hostSession.providerId());

        InventoryHostDescriptor preliminaryHost = new InventoryHostDescriptor(
                HostInstanceKey.of(context.menu(), hostSession.providerId(), hostSession.providerScopeId()),
                InventoryHostDescriptor.serverMenuRef(context.menu()),
                context.screenClassName(),
                context.title(),
                context.menu(),
                topology,
                hostSession,
                List.of(),
                PlayerRuntimeStateDescriptor.vanilla(context.playerInventory().selected),
                List.copyOf(sources),
                BuiltinInventoryDescriptors.builtInQuickAccessLanes(),
                BuiltinInventoryDescriptors.builtInEquipmentGroups(),
                hostSession.tools(),
                context.observationHints(),
                hostResult == null ? "" : hostResult.diagnostics().summary()
        );

        List<PlayerInventoryExtension> extensions = InventoryIntegrationRegistry.playerExtensions(
                new PlayerInventoryContext(
                        context.playerInventory(),
                        context.menu(),
                        context.screenClassName(),
                        preliminaryHost
                )
        );

        List<InventorySourceDescriptor> mergedSources = new ArrayList<>(preliminaryHost.sourceDescriptors());
        List<QuickAccessLaneDescriptor> mergedLanes = new ArrayList<>(preliminaryHost.quickAccessLanes());
        List<EquipmentGroupDescriptor> mergedGroups = new ArrayList<>(preliminaryHost.equipmentGroups());
        for (PlayerInventoryExtension extension : extensions) {
            appendUniqueSources(mergedSources, extension.additionalSources(), "extension:" + extension.providerId());
            appendUniqueLanes(mergedLanes, extension.additionalQuickAccessLanes(), "extension:" + extension.providerId());
            appendUniqueGroups(mergedGroups, extension.additionalEquipmentGroups(), "extension:" + extension.providerId());
        }

        validateUniqueToolIds(hostSession.tools());
        validateToolRegionLinks(mergedSources, hostSession.tools());

        return new InventoryHostDescriptor(
                preliminaryHost.hostId(),
                preliminaryHost.serverMenuRef(),
                preliminaryHost.screenClassName(),
                preliminaryHost.title(),
                preliminaryHost.menu(),
                preliminaryHost.topology(),
                preliminaryHost.hostSession(),
                extensions,
                preliminaryHost.playerRuntimeState(),
                List.copyOf(mergedSources),
                List.copyOf(mergedLanes),
                List.copyOf(mergedGroups),
                preliminaryHost.toolDescriptors(),
                preliminaryHost.observationHints(),
                preliminaryHost.diagnostics()
        );
    }

    private static InventoryTopologyDescriptor mergeTopologies(
            InventoryTopologyDescriptor first,
            InventoryTopologyDescriptor second
    ) {
        Map<String, List<Integer>> menuSlotsBySource = new LinkedHashMap<>();
        if (first != null) {
            menuSlotsBySource.putAll(first.menuSlotsBySourceId());
        }
        if (second != null) {
            menuSlotsBySource.putAll(second.menuSlotsBySourceId());
        }

        Map<Integer, String> sourceIdByMenuSlot = new LinkedHashMap<>();
        if (first != null) {
            sourceIdByMenuSlot.putAll(first.sourceIdByMenuSlot());
        }
        if (second != null) {
            sourceIdByMenuSlot.putAll(second.sourceIdByMenuSlot());
        }

        Map<String, List<Integer>> toolRegionSlots = new LinkedHashMap<>();
        if (first != null) {
            toolRegionSlots.putAll(first.menuSlotsByToolRegionId());
        }
        if (second != null) {
            toolRegionSlots.putAll(second.menuSlotsByToolRegionId());
        }

        return new InventoryTopologyDescriptor(menuSlotsBySource, sourceIdByMenuSlot, toolRegionSlots);
    }

    private static void appendUniqueSources(
            List<InventorySourceDescriptor> target,
            List<InventorySourceDescriptor> additions,
            String owner
    ) {
        if (additions == null || additions.isEmpty()) {
            return;
        }
        LinkedHashMap<String, InventorySourceDescriptor> merged = new LinkedHashMap<>();
        for (InventorySourceDescriptor source : target) {
            merged.put(source.id(), source);
        }
        for (InventorySourceDescriptor source : additions) {
            if (source != null) {
                if (merged.containsKey(source.id())) {
                    throw new IllegalStateException("duplicate_source_id:" + source.id() + ":" + owner);
                }
                merged.put(source.id(), source);
            }
        }
        target.clear();
        target.addAll(merged.values());
    }

    private static void appendUniqueLanes(
            List<QuickAccessLaneDescriptor> target,
            List<QuickAccessLaneDescriptor> additions,
            String owner
    ) {
        if (additions == null || additions.isEmpty()) {
            return;
        }
        LinkedHashMap<String, QuickAccessLaneDescriptor> merged = new LinkedHashMap<>();
        for (QuickAccessLaneDescriptor lane : target) {
            merged.put(lane.id(), lane);
        }
        for (QuickAccessLaneDescriptor lane : additions) {
            if (lane != null) {
                if (merged.containsKey(lane.id())) {
                    throw new IllegalStateException("duplicate_lane_id:" + lane.id() + ":" + owner);
                }
                merged.put(lane.id(), lane);
            }
        }
        target.clear();
        target.addAll(merged.values());
    }

    private static void appendUniqueGroups(
            List<EquipmentGroupDescriptor> target,
            List<EquipmentGroupDescriptor> additions,
            String owner
    ) {
        if (additions == null || additions.isEmpty()) {
            return;
        }
        LinkedHashMap<String, EquipmentGroupDescriptor> merged = new LinkedHashMap<>();
        for (EquipmentGroupDescriptor group : target) {
            merged.put(group.id(), group);
        }
        for (EquipmentGroupDescriptor group : additions) {
            if (group != null) {
                if (merged.containsKey(group.id())) {
                    throw new IllegalStateException("duplicate_equipment_group_id:" + group.id() + ":" + owner);
                }
                merged.put(group.id(), group);
            }
        }
        target.clear();
        target.addAll(merged.values());
    }

    private static void validateUniqueToolIds(List<InventoryToolDescriptor> tools) {
        if (tools == null || tools.isEmpty()) {
            return;
        }
        LinkedHashMap<String, InventoryToolDescriptor> seen = new LinkedHashMap<>();
        for (InventoryToolDescriptor tool : tools) {
            if (tool == null) {
                continue;
            }
            if (seen.putIfAbsent(tool.id(), tool) != null) {
                throw new IllegalStateException("duplicate_tool_id:" + tool.id());
            }
        }
    }

    private static void validateToolRegionLinks(
            List<InventorySourceDescriptor> sources,
            List<InventoryToolDescriptor> tools
    ) {
        if (tools == null || tools.isEmpty()) {
            return;
        }
        LinkedHashMap<String, InventorySourceDescriptor> sourceIndex = new LinkedHashMap<>();
        if (sources != null) {
            for (InventorySourceDescriptor source : sources) {
                if (source != null) {
                    sourceIndex.put(source.id(), source);
                }
            }
        }
        for (InventoryToolDescriptor tool : tools) {
            if (tool == null) {
                continue;
            }
            LinkedHashMap<String, ToolRegionDescriptor> regions = new LinkedHashMap<>();
            for (ToolRegionDescriptor region : tool.regions()) {
                if (region == null) {
                    continue;
                }
                if (regions.putIfAbsent(region.id(), region) != null) {
                    throw new IllegalStateException("duplicate_tool_region_id:" + tool.id() + ":" + region.id());
                }
                if (region.sourceLike() && region.linkedSourceId().isBlank()) {
                    throw new IllegalStateException("source_like_tool_region_missing_linked_source:" + tool.id() + ":" + region.id());
                }
                if (!region.linkedSourceId().isBlank() && !sourceIndex.containsKey(region.linkedSourceId())) {
                    throw new IllegalStateException("tool_region_unknown_linked_source:" + tool.id() + ":" + region.id() + ":" + region.linkedSourceId());
                }
            }
        }
    }
}
