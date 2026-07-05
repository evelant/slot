package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityCollections;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.core.ItemStackTags;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.platform.SlotStackAccess;
import dev.imagio.slot.workflow.domain.WorkflowAcceptedInputRule;
import dev.imagio.slot.workflow.domain.WorkflowDomainSnapshot;
import dev.imagio.slot.workflow.domain.WorkflowTabTargets;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Session-local read-side facts for workspace projection.
 *
 * <p>This store is not authority. It snapshots immutable display/projection
 * facts from the request so future incremental slices can update local cards,
 * storage chips, and edges without asking loader UI code to own semantics.
 */
public final class WorkspaceProjectionStore {
    private static final WorkspaceProjectionStore EMPTY = new WorkspaceProjectionStore(
            ProjectionIdentityContext.empty(),
            Map.of(),
            Map.of(),
            Map.of(),
            Map.of(),
            Map.of(),
            Map.of());

    private final ProjectionIdentityContext identityContext;
    private final Map<SourceSlotKey, SourceEntryFact> sourceEntries;
    private final Map<ItemIdentity, CarriedIdentityFact> carriedIdentities;
    private final Map<String, StorageMetaFact> storageMeta;
    private final Map<String, StorageContentsFact> storageContents;
    private final Map<StoragePresenceKey, StoragePresenceFact> storagePresence;
    private final Map<ItemIdentity, TargetFact> targetFacts;

    private WorkspaceProjectionStore(
            ProjectionIdentityContext identityContext,
            Map<SourceSlotKey, SourceEntryFact> sourceEntries,
            Map<ItemIdentity, CarriedIdentityFact> carriedIdentities,
            Map<String, StorageMetaFact> storageMeta,
            Map<String, StorageContentsFact> storageContents,
            Map<StoragePresenceKey, StoragePresenceFact> storagePresence,
            Map<ItemIdentity, TargetFact> targetFacts
    ) {
        this.identityContext = identityContext == null ? ProjectionIdentityContext.empty() : identityContext;
        this.sourceEntries = sourceEntries == null ? Map.of() : Map.copyOf(sourceEntries);
        this.carriedIdentities = carriedIdentities == null ? Map.of() : Map.copyOf(carriedIdentities);
        this.storageMeta = storageMeta == null ? Map.of() : Map.copyOf(storageMeta);
        this.storageContents = storageContents == null ? Map.of() : Map.copyOf(storageContents);
        this.storagePresence = storagePresence == null ? Map.of() : Map.copyOf(storagePresence);
        this.targetFacts = targetFacts == null ? Map.of() : Map.copyOf(targetFacts);
    }

    public static WorkspaceProjectionStore empty() {
        return EMPTY;
    }

    public static WorkspaceProjectionStore from(WorkspaceProjectionRequest request) {
        WorkspaceProjectionRequest resolved = request == null
                ? new WorkspaceProjectionRequest(
                        null, null, "ready", "", 0, -1, 0,
                        null, null, null, null, null, null, "",
                        null, 0L, null, null, null, null, null, null, null, null, null)
                : request;
        ProjectionIdentityContext identityContext = ProjectionIdentityContext.from(resolved.authority());
        LinkedHashMap<SourceSlotKey, SourceEntryFact> sourceEntries = sourceEntryFacts(identityContext);
        LinkedHashMap<ItemIdentity, CarriedIdentityFact> carriedIdentities =
                carriedIdentityFacts(identityContext, sourceEntries);
        StorageFacts storageFacts = storageFacts(resolved.storageIndex());
        LinkedHashMap<ItemIdentity, TargetFact> targetFacts =
                targetFacts(resolved.workflow(), identityContext, storageFacts.presence());
        return new WorkspaceProjectionStore(
                identityContext,
                sourceEntries,
                carriedIdentities,
                storageFacts.meta(),
                storageFacts.contents(),
                storageFacts.presence(),
                targetFacts);
    }

