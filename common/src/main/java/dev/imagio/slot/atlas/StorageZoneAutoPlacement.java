package dev.imagio.slot.atlas;

import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ClaimedChest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class StorageZoneAutoPlacement {
    private StorageZoneAutoPlacement() {
    }

    public static Result compute(List<ClaimedChest> existing, ChestAnchor newAnchor, Config config) {
        if (newAnchor == null) {
            throw new IllegalArgumentException("newAnchor must not be null");
        }
        Config resolved = config == null ? Config.defaults() : config;

        Neighbor nearest = findNearestNeighbor(existing, newAnchor, resolved.worldRadius());
        int baseX;
        int baseY;
        if (nearest == null) {
            baseX = resolved.defaultSeedX();
            baseY = resolved.defaultSeedY();
        } else {
            int deltaWorldX = newAnchor.x() - nearest.anchor().x();
            int deltaWorldZ = newAnchor.z() - nearest.anchor().z();
            baseX = nearest.chest().atlasX() + scaleToAtlas(deltaWorldX, resolved.worldToAtlasScale());
            baseY = nearest.chest().atlasY() + scaleToAtlas(deltaWorldZ, resolved.worldToAtlasScale());
        }

        int snappedX = snap(baseX, resolved.atlasStepX());
        int snappedY = snap(baseY, resolved.atlasStepY());

        Set<Long> occupied = buildOccupiedSet(existing, resolved);
        int[] freeCell = findFreeCell(snappedX, snappedY, resolved, occupied);
        return new Result(freeCell[0], freeCell[1], nearest != null);
    }

    private static Neighbor findNearestNeighbor(
            List<ClaimedChest> existing,
            ChestAnchor newAnchor,
            int worldRadius
    ) {
        if (existing == null || existing.isEmpty()) {
            return null;
        }
        long radiusSquared = (long) worldRadius * worldRadius;
        Neighbor best = null;
        long bestDistance = Long.MAX_VALUE;
        for (ClaimedChest chest : existing) {
            if (chest == null) {
                continue;
            }
            for (ChestAnchor anchor : chest.anchors()) {
                if (!anchor.sameDimension(newAnchor)) {
                    continue;
                }
                long distance = anchor.squaredDistanceTo(newAnchor);
                if (distance <= radiusSquared && distance < bestDistance) {
                    bestDistance = distance;
                    best = new Neighbor(chest, anchor);
                }
            }
        }
        return best;
    }

    private static int scaleToAtlas(int worldDelta, double scale) {
        return (int) Math.round(worldDelta * scale);
    }

    private static int snap(int value, int step) {
        if (step <= 0) {
            return value;
        }
        return Math.floorDiv(value + (step / 2), step) * step;
    }

    private static Set<Long> buildOccupiedSet(List<ClaimedChest> existing, Config config) {
        Set<Long> cells = new HashSet<>();
        if (existing == null) {
            return cells;
        }
        for (ClaimedChest chest : existing) {
            if (chest == null) {
                continue;
            }
            int cellX = snap(chest.atlasX(), config.atlasStepX());
            int cellY = snap(chest.atlasY(), config.atlasStepY());
            cells.add(packCell(cellX, cellY));
        }
        return cells;
    }

    private static int[] findFreeCell(int startX, int startY, Config config, Set<Long> occupied) {
        if (!occupied.contains(packCell(startX, startY))) {
            return new int[]{startX, startY};
        }
        int stepX = Math.max(1, config.atlasStepX());
        int stepY = Math.max(1, config.atlasStepY());
        for (int radius = 1; radius <= 16; radius++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dx = -radius; dx <= radius; dx++) {
                    if (Math.abs(dx) != radius && Math.abs(dy) != radius) {
                        continue;
                    }
                    int candidateX = startX + dx * stepX;
                    int candidateY = startY + dy * stepY;
                    if (!occupied.contains(packCell(candidateX, candidateY))) {
                        return new int[]{candidateX, candidateY};
                    }
                }
            }
        }
        return new int[]{startX, startY};
    }

    private static long packCell(int x, int y) {
        return (((long) x) << 32) | (y & 0xFFFFFFFFL);
    }

    public record Config(
            int worldRadius,
            int atlasStepX,
            int atlasStepY,
            int defaultSeedX,
            int defaultSeedY,
            double worldToAtlasScale
    ) {
        public Config {
            worldRadius = Math.max(1, worldRadius);
            atlasStepX = Math.max(1, atlasStepX);
            atlasStepY = Math.max(1, atlasStepY);
            worldToAtlasScale = worldToAtlasScale <= 0.0 ? 1.0 : worldToAtlasScale;
        }

        public static Config defaults() {
            return new Config(48, 160, 160, 2400, 0, 4.0);
        }
    }

    public record Result(int atlasX, int atlasY, boolean usedNeighbor) {
    }

    private record Neighbor(ClaimedChest chest, ChestAnchor anchor) {
    }
}
