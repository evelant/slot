package dev.imagio.slot.network;

import dev.imagio.slot.client.model.ComparisonMode;
import dev.imagio.slot.client.model.ItemIdentity;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class ActionRequestIdentityCodec {
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private ActionRequestIdentityCodec() {
    }

    public static String encode(ItemIdentity identity) {
        if (identity == null) {
            return "";
        }
        return identity.comparisonMode().name()
                + ":" + encodePart(identity.itemId())
                + ":" + encodePart(identity.componentFingerprint());
    }

    public static ItemIdentity decode(String encoded) {
        if (encoded == null || encoded.isBlank()) {
            return null;
        }

        String[] parts = encoded.split(":", 3);
        if (parts.length != 3) {
            return null;
        }

        try {
            ComparisonMode comparisonMode = ComparisonMode.valueOf(parts[0]);
            String itemId = decodePart(parts[1]);
            String componentFingerprint = decodePart(parts[2]);
            if (itemId.isBlank()) {
                return null;
            }
            return new ItemIdentity(itemId, comparisonMode, componentFingerprint);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static String encodePart(String value) {
        String resolved = value == null ? "" : value;
        return ENCODER.encodeToString(resolved.getBytes(StandardCharsets.UTF_8));
    }

    private static String decodePart(String encoded) {
        if (encoded == null || encoded.isBlank()) {
            return "";
        }
        return new String(DECODER.decode(encoded), StandardCharsets.UTF_8);
    }
}
