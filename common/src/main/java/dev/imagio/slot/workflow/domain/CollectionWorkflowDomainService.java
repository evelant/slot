package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.browse.InventoryBrowseSessionState;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public final class CollectionWorkflowDomainService {
    private final WorkflowDomainStateRepository repository;
    private final Runnable mutationObserver;

    public CollectionWorkflowDomainService(WorkflowDomainStateRepository repository) {
        this(repository, () -> {
        });
    }

    public CollectionWorkflowDomainService(
            WorkflowDomainStateRepository repository,
            Runnable mutationObserver
    ) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.mutationObserver = mutationObserver == null ? () -> {
        } : mutationObserver;
    }

    public CollectionDefinition createCollection(String name) {
        return createCollection(name, DomainEventMetadata.origin("workflow.collection.create"));
    }

    public CollectionDefinition createCollection(String name, DomainEventMetadata metadata) {
        String normalizedName = normalizeName(name, "Collection name must not be blank");
        String id = uniqueSlug(normalizedName, collections().collectionIds(), "collection");
        repository.appendWorkflowEvent(
                new WorkflowEvent.CollectionCreated(id, normalizedName),
                (metadata == null ? DomainEventMetadata.origin("") : metadata).withOrigin("workflow.collection.create")
        );
        notifyMutated();
        return definition(id);
    }

    public boolean renameCollection(String collectionId, String newName) {
        return renameCollection(collectionId, newName, DomainEventMetadata.origin("workflow.collection.rename"));
    }

    public boolean renameCollection(String collectionId, String newName, DomainEventMetadata metadata) {
        CollectionDefinition existing = requireCollection(collectionId);
        String normalizedName = normalizeName(newName, "Collection name must not be blank");
        if (existing.name().equals(normalizedName)) {
            return false;
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.CollectionRenamed(existing.id(), normalizedName),
                (metadata == null ? DomainEventMetadata.origin("") : metadata).withOrigin("workflow.collection.rename")
        );
        notifyMutated();
        return true;
    }

    public boolean deleteCollection(String collectionId) {
        return deleteCollection(collectionId, DomainEventMetadata.origin("workflow.collection.delete"));
    }

    public boolean deleteCollection(String collectionId, DomainEventMetadata metadata) {
        requireCollection(collectionId);
        repository.appendWorkflowEvent(
                new WorkflowEvent.CollectionDeleted(collectionId),
                (metadata == null ? DomainEventMetadata.origin("") : metadata).withOrigin("workflow.collection.delete")
        );
        repository.browseSessionState().update(state -> clearSelectionForCollection(state, collectionId));
        notifyMutated();
        return true;
    }

    public boolean collectionHasLoadouts(String collectionId) {
        return collectionId != null && !loadouts(collectionId).isEmpty();
    }

    public QuickAccessLoadoutDefinition selectedLoadout(String collectionId) {
        if (collectionId == null || collectionId.isBlank()) {
            return null;
        }
        List<QuickAccessLoadoutDefinition> loadouts = loadouts(collectionId);
        if (loadouts.isEmpty()) {
            return null;
        }
        ensureSelectedLoadout(collectionId, loadouts);
        String selectedId = repository.browseSessionState().current().selectedLoadoutId();
        return loadouts.stream()
                .filter(loadout -> loadout.id().equals(selectedId))
                .findFirst()
                .orElse(loadouts.getFirst());
    }

    public QuickAccessLoadoutDefinition createLoadout(
            String collectionId,
            String name,
            Set<QuickAccessLoadoutEntry> entries
    ) {
        return createLoadout(collectionId, name, entries, DomainEventMetadata.origin("workflow.loadout.create"));
    }

    public QuickAccessLoadoutDefinition createLoadout(
            String collectionId,
            String name,
            Set<QuickAccessLoadoutEntry> entries,
            DomainEventMetadata metadata
    ) {
        requireCollection(collectionId);
        String normalizedName = normalizeName(name, "Loadout name must not be blank");
        if (entries == null || entries.isEmpty()) {
            throw new IllegalArgumentException("Loadout must contain at least one target");
        }
        String id = uniqueSlug(
                normalizedName,
                loadouts(collectionId).stream().map(QuickAccessLoadoutDefinition::id).collect(java.util.stream.Collectors.toSet()),
                "loadout"
        );
        QuickAccessLoadoutDefinition loadout = new QuickAccessLoadoutDefinition(id, normalizedName, entries);
        repository.appendWorkflowEvent(
                new WorkflowEvent.LoadoutCreated(collectionId, loadout),
                (metadata == null ? DomainEventMetadata.origin("") : metadata).withOrigin("workflow.loadout.create")
        );
        select(collectionId, loadout.id());
        notifyMutated();
        return loadout(collectionId, loadout.id());
    }

    public QuickAccessLoadoutDefinition captureNewLoadout(
            String collectionId,
            String name,
            InventoryAuthoritySnapshot authority,
            Function<InventoryEntrySnapshot, ItemIdentity> identityResolver
    ) {
        return captureNewLoadout(
                collectionId,
                name,
                authority,
                identityResolver,
                DomainEventMetadata.origin("workflow.loadout.capture")
        );
    }

    public QuickAccessLoadoutDefinition captureNewLoadout(
            String collectionId,
            String name,
            InventoryAuthoritySnapshot authority,
            Function<InventoryEntrySnapshot, ItemIdentity> identityResolver,
            DomainEventMetadata metadata
    ) {
        QuickAccessLoadoutDefinition definition = LoadoutCaptureService.captureDefinition(
                uniqueSlug(normalizeName(name, "Loadout name must not be blank"), loadouts(collectionId).stream().map(QuickAccessLoadoutDefinition::id).collect(java.util.stream.Collectors.toSet()), "loadout"),
                name,
                authority,
                identityResolver
        );
        repository.appendWorkflowEvent(
                new WorkflowEvent.LoadoutCreated(collectionId, definition),
                (metadata == null ? DomainEventMetadata.origin("") : metadata).withOrigin("workflow.loadout.capture")
        );
        select(collectionId, definition.id());
        notifyMutated();
        return loadout(collectionId, definition.id());
    }

    public QuickAccessLoadoutDefinition updateSelectedLoadout(
            String collectionId,
            Set<QuickAccessLoadoutEntry> entries
    ) {
        return updateSelectedLoadout(collectionId, entries, DomainEventMetadata.origin("workflow.loadout.update"));
    }

    public QuickAccessLoadoutDefinition updateSelectedLoadout(
            String collectionId,
            Set<QuickAccessLoadoutEntry> entries,
            DomainEventMetadata metadata
    ) {
        QuickAccessLoadoutDefinition selected = selectedLoadout(collectionId);
        if (selected == null) {
            return null;
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.LoadoutUpdated(collectionId, selected.id(), entries),
                (metadata == null ? DomainEventMetadata.origin("") : metadata).withOrigin("workflow.loadout.update")
        );
        select(collectionId, selected.id());
        notifyMutated();
        return loadout(collectionId, selected.id());
    }

    public QuickAccessLoadoutDefinition recaptureSelectedLoadout(
            String collectionId,
            InventoryAuthoritySnapshot authority,
            Function<InventoryEntrySnapshot, ItemIdentity> identityResolver
    ) {
        return recaptureSelectedLoadout(
                collectionId,
                authority,
                identityResolver,
                DomainEventMetadata.origin("workflow.loadout.recapture")
        );
    }

    public QuickAccessLoadoutDefinition recaptureSelectedLoadout(
            String collectionId,
            InventoryAuthoritySnapshot authority,
            Function<InventoryEntrySnapshot, ItemIdentity> identityResolver,
            DomainEventMetadata metadata
    ) {
        QuickAccessLoadoutDefinition selected = selectedLoadout(collectionId);
        if (selected == null) {
            return null;
        }
        Set<QuickAccessLoadoutEntry> captured = LoadoutCaptureService.captureEntries(authority, identityResolver);
        repository.appendWorkflowEvent(
                new WorkflowEvent.LoadoutUpdated(collectionId, selected.id(), captured),
                (metadata == null ? DomainEventMetadata.origin("") : metadata).withOrigin("workflow.loadout.recapture")
        );
        select(collectionId, selected.id());
        notifyMutated();
        return loadout(collectionId, selected.id());
    }

    public LoadoutApplyService.LoadoutApplyPlan planSelectedLoadoutApply(
            String collectionId,
            InventoryAuthoritySnapshot authority,
            ProtectionPolicy protectionPolicy
    ) {
        return planSelectedLoadoutApply(
                collectionId,
                authority,
                protectionPolicy,
                entry -> dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(entry.stack())
        );
    }

    public LoadoutApplyService.LoadoutApplyPlan planSelectedLoadoutApply(
            String collectionId,
            InventoryAuthoritySnapshot authority,
            ProtectionPolicy protectionPolicy,
            Function<InventoryEntrySnapshot, ItemIdentity> identityResolver
    ) {
        QuickAccessLoadoutDefinition selected = selectedLoadout(collectionId);
        return selected == null
                ? LoadoutApplyService.LoadoutApplyPlan.empty("")
                : LoadoutApplyService.plan(
                        selected,
                        authority,
                        protectionPolicy,
                        dev.imagio.slot.inventory.action.InventoryActionMode.EXECUTE,
                        identityResolver
                );
    }

    public LoadoutApplyResult executeSelectedLoadoutApply(
            String collectionId,
            InventoryAuthoritySnapshot authority,
            ProtectionPolicy protectionPolicy,
            java.util.function.Function<dev.imagio.slot.inventory.action.InventoryActionRequest, dev.imagio.slot.inventory.action.InventoryActionOutcome> actionExecutor
    ) {
        return executeSelectedLoadoutApply(
                collectionId,
                authority,
                protectionPolicy,
                entry -> dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(entry.stack()),
                actionExecutor
        );
    }

    public LoadoutApplyResult executeSelectedLoadoutApply(
            String collectionId,
            InventoryAuthoritySnapshot authority,
            ProtectionPolicy protectionPolicy,
            Function<InventoryEntrySnapshot, ItemIdentity> identityResolver,
            java.util.function.Function<dev.imagio.slot.inventory.action.InventoryActionRequest, dev.imagio.slot.inventory.action.InventoryActionOutcome> actionExecutor
    ) {
        LoadoutApplyService.LoadoutApplyPlan plan = planSelectedLoadoutApply(collectionId, authority, protectionPolicy, identityResolver);
        return new LoadoutApplyExecutor(actionExecutor).execute(plan);
    }

    public boolean selectLoadout(String collectionId, String loadoutId) {
        if (collectionId == null || collectionId.isBlank() || loadoutId == null || loadoutId.isBlank()) {
            return false;
        }
        QuickAccessLoadoutDefinition existing = loadout(collectionId, loadoutId);
        InventoryBrowseSessionState state = repository.browseSessionState().current();
        if (existing == null
                || (collectionId.equals(state.selectedCollectionId()) && loadoutId.equals(state.selectedLoadoutId()))) {
            return false;
        }
        select(collectionId, loadoutId);
        notifyMutated();
        return true;
    }

    public boolean renameLoadout(String collectionId, String loadoutId, String newName) {
        return renameLoadout(collectionId, loadoutId, newName, DomainEventMetadata.origin("workflow.loadout.rename"));
    }

    public boolean renameLoadout(
            String collectionId,
            String loadoutId,
            String newName,
            DomainEventMetadata metadata
    ) {
        QuickAccessLoadoutDefinition existing = loadout(collectionId, loadoutId);
        if (existing == null) {
            return false;
        }
        String normalizedName = normalizeName(newName, "Loadout name must not be blank");
        if (existing.name().equals(normalizedName)) {
            return false;
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.LoadoutRenamed(collectionId, loadoutId, normalizedName),
                (metadata == null ? DomainEventMetadata.origin("") : metadata).withOrigin("workflow.loadout.rename")
        );
        ensureSelectedLoadout(collectionId, loadouts(collectionId));
        notifyMutated();
        return true;
    }

    public boolean deleteSelectedLoadout(String collectionId) {
        return deleteSelectedLoadout(collectionId, DomainEventMetadata.origin("workflow.loadout.delete"));
    }

    public boolean deleteSelectedLoadout(String collectionId, DomainEventMetadata metadata) {
        QuickAccessLoadoutDefinition selected = selectedLoadout(collectionId);
        if (selected == null) {
            return false;
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.LoadoutDeleted(collectionId, selected.id()),
                (metadata == null ? DomainEventMetadata.origin("") : metadata).withOrigin("workflow.loadout.delete")
        );
        ensureSelectedLoadout(collectionId, loadouts(collectionId));
        notifyMutated();
        return true;
    }

    public boolean cycleSelectedLoadout(String collectionId, int delta) {
        List<QuickAccessLoadoutDefinition> loadouts = loadouts(collectionId);
        if (loadouts.size() < 2) {
            return false;
        }

        ensureSelectedLoadout(collectionId, loadouts);
        String currentId = repository.browseSessionState().current().selectedLoadoutId();
        int currentIndex = 0;
        for (int index = 0; index < loadouts.size(); index++) {
            if (loadouts.get(index).id().equals(currentId)) {
                currentIndex = index;
                break;
            }
        }

        String nextId = loadouts.get(Math.floorMod(currentIndex + delta, loadouts.size())).id();
        if (Objects.equals(nextId, currentId)) {
            return false;
        }
        select(collectionId, nextId);
        notifyMutated();
        return true;
    }

    public boolean toggleFavorite(ItemIdentity identity) {
        return toggleFavorite(identity, DomainEventMetadata.origin("workflow.favorite.toggle"));
    }

    public boolean toggleFavorite(ItemIdentity identity, DomainEventMetadata metadata) {
        if (identity == null) {
            return false;
        }
        boolean nextState = !repository.workflowProjection().favoriteTags().contains(identity);
        repository.appendWorkflowEvent(
                nextState ? new WorkflowEvent.FavoriteMarked(identity) : new WorkflowEvent.FavoriteUnmarked(identity),
                (metadata == null ? DomainEventMetadata.origin("") : metadata).withOrigin("workflow.favorite.toggle")
        );
        notifyMutated();
        return true;
    }

    public boolean toggleJunk(ItemIdentity identity) {
        return toggleJunk(identity, DomainEventMetadata.origin("workflow.junk.toggle"));
    }

    public boolean toggleJunk(ItemIdentity identity, DomainEventMetadata metadata) {
        if (identity == null) {
            return false;
        }
        boolean nextState = !repository.workflowProjection().junkTags().contains(identity);
        repository.appendWorkflowEvent(
                nextState ? new WorkflowEvent.JunkMarked(identity) : new WorkflowEvent.JunkUnmarked(identity),
                (metadata == null ? DomainEventMetadata.origin("") : metadata).withOrigin("workflow.junk.toggle")
        );
        notifyMutated();
        return true;
    }

    public boolean toggleCollectionMembership(ItemIdentity identity, String collectionId) {
        return toggleCollectionMembership(identity, collectionId, DomainEventMetadata.origin("workflow.collection.toggle_membership"));
    }

    public boolean toggleCollectionMembership(
            ItemIdentity identity,
            String collectionId,
            DomainEventMetadata metadata
    ) {
        if (identity == null || collectionId == null || collectionId.isBlank()) {
            return false;
        }
        if (!collections().collectionIds().contains(collectionId)) {
            return false;
        }
        if (collections().memberships().getOrDefault(identity, Set.of()).contains(collectionId)) {
            repository.appendWorkflowEvent(
                    new WorkflowEvent.CollectionItemRemoved(collectionId, identity),
                    (metadata == null ? DomainEventMetadata.origin("") : metadata).withOrigin("workflow.collection.toggle_membership")
            );
        } else {
            repository.appendWorkflowEvent(
                    new WorkflowEvent.CollectionItemAdded(collectionId, identity),
                    (metadata == null ? DomainEventMetadata.origin("") : metadata).withOrigin("workflow.collection.toggle_membership")
            );
        }
        notifyMutated();
        return true;
    }

    private void notifyMutated() {
        mutationObserver.run();
    }

    private void ensureSelectedLoadout(String collectionId, List<QuickAccessLoadoutDefinition> loadouts) {
        if (loadouts == null || loadouts.isEmpty()) {
            repository.browseSessionState().update(state -> clearSelectionForCollection(state, collectionId));
            return;
        }
        InventoryBrowseSessionState state = repository.browseSessionState().current();
        String selectedId = collectionId != null && collectionId.equals(state.selectedCollectionId())
                ? state.selectedLoadoutId()
                : "";
        if (selectedId == null || selectedId.isBlank() || loadouts.stream().noneMatch(loadout -> loadout.id().equals(selectedId))) {
            select(collectionId, loadouts.getFirst().id());
        }
    }

    private void select(String collectionId, String loadoutId) {
        repository.browseSessionState().update(state -> new InventoryBrowseSessionState(
                state.filter(),
                state.sortMode(),
                state.groupingMode(),
                state.paneMode(),
                state.activePane(),
                collectionId == null ? "" : collectionId,
                loadoutId == null ? "" : loadoutId,
                state.pinnedToolId(),
                state.bulkActionScope(),
                state.selectedSubject(),
                state.expandedSectionIds()
        ));
    }

    private CollectionProjection collections() {
        return repository.workflowProjection().collections();
    }

    private CollectionDefinition definition(String collectionId) {
        return collections().userCollections().stream()
                .filter(collection -> collection.id().equals(collectionId))
                .findFirst()
                .orElse(null);
    }

    private CollectionDefinition requireCollection(String collectionId) {
        CollectionDefinition definition = definition(collectionId);
        if (definition == null) {
            throw new IllegalArgumentException("Unknown collection: " + collectionId);
        }
        return definition;
    }

    private List<QuickAccessLoadoutDefinition> loadouts(String collectionId) {
        requireCollection(collectionId);
        return collections().loadoutsByCollection().getOrDefault(collectionId, List.of());
    }

    private QuickAccessLoadoutDefinition loadout(String collectionId, String loadoutId) {
        return loadouts(collectionId).stream()
                .filter(loadout -> loadout.id().equals(loadoutId))
                .findFirst()
                .orElse(null);
    }

    private static InventoryBrowseSessionState clearSelectionForCollection(
            InventoryBrowseSessionState state,
            String collectionId
    ) {
        if (state == null) {
            return InventoryBrowseSessionState.defaults(null);
        }
        boolean clear = collectionId != null && collectionId.equals(state.selectedCollectionId());
        return new InventoryBrowseSessionState(
                state.filter(),
                state.sortMode(),
                state.groupingMode(),
                state.paneMode(),
                state.activePane(),
                clear ? "" : state.selectedCollectionId(),
                clear ? "" : state.selectedLoadoutId(),
                state.pinnedToolId(),
                state.bulkActionScope(),
                clearSelectedSubjectForCollection(state.selectedSubject(), collectionId),
                state.expandedSectionIds()
        );
    }

    private static dev.imagio.slot.inventory.browse.InventoryBrowseSubjectRef clearSelectedSubjectForCollection(
            dev.imagio.slot.inventory.browse.InventoryBrowseSubjectRef subjectRef,
            String collectionId
    ) {
        if (subjectRef instanceof dev.imagio.slot.inventory.browse.InventoryBrowseSubjectRef.PlaceholderRef placeholderRef
                && placeholderRef.collectionId().equals(collectionId)) {
            return null;
        }
        if (subjectRef instanceof dev.imagio.slot.inventory.browse.InventoryBrowseSubjectRef.LoadoutRef loadoutRef
                && loadoutRef.collectionId().equals(collectionId)) {
            return null;
        }
        return subjectRef;
    }

    private static String normalizeName(String name, String message) {
        String normalized = name == null ? "" : name.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return normalized;
    }

    private static String uniqueSlug(String value, Set<String> existingIds, String fallback) {
        String baseId = slugify(value, fallback);
        String candidate = baseId;
        int counter = 2;
        while (existingIds.contains(candidate)) {
            candidate = baseId + "-" + counter++;
        }
        return candidate;
    }

    private static String slugify(String value, String fallback) {
        String normalized = value == null ? "" : value.toLowerCase(Locale.ROOT).trim().replaceAll("[^a-z0-9]+", "-");
        normalized = normalized.replaceAll("^-+", "").replaceAll("-+$", "");
        return normalized.isEmpty() ? fallback : normalized;
    }
}
