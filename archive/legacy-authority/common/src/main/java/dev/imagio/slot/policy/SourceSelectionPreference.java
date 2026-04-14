package dev.imagio.slot.policy;

import dev.imagio.slot.source.SourceId;

public record SourceSelectionPreference(
        SourceId explicitSourceId,
        SourceId focusedSourceId,
        SourceId cursorCompatibleSourceId
) {
}
