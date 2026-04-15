package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.browse.InventoryBrowseSubjectRef;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public sealed interface InventoryWorkspaceSubjectRef permits
        InventoryWorkspaceSubjectRef.BrowseRef,
        InventoryWorkspaceSubjectRef.QuickAccessLaneRef,
        InventoryWorkspaceSubjectRef.QuickAccessSlotRef,
        InventoryWorkspaceSubjectRef.EquipmentGroupRef,
        InventoryWorkspaceSubjectRef.EquipmentSlotRef,
        InventoryWorkspaceSubjectRef.ToolRef,
        InventoryWorkspaceSubjectRef.ToolRegionSlotRef,
        InventoryWorkspaceSubjectRef.CraftingInputRef,
        InventoryWorkspaceSubjectRef.CraftingOutputRef,
        InventoryWorkspaceSubjectRef.WorkflowRef,
        InventoryWorkspaceSubjectRef.StatusRef {

    String stableKey();

    static InventoryWorkspaceSubjectRef parse(String stableKey) {
        if (stableKey == null || stableKey.isBlank()) {
            return null;
        }
        String[] parts = stableKey.split("\\|", -1);
        if (parts.length == 0) {
            return null;
        }
        return switch (parts[0]) {
            case "browse" -> parts.length == 2 ? new BrowseRef(InventoryBrowseSubjectRef.parse(decode(parts[1]))) : null;
            case "quick_access_lane" -> parts.length == 2 ? new QuickAccessLaneRef(decode(parts[1])) : null;
            case "quick_access_slot" -> parts.length == 3 ? new QuickAccessSlotRef(decode(parts[1]), parseIndex(parts[2])) : null;
            case "equipment_group" -> parts.length == 2 ? new EquipmentGroupRef(decode(parts[1])) : null;
            case "equipment_slot" -> parts.length == 3 ? new EquipmentSlotRef(decode(parts[1]), parseIndex(parts[2])) : null;
            case "tool" -> parts.length == 2 ? new ToolRef(decode(parts[1])) : null;
            case "tool_region_slot" -> parts.length == 4 ? new ToolRegionSlotRef(decode(parts[1]), decode(parts[2]), parseIndex(parts[3])) : null;
            case "crafting_input" -> parts.length == 3 ? new CraftingInputRef(decode(parts[1]), parseIndex(parts[2])) : null;
            case "crafting_output" -> parts.length == 2 ? new CraftingOutputRef(decode(parts[1])) : null;
            case "workflow" -> parts.length == 2 ? new WorkflowRef(decode(parts[1])) : null;
            case "status" -> parts.length == 2 ? new StatusRef(decode(parts[1])) : null;
            default -> null;
        };
    }

    private static int parseIndex(String value) {
        try {
            return Math.max(0, Integer.parseInt(value == null ? "0" : value));
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private static String encode(String value) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }

    record BrowseRef(InventoryBrowseSubjectRef browseSubjectRef) implements InventoryWorkspaceSubjectRef {
        @Override
        public String stableKey() {
            return "browse|" + encode(browseSubjectRef == null ? "" : browseSubjectRef.stableKey());
        }
    }

    record QuickAccessLaneRef(String laneId) implements InventoryWorkspaceSubjectRef {
        public QuickAccessLaneRef {
            laneId = laneId == null ? "" : laneId;
        }

        @Override
        public String stableKey() {
            return "quick_access_lane|" + encode(laneId);
        }
    }

    record QuickAccessSlotRef(String laneId, int slotIndex) implements InventoryWorkspaceSubjectRef {
        public QuickAccessSlotRef {
            laneId = laneId == null ? "" : laneId;
            slotIndex = Math.max(0, slotIndex);
        }

        @Override
        public String stableKey() {
            return "quick_access_slot|" + encode(laneId) + "|" + slotIndex;
        }
    }

    record EquipmentGroupRef(String groupId) implements InventoryWorkspaceSubjectRef {
        public EquipmentGroupRef {
            groupId = groupId == null ? "" : groupId;
        }

        @Override
        public String stableKey() {
            return "equipment_group|" + encode(groupId);
        }
    }

    record EquipmentSlotRef(String groupId, int slotIndex) implements InventoryWorkspaceSubjectRef {
        public EquipmentSlotRef {
            groupId = groupId == null ? "" : groupId;
            slotIndex = Math.max(0, slotIndex);
        }

        @Override
        public String stableKey() {
            return "equipment_slot|" + encode(groupId) + "|" + slotIndex;
        }
    }

    record ToolRef(String toolId) implements InventoryWorkspaceSubjectRef {
        public ToolRef {
            toolId = toolId == null ? "" : toolId;
        }

        @Override
        public String stableKey() {
            return "tool|" + encode(toolId);
        }
    }

    record ToolRegionSlotRef(String toolId, String regionId, int slotIndex) implements InventoryWorkspaceSubjectRef {
        public ToolRegionSlotRef {
            toolId = toolId == null ? "" : toolId;
            regionId = regionId == null ? "" : regionId;
            slotIndex = Math.max(0, slotIndex);
        }

        @Override
        public String stableKey() {
            return "tool_region_slot|" + encode(toolId) + "|" + encode(regionId) + "|" + slotIndex;
        }
    }

    record CraftingInputRef(String toolId, int inputIndex) implements InventoryWorkspaceSubjectRef {
        public CraftingInputRef {
            toolId = toolId == null ? "" : toolId;
            inputIndex = Math.max(0, inputIndex);
        }

        @Override
        public String stableKey() {
            return "crafting_input|" + encode(toolId) + "|" + inputIndex;
        }
    }

    record CraftingOutputRef(String toolId) implements InventoryWorkspaceSubjectRef {
        public CraftingOutputRef {
            toolId = toolId == null ? "" : toolId;
        }

        @Override
        public String stableKey() {
            return "crafting_output|" + encode(toolId);
        }
    }

    record WorkflowRef(String surfaceId) implements InventoryWorkspaceSubjectRef {
        public WorkflowRef {
            surfaceId = surfaceId == null ? "" : surfaceId;
        }

        @Override
        public String stableKey() {
            return "workflow|" + encode(surfaceId);
        }
    }

    record StatusRef(String surfaceId) implements InventoryWorkspaceSubjectRef {
        public StatusRef {
            surfaceId = surfaceId == null ? "" : surfaceId;
        }

        @Override
        public String stableKey() {
            return "status|" + encode(surfaceId);
        }
    }
}
