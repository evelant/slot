package dev.imagio.slot.workflow.domain;

import java.util.Objects;
import java.util.UUID;

public final class ChestLinkWorkflowDomainService {
    private final WorkflowDomainStateRepository repository;
    private final Runnable mutationObserver;

    public ChestLinkWorkflowDomainService(WorkflowDomainStateRepository repository) {
        this(repository, () -> {
        });
    }

    public ChestLinkWorkflowDomainService(
            WorkflowDomainStateRepository repository,
            Runnable mutationObserver
    ) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.mutationObserver = mutationObserver == null ? () -> {
        } : mutationObserver;
    }

    public ChestLinkMap chestLinkMap() {
        return repository.workflowProjection().chestLinkMap();
    }

    public boolean linkIslandToChest(String islandId, UUID storageId) {
        return linkIslandToChest(
                islandId,
                storageId,
                DomainEventMetadata.origin("workflow.storage.link.create")
        );
    }

    public boolean linkIslandToChest(String islandId, UUID storageId, DomainEventMetadata metadata) {
        if (islandId == null || islandId.isBlank() || storageId == null) {
            return false;
        }
        String normalizedId = islandId.trim();
        WorkflowProjection.Snapshot projection = repository.workflowProjection();
        ClaimedChest chest = projection.claimedChestMap().chest(storageId);
        if (chest == null) {
            return false;
        }
        VisualAtlasIsland island = projection.visualHomeMap().island(normalizedId);
        if (island == null) {
            return false;
        }
        if (projection.chestLinkMap().contains(normalizedId, storageId)) {
            return true;
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.ChestLinkCreated(normalizedId, storageId),
                resolveMetadata(metadata, "workflow.storage.link.create")
        );
        if (chest.label().isBlank() && !island.label().isBlank()) {
            repository.appendWorkflowEvent(
                    new WorkflowEvent.ClaimedChestRelabeled(storageId, island.label()),
                    resolveMetadata(metadata, "workflow.storage.link.auto_label")
            );
        }
        mutationObserver.run();
        return chestLinkMap().contains(normalizedId, storageId);
    }

    public boolean unlinkIslandFromChest(String islandId, UUID storageId) {
        return unlinkIslandFromChest(
                islandId,
                storageId,
                DomainEventMetadata.origin("workflow.storage.link.remove")
        );
    }

    public boolean unlinkIslandFromChest(
            String islandId,
            UUID storageId,
            DomainEventMetadata metadata
    ) {
        if (islandId == null || islandId.isBlank() || storageId == null) {
            return false;
        }
        String normalizedId = islandId.trim();
        if (!chestLinkMap().contains(normalizedId, storageId)) {
            return false;
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.ChestLinkRemoved(normalizedId, storageId),
                resolveMetadata(metadata, "workflow.storage.link.remove")
        );
        mutationObserver.run();
        return !chestLinkMap().contains(normalizedId, storageId);
    }

    private static DomainEventMetadata resolveMetadata(DomainEventMetadata metadata, String origin) {
        return (metadata == null ? DomainEventMetadata.origin("") : metadata).withOrigin(origin);
    }
}
