package dev.imagio.slot.ui.spi;

public final class SlotUiLayout {
    public enum FlexDirection {
        ROW,
        COLUMN
    }

    public enum AlignItems {
        STRETCH,
        CENTER,
        FLEX_START
    }

    public enum AlignContent {
        FLEX_START,
        SPACE_BETWEEN
    }

    public enum FlexWrap {
        NO_WRAP,
        WRAP
    }

    public enum PositionType {
        RELATIVE,
        ABSOLUTE
    }

    private float width = Float.NaN;
    private float height = Float.NaN;
    private float widthPercent = Float.NaN;
    private float heightPercent = Float.NaN;
    private float maxWidth = Float.NaN;
    private float flex = Float.NaN;
    private float paddingAll = Float.NaN;
    private float paddingHorizontal = Float.NaN;
    private float paddingVertical = Float.NaN;
    private float paddingLeft = Float.NaN;
    private float paddingRight = Float.NaN;
    private float gapAll = Float.NaN;
    private float left = Float.NaN;
    private float right = Float.NaN;
    private float top = Float.NaN;
    private float bottom = Float.NaN;
    private FlexDirection flexDirection;
    private AlignItems alignItems;
    private AlignContent alignContent;
    private FlexWrap flexWrap;
    private PositionType positionType;

    public SlotUiLayout width(float value) {
        this.width = value;
        return this;
    }

    public SlotUiLayout height(float value) {
        this.height = value;
        return this;
    }

    public SlotUiLayout widthPercent(float value) {
        this.widthPercent = value;
        return this;
    }

    public SlotUiLayout heightPercent(float value) {
        this.heightPercent = value;
        return this;
    }

    public SlotUiLayout maxWidth(float value) {
        this.maxWidth = value;
        return this;
    }

    public SlotUiLayout flex(float value) {
        this.flex = value;
        return this;
    }

    public SlotUiLayout paddingAll(float value) {
        this.paddingAll = value;
        return this;
    }

    public SlotUiLayout paddingHorizontal(float value) {
        this.paddingHorizontal = value;
        return this;
    }

    public SlotUiLayout paddingVertical(float value) {
        this.paddingVertical = value;
        return this;
    }

    public SlotUiLayout paddingLeft(float value) {
        this.paddingLeft = value;
        return this;
    }

    public SlotUiLayout paddingRight(float value) {
        this.paddingRight = value;
        return this;
    }

    public SlotUiLayout gapAll(float value) {
        this.gapAll = value;
        return this;
    }

    public SlotUiLayout flexDirection(FlexDirection value) {
        this.flexDirection = value;
        return this;
    }

    public SlotUiLayout alignItems(AlignItems value) {
        this.alignItems = value;
        return this;
    }

    public SlotUiLayout alignContent(AlignContent value) {
        this.alignContent = value;
        return this;
    }

    public SlotUiLayout flexWrap(FlexWrap value) {
        this.flexWrap = value;
        return this;
    }

    public SlotUiLayout positionType(PositionType value) {
        this.positionType = value;
        return this;
    }

    public SlotUiLayout left(float value) {
        this.left = value;
        return this;
    }

    public SlotUiLayout right(float value) {
        this.right = value;
        return this;
    }

    public SlotUiLayout top(float value) {
        this.top = value;
        return this;
    }

    public SlotUiLayout bottom(float value) {
        this.bottom = value;
        return this;
    }

    public boolean hasWidth() {
        return !Float.isNaN(width);
    }

    public float width() {
        return width;
    }

    public boolean hasHeight() {
        return !Float.isNaN(height);
    }

    public float height() {
        return height;
    }

    public boolean hasWidthPercent() {
        return !Float.isNaN(widthPercent);
    }

    public float widthPercent() {
        return widthPercent;
    }

    public boolean hasHeightPercent() {
        return !Float.isNaN(heightPercent);
    }

    public float heightPercent() {
        return heightPercent;
    }

    public boolean hasMaxWidth() {
        return !Float.isNaN(maxWidth);
    }

    public float maxWidth() {
        return maxWidth;
    }

    public boolean hasFlex() {
        return !Float.isNaN(flex);
    }

    public float flex() {
        return flex;
    }

    public boolean hasPaddingAll() {
        return !Float.isNaN(paddingAll);
    }

    public float paddingAll() {
        return paddingAll;
    }

    public boolean hasPaddingHorizontal() {
        return !Float.isNaN(paddingHorizontal);
    }

    public float paddingHorizontal() {
        return paddingHorizontal;
    }

    public boolean hasPaddingLeft() {
        return !Float.isNaN(paddingLeft);
    }

    public boolean hasPaddingVertical() {
        return !Float.isNaN(paddingVertical);
    }

    public float paddingVertical() {
        return paddingVertical;
    }

    public float paddingLeft() {
        return paddingLeft;
    }

    public boolean hasPaddingRight() {
        return !Float.isNaN(paddingRight);
    }

    public float paddingRight() {
        return paddingRight;
    }

    public boolean hasGapAll() {
        return !Float.isNaN(gapAll);
    }

    public float gapAll() {
        return gapAll;
    }

    public FlexDirection flexDirection() {
        return flexDirection;
    }

    public AlignItems alignItems() {
        return alignItems;
    }

    public AlignContent alignContent() {
        return alignContent;
    }

    public FlexWrap flexWrap() {
        return flexWrap;
    }

    public PositionType positionType() {
        return positionType;
    }

    public boolean hasLeft() {
        return !Float.isNaN(left);
    }

    public float left() {
        return left;
    }

    public boolean hasRight() {
        return !Float.isNaN(right);
    }

    public float right() {
        return right;
    }

    public boolean hasTop() {
        return !Float.isNaN(top);
    }

    public float top() {
        return top;
    }

    public boolean hasBottom() {
        return !Float.isNaN(bottom);
    }

    public float bottom() {
        return bottom;
    }
}
