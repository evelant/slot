package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
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

    public VisualAtlasIsland moveIsland(String islandId, double x, double y) {
        return moveIsland(
                islandId,
                x,
                y,
                DomainEventMetadata.origin("workflow.visual.island.move")
        );
    }

    public VisualAtlasIsland moveIsland(
            String islandId,
            double x,
            double y,
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
            double x,
            double y,
            int color,
            ItemIdentity iconIdentity
    ) {
        return createIsland(
                label,
                x,
                y,
                color,
                iconIdentity,
                DomainEventMetadata.origin("workflow.visual.island.create")
        );
    }

    public VisualAtlasIsland createIsland(
            String label,
            double x,
            double y,
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
                        color,
                        iconIdentity
                ),
                metadata
        );
    }

    public VisualAtlasIsland createIslandWithId(
            String id,
            String label,
            double x,
            double y,
            int color,
            ItemIdentity iconIdentity
    ) {
        return createIslandWithId(
                id,
                label,
                x,
                y,
                color,
                iconIdentity,
                DomainEventMetadata.origin("workflow.visual.island.create")
        );
    }

    public VisualAtlasIsland createIslandWithId(
            String id,
            String label,
            double x,
            double y,
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
            int ordinal
    ) {
        return assignHome(
                identity,
                islandId,
                ordinal,
                VisualHomeOrigin.PLAYER_PLACED,
                true,
                DomainEventMetadata.origin("workflow.visual.home.assign")
        );
    }

    public VisualHomeAssignment assignHome(
            ItemIdentity identity,
            String islandId,
            int ordinal,
            VisualHomeOrigin origin,
            boolean locked,
            DomainEventMetadata metadata
    ) {
        if (identity == null || islandId == null || islandId.isBlank()) {
            return null;
        }
        // The event carries the user-perspective insert position. The projection
        // performs the remove-from-source / insert-with-shift bookkeeping so
        // every other assignment in the affected islands gets its ordinal
        // adjusted in lockstep.
        VisualHomeAssignment requested = new VisualHomeAssignment(
                identity,
                islandId,
                ordinal,
                origin == null ? VisualHomeOrigin.PLAYER_PLACED : origin,
                locked
        );
        repository.appendWorkflowEvent(
                new WorkflowEvent.VisualHomeAssigned(requested),
                (metadata == null ? DomainEventMetadata.origin("") : metadata).withOrigin("workflow.visual.home.assign")
        );
        mutationObserver.run();
        return visualHomeMap().assignment(identity);
    }

    public Map<ItemIdentity, VisualHomeAssignment> assignHomes(
            Collection<VisualHomeAssignment> assignments,
            DomainEventMetadata metadata
    ) {
        if (assignments == null || assignments.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<ItemIdentity, VisualHomeAssignment> requestedByIdentity = new LinkedHashMap<>();
        for (VisualHomeAssignment assignment : assignments) {
            if (assignment == null
                    || assignment.identity() == null
                    || assignment.islandId() == null
                    || assignment.islandId().isBlank()) {
                continue;
            }
            VisualHomeAssignment requested = new VisualHomeAssignment(
                    assignment.identity(),
                    assignment.islandId(),
                    Math.max(0, assignment.ordinal()),
                    assignment.origin() == null ? VisualHomeOrigin.PLAYER_PLACED : assignment.origin(),
                    assignment.locked()
            );
            repository.appendWorkflowEvent(
                    new WorkflowEvent.VisualHomeAssigned(requested),
                    (metadata == null ? DomainEventMetadata.origin("") : metadata)
                            .withOrigin("workflow.visual.home.assign")
            );
            requestedByIdentity.put(requested.identity(), requested);
        }
        if (requestedByIdentity.isEmpty()) {
            return Map.of();
        }
        mutationObserver.run();
        LinkedHashMap<ItemIdentity, VisualHomeAssignment> results = new LinkedHashMap<>();
        for (ItemIdentity identity : requestedByIdentity.keySet()) {
            VisualHomeAssignment result = visualHomeMap().assignment(identity);
            if (result != null) {
                results.put(identity, result);
            }
        }
        return Map.copyOf(results);
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

    public int clearHomes(
            Collection<ItemIdentity> identities,
            DomainEventMetadata metadata
    ) {
        if (identities == null || identities.isEmpty()) {
            return 0;
        }
        int cleared = 0;
        for (ItemIdentity identity : identities) {
            if (identity == null || visualHomeMap().assignment(identity) == null) {
                continue;
            }
            repository.appendWorkflowEvent(
                    new WorkflowEvent.VisualHomeCleared(identity),
                    (metadata == null ? DomainEventMetadata.origin("") : metadata)
                            .withOrigin("workflow.visual.home.clear")
            );
            cleared++;
        }
        if (cleared > 0) {
            mutationObserver.run();
        }
        return cleared;
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

    public VisualAtlasIsland reorderIsland(String islandId, int targetIndex) {
        return reorderIsland(
                islandId,
                targetIndex,
                DomainEventMetadata.origin("workflow.visual.island.reorder")
        );
    }

    /**
     * Move {@code islandId} to ordinal {@code targetIndex} in the
     * {@link VisualHomeMap#playerIslands()} list. Target index is
     * clamped to {@code [0, size-1]} after removal so a drop past the
     * end pins to the tail. Drops onto the island's current position
     * are no-ops. Triage and template islands are never reorderable.
     */
    public VisualAtlasIsland reorderIsland(
            String islandId,
            int targetIndex,
            DomainEventMetadata metadata
    ) {
        VisualAtlasIsland existing = mutablePlayerIsland(islandId);
        if (existing == null) {
            return null;
        }
        java.util.List<VisualAtlasIsland> islands = visualHomeMap().playerIslands();
        int currentIndex = indexOf(islands, islandId);
        if (currentIndex < 0) {
            return null;
        }
        int clampedTarget = Math.max(0, Math.min(targetIndex, islands.size() - 1));
        if (clampedTarget == currentIndex) {
            return existing;
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.VisualIslandReordered(islandId, clampedTarget),
                (metadata == null ? DomainEventMetadata.origin("") : metadata).withOrigin("workflow.visual.island.reorder")
        );
        mutationObserver.run();
        return visualHomeMap().island(islandId);
    }

    /** Index of {@code islandId} in {@code islands}, or -1 if absent. */
    public int playerIslandIndex(String islandId) {
        if (islandId == null || islandId.isBlank()) {
            return -1;
        }
        return indexOf(visualHomeMap().playerIslands(), islandId);
    }

    private static int indexOf(java.util.List<VisualAtlasIsland> islands, String islandId) {
        for (int i = 0; i < islands.size(); i++) {
            VisualAtlasIsland candidate = islands.get(i);
            if (candidate != null && islandId.equals(candidate.id())) {
                return i;
            }
        }
        return -1;
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
