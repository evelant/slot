package dev.imagio.slot.client.category;

public enum SlotCategory {
    BUILDING("Building"),
    DECORATION("Decoration"),
    TOOLS_AND_UTILITY("Tools & Utility"),
    COMBAT("Combat"),
    WEARABLES("Wearables"),
    CONSUMABLES("Consumables"),
    NATURE_AND_FARMING("Nature & Farming"),
    MATERIALS("Materials"),
    COMPONENTS("Components"),
    MACHINES_AND_WORKSTATIONS("Machines & Workstations"),
    STORAGE_AND_TRANSPORT("Storage & Transport"),
    MISC("Misc");

    private final String displayName;

    SlotCategory(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
