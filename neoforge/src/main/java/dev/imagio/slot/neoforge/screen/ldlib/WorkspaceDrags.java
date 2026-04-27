package dev.imagio.slot.neoforge.screen.ldlib;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import net.minecraft.world.item.ItemStack;

final class WorkspaceDrags {
    private WorkspaceDrags() {
    }

    record AtlasItemDrag(
            SlotWorkspaceViewModel.IdentityRef identity,
            ItemStack displayStack,
            String originIslandId
    ) {
    }

    record HotbarSlotDrag(
            int hotbarIndex,
            ItemStack displayStack
    ) {
    }

    record IslandDrag(
            String islandId,
            int grabOffsetX,
            int grabOffsetY
    ) {
    }

    record ChestTileDrag(
            String storageId,
            int grabOffsetX,
            int grabOffsetY
    ) {
    }

    record ChestStackDrag(
            String storageId,
            int chestSlotIndex,
            ItemStack displayStack
    ) {
    }

    record KitSlotDrag(
            String kitId,
            int pageIndex,
            int slotIndex,
            SlotWorkspaceViewModel.IdentityRef identity,
            ItemStack displayStack
    ) {
    }

    record KitBringDrag(
            String kitId,
            SlotWorkspaceViewModel.IdentityRef identity,
            ItemStack displayStack
    ) {
    }
}