    public UpdateResult updateFrom(
            WorkspaceProjectionRequest request,
            WorkspaceInvalidationSummary invalidations
    ) {
        WorkspaceInvalidationSummary summary = invalidations == null
                ? WorkspaceInvalidationSummary.empty()
                : invalidations;
        WorkspaceProjectionRequest resolved = request == null
                ? new WorkspaceProjectionRequest(
                        null, null, "ready", "", 0, -1, 0,
                        null, null, null, null, null, null, "",
                        null, 0L, null, null, null, null, null, null, null, null, null)
                : request;
        if (summary.invalidationCount() == 0) {
            return new UpdateResult(this, 0L, factCount(), false);
        }
        if (summary.requiresFullProjection()) {
            WorkspaceProjectionStore rebuilt = from(resolved);
            return new UpdateResult(rebuilt, rebuilt.factCount(), 0L, false);
        }

        ProjectionIdentityContext nextIdentityContext = ProjectionIdentityContext.from(resolved.authority());
        LinkedHashMap<SourceSlotKey, SourceEntryFact> nextSourceEntries = new LinkedHashMap<>(sourceEntries);
        LinkedHashMap<ItemIdentity, CarriedIdentityFact> nextCarriedIdentities =
                new LinkedHashMap<>(carriedIdentities);
        LinkedHashMap<String, StorageMetaFact> nextStorageMeta = new LinkedHashMap<>(storageMeta);
        LinkedHashMap<String, StorageContentsFact> nextStorageContents = new LinkedHashMap<>(storageContents);
        LinkedHashMap<StoragePresenceKey, StoragePresenceFact> nextStoragePresence =
                new LinkedHashMap<>(storagePresence);
        LinkedHashMap<ItemIdentity, TargetFact> nextTargetFacts = new LinkedHashMap<>(targetFacts);

        long updated = 0L;
        if (!summary.identities().isEmpty()) {
            updated += updateIdentityFacts(
                    summary.identities(),
                    nextIdentityContext,
                    nextSourceEntries,
                    nextCarriedIdentities);
        }
        if (!summary.storageIds().isEmpty()) {
            updated += updateStorageFacts(
                    summary.storageIds(),
                    resolved.storageIndex(),
                    nextStorageMeta,
                    nextStorageContents,
                    nextStoragePresence);
        }
        if (!summary.identities().isEmpty()) {
            updated += updateTargetFacts(
                    summary.identities(),
                    resolved.workflow(),
                    nextIdentityContext,
                    nextStoragePresence,
                    nextTargetFacts);
        }
        if (updated == 0L) {
            return new UpdateResult(this, 0L, factCount(), false);
        }
        WorkspaceProjectionStore updatedStore = new WorkspaceProjectionStore(
                nextIdentityContext,
                nextSourceEntries,
                nextCarriedIdentities,
                nextStorageMeta,
                nextStorageContents,
                nextStoragePresence,
                nextTargetFacts);
        long boundedUpdated = Math.min(updated, updatedStore.factCount());
        return new UpdateResult(
                updatedStore,
                boundedUpdated,
                Math.max(0L, updatedStore.factCount() - boundedUpdated),
                true);
    }

    public ProjectionIdentityContext identityContext() {
        return identityContext;
    }

    public Map<SourceSlotKey, SourceEntryFact> sourceEntries() {
        return sourceEntries;
    }

    public Map<ItemIdentity, CarriedIdentityFact> carriedIdentities() {
        return carriedIdentities;
    }

    public Map<String, StorageMetaFact> storageMeta() {
        return storageMeta;
    }

    public Map<String, StorageContentsFact> storageContents() {
        return storageContents;
    }

    public Map<StoragePresenceKey, StoragePresenceFact> storagePresence() {
        return storagePresence;
    }

    public Map<ItemIdentity, TargetFact> targetFacts() {
        return targetFacts;
    }

    public int factCount() {
        return sourceEntries.size()
                + carriedIdentities.size()
                + storageMeta.size()
                + storageContents.size()
                + storagePresence.size()
                + targetFacts.size();
    }

