package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    private static ClaimedChest claimed(UUID id) {
        return new ClaimedChest(
                id,
                Set.of(new ChestAnchor("minecraft:overworld", 0, 64, 0)),
                0,
                0,
                "");
    }
}
