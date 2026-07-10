package dev.imagio.slot.inventory.storage;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.SlotResourceCollections;
import dev.imagio.slot.inventory.core.SlotResourceIdentity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
        List<WorldStorageAccess.FluidContent> fluidContents,
        List<AliasedBlock> aliasedBlocks,
        List<String> mediaIds,
        List<MediaObservation> mediaObservations,
        WorldStorageAccess.Target target
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
        this(storageId, kind, label, dimensionId, x, y, z, slotCount, contents, List.of(), List.of());
    }

    public WorldDisplayStorageSource(
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
        this(storageId, kind, label, dimensionId, x, y, z, slotCount, contents, List.of(), aliasedBlocks, List.of(), null);
    }

    public WorldDisplayStorageSource(
            String storageId,
            WorldDisplayStorageKind kind,
            String label,
            String dimensionId,
            int x,
            int y,
            int z,
            int slotCount,
            List<WorldStorageAccess.SlotContent> contents,
            List<AliasedBlock> aliasedBlocks,
            List<String> mediaIds,
            WorldStorageAccess.Target target
    ) {
        this(storageId, kind, label, dimensionId, x, y, z, slotCount, contents, List.of(), aliasedBlocks, mediaIds, target);
    }

    public WorldDisplayStorageSource(
            String storageId,
            WorldDisplayStorageKind kind,
            String label,
            String dimensionId,
            int x,
            int y,
            int z,
            int slotCount,
            List<WorldStorageAccess.SlotContent> contents,
            List<WorldStorageAccess.FluidContent> fluidContents,
            List<AliasedBlock> aliasedBlocks
    ) {
        this(storageId, kind, label, dimensionId, x, y, z, slotCount, contents, fluidContents, aliasedBlocks, List.of(), null);
    }

    public WorldDisplayStorageSource(
            String storageId,
            WorldDisplayStorageKind kind,
            String label,
            String dimensionId,
            int x,
            int y,
            int z,
            int slotCount,
            List<WorldStorageAccess.SlotContent> contents,
            List<WorldStorageAccess.FluidContent> fluidContents,
            List<AliasedBlock> aliasedBlocks,
            List<String> mediaIds,
            WorldStorageAccess.Target target
    ) {
        this(
                storageId,
                kind,
                label,
                dimensionId,
                x,
                y,
                z,
                slotCount,
                contents,
                fluidContents,
                aliasedBlocks,
                mediaIds,
                List.of(),
                target);
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
        fluidContents = copyFluidContents(fluidContents);
        aliasedBlocks = copyAliasedBlocks(aliasedBlocks);
        mediaIds = copyMediaIds(mediaIds);
        mediaObservations = copyMediaObservations(mediaObservations);
        target = target == null ? new WorldStorageAccess.Target.Display(kind, dimensionId, x, y, z) : target;
    }

    public boolean trackedStorage() {
        return kind.trackedStorage();
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

    private static List<WorldStorageAccess.FluidContent> copyFluidContents(
            List<WorldStorageAccess.FluidContent> input
    ) {
        if (input == null || input.isEmpty()) {
            return List.of();
        }
        ArrayList<WorldStorageAccess.FluidContent> copied = new ArrayList<>(input.size());
        for (WorldStorageAccess.FluidContent content : input) {
            if (content == null || !content.present()) {
                continue;
            }
            copied.add(new WorldStorageAccess.FluidContent(
                    content.tankIndex(),
                    content.containingSlotIndex(),
                    content.identity(),
                    content.amount(),
                    content.label()));
        }
        return copied.isEmpty() ? List.of() : List.copyOf(copied);
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
            case FLUID_TANK -> "Fluid tank";
            case AE2_TERMINAL, AE2_NETWORK -> "ME network";
        };
        return base + " @ " + x + "," + y + "," + z;
    }

    private static List<String> copyMediaIds(List<String> input) {
        if (input == null || input.isEmpty()) {
            return List.of();
        }
        ArrayList<String> copied = new ArrayList<>(input.size());
        for (String mediaId : input) {
            if (mediaId == null || mediaId.isBlank()) {
                continue;
            }
            copied.add(mediaId);
        }
        return copied.isEmpty() ? List.of() : List.copyOf(copied);
    }

    private static List<MediaObservation> copyMediaObservations(List<MediaObservation> input) {
        if (input == null || input.isEmpty()) {
            return List.of();
        }
        ArrayList<MediaObservation> copied = new ArrayList<>(input.size());
        for (MediaObservation observation : input) {
            if (observation != null && !observation.mediaId().isBlank()) {
                copied.add(observation);
            }
        }
        return copied.isEmpty() ? List.of() : List.copyOf(copied);
    }

    public record AliasedBlock(String dimensionId, int x, int y, int z) {
        public AliasedBlock {
            dimensionId = dimensionId == null ? "" : dimensionId;
        }
    }

    public record MediaObservation(
            String mediaId,
            String status,
            String holderKind,
            String dimensionId,
            int x,
            int y,
            int z,
            Map<ItemIdentity, Integer> countsByIdentity,
            Map<SlotResourceIdentity, Long> fluidCountsByIdentity
    ) {
        public static final String STATUS_ACTIVE = "active";
        public static final String STATUS_EMPTY = "empty";
        public static final String STATUS_NON_ITEM = "non_item";
        public static final String STATUS_UNREADABLE = "unreadable";

        public MediaObservation {
            mediaId = mediaId == null ? "" : mediaId;
            status = status == null || status.isBlank() ? STATUS_UNREADABLE : status;
            holderKind = holderKind == null ? "" : holderKind;
            dimensionId = dimensionId == null ? "" : dimensionId;
            countsByIdentity = normalizeCounts(countsByIdentity);
            fluidCountsByIdentity = SlotResourceCollections.normalizeAmounts(fluidCountsByIdentity);
        }

        public MediaObservation(
                String mediaId,
                String status,
                String holderKind,
                String dimensionId,
                int x,
                int y,
                int z,
                Map<ItemIdentity, Integer> countsByIdentity
        ) {
            this(mediaId, status, holderKind, dimensionId, x, y, z, countsByIdentity, Map.of());
        }

        public boolean removesItemCounts() {
            return STATUS_EMPTY.equals(status) || STATUS_NON_ITEM.equals(status);
        }

        private static Map<ItemIdentity, Integer> normalizeCounts(Map<ItemIdentity, Integer> source) {
            if (source == null || source.isEmpty()) {
                return Map.of();
            }
            LinkedHashMap<ItemIdentity, Integer> normalized = new LinkedHashMap<>();
            for (Map.Entry<ItemIdentity, Integer> entry : source.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null && entry.getValue() > 0) {
                    normalized.merge(entry.getKey(), entry.getValue(), Integer::sum);
                }
            }
            return normalized.isEmpty() ? Map.of() : Map.copyOf(normalized);
        }
    }
}
