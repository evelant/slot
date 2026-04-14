package dev.imagio.slot.client.screen;

import dev.imagio.slot.projection.InventoryViewData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.Objects;

public final class InventorySectionScreenRow extends InventoryScreenRow {
    private final InventoryViewData.Section section;
    private final int count;
    private final InventorySectionHeaderSupport headerSupport;
    private final Geometry geometry;
    private final HeaderStateFactory headerStateFactory;
    private final ClickHandler clickHandler;

    public InventorySectionScreenRow(
            String rowId,
            InventoryViewData.Section section,
            int count,
            InventorySectionHeaderSupport headerSupport,
            Geometry geometry,
            HeaderStateFactory headerStateFactory,
            ClickHandler clickHandler
    ) {
        super(rowId);
        this.section = Objects.requireNonNull(section, "section");
        this.count = Math.max(0, count);
        this.headerSupport = Objects.requireNonNull(headerSupport, "headerSupport");
        this.geometry = Objects.requireNonNull(geometry, "geometry");
        this.headerStateFactory = Objects.requireNonNull(headerStateFactory, "headerStateFactory");
        this.clickHandler = Objects.requireNonNull(clickHandler, "clickHandler");
    }

    @Override
    public String sectionId() {
        return section.id();
    }

    @Override
    public Component getNarration() {
        return Component.literal(section.label());
    }

    @Override
    public void render(
            GuiGraphics guiGraphics,
            int index,
            int y,
            int x,
            int width,
            int height,
            int mouseX,
            int mouseY,
            boolean hovered,
            float partialTick
    ) {
        headerSupport.render(
                guiGraphics,
                section,
                headerStateFactory.build(x, y, width),
                x,
                y,
                width,
                height,
                count,
                mouseX,
                mouseY
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }

        int rowLeft = geometry.rowLeft();
        int rowTop = geometry.rowTop(this);
        int rowWidth = geometry.rowWidth();
        InventorySectionHeaderSupport.SectionHeaderState headerState = headerStateFactory.build(rowLeft, rowTop, rowWidth);
        return clickHandler.handle(headerSupport.clickTarget(headerState, mouseX, mouseY), headerState);
    }

    public InventoryViewData.Section section() {
        return section;
    }

    @FunctionalInterface
    public interface Geometry {
        int rowTop(InventorySectionScreenRow row);

        default int rowLeft() {
            return 0;
        }

        default int rowWidth() {
            return 0;
        }
    }

    @FunctionalInterface
    public interface HeaderStateFactory {
        InventorySectionHeaderSupport.SectionHeaderState build(int x, int y, int width);
    }

    @FunctionalInterface
    public interface ClickHandler {
        boolean handle(
                InventorySectionHeaderSupport.SectionHeaderClickTarget target,
                InventorySectionHeaderSupport.SectionHeaderState headerState
        );
    }
}
