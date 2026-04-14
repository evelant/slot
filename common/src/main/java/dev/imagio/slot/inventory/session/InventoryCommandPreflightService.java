package dev.imagio.slot.inventory.session;

import dev.imagio.slot.inventory.action.InventoryActionDestination;
import dev.imagio.slot.inventory.action.InventoryActionKind;
import dev.imagio.slot.inventory.action.InventoryActionScope;
import dev.imagio.slot.inventory.action.InventoryCommandAvailability;
import dev.imagio.slot.inventory.action.InventoryCommandId;
import dev.imagio.slot.inventory.action.InventoryCommandReasonCode;
import dev.imagio.slot.inventory.action.ProjectedRowTransferIntent;
import dev.imagio.slot.inventory.action.ProjectedRowTransferPlan;
import dev.imagio.slot.inventory.action.ProjectedRowTransferPlanner;
import dev.imagio.slot.inventory.browse.InventoryBrowseDocument;
import dev.imagio.slot.inventory.browse.InventoryBrowseDocumentQueries;
import dev.imagio.slot.inventory.browse.InventoryBrowseEntry;
import dev.imagio.slot.inventory.browse.InventoryBrowsePane;
import dev.imagio.slot.inventory.browse.InventoryBrowseSection;
import dev.imagio.slot.inventory.browse.InventoryBrowseSubjectRef;
import dev.imagio.slot.inventory.core.InventoryCapability;
import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import dev.imagio.slot.inventory.intent.InventoryIntent;
import dev.imagio.slot.inventory.intent.InventoryMutationIntent;
import dev.imagio.slot.inventory.intent.InventoryWorkflowIntent;
import dev.imagio.slot.workflow.domain.LoadoutApplyService;
import dev.imagio.slot.workflow.domain.ProtectionPolicy;

import java.util.List;

public final class InventoryCommandPreflightService {
    private InventoryCommandPreflightService() {
    }

    public static InventoryCommandPreflight preflight(
            InventorySessionSnapshot session,
            InventoryCommandInvocation invocation
    ) {
        if (session == null) {
            return rejected(invocation, "missing_session", InventoryCommandReasonCode.INVALID_INTENT);
        }
        if (invocation == null || invocation.subjectRef() == null || invocation.commandId() == null) {
            return rejected(invocation, "missing_invocation_fields", InventoryCommandReasonCode.INVALID_INTENT);
        }
        if (!sameToken(session.token(), invocation.sessionToken())) {
            return rejected(invocation, "stale_session_revision", InventoryCommandReasonCode.INVALID_INTENT);
        }

        InventoryBrowseDocument document = session.browseDocument();
        return switch (invocation.subjectRef()) {
            case InventoryBrowseSubjectRef.ItemRowRef itemRowRef -> itemRow(session, invocation, document, itemRowRef);
            case InventoryBrowseSubjectRef.PlaceholderRef placeholderRef -> placeholder(session, invocation, document, placeholderRef);
            case InventoryBrowseSubjectRef.LoadoutRef loadoutRef -> loadout(session, invocation, document, loadoutRef);
            case InventoryBrowseSubjectRef.SectionRef sectionRef -> section(session, invocation, document, sectionRef);
            case InventoryBrowseSubjectRef.PaneRef paneRef -> pane(session, invocation, document, paneRef);
        };
    }

