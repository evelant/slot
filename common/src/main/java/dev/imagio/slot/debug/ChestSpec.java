package dev.imagio.slot.debug;

import java.util.List;

public record ChestSpec(
        int index,
        String linkedIslandId,
        List<ChestContentEntry> contents,
        int deltaX,
        int deltaZ,
        String areaLabel
) {
    public ChestSpec {
        contents = contents == null ? List.of() : List.copyOf(contents);
        areaLabel = areaLabel == null ? "" : areaLabel.trim();
    }

    public ChestSpec(
            int index,
            String linkedIslandId,
            List<ChestContentEntry> contents,
            int deltaX,
            int deltaZ
    ) {
        this(index, linkedIslandId, contents, deltaX, deltaZ, "");
    }

    public boolean isLinked() {
        return linkedIslandId != null && !linkedIslandId.isBlank();
    }
}
