package dev.imagio.slot.client.screen;

import dev.imagio.slot.projection.InventoryPane;
import dev.imagio.slot.projection.InventoryViewData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.List;

public final class InventoryTransferActionSupport {
    private InventoryTransferActionSupport() {
    }

    public static SlotActionResult movePlayerEntry(
            CarriedTransferService transfers,
            LocalPlayer player,
            InventoryViewData.EntryView entry,
            EntryMoveMode mode
    ) {
        if (transfers == null || player == null || entry == null || mode == null) {
            return SlotActionResult.NONE;
        }
        return switch (mode) {
            case ONE -> transfers.moveOne(player, entry);
            case STACK -> transfers.moveStack(player, entry);
            case ALL_OF_TYPE -> transfers.moveAllOfType(player, entry);
        };
    }

    public static SlotActionResult moveWorkspaceEntry(
            CarriedTransferService transfers,
            LocalPlayer player,
            AbstractContainerMenu menu,
            InventoryViewData.EntryView entry,
            InventoryPane pane,
            EntryMoveMode mode
    ) {
        if (transfers == null || player == null || menu == null || entry == null || pane == null || mode == null) {
            return SlotActionResult.NONE;
        }
        return switch (mode) {
            case ONE -> transfers.moveOne(player, menu, entry, pane);
            case STACK -> transfers.moveStack(player, menu, entry, pane);
            case ALL_OF_TYPE -> transfers.moveAllOfType(player, menu, entry, pane);
        };
    }

    public static SlotActionResult moveWorkspaceVisible(
            CarriedTransferService transfers,
            LocalPlayer player,
            AbstractContainerMenu menu,
            List<InventoryViewData.EntryView> entries,
            InventoryPane pane
    ) {
        if (transfers == null || player == null || menu == null || entries == null || entries.isEmpty() || pane == null) {
            return SlotActionResult.NONE;
        }
        return transfers.moveVisible(player, menu, entries, pane);
    }

    public static SlotUndoHistory.TransferDirection directionForPane(InventoryPane pane) {
        return pane == InventoryPane.OPEN_CONTAINER
                ? SlotUndoHistory.TransferDirection.EXTERNAL_TO_CARRIED
                : SlotUndoHistory.TransferDirection.CARRIED_TO_EXTERNAL;
    }

    public enum EntryMoveMode {
        ONE,
        STACK,
        ALL_OF_TYPE
    }
}
