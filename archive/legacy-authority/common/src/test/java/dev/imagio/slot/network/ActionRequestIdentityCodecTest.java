package dev.imagio.slot.network;

import dev.imagio.slot.client.model.ComparisonMode;
import dev.imagio.slot.client.model.ItemIdentity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ActionRequestIdentityCodecTest {
    @Test
    void roundTripsExactIdentity() {
        ItemIdentity identity = new ItemIdentity("minecraft:diamond_pickaxe", ComparisonMode.ITEM_ID_AND_COMPONENTS, "hash:abc/123");

        String encoded = ActionRequestIdentityCodec.encode(identity);
        ItemIdentity decoded = ActionRequestIdentityCodec.decode(encoded);

        assertEquals(identity, decoded);
    }

    @Test
    void rejectsMalformedIdentityPayload() {
        assertNull(ActionRequestIdentityCodec.decode("not-a-valid-identity-key"));
    }
}
