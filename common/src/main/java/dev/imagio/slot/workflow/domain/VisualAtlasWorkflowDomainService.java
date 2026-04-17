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
        return appendIslandCreated(
                new VisualAtlasIsland(
                        id,
                        normalizedLabel,
                        VisualAtlasIslandKind.PLAYER,
                        x,
                        y,
                        width,
                        height,
                        color,
                        iconIdentity
                ),
                metadata
        );
    }

    public VisualAtlasIsland createIslandWithId(
            String id,
            String label,
            int x,
            int y,
            int width,
            int height,
            int color,
            ItemIdentity iconIdentity
    ) {
        return createIslandWithId(
                id,
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

    public VisualAtlasIsland createIslandWithId(
            String id,
            String label,
            int x,
            int y,
            int width,
            int height,
            int color,
            ItemIdentity iconIdentity,
            DomainEventMetadata metadata
    ) {
        if (id == null || id.isBlank()) {
            return null;
        }
        if (visualHomeMap().island(id) != null) {
            return null;
        }
        String normalizedLabel = normalizeName(label, "Island label must not be blank");
        return appendIslandCreated(
                new VisualAtlasIsland(
                        id,
                        normalizedLabel,
                        VisualAtlasIslandKind.PLAYER,
                        x,
                        y,
                        width,
                        height,
                        color,
                        iconIdentity
                ),
                metadata
        );
    }

    private VisualAtlasIsland appendIslandCreated(VisualAtlasIsland island, DomainEventMetadata metadata) {
        repository.appendWorkflowEvent(
                new WorkflowEvent.VisualIslandCreated(island),
                (metadata == null ? DomainEventMetadata.origin("") : metadata).withOrigin("workflow.visual.island.create")
        );
        mutationObserver.run();
        return visualHomeMap().island(island.id());
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

    public VisualAtlasIsland renameIsland(String islandId, String label) {
        return renameIsland(islandId, label, DomainEventMetadata.origin("workflow.visual.island.rename"));
    }

    public VisualAtlasIsland renameIsland(String islandId, String label, DomainEventMetadata metadata) {
        VisualAtlasIsland existing = mutablePlayerIsland(islandId);
        if (existing == null) {
            return null;
        }
        String normalizedLabel = normalizeName(label, "Island label must not be blank");
        if (existing.label().equals(normalizedLabel)) {
            return existing;
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.VisualIslandRenamed(islandId, normalizedLabel),
                (metadata == null ? DomainEventMetadata.origin("") : metadata).withOrigin("workflow.visual.island.rename")
        );
        mutationObserver.run();
        return visualHomeMap().island(islandId);
    }

    public VisualAtlasIsland recolorIsland(String islandId, int color) {
        return recolorIsland(islandId, color, DomainEventMetadata.origin("workflow.visual.island.recolor"));
    }

    public VisualAtlasIsland recolorIsland(String islandId, int color, DomainEventMetadata metadata) {
        VisualAtlasIsland existing = mutablePlayerIsland(islandId);
        if (existing == null) {
            return null;
        }
        if (existing.color() == color) {
            return existing;
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.VisualIslandRecolored(islandId, color),
                (metadata == null ? DomainEventMetadata.origin("") : metadata).withOrigin("workflow.visual.island.recolor")
        );
        mutationObserver.run();
        return visualHomeMap().island(islandId);
    }

    public VisualAtlasIsland setIslandIcon(String islandId, ItemIdentity iconIdentity) {
        return setIslandIcon(islandId, iconIdentity, DomainEventMetadata.origin("workflow.visual.island.icon"));
    }

    public VisualAtlasIsland setIslandIcon(String islandId, ItemIdentity iconIdentity, DomainEventMetadata metadata) {
        VisualAtlasIsland existing = mutablePlayerIsland(islandId);
        if (existing == null) {
            return null;
        }
        if (Objects.equals(existing.iconIdentity(), iconIdentity)) {
            return existing;
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.VisualIslandIconChanged(islandId, iconIdentity),
                (metadata == null ? DomainEventMetadata.origin("") : metadata).withOrigin("workflow.visual.island.icon")
        );
        mutationObserver.run();
        return visualHomeMap().island(islandId);
    }

    public boolean deleteIsland(String islandId) {
        return deleteIsland(islandId, DomainEventMetadata.origin("workflow.visual.island.delete"));
    }

    public boolean deleteIsland(String islandId, DomainEventMetadata metadata) {
        VisualAtlasIsland existing = mutablePlayerIsland(islandId);
        if (existing == null) {
            return false;
        }
        boolean hasAssignments = visualHomeMap().assignments().values().stream()
                .anyMatch(assignment -> assignment != null && islandId.equals(assignment.islandId()));
        if (hasAssignments) {
            return false;
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.VisualIslandDeleted(islandId),
                (metadata == null ? DomainEventMetadata.origin("") : metadata).withOrigin("workflow.visual.island.delete")
        );
        mutationObserver.run();
        return true;
    }

    public boolean dismissTemplate(String templateId) {
        return dismissTemplate(templateId, DomainEventMetadata.origin("workflow.visual.template.dismiss"));
    }

    public boolean dismissTemplate(String templateId, DomainEventMetadata metadata) {
        if (templateId == null || templateId.isBlank()) {
            return false;
        }
        if (visualHomeMap().dismissedTemplateIds().contains(templateId)) {
            return false;
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.TemplateIslandDismissed(templateId),
                (metadata == null ? DomainEventMetadata.origin("") : metadata).withOrigin("workflow.visual.template.dismiss")
        );
        mutationObserver.run();
        return true;
    }

    private VisualAtlasIsland mutablePlayerIsland(String islandId) {
        if (islandId == null || islandId.isBlank()) {
            return null;
        }
        VisualAtlasIsland existing = visualHomeMap().island(islandId);
        if (existing == null || existing.kind() != VisualAtlasIslandKind.PLAYER) {
            return null;
        }
        return existing;
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
