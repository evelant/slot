package dev.imagio.slot.network;

import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.projection.InventoryPane;
import dev.imagio.slot.intent.ActionFamily;
import dev.imagio.slot.intent.ActionRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class CraftingGridActionRequestsTest {
    @Test
    void placeOneRequestResolvesBackToLegacyCraftingSpec() {
        ItemIdentity identity = ItemIdentity.exact("minecraft:oak_log", "components");
        ActionRequest request = CraftingGridActionRequests.placeOne(
                17,
                "fingerprint",
                9,
                identity,
                InventoryPane.OPEN_CONTAINER
        );

        CraftingGridActionRequests.Resolution resolution = CraftingGridActionRequests.resolve(request);

        assertEquals(ActionFamily.CRAFT, request.actionFamily());
        assertEquals("fingerprint", request.expectedSessionFingerprint());
        assertEquals(CraftingGridActionRequests.KIND_PANE_IDENTITY, request.primarySourceRef().kind());
        assertEquals("menu_slot", request.secondarySourceRef().kind());
        assertNotNull(resolution);
        assertEquals(CraftingGridActionRequests.Route.PANE_IDENTITY_PLACE, resolution.route());
        assertEquals(9, resolution.spec().targetMenuSlot());
        assertEquals(java.util.List.of(9), resolution.spec().targetMenuSlots());
        assertEquals(InventoryPane.OPEN_CONTAINER, resolution.spec().sourcePane());
        assertNull(resolution.spec().cursorMode());
        assertEquals(identity.itemId(), resolution.spec().itemId());
    }

    @Test
    void carriedPlacementPreservesCarriedPane() {
        ActionRequest request = CraftingGridActionRequests.placeOne(
                4,
                "fp",
                12,
                ItemIdentity.of("minecraft:torch"),
                InventoryPane.CARRIED
        );

        CraftingGridActionRequests.Resolution resolution = CraftingGridActionRequests.resolve(request);

        assertNotNull(resolution);
        assertEquals(CraftingGridActionRequests.Route.PANE_IDENTITY_PLACE, resolution.route());
        assertEquals(InventoryPane.CARRIED, resolution.spec().sourcePane());
        assertEquals(12, resolution.spec().targetMenuSlot());
        assertEquals(java.util.List.of(12), resolution.spec().targetMenuSlots());
    }

    @Test
    void cursorPlacementResolvesIntoCursorCraftRoute() {
        ItemIdentity identity = ItemIdentity.exact("minecraft:oak_log", "components");
        ActionRequest request = CraftingGridActionRequests.placeCursor(
                21,
                "fingerprint",
                7,
                identity,
                CraftingGridActionRequests.CursorMode.ONE
        );

        CraftingGridActionRequests.Resolution resolution = CraftingGridActionRequests.resolve(request);

        assertNotNull(resolution);
        assertEquals(ActionFamily.CRAFT, request.actionFamily());
        assertEquals(CraftingGridActionRequests.KIND_CARRIED_CURSOR, request.primarySourceRef().kind());
        assertEquals(CraftingGridActionRequests.Route.CURSOR_PLACE, resolution.route());
        assertNull(resolution.spec().sourcePane());
        assertEquals(CraftingGridActionRequests.CursorMode.ONE, resolution.spec().cursorMode());
        assertEquals(7, resolution.spec().targetMenuSlot());
        assertEquals(java.util.List.of(7), resolution.spec().targetMenuSlots());
        assertEquals(identity, resolution.spec().identity());
    }

    @Test
    void dragDistributionPreservesVisitedInputSlots() {
        ItemIdentity identity = ItemIdentity.exact("minecraft:oak_log", "components");
        ActionRequest request = CraftingGridActionRequests.distributeCursor(
                21,
                "fingerprint",
                java.util.List.of(3, 5, 7, 5),
                identity,
                CraftingGridActionRequests.CursorMode.STACK
        );

        CraftingGridActionRequests.Resolution resolution = CraftingGridActionRequests.resolve(request);

        assertNotNull(resolution);
        assertEquals(ActionFamily.CRAFT, request.actionFamily());
        assertEquals(CraftingGridActionRequests.KIND_CARRIED_CURSOR, request.primarySourceRef().kind());
        assertEquals(CraftingGridActionRequests.KIND_MENU_SLOT_GROUP, request.secondarySourceRef().kind());
        assertEquals(CraftingGridActionRequests.Route.CURSOR_DISTRIBUTE, resolution.route());
        assertEquals(java.util.List.of(3, 5, 7), resolution.spec().targetMenuSlots());
        assertEquals(identity, resolution.spec().identity());
        assertEquals(CraftingGridActionRequests.CursorMode.STACK, resolution.spec().cursorMode());
    }

    @Test
    void resultExtractionResolvesIntoCraftResultRoute() {
        ActionRequest request = CraftingGridActionRequests.extractResult(
                21,
                "fingerprint",
                11,
                CraftingGridActionRequests.ResultAction.QUICK_MOVE,
                1,
                3
        );

        CraftingGridActionRequests.Resolution resolution = CraftingGridActionRequests.resolve(request);

        assertNotNull(resolution);
        assertEquals(ActionFamily.CRAFT, request.actionFamily());
        assertEquals(CraftingGridActionRequests.KIND_RESULT_SLOT, request.primarySourceRef().kind());
        assertEquals(CraftingGridActionRequests.KIND_RESULT_ACTION, request.secondarySourceRef().kind());
        assertEquals(CraftingGridActionRequests.Route.RESULT_EXTRACT, resolution.route());
        assertEquals(11, resolution.spec().targetMenuSlot());
        assertEquals(java.util.List.of(11), resolution.spec().targetMenuSlots());
        assertEquals(CraftingGridActionRequests.ResultAction.QUICK_MOVE, resolution.spec().resultAction());
        assertEquals(1, resolution.spec().mouseButton());
        assertEquals(3, resolution.spec().repeatCount());
        assertNull(resolution.spec().identity());
    }
}
