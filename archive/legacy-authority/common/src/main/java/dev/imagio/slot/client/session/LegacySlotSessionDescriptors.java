package dev.imagio.slot.client.session;

import dev.imagio.slot.client.source.LegacySourceDescriptors;
import dev.imagio.slot.session.InventoryHostDescriptor;
import dev.imagio.slot.session.SlotSessionCapability;
import dev.imagio.slot.session.SlotSessionDescriptor;
import dev.imagio.slot.session.SlotSessionMode;
import dev.imagio.slot.source.SourceDescriptor;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

final class LegacySlotSessionDescriptors {
    private LegacySlotSessionDescriptors() {
    }

    static SlotSessionDescriptor describe(SlotScreenSession session) {
        if (session == null) {
            return new SlotSessionDescriptor(
                    "",
                    "",
                    SlotSessionMode.UNSUPPORTED,
                    "",
                    "",
                    -1,
                    List.of(),
                    Set.of()
            );
        }

        InventoryHostDescriptor host = session.host();
        String menuClassName = host == null ? "" : host.menu().getClass().getName();
        int containerId = host == null ? -1 : host.menu().containerId;
        List<SourceDescriptor> sources = host == null
                ? List.of()
                : LegacySourceDescriptors.describeAll(host.layout().sources());

        EnumSet<SlotSessionCapability> capabilities = EnumSet.noneOf(SlotSessionCapability.class);
        if (session.hasStorageView()) {
            capabilities.add(SlotSessionCapability.HAS_STORAGE_VIEW);
        }
        if (session.recordsRecentLoot()) {
            capabilities.add(SlotSessionCapability.RECORDS_RECENT);
        }
        if (session.slotOwned()) {
            capabilities.add(SlotSessionCapability.SLOT_OWNED);
        }
        if (sources.stream().anyMatch(source -> source.group().carried())) {
            capabilities.add(SlotSessionCapability.HAS_CARRIED_SOURCES);
        }
        if (sources.stream().anyMatch(source -> source.group().external())) {
            capabilities.add(SlotSessionCapability.HAS_EXTERNAL_SOURCES);
        }

        SlotSessionMode mode = mapMode(session.kind());
        String sessionId = menuClassName.isBlank()
                ? session.screenClassName()
                : menuClassName + "#" + containerId;

        return new SlotSessionDescriptor(
                sessionId,
                fingerprint(mode, menuClassName, containerId, sources),
                mode,
                session.screenClassName(),
                menuClassName,
                containerId,
                sources,
                Set.copyOf(capabilities)
        );
    }

    private static SlotSessionMode mapMode(SlotSessionKind kind) {
        if (kind == null) {
            return SlotSessionMode.UNSUPPORTED;
        }
        return switch (kind) {
            case GENERAL -> SlotSessionMode.GENERAL;
            case PLAYER_INVENTORY -> SlotSessionMode.PLAYER_INVENTORY;
            case SLOT_WORKSPACE -> SlotSessionMode.SLOT_WORKSPACE;
            case SLOT_CARRIED -> SlotSessionMode.SLOT_CARRIED;
            case EXTERNAL_CONTAINER -> SlotSessionMode.DUAL_PANE;
            case CARRIED_CONTAINER -> SlotSessionMode.CARRIED_ONLY;
            case NON_STORAGE_CONTAINER -> SlotSessionMode.NON_STORAGE;
        };
    }

    private static String fingerprint(
            SlotSessionMode mode,
            String menuClassName,
            int containerId,
            List<SourceDescriptor> sources
    ) {
        String sourceFingerprint = sources.stream()
                .map(source -> source.id().value())
                .collect(Collectors.joining(","));
        return mode.name()
                + "|" + normalize(menuClassName)
                + "|" + containerId
                + "|" + sourceFingerprint;
    }

    private static String normalize(String value) {
        return value == null ? "" : value;
    }
}
