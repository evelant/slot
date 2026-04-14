package dev.imagio.slot.client.screen.debug;

import dev.imagio.slot.client.collection.CollectionStore;
import dev.imagio.slot.client.screen.InventoryScreenContext;
import dev.imagio.slot.projection.InventoryHostSnapshotService;
import dev.imagio.slot.projection.InventoryViewData;
import net.minecraft.client.player.LocalPlayer;

public final class InventoryViewDataBuilder {
    private final InventoryHostSnapshotService snapshotService = new InventoryHostSnapshotService();

    public InventoryViewData build(LocalPlayer player, CollectionStore collectionStore) {
        return build(player, collectionStore, null);
    }

    public InventoryViewData build(LocalPlayer player, CollectionStore collectionStore, InventoryScreenContext context) {
        return snapshotService.buildPlayerInventory(
                player,
                collectionStore,
                context == null ? null : context.host()
        );
    }
}
