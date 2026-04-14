package dev.imagio.slot.client.screen;

public final class RailScrollState {
    private int offset;

    public int offset() {
        return offset;
    }

    public boolean scrollWheel(double scrollY, int stepSize, int maxScroll) {
        if (maxScroll <= 0) {
            offset = 0;
            return false;
        }
        offset = clamp(offset + (int) Math.round(-scrollY * stepSize), maxScroll);
        return true;
    }

    public void setOffset(int offset, int maxScroll) {
        this.offset = clamp(offset, maxScroll);
    }

    public void clamp(int maxScroll) {
        offset = clamp(offset, maxScroll);
    }

    public ScrollbarThumb scrollbar(int trackTop, int trackBottom, int viewportHeight, int contentHeight) {
        int maxScroll = maxScroll(contentHeight, viewportHeight);
        if (maxScroll <= 0) {
            return null;
        }

        int trackHeight = Math.max(1, trackBottom - trackTop);
        int thumbHeight = Math.max(14, Math.round((float) trackHeight * viewportHeight / Math.max(1, contentHeight)));
        int travel = Math.max(0, trackHeight - thumbHeight);
        int thumbTop = trackTop + Math.round((float) offset / Math.max(1, maxScroll) * travel);
        return new ScrollbarThumb(thumbTop, thumbHeight);
    }

    public static int maxScroll(int contentHeight, int viewportHeight) {
        return Math.max(0, contentHeight - viewportHeight);
    }

    private static int clamp(int offset, int maxScroll) {
        return Math.max(0, Math.min(offset, Math.max(0, maxScroll)));
    }

    public record ScrollbarThumb(int top, int height) {
    }
}
