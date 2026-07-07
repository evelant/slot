package dev.imagio.slot.inventory.storage;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Live snapshot of a nearby item-display block that acts like first-class
 * display storage for browsing/taking. Tool racks may also accept normal
 * deposits; ground placed items remain take/rollback only.
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
        List<WorldStorageAccess.SlotContent> contents,
        List<AliasedBlock> aliasedBlocks
) {
    private static final String PREFIX = "world-display";
    private static final String SEP = "|";

    public WorldDisplayStorageSource(
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
        this(storageId, kind, label, dimensionId, x, y, z, slotCount, contents, List.of());
    }

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
        aliasedBlocks = copyAliasedBlocks(aliasedBlocks);
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
            copied.add(new WorldStorageAccess.SlotContent(content.slotIndex(), stack, content.count()));
        }
        return List.copyOf(copied);
    }

    private static List<AliasedBlock> copyAliasedBlocks(List<AliasedBlock> input) {
        if (input == null || input.isEmpty()) {
            return List.of();
        }
        ArrayList<AliasedBlock> copied = new ArrayList<>(input.size());
        for (AliasedBlock alias : input) {
            if (alias == null || alias.dimensionId().isBlank()) {
                continue;
            }
            copied.add(alias);
        }
        return copied.isEmpty() ? List.of() : List.copyOf(copied);
    }

    private static String defaultLabel(WorldDisplayStorageKind kind, int x, int y, int z) {
        String base = switch (kind) {
            case TOOL_RACK -> "Tool rack";
            case PLACED_ITEM -> "Placed item";
            case AE2_TERMINAL -> "ME network";
        };
        return base + " @ " + x + "," + y + "," + z;
    }

    public record AliasedBlock(String dimensionId, int x, int y, int z) {
        public AliasedBlock {
            dimensionId = dimensionId == null ? "" : dimensionId;
        }
    }
}
