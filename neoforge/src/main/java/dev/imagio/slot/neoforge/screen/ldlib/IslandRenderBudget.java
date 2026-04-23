package dev.imagio.slot.neoforge.screen.ldlib;

record IslandRenderBudget(
        float titleFontPx,
        float subtitleFontPx,
        float headerHeightPx,
        float subtitleHeightPx,
        float ruleHeightPx,
        boolean showSubtitle
) {
    static IslandRenderBudget forScreenBudget(int islandScreenWidthPx) {
        int clamped = Math.max(1, islandScreenWidthPx);
        float titleFont = Math.max(8.5f, Math.min(12.5f, clamped * 0.026f));
        float subtitleFont = Math.max(6.5f, Math.min(8.5f, clamped * 0.018f));
        boolean showSubtitle = clamped >= 220;
        return new IslandRenderBudget(
                titleFont,
                subtitleFont,
                titleFont + 5.5f,
                subtitleFont + 3.5f,
                showSubtitle ? 1.5f : 1f,
                showSubtitle
        );
    }
}
