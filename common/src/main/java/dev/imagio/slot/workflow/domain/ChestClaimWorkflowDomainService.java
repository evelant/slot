package dev.imagio.slot.workflow.domain;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class ChestClaimWorkflowDomainService {
    private final WorkflowDomainStateRepository repository;
    private final StorageAreaWorkflowDomainService storageAreaService;
    private final Runnable mutationObserver;

    public ChestClaimWorkflowDomainService(WorkflowDomainStateRepository repository) {
        this(repository, new StorageAreaWorkflowDomainService(repository), () -> {
        });
    }

    public ChestClaimWorkflowDomainService(
            WorkflowDomainStateRepository repository,
            Runnable mutationObserver
    ) {
        this(repository, new StorageAreaWorkflowDomainService(repository, mutationObserver), mutationObserver);
    }

    public ChestClaimWorkflowDomainService(
            WorkflowDomainStateRepository repository,
            StorageAreaWorkflowDomainService storageAreaService,
            Runnable mutationObserver
    ) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.storageAreaService = Objects.requireNonNull(storageAreaService, "storageAreaService");
        this.mutationObserver = mutationObserver == null ? () -> {
        } : mutationObserver;
    }

    public ClaimedChestMap claimedChestMap() {
        return repository.workflowProjection().claimedChestMap();
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
            String label,
            UUID areaId
    ) {
        return claim(anchors, atlasX, atlasY, label, areaId, DomainEventMetadata.origin("workflow.storage.chest.claim"));
    }

    public ClaimedChest claim(
            Set<ChestAnchor> anchors,
            int atlasX,
            int atlasY,
            String label,
            UUID areaId,
            DomainEventMetadata metadata
    ) {
        return claimWithId(UUID.randomUUID(), anchors, atlasX, atlasY, label, areaId, metadata);
    }

    public ClaimedChest claimWithId(
            UUID storageId,
            Set<ChestAnchor> anchors,
            int atlasX,
            int atlasY,
            String label,
            UUID areaId
    ) {
        return claimWithId(
                storageId,
                anchors,
                atlasX,
                atlasY,
                label,
                areaId,
                DomainEventMetadata.origin("workflow.storage.chest.claim")
        );
    }

    public ClaimedChest claimWithId(
            UUID storageId,
            Set<ChestAnchor> anchors,
            int atlasX,
            int atlasY,
            String label,
            UUID areaId,
            DomainEventMetadata metadata
    ) {
        if (storageId == null || areaId == null) {
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
        if (!repository.workflowProjection().storageAreaMap().contains(areaId)) {
            if (!StorageAreaMap.DEFAULT_AREA_ID.equals(areaId)) {
                return null;
            }
            // Lazily materialise the default area on the first claim into it.
            storageAreaService.ensureDefaultArea(atlasX, atlasY, metadata);
        }
        ClaimedChest chest = new ClaimedChest(storageId, copied, atlasX, atlasY, label, areaId);
        repository.appendWorkflowEvent(
                new WorkflowEvent.ClaimedChestCreated(chest),
                resolveMetadata(metadata, "workflow.storage.chest.claim")
        );
        mutationObserver.run();
        return claimedChestMap().chest(storageId);
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

    public ClaimedChest moveChestToArea(UUID storageId, UUID areaId) {
        return moveChestToArea(
                storageId,
                areaId,
                DomainEventMetadata.origin("workflow.storage.chest.move_to_area")
        );
    }

    public ClaimedChest moveChestToArea(UUID storageId, UUID areaId, DomainEventMetadata metadata) {
        if (storageId == null || areaId == null) {
            return null;
        }
        ClaimedChest existing = claimedChestMap().chest(storageId);
        if (existing == null) {
            return null;
        }
        if (areaId.equals(existing.areaId())) {
            return existing;
        }
        if (!repository.workflowProjection().storageAreaMap().contains(areaId)) {
            return null;
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.ClaimedChestAreaChanged(storageId, areaId),
                resolveMetadata(metadata, "workflow.storage.chest.move_to_area")
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

    private static DomainEventMetadata resolveMetadata(DomainEventMetadata metadata, String origin) {
        return (metadata == null ? DomainEventMetadata.origin("") : metadata).withOrigin(origin);
    }
}
