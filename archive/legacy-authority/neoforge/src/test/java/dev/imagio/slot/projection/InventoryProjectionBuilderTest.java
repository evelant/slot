package dev.imagio.slot.projection;

import dev.imagio.slot.client.category.SlotCategory;
import dev.imagio.slot.client.collection.CollectionStore;
import dev.imagio.slot.client.model.ItemEntry;
import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.projection.InventoryPane;
import dev.imagio.slot.projection.InventoryViewData;
import org.junit.jupiter.api.Test;
import net.minecraft.world.item.ItemStack;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class InventoryProjectionBuilderTest {
    @Test
    void workspaceProjectionBuildsPaneLocalRowsAndCounts() {
        CollectionStore collectionStore = new CollectionStore();
        InventoryViewData.Section recent = InventoryViewData.Section.recent("Recent", 0);
        InventoryViewData.Section misc = InventoryViewData.Section.category(SlotCategory.MISC, 1);

        InventoryViewData.EntryView stone = entryView(
                ItemIdentity.of("minecraft:stone"),
                "Stone",
                SlotCategory.MISC,
                Map.of("open", 4, "carried", 2),
                Set.of()
        );
        InventoryViewData.EntryView dirt = entryView(
                ItemIdentity.of("minecraft:dirt"),
                "Dirt",
                SlotCategory.MISC,
                Map.of("carried", 3),
                Set.of()
        );

        InventoryProjection projection = InventoryProjectionBuilder.buildWorkspace(
                new InventoryProjectionBuilder.WorkspaceInput(
                        List.of(recent, misc),
                        List.of(stone, dirt),
                        List.of(stone),
                        "",
                        collectionStore,
                        Comparator.comparing(InventoryViewData.EntryView::displayName),
                        (entry, pane) -> entry.itemEntry().perSourceCounts().getOrDefault(
                                pane == InventoryPane.OPEN_CONTAINER ? "open" : "carried",
                                0
                        ),
                        (section, pane) -> {
                            if (section.isRecent()) {
                                return new InventoryProjectionBuilder.PaneSectionOptions(
                                        pane == InventoryPane.CARRIED,
                                        pane == InventoryPane.CARRIED,
                                        true,
                                        false,
                                        false
                                );
                            }
                            return new InventoryProjectionBuilder.PaneSectionOptions(
                                    true,
                                    false,
                                    true,
                                    false,
                                    false
                            );
                        }
                )
        );

        InventoryProjection.PaneProjection openPane = projection.pane(InventoryPane.OPEN_CONTAINER);
        InventoryProjection.PaneProjection carriedPane = projection.pane(InventoryPane.CARRIED);

        assertEquals(2, projection.combinedSectionCounts().get(misc.id()));
        assertEquals(1, projection.combinedSectionCounts().get(recent.id()));

        assertEquals(1, openPane.sectionCounts().get(misc.id()));
        assertEquals(1, openPane.visibleEntryCount());
        assertEquals(2, openPane.rows().size());
        assertEquals(List.of(stone), openPane.visibleEntriesBySection().get(misc.id()));

        assertEquals(1, carriedPane.sectionCounts().get(recent.id()));
        assertEquals(2, carriedPane.sectionCounts().get(misc.id()));
        assertEquals(3, carriedPane.visibleEntryCount());
        assertEquals(5, carriedPane.rows().size());
        assertEquals(List.of(stone), carriedPane.visibleEntriesBySection().get(recent.id()));
        assertEquals(List.of(dirt, stone), carriedPane.visibleEntriesBySection().get(misc.id()));
    }

    @Test
    void workspaceProjectionProducesStableRowIdsAcrossRebuilds() {
        CollectionStore collectionStore = new CollectionStore();
        InventoryViewData.Section recent = InventoryViewData.Section.recent("Recent", 0);
        InventoryViewData.Section misc = InventoryViewData.Section.category(SlotCategory.MISC, 1);

        InventoryViewData.EntryView stone = entryView(
                ItemIdentity.exact("minecraft:stone", "{foo:1b}"),
                "Stone",
                SlotCategory.MISC,
                Map.of("open", 4, "carried", 2),
                Set.of()
        );
        InventoryViewData.EntryView dirt = entryView(
                ItemIdentity.of("minecraft:dirt"),
                "Dirt",
                SlotCategory.MISC,
                Map.of("carried", 3),
                Set.of()
        );

        InventoryProjectionBuilder.WorkspaceInput input = new InventoryProjectionBuilder.WorkspaceInput(
                List.of(recent, misc),
                List.of(stone, dirt),
                List.of(stone),
                "",
                collectionStore,
                Comparator.comparing(InventoryViewData.EntryView::displayName),
                (entry, pane) -> entry.itemEntry().perSourceCounts().getOrDefault(
                        pane == InventoryPane.OPEN_CONTAINER ? "open" : "carried",
                        0
                ),
                (section, pane) -> new InventoryProjectionBuilder.PaneSectionOptions(
                        !section.isRecent() || pane == InventoryPane.CARRIED,
                        pane == InventoryPane.CARRIED && section.isRecent(),
                        true,
                        false,
                        false
                )
        );

        List<String> firstRowIds = InventoryProjectionBuilder.buildWorkspace(input)
                .pane(InventoryPane.CARRIED)
                .rows()
                .stream()
                .map(InventoryProjection.RowProjection::rowId)
                .toList();
        List<String> secondRowIds = InventoryProjectionBuilder.buildWorkspace(input)
                .pane(InventoryPane.CARRIED)
                .rows()
                .stream()
                .map(InventoryProjection.RowProjection::rowId)
                .toList();

        assertFalse(firstRowIds.isEmpty());
        assertFalse(firstRowIds.stream().anyMatch(String::isBlank));
        assertIterableEquals(firstRowIds, secondRowIds);
    }

    private static InventoryViewData.EntryView entryView(
            ItemIdentity identity,
            String displayName,
            SlotCategory category,
            Map<String, Integer> perSourceCounts,
            Set<String> collectionIds
    ) {
        ItemEntry itemEntry = new ItemEntry(
                identity,
                perSourceCounts.values().stream().mapToInt(Integer::intValue).sum(),
                perSourceCounts,
                List.of(),
                category,
                false,
                collectionIds
        );
        return new InventoryViewData.EntryView(
                itemEntry,
                new ItemStack(),
                displayName,
                (displayName + " " + identity.itemId()).toLowerCase(Locale.ROOT)
        );
    }
}
