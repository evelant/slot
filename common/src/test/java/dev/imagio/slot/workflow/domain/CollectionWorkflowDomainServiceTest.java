package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.action.InventoryActionKind;
import dev.imagio.slot.inventory.action.InventoryActionMode;
import dev.imagio.slot.inventory.action.InventoryActionOutcome;
import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.action.InventoryActionStatus;
import dev.imagio.slot.inventory.core.BuiltinInventoryDescriptors;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.HostInstanceKey;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.InventoryStackSnapshot;
import dev.imagio.slot.inventory.core.InventoryTopologyDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.PlayerRuntimeStateDescriptor;
import dev.imagio.slot.inventory.integration.InventoryHostSession;
import dev.imagio.slot.testsupport.InventoryAuthorityFixtures;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CollectionWorkflowDomainServiceTest {
    @Test
    void loadoutSelectionAndMembershipMutationsAreDomainOwned() {
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        CollectionWorkflowDomainService workflow = new CollectionWorkflowDomainService(repository);
        CollectionDefinition tools = workflow.createCollection("Tools");

        QuickAccessLoadoutDefinition first = workflow.createLoadout(
                tools.id(),
                "Builder",
                Set.of(new QuickAccessLoadoutEntry(
                        new LoadoutTarget.QuickAccessLaneTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, 0),
                        ItemIdentity.of("minecraft:stone")
                ))
        );
        QuickAccessLoadoutDefinition second = workflow.createLoadout(
                tools.id(),
                "Combat",
                Set.of(new QuickAccessLoadoutEntry(
                        new LoadoutTarget.EquipmentSlotTarget(BuiltinInventoryIds.EQUIPMENT_GROUP_OFFHAND, 0),
                        ItemIdentity.of("minecraft:shield")
                ))
        );

        assertEquals(second.id(), workflow.selectedLoadout(tools.id()).id());
        assertEquals(second.id(), repository.browseSessionState().current().selectedLoadoutId());
        assertTrue(workflow.cycleSelectedLoadout(tools.id(), 1));
        assertEquals(first.id(), workflow.selectedLoadout(tools.id()).id());
        assertEquals(first.id(), repository.browseSessionState().current().selectedLoadoutId());

        ItemIdentity identity = ItemIdentity.of("minecraft:stone");
        assertTrue(workflow.toggleCollectionMembership(identity, tools.id()));
        assertTrue(repository.workflowProjection().collections().memberships().get(identity).contains(tools.id()));
        assertTrue(workflow.toggleFavorite(identity));
        assertTrue(repository.workflowProjection().favoriteTags().contains(identity));
        // Collection-scoped desired counts retired with the kits
        // replacement of collections; player-global / kit-scoped counts
        // live on DesiredCountWorkflowDomainService now.
    }

    @Test
    void toggleCollectionMembershipRejectsUnknownCollectionIds() {
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        CollectionWorkflowDomainService workflow = new CollectionWorkflowDomainService(repository);
        ItemIdentity identity = ItemIdentity.of("minecraft:stone");

        assertFalse(workflow.toggleCollectionMembership(identity, "missing"));
        assertFalse(repository.workflowProjection().memberships().containsKey(identity));
    }

    @Test
    void junkMarksExpireAfterThirtyMinutes() {
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        CollectionWorkflowDomainService workflow = new CollectionWorkflowDomainService(repository);
        ItemIdentity identity = ItemIdentity.of("minecraft:cobblestone");

        assertTrue(workflow.setJunk(identity, true));
        assertTrue(repository.workflowProjection().junkTags().contains(identity));
        long markedAt = repository.workflowEvents().records().get(0).envelope().occurredAtEpochMillis();

        assertFalse(workflow.expireJunkTags(markedAt + CollectionWorkflowDomainService.JUNK_MARK_TTL_MILLIS - 1));
        assertTrue(repository.workflowProjection().junkTags().contains(identity));
        assertTrue(workflow.expireJunkTags(markedAt + CollectionWorkflowDomainService.JUNK_MARK_TTL_MILLIS));
        assertFalse(repository.workflowProjection().junkTags().contains(identity));
    }

    @Test
    void updateAndDeleteSelectedLoadoutStayAnchoredToTypedTargets() {
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        CollectionWorkflowDomainService workflow = new CollectionWorkflowDomainService(repository);
        CollectionDefinition collection = workflow.createCollection("Combat");

        QuickAccessLoadoutDefinition created = workflow.createLoadout(
                collection.id(),
                "PvE",
                Set.of(new QuickAccessLoadoutEntry(
                        new LoadoutTarget.QuickAccessLaneTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, 1),
                        ItemIdentity.of("minecraft:bow")
                ))
        );

        QuickAccessLoadoutDefinition updated = workflow.updateSelectedLoadout(
                collection.id(),
                Set.of(new QuickAccessLoadoutEntry(
                        new LoadoutTarget.EquipmentSlotTarget(BuiltinInventoryIds.EQUIPMENT_GROUP_OFFHAND, 0),
                        ItemIdentity.of("minecraft:torch")
                ))
        );

        assertNotNull(updated);
        assertEquals(created.id(), updated.id());
        assertEquals(
                "equipment:" + BuiltinInventoryIds.EQUIPMENT_GROUP_OFFHAND + "#0",
                updated.entries().iterator().next().target().stableKey()
        );

        assertTrue(workflow.deleteSelectedLoadout(collection.id()));
        assertFalse(workflow.collectionHasLoadouts(collection.id()));
        assertEquals(0, repository.workflowProjection().collections().loadoutsByCollection().getOrDefault(collection.id(), java.util.List.of()).size());
        assertEquals("", repository.browseSessionState().current().selectedLoadoutId());
    }

    @Test
    void captureAndApplyPlanningStayInsideWorkflowDomain() {
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        CollectionDefinition collection = new CollectionWorkflowDomainService(repository).createCollection("Utility");
        int[] mutations = new int[1];
        CollectionWorkflowDomainService workflow = new CollectionWorkflowDomainService(repository, () -> mutations[0]++);

        InventoryHostDescriptor host = host(true);
        var capturedAuthority = InventoryAuthorityFixtures.authority(
                host,
                Map.of(
                        BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0, java.util.List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:torch", 32, 64), 32)),
                        BuiltinInventoryIds.PLAYER_OFFHAND, java.util.List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:shield", 1, 1), 1))
                ),
                Map.of(
                        BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0, 9,
                        BuiltinInventoryIds.PLAYER_OFFHAND, 1
                )
        );

        QuickAccessLoadoutDefinition created = workflow.captureNewLoadout(
                collection.id(),
                "Field",
                capturedAuthority,
                snapshot -> ItemIdentity.of(snapshot.stack().itemId())
        );

        var applyAuthority = InventoryAuthorityFixtures.authority(
                host,
                Map.of(
                        BuiltinInventoryIds.PLAYER_MAIN, java.util.List.of(
                                new InventoryStackSnapshot(4, new ItemStack("minecraft:torch", 16, 64), 16)
                        ),
                        BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0, java.util.List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:dirt", 8, 64), 8))
                ),
                Map.of(
                        BuiltinInventoryIds.PLAYER_MAIN, 27,
                        BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0, 9,
                        BuiltinInventoryIds.PLAYER_OFFHAND, 1
                )
        );

        LoadoutApplyService.LoadoutApplyPlan plan = workflow.planSelectedLoadoutApply(
                collection.id(),
                applyAuthority,
                ProtectionPolicy.allowAll(),
                snapshot -> ItemIdentity.of(snapshot.stack().itemId())
        );

        assertEquals(created.id(), workflow.selectedLoadout(collection.id()).id());
        assertEquals(1, mutations[0]);
        assertEquals(3, plan.requests().size());
        assertEquals(0, plan.missingTargets().size());
        assertEquals(created.id(), repository.browseSessionState().current().selectedLoadoutId());
        assertEquals(
                java.util.Set.of(InventoryActionKind.TRANSFER, InventoryActionKind.ASSIGN),
                plan.requests().stream().map(request -> request.kind()).collect(java.util.stream.Collectors.toSet())
        );
        java.util.Map<String, String> sourceByTarget = plan.requests().stream().collect(java.util.stream.Collectors.toMap(
                request -> request.secondaryTarget().stableKey(),
                request -> request.primaryTarget().stableKey()
        ));
        assertEquals("source:" + BuiltinInventoryIds.PLAYER_MAIN + "#4", sourceByTarget.get("quick_access:" + BuiltinInventoryIds.QUICK_ACCESS_LANE_0 + "#0"));
        assertEquals("source:carried.backpack#1", sourceByTarget.get("equipment:" + BuiltinInventoryIds.EQUIPMENT_GROUP_OFFHAND + "#0"));
        assertTrue(plan.operations().stream().anyMatch(operation ->
                operation.target().stableKey().equals("quick_access:" + BuiltinInventoryIds.QUICK_ACCESS_LANE_0 + "#0")
                        && operation.requests().size() == 2
                        && operation.rollbackRequest() != null
        ));

        LoadoutApplyResult result = workflow.executeSelectedLoadoutApply(
                collection.id(),
                applyAuthority,
                ProtectionPolicy.allowAll(),
                snapshot -> ItemIdentity.of(snapshot.stack().itemId()),
                request -> new InventoryActionOutcome(
                        host.hostId(),
                        host.serverMenuRef(),
                        request.requestId(),
                        request.kind(),
                        InventoryActionMode.EXECUTE,
                        request.quantity(),
                        request.scope(),
                        request.conflictPolicy(),
                        request.origin(),
                        request.primaryTarget(),
                        request.secondaryTarget(),
                        InventoryActionStatus.SUCCESS,
                        java.util.List.of(),
                        request.requestedCount(),
                        request.requestedCount(),
                        false,
                        request.secondaryTarget() instanceof InventoryActionTarget.EquipmentTarget
                                ? java.util.List.of()
                                : java.util.List.of(new InventoryActivityEvent(
                                InventoryActivityKind.ACQUIRED,
                                InventoryActivityProducer.EXTERNAL_WITHDRAWAL,
                                InventoryActivityConfidence.AUTHORITATIVE,
                                request.identity(),
                                request.requestedCount(),
                                request.primaryTarget(),
                                request.secondaryTarget(),
                                request.requestId(),
                                "",
                                java.util.List.of(),
                                ""
                        )),
                        ItemStack.EMPTY,
                        ""
                )
        );

        assertEquals(2, result.requestedTargets().size());
        assertEquals(2, result.satisfiedTargets().size());
        assertEquals(0, result.missingTargets().size());
        assertEquals(2, result.outcomesByTarget().size());
    }

    @Test
    void loadoutPlanningRespectsProtectionAndTreatsOffhandAsEquipmentOnly() {
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        CollectionWorkflowDomainService workflow = new CollectionWorkflowDomainService(repository);
        CollectionDefinition collection = workflow.createCollection("Protected");

        InventoryHostDescriptor host = host(false);

        workflow.createLoadout(
                collection.id(),
                "Blocked",
                Set.of(
                        new QuickAccessLoadoutEntry(
                                new LoadoutTarget.QuickAccessLaneTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, 0),
                                ItemIdentity.of("minecraft:torch")
                        ),
                        new QuickAccessLoadoutEntry(
                                new LoadoutTarget.EquipmentSlotTarget(BuiltinInventoryIds.EQUIPMENT_GROUP_OFFHAND, 0),
                                ItemIdentity.of("minecraft:shield")
                        )
                )
        );

        var applyAuthority = InventoryAuthorityFixtures.authority(
                host,
                Map.of(
                        BuiltinInventoryIds.PLAYER_MAIN, java.util.List.of(
                                new InventoryStackSnapshot(4, new ItemStack("minecraft:torch", 16, 64), 16),
                                new InventoryStackSnapshot(7, new ItemStack("minecraft:shield", 1, 1), 1)
                        )
                ),
                Map.of(
                        BuiltinInventoryIds.PLAYER_MAIN, 27,
                        BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0, 9,
                        BuiltinInventoryIds.PLAYER_OFFHAND, 1
                )
        );

        ProtectionSnapshotPolicy protection = new ProtectionSnapshotPolicy(
                Set.of(ItemIdentity.of("minecraft:shield")),
                Set.of(new dev.imagio.slot.inventory.action.InventoryActionTarget.QuickAccessTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, 0)),
                false
        );

        LoadoutApplyService.LoadoutApplyPlan plan = workflow.planSelectedLoadoutApply(
                collection.id(),
                applyAuthority,
                protection,
                snapshot -> ItemIdentity.of(snapshot.stack().itemId())
        );

        assertEquals(0, plan.requests().size());
        assertEquals(
                Set.of(
                        new LoadoutTarget.QuickAccessLaneTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, 0),
                        new LoadoutTarget.EquipmentSlotTarget(BuiltinInventoryIds.EQUIPMENT_GROUP_OFFHAND, 0)
                ),
                Set.copyOf(plan.missingTargets())
        );
        assertTrue(plan.diagnostics().stream().anyMatch(diagnostic -> diagnostic.contains("quick_access")));
        assertTrue(plan.diagnostics().stream().anyMatch(diagnostic -> diagnostic.contains("equipment")));
    }

    private static InventoryHostDescriptor host(boolean includeBackpackProvider) {
        TestMenu menu = new TestMenu();
        java.util.List<InventorySourceDescriptor> sources = new java.util.ArrayList<>(java.util.List.of(
                BuiltinInventoryDescriptors.playerMain(InventoryTopologyDescriptor.empty()),
                BuiltinInventoryDescriptors.quickAccessLane0Source(InventoryTopologyDescriptor.empty()),
                BuiltinInventoryDescriptors.armorSource(InventoryTopologyDescriptor.empty()),
                BuiltinInventoryDescriptors.offhandSource(InventoryTopologyDescriptor.empty())
        ));
        InventoryHostSession session = InventoryHostSession.empty();
        if (includeBackpackProvider) {
            InventorySourceDescriptor backpack = InventorySourceDescriptor.builder("carried.backpack")
                    .label(Component.literal("Backpack"))
                    .domain(dev.imagio.slot.inventory.core.InventorySourceDomain.HOST_STORAGE)
                    .role(dev.imagio.slot.inventory.core.InventorySourceRole.PROVIDER_DEFINED)
                    .logicalSlotCount(4)
                    .bindingRoute(dev.imagio.slot.inventory.core.InventoryBindingRoute.PROVIDER)
                    .capabilities(java.util.Set.of(
                            dev.imagio.slot.inventory.core.InventoryCapability.INSERT,
                            dev.imagio.slot.inventory.core.InventoryCapability.EXTRACT
                    ))
                    .actionRoute(dev.imagio.slot.inventory.core.InventoryActionRoute.PROVIDER_MUTATION)
                    .paneMembership(dev.imagio.slot.inventory.core.InventoryPaneMembership.CARRIED)
                    .build();
            sources.add(backpack);
            session = new InventoryHostSession() {
                @Override
                public String providerId() {
                    return "test.host";
                }

                @Override
                public java.util.List<dev.imagio.slot.inventory.core.InventorySourceDescriptor> hostSources() {
                    return java.util.List.of(backpack);
                }

                @Override
                public java.util.List<InventoryStackSnapshot> readSnapshots(InventoryHostDescriptor host, String sourceId) {
                    if (!"carried.backpack".equals(sourceId)) {
                        return java.util.List.of();
                    }
                    return java.util.List.of(new InventoryStackSnapshot(1, new ItemStack("minecraft:shield", 1, 1), 1));
                }
            };
        }

        return new InventoryHostDescriptor(
                new HostInstanceKey(TestMenu.class.getName(), includeBackpackProvider ? 0 : 1, "test.host", ""),
                InventoryHostDescriptor.serverMenuRef(menu),
                "test.screen",
                Component.literal("Test"),
                menu,
                InventoryTopologyDescriptor.empty(),
                session,
                java.util.List.of(),
                PlayerRuntimeStateDescriptor.vanilla(0),
                java.util.List.copyOf(sources),
                BuiltinInventoryDescriptors.builtInQuickAccessLanes(),
                BuiltinInventoryDescriptors.builtInEquipmentGroups(),
                java.util.List.of(),
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
