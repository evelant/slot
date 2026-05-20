package dev.imagio.slot.workflow.domain;

/**
 * Feature gates for the experimental contextual suggestion system. The code
 * stays in place so we can revisit it, but live observation and row projection
 * stay off while the UI rows are hidden.
 */
public final class ContextualSuggestionFeatureFlags {
    public static final boolean ROWS_ENABLED = false;
    public static final boolean LIVE_OBSERVATION_ENABLED = false;

    private ContextualSuggestionFeatureFlags() {
    }
}
