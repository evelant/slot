package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.workspace.WayfindingTarget;

public final class WayfindingGlowMath {
    public static final double MAX_GLOW_RADIUS = 64.0;
    public static final double LOS_TRACE_LIMIT = 32.0;
    public static final int KIT_RGB = 0xFFB347;
    public static final int PLAYER_RGB = 0x4FB8FF;
    public static final float MIN_ALPHA = 0.10f;
    public static final float MAX_ALPHA = 0.85f;

    private WayfindingGlowMath() {
    }

    public static float computePulse(long gameTime, float partialTick) {
        double phase = (gameTime + partialTick) * Math.PI / 40.0;
        return (float) ((Math.sin(phase) + 1.0) * 0.5);
    }

    public static float computeAlpha(double distance, boolean clearLineOfSight, float pulse) {
        double clamped = Math.min(Math.max(0.0, distance), MAX_GLOW_RADIUS);
        double distanceFactor = 1.0 - 0.75 * (clamped / MAX_GLOW_RADIUS);
        double losFactor = clearLineOfSight ? 1.0 : 0.5;
        double pulseFactor = 0.6 + 0.4 * Math.max(0.0f, Math.min(1.0f, pulse));
        double alpha = MAX_ALPHA * distanceFactor * losFactor * pulseFactor;
        if (alpha < MIN_ALPHA && clearLineOfSight) {
            alpha = MIN_ALPHA;
        }
        return (float) Math.max(0.0, Math.min(1.0, alpha));
    }

    public static int scopeRgb(WayfindingTarget target) {
        return target != null && target.scope() == WayfindingTarget.Scope.KIT
                ? KIT_RGB
                : PLAYER_RGB;
    }
}
