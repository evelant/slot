package dev.imagio.slot.forge.compat.ae2;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

final class Ae2NetworkIdentitySupport {
    private Ae2NetworkIdentitySupport() {
    }

    static List<String> mediaIdsForNetworkIdentity(
            Set<String> activeMediaIds,
            Set<String> unreadableMediaIds,
            boolean hasAggregateItemContents
    ) {
        if (activeMediaIds != null && !activeMediaIds.isEmpty()) {
            return sortedMediaIds(activeMediaIds);
        }
        if (!hasAggregateItemContents || unreadableMediaIds == null || unreadableMediaIds.isEmpty()) {
            return List.of();
        }
        return sortedMediaIds(unreadableMediaIds);
    }

    private static List<String> sortedMediaIds(Set<String> mediaIds) {
        if (mediaIds == null || mediaIds.isEmpty()) {
            return List.of();
        }
        ArrayList<String> sorted = new ArrayList<>();
        for (String mediaId : mediaIds) {
            if (mediaId != null && !mediaId.isBlank()) {
                sorted.add(mediaId);
            }
        }
        sorted.sort(String::compareTo);
        return sorted.isEmpty() ? List.of() : List.copyOf(sorted);
    }
}
