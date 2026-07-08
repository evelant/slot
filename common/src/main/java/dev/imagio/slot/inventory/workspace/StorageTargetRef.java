package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.storage.WorldDisplayStorageKind;
import dev.imagio.slot.inventory.storage.WorldDisplayStorageSource;
import dev.imagio.slot.inventory.storage.WorldStorageAccess;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ClaimedChest;

import java.util.Set;
import java.util.UUID;

/**
 * Stable storage identity plus read/mutation capability flags for workspace
 * projections. The flags describe what the current index build may use; live
 * mutations still go through {@link WorldStorageAccess}.
 */
public record StorageTargetRef(
        String storageId,
        String targetKind,
        String label,
        String dimensionId,
        int x,
        int y,
        int z,
        boolean liveReadable,
        boolean depositTarget,
        boolean takeTarget,
        boolean remembered,
        boolean proximate
) {
    public static final String KIND_CLAIMED_CHEST = "claimed_chest";
    public static final String KIND_DISPLAY_PREFIX = "display:";
    public static final String KIND_AE2_NETWORK = "ae2_network";

    public StorageTargetRef {
        if (storageId == null || storageId.isBlank()) {
            throw new IllegalArgumentException("storageId must not be blank");
        }
        targetKind = targetKind == null || targetKind.isBlank() ? KIND_CLAIMED_CHEST : targetKind;
        label = label == null ? "" : label;
        dimensionId = dimensionId == null ? "" : dimensionId;
    }

    public static StorageTargetRef claimed(
            ClaimedChest chest,
            boolean liveReadable,
            boolean remembered,
            boolean proximate
    ) {
        return claimed(chest, liveReadable, remembered, proximate, true, true);
    }

    public static StorageTargetRef claimed(
            ClaimedChest chest,
            boolean liveReadable,
            boolean remembered,
            boolean proximate,
            boolean depositTarget,
            boolean takeTarget
    ) {
        if (chest == null) {
            return null;
        }
        ChestAnchor anchor = firstAnchor(chest.anchors());
        return claimed(
                chest.storageId(),
                anchor == null ? "" : anchor.dimensionId(),
                anchor == null ? 0 : anchor.x(),
                anchor == null ? 0 : anchor.y(),
                anchor == null ? 0 : anchor.z(),
                chest.label() == null || chest.label().isBlank() ? autoLabel(chest.storageId()) : chest.label(),
                liveReadable,
                remembered,
                proximate,
                depositTarget,
                takeTarget);
    }

    public static StorageTargetRef claimed(
            UUID storageId,
            String dimensionId,
            int x,
            int y,
            int z,
            String label,
            boolean liveReadable,
            boolean remembered,
            boolean proximate
    ) {
        return claimed(storageId, dimensionId, x, y, z, label, liveReadable, remembered, proximate, true, true);
    }

    public static StorageTargetRef claimed(
            UUID storageId,
            String dimensionId,
            int x,
            int y,
            int z,
            String label,
            boolean liveReadable,
            boolean remembered,
            boolean proximate,
            boolean depositTarget,
            boolean takeTarget
    ) {
        if (storageId == null) {
            return null;
        }
        return new StorageTargetRef(
                storageId.toString(),
                KIND_CLAIMED_CHEST,
                label == null || label.isBlank() ? autoLabel(storageId) : label,
                dimensionId,
                x,
                y,
                z,
                liveReadable,
                depositTarget,
                takeTarget,
                remembered,
                proximate);
    }

    public static StorageTargetRef display(
            WorldDisplayStorageSource source,
            boolean remembered,
            boolean proximate
    ) {
        if (source == null) {
            return null;
        }
        return display(
                source.storageId(),
                source.kind(),
                source.label(),
                source.dimensionId(),
                source.x(),
                source.y(),
                source.z(),
                true,
                remembered,
                proximate);
    }

    public static StorageTargetRef display(
            WorldStorageAccess.Target.Display target,
            String label,
            boolean liveReadable,
            boolean remembered,
            boolean proximate
    ) {
        if (target == null) {
            return null;
        }
        return display(
                target.storageId(),
                target.kind(),
                label,
                target.dimensionId(),
                target.x(),
                target.y(),
                target.z(),
                liveReadable,
                remembered,
                proximate);
    }

    private static StorageTargetRef display(
            String storageId,
            WorldDisplayStorageKind kind,
            String label,
            String dimensionId,
            int x,
            int y,
            int z,
            boolean liveReadable,
            boolean remembered,
            boolean proximate
    ) {
        WorldDisplayStorageKind resolvedKind = kind == null ? WorldDisplayStorageKind.PLACED_ITEM : kind;
        String targetKind = resolvedKind == WorldDisplayStorageKind.AE2_NETWORK
                ? KIND_AE2_NETWORK
                : KIND_DISPLAY_PREFIX + resolvedKind.key();
        return new StorageTargetRef(
                storageId == null || storageId.isBlank()
                        ? WorldDisplayStorageSource.storageId(resolvedKind, dimensionId, x, y, z)
                        : storageId,
                targetKind,
                label == null || label.isBlank() ? defaultDisplayLabel(resolvedKind, x, y, z) : label,
                dimensionId,
                x,
                y,
                z,
                liveReadable,
                resolvedKind.depositTarget(),
                true,
                remembered,
                proximate);
    }

    public boolean displayTarget() {
        return targetKind.startsWith(KIND_DISPLAY_PREFIX) || KIND_AE2_NETWORK.equals(targetKind);
    }

    public boolean trackedWorldStorage() {
        if (KIND_CLAIMED_CHEST.equals(targetKind) || KIND_AE2_NETWORK.equals(targetKind)) {
            return true;
        }
        WorldDisplayStorageKind kind = displayKind();
        return kind != null && kind.trackedStorage();
    }

    public boolean ae2Network() {
        return KIND_AE2_NETWORK.equals(targetKind);
    }

    public StorageTargetRef withDepositTarget(boolean depositTarget) {
        return new StorageTargetRef(
                storageId,
                targetKind,
                label,
                dimensionId,
                x,
                y,
                z,
                liveReadable,
                depositTarget,
                takeTarget,
                remembered,
                proximate);
    }

    public WorldDisplayStorageKind displayKind() {
        if (KIND_AE2_NETWORK.equals(targetKind)) {
            return WorldDisplayStorageKind.AE2_NETWORK;
        }
        if (!targetKind.startsWith(KIND_DISPLAY_PREFIX)) {
            return null;
        }
        return WorldDisplayStorageKind.fromKey(targetKind.substring(KIND_DISPLAY_PREFIX.length()));
    }

    private static ChestAnchor firstAnchor(Set<ChestAnchor> anchors) {
        if (anchors == null || anchors.isEmpty()) {
            return null;
        }
        return anchors.iterator().next();
    }

    private static String autoLabel(UUID storageId) {
        String raw = storageId == null ? "" : storageId.toString();
        int dash = raw.indexOf('-');
        String shortId = dash < 0 ? raw : raw.substring(0, dash);
        if (shortId.length() > 4) {
            shortId = shortId.substring(shortId.length() - 4);
        }
        return shortId.isBlank() ? "Chest" : "Chest #" + shortId;
    }

    private static String defaultDisplayLabel(WorldDisplayStorageKind kind, int x, int y, int z) {
        String base = switch (kind) {
            case TOOL_RACK -> "Tool rack";
            case PLACED_ITEM -> "Placed item";
            case AE2_TERMINAL, AE2_NETWORK -> "ME network";
        };
        return base + " @ " + x + "," + y + "," + z;
    }
}
