package dev.imagio.slot.client.collection;

import dev.imagio.slot.client.model.ItemIdentity;

import java.util.function.ToIntFunction;

public record CollectionStockSummary(int missingCount, int lowCount) {
    public static final CollectionStockSummary NONE = new CollectionStockSummary(0, 0);

    public CollectionStockSummary {
        missingCount = Math.max(0, missingCount);
        lowCount = Math.max(0, lowCount);
    }

    public static CollectionStockSummary summarize(
            CollectionStore collectionStore,
            String collectionId,
            ToIntFunction<ItemIdentity> ownedCountResolver
    ) {
        if (collectionStore == null || collectionId == null || collectionId.isBlank() || ownedCountResolver == null) {
            return NONE;
        }

        int missing = 0;
        int low = 0;
        for (CollectionStore.CollectionItemTarget target : collectionStore.trackedItems(collectionId)) {
            int desiredCount = Math.max(1, target.desiredCount());
            int ownedCount = Math.max(0, ownedCountResolver.applyAsInt(target.identity()));
            if (ownedCount <= 0) {
                missing++;
            } else if (ownedCount < desiredCount) {
                low++;
            }
        }
        return missing == 0 && low == 0 ? NONE : new CollectionStockSummary(missing, low);
    }

    public boolean hasShortfall() {
        return missingCount > 0 || lowCount > 0;
    }
}
