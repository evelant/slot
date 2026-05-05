package dev.imagio.slot.neoforge.screen.ldlib;

import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.lowdragmc.lowdraglib2.utils.ColorUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.platform.Lighting;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

/**
 * Renders an item icon with a true alpha-blended tint that works for
 * 3D-rendered stacks (blocks) as well as 2D sprites.
 *
 * <p>Stock {@link ItemStackTexture} feeds its color through
 * {@code RenderSystem.setShaderColor} into vanilla's GUI item path. That
 * works for items routed to {@link Sheets#translucentItemSheet()}
 * ({@code entityTranslucent}, alpha-blended). Most blocks route to
 * {@link Sheets#cutoutBlockSheet()} ({@code entityCutout}, alpha-test
 * only — no blending), which silently drops the alpha and renders the
 * block as if it were carried.
 *
 * <p>Fix: wrap the {@link MultiBufferSource} {@link ItemRenderer#render}
 * writes into and rewrite {@code cutoutBlockSheet} → {@code translucentItemSheet}
 * at vertex-write time. Both render types use the same texture atlas
 * and vertex format, so the redirect costs nothing at draw time but
 * routes the block's quads through the alpha-blended pipeline. The
 * resulting fragment chain is {@code texture * vertexColor * ColorModulator}
 * with proper SRC_ALPHA / ONE_MINUS_SRC_ALPHA blending, so a
 * low-alpha shader color renders the block at true partial opacity.
 *
 * <p>2D sprite items already take the translucent path; the redirect
 * is a no-op for them, so this texture is safe to use for any stack
 * regardless of underlying model type.
 */
@OnlyIn(Dist.CLIENT)
final class GhostItemTexture extends ItemStackTexture {
    private int ghostColor = -1;

    GhostItemTexture(ItemStack stack) {
        super(stack);
    }

    @Override
    public GhostItemTexture setColor(int color) {
        super.setColor(color);
        this.ghostColor = color;
        return this;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void drawInternal(GuiGraphics graphics, float mouseX, float mouseY,
                                float x, float y, float width, float height, float partialTicks) {
        drawTranslucent(graphics, x, y, width, height, ghostColor);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void drawInternal(GUIContext context, float x, float y, float width, float height) {
        int effective = context.elementColor == -1
                ? ghostColor
                : ColorUtils.mulColor(ghostColor, context.elementColor);
        drawTranslucent(context.graphics, x, y, width, height, effective);
    }

    private void drawTranslucent(GuiGraphics graphics, float x, float y,
                                 float width, float height, int drawColor) {
        if (items == null || items.length == 0) {
            return;
        }
        ItemStack stack = items[0];
        if (stack.isEmpty()) {
            return;
        }
        graphics.flush();
        graphics.pose().pushPose();
        graphics.pose().scale(width / 16f, height / 16f, 1f);
        graphics.pose().translate(x * 16f / width, y * 16f / height, -200f);
        renderItemTranslucent(graphics, stack, drawColor);
        graphics.pose().popPose();
    }

    private static void renderItemTranslucent(GuiGraphics graphics, ItemStack stack, int color) {
        float a = ColorUtils.alpha(color);
        float r = ColorUtils.red(color);
        float g = ColorUtils.green(color);
        float b = ColorUtils.blue(color);
        RenderSystem.setShaderColor(r, g, b, a);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);

        Minecraft mc = Minecraft.getInstance();
        ItemRenderer itemRenderer = mc.getItemRenderer();
        BakedModel bakedmodel = itemRenderer.getModel(stack, null, null, 0);

        graphics.pose().pushPose();
        // Match GuiGraphics.renderItem's framing: center within the
        // 16-unit slot and flip Y so the model renders right-side up.
        graphics.pose().translate(8f, 8f, 232f);
        graphics.pose().scale(16f, -16f, 16f);

        boolean flatLighting = !bakedmodel.usesBlockLight();
        if (flatLighting) {
            Lighting.setupForFlatItems();
        }

        MultiBufferSource delegate = graphics.bufferSource();
        MultiBufferSource translucentRedirect = new MultiBufferSource() {
            @Override
            public VertexConsumer getBuffer(RenderType renderType) {
                // Solid/cutout blocks land on cutoutBlockSheet in the
                // GUI — that path is alpha-test only, so reroute their
                // vertices into translucentItemSheet which actually
                // alpha-blends. Both share BLOCK_ATLAS and the same
                // vertex format, so the rewrite is purely a transparency-
                // state swap.
                if (renderType == Sheets.cutoutBlockSheet()) {
                    return delegate.getBuffer(Sheets.translucentItemSheet());
                }
                return delegate.getBuffer(renderType);
            }
        };

        itemRenderer.render(stack, ItemDisplayContext.GUI, false,
                graphics.pose(), translucentRedirect,
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, bakedmodel);
        graphics.flush();

        if (flatLighting) {
            Lighting.setupFor3DItems();
        }
        graphics.pose().popPose();

        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        RenderSystem.depthMask(false);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
    }
}
