package dev.imagio.slot.neoforge.screen.ldlib;

enum DisclosureLevel {
    REGION,
    BROWSE,
    READ,
    INSPECT,
    DETAIL;

    static DisclosureLevel fromScreenBudget(int cellBudgetPx) {
        return AtlasRenderBudget.forScreenBudget(cellBudgetPx).level();
    }

    boolean atLeast(DisclosureLevel minimum) {
        return ordinal() >= minimum.ordinal();
    }
}
