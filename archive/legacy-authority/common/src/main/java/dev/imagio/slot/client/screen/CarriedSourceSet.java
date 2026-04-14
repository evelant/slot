package dev.imagio.slot.client.screen;

import java.util.Objects;
import java.util.Set;

public record CarriedSourceSet(
        Set<String> sourceIds,
        Set<String> menuBackedSourceIds
) {
    public CarriedSourceSet {
        sourceIds = sourceIds == null ? Set.of() : Set.copyOf(sourceIds);
        menuBackedSourceIds = menuBackedSourceIds == null ? Set.of() : Set.copyOf(menuBackedSourceIds);
    }

    public boolean contains(String sourceId) {
        return sourceIds.contains(sourceId);
    }

    public boolean isMenuBacked(String sourceId) {
        return menuBackedSourceIds.contains(sourceId);
    }

    public boolean isEmpty() {
        return sourceIds.isEmpty();
    }

    public static CarriedSourceSet of(Set<String> sourceIds, Set<String> menuBackedSourceIds) {
        return new CarriedSourceSet(sourceIds, menuBackedSourceIds);
    }
}
