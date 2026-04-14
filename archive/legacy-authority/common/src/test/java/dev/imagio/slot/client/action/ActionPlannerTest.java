package dev.imagio.slot.client.action;

import dev.imagio.slot.client.category.SlotCategory;
import dev.imagio.slot.client.model.ItemEntry;
import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.client.model.SlotRef;
import dev.imagio.slot.client.source.BasicInventorySource;
import dev.imagio.slot.client.source.InventorySource;
import dev.imagio.slot.client.source.SourceGroup;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ActionPlannerTest {
    private final ActionPlanner planner = new ActionPlanner();

    @Test
    void prefersFocusedSourceWhenPresent() {
        ItemEntry entry = new ItemEntry(
                ItemIdentity.of("minecraft:cobblestone"),
                48,
                Map.of("main", 16, "bag", 32),
                List.of(new SlotRef("main", 9), new SlotRef("bag", 0)),
                SlotCategory.BUILDING,
                false,
                Set.of()
        );

        ActionPlannerContext context = ActionPlannerContext.of(
                List.of(
                        new BasicInventorySource("main", "Main", SourceGroup.PLAYER_MAIN, 0, false, true, true),
                        new BasicInventorySource("bag", "Bag", SourceGroup.CARRIED, 0, true, true, true)
                ),
                null,
                "bag",
                null
        );

        Optional<InventorySource> selected = planner.chooseExistingSource(entry, context);
        assertEquals("bag", selected.orElseThrow().id());
    }

    @Test
    void fallsBackToPlayerMainBeforeHotbarAndCarried() {
        ItemEntry entry = new ItemEntry(
                ItemIdentity.of("minecraft:stick"),
                7,
                Map.of("main", 2, "hotbar", 1, "bag-secondary", 4),
                List.of(new SlotRef("main", 10), new SlotRef("hotbar", 0), new SlotRef("bag-secondary", 2)),
                SlotCategory.MATERIALS,
                false,
                Set.of()
        );

        ActionPlannerContext context = ActionPlannerContext.of(
                List.of(
                        new BasicInventorySource("bag-secondary", "Bag Secondary", SourceGroup.CARRIED, 1, false, true, true),
                        new BasicInventorySource("hotbar", "Hotbar", SourceGroup.PLAYER_HOTBAR, 0, false, true, true),
                        new BasicInventorySource("main", "Main", SourceGroup.PLAYER_MAIN, 0, false, true, true)
                ),
                null,
                null,
                null
        );

        Optional<InventorySource> selected = planner.chooseExistingSource(entry, context);
        assertEquals("main", selected.orElseThrow().id());
    }

    @Test
    void prefersHotbarBeforeCarriedWhenMainIsAbsent() {
        ItemEntry entry = new ItemEntry(
                ItemIdentity.of("minecraft:torch"),
                40,
                Map.of("hotbar", 8, "bag-primary", 32),
                List.of(new SlotRef("hotbar", 4), new SlotRef("bag-primary", 1)),
                SlotCategory.TOOLS_AND_UTILITY,
                false,
                Set.of()
        );

        ActionPlannerContext context = ActionPlannerContext.of(
                List.of(
                        new BasicInventorySource("bag-primary", "Bag Primary", SourceGroup.CARRIED, 0, true, true, true),
                        new BasicInventorySource("hotbar", "Hotbar", SourceGroup.PLAYER_HOTBAR, 0, false, true, true)
                ),
                null,
                null,
                null
        );

        Optional<InventorySource> selected = planner.chooseExistingSource(entry, context);
        assertEquals("hotbar", selected.orElseThrow().id());
    }
}
