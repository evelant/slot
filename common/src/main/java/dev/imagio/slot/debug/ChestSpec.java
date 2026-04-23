package dev.imagio.slot.debug;

import java.util.List;

public record ChestSpec(
        int index,
        String linkedIslandId,
        List<ChestContentEntry> contents,
        int deltaX,
        int deltaZ
) {
    public ChestSpec {
        contents = contents == null ? List.of() : List.copyOf(contents);
    }

    public boolean isLinked() {
        return linkedIslandId != null && !linkedIslandId.isBlank();
    }
}
