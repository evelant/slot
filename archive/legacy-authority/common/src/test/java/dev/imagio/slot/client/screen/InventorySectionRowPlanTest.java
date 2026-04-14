package dev.imagio.slot.client.screen;

import dev.imagio.slot.projection.InventoryViewData;
import dev.imagio.slot.projection.InventorySectionRowPlan;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventorySectionRowPlanTest {
    @Test
    void dropsEmptySectionsUnlessRetained() {
        InventoryViewData.Section section = InventoryViewData.Section.recent("Recent", 0);

        assertFalse(InventorySectionRowPlan.plan(
                section,
                List.of(),
                new InventorySectionRowPlan.Options(false, true, false, false)
        ).included());

        InventorySectionRowPlan.Plan retained = InventorySectionRowPlan.plan(
                section,
                List.of(),
                new InventorySectionRowPlan.Options(true, true, false, false)
        );

        assertTrue(retained.included());
        assertEquals(1, retained.rows().size());
        assertEquals(InventorySectionRowPlan.Kind.SECTION, retained.rows().get(0).kind());
        assertEquals(0, retained.visibleItemCount());
    }

    @Test
    void collapsedSectionsKeepVisibleItemCountButDoNotCreateItemRows() {
        InventoryViewData.Section section = InventoryViewData.Section.collection("tools", "Tools", 0);
        List<InventoryViewData.EntryView> entries = Collections.nCopies(2, null);

        InventorySectionRowPlan.Plan plan = InventorySectionRowPlan.plan(
                section,
                entries,
                new InventorySectionRowPlan.Options(false, false, true, true)
        );

        assertTrue(plan.included());
        assertEquals(3, plan.rows().size());
        assertEquals(2, plan.sectionEntryCount());
        assertEquals(2, plan.visibleItemCount());
        assertEquals(InventorySectionRowPlan.Kind.SECTION, plan.rows().get(0).kind());
        assertEquals(InventorySectionRowPlan.Kind.LOADOUT, plan.rows().get(1).kind());
        assertEquals(InventorySectionRowPlan.Kind.LOADOUT_PREVIEW, plan.rows().get(2).kind());
    }

    @Test
    void expandedSectionsCreateOneItemRowPerEntry() {
        InventoryViewData.Section section = InventoryViewData.Section.collection("tools", "Tools", 0);
        List<InventoryViewData.EntryView> entries = Collections.nCopies(2, null);

        InventorySectionRowPlan.Plan plan = InventorySectionRowPlan.plan(
                section,
                entries,
                new InventorySectionRowPlan.Options(false, true, false, false)
        );

        assertTrue(plan.included());
        assertEquals(3, plan.rows().size());
        assertEquals(InventorySectionRowPlan.Kind.ITEM, plan.rows().get(1).kind());
        assertEquals(InventorySectionRowPlan.Kind.ITEM, plan.rows().get(2).kind());
        assertEquals(2, plan.visibleItemCount());
    }
}
