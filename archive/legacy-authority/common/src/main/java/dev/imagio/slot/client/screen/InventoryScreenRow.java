package dev.imagio.slot.client.screen;

import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;

public abstract class InventoryScreenRow extends ObjectSelectionList.Entry<InventoryScreenRow> implements InventoryProjectionListRow {
    private final String rowId;

    protected InventoryScreenRow(String rowId) {
        this.rowId = rowId;
    }

    @Override
    public String rowId() {
        return rowId;
    }

    @Override
    public String sectionId() {
        return null;
    }

    @Override
    public Component getNarration() {
        return Component.empty();
    }
}
