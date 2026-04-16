package dev.imagio.slot.inventory.browse;

import dev.imagio.slot.inventory.action.InventoryCommandAvailability;
import dev.imagio.slot.inventory.action.InventoryCommandId;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.query.ProjectedInventoryRow;
import dev.imagio.slot.workflow.domain.QuickAccessLoadoutDefinition;

import java.util.Map;

public sealed interface InventoryBrowseEntry permits
        InventoryBrowseEntry.ItemEntry,
        InventoryBrowseEntry.PlaceholderEntry,
        InventoryBrowseEntry.LoadoutEntry {

    InventoryBrowseSubjectRef subjectRef();

    Map<InventoryCommandId, InventoryCommandAvailability> commands();

    String diagnostics();

    record ItemEntry(
            InventoryBrowseSubjectRef.ItemRowRef subjectRef,
            ProjectedInventoryRow row,
            InventoryBrowseAnnotations annotations,
            boolean selected,
            Map<InventoryCommandId, InventoryCommandAvailability> commands,
            String diagnostics
    ) implements InventoryBrowseEntry {
        public ItemEntry {
            annotations = annotations == null ? InventoryBrowseAnnotations.empty() : annotations;
            commands = commands == null ? Map.of() : Map.copyOf(commands);
            diagnostics = diagnostics == null ? "" : diagnostics;
        }
    }

    record PlaceholderEntry(
            InventoryBrowseSubjectRef.PlaceholderRef subjectRef,
            String collectionId,
            ItemIdentity identity,
            InventoryBrowseAnnotations annotations,
            boolean selected,
            Map<InventoryCommandId, InventoryCommandAvailability> commands,
            String diagnostics
    ) implements InventoryBrowseEntry {
        public PlaceholderEntry {
            collectionId = collectionId == null ? "" : collectionId;
            annotations = annotations == null ? InventoryBrowseAnnotations.empty() : annotations;
            commands = commands == null ? Map.of() : Map.copyOf(commands);
            diagnostics = diagnostics == null ? "" : diagnostics;
        }
    }

    record LoadoutEntry(
            InventoryBrowseSubjectRef.LoadoutRef subjectRef,
            String collectionId,
            QuickAccessLoadoutDefinition loadout,
            boolean selected,
            Map<InventoryCommandId, InventoryCommandAvailability> commands,
            String diagnostics
    ) implements InventoryBrowseEntry {
        public LoadoutEntry {
            collectionId = collectionId == null ? "" : collectionId;
            commands = commands == null ? Map.of() : Map.copyOf(commands);
            diagnostics = diagnostics == null ? "" : diagnostics;
        }
    }
}
