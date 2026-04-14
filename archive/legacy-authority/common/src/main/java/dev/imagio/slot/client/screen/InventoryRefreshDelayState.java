package dev.imagio.slot.client.screen;

public final class InventoryRefreshDelayState {
    private int ticksRemaining;

    public void schedule(int ticks) {
        ticksRemaining = Math.max(0, ticks);
    }

    public void clear() {
        ticksRemaining = 0;
    }

    public boolean active() {
        return ticksRemaining > 0;
    }

    public int ticksRemaining() {
        return ticksRemaining;
    }

    public boolean tick(boolean refreshOnlyWhenExpired) {
        if (ticksRemaining <= 0) {
            return false;
        }

        ticksRemaining--;
        return !refreshOnlyWhenExpired || ticksRemaining <= 0;
    }
}
