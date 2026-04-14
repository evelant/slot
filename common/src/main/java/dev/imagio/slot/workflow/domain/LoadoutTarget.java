package dev.imagio.slot.workflow.domain;

public sealed interface LoadoutTarget permits
        LoadoutTarget.QuickAccessLaneTarget,
        LoadoutTarget.EquipmentSlotTarget {

    String stableKey();

    record QuickAccessLaneTarget(String laneId, int slotIndex) implements LoadoutTarget {
        public QuickAccessLaneTarget {
            laneId = laneId == null ? "" : laneId;
            slotIndex = Math.max(0, slotIndex);
        }

        @Override
        public String stableKey() {
            return "quick_access:" + laneId + "#" + slotIndex;
        }
    }

    record EquipmentSlotTarget(String groupId, int slotIndex) implements LoadoutTarget {
        public EquipmentSlotTarget {
            groupId = groupId == null ? "" : groupId;
            slotIndex = Math.max(0, slotIndex);
        }

        @Override
        public String stableKey() {
            return "equipment:" + groupId + "#" + slotIndex;
        }
    }
}
