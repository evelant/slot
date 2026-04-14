package dev.imagio.slot.recent;

public enum AcquisitionProducerId {
    UNKNOWN(""),
    WORLD_PICKUP("world_pickup"),
    BACKPACK_PICKUP("backpack_pickup"),
    EXTERNAL_WITHDRAWAL("external_withdrawal"),
    CRAFT_RESULT("craft_result");

    private final String serializedId;

    AcquisitionProducerId(String serializedId) {
        this.serializedId = serializedId == null ? "" : serializedId;
    }

    public String serializedId() {
        return serializedId;
    }

    public static AcquisitionProducerId fromSerializedId(String serializedId) {
        if (serializedId == null || serializedId.isBlank()) {
            return UNKNOWN;
        }
        for (AcquisitionProducerId value : values()) {
            if (value.serializedId.equals(serializedId)) {
                return value;
            }
        }
        return UNKNOWN;
    }
}