    private static LinkedHashMap<SourceSlotKey, SourceEntryFact> sourceEntryFacts(
            ProjectionIdentityContext identityContext
    ) {
        ProjectionIdentityContext context = identityContext == null
                ? ProjectionIdentityContext.empty()
                : identityContext;
        InventoryAuthoritySnapshot authority = context.authority();
        LinkedHashMap<SourceSlotKey, SourceEntryFact> facts = new LinkedHashMap<>();
        for (InventorySourceDescriptor source : authority.sourceDescriptors()) {
            if (source == null) {
                continue;
            }
            for (InventoryEntrySnapshot entry : authority.entries(source.id())) {
                if (entry == null || !entry.present() || entry.stack() == null || entry.stack().isEmpty()) {
                    continue;
                }
                ProjectionIdentityContext.EntryIdentity entryIdentity = context.entryIdentity(entry);
                ItemIdentity exact = entryIdentity == null
                        ? ItemIdentityMatcher.create(entry.stack())
                        : entryIdentity.exactIdentity();
                ItemIdentity movable = entryIdentity == null
                        ? ItemIdentityMatcher.normalizeMovable(exact)
                        : entryIdentity.movableIdentity();
                SourceSlotKey sourceSlot = new SourceSlotKey(source.id(), entry.entryKey().stableKey(), entry.slotIndex());
                facts.put(sourceSlot, new SourceEntryFact(
                        sourceSlot,
                        exact,
                        movable,
                        entry.count(),
                        copyStack(entry.stack()),
                        ExactStackKey.from(entry.stack()),
                        source.domain() == null ? "" : source.domain().name(),
                        source.role() == null ? "" : source.role().name()));
            }
        }
        return facts;
    }

    private static LinkedHashMap<ItemIdentity, CarriedIdentityFact> carriedIdentityFacts(
            ProjectionIdentityContext identityContext,
            Map<SourceSlotKey, SourceEntryFact> sourceEntries
    ) {
        ProjectionIdentityContext context = identityContext == null
                ? ProjectionIdentityContext.empty()
                : identityContext;
        LinkedHashMap<ItemIdentity, CarriedIdentityFact> facts = new LinkedHashMap<>();
        for (Map.Entry<ItemIdentity, Integer> entry : context.carriedCounts().counts().entrySet()) {
            ItemIdentity identity = ItemIdentityCollections.key(entry.getKey());
            int total = Math.max(0, entry.getValue() == null ? 0 : entry.getValue());
            SourceEntryFact largest = largestSourceEntry(sourceEntries, identity);
            facts.put(identity, new CarriedIdentityFact(
                    identity,
                    total,
                    context.displayStack(identity),
                    largest == null ? null : largest.sourceSlot(),
                    context.carriedFreeSlotCount(),
                    context.carriedSlotCapacity()));
        }
        return facts;
    }

    private static long updateIdentityFacts(
            Set<ItemIdentity> identities,
            ProjectionIdentityContext identityContext,
            LinkedHashMap<SourceSlotKey, SourceEntryFact> sourceEntries,
            LinkedHashMap<ItemIdentity, CarriedIdentityFact> carriedIdentities
    ) {
        if (identities == null || identities.isEmpty()) {
            return 0L;
        }
        Set<ItemIdentity> affected = canonicalIdentitySet(identities);
        if (affected.isEmpty()) {
            return 0L;
        }
        long removedSourceEntries = removeAffectedSourceEntries(sourceEntries, affected);
        long addedSourceEntries = 0L;
        for (SourceEntryFact fact : sourceEntryFacts(identityContext).values()) {
            if (fact == null || !matchesAny(fact.movableIdentity(), affected)) {
                continue;
            }
            sourceEntries.put(fact.sourceSlot(), fact);
            addedSourceEntries++;
        }
        long touched = Math.max(removedSourceEntries, addedSourceEntries);
        LinkedHashSet<ItemIdentity> affectedCarriedKeys = new LinkedHashSet<>();
        for (ItemIdentity identity : affected) {
            ItemIdentityCollections.add(affectedCarriedKeys, identity);
        }
        long removedCarriedIdentities = removeAffectedCarriedIdentities(carriedIdentities, affectedCarriedKeys);
        long addedCarriedIdentities = 0L;
        for (ItemIdentity key : affectedCarriedKeys) {
            if (key == null) {
                continue;
            }
            int total = identityContext.carriedCounts().count(key);
            if (total <= 0) {
                continue;
            }
            SourceEntryFact largest = largestSourceEntry(sourceEntries, key);
            ItemIdentity factKey = ItemIdentityCollections.key(key);
            carriedIdentities.put(factKey, new CarriedIdentityFact(
                    factKey,
                    total,
                    identityContext.displayStack(key),
                    largest == null ? null : largest.sourceSlot(),
                    identityContext.carriedFreeSlotCount(),
                    identityContext.carriedSlotCapacity()));
            addedCarriedIdentities++;
        }
        touched += Math.max(removedCarriedIdentities, addedCarriedIdentities);
        return touched;
    }

