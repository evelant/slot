package dev.imagio.slot.client.screen.container;

import dev.imagio.slot.client.collection.CollectionStore;
import dev.imagio.slot.client.screen.InventoryScreenContext;
import dev.imagio.slot.projection.InventoryHostSnapshotService;
import dev.imagio.slot.projection.InventoryViewData;
import dev.imagio.slot.storage.adapter.ExternalStorageStackSnapshot;
import net.minecraft.client.Minecraft;
import java.util.List;

public final class SlotContainerInventoryDataBuilder {
    private final InventoryHostSnapshotService snapshotService = new InventoryHostSnapshotService();

    public InventoryViewData build(InventoryScreenContext context, CollectionStore collectionStore) {
        return build(context, collectionStore, null);
    }

    public InventoryViewData build(
            InventoryScreenContext context,
            CollectionStore collectionStore,
            List<ExternalStorageStackSnapshot> primarySnapshots
    ) {
        return snapshotService.buildContainerInventory(
                context.host(),
                Minecraft.getInstance().player,
                collectionStore,
                primarySnapshots
        );
    }
}
