package dev.imagio.slot.workflow.domain.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.inventory.action.InventoryActionScope;
import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.action.InventoryCommandReasonCode;
import dev.imagio.slot.inventory.browse.InventoryBrowseFilter;
import dev.imagio.slot.inventory.browse.InventoryBrowseFilterScope;
import dev.imagio.slot.inventory.browse.InventoryBrowseGroupingMode;
import dev.imagio.slot.inventory.browse.InventoryBrowsePaneMode;
import dev.imagio.slot.inventory.browse.InventoryBrowsePreferences;
import dev.imagio.slot.inventory.browse.InventoryBrowseSessionState;
import dev.imagio.slot.inventory.browse.InventoryBrowseSortMode;
import dev.imagio.slot.inventory.browse.InventoryBrowseSubjectRef;
import dev.imagio.slot.inventory.core.ItemComparisonMode;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import dev.imagio.slot.workflow.domain.ActivityProjection;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ChestLink;
import dev.imagio.slot.workflow.domain.ChestLinkMap;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.ClaimedChestMap;
import dev.imagio.slot.workflow.domain.KitMap;
import dev.imagio.slot.workflow.domain.CollectionDefinition;
import dev.imagio.slot.workflow.domain.CollectionProjection;
import dev.imagio.slot.workflow.domain.DomainEventEnvelope;
import dev.imagio.slot.workflow.domain.DomainEventStreamKind;
import dev.imagio.slot.workflow.domain.InventoryActivityConfidence;
import dev.imagio.slot.workflow.domain.InventoryActivityEvent;
import dev.imagio.slot.workflow.domain.InventoryActivityKind;
import dev.imagio.slot.workflow.domain.InventoryActivityProducer;
import dev.imagio.slot.workflow.domain.InventoryActivityRecord;
import dev.imagio.slot.workflow.domain.InventoryActivityStore;
import dev.imagio.slot.workflow.domain.LoadoutTarget;
import dev.imagio.slot.workflow.domain.ProtectionSnapshotPolicy;
import dev.imagio.slot.workflow.domain.QuickAccessLoadoutDefinition;
import dev.imagio.slot.workflow.domain.QuickAccessLoadoutEntry;
import dev.imagio.slot.workflow.domain.RecentView;
import dev.imagio.slot.workflow.domain.VisualAtlasIsland;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import dev.imagio.slot.workflow.domain.VisualHomeAssignment;
import dev.imagio.slot.workflow.domain.VisualHomeMap;
import dev.imagio.slot.workflow.domain.VisualHomeOrigin;
import dev.imagio.slot.workflow.domain.WorkflowDomainPersistencePort;
import dev.imagio.slot.workflow.domain.WorkflowDomainSnapshot;
import dev.imagio.slot.workflow.domain.WorkflowEvent;
import dev.imagio.slot.workflow.domain.WorkflowEventRecord;
import dev.imagio.slot.workflow.domain.WorkflowEventStore;
import dev.imagio.slot.workflow.domain.WorkflowProjection;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class WorkflowDomainFileStore implements WorkflowDomainPersistencePort {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final int SCHEMA_VERSION = 5;

    private final Path statePath;

    public WorkflowDomainFileStore(Path statePath) {
        this.statePath = statePath;
    }

    @Override
    public WorkflowDomainSnapshot load() {
        if (statePath == null || !Files.exists(statePath)) {
            return WorkflowDomainSnapshot.empty();
        }

        try (Reader reader = Files.newBufferedReader(statePath)) {
            StateData state = GSON.fromJson(reader, StateData.class);
            if (state == null) {
                return WorkflowDomainSnapshot.empty();
            }
            return decode(state);
        } catch (IOException | JsonParseException | IllegalArgumentException exception) {
            SlotCommon.LOGGER.warn("Failed to load SLOT workflow state from {}", statePath, exception);
            return WorkflowDomainSnapshot.empty();
        }
    }

    @Override
    public void save(WorkflowDomainSnapshot snapshot) {
        WorkflowDomainSnapshot resolved = snapshot == null ? WorkflowDomainSnapshot.empty() : snapshot;
        StateData state = encode(resolved);
        state.version = SCHEMA_VERSION;

        try {
            if (statePath != null && statePath.getParent() != null) {
                Files.createDirectories(statePath.getParent());
            }
            try (Writer writer = Files.newBufferedWriter(statePath)) {
                GSON.toJson(state, writer);
            }
        } catch (IOException exception) {
            SlotCommon.LOGGER.warn("Failed to save SLOT workflow state to {}", statePath, exception);
        }
    }

    private static StateData encode(WorkflowDomainSnapshot snapshot) {
        StateData state = new StateData();
        state.nextGlobalSequence = snapshot.nextGlobalSequence();
        state.workflowNextStreamSequence = snapshot.workflowEvents().nextStreamSequence();
        state.activityNextStreamSequence = snapshot.activityEvents().nextStreamSequence();
        state.activityMaxEvents = snapshot.activityEvents().maxEvents();
        state.workflowCheckpoint = encodeWorkflowCheckpoint(snapshot.workflowProjection());
        state.workflowEvents = snapshot.workflowEvents().records().stream()
                .map(WorkflowDomainFileStore::encodeWorkflowRecord)
                .filter(java.util.Objects::nonNull)
                .toList();
        state.activityCheckpoint = encodeActivityCheckpoint(snapshot.activityProjection());
        state.activityEvents = snapshot.activityEvents().records().stream()
                .map(WorkflowDomainFileStore::encodeActivityRecord)
                .filter(java.util.Objects::nonNull)
                .toList();

        InventoryBrowsePreferences browsePreferences = snapshot.browsePreferences();
        state.browsePreferences = new BrowsePreferencesData(
                browsePreferences.defaultSortMode().name(),
                browsePreferences.defaultGroupingMode().name(),
                browsePreferences.defaultPaneMode().name(),
                browsePreferences.defaultBulkActionScope().name()
        );

        InventoryBrowseSessionState browseSessionState = snapshot.browseSessionState();
        state.browseSession = new BrowseSessionData(
                browseSessionState.filter().searchText(),
                browseSessionState.filter().scope().name(),
                browseSessionState.sortMode().name(),
                browseSessionState.groupingMode().name(),
                browseSessionState.paneMode().name(),
                browseSessionState.activePane().name(),
                browseSessionState.selectedCollectionId(),
                browseSessionState.selectedLoadoutId(),
                browseSessionState.pinnedToolId(),
                browseSessionState.bulkActionScope().name(),
                browseSessionState.selectedSubject() == null ? "" : browseSessionState.selectedSubject().stableKey(),
                List.copyOf(browseSessionState.expandedSectionIds())
        );
        return state;
    }

    private static WorkflowDomainSnapshot decode(StateData state) {
        InventoryBrowsePreferences browsePreferences = decodeBrowsePreferences(state.browsePreferences);
        InventoryBrowseSessionState browseSessionState = decodeBrowseSession(state.browseSession, state.query, browsePreferences);

        if (state.version < 3) {
            return migrateLegacy(state, browsePreferences, browseSessionState);
        }

        WorkflowProjection.Snapshot workflowCheckpoint = decodeWorkflowCheckpoint(state.workflowCheckpoint);
        WorkflowEventStore.Snapshot workflowEvents = new WorkflowEventStore.Snapshot(
                state.workflowNextStreamSequence <= 0L ? 1L : state.workflowNextStreamSequence,
                state.workflowEvents == null ? List.of() : state.workflowEvents.stream()
                        .map(WorkflowDomainFileStore::decodeWorkflowRecord)
                        .filter(java.util.Objects::nonNull)
                        .toList()
        );
        WorkflowProjection.Snapshot workflowProjection = workflowCheckpoint;

        ActivityProjection.Snapshot activityCheckpoint = decodeActivityCheckpoint(state.activityCheckpoint);
        InventoryActivityStore.Snapshot activityEvents = new InventoryActivityStore.Snapshot(
                state.activityMaxEvents <= 0 ? InventoryActivityStore.DEFAULT_MAX_EVENTS : state.activityMaxEvents,
                state.activityNextStreamSequence <= 0L ? 1L : state.activityNextStreamSequence,
                state.activityEvents == null ? List.of() : state.activityEvents.stream()
                        .map(WorkflowDomainFileStore::decodeActivityRecord)
                        .filter(java.util.Objects::nonNull)
                        .toList()
        );
        ActivityProjection.Snapshot activityProjection = ActivityProjection.applyDismissals(
                activityCheckpoint,
                workflowProjection.recentDismissedUpToByIdentity()
        );

        return new WorkflowDomainSnapshot(
                state.nextGlobalSequence <= 0L ? 1L : state.nextGlobalSequence,
                workflowProjection,
                workflowEvents,
                activityProjection,
                activityEvents,
                browsePreferences,
                browseSessionState
        );
    }

    private static WorkflowDomainSnapshot migrateLegacy(
            StateData state,
            InventoryBrowsePreferences browsePreferences,
            InventoryBrowseSessionState browseSessionState
    ) {
        ArrayList<CollectionDefinition> collections = new ArrayList<>();
        if (state.collections != null) {
            for (CollectionData collection : state.collections) {
                if (collection == null || blank(collection.id) || blank(collection.name)) {
                    continue;
                }
                collections.add(new CollectionDefinition(collection.id, collection.name, false));
            }
        }

        LinkedHashMap<ItemIdentity, Set<String>> memberships = new LinkedHashMap<>();
        if (state.memberships != null) {
            for (MembershipData membership : state.memberships) {
                ItemIdentity identity = decodeIdentity(membership == null ? null : membership.identity);
                if (identity == null) {
                    continue;
                }
                memberships.put(identity, membership.collectionIds == null ? Set.of() : Set.copyOf(membership.collectionIds));
            }
        }

        LinkedHashMap<String, Map<ItemIdentity, Integer>> desiredCounts = new LinkedHashMap<>();
        if (state.desiredCounts != null) {
            for (DesiredCountData desiredCount : state.desiredCounts) {
                ItemIdentity identity = decodeIdentity(desiredCount == null ? null : desiredCount.identity);
                if (desiredCount == null || blank(desiredCount.collectionId) || identity == null) {
                    continue;
                }
                desiredCounts.computeIfAbsent(desiredCount.collectionId, ignored -> new LinkedHashMap<>())
                        .put(identity, Math.max(1, desiredCount.desiredCount));
            }
        }

        LinkedHashMap<String, List<QuickAccessLoadoutDefinition>> loadouts = new LinkedHashMap<>();
        if (state.loadouts != null) {
            for (LoadoutData loadout : state.loadouts) {
                if (loadout == null || blank(loadout.collectionId) || blank(loadout.id) || blank(loadout.name)) {
                    continue;
                }
                LinkedHashSet<QuickAccessLoadoutEntry> entries = new LinkedHashSet<>();
                if (loadout.entries != null) {
                    for (LoadoutEntryData entry : loadout.entries) {
                        LoadoutTarget target = decodeTarget(entry == null ? null : entry.target);
                        ItemIdentity identity = decodeIdentity(entry == null ? null : entry.identity);
                        if (target != null && identity != null) {
                            entries.add(new QuickAccessLoadoutEntry(target, identity));
                        }
                    }
                }
                if (!entries.isEmpty()) {
                    loadouts.computeIfAbsent(loadout.collectionId, ignored -> new ArrayList<>())
                            .add(new QuickAccessLoadoutDefinition(loadout.id, loadout.name, entries));
                }
            }
        }

        LinkedHashMap<ItemIdentity, Integer> recents = new LinkedHashMap<>();
        if (state.recents != null) {
            for (RecentData recent : state.recents) {
                ItemIdentity identity = decodeIdentity(recent == null ? null : recent.identity);
                if (identity != null && recent.count > 0) {
                    recents.put(identity, recent.count);
                }
            }
        }

        LinkedHashSet<ItemIdentity> protectedIdentities = new LinkedHashSet<>();
        LinkedHashSet<InventoryActionTarget> protectedTargets = new LinkedHashSet<>();
        boolean protectPortableContainers = false;
        if (state.protection != null) {
            if (state.protection.identities != null) {
                for (IdentityData identityData : state.protection.identities) {
                    ItemIdentity identity = decodeIdentity(identityData);
                    if (identity != null) {
                        protectedIdentities.add(identity);
                    }
                }
            }
            if (state.protection.targets != null) {
                for (TargetData targetData : state.protection.targets) {
                    InventoryActionTarget target = decodeInventoryTarget(targetData);
                    if (target != null) {
                        protectedTargets.add(target);
                    }
                }
            }
            protectPortableContainers = state.protection.protectPortableContainers;
        }

        WorkflowProjection.Snapshot workflowProjection = new WorkflowProjection.Snapshot(
                collections,
                memberships,
                desiredCounts,
                loadouts,
                Set.of(),
                Set.of(),
                new ProtectionSnapshotPolicy(protectedIdentities, protectedTargets, protectPortableContainers),
                Map.of(),
                VisualHomeMap.empty(),
                ClaimedChestMap.empty(),
                ChestLinkMap.empty(),
                KitMap.empty()
        );

        ActivityProjection.Snapshot activityProjection = new ActivityProjection.Snapshot(
                new RecentView(recents, Map.of()),
                List.of(),
                List.of()
        );

        return new WorkflowDomainSnapshot(
                1L,
                workflowProjection,
                WorkflowEventStore.Snapshot.empty(),
                activityProjection,
                InventoryActivityStore.Snapshot.empty(),
                browsePreferences,
                browseSessionState
        );
    }

    private static WorkflowCheckpointData encodeWorkflowCheckpoint(WorkflowProjection.Snapshot snapshot) {
        WorkflowProjection.Snapshot resolved = snapshot == null ? WorkflowProjection.Snapshot.empty() : snapshot;
        ArrayList<CollectionData> collections = new ArrayList<>();
        resolved.userCollections().forEach(collection ->
                collections.add(new CollectionData(collection.id(), collection.name()))
        );

        ArrayList<MembershipData> memberships = new ArrayList<>();
        resolved.memberships().forEach((identity, collectionIds) ->
                memberships.add(new MembershipData(identity(identity), List.copyOf(collectionIds)))
        );

        ArrayList<DesiredCountData> desiredCounts = new ArrayList<>();
        resolved.desiredCountsByCollection().forEach((collectionId, counts) ->
                counts.forEach((identity, desiredCount) ->
                        desiredCounts.add(new DesiredCountData(collectionId, identity(identity), desiredCount))
                )
        );

        ArrayList<LoadoutData> loadouts = new ArrayList<>();
        resolved.loadoutsByCollection().forEach((collectionId, definitions) -> {
            for (QuickAccessLoadoutDefinition loadout : definitions) {
                ArrayList<LoadoutEntryData> entries = new ArrayList<>();
                for (QuickAccessLoadoutEntry entry : loadout.entries()) {
                    entries.add(new LoadoutEntryData(target(entry.target()), identity(entry.identity())));
                }
                loadouts.add(new LoadoutData(collectionId, loadout.id(), loadout.name(), entries));
            }
        });

        ArrayList<ClaimedChestData> claimedChests = new ArrayList<>();
        for (ClaimedChest chest : resolved.claimedChestMap().chests()) {
            claimedChests.add(claimedChest(chest));
        }
        ArrayList<ChestLinkData> chestLinks = new ArrayList<>();
        for (ChestLink link : resolved.chestLinkMap().links()) {
            chestLinks.add(chestLink(link));
        }
        return new WorkflowCheckpointData(
                collections,
                memberships,
                desiredCounts,
                loadouts,
                resolved.favoriteTags().stream().map(WorkflowDomainFileStore::identity).toList(),
                resolved.junkTags().stream().map(WorkflowDomainFileStore::identity).toList(),
                new ProtectionData(
                        resolved.protection().protectedIdentities().stream().map(WorkflowDomainFileStore::identity).toList(),
                        resolved.protection().protectedTargets().stream().map(WorkflowDomainFileStore::target).toList(),
                        resolved.protection().protectPortableContainers()
                ),
                resolved.recentDismissedUpToByIdentity().entrySet().stream()
                        .map(entry -> new RecentDismissalData(identity(entry.getKey()), entry.getValue()))
                        .toList(),
                resolved.visualHomeMap().playerIslands().stream()
                        .map(WorkflowDomainFileStore::visualIsland)
                        .toList(),
                resolved.visualHomeMap().assignments().values().stream()
                        .map(WorkflowDomainFileStore::visualHome)
                        .toList(),
                List.copyOf(resolved.visualHomeMap().dismissedTemplateIds()),
                claimedChests,
                chestLinks
        );
    }

    private static WorkflowProjection.Snapshot decodeWorkflowCheckpoint(WorkflowCheckpointData data) {
        if (data == null) {
            return WorkflowProjection.Snapshot.empty();
        }

        ArrayList<CollectionDefinition> collections = new ArrayList<>();
        if (data.collections != null) {
            for (CollectionData collection : data.collections) {
                if (collection == null || blank(collection.id) || blank(collection.name)) {
                    continue;
                }
                collections.add(new CollectionDefinition(collection.id, collection.name, false));
            }
        }

        LinkedHashMap<ItemIdentity, Set<String>> memberships = new LinkedHashMap<>();
        if (data.memberships != null) {
            for (MembershipData membership : data.memberships) {
                ItemIdentity identity = decodeIdentity(membership == null ? null : membership.identity);
                if (identity != null) {
                    memberships.put(identity, membership.collectionIds == null ? Set.of() : Set.copyOf(membership.collectionIds));
                }
            }
        }

        LinkedHashMap<String, Map<ItemIdentity, Integer>> desiredCounts = new LinkedHashMap<>();
        if (data.desiredCounts != null) {
            for (DesiredCountData desiredCount : data.desiredCounts) {
                ItemIdentity identity = decodeIdentity(desiredCount == null ? null : desiredCount.identity);
                if (desiredCount != null && identity != null && !blank(desiredCount.collectionId)) {
                    desiredCounts.computeIfAbsent(desiredCount.collectionId, ignored -> new LinkedHashMap<>())
                            .put(identity, Math.max(1, desiredCount.desiredCount));
                }
            }
        }

        LinkedHashMap<String, List<QuickAccessLoadoutDefinition>> loadouts = new LinkedHashMap<>();
        if (data.loadouts != null) {
            for (LoadoutData loadout : data.loadouts) {
                if (loadout == null || blank(loadout.collectionId) || blank(loadout.id) || blank(loadout.name)) {
                    continue;
                }
                LinkedHashSet<QuickAccessLoadoutEntry> entries = new LinkedHashSet<>();
                if (loadout.entries != null) {
                    for (LoadoutEntryData entry : loadout.entries) {
                        LoadoutTarget target = decodeTarget(entry == null ? null : entry.target);
                        ItemIdentity identity = decodeIdentity(entry == null ? null : entry.identity);
                        if (target != null && identity != null) {
                            entries.add(new QuickAccessLoadoutEntry(target, identity));
                        }
                    }
                }
                loadouts.computeIfAbsent(loadout.collectionId, ignored -> new ArrayList<>())
                        .add(new QuickAccessLoadoutDefinition(loadout.id, loadout.name, entries));
            }
        }

        LinkedHashSet<ItemIdentity> favoriteTags = new LinkedHashSet<>();
        if (data.favoriteTags != null) {
            for (IdentityData identityData : data.favoriteTags) {
                ItemIdentity identity = decodeIdentity(identityData);
                if (identity != null) {
                    favoriteTags.add(identity);
                }
            }
        }

        LinkedHashSet<ItemIdentity> junkTags = new LinkedHashSet<>();
        if (data.junkTags != null) {
            for (IdentityData identityData : data.junkTags) {
                ItemIdentity identity = decodeIdentity(identityData);
                if (identity != null) {
                    junkTags.add(identity);
                }
            }
        }

        LinkedHashSet<ItemIdentity> protectedIdentities = new LinkedHashSet<>();
        LinkedHashSet<InventoryActionTarget> protectedTargets = new LinkedHashSet<>();
        boolean protectPortableContainers = false;
        if (data.protection != null) {
            if (data.protection.identities != null) {
                for (IdentityData identityData : data.protection.identities) {
                    ItemIdentity identity = decodeIdentity(identityData);
                    if (identity != null) {
                        protectedIdentities.add(identity);
                    }
                }
            }
            if (data.protection.targets != null) {
                for (TargetData targetData : data.protection.targets) {
                    InventoryActionTarget target = decodeInventoryTarget(targetData);
                    if (target != null) {
                        protectedTargets.add(target);
                    }
                }
            }
            protectPortableContainers = data.protection.protectPortableContainers;
        }

        LinkedHashMap<ItemIdentity, Long> recentDismissals = new LinkedHashMap<>();
        if (data.recentDismissals != null) {
            for (RecentDismissalData dismissal : data.recentDismissals) {
                ItemIdentity identity = decodeIdentity(dismissal == null ? null : dismissal.identity);
                if (identity != null && dismissal != null) {
                    recentDismissals.put(identity, Math.max(0L, dismissal.dismissedUpToSequence));
                }
            }
        }

        ArrayList<VisualAtlasIsland> playerIslands = new ArrayList<>();
        if (data.visualIslands != null) {
            for (VisualIslandData islandData : data.visualIslands) {
                VisualAtlasIsland island = decodeVisualIsland(islandData);
                if (island != null) {
                    playerIslands.add(island);
                }
            }
        }

        LinkedHashMap<ItemIdentity, VisualHomeAssignment> visualHomes = new LinkedHashMap<>();
        if (data.visualHomes != null) {
            for (VisualHomeData homeData : data.visualHomes) {
                VisualHomeAssignment home = decodeVisualHome(homeData);
                if (home != null) {
                    visualHomes.put(home.identity(), home);
                }
            }
        }

        LinkedHashSet<String> dismissedTemplateIds = new LinkedHashSet<>();
        if (data.dismissedTemplateIds != null) {
            for (String templateId : data.dismissedTemplateIds) {
                if (templateId != null && !templateId.isBlank()) {
                    dismissedTemplateIds.add(templateId);
                }
            }
        }

        ArrayList<ClaimedChest> claimedChests = new ArrayList<>();
        if (data.claimedChests != null) {
            for (ClaimedChestData chestData : data.claimedChests) {
                ClaimedChest chest = decodeClaimedChest(chestData);
                if (chest != null) {
                    claimedChests.add(chest);
                }
            }
        }
        LinkedHashSet<ChestLink> chestLinks = new LinkedHashSet<>();
        if (data.chestLinks != null) {
            for (ChestLinkData linkData : data.chestLinks) {
                ChestLink link = decodeChestLink(linkData);
                if (link != null) {
                    chestLinks.add(link);
                }
            }
        }

        return new WorkflowProjection.Snapshot(
                collections,
                memberships,
                desiredCounts,
                loadouts,
                favoriteTags,
                junkTags,
                new ProtectionSnapshotPolicy(protectedIdentities, protectedTargets, protectPortableContainers),
                recentDismissals,
                new VisualHomeMap(playerIslands, visualHomes, dismissedTemplateIds),
                new ClaimedChestMap(claimedChests),
                new ChestLinkMap(chestLinks),
                KitMap.empty()
        );
    }

    private static ActivityCheckpointData encodeActivityCheckpoint(ActivityProjection.Snapshot snapshot) {
        ActivityProjection.Snapshot resolved = snapshot == null ? ActivityProjection.Snapshot.empty() : snapshot;
        ArrayList<RecentData> recents = new ArrayList<>();
        resolved.recents().countsByIdentity().forEach((identity, count) ->
                recents.add(new RecentData(identity(identity), count, resolved.recents().latestSequenceByIdentity().getOrDefault(identity, 0L)))
        );
        return new ActivityCheckpointData(
                recents,
                resolved.cleanupCandidates().stream().map(WorkflowDomainFileStore::encodeActivityRecord).toList(),
                resolved.undoCandidates().stream().map(WorkflowDomainFileStore::encodeActivityRecord).toList()
        );
    }

    private static ActivityProjection.Snapshot decodeActivityCheckpoint(ActivityCheckpointData data) {
        if (data == null) {
            return ActivityProjection.Snapshot.empty();
        }
        LinkedHashMap<ItemIdentity, Integer> recentCounts = new LinkedHashMap<>();
        LinkedHashMap<ItemIdentity, Long> recentSequences = new LinkedHashMap<>();
        if (data.recents != null) {
            for (RecentData recent : data.recents) {
                ItemIdentity identity = decodeIdentity(recent == null ? null : recent.identity);
                if (identity != null && recent.count > 0) {
                    recentCounts.put(identity, recent.count);
                    recentSequences.put(identity, Math.max(0L, recent.latestSequence));
                }
            }
        }
        List<InventoryActivityRecord> cleanup = data.cleanupCandidates == null ? List.of() : data.cleanupCandidates.stream()
                .map(WorkflowDomainFileStore::decodeActivityRecord)
                .filter(java.util.Objects::nonNull)
                .toList();
        List<InventoryActivityRecord> undo = data.undoCandidates == null ? List.of() : data.undoCandidates.stream()
                .map(WorkflowDomainFileStore::decodeActivityRecord)
                .filter(java.util.Objects::nonNull)
                .toList();
        return new ActivityProjection.Snapshot(new RecentView(recentCounts, recentSequences), cleanup, undo);
    }

    private static WorkflowEventData encodeWorkflowRecord(WorkflowEventRecord record) {
        if (record == null || record.event() == null) {
            return null;
        }
        DomainEventEnvelope envelope = record.envelope();
        WorkflowEventData data = new WorkflowEventData();
        data.envelope = encodeEnvelope(envelope);
        switch (record.event()) {
            case WorkflowEvent.CollectionCreated event -> {
                data.kind = "CollectionCreated";
                data.collectionId = event.collectionId();
                data.name = event.name();
            }
            case WorkflowEvent.CollectionRenamed event -> {
                data.kind = "CollectionRenamed";
                data.collectionId = event.collectionId();
                data.name = event.name();
            }
            case WorkflowEvent.CollectionDeleted event -> {
                data.kind = "CollectionDeleted";
                data.collectionId = event.collectionId();
            }
            case WorkflowEvent.CollectionItemAdded event -> {
                data.kind = "CollectionItemAdded";
                data.collectionId = event.collectionId();
                data.identity = identity(event.identity());
            }
            case WorkflowEvent.CollectionItemRemoved event -> {
                data.kind = "CollectionItemRemoved";
                data.collectionId = event.collectionId();
                data.identity = identity(event.identity());
            }
            case WorkflowEvent.DesiredCountSet event -> {
                data.kind = "DesiredCountSet";
                data.collectionId = event.collectionId();
                data.identity = identity(event.identity());
                data.desiredCount = event.desiredCount();
            }
            case WorkflowEvent.LoadoutCreated event -> {
                data.kind = "LoadoutCreated";
                data.collectionId = event.collectionId();
                data.loadout = encodeLoadout(event.loadout(), event.collectionId());
            }
            case WorkflowEvent.LoadoutRenamed event -> {
                data.kind = "LoadoutRenamed";
                data.collectionId = event.collectionId();
                data.loadoutId = event.loadoutId();
                data.name = event.name();
            }
            case WorkflowEvent.LoadoutUpdated event -> {
                data.kind = "LoadoutUpdated";
                data.collectionId = event.collectionId();
                data.loadoutId = event.loadoutId();
                data.loadoutEntries = encodeLoadoutEntries(event.entries());
            }
            case WorkflowEvent.LoadoutDeleted event -> {
                data.kind = "LoadoutDeleted";
                data.collectionId = event.collectionId();
                data.loadoutId = event.loadoutId();
            }
            case WorkflowEvent.FavoriteMarked event -> {
                data.kind = "FavoriteMarked";
                data.identity = identity(event.identity());
            }
            case WorkflowEvent.FavoriteUnmarked event -> {
                data.kind = "FavoriteUnmarked";
                data.identity = identity(event.identity());
            }
            case WorkflowEvent.JunkMarked event -> {
                data.kind = "JunkMarked";
                data.identity = identity(event.identity());
            }
            case WorkflowEvent.JunkUnmarked event -> {
                data.kind = "JunkUnmarked";
                data.identity = identity(event.identity());
            }
            case WorkflowEvent.ProtectedIdentityMarked event -> {
                data.kind = "ProtectedIdentityMarked";
                data.identity = identity(event.identity());
            }
            case WorkflowEvent.ProtectedIdentityUnmarked event -> {
                data.kind = "ProtectedIdentityUnmarked";
                data.identity = identity(event.identity());
            }
            case WorkflowEvent.ProtectedTargetMarked event -> {
                data.kind = "ProtectedTargetMarked";
                data.target = target(event.target());
            }
            case WorkflowEvent.ProtectedTargetUnmarked event -> {
                data.kind = "ProtectedTargetUnmarked";
                data.target = target(event.target());
            }
            case WorkflowEvent.PortableContainerProtectionSet event -> {
                data.kind = "PortableContainerProtectionSet";
                data.enabled = event.enabled();
            }
            case WorkflowEvent.RecentDismissedUpTo event -> {
                data.kind = "RecentDismissedUpTo";
                data.identity = identity(event.identity());
                data.sequence = event.dismissedUpToGlobalSequence();
            }
            case WorkflowEvent.VisualIslandCreated event -> {
                data.kind = "VisualIslandCreated";
                data.visualIsland = visualIsland(event.island());
            }
            case WorkflowEvent.VisualIslandMoved event -> {
                data.kind = "VisualIslandMoved";
                data.islandId = event.islandId();
                data.x = event.x();
                data.y = event.y();
            }
            case WorkflowEvent.VisualHomeAssigned event -> {
                data.kind = "VisualHomeAssigned";
                data.visualHome = visualHome(event.assignment());
            }
            case WorkflowEvent.VisualHomeCleared event -> {
                data.kind = "VisualHomeCleared";
                data.identity = identity(event.identity());
            }
            case WorkflowEvent.VisualIslandRenamed event -> {
                data.kind = "VisualIslandRenamed";
                data.islandId = event.islandId();
                data.label = event.label();
            }
            case WorkflowEvent.VisualIslandRecolored event -> {
                data.kind = "VisualIslandRecolored";
                data.islandId = event.islandId();
                data.color = event.color();
            }
            case WorkflowEvent.VisualIslandIconChanged event -> {
                data.kind = "VisualIslandIconChanged";
                data.islandId = event.islandId();
                data.iconIdentity = identity(event.iconIdentity());
            }
            case WorkflowEvent.VisualIslandDeleted event -> {
                data.kind = "VisualIslandDeleted";
                data.islandId = event.islandId();
            }
            case WorkflowEvent.TemplateIslandDismissed event -> {
                data.kind = "TemplateIslandDismissed";
                data.templateId = event.templateId();
            }
            case WorkflowEvent.ClaimedChestCreated event -> {
                data.kind = "ClaimedChestCreated";
                data.claimedChest = claimedChest(event.chest());
                if (event.chest() != null) {
                    data.storageId = event.chest().storageId().toString();
                }
            }
            case WorkflowEvent.ClaimedChestMoved event -> {
                data.kind = "ClaimedChestMoved";
                data.storageId = event.storageId() == null ? "" : event.storageId().toString();
                data.x = event.atlasX();
                data.y = event.atlasY();
            }
            case WorkflowEvent.ClaimedChestAnchorsChanged event -> {
                data.kind = "ClaimedChestAnchorsChanged";
                data.storageId = event.storageId() == null ? "" : event.storageId().toString();
                ArrayList<ChestAnchorData> anchors = new ArrayList<>();
                if (event.anchors() != null) {
                    for (ChestAnchor anchor : event.anchors()) {
                        anchors.add(chestAnchor(anchor));
                    }
                }
                data.anchors = anchors;
            }
            case WorkflowEvent.ClaimedChestRelabeled event -> {
                data.kind = "ClaimedChestRelabeled";
                data.storageId = event.storageId() == null ? "" : event.storageId().toString();
                data.label = event.label();
            }
            case WorkflowEvent.ClaimedChestDeleted event -> {
                data.kind = "ClaimedChestDeleted";
                data.storageId = event.storageId() == null ? "" : event.storageId().toString();
            }
            case WorkflowEvent.ChestLinkCreated event -> {
                data.kind = "ChestLinkCreated";
                data.islandId = event.islandId();
                data.storageId = event.storageId() == null ? "" : event.storageId().toString();
            }
            case WorkflowEvent.ChestLinkRemoved event -> {
                data.kind = "ChestLinkRemoved";
                data.islandId = event.islandId();
                data.storageId = event.storageId() == null ? "" : event.storageId().toString();
            }
            case WorkflowEvent.KitCreated event -> {
                data.kind = "KitCreated";
            }
            case WorkflowEvent.KitUpdated event -> {
                data.kind = "KitUpdated";
            }
            case WorkflowEvent.KitDeleted event -> {
                data.kind = "KitDeleted";
            }
            case WorkflowEvent.KitActivated event -> {
                data.kind = "KitActivated";
            }
            case WorkflowEvent.KitDeactivated event -> {
                data.kind = "KitDeactivated";
            }
            case WorkflowEvent.KitPageSwitched event -> {
                data.kind = "KitPageSwitched";
            }
        }
        if (data.kind != null && data.kind.startsWith("Kit")) {
            return null;
        }
        return data;
    }

    private static WorkflowEventRecord decodeWorkflowRecord(WorkflowEventData data) {
        if (data == null || blank(data.kind)) {
            return null;
        }
        DomainEventEnvelope envelope = decodeEnvelope(data.envelope, DomainEventStreamKind.WORKFLOW);
        WorkflowEvent event = switch (data.kind) {
            case "CollectionCreated" -> new WorkflowEvent.CollectionCreated(nonNull(data.collectionId), nonNull(data.name));
            case "CollectionRenamed" -> new WorkflowEvent.CollectionRenamed(nonNull(data.collectionId), nonNull(data.name));
            case "CollectionDeleted" -> new WorkflowEvent.CollectionDeleted(nonNull(data.collectionId));
            case "CollectionItemAdded" -> new WorkflowEvent.CollectionItemAdded(nonNull(data.collectionId), decodeIdentity(data.identity));
            case "CollectionItemRemoved" -> new WorkflowEvent.CollectionItemRemoved(nonNull(data.collectionId), decodeIdentity(data.identity));
            case "DesiredCountSet" -> new WorkflowEvent.DesiredCountSet(nonNull(data.collectionId), decodeIdentity(data.identity), data.desiredCount);
            case "LoadoutCreated" -> new WorkflowEvent.LoadoutCreated(nonNull(data.collectionId), decodeLoadout(data.loadout));
            case "LoadoutRenamed" -> new WorkflowEvent.LoadoutRenamed(nonNull(data.collectionId), nonNull(data.loadoutId), nonNull(data.name));
            case "LoadoutUpdated" -> new WorkflowEvent.LoadoutUpdated(nonNull(data.collectionId), nonNull(data.loadoutId), decodeLoadoutEntries(data.loadoutEntries));
            case "LoadoutDeleted" -> new WorkflowEvent.LoadoutDeleted(nonNull(data.collectionId), nonNull(data.loadoutId));
            case "FavoriteMarked" -> new WorkflowEvent.FavoriteMarked(decodeIdentity(data.identity));
            case "FavoriteUnmarked" -> new WorkflowEvent.FavoriteUnmarked(decodeIdentity(data.identity));
            case "JunkMarked" -> new WorkflowEvent.JunkMarked(decodeIdentity(data.identity));
            case "JunkUnmarked" -> new WorkflowEvent.JunkUnmarked(decodeIdentity(data.identity));
            case "ProtectedIdentityMarked" -> new WorkflowEvent.ProtectedIdentityMarked(decodeIdentity(data.identity));
            case "ProtectedIdentityUnmarked" -> new WorkflowEvent.ProtectedIdentityUnmarked(decodeIdentity(data.identity));
            case "ProtectedTargetMarked" -> new WorkflowEvent.ProtectedTargetMarked(decodeInventoryTarget(data.target));
            case "ProtectedTargetUnmarked" -> new WorkflowEvent.ProtectedTargetUnmarked(decodeInventoryTarget(data.target));
            case "PortableContainerProtectionSet" -> new WorkflowEvent.PortableContainerProtectionSet(data.enabled);
            case "RecentDismissedUpTo" -> new WorkflowEvent.RecentDismissedUpTo(decodeIdentity(data.identity), data.sequence);
            case "VisualIslandCreated" -> new WorkflowEvent.VisualIslandCreated(decodeVisualIsland(data.visualIsland));
            case "VisualIslandMoved" -> new WorkflowEvent.VisualIslandMoved(nonNull(data.islandId), data.x, data.y);
            case "VisualHomeAssigned" -> new WorkflowEvent.VisualHomeAssigned(decodeVisualHome(data.visualHome));
            case "VisualHomeCleared" -> new WorkflowEvent.VisualHomeCleared(decodeIdentity(data.identity));
            case "VisualIslandRenamed" -> new WorkflowEvent.VisualIslandRenamed(nonNull(data.islandId), nonNull(data.label));
            case "VisualIslandRecolored" -> new WorkflowEvent.VisualIslandRecolored(nonNull(data.islandId), data.color);
            case "VisualIslandIconChanged" -> new WorkflowEvent.VisualIslandIconChanged(nonNull(data.islandId), decodeIdentity(data.iconIdentity));
            case "VisualIslandDeleted" -> new WorkflowEvent.VisualIslandDeleted(nonNull(data.islandId));
            case "TemplateIslandDismissed" -> new WorkflowEvent.TemplateIslandDismissed(nonNull(data.templateId));
            case "ClaimedChestCreated" -> {
                ClaimedChest chest = decodeClaimedChest(data.claimedChest);
                yield chest == null ? null : new WorkflowEvent.ClaimedChestCreated(chest);
            }
            case "ClaimedChestMoved" -> {
                UUID storageId = parseUuid(data.storageId);
                yield storageId == null ? null : new WorkflowEvent.ClaimedChestMoved(storageId, data.x, data.y);
            }
            case "ClaimedChestAnchorsChanged" -> {
                UUID storageId = parseUuid(data.storageId);
                if (storageId == null) {
                    yield null;
                }
                LinkedHashSet<ChestAnchor> anchors = new LinkedHashSet<>();
                if (data.anchors != null) {
                    for (ChestAnchorData anchorData : data.anchors) {
                        ChestAnchor anchor = decodeChestAnchor(anchorData);
                        if (anchor != null) {
                            anchors.add(anchor);
                        }
                    }
                }
                yield new WorkflowEvent.ClaimedChestAnchorsChanged(storageId, anchors);
            }
            case "ClaimedChestRelabeled" -> {
                UUID storageId = parseUuid(data.storageId);
                yield storageId == null ? null : new WorkflowEvent.ClaimedChestRelabeled(storageId, nonNull(data.label));
            }
            case "ClaimedChestDeleted" -> {
                UUID storageId = parseUuid(data.storageId);
                yield storageId == null ? null : new WorkflowEvent.ClaimedChestDeleted(storageId);
            }
            case "ChestLinkCreated" -> {
                UUID storageId = parseUuid(data.storageId);
                yield storageId == null || blank(data.islandId) ? null
                        : new WorkflowEvent.ChestLinkCreated(data.islandId, storageId);
            }
            case "ChestLinkRemoved" -> {
                UUID storageId = parseUuid(data.storageId);
                yield storageId == null || blank(data.islandId) ? null
                        : new WorkflowEvent.ChestLinkRemoved(data.islandId, storageId);
            }
            default -> null;
        };
        return event == null ? null : new WorkflowEventRecord(envelope, event);
    }

    private static ActivityEventData encodeActivityRecord(InventoryActivityRecord record) {
        if (record == null || record.event() == null) {
            return null;
        }
        ActivityEventData data = new ActivityEventData();
        data.envelope = encodeEnvelope(record.envelope());
        data.kind = record.event().kind().name();
        data.producer = record.event().producer().name();
        data.confidence = record.event().confidence().name();
        data.identity = identity(record.event().identity());
        data.count = record.event().count();
        data.fromTarget = target(record.event().fromTarget());
        data.toTarget = target(record.event().toTarget());
        data.requestId = record.event().requestId();
        data.recoveryToken = record.event().recoveryToken();
        data.reasonCodes = record.event().reasonCodes().stream().map(Enum::name).toList();
        data.diagnostics = record.event().diagnostics();
        return data;
    }

    private static InventoryActivityRecord decodeActivityRecord(ActivityEventData data) {
        if (data == null || blank(data.kind)) {
            return null;
        }
        return new InventoryActivityRecord(
                decodeEnvelope(data.envelope, DomainEventStreamKind.ACTIVITY),
                new InventoryActivityEvent(
                        decodeEnum(InventoryActivityKind.class, data.kind, InventoryActivityKind.ACQUIRED),
                        decodeEnum(InventoryActivityProducer.class, data.producer, InventoryActivityProducer.UNKNOWN_EXTERNAL),
                        decodeEnum(InventoryActivityConfidence.class, data.confidence, InventoryActivityConfidence.OBSERVED),
                        decodeIdentity(data.identity),
                        data.count,
                        decodeInventoryTarget(data.fromTarget),
                        decodeInventoryTarget(data.toTarget),
                        nonNull(data.requestId),
                        nonNull(data.recoveryToken),
                        data.reasonCodes == null ? List.of() : data.reasonCodes.stream()
                                .map(raw -> decodeEnum(InventoryCommandReasonCode.class, raw, InventoryCommandReasonCode.UNKNOWN))
                                .toList(),
                        nonNull(data.diagnostics)
                )
        );
    }

    private static EnvelopeData encodeEnvelope(DomainEventEnvelope envelope) {
        DomainEventEnvelope resolved = envelope == null ? DomainEventEnvelope.empty(DomainEventStreamKind.WORKFLOW) : envelope;
        return new EnvelopeData(
                resolved.globalSequence(),
                resolved.streamSequence(),
                resolved.streamKind().name(),
                resolved.occurredAtEpochMillis(),
                resolved.origin(),
                resolved.correlationId(),
                resolved.causationId(),
                resolved.sessionId()
        );
    }

    private static DomainEventEnvelope decodeEnvelope(EnvelopeData data, DomainEventStreamKind fallbackKind) {
        if (data == null) {
            return DomainEventEnvelope.empty(fallbackKind);
        }
        return new DomainEventEnvelope(
                Math.max(0L, data.globalSequence),
                Math.max(0L, data.streamSequence),
                decodeEnum(DomainEventStreamKind.class, data.streamKind, fallbackKind),
                Math.max(0L, data.occurredAtEpochMillis),
                nonNull(data.origin),
                nonNull(data.correlationId),
                nonNull(data.causationId),
                nonNull(data.sessionId)
        );
    }

    private static LoadoutData encodeLoadout(QuickAccessLoadoutDefinition loadout, String collectionId) {
        if (loadout == null) {
            return null;
        }
        return new LoadoutData(collectionId, loadout.id(), loadout.name(), encodeLoadoutEntries(loadout.entries()));
    }

    private static QuickAccessLoadoutDefinition decodeLoadout(LoadoutData data) {
        if (data == null || blank(data.id) || blank(data.name)) {
            return null;
        }
        return new QuickAccessLoadoutDefinition(data.id, data.name, decodeLoadoutEntries(data.entries));
    }

    private static List<LoadoutEntryData> encodeLoadoutEntries(Set<QuickAccessLoadoutEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        ArrayList<LoadoutEntryData> encoded = new ArrayList<>();
        for (QuickAccessLoadoutEntry entry : entries) {
            encoded.add(new LoadoutEntryData(target(entry.target()), identity(entry.identity())));
        }
        return List.copyOf(encoded);
    }

    private static Set<QuickAccessLoadoutEntry> decodeLoadoutEntries(List<LoadoutEntryData> entries) {
        if (entries == null || entries.isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<QuickAccessLoadoutEntry> decoded = new LinkedHashSet<>();
        for (LoadoutEntryData entry : entries) {
            LoadoutTarget target = decodeTarget(entry == null ? null : entry.target);
            ItemIdentity identity = decodeIdentity(entry == null ? null : entry.identity);
            if (target != null && identity != null) {
                decoded.add(new QuickAccessLoadoutEntry(target, identity));
            }
        }
        return Set.copyOf(decoded);
    }

    private static InventoryBrowsePreferences decodeBrowsePreferences(BrowsePreferencesData data) {
        if (data == null) {
            return InventoryBrowsePreferences.defaults();
        }
        return new InventoryBrowsePreferences(
                decodeEnum(InventoryBrowseSortMode.class, data.defaultSortMode, InventoryBrowseSortMode.NAME),
                decodeEnum(InventoryBrowseGroupingMode.class, data.defaultGroupingMode, InventoryBrowseGroupingMode.FLAT),
                decodeEnum(InventoryBrowsePaneMode.class, data.defaultPaneMode, InventoryBrowsePaneMode.CARRIED_ONLY),
                decodeEnum(InventoryActionScope.class, data.defaultBulkActionScope, InventoryActionScope.VISIBLE_MATCHES)
        );
    }

    private static InventoryBrowseSessionState decodeBrowseSession(
            BrowseSessionData data,
            QueryStateData legacyQuery,
            InventoryBrowsePreferences preferences
    ) {
        if (data != null) {
            return new InventoryBrowseSessionState(
                    new InventoryBrowseFilter(
                            nonNull(data.searchText),
                            decodeEnum(InventoryBrowseFilterScope.class, data.filterScope, InventoryBrowseFilterScope.ALL)
                    ),
                    decodeEnum(InventoryBrowseSortMode.class, data.sortMode, preferences.defaultSortMode()),
                    decodeEnum(InventoryBrowseGroupingMode.class, data.groupingMode, preferences.defaultGroupingMode()),
                    decodeEnum(InventoryBrowsePaneMode.class, data.paneMode, preferences.defaultPaneMode()),
                    decodeEnum(InventoryPaneMembership.class, data.activePane, InventoryPaneMembership.CARRIED),
                    nonNull(data.selectedCollectionId),
                    nonNull(data.selectedLoadoutId),
                    nonNull(data.pinnedToolId),
                    decodeEnum(InventoryActionScope.class, data.bulkActionScope, preferences.defaultBulkActionScope()),
                    InventoryBrowseSubjectRef.parse(data.selectedSubjectKey),
                    data.expandedSectionIds == null ? Set.of() : Set.copyOf(data.expandedSectionIds)
            );
        }
        if (legacyQuery != null) {
            return new InventoryBrowseSessionState(
                    new InventoryBrowseFilter(
                            nonNull(legacyQuery.searchText),
                            decodeEnum(InventoryBrowseFilterScope.class, legacyQuery.filterScope, InventoryBrowseFilterScope.ALL)
                    ),
                    decodeEnum(InventoryBrowseSortMode.class, legacyQuery.sortMode, preferences.defaultSortMode()),
                    preferences.defaultGroupingMode(),
                    decodeEnum(InventoryBrowsePaneMode.class, legacyQuery.activePane, preferences.defaultPaneMode()),
                    decodeEnum(InventoryPaneMembership.class, legacyQuery.activePane, InventoryPaneMembership.CARRIED),
                    nonNull(legacyQuery.selectedCollectionId),
                    nonNull(legacyQuery.selectedLoadoutId),
                    nonNull(legacyQuery.pinnedToolId),
                    decodeEnum(InventoryActionScope.class, legacyQuery.bulkActionScope, preferences.defaultBulkActionScope()),
                    null,
                    decodeLegacyExpandedSections(legacyQuery.persistentSectionState)
            );
        }
        return InventoryBrowseSessionState.defaults(preferences);
    }

    private static IdentityData identity(ItemIdentity identity) {
        return identity == null ? null : new IdentityData(identity.itemId(), identity.comparisonMode().name(), identity.componentFingerprint());
    }

    private static VisualIslandData visualIsland(VisualAtlasIsland island) {
        if (island == null) {
            return null;
        }
        return new VisualIslandData(
                island.id(),
                island.label(),
                island.kind().name(),
                island.x(),
                island.y(),
                island.width(),
                island.height(),
                island.color(),
                identity(island.iconIdentity())
        );
    }

    private static VisualAtlasIsland decodeVisualIsland(VisualIslandData data) {
        if (data == null || blank(data.id)) {
            return null;
        }
        return new VisualAtlasIsland(
                data.id,
                nonNull(data.label),
                decodeEnum(VisualAtlasIslandKind.class, data.kind, VisualAtlasIslandKind.PLAYER),
                data.x,
                data.y,
                data.width,
                data.height,
                data.color,
                decodeIdentity(data.iconIdentity)
        );
    }

    private static ClaimedChestData claimedChest(ClaimedChest chest) {
        if (chest == null) {
            return null;
        }
        ArrayList<ChestAnchorData> anchors = new ArrayList<>();
        for (ChestAnchor anchor : chest.anchors()) {
            ChestAnchorData anchorData = chestAnchor(anchor);
            if (anchorData != null) {
                anchors.add(anchorData);
            }
        }
        return new ClaimedChestData(
                chest.storageId().toString(),
                anchors,
                chest.atlasX(),
                chest.atlasY(),
                chest.label()
        );
    }

    private static ClaimedChest decodeClaimedChest(ClaimedChestData data) {
        if (data == null) {
            return null;
        }
        UUID storageId = parseUuid(data.storageId);
        if (storageId == null) {
            return null;
        }
        LinkedHashSet<ChestAnchor> anchors = new LinkedHashSet<>();
        if (data.anchors != null) {
            for (ChestAnchorData anchorData : data.anchors) {
                ChestAnchor anchor = decodeChestAnchor(anchorData);
                if (anchor != null) {
                    anchors.add(anchor);
                }
            }
        }
        if (anchors.isEmpty()) {
            return null;
        }
        return new ClaimedChest(storageId, anchors, data.atlasX, data.atlasY, nonNull(data.label));
    }

    private static ChestAnchorData chestAnchor(ChestAnchor anchor) {
        return anchor == null ? null : new ChestAnchorData(anchor.dimensionId(), anchor.x(), anchor.y(), anchor.z());
    }

    private static ChestAnchor decodeChestAnchor(ChestAnchorData data) {
        if (data == null || blank(data.dimensionId)) {
            return null;
        }
        return new ChestAnchor(data.dimensionId, data.x, data.y, data.z);
    }

    private static ChestLinkData chestLink(ChestLink link) {
        if (link == null) {
            return null;
        }
        return new ChestLinkData(link.islandId(), link.storageId().toString());
    }

    private static ChestLink decodeChestLink(ChestLinkData data) {
        if (data == null || blank(data.islandId)) {
            return null;
        }
        UUID storageId = parseUuid(data.storageId);
        if (storageId == null) {
            return null;
        }
        return new ChestLink(data.islandId, storageId);
    }

    private static UUID parseUuid(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static VisualHomeData visualHome(VisualHomeAssignment assignment) {
        if (assignment == null) {
            return null;
        }
        return new VisualHomeData(
                identity(assignment.identity()),
                assignment.islandId(),
                assignment.localX(),
                assignment.localY(),
                assignment.origin().name(),
                assignment.locked()
        );
    }

    private static VisualHomeAssignment decodeVisualHome(VisualHomeData data) {
        ItemIdentity identity = decodeIdentity(data == null ? null : data.identity);
        if (identity == null || data == null || blank(data.islandId)) {
            return null;
        }
        return new VisualHomeAssignment(
                identity,
                data.islandId,
                data.x,
                data.y,
                decodeEnum(VisualHomeOrigin.class, data.origin, VisualHomeOrigin.PLAYER_PLACED),
                data.locked
        );
    }

    private static TargetData target(LoadoutTarget target) {
        return switch (target) {
            case LoadoutTarget.QuickAccessLaneTarget laneTarget ->
                    new TargetData("quick_access", laneTarget.laneId(), "", laneTarget.slotIndex());
            case LoadoutTarget.EquipmentSlotTarget equipmentTarget ->
                    new TargetData("equipment", "", equipmentTarget.groupId(), equipmentTarget.slotIndex());
        };
    }

    private static TargetData target(InventoryActionTarget target) {
        if (target == null) {
            return null;
        }
        return switch (target) {
            case InventoryActionTarget.CursorTarget ignored ->
                    new TargetData("cursor", "", "", 0);
            case InventoryActionTarget.SourceTarget sourceTarget ->
                    new TargetData("source_scope", sourceTarget.sourceId(), "", -1);
            case InventoryActionTarget.SourceSlotTarget slotTarget ->
                    new TargetData("source", slotTarget.sourceId(), "", slotTarget.slotIndex());
            case InventoryActionTarget.SourceEntryTarget sourceEntryTarget ->
                    new TargetData("source_entry", sourceEntryTarget.sourceId(), sourceEntryTarget.entryId(), 0);
            case InventoryActionTarget.QuickAccessTarget quickAccessTarget ->
                    new TargetData("quick_access", quickAccessTarget.laneId(), "", quickAccessTarget.slotIndex());
            case InventoryActionTarget.EquipmentTarget equipmentTarget ->
                    new TargetData("equipment", "", equipmentTarget.groupId(), equipmentTarget.slotIndex());
            case InventoryActionTarget.ToolRegionTarget toolRegionTarget ->
                    new TargetData("tool_region", toolRegionTarget.toolId() + "|" + toolRegionTarget.regionId(), "", toolRegionTarget.slotIndex());
            case InventoryActionTarget.ToolControlTarget toolControlTarget ->
                    new TargetData("tool_control", toolControlTarget.toolId() + "|" + toolControlTarget.controlId(), "", 0);
        };
    }

    private static ItemIdentity decodeIdentity(IdentityData data) {
        if (data == null || blank(data.itemId)) {
            return null;
        }
        ItemComparisonMode comparisonMode = decodeEnum(ItemComparisonMode.class, data.comparisonMode, ItemComparisonMode.ITEM_ID);
        return new ItemIdentity(data.itemId, comparisonMode, nonNull(data.componentFingerprint));
    }

    private static LoadoutTarget decodeTarget(TargetData data) {
        if (data == null || blank(data.kind)) {
            return null;
        }
        return switch (data.kind) {
            case "quick_access" -> new LoadoutTarget.QuickAccessLaneTarget(nonNull(data.primaryId), data.slotIndex);
            case "equipment" -> new LoadoutTarget.EquipmentSlotTarget(nonNull(data.secondaryId), data.slotIndex);
            default -> null;
        };
    }

    private static InventoryActionTarget decodeInventoryTarget(TargetData data) {
        if (data == null || blank(data.kind)) {
            return null;
        }
        return switch (data.kind) {
            case "cursor" -> new InventoryActionTarget.CursorTarget();
            case "source_scope" -> new InventoryActionTarget.SourceTarget(nonNull(data.primaryId));
            case "source" -> data.slotIndex < 0
                    ? new InventoryActionTarget.SourceTarget(nonNull(data.primaryId))
                    : new InventoryActionTarget.SourceSlotTarget(nonNull(data.primaryId), data.slotIndex);
            case "source_entry" -> new InventoryActionTarget.SourceEntryTarget(nonNull(data.primaryId), nonNull(data.secondaryId));
            case "quick_access" -> new InventoryActionTarget.QuickAccessTarget(nonNull(data.primaryId), data.slotIndex);
            case "equipment" -> new InventoryActionTarget.EquipmentTarget(nonNull(data.secondaryId), data.slotIndex);
            case "tool_region" -> {
                String[] parts = nonNull(data.primaryId).split("\\|", 2);
                yield new InventoryActionTarget.ToolRegionTarget(parts.length > 0 ? parts[0] : "", parts.length > 1 ? parts[1] : "", data.slotIndex);
            }
            case "tool_control" -> {
                String[] parts = nonNull(data.primaryId).split("\\|", 2);
                yield new InventoryActionTarget.ToolControlTarget(parts.length > 0 ? parts[0] : "", parts.length > 1 ? parts[1] : "");
            }
            default -> null;
        };
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private static String nonNull(String value) {
        return value == null ? "" : value;
    }

    private static <E extends Enum<E>> E decodeEnum(Class<E> type, String rawValue, E fallback) {
        if (type == null || blank(rawValue)) {
            return fallback;
        }
        try {
            return Enum.valueOf(type, rawValue);
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }

    private static Set<String> decodeLegacyExpandedSections(String encoded) {
        if (blank(encoded)) {
            return Set.of();
        }
        LinkedHashSet<String> sectionIds = new LinkedHashSet<>();
        for (String value : encoded.split(",")) {
            if (!blank(value)) {
                sectionIds.add(value.trim());
            }
        }
        return Set.copyOf(sectionIds);
    }

    private static final class StateData {
        private int version;
        private long nextGlobalSequence;
        private long workflowNextStreamSequence;
        private long activityNextStreamSequence;
        private int activityMaxEvents;
        private WorkflowCheckpointData workflowCheckpoint;
        private List<WorkflowEventData> workflowEvents;
        private ActivityCheckpointData activityCheckpoint;
        private List<ActivityEventData> activityEvents;
        private BrowsePreferencesData browsePreferences;
        private BrowseSessionData browseSession;
        private QueryStateData query;

        // Legacy v2 fields
        private List<CollectionData> collections;
        private List<MembershipData> memberships;
        private List<DesiredCountData> desiredCounts;
        private List<LoadoutData> loadouts;
        private List<RecentData> recents;
        private ProtectionData protection;
    }

    private record WorkflowCheckpointData(
            List<CollectionData> collections,
            List<MembershipData> memberships,
            List<DesiredCountData> desiredCounts,
            List<LoadoutData> loadouts,
            List<IdentityData> favoriteTags,
            List<IdentityData> junkTags,
            ProtectionData protection,
            List<RecentDismissalData> recentDismissals,
            List<VisualIslandData> visualIslands,
            List<VisualHomeData> visualHomes,
            List<String> dismissedTemplateIds,
            List<ClaimedChestData> claimedChests,
            List<ChestLinkData> chestLinks
    ) {
    }

    private record ClaimedChestData(
            String storageId,
            List<ChestAnchorData> anchors,
            int atlasX,
            int atlasY,
            String label
    ) {
    }

    private record ChestAnchorData(
            String dimensionId,
            int x,
            int y,
            int z
    ) {
    }

    private record ChestLinkData(
            String islandId,
            String storageId
    ) {
    }

    private record ActivityCheckpointData(
            List<RecentData> recents,
            List<ActivityEventData> cleanupCandidates,
            List<ActivityEventData> undoCandidates
    ) {
    }

    private static final class WorkflowEventData {
        private EnvelopeData envelope;
        private String kind;
        private String collectionId;
        private String islandId;
        private String loadoutId;
        private String name;
        private IdentityData identity;
        private int desiredCount;
        private boolean enabled;
        private long sequence;
        private int x;
        private int y;
        private TargetData target;
        private LoadoutData loadout;
        private List<LoadoutEntryData> loadoutEntries;
        private VisualIslandData visualIsland;
        private VisualHomeData visualHome;
        private String label;
        private int color;
        private String templateId;
        private IdentityData iconIdentity;
        private String storageId;
        private List<ChestAnchorData> anchors;
        private ClaimedChestData claimedChest;
    }

    private static final class ActivityEventData {
        private EnvelopeData envelope;
        private String kind;
        private String producer;
        private String confidence;
        private IdentityData identity;
        private int count;
        private TargetData fromTarget;
        private TargetData toTarget;
        private String requestId;
        private String recoveryToken;
        private List<String> reasonCodes;
        private String diagnostics;
    }

    private record EnvelopeData(
            long globalSequence,
            long streamSequence,
            String streamKind,
            long occurredAtEpochMillis,
            String origin,
            String correlationId,
            String causationId,
            String sessionId
    ) {
    }

    private record CollectionData(String id, String name) {
    }

    private record MembershipData(IdentityData identity, List<String> collectionIds) {
    }

    private record DesiredCountData(String collectionId, IdentityData identity, int desiredCount) {
    }

    private record LoadoutData(String collectionId, String id, String name, List<LoadoutEntryData> entries) {
    }

    private record LoadoutEntryData(TargetData target, IdentityData identity) {
    }

    private record RecentData(IdentityData identity, int count, long latestSequence) {
    }

    private record RecentDismissalData(IdentityData identity, long dismissedUpToSequence) {
    }

    private record VisualIslandData(
            String id,
            String label,
            String kind,
            int x,
            int y,
            int width,
            int height,
            int color,
            IdentityData iconIdentity
    ) {
    }

    private record VisualHomeData(
            IdentityData identity,
            String islandId,
            int x,
            int y,
            String origin,
            boolean locked
    ) {
    }

    private record ProtectionData(List<IdentityData> identities, List<TargetData> targets, boolean protectPortableContainers) {
    }

    private record BrowsePreferencesData(
            String defaultSortMode,
            String defaultGroupingMode,
            String defaultPaneMode,
            String defaultBulkActionScope
    ) {
    }

    private record BrowseSessionData(
            String searchText,
            String filterScope,
            String sortMode,
            String groupingMode,
            String paneMode,
            String activePane,
            String selectedCollectionId,
            String selectedLoadoutId,
            String pinnedToolId,
            String bulkActionScope,
            String selectedSubjectKey,
            List<String> expandedSectionIds
    ) {
    }

    private record QueryStateData(
            String searchText,
            String sortMode,
            String filterScope,
            String activePane,
            String selectedCollectionId,
            String selectedLoadoutId,
            String pinnedToolId,
            String bulkActionScope,
            String persistentSectionState
    ) {
    }

    private record IdentityData(String itemId, String comparisonMode, String componentFingerprint) {
    }

    private record TargetData(String kind, String primaryId, String secondaryId, int slotIndex) {
    }
}
