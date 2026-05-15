package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.Map;

/**
 * "I want N of this item carried at all times" intent, with two scopes:
 *
 * <ul>
 *   <li><b>Global</b> ({@code setPlayer}/{@code adjustPlayer}): the
 *   player's standing order, applies whenever no kit overrides it.</li>
 *   <li><b>Kit-scoped</b> ({@code setForKit}/{@code adjustForKit}): per
 *   kit; takes precedence over the global value while that kit is
 *   active.</li>
 * </ul>
 *
 * <p>Resolution rule (applied at view-model build, not in this service):
 * if a kit is active and has a non-zero entry for the identity, use it;
 * else fall back to the global value. Use {@link #activeScope(KitMap)}
 * if you need the same scope-pick logic the UI uses for writes.
 *
 * <p>Stored event-sourced via {@link WorkflowEvent.PlayerDesiredCountSet}
 * and {@link WorkflowEvent.KitDesiredCountSet}, so persistence and the
 * (eventual) undo path come for free.
 *
 * <p>Distinct from the legacy {@link CollectionWorkflowDomainService}'s
 * collection-scoped desired counts; that store is dead from the kits
 * replacement of collections and a candidate for separate cleanup.
 */
public final class DesiredCountWorkflowDomainService {
    private final WorkflowDomainStateRepository repository;
    private final Runnable mutationObserver;

    public DesiredCountWorkflowDomainService(
            WorkflowDomainStateRepository repository,
            Runnable mutationObserver
    ) {
        this.repository = repository;
        this.mutationObserver = mutationObserver == null ? () -> {} : mutationObserver;
    }

    // --- Player-global scope ---

    public Map<ItemIdentity, Integer> allPlayer() {
        return repository.workflowProjection().playerDesiredCounts();
    }

    public int getPlayer(ItemIdentity identity) {
        if (identity == null) {
            return 0;
        }
        return repository.workflowProjection().playerDesiredCounts().getOrDefault(identity, 0);
    }

    public boolean setPlayer(ItemIdentity identity, int count) {
        return setPlayer(identity, count, DomainEventMetadata.origin("workflow.desired_count.player.set"));
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
                new WorkflowEvent.PlayerDesiredCountSet(identity, normalized),
                (metadata == null ? DomainEventMetadata.origin("") : metadata)
                        .withOrigin("workflow.desired_count.player.set")
        );
        mutationObserver.run();
        return true;
    }

    public boolean adjustPlayer(ItemIdentity identity, int delta) {
        if (identity == null || delta == 0) {
            return false;
        }
        int current = getPlayer(identity);
        int next = Math.max(0, current + delta);
        return setPlayer(identity, next);
    }

    // --- Kit-scoped ---

    public Map<ItemIdentity, Integer> forKit(String kitId) {
        if (kitId == null || kitId.isBlank()) {
            return Map.of();
        }
        return repository.workflowProjection().kitDesiredCounts().getOrDefault(kitId, Map.of());
    }

    public int getForKit(String kitId, ItemIdentity identity) {
        if (kitId == null || kitId.isBlank() || identity == null) {
            return 0;
        }
        return forKit(kitId).getOrDefault(identity, 0);
    }

    public boolean setForKit(String kitId, ItemIdentity identity, int count) {
        return setForKit(kitId, identity, count, DomainEventMetadata.origin("workflow.desired_count.kit.set"));
    }

    public boolean setForKit(String kitId, ItemIdentity identity, int count, DomainEventMetadata metadata) {
        if (kitId == null || kitId.isBlank() || identity == null) {
            return false;
        }
        int normalized = Math.max(0, count);
        int current = getForKit(kitId, identity);
        if (current == normalized) {
            return false;
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.KitDesiredCountSet(kitId, identity, normalized),
                (metadata == null ? DomainEventMetadata.origin("") : metadata)
                        .withOrigin("workflow.desired_count.kit.set")
        );
        mutationObserver.run();
        return true;
    }

    public boolean adjustForKit(String kitId, ItemIdentity identity, int delta) {
        if (kitId == null || kitId.isBlank() || identity == null || delta == 0) {
            return false;
        }
        int current = getForKit(kitId, identity);
        int next = Math.max(0, current + delta);
        return setForKit(kitId, identity, next);
    }

    // --- Resolution helpers ---

    /**
     * Active write scope for the player. Returns the active kit's id when
     * a kit is active (writes go to that kit's scope), or null when no
     * kit is active (writes go to the player-global scope). Callers that
     * edit an already-visible value must still account for global fallback
     * themselves; {@link #resolved(KitMap, ItemIdentity)} remains the
     * read-side rule.
     */
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

    /**
     * Resolve the effective desired count for {@code identity}: kit-scoped
     * value if a kit is active and has a non-zero entry, else player-global.
     * Returns 0 when neither scope has a value.
     */
    public int resolved(KitMap kitMap, ItemIdentity identity) {
        if (identity == null) {
            return 0;
        }
        String kitId = activeScope(kitMap);
        if (kitId != null) {
            int kitVal = getForKit(kitId, identity);
            if (kitVal > 0) {
                return kitVal;
            }
        }
        return getPlayer(identity);
    }
}
