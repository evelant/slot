package dev.imagio.slot.neoforge.client.wayfinding;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.imagio.slot.inventory.workspace.WayfindingTarget;
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
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * Layer 1 of the wayfinding plan — see
 * docs/plans/wayfinding.md § Phase 2. Draws a soft wireframe AABB on
 * every claimed chest in the current dimension that holds a missing
 * (kit-needed or desired-count gap) identity.
 *
 * <p>Color tracks {@link WayfindingTarget#scope()}; alpha pulses with
 * {@code sin(tickPhase)} and falls off with distance + line-of-sight.
 * No work happens past {@link #MAX_GLOW_RADIUS} so a base 200 blocks
 * away never costs render time.
 */
public final class WayfindingChestGlowRenderer {
    /** Don't even consider chests past this many blocks. */
    private static final double MAX_GLOW_RADIUS = 64.0;
    /** Cap LOS-trace length so a missed-trace still finishes in microseconds. */
    private static final double LOS_TRACE_LIMIT = 32.0;

    /** Amber pulse for kit-scoped targets (matches the existing kit pip). */
    private static final int KIT_RGB = 0xFFB347;
    /** Cyan-blue for player-global desired-count gaps. */
    private static final int PLAYER_RGB = 0x4FB8FF;

    private static final float MIN_ALPHA = 0.10f;
    private static final float MAX_ALPHA = 0.85f;

    private WayfindingChestGlowRenderer() {
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
        var targets = WayfindingTargetCache.targets();
        if (targets.isEmpty()) {
            return;
        }
        String currentDimension = level.dimension().location().toString();
        Vec3 cameraPos = event.getCamera().getPosition();
        Vec3 eyePos = player.getEyePosition(event.getPartialTick().getGameTimeDeltaPartialTick(false));
        long gameTime = level.getGameTime();
        float pulse = computePulse(gameTime, event.getPartialTick().getGameTimeDeltaPartialTick(false));

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());

        for (WayfindingTarget target : targets) {
            if (!currentDimension.equals(target.dimensionId())) {
                continue;
            }
            BlockPos primaryPos = new BlockPos(target.worldX(), target.worldY(), target.worldZ());
            double dx = primaryPos.getX() + 0.5 - eyePos.x;
            double dy = primaryPos.getY() + 0.5 - eyePos.y;
            double dz = primaryPos.getZ() + 0.5 - eyePos.z;
            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq > MAX_GLOW_RADIUS * MAX_GLOW_RADIUS) {
                continue;
            }
            // Skip entries whose chunk isn't loaded — the glow has nothing
            // visible to anchor against and probing block state would force
            // a chunk load.
            if (!level.hasChunkAt(primaryPos)) {
                continue;
            }
            float alpha = computeAlpha(level, player, eyePos, primaryPos, Math.sqrt(distSq), pulse);
            if (alpha < MIN_ALPHA / 2f) {
                continue;
            }
            int rgb = target.scope() == WayfindingTarget.Scope.KIT ? KIT_RGB : PLAYER_RGB;
            float r = ((rgb >> 16) & 0xFF) / 255f;
            float g = ((rgb >> 8) & 0xFF) / 255f;
            float b = (rgb & 0xFF) / 255f;

            AABB aabb = chestAabb(level, primaryPos)
                    .move(-cameraPos.x, -cameraPos.y, -cameraPos.z);
            LevelRenderer.renderLineBox(poseStack, consumer, aabb, r, g, b, alpha);
        }
        bufferSource.endBatch(RenderType.lines());
    }

    /**
     * Slow alpha pulse on a sine wave — 4-second period so the glow
     * breathes rather than blinks. Phase is shared across all targets so
     * a base full of chests pulses in unison instead of becoming a
     * disco.
     */
    private static float computePulse(long gameTime, float partialTick) {
        double t = (gameTime + partialTick) * Math.PI / 40.0;
        return (float) ((Math.sin(t) + 1.0) * 0.5);
    }

    private static float computeAlpha(
            ClientLevel level,
            LocalPlayer player,
            Vec3 eye,
            BlockPos primaryPos,
            double distance,
            float pulse
    ) {
        double clamped = Math.min(distance, MAX_GLOW_RADIUS);
        // Falloff from 1.0 at zero blocks to ~0.25 at MAX_GLOW_RADIUS.
        double distanceFactor = 1.0 - 0.75 * (clamped / MAX_GLOW_RADIUS);
        boolean clear = hasLineOfSight(level, player, eye, primaryPos);
        // Behind walls drops to half so the cue still hints at "next room
        // over" without drawing through-wall as bright as line-of-sight.
        double losFactor = clear ? 1.0 : 0.5;
        // Pulse modulates between 0.6 and 1.0 of the base so the glow
        // breathes without disappearing.
        double pulseFactor = 0.6 + 0.4 * pulse;
        double alpha = MAX_ALPHA * distanceFactor * losFactor * pulseFactor;
        if (alpha < MIN_ALPHA) {
            alpha = clear ? MIN_ALPHA : alpha;
        }
        return (float) Math.max(0.0, Math.min(1.0, alpha));
    }

    private static boolean hasLineOfSight(ClientLevel level, LocalPlayer player, Vec3 eye, BlockPos primaryPos) {
        Vec3 target = Vec3.atCenterOf(primaryPos);
        // Cap trace length so a long sight line through air doesn't probe
        // blocks beyond what we care about.
        Vec3 direction = target.subtract(eye);
        double length = Math.min(direction.length(), LOS_TRACE_LIMIT);
        if (length <= 0.0) {
            return true;
        }
        Vec3 capped = eye.add(direction.normalize().scale(length));
        // ClipContext requires a non-null entity to build its
        // EntityCollisionContext (passing null NPEs in
        // EntityCollisionContext.<init>). The local player is the
        // sensible perspective entity for a player-facing visual cue.
        ClipContext ctx = new ClipContext(
                eye, capped,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        );
        HitResult hit = level.clip(ctx);
        if (hit.getType() == HitResult.Type.MISS) {
            return true;
        }
        // If we hit a block AT the target, that's the chest itself — count
        // as line-of-sight. Otherwise something is occluding.
        BlockPos hitPos = BlockPos.containing(hit.getLocation());
        return hitPos.equals(primaryPos);
    }

    /**
     * Tight AABB on the chest block. Double chests are detected by the
     * block-entity type at the primary anchor; the renderer extends the
     * box across both halves so visually the cue spans the actual chest.
     */
    private static AABB chestAabb(ClientLevel level, BlockPos primaryPos) {
        AABB single = new AABB(primaryPos);
        BlockEntity be = level.getBlockEntity(primaryPos);
        if (!(be instanceof ChestBlockEntity)) {
            return single;
        }
        // Probe the four cardinal neighbors for a paired chest BE. Avoid
        // committing to ChestBlock state property reads — this works for
        // vanilla and for any mod that fakes a "double chest" via block
        // entity adjacency.
        for (BlockPos neighbor : new BlockPos[] {
                primaryPos.north(), primaryPos.south(),
                primaryPos.east(), primaryPos.west()
        }) {
            BlockEntity adj = level.getBlockEntity(neighbor);
            if (adj instanceof ChestBlockEntity) {
                return single.minmax(new AABB(neighbor));
            }
        }
        return single;
    }
}
