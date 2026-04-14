package dev.imagio.slot.inventory.action;

import dev.imagio.slot.inventory.core.EquipmentGroupDescriptor;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceRole;
import dev.imagio.slot.inventory.core.InventoryToolDescriptor;
import dev.imagio.slot.inventory.core.ToolRegionDescriptor;
import dev.imagio.slot.workflow.domain.LoadoutTarget;

public final class InventoryTargetCanonicalizer {
    private InventoryTargetCanonicalizer() {
    }

    public static String canonicalKey(InventoryHostDescriptor host, InventoryActionTarget target) {
        if (target == null) {
            return "";
        }
        return switch (target) {
            case InventoryActionTarget.CursorTarget ignored -> "cursor";
            case InventoryActionTarget.SourceTarget sourceTarget -> "source:" + normalize(sourceTarget.sourceId());
            case InventoryActionTarget.SourceEntryTarget sourceEntryTarget ->
                    "entry:" + normalize(sourceEntryTarget.sourceId()) + "@" + normalize(sourceEntryTarget.entryId());
            case InventoryActionTarget.QuickAccessTarget quickAccessTarget ->
                    "quick_access:" + normalize(quickAccessTarget.laneId()) + "#" + normalizeIndex(quickAccessTarget.slotIndex());
            case InventoryActionTarget.EquipmentTarget equipmentTarget ->
                    "equipment:" + normalize(equipmentTarget.groupId()) + "#" + normalizeIndex(equipmentTarget.slotIndex());
            case InventoryActionTarget.SourceSlotTarget slotTarget -> canonicalSourceSlot(host, slotTarget.sourceId(), slotTarget.slotIndex());
            case InventoryActionTarget.ToolRegionTarget toolRegionTarget -> canonicalToolRegion(host, toolRegionTarget);
            case InventoryActionTarget.ToolControlTarget toolControlTarget ->
                    "tool_control:" + normalize(toolControlTarget.toolId()) + "|" + normalize(toolControlTarget.controlId());
        };
    }

    public static String canonicalKey(LoadoutTarget target) {
        if (target == null) {
            return "";
        }
        return switch (target) {
            case LoadoutTarget.QuickAccessLaneTarget quickAccessLaneTarget ->
                    "quick_access:" + normalize(quickAccessLaneTarget.laneId()) + "#" + normalizeIndex(quickAccessLaneTarget.slotIndex());
            case LoadoutTarget.EquipmentSlotTarget equipmentSlotTarget ->
                    "equipment:" + normalize(equipmentSlotTarget.groupId()) + "#" + normalizeIndex(equipmentSlotTarget.slotIndex());
        };
    }

    public static String canonicalSourceSlot(InventoryHostDescriptor host, String sourceId, int slotIndex) {
        InventorySourceDescriptor source = host == null ? null : host.source(sourceId);
        if (source == null) {
            if (BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0.equals(sourceId)) {
                return "quick_access:" + normalize(BuiltinInventoryIds.QUICK_ACCESS_LANE_0) + "#" + normalizeIndex(slotIndex);
            }
            if (BuiltinInventoryIds.PLAYER_ARMOR.equals(sourceId)) {
                return "equipment:" + normalize(BuiltinInventoryIds.EQUIPMENT_GROUP_ARMOR) + "#" + normalizeIndex(slotIndex);
            }
            if (BuiltinInventoryIds.PLAYER_OFFHAND.equals(sourceId)) {
                return "equipment:" + normalize(BuiltinInventoryIds.EQUIPMENT_GROUP_OFFHAND) + "#" + normalizeIndex(slotIndex);
            }
            return "source:" + normalize(sourceId) + "#" + normalizeIndex(slotIndex);
        }
        if (source.role() == InventorySourceRole.QUICK_ACCESS && !source.laneId().isBlank()) {
            return "quick_access:" + normalize(source.laneId()) + "#" + normalizeIndex(slotIndex);
        }
        if ((source.role() == InventorySourceRole.EQUIPMENT || source.role() == InventorySourceRole.OFFHAND) && !source.groupId().isBlank()) {
            return "equipment:" + normalize(source.groupId()) + "#" + normalizeIndex(slotIndex);
        }
        return "source:" + normalize(source.id()) + "#" + normalizeIndex(slotIndex);
    }

    private static String canonicalToolRegion(
            InventoryHostDescriptor host,
            InventoryActionTarget.ToolRegionTarget target
    ) {
        if (host == null) {
            return "tool_region:" + normalize(target.toolId()) + "|" + normalize(target.regionId()) + "#" + normalizeIndex(target.slotIndex());
        }
        InventoryToolDescriptor tool = host.tool(target.toolId());
        ToolRegionDescriptor region = tool == null ? null : tool.regions().stream()
                .filter(candidate -> candidate != null && candidate.id().equals(target.regionId()))
                .findFirst()
                .orElse(null);
        String keyStem = region == null || region.linkedSourceId().isBlank()
                ? normalize(target.toolId()) + "|" + normalize(target.regionId())
                : null;
        if (region != null && !region.linkedSourceId().isBlank()) {
            return canonicalSourceSlot(host, region.linkedSourceId(), target.slotIndex());
        }
        return "tool_region:" + keyStem + "#" + normalizeIndex(target.slotIndex());
    }

    public static InventoryActionTarget canonicalTarget(InventoryHostDescriptor host, InventoryActionTarget target) {
        if (host == null || target == null) {
            return target;
        }
        return switch (target) {
            case InventoryActionTarget.SourceTarget ignored -> target;
            case InventoryActionTarget.SourceSlotTarget ignored -> target;
            case InventoryActionTarget.SourceEntryTarget ignored -> target;
            case InventoryActionTarget.QuickAccessTarget quickAccessTarget -> {
                dev.imagio.slot.inventory.core.QuickAccessLaneDescriptor lane = host.quickAccessLane(quickAccessTarget.laneId());
                yield lane == null ? target : new InventoryActionTarget.SourceSlotTarget(lane.sourceId(), quickAccessTarget.slotIndex());
            }
            case InventoryActionTarget.EquipmentTarget equipmentTarget -> {
                EquipmentGroupDescriptor group = host.equipmentGroup(equipmentTarget.groupId());
                yield group == null ? target : new InventoryActionTarget.SourceSlotTarget(group.sourceId(), equipmentTarget.slotIndex());
            }
            case InventoryActionTarget.ToolRegionTarget toolRegionTarget -> {
                InventoryToolDescriptor tool = host.tool(toolRegionTarget.toolId());
                ToolRegionDescriptor region = tool == null ? null : tool.regions().stream()
                        .filter(candidate -> candidate != null && toolRegionTarget.regionId().equals(candidate.id()))
                        .findFirst()
                        .orElse(null);
                yield region == null || region.linkedSourceId().isBlank()
                        ? target
                        : new InventoryActionTarget.SourceSlotTarget(region.linkedSourceId(), toolRegionTarget.slotIndex());
            }
            default -> target;
        };
    }

    private static String normalize(String value) {
        return value == null ? "" : value;
    }

    private static int normalizeIndex(int value) {
        return Math.max(0, value);
    }
}
