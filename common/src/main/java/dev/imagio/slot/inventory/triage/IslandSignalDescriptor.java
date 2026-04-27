package dev.imagio.slot.inventory.triage;

import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record IslandSignalDescriptor(
        ItemIdentity identity,
        Set<IslandSignal> classSignals,
        Set<String> itemTags,
        String namespace,
        String creativeTabId,
        String role,
        List<String> roleAlternatives,
        String materialFamily,
        List<String> subsystems,
        List<String> activities,
        String flavor,
        String carryFrequency,
        String rarity,
        String origin,
        String dyeColor,
        String form,
        boolean emitsLight
) {
    public IslandSignalDescriptor {
        Objects.requireNonNull(identity, "identity");
        classSignals = classSignals == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(classSignals));
        itemTags = itemTags == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(itemTags));
        namespace = namespace == null ? "" : namespace;
        creativeTabId = creativeTabId == null ? "" : creativeTabId;
        role = role == null || role.isBlank() ? null : role;
        // roleAlternatives always at least contains the primary role when
        // role is set. Callers that only know a single role can pass null
        // and we'll backfill.
        if (roleAlternatives == null || roleAlternatives.isEmpty()) {
            roleAlternatives = role == null ? List.of() : List.of(role);
        } else {
            roleAlternatives = List.copyOf(roleAlternatives);
        }
        materialFamily = materialFamily == null || materialFamily.isBlank() ? null : materialFamily;
        subsystems = subsystems == null ? List.of() : List.copyOf(subsystems);
        activities = activities == null ? List.of() : List.copyOf(activities);
        flavor = flavor == null || flavor.isBlank() ? null : flavor;
        carryFrequency = carryFrequency == null || carryFrequency.isBlank() ? null : carryFrequency;
        rarity = rarity == null || rarity.isBlank() ? null : rarity;
        origin = origin == null || origin.isBlank() ? null : origin;
        dyeColor = dyeColor == null || dyeColor.isBlank() ? null : dyeColor;
        form = form == null || form.isBlank() ? null : form;
    }

    public IslandSignalDescriptor(
            ItemIdentity identity,
            Set<IslandSignal> classSignals,
            Set<String> itemTags,
            String namespace,
            String creativeTabId
    ) {
        this(identity, classSignals, itemTags, namespace, creativeTabId, null, null, null,
                List.of(), List.of(), null, null, null, null, null, null, false);
    }

    public IslandSignalDescriptor(
            ItemIdentity identity,
            Set<IslandSignal> classSignals,
            Set<String> itemTags,
            String namespace,
            String creativeTabId,
            String role
    ) {
        this(identity, classSignals, itemTags, namespace, creativeTabId, role, null, null,
                List.of(), List.of(), null, null, null, null, null, null, false);
    }

    public IslandSignalDescriptor(
            ItemIdentity identity,
            Set<IslandSignal> classSignals,
            Set<String> itemTags,
            String namespace,
            String creativeTabId,
            String role,
            String materialFamily
    ) {
        this(identity, classSignals, itemTags, namespace, creativeTabId, role, null, materialFamily,
                List.of(), List.of(), null, null, null, null, null, null, false);
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
