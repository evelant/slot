package dev.imagio.slot.neoforge.screen.ldlib;

import net.minecraft.resources.ResourceLocation;

final class WorkspaceTheme {
    private WorkspaceTheme() {
    }

    static final ResourceLocation FONT_UI =
            ResourceLocation.fromNamespaceAndPath("slot", "slot_ui");
    static final int BACKGROUND = 0x96060A0E;
    static final int PANEL = 0xC8162029;
    static final int PANEL_ALT = 0xD01E2933;
    static final int GLASS = 0xCC0C141A;
    static final int ROW = 0xEC24313D;
    static final int ROW_DIM = 0x7C24313D;
    static final int ROW_HOVER = 0xEC334354;
    static final int ROW_MATCH = 0xED345749;
    static final int SELECTED = 0xF0507E6B;
    static final int ACTIVE_HOTBAR = 0xF0665B33;
    // Brightened amber for kit-card hover so the active card stays recognizable.
    static final int ACTIVE_HOTBAR_HOVER = 0xF08A7A4D;
    static final int ACTIVE_HOTBAR_PRESSED = 0xF0524830;
    // Subtle amber fill behind the active page row inside a kit card.
    static final int ACTIVE_PAGE_ROW = 0x40A08544;
    static final int TEXT = 0xFFE8EEF2;
    static final int MUTED = 0xFFA0AAB3;
    static final int ACCENT = 0xFF7AC7A7;
    static final int WARNING = 0xFFFFC66D;
    static final int CARRIED_CHIP_OK = 0xCC4A8B5E;
    static final int CARRIED_CHIP_WARN = 0xCCB48A3A;
    static final int CARRIED_CHIP_DANGER = 0xCCB44A3A;
    static final int CARRIED_CONTAINER_PIP = 0xCC5A7DB4;
    static final int COLLECTION = 0xFFBE8CFF;
    static final int ISLAND_BORDER = 0xA04F6578;
    static final int STORAGE_ZONE_FILL = 0x501A2430;
    static final int STORAGE_ZONE_HEADER_FILL = 0xC02E3A48;
    static final int STORAGE_ZONE_HEADER_HEIGHT = 16;
    static final int STORAGE_TILE_FILL = 0xD02E3A48;
    static final int STORAGE_TILE_FILL_DIM = 0x602E3A48;
    static final int STORAGE_TILE_CELL_FILL = 0x801A2430;
    static final int STORAGE_TILE_CELL_FILL_DIM = 0x401A2430;
    static final int LINK_THREAD_COLOR = 0xC07AC7A7;
    // Dimmed thread color used on island hover to preview links to non-proximate
    // chests without competing visually with the full-color proximate threads.
    static final int LINK_THREAD_DIM_COLOR = 0x407AC7A7;
    static final int LINK_HIGHLIGHT_COLOR = 0xA07AC7A7;
    static final int LINK_HIGHLIGHT_THICKNESS = 2;
    static final int HOVER_TRAIL_COLOR = 0xD0FFC66D;
    static final int HOVER_TRAIL_THICKNESS = 2;
    static final int HOVER_ACCENT_OVERLAY = 0x60FFC66D;
    static final int BROWSE_CELL_PX = 16;
    static final int READ_CELL_PX = 22;
    static final int INSPECT_CELL_PX = 44;
    static final int DETAIL_CELL_PX = 96;
    static final float CARRIED_FIT_MIN_SCALE = 0.20f;
    static final float CARRIED_FIT_MAX_SCALE = 2.50f;
    static final float CARRIED_FIT_READABILITY_MIN_SCALE = 1.00f;
    static final float CARRIED_FIT_PADDING_PX = 72f;
    static final int BELT_HEIGHT = 24;
    static final int BELT_SLOT_SIZE = 20;
    // Fixed-width holding area for the Kit toggle + page cycle button. Wide enough
    // for "Longname 3/3" (≈10 name chars + " N/M" + padding) plus the ">" cycle
    // button. Changes to the kit label grow LEFT inside this slot instead of
    // shoving the hotbar.
    static final int KIT_CLUSTER_WIDTH = 130;
    static final int BELT_DIVIDER_HEIGHT = 16;
    static final int TRIAGE_PANEL_WIDTH = 152;
    static final float NAV_CAPSULE_INSET_PX = 96f;
    static final float BELT_CAMERA_INSET_PX = 44f;
    static final float SIDE_CAMERA_INSET_PX = 48f;
    static final float GHOST_CARD_ALPHA = 0.18f;
    static final int GHOST_ICON_OVERLAY_COLOR = 0xC8060A0E;
    // Ghost (non-carried) atlas cards render at this fraction of their
    // allocated cell size at every disclosure level EXCEPT DETAIL. Keeps
    // the close-up view showing 1:1 "this is the home slot" while pushed-
    // out zooms de-emphasise homes the player doesn't currently hold.
    static final float GHOST_SHRINK_SCALE = 0.6f;
    static final int DRAG_START_THRESHOLD_PX = 4;

    static final int[] ISLAND_PALETTE = {
            0xCC7D5A3A, 0xCC5A6E3D, 0xCC6E3D3D, 0xCC3D5A6E,
            0xCC3D6E5A, 0xCC5A3D6E, 0xCC5A4A6E, 0xCC4E5A4A
    };
}
