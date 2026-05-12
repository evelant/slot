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
        return repository.workflowProjection().playerWantedCounts().getOrDefault(identity, 0);
    }

    public boolean setPlayer(ItemIdentity identity, int count) {
        return setPlayer(identity, count, DomainEventMetadata.origin("workflow.wanted_count.player.set"));
    }

    public boolean setPlayer(ItemIdentity identity, int count, DomainEventMetadata metadata) {
        if (identity == null) {
            return false;
        }
        int normalized = Math.max(0, count);
        int current = getPlayer(identity);
        if (current == normalized) {
            return false;
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.PlayerWantedCountSet(identity, normalized),
                (metadata == null ? DomainEventMetadata.origin("") : metadata)
                        .withOrigin("workflow.wanted_count.player.set")
        );
        mutationObserver.run();
        return true;
    }

    public boolean clearPlayer(ItemIdentity identity) {
        return setPlayer(identity, 0, DomainEventMetadata.origin("workflow.wanted_count.player.clear"));
    }
}
