package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.SlotResourceIdentity;
import dev.imagio.slot.inventory.storage.WorldDisplayStorageSource;
import dev.imagio.slot.inventory.storage.WorldStorageAccess;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkspaceStorageMemoryStoreTest {
    private static final UUID CHEST_A = UUID.fromString("00000000-0000-0000-0000-000000000701");

    @Test
    void clearCachedStoresDropsPathScopedInstances(@TempDir Path tempDir) {
        try {
            Path statePath = tempDir.resolve("storage-memory.json");
            WorkspaceStorageMemoryStore first = WorkspaceStorageMemoryStore.forPath(statePath);
            WorkspaceStorageMemoryStore second = WorkspaceStorageMemoryStore.forPath(statePath);

            assertSame(first, second);

            WorkspaceStorageMemoryStore.clearCachedStores();
            WorkspaceStorageMemoryStore third = WorkspaceStorageMemoryStore.forPath(statePath);

            assertNotSame(first, third);
        } finally {
            WorkspaceStorageMemoryStore.clearCachedStores();
        }
    }

    @Test
    void volatileObservationDefersDiskWriteUntilPersistBoundary(@TempDir Path tempDir) {
        Path statePath = tempDir.resolve("storage-memory.json");
        WorkspaceStorageMemoryStore store = new WorkspaceStorageMemoryStore(statePath);
        StorageTargetRef target = StorageTargetRef.claimed(claimed(CHEST_A), true, false, true);
        SlotWorkspaceViewModel.ChestContentsSnapshot snapshot = new SlotWorkspaceViewModel.ChestContentsSnapshot(
                27,
                List.of(new ItemStack("minecraft:redstone", 8, 64)),
                List.of(0),
                Map.of(ItemIdentity.of("minecraft:redstone"), 8));

        assertTrue(store.observeSnapshot(target, snapshot, 10L, "workspace_index_live_read", false));
        assertEquals(1L, store.revision());
        assertEquals(8, store.remembered(CHEST_A.toString())
                .countsByIdentity()
                .get(ItemIdentity.of("minecraft:redstone")));

        WorkspaceStorageMemoryStore beforeFlush = new WorkspaceStorageMemoryStore(statePath);
        assertNull(beforeFlush.remembered(CHEST_A.toString()));

        store.flush();

        WorkspaceStorageMemoryStore afterFlush = new WorkspaceStorageMemoryStore(statePath);
        assertEquals(1L, afterFlush.revision());
        assertEquals(8, afterFlush.remembered(CHEST_A.toString())
                .countsByIdentity()
                .get(ItemIdentity.of("minecraft:redstone")));
    }

    @Test
    void snapshotObservationUsesLogicalCountsWhenPresent(@TempDir Path tempDir) {
        Path statePath = tempDir.resolve("storage-memory.json");
        WorkspaceStorageMemoryStore store = new WorkspaceStorageMemoryStore(statePath);
        StorageTargetRef target = StorageTargetRef.claimed(claimed(CHEST_A), true, false, true);
        SlotWorkspaceViewModel.ChestContentsSnapshot snapshot = new SlotWorkspaceViewModel.ChestContentsSnapshot(
                1,
                List.of(new ItemStack("minecraft:redstone", 64, 64)),
                List.of(0),
                Map.of(ItemIdentity.of("minecraft:redstone"), 10_000));

        assertTrue(store.observeSnapshot(target, snapshot, 10L, "workspace_index_live_read", false));

        assertEquals(10_000, store.remembered(CHEST_A.toString())
                .countsByIdentity()
                .get(ItemIdentity.of("minecraft:redstone")));
    }

    @Test
    void fluidCountsRoundTripAndInvalidateOnAmountChange(@TempDir Path tempDir) {
        Path statePath = tempDir.resolve("storage-memory.json");
        WorkspaceStorageMemoryStore store = new WorkspaceStorageMemoryStore(statePath);
        StorageTargetRef target = StorageTargetRef.claimed(claimed(CHEST_A), true, false, true);
        SlotResourceIdentity water = SlotResourceIdentity.fluid("minecraft:water");

        RememberedStorageContents first = RememberedStorageContents.fromContents(
                target,
                27,
                List.of(),
                List.of(
                        new WorldStorageAccess.FluidContent(
                                0,
                                WorldStorageAccess.FluidContent.DIRECT_TANK_SLOT,
                                water,
                                1000L,
                                "Water"),
                        new WorldStorageAccess.FluidContent(1, 4, water, 250L, "Water")),
                10L,
                "test");

        assertTrue(store.observe(first));
        assertEquals(1L, store.revision());
        assertEquals(1250L, store.remembered(CHEST_A.toString()).fluidCountsByIdentity().get(water));
        assertFalse(store.observe(first));
        assertEquals(1L, store.revision());

        RememberedStorageContents changed = RememberedStorageContents.fromContents(
                target,
                27,
                List.of(),
                List.of(new WorldStorageAccess.FluidContent(
                        0,
                        WorldStorageAccess.FluidContent.DIRECT_TANK_SLOT,
                        water,
                        2000L,
                        "Water")),
                11L,
                "test");

        assertTrue(store.observe(changed));
        assertEquals(2L, store.revision());

        WorkspaceStorageMemoryStore reloaded = new WorkspaceStorageMemoryStore(statePath);
        assertEquals(2000L, reloaded.remembered(CHEST_A.toString()).fluidCountsByIdentity().get(water));
    }

    @Test
    void legacyStorageMemoryWithoutFluidCountsLoadsEmpty(@TempDir Path tempDir) throws Exception {
        Path statePath = tempDir.resolve("storage-memory.json");
        Files.writeString(statePath, """
                {
                  "version": 1,
                  "revision": 7,
                  "contents": [
                    {
                      "storageId": "00000000-0000-0000-0000-000000000701",
                      "targetKind": "claimed_chest",
                      "label": "Old Chest",
                      "dimensionId": "minecraft:overworld",
                      "slotCapacity": 27,
                      "counts": []
                    }
                  ],
                  "ae2Media": []
                }
                """);

        WorkspaceStorageMemoryStore store = new WorkspaceStorageMemoryStore(statePath);

        assertEquals(7L, store.revision());
        assertTrue(store.remembered(CHEST_A.toString()).fluidCountsByIdentity().isEmpty());
    }

    @Test
    void ae2MediaLedgerRoundTripsObservedCellState(@TempDir Path tempDir) {
        Path statePath = tempDir.resolve("storage-memory.json");
        WorkspaceStorageMemoryStore store = new WorkspaceStorageMemoryStore(statePath);

        assertTrue(store.observeMediaObservations(List.of(media(
                "cell-a",
                WorldDisplayStorageSource.MediaObservation.STATUS_ACTIVE,
                Map.of(ItemIdentity.of("minecraft:redstone"), 10_000))), 10L, "test"));

        WorkspaceStorageMemoryStore reloaded = new WorkspaceStorageMemoryStore(statePath);
        WorkspaceStorageMemoryStore.Ae2MediaRecord record = reloaded.ae2MediaLedger().get("cell-a");

        assertEquals(WorldDisplayStorageSource.MediaObservation.STATUS_ACTIVE, record.status());
        assertEquals("drive", record.holderKind());
        assertEquals("minecraft:overworld", record.dimensionId());
        assertEquals(10_000, record.countsByIdentity().get(ItemIdentity.of("minecraft:redstone")));
    }

    @Test
    void emptyAe2MediaObservationRetiresRememberedNetworkCounts(@TempDir Path tempDir) {
        Path statePath = tempDir.resolve("storage-memory.json");
        WorkspaceStorageMemoryStore store = new WorkspaceStorageMemoryStore(statePath);
        RememberedStorageContents remembered = rememberedAe2Network(
                "ae2:network:old",
                32,
                List.of("cell-a"));

        assertTrue(store.observe(remembered));
        assertTrue(store.observeMediaObservations(List.of(media(
                "cell-a",
                WorldDisplayStorageSource.MediaObservation.STATUS_EMPTY,
                Map.of())), 20L, "io_port"));

        assertNull(store.remembered(remembered.storageId()));
        assertEquals(WorldDisplayStorageSource.MediaObservation.STATUS_EMPTY,
                store.ae2MediaLedger().get("cell-a").status());
    }

    @Test
    void unreadableAe2MediaObservationDoesNotRetireRememberedNetworkCounts(@TempDir Path tempDir) {
        Path statePath = tempDir.resolve("storage-memory.json");
        WorkspaceStorageMemoryStore store = new WorkspaceStorageMemoryStore(statePath);
        RememberedStorageContents remembered = rememberedAe2Network(
                "ae2:network:old",
                32,
                List.of("cell-a"));

        assertTrue(store.observe(remembered));
        assertTrue(store.observeMediaObservations(List.of(media(
                "cell-a",
                WorldDisplayStorageSource.MediaObservation.STATUS_UNREADABLE,
                Map.of())), 20L, "drive_scan"));

        assertEquals(32, store.remembered(remembered.storageId())
                .countsByIdentity()
                .get(ItemIdentity.of("minecraft:redstone")));
        assertEquals(WorldDisplayStorageSource.MediaObservation.STATUS_UNREADABLE,
                store.ae2MediaLedger().get("cell-a").status());
    }

    @Test
    void disjointAe2CellTransferRetiresOldMediaAndKeepsNewNetwork(@TempDir Path tempDir) {
        Path statePath = tempDir.resolve("storage-memory.json");
        WorkspaceStorageMemoryStore store = new WorkspaceStorageMemoryStore(statePath);
        RememberedStorageContents oldCell = rememberedAe2Network(
                "ae2:network:old",
                32,
                List.of("cell-a"));
        RememberedStorageContents newCell = rememberedAe2Network(
                "ae2:network:new",
                32,
                List.of("cell-b"));

        assertTrue(store.observe(oldCell));
        assertTrue(store.observeMediaObservations(List.of(media(
                "cell-a",
                WorldDisplayStorageSource.MediaObservation.STATUS_EMPTY,
                Map.of())), 20L, "io_port"));
        assertTrue(store.observe(newCell));

        assertNull(store.remembered(oldCell.storageId()));
        assertEquals(32, store.remembered(newCell.storageId())
                .countsByIdentity()
                .get(ItemIdentity.of("minecraft:redstone")));
    }

    @Test
    void sameRouteDifferentAe2NetworkDemotesOlderRememberedRoute(@TempDir Path tempDir) {
        Path statePath = tempDir.resolve("storage-memory.json");
        WorkspaceStorageMemoryStore store = new WorkspaceStorageMemoryStore(statePath);
        RememberedStorageContents oldRoute = rememberedAe2Network(
                "ae2:network:old",
                32,
                List.of("cell-a"));
        RememberedStorageContents newRoute = rememberedAe2Network(
                "ae2:network:new",
                64,
                List.of("cell-b"));

        assertTrue(store.observe(oldRoute));
        assertTrue(store.observe(newRoute));

        RememberedStorageContents demoted = store.remembered(oldRoute.storageId());
        assertFalse(demoted.routeReachable());
        assertEquals(32, demoted.countsByIdentity().get(ItemIdentity.of("minecraft:redstone")));
        assertFalse(demoted.targetRef(false, false).depositTarget());
        assertFalse(demoted.targetRef(false, false).takeTarget());
        assertEquals("", demoted.targetRef(false, false).dimensionId());
        assertEquals(64, store.remembered(newRoute.storageId())
                .countsByIdentity()
                .get(ItemIdentity.of("minecraft:redstone")));
    }

    private static ClaimedChest claimed(UUID id) {
        return new ClaimedChest(
                id,
                Set.of(new ChestAnchor("minecraft:overworld", 0, 64, 0)),
                0,
                0,
                "");
    }

    private static RememberedStorageContents rememberedAe2Network(
            String storageId,
            int redstoneCount,
            List<String> mediaIds
    ) {
        WorldDisplayStorageSource source = new WorldDisplayStorageSource(
                storageId,
                dev.imagio.slot.inventory.storage.WorldDisplayStorageKind.AE2_NETWORK,
                "ME network @ 1,64,0",
                "minecraft:overworld",
                1,
                64,
                0,
                1,
                List.of(new WorldStorageAccess.SlotContent(
                        0,
                        new ItemStack("minecraft:redstone", Math.min(redstoneCount, 64), 64),
                        redstoneCount)),
                List.of(),
                mediaIds,
                new WorldStorageAccess.Target.Virtual(
                        "ae2",
                        storageId,
                        "terminal",
                        "minecraft:overworld",
                        1,
                        64,
                        0));
        return RememberedStorageContents.fromSourceSnapshot(
                StorageTargetRef.display(source, false, true),
                WorkspaceStorageIndex.snapshotFromDisplay(source),
                source,
                10L,
                "test");
    }

    private static WorldDisplayStorageSource.MediaObservation media(
            String mediaId,
            String status,
            Map<ItemIdentity, Integer> counts
    ) {
        return new WorldDisplayStorageSource.MediaObservation(
                mediaId,
                status,
                "drive",
                "minecraft:overworld",
                1,
                64,
                0,
                counts);
    }
}
