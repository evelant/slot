package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.query.CursorStateSnapshot;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntryKey;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.inventory.query.InventorySourceSnapshot;
import dev.imagio.slot.inventory.storage.WorldDisplayStorageKind;
import dev.imagio.slot.inventory.storage.WorldDisplayStorageSource;
import dev.imagio.slot.inventory.storage.WorldStorageAccess;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.ClaimedChestMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkspaceStorageIndexTest {
    private static final UUID CHEST_A = UUID.fromString("00000000-0000-0000-0000-000000000501");
    private static final UUID CHEST_B = UUID.fromString("00000000-0000-0000-0000-000000000502");

    @TempDir
    Path tempDir;

    @BeforeEach
    void installGhostStackResolver() {
        SlotWorkspaceViewModel.setGhostStackResolver(id -> new ItemStack(id, 1, 64));
    }

    @AfterEach
    void clearGhostStackResolver() {
        SlotWorkspaceViewModel.setGhostStackResolver(null);
    }

    @Test
    void liveStorageIsEnumeratedOncePerIndexBuild() {
        FakeWorldStorage world = new FakeWorldStorage()
                .put(CHEST_A, 27, List.of(content(0, stack("minecraft:redstone", 16))));

        WorkspaceStorageIndex index = WorkspaceStorageIndex.forTesting(
                null,
                null,
                claimedMap(CHEST_A),
                world,
                Set.of(CHEST_A.toString()),
                List.of(),
                Map.of());

        assertEquals(1, world.enumerateCalls(CHEST_A));
        assertEquals(16, index.contents(CHEST_A.toString()).contents().get(0).getCount());
        assertTrue(index.liveChestContentPresence().contains(
                claimed(CHEST_A),
                ItemIdentity.of("minecraft:redstone")));
        assertEquals(1, world.enumerateCalls(CHEST_A));
    }

    @Test
    void smallLiveStationContentsDoNotAuthorizeStorageAffinity() {
        FakeWorldStorage world = new FakeWorldStorage()
                .put(CHEST_A, 1, List.of(content(0, stack("tfc:hot_metal_part", 1))));

        WorkspaceStorageIndex index = WorkspaceStorageIndex.forTesting(
                null,
                null,
                claimedMap(CHEST_A),
                world,
                Set.of(CHEST_A.toString()),
                List.of(),
                Map.of());

        assertFalse(index.liveChestContentPresence().contains(
                claimed(CHEST_A),
                ItemIdentity.of("tfc:hot_metal_part")));
        assertFalse(index.liveStorageAffinityEligibility().isEligible(claimed(CHEST_A)));
    }

    @Test
    void sealedLargeVesselStaysReadableButDoesNotAuthorizeBulkDeposit() {
        FakeWorldStorage world = new FakeWorldStorage()
                .putReadOnly(CHEST_A, 9, List.of(content(0, stack("minecraft:redstone", 16))));

        WorkspaceStorageIndex index = WorkspaceStorageIndex.forTesting(
                null,
                authority("minecraft:redstone", 1),
                claimedMap(CHEST_A),
                world,
                Set.of(CHEST_A.toString()),
                List.of(),
                Map.of());

        assertEquals(16, index.contents(CHEST_A.toString()).contents().get(0).getCount());
        assertFalse(index.target(CHEST_A.toString()).depositTarget());
        assertFalse(index.target(CHEST_A.toString()).takeTarget());
        assertFalse(index.liveDepositStorageIds().contains(CHEST_A.toString()));
        assertFalse(index.liveChestContentPresence().contains(
                claimed(CHEST_A),
                ItemIdentity.of("minecraft:redstone")));
        assertFalse(index.liveStorageAffinityEligibility().isEligible(claimed(CHEST_A)));
        assertEquals(16, index.liveWorldCountsByIdentity().get(ItemIdentity.of("minecraft:redstone")));
    }

    @Test
    void smallMutableStationsStayOutOfBulkDepositEligibility() {
        FakeWorldStorage world = new FakeWorldStorage()
                .put(CHEST_A, 3, List.of(content(2, stack("minecraft:redstone", 4))));

        WorkspaceStorageIndex index = WorkspaceStorageIndex.forTesting(
                null,
                authority("minecraft:redstone", 1),
                claimedMap(CHEST_A),
                world,
                Set.of(CHEST_A.toString()),
                List.of(),
                Map.of());

        assertTrue(index.target(CHEST_A.toString()).depositTarget());
        assertTrue(index.target(CHEST_A.toString()).takeTarget());
        assertFalse(index.liveDepositStorageIds().contains(CHEST_A.toString()));
        assertFalse(index.liveChestContentPresence().contains(
                claimed(CHEST_A),
                ItemIdentity.of("minecraft:redstone")));
        assertFalse(index.liveStorageAffinityEligibility().isEligible(claimed(CHEST_A)));
    }

    @Test
    void liveContentsTakePrecedenceOverRememberedContents() {
        FakeWorldStorage world = new FakeWorldStorage()
                .put(CHEST_A, 27, List.of(content(0, stack("minecraft:stone", 3))));
        RememberedStorageContents remembered = RememberedStorageContents.fromCounts(
                StorageTargetRef.claimed(claimed(CHEST_A), false, true, false),
                27,
                Map.of(ItemIdentity.of("minecraft:dirt"), 99),
                10L,
                "test");

        WorkspaceStorageIndex index = WorkspaceStorageIndex.forTesting(
                null,
                null,
                claimedMap(CHEST_A),
                world,
                Set.of(CHEST_A.toString()),
                List.of(),
                Map.of(CHEST_A.toString(), remembered));

        assertEquals("minecraft:stone", index.contents(CHEST_A.toString()).contents().get(0).itemId());
        assertFalse(index.contents(CHEST_A.toString()).contents().stream()
                .anyMatch(stack -> "minecraft:dirt".equals(stack.itemId())));
    }

    @Test
    void nonProximateTrackedStorageUsesRememberedContentsWithoutLiveEnumeration() {
        FakeWorldStorage world = new FakeWorldStorage()
                .put(CHEST_A, 27, List.of(content(0, stack("minecraft:stone", 3))));
        RememberedStorageContents remembered = RememberedStorageContents.fromCounts(
                StorageTargetRef.claimed(claimed(CHEST_A), false, true, false),
                27,
                Map.of(ItemIdentity.of("minecraft:dirt"), 99),
                10L,
                "test");

        WorkspaceStorageIndex index = WorkspaceStorageIndex.forTesting(
                null,
                null,
                claimedMap(CHEST_A),
                world,
                Set.of(),
                List.of(),
                Map.of(CHEST_A.toString(), remembered));

        assertEquals(0, world.enumerateCalls(CHEST_A));
        assertEquals("minecraft:dirt", index.contents(CHEST_A.toString()).contents().get(0).itemId());
        assertFalse(index.liveChestContentPresence().contains(
                claimed(CHEST_A),
                ItemIdentity.of("minecraft:dirt")));
    }

    @Test
    void liveContentsResolverDoesNotExposeRememberedOnlyStorage() {
        RememberedStorageContents remembered = RememberedStorageContents.fromCounts(
                StorageTargetRef.claimed(claimed(CHEST_A), false, true, false),
                27,
                Map.of(ItemIdentity.of("minecraft:dirt"), 99),
                10L,
                "test");

        WorkspaceStorageIndex index = WorkspaceStorageIndex.forTesting(
                null,
                null,
                claimedMap(CHEST_A),
                new FakeWorldStorage(),
                Set.of(),
                List.of(),
                Map.of(CHEST_A.toString(), remembered));

        assertEquals("minecraft:dirt", index.contents(CHEST_A.toString()).contents().get(0).itemId());
        assertTrue(index.liveContents(CHEST_A.toString()).contents().isEmpty());
        assertTrue(index.liveContentsResolver().apply(CHEST_A.toString()).contents().isEmpty());
    }

    @Test
    void rememberedOnlyContentsDoNotAuthorizeDepositRouting() {
        RememberedStorageContents remembered = RememberedStorageContents.fromCounts(
                StorageTargetRef.claimed(claimed(CHEST_A), false, true, false),
                27,
                Map.of(ItemIdentity.of("minecraft:redstone"), 8),
                10L,
                "test");

        WorkspaceStorageIndex index = WorkspaceStorageIndex.forTesting(
                null,
                null,
                claimedMap(CHEST_A),
                new FakeWorldStorage(),
                Set.of(),
                List.of(),
                Map.of(CHEST_A.toString(), remembered));

        assertEquals(8, index.contents(CHEST_A.toString()).contents().get(0).getCount());
        assertFalse(index.liveChestContentPresence().contains(
                claimed(CHEST_A),
                ItemIdentity.of("minecraft:redstone")));
    }

    @Test
    void proximateInaccessibleClaimedStorageDoesNotUseRememberedContents() {
        RememberedStorageContents remembered = RememberedStorageContents.fromCounts(
                StorageTargetRef.claimed(claimed(CHEST_A), false, true, false),
                27,
                Map.of(ItemIdentity.of("minecraft:redstone"), 8),
                10L,
                "test");

        WorkspaceStorageIndex index = WorkspaceStorageIndex.forTesting(
                null,
                null,
                claimedMap(CHEST_A),
                new FakeWorldStorage(),
                Set.of(CHEST_A.toString()),
                List.of(),
                Map.of(CHEST_A.toString(), remembered));

        assertTrue(index.contents(CHEST_A.toString()).contents().isEmpty());
        assertFalse(index.rememberedWorldCountsByIdentity().containsKey(ItemIdentity.of("minecraft:redstone")));
        assertNull(index.target(CHEST_A.toString()));
    }

    @Test
    void rememberedLargeCountsSplitIntoLegalDisplayStacks() {
        RememberedStorageContents remembered = RememberedStorageContents.fromCounts(
                StorageTargetRef.claimed(claimed(CHEST_A), false, true, false),
                27,
                Map.of(ItemIdentity.of("minecraft:redstone"), 130),
                10L,
                "test");

        SlotWorkspaceViewModel.ChestContentsSnapshot snapshot = remembered.toSnapshot();

        assertEquals(List.of(64, 64, 2), snapshot.contents().stream().map(ItemStack::getCount).toList());
        assertEquals(130, snapshot.contents().stream().mapToInt(ItemStack::getCount).sum());
        assertTrue(snapshot.contents().stream()
                .allMatch(stack -> stack.getCount() <= stack.getMaxStackSize()));
    }

    @Test
    void rememberedCountsNormalizeMovableIdentities() {
        RememberedStorageContents remembered = RememberedStorageContents.fromCounts(
                StorageTargetRef.claimed(claimed(CHEST_A), false, true, false),
                1,
                Map.of(ItemIdentity.exact("minecraft:diamond_sword", "damage=12"), 1),
                10L,
                "test");

        assertTrue(remembered.countsByIdentity().containsKey(ItemIdentity.of("minecraft:diamond_sword")));
        assertFalse(remembered.countsByIdentity().containsKey(
                ItemIdentity.exact("minecraft:diamond_sword", "damage=12")));
    }

    @Test
    void displayTargetsPreserveDepositCapabilityByKind() {
        WorldDisplayStorageSource rack = display(WorldDisplayStorageKind.TOOL_RACK, 1);
        WorldDisplayStorageSource placed = display(WorldDisplayStorageKind.PLACED_ITEM, 2);

        WorkspaceStorageIndex index = WorkspaceStorageIndex.forTesting(
                null,
                null,
                ClaimedChestMap.empty(),
                new FakeWorldStorage(),
                Set.of(),
                List.of(rack, placed),
                Map.of());

        assertTrue(index.target(rack.storageId()).depositTarget());
        assertTrue(index.target(rack.storageId()).takeTarget());
        assertFalse(index.target(placed.storageId()).depositTarget());
        assertTrue(index.target(placed.storageId()).takeTarget());
        assertEquals(2, index.trackedDisplayEntries().size());
    }

    @Test
    void rememberedDisplayStorageIsIndexedWithoutClaimedChest() {
        WorldStorageAccess.Target.Display target = new WorldStorageAccess.Target.Display(
                WorldDisplayStorageKind.PLACED_ITEM,
                "minecraft:overworld",
                3,
                64,
                0);
        RememberedStorageContents remembered = RememberedStorageContents.fromCounts(
                StorageTargetRef.display(target, "Placed item @ 3,64,0", false, true, false),
                1,
                Map.of(ItemIdentity.of("minecraft:redstone"), 1),
                10L,
                "test");

        WorkspaceStorageIndex index = WorkspaceStorageIndex.forTesting(
                null,
                null,
                ClaimedChestMap.empty(),
                new FakeWorldStorage(),
                Set.of(),
                List.of(),
                Map.of(target.storageId(), remembered));

        assertEquals("minecraft:redstone", index.contents(target.storageId()).contents().get(0).itemId());
        assertEquals(1, index.trackedDisplayEntries().size());
        assertEquals(target.storageId(), index.trackedDisplayEntries().get(0).target().storageId());
        assertFalse(index.trackedDisplayEntries().get(0).target().depositTarget());
        assertTrue(index.trackedDisplayEntries().get(0).target().takeTarget());
        assertTrue(index.liveTrackedDisplayEntries().isEmpty());
    }

    @Test
    void rememberedStorageRoundTripsAndOnlyChangedObservationsBumpRevision() {
        Path statePath = tempDir.resolve("storage-memory.json");
        WorkspaceStorageMemoryStore store = new WorkspaceStorageMemoryStore(statePath);
        StorageTargetRef target = StorageTargetRef.claimed(claimed(CHEST_B), true, false, true);

        assertTrue(store.observe(target, 9, List.of(content(0, stack("minecraft:copper_ingot", 4))), 10L, "test"));
        assertEquals(1L, store.revision());
        assertFalse(store.observe(target, 9, List.of(content(0, stack("minecraft:copper_ingot", 4))), 11L, "test-again"));
        assertEquals(1L, store.revision());
        assertTrue(store.observe(target, 9, List.of(content(0, stack("minecraft:copper_ingot", 5))), 12L, "test-change"));
        assertEquals(2L, store.revision());

        WorkspaceStorageMemoryStore reloaded = new WorkspaceStorageMemoryStore(statePath);
        RememberedStorageContents remembered = reloaded.remembered(CHEST_B.toString());

        assertEquals(2L, reloaded.revision());
        assertEquals(5, remembered.countsByIdentity().get(ItemIdentity.of("minecraft:copper_ingot")));
        assertEquals(9, remembered.slotCapacity());
    }

    private static WorldDisplayStorageSource display(WorldDisplayStorageKind kind, int x) {
        return new WorldDisplayStorageSource(
                null,
                kind,
                "",
                "minecraft:overworld",
                x,
                64,
                0,
                4,
                List.of(content(0, stack("minecraft:redstone", 1))));
    }

    private static ClaimedChestMap claimedMap(UUID... ids) {
        ArrayList<ClaimedChest> chests = new ArrayList<>();
        for (UUID id : ids) {
            chests.add(claimed(id));
        }
        return new ClaimedChestMap(chests);
    }

    private static ClaimedChest claimed(UUID id) {
        return new ClaimedChest(
                id,
                Set.of(new ChestAnchor("minecraft:overworld", 0, 64, 0)),
                0,
                0,
                "");
    }

    private static WorldStorageAccess.SlotContent content(int slot, ItemStack stack) {
        return new WorldStorageAccess.SlotContent(slot, stack);
    }

    private static ItemStack stack(String itemId, int count) {
        return new ItemStack(itemId, count, 64);
    }

    private static InventoryAuthoritySnapshot authority(String itemId, int count) {
        InventorySourceSnapshot source = new InventorySourceSnapshot(
                "player.main",
                36,
                List.of(new InventoryEntrySnapshot(
                        InventoryEntryKey.slot("player.main", 0),
                        stack(itemId, count),
                        count,
                        "")),
                "");
        return new InventoryAuthoritySnapshot(null, Map.of("player.main", source), CursorStateSnapshot.empty());
    }

    private static final class FakeWorldStorage implements WorldStorageAccess {
        private final Map<UUID, List<SlotContent>> contents = new LinkedHashMap<>();
        private final Map<UUID, Integer> slots = new LinkedHashMap<>();
        private final Map<UUID, Integer> enumerateCalls = new LinkedHashMap<>();
        private final Set<UUID> readOnly = new LinkedHashSet<>();

        FakeWorldStorage put(UUID storageId, int slotCount, List<SlotContent> slotContents) {
            slots.put(storageId, slotCount);
            contents.put(storageId, slotContents == null ? List.of() : List.copyOf(slotContents));
            readOnly.remove(storageId);
            return this;
        }

        FakeWorldStorage putReadOnly(UUID storageId, int slotCount, List<SlotContent> slotContents) {
            put(storageId, slotCount, slotContents);
            readOnly.add(storageId);
            return this;
        }

        int enumerateCalls(UUID storageId) {
            return enumerateCalls.getOrDefault(storageId, 0);
        }

        @Override
        public ItemStack insert(MinecraftServer server, Target target, ItemStack stack, boolean simulate) {
            UUID storageId = storageId(target);
            if (stack == null || stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            return storageId != null && !readOnly.contains(storageId) ? ItemStack.EMPTY : stack;
        }

        @Override
        public ItemStack extract(MinecraftServer server, Target target, int slotIndex, int amount, boolean simulate) {
            UUID storageId = storageId(target);
            if (storageId == null || readOnly.contains(storageId) || amount <= 0) {
                return ItemStack.EMPTY;
            }
            for (SlotContent content : contents.getOrDefault(storageId, List.of())) {
                if (content == null || content.slotIndex() != slotIndex || content.stack().isEmpty()) {
                    continue;
                }
                ItemStack extracted = content.stack().copy();
                extracted.setCount(Math.min(amount, content.stack().getCount()));
                return extracted;
            }
            return ItemStack.EMPTY;
        }

        @Override
        public List<SlotContent> enumerate(MinecraftServer server, Target target) {
            UUID storageId = storageId(target);
            if (storageId == null || !contents.containsKey(storageId)) {
                return List.of();
            }
            enumerateCalls.merge(storageId, 1, Integer::sum);
            return contents.get(storageId);
        }

        @Override
        public int slotCount(MinecraftServer server, Target target) {
            UUID storageId = storageId(target);
            return storageId == null ? 0 : slots.getOrDefault(storageId, 0);
        }

        @Override
        public boolean isAccessible(MinecraftServer server, Target target) {
            UUID storageId = storageId(target);
            return storageId != null && contents.containsKey(storageId);
        }

        @Override
        public void registerDelegate(Delegate delegate) {
        }

        private static UUID storageId(Target target) {
            return target instanceof Target.Chest chest ? chest.chest().storageId() : null;
        }
    }
}