    private static InventoryCommandPreflight itemRow(
            InventorySessionSnapshot session,
            InventoryCommandInvocation invocation,
            InventoryBrowseDocument document,
            InventoryBrowseSubjectRef.ItemRowRef subjectRef
    ) {
        InventoryBrowseEntry.ItemEntry entry = InventoryBrowseDocumentQueries.findItemEntry(document, subjectRef);
        if (entry == null) {
            return rejected(invocation, "subject_not_visible", InventoryCommandReasonCode.NOT_VISIBLE_IN_SCOPE);
        }
        InventoryBrowsePane pane = InventoryBrowseDocumentQueries.findPane(document, new InventoryBrowseSubjectRef.PaneRef(subjectRef.paneMembership()));
        return switch (invocation.commandId()) {
            case TRANSFER_ONE -> rowTransfer(
                    session,
                    invocation,
                    commandAvailability(entry.commands(), invocation.commandId()),
                    pane,
                    entry.row(),
                    InventoryActionKind.TRANSFER_ONE,
                    InventoryActionScope.BEST_SINGLE_SOURCE,
                    subjectRef.paneMembership()
            );
            case TRANSFER_STACK -> rowTransfer(
                    session,
                    invocation,
                    commandAvailability(entry.commands(), invocation.commandId()),
                    pane,
                    entry.row(),
                    InventoryActionKind.TRANSFER_STACK,
                    InventoryActionScope.BEST_SINGLE_SOURCE,
                    subjectRef.paneMembership()
            );
            case TRANSFER_ALL_EXACT -> rowTransfer(
                    session,
                    invocation,
                    commandAvailability(entry.commands(), invocation.commandId()),
                    pane,
                    entry.row(),
                    InventoryActionKind.TRANSFER_ALL,
                    InventoryActionScope.VISIBLE_MATCHES,
                    subjectRef.paneMembership()
            );
            case TOGGLE_FAVORITE -> workflow(invocation, commandAvailability(entry.commands(), invocation.commandId()), new InventoryWorkflowIntent.ToggleFavorite(entry.row().identity(), invocation.origin()));
            case TOGGLE_COLLECTION_MEMBERSHIP -> workflow(
                    invocation,
                    commandAvailability(entry.commands(), invocation.commandId()),
                    new InventoryWorkflowIntent.ToggleCollectionMembership(
                            entry.row().identity(),
                            session.workflow().browseSessionState().selectedCollectionId(),
                            invocation.origin()
                    )
            );
            case DISMISS_RECENT -> workflow(invocation, commandAvailability(entry.commands(), invocation.commandId()), new InventoryWorkflowIntent.DismissRecent(entry.row().identity(), invocation.origin()));
            default -> rejected(invocation, "unsupported_item_command", InventoryCommandReasonCode.UNSUPPORTED);
        };
    }

    private static InventoryCommandPreflight placeholder(
            InventorySessionSnapshot session,
            InventoryCommandInvocation invocation,
            InventoryBrowseDocument document,
            InventoryBrowseSubjectRef.PlaceholderRef subjectRef
    ) {
        InventoryBrowseEntry.PlaceholderEntry entry = InventoryBrowseDocumentQueries.findPlaceholderEntry(document, subjectRef);
        if (entry == null) {
            return rejected(invocation, "subject_not_visible", InventoryCommandReasonCode.NOT_VISIBLE_IN_SCOPE);
        }
        return switch (invocation.commandId()) {
            case TOGGLE_FAVORITE -> workflow(invocation, commandAvailability(entry.commands(), invocation.commandId()), new InventoryWorkflowIntent.ToggleFavorite(entry.identity(), invocation.origin()));
            case TOGGLE_COLLECTION_MEMBERSHIP -> workflow(
                    invocation,
                    commandAvailability(entry.commands(), invocation.commandId()),
                    new InventoryWorkflowIntent.ToggleCollectionMembership(entry.identity(), entry.collectionId(), invocation.origin())
            );
            default -> rejected(invocation, "unsupported_placeholder_command", InventoryCommandReasonCode.UNSUPPORTED);
        };
    }

    private static InventoryCommandPreflight loadout(
            InventorySessionSnapshot session,
            InventoryCommandInvocation invocation,
            InventoryBrowseDocument document,
            InventoryBrowseSubjectRef.LoadoutRef subjectRef
    ) {
        InventoryBrowseEntry.LoadoutEntry entry = InventoryBrowseDocumentQueries.findLoadoutEntry(document, subjectRef);
        if (entry == null) {
            return rejected(invocation, "subject_not_visible", InventoryCommandReasonCode.NOT_VISIBLE_IN_SCOPE);
        }

        InventoryCommandAvailability availability = commandAvailability(entry.commands(), invocation.commandId());
        if (!availability.available()) {
            return new InventoryCommandPreflight(invocation, availability, null, List.of(), null, null, availability.diagnostics());
        }

        return switch (invocation.commandId()) {
            case SELECT_LOADOUT -> workflow(
                    invocation,
                    availability,
                    new InventoryWorkflowIntent.SelectLoadout(entry.collectionId(), entry.loadout().id(), invocation.origin())
            );
            case APPLY_LOADOUT -> {
                LoadoutApplyService.LoadoutApplyPlan plan = LoadoutApplyService.plan(
                        entry.loadout(),
                        session.authority(),
                        session.workflow().protection(),
                        invocation.mode(),
                        candidate -> candidate == null || !candidate.present()
                                ? null
                                : dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(candidate.stack())
                );
                yield new InventoryCommandPreflight(
                        invocation,
                        availability,
                        new InventoryWorkflowIntent.ApplyLoadout(
                                entry.collectionId(),
                                entry.loadout().id(),
                                session.workflow().protection(),
                                invocation.origin()
                        ),
                        plan.requests(),
                        null,
                        plan,
                        joinDiagnostics(plan.diagnostics())
                );
            }
            default -> rejected(invocation, "unsupported_loadout_command", InventoryCommandReasonCode.UNSUPPORTED);
        };
    }

