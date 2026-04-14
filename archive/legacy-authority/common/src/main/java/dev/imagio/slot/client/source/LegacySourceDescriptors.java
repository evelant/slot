package dev.imagio.slot.client.source;

import dev.imagio.slot.policy.SourceSelectionPreference;
import dev.imagio.slot.source.SourceCapability;
import dev.imagio.slot.source.SourceDescriptor;
import dev.imagio.slot.source.SourceGroup;
import dev.imagio.slot.source.SourceId;
import dev.imagio.slot.source.SourceKind;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class LegacySourceDescriptors {
    private LegacySourceDescriptors() {
    }

    public static List<SourceDescriptor> describeAll(Collection<? extends InventorySource> sources) {
        if (sources == null || sources.isEmpty()) {
            return List.of();
        }
        return sources.stream()
                .map(LegacySourceDescriptors::describe)
                .toList();
    }

    public static SourceDescriptor describe(InventorySource source) {
        if (source == null) {
            throw new IllegalArgumentException("source must not be null");
        }

        EnumSet<SourceCapability> capabilities = EnumSet.of(SourceCapability.BROWSE_SNAPSHOT);
        if (source.canExtract()) {
            capabilities.add(SourceCapability.EXTRACT_ONE);
            capabilities.add(SourceCapability.EXTRACT_STACK);
            capabilities.add(SourceCapability.EXTRACT_ALL_MATCHING);
            capabilities.add(SourceCapability.CURSOR_SOURCE);
        }
        if (source.canInsert()) {
            capabilities.add(SourceCapability.INSERT);
            capabilities.add(SourceCapability.CURSOR_DESTINATION);
        }
        if (source.group() == dev.imagio.slot.client.source.SourceGroup.PLAYER_MAIN
                || source.group() == dev.imagio.slot.client.source.SourceGroup.PLAYER_HOTBAR
                || source.group() == dev.imagio.slot.client.source.SourceGroup.CARRIED) {
            capabilities.add(SourceCapability.QUICK_ACCESS_SOURCE);
            capabilities.add(SourceCapability.QUICK_ACCESS_DESTINATION);
        }

        return new SourceDescriptor(
                SourceId.of(source.id()),
                source.displayName(),
                source.displayName(),
                toSourceGroup(source.group()),
                inferKind(source),
                Set.copyOf(capabilities),
                source.stableOrder(),
                source.primaryCarried(),
                "legacy"
        );
    }

    public static SourceSelectionPreference preference(
            String explicitSourceId,
            String focusedSourceId,
            String cursorCompatibleSourceId
    ) {
        return new SourceSelectionPreference(
                parseId(explicitSourceId),
                parseId(focusedSourceId),
                parseId(cursorCompatibleSourceId)
        );
    }

    private static SourceId parseId(String sourceId) {
        if (sourceId == null || sourceId.isBlank()) {
            return null;
        }
        return SourceId.of(sourceId);
    }

    private static SourceGroup toSourceGroup(dev.imagio.slot.client.source.SourceGroup legacyGroup) {
        if (legacyGroup == null) {
            return SourceGroup.VIRTUAL;
        }
        return switch (legacyGroup) {
            case PLAYER_MAIN -> SourceGroup.PLAYER_MAIN;
            case PLAYER_HOTBAR -> SourceGroup.PLAYER_HOTBAR;
            case CARRIED -> SourceGroup.CARRIED;
            case OPEN_CONTAINER -> SourceGroup.EXTERNAL;
        };
    }

    private static SourceKind inferKind(InventorySource source) {
        if (source == null) {
            return SourceKind.UNKNOWN;
        }
        String sourceId = source.id() == null ? "" : source.id();
        return switch (source.group()) {
            case PLAYER_MAIN -> SourceKind.PLAYER_INVENTORY;
            case PLAYER_HOTBAR -> SourceKind.HOTBAR;
            case OPEN_CONTAINER -> SourceKind.MENU_STORAGE;
            case CARRIED -> sourceId.contains("backpack") ? SourceKind.BACKPACK : SourceKind.CARRIED_STORAGE;
        };
    }
}
