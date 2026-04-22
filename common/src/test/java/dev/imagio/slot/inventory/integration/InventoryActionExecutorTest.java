package dev.imagio.slot.inventory.integration;

import dev.imagio.slot.inventory.action.InventoryActionConflictPolicy;
import dev.imagio.slot.inventory.action.InventoryActionKind;
import dev.imagio.slot.inventory.action.InventoryActionMode;
import dev.imagio.slot.inventory.action.InventoryActionOutcome;
import dev.imagio.slot.inventory.action.InventoryActionQuantity;
import dev.imagio.slot.inventory.action.InventoryActionRequest;
import dev.imagio.slot.inventory.action.InventoryActionScope;
import dev.imagio.slot.inventory.action.InventoryActionStatus;
import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.core.BuiltinInventoryDescriptors;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.HostInstanceKey;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.InventoryTopologyDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.core.PlayerRuntimeStateDescriptor;
import dev.imagio.slot.workflow.domain.ProtectionPolicy;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryActionExecutorTest {
    @Test
    void quickAccessAssignReplacesOccupiedHotbarSlotAndDisplacesPreviousStackToSourceSlot() {
        TestMenu menu = new TestMenu();
        InventoryHostDescriptor host = host(menu);
        ServerPlayer player = new ServerPlayer();
        player.containerMenu = menu;
        player.getInventory().items.set(10, new ItemStack("minecraft:crossbow", 1, 1));
        player.getInventory().items.set(2, new ItemStack("toms_storage:inventory_cable", 23, 64));

        InventoryActionRequest request = request(
                host,
                new InventoryActionTarget.SourceSlotTarget(BuiltinInventoryIds.PLAYER_MAIN, 1),
                new InventoryActionTarget.QuickAccessTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, 2),
                player.getInventory().items.get(10)
        );

        InventoryActionOutcome outcome = InventoryActionExecutor.execute(
                host,
                player,
                request,
                ProtectionPolicy.allowAll()
        );

        assertEquals(InventoryActionStatus.SUCCESS, outcome.status());
        assertTrue(outcome.stackRemainder().isEmpty());
        assertEquals("minecraft:crossbow", player.getInventory().items.get(2).itemId());
        assertEquals(1, player.getInventory().items.get(2).getCount());
        assertEquals("toms_storage:inventory_cable", player.getInventory().items.get(10).itemId());
        assertEquals(23, player.getInventory().items.get(10).getCount());
    }

    @Test
    void quickAccessAssignMovesStackIntoEmptyHotbarSlotAndClearsSourceSlot() {
        TestMenu menu = new TestMenu();
        InventoryHostDescriptor host = host(menu);
        ServerPlayer player = new ServerPlayer();
        player.containerMenu = menu;
        player.getInventory().items.set(10, new ItemStack("minecraft:crossbow", 1, 1));

        InventoryActionRequest request = request(
                host,
                new InventoryActionTarget.SourceSlotTarget(BuiltinInventoryIds.PLAYER_MAIN, 1),
                new InventoryActionTarget.QuickAccessTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, 2),
                player.getInventory().items.get(10)
        );

        InventoryActionOutcome outcome = InventoryActionExecutor.execute(
                host,
                player,
                request,
                ProtectionPolicy.allowAll()
        );

        assertEquals(InventoryActionStatus.SUCCESS, outcome.status());
        assertTrue(outcome.stackRemainder().isEmpty());
        assertEquals("minecraft:crossbow", player.getInventory().items.get(2).itemId());
        assertTrue(player.getInventory().items.get(10).isEmpty());
    }

    @Test
    void transferFromProviderBackedCarriedSourceIntoHotbarRoutesThroughExtensionMutate() {
        // Stand-in for a Sophisticated Backpack: a PROVIDER-bound carried source owned
        // by a PlayerInventoryExtension. The builtin executor can't read or write it
        // directly — InventoryActionExecutor.executeTransfer must fall back to
        // providerExtract → InventoryMutationRouter → extension.mutate.
        String backpackSourceId = "test.backpack.a";
        RecordingBackpackExtension extension = new RecordingBackpackExtension(
                backpackSourceId,
                new ItemStack("minecraft:iron_pickaxe", 1, 1)
        );
        TestMenu menu = new TestMenu();
        InventoryHostDescriptor host = hostWithExtension(menu, extension);
        ServerPlayer player = new ServerPlayer();
        player.containerMenu = menu;

        ItemStack expectedStack = extension.currentStack().copy();
        InventoryActionRequest request = new InventoryActionRequest(
                host.hostId(),
                host.serverMenuRef(),
                "test-backpack-to-hotbar",
                InventoryActionKind.TRANSFER,
                InventoryActionMode.EXECUTE,
                InventoryActionQuantity.STACK,
                InventoryActionScope.SINGLE_TARGET,
                InventoryActionConflictPolicy.INSERT_ONLY,
                "test.backpack_to_hotbar",
                new InventoryActionTarget.SourceSlotTarget(backpackSourceId, 0),
                new InventoryActionTarget.QuickAccessTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, 3),
                expectedStack.getCount(),
                ItemIdentityMatcher.create(expectedStack),
                expectedStack.copy(),
                null,
                null,
                false,
                ""
        );

        InventoryActionOutcome outcome = InventoryActionExecutor.execute(
                host,
                player,
                request,
                ProtectionPolicy.allowAll()
        );

        assertEquals(InventoryActionStatus.SUCCESS, outcome.status(),
                "backpack → hotbar transfer should succeed via the extension fallback: "
                        + outcome.diagnostics());
        assertEquals("minecraft:iron_pickaxe", player.getInventory().items.get(3).itemId(),
                "item should land in hotbar slot 3");
        assertEquals(1, player.getInventory().items.get(3).getCount());
        assertTrue(extension.currentStack().isEmpty(),
                "backpack slot should be cleared after the successful transfer");
    }

    @Test
    void transferFromHotbarIntoProviderBackedCarriedSourceRoutesThroughExtensionInsert() {
        // Mirror of the backpack→hotbar test: destination is PROVIDER-bound (a backpack
        // staging slot) while source is PLAYER-bound (the hotbar). This is exactly what
        // LoadoutApplyService produces when kit-page staging falls back to a backpack
        // because the player's main inventory is full.
        String backpackSourceId = "test.backpack.stage";
        RecordingBackpackExtension extension = new RecordingBackpackExtension(
                backpackSourceId, ItemStack.EMPTY
        );
        TestMenu menu = new TestMenu();
        InventoryHostDescriptor host = hostWithExtension(menu, extension);
        ServerPlayer player = new ServerPlayer();
        player.containerMenu = menu;
        player.getInventory().items.set(2, new ItemStack("minecraft:diamond_sword", 1, 1));

        ItemStack stack = player.getInventory().items.get(2);
        InventoryActionRequest request = new InventoryActionRequest(
                host.hostId(),
                host.serverMenuRef(),
                "test-hotbar-to-backpack",
                InventoryActionKind.TRANSFER,
                InventoryActionMode.EXECUTE,
                InventoryActionQuantity.STACK,
                InventoryActionScope.SINGLE_TARGET,
                InventoryActionConflictPolicy.INSERT_ONLY,
                "test.hotbar_to_backpack",
                new InventoryActionTarget.QuickAccessTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, 2),
                new InventoryActionTarget.SourceSlotTarget(backpackSourceId, 0),
                stack.getCount(),
                ItemIdentityMatcher.create(stack),
                stack.copy(),
                null,
                null,
                false,
                ""
        );

        InventoryActionOutcome outcome = InventoryActionExecutor.execute(
                host,
                player,
                request,
                ProtectionPolicy.allowAll()
        );

        assertEquals(InventoryActionStatus.SUCCESS, outcome.status(),
                "hotbar → backpack transfer must work via builtin-extract + provider-insert: "
                        + outcome.diagnostics());
        assertTrue(player.getInventory().items.get(2).isEmpty(),
                "hotbar slot 2 should be cleared after transfer");
        assertEquals("minecraft:diamond_sword", extension.currentStack().itemId(),
                "backpack should now hold the diamond sword");
    }

    @Test
    void transferFromProviderBackedCarriedSourceToAnotherProviderBackedSourceRoutesThroughBothExtensions() {
        // PROVIDER → PROVIDER: both ends go through extension.mutate. Represents a
        // backpack-to-backpack swap where neither end is player-bound.
        RecordingBackpackExtension from = new RecordingBackpackExtension(
                "test.backpack.from",
                new ItemStack("minecraft:emerald", 16, 64)
        );
        RecordingBackpackExtension to = new RecordingBackpackExtension(
                "test.backpack.to", ItemStack.EMPTY
        );
        TestMenu menu = new TestMenu();
        InventoryHostDescriptor host = hostWithExtensions(menu, List.of(from, to));
        ServerPlayer player = new ServerPlayer();
        player.containerMenu = menu;

        ItemStack expected = from.currentStack().copy();
        InventoryActionRequest request = new InventoryActionRequest(
                host.hostId(),
                host.serverMenuRef(),
                "test-backpack-to-backpack",
                InventoryActionKind.TRANSFER,
                InventoryActionMode.EXECUTE,
                InventoryActionQuantity.STACK,
                InventoryActionScope.SINGLE_TARGET,
                InventoryActionConflictPolicy.INSERT_ONLY,
                "test.backpack_to_backpack",
                new InventoryActionTarget.SourceSlotTarget("test.backpack.from", 0),
                new InventoryActionTarget.SourceSlotTarget("test.backpack.to", 0),
                expected.getCount(),
                ItemIdentityMatcher.create(expected),
                expected.copy(),
                null, null, false, ""
        );

        InventoryActionOutcome outcome = InventoryActionExecutor.execute(
                host, player, request, ProtectionPolicy.allowAll()
        );

        assertEquals(InventoryActionStatus.SUCCESS, outcome.status(),
                "backpack → backpack transfer must work via provider-extract + provider-insert: "
                        + outcome.diagnostics());
        assertTrue(from.currentStack().isEmpty(), "source backpack should be cleared");
        assertEquals("minecraft:emerald", to.currentStack().itemId(),
                "destination backpack should now hold the emeralds");
        assertEquals(16, to.currentStack().getCount());
    }

    @Test
    void transferFromMainInventoryIntoHotbarUsesBuiltinOnBothEnds() {
        // PLAYER → PLAYER (hotbar): the fast path. No extension, no fallback.
        TestMenu menu = new TestMenu();
        InventoryHostDescriptor host = host(menu);
        ServerPlayer player = new ServerPlayer();
        player.containerMenu = menu;
        player.getInventory().items.set(10, new ItemStack("minecraft:bread", 5, 64));

        ItemStack stack = player.getInventory().items.get(10);
        InventoryActionRequest request = new InventoryActionRequest(
                host.hostId(), host.serverMenuRef(), "test-main-to-hotbar",
                InventoryActionKind.TRANSFER, InventoryActionMode.EXECUTE,
                InventoryActionQuantity.STACK, InventoryActionScope.SINGLE_TARGET,
                InventoryActionConflictPolicy.INSERT_ONLY, "test.main_to_hotbar",
                new InventoryActionTarget.SourceSlotTarget(BuiltinInventoryIds.PLAYER_MAIN, 1),
                new InventoryActionTarget.QuickAccessTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, 5),
                stack.getCount(), ItemIdentityMatcher.create(stack), stack.copy(),
                null, null, false, ""
        );

        InventoryActionOutcome outcome = InventoryActionExecutor.execute(
                host, player, request, ProtectionPolicy.allowAll()
        );

        assertEquals(InventoryActionStatus.SUCCESS, outcome.status());
        assertEquals("minecraft:bread", player.getInventory().items.get(5).itemId());
        assertEquals(5, player.getInventory().items.get(5).getCount());
        assertTrue(player.getInventory().items.get(10).isEmpty());
    }

    @Test
    void transferFromOffhandIntoHotbarUsesBuiltinOnBothEnds() {
        // PLAYER (offhand) → PLAYER (hotbar).
        TestMenu menu = new TestMenu();
        InventoryHostDescriptor host = host(menu);
        ServerPlayer player = new ServerPlayer();
        player.containerMenu = menu;
        player.getInventory().offhand.set(0, new ItemStack("minecraft:shield", 1, 1));

        ItemStack stack = player.getInventory().offhand.get(0);
        InventoryActionRequest request = new InventoryActionRequest(
                host.hostId(), host.serverMenuRef(), "test-offhand-to-hotbar",
                InventoryActionKind.TRANSFER, InventoryActionMode.EXECUTE,
                InventoryActionQuantity.STACK, InventoryActionScope.SINGLE_TARGET,
                InventoryActionConflictPolicy.INSERT_ONLY, "test.offhand_to_hotbar",
                new InventoryActionTarget.EquipmentTarget(BuiltinInventoryIds.EQUIPMENT_GROUP_OFFHAND, 0),
                new InventoryActionTarget.QuickAccessTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, 8),
                stack.getCount(), ItemIdentityMatcher.create(stack), stack.copy(),
                null, null, false, ""
        );

        InventoryActionOutcome outcome = InventoryActionExecutor.execute(
                host, player, request, ProtectionPolicy.allowAll()
        );

        assertEquals(InventoryActionStatus.SUCCESS, outcome.status());
        assertEquals("minecraft:shield", player.getInventory().items.get(8).itemId());
        assertTrue(player.getInventory().offhand.get(0).isEmpty());
    }

    @Test
    void transferFromMainIntoProviderBackedBackpackRoutesThroughExtensionInsert() {
        // PLAYER (main) → PROVIDER (backpack). Builtin extract + provider insert.
        String backpackSourceId = "test.backpack.overflow";
        RecordingBackpackExtension extension = new RecordingBackpackExtension(
                backpackSourceId, ItemStack.EMPTY
        );
        TestMenu menu = new TestMenu();
        InventoryHostDescriptor host = hostWithExtension(menu, extension);
        ServerPlayer player = new ServerPlayer();
        player.containerMenu = menu;
        player.getInventory().items.set(15, new ItemStack("minecraft:cobblestone", 64, 64));

        ItemStack stack = player.getInventory().items.get(15);
        InventoryActionRequest request = new InventoryActionRequest(
                host.hostId(), host.serverMenuRef(), "test-main-to-backpack",
                InventoryActionKind.TRANSFER, InventoryActionMode.EXECUTE,
                InventoryActionQuantity.STACK, InventoryActionScope.SINGLE_TARGET,
                InventoryActionConflictPolicy.INSERT_ONLY, "test.main_to_backpack",
                new InventoryActionTarget.SourceSlotTarget(BuiltinInventoryIds.PLAYER_MAIN, 6),
                new InventoryActionTarget.SourceSlotTarget(backpackSourceId, 0),
                stack.getCount(), ItemIdentityMatcher.create(stack), stack.copy(),
                null, null, false, ""
        );

        InventoryActionOutcome outcome = InventoryActionExecutor.execute(
                host, player, request, ProtectionPolicy.allowAll()
        );

        assertEquals(InventoryActionStatus.SUCCESS, outcome.status());
        assertTrue(player.getInventory().items.get(15).isEmpty());
        assertEquals("minecraft:cobblestone", extension.currentStack().itemId());
        assertEquals(64, extension.currentStack().getCount());
    }

    @Test
    void transferFromProviderBackedBackpackIntoMainRoutesThroughExtensionExtract() {
        // PROVIDER (backpack) → PLAYER (main). Provider extract + builtin insert.
        String backpackSourceId = "test.backpack.source";
        RecordingBackpackExtension extension = new RecordingBackpackExtension(
                backpackSourceId, new ItemStack("minecraft:redstone", 32, 64)
        );
        TestMenu menu = new TestMenu();
        InventoryHostDescriptor host = hostWithExtension(menu, extension);
        ServerPlayer player = new ServerPlayer();
        player.containerMenu = menu;

        ItemStack expected = extension.currentStack().copy();
        InventoryActionRequest request = new InventoryActionRequest(
                host.hostId(), host.serverMenuRef(), "test-backpack-to-main",
                InventoryActionKind.TRANSFER, InventoryActionMode.EXECUTE,
                InventoryActionQuantity.STACK, InventoryActionScope.SINGLE_TARGET,
                InventoryActionConflictPolicy.INSERT_ONLY, "test.backpack_to_main",
                new InventoryActionTarget.SourceSlotTarget(backpackSourceId, 0),
                new InventoryActionTarget.SourceSlotTarget(BuiltinInventoryIds.PLAYER_MAIN, 20),
                expected.getCount(), ItemIdentityMatcher.create(expected), expected.copy(),
                null, null, false, ""
        );

        InventoryActionOutcome outcome = InventoryActionExecutor.execute(
                host, player, request, ProtectionPolicy.allowAll()
        );

        assertEquals(InventoryActionStatus.SUCCESS, outcome.status());
        assertTrue(extension.currentStack().isEmpty());
        // PLAYER_MAIN slot 20 = inventory.items[20 + 9] = items[29]
        assertEquals("minecraft:redstone", player.getInventory().items.get(29).itemId());
        assertEquals(32, player.getInventory().items.get(29).getCount());
    }

    @Test
    void transferStackMovesHotbarStackIntoMainInventorySource() {
        TestMenu menu = new TestMenu();
        InventoryHostDescriptor host = host(menu);
        ServerPlayer player = new ServerPlayer();
        player.containerMenu = menu;
        player.getInventory().items.set(2, new ItemStack("toms_storage:inventory_cable", 23, 64));

        ItemStack stack = player.getInventory().items.get(2);
        InventoryActionRequest request = new InventoryActionRequest(
                host.hostId(),
                host.serverMenuRef(),
                "test-hotbar-to-main",
                InventoryActionKind.TRANSFER,
                InventoryActionMode.EXECUTE,
                InventoryActionQuantity.STACK,
                InventoryActionScope.SINGLE_TARGET,
                InventoryActionConflictPolicy.INSERT_ONLY,
                "test.hotbar_to_main",
                new InventoryActionTarget.QuickAccessTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, 2),
                new InventoryActionTarget.SourceTarget(BuiltinInventoryIds.PLAYER_MAIN),
                stack.getCount(),
                ItemIdentityMatcher.create(stack),
                stack.copy(),
                null,
                null,
                false,
                ""
        );

        InventoryActionOutcome outcome = InventoryActionExecutor.execute(
                host,
                player,
                request,
                ProtectionPolicy.allowAll()
        );

        assertEquals(InventoryActionStatus.SUCCESS, outcome.status());
        assertTrue(player.getInventory().items.get(2).isEmpty());
        assertEquals("toms_storage:inventory_cable", player.getInventory().items.get(9).itemId());
        assertEquals(23, player.getInventory().items.get(9).getCount());
    }

    private static InventoryActionRequest request(
            InventoryHostDescriptor host,
            InventoryActionTarget source,
            InventoryActionTarget destination,
            ItemStack stack
    ) {
        return new InventoryActionRequest(
                host.hostId(),
                host.serverMenuRef(),
                "test-quick-access-assign",
                InventoryActionKind.ASSIGN,
                InventoryActionMode.EXECUTE,
                InventoryActionQuantity.STACK,
                InventoryActionScope.SINGLE_TARGET,
                InventoryActionConflictPolicy.ASSIGN_WITH_DISPLACE,
                "test.quick_access_assign",
                source,
                destination,
                stack.getCount(),
                ItemIdentityMatcher.create(stack),
                stack.copy(),
                null,
                null,
                false,
                ""
        );
    }

    private static InventoryHostDescriptor host(TestMenu menu) {
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
                List.of(
                        BuiltinInventoryDescriptors.playerMain(InventoryTopologyDescriptor.empty()),
                        BuiltinInventoryDescriptors.quickAccessLane0Source(InventoryTopologyDescriptor.empty()),
                        BuiltinInventoryDescriptors.offhandSource(InventoryTopologyDescriptor.empty())
                ),
                BuiltinInventoryDescriptors.builtInQuickAccessLanes(),
                BuiltinInventoryDescriptors.builtInEquipmentGroups(),
                List.of(),
                InventoryHostObservationHints.defaults(),
                ""
        );
    }

    private static InventoryHostDescriptor hostWithExtensions(TestMenu menu, List<RecordingBackpackExtension> extensions) {
        java.util.List<InventorySourceDescriptor> sources = new java.util.ArrayList<>();
        sources.add(BuiltinInventoryDescriptors.playerMain(InventoryTopologyDescriptor.empty()));
        sources.add(BuiltinInventoryDescriptors.quickAccessLane0Source(InventoryTopologyDescriptor.empty()));
        for (RecordingBackpackExtension extension : extensions) {
            sources.addAll(extension.additionalSources());
        }
        return new InventoryHostDescriptor(
                new HostInstanceKey(TestMenu.class.getName(), 0, "slot.workspace.test", ""),
                InventoryHostDescriptor.serverMenuRef(menu),
                "slot.workspace.test",
                Component.literal("Workspace Test"),
                menu,
                InventoryTopologyDescriptor.empty(),
                InventoryHostSession.empty(),
                List.copyOf(extensions),
                PlayerRuntimeStateDescriptor.vanilla(0),
                List.copyOf(sources),
                BuiltinInventoryDescriptors.builtInQuickAccessLanes(),
                BuiltinInventoryDescriptors.builtInEquipmentGroups(),
                List.of(),
                InventoryHostObservationHints.defaults(),
                ""
        );
    }

    private static InventoryHostDescriptor hostWithExtension(TestMenu menu, RecordingBackpackExtension extension) {
        return new InventoryHostDescriptor(
                new HostInstanceKey(TestMenu.class.getName(), 0, "slot.workspace.test", ""),
                InventoryHostDescriptor.serverMenuRef(menu),
                "slot.workspace.test",
                Component.literal("Workspace Test"),
                menu,
                InventoryTopologyDescriptor.empty(),
                InventoryHostSession.empty(),
                List.of(extension),
                PlayerRuntimeStateDescriptor.vanilla(0),
                java.util.stream.Stream.concat(
                        java.util.stream.Stream.of(
                                BuiltinInventoryDescriptors.playerMain(InventoryTopologyDescriptor.empty()),
                                BuiltinInventoryDescriptors.quickAccessLane0Source(InventoryTopologyDescriptor.empty())
                        ),
                        extension.additionalSources().stream()
                ).toList(),
                BuiltinInventoryDescriptors.builtInQuickAccessLanes(),
                BuiltinInventoryDescriptors.builtInEquipmentGroups(),
                List.of(),
                InventoryHostObservationHints.defaults(),
                ""
        );
    }

    private static final class RecordingBackpackExtension implements PlayerInventoryExtension {
        private final String sourceId;
        private ItemStack stack;

        private RecordingBackpackExtension(String sourceId, ItemStack stack) {
            this.sourceId = sourceId;
            this.stack = stack == null ? ItemStack.EMPTY : stack.copy();
        }

        ItemStack currentStack() {
            return stack;
        }

        @Override
        public String providerId() {
            return "test:backpack";
        }

        @Override
        public List<InventorySourceDescriptor> additionalSources() {
            return List.of(InventorySourceDescriptor.builder(sourceId)
                    .label(Component.literal("Test Backpack"))
                    .domain(dev.imagio.slot.inventory.core.InventorySourceDomain.PLAYER_EXTENSION)
                    .role(dev.imagio.slot.inventory.core.InventorySourceRole.PROVIDER_DEFINED)
                    .logicalSlotCount(1)
                    .bindingRoute(dev.imagio.slot.inventory.core.InventoryBindingRoute.PROVIDER)
                    .capabilities(java.util.Set.of(
                            dev.imagio.slot.inventory.core.InventoryCapability.INSERT,
                            dev.imagio.slot.inventory.core.InventoryCapability.EXTRACT
                    ))
                    .actionRoute(dev.imagio.slot.inventory.core.InventoryActionRoute.PROVIDER_MUTATION)
                    .paneMembership(dev.imagio.slot.inventory.core.InventoryPaneMembership.CARRIED)
                    .stableOrder(50)
                    .build());
        }

        @Override
        public dev.imagio.slot.inventory.query.InventorySourceSnapshot readSourceSnapshot(
                ServerPlayer player, InventoryHostDescriptor host, String sourceId
        ) {
            if (!this.sourceId.equals(sourceId) || stack.isEmpty()) {
                return new dev.imagio.slot.inventory.query.InventorySourceSnapshot(sourceId, 1, List.of(), "");
            }
            return new dev.imagio.slot.inventory.query.InventorySourceSnapshot(
                    sourceId,
                    1,
                    List.of(new dev.imagio.slot.inventory.query.InventoryEntrySnapshot(
                            dev.imagio.slot.inventory.query.InventoryEntryKey.slot(sourceId, 0),
                            stack.copy(),
                            stack.getCount(),
                            ""
                    )),
                    ""
            );
        }

        @Override
        public int serverSlotCapacity(ServerPlayer player, InventoryHostDescriptor host, String sourceId) {
            return this.sourceId.equals(sourceId) ? 1 : 0;
        }

        @Override
        public MutationResult mutate(
                InventoryHostDescriptor host,
                InventoryMutationRequest request,
                InventoryMutationMode mode
        ) {
            if (!sourceId.equals(request.sourceId())) {
                return MutationResult.blocked("wrong_source", request.stack());
            }
            return switch (request.kind()) {
                case EXTRACT -> {
                    if (stack.isEmpty()) {
                        yield MutationResult.blocked("empty", ItemStack.EMPTY);
                    }
                    if (request.identity() != null
                            && !ItemIdentityMatcher.matchesMovable(stack, request.identity())) {
                        yield MutationResult.blocked("identity_mismatch", ItemStack.EMPTY);
                    }
                    ItemStack extracted = stack.copy();
                    if (mode == InventoryMutationMode.EXECUTE) {
                        stack = ItemStack.EMPTY;
                    }
                    yield MutationResult.success(extracted);
                }
                case INSERT -> {
                    ItemStack input = request.stack() == null ? ItemStack.EMPTY : request.stack().copy();
                    if (mode == InventoryMutationMode.EXECUTE) {
                        stack = input.copy();
                    }
                    yield MutationResult.success(ItemStack.EMPTY);
                }
                default -> MutationResult.blocked("unsupported_kind", request.stack());
            };
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
