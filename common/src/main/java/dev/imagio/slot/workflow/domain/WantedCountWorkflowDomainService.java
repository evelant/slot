package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.Map;

/**
 * Player-scoped "fetch this until I carry N" intent.
 *
 * <p>Wanted counts deliberately live beside, not inside,
 * {@link DesiredCountWorkflowDomainService}. Desired counts are standing
 * carried-count policy. Wanted counts are player-authored fetch requests that
 * persist across sessions but clear automatically once the carried count is
 * satisfied. Keeping a separate service and event prevents future desired-count
 * logic from accidentally treating a wanted count as a real desired count.
 */
public final class WantedCountWorkflowDomainService {
    private final WorkflowDomainStateRepository repository;
    private final Runnable mutationObserver;

    public WantedCountWorkflowDomainService(
            WorkflowDomainStateRepository repository,
            Runnable mutationObserver
    ) {
        this.repository = repository;
        this.mutationObserver = mutationObserver == null ? () -> {} : mutationObserver;
    }

    public Map<ItemIdentity, Integer> allPlayer() {
        return repository.workflowProjection().playerWantedCounts();
    }

    public int getPlayer(ItemIdentity identity) {
        if (identity == null) {
            return 0;
        }
        return WorkflowTargetCounts.count(repository.workflowProjection().playerWantedCounts(), identity);
    }

    public boolean setPlayer(ItemIdentity identity, int count) {
        return setPlayer(identity, count, DomainEventMetadata.origin("workflow.wanted_count.player.set"));
    }

    public boolean setPlayer(ItemIdentity identity, int count, DomainEventMetadata metadata) {
        if (identity == null) {
            return false;
        }
        int normalized = Math.max(0, count);
        ItemIdentity target = WorkflowTargetCounts.key(identity);
        int current = getPlayer(target);
        if (current == normalized) {
            return false;
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.PlayerWantedCountSet(target, normalized),
                (metadata == null ? DomainEventMetadata.origin("") : metadata)
                        .withOrigin("workflow.wanted_count.player.set")
        );
        mutationObserver.run();
        return true;
    }

    public boolean clearPlayer(ItemIdentity identity) {
        return setPlayer(identity, 0, DomainEventMetadata.origin("workflow.wanted_count.player.clear"));
    }

    public Map<ItemIdentity, Integer> forKit(String kitId) {
        if (kitId == null || kitId.isBlank()) {
            return Map.of();
        }
        return repository.workflowProjection().kitWantedCounts().getOrDefault(kitId, Map.of());
    }

    public int getForKit(String kitId, ItemIdentity identity) {
        if (kitId == null || kitId.isBlank() || identity == null) {
            return 0;
        }
        return WorkflowTargetCounts.count(forKit(kitId), identity);
    }

    public boolean setForKit(String kitId, ItemIdentity identity, int count) {
        return setForKit(kitId, identity, count, DomainEventMetadata.origin("workflow.wanted_count.kit.set"));
    }

    public boolean setForKit(String kitId, ItemIdentity identity, int count, DomainEventMetadata metadata) {
        if (kitId == null || kitId.isBlank() || identity == null) {
            return false;
        }
        int normalized = Math.max(0, count);
        ItemIdentity target = WorkflowTargetCounts.key(identity);
        int current = getForKit(kitId, target);
        if (current == normalized) {
            return false;
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.KitWantedCountSet(kitId, target, normalized),
                (metadata == null ? DomainEventMetadata.origin("") : metadata)
                        .withOrigin("workflow.wanted_count.kit.set")
        );
        mutationObserver.run();
        return true;
    }

    public boolean clearForKit(String kitId, ItemIdentity identity) {
        return setForKit(kitId, identity, 0, DomainEventMetadata.origin("workflow.wanted_count.kit.clear"));
    }

    public boolean clearKitScope(String kitId) {
        if (kitId == null || kitId.isBlank()) {
            return false;
        }
        boolean changed = false;
        for (ItemIdentity identity : forKit(kitId).keySet()) {
            changed |= setForKit(
                    kitId,
                    identity,
                    0,
                    DomainEventMetadata.origin("workflow.wanted_count.kit.clear_scope"));
        }
        return changed;
    }

    public String activeScope(KitMap kitMap) {
        if (kitMap == null) {
            return null;
        }
        KitActivation activation = kitMap.activation();
        if (activation == null || !activation.isActive()) {
            return null;
        }
        return activation.kitId();
    }
}
