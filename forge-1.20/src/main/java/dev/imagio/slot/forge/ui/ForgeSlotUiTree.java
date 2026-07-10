package dev.imagio.slot.forge.ui;

import dev.imagio.slot.ui.spi.SlotUiElement;
import dev.imagio.slot.ui.spi.SlotUiEvent;
import dev.imagio.slot.ui.spi.SlotUiEventKind;
import dev.imagio.slot.ui.spi.SlotUiLayout;
import dev.imagio.slot.ui.spi.SlotUiTextStyle;
import dev.imagio.slot.ui.workspace.WorkspaceUiPalette;
import dev.vfyjxf.taffy.geometry.FloatSize;
import dev.vfyjxf.taffy.geometry.TaffyPoint;
import dev.vfyjxf.taffy.geometry.TaffyRect;
import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.AvailableSpace;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.FlexWrap;
import dev.vfyjxf.taffy.style.LengthPercentage;
import dev.vfyjxf.taffy.style.LengthPercentageAuto;
import dev.vfyjxf.taffy.style.Overflow;
import dev.vfyjxf.taffy.style.TaffyDimension;
import dev.vfyjxf.taffy.style.TaffyDisplay;
import dev.vfyjxf.taffy.style.TaffyPosition;
import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.tree.Layout;
import dev.vfyjxf.taffy.tree.NodeId;
import dev.vfyjxf.taffy.tree.TaffyTree;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ForgeSlotUiTree {
    public static final String SCROLL_VIEWPORT = "slot.forge.scroll_viewport";
    public static final String PRIMARY_SCROLL_VIEWPORT_ID = "slot.forge.primary_scroll_viewport";
    public static final String TASK_PANEL_SCROLL_VIEWPORT_ID = "slot.forge.task_panel_scroll";
    public static final String ICON = "slot.forge.icon";
    private static final int ROW = 0xEC24313D;
    private static final int ROW_DIM = 0x7C24313D;
    private static final int ROW_HOVER = 0xEC334354;
    private static final int SELECTED = 0xF0507E6B;
    private final Minecraft minecraft;
    private final Font font;
    private final TaffyTree taffy = new TaffyTree();
    private final Map<NodeId, Node> nodes = new HashMap<>();
    private final Map<NodeId, NodeId> parents = new HashMap<>();
    private final List<Node> tickNodes = new ArrayList<>();
    private NodeId rootId;
    private Node lastMouseDown;
    private Node hovered;
    private float computedWidth = Float.NaN;
    private float computedHeight = Float.NaN;

    public enum Icon {
        GATHER,
        DEPOSIT,
        VANILLA_GRID
    }

    private ForgeSlotUiTree(Minecraft minecraft) {
        this.minecraft = minecraft;
        this.font = minecraft.font;
    }

    public static ForgeSlotUiTree build(Minecraft minecraft, SlotUiElement root) {
        ForgeSlotUiTree tree = new ForgeSlotUiTree(minecraft);
        tree.rootId = tree.buildNode(root == null ? SlotUiElement.element() : root).id;
        return tree;
    }

    public void compute(float width, float height) {
        if (rootId == null) {
            return;
        }
        if (Float.compare(computedWidth, width) == 0 && Float.compare(computedHeight, height) == 0) {
            return;
        }
        taffy.computeLayout(rootId, TaffySize.of(
                AvailableSpace.definite(width),
                AvailableSpace.definite(height)));
        for (Node node : nodes.values()) {
            node.refreshContentHeight();
        }
        computedWidth = width;
        computedHeight = height;
    }

    public float scrollY() {
        Node node = firstScrollableNode();
        return node == null ? 0f : node.scrollY;
    }

    public float scrollY(String elementId) {
        Node node = scrollableNodeByElementId(elementId);
        return node == null ? 0f : node.scrollY;
    }

    public float maxScrollY() {
        Node node = firstScrollableNode();
        return maxScrollY(node);
    }

    public float maxScrollY(String elementId) {
        return maxScrollY(scrollableNodeByElementId(elementId));
    }

    public void setScrollY(float scrollY) {
        Node node = firstScrollableNode();
        if (node == null) {
            return;
        }
        node.scrollY = clamp(scrollY, 0f, maxScrollY(node));
    }

    public void setScrollY(String elementId, float scrollY) {
        Node node = scrollableNodeByElementId(elementId);
        if (node == null) {
            return;
        }
        node.scrollY = clamp(scrollY, 0f, maxScrollY(node));
    }

    public void scrollToFraction(float fraction) {
        Node node = firstScrollableNode();
        if (node == null) {
            return;
        }
        Layout layout = taffy.getLayout(node.id);
        float max = maxScrollY(node);
        node.scrollY = clamp(max * clamp(fraction, 0f, 1f), 0f, max);
    }

    public boolean hasActivePointerGesture() {
        return lastMouseDown != null;
    }

    public <T> T attachmentAt(double mouseX, double mouseY, String key, Class<T> type) {
        Node node = hitTest(mouseX, mouseY);
        while (node != null) {
            T value = node.model.attachment(key, type);
            if (value != null) {
                return value;
            }
            node = parent(node);
        }
        return null;
    }

    public boolean scrollToElementId(String elementId) {
        if (elementId == null || elementId.isBlank()) {
            return false;
        }
        Node scroll = firstScrollableNode();
        Node target = nodeByElementId(elementId);
        if (scroll == null || target == null) {
            return false;
        }
        float contentY = 0f;
        Node current = target;
        while (current != null && current != scroll) {
            Layout layout = taffy.getLayout(current.id);
            contentY += layout.location().y;
            current = parent(current);
        }
        if (current != scroll) {
            return false;
        }
        Layout scrollLayout = taffy.getLayout(scroll.id);
        float max = Math.max(0f, scroll.contentHeight - scrollLayout.size().height);
        scroll.scrollY = clamp(contentY, 0f, max);
        return true;
    }

    public boolean isElementOffscreen(String elementId) {
        if (elementId == null || elementId.isBlank()) {
            return false;
        }
        Node scroll = firstScrollableNode();
        Node target = nodeByElementId(elementId);
        if (scroll == null || target == null) {
            return false;
        }
        float contentY = 0f;
        Node current = target;
        while (current != null && current != scroll) {
            Layout layout = taffy.getLayout(current.id);
            contentY += layout.location().y;
            current = parent(current);
        }
        if (current != scroll) {
            return false;
        }
        Layout targetLayout = taffy.getLayout(target.id);
        Layout scrollLayout = taffy.getLayout(scroll.id);
        float contentBottom = contentY + targetLayout.size().height;
        float viewportTop = scroll.scrollY;
        float viewportBottom = viewportTop + scrollLayout.size().height;
        return contentBottom <= viewportTop || contentY >= viewportBottom;
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY) {
        if (rootId == null) {
            return;
        }
        updateHover(mouseX, mouseY);
        renderNode(graphics, rootId, 0f, 0f, new ArrayDeque<>());
    }

    public void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (rootId == null) {
            return;
        }
        updateHover(mouseX, mouseY);
        TooltipContent tooltip = tooltipFor(hovered);
        if (tooltip == null) {
            return;
        }
        if (!tooltip.stack().isEmpty()) {
            List<Component> lines = tooltipLines(tooltip.stack(), tooltip.lines());
            graphics.renderTooltip(
                    font,
                    lines,
                    tooltip.stack().getTooltipImage(),
                    tooltip.stack(),
                    mouseX,
                    mouseY);
            return;
        }
        if (!tooltip.lines().isEmpty()) {
            graphics.renderComponentTooltip(font, tooltip.lines(), mouseX, mouseY);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, boolean shiftDown) {
        Node hit = hitTest(mouseX, mouseY);
        if (hit == null) {
            return false;
        }
        lastMouseDown = hit;
        bubble(hit, new SlotUiEvent(SlotUiEventKind.MOUSE_DOWN, button, (float) mouseX, (float) mouseY, shiftDown));
        return true;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button, boolean shiftDown) {
        Node hit = hitTest(mouseX, mouseY);
        if (hit == null) {
            lastMouseDown = null;
            return false;
        }
        if (lastMouseDown != null && sameOrDescendant(hit, lastMouseDown)) {
            bubble(hit, new SlotUiEvent(SlotUiEventKind.CLICK, button, (float) mouseX, (float) mouseY, shiftDown));
        }
        lastMouseDown = null;
        return true;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta, float step, boolean shiftDown) {
        Node hit = hitTest(mouseX, mouseY);
        if (hit != null) {
            SlotUiEvent event = new SlotUiEvent(
                    SlotUiEventKind.MOUSE_WHEEL,
                    0,
                    (float) mouseX,
                    (float) mouseY,
                    shiftDown,
                    Screen.hasControlDown(),
                    (float) delta);
            bubble(hit, event);
            if (event.propagationStopped()) {
                return true;
            }
        }
        Node scroll = scrollAncestor(hit);
        if (scroll == null) {
            return false;
        }
        Layout layout = taffy.getLayout(scroll.id);
        float max = Math.max(0f, scroll.contentHeight - layout.size().height);
        scroll.scrollY = clamp(scroll.scrollY - (float) delta * step, 0f, max);
        return true;
    }

    public void tick() {
        if (tickNodes.isEmpty()) {
            return;
        }
        SlotUiEvent event = new SlotUiEvent(SlotUiEventKind.TICK, 0, 0, 0, false);
        for (Node node : tickNodes) {
            node.model.dispatch(event);
        }
    }

    private Node buildNode(SlotUiElement model) {
        List<Node> childNodes = new ArrayList<>();
        List<NodeId> childIds = new ArrayList<>();
        for (SlotUiElement child : model.children()) {
            Node childNode = buildNode(child);
            childNodes.add(childNode);
            childIds.add(childNode.id);
        }

        TaffyStyle style = styleFor(model);
        NodeId id;
        if (childIds.isEmpty() && measuresText(model)) {
            id = taffy.newLeafWithMeasure(style, (knownDimensions, availableSpace) -> measureText(model));
        } else if (childIds.isEmpty()) {
            id = taffy.newLeaf(style);
        } else {
            id = taffy.newWithChildren(style, childIds);
        }
        Node node = new Node(id, model);
        nodes.put(id, node);
        if (model.hasEventBindings(SlotUiEventKind.TICK)) {
            tickNodes.add(node);
        }
        for (Node child : childNodes) {
            parents.put(child.id, id);
        }
        return node;
    }

    static TaffyStyle styleFor(SlotUiElement model) {
        SlotUiLayout layout = model.layout();
        TaffyStyle style = new TaffyStyle();
        style.display = TaffyDisplay.FLEX;
        style.size = TaffySize.of(
                layout.hasWidthPercent() ? TaffyDimension.percent(layout.widthPercent() / 100f)
                        : layout.hasWidth() ? TaffyDimension.length(layout.width()) : TaffyDimension.AUTO,
                layout.hasHeightPercent() ? TaffyDimension.percent(layout.heightPercent() / 100f)
                        : layout.hasHeight() ? TaffyDimension.length(layout.height()) : TaffyDimension.AUTO);
        if (layout.hasMaxWidth()) {
            style.maxSize = TaffySize.of(TaffyDimension.length(layout.maxWidth()), TaffyDimension.AUTO);
        }
        if (layout.hasFlex()) {
            style.flex = layout.flex();
            style.flexGrow = layout.flex();
            style.flexShrink = 1f;
            style.flexBasis = TaffyDimension.ZERO;
            style.minSize = TaffySize.of(TaffyDimension.ZERO, TaffyDimension.ZERO);
        }
        style.padding = paddingFor(layout);
        if (layout.hasGapAll()) {
            LengthPercentage gap = LengthPercentage.length(layout.gapAll());
            style.gap = TaffySize.of(gap, gap);
        }
        if (layout.flexDirection() != null) {
            style.flexDirection = map(layout.flexDirection());
        }
        if (layout.alignItems() != null) {
            style.alignItems = map(layout.alignItems());
        }
        if (layout.alignContent() != null) {
            style.alignContent = map(layout.alignContent());
        }
        if (layout.flexWrap() != null) {
            style.flexWrap = map(layout.flexWrap());
        }
        if (layout.positionType() != null) {
            style.position = map(layout.positionType());
        }
        style.inset = insetFor(layout);
        if (model.hasAttachment(SCROLL_VIEWPORT)) {
            style.overflow = new TaffyPoint<>(Overflow.HIDDEN, Overflow.HIDDEN);
        }
        return style;
    }

    private static TaffyRect<LengthPercentage> paddingFor(SlotUiLayout layout) {
        float left = 0f;
        float right = 0f;
        float top = 0f;
        float bottom = 0f;
        if (layout.hasPaddingAll()) {
            left = right = top = bottom = layout.paddingAll();
        }
        if (layout.hasPaddingHorizontal()) {
            left = right = layout.paddingHorizontal();
        }
        if (layout.hasPaddingVertical()) {
            top = bottom = layout.paddingVertical();
        }
        if (layout.hasPaddingLeft()) {
            left = layout.paddingLeft();
        }
        if (layout.hasPaddingRight()) {
            right = layout.paddingRight();
        }
        return TaffyRect.ltrb(
                LengthPercentage.length(left),
                LengthPercentage.length(top),
                LengthPercentage.length(right),
                LengthPercentage.length(bottom));
    }

    private static TaffyRect<LengthPercentageAuto> insetFor(SlotUiLayout layout) {
        LengthPercentageAuto left = LengthPercentageAuto.AUTO;
        LengthPercentageAuto right = LengthPercentageAuto.AUTO;
        LengthPercentageAuto top = LengthPercentageAuto.AUTO;
        LengthPercentageAuto bottom = LengthPercentageAuto.AUTO;
        if (layout.hasLeft()) {
            left = LengthPercentageAuto.length(layout.left());
        }
        if (layout.hasRight()) {
            right = LengthPercentageAuto.length(layout.right());
        }
        if (layout.hasTop()) {
            top = LengthPercentageAuto.length(layout.top());
        }
        if (layout.hasBottom()) {
            bottom = LengthPercentageAuto.length(layout.bottom());
        }
        return TaffyRect.ltrb(left, top, right, bottom);
    }

    private void renderNode(
            GuiGraphics graphics,
            NodeId id,
            float originX,
            float originY,
            ArrayDeque<int[]> scissors
    ) {
        Node node = nodes.get(id);
        Layout layout = taffy.getLayout(id);
        float x = originX + layout.location().x;
        float y = originY + layout.location().y;
        float width = layout.size().width;
        float height = layout.size().height;
        if (outsideCurrentScissor(scissors, x, y, width, height)) {
            return;
        }

        graphics.pose().pushPose();
        int zIndex = node.zIndex();
        if (zIndex != 0) {
            graphics.pose().translate(0, 0, zIndex);
        }

        fill(graphics, x, y, width, height, backgroundColor(node));
        if (node.scrollable()) {
            pushScissor(graphics, scissors, x, y, width, height);
        }

        float childOriginY = node.scrollable() ? y - node.scrollY : y;
        for (NodeId child : sortedChildren(id)) {
            renderNode(graphics, child, x, childOriginY, scissors);
        }

        renderText(graphics, node, x, y, width, height);
        renderIcon(graphics, node, x, y, width, height);
        renderFluid(graphics, node, x, y, width, height);
        renderItem(graphics, node, x, y, width, height);
        fill(graphics, x, y, width, height, node.model.overlayColor());

        if (node.scrollable()) {
            popScissor(graphics, scissors);
            renderScrollbar(graphics, node, x, y, width, height);
        }
        graphics.pose().popPose();
    }

    private void renderText(GuiGraphics graphics, Node node, float x, float y, float width, float height) {
        if (!(node.model.kind() == SlotUiElement.Kind.LABEL
                || (node.model.kind() == SlotUiElement.Kind.BUTTON && node.model.buttonHasText()))) {
            return;
        }
        String text = node.model.text();
        if (text == null || text.isBlank()) {
            return;
        }
        if (width <= 0f || height <= 0f) {
            return;
        }
        SlotUiTextStyle style = node.model.textStyle();
        float scale = textScale(style);
        Component component = uiText(truncateText(text, width, scale));
        if (component.getString().isBlank()) {
            return;
        }
        int textWidth = Math.round(font.width(component) * scale);
        int textHeight = Math.round(font.lineHeight * scale);
        float drawX = x;
        if (style.horizontal() == SlotUiTextStyle.Horizontal.CENTER) {
            drawX = x + (width - textWidth) / 2f;
        } else if (style.horizontal() == SlotUiTextStyle.Horizontal.RIGHT) {
            drawX = x + width - textWidth;
        }
        float drawY = y;
        if (style.vertical() == SlotUiTextStyle.Vertical.CENTER) {
            drawY = y + (height - textHeight) / 2f;
            if (style.fontSize() <= 6.0f && height <= 8.0f && node.model.backgroundColor() != null) {
                drawY += 1.0f;
            }
        } else if (style.vertical() == SlotUiTextStyle.Vertical.BOTTOM) {
            drawY = y + height - textHeight;
        }
        drawX = Math.round(drawX);
        drawY = Math.round(drawY);
        graphics.pose().pushPose();
        graphics.pose().translate(drawX, drawY, 0);
        graphics.pose().scale(scale, scale, 1f);
        graphics.drawString(font, component, 0, 0, style.color(), style.shadow());
        graphics.pose().popPose();
    }

    private String truncateText(String text, float width, float scale) {
        if (text == null || text.isBlank()) {
            return "";
        }
        Component component = uiText(text);
        if (font.width(component) * scale <= width) {
            return text;
        }
        String suffix = "...";
        if (font.width(uiText(suffix)) * scale > width) {
            return "";
        }
        StringBuilder builder = new StringBuilder(text);
        while (!builder.isEmpty() && font.width(uiText(builder + suffix)) * scale > width) {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.isEmpty() ? suffix : builder + suffix;
    }

    private void renderItem(GuiGraphics graphics, Node node, float x, float y, float width, float height) {
        if (node.model.kind() != SlotUiElement.Kind.ITEM_ICON) {
            return;
        }
        ItemStack stack = node.model.itemStackView();
        if (stack.isEmpty()) {
            return;
        }
        int size = Math.max(1, Math.round(Math.min(width, height)));
        float scale = size / 16f;
        int drawX = Math.round(x + (width - size) / 2f);
        int drawY = Math.round(y + (height - size) / 2f);
        graphics.pose().pushPose();
        graphics.pose().translate(drawX, drawY, 0);
        graphics.pose().scale(scale, scale, 1f);
        graphics.renderItem(stack, 0, 0);
        if (node.model.renderVanillaCount()) {
            graphics.renderItemDecorations(font, stack, 0, 0);
        }
        graphics.pose().popPose();
        if (!node.model.itemCarried()) {
            renderGhostItemOverlay(graphics, drawX, drawY, size);
        }
    }

    private void renderFluid(GuiGraphics graphics, Node node, float x, float y, float width, float height) {
        if (node.model.kind() != SlotUiElement.Kind.FLUID_ICON) {
            return;
        }
        Fluid fluid = resolveFluid(node.model.fluidId());
        if (fluid == null || fluid == Fluids.EMPTY) {
            return;
        }
        FluidStack stack = new FluidStack(fluid, 1000);
        TextureAtlasSprite sprite = fluidStillSprite(stack);
        int size = Math.max(1, Math.round(Math.min(width, height)));
        int drawX = Math.round(x + (width - size) / 2f);
        int drawY = Math.round(y + (height - size) / 2f);
        int tint = fluidTint(stack);
        float alpha = ((tint >>> 24) & 0xFF) / 255f;
        float red = ((tint >>> 16) & 0xFF) / 255f;
        float green = ((tint >>> 8) & 0xFF) / 255f;
        float blue = (tint & 0xFF) / 255f;
        graphics.blit(drawX, drawY, 0, size, size, sprite, red, green, blue, alpha);
    }

    private Fluid resolveFluid(String fluidId) {
        if (fluidId == null || fluidId.isBlank()) {
            return Fluids.EMPTY;
        }
        ResourceLocation location = ResourceLocation.tryParse(fluidId);
        return location == null ? Fluids.EMPTY : BuiltInRegistries.FLUID.get(location);
    }

    private TextureAtlasSprite fluidStillSprite(FluidStack stack) {
        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(stack.getFluid());
        ResourceLocation texture = extensions.getStillTexture(stack);
        if (texture == null) {
            texture = extensions.getStillTexture();
        }
        if (texture == null) {
            texture = MissingTextureAtlasSprite.getLocation();
        }
        return minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texture);
    }

    private static int fluidTint(FluidStack stack) {
        int tint = IClientFluidTypeExtensions.of(stack.getFluid()).getTintColor(stack);
        return (tint >>> 24) == 0 ? tint | 0xFF000000 : tint;
    }

    private void renderGhostItemOverlay(GuiGraphics graphics, int x, int y, int size) {
        int right = x + size;
        int bottom = y + size;
        int edge = size >= 24 ? 2 : 1;
        int dash = Math.max(3, size / 4);
        graphics.fill(x, y, right, bottom, 0xC40A1016);
        graphics.fill(x, y, right, y + edge, 0xE0809ACB);
        graphics.fill(x, bottom - edge, right, bottom, 0xE0809ACB);
        graphics.fill(x, y, x + edge, bottom, 0xE0809ACB);
        graphics.fill(right - edge, y, right, bottom, 0xE0809ACB);
        for (int offset = -size; offset < size; offset += dash) {
            int startX = x + Math.max(0, offset);
            int startY = y + Math.max(0, -offset);
            int length = Math.min(size - Math.max(0, offset), size - Math.max(0, -offset));
            if (length <= 0) {
                continue;
            }
            int hatch = Math.min(Math.max(2, size / 5), length);
            graphics.fill(startX, startY, startX + hatch, startY + edge, 0xAA809ACB);
        }
        int tab = Math.max(5, size / 3);
        graphics.fill(right - tab, y, right, y + edge, 0xF07AC7A7);
        graphics.fill(right - edge, y, right, y + tab, 0xF07AC7A7);
    }

    private void renderIcon(GuiGraphics graphics, Node node, float x, float y, float width, float height) {
        Icon icon = node.model.attachment(ICON, Icon.class);
        if (icon == null) {
            return;
        }
        int centerX = Math.round(x + width / 2f);
        int centerY = Math.round(y + height / 2f);
        int color = node.model.textStyle().color();
        switch (icon) {
            case GATHER -> renderArrowIcon(graphics, centerX, centerY, color, false);
            case DEPOSIT -> renderArrowIcon(graphics, centerX, centerY, color, true);
            case VANILLA_GRID -> renderGridIcon(graphics, centerX, centerY, color);
        }
    }

    private void renderArrowIcon(GuiGraphics graphics, int centerX, int centerY, int color, boolean up) {
        if (up) {
            graphics.fill(centerX - 1, centerY - 2, centerX + 1, centerY + 5, color);
            graphics.fill(centerX - 4, centerY - 3, centerX + 4, centerY - 2, color);
            graphics.fill(centerX - 3, centerY - 4, centerX + 3, centerY - 3, color);
            graphics.fill(centerX - 2, centerY - 5, centerX + 2, centerY - 4, color);
        } else {
            graphics.fill(centerX - 1, centerY - 5, centerX + 1, centerY + 2, color);
            graphics.fill(centerX - 4, centerY + 2, centerX + 4, centerY + 3, color);
            graphics.fill(centerX - 3, centerY + 3, centerX + 3, centerY + 4, color);
            graphics.fill(centerX - 2, centerY + 4, centerX + 2, centerY + 5, color);
        }
        graphics.fill(centerX - 5, centerY + 5, centerX + 5, centerY + 6, color);
    }

    private void renderGridIcon(GuiGraphics graphics, int centerX, int centerY, int color) {
        int left = centerX - 5;
        int top = centerY - 5;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int cellX = left + col * 4;
                int cellY = top + row * 4;
                graphics.fill(cellX, cellY, cellX + 2, cellY + 2, color);
            }
        }
    }

    private void renderScrollbar(GuiGraphics graphics, Node node, float x, float y, float width, float height) {
        if (node.contentHeight <= height + 0.5f) {
            return;
        }
        int trackX = Math.round(x + width - 3);
        int trackTop = Math.round(y + 2);
        int trackBottom = Math.round(y + height - 2);
        int trackHeight = Math.max(1, trackBottom - trackTop);
        float thumbHeight = Math.max(10f, trackHeight * (height / node.contentHeight));
        float maxScroll = Math.max(1f, node.contentHeight - height);
        float thumbY = trackTop + (trackHeight - thumbHeight) * (node.scrollY / maxScroll);
        graphics.fill(trackX, trackTop, trackX + 2, trackBottom, 0x5024313D);
        graphics.fill(trackX, Math.round(thumbY), trackX + 2, Math.round(thumbY + thumbHeight), 0xB0A0AAB3);
    }

    private void updateHover(int mouseX, int mouseY) {
        Node hit = hitTest(mouseX, mouseY);
        if (hit == hovered) {
            return;
        }
        if (hovered != null) {
            hovered.model.dispatch(new SlotUiEvent(SlotUiEventKind.MOUSE_LEAVE, 0, mouseX, mouseY, false));
        }
        hovered = hit;
        if (hovered != null) {
            hovered.model.dispatch(new SlotUiEvent(SlotUiEventKind.MOUSE_ENTER, 0, mouseX, mouseY, false));
        }
    }

    private Node hitTest(double mouseX, double mouseY) {
        if (rootId == null) {
            return null;
        }
        return hitNode(rootId, 0f, 0f, (float) mouseX, (float) mouseY);
    }

    private Node hitNode(NodeId id, float originX, float originY, float mouseX, float mouseY) {
        Node node = nodes.get(id);
        Layout layout = taffy.getLayout(id);
        float x = originX + layout.location().x;
        float y = originY + layout.location().y;
        float width = layout.size().width;
        float height = layout.size().height;
        if (mouseX < x || mouseY < y || mouseX >= x + width || mouseY >= y + height) {
            return null;
        }
        float childOriginY = node.scrollable() ? y - node.scrollY : y;
        List<NodeId> children = sortedChildren(id);
        for (int index = children.size() - 1; index >= 0; index--) {
            Node childHit = hitNode(children.get(index), x, childOriginY, mouseX, mouseY);
            if (childHit != null) {
                return childHit;
            }
        }
        return node.model.allowHitTest() ? node : null;
    }

    private void bubble(Node target, SlotUiEvent event) {
        Node current = target;
        while (current != null && !event.propagationStopped()) {
            current.model.dispatch(event);
            current = parent(current);
        }
    }

    private Node parent(Node node) {
        NodeId parent = parents.get(node.id);
        return parent == null ? null : nodes.get(parent);
    }

    private boolean sameOrDescendant(Node node, Node expectedAncestor) {
        Node current = node;
        while (current != null) {
            if (current == expectedAncestor) {
                return true;
            }
            current = parent(current);
        }
        return false;
    }

    private Node scrollAncestor(Node node) {
        Node current = node;
        while (current != null) {
            if (current.scrollable()) {
                return current;
            }
            current = parent(current);
        }
        return null;
    }

    private TooltipContent tooltipFor(Node node) {
        Node current = node;
        while (current != null) {
            ItemStack stack = current.model.tooltipStackView();
            List<Component> lines = current.model.tooltipLines();
            if (!stack.isEmpty()) {
                return new TooltipContent(stack, lines);
            }
            if (!lines.isEmpty()) {
                return new TooltipContent(ItemStack.EMPTY, lines);
            }
            current = parent(current);
        }
        return null;
    }

    private List<Component> tooltipLines(ItemStack stack, List<Component> extraLines) {
        List<Component> base = Screen.getTooltipFromItem(minecraft, stack);
        if (extraLines == null || extraLines.isEmpty()) {
            return base;
        }
        ArrayList<Component> lines = new ArrayList<>(base.size() + extraLines.size());
        lines.addAll(base);
        for (Component line : extraLines) {
            lines.add(styleSlotTooltipLine(line));
        }
        return List.copyOf(lines);
    }

    private static Component styleSlotTooltipLine(Component line) {
        if (line == null) {
            return Component.empty();
        }
        String text = line.getString();
        if (text == null || text.isBlank()) {
            return Component.empty();
        }
        int color = slotTooltipColor(text);
        if (color == 0) {
            return line;
        }
        return Component.literal(text)
                .withStyle(style -> style.withColor(TextColor.fromRgb(color & 0x00FFFFFF)));
    }

    private static int slotTooltipColor(String text) {
        if ("SLOT".equals(text)) {
            return WorkspaceUiPalette.ACCENT;
        }
        if (text.startsWith("Desired target")) {
            return WorkspaceUiPalette.COUNT_BADGE_DESIRED;
        }
        if (text.startsWith("Wanted target")) {
            return WorkspaceUiPalette.COUNT_BADGE_WANTED;
        }
        if (text.startsWith("Nearby stored") || text.startsWith("Nearby route")) {
            return WorkspaceUiPalette.ACCENT;
        }
        if (text.startsWith("Stored elsewhere")) {
            return WorkspaceUiPalette.MUTED;
        }
        if (text.startsWith("Workflow target")) {
            return WorkspaceUiPalette.COUNT_BADGE_WORKFLOW;
        }
        if (text.startsWith("Container")) {
            return 0xFF8DB7D6;
        }
        if (text.startsWith("Carried count")) {
            return WorkspaceUiPalette.TEXT;
        }
        if (text.startsWith("Contextual score")) {
            return WorkspaceUiPalette.ACCENT;
        }
        if (text.startsWith("  +")) {
            return 0xFF7AC7A7;
        }
        if (text.startsWith("  -")) {
            return 0xFFFFD166;
        }
        return 0;
    }

    private Node firstScrollableNode() {
        Node primary = nodeByElementId(PRIMARY_SCROLL_VIEWPORT_ID);
        if (primary != null && primary.scrollable()) {
            return primary;
        }
        for (Node node : nodes.values()) {
            if (node.scrollable()) {
                return node;
            }
        }
        return null;
    }

    private Node scrollableNodeByElementId(String elementId) {
        Node node = nodeByElementId(elementId);
        return node != null && node.scrollable() ? node : null;
    }

    private float maxScrollY(Node node) {
        if (node == null) {
            return 0f;
        }
        Layout layout = taffy.getLayout(node.id);
        return Math.max(0f, node.contentHeight - layout.size().height);
    }

    private Node nodeByElementId(String elementId) {
        for (Node node : nodes.values()) {
            if (elementId.equals(node.model.id())) {
                return node;
            }
        }
        return null;
    }

    private List<NodeId> sortedChildren(NodeId id) {
        List<NodeId> children = taffy.getChildren(id);
        if (children.size() < 2) {
            return children;
        }
        int previousZ = Integer.MIN_VALUE;
        boolean alreadySorted = true;
        for (NodeId child : children) {
            int zIndex = nodes.get(child).zIndex();
            if (zIndex < previousZ) {
                alreadySorted = false;
                break;
            }
            previousZ = zIndex;
        }
        if (alreadySorted) {
            return children;
        }
        ArrayList<NodeId> sorted = new ArrayList<>(children);
        sorted.sort(Comparator.comparingInt(child -> nodes.get(child).zIndex()));
        return sorted;
    }

    private FloatSize measureText(SlotUiElement model) {
        String text = model.text() == null ? "" : model.text();
        float scale = textScale(model.textStyle());
        return FloatSize.of(
                Math.max(1f, font.width(uiText(text)) * scale),
                Math.max(1f, font.lineHeight * scale));
    }

    private static Component uiText(String text) {
        return Component.literal(text == null ? "" : text)
                .withStyle(style -> style.withFont(Minecraft.DEFAULT_FONT));
    }

    private boolean measuresText(SlotUiElement model) {
        if (model.kind() == SlotUiElement.Kind.LABEL) {
            return true;
        }
        return model.kind() == SlotUiElement.Kind.BUTTON && model.buttonHasText();
    }

    private float textScale(SlotUiTextStyle style) {
        return Math.max(0.5f, style.fontSize() / 8f);
    }

    private Integer backgroundColor(Node node) {
        if (node.model.kind() == SlotUiElement.Kind.BUTTON) {
            int color = node.model.buttonColor();
            if (!node.model.buttonActive()) {
                return color;
            }
            if (lastMouseDown != null && sameOrDescendant(hovered, node) && sameOrDescendant(hovered, lastMouseDown)) {
                return SELECTED;
            }
            if (sameOrDescendant(hovered, node)) {
                return hoverColor(color);
            }
            return color;
        }
        return node.model.backgroundColor();
    }

    private static int hoverColor(int color) {
        if (color == ROW_DIM) {
            return ROW;
        }
        int baseAlpha = (color >>> 24) & 0xFF;
        if (baseAlpha < 0x80) {
            return (baseAlpha << 24) | (ROW_HOVER & 0x00FFFFFF);
        }
        return ROW_HOVER;
    }

    private void fill(GuiGraphics graphics, float x, float y, float width, float height, Integer color) {
        if (color == null || (color >>> 24) == 0) {
            return;
        }
        graphics.fill(
                Math.round(x),
                Math.round(y),
                Math.round(x + width),
                Math.round(y + height),
                color);
    }

    private boolean outsideCurrentScissor(
            ArrayDeque<int[]> scissors,
            float x,
            float y,
            float width,
            float height
    ) {
        if (scissors.isEmpty()) {
            return false;
        }
        int[] clip = scissors.peek();
        float right = x + width;
        float bottom = y + height;
        return right <= clip[0]
                || bottom <= clip[1]
                || x >= clip[2]
                || y >= clip[3];
    }

    private void pushScissor(
            GuiGraphics graphics,
            ArrayDeque<int[]> scissors,
            float x,
            float y,
            float width,
            float height
    ) {
        int left = Math.round(x);
        int top = Math.round(y);
        int right = Math.round(x + width);
        int bottom = Math.round(y + height);
        if (!scissors.isEmpty()) {
            int[] parent = scissors.peek();
            left = Math.max(left, parent[0]);
            top = Math.max(top, parent[1]);
            right = Math.min(right, parent[2]);
            bottom = Math.min(bottom, parent[3]);
        }
        int[] next = {left, top, Math.max(left, right), Math.max(top, bottom)};
        scissors.push(next);
        graphics.enableScissor(next[0], next[1], next[2], next[3]);
    }

    private void popScissor(GuiGraphics graphics, ArrayDeque<int[]> scissors) {
        scissors.pop();
        graphics.disableScissor();
        if (!scissors.isEmpty()) {
            int[] next = scissors.peek();
            graphics.enableScissor(next[0], next[1], next[2], next[3]);
        }
    }

    private static FlexDirection map(SlotUiLayout.FlexDirection value) {
        return value == SlotUiLayout.FlexDirection.ROW
                ? FlexDirection.ROW
                : FlexDirection.COLUMN;
    }

    private static AlignItems map(SlotUiLayout.AlignItems value) {
        if (value == SlotUiLayout.AlignItems.CENTER) {
            return AlignItems.CENTER;
        }
        if (value == SlotUiLayout.AlignItems.FLEX_START) {
            return AlignItems.FLEX_START;
        }
        return AlignItems.STRETCH;
    }

    private static AlignContent map(SlotUiLayout.AlignContent value) {
        if (value == SlotUiLayout.AlignContent.SPACE_BETWEEN) {
            return AlignContent.SPACE_BETWEEN;
        }
        return AlignContent.FLEX_START;
    }

    private static FlexWrap map(SlotUiLayout.FlexWrap value) {
        return value == SlotUiLayout.FlexWrap.WRAP
                ? FlexWrap.WRAP
                : FlexWrap.NO_WRAP;
    }

    private static TaffyPosition map(SlotUiLayout.PositionType value) {
        return value == SlotUiLayout.PositionType.ABSOLUTE
                ? TaffyPosition.ABSOLUTE
                : TaffyPosition.RELATIVE;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private final class Node {
        final NodeId id;
        final SlotUiElement model;
        float scrollY;
        float contentHeight;

        Node(NodeId id, SlotUiElement model) {
            this.id = id;
            this.model = model;
        }

        boolean scrollable() {
            return model.hasAttachment(SCROLL_VIEWPORT);
        }

        int zIndex() {
            return model.zIndex() == null ? 0 : model.zIndex();
        }

        void refreshContentHeight() {
            float max = 0f;
            for (NodeId childId : taffy.getChildren(id)) {
                Layout child = taffy.getLayout(childId);
                max = Math.max(max, child.location().y + child.size().height);
            }
            contentHeight = max;
            if (scrollable()) {
                float ownHeight = taffy.getLayout(id).size().height;
                scrollY = clamp(scrollY, 0f, Math.max(0f, contentHeight - ownHeight));
            }
        }
    }

    private record TooltipContent(ItemStack stack, List<Component> lines) {
    }
}