    private static long updateTargetFacts(
            Set<ItemIdentity> identities,
            WorkflowDomainSnapshot workflow,
            ProjectionIdentityContext identityContext,
            Map<StoragePresenceKey, StoragePresenceFact> storagePresence,
            LinkedHashMap<ItemIdentity, TargetFact> targetFacts
    ) {
        if (identities == null || identities.isEmpty() || targetFacts == null) {
            return 0L;
        }
        Set<ItemIdentity> affected = canonicalIdentitySet(identities);
        if (affected.isEmpty()) {
            return 0L;
        }
        long removed = removeAffectedTargetFacts(targetFacts, affected);
        Map<ItemIdentity, TargetFact> nextFacts = targetFacts(workflow, identityContext, storagePresence);
        long added = 0L;
        for (ItemIdentity identity : affected) {
            TargetFact fact = ItemIdentityCollections.find(nextFacts, identity);
            if (fact == null) {
                continue;
            }
            targetFacts.put(fact.identity(), fact);
            added++;
        }
        return Math.max(removed, added);
    }

    private static long updateStorageFacts(
            Set<String> storageIds,
            WorkspaceStorageIndex storageIndex,
            LinkedHashMap<String, StorageMetaFact> storageMeta,
            LinkedHashMap<String, StorageContentsFact> storageContents,
            LinkedHashMap<StoragePresenceKey, StoragePresenceFact> storagePresence
    ) {
        if (storageIds == null || storageIds.isEmpty()) {
            return 0L;
        }
        LinkedHashSet<String> affected = new LinkedHashSet<>();
        for (String storageId : storageIds) {
            if (storageId != null && !storageId.isBlank()) {
                affected.add(storageId);
            }
        }
        if (affected.isEmpty()) {
            return 0L;
        }
        StorageFacts nextFacts = storageFacts(storageIndex);
        long touched = 0L;
        for (String storageId : affected) {
            boolean hadMeta = storageMeta.remove(storageId) != null;
            StorageMetaFact meta = nextFacts.meta().get(storageId);
            if (meta != null) {
                storageMeta.put(storageId, meta);
            }
            if (hadMeta || meta != null) {
                touched++;
            }

            boolean hadContents = storageContents.remove(storageId) != null;
            StorageContentsFact contents = nextFacts.contents().get(storageId);
            if (contents != null) {
                storageContents.put(storageId, contents);
            }
            if (hadContents || contents != null) {
                touched++;
            }

            long removedPresence = removeStoragePresence(storagePresence, storageId);
            long addedPresence = 0L;
            for (Map.Entry<StoragePresenceKey, StoragePresenceFact> entry : nextFacts.presence().entrySet()) {
                StoragePresenceKey key = entry.getKey();
                if (key != null && storageId.equals(key.storageId())) {
                    storagePresence.put(key, entry.getValue());
                    addedPresence++;
                }
            }
            touched += Math.max(removedPresence, addedPresence);
        }
        return touched;
    }

