package dev.imagio.slot.forge.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.imagio.slot.forge.network.ForgeWorkspaceViewModelClientCache;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.inventory.workspace.WayfindingTarget;
import dev.imagio.slot.ui.workspace.WayfindingGlowMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;

import java.util.List;

public final class ForgeWayfindingChestGlowRenderer {
    private ForgeWayfindingChestGlowRenderer() {
    }

    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        LocalPlayer player = minecraft.player;
        if (level == null || player == null) {
            return;
        }
        SlotWorkspaceViewModel viewModel = ForgeWorkspaceViewModelClientCache.latest();
        List<WayfindingTarget> targets = viewModel == null ? List.of() : viewModel.wayfindingTargets();
        if (targets.isEmpty()) {
            return;
        }

        String currentDimension = level.dimension().location().toString();
        Vec3 cameraPos = event.getCamera().getPosition();
        float partialTick = event.getPartialTick();
        Vec3 eyePos = player.getEyePosition(partialTick);
        float pulse = WayfindingGlowMath.computePulse(level.getGameTime(), partialTick);

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());

        for (WayfindingTarget target : targets) {
            if (target == null || !currentDimension.equals(target.dimensionId())) {
                continue;
            }
            BlockPos primaryPos = BlockPos.containing(target.worldX(), target.worldY(), target.worldZ());
            double dx = primaryPos.getX() + 0.5 - eyePos.x;
            double dy = primaryPos.getY() + 0.5 - eyePos.y;
            double dz = primaryPos.getZ() + 0.5 - eyePos.z;
            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq > WayfindingGlowMath.MAX_GLOW_RADIUS * WayfindingGlowMath.MAX_GLOW_RADIUS) {
                continue;
            }
            if (!level.hasChunkAt(primaryPos)) {
                continue;
            }
            boolean clearLineOfSight = hasLineOfSight(level, player, eyePos, primaryPos);
            float alpha = WayfindingGlowMath.computeAlpha(Math.sqrt(distSq), clearLineOfSight, pulse);
            if (alpha < WayfindingGlowMath.MIN_ALPHA / 2f) {
                continue;
            }
            int rgb = WayfindingGlowMath.scopeRgb(target);
            float r = ((rgb >> 16) & 0xFF) / 255f;
            float g = ((rgb >> 8) & 0xFF) / 255f;
            float b = (rgb & 0xFF) / 255f;

            AABB box = chestAabb(level, primaryPos)
                    .move(-cameraPos.x, -cameraPos.y, -cameraPos.z);
            LevelRenderer.renderLineBox(poseStack, consumer, box, r, g, b, alpha);
        }
        bufferSource.endBatch(RenderType.lines());
    }

    private static boolean hasLineOfSight(ClientLevel level, LocalPlayer player, Vec3 eye, BlockPos primaryPos) {
        Vec3 target = Vec3.atCenterOf(primaryPos);
        Vec3 direction = target.subtract(eye);
        double length = Math.min(direction.length(), WayfindingGlowMath.LOS_TRACE_LIMIT);
        if (length <= 0.0) {
            return true;
        }
        Vec3 capped = eye.add(direction.normalize().scale(length));
        ClipContext context = new ClipContext(
                eye,
                capped,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player);
        HitResult hit = level.clip(context);
        if (hit.getType() == HitResult.Type.MISS) {
            return true;
        }
        BlockPos hitPos = BlockPos.containing(hit.getLocation());
        return hitPos.equals(primaryPos);
    }

    private static AABB chestAabb(ClientLevel level, BlockPos primaryPos) {
        AABB single = new AABB(primaryPos);
        BlockEntity blockEntity = level.getBlockEntity(primaryPos);
        if (!(blockEntity instanceof ChestBlockEntity)) {
            return single;
        }
        for (BlockPos neighbor : new BlockPos[] {
                primaryPos.north(), primaryPos.south(), primaryPos.east(), primaryPos.west()
        }) {
            BlockEntity adjacent = level.getBlockEntity(neighbor);
            if (adjacent instanceof ChestBlockEntity) {
                return single.minmax(new AABB(neighbor));
            }
        }
        return single;
    }
}