    private static InventoryCommandPreflight section(
            InventorySessionSnapshot session,
            InventoryCommandInvocation invocation,
            InventoryBrowseDocument document,
            InventoryBrowseSubjectRef.SectionRef subjectRef
    ) {
        InventoryBrowseSection section = InventoryBrowseDocumentQueries.findSection(document, subjectRef);
        if (section == null) {
            return rejected(invocation, "subject_not_visible", InventoryCommandReasonCode.NOT_VISIBLE_IN_SCOPE);
        }
        if (invocation.commandId() != InventoryCommandId.TRANSFER_ALL_VISIBLE) {
            return rejected(invocation, "unsupported_section_command", InventoryCommandReasonCode.UNSUPPORTED);
        }
        InventoryCommandAvailability availability = commandAvailability(section.commands(), invocation.commandId());
        if (!availability.available()) {
            return new InventoryCommandPreflight(invocation, availability, null, List.of(), null, null, availability.diagnostics());
        }
        InventoryBrowsePane pane = InventoryBrowseDocumentQueries.findPane(document, new InventoryBrowseSubjectRef.PaneRef(subjectRef.paneMembership()));
        List<InventoryBrowseEntry.ItemEntry> itemEntries = InventoryBrowseDocumentQueries.itemEntries(section);
        return visibleRowsTransfer(
                session,
                invocation,
                availability,
                pane,
                itemEntries,
                subjectRef.paneMembership()
        );
    }

    private static InventoryCommandPreflight pane(
            InventorySessionSnapshot session,
            InventoryCommandInvocation invocation,
            InventoryBrowseDocument document,
            InventoryBrowseSubjectRef.PaneRef subjectRef
    ) {
        InventoryBrowsePane pane = InventoryBrowseDocumentQueries.findPane(document, subjectRef);
        if (pane == null) {
            return rejected(invocation, "subject_not_visible", InventoryCommandReasonCode.NOT_VISIBLE_IN_SCOPE);
        }
        if (invocation.commandId() != InventoryCommandId.TRANSFER_ALL_VISIBLE) {
            return rejected(invocation, "unsupported_pane_command", InventoryCommandReasonCode.UNSUPPORTED);
        }
        InventoryCommandAvailability availability = commandAvailability(pane.commands(), invocation.commandId());
        if (!availability.available()) {
            return new InventoryCommandPreflight(invocation, availability, null, List.of(), null, null, availability.diagnostics());
        }
        return visibleRowsTransfer(
                session,
                invocation,
                availability,
                pane,
                InventoryBrowseDocumentQueries.itemEntries(pane),
                subjectRef.paneMembership()
        );
    }

    private static InventoryCommandPreflight rowTransfer(
            InventorySessionSnapshot session,
            InventoryCommandInvocation invocation,
            InventoryCommandAvailability declaredAvailability,
            InventoryBrowsePane pane,
            dev.imagio.slot.inventory.query.ProjectedInventoryRow anchorRow,
            InventoryActionKind kind,
            InventoryActionScope scope,
            InventoryPaneMembership sourcePaneMembership
    ) {
        if (!declaredAvailability.available()) {
            return new InventoryCommandPreflight(invocation, declaredAvailability, null, List.of(), null, null, declaredAvailability.diagnostics());
        }
        List<InventoryBrowseEntry.ItemEntry> itemEntries = pane == null ? List.of() : InventoryBrowseDocumentQueries.itemEntries(pane);
        ProjectedRowTransferIntent transferIntent = new ProjectedRowTransferIntent(
                session.authority(),
                InventoryBrowseDocumentQueries.projectedRows(itemEntries),
                anchorRow,
                kind,
                scope,
                defaultDestination(session, sourcePaneMembership),
                session.workflow().protection(),
                invocation.mode(),
                invocation.origin()
        );
        ProjectedRowTransferPlan plan = ProjectedRowTransferPlanner.plan(transferIntent);
        InventoryCommandAvailability availability = plan.requests().isEmpty()
                ? new InventoryCommandAvailability(false, plan.reasonCodes(), plan.capacityUncertain(), joinDiagnostics(plan.diagnostics()))
                : new InventoryCommandAvailability(true, plan.reasonCodes(), plan.capacityUncertain(), joinDiagnostics(plan.diagnostics()));
        return new InventoryCommandPreflight(
                invocation,
                availability,
                new InventoryMutationIntent.ProjectedRowTransfer(transferIntent, invocation.origin()),
                plan.requests(),
                plan,
                null,
                joinDiagnostics(plan.diagnostics())
        );
    }

