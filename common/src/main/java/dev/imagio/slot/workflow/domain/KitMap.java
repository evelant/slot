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

    public KitMap withKitReordered(String kitId, int targetIndex) {
        KitDefinition moving = kit(kitId);
        if (moving == null) {
            return this;
        }
        ArrayList<KitDefinition> siblings = new ArrayList<>();
        for (KitDefinition kit : kits) {
            if (sameSiblingGroup(moving, kit)) {
                siblings.add(kit);
            }
        }
        int currentIndex = indexOf(siblings, kitId);
        if (currentIndex < 0 || siblings.size() <= 1) {
            return this;
        }
        int target = Math.max(0, Math.min(targetIndex, siblings.size() - 1));
        if (target == currentIndex) {
            return this;
        }
        siblings.remove(currentIndex);
        siblings.add(target, moving);
        ArrayList<KitDefinition> next = new ArrayList<>(kits.size());
        int siblingIndex = 0;
        for (KitDefinition kit : kits) {
            if (sameSiblingGroup(moving, kit)) {
                next.add(siblings.get(siblingIndex++));
            } else {
                next.add(kit);
            }
        }
        return new KitMap(next, activation);
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

    private static int indexOf(List<KitDefinition> source, String kitId) {
        if (source == null || kitId == null) {
            return -1;
        }
        for (int index = 0; index < source.size(); index++) {
            KitDefinition kit = source.get(index);
            if (kit != null && kitId.equals(kit.id())) {
                return index;
            }
        }
        return -1;
    }

    private static boolean sameSiblingGroup(KitDefinition left, KitDefinition right) {
        if (left == null || right == null) {
            return false;
        }
        if (left.variant() || right.variant()) {
            return left.variant()
                    && right.variant()
                    && left.parentId().equals(right.parentId());
        }
        return !right.variant();
    }
}
