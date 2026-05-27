package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Per-player workflow service for claimed chests + their learned affinity.
 *
 * <p>Replaces the old explicit-claim / chest-link / storage-area trio with
 * a single surface: chests are auto-claimed on first deposit, role-gated by
 * {@link ChestRole}, and routing reads {@link ChestAffinityMap}. There is no
 * longer an "area" concept.
 *
 * <p>See docs/plans/learned-storage.md.
 */
public final class ChestClaimWorkflowDomainService {
    private final WorkflowDomainStateRepository repository;
    private final Runnable mutationObserver;
    private final Map<ItemIdentity, PendingRehomeOrigin> pendingRehomeOrigins = new LinkedHashMap<>();

    public ChestClaimWorkflowDomainService(WorkflowDomainStateRepository repository) {
        this(repository, () -> {
        });
    }

    public ChestClaimWorkflowDomainService(
            WorkflowDomainStateRepository repository,
            Runnable mutationObserver
    ) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.mutationObserver = mutationObserver == null ? () -> {
        } : mutationObserver;
    }

    public ClaimedChestMap claimedChestMap() {
        return repository.workflowProjection().claimedChestMap();
    }

    public ChestAffinityMap chestAffinityMap() {
        return repository.workflowProjection().chestAffinityMap();
    }

    public ClaimedChest chest(UUID storageId) {
        return claimedChestMap().chest(storageId);
    }

    public ClaimedChest chestByAnchor(ChestAnchor anchor) {
        return claimedChestMap().chestByAnchor(anchor);
    }

    public ClaimedChest claim(
            Set<ChestAnchor> anchors,
            int atlasX,
            int atlasY,
            String label
    ) {
        return claim(anchors, atlasX, atlasY, label, DomainEventMetadata.origin("workflow.storage.chest.claim"));
    }

    public ClaimedChest claim(
            Set<ChestAnchor> anchors,
            int atlasX,
            int atlasY,
            String label,
            DomainEventMetadata metadata
    ) {
        return claimWithId(UUID.randomUUID(), anchors, atlasX, atlasY, label, metadata);
    }

    public ClaimedChest claimWithId(
            UUID storageId,
            Set<ChestAnchor> anchors,
            int atlasX,
            int atlasY,
            String label
    ) {
        return claimWithId(storageId, anchors, atlasX, atlasY, label,
                DomainEventMetadata.origin("workflow.storage.chest.claim"));
    }

    public ClaimedChest claimWithId(
            UUID storageId,
            Set<ChestAnchor> anchors,
            int atlasX,
            int atlasY,
            String label,
            DomainEventMetadata metadata
    ) {
        if (storageId == null) {
            return null;
        }
        Set<ChestAnchor> copied = ClaimedChest.copyAnchors(anchors);
        if (copied.isEmpty()) {
            return null;
        }
        ClaimedChestMap current = claimedChestMap();
        if (current.chest(storageId) != null) {
            return null;
        }
        for (ChestAnchor anchor : copied) {
            if (current.chestByAnchor(anchor) != null) {
                return null;
            }
        }
        ClaimedChest chest = new ClaimedChest(storageId, copied, atlasX, atlasY, label);
        repository.appendWorkflowEvent(
                new WorkflowEvent.ClaimedChestCreated(chest),
                resolveMetadata(metadata, "workflow.storage.chest.claim")
        );
        mutationObserver.run();
        return claimedChestMap().chest(storageId);
    }

    public boolean setRole(UUID storageId, ChestRole role) {
        if (storageId == null) {
            return false;
        }
        ClaimedChest existing = claimedChestMap().chest(storageId);
        if (existing == null) {
            return false;
        }
        ChestRole next = role == null ? ChestRole.STORAGE : role;
        if (existing.role() == next) {
            return true;
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.ClaimedChestRoleChanged(storageId, next),
                DomainEventMetadata.origin("workflow.storage.chest.role_changed")
        );
        if (!next.learnsAffinity()) {
            pendingRehomeOrigins.entrySet().removeIf(entry ->
                    entry.getValue() != null && storageId.equals(entry.getValue().storageId()));
        }
        mutationObserver.run();
        return true;
    }

    /**
     * Auto-claim a chest if no claim exists for the supplied {@code anchor}.
     * Returns the live claim record (existing or new).
     */
    public ClaimedChest autoClaimByAnchor(
            ChestAnchor anchor,
            int atlasX,
            int atlasY,
            DomainEventMetadata metadata
    ) {
        if (anchor == null) {
            return null;
        }
        ClaimedChest existing = chestByAnchor(anchor);
        if (existing != null) {
            return existing;
        }
        return claim(Set.of(anchor), atlasX, atlasY, "",
                resolveMetadata(metadata, "workflow.storage.chest.auto_claim"));
    }

    public ClaimedChest moveChest(UUID storageId, int atlasX, int atlasY) {
        return moveChest(storageId, atlasX, atlasY, DomainEventMetadata.origin("workflow.storage.chest.move"));
    }

    public ClaimedChest moveChest(UUID storageId, int atlasX, int atlasY, DomainEventMetadata metadata) {
        if (storageId == null) {
            return null;
        }
        ClaimedChest existing = claimedChestMap().chest(storageId);
        if (existing == null) {
            return null;
        }
        if (existing.atlasX() == atlasX && existing.atlasY() == atlasY) {
            return existing;
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.ClaimedChestMoved(storageId, atlasX, atlasY),
                resolveMetadata(metadata, "workflow.storage.chest.move")
        );
        mutationObserver.run();
        return claimedChestMap().chest(storageId);
    }

    public ClaimedChest updateAnchors(UUID storageId, Set<ChestAnchor> anchors) {
        return updateAnchors(
                storageId,
                anchors,
                DomainEventMetadata.origin("workflow.storage.chest.anchors")
        );
    }

    public ClaimedChest updateAnchors(
            UUID storageId,
            Set<ChestAnchor> anchors,
            DomainEventMetadata metadata
    ) {
        if (storageId == null) {
            return null;
        }
        ClaimedChest existing = claimedChestMap().chest(storageId);
        if (existing == null) {
            return null;
        }
        Set<ChestAnchor> copied = ClaimedChest.copyAnchors(anchors);
        if (copied.equals(existing.anchors())) {
            return existing;
        }
        ClaimedChestMap current = claimedChestMap();
        for (ChestAnchor anchor : copied) {
            ClaimedChest owner = current.chestByAnchor(anchor);
            if (owner != null && !storageId.equals(owner.storageId())) {
                return null;
            }
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.ClaimedChestAnchorsChanged(storageId, copied),
                resolveMetadata(metadata, "workflow.storage.chest.anchors")
        );
        mutationObserver.run();
        return claimedChestMap().chest(storageId);
    }

    public ClaimedChest removeAnchor(UUID storageId, ChestAnchor anchor) {
        return removeAnchor(
                storageId,
                anchor,
                DomainEventMetadata.origin("workflow.storage.chest.anchors")
        );
    }

    public ClaimedChest removeAnchor(UUID storageId, ChestAnchor anchor, DomainEventMetadata metadata) {
        if (storageId == null || anchor == null) {
            return null;
        }
        ClaimedChest existing = claimedChestMap().chest(storageId);
        if (existing == null || !existing.anchors().contains(anchor)) {
            return existing;
        }
        LinkedHashSet<ChestAnchor> remaining = new LinkedHashSet<>(existing.anchors());
        remaining.remove(anchor);
        if (remaining.isEmpty()) {
            deleteChest(storageId, resolveMetadata(metadata, "workflow.storage.chest.anchors"));
            return null;
        }
        return updateAnchors(storageId, remaining, metadata);
    }

    public ClaimedChest relabelChest(UUID storageId, String label) {
        return relabelChest(
                storageId,
                label,
                DomainEventMetadata.origin("workflow.storage.chest.relabel")
        );
    }

    public ClaimedChest relabelChest(UUID storageId, String label, DomainEventMetadata metadata) {
        if (storageId == null) {
            return null;
        }
        ClaimedChest existing = claimedChestMap().chest(storageId);
        if (existing == null) {
            return null;
        }
        String normalized = label == null ? "" : label.trim();
        if (normalized.equals(existing.label())) {
            return existing;
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.ClaimedChestRelabeled(storageId, normalized),
                resolveMetadata(metadata, "workflow.storage.chest.relabel")
        );
        mutationObserver.run();
        return claimedChestMap().chest(storageId);
    }

    public boolean deleteChest(UUID storageId) {
        return deleteChest(storageId, DomainEventMetadata.origin("workflow.storage.chest.delete"));
    }

    public boolean deleteChest(UUID storageId, DomainEventMetadata metadata) {
        if (storageId == null || claimedChestMap().chest(storageId) == null) {
            return false;
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.ClaimedChestDeleted(storageId),
                resolveMetadata(metadata, "workflow.storage.chest.delete")
        );
        mutationObserver.run();
        return true;
    }

    /**
     * Record one observed deposit of {@code identity} into chest
     * {@code storageId}. Bumps affinity[storageId, identity].
     */
    public void recordDeposit(UUID storageId, ItemIdentity identity, int count, long tick) {
        recordDeposit(storageId, identity, count, tick,
                DomainEventMetadata.origin("workflow.storage.chest.deposit_observed"));
    }

    public void recordDeposit(
            UUID storageId,
            ItemIdentity identity,
            int count,
            long tick,
            DomainEventMetadata metadata
    ) {
        if (storageId == null || identity == null || claimedChestMap().chest(storageId) == null) {
            return;
        }
        ItemIdentity normalized = ItemIdentityMatcher.normalizeMovable(identity);
        ClaimedChest destination = claimedChestMap().chest(storageId);
        if (destination == null || !destination.role().learnsAffinity()) {
            return;
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.ChestDepositObserved(storageId, normalized, count, tick),
                resolveMetadata(metadata, "workflow.storage.chest.deposit_observed")
        );
        applyPendingRehome(storageId, normalized);
        repository.appendContextualSignal(
                new ContextualSignalEvent(
                        ContextualSignalKind.ITEM_DEPOSITED_TO_STORAGE,
                        normalized,
                        count,
                        tick,
                        "",
                        "",
                        storageId.toString(),
                        Map.of()),
                resolveMetadata(metadata, "contextual.storage.deposit_observed")
        );
        mutationObserver.run();
    }

    public void recordPossibleRehomeTake(
            UUID storageId,
            Map<ItemIdentity, Integer> takes,
            Set<ItemIdentity> identitiesStillPresent,
            long tick
    ) {
        if (storageId == null || takes == null || takes.isEmpty()) {
            return;
        }
        ClaimedChest origin = claimedChestMap().chest(storageId);
        if (origin == null || !origin.role().learnsAffinity()) {
            return;
        }
        Set<ItemIdentity> present = identitiesStillPresent == null ? Set.of() : identitiesStillPresent;
        for (Map.Entry<ItemIdentity, Integer> entry : takes.entrySet()) {
            if (entry == null || entry.getKey() == null || entry.getValue() == null || entry.getValue() <= 0) {
                continue;
            }
            ItemIdentity normalized = ItemIdentityMatcher.normalizeMovable(entry.getKey());
            if (chestAffinityMap().score(storageId, normalized) <= 0) {
                continue;
            }
            if (containsIdentity(present, normalized)) {
                PendingRehomeOrigin pending = pendingRehomeOrigins.get(normalized);
                if (pending != null && storageId.equals(pending.storageId())) {
                    pendingRehomeOrigins.remove(normalized);
                }
                continue;
            }
            pendingRehomeOrigins.put(normalized, new PendingRehomeOrigin(storageId, tick));
        }
    }

    private void applyPendingRehome(UUID destinationStorageId, ItemIdentity identity) {
        PendingRehomeOrigin pending = pendingRehomeOrigins.remove(identity);
        if (pending == null || pending.storageId() == null || pending.storageId().equals(destinationStorageId)) {
            return;
        }
        ClaimedChest origin = claimedChestMap().chest(pending.storageId());
        if (origin == null || !origin.role().learnsAffinity()) {
            return;
        }
        if (chestAffinityMap().score(pending.storageId(), identity) <= 0) {
            return;
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.ChestAffinityForgotten(pending.storageId(), identity),
                DomainEventMetadata.origin("workflow.storage.chest.rehome_on_move")
        );
    }

    private static boolean containsIdentity(Set<ItemIdentity> identities, ItemIdentity identity) {
        if (identities == null || identities.isEmpty() || identity == null) {
            return false;
        }
        for (ItemIdentity candidate : identities) {
            if (identity.equals(ItemIdentityMatcher.normalizeMovable(candidate))) {
                return true;
            }
        }
        return false;
    }

    /** Forget affinity[storageId, identity]. */
    public boolean forgetIdentity(UUID storageId, ItemIdentity identity) {
        if (storageId == null || identity == null) {
            return false;
        }
        ItemIdentity normalized = ItemIdentityMatcher.normalizeMovable(identity);
        if (chestAffinityMap().score(storageId, normalized) <= 0) {
            return false;
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.ChestAffinityForgotten(storageId, normalized),
                DomainEventMetadata.origin("workflow.storage.chest.forget_identity")
        );
        mutationObserver.run();
        return true;
    }

    /**
     * Player-authored label for a derived chest cluster. {@code clusterId}
     * matches the keys produced by
     * {@link ChestClusterMap#derive(ClaimedChestMap)}; an empty {@code label}
     * clears the rename and falls back to the default ordinal label.
     */
    public boolean relabelCluster(String clusterId, String label) {
        if (clusterId == null || clusterId.isBlank()) {
            return false;
        }
        String normalized = label == null ? "" : label.trim();
        repository.appendWorkflowEvent(
                new WorkflowEvent.ChestClusterRelabeled(clusterId, normalized),
                DomainEventMetadata.origin("workflow.storage.cluster.relabel")
        );
        mutationObserver.run();
        return true;
    }

    private static DomainEventMetadata resolveMetadata(DomainEventMetadata metadata, String origin) {
        return (metadata == null ? DomainEventMetadata.origin("") : metadata).withOrigin(origin);
    }

    private record PendingRehomeOrigin(UUID storageId, long tick) {
    }
}
