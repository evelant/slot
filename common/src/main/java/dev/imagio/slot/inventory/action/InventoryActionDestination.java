package dev.imagio.slot.inventory.action;

import dev.imagio.slot.inventory.core.InventoryPaneMembership;

import java.util.Locale;

public sealed interface InventoryActionDestination permits
        InventoryActionDestination.PaneDestination,
        InventoryActionDestination.SourceDestination {

    String stableKey();

    record PaneDestination(InventoryPaneMembership paneMembership) implements InventoryActionDestination {
        public PaneDestination {
            if (paneMembership != InventoryPaneMembership.CARRIED && paneMembership != InventoryPaneMembership.EXTERNAL) {
                throw new IllegalArgumentException("pane destination must be carried or external");
            }
        }

        @Override
        public String stableKey() {
            return "pane:" + paneMembership.name().toLowerCase(Locale.ROOT);
        }
    }

    record SourceDestination(String sourceId) implements InventoryActionDestination {
        public SourceDestination {
            if (sourceId == null || sourceId.isBlank()) {
                throw new IllegalArgumentException("destination source id must not be blank");
            }
        }

        @Override
        public String stableKey() {
            return "source:" + sourceId;
        }
    }
}
