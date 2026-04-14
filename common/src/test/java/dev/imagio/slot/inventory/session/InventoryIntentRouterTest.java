package dev.imagio.slot.inventory.session;

import dev.imagio.slot.inventory.action.InventoryActionMode;
import dev.imagio.slot.inventory.action.InventoryActionOutcome;
import dev.imagio.slot.inventory.action.InventoryActionRequest;
import dev.imagio.slot.inventory.action.InventoryActionScope;
import dev.imagio.slot.inventory.action.InventoryActionStatus;
import dev.imagio.slot.inventory.action.InventoryCommandId;
import dev.imagio.slot.inventory.action.InventoryCommandReasonCode;
import dev.imagio.slot.inventory.browse.HeuristicInventoryCategoryResolver;
import dev.imagio.slot.inventory.browse.InventoryBrowseDocument;
import dev.imagio.slot.inventory.browse.InventoryBrowseDocumentQueries;
import dev.imagio.slot.inventory.browse.InventoryBrowseFilter;
import dev.imagio.slot.inventory.browse.InventoryBrowseFilterScope;
import dev.imagio.slot.inventory.browse.InventoryBrowseGroupingMode;
import dev.imagio.slot.inventory.browse.InventoryBrowsePaneMode;
import dev.imagio.slot.inventory.browse.InventoryBrowseSubjectRef;
import dev.imagio.slot.inventory.browse.InventoryCategoryOverrides;
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
import dev.imagio.slot.inventory.integration.InventoryHostSession;
import dev.imagio.slot.inventory.intent.InventoryBrowseIntent;
import dev.imagio.slot.testsupport.InventoryAuthorityFixtures;
import dev.imagio.slot.workflow.domain.CollectionDefinition;
import dev.imagio.slot.workflow.domain.InMemoryWorkflowDomainStateRepository;
import dev.imagio.slot.workflow.domain.InventoryActivityConfidence;
import dev.imagio.slot.workflow.domain.InventoryActivityEvent;
import dev.imagio.slot.workflow.domain.InventoryActivityKind;
import dev.imagio.slot.workflow.domain.InventoryActivityProducer;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryIntentRouterTest {
    @Test
    void sameItemIdWithDifferentExactIdentitiesProduceDistinctStableRowRefsAndSelectionTargets() {
        TestContext context = context(Map.of(
                BuiltinInventoryIds.PLAYER_MAIN, List.of(
                        new InventoryStackSnapshot(0, new ItemStack("minecraft:potion", "healing", 1, 1), 1),
                        new InventoryStackSnapshot(1, new ItemStack("minecraft:potion", "swiftness", 1, 1), 1)
                )
        ));

        List<dev.imagio.slot.inventory.browse.InventoryBrowseEntry.ItemEntry> itemEntries =
                InventoryBrowseDocumentQueries.itemEntries(context.coordinator.snapshot().browseDocument());

        assertEquals(2, itemEntries.size());
        assertNotEquals(itemEntries.get(0).subjectRef().stableKey(), itemEntries.get(1).subjectRef().stableKey());

        InventoryIntentRoutingResult result = context.router.route(
                context.coordinator.sessionToken(),
                new InventoryBrowseIntent.SelectSubject(itemEntries.getFirst().subjectRef(), "test.select")
        );

        assertEquals(InventoryRoutingStatus.APPLIED, result.status());
        assertEquals(
                itemEntries.getFirst().subjectRef(),
                context.coordinator.snapshot().workflow().browseSessionState().selectedSubject()
        );
    }

    @Test
    void staleCommandInvocationsAreRejectedAfterRevisionChange() {
        TestContext context = context(Map.of(
                BuiltinInventoryIds.PLAYER_MAIN, List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:torch", 1, 64), 1)),
                "external.chest", List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:torch", 8, 64), 8))
        ));

        dev.imagio.slot.inventory.browse.InventoryBrowseEntry.ItemEntry externalTorch = itemEntry(
                context.coordinator.snapshot().browseDocument(),
                InventoryPaneMembership.EXTERNAL,
                "minecraft:torch"
        );
        InventoryCommandInvocation invocation = new InventoryCommandInvocation(
                context.coordinator.sessionToken(),
                externalTorch.subjectRef(),
                InventoryCommandId.TRANSFER_ONE,
                InventoryActionMode.EXECUTE,
                "test.stale"
        );

        context.coordinator.publishCurrent("revision.bump");

        InventoryIntentRoutingResult result = context.router.route(invocation);

        assertEquals(InventoryRoutingStatus.STALE, result.status());
        assertTrue(result.reasonCodes().contains(InventoryCommandReasonCode.INVALID_INTENT));
    }

    @Test
    void outcomeIngestionAndExternalInvalidationRebuildBrowseStateAndRecent() {
        TestContext context = context(Map.of(
                BuiltinInventoryIds.PLAYER_MAIN, List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:torch", 1, 64), 1)),
                "external.chest", List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:torch", 5, 64), 5))
        ));

        dev.imagio.slot.inventory.browse.InventoryBrowseEntry.ItemEntry externalTorch = itemEntry(
                context.coordinator.snapshot().browseDocument(),
                InventoryPaneMembership.EXTERNAL,
                "minecraft:torch"
        );
        InventoryCommandInvocation invocation = new InventoryCommandInvocation(
                context.coordinator.sessionToken(),
                externalTorch.subjectRef(),
                InventoryCommandId.TRANSFER_ONE,
                InventoryActionMode.EXECUTE,
                "test.transfer"
        );

        InventoryIntentRoutingResult dispatchResult = context.router.route(invocation);
        InventoryActionRequest dispatchedRequest = context.dispatcher.requests.getFirst();
        assertEquals(InventoryRoutingStatus.DISPATCHED, dispatchResult.status());
        assertEquals(1, context.coordinator.snapshot().pendingActions().size());

        context.source.setSnapshots(Map.of(
                BuiltinInventoryIds.PLAYER_MAIN, List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:torch", 2, 64), 2)),
                "external.chest", List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:torch", 4, 64), 4))
        ));

        context.coordinator.ingestOutcome(successfulOutcome(dispatchedRequest, ItemIdentity.of("minecraft:torch"), 1));

        assertTrue(context.coordinator.snapshot().pendingActions().isEmpty());
        assertEquals(
                2,
                itemEntry(context.coordinator.snapshot().browseDocument(), InventoryPaneMembership.CARRIED, "minecraft:torch")
                        .row()
                        .visibleTotalCount()
        );
        assertEquals(
                List.of(ItemIdentity.of("minecraft:torch")),
                context.runtime.activityProjection().recents().visibleItems()
        );

        context.source.setSnapshots(Map.of(
                BuiltinInventoryIds.PLAYER_MAIN, List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:torch", 4, 64), 4)),
                "external.chest", List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:torch", 4, 64), 4))
        ));

        context.coordinator.invalidate("external.pickup");

        assertEquals(
                4,
                itemEntry(context.coordinator.snapshot().browseDocument(), InventoryPaneMembership.CARRIED, "minecraft:torch")
                        .row()
                        .visibleTotalCount()
        );
        assertEquals(
                2,
                context.runtime.activityProjection().recents().countsByIdentity().get(ItemIdentity.of("minecraft:torch"))
        );
    }

    @Test
    void correlationAndSessionMetadataFlowIntoWorkflowAndActivityEnvelopes() {
        TestContext context = context(Map.of(
                BuiltinInventoryIds.PLAYER_MAIN, List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:torch", 1, 64), 1)),
                "external.chest", List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:torch", 5, 64), 5))
        ));

        InventorySessionToken initialToken = context.coordinator.sessionToken();
        InventoryIntentRoutingResult favoriteResult = context.router.route(
                initialToken,
                new dev.imagio.slot.inventory.intent.InventoryWorkflowIntent.ToggleFavorite(ItemIdentity.of("minecraft:torch"), "test.favorite")
        );

        assertEquals(InventoryRoutingStatus.APPLIED, favoriteResult.status());
        var workflowEnvelope = context.repository.workflowEvents().snapshot().records().getLast().envelope();
        assertEquals(initialToken.sessionId(), workflowEnvelope.sessionId());
        assertFalse(workflowEnvelope.correlationId().isBlank());
        assertEquals(initialToken.sessionId() + ":" + initialToken.revision(), workflowEnvelope.causationId());

        dev.imagio.slot.inventory.browse.InventoryBrowseEntry.ItemEntry externalTorch = itemEntry(
                context.coordinator.snapshot().browseDocument(),
                InventoryPaneMembership.EXTERNAL,
                "minecraft:torch"
        );
        InventoryIntentRoutingResult dispatchResult = context.router.route(new InventoryCommandInvocation(
                context.coordinator.sessionToken(),
                externalTorch.subjectRef(),
                InventoryCommandId.TRANSFER_ONE,
                InventoryActionMode.EXECUTE,
                "test.transfer"
        ));
        assertEquals(InventoryRoutingStatus.DISPATCHED, dispatchResult.status());
        InventoryActionRequest dispatchedRequest = context.dispatcher.requests.getLast();

        context.source.setSnapshots(Map.of(
                BuiltinInventoryIds.PLAYER_MAIN, List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:torch", 2, 64), 2)),
                "external.chest", List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:torch", 4, 64), 4))
        ));
        context.coordinator.ingestOutcome(successfulOutcome(dispatchedRequest, ItemIdentity.of("minecraft:torch"), 1));

        var activityEnvelope = context.repository.activityEvents().snapshot().records().getLast().envelope();
        assertEquals(dispatchedRequest.sessionId(), activityEnvelope.sessionId());
        assertEquals(dispatchedRequest.correlationId(), activityEnvelope.correlationId());
        assertEquals(dispatchedRequest.causationId(), activityEnvelope.causationId());
    }

    @Test
    void commandPreflightAvailabilityMatchesRouterExecutionAndPendingConflicts() {
        TestContext context = context(Map.of(
                BuiltinInventoryIds.PLAYER_MAIN, List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:torch", 1, 64), 1)),
                "external.chest", List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:torch", 5, 64), 5))
        ));

        dev.imagio.slot.inventory.browse.InventoryBrowseEntry.ItemEntry externalTorch = itemEntry(
                context.coordinator.snapshot().browseDocument(),
                InventoryPaneMembership.EXTERNAL,
                "minecraft:torch"
        );
        InventoryCommandInvocation transferInvocation = new InventoryCommandInvocation(
                context.coordinator.sessionToken(),
                externalTorch.subjectRef(),
                InventoryCommandId.TRANSFER_ONE,
                InventoryActionMode.EXECUTE,
                "test.preflight"
        );

        InventoryCommandPreflight transferPreflight = context.router.preflight(transferInvocation);
        InventoryIntentRoutingResult transferResult = context.router.route(transferInvocation);

        assertTrue(transferPreflight.availability().available());
        assertEquals(InventoryRoutingStatus.DISPATCHED, transferResult.status());

        InventoryIntentRoutingResult conflicted = context.router.route(new InventoryCommandInvocation(
                context.coordinator.sessionToken(),
                externalTorch.subjectRef(),
                InventoryCommandId.TRANSFER_ONE,
                InventoryActionMode.EXECUTE,
                "test.conflict"
        ));
        assertEquals(InventoryRoutingStatus.REJECTED, conflicted.status());
        assertTrue(conflicted.reasonCodes().contains(InventoryCommandReasonCode.INVALID_INTENT));

        InventoryCommandInvocation trashInvocation = new InventoryCommandInvocation(
                context.coordinator.sessionToken(),
                itemEntry(context.coordinator.snapshot().browseDocument(), InventoryPaneMembership.CARRIED, "minecraft:torch").subjectRef(),
                InventoryCommandId.TRASH,
                InventoryActionMode.EXECUTE,
                "test.trash"
        );
        InventoryCommandPreflight trashPreflight = context.router.preflight(trashInvocation);
        InventoryIntentRoutingResult trashResult = context.router.route(trashInvocation);

        assertFalse(trashPreflight.availability().available());
        assertEquals(InventoryRoutingStatus.REJECTED, trashResult.status());
        assertEquals(trashPreflight.availability().reasonCodes(), trashResult.reasonCodes());
    }

    private static InventoryActionOutcome successfulOutcome(
            InventoryActionRequest request,
            ItemIdentity identity,
            int count
    ) {
        return new InventoryActionOutcome(
                request.hostId(),
                request.serverMenuRef(),
                request.requestId(),
                request.kind(),
                request.mode(),
                request.origin(),
                request.correlationId(),
                request.causationId(),
                request.sessionId(),
                request.primaryTarget(),
                request.secondaryTarget(),
                InventoryActionStatus.SUCCESS,
                List.of(),
                count,
                count,
                false,
                List.of(new InventoryActivityEvent(
                        InventoryActivityKind.ACQUIRED,
                        InventoryActivityProducer.EXTERNAL_WITHDRAWAL,
                        InventoryActivityConfidence.AUTHORITATIVE,
                        identity,
                        count,
                        request.primaryTarget(),
                        request.secondaryTarget(),
                        request.requestId(),
                        "",
                        List.of(),
                        ""
                )),
                ItemStack.EMPTY,
                ""
        );
    }

    private static dev.imagio.slot.inventory.browse.InventoryBrowseEntry.ItemEntry itemEntry(
            InventoryBrowseDocument document,
            InventoryPaneMembership paneMembership,
            String itemId
    ) {
        return InventoryBrowseDocumentQueries.itemEntries(
                InventoryBrowseDocumentQueries.findPane(document, new InventoryBrowseSubjectRef.PaneRef(paneMembership))
        ).stream()
                .filter(entry -> entry.row().identity() != null && itemId.equals(entry.row().identity().itemId()))
                .findFirst()
                .orElseThrow();
    }

    private static TestContext context(Map<String, List<InventoryStackSnapshot>> snapshotsBySourceId) {
        InventoryHostDescriptor host = host();
        MutableSessionSource source = new MutableSessionSource(host, snapshotsBySourceId);
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(repository, null);
        repository.browseSessionState().replaceWith(new dev.imagio.slot.inventory.browse.InventoryBrowseSessionState(
                new InventoryBrowseFilter("", InventoryBrowseFilterScope.ALL),
                dev.imagio.slot.inventory.browse.InventoryBrowseSortMode.NAME,
                InventoryBrowseGroupingMode.FLAT,
                InventoryBrowsePaneMode.DUAL_PANE,
                InventoryPaneMembership.CARRIED,
                "",
                "",
                "",
                InventoryActionScope.VISIBLE_MATCHES,
                null,
                Set.of()
        ));
        CollectionDefinition collection = runtime.collectionWorkflow().createCollection("Tracked");
        runtime.collectionWorkflow().toggleCollectionMembership(ItemIdentity.of("minecraft:torch"), collection.id());
        RecordingDispatcher dispatcher = new RecordingDispatcher();
        InventorySessionCoordinator coordinator = new InventorySessionCoordinator(
                source,
                runtime,
                dispatcher,
                stackIdentity(),
                new HeuristicInventoryCategoryResolver(InventoryCategoryOverrides.empty())
        );
        InventoryIntentRouter router = new InventoryIntentRouter(coordinator);
        return new TestContext(source, repository, runtime, dispatcher, coordinator, router);
    }

    private static Function<dev.imagio.slot.inventory.query.InventoryEntrySnapshot, ItemIdentity> stackIdentity() {
        return entry -> {
            if (entry == null || !entry.present() || entry.stack() == null || entry.stack().isEmpty()) {
                return null;
            }
            return entry.stack().componentFingerprint().isBlank()
                    ? ItemIdentity.of(entry.stack().itemId())
                    : ItemIdentity.exact(entry.stack().itemId(), entry.stack().componentFingerprint());
        };
    }

    private static InventoryHostDescriptor host() {
        TestMenu menu = new TestMenu();
        return new InventoryHostDescriptor(
                new HostInstanceKey(TestMenu.class.getName(), 0, "router.test", ""),
                InventoryHostDescriptor.serverMenuRef(menu),
                "router.test",
                Component.literal("Router Test"),
                menu,
                InventoryTopologyDescriptor.empty(),
                InventoryHostSession.empty(),
                List.of(),
                PlayerRuntimeStateDescriptor.vanilla(0),
                List.of(
                        BuiltinInventoryDescriptors.playerMain(InventoryTopologyDescriptor.empty()),
                        BuiltinInventoryDescriptors.quickAccessLane0Source(InventoryTopologyDescriptor.empty()),
                        BuiltinInventoryDescriptors.armorSource(InventoryTopologyDescriptor.empty()),
                        BuiltinInventoryDescriptors.offhandSource(InventoryTopologyDescriptor.empty()),
                        carriedSource("carried.backpack.one", 40),
                        externalSource("external.chest", 100)
                ),
                BuiltinInventoryDescriptors.builtInQuickAccessLanes(),
                BuiltinInventoryDescriptors.builtInEquipmentGroups(),
                List.of(),
                false,
                true,
                false,
                ""
        );
    }

    private static InventorySourceDescriptor carriedSource(String sourceId, int stableOrder) {
        return InventorySourceDescriptor.builder(sourceId)
                .label(Component.literal(sourceId))
                .domain(InventorySourceDomain.PLAYER_EXTENSION)
                .role(InventorySourceRole.PROVIDER_DEFINED)
                .logicalSlotCount(9)
                .bindingRoute(InventoryBindingRoute.PROVIDER)
                .capabilities(Set.of(InventoryCapability.INSERT, InventoryCapability.EXTRACT))
                .actionRoute(InventoryActionRoute.PROVIDER_MUTATION)
                .paneMembership(InventoryPaneMembership.CARRIED)
                .stableOrder(stableOrder)
                .build();
    }

    private static InventorySourceDescriptor externalSource(String sourceId, int stableOrder) {
        return InventorySourceDescriptor.builder(sourceId)
                .label(Component.literal(sourceId))
                .domain(InventorySourceDomain.HOST_STORAGE)
                .role(InventorySourceRole.PRIMARY_STORAGE)
                .logicalSlotCount(9)
                .bindingRoute(InventoryBindingRoute.PROVIDER)
                .capabilities(Set.of(InventoryCapability.INSERT, InventoryCapability.EXTRACT))
                .actionRoute(InventoryActionRoute.PROVIDER_MUTATION)
                .paneMembership(InventoryPaneMembership.EXTERNAL)
                .stableOrder(stableOrder)
                .build();
    }

    private record TestContext(
            MutableSessionSource source,
            InMemoryWorkflowDomainStateRepository repository,
            WorkflowDomainRuntime runtime,
            RecordingDispatcher dispatcher,
            InventorySessionCoordinator coordinator,
            InventoryIntentRouter router
    ) {
    }

    private static final class MutableSessionSource implements InventorySessionSource {
        private final InventoryHostDescriptor host;
        private Map<String, List<InventoryStackSnapshot>> snapshotsBySourceId;
        private final Map<String, Integer> capacitiesBySourceId = new LinkedHashMap<>();

        private MutableSessionSource(
                InventoryHostDescriptor host,
                Map<String, List<InventoryStackSnapshot>> snapshotsBySourceId
        ) {
            this.host = host;
            this.snapshotsBySourceId = snapshotsBySourceId == null ? Map.of() : snapshotsBySourceId;
            capacitiesBySourceId.put(BuiltinInventoryIds.PLAYER_MAIN, 27);
            capacitiesBySourceId.put("carried.backpack.one", 9);
            capacitiesBySourceId.put("external.chest", 9);
        }

        @Override
        public InventoryHostDescriptor resolveHost() {
            return host;
        }

        @Override
        public dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot readAuthority(InventoryHostDescriptor resolvedHost) {
            return InventoryAuthorityFixtures.authority(resolvedHost, snapshotsBySourceId, capacitiesBySourceId);
        }

        private void setSnapshots(Map<String, List<InventoryStackSnapshot>> snapshotsBySourceId) {
            this.snapshotsBySourceId = snapshotsBySourceId == null ? Map.of() : snapshotsBySourceId;
        }
    }

    private static final class RecordingDispatcher implements InventoryActionDispatcher {
        private final ArrayList<InventoryActionRequest> requests = new ArrayList<>();

        @Override
        public void dispatch(InventoryActionRequest request) {
            if (request != null) {
                requests.add(request);
            }
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
