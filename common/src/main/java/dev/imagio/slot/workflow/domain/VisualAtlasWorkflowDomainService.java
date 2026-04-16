package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class VisualAtlasWorkflowDomainService {
    private final WorkflowDomainStateRepository repository;
    private final Runnable mutationObserver;

    public VisualAtlasWorkflowDomainService(WorkflowDomainStateRepository repository) {
        this(repository, () -> {
        });
    }

    public VisualAtlasWorkflowDomainService(
            WorkflowDomainStateRepository repository,
            Runnable mutationObserver
    ) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.mutationObserver = mutationObserver == null ? () -> {
        } : mutationObserver;
    }

    public VisualHomeMap visualHomeMap() {
        return repository.workflowProjection().visualHomeMap();
    }

    public VisualHomeAssignment assignment(ItemIdentity identity) {
        return visualHomeMap().assignment(identity);
    }

    public VisualAtlasIsland moveIsland(String islandId, int x, int y) {
        return moveIsland(
                islandId,
                x,
                y,
                DomainEventMetadata.origin("workflow.visual.island.move")
        );
    }

    public VisualAtlasIsland moveIsland(
            String islandId,
            int x,
            int y,
            DomainEventMetadata metadata
    ) {
        if (islandId == null || islandId.isBlank()) {
            return null;
        }
        VisualAtlasIsland existing = visualHomeMap().island(islandId);
        if (existing == null) {
            return null;
        }
        if (existing.x() == x && existing.y() == y) {
            return existing;
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.VisualIslandMoved(islandId, x, y),
                (metadata == null ? DomainEventMetadata.origin("") : metadata).withOrigin("workflow.visual.island.move")
        );
        mutationObserver.run();
        return visualHomeMap().island(islandId);
    }

    public VisualAtlasIsland createIsland(
            String label,
            int x,
            int y,
            int width,
            int height,
            int color,
            ItemIdentity iconIdentity
    ) {
        return createIsland(
                label,
                x,
                y,
                width,
                height,
                color,
                iconIdentity,
                DomainEventMetadata.origin("workflow.visual.island.create")
        );
    }

    public VisualAtlasIsland createIsland(
            String label,
            int x,
            int y,
            int width,
            int height,
            int color,
            ItemIdentity iconIdentity,
            DomainEventMetadata metadata
    ) {
        String normalizedLabel = normalizeName(label, "Island label must not be blank");
        String id = uniqueSlug(normalizedLabel, visualHomeMap().playerIslands().stream()
                .map(VisualAtlasIsland::id)
                .collect(Collectors.toSet()), "island");
        VisualAtlasIsland island = new VisualAtlasIsland(
                id,
                normalizedLabel,
                VisualAtlasIslandKind.PLAYER,
                x,
                y,
                width,
                height,
                color,
                iconIdentity
        );
        repository.appendWorkflowEvent(
                new WorkflowEvent.VisualIslandCreated(island),
                (metadata == null ? DomainEventMetadata.origin("") : metadata).withOrigin("workflow.visual.island.create")
        );
        mutationObserver.run();
        return visualHomeMap().island(id);
    }

    public VisualHomeAssignment assignHome(
            ItemIdentity identity,
            String islandId,
            int localX,
            int localY
    ) {
        return assignHome(
                identity,
                islandId,
                localX,
                localY,
                VisualHomeOrigin.PLAYER_PLACED,
                true,
                DomainEventMetadata.origin("workflow.visual.home.assign")
        );
    }

    public VisualHomeAssignment assignHome(
            ItemIdentity identity,
            String islandId,
            int localX,
            int localY,
            VisualHomeOrigin origin,
            boolean locked,
            DomainEventMetadata metadata
    ) {
        if (identity == null || islandId == null || islandId.isBlank()) {
            return null;
        }
        VisualHomeAssignment next = new VisualHomeAssignment(
                identity,
                islandId,
                localX,
                localY,
                origin == null ? VisualHomeOrigin.PLAYER_PLACED : origin,
                locked
        );
        if (next.equals(visualHomeMap().assignment(identity))) {
            return next;
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.VisualHomeAssigned(next),
                (metadata == null ? DomainEventMetadata.origin("") : metadata).withOrigin("workflow.visual.home.assign")
        );
        mutationObserver.run();
        return visualHomeMap().assignment(identity);
    }

    public boolean clearHome(ItemIdentity identity) {
        return clearHome(identity, DomainEventMetadata.origin("workflow.visual.home.clear"));
    }

    public boolean clearHome(ItemIdentity identity, DomainEventMetadata metadata) {
        if (identity == null || visualHomeMap().assignment(identity) == null) {
            return false;
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.VisualHomeCleared(identity),
                (metadata == null ? DomainEventMetadata.origin("") : metadata).withOrigin("workflow.visual.home.clear")
        );
        mutationObserver.run();
        return true;
    }

    private static String normalizeName(String value, String message) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return normalized;
    }

    private static String uniqueSlug(String value, Set<String> existingIds, String fallbackPrefix) {
        String base = slugify(value);
        if (base.isBlank()) {
            base = fallbackPrefix == null || fallbackPrefix.isBlank() ? "value" : fallbackPrefix;
        }
        if (existingIds == null || !existingIds.contains(base)) {
            return base;
        }
        for (int suffix = 2; suffix < Integer.MAX_VALUE; suffix++) {
            String candidate = base + "-" + suffix;
            if (!existingIds.contains(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Unable to create unique slug for " + value);
    }

    private static String slugify(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        boolean lastDash = false;
        for (char raw : value.toLowerCase(Locale.ROOT).toCharArray()) {
            char current = Character.isLetterOrDigit(raw) ? raw : '-';
            if (current == '-') {
                if (!lastDash && builder.length() > 0) {
                    builder.append('-');
                }
                lastDash = true;
            } else {
                builder.append(current);
                lastDash = false;
            }
        }
        int length = builder.length();
        while (length > 0 && builder.charAt(length - 1) == '-') {
            builder.deleteCharAt(length - 1);
            length--;
        }
        return builder.toString();
    }
}
