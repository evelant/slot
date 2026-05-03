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

    /**
     * Section reorder drag — payload for the TOC drag-to-reorder gesture
     * that moves an island to a new ordinal position in the wall. Carries
     * just the source island id; the drop target resolves the target
     * ordinal from cursor position over the TOC row strip.
     */
    record IslandDrag(
            String islandId
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
