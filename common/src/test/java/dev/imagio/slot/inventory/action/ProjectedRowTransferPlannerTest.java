package dev.imagio.slot.inventory.action;

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
import dev.imagio.slot.inventory.core.InventoryTopologyDescriptor;
import dev.imagio.slot.inventory.core.ItemComparisonMode;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.PlayerRuntimeStateDescriptor;
import dev.imagio.slot.inventory.integration.InventoryHostSession;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntryKey;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.inventory.query.InventorySourceSnapshot;
import dev.imagio.slot.inventory.query.InventoryWorkingSetProjection;
import dev.imagio.slot.inventory.query.InventoryWorkingSetProjectionService;
import dev.imagio.slot.inventory.query.ProjectedInventoryRow;
import dev.imagio.slot.workflow.domain.ProtectionPolicy;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectedRowTransferPlannerTest {
    @Test
    void moveStackUsesFirstBackingEntryOnly() {
        InventoryHostDescriptor host = host(List.of(
                BuiltinInventoryDescriptors.playerMain(InventoryTopologyDescriptor.empty()),
                carriedMenuSource("carried.backpack.one", 40, 9),
                carriedMenuSource("carried.backpack.two", 50, 9),
                externalMenuSource("external.chest", 100, 3)
        ));
        InventoryAuthoritySnapshot authority = new InventoryAuthoritySnapshot(
                host,
                Map.of(
                        BuiltinInventoryIds.PLAYER_MAIN, slotSource(BuiltinInventoryIds.PLAYER_MAIN, 27, slotEntry(BuiltinInventoryIds.PLAYER_MAIN, 0, "minecraft:apple", 3, 64)),
                        "carried.backpack.one", slotSource("carried.backpack.one", 9, slotEntry("carried.backpack.one", 0, "minecraft:apple", 2, 64)),
                        "carried.backpack.two", slotSource("carried.backpack.two", 9, slotEntry("carried.backpack.two", 0, "minecraft:apple", 5, 64)),
                        "external.chest", slotSource("external.chest", 3)
                ),
                dev.imagio.slot.inventory.query.CursorStateSnapshot.empty()
        );
        ProjectedInventoryRow row = projectedRow(authority, InventoryPaneMembership.CARRIED, "minecraft:apple");

        ProjectedRowTransferPlan plan = ProjectedRowTransferPlanner.plan(new ProjectedRowTransferIntent(
                authority,
                List.of(row),
                row,
                InventoryActionKind.TRANSFER_STACK,
                InventoryActionScope.BEST_SINGLE_SOURCE,
                new InventoryActionDestination.SourceDestination("external.chest"),
                ProtectionPolicy.allowAll(),
                InventoryActionMode.EXECUTE,
                "test"
        ));

        assertEquals(1, plan.operations().size());
        assertEquals(1, plan.requests().size());
        InventoryActionRequest request = plan.requests().getFirst();
        assertEquals(new InventoryActionTarget.SourceSlotTarget(BuiltinInventoryIds.PLAYER_MAIN, 0), request.primaryTarget());
        assertEquals(new InventoryActionTarget.SourceTarget("external.chest"), request.secondaryTarget());
        assertEquals(3, request.requestedCount());
        assertEquals(3, plan.requestedTotalCount());
        assertEquals(3, plan.plannedTotalCount());
    }

    @Test
    void moveAllExactPlansAllBackingEntriesInStableOrder() {
        InventoryHostDescriptor host = host(List.of(
                BuiltinInventoryDescriptors.playerMain(InventoryTopologyDescriptor.empty()),
                carriedMenuSource("carried.backpack.one", 40, 9),
                carriedMenuSource("carried.backpack.two", 50, 9),
                externalMenuSource("external.chest", 100, 3)
        ));
        InventoryAuthoritySnapshot authority = new InventoryAuthoritySnapshot(
                host,
                Map.of(
                        BuiltinInventoryIds.PLAYER_MAIN, slotSource(BuiltinInventoryIds.PLAYER_MAIN, 27, slotEntry(BuiltinInventoryIds.PLAYER_MAIN, 0, "minecraft:apple", 3, 64)),
                        "carried.backpack.one", slotSource("carried.backpack.one", 9, slotEntry("carried.backpack.one", 0, "minecraft:apple", 2, 64)),
                        "carried.backpack.two", slotSource("carried.backpack.two", 9, slotEntry("carried.backpack.two", 0, "minecraft:apple", 5, 64)),
                        "external.chest", slotSource("external.chest", 3)
                ),
                dev.imagio.slot.inventory.query.CursorStateSnapshot.empty()
        );
        ProjectedInventoryRow row = projectedRow(authority, InventoryPaneMembership.CARRIED, "minecraft:apple");

        ProjectedRowTransferPlan plan = ProjectedRowTransferPlanner.plan(new ProjectedRowTransferIntent(
                authority,
                List.of(row),
                row,
                InventoryActionKind.TRANSFER_ALL,
                InventoryActionScope.VISIBLE_MATCHES,
                new InventoryActionDestination.SourceDestination("external.chest"),
                ProtectionPolicy.allowAll(),
                InventoryActionMode.EXECUTE,
                "test"
        ));

        assertEquals(List.of(
                new InventoryActionTarget.SourceSlotTarget(BuiltinInventoryIds.PLAYER_MAIN, 0),
                new InventoryActionTarget.SourceSlotTarget("carried.backpack.one", 0),
                new InventoryActionTarget.SourceSlotTarget("carried.backpack.two", 0)
        ), plan.requests().stream().map(InventoryActionRequest::primaryTarget).toList());
        assertEquals(List.of(3, 2, 5), plan.requests().stream().map(InventoryActionRequest::requestedCount).toList());
        assertEquals(10, plan.requestedTotalCount());
        assertEquals(10, plan.plannedTotalCount());
        assertEquals(InventoryActionStatus.SUCCESS, plan.status());
    }

    @Test
    void moveAllVisiblePreservesCallerRowOrderThenBackingEntryOrder() {
        InventoryHostDescriptor host = host(List.of(
                BuiltinInventoryDescriptors.playerMain(InventoryTopologyDescriptor.empty()),
                carriedMenuSource("carried.backpack", 40, 9),
                externalMenuSource("external.chest", 100, 4)
        ));
        InventoryAuthoritySnapshot authority = new InventoryAuthoritySnapshot(
                host,
                Map.of(
                        BuiltinInventoryIds.PLAYER_MAIN, slotSource(
                                BuiltinInventoryIds.PLAYER_MAIN,
                                27,
                                slotEntry(BuiltinInventoryIds.PLAYER_MAIN, 0, "minecraft:apple", 2, 64),
                                slotEntry(BuiltinInventoryIds.PLAYER_MAIN, 1, "minecraft:beetroot", 1, 64)
                        ),
                        "carried.backpack", slotSource(
                                "carried.backpack",
                                9,
                                slotEntry("carried.backpack", 0, "minecraft:apple", 3, 64)
                        ),
                        "external.chest", slotSource("external.chest", 4)
                ),
                dev.imagio.slot.inventory.query.CursorStateSnapshot.empty()
        );
        InventoryWorkingSetProjection projection = InventoryWorkingSetProjectionService.project(authority, InventoryPaneMembership.CARRIED, entry -> identity(entry.stack()));
        ProjectedInventoryRow apples = row(projection, "minecraft:apple");
        ProjectedInventoryRow beetroot = row(projection, "minecraft:beetroot");

        ProjectedRowTransferPlan plan = ProjectedRowTransferPlanner.plan(new ProjectedRowTransferIntent(
                authority,
                List.of(beetroot, apples),
                null,
                InventoryActionKind.TRANSFER_ALL,
                InventoryActionScope.VISIBLE_ROWS,
                new InventoryActionDestination.SourceDestination("external.chest"),
                ProtectionPolicy.allowAll(),
                InventoryActionMode.EXECUTE,
                "test"
        ));

        assertEquals(List.of(
                new InventoryActionTarget.SourceSlotTarget(BuiltinInventoryIds.PLAYER_MAIN, 1),
                new InventoryActionTarget.SourceSlotTarget(BuiltinInventoryIds.PLAYER_MAIN, 0),
                new InventoryActionTarget.SourceSlotTarget("carried.backpack", 0)
        ), plan.requests().stream().map(InventoryActionRequest::primaryTarget).toList());
    }

    @Test
    void protectedSubSourceIsSkippedWhileOtherBackingEntriesStillPlan() {
        InventoryHostDescriptor host = host(List.of(
                BuiltinInventoryDescriptors.playerMain(InventoryTopologyDescriptor.empty()),
                carriedMenuSource("carried.backpack", 40, 9),
                externalMenuSource("external.chest", 100, 3)
        ));
        InventoryAuthoritySnapshot authority = new InventoryAuthoritySnapshot(
                host,
                Map.of(
                        BuiltinInventoryIds.PLAYER_MAIN, slotSource(BuiltinInventoryIds.PLAYER_MAIN, 27, slotEntry(BuiltinInventoryIds.PLAYER_MAIN, 0, "minecraft:apple", 3, 64)),
                        "carried.backpack", slotSource("carried.backpack", 9, slotEntry("carried.backpack", 0, "minecraft:apple", 2, 64)),
                        "external.chest", slotSource("external.chest", 3)
                ),
                dev.imagio.slot.inventory.query.CursorStateSnapshot.empty()
        );
        ProjectedInventoryRow row = projectedRow(authority, InventoryPaneMembership.CARRIED, "minecraft:apple");
        ProtectionPolicy protection = protectingTargets(Set.of(new InventoryActionTarget.SourceSlotTarget(BuiltinInventoryIds.PLAYER_MAIN, 0).stableKey()));

        ProjectedRowTransferPlan plan = ProjectedRowTransferPlanner.plan(new ProjectedRowTransferIntent(
                authority,
                List.of(row),
                row,
                InventoryActionKind.TRANSFER_ALL,
                InventoryActionScope.VISIBLE_MATCHES,
                new InventoryActionDestination.SourceDestination("external.chest"),
                protection,
                InventoryActionMode.EXECUTE,
                "test"
        ));

        assertEquals(List.of(new InventoryActionTarget.SourceSlotTarget("carried.backpack", 0)),
                plan.requests().stream().map(InventoryActionRequest::primaryTarget).toList());
        assertFalse(plan.blockedRows().contains(row));
        assertTrue(plan.blockedEntries().contains(InventoryEntryKey.slot(BuiltinInventoryIds.PLAYER_MAIN, 0)));
    }

    @Test
    void fullyProtectedRowIsReportedBlocked() {
        InventoryHostDescriptor host = host(List.of(
                BuiltinInventoryDescriptors.playerMain(InventoryTopologyDescriptor.empty()),
                externalMenuSource("external.chest", 100, 3)
        ));
        InventoryAuthoritySnapshot authority = new InventoryAuthoritySnapshot(
                host,
                Map.of(
                        BuiltinInventoryIds.PLAYER_MAIN, slotSource(BuiltinInventoryIds.PLAYER_MAIN, 27, slotEntry(BuiltinInventoryIds.PLAYER_MAIN, 0, "minecraft:apple", 3, 64)),
                        "external.chest", slotSource("external.chest", 3)
                ),
                dev.imagio.slot.inventory.query.CursorStateSnapshot.empty()
        );
        ProjectedInventoryRow row = projectedRow(authority, InventoryPaneMembership.CARRIED, "minecraft:apple");
        ProtectionPolicy protection = protectingTargets(Set.of(new InventoryActionTarget.SourceSlotTarget(BuiltinInventoryIds.PLAYER_MAIN, 0).stableKey()));

        ProjectedRowTransferPlan plan = ProjectedRowTransferPlanner.plan(new ProjectedRowTransferIntent(
                authority,
                List.of(row),
                row,
                InventoryActionKind.TRANSFER_ALL,
                InventoryActionScope.VISIBLE_MATCHES,
                new InventoryActionDestination.SourceDestination("external.chest"),
                protection,
                InventoryActionMode.EXECUTE,
                "test"
        ));

        assertTrue(plan.requests().isEmpty());
        assertTrue(plan.blockedRows().contains(row));
        assertTrue(plan.blockedEntries().contains(InventoryEntryKey.slot(BuiltinInventoryIds.PLAYER_MAIN, 0)));
    }

    @Test
    void externalPaneDestinationDistributesAcrossStableSourcesAndBuiltinCapacity() {
        InventoryHostDescriptor host = host(List.of(
                BuiltinInventoryDescriptors.playerMain(InventoryTopologyDescriptor.empty()),
                externalMenuSource("external.first", 100, 1),
                externalMenuSource("external.second", 110, 1)
        ));
        InventoryAuthoritySnapshot authority = new InventoryAuthoritySnapshot(
                host,
                Map.of(
                        BuiltinInventoryIds.PLAYER_MAIN, slotSource(BuiltinInventoryIds.PLAYER_MAIN, 27, slotEntry(BuiltinInventoryIds.PLAYER_MAIN, 0, "minecraft:apple", 5, 64)),
                        "external.first", slotSource("external.first", 1, slotEntry("external.first", 0, "minecraft:apple", 60, 64)),
                        "external.second", slotSource("external.second", 1)
                ),
                dev.imagio.slot.inventory.query.CursorStateSnapshot.empty()
        );
        ProjectedInventoryRow row = projectedRow(authority, InventoryPaneMembership.CARRIED, "minecraft:apple");

        ProjectedRowTransferPlan plan = ProjectedRowTransferPlanner.plan(new ProjectedRowTransferIntent(
                authority,
                List.of(row),
                row,
                InventoryActionKind.TRANSFER_ALL,
                InventoryActionScope.VISIBLE_MATCHES,
                new InventoryActionDestination.PaneDestination(InventoryPaneMembership.EXTERNAL),
                ProtectionPolicy.allowAll(),
                InventoryActionMode.EXECUTE,
                "test"
        ));

        assertEquals(List.of(
                new InventoryActionTarget.SourceTarget("external.first"),
                new InventoryActionTarget.SourceTarget("external.second")
        ), plan.requests().stream().map(InventoryActionRequest::secondaryTarget).toList());
        assertEquals(List.of(4, 1), plan.requests().stream().map(InventoryActionRequest::requestedCount).toList());
    }

    @Test
    void carriedPaneDestinationAvoidsQuickAccessAndEquipmentAndStopsWhenFull() {
        InventoryHostDescriptor host = host(List.of(
                BuiltinInventoryDescriptors.playerMain(InventoryTopologyDescriptor.empty()),
                BuiltinInventoryDescriptors.quickAccessLane0Source(InventoryTopologyDescriptor.empty()),
                BuiltinInventoryDescriptors.armorSource(InventoryTopologyDescriptor.empty()),
                BuiltinInventoryDescriptors.offhandSource(InventoryTopologyDescriptor.empty()),
                carriedMenuSource("carried.backpack", 40, 1),
                externalMenuSource("external.chest", 100, 1)
        ));
        InventoryAuthoritySnapshot authority = new InventoryAuthoritySnapshot(
                host,
                Map.of(
                        BuiltinInventoryIds.PLAYER_MAIN, slotSource(BuiltinInventoryIds.PLAYER_MAIN, 1, slotEntry(BuiltinInventoryIds.PLAYER_MAIN, 0, "minecraft:apple", 63, 64)),
                        BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0, slotSource(BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0, 9),
                        BuiltinInventoryIds.PLAYER_ARMOR, slotSource(BuiltinInventoryIds.PLAYER_ARMOR, 4),
                        BuiltinInventoryIds.PLAYER_OFFHAND, slotSource(BuiltinInventoryIds.PLAYER_OFFHAND, 1),
                        "carried.backpack", slotSource("carried.backpack", 1, slotEntry("carried.backpack", 0, "minecraft:stone", 64, 64)),
                        "external.chest", slotSource("external.chest", 1, slotEntry("external.chest", 0, "minecraft:apple", 5, 64))
                ),
                dev.imagio.slot.inventory.query.CursorStateSnapshot.empty()
        );
        ProjectedInventoryRow row = projectedRow(authority, InventoryPaneMembership.EXTERNAL, "minecraft:apple");

        ProjectedRowTransferPlan plan = ProjectedRowTransferPlanner.plan(new ProjectedRowTransferIntent(
                authority,
                List.of(row),
                row,
                InventoryActionKind.TRANSFER_ALL,
                InventoryActionScope.VISIBLE_MATCHES,
                new InventoryActionDestination.PaneDestination(InventoryPaneMembership.CARRIED),
                ProtectionPolicy.allowAll(),
                InventoryActionMode.EXECUTE,
                "test"
        ));

        assertEquals(1, plan.requests().size());
        assertEquals(new InventoryActionTarget.SourceTarget(BuiltinInventoryIds.PLAYER_MAIN), plan.requests().getFirst().secondaryTarget());
        assertEquals(1, plan.requests().getFirst().requestedCount());
        assertTrue(plan.blockedEntries().contains(InventoryEntryKey.slot("external.chest", 0)));
        assertTrue(plan.operations().getFirst().diagnostics().contains("entry_partially_planned:slot:external.chest#0"));
        assertEquals(InventoryActionStatus.PARTIAL, plan.status());
        assertTrue(plan.reasonCodes().contains(InventoryCommandReasonCode.ENTRY_PARTIALLY_PLANNED));
    }

    @Test
    void nonSimulatableProviderDestinationPlansBestEffortWithUncertainty() {
        InventoryHostDescriptor host = host(List.of(
                BuiltinInventoryDescriptors.playerMain(InventoryTopologyDescriptor.empty()),
                providerSource("external.provider", InventoryPaneMembership.EXTERNAL, 100, false)
        ));
        InventoryAuthoritySnapshot authority = new InventoryAuthoritySnapshot(
                host,
                Map.of(
                        BuiltinInventoryIds.PLAYER_MAIN, slotSource(BuiltinInventoryIds.PLAYER_MAIN, 27, slotEntry(BuiltinInventoryIds.PLAYER_MAIN, 0, "minecraft:apple", 5, 64)),
                        "external.provider", InventorySourceSnapshot.empty("external.provider")
                ),
                dev.imagio.slot.inventory.query.CursorStateSnapshot.empty()
        );
        ProjectedInventoryRow row = projectedRow(authority, InventoryPaneMembership.CARRIED, "minecraft:apple");

        ProjectedRowTransferPlan plan = ProjectedRowTransferPlanner.plan(new ProjectedRowTransferIntent(
                authority,
                List.of(row),
                row,
                InventoryActionKind.TRANSFER_ALL,
                InventoryActionScope.VISIBLE_MATCHES,
                new InventoryActionDestination.SourceDestination("external.provider"),
                ProtectionPolicy.allowAll(),
                InventoryActionMode.EXECUTE,
                "test"
        ));

        PlannedTransferStep step = plan.operations().getFirst().steps().getFirst();
        assertEquals(new InventoryActionTarget.SourceTarget("external.provider"), step.request().secondaryTarget());
        assertTrue(step.capacityUncertain());
        assertEquals(5, step.plannedCount());
        assertTrue(step.diagnostics().contains("provider_destination_capacity_uncertain:external.provider"));
        assertTrue(plan.capacityUncertain());
        assertEquals(InventoryActionStatus.PARTIAL, step.status());
        assertTrue(step.reasonCodes().contains(InventoryCommandReasonCode.PROVIDER_CAPACITY_UNCERTAIN));
        assertEquals(InventoryActionStatus.PARTIAL, plan.status());
    }

    private static ProtectionPolicy protectingTargets(Set<String> stableKeys) {
        return new ProtectionPolicy() {
            @Override
            public boolean protects(ItemIdentity identity, InventoryActionKind actionKind) {
                return false;
            }

            @Override
            public boolean protectsTarget(InventoryActionTarget target, InventoryActionKind actionKind) {
                return stableKeys.contains(target == null ? "" : target.stableKey());
            }

            @Override
            public boolean protectsPortableContainers() {
                return false;
            }
        };
    }

    private static ProjectedInventoryRow projectedRow(
            InventoryAuthoritySnapshot authority,
            InventoryPaneMembership paneMembership,
            String itemId
    ) {
        return row(InventoryWorkingSetProjectionService.project(authority, paneMembership, entry -> identity(entry.stack())), itemId);
    }

    private static ProjectedInventoryRow row(InventoryWorkingSetProjection projection, String itemId) {
        return projection.rows().stream()
                .filter(row -> row.identity() != null && itemId.equals(row.identity().itemId()))
                .findFirst()
                .orElseThrow();
    }

    private static InventoryEntrySnapshot slotEntry(String sourceId, int slotIndex, String itemId, int count, int maxStackSize) {
        return new InventoryEntrySnapshot(
                InventoryEntryKey.slot(sourceId, slotIndex),
                new ItemStack(itemId, count, maxStackSize),
                count,
                ""
        );
    }

    private static InventorySourceSnapshot slotSource(String sourceId, int slotCapacity, InventoryEntrySnapshot... entries) {
        return new InventorySourceSnapshot(sourceId, slotCapacity, List.of(entries), "");
    }

    private static ItemIdentity identity(ItemStack stack) {
        return new ItemIdentity(stack.itemId(), ItemComparisonMode.ITEM_ID, stack.componentFingerprint());
    }

    private static InventorySourceDescriptor carriedMenuSource(String sourceId, int stableOrder, int slotCount) {
        return source(sourceId, InventoryPaneMembership.CARRIED, InventorySourceRole.PROVIDER_DEFINED, stableOrder, slotCount, InventoryBindingRoute.MENU, false);
    }

    private static InventorySourceDescriptor externalMenuSource(String sourceId, int stableOrder, int slotCount) {
        return source(sourceId, InventoryPaneMembership.EXTERNAL, InventorySourceRole.PRIMARY_STORAGE, stableOrder, slotCount, InventoryBindingRoute.MENU, false);
    }

    private static InventorySourceDescriptor providerSource(String sourceId, InventoryPaneMembership pane, int stableOrder, boolean simulationSupported) {
        return source(sourceId, pane, InventorySourceRole.PROVIDER_DEFINED, stableOrder, 0, InventoryBindingRoute.PROVIDER, simulationSupported);
    }

    private static InventorySourceDescriptor source(
            String sourceId,
            InventoryPaneMembership paneMembership,
            InventorySourceRole role,
            int stableOrder,
            int slotCount,
            InventoryBindingRoute bindingRoute,
            boolean simulationSupported
    ) {
        return InventorySourceDescriptor.builder(sourceId)
                .label(Component.literal(sourceId))
                .domain(bindingRoute == InventoryBindingRoute.PROVIDER ? InventorySourceDomain.PLAYER_EXTENSION : InventorySourceDomain.HOST_STORAGE)
                .role(role)
                .logicalSlotCount(slotCount)
                .bindingRoute(bindingRoute)
                .capabilities(Set.of(InventoryCapability.INSERT, InventoryCapability.EXTRACT))
                .simulationSupported(simulationSupported)
                .actionRoute(bindingRoute == InventoryBindingRoute.PROVIDER ? InventoryActionRoute.PROVIDER_MUTATION : InventoryActionRoute.MENU_MUTATION)
                .paneMembership(paneMembership)
                .stableOrder(stableOrder)
                .build();
    }

    private static InventoryHostDescriptor host(List<InventorySourceDescriptor> sources) {
        TestMenu menu = new TestMenu();
        return new InventoryHostDescriptor(
                new HostInstanceKey(TestMenu.class.getName(), 1, "test.provider", ""),
                InventoryHostDescriptor.serverMenuRef(menu),
                "test.screen",
                Component.literal("Test"),
                menu,
                InventoryTopologyDescriptor.empty(),
                InventoryHostSession.empty(),
                List.of(),
                PlayerRuntimeStateDescriptor.vanilla(0),
                sources,
                BuiltinInventoryDescriptors.builtInQuickAccessLanes(),
                BuiltinInventoryDescriptors.builtInEquipmentGroups(),
                List.of(),
                dev.imagio.slot.inventory.integration.InventoryHostObservationHints.defaults(),
                ""
        );
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
