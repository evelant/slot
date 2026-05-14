package dev.imagio.slot.inventory.storage;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Live snapshot of a nearby item-display block that acts like storage for
 * browsing/taking, but is not part of the claimed-chest model.
 */
public record WorldDisplayStorageSource(
        String storageId,
        WorldDisplayStorageKind kind,
        String label,
        String dimensionId,
        int x,
        int y,
        int z,
        int slotCount,
        List<WorldStorageAccess.SlotContent> contents
) {
    private static final String PREFIX = "world-display";
    private static final String SEP = "|";

    public WorldDisplayStorageSource {
        if (kind == null) {
            throw new IllegalArgumentException("kind must not be null");
        }
        dimensionId = dimensionId == null ? "" : dimensionId;
        storageId = storageId == null || storageId.isBlank()
                ? storageId(kind, dimensionId, x, y, z)
                : storageId;
        label = label == null || label.isBlank() ? defaultLabel(kind, x, y, z) : label;
        slotCount = Math.max(0, slotCount);
        contents = copyContents(contents);
    }

    public WorldStorageAccess.Target.Display target() {
        return new WorldStorageAccess.Target.Display(kind, dimensionId, x, y, z);
    }

    public boolean depositTarget() {
        return kind.depositTarget();
    }

    public static String storageId(
            WorldDisplayStorageKind kind,
            String dimensionId,
            int x,
            int y,
            int z
    ) {
        WorldDisplayStorageKind resolvedKind = kind == null ? WorldDisplayStorageKind.PLACED_ITEM : kind;
        String dimension = dimensionId == null ? "" : dimensionId;
        return PREFIX + SEP + resolvedKind.key() + SEP + dimension + SEP + x + SEP + y + SEP + z;
    }

    public static Optional<WorldStorageAccess.Target.Display> targetFromStorageId(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String[] parts = raw.split("\\|", -1);
        if (parts.length != 6 || !PREFIX.equals(parts[0])) {
            return Optional.empty();
        }
        WorldDisplayStorageKind kind = WorldDisplayStorageKind.fromKey(parts[1]);
        if (kind == null || parts[2].isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(new WorldStorageAccess.Target.Display(
                    kind,
                    parts[2],
                    Integer.parseInt(parts[3]),
                    Integer.parseInt(parts[4]),
                    Integer.parseInt(parts[5])));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    private static List<WorldStorageAccess.SlotContent> copyContents(
            List<WorldStorageAccess.SlotContent> input
    ) {
        if (input == null || input.isEmpty()) {
            return List.of();
        }
        ArrayList<WorldStorageAccess.SlotContent> copied = new ArrayList<>(input.size());
        for (WorldStorageAccess.SlotContent content : input) {
            if (content == null || content.stack() == null || content.stack().isEmpty()) {
                continue;
            }
            ItemStack stack = content.stack().copy();
            copied.add(new WorldStorageAccess.SlotContent(content.slotIndex(), stack));
        }
        return List.copyOf(copied);
    }

    private static String defaultLabel(WorldDisplayStorageKind kind, int x, int y, int z) {
        String base = switch (kind) {
            case TOOL_RACK -> "Tool rack";
            case PLACED_ITEM -> "Placed item";
        };
        return base + " @ " + x + "," + y + "," + z;
    }
}
