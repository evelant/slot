package dev.imagio.slot.client.screen;

import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.projection.InventoryPane;
import net.minecraft.world.item.ItemStack;

public final class InventoryHotbarInteractionState {
    private ItemIdentity dragIdentity;
    private ItemStack dragStack = ItemStack.EMPTY;
    private InventoryPane dragPane = InventoryPane.CARRIED;
    private boolean cursorInteractionActive;
    private int cursorInteractionOriginSlot = -1;
    private int cursorInteractionButton;

    public void beginDrag(ItemIdentity identity, ItemStack displayStack, InventoryPane pane) {
        dragIdentity = identity;
        dragStack = displayStack == null ? ItemStack.EMPTY : displayStack.copy();
        if (!dragStack.isEmpty()) {
            dragStack.setCount(1);
        }
        dragPane = pane == null ? InventoryPane.CARRIED : pane;
    }

    public boolean hasDrag() {
        return dragIdentity != null && !dragStack.isEmpty();
    }

    public DragSnapshot consumeDrag() {
        if (!hasDrag()) {
            return DragSnapshot.inactive();
        }

        DragSnapshot snapshot = new DragSnapshot(true, dragIdentity, dragStack.copy(), dragPane);
        clearDrag();
        return snapshot;
    }

    public void clearDrag() {
        dragIdentity = null;
        dragStack = ItemStack.EMPTY;
        dragPane = InventoryPane.CARRIED;
    }

    public ItemStack dragStack() {
        return dragStack;
    }

    public void armCursorInteraction(int originSlot, int mouseButton) {
        cursorInteractionActive = true;
        cursorInteractionOriginSlot = originSlot;
        cursorInteractionButton = mouseButton;
    }

    public boolean cursorInteractionActive() {
        return cursorInteractionActive;
    }

    public CursorInteraction consumeCursorInteraction() {
        if (!cursorInteractionActive) {
            return CursorInteraction.inactive();
        }

        CursorInteraction interaction = new CursorInteraction(true, cursorInteractionOriginSlot, cursorInteractionButton);
        clearCursorInteraction();
        return interaction;
    }

    public void clearCursorInteraction() {
        cursorInteractionActive = false;
        cursorInteractionOriginSlot = -1;
        cursorInteractionButton = 0;
    }

    public record DragSnapshot(
            boolean active,
            ItemIdentity identity,
            ItemStack stack,
            InventoryPane pane
    ) {
        private static DragSnapshot inactive() {
            return new DragSnapshot(false, null, ItemStack.EMPTY, InventoryPane.CARRIED);
        }
    }

    public record CursorInteraction(
            boolean active,
            int originSlot,
            int button
    ) {
        private static CursorInteraction inactive() {
            return new CursorInteraction(false, -1, 0);
        }
    }
}
