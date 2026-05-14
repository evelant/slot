package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.storage.WorldStorageAccess;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * World-scoped remembered read model for one storage target.
 *
 * <p>v1 persists only movable identity counts, not full item stacks. That is
 * enough for search, goal authority, pips, and chest guidance while keeping
 * mutation authority strictly live.
 */
public record RememberedStorageContents(
        String storageId,
        String targetKind,
        String label,
        String dimensionId,
        int x,
        int y,
        int z,
        int slotCapacity,
        Map<ItemIdentity, Integer> countsByIdentity,
        long lastObservedTick,
        String source
) {
    public RememberedStorageContents {
        if (storageId == null || storageId.isBlank()) {
            throw new IllegalArgumentException("storageId must not be blank");
        }
        targetKind = targetKind == null || targetKind.isBlank()
                ? StorageTargetRef.KIND_CLAIMED_CHEST
                : targetKind;
        label = label == null ? "" : label;
        dimensionId = dimensionId == null ? "" : dimensionId;
        slotCapacity = Math.max(0, slotCapacity);
        countsByIdentity = normalizeCounts(countsByIdentity);
        lastObservedTick = Math.max(0L, lastObservedTick);
        source = source == null ? "" : source;
    }

    public static RememberedStorageContents fromContents(
            StorageTargetRef target,
            int slotCapacity,
            List<WorldStorageAccess.SlotContent> contents,
            long observedTick,
            String source
    ) {
        if (target == null) {
            return null;
        }
        LinkedHashMap<ItemIdentity, Integer> counts = new LinkedHashMap<>();
        if (contents != null) {
            for (WorldStorageAccess.SlotContent content : contents) {
                if (content == null || content.stack() == null || content.stack().isEmpty()) {
                    continue;
                }
                ItemIdentity identity = ItemIdentityMatcher.normalizeMovable(
                        ItemIdentityMatcher.create(content.stack()));
                if (identity != null) {
                    counts.merge(identity, content.stack().getCount(), Integer::sum);
                }
            }
        }
        return fromCounts(target, slotCapacity, counts, observedTick, source);
    }

    public static RememberedStorageContents fromSnapshot(
            StorageTargetRef target,
            SlotWorkspaceViewModel.ChestContentsSnapshot snapshot,
            long observedTick,
            String source
    ) {
        if (target == null) {
            return null;
        }
        LinkedHashMap<ItemIdentity, Integer> counts = new LinkedHashMap<>();
        if (snapshot != null) {
            for (ItemStack stack : snapshot.contents()) {
                if (stack == null || stack.isEmpty()) {
                    continue;
                }
                ItemIdentity identity = ItemIdentityMatcher.normalizeMovable(ItemIdentityMatcher.create(stack));
                if (identity != null) {
                    counts.merge(identity, stack.getCount(), Integer::sum);
                }
            }
        }
        int slots = snapshot == null ? 0 : snapshot.slotCount();
        return fromCounts(target, slots, counts, observedTick, source);
    }

    public static RememberedStorageContents fromCounts(
            StorageTargetRef target,
            int slotCapacity,
            Map<ItemIdentity, Integer> counts,
            long observedTick,
            String source
    ) {
        if (target == null) {
            return null;
        }
        return new RememberedStorageContents(
                target.storageId(),
                target.targetKind(),
                target.label(),
                target.dimensionId(),
                target.x(),
                target.y(),
                target.z(),
                slotCapacity,
                counts,
                observedTick,
                source);
    }

    public boolean sameObservation(RememberedStorageContents other) {
        return other != null
                && storageId.equals(other.storageId)
                && targetKind.equals(other.targetKind)
                && label.equals(other.label)
                && dimensionId.equals(other.dimensionId)
                && x == other.x
                && y == other.y
                && z == other.z
                && slotCapacity == other.slotCapacity
                && countsByIdentity.equals(other.countsByIdentity);
    }

    public StorageTargetRef targetRef(boolean liveReadable, boolean proximate) {
        return new StorageTargetRef(
                storageId,
                targetKind,
                label,
                dimensionId,
                x,
                y,
                z,
                liveReadable,
                depositCapability(),
                true,
                true,
                proximate);
    }

    public SlotWorkspaceViewModel.ChestContentsSnapshot toSnapshot() {
        if (countsByIdentity.isEmpty()) {
            return new SlotWorkspaceViewModel.ChestContentsSnapshot(slotCapacity, List.of(), List.of());
        }
        ArrayList<ItemStack> stacks = new ArrayList<>(countsByIdentity.size());
        ArrayList<Integer> slotIndices = new ArrayList<>(countsByIdentity.size());
        int index = 0;
        for (Map.Entry<ItemIdentity, Integer> entry : countsByIdentity.entrySet()) {
            ItemIdentity identity = entry.getKey();
            int count = entry.getValue() == null ? 0 : entry.getValue();
            if (identity == null || count <= 0) {
                continue;
            }
            ItemStack stack = SlotWorkspaceViewModel.resolveGhostStack(identity, count);
            if (stack.isEmpty()) {
                continue;
            }
            stacks.add(stack);
            slotIndices.add(index++);
        }
        return new SlotWorkspaceViewModel.ChestContentsSnapshot(slotCapacity, stacks, slotIndices);
    }

    private boolean depositCapability() {
        if (StorageTargetRef.KIND_CLAIMED_CHEST.equals(targetKind)) {
            return true;
        }
        if (!targetKind.startsWith(StorageTargetRef.KIND_DISPLAY_PREFIX)) {
            return false;
        }
        dev.imagio.slot.inventory.storage.WorldDisplayStorageKind kind =
                dev.imagio.slot.inventory.storage.WorldDisplayStorageKind.fromKey(
                        targetKind.substring(StorageTargetRef.KIND_DISPLAY_PREFIX.length()));
        return kind != null && kind.depositTarget();
    }

    private static Map<ItemIdentity, Integer> normalizeCounts(Map<ItemIdentity, Integer> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<ItemIdentity, Integer> normalized = new LinkedHashMap<>();
        for (Map.Entry<ItemIdentity, Integer> entry : source.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null || entry.getValue() <= 0) {
                continue;
            }
            ItemIdentity identity = ItemIdentityMatcher.normalizeMovable(entry.getKey());
            if (identity != null) {
                normalized.merge(identity, entry.getValue(), Integer::sum);
            }
        }
        return Map.copyOf(normalized);
    }
}
