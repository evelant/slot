package dev.imagio.slot.inventory.action;

public sealed interface InventoryActionTarget permits
        InventoryActionTarget.CursorTarget,
        InventoryActionTarget.SourceTarget,
        InventoryActionTarget.SourceSlotTarget,
        InventoryActionTarget.SourceEntryTarget,
        InventoryActionTarget.QuickAccessTarget,
        InventoryActionTarget.EquipmentTarget,
        InventoryActionTarget.ToolRegionTarget,
        InventoryActionTarget.ToolControlTarget {

    String stableKey();

    record CursorTarget() implements InventoryActionTarget {
        @Override
        public String stableKey() {
            return "cursor";
        }
    }

    record SourceTarget(String sourceId) implements InventoryActionTarget {
        @Override
        public String stableKey() {
            return "source:" + (sourceId == null ? "" : sourceId);
        }
    }

    record SourceSlotTarget(String sourceId, int slotIndex) implements InventoryActionTarget {
        @Override
        public String stableKey() {
            return "source:" + (sourceId == null ? "" : sourceId) + "#" + Math.max(0, slotIndex);
        }
    }

    record SourceEntryTarget(String sourceId, String entryId) implements InventoryActionTarget {
        @Override
        public String stableKey() {
            return "entry:" + (sourceId == null ? "" : sourceId) + "@" + (entryId == null ? "" : entryId);
        }
    }

    record QuickAccessTarget(String laneId, int slotIndex) implements InventoryActionTarget {
        @Override
        public String stableKey() {
            return "quick_access:" + (laneId == null ? "" : laneId) + "#" + Math.max(0, slotIndex);
        }
    }

    record EquipmentTarget(String groupId, int slotIndex) implements InventoryActionTarget {
        @Override
        public String stableKey() {
            return "equipment:" + (groupId == null ? "" : groupId) + "#" + Math.max(0, slotIndex);
        }
    }

    record ToolRegionTarget(String toolId, String regionId, int slotIndex) implements InventoryActionTarget {
        @Override
        public String stableKey() {
            return "tool_region:" + (toolId == null ? "" : toolId) + "|" + (regionId == null ? "" : regionId) + "#" + Math.max(0, slotIndex);
        }
    }

    record ToolControlTarget(String toolId, String controlId) implements InventoryActionTarget {
        @Override
        public String stableKey() {
            return "tool_control:" + (toolId == null ? "" : toolId) + "|" + (controlId == null ? "" : controlId);
        }
    }
}
