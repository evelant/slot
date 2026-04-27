package dev.imagio.slot.workflow.domain;

import java.util.Objects;
import java.util.UUID;

public final class StorageAreaWorkflowDomainService {
    private final WorkflowDomainStateRepository repository;
    private final Runnable mutationObserver;

    public StorageAreaWorkflowDomainService(WorkflowDomainStateRepository repository) {
        this(repository, () -> {
        });
    }

    public StorageAreaWorkflowDomainService(
            WorkflowDomainStateRepository repository,
            Runnable mutationObserver
    ) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.mutationObserver = mutationObserver == null ? () -> {
        } : mutationObserver;
    }

    public StorageAreaMap storageAreaMap() {
        return repository.workflowProjection().storageAreaMap();
    }

    public StorageArea area(UUID areaId) {
        return storageAreaMap().area(areaId);
    }

    public StorageArea createArea(String label, int atlasX, int atlasY) {
        return createArea(label, atlasX, atlasY, DomainEventMetadata.origin("workflow.storage.area.create"));
    }

    public StorageArea createArea(
            String label,
            int atlasX,
            int atlasY,
            DomainEventMetadata metadata
    ) {
        String normalized = label == null ? "" : label.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        StorageAreaMap current = storageAreaMap();
        int displayOrder = current.nextDisplayOrder();
        StorageArea area = new StorageArea(
                UUID.randomUUID(),
                normalized,
                StorageAreaMap.paletteColorFor(displayOrder),
                atlasX,
                atlasY,
                displayOrder
        );
        repository.appendWorkflowEvent(
                new WorkflowEvent.StorageAreaCreated(area),
                resolveMetadata(metadata, "workflow.storage.area.create")
        );
        mutationObserver.run();
        return storageAreaMap().area(area.areaId());
    }

    public StorageArea ensureDefaultArea(int atlasX, int atlasY) {
        return ensureDefaultArea(atlasX, atlasY, DomainEventMetadata.origin("workflow.storage.area.ensure_default"));
    }

    public StorageArea ensureDefaultArea(int atlasX, int atlasY, DomainEventMetadata metadata) {
        StorageArea existing = storageAreaMap().area(StorageAreaMap.DEFAULT_AREA_ID);
        if (existing != null) {
            return existing;
        }
        StorageArea defaultArea = StorageAreaMap.defaultArea(atlasX, atlasY);
        repository.appendWorkflowEvent(
                new WorkflowEvent.StorageAreaCreated(defaultArea),
                resolveMetadata(metadata, "workflow.storage.area.ensure_default")
        );
        mutationObserver.run();
        return storageAreaMap().area(StorageAreaMap.DEFAULT_AREA_ID);
    }

    public StorageArea renameArea(UUID areaId, String label) {
        return renameArea(areaId, label, DomainEventMetadata.origin("workflow.storage.area.rename"));
    }

    public StorageArea renameArea(UUID areaId, String label, DomainEventMetadata metadata) {
        if (areaId == null) {
            return null;
        }
        StorageArea existing = storageAreaMap().area(areaId);
        if (existing == null) {
            return null;
        }
        String normalized = label == null ? "" : label.trim();
        if (normalized.isEmpty() || normalized.equals(existing.label())) {
            return existing;
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.StorageAreaRenamed(areaId, normalized),
                resolveMetadata(metadata, "workflow.storage.area.rename")
        );
        mutationObserver.run();
        return storageAreaMap().area(areaId);
    }

    public StorageArea recolorArea(UUID areaId, int color) {
        return recolorArea(areaId, color, DomainEventMetadata.origin("workflow.storage.area.recolor"));
    }

    public StorageArea recolorArea(UUID areaId, int color, DomainEventMetadata metadata) {
        if (areaId == null) {
            return null;
        }
        StorageArea existing = storageAreaMap().area(areaId);
        if (existing == null) {
            return null;
        }
        if (existing.color() == color) {
            return existing;
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.StorageAreaRecolored(areaId, color),
                resolveMetadata(metadata, "workflow.storage.area.recolor")
        );
        mutationObserver.run();
        return storageAreaMap().area(areaId);
    }

    public StorageArea moveArea(UUID areaId, int atlasX, int atlasY) {
        return moveArea(areaId, atlasX, atlasY, DomainEventMetadata.origin("workflow.storage.area.move"));
    }

    public StorageArea moveArea(UUID areaId, int atlasX, int atlasY, DomainEventMetadata metadata) {
        if (areaId == null) {
            return null;
        }
        StorageArea existing = storageAreaMap().area(areaId);
        if (existing == null) {
            return null;
        }
        if (existing.atlasX() == atlasX && existing.atlasY() == atlasY) {
            return existing;
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.StorageAreaMoved(areaId, atlasX, atlasY),
                resolveMetadata(metadata, "workflow.storage.area.move")
        );
        mutationObserver.run();
        return storageAreaMap().area(areaId);
    }

    public boolean deleteArea(UUID areaId) {
        return deleteArea(areaId, DomainEventMetadata.origin("workflow.storage.area.delete"));
    }

    public boolean deleteArea(UUID areaId, DomainEventMetadata metadata) {
        if (areaId == null || StorageAreaMap.DEFAULT_AREA_ID.equals(areaId)) {
            return false;
        }
        if (storageAreaMap().area(areaId) == null) {
            return false;
        }
        for (ClaimedChest chest : repository.workflowProjection().claimedChestMap().chests()) {
            if (areaId.equals(chest.areaId())) {
                return false;
            }
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.StorageAreaDeleted(areaId),
                resolveMetadata(metadata, "workflow.storage.area.delete")
        );
        mutationObserver.run();
        return true;
    }

    private static DomainEventMetadata resolveMetadata(DomainEventMetadata metadata, String origin) {
        return (metadata == null ? DomainEventMetadata.origin("") : metadata).withOrigin(origin);
    }
}
