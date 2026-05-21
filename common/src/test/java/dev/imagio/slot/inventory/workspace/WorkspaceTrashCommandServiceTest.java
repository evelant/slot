package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.storage.CarriedSourceAccess;
import dev.imagio.slot.inventory.storage.StorageAccessRegistry;
import dev.imagio.slot.inventory.storage.WorldStorageAccess;
import dev.imagio.slot.workflow.domain.InMemoryWorkflowDomainStateRepository;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorkspaceTrashCommandServiceTest {
    @BeforeEach
    void resetStorageAccess() {
        StorageAccessRegistry.resetForTests();
    }

    @AfterEach
    void cleanupStorageAccess() {
        StorageAccessRegistry.resetForTests();
    }

    @Test
    void overflowPickupContinuesSweepingJunkStacksUntilHalfFullPressureTarget() {
        ServerPlayer player = new ServerPlayer();
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(
                new InMemoryWorkflowDomainStateRepository(),
                null);
        ItemIdentity logs = ItemIdentity.of("minecraft:oak_log");
        runtime.collectionWorkflow().setJunk(logs, true);

        FakeCarriedSourceAccess carried = new FakeCarriedSourceAccess();
        carried.put(BuiltinInventoryIds.PLAYER_MAIN, 0, stack("minecraft:oak_log", 1));
        carried.put("test:provider/main", 0, stack("minecraft:oak_log", 64));
        carried.pressure = new CarriedSourceAccess.CarriedStoragePressure(100, 76);
        StorageAccessRegistry.installCarriedSourceAccess(carried);
        StorageAccessRegistry.installWorldStorageAccess(new NoOpWorldStorageAccess());

        WorkspaceTrashCommandService.PostPickupOverflowTrashResult result =
                WorkspaceTrashCommandService.trashOverflowPickup(
                        player,
                        runtime,
                        stack("minecraft:oak_log", 1),
                        1);

        assertEquals(65, result.carriedTrashed());
        assertEquals(1, result.pickedTrashed());
        assertEquals(0, carried.peek(BuiltinInventoryIds.PLAYER_MAIN, 0).getCount());
        assertEquals(0, carried.peek("test:provider/main", 0).getCount());
        assertEquals(1, carried.providerExtractCalls);
        assertEquals(0, carried.currentAuthorityCalls);
        assertEquals(1, carried.pressureCalls);
    }

    @Test
    void overflowPickupDeletesSmallestPickupLaneStacksFirstBeforeBroaderSweep() {
        ServerPlayer player = new ServerPlayer();
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(
                new InMemoryWorkflowDomainStateRepository(),
                null);
        ItemIdentity logs = ItemIdentity.of("minecraft:oak_log");
        runtime.collectionWorkflow().setJunk(logs, true);

        FakeCarriedSourceAccess carried = new FakeCarriedSourceAccess();
        carried.put(BuiltinInventoryIds.PLAYER_MAIN, 0, stack("minecraft:oak_log", 64));
        carried.put(BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0, 0, stack("minecraft:oak_log", 1));
        carried.pressure = new CarriedSourceAccess.CarriedStoragePressure(100, 76);
        StorageAccessRegistry.installCarriedSourceAccess(carried);
        StorageAccessRegistry.installWorldStorageAccess(new NoOpWorldStorageAccess());

        WorkspaceTrashCommandService.PostPickupOverflowTrashResult result =
                WorkspaceTrashCommandService.trashOverflowPickup(
                        player,
                        runtime,
                        stack("minecraft:oak_log", 1),
                        1);

        assertEquals(65, result.carriedTrashed());
        assertEquals(1, result.pickedTrashed());
        assertEquals(0, carried.peek(BuiltinInventoryIds.PLAYER_MAIN, 0).getCount());
        assertEquals(0, carried.peek(BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0, 0).getCount());
    }

    @Test
    void overflowPickupSweepsExistingJunkWhenPickedItemIsNotJunk() {
        ServerPlayer player = new ServerPlayer();
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(
                new InMemoryWorkflowDomainStateRepository(),
                null);
        ItemIdentity dirt = ItemIdentity.of("minecraft:dirt");
        runtime.collectionWorkflow().setJunk(dirt, true);

        FakeCarriedSourceAccess carried = new FakeCarriedSourceAccess();
        carried.put(BuiltinInventoryIds.PLAYER_MAIN, 0, stack("minecraft:stone", 1));
        carried.put("test:provider/main", 0, stack("minecraft:dirt", 3));
        carried.pressure = new CarriedSourceAccess.CarriedStoragePressure(100, 76);
        StorageAccessRegistry.installCarriedSourceAccess(carried);
        StorageAccessRegistry.installWorldStorageAccess(new NoOpWorldStorageAccess());

        WorkspaceTrashCommandService.PostPickupOverflowTrashResult result =
                WorkspaceTrashCommandService.trashOverflowPickup(
                        player,
                        runtime,
                        stack("minecraft:stone", 1),
                        1);

        assertEquals(3, result.carriedTrashed());
        assertEquals(0, result.pickedTrashed());
        assertEquals(1, carried.peek(BuiltinInventoryIds.PLAYER_MAIN, 0).getCount());
        assertEquals(0, carried.peek("test:provider/main", 0).getCount());
        assertEquals(1, carried.providerExtractCalls);
        assertEquals(0, carried.currentAuthorityCalls);
        assertEquals(1, carried.pressureCalls);
    }

    @Test
    void overflowPickupSweepsEnoughJunkStacksToRestoreSlotPressure() {
        ServerPlayer player = new ServerPlayer();
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(
                new InMemoryWorkflowDomainStateRepository(),
                null);
        ItemIdentity dirt = ItemIdentity.of("minecraft:dirt");
        runtime.collectionWorkflow().setJunk(dirt, true);

        FakeCarriedSourceAccess carried = new FakeCarriedSourceAccess();
        carried.put(BuiltinInventoryIds.PLAYER_MAIN, 0, stack("minecraft:stone", 1));
        carried.put("test:provider/main", 0, stack("minecraft:dirt", 64));
        carried.put("test:provider/main", 1, stack("minecraft:dirt", 2));
        carried.pressure = new CarriedSourceAccess.CarriedStoragePressure(100, 77);
        StorageAccessRegistry.installCarriedSourceAccess(carried);
        StorageAccessRegistry.installWorldStorageAccess(new NoOpWorldStorageAccess());

        WorkspaceTrashCommandService.PostPickupOverflowTrashResult result =
                WorkspaceTrashCommandService.trashOverflowPickup(
                        player,
                        runtime,
                        stack("minecraft:stone", 1),
                        1);

        assertEquals(66, result.carriedTrashed());
        assertEquals(0, result.pickedTrashed());
        assertEquals(0, carried.peek("test:provider/main", 0).getCount());
        assertEquals(0, carried.peek("test:provider/main", 1).getCount());
    }

    @Test
    void overflowPickupUsesUnifiedCarriedPressureInsteadOfBuiltinPickupLanePressure() {
        ServerPlayer player = new ServerPlayer();
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(
                new InMemoryWorkflowDomainStateRepository(),
                null);
        ItemIdentity logs = ItemIdentity.of("minecraft:oak_log");
        runtime.collectionWorkflow().setJunk(logs, true);

        FakeCarriedSourceAccess carried = new FakeCarriedSourceAccess();
        carried.put(BuiltinInventoryIds.PLAYER_MAIN, 0, stack("minecraft:oak_log", 1));
        fillBuiltinPickupPressure(carried);
        carried.put("test:provider/main", 0, stack("minecraft:oak_log", 64));
        carried.pressure = new CarriedSourceAccess.CarriedStoragePressure(101, 28);
        StorageAccessRegistry.installCarriedSourceAccess(carried);
        StorageAccessRegistry.installWorldStorageAccess(new NoOpWorldStorageAccess());

        WorkspaceTrashCommandService.PostPickupOverflowTrashResult result =
                WorkspaceTrashCommandService.trashOverflowPickup(
                        player,
                        runtime,
                        stack("minecraft:oak_log", 1),
                        1);

        assertEquals(0, result.carriedTrashed());
        assertEquals(0, result.pickedTrashed());
        assertEquals(1, carried.peek(BuiltinInventoryIds.PLAYER_MAIN, 0).getCount());
        assertEquals(64, carried.peek("test:provider/main", 0).getCount());
        assertEquals(0, carried.providerExtractCalls);
        assertEquals(0, carried.currentAuthorityCalls);
        assertEquals(1, carried.pressureCalls);
    }

    @Test
    void prePickupOverflowSweepsExistingJunkWhenAlreadyOverThreshold() {
        ServerPlayer player = new ServerPlayer();
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(
                new InMemoryWorkflowDomainStateRepository(),
                null);
        ItemIdentity dirt = ItemIdentity.of("minecraft:dirt");
        runtime.collectionWorkflow().setJunk(dirt, true);

        FakeCarriedSourceAccess carried = new FakeCarriedSourceAccess();
        carried.put("test:provider/main", 0, stack("minecraft:dirt", 3));
        carried.pressure = new CarriedSourceAccess.CarriedStoragePressure(100, 76);
        StorageAccessRegistry.installCarriedSourceAccess(carried);
        StorageAccessRegistry.installWorldStorageAccess(new NoOpWorldStorageAccess());

        WorkspaceTrashCommandService.PickupOverflowTrashResult result =
                WorkspaceTrashCommandService.trashOverflowBeforePickup(
                        player,
                        runtime,
                        stack("minecraft:stone", 1),
                        1);

        assertEquals(3, result.carriedTrashed());
        assertEquals(0, result.incomingTrashed());
        assertEquals(0, carried.peek("test:provider/main", 0).getCount());
        assertEquals(1, carried.providerExtractCalls);
        assertEquals(0, carried.currentAuthorityCalls);
        assertEquals(1, carried.pressureCalls);
    }

    @Test
    void prePickupOverflowVoidsIncomingJunkWhenAlreadyOverThreshold() {
        ServerPlayer player = new ServerPlayer();
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(
                new InMemoryWorkflowDomainStateRepository(),
                null);
        ItemIdentity logs = ItemIdentity.of("minecraft:oak_log");
        runtime.collectionWorkflow().setJunk(logs, true);

        FakeCarriedSourceAccess carried = new FakeCarriedSourceAccess();
        carried.pressure = new CarriedSourceAccess.CarriedStoragePressure(100, 76);
        StorageAccessRegistry.installCarriedSourceAccess(carried);
        StorageAccessRegistry.installWorldStorageAccess(new NoOpWorldStorageAccess());

        WorkspaceTrashCommandService.PickupOverflowTrashResult result =
                WorkspaceTrashCommandService.trashOverflowBeforePickup(
                        player,
                        runtime,
                        stack("minecraft:oak_log", 64),
                        64);

        assertEquals(0, result.carriedTrashed());
        assertEquals(64, result.incomingTrashed());
        assertEquals(0, carried.providerExtractCalls);
        assertEquals(0, carried.currentAuthorityCalls);
        assertEquals(1, carried.pressureCalls);
    }

    @Test
    void prePickupOverflowTreatsHalfFullAsTheEffectivePressureLimit() {
        ServerPlayer player = new ServerPlayer();
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(
                new InMemoryWorkflowDomainStateRepository(),
                null);
        ItemIdentity looseRock = ItemIdentity.of("tfc:rock/loose/dolomite");
        runtime.collectionWorkflow().setJunk(looseRock, true);

        FakeCarriedSourceAccess carried = new FakeCarriedSourceAccess();
        carried.pressure = new CarriedSourceAccess.CarriedStoragePressure(138, 103);
        StorageAccessRegistry.installCarriedSourceAccess(carried);
        StorageAccessRegistry.installWorldStorageAccess(new NoOpWorldStorageAccess());

        WorkspaceTrashCommandService.PickupOverflowTrashResult result =
                WorkspaceTrashCommandService.trashOverflowBeforePickup(
                        player,
                        runtime,
                        stack("tfc:rock/loose/dolomite", 32),
                        32);

        assertEquals(0, result.carriedTrashed());
        assertEquals(32, result.incomingTrashed());
        assertEquals(1, carried.pressureCalls);
    }

    private static ItemStack stack(String itemId, int count) {
        return new ItemStack(itemId, count, 64);
    }

    private static void fillBuiltinPickupPressure(FakeCarriedSourceAccess carried) {
        for (int slot = 1; slot < 27; slot++) {
            carried.put(BuiltinInventoryIds.PLAYER_MAIN, slot, stack("minecraft:stone", 1));
        }
        carried.put(BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0, 0, stack("minecraft:stone", 1));
    }

    private static final class FakeCarriedSourceAccess implements CarriedSourceAccess {
        private final Map<String, Map<Integer, ItemStack>> contents = new LinkedHashMap<>();
        CarriedSourceAccess.CarriedStoragePressure pressure = CarriedSourceAccess.CarriedStoragePressure.empty();
        int providerExtractCalls;
        int currentAuthorityCalls;
        int pressureCalls;

        void put(String sourceId, int slotIndex, ItemStack stack) {
            contents.computeIfAbsent(sourceId, key -> new LinkedHashMap<>()).put(slotIndex, stack);
        }

        ItemStack peek(String sourceId, int slotIndex) {
            return peek(null, sourceId, slotIndex);
        }

        @Override
        public ItemStack peek(ServerPlayer player, String sourceId, int slotIndex) {
            Map<Integer, ItemStack> source = contents.get(sourceId);
            if (source == null) {
                return ItemStack.EMPTY;
            }
            return source.getOrDefault(slotIndex, ItemStack.EMPTY);
        }

        @Override
        public ItemStack extract(ServerPlayer player, String sourceId, int slotIndex, int amount, boolean simulate) {
            if ("test:provider/main".equals(sourceId)) {
                providerExtractCalls++;
            }
            ItemStack current = peek(player, sourceId, slotIndex);
            if (current.isEmpty() || amount <= 0) {
                return ItemStack.EMPTY;
            }
            int take = Math.min(amount, current.getCount());
            ItemStack extracted = new ItemStack(current.itemId(), take, current.getMaxStackSize());
            if (!simulate) {
                current.shrink(take);
            }
            return extracted;
        }

        @Override
        public ItemStack insertBestFit(ServerPlayer player, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public ItemStack insertIntoProviders(ServerPlayer player, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public Optional<CarriedLocation> findIdentity(ServerPlayer player, ItemIdentity identity) {
            return Optional.empty();
        }

        @Override
        public List<CarriedLocation> findAllMatching(ServerPlayer player, ItemIdentity identity) {
            return contents.entrySet().stream()
                    .flatMap(source -> source.getValue().entrySet().stream()
                            .filter(entry -> ItemIdentityMatcher.matchesMovable(entry.getValue(), identity))
                            .map(entry -> new CarriedLocation(source.getKey(), entry.getKey())))
                    .toList();
        }

        @Override
        public InventoryAuthoritySnapshot currentAuthority(ServerPlayer player) {
            currentAuthorityCalls++;
            return InventoryAuthoritySnapshot.empty();
        }

        @Override
        public CarriedSourceAccess.CarriedStoragePressure carriedStoragePressure(ServerPlayer player) {
            pressureCalls++;
            return pressure;
        }
    }

    private static final class NoOpWorldStorageAccess implements WorldStorageAccess {
        @Override
        public ItemStack insert(MinecraftServer server, Target target, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public ItemStack extract(MinecraftServer server, Target target, int slotIndex, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public List<SlotContent> enumerate(MinecraftServer server, Target target) {
            return List.of();
        }

        @Override
        public int slotCount(MinecraftServer server, Target target) {
            return 0;
        }

        @Override
        public boolean isAccessible(MinecraftServer server, Target target) {
            return false;
        }

        @Override
        public void registerDelegate(Delegate delegate) {
        }
    }
}
