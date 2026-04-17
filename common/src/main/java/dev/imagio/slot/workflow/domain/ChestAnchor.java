package dev.imagio.slot.workflow.domain;

public record ChestAnchor(
        String dimensionId,
        int x,
        int y,
        int z
) {
    public ChestAnchor {
        if (dimensionId == null || dimensionId.isBlank()) {
            throw new IllegalArgumentException("dimensionId must not be blank");
        }
        dimensionId = dimensionId.trim();
    }

    public boolean sameDimension(ChestAnchor other) {
        return other != null && dimensionId.equals(other.dimensionId);
    }

    public long squaredDistanceTo(ChestAnchor other) {
        if (other == null || !sameDimension(other)) {
            return Long.MAX_VALUE;
        }
        long dx = (long) x - other.x;
        long dy = (long) y - other.y;
        long dz = (long) z - other.z;
        return dx * dx + dy * dy + dz * dz;
    }
}
