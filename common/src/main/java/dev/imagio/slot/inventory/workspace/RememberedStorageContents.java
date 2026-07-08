package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.core.SlotResourceCollections;
import dev.imagio.slot.inventory.core.SlotResourceIdentity;
import dev.imagio.slot.inventory.storage.WorldDisplayStorageSource;
import dev.imagio.slot.inventory.storage.WorldStorageAccess;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
        Map<SlotResourceIdentity, Long> fluidCountsByIdentity,
        String providerId,
        List<String> mediaIds,
        List<WorldDisplayStorageSource.AliasedBlock> aliasedBlocks,
        boolean routeReachable,
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
        fluidCountsByIdentity = SlotResourceCollections.normalizeAmounts(fluidCountsByIdentity);
        providerId = providerId == null ? "" : providerId;
        mediaIds = normalizeMediaIds(mediaIds);
        aliasedBlocks = normalizeAliasedBlocks(aliasedBlocks);
        routeReachable = !StorageTargetRef.KIND_AE2_NETWORK.equals(targetKind) || routeReachable;
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
        return fromContents(target, slotCapacity, contents, List.of(), observedTick, source);
    }

    public static RememberedStorageContents fromContents(
            StorageTargetRef target,
            int slotCapacity,
            List<WorldStorageAccess.SlotContent> contents,
            List<WorldStorageAccess.FluidContent> fluidContents,
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
                    counts.merge(identity, content.count(), Integer::sum);
                }
            }
        }
        LinkedHashMap<SlotResourceIdentity, Long> fluidCounts = new LinkedHashMap<>();
        if (fluidContents != null) {
            for (WorldStorageAccess.FluidContent content : fluidContents) {
                if (content != null && content.present()) {
                    SlotResourceCollections.mergeAmount(fluidCounts, content.identity(), content.amount());
                }
            }
        }
        return fromCounts(target, slotCapacity, counts, fluidCounts, observedTick, source);
    }

    public static RememberedStorageContents fromSourceSnapshot(
            StorageTargetRef target,
            SlotWorkspaceViewModel.ChestContentsSnapshot snapshot,
            WorldDisplayStorageSource source,
            long observedTick,
            String eventSource
    ) {
        RememberedStorageContents remembered = fromSnapshot(target, snapshot, observedTick, eventSource);
        if (remembered == null || source == null) {
            return remembered;
        }
        String providerId = source.target() instanceof WorldStorageAccess.Target.Virtual virtual
                ? virtual.providerId()
                : "";
        return remembered.withVirtualMetadata(providerId, source.mediaIds(), source.aliasedBlocks());
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
            if (!snapshot.countsByIdentity().isEmpty()) {
                for (Map.Entry<ItemIdentity, Integer> entry : snapshot.countsByIdentity().entrySet()) {
                    ItemIdentity identity = ItemIdentityMatcher.normalizeMovable(entry.getKey());
                    int count = entry.getValue() == null ? 0 : entry.getValue();
                    if (identity != null && count > 0) {
                        counts.merge(identity, count, Integer::sum);
                    }
                }
            } else {
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
        }
        int slots = snapshot == null ? 0 : snapshot.slotCount();
        return fromCounts(
                target,
                slots,
                counts,
                snapshot == null ? Map.of() : snapshot.fluidCountsByIdentity(),
                observedTick,
                source);
    }

    public static RememberedStorageContents fromCounts(
            StorageTargetRef target,
            int slotCapacity,
            Map<ItemIdentity, Integer> counts,
            long observedTick,
            String source
    ) {
        return fromCounts(target, slotCapacity, counts, Map.of(), observedTick, source);
    }

    public static RememberedStorageContents fromCounts(
            StorageTargetRef target,
            int slotCapacity,
            Map<ItemIdentity, Integer> counts,
            Map<SlotResourceIdentity, Long> fluidCounts,
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
                fluidCounts,
                "",
                List.of(),
                List.of(),
                routeReachableFor(target),
                observedTick,
                source);
    }

    public RememberedStorageContents withVirtualMetadata(
            String providerId,
            List<String> mediaIds,
            List<WorldDisplayStorageSource.AliasedBlock> aliasedBlocks
    ) {
        return new RememberedStorageContents(
                storageId,
                targetKind,
                label,
                dimensionId,
                x,
                y,
                z,
                slotCapacity,
                countsByIdentity,
                fluidCountsByIdentity,
                providerId,
                mediaIds,
                aliasedBlocks,
                routeReachable,
                lastObservedTick,
                source);
    }

    public RememberedStorageContents withRouteReachable(boolean routeReachable) {
        return new RememberedStorageContents(
                storageId,
                targetKind,
                label,
                dimensionId,
                x,
                y,
                z,
                slotCapacity,
                countsByIdentity,
                fluidCountsByIdentity,
                providerId,
                mediaIds,
                aliasedBlocks,
                routeReachable,
                lastObservedTick,
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
                && countsByIdentity.equals(other.countsByIdentity)
                && fluidCountsByIdentity.equals(other.fluidCountsByIdentity)
                && providerId.equals(other.providerId)
                && mediaIds.equals(other.mediaIds)
                && aliasedBlocks.equals(other.aliasedBlocks)
                && routeReachable == other.routeReachable;
    }

    public StorageTargetRef targetRef(boolean liveReadable, boolean proximate) {
        boolean reachable = !StorageTargetRef.KIND_AE2_NETWORK.equals(targetKind) || routeReachable;
        return new StorageTargetRef(
                storageId,
                targetKind,
                label,
                reachable ? dimensionId : "",
                reachable ? x : 0,
                reachable ? y : 0,
                reachable ? z : 0,
                liveReadable,
                reachable && depositCapability(),
                reachable,
                true,
                proximate);
    }

    public SlotWorkspaceViewModel.ChestContentsSnapshot toSnapshot() {
        if (countsByIdentity.isEmpty()) {
            return new SlotWorkspaceViewModel.ChestContentsSnapshot(
                    slotCapacity,
                    List.of(),
                    List.of(),
                    Map.of(),
                    fluidCountsByIdentity);
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
            ItemStack template = SlotWorkspaceViewModel.resolveGhostStack(identity, 1);
            if (template.isEmpty()) {
                continue;
            }
            ItemStack stack = template.copy();
            stack.setCount(Math.min(count, Math.max(1, stack.getMaxStackSize())));
            stacks.add(stack);
            slotIndices.add(index++);
        }
        return new SlotWorkspaceViewModel.ChestContentsSnapshot(
                slotCapacity,
                stacks,
                slotIndices,
                countsByIdentity,
                fluidCountsByIdentity);
    }

    private boolean depositCapability() {
        if (StorageTargetRef.KIND_CLAIMED_CHEST.equals(targetKind)) {
            return true;
        }
        if (StorageTargetRef.KIND_AE2_NETWORK.equals(targetKind)) {
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

    private static List<String> normalizeMediaIds(List<String> source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<String> ids = new LinkedHashSet<>();
        for (String mediaId : source) {
            if (mediaId != null && !mediaId.isBlank()) {
                ids.add(mediaId);
            }
        }
        return ids.isEmpty() ? List.of() : List.copyOf(ids);
    }

    private static List<WorldDisplayStorageSource.AliasedBlock> normalizeAliasedBlocks(
            List<WorldDisplayStorageSource.AliasedBlock> source
    ) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        ArrayList<WorldDisplayStorageSource.AliasedBlock> aliases = new ArrayList<>();
        for (WorldDisplayStorageSource.AliasedBlock alias : source) {
            if (alias != null && !alias.dimensionId().isBlank()) {
                aliases.add(alias);
            }
        }
        return aliases.isEmpty() ? List.of() : List.copyOf(aliases);
    }

    private static boolean routeReachableFor(StorageTargetRef target) {
        if (target == null || !target.ae2Network()) {
            return true;
        }
        return !target.dimensionId().isBlank();
    }
}
