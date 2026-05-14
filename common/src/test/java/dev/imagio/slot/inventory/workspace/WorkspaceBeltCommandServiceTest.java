package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.action.InventoryActionKind;
import dev.imagio.slot.inventory.action.InventoryActionOutcome;
import dev.imagio.slot.inventory.action.InventoryActionRequest;
import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.core.BuiltinInventoryDescriptors;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.HostInstanceKey;
import dev.imagio.slot.inventory.core.InventoryActionRoute;
import dev.imagio.slot.inventory.core.InventoryBindingRoute;
import dev.imagio.slot.inventory.core.InventoryCapability;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceDomain;
import dev.imagio.slot.inventory.core.InventorySourceRole;
import dev.imagio.slot.inventory.core.InventoryStackSnapshot;
import dev.imagio.slot.inventory.core.InventoryTopologyDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.PlayerRuntimeStateDescriptor;
import dev.imagio.slot.inventory.integration.InventoryHostObservationHints;
import dev.imagio.slot.inventory.integration.InventoryHostSession;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.storage.CarriedSourceAccess;
import dev.imagio.slot.testsupport.InventoryAuthorityFixtures;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkspaceBeltCommandServiceTest {
    @Test
    void firstPartialOrFreeHotbarSlotPrefersMatchingPartialStack() {
        SlotWorkspaceViewModel viewModel = viewModel(
                occupied(1, "minecraft:dirt", 64, 64),
                occupied(4, "minecraft:stone", 12, 64)
        );

        int selected = WorkspaceBeltCommandService.firstPartialOrFreeHotbarSlot(
                viewModel,
                ItemIdentity.of("minecraft:stone")
        );

        assertEquals(4, selected);
    }

    @Test
    void firstPartialOrFreeHotbarSlotFallsBackToFirstFreeSlot() {
        SlotWorkspaceViewModel viewModel = viewModel(
                occupied(0, "minecraft:stone", 64, 64),
                occupied(1, "minecraft:dirt", 64, 64)
        );

        int selected = WorkspaceBeltCommandService.firstPartialOrFreeHotbarSlot(
                viewModel,
                ItemIdentity.of("minecraft:stone")
        );

        assertEquals(2, selected);
    }

    @Test
    void firstPartialOrFreeHotbarSlotRejectsFullBelt() {
        List<SlotWorkspaceViewModel.HotbarSlot> slots = new ArrayList<>();
        for (int index = 0; index < 9; index++) {
            slots.add(occupied(index, "minecraft:item_" + index, 64, 64));
        }

        int selected = WorkspaceBeltCommandService.firstPartialOrFreeHotbarSlot(
                viewModel(slots),
                ItemIdentity.of("minecraft:stone")
        );

        assertEquals(-1, selected);
    }

    @Test
    void firstPartialFreeOrOldestHotbarSlotStillPrefersMatchingPartialStack() {
        SlotWorkspaceViewModel viewModel = viewModel(
                occupied(0, "minecraft:dirt", 64, 64),
                occupied(1, "minecraft:stone", 12, 64)
        );

        int selected = WorkspaceBeltCommandService.firstPartialFreeOrOldestHotbarSlot(
                viewModel,
                ItemIdentity.of("minecraft:stone"),
                Map.of(0, 1L, 1, 2L));

        assertEquals(1, selected);
    }

    @Test
    void firstPartialFreeOrOldestHotbarSlotFallsBackToFreeSlotBeforeEviction() {
        SlotWorkspaceViewModel viewModel = viewModel(
                occupied(0, "minecraft:dirt", 64, 64),
                occupied(2, "minecraft:granite", 64, 64)
        );

        int selected = WorkspaceBeltCommandService.firstPartialFreeOrOldestHotbarSlot(
                viewModel,
                ItemIdentity.of("minecraft:stone"),
                Map.of(0, 1L, 2, 2L));

        assertEquals(1, selected);
    }

    @Test
    void firstPartialFreeOrOldestHotbarSlotEvictsLeastRecentlyPlacedSlotWhenFull() {
        List<SlotWorkspaceViewModel.HotbarSlot> slots = new ArrayList<>();
        for (int index = 0; index < 9; index++) {
            slots.add(occupied(index, "minecraft:item_" + index, 64, 64));
        }

        int selected = WorkspaceBeltCommandService.firstPartialFreeOrOldestHotbarSlot(
                viewModel(slots),
                ItemIdentity.of("minecraft:stone"),
                Map.of(0, 20L, 1, 10L, 2, 30L, 3, 40L, 4, 50L, 5, 60L, 6, 70L, 7, 80L, 8, 90L));

        assertEquals(1, selected);
    }

    @Test
    void firstPartialFreeOrOldestHotbarSlotProtectsUntrackedFullSlotsWhenTrackedSlotsExist() {
        List<SlotWorkspaceViewModel.HotbarSlot> slots = new ArrayList<>();
        for (int index = 0; index < 9; index++) {
            slots.add(occupied(index, "minecraft:item_" + index, 64, 64));
        }

        int selected = WorkspaceBeltCommandService.firstPartialFreeOrOldestHotbarSlot(
                viewModel(slots),
                ItemIdentity.of("minecraft:stone"),
                Map.of(1, 10L, 2, 20L));

        assertEquals(1, selected);
    }

    @Test
    void hotbarSlotRecencyTrackerLearnsManualSlotChangesFromProjection() {
        HotbarSlotRecencyTracker tracker = new HotbarSlotRecencyTracker();
        tracker.observe(viewModel(
                occupied(0, "minecraft:old_pick", 1, 1),
                occupied(1, "minecraft:old_shovel", 1, 1),
                selected(2, "minecraft:new_axe", 1, 1)
        ));

        int selected = WorkspaceBeltCommandService.firstPartialFreeOrOldestHotbarSlot(
                viewModel(
                        occupied(0, "minecraft:old_pick", 1, 1),
                        occupied(1, "minecraft:old_shovel", 1, 1),
                        selected(2, "minecraft:new_axe", 1, 1),
                        occupied(3, "minecraft:item_3", 64, 64),
                        occupied(4, "minecraft:item_4", 64, 64),
                        occupied(5, "minecraft:item_5", 64, 64),
                        occupied(6, "minecraft:item_6", 64, 64),
                        occupied(7, "minecraft:item_7", 64, 64),
                        occupied(8, "minecraft:item_8", 64, 64)
                ),
                ItemIdentity.of("minecraft:stone"),
                tracker.placementSequence());

        assertEquals(0, selected);
    }

    @Test
    void firstPartialFreeOrOldestHotbarSlotDoesNotEvictFullMatchingStackWhenAnotherSlotCanMove() {
        List<SlotWorkspaceViewModel.HotbarSlot> slots = new ArrayList<>();
        slots.add(occupied(0, "minecraft:oak_log", 64, 64));
        for (int index = 1; index < 9; index++) {
            slots.add(occupied(index, "minecraft:item_" + index, 64, 64));
        }

        int selected = WorkspaceBeltCommandService.firstPartialFreeOrOldestHotbarSlot(
                viewModel(slots),
                ItemIdentity.of("minecraft:oak_log"),
                Map.of(0, 1L, 1, 2L, 2, 3L, 3, 4L, 4, 5L, 5, 6L, 6, 7L, 7, 8L, 8, 9L));

        assertEquals(1, selected);
    }

    @Test
    void firstPartialFreeOrOldestHotbarSlotFallsBackToMatchingStackWhenAllSlotsAlreadyMatch() {
        List<SlotWorkspaceViewModel.HotbarSlot> slots = new ArrayList<>();
        for (int index = 0; index < 9; index++) {
            slots.add(occupied(index, "minecraft:oak_log", 64, 64));
        }

        int selected = WorkspaceBeltCommandService.firstPartialFreeOrOldestHotbarSlot(
                viewModel(slots),
                ItemIdentity.of("minecraft:oak_log"),
                Map.of(0, 9L, 1, 8L, 2, 7L, 3, 6L, 4, 5L, 5, 4L, 6, 3L, 7, 2L, 8, 1L));

        assertEquals(8, selected);
    }

    @Test
    void assignHomeToFreeHotbarUsesCommonFirstSlotSelection() {
        SlotWorkspaceViewModel viewModel = viewModel(
                occupied(0, "minecraft:stone", 64, 64),
                occupied(2, "minecraft:stone", 12, 64)
        );
        AtomicInteger assigned = new AtomicInteger(-1);

        WorkspaceCommandOutcome outcome = WorkspaceBeltCommandService.assignHomeToFreeHotbar(
                null,
                null,
                viewModel,
                ItemIdentity.of("minecraft:stone"),
                true,
                hotbarIndex -> {
                    assigned.set(hotbarIndex);
                    return WorkspaceCommandOutcome.accepted("assigned", "");
                });

        assertTrue(outcome.success());
        assertEquals(2, assigned.get());
    }

    @Test
    void assignHomeToFreeHotbarReportsFullBeltWithoutFallbackMasking() {
        List<SlotWorkspaceViewModel.HotbarSlot> slots = new ArrayList<>();
        for (int index = 0; index < 9; index++) {
            slots.add(occupied(index, "minecraft:item_" + index, 64, 64));
        }

        WorkspaceCommandOutcome outcome = WorkspaceBeltCommandService.assignHomeToFreeHotbar(
                null,
                null,
                viewModel(slots),
                ItemIdentity.of("minecraft:stone"),
                true,
                hotbarIndex -> WorkspaceCommandOutcome.accepted("assigned", ""));

        assertFalse(outcome.success());
        assertEquals("no_free_hotbar_slot", outcome.status());
        assertEquals("all hotbar slots are occupied", outcome.diagnostics());
    }

    @Test
    void hotbarFallbackDepositMissOnlyAllowsKnownMissDiagnostics() {
        assertTrue(WorkspaceBeltCommandService.isHotbarFallbackDepositMiss(
                WorkspaceCommandOutcome.rejected("nothing_to_deposit")));
        assertTrue(WorkspaceBeltCommandService.isHotbarFallbackDepositMiss(
                WorkspaceCommandOutcome.rejected("desired_count_reserved")));
        assertTrue(WorkspaceBeltCommandService.isHotbarFallbackDepositMiss(
                WorkspaceCommandOutcome.rejected("no_linked_proximate_chest_with_room")));
        assertFalse(WorkspaceBeltCommandService.isHotbarFallbackDepositMiss(
                WorkspaceCommandOutcome.rejected("provider_failure")));
        assertFalse(WorkspaceBeltCommandService.isHotbarFallbackDepositMiss(
                WorkspaceCommandOutcome.accepted("deposited_stack", "")));
    }

    @Test
    void hotbarAssignmentSourceLookupPrefersActiveAuthoritySourceOverProviderMirror() {
        InventorySourceDescriptor openBackpack = carriedSource(
                "sophisticatedbackpacks:open_backpack",
                InventorySourceDomain.HOST_STORAGE,
                InventorySourceRole.PRIMARY_STORAGE,
                InventoryBindingRoute.MENU,
                5);
        InventorySourceDescriptor providerMirror = carriedSource(
                "sophisticatedbackpacks:carried/abc",
                InventorySourceDomain.PLAYER_EXTENSION,
                InventorySourceRole.PROVIDER_DEFINED,
                InventoryBindingRoute.PROVIDER,
                15);
        InventoryHostDescriptor host = host(openBackpack, providerMirror);
        InventoryAuthoritySnapshot authority = InventoryAuthorityFixtures.authority(
                host,
                Map.of(
                        openBackpack.id(), List.of(new InventoryStackSnapshot(
                                7,
                                new ItemStack("minecraft:stone", 3, 64),
                                3)),
                        providerMirror.id(), List.of(new InventoryStackSnapshot(
                                2,
                                new ItemStack("minecraft:stone", 3, 64),
                                3))
                ),
                Map.of(openBackpack.id(), 27, providerMirror.id(), 27)
        );

        var located = WorkspaceBeltCommandService.findIdentityInAuthority(
                authority,
                ItemIdentity.of("minecraft:stone"));

        assertTrue(located.isPresent());
        assertEquals(openBackpack.id(), located.get().sourceId());
        assertEquals(7, located.get().slotIndex());
    }

    @Test
    void hotbarAssignmentUsesDirectAssignForPlayerSourceWhenHotbarIsOccupiedAndMainHasNoFreeSlot() {
        InventorySourceDescriptor main = BuiltinInventoryDescriptors.playerMain(InventoryTopologyDescriptor.empty());
        InventorySourceDescriptor hotbar = BuiltinInventoryDescriptors.quickAccessLane0Source(InventoryTopologyDescriptor.empty());
        InventoryHostDescriptor host = host(main, hotbar);
        InventoryAuthoritySnapshot authority = InventoryAuthorityFixtures.authority(
                host,
                Map.of(
                        BuiltinInventoryIds.PLAYER_MAIN, List.of(
                                new InventoryStackSnapshot(0, new ItemStack("minecraft:dirt", 64, 64), 64),
                                new InventoryStackSnapshot(1, new ItemStack("minecraft:barrel", 1, 64), 1)),
                        BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0, List.of(
                                new InventoryStackSnapshot(0, new ItemStack("minecraft:stone", 64, 64), 64))
                ),
                Map.of(BuiltinInventoryIds.PLAYER_MAIN, 2, BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0, 9)
        );
        AtomicReference<InventoryActionRequest> requestRef = new AtomicReference<>();

        WorkspaceCommandOutcome outcome = WorkspaceBeltCommandService.assignIdentityToHotbarByTransfer(
                new ServerPlayer(),
                host,
                authority,
                ThrowingCarriedSourceAccess.INSTANCE,
                request -> {
                    requestRef.set(request);
                    return accepted(host, request);
                },
                ItemIdentity.of("minecraft:barrel"),
                0,
                "test");

        assertTrue(outcome.success());
        assertEquals(InventoryActionKind.ASSIGN, requestRef.get().kind());
        assertEquals("test.assign_identity_to_hotbar_swap", requestRef.get().origin());
    }

    @Test
    void hotbarAssignmentFallsBackToUniqueStackableItemIdWhenExactFingerprintDrifts() {
        InventorySourceDescriptor main = BuiltinInventoryDescriptors.playerMain(InventoryTopologyDescriptor.empty());
        InventorySourceDescriptor hotbar = BuiltinInventoryDescriptors.quickAccessLane0Source(InventoryTopologyDescriptor.empty());
        InventoryHostDescriptor host = host(main, hotbar);
        ItemStack liveSteel = new ItemStack("mekanism:steel_ingot", "{server:1}", 16, 64);
        InventoryAuthoritySnapshot authority = InventoryAuthorityFixtures.authority(
                host,
                Map.of(BuiltinInventoryIds.PLAYER_MAIN, List.of(
                        new InventoryStackSnapshot(0, liveSteel, 16)
                )),
                Map.of(BuiltinInventoryIds.PLAYER_MAIN, 27, BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0, 9)
        );
        AtomicReference<InventoryActionRequest> requestRef = new AtomicReference<>();

        WorkspaceCommandOutcome outcome = WorkspaceBeltCommandService.assignIdentityToHotbarByTransfer(
                new ServerPlayer(),
                host,
                authority,
                ThrowingCarriedSourceAccess.INSTANCE,
                request -> {
                    requestRef.set(request);
                    return accepted(host, request);
                },
                ItemIdentity.exact("mekanism:steel_ingot", "{client:1}"),
                4,
                "test");

        assertTrue(outcome.success());
        assertEquals(InventoryActionKind.TRANSFER, requestRef.get().kind());
        assertEquals(ItemIdentity.exact("mekanism:steel_ingot", "{server:1}"), requestRef.get().identity());
        InventoryActionTarget.QuickAccessTarget target =
                (InventoryActionTarget.QuickAccessTarget) requestRef.get().secondaryTarget();
        assertEquals(4, target.slotIndex());
    }

    @Test
    void hotbarAssignmentDoesNotRelaxAmbiguousStackableVariants() {
        InventorySourceDescriptor main = BuiltinInventoryDescriptors.playerMain(InventoryTopologyDescriptor.empty());
        InventorySourceDescriptor hotbar = BuiltinInventoryDescriptors.quickAccessLane0Source(InventoryTopologyDescriptor.empty());
        InventoryHostDescriptor host = host(main, hotbar);
        InventoryAuthoritySnapshot authority = InventoryAuthorityFixtures.authority(
                host,
                Map.of(BuiltinInventoryIds.PLAYER_MAIN, List.of(
                        new InventoryStackSnapshot(0, new ItemStack("minecraft:firework_rocket", "{Flight:1}", 16, 64), 16),
                        new InventoryStackSnapshot(1, new ItemStack("minecraft:firework_rocket", "{Flight:3}", 16, 64), 16)
                )),
                Map.of(BuiltinInventoryIds.PLAYER_MAIN, 27, BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0, 9)
        );
        AtomicInteger dispatched = new AtomicInteger();

        WorkspaceCommandOutcome outcome = WorkspaceBeltCommandService.assignIdentityToHotbarByTransfer(
                new ServerPlayer(),
                host,
                authority,
                EmptyCarriedSourceAccess.INSTANCE,
                request -> {
                    dispatched.incrementAndGet();
                    return accepted(host, request);
                },
                ItemIdentity.exact("minecraft:firework_rocket", "{client:1}"),
                4,
                "test");

        assertFalse(outcome.success());
        assertEquals(WorkspaceBeltCommandService.CARRIED_IDENTITY_NOT_FOUND, outcome.diagnostics());
        assertEquals(0, dispatched.get());
    }

    private static SlotWorkspaceViewModel viewModel(SlotWorkspaceViewModel.HotbarSlot... occupied) {
        ArrayList<SlotWorkspaceViewModel.HotbarSlot> slots = new ArrayList<>(SlotWorkspaceViewModel.emptyHotbar());
        for (SlotWorkspaceViewModel.HotbarSlot slot : occupied) {
            slots.set(slot.hotbarIndex(), slot);
        }
        return viewModel(slots);
    }

    private static SlotWorkspaceViewModel viewModel(List<SlotWorkspaceViewModel.HotbarSlot> hotbar) {
        return new SlotWorkspaceViewModel(
                0,
                "ready",
                "",
                0,
                0,
                1,
                1,
                0,
                0,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                hotbar,
                SlotWorkspaceViewModel.OffhandSlot.empty(),
                List.of()
        );
    }

    private static SlotWorkspaceViewModel.HotbarSlot occupied(
            int index,
            String itemId,
            int count,
            int maxStackSize
    ) {
        return new SlotWorkspaceViewModel.HotbarSlot(
                index,
                false,
                true,
                new ItemStack(itemId, count, maxStackSize),
                count);
    }

    private static SlotWorkspaceViewModel.HotbarSlot selected(
            int index,
            String itemId,
            int count,
            int maxStackSize
    ) {
        return new SlotWorkspaceViewModel.HotbarSlot(
                index,
                true,
                true,
                new ItemStack(itemId, count, maxStackSize),
                count);
    }

    private static InventorySourceDescriptor carriedSource(
            String sourceId,
            InventorySourceDomain domain,
            InventorySourceRole role,
            InventoryBindingRoute bindingRoute,
            int stableOrder
    ) {
        return InventorySourceDescriptor.builder(sourceId)
                .label(Component.literal(sourceId))
                .domain(domain)
                .role(role)
                .logicalSlotCount(27)
                .bindingRoute(bindingRoute)
                .capabilities(Set.of(InventoryCapability.INSERT, InventoryCapability.EXTRACT))
                .actionRoute(InventoryActionRoute.PROVIDER_MUTATION)
                .paneMembership(InventoryPaneMembership.CARRIED)
                .stableOrder(stableOrder)
                .build();
    }

    private static InventoryHostDescriptor host(InventorySourceDescriptor... sources) {
        TestMenu menu = new TestMenu();
        return new InventoryHostDescriptor(
                new HostInstanceKey(TestMenu.class.getName(), 0, "slot.workspace.test", ""),
                InventoryHostDescriptor.serverMenuRef(menu),
                "slot.workspace.test",
                Component.literal("Workspace Test"),
                menu,
                InventoryTopologyDescriptor.empty(),
                InventoryHostSession.empty(),
                List.of(),
                PlayerRuntimeStateDescriptor.vanilla(0),
                List.of(sources),
                BuiltinInventoryDescriptors.builtInQuickAccessLanes(),
                BuiltinInventoryDescriptors.builtInEquipmentGroups(),
                List.of(),
                InventoryHostObservationHints.defaults(),
                ""
        );
    }

    private static InventoryActionOutcome accepted(InventoryHostDescriptor host, InventoryActionRequest request) {
        return new InventoryActionOutcome(
                host.hostId(),
                host.serverMenuRef(),
                request.requestId(),
                request.kind(),
                request.mode(),
                request.quantity(),
                request.scope(),
                request.conflictPolicy(),
                request.origin(),
                request.primaryTarget(),
                request.secondaryTarget(),
                true,
                List.of(),
                ItemStack.EMPTY,
                "");
    }

    private enum ThrowingCarriedSourceAccess implements CarriedSourceAccess {
        INSTANCE;

        @Override
        public ItemStack peek(ServerPlayer player, String sourceId, int slotIndex) {
            throw new AssertionError("authority should provide the source entry");
        }

        @Override
        public ItemStack extract(ServerPlayer player, String sourceId, int slotIndex, int amount, boolean simulate) {
            throw new AssertionError("not used");
        }

        @Override
        public ItemStack insertBestFit(ServerPlayer player, ItemStack stack, boolean simulate) {
            throw new AssertionError("not used");
        }

        @Override
        public ItemStack insertIntoProviders(ServerPlayer player, ItemStack stack, boolean simulate) {
            throw new AssertionError("not used");
        }

        @Override
        public Optional<CarriedLocation> findIdentity(ServerPlayer player, ItemIdentity identity) {
            throw new AssertionError("authority should provide the source location");
        }

        @Override
        public List<CarriedLocation> findAllMatching(ServerPlayer player, ItemIdentity identity) {
            throw new AssertionError("not used");
        }

        @Override
        public InventoryAuthoritySnapshot currentAuthority(ServerPlayer player) {
            throw new AssertionError("not used");
        }
    }

    private enum EmptyCarriedSourceAccess implements CarriedSourceAccess {
        INSTANCE;

        @Override
        public ItemStack peek(ServerPlayer player, String sourceId, int slotIndex) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack extract(ServerPlayer player, String sourceId, int slotIndex, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertBestFit(ServerPlayer player, ItemStack stack, boolean simulate) {
            return stack == null ? ItemStack.EMPTY : stack.copy();
        }

        @Override
        public ItemStack insertIntoProviders(ServerPlayer player, ItemStack stack, boolean simulate) {
            return stack == null ? ItemStack.EMPTY : stack.copy();
        }

        @Override
        public Optional<CarriedLocation> findIdentity(ServerPlayer player, ItemIdentity identity) {
            return Optional.empty();
        }

        @Override
        public List<CarriedLocation> findAllMatching(ServerPlayer player, ItemIdentity identity) {
            return List.of();
        }

        @Override
        public InventoryAuthoritySnapshot currentAuthority(ServerPlayer player) {
            return InventoryAuthoritySnapshot.empty();
        }
    }

    private static final class TestMenu extends AbstractContainerMenu {
        private TestMenu() {
            super(null, 0);
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    }
}
