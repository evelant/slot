package dev.imagio.slot.forge.ui;

import dev.imagio.slot.forge.test.MinecraftTestBootstrap;
import dev.imagio.slot.ui.spi.SlotUiElement;
import dev.imagio.slot.ui.spi.SlotUiLayout;
import dev.imagio.slot.ui.workspace.WorkspaceUiPalette;
import dev.vfyjxf.taffy.geometry.FloatSize;
import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.AvailableSpace;
import dev.vfyjxf.taffy.tree.Layout;
import dev.vfyjxf.taffy.tree.NodeId;
import dev.vfyjxf.taffy.tree.TaffyTree;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForgeSlotUiTreeLayoutTest {
    @BeforeAll
    static void bootstrapMinecraft() throws ReflectiveOperationException {
        MinecraftTestBootstrap.bootstrapVanillaRegistries();
    }

    @Test
    void flexTextCanShrinkInsideFixedCraftRunHeader() {
        SlotUiElement header = SlotUiElement.panel(0xA0365743)
                .layout(layout -> layout
                        .width(172)
                        .height(15)
                        .paddingHorizontal(2)
                        .gapAll(2)
                        .alignItems(SlotUiLayout.AlignItems.CENTER)
                        .flexDirection(SlotUiLayout.FlexDirection.ROW));
        SlotUiElement icon = SlotUiElement.element()
                .layout(layout -> layout.width(12).height(12));
        SlotUiElement title = SlotUiElement.label(
                        "A comically long modded recipe output name that must not push controls away",
                        WorkspaceUiPalette.TEXT)
                .layout(layout -> layout.flex(1).heightPercent(100));
        SlotUiElement stage = button("Stage", 31);
        SlotUiElement minus = button("-", 12);
        SlotUiElement plus = button("+", 12);
        SlotUiElement done = button("Done", 27);

        TaffyTree tree = new TaffyTree();
        NodeId iconId = tree.newLeaf(ForgeSlotUiTree.styleFor(icon));
        NodeId titleId = textLeaf(tree, title, 300);
        NodeId stageId = textLeaf(tree, stage, 25);
        NodeId minusId = textLeaf(tree, minus, 5);
        NodeId plusId = textLeaf(tree, plus, 5);
        NodeId doneId = textLeaf(tree, done, 21);
        NodeId headerId = tree.newWithChildren(
                ForgeSlotUiTree.styleFor(header),
                List.of(iconId, titleId, stageId, minusId, plusId, doneId));

        tree.computeLayout(
                headerId,
                TaffySize.of(AvailableSpace.definite(172), AvailableSpace.definite(15)));

        Layout iconLayout = tree.getLayout(iconId);
        Layout titleLayout = tree.getLayout(titleId);
        Layout stageLayout = tree.getLayout(stageId);
        Layout doneLayout = tree.getLayout(doneId);

        assertEquals(12f, iconLayout.size().width, 0.01f);
        assertEquals(64f, titleLayout.size().width, 0.01f);
        assertEquals(31f, stageLayout.size().width, 0.01f);
        assertTrue(doneLayout.location().x + doneLayout.size().width <= 172f);
    }

    private static SlotUiElement button(String text, int width) {
        return SlotUiElement.button(text, true, WorkspaceUiPalette.ROW_DIM)
                .layout(layout -> layout
                        .width(width)
                        .height(12)
                        .paddingHorizontal(3));
    }

    private static NodeId textLeaf(TaffyTree tree, SlotUiElement element, float measuredWidth) {
        return tree.newLeafWithMeasure(
                ForgeSlotUiTree.styleFor(element),
                (knownDimensions, availableSpace) -> FloatSize.of(measuredWidth, 8));
    }
}
