package dev.imagio.slot.network;

import dev.imagio.slot.intent.ActionFamily;
import dev.imagio.slot.intent.ActionRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ToolActionRequestsTest {
    @Test
    void toolActionRequestResolvesToolIdAndAction() {
        ActionRequest request = ToolActionRequests.request(
                17,
                "fingerprint",
                "crafting_grid",
                ToolActionRequests.Action.ROTATE_GRID_CCW
        );

        ToolActionRequests.Resolution resolution = ToolActionRequests.resolve(request);

        assertEquals(ActionFamily.TOOL_ACTION, request.actionFamily());
        assertEquals("fingerprint", request.expectedSessionFingerprint());
        assertEquals(ToolActionRequests.KIND_TOOL_ACTION, request.primarySourceRef().kind());
        assertEquals("crafting_grid", request.toolRef());
        assertNotNull(resolution);
        assertEquals("crafting_grid", resolution.toolId());
        assertEquals(ToolActionRequests.Action.ROTATE_GRID_CCW, resolution.action());
    }

    @Test
    void toolActionTokenRoundTrips() {
        ActionRequest request = ToolActionRequests.request(
                4,
                "fp",
                "tool-1",
                ToolActionRequests.Action.TOGGLE_AUTO_REFILL
        );

        ToolActionRequests.Resolution resolution = ToolActionRequests.resolve(request);

        assertNotNull(resolution);
        assertEquals("tool-1", resolution.toolId());
        assertEquals(ToolActionRequests.Action.TOGGLE_AUTO_REFILL, resolution.action());
    }
}
