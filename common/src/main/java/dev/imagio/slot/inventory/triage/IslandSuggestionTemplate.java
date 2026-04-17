package dev.imagio.slot.inventory.triage;

import java.util.Set;

public enum IslandSuggestionTemplate {
    FOOD(
            "template.food",
            "Food",
            0xCC7D5A3A,
            Set.of(IslandSignal.FOOD),
            Set.of()
    ),
    TOOLS(
            "template.tools",
            "Tools",
            0xCC5A6E3D,
            Set.of(IslandSignal.DIGGER_TOOL),
            Set.of()
    ),
    WEAPONS(
            "template.weapons",
            "Weapons",
            0xCC6E3D3D,
            Set.of(
                    IslandSignal.SWORD,
                    IslandSignal.BOW,
                    IslandSignal.CROSSBOW,
                    IslandSignal.TRIDENT,
                    IslandSignal.MACE
            ),
            Set.of()
    ),
    ARMOR(
            "template.armor",
            "Armor",
            0xCC3D5A6E,
            Set.of(
                    IslandSignal.ARMOR_HEAD,
                    IslandSignal.ARMOR_CHEST,
                    IslandSignal.ARMOR_LEGS,
                    IslandSignal.ARMOR_FEET
            ),
            Set.of()
    ),
    MATERIALS(
            "template.materials",
            "Materials",
            0xCC3D6E5A,
            Set.of(),
            Set.of("c:ingots", "c:gems", "c:raw_materials", "c:ores")
    ),
    STORAGE(
            "template.storage",
            "Storage",
            0xCC5A3D6E,
            Set.of(),
            Set.of("c:chests", "c:shulker_boxes", "c:barrels")
    );

    private final String defaultIslandId;
    private final String defaultLabel;
    private final int defaultColor;
    private final Set<IslandSignal> classSignals;
    private final Set<String> itemTagTriggers;

    IslandSuggestionTemplate(
            String defaultIslandId,
            String defaultLabel,
            int defaultColor,
            Set<IslandSignal> classSignals,
            Set<String> itemTagTriggers
    ) {
        this.defaultIslandId = defaultIslandId;
        this.defaultLabel = defaultLabel;
        this.defaultColor = defaultColor;
        this.classSignals = Set.copyOf(classSignals);
        this.itemTagTriggers = Set.copyOf(itemTagTriggers);
    }

    public String defaultIslandId() {
        return defaultIslandId;
    }

    public String defaultLabel() {
        return defaultLabel;
    }

    public int defaultColor() {
        return defaultColor;
    }

    public boolean matches(IslandSignalDescriptor descriptor) {
        if (descriptor == null) {
            return false;
        }
        for (IslandSignal signal : classSignals) {
            if (descriptor.classSignals().contains(signal)) {
                return true;
            }
        }
        for (String tag : itemTagTriggers) {
            if (descriptor.itemTags().contains(tag)) {
                return true;
            }
        }
        return false;
    }
}