    private static InventoryCommandPreflight visibleRowsTransfer(
            InventorySessionSnapshot session,
            InventoryCommandInvocation invocation,
            InventoryCommandAvailability declaredAvailability,
            InventoryBrowsePane pane,
            List<InventoryBrowseEntry.ItemEntry> itemEntries,
            InventoryPaneMembership sourcePaneMembership
    ) {
        if (!declaredAvailability.available()) {
            return new InventoryCommandPreflight(invocation, declaredAvailability, null, List.of(), null, null, declaredAvailability.diagnostics());
        }
        if (itemEntries == null || itemEntries.isEmpty()) {
            return rejected(invocation, "no_backing_entries", InventoryCommandReasonCode.NO_BACKING_ENTRIES);
        }
        ProjectedRowTransferIntent transferIntent = new ProjectedRowTransferIntent(
                session.authority(),
                InventoryBrowseDocumentQueries.projectedRows(itemEntries),
                itemEntries.getFirst().row(),
                InventoryActionKind.TRANSFER_ALL,
                InventoryActionScope.VISIBLE_ROWS,
                defaultDestination(session, sourcePaneMembership),
                session.workflow().protection(),
                invocation.mode(),
                invocation.origin()
        );
        ProjectedRowTransferPlan plan = ProjectedRowTransferPlanner.plan(transferIntent);
        InventoryCommandAvailability availability = plan.requests().isEmpty()
                ? new InventoryCommandAvailability(false, plan.reasonCodes(), plan.capacityUncertain(), joinDiagnostics(plan.diagnostics()))
                : new InventoryCommandAvailability(true, plan.reasonCodes(), plan.capacityUncertain(), joinDiagnostics(plan.diagnostics()));
        return new InventoryCommandPreflight(
                invocation,
                availability,
                new InventoryMutationIntent.ProjectedRowTransfer(transferIntent, invocation.origin()),
                plan.requests(),
                plan,
                null,
                joinDiagnostics(plan.diagnostics())
        );
    }

    private static InventoryCommandPreflight workflow(
            InventoryCommandInvocation invocation,
            InventoryCommandAvailability availability,
            InventoryIntent intent
    ) {
        if (!availability.available()) {
            return new InventoryCommandPreflight(invocation, availability, null, List.of(), null, null, availability.diagnostics());
        }
        return new InventoryCommandPreflight(invocation, availability, intent, List.of(), null, null, availability.diagnostics());
    }

    private static InventoryActionDestination defaultDestination(
            InventorySessionSnapshot session,
            InventoryPaneMembership sourcePaneMembership
    ) {
        if (sourcePaneMembership == InventoryPaneMembership.EXTERNAL) {
            return new InventoryActionDestination.PaneDestination(InventoryPaneMembership.CARRIED);
        }
        if (hasInsertableDestination(session, InventoryPaneMembership.EXTERNAL)) {
            return new InventoryActionDestination.PaneDestination(InventoryPaneMembership.EXTERNAL);
        }
        return new InventoryActionDestination.PaneDestination(InventoryPaneMembership.CARRIED);
    }

    private static boolean hasInsertableDestination(
            InventorySessionSnapshot session,
            InventoryPaneMembership paneMembership
    ) {
        if (session == null || session.authority() == null || session.authority().host() == null) {
            return false;
        }
        return session.authority().sourcesInPane(paneMembership).stream()
                .filter(source -> source != null && source.supports(InventoryCapability.INSERT))
                .findFirst()
                .isPresent();
    }

    private static InventoryCommandAvailability commandAvailability(
            java.util.Map<InventoryCommandId, InventoryCommandAvailability> commands,
            InventoryCommandId commandId
    ) {
        if (commands == null || commandId == null) {
            return InventoryCommandAvailability.unavailable(InventoryCommandReasonCode.UNSUPPORTED, "command_not_available");
        }
        InventoryCommandAvailability availability = commands.get(commandId);
        return availability == null
                ? InventoryCommandAvailability.unavailable(InventoryCommandReasonCode.UNSUPPORTED, "command_not_available")
                : availability;
    }

    private static InventoryCommandPreflight rejected(
            InventoryCommandInvocation invocation,
            String diagnostics,
            InventoryCommandReasonCode reasonCode
    ) {
        return new InventoryCommandPreflight(
                invocation,
                InventoryCommandAvailability.unavailable(reasonCode, diagnostics),
                null,
                List.of(),
                null,
                null,
                diagnostics
        );
    }

    private static boolean sameToken(InventorySessionToken expected, InventorySessionToken actual) {
        return expected != null
                && actual != null
                && expected.sessionId().equals(actual.sessionId())
                && expected.revision() == actual.revision();
    }

    private static String joinDiagnostics(List<String> diagnostics) {
        if (diagnostics == null || diagnostics.isEmpty()) {
            return "";
        }
        return String.join(",", diagnostics);
    }
}
