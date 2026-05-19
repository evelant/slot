package dev.imagio.slot.workflow.domain;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record KitMap(List<KitDefinition> kits, KitActivation activation) {
    public KitMap {
        kits = kits == null ? List.of() : List.copyOf(kits);
        activation = activation == null ? KitActivation.NONE : activation;
    }

    public static KitMap empty() {
        return new KitMap(List.of(), KitActivation.NONE);
    }

    public KitDefinition kit(String kitId) {
        if (kitId == null || kitId.isBlank()) {
            return null;
        }
        for (KitDefinition kit : kits) {
            if (kit.id().equals(kitId)) {
                return kit;
            }
        }
        return null;
    }

    public KitDefinition activeKit() {
        return activation == null || !activation.isActive() ? null : kit(activation.kitId());
    }

    public KitDefinition parentOf(KitDefinition kit) {
        if (kit == null || kit.parentId().isBlank()) {
            return null;
        }
        return kit(kit.parentId());
    }

    /**
     * Active workflow inheritance chain, ordered from broadest to narrowest:
     * parent tab first, then active variant; or just the active parent tab.
     */
    public List<KitDefinition> activeLineage() {
        KitDefinition active = activeKit();
        if (active == null) {
            return List.of();
        }
        KitDefinition parent = parentOf(active);
        if (parent == null) {
            return List.of(active);
        }
        return List.of(parent, active);
    }

    public List<KitDefinition> variantsOf(String parentId) {
        if (parentId == null || parentId.isBlank()) {
            return List.of();
        }
        ArrayList<KitDefinition> variants = new ArrayList<>();
        for (KitDefinition kit : kits) {
            if (kit != null && parentId.equals(kit.parentId())) {
                variants.add(kit);
            }
        }
        return List.copyOf(variants);
    }

    public Set<String> idsRemovedByDeleting(String kitId) {
        if (kitId == null || kitId.isBlank() || kit(kitId) == null) {
            return Set.of();
        }
        LinkedHashSet<String> removed = new LinkedHashSet<>();
        removed.add(kitId);
        for (KitDefinition kit : kits) {
            if (kit != null && kitId.equals(kit.parentId())) {
                removed.add(kit.id());
            }
        }
        return Set.copyOf(removed);
    }

    public KitMap withKit(KitDefinition kit) {
        if (kit == null || kit.id().isBlank()) {
            return this;
        }
        ArrayList<KitDefinition> next = new ArrayList<>(kits);
        int index = indexOf(kit.id());
        if (index >= 0) {
            next.set(index, kit);
        } else {
            next.add(kit);
        }
        return new KitMap(next, activation);
    }

    public KitMap withoutKit(String kitId) {
        if (kitId == null || kitId.isBlank()) {
            return this;
        }
        Set<String> removedIds = idsRemovedByDeleting(kitId);
        ArrayList<KitDefinition> next = new ArrayList<>(kits);
        next.removeIf(kit -> removedIds.contains(kit.id()));
        KitActivation nextActivation = removedIds.contains(activation.kitId()) ? KitActivation.NONE : activation;
        return new KitMap(next, nextActivation);
    }

    public KitMap withActivation(KitActivation next) {
        return new KitMap(kits, next == null ? KitActivation.NONE : next);
    }

    private int indexOf(String kitId) {
        for (int index = 0; index < kits.size(); index++) {
            if (kits.get(index).id().equals(kitId)) {
                return index;
            }
        }
        return -1;
    }
}
