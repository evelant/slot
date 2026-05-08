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
        if (target instanceof InventoryActionTarget.CursorTarget) {
            return "cursor";
        }
        if (target instanceof InventoryActionTarget.SourceTarget sourceTarget) {
            return "source:" + normalize(sourceTarget.sourceId());
        }
        if (target instanceof InventoryActionTarget.SourceEntryTarget sourceEntryTarget) {
            return "entry:" + normalize(sourceEntryTarget.sourceId()) + "@" + normalize(sourceEntryTarget.entryId());
        }
        if (target instanceof InventoryActionTarget.QuickAccessTarget quickAccessTarget) {
            return "quick_access:" + normalize(quickAccessTarget.laneId()) + "#" + normalizeIndex(quickAccessTarget.slotIndex());
        }
        if (target instanceof InventoryActionTarget.EquipmentTarget equipmentTarget) {
            return "equipment:" + normalize(equipmentTarget.groupId()) + "#" + normalizeIndex(equipmentTarget.slotIndex());
        }
        if (target instanceof InventoryActionTarget.SourceSlotTarget slotTarget) {
            return canonicalSourceSlot(host, slotTarget.sourceId(), slotTarget.slotIndex());
        }
        if (target instanceof InventoryActionTarget.ToolRegionTarget toolRegionTarget) {
            return canonicalToolRegion(host, toolRegionTarget);
        }
        if (target instanceof InventoryActionTarget.ToolControlTarget toolControlTarget) {
            return "tool_control:" + normalize(toolControlTarget.toolId()) + "|" + normalize(toolControlTarget.controlId());
        }
        return "";
    }

    public static String canonicalKey(LoadoutTarget target) {
        if (target == null) {
            return "";
        }
        if (target instanceof LoadoutTarget.QuickAccessLaneTarget quickAccessLaneTarget) {
            return "quick_access:" + normalize(quickAccessLaneTarget.laneId()) + "#" + normalizeIndex(quickAccessLaneTarget.slotIndex());
        }
        if (target instanceof LoadoutTarget.EquipmentSlotTarget equipmentSlotTarget) {
            return "equipment:" + normalize(equipmentSlotTarget.groupId()) + "#" + normalizeIndex(equipmentSlotTarget.slotIndex());
        }
        return "";
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
        if (target instanceof InventoryActionTarget.QuickAccessTarget quickAccessTarget) {
            dev.imagio.slot.inventory.core.QuickAccessLaneDescriptor lane = host.quickAccessLane(quickAccessTarget.laneId());
            return lane == null ? target : new InventoryActionTarget.SourceSlotTarget(lane.sourceId(), quickAccessTarget.slotIndex());
        }
        if (target instanceof InventoryActionTarget.EquipmentTarget equipmentTarget) {
            EquipmentGroupDescriptor group = host.equipmentGroup(equipmentTarget.groupId());
            return group == null ? target : new InventoryActionTarget.SourceSlotTarget(group.sourceId(), equipmentTarget.slotIndex());
        }
        if (target instanceof InventoryActionTarget.ToolRegionTarget toolRegionTarget) {
            InventoryToolDescriptor tool = host.tool(toolRegionTarget.toolId());
            ToolRegionDescriptor region = tool == null ? null : tool.regions().stream()
                    .filter(candidate -> candidate != null && toolRegionTarget.regionId().equals(candidate.id()))
                    .findFirst()
                    .orElse(null);
            return region == null || region.linkedSourceId().isBlank()
                    ? target
                    : new InventoryActionTarget.SourceSlotTarget(region.linkedSourceId(), toolRegionTarget.slotIndex());
        }
        return target;
    }

    private static String normalize(String value) {
        return value == null ? "" : value;
    }

    private static int normalizeIndex(int value) {
        return Math.max(0, value);
    }
}
