package dev.imagio.slot.inventory.goal;

public record GoalAuthorityCount(
        int carriedCount,
        int proximateStorageCount,
        int elsewhereStorageCount
) {
    public GoalAuthorityCount {
        carriedCount = Math.max(0, carriedCount);
        proximateStorageCount = Math.max(0, proximateStorageCount);
        elsewhereStorageCount = Math.max(0, elsewhereStorageCount);
    }

    public int storageCount() {
        return proximateStorageCount + elsewhereStorageCount;
    }

    public int totalCount() {
        return carriedCount + storageCount();
    }
}
