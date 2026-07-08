package dev.imagio.slot.inventory.core;

public record SlotResourceDisplay(
        SlotResourceIdentity identity,
        String label,
        String unit
) {
    public SlotResourceDisplay {
        label = label == null || label.isBlank()
                ? identity == null ? "Resource" : identity.id()
                : label.trim();
        unit = unit == null || unit.isBlank()
                ? identity != null && identity.fluid() ? "mB" : "items"
                : unit.trim();
    }

    public static SlotResourceDisplay fluid(SlotResourceIdentity identity, String label) {
        return new SlotResourceDisplay(identity, label, "mB");
    }

    public static String formatAmount(SlotResourceIdentity identity, long amount) {
        long value = Math.max(0L, amount);
        if (identity != null && identity.fluid()) {
            if (value >= 1000L && value % 1000L == 0L) {
                return (value / 1000L) + " B";
            }
            return value + " mB";
        }
        return Long.toString(value);
    }
}
