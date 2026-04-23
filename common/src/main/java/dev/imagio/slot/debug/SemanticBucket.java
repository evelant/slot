package dev.imagio.slot.debug;

public enum SemanticBucket {
    TOOLS("tools", "Tools", 0xCC455A70, 0, 0),
    COMBAT("combat", "Combat", 0xCC7A3C3C, 0, 1),
    ARMOR("armor", "Armor", 0xCC5A6E7A, 0, 2),
    FOOD("food", "Food & Drinks", 0xCC6E5A3C, 0, 3),
    MATERIALS("materials", "Materials", 0xCC5A4A3C, 1, 0),
    BUILDING("building", "Building Blocks", 0xCC5A5A5A, 1, 1),
    NATURAL("natural", "Natural", 0xCC3C6E3C, 1, 2),
    DECORATION("decoration", "Decoration", 0xCC5A3C6E, 1, 3),
    REDSTONE("redstone", "Redstone", 0xCC6E2E2E, 2, 0),
    MECHANISMS("mechanisms", "Mechanisms", 0xCC8A5E24, 2, 1),
    WORKBENCHES("workbenches", "Workbenches", 0xCC6E3C24, 2, 2),
    STORAGE("storage", "Storage", 0xCC6E4A2E, 2, 3),
    UPGRADES("upgrades", "Upgrades", 0xCC4A5E8A, 3, 0),
    MISC("misc", "Miscellaneous", 0xCC3C6E6E, 3, 1);

    private final String id;
    private final String label;
    private final int color;
    private final int clusterRow;
    private final int clusterColumn;

    SemanticBucket(String id, String label, int color, int clusterRow, int clusterColumn) {
        this.id = id;
        this.label = label;
        this.color = color;
        this.clusterRow = clusterRow;
        this.clusterColumn = clusterColumn;
    }

    public String id() {
        return id;
    }

    public String label() {
        return label;
    }

    public int color() {
        return color;
    }

    public int clusterRow() {
        return clusterRow;
    }

    public int clusterColumn() {
        return clusterColumn;
    }
}
