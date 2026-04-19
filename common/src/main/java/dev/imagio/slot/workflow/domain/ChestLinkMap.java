package dev.imagio.slot.workflow.domain;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public record ChestLinkMap(
        Set<ChestLink> links
) {
    public ChestLinkMap {
        links = copyLinks(links);
    }

    public static ChestLinkMap empty() {
        return new ChestLinkMap(Set.of());
    }

    public boolean contains(String islandId, UUID storageId) {
        if (islandId == null || storageId == null) {
            return false;
        }
        String normalized = islandId.trim();
        for (ChestLink link : links) {
            if (link.islandId().equals(normalized) && link.storageId().equals(storageId)) {
                return true;
            }
        }
        return false;
    }

    public Set<String> islandsLinkedTo(UUID storageId) {
        if (storageId == null) {
            return Set.of();
        }
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (ChestLink link : links) {
            if (link.storageId().equals(storageId)) {
                result.add(link.islandId());
            }
        }
        return Set.copyOf(result);
    }

    public Set<UUID> chestsLinkedFrom(String islandId) {
        if (islandId == null || islandId.isBlank()) {
            return Set.of();
        }
        String normalized = islandId.trim();
        LinkedHashSet<UUID> result = new LinkedHashSet<>();
        for (ChestLink link : links) {
            if (link.islandId().equals(normalized)) {
                result.add(link.storageId());
            }
        }
        return Set.copyOf(result);
    }

    public static Set<ChestLink> copyLinks(Set<ChestLink> source) {
        if (source == null || source.isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<ChestLink> copied = new LinkedHashSet<>();
        for (ChestLink link : source) {
            if (link != null) {
                copied.add(link);
            }
        }
        return Set.copyOf(copied);
    }
}
