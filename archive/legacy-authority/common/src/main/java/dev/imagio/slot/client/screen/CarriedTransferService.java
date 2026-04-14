package dev.imagio.slot.client.screen;

import dev.imagio.slot.session.ChestLikeMenuLayout;
import dev.imagio.slot.projection.InventoryPane;
import dev.imagio.slot.projection.InventoryViewData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.List;

public final class CarriedTransferService {
    private final PlayerCarriedTransferService playerTransfers;
    private final WorkspaceCarriedTransferService workspaceTransfers;

    private CarriedTransferService(
            PlayerCarriedTransferService playerTransfers,
            WorkspaceCarriedTransferService workspaceTransfers
    ) {
        this.playerTransfers = playerTransfers;
        this.workspaceTransfers = workspaceTransfers;
    }

    public static CarriedTransferService forPlayer(InventoryScreenContext context) {
        return new CarriedTransferService(new PlayerCarriedTransferService(context), null);
    }

    public static CarriedTransferService forWorkspace(ChestLikeMenuLayout layout) {
        return new CarriedTransferService(null, new WorkspaceCarriedTransferService(layout));
    }

    public SlotActionResult moveOne(LocalPlayer player, InventoryViewData.EntryView entry) {
        return playerTransfers == null ? SlotActionResult.NONE : playerTransfers.moveOne(player, entry);
    }

    public SlotActionResult moveStack(LocalPlayer player, InventoryViewData.EntryView entry) {
        return playerTransfers == null ? SlotActionResult.NONE : playerTransfers.moveStack(player, entry);
    }

    public SlotActionResult moveAllOfType(LocalPlayer player, InventoryViewData.EntryView entry) {
        return playerTransfers == null ? SlotActionResult.NONE : playerTransfers.moveAllOfType(player, entry);
    }

    public SlotActionResult moveVisible(LocalPlayer player, List<InventoryViewData.EntryView> visibleEntries) {
        return playerTransfers == null ? SlotActionResult.NONE : playerTransfers.moveVisible(player, visibleEntries);
    }

    public SlotActionResult moveOne(LocalPlayer player, AbstractContainerMenu menu, InventoryViewData.EntryView entry, InventoryPane pane) {
        return workspaceTransfers == null ? SlotActionResult.NONE : workspaceTransfers.moveOne(player, menu, entry, pane);
    }

    public SlotActionResult moveStack(LocalPlayer player, AbstractContainerMenu menu, InventoryViewData.EntryView entry, InventoryPane pane) {
        return workspaceTransfers == null ? SlotActionResult.NONE : workspaceTransfers.moveStack(player, menu, entry, pane);
    }

    public SlotActionResult moveAllOfType(LocalPlayer player, AbstractContainerMenu menu, InventoryViewData.EntryView entry, InventoryPane pane) {
        return workspaceTransfers == null ? SlotActionResult.NONE : workspaceTransfers.moveAllOfType(player, menu, entry, pane);
    }

    public SlotActionResult moveVisible(LocalPlayer player, AbstractContainerMenu menu, List<InventoryViewData.EntryView> visibleEntries, InventoryPane pane) {
        return workspaceTransfers == null ? SlotActionResult.NONE : workspaceTransfers.moveVisible(player, menu, visibleEntries, pane);
    }
}
