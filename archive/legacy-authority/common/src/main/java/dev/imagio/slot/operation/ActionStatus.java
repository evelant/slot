package dev.imagio.slot.operation;

public enum ActionStatus {
    REQUESTED,
    PENDING,
    CONFIRMED,
    BLOCKED,
    FAILED;

    public boolean terminal() {
        return this == CONFIRMED || this == BLOCKED || this == FAILED;
    }
}
