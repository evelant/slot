package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.ItemComparisonMode;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityCollections;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public final class RemoteDetailIdentityPayload {
    public static final int MAX_IDENTITIES = 128;
    public static final int MAX_PAYLOAD_CHARS = 32_767;

    private RemoteDetailIdentityPayload() {
    }

    public static String encode(Collection<ItemIdentity> identities) {
        if (identities == null || identities.isEmpty()) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        int count = 0;
        for (ItemIdentity identity : identities) {
            if (identity == null || count >= MAX_IDENTITIES) {
                continue;
            }
            String line = encodeLine(ItemIdentityCollections.key(identity));
            if (line.isBlank()) {
                continue;
            }
            int nextLength = out.length() + (out.isEmpty() ? 0 : 1) + line.length();
            if (nextLength > MAX_PAYLOAD_CHARS) {
                break;
            }
            if (!out.isEmpty()) {
                out.append('\n');
            }
            out.append(line);
            count++;
        }
        return out.toString();
    }

    public static Set<ItemIdentity> decode(String payload) {
        if (payload == null || payload.isBlank()) {
            return Set.of();
        }
        String bounded = payload.length() > MAX_PAYLOAD_CHARS
                ? payload.substring(0, MAX_PAYLOAD_CHARS)
                : payload;
        LinkedHashSet<ItemIdentity> identities = new LinkedHashSet<>();
        String[] lines = bounded.split("\\R");
        for (String line : lines) {
            if (identities.size() >= MAX_IDENTITIES) {
                break;
            }
            ItemIdentity identity = decodeLine(line);
            if (identity != null) {
                ItemIdentityCollections.add(identities, identity);
            }
        }
        return identities.isEmpty() ? Set.of() : Set.copyOf(identities);
    }

    private static String encodeLine(ItemIdentity identity) {
        if (identity == null || identity.itemId().isBlank()) {
            return "";
        }
        String fingerprint = identity.componentFingerprint() == null
                ? ""
                : identity.componentFingerprint();
        String encodedFingerprint = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(fingerprint.getBytes(StandardCharsets.UTF_8));
        return identity.itemId() + '\t' + identity.comparisonMode().name() + '\t' + encodedFingerprint;
    }

    private static ItemIdentity decodeLine(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }
        String[] parts = line.split("\\t", -1);
        if (parts.length != 3 || parts[0].isBlank()) {
            return null;
        }
        try {
            ItemComparisonMode mode = ItemComparisonMode.valueOf(parts[1]);
            String fingerprint = parts[2].isBlank()
                    ? ""
                    : new String(Base64.getUrlDecoder().decode(parts[2]), StandardCharsets.UTF_8);
            return new ItemIdentity(parts[0], mode, fingerprint);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
