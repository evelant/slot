package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Per-player workflow service for claimed chests + their learned affinity.
 *
 * <p>Replaces the old explicit-claim / chest-link / storage-area trio with
 * a single surface: chests are auto-claimed on first deposit, and routing
 * reads {@link ChestAffinityMap}. There is no longer an "area" concept.
 *
 * <p>See docs/plans/learned-storage.md.
 */
public final class ChestClaimWorkflowDomainService {
    private final WorkflowDomainStateRepository repository;
    private final Runnable mutationObserver;

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
        if (storageId == null || anchor == null) {
            return null;
        }
        ClaimedChest existing = claimedChestMap().chest(storageId);
        if (existing == null || !existing.anchors().contains(anchor)) {
            return existing;
        }
        LinkedHashSet<ChestAnchor> remaining = new LinkedHashSet<>(existing.anchors());
        remaining.remove(anchor);
        return updateAnchors(storageId, remaining);
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
        repository.appendWorkflowEvent(
                new WorkflowEvent.ChestDepositObserved(storageId, identity, count, tick),
                resolveMetadata(metadata, "workflow.storage.chest.deposit_observed")
        );
        mutationObserver.run();
    }

    /** Forget affinity[storageId, identity]. */
    public boolean forgetIdentity(UUID storageId, ItemIdentity identity) {
        if (storageId == null || identity == null) {
            return false;
        }
        if (chestAffinityMap().score(storageId, identity) <= 0) {
            return false;
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.ChestAffinityForgotten(storageId, identity),
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

    /** Forget all affinity for this chest. */
    public boolean forgetChestAffinity(UUID storageId) {
        if (storageId == null) {
            return false;
        }
        if (chestAffinityMap().forChest(storageId).isEmpty()) {
            return false;
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.ChestAffinityCleared(storageId),
                DomainEventMetadata.origin("workflow.storage.chest.forget_chest_affinity")
        );
        mutationObserver.run();
        return true;
    }

    private static DomainEventMetadata resolveMetadata(DomainEventMetadata metadata, String origin) {
        return (metadata == null ? DomainEventMetadata.origin("") : metadata).withOrigin(origin);
    }
}
