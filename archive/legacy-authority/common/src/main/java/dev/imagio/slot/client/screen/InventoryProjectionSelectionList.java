package dev.imagio.slot.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;

import java.util.List;
import java.util.Objects;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

public class InventoryProjectionSelectionList<E extends ObjectSelectionList.Entry<E> & InventoryProjectionListRow>
        extends ObjectSelectionList<E> {
    private final InventoryListNavigationState navigationState;
    private final IntPredicate validMouseClick;
    private final Predicate<E> itemRowPredicate;
    private final Predicate<E> selectionHighlightPredicate;
    private final int selectionHighlightColor;

    public InventoryProjectionSelectionList(
            Minecraft minecraft,
            int width,
            int height,
            int top,
            int left,
            int rowHeight,
            String allTargetId,
            IntPredicate validMouseClick,
            Predicate<E> itemRowPredicate,
            Predicate<E> selectionHighlightPredicate,
            int selectionHighlightColor
    ) {
        super(minecraft, width, height, top, rowHeight);
        this.navigationState = new InventoryListNavigationState(Objects.requireNonNull(allTargetId, "allTargetId"));
        this.validMouseClick = Objects.requireNonNull(validMouseClick, "validMouseClick");
        this.itemRowPredicate = Objects.requireNonNull(itemRowPredicate, "itemRowPredicate");
        this.selectionHighlightPredicate = Objects.requireNonNull(selectionHighlightPredicate, "selectionHighlightPredicate");
        this.selectionHighlightColor = selectionHighlightColor;
        setPosition(left, top);
    }

    @Override
    protected boolean isValidMouseClick(int button) {
        return validMouseClick.test(button);
    }

    public void setRows(List<E> rows) {
        clearEntries();
        for (E row : rows) {
            addEntry(row);
        }
        navigationState.indexRows(rows, InventoryProjectionListRow::sectionId);
        setScrollAmount(Math.min(getScrollAmount(), getMaxScroll()));
    }

    public void navigateToTarget(String targetId) {
        navigationState.navigateToTarget(targetId, currentSectionId(), getScrollAmount(), getMaxScroll(), getY(), this::getRowTop)
                .ifPresent(this::setScrollAmount);
    }

    public void scrollToTarget(String targetId) {
        navigationState.scrollToTarget(targetId, getScrollAmount(), getMaxScroll(), getY(), this::getRowTop)
                .ifPresent(this::setScrollAmount);
    }

    public boolean hasAnyRows() {
        return InventoryListNavigationState.hasMatchingRow(children(), itemRowPredicate::test);
    }

    public String currentSectionId() {
        return navigationState.currentSectionId(
                getItemCount(),
                getScrollAmount(),
                getY(),
                this::getRowBottom,
                index -> getEntry(index).sectionId()
        );
    }

    public <T extends E> T entryAtPositionAs(double mouseX, double mouseY, Class<T> type) {
        E entry = getEntryAtPosition(mouseX, mouseY);
        return type.isInstance(entry) ? type.cast(entry) : null;
    }

    public E entryAtPosition(double mouseX, double mouseY) {
        return getEntryAtPosition(mouseX, mouseY);
    }

    @Override
    public int getRowWidth() {
        return getWidth() - 12;
    }

    @Override
    protected int getScrollbarPosition() {
        return getX() + getWidth() - 6;
    }

    @Override
    protected void renderListBackground(GuiGraphics guiGraphics) {
    }

    @Override
    protected void renderListSeparators(GuiGraphics guiGraphics) {
    }

    @Override
    protected void renderSelection(GuiGraphics guiGraphics, int y, int width, int height, int outerColor, int innerColor) {
        E selected = getSelected();
        if (selected == null || !selectionHighlightPredicate.test(selected)) {
            return;
        }
        guiGraphics.fill(getRowLeft(), y, getRowRight(), y + height, selectionHighlightColor);
    }

    public int rowTop(int rowIndex) {
        return getRowTop(rowIndex);
    }
}
