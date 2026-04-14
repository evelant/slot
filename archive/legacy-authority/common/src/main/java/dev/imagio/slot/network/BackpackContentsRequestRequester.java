package dev.imagio.slot.network;

import dev.imagio.slot.client.SlotClientCompat;

public final class BackpackContentsRequestRequester {
    private BackpackContentsRequestRequester() {
    }

    public static boolean requestSync() {
        return SlotClientCompat.sendToServer(new BackpackContentsRequestPayload());
    }
}
