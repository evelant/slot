package dev.imagio.slot.client.category;

import dev.imagio.slot.client.model.ItemIdentity;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public final class CategoryResolver {
    private final Map<String, SlotCategory> exactOverrides = new HashMap<>();
    private final Map<String, SlotCategory> namespaceDefaults = new HashMap<>();

    public CategoryResolver addExactOverride(String itemId, SlotCategory category) {
        exactOverrides.put(itemId, category);
        return this;
    }

    public CategoryResolver addNamespaceDefault(String namespace, SlotCategory category) {
        namespaceDefaults.put(namespace, category);
        return this;
    }

    public SlotCategory resolve(CategorySubject subject) {
        return resolveDetailed(subject).category();
    }

    public Resolution resolveDetailed(CategorySubject subject) {
        ItemIdentity identity = subject.identity();
        EnumSet<CategorySignal> signals = subject.signals();

        SlotCategory exact = exactOverrides.get(identity.itemId());
        if (exact != null) {
            return new Resolution(exact, ResolutionSource.EXACT_OVERRIDE, signals);
        }

        SlotCategory namespaceDefault = namespaceDefaults.get(identity.namespace());
        if (namespaceDefault != null) {
            return new Resolution(namespaceDefault, ResolutionSource.NAMESPACE_DEFAULT, signals);
        }

        if (signals.contains(CategorySignal.STORAGE)) {
            return new Resolution(SlotCategory.STORAGE_AND_TRANSPORT, ResolutionSource.STORAGE_SIGNAL, signals);
        }
        if (signals.contains(CategorySignal.MACHINE)) {
            return new Resolution(SlotCategory.MACHINES_AND_WORKSTATIONS, ResolutionSource.MACHINE_SIGNAL, signals);
        }
        if (signals.contains(CategorySignal.WEARABLE)) {
            return new Resolution(SlotCategory.WEARABLES, ResolutionSource.WEARABLE_SIGNAL, signals);
        }
        if (signals.contains(CategorySignal.COMBAT)) {
            return new Resolution(SlotCategory.COMBAT, ResolutionSource.COMBAT_SIGNAL, signals);
        }
        if (signals.contains(CategorySignal.CONSUMABLE)) {
            return new Resolution(SlotCategory.CONSUMABLES, ResolutionSource.CONSUMABLE_SIGNAL, signals);
        }
        if (signals.contains(CategorySignal.NATURE)) {
            return new Resolution(SlotCategory.NATURE_AND_FARMING, ResolutionSource.NATURE_SIGNAL, signals);
        }
        if (signals.contains(CategorySignal.TOOL)) {
            return new Resolution(SlotCategory.TOOLS_AND_UTILITY, ResolutionSource.TOOL_SIGNAL, signals);
        }
        if (signals.contains(CategorySignal.BUILDING)) {
            return new Resolution(SlotCategory.BUILDING, ResolutionSource.BUILDING_SIGNAL, signals);
        }
        if (signals.contains(CategorySignal.DECORATION)) {
            return new Resolution(SlotCategory.DECORATION, ResolutionSource.DECORATION_SIGNAL, signals);
        }
        if (signals.contains(CategorySignal.COMPONENT)) {
            return new Resolution(SlotCategory.COMPONENTS, ResolutionSource.COMPONENT_SIGNAL, signals);
        }
        if (signals.contains(CategorySignal.MATERIAL)) {
            return new Resolution(SlotCategory.MATERIALS, ResolutionSource.MATERIAL_SIGNAL, signals);
        }

        return new Resolution(SlotCategory.MISC, ResolutionSource.FALLBACK_MISC, signals);
    }

    public enum ResolutionSource {
        EXACT_OVERRIDE("Exact override"),
        NAMESPACE_DEFAULT("Namespace default"),
        STORAGE_SIGNAL("Storage heuristic"),
        MACHINE_SIGNAL("Machine heuristic"),
        WEARABLE_SIGNAL("Wearable heuristic"),
        COMBAT_SIGNAL("Combat heuristic"),
        CONSUMABLE_SIGNAL("Consumable heuristic"),
        NATURE_SIGNAL("Nature heuristic"),
        TOOL_SIGNAL("Tool heuristic"),
        BUILDING_SIGNAL("Building heuristic"),
        DECORATION_SIGNAL("Decoration heuristic"),
        COMPONENT_SIGNAL("Component heuristic"),
        MATERIAL_SIGNAL("Material heuristic"),
        FALLBACK_MISC("Fallback misc");

        private final String displayName;

        ResolutionSource(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return displayName;
        }
    }

    public record Resolution(
            SlotCategory category,
            ResolutionSource source,
            EnumSet<CategorySignal> signals
    ) {
        public Resolution {
            signals = signals == null || signals.isEmpty()
                    ? EnumSet.noneOf(CategorySignal.class)
                    : EnumSet.copyOf(signals);
        }
    }
}
