package dev.imagio.slot.inventory.workspace;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.inventory.core.ItemComparisonMode;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.storage.WorldDisplayStorageSource;
import dev.imagio.slot.inventory.storage.WorldStorageAccess;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.ClaimedChestMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * World-scoped remembered storage contents. This is intentionally separate
 * from per-player workflow state: the world remembers what was observed, and
 * each player's claimed/tracked storage decides which records are visible.
 */
public final class WorkspaceStorageMemoryStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final int SCHEMA_VERSION = 1;
    private static final ConcurrentHashMap<Path, WorkspaceStorageMemoryStore> STORES = new ConcurrentHashMap<>();

    private final Path statePath;
    private boolean loaded;
    private long revision;
    private LinkedHashMap<String, RememberedStorageContents> contentsByStorageId = new LinkedHashMap<>();

    public WorkspaceStorageMemoryStore(Path statePath) {
        this.statePath = statePath;
    }

    public static WorkspaceStorageMemoryStore forServer(MinecraftServer server) {
        if (server == null) {
            return null;
        }
        Path path = server.getWorldPath(LevelResource.ROOT)
                .resolve("slot")
                .resolve("storage-memory.json");
        return STORES.computeIfAbsent(path.toAbsolutePath().normalize(), WorkspaceStorageMemoryStore::new);
    }

    public static WorkspaceStorageMemoryStore forPath(Path path) {
        if (path == null) {
            return new WorkspaceStorageMemoryStore(null);
        }
        return STORES.computeIfAbsent(path.toAbsolutePath().normalize(), WorkspaceStorageMemoryStore::new);
    }

    public static void clearCachedStoresForTests() {
        STORES.clear();
    }

    public synchronized long revision() {
        ensureLoaded();
        return revision;
    }

    public synchronized RememberedStorageContents remembered(String storageId) {
        ensureLoaded();
        if (storageId == null || storageId.isBlank()) {
            return null;
        }
        return contentsByStorageId.get(storageId);
    }

    public synchronized Map<String, RememberedStorageContents> rememberedContents() {
        ensureLoaded();
        return Map.copyOf(contentsByStorageId);
    }

    public synchronized boolean observe(
            StorageTargetRef target,
            int slotCapacity,
            List<WorldStorageAccess.SlotContent> contents,
            long tick,
            String source
    ) {
        RememberedStorageContents remembered = RememberedStorageContents.fromContents(
                target,
                slotCapacity,
                contents,
                tick,
                source);
        return observe(remembered);
    }

    public synchronized boolean observeSnapshot(
            StorageTargetRef target,
            SlotWorkspaceViewModel.ChestContentsSnapshot snapshot,
            long tick,
            String source
    ) {
        RememberedStorageContents remembered = RememberedStorageContents.fromSnapshot(target, snapshot, tick, source);
        return observe(remembered);
    }

    public synchronized boolean observe(RememberedStorageContents remembered) {
        ensureLoaded();
        if (remembered == null) {
            return false;
        }
        RememberedStorageContents previous = contentsByStorageId.get(remembered.storageId());
        if (remembered.sameObservation(previous)) {
            return false;
        }
        contentsByStorageId.put(remembered.storageId(), remembered);
        revision++;
        save();
        return true;
    }

    public synchronized boolean forget(String storageId) {
        ensureLoaded();
        if (storageId == null || storageId.isBlank()) {
            return false;
        }
        if (contentsByStorageId.remove(storageId) == null) {
            return false;
        }
        revision++;
        save();
        return true;
    }

    public static int observeStorageIds(
            MinecraftServer server,
            WorldStorageAccess worldStorage,
            ClaimedChestMap claimedChestMap,
            Collection<String> storageIds,
            long tick,
            String source
    ) {
        WorkspaceStorageMemoryStore store = forServer(server);
        if (store == null || worldStorage == null || storageIds == null || storageIds.isEmpty()) {
            return 0;
        }
        int changed = 0;
        for (String storageId : storageIds) {
            if (storageId == null || storageId.isBlank()) {
                continue;
            }
            if (observeStorageId(store, server, worldStorage, claimedChestMap, storageId, tick, source)) {
                changed++;
            }
        }
        return changed;
    }

    static boolean observeStorageId(
            WorkspaceStorageMemoryStore store,
            MinecraftServer server,
            WorldStorageAccess worldStorage,
            ClaimedChestMap claimedChestMap,
            String storageId,
            long tick,
            String source
    ) {
        if (store == null || server == null || worldStorage == null || storageId == null || storageId.isBlank()) {
            return false;
        }
        try {
            UUID uuid = UUID.fromString(storageId);
            ClaimedChest chest = claimedChestMap == null ? null : claimedChestMap.chest(uuid);
            if (chest == null) {
                return false;
            }
            WorldStorageAccess.Target target = new WorldStorageAccess.Target.Chest(chest);
            if (!worldStorage.isAccessible(server, target)) {
                return false;
            }
            StorageTargetRef ref = StorageTargetRef.claimed(chest, true, false, true);
            return observeLiveTarget(store, server, worldStorage, ref, target, tick, source);
        } catch (IllegalArgumentException ignored) {
            return WorldDisplayStorageSource.targetFromStorageId(storageId)
                    .map(target -> {
                        if (!worldStorage.isAccessible(server, target)) {
                            return false;
                        }
                        StorageTargetRef ref = StorageTargetRef.display(
                                target,
                                "",
                                true,
                                false,
                                true);
                        return observeLiveTarget(store, server, worldStorage, ref, target, tick, source);
                    })
                    .orElse(false);
        }
    }

    static boolean observeLiveTarget(
            WorkspaceStorageMemoryStore store,
            MinecraftServer server,
            WorldStorageAccess worldStorage,
            StorageTargetRef ref,
            WorldStorageAccess.Target target,
            long tick,
            String source
    ) {
        if (store == null || server == null || worldStorage == null || ref == null || target == null) {
            return false;
        }
        int slots = Math.max(0, worldStorage.slotCount(server, target));
        List<WorldStorageAccess.SlotContent> contents = worldStorage.enumerate(server, target);
        return store.observe(ref, slots, contents, tick, source);
    }

    private void ensureLoaded() {
        if (loaded) {
            return;
        }
        loaded = true;
        if (statePath == null || !Files.exists(statePath)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(statePath)) {
            StateData state = GSON.fromJson(reader, StateData.class);
            if (state == null) {
                return;
            }
            revision = Math.max(0L, state.revision);
            contentsByStorageId = decodeContents(state.contents);
        } catch (IOException | RuntimeException exception) {
            SlotCommon.LOGGER.warn("Failed to load SLOT storage memory from {}", statePath, exception);
            revision = 0L;
            contentsByStorageId = new LinkedHashMap<>();
        }
    }

    private void save() {
        if (statePath == null) {
            return;
        }
        StateData state = new StateData();
        state.version = SCHEMA_VERSION;
        state.revision = revision;
        state.contents = encodeContents(contentsByStorageId.values());
        try {
            if (statePath.getParent() != null) {
                Files.createDirectories(statePath.getParent());
            }
            try (Writer writer = Files.newBufferedWriter(statePath)) {
                GSON.toJson(state, writer);
            }
        } catch (IOException exception) {
            SlotCommon.LOGGER.warn("Failed to save SLOT storage memory to {}", statePath, exception);
        }
    }

    private static List<RememberedStorageData> encodeContents(Collection<RememberedStorageContents> records) {
        if (records == null || records.isEmpty()) {
            return List.of();
        }
        ArrayList<RememberedStorageData> out = new ArrayList<>();
        for (RememberedStorageContents record : records) {
            if (record == null) {
                continue;
            }
            ArrayList<CountData> counts = new ArrayList<>();
            record.countsByIdentity().entrySet().stream()
                    .sorted(Comparator.comparing(entry -> entry.getKey().itemId()))
                    .forEach(entry -> counts.add(new CountData(identity(entry.getKey()), entry.getValue())));
            out.add(new RememberedStorageData(
                    record.storageId(),
                    record.targetKind(),
                    record.label(),
                    record.dimensionId(),
                    record.x(),
                    record.y(),
                    record.z(),
                    record.slotCapacity(),
                    counts,
                    record.lastObservedTick(),
                    record.source()));
        }
        out.sort(Comparator.comparing(RememberedStorageData::storageId));
        return List.copyOf(out);
    }

    private static LinkedHashMap<String, RememberedStorageContents> decodeContents(
            List<RememberedStorageData> data
    ) {
        LinkedHashMap<String, RememberedStorageContents> out = new LinkedHashMap<>();
        if (data == null || data.isEmpty()) {
            return out;
        }
        for (RememberedStorageData raw : data) {
            if (raw == null || raw.storageId == null || raw.storageId.isBlank()) {
                continue;
            }
            LinkedHashMap<ItemIdentity, Integer> counts = new LinkedHashMap<>();
            if (raw.counts != null) {
                for (CountData count : raw.counts) {
                    ItemIdentity identity = decodeIdentity(count == null ? null : count.identity);
                    if (identity != null && count.count > 0) {
                        counts.merge(identity, count.count, Integer::sum);
                    }
                }
            }
            RememberedStorageContents remembered = new RememberedStorageContents(
                    raw.storageId,
                    raw.targetKind,
                    raw.label,
                    raw.dimensionId,
                    raw.x,
                    raw.y,
                    raw.z,
                    raw.slotCapacity,
                    counts,
                    raw.lastObservedTick,
                    raw.source);
            out.put(remembered.storageId(), remembered);
        }
        return out;
    }

    private static IdentityData identity(ItemIdentity identity) {
        return identity == null
                ? null
                : new IdentityData(identity.itemId(), identity.comparisonMode().name(), identity.componentFingerprint());
    }

    private static ItemIdentity decodeIdentity(IdentityData data) {
        if (data == null || data.itemId == null || data.itemId.isBlank()) {
            return null;
        }
        ItemComparisonMode mode = ItemComparisonMode.ITEM_ID;
        if (data.comparisonMode != null && !data.comparisonMode.isBlank()) {
            try {
                mode = ItemComparisonMode.valueOf(data.comparisonMode);
            } catch (IllegalArgumentException ignored) {
                mode = ItemComparisonMode.ITEM_ID;
            }
        }
        return new ItemIdentity(data.itemId, mode, data.componentFingerprint == null ? "" : data.componentFingerprint);
    }

    private static final class StateData {
        int version;
        long revision;
        List<RememberedStorageData> contents = List.of();
    }

    private record RememberedStorageData(
            String storageId,
            String targetKind,
            String label,
            String dimensionId,
            int x,
            int y,
            int z,
            int slotCapacity,
            List<CountData> counts,
            long lastObservedTick,
            String source
    ) {
    }

    private record CountData(IdentityData identity, int count) {
    }

    private record IdentityData(String itemId, String comparisonMode, String componentFingerprint) {
    }
}
