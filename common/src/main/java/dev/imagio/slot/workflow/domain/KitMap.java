package dev.imagio.slot.workflow.domain;

import java.util.ArrayList;
import java.util.List;

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
        ArrayList<KitDefinition> next = new ArrayList<>(kits);
        next.removeIf(kit -> kit.id().equals(kitId));
        KitActivation nextActivation = activation.kitId().equals(kitId) ? KitActivation.NONE : activation;
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
