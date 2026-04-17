package dev.imagio.slot.inventory.triage;

import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public record IslandSignalDescriptor(
        ItemIdentity identity,
        Set<IslandSignal> classSignals,
        Set<String> itemTags,
        String namespace,
        String creativeTabId
) {
    public IslandSignalDescriptor {
        Objects.requireNonNull(identity, "identity");
        classSignals = classSignals == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(classSignals));
        itemTags = itemTags == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(itemTags));
        namespace = namespace == null ? "" : namespace;
        creativeTabId = creativeTabId == null ? "" : creativeTabId;
    }

    public static IslandSignalDescriptor empty(ItemIdentity identity) {
        String ns = identity == null ? "" : namespaceOf(identity.itemId());
        return new IslandSignalDescriptor(identity, Set.of(), Set.of(), ns, "");
    }

    private static String namespaceOf(String itemId) {
        if (itemId == null) {
            return "";
        }
        int colon = itemId.indexOf(':');
        return colon <= 0 ? "" : itemId.substring(0, colon);
    }
}
