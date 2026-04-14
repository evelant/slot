package dev.imagio.slot.client.intent;

import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.projection.InventoryPane;
import dev.imagio.slot.client.screen.container.MenuSlotId;
import dev.imagio.slot.intent.ActionFamily;
import dev.imagio.slot.intent.ActionRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntentRouterTest {
    @Test
    void routesCraftingPlacementIntoCraftActionRequest() {
        AtomicReference<ActionRequest> captured = new AtomicReference<>();
        boolean requested = IntentRouter.route(
                new CraftingIntent.PlaceOne(
                        "fingerprint",
                        17,
                        MenuSlotId.of(9),
                        ItemIdentity.of("minecraft:oak_log"),
                        InventoryPane.OPEN_CONTAINER
                ),
                request -> {
                    captured.set(request);
                    return true;
                }
        );

        assertTrue(requested);
        assertEquals(ActionFamily.CRAFT, captured.get().actionFamily());
        assertEquals(17, captured.get().expectedContainerId());
        assertEquals("fingerprint", captured.get().expectedSessionFingerprint());
        assertEquals("menu_slot", captured.get().secondarySourceRef().kind());
        assertEquals("9", captured.get().secondarySourceRef().payload());
    }

    @Test
    void routesCursorCraftPlacementIntoCraftActionRequest() {
        AtomicReference<ActionRequest> captured = new AtomicReference<>();
        boolean requested = IntentRouter.route(
                new CraftingIntent.PlaceCursor(
                        "fingerprint",
                        17,
                        MenuSlotId.of(4),
                        ItemIdentity.of("minecraft:oak_log"),
                        CraftingIntent.CursorMode.ONE
                ),
                request -> {
                    captured.set(request);
                    return true;
                }
        );

        assertTrue(requested);
        assertEquals(ActionFamily.CRAFT, captured.get().actionFamily());
        assertEquals("carried_cursor", captured.get().primarySourceRef().kind());
        assertEquals("menu_slot", captured.get().secondarySourceRef().kind());
        assertEquals("4", captured.get().secondarySourceRef().payload());
        assertEquals(1, captured.get().requestedCount());
    }

    @Test
    void routesCursorDragDistributionIntoCraftActionRequest() {
        AtomicReference<ActionRequest> captured = new AtomicReference<>();
        boolean requested = IntentRouter.route(
                new CraftingIntent.DistributeCursor(
                        "fingerprint",
                        17,
                        List.of(MenuSlotId.of(1), MenuSlotId.of(4), MenuSlotId.of(7)),
                        ItemIdentity.of("minecraft:oak_log"),
                        CraftingIntent.CursorMode.STACK
                ),
                request -> {
                    captured.set(request);
                    return true;
                }
        );

        assertTrue(requested);
        assertEquals(ActionFamily.CRAFT, captured.get().actionFamily());
        assertEquals("carried_cursor", captured.get().primarySourceRef().kind());
        assertEquals("menu_slot_group", captured.get().secondarySourceRef().kind());
        assertEquals("1,4,7", captured.get().secondarySourceRef().payload());
    }

    @Test
    void routesCraftResultExtractionIntoCraftActionRequest() {
        AtomicReference<ActionRequest> captured = new AtomicReference<>();
        boolean requested = IntentRouter.route(
                new CraftingIntent.ExtractResult(
                        "fingerprint",
                        17,
                        MenuSlotId.of(10),
                        CraftingIntent.ResultAction.QUICK_MOVE,
                        1,
                        4
                ),
                request -> {
                    captured.set(request);
                    return true;
                }
        );

        assertTrue(requested);
        assertEquals(ActionFamily.CRAFT, captured.get().actionFamily());
        assertEquals("result_slot", captured.get().primarySourceRef().kind());
        assertEquals("10", captured.get().primarySourceRef().payload());
        assertEquals("result_action", captured.get().secondarySourceRef().kind());
        assertEquals("quick_move:1", captured.get().secondarySourceRef().payload());
        assertEquals(4, captured.get().requestedCount());
    }

    @Test
    void routesToolActionIntoToolActionRequest() {
        AtomicReference<ActionRequest> captured = new AtomicReference<>();
        boolean requested = IntentRouter.route(
                new ToolActionIntent(
                        "fingerprint",
                        4,
                        "crafting_grid",
                        ToolActionIntent.Action.ROTATE_GRID_CCW
                ),
                request -> {
                    captured.set(request);
                    return true;
                }
        );

        assertTrue(requested);
        assertEquals(ActionFamily.TOOL_ACTION, captured.get().actionFamily());
        assertEquals("crafting_grid", captured.get().toolRef());
        assertEquals("rotate_grid_ccw", captured.get().primarySourceRef().payload());
    }

    @Test
    void rejectsInvalidCraftingIntentBeforeRequestDispatch() {
        boolean requested = IntentRouter.route(
                new CraftingIntent.PlaceOne(
                        "fingerprint",
                        17,
                        MenuSlotId.of(-1),
                        ItemIdentity.of("minecraft:oak_log"),
                        InventoryPane.CARRIED
                ),
                request -> true
        );

        assertFalse(requested);
    }

    @Test
    void rejectsCursorCraftingIntentWithoutIdentityBeforeRequestDispatch() {
        boolean requested = IntentRouter.route(
                new CraftingIntent.PlaceCursor(
                        "fingerprint",
                        17,
                        MenuSlotId.of(9),
                        null,
                        CraftingIntent.CursorMode.STACK
                ),
                request -> true
        );

        assertFalse(requested);
    }

    @Test
    void rejectsCursorDragIntentWithoutTargetsBeforeRequestDispatch() {
        boolean requested = IntentRouter.route(
                new CraftingIntent.DistributeCursor(
                        "fingerprint",
                        17,
                        List.of(),
                        ItemIdentity.of("minecraft:oak_log"),
                        CraftingIntent.CursorMode.STACK
                ),
                request -> true
        );

        assertFalse(requested);
    }

    @Test
    void rejectsResultExtractionIntentWithoutResultSlotBeforeRequestDispatch() {
        boolean requested = IntentRouter.route(
                new CraftingIntent.ExtractResult(
                        "fingerprint",
                        17,
                        MenuSlotId.INVALID,
                        CraftingIntent.ResultAction.PICKUP,
                        0,
                        1
                ),
                request -> true
        );

        assertFalse(requested);
    }
}
