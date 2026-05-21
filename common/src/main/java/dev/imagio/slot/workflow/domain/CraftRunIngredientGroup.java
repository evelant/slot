package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityCollections;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public record CraftRunIngredientGroup(
        String groupId,
        String label,
        int requiredCountPerBatch,
        boolean consumed,
        ItemIdentity selectedAlternativeIdentity,
        List<CraftRunAlternative> alternatives,
        List<String> diagnostics
) {
    public CraftRunIngredientGroup(
            String groupId,
            String label,
            int requiredCountPerBatch,
            boolean consumed,
            List<CraftRunAlternative> alternatives,
            List<String> diagnostics
    ) {
        this(groupId, label, requiredCountPerBatch, consumed, null, alternatives, diagnostics);
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
        selectedAlternativeIdentity = ItemIdentityCollections.key(selectedAlternativeIdentity);
        if (selectedAlternativeIdentity != null && matchingAlternative(alternatives, selectedAlternativeIdentity) == null) {
            selectedAlternativeIdentity = null;
        }
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
        long required = consumed ? (long) requiredCountPerBatch * Math.max(1, batches) : 1L;
        return required >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) Math.max(1L, required);
    }

    public CraftRunAlternative selectedAlternative() {
        return matchingAlternative(alternatives, selectedAlternativeIdentity);
    }

    public CraftRunIngredientGroup withSelectedAlternative(ItemIdentity identity) {
        ItemIdentity key = ItemIdentityCollections.key(identity);
        if (key != null && matchingAlternative(alternatives, key) == null) {
            return this;
        }
        if (key == null && selectedAlternativeIdentity == null) {
            return this;
        }
        if (key != null && key.equals(selectedAlternativeIdentity)) {
            return this;
        }
        return new CraftRunIngredientGroup(
                groupId,
                label,
                requiredCountPerBatch,
                consumed,
                key,
                alternatives,
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
                .map(alternative -> identityKey(alternative.identity()))
                .distinct()
                .sorted()
                .reduce((left, right) -> left + ";" + right)
                .orElse("");
    }

    private static String identityKey(ItemIdentity identity) {
        ItemIdentity key = ItemIdentityCollections.key(identity);
        if (key == null) {
            return "";
        }
        return key.itemId() + "|" + key.comparisonMode().name() + "|" + key.componentFingerprint();
    }

    private static CraftRunAlternative matchingAlternative(
            List<CraftRunAlternative> alternatives,
            ItemIdentity identity
    ) {
        if (alternatives == null || alternatives.isEmpty() || identity == null) {
            return null;
        }
        for (CraftRunAlternative alternative : alternatives) {
            if (alternative != null
                    && alternative.identity() != null
                    && ItemIdentityMatcher.matchesMovable(alternative.identity(), identity)) {
                return alternative;
            }
        }
        return null;
    }

    private static int saturatedAdd(int left, int right) {
        long sum = (long) Math.max(0, left) + Math.max(0, right);
        return sum >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) sum;
    }

    private static final class Accumulator {
        private final String groupId;
        private final String label;
        private final boolean consumed;
        private final List<CraftRunAlternative> alternatives;
        private final LinkedHashMap<String, String> diagnostics = new LinkedHashMap<>();
        private ItemIdentity selectedAlternativeIdentity;
        private int requiredCountPerBatch;

        private Accumulator(CraftRunIngredientGroup group) {
            this.groupId = group.groupId();
            this.label = group.label();
            this.consumed = group.consumed();
            this.alternatives = group.alternatives();
            this.selectedAlternativeIdentity = group.selectedAlternativeIdentity();
            this.requiredCountPerBatch = group.requiredCountPerBatch();
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
            if (selectedAlternativeIdentity == null && group.selectedAlternativeIdentity() != null) {
                selectedAlternativeIdentity = group.selectedAlternativeIdentity();
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
                    selectedAlternativeIdentity,
                    alternatives,
                    List.copyOf(diagnostics.values()));
        }
    }
}
