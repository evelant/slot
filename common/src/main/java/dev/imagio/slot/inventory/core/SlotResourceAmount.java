package dev.imagio.slot.inventory.core;

public record SlotResourceAmount(
        SlotResourceIdentity identity,
        long amount
) {
    public SlotResourceAmount {
        amount = Math.max(0L, amount);
    }

    public boolean present() {
        return identity != null && amount > 0L;
    }
}
