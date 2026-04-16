package dev.imagio.slot.inventory.session;

import dev.imagio.slot.inventory.action.InventoryActionKind;
import dev.imagio.slot.inventory.action.InventoryActionMode;
import dev.imagio.slot.inventory.action.InventoryActionOutcome;
import dev.imagio.slot.inventory.action.InventoryActionQuantity;
import dev.imagio.slot.inventory.action.InventoryActionRequest;
import dev.imagio.slot.inventory.action.InventoryActionScope;
import dev.imagio.slot.inventory.action.InventoryActionStatus;
import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.action.InventoryCommandId;
import dev.imagio.slot.inventory.action.InventoryCommandReasonCode;
import dev.imagio.slot.inventory.browse.InventoryBrowseDocument;
import dev.imagio.slot.inventory.browse.InventoryBrowseDocumentQueries;
import dev.imagio.slot.inventory.browse.InventoryBrowseFilter;
import dev.imagio.slot.inventory.browse.InventoryBrowseFilterScope;
import dev.imagio.slot.inventory.browse.InventoryBrowseGroupingMode;
import dev.imagio.slot.inventory.browse.InventoryBrowsePaneMode;
import dev.imagio.slot.inventory.browse.InventoryBrowseSubjectRef;
import dev.imagio.slot.inventory.core.BuiltinInventoryDescriptors;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.CraftingSurfaceDescriptor;
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
import dev.imagio.slot.inventory.core.InventoryToolAction;
import dev.imagio.slot.inventory.core.InventoryToolActionId;
import dev.imagio.slot.inventory.core.InventoryToolDescriptor;
import dev.imagio.slot.inventory.core.InventoryToolKind;
import dev.imagio.slot.inventory.core.InventoryToolToggle;
import dev.imagio.slot.inventory.core.InventoryToolToggleId;
import dev.imagio.slot.inventory.core.InventoryTopologyDescriptor;
import dev.imagio.slot.inventory.core.MenuCursorAccess;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.PlayerRuntimeStateDescriptor;
import dev.imagio.slot.inventory.core.ToolPresentationHints;
import dev.imagio.slot.inventory.core.ToolRegionDescriptor;
import dev.imagio.slot.inventory.core.ToolRegionRole;
import dev.imagio.slot.inventory.integration.InventoryHostSession;
import dev.imagio.slot.inventory.intent.CraftingDragMode;
import dev.imagio.slot.inventory.intent.CraftingPlacementMode;
import dev.imagio.slot.inventory.intent.InventoryBrowseIntent;
import dev.imagio.slot.inventory.intent.InventoryMutationIntent;
import dev.imagio.slot.inventory.query.InventoryAuthorityReadService;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntryKey;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.inventory.query.InventorySourceSnapshot;
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

    @Test
    void craftingPlaceSelectedUsesExactProviderEntryTarget() {
        TestContext context = craftingContext(
                Map.of(
                        BuiltinInventoryIds.PLAYER_MAIN, List.of(),
                        "craft.input.source", List.of(),
                        "craft.output.source", List.of()
                ),
                Map.of(
                        "external.provider",
                        providerEntrySnapshot(
                                "external.provider",
                                "entry:healing",
                                new ItemStack("minecraft:potion", "healing", 6, 64),
                                6
                        )
                ),
                ItemStack.EMPTY,
                3,
                true
        );

        dev.imagio.slot.inventory.browse.InventoryBrowseEntry.ItemEntry externalPotion = itemEntry(
                context.coordinator.snapshot().browseDocument(),
                InventoryPaneMembership.EXTERNAL,
                "minecraft:potion"
        );
        InventoryIntentRoutingResult selectResult = context.router.route(
                context.coordinator.sessionToken(),
                new InventoryBrowseIntent.SelectSubject(externalPotion.subjectRef(), "test.select.crafting")
        );
        assertEquals(InventoryRoutingStatus.APPLIED, selectResult.status());

        InventoryIntentRoutingResult result = context.router.route(
                context.coordinator.sessionToken(),
                new InventoryMutationIntent.CraftingPlaceSelected(
                        "tool.crafting",
                        0,
                        CraftingPlacementMode.STACK,
                        InventoryActionMode.EXECUTE,
                        "test.craft.selected"
                )
        );

        assertEquals(InventoryRoutingStatus.DISPATCHED, result.status());
        assertEquals(1, result.dispatchedRequests().size());
        InventoryActionRequest request = result.dispatchedRequests().getFirst();
        assertEquals(InventoryActionKind.TRANSFER, request.kind());
        assertEquals(InventoryActionQuantity.STACK, request.quantity());
        assertEquals(6, request.requestedCount());
        assertTrue(request.primaryTarget() instanceof InventoryActionTarget.SourceEntryTarget);
        InventoryActionTarget.SourceEntryTarget sourceEntryTarget =
                (InventoryActionTarget.SourceEntryTarget) request.primaryTarget();
        assertEquals("external.provider", sourceEntryTarget.sourceId());
        assertEquals("entry:healing", sourceEntryTarget.entryId());
        assertEquals(
                new InventoryActionTarget.ToolRegionTarget("tool.crafting", "tool.crafting/input", 0),
                request.secondaryTarget()
        );
    }

    @Test
    void staleCraftingIntentsAreRejectedAfterRevisionChange() {
        TestContext context = craftingContext(
                Map.of(
                        BuiltinInventoryIds.PLAYER_MAIN, List.of(
                                new InventoryStackSnapshot(0, new ItemStack("minecraft:oak_planks", 12, 64), 12)
                        ),
                        "craft.input.source", List.of(),
                        "craft.output.source", List.of()
                ),
                Map.of(),
                ItemStack.EMPTY,
                3,
                false
        );

        dev.imagio.slot.inventory.browse.InventoryBrowseEntry.ItemEntry planks = itemEntry(
                context.coordinator.snapshot().browseDocument(),
                InventoryPaneMembership.CARRIED,
                "minecraft:oak_planks"
        );
        InventoryIntentRoutingResult selectResult = context.router.route(
                context.coordinator.sessionToken(),
                new InventoryBrowseIntent.SelectSubject(planks.subjectRef(), "test.select.crafting")
        );
        assertEquals(InventoryRoutingStatus.APPLIED, selectResult.status());

        InventorySessionToken staleToken = context.coordinator.sessionToken();
        context.coordinator.publishCurrent("revision.bump");

        InventoryIntentRoutingResult result = context.router.route(
                staleToken,
                new InventoryMutationIntent.CraftingPlaceSelected(
                        "tool.crafting",
                        0,
                        CraftingPlacementMode.SINGLE,
                        InventoryActionMode.EXECUTE,
                        "test.craft.stale"
                )
        );

        assertEquals(InventoryRoutingStatus.STALE, result.status());
        assertTrue(result.reasonCodes().contains(InventoryCommandReasonCode.INVALID_INTENT));
    }

    @Test
    void craftingPlaceCursorSingleUsesRequestedCountOne() {
        TestContext context = craftingContext(
                Map.of(
                        BuiltinInventoryIds.PLAYER_MAIN, List.of(),
                        "craft.input.source", List.of(),
                        "craft.output.source", List.of()
                ),
                Map.of(),
                new ItemStack("minecraft:oak_planks", 5, 64),
                3,
                false
        );

        InventoryIntentRoutingResult result = context.router.route(
                context.coordinator.sessionToken(),
                new InventoryMutationIntent.CraftingPlaceCursor(
                        "tool.crafting",
                        1,
                        CraftingPlacementMode.SINGLE,
                        InventoryActionMode.EXECUTE,
                        "test.craft.cursor"
                )
        );

        assertEquals(InventoryRoutingStatus.DISPATCHED, result.status());
        InventoryActionRequest request = result.dispatchedRequests().getFirst();
        assertEquals(InventoryActionKind.CURSOR_PLACE, request.kind());
        assertEquals(InventoryActionQuantity.ONE, request.quantity());
        assertEquals(1, request.requestedCount());
        assertEquals(1, request.stack().getCount());
        assertEquals(
                new InventoryActionTarget.ToolRegionTarget("tool.crafting", "tool.crafting/input", 1),
                request.primaryTarget()
        );
    }

    @Test
    void craftingDragCursorSkipsFullTargetsAndPreservesOrderedEligibleTraversal() {
        TestContext context = craftingContext(
                Map.of(
                        BuiltinInventoryIds.PLAYER_MAIN, List.of(),
                        "craft.input.source", List.of(
                                new InventoryStackSnapshot(0, new ItemStack("minecraft:oak_planks", 63, 64), 63),
                                new InventoryStackSnapshot(2, new ItemStack("minecraft:cobblestone", 16, 64), 16),
                                new InventoryStackSnapshot(3, new ItemStack("minecraft:oak_planks", 64, 64), 64)
                        ),
                        "craft.output.source", List.of()
                ),
                Map.of(),
                new ItemStack("minecraft:oak_planks", 7, 64),
                3,
                false
        );

        InventoryIntentRoutingResult result = context.router.route(
                context.coordinator.sessionToken(),
                new InventoryMutationIntent.CraftingDragCursor(
                        "tool.crafting",
                        List.of(3, 2, 0, 1),
                        CraftingDragMode.SINGLE_PER_SLOT,
                        InventoryActionMode.EXECUTE,
                        "test.craft.drag"
                )
        );

        assertEquals(InventoryRoutingStatus.DISPATCHED, result.status());
        assertEquals(2, result.dispatchedRequests().size());
        assertEquals(
                List.of(
                        new InventoryActionTarget.ToolRegionTarget("tool.crafting", "tool.crafting/input", 0),
                        new InventoryActionTarget.ToolRegionTarget("tool.crafting", "tool.crafting/input", 1)
                ),
                result.dispatchedRequests().stream().map(InventoryActionRequest::primaryTarget).toList()
        );
        assertEquals(
                List.of(1, 1),
                result.dispatchedRequests().stream().map(InventoryActionRequest::requestedCount).toList()
        );
    }

    @Test
    void rotateGridIsRejectedOnTwoByTwoCraftingSurface() {
        TestContext context = craftingContext(
                Map.of(
                        BuiltinInventoryIds.PLAYER_MAIN, List.of(),
                        "craft.input.source", List.of(),
                        "craft.output.source", List.of()
                ),
                Map.of(),
                ItemStack.EMPTY,
                2,
                false
        );

        InventoryIntentRoutingResult result = context.router.route(
                context.coordinator.sessionToken(),
                new InventoryMutationIntent.ToolAction(
                        "tool.crafting",
                        InventoryToolActionId.ROTATE_GRID,
                        InventoryActionMode.EXECUTE,
                        "test.rotate.unsupported"
                )
        );

        assertEquals(InventoryRoutingStatus.REJECTED, result.status());
        assertTrue(result.reasonCodes().contains(InventoryCommandReasonCode.UNSUPPORTED));
    }

    @Test
    void autoRefillToggleRoutesWhenExposedByCraftingTool() {
        TestContext context = craftingContext(
                Map.of(
                        BuiltinInventoryIds.PLAYER_MAIN, List.of(),
                        "craft.input.source", List.of(),
                        "craft.output.source", List.of()
                ),
                Map.of(),
                ItemStack.EMPTY,
                3,
                true
        );

        InventoryIntentRoutingResult result = context.router.route(
                context.coordinator.sessionToken(),
                new InventoryMutationIntent.ToolToggle(
                        "tool.crafting",
                        InventoryToolToggleId.AUTO_REFILL,
                        true,
                        InventoryActionMode.EXECUTE,
                        "test.toggle.refill"
                )
        );

        assertEquals(InventoryRoutingStatus.DISPATCHED, result.status());
        assertEquals(InventoryActionKind.TOOL_TOGGLE, result.dispatchedRequests().getFirst().kind());
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
                request.quantity(),
                request.scope(),
                request.conflictPolicy(),
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
        return context(host(), snapshotsBySourceId, Map.of(), ItemStack.EMPTY);
    }

    private static TestContext craftingContext(
            Map<String, List<InventoryStackSnapshot>> slotSnapshotsBySourceId,
            Map<String, InventorySourceSnapshot> explicitSourceSnapshotsById,
            ItemStack cursorStack,
            int gridWidth,
            boolean includeAutoRefillToggle
    ) {
        return context(
                craftingHost(gridWidth, includeAutoRefillToggle),
                slotSnapshotsBySourceId,
                explicitSourceSnapshotsById,
                cursorStack
        );
    }

    private static TestContext context(
            InventoryHostDescriptor host,
            Map<String, List<InventoryStackSnapshot>> snapshotsBySourceId,
            Map<String, InventorySourceSnapshot> explicitSourceSnapshotsById,
            ItemStack cursorStack
    ) {
        MutableSessionSource source = new MutableSessionSource(host, snapshotsBySourceId);
        source.setExplicitSourceSnapshots(explicitSourceSnapshotsById);
        source.setCursorStack(cursorStack);
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
                stackIdentity()
        );
        InventoryIntentRouter router = new InventoryIntentRouter(coordinator);
        return new TestContext(source, repository, runtime, dispatcher, coordinator, router);
    }

    private static InventorySourceSnapshot providerEntrySnapshot(
            String sourceId,
            String entryId,
            ItemStack stack,
            int count
    ) {
        return new InventorySourceSnapshot(
                sourceId,
                0,
                List.of(new InventoryEntrySnapshot(
                        InventoryEntryKey.providerEntry(sourceId, entryId),
                        stack,
                        count,
                        ""
                )),
                ""
        );
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
                dev.imagio.slot.inventory.integration.InventoryHostObservationHints.defaults(),
                ""
        );
    }

    private static InventoryHostDescriptor craftingHost(int gridWidth, boolean includeAutoRefillToggle) {
        TestMenu menu = new TestMenu();
        String toolId = "tool.crafting";
        String inputRegionId = toolId + "/input";
        String outputRegionId = toolId + "/output";
        String inputSourceId = "craft.input.source";
        String outputSourceId = "craft.output.source";
        int inputCount = gridWidth == 2 ? 4 : 9;

        InventorySourceDescriptor inputSource = InventorySourceDescriptor.builder(inputSourceId)
                .label(Component.literal("Crafting Input"))
                .domain(InventorySourceDomain.HOST_STORAGE)
                .role(InventorySourceRole.PROVIDER_DEFINED)
                .logicalSlotCount(inputCount)
                .bindingRoute(InventoryBindingRoute.MENU)
                .capabilities(Set.of(InventoryCapability.INSERT, InventoryCapability.EXTRACT))
                .actionRoute(InventoryActionRoute.MENU_MUTATION)
                .paneMembership(InventoryPaneMembership.HIDDEN)
                .stableOrder(150)
                .build();
        InventorySourceDescriptor outputSource = InventorySourceDescriptor.builder(outputSourceId)
                .label(Component.literal("Crafting Output"))
                .domain(InventorySourceDomain.HOST_STORAGE)
                .role(InventorySourceRole.PROVIDER_DEFINED)
                .logicalSlotCount(1)
                .bindingRoute(InventoryBindingRoute.MENU)
                .capabilities(Set.of(InventoryCapability.EXTRACT))
                .actionRoute(InventoryActionRoute.MENU_MUTATION)
                .paneMembership(InventoryPaneMembership.HIDDEN)
                .stableOrder(151)
                .build();
        InventorySourceDescriptor providerExternal = InventorySourceDescriptor.builder("external.provider")
                .label(Component.literal("External Provider"))
                .domain(InventorySourceDomain.HOST_STORAGE)
                .role(InventorySourceRole.PRIMARY_STORAGE)
                .logicalSlotCount(0)
                .bindingRoute(InventoryBindingRoute.PROVIDER)
                .capabilities(Set.of(InventoryCapability.INSERT, InventoryCapability.EXTRACT))
                .actionRoute(InventoryActionRoute.PROVIDER_MUTATION)
                .paneMembership(InventoryPaneMembership.EXTERNAL)
                .stableOrder(100)
                .build();

        ToolRegionDescriptor inputRegion = new ToolRegionDescriptor(
                inputRegionId,
                ToolRegionRole.INPUT,
                inputCount,
                InventoryBindingRoute.MENU,
                Set.of(InventoryCapability.TOOL_REGION_MUTATION, InventoryCapability.INSERT, InventoryCapability.EXTRACT),
                true,
                inputSourceId,
                ""
        );
        ToolRegionDescriptor outputRegion = new ToolRegionDescriptor(
                outputRegionId,
                ToolRegionRole.OUTPUT,
                1,
                InventoryBindingRoute.MENU,
                Set.of(InventoryCapability.TOOL_REGION_MUTATION, InventoryCapability.EXTRACT),
                true,
                outputSourceId,
                ""
        );
        List<InventoryToolAction> actions = gridWidth == 2
                ? List.of(
                new InventoryToolAction("clear_grid", InventoryToolActionId.CLEAR_GRID, Component.literal("Clear"), Component.empty()),
                new InventoryToolAction("balance_grid", InventoryToolActionId.BALANCE_GRID, Component.literal("Balance"), Component.empty())
        )
                : List.of(
                new InventoryToolAction("clear_grid", InventoryToolActionId.CLEAR_GRID, Component.literal("Clear"), Component.empty()),
                new InventoryToolAction("balance_grid", InventoryToolActionId.BALANCE_GRID, Component.literal("Balance"), Component.empty()),
                new InventoryToolAction("rotate_grid", InventoryToolActionId.ROTATE_GRID, Component.literal("Rotate"), Component.empty())
        );
        List<InventoryToolToggle> toggles = includeAutoRefillToggle
                ? List.of(new InventoryToolToggle("auto_refill", InventoryToolToggleId.AUTO_REFILL, Component.literal("Auto Refill"), Component.empty()))
                : List.of();
        InventoryToolDescriptor craftingTool = new InventoryToolDescriptor(
                toolId,
                "test",
                InventoryToolKind.CRAFTING_GRID,
                Component.literal("Crafting"),
                new ToolPresentationHints("Crafting", 50, "docked", 60),
                50,
                true,
                true,
                true,
                null,
                List.of(inputRegion, outputRegion),
                actions,
                toggles,
                Map.of(InventoryToolToggleId.AUTO_REFILL, includeAutoRefillToggle),
                Map.of(),
                new CraftingSurfaceDescriptor(
                        java.util.stream.IntStream.range(0, inputCount)
                                .mapToObj(index -> new InventoryActionTarget.SourceSlotTarget(inputSourceId, index))
                                .toList(),
                        new InventoryActionTarget.SourceSlotTarget(outputSourceId, 0),
                        gridWidth,
                        gridWidth,
                        true,
                        true,
                        true,
                        gridWidth == 3,
                        ""
                ),
                ""
        );

        LinkedHashMap<String, List<Integer>> menuSlotsBySource = new LinkedHashMap<>();
        menuSlotsBySource.put(inputSourceId, java.util.stream.IntStream.range(0, inputCount).boxed().toList());
        menuSlotsBySource.put(outputSourceId, List.of(inputCount));
        LinkedHashMap<Integer, String> sourceByMenuSlot = new LinkedHashMap<>();
        for (int index = 0; index < inputCount; index++) {
            sourceByMenuSlot.put(index, inputSourceId);
        }
        sourceByMenuSlot.put(inputCount, outputSourceId);

        return new InventoryHostDescriptor(
                new HostInstanceKey(TestMenu.class.getName(), 0, "router.crafting", ""),
                InventoryHostDescriptor.serverMenuRef(menu),
                "router.crafting",
                Component.literal("Crafting Test"),
                menu,
                new InventoryTopologyDescriptor(
                        Map.copyOf(menuSlotsBySource),
                        Map.copyOf(sourceByMenuSlot),
                        Map.of(
                                inputRegionId, menuSlotsBySource.get(inputSourceId),
                                outputRegionId, menuSlotsBySource.get(outputSourceId)
                        )
                ),
                InventoryHostSession.empty(),
                List.of(),
                PlayerRuntimeStateDescriptor.vanilla(0),
                List.of(
                        BuiltinInventoryDescriptors.playerMain(InventoryTopologyDescriptor.empty()),
                        BuiltinInventoryDescriptors.quickAccessLane0Source(InventoryTopologyDescriptor.empty()),
                        BuiltinInventoryDescriptors.armorSource(InventoryTopologyDescriptor.empty()),
                        BuiltinInventoryDescriptors.offhandSource(InventoryTopologyDescriptor.empty()),
                        providerExternal,
                        inputSource,
                        outputSource
                ),
                BuiltinInventoryDescriptors.builtInQuickAccessLanes(),
                BuiltinInventoryDescriptors.builtInEquipmentGroups(),
                List.of(craftingTool),
                dev.imagio.slot.inventory.integration.InventoryHostObservationHints.defaults(),
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
        private Map<String, InventorySourceSnapshot> explicitSourceSnapshotsById = Map.of();
        private ItemStack cursorStack = ItemStack.EMPTY;

        private MutableSessionSource(
                InventoryHostDescriptor host,
                Map<String, List<InventoryStackSnapshot>> snapshotsBySourceId
        ) {
            this.host = host;
            this.snapshotsBySourceId = snapshotsBySourceId == null ? Map.of() : snapshotsBySourceId;
            capacitiesBySourceId.put(BuiltinInventoryIds.PLAYER_MAIN, 27);
            capacitiesBySourceId.put("carried.backpack.one", 9);
            capacitiesBySourceId.put("external.chest", 9);
            capacitiesBySourceId.put("external.provider", 0);
            capacitiesBySourceId.put("craft.input.source", 9);
            capacitiesBySourceId.put("craft.output.source", 1);
        }

        @Override
        public InventoryHostDescriptor resolveHost() {
            return host;
        }

        @Override
        public dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot readAuthority(InventoryHostDescriptor resolvedHost) {
            if (resolvedHost == null) {
                return InventoryAuthoritySnapshot.empty();
            }
            MenuCursorAccess.set(resolvedHost.menu(), cursorStack);
            if (explicitSourceSnapshotsById.isEmpty()) {
                return InventoryAuthorityFixtures.authority(resolvedHost, snapshotsBySourceId, capacitiesBySourceId);
            }

            LinkedHashMap<String, InventorySourceSnapshot> sourceSnapshots = new LinkedHashMap<>();
            for (InventorySourceDescriptor source : resolvedHost.sourceDescriptors()) {
                if (source == null) {
                    continue;
                }
                InventorySourceSnapshot snapshot = explicitSourceSnapshotsById.get(source.id());
                if (snapshot == null) {
                    snapshot = slotBackedSnapshot(source, snapshotsBySourceId.get(source.id()));
                }
                sourceSnapshots.put(source.id(), snapshot == null ? InventorySourceSnapshot.empty(source.id()) : snapshot);
            }
            return new InventoryAuthoritySnapshot(
                    resolvedHost,
                    Map.copyOf(sourceSnapshots),
                    InventoryAuthorityReadService.cursor(resolvedHost.menu())
            );
        }

        private void setSnapshots(Map<String, List<InventoryStackSnapshot>> snapshotsBySourceId) {
            this.snapshotsBySourceId = snapshotsBySourceId == null ? Map.of() : snapshotsBySourceId;
        }

        private void setExplicitSourceSnapshots(Map<String, InventorySourceSnapshot> explicitSourceSnapshotsById) {
            this.explicitSourceSnapshotsById = explicitSourceSnapshotsById == null ? Map.of() : explicitSourceSnapshotsById;
        }

        private void setCursorStack(ItemStack cursorStack) {
            this.cursorStack = cursorStack == null ? ItemStack.EMPTY : cursorStack.copy();
        }

        private InventorySourceSnapshot slotBackedSnapshot(
                InventorySourceDescriptor source,
                List<InventoryStackSnapshot> snapshots
        ) {
            ArrayList<InventoryEntrySnapshot> entries = new ArrayList<>();
            if (snapshots != null) {
                for (InventoryStackSnapshot snapshot : snapshots) {
                    if (snapshot == null) {
                        continue;
                    }
                    entries.add(new InventoryEntrySnapshot(
                            InventoryEntryKey.slot(source.id(), snapshot.handle()),
                            snapshot.stack(),
                            snapshot.count(),
                            ""
                    ));
                }
            }
            int highestSlot = entries.stream().mapToInt(InventoryEntrySnapshot::slotIndex).max().orElse(-1);
            int slotCapacity = Math.max(
                    capacitiesBySourceId.getOrDefault(source.id(), source.logicalSlotCount()),
                    Math.max(source.logicalSlotCount(), highestSlot + 1)
            );
            return new InventorySourceSnapshot(source.id(), slotCapacity, List.copyOf(entries), "");
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
