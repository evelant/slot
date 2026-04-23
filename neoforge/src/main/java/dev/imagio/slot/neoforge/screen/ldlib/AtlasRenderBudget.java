package dev.imagio.slot.neoforge.screen.ldlib;

record AtlasRenderBudget(
        DisclosureLevel level,
        int cellBudgetPx,
        float shellPx,
        float iconPx,
        float pipPx,
        float primaryFontPx,
        float secondaryFontPx,
        float primaryLineHeightPx,
        float secondaryLineHeightPx,
        int primaryMaxChars,
        int secondaryMaxChars
) {
    static AtlasRenderBudget forScreenBudget(int cellBudgetPx) {
        int clamped = Math.max(1, cellBudgetPx);
        if (clamped >= WorkspaceTheme.DETAIL_CELL_PX) {
            return new AtlasRenderBudget(
                    DisclosureLevel.DETAIL,
                    clamped,
                    clamp(clamped * 0.42f, 30f, 54f),
                    clamp(clamped * 0.36f, 24f, 48f),
                    4f,
                    clamp(clamped * 0.078f, 7.75f, 9.0f),
                    clamp(clamped * 0.062f, 6.75f, 7.5f),
                    clamp(clamped * 0.100f, 11.0f, 13.0f),
                    clamp(clamped * 0.076f, 8.5f, 10.0f),
                    38,
                    26
            );
        }
        if (clamped >= WorkspaceTheme.INSPECT_CELL_PX) {
            return new AtlasRenderBudget(
                    DisclosureLevel.INSPECT,
                    clamped,
                    clamp(clamped * 0.40f, 26f, 44f),
                    clamp(clamped * 0.34f, 20f, 38f),
                    4f,
                    clamp(clamped * 0.072f, 7.25f, 8.5f),
                    clamp(clamped * 0.058f, 6.5f, 7.25f),
                    clamp(clamped * 0.090f, 10.0f, 12.0f),
                    clamp(clamped * 0.068f, 8.0f, 9.25f),
                    32,
                    18
            );
        }
        if (clamped >= WorkspaceTheme.READ_CELL_PX) {
            return new AtlasRenderBudget(
                    DisclosureLevel.READ,
                    clamped,
                    clamp(clamped * 0.70f, 14f, 32f),
                    clamp(clamped * 0.62f, 12f, 28f),
                    4f,
                    clamp(clamped * 0.066f, 6.75f, 7.75f),
                    0f,
                    clamp(clamped * 0.086f, 9.0f, 11.0f),
                    0f,
                    28,
                    0
            );
        }
        if (clamped >= WorkspaceTheme.BROWSE_CELL_PX) {
            return new AtlasRenderBudget(
                    DisclosureLevel.BROWSE,
                    clamped,
                    clamp(clamped - 4f, 22f, 46f),
                    clamp(clamped - 8f, 18f, 40f),
                    4f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0,
                    0
            );
        }
        return new AtlasRenderBudget(
                DisclosureLevel.REGION,
                clamped,
                clamp(clamped - 2f, 16f, 34f),
                clamp(clamped - 6f, 12f, 28f),
                3f,
                0f,
                0f,
                0f,
                0f,
                0,
                0
        );
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
