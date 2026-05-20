package dev.imagio.slot.workflow.domain.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
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
import dev.imagio.slot.inventory.core.ItemIdentityCollections;
import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import dev.imagio.slot.inventory.goal.GoalChoiceResolution;
import dev.imagio.slot.inventory.goal.GoalDescriptor;
import dev.imagio.slot.inventory.goal.GoalIngredientDescriptor;
import dev.imagio.slot.inventory.goal.GoalPlanState;
import dev.imagio.slot.inventory.goal.GoalRecipeDescriptor;
import dev.imagio.slot.inventory.goal.GoalStackDescriptor;
import dev.imagio.slot.workflow.domain.ActivityProjection;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ChestAffinity;
import dev.imagio.slot.workflow.domain.ChestAffinityMap;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.ClaimedChestMap;
import dev.imagio.slot.workflow.domain.KitActivation;
import dev.imagio.slot.workflow.domain.KitDefinition;
import dev.imagio.slot.workflow.domain.KitMap;
import dev.imagio.slot.workflow.domain.KitPage;
import dev.imagio.slot.workflow.domain.CollectionDefinition;
import dev.imagio.slot.workflow.domain.CollectionProjection;
import dev.imagio.slot.workflow.domain.ContextualAssociationHint;
import dev.imagio.slot.workflow.domain.ContextualAssociationIndex;
import dev.imagio.slot.workflow.domain.ContextualAssociationSet;
import dev.imagio.slot.workflow.domain.ContextualContextAggregate;
import dev.imagio.slot.workflow.domain.ContextualItemAggregate;
import dev.imagio.slot.workflow.domain.ContextualSignalEvent;
import dev.imagio.slot.workflow.domain.ContextualSignalKind;
import dev.imagio.slot.workflow.domain.ContextualSignalRecord;
import dev.imagio.slot.workflow.domain.ContextualSuggestionState;
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
import dev.imagio.slot.workflow.domain.WorkflowAcceptedInputRule;
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
    private static final int SCHEMA_VERSION = 11;

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
            StateData state = readStateSkippingWorkflowEvents(reader);
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
        // The checkpoint is the durable source of truth. Persisting the full
        // workflow event stream as well makes large auto-home/reclassify runs
        // grow the save file and retained heap without adding replay value.
        state.workflowEvents = List.of();
        state.activityCheckpoint = encodeActivityCheckpoint(snapshot.activityProjection());
        state.activityEvents = snapshot.activityEvents().records().stream()
                .map(WorkflowDomainFileStore::encodeActivityRecord)
                .filter(java.util.Objects::nonNull)
                .toList();
        state.contextualSuggestions = encodeContextualSuggestions(snapshot.contextualSuggestions());

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

    private static StateData readStateSkippingWorkflowEvents(Reader reader) throws IOException {
        if (reader == null) {
            return null;
        }
        JsonReader json = new JsonReader(reader);
        if (json.peek() != JsonToken.BEGIN_OBJECT) {
            json.skipValue();
            return null;
        }
        StateData state = new StateData();
        json.beginObject();
        while (json.hasNext()) {
            String name = json.nextName();
            switch (name) {
                case "version" -> state.version = readInt(json);
                case "nextGlobalSequence" -> state.nextGlobalSequence = readLong(json);
                case "workflowNextStreamSequence" -> state.workflowNextStreamSequence = readLong(json);
                case "activityNextStreamSequence" -> state.activityNextStreamSequence = readLong(json);
                case "activityMaxEvents" -> state.activityMaxEvents = readInt(json);
                case "workflowCheckpoint" -> state.workflowCheckpoint =
                        GSON.fromJson(json, WorkflowCheckpointData.class);
                case "workflowEvents" -> {
                    state.workflowEvents = List.of();
                    json.skipValue();
                }
                case "activityCheckpoint" -> state.activityCheckpoint =
                        GSON.fromJson(json, ActivityCheckpointData.class);
                case "activityEvents" -> state.activityEvents =
                        listFrom(GSON.fromJson(json, ActivityEventData[].class));
                case "contextualSuggestions" -> state.contextualSuggestions =
                        GSON.fromJson(json, ContextualSuggestionData.class);
                case "browsePreferences" -> state.browsePreferences =
                        GSON.fromJson(json, BrowsePreferencesData.class);
                case "browseSession" -> state.browseSession =
                        GSON.fromJson(json, BrowseSessionData.class);
                case "query" -> state.query = GSON.fromJson(json, QueryStateData.class);
                case "collections" -> state.collections =
                        listFrom(GSON.fromJson(json, CollectionData[].class));
                case "memberships" -> state.memberships =
                        listFrom(GSON.fromJson(json, MembershipData[].class));
                case "desiredCounts" -> state.desiredCounts =
                        listFrom(GSON.fromJson(json, DesiredCountData[].class));
                case "loadouts" -> state.loadouts =
                        listFrom(GSON.fromJson(json, LoadoutData[].class));
                case "recents" -> state.recents =
                        listFrom(GSON.fromJson(json, RecentData[].class));
                case "protection" -> state.protection =
                        GSON.fromJson(json, ProtectionData.class);
                default -> json.skipValue();
            }
        }
        json.endObject();
        return state;
    }

    private static int readInt(JsonReader json) throws IOException {
        if (json.peek() == JsonToken.NULL) {
            json.nextNull();
            return 0;
        }
        return json.nextInt();
    }

    private static long readLong(JsonReader json) throws IOException {
        if (json.peek() == JsonToken.NULL) {
            json.nextNull();
            return 0L;
        }
        return json.nextLong();
    }

    private static <T> List<T> listFrom(T[] values) {
        if (values == null || values.length == 0) {
            return List.of();
        }
        ArrayList<T> out = new ArrayList<>(values.length);
        java.util.Collections.addAll(out, values);
        return out;
    }

    private static WorkflowDomainSnapshot decode(StateData state) {
        InventoryBrowsePreferences browsePreferences = decodeBrowsePreferences(state.browsePreferences);
        InventoryBrowseSessionState browseSessionState = decodeBrowseSession(state.browseSession, state.query, browsePreferences);

        if (state.version < 3) {
            return migrateLegacy(state, browsePreferences, browseSessionState);
        }

        WorkflowProjection.Snapshot workflowCheckpoint = decodeWorkflowCheckpoint(state.workflowCheckpoint);
        // Phase 2.2 (schema 6) replaced freeform (localX, localY) with ordinal.
        // The checkpoint migrates cleanly via decodeVisualHomesWithMigration,
        // but cached VisualHomeAssigned/Cleared events stored before the bump
        // carry ordinal=0 and would collapse every identity onto a single slot
        // when replayed. Strip them — the only loss is unsaved homing actions
        // since the last checkpoint, acceptable for an unreleased mod.
        boolean stripLegacyHomeEvents = state.version < 6;
        WorkflowEventStore.Snapshot workflowEvents = new WorkflowEventStore.Snapshot(
                state.workflowNextStreamSequence <= 0L ? 1L : state.workflowNextStreamSequence,
                state.workflowEvents == null ? List.of() : state.workflowEvents.stream()
                        .filter(rec -> !stripLegacyHomeEvents || !isLegacyVisualHomeEvent(rec))
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
                browseSessionState,
                decodeContextualSuggestions(state.contextualSuggestions)
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
                memberships.put(
                        ItemIdentityCollections.key(identity),
                        membership.collectionIds == null ? Set.of() : Set.copyOf(membership.collectionIds));
            }
        }

        // Legacy v2 desiredCounts (collection-scoped) intentionally dropped on
        // load. The desired-counts concept moved to player-global / kit-scoped
        // domain entries; per project-memory "no migration / delete old code"
        // we silently let those legacy entries fall through.

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
                            entries.add(new QuickAccessLoadoutEntry(target, ItemIdentityCollections.key(identity)));
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
                    ItemIdentityCollections.mergePositive(recents, identity, recent.count);
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
                        ItemIdentityCollections.add(protectedIdentities, identity);
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
                loadouts,
                Set.of(),
                Set.of(),
                new ProtectionSnapshotPolicy(protectedIdentities, protectedTargets, protectPortableContainers),
                Map.of(),
                VisualHomeMap.empty(),
                ClaimedChestMap.empty(),
                ChestAffinityMap.empty(),
                Map.of(),
                KitMap.empty(),
                Map.of(),
                Map.of(),
                Map.of()
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
        ArrayList<ChestAffinityData> chestAffinity = new ArrayList<>();
        for (Map.Entry<UUID, Map<ItemIdentity, ChestAffinity>> entry : resolved.chestAffinityMap().entries().entrySet()) {
            ArrayList<ChestAffinityEntryData> bonds = new ArrayList<>();
            for (Map.Entry<ItemIdentity, ChestAffinity> bond : entry.getValue().entrySet()) {
                bonds.add(new ChestAffinityEntryData(
                        identity(bond.getKey()),
                        bond.getValue().score(),
                        bond.getValue().lastTouchedTick()
                ));
            }
            chestAffinity.add(new ChestAffinityData(entry.getKey().toString(), bonds));
        }
        ArrayList<KitDefinitionData> kitDefinitions = new ArrayList<>();
        for (KitDefinition kit : resolved.kitMap().kits()) {
            kitDefinitions.add(kitDefinition(kit));
        }
        KitActivation activation = resolved.kitMap().activation();
        KitActivationData activationData = activation.isActive()
                ? new KitActivationData(activation.kitId(), activation.pageIndex())
                : null;
        ArrayList<PlayerDesiredCountData> playerDesiredCounts = new ArrayList<>();
        resolved.playerDesiredCounts().forEach((identity, count) ->
                playerDesiredCounts.add(new PlayerDesiredCountData(identity(identity), count))
        );
        ArrayList<KitDesiredCountData> kitDesiredCounts = new ArrayList<>();
        resolved.kitDesiredCounts().forEach((kitId, counts) ->
                counts.forEach((identity, count) ->
                        kitDesiredCounts.add(new KitDesiredCountData(kitId, identity(identity), count))
                )
        );
        ArrayList<PlayerWantedCountData> playerWantedCounts = new ArrayList<>();
        resolved.playerWantedCounts().forEach((identity, count) ->
                playerWantedCounts.add(new PlayerWantedCountData(identity(identity), count))
        );
        ArrayList<KitWantedCountData> kitWantedCounts = new ArrayList<>();
        resolved.kitWantedCounts().forEach((kitId, counts) ->
                counts.forEach((identity, count) ->
                        kitWantedCounts.add(new KitWantedCountData(kitId, identity(identity), count))
                )
        );
        ArrayList<GoalRecipeDefaultData> goalRecipeDefaults = new ArrayList<>();
        resolved.goalRecipeDefaults().forEach((outputItemId, recipeId) ->
                goalRecipeDefaults.add(new GoalRecipeDefaultData(outputItemId, recipeId))
        );
        ArrayList<GoalPlanData> goalPlans = new ArrayList<>();
        for (GoalPlanState goal : resolved.goalPlans()) {
            GoalPlanData encoded = goalPlan(goal);
            if (encoded != null) {
                goalPlans.add(encoded);
            }
        }
        return new WorkflowCheckpointData(
                collections,
                memberships,
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
                chestAffinity,
                kitDefinitions,
                activationData,
                playerDesiredCounts,
                kitDesiredCounts,
                playerWantedCounts,
                kitWantedCounts,
                goalPlans,
                goalRecipeDefaults
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
                    memberships.put(
                            ItemIdentityCollections.key(identity),
                            membership.collectionIds == null ? Set.of() : Set.copyOf(membership.collectionIds));
                }
            }
        }

        // Legacy v2 desiredCounts (collection-scoped) silently dropped on load.

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
                            entries.add(new QuickAccessLoadoutEntry(target, ItemIdentityCollections.key(identity)));
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
                    ItemIdentityCollections.add(favoriteTags, identity);
                }
            }
        }

        LinkedHashSet<ItemIdentity> junkTags = new LinkedHashSet<>();
        if (data.junkTags != null) {
            for (IdentityData identityData : data.junkTags) {
                ItemIdentity identity = decodeIdentity(identityData);
                if (identity != null) {
                    ItemIdentityCollections.add(junkTags, identity);
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
                        ItemIdentityCollections.add(protectedIdentities, identity);
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
                    recentDismissals.put(
                            ItemIdentityCollections.key(identity),
                            Math.max(0L, dismissal.dismissedUpToSequence));
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

        LinkedHashMap<ItemIdentity, VisualHomeAssignment> visualHomes = decodeVisualHomesWithMigration(data.visualHomes);

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
        LinkedHashMap<UUID, Map<ItemIdentity, ChestAffinity>> affinity = new LinkedHashMap<>();
        if (data.chestAffinity != null) {
            for (ChestAffinityData entry : data.chestAffinity) {
                if (entry == null) {
                    continue;
                }
                UUID storageId = parseUuid(entry.storageId);
                if (storageId == null || entry.bonds == null) {
                    continue;
                }
                LinkedHashMap<ItemIdentity, ChestAffinity> bonds = new LinkedHashMap<>();
                for (ChestAffinityEntryData bond : entry.bonds) {
                    if (bond == null) {
                        continue;
                    }
                    ItemIdentity identity = decodeIdentity(bond.identity);
                    if (identity == null || bond.score <= 0) {
                        continue;
                    }
                    ItemIdentity key = ItemIdentityCollections.key(identity);
                    bonds.merge(
                            key,
                            new ChestAffinity(key, bond.score, bond.lastTouchedTick),
                            (left, right) -> new ChestAffinity(
                                    key,
                                    left.score() + right.score(),
                                    Math.max(left.lastTouchedTick(), right.lastTouchedTick())));
                }
                if (!bonds.isEmpty()) {
                    affinity.put(storageId, Map.copyOf(bonds));
                }
            }
        }

        ArrayList<KitDefinition> kits = new ArrayList<>();
        if (data.kits != null) {
            for (KitDefinitionData kitData : data.kits) {
                KitDefinition kit = decodeKitDefinition(kitData);
                if (kit != null) {
                    kits.add(kit);
                }
            }
        }
        KitActivation kitActivation = data.kitActivation == null
                ? KitActivation.NONE
                : new KitActivation(
                        data.kitActivation.kitId == null ? "" : data.kitActivation.kitId,
                        Math.max(0, data.kitActivation.pageIndex));
        // if activation references an unknown kit after decode, fall back to none
        if (kitActivation.isActive()) {
            boolean found = false;
            for (KitDefinition kit : kits) {
                if (kit.id().equals(kitActivation.kitId())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                kitActivation = KitActivation.NONE;
            }
        }
        KitMap kitMap = new KitMap(kits, kitActivation);

        LinkedHashMap<ItemIdentity, Integer> playerDesiredCounts = new LinkedHashMap<>();
        if (data.playerDesiredCounts != null) {
            for (PlayerDesiredCountData counted : data.playerDesiredCounts) {
                if (counted == null) {
                    continue;
                }
                ItemIdentity identity = decodeIdentity(counted.identity);
                if (identity != null && counted.count > 0) {
                    ItemIdentityCollections.mergePositive(playerDesiredCounts, identity, counted.count);
                }
            }
        }

        LinkedHashMap<String, Map<ItemIdentity, Integer>> kitDesiredCounts = new LinkedHashMap<>();
        if (data.kitDesiredCounts != null) {
            for (KitDesiredCountData counted : data.kitDesiredCounts) {
                if (counted == null || blank(counted.kitId)) {
                    continue;
                }
                ItemIdentity identity = decodeIdentity(counted.identity);
                if (identity != null && counted.count > 0) {
                    kitDesiredCounts.computeIfAbsent(counted.kitId, ignored -> new LinkedHashMap<>())
                            .merge(ItemIdentityCollections.key(identity), counted.count, Math::max);
                }
            }
        }
        // Freeze the inner maps so the Snapshot canonical form (Map.copyOf
        // of immutable inner maps) sees the same shape it gets from event
        // application — otherwise unit tests that compare snapshot equality
        // across save/replay round-trips diverge on map identity.
        LinkedHashMap<String, Map<ItemIdentity, Integer>> kitDesiredCountsFrozen = new LinkedHashMap<>();
        kitDesiredCounts.forEach((kitId, counts) -> kitDesiredCountsFrozen.put(kitId, Map.copyOf(counts)));

        LinkedHashMap<ItemIdentity, Integer> playerWantedCounts = new LinkedHashMap<>();
        if (data.playerWantedCounts != null) {
            for (PlayerWantedCountData counted : data.playerWantedCounts) {
                if (counted == null) {
                    continue;
                }
                ItemIdentity identity = decodeIdentity(counted.identity);
                if (identity != null && counted.count > 0) {
                    ItemIdentityCollections.mergePositive(playerWantedCounts, identity, counted.count);
                }
            }
        }
        LinkedHashMap<String, Map<ItemIdentity, Integer>> kitWantedCounts = new LinkedHashMap<>();
        if (data.kitWantedCounts != null) {
            for (KitWantedCountData counted : data.kitWantedCounts) {
                if (counted == null || blank(counted.kitId)) {
                    continue;
                }
                ItemIdentity identity = decodeIdentity(counted.identity);
                if (identity != null && counted.count > 0) {
                    kitWantedCounts.computeIfAbsent(counted.kitId, ignored -> new LinkedHashMap<>())
                            .merge(ItemIdentityCollections.key(identity), counted.count, Math::max);
                }
            }
        }
        LinkedHashMap<String, Map<ItemIdentity, Integer>> kitWantedCountsFrozen = new LinkedHashMap<>();
        kitWantedCounts.forEach((kitId, counts) -> kitWantedCountsFrozen.put(kitId, Map.copyOf(counts)));

        LinkedHashMap<String, String> goalRecipeDefaults = new LinkedHashMap<>();
        if (data.goalRecipeDefaults != null) {
            for (GoalRecipeDefaultData defaultData : data.goalRecipeDefaults) {
                if (defaultData == null || blank(defaultData.outputItemId) || blank(defaultData.recipeId)) {
                    continue;
                }
                goalRecipeDefaults.put(defaultData.outputItemId.trim(), defaultData.recipeId.trim());
            }
        }
        ArrayList<GoalPlanState> goalPlans = new ArrayList<>();
        if (data.goalPlans != null) {
            for (GoalPlanData goalData : data.goalPlans) {
                GoalPlanState goal = decodeGoalPlan(goalData);
                if (goal != null) {
                    goalPlans.add(goal);
                }
            }
        }

        return new WorkflowProjection.Snapshot(
                collections,
                memberships,
                loadouts,
                favoriteTags,
                junkTags,
                new ProtectionSnapshotPolicy(protectedIdentities, protectedTargets, protectPortableContainers),
                recentDismissals,
                new VisualHomeMap(playerIslands, visualHomes, dismissedTemplateIds),
                new ClaimedChestMap(claimedChests),
                new ChestAffinityMap(affinity),
                Map.of(),
                kitMap,
                playerDesiredCounts,
                kitDesiredCountsFrozen,
                playerWantedCounts,
                kitWantedCountsFrozen,
                goalPlans,
                goalRecipeDefaults
        );
    }

    private static KitDefinitionData kitDefinition(KitDefinition kit) {
        if (kit == null) {
            return null;
        }
        ArrayList<KitPageData> pages = new ArrayList<>();
        for (KitPage page : kit.pages()) {
            pages.add(kitPage(page));
        }
        IdentityData offhand = kit.offhand() == null ? null : identity(kit.offhand());
        List<IdentityData> members = kit.members().stream()
                .map(WorkflowDomainFileStore::identity)
                .toList();
        List<AcceptedInputData> acceptedInputs = kit.acceptedInputs().stream()
                .map(WorkflowDomainFileStore::acceptedInput)
                .toList();
        // bring list retired (folded into kit-scoped desired counts);
        // serialize as empty so older builds parsing this checkpoint fall
        // back to "no bring" cleanly.
        return new KitDefinitionData(kit.id(), kit.name(), pages, List.of(), offhand, kit.parentId(), members, acceptedInputs);
    }

    private static KitPageData kitPage(KitPage page) {
        if (page == null) {
            return new KitPageData(List.of());
        }
        ArrayList<IdentityData> identities = new ArrayList<>();
        for (int slotIndex = 0; slotIndex < KitPage.HOTBAR_SLOT_COUNT; slotIndex++) {
            ItemIdentity slotIdentity = page.slot(slotIndex);
            identities.add(slotIdentity == null ? null : identity(slotIdentity));
        }
        return new KitPageData(identities);
    }

    private static KitDefinition decodeKitDefinition(KitDefinitionData data) {
        if (data == null || blank(data.id) || blank(data.name)) {
            return null;
        }
        ArrayList<KitPage> pages = new ArrayList<>();
        if (data.pages != null) {
            for (KitPageData pageData : data.pages) {
                pages.add(decodeKitPage(pageData));
            }
        }
        if (pages.isEmpty()) {
            pages.add(KitPage.empty());
        }
        ItemIdentity offhand = decodeIdentity(data.offhand);
        LinkedHashSet<ItemIdentity> members = new LinkedHashSet<>();
        if (data.members != null) {
            for (IdentityData member : data.members) {
                ItemIdentity identity = decodeIdentity(member);
                if (identity != null) {
                    ItemIdentityCollections.add(members, identity);
                }
            }
        }
        LinkedHashSet<WorkflowAcceptedInputRule> acceptedInputs = new LinkedHashSet<>();
        if (data.acceptedInputs != null) {
            for (AcceptedInputData acceptedInput : data.acceptedInputs) {
                WorkflowAcceptedInputRule rule = decodeAcceptedInput(acceptedInput);
                if (rule != null) {
                    acceptedInputs.add(rule);
                }
            }
        }
        // Legacy data.bring is silently dropped — the bring concept moved
        // to kit-scoped desired counts (no migration / no compat per
        // project memory). Per-prototype users who had bring entries will
        // need to re-enter them as desired counts.
        return new KitDefinition(data.id, data.name, pages, offhand, data.parentId, members, acceptedInputs);
    }

    private static AcceptedInputData acceptedInput(WorkflowAcceptedInputRule rule) {
        if (rule == null) {
            return null;
        }
        IdentityData identity = rule.identity() == null ? null : identity(rule.identity());
        return new AcceptedInputData(rule.kind().name(), identity, rule.tagId());
    }

    private static WorkflowAcceptedInputRule decodeAcceptedInput(AcceptedInputData data) {
        if (data == null) {
            return null;
        }
        WorkflowAcceptedInputRule.Kind kind = WorkflowAcceptedInputRule.parseKind(data.kind);
        if (kind == WorkflowAcceptedInputRule.Kind.ITEM_TAG) {
            return WorkflowAcceptedInputRule.itemTag(data.tagId);
        }
        return WorkflowAcceptedInputRule.exact(decodeIdentity(data.identity));
    }

    private static KitPage decodeKitPage(KitPageData data) {
        if (data == null || data.hotbarIdentities == null) {
            return KitPage.empty();
        }
        ArrayList<ItemIdentity> identities = new ArrayList<>();
        for (int index = 0; index < KitPage.HOTBAR_SLOT_COUNT; index++) {
            if (index < data.hotbarIdentities.size()) {
                ItemIdentity identity = decodeIdentity(data.hotbarIdentities.get(index));
                identities.add(identity == null ? null : ItemIdentityCollections.key(identity));
            } else {
                identities.add(null);
            }
        }
        return new KitPage(identities);
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
                    ItemIdentity key = ItemIdentityCollections.key(identity);
                    recentCounts.merge(key, recent.count, Math::max);
                    recentSequences.merge(key, Math.max(0L, recent.latestSequence), Math::max);
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
        WorkflowEvent workflowEvent = record.event();
        if (workflowEvent instanceof WorkflowEvent.CollectionCreated event) {
                data.kind = "CollectionCreated";
                data.collectionId = event.collectionId();
                data.name = event.name();
            }
        else if (workflowEvent instanceof WorkflowEvent.CollectionRenamed event) {
                data.kind = "CollectionRenamed";
                data.collectionId = event.collectionId();
                data.name = event.name();
            }
        else if (workflowEvent instanceof WorkflowEvent.CollectionDeleted event) {
                data.kind = "CollectionDeleted";
                data.collectionId = event.collectionId();
            }
        else if (workflowEvent instanceof WorkflowEvent.CollectionItemAdded event) {
                data.kind = "CollectionItemAdded";
                data.collectionId = event.collectionId();
                data.identity = identity(event.identity());
            }
        else if (workflowEvent instanceof WorkflowEvent.CollectionItemRemoved event) {
                data.kind = "CollectionItemRemoved";
                data.collectionId = event.collectionId();
                data.identity = identity(event.identity());
            }
        else if (workflowEvent instanceof WorkflowEvent.LoadoutCreated event) {
                data.kind = "LoadoutCreated";
                data.collectionId = event.collectionId();
                data.loadout = encodeLoadout(event.loadout(), event.collectionId());
            }
        else if (workflowEvent instanceof WorkflowEvent.LoadoutRenamed event) {
                data.kind = "LoadoutRenamed";
                data.collectionId = event.collectionId();
                data.loadoutId = event.loadoutId();
                data.name = event.name();
            }
        else if (workflowEvent instanceof WorkflowEvent.LoadoutUpdated event) {
                data.kind = "LoadoutUpdated";
                data.collectionId = event.collectionId();
                data.loadoutId = event.loadoutId();
                data.loadoutEntries = encodeLoadoutEntries(event.entries());
            }
        else if (workflowEvent instanceof WorkflowEvent.LoadoutDeleted event) {
                data.kind = "LoadoutDeleted";
                data.collectionId = event.collectionId();
                data.loadoutId = event.loadoutId();
            }
        else if (workflowEvent instanceof WorkflowEvent.FavoriteMarked event) {
                data.kind = "FavoriteMarked";
                data.identity = identity(event.identity());
            }
        else if (workflowEvent instanceof WorkflowEvent.FavoriteUnmarked event) {
                data.kind = "FavoriteUnmarked";
                data.identity = identity(event.identity());
            }
        else if (workflowEvent instanceof WorkflowEvent.JunkMarked event) {
                data.kind = "JunkMarked";
                data.identity = identity(event.identity());
            }
        else if (workflowEvent instanceof WorkflowEvent.JunkUnmarked event) {
                data.kind = "JunkUnmarked";
                data.identity = identity(event.identity());
            }
        else if (workflowEvent instanceof WorkflowEvent.ProtectedIdentityMarked event) {
                data.kind = "ProtectedIdentityMarked";
                data.identity = identity(event.identity());
            }
        else if (workflowEvent instanceof WorkflowEvent.ProtectedIdentityUnmarked event) {
                data.kind = "ProtectedIdentityUnmarked";
                data.identity = identity(event.identity());
            }
        else if (workflowEvent instanceof WorkflowEvent.ProtectedTargetMarked event) {
                data.kind = "ProtectedTargetMarked";
                data.target = target(event.target());
            }
        else if (workflowEvent instanceof WorkflowEvent.ProtectedTargetUnmarked event) {
                data.kind = "ProtectedTargetUnmarked";
                data.target = target(event.target());
            }
        else if (workflowEvent instanceof WorkflowEvent.PortableContainerProtectionSet event) {
                data.kind = "PortableContainerProtectionSet";
                data.enabled = event.enabled();
            }
        else if (workflowEvent instanceof WorkflowEvent.RecentDismissedUpTo event) {
                data.kind = "RecentDismissedUpTo";
                data.identity = identity(event.identity());
                data.sequence = event.dismissedUpToGlobalSequence();
            }
        else if (workflowEvent instanceof WorkflowEvent.VisualIslandCreated event) {
                data.kind = "VisualIslandCreated";
                data.visualIsland = visualIsland(event.island());
            }
        else if (workflowEvent instanceof WorkflowEvent.VisualIslandMoved event) {
                data.kind = "VisualIslandMoved";
                data.islandId = event.islandId();
                data.x = event.x();
                data.y = event.y();
            }
        else if (workflowEvent instanceof WorkflowEvent.VisualHomeAssigned event) {
                data.kind = "VisualHomeAssigned";
                data.visualHome = visualHome(event.assignment());
            }
        else if (workflowEvent instanceof WorkflowEvent.VisualHomeCleared event) {
                data.kind = "VisualHomeCleared";
                data.identity = identity(event.identity());
            }
        else if (workflowEvent instanceof WorkflowEvent.VisualIslandRenamed event) {
                data.kind = "VisualIslandRenamed";
                data.islandId = event.islandId();
                data.label = event.label();
            }
        else if (workflowEvent instanceof WorkflowEvent.VisualIslandRecolored event) {
                data.kind = "VisualIslandRecolored";
                data.islandId = event.islandId();
                data.color = event.color();
            }
        else if (workflowEvent instanceof WorkflowEvent.VisualIslandIconChanged event) {
                data.kind = "VisualIslandIconChanged";
                data.islandId = event.islandId();
                data.iconIdentity = identity(event.iconIdentity());
            }
        else if (workflowEvent instanceof WorkflowEvent.VisualIslandDeleted event) {
                data.kind = "VisualIslandDeleted";
                data.islandId = event.islandId();
            }
        else if (workflowEvent instanceof WorkflowEvent.VisualIslandReordered event) {
                data.kind = "VisualIslandReordered";
                data.islandId = event.islandId();
                data.targetIndex = event.targetIndex();
            }
        else if (workflowEvent instanceof WorkflowEvent.TemplateIslandDismissed event) {
                data.kind = "TemplateIslandDismissed";
                data.templateId = event.templateId();
            }
        else if (workflowEvent instanceof WorkflowEvent.ClaimedChestCreated event) {
                data.kind = "ClaimedChestCreated";
                data.claimedChest = claimedChest(event.chest());
                if (event.chest() != null) {
                    data.storageId = event.chest().storageId().toString();
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.ClaimedChestMoved event) {
                data.kind = "ClaimedChestMoved";
                data.storageId = event.storageId() == null ? "" : event.storageId().toString();
                data.x = event.atlasX();
                data.y = event.atlasY();
            }
        else if (workflowEvent instanceof WorkflowEvent.ClaimedChestAnchorsChanged event) {
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
        else if (workflowEvent instanceof WorkflowEvent.ClaimedChestRelabeled event) {
                data.kind = "ClaimedChestRelabeled";
                data.storageId = event.storageId() == null ? "" : event.storageId().toString();
                data.label = event.label();
            }
        else if (workflowEvent instanceof WorkflowEvent.ClaimedChestDeleted event) {
                data.kind = "ClaimedChestDeleted";
                data.storageId = event.storageId() == null ? "" : event.storageId().toString();
            }
        else if (workflowEvent instanceof WorkflowEvent.ChestDepositObserved event) {
                data.kind = "ChestDepositObserved";
                data.storageId = event.storageId() == null ? "" : event.storageId().toString();
                data.identity = identity(event.identity());
                data.count = event.count();
                data.sequence = event.tick();
            }
        else if (workflowEvent instanceof WorkflowEvent.ChestAffinityForgotten event) {
                data.kind = "ChestAffinityForgotten";
                data.storageId = event.storageId() == null ? "" : event.storageId().toString();
                data.identity = identity(event.identity());
            }
        else if (workflowEvent instanceof WorkflowEvent.ChestAffinityCleared event) {
                data.kind = "ChestAffinityCleared";
                data.storageId = event.storageId() == null ? "" : event.storageId().toString();
            }
        else if (workflowEvent instanceof WorkflowEvent.ChestClusterRelabeled event) {
                data.kind = "ChestClusterRelabeled";
                data.collectionId = event.clusterId();
                data.name = event.label();
            }
        else if (workflowEvent instanceof WorkflowEvent.KitCreated event) {
                data.kind = "KitCreated";
                data.kit = kitDefinition(event.kit());
                data.kitId = event.kit() == null ? "" : event.kit().id();
            }
        else if (workflowEvent instanceof WorkflowEvent.KitUpdated event) {
                data.kind = "KitUpdated";
                data.kit = kitDefinition(event.kit());
                data.kitId = event.kit() == null ? "" : event.kit().id();
            }
        else if (workflowEvent instanceof WorkflowEvent.KitDeleted event) {
                data.kind = "KitDeleted";
                data.kitId = event.kitId();
            }
        else if (workflowEvent instanceof WorkflowEvent.KitActivated event) {
                data.kind = "KitActivated";
                data.kitId = event.kitId();
                data.pageIndex = event.pageIndex();
            }
        else if (workflowEvent instanceof WorkflowEvent.KitDeactivated event) {
                data.kind = "KitDeactivated";
            }
        else if (workflowEvent instanceof WorkflowEvent.KitPageSwitched event) {
                data.kind = "KitPageSwitched";
                data.pageIndex = event.pageIndex();
            }
        else if (workflowEvent instanceof WorkflowEvent.PlayerDesiredCountSet event) {
                data.kind = "PlayerDesiredCountSet";
                data.identity = identity(event.identity());
                data.desiredCount = event.count();
            }
        else if (workflowEvent instanceof WorkflowEvent.KitDesiredCountSet event) {
                data.kind = "KitDesiredCountSet";
                data.kitId = event.kitId();
                data.identity = identity(event.identity());
                data.desiredCount = event.count();
            }
        else if (workflowEvent instanceof WorkflowEvent.PlayerWantedCountSet event) {
                data.kind = "PlayerWantedCountSet";
                data.identity = identity(event.identity());
                data.count = event.count();
            }
        else if (workflowEvent instanceof WorkflowEvent.KitWantedCountSet event) {
                data.kind = "KitWantedCountSet";
                data.kitId = event.kitId();
                data.identity = identity(event.identity());
                data.count = event.count();
            }
        else if (workflowEvent instanceof WorkflowEvent.GoalRecipeDefaultSet event) {
                data.kind = "GoalRecipeDefaultSet";
                data.outputItemId = event.outputItemId();
                data.recipeId = event.recipeId();
            }
        else if (workflowEvent instanceof WorkflowEvent.GoalPlanSaved event) {
                data.kind = "GoalPlanSaved";
                data.goalPlan = goalPlan(event.goal());
            }
        else if (workflowEvent instanceof WorkflowEvent.GoalPlanRemoved event) {
                data.kind = "GoalPlanRemoved";
                data.goalId = event.goalId();
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
            case "DesiredCountSet" -> null; // Legacy collection-scoped desired count event — silently dropped on replay.
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
            case "VisualIslandReordered" -> new WorkflowEvent.VisualIslandReordered(nonNull(data.islandId), data.targetIndex);
            case "TemplateIslandDismissed" -> new WorkflowEvent.TemplateIslandDismissed(nonNull(data.templateId));
            case "ClaimedChestCreated" -> {
                ClaimedChest chest = decodeClaimedChest(data.claimedChest);
                yield chest == null ? null : new WorkflowEvent.ClaimedChestCreated(chest);
            }
            case "ClaimedChestMoved" -> {
                UUID storageId = parseUuid(data.storageId);
                yield storageId == null ? null
                        : new WorkflowEvent.ClaimedChestMoved(storageId, (int) data.x, (int) data.y);
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
            case "ChestDepositObserved" -> {
                UUID storageId = parseUuid(data.storageId);
                ItemIdentity identity = decodeIdentity(data.identity);
                yield storageId == null || identity == null ? null
                        : new WorkflowEvent.ChestDepositObserved(storageId, identity, data.count, data.sequence);
            }
            case "ChestAffinityForgotten" -> {
                UUID storageId = parseUuid(data.storageId);
                ItemIdentity identity = decodeIdentity(data.identity);
                yield storageId == null || identity == null ? null
                        : new WorkflowEvent.ChestAffinityForgotten(storageId, identity);
            }
            case "ChestAffinityCleared" -> {
                UUID storageId = parseUuid(data.storageId);
                yield storageId == null ? null : new WorkflowEvent.ChestAffinityCleared(storageId);
            }
            case "ChestClusterRelabeled" -> {
                String clusterId = data.collectionId == null ? "" : data.collectionId;
                String label = data.name == null ? "" : data.name;
                yield clusterId.isBlank() ? null : new WorkflowEvent.ChestClusterRelabeled(clusterId, label);
            }
            case "KitCreated" -> {
                KitDefinition kit = decodeKitDefinition(data.kit);
                yield kit == null ? null : new WorkflowEvent.KitCreated(kit);
            }
            case "KitUpdated" -> {
                KitDefinition kit = decodeKitDefinition(data.kit);
                yield kit == null ? null : new WorkflowEvent.KitUpdated(kit);
            }
            case "KitDeleted" -> blank(data.kitId) ? null : new WorkflowEvent.KitDeleted(data.kitId);
            case "KitActivated" -> blank(data.kitId) ? null
                    : new WorkflowEvent.KitActivated(data.kitId, Math.max(0, data.pageIndex));
            case "KitDeactivated" -> new WorkflowEvent.KitDeactivated();
            case "KitPageSwitched" -> new WorkflowEvent.KitPageSwitched(Math.max(0, data.pageIndex));
            case "PlayerDesiredCountSet" -> new WorkflowEvent.PlayerDesiredCountSet(
                    decodeIdentity(data.identity), Math.max(0, data.desiredCount));
            case "KitDesiredCountSet" -> blank(data.kitId) ? null : new WorkflowEvent.KitDesiredCountSet(
                    data.kitId, decodeIdentity(data.identity), Math.max(0, data.desiredCount));
            case "PlayerWantedCountSet" -> new WorkflowEvent.PlayerWantedCountSet(
                    decodeIdentity(data.identity), Math.max(0, data.count));
            case "KitWantedCountSet" -> blank(data.kitId) ? null : new WorkflowEvent.KitWantedCountSet(
                    data.kitId, decodeIdentity(data.identity), Math.max(0, data.count));
            case "GoalRecipeDefaultSet" -> new WorkflowEvent.GoalRecipeDefaultSet(
                    nonNull(data.outputItemId), nonNull(data.recipeId));
            case "GoalPlanSaved" -> {
                GoalPlanState goal = decodeGoalPlan(data.goalPlan);
                yield goal == null ? null : new WorkflowEvent.GoalPlanSaved(goal);
            }
            case "GoalPlanRemoved" -> blank(data.goalId) ? null : new WorkflowEvent.GoalPlanRemoved(data.goalId);
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

    private static ContextualSuggestionData encodeContextualSuggestions(ContextualSuggestionState state) {
        ContextualSuggestionState resolved = state == null ? ContextualSuggestionState.empty() : state;
        return new ContextualSuggestionData(
                resolved.nextStreamSequence(),
                resolved.itemAggregates().values().stream()
                        .map(WorkflowDomainFileStore::contextualItem)
                        .toList(),
                resolved.contextAggregates().values().stream()
                        .map(WorkflowDomainFileStore::contextualContext)
                        .toList(),
                contextualAssociationIndex(resolved.associationIndex()),
                resolved.recentSignals().stream()
                        .map(WorkflowDomainFileStore::contextualSignal)
                        .toList(),
                resolved.activeContextKey()
        );
    }

    private static ContextualSuggestionState decodeContextualSuggestions(ContextualSuggestionData data) {
        if (data == null) {
            return ContextualSuggestionState.empty();
        }
        LinkedHashMap<ItemIdentity, ContextualItemAggregate> items = new LinkedHashMap<>();
        if (data.itemAggregates != null) {
            for (ContextualItemAggregateData itemData : data.itemAggregates) {
                ContextualItemAggregate aggregate = decodeContextualItem(itemData);
                if (aggregate != null && aggregate.identity() != null) {
                    items.put(aggregate.identity(), aggregate);
                }
            }
        }
        LinkedHashMap<String, ContextualContextAggregate> contexts = new LinkedHashMap<>();
        if (data.contextAggregates != null) {
            for (ContextualContextAggregateData contextData : data.contextAggregates) {
                ContextualContextAggregate aggregate = decodeContextualContext(contextData);
                if (aggregate != null && !aggregate.contextKey().isBlank()) {
                    contexts.put(aggregate.contextKey(), aggregate);
                }
            }
        }
        ArrayList<ContextualSignalRecord> signals = new ArrayList<>();
        if (data.recentSignals != null) {
            for (ContextualSignalData signalData : data.recentSignals) {
                ContextualSignalRecord signal = decodeContextualSignal(signalData);
                if (signal != null) {
                    signals.add(signal);
                }
            }
        }
        return new ContextualSuggestionState(
                data.nextStreamSequence <= 0L ? 1L : data.nextStreamSequence,
                items,
                contexts,
                decodeContextualAssociationIndex(data.associationIndex),
                signals,
                nonNull(data.activeContextKey)
        );
    }

    private static ContextualItemAggregateData contextualItem(ContextualItemAggregate aggregate) {
        if (aggregate == null) {
            return null;
        }
        return new ContextualItemAggregateData(
                identity(aggregate.identity()),
                aggregate.timesAcquired(),
                aggregate.timesTakenFromStorage(),
                aggregate.timesDepositedToStorage(),
                aggregate.timesCraftedOrProduced(),
                aggregate.timesUsed(),
                aggregate.timesPlaced(),
                aggregate.timesConsumed(),
                aggregate.timesDamaged(),
                aggregate.lastActiveSequence(),
                aggregate.lastAcquiredSequence(),
                aggregate.lastDepositedSequence()
        );
    }

    private static ContextualItemAggregate decodeContextualItem(ContextualItemAggregateData data) {
        ItemIdentity identity = decodeIdentity(data == null ? null : data.identity);
        if (identity == null) {
            return null;
        }
        return new ContextualItemAggregate(
                identity,
                data.timesAcquired,
                data.timesTakenFromStorage,
                data.timesDepositedToStorage,
                data.timesCraftedOrProduced,
                data.timesUsed,
                data.timesPlaced,
                data.timesConsumed,
                data.timesDamaged,
                data.lastActiveSequence,
                data.lastAcquiredSequence,
                data.lastDepositedSequence
        );
    }

    private static ContextualContextAggregateData contextualContext(ContextualContextAggregate aggregate) {
        if (aggregate == null) {
            return null;
        }
        return new ContextualContextAggregateData(
                aggregate.contextKey(),
                aggregate.label(),
                aggregate.timesSeen(),
                aggregate.lastSeenSequence(),
                hints(aggregate.itemHints()),
                hints(aggregate.facetHints())
        );
    }

    private static ContextualContextAggregate decodeContextualContext(ContextualContextAggregateData data) {
        if (data == null || blank(data.contextKey)) {
            return null;
        }
        return new ContextualContextAggregate(
                data.contextKey,
                nonNull(data.label),
                data.timesSeen,
                data.lastSeenSequence,
                decodeHints(data.itemHints),
                decodeHints(data.facetHints)
        );
    }

    private static ContextualAssociationIndexData contextualAssociationIndex(ContextualAssociationIndex index) {
        ContextualAssociationIndex resolved = index == null ? ContextualAssociationIndex.empty() : index;
        return new ContextualAssociationIndexData(resolved.nextItemsBySignature().entrySet().stream()
                .map(entry -> new ContextualAssociationBucketData(
                        entry.getKey(),
                        entry.getValue().itemHints().values().stream()
                                .map(WorkflowDomainFileStore::contextualAssociationHint)
                                .toList()))
                .toList());
    }

    private static ContextualAssociationHintData contextualAssociationHint(ContextualAssociationHint hint) {
        if (hint == null) {
            return null;
        }
        return new ContextualAssociationHintData(
                hint.itemId(),
                hint.score(),
                hint.count(),
                hint.lastSequence(),
                hint.averageDelta());
    }

    private static ContextualAssociationIndex decodeContextualAssociationIndex(ContextualAssociationIndexData data) {
        if (data == null || data.nextItemsBySignature == null || data.nextItemsBySignature.isEmpty()) {
            return ContextualAssociationIndex.empty();
        }
        LinkedHashMap<String, ContextualAssociationSet> next = new LinkedHashMap<>();
        for (ContextualAssociationBucketData bucket : data.nextItemsBySignature) {
            if (bucket == null || blank(bucket.signature) || bucket.itemHints == null || bucket.itemHints.isEmpty()) {
                continue;
            }
            LinkedHashMap<String, ContextualAssociationHint> hints = new LinkedHashMap<>();
            for (ContextualAssociationHintData hintData : bucket.itemHints) {
                ContextualAssociationHint hint = decodeContextualAssociationHint(hintData);
                if (hint != null && !hint.itemId().isBlank()) {
                    hints.put(hint.itemId(), hint);
                }
            }
            if (!hints.isEmpty()) {
                next.put(bucket.signature, new ContextualAssociationSet(hints));
            }
        }
        return new ContextualAssociationIndex(next);
    }

    private static ContextualAssociationHint decodeContextualAssociationHint(ContextualAssociationHintData data) {
        if (data == null || blank(data.itemId)) {
            return null;
        }
        return new ContextualAssociationHint(
                data.itemId,
                data.score,
                data.count,
                data.lastSequence,
                data.averageDelta);
    }

    private static ContextualSignalData contextualSignal(ContextualSignalRecord record) {
        if (record == null || record.event() == null) {
            return null;
        }
        ContextualSignalEvent event = record.event();
        return new ContextualSignalData(
                encodeEnvelope(record.envelope()),
                event.kind().name(),
                identity(event.identity()),
                event.count(),
                event.observedTick(),
                event.contextKey(),
                event.contextLabel(),
                event.sourceKey(),
                metadata(event.metadata())
        );
    }

    private static ContextualSignalRecord decodeContextualSignal(ContextualSignalData data) {
        if (data == null || blank(data.kind)) {
            return null;
        }
        return new ContextualSignalRecord(
                decodeEnvelope(data.envelope, DomainEventStreamKind.CONTEXTUAL),
                new ContextualSignalEvent(
                        decodeEnum(ContextualSignalKind.class, data.kind, ContextualSignalKind.ITEM_ACQUIRED),
                        decodeIdentity(data.identity),
                        data.count,
                        data.observedTick,
                        nonNull(data.contextKey),
                        nonNull(data.contextLabel),
                        nonNull(data.sourceKey),
                        decodeMetadata(data.metadata)
                )
        );
    }

    private static List<HintData> hints(Map<String, Double> hints) {
        if (hints == null || hints.isEmpty()) {
            return List.of();
        }
        ArrayList<HintData> out = new ArrayList<>();
        hints.forEach((key, value) -> {
            if (key != null && !key.isBlank() && value != null && value > 0D && Double.isFinite(value)) {
                out.add(new HintData(key, value));
            }
        });
        return out.isEmpty() ? List.of() : List.copyOf(out);
    }

    private static Map<String, Double> decodeHints(List<HintData> hints) {
        if (hints == null || hints.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, Double> decoded = new LinkedHashMap<>();
        for (HintData hint : hints) {
            if (hint != null && !blank(hint.key) && hint.value > 0D && Double.isFinite(hint.value)) {
                decoded.put(hint.key, hint.value);
            }
        }
        return decoded.isEmpty() ? Map.of() : Map.copyOf(decoded);
    }

    private static List<MetadataEntryData> metadata(Map<String, String> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return List.of();
        }
        ArrayList<MetadataEntryData> out = new ArrayList<>();
        metadata.forEach((key, value) -> {
            if (key != null && !key.isBlank()) {
                out.add(new MetadataEntryData(key, value == null ? "" : value));
            }
        });
        return out.isEmpty() ? List.of() : List.copyOf(out);
    }

    private static Map<String, String> decodeMetadata(List<MetadataEntryData> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, String> decoded = new LinkedHashMap<>();
        for (MetadataEntryData entry : metadata) {
            if (entry != null && !blank(entry.key)) {
                decoded.put(entry.key, nonNull(entry.value));
            }
        }
        return decoded.isEmpty() ? Map.of() : Map.copyOf(decoded);
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
                decoded.add(new QuickAccessLoadoutEntry(target, ItemIdentityCollections.key(identity)));
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

    private static GoalPlanData goalPlan(GoalPlanState goal) {
        if (goal == null || goal.descriptor() == null) {
            return null;
        }
        return new GoalPlanData(
                goal.goalId(),
                goal.label(),
                goal.targetCount(),
                goalDescriptor(goal.descriptor()),
                goalChoiceResolution(goal.choiceResolution())
        );
    }

    private static GoalPlanState decodeGoalPlan(GoalPlanData data) {
        if (data == null) {
            return null;
        }
        GoalDescriptor descriptor = decodeGoalDescriptor(data.descriptor);
        if (descriptor == null) {
            return null;
        }
        return new GoalPlanState(
                nonNull(data.goalId),
                nonNull(data.label),
                data.targetCount,
                descriptor,
                decodeGoalChoiceResolution(data.choiceResolution)
        );
    }

    private static GoalDescriptorData goalDescriptor(GoalDescriptor descriptor) {
        if (descriptor == null) {
            return null;
        }
        return new GoalDescriptorData(
                descriptor.goalId(),
                descriptor.label(),
                goalStacks(descriptor.targetOutputs()),
                descriptor.targetCount(),
                descriptor.focusedRecipeId(),
                descriptor.focusedCategoryId(),
                goalRecipes(descriptor.recipes())
        );
    }

    private static GoalDescriptor decodeGoalDescriptor(GoalDescriptorData data) {
        if (data == null) {
            return null;
        }
        return new GoalDescriptor(
                nonNull(data.goalId),
                nonNull(data.label),
                decodeGoalStacks(data.targetOutputs),
                data.targetCount,
                nonNull(data.focusedRecipeId),
                nonNull(data.focusedCategoryId),
                decodeGoalRecipes(data.recipes)
        );
    }

    private static List<GoalRecipeData> goalRecipes(List<GoalRecipeDescriptor> recipes) {
        if (recipes == null || recipes.isEmpty()) {
            return List.of();
        }
        ArrayList<GoalRecipeData> encoded = new ArrayList<>(recipes.size());
        for (GoalRecipeDescriptor recipe : recipes) {
            GoalRecipeData data = goalRecipe(recipe);
            if (data != null) {
                encoded.add(data);
            }
        }
        return List.copyOf(encoded);
    }

    private static List<GoalRecipeDescriptor> decodeGoalRecipes(List<GoalRecipeData> data) {
        if (data == null || data.isEmpty()) {
            return List.of();
        }
        ArrayList<GoalRecipeDescriptor> recipes = new ArrayList<>(data.size());
        for (GoalRecipeData recipeData : data) {
            GoalRecipeDescriptor recipe = decodeGoalRecipe(recipeData);
            if (recipe != null) {
                recipes.add(recipe);
            }
        }
        return List.copyOf(recipes);
    }

    private static GoalRecipeData goalRecipe(GoalRecipeDescriptor recipe) {
        if (recipe == null) {
            return null;
        }
        return new GoalRecipeData(
                recipe.recipeId(),
                recipe.categoryId(),
                recipe.supportsTree(),
                goalStacks(recipe.outputs()),
                goalIngredients(recipe.inputs()),
                goalIngredients(recipe.catalysts()),
                recipe.diagnostics()
        );
    }

    private static GoalRecipeDescriptor decodeGoalRecipe(GoalRecipeData data) {
        if (data == null) {
            return null;
        }
        return new GoalRecipeDescriptor(
                nonNull(data.recipeId),
                nonNull(data.categoryId),
                data.supportsTree,
                decodeGoalStacks(data.outputs),
                decodeGoalIngredients(data.inputs),
                decodeGoalIngredients(data.catalysts),
                copyStringList(data.diagnostics)
        );
    }

    private static List<GoalIngredientData> goalIngredients(List<GoalIngredientDescriptor> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return List.of();
        }
        ArrayList<GoalIngredientData> encoded = new ArrayList<>(ingredients.size());
        for (GoalIngredientDescriptor ingredient : ingredients) {
            GoalIngredientData data = goalIngredient(ingredient);
            if (data != null) {
                encoded.add(data);
            }
        }
        return List.copyOf(encoded);
    }

    private static List<GoalIngredientDescriptor> decodeGoalIngredients(List<GoalIngredientData> data) {
        if (data == null || data.isEmpty()) {
            return List.of();
        }
        ArrayList<GoalIngredientDescriptor> ingredients = new ArrayList<>(data.size());
        for (GoalIngredientData ingredientData : data) {
            GoalIngredientDescriptor ingredient = decodeGoalIngredient(ingredientData);
            if (ingredient != null) {
                ingredients.add(ingredient);
            }
        }
        return List.copyOf(ingredients);
    }

    private static GoalIngredientData goalIngredient(GoalIngredientDescriptor ingredient) {
        if (ingredient == null) {
            return null;
        }
        return new GoalIngredientData(
                ingredient.ingredientId(),
                ingredient.label(),
                ingredient.quantity(),
                ingredient.chance(),
                ingredient.serializedIngredient(),
                goalStacks(ingredient.alternatives()),
                ingredient.choiceRequired(),
                ingredient.consumed(),
                ingredient.tagOrListLabel(),
                ingredient.diagnostics()
        );
    }

    private static GoalIngredientDescriptor decodeGoalIngredient(GoalIngredientData data) {
        if (data == null) {
            return null;
        }
        return new GoalIngredientDescriptor(
                nonNull(data.ingredientId),
                nonNull(data.label),
                data.quantity,
                data.chance,
                nonNull(data.serializedIngredient),
                decodeGoalStacks(data.alternatives),
                data.choiceRequired,
                data.consumed,
                nonNull(data.tagOrListLabel),
                copyStringList(data.diagnostics)
        );
    }

    private static List<GoalStackData> goalStacks(List<GoalStackDescriptor> stacks) {
        if (stacks == null || stacks.isEmpty()) {
            return List.of();
        }
        ArrayList<GoalStackData> encoded = new ArrayList<>(stacks.size());
        for (GoalStackDescriptor stack : stacks) {
            GoalStackData data = goalStack(stack);
            if (data != null) {
                encoded.add(data);
            }
        }
        return List.copyOf(encoded);
    }

    private static List<GoalStackDescriptor> decodeGoalStacks(List<GoalStackData> data) {
        if (data == null || data.isEmpty()) {
            return List.of();
        }
        ArrayList<GoalStackDescriptor> stacks = new ArrayList<>(data.size());
        for (GoalStackData stackData : data) {
            GoalStackDescriptor stack = decodeGoalStack(stackData);
            if (stack != null) {
                stacks.add(stack);
            }
        }
        return List.copyOf(stacks);
    }

    private static GoalStackData goalStack(GoalStackDescriptor stack) {
        if (stack == null || stack.identity() == null) {
            return null;
        }
        return new GoalStackData(identity(stack.identity()), stack.displayName(), stack.count());
    }

    private static GoalStackDescriptor decodeGoalStack(GoalStackData data) {
        if (data == null) {
            return null;
        }
        ItemIdentity identity = decodeIdentity(data.identity);
        return identity == null ? null : new GoalStackDescriptor(identity, nonNull(data.displayName), data.count);
    }

    private static GoalChoiceResolutionData goalChoiceResolution(GoalChoiceResolution resolution) {
        GoalChoiceResolution resolved = resolution == null ? GoalChoiceResolution.empty() : resolution;
        ArrayList<GoalChoiceData> choices = new ArrayList<>();
        resolved.choicesByKey().forEach((choiceGroupId, identity) ->
                choices.add(new GoalChoiceData(choiceGroupId, identity(identity)))
        );
        ArrayList<GoalRecipeChoiceData> recipeChoices = new ArrayList<>();
        resolved.recipeChoicesByKey().forEach((choiceGroupId, recipeId) ->
                recipeChoices.add(new GoalRecipeChoiceData(choiceGroupId, recipeId))
        );
        return new GoalChoiceResolutionData(choices, recipeChoices);
    }

    private static GoalChoiceResolution decodeGoalChoiceResolution(GoalChoiceResolutionData data) {
        if (data == null) {
            return GoalChoiceResolution.empty();
        }
        LinkedHashMap<String, ItemIdentity> choices = new LinkedHashMap<>();
        if (data.choices != null) {
            for (GoalChoiceData choice : data.choices) {
                ItemIdentity identity = decodeIdentity(choice == null ? null : choice.identity);
                if (choice != null && !blank(choice.choiceGroupId) && identity != null) {
                    choices.put(choice.choiceGroupId.trim(), ItemIdentityCollections.key(identity));
                }
            }
        }
        LinkedHashMap<String, String> recipeChoices = new LinkedHashMap<>();
        if (data.recipeChoices != null) {
            for (GoalRecipeChoiceData choice : data.recipeChoices) {
                if (choice != null && !blank(choice.choiceGroupId) && !blank(choice.recipeId)) {
                    recipeChoices.put(choice.choiceGroupId.trim(), choice.recipeId.trim());
                }
            }
        }
        return new GoalChoiceResolution(choices, recipeChoices);
    }

    private static List<String> copyStringList(List<String> source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        ArrayList<String> copy = new ArrayList<>(source.size());
        for (String value : source) {
            if (!blank(value)) {
                copy.add(value.trim());
            }
        }
        return List.copyOf(copy);
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

    private static boolean isLegacyVisualHomeEvent(WorkflowEventData record) {
        return record != null
                && ("VisualHomeAssigned".equals(record.kind) || "VisualHomeCleared".equals(record.kind));
    }

    /**
     * Decode the persisted visual-home list, deriving ordinals from legacy
     * {@code (x, y)} pairs when the file pre-dates Phase 2.2.
     *
     * <p>Migration heuristic: if any assignment carries an explicit
     * non-zero {@code ordinal}, treat the whole file as Phase-2.2 and
     * trust each ordinal as-is. Otherwise group assignments by island and
     * derive ordinals from the {@code (y, x, identity)} sort — matching
     * the canonical pre-2.2 client-side render order. The next save flushes
     * the migrated form back to disk.
     */
    private static LinkedHashMap<ItemIdentity, VisualHomeAssignment> decodeVisualHomesWithMigration(
            List<VisualHomeData> rawHomes
    ) {
        LinkedHashMap<ItemIdentity, VisualHomeAssignment> result = new LinkedHashMap<>();
        if (rawHomes == null || rawHomes.isEmpty()) {
            return result;
        }
        boolean hasExplicitOrdinal = false;
        for (VisualHomeData raw : rawHomes) {
            if (raw != null && raw.ordinal > 0) {
                hasExplicitOrdinal = true;
                break;
            }
        }
        if (hasExplicitOrdinal) {
            for (VisualHomeData raw : rawHomes) {
                VisualHomeAssignment assignment = decodeVisualHome(raw);
                if (assignment != null) {
                    ItemIdentityCollections.removeMatching(result, assignment.identity());
                    result.put(ItemIdentityCollections.key(assignment.identity()), assignment);
                }
            }
            return result;
        }
        // Group by island, sort by (y, x, identity), assign ordinals 0..N-1.
        // Iteration order of rawHomes is preserved for cross-island insertion
        // order so the result keeps a stable shape.
        LinkedHashMap<String, List<VisualHomeData>> byIsland = new LinkedHashMap<>();
        for (VisualHomeData raw : rawHomes) {
            if (raw == null || blank(raw.islandId)) {
                continue;
            }
            byIsland.computeIfAbsent(raw.islandId, k -> new ArrayList<>()).add(raw);
        }
        for (List<VisualHomeData> islandRaws : byIsland.values()) {
            islandRaws.sort((a, b) -> {
                int cmp = Integer.compare(a.y, b.y);
                if (cmp != 0) return cmp;
                cmp = Integer.compare(a.x, b.x);
                if (cmp != 0) return cmp;
                String aId = a.identity == null ? "" : nonNull(a.identity.itemId());
                String bId = b.identity == null ? "" : nonNull(b.identity.itemId());
                return aId.compareTo(bId);
            });
            for (int ordinal = 0; ordinal < islandRaws.size(); ordinal++) {
                VisualHomeData raw = islandRaws.get(ordinal);
                ItemIdentity identity = decodeIdentity(raw.identity);
                if (identity == null) {
                    continue;
                }
                ItemIdentity key = ItemIdentityCollections.key(identity);
                result.put(key, new VisualHomeAssignment(
                        key,
                        raw.islandId,
                        ordinal,
                        decodeEnum(VisualHomeOrigin.class, raw.origin, VisualHomeOrigin.PLAYER_PLACED),
                        raw.locked
                ));
            }
        }
        return result;
    }

    private static VisualHomeData visualHome(VisualHomeAssignment assignment) {
        if (assignment == null) {
            return null;
        }
        // Phase 2.2+: write {x = y = 0, ordinal = N}. The legacy x/y slots
        // stay in the schema only so older saves can still parse and migrate.
        return new VisualHomeData(
                identity(assignment.identity()),
                assignment.islandId(),
                0,
                0,
                assignment.ordinal(),
                assignment.origin().name(),
                assignment.locked()
        );
    }

    private static VisualHomeAssignment decodeVisualHome(VisualHomeData data) {
        ItemIdentity identity = decodeIdentity(data == null ? null : data.identity);
        if (identity == null || data == null || blank(data.islandId)) {
            return null;
        }
        // Pre-2.2 saves carry localX/localY in {x, y}; the ordinal field is
        // absent (decoded as 0). Migration to ordinals happens after the
        // checkpoint loads — see migrateOrdinals in load().
        return new VisualHomeAssignment(
                identity,
                data.islandId,
                Math.max(0, data.ordinal),
                decodeEnum(VisualHomeOrigin.class, data.origin, VisualHomeOrigin.PLAYER_PLACED),
                data.locked
        );
    }

    private static TargetData target(LoadoutTarget target) {
        if (target instanceof LoadoutTarget.QuickAccessLaneTarget laneTarget) {
            return new TargetData("quick_access", laneTarget.laneId(), "", laneTarget.slotIndex());
        }
        if (target instanceof LoadoutTarget.EquipmentSlotTarget equipmentTarget) {
            return new TargetData("equipment", "", equipmentTarget.groupId(), equipmentTarget.slotIndex());
        }
        return null;
    }

    private static TargetData target(InventoryActionTarget target) {
        if (target == null) {
            return null;
        }
        if (target instanceof InventoryActionTarget.CursorTarget) {
            return new TargetData("cursor", "", "", 0);
        }
        if (target instanceof InventoryActionTarget.SourceTarget sourceTarget) {
            return new TargetData("source_scope", sourceTarget.sourceId(), "", -1);
        }
        if (target instanceof InventoryActionTarget.SourceSlotTarget slotTarget) {
            return new TargetData("source", slotTarget.sourceId(), "", slotTarget.slotIndex());
        }
        if (target instanceof InventoryActionTarget.SourceEntryTarget sourceEntryTarget) {
            return new TargetData("source_entry", sourceEntryTarget.sourceId(), sourceEntryTarget.entryId(), 0);
        }
        if (target instanceof InventoryActionTarget.QuickAccessTarget quickAccessTarget) {
            return new TargetData("quick_access", quickAccessTarget.laneId(), "", quickAccessTarget.slotIndex());
        }
        if (target instanceof InventoryActionTarget.EquipmentTarget equipmentTarget) {
            return new TargetData("equipment", "", equipmentTarget.groupId(), equipmentTarget.slotIndex());
        }
        if (target instanceof InventoryActionTarget.ToolRegionTarget toolRegionTarget) {
            return new TargetData("tool_region", toolRegionTarget.toolId() + "|" + toolRegionTarget.regionId(), "", toolRegionTarget.slotIndex());
        }
        if (target instanceof InventoryActionTarget.ToolControlTarget toolControlTarget) {
            return new TargetData("tool_control", toolControlTarget.toolId() + "|" + toolControlTarget.controlId(), "", 0);
        }
        return null;
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
        private ContextualSuggestionData contextualSuggestions;
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
            List<LoadoutData> loadouts,
            List<IdentityData> favoriteTags,
            List<IdentityData> junkTags,
            ProtectionData protection,
            List<RecentDismissalData> recentDismissals,
            List<VisualIslandData> visualIslands,
            List<VisualHomeData> visualHomes,
            List<String> dismissedTemplateIds,
            List<ClaimedChestData> claimedChests,
            List<ChestAffinityData> chestAffinity,
            List<KitDefinitionData> kits,
            KitActivationData kitActivation,
            List<PlayerDesiredCountData> playerDesiredCounts,
            List<KitDesiredCountData> kitDesiredCounts,
            List<PlayerWantedCountData> playerWantedCounts,
            List<KitWantedCountData> kitWantedCounts,
            List<GoalPlanData> goalPlans,
            List<GoalRecipeDefaultData> goalRecipeDefaults
    ) {
    }

    private record PlayerDesiredCountData(IdentityData identity, int count) {
    }

    private record KitDesiredCountData(String kitId, IdentityData identity, int count) {
    }

    private record PlayerWantedCountData(IdentityData identity, int count) {
    }

    private record KitWantedCountData(String kitId, IdentityData identity, int count) {
    }

    private record GoalRecipeDefaultData(String outputItemId, String recipeId) {
    }

    private record GoalPlanData(
            String goalId,
            String label,
            int targetCount,
            GoalDescriptorData descriptor,
            GoalChoiceResolutionData choiceResolution
    ) {
    }

    private record GoalDescriptorData(
            String goalId,
            String label,
            List<GoalStackData> targetOutputs,
            int targetCount,
            String focusedRecipeId,
            String focusedCategoryId,
            List<GoalRecipeData> recipes
    ) {
    }

    private record GoalRecipeData(
            String recipeId,
            String categoryId,
            boolean supportsTree,
            List<GoalStackData> outputs,
            List<GoalIngredientData> inputs,
            List<GoalIngredientData> catalysts,
            List<String> diagnostics
    ) {
    }

    private record GoalIngredientData(
            String ingredientId,
            String label,
            int quantity,
            double chance,
            String serializedIngredient,
            List<GoalStackData> alternatives,
            boolean choiceRequired,
            boolean consumed,
            String tagOrListLabel,
            List<String> diagnostics
    ) {
    }

    private record GoalStackData(IdentityData identity, String displayName, int count) {
    }

    private record GoalChoiceResolutionData(
            List<GoalChoiceData> choices,
            List<GoalRecipeChoiceData> recipeChoices
    ) {
    }

    private record GoalChoiceData(String choiceGroupId, IdentityData identity) {
    }

    private record GoalRecipeChoiceData(String choiceGroupId, String recipeId) {
    }

    private record KitDefinitionData(
            String id,
            String name,
            List<KitPageData> pages,
            List<IdentityData> bring,
            IdentityData offhand,
            String parentId,
            List<IdentityData> members,
            List<AcceptedInputData> acceptedInputs
    ) {
    }

    private record AcceptedInputData(
            String kind,
            IdentityData identity,
            String tagId
    ) {
    }

    private record KitPageData(
            List<IdentityData> hotbarIdentities
    ) {
    }

    private record KitActivationData(
            String kitId,
            int pageIndex
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

    private record ChestAffinityData(
            String storageId,
            List<ChestAffinityEntryData> bonds
    ) {
    }

    private record ChestAffinityEntryData(
            IdentityData identity,
            int score,
            long lastTouchedTick
    ) {
    }

    private record ActivityCheckpointData(
            List<RecentData> recents,
            List<ActivityEventData> cleanupCandidates,
            List<ActivityEventData> undoCandidates
    ) {
    }

    private static final class ContextualSuggestionData {
        private long nextStreamSequence;
        private List<ContextualItemAggregateData> itemAggregates;
        private List<ContextualContextAggregateData> contextAggregates;
        private ContextualAssociationIndexData associationIndex;
        private List<ContextualSignalData> recentSignals;
        private String activeContextKey;

        private ContextualSuggestionData(
                long nextStreamSequence,
                List<ContextualItemAggregateData> itemAggregates,
                List<ContextualContextAggregateData> contextAggregates,
                ContextualAssociationIndexData associationIndex,
                List<ContextualSignalData> recentSignals,
                String activeContextKey
        ) {
            this.nextStreamSequence = nextStreamSequence;
            this.itemAggregates = itemAggregates;
            this.contextAggregates = contextAggregates;
            this.associationIndex = associationIndex;
            this.recentSignals = recentSignals;
            this.activeContextKey = activeContextKey;
        }
    }

    private record ContextualItemAggregateData(
            IdentityData identity,
            int timesAcquired,
            int timesTakenFromStorage,
            int timesDepositedToStorage,
            int timesCraftedOrProduced,
            int timesUsed,
            int timesPlaced,
            int timesConsumed,
            int timesDamaged,
            long lastActiveSequence,
            long lastAcquiredSequence,
            long lastDepositedSequence
    ) {
    }

    private record ContextualContextAggregateData(
            String contextKey,
            String label,
            int timesSeen,
            long lastSeenSequence,
            List<HintData> itemHints,
            List<HintData> facetHints
    ) {
    }

    private record ContextualSignalData(
            EnvelopeData envelope,
            String kind,
            IdentityData identity,
            int count,
            long observedTick,
            String contextKey,
            String contextLabel,
            String sourceKey,
            List<MetadataEntryData> metadata
    ) {
    }

    private record ContextualAssociationIndexData(
            List<ContextualAssociationBucketData> nextItemsBySignature
    ) {
    }

    private record ContextualAssociationBucketData(
            String signature,
            List<ContextualAssociationHintData> itemHints
    ) {
    }

    private record ContextualAssociationHintData(
            String itemId,
            double score,
            int count,
            long lastSequence,
            double averageDelta
    ) {
    }

    private record HintData(String key, double value) {
    }

    private record MetadataEntryData(String key, String value) {
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
        private double x;
        private double y;
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
        private String kitId;
        private int pageIndex;
        private int count;
        private String outputItemId;
        private String recipeId;
        private String goalId;
        private GoalPlanData goalPlan;
        private int targetIndex;
        private KitDefinitionData kit;
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
            double x,
            double y,
            int color,
            IdentityData iconIdentity
    ) {
    }

    /**
     * Persisted shape for {@link VisualHomeAssignment}.
     *
     * <p>{@code x} / {@code y} were the freeform-coordinate pair used by
     * pre-2.2 SLOT. They linger only so older saves still parse — they
     * feed {@code migrateOrdinalsFromLegacyCoords} on load. Phase 2.2+
     * writes use {@code ordinal} only and the migrated state writes back
     * with {@code x = y = 0}.
     */
    private record VisualHomeData(
            IdentityData identity,
            String islandId,
            int x,
            int y,
            int ordinal,
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
