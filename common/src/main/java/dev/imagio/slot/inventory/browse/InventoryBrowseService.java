package dev.imagio.slot.inventory.browse;

import dev.imagio.slot.inventory.action.InventoryCommandAvailability;
import dev.imagio.slot.inventory.action.InventoryCommandId;
import dev.imagio.slot.inventory.action.InventoryCommandReasonCode;
import dev.imagio.slot.inventory.action.InventoryActionScope;
import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.query.InventoryWorkingSetProjection;
import dev.imagio.slot.inventory.query.InventoryWorkingSetProjectionService;
import dev.imagio.slot.inventory.query.ProjectedInventoryRow;
import dev.imagio.slot.workflow.domain.CollectionProjection;
import dev.imagio.slot.workflow.domain.QuickAccessLoadoutDefinition;
import dev.imagio.slot.workflow.domain.RecentView;
import dev.imagio.slot.workflow.domain.WorkflowDomainSnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class InventoryBrowseService {
    private InventoryBrowseService() {
    }

    public static InventoryBrowseDocument browse(InventoryBrowseRequest request) {
        InventoryBrowseRequest resolved = request == null ? new InventoryBrowseRequest(null, null, null, null, null) : request;
        if (resolved.authority().host() == null) {
            return new InventoryBrowseDocument(
                    InventoryBrowsePaneMode.CARRIED_ONLY,
                    InventoryPaneMembership.CARRIED,
                    List.of(),
                    resolved.sessionState(),
                    "missing_authority"
            );
        }

        InventoryBrowsePaneMode paneMode = resolvePaneMode(resolved);
        InventoryPaneMembership activePane = resolveActivePane(resolved, paneMode);
        ArrayList<InventoryBrowsePane> panes = new ArrayList<>();
        for (InventoryPaneMembership paneMembership : visiblePanes(paneMode)) {
            panes.add(buildPane(resolved, paneMembership));
        }

        return new InventoryBrowseDocument(
                paneMode,
                activePane,
                List.copyOf(panes),
                resolved.sessionState(),
                ""
        );
    }

    private static InventoryBrowsePane buildPane(InventoryBrowseRequest request, InventoryPaneMembership paneMembership) {
        InventoryWorkingSetProjection projection = InventoryWorkingSetProjectionService.project(
                request.authority(),
                paneMembership,
                request.identityResolver()
        );
        List<InventoryBrowseEntry.ItemEntry> itemEntries = itemEntries(request, projection);
        List<InventoryBrowseEntry.PlaceholderEntry> placeholders = placeholderEntries(request, paneMembership, itemEntries);
        List<InventoryBrowseEntry.LoadoutEntry> loadouts = loadoutEntries(request, paneMembership);

        ArrayList<InventoryBrowseSection> sections = new ArrayList<>();
        if (!loadouts.isEmpty()) {
            String sectionId = "pane:" + paneMembership.name().toLowerCase(Locale.ROOT) + "/loadouts";
            sections.add(new InventoryBrowseSection(
                    new InventoryBrowseSubjectRef.SectionRef(paneMembership, sectionId),
                    sectionId,
                    "Loadouts",
                    InventoryBrowseSectionKind.LOADOUTS,
                    expanded(request.sessionState(), sectionId),
                    List.copyOf(loadouts),
                    unsupportedSectionCommands("loadout_section_transfer_not_supported"),
                    ""
            ));
        }
        sections.addAll(groupedItemSections(request, paneMembership, itemEntries));
        if (!placeholders.isEmpty()) {
            String sectionId = "pane:" + paneMembership.name().toLowerCase(Locale.ROOT) + "/placeholders";
            sections.add(new InventoryBrowseSection(
                    new InventoryBrowseSubjectRef.SectionRef(paneMembership, sectionId),
                    sectionId,
                    "Tracked Missing",
                    InventoryBrowseSectionKind.PLACEHOLDERS,
                    expanded(request.sessionState(), sectionId),
                    List.copyOf(placeholders),
                    unsupportedSectionCommands("placeholder_section_transfer_not_supported"),
                    ""
            ));
        }

        return new InventoryBrowsePane(
                new InventoryBrowseSubjectRef.PaneRef(paneMembership),
                paneMembership,
                List.copyOf(sections),
                paneCommands(itemEntries, placeholders, loadouts),
                ""
        );
    }

    private static List<InventoryBrowseSection> groupedItemSections(
            InventoryBrowseRequest request,
            InventoryPaneMembership paneMembership,
            List<InventoryBrowseEntry.ItemEntry> itemEntries
    ) {
        if (itemEntries.isEmpty()) {
            String sectionId = "pane:" + paneMembership.name().toLowerCase(Locale.ROOT) + "/items";
            return List.of(new InventoryBrowseSection(
                    new InventoryBrowseSubjectRef.SectionRef(paneMembership, sectionId),
                    sectionId,
                    "Items",
                    InventoryBrowseSectionKind.ITEMS,
                    true,
                    List.of(),
                    sectionCommands(true),
                    ""
            ));
        }

        InventoryBrowseGroupingMode groupingMode = request.sessionState().groupingMode();
        if (groupingMode == InventoryBrowseGroupingMode.FLAT) {
            String sectionId = "pane:" + paneMembership.name().toLowerCase(Locale.ROOT) + "/items";
            return List.of(new InventoryBrowseSection(
                    new InventoryBrowseSubjectRef.SectionRef(paneMembership, sectionId),
                    sectionId,
                    "Items",
                    InventoryBrowseSectionKind.ITEMS,
                    expanded(request.sessionState(), sectionId),
                    List.copyOf(itemEntries),
                    sectionCommands(false),
                    ""
            ));
        }

        LinkedHashMap<String, ArrayList<InventoryBrowseEntry>> entriesBySectionId = new LinkedHashMap<>();
        LinkedHashMap<String, String> titleBySectionId = new LinkedHashMap<>();
        LinkedHashMap<String, InventoryBrowseSectionKind> kindBySectionId = new LinkedHashMap<>();
        for (InventoryBrowseEntry.ItemEntry itemEntry : itemEntries) {
            String sourceId = itemEntry.row().backingSources().isEmpty() ? "unknown" : itemEntry.row().backingSources().get(0);
            InventorySourceDescriptor source = request.authority().source(sourceId);
            String sectionId = "source:" + sourceId;
            String title = source == null ? sourceId : source.label().getString();
            entriesBySectionId.computeIfAbsent(sectionId, ignored -> new ArrayList<>()).add(itemEntry);
            titleBySectionId.putIfAbsent(sectionId, title);
            kindBySectionId.putIfAbsent(sectionId, InventoryBrowseSectionKind.SOURCE);
        }

        ArrayList<InventoryBrowseSection> sections = new ArrayList<>();
        entriesBySectionId.forEach((sectionId, entries) -> {
            String visibleSectionId = "pane:" + paneMembership.name().toLowerCase(Locale.ROOT) + "/" + sectionId;
            sections.add(new InventoryBrowseSection(
                new InventoryBrowseSubjectRef.SectionRef(paneMembership, visibleSectionId),
                visibleSectionId,
                titleBySectionId.getOrDefault(sectionId, "Items"),
                kindBySectionId.getOrDefault(sectionId, InventoryBrowseSectionKind.ITEMS),
                expanded(request.sessionState(), visibleSectionId),
                List.copyOf(entries),
                sectionCommands(entries.isEmpty()),
                ""
            ));
        });
        return List.copyOf(sections);
    }

    private static List<InventoryBrowseEntry.ItemEntry> itemEntries(
            InventoryBrowseRequest request,
            InventoryWorkingSetProjection projection
    ) {
        ArrayList<InventoryBrowseEntry.ItemEntry> entries = new ArrayList<>();
        WorkflowDomainSnapshot workflow = request.workflow();
        CollectionProjection collections = workflow.collections();
        RecentView recents = workflow.recents();
        String selectedCollectionId = request.sessionState().selectedCollectionId();
        for (ProjectedInventoryRow row : projection.rows()) {
            if (row == null || row.identity() == null) {
                continue;
            }
            InventoryBrowseAnnotations annotations = annotationsForRow(
                    row.identity(),
                    collections,
                    recents,
                    selectedCollectionId
            );
            if (!matchesScope(request.sessionState(), annotations)) {
                continue;
            }
            if (!matchesSearch(request.sessionState().filter().searchText(), row.identity().itemId())) {
                continue;
            }
            InventoryBrowseSubjectRef.ItemRowRef subjectRef = new InventoryBrowseSubjectRef.ItemRowRef(
                    row.paneMembership(),
                    row.identity()
            );
            entries.add(new InventoryBrowseEntry.ItemEntry(
                    subjectRef,
                    row,
                    annotations,
                    subjectRef.equals(request.sessionState().selectedSubject()),
                    itemCommands(row, annotations, selectedCollectionId),
                    row.diagnostics()
            ));
        }
        entries.sort(itemComparator(request.sessionState().sortMode(), recents.countsByIdentity()));
        return List.copyOf(entries);
    }

    private static List<InventoryBrowseEntry.PlaceholderEntry> placeholderEntries(
            InventoryBrowseRequest request,
            InventoryPaneMembership paneMembership,
            List<InventoryBrowseEntry.ItemEntry> visibleItemEntries
    ) {
        if (paneMembership != InventoryPaneMembership.CARRIED
                || request.sessionState().filter().scope() != InventoryBrowseFilterScope.SELECTED_COLLECTION
                || request.sessionState().selectedCollectionId().isBlank()) {
            return List.of();
        }

        String collectionId = request.sessionState().selectedCollectionId();
        LinkedHashSet<ItemIdentity> trackedIdentities = selectedCollectionIdentities(request.workflow(), collectionId);
        if (trackedIdentities.isEmpty()) {
            return List.of();
        }

        LinkedHashSet<ItemIdentity> visibleIdentities = new LinkedHashSet<>();
        for (InventoryBrowseEntry.ItemEntry itemEntry : visibleItemEntries) {
            visibleIdentities.add(itemEntry.row().identity());
        }

        ArrayList<InventoryBrowseEntry.PlaceholderEntry> placeholders = new ArrayList<>();
        for (ItemIdentity identity : trackedIdentities) {
            if (identity == null || visibleIdentities.contains(identity)) {
                continue;
            }
            if (!matchesSearch(request.sessionState().filter().searchText(), identity.itemId())) {
                continue;
            }
            InventoryBrowseAnnotations annotations = annotationsForRow(
                    identity,
                    request.workflow().collections(),
                    request.workflow().recents(),
                    collectionId
            );
            InventoryBrowseSubjectRef.PlaceholderRef subjectRef = new InventoryBrowseSubjectRef.PlaceholderRef(collectionId, identity);
            placeholders.add(new InventoryBrowseEntry.PlaceholderEntry(
                    subjectRef,
                    collectionId,
                    identity,
                    annotations,
                    subjectRef.equals(request.sessionState().selectedSubject()),
                    placeholderCommands(identity, collectionId),
                    ""
            ));
        }
        placeholders.sort(Comparator.comparing(entry -> entry.identity().itemId(), String.CASE_INSENSITIVE_ORDER));
        return List.copyOf(placeholders);
    }

    private static List<InventoryBrowseEntry.LoadoutEntry> loadoutEntries(
            InventoryBrowseRequest request,
            InventoryPaneMembership paneMembership
    ) {
        if (paneMembership != InventoryPaneMembership.CARRIED || request.sessionState().selectedCollectionId().isBlank()) {
            return List.of();
        }
        List<QuickAccessLoadoutDefinition> loadouts = request.workflow().collections()
                .loadoutsByCollection()
                .getOrDefault(request.sessionState().selectedCollectionId(), List.of());
        if (loadouts.isEmpty()) {
            return List.of();
        }
        ArrayList<InventoryBrowseEntry.LoadoutEntry> entries = new ArrayList<>();
        for (QuickAccessLoadoutDefinition loadout : loadouts) {
            if (loadout == null) {
                continue;
            }
            if (!matchesSearch(request.sessionState().filter().searchText(), loadout.name())) {
                continue;
            }
            boolean selected = loadout.id().equals(request.sessionState().selectedLoadoutId());
            entries.add(new InventoryBrowseEntry.LoadoutEntry(
                    new InventoryBrowseSubjectRef.LoadoutRef(
                            request.sessionState().selectedCollectionId(),
                            loadout.id()
                    ),
                    request.sessionState().selectedCollectionId(),
                    loadout,
                    selected,
                    loadoutCommands(loadout, selected),
                    ""
            ));
        }
        return List.copyOf(entries);
    }

    private static InventoryBrowseAnnotations annotationsForRow(
            ItemIdentity identity,
            CollectionProjection collections,
            RecentView recents,
            String selectedCollectionId
    ) {
        Set<String> collectionIds = collections.memberships().getOrDefault(identity, Set.of());
        // Collection-scoped desired counts retired with the kits replacement
        // of collections; this annotation is now always 0 for the
        // collection pane. Player-global / kit-scoped counts surface on the
        // atlas card, not in the collection browse rows.
        return new InventoryBrowseAnnotations(
                collections.favoriteTags().contains(identity),
                collections.junkTags().contains(identity),
                recents.countsByIdentity().containsKey(identity),
                collectionIds,
                0
        );
    }

    private static boolean matchesScope(
            InventoryBrowseSessionState sessionState,
            InventoryBrowseAnnotations annotations
    ) {
        if (sessionState == null || annotations == null) {
            return false;
        }
        return switch (sessionState.filter().scope()) {
            case ALL -> true;
            case FAVORITES -> annotations.favorite();
            case RECENT -> annotations.recent();
            case SELECTED_COLLECTION -> !sessionState.selectedCollectionId().isBlank()
                    && annotations.collectionIds().contains(sessionState.selectedCollectionId());
        };
    }

    private static boolean matchesSearch(String searchText, String candidate) {
        if (searchText == null || searchText.isBlank()) {
            return true;
        }
        if (candidate == null || candidate.isBlank()) {
            return false;
        }
        return candidate.toLowerCase(Locale.ROOT).contains(searchText.toLowerCase(Locale.ROOT).trim());
    }

    private static Comparator<InventoryBrowseEntry.ItemEntry> itemComparator(
            InventoryBrowseSortMode sortMode,
            Map<ItemIdentity, Integer> recentCounts
    ) {
        InventoryBrowseSortMode resolved = sortMode == null ? InventoryBrowseSortMode.NAME : sortMode;
        return switch (resolved) {
            case COUNT_DESC -> Comparator
                    .comparingInt((InventoryBrowseEntry.ItemEntry entry) -> entry.row().visibleTotalCount())
                    .reversed()
                    .thenComparing(entry -> entry.row().identity().itemId(), String.CASE_INSENSITIVE_ORDER);
            case RECENT_FIRST -> Comparator
                    .comparingInt((InventoryBrowseEntry.ItemEntry entry) -> recentCounts.getOrDefault(entry.row().identity(), 0))
                    .reversed()
                    .thenComparing(entry -> entry.row().identity().itemId(), String.CASE_INSENSITIVE_ORDER);
            case NAME -> Comparator.comparing(entry -> entry.row().identity().itemId(), String.CASE_INSENSITIVE_ORDER);
        };
    }

    private static Map<InventoryCommandId, InventoryCommandAvailability> itemCommands(
            ProjectedInventoryRow row,
            InventoryBrowseAnnotations annotations,
            String selectedCollectionId
    ) {
        LinkedHashMap<InventoryCommandId, InventoryCommandAvailability> commands = new LinkedHashMap<>();
        boolean hasBackingEntries = row != null && !row.backingEntries().isEmpty();
        InventoryCommandAvailability transferAvailability = hasBackingEntries
                ? InventoryCommandAvailability.enabled()
                : InventoryCommandAvailability.unavailable(InventoryCommandReasonCode.NO_BACKING_ENTRIES, "no_backing_entries");
        commands.put(InventoryCommandId.TRANSFER_ONE, transferAvailability);
        commands.put(InventoryCommandId.TRANSFER_STACK, transferAvailability);
        commands.put(InventoryCommandId.TRANSFER_ALL_EXACT, transferAvailability);
        commands.put(InventoryCommandId.TOGGLE_FAVORITE, InventoryCommandAvailability.enabled());
        commands.put(
                InventoryCommandId.TOGGLE_COLLECTION_MEMBERSHIP,
                selectedCollectionId == null || selectedCollectionId.isBlank()
                        ? InventoryCommandAvailability.unavailable(InventoryCommandReasonCode.NO_SELECTED_COLLECTION, "no_selected_collection")
                        : InventoryCommandAvailability.enabled()
        );
        commands.put(
                InventoryCommandId.DISMISS_RECENT,
                annotations != null && annotations.recent()
                        ? InventoryCommandAvailability.enabled()
                        : InventoryCommandAvailability.unavailable(InventoryCommandReasonCode.NOT_RECENT, "not_recent")
        );
        commands.put(InventoryCommandId.TRASH, InventoryCommandAvailability.unavailable(InventoryCommandReasonCode.UNSUPPORTED, "trash_not_yet_specified"));
        commands.put(InventoryCommandId.VOID, InventoryCommandAvailability.unavailable(InventoryCommandReasonCode.UNSUPPORTED, "void_not_yet_specified"));
        return Map.copyOf(commands);
    }

    private static Map<InventoryCommandId, InventoryCommandAvailability> placeholderCommands(
            ItemIdentity identity,
            String selectedCollectionId
    ) {
        LinkedHashMap<InventoryCommandId, InventoryCommandAvailability> commands = new LinkedHashMap<>();
        InventoryCommandAvailability unavailable = InventoryCommandAvailability.unavailable(InventoryCommandReasonCode.PLACEHOLDER_ONLY, "placeholder_only");
        commands.put(InventoryCommandId.TRANSFER_ONE, unavailable);
        commands.put(InventoryCommandId.TRANSFER_STACK, unavailable);
        commands.put(InventoryCommandId.TRANSFER_ALL_EXACT, unavailable);
        commands.put(InventoryCommandId.TRASH, InventoryCommandAvailability.unavailable(InventoryCommandReasonCode.UNSUPPORTED, "trash_not_yet_specified"));
        commands.put(InventoryCommandId.VOID, InventoryCommandAvailability.unavailable(InventoryCommandReasonCode.UNSUPPORTED, "void_not_yet_specified"));
        commands.put(InventoryCommandId.TOGGLE_FAVORITE, identity == null
                ? InventoryCommandAvailability.unavailable(InventoryCommandReasonCode.MISSING_IDENTITY, "missing_identity")
                : InventoryCommandAvailability.enabled());
        commands.put(
                InventoryCommandId.TOGGLE_COLLECTION_MEMBERSHIP,
                selectedCollectionId == null || selectedCollectionId.isBlank()
                        ? InventoryCommandAvailability.unavailable(InventoryCommandReasonCode.NO_SELECTED_COLLECTION, "no_selected_collection")
                        : InventoryCommandAvailability.enabled()
        );
        return Map.copyOf(commands);
    }

    private static Map<InventoryCommandId, InventoryCommandAvailability> loadoutCommands(
            QuickAccessLoadoutDefinition loadout,
            boolean selected
    ) {
        LinkedHashMap<InventoryCommandId, InventoryCommandAvailability> commands = new LinkedHashMap<>();
        commands.put(
                InventoryCommandId.APPLY_LOADOUT,
                loadout == null || loadout.entries().isEmpty()
                        ? InventoryCommandAvailability.unavailable(InventoryCommandReasonCode.NO_LOADOUT_ENTRIES, "loadout_empty")
                        : InventoryCommandAvailability.enabled()
        );
        commands.put(
                InventoryCommandId.SELECT_LOADOUT,
                selected
                        ? InventoryCommandAvailability.unavailable(InventoryCommandReasonCode.ALREADY_SELECTED, "already_selected")
                        : InventoryCommandAvailability.enabled()
        );
        return Map.copyOf(commands);
    }

    private static Map<InventoryCommandId, InventoryCommandAvailability> sectionCommands(boolean empty) {
        if (!empty) {
            return Map.of(InventoryCommandId.TRANSFER_ALL_VISIBLE, InventoryCommandAvailability.enabled());
        }
        return Map.of(
                InventoryCommandId.TRANSFER_ALL_VISIBLE,
                InventoryCommandAvailability.unavailable(InventoryCommandReasonCode.NO_BACKING_ENTRIES, "no_backing_entries")
        );
    }

    private static Map<InventoryCommandId, InventoryCommandAvailability> paneCommands(
            List<InventoryBrowseEntry.ItemEntry> itemEntries,
            List<InventoryBrowseEntry.PlaceholderEntry> placeholders,
            List<InventoryBrowseEntry.LoadoutEntry> loadouts
    ) {
        boolean empty = itemEntries == null || itemEntries.isEmpty();
        return sectionCommands(empty);
    }

    private static Map<InventoryCommandId, InventoryCommandAvailability> unsupportedSectionCommands(String diagnostics) {
        return Map.of(
                InventoryCommandId.TRANSFER_ALL_VISIBLE,
                InventoryCommandAvailability.unavailable(InventoryCommandReasonCode.UNSUPPORTED, diagnostics)
        );
    }

    private static LinkedHashSet<ItemIdentity> selectedCollectionIdentities(
            WorkflowDomainSnapshot workflow,
            String collectionId
    ) {
        LinkedHashSet<ItemIdentity> identities = new LinkedHashSet<>();
        if (workflow == null || collectionId == null || collectionId.isBlank()) {
            return identities;
        }
        workflow.collections().memberships().forEach((identity, collectionIds) -> {
            if (identity != null && collectionIds != null && collectionIds.contains(collectionId)) {
                identities.add(identity);
            }
        });
        // Legacy desired-count membership join retired alongside the
        // collection-scoped DesiredCount domain.
        return identities;
    }

    private static InventoryBrowsePaneMode resolvePaneMode(InventoryBrowseRequest request) {
        boolean hasExternal = request.authority().sourceDescriptors().stream()
                .filter(Objects::nonNull)
                .anyMatch(InventorySourceDescriptor::inExternalPane);
        return hasExternal && request.sessionState().paneMode() == InventoryBrowsePaneMode.DUAL_PANE
                ? InventoryBrowsePaneMode.DUAL_PANE
                : InventoryBrowsePaneMode.CARRIED_ONLY;
    }

    private static InventoryPaneMembership resolveActivePane(
            InventoryBrowseRequest request,
            InventoryBrowsePaneMode paneMode
    ) {
        if (paneMode == InventoryBrowsePaneMode.CARRIED_ONLY) {
            return InventoryPaneMembership.CARRIED;
        }
        return request.sessionState().activePane() == InventoryPaneMembership.EXTERNAL
                ? InventoryPaneMembership.EXTERNAL
                : InventoryPaneMembership.CARRIED;
    }

    private static List<InventoryPaneMembership> visiblePanes(InventoryBrowsePaneMode paneMode) {
        return paneMode == InventoryBrowsePaneMode.DUAL_PANE
                ? List.of(InventoryPaneMembership.CARRIED, InventoryPaneMembership.EXTERNAL)
                : List.of(InventoryPaneMembership.CARRIED);
    }

    private static boolean expanded(InventoryBrowseSessionState sessionState, String sectionId) {
        return sessionState == null
                || sessionState.expandedSectionIds().isEmpty()
                || sectionId == null
                || sectionId.isBlank()
                || sessionState.expandedSectionIds().contains(sectionId);
    }
}
