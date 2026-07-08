package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityCollections;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.core.SlotResourceCollections;
import dev.imagio.slot.inventory.core.SlotResourceIdentity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public record CraftRunIngredientGroup(
        String groupId,
        String label,
        int requiredCountPerBatch,
        boolean consumed,
        ItemIdentity selectedAlternativeIdentity,
        SlotResourceIdentity selectedAlternativeResource,
        List<CraftRunAlternative> alternatives,
        long requiredAmountPerBatch,
        List<String> diagnostics
) {
    public CraftRunIngredientGroup(
            String groupId,
            String label,
            int requiredCountPerBatch,
            boolean consumed,
            ItemIdentity selectedAlternativeIdentity,
            List<CraftRunAlternative> alternatives,
            List<String> diagnostics
    ) {
        this(
                groupId,
                label,
                requiredCountPerBatch,
                consumed,
                selectedAlternativeIdentity,
                SlotResourceIdentity.item(selectedAlternativeIdentity),
                alternatives,
                requiredCountPerBatch,
                diagnostics);
    }

    public CraftRunIngredientGroup(
            String groupId,
            String label,
            int requiredCountPerBatch,
            boolean consumed,
            List<CraftRunAlternative> alternatives,
            List<String> diagnostics
    ) {
        this(groupId, label, requiredCountPerBatch, consumed, null, null, alternatives, requiredCountPerBatch, diagnostics);
    }

    public CraftRunIngredientGroup(
            String groupId,
            String label,
            long requiredAmountPerBatch,
            boolean consumed,
            SlotResourceIdentity selectedAlternativeResource,
            List<CraftRunAlternative> alternatives,
            List<String> diagnostics
    ) {
        this(
                groupId,
                label,
                saturatedInt(requiredAmountPerBatch),
                consumed,
                selectedAlternativeResource == null ? null : selectedAlternativeResource.toItemIdentity(),
                selectedAlternativeResource,
                alternatives,
                requiredAmountPerBatch,
                diagnostics);
    }

    public CraftRunIngredientGroup(
            String groupId,
            String label,
            int requiredCountPerBatch,
            List<CraftRunAlternative> alternatives,
            List<String> diagnostics
    ) {
        this(groupId, label, requiredCountPerBatch, true, alternatives, diagnostics);
    }

    public CraftRunIngredientGroup {
        groupId = groupId == null || groupId.isBlank() ? "ingredient" : groupId.trim();
        label = label == null || label.isBlank() ? groupId : label.trim();
        requiredCountPerBatch = Math.max(1, requiredCountPerBatch);
        alternatives = alternatives == null
                ? List.of()
                : List.copyOf(alternatives.stream()
                        .filter(alternative -> alternative != null && alternative.present())
                        .toList());
        selectedAlternativeResource = SlotResourceCollections.key(selectedAlternativeResource != null
                ? selectedAlternativeResource
                : SlotResourceIdentity.item(selectedAlternativeIdentity));
        if (selectedAlternativeResource != null && matchingAlternative(alternatives, selectedAlternativeResource) == null) {
            selectedAlternativeResource = null;
        }
        selectedAlternativeIdentity = selectedAlternativeResource != null && selectedAlternativeResource.item()
                ? selectedAlternativeResource.toItemIdentity()
                : ItemIdentityCollections.key(selectedAlternativeIdentity);
        if (selectedAlternativeResource == null
                || selectedAlternativeResource.fluid()
                || matchingAlternative(alternatives, selectedAlternativeIdentity) == null) {
            selectedAlternativeIdentity = null;
        }
        requiredAmountPerBatch = Math.max(1L, requiredAmountPerBatch <= 0L
                ? requiredCountPerBatch
                : requiredAmountPerBatch);
        diagnostics = diagnostics == null
                ? List.of()
                : List.copyOf(diagnostics.stream()
                        .filter(value -> value != null && !value.isBlank())
                        .map(String::trim)
                        .distinct()
                        .toList());
    }

    public boolean resolvable() {
        return !alternatives.isEmpty();
    }

    public int requiredForBatches(int batches) {
        if (batches <= 0) {
            return 0;
        }
        long required = consumed ? (long) requiredCountPerBatch * Math.max(1, batches) : 1L;
        return required >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) Math.max(1L, required);
    }

    public long requiredAmountForBatches(int batches) {
        if (batches <= 0) {
            return 0L;
        }
        long multiplier = consumed ? Math.max(1, batches) : 1L;
        if (requiredAmountPerBatch >= Long.MAX_VALUE / multiplier) {
            return Long.MAX_VALUE;
        }
        return Math.max(1L, requiredAmountPerBatch * multiplier);
    }

    public CraftRunAlternative selectedAlternative() {
        return matchingAlternative(alternatives, selectedAlternativeResource);
    }

    public CraftRunIngredientGroup withSelectedAlternative(ItemIdentity identity) {
        return withSelectedAlternative(SlotResourceIdentity.item(identity));
    }

    public CraftRunIngredientGroup withSelectedAlternative(SlotResourceIdentity identity) {
        SlotResourceIdentity key = SlotResourceCollections.key(identity);
        if (key != null && matchingAlternative(alternatives, key) == null) {
            return this;
        }
        if (key == null && selectedAlternativeResource == null) {
            return this;
        }
        if (key != null && key.equals(selectedAlternativeResource)) {
            return this;
        }
        return new CraftRunIngredientGroup(
                groupId,
                label,
                requiredCountPerBatch,
                consumed,
                key == null ? null : key.toItemIdentity(),
                key,
                alternatives,
                requiredAmountPerBatch,
                diagnostics);
    }

    public List<CraftRunAlternative> selectedOrAllAlternatives() {
        CraftRunAlternative selected = selectedAlternative();
        return selected == null ? alternatives : List.of(selected);
    }

    public static List<CraftRunIngredientGroup> normalize(List<CraftRunIngredientGroup> groups) {
        if (groups == null || groups.isEmpty()) {
            return List.of();
        }
        LinkedHashMap<String, Accumulator> byAlternatives = new LinkedHashMap<>();
        for (CraftRunIngredientGroup group : groups) {
            if (group == null || !group.resolvable()) {
                continue;
            }
            String key = group.consumed() + "|" + alternativesKey(group.alternatives());
            Accumulator accumulator = byAlternatives.get(key);
            if (accumulator == null) {
                byAlternatives.put(key, new Accumulator(group));
                continue;
            }
            accumulator.merge(group);
        }
        if (byAlternatives.isEmpty()) {
            return List.of();
        }
        ArrayList<CraftRunIngredientGroup> normalized = new ArrayList<>(byAlternatives.size());
        for (Accumulator accumulator : byAlternatives.values()) {
            normalized.add(accumulator.toGroup());
        }
        return List.copyOf(normalized);
    }

    private static String alternativesKey(List<CraftRunAlternative> alternatives) {
        if (alternatives == null || alternatives.isEmpty()) {
            return "";
        }
        return alternatives.stream()
                .filter(alternative -> alternative != null && alternative.present())
                .map(alternative -> identityKey(alternative.resourceIdentity()))
                .distinct()
                .sorted()
                .reduce((left, right) -> left + ";" + right)
                .orElse("");
    }

    private static String identityKey(SlotResourceIdentity identity) {
        SlotResourceIdentity key = SlotResourceCollections.key(identity);
        if (key == null) {
            return "";
        }
        return key.stableKey();
    }

    private static CraftRunAlternative matchingAlternative(
            List<CraftRunAlternative> alternatives,
            ItemIdentity identity
    ) {
        return matchingAlternative(alternatives, SlotResourceIdentity.item(identity));
    }

    private static CraftRunAlternative matchingAlternative(
            List<CraftRunAlternative> alternatives,
            SlotResourceIdentity identity
    ) {
        if (alternatives == null || alternatives.isEmpty() || identity == null) {
            return null;
        }
        SlotResourceIdentity target = SlotResourceCollections.key(identity);
        for (CraftRunAlternative alternative : alternatives) {
            if (alternative != null
                    && alternative.resourceIdentity() != null
                    && resourcesMatch(alternative.resourceIdentity(), target)) {
                return alternative;
            }
        }
        return null;
    }

    private static boolean resourcesMatch(SlotResourceIdentity left, SlotResourceIdentity right) {
        SlotResourceIdentity leftKey = SlotResourceCollections.key(left);
        SlotResourceIdentity rightKey = SlotResourceCollections.key(right);
        if (leftKey == null || rightKey == null || leftKey.kind() != rightKey.kind()) {
            return false;
        }
        if (leftKey.item()) {
            return ItemIdentityMatcher.matchesMovable(leftKey.toItemIdentity(), rightKey.toItemIdentity());
        }
        return leftKey.equals(rightKey);
    }

    private static int saturatedAdd(int left, int right) {
        long sum = (long) Math.max(0, left) + Math.max(0, right);
        return sum >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) sum;
    }

    private static long saturatedAdd(long left, long right) {
        if (left >= Long.MAX_VALUE - right) {
            return Long.MAX_VALUE;
        }
        return left + right;
    }

    private static int saturatedInt(long value) {
        if (value <= 0L) {
            return 1;
        }
        return value >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
    }

    private static final class Accumulator {
        private final String groupId;
        private final String label;
        private final boolean consumed;
        private final List<CraftRunAlternative> alternatives;
        private final LinkedHashMap<String, String> diagnostics = new LinkedHashMap<>();
        private SlotResourceIdentity selectedAlternativeResource;
        private int requiredCountPerBatch;
        private long requiredAmountPerBatch;

        private Accumulator(CraftRunIngredientGroup group) {
            this.groupId = group.groupId();
            this.label = group.label();
            this.consumed = group.consumed();
            this.alternatives = group.alternatives();
            this.selectedAlternativeResource = group.selectedAlternativeResource();
            this.requiredCountPerBatch = group.requiredCountPerBatch();
            this.requiredAmountPerBatch = group.requiredAmountPerBatch();
            for (String diagnostic : group.diagnostics()) {
                diagnostics.putIfAbsent(diagnostic, diagnostic);
            }
        }

        private void merge(CraftRunIngredientGroup group) {
            if (group == null) {
                return;
            }
            requiredCountPerBatch = consumed
                    ? saturatedAdd(requiredCountPerBatch, group.requiredCountPerBatch())
                    : Math.max(1, requiredCountPerBatch);
            requiredAmountPerBatch = consumed
                    ? saturatedAdd(requiredAmountPerBatch, group.requiredAmountPerBatch())
                    : Math.max(1L, requiredAmountPerBatch);
            if (selectedAlternativeResource == null && group.selectedAlternativeResource() != null) {
                selectedAlternativeResource = group.selectedAlternativeResource();
            }
            for (String diagnostic : group.diagnostics()) {
                diagnostics.putIfAbsent(diagnostic, diagnostic);
            }
        }

        private CraftRunIngredientGroup toGroup() {
            return new CraftRunIngredientGroup(
                    groupId,
                    label,
                    requiredCountPerBatch,
                    consumed,
                    selectedAlternativeResource == null ? null : selectedAlternativeResource.toItemIdentity(),
                    selectedAlternativeResource,
                    alternatives,
                    requiredAmountPerBatch,
                    List.copyOf(diagnostics.values()));
        }
    }
}