    private static long removeAffectedCarriedIdentities(
            LinkedHashMap<ItemIdentity, CarriedIdentityFact> carriedIdentities,
            Set<ItemIdentity> affected
    ) {
        if (carriedIdentities == null || carriedIdentities.isEmpty() || affected == null || affected.isEmpty()) {
            return 0L;
        }
        long touched = 0L;
        java.util.Iterator<Map.Entry<ItemIdentity, CarriedIdentityFact>> iterator =
                carriedIdentities.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<ItemIdentity, CarriedIdentityFact> entry = iterator.next();
            ItemIdentity key = entry.getKey();
            CarriedIdentityFact fact = entry.getValue();
            if (matchesAny(key, affected) || (fact != null && matchesAny(fact.identity(), affected))) {
                iterator.remove();
                touched++;
            }
        }
        return touched;
    }

    private static long removeAffectedTargetFacts(
            LinkedHashMap<ItemIdentity, TargetFact> targetFacts,
            Set<ItemIdentity> affected
    ) {
        if (targetFacts == null || targetFacts.isEmpty() || affected == null || affected.isEmpty()) {
            return 0L;
        }
        long touched = 0L;
        java.util.Iterator<Map.Entry<ItemIdentity, TargetFact>> iterator =
                targetFacts.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<ItemIdentity, TargetFact> entry = iterator.next();
            ItemIdentity key = entry.getKey();
            TargetFact fact = entry.getValue();
            if (matchesAny(key, affected) || (fact != null && matchesAny(fact.identity(), affected))) {
                iterator.remove();
                touched++;
            }
        }
        return touched;
    }

    private static long removeStoragePresence(
            LinkedHashMap<StoragePresenceKey, StoragePresenceFact> storagePresence,
            String storageId
    ) {
        if (storagePresence == null || storagePresence.isEmpty() || storageId == null || storageId.isBlank()) {
            return 0L;
        }
        long touched = 0L;
        java.util.Iterator<Map.Entry<StoragePresenceKey, StoragePresenceFact>> presenceIterator =
                storagePresence.entrySet().iterator();
        while (presenceIterator.hasNext()) {
            Map.Entry<StoragePresenceKey, StoragePresenceFact> entry = presenceIterator.next();
            if (entry.getKey() != null && storageId.equals(entry.getKey().storageId())) {
                presenceIterator.remove();
                touched++;
            }
        }
        return touched;
    }

    private static long removeAffectedSourceEntries(
            LinkedHashMap<SourceSlotKey, SourceEntryFact> sourceEntries,
            Set<ItemIdentity> affected
    ) {
        if (sourceEntries == null || sourceEntries.isEmpty() || affected == null || affected.isEmpty()) {
            return 0L;
        }
        long touched = 0L;
        java.util.Iterator<Map.Entry<SourceSlotKey, SourceEntryFact>> iterator =
                sourceEntries.entrySet().iterator();
        while (iterator.hasNext()) {
            SourceEntryFact fact = iterator.next().getValue();
            if (fact != null && matchesAny(fact.movableIdentity(), affected)) {
                iterator.remove();
                touched++;
            }
        }
        return touched;
    }

    private static Set<ItemIdentity> canonicalIdentitySet(Set<ItemIdentity> identities) {
        if (identities == null || identities.isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<ItemIdentity> out = new LinkedHashSet<>();
        for (ItemIdentity identity : identities) {
            ItemIdentityCollections.add(out, identity);
        }
        return out.isEmpty() ? Set.of() : Set.copyOf(out);
    }

    private static boolean matchesAny(ItemIdentity identity, Set<ItemIdentity> candidates) {
        if (identity == null || candidates == null || candidates.isEmpty()) {
            return false;
        }
        for (ItemIdentity candidate : candidates) {
            if (ItemIdentityMatcher.matchesMovable(identity, candidate)) {
                return true;
            }
        }
        return false;
    }

    private static SourceEntryFact largestSourceEntry(
            Map<SourceSlotKey, SourceEntryFact> sourceEntries,
            ItemIdentity identity
    ) {
        if (sourceEntries == null || sourceEntries.isEmpty() || identity == null) {
            return null;
        }
        SourceEntryFact best = null;
        for (SourceEntryFact fact : sourceEntries.values()) {
            if (fact == null || !ItemIdentityMatcher.matchesMovable(fact.movableIdentity(), identity)) {
                continue;
            }
            if (best == null || fact.count() > best.count()) {
                best = fact;
            }
        }
        return best;
    }

    private static StorageFacts storageFacts(WorkspaceStorageIndex storageIndex) {
        WorkspaceStorageIndex resolved = storageIndex == null ? WorkspaceStorageIndex.empty() : storageIndex;
        LinkedHashMap<String, StorageMetaFact> meta = new LinkedHashMap<>();
        LinkedHashMap<String, StorageContentsFact> contents = new LinkedHashMap<>();
        LinkedHashMap<StoragePresenceKey, StoragePresenceFact> presence = new LinkedHashMap<>();
        for (WorkspaceStorageIndex.StorageEntry entry : resolved.entries()) {
            if (entry == null || entry.target() == null) {
                continue;
            }
            StorageTargetRef target = entry.target();
            String storageId = target.storageId();
            meta.put(storageId, new StorageMetaFact(
                    storageId,
                    target.targetKind(),
                    target.label(),
                    target.dimensionId(),
                    target.x(),
                    target.y(),
                    target.z(),
                    target.liveReadable(),
                    target.depositTarget(),
                    target.takeTarget(),
                    target.remembered(),
                    target.proximate()));
            SlotWorkspaceViewModel.ChestContentsSnapshot snapshot = entry.snapshot();
            contents.put(storageId, new StorageContentsFact(
                    storageId,
                    storageContentsFingerprint(snapshot),
                    snapshot.slotCount(),
                    filledSlotCount(snapshot),
                    snapshot.contents()));
            for (Map.Entry<ItemIdentity, Integer> count : entry.countsByIdentity().entrySet()) {
                ItemIdentity identity = ItemIdentityCollections.key(count.getKey());
                StoragePresenceKey key = new StoragePresenceKey(storageId, identity);
                presence.put(key, new StoragePresenceFact(
                        key,
                        Math.max(0, count.getValue() == null ? 0 : count.getValue()),
                        representativeStack(snapshot, identity)));
            }
        }
        return new StorageFacts(meta, contents, presence);
    }

    private static LinkedHashMap<ItemIdentity, TargetFact> targetFacts(
            WorkflowDomainSnapshot workflow,
            ProjectionIdentityContext identityContext,
            Map<StoragePresenceKey, StoragePresenceFact> storagePresence
    ) {
        WorkflowDomainSnapshot resolvedWorkflow = workflow == null ? WorkflowDomainSnapshot.empty() : workflow;
        ProjectionIdentityContext context = identityContext == null
                ? ProjectionIdentityContext.empty()
                : identityContext;
        Map<StoragePresenceKey, StoragePresenceFact> storage = storagePresence == null
                ? Map.of()
                : storagePresence;
        WorkflowTabTargets.Resolution targets = WorkflowTabTargets.resolve(
                context.carriedCounts(),
                resolvedWorkflow);
        LinkedHashSet<ItemIdentity> identities = new LinkedHashSet<>();
        for (ItemIdentity identity : targets.desiredCounts().keySet()) {
            ItemIdentityCollections.add(identities, identity);
        }
        for (ItemIdentity identity : targets.wantedCounts().keySet()) {
            ItemIdentityCollections.add(identities, identity);
        }
        for (ItemIdentity identity : targets.missingWorkflowIdentities()) {
            ItemIdentityCollections.add(identities, identity);
        }
        for (WorkflowAcceptedInputRule rule : targets.acceptedInputs()) {
            if (rule != null && rule.exactItem()) {
                ItemIdentityCollections.add(identities, rule.identity());
            } else if (rule != null && rule.itemTag()) {
                addAcceptedTagEvidenceIdentities(identities, rule, context, storage);
            }
        }
        for (ItemIdentity identity : resolvedWorkflow.workflowProjection().junkTags()) {
            ItemIdentityCollections.add(identities, identity);
        }

        LinkedHashMap<ItemIdentity, TargetFact> facts = new LinkedHashMap<>();
        for (ItemIdentity identity : identities) {
            ItemIdentity key = ItemIdentityCollections.key(identity);
            if (key == null) {
                continue;
            }
            int desiredCount = targets.desiredCount(key);
            int wantedCount = targets.wantedCount(key);
            boolean desiredFromWorkflowTab = targets.desiredFromWorkflowTab(key);
            boolean kitNeeded = ItemIdentityCollections.containsCanonical(targets.missingWorkflowIdentities(), key);
            boolean junk = ItemIdentityCollections.containsCanonical(
                    resolvedWorkflow.workflowProjection().junkTags(),
                    key);
            boolean acceptedWorkflowInput = targets.acceptedInput(
                    key,
                    explicitItemTags(key, context, storage));
            if (desiredCount <= 0
                    && wantedCount <= 0
                    && !desiredFromWorkflowTab
                    && !kitNeeded
                    && !junk
                    && !acceptedWorkflowInput) {
                continue;
            }
            facts.put(key, new TargetFact(
                    key,
                    desiredCount,
                    desiredFromWorkflowTab,
                    wantedCount,
                    kitNeeded,
                    junk,
                    acceptedWorkflowInput));
        }
        return facts;
    }

    private static void addAcceptedTagEvidenceIdentities(
            Set<ItemIdentity> identities,
            WorkflowAcceptedInputRule rule,
            ProjectionIdentityContext identityContext,
            Map<StoragePresenceKey, StoragePresenceFact> storagePresence
    ) {
        if (identities == null || rule == null || !rule.itemTag() || rule.tagId().isBlank()) {
            return;
        }
        ProjectionIdentityContext context = identityContext == null
                ? ProjectionIdentityContext.empty()
                : identityContext;
        for (Map.Entry<ItemIdentity, ItemStack> entry : context.displayStacksByIdentity().entrySet()) {
            if (stackHasTag(entry.getValue(), rule.tagId())) {
                ItemIdentityCollections.add(identities, entry.getKey());
            }
        }
        if (storagePresence == null || storagePresence.isEmpty()) {
            return;
        }
        for (StoragePresenceFact fact : storagePresence.values()) {
            if (fact != null
                    && fact.key() != null
                    && stackHasTag(fact.representativeDisplayStack(), rule.tagId())) {
                ItemIdentityCollections.add(identities, fact.key().identity());
            }
        }
    }

    private static Set<String> explicitItemTags(
            ItemIdentity identity,
            ProjectionIdentityContext identityContext,
            Map<StoragePresenceKey, StoragePresenceFact> storagePresence
    ) {
        if (identity == null) {
            return Set.of();
        }
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        ProjectionIdentityContext context = identityContext == null
                ? ProjectionIdentityContext.empty()
                : identityContext;
        tags.addAll(ItemStackTags.itemTagIds(context.displayStack(identity)));
        if (storagePresence != null) {
            for (StoragePresenceFact fact : storagePresence.values()) {
                if (fact == null
                        || fact.key() == null
                        || fact.key().identity() == null
                        || !ItemIdentityMatcher.matchesMovable(fact.key().identity(), identity)) {
                    continue;
                }
                tags.addAll(ItemStackTags.itemTagIds(fact.representativeDisplayStack()));
            }
        }
        return tags.isEmpty() ? Set.of() : Set.copyOf(tags);
    }

    private static boolean stackHasTag(ItemStack stack, String tagId) {
        return tagId != null
                && !tagId.isBlank()
                && ItemStackTags.itemTagIds(stack).contains(tagId);
    }

    private static String storageContentsFingerprint(SlotWorkspaceViewModel.ChestContentsSnapshot snapshot) {
        if (snapshot == null || snapshot.contents().isEmpty()) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        List<ItemStack> contents = snapshot.contents();
        List<Integer> slotIndices = snapshot.slotIndices();
        for (int i = 0; i < contents.size(); i++) {
            ItemStack stack = contents.get(i);
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            int slotIndex = i < slotIndices.size() && slotIndices.get(i) != null ? slotIndices.get(i) : i;
            if (!out.isEmpty()) {
                out.append('|');
            }
            out.append(slotIndex)
                    .append(':')
                    .append(SlotStackAccess.current().itemId(stack))
                    .append(':')
                    .append(stack.getCount())
                    .append(':')
                    .append(SlotStackAccess.current().dataFingerprint(stack));
        }
        return out.toString();
    }

    private static int filledSlotCount(SlotWorkspaceViewModel.ChestContentsSnapshot snapshot) {
        if (snapshot == null || snapshot.contents().isEmpty()) {
            return 0;
        }
        int count = 0;
        for (ItemStack stack : snapshot.contents()) {
            if (stack != null && !stack.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private static ItemStack representativeStack(
            SlotWorkspaceViewModel.ChestContentsSnapshot snapshot,
            ItemIdentity identity
    ) {
        if (snapshot == null || identity == null) {
            return ItemStack.EMPTY;
        }
        for (ItemStack stack : snapshot.contents()) {
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            if (ItemIdentityMatcher.matchesMovable(ItemIdentityMatcher.create(stack), identity)) {
                return stack.copy();
            }
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack copyStack(ItemStack stack) {
        return stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
    }

    private record StorageFacts(
            Map<String, StorageMetaFact> meta,
            Map<String, StorageContentsFact> contents,
            Map<StoragePresenceKey, StoragePresenceFact> presence
    ) {
    }

    public record UpdateResult(
            WorkspaceProjectionStore store,
            long factsUpdated,
            long factsReused,
            boolean localized
    ) {
        public UpdateResult {
            store = store == null ? WorkspaceProjectionStore.empty() : store;
            factsUpdated = Math.max(0L, factsUpdated);
            factsReused = Math.max(0L, factsReused);
        }
    }

    public record SourceSlotKey(String sourceId, String stableEntryKey, int slotIndex) {
        public SourceSlotKey {
            sourceId = sourceId == null ? "" : sourceId;
            stableEntryKey = stableEntryKey == null ? "" : stableEntryKey;
            slotIndex = Math.max(-1, slotIndex);
        }
    }

    public record ExactStackKey(String itemId, String dataFingerprint) {
        public ExactStackKey {
            itemId = itemId == null ? "" : itemId;
            dataFingerprint = dataFingerprint == null ? "" : dataFingerprint;
        }

        public static ExactStackKey from(ItemStack stack) {
            return new ExactStackKey(
                    SlotStackAccess.current().itemId(stack),
                    SlotStackAccess.current().dataFingerprint(stack));
        }
    }

    public record SourceEntryFact(
            SourceSlotKey sourceSlot,
            ItemIdentity exactIdentity,
            ItemIdentity movableIdentity,
            int count,
            ItemStack displayStack,
            ExactStackKey exactStackKey,
            String sourceDomain,
            String sourceRole
    ) {
        public SourceEntryFact {
            count = Math.max(0, count);
            displayStack = copyStack(displayStack);
            sourceDomain = sourceDomain == null ? "" : sourceDomain;
            sourceRole = sourceRole == null ? "" : sourceRole;
        }
    }

    public record CarriedIdentityFact(
            ItemIdentity identity,
            int totalCount,
            ItemStack representativeDisplayStack,
            SourceSlotKey largestSourceSlot,
            int carriedFreeSlotCount,
            int carriedSlotCapacity
    ) {
        public CarriedIdentityFact {
            totalCount = Math.max(0, totalCount);
            representativeDisplayStack = copyStack(representativeDisplayStack);
            carriedFreeSlotCount = Math.max(0, carriedFreeSlotCount);
            carriedSlotCapacity = Math.max(carriedFreeSlotCount, carriedSlotCapacity);
        }
    }

    public record StorageMetaFact(
            String storageId,
            String targetKind,
            String label,
            String dimensionId,
            int x,
            int y,
            int z,
            boolean liveReadable,
            boolean depositTarget,
            boolean takeTarget,
            boolean remembered,
            boolean proximate
    ) {
        public StorageMetaFact {
            storageId = storageId == null ? "" : storageId;
            targetKind = targetKind == null ? "" : targetKind;
            label = label == null ? "" : label;
            dimensionId = dimensionId == null ? "" : dimensionId;
        }
    }

    public record StorageContentsFact(
            String storageId,
            String contentsFingerprint,
            int slotCount,
            int filledCount,
            List<ItemStack> slotSummaries
    ) {
        public StorageContentsFact {
            storageId = storageId == null ? "" : storageId;
            contentsFingerprint = contentsFingerprint == null ? "" : contentsFingerprint;
            slotCount = Math.max(0, slotCount);
            filledCount = Math.max(0, filledCount);
            slotSummaries = copyStacks(slotSummaries);
        }
    }

    public record StoragePresenceKey(String storageId, ItemIdentity identity) {
        public StoragePresenceKey {
            storageId = storageId == null ? "" : storageId;
            identity = ItemIdentityCollections.key(identity);
        }
    }

    public record StoragePresenceFact(
            StoragePresenceKey key,
            int count,
            ItemStack representativeDisplayStack
    ) {
        public StoragePresenceFact {
            count = Math.max(0, count);
            representativeDisplayStack = copyStack(representativeDisplayStack);
        }
    }

    public record TargetFact(
            ItemIdentity identity,
            int desiredCount,
            boolean desiredCountFromWorkflowTab,
            int wantedCount,
            boolean kitNeeded,
            boolean junk,
            boolean acceptedWorkflowInput
    ) {
        public TargetFact {
            identity = ItemIdentityCollections.key(identity);
            desiredCount = Math.max(0, desiredCount);
            desiredCountFromWorkflowTab = desiredCount > 0 && desiredCountFromWorkflowTab;
            wantedCount = Math.max(0, wantedCount);
        }
    }

    private static List<ItemStack> copyStacks(List<ItemStack> stacks) {
        if (stacks == null || stacks.isEmpty()) {
            return List.of();
        }
        java.util.ArrayList<ItemStack> copy = new java.util.ArrayList<>(stacks.size());
        for (ItemStack stack : stacks) {
            copy.add(copyStack(stack));
        }
        return List.copyOf(copy);
    }
}
