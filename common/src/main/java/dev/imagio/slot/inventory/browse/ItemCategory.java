package dev.imagio.slot.inventory.browse;

public enum ItemCategory {
    BUILDING("Building"),
    DECORATION("Decoration"),
    TOOLS_UTILITY("Tools & Utility"),
    COMBAT("Combat"),
    WEARABLES("Wearables"),
    CONSUMABLES("Consumables"),
    NATURE_FARMING("Nature & Farming"),
    MATERIALS("Materials"),
    COMPONENTS("Components"),
    MACHINES_WORKSTATIONS("Machines & Workstations"),
    STORAGE_TRANSPORT("Storage & Transport"),
    MISC("Misc");

    private final String title;

    ItemCategory(String title) {
        this.title = title;
    }

    public String title() {
        return title;
    }
}
