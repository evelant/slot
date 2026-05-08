package dev.imagio.slot.ui.workspace;

public final class WorkspaceCountFormat {
    private WorkspaceCountFormat() {
    }

    public static String compact(int count) {
        if (count <= 0) {
            return "";
        }
        if (count < 1_000) {
            return Integer.toString(count);
        }
        if (count < 10_000) {
            int tenths = count / 100;
            return tenths % 10 == 0
                    ? (tenths / 10) + "k"
                    : (tenths / 10) + "." + (tenths % 10) + "k";
        }
        if (count < 1_000_000) {
            return (count / 1_000) + "k";
        }
        return (count / 1_000_000) + "m";
    }
}
