package dev.imagio.slot.atlas.lod;

/**
 * LOD bands an atlas item can render at, ordered from least to most
 * detail. Picked by {@code BandPicker} from camera scale and per-item
 * relevance score; the chosen band drives both layout cell size (via
 * the weighted-grid packer) and render fidelity (via the platform-side
 * {@code AtlasRenderBudget}).
 */
public enum Band {
    PIP,
    REGION,
    BROWSE,
    READ,
    INSPECT,
    DETAIL
}
